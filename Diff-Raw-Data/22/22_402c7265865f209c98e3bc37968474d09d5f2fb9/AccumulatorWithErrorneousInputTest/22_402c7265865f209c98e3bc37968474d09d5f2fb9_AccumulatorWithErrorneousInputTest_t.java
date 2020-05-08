 package gemcfadyen.accumulator;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AccumulatorWithErrorneousInputTest {
 
 	private Accumulator accumulator;
 
 	@Before
 	public void setupAccumulator() {
 		accumulator = new Accumulator();
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenNoNumbersBetweenCommaAndNewLineDelimiter()
 			throws InvalidValueException {
 		accumulator.add("1,\n");
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenNoNumbersBetweenTwoCommas()
 			throws InvalidValueException {
 		accumulator.add("1,,");
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenNoNumbersBetweenTwoNewLineDelimeters()
 			throws InvalidValueException {
 		accumulator.add("1\n\n");
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenOnlyNewLineDelimiterIsPresentInInput()
 			throws InvalidValueException {
 		accumulator.add("\n");
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenOnlyCommaDelimiterIsPresentInInput()
 			throws InvalidValueException {
 		accumulator.add(",");
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenTherAreSeveralDelimitersWithNoNumbersInBetweenThem()
 			throws InvalidValueException {
 		accumulator.add("1,,,\n,\n\n\n\n\n,,\n\n,,,,3");
 	}
 
 	@Test(expected = InvalidValueException.class)
 	public void shouldThrowExceptionWhenANegativeValueIsInput()
 			throws InvalidValueException {
 		accumulator.add("1,2,-3");
 	}
 
 	public void shouldThrowExceptionWhenSeveralNegativeValuesAreInput() throws InvalidValueException {
 		String expectedErrorMessage = "negatives not allowed [-1] [-3] ";
 		String actualErrorMessage = "";
 		try {
 			accumulator.add("//.\n-1.2.-3");
 		} catch (InvalidValueException exception) {
 			actualErrorMessage = exception.getMessage();
 		}
 		assertThat(actualErrorMessage, is(expectedErrorMessage));
 	}
	
	public void shouldThrowExceptionWhenSeveralNegativeValuesAreInputUsingMulipleDelimitersOfSingleleLength() throws InvalidValueException {
		String expectedErrorMessage = "negatives not allowed [-9] [-3] ";
		String actualErrorMessage = "";
		try {
			accumulator.add("//%|^\n1%-9^-3");
		} catch (InvalidValueException exception) {
			actualErrorMessage = exception.getMessage();
		}
		assertThat(actualErrorMessage, is(expectedErrorMessage));
	}
	
	public void shouldThrowExceptionWhenSeveralNegativeValuesAreInputUsingMulipleDelimitersOfMultipleLength() throws InvalidValueException {
		String expectedErrorMessage = "negatives not allowed [-9] [-3] ";
		String actualErrorMessage = "";
		try {
			accumulator.add("//%%|^\n1%%-9^-3");
		} catch (InvalidValueException exception) {
			actualErrorMessage = exception.getMessage();
		}
		assertThat(actualErrorMessage, is(expectedErrorMessage));
	}
 }
