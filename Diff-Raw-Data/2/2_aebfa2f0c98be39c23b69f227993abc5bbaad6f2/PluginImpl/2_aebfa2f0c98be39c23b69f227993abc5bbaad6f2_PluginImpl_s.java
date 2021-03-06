 package hudson.plugins.clover;
 
 import hudson.Plugin;
 import hudson.tasks.BuildStep;
 import hudson.plugins.clover.CloverPublisher;
 
 /**
  * Entry point of a plugin.
  *
  * <p>
  * There must be one {@link Plugin} class in each plugin.
  * See javadoc of {@link Plugin} for more about what can be done on this class.
  *
  * @author Kohsuke Kawaguchi
  * @plugin
  */
 public class PluginImpl extends Plugin {
     public void start() throws Exception {
         // plugins normally extend Hudson by providing custom implementations
         // of 'extension points'. In this example, we'll add one builder.
        BuildStep.PUBLISHERS.add(CloverPublisher.DESCRIPTOR);
     }
 }
