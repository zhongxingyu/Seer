 /* OperationAgentManager.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		Mar 20, 2012 Created by pao
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
  */
 package org.zkoss.zats.mimic.impl.operation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.zkoss.zats.mimic.impl.Util;
 import org.zkoss.zats.mimic.operation.CheckAgent;
 import org.zkoss.zats.mimic.operation.ClickAgent;
 import org.zkoss.zats.mimic.operation.CloseAgent;
 import org.zkoss.zats.mimic.operation.DragAgent;
 import org.zkoss.zats.mimic.operation.FocusAgent;
 import org.zkoss.zats.mimic.operation.HoverAgent;
 import org.zkoss.zats.mimic.operation.KeyStrokeAgent;
 import org.zkoss.zats.mimic.operation.MultipleSelectAgent;
 import org.zkoss.zats.mimic.operation.OpenAgent;
 import org.zkoss.zats.mimic.operation.OperationAgent;
 import org.zkoss.zats.mimic.operation.RenderAgent;
 import org.zkoss.zats.mimic.operation.SelectAgent;
 import org.zkoss.zats.mimic.operation.SizeAgent;
 import org.zkoss.zats.mimic.operation.TypeAgent;
import org.zkoss.zhtml.Input;
 import org.zkoss.zk.ui.AbstractComponent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.HtmlBasedComponent;
 import org.zkoss.zul.A;
 import org.zkoss.zul.Bandbox;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Checkbox;
 import org.zkoss.zul.Combobox;
 import org.zkoss.zul.Comboitem;
 import org.zkoss.zul.Datebox;
 import org.zkoss.zul.Decimalbox;
 import org.zkoss.zul.Detail;
 import org.zkoss.zul.Doublebox;
 import org.zkoss.zul.Doublespinner;
 import org.zkoss.zul.Grid;
 import org.zkoss.zul.Group;
 import org.zkoss.zul.Groupbox;
 import org.zkoss.zul.Intbox;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listgroup;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.Longbox;
 import org.zkoss.zul.Menuitem;
 import org.zkoss.zul.Panel;
 import org.zkoss.zul.Spinner;
 import org.zkoss.zul.Tab;
 import org.zkoss.zul.Timebox;
 import org.zkoss.zul.Tree;
 import org.zkoss.zul.Treeitem;
 import org.zkoss.zul.Window;
 import org.zkoss.zul.impl.InputElement;
 
 public class OperationAgentManager {
 	private static Map<Key, OperationAgentBuilder<? extends OperationAgent>> builders;
 
 	static {
 		builders = new HashMap<OperationAgentManager.Key, OperationAgentBuilder<? extends OperationAgent>>();
 
 		// TODO load default implement
 		registerBuilder("5.0.0", "*", AbstractComponent.class, ClickAgent.class, new GenericClickAgentBuilder());
 		registerBuilder("5.0.0", "*", AbstractComponent.class, KeyStrokeAgent.class, new GenericKeyStrokeAgentBuilder());
 
 		// the focus
 		registerBuilder("5.0.0", "*", InputElement.class, FocusAgent.class, new GenericFocusAgentBuilder());
 		registerBuilder("5.0.0", "*", A.class, FocusAgent.class, new GenericFocusAgentBuilder());
 		registerBuilder("5.0.0", "*", Button.class, FocusAgent.class, new GenericFocusAgentBuilder());
 		registerBuilder("5.0.0", "*", Checkbox.class, FocusAgent.class, new GenericFocusAgentBuilder());
 		registerBuilder("5.0.0", "*", Listbox.class, FocusAgent.class, new GenericFocusAgentBuilder());
 		registerBuilder("5.0.0", "*", Tree.class, FocusAgent.class, new GenericFocusAgentBuilder());
 		
 		// the inputs
 		registerBuilder("5.0.0", "*", InputElement.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.TextTypeAgentBuilder());
 		registerBuilder("5.0.0", "*", Intbox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.IntegerTypeAgentBuilder());
 		registerBuilder("5.0.0", "*", Longbox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.IntegerStringTypeAgentBuilder());
 		registerBuilder("5.0.0", "*", Spinner.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.IntegerTypeAgentBuilder());
 
 		registerBuilder("5.0.0", "*", Decimalbox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DecimalStringTypeAgentBuilder());
 		registerBuilder("5.0.0", "*", Doublebox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DecimalTypeAgentBuilder());
 		registerBuilder("5.0.0", "*", Doublespinner.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DecimalTypeAgentBuilder());
 
 		registerBuilder("5.0.0", "*", Datebox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.DateTypeAgentBuilder());
 		registerBuilder("5.0.0", "*", Timebox.class, TypeAgent.class,
 				new AbstractTypeAgentBuilder.TimeTypeAgentBuilder());
		//colorbox in zkex.jar which is optional
		try{
			registerBuilder("5.0.0", "*", "org.zkoss.zkex.zul.Colorbox", TypeAgent.class,
					"org.zkoss.zats.mimic.impl.operation.ColorTypeAgentBuilder");
		}catch (Exception e) {
			// doesn't exist
		}
 
 		// the check
 		registerBuilder("5.0.0", "*", Menuitem.class, CheckAgent.class, new GenericCheckAgentBuilder());
 		// the check of check box and radio button
 		registerBuilder("5.0.0", "*", Checkbox.class, CheckAgent.class, new GenericCheckAgentBuilder());
		// the check of zhtml
		registerBuilder("5.0.0", "*", Input.class, CheckAgent.class, new GenericCheckAgentBuilder());
 		
 		// the single select
 		registerBuilder("5.0.0", "*", Comboitem.class, SelectAgent.class,
 				new AbstractSelectAgentBuilder.ComboitemSelectAgentBuilder());
 		registerBuilder("5.0.0", "*", Tab.class, SelectAgent.class,
 				new AbstractSelectAgentBuilder.TabSelectAgentBuilder());
 		registerBuilder("5.0.0", "*", Listitem.class, SelectAgent.class,
 				new AbstractSelectAgentBuilder.LisitemSelectAgentBuilder());
 		registerBuilder("5.0.0", "*", Treeitem.class, SelectAgent.class,
 				new AbstractSelectAgentBuilder.TreeSelectAgentBuilder());
 		
 		// the multiple select
 		registerBuilder("5.0.0", "*", Listitem.class, MultipleSelectAgent.class,
 				new AbstractMultipleSelectAgentBuilder.ListitemMultipleSelectAgentBuilder());
 		registerBuilder("5.0.0", "*", Treeitem.class, MultipleSelectAgent.class,
 				new AbstractMultipleSelectAgentBuilder.TreeitemMultipleSelectAgentBuilder());
 		
 		// the open
 		registerBuilder("5.0.0", "*", Groupbox.class, OpenAgent.class, new GenericOpenAgentBuilder());
 		registerBuilder("5.0.0", "*", Detail.class, OpenAgent.class, new GenericOpenAgentBuilder());
 		registerBuilder("5.0.0", "*", Group.class, OpenAgent.class, new GenericOpenAgentBuilder());
 		registerBuilder("5.0.0", "*", Listgroup.class, OpenAgent.class, new GenericOpenAgentBuilder());
 		registerBuilder("5.0.0", "*", Treeitem.class, OpenAgent.class, new GenericOpenAgentBuilder());
 		registerBuilder("5.0.0", "*", Bandbox.class, OpenAgent.class, new TextboxOpenAgentBuilder());
 		registerBuilder("5.0.0", "*", Combobox.class, OpenAgent.class, new TextboxOpenAgentBuilder());
 		
 		// the close
 		registerBuilder("5.0.0", "*", Window.class, CloseAgent.class, new GenericCloseAgentBuilder());
 		registerBuilder("5.0.0", "*", Panel.class, CloseAgent.class, new GenericCloseAgentBuilder());
 		registerBuilder("5.0.0", "*", Tab.class, CloseAgent.class, new GenericCloseAgentBuilder());
 
 		// the render
 		registerBuilder("5.0.0", "*", Listbox.class, RenderAgent.class, new ListboxRendererAgentBuilder());
 		registerBuilder("5.0.0", "*", Grid.class, RenderAgent.class, new GridRendererAgentBuilder());
 
 		// the maximize
 		registerBuilder("5.0.0", "*", Window.class, SizeAgent.class, new GenericSizeAgentBuilder());
 		registerBuilder("5.0.0", "*", Panel.class, SizeAgent.class, new GenericSizeAgentBuilder());
 		
 		//drag & drop
 		registerBuilder("5.0.0", "*", HtmlBasedComponent.class, DragAgent.class, new GenericDragAgentBuilder());
 		
 		//hover
 		registerBuilder("5.0.0", "*", HtmlBasedComponent.class, HoverAgent.class, new GenericHoverAgentBuilder());
 		
 		//----------special case ---
 		
 		// the ckeditor (optional)
 		try {
 			registerBuilder("5.0.0", "*", "org.zkforge.ckez.CKeditor", TypeAgent.class,
 					"org.zkoss.zats.mimic.impl.operation.AbstractTypeAgentBuilder$TextTypeAgentBuilder");
 		} catch (Exception e) {
 			// ckeditor doesn't exist
 		}
 		
 	}
 
 	/**
 	 * Register a operation builder mapping to component and operation. We can
 	 * specify zk version worked on. The version text could be normal version
 	 * format (e.g 6.0.0 or 5.0.7.1) or "*" sign means no specify. If specify
 	 * version range doesn't include current zk version at runtime, this
 	 * register will be ignored.
 	 * <p/>
 	 * 
 	 * Use this API if the component is only in a particular zk version only to
 	 * avoid initial exception.
 	 * <p/>
 	 * 
 	 * @param startVersion
 	 *            start version (include)
 	 * @param endVersion
 	 *            end version (include)
 	 * @param compClazz
 	 *            the component class that builder maps to ( *notice: it should
 	 *            not specify interface)
 	 * @param opClass
 	 *            the operation class that builder maps to
 	 * @param builder
 	 *            operation builder
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public static <T extends OperationAgent> void registerBuilder(String startVersion, String endVersion,
 			String compClazz, Class<T> opClass, String builderClazz) {
 		if (startVersion == null || endVersion == null || compClazz == null || opClass == null || builderClazz == null)
 			throw new IllegalArgumentException();
 
 		if (!Util.checkVersion(startVersion, endVersion))
 			return;
 
 		Class clz = null;
 		try {
 			clz = Class.forName(compClazz);
 		} catch (ClassNotFoundException e) {
 			throw new IllegalArgumentException("compClazz " + compClazz + " not found ", e);
 		}
 		OperationAgentBuilder<T> builder = null;
 		try {
 			Class buildClz = Class.forName(builderClazz);
 			builder = (OperationAgentBuilder) buildClz.newInstance();
 		} catch (Exception x) {
 			throw new IllegalArgumentException(x.getMessage(), x);
 		}
 
 		if (Component.class.isAssignableFrom(clz)) {
 			registerBuilder(startVersion, endVersion, clz, opClass, builder);
 		} else {
 			throw new IllegalArgumentException("compClazz " + compClazz + " is not a Component");
 		}
 	}
 
 	/**
 	 * register a operation builder mapping to component and operation. We can
 	 * specify zk version worked on. The version text could be normal version
 	 * format (e.g 6.0.0 or 5.0.7.1) or "*" sign means no specify. If specify
 	 * version range doesn't include current zk version at runtime, this
 	 * register will be ignored.
 	 * 
 	 * @param startVersion
 	 *            start version (include)
 	 * @param endVersion
 	 *            end version (include)
 	 * @param compClazz
 	 *            the component class that builder maps to ( *notice: it should
 	 *            not specify interface)
 	 * @param opClass
 	 *            the operation class that builder maps to
 	 * @param builder
 	 *            operation builder
 	 */
 	public static <T extends OperationAgent, C extends Component> void registerBuilder(String startVersion,
 			String endVersion, Class<C> compClazz, Class<T> opClass, OperationAgentBuilder<T> builder) {
 
 		if (startVersion == null || endVersion == null || compClazz == null || opClass == null || builder == null)
 			throw new IllegalArgumentException();
 
 		if (!Util.checkVersion(startVersion, endVersion))
 			return;
 
 		// component and operation classes mapping to builder
 		// builder would be replace by later register
 		builders.put(new Key(compClazz, opClass), builder);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static <T extends OperationAgent> OperationAgentBuilder<T> getBuilder(Component component, Class<T> operation) {
 		// search from self class to parent class
 		Class<?> c = component.getClass();
 		while (c != null) {
 			OperationAgentBuilder<? extends OperationAgent> builder = builders.get(new Key(c, operation));
 			if (builder != null)
 				return (OperationAgentBuilder<T>) builder;
 			c = c.getSuperclass();
 		}
 		return null; // not found
 	}
 
 	/**
 	 * for operation builder mapping
 	 * 
 	 * @author pao
 	 */
 	private static class Key {
 		public Class<?> c;
 		public Class<?> t;
 
 		public <T extends OperationAgent, C extends Component> Key(Class<?> c, Class<?> t) {
 			this.c = c;
 			this.t = t;
 		}
 
 		@Override
 		public int hashCode() {
 			final int prime = 31;
 			int result = 1;
 			result = prime * result + ((c == null) ? 0 : c.hashCode());
 			result = prime * result + ((t == null) ? 0 : t.hashCode());
 			return result;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			if (obj == this)
 				return true;
 			else if (obj instanceof Key) {
 				Key o = (Key) obj;
 				return o.t == this.t && o.c == this.c;
 			}
 			return false;
 		}
 	}
 }
