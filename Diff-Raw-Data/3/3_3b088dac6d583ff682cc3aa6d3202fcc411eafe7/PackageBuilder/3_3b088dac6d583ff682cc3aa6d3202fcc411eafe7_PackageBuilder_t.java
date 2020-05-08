 /*******************************************************************************
  * Copyright (c) May 26, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.application;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.bind.JAXBException;
 
 import org.zend.sdklib.SdkException;
 import org.zend.sdklib.descriptor.pkg.Package;
 import org.zend.sdklib.descriptor.pkg.Version;
 import org.zend.sdklib.internal.library.AbstractChangeNotifier;
 import org.zend.sdklib.internal.mapping.LibraryMapping;
 import org.zend.sdklib.internal.project.ProjectResourcesWriter;
 import org.zend.sdklib.internal.utils.JaxbHelper;
 import org.zend.sdklib.mapping.IMapping;
 import org.zend.sdklib.mapping.IMappingEntry;
 import org.zend.sdklib.mapping.IMappingEntry.Type;
 import org.zend.sdklib.mapping.IMappingLoader;
 import org.zend.sdklib.mapping.IMappingModel;
 import org.zend.sdklib.mapping.IVariableResolver;
 import org.zend.sdklib.mapping.MappingModelFactory;
 import org.zend.sdklib.project.DeploymentScriptTypes;
 import org.zend.webapi.core.progress.BasicStatus;
 import org.zend.webapi.core.progress.IChangeNotifier;
 import org.zend.webapi.core.progress.StatusCode;
 
 /**
  * Provides ability to create zpk application package based on
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class PackageBuilder extends AbstractChangeNotifier {
 
 	public static final String EXTENSION = ".zpk";
 
 	private static final int BUFFER = 1024;
 
 	private ZipOutputStream out;
 	private File container;
 	private File configLocation;
 	private IMappingModel model;
 
 	private Set<String> addedPaths;
 	private IVariableResolver variableResolver;
 
 	public PackageBuilder(File container, File configLocation,
 			IMappingLoader loader, IChangeNotifier notifier) {
 		super(notifier);
 		this.container = container;
 		this.configLocation = configLocation;
 		this.model = loader != null ? MappingModelFactory.createModel(loader,
 				configLocation) : MappingModelFactory
 				.createDefaultModel(configLocation);
 		this.addedPaths = new HashSet<String>();
 	}
 
 	public PackageBuilder(File container, File configLocation,
 			IChangeNotifier notifier) {
 		this(container, configLocation, null, notifier);
 	}
 
 	public PackageBuilder(File container, File configLocation,
 			IMappingLoader loader) {
 		super();
 		this.container = container;
 		this.configLocation = configLocation;
 		this.model = loader != null ? MappingModelFactory.createModel(loader,
 				configLocation) : MappingModelFactory
 				.createDefaultModel(configLocation);
 		this.addedPaths = new HashSet<String>();
 	}
 
 	public PackageBuilder(File container, File configLocation) {
 		this(container, configLocation, (IMappingLoader) null);
 	}
 
 	public PackageBuilder(File container, IMappingLoader loader,
 			IChangeNotifier notifier) {
 		this(container, container, loader, notifier);
 	}
 
 	public PackageBuilder(File container, IChangeNotifier notifier) {
 		this(container, container, notifier);
 	}
 
 	public PackageBuilder(File container, IMappingLoader loader) {
 		this(container, container, loader);
 	}
 
 	public PackageBuilder(File container) {
 		this(container, (IMappingLoader) null);
 	}
 
 	public void setVariableResolver(IVariableResolver variableResolver) {
 		this.variableResolver = variableResolver;
 	}
 
 	/**
 	 * @param directory
 	 * @return the file name to be created when deployment package file is
 	 *         created
 	 * @throws IOException
 	 */
 	public File getDeploymentPackageFile(File directory) throws IOException {
 		if (directory == null || !directory.isDirectory()) {
 			log.error(new IllegalArgumentException(
 					"Location cannot be null or non-existing directory"));
 			return null;
 		}
 		configLocation = configLocation.getCanonicalFile();
 		String name = getPackageName(configLocation);
 		if (name == null) {
 			return null;
 		}
 		return new File(directory, name + EXTENSION);
 	}
 
 	/**
 	 * Creates compressed package file in the given folder.
 	 * 
 	 * @param path
 	 *            - location where package should be created
 	 * @return
 	 */
 	public File createDeploymentPackage(File directory) {
 		try {
 			File result = getDeploymentPackageFile(directory);
 			if (result == null) {
 				return null;
 			}
 			out = new ZipOutputStream(new BufferedOutputStream(
 					new FileOutputStream(result)));
 			if (!model.isLoaded()) {
 				createDefaultModel();
 			}
 			notifier.statusChanged(new BasicStatus(StatusCode.STARTING,
 					"Package creation", "Creating " + result.getName()
 							+ " deployment package...", calculateTotalWork()));
 			File descriptorFile = new File(configLocation,
 					ProjectResourcesWriter.DESCRIPTOR);
 			addFileToZip(descriptorFile, null, null, null, false);
 			resolveIconAndLicence();
 			resolveMappings();
 			out.close();
 			notifier.statusChanged(new BasicStatus(StatusCode.STOPPING,
 					"Package creation",
 					"Deployment package created successfully."));
 			return result;
 		} catch (IOException e) {
 			notifier.statusChanged(new BasicStatus(StatusCode.ERROR,
 					"Package creation",
 					"Error during building deployment package", e));
 			log.error("Error during building deployment package");
 			log.error(e);
 		}
 		return null;
 	}
 
 	/**
 	 * Creates compressed package file in the given location.
 	 * 
 	 * @param destination
 	 *            - location where package should be created
 	 * @return
 	 */
 	public File createDeploymentPackage(String destination) {
 		return createDeploymentPackage(new File(destination));
 	}
 
 	/**
 	 * Creates compressed package file in the current location.
 	 * 
 	 * @param path
 	 *            - location where package should be created
 	 * @return
 	 */
 	public File createDeploymentPackage() {
 		return createDeploymentPackage(new File("."));
 	}
 
 	private void resolveIconAndLicence() {
 		String icon = getIconName(configLocation);
 		if (icon != null) {
 			try {
 				addFileToZip(new File(container, icon), null, null, null, false);
 			} catch (IOException e) {
 				// do nothing, it means that descriptor has entries which are
 				// not valid
 			}
 		}
 		String license = getLicenseName(configLocation);
 		if (license != null) {
 			try {
 				addFileToZip(new File(container, license), null, null, null,
 						false);
 			} catch (IOException e) {
 				// do nothing, it means that descriptor has entries which are
 				// not valid
 			}
 		}
 	}
 
 	private void resolveMappings() throws IOException {
 		String appdir = getAppdirName(configLocation);
 		String scriptsdir = getScriptsdirName(configLocation);
 		if (appdir != null) {
 			if (!appdir.isEmpty()) {
 				addNewFolderToZip(new File(container, appdir));
 			}
 			resolveMapping(IMappingModel.APPDIR, appdir, false);
 			resolveLibraryMapping(appdir, false);
 		}
 		if (scriptsdir != null && !scriptsdir.isEmpty()) {
 			addNewFolderToZip(new File(container, scriptsdir));
 			resolveMapping(IMappingModel.SCRIPTSDIR, scriptsdir, true);
 		}
 	}
 
 	private void resolveLibraryMapping(String folderName, boolean b)
 			throws IOException {
 		List<IMappingEntry> entries = model.getEnties(Type.INCLUDE, "library");
 
 		for (IMappingEntry entry : entries) {
 			List<IMapping> mappings = entry.getMappings();
 			for (IMapping mapping : mappings) {
 				LibraryMapping libraryMapping = LibraryMapping.create(
 						entry.getFolder(), mapping.getPath());
 				if (libraryMapping != null) {
 					String mappingFolder = folderName;
 					if (!libraryMapping.getFolder().isEmpty()) {
 						mappingFolder += File.separator
 								+ libraryMapping.getFolder();
 					}
 					String library = resolveVariables(libraryMapping
 							.getLibraryPath());
 					File libraryFile = new File(library);
 					if (!libraryFile.isAbsolute()) {
 						libraryFile = new File(container, library);
 					}
 					File resource = new File(libraryFile.getCanonicalPath());
 					if (resource.exists()) {
 						addFileToZip(resource, mappingFolder, library,
 								"library", false);
 					}
 				}
 			}
 		}
 	}
 
 	private String resolveVariables(String libraryPath) throws IOException {
 		try {
 			return variableResolver != null ? variableResolver
 					.resolve(libraryPath) : libraryPath;
 		} catch (SdkException e) {
 			throw new IOException(e);
 		}
 	}
 
 	private void resolveMapping(String tag, String folderName, boolean allowFlat)
 			throws IOException {
 		List<IMappingEntry> entries = model.getEnties(Type.INCLUDE, tag);
 
 		for (IMappingEntry entry : entries) {
 			List<IMapping> mappings = entry.getMappings();
 			for (IMapping mapping : mappings) {
 				File resource = new File(
 						new File(container, mapping.getPath())
 								.getCanonicalPath());
 
 				if (resource.exists()) {
 					allowFlat &= resource.isDirectory() && entries.size() == 1
 							&& mappings.size() == 1;
 
 					addFileToZip(resource, folderName, mapping.getPath(), tag,
 							allowFlat);
 				}
 			}
 		}
 	}
 
 	private void addNewFolderToZip(File root) throws IOException {
 		String location = root.getCanonicalPath();
 		String path = getContainerRelativePath(location) + "/";
 		ZipEntry entry = new ZipEntry(path.replaceAll("\\\\", "/"));
 		out.putNextEntry(entry);
 	}
 
 	private void addFileToZip(File root, String mappingFolder,
 			String mappingPath, String tag, boolean allowFlat)
 			throws IOException {
 		if (!model.isExcluded(tag, root.getCanonicalPath())) {
 			if (root.isDirectory() && !isExcludeAllChildren(tag, root)) {
 				File[] children = root.listFiles();
 				for (File child : children) {
 					addFileToZip(child, mappingFolder, mappingPath, tag,
 							allowFlat);
 				}
 			} else {
 				String location = root.getCanonicalPath();
 				String path = getContainerRelativePath(location);
 				if (mappingPath != null) {
 					path = root.getCanonicalPath();
 					File file = new File(mappingPath);
 					if (!file.isAbsolute()) {
 						file = new File(container, mappingPath);
 					}
 					String fullMapping = file.getCanonicalPath();
 					String destFolder = path.substring((allowFlat ? path
 							: fullMapping).lastIndexOf(File.separator));
 					path = mappingFolder + destFolder;
 				}
 				if (root.isDirectory()) {
 					path += "/";
 				}
 				path = path.replaceAll("\\\\", "/");
 				if (path.startsWith("/")) {
 					path = path.substring(1);
 				}
 				if (addedPaths.add(path)) {
 					ZipEntry entry = new ZipEntry(path);
 					out.putNextEntry(entry);
 					if (!root.isDirectory()) {
 						int count;
 						byte data[] = new byte[BUFFER];
 						BufferedInputStream in = new BufferedInputStream(
 								new FileInputStream(location), BUFFER);
 						while ((count = in.read(data, 0, BUFFER)) != -1) {
 							out.write(data, 0, count);
 						}
 						in.close();
 						notifier.statusChanged(new BasicStatus(
 								StatusCode.PROCESSING, "Package creation",
 								"Creating deployment package...", 1));
 					}
 				}
 			}
 		}
 	}
 
 	private boolean isExcludeAllChildren(String tag, File root)
 			throws IOException {
 		File[] children = root.listFiles();
 		for (File file : children) {
 			if (!model.isExcluded(tag, file.getCanonicalPath())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private String getContainerRelativePath(String path) {
 		String containerPath = container.getAbsolutePath();
 		int position = containerPath.length() + 1;
 		if (!path.startsWith(containerPath) || position >= path.length()) {
 			containerPath = configLocation.getAbsolutePath();
 			position = containerPath.length() + 1;
 			if (!path.startsWith(containerPath) || position >= path.length()) {
 				return path;
 			}
 		}
 		return path.substring(position);
 	}
 
 	private String getPackageName(File container) {
 		String result = null;
 		Package p = getPackage(container);
 		if (p != null) {
 			String name = p.getName();
 			final Version version2 = p.getVersion();
 			if (version2 == null) {
 				throw new IllegalStateException(
 						"Error, missing <version> element in deployment descriptor");
 			}
 
 			String version = version2.getRelease();
 
 			if (name != null && version != null) {
 				result = name + "-" + version;
 			}
 		}
 		return result;
 	}
 
 	private String getAppdirName(File container) {
 		String result = null;
 		Package p = getPackage(container);
 		if (p != null) {
 			result = p.getAppdir();
 		}
		if (result == null || "library".equals(p.getType())) {
			result = "";
		}
 		return result;
 	}
 
 	private String getScriptsdirName(File container) {
 		String result = null;
 		Package p = getPackage(container);
 		if (p != null) {
 			result = p.getScriptsdir();
 		}
 		return result;
 	}
 
 	private String getIconName(File container) {
 		String result = null;
 		Package p = getPackage(container);
 		if (p != null) {
 			result = p.getIcon();
 		}
 		return result;
 	}
 
 	private String getLicenseName(File container) {
 		String result = null;
 		Package p = getPackage(container);
 		if (p != null) {
 			result = p.getEula();
 		}
 		return result;
 	}
 
 	private Package getPackage(File container) {
 		File descriptorFile = new File(container,
 				ProjectResourcesWriter.DESCRIPTOR);
 		if (!descriptorFile.exists()) {
 			log.error(descriptorFile.getAbsoluteFile() + " does not exist.");
 			return null;
 		}
 		FileInputStream pkgStream = null;
 		Package p = null;
 		try {
 			pkgStream = new FileInputStream(descriptorFile);
 			p = JaxbHelper.unmarshalPackage(pkgStream);
 		} catch (IOException e) {
 			throw new IllegalStateException(e);
 		} catch (JAXBException e) {
 			throw new IllegalStateException(e);
 		} finally {
 			try {
 				pkgStream.close();
 			} catch (IOException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 		return p;
 	}
 
 	private void createDefaultModel() throws IOException {
 		if (container.isDirectory()) {
 			String scriptdir = getScriptsdirName(configLocation);
 			File[] files = container.listFiles();
 			for (File file : files) {
 				String name = file.getName();
 				if (!model.isExcluded(null, name) && !shoudBeExcluded(name)) {
 					if (name.equals(scriptdir) && file.isDirectory()) {
 						String[] scripts = file.list();
 						for (String script : scripts) {
 							if (DeploymentScriptTypes.byName(script) != null) {
 								String path = name + "/" + script;
 								model.addMapping(IMappingModel.SCRIPTSDIR,
 										Type.INCLUDE, path, false);
 							}
 						}
 					} else {
 						model.addMapping(IMappingModel.APPDIR, Type.INCLUDE,
 								name, false);
 					}
 				}
 			}
 			if (scriptdir != null
 					&& model.getEntry(IMappingModel.SCRIPTSDIR, Type.INCLUDE)
 							.getMappings().size() == 0) {
 				notifier.statusChanged(new BasicStatus(StatusCode.WARNING,
 						"Package creation",
 						"Scriptsdir declared in descriptor file does not exist in the project"));
 				log.warning("Scriptsdir declared in descriptor file does not exist in the project");
 			}
 		}
 	}
 
 	private boolean shoudBeExcluded(String name) {
 		return ProjectResourcesWriter.DESCRIPTOR.equals(name)
 				|| name.toLowerCase().contains("test") || name.startsWith(".");
 	}
 
 	private int calculateTotalWork() throws IOException {
 		// is 1 because of deployment.xml file which is always added to the
 		// package
 		int totalWork = 1;
 		List<String> folders = model.getFolders();
 		for (String folder : folders) {
 			IMappingEntry entry = model.getEntry(folder, Type.INCLUDE);
 			if (entry != null) {
 				List<IMapping> includes = entry.getMappings();
 				for (IMapping mapping : includes) {
 					String path = mapping.getPath();
 					LibraryMapping libraryMapping = LibraryMapping.create(
 							entry.getFolder(), path);
 					if (libraryMapping != null) {
 						path = libraryMapping.getLibraryPath();
 					}
 					File file = new File(path);
 					if (!file.isAbsolute()) {
 						file = new File(container, mapping.getPath());
 					}
 					if (file.exists()) {
 						totalWork += countFiles(file, folder);
 					}
 				}
 			}
 		}
 		return totalWork;
 	}
 
 	private int countFiles(File file, String folder) throws IOException {
 		int counter = 0;
 		if (!model.isExcluded(folder, file.getCanonicalPath())) {
 			if (file.isDirectory()) {
 				File[] children = file.listFiles();
 				for (File child : children) {
 					counter += countFiles(child, folder);
 				}
 			} else {
 				counter++;
 			}
 		}
 		return counter;
 	}
 
 }
