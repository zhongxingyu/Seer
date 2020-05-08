 package edu.wvup.cs460.transform;
 
 import edu.wvup.cs460.datamodel.CourseInstance;
 import edu.wvup.cs460.util.StringUtils;
 import edu.wvup.cs460.util.Tuple;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * User: Tom Byrne(kylar42@gmail.com)
  * "Code early, Code often."
  * This code is a big ball of suck. It does it's best to parse out CourseInstances from WVUP's web site
  * but it's big, and it's ugly.
  */
 public class HTMLScraperImporter implements CourseImporter {
 
     private static Logger LOG = LoggerFactory.getLogger(HTMLScraperImporter.class);
 
     private final MessageDigest md;
 
     public HTMLScraperImporter() {
         try {
             md = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException e) {
             LOG.error("Unable to create message Digest. ", e);
             throw new IllegalArgumentException(e);
         }
     }
 
     @Override
     public List<CourseImportContext> getCourses(List<Tuple<String, String>> urlCacheSigs) {
 
         /**
          *
          InputStream is = new FileInputStream("file.txt");
          try {
          is = new DigestInputStream(is, md);
          // read stream to EOF as normal...
          }
          finally {
          is.close();
          }
          byte[] digest = md.digest();
          */
         LOG.debug("Beginning Course Retrieval.");
         final List<CourseImportContext> toReturn = new ArrayList<CourseImportContext>();
         final List<URL> urls = findURLs();
         for (URL url : urls) {
             String lastMD5ForURL = findMD5(urlCacheSigs, url.toExternalForm());
            LOG.debug("Found MD5: {} for url:{}", lastMD5ForURL, url.toExternalForm());
 
             LOG.debug("Retrieving Courses from:" + url);
             StringBuilder builder = new StringBuilder();
             try {
                 final HttpURLConnection httpConnection;
                 URLConnection urlConnection = url.openConnection();
 
                 //first things first - check to see if we got a 200, 304 or other.
                 if (urlConnection instanceof HttpURLConnection) {
                     httpConnection = (HttpURLConnection) urlConnection;
                 } else {
                     LOG.error("URL Connection was not of a recognized type for URL:" + url);
                     continue;
                 }
 
                 httpConnection.connect();//make the call.
 
                 final int responseCode = httpConnection.getResponseCode();
                 if (HttpResponseStatus.NOT_MODIFIED.getCode() == responseCode) {
                     LOG.debug("Data at URL has not changed since we last checked it. URL:" + url);
                     continue;//jump out of loop.
                 }
 
                 if (HttpResponseStatus.OK.getCode() != responseCode) {
                     LOG.error("We recieved a response from the server that indicates Failure. Response:" + responseCode + " URL:" + url);
                 }
 
                 //Let's set up the reader stream
                 md.reset();
                 InputStream is = new DigestInputStream(urlConnection.getInputStream(), md);
 
 
                 //Here I need to read in the data from the URL.
                 BufferedReader br = new BufferedReader(new InputStreamReader(is));
                 String line = br.readLine();
                 while (line != null) {
                     builder.append(line);
                     line = br.readLine();
                 }
             } catch (IOException ioe) {
                 LOG.error("Unable to properly retrieve data from URL[" + url + "] for Import");
             }
             LOG.debug("Finished receiving data from:" + url);
 
             //OK, we got the data, but let's see if it's the same as what we had last time before we go parse it.
             String newMD5 = StringUtils.toHexString(md.digest());
 
             //if there's no previous MD5, or if they're not equal, then parse away!
             if (null == lastMD5ForURL || !lastMD5ForURL.equals(newMD5)) {
                 //parse and add it to our list.
                 List<CourseInstance> parsed = parseFromHTML(builder.toString());
                 toReturn.add(new CourseImportContext(newMD5, url.toExternalForm(), parsed));
                 LOG.debug("Data Parsed. I was able to find: " + parsed.size() + " courses.");
             } else {
                 LOG.debug("MD5 of incoming content matched DB. We did not parse or insert.");
             }
 
         }
 
 
         return toReturn;
     }
 
     private String findMD5(List<Tuple<String, String>> urlCacheSigs, String url) {
         for (Tuple<String, String> mod : urlCacheSigs) {
             if (url.equalsIgnoreCase(mod.getKey().toLowerCase())) {
                 return mod.getValue();
             }
         }
         return null;
     }
 
     /*< Last-Modified: Tue, 19 Mar 2013 13:10:29 GMT */
     private Date parseLastModified(String lastMod) {
         //return null if parseable.
         SimpleDateFormat format = new SimpleDateFormat(StringUtils.DATE_FORMAT_STRING);
         try {
             return format.parse(lastMod);
         } catch (ParseException e) {
             LOG.debug("Unable to parse Last Modified date:" + lastMod);
             return new Date();
         }
     }
 
     /**
      * Here's our huge bit of parsing code. Basically it takes the entire
      * HTML page and goes through it piece by piece trying to find known start/end pieces for each Course.
      * I ported this from a Python program that I had written earlier.
      *
      * @param htmlBody
      * @return
      */
     private List<CourseInstance> parseFromHTML(String htmlBody) {
         List<CourseInstance> toReturn = new ArrayList<CourseInstance>();
         try {
             //OK first things, let's find the course term.
             int ndx = htmlBody.indexOf("Course Schedule - ");
            int endNdx = htmlBody.indexOf("\"", ndx + 16);
             String courseTerm = htmlBody.substring(ndx + 17, endNdx);
             courseTerm = courseTerm.trim();
             String[] split = courseTerm.split(" ");
 
             //first is term, second is year.
             final String term = split[0];
             final String year = split[1];
 
             /**
              startIndex = pageText.find("<a name=\"TOP\">Main Campus", startIndex)
              startIndex = pageText.find("<span id=\"", startIndex)
              startIndex = pageText.find("<tr>", startIndex)
              while startIndex > -1:
 
              #from start index, we need the first <A HREF.
              endNdx = pageText.find("</tr>", startIndex)
              pullApart(pageText[startIndex:endNdx], outputFile)
              tableEnd = pageText.find("/table>", endNdx)
              startIndex = pageText.find("<tr>", endNdx)
              # check for end of table, if it's less than..
              if(tableEnd < startIndex):
              startIndex = pageText.find("<span id=\"", endNdx)
              startIndex = pageText.find("<tr>", startIndex)
              outputFile.flush()
              outputFile.close()
              <a name="TOP">Main Campus
 
              */
 
             //look for the start
             int startNdx = htmlBody.indexOf("name=\"TOP\"");
             startNdx = htmlBody.indexOf("<span id=\"", startNdx);
             startNdx = htmlBody.indexOf("<tr", startNdx);
             while (-1 < startNdx) {
 
                 endNdx = htmlBody.indexOf("</tr>", startNdx);
                 String classEntry = htmlBody.substring(startNdx, endNdx);
                 CourseInstance courseInstance = pullApart(classEntry, term, year);
 
                 if (null != courseInstance) {
                     toReturn.add(courseInstance);
                    //if (courseInstance.getCrn().equals("5704")) {
                    //    System.out.print("Yay!");
                    //}
                 }
                 int tableEnd = htmlBody.indexOf("/table>", endNdx);
                 startNdx = htmlBody.indexOf("<tr", endNdx);
                 if (tableEnd < startNdx) {
                     startNdx = htmlBody.indexOf("<span id=\"", endNdx);
                     if (-1 == startNdx) {
                         continue;//break.
                     }
                     startNdx = htmlBody.indexOf("<tr", startNdx);
                 }
             }
         } catch (Throwable t) {
 
             LOG.error("A really bad error occurred while trying to parse HTML.", t);
         }
 
         return toReturn;
     }
 
     /**
      * Pull apart just the class text.
      *
      * @param classText
      * @param term
      * @param year
      * @return
      */
     private CourseInstance pullApart(String classText, String term, String year) {
         CourseInstance toReturn = null;
         int startNdx = classText.indexOf("<tr") + 4;
         startNdx = classText.indexOf("<td>", startNdx) + 4;
         while (-1 < startNdx) {
             int endNdx = classText.indexOf("</td>");
             String tmp = classText.substring(startNdx, endNdx);
             int tmpSI = classText.indexOf("center\">", startNdx) + 8;
             int tmpEI = classText.indexOf("</a>", tmpSI);
             if (-1 == tmpEI) {
                 //didn't find the right end.
                 LOG.debug("Unable to find correct end of class piece.");
                 return null;
             }
             String classType = classText.substring(tmpSI, tmpEI).trim();
 
             //----------------------------------------------- CrossListed
 
             tmpSI = classText.indexOf("<td>", tmpEI) + 4;
             tmpSI = classText.indexOf("center\">", tmpSI) + 8;
 
 
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             //# looks like either X or &nbsp;
             boolean crosslisted = "X".equalsIgnoreCase(tmp);
 
             //----------------------------------------------- CRN
             //<td style="margin: 0px; padding: 0px 5px;"> 1409
 
             tmpSI = classText.indexOf("<td", tmpEI) + 4;
             tmpSI = classText.indexOf(">", tmpSI) + 1;
 
             //normally the end for this would be </td> but the html is malformed, and there's just another <td next.
             //but sometimes, there's a <span id> next.
             int tmpA = classText.indexOf("<span", tmpSI);
             int tmptd = classText.indexOf("<td", tmpSI);
             if (tmpA > -1 && tmpA < tmptd) {
                 tmpEI = tmpA;
             } else {
                 tmpEI = tmptd;
             }
             tmp = classText.substring(tmpSI, tmpEI).trim();
 
             String crn = tmp;
 
             //----------------------------------------------- Subject
             //Looks like <td style="margin: 0px; padding: 0px 5px;"> ACCT </td>
 
             tmpSI = classText.indexOf("<td", tmpEI);
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String subject = tmp;
 
             //----------------------------------------------- Course
             //<td style="margin: 0px; padding: 0px 5px;"> 201 </td>
 
             tmpSI = classText.indexOf("<td", tmpEI);
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String course = tmp;
 
 
             //-----------------------------------------------Title.
             // Going to be a bit more interesting here.
             //#<td style="margin: 0px; padding: 0px 5px;"> <a onmouseover="TagToTip('ACCT201', TITLE, 'ACCT 201', WIDTH, 240, TITLEBGCOLOR, '#1c2a3f', SHADOW, true, BORDERCOLOR,
             // '#1c2a3f')"onmouseout="UnTip()"> PRIN OF ACCOUNTING 1 </a><br/>
             //<a onmouseover="Tip(' Lecture ', WIDTH, 200, SHADOW, true, BORDERCOLOR, '#1c2a3f')"onmouseout="UnTip()">
             // <span style="font-style: italic"> &nbsp;&nbsp;&nbsp;&nbsp;(Lecture) </span> </td>
 
             tmpSI = classText.indexOf("<td", tmpEI);
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             //#now need to find the end of the <a
 
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             tmpEI = classText.indexOf("<", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String title = tmp;
 
             //-----------------------------------------------credit hours
             //<td> <p align="center"> 3 </td>
             tmpSI = classText.indexOf("<td>", tmpEI) + 4;
             tmpSI = classText.indexOf("center\">", tmpSI) + 8;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             short credithours = parseShort(tmp);
 
             //-----------------------------------------------Days
             //<td style="margin: 0px; padding: 0px 5px;"> T R </td>
 
             tmpSI = classText.indexOf("<td", tmpEI);
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String coursedays = "&nbsp;".equalsIgnoreCase(tmp) ? "" : tmp;
 
 
             //-----------------------------------------------Times
             //<td style="margin: 0px; padding: 0px 5px;"> 1100 - 1215 pm </td>
             tmpSI = classText.indexOf("<td", tmpEI);
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String times = "&nbsp;".equalsIgnoreCase(tmp) ? "" : tmp;
 
             //-----------------------------------------------instructor
             //<td style="margin: 0px; padding: 0px 5px;"> Morgan S </td>
             tmpSI = classText.indexOf("<td", tmpEI);
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             // there may be a <a href= "mailto:first.last@wvup.edu" >
             tmpA = classText.indexOf("<a ", tmpSI);
             tmptd = classText.indexOf("</td>", tmpSI);
             if (tmpA > -1 && tmpA < tmptd) {
                 tmpEI = tmpA;
             } else {
                 tmpEI = tmptd;
             }
 
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String instructor = "&nbsp;".equalsIgnoreCase(tmp) ? "" : tmp;
 
             //-----------------------------------------------classroom
             //<td style="margin: 0px; padding: 0px 5px;">
             // <a onmouseover="Tip(' Classroom (MAIN CAMPUS) ', WIDTH, 0, SHADOW, true, BORDERCOLOR, '#1c2a3f')"onmouseout="UnTip()"> 1330 (MAIN) </a></td>
             tmpSI = classText.indexOf("<td", tmpEI) + 4;
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             //now need to find the end of the <a
             tmpSI = classText.indexOf(">", tmpSI) + 1;
             tmpEI = classText.indexOf("<", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String classroom = "&nbsp;".equalsIgnoreCase(tmp) ? "" : tmp;
 
 
             //-----------------------------------------------startdate
             //<td> <p align="center"> 19-AUG-13 </td>
 
             tmpSI = classText.indexOf("<td>", tmpEI) + 4;
             tmpSI = classText.indexOf("center\">", tmpSI) + 8;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             Date startdate = parseDate(tmp);
             //todo make a real date.
 
             //-----------------------------------------------Seats available
             //td> <a onmouseover="Tip(' Seats Available: 3 <br/>
             // \Waitlist: 0 ', WIDTH, 120, SHADOW, true, BORDERCOLOR, '#1c2a3f')"onmouseout="UnTip()"> <p align="center"> <p align="center">3 </a> </td>
 
             //note that there MAY be an empty bit here, so let's just check
             tmpSI = classText.indexOf("<td", tmpEI) + 4;
             int tmpEndNDX = classText.indexOf("</td>", tmpSI);
             int nextCenter = classText.indexOf("center\">", tmpSI) + 8;
             if (tmpEndNDX < tmpSI || nextCenter < tmpSI) {
                 tmpEI = tmpEndNDX + 5;//reset end index.
                 tmp = "-10";//set it so that we know we didn't get it for real.
 
             } else {
                 tmpSI = classText.indexOf("center\">", nextCenter) + 8;// #it's malformed.
                 tmpEI = classText.indexOf("<", tmpSI);
                 tmp = classText.substring(tmpSI, tmpEI).trim();
             }
             short seatsavailable = parseShort(tmp);
 
 
             //-----------------------------------------------Term Length
             //#<td> <p align="center">FULL TERM<br />(14-JAN-13 - 10-MAY-13) </td>
             tmpSI = classText.indexOf("<td>", tmpEI) + 4;
             nextCenter = classText.indexOf("center\">", tmpSI) + 8;
             tmpEI = classText.indexOf("</td>", tmpSI);
             if (nextCenter < tmpSI || nextCenter > tmpEI) {
                 tmp = "";
             } else {
                 tmp = classText.substring(nextCenter, tmpEI).trim();
                 //#need to remove embedded html linebreaks too
                 tmp = tmp.replace("<br />", " ");
                 tmp = tmp.replace("<br>", " ");
             }
             String termlength = tmp;
             Date enddate = parseDateFromTerm(termlength);
 
             //-----------------------------------------------campus
             //#<td> Main </td>
             tmpSI = classText.indexOf("<td", tmpEI) + 4;
            tmpSI = classText.indexOf(">", tmpSI)+1;
             tmpEI = classText.indexOf("</td>", tmpSI);
             tmp = classText.substring(tmpSI, tmpEI).trim();
             String campus = "&nbsp;".equalsIgnoreCase(tmp) ? "" : tmp;
 
             toReturn = new CourseInstance(crn, classType, crosslisted, subject, course, title, credithours,
                                           coursedays, times, instructor, classroom, startdate, enddate, seatsavailable,
                                           termlength, campus, term, year);
 
 
             LOG.debug(toReturn.toString());
 
             startNdx = classText.indexOf("<tr>", endNdx);
 
         }
 
 
         return toReturn;
     }
 
     /**
      * Convenience method to return a short or known value if it throws.
      *
      * @param val
      * @return
      */
     private short parseShort(String val) {
         short toReturn = Short.MIN_VALUE;
         try {
             toReturn = Short.parseShort(val);
         } catch (NumberFormatException ignore) {
         }
         return toReturn;
     }
 
     // Convenience method to return a Date or new value if it can't parse.
 
     private Date parseDateFromTerm(String term) {
         if (null == term || term.isEmpty()) {
             return new Date();
         }
         //get what's in the brackets, break it out by " - " and use the last half.
         int startNdx = term.indexOf('(');
         int endNdx = term.lastIndexOf(')');
         if (-1 == startNdx || -1 == endNdx) {
             return new Date();
         }
         String middle = term.substring(startNdx + 1, endNdx).trim();
         String[] split = middle.split(" - ");
         return parseDate(split[1]);
     }
 
     /**
      * Parse the date from the known published format.
      *
      * @param strDate
      * @return
      */
     private Date parseDate(String strDate) {
         final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yy");
         try {
             final Date parse = simpleDateFormat.parse(strDate);
             return parse;
         } catch (ParseException e) {
             return new Date();
         }
     }
 
     //these are the URL's we're going to look at.
     String[] url_list = {
             "http://schedules.wvup.edu/fall.htm",
             "http://schedules.wvup.edu/fall_jcc.htm",
             "http://schedules.wvup.edu/spring.htm",
             "http://schedules.wvup.edu/spring_jcc.htm",
             "http://schedules.wvup.edu/summer.htm",
             "http://schedules.wvup.edu/summer_jcc.htm",
     };
 
     //Convenience method to get the URL's as URL objects, not as Strings.
     private List<URL> findURLs() {
         ArrayList<URL> urlList = new ArrayList<URL>();
         for (String s : url_list) {
             try {
                 urlList.add(new URL(s));
             } catch (MalformedURLException e) {
                 LOG.error("Unable to create URL from string.", e);
             }
         }
         return urlList;
     }
 
 }
