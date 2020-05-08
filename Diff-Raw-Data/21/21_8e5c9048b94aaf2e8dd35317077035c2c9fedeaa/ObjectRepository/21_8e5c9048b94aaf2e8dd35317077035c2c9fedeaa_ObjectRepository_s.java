 /*
  * Copyright (c) 2007-2009, James Leigh All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution. 
  * - Neither the name of the openrdf.org nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  * 
  */
 package org.openrdf.repository.object;
 
 import static org.openrdf.query.QueryLanguage.SPARQL;
 import info.aduna.io.FileUtil;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.Namespace;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.query.GraphQuery;
 import org.openrdf.query.GraphQueryResult;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.impl.DatasetImpl;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.openrdf.repository.contextaware.ContextAwareConnection;
 import org.openrdf.repository.contextaware.ContextAwareRepository;
 import org.openrdf.repository.object.annotations.triggeredBy;
 import org.openrdf.repository.object.compiler.OWLCompiler;
 import org.openrdf.repository.object.compiler.OntologyLoader;
 import org.openrdf.repository.object.composition.ClassFactory;
 import org.openrdf.repository.object.composition.ClassResolver;
 import org.openrdf.repository.object.config.ObjectRepositoryFactory;
 import org.openrdf.repository.object.exceptions.ObjectStoreConfigException;
 import org.openrdf.repository.object.managers.LiteralManager;
 import org.openrdf.repository.object.managers.PropertyMapper;
 import org.openrdf.repository.object.managers.RoleMapper;
 import org.openrdf.repository.object.managers.TypeManager;
 import org.openrdf.repository.object.managers.helpers.RoleClassLoader;
 import org.openrdf.repository.object.trigger.Trigger;
 import org.openrdf.repository.object.trigger.TriggerConnection;
 import org.openrdf.repository.object.vocabulary.OBJ;
 import org.openrdf.rio.RDFParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Creates the {@link ObjectConnection} used to interact with the repository.
  * Use {@link ObjectRepositoryFactory} to create this.
  * 
  * @author James Leigh
  * 
  */
 public class ObjectRepository extends ContextAwareRepository {
 	private static final URI[] LIST_PROPERTIES = new URI[] { RDF.REST,
 			OWL.DISTINCTMEMBERS, OWL.UNIONOF, OWL.INTERSECTIONOF, OWL.ONEOF };
 	private static final String PREFIX = "PREFIX obj:<" + OBJ.NAMESPACE + ">\n"
 			+ "PREFIX owl:<" + OWL.NAMESPACE + ">\n" + "PREFIX rdfs:<"
 			+ RDFS.NAMESPACE + ">\n" + "PREFIX rdf:<" + RDF.NAMESPACE + ">\n";
 	private static final String CONSTRUCT_SCHEMA = PREFIX
 			+ "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o { ?s a rdfs:Datatype } UNION "
 			+ "{ ?s a owl:Class } UNION { ?s a rdfs:Class } UNION "
 			+ "{ ?s a owl:DeprecatedClass } UNION { ?s a owl:DeprecatedProperty } UNION "
 			+ "{ ?s a owl:DatatypeProperty } UNION { ?s a owl:ObjectProperty } UNION "
 			+ "{ ?s a owl:Restriction } UNION { ?s a owl:Ontology } UNION "
 			+ "{ ?s a rdf:Property } UNION { ?s a owl:FunctionalProperty } UNION "
 			+ "{ ?s owl:complementOf [] } UNION { ?s owl:intersectionOf [] } UNION "
 			+ "{ ?s owl:oneOf []} UNION { ?s owl:unionOf [] } UNION "
 			+ "{ ?s owl:equivalentClass []} UNION { ?s owl:equivalentProperty []} UNION "
 			+ "{ ?s rdfs:domain [] } UNION { ?s rdfs:range [] } UNION "
 			+ "{ ?s rdfs:subClassOf [] } UNION { ?s rdfs:subPropertyOf [] } UNION "
 			+ "{ ?s owl:onProperty [] } UNION { ?s obj:matches ?lit } }";
 	private static final Pattern PATTERN = Pattern.compile("composed(\\d+)",
 			Pattern.CASE_INSENSITIVE);
 	private static final Collection<File> temporary = new ArrayList<File>();
 
 	private static void deleteOnExit(File dir) {
 		synchronized (temporary) {
 			if (temporary.isEmpty()) {
 				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
 					public void run() {
 						synchronized (temporary) {
 							for (File dir : temporary) {
 								try {
 									if (dir.isDirectory()) {
 										FileUtil.deleteDir(dir);
 									} else {
 										dir.delete();
 									}
 								} catch (IOException e) {
 									// ignore
 								}
 							}
 						}
 					}
 				}, "ObjectRepository.cleanup"));
 			}
 			temporary.add(dir);
 		}
 	}
 
 	private Logger logger = LoggerFactory.getLogger(ObjectRepository.class);
 	private boolean initialized;
 	private ClassLoader baseClassLoader;
 	private RoleMapper baseRoleMapper;
 	private LiteralManager baseLiteralManager;
 	private String pkgPrefix = "";
 	private String propertyPrefix;
 	private boolean followImports;
 	private List<URL> imports = Collections.emptyList();
 	private URL[] cp;
 	private File libDir;
 	private volatile int revision;
 	private ClassLoader cl;
 	private RoleMapper mapper;
 	private LiteralManager literals;
 	private PropertyMapper pm;
 	private ClassResolver resolver;
 	private Map<URI, Set<Trigger>> triggers;
 	private Set<ObjectConnection> compileAfter;
 	private List<Runnable> schemaListeners = new ArrayList<Runnable>();
 	private DatasetImpl schemaDataset;
 	private long schemaHash;
 
 	public ObjectRepository(RoleMapper mapper, LiteralManager literals,
 			ClassLoader cl) {
 		this.baseClassLoader = cl;
 		this.baseRoleMapper = mapper;
 		this.baseLiteralManager = literals;
 	}
 
 	/**
 	 * Assigns a name prefix prepended to the beginning of all compiled
 	 * packages.
 	 * 
 	 * @param prefix
 	 *            such as "compiled."
 	 */
 	public void setPackagePrefix(String prefix) {
 		this.pkgPrefix = prefix;
 	}
 
 	/**
 	 * Assigns the member prefix to use when compiling classes.
 	 */
 	public void setMemberPrefix(String prefix) {
 		this.propertyPrefix = prefix;
 	}
 
 	/**
 	 * Additional classpath that should be included after the model is compiled.
 	 */
 	public void setBehaviourClassPath(URL[] cp) {
 		this.cp = cp;
 	}
 
 	public void setFollowImports(boolean followImports) {
 		this.followImports = followImports;
 	}
 
 	public void setOWLImports(List<URL> imports) {
 		this.imports = imports;
 	}
 
 	public Set<URI> getSchemaDataset() {
 		if (schemaDataset == null)
 			return null;
 		return schemaDataset.getDefaultGraphs();
 	}
 
 	public void addSchemaDataset(URI graphURI) throws RepositoryException {
 		if (schemaDataset != null
 				&& schemaDataset.getDefaultGraphs().contains(graphURI))
 			return;
 		boolean changed = false;
 		synchronized (this) {
 			if (schemaDataset == null) {
 				schemaDataset = new DatasetImpl();
 			}
 			schemaDataset.addDefaultGraph(graphURI);
 			if (isCompileRepository() && compileAfter.isEmpty()) {
 				changed = recompile();
 			}
 		}
 		if (changed) {
 			for (Runnable action : schemaListeners) {
 				action.run();
 			}
 		}
 	}
 
 	public void resetSchemaDataset() throws RepositoryException {
 		if (schemaDataset == null)
 			return;
 		boolean changed = false;
 		synchronized (this) {
 			schemaDataset = null;
 			if (isCompileRepository() && compileAfter.isEmpty()) {
 				changed = recompile();
 			}
 		}
 		if (changed) {
 			for (Runnable action : schemaListeners) {
 				action.run();
 			}
 		}
 	}
 
 	public boolean addSchemaListener(Runnable action) {
 		return schemaListeners.add(action);
 	}
 
 	public boolean removeSchemaListener(Runnable action) {
 		return schemaListeners.remove(action);
 	}
 
 	@Override
 	public void initialize() throws RepositoryException {
 		super.initialize();
 		try {
 			init(getDataDir());
 		} catch (ObjectStoreConfigException e) {
 			throw new RepositoryException(e);
 		}
 	}
 
 	public ValueFactory getURIFactory() {
 		return super.getValueFactory();
 	}
 
 	public ValueFactory getLiteralFactory() {
 		return super.getValueFactory();
 	}
 
 	public boolean isCompileRepository() {
 		return compileAfter != null;
 	}
 
 	public synchronized void setCompileRepository(boolean compileRepository)
 			throws ObjectStoreConfigException, RepositoryException,
 			AssertionError {
 		if (compileRepository && compileAfter == null) {
 			compileAfter = new HashSet<ObjectConnection>();
 			try {
 				if (initialized) {
 					Model schema = new LinkedHashModel();
 					loadSchema(schema);
 					compile(schema);
 					schemaHash = hash(schema);
 					System.gc();
 				}
 			} catch (RDFParseException e) {
 				throw new ObjectStoreConfigException(e);
 			} catch (IOException e) {
 				throw new ObjectStoreConfigException(e);
 			}
 		} else if (!compileRepository && compileAfter != null) {
 			compileAfter = null;
 		}
 	}
 
 	/**
 	 * Called by {@link ObjectRepositoryFactory} when the delegate repository
 	 * has already been initialized.
 	 */
 	public synchronized void init(File dataDir) throws RepositoryException,
 			ObjectStoreConfigException {
 		initialized = true;
 		if (dataDir == null) {
 			try {
 				libDir = createTempDir("lib");
 			} catch (IOException e) {
 				throw new RepositoryException(e);
 			}
 		} else {
 			libDir = new File(dataDir, "lib");
 			libDir.mkdirs();
 		}
 		deleteOnExit(libDir);
 		Model schema = new LinkedHashModel();
 		try {
 			if (isCompileRepository()) {
 				loadSchema(schema);
 			}
 			compile(schema);
 			schemaHash = hash(schema);
 			System.gc();
 		} catch (RDFParseException e) {
 			throw new ObjectStoreConfigException(e);
 		} catch (IOException e) {
 			throw new ObjectStoreConfigException(e);
 		}
 	}
 
 	/**
 	 * Creates a new ObjectConnection that will need to be closed by the caller.
 	 */
 	@Override
 	public synchronized ObjectConnection getConnection()
 			throws RepositoryException {
 		ObjectConnection con;
 		TriggerConnection tc = null;
 		RepositoryConnection conn = getDelegate().getConnection();
 		ObjectFactory factory = createObjectFactory(mapper, pm, literals,
 				resolver, cl);
 		if (triggers != null) {
 			conn = tc = new TriggerConnection(conn, triggers);
 		}
 		con = new ObjectConnection(this, conn, factory, createTypeManager());
 		if (tc != null) {
 			tc.setObjectConnection(con);
 		}
 		con.setIncludeInferred(isIncludeInferred());
 		con.setMaxQueryTime(getMaxQueryTime());
 		// con.setQueryResultLimit(getQueryResultLimit());
 		con.setQueryLanguage(getQueryLanguage());
 		con.setReadContexts(getReadContexts());
 		con.setAddContexts(getAddContexts());
 		con.setRemoveContexts(getRemoveContexts());
 		con.setArchiveContexts(getArchiveContexts());
 		return con;
 	}
 
 	protected synchronized void compileAfter(ObjectConnection con) {
 		if (isCompileRepository()) {
 			compileAfter.add(con);
 		}
 	}
 
 	protected void closed(ObjectConnection con) throws RepositoryException {
 		boolean changed = false;
 		synchronized (this) {
 			if (isCompileRepository()
 					&& (con == null || compileAfter.remove(con))
 					&& compileAfter.isEmpty()) {
 				changed = recompile();
 			}
 		}
 		if (changed) {
 			for (Runnable action : schemaListeners) {
 				action.run();
 			}
 		}
 	}
 
 	protected TypeManager createTypeManager() {
 		return new TypeManager(mapper.isNamedTypePresent());
 	}
 
 	protected ObjectFactory createObjectFactory(RoleMapper mapper,
 			PropertyMapper pm, LiteralManager literalManager,
 			ClassResolver resolver, ClassLoader cl) {
 		return new ObjectFactory(mapper, pm, literalManager, resolver, cl);
 	}
 
 	protected ClassResolver createClassResolver(ClassFactory definer,
 			RoleMapper mapper, PropertyMapper pm) {
 		ClassResolver resolver = new ClassResolver();
 		resolver.setPropertyMapper(pm);
 		resolver.setRoleMapper(mapper);
 		resolver.setClassDefiner(definer);
 		resolver.setBaseClassRoles(mapper.getConceptClasses());
 		resolver.init();
 		return resolver;
 	}
 
 	protected PropertyMapper createPropertyMapper(ClassLoader cl) {
 		return new PropertyMapper(cl, mapper.isNamedTypePresent());
 	}
 
 	protected ClassFactory createClassFactory(File composed, ClassLoader cl) {
 		return new ClassFactory(composed, cl);
 	}
 
 	private boolean recompile() throws RepositoryException,
 			AssertionError {
 		Model schema = new LinkedHashModel();
 		loadSchema(schema);
 		try {
 			long hash = hash(schema);
 			if (schemaHash != hash) {
 				compileSchema(schema);
 				schemaHash = hash;
 				System.gc();
 				return true;
 			}
 		} catch (ObjectStoreConfigException e) {
 			throw new RepositoryException(e);
 		} catch (RDFParseException e) {
 			throw new RepositoryException(e);
 		} catch (IOException e) {
 			throw new RepositoryException(e);
 		}
 		return false;
 	}
 
 	private File createTempDir(String name) throws IOException {
 		String tmpDirStr = System.getProperty("java.io.tmpdir");
 		if (tmpDirStr != null) {
 			File tmpDir = new File(tmpDirStr);
 			if (!tmpDir.exists()) {
 				tmpDir.mkdirs();
 			}
 		}
 		File tmp = File.createTempFile(name, "");
 		tmp.delete();
 		tmp.mkdir();
 		return tmp;
 	}
 
 	private long hash(Model schema) {
 		long hash = 0;
 		for (Statement st : schema) {
 			Resource subj = st.getSubject();
 			URI pred = st.getPredicate();
 			Value obj = st.getObject();
 			boolean relevant = OWL.NAMESPACE.equals(pred.getNamespace());
 			relevant |= RDFS.NAMESPACE.equals(pred.getNamespace());
 			relevant |= RDF.NAMESPACE.equals(pred.getNamespace());
 			relevant |= OBJ.NAMESPACE.equals(pred.getNamespace());
 			final URI ANN = OWL.ANNOTATIONPROPERTY;
 			if (relevant || mapper.isRecordedAnnotation(pred)
 					|| schema.contains(pred, RDF.TYPE, ANN)) {
 				hash += 31 * pred.hashCode();
 				if (subj instanceof URI) {
 					hash += 961 * subj.hashCode();
 				}
 				if (obj instanceof URI) {
 					hash += obj.hashCode();
 				}
 				if (obj instanceof Literal) {
 					hash += obj.hashCode();
 				}
 			}
 		}
 		return hash;
 	}
 
 	private void compile(Model schema) throws RepositoryException,
 			ObjectStoreConfigException, RDFParseException, IOException {
 		try {
 			compileSchema(schema);
 		} catch (ObjectStoreConfigException e) {
 			try {
 				logger.error(e.toString(), e);
 				if (schema == null || schema.isEmpty())
 					throw e;
 				compileSchema(new LinkedHashModel());
 			} catch (ObjectStoreConfigException e2) {
 				throw e;
 			} catch (RepositoryException e2) {
 				throw e;
 			} catch (RDFParseException e2) {
 				throw e;
 			} catch (IOException e2) {
 				throw e;
 			}
 		} catch (RepositoryException e) {
 			try {
 				logger.error(e.toString(), e);
 				if (schema == null || schema.isEmpty())
 					throw e;
 				compileSchema(new LinkedHashModel());
 			} catch (ObjectStoreConfigException e2) {
 				throw e;
 			} catch (RepositoryException e2) {
 				throw e;
 			} catch (RDFParseException e2) {
 				throw e;
 			} catch (IOException e2) {
 				throw e;
 			}
 		} catch (RDFParseException e) {
 			try {
 				logger.error(e.toString(), e);
 				if (schema == null || schema.isEmpty())
 					throw e;
 				compileSchema(new LinkedHashModel());
 			} catch (ObjectStoreConfigException e2) {
 				throw e;
 			} catch (RepositoryException e2) {
 				throw e;
 			} catch (RDFParseException e2) {
 				throw e;
 			} catch (IOException e2) {
 				throw e;
 			}
 		} catch (IOException e) {
 			try {
 				logger.error(e.toString(), e);
 				if (schema == null || schema.isEmpty())
 					throw e;
 				compileSchema(new LinkedHashModel());
 			} catch (ObjectStoreConfigException e2) {
 				throw e;
 			} catch (RepositoryException e2) {
 				throw e;
 			} catch (RDFParseException e2) {
 				throw e;
 			} catch (IOException e2) {
 				throw e;
 			}
 		}
 	}
 
 	private void compileSchema(Model schema) throws RepositoryException,
 			ObjectStoreConfigException, RDFParseException, IOException {
 		incrementRevision();
 		ClassLoader cl = baseClassLoader;
 		RoleMapper mapper = baseRoleMapper.clone();
 		LiteralManager literals = baseLiteralManager.clone();
 		File concepts = new File(libDir, "concepts" + revision + ".jar");
 		File behaviours = new File(libDir, "behaviours" + revision + ".jar");
 		final File composed = new File(libDir, "composed" + revision);
 		OntologyLoader ontologies = new OntologyLoader(schema);
 		ontologies.loadOntologies(imports);
 		if (followImports) {
 			ontologies.followImports();
 		}
 		Map<URI, Map<String, String>> namespaces;
 		namespaces = getNamespaces(ontologies.getNamespaces());
 		if (schema != null && !schema.isEmpty()) {
 			OWLCompiler compiler = new OWLCompiler(mapper, literals);
 			compiler.setModel(schema);
 			compiler.setPackagePrefix(pkgPrefix);
 			compiler.setMemberPrefix(propertyPrefix);
 			compiler.setPrefixNamespaces(namespaces);
 			compiler.setClassLoader(cl);
 			cl = compiler.createConceptJar(concepts);
 			compiler.setClassLoader(cl);
 			cl = compiler.createBehaviourJar(behaviours);
 			RoleClassLoader loader = new RoleClassLoader(mapper);
 			loader.loadRoles(cl);
 			literals.setClassLoader(cl);
 		}
 		if (isCompileRepository()) {
 			mapper.addBehaviour(CompileTrigger.class, RDFS.RESOURCE);
 		}
 		if (cp != null && cp.length > 0) {
 			cl = new URLClassLoader(cp, cl);
 			RoleClassLoader loader = new RoleClassLoader(mapper);
 			loader.loadRoles(cl);
 			for (URL url : cp) {
 				loader.scan(url, cl);
 			}
 			literals.setClassLoader(cl);
 		}
 		ClassFactory definer = createClassFactory(composed, cl);
 		if (composed.exists()) {
 			deleteOnExit(composed);
 		}
 		if (behaviours.exists()) {
 			behaviours.deleteOnExit();
 		}
 		if (concepts.exists()) {
 			concepts.deleteOnExit();
 		}
 		cl = definer;
 		this.cl = cl;
 		this.mapper = mapper;
 		this.literals = literals;
 		pm = createPropertyMapper(definer);
 		resolver = createClassResolver(definer, mapper, pm);
 		Collection<Method> methods = mapper.getTriggerMethods();
 		if (methods.isEmpty()) {
 			triggers = null;
 		} else {
 			triggers = new HashMap<URI, Set<Trigger>>(methods.size());
 			for (Method method : methods) {
 				for (String uri : method.getAnnotation(triggeredBy.class)
 						.value()) {
 					URI key = getURIFactory().createURI(uri);
 					Set<Trigger> set = triggers.get(key);
 					if (set == null) {
 						triggers.put(key, set = new HashSet<Trigger>());
 					}
 					set.add(new Trigger(method));
 				}
 			}
 		}
 	}
 
 	private void incrementRevision() {
 		File[] listFiles = libDir.listFiles();
 		if (listFiles != null) {
 			for (File file : listFiles) {
 				Matcher m = PATTERN.matcher(file.getName());
 				if (m.matches()) {
 					int version = Integer.parseInt(m.group(1));
 					if (revision < version) {
 						revision = version;
 					}
 				}
 			}
 		}
 		revision++;
 	}
 
 	private Map<URI, Map<String, String>> getNamespaces(
 			Map<URI, Map<String, String>> namespaces)
 			throws RepositoryException {
 		if (!isCompileRepository())
 			return namespaces;
 		ContextAwareConnection con = super.getConnection();
 		try {
 			Map<String, String> map = new HashMap<String, String>();
 			RepositoryResult<Namespace> ns = con.getNamespaces();
 			try {
 				while (ns.hasNext()) {
 					Namespace n = ns.next();
 					map.put(n.getPrefix(), n.getName());
 				}
 			} finally {
 				ns.close();
 			}
 			namespaces.put(null, map);
 		} finally {
 			con.close();
 		}
 		return namespaces;
 	}
 
 	private void loadSchema(Model schema) throws RepositoryException,
 			AssertionError {
 		RepositoryConnection conn = getDelegate().getConnection();
 		try {
 			GraphQuery query = conn.prepareGraphQuery(SPARQL, CONSTRUCT_SCHEMA);
 			query.setDataset(schemaDataset);
 			GraphQueryResult result = query.evaluate();
 			try {
 				while (result.hasNext()) {
 					schema.add(result.next());
 				}
 			} finally {
 				result.close();
 			}
 			addLists(conn, schema);
 		} catch (MalformedQueryException e) {
 			throw new AssertionError(e);
 		} catch (QueryEvaluationException e) {
 			throw new RepositoryException(e);
 		} finally {
 			conn.close();
 		}
 	}
 
 	private void addLists(RepositoryConnection conn, Model schema)
 			throws RepositoryException {
 		boolean modified = false;
 		List<Value> lists = new ArrayList<Value>();
 		for (URI pred : LIST_PROPERTIES) {
 			lists.addAll(schema.filter(null, pred, null).objects());
 		}
 		for (Value list : lists) {
 			if (list instanceof Resource
 					&& !schema.contains((Resource) list, null, null)) {
 				RepositoryResult<Statement> stmts = conn.getStatements(
 						(Resource) list, null, null, true);
 				try {
 					while (stmts.hasNext()) {
 						schema.add(stmts.next());
 						modified = true;
 					}
 				} finally {
 					stmts.close();
 				}
 			}
 		}
 		if (modified) {
 			addLists(conn, schema);
 		}
 	}
 
 	public abstract static class CompileTrigger implements RDFObject {
 		private static final String OBJNS = OBJ.NAMESPACE;
 		private static final String RDFSNS = RDFS.NAMESPACE;
 		private static final String OWLNS = OWL.NAMESPACE;
 
 		@triggeredBy( { OWLNS + "imports", OWLNS + "complementOf",
 				OWLNS + "intersectionOf", OWLNS + "oneOf",
 				OWLNS + "onProperty", OWLNS + "unionOf", RDFSNS + "domain",
 				RDFSNS + "range", RDFSNS + "subClassOf",
 				RDFSNS + "subPropertyOf", OBJNS + "matches" })
 		public void schemaChanged() {
 			ObjectConnection con = getObjectConnection();
 			con.getRepository().compileAfter(con);
 		}
 	}
 }
