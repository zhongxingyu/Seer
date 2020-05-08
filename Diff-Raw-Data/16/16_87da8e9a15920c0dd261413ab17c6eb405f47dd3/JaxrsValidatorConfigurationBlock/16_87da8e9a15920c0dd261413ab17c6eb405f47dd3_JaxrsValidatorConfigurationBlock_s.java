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
 package org.jboss.tools.ws.jaxrs.ui.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 import org.jboss.tools.common.preferences.SeverityPreferences;
 import org.jboss.tools.common.ui.preferences.SeverityConfigurationBlock;
 import org.jboss.tools.ws.jaxrs.core.JBossJaxrsCorePlugin;
 
 /**
  * @author Alexey Kazakov
  * @author Xavier Coulon
  */
 @SuppressWarnings("restriction")
 public class JaxrsValidatorConfigurationBlock extends SeverityConfigurationBlock {
 
 	private static final String SETTINGS_SECTION_NAME = "JaxrsValidatorConfigurationBlock";
 
 	private static Key[] getKeys() {
 		ArrayList<Key> keys = new ArrayList<Key>();
 		for (SectionDescription s: JaxrsValidatorConfigurationBlockDescriptionProvider.getInstance().getSections()) {
 			s.collectKeys(keys);
 		}
 		keys.add(MAX_NUMBER_OF_PROBLEMS_KEY);
 		keys.add(WRONG_BUILDER_ORDER_KEY);
 		return keys.toArray(new Key[0]);
 	}
 
 	private static final Key MAX_NUMBER_OF_PROBLEMS_KEY = getKey(JBossJaxrsCorePlugin.PLUGIN_ID, SeverityPreferences.MAX_NUMBER_OF_MARKERS_PREFERENCE_NAME);
 
 	@Override
 	protected Key getMaxNumberOfProblemsKey() {
 		return MAX_NUMBER_OF_PROBLEMS_KEY;
 	}
 
 	private static final Key WRONG_BUILDER_ORDER_KEY = getKey(JBossJaxrsCorePlugin.PLUGIN_ID, SeverityPreferences.WRONG_BUILDER_ORDER_PREFERENCE_NAME);
 
 	protected Key getWrongBuilderOrderKey() {
 		return WRONG_BUILDER_ORDER_KEY;
 	}
 
 	@SuppressWarnings("restriction")
 	public JaxrsValidatorConfigurationBlock(IStatusChangeListener context,
 			IProject project, IWorkbenchPreferenceContainer container) {
 		super(context, project, getKeys(), container);
 	}
 
 	@Override
 	protected SectionDescription[] getAllSections() {
 		return JaxrsValidatorConfigurationBlockDescriptionProvider.getInstance().getSections();
 	}
 
 	@Override
 	protected String getCommonDescription() {
 		return JaxrsPreferencesMessages.JaxrsValidatorConfigurationBlock_common_description;
 	}
 
 	@Override
 	protected IDialogSettings getDialogSettings() {
 		return JBossJaxrsCorePlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
 	}
 
 	@Override
 	protected String getQualifier() {
 		return JBossJaxrsCorePlugin.PLUGIN_ID;
 	}
 }
