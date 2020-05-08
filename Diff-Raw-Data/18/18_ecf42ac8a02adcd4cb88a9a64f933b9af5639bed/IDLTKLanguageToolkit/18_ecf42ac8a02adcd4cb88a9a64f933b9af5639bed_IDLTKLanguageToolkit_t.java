 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.dltk.core;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.dltk.core.DLTKFeatures.BooleanFeature;
 import org.eclipse.dltk.core.DLTKFeatures.IntegerFeature;
 import org.eclipse.dltk.core.DLTKFeatures.StringFeature;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 
 public interface IDLTKLanguageToolkit {
 
 	/**
 	 * Return content type associated with this language. DLTK will check all
 	 * derived content types to detect this language model elements.
 	 */
 	String getLanguageContentType();
 
 	boolean validateSourcePackage(IPath path, IEnvironment environment);
 
 	/**
 	 * Preliminary resource validation. This method is here to give the language
 	 * toolkit the chances to skip this resource.
 	 * 
 	 * @param resource
 	 * @return {@link IStatus#OK} if this resource should be processed or other
 	 *         value to skip this resource.
 	 */
 	IStatus validateSourceModule(IResource resource);
 
 	boolean languageSupportZIPBuildpath();
 
 	String getNatureId();
 
 	String getLanguageName();
 
 	/**
 	 * Tests if is is allowed to determine ContentType from content for the
 	 * specified resource.
 	 * 
 	 * @param resource
 	 * @return
 	 */
 	boolean canValidateContent(IResource resource);
 
 	/**
 	 * Tests if it is allowed to determine ContentType from content for the
 	 * specified local file.
 	 * 
 	 * @param file
 	 * @return
 	 */
 	boolean canValidateContent(File file);
 
 	/**
 	 * Tests if it is allowed to determine ContentType from content for the
 	 * specified remote file.
 	 * 
 	 * @param file
 	 * @return
 	 */
 	boolean canValidateContent(IFileHandle file);
 
 	/**
 	 * Return the qualifier for the "core" language preferences. It should match
 	 * plugin-id most of the time.
 	 * 
 	 * @return
 	 */
 	String getPreferenceQualifier();
 
 	boolean get(BooleanFeature feature);
 
 	int get(IntegerFeature feature);
 
 	String get(StringFeature feature);
 
 	/**
 	 * @param archiveProjectFragment
 	 * @throws IOException
 	 * @since 2.0
 	 */
	IArchive openArchive(File localFile) throws IOException;
 
 }
