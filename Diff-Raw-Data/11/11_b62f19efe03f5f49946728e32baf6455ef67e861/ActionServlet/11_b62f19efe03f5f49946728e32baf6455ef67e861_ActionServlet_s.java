 package com.lavans.lacoder2.controller;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletRequestWrapper;
 import javax.servlet.http.HttpServletResponse;
 
 
 import org.apache.commons.lang3.time.StopWatch;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.lavans.lacoder2.di.BeanManager;
 import com.lavans.lacoder2.lang.StringUtils;
 import com.lavans.lacoder2.util.ParameterUtils;
 
 /**
  * lacoder main servlet
  * @author dobashi
  *
  */
 @WebServlet(urlPatterns={"/as/*"})
 public class ActionServlet extends HttpServlet {
 	//public static final String EXCEPTION="lacoder_exception";
 	/** logger */
 	private static final Logger logger = LoggerFactory.getLogger(ActionServlet.class);
 
 	/**	serial id */
 	private static final long serialVersionUID = 1L;
 
 	/** Action configuration. */
 	private WebAppConfig webAppConfig = BeanManager.getBean(WebAppConfig.class);
 
 	/**
 	 * doPost.
 	 * for parse POST string and dispose prev GET string,
 	 * this method decodes requestBody from InputStream instead of tomcat's default parse.
 	 *
 	 * GETで画面表示した後にform画面が来た場合、formタグのactionになにも記載しないとブラウザは前回のURLとして
 	 * "?"以降も使用するため、直前のGETパラメータがquery-stringとして残ってしまう。これをrequest.getParamterMap()
 	 * すると前回のGETパラメータと今回のPOSTパラメータの両方が取得できる。これを避けるため
 	 * query-string破棄、自前でRequestBodyをInputStream経由で取得する。
 	 *
 	 * 文字コードはParameterUtils.toMap()で行うのでrequest.setCharacterEncoding(env)は使わない。
 	 *
 	 *
 	 */
 	@Override
 	protected void doPost(HttpServletRequest requestOrg, HttpServletResponse response)
 	throws ServletException, IOException {
 		BufferedReader br = new BufferedReader(new InputStreamReader(requestOrg.getInputStream(),"ISO-8859-1"));
 		String query="";
 
 		try {
 			String line;
 			while((line = br.readLine()) != (null)){
 				query += '\n'+line;
 			}
 			br.close();
 			if(query.startsWith("\n")) query = query.substring(1);
 		}catch (IOException e){
 			throw new RuntimeException(e);
 		}
 
 		// wrap for
 		HttpRequestParamWrapper request= new HttpRequestParamWrapper(requestOrg);
 
 		// ParameterMap contains prev GET query string, when <form action=""> is empty.
 		// remove them all.
 		request.parameterMap.clear();
 		// put this time POST string.
 		request.parameterMap.putAll(ParameterUtils.toMap(query, webAppConfig.encoding));
 
 		doService(request, response);
 	}
 
 	/**
 	 * doGet.
 	 * for HttpRequestParamWrapper, GET strings are also decoded.
 	 * then useBodyEncodingforURI is not need to read GET string.
 	 *
 	 */
 	@Override
 	protected void doGet(HttpServletRequest requestOrg, HttpServletResponse response)
 			throws ServletException, IOException {
 		// wrap for parameter edit
 		HttpRequestParamWrapper request= new HttpRequestParamWrapper(requestOrg);
 		if(requestOrg.getQueryString()!=null){
 			request.parameterMap.clear();
 			request.parameterMap.putAll(ParameterUtils.toMap(requestOrg.getQueryString(), webAppConfig.encoding));
 		}
 		doService(request, response);
 	}
 
 	/**
 	 * doService
 	 *
 	 * @param request
 	 * @param response
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	protected void doService(HttpRequestParamWrapper request, HttpServletResponse response)
 		throws ServletException, IOException {
 		// for <jsp:param>
 		request.setCharacterEncoding(webAppConfig.encoding);
 		// for default error page
 		response.setCharacterEncoding(webAppConfig.encoding);
 
 		// Calc time for log.
 		StopWatch stopWatch = new StopWatch();
 		stopWatch.start();
 
 		// get action
 		String actionURI = getActionURI(request);
 		if(StringUtils.isEmpty(actionURI)){
 			getServletContext().getRequestDispatcher(request.getPathInfo()).forward(request, response);
 			return;
 		}
 		logger.debug("action.uri="+actionURI);
 		webAppConfig.accessLogger.preLog(request, actionURI);
 
 		try {
 			// ActionInfo取得
 			ActionInfo info = ActionInfo.getInfo(actionURI);
 
 			// 前処理
 			preAction(info, request, response);
 			if(response.isCommitted()){
 //				accessLogger.log("(filtered)" + actionURI, 0, request);
 				return;
 			}
 
 			Object action = info.createAction();
 
 			// request/responseセット
 			setContext(action, request, response);
 
 			//long starttime2 = System.currentTimeMillis();
 			logger.debug("\n===================== "+ info.actionName + ActionInfo.METHOD_SPLITTER + info.methodName +"() start =====================");
 			String jspFile = (String)info.method.invoke(action);
 			logger.debug("\n===================== "+ info.actionName + ActionInfo.METHOD_SPLITTER + info.methodName +"() end =====================");
 
 			jspFile = postAction(action, info, request, response, jspFile);
 
 			if(!response.isCommitted()){
 				getServletContext().getRequestDispatcher(webAppConfig.jspPath+info.relativePath+"/"+jspFile).forward(request, response);
 			}
 		} catch (Exception e) {
 
 			handleException(e, request, response);
 		}finally{
 			// log URI, execute time, request parameters.
 			stopWatch.stop();
 			webAppConfig.accessLogger.log(request, actionURI, stopWatch.getTime());
 			response.flushBuffer();
 		}
 	}
 
 	/**
 	 * Get ActionURI from request.
 	 *
 	 * @param request
 	 * @return
 	 */
 	private String getActionURI(HttpRequestParamWrapper request){
 		// DynamicMethodInvocation
 		// "action:"で始まる指定があればそれを優先。form等でaction先を変更できる。
 		String actionURI = getActionFromParam(request);
 		if(StringUtils.isEmpty(actionURI)){
 			// パラメータ指定がなければRequestURIから。
 			actionURI = getActionFromPath(request);
 		}
 		return actionURI;
 	}
 
 	/**
 	 * パラメータからAction!Method取得、
 	 * "action:"で始まるパラメータがあれば取得。無ければnull.
 	 *
 	 * @param req
 	 * @return
 	 */
 	private String getActionFromParam(HttpRequestParamWrapper request){
 		// get parameter starts with "action:"
 		String action = getParamStartsWithAction(request.parameterMap);
 
 		// no "action:" in parameters
 		if(action==null){
 			return null;
 		}
 
 		logger.debug("action :"+action);
 		// get query string;
 		action = getQueryString(request, action);
 		// relative check
 		action = setRelative(request, action);
 		// ".." check
 		action = checkParent(request, action);
 
 		return action;
 	}
 
 	/**
 	 * Find paramater starts with "action" from parameterMap.
 	 *
 	 * @param parameterMap
 	 * @return
 	 */
 	private String getParamStartsWithAction(Map<String, String[]> parameterMap){
 		String result=null;
 		Iterator<String> ite = parameterMap.keySet().iterator();
 		while(ite.hasNext()){
 			String parameterName = ite.next();
 			// find parameter starting with "action:"
 			if(parameterName.startsWith("action:")){
 				// remove this parameter
 				ite.remove();
 				result = parameterName.substring("action:".length());;
 				break;
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get query string.
 	 *
 	 * @param request
 	 * @param action
 	 * @return
 	 */
 	private String getQueryString(HttpRequestParamWrapper request, String action){
 		if(action.contains("?")){
 			String params = action.split("\\?")[1];
 			request.parameterMap.putAll(ParameterUtils.toMap(params));
 			action  = action.split("\\?")[0];
 		}
 		return action;
 	}
 
 	/**
 	 * if action starts with not "/", the next action path is relative path from prev action.
 	 *
 	 * @param action
 	 * @return
 	 */
 	private String setRelative(HttpServletRequest request, String action){
 		if(!action.startsWith("/")){
 			String requestURI = request.getRequestURI();
 			int beginIndex = requestURI.indexOf(request.getServletPath()) + request.getServletPath().length();
 			int lastIndex = requestURI.lastIndexOf("/");
 			action = requestURI.substring(beginIndex, lastIndex) +"/"+ action;
 		}
 		return action;
 	}
 
 	/**
 	 * if action path contains "..", shorten path.
 	 *
 	 * @param request
 	 * @param action
 	 * @return
 	 */
 	private String checkParent(HttpServletRequest request, String action){
 		while(action.contains("../")){
 			int index = action.indexOf("../");
 			String first = action.substring(0,index-1);
 			first = first.substring(0, first.lastIndexOf("/"));
 			String second = action.substring(index+2, action.length());
 
 			action = first + second;
 			logger.debug(action);
 		}
 		return action;
 	}
 
 	/**
 	 * Get Action URI from request path.
 	 *
 	 * @param request
 	 * @return
 	 */
 	private String getActionFromPath(HttpServletRequest request){
		String actionURI = request.getPathInfo();
 		// Check whether action URI end with action-extention
 		String extension = actionURI.contains(".")?
 				actionURI.substring(actionURI.indexOf(".")+1):"";
 		if(!webAppConfig.extenstions.contains(extension)){
 			return null;
 		}
 
 		// remove extension
 		actionURI = actionURI.substring(0, actionURI.length()-(extension.length()+1));
 
 		return actionURI;
 	}
 
 
 	/**
 	 *
 	 * @param info
 	 * @param request
 	 * @param response
 	 * @return true: if response is already commited then true. Do not write any more.
 	 * @throws ServletException
 	 * @throws IOException
 	 */
 	private void preAction(ActionInfo info, HttpServletRequest request, HttpServletResponse response) throws Exception{
 		// filter#preAction
 		for(ActionFilter filter: info.filterList){
 			filter.preAction(request, response, info);
 		}
 	}
 
 	/**
 	 * Set Context info.
 	 *
 	 * @param info
 	 * @param request
 	 * @param response
 	 * @throws Exception
 	 */
 	private void setContext(Object action, HttpServletRequest request, HttpServletResponse response) throws Exception{
 		// Set request & response.
 		ActionSupport actionSupport = null;
 		if(action instanceof ActionSupport){
 			actionSupport = (ActionSupport)action;
 			actionSupport.setRequest(request);
 			actionSupport.setResponse(response);
 		}
 	}
 
 	/**
 	 * do somethig after method executed.
 	 * @param action
 	 */
 	private String  postAction(Object action, ActionInfo info, HttpRequestParamWrapper request, HttpServletResponse response, String jspFile) throws Exception{
 		// ActionSupport
 		if(action instanceof ActionSupport){
 			ActionSupport actionSupport = (ActionSupport)action;
 			// do post method action
 			request.setAttribute("actionMessages", actionSupport.getActionMessages());
 			request.setAttribute("actionErrors", actionSupport.getActionErrors());
 			request.setAttribute("fieldErrors", actionSupport.getFieldErrors());
 			// check chain action.
 			if(!StringUtils.isEmpty(actionSupport.getChainAction())){
 				// to remove "ChainAction", request parameter must be modifiable
 				request.parameterMap.put(actionSupport.getChainAction(), new String[]{""});
 				// for action, it must be unmodifiable.
 				doService(request, response);
 			}
 		}
 
 		// filter#preAction
 		for(ActionFilter filter: info.filterList){
 			jspFile = filter.postAction(request, response, jspFile);
 		}
 
 		return jspFile;
 	}
 
 	/**
 	 * Handle Exception
 	 * @param e
 	 * @throws ServletException
 	 */
 	private void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws ServletException{
 		// Get nested Exception
 		Throwable t = e;
 		while(t.getCause()!=null){
 			t = t.getCause();
 		}
 
 		try {
 			// handler exception
 			webAppConfig.exceptionHandler.handle(request, response, t);
 		} catch (Exception e2) {
 			// ログファイルに出力
 //			logger.info(accessLogger.log(actionURI, 0, request));
 			logger.error("Exception handle error.",e2);
 			throw new ServletException(e2);
 		}
 	}
 
 	/**
 	 * wrap for temporary access to request parameters.
 	 * @author dobashi
 	 *
 	 */
 	protected class HttpRequestParamWrapper extends HttpServletRequestWrapper {
 		private Map<String, String[]> parameterMap;
 		public HttpRequestParamWrapper(HttpServletRequest request) {
 			super(request);
 			parameterMap = new HashMap<String, String[]>(request.getParameterMap());
 		}
 
 		@Override
 		public Map<String, String[]> getParameterMap() {
 			return Collections.unmodifiableMap(parameterMap);
 		}
 		/* (非 Javadoc)
 		 * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
 		 */
 		@Override
 		public String getParameter(String name) {
 			if(parameterMap.containsKey(name)){
 				return parameterMap.get(name)[0];
 			}
 			// <jsp:param> adds parameter after parse.
 			return super.getParameter(name);
 		}
 
 		/* (非 Javadoc)
 		 * @see javax.servlet.ServletRequestWrapper#getParameterNames()
 		 */
 		@Override
 		public Enumeration<String> getParameterNames() {
 			Enumeration<String> e = new Enumeration<String>() {
 				Iterator<String> ite = parameterMap.keySet().iterator();
 
 				@Override
 				public boolean hasMoreElements() {
 					return ite.hasNext();
 				}
 				@Override
 				public String nextElement() {
 					return ite.next();
 				}
 
 			};
 			return e;
 		}
 
 		/* (非 Javadoc)
 		 * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
 		 */
 		@Override
 		public String[] getParameterValues(String name) {
 			return parameterMap.get(name);
 		}
 	}
 }
 
