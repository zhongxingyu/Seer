 package com.xaf.task.sql;
 
 import java.io.*;
 import java.sql.*;
 import java.util.*;
 
 import javax.naming.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.servlet.jsp.*;
 import javax.servlet.jsp.tagext.*;
 
 import com.xaf.report.*;
 import com.xaf.task.*;
 import com.xaf.task.sql.*;
 
 public class StatementTag extends TagSupport
 {
 	private StatementTask task = new StatementTask();
 	private String listenerAttrName;
 
 	public void release()
 	{
 		super.release();
 		task.reset();
 		listenerAttrName = null;
 	}
 
 	public void setDebug(String value) { if(value.equals("yes")) task.setFlag(Task.TASKFLAG_DEBUG); }
 	public void setName(String value) {	task.setStmtName(value); }
 	public void setStmtSource(String value) { task.setStmtSource(value); }
 	public void setDataSource(String value) { task.setDataSource(value); }
 	public void setListener(String value) { listenerAttrName = value; }
 	public void setReport(String value) { task.setReport(value); }
 	public void setSkin(String value) { task.setSkin(value); }
 	public void setStore(String value) { task.setStore(value); }
 	public void setDestination(String value) { task.setReportDestId(value); }
 	public void setStoreType(String value) { task.setStoreType(value); }
 
 	public int doStartTag() throws JspException
 	{
 		return EVAL_BODY_INCLUDE;
 	}
 
 	public int doEndTag() throws JspException
 	{
 		ServletRequest req = pageContext.getRequest();
 		if(listenerAttrName != null)
 		{
 			ReportContextListener listener = (ReportContextListener) req.getAttribute(listenerAttrName);
 		    if(listener == null)
 				throw new JspException("No ReportContextListener found for listener attribute '"+listenerAttrName+"'");
 			req.setAttribute(ReportContext.REQUESTATTRNAME_LISTENER, listener);
 		}
 
 		JspWriter out = pageContext.getOut();
 		TaskContext tc = new TaskContext(pageContext.getServletContext(), (Servlet) pageContext.getPage(), pageContext.getRequest(), pageContext.getResponse());
 		try
 		{
 			task.execute(tc);
 			if(tc.hasError())
				out.write(tc.getErrorMessage());
 			else if(tc.hasResultMessage())
 				out.write(tc.getResultMessage());
 		}
 		catch(IOException e)
 		{
 			throw new JspException(e.toString());
 		}
 		catch(TaskExecuteException e)
 		{
 			req.removeAttribute(ReportContext.REQUESTATTRNAME_LISTENER);
			try{ out.write(e.getDetailedMessage()); } catch(IOException ie) { throw new JspException(ie.getMessage()); }
 			return SKIP_PAGE;
 		}
 
 		req.removeAttribute(ReportContext.REQUESTATTRNAME_LISTENER);
 		return EVAL_PAGE;
 	}
 }
