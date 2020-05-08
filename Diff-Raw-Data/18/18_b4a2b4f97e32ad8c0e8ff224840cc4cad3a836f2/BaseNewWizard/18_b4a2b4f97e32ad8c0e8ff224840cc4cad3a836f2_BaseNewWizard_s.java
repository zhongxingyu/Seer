 package am.lodge.code.scaffold.wizards;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.DatabaseMetaData;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 
 import am.lodge.code.scaffold.Activator;
 import am.lodge.code.scaffold.stirrer.CodeStirrer;
 import am.lodge.code.scaffold.util.Constants;
 import am.lodge.code.scaffold.util.DatabaseUtils;
 
 public abstract class BaseNewWizard extends Wizard implements INewWizard{
 
   private IStructuredSelection selection;
 
   private DatabaseMetaData databaseMetaData;
 
   private String separator = System.getProperties().getProperty("file.separator");
 
   public BaseNewWizard(){
     databaseMetaData = DatabaseUtils.getDatabaseMetaData(Activator.getDefault().getPreferenceStore());
     setNeedsProgressMonitor(true);
   }
 
   @Override
   public void addPages(){
     if(selection.getFirstElement() == null){
       MessageDialog.openError(getShell(), "Error", "select a project please");
       return;
     }
     initWizardPages();
   }
 
   protected abstract void initWizardPages();
 
   protected void doFinish(Map<String, Object> data){
     IProject project = null;
     if(selection.getFirstElement() instanceof IResource){
       IResource resource = (IResource) selection.getFirstElement();
       project = resource.getProject();
     }else if(selection.getFirstElement() instanceof IJavaElement){
       IJavaElement javaElement = (IJavaElement) selection.getFirstElement();
       project = javaElement.getResource().getProject();
     }
     String soundCodePath = Activator.getDefault().getPreferenceStore().getString("soundCodePath");
     IContainer outRoot = project.getFolder(new Path(soundCodePath));
     String templatePath = Activator.getDefault().getPreferenceStore().getString("templatePath");
    String sourchPath = templatePath + separator + Constants.SOURCE;
    CodeStirrer stirrer = new CodeStirrer(data, new File(sourchPath), outRoot, (String)data.get(Constants.PACKAGE), getShell());
    stirrer.create();

    String pagePath = Activator.getDefault().getPreferenceStore().getString("pagePath");
    outRoot = project.getFolder(new Path(pagePath));
    sourchPath = templatePath + separator + Constants.PAGE;
 
     String parameters = templatePath + separator + Constants.CUSTOM;
     Properties pros = new Properties();
     try {
       pros.load(new FileInputStream(parameters));
       Set<Entry<Object, Object>> set = pros.entrySet();
       for(Entry<Object, Object> e : set){
         data.put((String)e.getKey(), e.getValue());
       }
     } catch (IOException e) {
     }
     stirrer = new CodeStirrer(data, new File(sourchPath), outRoot, (String)data.get(Constants.PAGE_PATH), getShell());
     stirrer.create();
   }
 
   @Override
   public void init(IWorkbench workbench, IStructuredSelection selection){
     this.selection = selection;
   }
 
   @Override
   public void dispose(){
     if(databaseMetaData != null)
       DatabaseUtils.close(databaseMetaData);
     super.dispose();
   }
 
   public DatabaseMetaData getDatabaseMetaData(){
     return databaseMetaData;
   }
 }
