 package no.yr.svg;
 
 import java.util.Calendar;
 
 import no.yr.weather.Rain;
 import no.yr.weather.Weather;
 import no.yr.weather.Wind;
 
 import org.jdom.Document;
 import org.jdom.Element;
 
 /**
  * rendrers symbols and svg parts
  * @author aslak
  *
  */
 public class WeatherRendrer {
 	Element svg = new Element("svg");
 	Document doc = new Document(svg);
 	
 	Weather weather;
 	
 	public WeatherRendrer(Weather weather)
 	{
 		this.weather = weather;
 	}
 	
 	/**
 	 * returns the svg xml document for this weather 
 	 * @return
 	 */
 	public Document getSvg()
 	{
 			
 		svg.setAttribute("height", "130");
 		svg.setAttribute("width", "30");
 		
 		renderCloud(1, weather.getClouds().getHigh());
 		renderCloud(2, weather.getClouds().getMedium());
 		renderCloud(3, weather.getClouds().getLow());
 		
 		renderWind(weather.getWind());
 		
 		renderRain(weather.getRain());
 		
 		Element time =  new Element("text");
 		time.setText(weather.getValid().getFrom().get(Calendar.HOUR_OF_DAY)
 				+ "/" + 
 				weather.getValid().getFrom().get(Calendar.DAY_OF_MONTH));
 		
 		time.setAttribute("x", "2");
 		time.setAttribute("y", "120");
 		svg.addContent(time);
 		
 		return doc;
 	}
 	
 	private void renderRain(Rain rain) {
 
         float rainBarHeight = rain.getAmount()*2;
 
 		//box
 		Element mmBox = new Element("rect");
 		mmBox.setAttribute("x", "3");
 		mmBox.setAttribute("y", "" + (80 - rainBarHeight));
 		mmBox.setAttribute("fill", "blue");
 		
 		
 		mmBox.setAttribute("width", "25");
 		mmBox.setAttribute("height", "" + rainBarHeight);
 		
 		svg.addContent(mmBox);
 		
 		
 		
 		//text
 		Element mm = new Element("text");
 		mm.setAttribute("x", "5");
 		mm.setAttribute("y", "70");
 		mm.setText(String.valueOf(rain.getAmount()));
 		
 		svg.addContent(mm);
 		
 		
 	}
 
 	private void renderWind(Wind wind) {
 		Element windNumber = new Element("text");
 		windNumber.setAttribute("y", "97");
 		if(wind.getSpeed() < 10.0)
 			windNumber.setAttribute("x", "2");
 		else
 			windNumber.setAttribute("x", "0");
 		
 		windNumber.setText(""+wind.getSpeed());
 		
 		svg.addContent(windNumber);
 		
 		Element windArrow = new Element("line");
 		windArrow.setAttribute("x2", "0");
 		windArrow.setAttribute("y2", "10");
 		windArrow.setAttribute("x1", "0");
 		windArrow.setAttribute("y1", "0");
 		windArrow.setAttribute("stroke-width", "3");
 		
 		Element windArrow2 = new Element("line");
 		windArrow2.setAttribute("x2", "10");
 		windArrow2.setAttribute("y2", "0");
 		windArrow2.setAttribute("x1", "0");
 		windArrow2.setAttribute("y1", "0");
 		windArrow2.setAttribute("stroke-width", "3");
 		
 		Element rotate = new Element("g");
 		rotate.setAttribute("stroke", "green");

        int windArrowOffset = 45 + 180;
		rotate.setAttribute("transform", "rotate("+ (weather.getWind().getDirection() + windArrowOffset ) +")");
 		rotate.addContent(windArrow);
 		rotate.addContent(windArrow2);
 		
 		Element move = new Element("g");
 		move.setAttribute("transform", "translate(15,80)");
 		
 		move.addContent(rotate);
 		
 		svg.addContent(move);
 	}
 
 	private void renderCloud(int level, float percent)
 	{
 		int downOffset = (level * 5) - 3 ;
 		
 		Element highCloud = new Element("rect");
 		highCloud.setAttribute("y", ""+downOffset);
 		highCloud.setAttribute("x", "5");
 		highCloud.setAttribute("rx", "0");
 		highCloud.setAttribute("width", "20");
 		highCloud.setAttribute("height","5");
 		highCloud.setAttribute("fill","black");	
 		
 		
 		Element highCloudIndicator = new Element("rect");
 		highCloudIndicator.setAttribute("y",""+( downOffset + 1));
 		highCloudIndicator.setAttribute("x", "6");
 		highCloudIndicator.setAttribute("rx", "0");
 		highCloudIndicator.setAttribute("width", ""+ (18* percent/100));
 		highCloudIndicator.setAttribute("height","3");
 		highCloudIndicator.setAttribute("fill","white");
 		
 		svg.addContent(highCloud);
 		svg.addContent(highCloudIndicator);
 	}
 	
 	
 }
