 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.rubypeople.rdt.internal.ui.preferences;
 
 import java.util.ArrayList;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.internal.ui.actions.StatusInfo;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.preference.ColorFieldEditor;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.TabFolder;
 import org.eclipse.swt.widgets.TabItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.editors.text.ITextEditorHelpContextIds;
 import org.eclipse.ui.help.WorkbenchHelp;
 import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.RubyUIMessages;
 import org.rubypeople.rdt.internal.ui.text.IRubyColorConstants;
 import org.rubypeople.rdt.ui.PreferenceConstants;
 
 /**
  * The preference page for setting the editor options.
  * <p>
  * This class is internal and not intended to be used by clients.
  * </p>
  * 
  * @since 2.1
  */
 public class TextEditorPreferencePage2 extends RubyAbstractPreferencePage implements IWorkbenchPreferencePage {
 
 	private final String[][] fAppearanceColorListModel = new String[][] { { RubyUIMessages.getString("TextEditorPreferencePage.lineNumberForegroundColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR}, //$NON-NLS-1$
 			{ RubyUIMessages.getString("TextEditorPreferencePage.currentLineHighlighColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR}, //$NON-NLS-1$
 			{ RubyUIMessages.getString("TextEditorPreferencePage.printMarginColor"), AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR}, //$NON-NLS-1$
 	};
 
 	protected TextPropertyWidget[] textPropertyWidgets;
 	protected Text indentationWidget;
 	protected final String[] colorProperties = { IRubyColorConstants.RUBY_KEYWORD, IRubyColorConstants.RUBY_MULTI_LINE_COMMENT, IRubyColorConstants.RUBY_SINGLE_LINE_COMMENT, IRubyColorConstants.RUBY_STRING, IRubyColorConstants.TASK_TAG, IRubyColorConstants.RUBY_REGEXP, IRubyColorConstants.RUBY_COMMAND, IRubyColorConstants.RUBY_DEFAULT};
 
 	//private final String[][] fAnnotationColorListModel;
 
 	private ModifyListener fTextFieldListener = new ModifyListener() {
 
 		public void modifyText(ModifyEvent e) {
 			Text text = (Text) e.widget;
 			fOverlayStore.setValue((String) fTextFields.get(text), text.getText());
 		}
 	};
 
 	private ArrayList fNumberFields = new ArrayList();
 	private ModifyListener fNumberFieldListener = new ModifyListener() {
 
 		public void modifyText(ModifyEvent e) {
 			numberFieldChanged((Text) e.widget);
 		}
 	};
 
 	private List fAppearanceColorList;
 	private ColorEditor fAppearanceColorEditor;
 
     private org.rubypeople.rdt.internal.ui.preferences.FoldingConfigurationBlock fFoldingConfigurationBlock;
 
 
 	public TextEditorPreferencePage2() {
 		setDescription(RubyUIMessages.getString("RubyEditorPreferencePage.description")); //$NON-NLS-1$
 		setPreferenceStore(RubyPlugin.getDefault().getPreferenceStore());
 		fOverlayStore = createOverlayStore();
 	}
 
 	private OverlayPreferenceStore createOverlayStore() {
 
 		ArrayList overlayKeys = new ArrayList();
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE));
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH));
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, PreferenceConstants.FORMAT_INDENTATION));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, PreferenceConstants.FORMAT_USE_TAB));
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLOR));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.INT, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN));
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN));
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER));
 
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.STRING, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER_COLOR));
 		overlayKeys.add(new OverlayPreferenceStore.OverlayKey(OverlayPreferenceStore.BOOLEAN, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER));
 
 		OverlayPreferenceStore.OverlayKey[] keys = new OverlayPreferenceStore.OverlayKey[overlayKeys.size()];
 		overlayKeys.toArray(keys);
 		return new OverlayPreferenceStore(getPreferenceStore(), keys);
 	}
 
 	/*
 	 * @see IWorkbenchPreferencePage#init()
 	 */
 	public void init(IWorkbench workbench) {}
 
 	/*
 	 * @see PreferencePage#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		super.createControl(parent);
 		WorkbenchHelp.setHelp(getControl(), ITextEditorHelpContextIds.TEXT_EDITOR_PREFERENCE_PAGE);
 	}
 
 	private void handleAppearanceColorListSelection() {
 		int i = fAppearanceColorList.getSelectionIndex();
 		String key = fAppearanceColorListModel[i][1];
 		RGB rgb = PreferenceConverter.getColor(fOverlayStore, key);
 		fAppearanceColorEditor.setColorValue(rgb);
 	}
 
 	private Control createAppearancePage(Composite parent) {
 
 		Composite appearanceComposite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		appearanceComposite.setLayout(layout);
 
 		String label = RubyUIMessages.getString("TextEditorPreferencePage.displayedTabWidth"); //$NON-NLS-1$
 		addTextField(appearanceComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH, 3, 0, true);
 
 		label = RubyUIMessages.getString("TextEditorPreferencePage.printMarginColumn"); //$NON-NLS-1$
 		addTextField(appearanceComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN_COLUMN, 3, 0, true);
 
 		label = RubyUIMessages.getString("TextEditorPreferencePage.showOverviewRuler"); //$NON-NLS-1$
 		addCheckBox(appearanceComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_OVERVIEW_RULER, 0);
 
 		label = RubyUIMessages.getString("TextEditorPreferencePage.showLineNumbers"); //$NON-NLS-1$
 		addCheckBox(appearanceComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_LINE_NUMBER_RULER, 0);
 
 		label = RubyUIMessages.getString("TextEditorPreferencePage.highlightCurrentLine"); //$NON-NLS-1$
 		addCheckBox(appearanceComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE, 0);
 
 		label = RubyUIMessages.getString("TextEditorPreferencePage.showPrintMargin"); //$NON-NLS-1$
 		addCheckBox(appearanceComposite, label, AbstractDecoratedTextEditorPreferenceConstants.EDITOR_PRINT_MARGIN, 0);
 
 		Label l = new Label(appearanceComposite, SWT.LEFT);
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = 2;
 		gd.heightHint = convertHeightInCharsToPixels(1) / 2;
 		l.setLayoutData(gd);
 
 		l = new Label(appearanceComposite, SWT.LEFT);
 		l.setText(RubyUIMessages.getString("TextEditorPreferencePage.appearanceOptions")); //$NON-NLS-1$
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
 		gd.horizontalSpan = 2;
 		l.setLayoutData(gd);
 
 		Composite editorComposite = new Composite(appearanceComposite, SWT.NONE);
 		layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		editorComposite.setLayout(layout);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
 		gd.horizontalSpan = 2;
 		editorComposite.setLayoutData(gd);
 
 		fAppearanceColorList = new List(editorComposite, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
 		gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
 		gd.heightHint = convertHeightInCharsToPixels(3);
 		fAppearanceColorList.setLayoutData(gd);
 
 		Composite stylesComposite = new Composite(editorComposite, SWT.NONE);
 		layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.numColumns = 2;
 		stylesComposite.setLayout(layout);
 		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		l = new Label(stylesComposite, SWT.LEFT);
 		l.setText(RubyUIMessages.getString("TextEditorPreferencePage.color")); //$NON-NLS-1$
 		gd = new GridData();
 		gd.horizontalAlignment = GridData.BEGINNING;
 		l.setLayoutData(gd);
 
 		fAppearanceColorEditor = new ColorEditor(stylesComposite);
 		Button foregroundColorButton = fAppearanceColorEditor.getButton();
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalAlignment = GridData.BEGINNING;
 		foregroundColorButton.setLayoutData(gd);
 
 		fAppearanceColorList.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 			// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				handleAppearanceColorListSelection();
 			}
 		});
 		foregroundColorButton.addSelectionListener(new SelectionListener() {
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 			// do nothing
 			}
 
 			public void widgetSelected(SelectionEvent e) {
 				int i = fAppearanceColorList.getSelectionIndex();
 				String key = fAppearanceColorListModel[i][1];
 
 				PreferenceConverter.setValue(fOverlayStore, key, fAppearanceColorEditor.getColorValue());
 			}
 		});
 
 		return appearanceComposite;
 	}
 
 	private Control createCodeFormatterPage(Composite parent) {
 
 		Composite codeFormatterComposite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		codeFormatterComposite.setLayout(layout);
 
 		String label = RubyUIMessages.getString("RubyEditorPropertyPage.indentation"); //$NON-NLS-1$
 		addTextField(codeFormatterComposite, label, PreferenceConstants.FORMAT_INDENTATION, 3, 0, true);
 
 		label = RubyUIMessages.getString("RubyEditorPropertyPage.useTab"); //$NON-NLS-1$
 		addCheckBox(codeFormatterComposite, label, PreferenceConstants.FORMAT_USE_TAB, 0);
 
 		Label labelControl = new Label(codeFormatterComposite, SWT.WRAP);
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = 3;
 		gd.horizontalSpan = 2;
 		gd.grabExcessVerticalSpace = true;
 		labelControl.setLayoutData(gd);
 
 		Point parentSize = parent.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 		gd.widthHint = parentSize.x;
 		labelControl.setText(RubyUIMessages.getString("RubyEditorPropertyPage.tabSpaceExplanation"));
 		labelControl.getParent().layout(true);
 
 		labelControl.setSize(300, SWT.DEFAULT);
 
 		return codeFormatterComposite;
 	}
 
 
 
 	/*
 	 * @see PreferencePage#createContents(Composite)
 	 */
 	protected Control createContents(Composite parent) {	    
 	    
 	    fFoldingConfigurationBlock= new FoldingConfigurationBlock(fOverlayStore);
 	    
 	    fOverlayStore.load();
 		fOverlayStore.start();
 
 		TabFolder folder = new TabFolder(parent, SWT.NONE);
 		folder.setLayout(new TabFolderLayout());
 		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		TabItem item = new TabItem(folder, SWT.NONE);
 		item.setText(RubyUIMessages.getString("TextEditorPreferencePage.general")); //$NON-NLS-1$
 		item.setControl(createAppearancePage(folder));
 
 		item = new TabItem(folder, SWT.NONE);
 		item.setText(RubyUIMessages.getString("RubyEditorPropertyPage.codeFormatterTabTitle"));
 		item.setControl(createCodeFormatterPage(folder));
 
 		item = new TabItem(folder, SWT.NONE);
 		item.setText("Syntax");
 		item.setControl(createSyntaxPage(folder));
 		
 		item= new TabItem(folder, SWT.NONE);
 		item.setText(RubyUIMessages.getString("RubyEditorPreferencePage.folding.title")); //$NON-NLS-1$
 		item.setControl(fFoldingConfigurationBlock.createControl(folder));
 
 		initialize();
 		Dialog.applyDialogFont(folder);
 		return folder;
 	}
 
 	/**
 	 * @param folder
 	 * @return
 	 */
 	private Control createSyntaxPage(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL));
 
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		layout.verticalSpacing = 10;
 
 		composite.setLayout(layout);
 
 		Group colorComposite = new Group(composite, SWT.NONE);
 		layout = new GridLayout();
 		layout.numColumns = 4;
 		layout.horizontalSpacing = 10;
 		layout.verticalSpacing = 8;
 		layout.marginWidth = 10;
 		layout.marginHeight = 10;
 
 		colorComposite.setLayout(layout);
 		colorComposite.setText(RubyUIMessages.getString("RubyEditorPropertyPage.highlighting.group")); //$NON-NLS-1$
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.horizontalSpan = 2;
 		colorComposite.setLayoutData(data);
 
 		Label header = new Label(colorComposite, SWT.BOLD);
 		header.setText(RubyUIMessages.getString("RubyEditorPropertyPage.property"));
 		header = new Label(colorComposite, SWT.BOLD);
 		header.setText(RubyUIMessages.getString("RubyEditorPropertyPage.color"));
 		header.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
 		header = new Label(colorComposite, SWT.BOLD);
 		header.setText(RubyUIMessages.getString("RubyEditorPropertyPage.bold"));
 		header.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
 		header = new Label(colorComposite, SWT.BOLD);
 		header.setText(RubyUIMessages.getString("RubyEditorPropertyPage.italic"));
 		header.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		
 		textPropertyWidgets = new TextPropertyWidget[colorProperties.length];
 		for (int i = 0; i < colorProperties.length; i++) {
 			textPropertyWidgets[i] = new TextPropertyWidget(colorComposite, colorProperties[i]);
 		}
 
 		return composite;
 	}
 
 	private void initialize() {
 
 		initializeFields();
 
 		for (int i = 0; i < fAppearanceColorListModel.length; i++)
 			fAppearanceColorList.add(fAppearanceColorListModel[i][0]);
 		fAppearanceColorList.getDisplay().asyncExec(new Runnable() {
 
 			public void run() {
 				if (fAppearanceColorList != null && !fAppearanceColorList.isDisposed()) {
 					fAppearanceColorList.select(0);
 					handleAppearanceColorListSelection();
 				}
 			}
 		});
 	
 		fFoldingConfigurationBlock.initialize();
 	}
 
 	/*
 	 * @see PreferencePage#performOk()
 	 */
 	public boolean performOk() {
 		for (int i = 0; i < textPropertyWidgets.length; i++) {
 			TextPropertyWidget widget = textPropertyWidgets[i];
 			widget.stringColorEditor.store();
 			RubyPlugin.getDefault().getPreferenceStore().setValue(widget.property + PreferenceConstants.EDITOR_BOLD_SUFFIX, widget.boldCheckBox.getSelection());
 			RubyPlugin.getDefault().getPreferenceStore().setValue(widget.property + PreferenceConstants.EDITOR_ITALIC_SUFFIX, widget.italicCheckBox.getSelection());
 		}
 		fFoldingConfigurationBlock.performOk();
 		fOverlayStore.propagate();
 		RubyPlugin.getDefault().savePluginPreferences();
 		return true;
 	}
 
 	/*
 	 * @see PreferencePage#performDefaults()
 	 */
 	protected void performDefaults() {
 
 		fOverlayStore.loadDefaults();
 
 		initializeFields();
 
 		for (int i = 0; i < textPropertyWidgets.length; i++) {
 			textPropertyWidgets[i].loadDefault();
 		}
 
 		handleAppearanceColorListSelection();
 		fFoldingConfigurationBlock.performDefaults();
 
 		super.performDefaults();
 	}
 
 	/*
 	 * @see DialogPage#dispose()
 	 */
 	public void dispose() {
 	    fFoldingConfigurationBlock.dispose();
 	    
 		if (fOverlayStore != null) {
 			fOverlayStore.stop();
 			fOverlayStore = null;
 		}
 		
 		super.dispose();
 	}
 
 	private Control addTextField(Composite composite, String label, String key, int textLimit, int indentation, boolean isNumber) {
 
 		Label labelControl = new Label(composite, SWT.NONE);
 		labelControl.setText(label);
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.horizontalIndent = indentation;
 		labelControl.setLayoutData(gd);
 
 		Text textControl = new Text(composite, SWT.BORDER | SWT.SINGLE);
 		gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
 		gd.widthHint = convertWidthInCharsToPixels(textLimit + 1);
 		textControl.setLayoutData(gd);
 		textControl.setTextLimit(textLimit);
 		fTextFields.put(textControl, key);
 		if (isNumber) {
 			fNumberFields.add(textControl);
 			textControl.addModifyListener(fNumberFieldListener);
 		} else {
 			textControl.addModifyListener(fTextFieldListener);
 		}
 
 		return textControl;
 	}
 
 	private void numberFieldChanged(Text textControl) {
 		String number = textControl.getText();
 		IStatus status = validatePositiveNumber(number);
 		if (!status.matches(IStatus.ERROR)) fOverlayStore.setValue((String) fTextFields.get(textControl), number);
 		updateStatus(status);
 	}
 
 	private IStatus validatePositiveNumber(String number) {
 		StatusInfo status = new StatusInfo();
 		if (number.length() == 0) {
 			status.setError(RubyUIMessages.getString("TextEditorPreferencePage.empty_input")); //$NON-NLS-1$
 		} else {
 			try {
 				int value = Integer.parseInt(number);
 				if (value < 0) status.setError(RubyUIMessages.getFormattedString("TextEditorPreferencePage.invalid_input", number)); //$NON-NLS-1$
 			} catch (NumberFormatException e) {
 				status.setError(RubyUIMessages.getFormattedString("TextEditorPreferencePage.invalid_input", number)); //$NON-NLS-1$
 			}
 		}
 		return status;
 	}
 
 	void updateStatus(IStatus status) {
 		if (!status.matches(IStatus.ERROR)) {
 			for (int i = 0; i < fNumberFields.size(); i++) {
 				Text text = (Text) fNumberFields.get(i);
 				IStatus s = validatePositiveNumber(text.getText());
 				status = s.getSeverity() > status.getSeverity() ? s : status;
 			}
 		}
 		setValid(!status.matches(IStatus.ERROR));
 		applyToStatusLine(this, status);
 	}
 
 	/**
 	 * Applies the status to the status line of a dialog page.
 	 */
 	public void applyToStatusLine(DialogPage page, IStatus status) {
 		String message = status.getMessage();
 		switch (status.getSeverity()) {
 		case IStatus.OK:
 			page.setMessage(message, IMessageProvider.NONE);
 			page.setErrorMessage(null);
 			break;
 		case IStatus.WARNING:
 			page.setMessage(message, IMessageProvider.WARNING);
 			page.setErrorMessage(null);
 			break;
 		case IStatus.INFO:
 			page.setMessage(message, IMessageProvider.INFORMATION);
 			page.setErrorMessage(null);
 			break;
 		default:
 			if (message.length() == 0) {
 				message = null;
 			}
 			page.setMessage(null);
 			page.setErrorMessage(message);
 			break;
 		}
 	}
 
 	class TextPropertyWidget {
 
 		protected ColorFieldEditor stringColorEditor;
 		protected Button boldCheckBox;
 		protected Button italicCheckBox;
 		protected String property;
 
 		TextPropertyWidget(Composite parent, String property) {
 			this.property = property;
 			Label label = new Label(parent, SWT.NORMAL);
 			label.setText(RubyUIMessages.getString("RubyEditorPropertyPage." + property));
 
 			Composite dummyComposite = new Composite(parent, SWT.NONE);
 			// ColorFieldEditor sets its parent composite to 2 columns,
 			// therefore a dummyComposite is used here
 			stringColorEditor = new ColorFieldEditor(property, "", dummyComposite);
 			stringColorEditor.setPreferenceStore(getPreferenceStore());
 			stringColorEditor.load();
 
 			boldCheckBox = new Button(parent, SWT.CHECK);
 			boldCheckBox.setSelection(getPreferenceStore().getBoolean(property + PreferenceConstants.EDITOR_BOLD_SUFFIX));
 			boldCheckBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
 			
 			italicCheckBox = new Button(parent, SWT.CHECK);
 			italicCheckBox.setSelection(getPreferenceStore().getBoolean(property + PreferenceConstants.EDITOR_ITALIC_SUFFIX));
 			italicCheckBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		}
 
 		public void loadDefault() {
 			stringColorEditor.loadDefault();
 			boldCheckBox.setSelection(getPreferenceStore().getBoolean(property + PreferenceConstants.EDITOR_BOLD_SUFFIX));
 		}
 	}
 }
