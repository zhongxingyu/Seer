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
 
 package org.eclipse.wazaabi.engine.core.editparts;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.wazaabi.engine.core.CoreSingletons;
 import org.eclipse.wazaabi.engine.core.CoreUtils;
 import org.eclipse.wazaabi.engine.core.annotations.managers.AnnotationManager;
 import org.eclipse.wazaabi.engine.core.editparts.stylerules.StylePropertyDescriptor;
 import org.eclipse.wazaabi.engine.core.editparts.stylerules.StyleRulesHelper;
 import org.eclipse.wazaabi.engine.core.gef.EditPart;
 import org.eclipse.wazaabi.engine.core.gef.EditPartViewer;
 import org.eclipse.wazaabi.engine.core.gef.editparts.AbstractEditPart;
 import org.eclipse.wazaabi.engine.core.views.WidgetView;
 import org.eclipse.wazaabi.engine.core.views.factories.WidgetViewFactory;
 import org.eclipse.wazaabi.engine.edp.EDPUtils;
 import org.eclipse.wazaabi.engine.edp.adapters.EventDispatcherAdapter;
 import org.eclipse.wazaabi.engine.edp.adapters.EventDispatcherAdapterImpl;
 import org.eclipse.wazaabi.engine.edp.locationpaths.IPointersEvaluator;
 import org.eclipse.wazaabi.mm.core.styles.CoreStylesPackage;
 import org.eclipse.wazaabi.mm.core.styles.StyleRule;
 import org.eclipse.wazaabi.mm.core.styles.StyledElement;
 import org.eclipse.wazaabi.mm.core.styles.impl.BlankRuleImpl;
 import org.eclipse.wazaabi.mm.core.widgets.CoreWidgetsPackage;
 import org.eclipse.wazaabi.mm.core.widgets.Widget;
 import org.eclipse.wazaabi.mm.edp.EventDispatcher;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 
 public abstract class AbstractWidgetEditPart extends AbstractEditPart implements
 		WidgetEditPart {
 
 	public static class StyleRuleManager extends AdapterImpl {
 
 		// TODO : need to be simplified urgently
 		private AbstractWidgetEditPart host = null;
 
 		@Override
 		public void notifyChanged(Notification notification) {
 			assert getHost() != null;
 			if (notification.getEventType() != Notification.SET)
 				return;
 			WidgetView widgetView = getHost().getWidgetView();
 			switch (notification.getFeatureID(StyleRule.class)) {
 			case CoreStylesPackage.STYLE_RULE__PROPERTY_NAME:
 				String oldPropertyName = notification.getOldStringValue();
 				String newPropertyName = notification.getNewStringValue();
 				StyleRule previousStyleRule = null;
 				boolean styleRuleRemoved = false;
 				boolean styleRuleAdded = false;
 				if (oldPropertyName != null && !"".equals(oldPropertyName)) //$NON-NLS-1$
 					if (!oldPropertyName.equals(newPropertyName)) {
 						// we duplicate the previous styleRule and set the
 						// propertyName
 						previousStyleRule = (StyleRule) EcoreUtil
 								.copy((EObject) notification.getNotifier());
 						previousStyleRule.setPropertyName(oldPropertyName);
 						getHost().styleRuleRemoved(previousStyleRule);
 						styleRuleRemoved = true;
 					}
 				if (newPropertyName != null && !"".equals(newPropertyName)) {//$NON-NLS-1$
 					getHost().styleRuleAdded(
 							(StyleRule) notification.getNotifier());
 					styleRuleAdded = true;
 				}
 				if (styleRuleAdded || styleRuleRemoved) {
 					if ((widgetView.needReCreateWidgetView(previousStyleRule) || widgetView
 							.needReCreateWidgetView((StyleRule) notification
 									.getNotifier())))
 						getHost().reCreateWidgetView();
 					else {
 						if (styleRuleRemoved)
 							widgetView
 									.updateStyleRule(createBlankStyleRule(oldPropertyName));
 
 						if (styleRuleAdded) {
 							widgetView.updateStyleRule((StyleRule) notification
 									.getNotifier());
 						}
 					}
 				}
 				break;
 			}
 		}
 
 		protected AbstractWidgetEditPart getHost() {
 			return host;
 		}
 
 		protected void setHost(AbstractWidgetEditPart host) {
 			this.host = host;
 		}
 
 		/**
 		 * Calls the reCreateWidgetView (when used from outside this class)
 		 */
 		protected void reCreateWidgetView() {
 			assert getHost() != null;
 			getHost().reCreateWidgetView();
 		}
 
 		/**
 		 * Calls the refreshFeaturesAndStyles (when used from outside this
 		 * class)
 		 */
 		protected void refreshFeaturesAndStyles() {
 			assert getHost() != null;
 			getHost().refreshFeaturesAndStyles();
 		}
 	}
 
 	// protected void forceUIRefreshEvent() {
 	// // for (EventHandler eventHandler : (List<EventHandler>)
 	// // ((EventDispatcher) getModel())
 	// // .getEventHandlers())
 	// // if (REFRESH_EVENT_NAME.equals(eventHandler.getEvent()))
 	// // for (Adapter adapter : eventHandler.eAdapters())
 	// // if (adapter instanceof AbstractEventHandlerAdapter)
 	// // ((AbstractEventHandlerAdapter) adapter)
 	// // .trigger(REFRESH_EVENT_NAME);
 	// }
 
 	private static HashMap<String, StylePropertyDescriptor> coreStylePropertyDescriptors = null;;
 
 	protected static HashMap<String, StylePropertyDescriptor> getCoreStylePropertyDescriptors() {
 		return coreStylePropertyDescriptors;
 	}
 
 	public AbstractWidgetEditPart() {
 		if (coreStylePropertyDescriptors == null) {
 			coreStylePropertyDescriptors = new HashMap<String, StylePropertyDescriptor>();
 			StyleRulesHelper.buildCoreStylePropertyDescriptors(
 					getModelEClass(), coreStylePropertyDescriptors);
 		}
 	}
 
 	public abstract EClass getModelEClass();
 
 	protected void hookModel() {
 		assert getModel() instanceof org.eclipse.wazaabi.mm.core.widgets.Widget;
 		((org.eclipse.wazaabi.mm.core.widgets.Widget) getModel()).eAdapters()
 				.add(this);
 		for (StyleRule styleRule : ((StyledElement) getModel()).getStyleRules())
 			hookStyleRule(styleRule);
 
 	}
 
 	/**
 	 * This method is called after a list of new style rules has been added to
 	 * this widget's model. Using this method avoid too many widget creations
 	 * when only one is enough.
 	 * 
 	 * @param styleRules
 	 *            the list style rule which is attached
 	 */
 
 	protected void reCreateWidgetView() {
 		replaceChildVisual();
 		refreshFeaturesAndStyles();
 		getWidgetView().fireWidgetViewRepainted();
 	}
 
 	@SuppressWarnings("unchecked")
 	public void notifyChanged(Notification notification) {
 		switch (notification
 				.getFeatureID(org.eclipse.wazaabi.mm.core.widgets.Widget.class)) {
 		case CoreWidgetsPackage.WIDGET__STYLE_RULES:
 			switch (notification.getEventType()) {
 			case Notification.ADD:
 				hookStyleRule((StyleRule) notification.getNewValue());
 				if (styleRuleAdded((StyleRule) notification.getNewValue()))
 					reCreateWidgetView();
 				break;
 			case Notification.ADD_MANY:
 				boolean needToRecreateView = false;
 				for (StyleRule rule : (List<StyleRule>) notification
 						.getNewValue()) {
 					needToRecreateView |= styleRuleAdded(rule);
 					hookStyleRule(rule);
 				}
 				if (needToRecreateView)
 					reCreateWidgetView();
 				break;
 			case Notification.REMOVE:
 				unhookStyleRule((StyleRule) notification.getOldValue());
 				if (styleRuleRemoved((StyleRule) notification.getOldValue()))
 					reCreateWidgetView();
 				break;
 			case Notification.REMOVE_MANY:
 				needToRecreateView = false;
 				for (StyleRule rule : (List<StyleRule>) notification
 						.getOldValue()) {
 					needToRecreateView |= styleRuleRemoved(rule);
 					unhookStyleRule(rule);
 				}
 				if (needToRecreateView)
 					reCreateWidgetView();
 				break;
 			case Notification.MOVE:
 				StyleRule newStyle = (StyleRule) notification.getNewValue();
 				StyleRule firstRule = ((StyledElement) getModel())
 						.getFirstStyleRule(newStyle.getPropertyName(), null);
 				if (firstRule.equals(newStyle))
 					reCreateWidgetView();
 				break;
 			}
 			break;
 		default:
 			getInnerEventDispatcherAdapter().notifyChanged(notification);
 		}
 	}
 
 	/**
 	 * Replaces the existing widget view by deleting it and recreating it at the
 	 * same index in its container. Some platform like SWT simply remove and
 	 * re-create the underlying widget, others platforms like swing do not need
 	 * to remove the widget.
 	 */
 	protected void replaceChildVisual() {
 		int idx = getParent().getChildren().indexOf(this);
 		if (getParent() instanceof ContainerEditPart) {
 			((ContainerEditPart) getParent()).removeChildVisual(this);
 			((ContainerEditPart) getParent()).addChildVisual(this, idx);
<<<<<<< HEAD
			activate();
=======
 			deactivate();
>>>>>>> 9123a980bdae40de1de18a174e66ab786aea7346
 		}
 	}
 
 	abstract protected void refreshFeaturesAndStyles();
 
 	protected void refreshUniqueStyleRule(String propertyName) {
 		if (propertyName == null || "".equals(propertyName)) //$NON-NLS-1$
 			return;
 		final WidgetView view = getWidgetView();
 		StyleRule rule = ((StyledElement) getModel()).getFirstStyleRule(
 				propertyName, null);
 		if (rule == null)
 			rule = getCoreDefaultStyleRule(propertyName);
 		if (view.needReCreateWidgetView(rule))
 			return;
 
 		view.updateStyleRule(rule);
 	}
 
 	protected void refreshStyleRules(String propertyName) {
 		if (propertyName == null || "".equals(propertyName)) //$NON-NLS-1$
 			return;
 		final WidgetView view = getWidgetView();
 		List<StyleRule> rules = new ArrayList<StyleRule>();
 		for (StyleRule rule : ((StyledElement) getModel()).getStyleRules())
 			if (propertyName.equals(rule.getPropertyName()))
 				rules.add(rule);
 
 		if (!rules.isEmpty() && !view.needReCreateWidgetView(rules))
 			view.updateSameStyleRules(rules);
 	}
 
 	@Override
 	protected void refreshVisuals() {
 		boolean needReplaceChildVisual = false;
 		WidgetView view = getWidgetView();
 		for (StyleRule styleRule : ((StyledElement) getModel()).getStyleRules())
 			if (view.needReCreateWidgetView(styleRule)) {
 				needReplaceChildVisual = true;
 				break;
 			}
 
 		if (needReplaceChildVisual)
 			replaceChildVisual();
 
 		refreshFeaturesAndStyles();
 		getWidgetView().fireWidgetViewRepainted();
 	}
 
 	/**
 	 * Notifies the WidgetView that a style rule has been added. If the
 	 * WidgetView needs to be recreated after the removal, the method returns
 	 * true.
 	 * 
 	 * @param oldRule
 	 *            The style rule to be added
 	 * @return true if the WidgetView needs to be recreated.
 	 */
 	public boolean styleRuleAdded(StyleRule newRule) {
 		final String propertyName = newRule.getPropertyName();
 		if (propertyName == null || "".equals(propertyName)) //$NON-NLS-1$
 			return false;
 		if (getWidgetView().needReCreateWidgetView(newRule))
 			return true;
 		else {
 			if (((StyledElement) getModel()).getFirstStyleRule(propertyName,
 					null) == newRule) {
 				getWidgetView().updateStyleRule(newRule);
 				// TODO
 				// this is not a good performance solution : we should not force
 				// the recreate
 				// we should force
 				// AbstractSWTViewer.getUpdateManager().performUpdate()
 				// if
 				// (newRule.getPropertyName().equals(ContainerEditPart.LAYOUT_PROPERTY_NAME))
 				// {
 				// return true;
 				// }
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Notifies the WidgetView that a style rule has been removed. Updates the
 	 * WidgetView with the default value associated to this style rule or with
 	 * the previous rule if no default value has been associated. If the
 	 * WidgetView needs to be recreated after the removal, the method returns
 	 * true.
 	 * 
 	 * @param oldRule
 	 *            The style rule to be removed
 	 * @return true if the WidgetView needs to be recreated.
 	 */
 	public boolean styleRuleRemoved(StyleRule oldRule) {
 		final String propertyName = oldRule.getPropertyName();
 		if (propertyName == null || "".equals(propertyName)) //$NON-NLS-1$
 			return false;
 
 		StyleRule rule = ((StyledElement) getModel()).getFirstStyleRule(
 				propertyName, null);
 
 		if (rule == null) {
 			final StyleRule defaultStyleRule = getDefaultStyleRule(propertyName);
 			if (getWidgetView().needReCreateWidgetView(defaultStyleRule))
 				return true;
 			if (defaultStyleRule != null) {
 				getWidgetView().updateStyleRule(defaultStyleRule);
 			} else
 				getWidgetView().updateStyleRule(
 						createBlankStyleRule(propertyName));
 		} else
 			getWidgetView().updateStyleRule(rule);
 		return false;
 	}
 
 	protected static StyleRule createBlankStyleRule(final String ruleName) {
 		return new BlankRuleImpl() {
 			@Override
 			public String getPropertyName() {
 				return ruleName;
 			}
 		};
 	}
 
 	/**
 	 * Notifies the WidgetView that a style rule has been updated. If the
 	 * WidgetView needs to be recreated after the removal, the method returns
 	 * true.
 	 * 
 	 * @param oldRule
 	 *            The style rule to be update
 	 * @return true if the WidgetView needs to be recreated.
 	 */
 	public boolean styleRuleUpdated(StyleRule rule) {
 		final String propertyName = rule.getPropertyName();
 		if (propertyName == null || "".equals(propertyName)) //$NON-NLS-1$
 			return false;
 		if (getWidgetView().needReCreateWidgetView(rule))
 			return true;
 		else
 			getWidgetView().updateStyleRule(rule);
 		return false;
 	}
 
 	protected void unhookModel() {
 		for (StyleRule styleRule : ((StyledElement) getModel()).getStyleRules())
 			unhookStyleRule(styleRule);
 
 		((org.eclipse.wazaabi.mm.core.widgets.Widget) getModel()).eAdapters()
 				.remove(this);
 	}
 
 	/**
 	 * Returns the default style rule for a given propertyName by trying to find
 	 * a such definition in the platform specific property descriptors in a
 	 * first time and if not found, in the core property descriptors. Returns
 	 * null if no styleRule has been defined for this property name
 	 * 
 	 * @param propertyName
 	 *            A non null, non empty string corresponding to the property
 	 *            Name of the default style rule to resolve
 	 * @return A Style rule if found, null otherwise
 	 */
 	protected StyleRule getDefaultStyleRule(String propertyName) {
 		StyleRule result = getPlatformSpecificDefaultStyleRule(propertyName);
 		if (result == null)
 			return getCoreDefaultStyleRule(propertyName);
 		return result;
 	}
 
 	/**
 	 * Detaches/removes all the StyleRuleManagers attached to the given style
 	 * rule. Actually, only one StyleRuleManager is supposed to be attached to a
 	 * StyleRule, but we prefer being sure.
 	 * 
 	 * @param styleRule
 	 */
 	protected void unhookStyleRule(StyleRule styleRule) {
 		List<Adapter> toRemove = new ArrayList<Adapter>(2);
 		for (Adapter adapter : styleRule.eAdapters())
 			if (adapter instanceof StyleRuleManager)
 				toRemove.add(adapter);
 		for (Adapter adapter : toRemove)
 			styleRule.eAdapters().remove(adapter);
 	}
 
 	/**
 	 * Finds the StyleRuleManager corresponding to the given StyleRule and if
 	 * found, attaches it and sets initialization parameters.
 	 * 
 	 * @param styleRule
 	 */
 	protected void hookStyleRule(StyleRule styleRule) {
 		StyleRuleManager manager = CoreSingletons
 				.getComposedStyleRuleManagerFactory().createStyleRuleManager(
 						styleRule);
 		if (manager != null) {
 			manager.setHost(this);
 			// create a style rule adapter and attach it to the style rule model
 			styleRule.eAdapters().add(manager);
 		}
 	}
 
 	/**
 	 * Returns a default style rule by iterating over the core style property
 	 * descriptors. Returns null if not default style rule has been found. Core
 	 * descriptors are defined in the Core metamodel.
 	 * 
 	 * @param propertyName
 	 *            A non null, non empty string corresponding to the property
 	 *            Name of the default style rule to resolve
 	 * @return A Style rule if found, null otherwise
 	 */
 	protected StyleRule getCoreDefaultStyleRule(String propertyName) {
 		StyleRule result = null;
 		StylePropertyDescriptor descriptor = getCoreStylePropertyDescriptors()
 				.get(propertyName);
 		if (descriptor != null)
 			result = descriptor.getDefaultStyleRule();
 		return result;
 	}
 
 	/**
 	 * Returns a default style rule by iterating over the platform specific
 	 * style property descriptors. Returns null if not default style rule has
 	 * been found. Platform specific descriptors are defined in the platform
 	 * specific metamodel.
 	 * 
 	 * @param propertyName
 	 *            A non null, non empty string corresponding to the property
 	 *            Name of the default style rule to resolve
 	 * @return A Style rule if found, null otherwise
 	 */
 	protected StyleRule getPlatformSpecificDefaultStyleRule(String propertyName) {
 		StyleRule result = null;
 		assert getWidgetView() != null;
 		StylePropertyDescriptor descriptor = getWidgetView()
 				.getPlatformSpecificStylePropertyDescriptors()
 				.get(propertyName);
 		if (descriptor != null)
 			result = descriptor.getDefaultStyleRule();
 
 		return result;
 	}
 
 	// /////////////////////////// END COPIED FROM ABSTRACTCOMPONENTEDITPART
 	// ///////////////////////////////
 
 	private WidgetView widgetView = null;
 
 	// TODO : not sure that 'public' is appropriate
 	public class InnerEventDispatcherAdapter extends EventDispatcherAdapterImpl {
 
 		@Override
 		protected void eventHandlerAdded(EventHandler eventHandler) {
 			// getWidgetView().addEventHandler(eventHandler);
 		}
 
 		@Override
 		protected void eventHandlerRemoved(EventHandler eventHandler) {
 			// getWidgetView().removeEventHandler(eventHandler);
 		}
 
 		@Override
 		public EventDispatcherAdapter getEventDispatcherAdapter() {
 			return AbstractWidgetEditPart.this;
 		}
 
 		public IPointersEvaluator getPointersEvaluator() {
 			return AbstractWidgetEditPart.this.getPointersEvaluator();
 		}
 
 		public String getCodeLocatorBaseUri() {
 			if (getViewer() != null)
 				return getViewer().getCodeLocatorBaseUri();
 			return null;
 		}
 
 	};
 
 	private InnerEventDispatcherAdapter innerEventDispatcherAdapter = new InnerEventDispatcherAdapter();
 
 	protected EventDispatcherAdapterImpl getInnerEventDispatcherAdapter() {
 		return innerEventDispatcherAdapter;
 	}
 
 	protected void addChildVisual(EditPart childEditPart, int index) {
 		if (childEditPart instanceof AbstractWidgetEditPart) {
 			// first of all, we create the WidgetView corresponding to this
 			// EditPart (and using the WidgetViewFactory)
 			WidgetView newChild = ((AbstractWidgetEditPart) childEditPart)
 					.createWidgetView();
 			if (newChild != null) {
 				// we attach this newly created WidgetView to the child
 				// EditPart.
 				((AbstractWidgetEditPart) childEditPart)
 						.setWidgetView(newChild);
 
 				// we add the newly created WidgetView to the list of children.
 				// Under SWT platform, the SWT widget is created here.
 				getWidgetView().add(newChild, index);
 				newChild.addNotify();
 
 				// Now the WidgetView and the EditPart are consistent, then we
 				// can start to listen any WidgetView event.
 				((AbstractWidgetEditPart) childEditPart)
 						.hookWidgetView(newChild);
 			} else
 				throw new RuntimeException(); // TODO put a message here and log
 												// it, means we did not find any
 												// WidgetView for this EditPart
 		} else
 			throw new RuntimeException(); // TODO : do we need to manage this
 											// case ???
 	}
 
 	public WidgetView createWidgetView() {
 		WidgetViewFactory widgetViewFactory = getViewer()
 				.getWidgetViewFactory();
 
 		if (widgetViewFactory == null)
 			throw new RuntimeException("WidgetViewFactory cannot be null"); //$NON-NLS-1$
 		return widgetViewFactory.createWidgetView(this, null);
 	}
 
 	public WidgetView getWidgetView() {
 		return widgetView;
 	}
 
 	/**
 	 * Attaches Adapters wherever required in model parts.
 	 */
 	// protected void hookModel() {
 	//
 	// assert getModel() instanceof org.eclipse.wazaabi.mm.core.widgets.Widget;
 	// ((org.eclipse.wazaabi.mm.core.widgets.Widget) getModel()).eAdapters()
 	// .add(this);
 	// }
 
 	protected void hookWidgetView(WidgetView widgetView) {
 	}
 
 	protected void removeChildVisual(EditPart childEditPart) {
 		if (childEditPart instanceof WidgetEditPart) {
 			WidgetView childView = ((WidgetEditPart) childEditPart)
 					.getWidgetView();
 			((AbstractWidgetEditPart) childEditPart)
 					.unhookWidgetView(childView);
 			childView.removeNotify();
 			getWidgetView().remove(childView);
 			((AbstractWidgetEditPart) childEditPart).setWidgetView(null);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.wazaabi.engine.core.editparts.WidgetEditPart#renewVisuals ()
 	 */
 	public void renewVisuals() {
 		throw new RuntimeException(); // TODO check if we need to implement this
 		// one
 	}
 
 	public void setWidgetView(WidgetView widgetView) {
 		if (getWidgetView() != null)
 			getWidgetView().setHost(null);
 		this.widgetView = widgetView;
 		if (getWidgetView() != null)
 			getWidgetView().setHost(this);
 	}
 
 	protected EventDispatcher getModelBindingContext() {
 		return (EventDispatcher) getModel();
 	}
 
 	// protected void unhookModel() {
 	// // FIXME
 	//
 	// // getWidgetEditPartBindingContextAdapter().unhookBindingContext(
 	// // getModelBindingContext());
 	// ((org.eclipse.wazaabi.mm.core.widgets.Widget) getModel()).eAdapters()
 	// .remove(this);
 	// }
 
 	protected void unhookWidgetView(WidgetView widgetView) {
 	}
 
 	@SuppressWarnings("unchecked")
 	protected void registerVisuals() {
 		getViewer().getVisualPartMap().put(getWidgetView(), this);
 	}
 
 	protected void unregisterVisuals() {
 		getViewer().getVisualPartMap().remove(getWidgetView());
 	}
 
 	private Notifier target = null;
 
 	public Notifier getTarget() {
 		return target;
 	}
 
 	public void setTarget(Notifier newTarget) {
 		this.target = newTarget;
 		getInnerEventDispatcherAdapter().setTarget(newTarget);
 	}
 
 	public boolean isAdapterForType(Object type) {
 		return getInnerEventDispatcherAdapter().isAdapterForType(type);
 	}
 
 	public void activate() {
 		if (!isActive()) {
 			hookModel();
 			super.activate();
 		}
 	}
 
 	public void deactivate() {
 		if (isActive()) {
 			super.deactivate();
 			unhookModel();
 		}
 	}
 
 	protected void throwCoreUIRefreshEvent() {
 		EDPUtils.throwEvent((EventDispatcher) getModel(),
 				CoreUtils.CORE_UI_REFRESH_EVENT);
 	}
 
 	protected void forceUIRefreshEvent() {
 		// widgets do not receive refresh events
 	}
 
 	public void forceRefreshEvent() {
 		forceUIRefreshEvent();
 		throwCoreUIRefreshEvent();
 	}
 
 	public IPointersEvaluator getPointersEvaluator() {
 		return getViewer().getPointersEvaluator();
 	}
 
 	/**
 	 * This method is called AFTER all the UI components have been inserted into
 	 * the viewer (which means after the whole UI has been built). This method
 	 * is called either after the viewer's setContents either after an subtree
 	 * addition while the viewer is rendering the UI. This method is called only
 	 * once during the lifecycle of the component.
 	 * 
 	 * @see EditPartViewer#setContents(Object)
 	 * 
 	 */
 	public void processPostUIBuilding() {
 		// first, we process annotations associated to this widget
 		for (AnnotationManager annotationManager : CoreSingletons
 				.getComposedAnnotationManagerFactory()
 				.getRelevantAnnotationManagers((Widget) this.getModel())) {
 			annotationManager.processAnnotation(this);
 		}
 
 		// TODO : merge EDPEvent and ui refreshEvent
 		throwCoreUIRefreshEvent();
 		// getWidgetView().processPostControlCreation();
 	}
 
 	public void lock(String id) {
 		getInnerEventDispatcherAdapter().lock(id);
 	}
 
 	public void unlock(String id) {
 		getInnerEventDispatcherAdapter().unlock(id);
 	}
 
 	public boolean isLocked(String id) {
 		return getInnerEventDispatcherAdapter().isLocked(id);
 	}
 
 	public String getCodeLocatorBaseUri() {
 		return innerEventDispatcherAdapter.getCodeLocatorBaseUri();
 	}
 }
