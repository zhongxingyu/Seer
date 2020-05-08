 package cz.muni.fi.pb138.log4jconverter;
 
 
 
 public class Config {
 	String inputFile = null;
 	String outputFile = null;
 	InputLoader.Type outputType = null;
 	InputLoader.Type inputType = null;
 	int verbose = 0;
 	
     private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Config.class);
 	
 	public Config(String args[]){
 		int i=0;
 		while(i < args.length){
 			String arg = args[i++];
 			if(logger.isTraceEnabled()){logger.trace("Parsing argument " + arg);}
 			
 			if(arg.startsWith("--")){	//LONG
 				arg = arg.substring(2);
 				
 				if(arg.equals("input") && i < args.length){
 					inputFile = args[i++];
 				}else if(arg.equals("output") && i < args.length){
 					outputFile = args[i++];
 				}else if(arg.equals("output-type") && i < args.length){
 					outputType = parseType(args[i++]);
 				}else if(arg.equals("input-type") && i < args.length){
 					inputType = parseType(args[i++]);
 				}else if(arg.equals("help")){
 					printHelp();
 				}else if(arg.equals("verbose")){
 					verbose++;
 				}else if(arg.equals("quiet")){
 					verbose--;
 				}
 			}else if(arg.startsWith("-")){	//SHORT
 				arg = arg.substring(1);
 				
 				if(arg.equals("i") && i < args.length){
 					inputFile = args[i++];
 				}else if(arg.equals("o") && i < args.length){
 					outputFile = args[i++];
 				}else if(arg.equals("T") && i < args.length){
 					outputType = parseType(args[i++]);
 				}else if(arg.equals("t") && i < args.length){
 					inputType = parseType(args[i++]);
 				}else if(arg.equals("h")){
 					printHelp();
 				}else if(arg.equals("v")){
 					verbose++;
 				}else if(arg.equals("q")){
 					verbose--;
 				}
 			}
 		}
 	}
 
 	private void printHelp() {
 		System.out.println("log4j configuration converter");
 		System.out.println("Convert log4j configuration from Properties file to XML or vice versa");
 		System.out.println();
 		System.out.println("Arguments:");
		System.out.println("  -o FILE --output FILE       Generated configuration will be written to file FILE.");
		System.out.println("                              If not set, configuration will be written to standard output.");
 		System.out.println("  -i FILE --input FILE        Configuration will be read from file FILE.");
		System.out.println("                              If not set, configuration will be read from standard input.");
 		System.out.println("  -t TYPE --input-type TYPE   Specify the input file type. If not set,");
		System.out.println("                              converter will try to determine the file type automatically.");
 		System.out.println("  -T TYPE --output-type TYPE  Specify the output file type. If not set,");
		System.out.println("                              converter will set this to the type different from the input file.");
 		System.out.println("  -h --help                   Print this help.");
 		System.out.println("  -v --verbose                Print more detailed messages. You can enter multiple times.");
 		System.out.println("  -q --quiet                  Print less detailed messages. You can enter multiple times.");
 		System.out.println();
 		System.out.println("TYPE can be XML or Properties.");
 		System.exit(0);		
 	}
 
 	private InputLoader.Type parseType(String type) {
 		if(type.toUpperCase().equals("XML")){
 			return InputLoader.Type.XML;
 		}else if(type.toLowerCase().equals("properties")){
 			return InputLoader.Type.PROPERTIES;
 		}
 		return null;
 	}
 
 }
