 /*******************************************************************************
  * Copyright (c) 2013 Juuso Vilmunen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Juuso Vilmunen - initial API and implementation
  ******************************************************************************/
 package waazdoh.client.rest;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import waazdoh.client.MBinarySource;
 import waazdoh.client.MStringID;
 import waazdoh.client.URLCaller;
 import waazdoh.cutils.JBeanResponse;
 import waazdoh.cutils.MID;
 import waazdoh.cutils.MLogger;
 import waazdoh.cutils.MURL;
 import waazdoh.cutils.UserID;
 import waazdoh.cutils.xml.JBean;
 import waazdoh.service.CMService;
 
 public final class RestClient implements CMService {
 	private URL url;
 	private MLogger log = MLogger.getLogger(this);
 	private String sessionid;
 	private UserID userid;
 	private String username;
 	private boolean loggedin;
 	private MBinarySource source;
 
 	public RestClient(String localurl, MBinarySource source)
 			throws MalformedURLException {
 		this.url = new URL(localurl);
 		this.source = source;
 	}
 
 	@Override
 	public String getInfoText() {
 		return "Session:" + sessionid + " User:" + userid;
 	}
 
 	@Override
 	public String requestAppLogin(String email, String appname, MStringID appid) {
 		// @Path("/authenticateapp/{email}/{appid}/{appname}")
 		JBean request = new JBean("request");
 		request.addValue("email", email);
 		request.addValue("appid", appid.toString());
 		request.addValue("appname", appname);
 		//
 		String method = "authenticateapp";
 		JBeanResponse response = post("users", method,
 				new LinkedList<String>(), request);
 		log.info("response " + response);
		JBean responsebean = response.getBean();
		sessionid = responsebean.getValue("sessionid");
		userid = new UserID(responsebean.getValue("user"));
		this.username = email;
		//
 		loggedin = response.isSuccess();
 		if (loggedin) {
 			return sessionid;
 		} else {
 			sessionid = null;
 			userid = null;
 			return null;
 		}
 	}
 
 	@Override
 	public String getSessionID() {
 		return sessionid;
 	}
 
 	@Override
 	public boolean isLoggedIn() {
 		return loggedin;
 	}
 
 	@Override
 	public boolean setSession(String username, String session) {
 		if (session != null && session.length() > 0) {
 			sessionid = session;
 			List<String> params = new LinkedList<String>();
 			params.add(username);
 			JBeanResponse response = get("users", "checksession", true, params);
 			log.info("checksession response " + response);
 			if (response.isSuccess()) {
 				String suserid = response.getBean().find("uid").getText();
 				if (suserid != null) {
 					userid = new UserID(suserid);
 					this.username = username;
 					this.loggedin = true;
 					return true;
 				} else {
 					loggedin = false;
 					sessionid = null;
 					return false;
 				}
 			} else {
 				sessionid = null;
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public String getUsername() {
 		return username;
 	}
 
 	@Override
 	public MURL getURL(String service, String type, MID id) {
 		LinkedList<String> params = new LinkedList<String>();
 		params.add("" + id);
 		return getAnonymousURL(service, type, params);
 	}
 
 	private MURL getAnonymousURL(String service, String string,
 			LinkedList<String> params) {
 		return getAuthURL(service, string, params, null);
 	}
 
 	@Override
 	public JBeanResponse reportDownload(MStringID id, boolean success) {
 		List<String> params = new LinkedList<String>();
 		params.add(sessionid.toString());
 		params.add(id.toString());
 		params.add("" + success);
 		JBeanResponse response = get("objects", "reportdownload", false, params);
 		return response;
 	}
 
 	@Override
 	public JBeanResponse getUser(UserID userid) {
 		JBeanResponse b = source.getBean(userid.toString());
 		if (b == null) {
 			List<String> params = new LinkedList<String>();
 			params.add(userid.toString());
 			b = get("users", "get", true, params);
 			//
 			source.addBean(userid.toString(), b);
 		}
 
 		return b;
 	}
 
 	@Override
 	public JBeanResponse read(MStringID id) {
 		JBeanResponse bean = source.getBean(id.toString());
 		if (bean != null) {
 			return bean;
 		} else {
 			List<String> params = new LinkedList<String>();
 			params.add(id.toString());
 			JBeanResponse response = get("objects", "read", false, params);
 			if (response.isSuccess()) {
 				source.addBean(id.toString(), response);
 			}
 			return response;
 		}
 	}
 
 	@Override
 	public JBeanResponse write(MStringID id, JBean b) {
 		// TODO
 		JBeanResponse resp = new JBeanResponse();
 		resp.setBean(b);
 		source.addBean(id.toString(), resp);
 		return resp;
 	}
 
 	private JBeanResponse store(MStringID id, JBeanResponse beanresponse) {
 		List<String> params = new LinkedList<String>();
 		params.add(id.toString());
 		return post("objects", "write", params, beanresponse.getBean());
 	}
 
 	private JBeanResponse get(String service, String method, boolean auth,
 			List<String> params) {
 		MURL murl = getURL(service, method, auth, params);
 		//
 		URLCaller urlcaller = new URLCaller(murl, new ClientProxySettings());
 		byte[] responseBody = urlcaller.getResponseBody();
 		String sbody;
 		if (responseBody != null) {
 			sbody = new String(responseBody);
 		} else {
 			sbody = "";
 		}
 		return new JBeanResponse(sbody);
 	}
 
 	@Override
 	public boolean publish(MID id) {
 		return publish(id.getStringID());
 	}
 
 	@Override
 	public boolean publish(MStringID id) {
 		List<String> params = new LinkedList<String>();
 		params.add(id.toString());
 		store(id, read(id));
 		return get("objects", "publish", true, params).isSuccess();
 	}
 
 	private MURL getURL(String service, String method, boolean doauth,
 			List<String> params) {
 		String auth = null;
 		if (doauth) {
 			auth = sessionid;
 		}
 		return getAuthURL(service, method, params, auth);
 	}
 
 	public MURL getAuthURL(String service, String method, List<String> params,
 			String auth) {
 		MURL murl = new MURL(url.getHost(), url.getPort());
 		murl.append(url.getPath());
 		murl.append("/" + service);
 		murl.append("/" + method);
 		if (auth != null) {
 			murl.append("/" + auth);
 		}
 		if (params != null) {
 			for (String string : params) {
 				murl.append("/" + string);
 			}
 		}
 		return murl;
 	}
 
 	@Override
 	public JBeanResponse search(String filter, int index, int i) {
 		List<String> params = new LinkedList<String>();
 		params.add(filter);
 		params.add("" + index);
 		params.add("" + i);
 		//
 		return get("objects", "search", false, params);
 	}
 
 	private JBeanResponse post(String service, String method,
 			List<String> params, JBean b) {
 		String sbody = null;
 		try {
 			MURL mnurl = getURL(service, method, true, params);
 			URLCaller urlcaller = new URLCaller(mnurl,
 					new ClientProxySettings());
 			urlcaller.setPost(b.toXML().toString());
 			log.info("posting " + b.toXML() + " to " + mnurl);
 			byte[] responseBody = urlcaller.getResponseBody();
 			if (responseBody != null) {
 				sbody = new String(responseBody);
 			} else {
 				sbody = "";
 			}
 			return new JBeanResponse(sbody);
 		} catch (Exception e) {
 			log.error(e);
 			log.info("exception with response " + sbody);
 			return JBeanResponse.getError("ERROR " + sbody);
 		}
 	}
 
 	@Override
 	public HashMap<String, String> getBookmarkGroups() {
 		JBeanResponse ret = get("bookmarks", "listgroups", true, null);
 		if (ret.isSuccess()) {
 			HashMap<String, String> list = new HashMap<String, String>();
 
 			JBean bgroups = ret.getBean().get("bookmarkgroups");
 			List<JBean> cs = bgroups.getChildren();
 			for (JBean groupbean : cs) {
 				/*
 				 * <group> <name>users</name>
 				 * <groupid>fac8093e-c9ed-43b6-99bd-7fc9207f3c7d</groupid>
 				 * </group>
 				 */
 				log.info("bookmarkgroup " + groupbean);
 				list.put(groupbean.getValue("groupid"),
 						groupbean.getValue("name"));
 			}
 
 			return list;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public JBeanResponse getBookmarkGroup(String id) {
 		List<String> params = new LinkedList<String>();
 		params.add(id.toString());
 		return get("bookmarks", "getgroup", true, params);
 	}
 
 	@Override
 	public UserID getUserID() {
 		return userid;
 	}
 }
