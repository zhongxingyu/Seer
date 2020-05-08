 package play.modules.ide;
 
 import groovy.text.SimpleTemplateEngine;
 import groovy.text.Template;
 import play.Play;
 import play.libs.IO;
 
 import java.io.File;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 import play.mvc.Http;
 import play.vfs.VirtualFile;
 
 public class AceEditor {
 
     // TODO : add snippets
     // TODO : add more key shortcuts for testing ??
     // TODO : vi mode ?
     // TODO : shortcuts link => https://github.com/ajaxorg/ace/wiki/Default-Keyboard-Shortcuts
     public static void index(Http.Request request, Http.Response response) throws Exception {
         String projectName = Play.configuration.getProperty("application.name", "Unknown");
         VirtualFile vf = Play.roots.get(0);
         List<SourceFile> files = getFiles(vf, new ArrayList<SourceFile>());
         vf = vf.child("/app/controllers/Application.java");
         String src = "";
         String currentFile = "NONE";
         if (vf.exists()) {
             src = vf.contentAsString();
             currentFile = vf.getRealFile().getAbsolutePath();
         }
         String type = "ace/mode/java";
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("projectName", projectName);
         params.put("src", src);
         params.put("files", files);
         params.put("currentFile", currentFile);
         params.put("type", type);
         params.put("line", 0);
         response.contentType = "text/html";
         for (VirtualFile f : Play.roots) {
             if (f.getName().equals("ace")) {
                 VirtualFile root = f;
                 root = root.child("/app/views/AceEditor/index.html");
                 renderGroovytemplate(root.getRealFile(), params, response.out);
             }
         }
     }
 
     public static void file(Http.Request request, Http.Response response, String path, int line) throws Exception {
         String projectName = Play.configuration.getProperty("application.name", "Unknown");
         VirtualFile vf = Play.roots.get(0);
         List<SourceFile> files = getFiles(vf, new ArrayList<SourceFile>());
         File file = new File(path);
         String src = IO.readContentAsString(file).trim();
         String currentFile = file.getAbsolutePath();
         String type = type(file);
         Map<String, Object> params = new HashMap<String, Object>();
         params.put("projectName", projectName);
         params.put("src", src);
         params.put("files", files);
         params.put("currentFile", currentFile);
         params.put("type", type);
         params.put("line", line);
         response.contentType = "text/html";
         for (VirtualFile f : Play.roots) {
             if (f.getName().equals("ace")) {
                 VirtualFile root = f;
                 root = root.child("/app/views/AceEditor/index.html");
                 renderGroovytemplate(root.getRealFile(), params, response.out);
             }
         }
     }
 
     public static void save(Http.Request request, Http.Response response, String currentFile, String src) throws Exception {
         response.contentType = "text/plain";
         if (currentFile.equals("NONE")) {
             response.out.write("".getBytes("utf-8"), 0, 0);
         } else {
             File vf = new File(currentFile);
             IO.writeContent(src, vf);
             byte[] content = IO.readContentAsString(vf).trim().getBytes("utf-8");
             response.out.write(content, 0, content.length);
         }
     }
 
    public static void createFile(Http.Request request, Http.Response response, String name) {

    }

    public static void deleteFile(Http.Request request, Http.Response response, String name) {

    }

    public static void insertSnippet(Http.Request request, Http.Response response, String snippetId) {

    }

     private static List<SourceFile> getFiles(VirtualFile file, List<SourceFile> files) {
         VirtualFile vf = Play.roots.get(0);
         for (VirtualFile f : file.list()) {
             if (f.isDirectory()) {
                 getFiles(f, files);
             } else {
                 String display = f.getRealFile().getAbsolutePath().replace(vf.getRealFile().getAbsolutePath(), "");
                 if (isValid(display)) {
                     files.add(new SourceFile(f.getRealFile().getAbsolutePath(), f.getName(), display));
                 }
             }
         }
         return files;
     }
 
     private static boolean isValid(String path) {
         if (path.startsWith("/tmp")) {
             return false;
         } else if (path.startsWith("/public")) {
             if (path.startsWith("/public/javascripts")) {
                 return true;
             }
             if (path.startsWith("/public/stylesheets")) {
                 return true;
             }
             if (path.equals("/public")) {
                 return true;
             }
             return false;
         } else if (path.startsWith("/lib")) {
             return false;
         } else if (path.startsWith("/modules")) {
             return false;
         } else if (path.contains("DS_Store")) {
             return false;
         } else {
             return true;
         }
     }
 
     private static String type(File f) {
         if (f.getAbsolutePath().endsWith("application.conf")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().contains("/conf/messages")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().endsWith(".java")) {
             return "ace/mode/java";
         }
         if (f.getAbsolutePath().endsWith(".js")) {
             return "ace/mode/javascript";
         }
         if (f.getAbsolutePath().endsWith(".html")) {
             return "ace/mode/html";
         }
         if (f.getAbsolutePath().endsWith(".yml")) {
             return "ace/mode/yaml";
         }
         if (f.getAbsolutePath().endsWith(".sh")) {
             return "ace/mode/sh";
         }
         if (f.getAbsolutePath().endsWith(".xml")) {
             return "ace/mode/xml";
         }
         if (f.getAbsolutePath().endsWith(".sql")) {
             return "ace/mode/sql";
         }
         if (f.getAbsolutePath().endsWith(".scala")) {
             return "ace/mode/scala";
         }
         if (f.getAbsolutePath().endsWith(".less")) {
             return "ace/mode/less";
         }
         if (f.getAbsolutePath().endsWith(".cs")) {
             return "ace/mode/coffee";
         }
         if (f.getAbsolutePath().endsWith(".css")) {
             return "ace/mode/css";
         }
         return "ace/mode/text";
     }
 
     public static class SourceFile {
         public String path;
         public String name;
         public String displayPath;
         public SourceFile(String path, String name, String displayPath) {
             this.path = path;
             this.name = name;
             this.displayPath = displayPath;
         }
     }
 
     private static final SimpleTemplateEngine engine = new SimpleTemplateEngine();
 
     private static final ConcurrentHashMap<File, Template> templates =
             new ConcurrentHashMap<File, groovy.text.Template>();
 
     private static void renderGroovytemplate(File file, Map<String, Object> context, OutputStream os) throws Exception {
         OutputStreamWriter osw = new OutputStreamWriter(os);
         if (!templates.containsKey(file)) {
             String code = IO.readContentAsString(file);
             code = code.replace("$.", "\\$.").replace("$(", "\\$(");
             Template template = engine.createTemplate(code);
             templates.putIfAbsent(file, template);
         }
         templates.get(file).make(context).writeTo(osw);
     }
 }
