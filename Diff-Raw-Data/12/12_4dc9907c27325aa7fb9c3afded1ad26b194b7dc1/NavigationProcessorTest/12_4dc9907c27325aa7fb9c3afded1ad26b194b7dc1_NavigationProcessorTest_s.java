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
 package org.eclipse.riena.navigation.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 
 import org.eclipse.riena.core.marker.IMarker;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.IJumpTargetListener;
 import org.eclipse.riena.navigation.IJumpTargetListener.JumpTargetState;
 import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.INavigationContext;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.INavigationProcessor;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.ui.controllers.NavigationNodeController;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.core.marker.OutputMarker;
 import org.eclipse.riena.ui.ridgets.IRidget;
 
 /**
  * Tests for the NavigationProcessor. Contains all tests for the
  * NavigationProcessor that don't use addPluginXml() and can be run on the
  * buildserver.
  * <p>
  * FIXME move all tests from {@link NavigationProcessorTest2} over here again,
  * when the addPluginXml() issue is fixed.
  */
 @NonUITestCase
 public class NavigationProcessorTest extends RienaTestCase {
 
 	private static final String TARGET_MODULE_GROUP = "org.eclipse.riena.navigation.model.test.moduleGroup.2";
 	private NavigationProcessor navigationProcessor;
 	private IApplicationNode applicationNode;
 	private ISubApplicationNode subApplication;
 	private IModuleGroupNode moduleGroup;
 	private IModuleNode module;
 	private ISubModuleNode subModule1;
 	private ISubModuleNode subModule2;
 	private ISubModuleNode subModule3;
 	private TestSubModuleNode subModule4;
 
 	private ModuleGroupNode moduleGroup2;
 	private ModuleNode module2;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		initializeNavigationStructure();
 	}
 
 	private void initializeNavigationStructure() {
 		applicationNode = new ApplicationNode(new NavigationNodeId(
 				"org.eclipse.riena.navigation.model.test.application"));
 		navigationProcessor = new NavigationProcessor();
 		applicationNode.setNavigationProcessor(navigationProcessor);
 
 		subApplication = new SubApplicationNode(new NavigationNodeId(
 				"org.eclipse.riena.navigation.model.test.subApplication"));
 		applicationNode.addChild(subApplication);
 		moduleGroup = new ModuleGroupNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.moduleGroup"));
 		subApplication.addChild(moduleGroup);
 		moduleGroup2 = new ModuleGroupNode(new NavigationNodeId(TARGET_MODULE_GROUP));
 		subApplication.addChild(moduleGroup2);
 
 		module = new ModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.module"));
 		moduleGroup.addChild(module);
 		module2 = new ModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.module.2"));
 		moduleGroup.addChild(module2);
 		subModule1 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule"));
 		module.addChild(subModule1);
 		subModule2 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule2"));
 		module.addChild(subModule2);
 		subModule3 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule3"));
 		module.addChild(subModule3);
 		subModule4 = new TestSubModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule4"));
 		module2.addChild(subModule4);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		applicationNode = null;
 		super.tearDown();
 	}
 
 	public void testNavigateToRidget() throws Exception {
 		// create a IRidgetMock that returns false on hasFocus
 		final IRidget ridgetStubWithoutFocus = EasyMock.createStrictMock(IRidget.class);
 		EasyMock.expect(ridgetStubWithoutFocus.hasFocus()).andReturn(false);
 		EasyMock.replay(ridgetStubWithoutFocus);
 
 		// create a IRidgetMock that ensures that the requestFocus-Method is called
 		final IRidget ridgetStub = EasyMock.createStrictMock(IRidget.class);
 		ridgetStub.requestFocus();
 		EasyMock.expect(ridgetStub.hasFocus()).andReturn(true);
 		EasyMock.replay(ridgetStub);
 
 		final String ridgetId = "myRidget";
 
 		// create a NavigationNodeControllerStub that returns the IRidgetMock
 		final NavigationNodeController<ISubModuleNode> nodeControllerStub = new NavigationNodeController<ISubModuleNode>() {
 			public void configureRidgets() {
 			}
 
 			@Override
 			public IRidget getRidget(final String id) {
 				return ridgetId.equals(id) ? ridgetStub : ridgetStubWithoutFocus;
 			}
 		};
 
 		subModule1.setNavigationNodeController(nodeControllerStub);
 
 		navigationProcessor.activate(subApplication);
 		navigationProcessor.navigate(subApplication, new NavigationNodeId(
 				"org.eclipse.riena.navigation.model.test.subModule"), new NavigationArgument(null, ridgetId));
 		//		assertTrue(subModule.isActivated());
 		//		assertTrue(ridgetStub.hasFocus());
 		//
 		//		assertFalse(ridgetStubWithoutFocus.hasFocus());
 		//
 		//		EasyMock.verify(ridgetStub);
 		//		EasyMock.reset(ridgetStub);
 	}
 
 	public void testActivateChildren() throws Exception {
 
 		assertFalse(subApplication.isActivated());
 		assertFalse(moduleGroup.isActivated());
 		assertFalse(module.isActivated());
 		assertFalse(subModule1.isActivated());
 
 		navigationProcessor.activate(subApplication);
 
 		assertTrue(subApplication.isActivated());
 		assertTrue(moduleGroup.isActivated());
 		assertTrue(module.isActivated());
 		assertTrue(subModule1.isActivated());
 
 		subApplication.deactivate(null);
 		moduleGroup.deactivate(null);
 		module.deactivate(null);
 		subModule1.deactivate(null);
 
 		subApplication.setEnabled(false);
 		navigationProcessor.activate(subApplication);
 		assertFalse(subApplication.isActivated());
 
 		subApplication.setEnabled(true);
 		subApplication.setVisible(false);
 		navigationProcessor.activate(subApplication);
 		assertFalse(subApplication.isActivated());
 
 	}
 
 	class DummyJumpTargetListener implements IJumpTargetListener {
 
 		IJumpTargetListener.JumpTargetState jumpTargetState;
 		INavigationNode<?> node;
 
 		DummyJumpTargetListener() {
 			reset();
 		}
 
 		public void jumpTargetStateChanged(final INavigationNode<?> node, final JumpTargetState jumpTargetState) {
 			this.jumpTargetState = jumpTargetState;
 			this.node = node;
 
 		}
 
 		void reset() {
 			jumpTargetState = null;
 			node = null;
 		}
 
 	}
 
 	public void testDefaultJump() throws Exception {
 		final DummyJumpTargetListener listener = new DummyJumpTargetListener();
 		subModule1.activate();
 		subModule2.addJumpTargetListener(listener);
 		subModule1.jump(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule2"));
 		assertSame(subModule2, listener.node);
 		assertSame(IJumpTargetListener.JumpTargetState.ENABLED, listener.jumpTargetState);
 		listener.reset();
 		assertTrue(subModule2.isActivated());
 		assertTrue(subModule2.isJumpTarget());
 		subModule2.jumpBack();
 		assertSame(subModule2, listener.node);
 		assertSame(IJumpTargetListener.JumpTargetState.DISABLED, listener.jumpTargetState);
 		assertFalse(subModule2.isJumpTarget());
 		assertTrue(subModule1.isActivated());
 		subModule1.jump(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule2"));
 		subModule2.jump(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule3"));
 		assertTrue(subModule3.isActivated());
 		subModule2.dispose();
 		subModule3.jumpBack();
 		assertTrue(subModule3.isActivated());
 
 		subModule2 = new SubModuleNode(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule2"));
 		module.addChild(subModule2);
 
 		subModule1.activate();
 		subModule1.jump(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule2"));
 		subModule2.navigate(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule3"));
 		subModule3.jump(new NavigationNodeId("org.eclipse.riena.navigation.model.test.subModule2"));
 		subModule2.jumpBack();
 		assertTrue(subModule3.isActivated());
 
 	}
 
 	public void testDeepJump() throws Exception {
 		subModule1.activate();
 		subModule1.jump(new NavigationNodeId("org.eclipse.riena.navigation.model.test.module.2"));
 		assertTrue(module2.isJumpTarget());
 		assertTrue(subModule4.isJumpTarget());
 		assertTrue(subModule4.isActivated());
 		subModule4.jumpBack();
 		assertTrue(subModule1.isActivated());
 		assertFalse(subModule4.isJumpTarget());
 		assertFalse(module2.isJumpTarget());
 	}
 
 	/**
	 * Tests the <i>private</i> method {@code getActivateableNodes}.
 	 */
	public void testGetActivateableNodes() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final SubModuleNode node = new SubModuleNode(id);
 		node.setNavigationProcessor(navigationProcessor);
 		id = new NavigationNodeId("0815");
 		final SubModuleNode node2 = new SubModuleNode(id);
 		node2.setNavigationProcessor(navigationProcessor);
 		id = new NavigationNodeId("node3");
 		final SubModuleNode node3 = new SubModuleNode(id);
 		node3.setNavigationProcessor(navigationProcessor);
 		id = new NavigationNodeId("node4");
 		final SubModuleNode node4 = new SubModuleNode(id);
 		node4.setNavigationProcessor(navigationProcessor);
 		final List<INavigationNode<?>> nodes = new ArrayList<INavigationNode<?>>();
 		nodes.add(node);
 		nodes.add(node2);
 		nodes.add(node3);
 		nodes.add(node4);
 
 		List<INavigationNode<?>> activateableNodes = ReflectionUtils.invokeHidden(navigationProcessor,
				"getActivateableNodes", nodes);
 		assertTrue(activateableNodes.size() == 4);
 		assertTrue(activateableNodes.contains(node));
 		assertTrue(activateableNodes.contains(node2));
 		assertTrue(activateableNodes.contains(node3));
 		assertTrue(activateableNodes.contains(node4));
 
 		node2.addMarker(new DisabledMarker());
 		node3.addMarker(new HiddenMarker());
 		node3.addMarker(new OutputMarker());
		activateableNodes = ReflectionUtils.invokeHidden(navigationProcessor, "getActivateableNodes", nodes);
 		assertTrue(activateableNodes.size() == 2);
 		assertTrue(activateableNodes.contains(node));
 		assertFalse(activateableNodes.contains(node2));
 		assertFalse(activateableNodes.contains(node3));
 		assertTrue(activateableNodes.contains(node4));
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getChildToActivate}.
 	 */
 	public void testGetChildToActivate() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final SubModuleNode node = new SubModuleNode(id);
 
 		INavigationNode<?> toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", node);
 		assertNull(toActivate);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleNode);
 		assertNull(toActivate);
 
 		moduleNode.addChild(node);
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleNode);
 		assertSame(node, toActivate);
 
 		id = new NavigationNodeId("sm2");
 		final SubModuleNode node2 = new SubModuleNode(id);
 		moduleNode.addChild(node2);
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleNode);
 		assertSame(node, toActivate);
 
 		node2.setSelected(true);
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleNode);
 		assertSame(node2, toActivate);
 
 		id = new NavigationNodeId("mg1");
 		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode(id);
 		moduleGroupNode.setNavigationProcessor(navigationProcessor);
 		moduleGroupNode.addChild(moduleNode);
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleGroupNode);
 		assertSame(moduleNode, toActivate);
 
 		final DisabledMarker disabledMarker = new DisabledMarker();
 		moduleNode.addMarker(disabledMarker);
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleGroupNode);
 		assertNull(toActivate);
 
 		moduleNode.removeAllMarkers();
 		moduleNode.addMarker(new OutputMarker());
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleGroupNode);
 		assertSame(moduleNode, toActivate);
 
 		moduleNode.addMarker(new HiddenMarker());
 		toActivate = ReflectionUtils.invokeHidden(navigationProcessor, "getChildToActivate", moduleGroupNode);
 		assertNull(toActivate);
 
 	}
 
 	/**
 	 * Tests the method {@code dispose}.
 	 */
 	public void testDispose() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final TestSubModuleNode node = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("0815");
 		final TestSubModuleNode node2 = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 		moduleNode.addChild(node);
 		moduleNode.addChild(node2);
 
 		id = new NavigationNodeId("m2");
 		final ModuleNode moduleNode2 = new ModuleNode(id);
 
 		id = new NavigationNodeId("mg1");
 		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode(id);
 		moduleGroupNode.addChild(moduleNode);
 		moduleGroupNode.addChild(moduleNode2);
 
 		navigationProcessor.activate(node2);
 		node2.setAllowsDeactivate(false);
 		navigationProcessor.dispose(node2);
 		assertFalse(node2.isDisposed());
 		assertTrue(node2.isActivated());
 
 		node2.setAllowsDeactivate(true);
 		node2.setAllowsDispose(false);
 		navigationProcessor.dispose(node2);
 		assertFalse(node2.isDisposed());
 		assertTrue(node2.isActivated());
 
 		node2.setAllowsDispose(true);
 		node.setAllowsActivate(false);
 		navigationProcessor.dispose(node2);
 		assertFalse(node2.isDisposed());
 		assertTrue(node2.isActivated());
 
 		node.setAllowsActivate(true);
 		navigationProcessor.dispose(node2);
 		assertTrue(node2.isDisposed());
 		assertTrue(node.isActivated());
 
 		navigationProcessor.dispose(node2);
 		assertTrue(node2.isDisposed());
 
 		navigationProcessor.dispose(moduleNode2);
 		assertTrue(moduleNode2.isDisposed());
 		assertFalse(moduleGroupNode.isDisposed());
 
 		navigationProcessor.dispose(moduleNode);
 		assertTrue(moduleNode.isDisposed());
 		assertTrue(moduleGroupNode.isDisposed());
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getNodeToDispose}.
 	 */
 	public void testGetNodeToDispose() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final SubModuleNode node = new SubModuleNode(id);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 		moduleNode.addChild(node);
 
 		id = new NavigationNodeId("m2");
 		final ModuleNode moduleNode2 = new ModuleNode(id);
 
 		id = new NavigationNodeId("mg1");
 		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode(id);
 		moduleGroupNode.addChild(moduleNode);
 		moduleGroupNode.addChild(moduleNode2);
 
 		INavigationNode<?> toDispose = ReflectionUtils.invokeHidden(navigationProcessor, "getNodeToDispose", node);
 		assertSame(node, toDispose);
 
 		toDispose = ReflectionUtils.invokeHidden(navigationProcessor, "getNodeToDispose", moduleNode);
 		assertSame(moduleGroupNode, toDispose);
 
 		toDispose = ReflectionUtils.invokeHidden(navigationProcessor, "getNodeToDispose", moduleNode2);
 		assertSame(moduleNode2, toDispose);
 
 		toDispose = ReflectionUtils.invokeHidden(navigationProcessor, "getNodeToDispose", moduleGroupNode);
 		assertSame(moduleGroupNode, toDispose);
 
 	}
 
 	/**
 	 * Tests the method {@code addMarker}.
 	 */
 	public void testAddMarker() {
 
 		final IMarker disabledMarker = new DisabledMarker();
 		final IMarker hiddenMarker = new HiddenMarker();
 		final IMarker errorMarker = new ErrorMarker();
 
 		navigationProcessor.addMarker(null, disabledMarker);
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final TestSubModuleNode node = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("0815");
 		final TestSubModuleNode node2 = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 		moduleNode.addChild(node);
 		moduleNode.addChild(node2);
 		moduleNode.setNavigationProcessor(navigationProcessor);
 
 		navigationProcessor.addMarker(node, disabledMarker);
 		assertFalse(node.isEnabled());
 		assertTrue(node.isVisible());
 
 		navigationProcessor.addMarker(node2, hiddenMarker);
 		assertTrue(node2.isEnabled());
 		assertFalse(node2.isVisible());
 
 		node.removeAllMarkers();
 		node2.removeAllMarkers();
 
 		node.setSelected(true);
 		navigationProcessor.addMarker(node, disabledMarker);
 		assertFalse(node.isEnabled());
 		assertFalse(node.isSelected());
 
 		node.setSelected(true);
 		navigationProcessor.addMarker(node, hiddenMarker);
 		assertFalse(node.isVisible());
 		assertFalse(node.isSelected());
 
 		node.setSelected(true);
 		navigationProcessor.addMarker(node, errorMarker);
 		assertTrue(node.isSelected());
 
 		node.removeAllMarkers();
 		node2.removeAllMarkers();
 		node2.activate();
 		navigationProcessor.addMarker(node2, errorMarker);
 		assertTrue(node2.isActivated());
 
 		node2.removeAllMarkers();
 		node2.activate();
 		navigationProcessor.addMarker(node2, disabledMarker);
 		assertFalse(node2.isActivated());
 		assertFalse(node2.isEnabled());
 		assertTrue(node.isActivated());
 
 		node2.removeAllMarkers();
 		node2.activate();
 		navigationProcessor.addMarker(node2, hiddenMarker);
 		assertFalse(node2.isActivated());
 		assertFalse(node2.isVisible());
 		assertTrue(node.isActivated());
 
 		node2.removeAllMarkers();
 		node2.activate();
 		node2.setAllowsDeactivate(false);
 		navigationProcessor.addMarker(node2, hiddenMarker);
 		assertTrue(node2.isVisible());
 		assertFalse(node.isActivated());
 		assertTrue(node2.isActivated());
 
 		node2.removeAllMarkers();
 		node2.activate();
 		node2.setAllowsDeactivate(true);
 		node.setAllowsActivate(false);
 		navigationProcessor.addMarker(node2, hiddenMarker);
 		assertTrue(node2.isVisible());
 		assertFalse(node.isActivated());
 		assertTrue(node2.isActivated());
 
 		node2.removeAllMarkers();
 		node2.activate();
 		node.setAllowsActivate(true);
 		navigationProcessor.addMarker(node2, hiddenMarker);
 		assertFalse(node2.isVisible());
 		assertTrue(node.isActivated());
 		assertFalse(node2.isActivated());
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getTopParent}.
 	 */
 	public void testGetTopParent() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final TestSubModuleNode node = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("0815");
 		final TestSubModuleNode node2 = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 		moduleNode.addChild(node);
 		moduleNode.addChild(node2);
 
 		id = new NavigationNodeId("m2");
 		final ModuleNode moduleNode2 = new ModuleNode(id);
 
 		id = new NavigationNodeId("mg1");
 		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode(id);
 		moduleGroupNode.addChild(moduleNode);
 
 		INavigationNode<?> top = ReflectionUtils.invokeHidden(navigationProcessor, "getTopParent", node);
 		assertSame(moduleGroupNode, top);
 
 		top = ReflectionUtils.invokeHidden(navigationProcessor, "getTopParent", node2);
 		assertSame(moduleGroupNode, top);
 
 		top = ReflectionUtils.invokeHidden(navigationProcessor, "getTopParent", moduleNode);
 		assertSame(moduleGroupNode, top);
 
 		top = ReflectionUtils.invokeHidden(navigationProcessor, "getTopParent", moduleGroupNode);
 		assertSame(moduleGroupNode, top);
 
 		top = ReflectionUtils.invokeHidden(navigationProcessor, "getTopParent", moduleNode2);
 		assertSame(moduleNode2, top);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getNextActiveParent}.
 	 */
 	public void testGetNextActiveParent() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final TestSubModuleNode node = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("0815");
 		final TestSubModuleNode node2 = new TestSubModuleNode(id);
 		node.addChild(node2);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 		moduleNode.addChild(node);
 
 		id = new NavigationNodeId("m2");
 		final ModuleNode moduleNode2 = new ModuleNode(id);
 
 		id = new NavigationNodeId("mg1");
 		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode(id);
 		moduleGroupNode.addChild(moduleNode);
 		moduleGroupNode.addChild(moduleNode2);
 		moduleGroupNode.setNavigationProcessor(navigationProcessor);
 
 		INavigationNode<?> parent = ReflectionUtils.invokeHidden(navigationProcessor, "getNextActiveParent", node2);
 		assertNull(parent);
 
 		node.activate();
 		parent = ReflectionUtils.invokeHidden(navigationProcessor, "getNextActiveParent", node2);
 		assertSame(moduleNode, parent);
 
 		node.deactivate(null);
 		parent = ReflectionUtils.invokeHidden(navigationProcessor, "getNextActiveParent", node2);
 		assertSame(moduleNode, parent);
 
 		moduleNode.deactivate(null);
 		parent = ReflectionUtils.invokeHidden(navigationProcessor, "getNextActiveParent", node2);
 		assertSame(moduleGroupNode, parent);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getActiveChild}.
 	 */
 	public void testGetActiveChild() {
 
 		NavigationNodeId id = new NavigationNodeId("4711");
 		final TestSubModuleNode node = new TestSubModuleNode(id);
 
 		id = new NavigationNodeId("0815");
 		final TestSubModuleNode node2 = new TestSubModuleNode(id);
 		node.addChild(node2);
 
 		id = new NavigationNodeId("m1");
 		final ModuleNode moduleNode = new ModuleNode(id);
 		moduleNode.addChild(node);
 
 		id = new NavigationNodeId("m2");
 		final ModuleNode moduleNode2 = new ModuleNode(id);
 
 		id = new NavigationNodeId("mg1");
 		final ModuleGroupNode moduleGroupNode = new ModuleGroupNode(id);
 		moduleGroupNode.addChild(moduleNode);
 		moduleGroupNode.addChild(moduleNode2);
 		moduleGroupNode.setNavigationProcessor(navigationProcessor);
 
 		INavigationNode<?> child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", node);
 		assertNull(child);
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", moduleNode);
 		assertNull(child);
 
 		node.setSelected(true);
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", node);
 		assertNull(child);
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", moduleNode);
 		assertNull(child);
 
 		node.activate();
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", node);
 		assertNull(child);
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", moduleNode);
 		assertSame(node, child);
 
 		node2.setSelected(true);
 		node2.activate();
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", node);
 		assertNull(child);
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", moduleNode);
 		assertSame(node2, child);
 
 		child = ReflectionUtils.invokeHidden(navigationProcessor, "getActiveChild", moduleGroupNode);
 		assertSame(moduleNode, child);
 
 	}
 
 	/**
 	 * Tests the method {@code move}.
 	 */
 	public void testMove() throws Exception {
 		final INavigationNode<?> targetModuleGroup = createTargetModuleGroup();
 		assertEquals(module2, moduleGroup.getChild(1));
 		assertEquals(2, moduleGroup.getChildren().size());
 		assertEquals(0, moduleGroup2.getChildren().size());
 		module2.moveTo(new NavigationNodeId(TARGET_MODULE_GROUP));
 		assertEquals(1, moduleGroup.getChildren().size());
 		assertEquals(1, moduleGroup2.getChildren().size());
 		assertEquals(module2, targetModuleGroup.getChild(0));
 	}
 
 	/**
 	 * Tests the method {@code move} with the attribute blocked set.
 	 */
 	public void testMoveBlocked() throws Exception {
 		final INavigationNode<?> targetModuleGroup = createTargetModuleGroup();
 		module2.setBlocked(true);
 		module2.moveTo(new NavigationNodeId(TARGET_MODULE_GROUP));
 		assertEquals(module2, targetModuleGroup.getChild(0));
 		assertTrue(module2.isBlocked());
 		assertFalse(moduleGroup2.isBlocked());
 	}
 
 	/**
 	 * Tests the method {@code move} with the attribute enabled set.
 	 */
 	public void testMoveEnabled() throws Exception {
 		final INavigationNode<?> targetModuleGroup = createTargetModuleGroup();
 		module2.setEnabled(true);
 		module2.moveTo(new NavigationNodeId(TARGET_MODULE_GROUP));
 		assertEquals(module2, targetModuleGroup.getChild(0));
 		assertTrue(module2.isEnabled());
 	}
 
 	/**
 	 * Tests the method {@code move} with the attribute visible set.
 	 */
 	public void testMoveVisible() throws Exception {
 		final INavigationNode<?> targetModuleGroup = createTargetModuleGroup();
 		module2.setVisible(true);
 		module2.moveTo(new NavigationNodeId(TARGET_MODULE_GROUP));
 		assertEquals(module2, targetModuleGroup.getChild(0));
 		assertTrue(module2.isVisible());
 	}
 
 	/**
 	 * Tests the method {@code prepare(INavigationNode<?>)}.
 	 */
 	public void testPrepare() {
 
 		final TestSubModuleNode node = new TestSubModuleNode(new NavigationNodeId("4711"));
 		navigationProcessor.prepare(node);
 		final INavigationContext context = node.getNaviContext();
 		assertNotNull(context);
 		assertNotNull(context.getToPrepare());
 		assertEquals(1, context.getToPrepare().size());
 		assertTrue(context.getToActivate().isEmpty());
 		assertTrue(context.getToDeactivate().isEmpty());
 		assertSame(node, context.getToPrepare().get(0));
 
 	}
 
 	/**
 	 * Tests the method {@code historyBack()}.
 	 */
 	public void testHistoryBack() {
 
 		final TestSubModuleNode node = new TestSubModuleNode(new NavigationNodeId("4711"));
 		module.addChild(node);
 		subModule1.activate();
 		node.activate();
 		assertFalse(subModule1.isActivated());
 		assertTrue(node.isActivated());
 		assertEquals(0, navigationProcessor.getHistoryForwardSize());
 		assertEquals(1, navigationProcessor.getHistoryBackSize());
 
 		navigationProcessor.historyBack();
 		assertTrue(subModule1.isActivated());
 		assertFalse(node.isActivated());
 		assertEquals(1, navigationProcessor.getHistoryForwardSize());
 		assertEquals(0, navigationProcessor.getHistoryBackSize());
 
 		navigationProcessor.historyBack();
 		assertTrue(subModule1.isActivated());
 		assertFalse(node.isActivated());
 		assertEquals(1, navigationProcessor.getHistoryForwardSize());
 		assertEquals(0, navigationProcessor.getHistoryBackSize());
 
 	}
 
 	/**
 	 * Tests the method {@code historyForeward()}.
 	 */
 	public void testHistoryForeward() {
 
 		final TestSubModuleNode node = new TestSubModuleNode(new NavigationNodeId("4711"));
 		module.addChild(node);
 		subModule1.activate();
 		node.activate();
 		assertFalse(subModule1.isActivated());
 		assertTrue(node.isActivated());
 		assertEquals(0, navigationProcessor.getHistoryForwardSize());
 		assertEquals(1, navigationProcessor.getHistoryBackSize());
 
 		navigationProcessor.historyBack();
 		assertTrue(subModule1.isActivated());
 		assertFalse(node.isActivated());
 		assertEquals(1, navigationProcessor.getHistoryForwardSize());
 		assertEquals(0, navigationProcessor.getHistoryBackSize());
 
 		navigationProcessor.historyForward();
 		assertFalse(subModule1.isActivated());
 		assertTrue(node.isActivated());
 		assertEquals(0, navigationProcessor.getHistoryForwardSize());
 		assertEquals(1, navigationProcessor.getHistoryBackSize());
 
 		navigationProcessor.historyForward();
 		assertFalse(subModule1.isActivated());
 		assertTrue(node.isActivated());
 		assertEquals(0, navigationProcessor.getHistoryForwardSize());
 		assertEquals(1, navigationProcessor.getHistoryBackSize());
 
 	}
 
 	/**
 	 * Tests the method {@code getHistory()}.
 	 */
 	public void testGetHistory() {
 
 		TestSubModuleNode node = new TestSubModuleNode(new NavigationNodeId("4711"));
 		module.addChild(node);
 		subModule1.activate();
 		node.activate();
 		assertFalse(subModule1.isActivated());
 		assertTrue(node.isActivated());
 		assertEquals(0, navigationProcessor.getHistoryForwardSize());
 		assertEquals(1, navigationProcessor.getHistoryBackSize());
 		assertEquals(2, navigationProcessor.getHistory().size());
 		assertEquals(subModule1, navigationProcessor.getHistory().get(0));
 		assertEquals(node, navigationProcessor.getHistory().get(1));
 
 		node = new TestSubModuleNode(new NavigationNodeId("0815"));
 		try {
 			navigationProcessor.getHistory().add(node);
 			fail();
 		} catch (final UnsupportedOperationException e) {
 			// getHistory().add() must provide an UnmodifiableCollection.
 			assertTrue(e instanceof UnsupportedOperationException);
 		}
 		try {
 			navigationProcessor.getHistory().remove(subModule1);
 			fail();
 		} catch (final UnsupportedOperationException e) {
 			// getHistory().remove() must provide an UnmodifiableCollection.
 			assertTrue(e instanceof UnsupportedOperationException);
 		}
 	}
 
 	/**
 	 * Tests the <i>private</i> method
 	 * {@code findSelectableChildNode(ISubModuleNode)}
 	 */
 	public void testFindSelectableChildNode() {
 
 		ISubModuleNode selectableChild = ReflectionUtils.invokeHidden(navigationProcessor, "findSelectableChildNode",
 				subModule1);
 		assertSame(subModule1, selectableChild);
 
 		subModule1.setSelectable(false);
 		selectableChild = ReflectionUtils.invokeHidden(navigationProcessor, "findSelectableChildNode", subModule1);
 		assertNull(selectableChild);
 
 		final TestSubModuleNode node = new TestSubModuleNode(new NavigationNodeId("4711"));
 		subModule1.addChild(node);
 		selectableChild = ReflectionUtils.invokeHidden(navigationProcessor, "findSelectableChildNode", subModule1);
 		assertSame(node, selectableChild);
 
 	}
 
 	/**
 	 * Tests the methods
 	 * {@link INavigationProcessor#jump(INavigationNode, NavigationNodeId, NavigationArgument)}
 	 * and {@link INavigationProcessor#jumpBack(INavigationNode)}
 	 * 
 	 * @throws Exception
 	 */
 	public void testJump() throws Exception {
 
 		final ApplicationNode app = new ApplicationNode();
 		final SubApplicationNode subApp = new SubApplicationNode();
 		app.addChild(subApp);
 
 		final ModuleGroupNode mg = new ModuleGroupNode();
 		subApp.addChild(mg);
 
 		final ModuleNode m1 = new ModuleNode(new NavigationNodeId("m1"));
 		mg.addChild(m1);
 		final ModuleNode m2 = new ModuleNode(new NavigationNodeId("m2"));
 		mg.addChild(m2);
 
 		final SubModuleNode m1s1 = new SubModuleNode(new NavigationNodeId("m1s1"));
 		m1.addChild(m1s1);
 
 		final SubModuleNode m1s2 = new SubModuleNode(new NavigationNodeId("m1s2"));
 		m1.addChild(m1s2);
 
 		final SubModuleNode m1s1s1 = new SubModuleNode(new NavigationNodeId("m1s1s1"));
 		m1s1.addChild(m1s1s1);
 
 		final SubModuleNode m2s1 = new SubModuleNode(new NavigationNodeId("m2s1"));
 		m2.addChild(m2s1);
 
 		final SubModuleNode m2s2 = new SubModuleNode(new NavigationNodeId("m2s2"));
 		m2.addChild(m2s2);
 
 		app.activate();
 
 		final IJumpTargetListener listenerMock = EasyMock.createMock(IJumpTargetListener.class);
 		listenerMock.jumpTargetStateChanged(m1, JumpTargetState.ENABLED);
 		EasyMock.expectLastCall().times(1);
 		EasyMock.replay(listenerMock);
 		m1.addJumpTargetListener(listenerMock);
 
 		m1s2.jump(new NavigationNodeId("m1s1s1"));
 		assertTrue(m1s1s1.isActivated());
 		assertTrue(m1s1s1.isJumpTarget());
 		assertTrue(m1.isJumpTarget());
 		assertTrue(m2.isJumpTarget());
 		assertTrue(m2s1.isJumpTarget());
 		assertFalse(subApp.isJumpTarget());
 
 		EasyMock.verify(listenerMock);
 		EasyMock.reset(listenerMock);
 
 		listenerMock.jumpTargetStateChanged(m1, JumpTargetState.DISABLED);
 		EasyMock.expectLastCall().times(1);
 		EasyMock.replay(listenerMock);
 		m1s2.addSimpleListener(new SimpleNavigationNodeAdapter() {
 			@Override
 			public void activated(final INavigationNode<?> source) {
 				m1s2.isJumpTarget();
 			}
 		});
 
 		m2s1.jumpBack();
 		EasyMock.verify(listenerMock);
 		EasyMock.reset(listenerMock);
 		assertTrue(m1s2.isActivated());
 
 		m1s2.jump(new NavigationNodeId("m1s1"));
 		assertTrue(m1s1.isActivated());
 
 		m1s1.jump(new NavigationNodeId("m2s1"));
 		assertTrue(m2s1.isActivated());
 		m2s1.jumpBack();
 		assertTrue(m1s1.isJumpTarget());
 		assertTrue(m1.isJumpTarget());
 		assertTrue(m2.isJumpTarget());
 		assertTrue(m2s1.isJumpTarget());
 		assertFalse(subApp.isJumpTarget());
 		assertTrue(m1s1.isActivated());
 		m1.jumpBack();
 		assertTrue(m1s2.isActivated());
 		m1.jumpBack();
 		//clean state / no jump targets registered
 
 		m1.jump(new NavigationNodeId("m2s1"));
 
 		EasyMock.reset(listenerMock);
 		listenerMock.jumpTargetStateChanged(m1, JumpTargetState.DISABLED);
 		EasyMock.expectLastCall().times(1);
 		EasyMock.replay(listenerMock);
 
 		m2s1.dispose();
 		EasyMock.verify(listenerMock);
 		EasyMock.reset(listenerMock);
 		assertFalse(m1s1.isJumpTarget());
 		assertFalse(m1.isJumpTarget());
 		assertFalse(m2.isJumpTarget());
 
 		m1s1.jump(new NavigationNodeId("m2s2"));
 		EasyMock.reset(listenerMock);
 		listenerMock.jumpTargetStateChanged(m1, JumpTargetState.DISABLED);
 		EasyMock.expectLastCall().times(1);
 		EasyMock.replay(listenerMock);
 		assertTrue(m2s2.isActivated());
 		assertTrue(m1s1.isJumpTarget());
 		assertTrue(m1.isJumpTarget());
 		assertTrue(m2.isJumpTarget());
 		m1s1.dispose();
 		EasyMock.verify(listenerMock);
 		EasyMock.reset(listenerMock);
 		assertFalse(m1s1.isJumpTarget());
 		assertFalse(m1.isJumpTarget());
 		assertFalse(m2.isJumpTarget());
 
 	}
 
 	private INavigationNode<?> createTargetModuleGroup() {
 		final INavigationNode<?> targetModuleGroup = navigationProcessor.create(subApplication, new NavigationNodeId(
 				TARGET_MODULE_GROUP), null);
 		return targetModuleGroup;
 	}
 
 	public static class TestSubModuleNode extends SubModuleNode {
 
 		private boolean allowsActivate;
 		private boolean allowsDeactivate;
 		private boolean allowsDispose;
 		private INavigationContext naviContext;
 
 		public TestSubModuleNode(final NavigationNodeId nodeId) {
 			super(nodeId);
 			allowsActivate = true;
 			allowsDeactivate = true;
 			allowsDispose = true;
 		}
 
 		@Override
 		public boolean allowsActivate(final INavigationContext context) {
 			return allowsActivate;
 		}
 
 		@Override
 		public boolean allowsDeactivate(final INavigationContext context) {
 			return allowsDeactivate;
 		}
 
 		@Override
 		public boolean allowsDispose(final INavigationContext context) {
 			return allowsDispose;
 		}
 
 		public void setAllowsActivate(final boolean allowsActivate) {
 			this.allowsActivate = allowsActivate;
 		}
 
 		public void setAllowsDeactivate(final boolean allowsDeactivate) {
 			this.allowsDeactivate = allowsDeactivate;
 		}
 
 		public void setAllowsDispose(final boolean allowsDispose) {
 			this.allowsDispose = allowsDispose;
 		}
 
 		@Override
 		public void prepare(final INavigationContext context) {
 			super.prepare(context);
 			naviContext = context;
 		}
 
 		public INavigationContext getNaviContext() {
 			return naviContext;
 		}
 
 	}
 
 }
