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
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import javax.xml.bind.JAXBException;
 
 import org.zend.sdklib.descriptor.pkg.Package;
 import org.zend.sdklib.descriptor.pkg.Version;
 import org.zend.sdklib.internal.library.AbstractChangeNotifier;
 import org.zend.sdklib.internal.library.BasicStatus;
 import org.zend.sdklib.internal.project.ProjectResourcesWriter;
 import org.zend.sdklib.internal.utils.JaxbHelper;
 import org.zend.sdklib.library.IChangeNotifier;
 import org.zend.sdklib.library.StatusCode;
 import org.zend.sdklib.mapping.IMapping;
 import org.zend.sdklib.mapping.IMappingEntry;
 import org.zend.sdklib.mapping.IMappingEntry.Type;
 import org.zend.sdklib.mapping.IMappingLoader;
 import org.zend.sdklib.mapping.IMappingModel;
 import org.zend.sdklib.mapping.MappingModelFactory;
 
 /**
  * Provides ability to create zpk application package based on
  * 
  * @author Wojciech Galanciak, 2011
  * 
  */
 public class PackageBuilder extends AbstractChangeNotifier {
 
 	private static final String EXTENSION = ".zpk";
 	private static final int BUFFER = 1024;
 
 	private ZipOutputStream out;
 	private File container;
 	private IMappingModel model;
 
 	public PackageBuilder(File container, IMappingLoader loader,
 			IChangeNotifier notifier) {
 		super(notifier);
 		this.container = container;
 		this.model = MappingModelFactory.createModel(loader, container);
 	}
 
 	public PackageBuilder(File container, IChangeNotifier notifier) {
 		super(notifier);
 		this.container = container;
 		this.model = MappingModelFactory.createDefaultModel(container);
 	}
 
 	public PackageBuilder(File container, IMappingLoader loader) {
 		super();
 		this.container = container;
 		this.model = MappingModelFactory.createModel(loader, container);
 	}
 
 	public PackageBuilder(File container) {
 		super();
 		this.container = container;
 		this.model = MappingModelFactory.createDefaultModel(container);
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
 		container = container.getCanonicalFile();
 		String name = getPackageName(container);
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
 			File descriptorFile = new File(container,
 					ProjectResourcesWriter.DESCRIPTOR);
 			addFileToZip(descriptorFile, null, null, null);
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
 
 	private void resolveMappings() throws IOException {
 		String appdir = getAppdirName(container);
 		String scriptsdir = getScriptsdirName(container);
 		if (appdir != null) {
 			resolveMapping(IMappingModel.APPDIR, appdir);
 		}
 		if (scriptsdir != null) {
 			resolveMapping(IMappingModel.SCRIPTSDIR, scriptsdir);
 		}
 	}
 
 	private void resolveMapping(String tag, String folderName)
 			throws IOException {
 		List<IMappingEntry> entries = model.getEnties();
 		for (IMappingEntry entry : entries) {
 			if (entry.getType() == Type.INCLUDE
 					&& entry.getFolder().equals(tag)) {
 				List<IMapping> mappings = entry.getMappings();
 				for (IMapping mapping : mappings) {
 					File resource = new File(new File(container,
 							mapping.getPath()).getCanonicalPath());
 					if (resource.exists()) {
 						addFileToZip(resource, folderName, mapping, tag);
 					}
 				}
 			}
 		}
 	}
 
 	private void addFileToZip(File root, String mappingFolder,
 			IMapping mapping, String tag) throws IOException {
 		if (!model.isExcluded(tag, root.getCanonicalPath())) {
 			if (root.isDirectory()) {
 				File[] children = root.listFiles();
 				for (File child : children) {
 					addFileToZip(child, mappingFolder, mapping, tag);
 				}
 			} else {
 				String location = root.getCanonicalPath();
 				BufferedInputStream in = new BufferedInputStream(
 						new FileInputStream(location), BUFFER);
 				String path = getContainerRelativePath(location);
 				if (mapping != null && mapping.getPath() != null) {
 					path = root.getCanonicalPath();
 					String fullMapping = new File(container, mapping.getPath())
 							.getCanonicalPath();
 					int position = 0;
 					if (mapping.isContent()) {
 						position = fullMapping.length();
 					} else {
 						position = fullMapping.lastIndexOf(File.separator);
 					}
 					String destFolder = path.substring(position);
 					path = mappingFolder + destFolder;
 				}
 				ZipEntry entry = new ZipEntry(path.replaceAll("\\\\", "/"));
 				out.putNextEntry(entry);
 				int count;
 				byte data[] = new byte[BUFFER];
 				while ((count = in.read(data, 0, BUFFER)) != -1) {
 					out.write(data, 0, count);
 				}
 				in.close();
 				notifier.statusChanged(new BasicStatus(StatusCode.PROCESSING,
 						"Package creation", "Creating deployment package...", 1));
 			}
 		}
 	}
 
 	private String getContainerRelativePath(String path) {
 		int position = container.getAbsolutePath().length() + 1;
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
 			String scriptdir = getScriptsdirName(container);
 			File[] files = container.listFiles();
 			for (File file : files) {
 				String name = file.getName();
 				if (!model.isExcluded(null, name)
 						&& !ProjectResourcesWriter.DESCRIPTOR.equals(name)
 						&& !name.toLowerCase().contains("test")) {
 					if (name.equals(scriptdir)) {
 						model.addMapping(IMappingModel.SCRIPTSDIR,
 								Type.INCLUDE, name, false, true);
 					} else {
 						model.addMapping(IMappingModel.APPDIR, Type.INCLUDE,
 								name, false, false);
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
 					File resource = new File(new File(container,
 							mapping.getPath()).getCanonicalPath());
 					if (resource.exists()) {
 						totalWork += countFiles(resource, folder);
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
