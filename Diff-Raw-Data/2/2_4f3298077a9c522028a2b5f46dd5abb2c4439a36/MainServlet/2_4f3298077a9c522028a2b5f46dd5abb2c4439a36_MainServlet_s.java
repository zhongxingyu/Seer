 /*
 *    
 *   TODO:
 *   
 *  1 ) be asyncronous ( fetch url / memcache / mysql use Future ) *UPDATE no needed to be async at all
 *  2 ) be wise and modular ( use task queue, enable billing to handle 20M api calls ) *UPDATE no billing needed
 *  3 ) be fast use threads *UPDATE fine!
 * 
 */
 package com.main;
 
 // Utils
 import java.io.IOException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 // Servlet
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest; 
 import javax.servlet.http.HttpServletResponse;
 
 //Appengine low api
 import com.google.appengine.api.taskqueue.Queue;
 import com.google.appengine.api.taskqueue.QueueFactory;
 import com.google.appengine.api.taskqueue.TaskOptions;
 import com.google.appengine.api.taskqueue.TaskOptions.Method;
 
 // commom 
 import com.main.Sets;
 import appengine.MemCache;
 
 @SuppressWarnings("serial")
 public final class MainServlet extends HttpServlet {
   
   private static Sets sets;
   //private Vars vars;
   private MemCache cache;
   private HttpServletResponse resp;
   private Pattern clientPattern;
   private Pattern ipPattern;
   
   //private ServletContext context;
   
   public void init(ServletConfig config) throws ServletException {  
     sets          = new Sets(getServletConfig());
     cache         = new MemCache();
     clientPattern = Pattern.compile("([^\\d?$?]*)(\\d?[^$]*)$");
     ipPattern     = Pattern.compile("[.?:]");
     super.init(config);
   }
   
   // we only support get
   @SuppressWarnings("unchecked")
   public final void doGet(HttpServletRequest rq, HttpServletResponse rs) {
     
     Map<String,String[]> params = rq.getParameterMap();
     
     boolean isPing   = params.containsKey("ping"); 
     boolean isUpdate = params.containsKey("update"); 
     boolean isGet    = params.containsKey("get") || params.containsKey("bfile");  
     boolean isCaches = params.containsKey("gwcs") || params.containsKey("urlfile"); 
     boolean isHosts  = params.containsKey("showhosts") || params.containsKey("hostfile");  
 
     resp                    = rs;
     long timeStamp          = Math.round((new Date()).getTime() / 1000);
     StringBuilder content   = null;
     Queue queue             = null;
     ServletOutputStream out = null;
     
     try {
       
       content = new StringBuilder();
       out     = rs.getOutputStream();
       
       rs.setContentType("text/plain");
       rs.setCharacterEncoding("UTF-8");
       
       //get, ping, update request 
       if (isGet || isUpdate || isPing) {
         
         // check if has network and valid it
         if(!params.containsKey("net")) {
           sendError("No Network"); 
           return;
         } else {
           if(!validateNet(params.get("net")[0])) { 
             sendError("Unsupported network"); 
             return; 
           }
         }
       
         // check if has client
         if(!params.containsKey("client")) { 
           sendError("No Client"); 
           return; 
         }  
         
         String client  = "";
         String version = "";
         
         // check (old gnutella2 clients send clientName clientVersion together)
         Matcher clientMatch = clientPattern.matcher(params.get("client")[0]);
         // assert search
         if(clientMatch.find()) {
           if(clientMatch.groupCount()==2) {
             // version match
             if(clientMatch.group(2).isEmpty() && params.containsKey("version")) {
               Matcher versionMatch = clientPattern.matcher(params.get("version")[0]);
               // assert search
               if(versionMatch.find()) {
                 if(versionMatch.groupCount()==2) {
                   if(clientMatch.group(1).equals("TEST") && !versionMatch.group(1).isEmpty()) {
                     client  = clientMatch.group(1) + versionMatch.group(1);
                     version = versionMatch.group(2);
                   } else {
                     client  = clientMatch.group(1);
                     version = versionMatch.group(2);
                   }
                 }
               }
               //versionMatch = null;
             } else {
               client  = clientMatch.group(1);
               version = clientMatch.group(2);
             }
             //clientMatch = null;
           }
         }
         
         if(isPing)
           content.append(params.containsKey("getnetworks") ? doPong(true) + listNets() : doPong(false));
         else
           if(params.containsKey("getnetworks")) 
             content.append(listNets()); 
         
         String cacheContent;
 
         if (isGet || (isHosts && isCaches)) {
           cacheContent = cache.get("url_ip" + (params.containsKey("getleaves") ? "_leaves" : "") + (params.containsKey("getvendors") ? "_vendors" : "") + (params.containsKey("getuptime") ? "_uptime" : ""));
           content.append(cacheContent==null ? "i|no-url-no-hosts\n" : cacheContent);
         } else if (isCaches) {
           cacheContent = cache.get("url");
           content.append(cacheContent==null ? "i|no-url\n" : cacheContent);
         } else if (isHosts) {
           cacheContent = cache.get("ip" +  (params.containsKey("getleaves") ? "_leaves" : "") + (params.containsKey("getvendors") ? "_vendors" : "") + (params.containsKey("getuptime") ? "_uptime" : ""));
           content.append(cacheContent==null ? "i|no-hosts\n" : cacheContent);
         }
 
         if(isUpdate) {
           
           if(params.containsKey("ip")) {
             
             //if ( rq.getRemoteAddr() != vars.ip.substring( 0, vars.ip.indexOf(":") ) && !sets.debug ) {
             //  sendError( "Query IP doesn't match client IP" ); return;
             //}
             
             long[] validIp;
               
             if ((validIp = validateIp(params.get("ip")[0])) == null) {
               content.append("i|update|WARNING|Invalid Ip\n");
             }else{
               
               if (isTooEarly(validIp[0])) {
                 content.append("i|update|WARNING|Returned too soon\n");
               } else {
                 
                 long xLeaves = params.containsKey("x_leaves") ? Long.parseLong(params.get("x_leaves")[0]) : (params.containsKey("x.leaves") ? Long.parseLong(params.get("x.leaves")[0]) : 0);
                 long xMax    = params.containsKey("x_max")    ? Long.parseLong(params.get("x_max")[0])    : (params.containsKey("x.max")    ? Long.parseLong(params.get("x.max")[0])    : 0);
                 long uptime  = params.containsKey("uptime")   ? Long.parseLong(params.get("uptime")[0])   : 0;
                 
                 //POSSIBLY RAZA 2.2.5.6 BUG!
                 if (xLeaves > xMax) {
                   content.append("i|update|WARNING|Bad host\n");
                 }else{
 
                   // push host to queue
                   // host|timeStamp|ip|port|clientNick|clientVersion|hostUptime|totalLeaves|maxLeaves
                   // host|191919191|129090909|6346|RAZA|2.5.8.0|121999292|200|300
                   queue = QueueFactory.getQueue("update");
                   queue.add(TaskOptions.Builder.withMethod(Method.PULL).payload(
                     String.format(
                       "host|%d|%d|%d|%s|%s|%d|%d|%d", timeStamp, validIp[0], validIp[1], client, version, uptime, xLeaves, xMax
                     )
                   ));              
                   content.append("i|update|OK\ni|update|period|"+ sets.hostExpirationTime +"\n");
                 }
               }
             }
             
           }
           
           if (params.containsKey("url")) {
             
             String validUrl;
             
             if ((validUrl = validateUrl(params.get("url")[0])).isEmpty()) {
               content.append("i|update|WARNING|\n");
             } else {
               
               GnutellaUrlInfo info = new GnutellaUrlInfo();
               
               if(info.getUrl(validUrl, params.get("net")[0], sets.cacheVendor, sets.cacheVersion)) {
                 
                 // push cache to queue
                 // url|url (address)|cacheName|cacheVersion|clientNick|clientVersion|rank|timeStamp|urlCount|ipCount|g1|g2
                 // url|http://cache.leite.us/|GUAR|0.3|RAZA|2.5.5.3|10|123232323|40|80|false|true
                 queue = (queue==null) ? QueueFactory.getQueue("update") : queue;
                 queue.add(TaskOptions.Builder.withMethod(Method.PULL).payload(
                   String.format(
                     "url|%s|%s|%s|%s|%s|%d|%d|%d|%d|%s|%s", info.url, info.cacheName, info.cacheVersion, client, version, info.rank, timeStamp, info.urlCount, info.ipCount, info.g1, info.g2
                   )
                 ));
                 content.append("i|update|OK\ni|update|period|"+ sets.urlExpirationTime +"\n");
               } else {
                 
                 // oh man, thats bad
                 content.append("i|update|WARNING|Bad Cache\n");
               }
             }
           }
         
         }  
         
         // everything is fine
         rs.setStatus(200);
         
         if(isGet || (isHosts && isCaches) || isUpdate) {
           content.append("i|access|period|"+ sets.accessWait + "\n");
         }
         
         out.print(content.toString());
         content.setLength(0);
         return;
       }
       
     } catch (Exception ex) {
       ex.printStackTrace(System.err);
     } finally {
       content = null;
       out     = null;
       queue   = null;
       params  = null;
     }
   }
   
   // pong
   private final String doPong(boolean withNets){
     return "i|pong|" + sets.cacheName + " " + sets.cacheVersion + ((withNets)? '|'+ join(sets.supportedNetworks, "-") : "") + "\n";
   }
         
   // list supported networks
   private final String listNets(){
     return "i|networks|" + join(sets.supportedNetworks, "|") + "\ni|nets|"+ join(sets.supportedNetworks, "-") +"\n";
   }  
   
   // validate ip range and port
   private final long[] validateIp(String host) {
     try {
     
       if(host == null || host == "")
         return null;
       
       String[] ret = ipPattern.split(host);
       
       if (ret==null || ret.length!=5)
         return null;
       
       long ip, e, a, b, c, d;
       
       a = Long.parseLong(ret[0]);
       b = Long.parseLong(ret[1]);
       c = Long.parseLong(ret[2]);
       d = Long.parseLong(ret[3]);
       e = Long.parseLong(ret[4]);
       
       if (a>255 || b>255 || c>255 || d>255 || e>65555) 
         return null;
       
       ip = (16777216 * a) + (65536 * b) + (256 * c) + d;
       
       if(ip > 0L && 16777215L > ip || ip > 167772160L && 184549375L > ip || ip > 2130706432L && 2147483647L > ip || ip > 2851995648L && 2852061183L > ip ||
       ip > 2886729728L && 2887778303L > ip || ip > 3221225984L && 3221226239L > ip || ip > 3227017984L && 3227018239L > ip ||
       ip > 3232235520L && 3232301055L > ip || ip > 3323068416L && 3323199487L > ip || ip > 3325256704L && 3325256959L > ip ||
       ip > 3405803776L && 3405804031L > ip || ip > 3758096384L && 4026531839L > ip || ip > 4026531840L && 4294967295L > ip)
         return null;
       
       long[] valid = { ip, e };
       
       return valid;
           
     } catch (Exception ex) {
       return null;
     }
   }
   
   //validate Url
   private final String validateUrl(String url) {
     
     if(url.isEmpty()) { return ""; }
     
     String pattern = "\\b(http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
     if(!url.matches(pattern)) { return ""; }
     
     Iterator<String> it = sets.urlBlacklist.iterator();
     while (it.hasNext()) { 
       if(url.indexOf(it.next()) > -1)
         return ""; 
     } 
     
     return url
       .toLowerCase()
       .replaceAll("\\/(default|index)\\.(aspx|php|cgi|cfm|asp|pl|lp|jsp|js)\\/?$", "")
      .replaceAll("\\.(aspx|php|cgi|cfm|asp|pl|lp|jsp|js)\\/?$", ".$2");
   }
   
   // access control to avoid ddos
   private final boolean isTooEarly(Long ip){
     
     if (cache.increaseKey(ip, sets.accessWait) > 2) { return true; }
     return false;
   }
   
   // send error
   private final void sendError(String message) { 
     
     System.out.println("sendError: "+ message);
     
     try {
       
       this.resp.setStatus(200);
       this.resp.getOutputStream().print(message);
     } catch (IOException ex) {
       ex.printStackTrace(System.err);
     }
   }
     
   // validate network against supported networks
   private final boolean validateNet(String net) {
     
     if(net == null) 
       return false;
       
     if(sets.supportedNetworks.contains(net.toLowerCase())) 
       return true;
     
     return false;
   }
   
   //
   /*
   private final void checkClientAndVersion() {
     // client match
     Matcher clientMatch = clientPattern.matcher(vars.client);
     // assert search
     if(!clientMatch.find())         { return; }
     if(clientMatch.groupCount()!=2) { return; }
     // version match
     if(clientMatch.group(2).isEmpty() && vars.version!=null) {
       Matcher versionMatch = clientPattern.matcher(vars.version);
       // assert search
       if(!versionMatch.find())         { return; }
       if(versionMatch.groupCount()!=2) { return; }
       if(clientMatch.group(1).equals("TEST") && !versionMatch.group(1).isEmpty()){
         vars.client  = clientMatch.group(1) + versionMatch.group(1);
         vars.version = versionMatch.group(2);
       } else {
         vars.client  = clientMatch.group(1);
         vars.version = versionMatch.group(2);
       }
       versionMatch = null;
     } else {
       vars.client  = clientMatch.group(1);
       vars.version = clientMatch.group(2);
     }
     clientMatch = null;
   }
   */
   
   private final static String join(Set<String> s, String delimiter) {
     if (s == null || s.isEmpty()) return "";
     Iterator<String> iter = s.iterator();
     StringBuilder builder = new StringBuilder(iter.next());
     while( iter.hasNext() ) {
       builder.append(delimiter).append(iter.next());
     }
     return builder.toString();
   }
   
   // 405 will be precise but 404 is more interesting here
   public final void doPost(HttpServletRequest req, HttpServletResponse resp) { resp.setStatus(404); }
   
   // 405 will be precise but 404 is more interesting here
   public final void doHead(HttpServletRequest req, HttpServletResponse resp) { resp.setStatus(404); }
   
 }
