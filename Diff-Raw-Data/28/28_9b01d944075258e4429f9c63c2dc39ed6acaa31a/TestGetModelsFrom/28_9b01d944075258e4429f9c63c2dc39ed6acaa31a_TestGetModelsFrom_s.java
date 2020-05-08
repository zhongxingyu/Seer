 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.tests.unit.core.util.modelutils;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.emf.compare.tests.EMFCompareTestPlugin;
 import org.eclipse.emf.compare.tests.util.FileUtils;
 import org.eclipse.emf.compare.util.ModelUtils;
 import org.eclipse.emf.ecore.EObject;
 
 /**
  * Tests the behavior of {@link ModelUtils#getModelsFrom(File)}.
  * 
  * @author Laurent Goubet <a href="mailto:laurent.goubet@obeo.fr">laurent.goubet@obeo.fr</a>
  */
 @SuppressWarnings("nls")
 public class TestGetModelsFrom extends TestCase {
 	/** Full path to the directory containing the non-regression models. */
 	private static final String INPUT_DIRECTORY = "/inputs";
 
 	/**
 	 * This array contains references to all subdirectories of {@link #INPUT_DIRECTORY} that does not contain
 	 * models.
 	 */
 	private ArrayList<File> invalidDirectories = new ArrayList<File>();
 
 	/**
 	 * This array contains references to all subdirectories of {@link #INPUT_DIRECTORY} directly containing
 	 * non-regression models.
 	 */
 	private ArrayList<File> modelDirectories = new ArrayList<File>();
 
 	/** Names of directories we shouldn't be able to read. */
 	private String[] unreadableDirectories = {"", "\r\n", "/lost+found/", };
 
 	/**
 	 * Default constructor. Scans for model files in {@link #INPUT_DIRECTORY}.
 	 */
	public TestGetModelsFrom() {
 		File inputDir = null;
 		try {
 			inputDir = new File(FileLocator.toFileURL(
 					EMFCompareTestPlugin.getDefault().getBundle().getEntry(INPUT_DIRECTORY)).getFile());
 		} catch (IOException e) {
 			// shouldn't happen
 			assert false;
 		}
 
 		final File[] directories = FileUtils.listDirectories(inputDir);
 		if (directories != null) {
 			for (int i = 0; i < directories.length; i++) {
 				scanForModels(directories[i]);
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link ModelUtils#getModelsFrom(File)} with invalid directories. Expects an empty list to be
 	 * returned.
 	 */
 	public void testGetModelsFromInvalidDirectory() {
 		for (File invalidDirectory : invalidDirectories) {
 			try {
 				assertEquals("Unexpected result of getModelsFrom() with invalid directory.",
						new ArrayList<EObject>(), ModelUtils.getModelsFrom(invalidDirectory));
 			} catch (IOException e) {
 				fail("Unexpected IOException thrown while loading models.");
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link ModelUtils#getModelsFrom(File)} with <code>null</code> directory. Expects a
 	 * {@link NullPointerException} to be thrown.
 	 */
 	public void testGetModelsFromNullDirectory() {
 		try {
			ModelUtils.getModelsFrom(null);
 			fail("Expected NullPointerException hasn't been thrown.");
 		} catch (NullPointerException e) {
 			// We expected this
 		} catch (IOException e) {
 			fail("Expected NullPointerException hasn't been thrown.");
 		}
 	}
 
 	/**
 	 * Tests {@link ModelUtils#getModelsFrom(File)} with directories that cannot be read. Expects an empty
 	 * list to be returned.
 	 */
 	public void testGetModelsFromUnreadableDirectory() {
 		for (String unreadableDirectory : unreadableDirectories) {
 			try {
 				assertEquals("Unexpected result of getModelsFrom() with unreadable directory.",
						new ArrayList<EObject>(), ModelUtils.getModelsFrom(new File(unreadableDirectory)));
 			} catch (IOException e) {
 				fail("Unexpected IOException has been thrown.");
 			}
 		}
 	}
 
 	/**
 	 * Tests {@link ModelUtils#getModelsFrom(File)} with valid directories. Expects the returned list to have
 	 * a size equal to the number of files contained by the given directory, and all of the list's objects to
 	 * have an associated resource.
 	 */
 	public void testGetModelsFromValidDirectory() {
 		for (File modelDirectory : modelDirectories) {
 			List<EObject> result = null;
 			try {
				result = ModelUtils.getModelsFrom(modelDirectory);
 			} catch (IOException e) {
 				fail("Unexpected IOException thrown while loading models.");
 			}
 
 			// Keeps compiler happy
 			assert result != null;
 
 			int expectedSize = 0;
 			for (File aFile : modelDirectory.listFiles()) {
 				if (!aFile.isDirectory() && !aFile.getName().startsWith(".")) {
 					expectedSize++;
 				}
 			}
 			assertSame("Resulting list doesn't contain the expected number of objects.", expectedSize, result
 					.size());
 
 			for (EObject loadedModel : result) {
 				assertNotNull("Loaded EObject isn't associate to a resource.", loadedModel.eResource());
 			}
 		}
 	}
 
 	/**
 	 * Called from initializer, this method allows retrieval of references to the files corresponding to the
 	 * non-regression models.
 	 * 
 	 * @param folder
 	 *            Folder in which model files are to be found.
 	 */
 	private void scanForModels(File folder) {
 		// Ignores the folder containing non-standard models (uml, gmfgen, ...)
 		if (folder.getName().contains("nonstd"))
 			return;
 		
 		final File[] subFolders = FileUtils.listDirectories(folder);
 		if (subFolders.length != 0) {
 			invalidDirectories.add(folder);
 			for (File aSubFolder : subFolders) {
 				scanForModels(aSubFolder);
 			}
 		} else if (folder.exists() && folder.isDirectory()) {
 			final File[] files = folder.listFiles();
 			for (File aFile : files) {
 				// All directories containing at least one file that isn't a folder is considered to contain
 				// models.
 				if (!aFile.isDirectory() && !aFile.getName().startsWith(".")) {
 					modelDirectories.add(folder);
 					break;
 				}
 			}
 		}
 	}
 }
