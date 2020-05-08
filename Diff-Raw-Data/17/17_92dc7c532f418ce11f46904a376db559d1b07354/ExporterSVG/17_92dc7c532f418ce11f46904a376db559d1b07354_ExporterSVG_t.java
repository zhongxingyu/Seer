 package ee.alkohol.juks.sirvid.exporters;
 
 import java.text.SimpleDateFormat;
 
 import ee.alkohol.juks.sirvid.containers.graphics.SirvidDay;
 import ee.alkohol.juks.sirvid.containers.graphics.SirvidMonth;
 import ee.alkohol.juks.sirvid.containers.graphics.SirvidRune;
 import ee.alkohol.juks.sirvid.containers.graphics.SirvidSVG;
 import ee.alkohol.juks.sirvid.containers.ical.ICalculator;
 import ee.alkohol.juks.sirvid.containers.ical.ICalendar;
 
 public class ExporterSVG extends Exporter {
 	
 	public static final String SDF_DATE = "dd.MM";
 	
     public ExporterSVG () {
         super();
         this.setFileExtension(".svg");
         this.setMimeType("image/svg+xml");
     }
     
     @Override
     public String generate(ICalculator iC) {
         
         SirvidSVG sSVG = new SirvidSVG(iC);
         
         SimpleDateFormat dateFormat = new SimpleDateFormat(SDF_DATE);
         StringBuilder sb = new StringBuilder();
         
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
         sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
         sb.append("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n");
         sb.append("<title>");
         sb.append(iC.iCal.iCalBody.get(ICalendar.Keys.CALENDAR_NAME).value);
         sb.append(" (under construction)</title>\n");
         
         if(sSVG.errorMsgs.size() > 0) {
         	
             sb.append(SirvidSVG.errorTxtTags[0]);
             for(String errMsg : sSVG.errorMsgs) {
                 sb.append(errMsg);
                 sb.append("\n");
             }
             sb.append(SirvidSVG.errorTxtTags[1]);
             
         } else {
         	
         	// stylesheet 
         	String strokes = "stroke: " + sSVG.props.getProperty("strokeColor") + "; stroke-width : " + sSVG.props.getProperty("strokeWidth") + ";";
             sb.append("\n<style type=\"text/css\">\n");
             sb.append("<![CDATA[\n");
             sb.append("line {");
             sb.append(strokes);
             sb.append(" }\n");
             sb.append("polyline, path { ");
             sb.append(strokes);
             sb.append(" fill: none; }\n");
             sb.append("]]>\n");
             sb.append("</style>\n");
             
             // define runes
             sb.append("\n<defs>\n");
             for(Integer sRdbID : sSVG.runes.keySet()) {
             	SirvidRune sR = sSVG.runes.get(sRdbID);
             	sb.append("<g id=\"");
             	sb.append(sR.getFilename());
             	sb.append("\">\n");
             	sb.append(sR.getSvgContent());
             	sb.append("</g>\n");
             }
             sb.append("</defs>\n\n");
             
             // scaling
             //sb.append("<g transform=\"scale(0.5)\">\n");
             
             for(SirvidMonth sM : sSVG.months) {
             	for(SirvidDay sD : sM.days){
             		
             		//weekdays
             		sb.append("\n<g transform=\"translate(");
             		sb.append(sD.beginX);
             		sb.append(" 20)\">\n");
             		sb.append("<title content=\"structured text\">");
            		sb.append(dateFormat.format(sD.date));
             		sb.append("\n");
             		sb.append(sSVG.weekDays[sD.weekDay]);
             		sb.append("</title>\n");
             		sb.append("<use xlink:href=\"#");
             		sb.append(sSVG.runes.get(new Integer(sD.weekDay)).getFilename());
             		sb.append("\"/>\n</g>\n");
             	}
 
             }
             
             
         }
         
         // scaling
         //sb.append("\n</g>\n");
         
         sb.append("</svg>");
         return sb.toString();
     }
     
 }
