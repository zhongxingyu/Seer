 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.splash.ui.importWizards;
 
 import java.lang.reflect.InvocationTargetException;
 
 import org.amanzi.neo.core.database.nodes.SpreadsheetNode;
 import org.amanzi.splash.ui.SplashPlugin;
 import org.amanzi.splash.utilities.Messages;
 import org.amanzi.splash.utilities.NeoSplashUtil;
 import org.amanzi.splash.importer.CSVImporter;
 import org.amanzi.splash.importer.CSVImporter.CSVImportException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IImportWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.internal.ui.wizards.NewRubyElementCreationWizard;
 
 public class NeoDataFileImportWizard extends NewRubyElementCreationWizard implements IImportWizard {
 
 	NeoDataImportWizardPage mainPage;
 
 	public NeoDataFileImportWizard() {
 		super();
 	}
 	
 	/*
 	 * Importer for this Wizard 
 	 */
 	private CSVImporter importer;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
 	 */
 	public boolean performFinish() {
 	    
 	    importer = null;
 	    boolean toContinue = false;
 		try {		    
 		    //create importer and run it
 		    importer = new CSVImporter(mainPage.getContainerFullPath(), mainPage.getFileName(), mainPage.getInitialContents(), mainPage.getFileSize());
 	        getContainer().run(true, false, importer);
 		} catch (InterruptedException e) {
 			return false;
 		} catch (InvocationTargetException e) {		    
 			Throwable realException = e.getTargetException();
 			if (realException instanceof CSVImportException) {
 			    toContinue = true;
 			}
 			else {
 			    MessageDialog.openError(getShell(), Messages.Wizard_Error_Message, realException.getMessage());
 			    return false;
 			}			
 		}
 		catch (Exception e) {
 		    MessageDialog.openError(getShell(), Messages.Wizard_Error_Message, e.getMessage());
 		    return false;
 		}
 		finally {
 		    //finally open imported spreadsheet
 		    final SpreadsheetNode spreadsheetNode = importer.getSpreadsheet();
 		    
 		    if (toContinue) {
		        Job importJob = new Job(Messages.Import_Job){
                 
 		            @Override
 		            protected IStatus run(IProgressMonitor monitor) {
 		                try {
 		                    importer.run(monitor);
 		                }
 		                catch (InvocationTargetException e) {
 		                    return new Status(Status.ERROR, SplashPlugin.getId(), e.getTargetException().getMessage(), e.getTargetException());
 		                }
 		                catch (Exception e) {
 		                    return new Status(Status.ERROR, SplashPlugin.getId(), e.getMessage(), e);
 		                }
 		                return Status.OK_STATUS;
 		            }
 		        };
 		        importJob.schedule();
 		    }
 		    
 		    getShell().getDisplay().asyncExec(new Runnable() {
 		            
 		        @Override
 		        public void run() {
 		            NeoSplashUtil.openSpreadsheet(PlatformUI.getWorkbench(), spreadsheetNode);
 		        }
 		    });
 		}
 		
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
 	 */
 	public void init(IWorkbench workbench, IStructuredSelection selection) {
 	    super.init(workbench, selection);
 		setWindowTitle(Messages.File_Import_Wizard_Title); //NON-NLS-1
 		setNeedsProgressMonitor(true);
 		mainPage = new NeoDataImportWizardPage(Messages.CSV_Import_Title, selection); //NON-NLS-1
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.wizard.IWizard#addPages()
 	 */
 	public void addPages() {
 		super.addPages(); 
 		addPage(mainPage);        
 	}
 
     @Override
     protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
         //no need
     }
 
     @Override
     public IRubyElement getCreatedElement() {
         //no need
         return null;
     }
 
 }
