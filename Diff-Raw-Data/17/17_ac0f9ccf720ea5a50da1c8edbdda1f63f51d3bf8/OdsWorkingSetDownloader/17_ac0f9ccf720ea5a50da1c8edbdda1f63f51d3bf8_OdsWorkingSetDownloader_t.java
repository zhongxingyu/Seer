 package org.openstreetmap.josm.plugins.openservices;
 
 import static org.openstreetmap.josm.tools.I18n.tr;
 
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.concurrent.Future;
 
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 
 import org.opengis.feature.simple.SimpleFeature;
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.actions.downloadtasks.DownloadOsmTask;
 import org.openstreetmap.josm.actions.downloadtasks.DownloadTask;
 import org.openstreetmap.josm.data.Bounds;
import org.openstreetmap.josm.data.osm.visitor.BoundingXYVisitor;
 import org.openstreetmap.josm.gui.ExceptionDialogUtil;
 
 /**
  * A OdsWorkingSetDownloader performs all tasks needed to download data required
  * for an OdsWorkingSet. This may include data for the Osm Layer.
  * 
  * @author Gertjan Idema
  * 
  */
 public class OdsWorkingSetDownloader {
   private final OdsWorkingSet workingSet;
   private final Bounds boundingBox;
   private final List<DownloadTask> tasks = new LinkedList<DownloadTask>();
   List<Future<?>> futures = new LinkedList<Future<?>>();
   final Collection<SimpleFeature> featureCollection = new LinkedList<SimpleFeature>();
 
   public OdsWorkingSetDownloader(OdsWorkingSet workingSet, Bounds boundingBox,
       boolean getOsmData) {
     super();
     this.workingSet = workingSet;
     this.boundingBox = boundingBox;
     if (getOsmData) {
       prepareOsmTask();
     }
     prepareOdsTasks();
   }
 
   private void prepareOsmTask() {
     Future<?> future = null;
     DownloadOsmTask task;
     // TODO fix this.
     workingSet.activateOsmLayer();
     String query = workingSet.getOsmQuery();
     if (query != null) {
       String url = getOverpassUrl(query, boundingBox);
       task = new DownloadOsmTask();
       future = task.loadUrl(false, url, null);
     }
     else {
       task = new DownloadOsmTask();
       future = task.download(false, boundingBox, null);
     }
     tasks.add(task);
     futures.add(future);
     return;
   }
 
   private void prepareOdsTasks() {
     for (OdsDataSource dataSource : workingSet.getDataSources().values()) {
       OdsDownloadTask downloadTask = dataSource.getDownloadTask(featureCollection);
       tasks.add(downloadTask);
       Future<?> future = downloadTask.download(false, boundingBox, null);
       futures.add(future);
     }
     return;
   }
 
   public void download() {
     Main.worker.submit(new PostDownloadHandler());
   }
   
   private static String getOverpassUrl(String query, Bounds bounds) {
     String host = "http://overpass-api.de/api";
     String bbox = String.format(Locale.ENGLISH, "%f,%f,%f,%f", bounds.getMin()
         .getY(), bounds.getMin().getX(),
         bounds.getMax().getY(), bounds.getMax().getX());
     String q = query.replaceAll("\\$bbox", bbox);
     q = q.replaceAll("\\{\\{bbox\\}\\}", bbox);
     q = q.replace(";$", "");
     return String.format("%s/interpreter?data=%s;out meta;", host, q);
   }
  
  protected void computeBboxAndCenterScale() {
    BoundingXYVisitor v = new BoundingXYVisitor();
    if (boundingBox != null) {
      v.visit(boundingBox);
      Main.map.mapView.recalculateCenterScale(v);
    }
  }

 
   private class PostDownloadHandler implements Runnable {
 
     @Override
     public void run() {
       // wait for all downloads task to finish (by waiting for the futures
       // to return a value)
       //
       for (Future<?> future : futures) {
         try {
           future.get();
         } catch (Exception e) {
           e.printStackTrace();
           return;
         }
       }
       workingSet.addFeatures(featureCollection, boundingBox);
       
       // make sure errors are reported only once
       //
       LinkedHashSet<Object> errors = new LinkedHashSet<Object>();
       for (DownloadTask task : tasks) {
         errors.addAll(task.getErrorObjects());
       }
      if (errors.isEmpty()) {
        computeBboxAndCenterScale();
         return;
      }
 
       // just one error object?
       //
       if (errors.size() == 1) {
         final Object error = errors.iterator().next();
         SwingUtilities.invokeLater(new Runnable() {
           @Override
           public void run() {
             if (error instanceof Exception) {
               ExceptionDialogUtil.explainException((Exception) error);
             } else {
               JOptionPane.showMessageDialog(
                   Main.parent,
                   error.toString(),
                   tr("Error during download"),
                   JOptionPane.ERROR_MESSAGE);
             }
           }
         });
         return;
       }
     };
   }
 }
