 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator - Eclipse
  * Copyright (c) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.eclipse.ui.commands;
 
 
 import java.util.Random;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.ColorDialog;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.events.SelectionEvent;
 
 import de.tuilmenau.ics.fog.ui.Marker;
 
 
 
 public class MarkElementsDialog extends Dialog 
 {
 	public MarkElementsDialog(Shell parent)
 	{
 		super(parent);
 	}
 
 	public MarkElementsDialog(Shell parent, int style)
 	{
 		super(parent, style);
 	}
 	
 	private void selectColor()
 	{
 		// Create the color-change dialog
 		ColorDialog dlg = new ColorDialog(getParent());
 
 		// Set the selected color in the dialog from
 		// user's selected color
 		dlg.setRGB(mColorLabel.getBackground().getRGB());
 
 		// Change the title bar text
 		dlg.setText(mColorLabel.getText());
 
 		// Open the dialog and retrieve the selected color
 		RGB rgb = dlg.open();
 		if (rgb != null) {
 			// Dispose the old color, create the
 			// new one, and set into the label
 			Color color = mColorLabel.getBackground();
 			color.dispose();
 			color = new Color(getParent().getDisplay(), rgb);
 			mColorLabel.setBackground(color);
 		}
 	}
 
 
 	public Marker open(String pMarkerNameSuggestion)
 	{
 		Shell parent = getParent();
 		final Shell shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
 
 		shell.setText("Marker Dialog");
 		shell.setLayout(new GridLayout(2, true));
 
 		final Label label = new Label(shell, SWT.NULL);
 		label.setText("Marker name:");
 
 		final Text text = new Text(shell, SWT.SINGLE | SWT.BORDER);
 		if(pMarkerNameSuggestion != null) {
 			text.setText(pMarkerNameSuggestion);
 		} else {
 			text.setText("Marker " +new Random().nextInt());
 		}
 
 		Color color = new Color(shell.getDisplay(), new RGB(0, 0, 255));
 		mColorLabel = new Label(shell, SWT.NONE);
 		mColorLabel.setText("Marker color        ");
 		mColorLabel.setBackground(color);
 
 		Button button = new Button(shell, SWT.PUSH);
 		button.setText("Choose a color");
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent event) {
 				selectColor();
 			}
 		});
 
 		final Button buttonOK = new Button(shell, SWT.PUSH);
 		buttonOK.setText("Ok");
 		buttonOK.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
 		Button buttonCancel = new Button(shell, SWT.PUSH);
 		buttonCancel.setText("Cancel");
 
 		buttonOK.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event)
 			{
 				Color color = mColorLabel.getBackground();
 				java.awt.Color colorAWT = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
 				color.dispose();
 				
 				mMarker = new Marker(text.getText(), colorAWT);
 				shell.dispose();
 			}
 		});
 
 		buttonCancel.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				Color color = mColorLabel.getBackground();
 				color.dispose();
 				
 				shell.dispose();
 			}
 		});
 
 		shell.addListener(SWT.Traverse, new Listener() {
 			public void handleEvent(Event event) {
 				if(event.detail == SWT.TRAVERSE_ESCAPE)
 					event.doit = false;
 			}
 		});
 		
 		shell.pack();
 		shell.open();
 
 		Display display = parent.getDisplay();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch())
 				display.sleep();
 		}
 
 		return mMarker;
 	}
 
 	
 	private Marker mMarker = null;
 	private Label mColorLabel;
 }
