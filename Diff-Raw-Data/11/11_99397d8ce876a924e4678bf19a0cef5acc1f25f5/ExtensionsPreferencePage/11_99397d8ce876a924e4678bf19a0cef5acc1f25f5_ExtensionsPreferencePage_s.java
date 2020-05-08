 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.ui.dialogs;
 
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.wst.css.core.internal.util.declaration.CSSPropertyContext;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 
 /**
  * @author mengbo
  * @version 1.5
  */
 public class ExtensionsPreferencePage extends PreferencePage {
 	private CSSPropertyContext _style;
 
 	private StyleCombo _beforeCombo, _afterCombo, _cursorCombo;
 
 	public ExtensionsPreferencePage(IDOMElement element,
 			CSSPropertyContext style) {
 		super();
 		_style = style;
 
 		setTitle(DialogsMessages.getString("ExtensionsPreferencePage.Title")); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.jface.preference.
 	 *      PreferencePage#createContents(Composite)
 	 */
 	protected Control createContents(Composite parent) {
 		GridLayout layout;
 		GridData data;
 
 		Composite top = new Composite(parent, SWT.NONE);
 		layout = new GridLayout(1, false);
 		data = new GridData(GridData.FILL_BOTH);
 		top.setLayout(layout);
 		top.setLayoutData(data);
 
 		Group pageGroup = new Group(top, SWT.NONE);
 		pageGroup.setText(DialogsMessages
 				.getString("ExtensionsPreferencePage.PageBreak"));
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		pageGroup.setLayoutData(data);
 		layout = new GridLayout(2, false);
 		pageGroup.setLayout(layout);
 
 		Label beforeLabel = new Label(pageGroup, SWT.NONE);
 		beforeLabel.setText(DialogsMessages
 				.getString("ExtensionsPreferencePage.Before")); //$NON-NLS-1$
 		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		beforeLabel.setLayoutData(data);
 
 		_beforeCombo = new StyleCombo(pageGroup, SWT.NONE);
 		_beforeCombo.setItems(IStyleConstants.PAGE_BREAK);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		_beforeCombo.setLayoutData(data);
 		_beforeCombo.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				String page = _beforeCombo.getText();
 				_style.setPageBreakBefore(page);
 			}
 		});
 
 		Label afterLabel = new Label(pageGroup, SWT.NONE);
 		afterLabel.setText(DialogsMessages
 				.getString("ExtensionsPreferencePage.After")); //$NON-NLS-1$
 		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		afterLabel.setLayoutData(data);
 
 		_afterCombo = new StyleCombo(pageGroup, SWT.NONE);
 		_afterCombo.setItems(IStyleConstants.PAGE_BREAK);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		_afterCombo.setLayoutData(data);
 		_afterCombo.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				String page = _afterCombo.getText();
 				_style.setPageBreakAfter(page);
 			}
 		});
 
 		Group visualGroup = new Group(top, SWT.NONE);
 		visualGroup.setText(DialogsMessages
 				.getString("ExtensionsPreferencePage.VisualEffect"));
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		visualGroup.setLayoutData(data);
 		layout = new GridLayout(2, false);
 		visualGroup.setLayout(layout);
 
 		Label cursorLabel = new Label(visualGroup, SWT.NONE);
 		cursorLabel.setText(DialogsMessages
 				.getString("ExtensionsPreferencePage.Cursor")); //$NON-NLS-1$
 		data = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		cursorLabel.setLayoutData(data);
 
 		_cursorCombo = new StyleCombo(visualGroup, SWT.NONE);
 		_cursorCombo.setItems(IStyleConstants.CURSOR);
 		data = new GridData(GridData.FILL_HORIZONTAL);
 		_cursorCombo.setLayoutData(data);
 		_cursorCombo.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
				String position = _cursorCombo.getText();
				_style.setListStylePosition(position);
 			}
 		});
 
 		initializeControls();
 
 		return top;
 	}
 
 	private void initializeControls() {
 		// page-break-before
 		String page = _style.getPageBreakBefore();
 		if (!isEmptyString(page)) {
 			int index = _beforeCombo.indexOf(page);
 			if (index != -1) {
 				_beforeCombo.select(index);
 			} else {
 				_beforeCombo.setText(page);
 			}
 		}
 
 		// page-break-after
 		page = _style.getPageBreakAfter();
 		if (!isEmptyString(page)) {
 			int index = _afterCombo.indexOf(page);
 			if (index != -1) {
 				_afterCombo.select(index);
 			} else {
 				_afterCombo.setText(page);
 			}
 		}
 
 		// cursor
 		String cursor = _style.getCursor();
 		if (!isEmptyString(cursor)) {
 			int index = _cursorCombo.indexOf(cursor);
 			if (index != -1) {
 				_cursorCombo.select(index);
 			} else {
 				_cursorCombo.setText(cursor);
 			}
 		}
 	}
 
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 
 		getApplyButton().setVisible(false);
 		getDefaultsButton().setVisible(false);
 	}
 
 	private boolean isEmptyString(String str) {
 		if (str == null || str.length() == 0) {
 			return true;
 		}
         return false;
 	}
 }
