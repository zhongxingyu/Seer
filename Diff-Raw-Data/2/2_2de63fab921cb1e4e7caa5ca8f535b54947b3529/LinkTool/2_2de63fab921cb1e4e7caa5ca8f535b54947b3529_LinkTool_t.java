 /**********************************************************************************
  * $URL: https://source.sakaiproject.org/svn/tags/sakai_2-1-1/sample-tools/browser/src/java/org/sakaiproject/tool/sample/BrowserTool.java $
  * $Id: BrowserTool.java 632 2005-07-14 21:22:50Z janderse@umich.edu $
  **********************************************************************************
  *
  * Copyright (c) 2005 The Regents of the University of Michigan, Trustees of Indiana University,
  *                  Board of Trustees of the Leland Stanford, Jr., University, and The MIT Corporation
  * 
  * Licensed under the Educational Community License Version 1.0 (the "License");
  * By obtaining, using and/or copying this Original Work, you agree that you have read,
  * understand, and will comply with the terms and conditions of the Educational Community License.
  * You may obtain a copy of the License at:
  * 
  *      http://cvs.sakaiproject.org/licenses/license_1_0.html
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
  * AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
  * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  *
  **********************************************************************************/
 
 package org.sakaiproject.tool.rutgers;
 
 import java.io.*;
 
 import java.security.*;
 import java.security.spec.*;
 import java.security.interfaces.*;
 
 import javax.crypto.*;
 import javax.crypto.spec.*;
 import javax.crypto.interfaces.*;
 
 import java.math.BigInteger;
 
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.tool.api.Session;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 import org.sakaiproject.tool.api.Placement;
 import org.sakaiproject.util.Web;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 
 import org.sakaiproject.site.cover.SiteService;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.SitePage;
 
 import org.sakaiproject.site.api.ToolConfiguration;
 
 import org.sakaiproject.authz.cover.AuthzGroupService;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.api.Role;
 
 import org.sakaiproject.authz.cover.SecurityService;
 
 import org.sakaiproject.exception.IdUsedException;
 import org.sakaiproject.exception.PermissionException;
 
 /**
  * <p>
  * Sakai browser sample tool.
  * </p>
  * 
  * @author University of Michigan, Sakai Software Development Team
  * @version $Revision: 632 $
  */
 public class LinkTool extends HttpServlet
 {
     	private static final String headHtml = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"> 		<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">   <head>     <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />     <meta http-equiv=\"Content-Style-Type\" content=\"text/css\" />  <title>Link Tool</title>";
 
         private static final String headHtml1 = "<script type=\"text/javascript\" language=\"JavaScript\"> 		var _editor_url = \"/library/htmlarea/\"; function setFrameHeight(id) { var frame = parent.document.getElementById(id); if (frame) {                var objToResize = (frame.style) ? frame.style : frame; objToResize.height = \""; 
 
     	private static final String headHtml2 = "\";  }} </script> 		<script type=\"text/javascript\" language=\"JavaScript\" src=\"/library/htmlarea/htmlarea.js\"> 		</script> 		  </head><body onload=\"";
 
 	private static final String headHtml3 = "\" style='margin:0;padding:0;'>";
 
 	private static final String tailHtml = "</body></html>";
 
         private static final String stylesHtml = "<link href='/library/skin/tool_base.css' type='text/css' rel='stylesheet' media='all' /><link href='/library/skin/default/tool.css' type='text/css' rel='stylesheet' media='all' /><script type='text/javascript' language='JavaScript' src='/library/js/headscripts.js'></script>";
 
 
 	/** Our log (commons). */
 	private static Log M_log = LogFactory.getLog(LinkTool.class);
 
         private static String homedir = null;
         private static SecretKey secretKey = null;
         private static SecretKey salt = null;
         private static String ourUrl = null;
 
 	/** Helper tool for options. */
 	private static final String OPTIONS_HELPER = "sakai.tool_config.helper";
 
         private static final String privkeyname = "sakai.rutgers.linktool.privkey";
         private static final String saltname = "sakai.rutgers.linktool.salt";
 
         private Set illegalParams;
         private Pattern legalKeys;
 
 	/**
 	 * Access the Servlet's information display.
 	 * 
 	 * @return servlet information.
 	 */
 	public String getServletInfo()
 	{
 		return "Link Tool";
 	}
 
 	/**
 	 * Initialize the servlet.
 	 * 
 	 * @param config
 	 *        The servlet config.
 	 * @throws ServletException
 	 */
 	public void init(ServletConfig config) throws ServletException
 	{
 		super.init(config);
 		homedir = ServerConfigurationService.getSakaiHomePath();
 		if (homedir == null)
 		    homedir = "/etc/";
 
 		//		System.out.println("canread " + homedir + pubkeyname + (new File(homedir + pubkeyname)).canRead());
 		//		System.out.println("canread " + homedir + privkeyname + (new File(homedir + privkeyname)).canRead());
 
 		if (!(new File(homedir + privkeyname)).canRead()) {
 		    genkey(homedir);
 		}
 
 		//		System.out.println("canread public " + (new File(homedir + pubkeyname)).canRead());
 		//		System.out.println("canread private " + (new File(homedir + privkeyname)).canRead());
 
 		secretKey = readSecretKey(homedir + privkeyname, "Blowfish");
 		//		if (secretKey != null)
 		//		    System.out.println("got private key");
 
 		if (!(new File(homedir + saltname)).canRead()) {
 		    gensalt(homedir);
 		}
 
 		salt = readSecretKey(homedir + saltname, "HmacSHA1");
 
 		ourUrl = ServerConfigurationService.getString("sakai.rutgers.linktool.serverUrl");
 		// System.out.println("linktool url " + ourUrl);
 		if (ourUrl == null || ourUrl.equals(""))
 		    ourUrl = ServerConfigurationService.getString("serverUrl");
 		// System.out.println("linktool url " + ourUrl);
 		if (ourUrl == null || ourUrl.equals(""))
 		    ourUrl = "http://127.0.0.1:8080";
 
 		// System.out.println("linktool url " + ourUrl);
 
                 illegalParams = new HashSet();
 		illegalParams.add("user");
 		illegalParams.add("internaluser");
 		illegalParams.add("site");
 		illegalParams.add("role");
 		illegalParams.add("session");
 		illegalParams.add("serverurl");
 		illegalParams.add("url");
 		illegalParams.add("time");
 		illegalParams.add("sign");
 
 		legalKeys = Pattern.compile("^[a-zA-Z0-9]+$");
 
 		M_log.info("init()");
 	}
 
 	/**
 	 * Shutdown the servlet.
 	 */
 	public void destroy()
 	{
 		M_log.info("destroy()");
 
 		super.destroy();
 	}
 
 	/**
 	 * Respond to Get requests:
 	 *   display main content by redirecting to it and adding
 	 *     user= euid= site= role= serverurl= time= sign=
 	 *   for privileged users, add a bar at the top with a link to
 	 *     the setup screen
 	 *   ?Setup generates the setup screen
 	 * 
 	 * @param req
 	 *        The servlet request.
 	 * @param res
 	 *        The servlet response.
 	 * @throws ServletException.
 	 * @throws IOException.
 	 */
 
 	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 
 	    // get the Tool
 	    Placement placement = ToolManager.getCurrentPlacement();
 	    Properties config = null;
 	    if (placement != null)
    	      config = placement.getConfig();
 	    PrintWriter out = res.getWriter();
		 res.setContentType("text/html");
		 
 	    String userid = null;
 	    String euid = null;
 	    String siteid = null;
 	    String sessionid = null;
 	    String url = null;
 	    String command = null;
 	    String signature = null;
 	    String element = null;
 	    String oururl = req.getRequestURI();
 	    String query = req.getQueryString();
 
 	    // set frame height
 
 	    StringBuffer bodyonload = new StringBuffer();
 	    if (placement != null)
                 {
 		    element = Web.escapeJavascript("Main" + placement.getId( ));
 		    bodyonload.append("setFrameHeight('" + element + "');");
                 }
 
 	    // prepare the data for the redirect
 
 	    // we can always get the userid from the session
 	    Session s = SessionManager.getCurrentSession();
 	    if (s != null) {
 		// System.out.println("got session " + s.getId());
 		userid = s.getUserId();
 		euid = s.getUserEid();
 		sessionid = s.getId();
 	    }
 
 	    if (userid != null && (euid == null || euid.equals("")))
 		euid = userid;
 
 	    // site is there only for tools, otherwise have to use user's arg
 	    // this is safe because we verify that the user has a role in site
 	    if (placement != null)
 		siteid = placement.getContext();
 	    if (siteid == null)
 		siteid = req.getParameter("site");
 
 	    // if user has asked for a url, use it
 	    url = req.getParameter("url");
 	    // else take it from the tool config
 	    if (url == null && config != null)
 		url = config.getProperty("url", null);
 
 	    // now get user's role in site; must be defined
 	    String realmId = null;
 	    AuthzGroup realm = null;
 	    Role r = null;
 	    String rolename = null;
 
 	    if (siteid != null)
 		realmId = SiteService.siteReference(siteid);
 	    if (realmId != null) {
 		try {
 		    realm = AuthzGroupService.getAuthzGroup(realmId);
 		} catch (Exception e) {}
 	    }
 	    if (realm != null && userid != null)
 		r = realm.getUserRole(userid);
 	    if (r != null) {
 		rolename = r.getId();
 	    }
 
 	    if (sessionid != null)
 		sessionid = encrypt(sessionid);
 
 	    // generate redirect, as url?user=xxx&site=xxx
 
 	    if (url != null && userid != null && siteid != null && rolename != null && sessionid != null) {
 		// command is the thing that will be signed
 		command = "user=" + URLEncoder.encode(euid) + 
 		    "&internaluser=" + URLEncoder.encode(userid) + 
 		    "&site=" + URLEncoder.encode(siteid) + 
 		    "&role=" + URLEncoder.encode(rolename) +
 		    "&session=" + URLEncoder.encode(sessionid) +
 		    "&serverurl=" + URLEncoder.encode(ourUrl) +
 		    "&time=" + System.currentTimeMillis();
 
 		// pass on any other arguments from the user.
 		// but sanitize them to prevent people from trying to
 		// fake out the parameters we pass, or using odd syntax
 		// whose effect I can't predict
 		Map params = req.getParameterMap();
 		Set entries = params.entrySet();
 		Iterator pIter = entries.iterator();
 		while (pIter.hasNext()) {
 		    Map.Entry entry = (Map.Entry)pIter.next();
 		    String key = "";
 		    String value = "";
 		    try {
 			key = (String)entry.getKey();
 			value = ((String [])entry.getValue())[0];
 		    } catch (Exception ignore) {}
 		    if (!illegalParams.contains(key.toLowerCase()) &&
 			legalKeys.matcher(key).matches())
 			command = command + "&" + key + "=" +
 			    URLEncoder.encode(value);
 		}
 
 		try {
 		    // System.out.println("sign >" + command + "<");
 		    signature = sign(command);
 		    url = url + "?" + command + "&sign=" + signature + "';";
 		    bodyonload.append("window.location = '" + url);
 		} catch (Exception e) {};
 	    }
 
 	    // now put out a vestigial web page, whose main functional
 	    // part is actually the <body onload=
 
 	    int height = 600;
 	    String heights;
 	    if (config != null) {
 		heights =  safetrim(config.getProperty("height", "600"));
 		if (heights.endsWith("px"))
 		    heights = safetrim(heights.substring(0, heights.length()-2));
 		height = Integer.parseInt(heights);
 	    }
 
 	    // now generate the page
 
 	    // User asked for setup menu
 	    //	    if (query != null)
 	    //		System.out.println("query: " + query);
 	    //	    else
 	    //		System.out.println("no query");
 	    if (query != null && query.equals("Setup")) {
 		if (writeSetupPage(out, placement, element, config, oururl))
 		    return;
 	    }
 
 	    // If user can update site, add config menu
 	    // placement and config should be defined in tool mode
 	    // in non-tool mode, there's no config to update
 	    if (placement != null && config != null &&
 		SiteService.allowUpdateSite(siteid)) {
 		if (writeOwnerPage(out, height, url, element, oururl))
 		    return;
 	    }
 
 	    // default output - show the requested application
 	    out.println(headHtml + headHtml1 + height + "px" + headHtml2 + bodyonload + headHtml3);
 	    out.println(tailHtml);
 
 
 	    //	    res.sendRedirect(res.encodeRedirectURL(config.getProperty("url", "/")));
 
 	}
 
 	/**
 	 * Called by doGet to display the main contents. Differs from
 	 *   the default output in that it adds a bar at the top containing
 	 *   a link to the Setup option.
 	 * 
 	 * @param out
 	 *        printwriter generating web display
 	 * @param height
 	 *        height of the window to display
 	 * @param url
 	 *        url to redirect to
 	 * @param element
 	 *        Javascript window id
 	 * @param oururl
 	 *        URL for this application
 	 */
 
 	private boolean writeOwnerPage(PrintWriter out, int height, String url, String element, String oururl) {
 
 	    String bodyonload = "";
 
 	    if (url == null)
 		return false;
 
 	    if (element != null)
 		bodyonload = "setFrameHeight('" + element + "');";
 
 	    out.println(headHtml + headHtml1 + (height+30) + "px" + headHtml2 + bodyonload + headHtml3);
 	    out.println("<div><div style='color:#000;text-align:center;font-size:.9em;padding-bottom:5px;line-height:1.3em;background:#DDDDDD;height:22px;overflow:hidden'><a href='" + oururl + "?Setup' style='border-bottom:1px dashed #999999;color:black;text-decoration:none;font:80% Verdana,Arial,Helvetica,sans-serif'>Setup</a></div></div><iframe src='" + url + "' height='" + height + "px' width='100%' frameborder='0' marginwidth='0' marginheight='0'></iframe>");
 
 	    out.println(tailHtml);
 
 	    return true;
 	}
 
 	/**
 	 * Called by doGet to display the main contents. Differs from
 	 *   the default output in that it adds a bar at the top containing
 	 *   a link to the Setup option.
 	 * 
 	 * @param out
 	 *        printwriter generating web display
 	 * @param placement
 	 *        Sakai Placement struct for this tool
 	 * @param element
 	 *        Javascript window id
 	 * @param config
 	 *        Properties list for this tool
 	 * @param oururl
 	 *        URL for this application
 	 */
 
 	private boolean writeSetupPage(PrintWriter out, Placement placement, String element, Properties config, String oururl) {
 	    String bodyonload = "";
 
 	    // if not in tool mode, nothing to do
 	    if (placement == null || config == null)
 		return false;
 
 	    if (element != null)
 		bodyonload = "setMainFrameHeight('" + element + "');setFocus(focus_path);";
 
 	    out.println(headHtml);
 	    out.println(stylesHtml);
 	    out.println(headHtml1 + "300px" + headHtml2 + bodyonload + headHtml3);
 	    //	    out.println("<h2>Setup page</h2>");
 	    out.println("<div class='portletBody'><h2>Setup</h2>");
 	    out.println("<form method='post' action='" + oururl + "?SetupForm'>");
 	    out.println("URL: <input type=text name=url size=70 value='" +
 			config.getProperty("url") + "'><br>");
 	    out.println("Height: <input type=text name=height value='" +
 			config.getProperty("height") + "'><br>");
 	    if (placement != null)
 		out.println("Page title: <input type=text name=title><br>");
 	    out.println("<input type=submit value='Update Configuration'>");
 	    out.println("</form>");
 	    out.println("<p>NOTE: setting the Page title changes the title for the entire page (i.e. what is in the left margin). If there is more than one tool on the page, this may not be what you want to do. Admittedly, having more than one tool on the page is fairly rare.");
 
 	    out.println("<h3>Session Access</h3>");
 	    out.println("<p> This section allows you to request a cryptographically signed object that can be used to request access to a Sakai session ID. Session IDs are needed to access most of the web services. ");
 
 	    // Session s = SessionManager.getCurrentSession();
 	    //	    String userid = null;
 	    // if (s != null) {
 	    //	// System.out.println("got session " + s.getId());
 	    //	userid = s.getUserId();
 	    //}
 
 	    boolean isprived = SecurityService.getInstance().isSuperUser();
 	    //	    System.out.println("user " + userid + "prived " + isprived);
 	    if (!isprived) {
 		out.println("<p>You can request an object that will generate a session logged with your userid. For applications that deal with sites that you own, such an object should be sufficient for most purposes.");
 		out.println("<p>For applications that need to create site or users, or deal with many sites, an administrator can generate objects with more privileges");
 		out.println("<form method='post' action='" + oururl + "?SignForm'>");
 		out.println("<input type=submit value='Generate Signed Object'>");
 		out.println("</form>");
 	    } else {
 		out.println("<p>As a privileged user, you can request an object that will generate a session logged in as any user. For applications that just deal with a single site, and which need site owner privileges, you should ask for an object in the name of the site owner. For applications that need to create site or users, or deal with many sites, you should ask for an object in the name of a user with administrative privileges. If you generate an object in the name of an administrator, please be careful only to put it in sites whose security you trust.<p>You can also request a second kind of object. This one will generate a session for the current user. That is, when an end user accesses an application, this will return a session for that end user. Please be careful about what sites you put this in, because it will allow the owner of the site to compromise the privacy of any user using the site.");
 
 		out.println("<form method='post' action='" + oururl + "?SignForm'>");
 		out.println("Specific user: <input type=text name=user size=30> [an internal Sakai user, not the Enterprise ID]<br>");
 		out.println("The current user: <input type=checkbox name=current value=yes><br>");
 		out.println("<input type=submit value='Generate Signed Object'>");
 		out.println("</form>");
 	    }
 
 	    //	    if (SecurityService.getInstance().isSuperUser())
 
 	    out.println("<h3>Exit</h3><p><form action='" + oururl + "?panel=Main' method='get'><input type=submit value='Exit Setup'></form>");
 	    out.println("</div>");
 
 	    out.println(tailHtml);
 	    return true;
 	}
 
 
 	/**
 	 * Output a page with an error message on it
 	 * 
 	 * @param out
 	 *        printwriter generating web display
 	 * @param element
 	 *        Javascript window id
 	 * @param error
 	 *        the actual error message
 	 * @param oururl
 	 *        URL for this application
 	 */
 
 	private boolean writeErrorPage(PrintWriter out, String element, String error, String oururl) {
 
 	    String bodyonload = "";
 
 	    if (element != null)
 		bodyonload = "setMainFrameHeight('" + element + "');setFocus(focus_path);";
 
 	    out.println(headHtml);
 	    out.println(stylesHtml);
 	    out.println(headHtml1 + "300px" + headHtml2 + bodyonload + headHtml3);
 
 	    out.println("<div class='portletBody'><h2>Error</h2>");
 	    out.println("<p>" + error);
 
 	    out.println("<p><a href='" + oururl + "?panel=Main'>Return to tool</a>");
 	    out.println("</div>");
 
 	    out.println(tailHtml);
 	    return true;
 	}
 
 	/**
 	 * Respond to data posting requests.  Request we support are
 	 *   ?SetupForm - when Setup form is submitted. Implement the
 	 *      changes and redisplay the setup form with the updated values
 	 *   ?SignForm - when user submits a request for us to generate a
 	 *      signed object. Generate the object and display it
 	 * 
 	 * @param req
 	 *        The servlet request.
 	 * @param res
 	 *        The servlet response.
 	 * @throws ServletException.
 	 * @throws IOException.
 	 */
 	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 	    String query = req.getQueryString();
 	    if (query.equals("SignForm")) {
 		doSignForm(req, res);
 		return;
 	    }
 
 	    Placement placement = ToolManager.getCurrentPlacement();
 	    Properties config = null;
 	    PrintWriter out = res.getWriter();
 	    String userid = null;
 	    String siteid = null;
 	    String url = null;
 	    String command = null;
 	    String signature = null;
 	    String element = null;
 	    String oururl = req.getRequestURI();
 
 	    // must be in tool mode
 	    if (placement == null) {
 		writeErrorPage(out, element, "Unable to find the current tool", oururl);
 		return;
 	    }
 
 	    // site is there only for tools, otherwise have to use user's arg
 	    // this is safe because we verify that the user has a role in site
 	    siteid = placement.getContext();
 	    if (siteid == null) {
 		writeErrorPage(out, element, "Unable to find the current site", oururl);
 		return;
 	    }
 
 	    Session s = SessionManager.getCurrentSession();
 	    if (s != null) {
 		// System.out.println("got session " + s.getId());
 		userid = s.getUserId();
 	    }
 
 	    if (userid == null) {
 		writeErrorPage(out, element, "Unable to figure out your userid", oururl);
 		return;
 	    }
 	    if (!SiteService.allowUpdateSite(siteid)) {
 		writeErrorPage(out, element, "You are not allowed to update this site", oururl);
 		return;
 	    }
 
 	    ToolConfiguration toolConfig = SiteService.findTool(placement.getId());
 
 	    placement.getPlacementConfig().setProperty("url", 
 			      safetrim(req.getParameter("url")));
 	    placement.getPlacementConfig().setProperty("height", 
 		    	      safetrim(req.getParameter("height")));
 
 	    String newtitle = safetrim(req.getParameter("title"));
 	    if (newtitle != null && newtitle.equals(""))
 		newtitle = null;
 
 	    if (newtitle != null) {
 
 		placement.setTitle(safetrim(req.getParameter("title")));
 
 		if (toolConfig != null) {
 		    try {
 			Site site = SiteService.getSite(toolConfig.getSiteId());
 			SitePage page = site.getPage(toolConfig.getPageId());
 			page.setTitle(safetrim(req.getParameter("title")));
 			SiteService.save(site);
 		    } catch (Exception ignore) {}
 		}
 
 	    }
 
 	    placement.save();
 
 	    if (placement != null)
 		element = Web.escapeJavascript("Main" + placement.getId( ));
 
 	    if (placement != null)
    	      config = placement.getConfig();
 	    writeSetupPage(out, placement, element, config, oururl);
 
 	}
 
 	/**
 	 * Respond to data posting requests. Called from doPost for
 	 *   ?SignForm - when user submits a request for us to generate a
 	 *      signed object. Generate the object and display it
 	 * 
 	 * @param req
 	 *        The servlet request.
 	 * @param res
 	 *        The servlet response.
 	 * @throws ServletException.
 	 * @throws IOException.
 	 */
 
 	private void doSignForm(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
 	{
 	    Placement placement = ToolManager.getCurrentPlacement();
 	    PrintWriter out = res.getWriter();
 	    String userid = null;
 	    String element = null;
 	    String command = null;
 	    String signature = null;
 	    String oururl = req.getRequestURI();
 	    String object = null;
 	    String bodyonload = null;
 
 	    if (placement != null)
 		element = Web.escapeJavascript("Main" + placement.getId( ));
 	    else {
 		writeErrorPage(out, element, "Unable to find the current tool", oururl);
 		return;
 	    }
 
 	    Session s = SessionManager.getCurrentSession();
 	    if (s != null) {
 		// System.out.println("got session " + s.getId());
 		userid = s.getUserId();
 	    }
 
 	    if (userid == null) {
 		writeErrorPage(out, element, "Unable to figure out your userid", oururl);
 		return;
 	    }
 
 	    boolean isprived = SecurityService.getInstance().isSuperUser();
 
 	    if (isprived) {
 		String requser = safetrim(req.getParameter("user"));
 		String current = safetrim(req.getParameter("current"));
 
 		if (current != null && current.equals("yes"))
 		    command = "currentuser";
 		else if (requser != null & !requser.equals(""))
 		    command = "user=" + requser;
 		else {
 		    writeErrorPage(out, element, "No username supplied", oururl);
 		    return;
 		}
 		
 	    } else {
 		command = "user=" + userid;
 	    }
 
 	    if (command != null) {
 		try {
 		    signature = sign(command);
 		    object = command + "&sign=" + signature;
 		} catch (Exception e) {};
 	    }
 
 	    if (object == null) {
 		writeErrorPage(out, element, "Attempt to generate signed object failed", oururl);
 		return;
 	    }
 
 	    bodyonload = "setMainFrameHeight('" + element + "');setFocus(focus_path);";
 
 	    out.println(headHtml);
 	    out.println(stylesHtml);
 	    out.println(headHtml1 + "300px" + headHtml2 + bodyonload + headHtml3);
 
 	    out.println("<div class='portletBody'><h2>Your object</h2>");
 	    out.println("<p>Here is your object. You should copy it and then paste it into a configuration file to be used in your application.");
 	    out.println("<p>" + object);
 
 	    out.println("<p><a href='" + oururl + "?panel=Main'>Return to tool</a>");
 	    out.println("</div>");
 
 	    out.println(tailHtml);
 
 	}
 
 	/**
 	 * Sign a string with our private signing key. Returns a hex string
 	 * 
 	 * @param data
 	 *        The data to sign
 	 * @throws Exception.
 	 */
 
 	private static String sign(String data) throws Exception {
 	    Mac sig = Mac.getInstance("HmacSHA1");
 	    sig.init(salt);
             return byteArray2Hex(sig.doFinal(data.getBytes()));
         }
 
 	/**
 	 * Read our secret key from a file. returns the key
 	 * 
 	 * @param filename
 	 *        Contains the key in proper binary format
 	 */
 
 	private static SecretKey readSecretKey(String filename, String alg) {
 	    try {
 		FileInputStream file = new FileInputStream(filename);
 		byte[] bytes = new byte[file.available()];
 		file.read(bytes);
 		file.close();
 		SecretKey privkey = new SecretKeySpec(bytes, alg);
 		return privkey;
 	    } catch (Exception ignore) {
 		return null;
 	    }
 	}
 
     	private static char[] hexChars = {
 	    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
 	};
 
 	/**
 	 * Convert byte array to hex string
 	 * 
 	 * @param ba
 	 *        array of bytes
 	 * @throws Exception.
 	 */
 
 	private static String byteArray2Hex(byte[] ba){
 	    StringBuffer sb = new StringBuffer();
 	    for (int i = 0; i < ba.length; i++){
 		int hbits = (ba[i] & 0x000000f0) >> 4;
 		int lbits = ba[i] & 0x0000000f;
 		sb.append("" + hexChars[hbits] + hexChars[lbits]);
 	    }
 	    return sb.toString();
 	}
 
 	/**
 	 * Version of trim that won't blow up if fed null
 	 * 
 	 * @param a
 	 *        string
 	 */
 
 	private String safetrim(String s) {
 	    if (s == null)
 		return null;
 	    return s.trim();
 	}
 
         // genkey
 
         // from http://www.cs.ru.nl/~martijno/
         // Martijn Oostdijk. by permission
 
 	/**
 	 * Generate a secret key, and write it to a file
 	 * 
 	 * @param dirname
 	 *        writes to file privkeyname in this 
 	 *        directory. dirname assumed to end in /
 	 */
 
         private void genkey(String dirname) {
 	    try {
 		/* Generate key. */
 		System.out.println("Generating key...");
 		SecretKey key = KeyGenerator.getInstance("Blowfish").generateKey();
 
 		/* Write private key to file. */
 		writeKey(key, dirname + privkeyname);
 
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	}
 
         /**
 	 * Writes <code>key</code> to file with name <code>filename</code>
 	 *
 	 * @throws IOException if something goes wrong.
 	 */
         private static void writeKey(Key key, String filename) throws IOException {
 	    FileOutputStream file = new FileOutputStream(filename);
 	    file.write(key.getEncoded());
 	    file.close();
 	}
 
         // gensalt
 
 	/**
 	 * Generate a random salt, and write it to a file
 	 * 
 	 * @param dirname
 	 *        writes to file saltname in this 
 	 *        directory. dirname assumed to end in /
 	 */
 
         private void gensalt(String dirname) {
 	    try {
 		// Generate a key for the HMAC-SHA1 keyed-hashing algorithm
 		KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
 		SecretKey key = keyGen.generateKey();
 		writeKey(key, dirname + saltname);
 	    } catch (Exception e) {
 		e.printStackTrace();
 	    }
 	}
 
         public String encrypt(String str) {
 	    try {
 		Cipher ecipher = Cipher.getInstance("Blowfish");
 		ecipher.init(Cipher.ENCRYPT_MODE, secretKey);
 
 		// Encode the string into bytes using utf-8
 		byte[] utf8 = str.getBytes("UTF8");
 
 		// Encrypt
 		byte[] enc = ecipher.doFinal(utf8);
     
 		// Encode bytes to base64 to get a string
 		return byteArray2Hex(enc);
 	    } catch (javax.crypto.BadPaddingException e) {
 		System.out.println("linktool encrypt bad padding");
 	    } catch (javax.crypto.IllegalBlockSizeException e) {
 		System.out.println("linktool encrypt illegal block size");
 	    } catch (java.security.NoSuchAlgorithmException e) {
 		System.out.println("linktool encrypt no such algorithm");
 	    } catch (java.security.InvalidKeyException e) {
 		System.out.println("linktool encrypt invalid key");
 	    } catch (javax.crypto.NoSuchPaddingException e) {
 		System.out.println("linktool encrypt no such padding");
 	    } catch (java.io.UnsupportedEncodingException e) {
 		System.out.println("linktool encrypt unsupported encoding");
 	    } catch (java.io.IOException e) {
 		System.out.println("linktool encrypted io exc");
 	    }
 	    return null;
 	}
 
 }
 
 
 
 
