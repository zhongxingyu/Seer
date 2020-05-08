 /******************************************************************************* 
  * Copyright (c) 2011 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.ws.jaxrs.ui.preferences;
 
 import org.jboss.tools.common.ui.preferences.SeverityConfigurationBlock.SectionDescription;
 import org.jboss.tools.ws.jaxrs.core.JBossJaxrsCorePlugin;
 import org.jboss.tools.ws.jaxrs.core.preferences.JaxrsPreferences;
 
 /**
  * 
  * @author Alexey Kazakov & Viacheslav Kabanovich
  *
  */
 public class JaxrsValidatorConfigurationBlockDescriptionProvider {
 
 	private static JaxrsValidatorConfigurationBlockDescriptionProvider INSTANCE = null;
 
 	private JaxrsValidatorConfigurationBlockDescriptionProvider() {
 	}
 
 	public static JaxrsValidatorConfigurationBlockDescriptionProvider getInstance() {
 		if(INSTANCE == null) {
 			JaxrsValidatorConfigurationBlockDescriptionProvider q = new JaxrsValidatorConfigurationBlockDescriptionProvider();
 			INSTANCE = q;
 		}
 		return INSTANCE;
 	}
 
 	private SectionDescription SECTION_ACTIVATORS = new SectionDescription(
		JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_section_httpMethods,
 		new String[][]{
 			{JaxrsPreferences.APPLICATION_NO_OCCURRENCE_FOUND, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_applicationNoOccurrenceFound_label},
 			{JaxrsPreferences.APPLICATION_TOO_MANY_OCCURRENCES, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_applicationTooManyOccurrencesFound_label}
 		},
 		JBossJaxrsCorePlugin.PLUGIN_ID
 	);
 	private SectionDescription SECTION_HTTP_METHODS = new SectionDescription(
 			JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_section_httpMethods,
 			new String[][]{
 					{JaxrsPreferences.HTTP_METHOD_MISSING_RETENTION_ANNOTATION, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_httpMethodMissingRetentionAnnotation_label},
 					{JaxrsPreferences.HTTP_METHOD_INVALID_RETENTION_ANNOTATION_VALUE, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_httpMethodInvalidRetentionAnnotationValue_label},
 					{JaxrsPreferences.HTTP_METHOD_MISSING_RETENTION_ANNOTATION, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_httpMethodMissingTargetAnnotation_label},
 					{JaxrsPreferences.HTTP_METHOD_INVALID_RETENTION_ANNOTATION_VALUE, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_httpMethodInvalidTargetAnnotationValue_label},
 			},
 			JBossJaxrsCorePlugin.PLUGIN_ID
 			);
 	private SectionDescription SECTION_RESOURCE_METHODS = new SectionDescription(
 			JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_section_resourceMethods,
 			new String[][]{
 					{JaxrsPreferences.RESOURCE_METHOD_NO_PUBLIC_MODIFIER, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_resourceMethodNoPublicModifier_label},
 					{JaxrsPreferences.RESOURCE_METHOD_UNBOUND_PATH_ANNOTATION_TEMPLATE_PARAMETER, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_resourceMethodUnboundPathAnnotationTemplateParameter_label},
 					{JaxrsPreferences.RESOURCE_METHOD_UNBOUND_PATHPARAM_ANNOTATION_VALUE, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_resourceMethodUnboundPathParameterAnnotationValue_label},
 					{JaxrsPreferences.RESOURCE_METHOD_INVALID_PATHPARAM_ANNOTATION_VALUE, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_resourceMethodInvalidPathParamAnnotationValue_label},
 					{JaxrsPreferences.RESOURCE_METHOD_MORE_THAN_ONE_UNANNOTATED_PARAMETER, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_resourceMethodMoreThanOneUnannotatedParameter_label},
 					{JaxrsPreferences.RESOURCE_METHOD_ILLEGAL_CONTEXT_ANNOTATION, JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_pb_resourceMethodIllegalContextAnnotation_label}
 			},
 			JBossJaxrsCorePlugin.PLUGIN_ID
 			);
 
 	private SectionDescription[] ALL_SECTIONS = new SectionDescription[]{
 			SECTION_ACTIVATORS,
 			SECTION_HTTP_METHODS,
 			SECTION_RESOURCE_METHODS
 	};
 
 	public SectionDescription[] getSections() {
 		return ALL_SECTIONS;
 	}
 
 
 }
