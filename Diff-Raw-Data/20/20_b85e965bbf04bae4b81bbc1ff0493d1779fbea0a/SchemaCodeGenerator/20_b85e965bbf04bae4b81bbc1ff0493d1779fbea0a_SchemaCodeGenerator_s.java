 /*
  * JGraLab - The Java Graph Laboratory
  * 
  * Copyright (C) 2006-2010 Institute for Software Technology
  *                         University of Koblenz-Landau, Germany
  *                         ist@uni-koblenz.de
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * Additional permission under GNU GPL version 3 section 7
  * 
  * If you modify this Program, or any covered work, by linking or combining
  * it with Eclipse (or a modified version of that program or an Eclipse
  * plugin), containing parts covered by the terms of the Eclipse Public
  * License (EPL), the licensors of this Program grant you additional
  * permission to convey the resulting work.  Corresponding Source for a
  * non-source form of such a combination shall include the source code for
  * the parts of JGraLab used as well as that of the covered work.
  */
 
 package de.uni_koblenz.jgralab.codegenerator;
 
 import java.util.List;
 import java.util.Stack;
 
 import de.uni_koblenz.jgralab.GraphIO;
 import de.uni_koblenz.jgralab.schema.Attribute;
 import de.uni_koblenz.jgralab.schema.AttributedElementClass;
 import de.uni_koblenz.jgralab.schema.CompositeDomain;
 import de.uni_koblenz.jgralab.schema.Constraint;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.EnumDomain;
 import de.uni_koblenz.jgralab.schema.GraphClass;
 import de.uni_koblenz.jgralab.schema.GraphElementClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.ListDomain;
 import de.uni_koblenz.jgralab.schema.MapDomain;
 import de.uni_koblenz.jgralab.schema.NamedElementClass;
 import de.uni_koblenz.jgralab.schema.Package;
 import de.uni_koblenz.jgralab.schema.RecordDomain;
 import de.uni_koblenz.jgralab.schema.RecordDomain.RecordComponent;
 import de.uni_koblenz.jgralab.schema.Schema;
 import de.uni_koblenz.jgralab.schema.SetDomain;
 import de.uni_koblenz.jgralab.schema.TypedElementClass;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * TODO add comment
  * 
  * @author ist@uni-koblenz.de
  * 
  */
 public class SchemaCodeGenerator extends CodeGenerator {
 
 	private final Schema schema;
 
 	/**
 	 * Creates a new SchemaCodeGenerator which creates code for the given schema
 	 * 
 	 * @param schema
 	 *            the schema to create the code for
 	 * @param schemaPackageName
 	 *            the package the schema is located in
 	 * @param implementationName
 	 *            the special jgralab package name to use
 	 */
 	public SchemaCodeGenerator(Schema schema, String schemaPackageName,
 			 CodeGeneratorConfiguration config) {
 		super(schemaPackageName, "", config);
 		this.schema = schema;
 		rootBlock.setVariable("simpleClassName", schema.getName());
 		rootBlock.setVariable("simpleImplClassName", schema.getName());
 		rootBlock.setVariable("baseClassName", "SchemaImpl");
 		rootBlock.setVariable("isAbstractClass", "false");
 		rootBlock.setVariable("isClassOnly", "true");
 		rootBlock.setVariable("isImplementationClassOnly", "false");
 	}
 
 	@Override
 	protected CodeBlock createHeader() {
 		addImports("#jgSchemaImplPackage#.#baseClassName#");
 		addImports("#jgSchemaPackage#.VertexClass");
 		addImports("#jgSchemaPackage#.EdgeClass");
 		addImports("#jgImplPackage#.GraphFactoryImpl");
 		addImports("#jgPackage#.ImplementationType");
 		addImports("#jgDiskImplPackage#.GraphDatabaseBaseImpl");
 		addImports("#jgDiskImplPackage#.CompleteGraphDatabaseImpl");
 		addImports("java.net.UnknownHostException");
 		addImports("java.lang.ref.WeakReference");
 		CodeSnippet code = new CodeSnippet(
 				true,
 				"/**",
 				" * The schema #simpleClassName# is implemented following the singleton pattern.",
 				" * To get the instance, use the static method <code>instance()</code>.",
 				" */",
 				"public class #simpleClassName# extends #baseClassName# {");
 		return code;
 	}
 
 	@Override
 	protected CodeBlock createBody() {
 		CodeList code = new CodeList();
 		if (currentCycle.isClassOnly()) {
 			code.add(createVariables());
 			code.add(createConstructor());
 			code.add(createGraphFactoryMethod());
 		}
 		return code;
 	}
 
 	@Override
 	protected CodeBlock createFooter() {
 		CodeList footer = new CodeList();
 		// override equals and hashCode methods
 		footer.add(new CodeSnippet("", "@Override",
 				"public boolean equals(Object o) {",
 				"\treturn super.equals(o);", "}"));
 		footer.add(new CodeSnippet("", "@Override", "public int hashCode() {",
 				"\treturn super.hashCode();", "}"));
 		footer.addNoIndent(super.createFooter());
 		return footer;
 	}
 
 	private CodeBlock createGraphFactoryMethod() {
 		addImports("#jgPackage#.Graph", "#jgPackage#.ProgressFunction",
 				"#jgPackage#.GraphIO",
 				"#jgPackage#.GraphIOException",
 				"#jgDiskImplPackage#.GraphDatabaseElementaryMethods");
 		CodeSnippet code = new CodeSnippet(
 				true,
 				"/**",
 				" * Creates a new #gcName# graph with initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
 				" *",
 				" * @param vMax initial vertex count",
 				" * @param eMax initial edge count",
 				"*/",
 				"public #gcName# create#gcCamelName#InMem(int vMax, int eMax) {",
 				"\treturn (#gcCamelName#) graphFactory.createGraphInMemoryStorage(#gcCamelName#.class, GraphFactoryImpl.generateUniqueGraphId(), vMax, eMax);",
 				"}",
 				"",
 				"/**",
 				" * Creates a new #gcName# graph with the ID <code>id</code> initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
 				" *",
 				" * @param id the id name of the new graph",
 				" * @param vMax initial vertex count",
 				" * @param eMax initial edge count",
 				" */",
 				"public #gcName# create#gcCamelName#InMem(String id, int vMax, int eMax) {",
 				"\treturn (#gcCamelName#) graphFactory.createGraphInMemoryStorage(#gcCamelName#.class, id, vMax, eMax);",
 				"}",
 				"",
 				"/**",
 				" * Creates a new #gcName# graph.",
 				"*/",
 				"public #gcName# create#gcCamelName#InMem() {",
 				"\treturn (#gcCamelName#) graphFactory.createGraphInMemoryStorage(#gcCamelName#.class, GraphFactoryImpl.generateUniqueGraphId());",
 				"}",
 				"",
 				"/**",
 				" * Creates a new #gcName# graph with the ID <code>id</code>.",
 				" *",
 				" * @param id the id name of the new graph",
 				" */",
 				"public #gcName# create#gcCamelName#InMem(String id) {",
 				"\treturn (#gcCamelName#) graphFactory.createGraphInMemoryStorage(#gcCamelName#.class, id);",
 				"}",
 				"",
 				// ---- disk bases storage support ----
 				"/**",
 				" * Creates a new #gcName# graph using disk based storage. To be called only by the graph database class!",
 				" *",
 				"*/",
 				"public #gcName# create#gcCamelName#OnDisk(String uniqueGraphId, long subgraphId, GraphDatabaseBaseImpl graphDb) {",
 				"\treturn (#gcCamelName#) graphFactory.createGraphDiskBasedStorage(#gcCamelName#.class, uniqueGraphId, subgraphId, graphDb, graphDb);",
 				"}",
 				"",
 				"/**",
 				" * Creates a new #gcName# graph using disk based storage. This method should be called by a user  ",
 				" * to create a new #gcName#-graph instance. The local hostname is detected automatically",
 				" *",
 				"*/",
 				"public #gcName# create#gcCamelName#OnDisk() {",
 				"\tString uniqueGraphId = GraphFactoryImpl.generateUniqueGraphId();",
 				"\tString hostname = null;",
 				"\ttry {",
 				"\t\thostname = InetAddress.getLocalHost().getHostAddress();",
 				"\t} catch (UnknownHostException ex) {",
 				"\t\tthrow new RuntimeException(ex);",
 				"\t}",
 				"\tlong subgraphId = GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID;",
 				"\tGraphDatabaseBaseImpl graphDb = new CompleteGraphDatabaseImpl(this, uniqueGraphId, hostname);",
 				"\treturn (#gcCamelName#) graphFactory.createGraphDiskBasedStorage(#gcCamelName#.class, uniqueGraphId, subgraphId, graphDb, graphDb);",
 				"}",
 				"",
 				"/**",
 				" * Creates a new #gcName# graph using disk based storage. This method should be called by a user ",
 				" * to create a  new #gcName#-graph instance.",
 				" * @param hostAddress the address or resolvable hostname of the local host",
 				" *",
 				"*/",
 				"public #gcName# create#gcCamelName#OnDisk(String localHostAddress) {",
 				"\tString uniqueGraphId = GraphFactoryImpl.generateUniqueGraphId();",
 				"\tlong subgraphId = GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID;",
 				"\tGraphDatabaseBaseImpl graphDb = new CompleteGraphDatabaseImpl(this, uniqueGraphId, localHostAddress);",
 				"\treturn (#gcCamelName#) graphFactory.createGraphDiskBasedStorage(#gcCamelName#.class, uniqueGraphId, subgraphId, graphDb, graphDb);",
 				"}",
 				"",
 //				"/**",
 //				" * Creates a new #gcName# graph with savemem support with the ID <code>id</code> initial vertex and edge counts <code>vMax</code>, <code>eMax</code>.",
 //				" *",
 //				" * @param id the id name of the new graph",
 //				" * @param vMax initial vertex count",
 //				" * @param eMax initial edge count",
 //				" */",
 //				"public #gcName# create#gcCamelName#Proxy(String uniqueGraphId, long subgraphId, GraphDatabaseBaseImpl graphDb, RemoteGraphDatabase remoteGraphDb) {",
 //				"\treturn (#gcCamelName#) graphFactory.createGraphDiskBasedStorage(#gcCamelName#.class, uniqueGraphId, subgraphId, graphDb, remoteGraphDb);",
 //				"}",
 
 				// ---- file handling methods ----
 				"/**",
 				" * Loads a #gcName# graph from the file <code>filename</code>.",
 				" *",
 				" * @param filename the name of the file",
 				" * @return the loaded #gcName#",
 				" * @throws GraphIOException if the graph cannot be loaded",
 				" */",
 				"public #gcName# load#gcCamelName#(String filename, ImplementationType implType) throws GraphIOException {",
 				"\treturn load#gcCamelName#(filename, implType, null);",
 				"}",
 				"",
 				"/**",
 				" * Loads a #gcName# graph from the file <code>filename</code> usign the in-memory storage.",
 				" *",
 				" * @param filename the name of the file",
 				" * @param pf a progress function to monitor graph loading",
 				" * @return the loaded #gcName#",
 				" * @throws GraphIOException if the graph cannot be loaded",
 				" */",
 				"public #gcName# load#gcCamelName#(String filename, ImplementationType implType, ProgressFunction pf) throws GraphIOException {",
 				"\tGraph graph = GraphIO.loadGraphFromFile(filename, this, pf, implType);\n"
 						+ "\tif (!(graph instanceof #gcName#)) {\n"
 						+ "\t\tthrow new GraphIOException(\"Graph in file '\" + filename + \"' is not an instance of GraphClass #gcName#\");\n"
 						+ "\t}" + "\treturn (#gcName#) graph;\n",
 				"}",
 				""
 				);
 		code.setVariable("gcName", schema.getGraphClass().getQualifiedName());
 		code.setVariable("gcCamelName", camelCase(schema.getGraphClass()
 				.getQualifiedName()));
 		code.setVariable("gcImplName", schema.getGraphClass()
 				.getQualifiedName()
 				+ "Impl");
 		addImports("java.net.InetAddress");
 		return code;
 	}
 
 	private CodeBlock createConstructor() {
 		CodeList code = new CodeList();
 		code
 				.addNoIndent(new CodeSnippet(
 						true,
 						"/**",
 						" * the weak reference to the singleton instance",
 						" */",
 						"static WeakReference<#simpleClassName#> theInstance = new WeakReference<#simpleClassName#>(null);",
 						"",
 						"/**",
 						" * @return the singleton instance of #simpleClassName#",
 						" */",
 						"public static #simpleClassName# instance() {",
 						"\t#simpleClassName# s = theInstance.get();",
 						"\tif (s != null) {",
 						"\t\treturn s;",
 						"\t}",
 						"\tsynchronized (#simpleClassName#.class) {",
 						"\t\ts = theInstance.get();",
 						"\t\tif (s != null) {",
 						"\t\t\treturn s;",
 						"\t\t}",
 						"\t\ts = new #simpleClassName#();",
 						"\t\ttheInstance = new WeakReference<#simpleClassName#>(s);",
 						"\t}",
 						"\treturn s;",
 						"}",
 						"",
 						"/**",
 						" * Creates a #simpleClassName# and builds its schema classes.",
 						" * This constructor is private. Use the <code>instance()</code> method",
 						" * to acess the schema.", " */",
 						"private #simpleClassName#() {",
 						"\tsuper(\"#simpleClassName#\", \"#schemaPackage#\");"));
 
 		code.add(createEnumDomains());
 		code.add(createCompositeDomains());
 		code.add(createGraphClass());
 		code.add(createPackageComments());
 		addImports("#schemaPackage#.#simpleClassName#Factory");
 		code.add(new CodeSnippet(true,
 				"graphFactory = new #simpleClassName#Factory();"));
 		code.addNoIndent(new CodeSnippet(true, "}"));
 		return code;
 	}
 
 	private CodeBlock createPackageComments() {
 		CodeList code = new CodeList();
 		Package pkg = schema.getDefaultPackage();
 		Stack<Package> s = new Stack<Package>();
 		s.push(pkg);
 		boolean hasComment = false;
 		while (!s.isEmpty()) {
 			pkg = s.pop();
 			for (Package sub : pkg.getSubPackages().values()) {
 				s.push(sub);
 			}
 			List<String> comments = pkg.getComments();
 			if (comments.isEmpty()) {
 				continue;
 			}
 			if (!hasComment) {
 				code.addNoIndent(new CodeSnippet(true, "{"));
 				hasComment = true;
 			}
 			if (comments.size() == 1) {
 				code.add(new CodeSnippet("getPackage(\""
 						+ pkg.getQualifiedName() + "\").addComment(\""
 						+ stringQuote(comments.get(0)) + "\");"));
 			} else {
 				int n = 0;
 				code.add(new CodeSnippet("getPackage(\""
 						+ pkg.getQualifiedName() + "\").addComment("));
 				for (String comment : comments) {
 					code.add(new CodeSnippet("\t\"" + stringQuote(comment)
 							+ "\"" + (++n == comments.size() ? ");" : ",")));
 				}
 
 			}
 		}
 		if (hasComment) {
 			code.addNoIndent(new CodeSnippet(false, "}"));
 		}
 		return code;
 	}
 
 	private CodeBlock createGraphClass() {
 		GraphClass gc = schema.getGraphClass();
 		CodeList code = new CodeList();
 		addImports("#jgSchemaPackage#.GraphClass");
 		code.setVariable("gcName", gc.getQualifiedName());
 		code.setVariable("gecVariable", "gc");
 		code.setVariable("gcVariable", "gc");
 		code.setVariable("aecVariable", "gc");
 		code.setVariable("schemaVariable", gc.getVariableName());
 		code.setVariable("gcAbstract", gc.isAbstract() ? "true" : "false");
 		code.addNoIndent(new CodeSnippet(
 						true,
 						"{",
 						"\tGraphClass #gecVariable# = #schemaVariable# = createGraphClass(\"#gcName#\");",
 						"\t#gecVariable#.setAbstract(#gcAbstract#);"));
 		code.add(createAttributes(gc));
 		code.add(createConstraints(gc));
 		code.add(createComments("gc", gc));
 		code.add(createVertexClasses(gc));
 		code.add(createEdgeClasses(gc));
 		code.add(createIncidenceClasses(gc));
 		code.addNoIndent(new CodeSnippet(false, "}"));
 		return code;
 	}
 
 	private CodeBlock createComments(String variableName, NamedElementClass ne) {
 		CodeList code = new CodeList();
 		code.setVariable("namedElement", variableName);
 		for (String comment : ne.getComments()) {
 			code.addNoIndent(new CodeSnippet("#namedElement#.addComment("
 					+ GraphIO.toUtfString(comment) + ");"));
 		}
 		return code;
 	}
 
 	private CodeBlock createVariables() {
 		CodeList code = new CodeList();
 
 		code.addNoIndent(new CodeSnippet("public final GraphClass "
 				+ schema.getGraphClass().getVariableName() + ";"));
 
 		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
 			if (!vc.isInternal()) {
 				code.addNoIndent(new CodeSnippet("public final VertexClass "
 						+ vc.getVariableName() + ";"));
 			}
 		}
 		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
 			if (!ec.isInternal()) {
 				if (ec.isBinary()) {
 					addImports("#jgSchemaPackage#.BinaryEdgeClass");
 					code.addNoIndent(new CodeSnippet("public final BinaryEdgeClass "
 						+ ec.getVariableName() + ";"));
 				} else {
 					code.addNoIndent(new CodeSnippet("public final EdgeClass "
 							+ ec.getVariableName() + ";"));
 				}
 			}
 		}
 		for (IncidenceClass ic : schema.getIncidenceClassesInTopologicalOrder()) {
 			if (!ic.isInternal()) {
 				code.addNoIndent(new CodeSnippet("public final IncidenceClass "
 						+ ic.getVariableName() + ";"));
 			}
 		}
 		return code;
 	}
 
 
 	
 	private CodeBlock createIncidenceClass(IncidenceClass ic) {
 		CodeList code = new CodeList();
 		addImports("#jgSchemaPackage#.IncidenceClass");
 		addImports("#jgSchemaPackage#.IncidenceType");
 		addImports("#jgPackage#.Direction");
 		
 		code.setVariable("icName", ic.getQualifiedName());
 		code.setVariable("schemaVariable", ic.getVariableName());
 		code.setVariable("icEdgeClass", ic.getEdgeClass().getQualifiedName());
 		code.setVariable("icVertexClass", ic.getVertexClass().getQualifiedName());
 		code.setVariable("icAbstract", ic.isAbstract() ? "true" : "false");
 		code.setVariable("icRoleName", ic.getRolename() != null ? ic.getRolename() : "");
 		code.setVariable("dir", ic.getDirection().toString());
 		code.setVariable("schemaVariable", ic.getVariableName());
 		code.setVariable("incidenceType", ic.getIncidenceType().toString());
 		code.setVariable("minEdgesAtVertex", Integer.toString(ic.getMinEdgesAtVertex()));
 		code.setVariable("minVerticesAtEdge", Integer.toString(ic.getMinVerticesAtEdge()));
 		code.setVariable("maxEdgesAtVertex", Integer.toString(ic.getMaxEdgesAtVertex()));
 		code.setVariable("maxVerticesAtEdge", Integer.toString(ic.getMaxVerticesAtEdge()));
 		
 		code.addNoIndent(new CodeSnippet(
 						true,
 						"{",
 						/*"\tIncidenceClass #icVariable# =*/ "#schemaVariable# = #gcVariable#.createIncidenceClass(",
 						"\t\t#gcVariable#.getEdgeClass(\"#icEdgeClass#\"),",
 						"\t\t#gcVariable#.getVertexClass(\"#icVertexClass#\"),",
 						"\t\t\"#icRoleName#\",#icAbstract#,#minEdgesAtVertex#,#maxEdgesAtVertex#,",
 						"\t\t#minVerticesAtEdge#,#maxVerticesAtEdge#,Direction.#dir#,IncidenceType.#incidenceType#);"));
 
 		for (IncidenceClass superClass : ic.getDirectSuperClasses()) {
 			if (superClass.isInternal()) {
 				continue;
 			}
 			CodeSnippet s = new CodeSnippet(
 					"#schemaVariable#.addSuperClass(#superClassName#);");
 			s.setVariable("superClassName", superClass.getVariableName());
 			code.add(s);
 		}
 
 
 		code.add(createConstraints(ic));
 		code.add(createComments("ic", ic));
 		code.addNoIndent(new CodeSnippet("}"));
 		return code;
 	}
 	
 	
 	private CodeBlock createIncidenceClasses(GraphClass gc) {
 		CodeList code = new CodeList();
 		for (IncidenceClass ic : schema.getIncidenceClassesInTopologicalOrder()) {
 			if (!ic.isInternal()) {
 				code.addNoIndent(createIncidenceClass(ic));
 			}
 		}
 		return code;
 	}
 	
 
 	private CodeBlock createVertexClasses(GraphClass gc) {
 		CodeList code = new CodeList();
 		for (VertexClass vc : schema.getVertexClassesInTopologicalOrder()) {
 //			if (vc.isInternal()) {
 //				CodeSnippet s = new CodeSnippet();
 //				s.setVariable("schemaVariable", vc.getVariableName());
 //				s.add("@SuppressWarnings(\"unused\")");
 //				s.add("VertexClass #schemaVariable# = getDefaultVertexClass();");
 //				code.addNoIndent(s);
 //			} 
 //			else
 				if (!vc.isInternal() && vc.getGraphClass() == gc) {
 				code.addNoIndent(createGraphElementClass(vc, "Vertex"));
 			}
 		}
 		return code;
 	}
 	
 	private CodeBlock createEdgeClasses(GraphClass gc) {
 		CodeList code = new CodeList();
 		for (EdgeClass ec : schema.getEdgeClassesInTopologicalOrder()) {
 			if (!ec.isInternal() && (ec.getGraphClass() == gc)) {
 				if (ec.isBinary()) {
 					code.addNoIndent(createGraphElementClass(ec, "BinaryEdge"));
 				} else {
 					code.addNoIndent(createGraphElementClass(ec, "Edge"));					
 				}
 			}
 		}
 		return code;
 	}
 	
 
 	/**
 	 * Generates the code snippet to create the metaclass gec in the schema as soon as the 
 	 * schema-impl is loaded. 
 	 * @param gec the MetaClass to be generated
 	 * @param typeName the name of the type of the MetaClass, i.e. Vertex or Edge
 	 * @return
 	 */
 	private CodeBlock createGraphElementClass(GraphElementClass<?,?> gec, String typeName ) {
 		CodeList code = new CodeList();
 		code.setVariable("gecName", gec.getQualifiedName());
 		code.setVariable("gecVariable", "gec");
 		code.setVariable("gecType", typeName);
 		code.setVariable("schemaVariable", gec.getVariableName());
 		code.setVariable("gecAbstract", gec.isAbstract() ? "true" : "false");
 		code.addNoIndent(new CodeSnippet(
 						true,
 						"{",
 						"\t#gecType#Class #gecVariable# = #schemaVariable# = gc.create#gecType#Class(\"#gecName#\");",
 						"\t#gecVariable#.setAbstract(#gecAbstract#);"));
 		for (GraphElementClass<?,?> superClass : gec.getDirectSuperClasses()) {
 			if (superClass.isInternal()) {
 				continue;
 			}
 			CodeSnippet s = new CodeSnippet(
 					"#gecVariable#.addSuperClass(#superClassName#);");
 			s.setVariable("superClassName", superClass.getVariableName());
 			code.add(s);
 		}
 		code.add(createAttributes(gec));
 		code.add(createConstraints(gec));
 		code.add(createComments("gec", gec));
 		code.addNoIndent(new CodeSnippet("}"));
 		return code;
 	}
 
 	private CodeBlock createAttributes(AttributedElementClass<?, ?> aec) {
 		CodeList code = new CodeList();
 		for (Attribute attr : aec.getOwnAttributeList()) {
 			CodeSnippet s = new CodeSnippet(
 					false,
 					"#gecVariable#.addAttribute(createAttribute(\"#attrName#\", getDomain(\"#domainName#\"), getAttributedElementClass(\"#aecName#\"), #defaultValue#));");
 			s.setVariable("attrName", attr.getName());
 			s.setVariable("domainName", attr.getDomain().getQualifiedName());
 			s.setVariable("aecName", aec.getQualifiedName());
 			if (attr.getDefaultValueAsString() == null) {
 				s.setVariable("defaultValue", "null");
 			} else {
 				s.setVariable("defaultValue", "\""
 						+ attr.getDefaultValueAsString().replaceAll("([\\\"])",
 								"\\\\$1") + "\"");
 			}
 			code.addNoIndent(s);
 		}
 		return code;
 	}
 
 	private CodeBlock createConstraints(TypedElementClass<?, ?> aec) {
 		CodeList code = new CodeList();
 		for (Constraint constraint : aec.getConstraints()) {
 			addImports("#jgSchemaImplPackage#.ConstraintImpl");
 			CodeSnippet constraintSnippet = new CodeSnippet(false);
 			constraintSnippet
 					.add("#aecVariable#.addConstraint("
 							+ "new ConstraintImpl(#message#, #predicate#, #offendingElements#));");
 			constraintSnippet.setVariable("message", "\""
 					+ stringQuote(constraint.getMessage()) + "\"");
 			constraintSnippet.setVariable("predicate", "\""
 					+ stringQuote(constraint.getPredicate()) + "\"");
 			if (constraint.getOffendingElementsQuery() != null) {
 				constraintSnippet.setVariable("offendingElements", "\""
 						+ stringQuote(constraint.getOffendingElementsQuery())
 						+ "\"");
 			} else {
 				constraintSnippet.setVariable("offendingElements", "null");
 			}
 			code.addNoIndent(constraintSnippet);
 		}
 		return code;
 	}
 
 	private CodeBlock createEnumDomains() {
 		CodeList code = new CodeList();
 		for (EnumDomain dom : schema.getEnumDomains()) {
 			CodeSnippet s = new CodeSnippet(true);
 			s.setVariable("domName", dom.getQualifiedName());
 			code.addNoIndent(s);
 			addImports("#jgSchemaPackage#.EnumDomain");
 			s.add("{", "\tEnumDomain dom = createEnumDomain(\"#domName#\");");
 			for (String c : dom.getConsts()) {
 				s.add("\tdom.addConst(\"" + c + "\");");
 			}
 			code.add(createComments("dom", dom));
 			code.addNoIndent(new CodeSnippet("}"));
 		}
 		return code;
 	}
 
 	private CodeBlock createCompositeDomains() {
 		CodeList code = new CodeList();
 		for (CompositeDomain dom : schema
 				.getCompositeDomainsInTopologicalOrder()) {
 			CodeSnippet s = new CodeSnippet(true);
 			s.setVariable("domName", dom.getQualifiedName());
 			code.addNoIndent(s);
 			if (dom instanceof ListDomain) {
 				s.setVariable("componentDomainName", ((ListDomain) dom)
 						.getBaseDomain().getQualifiedName());
 				s
 						.add("createListDomain(getDomain(\"#componentDomainName#\"));");
 			} else if (dom instanceof SetDomain) {
 				s.setVariable("componentDomainName", ((SetDomain) dom)
 						.getBaseDomain().getQualifiedName());
 				s.add("createSetDomain(getDomain(\"#componentDomainName#\"));");
 			} else if (dom instanceof MapDomain) {
 				MapDomain mapDom = (MapDomain) dom;
 				s.setVariable("keyDomainName", mapDom.getKeyDomain()
 						.getQualifiedName());
 				s.setVariable("valueDomainName", mapDom.getValueDomain()
 						.getQualifiedName());
 				s
 						.add("createMapDomain(getDomain(\"#keyDomainName#\"), getDomain(\"#valueDomainName#\"));");
 			} else if (dom instanceof RecordDomain) {
 				addImports("#jgSchemaPackage#.RecordDomain");
 				s
 						.add("{",
 								"\tRecordDomain dom = createRecordDomain(\"#domName#\");");
 				RecordDomain rd = (RecordDomain) dom;
 				for (RecordComponent c : rd.getComponents()) {
 					s.add("\tdom.addComponent(\"" + c.getName()
 							+ "\", getDomain(\""
 							+ c.getDomain().getQualifiedName() + "\"));");
 				}
 				code.add(createComments("dom", rd));
 				code.addNoIndent(new CodeSnippet("}"));
 			} else {
 				throw new RuntimeException("FIXME!"); // never reachable
 			}
 		}
 		return code;
 	}
 }
