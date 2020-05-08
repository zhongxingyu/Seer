 package berlin.reiche.virginia;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.TreeMap;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import berlin.reiche.virginia.model.Equipment;
 import berlin.reiche.virginia.model.User;
 import freemarker.template.Configuration;
 import freemarker.template.DefaultObjectWrapper;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 /**
  * The main servlet of the application which handles all incoming HTTP requests
  * which are not dedicated to a special servlet.
  * 
  * @author Konrad Reiche
  * 
  */
 @SuppressWarnings("serial")
 public class AppServlet extends HttpServlet {
 
     /**
      * Paths to the web resources.
      */
     static final String WEB_PATH = "site/";
     static final String ERROR_SITE = "ftl/error.ftl";
     static final String NOT_FOUND_SITE = "ftl/404.ftl";
 
     private static final String LOGIN_SITE = "ftl/login.ftl";
     private static final String MAIN_SITE = "ftl/main.ftl";
     private static final String DEFAULT_VALUES_PATH = "site/resources/default-values.properties";
 
     /**
      * Further constants which appear more than one in the source code.
      */
     static final String LOGIN_ATTRIBUTE = "login.isLoggedIn";
     static final String DESTINATION_ATTRIBUTE = "login.destination";
     static final String REQUEST_HEADLINE_VAR = "requestHeadline";
 
     /**
      * Regular expression for matching a MongoDB object ids.
      */
     static final String ID_REGEX = "[a-f0-9]*";
 
     /**
      * Configuration used for the Freemarker template processing.
      */
     static Configuration configuration;
 
     private final static AppServlet instance = new AppServlet();
 
     /**
      * The configuration for the template engine Freemarker is set up with
      * default settings.
      */
     static {
 
         try {
             configuration = new Configuration();
             configuration.setDirectoryForTemplateLoading(new File(WEB_PATH));
             configuration.setObjectWrapper(new DefaultObjectWrapper());
         } catch (IOException e) {
             System.err.println("The path " + WEB_PATH
                     + " could not be retrieved.");
             e.printStackTrace();
         }
     }
 
     /**
      * The constructor is private in order to enforce the singleton pattern.
      */
     private AppServlet() {
 
     }
 
     /**
      * Parses the HTTP request and writes the response by using the template
      * engine.
      */
     @Override
     protected void doGet(HttpServletRequest request,
             HttpServletResponse response) throws ServletException, IOException {
 
         String path = request.getPathInfo();
         Map<String, Object> data = getDefaultData();
         Writer writer = response.getWriter();
 
         if (path == null) {
             throw new IOException("The associated path information is null");
         }
 
         if (path.startsWith("/resources")) {
             processTemplate(path, data, writer);
             return;
         }
 
 
         if (path.equals("/")) {
             processTemplate(MAIN_SITE, data, writer);
        } else if (path.equals("/login")) {
             if (request.getRemoteUser() != null) {
                 response.sendRedirect("/");
             } else {
                 processTemplate(LOGIN_SITE, data, writer);
             }
         } else if (path.startsWith("/login/error")) {
             data.put("hasLoginFailed", true);
             AppServlet.processTemplate(LOGIN_SITE, data, response.getWriter());
         } else if (path.equals("/logout")) {
             request.getSession().invalidate();
             response.sendRedirect("/login");
         } else {
             processTemplate(NOT_FOUND_SITE, data, writer);
         }
 
     }
 
     /**
      * Processed the given template file with the provided data. The result is
      * written immediately to the given writer.
      * 
      * @param relativePath
      *            the relative path from the base directory pointing to the
      *            template file.
      * @param data
      *            the root node of the data model.
      * @param writer
      *            the writer to which the processed template is written.
      * @throws IOException
      *             if an I/O exception occurs due to writing or flushing.
      */
     public static void processTemplate(String relativePath,
             Map<String, ?> data, Writer writer) throws IOException {
 
         try {
             Template template = AppServlet.configuration
                     .getTemplate(relativePath);
             template.process(data, writer);
             writer.flush();
         } catch (TemplateException e) {
             System.err.println("The template" + relativePath
                     + "could not be processed properly.");
             e.printStackTrace();
         }
     }
 
     /**
      * Generates a data model out of the default value property file and other
      * sources.
      * 
      * @return the data model.
      * @throws IOException
      *             if the file with the default value properties could not been
      *             found or an error occurred during reading from it.
      * 
      */
     static Map<String, Object> getDefaultData() throws IOException {
 
         Map<String, Object> defaultData = new TreeMap<>();
         Properties defaultValues = new Properties();
         FileInputStream input = new FileInputStream(DEFAULT_VALUES_PATH);
         defaultValues.load(input);
         for (Entry<Object, Object> entry : defaultValues.entrySet()) {
             defaultData.put((String) entry.getKey(), (String) entry.getValue());
         }
 
         Equipment equipment = MongoDB.get(Equipment.class);
         defaultData.put("equipment", equipment);
         return defaultData;
     }
 
     /**
      * @return a singleton instance of {@link RoomServlet}.
      */
     public static AppServlet getInstance() {
         return instance;
     }
 
     /**
      * @param request
      *            provides request information for HTTP servlets.
      * 
      * @return the current user logged in, null if no user is logged in.
      */
     static User getCurrentUser(HttpServletRequest request) {
         return (User) request.getSession().getAttribute(LOGIN_ATTRIBUTE);
     }
 }
