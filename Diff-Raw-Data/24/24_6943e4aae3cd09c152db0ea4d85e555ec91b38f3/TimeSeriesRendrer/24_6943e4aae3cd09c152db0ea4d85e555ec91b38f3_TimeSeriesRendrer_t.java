 package no.yr.svg;
 
 import java.util.Iterator;
 
 import no.yr.weather.TimeSeries;
 import no.yr.weather.Weather;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 public class TimeSeriesRendrer {
 
     protected TimeSeries series;
     float min = 0;
     float max = 0;
     int magnitude = -3;
 
     public TimeSeriesRendrer(TimeSeries series) {
         this.series = series;
     }
 
     public Document getSvg() {
         Element frame = new Element("svg");
         Document svg = new Document(frame);
 
         frame.setAttribute("height", "200");
 
         Iterator<Weather> it = series.getIterator();
         int count = 0;
         while (it.hasNext()) {
             Weather weather = it.next();
             Document partSvg = new WeatherRendrer(weather).getSvg();
             Element subFrame = partSvg.detachRootElement();
             subFrame.setName("g");
             subFrame.setAttribute("transform", "translate(" + count * 30 + ",0)");
 
             frame.addContent(subFrame);
 
             count++;
         }
 
         addTempratureGraf(frame);
 
 
         return svg;
 
 
     }
 
     public String getXml() {
         Document svg = getSvg();
 
         XMLOutputter output = new XMLOutputter();
         return output.outputString(svg.getRootElement());
 
     }
 
     private void addTempratureGraf(Element svg) {
         Element graf = new Element("polyline");
         int zero = 90;
         int left = 15;
 
         String points = "";
         Iterator<Weather> it = series.getIterator();
         while (it.hasNext()) {
             Weather weather = it.next();
             points += " " + left + "," + (int) ((weather.getTempratur().getTemperatur() * magnitude) + zero);
 
             if (weather.getTempratur().getTemperatur() < min) {
                 min = weather.getTempratur().getTemperatur();
             }
 
             if (weather.getTempratur().getTemperatur() > max) {
                 max = weather.getTempratur().getTemperatur();
             }
 
             left += 30;
         }
 
         Element tempFrame = new Element("g");
 
         Element zeroLine = generateZeroLine(zero, left);
         tempFrame.addContent(generateTempGrid(left));
         tempFrame.addContent(zeroLine);
 
         if (max > 0) {
             Element maxLine = new Element("rect");
             maxLine.setAttribute("y", "" + (zero + (max * magnitude)));
             maxLine.setAttribute("x", "0");
             maxLine.setAttribute("width", "" + (left - 15));
             maxLine.setAttribute("height", "0.2");
             maxLine.setAttribute("stroke", "green");
             tempFrame.addContent(maxLine);
 
             Element maxTempText = new Element("text");
             maxTempText.setAttribute("y", "" + (zero + (max * magnitude) + 5));
             maxTempText.setAttribute("x", "" + (left - 10));
 
             maxTempText.setText("max " + max + "c");
             tempFrame.addContent(maxTempText);
 
         }
 
         if (min < 0) {
             Element minLine = new Element("rect");
             minLine.setAttribute("y", "" + (zero + (min * magnitude)));
             minLine.setAttribute("x", "0");
             minLine.setAttribute("width", "" + (left - 15));
             minLine.setAttribute("height", "0.2");
             minLine.setAttribute("stroke", "green");
             tempFrame.addContent(minLine);
 
             Element mintempText = new Element("text");
             mintempText.setAttribute("y", "" + (zero + (min * magnitude) + 5));
             mintempText.setAttribute("x", "" + (left - 10));
 
             mintempText.setText("min " + min + "c");
 
             tempFrame.addContent(mintempText);
         }
 
 
         moveTempGraphRelative(tempFrame);
         tempFrame.addContent(graf);
 
         graf.setAttribute("points", points.trim());
         graf.setAttribute("stroke", "red");
         graf.setAttribute("fill", "none");
 
        tempFrame.setAttribute("transform", "translate(0,"+((min+max)/2)*1.4 +")");

         svg.addContent(tempFrame);
     }
 
     private Element generateZeroLine(int zero, int left) {
         Element zeroLine= new Element("rect");
         zeroLine.setAttribute("y", "" + zero);
         zeroLine.setAttribute("x", "0");
         zeroLine.setAttribute("width", "" + (left - 15));
         zeroLine.setAttribute("height", "0.5");
         zeroLine.setAttribute("stroke", "black");
 
         return zeroLine;
     }
 
     private Element generateTempGrid(int left)
     {
         Element grid = new Element("g");
         
         int temp = 15;
         Element gridLine= new Element("rect");
         gridLine.setAttribute("y", "" + temp * magnitude);
         gridLine.setAttribute("x", "0");
         gridLine.setAttribute("width", "" + (left - 15));
         gridLine.setAttribute("height", "0.5");
         gridLine.setAttribute("stroke", "gray");
 
         grid.addContent(gridLine);
 
         return grid;
     }
 
     private void moveTempGraphRelative(Element tempGraph) {
         int medianTemp = (int) (max - min) / 2;
         tempGraph.setAttribute("transform", "translate(0," + medianTemp * magnitude + ")");
     }
 }
