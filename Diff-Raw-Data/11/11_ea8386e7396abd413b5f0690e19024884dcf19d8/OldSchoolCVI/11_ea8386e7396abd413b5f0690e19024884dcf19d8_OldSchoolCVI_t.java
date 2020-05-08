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
 public final class OldSchoolCVI extends SLDGenerator {
 
     private static final String[] attrs = {"TIDERISK", "SLOPERISK", "ERRRISK", "SLRISK", "GEOM", "WAVERISK", "CVIRISK"};
     private static final int STROKE_WIDTH = 3;
     private static final int STROKE_OPACITY = 1;
     private static final float[] thresholds = {1.0f, 2.0f, 3.0f, 4.0f, 5.0f};
     private static final String[] colors = {"#006945", "#3B6800", "#FFFF00", "#FEAC00", "#FF0000"};
     private static final String[] categories = {"Very Low", "Low", "Moderate", "High", "Very High"};
     
     public OldSchoolCVI(Item item) {
         super(item);
     }
     
     @Override
     public String[] getAttrs() {
         return attrs;
     }
     
     @Override
     public Response generateSLD() {
        // old school is actually line, but to render bayes categorical, need it to be point
        //return Response.ok(new Viewable("/categorical_line.jsp", this)).build();
        return Response.ok(new Viewable("/categorical_point.jsp", this)).build();
     }
     
     @Override
     public Response generateSLDInfo() {
         Map<String, Object> sldInfo = new LinkedHashMap<String, Object>();
         sldInfo.put("title", item.getSummary().getTiny().getText());
         sldInfo.put("units", "");
         sldInfo.put("style", getStyle());
         List<Map<String,Object>> bins = new ArrayList<Map<String,Object>>();
         for (int i=0; i<getBinCount(); i++) {
             Map<String, Object> binMap = new LinkedHashMap<String,Object>();
             binMap.put("category", categories[i]);
             binMap.put("color", getColors()[i]);
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
         return colors.length;
     }
 
     @Override
     public String getStyle() {
         return style;
     }
 
     public int getSTROKE_WIDTH() {
         return STROKE_WIDTH;
     }
 
     public int getSTROKE_OPACITY() {
         return STROKE_OPACITY;
     }
 }
