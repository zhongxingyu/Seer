 package serial;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.BufferedReader;
 import java.io.PrintStream;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
/*
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

*/

 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 /**
  * Tool to convert data to JSON data fit for consumption by the Rickshaw package.
  * This tool is currently only targeted at converting data output from my Arduino Hygrometer,
  * so not very generic. Just needed a convenient way to transform the data.
  *
  * Example:
  * <pre>
  * {@code
  *[
  *	{
  *		"color": "blue",
  *		"name": "New York",
  *		"data": [ { "x": 0, "y": 40 }, { "x": 1, "y": 49 }, { "x": 2, "y": 38 }, { "x": 3, "y": 30 }, { "x": 4, "y": 32"} ]
  *	}, {
  *		"name": "London",
  *		"data": [ { "x": 0, "y": 19 }, { "x": 1, "y": 22 }, { "x": 2, "y": 29 }, { "x": 3, "y": 20 }, { "x": 4, "y": 14 } ]
  *	}, {
  *		"name": "Tokyo",
  *		"data": [ { "x": 0, "y": 8 }, { "x": 1, "y": 12 }, { "x": 2, "y": 15 }, { "x": 3, "y": 11 }, { "x": 4, "y": 10 } ]
  *	}
  *]
  * }
  * </pre>
  */
 public class ToJSONSeries {
 
     public ToJSONSeries() {
 	
     }

     public void convert(String fileName, PrintStream out) {
 	byte history = 20;
 	DataSeries humidity = new DataSeries("Humidity", "#4682b4");
 	DataSeries temps = new DataSeries("Temperature", "#9cc1e0");
 	
 	try {
 	    String nextLine;
 	    BufferedReader in = new BufferedReader(new FileReader(fileName));
 	    
 	    //Read File Line By Line
 	    byte timeStampIndex = 0;
 	    byte rHIndex = 1;
 	    byte tIndex = 2;
 	    int lineNr = 0;
 	    HashMap pair;
 	    float[] rH = new float[history];
 	    float[] t = new float[history];
 	    while ((nextLine = in.readLine()) != null)   {
 		// Remove debug statements and filter out empty lines
 		if(nextLine.startsWith("DEBUG") || nextLine.trim().isEmpty())
 		    continue;
 		
 		String[] splitted = nextLine.split(",");
 		rH[lineNr % history] = Float.parseFloat(splitted[rHIndex]);
 		t[lineNr % history] = Float.parseFloat(splitted[tIndex]);
 
 		if(lineNr % history == 0) {
 		    float rHAvg = average(rH);
 		    float tAvg = average(t);
 		    long timeStampSeconds = 0;
 		    try {
 			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 			
 			timeStampSeconds = df.parse(splitted[timeStampIndex]).getTime()/1000;
 		    } catch(ParseException e) {
 			System.err.println("ERROR! "+e.getMessage());
 			return;
 		    }
 		    pair = new HashMap();
 		    pair.put("x",new Long(timeStampSeconds));
 		    pair.put("y",new Float(rHAvg));
 		    humidity.addData(pair);
 		    
 		    pair = new HashMap();
 		    pair.put("x", new Long(timeStampSeconds));
 		    pair.put("y", new Float(tAvg));		
 		    temps.addData(pair);
 		}
 		lineNr++;
 	    }
 	    
 	    DataSeries[] combined = new DataSeries[2];
 	    combined[0] = humidity;
 	    combined[1] = temps;
 	    Gson gson = new GsonBuilder().setPrettyPrinting().create();
 	    out.println(gson.toJson(combined));
 
 	    in.close();
 	} catch(IOException e) {
 	    System.err.println("ERROR "+e.getMessage());
 	    return;
 	}
     }
 
     private float average(float[] vals) {
 	float avg = 0.0f;
 	for(int i = 0; i < vals.length; i++) {
 	    avg += vals[i];
 	}
 	return avg/vals.length;
     }
 
     protected class DataSeries {	
 	private String name, color;
 	private ArrayList<HashMap> data;
 
 	public DataSeries(String name, String color) {
 	    this.name = name;
 	    this.color = color;
 	    this.data = new ArrayList<HashMap>();
 	}
 
 	public void setData(ArrayList<HashMap> data) {
 	    this.data = data;
 	}
 
 	public ArrayList<HashMap> getData() {
 	    return data;
 	}
 
 	public void addData(HashMap p) {
 	    data.add(p);
 	}
     }
 
    /*
    protected class DataPair {
	private HashMap pair;

	public DataPair(String date, String y) {
	    pair = new HashMap();
	    pair.put("x", date);
	    pair.put("y", y);
	}
    }
    */

     public static void main ( String[] args ) {
 	ToJSONSeries converter = new ToJSONSeries();
 	converter.convert(args[0], System.out);
    }
 }
