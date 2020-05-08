 package org.jenkinsci.plugins.proccleaner;
 
 import hudson.DescriptorExtensionList;
 import hudson.ExtensionList;
 import hudson.ExtensionPoint;
 import hudson.model.BuildListener;
 import hudson.model.Describable;
 import hudson.model.Descriptor;
 import hudson.remoting.Callable;
 import jenkins.model.Jenkins;
 
 public abstract class ProcCleaner implements Callable<Void,Exception>, Describable<ProcCleaner>, ExtensionPoint {
 
	private BuildListener log;
 	
 	public BuildListener getLog() {
 		return log;
 	}
 	
 	public void setLog(BuildListener log) {
 		this.log = log;
 	}
 	
 	//public abstract void doCleanup();
 
 	public ProcCleanerDescriptor getDescriptor() {
 		return (ProcCleanerDescriptor) Jenkins.getInstance().getDescriptor(getClass());
 	}
 
 	public static ExtensionList<ProcCleaner> all() {
 		return Jenkins.getInstance().getExtensionList(ProcCleaner.class);
 	}
 
 	public static DescriptorExtensionList<ProcCleaner,Descriptor<ProcCleaner>> getCleanerDescriptors() {
 		return Jenkins.getInstance().<ProcCleaner,Descriptor<ProcCleaner>>getDescriptorList(ProcCleaner.class);
 	}
 	
 	public static class ProcCleanerDescriptor extends Descriptor<ProcCleaner> {
 	
 		protected ProcCleanerDescriptor(Class<? extends ProcCleaner> clazz) {
 			super(clazz);
 		}
 
 		protected ProcCleanerDescriptor() {
 		}
 		
 		public String getDisplayName() {
             return clazz.getSimpleName();
         }
 	}
 
 }
