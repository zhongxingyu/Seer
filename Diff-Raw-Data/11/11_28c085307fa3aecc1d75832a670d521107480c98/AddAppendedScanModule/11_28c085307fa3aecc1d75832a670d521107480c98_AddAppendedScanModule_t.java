 package de.ptb.epics.eve.editor.handler.editor;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.commands.Command;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 import de.ptb.epics.eve.data.scandescription.Connector;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.ScanModuleTypes;
 import de.ptb.epics.eve.data.scandescription.StartEvent;
 import de.ptb.epics.eve.editor.gef.ScanDescriptionEditor;
 import de.ptb.epics.eve.editor.gef.commands.CreateSEConnection;
 import de.ptb.epics.eve.editor.gef.commands.CreateSMConnection;
 import de.ptb.epics.eve.editor.gef.commands.CreateScanModule;
 import de.ptb.epics.eve.editor.gef.editparts.ScanModuleEditPart;
 import de.ptb.epics.eve.editor.gef.editparts.StartEventEditPart;
 
 /**
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class AddAppendedScanModule extends AbstractHandler {
 
 	private static Logger logger = Logger.getLogger(AddAppendedScanModule.class
 			.getName());
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		ISelection selection = HandlerUtil.getCurrentSelection(event);
 		// look at what is selected
 		if (!(selection instanceof IStructuredSelection)) {
 			return null; // not compatible
 		}
 		if (((IStructuredSelection) selection).size() == 0) {
 			return null; // nothing selected
 		}
 		
 		ScanModule scanModule = null;
 		StartEvent startEvent = null;
 		Object element = ((IStructuredSelection)selection).getFirstElement();
 		
 		if (element instanceof ScanModuleEditPart) {
 			scanModule = ((ScanModuleEditPart)element).getModel();
 		} else if (element instanceof StartEventEditPart) {
 			startEvent = ((StartEventEditPart)element).getModel();
 		} else {
 			return null;
 		}
 		
 		if (HandlerUtil.getActiveEditor(event) instanceof ScanDescriptionEditor) {
 			ScanDescriptionEditor editor = (ScanDescriptionEditor)
 					HandlerUtil.getActiveEditor(event);
 			CommandStack commandStack = editor.getCommandStack();
 			Command compositeCmd = null;
 			
 			if (scanModule != null) {
 				CreateScanModule createCmd = new CreateScanModule(
 						scanModule.getChain(), new Rectangle(
 								scanModule.getX() + 130, scanModule.getY(),
 								scanModule.getWidth(), scanModule.getHeight()),
						ScanModuleTypes.CLASSIC);
 				Command connCmd = new CreateSMConnection(scanModule,
 						createCmd.getScanModule(), Connector.APPENDED);
 				compositeCmd = createCmd.chain(connCmd);
 			} else if (startEvent != null) {
 				CreateScanModule createCmd = new CreateScanModule(
 						startEvent.getChain(), new Rectangle(
 								startEvent.getX() + 130, startEvent.getY(),
 								ScanModule.default_width,
 								ScanModule.default_height), ScanModuleTypes.CLASSIC);
 				Command connCmd = new CreateSEConnection(startEvent,
 						createCmd.getScanModule());
 				compositeCmd = createCmd.chain(connCmd);
 			} else {
 				return null;
 			}
 			
 			commandStack.execute(compositeCmd);
 		}
 		return null;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isEnabled() {
 		ISelection selection = PlatformUI.getWorkbench().
 				getActiveWorkbenchWindow().getSelectionService().getSelection();
 		// look at what is selected
 		if (!(selection instanceof IStructuredSelection)) {
 			return false; // not compatible
 		}
 		if (((IStructuredSelection) selection).size() == 0) {
 			return false; // nothing selected
 		}
 		Object element = ((IStructuredSelection) selection).getFirstElement();
 		if (element instanceof ScanModuleEditPart) {
 			if (logger.isDebugEnabled()) {
 				logger.debug("isEnabled: " + 
 						Boolean.toString(((ScanModuleEditPart) element).getModel()
 						.getAppended() == null));
 			}
 			return ((ScanModuleEditPart) element).getModel().getAppended() == null;
 		} else if (element instanceof StartEventEditPart) {
 			return ((StartEventEditPart) element).getModel().getConnector() == null;
 		}
 		return false;
 	}
 }
