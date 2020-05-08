 package org.hackystat.sensorbase.resource.ping;
 
 import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
 import org.restlet.Context;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.resource.Representation;
 import org.restlet.resource.StringRepresentation;
 import org.restlet.resource.Variant;
 
 /**
  * The PingResource responds to a GET <host>/ping with the name of this service. 
  * @author Philip Johnson
  */
 public class PingResource extends SensorBaseResource {
   /**
    * The standard constructor.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public PingResource(Context context, Request request, Response response) {
     super(context, request, response);
   }
   
   /**
   * Returns the string "SensorBase".
    * @param variant The representational variant requested.
    * @return The representation. 
    */
   @Override
   public Representation getRepresentation(Variant variant) {
     return new StringRepresentation("SensorBase");
   }
   
 
 }
