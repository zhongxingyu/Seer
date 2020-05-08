 package net.sf.javascribe.patterns.translator.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import net.sf.javascribe.api.AttributeHolder;
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaCode;
 import net.sf.javascribe.langsupport.java.JavaOperation;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.JavaVariableType;
 import net.sf.javascribe.langsupport.java.LocatedJavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.ServiceLocator;
 import net.sf.javascribe.langsupport.java.jsom.JsomJavaCode;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.javascribe.patterns.translator.FieldTranslator;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5CodeSnippet;
 
 @Scannable
 public class DependencyScanTranslator implements FieldTranslator {
 
 	public static final String DATA_OBJECT_TRANSLATOR_DEPENDENCIES = "net.sf.javascribe.patterns.translator.DataObjectTranslator.dependencies";
 
 	@Override
 	public String name() {
 		return "dependencyScan";
 	}
 
 	@Override
 	public JavaCode translateFields(AttributeHolder targetType,
 			String targetVarName, CodeExecutionContext execCtx,ProcessorContext ctx,
 			List<String> fieldsToTranslate) throws JavascribeException {
 		Java5CodeSnippet ret = new Java5CodeSnippet();
 		List<String> remove = new ArrayList<String>();
 		
 		String depString = ctx.getRequiredProperty(DATA_OBJECT_TRANSLATOR_DEPENDENCIES);
 		
 		HashMap<String,JavaServiceObjectType> deps = new HashMap<String,JavaServiceObjectType>();
 		StringTokenizer tok = new StringTokenizer(depString,",");
 		
 		try {
 		while(tok.hasMoreTokens()) {
 			String s = tok.nextToken();
 			String typeName = ctx.getAttributeType(s);
 			if (typeName==null) {
 				throw new JavascribeException("Found an invalid dependency for a translator: '"+s+"'");
 			}
 			JavaVariableType type = (JavaVariableType)execCtx.getType(typeName);
 			if (type==null) {
 				throw new JavascribeException("Found an invalid dependency for a translator: '"+s+"'");
 			}
 			if (type instanceof ServiceLocator) {
 				ServiceLocator locator = (ServiceLocator)type;
 				JsomUtils.merge(ret, (JavaCode)locator.declare(s, execCtx));
 				JsomUtils.merge(ret, (JavaCode)locator.instantiate(s, null, execCtx));
 				execCtx.addVariable(s, type.getName());
 				List<String> services = locator.getAvailableServices();
 				for(String service : services) {
 					String ref = locator.getService(s, service, execCtx);
 					JavaServiceObjectType srv = (JavaServiceObjectType)ctx.getObject(service);
 					deps.put(ref, srv);
 				}
 			} else if (type instanceof LocatedJavaServiceObjectType) {
 				LocatedJavaServiceObjectType srv = (LocatedJavaServiceObjectType)type;
 				deps.put(s, srv);
 				JsomUtils.merge(ret, (JavaCode)srv.declare(s));
 				JsomUtils.merge(ret, (JavaCode)srv.instantiate(s, null));
 			} else if (type instanceof JavaServiceObjectType) {
 				deps.put(s, (JavaServiceObjectType)type);
 				JavaServiceObjectType srv = (JavaServiceObjectType)type;
 				JsomUtils.merge(ret, (JavaCode)srv.declare(s));
 				JsomUtils.merge(ret, (JavaCode)srv.instantiate(s, null));
 			} 
 		}
 		
 		for(String f : fieldsToTranslate) {
 			for(String dep : deps.keySet()) {
 				JavaVariableType type = deps.get(dep);
 				if (type instanceof JavaServiceObjectType) {
 					JavaServiceObjectType srv = (JavaServiceObjectType)type;
 					boolean result = tryServiceObject(srv,targetVarName,f,dep,ret,execCtx);
 					if (result) {
 						remove.add(f);
 						break;
 					}
 				}
 			}
 		}
 		fieldsToTranslate.removeAll(remove);
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while generating data object translator",e);
 		}
 		
 		return new JsomJavaCode(ret);
 	}
 	
 	private boolean tryServiceObject(JavaServiceObjectType srv,String targetVarName,String field,String ref,Java5CodeSnippet code,CodeExecutionContext execCtx) {
 		String name = "get"+Character.toUpperCase(field.charAt(0))+field.substring(1);
 		AttributeHolder targetType = null;
 		
 		targetType = (AttributeHolder)execCtx.getTypeForVariable(targetVarName);
 		
 		for(String n : srv.getOperationNames()) {
 			if (!n.equals(name)) {
 				continue;
 			}
 			JavaOperation op = srv.getMethod(n);
			if (attemptInvoke(op,targetVarName,field,targetType,execCtx,ref,code)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
	private boolean attemptInvoke(JavaOperation op,String targetVarName,String attribName,AttributeHolder targetType,CodeExecutionContext execCtx,String ref,Java5CodeSnippet code) {
 		String result = null;
 		
 		try {
			result = JavaUtils.callJavaOperation(null, ref, op, execCtx, null,false).trim();
			String c = targetType.getCodeToSetAttribute(targetVarName, attribName, result.trim(), execCtx).trim();
			code.append(c+";\n");
 		} catch(JavascribeException e) {
 			return false;
 		}
 		
 		return true;
 	}
 
 }
 
