 package org.openetcs.ui.internal.ecp;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.emf.ecp.core.ECPProject;
 import org.eclipse.emf.ecp.ui.util.HandlerHelper;
 import org.eclipse.emf.edit.command.ChangeCommand;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.openetcs.model.ertmsformalspecs.Dictionary;
 import org.openetcs.model.ertmsformalspecs.ModelFactory;
 
 public class NewDictionaryHandler extends AbstractHandler {
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		ISelection selection=HandlerUtil.getCurrentSelection(event);
 		if(selection instanceof IStructuredSelection){
 			IStructuredSelection ssel=(IStructuredSelection)selection;
 			if(ssel.getFirstElement() instanceof ECPProject){
 				final ECPProject project=(ECPProject) ssel.getFirstElement();
 				
 				final Dictionary dictionary=ModelFactory.eINSTANCE.createDictionary();
 				
 				project.getEditingDomain().getCommandStack().execute(new ChangeCommand(dictionary) {
 					
 					@Override
 					protected void doExecute() {
 						project.getElements().add(dictionary);
 					}
 				});
 				
				HandlerHelper.openModelElement(dictionary, NewDictionaryHandler.class.getName(), project);
 			}
 		}
 		return null;
 	}
 
 }
