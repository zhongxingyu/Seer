 package com.conx.logistics.kernel.pageflow.engine.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.vaadin.teemu.wizards.Wizard;
 
 import com.conx.logistics.kernel.pageflow.engine.PageFlowEngineImpl;
 import com.conx.logistics.kernel.pageflow.engine.PageFlowSessionImpl;
 import com.conx.logistics.kernel.pageflow.services.ITaskWizard;
 import com.conx.logistics.kernel.pageflow.services.PageFlowPage;
 import com.conx.logistics.mdm.domain.application.Feature;
 import com.vaadin.ui.Component;
 
 public class TaskWizard extends Wizard implements ITaskWizard {
 	private static final long serialVersionUID = 8417208260717324494L;
 	
 	private PageFlowSessionImpl session;
 	private PageFlowEngineImpl engine;
 	
 	public TaskWizard(PageFlowSessionImpl session, PageFlowEngineImpl engine) {
 		this.engine = engine;
 		this.session = session;
 	}
 
 	public PageFlowSessionImpl getSession() {
 		return session;
 	}
 
 	public void setSession(PageFlowSessionImpl session) {
 		this.session = session;
 	}
 
 	@Override
 	public Component getComponent() {
 		return this;
 	}
 
 	@Override
 	public Map<String, Object> getProperties() {
 		Map<String,Object> props = new HashMap<String, Object>();
 		props.put("session",session);
 		props.putAll(session.getProcessVars());
 		return props;
 	}
 
 	@Override
 	public Feature getOnCompletionFeature() {
 		return session.getOnCompletionFeature();
 	}
 
 	@Override
 	public void onNext(PageFlowPage currentPage, Map<String, Object> taskOutParams) {
 		try {
 			engine.executeTaskWizard(this, taskOutParams);
 //			getProperties().
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void onPrevious(PageFlowPage currentPage, Map<String, Object> state) {
 		// TODO Implement previous
 	}
 	
 	@Override
 	public void next() {
 		PageFlowPage currentPage, nextPage;
 		Map<String, Object> params = null;
 		currentPage = (PageFlowPage) currentStep;
 		// Complete current task and get input variables for the next task
 		try {
 			params = currentPage.getOnCompleteState(); // Completes current task with
 			params = engine.executeTaskWizard(this, params).getProperties();
 		} catch (Exception e) {
 			getWindow().showNotification("Could not complete this task");
 			// TODO Exception Handing
 			e.printStackTrace();
 			return;
 		}
 		// Start the next task (if it exists) with input variables from previous task
 		int index = steps.indexOf(currentStep);
 		if ((index + 1) < steps.size()) {
			nextPage = (PageFlowPage) steps.get(index + 1);
 			nextPage.setOnStartState(params);
 		}
 		super.next();
 	}
 }
