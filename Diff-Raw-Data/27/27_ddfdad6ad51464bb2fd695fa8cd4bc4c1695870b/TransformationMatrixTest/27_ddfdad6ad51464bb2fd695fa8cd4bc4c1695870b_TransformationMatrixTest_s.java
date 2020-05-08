 package it.unito.geosummly;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import junit.framework.TestCase;
 
 public class TransformationMatrixTest extends TestCase {
 	
 	public void testUpdateMap() {
 		//Initialize the transformation matrix and create the hash map 
 		TransformationMatrix tm=new TransformationMatrix();
 		HashMap<String, Integer> actualMap=new HashMap<String, Integer>();
 		tm.setMap(actualMap);
 		ArrayList<String> actualHeader=new ArrayList<String>();
 		tm.setHeader(actualHeader);
 		ArrayList<String> a=new ArrayList<String>();
 		a.add("Cat 1");
 		a.add("Cat 1");
 		a.add("Cat 2");
 		a.add("Cat 1");
 		a.add("Cat 3");
 		a.add("Cat 2");
 		a.add("Cat 3");
 		a.add("Cat 4");
 		a.add("Cat 1");
 		a.add("Cat 5");
 		a.add("Cat 2");
 		a.add("Cat 5");
 		tm.updateMap(a);
 		a.add("Cat 5");
 		a.add("Cat 6");
 		a.add("Cat 2");
 		a.add("Cat 6");
 		tm.updateMap(a);
 		
 		//Construct the test case
 		HashMap<String, Integer> expectedMap=new HashMap<String, Integer>();
 		expectedMap.put("Cat 1", 2);
 		expectedMap.put("Cat 2", 3);
 		expectedMap.put("Cat 3", 4);
 		expectedMap.put("Cat 4", 5);
 		expectedMap.put("Cat 5", 6);
 		expectedMap.put("Cat 6", 7);
 		ArrayList<String> expectedHeader=new ArrayList<String>();
 		expectedHeader.add("Cat 1");
 		expectedHeader.add("Cat 2");
 		expectedHeader.add("Cat 3");
 		expectedHeader.add("Cat 4");
 		expectedHeader.add("Cat 5");
 		expectedHeader.add("Cat 6");
 		
 		//Start the tests
 		assertNotNull(actualMap);
 		assertNotNull(expectedHeader);
 		assertEquals(expectedMap, actualMap);
 		assertEquals(expectedHeader, actualHeader);
 	}
 	
 	public void testFillRow() {
 		//Initialize the transformation matrix and create a consistent row 
 		TransformationMatrix tm=new TransformationMatrix();
 		HashMap<String, Integer> map=new HashMap<String, Integer>();
 		map.put("Cat 1", 2);
 		map.put("Cat 2", 3);
 		map.put("Cat 3", 4);
 		map.put("Cat 4", 5);
 		map.put("Cat 5", 6);
 		tm.setMap(map);
 		ArrayList<Integer> occurrences=new ArrayList<Integer>();
 		occurrences.add(3);
 		occurrences.add(5);
 		occurrences.add(1);
 		occurrences.add(4);
 		occurrences.add(2);
 		ArrayList<String> distincts=new ArrayList<String>();
 		distincts.add("Cat 3");
 		distincts.add("Cat 5");
 		distincts.add("Cat 1");
 		distincts.add("Cat 4");
 		distincts.add("Cat 2");
 		int cat_num=10;
 		double lat=10.0;
 		double lng=20.0;
 		ArrayList<Double> actual;
 		actual=tm.fillRow(occurrences, distincts, cat_num, lat, lng);
 		
 		//Construct the test case
 		ArrayList<Double> expected = new ArrayList<Double>();
 		expected.add(10.0);
 		expected.add(20.0);
 		expected.add(0.1);
 		expected.add(0.2);
 		expected.add(0.3);
 		expected.add(0.4);
 		expected.add(0.5);
 		
 		//Start the tests
 		assertNotNull(actual);
 		assertEquals(expected, actual);
 	}
 	
 	public void testFixRowsLength() {
 		//Initialize the transformation matrix and execute the method
 		TransformationMatrix tm=new TransformationMatrix();
 		ArrayList<ArrayList<Double>> matrix= new ArrayList<ArrayList<Double>>();
 		ArrayList<Double> row1=new ArrayList<Double>();
 		row1.add(0.1);
 		row1.add(0.2);
 		ArrayList<Double> row2=new ArrayList<Double>();
 		row2.add(0.5);
 		row2.add(0.3);
 		row2.add(0.0);
 		row2.add(0.4);
 		ArrayList<Double> row3=new ArrayList<Double>();
 		row3.add(0.2);
 		row3.add(0.0);
 		row3.add(0.1);
 		matrix.add(row1);
 		matrix.add(row2);
 		matrix.add(row3);
 		tm.setMatrix(matrix);
 		tm.fixRowsLength(row2.size());
 		ArrayList<ArrayList<Double>> actual=tm.getMatrix();
 		
 		//Construct the test case
 		ArrayList<ArrayList<Double>> expected=new ArrayList<ArrayList<Double>>();
 		ArrayList<Double> e1=new ArrayList<Double>();
 		e1.add(0.1);
 		e1.add(0.2);
 		e1.add(0.0);
 		e1.add(0.0);
 		ArrayList<Double> e2=new ArrayList<Double>();
 		e2.add(0.5);
 		e2.add(0.3);
 		e2.add(0.0);
 		e2.add(0.4);
 		ArrayList<Double> e3=new ArrayList<Double>();
 		e3.add(0.2);
 		e3.add(0.0);
 		e3.add(0.1);
 		e3.add(0.0);
 		expected.add(e1);
 		expected.add(e2);
 		expected.add(e3);
 		
 		//Start the test
 		assertEquals(expected, actual);
 	}
 	
 }
