 package org.makumba.parade.view.managers;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 
 import org.makumba.parade.init.InitServlet;
 import org.makumba.parade.model.Row;
 import org.makumba.parade.view.interfaces.CommandView;
 
 import freemarker.template.SimpleHash;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class CommandViewManager implements CommandView {
 
     public String getCommandView(String view, Row r, String path, String file, String opResult) {
         StringWriter result = new StringWriter();
         PrintWriter out = new PrintWriter(result);
         String template = "";
         
         // treating parameters
         if(opResult == null) opResult = new String("");
         if(path == null) path = new String("");
 
         String pathEncoded = "";
 
         try {
             pathEncoded = URLEncoder.encode(path, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         // depending on what we want, we generate different results
         
         if (view == null || view.equals(""))
             return "jsp:/tipOfTheDay.jsp";
         else if (view != null && view.equals("newFile"))
             template = "newFile.ftl";
         else if (view != null && view.equals("newDir"))
             template = "newDir.ftl";
         else if(view != null && view.equals("commandOutput"))
             template ="commandOutput.ftl";
         else if(view != null && view.equals("commit"))
             template="cvsCommit.ftl";
         else
             return "No such view defined for Command";
         
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate(template);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         
         /* Creating the data model */
         SimpleHash root = new SimpleHash();
         root.put("rowName", r.getRowname());
         root.put("path", path);
         root.put("pathURI", pathEncoded);
         root.put("opResult", opResult);
         
         if(file != null) {
             java.io.File f = new java.io.File(file);
             root.put("fileName", f.getName());
            root.put("file", file);
         }
 
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
         
         out.flush();
         
         return result.toString();
 
     }
 
 
     public static String getUploadResponseView(String context, String path, String fileName, String contentType, int fileSize, String saveFilePath) {
         StringWriter resultWriter = new StringWriter();
         PrintWriter out = new PrintWriter(resultWriter);
     
         Template temp = null;
         try {
             temp = InitServlet.getFreemarkerCfg().getTemplate("uploadFileResponse.ftl");
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         /* Creating the data model */
         SimpleHash root = new SimpleHash();
         root.put("rowName", context);
         root.put("relativeFilePath", path + java.io.File.separator + fileName);
         root.put("saveFilePath", saveFilePath);
         root.put("contentType", contentType);
         root.put("contentLength", new Integer(fileSize));
         root.put("path", path);
         
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
         
         out.flush();
         return resultWriter.toString();
     }
 
 }
