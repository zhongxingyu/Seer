 import edu.uci.ics.crawler4j.crawler.CrawlConfig;
 import edu.uci.ics.crawler4j.crawler.CrawlController;
 import edu.uci.ics.crawler4j.fetcher.PageFetcher;
 import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
 import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
 import org.apache.log4j.Logger;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class CrawlerCore implements WServerHandler
 {
     static final private Logger logger = Logger.getLogger(CrawlerCore.class);
 
     private String storageFolder;
     private Integer numberOfThreads;
     private MetaInfo meta;
     private PageFetcher pageFetcher;
     private CrawlController controller;
     private WServer wserver;
     CrawlerCore.State state = CrawlerCore.State.S_NODATA;
 
     static enum State
     {
         S_NODATA,
         S_CANRUN,
         S_RUNNING,
         S_QUIT
     }
 
     public CrawlerCore(String storageFolder, Integer numberOfThreads)
     {
         this.storageFolder = storageFolder;
         this.numberOfThreads = numberOfThreads;
     }
 
     private static String getHtmlHistogram(List<Integer> values, int width, int height, int colCount, String xLegend)
     {
         StringBuilder sb = new StringBuilder();
 
         Collections.sort(values);
         int tileSize = width / colCount;
 
         int maxStat = 0;
         int[] stat = new int[colCount];
         int window = (values.get(values.size() - 1) + stat.length - 2) / (stat.length - 1);
         for (Integer v : values) {
             int newValue = stat[(v / window)] += 1;
             if (newValue > maxStat) {
                 maxStat = newValue;
             }
         }
        sb.append("<table cellspacing=\"0\" cellpadding=\"0\" style=\"padding:10px;\"><tr><td>");

        sb.append(String.format("<div style=\"text-align:right;height:%dpx;padding-right:5px;border-right:1px solid black;\">", height));
         sb.append(String.format("<div style=\"position:relative;left:0;top:0px;\">%d</div>", maxStat));
         sb.append(String.format("<div style=\"position:relative;left:0;top:%dpx;\">%d</div>", (height - 100) / 2, maxStat / 2));
         sb.append(String.format("<div style=\"position:relative;left:0;top:%dpx;\">0</div>", height - 100));
        sb.append("</div></td><td>");
 
        sb.append("<table align=\"left\" cellspacing=\"0px\" style=\"margin-bottom:30px;\"><tr>");
         for (int v : stat) {
             sb.append("<td style=\"vertical-align:bottom;border-bottom:1px solid black;\">");
             sb.append(String.format("<div style=\"background-color:blue;display:block;height:%dpx;width:%dpx\"></td>", v * (height - 50) / maxStat, tileSize));
         }
         sb.append("</tr><tr style=\"font-size:11px;\">");
         for (int i = 0; i < stat.length; i++) {
             sb.append(String.format("<td style=\"vertical-align:top;\"><div style=\"position:relative;top:%dpx;width:%dpx;writing-mode:tb-rl;-webkit-transform:rotate(90deg);-moz-transform:rotate(90deg);\">", (tileSize - 10) / 2 + 3, tileSize));
             sb.append((i + 1) * window);
             sb.append("</div></td>");
         }
        sb.append("</tr></table></td></tr><tr><td colspan=\"2\"><div style=\"text-align:center\"><b>");
         sb.append(xLegend);
        sb.append("</b></div></td></tr></table>");
 
         return sb.toString();
     }
 
     private static Map<Integer, Double> calculatePageRank(HashMap<Integer, PageLinkData> pages, int iterCount) {
         double delta = 0.75D;
         HashMap<Integer, Double> pr = new HashMap<>();
         for (Integer i : pages.keySet()) pr.put(i, 1.);
         double sumBad;
         HashMap<Integer, Double> newPR;
         for (int iters = 0; iters < iterCount; iters++) {
             newPR = new HashMap<>();
 
             sumBad = 0.0D;
             for (PageLinkData p : pages.values())
                 if (p.links.size() == 0) {
                     sumBad += pr.get(p.docid);
                 } else {
                     double addPr = pr.get(p.docid) / p.links.size();
                     for (Integer to : p.links) {
                         Double oldValue = newPR.get(to);
                         if (oldValue == null) oldValue = 0.;
                         newPR.put(to, oldValue + addPr);
                     }
                 }
             sumBad /= pages.size();
             for (Integer i : pages.keySet()) {
                 double r = (newPR.get(i) + sumBad) * delta + 1. - delta;
                 pr.put(i, r);
             }
         }
         return pr;
     }
 
     public String getResponse(String u) {
         String[] parts = u.split("\\?");
         String path = parts[0];
         Map<String, String> params = new HashMap<>();
         if (parts.length > 1) {
             for (String pair : parts[1].split("&"))
                 try {
                     params.put(URLDecoder.decode(pair.split("=")[0], "utf8"), URLDecoder.decode(pair.split("=")[1], "utf8"));
                 }
                 catch (UnsupportedEncodingException ignored) {}
         }
         switch (path) {
             case "start" :
                 logger.debug("Request: start");
                 startCrawler();
                 break;
             case "exit" :
                 logger.debug("Request: exit");
                 exit();
                 break;
             case "add" :
                 String url = params.get("url");
                 if (url != null) {
                     logger.debug("Request: add seed " + url);
                     addUrl(url);
                 }
                 break;
         }
 
         if (this.meta == null) return "<html><body><head><title>Qrawler control panel</title></head><center><h1><a style=\"color:black\" href=\"/\">Qrawler Control Panel</a></h1><br>Wait a second...</center></body></html>";
         MetaStats stats = this.meta.getStats();
 
         StringBuilder sb = new StringBuilder();
         sb.append("<html><body><head><title>Qrawler control panel</title></head><center><h1><a style=\"color:black\" href=\"/\">Qrawler Control Panel</a></h1><table width=\"800px\" cellpadding=\"2\">");
 
         sb.append("<tr><td width=\"50%\" style=\"text-align:right;vertical-align:top\">Pages downloaded:</td><td>");
         sb.append(stats.pageCount);
         sb.append("</td></tr>");
 
         if ((path.equals("psizehist")) && (stats.docSizeStat.size() > 2)) {
             int cols = 50;
             if (params.containsKey("cols")) {
                 try {
                     cols = Integer.parseInt(params.get("cols")); } catch (NumberFormatException ignored) {
                 }
                 if (cols < 3) cols = 3;
                 if (cols > 100) cols = 100;
             }
 
             sb.append("<tr><td colspan=\"2\" align=\"center\">");
             sb.append(getHtmlHistogram(stats.docSizeStat, 500, 500, cols, "Size of page, bytes"));
             sb.append("</td></tr>");
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Back</a></td></tr>");
         }
         else if ((path.equals("depthhist")) && (stats.depthStat.size() > 2)) {
             int cols = 10;
             if (params.containsKey("cols")) {
                 try {
                     cols = Integer.parseInt(params.get("cols")); } catch (NumberFormatException ignored) {
                 }
                 if (cols < 3) cols = 3;
                 if (cols > 100) cols = 100;
             }
 
             sb.append("<tr><td colspan=\"2\" align=\"center\">");
             sb.append(getHtmlHistogram(stats.depthStat, 500, 500, cols, "Depth of page"));
             sb.append("</td></tr>");
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Back</a></td></tr>");
         }
         else if ((path.equals("outdeghist")) && (stats.pageCount > 2)) {
             int cols = 10;
             if (params.containsKey("cols")) {
                 try {
                     cols = Integer.parseInt(params.get("cols")); } catch (NumberFormatException ignored) {
                 }
                 if (cols < 3) cols = 3;
                 if (cols > 100) cols = 100;
             }
 
             List<Integer> values = new ArrayList<>();
             for (PageLinkData pld : this.meta.getClosedLinks().values()) {
                 values.add(pld.links.size());
             }
 
             sb.append("<tr><td colspan=\"2\" align=\"center\">");
             sb.append(getHtmlHistogram(values, 500, 500, cols, "Depth of page"));
             sb.append("</td></tr>");
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Back</a></td></tr>");
         }
         else if ((path.equals("indeghist")) && (stats.pageCount > 2)) {
             int cols = 10;
             if (params.containsKey("cols")) {
                 try {
                     cols = Integer.parseInt(params.get("cols")); } catch (NumberFormatException ignored) {
                 }
                 if (cols < 3) cols = 3;
                 if (cols > 100) cols = 100;
             }
 
             HashMap<Integer, Integer> inDeg = new HashMap<>();
             for (PageLinkData pld : this.meta.getClosedLinks().values()) {
                 for (Integer i : pld.links) {
                     Integer old = inDeg.get(i);
                     inDeg.put(i, (old == null ? 0 : old) + 1);
                 }
             }
 
             sb.append("<tr><td colspan=\"2\" align=\"center\">");
             sb.append(getHtmlHistogram(new ArrayList<>(inDeg.values()), 500, 500, cols, "Depth of page"));
             sb.append("</td></tr>");
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Back</a></td></tr>");
         }
         else if ((path.equals("pagerankhist")) && (stats.pageCount > 2)) {
             int cols = 10;
             if (params.containsKey("cols")) {
                 try {
                     cols = Integer.parseInt(params.get("cols")); } catch (NumberFormatException ignored) {
                 }
                 if (cols < 3) cols = 3;
                 if (cols > 100) cols = 100;
             }
             int iterCount = 50;
             if (params.containsKey("iters")) {
                 try {
                     iterCount = Integer.parseInt(params.get("iters")); } catch (NumberFormatException ignored) {
                 }
                 if (iterCount < 1) iterCount = 1;
             }
 
             List<Integer> values = new ArrayList<>();
             Map<Integer, Double> pageRank = calculatePageRank(this.meta.getClosedLinks(), iterCount);
             for (Double entry : pageRank.values()) {
                 values.add((int)Math.round(entry));
             }
 
             sb.append("<tr><td colspan=\"2\" align=\"center\">");
             sb.append(getHtmlHistogram(values, 500, 500, cols, "PageRank of page"));
             sb.append("</td></tr>");
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Back</a></td></tr>");
         }
         else if ((path.equals("pageranktop")) && (stats.pageCount > 2)) {
             int count = 10;
             if (params.containsKey("count")) {
                 try {
                     count = Integer.parseInt(params.get("count")); } catch (NumberFormatException ignored) {
                 }
                 if (count < 1) count = 1;
                 if (count > 1000) count = 1000;
             }
             int iterCount = 50;
             if (params.containsKey("iters")) {
                 try {
                     iterCount = Integer.parseInt(params.get("iters")); } catch (NumberFormatException ignored) {
                 }
                 if (iterCount < 1) iterCount = 1;
             }
 
             Map<Integer, Double> pageRank = calculatePageRank(this.meta.getClosedLinks(), iterCount);
             ArrayList<Map.Entry<Integer, Double>> ranks = new ArrayList<>(pageRank.entrySet());
             Collections.sort(ranks, new Comparator<Map.Entry<Integer, Double>>()
             {
                 public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                     return -Double.compare(o1.getValue(), o2.getValue());
                 }
             });
             if (count > ranks.size()) count = ranks.size();
 
             for (int i = 0; i < count; i++) {
                 sb.append(String.format("<tr><td width=\"50%%\" style=\"text-align:right;\">%f</td><td>%s</td></tr>", ranks.get(i).getValue(), meta.pages.get(ranks.get(i).getKey()).url));
             }
             if (count < stats.pageCount)
                 sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\">...</td></tr>");
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Back</a></td></tr>");
         } else {
             sb.append("<tr><td width=\"50%\" style=\"text-align:right;vertical-align:top\">Last ten:</td><td>");
 
             if (stats.pageCount > 10) sb.append("...");
             for (int i = 0; (i < 10) && (i < stats.pageCount); i++) {
                 sb.append("<br>");
                 sb.append(stats.lastTen[((stats.lastTenI + i) % 10)]);
             }
 
             sb.append("</td></tr>");
             if (stats.pageCount > 2) {
                 sb.append("<tr><td colspan=\"2\"><form action=\"/psizehist\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;\"><input type=\"submit\" value=\"Distribution of page size\" /></td><td>columns: <input type=\"text\" name=\"cols\" value=\"50\" style=\"width:50px\" /></td></tr></table></form> </td></tr>");
                 sb.append("<tr><td colspan=\"2\"><form action=\"/depthhist\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;\"><input type=\"submit\" value=\"Distribution of page depth\" /></td><td>columns: <input type=\"text\" name=\"cols\" value=\"10\" style=\"width:50px\" /></td></tr></table></form> </td></tr>");
                 sb.append("<tr><td colspan=\"2\"><form action=\"/pageranktop\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;\"><input type=\"submit\" value=\"PageRank top\" /></td><td>count: <input type=\"text\" name=\"count\" value=\"20\" style=\"width:50px\" /></td></tr></table></form> </td></tr>");
                 sb.append("<tr><td colspan=\"2\"><form action=\"/pagerankhist\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;\"><input type=\"submit\" value=\"Distribution of PageRank\" /></td><td>columns: <input type=\"text\" name=\"cols\" value=\"50\" style=\"width:50px\" /></td></tr></table></form> </td></tr>");
                 sb.append("<tr><td colspan=\"2\"><form action=\"/indeghist\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;\"><input type=\"submit\" value=\"Distribution of input degree\" /></td><td>columns: <input type=\"text\" name=\"cols\" value=\"50\" style=\"width:50px\" /></td></tr></table></form> </td></tr>");
                 sb.append("<tr><td colspan=\"2\"><form action=\"/outdeghist\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;\"><input type=\"submit\" value=\"Distribution of output degree\" /></td><td>columns: <input type=\"text\" name=\"cols\" value=\"50\" style=\"width:50px\" /></td></tr></table></form> </td></tr>");
             }
 
             sb.append("<tr><td colspan=\"2\"><form action=\"/add\" method=\"get\"><table width=\"100%\"><tr><td width=\"50%\" style=\"text-align:right;vertical-align:top\"><input type=\"submit\" value=\"Add url\" /></td><td><input type=\"text\" name=\"url\" value=\"http://simple.wikipedia.org\" style=\"width:200px\" /></td></tr></table></form> </td></tr>");
 
             if (this.state == CrawlerCore.State.S_CANRUN) {
                 sb.append("<tr><td colspan=\"2\" align=\"center\"><form style=\"display:inline;\" action=\"/start\" method=\"get\"><input type=\"submit\" value=\"Start crawling\" /></form><form style=\"display:inline;\" action=\"/exit\" method=\"get\"><input type=\"submit\" value=\"Exit crawling\" /></form></td></tr>");
             }
             else if (this.state == CrawlerCore.State.S_RUNNING) {
                 sb.append("<tr><td colspan=\"2\" align=\"center\"><form style=\"display:inline;\" action=\"/exit\" method=\"get\"><input type=\"submit\" value=\"Exit crawling\" /></form></td></tr>");
             }
             sb.append("<tr><td colspan=\"2\" style=\"text-align:center;\"><a href=\"/\" style=\"color:black;\">Refresh</a></td></tr>");
         }
         sb.append("</table></center></body></html>");
 
         return sb.toString();
     }
 
     private void loadMeta()
     {
         this.meta = new MetaInfo(this.storageFolder + "/meta.dat");
 
         if (this.meta.stats.pageCount > 0) this.state = CrawlerCore.State.S_CANRUN;
 
         CrawlConfig config = new CrawlConfig();
         config.setCrawlStorageFolder(this.storageFolder + "/storage/");
         config.setResumableCrawling(true);
         this.pageFetcher = new PageFetcher(config);
         try
         {
             RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
             RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, this.pageFetcher);
             this.controller = new CrawlController(config, this.pageFetcher, robotstxtServer);
             CrawlerParams params = new CrawlerParams();
             params.meta = this.meta;
             params.storagePath = this.storageFolder;
             this.controller.setCustomData(params);
         } catch (Exception e) {
             e.printStackTrace();
             exit();
         }
     }
 
     private void startCrawler() {
         if (this.state != CrawlerCore.State.S_CANRUN) return;
         this.controller.startNonBlocking(SimpleWikiCrawler.class, this.numberOfThreads);
         this.state = CrawlerCore.State.S_RUNNING;
     }
 
     private synchronized void addUrl(String url) {
         if (this.controller != null) {
             this.controller.addSeed(url);
             this.state = CrawlerCore.State.S_CANRUN;
         }
     }
 
     private void unloadAll() {
         if (this.pageFetcher != null) {
             this.pageFetcher.shutDown();
         }
         if (this.controller != null) {
             this.controller.shutdown();
             this.controller.waitUntilFinish();
         }
         if (this.meta != null) {
             this.meta.Finish();
         }
         this.meta = null;
         this.controller = null;
         this.pageFetcher = null;
         this.state = CrawlerCore.State.S_QUIT;
     }
 
     private synchronized void exit() {
         if (this.wserver.isAlive()) {
             this.wserver.setStop();
             this.wserver.waitFinish();
         }
         unloadAll();
     }
 
     public void run()
     {
         this.wserver = new WServer(this, 80);
         this.wserver.start();
 
         loadMeta();
     }
 
     public static void main(String[] args) throws Exception {
         CrawlerCore core = new CrawlerCore(".data/", 10);
         core.run();
     }
 }
