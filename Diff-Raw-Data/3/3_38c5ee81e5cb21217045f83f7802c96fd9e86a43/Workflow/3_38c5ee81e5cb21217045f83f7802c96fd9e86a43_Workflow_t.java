 /**
  * 
  */
 package org.eclipse.emf.eef.codegen.flow;
 
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.eef.codegen.EEFCodegenPlugin;
 import org.eclipse.emf.eef.codegen.flow.var.WorkflowContext;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  *
  */
 public class Workflow extends Step {
 	
 	private Map<String, Step> steps;
 
 	/**
 	 * Create an empty flow 
 	 */
 	public Workflow(String name) {
 		super(name);
 		steps = new LinkedHashMap<String, Step>();
 	}
 
 	/**
 	 * Create a flow with the given steps
 	 * @param steps the workflow's steps
 	 */
 	public Workflow(String name, Map<String, Step> steps) {
 		super(name);
 		this.steps = steps;
 	}
 
 	/**
 	 * @param context
 	 */
 	public void setContext(WorkflowContext context) {
 		super.setContext(context);
 		for (Step step : steps.values()) {
 			step.setContext(context);
 		}
 	}
 
 	/**
 	 * Add a new step to the workflow.
 	 * @param key title of the step (must be unique)
 	 * @param step the step to add
 	 */
 	public void addStep(String key, Step step) {
 		if (context == null) {
 			context = new WorkflowContext();
 		}
 		step.setContext(context);
 		if (resourceSet == null) {
 			resourceSet = new ResourceSetImpl();
 		}
 		step.setResourceSet(resourceSet);
 		steps.put(key, step);
 	}
 	
 	
 	/**
 	 * {@inheritDoc]
 	 * @see org.eclipse.emf.eef.codegen.flow.Step#getResourceSet()
 	 */
 	public ResourceSet getResourceSet() {
 		if (resourceSet == null) {
 			resourceSet = new ResourceSetImpl();
 			for (Step step : steps.values()) {
 				step.setResourceSet(resourceSet);
 			}
 		}
 		return resourceSet;
 	}
 
 	/**
 	 * {@inheritDoc]
 	 * @see org.eclipse.emf.eef.codegen.flow.Step#setResourceSet(org.eclipse.emf.ecore.resource.ResourceSet)
 	 */
 	public void setResourceSet(ResourceSet resourceSet) {
 		super.setResourceSet(resourceSet);
 		for (Step step : steps.values()) {
 			step.setResourceSet(resourceSet);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.codegen.flow.Step#execute(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public IStatus execute(IProgressMonitor monitor) {
 		monitor.beginTask(name, steps.size());
 		for (Iterator<String> iterator = steps.keySet().iterator(); iterator.hasNext();) {
 			String key = (String) iterator.next();
 			monitor.subTask(key);
 			Step step = steps.get(key);
 			IStatus execute = step.execute(monitor);
 			if (!execute.isOK()) {
				EEFCodegenPlugin.getDefault().logError((Exception) execute.getException());
 				return execute;
 			}
 			monitor.worked(1);
 		}
 		return Status.OK_STATUS;
 	}
 
 }
