 package com.karhatsu.suosikkipysakit.datasource.parsers.test;
 
 import org.json.JSONException;
 
 import com.karhatsu.suosikkipysakit.datasource.DataNotFoundException;
 import com.karhatsu.suosikkipysakit.datasource.parsers.LinesJSONParser;
 import com.karhatsu.suosikkipysakit.domain.Line;
 import com.karhatsu.suosikkipysakit.domain.Stop;
 
 public class LinesJSONParserTest extends AbstractJSONParserTest {
 
 	private static final String TEST_JSON_FILE = "test-lines-response.json";
 
 	private static String jsonString;
 	private LinesJSONParser parser = new LinesJSONParser();
 
 	@Override
 	protected void setUp() throws Exception {
 		if (jsonString == null) {
 			jsonString = readTestJson(TEST_JSON_FILE);
 		}
 	}
 
 	public void testLinesCount() throws JSONException, DataNotFoundException {
 		assertEquals(6, parser.parse(jsonString).size());
 	}
 
 	public void testLineCode() throws JSONException, DataNotFoundException {
 		assertEquals("14", getFirstLine().getCode());
 	}
 
 	public void testLineName() throws JSONException, DataNotFoundException {
 		assertEquals("Hernesaari - Kamppi - Pajamäki", getFirstLine().getName());
 	}
 
 	public void testLineStart() throws JSONException, DataNotFoundException {
 		assertEquals("Hernesaari", getFirstLine().getLineStart());
 	}
 
 	public void testLineEnd() throws JSONException, DataNotFoundException {
 		assertEquals("Pajamäki", getFirstLine().getLineEnd());
 	}
 
 	public void testStopCount() throws JSONException, DataNotFoundException {
 		assertEquals(35, getFirstLine().getStops().size());
 	}
 
 	public void testStopCode() throws JSONException, DataNotFoundException {
 		assertEquals("1195", getFirstLineFirstStop().getCode());
 	}
 
	public void testLongStopCodeIsUsedIfShortStopCodeIsEmpty()
			throws JSONException, DataNotFoundException {
		assertEquals("1204115", getFirstLine().getStops().get(1).getCode());
	}

 	public void testStopName() throws JSONException, DataNotFoundException {
 		assertEquals("Hernesaaren laituri", getFirstLineFirstStop().getName());
 	}
 
 	public void testStopCoordinates() throws JSONException,
 			DataNotFoundException {
 		assertEquals("2551480,6671094", getFirstLineFirstStop()
 				.getCoordinates());
 	}
 
 	private Line getFirstLine() throws JSONException, DataNotFoundException {
 		return parser.parse(jsonString).get(0);
 	}
 
 	private Stop getFirstLineFirstStop() throws JSONException,
 			DataNotFoundException {
 		return getFirstLine().getStops().get(0);
 	}
 
 	public void testEmptyJSON() {
 		try {
 			parser.parse("");
 			fail("Did not throw StopRequestException");
 		} catch (DataNotFoundException e) {
 		} catch (JSONException e) {
 			fail("Threw JSONException");
 		}
 	}
 }
