 package controllers.cms;
 
 import elfinder.Elfinder;
 import elfinder.ElfinderException._403;
 import elfinder.ElfinderException._404;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.cms.Editor;
 import play.Play;
 import play.data.Upload;
 import play.mvc.Controller;
import plugins.cms.CmsContext;
 import plugins.router.Route;
 
 /**
  * @author benoit
  */
 @Route("/--editor/*")
 public class EditorController extends Controller {
 
 //    public static final String TMP_LOCATION = "/tmp/nemo-upload";
 //
 //    private static final String TYPE_UNKNOWN = "UNKNOWN";
 //    private static final String TYPE_IMAGE = "IMAGE";
 //
     @Route("save")
     public static void save(String path, String lang) {
 
         int i = 0;
         while (true) {
             
             String code     = params.get("editors[" + i + "][code]");
             String content  = params.get("editors[" + i + "][content]");
 
             i++;
 
             if (code == null) {
                 break;
             }
 
             Editor editor = Editor.findByPathAndCodeAndLanguage(path, code, lang);
 
             if (editor == null) {
 
                 editor = new Editor();
 
                 editor.path     = path;
                 editor.code     = code;
                 editor.language = lang;
             }
 
            if (!CmsContext.Constant.CMS_EDITOR_DEFAULT.equals(content)){
                editor.content = content;
            }
            
             editor.save();
         }
     }
 
     @Route("browser")
     public static void browser() throws Exception {
 
         String rootPath = Play.applicationPath.getAbsolutePath();
         
         rootPath = rootPath.substring(0, rootPath.lastIndexOf("/"));
         rootPath += "/__files/" + Play.configuration.getProperty("application.name");
         
         File root = new File(rootPath);
         if(!root.exists()){
             root.mkdirs();
         }
         
         List<Upload> uploads = (List<Upload>) request.args.get("__UPLOADS");
         List<File> fileArray = new ArrayList<File>();
         if (uploads != null){
             for (Upload upload : uploads) {
                 
                 if (upload.getSize() > 0 && upload.getFieldName().equals("upload[]")) {
                     
                     File file = null;
                     
                     if (upload.isInMemory()){
                         
                         file = new File(upload.getFileName());
                         play.libs.IO.write(upload.asStream(), file);
                         
                     } else {
                         
                         file = upload.asFile();
                     }
                     
                     if (file.length() > 0) {
                         fileArray.add(file);
                     }
                 }
             }
         }
         
         //File[] files = params.get("upload[]", File.class);
         
         
         Elfinder.options opts = new Elfinder.options();
         opts.root = root.getAbsolutePath();
         opts.URL  = "/public/files";
         //opts.rootAlias = "/public/files";
         
         Elfinder elfinder = new Elfinder(opts);
         
         try {
             Object result = elfinder.run(params.all(), fileArray);
             
             if (result.getClass() == File.class){
                
                // handle the file 
                renderBinary((File) result);
             }
             else {
                 
                //put json in http response;
                
                renderHtml((String) result);
             }
             
         } catch (_404 ex) {
             play.Logger.info("404");
         } catch (_403 ex) {
             play.Logger.info("403");
         } catch (Exception ex){
             play.Logger.info("ex");
             throw ex;
         }
         
     }
     
     
 
 }
