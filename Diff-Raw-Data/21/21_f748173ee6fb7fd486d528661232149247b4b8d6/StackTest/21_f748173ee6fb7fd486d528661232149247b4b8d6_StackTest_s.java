 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class StackTest {
 
     private Stack stack;
 
     @Before
     public void setUp() {
         stack = new BoundedStack(10);
     }
 
     @Test
     public void newlyCreatedStack_shouldBeEmpty() {
         assertThat(stack.isEmpty(), is(true));
         assertThat(stack.getSize(), is(0));
     }
 
     @Test
     public void afterOnePush_StackSizeShouldBeOne() {
         stack.push(1);
         assertThat(stack.getSize(), is(1));
         assertThat(stack.isEmpty(), is(false));
     }
 
     @Test
     public void afterOnePushAndOnePop_shouldBeEmpty() {
         stack.push(1);
         stack.pop();
         assertThat(stack.isEmpty(), is(true));
     }
 
     @Test(expected = BoundedStack.Overflow.class)
     public void whenPushedOverCapacity_StackOverflows() {
         stack = BoundedStack.make(1);
         stack.push(1);
         stack.push(1);
     }
 
     @Test(expected = BoundedStack.IllegalCapacity.class)
     public void whenCreatingStackWithNegativeSize_shouldThrowIllegalCapacity() {
         BoundedStack.make(-1);
     }
 
     @Test(expected = BoundedStack.Underflow.class)
     public void whenEmptyStackIsPopped_StackUnderflows() {
         stack.pop();
     }
 
     @Test
     public void whenOneIsPushed_OneIsPopped() {
         stack.push(1);
         assertThat(stack.pop(), is(1));
     }
 
     @Test
     public void whenOneAndTwoArePushed_TwoAndOneArePopped() {
         stack.push(1);
         stack.push(2);
         assertThat(stack.pop(), is(2));
         assertThat(stack.pop(), is(1));
     }
 
     @Test
     public void
     whenOneIsPushed_OneIsOnTop() {
         stack.push(1);
         assertThat(stack.top(), is(1));
     }
 }
