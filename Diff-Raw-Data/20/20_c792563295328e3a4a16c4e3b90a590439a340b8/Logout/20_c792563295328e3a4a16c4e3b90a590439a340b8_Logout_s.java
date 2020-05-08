 package org.uigl.veemanage.httpd.pages;
 
 import java.io.InputStream;
 import java.util.Properties;
 
 import org.uigl.veemanage.httpd.Session;
 import org.uigl.veemanage.httpd.VeeManageHTTPD;
 import org.uigl.veemanage.httpd.VeeManageHTTPD.VeeManageHTTPPage;
 
 public class Logout implements VeeManageHTTPPage {
 	
 	@Override
 	public boolean hasMatch(String uri, String method) {
 		return uri.equals("/logout/");
 	}
 
 	@Override
 	public void init(String uri, String method, Properties headers, Properties params, Properties files, Session userSession) {
 		userSession.putBoolean("UserAuthenticated", false);
 	}
 
 	@Override
 	public String getStatus() {
 		return VeeManageHTTPD.HTTP_OK;
 	}
 
 	@Override
 	public String getMimeType() {
 		return VeeManageHTTPD.MIME_HTML;
 	}
 
 	@Override
 	public Properties getHeaders() {
 		return null;
 	}
 
 	@Override
 	public InputStream getData() {
 		return null;
 	}
 
 	@Override
 	public int getFlags() {
		return VeeManageHTTPD.FLAG_REDIRECT;
 	}
 
 	@Override
 	public String getRedirectLocation() {
 		return "/";
 	}
 
 }
