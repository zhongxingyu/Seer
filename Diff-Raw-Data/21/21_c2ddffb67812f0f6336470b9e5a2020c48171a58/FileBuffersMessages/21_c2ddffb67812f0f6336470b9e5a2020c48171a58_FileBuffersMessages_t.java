 /*******************************************************************************
  * Copyright (c) 2000, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.core.internal.filebuffers;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * Helper class to get NLSed messages.
  * 
 * <p>
 * NOTE: This class and all its fields are not considered public.
 *       WILL BE MADE PACKAGE VISIBLE FOR 3.1.
 * </p>
 * 
  * @since 3.0
  */
public final class FileBuffersMessages extends NLS {
 
 	private static final String BUNDLE_NAME= FileBuffersMessages.class.getName();
 
 	private FileBuffersMessages() {
 		// Do not instantiate
 	}
 
 	public static String ExtensionsRegistry_error_extensionPointNotFound;
 	public static String ExtensionsRegistry_error_contentTypeDoesNotExist;
 	public static String ResourceFileBuffer_error_fileDoesNotExist;
 	public static String FileBuffer_error_outOfSync;
 	public static String FileBuffer_status_ok;
 	public static String FileBuffer_status_error;
 	public static String FileBuffer_error_queryContentDescription;
 	public static String FileBufferManager_error_canNotCreateFilebuffer;
 	public static String ResourceTextFileBuffer_error_unsupported_encoding_message_arg;
 	public static String ResourceTextFileBuffer_task_saving;
 	public static String ResourceFileBuffer_task_creatingFileBuffer;
 	public static String JavaTextFileBuffer_error_closeStream;
 	public static String TextFileBufferManager_error_documentSetupFailed;
 	public static String TextFileBufferManager_error_documentFactoryFailed;
 	public static String DocumentInputStream_error_read;
 	public static String DocumentInputStream_error_streamClosed;
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, FileBuffersMessages.class);
 	}
 }
