 package controllers.mockups;
 
 import models.mockups.Mockup;
 import play.mvc.Controller;
 import play.mvc.results.NotFound;
 import play.mvc.results.RenderTemplate;
 import play.templates.Template;
 
 import java.util.HashMap;
 import java.util.List;
 
 import static models.mockups.Mockup.allMockups;
 import static models.mockups.Mockup.mockupByName;
 import static org.apache.commons.lang.StringUtils.isBlank;
 
 @SuppressWarnings("UnusedDeclaration")
 public class Mockups extends Controller {
 
 
     public static void show(String m) {
         if (isBlank(m)) {
             List<Mockup> mockups = allMockups();
             render(mockups);
         } else {
             Mockup mockup = mockupByName(m);
             if (mockup == null) {
                throw new NotFound("Mockup [" + m + "] not found");
             } else if (mockup.isDirectory()) {
                 List<Mockup> mockups = allMockups(m);
                 render(mockups);
             } else {
                 Template template = mockup.getTemplate();
                 HashMap<String, Object> args = new HashMap<String, Object>();
                 throw new RenderTemplate(template, args);
             }
         }
     }
 
 }
