 package org.openstreetmap.josm.plugins.ods.entities.internal;
 
 import java.util.concurrent.Callable;
 
import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.gui.progress.NullProgressMonitor;
 import org.openstreetmap.josm.io.BoundingBoxDownloader;
 import org.openstreetmap.josm.io.OsmApiException;
 import org.openstreetmap.josm.io.OsmServerLocationReader;
 import org.openstreetmap.josm.io.OsmServerReader;
 import org.openstreetmap.josm.io.OsmTransferException;
 import org.openstreetmap.josm.plugins.ods.DownloadTask;
 import org.openstreetmap.josm.plugins.ods.OdsWorkingSet;
 import org.openstreetmap.josm.plugins.ods.jts.Boundary;
 import org.openstreetmap.josm.plugins.ods.jts.PolygonFilter;
 import org.openstreetmap.josm.tools.I18n;
 
 public class InternalDownloadTask implements DownloadTask {
     final OdsWorkingSet workingSet;
     final Boundary boundary;
     OsmServerReader osmServerReader;
     private static String overpassQuery = 
         "(node($bbox);rel(bn)->.x;way($bbox);" +
         "node(w)->.x;rel(bw);)";
 //    List<Exception> exceptions = new LinkedList<Exception>();
     boolean failed = false;
     boolean cancelled = false;
     Exception exception = null;
     String errorMessage = null;
     private DownloadSource downloadSource=  DownloadSource.OSM;
     private DataSet dataSet;
 
     protected InternalDownloadTask(OdsWorkingSet workingSet, Boundary boundary) {
         super();
         this.workingSet = workingSet;
         this.boundary = boundary;
     }
 
     public boolean failed() {
         return failed;
     }
     
     @Override
     public boolean cancelled() {
         return cancelled;
     }
 
     public String getErrorMessage() {
         if (errorMessage != null) {
             return errorMessage;
         }
         if (exception != null) {
             return exception.getMessage();
         }
         return null;
     }
     
     public Exception getException() {
         return exception;
     }
     
     public DataSet getDataSet() {
         return dataSet;
     }
     
     @Override
     public Callable<Object> getPrepareCallable() {
         return new Callable<Object>() {
 
             @Override
             public Object call() {
                 switch (downloadSource) {
                 case OSM:
                     osmServerReader = new BoundingBoxDownloader(boundary.getBounds());
                     break;
                 case OVERPASS:
                     String url = Overpass.getURL(overpassQuery, boundary);
                     osmServerReader = new OsmServerLocationReader(url);
                     break;
                 }
                 return null;
             }
         };
     }
 
     @Override
     public Callable<?> getDownloadCallable() {
         return new Callable<Object>() {
 
             @Override
             public Object call() {
                 try {
                     if (cancelled)
                         return null;
                     dataSet = parseDataSet();
                     if (downloadSource == DownloadSource.OSM) {
                         PolygonFilter filter = new PolygonFilter(boundary.getPolygon());
                         dataSet = filter.filter(dataSet);
                     }
                 }
                 catch(Exception e) {
                     failed = true;
                    if (cancelled()) {
                        Main.info(I18n.tr("Ignoring exception because download has been canceled. Exception was: {0}", e.toString()));
                        return null;
                    }
                     if (e instanceof OsmApiException) {
                         if ( ((OsmApiException) e).getResponseCode() == 400) {
                             errorMessage = I18n.tr("You tried to download too much Openstreetmap data. Please select a smaller download area.");
                         }
                     }
                     exception = e;
                 }
                 return null;
             }
                 
             protected DataSet parseDataSet() throws OsmTransferException {
                 return osmServerReader.parseOsm(NullProgressMonitor.INSTANCE);
             }
 
         };
     }
 
     @Override
     public void operationCanceled() {
         cancelled = true;
         if (osmServerReader != null) {
             osmServerReader.cancel();
         }
     }
     
     static enum DownloadSource {
         OSM,
         OVERPASS;
     }
 }
