 package edu.cudenver.bios.powersvc.resource;
 
 import java.io.IOException;
 
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Resource;
 import org.restlet.resource.ResourceException;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 import org.restlet.resource.XmlRepresentation;
 
 import edu.cudenver.bios.powersvc.application.PowerLogger;
 
 public class EchoResource extends Resource
 {
     private String modelName = null;
 
     public EchoResource(Context context, Request request, Response response) 
     {
         super(context, request, response);
 
         modelName = (String) request.getAttributes().get("modelName");
 
         getVariants().add(new Variant(MediaType.APPLICATION_XML));
     }
 
     @Override
     public boolean allowGet()
     {
         return false;
     }
 
     @Override
     public boolean allowPut()
     {
         return false;
     }
 
     @Override
     public boolean allowPost() 
     {
         return  true;
     }
 
     @Override 
     public void acceptRepresentation(Representation entity)
     {
         try
         {
             // build the response xml
            getResponse().setEntity(entity); 
             
             Form responseHeaders = (Form) getResponse().getAttributes().get("org.restlet.http.headers");  
             if (responseHeaders == null)  
             {  
             	responseHeaders = new Form();  
             	getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);  
             }  
             responseHeaders.add("Content-disposition", "attachment; filename=study.xml");
             
             getResponse().setStatus(Status.SUCCESS_CREATED);
         }
 //        catch (IOException ioe)
 //        {
 //            PowerLogger.getInstance().error(ioe.getMessage());
 //            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 //        }
         catch (IllegalArgumentException iae)
         {
             PowerLogger.getInstance().error(iae.getMessage());
             getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
         }
//        catch (ResourceException re)
//        {
//            PowerLogger.getInstance().error(re.getMessage());
//            getResponse().setStatus(re.getStatus());
//        }
 
     }
 
 }
