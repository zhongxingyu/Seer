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
 
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

 import org.eclipse.riena.ui.core.marker.ErrorMessageMarker;
 import org.eclipse.riena.ui.core.marker.ValidationTime;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.marker.TooltipMessageMarkerViewer;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.ridgets.validation.ValidationRuleStatus;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Text ridgets using a {@link TooltipMessageMarkerViewer} for showing error
  * messages in their tooltip.
  */
 public final class SnippetTextRidget002 {
 
 	public static void main(String[] args) {
 		Display display = Display.getDefault();
 		try {
 			Shell shell = new Shell();
 			GridLayoutFactory.fillDefaults().numColumns(1).margins(10, 10).spacing(20, 10).applyTo(shell);
 
 			// #1 Showing error message returned by AlwaysWrongValidator
 			Text text0 = UIControlsFactory.createText(shell);
 			GridDataFactory.fillDefaults().grab(true, false).applyTo(text0);
 			ITextRidget textRidget0 = (ITextRidget) SwtRidgetFactory.createRidget(text0);
 			textRidget0.addValidationRule(new AlwaysWrongValidator(), ValidationTime.ON_UI_CONTROL_EDIT);
 			textRidget0.setText("Hover over this..."); //$NON-NLS-1$
 
 			// #2 Showing error message from an ErrorMessageMarker. 
 			Text text1 = UIControlsFactory.createText(shell);
 			GridDataFactory.fillDefaults().grab(true, false).applyTo(text1);
 			ITextRidget textRidget1 = (ITextRidget) SwtRidgetFactory.createRidget(text1);
 			textRidget1.setText("Hover over this..."); //$NON-NLS-1$
			textRidget1.addMarker(new ErrorMessageMarker("Brought to you by an ErrorMessageMarker")); //$NON-NLS-1$
 
 			// #3 Showing error message via addValidationMessage(...)
 			Text text2 = UIControlsFactory.createText(shell);
 			GridDataFactory.fillDefaults().grab(true, false).applyTo(text2);
 			ITextRidget textRidget2 = (ITextRidget) SwtRidgetFactory.createRidget(text2);
 			IValidator alwaysWrong = new AlwaysWrongValidator();
 			textRidget2.addValidationMessage("Brought to you by a ValidationMessage", alwaysWrong); //$NON-NLS-1$
 			textRidget2.addValidationRule(alwaysWrong, ValidationTime.ON_UI_CONTROL_EDIT);
 			textRidget2.setText("Hover over this..."); //$NON-NLS-1$
 
 			TooltipMessageMarkerViewer messageViewer = new TooltipMessageMarkerViewer();
 			messageViewer.addRidget(textRidget0);
 			messageViewer.addRidget(textRidget1);
 			messageViewer.addRidget(textRidget2);
 
 			shell.setSize(200, 200);
 			shell.open();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 		} finally {
 			display.dispose();
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Validator that always returns an error status.
 	 */
 	private static final class AlwaysWrongValidator implements IValidator {
 		public IStatus validate(Object value) {
			return ValidationRuleStatus.error(false, "This comes from the IStatus returned by the validator"); //$NON-NLS-1$
 		}
 
 	}
 }
