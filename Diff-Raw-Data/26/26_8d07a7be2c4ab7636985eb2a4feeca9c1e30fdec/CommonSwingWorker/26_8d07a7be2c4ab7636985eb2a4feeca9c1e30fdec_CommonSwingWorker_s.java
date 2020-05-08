 package org.aesthete.swingobjects.workers;
 
 import javax.swing.SwingWorker;
 
 import org.aesthete.swingobjects.datamap.DataMapper;
 import org.aesthete.swingobjects.datamap.SwingObjData;
 import org.aesthete.swingobjects.exceptions.ErrorSeverity;
 import org.aesthete.swingobjects.exceptions.SwingObjectException;
 import org.aesthete.swingobjects.exceptions.SwingObjectRunException;
 import org.aesthete.swingobjects.exceptions.SwingObjectsExceptions;
 import org.aesthete.swingobjects.scope.RequestScope;
 import org.aesthete.swingobjects.scope.RequestScopeObject;
 import org.aesthete.swingobjects.view.CommonUI;
 import org.aesthete.swingobjects.view.WaitDialog;
 
 public abstract class CommonSwingWorker extends SwingWorker<Void, Void> implements SwingWorkerInterface{
 
 	private String action;
 
 	public CommonSwingWorker() {
 		this.action = "";
 	}
 
 	public CommonSwingWorker(String action) {
 		this.action = action;
 	}
 
 
 	@Override
 	protected Void doInBackground() throws Exception {
 		WaitDialog.displayWaitDialog();
         RequestScopeObject scopeObj=RequestScope.getRequestObj();
         try {
 			callModel(scopeObj);
 		}catch(SwingObjectException e) {
 			scopeObj.setErrorObj(e);
 		}catch(SwingObjectRunException e) {
			scopeObj.setErrorObj(e);
 		}catch(Exception e) {
			scopeObj.setErrorObj(new SwingObjectException(e,ErrorSeverity.SEVERE, CommonSwingWorker.class));
 		}
 
 		return null;
 	}
 
 
 
 	@Override
 	protected void done() {
         RequestScopeObject scopeObj=RequestScope.getRequestObj();
         try {
             WaitDialog.hideWaitDialog();
             CommonUI.restoreComponentsToInitialState(scopeObj.getFieldsOfTheContainer());
 			handleErrorAndCallConnector();
 		}finally {
 			RequestScope.endOfRequest(scopeObj);
 		}
 	}
 
 	/**
 	 * Only errors INFO and WARNING will go ahead and call the Connector. Error and Severe, will not.
 	 * Throw the correct errors from the model and user will only see the error and stop.
 	 * If you would like to call the connector inspite of the error/severe in your application, override this method
 	 * and change the implementation.
 	 * Make sure you call the DataMapper, to get the data in {@link SwingObjData} that you have set in your model, to automatically update the
 	 * GUIs. You will get to set the unmapped data from the model to the GUI in the connector.
 	 * The line you will need to retain for auto Data Mapping is:
 	 * <pre>
 	 *    DataMapper.mapGUI(scopeObj.getContainer());
 	 * </pre>
 	 */
 	protected void handleErrorAndCallConnector() {
 		try {
             RequestScopeObject scopeObj=RequestScope.getRequestObj();
 
             SwingObjectsExceptions e = scopeObj.getErrorObj();
 			boolean isCall=true;
 			if(e!=null) {
 				CommonUI.showErrorDialogForComponent(e);
 				switch(e.getErrorSeverity()) {
 				case ERROR:
 				case SEVERE: isCall=false; break;
 				default: // call connector
 					break;
 				}
 			}
 
 			if(isCall) {
 				DataMapper.mapGUI(scopeObj.getContainer());
 				callConnector(scopeObj);
 			}
 		}catch(SwingObjectRunException e) {
 			CommonUI.showErrorDialogForComponent(e);
 		}catch(Exception e) {
 			CommonUI.showErrorDialogForComponent(new SwingObjectRunException(e, this.getClass()));
 		}
 	}
 
 	public String getAction() {
 		return action;
 	}
 
 	public void setAction(String action) {
 		this.action = action;
 	}
 
 	@Override
 	public boolean validateAndPopulate(RequestScopeObject scopeObj) {
 		return true;
 	}
 
 	/**
 	 * Override to provide your implementation. This does not execute in the EDT. Its called from the SwingWorker's doInBackground()
 	 * method. This is where a call to your model should go.
 	 */
 	@Override
 	public void callModel(RequestScopeObject scopeObj) throws Exception {
 	}
 
 	/**
 	 * Override to provide your implementation. This executed in the EDT. Its called from the SwingWorker's done() method.
 	 * In this method you would have to update your UI. If in model you have set your UI details into the SwingObjData class,
 	 * then this method will be called after the data from the SwingObjData is set into your UI elements. Use this method
 	 * to update additional UI elements. Like closing a frame, for example.
 	 */
 	@Override
 	public void callConnector(RequestScopeObject scopeObj) {
 	}
 
     @Override
     public boolean proceed(){
         return true;
     }
 }
