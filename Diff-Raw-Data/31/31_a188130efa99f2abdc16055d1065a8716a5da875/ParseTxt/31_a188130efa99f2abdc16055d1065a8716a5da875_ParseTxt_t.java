 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JOptionPane;
 
 
 public class ParseTxt {
 	
 	ArrayList<MessageStorage> Messages = new ArrayList<MessageStorage>();
 	Main Main = new Main();
 	
 	
 	
 	
 	public final ArrayList<MessageStorage> processLineByLine(File file, String imei) throws FileNotFoundException {
 		   Scanner scanner = new Scanner(new FileReader(file));
 		    try {
 		      //     
 		      while ( scanner.hasNextLine() ){
 		        processLine( scanner.nextLine(), imei );
 		      }
 		    }
 		    finally {
 		     scanner.close();
 		     
 		     
 		    }
 			return Messages;
 		  }
 		  
 		
 
 		  protected void processLine(String aLine, String imei){
 			 
 		   	  int latitude;
 		   	  int longitude;
 		      int speed;
 		      int height;  
 		      int hdop = 0;
 		      int pwr_ext = 0;
 		      int pwr_int = 0;
 		      int adc1 = 0;
 		      int adc2 = 0;
 		      int sats = 0;
 			
		    String aLineClear = aLine.replaceAll(";", ",");  
		  
 		    //       
 		    Scanner scanner = new Scanner(aLineClear);
 		    scanner.useDelimiter(",");
 		    if ( scanner.hasNext() ){
 		   	  scanner.next();
 		   	  scanner.next();
 		   	  latitude = (int)(Float.valueOf(scanner.next())*1000000);
 		      longitude = (int)(Float.valueOf(scanner.next())*1000000);
 		      speed = Integer.valueOf(scanner.next())*10;
 		      height = Integer.valueOf(scanner.next());
		      while(scanner.hasNext() ){
 		    	 String test = scanner.next();
 		    	if (test.startsWith("hdop:")) {
 		    		hdop = (int)(Float.valueOf(test.substring(5))*10);
 		    	}
 		    	else if (test.startsWith("pwr_ext:")) {
 		    		pwr_ext = (int)(Float.valueOf(test.substring(8))*1000);
 		    	} 
 		    	else if (test.startsWith("pwr_int:")) {
 		    		pwr_int = (int)(Float.valueOf(test.substring(8))*1000);
 		    	}
 		    	else if (test.startsWith("adc1:")) {
 		    		adc1 = (int)(Float.valueOf(test.substring(5))*1000);
 		    	}
 		    	else if (test.startsWith("adc2:")) {
 		    		adc2 = (int)(Float.valueOf(test.substring(5))*1000);
 		    	}
 		    	else if (test.startsWith("SATS:")) {
 		    		sats = Integer.valueOf(test.substring(5));
 		    	}
 		    	
 		      }
 		      Messages.add(new MessageStorage(imei, latitude,longitude, speed,height,hdop,pwr_ext,pwr_int,adc1,adc2,sats));
 		     
 		      }
 		    else {
 		      JOptionPane.showMessageDialog(null, "   .  .");
 		    }
 		    scanner.close();
 ;}
 
 		 
 	
 	
 	
 }
