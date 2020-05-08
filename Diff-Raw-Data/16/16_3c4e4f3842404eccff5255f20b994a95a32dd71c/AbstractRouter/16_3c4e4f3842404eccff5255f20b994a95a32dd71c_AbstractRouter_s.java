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
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import net.moetang.turismo_plus.pipeline.actionresult.ActionResult;
 import net.moetang.turismo_plus.pipeline.actionresult.ContinueResult;
 import net.moetang.turismo_plus.pipeline.actionresult.FwOrIncResult;
 import net.moetang.turismo_plus.pipeline.actionresult.NotFoundResult;
 import net.moetang.turismo_plus.pipeline.actionresult.SuccessResult;
 import net.moetang.turismo_plus.pipeline.processing.Action;
 import net.moetang.turismo_plus.pipeline.processing.AfterAll;
 import net.moetang.turismo_plus.pipeline.processing.BeforeAll;
 import net.moetang.turismo_plus.pipeline.processing.IAction;
 import net.moetang.turismo_plus.pipeline.processing.Resolver;
 import net.moetang.turismo_plus.pipeline.routing.Condition;
 import net.moetang.turismo_plus.util.AfterChain;
 import net.moetang.turismo_plus.util.BeforeChain;
 import net.moetang.turismo_plus.util.Env;
 import net.moetang.turismo_plus.util.PathMapper;
 import net.moetang.turismo_plus.util.UrlUtils;
 import net.moetang.turismo_plus.util.UrlUtils.PathEntry;
 
 public abstract class AbstractRouter implements Router {
 	public AbstractRouter() {
 		map();
 		makeResolver();
 	}
 
 	@Override
 	public final Resolver resolver() {
 		return resolver;
 	}
 	
 	private Resolver resolver;
 	
 	protected abstract void map();
 
 	//===============================================
 	private String g_prefix;
 	private int prefixCutIdx;
 	//map all url with 'prefix' uri - means uri like : prefix/*
 	//no wildcard or regex support
 	//should start with '/'
 	//when use _custom to map uri will map prefix/#uri#
 	protected final void _prefix(final String prefix){
 		if(prefix.endsWith("/")){
 			this.g_prefix = prefix;
 		}
 		else{
 			this.g_prefix = prefix+"/";
 		}
		prefixCutIdx = prefix.length()-1;
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
 	private List<BeforeAll> befores = new ArrayList<>();
 	private List<AfterAll> afters = new ArrayList<>();
 	protected final void _before(final BeforeAll before){
 		if (this.resolver == null) {
 			befores.add(before);
 		}
 	}
 	protected final void _after(final AfterAll after){
 		if (this.resolver == null) {
 			afters.add(after);
 		}
 	}
 	//===============================================
 	private static final UrlUtils urlUtils = new UrlUtils();
 	private PathMapper pathMapper = new PathMapper();
 	protected final void _custom(final String method, final String path, final Condition[] conditions, final IAction action) {
 		if (this.resolver == null) {
 			List<PathEntry> paths = urlUtils.uriToPathEntry(path);
 			pathMapper.add(method, paths, conditions, action);
 		}
     }
 	//===============================================
 	private IAction defaultAction;
 	//only once
 	protected final void _default(final IAction action){
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
 	protected final void get(final String path, final Condition[] conditions, final IAction action) {
 		_custom(GET, path, conditions, action);
     }
 	protected final void post(final String path, final Condition[] conditions, final IAction action) {
 		_custom(POST, path, conditions, action);
     }
 	protected final void put(final String path, final Condition[] conditions, final IAction action) {
 		_custom(PUT, path, conditions, action);
     }
 	protected final void head(final String path, final Condition[] conditions, final IAction action) {
 		_custom(HEAD, path, conditions, action);
     }
 	protected final void options(final String path, final Condition[] conditions, final IAction action) {
 		_custom(OPTIONS, path, conditions, action);
     }
 	protected final void delete(final String path, final Condition[] conditions, final IAction action) {
 		_custom(DELETE, path, conditions, action);
     }
 	protected final void trace(final String path, final Condition[] conditions, final IAction action) {
 		_custom(TRACE, path, conditions, action);
     }
     // Shortcuts methods
 	protected final void get(final String path, final IAction action) {
 		get(path, new Condition[0], action);
     }
 	protected final void post(final String path, final IAction action) {
 		post(path, new Condition[0], action);
     }
 	protected final void put(final String path, final IAction action) {
 		put(path, new Condition[0], action);
     }
 	protected final void head(final String path, final IAction action) {
 		head(path, new Condition[0], action);
     }
 	protected final void options(final String path, final IAction action) {
 		options(path, new Condition[0], action);
     }
 	protected final void delete(final String path, final IAction action) {
 		delete(path, new Condition[0], action);
     }
 	protected final void trace(final String path, final IAction action) {
 		trace(path, new Condition[0], action);
     }
 	protected final void _custom(final String method, final String path, final IAction action){
 		_custom(method, path, new Condition[0], action);
 	}
 
     protected static final String POST = "POST";
     protected static final String GET = "GET";
     protected static final String HEAD = "HEAD";
     protected static final String OPTIONS = "OPTIONS";
     protected static final String PUT = "PUT";
     protected static final String DELETE = "DELETE";
     protected static final String TRACE = "TRACE";
 
 	private void makeResolver() {
 		if(resolver == null){
 			this.resolver = new ResolverImpl();
 		}
 	}
 
 	private DoChain createAfterChain(){
 		return new AfterChainImpl();
 	}
 	private interface DoChain{
 		public void doChain(Env env);
 	}
 	private class AfterChainImpl implements AfterChain, DoChain{
 		private List<AfterAll> list = afters;
 		private int i = 0;
 		private int length = list.size();
 		private boolean stopAfter = false;
 		@Override
 		public void stopAfters() {
 			this.stopAfter = true;
 		}
 		@Override
 		public void doNext() {
 		}
 		@Override
 		public void doChain(Env env) {
 			for(AfterAll a : list){
 				if(stopAfter || i < length)
 					return;
 				a.doAfter(env, this);
 			}
 		}
 	}
 	private DoChain createActionChain(final IAction action){
 		return new BeforeChainImpl(action);
 	}
 	private class BeforeChainImpl implements BeforeChain, DoChain{
 		private IAction action;
 		private List<BeforeAll> beforeList = befores;
 		private int i = 0;
 		private int length = befores.size();
 		private boolean stopAll = false;
 		private boolean stopAction = false;
 		private boolean stopBefores = false;
 
 		public BeforeChainImpl(IAction action) {
 			this.action = action;
 		}
 		@Override
 		public void doChain(Env env) {
 			for(BeforeAll b : beforeList){
 				if(stopAll)
 					return;
 				if(stopBefores || i < length)
 					break;
 				b.doBefore(env, this);
 			}
 			if(!stopAction){
 				this.action.doAction(env);
 			}
 			if(afters.size() != 0){
 				createAfterChain().doChain(env);
 			}
 		}
 		@Override
 		public void doNext() {
 		}
 		@Override
 		public void stopBefores() {
 			this.stopBefores = true;
 		}
 		@Override
 		public void stopAction() {
 			this.stopAction = true;
 			this.stopBefores = true;
 		}
 		@Override
 		public void stopAll() {
 			this.stopAll = true;
 		}
 		
 	}
 	private static final ActionResult SUCCESS_RESULT = new SuccessResult();
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
 					uri = uri.substring(prefixCutIdx);
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
 			IAction action = pathMapper.getAction(method, urlUtils.uriToPathEntry(uri));
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
 				if(befores.size() != 0){
 					createActionChain(action).doChain(env);
 				}else{
 					action.doAction(env);
 					if(afters.size() != 0){
 						createAfterChain().doChain(env);
 					}
 				}
 				// finally : success or found
 				return SUCCESS_RESULT;
 			}
 		}
 	}
 	private static final ActionResult NO_MAPPING = new NotFoundResult() {
 		@Override
 		public void doResult() {
 			Env._doChain();
 		}
 	};
 
 }
