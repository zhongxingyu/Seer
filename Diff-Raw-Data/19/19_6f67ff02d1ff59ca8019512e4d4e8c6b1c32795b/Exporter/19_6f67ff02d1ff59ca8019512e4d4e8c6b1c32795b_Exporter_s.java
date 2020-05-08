 package io;
 
 import java.io.*;
 import freemarker.template.*;
 
 import objects.MyCourse;
 
 // This class encapsulates logic to export the html
 public class Exporter
 {
     // Instantiate the freemarker configuration instance
     // and get our settings configured
     private Configuration cfg = new Configuration();
 
     public Exporter() {
         try {
            cfg.setDirectoryForTemplateLoading(new File("."+File.separator+"io"));
         }
        catch (IOException e) {
             System.out.println("Unable to find the resource directory.");
         }
         cfg.setDefaultEncoding("UTF-8");
         cfg.setObjectWrapper(ObjectWrapper.BEANS_WRAPPER);
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
     }
 
     public void exportCourseToHTML(MyCourse course, String filepath) {
         try {
             Writer templateWriter = new FileWriter(filepath);
 
             Template temp = cfg.getTemplate("export_template.html");
             temp.process(course, templateWriter);
 
             templateWriter.close();
         }
         catch (IOException e) {
             System.err.println("Unable to access given filepath.");
             e.printStackTrace();
         }
         catch (TemplateException e) {
             System.err.println("Could not process template.");
             e.printStackTrace();
         }
     }
 
 }
