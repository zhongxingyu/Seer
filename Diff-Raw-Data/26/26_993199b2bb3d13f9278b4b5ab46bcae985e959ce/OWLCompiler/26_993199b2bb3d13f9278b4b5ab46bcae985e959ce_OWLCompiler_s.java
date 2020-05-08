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
 package org.openrdf.repository.object.compiler;
 
 import info.aduna.io.FileUtil;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.openrdf.model.Literal;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.URI;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.impl.URIImpl;
 import org.openrdf.model.impl.ValueFactoryImpl;
 import org.openrdf.model.vocabulary.OWL;
 import org.openrdf.model.vocabulary.RDF;
 import org.openrdf.model.vocabulary.RDFS;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.annotations.iri;
 import org.openrdf.repository.object.compiler.model.RDFClass;
 import org.openrdf.repository.object.compiler.model.RDFOntology;
 import org.openrdf.repository.object.compiler.model.RDFProperty;
 import org.openrdf.repository.object.compiler.source.JavaCompiler;
 import org.openrdf.repository.object.exceptions.ObjectCompileException;
 import org.openrdf.repository.object.exceptions.ObjectStoreConfigException;
 import org.openrdf.repository.object.managers.LiteralManager;
 import org.openrdf.repository.object.managers.RoleMapper;
 import org.openrdf.repository.object.vocabulary.OBJ;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Converts OWL ontologies into Java source code.
  * 
  * @author James Leigh
  * 
  */
 public class OWLCompiler {
 
 	private class AnnotationBuilder implements Runnable {
 		private final RDFProperty bean;
 		private final List<String> content;
 		private final File target;
 
 		private AnnotationBuilder(File target, List<String> content,
 				RDFProperty bean) {
 			this.target = target;
 			this.content = content;
 			this.bean = bean;
 		}
 
 		public void run() {
 			try {
 				bean.generateAnnotationCode(target, resolver);
 				URI uri = bean.getURI();
 				String pkg = resolver.getPackageName(uri);
 				String className = resolver.getSimpleName(uri);
 				if (pkg != null) {
 					className = pkg + '.' + className;
 				}
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 					annotations.add(className);
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", bean);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private class ConceptBuilder implements Runnable {
 		private final RDFClass bean;
 		private final List<String> content;
 		private final File target;
 
 		private ConceptBuilder(File target, List<String> content, RDFClass bean) {
 			this.target = target;
 			this.content = content;
 			this.bean = bean;
 		}
 
 		public void run() {
 			try {
 				bean.generateSourceCode(target, resolver);
 				URI uri = bean.getURI();
 				String pkg = resolver.getPackageName(uri);
 				String className = resolver.getSimpleName(uri);
 				if (pkg != null) {
 					className = pkg + '.' + className;
 				}
 				boolean anon = resolver.isAnonymous(uri) && bean.isEmpty(resolver);
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 					if (!anon) {
 						concepts.add(className);
 					}
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", bean);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private final class DatatypeBuilder implements Runnable {
 		private final RDFClass bean;
 		private final List<String> content;
 		private final File target;
 
 		private DatatypeBuilder(List<String> content, RDFClass bean, File target) {
 			this.content = content;
 			this.bean = bean;
 			this.target = target;
 		}
 
 		public void run() {
 			try {
 				bean.generateSourceCode(target, resolver);
 				String pkg = resolver.getPackageName(bean.getURI());
 				String className = resolver.getSimpleName(bean.getURI());
 				if (pkg != null) {
 					className = pkg + '.' + className;
 				}
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 					datatypes.add(className);
 				}
 			} catch (Exception exc) {
 				logger.error("Error processing {}", bean);
 				if (exception == null) {
 					exception = exc;
 				}
 			}
 		}
 	}
 
 	private static final String JAVA_NS = "java:";
 
 	Runnable helper = new Runnable() {
 		public void run() {
 			try {
 				for (Runnable r = queue.take(); r != helper; r = queue.take()) {
 					r.run();
 				}
 			} catch (InterruptedException e) {
 				logger.error(e.toString(), e);
 			}
 		}
 	};
 	final Logger logger = LoggerFactory.getLogger(OWLCompiler.class);
 	BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
 	private String[] baseClasses = new String[0];
 	private File behaviours;
 	private Set<String> annotations = new TreeSet<String>();
 	private Set<String> concepts = new TreeSet<String>();
 	private File conceptsJar;
 	private Set<String> datatypes = new TreeSet<String>();
 	private Exception exception;
 	private LiteralManager literals;
 	private RoleMapper mapper;
 	private String memberPrefix;
 	private Model model;
 	/** namespace -&gt; package */
 	private Map<String, String> packages = new HashMap<String, String>();
 	/** context -&gt; prefix -&gt; namespace */
 	private Map<URI, Map<String, String>> namespaces = new HashMap<URI, Map<String, String>>();
 	private String pkgPrefix = "";
 	private JavaNameResolver resolver;
 	private Collection<URL> ontologies;
 
 	public OWLCompiler(RoleMapper mapper, LiteralManager literals) {
 		this.mapper = mapper;
 		this.literals = literals;
 	}
 
 	public void setBaseClasses(String[] baseClasses) {
 		this.baseClasses = baseClasses;
 	}
 
 	public void setBehaviourJar(File jar) {
 		this.behaviours = jar;
 	}
 
 	public void setConceptJar(File jar) {
 		this.conceptsJar = jar;
 	}
 
 	public void setPackagePrefix(String prefix) {
 		if (prefix == null) {
 			this.pkgPrefix = "";
 		} else {
 			this.pkgPrefix = prefix;
 		}
 	}
 
 	public void setMemberPrefix(String prefix) {
 		this.memberPrefix = prefix;
 	}
 
 	public void setOntologies(Collection<URL> ontologies) {
 		this.ontologies = ontologies;
 	}
 
 	/**
 	 * 
 	 * @param namespaces
 	 *            graph -&gt; prefix -&gt; namespace
 	 * @param model
 	 * @param cl
 	 * @return
 	 * @throws RepositoryException
 	 * @throws ObjectStoreConfigException
 	 */
 	public synchronized ClassLoader compile(
 			Map<URI, Map<String, String>> namespaces, Model model,
 			ClassLoader cl) throws RepositoryException,
 			ObjectStoreConfigException {
 		this.annotations.clear();
 		this.concepts.clear();
 		this.datatypes.clear();
 		this.exception = null;
 		this.packages.clear();
 		this.model = model;
 		this.namespaces = namespaces;
 		OwlNormalizer normalizer = new OwlNormalizer(new RDFDataSource(model));
 		normalizer.normalize();
 		Set<String> unknown = findUndefinedNamespaces(model, cl);
 		if (unknown.isEmpty())
 			return cl;
 		populateJavaNames();
		resolver = createJavaNameResolver(cl, mapper, literals);
 		for (URI uri : normalizer.getAnonymousClasses()) {
 			resolver.assignAnonymous(uri);
 		}
 		for (Map.Entry<URI, URI> e : normalizer.getAliases().entrySet()) {
 			resolver.assignAlias(e.getKey(), e.getValue());
 		}
 		resolver.setImplNames(normalizer.getImplNames());
		for (String ns : unknown) {
			String prefix = findPrefix(ns, model);
			String pkgName = pkgPrefix + prefix;
			if (!Character.isLetter(pkgName.charAt(0))) {
				pkgName = "_" + pkgName;
			}
			packages.put(ns, pkgName);
			resolver.bindPackageToNamespace(pkgName, ns);
 		}
 		try {
 			cl = compileConcepts(conceptsJar, cl);
 			return compileBehaviours(behaviours, cl);
 		} catch (ObjectStoreConfigException e) {
 			throw e;
 		} catch (RepositoryException e) {
 			throw e;
 		} catch (Exception e) {
 			throw new RepositoryException(e);
 		}
 	}
 
 	private void populateJavaNames() {
 		ValueFactory vf = ValueFactoryImpl.getInstance();
 		for (Class<?> role : mapper.findAllRoles()) {
 			for (Method m : role.getDeclaredMethods()) {
 				if (m.isAnnotationPresent(iri.class) && !isProperty(m)) {
 					String uri = m.getAnnotation(iri.class).value();
 					URI subj = vf.createURI(uri);
 					if (!model.contains(subj, OBJ.NAME, null)) {
 						Literal obj = vf.createLiteral(m.getName());
 						model.add(subj, OBJ.NAME, obj);
 					}
 				}
 			}
 		}
 	}
 
 	private boolean isProperty(Method m) {
 		String name = m.getName();
 		int argc = m.getParameterTypes().length;
 		if (name.startsWith("set") && argc == 1)
 			return true;
 		if (name.startsWith("get") && argc == 0)
 			return true;
 		if (name.startsWith("is") && argc == 0
 				&& m.getReturnType().equals(Boolean.TYPE))
 			return true;
 		return false;
 	}
 
 	private void addBaseClass(RDFClass klass) {
 		if (klass.getRDFClasses(RDFS.SUBCLASSOF).isEmpty()) {
 			for (String b : baseClasses) {
 				URI name = new URIImpl(JAVA_NS + b);
 				model.add(klass.getURI(), RDFS.SUBCLASSOF, name);
 			}
 		}
 	}
 
 	private File asLocalFile(URL rdf) throws UnsupportedEncodingException {
 		return new File(URLDecoder.decode(rdf.getFile(), "UTF-8"));
 	}
 
 	private List<String> buildConcepts(final File target, ClassLoader cl) throws Exception {
 		if (baseClasses.length > 0) {
 			Set<Resource> classes = model.filter(null, RDF.TYPE, OWL.CLASS)
 					.subjects();
 			for (Resource o : new ArrayList<Resource>(classes)) {
 				RDFClass bean = new RDFClass(model, o);
 				if (bean.getURI() == null)
 					continue;
 				if (bean.isDatatype())
 					continue;
 				if (mapper.isRecordedConcept(bean.getURI(), cl))
 					continue;
 				addBaseClass(bean);
 			}
 		}
 		List<Thread> threads = new ArrayList<Thread>();
 		for (int i = 0; i < 3; i++) {
 			threads.add(new Thread(helper));
 		}
 		for (Thread thread : threads) {
 			thread.start();
 		}
 		Set<String> usedNamespaces = new HashSet<String>(packages.size());
 		List<String> content = new ArrayList<String>();
 		for (Resource o : model.filter(null, RDF.TYPE, OWL.ANNOTATIONPROPERTY)
 				.subjects()) {
 			RDFProperty bean = new RDFProperty(model, o);
 			if (bean.getURI() == null)
 				continue;
 			if (mapper.isRecordedAnnotation(bean.getURI()))
 				continue;
 			String namespace = bean.getURI().getNamespace();
 			usedNamespaces.add(namespace);
 			queue.add(new AnnotationBuilder(target, content, bean));
 		}
 		for (Resource o : model.filter(null, RDF.TYPE, OWL.CLASS).subjects()) {
 			RDFClass bean = new RDFClass(model, o);
 			if (bean.getURI() == null)
 				continue;
 			if (bean.isDatatype())
 				continue;
 			if (mapper.isRecordedConcept(bean.getURI(), cl))
 				continue;
 			String namespace = bean.getURI().getNamespace();
 			usedNamespaces.add(namespace);
 			queue.add(new ConceptBuilder(target, content, bean));
 		}
 		for (Resource o : model.filter(null, RDF.TYPE, RDFS.DATATYPE)
 				.subjects()) {
 			RDFClass bean = new RDFClass(model, o);
 			if (bean.getURI() == null)
 				continue;
 			if (literals.isRecordedeType(bean.getURI()))
 				continue;
 			String namespace = bean.getURI().getNamespace();
 			usedNamespaces.add(namespace);
 			queue.add(new DatatypeBuilder(content, bean, target));
 		}
 		for (int i = 0, n = threads.size(); i < n; i++) {
 			queue.add(helper);
 		}
 		for (String namespace : usedNamespaces) {
 			if (JAVA_NS.equals(namespace))
 				continue;
 			RDFOntology ont = findOntology(namespace);
 			ont.generatePackageInfo(target, namespace, resolver);
 			String pkg = getPackageName(namespace);
 			if (pkg != null) {
 				String className = pkg + ".package-info";
 				synchronized (content) {
 					logger.debug("Saving {}", className);
 					content.add(className);
 				}
 			}
 		}
 		for (Thread thread1 : threads) {
 			thread1.join();
 		}
 		if (exception != null)
 			throw exception;
 		if (content.isEmpty())
 			throw new IllegalArgumentException(
 					"No classes found - Try a different namespace.");
 		return content;
 	}
 
 	private String getPackageName(String namespace) {
 		return packages.get(namespace);
 	}
 
 	private ClassLoader compileBehaviours(File jar, ClassLoader cl)
 			throws Exception, IOException {
 		File target = FileUtil.createTempDir(getClass().getSimpleName());
 		List<File> classpath = getClassPath(cl);
 		classpath.add(target);
 		List<String> methods = compileMethods(target, classpath, resolver);
 		if (methods.isEmpty()) {
 			FileUtil.deleteDir(target);
 			return cl;
 		}
 		JarPacker packer = new JarPacker(target);
 		packer.setBehaviours(methods);
 		packer.packageJar(jar);
 		FileUtil.deleteDir(target);
 		return new URLClassLoader(new URL[] { jar.toURI().toURL() }, cl);
 	}
 
 	/**
 	 * Generate concept Java classes from the ontology in the local repository.
 	 * 
 	 * @param jar
 	 * @see {@link #addOntology(URI, String)}
 	 * @see {@link #addImports(URL)}
 	 */
 	private ClassLoader compileConcepts(File jar, ClassLoader cl)
 			throws Exception {
 		File target = FileUtil.createTempDir(getClass().getSimpleName());
 		List<File> classpath = getClassPath(cl);
 		List<String> classes = buildConcepts(target, cl);
 		new JavaCompiler().compile(classes, target, classpath);
 		JarPacker packer = new JarPacker(target);
 		packer.setAnnotations(annotations);
 		packer.setConcepts(concepts);
 		packer.setDatatypes(datatypes);
 		packer.setOntologies(ontologies);
 		packer.packageJar(jar);
 		FileUtil.deleteDir(target);
 		return new URLClassLoader(new URL[] { jar.toURI().toURL() }, cl);
 	}
 
 	private List<String> compileMethods(File target, List<File> cp,
 			JavaNameResolver resolver) throws Exception {
 		List<String> roles = new ArrayList<String>();
 		for (RDFClass method : getOrderedMethods()) {
 			Map<String, String> map = new HashMap<String, String>();
 			Resource subj = method.getResource();
 			for (Resource ctx : model.filter(subj, null, null).contexts()) {
 				if (namespaces.containsKey(ctx)) {
 					map.putAll(namespaces.get(ctx));
 				}
 			}
 			try {
 				roles.addAll(method.msgCompile(resolver, map, target, cp));
 			} catch (Exception e) {
 				throw new ObjectCompileException(
 						"Could not compile: " + method, e);
 			}
 		}
 		return roles;
 	}
 
 	private List<RDFClass> getOrderedMethods() throws Exception {
 		List<RDFClass> list = getMethods();
 		List<RDFClass> result = new ArrayList<RDFClass>();
 		while (!list.isEmpty()) {
 			Iterator<RDFClass> iter = list.iterator();
 			loop: while (iter.hasNext()) {
 				RDFClass item = iter.next();
 				for (RDFClass p : list) {
 					if (item.precedes(p)) {
 						continue loop;
 					}
 				}
 				result.add(item);
 				iter.remove();
 			}
 		}
 		return result;
 	}
 
 	private List<RDFClass> getMethods() throws Exception {
 		List<RDFClass> methods = new ArrayList<RDFClass>();
 		for (URI body : OBJ.MESSAGE_IMPLS) {
 			for (Resource subj : model.filter(null, body, null).subjects()) {
 				RDFClass rc = new RDFClass(model, subj);
 				if (rc.isA(OWL.CLASS)) {
 					methods.add(rc);
 				}
 			}
 		}
 		return methods;
 	}
 
 	private JavaNameResolver createJavaNameResolver(ClassLoader cl,
			RoleMapper mapper, LiteralManager literals) {
 		JavaNameResolver resolver = new JavaNameResolver(cl);
 		resolver.setModel(model);
 		for (Map.Entry<String, String> e : packages.entrySet()) {
 			resolver.bindPackageToNamespace(e.getValue(), e.getKey());
 		}
 		for (Map.Entry<String, String> e : model.getNamespaces().entrySet()) {
 			resolver.bindPrefixToNamespace(e.getKey(), e.getValue());
 		}
 		if (memberPrefix == null) {
 			for (Map<String, String> p : namespaces.values()) {
 				for (Map.Entry<String, String> e : p.entrySet()) {
 					resolver.bindPrefixToNamespace(e.getKey(), e.getValue());
 				}
 			}
 		} else {
 			for (Map.Entry<String, String> e : packages.entrySet()) {
 				resolver.bindPrefixToNamespace(memberPrefix, e.getKey());
 			}
 		}
 		resolver.setRoleMapper(mapper);
 		resolver.setLiteralManager(literals);
 		return resolver;
 	}
 
 	private RDFOntology findOntology(String namespace) {
 		if (namespace.endsWith("#"))
 			return new RDFOntology(model, new URIImpl(namespace.substring(0,
 					namespace.length() - 1)));
 		return new RDFOntology(model, new URIImpl(namespace));
 	}
 
 	private String findPrefix(String ns, Model model) {
 		for (Map.Entry<String, String> e : model.getNamespaces().entrySet()) {
 			if (ns.equals(e.getValue()) && e.getKey().length() > 0) {
 				return e.getKey();
 			}
 		}
 		return "ns" + Integer.toHexString(ns.hashCode());
 	}
 
 	private Set<String> findUndefinedNamespaces(Model model, ClassLoader cl) {
 		Set<String> unknown = new HashSet<String>();
 		for (Resource subj : model.filter(null, RDF.TYPE, null).subjects()) {
 			if (subj instanceof URI) {
 				URI uri = (URI) subj;
 				String ns = uri.getNamespace();
 				if (!mapper.isRecordedConcept(uri, cl)
 						&& !literals.isRecordedeType(uri)
 						&& !mapper.isRecordedAnnotation(uri)) {
 					unknown.add(ns);
 				}
 			}
 		}
 		return unknown;
 	}
 
 	private List<File> getClassPath(ClassLoader cl)
 			throws UnsupportedEncodingException {
 		List<File> classpath = new ArrayList<File>();
 		String classPath = System.getProperty("java.class.path");
 		for (String path : classPath.split(File.pathSeparator)) {
 			classpath.add(new File(path));
 		}
 		getClassPath(classpath, cl);
 		return getClassPath(classpath, getClass().getClassLoader());
 	}
 
 	private List<File> getClassPath(List<File> classpath, ClassLoader cl)
 			throws UnsupportedEncodingException {
 		if (cl == null) {
 			return classpath;
 		} else if (cl instanceof URLClassLoader) {
 			for (URL jar : ((URLClassLoader) cl).getURLs()) {
 				File file = asLocalFile(jar);
 				if (!classpath.contains(file)) {
 					classpath.add(file);
 				}
 			}
 			return classpath;
 		} else {
 			return getClassPath(classpath, cl.getParent());
 		}
 	}
 
 }
