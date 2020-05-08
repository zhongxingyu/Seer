 /* A UI subcomponent to insert a wiki version as a Freesite.
  *
  * Copyright (C) 2010, 2011 Darrell Karbott
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public
  * License as published by the Free Software Foundation; either
  * version 2.0 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * Author: djk@isFiaD04zgAgnrEC5XJt1i4IE7AkNPqhBG5bONi6Yks
  *
  *  This file was developed as component of
  * "fniki" (a wiki implementation running over Freenet).
  */
 
 package fniki.wiki.child;
 
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 
 import static ys.wikiparser.Utils.*;
 import static fniki.wiki.HtmlUtils.*;
 
 import fniki.wiki.ArchiveManager;
 import fniki.wiki.ChildContainer;
 import fniki.wiki.ChildContainerException;
 import fniki.wiki.Validations;
 import fniki.wiki.WikiContext;
 
 import wormarc.IOUtil;
 
 public class InsertingFreesite extends AsyncTaskContainer {
     private String mUri;
     private String mThemeName;
 
     public InsertingFreesite(ArchiveManager archiveManager) {
         super(archiveManager);
     }
 
     private String buildThemeOptionsHtml() {
         StringBuilder sb = new StringBuilder();
         for (String theme :  mArchiveManager.getSiteThemes()) {
             if (!Validations.isValidThemeName(theme)) {
                 continue;
             }
             sb.append("<option value=\"");
             sb.append(theme); // Should be ok because of checks above.
             sb.append("\">");
             sb.append(escapeHTML(theme));
             sb.append("</option>\n");
         }
         return sb.toString();
     }
     private String getFormHtml(WikiContext context) {
         String template = null;
         try {
             // NOTE: There is CSS in this template. Keep it in sync with the add_header.css file.
             //
             // IMPORTANT: Only multipart/form-data encoding works in plugins.
             // IMPORTANT: Must be multipart/form-data even for standalone because
             //            the Freenet ContentFilter rewrites the encoding in all forms
             //            to this value.
             template = IOUtil.readUtf8StringAndClose(SettingConfig.class.getResourceAsStream("/freesite_form.html"));
         } catch (IOException ioe) {
             return "Couldn't load freesite_form.html template from jar???";
         }
 
         String formAction = context.makeLink("/" + context.getPath());
         String siteName = mArchiveManager.getBissName();
         String formPassword = context.getString("form_password", "FORM_PASSWORD_NOT_SET");
 
         return String.format(template,
                              formAction,  // %1$s
                              siteName,    // %2$s
                              formPassword, // %3$s
                              buildThemeOptionsHtml(), // %4$s
                              "" // %5s, optional private key, never displayed
                              );
     }
 
     private void handleThemeUpload(WikiContext context) throws ChildContainerException, IOException {
         mArchiveManager.loadSiteTheme(context.getQuery().get("upload.filename"),
                                       context.getQuery().getBytes("upload"));
         sendRedirect(context, context.getPath());
     }
 
     private void setupFromFormParams(WikiContext context) throws ChildContainerException {
         String sitename = context.getQuery().get("sitename");
         if (sitename.length() == 0) {
             // Empty sitename not allowed.
             sendRedirect(context, context.getPath());
         }
 
         if (context.getQuery().get("keytype").equals("chk")) {
             mUri = "CHK@";
         } else {
            if (context.getQuery().get("sitekey") != null) {
                 // Support optional private key.
                 mUri = "USK" + context.getQuery().get("sitekey").substring(3) +
                 sitename + "/0/";
             } else {
                 mUri = "USK" + mArchiveManager.getPrivateSSK().substring(3) +
                 sitename + "/0/";
             }
         }
         mThemeName = context.getQuery().get("theme");
     }
 
     public String getHtml(WikiContext context) throws ChildContainerException {
         try {
             if (context.getAction().toLowerCase().equals("confirm")) {
                 setupFromFormParams(context);
                 startTask();
                 sendRedirect(context, context.getPath());
                 return "Unreachable code";
             } else  if (context.getAction().toLowerCase().equals("sentfile")) {
                 handleThemeUpload(context);
                 sendRedirect(context, context.getPath());
             }
 
             boolean showBuffer = false;
             boolean showUri = false;
             String confirmTitle = null;
             String cancelTitle = null;
             String title = null;
             switch (getState()) {
             case STATE_WORKING:
                 showBuffer = true;
                 title = "Inserting Freesite: " + mUri;;
                 cancelTitle = "Cancel";
                 break;
 
             case STATE_WAITING:
                 setTitle("Insert Freesite");
                 return getFormHtml(context);
 
             case STATE_SUCCEEDED:
                 showBuffer = true;
                 title = "Freesite Inserted: " + mUri;
                 cancelTitle = "Done";
                 break;
             case STATE_FAILED:
                 showBuffer = true;
                 title = "Freesite Insert Failed";
                 cancelTitle = "Done";
                 break;
             }
 
             setTitle(title);
 
             StringWriter buffer = new StringWriter();
             PrintWriter body = new PrintWriter(buffer);
 
             if (getState() == STATE_WORKING || getState() == STATE_SUCCEEDED) {
                 if (getState() == STATE_WORKING) {
                     title = "Inserting Freesite:"; // Don't put full uri in header
                 } else {
                     title = "Inserted Freesite.";
                 }
             }
             body.println("<h3>" + escapeHTML(title) + "</h3>");
             if (showUri) {
                 body.println(escapeHTML("Insert Freesite: " + mUri));
             } else if (getState() == STATE_WORKING || getState() == STATE_SUCCEEDED) {
                 body.println(escapeHTML(mUri));
             }
 
             if (showBuffer) {
                 body.println("<pre>");
                 body.print(escapeHTML(getOutput()));
                 body.println("</pre>");
             }
 
             addButtonsHtml(context, body, confirmTitle, cancelTitle);
 
             body.close();
             return buffer.toString();
 
         } catch (IOException ioe) {
             context.logError("Inserting Freesite", ioe);
             context.raiseServerError("InsertingFreesite.handle coding error. Sorry :-(");
             return "unreachable code";
         }
     }
 
     public boolean doWork(PrintStream out) throws Exception {
         if (mUri == null) {
             out.println("The insert uri isn't set");
             return false;
         }
 
         try {
             String msg = "";
             out.println("Inserting. Please be patient...");
             mArchiveManager.setSiteTheme(mThemeName);
             String requestUri = mArchiveManager.insertSite(mUri, out);
             out.println("Inserted: " + requestUri);
             return true;
         } catch (Exception e) {
             out.println("Insert failed from background thread: " + e.getMessage());
             return false;
         }
     }
 
     public void entered(WikiContext context) {
         mUri = null;
     }
 }
