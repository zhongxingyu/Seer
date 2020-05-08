 /*
  * Copyright (c) 2013 goodplayer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package net.moetang.turismo_plus.util;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.moetang.turismo_plus.pipeline.actionresult.ActionResult;
 import net.moetang.turismo_plus.pipeline.router.Router;
 
 public class Env {
     private static ThreadLocal<Env> locals = new ThreadLocal<Env>();
 
     private Env(HttpServletRequest request,
 			HttpServletResponse response, FilterChain filterChain) {
     	this.request = request;
     	this.response = response;
    	this.filterChain = filterChain;
 	}
 
 	public static void createReq(HttpServletRequest request,
 			HttpServletResponse response, FilterChain filterChain) {
 		locals.set(new Env(request, response, filterChain));
 	}
 
 	public static void endCurReq() {
 		locals.remove();
 	}
 
 	public static void doReq(List<Router> routerList) {
 		HttpServletRequest r = Env._req();
 		String uri = r.getRequestURI();
 		String method = r.getMethod();
 		get().curRouters = routerList;
 		doReq(routerList, method, uri);
 	}
 
 	public static void doReq(String method, String uri) {
 		doReq(get().curRouters, method, uri);
 	}
 
 	public static void doReq(List<Router> routerList, String method, String uri){
 		ActionResult ar = null;
 		ActionResult ar2 = null;
 		for(Router router : routerList){
 			//need to handle exception
 			try {
 				ar = router.resolver().resolve(method, uri);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			ar2 = Env._getResult();
 			if(ar2 == null){
 				if(ar != null){
 					Env._setResult(ar);
 					return ;
 				}
 			}else{
 				return ;
 			}
 		}
 		//when no result or usually no router maps this req
 //		if(Env.getResult()==null){
 //		}
 	}
 
 	private static void clearResult() {
 		get().actionResult = null;
 	}
 
 	public static void doResult(ActionResult result) {
 		if(result != null){
 			Env.clearResult();
 			result.doResult();
 		}
 	}
 
     public static Env get() {
         return locals.get();
     }
     
     private HttpServletRequest request;
     private HttpServletResponse response;
     private FilterChain filterChain;
     private ActionResult actionResult;
     private List<Router> curRouters;
     private Map<String, String> params =  new HashMap<>();
 
 	public static ActionResult _getResult(){
 		return get().actionResult;
 	}
 	public ActionResult getResult(){
 		return actionResult;
 	}
 	
 	public static void _setResult(ActionResult actionResult){
 		get().actionResult = actionResult;
 	}
 	public void setResult(ActionResult actionResult){
 		this.actionResult = actionResult;
 	}
 	
 	/**
 	 * push request to next one , no matter when there is another handler in the container's handle chain
 	 */
 	public static void _doChain(){
 		try {
 			get().filterChain.doFilter(get().request, get().response);
 		} catch (IOException | ServletException e) {
 			e.printStackTrace();
 		}
 	}
 	public void doChain(){
 		try {
 			filterChain.doFilter(request, response);
 		} catch (IOException | ServletException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void _setParam(String key, String value) {
 		get().params.put(key, value);
 	}
 	public static String _getParam(String key){
         Map<String, String> params2 = get().params;
         String string = params2.get(key);
         if(string == null) {
             return _req().getParameter(key);
         }
         return string;
 	}
 	public void setParam(String key, String value) {
 		params.put(key, value);
 	}
 	public String getParam(String key){
         String string = params.get(key);
         if(string == null) {
             return request.getParameter(key);
         }
         return string;
 	}
 	public static String[] _getParamArray(String key){
 		return _req().getParameterValues(key);
 	}
 	public static Object _getAttri(String key){
 		return _req().getAttribute(key);
 	}
 	public String[] getParamArray(String key){
 		return request.getParameterValues(key);
 	}
 	public Object getAttri(String key){
 		return request.getAttribute(key);
 	}
 
 	public static HttpServletRequest _req() {
 		return get().request;
 	}
 	public HttpServletRequest req() {
 		return request;
 	}
 
 	public static HttpServletResponse _res() {
 		return get().response;
 	}
 	public HttpServletResponse res() {
 		return response;
 	}
 
 	public static ServletContext _ctx() {
 		return get().request.getServletContext();
 	}
 	public ServletContext ctx(){
 		return request.getServletContext();
 	}
 
 }
