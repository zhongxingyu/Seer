 /**
  * 
  */
 package org.cotrix.web.importwizard.client;
 
 import org.cotrix.web.importwizard.client.event.ImportBus;
 import org.cotrix.web.importwizard.client.event.ImportProgressEvent;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent;
 import org.cotrix.web.importwizard.client.event.ResetWizardEvent.ResetWizardHandler;
 import org.cotrix.web.importwizard.client.event.SaveEvent;
 import org.cotrix.web.importwizard.client.event.ImportProgressEvent.ImportProgressHandler;
 import org.cotrix.web.importwizard.client.flow.CheckPointNode.CheckPointHandler;
 
 import com.google.inject.Inject;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class SaveCheckPoint implements CheckPointHandler, ImportProgressHandler, ResetWizardHandler {
 	

 	protected EventBus importEventBus;
 	
 	protected boolean importComplete = false;
 	protected boolean eventFired = false;
 	
 	@Inject
 	public SaveCheckPoint(@ImportBus EventBus importEventBus)
 	{
 		this.importEventBus = importEventBus;
 		bind();
 	}
 	
 	protected void bind()
 	{
 		importEventBus.addHandler(ImportProgressEvent.TYPE, this);
 		importEventBus.addHandler(ResetWizardEvent.TYPE, this);
 	}
 	
 	@Override
 	public boolean check() {
 		if (!eventFired) {
 			fireSaveEvent();
 			eventFired = true;
 		}
 		return importComplete;
 	}
 	
 	protected void fireSaveEvent()
 	{
 		importEventBus.fireEvent(new SaveEvent());
 	}
 	
 	@Override
 	public void onImportProgress(ImportProgressEvent event) {
 		importComplete = event.getProgress().isComplete();
 	}
 
 	@Override
 	public void onResetWizard(ResetWizardEvent event) {
 		importComplete = false;
 		eventFired = false;
 	}
 
 }
