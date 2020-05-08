 package org.agmip.functions;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import org.agmip.util.JSONAdapter;
 import static org.agmip.util.MapUtil.*;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  *
  * @author Meng Zhang
  */
 public class ExperimentHelperTest {
 
     URL resource;
     URL resource2;
 
     @Before
     public void setUp() throws Exception {
         resource = this.getClass().getResource("/ufga8201_multi.json");
         resource2 = this.getClass().getResource("/machakos.json");
     }
 
     @Test
     public void testGetAutoPlantingDate() throws IOException, Exception {
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "29.2";
         String dayNum = "4";
         String expected_1 = "19820204";
         int expected_2 = 5;
         String acctual_1 = "";
         int acctual_2 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(resource.getPath())));
 
         if ((line = br.readLine()) != null) {
            HashMap<String, Object> data = JSONAdapter.fromJSON(line);
            Map<String, Object> expData = getRawPackageContents(data, "experiments").get(0);        
            // Map<String, Object> expData = (Map)((ArrayList) data.get("experiments")).get(0);
             expData.put("exp_dur", "2");
             ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
             Map<String, ArrayList> mgnData = (Map) expData.get("management");
             ArrayList<Map<String, String>> events = mgnData.get("events");
             acctual_1 = events.get(0).get("date");
             acctual_2 = events.size();
         }
 
         assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoPlantingDate: no date find case", expected_2, acctual_2);
 
     }
 
     @Test
     public void testGetAutoPlantingDate_oneYear() throws IOException, Exception {
         String line;
         String startDate = "03-01";
         String endDate = "04-01";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19990310"; // TODO
         int expected_2 = 2;
         String acctual_1 = "";
         int acctual_2 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(resource2.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             Map<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
             Map<String, Object> expData = JSONAdapter.fromJSON(line);
             data.put("experiments", new ArrayList());
             data.put("weathers", new ArrayList());
             data.get("experiments").add(expData);
             data.get("weathers").add((Map) expData.get("weather"));
             expData.put("sc_year", "1982");
             ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
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
                 new FileInputStream(resource2.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             Map<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
             Map<String, Object> expData = JSONAdapter.fromJSON(line);
             data.put("experiments", new ArrayList());
             data.put("weathers", new ArrayList());
             data.get("experiments").add(expData);
             data.get("weathers").add((Map) expData.get("weather"));
             expData.put("exp_dur", "3");
             ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
             Map<String, ArrayList> mgnData = (Map) expData.get("management");
             ArrayList<Map<String, String>> events = mgnData.get("events");
             acctual_1 = events.get(0).get("date");
             acctual_2 = events.get(1).get("date");
             acctual_3 = events.size();
         }
 
         assertEquals("getAutoPlantingDate: normal case", expected_1, acctual_1);
         assertEquals("getAutoPlantingDate: copy case", expected_2, acctual_2);
         assertEquals("getAutoPlantingDate: no date find case", expected_3, acctual_3);
 
     }
 
     @Test
     public void testGetAutoPlantingDate_machakos_scYear() throws IOException, Exception {
         String line;
         String startDate = "01-15";
         String endDate = "02-28";
         String accRainAmt = "9.0";
         String dayNum = "6";
         String expected_1 = "19830214";
         String expected_2 = "19840131";
         String expected_3 = "19850202";
         int expected_99 = 4;
         String acctual_1 = "";
         String acctual_2 = "";
         String acctual_3 = "";
         int acctual_99 = 0;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(resource2.getPath())));
 
         if ((line = br.readLine()) != null) {
 
             Map<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
             Map<String, Object> expData = JSONAdapter.fromJSON(line);
             data.put("experiments", new ArrayList());
             data.put("weathers", new ArrayList());
             data.get("experiments").add(expData);
             data.get("weathers").add((Map) expData.get("weather"));
             expData.put("exp_dur", "3");
             expData.put("sc_year", "1983");
             ExperimentHelper.getAutoPlantingDate(startDate, endDate, accRainAmt, dayNum, data);
             Map<String, ArrayList> mgnData = (Map) expData.get("management");
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
         Map acctual_1 = null;
         Map acctual_2 = null;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(resource2.getPath())));
 
         if ((line = br.readLine()) != null) {
             HashMap<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
             Map<String, Object> expData = JSONAdapter.fromJSON(line);
             data.put("experiments", new ArrayList());
             data.put("weathers", new ArrayList());
             data.get("experiments").add(expData);
             data.get("weathers").add((Map) expData.get("weather"));
             ExperimentHelper.getFertDistribution(num, fecd, feacd, fedep, offsets, ptps, data);
             Map mgnData = getObjectOr((HashMap) getObjectOr(data, "experiments", new ArrayList()).get(0), "management", new HashMap());
             ArrayList<Map> events = getObjectOr(mgnData, "events", new ArrayList());
             acctual_1 = events.get(1);
             acctual_2 = events.get(2);
         }
         assertEquals("getRootDistribution: fert app 1", expected_1, acctual_1);
         assertEquals("getRootDistribution: fert app 2", expected_2, acctual_2);
     }
 
     @Test
     public void testGetOMDistribution() throws IOException, Exception {
         String line;
         String offset = "-7";
         String omcd = "RE003";
         String omc2n = "8.3";
         String omdep = "5";
         String ominp = "50";
         // planting data is 19990415
         // fen_tot is 110
         Map expected_1 = new HashMap();
         expected_1.put("event", "organic-materials");
         expected_1.put("date", "19990408");
         expected_1.put("omcd", "RE003");
         expected_1.put("omamt", "1000");
         expected_1.put("omc2n", "8.3");
         expected_1.put("omdep", "5");
         expected_1.put("ominp", "50");
         Map acctual_1 = null;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(resource2.getPath())));
 
         if ((line = br.readLine()) != null) {
             HashMap<String, ArrayList<Map>> data = new LinkedHashMap<String, ArrayList<Map>>();
             Map<String, Object> expData = JSONAdapter.fromJSON(line);
             data.put("experiments", new ArrayList());
             data.put("weathers", new ArrayList());
             data.get("experiments").add(expData);
             data.get("weathers").add((Map) expData.get("weather"));
             expData.put("omamt", "1000");
             Map omEvent = new LinkedHashMap();
             omEvent.put("event", "organic-materials");
             omEvent.put("date", "19990414");
             Map mgnData = getObjectOr((HashMap) getObjectOr(data, "experiments", new ArrayList()).get(0), "management", new HashMap());
             ArrayList<Map> events = getObjectOr(mgnData, "events", new ArrayList());
             events.add(0, omEvent);
 
             ExperimentHelper.getOMDistribution(offset, omcd, omc2n, omdep, ominp, data);
             acctual_1 = events.get(0);
         }
         assertEquals("getRootDistribution: om app 1", expected_1, acctual_1);
     }
 
     @Test
     public void testGetStableCDistribution() throws IOException, Exception {
         String line;
         String som3_0 = ".55";
         String pp = "20";
         String rd = "60";
         String[] expected = {"1.10", "0.55", "0.65", "0.48", "0.10", "0.10", "0.04", "0.23"};
        ArrayList<HashMap<String, String>> acctual = null;
 
         BufferedReader br = new BufferedReader(
                 new InputStreamReader(
                 new FileInputStream(resource.getPath())));
 
         if ((line = br.readLine()) != null) {
            HashMap<String, Object> data = JSONAdapter.fromJSON(line);
            HashMap<String, Object> exp = getRawPackageContents(data, "experiments").get(0);
            HashMap<String, Object> icData = (HashMap<String, Object>) getObjectOr(exp, "initial_conditions", new HashMap());
             ExperimentHelper.getStableCDistribution(som3_0, pp, rd, data);
            acctual = (ArrayList<HashMap<String, String>>) getObjectOr(icData, "soilLayer", new ArrayList());
             
             File f = new File("RootDistJson.txt");
             BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
             bo.write(JSONAdapter.toJSON(data).getBytes());
             bo.close();
 //            f.delete();
         }
 
         for (int i = 0; i < expected.length; i++) {
             assertEquals("getRootDistribution: normal case " + i, expected[i], (String) acctual.get(i).get("slsc"));
         }
     }
 }
