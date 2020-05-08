 package torrentweb;
 
 import java.io.*;
 
 import java.util.*;
 
 import com.sun.net.httpserver.*;
 import java.text.*;
 
 //import javax.servlet.http.*;
 
 //import org.apache.catalina.core.*;
 public class Frontpage implements HttpHandler {
     public final static String VersionString = "TorrentWeb Server 1.01";
     public final  static String AddressString = VersionString + " (" + System.getProperty("os.name") + " " + System.getProperty("os.version") + ")";
     private final static SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
     String contentRoot;
 
     //String jspFile; // External Path to jspFile
     TorrentManager torrentManager;
     String torrentRoot;
     public File absoluteFile(String path) {
     return new File(contentRoot + path);
     }
     public void handle(HttpExchange exchange) throws IOException {
 
 
         InputStream requestBody = exchange.getRequestBody();
         while (requestBody.read() != -1) {
         }
 
 
         exchange.getResponseHeaders().set("Content-Type", "text/html");
         //exchange.sendResponseHeaders(200,0);
 
 
         PrintWriter out = new PrintWriter(exchange.getResponseBody());
         //out.write("<html><head><title>torrent index</title></head><body>");
         //displayContent(exchange.getRequestURI().getPath(),"astorrent=true".equals(exchange.getRequestURI().getQuery()), printer);
         String path = exchange.getRequestURI().getPath();
         boolean directoryAsTorrent = "astorrent=true".equals(exchange.getRequestURI().getQuery());
         if (path == null) {
             path = "";
         }// {return "No directory given";} //ErrorBox()
         path = securePath(path);
         String absolutePath = contentRoot + path;
         if (path == null) {
             exchange.sendResponseHeaders(404, 0);
             out.println("<html><head><title>Error</title></head><body>");
             out.println("<h1>Invalid path</h1><br>");
         //errorBox("Invalid argument", "Invalid path",out);
 
         } else { // ErrorBox()
 
 
 
             File sessionPath = new File(absolutePath);
             
             try {
             	boolean sessionPathExists = sessionPath.exists(); 
             	try {
             	
 
 
                 if (sessionPathExists) {
 
 
                     exchange.sendResponseHeaders(200, 0);
                     out.println("<html><head><title>torrent index</title></head><body>");
 
                     if (sessionPath.isDirectory()) {
 
 
                         String displayPath;
                         if (path.isEmpty()) {
                             displayPath = "/";
                         } else {
                             displayPath = path;
                         }
                         String dirName = Conversion.HTMLString(displayPath);
                         if (!dirName.endsWith("/")) {dirName+="/";}
                         out.println("<h1>Directory: " + dirName + "</h1><br>");
                         if (directoryAsTorrent) {
 
                             displayTorrent(path, out);
                         }
                         DirectoryListing listing = new DirectoryListing(path);
                         listing.display(out);
                     // displayDirectory(path, sort, sessionPath);
 
                     } else if (sessionPath.isFile()) {
                         // out.print("torrent download to be implemented..");return;
                         out.println("<h1>File: " + Conversion.HTMLString(path) + "</h1><br>");
 
                         displayParent(path, out);
 
                         displayTorrent(path, out);
 
 
                     }
                 } else {
 
                     exchange.sendResponseHeaders(404, 0);
                     out.println("<html><head><title>Error</title></head><body>");
                     out.println("<h1>File not found: " + Conversion.HTMLString(path) + "</h1><br>");
                 }
             } 
             catch (Exception ex) {
             	//exchange.sendResponseHeaders(500, 0);
             	//out.println("<html><head><title>Error</title></head><body>");
             	out.println("Error: " + ex.getMessage());
             }
             } catch (SecurityException ex) {
             	exchange.sendResponseHeaders(403, 0);
             	out.println("<html><head><title>Error</title></head><body>");
             	out.println("<h1>Forbidden: " + ex.getMessage() + "</h1><br>");
             }
         }
 
         out.println("<hr><address>" + AddressString + "</address>");
         out.println("</body></html>");
         out.flush();
         out.close();
         exchange.close();
     }
 
     public Frontpage(String in_contentRoot, TorrentManager in_torrentManager) {
         contentRoot = in_contentRoot;
         torrentManager = in_torrentManager;
 
     }
 
     public String securePath(String path) {
         if (path.length() == 0) {
             return path;
         } // Empty String, that's okay
 
         if (path.charAt(path.length() - 1) == File.separatorChar) {
             return securePath(path.substring(0, path.length() - 1));
         } // path must not have "/" at the end.
         if (path.charAt(0) != File.separatorChar) {
             return securePath(File.separatorChar + path);
         } // path must have "/" at the beginning if the be path length is >0.
         // conclusion: the path length can't be 1
         if (path.contains(File.separator + "..") || path.contains(File.separator + File.separator)) {
             return null;
         }
         return path;
     }
 
     public void displayParent(String path, PrintWriter out) throws IOException {
 
         if (path.length() < 1) {
             return;
         } else {
             out.print("[PARENT]");
             String address = Conversion.VeryCompatibleQueryStringCompliant(convertToVirtualPath(absoluteFile(path).getParent()));
             if (!address.endsWith("/")) {address+="/";}
             displayLink(address,
                     "Parent Directory", out);
             out.print("<br>");
             return;
         }
     }
 
     public String convertToVirtualPath(String absolutePath) {
         if (absolutePath == null || absolutePath.length() <= contentRoot.length()) {
             return "/";
         }
         return absolutePath.substring(contentRoot.length());
 
     }
 
     public void displayLink(String address, String name, PrintWriter out) throws IOException {
         out.print("<a href='" + address + "'>" + Conversion.HTMLString(name) + "</a>");
     }
     public void displayTorrent(String path, PrintWriter out) throws Exception {
 
         final TorrentManager.WebTorrent torrent = torrentManager.getWebTorrent(path); //new WebTorrent(path, sessionPath);
         TorrentManager.TorrentState state = torrent.getState();
         String refreshLink = "<a href='javascript:this.location.reload();'>Refresh</a><br>";
         if (state == TorrentManager.TorrentState.UNCREATED) {
             out.print("Creating torrent..<br>");
             out.print("Progress: " + torrent.getProgress() + "%<br>");
             out.print(refreshLink);
             //out.flush(); //I expected it to write to the page
             new Thread(new Runnable() {
 
                 public void run() {
                     try {
                         torrent.create();
                     } catch (Exception ex) {
                         System.err.println(ex);
                     }
                 }
             }).start();
 
 
         } else if (state == TorrentManager.TorrentState.PROGRESSING) {
             out.print("Torrent creation in progress: " + torrent.getProgress() + "%<br>");
             out.println(refreshLink);
         } else {
             if (torrent.update()) {
                 out.print("Torrent updated<br>");
             }
             out.print("torrent ready: ");
             displayLink(torrent.getURL(), absoluteFile(path).getName(), out);
             out.print("<br>");
         }
     }
 
     private class DirectoryListing {
 
         String path;
         File directory;
         ArrayList<File> directoryList;
         ArrayList<File> fileList;
         //ArrayList<File> unknownList;
         DirectoryListing(String in_path) {
             path = in_path;
             directory = absoluteFile(in_path);
             String[] files = directory.list();
             directoryList = new ArrayList<File>();
             fileList = new ArrayList<File>();
             for (int i = 0; i < files.length; i++) {
                 File toAdd = new File(contentRoot + path + File.separator + files[i]);
                 try {
                 if (toAdd.isDirectory()) {
                     directoryList.add(toAdd);
                 } else {
                     fileList.add(toAdd);
                 }
                 } catch (SecurityException ex) {
                 	fileList.add(toAdd);
                 }
                 
             }
         }
 
         /*
          * void sort(String sort) { if (sort.equals("date")) { sortByDate(); }
          * else if (sort.equals("size")) { sortBySize(); } else { sortByName(); } }
          * void sortByDate() { }
          */
         void display(PrintWriter out) throws IOException {
 
             displayParent(path, out);
 
             out.println("<table>");
 
             Comparator alphabetically = new Comparator<File>() {
 
                 public int compare(File o1, File o2) {
                     return o1.getName().compareToIgnoreCase(o2.getName());
                 }
 
             };
             Collections.sort(directoryList, alphabetically);
             for (int i = 0; i < directoryList.size(); i++) {
                 displayItem(directoryList.get(i), out);
 
             }
             Collections.sort(fileList, alphabetically);
             for (int i = 0; i < fileList.size(); i++) {
                 displayItem(fileList.get(i), out);
 
             }
             out.print("</table>");
         }
 
         public void displayItem(File file, PrintWriter out) throws IOException {
         	boolean isSpecial = false;
         	try {
             if (file.isHidden()) {
                 return;
             }} catch (SecurityException ex) {
             	isSpecial = true;
             }
             out.print("<tr><td>");
             
             boolean isdirectory = false;
             if (!isSpecial) {
             try {
             isdirectory = file.isDirectory();}
             catch (SecurityException ex) {
             	isSpecial = true;
             }
             }
             
             if (isdirectory) {
                 out.print("[DIR]");
                 displayLink(Conversion.VeryCompatibleQueryStringCompliant(path + File.separator + file.getName()) + "?astorrent=true", "<TORRENT>", out);
             } else if (isSpecial) {
             	out.print("[SPECIAL]");
             }
             else {
                 out.print("[FILE]");
             }
             out.print("</td>");
             out.print("<td>");
             long filelength = 0;
             if (!isdirectory && !isSpecial) {
                 filelength = file.length();
             }
             if (isdirectory) {
                 displayLink(Conversion.VeryCompatibleQueryStringCompliant(path + File.separator + file.getName() + "/"), file.getName() + "/", out);
             } else if (filelength > 0) {
             displayLink(Conversion.VeryCompatibleQueryStringCompliant(path + File.separator + file.getName()), file.getName(), out);
             }
             else {
                 out.print(Conversion.HTMLString(file.getName()));
             }
            out.print("</td><td align='right'>" + format.format(new Date(file.lastModified())) + " ");
             out.print("</td><td align='right'>");
             if (!isdirectory && !isSpecial) {
                 out.print(HumanReadable(filelength));
             } else {
                 out.print("-");
             }
             out.println("</td></tr>");
 
         }
     }
 
     public static String HumanReadable(long fileSize) {
         if (fileSize < 1) {
             return "0B";
         }
 
         final String levels = "KMGTPE";
         int level = (int) (java.lang.Math.log((double) fileSize) / java.lang.Math.log(1024));
         if (level > levels.length()) {
             level = levels.length();
         }
         String humanReadable;
         humanReadable = (java.lang.Math.round(10 * fileSize / java.lang.Math.pow(1024, level))) / 10.0 + "";
 
         if (level > 0) {
             humanReadable += levels.charAt(level - 1);
         }
         humanReadable += "B";
         return humanReadable;
     }
 }
