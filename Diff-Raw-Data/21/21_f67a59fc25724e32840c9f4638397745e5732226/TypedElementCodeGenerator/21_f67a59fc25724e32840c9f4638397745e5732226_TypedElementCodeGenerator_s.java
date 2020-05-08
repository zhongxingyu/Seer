 package de.uni_koblenz.jgralab.codegenerator;
 
 import java.util.SortedSet;
 import java.util.TreeSet;
 
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.TypedElementClass;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 public abstract class TypedElementCodeGenerator<ConcreteMetaClass extends TypedElementClass<ConcreteMetaClass, ?>>
 		extends CodeGenerator {
 
 	/**
 	 * all the interfaces of the class which are being implemented
 	 */
 	protected SortedSet<String> interfaces;
 	/**
 	 * the AttributedElementClass to generate code for
 	 */
 	protected ConcreteMetaClass aec;
 
 	public TypedElementCodeGenerator(ConcreteMetaClass metaClass, String schemaRootPackageName,
 			String packageName, CodeGeneratorConfiguration config) {
 		super(schemaRootPackageName, packageName, config);
 		this.aec = metaClass;
 		rootBlock.setVariable("ecName", aec.getSimpleName());
 		rootBlock.setVariable("qualifiedClassName", aec.getQualifiedName());
 		rootBlock.setVariable("schemaName", aec.getSchema().getName());
 		rootBlock.setVariable("schemaVariableName", aec.getVariableName());
 		rootBlock.setVariable("javaClassName", schemaRootPackageName + "."
 				+ aec.getQualifiedName());
 		rootBlock.setVariable("qualifiedImplClassName", schemaRootPackageName
 				+ ".impl." + (config.hasTransactionSupport() ? "trans" : "std")
 				+ aec.getQualifiedName() + "Impl");
 		rootBlock.setVariable("simpleClassName", aec.getSimpleName());
 		rootBlock.setVariable("simpleImplClassName", aec.getSimpleName()
 				+ "Impl");
 		rootBlock.setVariable("uniqueClassName", aec.getUniqueName());
 		rootBlock.setVariable("schemaPackageName", schemaRootPackageName);
 		rootBlock.setVariable("theGraph", "graph");
 		
 
 		rootBlock.setVariable("isAbstractClass", aec.isAbstract() ? "true"
 				: "false");
 		interfaces = new TreeSet<String>();
 		interfaces.add(aec.getQualifiedName());
 		for (TypedElementClass<?,?> superClass : aec.getDirectSuperClasses()) {
			interfaces.add(superClass.getQualifiedName());
 		}
 	}
 	
 	/**
 	 * Returns the absolute name of the given AttributdelementClass. The name is
 	 * composed of the package-prefix of the schema the class belongs to and the
 	 * qualified name of the class
 	 * 
 	 * @param ,Classmet
 	 * @return
 	 */
 	protected String absoluteName(TypedElementClass<?,?> metaClass) {
 		return schemaRootPackageName + "." +  metaClass.getQualifiedName();
 	}
 
 	@Override
 	protected CodeList createBody() {
 		if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()) {
 //			if (currentCycle.isStdImpl()) {
 //				addImports("#jgImplStdPackage#.#baseClassName#");
 //			} else if (currentCycle.isSaveMemImpl()) {
 //				addImports("#jgImplSaveMemPackage#.#baseClassName#");
 //			} else if (currentCycle.isTransImpl()) {
 //				addImports("#jgImplTransPackage#.#baseClassName#");
 //			} else if (currentCycle.isDbImpl()) {
 //				addImports("#jgImplDbPackage#.#baseClassName#");
 //			}
 			addImports("#usedJgImplPackage#.#baseClassName#");
 
 			rootBlock.setVariable("baseClassName", "#ownElementClass#Impl");
 			//add valid incidences
 		}
 		CodeList code = new CodeList();
 		if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()) {
 			code.add(createGetTypeMethod());
 			code.add(createConstructor());
 			code.add(createGetM1ClassMethod());
 	//		code.add(createGetM1ImplementationClassMethod());
 
 		}
 		return code;
 	}
 
 	@Override
 	protected CodeBlock createHeader() {
 		CodeSnippet code = new CodeSnippet(true);
 		code.setVariable("classOrInterface", currentCycle
 				.isStdOrSaveMemOrDbImplOrTransImpl() ? " class" : " interface");
 		code.setVariable("abstract", currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()
 				                       && aec.isAbstract() ? " abstract" : "");
 		code.setVariable("impl", currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()
 				                    && !aec.isAbstract() ? "Impl" : "");
 		code.add("public#abstract##classOrInterface# #simpleClassName##impl##extends##implements# {");
 		code.setVariable("extends",	currentCycle.isStdOrSaveMemOrDbImplOrTransImpl() ? 
 				                    " extends #baseClassName#" : "");
 		StringBuffer buf = new StringBuffer();
 		if (interfaces.size() > 0) {
 			String delim = currentCycle.isStdOrSaveMemOrDbImplOrTransImpl() ? " implements "
 					: " extends ";
 			for (String interfaceName : interfaces) {
 				if (currentCycle.isStdOrSaveMemOrDbImplOrTransImpl()
 						|| !interfaceName.equals(aec.getQualifiedName())) {
 					if (interfaceName.equals("Vertex")
 							|| interfaceName.equals("Edge")
 							|| interfaceName.equals("Graph")
 							|| interfaceName.equals("Incidence")) {
 						buf.append(delim);
 						buf.append("#jgPackage#." + interfaceName);
 						delim = ", ";
 					} else {
 						buf.append(delim);
 						buf.append(schemaRootPackageName + "." + interfaceName);
 						delim = ", ";
 					}
 				}
 			}
 		}
 		code.setVariable("implements", buf.toString());
 		return code;
 	}
 
 	protected CodeBlock createStaticImplementationClassField() {
 		return new CodeSnippet(
 				true,
 				"/**",
 				" * refers to the default implementation class of this interface",
 				" */",
 				"public static final java.lang.Class<#qualifiedImplClassName#> IMPLEMENTATION_CLASS = #qualifiedImplClassName#.class;");
 	}
 
 	protected abstract CodeBlock createConstructor();
 
 	protected CodeBlock createSpecialConstructorCode() {
 		return null;
 	}
 	
 	protected CodeBlock createGetTypeMethod() {
 		return new CodeSnippet(
 				true,
 				"public final #jgSchemaPackage#.#graphElementClass#Class getType() {",
 				"\treturn #schemaPackageName#.#schemaName#.instance().#schemaVariableName#;",
 				"}");
 	}
 
 	
 	protected CodeBlock createGetM1ClassMethod() {
 		return new CodeSnippet(
 				true,
 				"public final java.lang.Class<? extends #jgPackage#.#graphElementClass#> getM1Class() {",
 				"\treturn #javaClassName#.class;", "}");
 	}
 
 	
 	protected CodeBlock createGetM1ImplementationClassMethod() {
 		return new CodeSnippet(
 				true,
 				"public final java.lang.Class<? extends #jgPackage#.#graphElementClass#> getM1ImplementationClass() {",
 				"\treturn #javaImplClassName#.class;", "}");
 	}
 
 
 }
