 package net.sf.javascribe.patterns.domain.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.javascribe.api.AttributeHolder;
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.VariableType;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaCode;
 import net.sf.javascribe.langsupport.java.JavaCodeImpl;
 import net.sf.javascribe.langsupport.java.JavaOperation;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.patterns.domain.Resolver;
 import net.sf.javascribe.patterns.domain.ResolverContext;
 
 import org.apache.log4j.Logger;
 
 @Scannable
 public class ResolveAttributeInExecCtx implements Resolver {
 
 	public static final Logger log = Logger.getLogger(ResolveAttributeInExecCtx.class);
 
 	@Override
 	public String name() {
 		return "resolveAttributeInExecCtx";
 	}
 
 	@Override
 	public JavaCode resolve(String attribute,ResolverContext ctx) throws JavascribeException {
 		JavaServiceObjectType type = null;
 		String ruleName = "get"+JavascribeUtils.getUpperCamelName(attribute);
 
 		CodeExecutionContext execCtx = ctx.getExecCtx();
 		Map<String,JavaServiceObjectType> dependencyRefs = ctx.getDependencyRefs();
 
 		for(String key : dependencyRefs.keySet()) {
 			type = dependencyRefs.get(key);
 			List<JavaOperation> ops = type.getMethods(ruleName);
 			for(JavaOperation op : ops) {
 				if (!op.getName().equals(ruleName)) continue;
 				
 				log.debug("Level "+ctx.getLevel()+": Attempting to invoke "+key+"."+op.getName());
 				JavaCode invoke = invokeOperation(attribute,key,op,execCtx,ctx);
 				
 				if (invoke!=null) return invoke;
 			}
 		}
		log.debug("Unable to resolve for attribute '"+attribute+"'");
 		
 		return null;
 	}
 	
 	private JavaCode invokeOperation(String attribute,String obj,JavaOperation op,CodeExecutionContext execCtx,ResolverContext ctx) throws JavascribeException {
 		JavaCodeImpl ret = new JavaCodeImpl();
 		boolean doContinue = true;
 		List<String> paramNames = new ArrayList<String>();
 		HashMap<String,String> params = new HashMap<String,String>();
 
 		paramNames.addAll(op.getParameterNames());
 
 		// while we haven't had a loop with no success
 		while((doContinue) && (paramNames.size()>0)) {
 			doContinue = false;
 			for(int i=0;i<paramNames.size();i++) {
 				String p = paramNames.get(i);
 				if (execCtx.getVariableType(p)!=null) {
 					params.put(p, p);
 					doContinue = true;
 					paramNames.remove(p);
 				} else if (findParamInAttributeHolders(attribute,p,execCtx)!=null) {
 					params.put(p, findParamInAttributeHolders(attribute,p,execCtx));
 					doContinue = true;
 					paramNames.remove(p);
 				} else {
 					JavaCode resolve = ctx.resolveForAttribute(p);
 					if (resolve!=null) {
 						doContinue = true;
 						JavaUtils.append(ret, resolve);
 						paramNames.remove(p);
 						params.put(p, p);
 					}
 				}
 			}
 		}
 		
 		if (doContinue) {
 			// We resolved for the rule
 			ret.appendCodeText(attribute+" = "+obj+'.'+op.getName()+"(");
 			boolean first = true;
 			for(String p : op.getParameterNames()) {
 				if (first) first = false;
 				else ret.appendCodeText(",");
 				ret.appendCodeText(params.get(p));
 			}
 			ret.appendCodeText(");\n");
 		} else {
 			ret = null;
 		}
 		
 		return ret;
 	}
 	
 	private String findParamInAttributeHolders(String currentAttribute,String attribute,CodeExecutionContext execCtx) throws JavascribeException {
 		
 		for(String s : execCtx.getVariableNames()) {
 			if (s.equals(currentAttribute)) continue;
 			VariableType var = execCtx.getTypeForVariable(s);
 			if (var instanceof AttributeHolder) {
 				AttributeHolder h = (AttributeHolder)var;
 				if (h.getAttributeType(attribute)!=null) {
 					return h.getCodeToRetrieveAttribute(s, attribute, "object", execCtx);
 				}
 			}
 		}
 		
 		return null;
 	}
 
 }
 
