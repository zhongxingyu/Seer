 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui.pa2;
 
 import java.util.Vector;
 import java.util.TreeMap;
 import java.util.Map;
 /**
  *
  * @author jmk
  */
 public class DataCalculator {
     
     private Vector<Conditions> points;
     
     // Accessor constants for the map. Add a new one if you add a new field to compute.
     public static final String AVG_TEMP = "    Average Temperature";
     public static final String MAX_TEMP = "    Max Temperature";
     public static final String MIN_TEMP = "    Min Temperature";
     
     public static final String AVG_WIND = " Average Wind Speed";
     public static final String MAX_WIND = " Max Wind Speed";
     public static final String WIND_DIR = " Prevailing Wind";
     
     public static final String TOTAL_RAIN = "      Total Rainfall";
     public static final String MAX_RAIN = "      Maximum Rainfall";
     public static final String MIN_RAIN = "      Minimum Rainfall";
     
     public static final String AVG_PRESSURE = "     Average Barometric Pressure";
     public static final String MAX_PRESSURE = "     Max Barometric Pressure";
     public static final String MIN_PRESSURE = "     Min Barometric Pressure";
     
     public static final String AVG_HUM = "  Average Humidity";
     public static final String MAX_HUM = "  Maximum Humidity";
     public static final String MIN_HUM = "  Minimum Humidity";
     
     public static final String AVG_UV = "Average UV";
     public static final String MAX_UV = "Maximum UV";
     public static final String MIN_UV = "Minimum UV";
     
     public DataCalculator( Vector<Conditions> p )
     {
         points = p;
     }
     
     public DataCalculator()
     {
         points = null;
     }
     
     public TreeMap<String,Object> computeData()
     {
         TreeMap<String, Object> map = new TreeMap<String, Object>();
         
         // Calculated Values
         double maxTemp = 0.0;
         double minTemp = 1000.0;
         double maxWindSpeed = 0.0;
         double maxPressure = 0.0;
         double minPressure = 100.0;
         double minRain = 1000.0;
         double maxRain = 0.0;
         double maxHum = 0.0;
         double minHum = 105.0;
         double maxUV = -1;
         double minUV = 100;
         
         // Values used for Averaging
         double pressureSum = 0;
         int pressureCount = 0;
         double TempSum = 0.0;
         double humSum = 0.0;
         double uvSum = 0.0;
         int uvCount = 0;
         int humCount = 0;
         int tempCount = 0;
         double totalRainfall = 0.0;
         int rainCount = 0;
         double WindSpeedSum = 0.0;
         int windCount = 0;
         
         TreeMap<String, Integer> windDirCount = new TreeMap<String, Integer>();
         String prevailingWind = "";
         
         windDirCount.put( "N",new Integer(0));
         windDirCount.put( "NNE",new Integer(0));
         windDirCount.put( "NE",new Integer(0));
         windDirCount.put( "ENE",new Integer(0));
         windDirCount.put( "E",new Integer(0));
         windDirCount.put( "ESE",new Integer(0));
         windDirCount.put( "SE",new Integer(0));
         windDirCount.put( "SSE",new Integer(0));
         windDirCount.put( "S",new Integer(0));
         windDirCount.put( "SSW",new Integer(0));
         windDirCount.put( "SW",new Integer(0));
         windDirCount.put( "WSW",new Integer(0));
         windDirCount.put( "W",new Integer(0));
         windDirCount.put( "WNW",new Integer(0));
         windDirCount.put( "NW",new Integer(0));
         windDirCount.put( "NNW",new Integer(0));
         
         for(Conditions current : points)
         {       
         	if(current.getRain() != Conditions.INVALID_RAIN)
         	{	
         		totalRainfall += current.getRain();
         		rainCount++;
         		if(current.getRain() > maxRain)
         		{
         			maxRain = current.getRain();
         		}
         		if(current.getRain() < minRain)
         		{
         			minRain = current.getRain();
         		}
         	}
         	
             if(current.getTemperature() != Conditions.INVALID_TEMP)
             {
             	TempSum = current.getTemperature();
             	tempCount++;
             	
             	if( current.getTemperature() > maxTemp )
                 {
                     maxTemp = current.getTemperature();
                 }
                 
                 if( current.getTemperature() < minTemp )
                 {
                     minTemp = current.getTemperature();
                 }
             }
             if(current.getWind() != Conditions.INVALID_WIND)
             {
             	WindSpeedSum = current.getWind();
             	windCount++;
 	            if( current.getWind() > maxWindSpeed )
 	            {
 	                maxWindSpeed = current.getWind();
 	            }
             }
             if(current.getPressure() != Conditions.INVALID_PRESSURE)
             {
             	pressureSum += current.getPressure();
             	++pressureCount;
             	if( current.getPressure() > maxPressure)
             	{
                 	maxPressure = current.getPressure();
                 }
                 if( current.getPressure() < minPressure && current.getPressure() != Conditions.INVALID_PRESSURE)
                 {
                 	minPressure = current.getPressure();
                 }
             }
             if(current.getHumidity() != Conditions.INVALID_HUMIDITY)
             {
             	humCount++;
             	humSum += current.getHumidity();
             	if(current.getHumidity() > maxHum)
             	{
             		maxHum = current.getHumidity();
             	}
             	if(current.getHumidity() < minHum)
             	{
             		minHum = current.getHumidity();
             	}
             }
             
             if(current.getUv() != Conditions.INVALID_UV)
             {
             	uvCount++;
             	uvSum += current.getUv();
             	if(current.getUv() > maxUV)
             	{
             		maxUV = current.getUv();
             	}
             	if(current.getUv() < minUV)
             	{
             		minUV = current.getUv();
             	}
             }
                       
            int dir = windDirCount.get( current.getWindAngle().trim() );
            ++dir;
            windDirCount.put(current.getWindAngle().trim(), dir);
         }
         
         int maxCount = 0;
         
         for( Map.Entry<String, Integer> current : windDirCount.entrySet())
         {            
             if( (Integer) current.getValue() > maxCount )
             {
                 maxCount = (Integer) current.getValue();
                 prevailingWind = (String) current.getKey();
             }
         }
 
         /*
          * It looks a lot nicer if we organize these strings. This is probably the best time, since doing it later requires
          * string comparisons and gets painful. These strings should be sorted alphabetically, so adding an extra space to
          * the start of the string is a cost-efficient way of forcing the strings into the correct order. We can remove the
          * extra white space by simply calling trim() on the strings before putting them into JLabels.
          */
         map.put("       Rainfall", "-------------");
         map.put(TOTAL_RAIN, new Float(totalRainfall) );
         map.put(MAX_RAIN, new Float(maxRain));
         map.put(MIN_RAIN, new Float(minRain));
         
         map.put("     Temperature", "-------------");
         if(tempCount > 0)
         	map.put( AVG_TEMP, new Float( TempSum / tempCount ) );
         else
         	map.put( AVG_TEMP, "N/A");
         map.put( MAX_TEMP, new Float( maxTemp) );
         map.put( MIN_TEMP, new Float( minTemp) );
         
         map.put("   Wind", "-------------");
         if(windCount != 0)
         	map.put( AVG_WIND, new Float( WindSpeedSum / windCount ) );
         else
         	map.put( "  Average Wind Speed", "N/A" );
         map.put( MAX_WIND, new Float( maxWindSpeed) );
         map.put( WIND_DIR, prevailingWind );
         
         map.put("      Pressure","-------------");
         if(pressureCount != 0)
         	map.put(AVG_PRESSURE, new Float( pressureSum / pressureCount));
         else
         	map.put(AVG_PRESSURE, "N/A");
         map.put(MAX_PRESSURE, new Float(maxPressure));
         map.put(MIN_PRESSURE, new Float(minPressure));
         map.put("   Humidity", "-------------");
         if(humCount > 0)
         {
         	map.put(AVG_HUM, new Float(humSum / humCount));
         }
         else map.put(AVG_HUM, "N/A");
         map.put(MAX_HUM, maxHum);
         map.put(MIN_HUM, minHum);
         
         map.put( " UV", "-------------" );
         if(uvCount > 0)
         {
         	map.put(AVG_UV, uvSum / uvCount);
         }
         else map.put(AVG_UV, "N/A");
         map.put(MAX_UV, new Float(maxUV));
         map.put(MIN_UV, new Float(minUV));
         
         return map;
     }
     
     public Vector<Conditions> getPoints()
     {
         return points;
     }
     
     public void setPoints(Vector<Conditions> p)
     {
         points = p;
     }
     
 }
