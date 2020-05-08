 package gov.usgs.cida.coastalhazards.sld;
 
 import com.google.gson.Gson;
 import com.sun.jersey.api.view.Viewable;
 import gov.usgs.cida.coastalhazards.model.Item;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import javax.ws.rs.core.Response;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 public final class Extreme extends SLDGenerator {
 
     private String[] attrs = {"EXTREME1", "EXTREME2", "EXTREME3", "EXTREME4", "EXTREME5"};
     public final int STROKE_WIDTH = 3;
     public final int STROKE_OPACITY = 1;
     private float[] thresholds = {3.5f, 5.0f, 6.5f, 8.0f};
    private String[] colors = {"#DFB8E6", "#C78CEB", "#AC64EE", "#9040F1", "#6F07F3"};
     private int binCount = colors.length;
     
     public Extreme(Item item) {
         super(item);
     }
     
     @Override
     public String[] getAttrs() {
         return this.attrs;
     }
     
     @Override
     public Response generateSLD() {
         return Response.ok(new Viewable("/extreme.jsp", this)).build();
     }
     
     @Override
     public Response generateSLDInfo() {
         Map<String, Object> sldInfo = new LinkedHashMap<String, Object>();
         sldInfo.put("title", item.getSummary().getTiny().getText());
         sldInfo.put("units", "m");
         List<Map<String,Object>> bins = new ArrayList<Map<String,Object>>();
         for (int i=0; i<binCount; i++) {
             Map<String, Object> binMap = new LinkedHashMap<String,Object>();
             if (i > 0) {
                 binMap.put("lowerBound", thresholds[i]);
             }
             if (i+1 < binCount) {
                 binMap.put("upperBound", thresholds[i+1]);
             }
             binMap.put("color", colors[i]);
             bins.add(binMap);
         }
         sldInfo.put("bins", bins);
         String toJson = new Gson().toJson(sldInfo, HashMap.class);
         return Response.ok(toJson).build();
     }
     
     public String getId() {
         return item.getWmsService().getLayers();
     }
 
     public String getAttr() {
         return item.getAttr();
     }
 
     public float[] getThresholds() {
         return thresholds;
     }
 
     public String[] getColors() {
         return colors;
     }
     
     public int getBinCount() {
         return binCount;
     }
 
     public int getSTROKE_WIDTH() {
         return STROKE_WIDTH;
     }
 
     public int getSTROKE_OPACITY() {
         return STROKE_OPACITY;
     }
 }
