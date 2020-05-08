 /*
  * This code is part of the Architecture and Compilers Conference Map webpage. Copyright (C) 2013
  * Joseph Devietti
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the
  * GNU Affero General Public License as published by the Free Software Foundation, either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program.
  * If not, see <http://www.gnu.org/licenses/>.
  * 
  * 
  * This web service provides means to 1) find the list of tracked conferences, 2) find the link to
  * the main conference website given a WikiCFP eventid, and 3) pull conference information from
  * WikiCFP.
  */
 
 package net.devietti;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.servlet.http.*;
 
 import org.apache.commons.lang3.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.*;
 import org.jsoup.select.Elements;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonPrimitive;
 import com.google.gson.JsonSerializationContext;
 import com.google.gson.JsonSerializer;
 
 @SuppressWarnings( "serial" )
 public class ArchConfMapServlet extends HttpServlet {
 
    private PrintWriter           err;
    private HttpServletResponse   rsp;
    private final static Gson     GSON;
 
    final private static String[] CONFERENCE_NAMES = { "ISCA", "HPCA", "MICRO", "ASPLOS", "PLDI",
          "CGO", "SPLASH", "PPoPP", "ICS", "PACT", "ICPP", "CC" };
 
    static {
       Arrays.sort(CONFERENCE_NAMES);
       GsonBuilder gsb = new GsonBuilder();
       gsb.registerTypeAdapter(DateTime.class, new MyDateTimeSerializer());
       GSON = gsb.create();
    }
 
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 
       err = resp.getWriter();
       rsp = resp;
 
       String rt = req.getParameter("req");
       if ( rt == null ) return;
 
       switch ( rt ) {
       case "names":
          names(resp);
          break;
       case "conflink":
          getConfLink(req, resp);
          break;
       case "confs":
          getconfs(req, resp);
          break;
       default:
          error("invalid request: " + rt);
          break;
       }
    }
 
    /** Returns names of conferences we track. */
    private void names(HttpServletResponse resp) throws IOException {
       resp.setContentType("application/json");
       resp.getWriter().println(GSON.toJson(CONFERENCE_NAMES));
    }
 
    /**
     * Returns the URL of the external conference website (not the WikiCFP page) for the given
     * eventid.
     */
    private void getConfLink(HttpServletRequest req, HttpServletResponse resp) throws IOException {
       String eids = req.getParameter("eventid");
       if ( eids == null ) {
          error("missing required URL parameter: eventid");
          return;
       }
       Integer eid;
       try {
          eid = Integer.valueOf(eids);
       } catch ( NumberFormatException e ) {
          error(e.getMessage());
          return;
       }
       if ( eid == null || eid == 0 ) {
          error("error parsing eventid");
          return;
       }
 
       // pull down the CFP
       Document cfp = getURL("http://www.wikicfp.com/cfp/servlet/event.showcfp?eventid=" + eids);
 
       for ( Element a : cfp.select("tr td[align=center] a") ) {
          Element td = a.parent();
          if ( td.text().contains("Link:") && a.hasAttr("href")
               && a.attr("href").contains("http://") ) {
             // got the link!
             resp.setContentType("application/json");
             resp.getWriter().println(GSON.toJson(a.attr("href")));
             return;
          }
       }
 
       error("no matching link");
    }
 
    private static class MyDateTimeSerializer implements JsonSerializer<DateTime> {
       public JsonElement serialize(DateTime d, java.lang.reflect.Type typeOfSrc,
                                    JsonSerializationContext context) {
          return new JsonPrimitive(cfpDateFormat.print(d));
       }
    }
 
    private static class Conf implements Comparable<Conf> {
       String   name;
       DateTime start;
       DateTime end;
       String   dates;
       String   url;
       int      eventid;
       String   location;
       DateTime deadline;
       String   deadlineStatus;
 
       @Override
       public int compareTo(Conf c) {
          return start.compareTo(c.start);
       }
    }
 
    /** Fetch info for all tracked conferences from WikiCFP */
    private void getconfs(HttpServletRequest req, HttpServletResponse resp) throws IOException {
       // pull conference info from WikiCFP
       List<Conf> confs = new LinkedList<Conf>();
 
       // NB: WikiCFP's search only recognizes the first 8 terms, so we have multiple
       // *serialized* requests :-(
       final int BATCH_SIZE = 8;
       for ( int i = 0; i < CONFERENCE_NAMES.length; i += BATCH_SIZE ) {
          String[] slice = Arrays.copyOfRange(CONFERENCE_NAMES, i,
                                              Math.min(i + BATCH_SIZE, CONFERENCE_NAMES.length));
          confs.addAll(getConfInfo(Arrays.asList(slice)));
       }
 
       // sort conferences by start date
       Collections.sort(confs);
 
       List<Conf> toRemove = new LinkedList<Conf>();
       for ( Conf c : confs ) {
          DateTime now = new DateTime();
          // conference is already over; don't display it
          if ( c.end.isBefore(now) ) {
             toRemove.add(c);
             continue;
          }
 
          c.deadlineStatus = "pre";
          if ( c.deadline.isBefore(now) ) {
             c.deadlineStatus = "post";
          }
       }
       confs.removeAll(toRemove);
 
       resp.setContentType("application/json");
       resp.getWriter().println(GSON.toJson(confs));
    }
 
    private static final DateTimeFormatter cfpDateFormat = DateTimeFormat.forPattern("MMM d, yyyy");
    private static final DateTimeFormatter monthFormat   = DateTimeFormat.forPattern("MMM");
    private static final DateTimeFormatter dayFormat     = DateTimeFormat.forPattern("d");
    private static final DateTimeFormatter yearFormat    = DateTimeFormat.forPattern("yyyy");
 
    /** Fetch info for a list of conferences from WikiCFP */
    private List<Conf> getConfInfo(List<String> confs) throws IOException {
       String query = StringUtils.join(confs, "+");
       List<Conf> results = new LinkedList<Conf>();
 
       /*
        * NB: year=f returns hits for this year and future years. This is exactly what we want, since
        * we automatically discard conferences that have already happened.
        */
       Document doc = getURL("http://www.wikicfp.com/cfp/servlet/tool.search?year=f&q=" + query);
 
       Elements rows = doc.select("div[class=contsec] table table tr");
       for ( Iterator<Element> iter = rows.iterator(); iter.hasNext(); ) {
         final Element firstRow = iter.next();
         final Elements confName = firstRow.select("td a");
          if ( confName.isEmpty() ) continue;
 
          final Conf conf = new Conf();
 
          // make sure we match one of the conferences we're interested in
          String cn = confName.first().text().split(" ")[0];
          int found = Arrays.binarySearch(CONFERENCE_NAMES, cn);
          if ( found < 0 ) continue; // not found
         
         // don't match other ICS conferences, eg Information, Communication, Society
         if ( CONFERENCE_NAMES[found].equals("ICS") ) {
            String confFullName = firstRow.select("td").get(1).text();
            if (!confFullName.toLowerCase().contains("supercomputing")) {
               continue; // found some other ICS conference
            }
         }
         
          conf.name = confName.first().text();
 
          /*
           * we found a hit! The conference information is split across two <tr> table elements.
           * Conference name and link to cfp are in the first <tr>, and dates, location and deadline
           * in the second.
           */
 
         final Element secondRow = iter.next();
         String dates = secondRow.select("td").first().text();
          String startDate = dates.substring(0, dates.indexOf('-')).trim();
          conf.start = cfpDateFormat.parseDateTime(startDate);
          conf.end = cfpDateFormat.parseDateTime(dates.substring(dates.indexOf('-') + 1).trim());
 
          conf.dates = cfpDateFormat.print(conf.start) + " - " + cfpDateFormat.print(conf.end);
          if ( conf.start.year().equals(conf.end.year())
               && conf.start.monthOfYear().equals(conf.end.monthOfYear()) ) {
             conf.dates = monthFormat.print(conf.start) + " " + dayFormat.print(conf.start) + "-"
                          + dayFormat.print(conf.end) + " " + yearFormat.print(conf.start);
          }
 
         String deadline = secondRow.select("td").get(2).text().trim();
          if ( deadline.contains("(") ) { // abstract deadline may be in parentheses
             deadline = deadline.substring(0, deadline.indexOf('(')).trim();
          }
          conf.deadline = cfpDateFormat.parseDateTime(deadline);
 
          conf.url = "http://www.wikicfp.com" + confName.attr("href");
          /*
           * extract the WikiCFP eventid from the link, so that, later on, the client can pull the
           * cfp page and get the direct conference site link.
           */
 
          com.shopobot.util.URL url = new com.shopobot.util.URL(conf.url);
          String[] eid = url.getParameters("eventid");
          if ( 0 == eid.length ) continue;
          try {
             conf.eventid = Integer.valueOf(eid[0]);
          } catch ( NumberFormatException e ) {
             error("invalid event id " + eid);
             continue;
          }
 
         conf.location = secondRow.select("td").get(1).text();
 
          results.add(conf);
       }
       return results;
    }
 
    /** Parse a URL (presumed to be pointing at an HTML page) into a Jsoup Document */
    private Document getURL(String url) {
       Scanner s = null;
       try {
 
          s = new Scanner(new URL(url).openStream(), "UTF-8");
          return Jsoup.parse(s.useDelimiter("\\A").next());
 
       } catch ( MalformedURLException e ) {
          error(e.getMessage());
       } catch ( IOException e ) {
          error(e.getMessage());
       } finally {
          if ( s != null ) s.close();
       }
       throw new IllegalStateException("error parsing URL " + url);
    }
 
    private void error(String s) {
       rsp.setContentType("text/plain");
       err.println(s);
       throw new IllegalStateException(s);
    }
 }
