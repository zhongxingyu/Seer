 /**
  * Created on Dec 20, 2006 8:42:13 AM
  * 
  * Copyright FullSIX
  */
 package hudson.plugins.jmx;
 
 import hudson.model.Hudson;
 import hudson.model.Job;
 import hudson.model.listeners.JobListener;
 
 import java.util.List;
 
 import javax.management.Attribute;
 import javax.management.AttributeList;
 import javax.management.AttributeNotFoundException;
 import javax.management.DynamicMBean;
 import javax.management.InstanceAlreadyExistsException;
 import javax.management.InstanceNotFoundException;
 import javax.management.InvalidAttributeValueException;
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanException;
 import javax.management.MBeanInfo;
 import javax.management.MBeanRegistrationException;
 import javax.management.MBeanServer;
 import javax.management.MalformedObjectNameException;
 import javax.management.NotCompliantMBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 
 /**
  * @author bruyeron
 * @version $Id: JmxJobListener.java 1834 2007-01-20 07:10:24Z kohsuke $
  */
 public class JmxJobListener extends JobListener {
 	
 	public final static String JMX_NAME_PREFIX = "hudson:type=Job,name=";
 	protected MBeanServer server;
 
	/**
 	 * @param server
 	 */
 	public JmxJobListener(MBeanServer server) {
 		super();
 		this.server = server;
 	}
 
 	/**
 	 * @see hudson.model.listeners.JobListener#onCreated(hudson.model.Job)
 	 */
 	@Override
 	public void onCreated(Job j) {
 		try {
 			ObjectName n = new ObjectName(JMX_NAME_PREFIX + j.getName());
 			JobMBean mbean = new JobMBean(j);
 			server.registerMBean(mbean, n);
 		} catch (MalformedObjectNameException e) {
 			e.printStackTrace();
 		} catch (InstanceAlreadyExistsException e) {
 			e.printStackTrace();
 		} catch (MBeanRegistrationException e) {
 			e.printStackTrace();
 		} catch (NotCompliantMBeanException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see hudson.model.listeners.JobListener#onDeleted(hudson.model.Job)
 	 */
 	@Override
 	public void onDeleted(Job j) {
 		try {
 			ObjectName n = new ObjectName(JMX_NAME_PREFIX + j.getName());
 			server.unregisterMBean(n);
 		} catch (MalformedObjectNameException e) {
 			e.printStackTrace();
 		} catch (InstanceNotFoundException e) {
 			e.printStackTrace();
 		} catch (MBeanRegistrationException e) {
 			e.printStackTrace();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * @see hudson.model.listeners.JobListener#onLoaded()
 	 */
 	@Override
 	public void onLoaded() {
 		List<Job> jobs = Hudson.getInstance().getAllItems(Job.class);
 		for(Job j : jobs){
 			onCreated(j);
 		}
	}
 	
 	public void unregister(){
		List<Job> jobs = Hudson.getInstance().getAllItems(Job.class);
 		for(Job j : jobs){
 			onDeleted(j);
 		}
 	}
 	
 	static class JobMBean implements DynamicMBean {
     	private static final String STATUS = "status";
 		static final String START = "start";
     	static final String STOP = "stop";
     	
     	private Job job = null;
     	private MBeanAttributeInfo[] attributeInfos = new MBeanAttributeInfo[1];
         private MBeanInfo dMBeanInfo = null;
         
 		/**
 		 * @param job
 		 */
 		private JobMBean(Job job) {
 			super();
 			this.job = job;
 			attributeInfos[0] = new MBeanAttributeInfo(STATUS, "java.lang.String", "Status of the Job", true, false, false);
 			dMBeanInfo = new MBeanInfo(this.getClass().getName(), "Hudson Job", attributeInfos, null, null, null);
 		}
 
 		/**
 		 * @see javax.management.DynamicMBean#getAttribute(java.lang.String)
 		 */
 		public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
 			if(STATUS.equals(attribute)){
 				return getStatus();
 			}
 			return null;
 		}
 		
 		private String getStatus(){
 			return job.getLastBuild() != null ? job.getLastBuild().getResult().toString():null;
 		}
 
 		/**
 		 * @see javax.management.DynamicMBean#getAttributes(java.lang.String[])
 		 */
 		public AttributeList getAttributes(String[] attributes) {
 			AttributeList result = null;
 			for(String name : attributes){
 				if(STATUS.equals(name)){
 					result = new AttributeList();
 					result.add(new Attribute(name, getStatus()));
 				}
 			}
 			return result;
 		}
 
 		/**
 		 * @see javax.management.DynamicMBean#getMBeanInfo()
 		 */
 		public MBeanInfo getMBeanInfo() {
 			return dMBeanInfo;
 		}
 
 		/**
 		 * @see javax.management.DynamicMBean#invoke(java.lang.String, java.lang.Object[], java.lang.String[])
 		 */
 		public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
 			return null;
 		}
 
 		/**
 		 * @see javax.management.DynamicMBean#setAttribute(javax.management.Attribute)
 		 */
 		public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
 		}
 
 		/**
 		 * @see javax.management.DynamicMBean#setAttributes(javax.management.AttributeList)
 		 */
 		public AttributeList setAttributes(AttributeList attributes) {
 			return null;
 		}
 
 	}
 }
