 /* EmulatorClient.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Mar 20, 2012 Created by Pao Wang
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic.impl;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.servlet.http.HttpSession;
 
 import org.eclipse.jetty.util.UrlEncoded;
 import org.zkoss.json.JSONValue;
 import org.zkoss.zats.ZatsException;
 import org.zkoss.zats.mimic.Client;
 import org.zkoss.zats.mimic.DesktopAgent;
 import org.zkoss.zats.mimic.impl.emulator.Emulator;
 import org.zkoss.zk.ui.Desktop;
 
 /**
  * The server emulator client implement
  * 
  * @author pao
  */
 public class EmulatorClient implements Client , ClientCtrl{
 	private static Logger logger = Logger.getLogger(EmulatorClient.class.getName());
 	private Emulator emulator;
 	private List<DesktopAgent> desktopAgentList = new LinkedList<DesktopAgent>();
 	private List<String> cookies;
 	private DestroyListener destroyListener;
 
 	public EmulatorClient(Emulator emulator){
 		this.emulator = emulator;
 	}
 
 
 	public DesktopAgent connect(String zulPath) {
 		InputStream is = null;
 		try {
 			// load zul page
 			HttpURLConnection huc = getConnection(zulPath, "GET");
 			huc.connect();
 			// TODO, read response, handle redirect.
 
 			cookies = huc.getHeaderFields().get("Set-Cookie");
 			is = huc.getInputStream();
			if (logger.isLoggable(Level.FINEST))
 				logger.finest(getReplyString(is, huc.getContentEncoding()));
 			// get specified objects such as Desktop
 			Desktop desktop = (Desktop) emulator.getRequestAttributes().get(
 					"javax.zkoss.zk.ui.desktop");
 			// TODO, what if a non-zk(zul) page, throw exception?
 			DesktopAgent desktopAgent = new DefaultDesktopAgent(this, desktop);
 			desktopAgentList.add(desktopAgent);
 			return desktopAgent;
 		} catch (Exception e) {
 			throw new ZatsException("", e);
 		} finally {
 			close(is);
 		}
 	}
 	
 
 	public void destroy() {
 		if(destroyListener!=null){
 			destroyListener.willDestroy(this);
 		}
 		
 		for (DesktopAgent d : desktopAgentList){
 			destroy(d);
 		}
 		desktopAgentList.clear();
 	}
 	
 	public void destroy(DesktopAgent desktopAgent){
 		InputStream is = null;
 		try {
 			if (desktopAgent != null) {
 				// use au to remove a desktop
 				logger.config("destory desktop:"+desktopAgent.getId());
 				String url = MessageFormat.format(
 						"/zkau?dtid={0}&cmd_0=rmDesktop&opt_0=i",
 						desktopAgent.getId());
 				HttpURLConnection connection = getConnection(url, "GET");
 				for (String cookie : cookies){
 					connection.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
 				}
 				connection.connect();
 				is = connection.getInputStream();
 				logger.config(getReplyString(is, "utf-8"));
 				
 			}
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "", e);
 		} finally {
 			Util.close(is);
 			desktopAgent = null;
 			cookies = new LinkedList<String>();
 		}
 	}
 	                      
 
 	public HttpSession getSession() {
 		if (desktopAgentList.isEmpty()){
 			return null;
 		}else{
 			return (HttpSession)desktopAgentList.get(0).getDesktop().getSession().getNativeSession();
 		}
 		
 	}
 
 	public void postUpdate(String desktopId, String targetUUID,
 			String cmd, Map<String, Object> data) {
 		
 		// prepare au data
 		String dtid = UrlEncoded.encodeString(desktopId);
 		cmd = UrlEncoded.encodeString(cmd);
 		String uuid = UrlEncoded.encodeString(targetUUID);
 		String param;
 		if (data != null && data.size() > 0) {
 			String jsonData = UrlEncoded.encodeString(JSONValue
 					.toJSONString(data));
 			param = MessageFormat.format(
 					"dtid={0}&cmd_0={1}&uuid_0={2}&data_0={3}", dtid, cmd,
 					uuid, jsonData);
 		} else
 			param = MessageFormat.format("dtid={0}&cmd_0={1}&uuid_0={2}", dtid,
 					cmd, uuid);
 
 		if (logger.isLoggable(Level.FINEST)) {
 			logger.finest(targetUUID + " perform AU: " + UrlEncoded.decodeString(param, 0, param.length(), "utf-8"));
 		}
 		
 		OutputStream os = null;
 		InputStream is = null;
 		try {
 			// create http request and perform it
 			HttpURLConnection c = getConnection("/zkau", "POST");
 			c.setDoOutput(true);
 			c.setDoInput(true);
 			for (String cookie : cookies){
 				// handle cookie for session
 				c.addRequestProperty("Cookie", cookie.split(";", 2)[0]);
 			}
 			c.setRequestProperty("Content-Type",
 					"application/x-www-form-urlencoded;charset=UTF-8");
 			c.connect();
 			os = c.getOutputStream();
 			os.write(param.getBytes("utf-8"));
 			close(os);
 			// TODO, read response, handle redirect.
 			// read response
 			is = c.getInputStream();
			if (logger.isLoggable(Level.FINEST))
 				logger.finest(getReplyString(is, c.getContentEncoding()));
 		} catch (Exception e) {
 			throw new ZatsException("", e);
 		} finally {
 			close(os);
 			close(is);
 		}
 	}
 
 	private HttpURLConnection getConnection(String path, String method) {
 		try {
 			URL url = new URL(emulator.getAddress() + path);
 			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
 			huc.setRequestMethod(method);
 			huc.setUseCaches(false);
 			huc.addRequestProperty("Host",
 					emulator.getHost() + ":" + emulator.getPort());
 			huc.addRequestProperty("User-Agent",
 					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0)");
 			huc.addRequestProperty("Accept",
 					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 			huc.addRequestProperty("Accept-Language",
 					"zh-tw,en-us;q=0.7,en;q=0.3");
 			return huc;
 		} catch (Exception e) {
 			throw new ZatsException("", e);
 		}
 	}
 
 	private void close(Closeable c) {
 		try {
 			c.close();
 		} catch (Throwable e) {
 		}
 	}
 
 	private String getReplyString(InputStream is, String encoding) {
 		String reply = null;
 		Reader r = null;
 		try {
 			StringBuilder sb = new StringBuilder();
 			r = new BufferedReader(new InputStreamReader(is,
 					encoding != null ? encoding : "ISO-8859-1"));
 			while (true) {
 				int c = r.read();
 				if (c < 0)
 					break;
 				sb.append((char) c);
 			}
 			reply = sb.toString();
 		} catch (Exception e) {
 			logger.log(Level.WARNING, "", e);
 		} finally {
 			close(r);
 		}
 		return reply;
 	}
 
 	public void setDestroyListener(DestroyListener l) {
 		destroyListener = l;
 	}
 }
