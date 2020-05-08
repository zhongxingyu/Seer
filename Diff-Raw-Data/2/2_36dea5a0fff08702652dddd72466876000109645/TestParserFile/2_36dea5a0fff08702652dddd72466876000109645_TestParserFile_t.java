 package Test;
 import java.io.File;
 import GCParser.CircuitParser;
 import GCParser.Variable_Context;
 import jargs.gnu.CmdLineParser;
 import java.util.TreeMap;
 import java.math.BigInteger;
 public class TestParserFile {
   public static boolean DEBUG = false;
   public static boolean DEBUG_LOCAL = false;
   public static String[] parse_args(String[] args){
     CmdLineParser parser = new CmdLineParser();
     CmdLineParser.Option debug = parser.addBooleanOption('d',"debug");
     CmdLineParser.Option help = parser.addBooleanOption('h',"help");
     CmdLineParser.Option debug_local = parser.addBooleanOption('l',"debug-local");
 
     try{
       parser.parse(args);
     } catch( CmdLineParser.OptionException e ){
       printHelp(1);
     }
     if( (Boolean) parser.getOptionValue(help,false) ){
       printHelp(0);
     }
     DEBUG = (Boolean) parser.getOptionValue(debug,false);
     DEBUG_LOCAL = (Boolean) parser.getOptionValue(debug_local,false);
     return parser.getRemainingArgs();
   }
   public static void main( String[] args ){
     String[] files = parse_args(args);
     for( String s : files ){
       try {
 	Variable_Context v = CircuitParser.read( new File(s) );
 	if( DEBUG_LOCAL ){
 	  v.collapseLocalVars( new TreeMap<String,BigInteger>(), 1 );
 	}
 	if( DEBUG || DEBUG_LOCAL ){
 	  v.debugPrint();
 	}
 	System.out.println(s+": ok");
       } catch (Exception e){
 	System.out.println(s+": not ok -"+e.getMessage());
       }
     }
   }
   public static void printHelp(int exit){
    System.out.println("TestParserFile [-h --help] [-d --debug] [-l --debug-local] FILES...");
     System.exit(exit);
   }
 }
