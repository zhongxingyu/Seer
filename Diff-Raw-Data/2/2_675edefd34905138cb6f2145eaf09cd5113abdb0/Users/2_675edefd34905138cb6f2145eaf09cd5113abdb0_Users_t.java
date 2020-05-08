 /*-
  * Copyright (c) 2003-2011 Lucas Holt
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
  * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
  * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  */
 
 package com.justjournal.ctl;
 
 import com.justjournal.Cal;
 import com.justjournal.User;
 import com.justjournal.WebError;
 import com.justjournal.atom.AtomFeed;
 import com.justjournal.core.Settings;
 import com.justjournal.db.*;
 import com.justjournal.rss.CachedHeadlineBean;
 import com.justjournal.rss.Rss;
 import com.justjournal.search.BaseSearch;
 import com.justjournal.utility.HTMLUtil;
 import com.justjournal.utility.StringUtil;
 import com.justjournal.utility.Xml;
 import com.lowagie.text.*;
 import com.lowagie.text.Font;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfWriter;
 import com.lowagie.text.rtf.RtfWriter2;
 import org.apache.log4j.Logger;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import java.awt.*;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * Journal viewer for JustJournal.
  *
  * @author Lucas Holt
  * @version $Id: Users.java,v 1.43 2012/06/23 18:15:31 laffer1 Exp $
  * @since 1.0
  */
 @SuppressWarnings({"ClassWithoutLogger"})
 public final class Users extends HttpServlet {
     // constants
     private static final char endl = '\n';
     private static final int RESPONSE_BUFSIZE = 8192;
 
     enum RequestType {
         entries, friends, calendar, atom, rss, rsspics,
         calendarnumeric, calendarmonth, calendarday,
         feedreader, singleentry, imagelist, search, pdf, rtf, tag
     }
 
     private static final Logger log = Logger.getLogger(Users.class);
     @SuppressWarnings({"InstanceVariableOfConcreteClass"})
     private Settings set = null;
 
     /**
      * Initializes the servlet.
      *
     * @param config The servlet config
      * @throws ServletException The servlet is hosed.
      */
     @Override
     public void init(final ServletConfig config) throws ServletException {
         super.init(config);
         final ServletContext ctx = config.getServletContext();
         set = Settings.getSettings(ctx);
     }
 
     /**
      * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
      *
      * @param request  servlet request
      * @param response servlet response
      * @throws ServletException    We screwed up.
      * @throws java.io.IOException Doh.
      */
     private void processRequest(final HttpServletRequest request, final HttpServletResponse response)
             throws ServletException, java.io.IOException {
 
         final StringBuffer sb = new StringBuffer(512);
 
         /* start session if one does not exist.*/
         final HttpSession session = request.getSession(true);
 
         String authUserTemp;
         com.justjournal.User aUser = null;
         /* Does a username exist in session?  i.e. are we logged in?*/
         authUserTemp = (String) session.getAttribute("auth.user");
 
         if (authUserTemp != null) {
             try {
                 aUser = new com.justjournal.User(authUserTemp); // authenticated user
             } catch (Exception ex) {
                 log.error("processRequest(): Unable to load user information; " + ex.getMessage());
             }
         }
         /*
             Get username for Journal we are trying to view/access.
         */
         String userName = "";
 
         // Create a pattern to match slashes
         final Pattern p = Pattern.compile("[/]+");
 
         // Split input with the pattern
         final String[] arrUri = p.split(request.getRequestURI());
         final int arrUriLength = java.lang.reflect.Array.getLength(arrUri);
 
         if (arrUriLength > 2) {
             if (arrUri[1].equals("justjournal"))
                 userName = arrUri[3].toLowerCase();
             else
                 userName = arrUri[2].toLowerCase();
         }
 
         /* Initialize Preferences Object */
         final User buser;
         try {
             buser = new User(userName);
         } catch (Exception ex) {
             throw new ServletException(ex);
         }
 
         final UserContext userc = new UserContext(sb, buser, aUser);
 
         // if the length is greater than three then the uri is like /users/laffer1/friends
         RequestType reqtype = RequestType.entries;  //default
 
         int year = 0;
         int month = 0;
         int day = 0;
         int singleEntryId = 0;
         String tag = "";
 
         if (arrUriLength > 3) {
             if (arrUri[3].compareTo("friends") == 0) {
                 reqtype = RequestType.friends;
             } else if (arrUri[3].compareTo("calendar") == 0) {
                 final java.util.Calendar cal = new GregorianCalendar();
                 year = cal.get(java.util.Calendar.YEAR);
                 reqtype = RequestType.calendar;
             } else if (arrUri[3].compareTo("atom") == 0) {
                 reqtype = RequestType.atom;
             } else if (arrUri[3].compareTo("rss") == 0) {
                 reqtype = RequestType.rss;
             } else if (arrUri[3].compareTo("rsspics") == 0) {
                 reqtype = RequestType.rsspics;
             } else if (arrUri[3].compareTo("subscriptions") == 0) {
                 reqtype = RequestType.feedreader;
             } else if (arrUri[3].compareTo("search") == 0) {
                 reqtype = RequestType.search;
             } else if (arrUri[3].compareTo("pictures") == 0) {
                 reqtype = RequestType.imagelist;
             } else if (arrUri[3].compareTo("pdf") == 0) {
                 reqtype = RequestType.pdf;
             } else if (arrUri[3].compareTo("rtf") == 0) {
                 reqtype = RequestType.rtf;
             } else if (arrUri[3].compareTo("tag") == 0) {
                 reqtype = RequestType.tag;
                 if (arrUriLength > 3)
                     tag = arrUri[4];
 
             } else if (arrUri[3].matches("\\d\\d\\d\\d")) {
 
                 year = Integer.parseInt(arrUri[3]);
 
                 if (arrUriLength > 4 && arrUri[4].matches("\\d\\d")) {
                     month = Integer.parseInt(arrUri[4]);
 
                     if (arrUriLength > 5 && arrUri[5].matches("\\d\\d")) {
                         day = Integer.parseInt(arrUri[5]);
                         reqtype = RequestType.calendarday;
                     } else {
                         reqtype = RequestType.calendarmonth;
                     }
                 } else {
                     reqtype = RequestType.calendarnumeric;
                 }
             } else if (arrUri[3].compareTo("entry") == 0 && arrUriLength > 4) {
                 reqtype = RequestType.singleentry;
                 try {
                     singleEntryId = Integer.parseInt(arrUri[4]);
                 } catch (NumberFormatException e) {
                     log.error(e.getMessage());
                     singleEntryId = -1; // invalid entry id.  flag the problem.
                 }
             }
         }
 
         if (reqtype == RequestType.rss || reqtype == RequestType.rsspics) {
             response.setContentType("application/rss+xml; charset=ISO-8859-1");
             response.setBufferSize(RESPONSE_BUFSIZE);
 
             if (!userc.getBlogUser().isPrivateJournal() || userc.isAuthBlog())
                 if (reqtype == RequestType.rss)
                     getRSS(userc);
                 else
                     getPicturesRSS(userc);
             else
                 sb.append("Security restriction");
         } else if (reqtype == RequestType.atom) {
             response.setContentType("text/xml; charset=UTF-8");
             response.setBufferSize(RESPONSE_BUFSIZE);
 
             if (!userc.getBlogUser().isPrivateJournal() || userc.isAuthBlog())
                 getAtom(userc);
             else
                 sb.append("Security restriction");
         } else if (reqtype == RequestType.pdf) {
             response.setContentType("application/pdf");
             response.setBufferSize(RESPONSE_BUFSIZE);
 
             if ((!(userc.getBlogUser().isPrivateJournal()) || userc.isAuthBlog()))
                 getPDF(response, userc);
         } else if (reqtype == RequestType.rtf) {
             // this version is from an RFC
             response.setContentType("application/rtf"); // originally text/rtf
             response.setBufferSize(RESPONSE_BUFSIZE);
 
             if (!(userc.getBlogUser().isPrivateJournal()) || userc.isAuthBlog())
                 getRTF(response, userc);
         } else {
 
             /*
                 Example of Expiring Headers for browsers!
 
                 header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");    // Date in the past
                 header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT"); // always modified
                 header("Cache-Control: no-store, no-cache, must-revalidate");  // HTTP/1.1
                 header("Cache-Control: post-check=0, pre-check=0", false);
                 header("Pragma: no-cache"); // HTTP/1.0
             */
             response.setContentType("text/html; charset=utf-8");
             response.setBufferSize(RESPONSE_BUFSIZE);
             // response.setHeader("Vary", "Accept"); // content negotiation
             response.setDateHeader("Expires", System.currentTimeMillis());
             response.setDateHeader("Last-Modified", System.currentTimeMillis());
             response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
             response.setHeader("Pragma", "no-cache");
 
             sb.append("<!DOCTYPE html>");
             sb.append(endl);
 
             sb.append("<html lang=\"en\">");
             sb.append(endl);
 
             sb.append("<head>");
             sb.append(endl);
             if (!userc.getBlogUser().isSpiderAllowed()) {
                 sb.append("\t<meta name=\"robots\" content=\"noindex, nofollow, noarchive\">");
                 sb.append(endl);
                 sb.append("\t<meta name=\"googlebot\" content=\"nosnippet\">");
                 sb.append(endl);
             }
             sb.append("\t<title>").append(userc.getBlogUser().getJournalName()).append("</title>");
             sb.append(endl);
 
             sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" href=\"/styles/users.css\">");
             sb.append(endl);
 
             /* User's custom style URL.. i.e. uri to css doc outside domain */
             if (userc.getBlogUser().getStyleUrl() != null && userc.getBlogUser().getStyleUrl().length() != 0) {
                 sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"").append(userc.getBlogUser().getStyleUrl()).append("\">");
                 sb.append(endl);
             } else {
                 /* use our template system instead */
                 sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"/styles/");
                 sb.append(userc.getBlogUser().getStyleId());
                 sb.append(".css\" />");
                 sb.append(endl);
             }
             sb.append("<link href=\"//netdna.bootstrapcdn.com/font-awesome/4.0.3/css/font-awesome.css\" rel=\"stylesheet\">");
 
             /* Optional style sheet overrides! */
             if (userc.getBlogUser().getStyleDoc().length() != 0 && userc.getBlogUser().getStyleDoc() != null && userc.getBlogUser().getStyleDoc().length() > 0) {
                 sb.append("\t<style type=\"text/css\" media=\"all\" id=\"UserStyleSheet\">");
                 sb.append(endl);
                 sb.append("\t<!--");
                 sb.append(endl);
                 sb.append(userc.getBlogUser().getStyleDoc());
                 sb.append("\t-->");
                 sb.append(endl);
                 sb.append("\t</style>");
                 sb.append(endl);
             }
             /* End overrides */
             // rss alt link.
             sb.append("\t<link rel=\"alternate\" type=\"application/rss+xml\" title=\"RSS\" href=\"http://www.justjournal.com/users/").append(userName).append("/rss\">\n");
             sb.append("\t<link rel=\"alternate\" type=\"application/atom+xml\" title=\"Atom\" href=\"http://www.justjournal.com/users/").append(userName).append("/atom\">\n");
 
             // Service definitions
             sb.append("\t<link rel=\"EditURI\" type=\"application/rsd+xml\" title=\"RSD\" href=\"http://www.justjournal.com/rsd?blogID=").append(userName).append("\">\n");
 
             // content switch javascript
             sb.append("\t<script type=\"text/javascript\" src=\"/js/switchcontent.js\"></script>\n");
             // lightbox
             sb.append("\t<script type=\"text/javascript\" src=\"/components/jquery/jquery.min.js\"></script>\n");
             sb.append("\t<script type=\"text/javascript\" src=\"/components/jquery-ui/ui/minified/jquery-ui.min.js\"></script>\n");
             sb.append("\t<script type=\"text/javascript\" src=\"/js/lightbox.js\"></script>\n");
             sb.append("\t<link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"/lightbox.css\">\n");
             sb.append("</head>\n");
 
             sb.append("<body>\n");
 
             // if the user has owner only the first check will fail
             // but if they are logged in then they can see it anyway!
             if (!(userc.getBlogUser().isPrivateJournal()) || userc.isAuthBlog()) {
 
                 // BEGIN MENU
                 sb.append("\t<!-- Header: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"header\">");
                 sb.append(endl);
                 sb.append("\t\t<h1>");
                 sb.append(userc.getBlogUser().getJournalName());
                 sb.append("</h1>");
                 sb.append(endl);
                 sb.append("\t</div>");
                 sb.append(endl);
                 sb.append("\t<!-- Header: End -->\n");
                 sb.append(endl);
 
                 sb.append("\t<!-- Menu: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"menu\">");
                 sb.append(endl);
 
                 if (userc.getBlogUser().showAvatar()) {
                     sb.append("\t\t<img alt=\"avatar\" src=\"/image?id=");
                     sb.append(userc.getBlogUser().getUserId());
                     sb.append("\"/>");
                     sb.append(endl);
                 }
 
                 sb.append("\t<p id=\"muser\">");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("\">Journal Entries</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/calendar\">Calendar</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/friends\">Friends</a><br />");
                 sb.append(endl);
                 if (aUser != null && aUser.getUserId() > 0) {
                     sb.append("\t\t<a href=\"/favorite/view.h");
                     sb.append("\">Favorites</a><br />");
                     sb.append(endl);
                 }
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/pictures\">Pictures</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/profile.jsp?user=");
                 sb.append(userName);
                 sb.append("\">Profile</a><br />");
                 sb.append(endl);
                 sb.append("\t</p>");
                 sb.append(endl);
 
                 // General stuff...
                 sb.append("\t<p id=\"mgen\">");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/#/entry\">Update Journal</a><br />");
                 sb.append(endl);
 
                 // Authentication menu choice
                 if (aUser != null && aUser.getUserId() > 0) {
                     // User is logged in.. give them the option to log out.
                     sb.append("\t\t<a href=\"/prefs/index.jsp\">Preferences</a><br />");
                     sb.append(endl);
                     sb.append("\t\t<a href=\"/logout.jsp\">Log Out</a>");
                     sb.append(endl);
                 } else {
                     // User is logged out.. give then the option to login.
                     sb.append("\t\t<a href=\"/login.jsp\">Login</a>");
                     sb.append(endl);
                 }
                 sb.append("\t</p>");
                 sb.append(endl);
 
                 sb.append("\t<p id=\"mrssreader\">");
                 sb.append(endl);
                 // rss reader link
                 sb.append("\t\t<a href=\"/users/");
                 sb.append(userName);
                 sb.append("/subscriptions\">RSS Reader</a><br />");
                 sb.append(endl);
                 sb.append("\t</p>");
                 sb.append(endl);
 
                 sb.append("<div id=\"mformats\"><strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 10px; width: 100%; font-size: 10px;\">Formats</strong><br />\n");
                 sb.append("<p>");
                 /* If the user has a private journal, their RSS feed
                    isn't public anyway. */
                 if (!userc.getBlogUser().isPrivateJournal()) {
                     // rss feed link
                     sb.append("\t\t<a rel=\"alternate\" href=\"/users/");
                     sb.append(userName);
                     sb.append("/rss\"><i class=\"fa fa-rss\"></i> RSS</a><br />");
                     sb.append(endl);
                     sb.append("\t\t<a rel=\"alternate\" href=\"/users/");
                     sb.append(userName);
                     sb.append("/atom\"><i class=\"fa fa-rss\"></i> ATOM</a><br />");
                     sb.append(endl);
                 }
 
                 sb.append("\t\t<img src=\"/images/icon_pdf.gif\" alt=\"PDF\" /><a href=\"").append(set.getBaseUri()).append("users/");
                 sb.append(userName);
                 sb.append("/pdf\">PDF</a><br />");
                 sb.append(endl);
                 sb.append("\t\t<img src=\"/images/icon_rtf.gif\" alt=\"RTF\" /><a href=\"").append(set.getBaseUri()).append("users/");
                 sb.append(userName);
                 sb.append("/rtf\">RTF</a><br />");
                 sb.append(endl);
                 sb.append("\t</p>");
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("<div id=\"msearchbox\"><strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\">Search</strong>\n");
 
                 sb.append("\t<form id=\"msearch\" action=\"");
                 sb.append(set.getBaseUri()).append("/users/");
                 sb.append(userName);
                 sb.append("/search\" method=\"get\">");
                 sb.append(endl);
                 sb.append("\t\t<p><input type=\"text\" name=\"bquery\" id=\"bquery\" style=\"width: 90px;\" /><br />");
                 sb.append(endl);
                 sb.append("\t\t<input type=\"submit\" name=\"search\" id=\"searchbtn\" value=\"Search Blog\" /></p>");
 
                 sb.append(endl);
                 sb.append("\t</form>");
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 // display mini calendar in menu
                 getCalendarMini(userc);
 
                 // display recent entry links
                 getUserRecentEntries(userc);
 
                 log.debug("getUserLinks call");
                 // user links display
                 getUserLinks(userc);
 
                 // Archive of entries...
                 getArchive(userc);
 
                 // List tags
                 getTagMini(userc);
 
                 sb.append("\t</div>");
                 sb.append(endl);
                 sb.append("\t<!-- Menu: End -->\n");
                 sb.append(endl);
 
                 // END MENU
 
                 sb.append("\t<!-- Content: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"content\">");
                 sb.append(endl);
 
                 if (aUser != null && aUser.getUserId() > 0) {
                     sb.append("\t<p>You are logged in as <a href=\"/users/");
                     sb.append(aUser.getUserName());
                     sb.append("\"><img src=\"/images/userclass_16.png\" alt=\"user\" />");
                     sb.append(aUser.getUserName());
                     sb.append("</a>.</p>");
                     sb.append(endl);
                 }
 
                 try {
                     switch (reqtype) {
                         case entries:
                             // number of entries to skip!  (currently only userful for recent entries)
                             int skip = 0;
 
                             try {
                                 if (request.getParameter("skip") != null)
                                     skip = Integer.valueOf(request.getParameter("skip"));
                             } catch (NumberFormatException exInt) {
                                 skip = 0;
                                 log.error(exInt.getMessage());
                             }
 
                             getEntries(userc, skip);
 
                             break;
                         case friends:
                             getFriends(userc);
                             break;
                         case calendar:
                             getCalendar(year, userc);
                             break;
                         case feedreader:
                             getSubscriptions(userc);
                             break;
                         case calendarnumeric:
                             getCalendar(year, userc);
                             break;
                         case calendarmonth:
                             getCalendarMonth(year, month, userc);
                             break;
                         case calendarday:
                             getCalendarDay(year, month, day, userc);
                             break;
                         case singleentry:
                             getSingleEntry(singleEntryId, userc);
                             break;
                         case imagelist:
                             getImageList(userc);
                             break;
                         case search:
                             int maxr = 20;
                             final String max = request.getParameter("max");
                             String bquery = request.getParameter("bquery");
                             if (bquery == null)
                                 bquery = "";
 
                             if (max != null && max.length() > 0)
                                 try {
                                     maxr = Integer.parseInt(max);
                                 } catch (NumberFormatException exInt) {
                                     maxr = 20;
                                     log.error(exInt.getMessage());
                                 }
                             search(userc, maxr, bquery);
                             break;
                         case tag:
                             getTags(userc, tag);
                             break;
                     }
                 } catch (Exception ex) {
                     log.error(ex.getMessage());
                     log.error(ex.toString());
                     WebError.Display(" Error",
                             "Error accessing data.",
                             sb);
                 }
 
                 sb.append("\t</div>");
                 sb.append(endl);
                 sb.append("\t<!-- Content: End -->");
                 sb.append(endl);
                 sb.append(endl);
 
                 sb.append("\t<!-- Footer: Begin -->");
                 sb.append(endl);
                 sb.append("\t<div id=\"footer\">");
                 sb.append(endl);
                 sb.append("\t\t<a href=\"/index.jsp\" title=\"JustJournal.com: Online Journals\">JustJournal.com</a> ");
                 sb.append("\t</div>");
                 sb.append(endl);
 
                 sb.append("\t<!-- Footer: End -->\n");
                 sb.append(endl);
             }
 
             sb.append("</body>");
             sb.append(endl);
             sb.append("</html>");
             sb.append(endl);
         }
 
         response.setContentLength(sb.length());
         // output the result of our processing
         final ServletOutputStream outstream = response.getOutputStream();
         outstream.print(sb.toString());
         outstream.flush();
         outstream.close();
     }
 
     private static void getPDF(final HttpServletResponse response, final UserContext uc) {
         try {
             final Document document = new Document();
             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PdfWriter.getInstance(document, baos);
             formatRTFPDF(uc, document);
             document.close();
 
             response.setHeader("Expires", "0");
             response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
             response.setHeader("Pragma", "public");
             // RFC 1806
             response.setHeader("Content-Disposition", "attachment; filename=" + uc.getBlogUser().getUserName() + ".pdf");
             response.setContentLength(baos.size()); /* required by IE */
             final ServletOutputStream out = response.getOutputStream();
             baos.writeTo(out);
             out.flush();
             out.close();
         } catch (DocumentException e) {
             log.error("Users.getPDF():" + e.getMessage());
         } catch (IOException e1) {
             log.error("Users.getPDF():" + e1.getMessage());
         } catch (Exception e) {
             // user class caused this
             log.error("Users.getPDF():" + e.getMessage());
         }
     }
 
     private static void getRTF(final HttpServletResponse response, final UserContext uc) {
         try {
             final Document document = new Document();
             final ByteArrayOutputStream baos = new ByteArrayOutputStream();
             RtfWriter2.getInstance(document, baos);
             formatRTFPDF(uc, document);
             document.close();
 
             response.setHeader("Expires", "0");
             response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
             response.setHeader("Pragma", "public");
             // RFC 1806
             response.setHeader("Content-Disposition", "attachment; filename=" + uc.getBlogUser().getUserName() + ".rtf");
             response.setContentLength(baos.size()); /* required by IE */
             final ServletOutputStream out = response.getOutputStream();
             baos.writeTo(out);
             out.flush();
             out.close();
         } catch (DocumentException e) {
             log.error("Users.getPDF():" + e.getMessage());
         } catch (IOException e1) {
             log.error("Users.getPDF():" + e1.getMessage());
         } catch (Exception e) {
             // user class caused this
             log.error("Users.getPDF():" + e.getMessage());
         }
     }
 
     private static void formatRTFPDF(final UserContext uc, final Document document)
             throws Exception {
 
         document.open();
         document.add(new Paragraph(""));
         Chunk chunk = new Chunk(uc.getBlogUser().getJournalName());
         chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_STROKE, 0.4f, new Color(0x00, 0x00, 0xFF));
         document.add(chunk);
         document.add(new Paragraph(new Date().toString(), new Font(Font.HELVETICA, 10.0F)));
         document.add(Chunk.NEWLINE);
 
         final Collection<EntryTo> entries;
 
         // The blog owner should see all entries
         if (uc.isAuthBlog())
             entries = EntryDAO.viewAll(uc.getBlogUser().getUserName(), true);
         else
             entries = EntryDAO.viewAll(uc.getBlogUser().getUserName(), false); // not logged in security
 
         // Format the current time.
         final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
         final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
         final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
         String lastDate = "";
         String curDate;
 
         EntryTo o;
         final Iterator itr = entries.iterator();
 
         for (int i = 0, n = entries.size(); i < n; i++) {
             o = (EntryTo) itr.next();
 
             // Parse the previous string back into a Date.
             final ParsePosition pos = new ParsePosition(0);
             final java.util.Date currentDate = formatter.parse(o.getDate().toString(), pos);
 
             curDate = formatmydate.format(currentDate);
 
             if (curDate.compareTo(lastDate) != 0) {
                 document.add(new Paragraph(curDate, new Font(Font.HELVETICA, 14.0F)));
                 lastDate = curDate;
             }
 
             document.add(new Paragraph(formatmytime.format(currentDate), new Font(Font.HELVETICA, 12.0F)));
             document.add(Chunk.NEWLINE);
             chunk = new Chunk(o.getSubject());
             chunk.setTextRenderMode(PdfContentByte.TEXT_RENDER_MODE_FILL, 0.3F, new Color(0x00, 0x00, 0x00));
             document.add(chunk);
             document.add(Chunk.NEWLINE);
 
             document.add(new Paragraph(HTMLUtil.textFromHTML(o.getBody()), new Font(Font.TIMES_ROMAN, 11.0F)));
             document.add(Chunk.NEWLINE);
 
             if (o.getSecurityLevel() == 0)
                 document.add(new Paragraph("Security: " + "Private", new Font(Font.HELVETICA, 10.0F)));
             else if (o.getSecurityLevel() == 1)
                 document.add(new Paragraph("Security: " + "Friends", new Font(Font.HELVETICA, 10.0F)));
             else
                 document.add(new Paragraph("Security: " + "Public", new Font(Font.HELVETICA, 10.0F)));
 
             document.add(new Chunk("Location: " + o.getLocationName()));
             document.add(Chunk.NEWLINE);
             document.add(new Chunk("Mood: " + o.getMoodName()));
             document.add(Chunk.NEWLINE);
             document.add(new Chunk("Music: " + o.getMusic()));
             document.add(Chunk.NEWLINE);
             document.add(Chunk.NEWLINE);
         }
     }
 
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void getImageList(final UserContext uc) {
         final StringBuffer sb = uc.getSb();
         sb.append("<h2>Pictures</h2>");
         sb.append(endl);
         sb.append("<ul style=\"list-style-image: url('/images/pictureframe.png'); list-style-type: circle;\">");
 
 
         ResultSet rs = null;
         String imageTitle;
         final String sqlStmt = "SELECT id, title FROM user_images WHERE owner='" + uc.getBlogUser().getUserId() + "' ORDER BY title;";
 
         try {
             rs = SQLHelper.executeResultSet(sqlStmt);
 
             while (rs.next()) {
                 imageTitle = rs.getString("title");
 
                 sb.append("\t<li>");
                 sb.append("<a href=\"/AlbumImage?id=");
                 sb.append(rs.getString("id"));
                 sb.append("\" rel=\"lightbox\" title=\"");
                 sb.append(imageTitle);
                 sb.append("\">");
                 sb.append(imageTitle);
                 sb.append("</a>");
                 sb.append("</li>");
                 sb.append(endl);
             }
 
             rs.close();
 
         } catch (Exception e1) {
             if (rs != null) {
                 try {
                     rs.close();
                 } catch (Exception e) {
                     // NOTHING TO DO
                 }
             }
         }
         sb.append("</ul>\n");
         sb.append("<p>Subscribe to pictures ");
         sb.append("<a href=\"/users/");
         sb.append(uc.getBlogUser().getUserName());
         sb.append("/rsspics\">feed</a>.</p>");
     }
 
     private static void getSubscriptions(final UserContext uc) {
         final StringBuffer sb = uc.getSb();
 
         sb.append("<h2>RSS Reader</h2>");
         sb.append(endl);
         sb.append("<p>This page might be slow because we must wait for RSS to download from different servers!.</p>");
         sb.append(endl);
         sb.append("<p><a href=\"javascript:sweeptoggle('contract')\">Contract All</a> | <a href=\"javascript:sweeptoggle('expand')\">Expand All</a></p>");
         sb.append(endl);
 
         final CachedHeadlineBean hb = new CachedHeadlineBean();
 
         try {
             final Collection<RssSubscriptionsTO> rssfeeds = RssSubscriptionsDAO.view(uc.getBlogUser().getUserId());
 
             /* Iterator */
             RssSubscriptionsTO o;
             final Iterator itr = rssfeeds.iterator();
             for (int i = 0, n = rssfeeds.size(); i < n; i++) {
                 o = (RssSubscriptionsTO) itr.next();
 
                 sb.append(hb.parse(o.getUri()));
                 sb.append(endl);
             }
 
         } catch (Exception e) {
             log.error(e.getMessage());
             WebError.Display("RSS Subscriptions Error", "Can not retrieve RSS content.", sb);
         }
     }
 
     private static void getSingleEntry(final int singleEntryId, final UserContext uc) {
         if (log.isDebugEnabled())
             log.debug("getSingleEntry: Loading DAO");
 
         final StringBuffer sb = uc.getSb();
         EntryTo o;
 
         if (singleEntryId < 1) {
             WebError.Display("Invalid Entry Id", "The entry id was invalid for the journal entry you tried to view.", sb);
         } else {
             try {
                 if (uc.isAuthBlog()) {
                     o = EntryDAO.viewSingle(singleEntryId);
 
 
                     if (log.isDebugEnabled())
                         log.debug("getSingleEntry: User is logged in.");
                 } else {
                     o = EntryDAO.viewSinglePublic(singleEntryId);
 
                     if (log.isDebugEnabled())
                         log.debug("getSingleEntry: User is not logged in.");
                 }
 
                 // Format the current time.
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
                 String curDate;
 
                 if (log.isDebugEnabled())
                     log.debug("getSingleEntry: Begin reading record.");
 
 
                 if (o != null && o.getId() > 0) {
 
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(o.getDate().toString(), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     sb.append("<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
 
                     sb.append(formatEntry(uc, o, currentDate, true));
                 }
             } catch (Exception e1) {
                 WebError.Display("Error",
                         "Unable to retrieve journal entry from data store.",
                         sb);
 
                 if (log.isDebugEnabled())
                     log.debug("getSingleEntry: Exception is " + e1.getMessage() + '\n' + e1.toString());
             }
         }
     }
 
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void search(final UserContext uc, final int maxresults, final String bquery) {
         final StringBuffer sb = uc.getSb();
         ResultSet brs = null;
         String sql;
 
         if (uc.isAuthBlog())
             sql = "SELECT entry.subject AS subject, entry.body AS body, entry.date AS date, entry.id AS id, user.username AS username from entry, user, user_pref WHERE entry.uid = user.id AND entry.uid=user_pref.id AND user.username='" + uc.getBlogUser().getUserName() + "' AND ";
         else
             sql = "SELECT entry.subject AS subject, entry.body AS body, entry.date AS date, entry.id AS id, user.username AS username from entry, user, user_pref WHERE entry.uid = user.id AND entry.uid=user_pref.id AND user_pref.owner_view_only = 'N' AND entry.security=2 AND user.username='" + uc.getBlogUser().getUserName() + "' AND ";
 
         if (bquery != null && bquery.length() > 0) {
             try {
                 final BaseSearch b = new BaseSearch();
 
                 if (log.isDebugEnabled()) {
                     log.debug("Search base is: " + sql);
                     log.debug("Max results are: " + maxresults);
                     log.debug("Search query is: " + bquery);
                 }
 
                 b.setBaseQuery(sql);
                 b.setFields("subject body");
                 b.setMaxResults(maxresults);
                 b.setSortAscending("date");
                 brs = b.search(bquery);
 
                 sb.append("<h2><img src=\"/images/icon_search.gif\" alt=\"Search Blog\" style=\"float: left;\" /> Blog Search</h2>");
                 sb.append(endl);
 
                 if (brs == null) {
                     sb.append("<p>No items were found matching your search criteria.</p>");
                     sb.append(endl);
                 } else {
 
                     while (brs.next()) {
 
                         // Format the current time.
                         final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                         final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
                         final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
                         String curDate;
 
                         // Parse the previous string back into a Date.
                         final ParsePosition pos = new ParsePosition(0);
                         final java.util.Date currentDate = formatter.parse(brs.getString("date"), pos);
 
                         curDate = formatmydate.format(currentDate);
 
                         sb.append("<h2>");
                         sb.append(curDate);
                         sb.append("</h2>");
                         sb.append(endl);
 
 
                         sb.append("<div class=\"ebody\">");
                         sb.append(endl);
 
                         sb.append("<h3>");
                         sb.append("<span class=\"time\">");
                         sb.append(formatmytime.format(currentDate));
                         sb.append("</span> - <span class=\"subject\"><a href=\"/users/").append(brs.getString("username")).append("/entry/").append(brs.getString("id")).append("\">");
                         sb.append(Xml.cleanString(brs.getString("subject")));
                         sb.append("</a></span></h3> ");
                         sb.append(endl);
 
                         sb.append("<div class=\"ebody\">");
                         sb.append(endl);
                         sb.append(brs.getString("body"));
                         sb.append(endl);
                         sb.append("</div>");
                         sb.append(endl);
                     }
 
                     brs.close();
                 }
 
             } catch (Exception e1) {
                 log.error("Could not close database resultset on first attempt: " + e1.getMessage());
                 if (brs != null) {
                     try {
                         brs.close();
                     } catch (Exception e) {
                         log.error("Could not close database resultset: " + e.getMessage());
                     }
                 }
             }
         }
     }
 
     private static void getEntries(final UserContext uc, final int skip) {
         if (log.isDebugEnabled())
             log.debug("getEntries: Loading DAO");
 
         final StringBuffer sb = uc.getSb();
         final Collection entries;
 
         try {
             if (uc.isAuthBlog()) {
                 entries = EntryDAO.view(uc.getBlogUser().getUserName(), true, skip);  // should be true
 
                 if (log.isDebugEnabled())
                     log.debug("getEntries: User is logged in.");
             } else {
                 entries = EntryDAO.view(uc.getBlogUser().getUserName(), false, skip);
 
                 if (log.isDebugEnabled())
                     log.debug("getEntries: User is not logged in.");
             }
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
             String lastDate = "";
             String curDate;
 
             jumpmenu(skip, 20, entries.size() > 19, skip > 0, uc);
 
             if (log.isDebugEnabled())
                 log.debug("getEntries: Begin Iteration of records.");
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 if (curDate.compareTo(lastDate) != 0) {
                     sb.append("\t\t<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
                     lastDate = curDate;
                 }
 
                 sb.append(formatEntry(uc, o, currentDate, false));
             }
 
             jumpmenu(skip, 20, entries.size() > 19, skip > 0, uc);
 
         } catch (Exception e1) {
             WebError.Display("Error",
                     "Unable to retrieve journal entries from data store.",
                     sb);
 
             if (log.isDebugEnabled())
                 log.debug("getEntries: Exception is " + e1.getMessage() + '\n' + e1.toString());
         }
 
     }
 
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void jumpmenu(final int skip, final int offset, final boolean back, final boolean forward, final UserContext uc) {
         final StringBuffer sb = uc.sb;
         sb.append("\t\t<p>");
 
         if (back) {
             sb.append(" Go: <a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append("?skip=");
             sb.append((skip + offset));
             sb.append("\">older entries</a> ");
         }
 
         if (forward) {
             sb.append(" or <a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append("?skip=");
             sb.append((skip - offset));
             sb.append("\">forward</a>");
         }
         sb.append("</p>");
         sb.append(endl);
     }
 
     /**
      * Displays friends entries for a particular user.
      *
      * @param uc The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private static void getFriends(final UserContext uc) {
 
         if (log.isDebugEnabled())
             log.debug("getFriends: Load DAO.");
 
         final StringBuffer sb = uc.getSb();
         final Collection entries;
 
         if (uc.getAuthenticatedUser() != null)
             entries = EntryDAO.viewFriends(uc.getBlogUser().getUserId(), uc.getAuthenticatedUser().getUserId());
         else
             entries = EntryDAO.viewFriends(uc.getBlogUser().getUserId(), 0);
 
         sb.append("<h2>Friends</h2>");
         sb.append(endl);
 
         try {
             if (log.isDebugEnabled())
                 log.debug("getFriends: Init Date Parsers.");
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
             final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
             String lastDate = "";
             String curDate;
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             if (log.isDebugEnabled())
                 log.debug("getFriends: Number of entries " + entries.size());
 
             if (entries.isEmpty())
                 sb.append("<p>No friends entries found</p>.");
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 if (curDate.compareTo(lastDate) != 0) {
                     sb.append("<h2>");
                     sb.append(curDate);
                     sb.append("</h2>");
                     sb.append(endl);
                     lastDate = curDate;
                 }
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 final User p = new User(o.getUserName());
                 if (p.showAvatar()) {
                     sb.append("<img alt=\"avatar\" style=\"float: right\" src=\"/image?id=");
                     sb.append(o.getUserId());
                     sb.append("\"/>");
                     sb.append(endl);
                 }
 
                 sb.append("<h3>");
                 sb.append("<a href=\"/users/");
                 sb.append(o.getUserName());
                 sb.append("\" title=\"");
                 sb.append(o.getUserName());
                 sb.append("\">");
                 sb.append(o.getUserName());
                 sb.append("</a> ");
 
                 sb.append("<span class=\"time\">");
                 sb.append(formatmytime.format(currentDate));
                 sb.append("</span> - <span class=\"subject\">");
                 sb.append(Xml.cleanString(o.getSubject()));
                 sb.append("</span></h3> ");
                 sb.append(endl);
 
                 sb.append("<div class=\"ebody\">");
                 sb.append(endl);
 
                 // Keep this synced with getEntries()
                 if (o.getAutoFormat()) {
                     sb.append("<p>");
                     if (o.getBodyWithLinks().contains("\n"))
                         sb.append(StringUtil.replace(o.getBodyWithLinks(), '\n', "<br />"));
                     else if (o.getBody().contains("\r"))
                         sb.append(StringUtil.replace(o.getBodyWithLinks(), '\r', "<br />"));
                     else
                         // we do not have any "new lines" but it might be
                         // one long line.
                         sb.append(o.getBodyWithLinks());
 
                     sb.append("</p>");
                 } else {
                     sb.append(o.getBody());
                 }
 
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("<p>");
 
                 if (o.getSecurityLevel() == 0) {
                     sb.append("<span class=\"security\">security: ");
                     sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
                     sb.append("private");
                     sb.append("</span><br />");
                     sb.append(endl);
                 } else if (o.getSecurityLevel() == 1) {
                     sb.append("<span class=\"security\">security: ");
                     sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
                     sb.append("friends");
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getLocationId() > 0) {
                     sb.append("<span class=\"location\">location: ");
                     sb.append(o.getLocationName());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getMoodName().length() > 0 && o.getMoodId() != 12) {
                     final EmoticonTo emoto = EmoticonDao.get(1, o.getMoodId());
 
                     sb.append("<span class=\"mood\">mood: <img src=\"/images/emoticons/1/");
                     sb.append(emoto.getFileName());
                     sb.append("\" width=\"");
                     sb.append(emoto.getWidth());
                     sb.append("\" height=\"");
                     sb.append(emoto.getHeight());
                     sb.append("\" alt=\"");
                     sb.append(o.getMoodName());
                     sb.append("\" /> ");
                     sb.append(o.getMoodName());
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 if (o.getMusic().length() > 0) {
                     sb.append("<span class=\"music\">music: ");
                     sb.append(Xml.cleanString(o.getMusic()));
                     sb.append("</span><br />");
                     sb.append(endl);
                 }
 
                 sb.append("</p>");
                 sb.append(endl);
 
                 sb.append("<p>tags:");
                 for (final String s : o.getTags()) {
                     sb.append(" ");
                     sb.append(s);
                 }
                 sb.append("</p>");
                 sb.append(endl);
 
                 sb.append("<div>");
                 sb.append(endl);
                 sb.append("<table width=\"100%\"  border=\"0\">");
                 sb.append(endl);
                 sb.append("<tr>");
                 sb.append(endl);
 
                 if (uc.getAuthenticatedUser() != null && uc.getAuthenticatedUser().getUserId() == o.getUserId()) {
                     sb.append("<td width=\"30\"><a title=\"Edit Entry\" href=\"/#/entry/").append(o.getId());
                     sb.append("\"><i class=\"fa fa-pencil-square-o\"></i></a></td>");
                     sb.append(endl);
                     sb.append("<td width=\"30\"><a title=\"Delete Entry\" onclick=\"return confirmDelete()\"; href=\"/entry/delete.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><i class=\"fa fa-trash-o\"></i></a>");
                     sb.append("</td>");
                     sb.append(endl);
 
                     sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/add.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><i class=\"fa fa-heart\"></i></a></td>");
                     sb.append(endl);
                 } else if (uc.getAuthenticatedUser() != null) {
                     sb.append("<td width=\"30\"><a title=\"Add Favorite\" href=\"/favorite/add.h?entryId=");
                     sb.append(o.getId());
                     sb.append("\"><i class=\"fa fa-heart\"></i></a></td>");
                     sb.append(endl);
                 }
 
                 sb.append("<td><div style=\"float: right\"><a href=\"/users/").append(o.getUserName()).append("/entry/");
                 sb.append(o.getId());
                 sb.append("\" title=\"Link to this entry\">link</a> ");
                 sb.append('(');
 
                 switch (o.getCommentCount()) {
                     case 0:
                         break;
                     case 1:
                         sb.append("<a href=\"/comment/index.jsp?id=");
                         sb.append(o.getId());
                         sb.append("\" title=\"View Comment\">1 comment</a> | ");
                         break;
                     default:
                         sb.append("<a href=\"/comment/index.jsp?id=");
                         sb.append(o.getId());
                         sb.append("\" title=\"View Comments\">");
                         sb.append(o.getCommentCount());
                         sb.append(" comments</a> | ");
                 }
 
                 sb.append("<a href=\"/comment/add.jsp?id=");
                 sb.append(o.getId());
                 sb.append("\" title=\"Leave a comment on this entry\">comment on this</a>)");
 
                 sb.append("</div></td>");
                 sb.append(endl);
                 sb.append("</tr>");
                 sb.append(endl);
                 sb.append("</table>");
                 sb.append(endl);
                 sb.append("</div>");
                 sb.append(endl);
 
                 sb.append("</div>");
                 sb.append(endl);
             }
 
         } catch (Exception e1) {
             log.error(e1.getMessage());
             WebError.Display(" Error",
                     "Error retrieving the friends entries",
                     sb);
         }
 
     }
 
     /**
      * Prints the calendar for the year specified for months with journal entries.  Other months are not printed.
      *
      * @param year The year to print
      * @param uc   The UserContext we are working on including blog owner, authenticated user, and sb to write
      * @see com.justjournal.Cal
      * @see com.justjournal.CalMonth
      */
     private static void getCalendar(final int year,
                                     final UserContext uc) {
         StringBuffer sb = uc.getSb();
         final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
         int yearNow = calendarg.get(Calendar.YEAR);
 
         // print out header
         sb.append("<h2>Calendar: ");
         sb.append(year);
         sb.append("</h2>");
         sb.append(endl);
 
         sb.append("<p>The calendar lists months with journal entries.</p>");
         sb.append(endl);
 
         // BEGIN: YEARS
         sb.append("<p>");
 
         for (int i = yearNow; i >= uc.getBlogUser().getStartYear(); i--) {
 
             sb.append("<a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append('/');
             sb.append(i);
             sb.append("\">");
             sb.append(i);
             sb.append("</a> ");
 
             // just in case!
             if (i == 2002)
                 break;
         }
 
         sb.append("</p>");
         sb.append(endl);
         // END: YEARS
 
         // load ResultSet and display the calendar
         try {
 
             ResultSet RS;
 
             // are we logged in?
             RS = EntryDAO.ViewCalendarYear(year, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (RS == null) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
                 // we have calendar data!
                 final Cal mycal = new Cal(RS);
                 sb.append(mycal.render());
             }
 
         } catch (Exception e1) {
             WebError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
 
     }
 
     /**
      * Lists all of the journal entries for the month specified in the year specified.
      *
      * @param year  the year to display data for
      * @param month the month we want
      * @param uc    The UserContext we are working on including blog owner, authenticated user, and sb to write
      *              <p/>
      *              TODO: change to Entries DAO code
      */
     private static void getCalendarMonth(final int year,
                                          final int month,
                                          final UserContext uc) {
         StringBuffer sb = uc.getSb();
 
         sb.append("<h2>Calendar: ");
         sb.append(month);
         sb.append('/');
         sb.append(year);
         sb.append("</h2>");
         sb.append(endl);
 
         sb.append("<p>This page lists all of the journal entries for the month.</p>");
         sb.append(endl);
 
         try {
 
             final ResultSet RS;
             RS = EntryDAO.ViewCalendarMonth(year, month, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (RS == null) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
 
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("dd");
                 final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
 
                 String curDate;
                 String lastDate = "";
 
                 while (RS.next()) {
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(RS.getString("date"), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("<p><strong>").append(curDate).append("</strong></p>");
                         lastDate = curDate;
                     }
 
                     sb.append("<p><span class=\"time\">").append(formatmytime.format(currentDate)).append("</span> - <span class=\"subject\"><a href=\"");
 
                     /*TODO: fix bug where relative url is incorrect
                        Need to check if we are in a calendar state and
                        drop the extra parts on the request.
                        it is appended 08/02/08/02 etc. */
                     if (month < 10)
                         sb.append('0');
 
                     sb.append(month).append('/').append(curDate).append("\">").append(RS.getString("subject")).append("</a></span></p> ");
                     sb.append(endl);
                 }
             }
 
         } catch (Exception e1) {
             WebError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
     }
 
     /**
      * Print a mini calendar for the current month with blog entries counts for given days in HTML.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void getCalendarMini(UserContext uc) {
         try {
             final StringBuffer sb = uc.getSb();
             final ResultSet RS;
             final Calendar cal = new GregorianCalendar(java.util.TimeZone.getDefault());
             int year = cal.get(Calendar.YEAR);
             int month = cal.get(Calendar.MONTH) + 1; // zero based
 
             RS = EntryDAO.ViewCalendarMonth(year, month, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (RS == null) {
                 sb.append("\t<!-- could not render calendar -->");
                 sb.append(endl);
             } else {
                 final Cal mycal = new Cal(RS);
                 mycal.setBaseUrl("/users/" + uc.getBlogUser().getUserName() + '/');
                 sb.append(mycal.renderMini());
             }
 
         } catch (Exception ignored) {
 
         }
     }
 
     /**
      * Print a list of tags in HTML that the blog owner is using.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void getTagMini(final UserContext uc) {
         final StringBuffer sb = uc.getSb();
         Tag tag;
         final ArrayList<Tag> tags = EntryDAO.getUserTags(uc.getBlogUser().getUserId());
         int largest = 0;
         int smallest = 10;
         int cutSmall;
         int cutLarge;
 
         for (final Tag tag1 : tags) {
             tag = tag1;
             if (tag.getCount() > largest)
                 largest = tag.getCount();
 
             if (tag.getCount() < smallest)
                 smallest = tag.getCount();
         }
 
         cutSmall = largest / 3;
         cutLarge = cutSmall * 2;
 
         sb.append("\t<div class=\"menuentity\" id=\"usertags\" style=\"padding-top: 10px;\">\n\t\t<strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\"><i class=\"fa fa-tags\"></i> Tags</strong>\n\t\t<p style=\"padding-left: 0; margin-left: 0;\">\n");
         for (final Tag tag1 : tags) {
             tag = tag1;
             sb.append("<a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append("/tag/");
             sb.append(tag.getName());
             sb.append("\" class=\"");
             if (tag.getCount() > cutLarge)
                 sb.append("TagCloudLarge");
             else if (tag.getCount() < cutSmall)
                 sb.append("TagCloudSmall");
             else
                 sb.append("TagCloudMedium");
             sb.append("\">");
             sb.append(tag.getName());
             sb.append("</a>");
             sb.append(endl);
         }
         sb.append("\t\t</p>\n\t</div>");
         sb.append(endl);
     }
 
     /**
      * Print a list of links the user has added to their blog in HTML.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void getUserLinks(final UserContext uc) {
         log.debug("getUserLinks(): Init and load collection");
         StringBuffer sb = uc.getSb();
         UserLinkTo link;
         Collection links = UserLinkDao.view(uc.getBlogUser().getUserId());
 
         if (!links.isEmpty()) {
             sb.append("\t<div class=\"menuentity\" id=\"userlinks\" style=\"padding-top: 10px;\">\n\t\t<strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\"><i class=\"fa fa-external-link-square\"></i> Links</strong>\n\t\t<ul style=\"padding-left: 0; margin-left: 0;\">\n");
             final Iterator itr = links.iterator();
             for (int i = 0, n = links.size(); i < n; i++) {
                 link = (UserLinkTo) itr.next();
                 sb.append("\t\t\t<li><a href=\"").append(link.getUri()).append("\" title=\"").append(link.getTitle()).append("\">").append(link.getTitle()).append("</a></li>");
                 sb.append(endl);
             }
             sb.append("\t\t</ul>\n\t</div>");
             sb.append(endl);
         }
     }
 
     /**
      * Print a short list of recent blog entries in HTML
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void getUserRecentEntries(final UserContext uc) {
         if (log.isDebugEnabled())
             log.debug("getUserRecentEntries: Loading DAO");
 
         StringBuffer sb = uc.getSb();
         final Collection entries;
         final int maxrecent = 5;
 
         try {
             entries = EntryDAO.view(uc.getBlogUser().getUserName(), uc.isAuthBlog(), 0);
 
             if (log.isDebugEnabled())
                 log.debug("getUserRecentEntries: Begin Iteration of records.");
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             sb.append("\t<div class=\"menuentity\" id=\"userRecentEntries\">\n<strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\">Recent Entries</strong>\n");
             sb.append("\t\t<ul style=\"padding-left: 0; margin-left: 0;\">");
             sb.append(endl);
 
             int n = entries.size();
             if (maxrecent < n) {
                 n = maxrecent;
             }
             for (int i = 0; i < n; i++) {
                 o = (EntryTo) itr.next();
                 sb.append("\t\t\t<li><a href=\"/users/");
                 sb.append(uc.getBlogUser().getUserName());
                 sb.append("/entry/");
                 sb.append(o.getId());
                 sb.append("\" title=\"");
                 sb.append(Xml.cleanString(o.getSubject()));
                 sb.append("\">");
                 sb.append(Xml.cleanString(o.getSubject()));
                 sb.append("</a></li>");
                 sb.append(endl);
 
             }
             sb.append("\t\t</ul>");
             sb.append(endl);
             sb.append("\t</div>");
             sb.append(endl);
         } catch (Exception e) {
             log.error(e.getMessage());
         }
     }
 
     /**
      * Generates all of the HTML to display journal entires for a particular day specified in the url.
      *
      * @param year  the year to display
      * @param month the month we want to look at
      * @param day   the day we are interested in
      * @param uc    The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private static void getCalendarDay(final int year,
                                        final int month,
                                        final int day,
                                        final UserContext uc) {
 
         StringBuffer sb = uc.getSb();
 
         // sb.append("<h2>Calendar: " + day + "/" + month + "/" + year + "</h2>" );
 
         sb.append("<p>Lists all of the journal entries for the day.</p>");
         sb.append(endl);
 
         try {
 
             final Collection entries;
              entries = EntryDAO.ViewCalendarDay(year, month, day, uc.getBlogUser().getUserName(), uc.isAuthBlog());
 
             if (entries == null || entries.size() == 0) {
                 sb.append("<p>Calendar data not available.</p>");
                 sb.append(endl);
             } else {
                 final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
                 final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
                 String lastDate = "";
                 String curDate;
 
                 /* Iterator */
                 EntryTo o;
                 final Iterator itr = entries.iterator();
 
                 for (int i = 0, n = entries.size(); i < n; i++) {
                     o = (EntryTo) itr.next();
 
                     // Parse the previous string back into a Date.
                     final ParsePosition pos = new ParsePosition(0);
                     final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                     curDate = formatmydate.format(currentDate);
 
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("\t\t<h2>");
                         sb.append(curDate);
                         sb.append("</h2>");
                         sb.append(endl);
                         lastDate = curDate;
                     }
 
                     sb.append(formatEntry(uc, o, currentDate, false));
                 }
             }
 
         } catch (Exception e1) {
             WebError.Display(" Error",
                     "An error has occured rendering calendar.",
                     sb);
         }
     }
 
     /**
      * Print a list of months and years that users have blogged in as a history breadcrumb to access the calendar list
      * of blog entries in HTML.
      *
      * @param uc User Context
      */
     @SuppressWarnings("MismatchedQueryAndUpdateOfStringBuilder")
     private static void getArchive(final UserContext uc) {
         final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
         int yearNow = calendarg.get(Calendar.YEAR);
         StringBuffer sb = uc.getSb();
 
         // BEGIN: YEARS
         sb.append("\t<div class=\"menuentity\" id=\"archive\" style=\"padding-top: 10px;\"><strong style=\"text-transform: uppercase; letter-spacing: 2px; border: 0 none; border-bottom: 1px; border-style: dotted; border-color: #999999; margin-bottom: 5px; width: 100%; font-size: 10px;\">Archive</strong><ul style=\"padding-left: 0; margin-left: 0;\">");
 
         for (int i = yearNow; i >= uc.getBlogUser().getStartYear(); i--) {
 
             sb.append("<li><a href=\"/users/");
             sb.append(uc.getBlogUser().getUserName());
             sb.append('/');
             sb.append(i);
             sb.append("\">");
             sb.append(i);
             sb.append(" (");
             try {
                 sb.append(EntryDAO.calendarCount(i, uc.getBlogUser().getUserName()));
             } catch (Exception e) {
                 log.error("getArchive: could not fetch count for " + uc.getBlogUser().getUserName() + ": " + i + e.getMessage());
                 sb.append("0");
             }
             sb.append(")</a></li> ");
 
             // just in case!
             if (i == 2002)
                 break;
         }
 
         sb.append("</ul>");
         sb.append(endl);
         sb.append("</div>");
         sb.append(endl);
         // END: YEARS
     }
 
     /**
      * Handles requests for syndication content (RSS). Only returns public journal entries for the specified user.
      *
      * @param uc The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private static void getRSS(final UserContext uc) {
         // Create an RSS object, set the required
         // properites (title, description language, url)
         // and write it to the sb output.
         try {
             Rss rss = new Rss();
 
             final java.util.GregorianCalendar calendar = new java.util.GregorianCalendar();
             calendar.setTime(new java.util.Date());
 
             rss.setTitle(uc.getBlogUser().getUserName());
             rss.setLink("http://www.justjournal.com/users/" + uc.getBlogUser().getUserName());
             rss.setSelfLink("http://www.justjournal.com/users/" + uc.getBlogUser().getUserName() + "/rss");
             rss.setDescription("Just Journal for " + uc.getBlogUser().getUserName());
             rss.setLanguage("en-us");
             rss.setCopyright("Copyright " + calendar.get(Calendar.YEAR) + ' ' + uc.getBlogUser().getFirstName());
             rss.setWebMaster("webmaster@justjournal.com (Lucas)");
             // RSS advisory board format
             rss.setManagingEditor(uc.getBlogUser().getEmailAddress() + " (" + uc.getBlogUser().getFirstName() + ")");
             rss.populate(EntryDAO.view(uc.getBlogUser().getUserName(), false));
             uc.getSb().append(rss.toXml());
         } catch (Exception e) {
             log.error(e.getMessage());
             // oops we goofed somewhere.  Its not in the original spec
             // how to handle error conditions with rss.
             // html back isn't good, but what do we do?
             WebError.Display("RSS ERROR", "Unable to retrieve RSS content.", uc.getSb());
         }
     }
 
     /**
      * Handles requests for syndication content (Atom). Only returns public journal entries for the specified user.
      *
      * @param uc The UserContext we are working on including blog owner, authenticated user, and sb to write
      */
     private static void getAtom(final UserContext uc) {
         // Create an Atom object, set the required
         // properites (title, description language, url)
         // and write it to the sb output.
         try {
             AtomFeed atom = new AtomFeed();
 
             final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
             calendarg.setTime(new java.util.Date());
 
             atom.setUserName(uc.getBlogUser().getUserName());
             atom.setAlternateLink("http://www.justjournal.com/users/" + uc.getBlogUser().getUserName());
             atom.setAuthorName(uc.getBlogUser().getFirstName());
             atom.setUpdated(calendarg.toString());
             atom.setTitle(uc.getBlogUser().getJournalName());
             atom.setId("http://www.justjournal.com/users/" + uc.getBlogUser().getUserName() + "/atom");
             atom.setSelfLink("/users/" + uc.getBlogUser().getUserName() + "/atom");
             atom.populate(EntryDAO.view(uc.getBlogUser().getUserName(), false));
             uc.getSb().append(atom.toXml());
         } catch (Exception e) {
             // oops we goofed somewhere.  Its not in the original spec
             // how to handle error conditions with atom.
             // html back isn't good, but what do we do?
             WebError.Display("Atom ERROR", "Unable to retrieve Atom content.", uc.getSb());
         }
     }
 
     /**
      * List the pictures associated with a blog in RSS.  This should be compatible with iPhoto.
      *
      * @param uc User Context
      */
     private static void getPicturesRSS(final UserContext uc) {
         // Create an RSS object, set the required
         // properites (title, description language, url)
         // and write it to the sb output.
         try {
             final Rss rss = new Rss();
 
             final java.util.GregorianCalendar calendarg = new java.util.GregorianCalendar();
             calendarg.setTime(new java.util.Date());
 
             rss.setTitle(uc.getBlogUser().getUserName() + "\'s pictures");
             rss.setLink("http://www.justjournal.com/users/" + uc.getBlogUser().getUserName() + "/pictures");
             rss.setSelfLink("http://www.justjournal.com/users/" + uc.getBlogUser().getUserName() + "/pictures/rss");
             rss.setDescription("Just Journal Pictures for " + uc.getBlogUser().getUserName());
             rss.setLanguage("en-us");
             rss.setCopyright("Copyright " + calendarg.get(Calendar.YEAR) + ' ' + uc.getBlogUser().getFirstName());
             rss.setWebMaster("webmaster@justjournal.com (Luke)");
             // RSS advisory board format
             rss.setManagingEditor(uc.getBlogUser().getEmailAddress() + " (" + uc.getBlogUser().getFirstName() + ")");
             rss.populateImageList(uc.getBlogUser().getUserId(), uc.getBlogUser().getUserName());
             uc.getSb().append(rss.toXml());
         } catch (Exception e) {
             // oops we goofed somewhere.  Its not in the original spec
             // how to handle error conditions with rss.
             // html back isn't good, but what do we do?
             WebError.Display("RSS ERROR", "Unable to retrieve RSS content.", uc.getSb());
         }
     }
 
     /* TODO: finish this */
     private static void getTags(final UserContext uc, String tag) {
         StringBuffer sb = uc.getSb();
         final Collection entries;
 
         try {
             if (uc.isAuthBlog()) {
                 entries = EntryDAO.viewAll(uc.getBlogUser().getUserName(), true);  // should be true
 
                 if (log.isDebugEnabled())
                     log.debug("getTags: User is logged in.");
             } else {
                 entries = EntryDAO.viewAll(uc.getBlogUser().getUserName(), false);
 
                 if (log.isDebugEnabled())
                     log.debug("getTags: User is not logged in.");
             }
 
             // Format the current time.
             final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm");
             final SimpleDateFormat formatmydate = new SimpleDateFormat("EEE, d MMM yyyy");
 
             String lastDate = "";
             String curDate;
 
             if (log.isDebugEnabled())
                 log.debug("getTags: Begin Iteration of records.");
 
             /* Iterator */
             EntryTo o;
             final Iterator itr = entries.iterator();
 
             for (int i = 0, n = entries.size(); i < n; i++) {
                 o = (EntryTo) itr.next();
 
                 // Parse the previous string back into a Date.
                 final ParsePosition pos = new ParsePosition(0);
                 final java.util.Date currentDate = formatter.parse(o.getDateTime().toString(), pos);
 
                 curDate = formatmydate.format(currentDate);
 
                 Collection entryTags = o.getTags();
 
                 if (entryTags.contains(tag.toLowerCase())) {
                     if (curDate.compareTo(lastDate) != 0) {
                         sb.append("\t\t<h2>");
                         sb.append(curDate);
                         sb.append("</h2>");
                         sb.append(endl);
                         lastDate = curDate;
                     }
 
                     sb.append(formatEntry(uc, o, currentDate, false));
                 }
             }
         } catch (Exception e1) {
             WebError.Display("Error",
                     "Unable to retrieve journal entries from data store.",
                     sb);
 
             if (log.isDebugEnabled())
                 log.debug("getTags: Exception is " + e1.getMessage() + '\n' + e1.toString());
         }
     }
 
     /**
      * Handles the HTTP <code>GET</code> method.
      *
      * @param request  servlet request
      * @param response servlet response
      * @throws ServletException
      * @throws java.io.IOException
      */
     @Override
     protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
             throws ServletException, java.io.IOException {
         processRequest(request, response);
 
     }
 
     /**
      * Returns a short description of the servlet.
      *
      * @return description string
      */
     @Override
     public String getServletInfo() {
         return "view journal entries";
     }
 
     /**
      * Format a blog entry in HTML
      *
      * @param uc          User Context
      * @param o           Entry to format
      * @param currentDate Date to format (of the entry)
      * @param single      Single blog entries are formatted differently
      * @return HTML formatted entry
      */
     protected static String formatEntry(final UserContext uc, final EntryTo o, final Date currentDate, boolean single) {
         final StringBuilder sb = new StringBuilder();
         final SimpleDateFormat formatmytime = new SimpleDateFormat("h:mm a");
 
         sb.append("\t\t<div class=\"ebody\">");
         sb.append(endl);
 
         if (single) {
             sb.append("\t\t\t<h3>");
             sb.append("<span class=\"time\">");
             sb.append(formatmytime.format(currentDate));
             sb.append("</span> - <span class=\"subject\"><a name=\"#e");
             sb.append(o.getId());
             sb.append("\">");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("</a></span></h3> ");
             sb.append(endl);
 
             sb.append("<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" \n");
             sb.append("xmlns:dc=\"http://purl.org/dc/elements/1.1/\" \n");
             sb.append("xmlns:trackback=\"http://madskills.com/public/xml/rss/module/trackback/\">\n");
             sb.append("\t<rdf:Description ");
             sb.append("rdf:about=\"");
             sb.append("http://www.justjournal.com/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("#e");
             sb.append(o.getId());
             sb.append("\" dc:identifier=\"");
             sb.append("http://www.justjournal.com/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("#e");
             sb.append(o.getId());
             sb.append("\" dc:title=\"");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("\" ");
             sb.append("trackback:ping=\"http://www.justjournal.com/trackback?entryID=");
             sb.append(o.getId());
             sb.append("\" />\n");
             sb.append("</rdf:RDF>\n");
         } else {
             sb.append("\t\t\t<h3>");
             sb.append("<span class=\"time\">");
             sb.append(formatmytime.format(currentDate));
             sb.append("</span> - <span class=\"subject\">");
             sb.append("<a href=\"/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("\" rel=\"bookmark\" title=\"");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("\">");
             sb.append(Xml.cleanString(o.getSubject()));
             sb.append("</a></span></h3> ");
             sb.append(endl);
         }
 
         sb.append("\t\t\t<div class=\"ebody\">");
         sb.append(endl);
 
 
         /*
            autoformat controls whether new lines should be
            converted to br's.  If someone used html, we don't want autoformat!
            We handle Windows/UNIX with the \n case and Mac OS Classic with \r
          */
         if (o.getAutoFormat()) {
 
             sb.append("\t\t\t\t<p>");
             if (o.getBodyWithLinks().contains("\n"))
                 sb.append(StringUtil.replace(o.getBodyWithLinks(), '\n', "<br />"));
             else if (o.getBody().contains("\r"))
                 sb.append(StringUtil.replace(o.getBodyWithLinks(), '\r', "<br />"));
             else
                 // we do not have any "new lines" but it might be
                 // one long line.
                 sb.append(o.getBodyWithLinks());
 
             sb.append("</p>");
         } else {
             sb.append(o.getBody());
         }
 
         sb.append(endl);
         sb.append("\t\t\t</div>");
         sb.append(endl);
 
         sb.append("\t\t\t<p>");
 
         if (o.getSecurityLevel() == 0) {
             sb.append("<span class=\"security\">security: ");
             sb.append("<img src=\"/img/icon_private.gif\" alt=\"private\" /> ");
             sb.append("private");
             sb.append("</span><br />");
             sb.append(endl);
         } else if (o.getSecurityLevel() == 1) {
             sb.append("\t\t\t<span class=\"security\">security: ");
             sb.append("<img src=\"/img/icon_protected.gif\" alt=\"friends\" /> ");
             sb.append("friends");
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         if (o.getLocationId() > 0) {
             sb.append("\t\t\t<span class=\"location\">location: ");
             sb.append(o.getLocationName());
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         if (o.getMoodName().length() > 0 && o.getMoodId() != 12) {
             final EmoticonTo emoto = EmoticonDao.get(1, o.getMoodId());
 
             sb.append("\t\t\t<span class=\"mood\">mood: <img src=\"/images/emoticons/1/");
             sb.append(emoto.getFileName());
             sb.append("\" width=\"");
             sb.append(emoto.getWidth());
             sb.append("\" height=\"");
             sb.append(emoto.getHeight());
             sb.append("\" alt=\"");
             sb.append(o.getMoodName());
             sb.append("\" /> ");
             sb.append(o.getMoodName());
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         if (o.getMusic().length() > 0) {
             sb.append("\t\t\t<span class=\"music\">music: ");
             sb.append(Xml.cleanString(o.getMusic()));
             sb.append("</span><br />");
             sb.append(endl);
         }
 
         sb.append("\t\t\t</p>");
         sb.append(endl);
 
         Collection<String> ob = o.getTags();
         if (ob.size() > 0) {
             sb.append("<p>tags:");
             for (final String tagname : ob) {
                 sb.append(" ");
                 sb.append("<a href=\"/users/");
                 sb.append(uc.getBlogUser().getUserName());
                 sb.append("/tag/");
                 sb.append(tagname);
                 sb.append("\">");
                 sb.append(tagname);
                 sb.append("</a>");
             }
             sb.append("</p>");
             sb.append(endl);
         }
 
         sb.append("\t\t\t<div>");
         sb.append(endl);
         sb.append("\t\t\t\t<table width=\"100%\"  border=\"0\">");
         sb.append(endl);
         sb.append("\t\t\t\t\t<tr>");
         sb.append(endl);
 
         if (uc.isAuthBlog()) {
             sb.append("<td style=\"width: 30px\"><a title=\"Edit Entry\" href=\"/#/entry/");
             sb.append(o.getId());
             sb.append("\"><i class=\"fa fa-pencil-square-o\"></i></a></td>");
             sb.append(endl);
             sb.append("<td style=\"width: 30px\"><a title=\"Delete Entry\" onclick=\"return confirmDelete()\"; href=\"/entry/delete.h?entryId=");
             sb.append(o.getId());
             sb.append("\"><i class=\"fa fa-trash-o\"></i></a>");
             sb.append("</td>");
             sb.append(endl);
 
             sb.append("<td style=\"width: 30px\"><a title=\"Add Favorite\" href=\"/favorite/add.h?entryId=");
             sb.append(o.getId());
             sb.append("\"><i class=\"fa fa-heart\"></i></a></td>");
             sb.append(endl);
         }
 
         if (single) {
             sb.append("<td><div align=\"right\">");
             if (o.getSecurityLevel() == 2) {
                 sb.append("<iframe src=\"https://www.facebook.com/plugins/like.php?href=http://www.justjournal.com/users/").append(o.getUserName()).append("/entry/");
                 sb.append(o.getId());
                 sb.append("\" scrolling=\"no\" frameborder=\"0\" style=\"border:none; width:450px; height:80px\"></iframe>");
 
             }
             sb.append("</div></td>");
 
         } else {
 
             sb.append("<td><div style=\"float: right\"><a href=\"/users/").append(o.getUserName()).append("/entry/");
             sb.append(o.getId());
             sb.append("\" title=\"Link to this entry\"><i class=\"fa fa-external-link\"></i></a> ");
 
             sb.append('(');
 
             switch (o.getCommentCount()) {
                 case 0:
                     break;
                 case 1:
                     sb.append("<a href=\"/comment/index.jsp?id=");
                     sb.append(o.getId());
                     sb.append("\" title=\"View Comment\">1 comment</a> | ");
                     break;
                 default:
                     sb.append("<a href=\"/comment/index.jsp?id=");
                     sb.append(o.getId());
                     sb.append("\" title=\"View Comments\">");
                     sb.append(o.getCommentCount());
                     sb.append(" comments</a> | ");
             }
 
             sb.append("<a href=\"/comment/add.jsp?id=");
             sb.append(o.getId());
             sb.append("\" title=\"Leave a comment on this entry\"><i class=\"fa fa-comment-o\"></i></a>)");
             sb.append("\t\t\t\t\t\t</div></td>");
             sb.append(endl);
         }
 
         sb.append("\t\t\t\t\t</tr>");
         sb.append(endl);
         sb.append("\t\t\t\t</table>");
         sb.append(endl);
         sb.append("\t\t\t</div>");
         sb.append(endl);
 
         if (single) {
             Collection comments = CommentDao.list(o.getId());
 
             sb.append("<div class=\"commentcount\">");
             sb.append(o.getCommentCount());
             sb.append(" comments</div>\n");
 
             sb.append("<div class=\"rightflt\">");
             sb.append("<a href=\"add.jsp?id=").append(o.getId()).append("\" title=\"Add Comment\">Add Comment</a></div>\n");
 
             CommentTo co;
             final Iterator itr = comments.iterator();
 
             for (int i = 0, n = comments.size(); i < n; i++) {
                 co = (CommentTo) itr.next();
 
                 sb.append("<div class=\"comment\">\n");
                 sb.append("<div class=\"chead\">\n");
                 sb.append("<h3><span class=\"subject\">");
                 sb.append(Xml.cleanString(co.getSubject()));
                 sb.append("</span></h3>\n");
                 sb.append("<img src=\"../images/userclass_16.png\" alt=\"user\"/>");
                 sb.append("<a href=\"../users/");
                 sb.append(co.getUserName());
                 sb.append("\" title=\"");
                 sb.append(co.getUserName());
                 sb.append("\">");
                 sb.append(co.getUserName());
                 sb.append("</a>\n");
 
                 sb.append("<br/><span class=\"time\">");
                 sb.append(co.getDate().toPubDate());
                 sb.append("</span>\n");
 
 
                 if (uc.getAuthenticatedUser().getUserName().equalsIgnoreCase(co.getUserName())) {
                     sb.append("<br/><span class=\"actions\">\n");
                     sb.append("<a href=\"edit.h?commentId=");
                     sb.append(co.getId());
                     sb.append("\" title=\"Edit Comment\">");
                     sb.append("     <i class=\"fa fa-pencil-square-o\"></i>");
                     sb.append("</a>\n");
 
                     sb.append("<a href=\"delete.h?commentId=");
                     sb.append(co.getId());
                     sb.append("\" title=\"Delete Comment\">");
                     sb.append("<i class=\"fa fa-trash-o\"></i>");
                     sb.append("</a>\n");
                     sb.append("</span>\n");
                 }
                 sb.append("</div>\n");
 
                 sb.append("<p>");
                 sb.append(Xml.cleanString(o.getBody()));
                 sb.append("</p>\n</div>\n");
             }
         }
 
         sb.append("\t\t</div>");
         sb.append(endl);
 
         return sb.toString();
     }
 
     /**
      * Represent the blog user and authenticated user in one package along with the output buffer.
      */
     @SuppressWarnings({"InstanceVariableOfConcreteClass"})
     private class UserContext {
         private StringBuffer sb;        // the output buffer.
         private User blogUser;          // the blog owner
         private User authenticatedUser; // the logged in user
 
         /**
          * Default constructor for User Context.  Creates a usable instance.
          *
          * @param sb       Output buffer
          * @param blogUser blog owner
          * @param authUser logged in user
          */
         UserContext(final StringBuffer sb, final User blogUser, final User authUser) {
             this.blogUser = blogUser;
             this.authenticatedUser = authUser;
             this.sb = sb;
         }
 
         /**
          * Retrieve the output buffer
          *
          * @return output string buffer
          */
         public StringBuffer getSb() {
             return sb;
         }
 
         /**
          * Retrieve the blog owner
          *
          * @return blog owner
          */
         public User getBlogUser() {
             return blogUser;
         }
 
         /**
          * Retrieve the authenticated aka logged in user.
          *
          * @return logged in user.
          */
         public User getAuthenticatedUser() {
             return authenticatedUser;
         }
 
         /**
          * Check to see if the authenticated user is the blog owner also. Used for private information.
          *
          * @return true if blog owner = auth owner
          */
         public boolean isAuthBlog() {
             return authenticatedUser != null && blogUser != null
                     && authenticatedUser.getUserName().compareTo(blogUser.getUserName()) == 0;
         }
     }
 }
