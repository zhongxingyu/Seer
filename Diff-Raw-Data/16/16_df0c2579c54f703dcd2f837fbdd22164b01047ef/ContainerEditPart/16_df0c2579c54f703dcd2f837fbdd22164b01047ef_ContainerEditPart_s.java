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
 
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.wazaabi.engine.core.gef.EditPart;
 import org.eclipse.wazaabi.engine.core.views.ContainerView;
 import org.eclipse.wazaabi.mm.core.widgets.AbstractComponent;
 import org.eclipse.wazaabi.mm.core.widgets.Container;
 import org.eclipse.wazaabi.mm.core.widgets.CoreWidgetsPackage;
 
 public class ContainerEditPart extends AbstractComponentEditPart {
 
 	public static final String TITLE_VALUE_PROPERTY_NAME = "title-value";
 	public static final String TITLE_BORDER_PROPERTY_NAME = "title-border";
 
 	// TODO : layout is not a platform specific rule ?
 	public static final String LAYOUT_PROPERTY_NAME = "layout"; //$NON-NLS-1$
 
 	public EClass getModelEClass() {
 		return CoreWidgetsPackage.Literals.CONTAINER;
 	}
 
 	protected void createChildEditPartAndInsert(
 			AbstractComponent componentModel, int position) {
 		if (componentModel == null)
 			return;
 		EditPart newChild = createChild(componentModel);
 		if (newChild instanceof AbstractComponentEditPart) {
 			addChild(newChild, position);
 			((AbstractComponentEditPart) newChild).processPostUIBuilding();
 		}
 	}
 
 	public void notifyChanged(Notification notification) {
 		try {
 			switch (notification
 					.getFeatureID(org.eclipse.wazaabi.mm.core.widgets.Container.class)) {
 			case CoreWidgetsPackage.CONTAINER__CHILDREN:
 				switch (notification.getEventType()) {
 
 				case Notification.ADD:
 					if (notification.getNewValue() instanceof AbstractComponent) {
 						createChildEditPartAndInsert(
 								(AbstractComponent) notification.getNewValue(),
 								notification.getPosition());
 						getWidgetView().setValid(false);
 						getWidgetView().validate();
 					}
 					break;
 				case Notification.REMOVE: {
 					WidgetEditPart editPart = (WidgetEditPart) getViewer()
 							.getEditPartRegistry().get(
 									notification.getOldValue());
 					if (editPart instanceof AbstractComponentEditPart) {
 						removeChild(editPart);
 						getWidgetView().setValid(false);
 						getWidgetView().validate();
 					}
 				}
 					break;
 				case Notification.MOVE:
 					WidgetEditPart child = (WidgetEditPart) getViewer()
 							.getEditPartRegistry().get(
 									notification.getNewValue());
 					if (child != null) {
 						reorderChild(child, notification.getPosition());
 						child.refresh();
 						child.getWidgetView().revalidate();
 					}
 					break;
 				case Notification.REMOVE_MANY:
					for (Object model : (List<?>) notification.getNewValue()) {
 						WidgetEditPart editPart = (WidgetEditPart) getViewer()
 								.getEditPartRegistry().get(model);
 						if (editPart instanceof AbstractComponentEditPart) {
 							removeChild(editPart);
 							getWidgetView().setValid(false);
 							// TODO : validate or revalidate ??
 							getWidgetView().validate();
 						}
 					}
					throw new RuntimeException("not implemented"); //$NON-NLS-1$
 				case Notification.ADD_MANY:
 					int position = notification.getPosition();
 					boolean abstractComponentAdded = false;
 					for (Object model : (List<?>) notification.getNewValue())
 						if (model instanceof AbstractComponent) {
 							createChildEditPartAndInsert(
 									(AbstractComponent) model, position++);
 							abstractComponentAdded = true;
 						}
 					if (abstractComponentAdded) {
 						getWidgetView().setValid(false);
 						getWidgetView().validate();
 					}

 				}
 			default:
 				super.notifyChanged(notification);
 			}
 		} catch (Throwable t) {
 			t.printStackTrace();
 		}
 	}
 
 	public void refreshFeaturesAndStyles() {
 		super.refreshFeaturesAndStyles();
 		refreshUniqueStyleRule(LAYOUT_PROPERTY_NAME);
 		refreshUniqueStyleRule(LAYOUT_DATA_PROPERTY_NAME);
 		refreshUniqueStyleRule(TITLE_VALUE_PROPERTY_NAME);
 		refreshUniqueStyleRule(TITLE_BORDER_PROPERTY_NAME);
 		refreshUniqueStyleRule(TextComponentEditPart.HORIZONTAL_SCROLLBAR_PROPERTY_NAME);
 		refreshUniqueStyleRule(TextComponentEditPart.VERTICAL_SCROLLBAR_PROPERTY_NAME);
 	}
 
 	protected void removeSubtreeVisuals(AbstractComponentEditPart rootEditPart) {
 		// only containers have children
 		if (rootEditPart instanceof ContainerEditPart) {
 			for (EditPart child : rootEditPart.getChildren())
 				if (child.getParent() instanceof ContainerEditPart
 						&& child instanceof AbstractComponentEditPart)
 					((ContainerEditPart) child.getParent())
 							.removeSubtreeVisuals((AbstractComponentEditPart) child);
 		}
 		removeChildVisual(rootEditPart);
 	}
 
 	protected void addSubtreeVisuals(AbstractComponentEditPart rootEditPart,
 			int idx) {
 		addChildVisual(rootEditPart, idx);
 		if (rootEditPart instanceof ContainerEditPart) {
 			int i = 0;
 			for (EditPart child : ((ContainerEditPart) rootEditPart)
 					.getChildren())
 				if (child.getParent() instanceof ContainerEditPart
 						&& child instanceof AbstractComponentEditPart)
 					((ContainerEditPart) child.getParent()).addSubtreeVisuals(
 							(AbstractComponentEditPart) child, i++);
 		}
 	}
 
 	protected void replaceChildVisual() {
 		int idx = getParent().getChildren().indexOf(this);
 		if (getParent() instanceof ContainerEditPart) {
 			deactivate();
 			((ContainerEditPart) getParent()).removeSubtreeVisuals(this);
 			((ContainerEditPart) getParent()).addSubtreeVisuals(this, idx);
 			activate();
 		}
 	}
 
 	protected void refreshDescendantsFeaturesAndStyles() {
 		for (EditPart child : getChildren()) {
 			if (child instanceof ContainerEditPart) {
 				((ContainerEditPart) child)
 						.refreshDescendantsFeaturesAndStyles();
 				((AbstractComponentEditPart) child).refreshFeaturesAndStyles();
 			} else if (child instanceof AbstractComponentEditPart)
 				((AbstractComponentEditPart) child).refreshFeaturesAndStyles();
 		}
 	}
 
 	protected List<AbstractComponent> getModelChildren() {
 		return ((Container) getModel()).getChildren();
 	}
 
 	protected void reCreateWidgetView() {
 		replaceChildVisual();
 		refreshDescendantsFeaturesAndStyles();
 		refreshFeaturesAndStyles();
 		getWidgetView().fireWidgetViewRepainted();
 	}
 
 	/**
 	 * The ContainerView into which childrens' WidgetViews will be added. May
 	 * return the same WidgetView as {@link #getWidgetView()}.
 	 * 
 	 * @return the <i>content pane</i> WidgetContainer
 	 */
 	public ContainerView getContentPane() {
 		return (ContainerView) getWidgetView();
 	}
 
 	@Override
 	public void processPostUIBuilding() {
 		((ContainerView) getWidgetView()).refreshTabIndexes();
 		super.processPostUIBuilding();
 		for (EditPart child : getChildren())
 			if (child instanceof AbstractWidgetEditPart)
 				((AbstractWidgetEditPart) child).processPostUIBuilding();
 	}
 
 }
