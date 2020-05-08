 package org.makumba.parade.view.managers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.File;
 import org.makumba.parade.model.Parade;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowWebapp;
 import org.makumba.parade.model.managers.ServletContainer;
 import org.makumba.parade.tools.DisplayFormatter;
 import org.makumba.parade.view.interfaces.FileView;
 import org.makumba.parade.view.interfaces.TreeView;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class FileViewManager implements FileView, TreeView {
     
     static Logger logger = Logger.getLogger(FileViewManager.class.getName());
     
     public void setFileView(SimpleHash fileView, Row r, String path, File f) {
         
         String pathEncoded = "";
         String nameEncoded = "";
         try {
             // we encode the path twice, because of the javascript that uses it
             pathEncoded = URLEncoder.encode(URLEncoder.encode(f.getPath().substring(r.getRowpath().length() + 1), "UTF-8"), "UTF-8");
             nameEncoded = URLEncoder.encode(f.getName(), "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } 
         
         fileView.put("path", f.getPath().substring(r.getRowpath().length() + 1));
         fileView.put("pathEncoded", pathEncoded);
         fileView.put("nameEncoded", nameEncoded);
         fileView.put("name", f.getName());
         fileView.put("isDir", f.getIsDir());
 
         // icons
         String fl = f.getName().toLowerCase();
         String image = "unknown";
 
         if (fl.endsWith(".java"))
             image = "java";
         if (fl.endsWith(".mdd") || fl.endsWith(".idd"))
             image = "text";
         if (fl.endsWith(".jsp") || fl.endsWith(".properties") || fl.endsWith(".xml") || fl.endsWith(".txt")
                 || fl.endsWith(".conf"))
             image = "text";
         if (fl.endsWith(".doc") || fl.endsWith(".jsp") || fl.endsWith(".html") || fl.endsWith(".htm")
                 || fl.endsWith(".rtf"))
             image = "layout";
         if (fl.endsWith(".gif") || fl.endsWith(".png") || fl.endsWith(".jpg") || fl.endsWith(".jpeg"))
             image = "image";
         if (fl.endsWith(".zip") || fl.endsWith(".gz") || fl.endsWith(".tgz") || fl.endsWith(".jar"))
             image = "zip";
         if (fl.endsWith(".avi") || fl.endsWith(".mpg") || fl.endsWith(".mpeg") || fl.endsWith(".mov"))
             image = "movie";
         if (fl.endsWith(".au") || fl.endsWith(".mid") || fl.endsWith(".vaw") || fl.endsWith(".mp3"))
             image = "sound";
         
         fileView.put("image", image);
 
         // name
         String addr = "";
         RowWebapp webappdata = (RowWebapp) r.getRowdata().get("webapp");
         String webappPath = webappdata.getWebappPath();
         
         if (webappdata.getStatus().intValue() == ServletContainer.RUNNING
                 && path.startsWith(webappPath)) {
             
             String pathURI = path.substring(path.indexOf(webappPath) + webappPath.length()).replace(
                     java.io.File.separatorChar, '/')
                     + "/";
 
             if (fl.endsWith(".java")) {
                 String dd = pathURI + f.getName();
                 dd = dd.substring(dd.indexOf("classes") + 8, dd.lastIndexOf(".")).replace('/', '.');
                 addr = "/" + r.getRowname() + "/classes/" + dd;
             }
             if (fl.endsWith(".mdd") || fl.endsWith(".idd")) {
                 String dd = pathURI + f.getName();
                 dd = dd.substring(dd.indexOf("dataDefinitions") + 16, dd.lastIndexOf(".")).replace('/', '.');
                 addr = "/" + r.getRowname() + "/dataDefinitions/" + dd;
             }
             if (fl.endsWith(".jsp") || fl.endsWith(".html") || fl.endsWith(".htm") || fl.endsWith(".txt")
                     || fl.endsWith(".gif") || fl.endsWith(".png") || fl.endsWith(".jpeg") || fl.endsWith(".jpg")
                     || fl.endsWith(".css") || fl.startsWith("readme"))
                 addr = "/" + r.getRowname() + pathURI + f.getName();
 
             if (fl.endsWith(".jsp"))
                 addr += "x";
         }
         
         fileView.put("isLinked", new Boolean(!addr.equals("")));
         fileView.put("address", addr);
        
         // time && size
         
         fileView.put("dateLong", new java.util.Date(f.getDate().longValue()).toString());
         fileView.put("dateNice", DisplayFormatter.readableTime(f.getAge().longValue()));
         fileView.put("isEmpty", f.getSize().longValue() < 0l);
         fileView.put("sizeLong", f.getSize());
         fileView.put("sizeNice", DisplayFormatter.readableBytes(f.getSize().longValue()));
     }
 
     public String getTreeView(Parade p, Row r) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         
         /* Initalising template */
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("tree.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         List b = computeTree(r);
 
         /* Creating data model */
         SimpleHash root = new SimpleHash();
         root.put("rowName", r.getRowname());
         root.put("branches", b);
         
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
 
     /**
      * Computes the tree for a row
      * @param r the Row for which the tree should be computed
      * @return a List containing the tree of folders
      * 
      */
     private List computeTree(Row r) {
         List<SimpleHash> b = new LinkedList<SimpleHash>();
         
         Session s = InitServlet.getSessionFactory().openSession();
         Transaction tx = s.beginTransaction();
         
         Query q = s.createSQLQuery("SELECT * FROM File f JOIN Row r WHERE f.ID_ROW = r.ID AND f.isDir = '1' AND r.rowname = ? ORDER BY f.path ASC").addScalar("PATH", Hibernate.STRING).addScalar("NAME", Hibernate.STRING);
         q.setString(0, r.getRowname());
         
         List l =q.list();
         
         // this vector holds the order of a directory in a given level
         // the position in the vector represents the level, the value represents the order
         Vector<Integer> levels = new Vector<Integer>();
         Vector<String> directories = new Vector<String>();
         Integer order = 0;
         Integer previousLevel = -1;
         
         levels.add(0, 0);
         
         Iterator i = l.iterator();
         while(i.hasNext()) {
             Object[] line = (Object[]) i.next();
             String path = (String)line[0];
             String name = (String)line[1];
             
             String simplePath = null;
             try {
                 simplePath = !path.equals(r.getRowpath())?path.substring(path.indexOf(r.getRowpath()) + r.getRowpath().length() + 1):r.getRowname();
             } catch(StringIndexOutOfBoundsException e) {
                 logger.warn("Symbolic link detected while computing the tree, "+path+" of row "+r.getRowname()+ " links to something outside of the row");
             }
             
             if(simplePath == null)
                 continue;
             
             simplePath = simplePath.replace(java.io.File.separatorChar, '/');
             if(!simplePath.equals(r.getRowname())) simplePath = r.getRowname() + "/" + simplePath;
             
             //we split the path in directory names
             StringTokenizer st = new StringTokenizer(simplePath, "/");
             int level = -1;
             
             // for each directory, we store its name, level and order
             while(st.hasMoreTokens()) {
                 
                 level++;
                 directories.add(level, st.nextToken());
                 
                 // we reach the end of the path
                 if(!st.hasMoreTokens()) {
                     // we are in a situation where the previous path and the current one are on the same level
                     // so we increment the order of the current path
                     if(level == previousLevel) {
                         order = levels.get(level) + 1;
                         levels.add(level, order);
                         
                     // we are one level above the one of the previous path
                     // so we reset the level beneath us
                     // as well we increment the current order
                     } else if(level < previousLevel) {
                         levels.add(previousLevel, 0);
                         order = levels.get(level) + 1;
                         levels.add(level, order);
                     // we are one level beneath the previous level
                     // so we set the order to a new minimum
                     } else if(level > previousLevel) {
                         levels.add(level, 0);
                         
                     }
                     previousLevel = level;
                 }
             }
             
             String treeRow = "objTreeMenu"; // start a javascript line to compose a tree
             
             for(int j = 0; j < previousLevel+1; j++) {
                 if(levels.get(j) == -1) continue;
                 treeRow = treeRow + ".n[" + levels.get(j) + "]";
             }
             
             SimpleHash branch = new SimpleHash();
             try {
                 branch.put("treeRow", treeRow);
                 branch.put("fileName", name.equals("_root_")?r.getRowname():name);
                 String nicePath = !simplePath.equals(r.getRowname())?simplePath.substring(r.getRowname().length() + 1):"";
                 branch.put("filePath", URLEncoder.encode(nicePath, "UTF-8"));
             } catch (UnsupportedEncodingException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
             b.add(branch);
             
         }
         
         tx.commit();
         s.close();
         
         
         return b;
     }
 
 }
