 import freemarker.cache.ClassTemplateLoader;
 import freemarker.cache.FileTemplateLoader;
 import freemarker.cache.MultiTemplateLoader;
 import freemarker.cache.TemplateLoader;
 import freemarker.template.*;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 /**
  * Author: Mat Schaffer <matschaffer@netflix.com>
  * Created: 8/17/13 10:06 PM
  */
 public class RevealHandler extends AbstractHandler {
     private Configuration cfg;
 
     public RevealHandler() throws IOException {
         super();
         cfg = new Configuration();
 
         ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(getClass(), "");
 
         File localResources = new File("src/main/resources");
         TemplateLoader[] loaders;
 
         if (localResources.exists()) {
             FileTemplateLoader devTemplateLoader = new FileTemplateLoader(localResources);
             loaders = new TemplateLoader[] { devTemplateLoader, classTemplateLoader };
         } else {
             loaders = new TemplateLoader[] { classTemplateLoader };
         }
 
         MultiTemplateLoader mtl = new MultiTemplateLoader(loaders);
 
         cfg.setTemplateLoader(mtl);
 
         cfg.setObjectWrapper(new DefaultObjectWrapper());
         cfg.setDefaultEncoding("UTF-8");
         cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
         cfg.setIncompatibleImprovements(new Version(2, 3, 20));
     }
 
     @Override
     public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
         if(!request.getRequestURI().equals("/")){
             return;
         }
 
         baseRequest.setHandled(true);
 
         HashMap<String,Object> data = new HashMap<>();
         data.put("config", SlideConfiguration.parse(new File("config.json")));
         data.put("slides", new SlideParser().getSlides());
 
         Template template = cfg.getTemplate("reveal.html.ftl");
         try {
             template.process(data, response.getWriter());
             response.setContentType("text/html");
             response.setStatus(HttpServletResponse.SC_OK);
         } catch (TemplateException e) {
             response.setContentType("text/plain");
             response.setStatus(500);
             response.getWriter().println("Couldn't find reveal.html.ftl");
         }
     }
 }
