 package hudson.plugins.excludeMatrixParent;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import hudson.Extension;
 import hudson.model.Node;
 import hudson.model.Queue.FlyweightTask;
 import hudson.model.Queue.Task;
 import hudson.model.queue.CauseOfBlockage;
 import hudson.slaves.NodeProperty;
 import hudson.slaves.NodePropertyDescriptor;
 
 public class NodeExcludeMatrixParent extends NodeProperty<Node>{
 	
 	@DataBoundConstructor
 	public NodeExcludeMatrixParent(){
 	}
 	
 	@Override
 	public CauseOfBlockage canTake(Task task) {
 		if(task instanceof FlyweightTask){
 			return CauseOfBlockage.fromMessage(Messages._Excludes_Matrix_Parents());
 		}
         return null;
     }
 	
 	@Extension // this marker indicates Hudson that this is an implementation of an extension point.
     public static final class NodePropertyDescriptorImpl extends NodePropertyDescriptor {
 
         public static final NodePropertyDescriptorImpl DESCRIPTOR = new NodePropertyDescriptorImpl();
         
         public NodePropertyDescriptorImpl(){
         	super(NodeExcludeMatrixParent.class);
         }
 		
 		/**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
            return "Exclude matrix parens to run on this slave";
         }
 
 	}
 }
