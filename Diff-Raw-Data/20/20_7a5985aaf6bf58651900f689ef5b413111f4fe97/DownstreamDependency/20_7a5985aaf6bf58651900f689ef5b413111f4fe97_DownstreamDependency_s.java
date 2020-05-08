 package hudson.plugins.downstream_ext;
 
 import java.io.PrintStream;
 import java.util.List;
 
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.TaskListener;
 import hudson.model.DependencyGraph.Dependency;
 
 /**
  * Defines a dependency introduced by the downstream-ext plugin.
  * 
  * @author kutzi
  */
 public class DownstreamDependency extends Dependency {
 
 	private final DownstreamTrigger trigger;
 
 	public DownstreamDependency(AbstractProject<?, ?> upstream, AbstractProject<?, ?> downstream,
 			DownstreamTrigger trigger) {
 		super(upstream, downstream);
 		this.trigger = trigger;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	@SuppressWarnings("unchecked")
 	public boolean shouldTriggerBuild(AbstractBuild build,
 			TaskListener listener, List<Action> actions) {
 		PrintStream logger = listener.getLogger();
 		if(trigger.getStrategy().evaluate(trigger.getThreshold(), build.getResult())) {
             AbstractProject p = getDownstreamProject();
                 
            if(trigger.isOnlyIfSCMChanges() && !p.pollSCMChanges(listener)) {
            	logger.println(Messages.DownstreamTrigger_NoSCMChanges(p.getName()));
            	return false;
             }
             return true;
 		} else {
 			logger.println(Messages.DownstreamTrigger_ConditionNotMet(trigger.getStrategy().getDisplayName(),
 					trigger.getThreshold()));
 			return false;
 		}
 	}
 
 	// Technically it'd be safe to not override equals
 	// since superclass implements it well.
 	// But maybe that changes in the future.
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof DownstreamDependency)) {
 			return false;
 		}
 		
 		// Currently, there can be only one downstream-ext dependency per project
 		// If that'd change later we must check the trigger instance here, too.
 		
 		return super.equals(obj);
 	}
 }
