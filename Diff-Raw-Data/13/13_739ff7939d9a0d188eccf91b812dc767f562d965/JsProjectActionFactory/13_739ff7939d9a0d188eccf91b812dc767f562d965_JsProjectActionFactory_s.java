 package hudson.plugins.jswidgets;
 
 import hudson.Extension;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.TransientProjectActionFactory;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.logging.Logger;
 
 /**
  * Extends project actions for all jobs.
  * 
  * @author mfriedenhagen
  */
 @Extension
 public class JsProjectActionFactory extends TransientProjectActionFactory {
 
     /** Our logger. */
     private static final Logger LOG = Logger.getLogger(JsProjectActionFactory.class.getName());
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Collection<? extends Action> createFor(@SuppressWarnings("unchecked") AbstractProject target) {
         LOG.fine(this + " adds JsJobAction for " + target);
         final ArrayList<Action> actions = new ArrayList<Action>();
        final JsJobAction jsJobAction = target.getAction(JsJobAction.class);
        if (jsJobAction == null) {
            actions.add(new JsJobAction(target));
        } else {
            LOG.fine(target + " already has " + jsJobAction);
        }
         return actions;
     }
 
 }
