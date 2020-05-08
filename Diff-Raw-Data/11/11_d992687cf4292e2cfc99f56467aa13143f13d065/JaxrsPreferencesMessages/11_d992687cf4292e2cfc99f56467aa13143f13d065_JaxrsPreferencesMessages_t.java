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
 
 package org.jboss.tools.ws.jaxrs.ui.preferences;
 
 import org.eclipse.osgi.util.NLS;
 
 /**
  * @author Alexey Kazakov
  * @author Xavier Coulon
  */
 public class JaxrsPreferencesMessages extends NLS {
 	private static final String BUNDLE_NAME = "org.jboss.tools.ws.jaxrs.ui.preferences.JaxrsPreferencesMessages"; //$NON-NLS-1$
 
 	public static String JAXRS_SETTINGS_PREFERENCE_PAGE_JAXRS_SUPPORT;
 
 	// Validator Preference page
 	public static String JaxrsValidatorConfigurationBlock_common_description;
 	
 	public static String JaxrsValidatorConfigurationBlock_needsbuild_title;
 	public static String JaxrsValidatorConfigurationBlock_needsfullbuild_message;
 	public static String JaxrsValidatorConfigurationBlock_needsprojectbuild_message;
 
 	// Section Application/Activators
 	public static String JaxrsValidatorConfigurationBlock_section_applications ;
 	public static String JaxrsValidatorConfigurationBlock_pb_applicationNoOccurrenceFound_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_applicationTooManyOccurrencesFound_label;
	public static String JaxrsValidatorConfigurationBlock_pb_applicationMissingApplicationPathAnnotation_label;
	public static String JaxrsValidatorConfigurationBlock_pb_applicationInvalidTypeHierarchy_label;
 
 	// Section HTTP Method
 	public static String JaxrsValidatorConfigurationBlock_section_httpMethods;
 	public static String JaxrsValidatorConfigurationBlock_pb_httpMethodMissingRetentionAnnotation_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_httpMethodInvalidRetentionAnnotationValue_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_httpMethodMissingTargetAnnotation_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_httpMethodInvalidTargetAnnotationValue_label;
 	
 	// Section Resource 
 	
 	// Section Resource Methods
  	public static String JaxrsValidatorConfigurationBlock_section_resourceMethods; 
 	public static String JaxrsValidatorConfigurationBlock_pb_resourceMethodIllegalContextAnnotation_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_resourceMethodUnboundPathParameterAnnotationValue_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_resourceMethodUnboundPathAnnotationTemplateParameter_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_resourceMethodMoreThanOneUnannotatedParameter_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_resourceMethodInvalidPathParamAnnotationValue_label;
 	public static String JaxrsValidatorConfigurationBlock_pb_resourceMethodNoPublicModifier_label;
 
 	// Section Resource Fields
 
 	public static String JAXRS_VALIDATOR_PREFERENCE_PAGE_JAXRS_VALIDATOR;
 
 	static {
 		NLS.initializeMessages(BUNDLE_NAME, JaxrsPreferencesMessages.class);
 	}
 }
