 package com.weibogrep.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import net.paoding.analysis.examples.gettingstarted.BoldFormatter;
 import net.paoding.analysis.analyzer.PaodingAnalyzer;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.TermPositionVector;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleFragmenter;
 import org.apache.lucene.search.highlight.TokenSources;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 import com.weibogrep.indexer.*;
 import com.weibogrep.user.*;
import com.weibogrep.util.ZLog;
 
 import weibo4j.org.json.*;
 
 public class QueryServlet extends HttpServlet {
     private static final long serialVersionUID = 1L;
        
     public QueryServlet() {
         super();
     }
 
     public void init(ServletConfig config) throws ServletException {
         // TODO Auto-generated method stub
     }
 
     protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         doPost(request, response);
     }
 
     protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
         HttpSession session = request.getSession(true);
         
         response.setContentType("application/json");
         response.setCharacterEncoding("UTF-8");
 
         try {
             UserMgmt um = (UserMgmt) session.getAttribute("user");
             if (um == null) {
                 new JSONObject().put("error" , -1)
                                 .put("errmsg", "user not logged in")
                                 .write(response.getWriter());
                 return;
             }
 
             String queryString = request.getParameter("q");
             if (queryString == null) {
                 new JSONObject().put("error" , -2)
                                 .put("errmsg", "query string not provided")
                                 .write(response.getWriter());
                 return;
             }
             
             try {
                 if (session.getAttribute("reader") == null) {
                     session.setAttribute("reader"
                                         ,IndexReader.open(
                                              FSDirectory.open(um.getIndexDir())
                                          )
                     );
                 }
 
                 if (session.getAttribute("greper") == null) {
                     session.setAttribute("greper"
                                         ,new IndexSearcher(
                                             FSDirectory.open(um.getIndexDir())
                                         )
                     );
                 }
 
                 if (session.getAttribute("parser") == null) {
                     session.setAttribute("parser"
                                         ,new QueryParser(Version.LUCENE_CURRENT
                                                         ,"content"
                                                         , new PaodingAnalyzer()
                                                         )
                     );
                 }
             } catch (Exception e) {
                 new JSONObject().put("error",  -3)
                                 .put("errmsg", "internal error, cannot create greper: " + e.getMessage())
                                 .write(response.getWriter());
                 return;
             }
             
             try {
                 String[] rs = new Greper(queryString, um.getIndexDir())
                                         .grep((QueryParser  )session.getAttribute("parser")
                                              ,(IndexReader  )session.getAttribute("reader")
                                              ,(IndexSearcher)session.getAttribute("greper")
                                              );
                 new JSONObject().put("error", 0)
                 				.put("result", rs)
                 				.write(response.getWriter());
                ZLog.info("user: " + um.getId() + " query: " + queryString);
             } catch (Exception e){
                 new JSONObject().put("error",  -4)
                                 .put("errmsg", "internal error, grep error: " + e.getMessage())
                                 .write(response.getWriter());
                 return;
             }
         } catch (JSONException e) {
             e.printStackTrace();
             response.getWriter().print("{error:-100, errmsg: json error}");
         }
 
         response.getWriter().flush();
     }
 
 }
