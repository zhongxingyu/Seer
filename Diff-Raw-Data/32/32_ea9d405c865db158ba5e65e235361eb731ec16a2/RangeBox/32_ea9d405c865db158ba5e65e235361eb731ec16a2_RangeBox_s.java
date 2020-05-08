 /*-
  * Copyright © 2013 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.richbeans.components.scalebox;
 
 import java.util.List;
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.FocusAdapter;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 
 import uk.ac.gda.doe.DOEUtils;
 import uk.ac.gda.richbeans.beans.IExpressionManager;
 import uk.ac.gda.richbeans.beans.IRangeWidget;
 import uk.ac.gda.richbeans.components.scalebox.internal.RangeDialog;
 import uk.ac.gda.richbeans.event.ValueEvent;
 
 import com.swtdesigner.SWTResourceManager;
 
 /**
  * This class is a bounded, united object which can be used for a general value input in gda SWT UI. In addition to the
  * standard single value the box can save value as a list of values or an iteration.
  * 
  */
 public class RangeBox extends NumberBox implements IRangeWidget {
 
 	private String dialogTitle;
 	private boolean rangeOnly = false;
 	private boolean error = false;
 	private RangeDialog dialog = null;
 
 	/**
 	 * Constructor to overrule the default icon and tooltip for the button.
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param buttonText
 	 * @param buttonToolTip
 	 */
 	public RangeBox(final Composite parent, final int style, String buttonText, String buttonToolTip) {
 		this(parent, style);
 		this.button.setImage(null);
 		this.button.setToolTipText(buttonToolTip);
 		this.button.setText(buttonText);
 		final GridData bLayout = (GridData) button.getLayoutData();
 		bLayout.widthHint = SWT.DEFAULT;
 	}
 
 	/**
 	 * Create the composite RangeBox for use in GDA UI. After creating the composite set the scale properties (max, min
 	 * etc.). These are used at run time by a lazy initiated form.
 	 * 
 	 * @param parent
 	 * @param style
 	 */
 	public RangeBox(final Composite parent, final int style) {
 
 		super(parent, style);
 
 		this.buttonSelection = new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				dialog = new RangeDialog(getShell(), isIntegerBox());
 				dialog.setRangeOnly(isRangeOnly());
 				dialog.create();
 				if (getDialogTitle() != null)
 					dialog.getShell().setText(getDialogTitle());
 				dialog.setUnit(getUnit());
 				dialog.setValue(getValue());
 				dialog.setBounds(RangeBox.this);
 				if (dialog.open() == Window.OK) {
 					checkValue(dialog.getValue());
 				}
 				dialog = null;
 			}
 		};
 
 		setButtonVisible(true);
 		this.button.setToolTipText("Open range form which allows this value to be defined as a range.");
 		this.button.setImage(SWTResourceManager.getImage(RangeBox.class, "/icons/arrow_divide.png"));
 		this.button.setText("");
 		final GridData bLayout = (GridData) button.getLayoutData();
 
 		// Platform dependant sizes but they work on linux RHEL5 ok.
 		bLayout.widthHint = 29;
 
 	}
 
 	@Override
 	public void closeDialog() {
 		if (dialog != null)
 			dialog.close();
 	}
 
 	@Override
 	protected void createTextListeners(final StyledText text) {
 		this.focusListener = new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
				if (!isOn())
 					return;
 				try {
 					off();
 					checkValue(text.getText());
 				} finally {
 					on();
 				}
 			}
 		};
 		text.addFocusListener(focusListener);
 
 		this.modifyListener = new ModifyListener() {
 			@Override
 			public void modifyText(final ModifyEvent e) {
 				checkValue(text.getText());
 				final ValueEvent evt = new ValueEvent(RangeBox.this, getFieldName());
 				evt.setValue(getValue());
 				eventDelegate.notifyValueListeners(evt);
 			}
 		};
 		text.addModifyListener(modifyListener);
 
 	}
 
 	@Override
 	protected void checkValue(String txt) {
 
 		if (txt == null)
 			return;
 		if ("".equals(txt.trim()))
 			return;
 		if ("-".equals(txt.trim()))
 			return;
 
 		// If a simple double value then super can deal with it.
 		try {
 			final Pattern regExp = getRegExpression();
 			if (regExp.matcher(txt).matches() || regExp.matcher(txt + " " + unit).matches()) {
 				super.checkValue(txt);
 				return;
 			}
 
 		} catch (Throwable ignored) {
 			// We parse the value as if it was multiple
 		}
 		final String value = DOEUtils.removeUnit(txt, unit);
 		if (unit != null) {
 			txt = value.trim() + " " + unit;
 		}
 		if (!txt.equals(text.getText())) {
 			final int pos = text.getCaretOffset();
 			text.setText(txt);
 			text.setCaretOffset(pos);
 		}
 
 		// We have stripped unit from value so do not need to match it.
 		boolean isList = DOEUtils.isList(value, getDecimalPlaces(), null);
 		if (isRangeOnly())
 			isList = false;
 		final boolean isRange = DOEUtils.isRange(value, getDecimalPlaces(), null);
 		if (!isList && !isRange) {
 			error = true;
 			if (this.red == null)
 				red = getDisplay().getSystemColor(SWT.COLOR_RED);
 			if (!red.isDisposed())
 				text.setForeground(red);
 
 			if (isRangeOnly()) {
 				setTooltipOveride("A range should be entered in the form: <start>; <stop>; <increment>.\n");
 
 			} else {
 				setTooltipOveride("A range should be entered in the form: <start>; <stop>; <increment>.\n\n"
 						+ "A list should be a comma separerated list of numbers (two or more).");
 			}
 
 		} else {
 			error = false;
 
 			// Check bounds values
 			if (!checkBounds(value)) {
 				return;
 			}
 
 			setTooltipOveride(null);
 			if (isEditable()) {
 				if (black == null)
 					black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
 				if (!black.isDisposed())
 					text.setForeground(black);
 			} else {
 				if (grey == null)
 					grey = getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY);
 				if (!grey.isDisposed())
 					text.setForeground(grey);
 			}
 		}
 	}
 
 	private boolean checkBounds(final String value) {
 
 		error = false;
 		final List<? extends Number> dbls = DOEUtils.expand(value);
 		for (Number val : dbls) {
 			if (!isValidBounds(val.doubleValue())) {
 				error = true;
 				if (this.red == null)
 					red = getDisplay().getSystemColor(SWT.COLOR_RED);
 				if (!red.isDisposed())
 					text.setForeground(red);
 
 				setTooltipOveride("The range '" + value + "' would give the value '" + val
 						+ "' which is out of bounds.");
 				return false;
 			}
 		}
 		return true;
 	}
 
 	@Override
 	public void setValue(final Object value) {
 		if (value == null) {
 			this.text.setText("");
 			return;
 		}
 		checkValue(value.toString());
 	}
 
 	@Override
 	public String getValue() {
 
 		final String txt = text.getText();
 		if (txt == null)
 			return null;
 		if ("".equals(txt.trim()))
 			return null;
 
 		return DOEUtils.removeUnit(txt, unit);
 	}
 
 	/**
 	 * Expressions are not currently evaluated by RangeBoxes
 	 */
 	@Override
 	public void setExpressionManager(IExpressionManager man) {
 		this.expressionManager = null;
 	}
 
 	@Override
 	public boolean isExpressionAllowed() {
 		return false;
 	}
 
 	public List<? extends Number> getRange() {
 		return DOEUtils.expand(getValue());
 	}
 
 	public String getDialogTitle() {
 		return dialogTitle;
 	}
 
 	public void setDialogTitle(String dialogTitle) {
 		this.dialogTitle = dialogTitle;
 	}
 
 	public boolean isRangeOnly() {
 		return rangeOnly;
 	}
 
 	public void setRangeOnly(boolean rangeOnly) {
 		this.rangeOnly = rangeOnly;
 	}
 
 	public boolean isError() {
 		return error;
 	}
 
 }
