 package uurss;
 
 import java.io.*;
 import java.sql.*;
 import java.text.*;
 import java.util.*;
 import java.util.Map.Entry;
 import java.util.Date;
 import java.util.concurrent.*;
 
 import org.apache.log4j.*;
 import org.apache.velocity.*;
 import org.apache.velocity.app.*;
 
 import com.sun.syndication.feed.synd.*;
 import com.sun.syndication.io.*;
 
 /**
  * uuRSS main.
  */
 public final class Main {
 
     private static final Logger log = Logger.getLogger(Main.class);
 
     private static final String NAME = "uuRSS";
     private static final String VERSION = getResourceAsString("version");
     private static final String NAMEWITHVERSION = String.format("%s(%s)", NAME, VERSION);
 
     private Main() {
         // empty
     }
 
     private static List<FeedInfo> getFeedInfos(String[] categories, File cachedir) throws Exception {
         List<FeedInfo> a = new ArrayList<FeedInfo>();
         FeedListDAO dao = (System.getProperties().containsKey("csv"))
                 ? new FeedListCsvDAO()
                 : new FeedListJdbcDAO();
         try {
             for (final String category : categories) {
                 a.addAll(dao.select(category));
             }
         } finally {
             dao.close();
         }
         ExecutorService exe = Executors.newFixedThreadPool(8);
         Map<FeedInfo, Future<?>> futureMap = new LinkedHashMap<FeedInfo, Future<?>>();
         for (final FeedInfo info : a) {
             Future<File> future = exe.submit(new DownloadTask(info, cachedir));
             futureMap.put(info, future);
         }
         exe.shutdown();
         // TODO go next asap
         while (!exe.awaitTermination(1, TimeUnit.SECONDS)) {
             int taskCount = 0;
             for (Entry<FeedInfo, Future<?>> entry : futureMap.entrySet()) {
                 FeedInfo info = entry.getKey();
                 if (!entry.getValue().isDone()) {
                     ++taskCount;
                     if (log.isDebugEnabled()) {
                         log.debug(String.format("waiting %s(%s) ...", info.getName(), info.getUrl()));
                     }
                 }
             }
             if (log.isInfoEnabled()) {
                 log.info("waiting tasks = " + taskCount);
             }
         }
         return a;
     }
 
     private static Summary getSummary(List<FeedInfo> a) {
         Summary summary = new Summary();
         DateFormat df = new SimpleDateFormat("yyyyMMdd");
         SyndFeedInput input = new SyndFeedInput();
         for (final FeedInfo info : a) {
             try {
                 Reader reader = new XmlReader(info.getFile());
                 try {
                     if (log.isDebugEnabled()) {
                         log.debug("parsing " + info.name);
                     }
                     SyndFeed feed = input.build(reader);
                     @SuppressWarnings("unchecked")
                     final List<SyndEntry> entries = feed.getEntries();
                     for (SyndEntry entry : entries) {
                         Date updated = (entry.getUpdatedDate() == null)
                                 ? entry.getPublishedDate()
                                 : entry.getUpdatedDate();
                         final int key = Integer.parseInt(df.format(updated));
                         if (log.isTraceEnabled()) {
                             log.trace("entry=" + entry);
                         }
                         ListMap<FeedInfo, SyndEntry> m = summary.get(key);
                         if (m == null) {
                             m = new ListMap<FeedInfo, SyndEntry>();
                             summary.put(key, m);
                         }
                         m.add(info, entry);
                     }
                 } finally {
                     reader.close();
                 }
             } catch (Exception ex) {
                log.warn(String.format("%s at %s", ex, info));
                 if (log.isDebugEnabled()) {
                     log.debug("", ex);
                 }
             }
         }
         return summary;
     }
 
     private static Summary extractByCategory(Summary summary, String category) {
         Summary summary1 = new Summary();
         for (Entry<Integer, ListMap<FeedInfo, SyndEntry>> entry : summary.entrySet()) {
             ListMap<FeedInfo, SyndEntry> v = entry.getValue();
             List<FeedInfo> includes = new ArrayList<FeedInfo>();
             for (FeedInfo info : v.keySet()) {
                 if (info.category.equals(category)) {
                     includes.add(info);
                 }
             }
             if (!includes.isEmpty()) {
                 ListMap<FeedInfo, SyndEntry> a = new ListMap<FeedInfo, SyndEntry>();
                 for (FeedInfo info : includes) {
                     a.put(info, v.get(info));
                 }
                 summary1.put(entry.getKey(), a);
             }
         }
         return summary1;
     }
 
     private static Properties getVelocityProperties() throws IOException {
         Properties velocityProperties = new Properties();
         InputStream is = Main.class.getResourceAsStream("velocity.properties");
         try {
             velocityProperties.load(is);
         } finally {
             is.close();
         }
         return velocityProperties;
     }
 
     private static void generatePage(VelocityEngine velocity,
                                      VelocityContext context,
                                      String templateId,
                                      File file) throws Exception {
         final String templatePath = "uurss/" + templateId + ".vm";
         PrintWriter out = new PrintWriter(file);
         try {
             velocity.mergeTemplate(templatePath, context, out);
         } finally {
             out.close();
         }
     }
 
     private static String getResourceAsString(String name) {
         StringBuilder buffer = new StringBuilder();
         try {
             InputStream is = Main.class.getResourceAsStream(name);
             try {
                 while (true) {
                     final int b = is.read();
                     if (b == -1) {
                         break;
                     }
                     buffer.append((char)b);
                 }
             } finally {
                 is.close();
             }
         } catch (IOException ex) {
             throw new RuntimeException(ex);
         }
         return buffer.toString();
     }
 
     private static void mkdirs(File dir) {
         if (dir.exists() && !dir.isDirectory()) {
             throw new IllegalStateException("not a directory: " + dir);
         }
         if (!dir.exists()) {
             dir.mkdirs();
         }
         if (!dir.exists() || !dir.isDirectory()) {
             throw new IllegalStateException("failed to make directory: " + dir);
         }
     }
 
     /**
      * @param args categories
      */
     public static void main(String[] args) {
         if (args.length == 0) {
             System.err.println("usage: run category1 [category2 ...]");
             return;
         }
         if (log.isInfoEnabled()) {
             log.info(NAMEWITHVERSION + " START");
         }
         int status;
         try {
             final File root = new File(System.getProperty("result.dir", "./"));
             final File cachedir = new File(root, ".cache");
             mkdirs(cachedir);
             if (log.isInfoEnabled()) {
                 log.info("get feed infos");
             }
             List<FeedInfo> a = getFeedInfos(args, cachedir);
             if (log.isInfoEnabled()) {
                 log.info("edit feed into summary");
             }
             Summary summary = getSummary(a);
             if (log.isInfoEnabled()) {
                 log.info("create result end");
             }
             // generate pages
             VelocityEngine velocity = new VelocityEngine(getVelocityProperties());
             for (final String category : args) {
                 File dir = new File(root, category);
                 mkdirs(dir);
                 Summary summary1 = extractByCategory(summary, category);
                 List<Integer> days = new ArrayList<Integer>(summary1.keySet());
                 // generate index page
                 if (log.isInfoEnabled()) {
                     log.info("index start: " + category);
                 }
                 try {
                     VelocityContext context = new VelocityContext();
                     context.put("title", String.format("%s [%s] index", NAME, category));
                     context.put("version", NAMEWITHVERSION);
                     Collections.reverse(days);
                     context.put("summary", summary1);
                     context.put("days", days);
                     generatePage(velocity, context, "index", new File(dir, "index.html"));
                 } catch (Exception ex) {
                     log.warn("failed to generate index of " + category, ex);
                 }
                 // generates dairy page 
                 if (log.isInfoEnabled()) {
                     log.info("day start");
                 }
                 for (Integer day : summary1.keySet()) {
                     ListMap<FeedInfo, SyndEntry> m = summary1.get(day);
                     try {
                         VelocityContext context = new VelocityContext();
                         context.put("title", String.format("%s [%s] %s", NAME, category, day));
                         context.put("version", NAMEWITHVERSION);
                         context.put("results", m);
                         context.put("day", day);
                         context.put("daylist", new DayList(days, day));
                         generatePage(velocity, context, "day", new File(dir, "" + day + ".html"));
                     } catch (Exception ex) {
                         final String message = "failed to generate day page (%s, %s)";
                         log.warn(String.format(message, category, day), ex);
                     }
                 }
             }
             status = 0;
         } catch (SQLException ex) {
             log.error("", ex);
             status = 1;
         } catch (RuntimeException ex) {
             log.error("", ex);
             status = 1;
         } catch (Throwable th) {
             log.fatal("", th);
             status = 255;
         }
         if (log.isInfoEnabled()) {
             log.info(NAMEWITHVERSION + " END");
         }
         if (status != 0) {
             System.exit(status);
         }
     }
 
 }
