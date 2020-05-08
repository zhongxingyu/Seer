 /* A base class for common parts of FreenetWikiTextParser.ParserDelegate implementations.
  *
  * INTENT: I did this so code could be shared between the live wiki and html dumping.
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
 
 package fniki.wiki;
 
 import static ys.wikiparser.Utils.*; // DCI: clean up
 
 import java.io.IOException;
 import java.util.List;
 
 import wormarc.FileManifest;
 
 import static fniki.wiki.HtmlUtils.*;
 import static fniki.wiki.Validations.*;
 
 public abstract class WikiParserDelegate implements FreenetWikiTextParser.ParserDelegate {
     final protected ArchiveManager mArchiveManager;
 
     public WikiParserDelegate(ArchiveManager archiveManager) {
         mArchiveManager = archiveManager;
     }
 
     protected abstract String getContainerPrefix();
     protected abstract boolean getFreenetLinksAllowed();
     protected abstract boolean getImagesAllowed();
     protected abstract String makeLink(String containerRelativePath);
     protected abstract String makeFreenetLink(String uri);
 
     protected boolean processedLocalChangesMacro(StringBuilder sb, String text) {
         try {
             FileManifest.Changes changes =  mArchiveManager.getLocalChanges();
             if (changes.isUnmodified()) {
                 sb.append("<br>No local changes.<br>");
                 return true;
             }
              // Should never be reached while dumping an existing archive to html.
             appendChangesHtml(changes, getContainerPrefix(), sb);
         } catch (IOException ioe) {
             sb.append("{ERROR PROCESSING LOCALCHANGES MACRO}");
         }
         return true;
     }
 
     protected boolean processedRebasedChangesMacro(StringBuilder sb, String text) {
         try {
             List<RebaseStatus.Record> records = mArchiveManager.getRebaseStatus();
             if (records.isEmpty()) {
                 sb.append("<br>No rebased changes.<br>");
                 return true;
             }
             // Should never be reached while dumping an existing archive to html.
             sb.append(String.format("Displaying changes from version: %s to version: %s<br>",
                                     getVersionHex(mArchiveManager.getParentUri()),
                                     getVersionHex(mArchiveManager.getSecondaryUri())
                                     ));
 
             appendRebaseStatusHtml(records, getContainerPrefix(), sb);
         } catch (IOException ioe) {
             sb.append("{ERROR PROCESSING REBASEDCHANGES MACRO}");
         }
         return true;
     }
 
     protected boolean processedTitleIndexMacro(StringBuilder sb, String text) {
         try {
             for (String name : mArchiveManager.getStorage().getNames()) {
                 appendPageLink(getContainerPrefix(), sb, name, null, true);
                 sb.append("<br>");
             }
         } catch (IOException ioe) {
             sb.append("{ERROR PROCESSING TITLEINDEX MACRO}");
             return true;
         }
         return true;
     }
 
     // USES 'archiveuri' in CSS.
     protected boolean processedArchiveUriMacro(StringBuilder sb, String text) {
         String uri = mArchiveManager.getParentUri();
         if (uri == null) {
             sb.append("???");
             return true;
         }
 
         sb.append("<a class=\"archiveuri\" href=\""+ makeFreenetLink("freenet:" + uri) +"\">");
         sb.append(escapeHTML(uri));
         sb.append("</a>");
         return true;
     }
 
     // USES 'archivever' in CSS.
     protected boolean processedArchiveVersionMacro(StringBuilder sb, String text) {
         String uri = mArchiveManager.getParentUri();
         if (uri == null) {
             sb.append("???");
             return true;
         }
         sb.append("<span class=\"archivever\">");
         sb.append(escapeHTML(getVersionHex(uri)));
         sb.append("</span>");
         return true;
     }
 
     private static int getUskIndex(String usk) {
         return Integer.parseInt(usk.split("/")[2]);
     }
 
     private static String getShortTitle(String uri) {
         if (uri.startsWith("freenet:")) {
             uri = uri.substring("freenet:".length());
         }
 
         String[] fields = uri.split("/");
         if (fields.length < 2) {
             return uri;
         }
 
         StringBuilder sb = new StringBuilder();
 
         sb.append(fields[0].substring(0, 10) + "...");
         int index = 1;
         while (index < fields.length) {
             sb.append("/");
             sb.append(fields[index]);
             index++;
         }
         if (uri.endsWith("/")) {
             // Because String.split() doesn't generate an empty field
             // for this case.
             sb.append("/");
         }
 
         return sb.toString();
     }
 
     private static String getSskForUsk(String usk, int index) {
         String[] fields = usk.split("/");
 
         StringBuilder sb = new StringBuilder();
         sb.append(fields[0].replace("USK@", "SSK@"));
         sb.append("/");
         sb.append(fields[1]);
         sb.append("-" + index);
         index = 3;
         while (index < fields.length) {
             sb.append("/");
             sb.append(fields[index]);
             index++;
         }
         if (usk.endsWith("/")) {
             // Because String.split() doesn't generate an empty field
             // for this case.
             sb.append("/");
         }
 
         return sb.toString();
     }
     //               0              1     2     3
     // SskVersionLinks|freenet:USK@..|count|first
     protected boolean processedSskVersionLinks(StringBuilder sb, String text) {
         String[] fields = text.split("\\|"); // Because it's a regex.
         if (fields.length < 2 ||
             !isValidFreenetUri(fields[1]) ||
             !fields[1].startsWith("freenet:USK@")) {
             sb.append("{ERROR: NO VALID USK}");
             return true;
         }
 
         String usk = fields[1];
         int latest = getUskIndex(usk);
         int count = 1;
         String separator = "\n";
 
         try {
             if (fields.length > 2) {
                 count = Integer.parseInt(fields[2]);
             }
 
             if (fields.length > 3) {
                 if (fields[3].startsWith("+")) {
                     latest += Integer.parseInt(fields[3].substring(1));
                 } else {
                     latest = Integer.parseInt(fields[3]);
                 }
             }
         } catch (NumberFormatException nfe) {
             sb.append("{ERROR: BAD ARGUMENTS}");
             return true;
         }
 
         int index = latest;
         while (index > latest - count && index >= 0) {
             String ssk = getSskForUsk(usk, index);
             sb.append("<a class=\"jfnikiLinkFreenet\" href=\""+ makeFreenetLink(ssk) +"\">");
             sb.append("" + index);
             sb.append("</a>");
             if (index - 1 > latest - count && index - 1 >= 0) { // ugly
                 sb.append(separator);
             }
             index--;
         }
 
         return true;
     }
 
     public boolean processedMacro(StringBuilder sb, String text) {
         if (text.equals("LocalChanges")) {
             return processedLocalChangesMacro(sb, text);
         } else if (text.equals("RebasedChanges")) {
             return processedRebasedChangesMacro(sb, text);
         } else if (text.equals("TitleIndex")) {
             return processedTitleIndexMacro(sb, text);
         } else if (text.equals("ArchiveUri")) {
             return processedArchiveUriMacro(sb, text);
         } else if (text.equals("ArchiveVersion")) {
             return processedArchiveVersionMacro(sb, text);
         } else if (text.startsWith("SskVersionLinks")) {
             // LOOK ----^, has arguments.
             return processedSskVersionLinks(sb, text);
         }
         return false;
     }
 
     // CHK, SSK, USK freenet links.
     public void appendLink(StringBuilder sb, String text) {
         String[] link=split(text, '|');
         if (getFreenetLinksAllowed() &&
             isValidFreenetUri(link[0])) {
             String title = link.length>=2 && !isEmpty(link[1].trim())? link[1]:link[0];
             if (title.equals("__short__")) {
                 title = getShortTitle(link[0]);
             } else if (title.indexOf("__ordinal__") != -1 && link[0].startsWith("freenet:USK@")) {
                 // e.g. 'FMS@__ordinal__' -> 'FMS@123'
                 title = title.replace("__ordinal__", "" + getUskIndex(link[0]));
             }
             sb.append("<a class=\"jfnikiLinkFreenet\" href=\""+ makeFreenetLink(link[0].trim()) +"\">");
             sb.append(escapeHTML(unescapeHTML(title)));
             sb.append("</a>");
             return;
         }
 
         if (isValidLocalLink(link[0])) {
             // Link to an internal wiki page.
             sb.append("<a class=\"jfnikiLinkInternal\" href=\""+ makeHref(makeLink("/" + link[0].trim())) +"\">");
             sb.append(escapeHTML(unescapeHTML(link.length>=2 && !isEmpty(link[1].trim())? link[1]:link[0])));
             sb.append("</a>");
             return;
         }
 
         sb.append("<a class=\"jfnikiLinkExternal\" href=\"" + makeHref(makeLink("/ExternalLink")) +"\">");
         sb.append(escapeHTML(unescapeHTML(link.length>=2 && !isEmpty(link[1].trim())? link[1]:link[0])));
         sb.append("</a>");
     }
 
     // Only CHK and SSK freenet links.
     public void appendImage(StringBuilder sb, String text) {
         if (!getImagesAllowed()) {
             sb.append("{IMAGES DISABLED. IMAGE WIKITEXT IGNORED}");
             return;
         }
 
         if (!getFreenetLinksAllowed()) {
             // Hmmm... A little wonky. But this is ok.
             sb.append("{FPROXY PREFIX NOT SET. IMAGE WIKITEXT IGNORED}");
             return;
         }
 
         String[] link=split(text, '|');
         if (isValidFreenetUri(link[0])) {
             String alt=escapeHTML(unescapeHTML(link.length>=2 && !isEmpty(link[1].trim())? link[1]:link[0]));
 
             String imgUri = link[0];
             if(imgUri.startsWith("freenet:USK@")) {
                 // "Freeze" usk image links.
                 // Notice that the alt text above will refer to the USK.
                 imgUri = getSskForUsk(imgUri, getUskIndex(imgUri));
             }
 
             if (link.length == 3 &&
                 (isValidLocalLink(link[2].trim()) || isValidFreenetUri(link[2].trim()))) {
                 sb.append("<a href=\"");
                 if (isValidLocalLink(link[2].trim())) {
                     sb.append(makeHref(makeLink("/" + link[2].trim())));
                 } else {
                     sb.append(makeFreenetLink(link[2].trim()));
                 }
                 sb.append("\">");
                 sb.append("<img src=\"" + makeFreenetLink(imgUri.trim())
                           + "\" alt=\""+alt+"\" title=\""+alt+"\" />");
                 sb.append("</a>");
                 return;
             } else {
                 // Hmmm... allows extra fields
                 sb.append("<img src=\"" + makeFreenetLink(imgUri.trim())
                           + "\" alt=\""+alt+"\" title=\""+alt+"\" />");
                 return;
             }
         }
         sb.append("{ERROR PROCESSING IMAGE WIKITEXT}");;
     }
 }
 
