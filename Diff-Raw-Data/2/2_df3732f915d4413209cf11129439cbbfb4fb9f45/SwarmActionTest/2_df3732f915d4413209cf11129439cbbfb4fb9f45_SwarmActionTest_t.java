 /*
  * $Id: WaspActionTest.java,v 1.4 2006/10/26 12:31:52 Marrink Exp $
  * $Revision: 1.4 $
  * $Date: 2006/10/26 12:31:52 $
  *
  * ====================================================================
  * Copyright (c) 2005, Topicus B.V.
  * All rights reserved.
  */
 package org.apache.wicket.security.swarm.actions;
 
 import junit.framework.TestCase;
 
 import org.apache.wicket.security.actions.Access;
 import org.apache.wicket.security.actions.ActionFactory;
 import org.apache.wicket.security.actions.Enable;
 import org.apache.wicket.security.actions.Inherit;
 import org.apache.wicket.security.actions.Render;
 import org.apache.wicket.security.actions.WaspAction;
 import org.apache.wicket.security.hive.HiveMind;
 import org.apache.wicket.security.hive.config.PolicyFileHiveFactory;
 import org.apache.wicket.security.pages.MockHomePage;
 import org.apache.wicket.security.pages.MockLoginPage;
 import org.apache.wicket.security.swarm.SwarmWebApplication;
 import org.apache.wicket.security.swarm.actions.SwarmAction;
 import org.apache.wicket.util.tester.WicketTester;
 
 /**
  * Tests WaspAction class.
  * @author marrink
  */
 public class SwarmActionTest extends TestCase
 {
 	private SwarmWebApplication application;
 
 	public WicketTester mock;
 
 	/**
 	 * Constructor for WaspActionTest.
 	 * @param arg0
 	 */
 	public SwarmActionTest(String arg0)
 	{
 		super(arg0);
 	}
 
 	/**
 	 * Test method for {@link SwarmAction#hashCode()}
 	 */
 	public void testHashCode()
 	{
 		WaspAction action = new SwarmAction(5, "test");
 		// persistent
 		assertEquals(action.hashCode(), action.hashCode());
 		WaspAction action2 = new SwarmAction(5, "test");
 		// equal
 		assertEquals(action.hashCode(), action2.hashCode());
 		WaspAction action3 = new TestAction(5, "does not matter");
 		assertEquals(action2, action3);
 		assertTrue(action3.hashCode() == action2.hashCode());
 
 	}
 
 	/**
 	 * Test method for {@link SwarmAction#implies(int)}
 	 */
 	public void testImpliesInt()
 	{
 		SwarmAction action = new SwarmAction(5, "test");
 		assertTrue(action.implies(5));
 		assertTrue(action.implies(1));
 		assertFalse(action.implies(6));
 
 	}
 
 	/**
 	 * Test method for {@link SwarmAction#implies(WaspAction)}
 	 */
 	public void testImpliesWaspAction()
 	{
 		ActionFactory factory = application.getActionFactory();
 		WaspAction action = factory.getAction(Render.class);
 		WaspAction action2 = action.add(factory.getAction(Inherit.class));
 		assertTrue(action2.implies(action));
 		assertFalse(action.implies(action2));
 	}
 	/**
 	 * 
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	protected void setUp()
 	{
 		mock = new WicketTester(application = new SwarmWebApplication()
 		{
 
 			protected Object getHiveKey()
 			{
 				return "action test";
 			}
 
 			protected void setUpHive()
 			{
 				PolicyFileHiveFactory factory = new PolicyFileHiveFactory();
 				// don't need policy for this simple test
 				HiveMind.registerHive(getHiveKey(), factory);
 			}
 
 			public Class getHomePage()
 			{
 				return MockHomePage.class;
 			}
 
 			public Class getLoginPage()
 			{
 				return MockLoginPage.class;
 			}
		}, "src/test/java/" + getClass().getPackage().getName().replace('.', '/'));
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	protected void tearDown() throws Exception
 	{
 		mock.destroy();
 		mock = null;
 		application = null;
 		HiveMind.unregisterHive("action test");
 	}
 
 	/**
 	 * Test method for {@link SwarmAction#equals(Object)}
 	 */
 	public void testEqualsObject()
 	{
 		WaspAction action = new SwarmAction(5, "test");
 		// null
 		assertFalse(action.equals(null));
 		// reflexive
 		assertEquals(action, action);
 		// symmetric
 		WaspAction action2 = new SwarmAction(5, "test");
 		assertEquals(action, action2);
 		assertEquals(action2, action);
 		WaspAction action3 = new SwarmAction(6, "test2");
 		assertFalse(action.equals(action3));
 		assertFalse(action3.equals(action));
 		// transitive
 		WaspAction action4 = new TestAction(5, "test");
 		assertEquals(action, action4);
 		assertEquals(action4, action2);
 		// action2 already equals action
 		// consistent
 		// action is inmutable so it is consistent
 	}
 
 	/**
 	 * Test method for {@link SwarmAction#add(int)}
 	 */
 	public void testAddInt()
 	{
 		 ActionFactory factory=application.getActionFactory();
 		 SwarmAction action=(SwarmAction)factory.getAction(Render.class);
 		 assertEquals(2,action.actions());
 		 SwarmAction action2=(SwarmAction)action.add(((SwarmAction)factory.getAction(Access.class)).actions());
 		 assertEquals(2,action.actions()); //check inmutability
 		 assertEquals(2,action2.actions());
 		 assertNotSame(action, action2);
 		 SwarmAction action3=(SwarmAction)action.add(((SwarmAction)factory.getAction(Enable.class)).actions());
 		 assertEquals(2,action.actions());
 		 assertEquals(6,action3.actions());
 
 	}
 
 	/**
 	 * Test method for {@link SwarmAction#add(WaspAction)}
 	 */
 	public void testAddWaspAction()
 	{
 		ActionFactory factory=application.getActionFactory();
 		 SwarmAction action=(SwarmAction)factory.getAction(Render.class);
 		 assertEquals(2,action.actions());
 		 SwarmAction action2=(SwarmAction)action.add(factory.getAction(Access.class));
 		 assertEquals(2,action.actions()); //check inmutability
 		 assertEquals(2,action2.actions());
 		 assertNotSame(action, action2);
 		 SwarmAction action3=(SwarmAction)action.add(factory.getAction(Enable.class));
 		 assertEquals(2,action.actions());
 		 assertEquals(6,action3.actions());
 
 	}
 
 	public void testConstructor()
 	{
 		try
 		{
 			new SwarmAction(6, null);
 			fail("description should be required");
 		}
 		catch (IllegalArgumentException e)
 		{
 			// noop
 		}
 		try
 		{
 			new SwarmAction(-10, "foobar");
 			fail("negative numbers should not be allowed");
 		}
 		catch (IllegalArgumentException e)
 		{
 			// noop
 		}
 	}
 
 	private static class TestAction extends SwarmAction
 	{
 		private static final long serialVersionUID = 1L;
 
 		protected TestAction(int action, String name)
 		{
 			super(action, name);
 		}
 
 	}
 }
