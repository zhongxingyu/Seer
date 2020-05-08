 // License: GPL. For details, see LICENSE file.
 package org.openstreetmap.beboj.server;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.openstreetmap.beboj.client.io.API;
 import org.openstreetmap.beboj.shared.data.osm.SimpleDataSet;
 import org.openstreetmap.beboj.shared.data.osm.UploadRequestData;
 import org.openstreetmap.beboj.shared.data.osm.UploadResponseData;
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.APIDataSet;
 import org.openstreetmap.josm.data.Bounds;
 import org.openstreetmap.josm.data.Preferences;
 import org.openstreetmap.josm.data.osm.Changeset;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.data.osm.PrimitiveId;
 import org.openstreetmap.josm.data.osm.SimplePrimitiveId;
 import org.openstreetmap.josm.data.projection.Mercator;
 import org.openstreetmap.josm.io.DiffResultEntry;
 import org.openstreetmap.josm_server.gui.io.UploadStrategySpecification;
 import org.openstreetmap.josm_server.gui.progress.NullProgressMonitor;
 import org.openstreetmap.josm_server.io.BoundingBoxDownloader;
 import org.openstreetmap.josm_server.io.OsmServerWriter;
 import org.openstreetmap.josm_server.io.OsmTransferException;
 import org.openstreetmap.josm_server.io.auth.CredentialsManagerResponse;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 @SuppressWarnings("serial")
 public class APIImpl extends RemoteServiceServlet implements API {
 
     @Override
     public String greetServer(String input)
             throws IllegalArgumentException {
         return String.format("Hi %s!", input);
     }
 
     @Override
     public SimpleDataSet downloadOsmData(double minlat, double minlon, double maxlat, double maxlon) {
         initialize();
         BoundingBoxDownloader down = new BoundingBoxDownloader(new Bounds(minlat, minlon, maxlat, maxlon));
         DataSet ds = null;
         try {
             ds = down.parseOsm(NullProgressMonitor.INSTANCE);
         } catch (OsmTransferException e) {
             e.printStackTrace();
             return null;
         }
         SimpleDataSet sds = SimpleDataSet.fromDataSet(ds);
         return sds;
     }
 
     @Override
     public UploadResponseData uploadOsmData(UploadRequestData request) {
         initialize();
        OsmServerWriter writer = new OsmServerWriter();
        DataSet dsInput = request.data.toDataSet();
        APIDataSet apiData = new APIDataSet(dsInput);
         Map<String, String> csTags = new HashMap<String, String>();
         csTags.put("comment", request.changeSetComment);
         csTags.put("created_by", "Beboj alpha");
         Changeset cs = new Changeset();
         cs.setKeys(csTags);
 
         CredentialsManagerResponse credentials = new CredentialsManagerResponse();
         credentials.setUsername(request.username);
         credentials.setPassword(request.password.toCharArray());
 
         UploadResponseData response = new UploadResponseData();
 
         Map<PrimitiveId, DiffResultEntry> diffResults = null;
 
         try {
             writer.uploadOsm(
                     new UploadStrategySpecification(),
                    apiData.getPrimitives(),
                     cs,
                     credentials,
                     NullProgressMonitor.INSTANCE
             );
         } catch (OsmTransferException e) {
             e.printStackTrace();
         } finally {
             if (writer != null) {
                 diffResults = writer.getDiffResults();
             }
             writer = null;
         }
         if (diffResults != null) {
             response.diffResults = new HashMap<SimplePrimitiveId, DiffResultEntry>();
             for (Entry<PrimitiveId, DiffResultEntry> e : diffResults.entrySet()) {
                 response.diffResults.put(new SimplePrimitiveId(e.getKey().getUniqueId(), e.getKey().getType()), e.getValue());
             }
         }
         return response;
     }
 
     private void initialize() {
         if (Main.pref == null) {
             Main.pref = new Preferences();
             Main.pref.put("osm-server.url", "http://api06.dev.openstreetmap.org/api");
         }
         if (Main.proj == null) {
             Main.proj = new Mercator();
         }
         if (Main.main == null) {
             Main.main = new Main();
         }
     }
 }
