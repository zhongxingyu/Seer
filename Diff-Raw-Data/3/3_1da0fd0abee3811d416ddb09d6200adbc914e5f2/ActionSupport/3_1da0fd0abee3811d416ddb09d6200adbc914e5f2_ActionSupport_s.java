 package com.lavans.lacoder2.controller;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import net.arnx.jsonic.JSON;
 
 import org.apache.commons.lang3.StringUtils;
 import org.w3c.dom.Document;
 
 import com.lavans.lacoder2.controller.util.WriteUtils;
 import com.lavans.lacoder2.di.BeanManager;
 import com.lavans.lacoder2.lang.StringEscapeUtils;
 
 /**
  * ActionSupport
  * Common action functions.
  * Request/Response getter.
  * ActionErrors/FieldErrors/ActionMessages setter.
  * Action chain.
  * Json/Jsop writer.
  * TODO CSV writer.
  *
  * @author
  *
  */
 public class ActionSupport {
 //	private static Logger logger = LogUtils.getLogger();
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 
 	/** action error messages */
 	private final List<String> actionErrors = new ArrayList<String>();
 	/** field error messages.  */
 	private final Map<String, String> fieldErrors = new HashMap<String, String>();
 	/** action messages */
 	private final List<String> actionMessages = new ArrayList<String>();
 
 	private String chainAction = null;
 	public String getChainAction() {
 		return chainAction;
 	}
 
 	/**
 	 * Action Chain.
 	 * Call next action within one request.
 	 *
 	 * @param chainAction
 	 */
 	public String chain(String chainAction) {
 		if(!chainAction.startsWith("action:")){
 			chainAction = "action:"+chainAction;
 		}
 		this.chainAction = chainAction;
 		return NO_JSP;
 	}
 
 	/**
 	 * Redirect to other url.
 	 * Call next action with new request.
 	 *
 	 * @param url
 	 * @throws IOException
 	 */
 	public String redirect(String url) {
 		// "/"で始まるならContextPathを足す
 		if(url.startsWith("/")){
 			url = request.getContextPath()+url;
 		}
 		url = response.encodeRedirectURL(url);
 		try {
 			response.sendRedirect(url);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 
 		return NO_JSP;
 	}
 
 	public HttpServletRequest getRequest() {
 		return request;
 	}
 	public void setRequest(HttpServletRequest request) {
 		this.request = request;
 	}
 	public HttpServletResponse getResponse() {
 		return response;
 	}
 	public void setResponse(HttpServletResponse response) {
 		this.response = response;
 	}
 
 	/**
 	 * set request attribute
 	 * @param key
 	 * @param value
 	 */
 	public void setAllAttributes(Map<String, Object> map){
 		for(Entry<String, Object> entry: map.entrySet()){
 			request.setAttribute(entry.getKey(), entry.getValue());
 		}
 	}
 	/**
 	 * set request attribute
 	 * @param key
 	 * @param value
 	 */
 	public void setAttribute(String key, Object value){
 		request.setAttribute(key, value);
 	}
 	/**
 	 * getParameter
 	 * @param key
 	 */
 	public String getParameter(String key){
 		return request.getParameter(key);
 	}
 	public String[] getParameterValues(String key){
 		return request.getParameterValues(key);
 	}
 
 	protected ServletContext getServletContext(){
 		return request.getServletContext();
 	}
 	/**
 	 * Add Action Error.
 	 * @param key
 	 * @param message
 	 */
 	public void addActionError(String message){
 		actionErrors.add(message);
 	}
 	public void addActionErrors(Collection<String> messages){
 		actionErrors.addAll(messages);
 	}
 	public List<String> getActionErrors(){
 		return actionErrors;
 	}
 
 	/*
 	 * @return
 	 */
	protected boolean hasErrors(){
		return actionErrors.size()>0 || fieldErrors.size()>0;
	}
 	protected boolean hasActionErrors(){
 		return actionErrors.size()>0;
 	}
 
 	/**
 	 * Add Field Error.
 	 * @param key
 	 * @param message
 	 */
 	public void addFieldError(String key, String message){
 		fieldErrors.put(key, message);
 	}
 	public Map<String, String> getFieldErrors(){
 		return fieldErrors;
 	}
 	protected boolean hasFieldErrors(){
 		return fieldErrors.size()>0;
 	}
 
 	protected boolean hasErrors(){
 		return hasActionErrors() || hasFieldErrors();
 	}
 
 	/**
 	 * Add Action Message.
 	 * @param message
 	 */
 	public void addActionMessage(String message){
 		actionMessages.add(message);
 	}
 	/**
 	 * Add Action Message.
 	 * @param messages
 	 */
 	public void addActionMessages(Collection<String> messages){
 		actionMessages.addAll(messages);
 	}
 	/**
 	 * Get action message.
 	 * @param message
 	 */
 	public List<String> getActionMessages(){
 		return actionMessages;
 	}
 
 	/** Return parameter from action method will ignored when response is commited. */
 	private final String NO_JSP=null;
 
 	/**
 	 * json
 	 *
 	 * @param data
 	 * @return
 	 * @throws IOException
 	 */
 	protected String json(Object data){
 //		Stopwatch stopwatch = new Stopwatch().start();
 		String str = JSON.encode(data);
 //		logger.debug("json():Object"+DateUtils.prettyFormat(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
 		return json(str);
 	}
 	protected String json(String jsonString){
 //		Stopwatch stopwatch = new Stopwatch().start();
 		WriteUtils writeUtils = BeanManager.getBean(WriteUtils.class);
 		writeUtils.writeJson(getResponse(), jsonString);
 //		logger.debug("json():String"+DateUtils.prettyFormat(stopwatch.stop().elapsed(TimeUnit.MILLISECONDS)));
 
 		return NO_JSP;
 	}
 
 	/** Default method name of jsonp callback. */
 	private final String DEFAULT_METHOD="callback";
 
 	/**
 	 * jsonp.
 	 * Get callback method name and add to data.
 	 *
 	 * @param data
 	 * @return
 	 * @throws IOException
 	 */
 	protected String jsonp(String data) throws IOException{
 		// Get callback method name
 		String method=getRequest().getParameter("callback");
 		method=StringEscapeUtils.escapeHtml4(method);
 		if(StringUtils.isEmpty(method)){
 			method=DEFAULT_METHOD;
 		}
 
 		// if "jsonp" make it function
 		data = method+"("+data+");";
 
 		return json(data);
 	}
 
 	/**
 	 * image出力
 	 * @param img
 	 * @return
 	 */
 	protected String image(BufferedImage img){
 		WriteUtils writeUtils = BeanManager.getBean(WriteUtils.class);
 		writeUtils.writeImage(getResponse(), img);
 
 		return NO_JSP;
 	}
 
 	/**
 	 * xml出力
 	 * @param document
 	 * @return
 	 */
 	protected String xml(Document document){
 		WriteUtils writeUtils = BeanManager.getBean(WriteUtils.class);
 		writeUtils.writeXml(getResponse(), document);
 
 		return NO_JSP;
 	}
 
 	/**
 	 * binay出力
 	 * @param document
 	 * @return
 	 */
 	protected String binary(String contentType, byte[] data){
 		WriteUtils writeUtils = BeanManager.getBean(WriteUtils.class);
 		writeUtils.writeBytes(getResponse(), contentType,  data);
 
 		return NO_JSP;
 	}
 }
