 package com.xaf.navigate.taglib;
 
 import java.io.*;
 import java.util.*;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 import javax.servlet.jsp.tagext.*;
 
 import com.xaf.form.*;
 import com.xaf.security.*;
 import com.xaf.log.*;
 import com.xaf.skin.*;
 import com.xaf.value.*;
 import com.xaf.sql.StatementManager;
 import com.xaf.sql.StatementManagerFactory;
 import com.xaf.sql.query.QueryDefinition;
 import com.xaf.sql.query.QuerySelectDialog;
 
 public class PageTag extends TagSupport
 {
 	static public final String PAGE_COMMAND_REQUEST_PARAM_NAME = "cmd";
 	static public final String PAGE_SECURITY_MESSAGE_ATTRNAME = "security-message";
 	static public final String PAGE_DEFAULT_LOGIN_DIALOG_CLASS = "com.xaf.security.LoginDialog";
 
 	static public final String[] DIALOG_COMMAND_RETAIN_PARAMS =
 	{
 		PAGE_COMMAND_REQUEST_PARAM_NAME
 	};
 
 	static private LoginDialog loginDialog;
 
 	private String title;
 	private String heading;
 	private String[] permissions;
     private long startTime;
 
 	public void release()
 	{
 		super.release();
 		title = null;
 		heading = null;
 		permissions = null;
 	}
 
 	public final String getTitle() { return title; }
 	public final String getHeading() { return heading; }
 
 	public void setTitle(String value) { title = value; }
 	public void setHeading(String value) { heading = value;	}
 
 	public final String[] getPermissions() { return permissions; }
 	public void setPermission(String value)
 	{
 		if(value == null || value.length() == 0)
 			return;
 
 		List perms = new ArrayList();
 		StringTokenizer st = new StringTokenizer(value, ",");
 		while(st.hasMoreTokens())
 		{
 			perms.add(st.nextToken());
 		}
 		permissions = (String[]) perms.toArray(new String[perms.size()]);
 	}
 
 	public String getLoginDialogClassName()
 	{
 		return PAGE_DEFAULT_LOGIN_DIALOG_CLASS;
 	}
 
 	public String getLoginDialogSkinName()
 	{
 		return null; // the "default" skin
  	}
 
 	protected boolean doLogin(ServletContext servletContext, Servlet page, HttpServletRequest req, HttpServletResponse resp) throws IOException, JspException
 	{
 		if(loginDialog == null)
 		{
 			String className = getLoginDialogClassName();
 			try
 			{
 			    Class loginDialogClass = Class.forName(className);
 			    loginDialog = (LoginDialog) loginDialogClass.newInstance();
 			}
 			catch(ClassNotFoundException e)
 			{
 				throw new JspException("Login dialog class '"+ className +"' not found in classpath.");
 			}
 			catch(IllegalAccessException e)
 			{
 				throw new JspException("Unable to access login dialog class '"+ className +"'.");
 			}
 			catch(InstantiationException e)
 			{
 				throw new JspException("Unable to instantiate login dialog class '"+ className +"'.");
 			}
 			loginDialog.initialize();
 		}
 
 		String logout = req.getParameter("_logout");
 		if(logout != null)
 		{
 			ValueContext vc = new ServletValueContext(servletContext, page, req, resp);
 			loginDialog.logout(vc);
 
 			/** If the logout parameter included a non-zero length value, then
 			 *  we'll redirect to the value provided.
 			 */
 			if(logout.length() == 0 || logout.equals("1") || logout.equals("yes"))
 				resp.sendRedirect(req.getContextPath());
 			else
 				resp.sendRedirect(logout);
 			return true;
 		}
 
 		if(! loginDialog.accessAllowed(servletContext, req, resp))
 		{
 			String skinName = getLoginDialogSkinName();
 			DialogContext dc = loginDialog.createContext(servletContext, page, req, resp, skinName == null ? SkinFactory.getDialogSkin() : SkinFactory.getDialogSkin(skinName));
 			loginDialog.prepareContext(dc);
 			if(dc.inExecuteMode())
 			{
 				loginDialog.execute(dc);
 			}
 			else
 			{
 				loginDialog.producePage(dc, resp.getWriter());
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean hasPermission()
 	{
 		if(permissions == null)
 			return true;
 
 		HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
 
 		AuthenticatedUser user = (AuthenticatedUser) request.getSession(true).getAttribute("authenticated-user");
 		if(user == null)
 		{
 			request.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "No user identified.");
 			return false;
 		}
 
 		AccessControlList acl = AccessControlListFactory.getACL(pageContext.getServletContext());
 		if(acl == null)
 		{
 			request.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "No ACL defined.");
 			return false;
 		}
 
 		if(! user.hasAnyPermission(acl, permissions))
 		{
 			request.setAttribute(PAGE_SECURITY_MESSAGE_ATTRNAME, "Permission denied.");
 			return false;
 		}
 
 		return true;
 	}
 
     /**
      * When cmd=dialog is called, the expected parameters are:
      *   0 dialog name (required)
      *   1 data command like add,edit,delete,confirm (optional, may be empty or set to "-" to mean "none")
      *   2 skin name (optional, may be empty or set to "-" to mean "none")
      */
 
 	public void handleDialogInBody(String[] params) throws JspException
 	{
 		HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
 		String dialogName = params[0];
         String dataCmd = params.length > 1 ? ("-".equals(params[1]) ? null : params[1]) : null;
 		String skinName = params.length > 2 ? ("-".equals(params[2]) ? null : params[2]) : null;
 
         if(dataCmd != null)
             pageContext.getRequest().setAttribute(Dialog.PARAMNAME_DATA_CMD_INITIAL, dataCmd);
 
 		try
 		{
 			JspWriter out = pageContext.getOut();
 			ServletContext context = pageContext.getServletContext();
 			DialogManager manager = DialogManagerFactory.getManager(context);
 			if(manager == null)
 			{
 				out.write("DialogManager not found in ServletContext");
 				return;
 			}
 
 			Dialog dialog = manager.getDialog(dialogName);
 			if(dialog == null)
 			{
 				out.write("Dialog '"+dialogName+"' not found in manager '"+manager+"'.");
 				return;
 			}
 
 			DialogSkin skin = skinName == null ? SkinFactory.getDialogSkin() : SkinFactory.getDialogSkin(skinName);
 			if(skin == null)
 			{
 				out.write("DialogSkin '"+skinName+"' not found in skin factory.");
 				return;
 			}
 
 			DialogContext dc = dialog.createContext(context, (Servlet) pageContext.getPage(), (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), skin);
 			dc.setRetainRequestParams(DIALOG_COMMAND_RETAIN_PARAMS);
 			dialog.prepareContext(dc);
 
 			if(dc.inExecuteMode())
 			{
 				String html = dialog.execute(dc);
 				if(! dc.executeStageHandled())
 				{
 					out.write("Dialog '"+dialogName+"' did not handle the execute mode.<p>");
 					out.write(dc.getDebugHtml());
 				}
 				else if(html != null)
 					out.write(html);
 			}
 			else
 				out.write(dialog.getHtml(dc, true));
 		}
 		catch(IOException e)
 		{
 			throw new JspException(e.toString());
 		}
 	}
 
     /**
      * When cmd=qd-dialog is called, the expected parameters are:
      *   0 query definition name (required)
      *   1 dialog name (required)
      *   2 skin name (optional, may be empty or set to "-" to mean "none")
      */
 
     public void handleQuerySelectDialogInBody(String[] params) throws JspException
 	{
 		HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
         String source = params[0];
         String dialogName = params[1];
 		String skinName = params.length > 2 ? ("-".equals(params[2]) ? null : params[2]) : null;
 
         try
 		{
 			JspWriter out = pageContext.getOut();
 			ServletContext context = pageContext.getServletContext();
 
     		StatementManager manager = StatementManagerFactory.getManager(context);
             if(manager == null)
             {
                 out.write("StatementManager not found in ServletContext");
                 return;
             }
 
     		QueryDefinition queryDefn = manager.getQueryDefn(source);
             if(queryDefn == null)
             {
                 out.write("QueryDefinition '"+source+"' not found in StatementManager");
                 return;
             }
 
 	    	QuerySelectDialog dialog = queryDefn.getSelectDialog(dialogName);
             if(dialog == null)
             {
                 out.write("QuerySelectDialog '"+dialogName+"' not found in QueryDefinition '"+ source +"'");
                 return;
             }
 
 			DialogSkin skin = skinName == null ? SkinFactory.getDialogSkin() : SkinFactory.getDialogSkin(skinName);
 			if(skin == null)
 			{
 				out.write("DialogSkin '"+skinName+"' not found in skin factory.");
 				return;
 			}
 
 			DialogContext dc = dialog.createContext(pageContext.getServletContext(), (Servlet) pageContext.getPage(), (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), skin);
             dc.setRetainRequestParams(DIALOG_COMMAND_RETAIN_PARAMS);
 			dialog.prepareContext(dc);
 
 			out.write(dialog.getHtml(dc, true));
 			return;
 		}
 		catch(IOException e)
 		{
 			throw new JspException(e.toString());
 		}
 	}
 
 	public boolean handleDefaultBodyItem() throws JspException
 	{
 		HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
 		String pageCmdParam = request.getParameter(PAGE_COMMAND_REQUEST_PARAM_NAME);
 		if(pageCmdParam == null)
 			return false;
 
         StringTokenizer st = new StringTokenizer(pageCmdParam, ",");
         String pageCmd = st.nextToken();
         List pageCmdParamsList = new ArrayList();
         while(st.hasMoreTokens())
             pageCmdParamsList.add(st.nextToken());
 
         String[] pageCmdParamsArray = null;
         if(pageCmdParamsList.size() > 0)
             pageCmdParamsArray = (String[]) pageCmdParamsList.toArray(new String[pageCmdParamsList.size()]);
 
 		// a "standard" page command needs to be handled
 		if(pageCmd.equals("dialog"))
 		    handleDialogInBody(pageCmdParamsArray);
         else if(pageCmd.equals("qd-dialog"))
             handleQuerySelectDialogInBody(pageCmdParamsArray);
         else
         {
             try
             {
                 pageContext.getResponse().getWriter().write("Page command '"+ pageCmd +"' not recognized.");
             }
             catch(IOException e)
             {
                throw new JspException(e.toString());
             }
             return false;
         }
 
 		return true;
 	}
 
 	public int doStartTag() throws JspException
 	{
 		return EVAL_BODY_INCLUDE;
 	}
 
 	public int doEndTag() throws JspException
 	{
 		return EVAL_PAGE;
 	}
 
     /**
      * Records the start time when the page is loaded
      */
     public void doPageBegin()
     {
         startTime = new Date().getTime();
         HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
         org.apache.log4j.NDC.push(request.getSession(true).getId());
     }
 
     /**
      * Records the total time when the page is finished loading
      */
     public void doPageEnd()
     {
         HttpServletRequest request = ((HttpServletRequest) pageContext.getRequest());
         com.xaf.log.LogManager.recordAccess(request, null, pageContext.getPage().getClass().getName(), request.getRequestURI(), startTime);
         org.apache.log4j.NDC.pop();
     }
 }
