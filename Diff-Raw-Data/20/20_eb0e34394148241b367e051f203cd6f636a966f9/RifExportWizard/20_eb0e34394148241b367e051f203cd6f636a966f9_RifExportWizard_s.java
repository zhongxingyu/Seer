 package era.foss.erf.exporter;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.IExportWizard;
 import org.eclipse.ui.IWorkbench;
 
 public class RifExportWizard extends Wizard implements IExportWizard {
 
     // List of files to process
     private RifExportWizardMainPage mainPage;
     private IStructuredSelection selection;
 
     @Override
     public void init( IWorkbench workbench, IStructuredSelection selection ) {
         this.selection = selection;
     }
 
     /**
      * @param selection
      */
    @SuppressWarnings("unchecked")
     private List<IFile> getWhiteCheckedInputResources( IStructuredSelection selection ) {
         List<IFile> erfFileList = new ArrayList<IFile>();
        Iterator fileIter = selection.iterator();
         while (fileIter.hasNext()) {
             // check if selected element is a file
             Object file = fileIter.next();
             if( file instanceof IFile ) {
                 try {
                     if( ((IFile)file).getContentDescription()
                                      .getContentType()
                                      .getId()
                                      .equals( "era.foss.erf.ErfContentType" ) ) {
                         erfFileList.add( (IFile)file );
                     }
                 } catch( CoreException e ) {
                 }
             }
         }
         return erfFileList;
     }
 
     /*
      * (non-Javadoc) Method declared on IWizard.
      */
     public void addPages() {
         super.addPages();
         mainPage = new RifExportWizardMainPage( "Rif", getWhiteCheckedInputResources( selection ) );
         addPage( mainPage );
     }
 
     @Override
     public boolean performFinish() {
         try {
             // delegate to main page
             return mainPage.finish();
         } catch( IOException e ) {
             e.printStackTrace();
             return false;
         }
     }
 
 }
