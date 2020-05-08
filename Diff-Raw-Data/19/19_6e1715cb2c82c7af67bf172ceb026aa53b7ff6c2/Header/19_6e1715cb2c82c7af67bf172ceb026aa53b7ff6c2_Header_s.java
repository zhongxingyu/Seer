 package org.gwtapp.template.server;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.gwtapp.template.client.Template;
 
 public class Header {
 
 	private HttpServletResponse response;
 
 	public void setTag(String tag) {
		response.addHeader(Template.Header.TAG, StringUtils.defaultString(tag));
 	}
 
 	public void setStyle(String style) {
		response.addHeader(Template.Header.STYLE, StringUtils
				.defaultString(style));
 	}
 
 	public void setStyleclass(String styleClass) {
		response.addHeader(Template.Header.STYLE_CLASS, StringUtils
				.defaultString(styleClass));
 	}
 
 	public void setResponse(HttpServletResponse response) {
 		this.response = response;
 	}
 }
