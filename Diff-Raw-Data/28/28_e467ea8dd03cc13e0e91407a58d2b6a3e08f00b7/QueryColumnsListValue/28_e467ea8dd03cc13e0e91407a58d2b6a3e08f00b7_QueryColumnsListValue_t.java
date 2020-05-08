 package com.xaf.value;
 
 import java.io.*;
 import java.util.*;
 import java.sql.*;
 
 import javax.naming.*;
 import javax.servlet.http.*;
 
 import com.xaf.form.*;
 import com.xaf.form.field.*;
 import com.xaf.db.*;
 import com.xaf.sql.*;
 
 public class QueryColumnsListValue extends ListSource
 {
 	private String stmtMgrName;
 	private String dataSourceId;
 	private String stmtName;
 
     public QueryColumnsListValue()
     {
     }
 
 	public QueryColumnsListValue(String stmtMgrName, String dataSourceId, String queryName)
 	{
 		this.stmtMgrName = stmtMgrName;
 		this.dataSourceId = dataSourceId;
 		this.stmtName = queryName;
 	}
 
     public void initializeSource(String srcParams)
     {
 		super.initializeSource(srcParams);
         int delimPos = srcParams.indexOf('/');
         if(delimPos >= 0)
         {
             stmtMgrName = srcParams.substring(0, delimPos);
             stmtName = srcParams.substring(delimPos+1);
         }
         else
             this.stmtName = srcParams;
     }
 
     public ResultSet getResultSet(ValueContext vc) throws NamingException, StatementNotFoundException, SQLException, StatementExecutionException
     {
         StatementManager stmtMgr = stmtMgrName == null ? StatementManagerFactory.getManager(vc.getServletContext()) : StatementManagerFactory.getManager(stmtMgrName);
         DatabaseContext dbContext = DatabaseContextFactory.getContext(vc.getRequest(), vc.getServletContext(), false);
 
         StatementManager.ResultInfo ri = stmtMgr.execute(dbContext, vc, dataSourceId, stmtName);
         return ri.getResultSet();
     }
 
 
     public SelectChoicesList getSelectChoices(ValueContext vc)
     {
 		SelectChoicesList choices = new SelectChoicesList();
 
 		ResultSet rs = null;
 		try
 		{
 			rs = getResultSet(vc);
 			ResultSetMetaData rsmd = rs.getMetaData();
 			int colsCount = rsmd.getColumnCount();
 
 			if(rs.next())
 			{
 				for(int i = 1; i <= colsCount; i++)
 				{
 					choices.add(new SelectChoice(rsmd.getColumnLabel(i), rs.getString(i)));
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			choices.add(new SelectChoice(e.toString()));
 		}
 		finally
 		{
 			try
 			{
 				if(rs != null)
                 {
                    Connection con = rs.getStatement().getConnection();
 					rs.close();
                    con.close();
                 }
 			}
 			catch(Exception e)
 			{
 				choices.add(new SelectChoice(e.toString()));
 			}
 		}
 
         return choices;
 	}
 
     public String[] getValues(ValueContext vc)
     {
 		ResultSet rs = null;
 		try
 		{
 			String[] result = StatementManager.getResultSetSingleRowAsStrings(getResultSet(vc));
 			if(result == null)
 				return new String[] { "" };
 			else
 				return result;
 		}
 		catch(Exception e)
 		{
 			return new String[] { e.toString() };
 		}
 		finally
 		{
 			try
 			{
 				if(rs != null)
                 {
                    Connection con = rs.getStatement().getConnection();
 					rs.close();
                    con.close();
                 }
 
 			}
 			catch(Exception e)
 			{
 				return new String[] { e.toString() };
 			}
 		}
 	}
 }
