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
 package net.moetang.turismo_plus.filter;
 
 import static com.ghosthack.turismo.util.ClassForName.createInstance;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.Filter;
 import javax.servlet.FilterChain;
 import javax.servlet.FilterConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.moetang.turismo_plus.pipeline.router.Router;
 import net.moetang.turismo_plus.util.Env;
 public class StartPointFilter implements Filter {
 	private List<Router> routerList;
 
 	@Override
 	public final void destroy() {
 	}
 
 	@Override
 	public final void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain filterChain) throws IOException, ServletException {
 		try {
 			Env.createReq((HttpServletRequest)request, (HttpServletResponse)response, filterChain);
 			Env env = Env.get();
 			Env.doReq(routerList);
 			Env.doResult(env.getResult());
 		} finally{
 			Env.endCurReq();
 		}
 	}
 
 	@Override
 	public final void init(FilterConfig filterConfig) throws ServletException {
 		routerList = new ArrayList<>();
         final String routesParam = filterConfig.getInitParameter(ROUTES);
         if(routesParam != null){
             String[] routes = routesParam.split(",");
             for(String route : routes){
         		try {
 					routerList.add(createInstance(route, Router.class));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
         	}
         }else{
         	// way 1st : subclass loads all routers
         	routers();
         }
 	}
 
     private static final String ROUTES = "routes";
     
     protected void routers() {
 		
 	}
    protected final void add(Router router) {
    	routerList.add(router);
 	}
 
 }
