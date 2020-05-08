 /*******************************************************************************
 * Copyright (c) 2010, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.launch;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MessageBox;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.services.IPathMap;
 
 class PathMapRuleDialog extends Dialog {
 
     private final IPathMap.PathMapRule pathMapRule;
     private final boolean enable_editing;
     private final boolean showContextQuery;
     private final Image image;
 
     private Text source_text;
     private Text destination_text;
     private Text context_query_text;
     private Button destination_button;
 
     PathMapRuleDialog(Shell parent, Image image, IPathMap.PathMapRule pathMapRule, boolean enable_editing, boolean showContextQuery) {
         super(parent);
         this.image = image != null ? image : ImageCache.getImage(ImageCache.IMG_PATH);
         this.pathMapRule = pathMapRule;
         this.enable_editing = enable_editing;
         this.showContextQuery = showContextQuery;
     }
 
     @Override
     protected void configureShell(Shell shell) {
         super.configureShell(shell);
         shell.setText("File Path Map Rule"); //$NON-NLS-1$
         shell.setImage(image);
     }
 
     @Override
     protected void createButtonsForButtonBar(Composite parent) {
         createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
         createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
         updateButtons();
     }
 
     @Override
     protected Control createDialogArea(Composite parent) {
         Composite composite = (Composite)super.createDialogArea(parent);
         createFileNameFields(composite);
         setData();
         composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
         return composite;
     }
 
     private void createFileNameFields(Composite parent) {
         Font font = parent.getFont();
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(3, false);
         composite.setFont(font);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
         Label source_label = new Label(composite, SWT.NONE);
         source_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
         source_label.setFont(font);
         source_label.setText("Source:"); //$NON-NLS-1$
 
         source_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
         GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.widthHint = 300;
         gd.horizontalSpan = 2;
         source_text.setLayoutData(gd);
         source_text.setFont(font);
         source_text.setEditable(enable_editing);
 
         source_text.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 updateButtons();
             }
         });
 
         Label destination_label = new Label(composite, SWT.NONE);
         destination_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
         destination_label.setFont(font);
         destination_label.setText("Destination:"); //$NON-NLS-1$
 
         destination_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
         destination_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
         destination_text.setFont(font);
         destination_text.setEditable(enable_editing);
 
         destination_text.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 updateButtons();
             }
         });
 
         destination_button = new Button(composite, SWT.PUSH);
         destination_button.setText("Browse..."); //$NON-NLS-1$
         destination_button.setFont(font);
         destination_button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
         destination_button.setEnabled(enable_editing);
         destination_button.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.NONE);
                 dialog.setFilterPath(destination_text.getText());
                 String path = dialog.open();
                 if (path != null) destination_text.setText(path);
             }
         });
 
         if (showContextQuery) {
             Label context_query_label = new Label(composite, SWT.NONE);
             context_query_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
             context_query_label.setFont(font);
             context_query_label.setText("Context Query:"); //$NON-NLS-1$
 
             context_query_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
             gd = new GridData(GridData.FILL_HORIZONTAL);
             gd.widthHint = 200;
             gd.horizontalSpan = 2;
             context_query_text.setLayoutData(gd);
             context_query_text.setFont(font);
             context_query_text.setEditable(enable_editing);
 
             context_query_text.addModifyListener(new ModifyListener() {
                 public void modifyText(ModifyEvent e) {
                     updateButtons();
                 }
             });
         }
     }
 
     private void setData() {
         source_text.setText(pathMapRule.getSource() != null ? pathMapRule.getSource() : ""); //$NON-NLS-1$
         destination_text.setText(pathMapRule.getDestination() != null ? pathMapRule.getDestination() : ""); //$NON-NLS-1$
         if (context_query_text != null)
             context_query_text.setText(pathMapRule.getContextQuery() != null ? pathMapRule.getContextQuery() : ""); //$NON-NLS-1$
         updateButtons();
     }
 
     private void getData() {
         if (source_text.getText().trim().length() > 0)
             pathMapRule.getProperties().put(IPathMap.PROP_SOURCE, source_text.getText());
         else
             pathMapRule.getProperties().remove(IPathMap.PROP_SOURCE);
 
         if (destination_text.getText().trim().length() > 0)
             pathMapRule.getProperties().put(IPathMap.PROP_DESTINATION, destination_text.getText());
         else
             pathMapRule.getProperties().remove(IPathMap.PROP_DESTINATION);
 
         if (context_query_text != null && context_query_text.getText().trim().length() > 0)
             pathMapRule.getProperties().put(IPathMap.PROP_CONTEXT_QUERY, context_query_text.getText());
         else
             pathMapRule.getProperties().remove(IPathMap.PROP_CONTEXT_QUERY);
     }
 
     private void updateButtons() {
         Button btn = getButton(IDialogConstants.OK_ID);
         if (btn != null && source_text != null) btn.setEnabled(!enable_editing || source_text.getText().trim().length() > 0);
     }
 
     @Override
     protected void okPressed() {
         if (enable_editing) {
             try {
                 getData();
             }
             catch (Throwable x) {
                 MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                 mb.setText("Invalid data"); //$NON-NLS-1$
                 mb.setMessage(TCFModel.getErrorMessage(x, true));
                 mb.open();
                 return;
             }
         }
         super.okPressed();
     }
 }
