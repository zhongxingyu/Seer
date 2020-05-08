 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
 * Contributors:
  * wesendon
  * koegel
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.startup;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.emfstore.internal.common.ConsoleProgressMonitor;
 import org.eclipse.emf.emfstore.internal.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.common.model.ModelVersion;
 import org.eclipse.emf.emfstore.internal.common.model.util.MalformedModelVersionException;
 import org.eclipse.emf.emfstore.internal.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.internal.migration.EMFStoreMigrationException;
 import org.eclipse.emf.emfstore.internal.migration.EMFStoreMigratorUtil;
 import org.eclipse.emf.emfstore.internal.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
 
 /**
  * Applies migrator to files on server.
  * 
  * @author koegel
  * @author wesendon
  */
 // TODO: internal
 public class MigrationManager {
 
 	/**
 	 * Starts migration.
 	 * 
 	 * @throws FatalESException in case of failure
 	 */
 	public void migrateModel() throws FatalESException {
 		int modelVersionNumber;
 		try {
 			modelVersionNumber = ModelUtil.getModelVersionNumber();
 		} catch (MalformedModelVersionException e1) {
 			throw new FatalESException(e1);
 		}
 
 		// check for legacy server space
 		File versionFile = new File(ServerConfiguration.getModelReleaseNumberFileName());
 		if (!versionFile.exists()) {
 			stampCurrentVersionNumber(modelVersionNumber);
 		}
 
 		// check if we need to migrate
 		ModelVersion modelVersion;
 		URI versionFileUri = URI.createFileURI(ServerConfiguration.getModelReleaseNumberFileName());
 		ResourceSet resourceSet = new ResourceSetImpl();
 		try {
 			Resource resource = resourceSet.getResource(versionFileUri, true);
 			EList<EObject> directContents = resource.getContents();
 			modelVersion = (ModelVersion) directContents.get(0);
 			// BEGIN SUPRESS CATCH EXCEPTION
 		} catch (RuntimeException e) {
 			// END SUPRESS CATCH EXCEPTION
 			// resource can not be loaded, assume version number before metamodel split
 			modelVersion = ModelFactory.eINSTANCE.createModelVersion();
 			modelVersion.setReleaseNumber(4);
 		}
 
 		if (modelVersion.getReleaseNumber() == modelVersionNumber) {
 			return;
 		}
 
 		if (!EMFStoreMigratorUtil.isMigratorAvailable()) {
 			throw new FatalESException("Model must be migrated to new version, but no migrators are available.");
 		}
 
 		// ask for confirmation
 		boolean doProcceed = askForConfirmationForMigration();
 		if (!doProcceed) {
 			String message = "Server shutting down, model update is mandatory.";
 			System.out.println(message);
 			throw new FatalESException(message);
 		}
 
 		// migrate all versions of all projects
 		// we need to migrate
 		File serverSpaceDirectory = new File(ServerConfiguration.getServerHome());
 		// for all projects
 		for (File projectDirectory : serverSpaceDirectory.listFiles()) {
 			if (projectDirectory.getName().startsWith(ServerConfiguration.FILE_PREFIX_PROJECTFOLDER)
 				&& projectDirectory.isDirectory()) {
 
 				System.out.println("Migrating project at " + projectDirectory + "...");
 
 				convertInitialProjectState(modelVersion, projectDirectory);
 
 				File[] listFiles = projectDirectory.listFiles(new FileFilter() {
 					public boolean accept(File pathname) {
 						if (pathname.isFile()
 							&& (pathname.getName().endsWith(".ucp") || pathname.getName().endsWith(".ups"))) {
 							return true;
 						}
 						return false;
 					}
 				});
 
 				convertAllVersions(modelVersion, projectDirectory, listFiles);
 
 				// convertAllBackupStates(modelVersion, projectDirectory, listFiles);
 			}
 		}
 		stampCurrentVersionNumber(modelVersionNumber);
 	}
 
 	private void convertInitialProjectState(ModelVersion modelVersion, File projectDirectory)
 		throws FatalESException {
 		URI version0StateURI = URI.createFileURI(projectDirectory.getAbsolutePath() + File.separatorChar
 			+ ServerConfiguration.FILE_PREFIX_PROJECTSTATE + "0" + ServerConfiguration.FILE_EXTENSION_PROJECTSTATE);
 		try {
 			System.out.println("Migrating version 0...");
 			migrate(version0StateURI, new ArrayList<URI>(), modelVersion.getReleaseNumber());
 		} catch (EMFStoreMigrationException e) {
 			throw new FatalESException("Migration of project at " + projectDirectory + " failed!", e);
 		}
 	}
 
 	private void convertAllVersions(ModelVersion modelVersion, File projectDirectory, File[] listFiles)
 		throws FatalESException {
 		List<URI> changePackageURIs = new ArrayList<URI>();
 
 		Arrays.sort(listFiles, new Comparator<File>() {
 
 			public int compare(File o1, File o2) {
 				return compare(o1.getName(), o2.getName());
 			}
 
 			private int compare(String name1, String name2) {
 				return getNumber(name1).compareTo(getNumber(name2));
 			}
 
 			private Integer getNumber(String filename) {
 				String name = filename.substring(0, filename.lastIndexOf("."));
 
 				int i = name.length() - 1;
 				while (i >= 0 && '0' <= name.charAt(i) && name.charAt(i) <= '9') {
 					i--;
 				}
 				i++;
 
 				String number = name.substring(i);
 				return number.equals("") ? 0 : Integer.parseInt(number);
 			}
 		});
 
 		for (File changePackageFile : listFiles) {
 			String changePackageName = changePackageFile.getName();
 			if (changePackageName.startsWith(ServerConfiguration.FILE_PREFIX_CHANGEPACKAGE)) {
 				int versionSpec = parseVersionSpecFromFileName(changePackageName);
 				URI changePackageURI = URI.createFileURI(changePackageFile.getAbsolutePath());
 				changePackageURIs.add(changePackageURI);
 				String projectStateFilename = projectDirectory.getAbsolutePath() + File.separatorChar
 					+ ServerConfiguration.FILE_PREFIX_PROJECTSTATE + versionSpec
 					+ ServerConfiguration.FILE_EXTENSION_PROJECTSTATE;
 				File projectStateFile = new File(projectStateFilename);
 				if (projectStateFile.exists()) {
 					URI projectURI = URI.createFileURI(projectStateFilename);
 					try {
 						System.out.println("Migrating version " + versionSpec + " with its "
 							+ (changePackageURIs.size() - 1) + " previous versions...");
 						migrate(projectURI, changePackageURIs, modelVersion.getReleaseNumber());
 					} catch (EMFStoreMigrationException e) {
 						throw new FatalESException("Migration of project at " + projectDirectory + " failed!", e);
 					}
 					changePackageURIs.clear();
 				}
 
 			}
 
 		}
 	}
 
 	private int parseVersionSpecFromFileName(String versionName) {
 		int startOfFileExtension = versionName.lastIndexOf(".");
 		int prefixLength = ServerConfiguration.FILE_PREFIX_CHANGEPACKAGE.length();
 		String versionSpecString = versionName.substring(prefixLength, startOfFileExtension);
 		int versionSpec = Integer.parseInt(versionSpecString);
 		return versionSpec;
 	}
 
 	/**
 	 * Migrate the model instance if neccessary.
 	 * 
 	 * @param projectURI the uri of the project state
 	 * @param changesURI the uri of the local changes of the project state
 	 * @param sourceModelReleaseNumber
 	 * @throws ModelMigrationException
 	 */
 	private void migrate(URI projectURI, List<URI> changesURIs, int sourceModelReleaseNumber)
 		throws EMFStoreMigrationException {
 
 		List<URI> modelURIs = new ArrayList<URI>();
 		modelURIs.add(projectURI);
 		for (URI changeURI : changesURIs) {
 			modelURIs.add(changeURI);
 		}
		EMFStoreMigratorUtil.getEMFStoreMigrator().migrate(modelURIs,
 			new ConsoleProgressMonitor());
 	}
 
 	private void stampCurrentVersionNumber(int modelReleaseNumber) {
 		URI versionFileUri = URI.createFileURI(ServerConfiguration.getModelReleaseNumberFileName());
 		Resource versionResource = new ResourceSetImpl().createResource(versionFileUri);
 		ModelVersion modelVersion = ModelFactory.eINSTANCE.createModelVersion();
 		modelVersion.setReleaseNumber(modelReleaseNumber);
 		versionResource.getContents().add(modelVersion);
 		try {
 			ModelUtil.saveResource(versionResource, ModelUtil.getResourceLogger());
 		} catch (IOException e) {
 			// MK Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Ask for Confirmation for model migration.
 	 * 
 	 * @return true if user wants to proceed
 	 * @throws FatalESException
 	 */
 	private boolean askForConfirmationForMigration() throws FatalESException {
 		System.out
 			.println("Your model is not up to date. Do you want to update now and did you backup your emfstore folder? (y/n)");
 
 		byte[] buffer = new byte[1];
 		String input = "";
 		int read = 0;
 
 		try {
 			read = System.in.read(buffer, 0, 1);
 		} catch (IOException e) {
 			throw new FatalESException("Cannot read from input", e);
 		}
 
 		input = new String(buffer, 0, read);
 		return input.equalsIgnoreCase("y");
 	}
 }
