 /**
 * IModuleLoadDataProperties.java
  * Created on Jul 2, 2013
  *
  * Copyright (c) 2013 Wind River Systems, Inc.
  *
  * The right to copy, distribute, modify, or otherwise make use
  * of this software may be licensed only pursuant to the terms
  * of an applicable Wind River license agreement.
  */
 package org.eclipse.tcf.te.tcf.locator.interfaces;
 
 /**
 * Keys for module load data.
  */
 public interface IStepAttributes {
 
 	/**
 	 * Define the prefix used by all other attribute id's as prefix.
 	 */
 	public static final String ATTR_PREFIX = "org.eclipse.tcf.te.tcf.locator"; //$NON-NLS-1$
 
 	/**
 	 * Marker for AttachDebuggerStep if the debugger should be attached or not to the active context.
 	 */
 	public static final String ATTR_START_DEBUGGER = ATTR_PREFIX + ".start_debugger"; //$NON-NLS-1$
 }
