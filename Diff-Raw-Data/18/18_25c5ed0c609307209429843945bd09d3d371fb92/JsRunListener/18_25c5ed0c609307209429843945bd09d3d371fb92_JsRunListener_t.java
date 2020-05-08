 package hudson.plugins.jswidgets;
 
 import hudson.Extension;
 import hudson.model.AbstractBuild;
 import hudson.model.listeners.RunListener;
 
 import java.util.logging.Logger;
 
 /**
  * This listener adds a {@link JsBuildAction} to every new build.
 *
  * @author mfriedenhagen
  */
 @SuppressWarnings("rawtypes")
 @Extension
 public final class JsRunListener extends RunListener<AbstractBuild> {
 
     /** The Logger. */
     private static final Logger LOG = Logger.getLogger(JsRunListener.class.getName());
 
     /**
      * {@link Extension} needs parameterless constructor.
      */
     public JsRunListener() {
         super(AbstractBuild.class);
     }
 
     /**
      * {@inheritDoc}
     *
      * Adds {@link JsBuildAction} to the build. Do this in <tt>onFinalized</tt>, so the XML-data of the build is not
      * affected.
      */
     @Override
     public void onFinalized(AbstractBuild r) {
        if (r.getAction(JsBuildAction.class) == null) {
            final JsBuildAction jsBuildAction = new JsBuildAction(r);
            r.addAction(jsBuildAction);
            LOG.fine(r.toString() + ":" + r.getActions().toString());
            LOG.fine("Registering " + jsBuildAction.getDisplayName() + " for " + r);
        } else {
            LOG.fine(r.toString() + " already has " + JsBuildAction.class.getName());
        }
         super.onFinalized(r);
     }
 }
