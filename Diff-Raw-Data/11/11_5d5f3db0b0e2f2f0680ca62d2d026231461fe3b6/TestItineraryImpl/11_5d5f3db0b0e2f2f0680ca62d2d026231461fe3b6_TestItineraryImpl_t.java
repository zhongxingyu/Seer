 package traffic;
 
 import static java.util.Arrays.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hamcrest.Description;
 import org.jmock.Expectations;
 import org.jmock.api.Action;
 import org.jmock.api.Invocation;
 import org.junit.Rule;
 import org.junit.Test;
 
 import com.google.common.collect.Iterators;
 
 public class TestItineraryImpl {
 	@Rule
 	public final JUnitRuleMockery context = new JUnitRuleMockery();
 
 	private final Segment segment0 = context.mock(Segment.class, "segment0");
 	private final Segment segment1 = context.mock(Segment.class, "segment1");
 
 	private final CellChain cellChain0 = context.mock(CellChain.class, "cellChain0");
 	private final CellChain cellChain1 = context.mock(CellChain.class, "cellChain1");
 
 	private final Junction junctionCell0 = context.mock(Junction.class, "junctionCell0");
 	private final Junction junctionCell1 = context.mock(Junction.class, "junctionCell1");
 	private final Junction junctionCell2 = context.mock(Junction.class, "junctionCell2");
 
 	private final Cell segmentCell0 = context.mock(Cell.class, "segmentCell0");
 	private final Cell segmentCell1 = context.mock(Cell.class, "segmentCell1");
 	private final Cell segmentCell2 = context.mock(Cell.class, "segmentCell2");
 
 	@Test
 	public void itineraryContainsCellsFromRouteJunctionsAndSegmentsInOrderForMultipleSegments() throws Exception {
 		context.checking(new Expectations() {
 			{
 				oneOf(segment0).cells(); will(returnList(junctionCell0, segmentCell0, segmentCell1, junctionCell1));
 				oneOf(segment1).cells(); will(returnList(junctionCell1, segmentCell2, junctionCell2));
 			}
 		});
 
		assertThat(cellsIn(new ItineraryImpl(segment0, segment1)), contains(junctionCell0, segmentCell0, segmentCell1, junctionCell1, segmentCell2, junctionCell2));
	}

	private List<Cell> cellsIn(final ItineraryImpl itineraryImpl) {
 		final List<Cell> cells = new ArrayList<Cell>();
 		Iterators.addAll(cells, itineraryImpl.iterator());
		return cells;
 	}
 
 	@Test
 	public void itineraryContainsCellsFromRouteJunctionsAndSegmentsInOrder() throws Exception {
 		context.checking(new Expectations() {
 			{
 				oneOf(segment0).cells(); will(returnList(junctionCell0, segmentCell0, segmentCell1, junctionCell1));
 			}
 		});
 
		assertThat(cellsIn(new ItineraryImpl(segment0)), contains(junctionCell0, segmentCell0, segmentCell1, junctionCell1));
 	}
 
 	//type variable - generics
 	private <T> Action returnList(final T...ts) {
 		return new Action(){
 			@Override
 			public void describeTo(final Description description) {
 				description.appendText("return list of ").appendValue(ts);
 			}
 
 			@Override
 			public Object invoke(final Invocation arg0) throws Throwable {
 				return asList(ts);
 			}};
 	}
 }
