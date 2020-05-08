 package org.sonar.plugins.profiler;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.Phase;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.measures.Metric;
 import org.sonar.api.resources.JavaFile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.Resource;
 import org.sonar.api.utils.SonarException;
 import org.sonar.plugins.profiler.jprofiler.JProfilerExporter;
 import org.sonar.plugins.profiler.utils.FilterFilesBySuffix;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Evgeny Mandrikov
  */
 @Phase(name = Phase.Name.POST)
 public class ProfilerSensor implements Sensor {
   private static final Logger LOG = LoggerFactory.getLogger(ProfilerSensor.class);
 
   public boolean shouldExecuteOnProject(Project project) {
     return true;
   }
 
   /**
    * @param project project
    * @return ${basedir}/target/profiler
    */
   private File getDir(Project project) {
     return new File(project.getFileSystem().getBuildDir(), "/profiler");
   }
 
   public void analyse(Project project, SensorContext context) {
     try {
       File dir = getDir(project);
       LOG.info("Parsing {}", dir);
       exportDir(dir);
 
       File[] files;
      files = dir.listFiles(new FilterFilesBySuffix(JProfilerExporter.HOTSPOTS_VIEW + ".html"));
       for (File file : files) {
         context.saveMeasure(getProfilerResource(file), ProfilerMetrics.TESTS, 1.0); // TODO
         saveMeasure(ProfilerMetrics.CPU_HOTSPOTS_DATA, file, context);
       }
 
      files = dir.listFiles(new FilterFilesBySuffix(JProfilerExporter.ALLOCATION_HOTSPOTS_VIEW + ".html"));
       for (File file : files) {
         saveMeasure(ProfilerMetrics.MEMORY_HOTSPOTS_DATA, file, context);
       }
     } catch (Exception e) {
       throw new SonarException(e);
     }
   }
 
   protected void saveMeasure(Metric metric, File file, SensorContext context) throws IOException {
     Measure measure = new Measure(metric);
     String data = FileUtils.readFileToString(file);
     measure.setData(data);
     Resource<?> resource = getProfilerResource(file);
     LOG.info("Saving {} for {} from {}", new Object[]{metric.getName(), resource.getKey(), file});
     context.saveMeasure(resource, measure);
   }
 
   protected Resource<?> getProfilerResource(File file) {
     // TODO support multiple snapshots for one test file
     String key = StringUtils.substringBeforeLast(file.getName(), "-");
     return new JavaFile(key, true);
   }
 
   @Override
   public String toString() {
     return getClass().getSimpleName();
   }
 
   private static void exportDir(File dir) {
     for (String filename : dir.list(new FilterFilesBySuffix(JProfilerExporter.JPS_EXT))) {
       // TODO get jprofiler.home from maven properties
       JProfilerExporter.create("~/applications/jprofiler5", dir, filename)
           .setExportDir(dir)
           .addHotSpotsView(JProfilerExporter.HTML_FORMAT, "method", "method", false)
           .addAllocationHotSpotsView(JProfilerExporter.HTML_FORMAT, "method", false)
           .export();
     }
   }
 }
