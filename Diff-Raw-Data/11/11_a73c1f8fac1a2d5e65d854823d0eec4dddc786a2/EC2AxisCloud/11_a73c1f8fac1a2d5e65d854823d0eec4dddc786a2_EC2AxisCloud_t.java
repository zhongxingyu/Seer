 package hudson.plugins.ec2;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.model.Api;
 import hudson.model.Computer;
 import hudson.model.Executor;
 import hudson.model.Label;
 import hudson.model.Node;
 import hudson.model.labels.LabelAtom;
 import hudson.slaves.Cloud;
 import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.util.TimeUnit2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.locks.ReentrantLock;
 
 import jenkins.model.Jenkins;
 
 import org.apache.commons.lang.StringUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.amazonaws.AmazonClientException;
 import com.amazonaws.services.ec2.AmazonEC2;
 import com.amazonaws.services.ec2.model.KeyPair;
 
 import static hudson.model.Queue.Item;
 
 public class EC2AxisCloud extends AmazonEC2Cloud {
 	private static final String SLAVE_MATRIX_ENV_VAR_NAME = "MATRIX_EXEC_ID";
 	private static final String SLAVE_NUM_SEPARATOR = "__";
 	private final EC2AxisPrivateKey ec2PrivateKey;
 
 	@DataBoundConstructor
 	public EC2AxisCloud(String accessId, String secretKey, String region, String privateKey, String instanceCapStr, List<SlaveTemplate> templates) {
 		super(accessId,secretKey,region, privateKey,instanceCapStr,replaceByEC2AxisSlaveTemplates(templates));
 		ec2PrivateKey = new EC2AxisPrivateKey(privateKey);
 	}
 	
 	public Api getApi() {
         return new Api(this);
     }
 		
 	public boolean acceptsLabel(Label label) {
 		return getTemplateGivenLabel(label) != null;
 	}
 	
 	@Override
 	public Ec2AxisSlaveTemplate getTemplate(Label label) {
 		String displayName = label.getDisplayName();
 		if (displayName == null)
 			return null;
 		
     	String labelPrefix = StringUtils.substringBefore(displayName,SLAVE_NUM_SEPARATOR);
 		LabelAtom prefixAtom = new LabelAtom(labelPrefix);
     	Ec2AxisSlaveTemplate template = (Ec2AxisSlaveTemplate)super.getTemplate(prefixAtom);
     	if (template == null)
     		return null;
     	template.setInstanceLabel(displayName);
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
 
 	final static ReentrantLock  labelAllocationLock = new ReentrantLock();
 	
 	public List<String> allocateSlavesLabels(
 			final EC2Logger logger, 
 			String ec2Label, 
 			Integer numberOfSlaves, 
 			Integer instanceBootTimeoutLimit) 
 	{
 		try {
 			labelAllocationLock.lockInterruptibly();
 			
 			LinkedList<EC2AbstractSlave> onlineAndAvailableSlaves = findOnlineEligibleSlavesToAllocate(logger, ec2Label, numberOfSlaves);
 			int countOfRemainingLabelsToCreate = numberOfSlaves - onlineAndAvailableSlaves.size();
 			LinkedList<EC2AbstractSlave> allSlaves = new LinkedList<EC2AbstractSlave>();
 			allSlaves.addAll(onlineAndAvailableSlaves);
 
 			if (countOfRemainingLabelsToCreate > 0) {
 				int nextMatrixId = onlineAndAvailableSlaves.size()+1;
 				
 				List<EC2AbstractSlave> newSlaves = createMissingSlaves(
 						logger,
 						ec2Label, 
 						countOfRemainingLabelsToCreate, 
 						nextMatrixId);
 				allSlaves.addAll(newSlaves);
 			}
 			
 			List<String> slaveLabels = new ArrayList<String>();
 			for (EC2AbstractSlave slave : allSlaves) {
 				slaveLabels.add(slave.getNodeName());
 			}
 			
 			return slaveLabels;
 		} catch (InterruptedException e1) {
 			return Arrays.asList();
 		} finally {
 			labelAllocationLock.unlock();
 		}
 	}
 
 	private List<EC2AbstractSlave> createMissingSlaves(
 			EC2Logger logger, 
 			String ec2Label, 
 			int remainingLabelsToCreate,
 			int nextMatrixId) 
 	{
 		try {
 			return allocateSlavesAndLaunchThem(ec2Label, logger, nextMatrixId, remainingLabelsToCreate);
 		} catch (Exception e) {
 			logger.printStackTrace(e);
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private List<EC2AbstractSlave> allocateSlavesAndLaunchThem(
 			String ec2Label,
 			final EC2Logger logger, 
 			int nextMatrixId, 
 			int remainingLabelsToCreate) throws IOException 
 	{
 		logger.println("Will provision instances for label: " + ec2Label);
 		Ec2AxisSlaveTemplate slaveTemplate = getTemplate(new LabelAtom(ec2Label));
 		
 		List<EC2AbstractSlave> allocatedSlaves = slaveTemplate.provisionMultipleSlaves(logger, remainingLabelsToCreate);
 		int matrixIdSeq = nextMatrixId;
 		 
 		for (EC2AbstractSlave ec2Slave : allocatedSlaves) {
 			ec2Slave.setLabelString(getAxisLabel(ec2Label));
 			EnvVars slaveEnvVars = getSlaveEnvVars(ec2Slave);
 			slaveEnvVars.put(SLAVE_MATRIX_ENV_VAR_NAME, ""+matrixIdSeq++);
 		}
 		return allocatedSlaves;
 	}
 
 	private String getAxisLabel(String ec2Label) {
 		return ec2Label+"$axis";
 	}
 
 	private EnvVars getSlaveEnvVars(EC2AbstractSlave provisionedSlave) {
 		EnvironmentVariablesNodeProperty v = provisionedSlave.getNodeProperties().get(EnvironmentVariablesNodeProperty.class);
 		if (v == null) {
 			v = new EnvironmentVariablesNodeProperty();
 			provisionedSlave.getNodeProperties().add(v);
 		}
 		return v.getEnvVars();
 	}
 	
 	private LinkedList<EC2AbstractSlave> findOnlineEligibleSlavesToAllocate(
 			EC2Logger logger,
 			String ec2Label, 
 			Integer numberOfSlaves) 
 	{
 		logger.println("Starting selection of labels with idle executors for job");
 		final LinkedList<EC2AbstractSlave> onlineAndAvailableLabels = new LinkedList<EC2AbstractSlave>();
 		int matrixId = 1;
 		logger.println("Will check " + ec2Label);
 		
 		Label label = Jenkins.getInstance().getLabel(getAxisLabel(ec2Label));
 		
 		for (Node node : label.getNodes()) {
 			if(!isNodeAvailable(logger, node)) 
 				continue;
 			
 			EC2AbstractSlave ec2AbstractSlave = (EC2AbstractSlave) node;
 			EnvVars slaveEnvVars = getSlaveEnvVars(ec2AbstractSlave);
 			slaveEnvVars.put(SLAVE_MATRIX_ENV_VAR_NAME, matrixId+"");
 			onlineAndAvailableLabels.add(ec2AbstractSlave);
 			matrixId++;
 			
 			if (onlineAndAvailableLabels.size() >= numberOfSlaves)
 				break;
 		}
 		
 		logger.println("Online labels found : " + onlineAndAvailableLabels.size());
 		return onlineAndAvailableLabels;
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
 	
 	private boolean isNodeAvailable(EC2Logger logger, Node node) {
 		boolean isAvailable = false;
 		
 		String nodeName = node.getDisplayName();
 		logger.println("Checking node : " + nodeName);
 		Computer c = node.toComputer();
 		if (c.isOffline() || c.isConnecting()) {
 			isAvailable = false;
 		}
 		if (isNodeOnlineAndAvailable(c) && hasAvailableExecutor(c))
 			isAvailable = true;
 		return isAvailable;
 	}
 
 	private boolean hasAvailableExecutor(Computer c) {
 		final List<Executor> executors = c.getExecutors();
 		for (Executor executor : executors) {
 			if (executor.isIdle()) 
 				return true;
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
 
 	public KeyPair getKeyPair(AmazonEC2 ec2) throws AmazonClientException, IOException {
 		return ec2PrivateKey.find(ec2);
 	}
 
 	public String getSpotPriceIfApplicable(String ec2Label) {
 		Ec2AxisSlaveTemplate slaveTemplate = getTemplate(new LabelAtom(ec2Label));
 		if (slaveTemplate.getSpotMaxBidPrice() == null)
 			return null;
 		return slaveTemplate.getCurrentSpotPrice();
 	}
 
 	public String getInstanceType(String ec2Label) {
 		Ec2AxisSlaveTemplate slaveTemplate = getTemplate(new LabelAtom(ec2Label));
 		return slaveTemplate.type.name();
 	}
 
 	public static long getTimeout(EC2AbstractSlave slave) {
 		if (slave.getLaunchTimeoutInMillis() == 0)
			return TimeUnit2.MINUTES.toMillis(20);
 		return slave.getLaunchTimeoutInMillis();
 	}
 
 	public static void finishSlaveAndQueuedItems(EC2AbstractSlave slave) {
 		Item[] items = Jenkins.getInstance().getQueue().getItems();
 		for (Item item : items) {
 			if (item.task.getAssignedLabel().getDisplayName().equals(slave.getDisplayName())) {
 				Jenkins.getInstance().getQueue().cancel(item);
 			}
 		}
 		if (!slave.stopOnTerminate)
 			slave.terminate();
 	}
 }
