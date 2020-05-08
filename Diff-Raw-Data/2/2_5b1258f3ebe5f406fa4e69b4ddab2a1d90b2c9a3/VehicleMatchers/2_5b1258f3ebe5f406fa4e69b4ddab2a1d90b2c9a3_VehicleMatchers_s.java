 package traffic;
 
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 
 
 public class VehicleMatchers {
 	public static Matcher<Vehicle> isLocatedAt(final Cell expectedLocation) {
 		return new TypeSafeMatcher<Vehicle>(Vehicle.class) {
 			@Override
 			public void describeTo(final Description description) {
				description.appendText("road user is located at junction ").appendValue(expectedLocation);
 			}
 
 			@Override
 			protected boolean matchesSafely(final Vehicle item) {
 				return item.location().equals(expectedLocation);
 			}
 		};
 	}
 }
