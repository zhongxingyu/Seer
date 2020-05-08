 /*******************************************************************************
  * Copyright (c) 2007-2012 WalWare/StatET-Project (www.walware.de/goto/statet).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Stephan Wahlbrink - initial API and implementation
  *******************************************************************************/
 
 package de.walware.docmlet.tex.internal.ui.preferences;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.core.databinding.observable.set.WritableSet;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ICellEditorListener;
 import org.eclipse.jface.viewers.ICellEditorValidator;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.jface.viewers.ViewerComparator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 
 import de.walware.ecommons.IStatusChangeListener;
 import de.walware.ecommons.databinding.IntegerValidator;
 import de.walware.ecommons.preferences.Preference;
 import de.walware.ecommons.preferences.ui.ManagedConfigurationBlock;
 import de.walware.ecommons.text.ui.settings.IndentSettingsUI;
 import de.walware.ecommons.ui.CombineStatusChangeListener;
 import de.walware.ecommons.ui.components.EditableTextList;
 import de.walware.ecommons.ui.util.LayoutUtil;
 
 import de.walware.docmlet.tex.core.TexCodeStyleSettings;
 import de.walware.docmlet.tex.internal.ui.TexUIPlugin;
 
 
 /**
  * A PreferenceBlock for TexCodeStyleSettings (code formatting preferences).
  */
 public class TexCodeStylePreferenceBlock extends ManagedConfigurationBlock {
 	// in future supporting multiple profiles?
 	// -> we bind to bean not to preferences
 	
 	
 	private class LabelEditing extends EditingSupport {
 		
 		private final TextCellEditor fCellEditor;
 		
 		private final EditableTextList fList;
 		
 		private Object fLast;
 		private IStatusChangeListener fListener;
 		
 		public LabelEditing(final EditableTextList list) {
 			super(list.getViewer());
 			fList = list;
 			fCellEditor = new TextCellEditor(list.getViewer().getTable());
 			fCellEditor.addListener(new ICellEditorListener() {
 				@Override
 				public void editorValueChanged(final boolean oldValidState, final boolean newValidState) {
 					if (fListener == null) {
 						fListener = fStatusListener.newListener();
 					}
 					if (!newValidState) {
 						fListener.statusChanged(new Status(Status.ERROR, TexUIPlugin.PLUGIN_ID,
 								fCellEditor.getErrorMessage() ));
 					}
 					else {
 						fListener.statusChanged(Status.OK_STATUS);
 					}
 				}
 				@Override
 				public void applyEditorValue() {
 					fLast = null;
 					if (fListener != null) {
 						fStatusListener.removeListener(fListener);
 						fListener = null;
 					}
 				}
 				@Override
 				public void cancelEditor() {
 					if (fLast == "") { //$NON-NLS-1$
 						fList.applyChange("", null); //$NON-NLS-1$
 					}
 					if (fListener != null) {
 						fStatusListener.removeListener(fListener);
 						fListener = null;
 					}
 				}
 			});
 			fCellEditor.setValidator(new ICellEditorValidator() {
 				@Override
 				public String isValid(final Object value) {
 					final String s = (String) value;
 					for (int i = 0; i < s.length(); i++) {
 						final char c = s.charAt(i);
 						if (!((c >= 0x41 && c <= 0x5A)
 								|| (c >= 0x61 && c <= 0x7A)
 								|| (c == '*' && i == s.length()-1) )) {
 							return "Invalid environment name";
 						}
 					}
 					return null;
 				}
 			});
 		}
 		
 		@Override
 		protected boolean canEdit(final Object element) {
 			return true;
 		}
 		
 		@Override
 		protected CellEditor getCellEditor(final Object element) {
 			return fCellEditor;
 		}
 		
 		@Override
 		protected Object getValue(final Object element) {
 			fLast = element;
 			return element;
 		}
 		
 		@Override
 		protected void setValue(final Object element, final Object value) {
 			if (value != null) {
 				fList.applyChange(element, (value != "") ? value : null); //$NON-NLS-1$
 			}
 		}
 		
 	}
 	
 	
 	private TexCodeStyleSettings fModel;
 	
 	private IndentSettingsUI fStdIndentSettings;
 	private Text fIndentBlockDepthControl;
 	private Text fIndentEnvDepthControl;
 	private EditableTextList fIndentEnvLabelsControl;
 	
 	private final CombineStatusChangeListener fStatusListener;
 	
 	
 	public TexCodeStylePreferenceBlock(final IProject project, final IStatusChangeListener statusListener) {
 		super(project);
 		fStatusListener = new CombineStatusChangeListener(statusListener);
 		setStatusListener(fStatusListener);
 	}
 	
 	
 	@Override
 	protected void createBlockArea(final Composite pageComposite) {
 		final Map<Preference<?>, String> prefs = new HashMap<Preference<?>, String>();
 		
 		prefs.put(TexCodeStyleSettings.TAB_SIZE_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.INDENT_DEFAULT_TYPE_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.INDENT_SPACES_COUNT_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.REPLACE_CONVERSATIVE_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.REPLACE_TABS_WITH_SPACES_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.INDENT_BLOCK_DEPTH_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.INDENT_ENV_DEPTH_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		prefs.put(TexCodeStyleSettings.INDENT_ENV_LABELS_PREF, TexCodeStyleSettings.INDENT_GROUP_ID);
 		
 		setupPreferenceManager(prefs);
 		
 		fModel = new TexCodeStyleSettings(0);
 		fStdIndentSettings = new IndentSettingsUI();
 		
 		final Composite mainComposite = new Composite(pageComposite, SWT.NONE);
 		mainComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		mainComposite.setLayout((LayoutUtil.applyCompositeDefaults(new GridLayout(), 2)));
 		
 		final TabFolder folder = new TabFolder(mainComposite, SWT.NONE);
 		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		
 		{	final TabItem item = new TabItem(folder, SWT.NONE);
 			item.setText(fStdIndentSettings.getGroupLabel());
 			item.setControl(createIndentControls(folder));
 		}
 		{	final TabItem item = new TabItem(folder, SWT.NONE);
 			item.setText("&Line Wrapping");
 			item.setControl(createLineControls(folder));
 		}
 		
 		initBindings();
 		updateControls();
 	}
 	
 	private Control createIndentControls(final Composite parent) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(LayoutUtil.applyTabDefaults(new GridLayout(), 2));
 		
 		fStdIndentSettings.createControls(composite);
 		LayoutUtil.addSmallFiller(composite, false);
 		
 		final Composite depthComposite = new Composite(composite, SWT.NONE);
 		depthComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
 		depthComposite.setLayout(LayoutUtil.applyCompositeDefaults(new GridLayout(), 4));
 		fIndentBlockDepthControl = createIndentDepthLine(depthComposite, Messages.CodeStyle_Indent_IndentInBlocks_label);
 		fIndentEnvDepthControl = createIndentDepthLine(depthComposite, Messages.CodeStyle_Indent_IndentInEnvs_label);
 		
 		{	final Label label = new Label(depthComposite, SWT.NONE);
 			final GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 4, 1);
 			gd.horizontalIndent = LayoutUtil.defaultIndent();
 			label.setLayoutData(gd);
			label.setText("Environments to be intended:");
 		}
 		fIndentEnvLabelsControl = new EditableTextList();
 		{	final Control control = fIndentEnvLabelsControl.create(depthComposite, new ViewerComparator());
 			final GridData gd = new GridData(SWT.FILL, SWT.FILL, false, true, 3, 1);
 			gd.horizontalIndent = LayoutUtil.defaultIndent();
 			control.setLayoutData(gd);
 			LayoutUtil.addGDDummy(depthComposite, true);
 		}
 		fIndentEnvLabelsControl.getColumn().setEditingSupport(new LabelEditing(fIndentEnvLabelsControl));
 		
 		LayoutUtil.addSmallFiller(depthComposite, false);
 		
 		LayoutUtil.addSmallFiller(composite, false);
 		return composite;
 	}
 	
 	private Text createIndentDepthLine(final Composite composite, final String label) {
 		final Label labelControl = new Label(composite, SWT.LEFT);
 		labelControl.setText(label);
 		labelControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		final Text textControl = new Text(composite, SWT.RIGHT | SWT.SINGLE | SWT.BORDER);
 		final GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, false);
 		gd.widthHint = LayoutUtil.hintWidth(textControl, 2);
 		textControl.setLayoutData(gd);
 		final Label typeControl = new Label(composite, SWT.LEFT);
 		typeControl.setText(fStdIndentSettings.getLevelUnitLabel());
 		typeControl.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
 		
 		LayoutUtil.addGDDummy(composite);
 		
 		return textControl;
 	}
 	
 	private Control createLineControls(final Composite parent) {
 		final Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(LayoutUtil.applyTabDefaults(new GridLayout(), 2));
 		
 		fStdIndentSettings.addLineWidth(composite);
 		
 		return composite;
 	}
 	
 	@Override
 	protected void addBindings(final DataBindingContext dbc, final Realm realm) {
 		fStdIndentSettings.addBindings(dbc, realm, fModel);
 		
 		dbc.bindValue(SWTObservables.observeText(fIndentBlockDepthControl, SWT.Modify),
 				BeansObservables.observeValue(realm, fModel, TexCodeStyleSettings.INDENT_BLOCK_DEPTH_PROP),
 				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.CodeStyle_Indent_IndentInBlocks_error_message)),
 				null);
 		dbc.bindValue(SWTObservables.observeText(fIndentEnvDepthControl, SWT.Modify),
 				BeansObservables.observeValue(realm, fModel, TexCodeStyleSettings.INDENT_ENV_DEPTH_PROP),
 				new UpdateValueStrategy().setAfterGetValidator(new IntegerValidator(1, 10, Messages.CodeStyle_Indent_IndentInEnvs_error_message)),
 				null);
 		
 		final WritableSet labels = new WritableSet(realm);
 		fIndentEnvLabelsControl.setInput(labels);
 		dbc.bindSet(labels, BeansObservables.observeSet(realm, fModel, TexCodeStyleSettings.INDENT_ENV_LABELS_PROP),
 				null, null );
 	}
 	
 	@Override
 	protected void updateControls() {
 		fModel.load(this);
 		fModel.resetDirty();
 		getDbc().updateTargets();  // required for invalid target values
 		fIndentEnvLabelsControl.refresh();
 	}
 	
 	@Override
 	protected void updatePreferences() {
 		if (fModel.isDirty()) {
 			fModel.resetDirty();
 			setPrefValues(fModel.toPreferencesMap());
 		}
 	}
 	
 }
