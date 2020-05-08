 package nl.jqno.equalsverifier.talk;
 
 import nl.jqno.equalsverifier.EqualsVerifier;
 import nl.jqno.equalsverifier.talk.S04_intermezzo.Color;
 import nl.jqno.equalsverifier.talk.helper.NonNull;
 
 import org.junit.Test;
 
 public class S07_an_unsatisfying_solution {
 	
 	
 	/*
 	 * "There is no way to extend an instantiable class and add a value component 
 	 * while preserving the equals contract"
 	 * -- Joshua Bloch, Effective Java
 	 */
 	
 	
 	/*
 	 * :( :( :(
 	 * 
 	 * 
 	 * OK... I give up.
 	 */
 	
 
 	
 	
 	
 	public class Point {
 		private final int x;
 		private final int y;
 		
 		public Point(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@Override
 		public int hashCode() {
 			return 31 * (31 + x) + y;
 		}
 
 		@Override
 		public boolean equals(Object obj) {
 			// Use getClass instead of instanceof
 			if (obj == null || getClass() != obj.getClass()) {
 				return false;
 			}
 			Point other = (Point)obj;
 			return x == other.x && y == other.y;
 		}
	}	
 	
 	
 	
 	
 	
 	
 	/*
 	 * This is the previously asymmetric ColorPoint.
 	 */
 	public class ColorPoint extends Point {
 		@NonNull private final Color color;
 		
 		public ColorPoint(int x, int y, Color color) {
 			super(x, y);
 			this.color = color;
 		}
 		
 		@Override
 		public int hashCode() {
 			return 31 * (31 + super.hashCode()) + color.hashCode();
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			if (obj == null || getClass() != obj.getClass()) {
 				return false;
 			}
 			ColorPoint other = (ColorPoint)obj;
 			return color.equals(other.color) && super.equals(obj);
 		}
 	}	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	@Test
 	public void equalsverifier_on_point() {
 		EqualsVerifier.forClass(Point.class)
 				.usingGetClass()
 				.verify();
 	}
 
 	@Test
 	public void equalsverifier_on_color_point() {
 		EqualsVerifier.forClass(ColorPoint.class)
 				.usingGetClass()
 				.verify();
 	}
 
 	/*
 	 * But... what's that "usingGetClass" thingy doing there?
 	 * 
 	 * It says we're breaking the Liskov Substitution Principle.
 	 */
 	
 	
 	
 	
 	
 	
 }
