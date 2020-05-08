 package org.openstreetmap.josm.plugins.ods.entities.internal;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import javax.swing.JOptionPane;
 
 import org.openstreetmap.josm.Main;
 import org.openstreetmap.josm.data.osm.DataSet;
 import org.openstreetmap.josm.plugins.ods.DataLayer;
 import org.openstreetmap.josm.plugins.ods.DownloadJob;
 import org.openstreetmap.josm.plugins.ods.DownloadTask;
 import org.openstreetmap.josm.plugins.ods.ODS;
 import org.openstreetmap.josm.plugins.ods.OdsWorkingSet;
 import org.openstreetmap.josm.plugins.ods.analysis.Analyzer;
 import org.openstreetmap.josm.plugins.ods.entities.BuildException;
 import org.openstreetmap.josm.plugins.ods.entities.EntityFactory;
 import org.openstreetmap.josm.plugins.ods.entities.EntitySet;
 import org.openstreetmap.josm.plugins.ods.entities.builtenvironment.AddressToBuildingMatcher;
 import org.openstreetmap.josm.plugins.ods.issue.Issue;
 import org.openstreetmap.josm.plugins.ods.jts.Boundary;
 import org.openstreetmap.josm.tools.I18n;
 
 public class InternalDownloadJob implements DownloadJob {
     private final OdsWorkingSet workingSet;
     private final Boundary boundary;
     private List<InternalDownloadTask> downloadTasks;
     private DataLayer dataLayer;
     private EntityFactory entityFactory;
     private List<Analyzer> analyzers;
     private EntitySet newEntities;
 
     public InternalDownloadJob(Boundary boundary) {
         super();
         this.workingSet = ODS.getModule().getWorkingSet();
         this.boundary = boundary;
         this.dataLayer = workingSet.getInternalDataLayer();
         this.entityFactory = workingSet.getEntityFactory();
         Double tolerance = 2e-7;
         analyzers = new ArrayList<>(5);
         analyzers.add(new AddressToBuildingMatcher());
 //        analyzers.add(new AddressToStreetMatcher());
     }
 
     public void setup() {
         InternalDownloadTask downloadTask = new InternalDownloadTask(workingSet, boundary);
         downloadTasks = Collections.singletonList(downloadTask);
     }
 
     @Override
     public List<Callable<?>> getPrepareCallables() {
         List<Callable<?>> callables = new ArrayList<>(downloadTasks.size());
         for (InternalDownloadTask task : downloadTasks) {
             callables.add(task.getPrepareCallable());
         }
         return callables;
     }
 
     @Override
     public List<Callable<?>> getDownloadCallables() {
         List<Callable<?>> callables = new ArrayList<>(downloadTasks.size());
         for (InternalDownloadTask task : downloadTasks) {
             callables.add(task.getDownloadCallable());
         }
         return callables;
     }
 
     @Override
     public List<? extends DownloadTask> getDownloadTasks() {
         return downloadTasks;
     }
 
     @Override
     public void build() throws BuildException {
        DataSet dataSet = downloadTasks.get(0).getDataSet();
         workingSet.internalDataLayer.getOsmDataLayer().mergeFrom(dataSet);
 
         BuiltEnvironmentEntityBuilder builder = new BuiltEnvironmentEntityBuilder(
                 workingSet.internalDataLayer);
         builder.setEntityFactory(entityFactory);
         try {
             builder.build();
             newEntities = builder.getNewEntities();
             analyze();
         } catch (BuildException e) {
             Collection<Issue> issues = e.getIssues();
             StringBuilder sb = new StringBuilder(1000);
             sb.append(I18n.trn("An object could not be built:",
                     "{0} objects could not be built:", issues.size(),
                     issues.size()));
             for (Issue issue : e.getIssues()) {
                 sb.append("\n").append(issue.getMessage());
             }
             JOptionPane.showMessageDialog(Main.parent, sb.toString());
         }
     }
     
     private void analyze() {
         for (Analyzer analyzer : analyzers) {
             analyzer.analyze(dataLayer, newEntities);
         }        
     }
 }
