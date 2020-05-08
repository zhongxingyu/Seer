 package org.eclipse.stem.analysis.automaticexperiment;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 
 import automaticexperiment.AutomaticExperiment;
 
 public class AutomaticExperimentManager {
 	private static AutomaticExperimentManager instance = null;
 	private ArrayList<AutomaticExperimentManagerListener> listeners = new ArrayList<AutomaticExperimentManagerListener>();
 	
 	private AutomaticExperimentManager() {
 	}
 	public static AutomaticExperimentManager getInstance() {
 		if(instance == null)instance = new AutomaticExperimentManager();
 		return instance;
 	}
 	
 	public ErrorAnalysisAlgorithm createAlgorithm(String name) {
 		ErrorAnalysisAlgorithm result = ErrorAnalysisAlgorithmFactory.INSTANCE.createErrorAnalysisAlgorithm(name);
 		return result;
 	}
 	
 	public void executeAlgorithm(final ErrorAnalysisAlgorithm algorithm, final AutomaticExperiment automaticExperiment) {
 		algorithm.init(automaticExperiment, algorithm);
 		// Stefan fix, we can't hold up the UI thread, it causes memory leaks
 		Job j = new Job("Minimizer algorith") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				algorithm.execute();
 				return Status.OK_STATUS;
 			}
 		};
 		j.schedule();
 		
 		AutomaticExperimentManagerEvent newEvent = new AutomaticExperimentManagerEvent(automaticExperiment, algorithm, MANAGER_STATUS.SCHEDULED);
 		fireEvent(newEvent);
 	}
 	
 	private void fireEvent(AutomaticExperimentManagerEvent evt) {
 		for(AutomaticExperimentManagerListener l:listeners) l.eventReceived(evt);
 	}
 	
 	public void addListener(AutomaticExperimentManagerListener l) {
 		this.listeners.add(l);
 	}
 }
