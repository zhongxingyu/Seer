/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
 package de.kiezatlas.plugins.view.maps;
 
 import de.deepamehta.core.osgi.PluginActivator;
 import de.deepamehta.core.service.ClientState;
 import java.io.InputStream;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 
 @Path("/")
 @Produces(MediaType.TEXT_HTML)
 public class MapsPlugin extends PluginActivator {
 
     @GET
    @Path("/")
     @Produces(MediaType.TEXT_HTML)
     public InputStream getStartView(@HeaderParam("Cookie") ClientState clientState) {
         return invokeStartView();
     }
 
     // ------------------------------------------------------------------------------------------------ Private Methods
 
     private InputStream invokeStartView() {
         try {
             return dms.getPlugin("de.kiezatlas.new-maps").getResourceAsStream("web/script/index.html");
         } catch (Exception e) {
             throw new WebApplicationException(e);
         }
     }
 
 }
