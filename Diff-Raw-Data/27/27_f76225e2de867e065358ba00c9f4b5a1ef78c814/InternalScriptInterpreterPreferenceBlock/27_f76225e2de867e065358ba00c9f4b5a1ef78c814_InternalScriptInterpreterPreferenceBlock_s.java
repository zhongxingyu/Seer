 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.IInterpreterInstallType;
 import org.eclipse.dltk.launching.ScriptRuntime;
 import org.eclipse.dltk.ui.preferences.ComboViewerBlock;
 import org.eclipse.dltk.ui.preferences.ImprovedAbstractConfigurationBlock;
 import org.eclipse.dltk.ui.preferences.OverlayPreferenceStore;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 
 /**
  * Preference block that can be used to select an installed interpreter for
  * 'internal' editor, etc use.
  */
 public abstract class InternalScriptInterpreterPreferenceBlock extends
 		ImprovedAbstractConfigurationBlock {
 
 	private ComboViewerBlock viewer;
 
 	public InternalScriptInterpreterPreferenceBlock(
 			OverlayPreferenceStore store, PreferencePage page) {
 		super(store, page);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.IPreferenceConfigurationBlock#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	public final Control createControl(Composite parent) {
 		Composite composite = SWTFactory.createComposite(parent, parent
 				.getFont(), 1, 1, GridData.FILL);
 
 		Group group = SWTFactory.createGroup(composite,
 				getSelectorGroupLabel(), 1, 1, GridData.FILL_HORIZONTAL);
 
 		SWTFactory.createLabel(group, getSelectorNameLabel(), 1);
 
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		viewer = new ComboViewerBlock(group, gd) {
 			protected String getObjectName(Object element) {
 				return ((IInterpreterInstall) element).getName();
 			}
 
 			protected void selectedObjectChanged(Object element) {
 				setString(getPreferenceKey(), getObjectId(element));
 			}
 
 			protected Object getDefaultObject() {
 				return getDefaultSelectedInterpreter();
 			}
 
 			protected String getObjectId(Object element) {
 				return ScriptRuntime
 						.getCompositeIdFromInterpreter((IInterpreterInstall) element);
 			}
 
 			protected String getSavedObjectId() {
 				return getString(getPreferenceKey());
 			}
 
 			protected Object getObjectById(String id) {
 				return ScriptRuntime.getInterpreterFromCompositeId(id);
 			}
 		};
 
 		viewer.initialize(getInterpreterInstalls());
 
 		return composite;
 	}
 
 	/**
 	 * Returns the language's nature id.
 	 */
 	protected abstract String getNatureId();
 
 	/**
 	 * Returns the preference key that will be used to store the interpreter
 	 * preference.
 	 */
 	protected abstract String getPreferenceKey();
 
 	/**
 	 * Returns the label that will be used for the selector group.
 	 */
 	protected abstract String getSelectorGroupLabel();
 
 	/**
 	 * Returns the label that will be used for the selector name.
 	 */
 	protected abstract String getSelectorNameLabel();
 
 	/**
	 * Returns the {@link IInterpreterInstall} that will be auto-selected if 
	 * an interpreter id is not found in the preference store.
	 * 
	 * <p>Subclasses may return <code>null</code> if they do not wish to auto
	 * select an interpreter or if no interpreters are installed.</p>
 	 */
	protected abstract Object getDefaultSelectedInterpreter();
 
 	/*
 	 * @see org.eclipse.dltk.ui.preferences.ImprovedAbstractConfigurationBlock#createOverlayKeys()
 	 */
 	protected List createOverlayKeys() {
 		ArrayList keys = new ArrayList(1);
 
 		keys.add(new OverlayPreferenceStore.OverlayKey(
 				OverlayPreferenceStore.STRING, getPreferenceKey()));
 
 		return keys;
 	}
 
 	private IInterpreterInstall[] getInterpreterInstalls() {
 		List interpreters = new ArrayList();
 		IInterpreterInstallType[] types = ScriptRuntime
 				.getInterpreterInstallTypes(getNatureId());
 		for (int i = 0; i < types.length; i++) {
 			IInterpreterInstall[] installs = types[i].getInterpreterInstalls();
 			for (int j = 0; j < installs.length; j++) {
 				interpreters.add(installs[j]);
 			}
 		}
 
 		return (IInterpreterInstall[]) interpreters
 				.toArray(new IInterpreterInstall[interpreters.size()]);
 	}
 }
