 package net.sf.javascribe.patterns.classification;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.sf.javascribe.api.Attribute;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaBeanType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5DataObjectSourceFile;
 import net.sf.jsom.java5.Java5MethodSignature;
 
 //Once all classifications have been added to types, this processor generates them.
 @Scannable
 @Processor
 public class ClassificationAttributeProcessor {
 
 	@ProcessorMethod(componentClass=ClassificationAttributeProcessingComp.class)
 	public void process(ClassificationAttributeProcessingComp comp,ProcessorContext ctx) throws JavascribeException {
 		Map<String,Classification> classifications = null;
 		
		ctx.setLanguageSupport("Java");
 		classifications = (Map<String,Classification>)ctx.getObject("Classifications");
 
 		Set<String> names = classifications.keySet();
 
 		List<String> processed = new ArrayList<String>();
 		List<String> classificationNames = new ArrayList<String>();
 		
 		try {
 		for(String s : names) {
 			Classification classification = classifications.get(s);
 			processClassification(s,classification,ctx,processed,classifications,classificationNames);
 		}
 
 		ctx.putObject("ClassificationNames", classificationNames);
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while processing component",e);
 		}
 	}
 
 	protected void processClassification(String name,Classification classification,ProcessorContext ctx,List<String> processed,Map<String,Classification> classifications,List<String> classificationNames) throws CodeGenerationException,JavascribeException {
 		Java5DataObjectSourceFile src = null;
 		String pkg = null;
 		String className = null;
 		JavascribeVariableTypeResolver types = new JavascribeVariableTypeResolver(ctx);
 
 		if (classification==null) throw new JavascribeException("No classification named '"+name+"' found");
		if (processed.contains(name)) return;
 
 		pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(Classification.CLASSIFICATION_PKG));
 		className = classification.getName();
 		processed.add(classification.getName());
 		
 		src = new Java5DataObjectSourceFile(types);
 		src.setPackageName(pkg);
 		src.getPublicClass().setClassName(className);
 		src.getPublicClass().setInterface(true);
 		JsomUtils.addJavaFile(src, ctx);
 		JavaBeanType type = (JavaBeanType)ctx.getType(classification.getName());
 		
 		if ((classification.getExt()!=null) && (classification.getExt().trim().length()>0)) {
 			String t = classification.getExt();
 			if (!processed.contains(t)) {
 				processClassification(t,classifications.get(t),ctx,processed,classifications,classificationNames);
 			}
 			// Add attributes from supertype
 			JavaBeanType superType = (JavaBeanType)ctx.getTypes().getType(classification.getExt());
 			src.getPublicClass().setSuperClass(superType.getClassName());
 			src.addImport(superType.getImport());
 			List<String> superAtts = superType.getAttributeNames();
 			for(String s : superAtts) {
 				type.addAttribute(s, superType.getAttributeType(s));
 			}
 		}
 		// Add attributes declared
 		List<Attribute> attributes = JavascribeUtils.readAttributes(ctx, classification.getAttributes());
 		for(Attribute a : attributes) {
 			Java5MethodSignature sig = null;
 			sig = new Java5MethodSignature(types);
 			sig.setMethodName("set"+JavascribeUtils.getUpperCamelName(a.getName()));
 			sig.addArg(a.getType(), a.getName());
 			src.getPublicClass().addMethod(sig);
 			
 			sig = new Java5MethodSignature(types);
 			sig.setMethodName("get"+JavascribeUtils.getUpperCamelName(a.getName()));
 			sig.setReturnType(a.getType());
 			src.getPublicClass().addMethod(sig);
 			
 			type.addAttribute(a.getName(), a.getType());
 			if (ctx.getAttributeType(a.getName())==null) {
 				ctx.addAttribute(a.getName(), a.getType());
 			}
 		}
 		classificationNames.add(0, classification.getName());
 	}
 	
 }
 
