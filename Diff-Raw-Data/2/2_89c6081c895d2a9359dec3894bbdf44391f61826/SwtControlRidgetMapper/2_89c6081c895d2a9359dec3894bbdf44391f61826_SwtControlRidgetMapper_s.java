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
 package org.eclipse.riena.ui.ridgets.swt.uibinding;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.nebula.widgets.compositetable.CompositeTable;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.ToolItem;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.Widget;
 
 import org.eclipse.riena.internal.ui.ridgets.swt.ActionRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ComboRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.CompositeTableRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.DateTextRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.DateTimeRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.DecimalTextRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.EmbeddedTitleBarRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.LabelRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ListRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.MasterDetailsRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.MenuItemRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.MenuRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.MessageBoxRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ModuleTitleBarRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.MultipleChoiceRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.NumericTextRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ShellRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.SingleChoiceRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.StatuslineNumberRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.StatuslineRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.StatuslineUIProcessRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.TableRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.TextRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ToggleButtonRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.ToolItemRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.TreeRidget;
 import org.eclipse.riena.internal.ui.ridgets.swt.TreeTableRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.uibinding.IControlRidgetMapper;
 import org.eclipse.riena.ui.ridgets.uibinding.IMappingCondition;
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 import org.eclipse.riena.ui.swt.EmbeddedTitleBar;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 import org.eclipse.riena.ui.swt.MessageBox;
 import org.eclipse.riena.ui.swt.ModuleTitleBar;
 import org.eclipse.riena.ui.swt.Statusline;
 import org.eclipse.riena.ui.swt.StatuslineNumber;
 import org.eclipse.riena.ui.swt.StatuslineUIProcess;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Default implementation of {@link IControlRidgetMapper} for SWT.
  */
 public final class SwtControlRidgetMapper implements IControlRidgetMapper<Object> {
 
 	private static final int IGNORE_SWT_STYLE = -99;
 	// remove final here so that a SwtControlRidgetMapperTest can reinstantiate a new SwtControlRidgetMapper in tearDown
 	// dont add final again, maybe add a better fix later (i.e. protected removeMapping or initSingleton method) TODO
 	private static SwtControlRidgetMapper INSTANCE = new SwtControlRidgetMapper();
 
 	private final List<Mapping> mappings;
 
 	private SwtControlRidgetMapper() {
 		mappings = new ArrayList<Mapping>();
 		initDefaultMappings();
 	}
 
 	/**
 	 * Answer the singleton <code>SwtControlRidgetMapper</code>
 	 * 
 	 * @return the SwtControlRidgetMapper singleton
 	 */
 	public static SwtControlRidgetMapper getInstance() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Sets the default mapping of UI control-classes to a ridget-classes
 	 */
 	private void initDefaultMappings() {
 		addMapping(MenuItem.class, MenuItemRidget.class, new MenuItemCondition());
 		addMapping(MenuItem.class, MenuRidget.class, new MenuCondition());
 		addMapping(ToolItem.class, ToolItemRidget.class);
 		addMapping(Text.class, NumericTextRidget.class, new TypedTextWidgetCondition(UIControlsFactory.TYPE_NUMERIC));
 		addMapping(Text.class, DecimalTextRidget.class, new TypedTextWidgetCondition(UIControlsFactory.TYPE_DECIMAL));
 		addMapping(Text.class, DateTextRidget.class, new TypedTextWidgetCondition(UIControlsFactory.TYPE_DATE));
 		addMapping(Text.class, TextRidget.class);
 		addMapping(Label.class, LabelRidget.class);
 		addMapping(Table.class, TableRidget.class);
 		addMapping(Button.class, ToggleButtonRidget.class, SWT.CHECK);
 		addMapping(Button.class, ToggleButtonRidget.class, SWT.TOGGLE);
 		addMapping(Button.class, ToggleButtonRidget.class, SWT.RADIO);
 		addMapping(Button.class, ActionRidget.class);
 		addMapping(ChoiceComposite.class, SingleChoiceRidget.class, new SingleChoiceCondition());
 		addMapping(ChoiceComposite.class, MultipleChoiceRidget.class, new MultipleChoiceCondition());
 		addMapping(Combo.class, ComboRidget.class);
 		addMapping(DateTime.class, DateTimeRidget.class);
 		addMapping(org.eclipse.swt.widgets.List.class, ListRidget.class);
 		addMapping(Tree.class, TreeRidget.class, new TreeWithoutColumnsCondition());
 		addMapping(Tree.class, TreeTableRidget.class, new TreeWithColumnsCondition());
 		addMapping(Shell.class, ShellRidget.class);
 		addMapping(MessageBox.class, MessageBoxRidget.class);
 		addMapping(Statusline.class, StatuslineRidget.class);
 		addMapping(StatuslineNumber.class, StatuslineNumberRidget.class);
 		addMapping(StatuslineUIProcess.class, StatuslineUIProcessRidget.class);
 		addMapping(EmbeddedTitleBar.class, EmbeddedTitleBarRidget.class);
 		addMapping(ModuleTitleBar.class, ModuleTitleBarRidget.class);
 		addMapping(CompositeTable.class, CompositeTableRidget.class);
 		addMapping(MasterDetailsComposite.class, MasterDetailsRidget.class);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.uibinding.IControlRidgetMapper#addMapping(java.lang.Class,
 	 *      java.lang.Class)
 	 */
 	public void addMapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz) {
 		Mapping mapping = new Mapping(controlClazz, ridgetClazz);
 		mappings.add(mapping);
 	}
 
 	/**
 	 * Adds a mapping of a UI control-class to a ridget-class. The mapping will
 	 * only apply when the control has the specified swt style.
 	 * <p>
 	 * Example:
 	 * <p>
 	 * {@code addMapping(Button.class, ToggleButtonRidget.class, SWT.CHECK);}
 	 * <p>
 	 * Adding the same mapping twice has no effect (but is possible).
 	 * 
 	 * @param controlClazz
 	 *            - the class of the UI control (<code>Object</code>)
 	 * @param ridgetClazz
 	 *            - the class of the ridget
 	 * @param swtStyle
 	 *            - SWT style of the UI control (<code>Object</code>)
 	 */
 	public void addMapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz, int swtStyle) {
 		Mapping mapping = new Mapping(controlClazz, ridgetClazz, swtStyle);
 		mappings.add(mapping);
 	}
 
 	/**
 	 * Adds a mapping of a UI control-class to a ridget-class. The mapping will
 	 * only apply when the given condition evaluates to true.
 	 * <p>
 	 * Example:
 	 * <p>
 	 * {@code addMapping(Tree.class, TreeRidget.class, new
 	 * TreeWithoutColumnsCondition());}
 	 * <p>
 	 * Adding the same mapping twice has no effect (but is possible).
 	 * 
 	 * @param controlClazz
 	 *            - the class of the UI control (<code>Object</code>)
 	 * @param ridgetClazz
 	 *            - the class of the ridget
 	 * @param condition
 	 *            (non-null) - the condition to evaluate (non-null)
 	 * @see IMappingCondition
 	 */
 	public void addMapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz,
 			IMappingCondition condition) {
 		Mapping mapping = new Mapping(controlClazz, ridgetClazz, condition);
 		mappings.add(mapping);
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.uibinding.IControlRidgetMapper#getRidgetClass(java.lang.Class)
 	 */
 	public Class<? extends IRidget> getRidgetClass(Class<? extends Object> controlClazz) {
 		for (Mapping mapping : mappings) {
 			if (mapping.isMatching(controlClazz)) {
 				return mapping.getRidgetClazz();
 			}
 		}
		throw new BindingException("No ridget found for " + controlClazz.getSimpleName()); //$NON-NLS-1$
 	}
 
 	/**
 	 * @see org.eclipse.riena.ui.ridgets.uibinding.IControlRidgetMapper#getRidgetClass(java.lang.Object)
 	 */
 	public Class<? extends IRidget> getRidgetClass(Object control) {
 		// first look for matching mappings with style or condition
 		// TODO: to optimize avoid double iteration over mappings
 		for (Mapping mapping : mappings) {
 			if ((!mapping.isControlStyleIgnore() || mapping.hasCondition()) && mapping.isMatching(control)) {
 				return mapping.getRidgetClazz();
 			}
 		}
 		// then look for matching mappings without style and condition
 		for (Mapping mapping : mappings) {
 			if (mapping.isControlStyleIgnore() && !mapping.hasCondition() && mapping.isMatching(control)) {
 				return mapping.getRidgetClazz();
 			}
 		}
 		return getRidgetClass(control.getClass());
 	}
 
 	public void addSpecialMapping(String controlName, Class<? extends Object> ridgetClazz) {
 		// TODO Auto-generated method stub
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Mapping of UI control and ridget.
 	 */
 	public static final class Mapping {
 
 		private Class<? extends Object> controlClazz;
 		private Class<? extends IRidget> ridgetClazz;
 		private int controlStyle;
 		private IMappingCondition condition;
 
 		/**
 		 * Create a new mapping of UI control and ridget.
 		 * 
 		 * @param controlClazz
 		 *            - the class of the UI control
 		 * @param ridgetClazz
 		 *            - the class of the ridget
 		 */
 		public Mapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz) {
 			this(controlClazz, ridgetClazz, IGNORE_SWT_STYLE, null);
 		}
 
 		/**
 		 * Create a new mapping of UI control and ridget.
 		 * 
 		 * @param controlClazz
 		 *            - the class of the UI control
 		 * @param ridgetClazz
 		 *            - the class of the ridget
 		 * @param controlStyle
 		 *            - the SWT style of the UI control
 		 */
 		public Mapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz, int controlStyle) {
 			this(controlClazz, ridgetClazz, controlStyle, null);
 		}
 
 		/**
 		 * Create a new mapping of UI control and ridget.
 		 * 
 		 * @param controlClazz
 		 *            - the class of the UI control
 		 * @param ridgetClazz
 		 *            - the class of the ridget
 		 * @param condition
 		 *            - a non-null {@link IMappingCondition} instance
 		 */
 		public Mapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz,
 				IMappingCondition condition) {
 			this(controlClazz, ridgetClazz, IGNORE_SWT_STYLE, condition);
 			Assert.isNotNull(condition);
 		}
 
 		private Mapping(Class<? extends Object> controlClazz, Class<? extends IRidget> ridgetClazz, int controlStyle,
 				IMappingCondition condition) {
 			this.controlClazz = controlClazz;
 			this.ridgetClazz = ridgetClazz;
 			this.controlStyle = controlStyle;
 			this.condition = condition;
 		}
 
 		/**
 		 * Checks if this mapping is for given UI control.
 		 * 
 		 * @param control
 		 *            - the UI control-class
 		 * @return true, if the control matches; otherwise false
 		 */
 		public boolean isMatching(Class<? extends Object> controlClazz) {
 			if (isControlStyleIgnore() && condition == null) {
 				return getControlClazz().isAssignableFrom(controlClazz);
 			} else {
 				return false;
 			}
 		}
 
 		/**
 		 * Checks if this mapping is for given UI control.
 		 * 
 		 * @param control
 		 *            - the UI control
 		 * @return true, if the control matches; otherwise false
 		 */
 		public boolean isMatching(Object control) {
 			if (control.getClass() != getControlClazz()) {
 				return false;
 			}
 			if (condition != null && !condition.isMatch(control)) {
 				return false;
 			}
 			if (control instanceof Widget && !isControlStyleIgnore()) {
 				if ((((Widget) control).getStyle() & getControlStyle()) != getControlStyle()) {
 					return false;
 				}
 			}
 			return true;
 
 		}
 
 		public Class<? extends IRidget> getRidgetClazz() {
 			return ridgetClazz;
 		}
 
 		// helping methods
 		// ////////////////
 
 		private boolean isControlStyleIgnore() {
 			return getControlStyle() == IGNORE_SWT_STYLE;
 		}
 
 		private boolean hasCondition() {
 			return getCondition() != null;
 		}
 
 		private Class<?> getControlClazz() {
 			return controlClazz;
 		}
 
 		private int getControlStyle() {
 			return controlStyle;
 		}
 
 		public IMappingCondition getCondition() {
 			return condition;
 		}
 	}
 }
