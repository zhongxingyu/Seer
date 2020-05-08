 package main;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Properties;
 
 
 public class GenerateConfig {
 	Properties configFile;
 	String start = "";
 	String stop = "";
 	String name = "";
 	String result = "";
 	String raceTime  = "";
 	String raceType = "";
 	int distance = 0;
 	String attributeString = "";
 	
 	public GenerateConfig(Properties configFile){
 		this.configFile = configFile; 
 	}
 	
 	
 	public void autogenerateConfig(){
 		 try {  
 				FileOutputStream createTheFile = new FileOutputStream(new File("config.properties"));
 
 		        BufferedWriter out = new BufferedWriter(new FileWriter("config.properties")); 
 		        out.write("#AUTO GENERATED config.properties");
 		        out.write("\n");
 		        out.write("#Values for Enduro.java stored as <key>=<value>");
 		        out.write("\n");
 		        out.write("\n");
		        out.write("#Filnamn" + "\n");
 		        out.write("STARTFILE=" + "\n");
 		        out.write("STOPFILE=" + "\n");
 		        out.write("NAMEFILE=" + "\n");
 		        out.write("RESULTFILE=" + "\n");
 		        
 		        out.write("#Ex: \"varv\" (Etapplopp EJ implementerad)" + "\n");
 		        out.write("RACETYPE=" + "\n");
 		        out.write("#Ex: \"enkelstart\", \"masstart\", " + "\n");
 		        out.write("STARTTYPE=" + "\n");
 		        out.write("#Antal etapper, SpecialSträckor och faktor" + "\n");
 		        out.write("NBR_ETAPP=" + "\n");
 		        out.write("SPEC_DISTANCES=" + "\n");
 		        out.write("FACTOR=" + "\n");
 		        out.write("#Minimumtid för varv resp etapp (För att hitta orimliga tider)" + "\n");
 		        out.write("MINTIME=" + "\n");
 		        out.write("#Totaltiden för hela racet i timmar (ex: hh.mm )" + "\n");
 		        out.write("RACETIME=" + "\n");
 		        out.write("#Ex: 5" + "\n");
 		        out.write("DISTANCE=0" + "\n");
 		        out.write("#Vilka resultat vill man ha (med o utan sortering, i vilka format" + "\n");
 		        out.write("RESULT_FORMAT=" + "\n");
 		        out.write("#Vad alla deltagare i denna tävlingen behöver ange för information om sig själva. Exempel: Klubb;MC-fabrikat;Sponsor1;Sponsor2;(etc...)" + "\n");
 		        out.write("DRIVER_ATTRIBUTES=" + "\n");
 		        out.write("#Vilka etapper är specialsträckor? Ange nummer för dessa"+"\n");
 		        out.write("#Ex \"2,3\" om 2 och 3 är specialsträckor"+"\n");
 		        out.write("SPECIAL_DISTANCES="+"\n");
 		        out.write("#Faktorn som specialsträckor skall multipliceras med"+"\n");
 		        out.write("#Ex: 3" +"\n");
 		        out.write("FACTOR=");
 		        out.close();  
 		        System.out.println("Autogenerate file: config.properties.");
 		    } catch (IOException e) {  
 		    	System.out.println("Fail to generate config.properties");
 		    }  
 		
 
 	}
 	
 	// VILKA KEYS �R OBLIGATORISKA F�R VARJE LOPP?
 	public boolean checkKey(){
 		boolean toggle = true;
 	
 		if(configFile.getProperty("STARTFILE").equals("")){
 			System.err.println("the key STARTFILE not found, check your configfile");
 			toggle = false;
 		}
 		if(configFile.getProperty("STOPFILE").equals("")){
 			System.err.println("the key STOPFILE not found, check your configfile");
 			toggle = false;
 		}
 		if(configFile.getProperty("NAMEFILE").equals("")){
 			System.err.println("the key name not found, check your configfile");
 			toggle = false;
 		}
 		if(configFile.getProperty("RESULTFILE").equals("")){
 			System.err.println("the key result not found, check your configfile");
 			toggle = false;
 		}
 		if(configFile.getProperty("RACETIME").equals("")){
 			System.err.println("the key race not found, check your configfile");
 			toggle = false;
 		}
 		if(configFile.getProperty("RACETYPE").equals("")){
 			System.err.println("the key rtype not found, check your configfile");
 			toggle = false;
 		}
 		if((configFile.getProperty("DISTANCE")).equals("")){
 			System.err.println("the key distance not found, check your configfile");
 			toggle = false;
 		}
 		return toggle;
 	}
 	
 }
