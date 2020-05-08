 package gov.nih.ncgc.bardplugin;
 
 import gov.nih.ncgc.bard.plugin.IPlugin;
 import gov.nih.ncgc.bard.plugin.PluginManifest;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.*;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.HttpHeaders;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.*;
 
 /**
  * A BARD plugin whose main goal is to retrieve data from an external service.
  * <p/>
  * This can be used as an example of a stub plugin, which offloads the actual
  * job to an external service (which can be implemented in arbitrary) languages
  *
  * @author Rajarshi Guha
  */
 @Path("/csls")
 public class CSLS implements IPlugin {
     private static final String VERSION = "1.1";
 
     public CSLS() {
 
     }
 
     public CSLS(
             @Context ServletConfig servletConfig,
             @Context ServletContext servletContext,
             @Context HttpServletRequest httpServletRequest,
             @Context HttpHeaders headers) {
         // use the args to access parameters etc
     }
 
     /**
      * Get a description of the plugin.
      *
      * @return a description of the plugin.
      */
     @GET
     @Produces("text/plain")
     @Path("/_info")
     public String getDescription() {
         return "Looks up the SMILES string for a chemical name using the NCI gov.nih.ncgc.bardplugin.CSLS. Return type is plain text";
     }
 
     @GET
     @Path("/{term}")
     @Produces("text/plain")
     public Response getTermFromCsls(@PathParam("term") String term) {
         try {
             URL url = new URI("http://cactus.nci.nih.gov/chemical/structure/" + URLEncoder.encode(term, "UTF-8") + "/smiles").toURL();
             URLConnection con = url.openConnection();
             con.connect();
             HttpURLConnection hcon = (HttpURLConnection) con;
             int response = hcon.getResponseCode();
             if (response == 200) {
                 StringBuilder sb = new StringBuilder();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                 String str;
                 while ((str = reader.readLine()) != null) sb.append(str);
                 reader.close();
                 return Response.ok(sb.toString(), MediaType.TEXT_PLAIN).build();
             } else throw new WebApplicationException(response);
         } catch (MalformedURLException e) {
             throw new WebApplicationException(e, 500);
         } catch (URISyntaxException e) {
             throw new WebApplicationException(e, 500);
         } catch (IOException e) {
             throw new WebApplicationException(e, 500);
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
        pm.setMaintainerEmail(pm.getMaintainerEmail());
         pm.setTitle("Chemical Structure Lookup Service Wrapper");
         pm.setDescription("Uses the NCI CSLS service to convert a chemical name to a structure");
         pm.setVersion(VERSION);
 
         PluginManifest.PluginResource res1 = new PluginManifest.PluginResource();
         res1.setPath("/{term}");
         res1.setMimetype("text/plain");
         res1.setMethod("GET");
         res1.setArgs(new PluginManifest.PathArg[]{new PluginManifest.PathArg("term", "string")});
 
         pm.setResources(new PluginManifest.PluginResource[]{res1});
 
         return pm.toJson();
     }
 
 
 }
