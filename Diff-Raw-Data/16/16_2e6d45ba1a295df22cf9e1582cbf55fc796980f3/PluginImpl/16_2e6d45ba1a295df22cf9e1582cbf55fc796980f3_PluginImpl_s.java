 package hudson.plugins.javancss;
 
 import hudson.Plugin;
 import hudson.maven.MavenReporters;
 import hudson.tasks.BuildStep;
 
 /**
  * Entry point of JavaNCSS plugin.
  *
  * @author Stephen Connolly
 * @plugin
  */
 public class PluginImpl extends Plugin {
     /**
      * {@inheritDoc}
      */
     public void start() throws Exception {
         BuildStep.PUBLISHERS.add(JavaNCSSPublisher.DESCRIPTOR);
         MavenReporters.LIST.add(JavaNCSSMavenPublisher.DESCRIPTOR);
     }
 
     public static String DISPLAY_NAME = "Java NCSS Report";
     public static String GRAPH_NAME = "Java NCSS Trend";
     public static String URL = "javancss";
     public static String ICON_FILE_NAME = "graph.gif";
 }
