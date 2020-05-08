 /**
  * 
  */
 package AP2DX;
 
 import java.io.*;
 import java.util.logging.*;
 import java.util.Map;
 
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 
 /**
  * @author Jasper Timmer
  * Baseclass for AP2DX components
  * 
  * Can read config file
  * Can write logfile
  * Can listen at socket
  * Can send to socket[s]
  */
 public abstract class AP2DXBase {
 	
 	public static Logger logger;
 
 	/**
 	 * constructor
 	 */
 	public AP2DXBase() {
 		Map config = ReadConfig();
 	    try {
 	      boolean append = true;
 	      FileHandler fh = new FileHandler(config.get("logfile").toString(), append);
 	      fh.setFormatter(new Formatter() {
 	          public String format(LogRecord rec) {
 	             StringBuffer buf = new StringBuffer(1000);
 	             buf.append(new java.util.Date());
 	             buf.append(' ');
 	             buf.append(rec.getMillis());
 	             buf.append(' ');
 	             buf.append(rec.getSourceMethodName());
 	             buf.append(' ');
 	             buf.append(rec.getLevel());
 	             buf.append(' ');
 	             buf.append(formatMessage(rec));
 	             buf.append('\n');
 	             return buf.toString();
 	             }
 	           });
 
 	      logger = Logger.getLogger(this.getClass().getPackage().getName());
 	      logger.addHandler(fh);
 	    }
 	    catch (IOException e) {
 	      e.printStackTrace();
 	    }
 	    
 	    logger.info("Package: " + this.getClass().getPackage().getName());
 	    
 	}
 	
 	/**
 	 * read hardcoded configfile
 	 */
 	protected Map ReadConfig() {
 		  //get text contents of file config.json (hard-coded filename)
 		  String jsonText = getContents(new File("coordinator.json"));
 		  Object jsonObj = null;
 		  Map jsonMap = null;
 		  JSONParser parser = new JSONParser();
 		                
 		  try{
 			  jsonObj = parser.parse(jsonText);
 			  jsonMap = (Map)jsonObj;
 		  }
 		  catch(ParseException pe){
 		    System.out.println("position: " + pe.getPosition());
 		    System.out.println(pe);
 		  }
 		  
 		  return jsonMap;
	}
 	
 	/**
 	  * Fetch the entire contents of a text file, and return it in a String.
 	  * This style of implementation does not throw Exceptions to the caller.
 	  *
 	  * @param aFile is a file which already exists and can be read.
 	  */
 	static protected String getContents(File aFile) {
 	    //...checks on aFile are elided
 	    StringBuilder contents = new StringBuilder();
 	    
 	    try {
 	      //use buffering, reading one line at a time
 	      //FileReader always assumes default encoding is OK!
 	      BufferedReader input =  new BufferedReader(new FileReader(aFile));
 	      try {
 	        String line = null; //not declared within while loop
 	        /*
 	        * readLine is a bit quirky :
 	        * it returns the content of a line MINUS the newline.
 	        * it returns null only for the END of the stream.
 	        * it returns an empty String if two newlines appear in a row.
 	        */
 	        while (( line = input.readLine()) != null){
 	          contents.append(line);
 	          contents.append(System.getProperty("line.separator"));
 	        }
 	      }
 	      finally {
 	        input.close();
 	      }
 	    }
 	    catch (IOException ex){
 	      ex.printStackTrace();
 	    }
 	    
 	    return contents.toString();
 	  }
 
 }
