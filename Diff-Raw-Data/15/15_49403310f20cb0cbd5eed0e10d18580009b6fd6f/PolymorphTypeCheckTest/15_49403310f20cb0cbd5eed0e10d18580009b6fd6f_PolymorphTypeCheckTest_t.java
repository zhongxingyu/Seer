 package de.age.util.duck;
 
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

 import org.junit.Test;
 
 public class PolymorphTypeCheckTest {
 
 	private static interface SimpleDuckIF {
 		Number quack(String quackedMessage);
 	}
 	
 	private static class ObjectDuck {
 		@SuppressWarnings("unused")
		public Number quack(Object quackedObject) {return null;};
 	}
 	
 	private static class NumberDuck {
 		@SuppressWarnings("unused")
		public Integer quack(String quackedMessage) {return null;};
 	}
 	
 	@Test
 	public void moreGeneralArgumentTypeIsAllowed() {
		assertThat(Ducktype.check(SimpleDuckIF.class, new ObjectDuck()), is(true));
 	}
 	
 	@Test
 	public void moreSpecificReturnTypeIsAllowed() {
		assertThat(Ducktype.check(SimpleDuckIF.class, new NumberDuck()), is(true));
 	}
 
 }
