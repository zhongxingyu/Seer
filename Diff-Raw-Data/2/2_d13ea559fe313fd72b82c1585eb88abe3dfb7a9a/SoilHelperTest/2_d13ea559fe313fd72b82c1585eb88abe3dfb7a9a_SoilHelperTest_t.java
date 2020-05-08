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
 import org.agmip.util.JSONAdapter;
 import static org.agmip.util.MapUtil.*;
 import static org.junit.Assert.*;
 import org.agmip.ace.util.AcePathfinderUtil;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Meng Zhang
  */
 public class SoilHelperTest {
 
     URL resource;
     private static final Logger log = LoggerFactory.getLogger(SoilHelperTest.class);
 
 
     @Before
     public void setUp() throws Exception {
         resource = this.getClass().getResource("/ufga8201_multi.json");
     }
 
     @Test
     public void testGetRootDistribution() throws IOException, Exception {
         String line;
         String m = "1";
         String pp = "20";
         String rd = "180";
         String[] expected = {"1.000", "1.000", "0.941", "0.543", "0.261", "0.125", "0.060", "0.029"};
         ArrayList<HashMap> acctual = null;
 
         // BufferedReader br = new BufferedReader(
         //         new InputStreamReader(
         //         new FileInputStream(resource.getPath())));
 
         // if ((line = br.readLine()) != null) {
         //     HashMap data = JSONAdapter.fromJSON(line);
         //     SoilHelper.getRootDistribution(m, pp, rd, data);
         //     acctual = getObjectOr((HashMap) getObjectOr(data, "soils", new ArrayList()).get(0), "soilLayer", new ArrayList());
 //            File f = new File("RootDistJson.txt");
 //            BufferedOutputStream bo = new BufferedOutputStream(new FileOutputStream(f));
 //            bo.write(JSONAdapter.toJSON(data).getBytes());
 //            bo.close();
 //            f.delete();
         // }
 
         // for (int i = 0; i < expected.length; i++) {
             // assertEquals("getRootDistribution: normal case", expected[i], (String) acctual.get(i).get("slrgf"));
         // }
         HashMap<String, Object> data = new HashMap<String, Object>();
         AcePathfinderUtil.insertValue(data, "sllb", "5");
         AcePathfinderUtil.insertValue(data, "sllb", "15");
         AcePathfinderUtil.insertValue(data, "sllb", "30");
         AcePathfinderUtil.insertValue(data, "sllb", "60");
         AcePathfinderUtil.insertValue(data, "sllb", "90");
         AcePathfinderUtil.insertValue(data, "sllb", "120");
         AcePathfinderUtil.insertValue(data, "sllb", "150");
         AcePathfinderUtil.insertValue(data, "sllb", "180");
 
        SoilHelper.getRootDistribution("slrgf", m, pp, rd, data);
         log.info("getRootDistribution() output: {}", data.toString());
     }
 }
