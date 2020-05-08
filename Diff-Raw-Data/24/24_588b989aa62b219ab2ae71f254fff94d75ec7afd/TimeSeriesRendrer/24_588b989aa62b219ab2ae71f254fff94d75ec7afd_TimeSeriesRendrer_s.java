 package no.yr.svg;
 
 import java.util.Iterator;
 
 import no.yr.weather.TimeSeries;
 import no.yr.weather.Weather;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.output.XMLOutputter;
 
 public class TimeSeriesRendrer {
 
 	protected TimeSeries series;
 	
 	
 	public TimeSeriesRendrer(TimeSeries series)
 	{
 		this.series = series;
 	}
 	
 	public Document getSvg()
 	{
 		Element frame = new Element("svg");
 		Document svg = new Document(frame);
 		
 		frame.setAttribute("height", "200");
		//frame.setAttribute("width", "2024");
 		
 		Iterator<Weather> it = series.getIterator();
 		int count = 0;
 		while(it.hasNext())
 		{
 			Weather weather = it.next();
 			Document partSvg = new WeatherRendrer(weather).getSvg();
 			Element subFrame = partSvg.detachRootElement();
 			subFrame.setName("g");
 			subFrame.setAttribute("transform", "translate(" + count * 30+ ",0)");
 			
 			frame.addContent(subFrame);
 			
 			count++;
 		}
 		
 		addTempratureGraf(frame);
 		
 		
 		return svg;
 		
 		
 	}
 	
 	public String getXml()
 	{
 		Document svg = getSvg();
 		
 		XMLOutputter output = new XMLOutputter();
 		return output.outputString(svg.getRootElement());
 		
 	}
 
 	private void addTempratureGraf(Element svg) {
 		Element graf = new Element("polyline");
 		int zero = 25;
 		int left = 15;
 		
 		float min = 0;
 		float max = 0;
 		
 		int magnitude = -3;
 		
 		String points = "";
 		Iterator<Weather> it = series.getIterator();
 		while(it.hasNext())
 		{
 			Weather weather = it.next();
 			points += " " + left +","+ (int)((weather.getTempratur().getTemperatur()*magnitude) + zero);
 			
 			if(weather.getTempratur().getTemperatur() < min)
 				min = weather.getTempratur().getTemperatur();
 			
 			if(weather.getTempratur().getTemperatur() > max)
 				max = weather.getTempratur().getTemperatur();
 			
 			left += 30;
 		}
 		
 		
 		Element subFrame = new Element("g");
 		subFrame.setAttribute("transform", "translate(0,15)");
 		subFrame.addContent(graf);
 		
 		Element zeroLine = new Element("rect");
 		zeroLine.setAttribute("y", ""+zero);
 		zeroLine.setAttribute("x", "0");
 		zeroLine.setAttribute("width", ""+ (left-15));
 		zeroLine.setAttribute("height", "0.5");
 		zeroLine.setAttribute("stroke", "black");
 		subFrame.addContent(zeroLine);
 		
 		if(max > 0)
 		{
 			Element maxLine = new Element("rect");
 			maxLine.setAttribute("y", ""+ (zero + (max*magnitude)));
 			maxLine.setAttribute("x", "0");
 			maxLine.setAttribute("width", ""+ (left-15));
 			maxLine.setAttribute("height", "0.2");
 			maxLine.setAttribute("stroke", "green");
 			subFrame.addContent(maxLine);
 			
 			Element tempNumber = new Element("text");
 			tempNumber.setAttribute("y", "" + (zero + (max*magnitude) + 5));
 			tempNumber.setAttribute("x", "" + (left-10));
 			
 			tempNumber.setText("max "+ max + "c");
 			subFrame.addContent(tempNumber);
 			
 		}
 		
 		if(min < 0 )
 		{
 			Element minLine = new Element("rect");
 			minLine.setAttribute("y", ""+ (zero + (min*magnitude)));
 			minLine.setAttribute("x", "0");
 			minLine.setAttribute("width", ""+ (left-15));
 			minLine.setAttribute("height", "0.2");
 			minLine.setAttribute("stroke", "green");
 			subFrame.addContent(minLine);
 			
 			Element tempNumber = new Element("text");
 			tempNumber.setAttribute("y", "" + (zero + (min*magnitude) + 5));
 			tempNumber.setAttribute("x", "" + (left-10));
 			
 			tempNumber.setText("min "+ min + "c");
 			
 			subFrame.addContent(tempNumber);
 		}
 
         graf.setAttribute("points", points.trim());
 		graf.setAttribute("stroke", "red");
         graf.setAttribute("fill", "none");
 		
 		svg.addContent(subFrame);
 	}
 }
