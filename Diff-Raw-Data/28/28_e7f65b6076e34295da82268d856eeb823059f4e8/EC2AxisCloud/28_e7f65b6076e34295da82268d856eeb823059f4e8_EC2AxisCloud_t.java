 package hudson.plugins.ec2;
 
 import hudson.Extension;
 import hudson.matrix.MatrixBuild.MatrixBuildExecution;
 import hudson.matrix.MatrixProject;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.Cause;
 import hudson.model.Computer;
 import hudson.model.Executor;
 import hudson.model.Job;
 import hudson.model.Label;
 import hudson.model.Node;
 import hudson.model.Queue;
 import hudson.model.Queue.Item;
 import hudson.model.Queue.Task;
 import hudson.model.labels.LabelAtom;
 import hudson.slaves.Cloud;
 import hudson.slaves.NodeProvisioner.PlannedNode;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import jenkins.model.Jenkins;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 public class EC2AxisCloud extends AmazonEC2Cloud {
 
 	private static final String SLAVE_NUM_SEPARATOR = "__";
 	private static final Map<String, JobAllocationManager> jobsByRequestedLabels = new HashMap<String, JobAllocationManager>();
 
 	@DataBoundConstructor
 	public EC2AxisCloud(String accessId, String secretKey, String region, String privateKey, String instanceCapStr, List<SlaveTemplate> templates) {
 		super(accessId,secretKey,region, privateKey,instanceCapStr,replaceByEC2AxisSlaveTemplates(templates));
 	}
 	
 	public boolean acceptsLabel(Label label) {
 		return getTemplateGivenLabel(label) != null;
 	}
 	
 	@Override
 	public SlaveTemplate getTemplate(Label label) {
 		String displayName = label.getDisplayName();
 		if (displayName == null)
 			return null;
 		
     	String labelPrefix = StringUtils.substringBefore(displayName,SLAVE_NUM_SEPARATOR);
 		LabelAtom prefixAtom = new LabelAtom(labelPrefix);
     	Ec2AxisSlaveTemplate template = (Ec2AxisSlaveTemplate)super.getTemplate(prefixAtom);
     	if (template == null)
     		return null;
     	template.setInstanceLabel(displayName);
     	
     	JobAllocationManager jobAllocationManager = jobsByRequestedLabels.get(displayName);
    	if (jobAllocationManager != null)
    		template.setMatrixId(jobAllocationManager.getMatrixId());
     	
 		return template;
 	}
 
 	public static EC2AxisCloud getCloudToUse(String ec2label) {
 		Iterator<Cloud> iterator = Jenkins.getInstance().clouds.iterator();
 		EC2AxisCloud cloudToUse = null;
 		while(iterator.hasNext()) {
 			Cloud next = iterator.next();
 			if (next instanceof EC2AxisCloud) {
 				if (((EC2AxisCloud)next).acceptsLabel(new LabelAtom(ec2label)))
 					cloudToUse = (EC2AxisCloud) next;
 			}
 		}
 		return cloudToUse;
 	}
 		
 	@Override
 	public Collection<PlannedNode> provision(Label label, int excessWorkload) {
 		JobAllocationManager jobStatus = jobsByRequestedLabels.get(label.getDisplayName());
 		if (jobStatus == null) {
 			if (label.getDisplayName().matches(SLAVE_NUM_SEPARATOR+"[0-9]+$")) {
 				cancelAllItemsInQueueMatchingLabel(label);
 			}
 			return super.provision(label, excessWorkload);
 		}
 		
 		if (jobStatus.isAllocated()) {
 			jobStatus.abortBuildIfBootTimedOut();
 			return Arrays.asList();
 		}
 		jobStatus.setAllocated();
 		return super.provision(label, excessWorkload);
 	}
 
 	public synchronized List<String> allocateSlavesLabels(
 			MatrixBuildExecution context, 
 			String ec2Label, 
 			Integer numberOfSlaves, 
 			Integer instanceBootTimeoutLimit)
 	{
 		removePreviousLabelsAllocatedToGivenProject(context.getProject());
 		addListenerToCleanupAllocationTableOnBuildCompletion(context);
 		
 		LinkedList<String> allocatedLabels = allocateLabels(ec2Label, numberOfSlaves);
 		
		int matrixId = 1;
 		for (String allocatedLabel : allocatedLabels) {
 			JobAllocationManager value = new JobAllocationManager(
 					(MatrixProject)context.getProject(), 
 					new LabelAtom(allocatedLabel), 
 					instanceBootTimeoutLimit,
 					matrixId++);
 			jobsByRequestedLabels.put(allocatedLabel, value);
 		}
 		
 		return allocatedLabels;
 	}
 	
 	private BuildListener getListenerFor(final Job<?,?> project) {
 		return new BuildListenerImplementation(project);
 	}	
 	
 	private void cancelAllItemsInQueueMatchingLabel(Label label) 
 	{
 		Queue queue = Jenkins.getInstance().getQueue();
 		Item[] items = queue.getItems();
 		for (Item item : items) {
 			Task task = item.task;
 			if (task.getAssignedLabel().getName().equals(label.getDisplayName())) {
 				queue.cancel(item);
 			}
 		}
 	}
 	
 	private LinkedList<String> allocateLabels(String ec2Label, Integer numberOfSlaves) 
 	{
 		Set<Label> labels = Jenkins.getInstance().getLabels();
 		LinkedList<String> allocatedLabels = new LinkedList<String>();
 		int lastAllocatedSlaveNumber = 0;
 		
 		LinkedList<String> idleLabels = new LinkedList<String>();
 		for (Label label : labels) {
 			String labelString = label.getDisplayName();
 			if (!labelString.startsWith(ec2Label))
 				continue;
 			
 			final String[] prefixAndSlaveNumber = labelString.split(SLAVE_NUM_SEPARATOR);
 			boolean hasNoSuffix = prefixAndSlaveNumber.length == 1;
 			if (hasNoSuffix)
 				continue;
 			
 			String suffix = prefixAndSlaveNumber[1];
 			int slaveNumber = Integer.parseInt(suffix);
 			if (slaveNumber > lastAllocatedSlaveNumber)
 				lastAllocatedSlaveNumber = slaveNumber;
 			
 			if (hasAvailableNode(label))
 				idleLabels.add(labelString);
 			
 			if (idleLabels.size() >= numberOfSlaves)
 				break;
 		}
 		lastAllocatedSlaveNumber++;
 		
 		allocatedLabels.addAll(idleLabels);
 		Integer slavesToComplete = numberOfSlaves - allocatedLabels.size();
 		for (int i = 0; i < slavesToComplete; i++) {
 			int slaveNumber = lastAllocatedSlaveNumber+i;
 			allocatedLabels.add(ec2Label + SLAVE_NUM_SEPARATOR + slaveNumber);
 		}
 		return allocatedLabels;
 	}
 
 	private void addListenerToCleanupAllocationTableOnBuildCompletion( MatrixBuildExecution context) {
 		try {
 			context.post(getListenerFor(context.getProject()));
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static List<SlaveTemplate> replaceByEC2AxisSlaveTemplates(List<SlaveTemplate> templates) {
 		List<SlaveTemplate> ec2axisTemplates = new LinkedList<SlaveTemplate>();
 		for (SlaveTemplate slaveTemplate : templates) {
 			ec2axisTemplates.add(new Ec2AxisSlaveTemplate(slaveTemplate));
 		}
 		return ec2axisTemplates;
 	}
 
 	private Ec2AxisSlaveTemplate getTemplateGivenLabel(Label label) {
 		String displayName = label.getDisplayName();
 		if (displayName == null)
 			return null;
 		
     	String labelPrefix = StringUtils.substringBefore(displayName,SLAVE_NUM_SEPARATOR);
 		LabelAtom prefixAtom = new LabelAtom(labelPrefix);
     	Ec2AxisSlaveTemplate template = (Ec2AxisSlaveTemplate)super.getTemplate(prefixAtom);
 		return template;
 	}
 	
 
 	private void removePreviousLabelsAllocatedToGivenProject(Job<?, ?> requestingProject) {
 		Iterator<Entry<String, JobAllocationManager>> iterator = jobsByRequestedLabels.entrySet().iterator();
 		while(iterator.hasNext()) {
 			Entry<String, JobAllocationManager> entry = iterator.next();
 			if (entry.getValue().job.equals(requestingProject))
 				iterator.remove();
 		}
 	}
 
 	private boolean hasAvailableNode(Label label) {
 		if (jobsByRequestedLabels.containsKey(label.getName()))
 			return false;
 		
 		Set<Node> nodes = label.getNodes();
 		return  isLabelAvailable(nodes);
 	}
 
 	private boolean isLabelAvailable(Set<Node> nodes) {
 		if (nodes.size() == 0)
 			return true;
 		
 		for (Node node : nodes) {
 			Computer c = node.toComputer();
 			if (c.isOffline() && !c.isConnecting()) {
 				return true;
 			}
 			if (isNodeOnlineAndAvailable(c))
 				return true;
 			
 			if (hasAvailableExecutor(c))
 				return true;
 		}
 		return false;
 	}
 
 	private boolean hasAvailableExecutor(Computer c) {
 		final List<Executor> executors = c.getExecutors();
 		for (Executor executor : executors) {
 			if (executor.isIdle()) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private boolean isNodeOnlineAndAvailable(Computer c) {
 		return (c.isOnline() || c.isConnecting()) && c.isAcceptingTasks();
 	}
 
 	@Extension
 	public static class DescriptorImpl extends AmazonEC2Cloud.DescriptorImpl {
 	    @Override
 		public String getDisplayName() {
 	        return "EC2 Axis Amazon Cloud";
 	    }
 	}
 
 	@SuppressWarnings("serial")
 	private final class BuildListenerImplementation extends BuildStartEndListener {
 		private final Job<?, ?> project;
 		private BuildListenerImplementation(Job<?, ?> project) {
 			this.project = project;
 		}
 	
 		public void finished(Result result) {
 			removePreviousLabelsAllocatedToGivenProject(project);
 		}
 	
 		public void started(List<Cause> causes) {  }
 	}
 
 }
