 package com.runwaysdk.eclipse.plugin.wizards;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import org.apache.maven.cli.MavenCli;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.command.BasicCommandStack;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.wizard.Wizard;
 import org.eclipse.ui.INewWizard;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 import org.eclipse.ui.dialogs.IOverwriteQuery;
 import org.eclipse.ui.internal.ide.filesystem.FileSystemStructureProvider;
 import org.eclipse.ui.wizards.datatransfer.ImportOperation;
 
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.Messages;
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.RunwayCreationWizardPage;
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.RunwayDiagramEditorPlugin;
 import com.runwaysdk.eclipse.plugin.runway.diagram.part.RunwayDiagramEditorUtil;
 import com.runwaysdk.eclipse.plugin.runway.impl.DocumentRootImpl;
 import com.runwaysdk.eclipse.plugin.schema.importer.RunwayDOMParser;
 
 public class NewRunwayProjectWizard extends Wizard implements INewWizard
 {
   protected RunwayCreationWizardPage    diagramModelFilePage;
 
   protected NewRunwayProjectWizardPage1 page1;
 
   protected IStructuredSelection        selection;
 
   static final String                   schemaFileName              = "schema(0001352140861497)HelloWorld";
 
   private static final String           schemaFileNameWithExtension = schemaFileName + ".xml";
 
   private static final String           schemaPath                  = "/src/main/domain/application/"
                                                                         + schemaFileNameWithExtension;
 
   private static final String           runwayArchetypeVersion      = "0.0.2-SNAPSHOT";
 
   private static final String           runway_diagram_filename     = schemaFileName;
 
   private static final String           runway_filename             = runway_diagram_filename;
 
   private static final String           diagram_files_path          = "/src/main/domain/display/";
 
   static final String                   MAVEN_EMBEDDED              = "EMBEDDED";
 
   //private static final String           ARCHETYPE_SERVER            = "http://rowlands.dyndns.info:8080/nexus/content/groups/allrepos";
   private static final String           ARCHETYPE_SERVER            = "http://192.168.1.210:8080/nexus/content/groups/allrepos";
   
   public NewRunwayProjectWizard()
   {
     super();
     // setNeedsProgressMonitor(true);
   }
 
   @Override
   public void init(IWorkbench arg0, IStructuredSelection arg1)
   {
     this.selection = arg1;
   }
 
   @Override
   public void addPages()
   {
     page1 = new NewRunwayProjectWizardPage1();
     page1.setSelection(selection);
     addPage(page1);
   }
 
   public static void main(String[] args)
   {
     System.out.println( new MavenCli().doMain(new String[] { "archetype:generate", "-U", "-DarchetypeGroupId=com.runwaysdk",
         "-DarchetypeArtifactId=runwaysdk-archetype", "-DarchetypeVersion=0.0.2-SNAPSHOT",
         "-DgroupId=com.example", "-DartifactId=RunwayMavenTemplate", "-Dpackage=com.example",
         "-Dversion=0.0.1-SNAPSHOT", "-DinteractiveMode=false" },
 
     "/users/terraframe/documents/workspace/runway-sdk", System.out, System.out));
   }
   
   private boolean handleError(Exception e) {
     e.printStackTrace();
     MessageDialog dialog = new MessageDialog(this.getShell(), "An exception has occurred.", null,
         e.getLocalizedMessage(), MessageDialog.ERROR, new String[] { "First",
       "Second", "Third" }, 0);
     int result = dialog.open();
     
     return false;
   }
 
   @Override
   public boolean performFinish()
   {
    File file = new File(page1.getLocation() + "/" + page1.getArtifactId() + "/pom.xml");
    
    if (file.exists()) {
      MessageDialog dialog = new MessageDialog(this.getShell(), "Project already exists at location.", null,
          "A pom.xml already exists at " + file.getAbsolutePath(), MessageDialog.ERROR, new String[] { "Ok" }, 0);
      int result = dialog.open();
      return false;
    }
    
     /*
      * Generate the project with a mvn archetype:generate
      * 
      * This call uses the Maven API provided by M2E.
      */
     if (page1.getMavenLoc() == MAVEN_EMBEDDED)
     {
       try {
         int retVal = new MavenCli()
             .doMain(new String[] {
                 "archetype:generate",
                 "-U", // Force update
                 "-DarchetypeGroupId=com.runwaysdk", "-DarchetypeArtifactId=runwaysdk-archetype",
                 "-DarchetypeVersion=" + runwayArchetypeVersion, "-DgroupId=" + page1.getGroupId(),
                 "-DartifactId=" + page1.getArtifactId(), "-Dpackage=" + page1.getPkge(),
                 "-Dversion=" + page1.getVersion(), "-DinteractiveMode=false",
                 "-DarchetypeRepository=" + ARCHETYPE_SERVER }, page1.getLocation(), System.out, System.out);
         
         if (retVal != 0) {
           MessageDialog dialog = new MessageDialog(this.getShell(), "An error has occurred.", null,
              "An exception has occurred while generating the maven archetype. (Maven exited with status code " + retVal + ")", MessageDialog.ERROR, new String[] { "Ok" }, 0);
           int result = dialog.open();
          return false;
         }
       }
       catch (Exception e) {
         return handleError(e);
       }
     }
     else
     {
 
       /*
        * This code uses an external installation of mvn
        * 
        * InvocationRequest request = new DefaultInvocationRequest();
        * request.setBaseDirectory(new File(page1.getLocation()));
        * request.setGoals( Collections.singletonList( "archetype:generate" +
        * " -DarchetypeGroupId=com.runwaysdk" +
        * " -DarchetypeArtifactId=runwaysdk-archetype " + " -DarchetypeVersion="
        * + runwayArchetypeVersion + " -DgroupId=" + page1.getGroupId() +
        * " -DartifactId=" + page1.getArtifactId() + " -Dpackage=" +
        * page1.getPkge() + " -Dversion=" + page1.getVersion() ) );
        * 
        * Invoker invoker = new DefaultInvoker(); invoker.setMavenHome(new
        * File(page1.getMavenLoc() + "/../../")); try { invoker.execute( request
        * ); } catch (MavenInvocationException e) { e.printStackTrace(); }
        */
       try {
         throw new UnsupportedOperationException("");
       }
       catch (Exception e) {
         return handleError(e);
       }
     }
 
     String projectDir = page1.getLocation() + "/" + page1.getArtifactId();
 
     /**
      * Import the generated project into Eclipse.
      */
     IOverwriteQuery overwriteQuery = new IOverwriteQuery()
     {
       public String queryOverwrite(String file)
       {
         return ALL;
       }
     };
 
     ImportOperation importOperation = new ImportOperation(new Path(page1.getArtifactId()), new File(
         projectDir), new FileSystemStructureProvider(), overwriteQuery);
     importOperation.setCreateContainerStructure(false);
     try
     {
       importOperation.run(new NullProgressMonitor());
     }
     catch (InvocationTargetException e)
     {
       return handleError(e);
     }
     catch (InterruptedException e)
     {
       return handleError(e);
     }
 
     // This code creates a .project file (and fails if one already exists)
     // try
     // {
     // IProjectDescription description;
     // description = ResourcesPlugin.getWorkspace().loadProjectDescription(new
     // Path(page1.getLocation() + "/" + page1.getArtifactId() + "/.project"));
     // IProject project =
     // ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
     // project.create(description, null);
     // project.open(null);
     // }
     // catch (CoreException e)
     // {
     // e.printStackTrace();
     // }
 
     // Attempted M2E Import integration
     // final Collection<MavenProjectInfo> projects = new
     // ArrayList<MavenProjectInfo>();
     // projects.add(new MavenProjectInfo(page1.getArtifactId(), new File(baseDir
     // + "/pom.xml"), new Model(), null));
     // try
     // {
     // List<IMavenProjectImportResult> results =
     // MavenPlugin.getProjectConfigurationManager().importProjects(
     // projects, new ProjectImportConfiguration(), new NullProgressMonitor());
     // }
     // catch (CoreException e)
     // {
     // e.printStackTrace();
     // }
 
     // final IRunnableWithProgress importOperation = new IRunnableWithProgress()
     // {
     // @Override
     // public void run(IProgressMonitor arg0) throws InvocationTargetException,
     // InterruptedException
     // {
     // SubMonitor monitor = SubMonitor.convert(arg0, 101);
     // try {
     // List<IMavenProjectImportResult> results =
     // MavenPlugin.getProjectConfigurationManager().importProjects(
     // projects, new ProjectImportConfiguration(), monitor.newChild(100));
     // }
     // catch (CoreException e)
     // {
     // e.printStackTrace();
     // } finally {
     // monitor.done();
     // }
     // }
     // };
 
     /*
      * Create a new diagram file in the project and open the diagram file.
      */
     final URI diagramModel = URI.createURI("platform:/resource/" + page1.getArtifactId()
         + diagram_files_path + runway_diagram_filename + ".runway_diagram");
     final URI domainModel = URI.createURI("platform:/resource/" + page1.getArtifactId()
         + diagram_files_path + runway_filename + ".runway");
 
     IRunnableWithProgress op = new WorkspaceModifyOperation(null)
     {
 
       protected void execute(IProgressMonitor monitor) throws CoreException, InterruptedException
       {
         Resource diagram = RunwayDiagramEditorUtil.createDiagram(diagramModel, domainModel, monitor);
 
         if (diagram != null)
         {
           try
           {
             RunwayDiagramEditorUtil.openDiagram(diagram);
           }
           catch (PartInitException e)
           {
             ErrorDialog.openError(getContainer().getShell(),
                 Messages.RunwayCreationWizardOpenEditorError, null, e.getStatus());
           }
         }
         else
         {
           ErrorDialog.openError(getContainer().getShell(), "Error creating Runway Diagram.", null, null);
         }
       }
     };
     try
     {
       getContainer().run(false, true, op);
     }
     catch (InterruptedException e)
     {
       return handleError(e);
     }
     catch (InvocationTargetException e)
     {
       if (e.getTargetException() instanceof CoreException)
       {
         ErrorDialog.openError(getContainer().getShell(), Messages.RunwayCreationWizardCreationError,
             null, ( (CoreException) e.getTargetException() ).getStatus());
       }
       else
       {
         RunwayDiagramEditorPlugin.getInstance().logError(
             "Error creating diagram", e.getTargetException()); //$NON-NLS-1$
       }
       return false;
     }
     catch (Exception e) {
       return handleError(e);
     }
 
     /*
      * Import the HelloWorld schema.
      */
     AdapterFactory adapterFactory = RunwayDiagramEditorPlugin.getInstance()
         .getItemProvidersAdapterFactory();
     AdapterFactoryEditingDomain editer = new AdapterFactoryEditingDomain(adapterFactory,
         new BasicCommandStack());
     Resource resource = editer.createResource(domainModel.toPlatformString(true));
     try
     {
       // Load the file
       resource.load(null);
 
       // Retrieve the editing domain on the DocumentRoot
       EList<EObject> contents = resource.getContents();
       DocumentRootImpl documentRoot = (DocumentRootImpl) contents.get(0); // (DocumentRootImpl)
                                                                           // diagram.getElement();
       EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(documentRoot);
 
       // Parse the schema and add it to GMF
       RunwayDOMParser parser = new RunwayDOMParser(editingDomain, documentRoot);
       parser.parse(projectDir + schemaPath);
 
       resource.save(null);
 
       resource.unload();
     }
     catch (Exception e)
     {
       return handleError(e);
     }
 
     return true;
   }
 }
