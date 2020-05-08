 package business.dataEdit.lawFL;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.commons.io.FileUtils;
 import org.apache.struts2.ServletActionContext;
 import org.w3c.dom.Node;
 
 
 import admin.Search.SearchInter;
 //import admin.Search.SearchItem;
 import service.SearchItem;
 import admin.user.User;
 //import business.dataEdit.lawCheck.TreeItem;
 import service.TreeItem;
 import business.dbfWork.DbfExportBj;
 
 import client.Service1Stub;
 import client.TestHelloFriend;
 import client.Service1Stub.Ds_checkinfo_type2;
 
 
 import com.googlecode.jsonplugin.annotations.JSON;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 import com.oreilly.servlet.MultipartRequest;
 
 import db.DAOFactory;
 import db.DBFactory;
 
 public class LawFLAction extends ActionSupport implements SearchInter{
 
 	private static final long serialVersionUID = 1L;
 	
 	private int applyNum;
 	//ֵ
 	private boolean success;
 	private String message;
 	private int totalProperty;
 	private List<SearchItem> searchList = new ArrayList<SearchItem>();
 	private List<FLSearchItem> FLsearchList = new ArrayList<FLSearchItem>();
 	List<LawArticleInfo> articles = new ArrayList<LawArticleInfo>();
 	private List<HashMap<String,String>> strResutlItem;
 	private String searchSql ;
 	private String searchTag ;
 	private SearchItem item;
 	private int compareProgress;
 	private int compareTotal;
 	
 	private List<ComboItem> comsearchList = new ArrayList<ComboItem>();
 	private List<ComboItem> comboList = new ArrayList<ComboItem>();
 	private List<TreeItem> treeItems = new ArrayList<TreeItem>();		
 	private String content;
 	private String sn;
 	private String type;
 	private boolean countForAll=false;
 	private HttpServletRequest request;
 	public String getType() {
 		return type;
 	}
 	public void setType(String type) {
 		this.type = type;
 	}
 	public String getContent() {
 		return content;
 	}
 	public void setContent(String content) {
 		this.content = content;
 	}
 	public String getSn() {
 		return sn;
 	}
 	public void setSn(String sn) {
 		this.sn = sn;
 	}
 	public List<TreeItem> getTreeItems() {
 		return treeItems;
 	}
 	public void setTreeItems(List<TreeItem> treeItems) {
 		this.treeItems = treeItems;
 	}
 	public int getApplyNum() {
 		return applyNum;
 	}
 	public void setApplyNum(int applyNum) {
 		this.applyNum = applyNum;
 	}
 
 	//ֵ
 	public boolean isSuccess() {
 		return success;
 	}
 
 	public void setSuccess(boolean success) {
 		this.success = success;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 	@JSON(name="totalProperty")
 	public int getTotalProperty() {
 		return totalProperty;
 	}
 
 	public void setTotalProperty(int totalProperty) {
 		this.totalProperty = totalProperty;
 	}
 
 	@JSON(name="searchList")
 	public List<SearchItem> getSearchList() {
 		return searchList;
 	}
 	public void setSearchList(List<SearchItem> searchList) {
 		this.searchList = searchList;
 	}
 	@JSON(name="searchSql")
 	public String getSearchSql() {
 		return searchSql;
 	}
 	
 	public void setSearchSql(String searchSql) {
 		this.searchSql = searchSql;
 	}
 	@JSON(name="searchTag")
 	public String getSearchTag() {
 		return searchTag;
 	}
 
 	public void setSearchTag(String searchTag) {
 		this.searchTag = searchTag;
 	}
 	
 	@JSON(name="resutlItems")
 	public List<HashMap<String,String>> getStrResutlItem() {
 		return strResutlItem;
 	}
 	public void setStrResutlItem(List<HashMap<String,String>> strResutlItem) {
 		this.strResutlItem = strResutlItem;
 	}
 	
 	public SearchItem getItem() {
 		return item;
 	}
 
 	public void setItem(SearchItem item) {
 		this.item = item;
 	}
 	
 	public int getCompareProgress() {
 		return compareProgress;
 	}
 
 	public void setCompareProgress(int compareProgress) {
 		this.compareProgress = compareProgress;
 	}
 
 	public int getCompareTotal() {
 		return compareTotal;
 	}
 
 	public void setCompareTotal(int compareTotal) {
 		this.compareTotal = compareTotal;
 	}
 	
 
 	public List<FLSearchItem> getFLsearchList() {
 		return FLsearchList;
 	}
 
 	public void setFLsearchList(List<FLSearchItem> lsearchList) {
 		FLsearchList = lsearchList;
 	}
 	
 	public List<LawArticleInfo> getArticles() {
 		return articles;
 	}
 	public void setArticles(List<LawArticleInfo> articles) {
 		this.articles = articles;
 	}
 	
 	
 	@JSON(name="comsearchList")
 	public List<ComboItem> getCombItemList() {
 		return comsearchList;
 	}
 	public void setCombItemList(List<ComboItem> comsearchList) {
 		this.comsearchList = comsearchList;
 	}
 	public List<ComboItem> getComboList() {
 		return comboList;
 	}
 	public void setComboList(List<ComboItem> comboList) {
 		this.comboList = comboList;
 	}
 	//ȡѯ
 	public String Execute() throws Exception {
 	
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		String	sql = request.getParameter("sql");		
 //		   byte b[] = comboboxChoosed.getBytes("ISO-8859-1");
 //
 //		   comboboxChoosed = new String(b);
 
 		String paramLog = null;	
 		try
 		{
 			if(null!=request.getParameter("paramLog")){
 				paramLog = request.getParameter("paramLog");
 				request.getSession().setAttribute("paramLog", paramLog);
 			}else{
 				paramLog = (String)request.getSession().getAttribute("paramLog");
 			}
 		//	paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 			
 		
 //			if(paramLog.contains("WHERE")){
 //				int index = paramLog.indexOf("WHERE");
 //				paramLog = paramLog.substring(0,index);
 //				paramLog = paramLog + " WHERE 1=0";			
 //			}else{
 //				paramLog = paramLog + " WHERE 1=0";	
 //			}				
 			lawFLDAO.executeSql(sql,request);
 		
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	//ȡѯ
 			public String SetValueItem() throws Exception {
 			
 				LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 				HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 				//User user = (User)request.getSession().getAttribute("app_user");
 				String	newValue = request.getParameter("newValue");
 				String	comboboxChoosed = request.getParameter("comboboxChoosed");
 				   byte b[] = comboboxChoosed.getBytes("ISO-8859-1");
 
 				   comboboxChoosed = new String(b);
 		
 				String paramLog = null;	
 				try
 				{
 					if(null!=request.getParameter("paramLog")){
 						paramLog = request.getParameter("paramLog");
 						request.getSession().setAttribute("paramLog", paramLog);
 					}else{
 						paramLog = (String)request.getSession().getAttribute("paramLog");
 					}
 					paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 					
 					paramLog = paramLog.toUpperCase();
 //					if(paramLog.contains("WHERE")){
 //						int index = paramLog.indexOf("WHERE");
 //						paramLog = paramLog.substring(0,index);
 //						paramLog = paramLog + " WHERE 1=0";			
 //					}else{
 //						paramLog = paramLog + " WHERE 1=0";	
 //					}				
 					lawFLDAO.execute("UPDATE dbo.BJ_Ʒñ SET ȼ="+newValue+" WHERE Ʒ='"+comboboxChoosed+"'",request);
 				
 				}
 				catch(Exception e)
 				{
 					e.printStackTrace();
 					setSuccess(false);
 					setMessage("ݿʴ");
 					return "success";	
 				}		
 				setSuccess(true);
 		        return SUCCESS;  
 			}
 	//ȡѯ
 		public String GetSearchItem() throws Exception {
 			LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 			HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 			//User user = (User)request.getSession().getAttribute("app_user");
 			 
 			String paramLog = null;	
 			try
 			{
 				if(null!=request.getParameter("paramLog")){
 					paramLog = request.getParameter("paramLog");
 					request.getSession().setAttribute("paramLog", paramLog);
 				}else{
 					paramLog = (String)request.getSession().getAttribute("paramLog");
 				}
 				paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 				
 				paramLog = paramLog.toUpperCase();
 //				if(paramLog.contains("WHERE")){
 //					int index = paramLog.indexOf("WHERE");
 //					paramLog = paramLog.substring(0,index);
 //					paramLog = paramLog + " WHERE 1=0";			
 //				}else{
 //					paramLog = paramLog + " WHERE 1=0";	
 //				}				
 				
 				setSearchList(lawFLDAO.GetSearchItem(paramLog,request));
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";	
 			}		
 			setSuccess(true);
 	        return SUCCESS;  
 		}
 		public String GetSearchItem2() throws Exception {
 			LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 			HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 			//User user = (User)request.getSession().getAttribute("app_user");
 			 
 			String paramLog = null;	
 			try
 			{
 				if(null!=request.getParameter("paramLog")){
 					paramLog = request.getParameter("paramLog");
 					request.getSession().setAttribute("paramLog", paramLog);
 				}else{
 					paramLog = (String)request.getSession().getAttribute("paramLog");
 				}
 				paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 				
 				paramLog = paramLog.toUpperCase();
 //				if(paramLog.contains("WHERE")){
 //					int index = paramLog.indexOf("WHERE");
 //					paramLog = paramLog.substring(0,index);
 //					paramLog = paramLog + " WHERE 1=0";			
 //				}else{
 //					paramLog = paramLog + " WHERE 1=0";	
 //				}				
 				paramLog=paramLog.replace('','ƪ');
 				setSearchList(lawFLDAO.GetSearchItem2(paramLog,request));
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";	
 			}		
 			setSuccess(true);
 	        return SUCCESS;  
 		}
 		//ȡѯ
 		public String GetSearchComboItem() throws Exception {
 			LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 			HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 			//User user = (User)request.getSession().getAttribute("app_user");
 			 
 			String paramLog = null;	
 			try
 			{
 				if(null!=request.getParameter("paramLog")){
 					paramLog = request.getParameter("paramLog");
 					request.getSession().setAttribute("paramLog", paramLog);
 				}else{
 					paramLog = (String)request.getSession().getAttribute("paramLog");
 				}
 				paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 				
 				paramLog = paramLog.toUpperCase();
 				if(paramLog.contains("WHERE")){
 					int index = paramLog.indexOf("WHERE");
 					paramLog = paramLog.substring(0,index);
 					paramLog = paramLog + " WHERE 1=0";			
 				}else{
 					paramLog = paramLog + " WHERE 1=0";	
 				}				
 				
 				setCombItemList(lawFLDAO.GetSearchComboItem(paramLog,request));
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";	
 			}		
 			setSuccess(true);
 	        return SUCCESS;  
 		}
 	
 	public String GetdbfItem() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		 
 		String paramLog = null;
 		try
 		{
 			if(null!=request.getParameter("paramLog")){
 				paramLog = request.getParameter("paramLog");
 				request.getSession().setAttribute("paramLog", paramLog);
 			}else{
 				paramLog = (String)request.getSession().getAttribute("paramLog");
 			}
 			paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");
 			
 			paramLog = paramLog.replaceAll("!A!", user.getUserName());
 			paramLog = paramLog.replaceAll("!N!", user.getNickName());
 			paramLog = paramLog.replaceAll("!P!", user.getUserType());
 			paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 			paramLog = paramLog.replace("*", "top 1 *");
 			
 			setSearchList(lawFLDAO.GetdbfItem(paramLog,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetFilter() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		String filter = null,col=null,sptask=null,spget=null,spsu=null,spup=null;
 		try
 		{
 			filter = (String)request.getSession().getAttribute("filter");
 			if(filter != null)
 				filter = new String(filter.getBytes("iso-8859-1"),"gbk");
 			
 			col = (String)request.getSession().getAttribute("col");
 			if(col != null)
 				col = new String(col.getBytes("iso-8859-1"),"gbk");
 			
 //			sptask = (String)request.getSession().getAttribute("sptask");
 //			sptask = new String(sptask.getBytes("iso-8859-1"),"gbk");
 //			
 //			spget = (String)request.getSession().getAttribute("spget");
 //			spget = new String(spget.getBytes("iso-8859-1"),"gbk");
 //			
 //			spsu = (String)request.getSession().getAttribute("spsu");
 //			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 //			
 //			spup = (String)request.getSession().getAttribute("spup");
 //			spup = new String(spup.getBytes("iso-8859-1"),"gbk");
 			
 			
 			List<SearchItem> list = new ArrayList<SearchItem>();
 			list.add(new SearchItem(filter,col,sptask,spget,spsu,spup));
 			setSearchList(list);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 
 	public String GetProgress() throws Exception{
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 		
 		String text = request.getParameter("sqlStr");
 		
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			paramLog = paramLog + " " + text;
 		}
 		
 		paramLog = paramLog.replace("*", "count(*)");
         
 		try{
 			this.setCompareProgress((Integer)request.getSession().getAttribute("compareProgressDc"));
 			this.setCompareTotal(lawFLDAO.getCompareTotal(paramLog,request));
 		}catch(Exception e){
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 		setSuccess(true);
 		return SUCCESS;
 	}
 	
 	
 	public String GetGrid() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");		
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 		
 		String articleSn = request.getParameter("articleSn");
 		//String orderBy = request.getParameter("orderBy");
 		
 		if(! "".equals(articleSn) && articleSn != null)
 			paramLog = paramLog + " and ±='"+articleSn+"'";
 		
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		
 		String orderStr = "";
 		if(text != null && !"".equals(text)){
 			if(text.toLowerCase().contains("order by")){
 				String[] str = text.toLowerCase().split("order by");
 				orderStr = " order by "+str[1];
 				
 				paramLog += " and " + str[0];
 			}else
 				paramLog += " and " + text;			
 		}
 		
 		String sort = request.getParameter("sort");
 		String dir = request.getParameter("dir");
 		
 		String[] fids={},dirs={};
 		if(sort != null)
 			if(sort.contains(",") && dir.contains(",")){
 				fids = sort.split(",");
 				dirs = dir.split(",");
 			}
 		if(fids.length > 1)
 			orderStr = " order by "+fids[0]+" "+dirs[0]+" ,"+fids[1]+" "+dirs[1];
 		else
 			if("".equals(orderStr) && !"".equals(sort) && null!=sort)
 				orderStr = " order by "+sort+" "+dir;
 		
 		paramLog = paramLog + orderStr;
 		
 //		if(text != null && !"".equals(text)){
 //			if(! text.trim().startsWith("order"))
 //				text = " and " + text;	
 //			paramLog = paramLog + " " + text;
 //		}
 //		if(! text.contains("order") && orderBy != null)
 //			paramLog = paramLog + orderBy;
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(paramLog));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	public String GetGrid2() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");		
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 		
 		String articleSn = request.getParameter("articleSn");
 		//String orderBy = request.getParameter("orderBy");
 		
 		if(! "".equals(articleSn) && articleSn != null)
 			paramLog = paramLog + " and ±='"+articleSn+"'";
 		
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		
 		String orderStr = "";
 		if(text != null && !"".equals(text)){
 			if(text.toLowerCase().contains("order by")){
 				String[] str = text.toLowerCase().split("order by");
 				orderStr = " order by "+str[1];
 				
 				paramLog += " and " + str[0];
 			}else
 				paramLog += " and " + text;			
 		}
 		
 		String sort = request.getParameter("sort");
 		String dir = request.getParameter("dir");
 		
 		String[] fids={},dirs={};
 		if(sort != null)
 			if(sort.contains(",") && dir.contains(",")){
 				fids = sort.split(",");
 				dirs = dir.split(",");
 			}
 		if(fids.length > 1)
 			orderStr = " order by "+fids[0]+" "+dirs[0]+" ,"+fids[1]+" "+dirs[1];
 		else
 			if("".equals(orderStr) && !"".equals(sort) && null!=sort)
 				orderStr = " order by "+sort+" "+dir;
 		
 		paramLog = paramLog + orderStr;
 		
 //		if(text != null && !"".equals(text)){
 //			if(! text.trim().startsWith("order"))
 //				text = " and " + text;	
 //			paramLog = paramLog + " " + text;
 //		}
 //		if(! text.contains("order") && orderBy != null)
 //			paramLog = paramLog + orderBy;
 		String ids =null;
 		String  start = request.getParameter("start");
 		if(null!=request.getParameter("ids")){
 			ids = request.getParameter("ids");
 			request.getSession().setAttribute("ids", ids);
 		}else{
 			ids = (String)request.getSession().getAttribute("ids");
 		}
 		if(ids!=null){
 			paramLog=paramLog+" where  = '"+ids+"'";
 		}
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 			{ 
 				paramLog=paramLog.replace('','ƪ');
 				setStrResutlItem(Select(paramLog));
 			}
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetTotalGrid() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");		
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 		
 		String num = request.getParameter("num");
 		//String text = request.getParameter("sqlStr");
 		String text = request.getParameter("sqlStr");
 		String  start = request.getParameter("start");
 		String limit = request.getParameter("limit");
 		
 		int limitFinall = Integer.parseInt(limit);
 		int startFinall = Integer.parseInt(start);
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		String countAll = paramLog.trim().replace("*"," count(*) ");
 		String s1 = paramLog;
 			s1=s1.trim().replace("*","top "+limitFinall+ " * ");
 		String s2 = paramLog;
 			s2=s2.trim().replace("*","top "+startFinall+ "  ");
 		s1+= " where ( not in ( ";
 		s2+= " order by )) order by ";
 		s1+=s2;
 		s1 = s1.replace("WHERE 1=1", " ");	
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(s1));
 				setTotalProperty(SelectAll(countAll));
 			// this.setTotalProperty(1000);
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	public String GetTotalGrid2() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");		
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 		
 		String num = request.getParameter("num");
 		//String text = request.getParameter("sqlStr");
 		String text = request.getParameter("sqlStr");
 		String  start = request.getParameter("start");
 		String limit = request.getParameter("limit");
 		
 		int limitFinall = Integer.parseInt(limit);
 		int startFinall = Integer.parseInt(start);
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		String countAll = paramLog.trim().replace("*"," count(*) ");
 		String s1 = paramLog;
 			s1=s1.trim().replace("*","top "+limitFinall+ " * ");
 		String s2 = paramLog;
 			s2=s2.trim().replace("*","top "+startFinall+ " ± ");
 		s1+= " where (± not in ( ";
 		s2+= " order by ±)) order by ±";
 		s1+=s2;
 		s1 = s1.replace("WHERE 1=1", " ");
 				
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(s1));
 				setTotalProperty(SelectAll(countAll));
 			// this.setTotalProperty(1000);
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	private int SelectAll(String countAll) {
 request = (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
 		List<HashMap<String,String>> jsonList=new ArrayList<HashMap<String,String>>();
 		
 		sql =countAll;
 		//ɶȡ¼﷨
 //		int start = paramLog.indexOf("FROM");
 //		int end =  paramLog.length();
 //		sqlCount = "select count(*) "+paramLog.substring(paramLog.indexOf("from"),paramLog.length());
 		int count =0;
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
 
 
 			pstmt = conn.prepareStatement(sql);
 		
 			rs = pstmt.executeQuery() ;
 			
 			ResultSetMetaData rsData=rs.getMetaData(); 
 				rs.next();
 			count =Integer.parseInt(rs.getString(1));
 			//totalProperty = 1000;
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
       
         	return count;
 	}
 	public List<HashMap<String,String>> Select(String paramLog) throws Exception
     {
 		request = (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
         DBFactory db 			= null;
 	    Connection conn 		= null;
 		PreparedStatement pstmt	= null;
 		ResultSet rs			= null;
 		String sql				= null;
 		
 		List<HashMap<String,String>> jsonList=new ArrayList<HashMap<String,String>>();
 	//-----------------------------------------------------------------------------------	
 		String[] sqlq =paramLog.split("and");
 		sql=sqlq[0];
 	//-----------------------------------------------------------------------------------
 		//ɶȡ¼﷨
 //		int start = paramLog.indexOf("FROM");
 //		int end =  paramLog.length();
 //		sqlCount = "select count(*) "+paramLog.substring(paramLog.indexOf("from"),paramLog.length());
 		
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
 
 
 			pstmt = conn.prepareStatement(sql);
 			
 			rs = pstmt.executeQuery() ;
 			
 			ResultSetMetaData rsData=rs.getMetaData(); 
 			int count =0;
 			while(rs.next())
 			{
 				HashMap<String,String> itemMap=new HashMap<String,String> ();
 				for(int i=1; i<=rsData.getColumnCount(); ++i)
 				{
 					if("int".equals(rsData.getColumnTypeName(i)))
 					{
 						itemMap.put(rsData.getColumnName(i), String.valueOf(rs.getInt(i)));
 //						if("±".equals(rsData.getColumnName(i)))
 //							itemMap.put("downURL", "LawFLAction!DownFileEdit.action?&sn="+String.valueOf(rs.getInt(i)));
 					}
 					else
 					{
 						itemMap.put(rsData.getColumnName(i), rs.getString(i));
 //						if("±".equals(rsData.getColumnName(i)))
 //							itemMap.put("downURL", "LawFLAction!DownFileEdit.action?&sn="+rs.getString(i));
 					}
 					count++;
 				}				
 				
 				jsonList.add(itemMap);
 			}
 			//totalProperty = 1000;
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
 			db.close();
 		}
       
         	return jsonList;
         
     }
 	
 	public String GetLawFLClick() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("spup");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		
 		String newValue = request.getParameter("editParam");
 		String taskId = request.getParameter("taskNum");
 		String colName = request.getParameter("colName");
 		 
 		try
 		{	if(request.getSession().getAttribute("app_user") == null)
 				setMessage("ҳ泤ʱδ,µ¼!");
 			else
 				setMessage(lawFLDAO.GetLawFLClick(Integer.parseInt(taskId),newValue,colName,name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetSgSave() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("spup");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		
 		String newValue = request.getParameter("editParam");
 		String sn = request.getParameter("sn");
 		String colName = request.getParameter("colName");
 		 
 		try
 		{								
 			setMessage(lawFLDAO.GetSgSave(sn,newValue,colName,name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetApplyList() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("sptask");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		 
 		try
 		{								
 			setFLsearchList(lawFLDAO.GetApplyList(user.getUserName(),name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String setApplyTask() throws Exception {			
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 	
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("spget");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		
 		request.getSession().setAttribute("compareTotalApply", this.getApplyNum());
 		request.getSession().setAttribute("compareProgressApply", 0);
 		 
 		try
 		{								
 			setMessage(lawFLDAO.setApplyTask(user.getUserName(),this.getApplyNum(),name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String setErrorApplyTask() throws Exception {			
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 	
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("spget");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		 
 		try
 		{								
 			setMessage(lawFLDAO.setErrorApplyTask(user.getUserName(),this.getApplyNum(),name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetLawArticleInfoGrid() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String articleId = request.getParameter("articleId");
 		String type = request.getParameter("type");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetLawArticleInfoGrid(articleId,type,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetInfoHtml() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String fntree = (String)request.getSession().getAttribute("fntree");
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		
 		String articleId = request.getParameter("articleId");
 		String type = request.getParameter("type");
 
 		try
 		{								
 			setMessage(lawFLDAO.GetInfoHtml(articleId,type,fntree,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetSgArticleInfoGrid() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("navsp");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		String articleId = request.getParameter("articleId");
 		String type = request.getParameter("type");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetSgArticleInfoGrid(articleId,name,type,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetLocationItem() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String nav = (String)request.getSession().getAttribute("nav");	
 			nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 			
 			setSearchList(lawFLDAO.GetLocationItem(nav,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetLocationGrid() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String navsp = (String)request.getSession().getAttribute("navsp");
 		navsp = new String(navsp.getBytes("iso-8859-1"),"gbk");
 		
 		String text = request.getParameter("text");
 		String name = request.getParameter("name");
 		String type = request.getParameter("type");
 		
 		setStrResutlItem(lawFLDAO.GetLocationGrid(navsp,text,name,type,request));
 		
 //		String nav = (String)request.getSession().getAttribute("nav");
 //		nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 //		
 //		String sql = "select * from "+nav+" where 1=1";
 //		
 //		
 //		String text = request.getParameter("sqlStr");
 //		if(null!=text)
 //			request.getSession().setAttribute("queryStr", text);
 //		else
 //			text = (String)request.getSession().getAttribute("queryStr");
 //		
 //		if(text != null && !"".equals(text)){
 //			if(! text.trim().startsWith("order"))
 //				text = " and " + text;	
 //			sql = sql + " " + text;
 //		}
 //		
 //		//System.out.println(sql);
 //
 //        try {
 //			String param = (String)request.getParameter("Param");
 //			if("data".equals(param))
 //				setStrResutlItem(Select(sql));
 //		} catch (Exception e) {
 //			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 //				setSuccess(false);
 //				setMessage("༭Ĳѯ䲻SQL﷨");
 //				return "success";
 //			}else{
 //				e.printStackTrace();
 //				setSuccess(false);
 //				setMessage("ݿʴ");
 //				return "success";
 //			}
 //		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetLocationName() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String nav = (String)request.getSession().getAttribute("nav");	
 			nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 			
 			setSearchList(lawFLDAO.GetLocationName(nav,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetErrorType() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String nav = (String)request.getSession().getAttribute("nav");	
 			nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 			String errorPost = request.getParameter("errorPost");
 			
 			setComboList(lawFLDAO.GetErrorType(nav,errorPost,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String submitLawFL() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String spsu = (String)request.getSession().getAttribute("spsu");	
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 			
 			setMessage(lawFLDAO.submitLawFL(spsu,str,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	public String submitLawFL2() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String spsu = (String)request.getSession().getAttribute("spsu");	
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 				
 			setMessage(lawFLDAO.submitLawFL2(spsu,str,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String submitSgFaith() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String spsu = (String)request.getSession().getAttribute("spsu");	
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 			
 			setMessage(lawFLDAO.submitSgFaith(spsu,str,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String backSgFaith() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String spget = (String)request.getSession().getAttribute("spget");	
 			spget = new String(spget.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 			
 			setMessage(lawFLDAO.submitSgFaith(spget,str,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetSubmitProgress() throws Exception{
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
         
 		this.setCompareProgress((Integer)request.getSession().getAttribute("compareProgressSubmit"));
 		setSuccess(true);
 		return SUCCESS;
 	}
 	
 	public String deleteFunc() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String filter = (String)request.getSession().getAttribute("filter");
 		if(filter != null)
 			filter = new String(filter.getBytes("iso-8859-1"),"gbk");
 		
 		String ids = request.getParameter("ids");
 		String states = request.getParameter("states");
 
 		try
 		{								
 			setMessage(lawFLDAO.deleteFunc(ids,states,filter,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetApplyProgress() throws Exception{
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
         
 		this.setCompareProgress((Integer)request.getSession().getAttribute("compareProgressApply"));
 		this.setCompareTotal((Integer)request.getSession().getAttribute("compareTotalApply"));
 		//System.out.println(request.getSession().getAttribute("compareProgressApply"));
 		//System.out.println(request.getSession().getAttribute("compareTotalApply"));
 		setSuccess(true);
 		return SUCCESS;
 	}
 	
 //	public String GetDirectory() throws Exception {	
 //		
 //		String message = "";
 //
 //		try
 //		{	
 //			JFrame f = new Test(); 
 //			
 //		    JFileChooser fc = new JFileChooser(); 
 //		    fc.setFileSelectionMode(JFileChooser.FILES_ONLY); 
 //		    int intRetVal = fc.showOpenDialog(f); 
 //		    if( intRetVal == JFileChooser.APPROVE_OPTION){ 
 //		    	
 //		    	String fileName = fc.getSelectedFile().getName();
 //		    	if(!fileName.contains(".dbf")){
 //		    		setMessage("ļչdbf");
 //		    		setSuccess(false);
 //		    		return "success";
 //		    	}
 //		    	
 //		       message = fc.getSelectedFile().getPath(); 
 //		    }
 //			
 //			setMessage(message);
 //		}
 //		catch(Exception e)
 //		{
 //			e.printStackTrace();
 //			setSuccess(false);
 //			setMessage("ݿʴ");
 //			return "success";	
 //		}		
 //		setSuccess(true);
 //	    return SUCCESS;  
 //	}
 	
 	public String downLoadDbf() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		//String path = request.getParameter("path");
 
 		try{
 			String piciId = request.getParameter("piciId");
 			String downPici = request.getParameter("downPici");
 			//String flow = request.getParameter("flow");
 			//String type = request.getParameter("type");
 
 			DbfExportBj.exportByPiciId("clkl_day", piciId, downPici);	
 			
     	}catch(Exception e){
     		setSuccess(false);
 			setMessage("ļʧ");
     		e.printStackTrace();
     		return SUCCESS;
     	}      	
 
     	setSuccess(true);
 		return SUCCESS;
 	}
 	
 	
 	
 	
 	
 	public String GetSearchItemWait() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		 
 		String spget = null;
 		try
 		{
 
 			spget = (String)request.getSession().getAttribute("spget");
 			spget = new String(spget.getBytes("iso-8859-1"),"gbk");	
 			
 			spget = spget.toUpperCase();
 			if(spget.contains("WHERE")){
 				int index = spget.indexOf("WHERE");
 				spget = spget.substring(0,index);
 				spget = spget + " WHERE 1=0";			
 			}else{
 				spget = spget + " WHERE 1=0";	
 			}
 			
 			setSearchList(lawFLDAO.GetSearchItem(spget,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetGridWait() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);	
 		
 		String spget = null;
 		
 		String num = request.getParameter("num");
 		
 		spget = (String)request.getSession().getAttribute("spget");
 		spget = new String(spget.getBytes("iso-8859-1"),"gbk");		
 		
 		if(num != null && !"".equals(num))
 			spget = spget.replace("*", "top "+Integer.parseInt(num)+" *");
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			spget = spget + " " + text;
 		}
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(spget));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetSearchItemOn() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		 
 		String spsu = null;
 		try
 		{
 
 			spsu = (String)request.getSession().getAttribute("spsu");
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");	
 			
 			spsu = spsu.toUpperCase();
 			if(spsu.contains("WHERE")){
 				int index = spsu.indexOf("WHERE");
 				spsu = spsu.substring(0,index);
 				spsu = spsu + " WHERE 1=0";			
 			}else{
 				spsu = spsu + " WHERE 1=0";	
 			}
 			
 			setSearchList(lawFLDAO.GetSearchItem(spsu,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetGridOn() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);	
 		
 		String spsu = null;
 		
 		String num = request.getParameter("num");
 		spsu = (String)request.getSession().getAttribute("spsu");
 		spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");		
 		
 		if(num != null && !"".equals(num))
 			spsu = spsu.replace("*", "top "+Integer.parseInt(num)+" *");
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			spsu = spsu + " " + text;
 		}
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(spsu));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetSearchItemHis() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		 
 		String spup = null;
 		try
 		{
 
 			spup = (String)request.getSession().getAttribute("spup");
 			spup = new String(spup.getBytes("iso-8859-1"),"gbk");	
 			
 			spup = spup.toUpperCase();
 			if(spup.contains("WHERE")){
 				int index = spup.indexOf("WHERE");
 				spup = spup.substring(0,index);
 				spup = spup + " WHERE 1=0";			
 			}else{
 				spup = spup + " WHERE 1=0";	
 			}
 			
 			setSearchList(lawFLDAO.GetSearchItem(spup,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetGridHis() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);	
 		
 		String spup = null;
 		
 		String num = request.getParameter("num");
 		
 		spup = (String)request.getSession().getAttribute("spup");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");	
 		
 		if(num != null && !"".equals(num))
 			spup = spup.replace("*", "top "+Integer.parseInt(num)+" *");
 		
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			spup = spup + " " + text;
 		}
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(spup));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	
 	public String GetFLNotFinishGrid() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);	
 		
 		String spup = null;
 		
 		String num = request.getParameter("num");		
 		String type = request.getParameter("type");
 		String sPici = request.getParameter("sPici");
 		
 		spup = (String)request.getSession().getAttribute("spup");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");			
 		
 		if(num !="" && num != null)
 			spup = spup.replace("*", "top "+Integer.parseInt(num)+" *");
 		
 		if("ʱ".equals(type) || "ʱ".equals(type))
 			spup = spup + " and Դո='"+sPici+"'";
 		else
 			spup = spup + " and Դ¸='"+sPici+"'";
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			spup = spup + " " + text;
 		}
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(spup));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	
 	
 	
 	
 	public String GCapply() throws Exception {			
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 	
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("spget");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		
 		//request.getSession().setAttribute("compareTotalApply", this.getApplyNum());
 		 
 		try
 		{								
 			setMessage(lawFLDAO.GCapply(user.getUserName(),name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetLawGCInfoGrid() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String sptask = (String)request.getSession().getAttribute("sptask");
 		sptask = new String(sptask.getBytes("iso-8859-1"),"gbk");		
 		String fntree = (String)request.getSession().getAttribute("fntree");
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		
 		String articleId = request.getParameter("articleId");
 		String type = request.getParameter("type");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetLawGCInfoGrid(articleId,type,sptask,fntree,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	
 	
 	public String GetdbfItemMonitor() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String spup = null;
 		
 		try
 		{
 			spup = (String)request.getSession().getAttribute("spup");
 			spup = new String(spup.getBytes("iso-8859-1"),"gbk");	
 			
 			spup = spup.replace("*", "top "+3+" *");
 			
 			setSearchList(lawFLDAO.GetdbfItem(spup,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetMonitorProgress() throws Exception{
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String spup = null;
 		spup = (String)request.getSession().getAttribute("spup");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");
 		
 		String text = request.getParameter("sqlStr");
 		
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			spup = spup + " " + text;
 		}
 		
 		spup = spup.replace("*", "count(*)");
         
 		try{
 			this.setCompareProgress((Integer)request.getSession().getAttribute("compareProgressMonitorDc"));
 			this.setCompareTotal(lawFLDAO.getCompareTotal(spup,request));
 		}catch(Exception e){
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 		setSuccess(true);
 		return SUCCESS;
 	}
 	
 	
 	public String GetLine() throws Exception {			
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 	
 		//HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		 
 		try
 		{								
 			setMessage(lawFLDAO.GetLine());
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetLine1() throws Exception {			
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();		
 		 
 		try
 		{		
 	    	double time=0;
             DecimalFormat df = new DecimalFormat("###.00");
 			
 			 //String[] addrs= {"202.108.33.87"};
 		     String[] addrs= {"219.234.145.211"};
 		     if (addrs.length < 1){ 
 		         System.out.println("syntax Error!"); 
 		     }else{ 
 		         for(int i=0;i<addrs.length;i++){
 			         String line = null; 
 
 		             Process pro = Runtime.getRuntime().exec("ping " + addrs[i]+" -l 1024 -n 10"); 
 		             BufferedReader buf = new BufferedReader(new InputStreamReader(pro.getInputStream())); 
 		             while((line = buf.readLine()) != null){	              
 		            	int position=0;
 		              	if((position=line.indexOf("ƽ"))>=0){  
 			            	  System.out.println(line); 
 				              String value=line.substring(position+4,line.lastIndexOf("ms")).trim();
 				              time = Double.valueOf(value);
 				              time = Double.parseDouble(df.format(time));
 				              //System.out.println(position);
 				              //System.out.println(time);
 				              //System.out.println("your speed is:"+Double.parseDouble(df.format(1000/time))+"KB/S");               
 		              	}
 		             }         
 		         } 
 		     } 
 			
 		//	setMessage(Double.parseDouble(df.format(1000/time))+"KB/S");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetTotalNum() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String spup = null;
 		spup = (String)request.getSession().getAttribute("spup");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");
 		
 		
 		spup = spup.replaceAll("!A!", user.getUserName());
 		spup = spup.replaceAll("!N!", user.getNickName());
 		spup = spup.replaceAll("!P!", user.getUserType());
 		spup = spup.replaceAll("!D!", user.getDepartmentId());
 		
 		try
 		{
 			String text = request.getParameter("sqlStr");
 			
 			if(null!=text)
 				request.getSession().setAttribute("queryStr", text);
 			else
 				text = (String)request.getSession().getAttribute("queryStr");
 			
 			if(text != null && !"".equals(text)){
 				if(! text.trim().startsWith("order"))
 					text = " and " + text;	
 				spup = spup + " " + text;
 			}
 			
 			spup = spup.replace("*", "count(*)");
 			
 			
 			setSearchList(lawFLDAO.GetTotalNum(spup,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetOnTotalNum() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String spsu = null;
 		spsu = (String)request.getSession().getAttribute("spsu");
 		spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 		
 		
 		spsu = spsu.replaceAll("!A!", user.getUserName());
 		spsu = spsu.replaceAll("!N!", user.getNickName());
 		spsu = spsu.replaceAll("!P!", user.getUserType());
 		spsu = spsu.replaceAll("!D!", user.getDepartmentId());
 		
 		try
 		{
 			String text = request.getParameter("sqlStr");
 			
 			if(null!=text)
 				request.getSession().setAttribute("queryStr", text);
 			else
 				text = (String)request.getSession().getAttribute("queryStr");
 			
 			if(text != null && !"".equals(text)){
 				if(! text.trim().startsWith("order"))
 					text = " and " + text;	
 				spsu = spsu + " " + text;
 			}
 			
 			spsu = spsu.replace("*", "count(*)");
 			
 			
 			setSearchList(lawFLDAO.GetTotalNum(spsu,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetWaitTotalNum() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String spget = null;
 		spget = (String)request.getSession().getAttribute("spget");
 		spget = new String(spget.getBytes("iso-8859-1"),"gbk");
 		
 		
 		spget = spget.replaceAll("!A!", user.getUserName());
 		spget = spget.replaceAll("!N!", user.getNickName());
 		spget = spget.replaceAll("!P!", user.getUserType());
 		spget = spget.replaceAll("!D!", user.getDepartmentId());
 		
 		try
 		{
 			String text = request.getParameter("sqlStr");
 			
 			if(null!=text)
 				request.getSession().setAttribute("queryStr", text);
 			else
 				text = (String)request.getSession().getAttribute("queryStr");
 			
 			if(text != null && !"".equals(text)){
 				if(! text.trim().startsWith("order"))
 					text = " and " + text;	
 				spget = spget + " " + text;
 			}
 			
 			spget = spget.replace("*", "count(*)");
 			
 			
 			setSearchList(lawFLDAO.GetTotalNum(spget,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetAllTotalNum() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 
 		String spup = null;
 		spup = (String)request.getSession().getAttribute("paramLog");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");
 		
 		spup = spup.replaceAll("!A!", user.getUserName());
 		spup = spup.replaceAll("!N!", user.getNickName());
 		spup = spup.replaceAll("!P!", user.getUserType());
 		spup = spup.replaceAll("!D!", user.getDepartmentId());
 		
 		try
 		{
 			String text = request.getParameter("sqlStr");
 			
 			if(null!=text)
 				request.getSession().setAttribute("queryStr", text);
 			else
 				text = (String)request.getSession().getAttribute("queryStr");
 			
 			if(text != null && !"".equals(text)){
 				if(! text.trim().startsWith("order"))
 					text = " and " + text;	
 				spup = spup + " " + text;
 			}
 			
 			spup = spup.replace("*", "count(*)");
 			
 			
 			setSearchList(lawFLDAO.GetTotalNum(spup,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	public String GetAllTotalNum2() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 
 		String spup = null;
 		spup = (String)request.getSession().getAttribute("paramLog");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");
 		
 		spup = spup.replaceAll("!A!", user.getUserName());
 		spup = spup.replaceAll("!N!", user.getNickName());
 		spup = spup.replaceAll("!P!", user.getUserType());
 		spup = spup.replaceAll("!D!", user.getDepartmentId());
 		
 		try
 		{
 			String text = request.getParameter("sqlStr");
 			
 //			if(null!=text)
 //				request.getSession().setAttribute("queryStr", text);
 //			else
 //				text = (String)request.getSession().getAttribute("queryStr");
 //			
 //			if(text != null && !"".equals(text)){
 //				if(! text.trim().startsWith("order"))
 //					text = " and " + text;	
 //				spup = spup + " " + text;
 //			}
 			
 			spup = spup.replace("*", "count(*)");
 			
 			
 			setSearchList(lawFLDAO.GetTotalNum(spup,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	public String GetFLNotFinishTotalNum() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 
 		String spup = null;
 		spup = (String)request.getSession().getAttribute("spup");
 		spup = new String(spup.getBytes("iso-8859-1"),"gbk");
 		
 		try
 		{
 			String type = request.getParameter("type");
 			String sPici = request.getParameter("sPici");
 			if("ʱ".equals(type) || "ʱ".equals(type))
 				spup = spup+" and Դո='"+sPici+"'";
 			else
 				spup = spup+" and Դ¸='"+sPici+"'";			
 			
 			
 			String text = request.getParameter("sqlStr");
 			
 			if(null!=text)
 				request.getSession().setAttribute("queryStr", text);
 			else
 				text = (String)request.getSession().getAttribute("queryStr");
 			
 			if(text != null && !"".equals(text)){
 				if(! text.trim().startsWith("order"))
 					text = " and " + text;	
 				spup = spup + " " + text;
 			}
 			
 			spup = spup.replace("*", "count(*)");
 			
 			
 			setSearchList(lawFLDAO.GetTotalNum(spup,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public String GetMEDLocationName() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String type = request.getParameter("type");
 			String nav = (String)request.getSession().getAttribute("nav");	
 			if("ڿ".equals(type))
 				nav = nav.split("@@")[0];
 			else if("".equals(type))
 				nav = nav.split("@@")[1];
 			else if("ѧλ".equals(type))
 				nav = nav.split("@@")[2];
 			nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 			
 			setSearchList(lawFLDAO.GetLocationName(nav,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetMEDLocationItem() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{
 			String type = request.getParameter("type");
 			String nav = (String)request.getSession().getAttribute("nav");	
 			if("ڿ".equals(type))
 				nav = nav.split("@@")[0];
 			else if("".equals(type))
 				nav = nav.split("@@")[1];
 			else if("ѧλ".equals(type))
 				nav = nav.split("@@")[2];
 			nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 			
 			setSearchList(lawFLDAO.GetLocationItem(nav,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetMEDLocationGrid() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String type = request.getParameter("type");
 		String nav = (String)request.getSession().getAttribute("nav");
 		if("ڿ".equals(type))
 			nav = nav.split("@@")[0];
 		else if("".equals(type))
 			nav = nav.split("@@")[1];
 		else if("ѧλ".equals(type))
 			nav = nav.split("@@")[2];
 		nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 		
 		String sql = "select * from "+nav+" where 1=1";
 		
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			sql = sql + " " + text;
 		}
 		
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(sql));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetSearchItemInfo() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		String paramLog = null;
 		try
 		{
 			String sn = request.getParameter("sn");		
 			String pubSn = request.getParameter("pubSn");
 			//paramLog = "select * from V_MED__  WHERE 1=1 and ='"+sn+"'";
 			paramLog = "select * from ҵ༭.[dbo].Fu_MED_GetInfo_('"+sn+"','"+pubSn+"') WHERE 1=1";
 			
 			paramLog = paramLog.toUpperCase();
 			if(paramLog.contains("WHERE")){
 				int index = paramLog.indexOf("WHERE");
 				paramLog = paramLog.substring(0,index);
 				paramLog = paramLog + " WHERE 1=0";			
 			}else{
 				paramLog = paramLog + " WHERE 1=0";	
 			}
 			
 			setSearchList(lawFLDAO.GetSearchItem(paramLog,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetGridInfo() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String paramLog = null;
 		
 		String sn = request.getParameter("sn");	
 		String pubSn = request.getParameter("pubSn");
 		//paramLog = "select * from V_MED__ WHERE 1=1 and ='"+sn+"'";		
 		paramLog = "select * from ҵ༭.[dbo].Fu_MED_GetInfo_('"+sn+"','"+pubSn+"')";
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			paramLog = paramLog + " " + text;
 		}
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param))
 				setStrResutlItem(Select(paramLog));
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetmedGCInfoGrid() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		
 		String articleId = request.getParameter("articleId");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetmedGCInfoGrid(articleId,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetFilterHtmlPage() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		StringBuffer url = request.getRequestURL();
 		String requestUrl = url.toString();
 		
 		String sn = request.getParameter("sn");
 
 		try
 		{		
 			setMessage(lawFLDAO.GetFileInfo(requestUrl,sn,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String DownFileEdit() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		HttpServletResponse response=ServletActionContext.getResponse();
 
 		String sn = request.getParameter("sn");
 		String filePath = "";
 
 		try
 		{		
 			filePath = lawFLDAO.GetDownFileName(sn,request);
 			if("".equals(filePath)){
 				setMessage("ļѾ");
 				return SUCCESS;
 			}else{				
 				int pos = filePath.indexOf("create_file");
 				String name = filePath.substring(pos+12, filePath.length());
 				
 				response.setContentType("application/x-msdownload");
 		        response.setHeader("Content-Disposition",
 		                          "attachment;" + " filename="+name);
 		        downFile(response.getOutputStream(),filePath);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String isExist() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 
 		String sn = request.getParameter("sn");
 		String filePath = "";
 
 		try
 		{		
 			filePath = lawFLDAO.isExist(sn,request);
 			if("".equals(filePath)){
 				setMessage("ļѾ");
 				return SUCCESS;
 			}else
 				setMessage("");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	private void downFile(OutputStream out,String filePath) throws IOException {
 		FileInputStream in = new FileInputStream(filePath);
 		byte[] buf = new byte[10240]; 
         int len = 0; 
         while ((len = in.read(buf, 0, 10240)) != -1) {
 			out.write(buf, 0, len);
 		}
 
 		in.close();
         out.flush();
 		out.close();
 	}
 	
 	
 	
 	
 	
 	
 	
 	public String GetJunCheckTree() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		
 		String fntree = (String)request.getSession().getAttribute("fntree");	
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		
 		client.ServiceSgStub stub = null;
 
 		try
 		{								
 			//setTreeItems(lawFLDAO.GetJunCheckTree(user.getUserName(),fntree,request));
 			
 //			TestHelloFriend hf = new TestHelloFriend();
 //			List<TreeItem> items = hf.GetJunCheckTreeClient(user.getUserName(),fntree,request,"GetJunCheckTree");
 //			setTreeItems(items);
 			
 //			TestHelloFriend hf = new TestHelloFriend();
 //			List<TreeItem> items = hf.GetDataset(user.getUserName(),fntree);
 //			setTreeItems(items);
 			
 //			Service1Stub stub = new Service1Stub();
 //			Service1Stub.ExecuteSql sh= new Service1Stub.ExecuteSql();
 //			
 //			sh.setServerInfo("192.168.60.244@sa@newspaper@@");
 //			sh.setSSql("select * from "+ fntree +"("+user.getUserName()+") order by ʱ desc");
 //			Object obj = stub.executeSql(sh);
 //			System.out.println((String)obj);
 			
 //			Service1Stub stub = new Service1Stub();
 //			Service1Stub.AddNumbers sh= new Service1Stub.AddNumbers();
 //			
 //			sh.setNumberOne(1);
 //			sh.setNumberTwo(1);
 //			AddNumbersResponse res = stub.addNumbers(sh);
 //			int num = res.getAddNumbersResult();
 //			System.out.println(num);
 			
 //			Fantasy.WebServiceDemo.Service1Locator sl = new Fantasy.WebServiceDemo.Service1Locator();
 //			Fantasy.WebServiceDemo.Service1Soap _stub = sl.getService1Soap12();
 //			System.out.println(_stub.addNumbers(1, 1));
 			
 //			Fantasy.WebServiceDemo.Service1Locator sl = new Fantasy.WebServiceDemo.Service1Locator();
 //			Fantasy.WebServiceDemo.Service1Soap _stub = sl.getService1Soap12();
 //			String str = _stub.executeSql("192.168.60.244@sa@newspaper@@","select * from "+ fntree +"('"+user.getUserName()+"') order by ʱ desc");
 //			
 //			str = str.substring(2);
 			
 //			str = "<?xml version=\"1.0\" encoding=\"GB2312\"?>"+str;
 //			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 //			javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 //			java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(str.getBytes()));
 //			//java.io.InputStream in = new java.io.FileInputStream("C:\\Users\\whc\\Desktop\\aa.xml");
 //			org.w3c.dom.Document doc = builder.parse(in);
 //			org.w3c.dom.Element root = doc.getDocumentElement();
 //			org.w3c.dom.NodeList nodeList = root.getElementsByTagName("a");
 //			List<TreeItem> items = new ArrayList<TreeItem>();
 //			for(int i=0;i<nodeList.getLength();i++){
 //				org.w3c.dom.Node node = nodeList.item(i);
 //				org.w3c.dom.NodeList conList = node.getChildNodes();
 //				TreeItem item = new TreeItem();
 //				for(int j=0;j<conList.getLength();j++){
 //					org.w3c.dom.Node node1 = conList.item(j);
 //					if(node1.getNodeType() == Node.ELEMENT_NODE){
 //						//org.w3c.dom.Element element = (org.w3c.dom.Element)node1;
 //						if("".equals(node1.getNodeName()))
 //							item.setId(node1.getTextContent());
 //						else if("±".equals(node1.getNodeName()))
 //							item.setName(node1.getTextContent());
 //					}					
 //				}
 //				item.setParentId("0");
 //				item.setChecked(false);
 //				item.setLeaf(true);
 //				items.add(item);				
 //			}
 //			setTreeItems(items);
 			
 			
 			
 			stub = new client.ServiceSgStub();
 			System.out.println("GetCheckTree---ʼ");
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetCheckTree tree = new client.ServiceSgStub.GetCheckTree();			
 			tree.setStrTree(fntree);
 			tree.setStrName(user.getUserName());
 			
 			client.ServiceSgStub.GetCheckTreeResponse res = stub.getCheckTree(tree);
 			int m = res.getGetCheckTreeResult();
 			
 			String xml = res.getStrXml();
 			String str = res.getStrMsg();
 			String xmlNew = "";
 			
 			
 			
 //			client.ServiceSgStub.GetStepCompare compare = new client.ServiceSgStub.GetStepCompare();
 //			compare.setStrFilename(param)
 			
 			
 			
 			
 			
 			if(xml.contains("<ʱ>") && xml.contains("</ʱ>")){
 				int start = xml.indexOf("<ʱ>");
 				int end = xml.indexOf("</ʱ>");
 				xmlNew = xml.substring(0, start)+xml.substring(end+7,xml.length());
 			}
 			
 			
 			if(m==0){
 				setSuccess(false);
 				setMessage(str);
 				if(stub!=null)
 					stub.cleanup();
 				System.out.println("GetCheckTree---0");
 				return "success";
 			}else{
 				//xmlNew = "<?xml version=\"1.0\" encoding=\"GB2312\"?>"+xmlNew;
 				javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 				javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 				java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(xmlNew.getBytes()));
 				org.w3c.dom.Document doc = builder.parse(in);
 				org.w3c.dom.Element root = doc.getDocumentElement();
 				org.w3c.dom.NodeList nodeList = root.getElementsByTagName("REC");
 				List<TreeItem> items = new ArrayList<TreeItem>();		
 				
 				HashMap<String,TreeItem> hash = new HashMap<String,TreeItem>();
 				
 				for(int i=0;i<nodeList.getLength();i++){
 					org.w3c.dom.Node node = nodeList.item(i);
 					org.w3c.dom.NodeList conList = node.getChildNodes();
 					TreeItem item = new TreeItem();
 					for(int j=0;j<conList.getLength();j++){
 						org.w3c.dom.Node node1 = conList.item(j);
 						if(node1.getNodeType() == Node.ELEMENT_NODE){
 							//org.w3c.dom.Element element = (org.w3c.dom.Element)node1;
 							if("".equals(node1.getNodeName()))
 								item.setId(node1.getTextContent());
 							else if("±".equals(node1.getNodeName())){
 								item.setName(node1.getTextContent());
 								item.setText(node1.getTextContent());						
 							}else if("ű".equals(node1.getNodeName()))
 								item.setLevel(node1.getTextContent());
 							else if("".equals(node1.getNodeName()))
 								item.setType(node1.getTextContent());
 						}						
 					}
 					
 					
 					if(hash.containsKey(item.getType()))
 					{
 						TreeItem itemPat = (TreeItem)hash.get(item.getType());
 						item.setParentId(itemPat.getId());
 					}
 					else
 						item.setParentId("-1");
 					hash.put(item.getName(), item);
 					
 					item.setChecked(false);
 					item.setLeaf(true);
 					items.add(item);							
 				}	
 				
 				if(items.size() != 0){
 					TreeItem ti = new TreeItem();
 					ti.setId("-1");
 					ti.setText("");
 					ti.setName("");
 					ti.setLeaf(false);
 					ti.setLevel("1");
 					ti.setChecked(false);
 					ti.setParentId("0");
 					items.add(0, ti);
 				}
 				
 				setTreeItems(items);
 			}
 
 			//System.out.println(xml);
 			//System.out.println(str);
 			if(stub!=null)
 				stub.cleanup();
 		
 			System.out.println("GetCheckTree---");
 			
 //			org.tempuri.ServiceSgStub stub = new org.tempuri.ServiceSgStub();
 //			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 //
 //		
 //			org.tempuri.GetCheckTree tree = new org.tempuri.GetCheckTree();
 //			tree.setStrTree(fntree);
 //			tree.setStrName(user.getUserName());
 //			
 ////			org.tempuri.ServiceSgCallbackHandler callback = new org.tempuri.ServiceSgCallbackHandler();
 ////			stub.startgetCheckTree(tree, callback);
 //			
 //			org.tempuri.GetCheckTreeResponse res = stub.getCheckTree(tree);
 //			int i = res.getGetCheckTreeResult();
 //			
 //			String xml = res.getStrXml();
 //			String str = res.getStrMsg();
 //
 //			System.out.println(xml);
 //			System.out.println(str);
 //			stub.cleanup();
 			
 			
 			
 			
 //			org.tempuri.ServiceSgLocator local = new org.tempuri.ServiceSgLocator();
 //			org.tempuri.IServiceSg stub = local.getWSHttpBinding_IServiceSg();
 //			javax.xml.rpc.holders.StringHolder strXml = new javax.xml.rpc.holders.StringHolder("");
 //			javax.xml.rpc.holders.StringHolder strMsg = new javax.xml.rpc.holders.StringHolder("");
 //			javax.xml.rpc.holders.IntegerWrapperHolder hold = new javax.xml.rpc.holders.IntegerWrapperHolder();
 //			stub.getCheckTree(fntree, user.getUserName(), hold, strXml, strMsg);
 //			
 //			System.out.println(strXml.value);
 //
 //			System.out.println(strMsg.value);
 
 			
 			
 //			TestHelloFriend hf = new TestHelloFriend();
 //			List<TreeItem> items = hf.GetJunCheckTreeClient(fntree,user.getUserName(),"GetJunCheckTree");
 //			setTreeItems(items);
 
 			
 //			SgDemoClient sg = new SgDemoClient();
 //			List<TreeItem> items = sg.GetJunCheckTreeClient(fntree,user.getUserName(),"GetJunCheckTree");
 //			setTreeItems(items);
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			if(stub!=null)
 				stub.cleanup();
 			System.out.println("GetCheckTree---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetJunArticleInfoGrid() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String articleId = request.getParameter("articleId");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetJunArticleInfoGrid(articleId,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetCheckResultHtml() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String result = (String)request.getSession().getAttribute("result");	
 		result = new String(result.getBytes("iso-8859-1"),"gbk");
 		String articleId = request.getParameter("articleId");
 		
 		client.ServiceSgStub stub  = null;
 
 		try
 		{		
 			
 //			org.tempuri.ServiceLineLocator local = new org.tempuri.ServiceLineLocator();
 //			org.tempuri.IServiceLine line = local.getBasicHttpBinding_IServiceLine();
 //			String sss = line.helloWCF("ddddddddddddd");
 //			System.out.println(sss);
 			
 			
 			
 //			TestHelloFriend hf = new TestHelloFriend();
 //			String res = hf.GetCheckResultClient(articleId,result,"GetCheckResult");
 //			res = res.replaceAll("&amp;", "&");
 			
 			
 			System.out.println("GetXml(GetCheckResultHtml)---ʼ");
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetXml xml = new client.ServiceSgStub.GetXml();
 			xml.setStrSql("select dbo.SG_GetInfoHtml('"+articleId+"') as htm");
 			
 			client.ServiceSgStub.GetXmlResponse res = stub.getXml(xml);
 			int m = res.getGetXmlResult();
 			
 			if(m < 1){
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetXml(GetCheckResultHtml)---С1");
 				return "success";
 			}
 			
 			
 			String str = res.getStrXml();
 			
 			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 			javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 			java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(str.getBytes()));
 			org.w3c.dom.Document doc = builder.parse(in);
 			org.w3c.dom.Element root = doc.getDocumentElement();
 			String ss =root.getFirstChild().getFirstChild().getTextContent(); 		
 			
 			ss = ss.replaceAll("&amp;", "&");
 			
 			setMessage(ss);
 			
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("GetXml(GetCheckResultHtml)---");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("GetXml(GetCheckResultHtml)---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String setContent() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String nav = (String)request.getSession().getAttribute("nav");	
 		nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 
 		try
 		{								
 			setMessage(lawFLDAO.setContent(this.getSn(),this.getContent(),nav,this.getType(),request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String backJunCheck() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String navsp = (String)request.getSession().getAttribute("navsp");	
 		navsp = new String(navsp.getBytes("iso-8859-1"),"gbk");
 
 		try
 		{								
 			setMessage(lawFLDAO.backJunCheck(this.getType(),this.getSn(),navsp,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String setContentNew() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String nav = (String)request.getSession().getAttribute("nav");	
 		nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 		
 		String sn = request.getParameter("sn");
 		String content = request.getParameter("content");
 		String type = request.getParameter("type");
 
 		try
 		{								
 			setMessage(lawFLDAO.setContent(sn,content,nav,type,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetContent() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetContent(sn,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetSenContent() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetSenContent(sn,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String setDecision() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String nav = (String)request.getSession().getAttribute("nav");	
 		nav = new String(nav.getBytes("iso-8859-1"),"gbk");
 		String sn = request.getParameter("sn");
 		String decision = request.getParameter("decision");
 		String type = request.getParameter("type");
 
 		try
 		{								
 			setMessage(lawFLDAO.setDecision(sn,decision,nav,type,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetDecision() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetDecision(sn,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetSearchItemSg() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		client.ServiceSgStub stub = null;
 		String paramLog = null;	
 		try
 		{
 			if(null!=request.getParameter("paramLog")){
 				paramLog = request.getParameter("paramLog");
 				//request.getSession().setAttribute("paramLog", paramLog);
 			}else{
 				paramLog = (String)request.getSession().getAttribute("paramLog");
 			}
 			//paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 			
 			paramLog = paramLog.toUpperCase();
 			if(paramLog.contains("*")){
 				int index = paramLog.indexOf("*");
 				String str = paramLog.substring(0,index);
 				str = str + " top 1 ";
 				paramLog = str + paramLog.substring(index);
 			}
 			if(paramLog.contains("WHERE")){
 				int index = paramLog.indexOf("WHERE");
 				paramLog = paramLog.substring(0,index);
 				paramLog = paramLog + " WHERE 1=0";			
 			}else{
 				paramLog = paramLog + " WHERE 1=0";	
 			}				
 			
 			//setSearchList(lawFLDAO.GetSearchItem(paramLog,request));
 //			TestHelloFriend tf = new TestHelloFriend();
 //			setSearchList(tf.getSgFieldClient(paramLog, "GetSgFields"));
 			
 			
 			System.out.println("GetFeilds---ʼ"+paramLog);
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetFeilds field = new client.ServiceSgStub.GetFeilds();			
 			field.setStrSql(paramLog);
 			
 			client.ServiceSgStub.GetFeildsResponse res = stub.getFeilds(field);
 			int m = res.getGetFeildsResult();
 			
 			if(m < 1){
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetFeilds---С1"+paramLog);
 				return "success";
 			}
 			
 			
 			String str = res.getStrFeild();
 			String[] fd = str.split(";");
 			List<SearchItem> items = new ArrayList<SearchItem>();
 			for(String ff:fd){
 				SearchItem item = null;
 				item = new SearchItem(ff,ff,"String");
 				items.add(item);
 			}
 			
 			setSearchList(items);
 			if(stub != null)
 				stub.cleanup();	
 			System.out.println("GetFeilds---"+paramLog);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			if(stub != null)
 				stub.cleanup();
 			setMessage("ݿʴ");
 			System.out.println("GetFeilds---"+paramLog);
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetGridSg() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		client.ServiceSgStub stub = null;
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			//request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		//paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");		
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());		
 		
 		String articleSn = request.getParameter("articleSn");
 		
 		if(! "".equals(articleSn) && articleSn != null)
 			//paramLog = paramLog + " and ±='"+articleSn+"'";
 			paramLog = paramLog + " and ű='"+articleSn+"'";	
 		else
 			return "success";
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			paramLog = paramLog + " " + text;
 		}
 		
 		String orderStr = request.getParameter("sort");
 		String dirStr = request.getParameter("dir");
 		if(orderStr != null && !"".equals(orderStr) && dirStr != null && !"".equals(dirStr))
 			orderStr = " order by "+orderStr+" "+dirStr;
 		
 		if(! paramLog.contains("order")){
 			if(orderStr != null && !"".equals(orderStr))
 				paramLog += orderStr;
 			else
 				paramLog += " order by  desc, desc ";
 		}
 		paramLog = paramLog.replaceAll("1=1 and", "");
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param)){
 				//setStrResutlItem(Select(paramLog));
 				
 //				service.HelloFriend tf = new service.HelloFriend();
 //				setStrResutlItem(tf.GetSgGridPanel(paramLog));
 				
 				System.out.println("GetXml(GetGridSg)---ʼ"+paramLog);
 				stub = new client.ServiceSgStub();
 				stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 				client.ServiceSgStub.GetXml xml = new client.ServiceSgStub.GetXml();	
 				xml.setStrSql(paramLog);
 				
 				client.ServiceSgStub.GetXmlResponse res = stub.getXml(xml);
 				int m = res.getGetXmlResult();
 				
 				if(m < 1){
 					setSuccess(false);
 					setMessage(res.getStrMsg());
 					if(stub != null)
 						stub.cleanup();
 					System.out.println("GetXml(GetGridSg)---С1"+paramLog);
 					return "success";
 				}
 				
 				String str = res.getStrXml();
 				
 				javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 				javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 				java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(str.getBytes()));
 				org.w3c.dom.Document doc = builder.parse(in);
 				org.w3c.dom.Element root = doc.getDocumentElement();
 				org.w3c.dom.NodeList nodeList = root.getElementsByTagName("REC");
 				List<HashMap<String,String>> items = new ArrayList<HashMap<String,String>>();
 				for(int i=0;i<nodeList.getLength();i++){
 					org.w3c.dom.Node node = nodeList.item(i);
 					org.w3c.dom.NodeList conList = node.getChildNodes();
 					HashMap<String,String> item = new HashMap<String,String>();
 					for(int j=0;j<conList.getLength();j++){
 						org.w3c.dom.Node node1 = conList.item(j);
 						if(node1.getNodeType() == Node.ELEMENT_NODE){
 							if("int".equals(node1.getNodeType()))
 								item.put(node1.getNodeName(), String.valueOf(node1.getTextContent()));
 							else
 								item.put(node1.getNodeName(), node1.getTextContent());
 						}	
 					}
 					items.add(item);
 				}
 				
 				setStrResutlItem(items);
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetXml(GetGridSg)---");
 			}
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetXml(GetGridSg)---");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetHtmlPage() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String sn = request.getParameter("sn");
 		String fileName = request.getParameter("fileName");
 
 		try
 		{		
 			TestHelloFriend tf = new TestHelloFriend();
 			setMessage(tf.GetHtmlPageClient(sn,fileName,"GetHtmlPage"));
 			
 			
 //			Fantasy.WebServiceDemo.Service1Locator sl = new Fantasy.WebServiceDemo.Service1Locator();
 //			Fantasy.WebServiceDemo.Service1Soap _stub = sl.getService1Soap12();
 //			String str = "";
 //			str = _stub.executeSql("192.168.60.244@sa@newspaper@@","select ȶı from dbo.SG_Ϣ(nolock) where ±='"+sn+"' and ļ='"+fileName+"'");	//		
 //			String[] sp = str.split("");
 //			str = sp[1];
 //			str = str.replaceAll("&lt;", "<");
 //			str = str.replaceAll("&gt;", ">");
 //			str = str.substring(20, str.length()-23);
 //			setMessage(str);
 			
 			
 //			Fantasy.WebServiceDemo.Service1Locator sl = new Fantasy.WebServiceDemo.Service1Locator();
 //			Fantasy.WebServiceDemo.Service1Soap _stub = sl.getService1Soap12();
 //			int m = 1;
 //			int n = 30000;
 //			int remain = 1;
 //			String text = "",finalStr="",error="";	
 //			while(remain > 0){
 //				String str = _stub.executeSql("192.168.60.244@sa@newspaper@@","select * from [dbo].[SG_GetLink]('"+sn+"','"+fileName+"',"+m+","+n+")");	//
 //				
 //				int sta = str.indexOf("");
 //				int num = Integer.parseInt(str.substring(0, sta));
 //				if(num==1){			
 //					//String[] sp = str.split("");
 //					//str = sp[1];
 //					str = str.replaceAll("1<NewDataSet><a>", "");
 //					str = str.replaceAll("</a></NewDataSet>", "");
 //					//str = str.substring(15, str.length()-17);
 //					
 //					if(str.contains("<ı>") && str.contains("</ı>")){
 //						int start = str.indexOf("<ı>");
 //						int end = str.indexOf("</ı>");
 //						text = str.substring(start+4, end);
 //					}
 //					
 //					
 //					if(str.contains("<ʣ>") && str.contains("</ʣ>")){
 //						int start = str.indexOf("<ʣ>");
 //						int end = str.indexOf("</ʣ>");
 //						remain = Integer.parseInt(str.substring(start+4, end));
 //					}
 //					m+=n;
 //					
 //					finalStr+=text;
 //				}else{
 //					error = str.substring(sta+1, str.length());
 //					setSuccess(false);
 //					setMessage(error);
 //					return "success";
 //				}		
 //			}
 //			finalStr = finalStr.replaceAll("&lt;", "<");
 //			finalStr = finalStr.replaceAll("&gt;", ">");	
 //			
 //			setMessage(finalStr);
 //			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetCompare() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		client.ServiceSgStub stub = null;
 		String sn = request.getParameter("sn");
 		String fileNames = request.getParameter("fileNames");
 		//String similar = request.getParameter("similar");
 		String articleSn = request.getParameter("articleSn");
 		//String userName = request.getParameter("userName");
 		//String password = request.getParameter("password");
 
 		try
 		{		
 //			TestHelloFriend tf = new TestHelloFriend();
 //			String res = tf.GetCompareClient(userName,password,sn,fileNames,similar,articleSn,"GetCompare");
 //			res = res.replaceAll("&amp;", "&");
 //			setMessage(res);
 			
 			
 //			setMessage(lawFLDAO.GetCompare(userName,password,sn,fileNames,similar,articleSn,request));
 			
 			
 			System.out.println("GetStepCompare---ʼ");
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetStepCompare step = new client.ServiceSgStub.GetStepCompare();
 			step.setStrFilename(articleSn);
 			step.setStrFiles(fileNames);
 			step.setStrId(sn);
 			
 			client.ServiceSgStub.GetStepCompareResponse res = stub.getStepCompare(step);
 			String str = res.getGetStepCompareResult();
 			
 			if(str.length() < 1){
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetStepCompare---سС1");
 				return "success";
 			}
 			
 			str = str.replaceAll("&amp;", "&");
 			
 			setMessage(str);
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("GetStepCompare---");
 			
 //			net.cnki.check.Service1Locator sl = new net.cnki.check.Service1Locator();
 //			net.cnki.check.Service1Soap _stub = sl.getService1Soap12();
 //			javax.xml.rpc.holders.StringHolder mess = new javax.xml.rpc.holders.StringHolder("");
 //			javax.xml.rpc.holders.BooleanHolder result = new javax.xml.rpc.holders.BooleanHolder(false);
 //			javax.xml.rpc.holders.ByteArrayHolder ds_checkInfo = new javax.xml.rpc.holders.ByteArrayHolder();
 //			_stub.getCheckinfoStepXML("user06","11111111",sn,fileNames,mess,result,ds_checkInfo);
 //			System.out.println(mess);
 			
 			
 //			String ss = "aGVsbG8sy++93A==";//new sun.misc.BASE64Encoder().encode("hello,123".getBytes());
 //			
 //			BASE64Decoder decoder = new BASE64Decoder(); 
 //			try { 
 //			byte[] b = decoder.decodeBuffer(ss); 
 //			String str = new String(b,"gb2312");
 //			System.out.println(str); 
 //			} catch (Exception e) { 
 //			return null; 
 //			} 
 			
 //			Service1Stub stub = new Service1Stub();
 //			Service1Stub.GetCheckinfoStepXML step = new Service1Stub.GetCheckinfoStepXML();
 //			step.setStr_uid("user06");
 //			step.setStr_pwd("11111111");
 //			step.setStr_LeftName(sn);
 //			step.setStr_RightNameList(fileNames);
 //			step.setMess("");
 //			
 //			String sss = stub.getCheckinfoStepXML(step).getXML();
 //			System.out.println(sss);
 
 //			Service1Stub stub = new Service1Stub();
 //			Service1Stub.GetCheckinfoStep step = new Service1Stub.GetCheckinfoStep();
 //			step.setStr_uid(userName);
 //			step.setStr_pwd(password);
 //			step.setStr_LeftName(sn);
 //			step.setStr_RightNameList(fileNames);
 //			step.setMess("");
 //			
 //			Ds_checkinfo_type1 sss = stub.getCheckinfoStep(step).getDs_checkinfo();
 //			OMElement text = sss.getExtraElement();
 //			String string = text.toString();
 //			String remain = "";
 //
 //			if(string.contains("<ָƱ>") && string.contains("</ȫı>")){
 //				int start = string.indexOf("<ָƱ>");
 //				int end = string.indexOf("</ȫı>");
 //				remain = string.substring(start, end+7);
 //			}
 //			
 //			System.out.println(text);
 //			System.out.println(remain);
 
 
 			
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("GetStepCompare---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String deleteFileNames() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		client.ServiceSgStub stub = null;
 		String fileNames = request.getParameter("fileNames");
 		//String userName = request.getParameter("userName");
 		//String password = request.getParameter("password");
 		String sn = request.getParameter("sn");
 		String articleSn = request.getParameter("articleSn");
 
 		try
 		{		
 //			TestHelloFriend tf = new TestHelloFriend();
 //			setMessage(tf.deleteFileNamesClient(userName,password,fileNames,"deleteFileNames"));		
 
 			
 			System.out.println("DelFiles---ʼ");
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.DelFiles xml = new client.ServiceSgStub.DelFiles();
 			xml.setStrId(sn);
 			xml.setStrFiles(fileNames);
 			xml.setStrSn(articleSn);
 			
 			client.ServiceSgStub.DelFilesResponse res = stub.delFiles(xml);
 			int m = res.getDelFilesResult();
 			
 			if(m==2){
 				setSuccess(true);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("DelFiles---ص2");
 				return "success";
 			}
 			else if(m < 0){
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("DelFiles---С0");
 				return "success";
 			}else if(m==1){
 				setSuccess(false);
 				setMessage("-1");
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("DelFiles---ص1");
 				return "success";
 			}else{
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("DelFiles---");
 				return "success";
 			}
 			
 			
 			
 			
 //			Service1Stub stub = new Service1Stub();		
 //			Service1Stub.DeleteFile file = new Service1Stub.DeleteFile();
 //			file.setStr_uid(userName);
 //			file.setStr_pwd(password);
 //			file.setStr_LeftFileName(sn);
 //			file.setStr_RightFileNameList(fileNames);
 //			file.setMess("");
 //			
 //			client.Service1Stub.DeleteFileResponse res = stub.deleteFile(file);
 //			boolean bool = res.getDeleteFileResult();
 //			String sss = res.getMess();
 //
 //			
 //			if(bool == true){				
 //				Service1Stub.GetCheckResult result = new Service1Stub.GetCheckResult();
 //				result.setStr_uid(userName);
 //				result.setStr_pwd(password);
 //				result.setFileNumber(sn);
 //				result.setMess("");
 //				client.Service1Stub.GetCheckResultResponse resultRes = stub.getCheckResult(result);
 //				boolean resultBool = resultRes.getGetCheckResultResult();
 //				
 //				String strMess = "";
 //				if(resultBool == true){
 //					strMess = resultRes.getMess();
 //					setMessage(strMess);
 //					
 //					setSuccess(true);
 //					return "success";
 //				}else{				
 //					setMessage(strMess);
 //					
 //					setSuccess(false);
 //					return "success";
 //				}
 //				
 //			}else{
 //				setMessage(sss);
 //				
 //				setSuccess(false);
 //				return "success";
 //			}
 
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ɾļִ!");
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("DelFiles---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}		
 		
 	}
 	
 	public String GetCheckNum() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		client.ServiceSgStub stub = null;
 		//String userName = request.getParameter("userName");
 		//String password = request.getParameter("password");
 		String sn = request.getParameter("sn");
 		String fileNames = request.getParameter("fileNames");;
 		String articleSn = request.getParameter("articleSn");
 
 		try
 		{		
 			System.out.println("GetDelResult---ʼ");
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetDelResult xml = new client.ServiceSgStub.GetDelResult();
 			xml.setStrId(sn);
 			xml.setStrFiles(fileNames);
 			xml.setStrSn(articleSn);
 			
 			client.ServiceSgStub.GetDelResultResponse res = stub.getDelResult(xml);
 			int m = res.getGetDelResultResult();
 			
 			if(m==0){
 				setSuccess(false);
 				setMessage("-1");
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetDelResult---ص0");
 				return "success";
 			}else if(m<0){
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetDelResult---С0");
 				return "success";
 			}else{
 				setSuccess(true);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetDelResult---");
 				return "success";
 			}		
 			
 			
 			
 //			client.Service1Stub stub = new client.Service1Stub();								
 //			client.Service1Stub.GetCheckResult result = new client.Service1Stub.GetCheckResult();
 //			result.setStr_uid(userName);
 //			result.setStr_pwd(password);
 //			result.setFileNumber(sn);
 //			result.setMess("");
 //			client.Service1Stub.GetCheckResultResponse resultRes = stub.getCheckResult(result);
 //			boolean resultBool = resultRes.getGetCheckResultResult();
 //			
 //			String strMess = "";
 //			if(resultBool == true){
 //				strMess = resultRes.getMess();
 //				setMessage(strMess);
 //				
 //				setSuccess(true);
 //				return "success";
 //			}else{				
 //				setMessage(strMess);
 //				
 //				setSuccess(false);
 //				return "success";
 //			}
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ȡƱȷִ!");
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("GetDelResult---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}				
 	}
 	
 	public String deleteLocalFileNames() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		String userName = request.getParameter("userName");
 		String password = request.getParameter("password");
 		String sn = request.getParameter("sn");
 
 		String fileNames = request.getParameter("fileNames");
 		String articleSn = request.getParameter("articleSn");
 		String percent = request.getParameter("percent");
 
 		try
 		{		
 //			TestHelloFriend tf = new TestHelloFriend();
 //			setMessage(tf.deleteLocalFileNamesClient(articleSn,fileNames,percent,"deleteLocalFileNames"));
 			
 			
 			
 			client.Service1Stub stub = new client.Service1Stub();								
 			client.Service1Stub.GetInputList result = new client.Service1Stub.GetInputList();
 			result.setStr_uid(userName);
 			result.setStr_pwd(password);
 			result.setFileNumber(sn);
 			result.setMess("");
 			
 			Ds_checkinfo_type2 sss = stub.getInputList(result).getDs_checkinfo();
 			OMElement text = sss.getExtraElement();
 			String string = text.toString();
 			String remain = "";
 
 			if(string.contains("<ȫı>") && string.contains("</ȫı>")){
 				int start = string.indexOf("<ȫı>");
 				int end = string.indexOf("</ȫı>");
 				
 				if(end < start){
 					setMessage("ȡݱض!");
 					setSuccess(true);
 				    return SUCCESS;  
 				}
 				
 				remain = string.substring(start+6, end);
 			}			
 			
 			
 			//Fantasy.WebServiceDemo.Service1Locator sl = new Fantasy.WebServiceDemo.Service1Locator();
 			//Fantasy.WebServiceDemo.Service1Soap _stub = sl.getService1Soap12();
 			
             //String retRows = "";
             String error = "";
 			//String strSql = "";
 			if(percent == null || "".equals(percent)){
 				error = "İٷֱΪ!";
 				setMessage(error);
 			}else{
 				
 				
 				
 				
 				setMessage(lawFLDAO.articleDelete(articleSn,fileNames,percent,remain,"SP_±ȶ_ɾ",request));
 
 				
 				
 //				strSql = "@Sn#" + articleSn + "#VarChar#in#$";
 //	            strSql = strSql + "@Files#" + fileNames + "#VarChar#in#$";
 //	            strSql = strSql + "@Copy#" + Integer.parseInt(percent) + "#int#in#$";
 //	            strSql = strSql + "@RedText#" + remain + "#VarChar#in#$";
 //	            strSql = strSql + "@RetRows##Int#out#$";
 //	            strSql = strSql + "@err##VarChar#out#$";
 //	            String strErrMsg = _stub.executeStorageProcess("192.168.60.244@sa@newspaper@@", "SP_±ȶ_ɾ", strSql);
 //	            
 //	            if(strErrMsg == null || "".equals(strErrMsg)){
 //	            	setMessage("洢ִд!");
 //	            	setSuccess(true);
 //	        	    return SUCCESS; 
 //	            }else{
 //	            	if(strErrMsg.indexOf("@RetRows=1")>0)
 //	            		setMessage(error);
 //	            	else{
 //	            		setMessage(strErrMsg);
 //	            	}
 //	            }	            
 			}	
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ɾļ洢ִ̳!");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String submitJun() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		 
 		try
 		{
 			String spsu = (String)request.getSession().getAttribute("spsu");	
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 			String spget = (String)request.getSession().getAttribute("spget");	
 			spget = new String(spget.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 			String check = request.getParameter("check");
 			
 			setMessage(lawFLDAO.submitJun(spsu,str,check,user.getUserName(),spget,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	
 	public String GetSenCheckTree() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		
 		String fntree = (String)request.getSession().getAttribute("fntree");	
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		
 		client.ServiceSgStub stub = null;
 
 		try
 		{								
 			
 			
 //			Fantasy.WebServiceDemo.Service1Locator sl = new Fantasy.WebServiceDemo.Service1Locator();
 //			Fantasy.WebServiceDemo.Service1Soap _stub = sl.getService1Soap12();
 //			String str = _stub.executeSql("192.168.60.244@sa@newspaper@@","select * from "+ fntree +"('"+user.getUserName()+"') order by ű");			
 //			String[] sp = str.split("");
 //			str = sp[1];
 //			str = "<?xml version=\"1.0\" encoding=\"GB2312\"?>"+str;
 //			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 //			javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 //			java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(str.getBytes()));
 //			org.w3c.dom.Document doc = builder.parse(in);
 //			org.w3c.dom.Element root = doc.getDocumentElement();
 //			org.w3c.dom.NodeList nodeList = root.getElementsByTagName("a");
 //			List<TreeItem> items = new ArrayList<TreeItem>();		
 //			
 //			for(int i=0;i<nodeList.getLength();i++){
 //				org.w3c.dom.Node node = nodeList.item(i);
 //				org.w3c.dom.NodeList conList = node.getChildNodes();
 //				TreeItem item = new TreeItem();
 //				for(int j=0;j<conList.getLength();j++){
 //					org.w3c.dom.Node node1 = conList.item(j);
 //					if(node1.getNodeType() == Node.ELEMENT_NODE){
 //						//org.w3c.dom.Element element = (org.w3c.dom.Element)node1;
 //						if("".equals(node1.getNodeName()))
 //							item.setId(node1.getTextContent());
 //						else if("±".equals(node1.getNodeName())){
 //							item.setName(node1.getTextContent());
 //							item.setText(node1.getTextContent());						
 //						}else if("ű".equals(node1.getNodeName()))
 //							item.setLevel(node1.getTextContent());
 //						else if("".equals(node1.getNodeName()))
 //							item.setType(node1.getTextContent());
 //					}					
 //				}
 //				if(item.getName()!=null)
 //					if(item.getName().equals(item.getType()))
 //						item.setParentId("0");
 //					else{
 //						for(TreeItem it:items){
 //							if(it.getName().equals(it.getType()) && it.getType().equals(item.getType())){
 //								item.setParentId(it.getId());
 //								break;
 //							}
 //						}
 //					}
 //						
 //				item.setChecked(false);
 //				item.setLeaf(true);
 //				items.add(item);				
 //			}	
 			
 			
 			
 			
 //			TestHelloFriend hf = new TestHelloFriend();
 //			List<TreeItem> items = hf.GetJunCheckTreeClient(fntree,user.getUserName(),"GetSenCheckTree");
 			
 			
 			
 			System.out.println("GetCheckTree---ʼ");
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetCheckTree tree = new client.ServiceSgStub.GetCheckTree();			
 			tree.setStrTree(fntree);
 			tree.setStrName(user.getUserName());
 			
 			client.ServiceSgStub.GetCheckTreeResponse res = stub.getCheckTree(tree);
 			int m = res.getGetCheckTreeResult();
 			
 			String xml = res.getStrXml();
 			String str = res.getStrMsg();
 			
 			
 			if(m==0){
 				setSuccess(false);
 				setMessage(str);
 				if(stub!=null)
 					stub.cleanup();
 				System.out.println("GetCheckTree---ص0");
 				return "success";
 			}else{
 				//xmlNew = "<?xml version=\"1.0\" encoding=\"GB2312\"?>"+xmlNew;
 				javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 				javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 				java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(xml.getBytes()));
 				org.w3c.dom.Document doc = builder.parse(in);
 				org.w3c.dom.Element root = doc.getDocumentElement();
 				org.w3c.dom.NodeList nodeList = root.getElementsByTagName("REC");
 				List<TreeItem> items = new ArrayList<TreeItem>();		
 				
 				HashMap<String,TreeItem> hash = new HashMap<String,TreeItem>();
 				
 				for(int i=0;i<nodeList.getLength();i++){
 					org.w3c.dom.Node node = nodeList.item(i);
 					org.w3c.dom.NodeList conList = node.getChildNodes();
 					TreeItem item = new TreeItem();
 					for(int j=0;j<conList.getLength();j++){
 						org.w3c.dom.Node node1 = conList.item(j);
 						if(node1.getNodeType() == Node.ELEMENT_NODE){
 							//org.w3c.dom.Element element = (org.w3c.dom.Element)node1;
 							if("".equals(node1.getNodeName()))
 								item.setId(node1.getTextContent());
 							else if("±".equals(node1.getNodeName())){
 								item.setName(node1.getTextContent());
 								item.setText(node1.getTextContent());						
 							}else if("ű".equals(node1.getNodeName()))
 								item.setLevel(node1.getTextContent());
 							else if("".equals(node1.getNodeName()))
 								item.setType(node1.getTextContent());
 						}						
 					}
 					
 					
 					if(hash.containsKey(item.getType()))
 					{
 						TreeItem itemPat = (TreeItem)hash.get(item.getType());
 						item.setParentId(itemPat.getId());
 					}
 					else
 						item.setParentId("-1");
 					hash.put(item.getName(), item);
 					
 					item.setChecked(false);
 					item.setLeaf(true);
 					items.add(item);							
 				}	
 				
 				if(items.size() != 0){
 					TreeItem ti = new TreeItem();
 					ti.setId("-1");
 					ti.setText("");
 					ti.setName("");
 					ti.setLeaf(false);
 					ti.setLevel("1");
 					ti.setChecked(false);
 					ti.setParentId("0");
 					items.add(0, ti);
 				}
 				
 				setTreeItems(items);
 			}
 
 			//System.out.println(xml);
 			if(stub!=null)
 				stub.cleanup();
 			System.out.println("GetCheckTree---");
 			
 			
 			//setTreeItems(items);		
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			if(stub!=null)
 				stub.cleanup();	
 			setMessage("ݿʴ");
 			System.out.println("GetCheckTree---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String submitSen() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		 
 		try
 		{
 			String spsu = (String)request.getSession().getAttribute("spsu");	
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 			String spget = (String)request.getSession().getAttribute("spget");	
 			spget = new String(spget.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 			String check = request.getParameter("check");
 			
 			setMessage(lawFLDAO.submitSen(spsu,str,check,user.getUserName(),spget,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	
 	public String GetComboxStore() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		try
 		{	
 			setSearchList(lawFLDAO.GetComboxStore(request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	
 	
 	
 	
 	public String GetProAuthorTree() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		
 		String fntree = (String)request.getSession().getAttribute("fntree");	
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 
 		try
 		{								
 			setTreeItems(lawFLDAO.GetProAuthorTree(user.getUserName(),fntree,request));
 			
 //			TestHelloFriend hf = new TestHelloFriend();
 //			List<TreeItem> items = hf.GetJunCheckTreeClient(fntree,user.getUserName(),"GetJunCheckTree");
 //			setTreeItems(items);
 			
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetProAuthorSearchItem() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		 
 		String paramLog = null;	
 		try
 		{
 			if(null!=request.getParameter("paramLog")){
 				paramLog = request.getParameter("paramLog");
 				request.getSession().setAttribute("paramLog", paramLog);
 			}else{
 				paramLog = (String)request.getSession().getAttribute("paramLog");
 			}
 			paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");	
 			
 			paramLog = paramLog.toUpperCase();
 			if(paramLog.contains("*")){
 				int index = paramLog.indexOf("*");
 				String str = paramLog.substring(0,index);
 				str = str + " top 1 ";
 				paramLog = str + paramLog.substring(index);
 			}
 			if(paramLog.contains("WHERE")){
 				int index = paramLog.indexOf("WHERE");
 				paramLog = paramLog.substring(0,index);
 				paramLog = paramLog + " WHERE 1=1";			
 			}else{
 				paramLog = paramLog + " WHERE 1=1";	
 			}				
 			
 			setSearchList(lawFLDAO.GetSearchItem(paramLog,request));
 			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetProAuthorGrid() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		User user = (User)request.getSession().getAttribute("app_user");
 		
 		String paramLog = null;
 		if(null!=request.getParameter("paramLog")){
 			paramLog = request.getParameter("paramLog");
 			request.getSession().setAttribute("paramLog", paramLog);
 		}else{
 			paramLog = (String)request.getSession().getAttribute("paramLog");
 		}
 		paramLog = new String(paramLog.getBytes("iso-8859-1"),"gbk");		
 		
 		paramLog = paramLog.replaceAll("!A!", user.getUserName());
 		paramLog = paramLog.replaceAll("!N!", user.getNickName());
 		paramLog = paramLog.replaceAll("!P!", user.getUserType());
 		paramLog = paramLog.replaceAll("!D!", user.getDepartmentId());
 		
 		String articleSn = request.getParameter("articleSn");
 		
 		if(! "".equals(articleSn) && articleSn != null)
 			paramLog = paramLog + " and ='"+articleSn+"'";
 		
 		
 		String text = request.getParameter("sqlStr");
 		if(null!=text)
 			request.getSession().setAttribute("queryStr", text);
 		else
 			text = (String)request.getSession().getAttribute("queryStr");
 		
 		if(text != null && !"".equals(text)){
 			if(! text.trim().startsWith("order"))
 				text = " and " + text;	
 			paramLog = paramLog + " " + text;
 		}
 		
 		String orderStr = request.getParameter("sort");
 		String dirStr = request.getParameter("dir");
 		if(orderStr != null && !"".equals(orderStr) && dirStr != null && !"".equals(dirStr))
 			orderStr = " order by "+orderStr+" "+dirStr;
 		
 		if(! paramLog.contains("order")){
 			if(orderStr != null && !"".equals(orderStr))
 				paramLog += orderStr;
 			else
 				paramLog += " order by ʱ ";
 		}
 
         try {
 			String param = (String)request.getParameter("Param");
 			if("data".equals(param)){
 				setStrResutlItem(Select(paramLog));
 				
 			}
 		} catch (Exception e) {
 			if(e.toString().indexOf("com.microsoft.sqlserver.jdbc.SQLServerException")==0){
 				setSuccess(false);
 				setMessage("༭Ĳѯ䲻SQL﷨");
 				return "success";
 			}else{
 				e.printStackTrace();
 				setSuccess(false);
 				setMessage("ݿʴ");
 				return "success";
 			}
 		}
 
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetProAuthorHtml() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		String articleId = request.getParameter("articleId");
 
 		try
 		{								
 			String res = lawFLDAO.GetProAuthorHtml(articleId,request);
 			//res = res.replaceAll("&amp;", "&");
 			setMessage(res);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetJouFirstHtml() throws Exception {
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		String articleId = request.getParameter("articleId");
 
 		try
 		{								
 			String res = lawFLDAO.GetJouFirstHtml(articleId,request);
 			//res = res.replaceAll("&amp;", "&");
 			setMessage(res);
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetCovFLClick() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String name = (String)request.getSession().getAttribute("spup");
 		name = new String(name.getBytes("iso-8859-1"),"gbk");
 		
 		String newValue = request.getParameter("editParam");
 		String taskId = request.getParameter("taskNum");
 		String colName = request.getParameter("colName");
 		 
 		try
 		{								
 			setMessage(lawFLDAO.GetCovFLClick(taskId,newValue,colName,name,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String downCovFile() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		HttpServletResponse response=ServletActionContext.getResponse();
 
 		StringBuffer url = request.getRequestURL();
 		String requestUrl = url.toString();
 		String sn = request.getParameter("sn");
 		String procedure = request.getParameter("procedure");
 		procedure = new String(procedure.getBytes("iso-8859-1"),"gbk");
 		String filePath = "";
 
 		try
 		{		
 			filePath = lawFLDAO.downCovFile(sn,procedure,requestUrl,request);
 			if("".equals(filePath)){
 				setMessage("ļѾ");
 				return SUCCESS;
 			}else{				
 				int pos = filePath.indexOf("create_file");
 				String name = filePath.substring(pos+12, filePath.length());
 				
 				setMessage("");
 				response.reset();
 				response.setContentType("application/x-msdownload");
 		        response.setHeader("Content-Disposition","attachment;" + " filename="+name);
 		        downFile(response.getOutputStream(),filePath);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String submitCover() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		 
 		try
 		{
 			String spsu = (String)request.getSession().getAttribute("spsu");	
 			spsu = new String(spsu.getBytes("iso-8859-1"),"gbk");
 			String str = request.getParameter("paramStr");
 			
 			setMessage(lawFLDAO.submitCover(spsu,str,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
         return SUCCESS;  
 	}
 	
 	public String GetArticleTree() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		
 		String fntree = (String)request.getSession().getAttribute("fntree");	
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setTreeItems(lawFLDAO.GetArticleTree(sn,fntree,request));			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	
 	
 	public String GetMaintainList() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 
 		String type = request.getParameter("type");
 		String post = request.getParameter("post");
 		try
 		{								
 			setComboList(lawFLDAO.GetMaintainList(type,post,request));			
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetSenContent1() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetSenContent1(sn,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetDecision1() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String articleId = request.getParameter("articleId");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetDecision1(articleId,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetContent1() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		
 		
 		String articleId = request.getParameter("articleId");
 
 		try
 		{								
 			setArticles(lawFLDAO.GetContent1(articleId,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetMuluTree() throws Exception {
 		//LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		client.ServiceSgStub stub = null;
 //		String fntree = (String)request.getSession().getAttribute("fntree");	
 //		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		
 		String sn = request.getParameter("sn");
 		String faithSn = request.getParameter("faithSn");
 		String fntree = "select a., a.Ŀ¼, a., a.±, SG_ȶϢ.ű,a.,a.ҳ,a.,left(a.Ŀ¼,len(a.Ŀ¼)-4) as ױ"
 				+" from [dbo].[Fu_SG_GetCata]('"+sn+"') a left join SG_ȶϢ with (nolock) on  SG_ȶϢ.±=a.± and SG_ȶϢ.Ŀ¼=a.Ŀ¼";
 
 		try
 		{		
 			System.out.println("GetXml(GetMuluTree)---ʼ");
 			stub = new client.ServiceSgStub();
 			stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(60000);
 			client.ServiceSgStub.GetXml xml = new client.ServiceSgStub.GetXml();
 			xml.setStrSql(fntree);
 			
 			client.ServiceSgStub.GetXmlResponse res = stub.getXml(xml);
 			int m = res.getGetXmlResult();
 			if(m < 1){
 				setSuccess(false);
 				setMessage(res.getStrMsg());
 				if(stub != null)
 					stub.cleanup();
 				System.out.println("GetXml(GetMuluTree)---С1");
 				return "success";
 			}
 			
 			
 			String str = res.getStrXml();
 
 			//xmlNew = "<?xml version=\"1.0\" encoding=\"GB2312\"?>"+xmlNew;
 			javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
 			javax.xml.parsers.DocumentBuilder  builder = factory.newDocumentBuilder();
 			java.io.InputStream in = new java.io.BufferedInputStream(new java.io.ByteArrayInputStream(str.getBytes()));
 			org.w3c.dom.Document doc = builder.parse(in);
 			org.w3c.dom.Element root = doc.getDocumentElement();
 			org.w3c.dom.NodeList nodeList = root.getElementsByTagName("REC");
 			List<TreeItem> items = new ArrayList<TreeItem>();		
 			
 			HashMap<String,TreeItem> hash = new HashMap<String,TreeItem>();
 			
 			for(int i=0;i<nodeList.getLength();i++){
 				org.w3c.dom.Node node = nodeList.item(i);
 				org.w3c.dom.NodeList conList = node.getChildNodes();
 				TreeItem item = new TreeItem();
 				for(int j=0;j<conList.getLength();j++){
 					org.w3c.dom.Node node1 = conList.item(j);
 					if(node1.getNodeType() == Node.ELEMENT_NODE){
 						//org.w3c.dom.Element element = (org.w3c.dom.Element)node1;
 						if("Ŀ¼".equals(node1.getNodeName()))
 							item.setId(node1.getTextContent());
 						else if("".equals(node1.getNodeName())){
 							item.setName(node1.getTextContent());
 							item.setText(node1.getTextContent());						
 						}else if("±".equals(node1.getNodeName()))
 							item.setLevel(node1.getTextContent());
 						else if("ױ".equals(node1.getNodeName()))
 							item.setType(node1.getTextContent());
 						else if("ű".equals(node1.getNodeName()))
 							item.setDataType(node1.getTextContent());
 					}						
 				}
 				
 				
 				if(hash.containsKey(item.getType()))
 				{
 					TreeItem itemPat = (TreeItem)hash.get(item.getType());
 					item.setParentId(itemPat.getId());
 				}
 				else
 					item.setParentId("-1");
 				hash.put(item.getId(), item);
 				
 				//item.setChecked(false);
 				//item.setLeaf(true);
 				items.add(item);					
 			}	
 			
 			if(items.size() != 0){
 				TreeItem ti = new TreeItem();
 				ti.setId("-1");
 				ti.setText("");
 				ti.setName("");
 				//ti.setLeaf(false);
 				ti.setLevel(sn);
 				ti.setDataType(faithSn);
 				//ti.setChecked(false);
 				ti.setParentId("0");
 				items.add(0, ti);
 			}
 			
 			setTreeItems(items);
 
 			//System.out.println(xml);
 			//System.out.println(str);
 
 			System.out.println("GetXml(GetMuluTree)---");
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			if(stub != null)
 				stub.cleanup();
 			System.out.println("GetXml(GetMuluTree)---");
 			return "success";	
 		}finally{
 			if(stub != null){
 				stub.cleanup();	
 				stub._getServiceClient().cleanup();
 				stub._getServiceClient().cleanupTransport();
 			}
 		}
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	public String GetMuluInfoHtml() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String fntree = (String)request.getSession().getAttribute("fntree");
 		fntree = new String(fntree.getBytes("iso-8859-1"),"gbk");
 		
 		String articleId = request.getParameter("articleId");
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setMessage(lawFLDAO.GetMuluInfoHtml(articleId,sn,fntree,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 	
 	
 
 	public String GetMuluInfoHtml1() throws Exception {
 		LawFLDAO lawFLDAO = DAOFactory.getLawFL();
 		HttpServletRequest request= (HttpServletRequest)ActionContext.getContext().get(ServletActionContext.HTTP_REQUEST);
 		//User user = (User)request.getSession().getAttribute("app_user");
 		
 		String navsp = (String)request.getSession().getAttribute("navsp");
 		navsp = new String(navsp.getBytes("iso-8859-1"),"gbk");
 		
 		String articleId = request.getParameter("articleId");
 		String sn = request.getParameter("sn");
 
 		try
 		{								
 			setMessage(lawFLDAO.GetMuluInfoHtml(articleId,sn,navsp,request));
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 			setSuccess(false);
 			setMessage("ݿʴ");
 			return "success";	
 		}		
 		setSuccess(true);
 	    return SUCCESS;  
 	}
 
 
 }
