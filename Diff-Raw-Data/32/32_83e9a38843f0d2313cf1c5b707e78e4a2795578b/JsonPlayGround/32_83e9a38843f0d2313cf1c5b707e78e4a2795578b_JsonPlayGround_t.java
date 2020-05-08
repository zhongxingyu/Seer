 package be.boeboe.scapsync.rest;
 
 import be.boeboe.scapsync.rest.interfaces.IScapSyncSearchResult;
 import be.boeboe.scapsync.rest.interfaces.IScapSyncSearchResultType;
 
 public class JsonPlayGround {
 
   /**
    * @param args
    */
   public static void main(String[] args) {
 
     ScapSyncSearcher searcher = new ScapSyncSearcher();
    System.out.println(searcher.getStatistics());
    
    String[] targetArray = { "explorer",
                             "openssl", "http", "nginx", "outlook",
                             "windows", "android", "iphone", "explorer",
                             "ftp", "linux", "sip", "firewall",
                             "apple", "cwmp", "dhcp", "dns" ,
                             "cisco", "openwrt", "huawei", "sagem",
                             "unix", "uhttpd", "torrent", "microsoft",
                             "sql", "mysql", "oracle", "vpn", 
                             "root", "guest", "admin", "overflow"
     };
     
     for (String target : targetArray) {
       searchTarget(searcher, target);
     }
   }
 
   /**
    * @param searcher
    * @param target
    */
   private static void searchTarget(ScapSyncSearcher searcher, String target) {
     for (IScapSyncSearchResult r : searcher.searchAll(target)) {
       if (r.getType().equals(IScapSyncSearchResultType.TYPE_CPE)) {
         System.out.println("\n=== CPE ===[ " + target + " ]=== CPE ===");
         System.out.println("RES: " + r.toString());
         // System.out.println("DETAILS: " + searcher.getCpeDetails(r).toString());
       } else if (r.getType().equals(IScapSyncSearchResultType.TYPE_CVE)) {
         System.out.println("\n=== CVE ===[ " + target + " ]=== CVE ===");
         System.out.println("RES: " + r.toString());
         //System.out.println("DETAILS: " + searcher.getCveDetails(r).toString());
       } else if (r.getType().equals(IScapSyncSearchResultType.TYPE_CWE)) {
         System.out.println("\n=== CWE ===[ " + target + " ]=== CWE ===");
         System.out.println("RES: " + r.toString());
         //System.out.println("DETAILS: " + searcher.getCweDetails(r).toString());
       }
     }
   }
 }
