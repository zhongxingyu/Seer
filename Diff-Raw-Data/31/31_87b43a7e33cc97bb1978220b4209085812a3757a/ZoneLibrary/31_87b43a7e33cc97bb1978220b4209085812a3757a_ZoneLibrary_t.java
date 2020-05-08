 /*
  * servlet for displaying and managing library elements
  */
 package servlets;
 
 import contrib.CIFSNetworkInterface;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.List;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import jcifs.smb.SmbFile;
 import multicastmusiccontroller.ProgramConstants;
 import zoneserver.ZoneServerUtility;
 
 /**
  * @author Jason Zerbe
  */
 public class ZoneLibrary extends HttpServlet implements ProgramConstants {
 
     public ZoneLibrary() {
     }
 
     @Override
     protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         PrintWriter out = resp.getWriter(); //get the response writer for later
 
         out.println("<div id='zoneLibraryPage' data-role='page' data-theme='d'>"
                 + "<div data-role='header' data-theme='b' data-position='fixed'>"
                 + "<a href='javascript:libraryAddPage_Load();' data-role='button' data-icon='plus'>Add</a>"
                 + "<h1>Media Library</h1>"
                 + "</div>"
                 + "<div data-role='content'>");
 
         String aPathStr = req.getParameter("path");
         if ((aPathStr != null) && (!aPathStr.equals(""))) { //make sure this contains a path
             out.println("<ul id='zoneLibraryList' data-role='listview' data-theme='d'>");
             if (aPathStr.contains(ServerType.smb.toString().concat(prefixUriStr))) { //CIFS share
                 HashMap<SmbFile, String> aCIFSDirMap = CIFSNetworkInterface.getInstance().getDirectoryList(aPathStr);
                 int i = 0;
                 for (SmbFile iSmbFile : aCIFSDirMap.keySet()) {
                     out.println("<li id='zoneLibraryListItem_" + i + "'>");
                     if (aCIFSDirMap.get(iSmbFile).equals("")) { //this is a directory
                         out.println("<a href='javascript:mediaLibrary_LoadDirectory(&quot;"
                                 + URLEncoder.encode(iSmbFile.getPath(), "UTF-8") + "&quot;);'>"
                                 + iSmbFile.getName() + "</a>");
                     } else {
                         out.println("<a href='javascript:playList_addMediaPath_NoRedir(&quot;"
                                 + URLEncoder.encode(iSmbFile.getPath(), "UTF-8") + "&quot;);'>"
                                 + iSmbFile.getName() + "</a>");
                     }
                     out.println("</li>");
                     i++;
                 }
             } //end CIFS share formatting
             out.println("</ul>");
         } else { //no path set, load various base preference directories
             out.println("<ul id='zoneLibraryList' data-role='listview' data-split-icon='delete' data-split-theme='d'>");
             List<String> rootPathStrList = ZoneServerUtility.getInstance().getMediaDirEntries();
             if (rootPathStrList.size() > 0) {
                 int i = 0;
                 for (String rootPathStr : rootPathStrList) {
                     out.println("<li id='zoneLibraryListItem_" + i + "'>");
                     out.println("<a href='javascript:mediaLibrary_LoadDirectory(&quot;"
                             + URLEncoder.encode(rootPathStr, "UTF-8") + "&quot;);'>" + rootPathStr + "</a>"
                             + "<a href='javascript:mediaLibrary_removeMediaRoot(&quot;zoneLibraryListItem_"
                             + i + "&quot;);' data-role='button' data-icon='delete'>Remove</a>");
                     out.println("</li>");
                     i++;
                 }
             }
             out.println("</ul>");
         }
 
         out.println("</div>" //close content
                 + "<div data-id='mainNavFooter' data-role='footer' data-position='fixed'>"
                 + "<div data-role='navbar'>"
                 + "<ul>"
                 + "<li><a href='javascript:zoneSelection_Load();'>Zone Selection</a></li>"
                 + "<li><a href='javascript:playList_Load();'>Now Playing</a></li>"
                 + "<li><a href='javascript:mediaLibrary_Load();' class='ui-btn-active ui-state-persist'>Media Library</a></li>"
                 + "</ul>"
                 + "</div>"
                 + "</div>"
                 + "</div>");
     }
 
     @Override
     protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
         String opt = req.getParameter("opt"); //what are we doing?
         if ((opt != null) && (!opt.equals(""))) { //non-empty
             if (opt.equals("addMediaRoot")) {
                 ServerType newServerType = ServerType.valueOf(req.getParameter("newServerType"));
                 String newServerAddress = req.getParameter("newServerAddress");
                 String newFilePath = req.getParameter("newFilePath");
                 if ((newServerType != null) && (newServerAddress != null)
                         && (!newServerAddress.equals("")) && (newFilePath != null)
                         && (!newFilePath.equals(""))) { //all variables are set
                     ZoneServerUtility.getInstance().updateMediaDirEntry(newServerType, newServerAddress, newFilePath);
                     resp.getWriter().println("added root media entry: "
                             + newServerType.toString() + "://" + newServerAddress
                             + newFilePath);
                 }
             } else if (opt.equals("removeMediaRoot")) {
                 String aRootIndexStr = req.getParameter("index");
                 if ((aRootIndexStr != null) && (!aRootIndexStr.equals(""))) {
                     int aRootIndexInt = Integer.valueOf(aRootIndexStr);
                     ZoneServerUtility.getInstance().removeMediaDirEntry(aRootIndexInt);
                     resp.getWriter().println("removed root media entry #" + String.valueOf(aRootIndexInt));
                 }
             }
         }
     }
 }
