 package gov.nih.ncgc.bardplugin;
 
 import gov.nih.ncgc.bard.entity.Assay;
 import gov.nih.ncgc.bard.plugin.IPlugin;
 import gov.nih.ncgc.bard.plugin.PluginManifest;
 import gov.nih.ncgc.bard.tools.DBUtils;
 
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.sql.SQLException;
 
 
 /**
  * A BARD plugin implementing trivial assay related operations.
  *
  * @author Rajarshi Guha
  */
 @Path("/ainfo")
 public class AssayInfo implements IPlugin {
     private static final String VERSION = "1.1";
 
     /**
      * Get a description of the plugin.
      *
      * @return a description of the plugin.
      */
     @GET
     @Produces("text/plain")
     @Path("/_info")
     public String getDescription() {
         return "description of the plugin, including inputs, outputs and media types";
     }
 
     @GET
     @Path("/{aid}/title")
     public Response getAssayTitle(@PathParam("aid") Long aid) {
         DBUtils db = new DBUtils();
         try {
             Assay assay = db.getAssayByAid(aid);
             db.closeConnection();
             return Response.ok(assay.getName(), MediaType.TEXT_PLAIN).build();
         } catch (SQLException e) {
             throw new WebApplicationException(500);
         }
     }
 
 
     @GET
     @Path("/{aid}/description")
     public Response getAssayDescription(@PathParam("aid") Long aid) {
         DBUtils db = new DBUtils();
         try {
             Assay assay = db.getAssayByAid(aid);
             db.closeConnection();
             return Response.ok(assay.getDescription(), MediaType.TEXT_PLAIN).build();
         } catch (SQLException e) {
             throw new WebApplicationException(500);
         }
     }
 
     /**
      * Get the manifest for this plugin.
      * <p/>
      * This should be an JSON document conforming
      * to the <a href="https://github.com/ncatsdpiprobedev/bardplugins/blob/master/resources/manifest.json">plugin manifest schema</a>
      * <p/>
      * In the implementing class, this method should be annotated using
      * <pre>
      * @GET
      * @Path("/_manifest")
      * @Produces(MediaType.APPLICATION_JSON) </pre>
      * where the annotations are from the <code>javax.ws.rs</code> hierarchy.
      *
      * @return a JSON document containing the plugin manifest
      */
     @GET
     @Path("/_manifest")
     @Produces(MediaType.APPLICATION_JSON)
     public String getManifest() {
         PluginManifest pm = new PluginManifest();
         pm.setAuthor("Rajarshi Guha");
         pm.setAuthorEmail("guhar@mail.nih.gov");
         pm.setMaintainer(pm.getAuthor());
        pm.setMaintainerEmail(pm.getAuthorEmail());
         pm.setTitle("BARD Assay Information");
         pm.setDescription("A brief description will go here");
         pm.setVersion(VERSION);
 
         PluginManifest.PluginResource res1 = new PluginManifest.PluginResource();
         res1.setPath("/{aid}/title");
         res1.setMimetype("text/plain");
         res1.setMethod("GET");
         res1.setArgs(new PluginManifest.PathArg[]{new PluginManifest.PathArg("aid", "integer")});
 
         PluginManifest.PluginResource res2 = new PluginManifest.PluginResource();
         res2.setPath("/{aid}/description");
         res2.setMimetype("text/plain");
         res2.setMethod("GET");
         res2.setArgs(new PluginManifest.PathArg[]{new PluginManifest.PathArg("aid", "integer")});
 
         pm.setResources(new PluginManifest.PluginResource[]{res1, res2});
 
         return pm.toJson();
     }
 
 }
