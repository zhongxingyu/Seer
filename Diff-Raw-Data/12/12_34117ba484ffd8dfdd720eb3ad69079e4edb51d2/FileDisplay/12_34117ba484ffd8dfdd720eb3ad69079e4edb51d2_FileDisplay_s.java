 package org.makumba.parade.view.managers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.managers.FileManager;
 import org.makumba.parade.tools.FileComparator;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class FileDisplay {
 
     // TODO move this somewhere else
    public static String creationFileOK(String rowname, String path, String filepath, String filename) {
         return "New file " + filename + " created. " + "<a href='/File.do?op=editFile&context=" + rowname + "&path="
                + path + "&file=" + filepath+"'>Edit</a></b>";
     }
 
     public static String creationDirOK(String filename) {
         return "New directory " + filename + " created. ";
     }
 
     public static String deletionFileOK(String filename) {
         return "File " + filename + " deleted";
     }
 
     public String getFileBrowserView(Parade p, Row r, String path, String opResult, boolean success) {
 
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
 
         // if this is the root of the row
         if (path == null || path.equals("null"))
             path = "/";
         
         if (opResult == null) opResult = "";
         
         String pathEncoded = "";
         
         try {
             pathEncoded = URLEncoder.encode(path, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         FileViewManager fileV = new FileViewManager();
         CVSViewManager cvsV = new CVSViewManager();
         
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("fileBrowser.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         /* Creating data model */
         SimpleHash root = new SimpleHash();
         root.put("rowName", r.getRowname());
         root.put("success", success);
         root.put("opResult", opResult);
         root.put("path", path);
         root.put("pathOnDisk", r.getRowpath() + (path.length()>1?java.io.File.separator + path.replace("/", java.io.File.separator):""));
         root.put("pathEncoded", pathEncoded);
         root.put("parentDirs", getParentDir(r, path));
         
         // computing file model data
         String absolutePath = "";
         if(path.equals("/")) absolutePath = r.getRowpath();
         else absolutePath = r.getRowpath() + java.io.File.separator + path.replace("/", java.io.File.separator);
         if(absolutePath.endsWith(java.io.File.separator)) absolutePath = absolutePath.substring(0, absolutePath.length() - 1);
         File file = (File) r.getFiles().get(absolutePath);
         List files = file.getChildren();
         FileComparator fc = new FileComparator();
 
         Collections.sort(files, fc);
         //String relativePath = path.substring(r.getRowpath().length(), path.length());
         
         List fileViews = new LinkedList();
 
         for (Iterator j = files.iterator(); j.hasNext();) {
             File currentFile = (File) j.next();
             SimpleHash fileView = new SimpleHash();
             fileV.setFileView(fileView, r, path, currentFile);
             cvsV.setFileView(fileView, r, path, currentFile);
             
             fileViews.add(fileView);
         }
         
         root.put("fileViews", fileViews);
         
         /* Merge data model with template */
         try {
             temp.process(root, out);
         } catch (TemplateException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         
         return result.toString();
     }
 
     private List getParentDir(Row r, String path) {
 
         if (path == null)
             path = "/";
         
         //String relativePath = path.substring(r.getRowpath().length(), path.length());
         
         List parentDirs = new LinkedList();
         //String currentPath = path.substring(0, r.getRowpath().length());
         String currentPath = "";
 
         StringTokenizer st = new StringTokenizer(path, "/");
         while (st.hasMoreTokens()) {
             SimpleHash parentDir = new SimpleHash();
             
             String thisDir = st.nextToken();
             currentPath +=  thisDir;
             if(st.hasMoreElements()) currentPath += "/";
             parentDir.put("path", currentPath);
             parentDir.put("directoryName", thisDir);
             parentDirs.add(parentDir);
         }
         return parentDirs;
     }
 
 }
