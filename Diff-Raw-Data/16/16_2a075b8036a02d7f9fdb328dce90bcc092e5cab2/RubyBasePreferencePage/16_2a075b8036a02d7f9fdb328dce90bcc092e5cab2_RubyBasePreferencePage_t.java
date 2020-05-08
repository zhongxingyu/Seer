 package org.rubypeople.rdt.internal.ui.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.rubypeople.rdt.internal.ui.RubyUIMessages;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.ui.PreferenceConstants;
 
 public class RubyBasePreferencePage extends RubyAbstractPreferencePage implements IWorkbenchPreferencePage {
 
 	public RubyBasePreferencePage() {
 		
 		setDescription(RubyUIMessages.getString("RubyBasePreferencePage.label")); //$NON-NLS-1$
 		setPreferenceStore(RubyPlugin.getDefault().getPreferenceStore());
 		fOverlayStore = createOverlayStore();
 	}
 
 	private OverlayPreferenceStore createOverlayStore() {
 
 		ArrayList overlayKeys = new ArrayList();
		
		//	overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.CREATE_PARSER_ANNOTATIONS));
 
 		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
 		overlayKeys.toArray(keys);
 		return new OverlayPreferenceStore(getPreferenceStore(), keys);
 	}
 
 	public void init(IWorkbench workbench) {}
 
 	protected Control createContents(Composite parent) {
 		fOverlayStore.load();
 		fOverlayStore.start();
 		
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		composite.setLayout(layout);
 
 		this.initializeFields() ;
 		return composite;
 	}
 
 	/*
 	 * @see PreferencePage#performOk()
 	 */
 	public boolean performOk() {
 
 		fOverlayStore.propagate();
 		RubyPlugin.getDefault().savePluginPreferences();
 		return true;
 	}
 
 	/*
 	 * @see PreferencePage#performDefaults()
 	 */
 	protected void performDefaults() {
 
 		fOverlayStore.loadDefaults();
 		this.initializeFields() ;
 		super.performDefaults();
 	}
 }
