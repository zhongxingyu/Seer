 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.utils;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.browser.Browser;
 import org.eclipse.swt.custom.CCombo;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ProgressBar;
 import org.eclipse.swt.widgets.Scale;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Slider;
 import org.eclipse.swt.widgets.Spinner;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Tree;
 
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 import org.eclipse.riena.ui.swt.CompletionCombo;
 import org.eclipse.riena.ui.swt.DatePickerComposite;
 import org.eclipse.riena.ui.swt.ImageButton;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 import org.eclipse.riena.ui.swt.MessageBox;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 /**
  * Convenience class for creating SWT controls.
  * <p>
  * Will apply consistent style settings and an optional binding id. Can create
  * special instances of certain generic controls, such as numeric or date Text
  * fields.
  * <p>
  * Factory methods are annotated for compatibility with the SWT Designer tool.
  * 
  * @wbp.factory
  */
 public class UIControlsFactory {
 
 	/**
 	 * @since 1.2
 	 */
 	public static final String KEY_LNF_STYLE = "lnfStyle"; //$NON-NLS-1$
 	/**
 	 * Key to retrieve a control's type (for non-subclassable SWT controls, that
 	 * are mapped to different types of ridgets).
 	 */
 	public static final String KEY_TYPE = "type"; //$NON-NLS-1$
 	/**
 	 * @since 1.2
 	 */
 	public static final String LNF_STYLE_SECTION_LABEL = "sectionLabel"; //$NON-NLS-1$
 	/**
 	 * Type designation for 'numeric' Text controls.
 	 */
 	public static final String TYPE_NUMERIC = "numeric"; //$NON-NLS-1$
 	/**
 	 * Type designation for 'decimal' Text controls.
 	 */
 	public static final String TYPE_DECIMAL = "decimal"; //$NON-NLS-1$
 	/**
 	 * Type designation for 'date' Text controls.
 	 */
 	public static final String TYPE_DATE = "date"; //$NON-NLS-1$
 
 	/**
 	 * @since 1.2
 	 */
 	protected static final Color SHARED_BG_COLOR;
 
 	static {
 		SHARED_BG_COLOR = LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND);
		Assert.isNotNull(SHARED_BG_COLOR);
 	}
 
 	/**
 	 * Creates a {@link Browser} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style of the control to construct
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @since 1.2
 	 */
 	public static Browser createBrowser(Composite parent, int style) {
 		return new Browser(parent, style);
 	}
 
 	/**
 	 * Creates a {@link Browser} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style of the control to construct
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myBrowserId"
 	 * @since 1.2
 	 */
 	public static Browser createBrowser(Composite parent, int style, String bindingId) {
 		Browser result = createBrowser(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a push {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButton(Composite parent) {
 		Button button = new Button(parent, SWT.PUSH);
 		button.setBackground(SHARED_BG_COLOR);
 		return button;
 	}
 
 	/**
 	 * Creates a push {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the button; never null
 	 * 
 	 * @wbp.factory.parameter.source text "myButtonText"
 	 */
 	public static Button createButton(Composite parent, String text) {
 		Button result = new Button(parent, SWT.PUSH);
 		result.setText(text);
 		return result;
 	}
 
 	/**
 	 * Creates a push {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the button; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myButtonText"
 	 * @wbp.factory.parameter.source bindingId "myButtonId"
 	 */
 	public static Button createButton(Composite parent, String text, String bindingId) {
 		Button result = createButton(parent, text);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a checkbox {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButtonCheck(Composite parent) {
 		Button result = new Button(parent, SWT.CHECK);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	@Deprecated
 	public static Button createButtonCheck(Composite parent, String bindingId) {
 		Button button = createButtonCheck(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(button, bindingId);
 		return button;
 	}
 
 	/**
 	 * Creates a checkbox {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the button; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myButtonCheckText"
 	 * @wbp.factory.parameter.source bindingId "myButtonCheckId"
 	 * @since 2.0
 	 */
 	public static Button createButtonCheck(Composite parent, String text, String bindingId) {
 		Button result = createButtonCheck(parent);
 		result.setText(text);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a radio {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButtonRadio(Composite parent) {
 		Button result = new Button(parent, SWT.RADIO);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	@Deprecated
 	public static Button createButtonRadio(Composite parent, String bindingId) {
 		Button button = createButtonRadio(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(button, bindingId);
 		return button;
 	}
 
 	/**
 	 * Creates a radio {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the button; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myButtonRadioText"
 	 * @wbp.factory.parameter.source bindingId "myButtonRadioId"
 	 * @since 2.0
 	 */
 	public static Button createButtonRadio(Composite parent, String text, String bindingId) {
 		Button result = createButtonRadio(parent);
 		result.setText(text);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a toggle {@link Button}. This control has two states: selected
 	 * (pushed-in), not selected (pushed-out).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButtonToggle(Composite parent) {
 		return new Button(parent, SWT.TOGGLE);
 	}
 
 	@Deprecated
 	public static Button createButtonToggle(Composite parent, String bindingId) {
 		Button button = createButtonToggle(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(button, bindingId);
 		return button;
 	}
 
 	/**
 	 * Creates a toggle {@link Button}. This control has two states: selected
 	 * (pushed-in), not selected (pushed-out).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the button; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myButtonToggleText"
 	 * @wbp.factory.parameter.source bindingId "myButtonToggleId"
 	 * @since 2.0
 	 */
 	public static Button createButtonToggle(Composite parent, String text, String bindingId) {
 		Button result = createButtonToggle(parent);
 		result.setText(text);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a calendar control, for selecting a date.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @return a {@link DateTime} instance with the SWT.CALENDAR style
 	 */
 	public static DateTime createCalendar(Composite parent) {
 		DateTime result = new DateTime(parent, SWT.CALENDAR);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Creates a calendar control, for selecting a date.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * @return a {@link DateTime} instance with the SWT.CALENDAR style
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myCalendarId"
 	 */
 	public static DateTime createCalendar(Composite parent, String bindingId) {
 		DateTime result = createCalendar(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link CCombo} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @return a {@link CCombo} instance with the SWT.BORDER and SWT.READ_ONLY
 	 *         styles
 	 * 
 	 * @since 1.2
 	 */
 	public static CCombo createCCombo(Composite parent) {
 		CCombo result = new CCombo(parent, SWT.BORDER | SWT.READ_ONLY);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link CCombo} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * @return a {@link CCombo} instance with the SWT.BORDER and SWT.READ_ONLY
 	 *         style
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myComboId"
 	 * @since 1.2
 	 */
 	public static CCombo createCCombo(Composite parent, String bindingId) {
 		CCombo combo = createCCombo(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(combo, bindingId);
 		return combo;
 	}
 
 	/**
 	 * Creates a {@link CompletionCombo} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @return a {@link CompletionCombo} instance with the SWT.BORDER style
 	 * 
 	 * @since 2.0
 	 */
 	public static CompletionCombo createCompletionCombo(Composite parent) {
 		CompletionCombo result = new CompletionCombo(parent, SWT.BORDER);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link CompletionCombo} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * @return a {@link CCombo} instance with the SWT.BORDER style
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myComboId"
 	 * @since 2.0
 	 */
 	public static CompletionCombo createCompletionCombo(Composite parent, String bindingId) {
 		CompletionCombo combo = createCompletionCombo(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(combo, bindingId);
 		return combo;
 	}
 
 	/**
 	 * Creates a {@link ChoiceComposite} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            a style value that is supported by {@link Composite}
 	 * @param multipleSelection
 	 *            true to allow multiple selection (=check boxes), false for
 	 *            single selection (=radio buttons)
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source multipleSelection false
 	 */
 	public static ChoiceComposite createChoiceComposite(Composite parent, int style, boolean multipleSelection) {
 		return new ChoiceComposite(parent, style, multipleSelection);
 	}
 
 	/**
 	 * Creates a {@link ChoiceComposite} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            a style value that is supported by {@link Composite}
 	 * @param multipleSelection
 	 *            true to allow multiple selection (=check boxes), false for
 	 *            single selection (=radio buttons)
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source multipleSelection false
 	 * @wbp.factory.parameter.source bindingId "myChoiceCompositeId"
 	 */
 	public static ChoiceComposite createChoiceComposite(Composite parent, int style, boolean multipleSelection,
 			String bindingId) {
 		ChoiceComposite composite = createChoiceComposite(parent, style, multipleSelection);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(composite, bindingId);
 		return composite;
 	}
 
 	/**
 	 * Creates a {@link Combo} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * 
 	 * @return a Combo with the SWT.BORDER, SWT.DROP_DOWN and SWT.READ_ONLY
 	 *         styles
 	 */
 	public static Combo createCombo(Composite parent) {
 		return new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
 	}
 
 	/**
 	 * Creates a {@link Combo} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * @return a Combo with the SWT.BORDER, SWT.DROP_DOWN and SWT.READ_ONLY
 	 *         styles
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myComboId"
 	 */
 	public static Combo createCombo(Composite parent, String bindingId) {
 		Combo combo = createCombo(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(combo, bindingId);
 		return combo;
 	}
 
 	/**
 	 * Creates a {@link Composite} with SWT.NONE style and the standard
 	 * background.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Composite createComposite(Composite parent) {
 		return createComposite(parent, SWT.NONE);
 	}
 
 	/**
 	 * Creates a {@link Composite} with the given style and the standard
 	 * background.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style of the Composite
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 */
 	public static Composite createComposite(Composite parent, int style) {
 		Composite composite = new Composite(parent, style);
 		composite.setBackground(SHARED_BG_COLOR);
 		return composite;
 	}
 
 	/**
 	 * Creates a {@link Composite} with the given style and the standard
 	 * background.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style of the Composite
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myCompositeId"
 	 * @since 2.0
 	 */
 	public static Composite createComposite(Composite parent, int style, String bindingId) {
 		Composite composite = createComposite(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(composite, bindingId);
 		return composite;
 	}
 
 	/**
 	 * Creates a {@link DateTime} control. The styles SWT.DATE and SWT.DROP_DOWN
 	 * will be applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            a style bit for the desired length/verbosity of the control.
 	 *            Supported values are SWT.SHORT, SWT.MEDIUM, SWT.LONG
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.MEDIUM
 	 */
 	public static DateTime createDate(Composite parent, int style) {
 		DateTime result = new DateTime(parent, SWT.DATE | SWT.DROP_DOWN | style);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link DateTime} control. The styles SWT.DATE and SWT.DROP_DOWN
 	 * will be applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            a style bit for the desired length/verbosity of the control.
 	 *            Supported values are SWT.SHORT, SWT.MEDIUM, SWT.LONG
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.MEDIUM
 	 * @wbp.factory.parameter.source bindingId "myDateTimeId"
 	 */
 	public static DateTime createDate(Composite parent, int style, String bindingId) {
 		DateTime result = createDate(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link DatePickerComposite} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * 
 	 * @since 1.2
 	 */
 	public static DatePickerComposite createDatePickerComposite(Composite parent) {
 		DatePickerComposite result = new DatePickerComposite(parent, SWT.SINGLE | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_DATE);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link DatePickerComposite} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myDatePickerId"
 	 * @since 1.2
 	 */
 	public static DatePickerComposite createDatePickerComposite(Composite parent, String bindingId) {
 		DatePickerComposite result = createDatePickerComposite(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Group} with the given text, SWT.NONE style and the
 	 * standard background.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the group's title; never null
 	 * 
 	 * @wbp.factory.parameter.source text "myGroupText"
 	 */
 	public static Group createGroup(Composite parent, String text) {
 		Group result = new Group(parent, SWT.NONE);
 		result.setText(text);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Group} with the given text, SWT.NONE style and the
 	 * standard background.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text for the group's title; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myGroupText"
 	 * @wbp.factory.parameter.source bindingId "myGroupId"
 	 * @since 2.0
 	 */
 	public static Group createGroup(Composite parent, String text, String bindingId) {
 		Group result = createGroup(parent, text);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a button showing only an image (no border, no text).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null A composite control which
 	 *            will be the parent of the new instance; never null
 	 * @param image
 	 *            the image to show on the button
 	 * @param style
 	 *            the style of image button to construct (SWT.NONE, SWT.HOT)
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * @return an {@link ImageButton} instance
 	 * @since 2.0
 	 */
 	public static ImageButton createImageButton(Composite parent, Image image, int style, String bindingId) {
 		ImageButton btn = createImageButton(parent, style, bindingId);
 		btn.setImage(image);
 		return btn;
 	}
 
 	/**
 	 * Creates a button showing only an image (no border, no text).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null A composite control which
 	 *            will be the parent of the new instance; never null
 	 * @param style
 	 *            the style of image button to construct (SWT.NONE, SWT.HOT)
 	 * @return an {@link ImageButton} instance
 	 * @since 2.0
 	 */
 	public static ImageButton createImageButton(Composite parent, int style) {
 		ImageButton btn = new ImageButton(parent, style);
 		btn.setBackground(SHARED_BG_COLOR);
 		return btn;
 	}
 
 	/**
 	 * Creates a button showing only an image (no border, no text).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null A composite control which
 	 *            will be the parent of the new instance; never null
 	 * @param style
 	 *            the style of image button to construct (SWT.NONE, SWT.HOT)
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * @return an {@link ImageButton} instance
 	 * @since 2.0
 	 */
 	public static ImageButton createImageButton(Composite parent, int style, String bindingId) {
 		ImageButton btn = createImageButton(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(btn, bindingId);
 		return btn;
 	}
 
 	/**
 	 * Creates a {@link Label}, with SWT.NONE style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text to show in the Label; never null
 	 * 
 	 * @wbp.factory.parameter.source text "myLabelText"
 	 */
 	public static Label createLabel(Composite parent, String text) {
 		return createLabel(parent, text, SWT.NONE);
 	}
 
 	/**
 	 * Creates a {@link Label}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text to show in the Label; never null
 	 * @param style
 	 *            the style bits for this Label
 	 * 
 	 * @wbp.factory.parameter.source text "myLabelText"
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 */
 	public static Label createLabel(Composite parent, String text, int style) {
 		Label label = new Label(parent, style);
 		label.setText(text);
 		label.setBackground(SHARED_BG_COLOR);
 		return label;
 	}
 
 	/**
 	 * Creates a {@link Label}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text to show in the Label; never null
 	 * @param style
 	 *            the style bits for this Label
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myLabelText"
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myLabelId"
 	 */
 	public static Label createLabel(Composite parent, String text, int style, String bindingId) {
 		Label label = createLabel(parent, text, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(label, bindingId);
 		return label;
 	}
 
 	/**
 	 * Creates a {@link Label}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param text
 	 *            the text to show in the Label; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myLabelText"
 	 * @wbp.factory.parameter.source bindingId "myLabelId"
 	 * @since 1.2
 	 */
 	public static Label createLabel(Composite parent, String text, String bindingId) {
 		return createLabel(parent, text, SWT.None, bindingId);
 	}
 
 	/**
 	 * Creates a {@link Link} control with the SWT.NONE style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * 
 	 * @since 1.2
 	 */
 	public static Link createLink(Composite parent) {
 		return createLink(parent, SWT.NONE);
 	}
 
 	/**
 	 * Creates a {@link Link} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits for this Link
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @since 1.2
 	 */
 	public static Link createLink(Composite parent, int style) {
 		Link result = new Link(parent, style);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Link} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits for this Link
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myLinkId"
 	 * @since 1.2
 	 */
 	public static Link createLink(Composite parent, int style, String bindingId) {
 		Link result = createLink(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link List} control, with the SWT.BORDER and SWT.MULTI styles.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param hScroll
 	 *            if true, SWT.H_SCROLL will be added to the style, to enable
 	 *            the horizontal scrollbar
 	 * @param vScroll
 	 *            if true, SWT.V_SCROLL will be added to the style, to enable
 	 *            the vertical scrollbar
 	 */
 	public static List createList(Composite parent, boolean hScroll, boolean vScroll) {
 		int style = SWT.BORDER | SWT.MULTI;
 		if (hScroll) {
 			style |= SWT.H_SCROLL;
 		}
 		if (vScroll) {
 			style |= SWT.V_SCROLL;
 		}
 		return new List(parent, style);
 	}
 
 	/**
 	 * Creates a {@link List} control, with the SWT.BORDER and SWT.MULTI styles.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param hScroll
 	 *            if true, SWT.H_SCROLL will be added to the style, to enable
 	 *            the horizontal scrollbar
 	 * @param vScroll
 	 *            if true, SWT.V_SCROLL will be added to the style, to enable
 	 *            the vertical scrollbar
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myListId"
 	 */
 	public static List createList(Composite parent, boolean hScroll, boolean vScroll, String bindingId) {
 		List list = createList(parent, hScroll, vScroll);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(list, bindingId);
 		return list;
 	}
 
 	/**
 	 * Create a {@link MasterDetailsComposite} control with the SWT.NONE style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static MasterDetailsComposite createMasterDetails(Composite parent) {
 		return new MasterDetailsComposite(parent, SWT.NONE);
 	}
 
 	/**
 	 * Create a {@link MasterDetailsComposite} control with the SWT.NONE style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myMasterDetailId"
 	 */
 	public static MasterDetailsComposite createMasterDetails(Composite parent, String bindingId) {
 		MasterDetailsComposite masterDetails = new MasterDetailsComposite(parent, SWT.NONE);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(masterDetails, bindingId);
 		return masterDetails;
 	}
 
 	/**
 	 * Create a context {@link Menu}.
 	 * <p>
 	 * You must invoke {@code parent.setMenu(...)} to attach the result to the
 	 * {@code parent}.
 	 * 
 	 * @param parent
 	 *            the parent Control for this context menu; never null
 	 * @return a {@link Menu} instance; never null
 	 * 
 	 * @since 1.2
 	 */
 	public static Menu createMenu(Control parent) {
 		Menu item = new Menu(parent);
 		return item;
 	}
 
 	/**
 	 * Create a sub-menu on the given {@link MenuItem}.
 	 * <p>
 	 * You must invoke (@code parent.setMenu(...)} to attach the result to the
 	 * {@code parent}.
 	 * 
 	 * @param parent
 	 *            the parent {@link MenuItem}; never null;
 	 * 
 	 * @since 1.2
 	 */
 	public static Menu createMenu(MenuItem parent) {
 		Menu item = new Menu(parent);
 		return item;
 	}
 
 	/**
 	 * Add a {@link MenuItem} to the given {@link Menu}.
 	 * 
 	 * @param parent
 	 *            the parent {@link Menu}; never null
 	 * @param text
 	 *            the text to show on the item; never null
 	 * 
 	 * @wbp.factory.parameter.source text "myMenuItemText"
 	 * @since 1.2
 	 */
 	public static MenuItem createMenuItem(Menu parent, String text) {
 		MenuItem item = new MenuItem(parent, SWT.None);
 		item.setText(text);
 		return item;
 	}
 
 	/**
 	 * Add a {@link MenuItem} to the given {@link Menu}.
 	 * 
 	 * @param parent
 	 *            the parent {@link Menu}; never null
 	 * @param text
 	 *            the text to show on the item; never null
 	 * @param style
 	 *            the style bits for this item; see {@link MenuItem} for details
 	 * 
 	 * @wbp.factory.parameter.source text "myMenuItemText"
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @since 1.2
 	 */
 	public static MenuItem createMenuItem(Menu parent, String text, int style) {
 		MenuItem item = new MenuItem(parent, style);
 		item.setText(text);
 		return item;
 	}
 
 	/**
 	 * Add a {@link MenuItem} to the given {@link Menu}.
 	 * 
 	 * @param parent
 	 *            the parent {@link Menu}; never null
 	 * @param text
 	 *            the text to show on the item; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source text "myMenuItemText"
 	 * @wbp.factory.parameter.source bindingId "myMenuItemId"
 	 * @since 1.2
 	 */
 	public static MenuItem createMenuItem(Menu parent, String text, String bindingId) {
 		MenuItem item = new MenuItem(parent, SWT.None);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(item, bindingId);
 		item.setText(text);
 		return item;
 	}
 
 	/**
 	 * Creates a {@link MessageBox} (a small dialog to show messages).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static MessageBox createMessageBox(Composite parent) {
 		return new MessageBox(parent);
 	}
 
 	/**
 	 * Creates a {@link Shell}.
 	 * 
 	 * @param display
 	 *            the parent display; never null.
 	 */
 	public static Shell createShell(Display display) {
 		Assert.isNotNull(display);
 		Shell shell = new Shell(display);
 		shell.setBackground(SHARED_BG_COLOR);
 		return shell;
 	}
 
 	/**
 	 * Creates a {@link Table}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Table} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 */
 	public static Table createTable(Composite parent, int style) {
 		return new Table(parent, style);
 	}
 
 	/**
 	 * Creates a {@link Table}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Table} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myTableId"
 	 */
 	public static Table createTable(Composite parent, int style, String bindingId) {
 		Table table = createTable(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(table, bindingId);
 		return table;
 	}
 
 	/**
 	 * Creates a {@link Text} control with the SWT.SINGLE and SWT.BORDER styles.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createText(Composite parent) {
 		return new Text(parent, SWT.SINGLE | SWT.BORDER);
 	}
 
 	/**
 	 * Creates a {@link Text} control. The SWT.BORDER style will be applied
 	 * automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Text} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.BORDER
 	 */
 	public static Text createText(Composite parent, int style) {
 		return new Text(parent, style | SWT.BORDER);
 	}
 
 	/**
 	 * Creates a {@link Text} control. The SWT.BORDER style will be applied
 	 * automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Text} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.BORDER
 	 * @wbp.factory.parameter.source bindingId "myTextId"
 	 */
 	public static Text createText(Composite parent, int style, String bindingId) {
 		Text text = new Text(parent, style | SWT.BORDER);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(text, bindingId);
 		return text;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering times or dates (
 	 * {@link #TYPE_DATE}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createTextDate(Composite parent) {
 		Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_DATE);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering times or dates (
 	 * {@link #TYPE_DATE}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myTextDateId"
 	 */
 	public static Text createTextDate(Composite parent, String bindingId) {
 		Text result = createTextDate(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering decimal numbers (
 	 * {@link #TYPE_DECIMAL}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createTextDecimal(Composite parent) {
 		Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_DECIMAL);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering decimal numbers (
 	 * {@link #TYPE_DECIMAL}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myTextDecimalId"
 	 */
 	public static Text createTextDecimal(Composite parent, String bindingId) {
 		Text result = createTextDecimal(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering multiple lines of text. The
 	 * styles SWT.MULTI and SWT.BORDER are applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param hScroll
 	 *            if true, SWT.H_SCROLL will be added to the style, to enable
 	 *            the horizontal scrollbar
 	 * @param vScroll
 	 *            if true, SWT.V_SCROLL will be added to the style, to enable
 	 *            the vertical scrollbar
 	 */
 	public static Text createTextMulti(Composite parent, boolean hScroll, boolean vScroll) {
 		return createTextMulti(parent, SWT.NONE, hScroll, vScroll);
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering multiple lines of text. The
 	 * styles SWT.MULTI and SWT.BORDER are applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param hScroll
 	 *            if true, SWT.H_SCROLL will be added to the style, to enable
 	 *            the horizontal scrollbar
 	 * @param vScroll
 	 *            if true, SWT.V_SCROLL will be added to the style, to enable
 	 *            the vertical scrollbar
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myTextMultiId"
 	 */
 	public static Text createTextMulti(Composite parent, boolean hScroll, boolean vScroll, String bindingId) {
 		Text text = createTextMulti(parent, hScroll, vScroll);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(text, bindingId);
 		return text;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering multiple lines of text. The
 	 * styles SWT.MULTI and SWT.BORDER are applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Text} for details
 	 * @param hScroll
 	 *            if true, SWT.H_SCROLL will be added to the style, to enable
 	 *            the horizontal scrollbar
 	 * @param vScroll
 	 *            if true, SWT.V_SCROLL will be added to the style, to enable
 	 *            the vertical scrollbar
 	 * 
 	 * @since 1.2
 	 */
 	public static Text createTextMulti(Composite parent, int style, boolean hScroll, boolean vScroll) {
 		int txStyle = style | SWT.MULTI | SWT.BORDER;
 		if (hScroll) {
 			txStyle |= SWT.H_SCROLL;
 		}
 		if (vScroll) {
 			txStyle |= SWT.V_SCROLL;
 		}
 		return new Text(parent, txStyle);
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering multiple styles of text.
 	 * Automating line wrapping is enabled. The styles SWT.WRAP, SWT.MULTI and
 	 * SWT.BORDER are applied automatically.
 	 * 
 	 * @param hScroll
 	 *            if true, SWT.H_SCROLL will be added to the style, to enable
 	 *            the horizontal scrollbar
 	 * @param vScroll
 	 *            if true, SWT.V_SCROLL will be added to the style, to enable
 	 *            the vertical scrollbar
 	 * 
 	 * @since 1.2
 	 */
 	public static Text createTextMultiWrap(Composite parent, boolean hScroll, boolean vScroll) {
 		return createTextMulti(parent, SWT.WRAP, hScroll, vScroll);
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering non-decimal numbers (
 	 * {@link #TYPE_NUMERIC}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createTextNumeric(Composite parent) {
 		Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_NUMERIC);
 		return result;
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering non-decimal numbers (
 	 * {@link #TYPE_NUMERIC}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myTextNumericId"
 	 */
 	public static Text createTextNumeric(Composite parent, String bindingId) {
 		Text result = createTextNumeric(parent);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Create a {@link DateTime} control, configured for entering a time value.
 	 * The style SWT.TIME is applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            a style bit for the desired length/verbosity of the control.
 	 *            Supported values are SWT.SHORT, SWT.MEDIUM, SWT.LONG
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.MEDIUM
 	 */
 	public static DateTime createTime(Composite parent, int style) {
 		DateTime result = new DateTime(parent, SWT.TIME | style);
 		result.setBackground(SHARED_BG_COLOR);
 		return result;
 	}
 
 	/**
 	 * Create a {@link DateTime} control, configured for entering a time value.
 	 * The style SWT.TIME is applied automatically.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            a style bit for the desired length/verbosity of the control.
 	 *            Supported values are SWT.SHORT, SWT.MEDIUM, SWT.LONG
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.MEDIUM
 	 * @wbp.factory.parameter.source bindingId "myDateTimeId"
 	 */
 	public static DateTime createTime(Composite parent, int style, String bindingId) {
 		DateTime result = createTime(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(result, bindingId);
 		return result;
 	}
 
 	/**
 	 * Create a {@link Tree} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Tree} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 */
 	public static Tree createTree(Composite parent, int style) {
 		return new Tree(parent, style);
 	}
 
 	/**
 	 * Create a {@link Tree} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Tree} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myTreeId"
 	 */
 	public static Tree createTree(Composite parent, int style, String bindingId) {
 		Tree tree = createTree(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(tree, bindingId);
 		return tree;
 	}
 
 	/**
 	 * Create a {@link Spinner} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Spinner} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.BORDER
 	 * @wbp.factory.parameter.source bindingId "mySpinnerId"
 	 */
 	public static Spinner createSpinner(Composite parent, int style, String bindingId) {
 		Spinner spinner = createSpinner(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(spinner, bindingId);
 		return spinner;
 	}
 
 	/**
 	 * Create a {@link Spinner} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Spinner} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.BORDER
 	 */
 	public static Spinner createSpinner(Composite parent, int style) {
 		return new Spinner(parent, style);
 	}
 
 	/**
 	 * Create a {@link Spinner} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Spinner createSpinner(Composite parent) {
 		return createSpinner(parent, SWT.BORDER);
 	}
 
 	/**
 	 * Create a {@link Scale} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Scale} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @wbp.factory.parameter.source bindingId "myScaleId"
 	 */
 	public static Scale createScale(Composite parent, int style, String bindingId) {
 		Scale scale = createScale(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(scale, bindingId);
 		return scale;
 	}
 
 	/**
 	 * Create a {@link Scale} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Scale} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 */
 	public static Scale createScale(Composite parent, int style) {
 		return new Scale(parent, style);
 	}
 
 	/**
 	 * Create a {@link Scale} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Scale createScale(Composite parent) {
 		return createScale(parent, SWT.NONE);
 	}
 
 	/**
 	 * Create a {@link ProgressBar} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link ProgressBar} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.HORIZONTAL
 	 * @wbp.factory.parameter.source bindingId "myProgressBarId"
 	 */
 	public static ProgressBar createProgressBar(Composite parent, int style, String bindingId) {
 		ProgressBar progressBar = createProgressBar(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(progressBar, bindingId);
 		return progressBar;
 	}
 
 	/**
 	 * Create a {@link ProgressBar} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link ProgressBar} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.HORIZONTAL
 	 */
 	public static ProgressBar createProgressBar(Composite parent, int style) {
 		return new ProgressBar(parent, style);
 	}
 
 	/**
 	 * Create a {@link ProgressBar} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static ProgressBar createProgressBar(Composite parent) {
 		return createProgressBar(parent, SWT.HORIZONTAL);
 	}
 
 	/**
 	 * Create a {@link Slider} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Slider} for details
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.HORIZONTAL
 	 * @wbp.factory.parameter.source bindingId "mySliderId"
 	 */
 	public static Slider createSlider(Composite parent, int style, String bindingId) {
 		Slider slider = createSlider(parent, style);
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(slider, bindingId);
 		return slider;
 	}
 
 	/**
 	 * Create a {@link Slider} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits; see {@link Slider} for details
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.HORIZONTAL
 	 */
 	public static Slider createSlider(Composite parent, int style) {
 		return new Slider(parent, style);
 	}
 
 	/**
 	 * Create a {@link Slider} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Slider createSlider(Composite parent) {
 		return createSlider(parent, SWT.HORIZONTAL);
 	}
 
 	/**
 	 * Inspect the given {@link List} widget and return the recommended widget
 	 * height, in pixels, so that the given number of items can be shown at
 	 * once.
 	 * 
 	 * @param list
 	 *            a List instance; never null
 	 * @param numItems
 	 *            the number of items to show at once (1 or greater).
 	 * @return suggested height, in pixels
 	 */
 	public static int getHeightHint(List list, int numItems) {
 		Assert.isLegal(numItems > 0, "numItems must be greater than 0"); //$NON-NLS-1$
 		int items = list.getItemHeight() * numItems;
 		return items;
 	}
 
 	/**
 	 * Inspect the given {@link Button} widget and return the recommended width,
 	 * in pixels, so that the Button is wide enough to show it's contents
 	 * (image, text, etc.).
 	 * 
 	 * @param button
 	 *            a Button instance; never null
 	 * @return suggested width; in pixels
 	 */
 	public static int getWidthHint(Button button) {
 		GC gc = new GC(button.getDisplay());
 		try {
 			FontMetrics fm = gc.getFontMetrics();
 			int widthHint = Dialog.convertHorizontalDLUsToPixels(fm, IDialogConstants.BUTTON_WIDTH);
 			Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 			return Math.max(widthHint, minSize.x);
 		} finally {
 			gc.dispose();
 		}
 	}
 
 	/**
 	 * Inspect the given {@link Text} widget and return the recommended width,
 	 * in pixels, so that at least {@code numChars} can be shown in one line.
 	 * 
 	 * @param text
 	 *            a Text instance; never null
 	 * @param numChars
 	 *            the number of characters to show at once (1 or greater)
 	 * @return suggested width; in pixels
 	 */
 	public static int getWidthHint(Text text, int numChars) {
 		Assert.isLegal(numChars > 0, "numChars must be greater than 0"); //$NON-NLS-1$
 		GC gc = new GC(text.getDisplay());
 		try {
 			FontMetrics fm = gc.getFontMetrics();
 			int widthHint = fm.getAverageCharWidth() * numChars;
 			Point minSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
 			return Math.max(widthHint, minSize.x);
 		} finally {
 			gc.dispose();
 		}
 	}
 
 	/**
 	 * This class has only static methods and no state. Do not instantiate.
 	 * Constructor has been made protected to allow subclassing.
 	 * 
 	 * @since 1.2
 	 */
 	protected UIControlsFactory() {
 	}
 
 }
