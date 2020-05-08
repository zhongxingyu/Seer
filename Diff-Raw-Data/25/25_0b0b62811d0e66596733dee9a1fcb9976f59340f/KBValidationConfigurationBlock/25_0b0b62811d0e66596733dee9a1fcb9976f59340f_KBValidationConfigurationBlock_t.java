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
 package org.jboss.tools.jst.web.kb.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
 import org.jboss.tools.common.ui.preferences.SeverityConfigurationBlock;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 
 /**
  * @author Viacheslav Kabanovich
  */
 public class KBValidationConfigurationBlock extends SeverityConfigurationBlock {
 
 	private static final String SETTINGS_SECTION_NAME = "KBValidationConfigurationBlock"; //$NON-NLS-1$
 
 	private static SectionDescription SECTION_KB_CONFIG = new SectionDescription(
 			KBPreferencesMessages.KBValidationConfigurationBlock_section_config,
 		new String[][]{
 			{KBSeverityPreferences.REQUIRED_KB_CAPABILITIES_ARE_MISSING, KBPreferencesMessages.KBValidationConfigurationBlock_pb_requiredKbCapabilitiesAreMissing_label},
 			{KBSeverityPreferences.KB_CAPABILITIES_ARE_NOT_ENABLED_IN_JAVA_MODULE, KBPreferencesMessages.KBValidationConfigurationBlock_pb_kbCapabilitiesAreNotEnabledInJavaModule_label},
 		},
 		WebKbPlugin.PLUGIN_ID
 	);
 
 	private static SectionDescription[] ALL_SECTIONS = new SectionDescription[]{
 		SECTION_KB_CONFIG,
 	};
 
 	private static Key[] getKeys() {
 		ArrayList<Key> keys = new ArrayList<Key>();
 		for (SectionDescription s: ALL_SECTIONS) {
 			s.collectKeys(keys);
 		}
 		return keys.toArray(new Key[0]);
 	}
 
 	@Override
 	protected Key getMaxNumberOfProblemsKey() {
 		return null;
 	}
 
 	public KBValidationConfigurationBlock(IStatusChangeListener context,
 			IProject project, IWorkbenchPreferenceContainer container) {
 		super(context, project, getKeys(), container);
 	}
 
 	@Override
 	protected SectionDescription[] getAllSections() {
 		return ALL_SECTIONS;
 	}
 
 	@Override
 	protected String getCommonDescription() {
 		return KBPreferencesMessages.KBValidationConfigurationBlock_common_description;
 	}
 
 	@Override
 	protected IDialogSettings getDialogSettings() {
 		return WebKbPlugin.getDefault().getDialogSettings().getSection(SETTINGS_SECTION_NAME);
 	}

	@Override
	protected String getQualifier() {
		return WebKbPlugin.PLUGIN_ID;
	}
 }
