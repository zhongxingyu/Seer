 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.easymock.EasyMock;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IViewSite;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ApplicationNode;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ModuleController;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.controller.IController;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.AbstractViewBindingDelegate;
 import org.eclipse.riena.ui.ridgets.swt.uibinding.DefaultSwtBindingDelegate;
 
 /**
  * Tests for the SubModuleNodeView.
  */
 @UITestCase
 public class SubModuleViewTest extends RienaTestCase {
 
 	private SubModuleView subModuleNodeView;
 	private SubModuleNode node;
 	private SubModuleNode anotherNode;
 	private SubModuleNode anotherNodeSameView;
 	private List<SubModuleNode> nodesBoundToView;
 	private ApplicationNode appNode;
 	private IModuleNode moduleNode;
 	private ArrayList<SubModuleNode> nodesBoundToSharedView;
 
 	private Shell shell;
 	private Composite parentComposite;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		shell = new Shell();
 		parentComposite = new Composite(shell, SWT.NONE);
 		addPluginXml(SubModuleViewTest.class, "SubModuleViewTest.xml");
 
 		appNode = new ApplicationNode();
 		final SubApplicationNode subAppNode = new SubApplicationNode();
 		appNode.addChild(subAppNode);
 		final ModuleGroupNode mgNode = new ModuleGroupNode(null);
 		subAppNode.addChild(mgNode);
 		moduleNode = new ModuleNode(null, "TestModuleLabel");
 		mgNode.addChild(moduleNode);
 
 		anotherNode = new SubModuleNode(new NavigationNodeId("testId2", "2"), "TestSubModuleLabel2");
 		moduleNode.addChild(anotherNode);
 		anotherNodeSameView = new SubModuleNode(new NavigationNodeId("testId", "1"), "TestSubModuleLabel3");
 		moduleNode.addChild(anotherNodeSameView);
 		nodesBoundToView = new ArrayList<SubModuleNode>();
 		nodesBoundToSharedView = new ArrayList<SubModuleNode>();
 
 		subModuleNodeView = new TestView();
 		node = new SubModuleNode(new NavigationNodeId("testId", "0"), "TestSubModuleLabel");
 		moduleNode.setNavigationNodeController(new ModuleController(moduleNode));
 		moduleNode.addChild(node);
 		subModuleNodeView.createPartControl(new Shell());
 		node.activate();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		node.deactivate(null);
 		removeExtension("sub.module.view.test");
 		shell.dispose();
 		super.tearDown();
 	}
 
 	public void testBlocking() {
 		final Composite parentComposite = ReflectionUtils.invokeHidden(subModuleNodeView, "getParentComposite");
 		final Composite contentComposite = ReflectionUtils.invokeHidden(subModuleNodeView, "getContentComposite");
 		final Cursor oldCursor = parentComposite.getCursor();
 		final Cursor waitCursor = parentComposite.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 
 		node.setBlocked(true);
 
 		assertFalse(contentComposite.isEnabled());
 		assertSame(waitCursor, parentComposite.getCursor());
 
 		node.setBlocked(false);
 
 		assertTrue(contentComposite.isEnabled());
 		assertSame(oldCursor, parentComposite.getCursor());
 
 		// blocking several times should still have the same cursor outcome
 		node.setBlocked(true);
 		node.setBlocked(true);
 
 		assertFalse(contentComposite.isEnabled());
 		assertSame(waitCursor, parentComposite.getCursor());
 
 		node.setBlocked(false);
 
 		assertTrue(contentComposite.isEnabled());
 		assertSame(oldCursor, parentComposite.getCursor());
 	}
 
 	public void testCreateController() throws Exception {
 		assertNotNull(subModuleNodeView.getController());
 		assertEquals(node, subModuleNodeView.getController().getNavigationNode());
 	}
 
 	public void testShared() {
 		subModuleNodeView.dispose();
 		final SubModuleNode s1 = new SubModuleNode(new NavigationNodeId("s", "1"));
 		final SubModuleNode s2 = new SubModuleNode(new NavigationNodeId("s", "2"));
 		moduleNode.addChild(s1);
 		moduleNode.addChild(s2);
 		final TestSharedView subModuleNodeSharedView = new TestSharedView();
 		subModuleNodeSharedView.s1 = s1;
 		subModuleNodeSharedView.s2 = s2;
 		subModuleNodeSharedView.createPartControl(new Shell());
 		assertNotNull(s1.getNavigationNodeController());
 		s1.activate();
 		final SubModuleController s1c = (SubModuleController) s1.getNavigationNodeController();
 		assertNotNull(s1c.getRidget("button"));
 		assertEquals(nodesBoundToSharedView.get(0), s1);
 		s2.activate();
 		assertEquals(nodesBoundToSharedView.get(2), s2);
 		s1.dispose();
 		assertFalse(subModuleNodeSharedView.unbindActiveCalled);
 		s2.dispose();
 		assertTrue(subModuleNodeSharedView.unbindActiveCalled);
 	}
 
 	public void testBindOnActivate() throws Exception {
 		nodesBoundToView.clear();
 
 		anotherNode.activate();
 
 		assertTrue(nodesBoundToView.isEmpty());
 
 		anotherNodeSameView.activate();
 
 		assertTrue(nodesBoundToView.isEmpty());
 
 		node.activate();
 		assertEquals(1, nodesBoundToView.size());
 		assertSame(node, nodesBoundToView.get(0));
 	}
 
 	public void testBlockOnBind() throws Exception {
 		node.setBlocked(true);
 		TestView2 view2 = new TestView2();
 		view2.bind(node);
 		assertNotNull(view2.blockViewCalled[0]);
 		assertTrue(view2.blockViewCalled[0]);
 		node.setBlocked(false);
 		view2 = new TestView2();
 		view2.bind(node);
 		assertNotNull(view2.blockViewCalled[0]);
 		assertFalse(view2.blockViewCalled[0]);
 	}
 
 	public void testShared2() {
 		final SubModuleNode node = new SubModuleNode(new NavigationNodeId("testId", SubModuleView.SHARED_ID));
 		final TestSharedView2 smv = new TestSharedView2(node);
 		try {
 			smv.bind(node);
 		} catch (final Exception e) {
 			fail();
 		}
 		assertTrue(smv.bindCalled);
 
 	}
 
 	private final class TestSharedView2 extends SubModuleView {
 
 		private final SubModuleNode node;
 		protected boolean bindCalled = false;
 
 		public TestSharedView2(final SubModuleNode node) {
 			this.node = node;
 			ReflectionUtils.setHidden(this, "contentComposite", parentComposite);
 		}
 
 		@Override
 		protected void basicCreatePartControl(final Composite parent) {
 
 		}
 
 		@Override
 		protected AbstractViewBindingDelegate createBinding() {
 			return new DefaultSwtBindingDelegate() {
 
 				@Override
 				public void bind(final IController controller) {
 					super.bind(controller);
 					bindCalled = true;
 				}
 
 			};
 		}
 
 		@Override
 		protected Composite getParentComposite() {
 			return parentComposite;
 		}
 
 		@Override
 		protected void addUIControls(final Composite composite) {
 		}
 
 		@Override
 		public SubModuleNode getNavigationNode() {
 			return node;
 		}
 	}
 
 	private final class TestSharedView extends SubModuleView {
 
 		private SubModuleNode s1;
 		private SubModuleNode s2;
 		private Button button;
 
 		private boolean unbindActiveCalled;
 
 		@Override
 		public void bind(final SubModuleNode node) {
 			if (node.getNavigationNodeController() == null) {
 				node.setNavigationNodeController(createController(node));
 			}
 			ReflectionUtils.setHidden(this, "currentController", node.getNavigationNodeController());
 			nodesBoundToSharedView.add(node);
 		}
 
 		@Override
 		public SubModuleNode getNavigationNode() {
 			return s1.isActivated() ? s1 : s2.isActivated() ? s2 : s1;
 		}
 
 		@Override
 		protected void basicCreatePartControl(final Composite parent) {
 			button = new Button(parent, SWT.NONE);
 			addUIControl(button, "button");
 		}
 
 		@Override
 		public IViewSite getViewSite() {
 			return EasyMock.createNiceMock(IViewSite.class);
 		}
 
 		@Override
 		protected void unbindActiveController() {
 			unbindActiveCalled = true;
 		}
 
 		@Override
 		protected SubModuleController createController(final ISubModuleNode node) {
 			return new SubModuleController(node);
 		}
 
 		@Override
 		protected String getSecondaryId() {
 			return SubModuleView.SHARED_ID;
 		}
 
 		@Override
 		protected IApplicationNode getAppNode() {
 			return appNode;
 		}
 	}
 
 	private final class TestView2 extends SubModuleView {
 
 		private final Composite cp;
 
 		TestView2() {
 			cp = new Composite(new Shell(), SWT.NONE);
 		}
 
 		private final boolean[] blockViewCalled = new boolean[1];
 
 		@Override
 		protected void basicCreatePartControl(final Composite parent) {
 		}
 
 		@Override
 		protected Composite getParentComposite() {
 			return cp;
 		}
 
 		@Override
 		protected Composite getContentComposite() {
 			return cp;
 		}
 
 		@Override
 		protected void blockView(final boolean block) {
 			blockViewCalled[0] = block;
 		}
 
 	}
 
 	// helping classes
 	//////////////////
 
 	private final class TestView extends SubModuleView {
 		@Override
 		public void bind(final SubModuleNode node) {
 			nodesBoundToView.add(node);
 		}
 
 		@Override
 		public SubModuleNode getNavigationNode() {
 			return node;
 		}
 
 		@Override
 		protected void basicCreatePartControl(final Composite parent) {
 		}
 
 		@Override
 		protected IApplicationNode getAppNode() {
 			return appNode;
 		}
 
 		@Override
 		protected void registerView() {
 		}
 
 		@Override
 		protected void destroyView() {
 		}
 	}
 
 }
