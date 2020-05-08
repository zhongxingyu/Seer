 package org.uigl.veemanage.httpd;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Properties;
 import java.util.logging.Level;
 
 import org.uigl.veemanage.VeeManage;
 
 import fi.iki.elonen.NanoHTTPD.NanoHTTPD;
 
 public class VeeManageHTTPD extends NanoHTTPD {
 
 	public static final int FLAG_NONE = 0;
 	public static final int FLAG_REDIRECT = 1;
 	public static final int FLAG_REMOVE_SESSION = 2;
 	public static final int FLAG_FILE = 4;
 	
 	public interface VeeManageHTTPPage {
 		public boolean hasMatch(String uri, String method);
 		public void init(String uri, String method, Properties headers, Properties params, Properties files, Session userSession);
 		public String getStatus();
 		public String getMimeType();
 		public Properties getHeaders();
 		public InputStream getData();
 		public int getFlags();
 		public String getRedirectLocation();
 	}
 	
 	private final VeeManageHTTPPage[] mPages = new VeeManageHTTPPage[]{
 			new org.uigl.veemanage.httpd.pages.Index(),
 			new org.uigl.veemanage.httpd.pages.Logout(),
 			new org.uigl.veemanage.httpd.pages.Login()
 	};
 	
 	private File mRoot;
 	private SessionManager mSessions;
 	
 	public VeeManageHTTPD(int port, File wwwroot) throws IOException {
 		super(port, wwwroot);
 		this.mRoot = wwwroot;
 		this.mSessions = new SessionManager();
 	}
 	
 	@Override
 	public Response serve(String uri, String method, Properties header, Properties params, Properties files) {
 
 		VeeManage.LOGGER.logp(Level.INFO, VeeManageHTTPD.class.getName(), "serve(String uri, String method, Properties header, Properties params, Properties files)", "URI:" + uri);
 		
 		Response page = new Response();
 		page.status = HTTP_OK;
 		page.mimeType = MIME_HTML;
 		cookiesToParams(header, params);
 		Session userSession = startSession(page, params);
 		
 		for (VeeManageHTTPPage pageClass : mPages) {
 			
 			try {
 				if (pageClass.hasMatch(uri, method)) {
 					pageClass.init(uri, method, header, params, files, userSession);
 					page.status = pageClass.getStatus();
 					//FIXME: Change these so they don't run twice.
 					if (pageClass.getMimeType() != null)
 						page.mimeType = pageClass.getMimeType();
 					if (pageClass.getHeaders() != null)
 						page.header = pageClass.getHeaders();
 					if (pageClass.getData() != null)
 						page.data = pageClass.getData();
 					else
 						page.data = new ByteArrayInputStream("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head></head><body></body></html>".getBytes());
	
 					if ((pageClass.getFlags() & FLAG_REDIRECT) == FLAG_REDIRECT)
 						redirectHeader(page, pageClass.getRedirectLocation());
 					if ((pageClass.getFlags() & FLAG_REMOVE_SESSION) == FLAG_REMOVE_SESSION)
 						userSession = removeSession(userSession);
 					if ((pageClass.getFlags() & FLAG_FILE) == FLAG_FILE)
 						break;
 					
 					return page;
 				}
 			} catch (Exception e) {
 				page.status = HTTP_INTERNALERROR;
 				page.mimeType = MIME_HTML;
 				page.data = new ByteArrayInputStream(("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\"><html><head><title>" + HTTP_INTERNALERROR + "</title></head><body><h1>" + HTTP_INTERNALERROR + "</h1><br><h2>Error contents:</h2><br><pre>" + e.getMessage() + "</pre><br><h2>Stack Trace:</h2><br><pre>" + getStackTrace(e) + "</pre></body></html>").getBytes());
 			}
 			
 		}
 			
 		return super.serveFile(uri, header, this.mRoot, false);
 	}
 	
 	private static void cookiesToParams(Properties header, Properties params) {
 		if (header.containsKey("cookie")) {
 			String cookieString = (String) header.get("cookie");
 			String[] cookieGroups = cookieString.split("(=|; )");
 			for (int i=0; i<cookieGroups.length; i+=2)
 				params.put(cookieGroups[i].trim(), cookieGroups[i+1].trim());
 		}
 	}
 	
 	private Session startSession(Response r, Properties params) {
 		Session ret = null;
 		if (params.containsKey("SessionID"))
 			ret = mSessions.findSession((String) params.get("SessionID"));
 		if (ret == null)
 			ret = mSessions.getNewSession();
 		r.addHeader("Set-Cookie", "SessionID=" + ret.getSessionID());
 		return ret;
 	}
 	
 	public static void redirectHeader(Response res, String location) {
 		res.status = HTTP_REDIRECT;
 		res.mimeType = MIME_HTML;
 		res.addHeader("Location", location);
 		res.data = new ByteArrayInputStream(("<html><body>Redirecting.<br />Click <a href=\"" + location + "\">Here</a> if you are not automatically redirected.</body></html>").getBytes());
 	}
 	
 	private Session removeSession(Session ses) {
 		mSessions.removeSession(ses.getSessionID());
 		return null;
 	}
 	
 	public static String getStackTrace(Throwable aThrowable) {
 		final Writer result = new StringWriter();
 		final PrintWriter printWriter = new PrintWriter(result);
 		aThrowable.printStackTrace(printWriter);
 		return result.toString();
 	}
 }
