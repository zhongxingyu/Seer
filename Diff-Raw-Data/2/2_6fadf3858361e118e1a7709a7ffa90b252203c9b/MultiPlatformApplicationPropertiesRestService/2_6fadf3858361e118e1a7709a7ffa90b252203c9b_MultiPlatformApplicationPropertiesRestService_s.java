 /**
  *** This software is licensed under the GNU General Public License, version 3.
  *** See http://www.gnu.org/licenses/gpl.html for full details of the license terms.
  *** Copyright 2012 Andrew Heald.
  */
 
 package uk.org.sappho.applications.restful.transcript.reporter;
 
 import freemarker.template.Configuration;
 import uk.org.sappho.applications.restful.transcript.jersey.AbstractRestService;
 import uk.org.sappho.applications.services.transcript.registry.ConfigurationException;
 
 import javax.servlet.ServletContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 
 @Path("/all-app-props/{template}/{application}")
 public class MultiPlatformApplicationPropertiesRestService extends AbstractRestService {
 
     @QueryParam("environments")
     private String environments;
     @PathParam("application")
     private String application;
     @PathParam("template")
     private String template;
    @QueryParam("includeVCSProps")
     private boolean includeVersionControlProperties;
     @Context
     private ServletContext servletContext;
 
     @GET
     @Produces(MediaType.TEXT_HTML)
     public String getProperty() throws ConfigurationException {
 
         String[] environmentList = null;
         if (environments != null && environments.length() != 0) {
             environmentList = environments.split(",");
         }
         Configuration freemarkerConfiguration = new Configuration();
         freemarkerConfiguration.setServletContextForTemplateLoading(servletContext, "templates/" + template);
         return getService().getInstance(MultiPlatformApplicationPropertiesReport.class).generate(
                 environmentList, application, includeVersionControlProperties, freemarkerConfiguration);
     }
 }
