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
 import de.ptb.epics.eve.editor.gef.ScanDescriptionEditor;
 import de.ptb.epics.eve.editor.gef.commands.CreateSMConnection;
 import de.ptb.epics.eve.editor.gef.commands.CreateScanModule;
 import de.ptb.epics.eve.editor.gef.editparts.ScanModuleEditPart;
 
 /**
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class AddNestedScanModule extends AbstractHandler {
 
 	private static Logger logger = Logger.getLogger(AddNestedScanModule.class
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
 		Object element = ((IStructuredSelection)selection).getFirstElement();
 		
 		if (element instanceof ScanModuleEditPart) {
 			scanModule = ((ScanModuleEditPart)element).getModel();
 		} else {
 			return null;
 		}
 		
 		if (HandlerUtil.getActiveEditor(event) instanceof ScanDescriptionEditor) {
 			ScanDescriptionEditor editor = (ScanDescriptionEditor)
 					HandlerUtil.getActiveEditor(event);
 			CommandStack commandStack = editor.getCommandStack();
 			Command compositeCmd = null;
 			CreateScanModule createCmd = new CreateScanModule(
 					scanModule.getChain(), new Rectangle(
 							scanModule.getX() + 130, scanModule.getY() + 100,
 							scanModule.getWidth(), scanModule.getHeight()),
					scanModule.getType());
 			Command connCmd = new CreateSMConnection(scanModule,
 					createCmd.getScanModule(), Connector.NESTED);
 			compositeCmd = createCmd.chain(connCmd);
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
 						.getNested() == null));
 			}
 			return ((ScanModuleEditPart) element).getModel().getNested() == null;
 		}
 		return false;
 	}
 }
