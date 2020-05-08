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
 package net.moetang.turismo_plus.pipeline.router;
 
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.moetang.turismo_plus.pipeline.actionresult.ActionResult;
 import net.moetang.turismo_plus.pipeline.actionresult.ContinueResult;
 import net.moetang.turismo_plus.pipeline.actionresult.FwOrIncResult;
 import net.moetang.turismo_plus.pipeline.actionresult.NotFoundResult;
 import net.moetang.turismo_plus.pipeline.actionresult.SuccessResult;
 import net.moetang.turismo_plus.pipeline.processing.Action;
 import net.moetang.turismo_plus.pipeline.processing.IAction;
 import net.moetang.turismo_plus.pipeline.processing.Resolver;
 import net.moetang.turismo_plus.util.Env;
 
 public abstract class FastMapRouter implements Router {
 	
 	public FastMapRouter() {
 		map();
 		this.resolver = new ResolverImpl();
 	}
 	
 	protected abstract void map();
 
 	//===============================================
 	private String g_prefix;
 	private int prefixIdx;
 	protected void _prefix(final String prefix) {
 		if(prefix.endsWith("/")){
 			this.g_prefix = prefix;
 		}
 		else{
 			this.g_prefix = prefix+"/";
 		}
		prefixIdx = prefix.length()-1;
 	}
 	//===============================================
 	private Map<String, Pattern> excludedRegex = new LinkedHashMap<>();
 	private Map<String, IAction> excludedAction = new LinkedHashMap<>();
 	//use RegEx to match uri which will be excluded
 	protected final void _exclude(final String pathRegEx, final IAction action) {
 		if(this.resolver == null){
 			Pattern p = Pattern.compile(pathRegEx);
 			this.excludedRegex.put(pathRegEx, p);
 			this.excludedAction.put(pathRegEx, action);
 		}
 	}
 	//===============================================
 	private Map<String, Map<String, String>> aliasMap = new HashMap<String, Map<String,String>>();
     // Route-alias shortcut methods
 	// no wildcard or regex support
 	// targetPath must with a specific prefix
 	//     - ex. prefix: /app/ , fromPath: /index , targetPath wants to be one with prefix '/other/' and path '/login' , then targetPath must be '/other/login'
 	protected final void _custom(final String method, final String fromPath, final String targetPath){
 		if (this.resolver == null) {
 			Map<String, String> methodMap = aliasMap.get(method);
 			if(methodMap == null){
 				methodMap = new HashMap<>();
 				aliasMap.put(method, methodMap);
 			}
 			methodMap.put(fromPath, targetPath);
 		}
 	}
 	//===============================================
 	private Map<String, Map<String, IAction>> map = new HashMap<>();
 	protected final void _custom(final String method, final String path, final IAction action) {
 		if (this.resolver == null) {
 			Map<String, IAction> methodMap = map.get(method);
 			if(methodMap == null){
 				methodMap = new HashMap<>();
 				map.put(method, methodMap);
 			}
 			methodMap.put(path, action);
 		}
     }
 	//===============================================
 	private IAction defaultAction;
 	//only once
 	protected final void _default(final Action action){
 		if (this.resolver == null) {
 			this.defaultAction = action;
 		}
 	}
 	//===============================================
 
 	protected final void post(final String fromPath, final String targetPath) {
 		this._custom(POST, fromPath, targetPath);
     }
 	protected final void get(final String fromPath, final String targetPath) {
 		this._custom(GET, fromPath, targetPath);
     }
 	protected final void put(final String fromPath, final String targetPath) {
 		this._custom(PUT, fromPath, targetPath);
     }
 	private final IAction continueAction = new Action() {
 		private ActionResult continueResult = new ContinueResult();
 		@Override
 		public void action(Env env) {
 			env.setResult(continueResult);
 		}
 	};
 	/**
 	 * push request to next one to handle
 	 * @param pathRegEx
 	 */
 	protected final void _exclude(final String pathRegEx) {
 		_exclude(pathRegEx, continueAction);
 	}
 	protected final void get(final String path, final IAction action) {
 		_custom(GET, path, action);
     }
 	protected final void post(final String path, final IAction action) {
 		_custom(POST, path, action);
     }
 	protected final void put(final String path, final IAction action) {
 		_custom(PUT, path, action);
     }
 	protected final void head(final String path, final IAction action) {
 		_custom(HEAD, path, action);
     }
 	protected final void options(final String path, final IAction action) {
 		_custom(OPTIONS, path, action);
     }
 	protected final void delete(final String path, final IAction action) {
 		_custom(DELETE, path, action);
     }
 	protected final void trace(final String path, final IAction action) {
 		_custom(TRACE, path, action);
     }
 
 	private Resolver resolver;
 	@Override
 	public Resolver resolver() {
 		return this.resolver;
 	}
 	
 	private class ResolverImpl implements Resolver{
 		private String prefix = g_prefix;
 		private Set<String> excludedUris = excludedRegex.keySet();
 		public ResolverImpl() {
 		}
 		@Override
 		public ActionResult resolve(String method, String uri) {
 			Env env = Env.get();
 			// 1st : check prefix
 			if((prefix != null)){
 				if(!uri.startsWith(prefix))
 					return null;
 				else{
 					uri = uri.substring(prefixIdx);
 				}
 			}
 			// 2nd : check if excluded
 			for(String exclUri : excludedUris){
 				if(excludedRegex.get(exclUri).matcher(uri).matches()){
 					excludedAction.get(exclUri).doAction(env);
 					return env.getResult();
 				}
 			}
 			// 3rd : Route-alias shortcut
 			Map<String, String> methodMap = aliasMap.get(method);
 			if(methodMap != null){
 				String target = methodMap.get(uri);
 				if(target != null){
 					return new FwOrIncResult(method, target);
 				}
 			}
 			// 4th : find action
 			IAction action = null;
 			Map<String, IAction> mm = map.get(method);
 			if(mm != null){
 				action = mm.get(uri);
 			}
 			// 5th : if it can't find any action, do default
 			//       if it can find one action, then do before->action->after
 			if(action == null){
 				if(defaultAction == null){
 					return NO_MAPPING;
 				}else{
 					defaultAction.doAction(env);
 					return env.getResult();
 				}
 			}
 			else{
 				action.doAction(env);
 				// finally : success or found
 				return SUCCESS_RESULT;
 			}
 		}
 	}
 	private static final ActionResult SUCCESS_RESULT = new SuccessResult();
 	private static final ActionResult NO_MAPPING = new NotFoundResult() {
 		@Override
 		public void doResult() {
 			Env._doChain();
 		}
 	};
 
     protected static final String POST = "POST";
     protected static final String GET = "GET";
     protected static final String HEAD = "HEAD";
     protected static final String OPTIONS = "OPTIONS";
     protected static final String PUT = "PUT";
     protected static final String DELETE = "DELETE";
     protected static final String TRACE = "TRACE";
 
 }
