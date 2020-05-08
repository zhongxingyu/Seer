 package welch.brainmess;
 
 
 import org.junit.Test;
 import org.jmock.*;
 
 public class InstructionTests {
 
 	@Test
 	public void moveForward_Execute() {
 
 		// Arrange
 		Mockery mock = new Mockery();
 		final IExecutionContext context = mock.mock(IExecutionContext.class);
 		
 		
 		mock.checking(new Expectations() {{
 			oneOf(context).moveForward();
 		}});
 		
 		
 		// Act
 		Instruction.MoveForward.execute(context);
 		
 		// Assert
 		mock.assertIsSatisfied();
 	}
 
 
 	@Test
 	public void moveBackward_Execute() {
 
 		// Arrange
 		Mockery mock = new Mockery();
 		final IExecutionContext context = mock.mock(IExecutionContext.class);
 		
 		
 		mock.checking(new Expectations() {{
 			oneOf(context).moveBackward();
 		}});
 		
 		
 		// Act
 		Instruction.MoveBackward.execute(context);
 		
 		// Assert
 		mock.assertIsSatisfied();
 	}
 	
 	@Test
	public void testAndJumpForward_Execute() {
 		// Arrange
 		Mockery mock = new Mockery();
 		final IExecutionContext context = mock.mock(IExecutionContext.class);
 		
 		mock.checking(new Expectations() {{
 			oneOf(context).testAndJumpForward();
 		}});
 		
 		// Act
 		Instruction.TestAndJumpForward.execute(context);
 		
 		// Assert
 		mock.assertIsSatisfied();
 	}
 }
