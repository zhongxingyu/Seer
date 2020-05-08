 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.controllers;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.databinding.observable.Realm;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.navigation.ui.filter.NavigationUIFilterAttributeDisabledMarker;
 import org.eclipse.riena.internal.navigation.ui.filter.NavigationUIFilterAttributeHiddenMarker;
 import org.eclipse.riena.internal.ui.ridgets.swt.LabelRidget;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.filter.IUIFilter;
 import org.eclipse.riena.ui.filter.IUIFilterAttribute;
 import org.eclipse.riena.ui.filter.impl.UIFilter;
 import org.eclipse.riena.ui.ridgets.filter.RidgetUIFilterAttributeHiddenMarker;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Tests of the class {@link NavigationUIFilterApplier}.
  */
 public class NavigationUIFilterApplierTest extends TestCase {
 
 	@Override
 	protected void setUp() throws Exception {
 		Display display = Display.getDefault();
 		Realm realm = SWTObservables.getRealm(display);
 		assertNotNull(realm);
 		ReflectionUtils.invokeHidden(realm, "setDefault", realm);
 	}
 
 	/**
 	 * Tests the method {@code collectFilters}.
 	 */
 	public void testCollectFilters() {
 
 		NavigationUIFilterApplier applier = new NavigationUIFilterApplier();
 
 		Collection<IUIFilter> filters = new ArrayList<IUIFilter>();
 		NavigationNodeId id = new NavigationNodeId("4711");
 		SubModuleNode node = new SubModuleNode(id);
 
 		ReflectionUtils.invokeHidden(applier, "collectFilters", node, filters);
 		assertTrue(filters.isEmpty());
 
 		UIFilter filter = new UIFilter();
 		node.addFilter(filter);
 
 		ReflectionUtils.invokeHidden(applier, "collectFilters", node, filters);
 		assertTrue(filters.size() == 1);
 		assertSame(filter, filters.iterator().next());
 
 		filters.clear();
 		NavigationNodeId id2 = new NavigationNodeId("parent");
 		SubModuleNode node2 = new SubModuleNode(id2);
 		node2.addChild(node);
 		UIFilter filter2 = new UIFilter();
 		node2.addFilter(filter2);
 		ReflectionUtils.invokeHidden(applier, "collectFilters", node, filters);
 		assertTrue(filters.size() == 2);
 		assertSame(filter, filters.iterator().next());
 		assertTrue(filters.contains(filter2));
 
 		filters.clear();
 		NavigationNodeId id3 = new NavigationNodeId("child");
 		SubModuleNode node3 = new SubModuleNode(id3);
 		node.addChild(node3);
 		UIFilter filter3 = new UIFilter();
 		node3.addFilter(filter3);
 		ReflectionUtils.invokeHidden(applier, "collectFilters", node, filters);
 		assertTrue(filters.size() == 2);
 		assertFalse(filters.contains(filter3));
 
 	}
 
 	/**
 	 * Tests the method {@code applyFilters}.
 	 */
 	public void testApplyFilters() {
 
 		NavigationUIFilterApplier applier = new NavigationUIFilterApplier();
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		SubModuleNode node = new SubModuleNode(id);
 		node.setNavigationProcessor(new NavigationProcessor());
 
 		IUIFilter filter = new UIFilter();
 		filter.addFilterAttribute(new NavigationUIFilterAttributeDisabledMarker(node.getNodeId().getTypeId()));
 		node.addFilter(filter);
 		IUIFilter filter2 = new UIFilter();
		filter2.addFilterAttribute(new NavigationUIFilterAttributeHiddenMarker(node.getNodeId().getTypeId()));
 		node.addFilter(filter2);
 		ReflectionUtils.invokeHidden(applier, "applyFilters", node);
 		assertFalse(node.getMarkersOfType(DisabledMarker.class).isEmpty());
 		assertFalse(node.getMarkersOfType(HiddenMarker.class).isEmpty());
 
 	}
 
 	/**
 	 * Tests the method {@code applyFilter}.
 	 */
 	public void testApplyFilter() {
 
 		NavigationUIFilterApplier applier = new NavigationUIFilterApplier();
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		SubModuleNode node = new SubModuleNode(id);
 		node.setNavigationProcessor(new NavigationProcessor());
 
 		NavigationNodeId id2 = new NavigationNodeId("0815");
 		SubModuleNode node2 = new SubModuleNode(id2);
 		node.addChild(node2);
 
 		IUIFilter filter = new UIFilter();
		filter.addFilterAttribute(new NavigationUIFilterAttributeDisabledMarker(node.getNodeId().getTypeId()));
		filter.addFilterAttribute(new NavigationUIFilterAttributeHiddenMarker(node.getNodeId().getTypeId()));
 		IUIFilterAttributeClosure closure = ReflectionUtils.getHidden(applier, "applyClosure");
 		ReflectionUtils.invokeHidden(applier, "applyFilter", node, filter, closure);
 		assertFalse(node.getMarkersOfType(DisabledMarker.class).isEmpty());
 		assertFalse(node.getMarkersOfType(HiddenMarker.class).isEmpty());
 		assertFalse(node2.getMarkersOfType(DisabledMarker.class).isEmpty());
 		assertFalse(node2.getMarkersOfType(HiddenMarker.class).isEmpty());
 
 		closure = ReflectionUtils.getHidden(applier, "removeClosure");
 		ReflectionUtils.invokeHidden(applier, "applyFilter", node, filter, closure);
 		assertTrue(node.getMarkersOfType(DisabledMarker.class).isEmpty());
 		assertTrue(node.getMarkersOfType(HiddenMarker.class).isEmpty());
 		assertTrue(node2.getMarkersOfType(DisabledMarker.class).isEmpty());
 		assertTrue(node2.getMarkersOfType(HiddenMarker.class).isEmpty());
 
 	}
 
 	/**
 	 * Tests the method {@code applyFilterAttribute}.
 	 */
 	public void testApplyFilterAttribute() {
 
 		NavigationUIFilterApplier applier = new NavigationUIFilterApplier();
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		SubModuleNode node = new SubModuleNode(id);
 		node.setNavigationProcessor(new NavigationProcessor());
 
		IUIFilterAttribute attribute = new NavigationUIFilterAttributeDisabledMarker(node.getNodeId().getTypeId());
 		IUIFilterAttributeClosure closure = ReflectionUtils.getHidden(applier, "applyClosure");
 		ReflectionUtils.invokeHidden(applier, "applyFilterAttribute", node, attribute, closure);
 		assertFalse(node.getMarkersOfType(DisabledMarker.class).isEmpty());
 
 		SubModuleController controller = new SubModuleController();
 		node.setNavigationNodeController(controller);
		controller.setNavigationNode(node);
 		Shell shell = new Shell();
 		Label label = new Label(shell, SWT.NONE);
 		label.setData(SWTBindingPropertyLocator.BINDING_PROPERTY, "0815");
 		LabelRidget ridget = new LabelRidget(label);
 		controller.addRidget("0815", ridget);
 
 		attribute = new RidgetUIFilterAttributeHiddenMarker("0815");
 		ReflectionUtils.invokeHidden(applier, "applyFilterAttribute", node, attribute, closure);
 		assertFalse(ridget.getMarkersOfType(HiddenMarker.class).isEmpty());
 
 	}
 }
