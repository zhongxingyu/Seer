 /**
  * 
  */
 package com.github.podd.restlet;
 
 import java.util.Map;
 
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.service.StatusService;
 
 import freemarker.template.Configuration;
 
 /**
  * This status service is based on the standard Restlet guide for returning custom error pages.
  * 
  * TODO: set this to Application via application.setStatusService()
  * TODO: create the templates used (i.e. PropertyUtils.PROPERTY_TEMPLATE...)
  * 
  * @author Peter Ansell p_ansell@yahoo.com
  * copied from the OAS project (https://github.com/ansell/oas)
  */
 public class PoddStatusService extends StatusService
 {
     
     private Configuration freemarkerConfiguration;
     
     /**
      * 
      */
     public PoddStatusService(final Configuration freemarkerConfiguration)
     {
         super();
         this.freemarkerConfiguration = freemarkerConfiguration;
     }
     
     /**
      * @param enabled
      */
     public PoddStatusService(final Configuration freemarkerConfiguration, final boolean enabled)
     {
         super(enabled);
         this.freemarkerConfiguration = freemarkerConfiguration;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see org.restlet.service.StatusService#getRepresentation(org.restlet.data.Status,
      * org.restlet.Request, org.restlet.Response)
      */
     @Override
     public Representation getRepresentation(final Status status, final Request request, final Response response)
     {
         final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(request);
         
         dataModel.put("contentTemplate", "error.html.ftl");
         dataModel.put("pageTitle", "An error occurred : HTTP " + status.getCode());
         dataModel.put("error_code", Integer.toString(status.getCode()));
         
         final StringBuilder message = new StringBuilder();
         if(status.getDescription() != null)
         {
             message.append(status.getDescription());
            message.append(" (");
         }
         if(status.getThrowable() != null && status.getThrowable().getMessage() != null)
         {
             message.append(status.getThrowable().getMessage());
             message.append(")");
         }
         dataModel.put("message", message.toString());
         
         // TODO: Support non-HTML error representations
         return RestletUtils.getHtmlRepresentation("poddBase.html.ftl", dataModel, MediaType.TEXT_HTML,
                 this.freemarkerConfiguration);
     }
 }
