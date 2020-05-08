 package net.sf.javascribe.patterns.js.page;
 
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 
 import net.sf.javascribe.api.AttributeHolder;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.VariableType;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.JavaBeanType;
 import net.sf.javascribe.langsupport.javascript.JavascriptSourceFile;
 import net.sf.javascribe.langsupport.javascript.JavascriptUtils;
 import net.sf.javascribe.langsupport.javascript.JavascriptVariableType;
 import net.sf.javascribe.patterns.js.page.elements.BinderUtils;
 import net.sf.javascribe.patterns.servlet.SingleUrlService;
 import net.sf.javascribe.patterns.servlet.UrlWebServiceType;
 
 @Scannable
 @Processor
 public class WsClientProcessor {
 
 	private static final Logger log = Logger.getLogger(WsClientProcessor.class);
 
 	@ProcessorMethod(componentClass=WsClient.class)
 	public void process(WsClient comp,ProcessorContext ctx) throws JavascribeException {
 
 		if ((comp.getPageName()==null) || (comp.getPageName().trim().length()==0)) {
 			throw new JavascribeException("Found a web service client with no pageName");
 		}
 		if ((comp.getModule()==null) || (comp.getModule().trim().length()==0)) {
 			throw new JavascribeException("Found a web service client with no module");
 		}
 		if ((comp.getService()==null) || (comp.getService().trim().length()==0)) {
 			throw new JavascribeException("Found a web service client with no service");
 		}
 
 		String pageName = comp.getPageName();
 
 		ctx.setLanguageSupport("Javascript");
 		
 		log.info("Processing WSClient on page '"+pageName+"': "+comp.getModule()+"."+comp.getService());
 		JavascriptSourceFile src = JavascriptUtils.getSourceFile(ctx);
 		StringBuilder code = src.getSource();
 		
 		JavascriptVariableType pageType = PageUtils.getPageType(ctx, pageName);
 		JavascriptVariableType modelType = PageUtils.getModelType(ctx, pageName);
 		HashMap<String,String> modelAttributes = PageUtils.getModelAttributes(ctx, pageName);
 		
 		if (modelType==null) {
 			throw new JavascribeException("Page '"+pageName+"' is required to have a model.");
 		}
 		
 		ctx.setLanguageSupport("Java");
 		SingleUrlService srv = findUrlService(ctx,comp.getModule(),comp.getService());
 		if (srv==null) {
 			throw new JavascribeException("Couldn't find URL service "+comp.getModule()+'/'+comp.getService());
 		}
 		JavaBeanType beanType = (JavaBeanType)ctx.getType(srv.getReturnType());
 		for(String att : beanType.getAttributeNames()) {
 			String type = ctx.getAttributeType(att);
 			if (type==null) type = "var";
 			if (modelType.getAttributeType(att)==null) {
 				PageModelProcessor.addModelAttribute(modelType,modelAttributes,att,type,code,null,pageName);
 			}
 		}
 
 		ctx.setLanguageSupport("Javascript");
 		
 		StringBuilder funcBody = new StringBuilder();
 		StringBuilder funcDec = new StringBuilder();
 		StringBuilder url = new StringBuilder();
 		StringBuilder dataString = new StringBuilder();
 		StringBuilder successFunc = new StringBuilder();
 		
 		funcDec.append("function (");
 		funcBody.append("$.ajax({\ncontext:document,\ndataType:'json',\n");
 		
 		// For query parameters and path parameters, look for them in the model object.  If they 
 		// are not there, add them as parameters to the function.
 		
 		dataString.append("{");
 		boolean firstData = true;
 		boolean firstParam = true;
 		// First query parameters
 		for(String p : srv.getQueryParams()) {
 			String modelAttrib = null;
 			if (!firstData) dataString.append(',');
 			else firstData = false;
 
 			if (modelAttributes.get(p)!=null) modelAttrib = p;
 			else {
 				for(String name : modelAttributes.keySet()) {
 					String type = ctx.getAttributeType(name);
					if (type==null) {
						throw new JavascribeException("Could find a type for model attribute '"+name+"'");
					}
 					VariableType t = ctx.getType(type);
 					if (t instanceof JavascriptVariableType) continue;
 					if (!(t instanceof AttributeHolder)) continue;
 					AttributeHolder h = (AttributeHolder)t;
 					if (h.getAttributeType(p)!=null) {
 						modelAttrib = name+'.'+p;
 						break;
 					}
 				}
 			}
 			
 
 			dataString.append(p+":");
 			if (modelAttrib==null) {
 				if (firstParam) firstParam = false;
 				else funcDec.append(',');
 				funcDec.append(p);
 				dataString.append(p);
 			} else {
 				dataString.append("this.model."+BinderUtils.getGetter(modelAttrib));
 //				dataString.append("this.model.get"+(Character.toUpperCase(p.charAt(0)))+p.substring(1)+"()");
 			}
 		}
 		dataString.append('}');
 		url.append(srv.getPath().substring(1));
 		// Add URL parameters
 
 		funcDec.append(") {\n");
 
 		// Build success function
 		successFunc.append("function success(data) {\n");
 
 		for(String n : beanType.getAttributeNames()) {
 			successFunc.append("this.model.set"+Character.toUpperCase(n.charAt(0))+n.substring(1)+"(data."+n+");\n");
 		}
 		if ((comp.getCompleteEvent()!=null) && (comp.getCompleteEvent().trim().length()>0)) {
 			successFunc.append(pageName+".controller.dispatch(\""+comp.getCompleteEvent()+"\");\n");
 		}
 		successFunc.append("}.bind("+comp.getPageName()+")\n");
 
 		if (comp.getFn()==null) {
 			throw new JavascribeException("WsClient component requires attribute 'fn'");
 		}
 		pageType.addFunctionAttribute(comp.getFn());
 		code.append(comp.getPageName()+"."+comp.getFn()+" = ");
 		// Append code to the javascript file
 
 		code.append(funcDec.toString());
 		code.append(funcBody.toString());
 		code.append("success:"+successFunc.toString()+",\n");
 		code.append("data:"+dataString.toString()+",\n");
 		code.append("url:'"+url.toString()+"'\n");
 		code.append("});\n");
 		code.append("}.bind("+comp.getPageName()+");\n");
 	}
 
 	private SingleUrlService findUrlService(ProcessorContext ctx,String module,String service) throws JavascribeException {
 		
 		SingleUrlService ret = null;
 		UrlWebServiceType srv = null;
 		String name = '/'+module+'/'+service;
 
 		if (!(ctx.getType(module) instanceof UrlWebServiceType)) {
 			throw new JavascribeException("Could not find web service module '"+module+"' or it is not a web service");
 		}
 		srv = (UrlWebServiceType)ctx.getType(module);
 		ret = srv.getServices().get(name);
 
 		return ret;
 	}
 	
 }
 
