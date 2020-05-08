 package chb.client;
 
 import chb.base.LoggingProxy;
 import chb.base.NewsProxy;
 import chb.bean.NewsLead;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.*;
 import java.net.URLDecoder;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class News extends AServlet {
 
     protected HttpServletRequest request = null;
     protected HttpServletResponse response = null;
 
     /*
      * Accepted parameters:
      * 1) num: -1 to show all news, any positive number to show some pieces of news.
      * 2) title: if this parameter exists, just show one single piece of news
      *    in detail.
      */
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 
         this.request = req;
         this.response = resp;
 
         /**
          * Save news files to database.
          * The news files are kept in the /news directory by default.
          */
         scanNews();
 
         /**
          * If it requests one single article, retrieve it.
          */
         req.setCharacterEncoding("UTF-8");
         String title = req.getParameter("title");
         if (title != null && title.length() > 0) {
             title = URLDecoder.decode(title, "UTF-8");
             processSingleNews(title);
         } else {
 
             /**
              * If it requests all the news returned as a list, returns it.
              */
             processAllNews();
         }
     }
 
     /**
      * Query one single piece of news according to the given news title.
      *
      * @param title a decoded string of news title
      * @throws ServletException
      * @throws IOException
      */
     protected void processSingleNews(String title) throws ServletException, IOException {
         if (title == null || title.length() < 1) {
             return;
         }
         /**
          * Set up configuration file and logging file.
          */
         String cpath = getInitParameterPath("path.datasource.config");
         NewsProxy proxy = new NewsProxy(cpath);
         String epath = getInitParameterPath("path.news.error.log");
         proxy.setErrorLogPath(epath);
 
         NewsLead lead = proxy.querySingleNews(title);
         if (lead == null) {
             LoggingProxy logger = new LoggingProxy(epath);
             logger.log(LoggingProxy.ERROR, "[---]Can't query news leads. Return null.");
             /* Create an instance as attribute. */
             lead = new NewsLead();
         }
 
         request.setAttribute("newsLead", lead);
         RequestDispatcher dispatcher = request.getRequestDispatcher("/news_lead.jsp");
         dispatcher.forward(request, response);
 
         return;
     }
 
 
     /**
      * Query all the news and encapsulate them into a sorted list.
      *
      * @throws ServletException
      * @throws IOException
      */
     protected void processAllNews() throws ServletException, IOException {
         /**
          * num as parameter.
          * If num is -1, show all the news, and if num is 0 or above, show
          * num number of news.
          * In the second case, a '更多' link will be displayed at the bottom.
          */
         String news_num = request.getParameter("num");
         int num = -1;
         if (news_num != null && news_num.length() > 0) {
             num = Integer.valueOf(news_num);
         }
         /**
          * Set up configuration file and logging file.
          */
         String cpath = getInitParameterPath("path.datasource.config");
         NewsProxy proxy = new NewsProxy(cpath);
         String epath = getInitParameterPath("path.news.error.log");
         proxy.setErrorLogPath(epath);
 
         List<NewsLead> list = proxy.queryALLNews();
 
         if (list == null) {
             LoggingProxy logger = new LoggingProxy(epath);
             logger.log(LoggingProxy.ERROR, "[---]Can't query news leads. Return null.");
 
             list = new ArrayList<NewsLead>();
         }
 
         /**
          * Limit the number of the returned news leads.
          */
         if (num >= 0) {
             if(num > list.size()) {
                 num = list.size();
             }
             list = list.subList(0, num);
         }
 
         /**
          * Sort the NewsLead by their updateTime.
          */
         Collections.sort(list, new ComparatorNewsByUpdateTime());
 
         request.setAttribute("newsList", list);
         request.setAttribute("num", String.valueOf(num));
         RequestDispatcher dispatcher = request.getRequestDispatcher("/news_list.jsp");
         dispatcher.forward(request, response);
 
         return;
     }
 
     /**
      * Inner class ofr comparing two NewsLead.
      * Compare NewsLeads by updateTime.
      */
     class ComparatorNewsByUpdateTime implements Comparator {
         @Override
         public int compare(Object o1, Object o2) {
             NewsLead l1 = (NewsLead) o1;
             NewsLead l2 = (NewsLead) o2;
             return compare_date(l1.getUpdateTime(), l2.getUpdateTime());
         }
 
         @Override
         public boolean equals(Object obj) {
             return false;
         }
 
         public int compare_date(String DATE1, String DATE2) {
             DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
             try {
                 Date dt1 = df.parse(DATE1);
                 Date dt2 = df.parse(DATE2);
                 if (dt1.after(dt2)) {
                     return -1;
                 } else {
                     return 1;
                 }
             } catch (Exception exception) {
                 exception.printStackTrace();
             }
             return 1;
         }
     }
 
     protected void scanNews() {
         String news = getInitParameterPath("directory.news.content");
         File dir = new File(news);
         if (dir.exists() == false) {
             dir.mkdirs();
             return;
         }
 
         /**
          * Only process the news files with suffix '.nz'.
          */
         File[] files = dir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 if (pathname.getName().endsWith(".nz")) {
                     return true;
                 } else {
                     return false;
                 }
             }
         });
         if (files.length < 1) {
             /* Nothing to be processed. */
             return;
         }
         for (File f : files) {
             if (f.getName().endsWith(".oz")) {
                 continue;
             }
             NewsLead lead = null;
             FileInputStream fis = null;
             InputStreamReader isr = null;
             BufferedReader br = null;
 
             try {
                //TODO Implement news write-in.
                 lead = new NewsLead();
                 lead.setCreateTime(LoggingProxy.getTimeStamp());
                 lead.setUpdateTime(LoggingProxy.getTimeStamp());
 
                 fis = new FileInputStream(f);
                 isr = new InputStreamReader(fis, "UTF-8");
                 br = new BufferedReader(isr);
 
                 String line = "";
                 line = br.readLine();
                 if (line == null) {
                     throw new IOException("Unexpected empty line. (Should be title)");
                 }
                 lead.setTitle(line);
 
                 line = br.readLine();
                 if (line == null) {
                     throw new IOException("Unexpected empty line. (Should be blank line)");
                 }
                 if (line.trim().length() < 1) {
                     line = br.readLine();
                 }
                 lead.setAuthorName(line);
 
                 line = br.readLine();
                 if (line == null) {
                     throw new IOException("Unexpected empty line. (Should be telephone)");
                 }
                 lead.setAuthorTelephone(line);
 
                 line = br.readLine();
                 if (line == null) {
                     throw new IOException("Unexpected empty line. (Should be email)");
                 }
                 lead.setAuthorEmail(line);
 
                 String content = "";
                 while ((line = br.readLine()) != null) {
                     content += line + "\n";
                 }
                 /**
                  * Clean the content at it first occurrence.
                  */
                 lead.setContentClean(content);
 
                 /**
                  * Store the news to database.
                  */
                 String cpath = getInitParameterPath("path.datasource.config");
                 NewsProxy proxy = new NewsProxy(cpath);
                String epath = getInitParameterPath("path.news.error.log");
                 proxy.setErrorLogPath(epath);
 
                 if (proxy.storeSingleNews(lead) == false) {
                    LoggingProxy logger = new LoggingProxy(epath);
                     logger.log(LoggingProxy.ERROR, "[---]News error. "
                             + lead.getTitle() + " can't be stored. ("
                             + f.getAbsolutePath() + ")");
 
                 }
 
                 /**
                  * Release the resources related to a specified file so
                  * that further modification can be made.
                  */
 
                 fis.close();
                 isr.close();
                 br.close();
 
             } catch (FileNotFoundException e) {
                 /**
                  * Can't return or continue in catch clause because
                  * it needs to rename the news file after it is processed.
                  */
                 String epath2 = getInitParameterPath("path.news.error.log");
                LoggingProxy logger = new LoggingProxy(epath2);
                 logger.log(LoggingProxy.ERROR, "[---]News error. " + e.getMessage());
 
             } catch (IOException e) {
                 String epath2 = getInitParameterPath("path.news.error.log");
                LoggingProxy logger = new LoggingProxy(epath2);
                 logger.log(LoggingProxy.ERROR, "[---]News error. "
                         + e.getMessage() + "(" + f.getAbsolutePath() + ")");
             }
 
             /**
              * Rename the news file so that it won't be processed twice.
              */
             String f1n = f.getAbsolutePath();
             f1n = f1n.replace(".nz", ".oz");
             File f1 = new File(f1n);
 
             f.renameTo(f1);
         }
     }
 }
