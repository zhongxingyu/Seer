 package net.sf.javascribe.patterns.domain.impl;
 
 import java.util.List;
 
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.api.types.ListType;
 import net.sf.javascribe.langsupport.java.JavaCode;
 import net.sf.javascribe.langsupport.java.JavaCodeImpl;
 import net.sf.javascribe.langsupport.java.JavaOperation;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.JavaVariableType;
 import net.sf.javascribe.patterns.domain.Resolver;
 import net.sf.javascribe.patterns.domain.ResolverContext;
 
 import org.apache.log4j.Logger;
 
 @Scannable
 public class ResolveListAttributeInExecCtx implements Resolver {
 
 	public static final Logger log = Logger.getLogger(ResolveListAttributeInExecCtx.class);
 
 	@Override
 	public String name() {
 		return "resolveListAttributeInExecCtx";
 	}
 
 	@Override
 	public JavaCode resolve(String attribute,ResolverContext ctx) throws JavascribeException {
 		JavaCode ret = null;
 		
 		// This resolver only applies to lists
 		String attributeType = ctx.getSystemAttributeType(attribute);
 		
 		if (!attributeType.startsWith("list/")) {
 			return null;
 		}
 		if (!attribute.endsWith("List")) {
 			return null;
 		}
//		if (ctx.getExecCtx().getVariableType(attribute)!=null) {
//			return null;
//		}
 		
 		String attributeElement = attribute.substring(0, attribute.length()-4);
 		String elementTypeName = attributeType.substring(5);
 
 		// Look for a rule that will return a single element of the list.
 		String targetRuleName = "get" + Character.toUpperCase(attributeElement.charAt(0)) + 
 				attributeElement.substring(1);
 		List<String> dependencies = ctx.getDependencyNames();
 		for(String dep : dependencies) {
 			JavaServiceObjectType objType = ctx.getDependencyRefs().get(dep);
 			
 			List<JavaOperation> ops = objType.getMethods();
 			for(JavaOperation op : ops) {
 				if (op.getName().equals(targetRuleName)) {
 					if ((op.getReturnType()!=null) && (op.getReturnType().equals(elementTypeName))) {
 						// Attempt to invoke this rule and 
 						ret = checkRule(attribute,attributeType,attributeElement,elementTypeName,dep,op,ctx);
 						if (ret!=null) return ret;
 					}
 				}
 			}
 		}
 		
 		return ret;
 	}
 	
 	private JavaCode checkRule(String returnValue,String returnType,String elementAttribute,String elementTypeName,String dep,JavaOperation op,ResolverContext ctx) throws JavascribeException {
 		JavaCode ret = new JavaCodeImpl();
 		ListType listType = (ListType)ctx.getExecCtx().getType("list");
 		JavaCode singleParam = null;
 		JavaVariableType elementType = (JavaVariableType)ctx.getExecCtx().getType(elementTypeName);
 		String listToLoop = null;
 		String loopItem = null;
 		JavaVariableType loopItemType = null;
 
 		JavaUtils.append(ret, (JavaCode)listType.instantiate(returnValue, elementTypeName, ctx.getExecCtx()));
 
 		// Attempt to resolve for operation's parameters.
 		List<String> paramNames = op.getParameterNames();
 		for(String paramName : paramNames) {
 			if (ctx.getExecCtx().getVariableType(paramName)!=null) continue;
 			singleParam = ctx.resolveForAttribute(paramName);
 			if (singleParam==null) {
 				if (listToLoop!=null) return null;
 				singleParam = ctx.resolveForAttribute(paramName+"List");
 				if (singleParam==null) {
 					return null;
 				} else {
 					JavaUtils.append(ret, singleParam);
 					listToLoop = paramName+"List";
 					loopItem = paramName;
 					String loopItemTypeName = ctx.getSystemAttributeType(loopItem);
 					loopItemType = (JavaVariableType)ctx.getExecCtx().getType(loopItemTypeName);
 				}
 			} else {
 				JavaUtils.append(ret, singleParam);
 			}
 		}
 		if (listToLoop==null) return null;
 
 		ret.addImport(loopItemType.getImport());
 		ret.appendCodeText("for ("+loopItemType.getClassName()+" "+loopItem);
 		ret.appendCodeText(" : "+listToLoop);
 		ret.appendCodeText(") {\n");
 		ctx.getExecCtx().addVariable(loopItem, loopItemType.getName());
 		JavaUtils.append(ret, (JavaCode)elementType.declare(elementAttribute, ctx.getExecCtx()));
 		ctx.getExecCtx().addVariable(elementAttribute, elementTypeName);
 		String invoke = JavaUtils.callJavaOperation(elementAttribute, dep, op, ctx.getExecCtx(), null);
 		ret.appendCodeText(invoke);
 		JavaUtils.append(ret, (JavaCode)listType.appendToList(returnValue, elementAttribute, ctx.getExecCtx()));
 		ret.appendCodeText("\n}\n");
 
 		return ret;
 	}
 
 	/*
 List<UserData> getUserDataList(int companyId) {
 	List<UserData> userDataList = null;
 	userDataList = new ArrayList<UserDataList>();
 	List<User> userList = ds.getUserList(companyId);
 	Company company = dep.getCompany(companyId);
 	
 	for(User user : userList) {
 		UserData userData = null;
 		userData = translator.getUserData(user,company);
 		ret.add(userData);
 	}
 }
 	 */
 	
 	/*
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
 	*/
 	
 	/*
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
 	*/
 
 }
 
