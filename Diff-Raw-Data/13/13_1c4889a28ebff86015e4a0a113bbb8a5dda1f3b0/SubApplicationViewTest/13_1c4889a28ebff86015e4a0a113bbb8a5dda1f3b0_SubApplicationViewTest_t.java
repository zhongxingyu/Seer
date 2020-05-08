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
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import junit.framework.TestCase;
 
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IPageLayout;
 
 import org.eclipse.riena.core.test.collect.UITestCase;
 import org.eclipse.riena.core.util.IConfigurationExtension;
 import org.eclipse.riena.core.util.InvocationTargetFailure;
 import org.eclipse.riena.core.util.Nop;
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.core.util.RienaConfiguration;
 import org.eclipse.riena.navigation.ISubApplicationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.listener.ModuleGroupNodeListener;
 import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubApplicationController;
 import org.eclipse.riena.navigation.ui.swt.presentation.SwtViewId;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Tests of the class {@link SubApplicationView}.
  */
 @UITestCase
 public class SubApplicationViewTest extends TestCase {
 
 	private Shell shell;
 	private TestSubApplicationView view;
 	private SubApplicationNode node;
 
 	@Override
 	protected void setUp() throws Exception {
 		view = new TestSubApplicationView();
 		shell = new Shell();
 		final SWTBindingPropertyLocator locator = SWTBindingPropertyLocator.getInstance();
 		locator.setBindingProperty(shell, ApplicationViewAdvisor.SHELL_RIDGET_PROPERTY);
 
 		node = new SubApplicationNode();
 		node.setNavigationProcessor(new NavigationProcessor());
 		view.bind(node);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		view = null;
 		SwtUtilities.dispose(shell);
 	}
 
 	public void testUnbind() throws Exception {
 
 		final List<ModuleGroupNodeListener> listeners = ReflectionUtils.getHidden(node, "listeners"); //$NON-NLS-1$
 
 		assertEquals(1, listeners.size());
 
 		node.dispose();
 
 		assertTrue(listeners.isEmpty());
 	}
 
 	public void testDisposeHandling() throws Exception {
 		node.setNavigationNodeController(new SubApplicationController(node));
 		final ModuleGroupNode mg = new ModuleGroupNode();
 		node.addChild(mg);
 		final ModuleNode m = new ModuleNode();
 		mg.addChild(m);
 		view.createInitialLayout(null);
 		final SubModuleNode sm1 = new SubModuleNode(new NavigationNodeId("x")); //$NON-NLS-1$
 		m.addChild(sm1);
 		final SubModuleNode sm2 = new SubModuleNode(new NavigationNodeId("y")); //$NON-NLS-1$
 		m.addChild(sm2);
 		final SubModuleNode sm3 = new SubModuleNode(new NavigationNodeId("z")); //$NON-NLS-1$
 		m.addChild(sm3);
 		view.viewUserCount = 1;
 		view.providedId = new SwtViewId("test:test"); //$NON-NLS-1$
 		view.hiddenId = null;
 		sm1.dispose();
 		assertSame(view.providedId, view.hiddenId);
 
 		view.viewUserCount = 0;
 		view.providedId = new SwtViewId("test:SHARED"); //$NON-NLS-1$
 		view.hiddenId = null;
 		sm2.dispose();
 		assertSame(view.providedId, view.hiddenId);
 
 		view.viewUserCount = 1;
 		view.providedId = new SwtViewId("test:SHARED"); //$NON-NLS-1$
 		view.hiddenId = null;
 		sm2.dispose();
 		assertSame(null, view.hiddenId);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code addToStock(SwtViewId)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testAddToStock() throws Exception {
 
 		final LinkedList<SwtViewId> stock = ReflectionUtils.getHidden(view, "subModuleViewStock"); //$NON-NLS-1$
 		assertNotNull(stock);
 		assertTrue(stock.isEmpty());
 
 		final SwtViewId id1 = new SwtViewId("sub1", "0001"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id1); //$NON-NLS-1$
 		assertEquals(1, stock.size());
 
 		final SwtViewId id2 = new SwtViewId("sub2", "0001"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id2); //$NON-NLS-1$
 		assertEquals(2, stock.size());
 		assertSame(id1, stock.getLast());
 		assertSame(id2, stock.getFirst());
 
 		final SwtViewId id12 = new SwtViewId("sub1", "0002"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id12); //$NON-NLS-1$
 		assertEquals(3, stock.size());
 		assertSame(id1, stock.getLast());
 		assertSame(id12, stock.getFirst());
 
 		ReflectionUtils.invokeHidden(view, "addToStock", id1); //$NON-NLS-1$
 		assertEquals(3, stock.size());
 		assertSame(id2, stock.getLast());
 		assertSame(id1, stock.getFirst());
 
 		final SwtViewId id21 = new SwtViewId("sub2", "0001"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id21); //$NON-NLS-1$
 		assertEquals(3, stock.size());
 		assertEquals(id12, stock.getLast());
 		assertSame(id21, stock.getFirst());
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getFirstOfStock()}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetFirstOfStock() throws Exception {
 
 		final LinkedList<SwtViewId> stock = ReflectionUtils.getHidden(view, "subModuleViewStock"); //$NON-NLS-1$
 		assertNotNull(stock);
 		assertTrue(stock.isEmpty());
 
 		SwtViewId retId;
 		try {
 			retId = ReflectionUtils.invokeHidden(view, "getFirstOfStock"); //$NON-NLS-1$
 			fail("Excpected exception wasn't thrown!"); //$NON-NLS-1$
 		} catch (final InvocationTargetFailure e) {
 			if (e.getCause() instanceof NoSuchElementException) {
 				Nop.reason("Excpected exception"); //$NON-NLS-1$
 			} else {
 				throw e;
 			}
 		}
 
 		final SwtViewId id1 = new SwtViewId("sub1", "0001"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id1); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getFirstOfStock"); //$NON-NLS-1$
 		assertEquals(id1, retId);
 
 		final SwtViewId id2 = new SwtViewId("sub2", "0002"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id2); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getFirstOfStock"); //$NON-NLS-1$
 		assertEquals(id2, retId);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getMaxStockedViews()}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetMaxStockedViews() throws Exception {
 
 		int ret = ReflectionUtils.invokeHidden(view, "getMaxStockedViews"); //$NON-NLS-1$
 		assertEquals(0, ret);
 
 		final IConfigurationExtension extension = new IConfigurationExtension() {
 
 			@Override
 			public String getKey() {
 				return RienaConfiguration.MAX_STOCKED_VIEWS_KEY;
 			}
 
 			@Override
 			public String getValue() {
 				return "4711"; //$NON-NLS-1$
 			}
 		};
 		final IConfigurationExtension[] extensions = new IConfigurationExtension[] { extension };
 
 		RienaConfiguration.getInstance().update(extensions);
 		ret = ReflectionUtils.invokeHidden(view, "getMaxStockedViews"); //$NON-NLS-1$
 		assertEquals(4711, ret);
 
		RienaConfiguration.getInstance().update(null);

 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code removeFromStock(SwtViewId)}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testRemoveFromStock() throws Exception {
 
 		final LinkedList<SwtViewId> stock = ReflectionUtils.getHidden(view, "subModuleViewStock"); //$NON-NLS-1$
 		assertNotNull(stock);
 		assertTrue(stock.isEmpty());
 
 		final SwtViewId id1 = new SwtViewId("sub1", "0001"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id1); //$NON-NLS-1$
 		final SwtViewId id2 = new SwtViewId("sub2", "0002"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id2); //$NON-NLS-1$
 
 		assertEquals(2, stock.size());
 		assertSame(id1, stock.getLast());
 		assertSame(id2, stock.getFirst());
 
 		boolean ret = ReflectionUtils.invokeHidden(view, "removeFromStock", id2); //$NON-NLS-1$
 		assertTrue(ret);
 		assertEquals(1, stock.size());
 		assertSame(id1, stock.getLast());
 		assertSame(id1, stock.getFirst());
 
 		ret = ReflectionUtils.invokeHidden(view, "removeFromStock", id2); //$NON-NLS-1$
 		assertFalse(ret);
 		assertEquals(1, stock.size());
 		assertSame(id1, stock.getLast());
 		assertSame(id1, stock.getFirst());
 
 		ret = ReflectionUtils.invokeHidden(view, "removeFromStock", id1); //$NON-NLS-1$
 		assertTrue(ret);
 		assertEquals(0, stock.size());
 
 		ret = ReflectionUtils.invokeHidden(view, "removeFromStock", id1); //$NON-NLS-1$
 		assertFalse(ret);
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code getLastOfStock()}.
 	 * 
 	 * @throws Exception
 	 *             handled by JUnit
 	 */
 	public void testGetLastOfStock() throws Exception {
 
 		SwtViewId retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertNull(retId);
 
 		final IConfigurationExtension extension = new IConfigurationExtension() {
 
 			@Override
 			public String getKey() {
 				return RienaConfiguration.MAX_STOCKED_VIEWS_KEY;
 			}
 
 			@Override
 			public String getValue() {
 				return "3"; //$NON-NLS-1$
 			}
 		};
 		final IConfigurationExtension[] extensions = new IConfigurationExtension[] { extension };
 
 		RienaConfiguration.getInstance().update(extensions);
 
 		final SwtViewId id1 = new SwtViewId("sub1", "0001"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id1); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertNull(retId);
 
 		final SwtViewId id2 = new SwtViewId("sub2", "0002"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id2); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertNull(retId);
 
 		final SwtViewId id3 = new SwtViewId("sub3", "0003"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id3); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertNull(retId);
 
 		final SwtViewId id4 = new SwtViewId("sub4", "0004"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id4); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertEquals(id1, retId);
 
 		final SwtViewId id5 = new SwtViewId("sub5", "0005"); //$NON-NLS-1$ //$NON-NLS-2$
 		ReflectionUtils.invokeHidden(view, "addToStock", id5); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertEquals(id1, retId);
 
 		ReflectionUtils.invokeHidden(view, "addToStock", id1); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertEquals(id2, retId);
 
 		ReflectionUtils.invokeHidden(view, "removeFromStock", id2); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertEquals(id3, retId);
 
 		ReflectionUtils.invokeHidden(view, "removeFromStock", id1); //$NON-NLS-1$
 		retId = ReflectionUtils.invokeHidden(view, "getLastOfStock"); //$NON-NLS-1$
 		assertNull(retId);
 
		RienaConfiguration.getInstance().update(null);

 	}
 
 	private class TestSubApplicationView extends SubApplicationView {
 
 		private int viewUserCount = 0;
 		private SwtViewId hiddenId = null;
 		private SwtViewId providedId = null;
 
 		@Override
 		public SubApplicationNode getNavigationNode() {
 			return node;
 		}
 
 		@Override
 		protected int getViewUserCount(final SwtViewId id) {
 			return viewUserCount;
 		}
 
 		@Override
 		protected boolean hideView(final SwtViewId id) {
 			hiddenId = id;
 			return true;
 		}
 
 		@Override
 		protected void doBaseLayout(final IPageLayout layout) {
 		}
 
 		@Override
 		protected ISubApplicationNode locateSubApplication(final IPageLayout layout) {
 			return node;
 		}
 
 		@Override
 		protected SwtViewId getViewId(final ISubModuleNode node) {
 			return providedId;
 		}
 
 	}
 
 }
