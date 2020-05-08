 package fr.univ.lille1;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 public class CodeSearchEngineImpl implements CodeSearchEngine {
 
 	private final Document mDocument;
 	private final Map<String, Type> mapTypes = new HashMap<String, CodeSearchEngine.Type>();
 	
 	{
 		// Initialize primitive types.
 		mapTypes.put("int", new TypeImpl("int", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("byte",  new TypeImpl("byte", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("short",  new TypeImpl("short", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("long",  new TypeImpl("long", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("float",  new TypeImpl("float", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("double",  new TypeImpl("double", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("boolean",  new TypeImpl("boolean", null, TypeKind.PRIMITIVE, null));
 		mapTypes.put("char",  new TypeImpl("char", null, TypeKind.PRIMITIVE, null));
 
 		mapTypes.put("Integer", new TypeImpl("Integer", "java.lang.Integer", TypeKind.CLASS, null));
 		mapTypes.put("Byte",  new TypeImpl("Byte", "java.lang.Byte", TypeKind.CLASS, null));
 		mapTypes.put("Short",  new TypeImpl("Short", "java.lang.Short", TypeKind.CLASS, null));
 		mapTypes.put("Long",  new TypeImpl("Long", "java.lang.Long", TypeKind.CLASS, null));
 		mapTypes.put("Float",  new TypeImpl("Float", "java.lang.Float", TypeKind.CLASS, null));
 		mapTypes.put("Double",  new TypeImpl("Double", "java.lang.Double", TypeKind.CLASS, null));
 		mapTypes.put("Boolean",  new TypeImpl("Boolean", "java.lang.Boolean", TypeKind.CLASS, null));
 		
 		mapTypes.put("void",  new TypeImpl("void", "java.lang.Void", TypeKind.CLASS, null));
 
 		mapTypes.put("Class",  new TypeImpl("Class", "java.lang.Class", TypeKind.CLASS, null));
 		mapTypes.put("String",  new TypeImpl("String", "java.lang.String", TypeKind.CLASS, null));
 		mapTypes.put("Object",  new TypeImpl("Object", "java.lang.Object", TypeKind.CLASS, null));
 	}
 
 	public CodeSearchEngineImpl(final Document document) {
 		mDocument = document;
 	}
 
 	public Type findType(String className) {
 		if (className == null || className.isEmpty()) {
 			return null;
 		}
 		// Primitive types.
 		if(mapTypes.containsKey(className)){
 			return mapTypes.get(className);
 		}
 		// unit > unit for exclude the root element unit.
 		final Element unit = mDocument.select("unit > unit:has(class > name:matches(^"+className+"$)),first-child").first();
 		return extractTypeFromUnitNode(className, unit);
 	}
 
 	public List<Type> findSubTypesOf(String className) {
 		if (className == null || className.isEmpty()) {
 			return new ArrayList<CodeSearchEngine.Type>();
 		}
 		final Elements extendsNameNodes = mDocument.select("extends > name:matches(^" + className + "$)");
 		final List<Type> res = new ArrayList<CodeSearchEngine.Type>();
 		for (Element extendsNameNode : extendsNameNodes) {
 			// For each extends name node, we retrieve unit node and classNode for type.
 			final Element classNode = extendsNameNode.parent().parent().parent();
 			final Element unitNode = classNode.parent();
 			final String name = classNode.getElementsByTag("name").first().text();
 			res.add(extractTypeFromUnitNode(name, unitNode));
 		}
 		return res;
 	}
 
 	public List<Field> findFieldsTypedWith(String className) {
 		if (className == null || className.isEmpty()) {
 			return new ArrayList<CodeSearchEngine.Field>();
 		}
 		final Elements fieldsTypeNames = mDocument.select("decl_stmt > decl > type > name:matches(^"+className+"$)");
 		final List<Field> res = new ArrayList<CodeSearchEngine.Field>();
 		for (Element fieldTypeName : fieldsTypeNames) {
 			// For each field type name, we retrieve his type and name declaration.
 			final Type type = findType(fieldTypeName.text());
 			final String nameDeclaration = fieldTypeName.parent().parent().getElementsByTag("name").get(1).text();
 			res.add(new FieldImpl(type, nameDeclaration));
 		}
 		return res;
 	}
 
 	public List<Location> findAllReadAccessesOf(Field field) {
 		if (field == null) {
 			return new ArrayList<CodeSearchEngine.Location>();
 		}
 		// Up first letter of the field name.
 		final String nameField = upFirstLetterOfString(field.getName());
 		final Elements functionNameNodes = mDocument.select("function:has(type > name:matches(^"+field.getType().getName()+"$)) > name:matches(^get"+nameField+"$)");
 		final List<Location> res = new ArrayList<CodeSearchEngine.Location>();
 		for (Element functionNameNode : functionNameNodes) {
 			// Retrieve unit node from the current function node.
 			final Element unit = functionNameNode.parent().parent().parent().parent();
 			res.add(new LocationImpl(unit.attr("filename"), null));
 		}
 		return res;
 	}
 
 	public List<Location> findAllWriteAccessesOf(Field field) {
 		if (field == null) {
 			return new ArrayList<CodeSearchEngine.Location>();
 		}
 		// Up first letter of the field name.
 		final String nameField = upFirstLetterOfString(field.getName());
 		final Elements functionNameNodes = mDocument.select("function:has(function > name:matches(^set"+nameField+"$)) > parameter_list type > name");
 		final List<Location> res = new ArrayList<CodeSearchEngine.Location>();
 		for (Element functionNameNode : functionNameNodes) {
 			// Retrieve unit node from the current function node.
 			final Element unit = functionNameNode.parent().parent().parent().parent().parent().parent().parent();
 			res.add(new LocationImpl(unit.attr("filename"), null));
 		}
 		return res;
 	}
 
 	public List<Method> findMethodsOf(String className) {
 		if (className == null || className.isEmpty()) {
 			return null;
 		}
 		final Elements functions = mDocument.select("class:has(class > name:matches(^"+className+"$)) > block > function");
 		final List<Method> res = new ArrayList<CodeSearchEngine.Method>();
 		for (Element function : functions) {
 			// For each function, we build method object from function node.
 			res.add(buildMethodObjectFromFunctionNode(function));
 		}
 		return res;
 	}
 
 	public List<Method> findMethodsReturning(String className) {
 		if (className == null || className.isEmpty()) {
 			return null;
 		}
 		final Elements functions = mDocument.select("function:has(type > name:matches(^"+className+"$))");
 		final List<Method> res = new ArrayList<CodeSearchEngine.Method>();
 		// Get one time the type before the loop.
 		final Type type = findType(className);
 		for (Element function : functions) {
 			// For each function, we extract method name from function node.
 			final String functionName = extractMethodNameFromFunctionNode(function);
 			res.add(new MethodImpl(type, functionName, null));
 		}
 		return res;
 	}
 
 	public List<Method> findMethodsTakingAsParameter(String className) {
 		if (className == null || className.isEmpty()) {
 			return null;
 		}
 		final Elements functions = mDocument.select("function:has(parameter_list:has(name:matches(^"+className+"$)))");
 		final List<Method> res = new ArrayList<CodeSearchEngine.Method>();
 		for (Element function : functions) {
 			// For each function, we build method object from function node.
 			res.add(buildMethodObjectFromFunctionNode(function));
 		}
 		return res;
 	}
 
 	public List<Method> findMethodsCalled(String methodName) {
 		if (methodName == null || methodName.isEmpty()) {
 			return null;
 		}		
 		final Elements functionNameNodes = mDocument.select("function > name:matches(^"+methodName+"$)");
 		final List<Method> res = new ArrayList<CodeSearchEngine.Method>();
 		for (Element functionNameNode : functionNameNodes) {
 			// For each function name node, we build method object from function node.
 			res.add(buildMethodObjectFromFunctionNode(functionNameNode.parent()));
 		}
 		return res;
 	}
 
 	public List<Method> findOverridingMethodsOf(Method method) {
 		if(method == null){
 			return null;
 		}
		final Elements functions = mDocument.select("function:has(name:matches(^"+method.getName()+"$)):has(type > name:matches(^Override$))");
 		final List<Method> res = new ArrayList<CodeSearchEngine.Method>();
 		for (Element function : functions) {
			System.out.println(function.text());
 			res.add(buildMethodObjectFromFunctionNode(function));
 		}
 		return res;
 	}
 
 	public List<Location> findNewOf(String className) {
 		if (className == null || className.isEmpty()) {
 			return new ArrayList<CodeSearchEngine.Location>();
 		}
 		final Elements units = mDocument.select("unit > unit:has(call > name:matches(^"+className+"$))");
 		final List<Location> res = new ArrayList<CodeSearchEngine.Location>();
 		for (Element unit : units) {
 			res.add(new LocationImpl(unit.attr("filename"), null));
 		}
 		return res;
 	}
 
 	public List<Location> findCastsTo(String className) {
 		if (className == null || className.isEmpty()) {
 			return new ArrayList<CodeSearchEngine.Location>();
 		}
 		// TODO Must be right but some weird results.
 		final Elements units = mDocument.select("unit > unit:has(expr:matches(("+className+")))");
 		final List<Location> res = new ArrayList<CodeSearchEngine.Location>();
 		for (Element unit : units) {
 			res.add(new LocationImpl(unit.attr("filename"), null));
 		}
 		return res;
 	}
 
 	public List<Location> findInstanceOf(String className) {
 		if (className == null || className.isEmpty()) {
 			return new ArrayList<CodeSearchEngine.Location>();
 		}
 		final Elements units = mDocument.select("unit > unit:has(expr:has(name:matches(^"+className+"$)) > name:matches(^instanceof$))");
 		final List<Location> res = new ArrayList<CodeSearchEngine.Location>();
 		for (Element unit : units) {
 			res.add(new LocationImpl(unit.attr("filename"), null));
 		}
 		return res;
 	}
 
 	public List<Method> findMethodsThrowing(String exceptionName) {
 		if (exceptionName == null || exceptionName.isEmpty()) {
 			return new ArrayList<CodeSearchEngine.Method>();
 		}
 		final Elements functions = mDocument.select("function:has(throws name:matches(^"+exceptionName+"$))");
 		final List<Method> res = new ArrayList<CodeSearchEngine.Method>();
 		for (Element function : functions) {
 			res.add(buildMethodObjectFromFunctionNode(function));
 		}
 		return res;
 	}
 
 	public List<Location> findCatchOf(String exceptionName) {
 		if (exceptionName == null || exceptionName.isEmpty()) {
 			return new ArrayList<Location>();
 		}
 		final Elements units = mDocument.select("unit > unit:has(catch > param > decl > type > name:matches("+exceptionName+"))");
 		final List<Location> res = new ArrayList<CodeSearchEngine.Location>();
 		for (Element unit : units) {
 			res.add(new LocationImpl(unit.attr("filename"), null));
 		}
 		return res;
 	}
 
 	public List<Type> findClassesAnnotatedWith(String annotationName) {
 		throw new UnsupportedOperationException("Not implemented because there is a bug in srcML.");
 	}
 	
 	/* Private methods */
 
 	private Method buildMethodObjectFromFunctionNode(final Element functionNode) {
 		final String functionName = extractMethodNameFromFunctionNode(functionNode);
 		final String typeName = extractMethodReturnTypeFromFunctionNode(functionNode);
 		final Type type = findType(typeName);
 		// Get parameters
 		final Element parameter = functionNode.select("parameter_list").first();
 		final List<Type> params = new ArrayList<CodeSearchEngine.Type>();
 		final Elements types = parameter.select("type");
 		for (Element typeNode : types) {
 			Elements nameNodes = typeNode.getElementsByTag("name");
 			if(nameNodes.size() > 1){
 				// it's a collection
 				params.add(findType(nameNodes.get(1).text()));
 			}
 			else {
 				params.add(findType(nameNodes.get(0).text()));
 			}
 		}
 		return new MethodImpl(type, functionName, params);
 	}
 	
 	private Type extractTypeFromUnitNode(final String className, final Element unitNode) {
 		if (unitNode == null) {
 			return new TypeImpl(className, extractPackageNameFromClass(className), TypeKind.CLASS, null);
 		}
 		final String packageName = extractPackageNameFromPackageNode(unitNode.getElementsByTag("package").first());
 		final String location = unitNode.attr("filename");
 		return new TypeImpl(className, packageName, TypeKind.CLASS, new LocationImpl(location, null));
 	}
 
 	private String extractPackageNameFromPackageNode(final Element packageNode) {
 		if (packageNode == null) {
 			return null;
 		}
 		String packageName = "";
 		final Elements nameNodes = packageNode.getElementsByTag("name");
 		for (int i = 0; i < nameNodes.size(); i++) {
 			final Element nameNode = nameNodes.get(i);
 			if (i == 0) {
 				packageName += nameNode.text();
 				continue;
 			}
 			packageName += "." + nameNode.text();
 		}
 		return packageName;
 	}
 	
 	private String extractPackageNameFromClass(String className) {
 		final Element importNode = mDocument.select("import:has(name:last-child:matches(^"+className+"$))").first();
 		String packageName = "";
 		final Elements nameNodes = importNode.getElementsByTag("name");
 		for (int i = 0; i < nameNodes.size(); i++) {
 			final Element nameNode = nameNodes.get(i);
 			if (i == 0) {
 				packageName += nameNode.text();
 				continue;
 			}
 			packageName += "." + nameNode.text();
 		}
 		return packageName;
 	}
 
 	private String extractMethodNameFromFunctionNode(final Element functionNode){
 		if(functionNode == null){
 			return null;
 		}
 		return functionNode.select("function > name").text();
 	}
 
 	private String extractMethodReturnTypeFromFunctionNode(final Element functionNode) {
 		if (functionNode == null) {
 			return null;
 		}
 		String typeName = "";
 		final Element types = functionNode.select("function > type:first-child").first();
 		final Elements nameNodes = types.select("type > name");
 		int i = 0;
 		if (nameNodes.size() > 1) {
 			// @Override
 			i = 1;
 		}
 		final Elements nameTypeReturn = nameNodes.get(i).getElementsByTag("name");
 		if(nameTypeReturn.size() > 1){
 			// generic class
 			typeName += nameTypeReturn.get(1).text();
 		}
 		else{
 			typeName += nameNodes.get(i).text();
 		}
 		return typeName;
 	}
 	
 	private String upFirstLetterOfString(final String fieldName) {
 		final char firstLetter = fieldName.toUpperCase().charAt(0);
 		return firstLetter + fieldName.substring(1);
 	}
 }
