 package net.bioclipse.brunn.ui.explorer;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.bioclipse.actions.LoginAction;
 import net.bioclipse.brunn.Springcontact;
 import net.bioclipse.brunn.business.audit.IAuditManager;
 import net.bioclipse.brunn.business.folder.IFolderManager;
 import net.bioclipse.brunn.business.origin.IOriginManager;
 import net.bioclipse.brunn.business.plate.IPlateManager;
 import net.bioclipse.brunn.business.plateLayout.IPlateLayoutManager;
 import net.bioclipse.brunn.pojos.LayoutWell;
 import net.bioclipse.brunn.pojos.User;
 import net.bioclipse.brunn.results.PlateResults;
 import net.bioclipse.brunn.ui.Activator;
 import net.bioclipse.brunn.ui.dialogs.CreateCellType;
 import net.bioclipse.brunn.ui.dialogs.CreateCompound;
 import net.bioclipse.brunn.ui.dialogs.CreateFolder;
 import net.bioclipse.brunn.ui.dialogs.CreateMasterPlate;
 import net.bioclipse.brunn.ui.dialogs.CreatePatientCell;
 import net.bioclipse.brunn.ui.dialogs.CreatePlate;
 import net.bioclipse.brunn.ui.dialogs.CreatePlateLayout;
 import net.bioclipse.brunn.ui.dialogs.CreatePlateType;
 import net.bioclipse.brunn.ui.dialogs.CreateUser;
 import net.bioclipse.brunn.ui.dialogs.Rename;
 import net.bioclipse.brunn.ui.dialogs.createMasterPlateFromSDF.CreateMasterPlateFromSDF;
 import net.bioclipse.brunn.ui.dialogs.importResults.ImportResults;
 import net.bioclipse.brunn.ui.editors.cellTypeEditor.CellTypeEditor;
 import net.bioclipse.brunn.ui.editors.compoundEditor.CompoundEditor;
 import net.bioclipse.brunn.ui.editors.masterPlateEditor.MasterPlateEditor;
 import net.bioclipse.brunn.ui.editors.patientSampleEditor.PatientCellEditor;
 import net.bioclipse.brunn.ui.editors.plateEditor.PlateMultiPageEditor;
 import net.bioclipse.brunn.ui.editors.plateLayoutEditor.PlateLayoutEditor;
 import net.bioclipse.brunn.ui.editors.plateTypeEditor.PlateTypeEditor;
 import net.bioclipse.brunn.ui.explorer.model.ITreeModelListener;
 import net.bioclipse.brunn.ui.explorer.model.ITreeObject;
 import net.bioclipse.brunn.ui.explorer.model.folders.AbstractFolder;
 import net.bioclipse.brunn.ui.explorer.model.folders.AbstractUniqueFolder;
 import net.bioclipse.brunn.ui.explorer.model.folders.CellTypes;
 import net.bioclipse.brunn.ui.explorer.model.folders.Compounds;
 import net.bioclipse.brunn.ui.explorer.model.folders.DataSets;
 import net.bioclipse.brunn.ui.explorer.model.folders.MasterPlates;
 import net.bioclipse.brunn.ui.explorer.model.folders.PatientCells;
 import net.bioclipse.brunn.ui.explorer.model.folders.PlateLayouts;
 import net.bioclipse.brunn.ui.explorer.model.folders.PlateTypes;
 import net.bioclipse.brunn.ui.explorer.model.folders.Resources;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.CellType;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.Compound;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.MasterPlate;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.NotLoggedIn;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.PatientSample;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.Plate;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.PlateLayout;
 import net.bioclipse.brunn.ui.explorer.model.nonFolders.PlateType;
 import net.bioclipse.brunn.ui.transferTypes.BrunnTransfer;
 import net.bioclipse.usermanager.IUserManagerListener;
 import net.bioclipse.usermanager.UserManagerEvent;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.PluginTransfer;
 import org.eclipse.ui.part.ViewPart;
 
 public class View extends ViewPart implements IUserManagerListener {
 
     public static final String ID = "net.bioclipse.brunn.ui.explorer.View";
 
 	private TreeViewer treeViewer;
 
 	private Action createPlateType;
 	private Action createPlateLayout;
 	private Action createMasterPlate;
 	private Action createCompound;
 	private Action createCellType;
 	private Action createFolder;
 	private Action rename;
 	private Action createMasterPlateFromSDF;
 	private Action createPlate;
 	private Action login;
 	private Action refresh;
 	private Action createPatientCell;
 	
 	private Action markPlateTypeDeleted;
 	private Action markPlateLayoutDeleted;
 	private Action markPlateDeleted;
 	private Action markMasterPlateDeleted;
 	private Action markCompoundDeleted;
 	private Action markCellDeleted;
 	private Action markFolderDeleted;
 	private Action markPatientCellDeleted;
 	
 	private Action unmarkPlateTypeDeleted;
 	private Action unmarkPlateLayoutDeleted;
 	private Action unmarkPlateDeleted;
 	private Action unmarkMasterPlateDeleted;
 	private Action unmarkCompoundDeleted;
 	private Action unmarkCellDeleted;
 	private Action unmarkFolderDeleted;
 	private Action unmarkPatientCellDeleted;
 	
 	private Action importResultsAction;
 	private Action toggleShowDeletedAction;
 	
 	private TreeRoot treeRoot;
 	private boolean showPojosMarkedAsDeleted;
 	/*
 	 * Used while creating many compounds by automagicly opening a new dialog 
 	 * after the first
 	 */
 	private static IStructuredSelection treeSelection;
 
 	private Action createUserAction;
 
 	private Shell shell;
 
 	public View(){
 		super();
 		net.bioclipse.usermanager.Activator
 		                         .getDefault()
 		                         .getUserManager()
 		                         .addListener(this);
 		showPojosMarkedAsDeleted = false;
 	}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		
 		this.treeRoot = new TreeRoot(this);
 		
 		if( Activator.getDefault().getCurrentUser() == null ) {
 			this.treeRoot.setLoggedOut();
 		}
 		else {
 			this.treeRoot.setLoggedIn();
 		}
 				
 		treeViewer = new TreeViewer(parent); 
 //		treeViewer.setLabelProvider(new ExplorerLabelProvider());
 		treeViewer.setContentProvider(new ExplorerContentProvider());
 		treeViewer.setLabelProvider(new DecoratingExplorerLabelProvider(new ExplorerLabelProvider(), null));
 		treeViewer.setInput(getInitialInput());
 		createActions();
 		createContextMenu();
 		addDoubleClickListeners();
 		addDragAndDropSupport();
 		createMenu();
 		shell = parent.getShell();
 
 		//Register as selection provider
 		getSite().setSelectionProvider(treeViewer);
 	}
 
 	private void createMenu() {
 		IMenuManager mgr = getViewSite().getActionBars().getMenuManager();
 		mgr.setRemoveAllWhenShown(true);
 		mgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager mgr) {
 				fillMenu(mgr);
 			}
 		});
 		mgr.add(login);
 	}
 
 	private void fillMenu(IMenuManager mgr) {
 		
 		if( !net.bioclipse.usermanager.Activator
 		        .getDefault().getUserManager().isLoggedIn() ) {
 			mgr.add(login);
 			return;
 		}
 		mgr.add(importResultsAction);
         mgr.add(toggleShowDeletedAction);
         try {
         	if( Activator.getDefault().getCurrentUser().isAdmin() ) {
             	mgr.add(createUserAction);
             }
         }
         catch (Exception e) {
         	e.printStackTrace();
         	System.out.println(Activator.getDefault().getCurrentUser());
 		}
 	}
 	
 	private void addDragAndDropSupport() {
 
 		Transfer[] transfers 
 			= new Transfer[] { BrunnTransfer.getInstance(), 
 				               PluginTransfer.getInstance() };
 		treeViewer.addDragSupport( DND.DROP_COPY | DND.DROP_MOVE, 
 				                   transfers, 
 				                   new ExplorerDragAdapter(treeViewer) );
 
 		treeViewer.addDropSupport( DND.DROP_MOVE, 
 				                   transfers, 
 				                   new ExplorerDropAdapter(treeViewer) );
 	}
 
 	private void addDoubleClickListeners() {
 
 		final View view = this;
 		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 
 				IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
 				final ITreeObject element = (ITreeObject)selection.getFirstElement();
 
 				if(element instanceof Plate) {
 					try {
 						PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, new IRunnableWithProgress() {
 							public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 								IPlateManager pm = (IPlateManager) Springcontact.getBean("plateManager");
 								long time = System.currentTimeMillis();
 								final PlateResults plateResults = 
 									pm.getPlateResults( (net.bioclipse.brunn.pojos.Plate)(((Plate)element).getPOJO()), 
 											            monitor);
 								System.out.println("Total time to create plateresults: " + (System.currentTimeMillis() - time) + "millis");
 								
 								final Plate plate = (Plate)element;
 								plate.setPlateResults(plateResults);
 								
 								Display.getDefault().asyncExec( new Runnable() {
 									public void run() {
 										try {
 											PlatformUI.getWorkbench()
 											.getActiveWorkbenchWindow()
 											.getActivePage()
 											.openEditor(  plate, PlateMultiPageEditor.ID, true );
 										} catch (PartInitException e) {
 											e.printStackTrace();
 										}
 									}
 								});
 							}
 						});
 					} 
 					catch (InvocationTargetException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} 
 					catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof PlateLayout) {
 					try {
 						PlateLayoutEditor editor = (PlateLayoutEditor) PlatformUI.getWorkbench().
 						getActiveWorkbenchWindow().
 						getActivePage().
 						openEditor( (PlateLayout)element, PlateLayoutEditor.ID, true );
 					} catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof PlateType) {
 					try {
 						PlatformUI.getWorkbench().
 						getActiveWorkbenchWindow().
 						getActivePage().
 						openEditor( (PlateType)element, PlateTypeEditor.ID, true );
 					} catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof MasterPlate) {
 					try {
						PlatformUI.getWorkbench()
						          .getActiveWorkbenchWindow()
						          .getActivePage()
						          .openEditor( (MasterPlate)element, 
						                       MasterPlateEditor.ID, 
						                       true );
 					} catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof Compound) {
 					try {
 						PlatformUI.getWorkbench().
 						getActiveWorkbenchWindow().
 						getActivePage().
 						openEditor( (Compound)element, CompoundEditor.ID, true );
 					} catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof CellType) {
 					try {
 						PlatformUI.getWorkbench().
 						getActiveWorkbenchWindow().
 						getActivePage().
 						openEditor( (CellType)element, CellTypeEditor.ID, true );
 					} catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof PatientSample) {
 					try {
 						PlatformUI.getWorkbench().
 						getActiveWorkbenchWindow().
 						getActivePage().
 						openEditor( (PatientSample)element, PatientCellEditor.ID, true );
 					}
 					catch (PartInitException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				if(element instanceof net.bioclipse.brunn.ui.explorer.model.folders.AbstractFolder) {
 					treeViewer.expandToLevel(element, 1);
 				}
 	            if( !net.bioclipse.usermanager.Activator
 	                    .getDefault().getUserManager().isLoggedIn() ) {
 	                login.run();
 	            }
 			}
 		});
 
 	}
 
 	private TreeRoot getInitialInput() {
 
 		return treeRoot;
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		if( treeViewer != null) {
 			treeViewer.getControl().setFocus();
 		}
 	}
 
 	protected void createActions(){
 
 		rename = new BrunnAction("Rename", treeViewer) {
 		    
 		    private Rename dialog;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 monitor.beginTask( "Renaming", IProgressMonitor.UNKNOWN );
                 try {
                     if (dialog.getReturnCode() == CreateFolder.OK) {
                         AbstractFolder recievingFolder 
                             = (AbstractFolder) selectedDomainObject
                                                .getFolder();
                         selectedDomainObject.changeName(dialog.getName());
                         recievingFolder.fireUpdate();                   
                     }
                 }
                 finally {
                     monitor.done();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 dialog = new Rename( PlatformUI.getWorkbench()
                                                .getActiveWorkbenchWindow()
                                                .getShell(), 
                                      selectedDomainObject.getName() );
                 return dialog.open();
             }
 		};
 
 		createFolder = new BrunnAction("Create Folder", treeViewer) {
 		    
 		    private CreateFolder dialog;
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 dialog 
                     = new CreateFolder( PlatformUI.getWorkbench()
                                                   .getActiveWorkbenchWindow()
                                                   .getShell() );
                 return dialog.open();
             }
             
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IFolderManager fom 
                     = (IFolderManager) Springcontact.getBean("folderManager");
 
                 fom.createFolder( Activator.getDefault().getCurrentUser(), 
                                   dialog.getName(),
                                   (net.bioclipse.brunn.pojos.Folder)
                                       recievingFolder.getPOJO() );
 
                 recievingFolder.fireUpdate();                   
             }
 		};
 
 		createPlate = new BrunnAction("Create Plate", treeViewer) {
 		    
 	        private CreatePlate dialog;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IPlateManager pm 
                     = (IPlateManager) Springcontact.getBean("plateManager");
                 
                 try {
                     if ( dialog.isDoingPatientCells() ) {
                         pm.createPlate( Activator.getDefault()
                                                  .getCurrentUser(), 
                                         dialog.getName(),
                                         dialog.getBarCode(),
                                         (net.bioclipse.brunn.pojos.Folder)
                                             recievingFolder.getPOJO(),
                                         dialog.getMasterPlate(),
                                         dialog.getPatientOrigin(),
                                         dialog.getTimestamp() );
                     }
                     else {
                         pm.createPlate( Activator.getDefault()
                                                  .getCurrentUser(), 
                                         dialog.getName(),
                                         dialog.getBarCode(),
                                         (net.bioclipse.brunn.pojos.Folder)
                                             recievingFolder.getPOJO(),
                                         dialog.getMasterPlate(),
                                         dialog.getCellOrigin(),
                                         dialog.getTimestamp() );
                     }
                 }
                 catch(IllegalStateException e) {
                     final Exception ex = e;
                     Display.getDefault().asyncExec( new Runnable() {
                         public void run() {
                             MessageDialog
                                 .openInformation( shell, 
                                                   "Could not create Plate", 
                                                   ex.getMessage() );
                         }
                     });
                 }
                 recievingFolder.fireUpdate();
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 dialog = new CreatePlate( PlatformUI.
                                           getWorkbench().
                                           getActiveWorkbenchWindow().
                                           getShell() );
                 return dialog.open();
             }
 		};
 
 		
 		createCellType = new BrunnAction("Create Cell Line", treeViewer) {
 		    
 		    private CreateCellType dialog;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IOriginManager orm 
                     = (IOriginManager) Springcontact.getBean("originManager");
 
                 orm.createCellOrigin( Activator.getDefault()
                                                .getCurrentUser(), 
                                       dialog.getName(),
                                       (net.bioclipse.brunn.pojos.Folder)
                                           recievingFolder.getPOJO() );
 
                 recievingFolder.fireUpdate();                
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 dialog 
                     = new CreateCellType( PlatformUI.getWorkbench()
                                                     .getActiveWorkbenchWindow()
                                                     .getShell() );
                 return dialog.open();
             }
 		};
 		
 		createPatientCell = new BrunnAction( "Create Patient Cell", 
 		                                     treeViewer ) {
 		    
 		    private CreatePatientCell dialog;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IOriginManager orm 
                     = (IOriginManager) Springcontact.getBean("originManager");
 
                 orm.createPatientOrigin( Activator.getDefault()
                                                   .getCurrentUser(), 
                                          dialog.getName(),
                                          dialog.getLid(),
                                          (net.bioclipse.brunn.pojos.Folder)
                                             recievingFolder.getPOJO() );
                 recievingFolder.fireUpdate();
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 dialog 
                     = new CreatePatientCell( PlatformUI
                                              .getWorkbench()
                                              .getActiveWorkbenchWindow()
                                              .getShell() );
                 return dialog.open();
             }
 		};
 
 		createCompound = new CreateCompoundAction(treeViewer); 
 
 		createMasterPlate = new BrunnAction( "Create Master Plate", 
 		                                     treeViewer ) {
 		    private CreateMasterPlate dialog;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IPlateManager pm 
                     = (IPlateManager) Springcontact.getBean("plateManager");
 
                 pm.createMasterPlate( Activator.getDefault().getCurrentUser(), 
                                       dialog.getName(),
                                       dialog.getSelectedPlateLayout(),
                                       (net.bioclipse.brunn.pojos.Folder)
                                           recievingFolder.getPOJO(), 
                                       dialog.getNumOfPlates() );
                 
                 recievingFolder.fireUpdate();
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 dialog = new CreateMasterPlate( PlatformUI
                                                 .getWorkbench()
                                                 .getActiveWorkbenchWindow()
                                                 .getShell() );
                 
                 int status =  dialog.open();
                 if ( status == dialog.OK ) {
                     // Check for well functions
                     boolean foundWellFunction = false;
                     
                     for ( LayoutWell lw : dialog.getSelectedPlateLayout()
                                                 .getLayoutWells() ) {
                         if ( lw.getWellFunctions().size() > 1) {
                             foundWellFunction = true;
                             break;
                         }
                     }
                     
                     if ( !foundWellFunction ) {
                         if ( !MessageDialog
                               .openConfirm( PlatformUI
                                             .getWorkbench()
                                             .getActiveWorkbenchWindow()
                                             .getShell(), 
                                             "No well functions", 
                                             "Selected plate layout has no "
                                             + "well functions. Are you sure "
                                             + "you want to continue?") ) {
                             return Dialog.CANCEL;
                         }
                     }
                 }
                 return Dialog.OK;
             }
 		};
 		
 		createMasterPlateFromSDF = new Action("Create Master Plate from SD-file") {
 			public void run() {
 				final Wizard wizard = new CreateMasterPlateFromSDF();
 				WizardDialog dialog = new WizardDialog( PlatformUI.
 						                                	getWorkbench().
 						                                	getActiveWorkbenchWindow().
 						                                	getShell(), 
 						                                wizard );
 				dialog.open();
 			}
 		};
 
 		importResultsAction = new Action("Import Results from File") {
 			public void run() {
 				
 				ITreeObject dataSets = null;
 				for( ITreeObject t : treeRoot.getChildren() ) {
 					if( "Data sets".equals(t.getName()) ) {
 						dataSets = t;
 					}
 				}
 				
 				final Wizard wizard = new ImportResults(dataSets);
 
 				WizardDialog dialog 
 				    = new WizardDialog( PlatformUI.getWorkbench()
 						                          .getActiveWorkbenchWindow()
 						                          .getShell(), 
 						                wizard );
 				dialog.open();
 			}
 		};
 		
 		createUserAction = new Action("Create User") {
 			public void run() {
 				final CreateUser dialog = new CreateUser( PlatformUI.
                     	                            getWorkbench().
                     	                            getActiveWorkbenchWindow().
                     	                            getShell() );
 				if ( dialog.open() == dialog.OK ) {
 				    Job job = new Job("Create User") {
 				        @Override
 				        protected IStatus run( IProgressMonitor monitor ) {
 				            monitor.beginTask( "Creating user", 
 				                               IProgressMonitor.UNKNOWN );
 				            try {
     		                    IAuditManager auditManager 
     		                        = (IAuditManager) 
     		                            Springcontact.getBean("auditManager");
     		                    auditManager
         		                    .createUser( Activator.getDefault()
         		                                          .getCurrentUser(), 
                                                  dialog.getUsername(), 
                                                  dialog.getPassWord(),
                                                  dialog.isAdmin() );
     		                    return Status.OK_STATUS;
 				            }
 				            finally { 
 				                monitor.done();
 				            }
 		                }
 				    };
 				    job.schedule();
 				}
 			}
 		};
 		
 		toggleShowDeletedAction = new Action("Show Objects Marked as Deleted", SWT.CHECK) {
 			public void run() {
 				showPojosMarkedAsDeleted = !showPojosMarkedAsDeleted;
 				if(showPojosMarkedAsDeleted) {
 					this.setText("don't show objects marked as deleted");
 				}
 				else {
 					this.setText("show objects marked as deleted");
 				}
 				try {
 					PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(true, false, new IRunnableWithProgress() {
 						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 							treeRoot.fireUpdate(monitor);
 						}
 					});
 				} 
 				catch (InvocationTargetException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} 
 				catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		};
 		
 		createPlateLayout = new BrunnAction( "Create Plate Layout", 
 		                                     treeViewer ) {
 		    
 		    private CreatePlateLayout dialog;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IPlateLayoutManager plm
                     = (IPlateLayoutManager) 
                           Springcontact.getBean("plateLayoutManager");
 
                 plm.createPlateLayout( Activator.getDefault()
                                                 .getCurrentUser(), 
                                        dialog.getName(),
                                        dialog.getPlateType(),
                                        (net.bioclipse.brunn.pojos.Folder)
                                            recievingFolder.getPOJO() );
 
                 recievingFolder.fireUpdate();
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 dialog = new CreatePlateLayout( 
                     PlatformUI.getWorkbench()
                               .getActiveWorkbenchWindow()
                               .getShell() );
                 return dialog.open();
             }
 		};
 
 		createPlateType = new BrunnAction("Create Plate Type", treeViewer) {
 		    
 		    private CreatePlateType dialog;
 			
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 AbstractFolder recievingFolder 
                     = (AbstractFolder) selectedDomainObject.getFolder();
                 IPlateLayoutManager plm 
                     = (IPlateLayoutManager) 
                         Springcontact.getBean("plateLayoutManager");
 
                 plm.createPlateType( Activator.getDefault().getCurrentUser(), 
                         dialog.getNumberOfColumns(),
                         dialog.getNumberOfRows(), 
                         dialog.getName(), 
                         (net.bioclipse.brunn.pojos.Folder)
                             recievingFolder.getPOJO() );
 
                 recievingFolder.fireUpdate();                   
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 dialog = new CreatePlateType( 
                     PlatformUI.getWorkbench()
                               .getActiveWorkbenchWindow()
                               .getShell() );
                 return dialog.open();
             }
 		};
 
 		refresh = new BrunnAction("Refresh", treeViewer) {
 		    
 		    private IStructuredSelection selection;
             
 		    @Override
             public void performWork( IProgressMonitor monitor ) {
 
 		        monitor.beginTask( "Refreshing", selection.size() ); 
 		        try {
 	                for ( Iterator iter = selection.iterator(); 
 	                      iter.hasNext() ; ) {
 	                    ITreeObject element = (ITreeObject) iter.next();
 	                    element.fireUpdate();
 	                    monitor.worked( 1 );
 	                }                
 		        }
 		        finally {
 		            monitor.done();
 		        }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 this.selection = selection;
                 return 0;
             }
 		};
 		
 		login = new Action("Login") {
 		    
 		    @Override
 		    public void run() {
 		        new LoginAction().run( null );
 		    }
 		    
         };
 		
 		markPlateTypeDeleted = new BrunnAction("Mark as Deleted", treeViewer) {
 		    
 		    private IStructuredSelection selection;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.PlateType plateType 
                         = (net.bioclipse.brunn.pojos.PlateType) 
                             element.getPOJO();
                     IPlateLayoutManager pm 
                         = (IPlateLayoutManager) 
                             Springcontact.getBean("plateLayoutManager");
                     plateType.delete();
                     pm.edit( Activator.getDefault().getCurrentUser(), 
                              plateType );
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 this.selection = selection; 
                 return Dialog.OK;
             }
 		};
 		
 		markPlateLayoutDeleted = new BrunnAction( "Mark as Deleted", 
 		                                          treeViewer ) {
 		    
 		    private IStructuredSelection selection;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.PlateLayout pojo 
                         = (net.bioclipse.brunn.pojos.PlateLayout) 
                             element.getPOJO();
                     IPlateLayoutManager pm 
                         = (IPlateLayoutManager) 
                             Springcontact.getBean("plateLayoutManager");
                     pojo.delete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		markPlateDeleted = new BrunnAction("Mark as Deleted", treeViewer) {
 		    
 		    private IStructuredSelection selection;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.Plate pojo 
                         = (net.bioclipse.brunn.pojos.Plate) element.getPOJO();
                     IPlateManager pm 
                         = (IPlateManager) 
                             Springcontact.getBean("plateManager");
                     pojo.delete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		markMasterPlateDeleted = new BrunnAction( "Mark as Deleted", 
 		                                          treeViewer ) {
 			private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.MasterPlate pojo 
                         = (net.bioclipse.brunn.pojos.MasterPlate) 
                             element.getPOJO();
                     IPlateManager pm 
                         = (IPlateManager) 
                             Springcontact.getBean("plateManager");
                     pojo.delete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		markCompoundDeleted = new BrunnAction("Mark as Deleted", treeViewer) {
 
 		    private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.DrugOrigin pojo 
                         = (net.bioclipse.brunn.pojos.DrugOrigin) 
                             element.getPOJO();
                     IOriginManager om 
                         = (IOriginManager) 
                             Springcontact.getBean("originManager");
                     pojo.delete();
                     om.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		markCellDeleted = new BrunnAction("Mark as Deleted", treeViewer) {
 				
 	        private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.CellOrigin pojo 
                         = (net.bioclipse.brunn.pojos.CellOrigin) 
                             element.getPOJO();
                     IOriginManager pm 
                         = (IOriginManager) 
                             Springcontact.getBean("originManager");
                     pojo.delete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		markFolderDeleted = new BrunnAction("Mark as Deleted", treeViewer) {
 
 		    private IStructuredSelection selection; 
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.Folder pojo 
                         = (net.bioclipse.brunn.pojos.Folder) 
                             element.getPOJO();
                     IFolderManager fm 
                         = (IFolderManager) 
                             Springcontact.getBean("folderManager");
                     pojo.delete();
                     fm.editMerging( Activator.getDefault().getCurrentUser(), 
                                     pojo );
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		markPatientCellDeleted = new BrunnAction( "Mark as Deleted", 
 		                                          treeViewer ) {
 			private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.PatientOrigin pojo 
                         = (net.bioclipse.brunn.pojos.PatientOrigin) 
                             element.getPOJO();
                     IOriginManager m 
                         = (IOriginManager) 
                             Springcontact.getBean("originManager");
                     pojo.delete();
                     m.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 	
 		unmarkPlateTypeDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                          treeViewer ) {
 				
 		    private IStructuredSelection selection;
 	
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.PlateType plateType 
                         = (net.bioclipse.brunn.pojos.PlateType) 
                             element.getPOJO();
                     IPlateLayoutManager pm 
                         = (IPlateLayoutManager) 
                             Springcontact.getBean("plateLayoutManager");
                     plateType.unDelete();
                     pm.edit( Activator.getDefault().getCurrentUser(), 
                              plateType );
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkPlateLayoutDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                            treeViewer ) {
 	
 			private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.PlateLayout pojo 
                         = (net.bioclipse.brunn.pojos.PlateLayout) 
                             element.getPOJO();
                     IPlateLayoutManager pm 
                         = (IPlateLayoutManager) 
                             Springcontact.getBean("plateLayoutManager");
                     pojo.unDelete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkPlateDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                      treeViewer ) {
 
 		    private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.Plate pojo 
                         = (net.bioclipse.brunn.pojos.Plate) 
                             element.getPOJO();
                     IPlateManager pm 
                         = (IPlateManager) 
                             Springcontact.getBean("plateManager");
                     pojo.unDelete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkMasterPlateDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                            treeViewer ) {
 	
 		    private IStructuredSelection selection;
 		    
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.MasterPlate pojo = (net.bioclipse.brunn.pojos.MasterPlate) element.getPOJO();
                     IPlateManager pm = (IPlateManager) Springcontact.getBean("plateManager");
                     pojo.unDelete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }            }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkCompoundDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                         treeViewer ) {
 			private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
 
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.DrugOrigin pojo 
                         = (net.bioclipse.brunn.pojos.DrugOrigin) 
                             element.getPOJO();
                     IOriginManager om 
                         = (IOriginManager) 
                             Springcontact.getBean("originManager");
                     pojo.unDelete();
                     om.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
 
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkCellDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                     treeViewer ) {
 			private IStructuredSelection selection;
 	
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.CellOrigin pojo 
                         = (net.bioclipse.brunn.pojos.CellOrigin) 
                             element.getPOJO();
                     IOriginManager pm 
                         = (IOriginManager) 
                             Springcontact.getBean("originManager");
                     pojo.unDelete();
                     pm.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }                
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkFolderDeleted = new BrunnAction( "Mark as Not Deleted", 
 		                                       treeViewer ) {
 	
 			private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.Folder pojo 
                         = (net.bioclipse.brunn.pojos.Folder) element.getPOJO();
                     IFolderManager fm 
                         = (IFolderManager) 
                             Springcontact.getBean("folderManager");
                     pojo.unDelete();
                     fm.editMerging( Activator.getDefault().getCurrentUser(), 
                                     pojo );
                     element.fireUpdate();
                 }                
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 		
 		unmarkPatientCellDeleted = new BrunnAction( "Mark as Not Deleted",
 		                                            treeViewer ) {
 	
 		    private IStructuredSelection selection;
 
             @Override
             public void performWork( IProgressMonitor monitor ) {
                 for (Iterator iter = selection.iterator(); iter.hasNext();) {
                     ITreeObject element = (ITreeObject) iter.next();
                     net.bioclipse.brunn.pojos.PatientOrigin pojo 
                         = (net.bioclipse.brunn.pojos.PatientOrigin) 
                             element.getPOJO();
                     IOriginManager m 
                         = (IOriginManager) 
                             Springcontact.getBean("originManager");
                     pojo.unDelete();
                     m.edit(Activator.getDefault().getCurrentUser(), pojo);
                     element.fireUpdate();
                 }
             }
 
             @Override
             public int popUpDialog( IStructuredSelection selection ) {
                 this.selection = selection;
                 return Dialog.OK;
             }
 		};
 	}
 
 	/**
 	 *  Creating the contextmenu for the treeviewer
 	 */
 	private void createContextMenu() {
 		// Create menu manager.
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager mgr) {
 				fillContextMenu(mgr);
 			}
 		});
 
 		// Create menu.
 		Menu menu = menuMgr.createContextMenu(treeViewer.getControl());
 		treeViewer.getControl().setMenu(menu);
 
 		// Register menu for extension.
 		getSite().registerContextMenu(menuMgr, treeViewer);
 	}
 
 	/**
 	 *  Populate the contextmenu for the treeviewer
 	 */
 	private void fillContextMenu(IMenuManager mgr) {
 
         Action[] initialElems = new Action[] {
              refresh,
              login,
              createPlateType,
              createPlateLayout,
              createMasterPlate,
              createCompound,
              createCellType,
              createFolder,
              rename,
              createMasterPlateFromSDF,
              createPlate,
              createPatientCell,
              markPlateTypeDeleted,
              markPlateLayoutDeleted,
              markPlateDeleted,
              markMasterPlateDeleted,
              markCompoundDeleted,
              markCellDeleted,            
              markFolderDeleted,
              markPatientCellDeleted,
              unmarkPlateTypeDeleted,
              unmarkPlateLayoutDeleted,
              unmarkPlateDeleted,
              unmarkMasterPlateDeleted,
              unmarkCompoundDeleted,
              unmarkCellDeleted,
              unmarkFolderDeleted,
              unmarkPatientCellDeleted,
         };
 
         List<Action> possibleActions = new ArrayList<Action>();
         for ( Action elem : initialElems ) {
             possibleActions.add(elem);
         }
 		if ( treeViewer.getSelection().isEmpty() ) {
             possibleActions.clear();
             if( !net.bioclipse.usermanager.Activator.getDefault()
                     .getUserManager().isLoggedIn() ) {
                 possibleActions.add( login );
             }
 		}
 		else {
             IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
 
             if (selection.size() > 1) {
                 possibleActions.clear();
             }
 
             for( Object element : selection.toList() ) {
 
                 ITreeObject treeObject = null;
 
                 if(element instanceof ITreeObject) {
 
                     treeObject = (ITreeObject) element;
 
                     // The following actions are only present for special
                     // types of folders
                     if( !(treeObject.getUniqueFolder() instanceof NotLoggedIn) ) {
                         possibleActions.remove( login );
                     }
                     if( !(treeObject.getUniqueFolder() instanceof DataSets) ) {
                         possibleActions.remove( createPlate );
                     }
                     if( !(treeObject.getUniqueFolder() instanceof PlateTypes) ) {
                         possibleActions.remove( createPlateType );
                     }
                     if( !(treeObject.getUniqueFolder() instanceof PlateLayouts) ) {
                         possibleActions.remove( createPlateLayout );
                     }
                     if( !(treeObject.getUniqueFolder() instanceof MasterPlates) ) {
                         possibleActions.remove( createMasterPlate );
                         possibleActions.remove( createMasterPlateFromSDF );
                     }               
                     if( !(treeObject.getUniqueFolder() instanceof Compounds) ) {
                         possibleActions.remove( createCompound );
                     }
                     if( !(treeObject.getUniqueFolder() instanceof CellTypes) ) {
                         possibleActions.remove( createCellType );
                     }
                     if( !(treeObject.getUniqueFolder() instanceof PatientCells) ) {
                     	possibleActions.remove( createPatientCell );
                     }
                     
                     // Special actions for special types of element
                     if( element instanceof Resources) {
                         possibleActions.remove( createFolder );
                     }
                     if( element instanceof AbstractUniqueFolder
                         || element instanceof Resources ) {
                         possibleActions.remove( rename );
                     }
                     if( element instanceof NotLoggedIn) {
                         possibleActions.clear();
                         possibleActions.add( login );
                     }
 
                     // Special actions for special data types
                     if( !(element instanceof PlateType) ) {
                         possibleActions.remove( markPlateTypeDeleted );
                         possibleActions.remove( unmarkPlateTypeDeleted );
                     }
                     if( !(element instanceof PlateLayout) ) {
                         possibleActions.remove( markPlateLayoutDeleted );
                         possibleActions.remove( unmarkPlateLayoutDeleted );
                     }
                     if( !(element instanceof Plate)) {
                         possibleActions.remove( markPlateDeleted );
                         possibleActions.remove( unmarkPlateDeleted );
                     }
                     if( !(element instanceof MasterPlate) ) {
                         possibleActions.remove( markMasterPlateDeleted );
                         possibleActions.remove( unmarkMasterPlateDeleted );
                     }
                     if( !(element instanceof Compound) ) {
                         possibleActions.remove( markCompoundDeleted );
                         possibleActions.remove( unmarkCompoundDeleted );
                     }
                     if( !(element instanceof CellType) ) {
                         possibleActions.remove( markCellDeleted );
                         possibleActions.remove( unmarkCellDeleted );
                     }
                     if( !(element instanceof net.bioclipse.brunn.ui.explorer.model.folders.Folder) ) {
                         possibleActions.remove( markFolderDeleted);
                         possibleActions.remove( unmarkFolderDeleted);
                     }
                     if( !(element instanceof PatientSample) ) {
                     	possibleActions.remove( markPatientCellDeleted );
                     	possibleActions.remove( unmarkPatientCellDeleted );
                     }
                     
                     User currentUser = Activator.getDefault().getCurrentUser();
                     
                     // Only one set -- markDeleted or unmarkDeleted -- is kept
                     Action[] markActions = new Action[] { markPlateTypeDeleted,
                                                           markPlateLayoutDeleted,
                                                           markPlateDeleted,
                                                           markMasterPlateDeleted,	
                                                           markCompoundDeleted,
                                                           markFolderDeleted,
                                                           markPatientCellDeleted,
                                                           markCellDeleted, };
                     
                     Action[] unmarkActions = new Action[] { unmarkPlateTypeDeleted,
                                                             unmarkPlateLayoutDeleted,
                                                             unmarkPlateDeleted,
                                                             unmarkMasterPlateDeleted,
                                                             unmarkCompoundDeleted,
                                                             unmarkFolderDeleted,
                                                             unmarkPatientCellDeleted,
                                                             unmarkCellDeleted, };
                     
                     if( treeObject.getPOJO() != null ) {
                     	if ( !currentUser.isAdmin() ) {
                     		for ( Action markAction : markActions ) {
                                 possibleActions.remove( markAction );
                             }
                     		for ( Action unmarkAction : unmarkActions ) {
                             	possibleActions.remove( unmarkAction );
                             }
                     	}
                     	
                         if ( treeObject.getPOJO().isDeleted() ) {
                             for ( Action markAction : markActions ) {
                                 possibleActions.remove( markAction );
                             }
                         }
                         else {
                             for ( Action unmarkAction : unmarkActions ) {
                             	possibleActions.remove( unmarkAction );
                             }
                         }
                     }
                 }
             }           
 		}
         
         for ( Action action : possibleActions ) {
             mgr.add(action);
             
             if (action == login) {
                 mgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));                
             }
         }
         mgr.add( new Separator(IWorkbenchActionConstants.MB_ADDITIONS) );
 	}
 
     public void receiveUserManagerEvent( UserManagerEvent event ) {
 		
 		switch (event) {
 		case LOGIN:
 			this.treeRoot.setLoggedIn();
 			Display.getDefault().asyncExec( new Runnable() {
 				public void run() {
 					createActions();
 					treeViewer.setContentProvider(new ExplorerContentProvider());
 					treeViewer.expandToLevel(2);
 				}
 			} );
 			break;
 		case LOGOUT:
 			this.treeRoot.setLoggedOut();
 			treeViewer.refresh();
 			break;
 		default:
 			break;
 		}
 		
 	}
 	
 	public boolean showPojosMarkedAsDeleted() {
 		return showPojosMarkedAsDeleted;
 	}
 	
 	public ITreeModelListener getTreeModelListener() {
 		return (ITreeModelListener) this.treeViewer.getContentProvider();
 	}
 
     public class CreateCompoundAction extends BrunnAction {
 
         private CreateCompound dialog;
         
         public CreateCompoundAction(TreeViewer treeViewer) {
             super("Create Compound", treeViewer);
         }
             
         @Override
         public void performWork( IProgressMonitor monitor ) {
 
             AbstractFolder recievingFolder 
                 = (AbstractFolder) selectedDomainObject.getFolder();
             IOriginManager orm 
                 = (IOriginManager) Springcontact.getBean("originManager");
 
             try {
                 orm.createDrugOrigin( Activator.getDefault()
                                                .getCurrentUser(), 
                                       dialog.getName(),
                                       dialog.getStructureURL()
                                             .equals("") 
                                             ? null
                                             : new FileInputStream(
                                                 dialog.getStructureURL() ),
                                       dialog.getMolecularWeight(),
                                       (net.bioclipse.brunn.pojos.Folder)
                                           recievingFolder.getPOJO() );
             } 
             catch (Exception e) {
                 throw new RuntimeException(
                     "The compound cold not be created. It might be that "
                     + "there already exists a compound with that name. " 
                     + "Error message: \"" + e.getMessage() + "\"", e );
             }
             recievingFolder.fireUpdate();
             if ( CreateCompound.isKeepAddingCompounds() ) {
                 final Action a = new CreateCompoundAction(treeViewer);
                 Display.getDefault().asyncExec( new Runnable() {
                     public void run() {
                         a.run();
                     }
                 } );
             }
         }
 
         @Override
         public int popUpDialog( IStructuredSelection selection ) {
             dialog 
                 = new CreateCompound( PlatformUI.getWorkbench()
                                                 .getActiveWorkbenchWindow()
                                                 .getShell() );
             return dialog.open();
         }
     }
 	
 }
