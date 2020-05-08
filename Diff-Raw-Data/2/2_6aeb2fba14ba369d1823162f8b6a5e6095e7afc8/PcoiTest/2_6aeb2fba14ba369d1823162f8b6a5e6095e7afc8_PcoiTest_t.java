 package gov.usgs.cida.coastalhazards.sld;
 
 import com.google.gson.Gson;
 import com.sun.jersey.api.view.Viewable;
 import gov.usgs.cida.coastalhazards.model.Item;
 import gov.usgs.cida.coastalhazards.model.ogc.WMSService;
 import gov.usgs.cida.coastalhazards.model.summary.Summary;
 import gov.usgs.cida.coastalhazards.model.summary.Tiny;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.core.Response;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jiwalker
  */
 public class PcoiTest {
     
     /**
      * Test of generateSLDInfo method, of class Pcoi.
      */
     @Test
     public void testGenerateSLDInfo() {
         Item item = new Item();
         item.setAttr("PCOL1");
         item.setId("abcde");
         item.setType(Item.Type.storms);
         WMSService wmsService = new WMSService();
         wmsService.setLayers("0");
         item.setWmsService(wmsService);
         Summary summary = new Summary();
         Tiny tiny = new Tiny();
         tiny.setText("Pcoi");
         summary.setTiny(tiny);
         item.setSummary(summary);
         
         Pcoi pcoi = new Pcoi(item);
         Response response = pcoi.generateSLDInfo();
         String json = (String)response.getEntity();
         Map<String, Object> sldInfo = new Gson().fromJson(json, HashMap.class);
         List<Object> bins = (List)sldInfo.get("bins");
         Map<String,Object> bin = (Map)bins.get(0);
         Double lowerBound = (Double)bin.get("lowerBound");
         String color = (String)bin.get("color");
         assertEquals(lowerBound, 0.0f, 0.01f);
        assertEquals(color, "#FFFFFE");
     }
     
     @Test
     public void testGenerateSLD() {
         Item item = new Item();
         item.setAttr("PCOL1");
         item.setId("abcde");
         item.setType(Item.Type.storms);
         WMSService wmsService = new WMSService();
         wmsService.setLayers("0");
         item.setWmsService(wmsService);
         Summary summary = new Summary();
         Tiny tiny = new Tiny();
         tiny.setText("Pcoi");
         summary.setTiny(tiny);
         item.setSummary(summary);
         
         Pcoi pcoi = new Pcoi(item);
         Response response = pcoi.generateSLD();
         Viewable sld = (Viewable)response.getEntity();
     }
 
 }
