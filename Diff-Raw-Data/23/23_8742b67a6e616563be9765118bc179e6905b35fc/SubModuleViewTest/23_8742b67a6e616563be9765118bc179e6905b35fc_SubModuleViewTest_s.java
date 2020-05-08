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
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ApplicationNode;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ModuleController;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.common.IComplexComponent;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Tests for the SubModuleNodeView.
  */
@UITestCase
 public class SubModuleViewTest extends RienaTestCase {
 
 	private SubModuleView<SubModuleController> subModuleNodeView;
 	private SubModuleNode node;
 	private SubModuleNode anotherNode;
 	private SubModuleNode anotherNodeSameView;
 	private List<SubModuleNode> nodesBoundToView;
 
 	@Override
 	protected void setUp() throws Exception {
 
 		super.setUp();
 		addPluginXml(SubModuleViewTest.class, "SubModuleViewTest.xml");
 		ApplicationNode appNode = new ApplicationNode();
 		SubApplicationNode subAppNode = new SubApplicationNode();
 		appNode.addChild(subAppNode);
 		ModuleGroupNode mgNode = new ModuleGroupNode(null);
 		subAppNode.addChild(mgNode);
 		IModuleNode parent = new ModuleNode(null, "TestModuleLabel");
 		mgNode.addChild(parent);
 
 		anotherNode = new SubModuleNode(new NavigationNodeId("testId2"), "TestSubModuleLabel2");
 		parent.addChild(anotherNode);
 		anotherNodeSameView = new SubModuleNode(new NavigationNodeId("testId"), "TestSubModuleLabel3");
 		parent.addChild(anotherNodeSameView);
 		nodesBoundToView = new ArrayList<SubModuleNode>();
 
 		subModuleNodeView = new TestView();
 		node = new SubModuleNode(new NavigationNodeId("testId"), "TestSubModuleLabel");
 		parent.setNavigationNodeController(new ModuleController(parent));
 		parent.addChild(node);
 		subModuleNodeView.createPartControl(new Shell());
 		node.activate();
 	}
 
 	public void testCreateController() throws Exception {
 
 		assertNotNull(subModuleNodeView.getController());
 		assertEquals(node, subModuleNodeView.getController().getNavigationNode());
 	}
 
 	public void testBlocking() {
 		node.setBlocked(true);
 		Composite parentComposite = ReflectionUtils.invokeHidden(subModuleNodeView, "getParentComposite");
 		Composite contentComposite = ReflectionUtils.invokeHidden(subModuleNodeView, "getContentComposite");
 		assertFalse(contentComposite.isEnabled());
 		Cursor waitCursor = parentComposite.getDisplay().getSystemCursor(SWT.CURSOR_WAIT);
 		assertSame(waitCursor, parentComposite.getCursor());
 		node.setBlocked(false);
 		assertTrue(contentComposite.isEnabled());
 		Cursor arrowCursor = parentComposite.getDisplay().getSystemCursor(SWT.CURSOR_ARROW);
 		assertSame(arrowCursor, parentComposite.getCursor());
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code
 	 * isChildOfComplexComponent(Control)}.
 	 */
 	public void testIsChildOfComplexComponent() {
 
 		subModuleNodeView = new TestView();
 
 		Shell shell = new Shell();
 		Label label = new Label(shell, SWT.NONE);
 		boolean ret = ReflectionUtils.invokeHidden(subModuleNodeView, "isChildOfComplexComponent", label);
 		assertFalse(ret);
 
 		TestComplexComponent complexComponent = new TestComplexComponent(shell, SWT.NONE);
 		Text text = new Text(complexComponent, SWT.NONE);
 		ret = ReflectionUtils.invokeHidden(subModuleNodeView, "isChildOfComplexComponent", text);
 		assertTrue(ret);
 
 		Composite composite = new Composite(shell, SWT.NONE);
 		Combo combo = new Combo(composite, SWT.NONE);
 		ret = ReflectionUtils.invokeHidden(subModuleNodeView, "isChildOfComplexComponent", combo);
 		assertFalse(ret);
 
 		Composite composite2 = new Composite(complexComponent, SWT.NONE);
 		Button button = new Button(composite2, SWT.NONE);
 		ret = ReflectionUtils.invokeHidden(subModuleNodeView, "isChildOfComplexComponent", button);
 		assertTrue(ret);
 
 		SwtUtilities.disposeWidget(shell);
 
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
 
 	private class TestView extends SubModuleView<SubModuleController> {
 
 		@Override
 		public void bind(SubModuleNode node) {
 			nodesBoundToView.add(node);
 		}
 
 		@Override
 		public SubModuleNode getNavigationNode() {
 			return node;
 		}
 
 		@Override
 		protected void basicCreatePartControl(Composite parent) {
 		}
 	}
 
 	private static class TestComplexComponent extends Composite implements IComplexComponent {
 
 		public TestComplexComponent(Composite parent, int style) {
 			super(parent, style);
 		}
 
 		public List<Object> getUIControls() {
 			return null;
 		}
 
 	}
 
 }
