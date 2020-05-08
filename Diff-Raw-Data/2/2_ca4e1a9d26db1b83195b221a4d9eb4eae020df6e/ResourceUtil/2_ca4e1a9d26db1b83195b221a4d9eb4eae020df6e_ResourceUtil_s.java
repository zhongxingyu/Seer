 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.internal.utils;
 
 import com.google.common.io.Closeables;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.Map;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 
 /**
  * This class will be used to provide various utilities aimed at IResource manipulation.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class ResourceUtil {
 	/**
 	 * This does not need to be instantiated.
 	 */
 	private ResourceUtil() {
 		// hides default constructor
 	}
 
 	/**
 	 * This will try and load the given file as an EMF model, and return the corresponding {@link Resource} if
 	 * at all possible.
 	 * 
 	 * @param storage
 	 *            The file we need to try and load as a model.
 	 * @param resourceSet
 	 *            The resource set in which to load this Resource.
 	 * @param options
 	 *            The options to pass to {@link Resource#load(java.util.Map)}.
 	 * @return The loaded EMF Resource if {@code file} was a model, {@code null} otherwise.
 	 */
 	// Suppressing the warning until bug 376938 is fixed
 	@SuppressWarnings("resource")
 	public static Resource loadResource(IStorage storage, ResourceSet resourceSet, Map<?, ?> options) {
 		final String resourceName = storage.getName();
 		String path = storage.getFullPath().toString();
 		if (!path.endsWith(resourceName)) {
 			final int endIndex = path.indexOf(resourceName) + resourceName.length();
 			path = path.substring(0, endIndex);
 		}
 		final URI uri = URI.createPlatformResourceURI(path, true);
 		final Resource existing = resourceSet.getResource(uri, false);
 		if (existing != null) {
 			return existing;
 		}
 
 		InputStream stream = null;
 		Resource resource = null;
 		try {
 			resource = resourceSet.createResource(uri);
 			stream = storage.getContents();
 			resource.load(stream, options);
 		} catch (IOException e) {
 			// return null
 		} catch (CoreException e) {
 			// return null
 		} catch (WrappedException e) {
 			// return null
 		} finally {
 			if (stream != null) {
 				try {
 					stream.close();
 				} catch (IOException e) {
 					// Should have been caught by the outer try
 				}
 			}
 		}
 
 		return resource;
 	}
 
 	/**
 	 * Checks whether the two given storages point to binary identical data.
 	 * 
 	 * @param left
 	 *            First of the two storages which content we are testing.
 	 * @param right
 	 *            Second of the two storages which content we are testing.
 	 * @return <code>true</code> if {@code left} and {@code right} are binary identical.
 	 */
 	public static boolean binaryIdentical(IStorage left, IStorage right) {
 		Reader leftReader = null;
 		Reader rightReader = null;
 		try {
 			leftReader = new BufferedReader(new InputStreamReader(left.getContents()));
 			rightReader = new BufferedReader(new InputStreamReader(right.getContents()));
 
 			final int bufferSize = 16384;
 			final char[] leftBuff = new char[bufferSize];
 			final char[] rightBuff = new char[bufferSize];
 			int readLeft = leftReader.read(leftBuff);
 			int readRight = rightReader.read(rightBuff);
 			while (readLeft > 0 && readRight > 0 && equalArrays(readLeft, readRight, leftBuff, rightBuff)) {
 				readLeft = leftReader.read(leftBuff);
 				readRight = rightReader.read(rightBuff);
 			}
 			// One last check in case we've reached the end of one side but not of the other
 			return equalArrays(readLeft, readRight, leftBuff, rightBuff);
 		} catch (CoreException e) {
 			// FIXME log
 		} catch (IOException e) {
 			// FIXME log
 		} finally {
 			if (leftReader != null) {
 				Closeables.closeQuietly(leftReader);
 			}
 			if (rightReader != null) {
 				Closeables.closeQuietly(rightReader);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether the three given storages point to binary identical data. This could be done by calling
 	 * {@link #binaryIdentical(IStorage, IStorage)} twice, though this implementation allows us to shortcut
 	 * whenever one byte differs... and will read one less file from its input stream.
 	 * 
 	 * @param left
 	 *            First of the three storages which content we are testing.
 	 * @param right
 	 *            Second of the three storages which content we are testing.
 	 * @param origin
 	 *            Third of the three storages which content we are testing.
 	 * @return <code>true</code> if {@code left}, {@code right} and {@code origin} are binary identical.
 	 */
 	public static boolean binaryIdentical(IStorage left, IStorage right, IStorage origin) {
 		Reader leftReader = null;
 		Reader rightReader = null;
 		Reader originReader = null;
 		try {
 			leftReader = new BufferedReader(new InputStreamReader(left.getContents()));
 			rightReader = new BufferedReader(new InputStreamReader(right.getContents()));
 			originReader = new BufferedReader(new InputStreamReader(origin.getContents()));
 
 			final int bufferSize = 16384;
 			final char[] leftBuff = new char[bufferSize];
 			final char[] rightBuff = new char[bufferSize];
 			final char[] originBuff = new char[bufferSize];
 			int readLeft = leftReader.read(leftBuff);
 			int readRight = rightReader.read(rightBuff);
 			int readOrigin = originReader.read(originBuff);
 			while (readLeft > 0 && readRight > 0 && readOrigin > 0
 					&& equalArrays(readLeft, readRight, readOrigin, leftBuff, rightBuff, originBuff)) {
 				readLeft = leftReader.read(leftBuff);
 				readRight = rightReader.read(rightBuff);
 				readOrigin = originReader.read(originBuff);
 			}
 			// One last check in case we've reached the end of one side but not of the other
 			return equalArrays(readLeft, readRight, readOrigin, leftBuff, rightBuff, originBuff);
 		} catch (CoreException e) {
 			// FIXME log
 		} catch (IOException e) {
 			// FIXME log
 		} finally {
 			if (leftReader != null) {
 				Closeables.closeQuietly(leftReader);
 			}
 			if (rightReader != null) {
 				Closeables.closeQuietly(rightReader);
 			}
 			if (originReader != null) {
 				Closeables.closeQuietly(originReader);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether the two arrays contain identical data in the {@code [0:length]} range. Note that we
 	 * won't even check the arrays' contents if {@code length1} is not equal to {@code length2}.
 	 * 
 	 * @param length1
 	 *            Length of the data range to check within {@code array1}.
 	 * @param length2
 	 *            Length of the data range to check within {@code array2}.
 	 * @param array1
 	 *            First of the two arrays which content we need to check.
 	 * @param array2
 	 *            Second of the two arrays which content we need to check.
 	 * @return <code>true</code> if the two given arrays contain identical data in the {@code [0:length]}
 	 *         range.
 	 */
 	private static boolean equalArrays(int length1, int length2, char[] array1, char[] array2) {
 		if (length1 == length2) {
 			boolean result = true;
 			if (array1 == array2) {
 				result = true;
 			} else if (array1 == null || array2 == null) {
 				result = false;
 			} else {
 				for (int i = 0; i < length1 && result; i++) {
 					result = array1[i] == array2[i];
 				}
 			}
 			return result;
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether the three arrays contain identical data in the {@code [0:length]} range. Note that we
 	 * will only check the arrays' contents if {@code length1} is equal to {@code length2} and {@code length3}
 	 * .
 	 * 
 	 * @param length1
 	 *            Length of the data range to check within {@code array1}.
 	 * @param length2
 	 *            Length of the data range to check within {@code array2}.
 	 * @param length3
 	 *            Length of the data range to check within {@code array3}.
 	 * @param array1
 	 *            First of the three arrays which content we need to check.
 	 * @param array2
 	 *            Second of the three arrays which content we need to check.
 	 * @param array3
 	 *            Third of the three arrays which content we need to check.
 	 * @return <code>true</code> if the three given arrays contain identical data in the {@code [0:length]}
 	 *         range.
 	 */
 	private static boolean equalArrays(int length1, int length2, int length3, char[] array1, char[] array2,
 			char[] array3) {
 		if (length1 == length2 && length1 == length3) {
 			boolean result = true;
			if (array1 == array2 && array1 == array2) {
 				result = true;
 			} else if (array1 == null || array2 == null || array3 == null) {
 				result = false;
 			} else {
 				for (int i = 0; i < length1 && result; i++) {
 					result = array1[i] == array2[i] && array1[1] == array3[i];
 				}
 			}
 			return result;
 		}
 		return false;
 	}
 }
