 package org.agmip.functions;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.agmip.ace.AcePathfinder;
 import org.agmip.ace.util.AcePathfinderUtil;
 import org.agmip.util.JSONAdapter;
 import org.agmip.util.MapUtil;
 import static org.agmip.util.MapUtil.*;
 import static org.junit.Assert.*;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Meng Zhang
  */
 public class ExperimentHelperTest {
 
     private static final Logger log = LoggerFactory.getLogger(ExperimentHelperTest.class);
 
     @Test
     public void testGetAutoPlantingDate() throws IOException, Exception {
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "25";
         String dayNum = "4";
         String expected_1 = "19820203";
         int expected_2 = 2; // 5 standard events + 1 new planting event
         String acctual_1 = "";
         int acctual_2 = 0;
         URL test_resource = this.getClass().getResource("/auto_plant_single_year_test.json");
         ArrayList<Map<String, String>> events = new ArrayList<Map<String, String>>();
         HashMap<String, ArrayList<String>> results = new HashMap<String, ArrayList<String>>();
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             //Map<String, Object> expData = getRawPackageContents(data, "experiments").get(0);        
             // Map<String, Object> expData = (Map)((ArrayList) data.get("experiments")).get(0);
             data.put("exp_dur", "2");
             results = ExperimentHelper.getAutoPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             //Map<String, ArrayList> mgnData = (Map) data.get("results");
             //events = mgnData.get("events");
             //acctual_1 = results.get(0);
         }
         log.info("Results: {}", results);
 
         assertEquals("getAutoPlantingDate: normal case", expected_1, results.get("pdate").get(0));
         assertEquals("getAutoPlantingDate: no date find case", expected_2, results.get("pdate").size());
 
     }
 
     @Test
     @Ignore
     public void testGetAutoPlantingDate_oneYear() throws IOException, Exception {
         String line;
         String startDate = "03-01";
         String endDate = "04-01";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19990310"; // TODO
         int expected_2 = 3;
         String acctual_1 = "";
         int acctual_2 = 0;
         URL test_resource = null;
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
             Map<String, Object> expData = JSONAdapter.fromJSON(line);
             data.put("experiments", new ArrayList());
             data.put("weathers", new ArrayList());
             data.get("experiments").add(expData);
             data.get("weathers").add((Map) expData.get("weather"));
             expData.put("sc_year", "1982");
             ExperimentHelper.getAutoPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             Map<String, ArrayList> mgnData = (Map) expData.get("management");
             ArrayList<Map<String, String>> events = mgnData.get("events");
             acctual_1 = events.get(0).get("date");
             acctual_2 = events.size();
         }
 
         assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoPlantingDate: no date find case", expected_2, acctual_2);
 
     }
 
     @Test
     public void testGetAutoPlantingDate_machakos() throws IOException, Exception {
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19800124";
         String expected_2 = "19810218";
         int expected_3 = 3;
         String acctual_1 = "";
         String acctual_2 = "";
         int acctual_3 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             data.put("exp_dur", "3");
             HashMap<String, ArrayList<String>> results = ExperimentHelper.getAutoPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             acctual_1 = results.get("pdate").get(0);
             acctual_2 = results.get("pdate").get(1);
             acctual_3 = results.get("pdate").size();
             log.info("Results: {}", results);
         }
 
         assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoPlantingDate: copy case", expected_2, acctual_2);
         assertEquals("getAutoPlantingDate: no date find case", expected_3, acctual_3);
     }
 
     @Test
     public void testGetAutoPlantingDate_machakos_lastDate() throws IOException, Exception {
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "50.0";
         String dayNum = "6";
         String expected_1 = "19800228";
         String expected_2 = "19810228";
         int expected_3 = 3;
         String acctual_1 = "";
         String acctual_2 = "";
         int acctual_3 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             data.put("exp_dur", "3");
             HashMap<String, ArrayList<String>> results = ExperimentHelper.getAutoPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             acctual_1 = results.get("pdate").get(0);
             acctual_2 = results.get("pdate").get(1);
             acctual_3 = results.get("pdate").size();
             log.info("Results: {}", results);
         }
 
         assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoPlantingDate: copy case", expected_2, acctual_2);
         assertEquals("getAutoPlantingDate: no date find case", expected_3, acctual_3);
     }
 
     @Test
     public void testGetAutoFillPlantingDate_machakos() throws IOException, Exception {
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19810218";
         int expected_3 = 1;
         String acctual_1 = "";
         int acctual_3 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             AcePathfinderUtil.insertValue(data, "crid", "MAZ");
             data.put("sc_year", "1981");
             HashMap<String, ArrayList<String>> results = ExperimentHelper.getAutoFillPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             acctual_1 = results.get("pdate").get(0);
             acctual_3 = results.get("pdate").size();
             log.info("Results: {}", results);
         }
 
         assertEquals("getAutoFillPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoFillPlantingDate: no date find case", expected_3, acctual_3);
     }
 
     @Test
     public void testGetAutoFillPlantingDate_machakos_NOSCYear() throws IOException, Exception {
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19800124";
         int expected_3 = 1;
         String acctual_1 = "";
         int acctual_3 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             AcePathfinderUtil.insertValue(data, "crid", "MAZ");
             HashMap<String, ArrayList<String>> results = ExperimentHelper.getAutoFillPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             acctual_1 = results.get("pdate").get(0);
             acctual_3 = results.get("pdate").size();
             log.info("Results: {}", results);
         }
 
         assertEquals("getAutoFillPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoFillPlantingDate: no date find case", expected_3, acctual_3);
     }
 
     @Test
     public void testGetAutoFillPlantingDate_machakos_ValidPdate() throws IOException, Exception {
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "9.0";
         String dayNum = "6";
         int expected_3 = 0;
         int acctual_3 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             data.put("sc_year", "1981");
             AcePathfinderUtil.insertValue(data, "crid", "MAZ");
             AcePathfinderUtil.insertValue(data, "pdate", "19830101");
             HashMap<String, ArrayList<String>> results = ExperimentHelper.getAutoFillPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             acctual_3 = results.size();
             log.info("Results: {}", results);
         }
 
         assertEquals("getAutoFillPlantingDate: pdate valid case", expected_3, acctual_3);
     }
 
     @Test
     public void testGetAutoFillPlantingDate_machakos_lastDate() throws IOException, Exception {
         log.debug("== testGetAutoFillPlantingDate_machakos_lastDate() == Start");
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "50.0";
         String dayNum = "6";
         String expected_1 = "19810228";
         int expected_3 = 1;
         String acctual_1 = "";
         int acctual_3 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             AcePathfinderUtil.insertValue(data, "crid", "MAZ");
             data.put("sc_year", "1981");
             HashMap<String, ArrayList<String>> results = ExperimentHelper.getAutoFillPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             acctual_1 = results.get("pdate").get(0);
             acctual_3 = results.get("pdate").size();
             log.info("Results: {}", results);
         }
 
         assertEquals("getAutoFillPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoFillPlantingDate: no date find case", expected_3, acctual_3);
         log.debug("== testGetAutoFillPlantingDate_machakos_lastDate() == End");
     }
 
     @Test
     @Ignore
     public void testGetAutoPlantingDate_machakos_scYear() throws IOException, Exception {
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19830214";
         String expected_2 = "19840131";
         String expected_3 = "19850202";
         int expected_99 = 3;
         String acctual_1 = "";
         String acctual_2 = "";
         String acctual_3 = "";
         int acctual_99 = 0;
 
         URL test_resource = this.getClass().getResource("/machakos_wth_only.json");
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(test_resource.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
             data.put("exp_dur", "3");
             data.put("sc_year", "1983");
             HashMap<String, ArrayList<String>> result = ExperimentHelper.getAutoPlantingDate(data, startDate, endDate, accRainAmt, dayNum);
             Map<String, ArrayList> mgnData = (Map) data.get("management");
             ArrayList<Map<String, String>> events = mgnData.get("events");
             acctual_1 = events.get(0).get("date");
             acctual_2 = events.get(1).get("date");
             acctual_3 = events.get(2).get("date");
             acctual_99 = events.size();
         }
 
         assertEquals("getAutoPlantingDate: normal 1st year case with start year", expected_1, acctual_1);
         assertEquals("getAutoPlantingDate: normal 2nd year case with start year", expected_2, acctual_2);
         assertEquals("getAutoPlantingDate: normal 3rd year case with start year", expected_3, acctual_3);
         assertEquals("getAutoPlantingDate: all year with auto-planting case", expected_99, acctual_99);
 
     }
 
     @Test
     public void testGetFertDistribution() throws IOException, Exception {
         String line;
         String num = "2";
         String fecd = "FE005";
         String feacd = "AP002";
         String fedep = "10";
         String[] offsets = {"10", "45"};
         String[] ptps = {"33.3", "66.7"};
         // planting data is 19990415
         // fen_tot is 110
         Map expected_1 = new HashMap();
         expected_1.put("event", "fertilizer");
         expected_1.put("date", "19990425");
         expected_1.put("fecd", "FE005");
         expected_1.put("feacd", "AP002");
         expected_1.put("fedep", "10");
         expected_1.put("feamn", "37");
         Map expected_2 = new HashMap();
         expected_2.put("event", "fertilizer");
         expected_2.put("date", "19990530");
         expected_2.put("fecd", "FE005");
         expected_2.put("feacd", "AP002");
         expected_2.put("fedep", "10");
         expected_2.put("feamn", "73");
         Map acctual_1;
         Map acctual_2;
 
         HashMap<String, Object> data = new HashMap<String, Object>();
 
         // BufferedReader br = new BufferedReader(
         //         new InputStreamReader(
         //         new FileInputStream(resource2.getPath())));
 
         // if ((line = br.readLine()) != null) {
         //     HashMap<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
         //     Map<String, Object> expData = JSONAdapter.fromJSON(line);
         //     data.put("experiments", new ArrayList());
         //     data.put("weathers", new ArrayList());
         //     data.get("experiments").add(expData);
         //     data.get("weathers").add((Map) expData.get("weather"));
         AcePathfinderUtil.insertValue(data, "fen_tot", "110");
         AcePathfinderUtil.insertValue(data, "pdate", "19990415");
         ArrayList<HashMap<String, String>> events = ExperimentHelper.getFertDistribution(data, num, fecd, feacd, fedep, offsets, ptps);
         //Map mgnData = getObjectOr((HashMap) getObjectOr(data, "experiments", new ArrayList()).get(0), "management", new HashMap());
         //ArrayList<Map> events = (ArrayList<Map>) getObjectOr(data, "events", new ArrayList());
         acctual_1 = events.get(0);
         acctual_2 = events.get(1);
         //}
 //        try {
 //            assertEquals("getRootDistribution: fert app 1", expected_1, acctual_1);
 //            assertEquals("getRootDistribution: fert app 2", expected_2, acctual_2);
 //        } catch (Error e) {
 //            log.error(e.getMessage());
 //        }
         assertEquals("getRootDistribution: fert app 1", expected_1, acctual_1);
         assertEquals("getRootDistribution: fert app 2", expected_2, acctual_2);
         log.info("getFertDistribution Output: {}", events.toString());
     }
 
     @Test
     public void testGetOMDistribution() throws IOException, Exception {
         String line;
         String offset = "-7";
         String omcd = "RE003";
         String omc2n = "8.3";
         String omdep = "5";
         String ominp = "50";
         String dmr = "2.5";
         // planting data is 19990415
         // fen_tot is 110
         Map expected_1 = new HashMap();
         expected_1.put("event", "organic_matter");
         expected_1.put("date", "19990408");
         expected_1.put("omcd", "RE003");
         expected_1.put("omamt", "1000");
         expected_1.put("omc2n", "8.3");
         expected_1.put("omdep", "5");
         expected_1.put("ominp", "50");
         expected_1.put("omn%", "4.82");
         Map acctual_1;
 
         // BufferedReader br = new BufferedReader(
         //         new InputStreamReader(
         //         new FileInputStream(resource2.getPath())));
 
         // if ((line = br.readLine()) != null) {
         //     HashMap<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
         //     Map<String, Object> expData = JSONAdapter.fromJSON(line);
         // data.put("experiments", new ArrayList());
         // data.put("weathers", new ArrayList());
         // data.get("experiments").add(expData);
         // data.get("weathers").add((Map) expData.get("weather"));
         // expData.put("omamt", "1000");
         // Map omEvent = new LinkedHashMap();
         // omEvent.put("event", "organic-materials");
         // omEvent.put("date", "19990414");
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", "19990415");
         AcePathfinderUtil.insertValue(data, "omamt", "1000");
         ArrayList<HashMap<String, String>> events = ExperimentHelper.getOMDistribution(data, offset, omcd, omc2n, omdep, ominp, dmr);
         acctual_1 = events.get(1);
         //}
 //        try {
 //            assertEquals("getRootDistribution: om app 1", expected_1, acctual_1);
 //        } catch (Error e) {
 //            log.error(e.getMessage());
 //        }
         assertEquals("getRootDistribution: om app 1", expected_1, acctual_1);
         log.info("getOMDistribution output: {}", data.toString());
     }
 
     @Test
     public void testGetOMDistribution_NoOMData() throws IOException, Exception {
 
         String offset = "-7";
         String omcd = "RE003";
         String omc2n = "8.3";
         String omdep = "5";
         String ominp = "50";
         String dmr = "2.5";
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", "19990415");
         ArrayList<HashMap<String, String>> events = ExperimentHelper.getOMDistribution(data, offset, omcd, omc2n, omdep, ominp, dmr);
 
 //        try {
 //            assertEquals("getRootDistribution: om no data", 1, events.size());
 //        } catch (Error e) {
 //            log.error(e.getMessage());
 //        }
         assertEquals("getRootDistribution: om no data", 1, events.size());
         log.info("getOMDistribution output: {}", data.toString());
     }
 
     @Test
     public void testGetOMDistribution2() throws IOException, Exception {
         String offset = "-7";
         String omcd = "RE003";
         String omc2n = "8.3";
         String omdep = "5";
         String ominp = "50";
         String dmr = "2.5";
 
         Map expected_1 = new HashMap();
         expected_1.put("event", "organic_matter");
         expected_1.put("date", "19990408");
         expected_1.put("omcd", "RE003");
         expected_1.put("omamt", "1000");
         expected_1.put("om_tot", "1000");
         expected_1.put("omc2n", "8.3");
         expected_1.put("omdep", "5");
         expected_1.put("ominp", "50");
         expected_1.put("omn%", "4.82");
         Map acctual_1;
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", "19990415");
         AcePathfinderUtil.insertValue(data, "om_tot", "1000");
         log.debug(AcePathfinder.INSTANCE.getPath("om_tot"));
         ArrayList<HashMap<String, String>> events = ExperimentHelper.getOMDistribution(data, offset, omcd, omc2n, omdep, ominp, dmr);
         acctual_1 = events.get(1);
 
         assertEquals("getRootDistribution: om app 2", expected_1, acctual_1);
         log.info("getOMDistribution output 2: {}", data.toString());
     }
 
     @Test
     public void testGetStableCDistribution() throws IOException, Exception {
         String line;
         String som3_0 = ".55";
         String pp = "20";
         String rd = "60";
         String[] expected = {"1.10", "0.55", "0.65", "0.48", "0.10", "0.10", "0.04", "0.23"};
         ArrayList<String> acctual;
 
 //         BufferedReader br = new BufferedReader(
 //                 new InputStreamReader(
 //                 new FileInputStream(resource.getPath())));
 
 //         if ((line = br.readLine()) != null) {
 //             HashMap<String, Object> data = JSONAdapter.fromJSON(line);
 //             HashMap<String, Object> exp = getRawPackageContents(data, "experiments").get(0);
 //             HashMap<String, Object> icData = (HashMap<String, Object>) getObjectOr(exp, "initial_conditions", new HashMap());
 //             ExperimentHelper.getStableCDistribution(som3_0, pp, rd, data);
 //             acctual = (ArrayList<HashMap<String, String>>) getObjectOr(icData, "soilLayer", new ArrayList());
 
 //             File f = new File("RootDistJson.txt");
 //             BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
 //             bo.write(JSONAdapter.toJSON(data).getBytes());
 //             bo.close();
 // //            f.delete();
 //         }
 
         //}
         HashMap<String, Object> data = new HashMap<String, Object>();
         // AcePathfinderUtil.insertValue(data, "icbl", "5");
         // AcePathfinderUtil.insertValue(data, "icbl", "15");
         // AcePathfinderUtil.insertValue(data, "icbl", "30");
         // AcePathfinderUtil.insertValue(data, "icbl", "60");
         // AcePathfinderUtil.insertValue(data, "icbl", "90");
         // AcePathfinderUtil.insertValue(data, "icbl", "120");
         // AcePathfinderUtil.insertValue(data, "icbl", "150");
         // AcePathfinderUtil.insertValue(data, "icbl", "180");
         AcePathfinderUtil.insertValue(data, "sllb", "5");
         AcePathfinderUtil.insertValue(data, "sloc", "2.00");
         AcePathfinderUtil.insertValue(data, "sllb", "15");
         AcePathfinderUtil.insertValue(data, "sloc", "1.00");
         AcePathfinderUtil.insertValue(data, "sllb", "30");
         AcePathfinderUtil.insertValue(data, "sloc", "1.00");
         AcePathfinderUtil.insertValue(data, "sllb", "60");
         AcePathfinderUtil.insertValue(data, "sloc", "0.50");
         AcePathfinderUtil.insertValue(data, "sllb", "90");
         AcePathfinderUtil.insertValue(data, "sloc", "0.10");
         AcePathfinderUtil.insertValue(data, "sllb", "120");
         AcePathfinderUtil.insertValue(data, "sloc", "0.10");
         AcePathfinderUtil.insertValue(data, "sllb", "150");
         AcePathfinderUtil.insertValue(data, "sloc", "0.04");
         AcePathfinderUtil.insertValue(data, "sllb", "180");
         AcePathfinderUtil.insertValue(data, "sloc", "0.24");
 
         HashMap<String, ArrayList<String>> result = ExperimentHelper.getStableCDistribution(data, som3_0, pp, rd);
         log.info("getStableCDistribution() output: {}", result.toString());
 
 //        Map icData = (HashMap) getObjectOr(data, "soil", new HashMap());
 //        acctual = getObjectOr(icData, "soilLayer", new ArrayList());
         acctual = getObjectOr(result, "slsc", new ArrayList());
         for (int i = 0; i < expected.length; i++) {
 //            try {
 //                assertEquals("getStableCDistribution: normal case " + i, expected[i], (String) acctual.get(i));
 //            } catch (Error e) {
 //                log.error(e.getMessage());
 //            }
             assertEquals("getStableCDistribution: normal case " + i, expected[i], (String) acctual.get(i));
         }
     }
 
     @Test
     public void testGetAutoEventDate() throws IOException, Exception {
 
         ArrayList<ArrayList<HashMap<String, String>>> expected = new ArrayList();
         int expDur = 3;
         HashMap pEvent = new HashMap();
         int pdate = 19820203;
         int edate = 19820204;
         pEvent.put("event", "planting");
         pEvent.put("crid", "MAZ");
         HashMap iEvent = new HashMap();
         int idate = 19820213;
         iEvent.put("event", "irrigation");
         iEvent.put("irop", "ir001");
         HashMap iaEvent = new HashMap();
         int iadate = 19820313;
         iaEvent.put("event", "auto_irrig");
         iaEvent.put("irmdp", "1234");
         HashMap feEvent = new HashMap();
         int fedate = 19820413;
         feEvent.put("event", "fertilizer");
         feEvent.put("fecd", "123");
         HashMap tEvent = new HashMap();
         int tdate = 19820513;
         tEvent.put("event", "tillage");
         tEvent.put("tiimp", "123456");
         HashMap omEvent = new HashMap();
         int omdate = 19820613;
         omEvent.put("event", "organic_matter");
         omEvent.put("omcd", "12345");
         HashMap hEvent = new HashMap();
         int hdate = 19820713;
         hEvent.put("event", "harvest");
         hEvent.put("harm", "111");
         HashMap cEvent = new HashMap();
         int cdate = 19820813;
         cEvent.put("event", "chemicals");
         cEvent.put("chcd", "222");
         HashMap mEvent = new HashMap();
         int mdate = 19820913;
         mEvent.put("event", "mulch");
         mEvent.put("mltp", "333");
 
         for (int i = 0; i < expDur; i++) {
             expected.add(new ArrayList());
             expected.get(i).add(createEvent(pEvent, pdate, i));
             expected.get(i).get(expected.get(i).size() - 1).put("edate", edate + 10000 * i + "");
             expected.get(i).add(createEvent(iEvent, idate, i));
             expected.get(i).add(createEvent(iaEvent, iadate, i));
             expected.get(i).add(createEvent(feEvent, fedate, i));
             expected.get(i).add(createEvent(tEvent, tdate, i));
             expected.get(i).add(createEvent(omEvent, omdate, i));
             expected.get(i).add(createEvent(hEvent, hdate, i));
             expected.get(i).add(createEvent(cEvent, cdate, i));
             expected.get(i).add(createEvent(mEvent, mdate, i));
         }
 
         HashMap<String, Object> data = new HashMap();
         AcePathfinderUtil.insertValue(data, "pdate", "19820203");
         MapUtil.getBucket(data, "management").getDataList().get(0).put("edate", "19820204");
         AcePathfinderUtil.insertValue(data, "crid", "MAZ");
         AcePathfinderUtil.insertValue(data, "idate", "19820213");
         AcePathfinderUtil.insertValue(data, "irop", "ir001");
 //        AcePathfinderUtil.insertValue(data, "iadate", "19820313");
         AcePathfinderUtil.insertValue(data, "irmdp", "1234");
         MapUtil.getBucket(data, "management").getDataList().get(2).put("date", "19820313");
         AcePathfinderUtil.insertValue(data, "fedate", "19820413");
         AcePathfinderUtil.insertValue(data, "fecd", "123");
         AcePathfinderUtil.insertValue(data, "tdate", "19820513");
         AcePathfinderUtil.insertValue(data, "tiimp", "123456");
         AcePathfinderUtil.insertValue(data, "omdat", "19820613");
         AcePathfinderUtil.insertValue(data, "omcd", "12345");
         AcePathfinderUtil.insertValue(data, "hadat", "19820713");
         AcePathfinderUtil.insertValue(data, "harm", "111");
         AcePathfinderUtil.insertValue(data, "cdate", "19820813");
         AcePathfinderUtil.insertValue(data, "chcd", "222");
         AcePathfinderUtil.insertValue(data, "mladat", "19820913");
         AcePathfinderUtil.insertValue(data, "mltp", "333");
         data.put("exp_dur", expDur + "");
 
 //        HashMap dummyEvent = new HashMap();
 //        dummyEvent.put("date", "abc");
 //        MapUtil.getBucket(data, "management").getDataList().add(dummyEvent);
 
         log.info("Inputs: {}", data);
         ArrayList<ArrayList<HashMap<String, String>>> results = ExperimentHelper.getAutoEventDate(data);
         log.info("Results: {}", results);
 
         assertEquals("getAutoPlantingDate: unexpected result", expected, results);
 
     }
 
     @Test
     public void testgetAutoEventDate_machakos_givenPdates() throws IOException, Exception {
 
         log.debug("==testgetAutoEventDate_machakos_givenPdates() Test Start ==");
         ArrayList<ArrayList<HashMap<String, String>>> expected = new ArrayList();
         int expDur = 2;
         HashMap pEvent = new HashMap();
         int pdate = 19820203;
         int edate = 19820206;
         pEvent.put("event", "planting");
         pEvent.put("crid", "MAZ");
         HashMap iEvent = new HashMap();
         int idate = 19820215;
         iEvent.put("event", "irrigation");
         iEvent.put("irop", "ir001");
         HashMap iaEvent = new HashMap();
         int iadate = 19820315;
         iaEvent.put("event", "auto_irrig");
         iaEvent.put("irmdp", "1234");
         HashMap feEvent = new HashMap();
         int fedate = 19820415;
         feEvent.put("event", "fertilizer");
         feEvent.put("fecd", "123");
         HashMap tEvent = new HashMap();
         int tdate = 19820515;
         tEvent.put("event", "tillage");
         tEvent.put("tiimp", "123456");
         HashMap omEvent = new HashMap();
         int omdate = 19820615;
         omEvent.put("event", "organic_matter");
         omEvent.put("omcd", "12345");
         HashMap hEvent = new HashMap();
         int hdate = 19820715;
         hEvent.put("event", "harvest");
         hEvent.put("harm", "111");
         HashMap cEvent = new HashMap();
         int cdate = 19820815;
         cEvent.put("event", "chemicals");
         cEvent.put("chcd", "222");
         HashMap mEvent = new HashMap();
         int mdate = 19820915;
         mEvent.put("event", "mulch");
         mEvent.put("mltp", "333");
 
         for (int i = 0; i < expDur; i++) {
             expected.add(new ArrayList());
             expected.get(i).add(createEvent(pEvent, pdate, i));
             expected.get(i).get(expected.get(i).size() - 1).put("edate", edate + 10000 * i + "");
             expected.get(i).add(createEvent(iEvent, idate, i));
             expected.get(i).add(createEvent(iaEvent, iadate, i));
             expected.get(i).add(createEvent(feEvent, fedate, i));
             expected.get(i).add(createEvent(tEvent, tdate, i));
             expected.get(i).add(createEvent(omEvent, omdate, i));
             expected.get(i).add(createEvent(hEvent, hdate, i));
             expected.get(i).add(createEvent(cEvent, cdate, i));
             expected.get(i).add(createEvent(mEvent, mdate, i));
         }
 
         HashMap<String, Object> data = new HashMap();
         AcePathfinderUtil.insertValue(data, "pdate", "19820203");
         MapUtil.getBucket(data, "management").getDataList().get(0).put("edate", "19820204");
         AcePathfinderUtil.insertValue(data, "crid", "MAZ");
         AcePathfinderUtil.insertValue(data, "idate", "19820213");
         AcePathfinderUtil.insertValue(data, "irop", "ir001");
 //        AcePathfinderUtil.insertValue(data, "iadate", "19820313");
         AcePathfinderUtil.insertValue(data, "irmdp", "1234");
         MapUtil.getBucket(data, "management").getDataList().get(2).put("date", "19820313");
         AcePathfinderUtil.insertValue(data, "fedate", "19820413");
         AcePathfinderUtil.insertValue(data, "fecd", "123");
         AcePathfinderUtil.insertValue(data, "tdate", "19820513");
         AcePathfinderUtil.insertValue(data, "tiimp", "123456");
         AcePathfinderUtil.insertValue(data, "omdat", "19820613");
         AcePathfinderUtil.insertValue(data, "omcd", "12345");
         AcePathfinderUtil.insertValue(data, "hadat", "19820713");
         AcePathfinderUtil.insertValue(data, "harm", "111");
         AcePathfinderUtil.insertValue(data, "cdate", "19820813");
         AcePathfinderUtil.insertValue(data, "chcd", "222");
         AcePathfinderUtil.insertValue(data, "mladat", "19820913");
         AcePathfinderUtil.insertValue(data, "mltp", "333");
         data.put("exp_dur", expDur + "");
         data.put("origin_pdate", "19820201");
         String[] pdates = {"19820203", "19830203"};
 
         log.info("Inputs: {}", data);
         ArrayList<ArrayList<HashMap<String, String>>> results = ExperimentHelper.getAutoEventDate(data, pdates);
         log.info("Results: {}", results);
 
         assertEquals("getAutoPlantingDate: unexpected result", expected, results);
 
     }
 
     private HashMap createEvent(HashMap template, int date, int yearIndex) {
         HashMap event = new HashMap();
         event.putAll(template);
         event.put("date", (date + yearIndex * 10000) + "");
         return event;
     }
 
     @Test
     public void testGetPaddyIrrigation() throws IOException, Exception {
         log.debug("== testGetPaddyIrrigation() Test Start ==");
         String num = "3";
         String percRate = "2";
         String dept = "150";
         String[] offsets = {"-3", "4", "11"};
         String[] maxVals = {"20", "30", "50"};
         String[] minVals = {"5", "10", "15"};
         // planting data is 19990415
         HashMap expected = new HashMap();
         AcePathfinderUtil.insertValue(expected, "idate", "19990412");
         AcePathfinderUtil.insertValue(expected, "irop", "IR010");
         AcePathfinderUtil.insertValue(expected, "irval", "150");
         AcePathfinderUtil.insertValue(expected, "idate", "19990412");
         AcePathfinderUtil.insertValue(expected, "irop", "IR008");
         AcePathfinderUtil.insertValue(expected, "irval", "2");
         AcePathfinderUtil.insertValue(expected, "idate", "19990412");
         AcePathfinderUtil.insertValue(expected, "irop", "IR009");
         AcePathfinderUtil.insertValue(expected, "irval", "20");
         AcePathfinderUtil.insertValue(expected, "idate", "19990412");
         AcePathfinderUtil.insertValue(expected, "irop", "IR011");
         AcePathfinderUtil.insertValue(expected, "irval", "5");
         AcePathfinderUtil.insertValue(expected, "idate", "19990419");
         AcePathfinderUtil.insertValue(expected, "irop", "IR009");
         AcePathfinderUtil.insertValue(expected, "irval", "30");
         AcePathfinderUtil.insertValue(expected, "idate", "19990419");
         AcePathfinderUtil.insertValue(expected, "irop", "IR011");
         AcePathfinderUtil.insertValue(expected, "irval", "10");
         AcePathfinderUtil.insertValue(expected, "idate", "19990426");
         AcePathfinderUtil.insertValue(expected, "irop", "IR009");
         AcePathfinderUtil.insertValue(expected, "irval", "50");
         AcePathfinderUtil.insertValue(expected, "idate", "19990426");
         AcePathfinderUtil.insertValue(expected, "irop", "IR011");
         AcePathfinderUtil.insertValue(expected, "irval", "15");
         ArrayList<HashMap<String, String>> expArr = MapUtil.getBucket(expected, "management").getDataList();
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", "19990415");
         ArrayList<HashMap<String, String>> actArr = ExperimentHelper.getPaddyIrrigation(data, num, percRate, dept, offsets, maxVals, minVals);
 
         assertEquals("getPaddyIrrigation: unexpected output", expArr, actArr);
         log.info("getPaddyIrrigation Output: {}", actArr.toString());
         log.debug("== testGetPaddyIrrigation() Test End ==");
     }
 
     @Test
     public void testCreateEvent() throws IOException, Exception {
         log.debug("== testCreateEvent() Test Start ==");
         String type = "IRRIGATION";
         String dap = "15";
         String pdate = "19820315";
         HashMap<String, String> info = new HashMap();
         info.put("irop", "IR010");
         info.put("irval", "150");
         // planting data is 19990415
         HashMap expected = new HashMap();
         expected.put("date", "19820330");
         expected.put("event", "irrigation");
         expected.putAll(info);
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", pdate);
         HashMap<String, String> actual = ExperimentHelper.createEvent(data, type, dap, info);
 
         assertEquals("createEvent: unexpected output", expected, actual);
         log.info("createEvent Output: {}", actual.toString());
         log.debug("== testCreateEvent() Test End ==");
     }
 
     @Test
     public void testCreateEvent_invalidType() throws IOException, Exception {
         log.debug("== testCreateEvent_invalidType() Test Start ==");
        String type = "Irrigation";
         String dap = "15";
         String pdate = "19820315";
         HashMap<String, String> info = new HashMap();
         info.put("irop", "IR010");
         info.put("irval", "150");
         // planting data is 19990415
         HashMap expected = new HashMap();
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", pdate);
         HashMap<String, String> actual = ExperimentHelper.createEvent(data, type, dap, info);
 
         assertEquals("createEvent: unexpected output", expected, actual);
         log.info("createEvent Output: {}", actual.toString());
         log.debug("== testCreateEvent_invalidType() Test End ==");
     }
 
     @Test
     public void testCreateEvent_InvalidDap() throws IOException, Exception {
         log.debug("== testCreateEvent_InvalidDap() Test Start ==");
         String type = "IRRIGATION";
         String dap = "a";
         String pdate = "19820315";
         HashMap<String, String> info = new HashMap();
         info.put("irop", "IR010");
         info.put("irval", "150");
         // planting data is 19990415
         HashMap expected = new HashMap();
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", pdate);
         HashMap<String, String> actual = ExperimentHelper.createEvent(data, type, dap, info);
 
         assertEquals("createEvent: unexpected output", expected, actual);
         log.info("createEvent Output: {}", actual.toString());
         log.debug("== testCreateEvent_InvalidDap() Test End ==");
     }
 
     @Test
     public void testCreateEvent_invalidPdate() throws IOException, Exception {
         log.debug("== testCreateEvent_invalidPdate() Test Start ==");
        String type = "IRRIGATION";
         String dap = "15";
         String pdate = "";
         HashMap<String, String> info = new HashMap();
         info.put("irop", "IR010");
         info.put("irval", "150");
         // planting data is 19990415
         HashMap expected = new HashMap();
 
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "pdate", pdate);
         HashMap<String, String> actual = ExperimentHelper.createEvent(data, type, dap, info);
 
         assertEquals("createEvent: unexpected output", expected, actual);
         log.info("createEvent Output: {}", actual.toString());
         log.debug("== testCreateEvent_invalidPdate() Test End ==");
     }
 }
