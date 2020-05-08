 package net.sf.javascribe.patterns.lookups;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaOperation;
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
 
 	private static final Logger log = Logger.getLogger(LookupsProcessor.class);
 
 	private static final String PROPERTY_LOOKUPS_PKG = "net.sf.javascribe.patterns.lookups.Lookups.pkg";
 
 	@ProcessorMethod(componentClass=Lookups.class)
 	public void processLookups(Lookups lookups,ProcessorContext ctx) throws JavascribeException {
 
 		ctx.setLanguageSupport("Java");
 		log.info("Processing Lookups definition");
 
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
 			log.info("Creating locator class '"+locatorClassName+"' for lookups");
 			locatorFile = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx));
 			locatorFile.setPackageName(pkg);
 			locatorFile.getPublicClass().setClassName(lookups.getLocator());
 			locatorType = new LookupsLocator(pkg, lookups.getLocator());
 			ctx.getTypes().addType(locatorType);
 			JsomUtils.addJavaFile(locatorFile, ctx);
 			ctx.addAttribute(JavascribeUtils.getLowerCamelName(locatorClassName), locatorClassName);
 		} else {
 			locatorType = (ServiceLocator)ctx.getType(locatorClassName);
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
 		String lookupClassName = e.getName()+"Lookup";
 		LookupType type = null;
 
 		src = JsomUtils.getJavaFile(pkg+'.'+lookupClassName, ctx);
 		if (src==null) {
 			log.debug("Adding lookup class for entity "+e.getName());
 			src = new Java5SourceFile(new JavascribeVariableTypeResolver(ctx.getTypes()));
 			src.setPackageName(pkg);
 			src.getPublicClass().setClassName(lookupClassName);
 			JsomUtils.addJavaFile(src, ctx);
 			locatorType.getAvailableServices().add(e.getName());
 			Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 			method.setName("get"+lookupClassName);
 			method.setType(lookupClassName);
 			Java5CodeSnippet code = new Java5CodeSnippet();
 			code.append("return new "+lookupClassName+"();\n");
 			method.setMethodBody(code);
 			locatorClass.addMethod(method);
 			ctx.addAttribute(JavascribeUtils.getLowerCamelName(lookupClassName), lookupClassName);
 		}
 
 		type = (LookupType)ctx.getTypes().getType(lookupClassName);
 		if (type==null) {
 			type = new LookupType(locatorType.getImport(),pkg,lookupClassName);
 			ctx.getTypes().addType(type);
 		}
 
 		List<Field> fields = e.getField();
 		for(Field f : fields) {
			handleField(src,e,f,new JavascribeVariableTypeResolver(ctx.getTypes()),type,ctx);
 		}
 	}
 
	private static void handleField(Java5SourceFile src,Entity e, Field f,VariableTypeResolver types,LookupType type,ProcessorContext ctx) throws JavascribeException,CodeGenerationException {
 		String constName = null;
 		Java5DeclaredMethod method = new Java5DeclaredMethod(types);
 		Java5CodeSnippet code = new Java5CodeSnippet();
 
 		log.debug("Adding lookup for field '"+f.getName()+"'");
		ctx.addAttribute(f.getName(), "integer");
		ctx.addAttribute(f.getName()+"String", "string");
		String methodName = "get"+JavascribeUtils.getUpperCamelName(f.getName()+"String");
 		method.setMethodBody(code);
 		method.setName(methodName);
 		method.setType("string");
 		method.addArg("integer", f.getName());
 		code.append("if ("+f.getName()+"==null) return null;\n");
 		code.append("String ret = null;\nswitch("+f.getName()+") {\n");
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
 		JavaOperation op = JsomUtils.createJavaOperation(method);
 		type.addMethod(op);
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
