 package com.vectorsf.jvoice.core.project;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.model.Build;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.model.PluginExecution;
 import org.apache.maven.model.Repository;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.m2e.core.MavenPlugin;
 import org.eclipse.m2e.core.project.ProjectImportConfiguration;
 
 public final class JVoiceApplicationConfigurator {
 
 	private static final String WS_ENDPOINTS_XML = "ws-endpoints.xml";
	private static final String ENDPOINTS_DIR = "src/main/resources/com/vectorsf/";
 	private static final String WS = "/ws";
 	public static final String PROPERTIES_FOLDER = "src/main/resources/properties";
 	public static final String PROPERTIES_FILENAME = "jvoiceframework.properties";
 
 	private JVoiceApplicationConfigurator() {
 	}
 
 	public static void configureApplication(final IProject project) throws CoreException {
 		assert project.exists();
 
 		ResourcesPlugin.getWorkspace()
 				.run(new AddJVoiceNatureRunnable(project), project, IWorkspace.AVOID_UPDATE, null);
 
 	}
 
 	public static IProject createApplication(final String groupId, final String artifactId, final String projectName,
 			final String descProject, IProgressMonitor monitor) throws CoreException {
 		final IProject result[] = new IProject[1];
 		SubProgressMonitor sub1 = new SubProgressMonitor(monitor, 85);
 		IWorkspaceRunnable action = new IWorkspaceRunnable() {
 
 			@Override
 			public void run(IProgressMonitor monitor) throws CoreException {
 				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
 				ProjectImportConfiguration configuration = new ProjectImportConfiguration();
 				configuration.getResolverConfiguration().setResolveWorkspaceProjects(false);
 				String wsFolderName = ENDPOINTS_DIR + projectName + WS;
 				MavenPlugin.getProjectConfigurationManager().createSimpleProject(project, null,
 						getModel(groupId, artifactId, projectName, descProject),
 						new String[] { wsFolderName, PROPERTIES_FOLDER }, configuration, monitor);
 
 				// Creamos el ws-endpoints.xml en dentro de la carpeta resources
 				IFolder folder = project.getFolder(wsFolderName);
 				IFile wsEndpoints = folder.getFile(WS_ENDPOINTS_XML);
 				wsEndpoints.create(
 						getClass().getResourceAsStream("/com/vectorsf/jvoice/core/project/resources/ws-endpoints.xml"),
 						true, null);
 
 				IFolder propertiesFolder = project.getFolder(PROPERTIES_FOLDER);
 				IFile properties = propertiesFolder.getFile(PROPERTIES_FILENAME);
 				properties.create(
 						getClass().getResourceAsStream(
 								"/com/vectorsf/jvoice/core/project/resources/jvoiceframework.properties"), true, null);
 
 				result[0] = project;
 			}
 		};
 		ResourcesPlugin.getWorkspace().run(action, sub1);
 		IProject project = result[0];
 		SubProgressMonitor sub2 = new SubProgressMonitor(monitor, 15);
 		ResourcesPlugin.getWorkspace().run(new AddJVoiceNatureRunnable(project), sub2);
 		monitor.done();
 
 		return result[0];
 	}
 
 	private static Model getModel(String groupId, String artifactId, String projectName, String descProject) {
 		Model model = new Model();
 		model.setModelVersion("4.0.0"); //$NON-NLS-1$
 
 		model.setGroupId(groupId);
 		model.setArtifactId(artifactId);
 		model.setVersion("1.0.0-SNAPSHOT");
 		model.setPackaging("war");
 
 		model.setName(projectName);
 		model.setDescription(descProject);
 
 		List<Dependency> dependencies = new ArrayList<Dependency>();
 		Dependency jvFlow = new Dependency();
 		jvFlow.setGroupId("com.vectorsf");
 		jvFlow.setArtifactId("jvoiceframework-flow");
 		jvFlow.setVersion(AbstractJVoiceNature.JVOICE_FRAMEWORK_VERSION);
 		dependencies.add(jvFlow);
 		model.setDependencies(dependencies);
 
 		Plugin dsl_builder = new Plugin();
 		dsl_builder.setGroupId("com.vectorsf.jvoice");
 		dsl_builder.setArtifactId("application-builder");
 		dsl_builder.setVersion(AbstractJVoiceNature.JVOICE_COMPILER_VERSION);
 		PluginExecution voiceDSL = new PluginExecution();
 		voiceDSL.setPhase("generate-resources");
 		voiceDSL.addGoal("copyXML");
 
 		dsl_builder.addExecution(voiceDSL);
 
 		Plugin dsl_builder2 = new Plugin();
 		dsl_builder2.setGroupId("org.apache.maven.plugins");
 		dsl_builder2.setArtifactId("maven-compiler-plugin");
 		dsl_builder2.setVersion("2.3.2");
 
 		Xpp3Dom configuration = new Xpp3Dom("configuration");
 
 		Xpp3Dom source = new Xpp3Dom("source");
 		source.setValue("1.7");
 		configuration.addChild(source);
 
 		Xpp3Dom target = new Xpp3Dom("target");
 		target.setValue("1.7");
 		configuration.addChild(target);
 
 		Xpp3Dom encoding = new Xpp3Dom("encoding");
 		encoding.setValue("UTF-8");
 		configuration.addChild(encoding);
 
 		dsl_builder2.setConfiguration(configuration);
 
 		Build build = new Build();
 		build.addPlugin(dsl_builder);
 		build.addPlugin(dsl_builder2);
 		build.addPlugin(getMavenCleanPlugin());
 
 		model.setBuild(build);
 
 		Repository repository = new Repository();
 		repository.setId(AbstractJVoiceNature.JVOICE_REPOSITORY_ID);
 		repository.setName(AbstractJVoiceNature.JVOICE_REPOSITORY_NAME);
 		repository.setUrl(AbstractJVoiceNature.JVOICE_REPOSITORY_URL);
 
 		model.addPluginRepository(repository);
 		model.addRepository(repository);
 
 		return model;
 	}
 
 	public static Plugin getMavenCleanPlugin() {
 
 		Plugin cleanPlugin = new Plugin();
 		
 		cleanPlugin.setArtifactId("maven-clean-plugin");
 		cleanPlugin.setVersion("2.5");
 		
 		Xpp3Dom configuration = new Xpp3Dom("configuration");
 
 		Xpp3Dom filesets = new Xpp3Dom("filesets");
 		
 		Xpp3Dom filesetWebInf = new Xpp3Dom("fileset");
 		Xpp3Dom directoryWebInf = new Xpp3Dom("directory");
 		directoryWebInf.setValue("src/main/webapp/WEB-INF");
 		Xpp3Dom includesWebInf = new Xpp3Dom("includes");
 		
 		Xpp3Dom include1 = new Xpp3Dom("include");
 		include1.setValue("web.xml");
 		includesWebInf.addChild(include1);
 
 		Xpp3Dom include2 = new Xpp3Dom("include");
 		include2.setValue("flows/**/*");
 		includesWebInf.addChild(include2);
 
 		Xpp3Dom include3 = new Xpp3Dom("include");
 		include3.setValue("spring/app-context.xml");		
 		includesWebInf.addChild(include3);
 
 		Xpp3Dom include4 = new Xpp3Dom("include");
 		include4.setValue("spring/root-context.xml");		
 		includesWebInf.addChild(include4);
 
 		Xpp3Dom include5 = new Xpp3Dom("include");
 		include5.setValue("spring/jvoiceframework-context.xml");		
 		includesWebInf.addChild(include5);
 
 		Xpp3Dom include6 = new Xpp3Dom("include");
 		include6.setValue("spring/appServlet/servlet-context.xml");		
 		includesWebInf.addChild(include6);
 
 		Xpp3Dom include7 = new Xpp3Dom("include");
 		include7.setValue("views/**/*");		
 		includesWebInf.addChild(include7);
 
 		Xpp3Dom include8 = new Xpp3Dom("include");
 		include8.setValue("jVoiceArchFlows/**");		
 		includesWebInf.addChild(include8);
 
 		Xpp3Dom include9 = new Xpp3Dom("include");
 		include9.setValue("jVoiceArchFlows");		
 		includesWebInf.addChild(include9);
 
 		Xpp3Dom filesetWebRes = new Xpp3Dom("fileset");
 		Xpp3Dom directoryWebRes = new Xpp3Dom("directory");
 		directoryWebRes.setValue("src/main/webapp/resources");
 		Xpp3Dom includesWebRes = new Xpp3Dom("includes");
 
 		Xpp3Dom incWebRes1 = new Xpp3Dom("include");
 		incWebRes1.setValue("js/isban-logger.js");
 		includesWebRes.addChild(incWebRes1);
 
 		Xpp3Dom incWebRes2 = new Xpp3Dom("include");
 		incWebRes2.setValue("audios/**/*");
 		includesWebRes.addChild(incWebRes2);
 
 		Xpp3Dom incWebRes3 = new Xpp3Dom("include");
 		incWebRes3.setValue("grammars/**/*");
 		includesWebRes.addChild(incWebRes3);
 
 		Xpp3Dom filesetMainRes = new Xpp3Dom("fileset");
 		Xpp3Dom directoryMainRes = new Xpp3Dom("directory");
 		directoryMainRes.setValue("src/main/resources");
 		Xpp3Dom includesMainRes = new Xpp3Dom("includes");
 
 		Xpp3Dom incMainRes1 = new Xpp3Dom("include");
 		incMainRes1.setValue("logback.xml");
 		includesMainRes.addChild(incMainRes1);
 
 		filesetWebInf.addChild(directoryWebInf);
 		filesetWebInf.addChild(includesWebInf);
 
 		filesetWebRes.addChild(directoryWebRes);
 		filesetWebRes.addChild(includesWebRes);
 
 		filesetMainRes.addChild(directoryMainRes);
 		filesetMainRes.addChild(includesMainRes);
 
 		filesets.addChild(filesetWebInf);
 		filesets.addChild(filesetWebRes);
 		filesets.addChild(filesetMainRes);
 		configuration.addChild(filesets);
 
 		cleanPlugin.setConfiguration(configuration);
 		
 		return cleanPlugin;
 	}
 
 	private static final class AddJVoiceNatureRunnable implements IWorkspaceRunnable {
 		private final IProject project;
 
 		private AddJVoiceNatureRunnable(IProject project) {
 			this.project = project;
 		}
 
 		@Override
 		public void run(IProgressMonitor monitor) throws CoreException {
 			monitor.beginTask("Add project nature", 8);
 			IProjectDescription description = project.getDescription();
 			String[] natureIds = description.getNatureIds();
 			monitor.worked(1);
 
 			String[] newNatureIds = new String[natureIds.length + 2];
 			System.arraycopy(natureIds, 0, newNatureIds, 1, natureIds.length);
 			newNatureIds[0] = JVoiceModuleNature.NATURE_ID;
 			newNatureIds[newNatureIds.length - 1] = JVoiceApplicationNature.NATURE_ID;
 			monitor.worked(1);
 			description.setNatureIds(newNatureIds);
 			monitor.worked(1);
 
 			this.project.setDescription(description, null);
 			monitor.worked(5);
 			monitor.done();
 		}
 	}
 
 }
