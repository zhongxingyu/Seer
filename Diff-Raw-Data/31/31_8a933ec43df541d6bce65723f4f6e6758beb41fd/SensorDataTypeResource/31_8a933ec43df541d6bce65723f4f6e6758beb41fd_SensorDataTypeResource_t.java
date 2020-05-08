 package org.hackystat.sensorbase.resource.sensordatatypes;
 
 import org.hackystat.sensorbase.logger.SensorBaseLogger;
 import org.hackystat.sensorbase.logger.StackTrace;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.DomRepresentation;
 import org.restlet.resource.Representation;
 import org.restlet.resource.Resource;
 import org.restlet.resource.Variant;
 
 /**
  * Implements a Restlet Resource for obtaining individual SensorDataType resources. 
  * @author Philip Johnson
  */
 public class SensorDataTypeResource extends Resource {
   
   /** To be retrieved from the URL. */
   private String sdtName; 
   
   /**
    * Provides the following representational variants: TEXT_XML.
    * @param context The context.
    * @param request The request object.
    * @param response The response object.
    */
   public SensorDataTypeResource(Context context, Request request, Response response) {
     super(context, request, response);
     this.sdtName = (String) request.getAttributes().get("sensordatatypename");
     getVariants().clear(); // copyied from BookmarksResource.java, not sure why needed.
     getVariants().add(new Variant(MediaType.TEXT_XML));
   }
   
   /**
    * Returns the representation of the SensorDataTypes resource. 
    * @param variant The representational variant requested.
    * @return The representation. 
    */
   @Override
   public Representation getRepresentation(Variant variant) {
     Representation result = null;
     SdtManager manager = (SdtManager)getContext().getAttributes().get("SdtManager");
     if (manager == null) {
       throw new RuntimeException("Failed to find SdtManager");
     }
     if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
       result = new DomRepresentation(MediaType.TEXT_XML, 
           manager.marshallSdt(this.sdtName));
     }
     return result;
   }
   
   /** 
    * Indicate the PUT method is supported. 
    * @return True.
    */
   @Override
   public boolean allowPut() {
       return true;
   }
 
   /**
    * Implement the PUT method that creates a new SDT. 
    * <ul>
    * <li> The XML must be marshallable into an SDT instance using the SDT XmlSchema definition.
    * <li> The SDT name in the URI string must match the SDT name in the XML.
    * </ul>
    * @param entity The XML representation of the new SDT. 
    */
   @Override
   public void put(Representation entity) {
     String entityString = null;
     SensorDataType sdt;
     // Try to make the XML payload into an SDT, return failure if this fails. 
     try { 
       entityString = entity.getText();
       sdt = SdtManager.unmarshallSdt(entityString);
     }
     catch (Exception e) {
       SensorBaseLogger.getLogger().warning("Bad Sdt Definition in PUT: " + StackTrace.toString(e));
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Bad SDT: " + entityString);
       return;
     }
     // Return failure if the URI SdtName is not the same as the XML SdtName.
     if (!(this.sdtName.equals(sdt.getName()))) {
       getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "URI/XML name mismatch");
       return;
     }
     // otherwise we add it to the Manager and return success.
    SdtManager manager = (SdtManager)getContext().getAttributes().get("SdtManager");
     manager.putSdt(sdt);      
     getResponse().setStatus(Status.SUCCESS_CREATED);
   }
   
   /** 
    * Indicate the DELETE method is supported. 
    * @return True.
    */
   @Override
   public boolean allowDelete() {
       return true;
   }
   
   /**
    * Implement the DELETE method that deletes an existing SDT given its name.
    */
   @Override
   public void delete() {
     SdtManager manager = (SdtManager)getContext().getAttributes().get("SdtManager");
     manager.deleteSdt(sdtName);      
     getResponse().setStatus(Status.SUCCESS_OK);
   }
 }
