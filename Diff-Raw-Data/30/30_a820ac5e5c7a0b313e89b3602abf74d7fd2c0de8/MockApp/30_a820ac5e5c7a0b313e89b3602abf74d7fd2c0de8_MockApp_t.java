 package com.conx.logistics.client.web.app.mock;
 
 import com.conx.logistics.kernel.pageflow.services.IPageFlowManager;
 import com.conx.logistics.kernel.pageflow.services.IPageFlowSession;
 import com.conx.logistics.mdm.domain.task.TaskDefinition;
 import com.vaadin.Application;
 import com.vaadin.ui.Window;
 
 public class MockApp extends Application {
 	private static final long serialVersionUID = -5470222303880854277L;
 	private IPageFlowSession pfs;
	private IPageFlowManager defaultPageFlowEngine;
 	
 	public void start() {
 	}
 	
 	public void stop() {
 	}
 
 	@Override
 	public void init() {
 		Window w = new Window();
 		w.setSizeFull();
 		
 		TaskDefinition td = new TaskDefinition();
 		td.setBpmn2ProcDefURL("");
 		td.setProcessId("whse.rcv.asn.CreateNewASNByOrg");
 		
 		pfs = defaultPageFlowEngine.startPageFlowSession("skeswa", td);
 		
 		w.addComponent(pfs.getWizardComponent());
 		
 		this.setMainWindow(w);
 	}
 
 	public IPageFlowManager getDefaultPageFlowEngine() {
 		return defaultPageFlowEngine;
 	}
 
 	public void setDefaultPageFlowEngine(IPageFlowManager defaultPageFlowEngine) {
		this.defaultPageFlowEngine = defaultPageFlowEngine;
 	}
 }
