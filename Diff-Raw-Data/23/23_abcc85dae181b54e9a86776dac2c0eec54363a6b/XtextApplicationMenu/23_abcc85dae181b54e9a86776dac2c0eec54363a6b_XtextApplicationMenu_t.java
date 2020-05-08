 package org.eclipse.emf.refactor.refactoring.xtext.ui;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.refactor.refactoring.configuration.managers.ConfigurationManager;
 import org.eclipse.emf.refactor.refactoring.core.Refactoring;
 import org.eclipse.emf.refactor.refactoring.managers.ProjectManager;
 import org.eclipse.emf.refactor.refactoring.xtext.interfaces.IXtextDataManagement;
 import org.eclipse.emf.refactor.refactoring.xtext.managers.XtextSelectionManager;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.jface.action.ContributionItem;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
 import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.editor.utils.EditorUtils;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 public class XtextApplicationMenu extends ContributionItem {
 	
 	private final List<EObject> selection;
 
 	public XtextApplicationMenu() {
 		selection = XtextSelectionManager.getESelection();
 		System.out.println("selection: " +  selection);
 	}
 
 	public XtextApplicationMenu(String id) {
 		super(id);
 		selection = XtextSelectionManager.getESelection();
 		System.out.println("selection: " +  selection);
 	}
 	
 	@Override
 	public void fill(Menu menu, int index) {			
 		ConfigurationManager.getInstance();
 		IProject project = ProjectManager.getActualProject();
 		LinkedList<Refactoring> refactorings = 
 				ConfigurationManager.getSelectedRefactorings(project);		
 		for(final Refactoring r : refactorings){
 			if(r.getGui().showInMenu(this.selection)){	
 				MenuItem menuItem = new MenuItem(menu, SWT.CHECK, index);
 				menuItem.setText(r.getName());
 				menuItem.addSelectionListener(new SelectionAdapter() {
 					public void widgetSelected(SelectionEvent e) {
 						try {
							XtextDocument doc = null;
							if (r.getController().getDataManagementObject()
									instanceof IXtextDataManagement) {
								IXtextDataManagement dm = (IXtextDataManagement) r.getController()
										.getDataManagementObject();
								doc = dm.getXtextDocument();
							} else {
								doc = getXtextDocument();
							}
 							doc.modify(new IUnitOfWork.Void<XtextResource>() {
 
 								@Override
 								public void process(XtextResource state)
 										throws Exception {
 									try {
 										// 1. Register EditingDomain for ResourceSet
 										ResourceSet synRset = selection.get(0).eResource().getResourceSet();
 										TransactionalEditingDomain domain = TransactionalEditingDomain.Factory.INSTANCE
 													.createEditingDomain(synRset);
 										TransactionalEditingDomain.Registry.INSTANCE.add("xtext.domain", domain);
 										// 2. Set Selection:
 										r.getController().setSelection(selection);							
 										// 3. Preselect Values:
 										r.getController().getDataManagementObject().preselect(selection);							
 										// 4. Start Refactoring:
 										Shell shell = Display.getDefault().getActiveShell();
 										RefactoringWizardOpenOperation dialog = 
 											new RefactoringWizardOpenOperation (r.getGui().show());
 										dialog.run(shell, "Refactoring: " + r.getName());
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
 								
 							} );
 							
 						} catch (Exception e2) {
 							MessageDialog
 								.openError(null, "Error", e2.getMessage());
 						} 
 					}

					private XtextDocument getXtextDocument() {
						XtextEditor editor = EditorUtils.getActiveXtextEditor();
						System.out.println("XtextEditor: " + editor);
						return (XtextDocument) editor.getDocument();
					}
 				});
 			}			
 		}	
 	}
 
 }
