 package com.runwaysdk.eclipse.plugin.wizards;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Random;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.preference.StringFieldEditor;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.dialogs.ElementListSelectionDialog;
 
 import com.runwaysdk.eclipse.plugin.schema.SchemaUtil;
 
 public class SchemaExportWizardPage1 extends WizardPage
 {
   private Composite container;
   private StringFieldEditor schemaNameField;
   private StringFieldEditor projectNameField;
   protected IFile diagramFile = null;
   protected String schemaName = "";
   protected IStructuredSelection selection;
   
   public SchemaExportWizardPage1() {
     super("Runway Schema Export Wizard");
     setTitle("Runway Schema Export Wizard");
     setDescription("Exports a Runway schema from the UML diagram into Runway XML.");
   }
   
   @Override
   public void createControl(Composite parent) {
     container = new Composite(parent, SWT.NULL);
     
     diagramFile = promptDiagramFile(null);
 
     URL url = Platform.getInstanceLocation().getURL();
     String workspace = new File(url.getPath()).getAbsolutePath();
     final String tempFolderStr = workspace + "/.metadata/.plugins/com.runwaysdk.eclipse.plugin/" + diagramFile.getProject().getName() + "/" + diagramFile.getName().replace(".runway_diagram", "");
     schemaName = calcSchemaName(diagramFile.getName(), tempFolderStr);
     
     schemaNameField = new StringFieldEditor("SchemaName", "Export Schema File Name", container); 
     schemaNameField.setEmptyStringAllowed(false);
     schemaNameField.setStringValue(schemaName);
     schemaNameField.setPropertyChangeListener(new IPropertyChangeListener() {
       
       @Override
       public void propertyChange(PropertyChangeEvent arg0)
       {
         if (arg0.getNewValue() instanceof String) {
           schemaName = (String) arg0.getNewValue();
           System.out.println("SchemaName = '" + schemaName + "'");
         }
       }
       
     });
     
     setControl(container);
     
     setPageComplete(true);
   }
   
   public static void main(String[] args)
   {
     String filename = "schema(000419239123)HelloWorld.runway_diagram";
     String schemaTempLoc = "/Users/terraframe/Documents/runtime-Runway_runtime_configuration/.metadata/.plugins/com.runwaysdk.eclipse.plugin/make-whatever/schema(0001352140861497)HelloWorld";
     
     String schemaName = calcSchemaName(filename, schemaTempLoc);
     
     System.out.println(schemaName);
   }
   
   public static String calcSchemaName(String diagramFilename, String schemaTempLoc) {
     String diagramNameContent = diagramFilename.replace(".runway_diagram", "").replaceAll("schema\\([0-9]+\\)", "");
     
    // TODO: Use Runway to generate this.
     String schemaNumber = String.format("%016d", new Random().nextInt(1000000000));
     
     return "schema(" + schemaNumber + ")" + diagramNameContent + "_changedSomething.xml";
   }
   
   /**
    * This complicated function opens a series of dialogs that eventually leads the user to select a runway_diagram file.
    */
   public IFile promptDiagramFile(IProject proj) {
     Object selecEl = selection.getFirstElement();
     if (selecEl != null && selecEl instanceof IFile)
     {
       IFile file = (IFile) selecEl;
       
       if (file.getFileExtension().equals("runway_diagram")) {
         return file;
       }
     }
     if (proj == null && selecEl != null && selecEl instanceof IResource) {
       proj = ((IResource) selecEl).getProject();
       
       // Don't use the project if it isn't a Runway project (This code prevents an infinite dialog spam loop)
       IFolder folder = proj.getFolder("src/main/domain/display");
       
       if (folder == null || !folder.exists()) {
         proj = null;
       }
     }
     
     if (proj == null) {
       ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
       dialog.setTitle("Project Selection");
       dialog.setMessage("Select the project:");
       
       IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
       dialog.setElements(projects);
       
       dialog.open();
       
       Object result = dialog.getResult()[0];
       
       return promptDiagramFile((IProject) result);
     }
     else {
       IFolder folder = proj.getFolder("src/main/domain/display");
       
       if (folder == null || !folder.exists()) {
         SchemaUtil.handleError(getShell(), "Unable to find the folder 'src/main/domain/display' in the project that you selected. The project must be a Runway project.");
         return promptDiagramFile(null);
       }
       
       ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider());
       dialog.setTitle("Diagram File Selection");
       dialog.setMessage("Select the diagram file to export:");
       
       LinkedList<IResource> resources = null;
       try
       {
         resources = new LinkedList<IResource>(Arrays.asList(folder.members()));
       }
       catch (CoreException e)
       {
         SchemaUtil.handleError(getShell(), e);
       }
       Iterator<IResource> it = resources.iterator();
       while (it.hasNext()) {
         IResource res = (IResource) it.next();
         if (res instanceof IFile) {
           IFile file = (IFile) res;
           
           if (!file.getFileExtension().equals("runway_diagram")) {
             it.remove();
           }
         }
         else {
           it.remove();
         }
       }
       dialog.setElements(resources.toArray());
       
       dialog.open();
       
       return (IFile) dialog.getResult()[0];
     }
   }
 
   public IFile getDiagramFile() {
     return diagramFile;
   }
   
   public String getSchemaName() {
     return schemaName;
   }
   
   public IStructuredSelection getSelection()
   {
     return selection;
   }
 
   public void setSelection(IStructuredSelection selection)
   {
     this.selection = selection;
   }
 }
