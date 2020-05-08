 package org.eclipse.dltk.debug.tests.breakpoints;
 
 import junit.framework.TestSuite;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
 import org.eclipse.dltk.debug.core.model.IScriptLineBreakpoint;
 import org.eclipse.dltk.debug.tests.AbstractDebugTests;
 import org.eclipse.dltk.internal.debug.core.model.ScriptLineBreakpoint;
 
 public class BreakpointTests extends AbstractDebugTests {
 	public static TestSuite suite() {
 		return new Suite(BreakpointTests.class);
 	}
 
 	public BreakpointTests() {
 		super("org.eclipse.dltk.debug.tests", "My Breakpoint tests");
 	}
 
 	private IScriptLineBreakpoint breakpoint;
 
 	public void setUpSuite() throws Exception {
 		super.setUpSuite();
 
 		IResource resource = scriptProject.getProject().findMember(
 				"src/test.rb");
 
		breakpoint = new ScriptLineBreakpoint("test_debug_model", resource, 1,
 				-1, -1, true);
 	}
 
 	public void tearDownSuite() throws Exception {
 		super.tearDownSuite();
 	}
 
 	// Helper methods
 	protected String getProjectName() {
 		return "debug";
 	}
 
 	// Real tests
 	public void testSetGet() throws Exception {
 		// Id
 		final String id = "32145";
 		breakpoint.setIdentifier(id);
 		assertEquals(id, breakpoint.getIdentifier());
 
 		// HitCount
 		final int hitCount = 234;
 		assertEquals(-1, breakpoint.getHitCount());
 		breakpoint.setHitCount(hitCount);
 		assertEquals(hitCount, breakpoint.getHitCount());
 
 		// Expression state
 		assertEquals(false, breakpoint.getExpressionState());
 		breakpoint.setExpressionState(true);
 		assertEquals(true, breakpoint.getExpressionState());
 
 		// Expression
 		final String expression = "x + y > 3245";
 		assertNull(breakpoint.getExpression());
 		breakpoint.setExpression(expression);
 		assertEquals(expression, breakpoint.getExpression());
 
 		// Hit condition
 		final int hitCondition = IScriptBreakpoint.HIT_CONDITION_EQUAL;
 		assertEquals(-1, breakpoint.getHitCondition());
 		breakpoint.setHitCondition(hitCondition);
 		assertEquals(hitCondition, breakpoint.getHitCondition());
 
 		// Hit value
 		final int hitValue = 22;
 		assertEquals(-1, breakpoint.getHitValue());
 		breakpoint.setHitValue(hitValue);
 		assertEquals(hitValue, breakpoint.getHitValue());
 	}
 }
