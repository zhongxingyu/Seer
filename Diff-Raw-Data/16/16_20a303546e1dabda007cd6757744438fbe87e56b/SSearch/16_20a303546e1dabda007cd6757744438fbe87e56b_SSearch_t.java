 package gov.nih.ncgc.bardplugin;
 
 import chemaxon.struc.Molecule;
 import com.sun.jersey.api.NotFoundException;
 import gov.nih.ncgc.bard.plugin.IPlugin;
 import gov.nih.ncgc.bard.plugin.PluginManifest;
 import gov.nih.ncgc.bard.tools.SearchResultHandler;
 import gov.nih.ncgc.bard.tools.Util;
 import gov.nih.ncgc.search.MoleculeService;
 import gov.nih.ncgc.search.SearchParams;
 import gov.nih.ncgc.search.SearchService2;
 import gov.nih.ncgc.util.MolRenderer;
 
 import javax.imageio.ImageIO;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.io.*;
 
 
 /**
  * A simplistic plugin that provides access to chemical structure search.
  * <p/>
  * The <code>runSearch</code> is the method of interest and accepts a query string
  * along with a type (super, sub or sim), cutoff (a float between 0 and 1) and
  * a method (search, count).
  *
  * @author Rajarshi Guha
  */
 @Path("/ssearch")
 public class SSearch implements IPlugin {
     static final String VERSION = "1.1";
 
     SearchService2 search = null;
 
     public SSearch() throws Exception {
 
         // get an instance of the search service
         search = Util.getSearchService();
     }
 
     /**
      * Get a description of the plugin.
      * <p/>
      * In the implementing class, this method should be annotated using
      * <p/>
      * <pre>
      *
      * @return a description of the plugin.
      * @GET
      * @Path("/_info") </pre>
      * <p/>
      * where the annotations are from the <code>javax.ws.rs</code> hierarchy.
      */
     @GET
     @Produces("text/plain")
     @Path("/_info")
     public String getDescription() {
         return "Provides a simple interface to chemical structure searches";
     }
 
 
     @GET
     @Produces("text/plain")
     @Path("/")
     public Response getSearch(@QueryParam("q") String query,
                               @QueryParam("type") String type,
                               @QueryParam("cutoff") String cutoff,
                               @QueryParam("method") String method) {
         return Response.ok(doSearch(query, type, cutoff, method)).build();
     }
 
     @POST
     @Produces("text/plain")
     @Path("/")
     public Response postSearch(@FormParam("q") String query,
                                @FormParam("type") String type,
                                @FormParam("cutoff") String cutoff,
                                @FormParam("method") String method) {
         return Response.ok(doSearch(query, type, cutoff, method)).build();
     }
 
     /**
      * oerform a chemical structure search.
      *
      * @param query  A SMILE string (URL encoded if required)
      * @param type   type of search. Can be super, sub or sim
      * @param cutoff a floating point value for similarity cutoff (0-1)
      * @param method specifying 'search' returns matching molecules and
      *               specifying 'count' returns an approximate count of the
      *               matches
      * @return an SDF formatted response with the matching molecules. The default
      *         response will have a Content-type of text/plain.
      */
     private String doSearch(String query,
                             String type,
                             String cutoff,
                             String method) {
 
         if (search == null)
             throw new WebApplicationException(new Exception("Did not get an instance of the search service"), 500);
 
         if (query == null) throw new WebApplicationException(new Exception("Need to specify the q parameter"), 400);
 
         SearchParams params = null;
         if (type != null) {
             if (type.startsWith("sub")) {
                 params = SearchParams.substructure();
             } else if (type.startsWith("super")) {
                 params = SearchParams.superstructure();
             } else if (type.startsWith("sim")) {
                 params = SearchParams.similarity();
                 if (cutoff != null) {
                     try {
                         params.setSimilarity(Double.parseDouble(cutoff));
                     } catch (NumberFormatException e) {
                         throw new WebApplicationException(new Exception("Bogus similarity value specified"), 400);
                     }
                 }
 
             } else if (type.startsWith("exact")) {
                 params = SearchParams.exact();
             } else {
                 params = SearchParams.substructure();
             }
         } else {
             params = SearchParams.substructure();
         }
 
         if (method == null) {
             method = "search";
         }
 
         Writer writer = new StringWriter();
         PrintWriter pw = new PrintWriter(writer);
         if ("search".equalsIgnoreCase(method)) {
             search.search(query, params, new SearchResultHandler(params, pw, 0, 10));
         } else if ("count".equalsIgnoreCase(method)) {
             pw.println(search.count(query, params));
         } else
             throw new WebApplicationException(new Exception("Unsupport method " + method), 400);
         return writer.toString();
     }
 
     /**
      * Return a 2D depiction of a CID.
      * <p/>
      * This method serves to highlight how one can use the structure search service
      * to obtain a molecule by CID (as opposed ot going via the DBUtils class.
      *
      * @param cid The CID of the structure
      * @param s   the size (between 16 and 512)
      * @param c   the background color (in hexadecimal form)
      * @param a   a comma seperated list of atoms indices, to highlight. Useful for
      *            displaying a substructure
      * @return a PNG depiction
      */
     @GET
     @Path("/image/{cid}")
     public Response getImage(@PathParam("cid") String cid,
                              @QueryParam("s") Integer s,
                              @QueryParam("c") String c,
                              @QueryParam("a") String a) throws IOException {
 
         MoleculeService molsrv = (MoleculeService) search;
         Molecule mol = molsrv.getMol(cid);
         if (mol == null) throw new NotFoundException("No molecule for CID = " + cid);
         String param;
         MolRenderer renderer = new MolRenderer();
 
         // size
         int size = 120;
 
         if (s != null && s >= 16 && s <= 512) size = s;
         else throw new WebApplicationException(new Exception("Invalid size specified"), 400);
 
         // atom
         if (a != null) {
             for (String idx : a.split(",")) {
                 try {
                     int i = Integer.parseInt(idx);
                     if (i > 0 && i <= mol.getAtomCount()) {
                         mol.getAtom(i - 1).setAtomMap(1);
                     }
                 } catch (NumberFormatException ex) {
                     throw new WebApplicationException(ex, 400);
                 }
             }
         }
 
         if (c != null) {
             try {
                 Color color = Color.decode(c);
                 renderer.setBackground(color);
             } catch (NumberFormatException ex) {
                 throw new WebApplicationException(ex, 400);
             }
         }
 
         BufferedImage img = renderer.createImage(mol, size);
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ImageIO.write(img, "png", baos);
         return Response.ok(baos.toByteArray()).type("image/png").build();
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
         pm.setTitle("Structure Search Plugin");
         pm.setDescription("Uses the BARD structure search service to perform similarity and substructure searches");
         pm.setVersion(VERSION);
 
         PluginManifest.PluginResource res1 = new PluginManifest.PluginResource();
         res1.setPath("/image/{cid}");
         res1.setMimetype("image/png");
         res1.setMethod("GET");
         res1.setArgs(new PluginManifest.PathArg[]{new PluginManifest.PathArg("cid", "integer")});
 
         pm.setResources(new PluginManifest.PluginResource[]{res1});
 
         return pm.toJson();
     }
 
 
 }
