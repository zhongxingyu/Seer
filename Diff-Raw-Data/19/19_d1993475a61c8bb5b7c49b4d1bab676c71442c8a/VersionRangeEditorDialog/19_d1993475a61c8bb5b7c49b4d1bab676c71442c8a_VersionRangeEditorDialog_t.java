 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.aggregator.presentation;
 
 import org.eclipse.equinox.internal.provisional.p2.metadata.Version;
 import org.eclipse.equinox.internal.provisional.p2.metadata.VersionRange;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.resource.JFaceColors;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author Karel Brezina
  * 
  */
 public class VersionRangeEditorDialog extends Dialog {

	// VersionRange.OSGi_versionMax is not visible
	private static final Version OSGi_versionMax = Version.createOSGi(Integer.MAX_VALUE, Integer.MAX_VALUE,
			Integer.MAX_VALUE);

 	private static final int VERSION_TEXT_WIDTH_HINT = 160;
 
 	private static String getString(String key) {
 		return AggregatorEditorPlugin.INSTANCE.getString(key);
 	}
 
 	protected ILabelProvider labelProvider;
 
 	protected VersionRange versionRange;
 
 	private Text minVersionText;
 
 	private Combo minVersionInclusiveCombo;
 
 	private Text maxVersionText;
 
 	private Combo maxVersionInclusiveCombo;
 
 	private Label statusLabel;
 
 	private Button okButton;
 
 	private VersionRange result;
 
 	protected VersionRangeEditorDialog(Shell parent, ILabelProvider labelProvider, Object object) {
 		super(parent);
 		setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
 		this.labelProvider = labelProvider;
 		this.versionRange = (VersionRange) object;
 	}
 
 	public VersionRange getResult() {
 		return result;
 	}
 
 	protected void buttonPressed(int buttonId) {
 		switch(buttonId) {
 		case IDialogConstants.CANCEL_ID:
 			close();
 			break;
 		default:
 			if(performAction(buttonId))
 				close();
 		}
 	}
 
 	protected void configureShell(Shell shell) {
 		super.configureShell(shell);
 		shell.setText(getString("_UI_VersionRangeEditor_windowTitle"));
 	}
 
 	protected Control createButtonBar(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 0; // create
 		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
 		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
 		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
 		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
 
 		composite.setLayout(layout);
 		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		Label filler = new Label(composite, SWT.NONE);
 		filler.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
 		layout.numColumns++;
 
 		okButton = createButton(composite, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
 		okButton.setEnabled(isOKEnabled());
 		createButton(composite, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
 
 		return composite;
 	}
 
 	protected Control createDialogArea(Composite parent) {
 		Composite result = (Composite) super.createDialogArea(parent);
 
 		Control fContents = createPageArea(result);
 		fContents.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		return result;
 	}
 
 	protected Control createPageArea(Composite parent) {
 		final String[] inclusiveExclusive = new String[] { getString("_UI_VersionRangeEditor_inclusiveChoice"),
 				getString("_UI_VersionRangeEditor_exclusiveChoice") };
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setFont(parent.getFont());
 		composite.setLayout(new GridLayout(3, false));
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		new Label(composite, SWT.NONE).setText(getString("_UI_VersionRangeEditor_minimumVersionLabel"));
 		minVersionText = new Text(composite, SWT.BORDER);
 		minVersionText.setText(versionRange.getMinimum().toString());
 		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
 		gridData.widthHint = VERSION_TEXT_WIDTH_HINT;
 		minVersionText.setLayoutData(gridData);
 		minVersionInclusiveCombo = new Combo(composite, SWT.READ_ONLY);
 		minVersionInclusiveCombo.setItems(inclusiveExclusive);
 		minVersionInclusiveCombo.select(versionRange.getIncludeMinimum()
 				? 0
 				: 1);
 
 		new Label(composite, SWT.NONE).setText(getString("_UI_VersionRangeEditor_maximumVersionLabel"));
 		maxVersionText = new Text(composite, SWT.BORDER);

		Version maxVersion = versionRange.getMaximum();
		maxVersionText.setText((Version.MAX_VERSION.equals(maxVersion) || OSGi_versionMax.equals(maxVersion))
				? ""
				: maxVersion.toString());
 		maxVersionText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		maxVersionInclusiveCombo = new Combo(composite, SWT.READ_ONLY);
 		maxVersionInclusiveCombo.setItems(inclusiveExclusive);
 		maxVersionInclusiveCombo.select(versionRange.getIncludeMaximum()
 				? 0
 				: 1);
 
 		ModifyListener modifyListener = new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				okButton.setEnabled(isOKEnabled());
 			}
 		};
 
 		minVersionText.addModifyListener(modifyListener);
 		minVersionInclusiveCombo.addModifyListener(modifyListener);
 		maxVersionText.addModifyListener(modifyListener);
 		maxVersionInclusiveCombo.addModifyListener(modifyListener);
 
 		statusLabel = new Label(composite, SWT.LEFT);
 		GridData layoutData = new GridData(GridData.FILL, GridData.CENTER, true, false);
 		layoutData.horizontalSpan = 4;
 		statusLabel.setLayoutData(layoutData);
 
 		return composite;
 	}
 
 	protected boolean performAction(int actionID) {
 		result = null;
 
 		switch(actionID) {
 		case CANCEL:
 			return true;
 		case OK:
 			try {
 				result = createVersionRange();
 			}
 			catch(IllegalArgumentException e) {
 				statusMessage(true, e.getMessage());
 				return false;
 			}
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	private VersionRange createVersionRange() throws IllegalArgumentException {
 		String minVersionString = UIUtils.trimmedValue(minVersionText);
 		String maxVersionString = UIUtils.trimmedValue(maxVersionText);
 
 		if(minVersionString == null)
 			throw new IllegalArgumentException(getString("_UI_VersionRangeEditor_minimumVersionMessage") + " "
 					+ getString("_UI_VersionRangeEditor_notSpecifiedMessage"));
 
 		Version minVersion = null;
 		Version maxVersion = null;
 
 		try {
 			minVersion = Version.create(minVersionString);
 		}
 		catch(IllegalArgumentException e) {
 			throw new IllegalArgumentException(getString("_UI_VersionRangeEditor_minimumVersionMessage") + " "
 					+ e.getMessage());
 		}
 
 		if(maxVersionString != null) {
 			try {
 				maxVersion = Version.create(maxVersionString);
 			}
 			catch(IllegalArgumentException e) {
 				throw new IllegalArgumentException(getString("_UI_VersionRangeEditor_maximumVersionMessage") + " "
 						+ e.getMessage());
 			}
 		}
 
 		return new VersionRange(minVersion, minVersionInclusiveCombo.getSelectionIndex() == 0, maxVersion,
 				maxVersionInclusiveCombo.getSelectionIndex() == 0);
 	}
 
 	private boolean isOKEnabled() {
 		try {
 			createVersionRange();
 		}
 		catch(IllegalArgumentException e) {
 			statusMessage(true, e.getMessage());
 			return false;
 		}
 
 		statusMessage(true, "");
 		return true;
 	}
 
 	private void statusMessage(boolean error, String message) {
 		statusLabel.setText(message);
 
 		if(error)
 			statusLabel.setForeground(JFaceColors.getErrorText(statusLabel.getDisplay()));
 		else
 			statusLabel.setForeground(null);
 
 		if(UIUtils.trimmedValue(message) != null)
 			getShell().getDisplay().beep();
 	}
 }
