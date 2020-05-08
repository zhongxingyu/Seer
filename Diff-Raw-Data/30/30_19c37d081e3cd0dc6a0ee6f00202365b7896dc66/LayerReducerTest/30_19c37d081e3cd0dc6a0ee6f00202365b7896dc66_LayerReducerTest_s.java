 package org.agmip.translators.common.soil;
 
 import static org.junit.Assert.assertEquals;
 import static org.agmip.translators.stics.util.Const.*;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import org.agmip.translators.soil.LayerReducer;
 import org.agmip.translators.soil.LayerReducerUtil;
 import org.agmip.translators.soil.SAReducerDecorator;
 import org.agmip.translators.stics.output.SoilAndInitOutput;
 import org.junit.Test;
 
 public class LayerReducerTest {
 	LayerReducer tool;
 	SoilAndInitOutput soilConverter;
	
	
 	public LayerReducerTest() {
 		SAReducerDecorator w = new SAReducerDecorator();
 		tool = new LayerReducer(w);
 		soilConverter = new SoilAndInitOutput(System.getProperty("user.dir"));
 		w.getCriteria().setFirstThreshold(5f);
 		w.getCriteria().setSecondThreshold(0.05f);
 	}
 
 	@Test
 	public void testComputeLayerSize() {
 		HashMap<String, String> soil1 = new HashMap<String, String>();
 		HashMap<String, String> soil2 = new HashMap<String, String>();
 		HashMap<String, String> soil3 = new HashMap<String, String>();
 		ArrayList<HashMap<String, String>> soilsData = new ArrayList<HashMap<String, String>>();
 		// First reference soil layer
 		soil1.put(SLLL, "0.026");
 		soil1.put(SLDUL, "0.096");
 		soil1.put(SLBDM, "1.3");
 		soil1.put(SKSAT, "1.0");
 		soil1.put(SLLB, "5.0");
 		soil1.put("sloc", "1.0");
 		// Second layer
 		soil2.put(SLLL, "0.025");
 		soil2.put(SLDUL, "0.086");
 		soil2.put(SLBDM, "1.3");
 		soil2.put(SLLB, "15.0");
 		soil3.put(SLLB, "18.0");
 		soilsData.add(soil1);
 		soilsData.add(soil2);
 		soilsData.add(soil3);
 
 		ArrayList<HashMap<String, String>> res = LayerReducerUtil.computeSoilLayerSize(soilsData);
 		assertEquals(res.get(0).get(SLLB), "5.0");
 		assertEquals(res.get(1).get(SLLB), "10.0");
 		assertEquals(res.get(2).get(SLLB), "3.0");
 	}
 
 	@Test
 	public void testNormalize() {
 		HashMap<String, String> soil1 = new HashMap<String, String>();
 		HashMap<String, String> soil2 = new HashMap<String, String>();
 		ArrayList<HashMap<String, String>> soilsData = new ArrayList<HashMap<String, String>>();
 		// First reference soil layer
 		soil1.put(SLLL, "0.026");
 		soil1.put(SLDUL, "0.096");
 		soil1.put(SLBDM, "1.3");
 		soil1.put(SKSAT, "1.0");
 		soil1.put(SLLB, "5.0");
 		soil1.put("sloc", "1.0");
 		// Second layer
 		soil2.put(SLLL, "0.025");
 		soil2.put(SLDUL, "0.086");
 		soil2.put(SLBDM, "1.3");
 		soil2.put(SLLB, "15.0");
 		soilsData.add(soil1);
 		soilsData.add(soil2);
 		LayerReducerUtil.computeSoilLayerSize(soilsData);
 		ArrayList<HashMap<String, String>> result = tool.normalizeSoilLayers(soilsData);
 		assertEquals(result.get(1).containsKey(SKSAT), true);
 	}
 
 	@Test
 	public void testAggregationMax() {
 		String[] keys = new String[] { SLLB, SKSAT, SLLL, SLDUL, SLBDM };
 		HashMap<String, String> soil1 = new HashMap<String, String>();
 		HashMap<String, String> soil2 = new HashMap<String, String>();
 		HashMap<String, String> expectedSoilResult1 = new HashMap<String, String>();
 		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
 		// First reference soil layer
 		soil1.put(SLLL, "0.12");
 		soil1.put(SLDUL, "0.19");
 		soil1.put(SLBDM, "1.30");
 		soil1.put(SKSAT, "1.0");
 		soil1.put(SLLB, "30.0");
 		soil1.put(ICNH4, "1.31");
 		soil1.put(ICNO3, "1.31");
 		soil1.put(ICH2O, "0.14");
 		// Second layer
 		soil2.put(SLLL, "0.13");
 		soil2.put(SLDUL, "0.20");
 		soil2.put(SLBDM, "1.31");
 		soil2.put(SLLB, "40.0");
 		soil2.put(ICNH4, "1.90");
 		soil2.put(ICNO3, "1.90");
 		soil2.put(ICH2O, "0.16");
 
 		// First reference soil layer
 		expectedSoilResult1.put(SLLL, "9.578545");
 		expectedSoilResult1.put(SLDUL, "14.942529");
 		expectedSoilResult1.put(SLBDM, "1.305");
 		expectedSoilResult1.put(SKSAT, "1.0");
 		expectedSoilResult1.put(SLLB, "40.0");
 		expectedSoilResult1.put(ICNH4, "7.6081495");
 		expectedSoilResult1.put(ICNO3, "7.6081495");
 		expectedSoilResult1.put(ICH2O, "11.494253");
 		list.add(soil1);
 		list.add(soil2);
 		tool.setMaxSoilLayers(1);
 		ArrayList<HashMap<String, String>> res = LayerReducerUtil.computeSoilLayerSize(list);
 		ArrayList<HashMap<String, String>> result = tool.process(res);
		System.out.println("sans chgt : "+result);
 
		soilConverter.getConverter().convertSoil(result, SoilAndInitOutput.SOIL_PARAM_TO_CONVERT);
		soilConverter.getConverter().convertInitValues(result, SoilAndInitOutput.INIT_PARAM_TO_CONVERT);
 		assertEquals(1, result.size());
 		System.out.println(result);
 		for (String key : expectedSoilResult1.keySet()) {
 			System.out.println(key + " ");
 			assertEquals(expectedSoilResult1.get(key), result.get(0).get(key));
 		}
 		tool.setMaxSoilLayers(5);
		
 	}
 
 	@Test
 	public void testAggregation() {
		String[] keys = new String[] { SLLB, SKSAT, SLLL, SLDUL, SLBDM};
 		HashMap<String, String> soil1 = new HashMap<String, String>();
 		HashMap<String, String> soil2 = new HashMap<String, String>();
 		HashMap<String, String> soil3 = new HashMap<String, String>();
 		HashMap<String, String> expectedSoilResult1 = new HashMap<String, String>();
 		HashMap<String, String> expectedSoilResult2 = new HashMap<String, String>();
 
 		// First reference soil layer
 		soil1.put(SLLL, "0.07");
 		soil1.put(SLDUL, "0.12");
 		soil1.put(SLBDM, "1.31");
 		soil1.put(SKSAT, "1.0");
 		soil1.put(SLLB, "5.0");
 		soil1.put(ICNH4, "1.31");
 		soil1.put(ICNO3, "1.31");
 		soil1.put(ICH2O, "0.15");
 		// Second layer
 		soil2.put(SLLL, "0.07");
 		soil2.put(SLDUL, "0.12");
 		soil2.put(SLBDM, "1.31");
 		soil2.put(SLLB, "10.0");
 		soil2.put(ICNH4, "1.31");
 		soil2.put(ICNO3, "1.31");
 		soil2.put(ICH2O, "0.15");
 		// Missing values should be taken from the first layer
 		soil3.put(SLLL, "1.5");
 		soil3.put(SLLB, "37.0");
 		soil3.put(SLDUL, "0.12");
 		soil3.put(SLBDM, "20");
 		soil3.put(ICNH4, "1.31");
 		soil3.put(ICNO3, "1.31");
 		soil3.put(ICH2O, "0.15");
 
 		// The merge result should be 2 soils layer
 		// First soil is a aggregated soil and the second is like the third but
 		// with reference values of the first
 
 		// First reference soil layer
 		expectedSoilResult1.put(SLLL, "5.3435116");
 		expectedSoilResult1.put(SLDUL, "9.160306");
 		expectedSoilResult1.put(SLBDM, "1.31");
 		expectedSoilResult1.put(SKSAT, "1.0");
 		expectedSoilResult1.put(SLLB, "10.0");
 		expectedSoilResult1.put(ICNH4, "5.1089997");
 		expectedSoilResult1.put(ICNO3, "5.1089997");
 		expectedSoilResult1.put(ICH2O, "11.538463");
 		// Second layer
 		expectedSoilResult2.put(SLLL, "7.5000005");
 		expectedSoilResult2.put(SLDUL, "0.6");
 		expectedSoilResult2.put(SLBDM, "20");
 		expectedSoilResult2.put(SKSAT, "1.0");
 		expectedSoilResult2.put(SLLB, "27.0");
 		expectedSoilResult2.put(ICNH4, "5.1089997");
 		expectedSoilResult2.put(ICNO3, "5.1089997");
 		expectedSoilResult2.put(ICH2O, "11.538463");
 
 		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
 		list.add(soil1);
 		list.add(soil2);
 		list.add(soil3);
 		ArrayList<HashMap<String, String>> res = LayerReducerUtil.computeSoilLayerSize(list);
 
 		ArrayList<HashMap<String, String>> result = tool.process(res);
		soilConverter.getConverter().convertSoil(result, SoilAndInitOutput.SOIL_PARAM_TO_CONVERT);
		soilConverter.getConverter().convertInitValues(result, SoilAndInitOutput.INIT_PARAM_TO_CONVERT);
 		int i = 0;
 		for (HashMap<String, String> layer : result) {
 			i++;
 			System.out.println("soil " + i + " " + layer);
 		}
 
 		for (String key : keys) {
 			System.out.println(key + " ");
 			assertEquals(expectedSoilResult1.get(key), result.get(0).get(key));
 			assertEquals(expectedSoilResult2.get(key), result.get(1).get(key));
 		}
 
 	}
 
 	@Test
 	public void testNoAggregation() {
 		String[] keys = new String[] { SLLB, SKSAT, SLLL, SLDUL, SLBDM, ICNH4, ICNO3, ICH2O };
 		HashMap<String, String> soil1 = new HashMap<String, String>();
 		HashMap<String, String> soil2 = new HashMap<String, String>();
 		HashMap<String, String> expectedSoilResult1 = new HashMap<String, String>();
 		HashMap<String, String> expectedSoilResult2 = new HashMap<String, String>();
 
 		// First reference soil layer
 		soil1.put(SLLL, "0.12");
 		soil1.put(SLDUL, "0.19");
 		soil1.put(SLBDM, "1.3");
 		soil1.put(SKSAT, "1.0");
 		soil1.put(ICNH4, "1.31");
 		soil1.put(ICNO3, "1.31");
 		soil1.put(ICH2O, "0.15");
 		soil1.put(SLLB, "30.0");
 		soil1.put(ICH2O, "0.15");
 		// Second layer
 		soil2.put(SLLL, "0.31");
 		soil2.put(SLDUL, "0.44");
 		soil2.put(SLBDM, "1.40");
 		soil2.put(ICNH4, "1.9");
 		soil2.put(ICNO3, "1.9");
 		soil2.put(SLLB, "40.0");
 		soil2.put(ICH2O, "0.40");
 
 		// The merge result should be 2 soils layer => no aggregation
 
 		// First reference soil layer
 		expectedSoilResult1.put(SLLL, "9.230769");
 		expectedSoilResult1.put(SLDUL, "14.615385");
 		expectedSoilResult1.put(SLBDM, "1.3");
 		expectedSoilResult1.put(SKSAT, "1.0");
 		expectedSoilResult1.put(SLLB, "30.0");
 		expectedSoilResult1.put(ICNH4, "5.1089997");
 		expectedSoilResult1.put(ICNO3, "5.1089997");
 		expectedSoilResult1.put(ICH2O, "11.538463");
 		// Second layer
 		expectedSoilResult2.put(SLLL, "22.142857");
 		expectedSoilResult2.put(SLDUL, "31.428572");
 		expectedSoilResult2.put(SLBDM, "1.40");
 		expectedSoilResult2.put(SKSAT, "1.0");
 		expectedSoilResult2.put(SLLB, "10.0");
 		expectedSoilResult2.put(ICNH4, "2.6599998");
 		expectedSoilResult2.put(ICNO3, "2.6599998");
 		expectedSoilResult2.put(ICH2O, "28.57143");
 
 		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
 		list.add(soil1);
 		list.add(soil2);
 		ArrayList<HashMap<String, String>> res = LayerReducerUtil.computeSoilLayerSize(list);
 		ArrayList<HashMap<String, String>> result = tool.process(res);
		soilConverter.getConverter().convertSoil(result, SoilAndInitOutput.SOIL_PARAM_TO_CONVERT);
		soilConverter.getConverter().convertInitValues(result, SoilAndInitOutput.INIT_PARAM_TO_CONVERT);
 		int i = 0;
 		for (HashMap<String, String> layer : result) {
 			i++;
 			System.out.println("soil " + i + " " + layer);
 		}
 		assertEquals(result.size(), 2);
 		for (String key : keys) {
 			System.out.println(key + " ");
 			assertEquals(expectedSoilResult1.get(key), result.get(0).get(key));
 			assertEquals(expectedSoilResult2.get(key), result.get(1).get(key));
 		}
 
 	}
 }
