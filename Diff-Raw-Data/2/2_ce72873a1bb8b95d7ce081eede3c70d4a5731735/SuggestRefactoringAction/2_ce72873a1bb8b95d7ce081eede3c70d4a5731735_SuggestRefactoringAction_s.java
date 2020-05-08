 package org.eclipse.emf.refactor.smells.xtext.handler;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.emf.common.command.Command;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.refactor.refactoring.core.Refactoring;
 import org.eclipse.emf.refactor.refactoring.xtext.interfaces.IXtextDataManagement;
 import org.eclipse.emf.refactor.smells.core.ModelSmell;
 import org.eclipse.emf.refactor.smells.eraser.managers.EraseManager;
 import org.eclipse.emf.refactor.smells.eraser.ui.SuggestionDialog;
 import org.eclipse.emf.refactor.smells.runtime.core.EObjectGroup;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.xtext.resource.XtextResource;
 import org.eclipse.xtext.ui.editor.XtextEditor;
 import org.eclipse.xtext.ui.editor.model.XtextDocument;
 import org.eclipse.xtext.ui.editor.utils.EditorUtils;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 public class SuggestRefactoringAction implements IObjectActionDelegate {
 	
 	private Shell shell;
 	private ISelection selection;
 	Refactoring selectedRefactoring = null;
 	EObject contextObject = null;
 
 	public SuggestRefactoringAction() {
 		super();
 	}
 
 	@Override
 	public void run(IAction action) {
 		try {
 		System.out.println("Here I am ...");
 		EObjectGroup group = null;
 		if(selection instanceof IStructuredSelection){
 			IStructuredSelection structSelect = (IStructuredSelection)selection;
 			if(!structSelect.isEmpty() && structSelect.getFirstElement() instanceof EObjectGroup)
 				group = (EObjectGroup) structSelect.getFirstElement();
 		}
 		if(group != null){
 			System.out.println("Group: " +  group);			
 			ModelSmell smell = group.getModelSmell();
			SuggestionDialog dialog = new SuggestionDialog(shell, EraseManager.getCausedModelSmells(EraseManager.getFixingRefactorings(smell)), group);
 			int dialogResult = dialog.open();
 			if(dialogResult == Dialog.OK) {
 				selectedRefactoring = dialog.getSelectedRefactoring();
 				contextObject = dialog.getSelectedContextObject();
 				IXtextDataManagement dm = 
 						(IXtextDataManagement) selectedRefactoring.getController().getDataManagementObject();
 				XtextDocument doc = dm.getXtextDocument();
 				XtextEditor editor = EditorUtils.getActiveXtextEditor();
 				System.out.println("Active XtextEditor: " + editor);
 				doc.modify(new IUnitOfWork.Void<XtextResource>() {
 
 					@Override
 					public void process(XtextResource state) throws Exception {
 						refactor();
 					}
 					
 				});
 //				ArrayList<EObject> contextList = new ArrayList<EObject>();
 //				contextList.add(contextObject);
 //				selectedRefactoring.getController().setSelection(contextList);
 //				selectedRefactoring.getController().getDataManagementObject().preselect(contextList);
 //				RefactoringWizard wizard = selectedRefactoring.getGui().show();
 //				RefactoringWizardOpenOperation wizardDialog = new RefactoringWizardOpenOperation(wizard);
 //				try {
 //					wizardDialog.run(shell, "EMF Refactor");
 //				} catch (InterruptedException e) {
 //					e.printStackTrace();
 //				}
 			}
 		}
 		} catch (Exception e2) {
 			MessageDialog
 				.openError(null, "Error", e2.getMessage());
 		} 
 	}
 
 //	private void doXtextRefactoring(Refactoring r, EObject contextObject) {
 //		
 //		
 //	}
 
 	@Override
 	public void selectionChanged(IAction action, ISelection selection) {
 		this.selection = selection;
 	}
 
 	@Override
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 		shell = targetPart.getSite().getShell();
 	}
 
 	private void refactor() {
 		try {
 			// 1. Register EditingDomain for ResourceSet
 			ResourceSet synRset = contextObject.eResource().getResourceSet();
 			TransactionalEditingDomain domain = TransactionalEditingDomain.Factory.INSTANCE
 						.createEditingDomain(synRset);
 			TransactionalEditingDomain.Registry.INSTANCE.add("xtext.domain", domain);
 			domain.getCommandStack()
 				.execute(new Command() {
 
 					@Override
 					public boolean canExecute() {
 						// TODO Auto-generated method stub
 						return false;
 					}
 
 					@Override
 					public void execute() {
 						// 2. Set Selection:
 						ArrayList<EObject> contextList = new ArrayList<EObject>();
 						contextList.add(contextObject);
 						selectedRefactoring.getController().setSelection(contextList);							
 						// 3. Preselect Values:
 						selectedRefactoring.getController().getDataManagementObject().preselect(contextList);							
 						// 4. Start Refactoring:
 						Shell shell = Display.getDefault().getActiveShell();
 						RefactoringWizardOpenOperation dialog = 
 							new RefactoringWizardOpenOperation (selectedRefactoring.getGui().show());
 						try {
 							dialog.run(shell, "Refactoring: " + selectedRefactoring.getName());
 						} catch (InterruptedException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					}
 
 					@Override
 					public boolean canUndo() {
 						// TODO Auto-generated method stub
 						return false;
 					}
 
 					@Override
 					public void undo() {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public void redo() {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public Collection<?> getResult() {
 						// TODO Auto-generated method stub
 						return null;
 					}
 
 					@Override
 					public Collection<?> getAffectedObjects() {
 						// TODO Auto-generated method stub
 						return null;
 					}
 
 					@Override
 					public String getLabel() {
 						// TODO Auto-generated method stub
 						return null;
 					}
 
 					@Override
 					public String getDescription() {
 						// TODO Auto-generated method stub
 						return null;
 					}
 
 					@Override
 					public void dispose() {
 						// TODO Auto-generated method stub
 						
 					}
 
 					@Override
 					public Command chain(Command command) {
 						// TODO Auto-generated method stub
 						return null;
 					}
 					
 				});
 //			// 2. Set Selection:
 //			ArrayList<EObject> contextList = new ArrayList<EObject>();
 //			contextList.add(contextObject);
 //			r.getController().setSelection(contextList);							
 //			// 3. Preselect Values:
 //			r.getController().getDataManagementObject().preselect(contextList);							
 //			// 4. Start Refactoring:
 //			Shell shell = Display.getDefault().getActiveShell();
 //			RefactoringWizardOpenOperation dialog = 
 //				new RefactoringWizardOpenOperation (r.getGui().show());
 //			dialog.run(shell, "Refactoring: " + r.getName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			// 5. Remove EditingDomain from Registry
 			TransactionalEditingDomain transEditingDomain 
 				= TransactionalEditingDomain.Registry.INSTANCE.getEditingDomain("xtext.domain");
 			transEditingDomain.dispose();
 			TransactionalEditingDomain.Registry.INSTANCE.remove("xtext.domain");
 		}
 	}
 
 }
