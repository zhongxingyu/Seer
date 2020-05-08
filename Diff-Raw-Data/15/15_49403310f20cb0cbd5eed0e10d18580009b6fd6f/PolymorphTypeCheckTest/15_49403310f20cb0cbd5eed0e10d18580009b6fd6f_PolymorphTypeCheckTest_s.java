 package de.age.util.duck;
 
 import org.junit.Test;
 
 public class PolymorphTypeCheckTest {
 
 	private static interface SimpleDuckIF {
 		Number quack(String quackedMessage);
 	}
 	
 	private static class ObjectDuck {
 		@SuppressWarnings("unused")
		Number quack(Object quackedObject) {return null;};
 	}
 	
 	private static class NumberDuck {
 		@SuppressWarnings("unused")
		Integer quack(String quackedMessage) {return null;};
 	}
 	
 	@Test
 	public void moreGeneralArgumentTypeIsAllowed() {
		Ducktype.check(SimpleDuckIF.class, new ObjectDuck());
 	}
 	
 	@Test
 	public void moreSpecificReturnTypeIsAllowed() {
		Ducktype.check(SimpleDuckIF.class, new NumberDuck());
 	}
 
 }
