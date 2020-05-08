 package net.sf.javascribe.patterns.lookups;
 
 import java.util.List;
 
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.ServiceLocator;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.VariableTypeResolver;
 import net.sf.jsom.java5.Java5ClassDefinition;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 import net.sf.jsom.java5.Java5VariableDeclaration;
 
 @Scannable
 @Processor
 public class LookupsProcessor {
 
 	private static final String PROPERTY_LOOKUPS_PKG = "net.sf.javascribe.patterns.lookups.Lookups.pkg";
 
 	@ProcessorMethod(componentClass=Lookups.class)
 	public void processLookups(Lookups lookups,ProcessorContext ctx) throws JavascribeException {
 
 		ctx.setLanguageSupport("Java");
 		System.out.println("Processing Lookups definition");
 
 		Java5SourceFile locatorFile = null;
 		ServiceLocator locatorType = null;
 		String pkg = ctx.getRequiredProperty(PROPERTY_LOOKUPS_PKG);
 
 		pkg = JavaUtils.findPackageName(ctx, pkg);
 
 		if (lookups.getLocator().trim().equals("")) {
 			throw new JavascribeException("Lookups must have a locator specified.");
 		}
 
 		String locatorClassName = pkg+'.'+lookups.getLocator();
 		locatorFile = JsomUtils.getJavaFile(locatorClassName, ctx);
 
 		if (locatorFile==null) {
 			locatorFile = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx));
 			locatorFile.setPackageName(pkg);
 			locatorFile.getPublicClass().setClassName(lookups.getLocator());
 			locatorType = new LookupsLocator(pkg, locatorClassName);
 			ctx.getTypes().addType(locatorType);
 			JsomUtils.addJavaFile(locatorFile, ctx);
 		}
 
 		for(Entity entity : lookups.getEntity()) {
 			try {
 				processEntity(pkg,ctx,entity,locatorFile.getPublicClass(),locatorType);
 			} catch(CodeGenerationException e) {
 				throw new JavascribeException("JSOM exception while processing lookup",e);
 			}
 		}
 	}
 
 	private void processEntity(String pkg,ProcessorContext ctx,Entity e,Java5ClassDefinition locatorClass,ServiceLocator locatorType) throws JavascribeException,CodeGenerationException {
 		Java5SourceFile src = null;
 		String lookupClassName = e.getName()+"Lookups";
 		LookupType type = null;
 
 		src = JsomUtils.getJavaFile(pkg+'.'+lookupClassName, ctx);
 		if (src==null) {
 			src = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx.getTypes()));
 			src.setPackageName(pkg);
 			src.getPublicClass().setClassName(lookupClassName);
 			JsomUtils.addJavaFile(src, ctx);
 			locatorType.getAvailableServices().add(e.getName());
 			Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 			method.setMethodName("get"+e.getName());
 			method.setReturnType(lookupClassName);
 			Java5CodeSnippet code = new Java5CodeSnippet();
 			code.append("return new "+lookupClassName+"();\n");
 			method.setMethodBody(code);
 			locatorClass.addMethod(method);
 		}
 
 		type = (LookupType)ctx.getTypes().getType(lookupClassName);
 		if (type==null) {
 			type = new LookupType(locatorType.getClassName(),pkg,lookupClassName);
 			ctx.getTypes().addType(type);
 		}
 
 		List<Field> fields = e.getField();
 		for(Field f : fields) {
 			handleField(src,e,f,new JavascribeVariableTypeResolver(ctx.getTypes()),type);
 		}
 	}
 
 	private static void handleField(Java5SourceFile src,Entity e, Field f,VariableTypeResolver types,LookupType type) throws JavascribeException,CodeGenerationException {
 		String constName = null;
 		Java5DeclaredMethod method = new Java5DeclaredMethod(types);
 		Java5CodeSnippet code = new Java5CodeSnippet();
 		StringBuilder methodNameBuild = new StringBuilder();
 
 		methodNameBuild.append("get").append(Character.toUpperCase(f.getName().charAt(0)))
 		.append(f.getName().substring(1)).append("String");
 		String methodName = methodNameBuild.toString();
 		method.setMethodBody(code);
 		method.setMethodName(methodName);
 		method.setReturnType("string");
		method.addArg("integer", "value");
		code.append("if (value==null) return null;\n");
		code.append("String ret = null;\nswitch(value) {\n");
 		src.getPublicClass().addMethod(method);
 
 		type.addMethod(JsomUtils.createJavaOperation(method));
 
 		for(FieldValue val : f.getValue()) {
 			Java5VariableDeclaration dec = new Java5VariableDeclaration(types);
 			dec.setStatic(true);
 			constName = findConstantName(f.getName(),val.getDesc());
 			dec.setType("integer");
 			dec.setName(constName);
 			dec.setFinal(true);
 			dec.setValue(""+val.getValue());
 			src.getPublicClass().addMemberVariable(dec);
 			if ((val.getValue()==0) || (val.getDesc().trim().length()==0)) {
 				throw new JavascribeException("Lookup field values must always have desc and value attribtes");
 			}
 			code.append("case "+val.getValue()+":\nret = \""+val.getDesc()+"\";\nbreak;\n");
 		}
 		code.append("}\nreturn ret;\n");
 		method.setMethodBody(code);
 	}
 
 	private static String findConstantName(String n,String v) {
 		StringBuilder build = new StringBuilder();
 		String valueStr = v.toUpperCase();
 
 		for(int i=0;i<n.length();i++) {
 			char c = n.charAt(i);
 			if (i==0) {
 				build.append(Character.toUpperCase(c));
 			} else {
 				if (Character.isUpperCase(c)) {
 					build.append("_"+c);
 				} else build.append(Character.toUpperCase(c));
 			}
 		}
 		valueStr = valueStr.replace(' ', '_');
 		build.append('_').append(valueStr);
 
 		return build.toString();
 	}
 
 }
