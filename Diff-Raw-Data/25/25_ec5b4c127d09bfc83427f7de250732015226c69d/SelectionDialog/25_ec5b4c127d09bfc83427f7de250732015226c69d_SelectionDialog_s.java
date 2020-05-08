 /*******************************************************************************
  * Copyright (c) 2008, 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.ui.widgets;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 
 /**
  * 
  */
 public class SelectionDialog extends Composite {
 
 	private FormToolkit widgetFactory;
 
 	private Text text;
 
 	private Button browseButton;
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param widgetFactory
 	 */
 	public SelectionDialog(Composite parent, int style, FormToolkit widgetFactory) {
 		super(parent, style);
 		this.widgetFactory = widgetFactory;
 		createControl();
 	}
 
 	/**
 	 * 
 	 */
 	private void createControl() {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		this.setLayout(layout);
 		if (widgetFactory != null) {
 			text = widgetFactory.createText(this, "");

 			text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);

 			widgetFactory.paintBordersFor(this);

 			browseButton = widgetFactory.createButton(this, "Browse...", SWT.PUSH);

 			text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		}
 	}
 
 	/**
 	 * @return the text of the text widget.
 	 */
 	public String getText() {
 		if (text != null)
 			return text.getText();
 		return "";
 	}
 
 	/**
 	 * @param text
 	 */
 	public void setText(String text) {
 		if (this.text != null)
 			this.text.setText(text);
 	}
 
 	/**
 	 * Adds a selection listener on browse button.
 	 * 
 	 * @param selectionListener
 	 */
 	public void addSelectionListener(SelectionListener selectionListener) {
 		if (browseButton != null)
 			browseButton.addSelectionListener(selectionListener);
 	}
 
 	/**
 	 * 
 	 */
 	public void addFocusListener(FocusListener focusListener) {
 		if (this.text != null)
 			this.text.addFocusListener(focusListener);
 	}
 
 	/**
 	 * 
 	 */
 	public void addKeyListener(KeyListener keyListener) {
 		if (this.text != null)
 			this.text.addKeyListener(keyListener);
 	}
 
 }
