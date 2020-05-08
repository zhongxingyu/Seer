 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.project;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.PropertyException;
 
 import org.zend.sdklib.application.ZendProject.TemplateApplications;
 import org.zend.sdklib.descriptor.PackageDescription;
 import org.zend.sdklib.descriptor.pkg.Package;
 import org.zend.sdklib.internal.library.AbstractChangeNotifier;
 import org.zend.sdklib.internal.utils.JaxbHelper;
 import org.zend.sdklib.mapping.IMappingEntry.Type;
 import org.zend.sdklib.mapping.IMappingLoader;
 import org.zend.sdklib.mapping.IMappingModel;
 import org.zend.sdklib.mapping.MappingModelFactory;
 import org.zend.sdklib.project.DeploymentScriptTypes;
 import org.zend.webapi.core.progress.BasicStatus;
 import org.zend.webapi.core.progress.IChangeNotifier;
 import org.zend.webapi.core.progress.StatusCode;
 
 /**
  * Project creation and update handling including descriptor, scripts and
  * application resources
  */
 public class ProjectResourcesWriter extends AbstractChangeNotifier {
 
 	private static final String DEFAULT_DOCROOT = "public";
 
 	private static final String DEFAULT_APP_DIR = "data";
 
 	public static final String DESCRIPTOR = "deployment.xml";
 
 	// properties of the subject project
 	private final String name;
 
 	/**
 	 * @param name
 	 *            of the application
 	 * @param path
 	 *            project root
 	 * @param withScripts2
 	 *            true if scripts are added to the project
 	 * @param WelcomePgae
 	 * @param isZend
 	 */
 	public ProjectResourcesWriter(String name) {
 		super();
 		this.name = name;
 	}
 
 	public ProjectResourcesWriter(String name, IChangeNotifier notifier) {
 		super(notifier);
 		this.name = name;
 	}
 
 	public ProjectResourcesWriter(File projectPath) {
 		this(getProjectName(new File(projectPath, DESCRIPTOR)));
 	}
 
 	public ProjectResourcesWriter(File projectPath, IChangeNotifier notifier) {
 		this(getProjectName(new File(projectPath, DESCRIPTOR)), notifier);
 	}
 
 	/**
 	 * Writing descriptor file to the root project
 	 * 
 	 * @param id
 	 *            - name of the project
 	 * @param withContent
 	 *            - whether to write other contents than scripts and descriptor
 	 * @param withScripts
 	 *            - whether to write scripts
 	 * @param destination
 	 *            - destination directory
 	 * @throws IOException
 	 * @throws JAXBException
 	 * @throws PropertyException
 	 */
 	public void writeDescriptor(File destination) throws IOException,
 			PropertyException, JAXBException {
 		notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
 				"Application Update", "Creating deployment descriptor...", -1));
 		File descrFile = new File(destination, DESCRIPTOR);
 
 		if (!descrFile.exists()) {
 			final boolean n = descrFile.createNewFile();
 			if (!n) {
 				throw new IOException("Error creating file "
 						+ destination.getAbsolutePath());
 			}
 			writeDescriptor(new FileOutputStream(descrFile));
 		}
 		updateDescriptor(descrFile);
 		notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
 				"Application Update", "Creating deployment descriptor..."));
 	}
 
 	/**
 	 * Writing project descriptor to a given output stream
 	 * 
 	 * @param outputStream
 	 * @throws IOException
 	 * @throws PropertyException
 	 * @throws JAXBException
 	 */
 	public void writeDescriptor(OutputStream outputStream) throws IOException,
 			PropertyException, JAXBException {
 		if (name == null) {
 			throw new IllegalArgumentException(
 					"Failed to create deployment descriptor. Project name is missing");
 		}
 		DescriptorWriter w = new DescriptorWriter(xmlEscape(name), DEFAULT_APP_DIR,
 				null, "1.0.0");
 		w.write(outputStream);
 		outputStream.close();
 	}
 
 	/**
 	 * Writes scripts under destination with a given list of scripts (all or
 	 * nothing are
 	 * 
 	 * @param path
 	 * @param withScripts
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public void writeScriptsByName(File container, String withScripts)
 			throws IOException, JAXBException {
 		if (withScripts == null) {
 			return;
 		}
 		notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
 				"Application Update", "Creating deployment scripts...", -1));
 		File destination = getScriptsDirectory(container);
 		if (destination != null && !destination.isDirectory()) {
 			destination.mkdirs();
 		}
 
 		final ScriptsWriter w = new ScriptsWriter();
 		if ("all".equals(withScripts)) {
 			w.writeAllScripts(destination);
 			return;
 		}
 
 		final DeploymentScriptTypes n = DeploymentScriptTypes
 				.byName(withScripts);
 		if (n != null) {
 			w.writeSpecificScript(destination, n);
 		} else {
 			throw new IllegalArgumentException(MessageFormat.format(
 					"script with name {0} cannot be found", withScripts));
 		}
 		notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
 				"Application Update", "Creating deployment scripts..."));
 	}
 
 	public void writeDeploymentProperties(File container, IMappingLoader loader, boolean isUpdate)
 			throws IOException, JAXBException {
 		File exisitngMapping = new File(container,
 				MappingModelFactory.DEPLOYMENT_PROPERTIES);
		if (exisitngMapping.exists()) {
 			return;
 		}
		IMappingModel model = loader == null ? MappingModelFactory.createDefaultModel(container)
				: MappingModelFactory.createModel(loader, container);
		notifier.statusChanged(new BasicStatus(StatusCode.STARTING, "Application Update",
				"Creating default deployment.properites file...", -1));
 		if (container.isDirectory()) {
 			String scriptdir = getScriptsDirectory(container).getName();
 			File[] files = container.listFiles();
 			for (File file : files) {
 				String name = file.getName();
 				if (!model.isExcluded(null, name) && !shoudBeExcluded(name)) {
 					if (name.equals(scriptdir) && file.isDirectory()) {
 						String[] scripts = file.list();
 						if (scripts != null) {
 							for (String script : scripts) {
 								String path = name + "/" + script;
 								model.addMapping(IMappingModel.SCRIPTSDIR, Type.INCLUDE, path,
 										false);
 							}
 						}
 					} else {
 						if (isUpdate == false) {
 							model.addMapping(IMappingModel.APPDIR, Type.INCLUDE, name, false);
 						}
 					}
 				}
 			}
 			model.store();
 			notifier.statusChanged(new BasicStatus(StatusCode.STOPPING, "Application Update",
 					"Creating default deployment.properites file..."));
 		}
 	}
 
 	private boolean shoudBeExcluded(String name) {
 		return ProjectResourcesWriter.DESCRIPTOR.equals(name)
 				|| name.toLowerCase().contains("test") || name.startsWith(".");
 	}
 
 	private void updateDescriptor(File descrFile) throws IOException, JAXBException {
 		final FileInputStream packageStream = new FileInputStream(descrFile);
 
 		PackageDescription desc = new PackageDescription(packageStream);
 		File scriptsDir = findExistingScripts(descrFile.getParentFile());
 		String scripts = scriptsDir != null ? scriptsDir.getName() : null;
 		boolean isDirty = false;
 		File docroot = findPublicFolder(descrFile.getParentFile());
 		if (scripts != null && !scripts.equals(desc.getPackage().getScriptsdir())) {
 			desc.getPackage().setScriptsdir(scripts);
 			isDirty = true;
 		}
 		if (docroot != null && !docroot.getName().equals(desc.getPackage().getDocroot())) {
 			desc.getPackage().setDocroot(DEFAULT_APP_DIR +"/" + DEFAULT_DOCROOT);
 			isDirty = true;
 		}
 		packageStream.close();
 		if (isDirty) {
 			final FileOutputStream printStream = new FileOutputStream(descrFile);
 			JaxbHelper.marshalPackage(printStream, desc.getPackage());
 			printStream.close();
 		}
 	}
 
 	private File getScriptsDirectory(File container) throws IOException {
 		Package pkg = getPackage(container);
 		if (pkg == null) {
 			// try to find existing scripts
 			File existingFolder = findExistingScripts(container);
 			if (existingFolder != null) {
 				return existingFolder;
 			}
 		}
 		String scriptFolder = pkg.getScriptsdir();
		if (scriptFolder == null) {
 			scriptFolder = "scripts";
 		}
 		return new File(container, scriptFolder);
 	}
 
 	private File findExistingScripts(File root) throws IOException {
 		if (root.isDirectory()) {
 			File[] files = root.listFiles();
 			for (File file : files) {
 				File result = findExistingScripts(file);
 				if (result != null) {
 					return result;
 				}
 			}
 		} else {
 			DeploymentScriptTypes[] types = DeploymentScriptTypes.values();
 			for (DeploymentScriptTypes type : types) {
 				if (type.getFilename().equals(root.getName())) {
 					return root.getParentFile();
 				}
 			}
 		}
 		return null;
 	}
 
 	private File findPublicFolder(File root) throws IOException {
 		if (root.isDirectory()) {
 			File[] files = root.listFiles();
 			for (File file : files) {
 				if (file.isDirectory() && file.getName().equals(DEFAULT_DOCROOT)) {
 					return file;
 				}
 			}
 		}
 		return null;
 	}
 
 	private static Package getPackage(File container) {
 		File descriptor = new File(container, ProjectResourcesWriter.DESCRIPTOR);
 		Package pkg;
 		try {
 			FileInputStream stream = new FileInputStream(descriptor);
 			pkg = JaxbHelper.unmarshalPackage(stream);
 		} catch (FileNotFoundException e) {
 			return null;
 		} catch (IOException e) {
 			throw new IllegalArgumentException("Error reading descriptor file "
 					+ descriptor.getAbsolutePath(), e);
 		} catch (JAXBException e) {
 			throw new IllegalArgumentException("Error reading descriptor file "
 					+ descriptor.getAbsolutePath(), e);
 		}
 
 		if (pkg == null) {
 			throw new IllegalArgumentException("Error reading descriptor file "
 					+ descriptor.getAbsolutePath());
 		}
 		return pkg;
 	}
 
 	private static String getProjectName(File descriptor) {
 		Package pkg = getPackage(descriptor.getParentFile());
 		if (pkg == null) {
 			// no descriptor file - choose project name as directory name
 			final File parentFile = descriptor.getParentFile();
 			return parentFile.getName();
 		}
 		return pkg.getName();
 	}
 
 	/**
 	 * Writes an application to a given destination directory
 	 * 
 	 * @param destination
 	 * @param app
 	 * @throws IOException
 	 */
 	public void writeApplication(File destination, TemplateApplications app)
 			throws IOException {
 
 		final List<String> allResources = getAllResources(app);
 
 		for (String path : allResources) {
 
 			// file handling
 			if (!path.endsWith("/")) {
 				copyFile(destination, path, app);
 			} else {
 				createFolder(destination, path, app);
 			}
 
 		}
 	}
 
 	private boolean createFolder(File destination, String path,
 			TemplateApplications app) {
 		final File file = new File(destination, relativeToApp(path, app));
 		return file.mkdirs();
 	}
 
 	private void copyFile(File destination, String path,
 			TemplateApplications app) throws IOException, FileNotFoundException {
 		if (path.length() == 0) {
 			return;
 		}
 		final InputStream is = this.getClass().getResourceAsStream(path);
 		if (is == null) {
 			throw new IOException(path + " path is not found");
 		}
 
 		File outputFile = new File(destination, relativeToApp(path, app));
 
 		// create canonical structure
 		outputFile.getParentFile().mkdirs();
 
 		if (!outputFile.createNewFile()) {
 			throw new IOException("Cannot create file "
 					+ outputFile.getAbsolutePath());
 		}
 
 		InputOutputResource ior = new InputOutputResource(is,
 				new FileOutputStream(outputFile));
 		ior.copy();
 	}
 
 	private String relativeToApp(String path, TemplateApplications app) {
 		final int length = app.getBasePath().length();
 		return path.substring(length);
 	}
 
 	private List<String> getAllResources(TemplateApplications app)
 			throws IOException {
 		final InputStream iStream = this.getClass().getResourceAsStream(
 				app.getMap());
 		BufferedReader reader = new BufferedReader(new InputStreamReader(
 				iStream));
 
 		List<String> paths = new ArrayList<String>();
 		String readLine = reader.readLine();
 		while (readLine != null) {
 			paths.add(readLine.trim());
 			readLine = reader.readLine();
 		}
 		return paths;
 	}
 
 	private String xmlEscape(String name) {
 		for (int i = 0; i < name.length(); i++) {
 			char c = name.charAt(i);
 			if (c == '&' || c == '<' || c == '>') {
 				return "<![CDATA[" + name.replaceAll("]]>", "]]>]]><![CDATA[")
 						+ "]]>";
 			}
 		}
 
 		return name;
 	}
 
 }
