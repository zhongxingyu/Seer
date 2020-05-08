 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.client.controller.test;
 
 import static org.easymock.EasyMock.*;
 
 import java.util.Comparator;
 
 import org.easymock.LogicalOperator;
 
 import org.eclipse.riena.beans.common.Person;
 import org.eclipse.riena.example.client.controllers.NavigateSubModuleController;
 import org.eclipse.riena.internal.core.test.collect.NonUITestCase;
 import org.eclipse.riena.internal.example.client.beans.PersonModificationBean;
 import org.eclipse.riena.navigation.INavigationNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.NodePositioner;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.swt.controllers.AbstractSubModuleControllerTest;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 
 /**
  * Tests for the NavigateSubModuleController.
  */
 @SuppressWarnings({ "restriction", "unchecked" })
 @NonUITestCase
 public class NavigateSubModuleControllerTest extends AbstractSubModuleControllerTest<NavigateSubModuleController> {
 
 	@Override
 	protected NavigateSubModuleController createController(final ISubModuleNode node) {
 		final NavigateSubModuleController newInst = new NavigateSubModuleController();
 		node.setNodeId(new NavigationNodeId("org.eclipse.riena.example.navigate"));
 		newInst.setNavigationNode(node);
 		return newInst;
 	}
 
 	public void testNavigateCombo() {
 
 		expect(
 				getMockNavigationProcessor().navigate(eq(getController().getNavigationNode()),
 						eq(new NavigationNodeId("org.eclipse.riena.example.navigate.comboAndList")),
 						(NavigationArgument) notNull())).andReturn(
 				createNavigationNode("org.eclipse.riena.example.navigate.comboAndList"));
 
 		replay(getMockNavigationProcessor());
 
 		final IActionRidget navigateToComboButton = getController().getRidget(IActionRidget.class, "comboAndList");
 		navigateToComboButton.fireAction();
 
 		verify(getMockNavigationProcessor());
 	}
 
 	/**
 	 * Tests whether the method <code>INavigationProcessor#navigate</code> is
 	 * called with the proper parameters: <br>
 	 * - a NavigationNode<br>
 	 * - a NavigationNodeId and<br>
 	 * - a <code>NavigationArgument</code> that has the ridgetId "textFirst" and
 	 * a parameter that is compared by a custom compare-method. This
 	 * compare-method returns 0, if the first- and lastName of the
 	 * <code>PersonModificationBean</code> match.
 	 */
 	public void testNavigateToRidgetWithCompare() {
 		final PersonModificationBean bean = new PersonModificationBean();
 		bean.setPerson(new Person("Doe", "Jane"));
 
 		expect(
 				getMockNavigationProcessor().navigate(eq(getController().getNavigationNode()),
 						eq(new NavigationNodeId("org.eclipse.riena.example.combo")),
 						cmp(new NavigationArgument(bean, "textFirst"), new Comparator<NavigationArgument>() {
 
 							public int compare(final NavigationArgument o1, final NavigationArgument o2) {
 								if (o1.getParameter() instanceof PersonModificationBean
 										&& o2.getParameter() instanceof PersonModificationBean) {
 									return comparePersonModificationBeans((PersonModificationBean) o1.getParameter(),
 											(PersonModificationBean) o2.getParameter());
 								} else {
 									return -1;
 								}
 							}
 
 						}, LogicalOperator.EQUAL))).andReturn(createNavigationNode("org.eclipse.riena.example.combo"));
 
 		replay(getMockNavigationProcessor());
 		final IActionRidget navigateToNavigateRidget = getController().getRidget(IActionRidget.class,
 				"btnNavigateToRidget");
 		navigateToNavigateRidget.fireAction();
 		verify(getMockNavigationProcessor());
 	}
 
 	/**
 	 * Tests whether the method <code>INavigationProcessor#navigate</code> is
 	 * called with the proper parameters: <br>
 	 * - a NavigationNode<br>
 	 * - a NavigationNodeId and<br>
 	 * - a <code>NavigationArgument</code> that has the ridgetId "textFirst" and
 	 * a parameter that is not null
 	 */
 	public void testNavigateToRidgetWithNotNull() {
 		expect(
 				getMockNavigationProcessor().navigate(eq(getController().getNavigationNode()),
 						eq(new NavigationNodeId("org.eclipse.riena.example.combo")),
 						new NavigationArgument(notNull(), "textFirst"))).andReturn(
 				createNavigationNode("org.eclipse.riena.example.combo"));
 
 		replay(getMockNavigationProcessor());
 		final IActionRidget navigateToNavigateRidget = getController().getRidget(IActionRidget.class,
 				"btnNavigateToRidget");
 		navigateToNavigateRidget.fireAction();
 		verify(getMockNavigationProcessor());
 	}
 
 	/**
 	 * Tests whether the method <code>INavigationProcessor#navigate</code> is
 	 * called with the proper parameters: <br>
 	 * - a NavigationNode<br>
 	 * - a NavigationNodeId and<br>
 	 * - a <code>NavigationArgument</code> that has the ridgetId "textFirst" and
 	 * a parameter that compared by the equals methods in the specific classes.
 	 */
 	public void testNavigateToRidgetWithEquals() {
 		final PersonModificationBean bean = new PersonModificationBean();
 		bean.setPerson(new Person("Doe", "Jane"));
 
 		expect(
 				getMockNavigationProcessor().navigate(eq(getController().getNavigationNode()),
 						eq(new NavigationNodeId("org.eclipse.riena.example.combo")),
 						eq(new NavigationArgument(bean, "textFirst")))).andReturn(
 				createNavigationNode("org.eclipse.riena.example.combo"));
 
 		replay(getMockNavigationProcessor());
 		final IActionRidget navigateToNavigateRidget = getController().getRidget(IActionRidget.class,
 				"btnNavigateToRidget");
 		navigateToNavigateRidget.fireAction();
 		verify(getMockNavigationProcessor());
 	}
 
	public void testNavigateFirstModule() {
 		final NavigationArgument naviAgr = new NavigationArgument();
 		naviAgr.setNodePositioner(NodePositioner.ADD_BEGINNING);
 		expect(
 				getMockNavigationProcessor().navigate(eq(getController().getNavigationNode()),
						eq(new NavigationNodeId("org.eclipse.riena.example.navigate.firstmodule")), eq(naviAgr)))
				.andReturn(createNavigationNode("org.eclipse.riena.example.navigate.firstmodule"));
 
 		replay(getMockNavigationProcessor());
		final IActionRidget navigateFirstModule = getController().getRidget(IActionRidget.class,
				"openAsFirstModule");
		navigateFirstModule.fireAction();
 		verify(getMockNavigationProcessor());
 	}
 
 	private int comparePersonModificationBeans(final PersonModificationBean p1, final PersonModificationBean p2) {
 		if (p1.getFirstName().equals(p2.getFirstName()) && p1.getLastName().equals(p2.getLastName())) {
 			return 0;
 		} else {
 			return -1;
 		}
 	}
 
 	@SuppressWarnings("rawtypes")
 	private INavigationNode createNavigationNode(final String id) {
 		return new SubModuleNode(new NavigationNodeId(id));
 	}
 }
