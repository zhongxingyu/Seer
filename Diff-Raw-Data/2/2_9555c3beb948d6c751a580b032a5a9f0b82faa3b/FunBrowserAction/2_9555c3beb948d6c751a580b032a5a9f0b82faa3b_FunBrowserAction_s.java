 package com.tp.action.nav;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.convention.annotation.Namespace;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.utils.Constants;
 import com.tp.utils.Struts2Utils;
 
 @Namespace("/nav")
 public class FunBrowserAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public String execute() throws Exception {
 
 		return compare();
 	}
 
 	public String getClient() throws Exception {
 		HttpServletRequest request=Struts2Utils.getRequest();
 		HttpServletResponse response=Struts2Utils.getResponse();
		Struts2Utils.getRequest().getRequestDispatcher("/download.html?inputPath=client/nav/funbrowser.apk").forward(request, response);
 		return null;
 	}
 
 	public String compare() throws Exception {
 		String version = Struts2Utils.getParameter(Constants.PARA_CLIENT_VERSION);
 		if (StringUtils.isNotBlank(version)&&version.equals("1.0.0")) {
 			Struts2Utils.renderText("FUNBROWSER_V:2.0.0");
 		}
 
 		return null;
 	}
 }
