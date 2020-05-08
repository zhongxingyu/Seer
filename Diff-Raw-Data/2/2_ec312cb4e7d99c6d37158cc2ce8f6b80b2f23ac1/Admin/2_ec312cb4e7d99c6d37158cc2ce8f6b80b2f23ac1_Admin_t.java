 package controllers.cms;
 
 import controllers.Check;
 import controllers.Secure;
 import models.cms.CMSFile;
 import models.cms.CMSPage;
 import play.data.validation.Valid;
 import play.db.jpa.Blob;
 import play.libs.MimeTypes;
 import play.mvc.Controller;
 import play.mvc.With;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.List;
 
 /**
  * Admin controller for CMS module.
  */
 @With(Secure.class)
 @Check("admin")
 public class Admin extends Controller {
 
     /**
      * Display all CMS pages.
      */
     public static void index(String template) {
         if (template == null) {
             if (CMSPage.getAllTemplate().size() > 0) {
                 template = CMSPage.getAllTemplate().get(0);
             } else {
                 template = "Fragment";
             }
         }
         List<CMSPage> pages = CMSPage.getAllByTemplate(template, Boolean.FALSE);
         render(pages, template);
     }
 
     /**
      * Display edit form for a CMS page.
      *
      * @param pageName
      */
     public static void editPage(String pageName) {
         CMSPage page = CMSPage.findById(pageName);
         String template = page.template;
         renderTemplate("@edit", page, template);
     }
 
     /**
      * Display form to add a CMS page.
      */
     public static void addPage(String template) {
         CMSPage page = new CMSPage();
         page.template = template;
         renderTemplate("@edit", page, template);
     }
 
     /**
      * Saving a CMS page.
      *
      * @param page
      */
     public static void savePage(@Valid CMSPage page) {
         String template = page.template;
         if (request.params.get("delete") != null) {
             page.delete();
             index(template);
         }
         if (validation.hasErrors()) {
             renderTemplate("@edit", page, template);
         }
         page.save();
         flash.success("Saved");
 
         if (request.params.get("savePage") != null) {
             Frontend.show(page.name);
         }
         editPage(page.name);
     }
 
     /**
      * Saving a CMS page.
      *
      * @param name
      * @param body
      */
     public static void ajaxSaveBody(String name, String body) {
         CMSPage page = CMSPage.findById(name);
         notFoundIfNull(page);
         page.body = body;
         page.save();
        ok();
     }
 
     /**
      * Delete a page.
      *
      * @param id
      */
     public static void delete(String id) {
         CMSPage page = CMSPage.findById(id);
         notFoundIfNull(page);
         String template = page.template;
         page.delete();
         index(template);
     }
 
     /**
      * Published the page.
      *
      * @param id
      */
     public static void publish(String id) {
         CMSPage page = CMSPage.findById(id);
         notFoundIfNull(page);
         page.published = Boolean.TRUE;
         page._save();
         index(page.template);
     }
 
     /**
      * UNpublished the page.
      *
      * @param id
      */
     public static void unpublish(String id) {
         CMSPage page = CMSPage.findById(id);
         notFoundIfNull(page);
         page.published = Boolean.FALSE;
         page._save();
         index(page.template);
     }
 
     /**
      * Manager action for filemanager.
      */
     public static void filemanager() throws FileNotFoundException {
         String method = params.get("mode");
         String error = null;
 
         // Get information for a file
         if (method.equals("getinfo")) {
             String path = params.get("path");
             if (path.endsWith("true")) {
                 path = path.replaceAll("true$", "");
             }
             if (path.endsWith("/")) {
                 path = path.replaceAll("/$", "");
             }
 
             CMSFile file = CMSFile.findById(path);
             if (!path.equals("") && file == null) {
                 error = "File '" + path + "' not found";
             }
             response.contentType = "application/json";
             render("cms/Admin/filemanager/getinfo.html", file, error);
 
         }
 
         // Get information for a folder
         if (method.equals("getfolder")) {
             String path = params.get("path");
             if (path.endsWith("true")) {
                 path = path.replaceAll("true$", "");
             }
             if (!path.endsWith("/")) {
                 path += "/";
             }
 
             Boolean getSize = params.get("getsize", Boolean.class);
 
             List<CMSFile> files = CMSFile.getFolderChildren(path);
             response.contentType = "application/json";
             render("cms/Admin/filemanager/getfolder.html", files);
         }
 
         // Rename a file / folder
         if (method.equals("rename")) {
             String old = params.get("old");
             String newName = params.get("new");
 
             String oldPath;
             String oldName;
             if (old.lastIndexOf('/') >= 0) {
                 oldPath = old.substring(0, old.lastIndexOf('/') + 1);
                 oldName = old.substring(old.lastIndexOf('/') + 1, old.length());
             } else {
                 oldPath = "";
                 oldName = old;
             }
             String newPath = oldPath + newName;
             CMSFile file = CMSFile.findById(old);
 
             if (file == null) {
                 error = "Source file '" + old + "' not found";
             }
 
             if (CMSFile.findById(newPath) == null) {
                 CMSFile newFile = new CMSFile();
                 newFile.isFolder = file.isFolder;
                 newFile.name = newPath;
                 newFile.title = newName;
                 newFile.data = file.data;
                 newFile.save();
                 file.delete();
             } else {
                 error = "Can't rename file '" + old + "' to '" + newPath + "'. A file with that name already exist !";
             }
 
             response.contentType = "application/json";
             render("cms/Admin/filemanager/rename.html", oldPath, oldName, newName, newPath, error);
         }
 
         // move a file / folder
         if (method.equals("move")) {
             String old = params.get("old");
             String newPath = params.get("new");
             if (!newPath.endsWith("/")) {
                 newPath += "/";
             }
 
             String oldPath;
             String oldName;
             if (old.lastIndexOf('/') >= 0) {
                 oldPath = old.substring(0, old.lastIndexOf('/') + 1);
                 oldName = old.substring(old.lastIndexOf('/') + 1, old.length());
             } else {
                 oldPath = "";
                 oldName = old;
             }
             CMSFile file = CMSFile.findById(old);
             if (file == null) {
                 error = "Source file '" + old + "' not found";
             }
 
             if (CMSFile.findById(newPath + oldName) == null) {
                 CMSFile newFile = new CMSFile();
                 newFile.isFolder = file.isFolder;
                 newFile.name = newPath + oldName;
                 newFile.title = oldName;
                 newFile.data = file.data;
                 newFile.save();
                 file.delete();
             } else {
                 error = "Can't move file '" + old + "' to '" + newPath + "'. A file with that name already exist !";
             }
 
             response.contentType = "application/json";
             render("cms/Admin/filemanager/move.html", oldPath, oldName, newPath, error);
         }
 
         // Delete a folder
         if (method.equals("delete")) {
             String path = params.get("path");
 
             CMSFile.deleteFolder(path);
             response.contentType = "application/json";
             render("cms/Admin/filemanager/delete.html", path);
         }
 
         // Adding a file
         if (request.method.equals("POST") && method.equals("add")) {
             String path = params.get("currentpath");
             if (path.endsWith("true")) {
                 path = path.replaceAll("true$", "");
             }
             if (!path.endsWith("/")) {
                 path += "/";
             }
             String filename = params.get("filepath");
             File upload = params.get("newfile", File.class);
 
             if (CMSFile.findById(path + filename) == null) {
                 CMSFile file = new CMSFile();
                 file.isFolder = Boolean.FALSE;
                 file.name = path + filename;
                 file.title = filename;
                 String mimeType = MimeTypes.getContentType(upload.getName());
                 file.data = new Blob();
                 file.data.set(new FileInputStream(upload), mimeType);
                 file.save();
             } else {
                 error = "A file with the name '" + path + filename + "' already exist !";
             }
 
             render("cms/Admin/filemanager/add.html", path, filename, error);
         }
 
         // Adding a folder
         if (method.equals("addfolder")) {
             String path = params.get("path");
             if (!path.endsWith("/")) {
                 path += "/";
             }
 
             String name = params.get("name");
             if (CMSFile.findById(path + name) == null) {
                 CMSFile folder = new CMSFile();
                 folder.isFolder = Boolean.TRUE;
                 folder.name = path + name;
                 folder.title = name;
                 folder.save();
             } else {
                 error = "A folder with the name '" + path + name + "' already exist !";
             }
             response.contentType = "application/json";
             render("cms/Admin/filemanager/addfolder.html", path, name, error);
         }
 
         // Serve the file to the user
         if (method.equals("download")) {
             String name = params.get("path");
             CMSFile image = CMSFile.findById(name);
             response.contentType = image.data.type();
             renderBinary(image.data.get());
         }
 
     }
 
 }
