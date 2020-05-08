 package ee.alkohol.juks.sirvid.exporters;
 
 import ee.alkohol.juks.sirvid.containers.ICalculator;
 import ee.alkohol.juks.sirvid.containers.ICalendar;
 
 public class ExporterSVG extends Exporter {
     
     public ExporterSVG () {
         super();
         this.setFileExtension(".svg");
         this.setMimeType("image/svg+xml");
     }
     
     @Override
     public String generate(ICalculator icalendar) {
         
         StringBuilder sb = new StringBuilder();
         
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\n");
         sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
        sb.append("<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" >\n");
         
         sb.append("<style type=\"text/css\">\n");
         sb.append("<![CDATA[\n");
         sb.append("line {stroke: black; stroke-width : 1; }\n");                  
         sb.append("polyline, path { stroke: black; stroke-width : 1; fill: none; }\n");
         sb.append("]]>\n");
         sb.append("</style>\n");
         
         sb.append("<title>");
         sb.append(icalendar.iCal.iCalBody.get(ICalendar.Keys.CALENDAR_NAME).value);
         sb.append("</title>\n");
         
         sb.append("</svg>");
         return sb.toString();
     }
     
 }
