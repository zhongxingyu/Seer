 /*
  * Copyright 2004-2013 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 
 package org.icemobile.jsp.tags;
 
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.icemobile.jsp.util.MobiJspConstants;
 import org.icemobile.jsp.util.Util;
 import org.icemobile.util.CSSUtils;
 import org.icemobile.util.ClientDescriptor;
 import org.icemobile.util.Constants;
 import org.icemobile.util.MobiEnvUtils;
 import org.icemobile.util.SXUtils;
 import org.icemobile.util.Utils;
 import org.icemobile.util.CSSUtils.Theme;
 
 /**
  * This is the Device specific detection and script writing tag.
  */
 public class DeviceResourceTag extends BaseSimpleTag {
 
 	static Logger log = Logger.getLogger(DeviceResourceTag.class.getName());
 
 	// compressed css post-fix notation.
 	public static final String CSS_COMPRESSION_POSTFIX = "-min";
 
 	// default resource library for a default themes,  if not specified in
 	// component definition this library will be loaded.
 	private static final String DEFAULT_LIBRARY = "org.icefaces.component.skins";
 	// url of a resource that could not be resolved, danger Will Robertson.
 	public static final String RESOURCE_URL_ERROR = "RES_NOT_FOUND";
 	// key to store if algorithm to detect device type has run. If a device
 	// was detected this key used to store the device name in session scope
 	// for later retrial on subsequent component renders.
 	public static final String MOBILE_DEVICE_TYPE_KEY = "mobile_device_type";
 	private final String EMPTY_STRING = "";
 	public static final String IOS_APP_ID = "485908934";
 	
 	public static final String META_CONTENTTYPE = "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>";
 	public static final String META_VIEWPORT = "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0'/>";
 	public static final String META_IOS_WEBAPPCAPABLE = "<meta name='apple-mobile-web-app-capable' content='yes'/>";
 	public static final String META_IOS_APPSTATUSBAR = "<meta name='apple-mobile-web-app-status-bar-style' content='black'/>";
 	public static final String META_IOS_SMARTAPPBANNER = "<meta name='apple-itunes-app' content=\"app-id=%s, app-argument=%s\"/>";
 	
 	public static final String LINK_SHORTCUT_ICON = "<link href='%s/resources/images/favicon.ico' rel='shortcut icon' type='image/x-icon'/>";
 	public static final String LINK_FAV_ICON = "<link href='%s/resources/images/favicon.ico' rel='icon' type='image/x-icon'/>";
 	
 	public static final String SCRIPT_ICEPUSH_PROD = "<script type='text/javascript' src='%s/code.icepush'></script>";
 	public static final String SCRIPT_ICEPUSH = "<script type='text/javascript' src='%s/code.min.icepush'></script>";
     public static final String SCRIPT_ICEMOBILE = "<script type='text/javascript' src='%s%s/javascript/icemobile.js'></script>";
 	public static final String SCRIPT_ICEMOBILE_PROD = "<script type='text/javascript' src='%s%s/javascript/icemobile-min.js'></script>";
 	public static final String SCRIPT_SIMULATOR = "<script type='text/javascript' src='%s%s/javascript/simulator-interface.js'></script>";
 	public static final String CSS_SIMULATOR = "<link type='text/css' rel='stylesheet' href='%s%s/%s/simulator/simulator.css' />";
 
 	//tag attributes
 	private String name;
 	private String library;
 	private String view;
 	private boolean includeIOSSmartAppBanner = true;
 	private boolean includePush = false;
 
 	public void doTag() throws IOException {
 		
 	    boolean ios6 = false;
 		boolean desktop = false;
 				
 		PageContext pageContext = getContext();
 		ClientDescriptor client = getClient();
 		
 		ios6 = client.isIOS6();
 		if( !ios6 ){
 			desktop = client.isDesktopBrowser();
 		}
 		JspWriter out = pageContext.getOut();
 		String contextRoot = getContextRoot();
 		out.write(META_CONTENTTYPE);
 		out.write(String.format(LINK_FAV_ICON, contextRoot));
 		out.write(String.format(LINK_SHORTCUT_ICON, contextRoot));
 		if( !desktop ){
 			out.write(META_VIEWPORT);
 			if( ios6 ){
 				out.write(META_IOS_WEBAPPCAPABLE);
 				out.write(META_IOS_APPSTATUSBAR);
 				if (isShowAppBanner(client))  {
 				    //String uploadURL = SXUtils.getRegisterSXURL(getRequest(),MobiJspConstants.SX_UPLOAD_PATH); //TODO MOBI-359
 					String uploadURL = SXUtils.getRegisterSXURL(getRequest());
 				    String smartAppMeta = String.format(META_IOS_SMARTAPPBANNER, IOS_APP_ID, uploadURL);
 					out.write(smartAppMeta);
                     HttpSession httpSession = getRequest().getSession(false);
                     if (null == httpSession)  {
                         getRequest().setAttribute(Constants.IOS_SMART_APP_BANNER_KEY, 
                                 Boolean.TRUE);
                     } else {
                         httpSession.setAttribute(Constants.IOS_SMART_APP_BANNER_KEY, 
                                 Boolean.TRUE);
                     }
 
 				}
 			}
 		}
 		writeOutDeviceStyleSheets(client);
 		boolean prod = MobiEnvUtils.isProductionStage(getServletContext());
 		if (prod)  {
 		    out.write(String.format(SCRIPT_ICEMOBILE_PROD, contextRoot, MobiJspConstants.RESOURCE_BASE_URL));
 		}
 		else{
 		    out.write(String.format(SCRIPT_ICEMOBILE, contextRoot, MobiJspConstants.RESOURCE_BASE_URL));
 		}
 		
 		if (client.isDesktopBrowser() && client.isSimulator())  {
 		    out.write(String.format(CSS_SIMULATOR, contextRoot, MobiJspConstants.RESOURCE_BASE_URL, DEFAULT_LIBRARY));
 		    out.write(String.format(SCRIPT_SIMULATOR, contextRoot,
                     MobiJspConstants.RESOURCE_BASE_URL));
 		}
 		
 		if( includePush ){
 		    if( prod ){
 		        out.write(String.format(SCRIPT_ICEPUSH_PROD,contextRoot));
 		    }
 		    else{
 		        out.write(String.format(SCRIPT_ICEPUSH_PROD,contextRoot));
 		        //out.write(String.format(SCRIPT_ICEPUSH,contextRoot)); MOBI-653
 		    }
             String cloudPushId = Utils.getCloudPushId(getRequest());
             if (null != cloudPushId) {
                 out.write("<script type='text/javascript'>");
                 out.write(
                         "window.addEventListener('load', function() { ice.push.parkInactivePushIds('"
                                 + cloudPushId + "'); }, false);");
                 out.write("</script>");
             }
 		}
 	}
 	
     private boolean isShowAppBanner(ClientDescriptor client)  {
         if (MobiEnvUtils.isDevelopmentStage(getServletContext()))  {
             return false;
         }
         return (includeIOSSmartAppBanner && !client.isSXRegistered());
     }
 
 	private void writeOutDeviceStyleSheets(ClientDescriptor client) throws IOException {
 		
 		PageContext pageContext = getContext();
 		JspWriter out = pageContext.getOut();
 		
 		String theme = getName();
 		if( theme == null || "".equals(theme) ){
 		    theme = CSSUtils.deriveTheme((HttpServletRequest)pageContext.getRequest()).fileName();
 		}
 		//android and honeycomb themes deprecated
 		else if( theme.toLowerCase().equals("android") || theme.toLowerCase().equals("honeycomb") ){
             theme = "android_dark";
         }
 		
 		String fileName = theme;
 		
 		if (MobiEnvUtils.isProductionStage(getServletContext())) {
             fileName = fileName.concat(CSS_COMPRESSION_POSTFIX);
         }
 		
 		String contextRoot = Util.getContextRoot(pageContext.getRequest());
 		
 		String markers = " " + theme + " ui-mobile";
 		if( client.isBlackBerry10OS() && client.isICEmobileContainer() ){
             markers += " bb10ctr";
         }
         if( client.isIE10Browser() ){
             markers += " ie10";
         }
         if( client.isAndroidBrowserOrWebView()){
             markers += " android-browser";
         }
        if( client.isDesktopBrowser()){
            markers += " desktop";
        }
         out.write("<script type='text/javascript'>document.documentElement.className = document.documentElement.className+'" 
                 + markers + "';if (window.addEventListener) window.addEventListener('load', function() {document.body.className = 'ui-body-c';});</script>");
 		
 		String cssLink = String.format("<link type='text/css' rel='stylesheet' href='%s%s/%s/%s/%s.css' />", 
 				contextRoot, MobiJspConstants.RESOURCE_BASE_URL, DEFAULT_LIBRARY, theme, fileName);
 		out.write(cssLink);
 		
 	}
 
 	public String getLibrary() {
 		return library;
 	}
 
 	public void setLibrary(String library) {
 		this.library = library;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getView() {
 		return view;
 	}
 
 	public void setView(String view) {
 		this.view = view;
 	}
 
 	public boolean isIncludeIOSSmartAppBanner() {
 		return includeIOSSmartAppBanner;
 	}
 
 	public void setIncludeIOSSmartAppBanner(boolean includeIOSSmartAppBanner) {
 		this.includeIOSSmartAppBanner = includeIOSSmartAppBanner;
 	}
 
 	public boolean isIncludePush() {
 		return includePush;
 	}
 
 	public void setIncludePush(boolean includePush) {
 		this.includePush = includePush;
 	}
 	
 	public void release(){
 	    super.release();
 	    name = null;
 	    library = null;
 	    view = null;
 	    includeIOSSmartAppBanner = true;
 	    includePush = false;
 	}
 
 }
