 package rover;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 @RunWith(JUnit4.class)
 public class PositionTest {
 	@Test
 	public void createPosition() {
 		Position p = new Position(2, 3);
 		assertThat(p.x, equalTo(2));
 		assertThat(p.y, equalTo(3));
 	}
 
 	@Test
 	public void translatePosition() {
 		Position p = new Position(2, 3);
 		Position rel = new Position(1, 2);
 		Position result = p.translate(rel);
 		assertThat(result.x, equalTo(3));
 		assertThat(result.y, equalTo(5));
 	}
 
 	@Test
 	public void negativePosition() {
 		Position p = new Position(2, 3);
 		Position n = new Position(-2, -3);
 
 		assertThat(p.negative(), equalTo(n));
 	}
 
 	@Test
 	public void testEquality() {
 		Position p = new Position(0, 0);
 		Position equalp = new Position(0, 0);
 		Position otherp = new Position(1, 2);
 
 		assertThat(p, not(equalTo(null)));
 		assertThat(p, not(equalTo(new Object())));
 		assertThat(p, not(equalTo(otherp)));
 		assertThat(p, equalTo(p));
 		assertThat(p, equalTo(equalp));
 	}

	@Test
	public void testToString() {
		Position p = new Position(3, 5);
		assertThat(
				p.toString(),
				allOf(containsString("Position"), containsString("3"),
						containsString("5")));
	}
 }
