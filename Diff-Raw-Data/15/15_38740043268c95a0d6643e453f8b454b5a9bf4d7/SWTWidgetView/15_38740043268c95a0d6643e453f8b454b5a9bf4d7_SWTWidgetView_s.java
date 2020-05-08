 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.views;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.wazaabi.engine.core.editparts.AbstractComponentEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.WidgetEditPart;
 import org.eclipse.wazaabi.engine.core.editparts.WidgetViewListener;
 import org.eclipse.wazaabi.engine.core.editparts.stylerules.StylePropertyDescriptor;
 import org.eclipse.wazaabi.engine.core.editparts.stylerules.StyleRulesHelper;
 import org.eclipse.wazaabi.engine.core.gef.editparts.ListenerList;
 import org.eclipse.wazaabi.engine.core.views.WidgetView;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 import org.eclipse.wazaabi.mm.core.styles.StyledElement;
<<<<<<< HEAD
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
=======
>>>>>>> 9123a980bdae40de1de18a174e66ab786aea7346
 
 public abstract class SWTWidgetView implements WidgetView {
 
 	private static HashMap<String, StylePropertyDescriptor> platformSpecificStylePropertyDescriptors = null;
 	private final Logger logger = LoggerFactory.getLogger(SWTWidgetView.class);
 
 	public static final String WAZAABI_HOST_KEY = "org.eclipse.wazaabi.engine.swt.DATA_KEY";
 
 	/**
 	 * Returns a style with exactly one style bit set out of the specified set
 	 * of exclusive style bits. All other possible bits are cleared when the
 	 * first matching bit is found. Bits that are not part of the possible set
 	 * are untouched.
 	 * 
 	 * @see org.eclipse.swt.widgets.Widget#checkBits
 	 * 
 	 * @param style
 	 *            the original style bits
 	 * @param int0
 	 *            the 0th possible style bit
 	 * @param int1
 	 *            the 1st possible style bit
 	 * @param int2
 	 *            the 2nd possible style bit
 	 * @param int3
 	 *            the 3rd possible style bit
 	 * @param int4
 	 *            the 4th possible style bit
 	 * @param int5
 	 *            the 5th possible style bit
 	 * 
 	 * @return the new style bits
 	 */
 	static int checkBits(int style, int int0, int int1, int int2, int int3,
 			int int4, int int5) {
 		int mask = int0 | int1 | int2 | int3 | int4 | int5;
 		if ((style & mask) == 0)
 			style |= int0;
 		if ((style & int0) != 0)
 			style = (style & ~mask) | int0;
 		if ((style & int1) != 0)
 			style = (style & ~mask) | int1;
 		if ((style & int2) != 0)
 			style = (style & ~mask) | int2;
 		if ((style & int3) != 0)
 			style = (style & ~mask) | int3;
 		if ((style & int4) != 0)
 			style = (style & ~mask) | int4;
 		if ((style & int5) != 0)
 			style = (style & ~mask) | int5;
 		return style;
 	}
 
 	protected static boolean isStyleBitCorrectlySet(
 			org.eclipse.swt.widgets.Widget widget, int styleBitMask,
 			boolean newStyleBitValue) {
 		int styleValue = widget.getStyle();
 		if (newStyleBitValue && (styleValue & styleBitMask) == 0) {
 			styleValue |= styleBitMask;
 		} else if (!newStyleBitValue && (styleValue & styleBitMask) != 0) {
 			styleValue ^= styleBitMask;
 		}
 		return styleValue == widget.getStyle();
 	}
 
 	private final org.eclipse.swt.events.DisposeListener disposeListener = new org.eclipse.swt.events.DisposeListener() {
 
 		public void widgetDisposed(DisposeEvent e) {
 			logger.info("\"{}\" disposed", getSWTWidget());
 //			System.out.println("widgetDisposed  "
 //					+ SWTWidgetView.this
 //							.getClass()
 //							.getName()
 //							.substring(
 //									SWTWidgetView.this.getClass().getName()
 //											.lastIndexOf(".") + 1)
 //					+ ", swt widget=" + System.identityHashCode(getSWTWidget())
 //					+ ", WidgetView="
 //					+ System.identityHashCode(SWTWidgetView.this));
 			getHost().deactivate();
 			SWTWidgetView.this.widgetDisposed();
 		}
 	};
 
 	private WidgetEditPart host = null;
 	private ListenerList listenerList;
 
 	private org.eclipse.swt.widgets.Widget widget = null;
 
 	public SWTWidgetView() {
 		initPlatformPropertyDescriptors();
 	}
 
 	public void add(WidgetView childView, int index) {
 		if (!(childView instanceof SWTWidgetView))
 			throw new RuntimeException("Invalid parent WidgetView"); //$NON-NLS-1$
 		org.eclipse.swt.widgets.Widget newWidget = ((SWTWidgetView) childView)
 				.createSWTWidget(getSWTWidget(), 0, index);
 		if (newWidget == null || newWidget.isDisposed())
 			throw new RuntimeException("Unable to create SWT widget"); //$NON-NLS-1$
 
 		((SWTWidgetView) childView).widget = newWidget;
 		newWidget
 				.addDisposeListener(((SWTWidgetView) childView).disposeListener);
 	}
 
 	public void addWidgetViewListener(WidgetViewListener listener) {
 		if (listenerList == null)
 			listenerList = new ListenerList();
 		listenerList.add(listener);
 	}
 
 	protected int computeSWTCreationStyle(StyleRule rule) {
 		return SWT.NONE;
 	}
 
 	protected int computeSWTCreationStyle(WidgetEditPart editPart) {
 		int style = SWT.None;
 		ArrayList<String> processedStyles = new ArrayList<String>();
 		for (StyleRule rule : ((StyledElement) getHost().getModel())
 				.getStyleRules())
 			if (!processedStyles.contains(rule.getPropertyName())) {
 				processedStyles.add(rule.getPropertyName());
 				style |= computeSWTCreationStyle(rule);
 			}
 		return style;
 	}
 
 	protected abstract org.eclipse.swt.widgets.Widget createSWTWidget(
 			org.eclipse.swt.widgets.Widget parent, int swtStyle, int index);
 
 	public void fireWidgetViewRepainted() {
 		if (listenerList != null) {
 			final Object[] listeners = listenerList.getListeners();
 			for (int i = 0; i < listeners.length; i++)
 				((WidgetViewListener) listeners[i]).viewChanged(this,
 						WidgetViewListener.VIEW_REPAINTED);
 		}
 	}
 
 	public void fireWidgetViewValidated() {
 		if (listenerList != null) {
 			final Object[] listeners = listenerList.getListeners();
 			for (int i = 0; i < listeners.length; i++)
 				((WidgetViewListener) listeners[i]).viewChanged(this,
 						WidgetViewListener.VIEW_VALIDATED);
 		}
 	}
 
 	public WidgetEditPart getHost() {
 		return host;
 	}
 
 	public SWTWidgetView getParent() {
 		if (getHost().getParent() instanceof AbstractComponentEditPart)
 			return (SWTWidgetView) ((AbstractComponentEditPart) getHost()
 					.getParent()).getWidgetView();
 		return null;
 	}
 
 	public HashMap<String, StylePropertyDescriptor> getPlatformSpecificStylePropertyDescriptors() {
 		return platformSpecificStylePropertyDescriptors;
 	}
 
 	public org.eclipse.swt.widgets.Widget getSWTWidget() {
 		// if (widget instanceof org.eclipse.swt.widgets.ToolItem) {
 		// return ((org.eclipse.swt.widgets.ToolItem) widget).getControl();
 		// } else if (widget instanceof org.eclipse.swt.widgets.CoolItem) {
 		// return ((org.eclipse.swt.widgets.CoolItem) widget).getControl();
 		// }
 		return widget;
 	}
 
 	public abstract EClass getWidgetViewEClass();
 
 	protected void initPlatformPropertyDescriptors() {
 		if (platformSpecificStylePropertyDescriptors == null) {
 			platformSpecificStylePropertyDescriptors = new HashMap<String, StylePropertyDescriptor>();
 			StyleRulesHelper.buildPlatformSpecificStylePropertyDescritors(
 					getWidgetViewEClass(),
 					platformSpecificStylePropertyDescriptors);
 		}
 
 	}
 
 	public boolean needReCreateWidgetView(List<StyleRule> styleRules) {
 		for (StyleRule styleRule : styleRules)
 			if (needReCreateWidgetView(styleRule))
 				return true;
 		return false;
 	}
 
 	public boolean needReCreateWidgetView(StyleRule styleRule) {
 		return false;
 	}
 
 	public void remove(WidgetView view) {
 		if (view instanceof SWTWidgetView
 				&& ((SWTWidgetView) view).getSWTWidget() != null
 				&& !((SWTWidgetView) view).getSWTWidget().isDisposed())
 			((SWTWidgetView) view).getSWTWidget().dispose();
 	}
 
 	public void removeWidgetViewListener(WidgetViewListener listener) {
 		if (listenerList == null)
 			return;
 		listenerList.remove(listener);
 	}
 
 	public void setHost(WidgetEditPart host) {
 		this.host = host;
 	}
 
 	public void updateStyleRule(StyleRule rule) {
 
 	}
 
 	public void updateSameStyleRules(List<StyleRule> rules) {
 
 	}
 
 	protected void widgetDisposed() {
 		if (listenerList != null) {
 			final Object[] listeners = listenerList.getListeners();
 			for (int i = 0; i < listeners.length; i++)
 				((WidgetViewListener) listeners[i]).viewChanged(this,
 						WidgetViewListener.VIEW_DISPOSED);
 		}
 	}
 
 }
