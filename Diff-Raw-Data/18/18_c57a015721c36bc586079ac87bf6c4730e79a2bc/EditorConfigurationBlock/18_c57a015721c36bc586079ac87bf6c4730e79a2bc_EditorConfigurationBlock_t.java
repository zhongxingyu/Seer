 /**
  * 
  */
 package org.eclipse.dltk.ui.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.dltk.ui.CodeFormatterConstants;
 import org.eclipse.dltk.ui.PreferenceConstants;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 
 public class EditorConfigurationBlock extends AbstractConfigurationBlock {
 	private boolean smartDisabled = false;
 	public EditorConfigurationBlock(PreferencePage mainPreferencePage, OverlayPreferenceStore store) {
 		super(store, mainPreferencePage);
 		getPreferenceStore().addKeys(createOverlayStoreKeys());
 	}
 	public EditorConfigurationBlock(PreferencePage mainPreferencePage, OverlayPreferenceStore store, boolean disableSmart) {
 		super(store, mainPreferencePage);
 		getPreferenceStore().addKeys(createOverlayStoreKeys());
 		this.smartDisabled = disableSmart;
 	}
 	
 	private OverlayPreferenceStore.OverlayKey[] createOverlayStoreKeys() {
 		ArrayList overlayKeys= new ArrayList();
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SMART_HOME_END));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.EDITOR_SMART_INDENT));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, CodeFormatterConstants.FORMATTER_TAB_CHAR));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CodeFormatterConstants.FORMATTER_TAB_SIZE));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, CodeFormatterConstants.FORMATTER_INDENTATION_SIZE));
 		
 		OverlayPreferenceStore.OverlayKey[] keys= new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
 		overlayKeys.toArray(keys);
 		return keys;
 	}
 
 	/**
 	 * Creates page for appearance preferences.
 	 * 
 	 * @param parent the parent composite
 	 * @return the control for the preference page
 	 */
 	public Control createControl(Composite parent) {
 		initializeDialogUnits(parent);
 
 		Composite control = new Composite(parent, SWT.NONE);
 		control.setLayout(new GridLayout());
 		
 		if( !smartDisabled ) {
 			Composite composite;
 
 			composite = createSubsection(control,null, PreferencesMessages.EditorPreferencePage_title0);
 			createSettingsGroup(composite);
 		}
 		
 		createTabsGroup(control);
 
 		return control;
 	}
 	
 	
 	private void createTabsGroup(Composite composite) {
 		Composite generalGroup = createSubsection(
 				composite,
 				null,
 				FormatterMessages.IndentationTabPage_preview_header);
 		
 		GridLayout layout = new GridLayout();
 		layout.numColumns= 2;
 		generalGroup.setLayout(layout);
 		
 		final String[] tabPolicyValues= new String[] {
 				CodeFormatterConstants.SPACE, CodeFormatterConstants.TAB, CodeFormatterConstants.MIXED};
 		final String[] tabPolicyLabels= new String[] {
 				FormatterMessages.IndentationTabPage_general_group_option_tab_policy_SPACE, 
 				FormatterMessages.IndentationTabPage_general_group_option_tab_policy_TAB, 
 				FormatterMessages.IndentationTabPage_general_group_option_tab_policy_MIXED
 		};
 		
 		addComboBox(generalGroup, FormatterMessages.IndentationTabPage_general_group_option_tab_policy, 
 				CodeFormatterConstants.FORMATTER_TAB_CHAR, tabPolicyLabels, tabPolicyValues);
 		
 		addLabelledTextField(generalGroup, FormatterMessages.IndentationTabPage_general_group_option_indent_size, 
 				CodeFormatterConstants.FORMATTER_INDENTATION_SIZE, 2, 1, true);
 		
 		addLabelledTextField(generalGroup, FormatterMessages.IndentationTabPage_general_group_option_tab_size, 
 				CodeFormatterConstants.FORMATTER_TAB_SIZE, 2, 1, true);
 	}	
 	    
     private Control createSettingsGroup(Composite composite) {
 		GridLayout layout= new GridLayout();
 		layout.numColumns= 2;
 		composite.setLayout(layout);
 
 		String label;
 		
 		label= PreferencesMessages.EditorPreferencePage_smartHomeEnd; 
 		addCheckBox(composite, label, PreferenceConstants.EDITOR_SMART_HOME_END, 0);
 		
 		label= PreferencesMessages.EditorPreferencePage_subWordNavigation; 
 		addCheckBox(composite, label, PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION, 0);
 		
 		label= PreferencesMessages.EditorPreferencePage_smartIndent; 
 		addCheckBox(composite, label, PreferenceConstants.EDITOR_SMART_INDENT, 0);			
 
 		return composite;
 	}
 	
 	public void initialize() {
 
 		super.initialize();
 		
 	}
 	
 	public void performDefaults() {
 		super.performDefaults();
 
 	}
 }
 
