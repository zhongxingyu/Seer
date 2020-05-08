 /******************************************************************************* 
  * Copyright (c) 2009 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.ws.jaxrs.core.preferences;
 
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.core.runtime.preferences.DefaultScope;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.jboss.tools.common.preferences.SeverityPreferences;
 import org.jboss.tools.ws.jaxrs.core.JBossJaxrsCorePlugin;
 
 /**
  * @author Alexey Kazakov
  * @author Xavier Coulon
  */
 public class JaxrsPreferenceInitializer extends AbstractPreferenceInitializer {
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
 	 */
 	@Override
 	public void initializeDefaultPreferences() {
 		IEclipsePreferences defaultPreferences = ((IScopeContext)DefaultScope.INSTANCE).getNode(JBossJaxrsCorePlugin.PLUGIN_ID);
		defaultPreferences.putBoolean(SeverityPreferences.ENABLE_BLOCK_PREFERENCE_NAME, true);
 		defaultPreferences.put(SeverityPreferences.WRONG_BUILDER_ORDER_PREFERENCE_NAME, JaxrsPreferences.ERROR);
 		for (String name : JaxrsPreferences.SEVERITY_OPTION_NAMES) {
 			defaultPreferences.put(name, SeverityPreferences.ERROR);
 		}
 		defaultPreferences.put(JaxrsPreferences.APPLICATION_NO_OCCURRENCE_FOUND, JaxrsPreferences.WARNING);
 		defaultPreferences.put(JaxrsPreferences.APPLICATION_TOO_MANY_OCCURRENCES, JaxrsPreferences.WARNING);
 		defaultPreferences.put(JaxrsPreferences.JAVA_APPLICATION_MISSING_APPLICATION_PATH_ANNOTATION, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.JAVA_APPLICATION_INVALID_TYPE_HIERARCHY, JaxrsPreferences.ERROR);
 
 		defaultPreferences.put(JaxrsPreferences.HTTP_METHOD_MISSING_TARGET_ANNOTATION, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.HTTP_METHOD_INVALID_TARGET_ANNOTATION_VALUE, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.HTTP_METHOD_MISSING_RETENTION_ANNOTATION, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.HTTP_METHOD_INVALID_RETENTION_ANNOTATION_VALUE, JaxrsPreferences.ERROR);
 		
 		defaultPreferences.put(JaxrsPreferences.RESOURCE_METHOD_ILLEGAL_CONTEXT_ANNOTATION, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.RESOURCE_METHOD_INVALID_PATHPARAM_ANNOTATION_VALUE, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.RESOURCE_METHOD_MORE_THAN_ONE_UNANNOTATED_PARAMETER, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.RESOURCE_METHOD_NO_PUBLIC_MODIFIER, JaxrsPreferences.WARNING);
 		defaultPreferences.put(JaxrsPreferences.RESOURCE_METHOD_UNBOUND_PATH_ANNOTATION_TEMPLATE_PARAMETER, JaxrsPreferences.ERROR);
 		defaultPreferences.put(JaxrsPreferences.RESOURCE_METHOD_UNBOUND_PATHPARAM_ANNOTATION_VALUE, JaxrsPreferences.ERROR);
 
 		defaultPreferences.putInt(SeverityPreferences.MAX_NUMBER_OF_MARKERS_PREFERENCE_NAME, SeverityPreferences.DEFAULT_MAX_NUMBER_OF_MARKERS_PER_FILE);
 	}
 }
