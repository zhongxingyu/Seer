 package hudson.plugins.jswidgets;
 
 import hudson.Extension;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.TransientProjectActionFactory;
 
import java.util.List;
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
        final List<JsJobAction> jsJobActions = target.getActions(JsJobAction.class);
        LOG.fine(target + " already has " + jsJobActions);
        final JsJobAction newAction = new JsJobAction(target);
         final ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(newAction);
         return actions;
     }
 
 }
