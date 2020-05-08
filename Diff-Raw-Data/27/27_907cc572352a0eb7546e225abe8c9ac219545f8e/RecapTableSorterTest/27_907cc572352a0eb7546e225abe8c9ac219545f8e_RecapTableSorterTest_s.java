 package hoplugins.teamAnalyzer.ui;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.lessThan;
 import hoplugins.teamAnalyzer.ui.RecapTableSorter;
 
 import java.util.Comparator;
 
 import org.junit.Test;
 
 public class RecapTableSorterTest {
 
 	private final class TestableRecapTableSorter extends RecapTableSorter {
 		private static final long serialVersionUID = 1L;
 
 		public TestableRecapTableSorter() {
 			super(null);
 		}
 
 		@Override
 		protected String getSkillDenomination(int i) {
 			return "ignore";
 		}
 	}
 
 	@Test
 	public void shouldReturnNaturalNumericComparatorForWeekColumn()
 			throws Exception {
 		RecapTableSorter recapTableSorter = new TestableRecapTableSorter();
 		assertThat(recapTableSorter.getCustomComparator(3),
 				is(instanceOf(RecapTableSorter.NaturalNumericComparator.class)));
 	}
 
 	@Test
 	public void shouldSortNumericStringsInNaturalAscendingOrder()
 			throws Exception {
 		Comparator<String> comparator = getComparator();
 
 		assertThat(comparator.compare("1", "2"), is(lessThan(0)));
 		assertThat(comparator.compare("1", "10"), is(lessThan(0)));
 		assertThat(comparator.compare("2", "10"), is(lessThan(0)));
 	}
 
 	@Test
 	public void shouldSortNumericStringsInNaturalDescendingOrder()
 			throws Exception {
 		Comparator<String> comparator = getComparator();
 
 		assertThat(comparator.compare("2", "1"), is(greaterThan(0)));
 		assertThat(comparator.compare("10", "1"), is(greaterThan(0)));
 		assertThat(comparator.compare("10", "2"), is(greaterThan(0)));
 	}
 
 	@Test
 	public void shouldRecognizeEqualsStrings() throws Exception {
 		Comparator<String> comparator = getComparator();
 
 		assertThat(comparator.compare("1", "1"), is(equalTo(0)));
 		assertThat(comparator.compare("10", "10"), is(equalTo(0)));
 	}
 
 	private Comparator<String> getComparator() {
 		RecapTableSorter recapTableSorter = new TestableRecapTableSorter();
 		return recapTableSorter.getCustomComparator(3);
 	}
 
 }
