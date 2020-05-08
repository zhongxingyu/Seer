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
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.ide.IDE;
 import org.mule.tooling.core.builder.TransformerUtils;
 import org.mule.tooling.core.model.IMuleProject;
 import org.mule.tooling.maven.MavenMuleProjectDecorator;
 import org.mule.tooling.maven.dependency.MavenDependency.Scope;
 import org.mule.tooling.maven.dependency.PojoMavenDependency;
 import org.mule.tooling.maven.utils.MavenUtils;
 import org.mule.tooling.maven.utils.XmlEditingHelper;
 import org.mule.tooling.maven.utils.XmlEditionCallable;
 import org.mule.tooling.messageflow.editor.MessageFlowEditor;
 import org.mule.tooling.model.messageflow.Property;
 import org.mule.tooling.model.messageflow.PropertyCollection;
 import org.mule.tooling.model.messageflow.util.MuleConfigurationNamingSupport;
 import org.mule.tooling.ui.contribution.munit.actions.MunitClassPathContainer;
 import org.mule.tooling.ui.modules.core.dsl.AbstractPipelineBuilder;
 import org.mule.tooling.ui.modules.core.dsl.ContainerBuilder;
 import org.mule.tooling.ui.modules.core.dsl.MuleConfigurationBuilder;
 import org.osgi.framework.Bundle;
 
 
 public class MunitResourceUtils {
 
     public static IFolder createMunitFolder(IMuleProject muleProject) throws CoreException {
         IFolder folder = muleProject.getFolder(MunitPlugin.MUNIT_FOLDER_PATH);
         if ( !folder.exists() )
         {
 
             folder.create(true, true, new NullProgressMonitor());
             return folder;
         }
 
         return folder;
     }
 
     public static void configureMaven(IMuleProject muleProject) {
         MavenMuleProjectDecorator mavenProject = MavenMuleProjectDecorator.decorate(muleProject);
        String munitVersion = null;
         IConfigurationElement[] configurationElementsFor = Platform.getExtensionRegistry().getConfigurationElementsFor("org.mule.tooling.ui.contribution.munit.munitRuntime");
         for (IConfigurationElement configElement : configurationElementsFor) {
             if ( Arrays.asList(configElement.getAttribute("muleVersion").split(",")).contains(muleProject.getRuntimeId()) ){
                 munitVersion = configElement.getAttribute("munitVersion");
                 for ( IConfigurationElement library : configElement.getChildren() ){
                     for ( IConfigurationElement mavenConfig : library.getChildren()){
                         mavenProject.addDependency(new PojoMavenDependency(mavenConfig.getAttribute("groupId"), 
                                 mavenConfig.getAttribute("artifactId"), mavenConfig.getAttribute("version"), Scope.TEST));
                     }
                 }
 
                 break;
             }
         }
        
        if ( munitVersion == null ){
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "No Munit Runtime", "No Munit runtime found for the current mule runtime"); 
            return;
        }
 
         mavenProject.addTestResource(MunitPlugin.MUNIT_FOLDER_PATH);
         mavenProject.addPlugin("org.mule.munit", "munit-maven-plugin", munitVersion, new  XmlEditionCallable() {
 
             @Override
             public boolean editXml(XmlEditingHelper xmlEditorHelper) {
                 if ( !xmlEditorHelper.hasChild("executions")){
                     xmlEditorHelper.createElement("executions")
                     .createElement("execution")
                     .createElement("id").setValue("test")
                     .createElement("phase").setValue("test")
                     .createElement("goals")
                     .createElement("goal").setValue("test");
 
                 }
                 return true;
             }
         } );
     }
 
 
     public static void createDefaultMunitTest(MessageFlowEditor flowEditor, String flowName) throws CoreException {
         MuleConfigurationBuilder muleConfigurationBuilder = new MuleConfigurationBuilder( flowEditor.getMuleConfigurationDecorator());
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
         TransformerUtils.updateConfigFileFromMFlow(flowEditor.getMuleProject(), flowEditor.getInputFile(),  muleConfigurationBuilder.getConfig().getEntity());
     }
 
 
     public static void configreProjectForMunit(IMuleProject muleProject)
     {
         if ( MavenUtils.isMavenBased(muleProject)) {
             MunitResourceUtils.configureMaven(muleProject);
         }
         else{
             MunitResourceUtils.configureProjectClasspath(muleProject);                
         }
 
     }
     public static void configureProjectClasspath(IMuleProject muleProject) {
         try {
             IClasspathEntry[] rawClasspath = muleProject.getJavaProject().getRawClasspath();
             ArrayList<IClasspathEntry> newClassPath = new ArrayList<IClasspathEntry>(Arrays.asList(rawClasspath));
             newClassPath.add(JavaCore.newSourceEntry(muleProject.getMuleAppsFolder().getFullPath()));
             newClassPath.add(JavaCore.newContainerEntry(MunitClassPathContainer.CONTAINER_ID, true));
             IClasspathEntry[] newClasspathEntries = (IClasspathEntry[])newClassPath.toArray(new IClasspathEntry[0]);
             muleProject.getJavaProject().setRawClasspath(newClasspathEntries,null);
 
         } catch (JavaModelException e) {
             e.printStackTrace();
         }
     }
 
 
     public static IEditorPart open(IFile munitFile) {
         IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 
         if (activeWorkbenchWindow == null) {
             return null;
         }
 
         try {
             IWorkbenchPage page =
                     activeWorkbenchWindow.getActivePage();
             return IDE.openEditor(page, munitFile, "org.mule.tooling.ui.contribution.munit.editors.MunitMultiPageEditor", true);
         } catch (PartInitException e) {
         }
         return null;
     }
 
     public static void openMunitRunner(){
         final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
         if (activeWorkbenchWindow == null) {
             return;
         }
         try {
             PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
             .showView("org.eclipse.jdt.munit.ResultView").setFocus();
 
         } catch (WorkbenchException e1) {
         }
     }
 
     public static IFile createXMLConfigurationFromTemplate(IMuleProject muleProject, String testFileName,String productionFileName,  IFolder outputFolder) throws IOException, CoreException {
         IFile muleConfigFile=null;
         InputStream inputStream = MunitResourceUtils.class.getClassLoader().getResourceAsStream("templates/munit_config_template.xml");
         StringWriter writer = new StringWriter();
         Reader reader = new InputStreamReader(inputStream, "UTF-8");
         try{
 
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
         }
         finally{
             reader.close();
             inputStream.close();
             writer.close();
         }
         return muleConfigFile;
     }
 }
