 package business.dataEdit.lawFL;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.axiom.om.OMElement;
 
 import client.Service1Stub;
 import client.Service1Stub.Ds_checkinfo_type1;
 
 //import business.dataEdit.lawCheck.TreeItem;
 import service.TreeItem;
 //import admin.Search.SearchItem;
 import service.SearchItem;
 
 import db.DBFactory;
 
 public class LawFLDAOImpl implements LawFLDAO {
 	
 	
 	public List<SearchItem> GetSearchItem(String param,HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 
 		sql = param;
 			
 		String ip=null,user=null,pwd=null,dbName=null,port=null;
 		
 		try
 		{		
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery();
 			
 			//if(rs.next()){
 				ResultSetMetaData rsmd = rs.getMetaData();
 				for(int i=1;i<=rsmd.getColumnCount();i++){					
 					SearchItem item = new SearchItem(rsmd.getColumnName(i),rsmd.getColumnName(i),"String");
 					itemAry.add(item);
 				}
 			//}
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}	public List<SearchItem> GetSearchItem2(String param,HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 
 		sql = param;
 			
 		String ip=null,user=null,pwd=null,dbName=null,port=null;
 		
 		try
 		{		
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 		
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery();
 			
 			//if(rs.next()){
 				ResultSetMetaData rsmd = rs.getMetaData();
 				for(int i=1;i<=rsmd.getColumnCount();i++){					
 					SearchItem item = new SearchItem(rsmd.getColumnName(i),rsmd.getColumnName(i),"String");
 					itemAry.add(item);
 				}
 			//}
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}
 	public List<ComboItem> GetSearchComboItem(String param,HttpServletRequest request) throws Exception {
 		List<ComboItem> itemAry = new ArrayList<ComboItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 //----------------------------------------------------------------------------
 		sql = "SELECT * FROM [dbo].[BJ_Ʒñ] where =''";
 			
 		String ip=null,user=null,pwd=null,dbName=null,port=null;
 		
 		try
 		{		
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 					String s1=rs.getString("Ʒ");
 					String s2 =rs.getString("ȼ");
 				ComboItem item = new ComboItem(s1,s2);
 				itemAry.add(item);
 			}
 			
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}
 	public List<SearchItem> GetdbfItem(String param,HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		String dbfStr = "";
 		SearchItem item = null;
 
 		sql = param;
 			
 		String ip=null,user=null,pwd=null,dbName=null,port=null;
 		
 		try
 		{			
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory() ;		
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery();
 			
 //			int count=0;   
 //		    if(rs.next()){   
 //		    	rs.last();   
 //		    	count=rs.getRow();   
 //		    	rs.beforeFirst();   
 //		    }
 //		    
 //			while(rs.next()){
 //				int row = rs.getRow();
 //				int len1 = rs.getString(row).getBytes().length;
 //				int len2 = rs.getString(row+1).getBytes().length;
 //			}
 
 			if(rs.next()){
 				ResultSetMetaData rsmd = rs.getMetaData();
 				for(int i=1;i<=rsmd.getColumnCount();i++){
 					
 					String strCol = rsmd.getColumnName(i);
 					if("".equals(strCol) || "".equals(strCol) || "ȫ".equals(strCol) || "ժҪ".equals(strCol) || "ע".equals(strCol)){
 						dbfStr = dbfStr + rsmd.getColumnName(i) + ",M;";
 						continue;
 					}
 					
 					if("int".equals(rsmd.getColumnTypeName(i))){
 						if(String.valueOf(rs.getInt(i)).getBytes().length >= 200)
 							dbfStr = dbfStr + rsmd.getColumnName(i) + ",M;";
 						else
 							dbfStr = dbfStr + rsmd.getColumnName(i) + ",C,254;";
 					}else{
 						if(rs.getString(i) == null)
 							dbfStr = dbfStr + rsmd.getColumnName(i) + ",C,254;";
 						else if(rs.getString(i).getBytes().length >= 200)
 							dbfStr = dbfStr + rsmd.getColumnName(i) + ",M;";
 						else
 							dbfStr = dbfStr + rsmd.getColumnName(i) + ",C,254;";
 					}						
 				}				
 			}
 			item = new SearchItem(dbfStr,dbfStr);
 			itemAry.add(item);
 			
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}
 	
 	public int getCompareTotal(String paramLog,HttpServletRequest request) throws Exception
     {
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		int num = 0;
 
 		sql =paramLog;
 		
 		String ip=null,user=null,pwd=null,dbName=null,port=null;
 		
         try
         {
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
         	
         	
         	//ȡǰҳ
         	db = new DBFactory() ;
         	if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				num = rs.getInt(1);
 			}
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         }
         finally
 		{
 			db.close();
 		}
         return num;
     }
 	
 	public List<FLSearchItem> GetApplyList(String userName,String pname,HttpServletRequest request) throws Exception {
 		List<FLSearchItem> itemAry = new ArrayList<FLSearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		
 
 		try
 		{	
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?,?,?,?,?,?,?)}"); 
 			cstmt.setString(1, userName);
 			
 			cstmt.registerOutParameter(2, Types.INTEGER); 
 			cstmt.registerOutParameter(3, Types.VARCHAR); 
 			cstmt.registerOutParameter(4, Types.INTEGER);
 			cstmt.registerOutParameter(5, Types.VARCHAR);
 			cstmt.registerOutParameter(6, Types.INTEGER);
 			cstmt.registerOutParameter(7, Types.VARCHAR);
 			cstmt.registerOutParameter(8, Types.INTEGER);
 			cstmt.registerOutParameter(9, Types.INTEGER);
 			cstmt.registerOutParameter(10, Types.VARCHAR);			
 
 			cstmt.execute();
 			
 			FLSearchItem item = new FLSearchItem(cstmt.getInt(2),cstmt.getInt(4),cstmt.getInt(8),cstmt.getInt(9),cstmt.getString(10));
 			itemAry.add(item);
 
 			cstmt.close();	
 			
 			return itemAry;
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public String setApplyTask(String userName,int applyNum,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int num = 0;
 		int m = 0;
 		int retRows = 0;
 		String error = "";
 		String strReason = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			
 			for(int j=0;j<applyNum;j++){
 				cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 				cstmt.setString(1, userName);
 				cstmt.setInt(2, 1);
 				
 				cstmt.registerOutParameter(3, Types.INTEGER);
 				cstmt.registerOutParameter(4, Types.VARCHAR); 			
 
 				cstmt.execute();
 				retRows = cstmt.getInt(3);
 				error = cstmt.getString(4);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=error+"\r\n";
 					request.getSession().setAttribute("compareProgressApply",applyNum);
 					break;
 				}else{
 					++num;
 					request.getSession().setAttribute("compareProgressApply",++m);
 				}
 			}
 			
 			if("".equals(strReason))
 				return "ѳɹ"+num+"";
 			else
 				return strReason;
 			
 			
 			
 //			if(applyNum>10){
 //				for(int i=0;i<=applyNum;i+=10){
 //					cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 //					cstmt.setString(1, userName);
 //					cstmt.setInt(2, 10);
 //					
 //					cstmt.registerOutParameter(3, Types.INTEGER);
 //					cstmt.registerOutParameter(4, Types.VARCHAR); 			
 //
 //					cstmt.execute();
 //					retRows = cstmt.getInt(3);
 //					error = cstmt.getString(4);
 //					
 //					if(retRows == 0){
 //						cstmt.close();
 //						return error;
 //					}
 //					num = num + retRows;
 //					
 //					applyNum = applyNum - 10;
 //					
 //					cstmt.close();
 //				}
 //				if(applyNum>0){
 //					cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 //					cstmt.setString(1, userName);
 //					cstmt.setInt(2, applyNum);
 //					
 //					cstmt.registerOutParameter(3, Types.INTEGER);
 //					cstmt.registerOutParameter(4, Types.VARCHAR); 			
 //	
 //					cstmt.execute();
 //					retRows = cstmt.getInt(3);
 //					error = cstmt.getString(4);
 //					
 //					if(retRows == 0){
 //						cstmt.close();
 //						return error;
 //					}
 //					num = num + retRows;
 //					
 //					cstmt.close();	
 //				}
 //				return "ɹ"+num+"";
 //			}else{
 //				cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 //				cstmt.setString(1, userName);
 //				cstmt.setInt(2, applyNum);
 //				
 //				cstmt.registerOutParameter(3, Types.INTEGER);
 //				cstmt.registerOutParameter(4, Types.VARCHAR); 			
 //
 //				cstmt.execute();
 //				retRows = cstmt.getInt(3);
 //				error = cstmt.getString(4);
 //				
 //				if(retRows == 0){
 //					cstmt.close();	
 //					return error;
 //				}else{
 //					cstmt.close();
 //					return "ɹ"+retRows+"";
 //				}
 //			}	
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public String setErrorApplyTask(String userName,int applyNum,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		
 
 		try
 		{	
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
 			
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 			cstmt.setString(1, userName);
 			cstmt.setInt(2, applyNum);
 			
 			cstmt.registerOutParameter(3, Types.INTEGER);
 			cstmt.registerOutParameter(4, Types.VARCHAR); 			
 
 			cstmt.execute();
 			retRows = cstmt.getInt(3);
 			error = cstmt.getString(4);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				return error;
 			}else{
 				cstmt.close();
 				return "ɹ"+applyNum+"";
 			}	
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public String GetLawFLClick(int taskId,String newValue,String colName,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		
 
 		try
 		{		
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?,?)}"); 
 			cstmt.setInt(1, taskId);
 			cstmt.setString(2, newValue);
 			cstmt.setString(3, colName);
 	
 			cstmt.registerOutParameter(4, Types.INTEGER);
 			cstmt.registerOutParameter(5, Types.VARCHAR);
 			
 			cstmt.execute();
 			retRows = cstmt.getInt(4);
 			error = cstmt.getString(5);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				return error;
 			}else{
 				cstmt.close();	
 				return "";
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public String GetSgSave(String sn,String newValue,String colName,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		
 
 		try
 		{		
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?,?)}"); 
 			cstmt.setString(1, sn);
 			cstmt.setString(2, newValue);
 			cstmt.setString(3, colName);
 	
 			cstmt.registerOutParameter(4, Types.INTEGER);
 			cstmt.registerOutParameter(5, Types.VARCHAR);
 			
 			cstmt.execute();
 			retRows = cstmt.getInt(4);
 			error = cstmt.getString(5);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				return error;
 			}else{
 				cstmt.close();	
 				return "";
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 
 	public List<LawArticleInfo> GetLawArticleInfoGrid(String articleId,String type,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
         	
         	
         	//ȡǰҳ
         	db = new DBFactory() ;
         	if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			//sql = "select * from ҵ༭.[dbo].[V_BJ_Ϣ] WITH(NOLOCK) where ±=?";
         	
        	conn = db.getConnection("192.168.60.244", "sa", "newspaper", "ҵ༭", "1433");
         	
         	sql = "select * from ҵ༭.[dbo].GetInfo(?,?)";
 			pstmt = conn.prepareStatement(sql);
 			//pstmt.setString(1, articleId);
 			pstmt.setString(1, articleId);
 			pstmt.setString(2, type);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setArticleName(rs.getString("ƪ"));
 				article.setProfessionalName(rs.getString("רҵ"));
 				article.setResearch(rs.getString("о"));
 				article.setProperty(rs.getString(""));
 				article.setAuthor(rs.getString(""));
 				article.setSummary(rs.getString("ժҪ"));
 				article.setText(rs.getString(""));
 				article.setArticleSn(rs.getString("±"));
 				article.setPublishName(rs.getString(""));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			db.close();
 		}
         return articles;
     }
 	
 	public String GetInfoHtml(String articleId,String type,String fntree,HttpServletRequest request) throws Exception
     {		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		String message = "";
 		
         try
         {
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
         	
         	
         	//ȡǰҳ
         	db = new DBFactory() ;
         	if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
         	sql = "select "+fntree+"_"+type+"(?) as htm";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				message = rs.getString("htm");
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			db.close();
 		}
         return message;
     }
 	
 	public List<LawArticleInfo> GetSgArticleInfoGrid(String articleId,String name,String type,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
         	
         	
         	//ȡǰҳ
         	db = new DBFactory() ;
         	if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
         	sql = "select * from "+name+"(?,?)";
 			pstmt = conn.prepareStatement(sql);
 			//pstmt.setString(1, articleId);
 			pstmt.setString(1, articleId);
 			pstmt.setString(2, type);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setArticleName(rs.getString("ƪ"));
 				article.setProfessionalName(rs.getString("ɾ"));
 				article.setProperty(rs.getString("ʱ"));
 				article.setAuthor(rs.getString(""));
 				article.setText(rs.getString(""));
 				article.setArticleSn(rs.getString("±"));
 				article.setPublishName(rs.getString(""));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			db.close();
 		}
         return articles;
     }
 	
 	public List<SearchItem> GetLocationItem(String locTable,HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 
 		sql = "select * from "+locTable;
 			
 		try
 		{		
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			if(locTable.length()<2){}
 			else{
 			rs = pstmt.executeQuery();
 			
 				ResultSetMetaData rsmd = rs.getMetaData();
 				for(int i=1;i<=rsmd.getColumnCount();i++){					
 					SearchItem item = new SearchItem(rsmd.getColumnName(i),rsmd.getColumnName(i),"String");
 					itemAry.add(item);
 				}
 			
 			rs.close();
 			pstmt.close();
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 		
 	}
 	
 	public List<SearchItem> GetLocationName(String locTable,HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
        if(locTable.equals("")){
     	   return null;
        }
        else{
 		sql = "select  from "+locTable+" where =1";
 			
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			
 			itemAry.add(new SearchItem("ȫ",""));
 			
 			rs = pstmt.executeQuery();
 			while(rs.next()){
 				SearchItem item = new SearchItem(rs.getString(""),rs.getString(""));
 				itemAry.add(item);
 			}
 
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
        }
 	}
 	
 	public List<ComboItem> GetErrorType(String nav,String errorPost,HttpServletRequest request) throws Exception {
 		List<ComboItem> itemAry = new ArrayList<ComboItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 
 		sql = "select  from [dbo].["+nav+"_ͱ] WITH(NOLOCK) where λ=?";
 			
 		try
 		{		
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, errorPost);
 			
 			rs = pstmt.executeQuery();
 			while(rs.next()){
 				ComboItem item = new ComboItem(rs.getString(""),rs.getString(""));
 				itemAry.add(item);
 			}
 
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}
 	
 	public String submitLawFL(String pname,String str,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			int j=1;
 			String[] temp = str.split("###");
 			for(String tem:temp){
 				String[] temp1 = tem.split("@@@");
 				
 				cstmt = conn.prepareCall("{call "+pname+"(?,?,?)}"); 
 				cstmt.setInt(1, Integer.parseInt(temp1[0]));
 		
 				cstmt.registerOutParameter(2, Types.INTEGER);
 				cstmt.registerOutParameter(3, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(2);
 				error = cstmt.getString(3);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=temp1[1]+error+"\r\n";
 				}else{
 					cstmt.close();	
 				}	
 				
 				request.getSession().setAttribute("compareProgressSubmit", j++);
 			}	
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 	public String submitSgFaith(String pname,String str,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 				
 			if(str == null || "".equals(str)){
 				strReason = "ô洢ʧ!";
 				return strReason;
 			}
 			
 			
 			int j=1;
 			String[] temp = str.split("###");
 			for(String tem:temp){
 				String[] temp1 = tem.split("@@@");
 				
 				cstmt = conn.prepareCall("{call "+pname+"(?,?,?)}"); 
 				cstmt.setInt(1, Integer.parseInt(temp1[0]));
 		
 				cstmt.registerOutParameter(2, Types.INTEGER);
 				cstmt.registerOutParameter(3, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(2);
 				error = cstmt.getString(3);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=temp1[1]+error+"\r\n";
 				}else{
 					cstmt.close();	
 				}	
 				
 				request.getSession().setAttribute("compareProgressSubmit", j++);
 			}	
 
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 	public String deleteFunc(String ids,String states,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{		
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			String[] num = ids.split(",");
 			String[] state = states.split(",");
 			for(int i=0;i<num.length;i++){			
 				cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 				cstmt.setInt(1, Integer.parseInt(num[i]));
 				cstmt.setString(2, state[i]);
 		
 				cstmt.registerOutParameter(3, Types.INTEGER);
 				cstmt.registerOutParameter(4, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(3);
 				error = cstmt.getString(4);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=num[i]+' '+error+"\r\n";
 				}else{
 					cstmt.close();	
 				}	
 				
 			}	
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 //	public String deleteFunc(String ids) throws Exception {
 //		
 //		DBFactory db 			= null;
 //	    Connection conn 		= null;
 //		PreparedStatement pstmt	= null;
 //		String sql				= null;
 //		int num = 0;
 //
 //		sql = "delete from [dbo].[BJ__ƪ_] where  in(?)";
 //			
 //		try
 //		{			
 //			db = new DBFactory() ;
 //			conn = db.getConnection();
 //
 //			pstmt = conn.prepareStatement(sql);
 //			pstmt.setString(1, ids);
 //			
 //			num  = pstmt.executeUpdate();
 //			pstmt.close();
 //		}
 //		catch(Exception e)
 //		{
 //			e.printStackTrace();
 //			throw e;
 //		}
 //		finally
 //		{
 //			db.close();
 //		}
 //		return "ɹɾ"+num+"¼";
 //	}
 	
 	
 //	public String updateDbf(String path) throws Exception {
 //		
 //		DBFactory db 			= null;
 //	    Connection conn 		= null;
 //		PreparedStatement pstmt	= null;
 //		ResultSet rs = null;
 //		String sql				= null;
 //		int num = 0;
 //			
 //
 //		sql = "select * from [dbo].[LAW_༭Ϣ] WITH(NOLOCK)";
 //			
 //		try
 //		{			
 //			db = new DBFactory() ;
 //			conn = db.getConnection();
 //
 //			pstmt = conn.prepareStatement(sql);
 //			rs = pstmt.executeQuery();
 //			
 //			
 //			DBF fDbf = null;
 //			fDbf = new DBF(path , true, "GBK");
 //			for(int i=0;i<fDbf.getRecordCount();i++){
 //				fDbf.read();
 ////				for(int j=0;j<fDbf.getFieldCount();j++){
 ////					Field field = fDbf.getField(j);
 ////				}
 //				Field field = fDbf.getField("Filename");
 //				Field title = fDbf.getField("Title");
 //				while(rs.next()){
 //					if(field.getName().equals(rs.getString("±"))){
 //						title.put(rs.getString("ͼ"));
 //						num++;
 //					}
 //				}
 //			}
 //			
 //			fDbf.close();
 //			rs.close();
 //			pstmt.close();
 //		}
 //		catch(Exception e)
 //		{
 //			e.printStackTrace();
 //			throw e;
 //		}
 //		finally
 //		{
 //			db.close();
 //		}
 //		return "ɹ"+num+"¼";
 //	}
 	
 //	public void deleteLogItem(String logDelId) throws Exception {
 //		DBFactory db 			= null;
 //	    Connection conn 		= null;
 //		PreparedStatement pstmt	= null;
 //		String sql				= null;
 //		
 //		// дSQL
 //		sql = "delete from webmonlog where logId in ("+logDelId+") ";
 //
 //		try
 //		{
 //			db = new DBFactory();
 //			conn = db.getConnection();
 //
 //			pstmt = conn.prepareStatement(sql);
 //			pstmt.execute();
 //			pstmt.close();
 //		}
 //		catch(Exception e)
 //		{
 //			e.printStackTrace();
 //		}
 //		finally
 //		{
 //			db.close();
 //		}
 //	}
 //
 //	public void deleteAllTable() throws Exception {
 //		DBFactory db 			= null;
 //	    Connection conn 		= null;
 //		PreparedStatement pstmt	= null;
 //		String sql				= null;
 //		
 //		// дSQL
 //		sql = "delete from webmonlog  ";
 //
 //		try
 //		{
 //			db = new DBFactory();
 //			conn = db.getConnection();
 //
 //			pstmt = conn.prepareStatement(sql);
 //			pstmt.execute();
 //			pstmt.close();
 //		}
 //		catch(Exception e)
 //		{
 //			e.printStackTrace();
 //		}
 //		finally
 //		{
 //			db.close();
 //		}
 //	}
 //	
 //	public void deleteLogAllItem(String sqlWhere) throws Exception {
 //		DBFactory db 			= null;
 //	    Connection conn 		= null;
 //		PreparedStatement pstmt	= null;
 //		String sql				= null;
 //		
 //		// дSQL
 //		sql = "delete from webmonlog  "+sqlWhere;
 //
 //		try
 //		{
 //			db = new DBFactory();
 //			conn = db.getConnection();
 //
 //			pstmt = conn.prepareStatement(sql);
 //			pstmt.execute();
 //			pstmt.close();
 //		}
 //		catch(Exception e)
 //		{
 //			e.printStackTrace();
 //		}
 //		finally
 //		{
 //			db.close();
 //		}
 //	}
 	
 	
 	public String GCapply(String userName,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int num = 0;
 		int retRows = 0;
 		String error = "";
 		String strReason = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?)}"); 
 			cstmt.setString(1, userName);
 			cstmt.setInt(2, 1);
 			
 			cstmt.registerOutParameter(3, Types.INTEGER);
 			cstmt.registerOutParameter(4, Types.VARCHAR); 			
 
 			cstmt.execute();
 			retRows = cstmt.getInt(3);
 			error = cstmt.getString(4);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				strReason+=error+"\r\n";
 			}else{
 				++num;
 			}
 				
 				
 				//request.getSession().setAttribute("compareProgressApply",++m);
 			
 			return strReason;
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public List<LawArticleInfo> GetLawGCInfoGrid(String articleId,String type,String sptask,String fntree,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		PreparedStatement pstmt1	= null;
 		ResultSet rs1			= null;
 		String sql				= null;
 		String sql1             = null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			//sql = "select * from "+sptask+" WITH(NOLOCK) where ±=?";
 			sql = "select * from ҵ༭.[dbo].GetInfo(?,?)";
 			sql1 = "select * from "+fntree+" WITH(NOLOCK) where ±=?";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			pstmt.setString(2, type);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setArticleName(rs.getString("ƪ"));
 				article.setProfessionalName(rs.getString("רҵ"));
 				article.setResearch(rs.getString("о"));
 				article.setProperty(rs.getString(""));
 				article.setAuthor(rs.getString(""));
 				article.setSummary(rs.getString("ժҪ"));
 				article.setText(rs.getString(""));
 				
 				pstmt1 = conn.prepareStatement(sql1);
 				pstmt1.setString(1, articleId);
 				rs1 = pstmt1.executeQuery();
 				String reference="",errorType="",errorDes="",errorTime="",errorPost="",enter="<br/>";
 				while(rs1.next()){
 					errorType = rs1.getString("");
 					errorDes = rs1.getString("");
 					errorTime = rs1.getString("Ǵʱ").substring(0, rs1.getString("Ǵʱ").length()-2);
 					errorPost = rs1.getString("Ǵλ");
 					if(errorDes != null)
 						reference+=errorTime+"		"+errorPost+"		"+errorType+"		"+errorDes+enter;
 					else
 						reference+=errorTime+"		"+errorPost+"		"+errorType+enter;
 				}
 				//System.out.print(reference);
 				article.setReference(reference);
 				
 				articles.add(article);
 				
 				rs1.close();
 				pstmt1.close();
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			db.close();
 		}
         return articles;
     }
 	
 	
 	public String GetLine() throws Exception {
 		String str = "";
 		
 		for(int i=0;i<10240;i++){
 			str+="a";
 		}
 
 		return str;
 	}
 	
 	public List<SearchItem> GetTotalNum(String spup,HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		int num = 0;
 		
 		sql = spup;
 
 			
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();

 			pstmt = conn.prepareStatement(sql);
 			
 			
 			rs = pstmt.executeQuery();
 			while(rs.next()){
 				num = rs.getInt(1);
 				SearchItem item = new SearchItem(String.valueOf(num),String.valueOf(num));
 				itemAry.add(item);
 			}
 
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
//			e.printStackTrace();
//			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}
 	
 	
 	
 	
 	
 	public List<LawArticleInfo> GetmedGCInfoGrid(String articleId,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		PreparedStatement pstmt1	= null;
 		ResultSet rs1			= null;
 		String sql				= null;
 		String sql1             = null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from [dbo].[V_BJ_Ϣ] WITH(NOLOCK) where ±=?";
 			sql1 = "select * from [dbo].[MED_Ϣ] WITH(NOLOCK) where ±=?";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setArticleName(rs.getString("ƪ"));
 				article.setProfessionalName(rs.getString("רҵ"));
 				article.setResearch(rs.getString("о"));
 				article.setProperty(rs.getString(""));
 				article.setAuthor(rs.getString(""));
 				article.setSummary(rs.getString("ժҪ"));
 				article.setText(rs.getString(""));
 				
 				pstmt1 = conn.prepareStatement(sql1);
 				pstmt1.setString(1, articleId);
 				rs1 = pstmt1.executeQuery();
 				String reference="",errorType="",errorDes="",errorTime="",errorPost="",enter="<br/>";
 				while(rs1.next()){
 					errorType = rs1.getString("");
 					errorDes = rs1.getString("");
 					errorTime = rs1.getString("Ǵʱ").substring(0, rs1.getString("Ǵʱ").length()-2);
 					errorPost = rs1.getString("Ǵλ");
 					if(errorDes != null)
 						reference+=errorTime+"		"+errorPost+"		"+errorType+"		"+errorDes+enter;
 					else
 						reference+=errorTime+"		"+errorPost+"		"+errorType+enter;
 				}
 				article.setReference(reference);
 				
 				articles.add(article);
 				
 				rs1.close();
 				pstmt1.close();
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			db.close();
 		}
         return articles;
     }
 	
 	
 	public List<HashMap<String,String>> GetLocationGrid(String navsp,String text,String name,String type,HttpServletRequest request) throws Exception
     {
 		List<HashMap<String,String>> jsonList = new ArrayList<HashMap<String,String>>();
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from "+ navsp +"(?,?,?) order by Ŀ";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, text);
 			pstmt.setString(2, name);
 			pstmt.setString(3, type);
 			
 			rs = pstmt.executeQuery() ;
 			ResultSetMetaData rsData=rs.getMetaData(); 
 			
 			while(rs.next()){
 				HashMap<String,String> itemMap=new HashMap<String,String> ();
 				for(int i=1; i<=rsData.getColumnCount(); ++i)
 				{
 					if("int".equals(rsData.getColumnTypeName(i)))
 					{
 						itemMap.put(rsData.getColumnName(i), String.valueOf(rs.getInt(i)));
 					}
 					else
 					{
 						itemMap.put(rsData.getColumnName(i), rs.getString(i));
 					}
 				}
 				jsonList.add(itemMap);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         return jsonList;
     }
 	
 	
 	
 	
 	public String GetFileInfo(String strUrl,String sn,HttpServletRequest request) throws Exception
     {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from ҵ༭.dbo.[Fu_Rep_GetFile](?,?)";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			pstmt.setString(2, "ڿ");
 			
 			rs = pstmt.executeQuery() ;
 			String fileId="",server = "",table="";
 			if(rs.next()){
 				fileId = rs.getString("ļ");
 				table = rs.getString("ļλ");
 				server = rs.getString("");
 			}						
 			rs.close();
 			pstmt.close();
 			
 			pstmt = conn.prepareStatement("select * from ͨƽ̨.dbo.LINE_ӷ  where =?");
 			pstmt.setString(1, server);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				ip = rs.getString("IP");
 				user = rs.getString("˺");
 				pwd = rs.getString("");
 				dbName = rs.getString("ݿ");
 				port = rs.getString("˿");
 			}
 			rs.close();
 			pstmt.close();
 			
 			
 			
 			String filename = "";			
 			
 			String sPath = LawFLDAOImpl.class.getResource("").getPath();
 			int nPos = sPath.indexOf("WEB-INF");
 			if(nPos < 0)
 				return "";
 			String tempPath = sPath.substring(1, nPos);
 			sPath = tempPath + "create_file/";
 			File f = new File(sPath);
 			if(!f.exists()){
 				f.mkdirs();
 			}
 			
 			nPos = strUrl.indexOf("WebBj");
 			if(nPos < 0)
 				return "";
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				return "";
 			
 			pstmt = conn.prepareStatement("select ļ, from "+table+" where ļ=?");
 			pstmt.setString(1, fileId);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				filename = rs.getString("ļ");
 				
 				File ff = new File(sPath+filename);
 				if(ff.exists()){
 					System.out.println(sPath+filename);
 					System.out.println(strUrl.substring(0, nPos) + "WebBj/create_file/" + filename);
 					return sPath+filename;
 					//return strUrl.substring(0, nPos) + "WebBj/create_file/" + filename;
 				}
 				
 
 				try{
 					FileOutputStream out = new FileOutputStream(ff);
 					InputStream in = rs.getBinaryStream("");
 					byte[] buf = new byte[10240]; 
 		            int readLen = 0; 
 					
 					while ((readLen = in.read(buf, 0, 10240)) != -1) {
 						out.write(buf, 0, readLen);
 					}
 					
 					out.flush();
 					in.close();
 					out.close();
 				}catch(Exception e){
 						
 					e.printStackTrace();
 				}
 			}
 
 				
 			rs.close();
 			pstmt.close();
 			
 			System.out.println(sPath+filename);
 			System.out.println(strUrl.substring(0, nPos) + "WebBj/create_file/" + filename);
 			return sPath + filename;			
 			//return strUrl.substring(0, nPos) + "WebLawLine/create_file/" + filename;
 	
 			
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         
     }
 	
 	
 	public String GetFileInfo1(String strUrl,String sn,HttpServletRequest request) throws Exception
     {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from ҵ༭.dbo.[Fu_Rep_GetFile](?,?)";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			pstmt.setString(2, "ڿ");
 			
 			rs = pstmt.executeQuery() ;
 			String fileId="",server = "",table="";
 			if(rs.next()){
 				fileId = rs.getString("ļ");
 				table = rs.getString("ļλ");
 				server = rs.getString("");
 			}						
 			rs.close();
 			pstmt.close();
 			
 			pstmt = conn.prepareStatement("select * from ͨƽ̨.dbo.LINE_ӷ  where =?");
 			pstmt.setString(1, server);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				ip = rs.getString("IP");
 				user = rs.getString("˺");
 				pwd = rs.getString("");
 				dbName = rs.getString("ݿ");
 				port = rs.getString("˿");
 			}
 			rs.close();
 			pstmt.close();
 			
 						
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				return null;
 			
 			pstmt = conn.prepareStatement("select ļ, from "+table+" where ļ=?");
 			pstmt.setString(1, fileId);
 			rs = pstmt.executeQuery();			
 
 			String page = "";	
 			if(rs.next()){
 				try{
 	
 					page = rs.getBinaryStream("").toString();
 					
 	
 				}catch(Exception e){
 						
 					e.printStackTrace();
 				}
 			}
 				
 			rs.close();
 			pstmt.close();
 			
 			return page;				
 			
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         
     }
 	
 	
 	public String GetDownFileName(String sn,HttpServletRequest request) throws Exception
     {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from ҵ༭.dbo.[Fu_Rep_GetFile](?,?)";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			pstmt.setString(2, "ڿ");
 			
 			rs = pstmt.executeQuery() ;
 			String fileId="",server = "",table="";
 			if(rs.next()){
 				fileId = rs.getString("ļ");
 				table = rs.getString("ļλ");
 				server = rs.getString("");
 			}						
 			rs.close();
 			pstmt.close();
 			
 			pstmt = conn.prepareStatement("select * from ͨƽ̨.dbo.LINE_ӷ  where =?");
 			pstmt.setString(1, server);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				ip = rs.getString("IP");
 				user = rs.getString("˺");
 				pwd = rs.getString("");
 				dbName = rs.getString("ݿ");
 				port = rs.getString("˿");
 			}
 			rs.close();
 			pstmt.close();
 			
 			
 			
 			String filename = "";			
 			
 			String sPath = LawFLDAOImpl.class.getResource("").getPath();
 			int nPos = sPath.indexOf("WEB-INF");
 			if(nPos < 0)
 				return "";
 			String tempPath = sPath.substring(1, nPos);
 			sPath = tempPath + "create_file/";
 			File f = new File(sPath);
 			if(!f.exists()){
 				f.mkdirs();
 			}
 			
 			//nPos = strUrl.indexOf("WebBj");
 			if(nPos < 0)
 				return "";
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				return "";
 			
 			pstmt = conn.prepareStatement("select ļ, from "+table+" where ļ=?");
 			pstmt.setString(1, fileId);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				filename = rs.getString("ļ");
 				
 				File ff = new File(sPath+filename);
 				if(ff.exists()){
 					System.out.println(sPath+filename);
 					//System.out.println(strUrl.substring(0, nPos) + "WebBj/create_file/" + filename);
 					return sPath+filename;
 					//return strUrl.substring(0, nPos) + "WebBj/create_file/" + filename;
 				}
 				
 
 				try{
 					FileOutputStream out = new FileOutputStream(ff);
 					InputStream in = rs.getBinaryStream("");
 					byte[] buf = new byte[10240]; 
 		            int readLen = 0; 
 					
 					while ((readLen = in.read(buf, 0, 10240)) != -1) {
 						out.write(buf, 0, readLen);
 					}
 					
 					out.flush();
 					in.close();
 					out.close();
 				}catch(Exception e){
 						
 					e.printStackTrace();
 				}
 			}
 
 				
 			rs.close();
 			pstmt.close();
 			
 			System.out.println(sPath+filename);
 			if("".equals(filename))
 					return "";
 			return sPath + filename;			
 			//return strUrl.substring(0, nPos) + "WebLawLine/create_file/" + filename;
 	
 			
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         
     }
 	
 	
 	public String isExist(String sn,HttpServletRequest request) throws Exception
     {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from ҵ༭.dbo.[Fu_Rep_GetFile](?,?)";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			pstmt.setString(2, "ڿ");
 			
 			rs = pstmt.executeQuery() ;
 			String fileId="",server = "",table="";
 			if(rs.next()){
 				fileId = rs.getString("ļ");
 				table = rs.getString("ļλ");
 				server = rs.getString("");
 			}						
 			rs.close();
 			pstmt.close();
 			
 			pstmt = conn.prepareStatement("select * from ͨƽ̨.dbo.LINE_ӷ  where =?");
 			pstmt.setString(1, server);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				ip = rs.getString("IP");
 				user = rs.getString("˺");
 				pwd = rs.getString("");
 				dbName = rs.getString("ݿ");
 				port = rs.getString("˿");
 			}
 			rs.close();
 			pstmt.close();
 			
 			
 			
 			String filename = "";			
 			
 			String sPath = LawFLDAOImpl.class.getResource("").getPath();
 			int nPos = sPath.indexOf("WEB-INF");
 			if(nPos < 0)
 				return "";
 			String tempPath = sPath.substring(1, nPos);
 			sPath = tempPath + "create_file/";
 			File f = new File(sPath);
 			if(!f.exists()){
 				f.mkdirs();
 			}
 			
 			//nPos = strUrl.indexOf("WebBj");
 			if(nPos < 0)
 				return "";
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				return "";
 			
 			pstmt = conn.prepareStatement("select ļ, from "+table+" where ļ=?");
 			pstmt.setString(1, fileId);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				filename = rs.getString("ļ");
 				
 				File ff = new File(sPath+filename);
 				if(ff.exists()){
 					return sPath+filename;
 				}
 
 			}
 				
 			rs.close();
 			pstmt.close();
 			
 			//System.out.println(sPath+filename);
 			if("".equals(filename))
 					return "";
 			return sPath + filename;			
 	
 			
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         
     }
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static Connection getConnection(HttpServletRequest request){
 		String ip=null,user=null,pwd=null,dbName=null,port=null;     	
     	Connection conn = null;
 		try {
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			DBFactory db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}			
 		
 		return conn;
 	}
 	
 	
 	
 	public List<TreeItem> GetJunCheckTree(String userName,String fntree,HttpServletRequest request) throws Exception
     {
 		List<TreeItem> treeItems = new ArrayList<TreeItem>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	conn = getConnection(request);
 			
 			sql = "select * from "+ fntree +"(?) order by ʱ desc";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, userName);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				TreeItem item = new TreeItem();
 				item.setId(rs.getString(""));
 				item.setParentId("0");
 				item.setName(rs.getString("±"));
 				item.setChecked(false);
 				item.setLeaf(true);
 				
 				treeItems.add(item);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         return treeItems;
     }
 	
 	public List<TreeItem> GetJunCheckTree(String userName,String fntree) throws Exception
     {
 		List<TreeItem> treeItems = new ArrayList<TreeItem>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	DBFactory db = new DBFactory();
         	conn = db.getConnection("192.168.60.244","sa","newspaper","","1433");
 			
 			sql = "select * from "+ fntree +"(?) order by ʱ desc";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, userName);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				TreeItem item = new TreeItem();
 				item.setId(rs.getString(""));
 				item.setParentId("0");
 				item.setName(rs.getString("±"));
 				item.setChecked(false);
 				item.setLeaf(true);
 				
 				treeItems.add(item);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         return treeItems;
     }
 	
 	
 	public List<LawArticleInfo> GetJunArticleInfoGrid(String articleId,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			//sql = "select * from ҵ༭.[dbo].[V_BJ_Ϣ] WITH(NOLOCK) where ±=?";
         	sql = "select * from [dbo].GetInfo(?)";
 			pstmt = conn.prepareStatement(sql);
 			//pstmt.setString(1, articleId);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setArticleSn(rs.getString(""));
 				article.setPublishName(rs.getString(""));
 				article.setProperty(rs.getString(""));
 				article.setAuthor(rs.getString("Ʊ"));
 				article.setReference(rs.getString("ֶ"));
 				article.setText(rs.getString("ȫıȶ"));
 				article.setSummary(rs.getString("ƪ"));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public String setContent(String sn,String content,String nav,String type,HttpServletRequest request) throws Exception {
 		
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		String strReason = "";
 		
 
 		try
 		{			
 			conn = getConnection(request);			
 			
 			//cstmt = conn.prepareCall("{call "+nav+"(?,?,?,?,?)}"); 
 			cstmt = conn.prepareCall("{call SP_ѧλ_SaveInfo(?,?,?,?,?)}"); 
 			cstmt.setString(1, sn);
 			cstmt.setString(2, content);
 			cstmt.setString(3, type);
 			
 			cstmt.registerOutParameter(4, Types.INTEGER);
 			cstmt.registerOutParameter(5, Types.VARCHAR); 			
 
 			cstmt.execute();
 			retRows = cstmt.getInt(4);
 			error = cstmt.getString(5);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				strReason+=error+"\r\n";
 			}
 
 			
 			return strReason;
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			conn.close();
 		}
 	}
 	
 	public String backJunCheck(String id,String ztdm,String navsp,HttpServletRequest request) throws Exception {
 		
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		String strReason = "";
 		
 
 		try
 		{			
 			conn = getConnection(request);	
 			
 			if(id != null && !"".equals(id)){		
 				cstmt = conn.prepareCall("{call "+navsp+"(?,?,?,?)}"); 
 				cstmt.setInt(1, Integer.parseInt(id));
 				cstmt.setString(2, ztdm);
 				
 				cstmt.registerOutParameter(3, Types.INTEGER);
 				cstmt.registerOutParameter(4, Types.VARCHAR); 			
 	
 				cstmt.execute();
 				retRows = cstmt.getInt(3);
 				error = cstmt.getString(4);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=error+"\r\n";
 				}
 			}else{
 				strReason = "Ϊ!";
 			}
 
 			
 			return strReason;
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			conn.close();
 		}
 	}
 	
 	public List<LawArticleInfo> GetContent(String sn,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			sql = "select ӹ˵ from .[dbo].[SG__] WITH(NOLOCK) where ±=? and λ=''";
         	//sql = "select * from [dbo].GetInfo(?)";
 			pstmt = conn.prepareStatement(sql);
 			//pstmt.setString(1, articleId);
 			pstmt.setString(1, sn);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setSummary(rs.getString("ӹ˵"));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public List<LawArticleInfo> GetSenContent(String sn,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			sql = "select ӹ˵ from .[dbo].[SG__] WITH(NOLOCK) where ±=? and λ=''";
         	//sql = "select * from [dbo].GetInfo(?)";
 			pstmt = conn.prepareStatement(sql);
 			//pstmt.setString(1, articleId);
 			pstmt.setString(1, sn);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setSummary(rs.getString("ӹ˵"));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public String setDecision(String sn,String content,String spup,String type,HttpServletRequest request) throws Exception {
 		
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		String strReason = "";
 		
 
 		try
 		{			
 			conn = getConnection(request);			
 			
 			//cstmt = conn.prepareCall("{call "+spup+"(?,?,?,?,?)}"); 
 			cstmt = conn.prepareCall("{call SP_ѧλ_SaveInfo(?,?,?,?,?)}"); 
 			cstmt.setString(1, sn);
 			cstmt.setString(2, content);
 			cstmt.setString(3, type);
 			
 			cstmt.registerOutParameter(4, Types.INTEGER);
 			cstmt.registerOutParameter(5, Types.VARCHAR); 			
 
 			cstmt.execute();
 			retRows = cstmt.getInt(4);
 			error = cstmt.getString(5);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				strReason+=error+"\r\n";
 			}
 
 			
 			return strReason;
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			conn.close();
 		}
 	}
 	
 	public List<LawArticleInfo> GetDecision(String sn,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			sql = "select  from .[dbo].[SG_Ϣ] WITH(NOLOCK) where ±=?";
         	//sql = "select * from [dbo].GetInfo(?)";
 			pstmt = conn.prepareStatement(sql);
 			//pstmt.setString(1, articleId);
 			pstmt.setString(1, sn);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setSummary(rs.getString(""));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public String submitJun(String pname,String str,String check,String userName,String spget,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");			
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 						
 			String[] ids = str.split(",");
 			for(String id:ids){
 				cstmt = conn.prepareCall("{call "+pname+"(?,?,?)}"); 
 				cstmt.setInt(1, Integer.parseInt(id));
 		
 				cstmt.registerOutParameter(2, Types.INTEGER);
 				cstmt.registerOutParameter(3, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(2);
 				error = cstmt.getString(3);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=error;
 				}else{
 					cstmt.close();	
 				}					
 			}
 			
 			if("true".equals(check) && "".equals(strReason)){
 				cstmt = conn.prepareCall("{call "+spget+"(?,?,?,?)}"); 
 				cstmt.setString(1, userName);
 				cstmt.setInt(2, 1);
 		
 				cstmt.registerOutParameter(3, Types.INTEGER);
 				cstmt.registerOutParameter(4, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(3);
 				error = cstmt.getString(4);
 				
 				if(retRows == 0){
 					cstmt.close();
 					if(!"".equals(error))
 						strReason = error;
 					strReason = "Ŀǰûп";
 				}else{
 					cstmt.close();	
 				}					
 			}
 				
 //			cstmt = conn.prepareCall("{call "+pname+"(?,?,?)}"); 
 //			cstmt.setInt(1, index);
 //	
 //			cstmt.registerOutParameter(2, Types.INTEGER);
 //			cstmt.registerOutParameter(3, Types.VARCHAR);
 //			
 //			cstmt.execute();
 //			retRows = cstmt.getInt(2);
 //			error = cstmt.getString(3);
 //			
 //			if(retRows == 0){
 //				cstmt.close();	
 //				strReason=error;
 //			}else{
 //				cstmt.close();	
 //			}					
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 	public String submitSen(String pname,String str,String check,String userName,String spget,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");			
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?)}"); 
 			cstmt.setString(1, str);
 	
 			cstmt.registerOutParameter(2, Types.INTEGER);
 			cstmt.registerOutParameter(3, Types.VARCHAR);
 			
 			cstmt.execute();
 			retRows = cstmt.getInt(2);
 			error = cstmt.getString(3);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				strReason+=error;
 			}else{
 				cstmt.close();	
 			}					
 			
 			if("true".equals(check) && "".equals(strReason)){
 				cstmt = conn.prepareCall("{call "+spget+"(?,?,?,?)}"); 
 				cstmt.setString(1, userName);
 				cstmt.setInt(2, 1);
 		
 				cstmt.registerOutParameter(3, Types.INTEGER);
 				cstmt.registerOutParameter(4, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(3);
 				error = cstmt.getString(4);
 				
 				if(retRows == 0){
 					cstmt.close();
 					if(!"".equals(error))
 						strReason = error;
 					strReason = "Ŀǰûп";
 				}else{
 					cstmt.close();	
 				}					
 			}
 				
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 	public String articleDelete(String articleSn,String fileNames,String percent,String remain,String spDelete,HttpServletRequest request) throws Exception {
 		
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		String strReason = "";
 		
 
 		try
 		{			
 			conn = getConnection(request);			
 			
 			cstmt = conn.prepareCall("{call "+spDelete+"(?,?,?,?,?,?)}"); 
 			cstmt.setString(1, articleSn);
 			cstmt.setString(2, fileNames);
 			cstmt.setInt(3, Integer.parseInt(percent));
 			cstmt.setString(4, remain);
 			
 			cstmt.registerOutParameter(5, Types.INTEGER);
 			cstmt.registerOutParameter(6, Types.VARCHAR); 			
 
 			cstmt.execute();
 			retRows = cstmt.getInt(5);
 			error = cstmt.getString(6);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				strReason=error;
 			}
 
 			
 			return strReason;
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			strReason = "ô洢̴!";
 			throw e;
 		}
 		finally
 		{
 			conn.close();
 		}
 	}
 	
 	public List<SearchItem> GetComboxStore(HttpServletRequest request) throws Exception {
 		List<SearchItem> itemAry = new ArrayList<SearchItem>(); 
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 
 		sql = "select * from V_SG_רЭ ";
 			
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory() ;
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery();
 			while(rs.next()){
 				SearchItem item = new SearchItem(rs.getString("ר"),rs.getString("ר"));
 				itemAry.add(item);
 			}
 
 			rs.close();
 			pstmt.close();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return itemAry;
 	}
 	
 	
 	
 	public List<TreeItem> GetProAuthorTree(String userName,String fntree,HttpServletRequest request) throws Exception
     {
 		List<TreeItem> treeItems = new ArrayList<TreeItem>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	conn = getConnection(request);
 			
 			sql = "select * from "+ fntree +"(?) order by ʱ desc";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, userName);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				TreeItem item = new TreeItem();
 				item.setId(rs.getString(""));
 				item.setParentId("-1");
 				item.setName(rs.getString(""));
 				item.setText(rs.getString(""));
 				item.setChecked(false);
 				item.setLeaf(true);
 				
 				treeItems.add(item);
 			}
 			
 			if(treeItems.size() != 0){
 				TreeItem ti = new TreeItem();
 				ti.setId("-1");
 				ti.setText("п");
 				ti.setName("п");
 				ti.setLeaf(false);
 				ti.setChecked(false);
 				ti.setParentId("0");
 				treeItems.add(0, ti);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         return treeItems;
     }
 	
 	public String GetProAuthorHtml(String articleId,HttpServletRequest request) throws Exception
     {
 		Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		String htm = "";
 		
         try
         {
         	conn = getConnection(request);
 			
         	sql = "select [dbo].FU_JOU_GetAutHtml(?) as htm";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				htm = rs.getString("htm");
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return htm;
     }
 	
 	public String GetJouFirstHtml(String articleId,HttpServletRequest request) throws Exception
     {
 		Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		String htm = "";
 		
         try
         {
         	conn = getConnection(request);
 			
         	sql = "select [dbo].Fu_JOU_GetFirstHtml(?) as htm";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				htm = rs.getString("htm");
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return htm;
     }
 	
 	public String GetCovFLClick(String taskId,String newValue,String colName,String pname,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String error = "";
 		
 
 		try
 		{		
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?,?,?)}"); 
 			cstmt.setString(1, taskId);
 			cstmt.setString(2, newValue);
 			cstmt.setString(3, colName);
 	
 			cstmt.registerOutParameter(4, Types.INTEGER);
 			cstmt.registerOutParameter(5, Types.VARCHAR);
 			
 			cstmt.execute();
 			retRows = cstmt.getInt(4);
 			error = cstmt.getString(5);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				return error;
 			}else{
 				cstmt.close();	
 				return "";
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 	}
 	
 	public String downCovFile(String sn,String procedure,String strUrl,HttpServletRequest request) throws Exception
     {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			sql = "select * from "+procedure+"(?)";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			
 			rs = pstmt.executeQuery() ;
 			String fileId="",server = "",table="";
 			if(rs.next()){
 				fileId = rs.getString("ļ");
 				table = rs.getString("ļλ");
 				server = rs.getString("");
 			}						
 			rs.close();
 			pstmt.close();
 			
 			pstmt = conn.prepareStatement("select * from ͨƽ̨.dbo.LINE_ӷ  where =?");
 			pstmt.setString(1, server);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				ip = rs.getString("IP");
 				user = rs.getString("˺");
 				pwd = rs.getString("");
 				dbName = rs.getString("ݿ");
 				port = rs.getString("˿");
 			}
 			rs.close();
 			pstmt.close();
 			
 			
 			
 			String filename = "";			
 			
 			String sPath = LawFLDAOImpl.class.getResource("").getPath();
 			int nPos = sPath.indexOf("WEB-INF");
 			if(nPos < 0)
 				return "";
 			String tempPath = sPath.substring(1, nPos);
 			sPath = tempPath + "create_file/";
 			File f = new File(sPath);
 			if(!f.exists()){
 				f.mkdirs();
 			}
 			
 			
 			
 			//nPos = strUrl.indexOf("WebBj");
 			
 			
 			
 			if(nPos < 0)
 				return "";
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				return "";
 			
 			pstmt = conn.prepareStatement("select ļ, from "+table+" where ļ=?");
 			pstmt.setString(1, fileId);
 			rs = pstmt.executeQuery();
 			
 			if(rs.next()){
 				filename = rs.getString("ļ");
 				
 				File ff = new File(sPath+filename);
 				if(ff.exists()){
 					System.out.println(sPath+filename);
 					return sPath+filename;
 					//System.out.println(strUrl.substring(0, nPos) + "WebBj/create_file/" + filename);
 					//return strUrl.substring(0, nPos) + "WebBj/create_file/" + filename;
 				}
 				
 
 				try{
 					FileOutputStream out = new FileOutputStream(ff);
 					InputStream in = rs.getBinaryStream("");
 					byte[] buf = new byte[10240]; 
 		            int readLen = 0; 
 					
 					while ((readLen = in.read(buf, 0, 10240)) != -1) {
 						out.write(buf, 0, readLen);
 					}
 					
 					out.flush();
 					in.close();
 					out.close();
 				}catch(Exception e){
 						
 					e.printStackTrace();
 				}
 			}
 
 				
 			rs.close();
 			pstmt.close();
 			
 			if("".equals(filename))
 					return "";
 			return sPath + filename;			
 			//return strUrl.substring(0, nPos) + "WebBj/create_file/" + filename;
 	
 			
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         
     }
 	
 	public String submitCover(String pname,String str,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");			
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 
 
 			cstmt = conn.prepareCall("{call "+pname+"(?,?,?)}"); 
 			cstmt.setString(1, str);
 	
 			cstmt.registerOutParameter(2, Types.INTEGER);
 			cstmt.registerOutParameter(3, Types.VARCHAR);
 			
 			cstmt.execute();
 			retRows = cstmt.getInt(2);
 			error = cstmt.getString(3);
 			
 			if(retRows == 0){
 				cstmt.close();	
 				strReason = error;
 			}else{
 				cstmt.close();	
 			}	
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 	public List<TreeItem> GetArticleTree(String sn,String fntree,HttpServletRequest request) throws Exception
     {
 		List<TreeItem> treeItems = new ArrayList<TreeItem>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	conn = getConnection(request);
 			
 			sql = "select * from "+ fntree +"(?) order by ű";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				TreeItem item = new TreeItem();
 				item.setId(rs.getString(""));
 				item.setParentId("0");
 				item.setName(rs.getString("±"));
 				item.setText(rs.getString("±"));
 				item.setLevel(rs.getString("ű"));
 				item.setChecked(false);
 				item.setLeaf(true);
 				
 				treeItems.add(item);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         return treeItems;
     }
 	
 	
 	public String GetCompare(String userName,String password,String sn,String fileNames,String similar,String articleSn,HttpServletRequest request){
 		try {	
 			
 			Connection conn 		= null;
 			PreparedStatement pstmt	= null;
 			ResultSet rs			= null;
 			String sql				= null;
 			
 			Service1Stub stub = new Service1Stub();
 			Service1Stub.GetCheckinfoStep step = new Service1Stub.GetCheckinfoStep();
 			
 			step.setStr_uid(userName);
 			step.setStr_pwd(password);
 			step.setStr_LeftName(sn);
 			step.setStr_RightNameList(fileNames);
 			step.setMess("");
 			
 			Ds_checkinfo_type1 sss = stub.getCheckinfoStep(step).getDs_checkinfo();
 			OMElement text = sss.getExtraElement();
 			String string = text.toString();
 			String remain = "";
 
 			if(string.contains("<ָƱ>") && string.contains("</ȫı>")){
 				int start = string.indexOf("<ָƱ>");
 				int end = string.indexOf("</ȫı>");
 				remain = string.substring(start, end+7);
 			}
 			remain = remain.replaceAll("'", "''");
 			//System.out.println(remain);
 			
 			String str = "";
 			conn = getConnection(request);
 			pstmt = conn.prepareStatement("select dbo.SG_GetStepInfoHtml('"+articleSn+"','"+remain+"') as htm");
 			rs = pstmt.executeQuery();
 			
 			while(rs.next()){
 				str = rs.getString("htm");
 			}
 
 			return str;
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 		
 	}	
 	
 	
 	
 	public List<ComboItem> GetMaintainList(String type,String post,HttpServletRequest request) throws Exception
     {
 		List<ComboItem> items = new ArrayList<ComboItem>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	//ȡǰҳ
         	conn = getConnection(request);
         	
         	if("".equals(post)){
         		sql = "select * from BJ__ͱ where ='"+type+"'";      	
     			
     			pstmt = conn.prepareStatement(sql);
     			//pstmt.setString(1, sn);
     			
     			rs = pstmt.executeQuery() ;
     			
     			while(rs.next()){
     				ComboItem item = new ComboItem(rs.getString(""),rs.getString(""));				
     				items.add(item);
     			}
         	}else{      	
 	        	sql = "select * from SG_ͱ where Ʒ='"+type+"'";      	
 				
 				pstmt = conn.prepareStatement(sql);
 				//pstmt.setString(1, sn);
 				
 				rs = pstmt.executeQuery() ;
 				
 				while(rs.next()){
 					ComboItem item = new ComboItem(rs.getString(""),rs.getString(""));				
 					items.add(item);
 				}
         	}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	e.printStackTrace();
         	throw e;
         }
         finally
 		{
         	conn.close();
 		}
         return items;
     }
 	
 	public List<LawArticleInfo> GetSenContent1(String sn,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			sql = "select ˵ from [dbo].[BJ__] WITH(NOLOCK) where ±=? and λ=''";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, sn);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setSummary(rs.getString("˵"));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public List<LawArticleInfo> GetDecision1(String articleId,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			sql = "select  from [dbo].BJ_ѧλ_λϢ  WITH(NOLOCK) where =?";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setSummary(rs.getString(""));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public List<LawArticleInfo> GetContent1(String articleId,HttpServletRequest request) throws Exception
     {
 		List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 		
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
         try
         {
         	conn = getConnection(request);
 			
 			sql = "select ˵ from [dbo].[BJ__] WITH(NOLOCK) where =?";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			
 			rs = pstmt.executeQuery() ;
 			
 			while(rs.next()){
 				LawArticleInfo article = new LawArticleInfo();
 				article.setSummary(rs.getString("˵"));
 				
 				articles.add(article);
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			conn.close();
 		}
         return articles;
     }
 	
 	public String GetMuluInfoHtml(String articleId,String sn,String fntree,HttpServletRequest request) throws Exception
     {		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		String message = "";
 		
         try
         {
         	String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
         	ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
         	
         	
         	//ȡǰҳ
         	db = new DBFactory() ;
         	if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
         	sql = "select "+fntree+"(?,?) as htm";
 			pstmt = conn.prepareStatement(sql);
 			pstmt.setString(1, articleId);
 			pstmt.setString(2, sn);
 			
 			rs = pstmt.executeQuery() ;
 			
 			if(rs.next()){
 				message = rs.getString("htm");
 			}
 			
 			rs.close();
 			pstmt.close();
         }
         catch (Exception e)
         {
         	throw e;
         }
         finally
 		{
 			db.close();
 		}
         return message;
     }
 
 	@Override
 	public void execute(String string, HttpServletRequest request) throws Exception {
 		// TODO Auto-generated method stub
 				 DBFactory db 			= null;
 				    Connection conn 		= null;
 					PreparedStatement pstmt	= null;
 					ResultSet rs			= null;
 					String sql				= null;
 					String message = "";
 					
 			        try
 			        {
 			        	String ip=null,user=null,pwd=null,dbName=null,port=null;
 			        	
 			        	ip = (String)request.getSession().getAttribute("ip");
 						if(ip != null)
 							ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 						user = (String)request.getSession().getAttribute("user");
 						if(user != null)
 							user = new String(user.getBytes("iso-8859-1"),"gbk");	
 						pwd = (String)request.getSession().getAttribute("pwd");
 						if(pwd != null)
 							pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 						dbName = (String)request.getSession().getAttribute("dbName");
 						if(dbName != null)
 							dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 						port = (String)request.getSession().getAttribute("port");
 						if(port != null)
 							port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			        	
 			        	
 			        	//ȡǰҳ
 			        	db = new DBFactory() ;
 			        	if(ip != null && user!=null && pwd!=null)				
 							conn = db.getConnection(ip,user,pwd,dbName,port);
 						else
 							conn = db.getConnection();
 						
 						pstmt = conn.prepareStatement(string);
 						System.out.print(string);
 						pstmt.execute();
 						
 						
 						pstmt.close();
 			        }
 			        catch (Exception e)
 			        {
 			        	throw e;
 			        }
 			        finally
 					{
 						db.close();
 					}
 			 
 		
 	}
 	@Override
 	public void executeSql(String string, HttpServletRequest request)
 			throws Exception {
 		    DBFactory db 			= null;
 		    Connection conn 		= null;
 			PreparedStatement pstmt	= null;
 			ResultSet rs			= null;
 			String sql				= null;
 			String message = "";
 			
 	        try
 	        {
 	        	String ip=null,user=null,pwd=null,dbName=null,port=null;
 	        	
 	        	ip = (String)request.getSession().getAttribute("ip");
 				if(ip != null)
 					ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 				user = (String)request.getSession().getAttribute("user");
 				if(user != null)
 					user = new String(user.getBytes("iso-8859-1"),"gbk");	
 				pwd = (String)request.getSession().getAttribute("pwd");
 				if(pwd != null)
 					pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 				dbName = (String)request.getSession().getAttribute("dbName");
 				if(dbName != null)
 					dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 				port = (String)request.getSession().getAttribute("port");
 				if(port != null)
 					port = new String(port.getBytes("iso-8859-1"),"gbk");	
 	        	
 	        	
 	        	//ȡǰҳ
 	        	db = new DBFactory() ;
 	        	if(ip != null && user!=null && pwd!=null)				
 					conn = db.getConnection(ip,user,pwd,dbName,port);
 				else
 					conn = db.getConnection();
 				
 				pstmt = conn.prepareStatement(string);
 				System.out.print("execute======="+string);
 				pstmt.execute();
 				
 				
 				pstmt.close();
 	        }
 	        catch (Exception e)
 	        {
 	        	throw e;
 	        }
 	        finally
 			{
 				db.close();
 			}
 	 
 		
 	}
 
 public String submitLawFL2(String pname,String str,HttpServletRequest request) throws Exception {
 		
 		DBFactory db 			= null;
 	    Connection conn 		= null;
 		CallableStatement cstmt = null;
 		int retRows = 0;
 		String strReason = "";
 		String error = "";
 		
 
 		try
 		{			
 			String ip=null,user=null,pwd=null,dbName=null,port=null;
         	
 			ip = (String)request.getSession().getAttribute("ip");
 			if(ip != null)
 				ip = new String(ip.getBytes("iso-8859-1"),"gbk");	
 			user = (String)request.getSession().getAttribute("user");
 			if(user != null)
 				user = new String(user.getBytes("iso-8859-1"),"gbk");	
 			pwd = (String)request.getSession().getAttribute("pwd");
 			if(pwd != null)
 				pwd = new String(pwd.getBytes("iso-8859-1"),"gbk");	
 			dbName = (String)request.getSession().getAttribute("dbName");
 			if(dbName != null)
 				dbName = new String(dbName.getBytes("iso-8859-1"),"gbk");	
 			port = (String)request.getSession().getAttribute("port");
 			if(port != null)
 				port = new String(port.getBytes("iso-8859-1"),"gbk");	
 			
 			
 			
 			db = new DBFactory();
 			if(ip != null && user!=null && pwd!=null)				
 				conn = db.getConnection(ip,user,pwd,dbName,port);
 			else
 				conn = db.getConnection();
 			
 			int j=1;
 			String[] temp = str.split("###");
 			for(String tem:temp){
 				String[] temp1 = tem.split("@@@");
 				
 				cstmt = conn.prepareCall("{call SP_ݼ_SubmitTask (?,?,?)}"); 
 				cstmt.setInt(1, Integer.parseInt(temp1[0]));
 		
 				cstmt.registerOutParameter(2, Types.INTEGER);
 				cstmt.registerOutParameter(3, Types.VARCHAR);
 				
 				cstmt.execute();
 				retRows = cstmt.getInt(2);
 				error = cstmt.getString(3);
 				
 				if(retRows == 0){
 					cstmt.close();	
 					strReason+=temp1[0]+error+"\r\n";
 				}else{
 					cstmt.close();	
 				}	
 				
 				request.getSession().setAttribute("compareProgressSubmit", j++);
 			}	
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			throw e;
 		}
 		finally
 		{
 			db.close();
 		}
 		return strReason;
 	}
 	
 }
