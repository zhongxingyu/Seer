 /* **********************************************************************
     Copyright 2008 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
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
 package edu.rpi.cmt.timezones;
 
 import edu.rpi.sss.util.DateTimeUtil;
 import edu.rpi.sss.util.DateTimeUtil.BadDateException;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.UnfoldingReader;
 import net.fortuna.ical4j.model.Component;
 import net.fortuna.ical4j.model.TimeZone;
 import net.fortuna.ical4j.model.component.VTimeZone;
 import net.fortuna.ical4j.util.TimeZones;
 
 import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.params.HttpMethodParams;
 import org.apache.log4j.Logger;
 
 import java.io.InputStream;
 import java.io.StringReader;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Properties;
 import java.util.TreeSet;
 
import javax.servlet.http.HttpServletResponse;

 /** Handle caching, retrieval and registration of timezones. There are possibly
  * two sets of timezones, public or system - shared across a system, and user owned,
  * private to the current user.
  *
  * <p>System timezones are typically initialised once per system while user
  * timezones are initialised once per session.
  *
  * <p>As there is a limited set of timezones (currently around 350) it makes sense
  * to hold all the system timezones in memory. Thus, there will be no updates to
  * the pool of system timezones. Any updates therefore are assumed to be to the
  * set of user timezones.
  *
  * @author Mike Douglass
  *
  */
 public class TimezonesImpl extends Timezones {
   private transient Logger log;
 
   protected boolean debug;
 
   private String serverUrl;
 
   protected String defaultTimeZoneId;
   protected transient TimeZone defaultTimeZone;
 
   /* Map of user TimezoneInfo */
   protected HashMap<String, TimeZone> timezones = new HashMap<String, TimeZone>();
 
   protected static volatile Collection<TimeZoneName> timezoneNames;
 
   /* Cache date only UTC values - we do a lot of those but the number of
    * different dates should be limited.
    *
    * We have one cache per timezone
    */
   private HashMap<String, HashMap<String, String>> dateCaches =
     new HashMap<String, HashMap<String, String>>();
 
   private HashMap<String, String> defaultDateCache = new HashMap<String, String>();
 
   private static Properties aliases;
 
   private long datesCached;
   private long dateCacheHits;
   private long dateCacheMisses;
 
   /**
    *
    */
   public TimezonesImpl() {
     this(false);
   }
 
   /**
    * @param debug
    */
   public TimezonesImpl(boolean debug) {
     this.debug = debug;
   }
 
   public void init(String serverUrl) {
     this.serverUrl = serverUrl;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getTimeZone(java.lang.String)
    */
   public TimeZone getTimeZone(String id) throws TimezonesException {
     id = unalias(id);
 
     TimeZone tz = timezones.get(id);
     if (tz != null) {
       return tz;
     }
 
     tz = fetchTimeZone(id);
     register(id, tz);
 
     return tz;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getTimeZoneNames()
    */
   public Collection<TimeZoneName> getTimeZoneNames() throws TimezonesException {
     if (timezoneNames != null) {
       return Collections.unmodifiableCollection(timezoneNames);
     }
 
     TzServer server = new TzServer(serverUrl);
 
     try {
       String tznames = server.getNames();
 
       Collection<TimeZoneName> ids = new TreeSet<TimeZoneName>();
 
       String[] names = tznames.split("\n");
 
       for (String name: names) {
         if ((name != null) && (name.length() > 0)) {
           ids.add(new TimeZoneName(name));
         }
       }
 
       timezoneNames = ids;
 
       return Collections.unmodifiableCollection(timezoneNames);
     } finally {
       server.close();
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#refreshTimezones()
    */
   public synchronized void refreshTimezones() throws TimezonesException {
     timezoneNames = null;
     timezones = new HashMap<String, TimeZone>();
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#unalias(java.lang.String)
    */
   public String unalias(String tzid) throws TimezonesException {
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
 
 //  private static DateFormat formatTd  = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
   private static Calendar cal = Calendar.getInstance();
   private static java.util.TimeZone utctz;
   private static java.util.TimeZone lasttz;
   private static String lasttzid;
   static {
     try {
       utctz = TimeZone.getTimeZone(TimeZones.UTC_ID);
     } catch (Throwable t) {
       throw new RuntimeException("Unable to initialise UTC timezone");
     }
     cal.setTimeZone(utctz);
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#setDefaultTimeZoneId(java.lang.String)
    */
   public void setDefaultTimeZoneId(String id) throws TimezonesException {
     defaultTimeZone = null;
     defaultTimeZoneId = id;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getDefaultTimeZoneId()
    */
   public String getDefaultTimeZoneId() throws TimezonesException {
     return defaultTimeZoneId;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getDefaultTimeZone()
    */
   public TimeZone getDefaultTimeZone() throws TimezonesException {
     if ((defaultTimeZone == null) && (defaultTimeZoneId != null)) {
       defaultTimeZone = getTimeZone(defaultTimeZoneId);
     }
 
     return defaultTimeZone;
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#getUtc(java.lang.String, java.lang.String, net.fortuna.ical4j.model.TimeZone)
    */
   public synchronized String getUtc(String time, String tzid,
                                     TimeZone tz) throws TimezonesException {
     try {
       //if (debug) {
       //  trace("Get utc for " + time + " tzid=" + tzid + " tz =" + tz);
       //}
       if (DateTimeUtil.isISODateTimeUTC(time)) {
         // Already UTC
         return time;
       }
 
       String dateKey = null;
       HashMap<String, String> cache = null;
 
       if ((time.length() == 8) && DateTimeUtil.isISODate(time)) {
         /* See if we have it cached */
 
         if (tzid == null) {
           cache = defaultDateCache;
         } else if (tzid.equals(getDefaultTimeZoneId())) {
           cache = defaultDateCache;
         } else {
           cache = dateCaches.get(tzid);
           if (cache == null) {
             cache = new HashMap<String, String>();
             dateCaches.put(tzid, cache);
           }
         }
 
         String utc = cache.get(time);
 
         if (utc != null) {
           dateCacheHits++;
           return utc;
         }
 
         /* Not in the cache - calculate it */
 
         dateCacheMisses++;
         dateKey = time;
         time += "T000000";
       } else if (!DateTimeUtil.isISODateTime(time)) {
         throw new BadDateException(time);
       }
 
       boolean tzchanged = false;
 
       /* If we get a null timezone and id we are being asked for the default.
        * If we get a null tz and the tzid is the default id same again.
        *
        * Otherwise we are asked for something other than the default.
        *
        * So lasttzid is either
        *    1. null - never been called
        *    2. the default tzid
        *    3. Some other tzid.
        */
 
       if (tz == null) {
         if (tzid == null) {
           tzid = getDefaultTimeZoneId();
         }
 
         if ((lasttzid == null) || (!lasttzid.equals(tzid))) {
           if (tzid.equals(getDefaultTimeZoneId())) {
             lasttz = getDefaultTimeZone();
           } else {
             lasttz = getTimeZone(tzid);
           }
 
           if (lasttz == null) {
             lasttzid = null;
             throw new TimezonesException(TimezonesException.unknownTimezone, tzid);
           }
           tzchanged = true;
           lasttzid = tzid;
         }
       } else {
         // tz supplied
         if (tz != lasttz) {
           /* Yes, that's a !=. I'm looking for it being the same object.
            * If I were sure that equals were correct and fast I'd use
            * that.
            */
           tzchanged = true;
           tzid = tz.getID();
           lasttz = tz;
         }
       }
 
 
       if (tzchanged) {
         if (debug) {
           trace("**********tzchanged for tzid " + tzid);
         }
 //XX        formatTd.setTimeZone(lasttz);
         lasttzid = tzid;
       }
       DateFormat formatTd  = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
       formatTd.setTimeZone(lasttz);
 
       java.util.Date date = formatTd.parse(time);
 
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
 
       String utc = sb.toString();
 
       if (dateKey != null) {
         cache.put(dateKey, utc);
         datesCached++;
       }
 
       return utc;
     } catch (TimezonesException cfe) {
       throw cfe;
     } catch (BadDateException bde) {
       throw new TimezonesException(TimezonesException.badDate, time);
     } catch (Throwable t) {
       //t.printStackTrace();
       throw new TimezonesException(t);
     }
   }
 
   /**
    * @return Number of utc values cached
    */
   public long getDatesCached() {
     return datesCached;
   }
 
   /**
    * @return date cache hits
    */
   public long getDateCacheHits() {
     return dateCacheHits;
   }
 
   /**
    * @return data cache misses.
    */
   public long getDateCacheMisses() {
     return dateCacheMisses;
   }
 
   /* ====================================================================
    *                   Protected methods
    * ==================================================================== */
 
   /** Fetch a timezone object from the server given the id.
    *
    * @param id
    * @return TimeZone with id or null
    * @throws TimezonesException
    */
   protected TimeZone fetchTimeZone(final String id) throws TimezonesException {
     TzServer server = new TzServer(serverUrl);
 
     try {
       String tzdef = server.getTz(id);
 
       if ((tzdef == null) || (tzdef.length() == 0)) {
         return null;
       }
 
       CalendarBuilder cb = new CalendarBuilder();
 
       UnfoldingReader ufrdr = new UnfoldingReader(new StringReader(tzdef), true);
 
       net.fortuna.ical4j.model.Calendar cal = cb.build(ufrdr);
       VTimeZone vtz = (VTimeZone)cal.getComponents().getComponent(Component.VTIMEZONE);
       if (vtz == null) {
         throw new TimezonesException("Incorrectly stored timezone");
       }
 
       TimeZone tz = new TimeZone(vtz);
       register(id, tz);
 
       return tz;
     } catch (Throwable t) {
       throw new TimezonesException(t);
     } finally {
       server.close();
     }
   }
 
   /* (non-Javadoc)
    * @see org.bedework.calfacade.timezones.CalTimezones#register(java.lang.String, net.fortuna.ical4j.model.TimeZone)
    */
   public synchronized void register(String id,
                                                TimeZone timezone)
           throws TimezonesException {
     timezones.put(id, timezone);
   }
 
   /* ====================================================================
    *                   private methods
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
 
   private void loadAliases() throws TimezonesException {
     TzServer server = new TzServer(serverUrl);
     InputStream is = null;
 
     try {
       Properties a = new Properties();
 
       is = server.getAliases();
       a.load(is);
 
       aliases = a;
     } catch (Throwable t) {
       error("loadTimezones error: " + t.getMessage());
       t.printStackTrace();
     } finally {
       if (is != null) {
         try {
           is.close();
         } catch (Throwable t1) {}
       }
 
       server.close();
     }
   }
 
   /** CLass to allow us to call the server
    */
   private static class TzServer {
     private static String tzserverUri;
 
     private GetMethod getter;
 
     TzServer(String uri) {
       tzserverUri = uri;
     }
 
     public String getTz(String id) throws TimezonesException {
       return call("tzid=" + id);
     }
 
     public String getNames() throws TimezonesException {
       return call("names");
     }
 
     public InputStream getAliases() throws TimezonesException {
       return callForStream("aliases");
     }
 
     public String call(String req) throws TimezonesException {
       try {
         doCall(req);
 
        int status = getter.getStatusCode();

        if (status != HttpServletResponse.SC_OK) {
          return null;
        }

         return getter.getResponseBodyAsString();
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
     public InputStream callForStream(String req) throws TimezonesException {
       try {
         doCall(req);
 
         return getter.getResponseBodyAsStream();
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
     public void close() throws TimezonesException {
       try {
         if (getter == null) {
           return;
         }
 
         getter.releaseConnection();
         getter = null;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
     private void doCall(String req) throws TimezonesException {
       try {
         if (tzserverUri == null) {
           throw new TimezonesException("No timezones server URI defined");
         }
 
         HttpClient client = new HttpClient();
 
         getter = new GetMethod(tzserverUri + "?" + req);
 
         client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                                         new DefaultHttpMethodRetryHandler());
 
         client.executeMethod(getter);
       } catch (TimezonesException cfe) {
         throw cfe;
       } catch (Throwable t) {
         throw new TimezonesException(t);
       }
     }
 
   }
 
   private void digit2(StringBuilder sb, int val) throws BadDateException {
     if (val > 99) {
       throw new BadDateException();
     }
     if (val < 10) {
       sb.append("0");
     }
     sb.append(val);
   }
 
   private void digit4(StringBuilder sb, int val) throws BadDateException {
     if (val > 9999) {
       throw new BadDateException();
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
 
   /* Get a logger for messages
    */
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void error(String msg) {
     getLogger().error(msg);
   }
 
   private void trace(String msg) {
     getLogger().debug(msg);
   }
 }
