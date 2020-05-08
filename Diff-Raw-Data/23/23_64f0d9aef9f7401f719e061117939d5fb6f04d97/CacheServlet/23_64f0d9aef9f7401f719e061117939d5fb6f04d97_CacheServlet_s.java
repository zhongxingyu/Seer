 package eionet.meta.exports;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.tee.util.Util;
 
 import eionet.meta.DDSearchEngine;
 import eionet.meta.GetPrintout;
 import eionet.meta.exports.pdf.DstPdfGuideline;
 import eionet.meta.exports.pdf.PdfHandout;
 import eionet.meta.exports.xls.DstXls;
 import eionet.meta.exports.xls.TblXls;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.sql.ConnectionUtil;
 import eionet.util.sql.DDConnectionException;
 import eionet.util.sql.INParameters;
 import eionet.util.sql.SQL;
 
 /**
  * 
  * @author Jaanus Heinlaid, e-mail: <a href="mailto:jaanus.heinlaid@tietoenator.com">jaanus.heinlaid@tietoenator.com</a>
  *
  */
 public class CacheServlet extends HttpServlet {
 	
 	public static final String KEY_ARTICLE  = "article";
 	public static final String KEY_FILENAME = "filename";
 	public static final String KEY_CREATED  = "created";
 	
 	private Hashtable objTypes = null;
 	private String cachePath = null;
 	
 	public void init() throws ServletException{
 		cachePath = Props.getProperty(PropsIF.DOC_PATH);		
 		if (!Util.nullString(cachePath)){
 			cachePath.trim();
 			if (!cachePath.endsWith(File.separator))
 				cachePath = cachePath + File.separator;
 		}
 	}
 	
 	protected void service(HttpServletRequest req, HttpServletResponse res)
 											throws ServletException, IOException {
 		
 		req.setCharacterEncoding("UTF-8");
 		
 		if (objTypes==null) setObjectTypes();
 		if (Util.nullString(cachePath))
 			throw new ServletException("Missing the path to cache directory!");
 			
 		try{
 			if (req.getMethod().equalsIgnoreCase("GET"))
 				get(req);
 			else{
 				post(req);
 				//loadEntries(req);
 			}
 			
 			dispatch(req, res);
 		}
 		catch (Exception e){
 			e.printStackTrace(System.out);
 			throw new ServletException(e.toString());
 		}
 		finally{
 			closeConnection(req);
 		}
 	}
 	
 	private void get(HttpServletRequest req) throws Exception {
 		loadEntries(req);
 		setTitleParts(req);
 	}
 
 	private void post(HttpServletRequest req) throws Exception {
 		
 		String objID = req.getParameter("obj_id");
		if (Util.nullString(objID)) throw new Exception("Missing object ID!");
 		String objType = req.getParameter("obj_type");
		if (objType==null || !objTypes.containsKey(objType))
				throw new Exception("Object type is missing or is illegal!");
 		String action = req.getParameter("action");
		if (Util.nullString(action)) throw new Exception("Missing action parameter!");
 		String[] articles = req.getParameterValues("article");
		if (articles==null || articles.length==0) throw new Exception("Missing articles!");
 		
 		ServletContext ctx = getServletContext();
 		
 		for (int i=0; i<articles.length; i++){
 			
 			String article = articles[i];
 			CachableIF cachable = null;
 			
 			if (article.equals("pdf") && objType.equals("dst")){
 				cachable = new DstPdfGuideline(getConnection(req));
 				cachable.setCachePath(cachePath);
 				String visualsPath = Props.getProperty(PropsIF.VISUALS_PATH);
 				((PdfHandout)cachable).setVsPath(visualsPath);
 				((PdfHandout)cachable).setLogo(ctx.getRealPath(GetPrintout.PDF_LOGO_PATH));
 			}
 			else if (article.equals("xls") && objType.equals("dst")){
 				cachable = new DstXls(getConnection(req));
 				cachable.setCachePath(cachePath);
 			}
 			else if (article.equals("xls") && objType.equals("tbl")){
 				cachable = new TblXls(getConnection(req));
 				cachable.setCachePath(cachePath);
 			}
 			else
 				throw new Exception("Article <" + article + "> for object <" + objType +
 															"> is not handled right now!");
 
 			if (action.equals("update"))
 				cachable.updateCache(objID);
 			else if (action.equals("clear"))
 				cachable.clearCache(objID);
 			else
 				throw new Exception("Unknown action: " + action);			
 			
 		}
 	}
 	
 	private void loadEntries(HttpServletRequest req) throws Exception{
 		
 		String objID = req.getParameter("obj_id");
 		if (Util.nullString(objID)) throw new Exception("Missing object ID!");
 		String objType = req.getParameter("obj_type");
 		if (objType==null || !objTypes.containsKey(objType))
 					throw new Exception("Object type is missing or is illegal!");
 		
 		req.removeAttribute("entries");
 		
 		Vector objArticles = getArticlesForObjType(req, objType);
 		if (objArticles==null || objArticles.size()==0) return;
 		
 		DDSearchEngine searchEngine =
 					new DDSearchEngine(getConnection(req), "", getServletContext());
 		Hashtable cache = searchEngine.getCache(objID, objType);
 		Hashtable supportedArticles = null;
 		for (int i=0; i<objArticles.size(); i++){
 			supportedArticles = (Hashtable)objArticles.get(i);
 			String article = (String)supportedArticles.get("article");
 			
 			Hashtable cachedArticle = (Hashtable)cache.get(article);
 			if (cachedArticle!=null){
 				String filename = (String)cachedArticle.get("filename");
 				Long created = (Long)cachedArticle.get("created");
 				String full = this.cachePath + filename;
 				File file = new File(full);
 				if (!file.exists() || created==null || created.longValue()==0){
 					deleteCacheEntry(objID, objType, article, getConnection(req));
 					continue;
 				}
 				else
 					supportedArticles.put("created", created);
 			}
 		}
 		
 		req.setAttribute("entries", objArticles);
 	}
 	
 	/**
 	 * 
 	 * @param objID
 	 * @param objType
 	 * @param article
 	 * @param conn
 	 * @throws SQLException
 	 */
 	protected static void deleteCacheEntry(String objID, String objType, String article, Connection conn)
 																throws SQLException{
 		
 		INParameters inParams = new INParameters();
 		
 		StringBuffer buf = new StringBuffer("delete from CACHE where OBJ_ID=").
 		append(inParams.add(objID, Types.INTEGER)).append(" and OBJ_TYPE=").append(inParams.add(objType)).
 		append(" and ARTICLE=").append(inParams.add(article));
 		
 		PreparedStatement stmt = null;
 		try{
 			SQL.preparedStatement(buf.toString(), inParams, conn).executeUpdate();
 		}
 		finally{
 			try{
 				if (stmt!=null) stmt.close();
 			}
 			catch (SQLException e){}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param req
 	 * @return
 	 */
 	private Hashtable getArticles(HttpServletRequest req){
 		
 		Hashtable articles = (Hashtable)req.getAttribute("articles");
 		if (articles==null) articles = new Hashtable();
 		
 		// set articles for dst
 		Vector v = new Vector();
 		Hashtable hash = new Hashtable();
 		hash.put("article", "pdf");
 		hash.put("text", "Technical specification");
 		hash.put("icon", "pdf.png");
 		v.add(hash);
 		
 		hash = new Hashtable();
 		hash.put("article", "xls");
 		hash.put("text", "MS Excel template");
 		hash.put("icon", "xls.png");
 		v.add(hash);
 		
 		articles.put("dst", v);
 
 		// set articles for tbl
 		v = new Vector();
 		hash = new Hashtable();
 		hash.put("article", "xls");
 		hash.put("text", "MS Excel template");
 		hash.put("icon", "xls.png");
 		v.add(hash);
 		
 		articles.put("tbl", v);
 		
 		return articles;
 	}
 	
 	private Vector getArticlesForObjType(HttpServletRequest req, String objType){
 		
 		Hashtable articles = getArticles(req);
 		return (Vector)articles.get(objType);
 	}
 
 	private void setObjectTypes(){
 		objTypes = new Hashtable();
 		objTypes.put("dst", "dataset");
 		objTypes.put("tbl", "table");
 		objTypes.put("elm", "element");
 	}
 	
 	/**
 	 * 
 	 * @param req
 	 * @return
 	 * @throws DDConnectionException
 	 * @throws SQLException
 	 */
 	private Connection getConnection(HttpServletRequest req) throws DDConnectionException, SQLException{
 		
 		Connection conn = (Connection)req.getAttribute("connection");
 		if (conn==null){
 			conn = ConnectionUtil.getConnection();
 		}
 		
 		return conn;
 	}
 	
 	private void closeConnection(HttpServletRequest req){
 		
 		Connection conn = (Connection)req.getAttribute("connection");
 		if (conn!=null){
 			try { conn.close(); } catch (SQLException e) {}
 		}
 	}
 	
 	private void dispatch(HttpServletRequest req, HttpServletResponse res)
 											throws ServletException, IOException {
 		
 		if (req.getMethod().equalsIgnoreCase("POST")){
 			StringBuffer buf = new StringBuffer("GetCache?obj_id=").
 			append(req.getParameter("obj_id")).
 			append("&obj_type=").append(req.getParameter("obj_type")).
 			append("&idf=").append(req.getParameter("idf"));
 			res.sendRedirect(buf.toString());
 		}
 		else
 			req.getRequestDispatcher("cache.jsp").forward(req, res);
 	}
 
 	private void setTitleParts(HttpServletRequest req) throws Exception{
 		
 		req.setAttribute("object_type", objTypes.get(req.getParameter("obj_type")));
 		String idf = req.getParameter("idf");
 		idf = idf==null || idf.length()==0 ? "?" : idf;
 		req.setAttribute("identifier", idf);
 	}
 	
 	private void guard(HttpServletRequest req) throws Exception{
 	}
 }
