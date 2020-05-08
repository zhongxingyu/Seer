 /**
  * @author Yi Huang (Celia)
  */
 package benchmark.storage.table;
 
 import benchmark.storage.PMF;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 
 public class InitServlet extends HttpServlet {
     private static final Logger log = Logger.getLogger(InitServlet.class.getName());
     private static final MemcacheService memcache;
 
     static {
         memcache = MemcacheServiceFactory.getMemcacheService("table");
     }
 
     /** 
      * Handles the HTTP <code>GET</code> method.
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         int max = Integer.parseInt(request.getParameter("max"));
         int num = Integer.parseInt(request.getParameter("num"));
         int size = Integer.parseInt(request.getParameter("size"));
         int seed = Integer.parseInt(request.getParameter("seed"));
         List<SmallData> list = new ArrayList<SmallData>(max*num);
         for(int i=0; i<max; i++) {
             String str = getRandomString(seed+i, size);
             log.info(str);
             for(int j=0; j<num; j++) {
                 list.add(new SmallData(str));
             }
         }
         PersistenceManager pm = PMF.getManager();
         try {
             pm.makePersistentAll(list);
         } finally {
             pm.close();
         }
         log.log(Level.INFO, "init max={0}, size={1}, seed={2}, num={3}", new Object[] {
             max, size, seed, num
         });
     }
 
     public static String getRandomString(int seed, int size) {
         String key = "table".concat(Integer.toString(size));
         String tmp = null;
         if(memcache.contains(key))
             tmp = (String) memcache.get(key);
         if(tmp == null || tmp.length() != size) {
             StringBuilder sb = new StringBuilder();
            sb.append(seed);
             while(sb.length() < size) {
                 sb.append('#');
             }
             tmp = sb.toString();
             memcache.put(key, tmp);
         }
         return Integer.toString(seed).concat(tmp).substring(0, size);
     }
 
     /** 
      * Returns a short description of the servlet.
      * @return a String containing servlet description
      */
     @Override
     public String getServletInfo() {
         return "GAEBenchmark Table Init";
     }
 }
