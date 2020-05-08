  /*******************************************************************************
   * Copyright (c) 2009 Red Hat, Inc.
   * Distributed under license by Red Hat, Inc. All rights reserved.
   * This program is made available under the terms of the
   * Eclipse Public License v1.0 which accompanies this distribution,
   * and is available at http://www.eclipse.org/legal/epl-v10.html
   *
   * Contributors:
   *     Red Hat, Inc. - initial API and implementation
   ******************************************************************************/
 package org.jboss.tools.jst.web.kb;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author Alexey Kazakov
  */
 public class KbMessages {
	private static final String BUNDLE_NAME = "org.jboss.tools.jst.web.kb.KbMessages"; //$NON-NLS-1$
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, KbMessages.class);
 	}
 
 	public static String VALIDATION_CONTEXT_LINKED_RESOURCE_PATH_MUST_NOT_BE_NULL;
 	public static String VALIDATION_CONTEXT_VARIABLE_NAME_MUST_NOT_BE_NULL;
 	public static String KBNATURE_NOT_FOUND;
 	public static String KBBUILDER_NOT_FOUND;
 	public static String KBPROBLEM;
 	public static String ENABLE_KB;
 	public static String ILLEGAL_CONTENTTYPE;
 }
