 /* **********************************************************************
     Copyright 2009 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package org.bedework.timezones.common;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.UnfoldingReader;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.util.TimeZones;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.servlet.ServletException;
 
 import edu.rpi.sss.util.DateTimeUtil;
 import edu.rpi.sss.util.OptionsException;
 import edu.rpi.sss.util.OptionsI;
 
 /** Common code for the timezone service.
  *
  *   @author Mike Douglass
  */
 public class TzServerUtil {
   private static String appname = "tzsvr";
 
   private TzsvrConfig config;
 
   private static TzServerUtil instance;
 
   /* ======================= Error codes ======================= */
 
   /** Unable to retrieve the data */
   public static final String errorNodata = "org.tserver.no.data";
 
   /* ======================= Caching ======================= */
 
   private static CacheManager manager;
   static {
    URL u = TzServerUtil.class.getResource("/tzsvrEhcache.xml");
 
     manager = CacheManager.create(u);
   }
 
  private static Cache vtzCache;
 
   private static SortedSet<String> nameList;
 
   /** Time we last fetched the data */
   public static long lastDataFetch;
 
   static String etagValue;
 
   /* ======================= TimeZone objects ======================= */
 
   private Map<String, TimeZone> tzs = new HashMap<String, TimeZone>();
 
   private Properties aliases;
 
   private String aliasesStr;
 
   private Collection<String> tzinfo;
 
   private ZipFile tzDefsZipFile;
 
   private File tzDefsFile;
 
   private static Object zipLock = new Object();
 
   volatile boolean refreshNow;
 
   /* ======================= Stats ======================= */
 
   static long gets;
   static long cacheHits;
   static long reads;
   static long nameLists;
   static long aliasReads;
   static long conversions;
   static long conversionsMillis;
   static long tzfetches;
   static long tzbuilds;
   static long tzbuildsMillis;
 
   /**
    * @return a singleton instance
    * @throws ServletException
    */
   public static TzServerUtil getInstance() throws ServletException {
     if (instance != null) {
       return instance;
     }
 
     synchronized (zipLock) {
       if (instance != null) {
         return instance;
       }
 
       instance = new TzServerUtil();
     }
 
     return instance;
   }
 
   /** Set before calling getInstance
    *
    * @param val
    */
   public static void setAppname(final String val) {
     appname = val;
   }
 
   /**
    * @return appname
    */
   public static String getAppname() {
     return appname;
   }
 
   /**
    * @throws ServletException
    */
   private TzServerUtil() throws ServletException {
     /* Note that the options factory returns a static object and we should
      * initialise the config once only
      */
     OptionsI opts;
     try {
       opts = TzsvrOptionsFactory.getOptions(false);
       config = (TzsvrConfig)opts.getAppProperty(appname);
       if (config == null) {
         config = new TzsvrConfig();
       }
     } catch (OptionsException e) {
       throw new ServletException(e);
     }
 
     cacheInit();
   }
 
   /** Cause a refresh of the data
    */
   public void fireRefresh() {
     refreshNow = true;
   }
 
   /**
    * @return an etag based on when we refreshed data
    * @throws ServletException
    */
   public String getEtag() throws ServletException {
     if (etagValue == null) {
       Collection<String> info = tzinfo;
 
       if (info == null) {
         info = getInfo();
       }
 
       if (info != null) {
         for (String s: info) {
           if (s.startsWith("buildTime=")) {
             etagValue = s.substring("buildTime=".length());
             break;
           }
         }
       }
 
       if (etagValue == null) {
         etagValue = String.valueOf(lastDataFetch);
       }
     }
 
     StringBuilder val = new StringBuilder();
 
     val.append("\"");
     val.append(etagValue);
     val.append("\"");
 
     return val.toString();
   }
 
   /**
    * @return config
    */
   public TzsvrConfig getConfig() {
     return config;
   }
 
   /**
    * @return stats for the service
    * @throws ServletException
    */
   public static List<Stat> getStats() throws ServletException {
     List<Stat> stats = new ArrayList<Stat>();
 
     stats.add(new Stat("Gets", String.valueOf(gets)));
     stats.add(new Stat("Hits", String.valueOf(cacheHits)));
     stats.add(new Stat("Name lists", String.valueOf(nameLists)));
     stats.add(new Stat("Reads", String.valueOf(reads)));
     stats.add(new Stat("conversions",
                        String.valueOf(conversions),
                        String.valueOf(conversionsMillis)));
     stats.add(new Stat("tzfetches", String.valueOf(tzfetches)));
     stats.add(new Stat("tzbuilds",
                        String.valueOf(tzbuilds),
                        String.valueOf(tzbuildsMillis)));
 
     return stats;
   }
 
   /** Retrieve the data and store in a temp file. Return the file object.
    *
    * @return File
    * @throws ServletException
    */
   public File getdata() throws ServletException {
     try {
       String dataUrl = config.getTzdataUrl();
       if (dataUrl == null) {
         throw new ServletException("No data url defined");
       }
 
       /* Fetch the data */
       HttpClient client = new HttpClient();
 
       HttpMethod get = new GetMethod(dataUrl);
 
       client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                       new DefaultHttpMethodRetryHandler());
 
       client.executeMethod(get);
 
       InputStream is = get.getResponseBodyAsStream();
 
       File f = File.createTempFile("bwtzserver", "zip");
 
       FileOutputStream fos = new  FileOutputStream(f);
 
       byte[] buff = new byte[4096];
 
       for (;;) {
         int num = is.read(buff);
 
         if (num < 0) {
           break;
         }
 
         if (num > 0) {
           fos.write(buff, 0, num);
         }
       }
 
       fos.close();
       is.close();
 
       get.releaseConnection();
 
       logIt("Data (re)fetched");
 
       return f;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   /**
    * @return names fromthe zip file.
    * @throws ServletException
    */
   public SortedSet<String> getNames() throws ServletException {
     nameLists++;
 
     /* Do this the right way round we don't need to synch */
     SortedSet<String> nl = nameList;
 
     if (nl != null) {
       return nl;
     }
 
     try {
       nl = new TreeSet<String>();
 
       Enumeration<? extends ZipEntry> zes = tzDefsZipFile.entries();
 
       while (zes.hasMoreElements()) {
         ZipEntry ze = zes.nextElement();
 
         if (!ze.isDirectory()) {
           String n = ze.getName();
 
           if (n.startsWith("zoneinfo/") && n.endsWith(".ics")) {
             nl.add(n.substring(9, n.length() - 4));
           }
         }
       }
 
       nameList = nl;
 
       return nl;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   /**
    * @param name
    * @return spec
    * @throws ServletException
    */
   public String getTz(final String name) throws ServletException {
     String s = getCachedVtz(name);
     if (s != null) {
       cacheHits++;
       return s;
     }
 
     try {
       reads++;
       ZipEntry ze = tzDefsZipFile.getEntry("zoneinfo/" + name + ".ics");
 
       if (ze == null) {
         return null;
       }
 
       s = entryToString(ze);
 
       putCachedVtz(name, s);
 
       return s;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   /**
    * @param tzid - possible alias
    * @return actual timezone id
    * @throws ServletException
    */
   public String unalias(String tzid) throws ServletException {
     if (tzid == null) {
       throw new ServletException("Null id for unalias");
     }
 
     /* First transform the name if it follows a known pattern, for example
      * we used to get     /mozilla.org/20070129_1/America/New_York
      */
 
     tzid = transformTzid(tzid);
 
     // Allow chains of aliases
 
     String target = tzid;
 
     if (aliases == null) {
       loadAliases();
     }
 
     for (int i = 0; i < 100; i++) {   // Just in case we get a circular chain
       String unaliased = aliases.getProperty(target);
 
       if (unaliased == null) {
         return target;
       }
 
       if (unaliased.equals(tzid)) {
         break;
       }
 
       target = unaliased;
     }
 
     error("Possible circular alias chain looking for " + tzid);
 
     return null;
   }
 
   /**
    * @return aliases
    * @throws ServletException
    */
   public String getAliases() throws ServletException {
     try {
       /* Do this the right way round we don't need to synch */
       String a = aliasesStr;
 
       if (a != null) {
         return a;
       }
 
       aliasReads++;
       ZipEntry ze = tzDefsZipFile.getEntry("aliases.txt");
 
       if (ze == null) {
         return null;
       }
 
       a = entryToString(ze);
       aliasesStr = a;
 
       return a;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   /**
    * @return info
    * @throws ServletException
    */
   public Collection<String> getInfo() throws ServletException {
     try {
       /* Do this the right way round we don't need to synch */
       Collection<String> a = tzinfo;
 
       if (a != null) {
         return a;
       }
 
       ZipEntry ze = tzDefsZipFile.getEntry("info.txt");
 
       if (ze == null) {
         return null;
       }
 
       String info = entryToString(ze);
 
       String[] infoLines = info.split("\n");
       a = new ArrayList<String>();
 
       for (String s: infoLines) {
         a.add(s);
       }
 
       tzinfo = a;
 
       return a;
     } catch (Throwable t) {
       throw new ServletException(t);
     }
   }
 
   /**
    * @param time
    * @param tzid
    * @return String utc date
    * @throws Throwable
    */
   public String getUtc(final String time,
                        final String tzid) throws Throwable {
     if (DateTimeUtil.isISODateTimeUTC(time)) {
       // Already UTC
       return time;
     }
 
     if (!DateTimeUtil.isISODateTime(time)) {
       return null;  // Bad datetime
     }
 
     conversions++;
     long smillis = System.currentTimeMillis();
 
     TimeZone tz = fetchTimeZone(tzid);
 
     DateFormat formatTd  = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
     formatTd.setTimeZone(tz);
 
     Date date = formatTd.parse(time);
     String utc;
 
     synchronized (cal) {
       cal.clear();
       cal.setTime(date);
 
       //formatTd.setTimeZone(utctz);
       //trace("formatTd with utc: " + formatTd.format(date));
 
       StringBuilder sb = new StringBuilder();
       digit4(sb, cal.get(Calendar.YEAR));
       digit2(sb, cal.get(Calendar.MONTH) + 1); // Month starts at 0
       digit2(sb, cal.get(Calendar.DAY_OF_MONTH));
       sb.append('T');
       digit2(sb, cal.get(Calendar.HOUR_OF_DAY));
       digit2(sb, cal.get(Calendar.MINUTE));
       digit2(sb, cal.get(Calendar.SECOND));
       sb.append('Z');
 
       utc = sb.toString();
     }
 
     conversionsMillis += System.currentTimeMillis() - smillis;
 
     return utc;
   }
 
   /** Convert from local time in fromTzid to local time in toTzid. If dateTime is
    * already an iso utc date time fromTzid may be null.
    *
    * @param dateTime
    * @param fromTzid
    * @param toTzid
    * @return String time in given timezone
    * @throws Throwable
    */
   public String convertDateTime(final String dateTime, final String fromTzid,
                                 final String toTzid) throws Throwable {
     String UTCdt = null;
     if (DateTimeUtil.isISODateTimeUTC(dateTime)) {
       // Already UTC
       UTCdt = dateTime;
     } else if (!DateTimeUtil.isISODateTime(dateTime)) {
       return null;  // Bad datetime
     } else if (toTzid == null) {
       return null;  // Bad toTzid
     } else {
       UTCdt = getUtc(dateTime, fromTzid);
       conversions--; // avoid double inc
     }
 
     conversions++;
     long smillis = System.currentTimeMillis();
 
     // Convert to time in toTzid
 
     Date dt = DateTimeUtil.fromISODateTimeUTC(UTCdt);
 
     TimeZone tz = fetchTimeZone(toTzid);
     if (tz == null) {
       return null;
     }
 
     String cdt = DateTimeUtil.isoDateTime(dt, tz);
     conversionsMillis += System.currentTimeMillis() - smillis;
 
     return cdt;
   }
 
   /** Get a timezone object from the server given the id.
    *
    * @param tzid
    * @return TimeZone with id or null
    * @throws Throwable
    */
   public TimeZone fetchTimeZone(final String tzid) throws Throwable {
     tzfetches++;
 
     TimeZone tz = tzs.get(tzid);
     if (tz != null) {
       return tz;
     }
 
     String tzdef = getTz(tzid);
 
     if (tzdef == null) {
       return null;
     }
 
     tzbuilds++;
     long smillis = System.currentTimeMillis();
 
     CalendarBuilder cb = new CalendarBuilder();
 
     UnfoldingReader ufrdr = new UnfoldingReader(new StringReader(tzdef), true);
 
     net.fortuna.ical4j.model.Calendar cal = cb.build(ufrdr);
     VTimeZone vtz = (VTimeZone)cal.getComponents().getComponent(Component.VTIMEZONE);
     if (vtz == null) {
       throw new Exception("Incorrectly stored timezone");
     }
 
     tz = new TimeZone(vtz);
     tzs.put(tzid, tz);
 
     tzbuildsMillis += System.currentTimeMillis() - smillis;
 
     return tz;
   }
 
   /**
    * @throws ServletException
    */
   public void refresh() throws ServletException {
     synchronized (zipLock) {
       if ((tzDefsFile != null) && !refreshNow) {
         if ((System.currentTimeMillis() - TzServerUtil.lastDataFetch) / 1000 < config.getRefetchInterval()) {
           // No fetch needed
           return;
         }
       }
 
       try {
         File f = getdata();
 
         ZipFile zf = new ZipFile(f);
 
         if (tzDefsZipFile != null) {
           try {
             tzDefsZipFile.close();
           } catch (Throwable t) {
           }
         }
 
         if (tzDefsFile != null) {
           try {
             tzDefsFile.delete();
           } catch (Throwable t) {
           }
         }
 
         tzDefsFile = f;
         tzDefsZipFile = zf;
 
         TzServerUtil.lastDataFetch = System.currentTimeMillis();
         refreshNow = false;
       } catch (ServletException se) {
         throw se;
       } catch (Throwable t) {
         throw new ServletException(t);
       }
     }
   }
 
   /* ====================================================================
    *                   Caching
    * ==================================================================== */
 
   private void cacheInit() throws ServletException {
     vtzCache = manager.getCache(config.getCacheName());
   }
 
   private void cacheRefresh() throws ServletException {
     cacheRefresh(vtzCache);
     tzs.clear();
     aliasesStr = null;
     nameList = null;
   }
 
  static String getCachedVtz(final String name) throws ServletException {
     Element el = vtzCache.get(name);
 
     if (el == null) {
       return null;
     }
 
     return (String)el.getValue();
   }
 
  static void putCachedVtz(final String name, final String vtz) throws ServletException {
     Element el = new Element(name, vtz);
 
     vtzCache.put(el);
   }
 
   /* ====================================================================
    *                   Private methods
    * ==================================================================== */
 
   private static String transformTzid(String tzid) {
     int len = tzid.length();
 
     if ((len > 13) && (tzid.startsWith("/mozilla.org/"))) {
       int pos = tzid.indexOf('/', 13);
 
       if ((pos < 0) || (pos == len - 1)) {
         return tzid;
       }
       return tzid.substring(pos + 1);
     }
 
     /* Special to get James Andrewartha going */
     String ss = "/softwarestudio.org/Tzfile/";
 
     if ((len > ss.length()) &&
         (tzid.startsWith(ss))) {
       return tzid.substring(ss.length());
     }
 
     return tzid;
   }
 
   private void loadAliases() throws ServletException {
     try {
       Properties a = new Properties();
 
       a.load(new StringReader(getAliases()));
 
       aliases = a;
     } catch (ServletException se) {
       throw se;
     } catch (Throwable t) {
       error("loadTimezones error: " + t.getMessage());
       t.printStackTrace();
       throw new ServletException(t);
     }
   }
 
   private String entryToString(final ZipEntry ze) throws Throwable {
     InputStreamReader is = new InputStreamReader(tzDefsZipFile.getInputStream(ze),
                                                  "UTF-8");
 
     StringWriter sw = new StringWriter();
 
     char[] buff = new char[4096];
 
     for (;;) {
       int num = is.read(buff);
 
       if (num < 0) {
         break;
       }
 
       if (num > 0) {
         sw.write(buff, 0, num);
       }
     }
 
     is.close();
 
     return sw.toString();
   }
 
   private static Calendar cal = Calendar.getInstance();
   private static java.util.TimeZone utctz;
 
   static {
     try {
       utctz = TimeZone.getTimeZone(TimeZones.UTC_ID);
     } catch (Throwable t) {
       throw new RuntimeException("Unable to initialise UTC timezone");
     }
     cal.setTimeZone(utctz);
   }
 
   private static void cacheRefresh(final Cache cache) throws ServletException {
     if (cache != null) {
       cache.flush();
     }
   }
 
   /**
    * @return Logger
    */
   static Logger getLogger() {
     return Logger.getLogger(TzServerUtil.class);
   }
 
   /** Debug
    *
    * @param msg
    */
   static void debugMsg(final String msg) {
     getLogger().debug(msg);
   }
 
   /** Info messages
    *
    * @param msg
    */
   static void logIt(final String msg) {
     getLogger().info(msg);
   }
 
   static void error(final String msg) {
     getLogger().error(msg);
   }
 
   static void error(final Throwable t) {
     getLogger().error(TzServerUtil.class, t);
   }
 
   private static void digit2(final StringBuilder sb, final int val) throws Throwable {
     if (val > 99) {
       throw new Exception("Bad date");
     }
     if (val < 10) {
       sb.append("0");
     }
     sb.append(val);
   }
 
   private static void digit4(final StringBuilder sb, final int val) throws Throwable {
     if (val > 9999) {
       throw new Exception("Bad date");
     }
     if (val < 10) {
       sb.append("000");
     } else if (val < 100) {
       sb.append("00");
     } else if (val < 1000) {
       sb.append("0");
     }
     sb.append(val);
   }
 }
