 package org.sonar.plugins.profiler;
 
 import org.sonar.api.Extension;
 import org.sonar.api.Plugin;
 import org.sonar.api.Properties;
 import org.sonar.api.Property;
 import org.sonar.plugins.profiler.viewer.CpuHotspotsViewerDefinition;
 import org.sonar.plugins.profiler.viewer.MemoryHotspotsViewerDefinition;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Evgeny Mandrikov
  */
 @Properties(
     @Property(
         key = ProfilerPlugin.LICENSE_PROPERTY,
         name = "License"
     )
 )
 public class ProfilerPlugin implements Plugin {
   public static final String LICENSE_PROPERTY = "sonar.profiler.license.secured";
  public static final String JPROFILER_HOME_PROPERTY = "jprofiler.home";
 
   public String getKey() {
     return "profiler";
   }
 
   public String getName() {
     return "Profiler";
   }
 
   public String getDescription() {
     return "<a href='http://www.ej-technologies.com/products/jprofiler/overview.html'>JProfiler</a> is a Java profiler.";
   }
 
   public List<Class<? extends Extension>> getExtensions() {
     return Arrays.asList(
         ProfilerMetrics.class,
         ProfilerConfigGenerator.class,
         ProfilerSensor.class,
         ProfilerDecorator.class,
 
         ProfilerWidget.class,
 
         CpuHotspotsViewerDefinition.class,
         MemoryHotspotsViewerDefinition.class
     );
   }
 }
