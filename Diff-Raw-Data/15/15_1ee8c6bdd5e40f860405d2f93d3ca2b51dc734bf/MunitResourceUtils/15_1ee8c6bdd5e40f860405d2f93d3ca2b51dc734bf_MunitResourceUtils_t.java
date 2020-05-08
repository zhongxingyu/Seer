 package org.mule.tooling.ui.contribution.munit;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.ide.IDE;
 import org.mule.tooling.core.builder.TransformerUtils;
 import org.mule.tooling.core.model.IMuleProject;
 import org.mule.tooling.maven.MavenMuleProjectDecorator;
 import org.mule.tooling.maven.utils.MavenUtils;
 import org.mule.tooling.maven.utils.XmlEditingHelper;
 import org.mule.tooling.maven.utils.XmlEditionCallable;
 import org.mule.tooling.messageflow.editor.MessageFlowEditor;
 import org.mule.tooling.model.messageflow.Property;
 import org.mule.tooling.model.messageflow.PropertyCollection;
 import org.mule.tooling.model.messageflow.util.MuleConfigurationNamingSupport;
 import org.mule.tooling.ui.contribution.munit.classpath.MunitClassPathContainer;
 import org.mule.tooling.ui.contribution.munit.runtime.MunitLibrary;
 import org.mule.tooling.ui.contribution.munit.runtime.MunitRuntime;
 import org.mule.tooling.ui.contribution.munit.runtime.MunitRuntimeExtension;
 import org.mule.tooling.ui.modules.core.dsl.AbstractPipelineBuilder;
 import org.mule.tooling.ui.modules.core.dsl.ContainerBuilder;
 import org.mule.tooling.ui.modules.core.dsl.MuleConfigurationBuilder;
 
 /**
  * <p>
  * Utility class to handle Munit resources. Use this class to manipulate files, find resources or update class path
  * </p>
  */
 public class MunitResourceUtils {
 
     /**
      * <p>
      * Creates the munit folder in the {@link IMuleProject}. If the folder already exists then it does nothing
      * </p>
      * 
      * @param muleProject
      *            The {@link IMuleProject} where we want to create the folder
      * @return The Munit folder
      * @throws CoreException
      *             If the folder could not be created
      */
     public static IFolder createMunitFolder(IMuleProject muleProject) throws CoreException {
         IFolder folder = muleProject.getFolder(MunitPlugin.MUNIT_FOLDER_PATH);
         if (!folder.exists()) {
             folder.create(true, true, new NullProgressMonitor());
             return folder;
         }
 
         return folder;
     }
 
     /**
      * <p>
      * Creates a new test as a template for user
      * </p>
      * 
      * @param flowEditor
      *            The Flow editor where the test has to be shown
      * @param flowName
      *            The flow name that is being tested
      * @throws CoreException
      *             In case the test could not be created
      */
     public static void createDefaultMunitTest(MessageFlowEditor flowEditor, String flowName) throws CoreException {
         MuleConfigurationBuilder muleConfigurationBuilder = new MuleConfigurationBuilder(flowEditor.getMuleConfigurationDecorator());
         MuleConfigurationNamingSupport naming = new MuleConfigurationNamingSupport(flowEditor.getMuleConfiguration());
         PropertyCollection properties = new PropertyCollection();
         properties.addProperty(new Property("description", "Test"));
         properties.addProperty(new Property("name", naming.getAvailableName(flowName + "Test")));
         ContainerBuilder<MuleConfigurationBuilder> container = muleConfigurationBuilder.addContainer("munit_test", "http://www.mulesoft.org/schema/mule/munit/test");
         container.setProperties(properties);
         AbstractPipelineBuilder<ContainerBuilder<MuleConfigurationBuilder>> addFlowRef = container.editNested(1).addFlowRef("Flow-ref to " + flowName);
         PropertyCollection properties2 = new PropertyCollection();
         properties2.addProperty(new Property("name", flowName));
         addFlowRef.setProperties(properties2);
         TransformerUtils.updateConfigFileFromMFlow(flowEditor.getMuleProject(), flowEditor.getInputFile(), muleConfigurationBuilder.getConfig().getEntity());
     }
 
     /**
      * <p>
      * Sets the classpath for a project with Munit, if the project is maven based then it configures the pom
      * </p>
      * 
      * @param muleProject
      *            The project to be configured
      */
     public static void configureProjectForMunit(IMuleProject muleProject) {
         if (MavenUtils.isMavenBased(muleProject)) {
             MunitResourceUtils.configureMaven(muleProject);
         } else {
             MunitResourceUtils.configureProjectClasspath(muleProject);
         }
     }
 
     /**
      * <p>
      * Add the Munit maven configuratio to the mule pom
      * </p>
      * 
      * @param muleProject
      *            The project that has to be configured
      */
     public static void configureMaven(IMuleProject muleProject) {
         MavenMuleProjectDecorator mavenProject = MavenMuleProjectDecorator.decorate(muleProject);
         MunitRuntime munitRuntime = MunitRuntimeExtension.getInstance().getMunitRuntimeFor(muleProject);
         if (munitRuntime != null) {
             for (MunitLibrary library : munitRuntime.getLibraries()) {
                 if (library.hasMavenSupport()) {
                     mavenProject.addDependency(library.getMavenConfiguration().asPojoDependency());
                 }
             }
         } else {
             MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "No Munit Runtime", "No Munit runtime found for the current mule runtime");
             return;
         }
 
         mavenProject.addTestResource(MunitPlugin.MUNIT_FOLDER_PATH);
        mavenProject.addPlugin("org.mule.munit.tools", "munit-maven-plugin", munitRuntime.getMunitVersion(), new XmlEditionCallable() {
 
             @Override
             public boolean editXml(XmlEditingHelper xmlEditorHelper) {
                 if (!xmlEditorHelper.hasChild("executions")) {
                     xmlEditorHelper.createElement("executions").createElement("execution").createElement("id").setValue("test").createElement("phase").setValue("test")
                             .createElement("goals").createElement("goal").setValue("test");
 
                 }
                 return true;
             }
         });
     }
 
     /**
      * <p>
      * Adds the {@link MunitClassPathContainer} to the class path
      * </p>
      * 
      * @param muleProject
      *            The project to be configured
      */
     public static void configureProjectClasspath(IMuleProject muleProject) {
         try {
             IClasspathEntry[] rawClasspath = muleProject.getJavaProject().getRawClasspath();
             ArrayList<IClasspathEntry> newClassPath = new ArrayList<IClasspathEntry>(Arrays.asList(rawClasspath));
             newClassPath.add(JavaCore.newSourceEntry(muleProject.getMuleAppsFolder().getFullPath()));
             newClassPath.add(JavaCore.newContainerEntry(MunitClassPathContainer.CONTAINER_ID, true));
             IClasspathEntry[] newClasspathEntries = (IClasspathEntry[]) newClassPath.toArray(new IClasspathEntry[0]);
             muleProject.getJavaProject().setRawClasspath(newClasspathEntries, null);
 
         } catch (JavaModelException e) {
             MunitPlugin.log(e);
         }
     }
 
     /**
      * <p>
      * Opens de Munit editor for a particular file
      * </p>
      */
     public static IEditorPart open(IFile munitFile) {
         try {
             IWorkbenchPage page = MunitPlugin.getActivePage();
             if (page != null) {
                 return IDE.openEditor(page, munitFile, MunitPlugin.EDITOR_ID, true);
             }
         } catch (PartInitException e) {
         }
         return null;
     }
 
     /**
      * <p>
      * Shows the Munit runner view part
      * </p>
      */
     public static void openMunitRunner() {
         try {
             IWorkbenchPage activePage = MunitPlugin.getActivePage();
             if (activePage != null) {
                 activePage.showView(MunitPlugin.RUNNER_ID).setFocus();
             }
         } catch (WorkbenchException e1) {
         }
     }
 
     /**
      * <p>
      * Creates the Munit first test template file
      * </p>
      */
     public static IFile createXMLConfigurationFromTemplate(IMuleProject muleProject, String testFileName, String productionFileName, IFolder outputFolder) throws IOException,
             CoreException {
         IFile muleConfigFile = null;
         InputStream inputStream = MunitResourceUtils.class.getClassLoader().getResourceAsStream("templates/munit_config_template.xml");
         StringWriter writer = new StringWriter();
         Reader reader = new InputStreamReader(inputStream, "UTF-8");
         try {
 
             Velocity.init();
             VelocityContext context = new VelocityContext();
 
             final boolean isEnterprise = muleProject.getServerDefinition().isEnterpriseRuntime();
             String version = (isEnterprise ? "EE" : "CE") + "-" + muleProject.getServerDefinition().getVersion();
 
             context.put("version", version);
             context.put("resource", productionFileName);
 
             Velocity.evaluate(context, writer, "velocity  rendering", reader);
             ByteArrayInputStream source = new ByteArrayInputStream(writer.getBuffer().toString().getBytes());
 
             muleConfigFile = outputFolder.getFile(testFileName);
             if (muleConfigFile.exists()) {
                 muleConfigFile.setContents(source, IFile.KEEP_HISTORY, new NullProgressMonitor());
 
             } else {
                 muleConfigFile.create(source, true, null);
             }
             source.close();
 
         } catch (Exception e) {
             // NOTHING
         } finally {
             reader.close();
             inputStream.close();
             writer.close();
         }
         return muleConfigFile;
     }
 }
