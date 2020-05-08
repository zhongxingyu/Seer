 package com.bluebarracudas.test;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.bluebarracudas.model.TFactory;
 import com.bluebarracudas.model.TRoute;
 import com.bluebarracudas.model.TStation;
 import com.bluebarracudas.model.TStationData;
 import com.bluebarracudas.model.TStop;
 import com.bluebarracudas.model.TStopData;
 
 public class TRouteTests {
 
 	/**
 	 * Test the orange line next connections
 	 */
 	@Test
 	public void testOLineNextConnections() {
 		List<TStop> stops = TFactory.getStop(TStopData.FOREST_HILLS.getID()).getNextStops();
 		List<TStop> expected = new ArrayList<TStop>();
 		expected.add(TFactory.getStop(TStopData.GREEN_ST_N.getID()));
 		assertEquals(expected, stops);
 		
 		List<TStop> stops2 = TFactory.getStop(TStopData.CHINATOWN_N.getID()).getNextStops();
 		List<TStop> expected2 = new ArrayList<TStop>();
 		expected2.add(TFactory.getStop(TStopData.DTX_N_O.getID()));
 		assertEquals(expected2, stops2);
 	}
 	
 	/**
 	 * Test the red line next connections
 	 * Especially around JFK/UMass
 	 */
 	@Test 
 	public void testRLineNextConnections() {
 		List<TStop> stops = TFactory.getStop(TStopData.CENTRAL_N.getID()).getNextStops();
 		List<TStop> expected = new ArrayList<TStop>();
 		expected.add(TFactory.getStop(TStopData.HARVARD_N.getID()));
 		assertEquals(expected, stops);
 		
 		List<TStop> stops2 = TFactory.getStop(TStopData.ANDREW_S.getID()).getNextStops();
 		List<TStop> expected2 = new ArrayList<TStop>();
 		expected2.add(TFactory.getStop(TStopData.JFK_S_A.getID()));
 		expected2.add(TFactory.getStop(TStopData.JFK_S_B.getID()));
 		assertEquals(expected2, stops2);
 	}
 	
 	/**
 	 * Test the orange line prev connections
 	 */
 	@Test
 	public void testOLinePrevConnections() {
 		List<TStop> stops = TFactory.getStop(TStopData.SULLIVAN_S.getID()).getPrevStops();
 		List<TStop> expected = new ArrayList<TStop>();
 		expected.add(TFactory.getStop(TStopData.WELLINGTON_S.getID()));
 		assertEquals(expected, stops);
 		
 		List<TStop> stops2 = TFactory.getStop(TStopData.DTX_N_O.getID()).getPrevStops();
 		List<TStop> expected2 = new ArrayList<TStop>();
 		expected2.add(TFactory.getStop(TStopData.CHINATOWN_N.getID()));
 		assertEquals(expected2, stops2);
 	}
 	
 	/**
 	 * Test the red line prev connections
 	 * Especially around JFK/UMass
 	 */
 	@Test
 	public void testRLinePrevConnections() {
 		List<TStop> stops = TFactory.getStop(TStopData.PARK_S.getID()).getPrevStops();
 		List<TStop> expected = new ArrayList<TStop>();
 		expected.add(TFactory.getStop(TStopData.CHARLES_S.getID()));
 		assertEquals(expected, stops);
 		
 		List<TStop> stops2 = TFactory.getStop(TStopData.ANDREW_N.getID()).getPrevStops();
 		List<TStop> expected2 = new ArrayList<TStop>();
 		expected2.add(TFactory.getStop(TStopData.JFK_N_A.getID()));
 		expected2.add(TFactory.getStop(TStopData.JFK_N_B.getID()));
 		assertEquals(expected2, stops2);
 	}
 	
 	/**
 	 * Test the use case of going from A to B where both
 	 * stations are on the same arm of the same line.
 	 */
 	@Test
 	public void testAtoBSameLine() {
 		TStation start = TFactory.getStation(TStationData.RUGGLES.getID());
 		TStation end = TFactory.getStation(TStationData.MALDEN.getID());
 		
 		TRoute route = TFactory.findShortestPath(start, end);
 		List<TStop> expectedRoute = new ArrayList<TStop>();
 		expectedRoute.add(TFactory.getStop(TStopData.RUGGLES_N.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.MALDEN_N.getID()));
 		TRoute expected = new TRoute(expectedRoute);
 		System.out.println(expected.printRoute());
 		System.out.println(route.printRoute());
 		assertEquals(expected, route);
 		
 		TStation end2 = TFactory.getStation(TStationData.FOREST_HILLS.getID());
 		
		TRoute route2 = TFactory.findShortestPath(start, end);
 		List<TStop> expectedRoute2 = new ArrayList<TStop>();
 		expectedRoute2.add(TFactory.getStop(TStopData.RUGGLES_S.getID()));
 		expectedRoute2.add(TFactory.getStop(TStopData.FOREST_HILLS_E.getID()));
 		TRoute expected2 = new TRoute(expectedRoute2);
 		
 		assertEquals(expected2, route2);
 	}
 	
 	/**
 	 * Test the use case of going from A to B where both
 	 * stations are on different arms of the same line.
 	 */
 	@Test
 	public void testAtoBSameLine2Arms() {
 		TStation start = TFactory.getStation(TStationData.ASHMONT.getID());
 		TStation end = TFactory.getStation(TStationData.BRAINTREE.getID());
 		
 		TRoute route = TFactory.findShortestPath(start, end);
 		TRoute expected = new TRoute();
 		
 		assertEquals(expected, route);
 	}
 	
 	/**
 	 * Test the use case of going from A to B where the
 	 * stations are on different lines and the lines have a transfer
 	 * station.
 	 */
 	@Test
 	public void testAtoB1Transfer() {
 		// Test Orange -> Red
 		TStation start = TFactory.getStation(TStationData.RUGGLES.getID());
 		TStation end = TFactory.getStation(TStationData.CENTRAL.getID());
 		
 		TRoute route = TFactory.findShortestPath(start, end);
 		
 		List<TStop> expectedRoute = new ArrayList<TStop>();
 		expectedRoute.add(TFactory.getStop(TStopData.RUGGLES_N.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.DTX_N_R.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.CENTRAL_N.getID()));
 		TRoute expected = new TRoute(expectedRoute);
 		
 		assertEquals(expected, route);
 		
 		// Test Blue -> Orange
 		TStation start2 = TFactory.getStation(TStationData.AIRPORT.getID());
 		TStation end2 = TFactory.getStation(TStationData.TUFTS.getID());
 		TRoute route2 = TFactory.findShortestPath(start2, end2);
 		
 		List<TStop> expectedRoute2 = new ArrayList<TStop>();
 		expectedRoute2.add(TFactory.getStop(TStopData.AIRPORT_S.getID()));
 		expectedRoute2.add(TFactory.getStop(TStopData.STATE_S_O.getID()));
 		expectedRoute2.add(TFactory.getStop(TStopData.TUFTS_S.getID()));
 		TRoute expected2 = new TRoute(expectedRoute2);
 		
 		assertEquals(expected2, route2);
 	}
 	
 	/**
 	 * Test the use case of going from A to B where the
 	 * stations are on different lines and the lines do not have 
 	 * a transfer station.
 	 */
 	@Test
 	public void testAtoB2Transfers() {
 		TStation start = TFactory.getStation(TStationData.AIRPORT.getID());
 		TStation end = TFactory.getStation(TStationData.CENTRAL.getID());
 		
 		TRoute route = TFactory.findShortestPath(start, end);
 		List<TStop> expectedRoute = new ArrayList<TStop>();
 		expectedRoute.add(TFactory.getStop(TStopData.AIRPORT_S.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.STATE_S_O.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.DTX_N_R.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.CENTRAL_N.getID()));
 		TRoute expected = new TRoute(expectedRoute);
 		
 		assertEquals(expected, route);
 	}
 	
 	/**
 	 * Test the use case of going to 3 ordered stops, where all three are on the
 	 * same line.
 	 */
 	@Test
 	public void testAtoBtoCSameLine() {
 		TStation aStation = TFactory.getStation(TStationData.ALEWIFE.getID());
 		TStation bStation = TFactory.getStation(TStationData.DAVIS.getID());
 		TStation cStation = TFactory.getStation(TStationData.CENTRAL.getID());
 		
 		TRoute route = new TRoute();
 		List<TStop> expectedRoute = new ArrayList<TStop>();
 		expectedRoute.add(TFactory.getStop(TStopData.ALEWIFE.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.DAVIS_S.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.CENTRAL_S.getID()));
 		TRoute expected = new TRoute(expectedRoute);
 
 		assertEquals(expected, route);
 	}
 	
 	/**
 	 * Test the use case of going to 3 ordered stops, where all three are 
 	 * on different lines.
 	 */
 	@Test
 	public void testAtoBtoCDiffLines() {
 		TStation aStation = TFactory.getStation(TStationData.ALEWIFE.getID());
 		TStation bStation = TFactory.getStation(TStationData.RUGGLES.getID());
 		TStation cStation = TFactory.getStation(TStationData.MAVERICK.getID());
 		
 		TRoute route = new TRoute();
 		List<TStop> expectedRoute = new ArrayList<TStop>();
 		expectedRoute.add(TFactory.getStop(TStopData.ALEWIFE.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.DTX_S_O.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.RUGGLES_N.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.STATE_N_B.getID()));
 		expectedRoute.add(TFactory.getStop(TStopData.MAVERICK_N.getID()));
 		TRoute expected = new TRoute(expectedRoute);
 
 		assertEquals(expected, route);
 	}
 }
