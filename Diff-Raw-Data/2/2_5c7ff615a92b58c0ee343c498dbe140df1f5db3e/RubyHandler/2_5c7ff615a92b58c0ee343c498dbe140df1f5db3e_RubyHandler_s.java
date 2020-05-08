 package org.microsauce.gravy.context.ruby;
 
 import java.lang.reflect.Proxy;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.FilterChain;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.jruby.RubyObject;
 import org.jruby.embed.ScriptingContainer;
 import org.microsauce.gravy.context.Handler;
 import org.microsauce.gravy.context.HandlerBinding;
 import org.microsauce.gravy.lang.object.CommonObject;
 import org.microsauce.gravy.lang.object.GravyType;
 import org.microsauce.gravy.module.Module;
 import org.microsauce.gravy.runtime.patch.GravyHttpServletRequest;
 import org.microsauce.gravy.runtime.patch.GravyHttpServletResponse;
 import org.microsauce.gravy.runtime.patch.GravyHttpSession;
 import org.microsauce.gravy.runtime.patch.GravyRequestProxy;
 import org.microsauce.gravy.runtime.patch.GravyResponseProxy;
 import org.microsauce.gravy.runtime.patch.GravySessionProxy;
 
 /**
  * 
  * @author microsuace
  *
  */
 class RubyHandler extends Handler {
 
 	ScriptingContainer container;
 	RubyObject callBack;
 	
 	RubyHandler(RubyObject callBack, ScriptingContainer container) {
 		this.container = container;
 		this.callBack = callBack;
 	}
 	
 	public Object doExecute(Object params) {
 		return container.callMethod(callBack, "invoke_handler", params);
 	}
 
 	@Override
 	public Object doExecute(HttpServletRequest req, HttpServletResponse res,
 			FilterChain chain, HandlerBinding handlerBinding) {
 			Map<String, Object> objectBinding = null;
 			if ( handlerBinding.getJson() != null ) { 
 				objectBinding = new HashMap<String, Object>(); 
 				CommonObject json = new CommonObject(null, GravyType.RUBY);
 				json.setSerializedRepresentation(handlerBinding.getJson());
 				objectBinding.put("json", json.toNative());
 			}
 			GravyHttpSession jsSess = patchSession(req, module);
 			GravyHttpServletRequest jsReq = patchRequest(req, res, jsSess, chain, module);
 			GravyHttpServletResponse jsRes = patchResponse(req, res, module);
 
 			return container.callMethod(callBack, "invoke", 
					new Object[] {/*callBack,*/ jsReq, jsRes, handlerBinding.getParamMap(), handlerBinding.getParamList(), objectBinding});
 	}
 	
 	protected GravyType context() {
 		return GravyType.RUBY;
 	}
 			
 	GravyHttpServletRequest patchRequest(HttpServletRequest req, HttpServletResponse res, GravyHttpSession sess, FilterChain chain, Module module) {
 		GravyHttpServletRequest rbReq = (GravyHttpServletRequest) Proxy.newProxyInstance(
 			this.getClass().getClassLoader(),
 			new Class[] {GravyHttpServletRequest.class},
 			new RubyRequestProxy(req, res, sess, chain, module));
 		return rbReq;
 	}
 	GravyHttpServletResponse patchResponse(HttpServletRequest req, HttpServletResponse res, Module module) {
 		GravyHttpServletResponse rbRes = (GravyHttpServletResponse)Proxy.newProxyInstance(
 			this.getClass().getClassLoader(),
 			new Class[] {GravyHttpServletResponse.class},
 			new RubyResponseProxy(res, req, module.getRenderUri(), module));
 		return rbRes;
 	}
 	GravyHttpSession patchSession(HttpServletRequest req, Module module) {
 		GravyHttpSession rbSess = (GravyHttpSession)Proxy.newProxyInstance(
 			this.getClass().getClassLoader(),
 			new Class[] {GravyHttpSession.class},
 			new RubySessionProxy(req.getSession(), module));
 		return rbSess;
 	}
 
 	class RubyResponseProxy<T extends HttpServletResponse> extends GravyResponseProxy {
 
 		RubyResponseProxy(HttpServletResponse res,
 				HttpServletRequest request, String renderUri, Module module) {
 			super(res, request, renderUri, module);
 		}
 				
 		protected GravyType context() {
 			return GravyType.RUBY;
 		}
 	}
 	
 	class RubySessionProxy<T extends HttpSession> extends GravySessionProxy {
 		
 		public RubySessionProxy(Object target, Module module) {
 			super(target, module);
 		}
 
 		protected GravyType context() {
 			return GravyType.RUBY;
 		}
 
 	} 
 	
 	class RubyRequestProxy<T extends HttpServletRequest> extends GravyRequestProxy {
 		
 		public RubyRequestProxy(Object target, HttpServletResponse res,
 				HttpSession session, FilterChain chain, Module module) {
 			super(target, res, session, chain, module);
 		}
 				
 		protected GravyType context() {
 			return GravyType.RUBY;
 		}
 	}
 	
 }
