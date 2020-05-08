 package com.xaf.form.taglib;
 
 import java.io.*;
 import java.sql.*;
 
 import javax.naming.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 import javax.servlet.jsp.tagext.*;
 
 import com.xaf.form.*;
 import com.xaf.skin.*;
 
 public class DialogTag extends TagSupport
 {
 	private String name;
 	private String source;
 	private String skinName;
 	private Dialog dialog;
 	private String listenerAttrName;
 
 	public void release()
 	{
 		super.release();
 		name = null;
 		source = null;
 		skinName = null;
 		dialog = null;
 		listenerAttrName = null;
 	}
 
 	public String getName() { return name; }
 	public void setName(String value) {	name = value; }
 
 	public String getSource() { return source; }
 	public void setSource(String value) { source = value; }
 
 	public String getSkin() { return skinName; }
 	public void setSkin(String value) { skinName = value; }
 
 	public void setListener(String value) { listenerAttrName = value; }
 
 	public int doStartTag() throws JspException
 	{
 		try
 		{
 			JspWriter out = pageContext.getOut();
 			ServletContext context = pageContext.getServletContext();
 			DialogManager manager;
 			if(source == null)
 			{
 				manager = DialogManagerFactory.getManager(context);
 				if(manager == null)
 				{
 					out.write("DialogManager not found in ServletContext");
 					return SKIP_BODY;
 				}
 			}
 			else
 			{
 				manager = DialogManagerFactory.getManager(source);
 				if(manager == null)
 				{
 					out.write("DialogManager '"+source+"' not found.");
 					return SKIP_BODY;
 				}
 			}
 
 			Dialog dialog = manager.getDialog(name);
 			if(dialog == null)
 			{
 				out.write("Dialog '"+name+"' not found in manager '"+manager+"'.");
 				return SKIP_BODY;
 			}
 
 			DialogSkin skin = skinName == null ? SkinFactory.getDialogSkin() : SkinFactory.getDialogSkin(skinName);
 			if(skin == null)
 			{
 				out.write("DialogSkin '"+skinName+"' not found in skin factory.");
 				return SKIP_BODY;
 			}
 
 			DialogContext dc = new DialogContext(context, (Servlet) pageContext.getPage(), (HttpServletRequest) pageContext.getRequest(), (HttpServletResponse) pageContext.getResponse(), dialog, skin);
 			ServletRequest req = pageContext.getRequest();
 			if(listenerAttrName != null)
 			{
 				DialogContextListener listener = (DialogContextListener) req.getAttribute(listenerAttrName);
 				if(listener == null)
 					throw new JspException("No DialogContextListener found for listener attribute '"+listenerAttrName+"'");
 				dc.addListener(listener);
 			}
 			dialog.prepareContext(dc);
 
 		    // if the dialog class has not been overridden (base class) and
 			// there are no listeners attached and there are no execut tasks then
 			// we will handle the "execute" portion in the JSP unless listeners
 			// are attached
 			if(dc.inExecuteMode())
 			{
				dialog.execute(dc);
 				if(! dc.executeStageHandled())
 				{
 					// these two attributes are set because they are defined by
 					// the DialogTagTEI so that the nested body (the "execute" portion
 					// of the dialog) has full access to the dialog that was created
 					// as well as the context it's running in
 
 					pageContext.setAttribute("dialog", dialog);
 					pageContext.setAttribute("dialogContext", dc);
 					return EVAL_BODY_INCLUDE;
 				}
 				else
 					return SKIP_BODY;
 			}
 			else
 			{
 				out.write(dialog.getHtml(dc, true));
 				return SKIP_BODY;
 			}
 		}
 		catch(IOException e)
 		{
 			throw new JspException(e.toString());
 		}
 	}
 
 	public int doEndTag() throws JspException
 	{
 		return EVAL_PAGE;
 	}
 }
