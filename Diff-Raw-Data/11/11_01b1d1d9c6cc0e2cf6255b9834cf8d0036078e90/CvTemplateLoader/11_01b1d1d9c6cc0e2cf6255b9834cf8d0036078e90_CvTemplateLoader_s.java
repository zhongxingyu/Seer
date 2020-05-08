 package org.isma.tools.cv;
 
 import org.apache.jasper.JspC;
 import org.springframework.mock.web.MockServletConfig;
 
 import javax.servlet.Servlet;
 import java.io.File;
 
 public class CvTemplateLoader {
     public static final String PROJECT_DIR = System.getProperty("user.dir");
    public static final String MODULE_DIR = "";//"isma-tools";
     public static final String TARGET_CLASSES_DIR = String.format("%s/%s/target/classes/", PROJECT_DIR, MODULE_DIR);
     private static final String TEMPLATE_RESOURCE_DIR = "/templates/";
     private static final String ORG_APACHE_JSP_PACKAGE = "org.apache.jsp";
 
 
     public Servlet load(Template template) throws Exception {
         System.out.printf("loadTemplate(%s, %s)\n", template.getDirectory(), template.getFile());
         File templateFile = getTemplateFile(template);
         compileJsp(templateFile.getParentFile());
         String jspServletClassName = templateFile.getName().replace(".jsp", "_jsp");
         return loadServlet(jspServletClassName);
     }
 
     public File getTemplateFile(Template template) {
         String currentPackage = CvMain.class.getPackage().getName().replace('.', '/');
         String templatePath = String.format("%s/%s/%s/%s", currentPackage, TEMPLATE_RESOURCE_DIR, template.getDirectory(), template.getFile());
         while (templatePath.contains("//")) {
             templatePath = templatePath.replace("//", "/");
         }
         return new File(TARGET_CLASSES_DIR, templatePath);
     }
 
     private void compileJsp(File jspDirectory) throws Exception {
         String currentPackage = CvMain.class.getPackage().getName().replace('.', '/');
         System.out.println(currentPackage);
         String[] args = {
                 "-uriroot", jspDirectory.getAbsolutePath()
                 , "-v",
                 "-compile",
                 "-d", TARGET_CLASSES_DIR//,
 //                "-source", "1.5",
 //                "-target", "1.5"
         };
         jspc(args);
 
     }
 
     private void jspc(String[] args) {
         JspC.main(args);
     }
 
     private Servlet loadServlet(String jspServletClassName) throws Exception {
         jspServletClassName = ORG_APACHE_JSP_PACKAGE + "." + jspServletClassName;
         Class jspClass = Class.forName(jspServletClassName);
         Servlet instance = (Servlet) jspClass.newInstance();
         instance.init(new MockServletConfig(jspServletClassName));
         return instance;
     }
 }
