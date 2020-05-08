 package gov.usgs.cida.coastalhazards.rest;
 
 import java.awt.Color;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 /**
  *
  * @author Jordan Walker <jiwalker@usgs.gov>
  */
 @Path("sld")
 public class SLDResource {
     
     private static final int NUMBER_OF_BINS = 10;
     private static final int STROKE_WIDTH = 1;
 
     @GET
     @Path("redwhite/{id}/{attr}")
     @Produces(MediaType.APPLICATION_XML)
     public String getRedWhiteSLD(
             @PathParam("id") String id,
             @PathParam("attr") String attr) {
         String result = "";
         
        float[] thresholds = makeSteps(0.0f, 100.0f, NUMBER_OF_BINS);
        String[] colors = makeColors("#FFFFFF", "#FF0000", NUMBER_OF_BINS-1);
         
         StringBuilder sld = new StringBuilder();
        sld.append("<sld:StyledLayerDescriptor")
             .append(" xmlns=\"http://www.opengis.net/ogc\"")
             .append(" xmlns:sld=\"http://www.opengis.net/sld\"")
             .append(" xmlns:ogc=\"http://www.opengis.net/ogc\"")
             .append(" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"")
             .append(" version=\"1.0.0\"")
             .append(" xsi:schemaLocation=\"http://www.opengis.net/sld http://schemas.opengis.net/sld/1.0.0/StyledLayerDescriptor.xsd\">")
 
             .append("<sld:NamedLayer>")
             .append("<sld:Name>")
                 .append(id)
             .append("</sld:Name>")
             .append("<sld:UserStyle>")
             .append("<sld:Name>redWhiteMap</sld:Name>")
             .append("<sld:Title>Red to White ColorMap</sld:Title>")
             .append("<sld:FeatureTypeStyle>");
         
       for (int i=0; i<NUMBER_OF_BINS-1; i++) {
             sld.append("<sld:Rule>")
            .append("<sld:Filter>")
             .append("<ogc:PropertyIsGreaterThanOrEqualTo>")
             .append("<ogc:PropertyName>")
                 .append(attr)
             .append("</ogc:PropertyName>")
             .append("<ogc:Literal>")
                 .append(thresholds[i])
             .append("</ogc:Literal>")
             .append("</ogc:PropertyIsGreaterThanOrEqualTo>")
             .append("<ogc:PropertyIsLessThan>")
             .append("<ogc:PropertyName>")
                 .append(attr)
             .append("</ogc:PropertyName>")
             .append("<ogc:Literal>")
                 .append(thresholds[i+1])
             .append("</ogc:Literal>")
             .append("</ogc:PropertyIsLessThan>")
            .append("</sld:Filter>")
             .append("<sld:LineSymbolizer>")
             .append("<sld:Stroke>")
             .append("<sld:CssParameter name=\"stroke\">")
                 .append(colors[i])
             .append("</sld:CssParameter>")
             .append("<sld:CssParameter name=\"stroke-width\">")
                 .append(STROKE_WIDTH)
             .append("</sld:CssParameter>")
             .append("</sld:Stroke>")
             .append("</sld:LineSymbolizer>")
             .append("</sld:Rule>");
         }
        
         sld.append("</sld:FeatureTypeStyle>") 
             .append("</sld:UserStyle>")
             .append("</sld:NamedLayer>")
             .append("</sld:StyledLayerDescriptor>");
                 
             result = sld.toString();
             return result;
     }
     
     protected static float[] makeSteps(float start, float end, int count) {
         float[] steps = new float[count];
         if (count < 2) {
             throw new IllegalArgumentException("Count must be 2 or more");
         }
         // range is negative if end < start
         float range = end - start;
         float stepSize = range / (count - 1);
         for (int i=0; i<count; i++) {
             steps[i] = start + (stepSize * i);
         }
         return steps;
     }
     
     protected static String[] makeColors(String hexStart, String hexEnd, int count) {
         String[] result = new String[count];
         Color startColor = Color.decode(hexStart);
         Color endColor = Color.decode(hexEnd);
         float[] reds = makeSteps((float)startColor.getRed(), (float)endColor.getRed(), count);
         float[] greens = makeSteps((float)startColor.getGreen(), (float)endColor.getGreen(), count);
         float[] blues = makeSteps((float)startColor.getBlue(), (float)endColor.getBlue(), count);
         for (int i=0; i<count; i++) {
             int rgb = (int)blues[i] | ((int)greens[i] << 8) | ((int)reds[i] << 16);
             String gradient = String.format("#%06X", (0xFFFFFF & rgb));
             result[i] = gradient;
         }
         return result;
     }
 }
