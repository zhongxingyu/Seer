 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.utils;
 
 import java.beans.Beans;
 
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
 import org.eclipse.riena.ui.swt.InfoFlyout;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 import org.eclipse.riena.ui.swt.MessageBox;
 import org.eclipse.riena.ui.swt.StatusMeterWidget;
 import org.eclipse.riena.ui.swt.facades.SWTFacade;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.separator.Separator;
 
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
  * @since 3.0
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
 	 * @deprecated Using this field is no longer necessary. The background color
 	 *             of widget families can be configured via LnF properties such
 	 *             as:
 	 *             <p>
 	 *             {@code lnf.putLnfResource("Text.background", new ColorLnfResource(255, 255, 255));}
 	 */
 	@Deprecated
 	protected static final Color SHARED_BG_COLOR;
 
 	static {
 		SHARED_BG_COLOR = LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND);
 		Assert.isNotNull(SHARED_BG_COLOR, "You must define a color for LnfKeyConstants.SUB_MODULE_BACKGROUND"); //$NON-NLS-1$
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected static <T> T registerConstruction(final T control) {
 		UIControlsCounter.getInstance().registerConstruction(control);
 		return control;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected static <T> T bind(final T widget, final String bindingId) {
 		SWTBindingPropertyLocator.getInstance().setBindingProperty(widget, bindingId);
 		return widget;
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
 	public static Browser createBrowser(final Composite parent, final int style) {
 		return registerConstruction(new Browser(parent, style));
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
 	public static Browser createBrowser(final Composite parent, final int style, final String bindingId) {
 		return bind(createBrowser(parent, style), bindingId);
 	}
 
 	/**
 	 * Creates a push {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButton(final Composite parent) {
 		return registerConstruction(new Button(parent, SWT.PUSH));
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
 	public static Button createButton(final Composite parent, final String text) {
 		final Button result = createButton(parent);
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
 	public static Button createButton(final Composite parent, final String text, final String bindingId) {
 		return bind(createButton(parent, text), bindingId);
 	}
 
 	/**
 	 * Creates a checkbox {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButtonCheck(final Composite parent) {
 		return registerConstruction(new Button(parent, SWT.CHECK));
 	}
 
	@Deprecated
	public static Button createButtonCheck(final Composite parent, final String bindingId) {
		return bind(createButtonCheck(parent), bindingId);
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
 	public static Button createButtonCheck(final Composite parent, final String text, final String bindingId) {
 		final Button result = createButtonCheck(parent);
 		result.setText(text);
 		return bind(result, bindingId);
 	}
 
 	/**
 	 * Creates a radio {@link Button}.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButtonRadio(final Composite parent) {
 		return registerConstruction(new Button(parent, SWT.RADIO));
 	}
 
	@Deprecated
	public static Button createButtonRadio(final Composite parent, final String bindingId) {
		return bind(createButtonRadio(parent), bindingId);
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
 	public static Button createButtonRadio(final Composite parent, final String text, final String bindingId) {
 		final Button result = createButtonRadio(parent);
 		result.setText(text);
 		return bind(result, bindingId);
 	}
 
 	/**
 	 * Creates a toggle {@link Button}. This control has two states: selected
 	 * (pushed-in), not selected (pushed-out).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Button createButtonToggle(final Composite parent) {
 		return registerConstruction(new Button(parent, SWT.TOGGLE));
 	}
 
	@Deprecated
	public static Button createButtonToggle(final Composite parent, final String bindingId) {
		return bind(createButtonToggle(parent), bindingId);
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
 	public static Button createButtonToggle(final Composite parent, final String text, final String bindingId) {
 		final Button result = createButtonToggle(parent);
 		result.setText(text);
 		return bind(result, bindingId);
 	}
 
 	/**
 	 * Creates a calendar control, for selecting a date.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @return a {@link DateTime} instance with the SWT.CALENDAR style
 	 */
 	public static DateTime createCalendar(final Composite parent) {
 		final DateTime result = new DateTime(parent, SWT.CALENDAR);
 		registerConstruction(result);
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
 	public static DateTime createCalendar(final Composite parent, final String bindingId) {
 		return bind(createCalendar(parent), bindingId);
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
 	public static CCombo createCCombo(final Composite parent) {
 		return registerConstruction(new CCombo(parent, SWT.BORDER | SWT.READ_ONLY));
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
 	public static CCombo createCCombo(final Composite parent, final String bindingId) {
 		return bind(createCCombo(parent), bindingId);
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
 	public static CompletionCombo createCompletionCombo(final Composite parent) {
 		return createCompletionCombo(parent, SWT.BORDER);
 	}
 
 	/**
 	 * Creates a {@link CompletionCombo} control with the given style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style of the control to construct
 	 * 
 	 * @return a {@link CompletionCombo} instance
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @since 3.0
 	 */
 	public static CompletionCombo createCompletionCombo(final Composite parent, final int style) {
 		return registerConstruction(SWTFacade.getDefault().createCompletionCombo(parent, style));
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
 	public static CompletionCombo createCompletionCombo(final Composite parent, final String bindingId) {
 		return bind(createCompletionCombo(parent, SWT.BORDER), bindingId);
 	}
 
 	/**
 	 * Creates a {@link CompletionCombo} control with support for images.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @return a {@link CompletionCombo} instance with the SWT.BORDER style
 	 * 
 	 * @since 3.0
 	 */
 	public static CompletionCombo createCompletionComboWithImage(final Composite parent) {
 		return createCompletionComboWithImage(parent, SWT.BORDER);
 	}
 
 	/**
 	 * Creates a {@link CompletionCombo} control with support for images using
 	 * the given style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style of the control to construct
 	 * 
 	 * @return a {@link CompletionCombo} instance
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.NONE
 	 * @since 3.0
 	 */
 	public static CompletionCombo createCompletionComboWithImage(final Composite parent, final int style) {
 		return registerConstruction(SWTFacade.getDefault().createCompletionComboWithImage(parent, style));
 	}
 
 	/**
 	 * Creates a {@link CompletionCombo} control with support for images.
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
 	 * @since 3.0
 	 */
 	public static CompletionCombo createCompletionComboWithImage(final Composite parent, final String bindingId) {
 		return bind(createCompletionComboWithImage(parent, SWT.BORDER), bindingId);
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
 	public static ChoiceComposite createChoiceComposite(final Composite parent, final int style,
 			final boolean multipleSelection) {
 		return registerConstruction(new ChoiceComposite(parent, style, multipleSelection));
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
 	public static ChoiceComposite createChoiceComposite(final Composite parent, final int style,
 			final boolean multipleSelection, final String bindingId) {
 		return bind(createChoiceComposite(parent, style, multipleSelection), bindingId);
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
 	public static Combo createCombo(final Composite parent) {
 		return registerConstruction(new Combo(parent, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY));
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
 	public static Combo createCombo(final Composite parent, final String bindingId) {
 		return bind(createCombo(parent), bindingId);
 	}
 
 	/**
 	 * Creates a {@link Composite} with SWT.NONE style and the standard
 	 * background.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Composite createComposite(final Composite parent) {
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
 	public static Composite createComposite(final Composite parent, final int style) {
 		return registerConstruction(new Composite(parent, style));
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
 	public static Composite createComposite(final Composite parent, final int style, final String bindingId) {
 		return bind(createComposite(parent, style), bindingId);
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
 	public static DateTime createDate(final Composite parent, final int style) {
 		return registerConstruction(new DateTime(parent, SWT.DATE | SWT.DROP_DOWN | style));
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
 	public static DateTime createDate(final Composite parent, final int style, final String bindingId) {
 		return bind(createDate(parent, style), bindingId);
 	}
 
 	/**
 	 * Creates a {@link DatePickerComposite} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * 
 	 * @since 1.2
 	 */
 	public static DatePickerComposite createDatePickerComposite(final Composite parent) {
 		final DatePickerComposite result = new DatePickerComposite(parent, SWT.SINGLE | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_DATE);
 		return registerConstruction(result);
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
 	public static DatePickerComposite createDatePickerComposite(final Composite parent, final String bindingId) {
 		return bind(createDatePickerComposite(parent), bindingId);
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
 	public static Group createGroup(final Composite parent, final String text) {
 		final Group result = new Group(parent, SWT.NONE);
 		result.setText(text);
 		return registerConstruction(result);
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
 	public static Group createGroup(final Composite parent, final String text, final String bindingId) {
 		return bind(createGroup(parent, text), bindingId);
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
 	public static ImageButton createImageButton(final Composite parent, final Image image, final int style,
 			final String bindingId) {
 		final ImageButton btn = createImageButton(parent, style, bindingId);
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
 	public static ImageButton createImageButton(final Composite parent, final int style) {
 		return registerConstruction(new ImageButton(parent, style));
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
 	public static ImageButton createImageButton(final Composite parent, final int style, final String bindingId) {
 		return bind(createImageButton(parent, style), bindingId);
 	}
 
 	/**
 	 * Creates a platform specific {@link InfoFlyout} on the given composite.
 	 * 
 	 * @param parent
 	 *            the parent Composite; never null
 	 * @return an {@link InfoFlyout} instance; never null
 	 * @since 3.0
 	 */
 	public static InfoFlyout createInfoFlyout(final Composite parent) {
 		return registerConstruction(SWTFacade.getDefault().createInfoFlyout(parent));
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
 	public static Label createLabel(final Composite parent, final String text) {
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
 	public static Label createLabel(final Composite parent, final String text, final int style) {
 		final Label label = new Label(parent, style);
 		label.setText(text);
 		return registerConstruction(label);
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
 	public static Label createLabel(final Composite parent, final String text, final int style, final String bindingId) {
 		return bind(createLabel(parent, text, style), bindingId);
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
 	public static Label createLabel(final Composite parent, final String text, final String bindingId) {
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
 	public static Link createLink(final Composite parent) {
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
 	public static Link createLink(final Composite parent, final int style) {
 		return registerConstruction(new Link(parent, style));
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
 	public static Link createLink(final Composite parent, final int style, final String bindingId) {
 		return bind(createLink(parent, style), bindingId);
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
 	public static List createList(final Composite parent, final boolean hScroll, final boolean vScroll) {
 		int style = SWT.BORDER | SWT.MULTI;
 		if (hScroll) {
 			style |= SWT.H_SCROLL;
 		}
 		if (vScroll) {
 			style |= SWT.V_SCROLL;
 		}
 		return registerConstruction(new List(parent, style));
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
 	public static List createList(final Composite parent, final boolean hScroll, final boolean vScroll,
 			final String bindingId) {
 		return bind(createList(parent, hScroll, vScroll), bindingId);
 	}
 
 	/**
 	 * Create a {@link MasterDetailsComposite} control with the SWT.NONE style.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static MasterDetailsComposite createMasterDetails(final Composite parent) {
 		return registerConstruction(new MasterDetailsComposite(parent, SWT.NONE));
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
 	public static MasterDetailsComposite createMasterDetails(final Composite parent, final String bindingId) {
 		return bind(registerConstruction(new MasterDetailsComposite(parent, SWT.NONE)), bindingId);
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
 	public static Menu createMenu(final Control parent) {
 		return registerConstruction(new Menu(parent));
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
 	public static Menu createMenu(final MenuItem parent) {
 		return registerConstruction(new Menu(parent));
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
 	public static MenuItem createMenuItem(final Menu parent, final String text) {
 		final MenuItem item = new MenuItem(parent, SWT.None);
 		item.setText(text);
 		return registerConstruction(item);
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
 	public static MenuItem createMenuItem(final Menu parent, final String text, final int style) {
 		final MenuItem item = new MenuItem(parent, style);
 		item.setText(text);
 		return registerConstruction(item);
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
 	public static MenuItem createMenuItem(final Menu parent, final String text, final String bindingId) {
 		final MenuItem item = new MenuItem(parent, SWT.None);
 		item.setText(text);
 		return bind(registerConstruction(item), bindingId);
 	}
 
 	/**
 	 * Creates a {@link MessageBox} (a small dialog to show messages).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static MessageBox createMessageBox(final Composite parent) {
 		return registerConstruction(new MessageBox(parent));
 	}
 
 	/**
 	 * Creates a {@link Shell}.
 	 * 
 	 * @param display
 	 *            the parent display; never null.
 	 */
 	public static Shell createShell(final Display display) {
 		Assert.isNotNull(display);
 		return registerConstruction(new Shell(display));
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
 	public static Table createTable(final Composite parent, final int style) {
 		return registerConstruction(new Table(parent, style));
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
 	public static Table createTable(final Composite parent, final int style, final String bindingId) {
 		return bind(createTable(parent, style), bindingId);
 	}
 
 	/**
 	 * Creates a {@link Text} control with the SWT.SINGLE and SWT.BORDER styles.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createText(final Composite parent) {
 		return registerConstruction(new Text(parent, SWT.SINGLE | SWT.BORDER));
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
 	public static Text createText(final Composite parent, final int style) {
 		return registerConstruction(new Text(parent, style | SWT.BORDER));
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
 	public static Text createText(final Composite parent, final int style, final String bindingId) {
 		return bind(createText(parent, style), bindingId);
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering times or dates (
 	 * {@link #TYPE_DATE}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createTextDate(final Composite parent) {
 		final Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_DATE);
 		return registerConstruction(result);
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
 	public static Text createTextDate(final Composite parent, final String bindingId) {
 		return bind(createTextDate(parent), bindingId);
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering decimal numbers (
 	 * {@link #TYPE_DECIMAL}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createTextDecimal(final Composite parent) {
 		final Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_DECIMAL);
 		return registerConstruction(result);
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
 	public static Text createTextDecimal(final Composite parent, final String bindingId) {
 		return bind(createTextDecimal(parent), bindingId);
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
 	public static Text createTextMulti(final Composite parent, final boolean hScroll, final boolean vScroll) {
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
 	public static Text createTextMulti(final Composite parent, final boolean hScroll, final boolean vScroll,
 			final String bindingId) {
 		return bind(createTextMulti(parent, hScroll, vScroll), bindingId);
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
 	public static Text createTextMulti(final Composite parent, final int style, final boolean hScroll,
 			final boolean vScroll) {
 		int txStyle = style | SWT.MULTI | SWT.BORDER;
 		if (hScroll) {
 			txStyle |= SWT.H_SCROLL;
 		}
 		if (vScroll) {
 			txStyle |= SWT.V_SCROLL;
 		}
 		return registerConstruction(new Text(parent, txStyle));
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
 	public static Text createTextMultiWrap(final Composite parent, final boolean hScroll, final boolean vScroll) {
 		return createTextMulti(parent, SWT.WRAP, hScroll, vScroll);
 	}
 
 	/**
 	 * Creates a {@link Text} control for entering non-decimal numbers (
 	 * {@link #TYPE_NUMERIC}).
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Text createTextNumeric(final Composite parent) {
 		final Text result = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
 		result.setData(KEY_TYPE, TYPE_NUMERIC);
 		return registerConstruction(result);
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
 	public static Text createTextNumeric(final Composite parent, final String bindingId) {
 		return bind(createTextNumeric(parent), bindingId);
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
 	public static DateTime createTime(final Composite parent, final int style) {
 		return registerConstruction(new DateTime(parent, SWT.TIME | style));
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
 	public static DateTime createTime(final Composite parent, final int style, final String bindingId) {
 		return bind(createTime(parent, style), bindingId);
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
 	public static Tree createTree(final Composite parent, final int style) {
 		return registerConstruction(new Tree(parent, style));
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
 	public static Tree createTree(final Composite parent, final int style, final String bindingId) {
 		return bind(createTree(parent, style), bindingId);
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
 	public static Spinner createSpinner(final Composite parent, final int style, final String bindingId) {
 		return bind(createSpinner(parent, style), bindingId);
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
 	public static Spinner createSpinner(final Composite parent, final int style) {
 		return registerConstruction(new Spinner(parent, style));
 	}
 
 	/**
 	 * Create a {@link Spinner} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Spinner createSpinner(final Composite parent) {
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
 	public static Scale createScale(final Composite parent, final int style, final String bindingId) {
 		return bind(createScale(parent, style), bindingId);
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
 	public static Scale createScale(final Composite parent, final int style) {
 		return registerConstruction(new Scale(parent, style));
 	}
 
 	/**
 	 * Create a {@link Scale} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Scale createScale(final Composite parent) {
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
 	public static ProgressBar createProgressBar(final Composite parent, final int style, final String bindingId) {
 		return bind(createProgressBar(parent, style), bindingId);
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
 	public static ProgressBar createProgressBar(final Composite parent, final int style) {
 		return registerConstruction(new ProgressBar(parent, style));
 	}
 
 	/**
 	 * Create a {@link ProgressBar} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static ProgressBar createProgressBar(final Composite parent) {
 		return createProgressBar(parent, SWT.HORIZONTAL);
 	}
 
 	/**
 	 * Create a {@link StatusMeterWidget} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @since 3.0
 	 */
 	public static StatusMeterWidget createStatusMeter(final Composite parent) {
 		return registerConstruction(new StatusMeterWidget(parent));
 	}
 
 	/**
 	 * Create a {@link StatusMeterWidget} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param bindingId
 	 *            the binding property; never null; must not be empty. The given
 	 *            value will also be assigned to the Ridget that is paired to
 	 *            this control.
 	 * 
 	 * @wbp.factory.parameter.source bindingId "myStatusMeterId"
 	 * @since 3.0
 	 */
 	public static StatusMeterWidget createStatusMeter(final Composite parent, final String bindingId) {
 		return bind(createStatusMeter(parent), bindingId);
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
 	public static Slider createSlider(final Composite parent, final int style, final String bindingId) {
 		return bind(createSlider(parent, style), bindingId);
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
 	public static Slider createSlider(final Composite parent, final int style) {
 		return registerConstruction(new Slider(parent, style));
 	}
 
 	/**
 	 * Create a {@link Slider} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 */
 	public static Slider createSlider(final Composite parent) {
 		return createSlider(parent, SWT.HORIZONTAL);
 	}
 
 	/**
 	 * Create a {@link Separator} control.
 	 * 
 	 * @param parent
 	 *            the parent composite; never null
 	 * @param style
 	 *            the style bits
 	 * 
 	 * @wbp.factory.parameter.source style org.eclipse.swt.SWT.HORIZONTAL
 	 * @since 3.0
 	 */
 	public static Separator createSeparator(final Composite parent, final int style) {
 		return createSeparatorTwoLine(parent, SWT.HORIZONTAL, LnfKeyConstants.TITLEBAR_SEPARATOR_FIRST_LINE_FOREGROUND,
 				LnfKeyConstants.TITLEBAR_SEPARATOR_SECOND_LINE_FOREGROUND);
 	}
 
 	private static Separator createSeparatorTwoLine(final Composite parent, final int style,
 			final String firstLineColor, final String secondLineColor) {
 
 		if (Beans.isDesignTime()) {
 			// design time fallback
 			final Display display = parent.getDisplay();
 			final Separator result = new Separator(parent, style, display.getSystemColor(SWT.COLOR_BLACK),
 					display.getSystemColor(SWT.COLOR_WHITE));
 			return registerConstruction(result);
 		}
 		final Separator result = new Separator(parent, style, LnfManager.getLnf().getColor(firstLineColor), LnfManager
 				.getLnf().getColor(secondLineColor));
 		return registerConstruction(result);
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
 	public static int getHeightHint(final List list, final int numItems) {
 		Assert.isLegal(numItems > 0, "numItems must be greater than 0"); //$NON-NLS-1$
 		final int items = list.getItemHeight() * numItems;
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
 	public static int getWidthHint(final Button button) {
 		final GC gc = new GC(button.getDisplay());
 		try {
 			final FontMetrics fm = gc.getFontMetrics();
 			final int widthHint = Dialog.convertHorizontalDLUsToPixels(fm, IDialogConstants.BUTTON_WIDTH);
 			final Point minSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
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
 	public static int getWidthHint(final Text text, final int numChars) {
 		Assert.isLegal(numChars > 0, "numChars must be greater than 0"); //$NON-NLS-1$
 		final GC gc = new GC(text.getDisplay());
 		try {
 			final FontMetrics fm = gc.getFontMetrics();
 			final int widthHint = fm.getAverageCharWidth() * numChars;
 			final Point minSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
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
