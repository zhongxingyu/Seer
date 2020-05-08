 /*******************************************************************************
  * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.views.internal;
 
 /**
  * TCF Console Plug-in Image registry constants.
  */
 public interface ImageConsts {
 
 	// ***** The directory structure constants *****
 
 	/**
 	 * The root directory where to load the images from, relative to
 	 * the bundle directory.
 	 */
     public final static String  IMAGE_DIR_ROOT = "icons/"; //$NON-NLS-1$
 
     /**
      * The directory where to load disabled local toolbar images from,
      * relative to the image root directory.
      */
     public final static String  IMAGE_DIR_DLCL = "dlcl16/"; //$NON-NLS-1$
 
     /**
      * The directory where to load enabled local toolbar images from,
      * relative to the image root directory.
      */
     public final static String  IMAGE_DIR_ELCL = "elcl16/"; //$NON-NLS-1$
 
     /**
      * The directory where to load model object images from,
      * relative to the image root directory.
      */
     public final static String  IMAGE_DIR_OBJ = "obj16/"; //$NON-NLS-1$
 
     /**
      * The directory where to load object overlay images from,
      * relative to the image root directory.
      */
     public final static String  IMAGE_DIR_OVR = "ovr16/"; //$NON-NLS-1$
 
     /**
      * The directory where to load disabled toolbar images from,
      * relative to the image root directory.
      */
     public final static String  IMAGE_DIR_DTOOL = "dtool16/"; //$NON-NLS-1$
 
     /**
      * The directory where to load enabled toolbar images from,
      * relative to the image root directory.
      */
     public final static String  IMAGE_DIR_ETOOL = "etool16/"; //$NON-NLS-1$
 
     // ***** The image constants *****
 
     /**
      * The key to access the Script Pad console image.
      */
     public static final String SCRIPT_PAD_CONSOLE = "ScriptPadConsole"; //$NON-NLS-1$
 
     /**
      * The key to access the Monitor console image.
      */
     public static final String MONITOR_CONSOLE = "MonitorConsole"; //$NON-NLS-1$
 }
