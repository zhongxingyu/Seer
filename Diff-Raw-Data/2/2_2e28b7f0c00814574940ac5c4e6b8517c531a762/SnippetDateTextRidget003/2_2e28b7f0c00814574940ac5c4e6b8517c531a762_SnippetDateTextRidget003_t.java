 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.sample.snippets;
 
 import java.util.Calendar;
 import java.util.Date;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.ui.ridgets.IDateTextRidget;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.swt.DatePickerComposite;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Snippet that shows the SWT DateTime widget compared to the Riena
  * DatePickerComposite.
  */
 public class SnippetDateTextRidget003 {
 	public static void main(String[] args) {
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		shell.setSize(400, 200);
 		shell.setText("DateTime Demoapp"); //$NON-NLS-1$
 		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(shell);
 
 		Calendar currentDate = Calendar.getInstance();
 		currentDate.setTime(new Date());
 
 		createLabel(shell, "SWT DateTime"); //$NON-NLS-1$
 		DateTime dateTime = new DateTime(shell, SWT.MEDIUM | SWT.BORDER | SWT.DROP_DOWN);
 		dateTime.setDay(currentDate.get(Calendar.DAY_OF_MONTH));
 		dateTime.setMonth(currentDate.get(Calendar.MONTH));
 		dateTime.setYear(currentDate.get(Calendar.YEAR));
 		GridDataFactory gdf = GridDataFactory.fillDefaults().grab(true, true).align(SWT.FILL, SWT.CENTER);
 		gdf.applyTo(dateTime);
 
 		createLabel(shell, "Riena DatePickerComposite"); //$NON-NLS-1$
 		DatePickerComposite textDatePicker = UIControlsFactory.createDatePickerComposite(shell, "test"); //$NON-NLS-1$
 		gdf.applyTo(textDatePicker);
 
 		IDateTextRidget datePickerRidget = (IDateTextRidget) SwtRidgetFactory.createRidget(textDatePicker);
		datePickerRidget.setFormat(IDateTextRidget.FORMAT_DDMMYYYY);
 		datePickerRidget.updateFromModel();
 		datePickerRidget.setMandatory(true);
 
 		shell.pack();
 		shell.open();
 		while (!shell.isDisposed()) {
 			if (!display.readAndDispatch()) {
 				display.sleep();
 			}
 		}
 		display.dispose();
 	}
 
 	private static void createLabel(Shell parent, String caption) {
 		Label label = new Label(parent, SWT.None);
 		label.setText(caption);
 	}
 
 }
