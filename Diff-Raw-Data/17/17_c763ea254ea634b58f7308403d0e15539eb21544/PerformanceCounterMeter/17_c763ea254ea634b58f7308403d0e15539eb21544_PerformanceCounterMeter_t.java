 package net.praqma.ccanalyzer;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.praqma.ccanalyzer.PerformanceCounterConfiguration.AggregateFunction;
 import net.praqma.util.execute.AbnormalProcessTerminationException;
 import net.praqma.util.execute.CmdResult;
 import net.praqma.util.execute.CommandLine;
 
 public class PerformanceCounterMeter {
     private static String prog = "typeperf";
     private static int numberOfSamples = 1;
     private static int timeInterval = 1; // Seconds
 
     private static Pattern rx_ = Pattern.compile( "^\\\"(.*?)\\\",\\\"(.*?)\\\"$" );
 
     public enum RequestType {
         NAMED_COUNTER, 
         SHORT_HAND_COUNTER
     }
 
     /**
      * <table style="border-style:solid">
      * <tr>
      * <td>Request Type</td>
      * <td>Named</td>
      * <td>ShortHand</td>
      * </tr>
      * <tr>
      * <td>0</td>
      * <td>Type</td>
      * <td>Type</td>
      * </tr>
      * <tr>
      * <td>1</td>
      * <td>Performance Counter</td>
      * <td>Method name</td>
      * </tr>
      * <tr>
      * <td>2</td>
      * <td>number of samples</td>
      * <td>Optional argument</td>
      * </tr>
      * <tr>
      * <td>3</td>
      * <td>Interval in seconds</td>
      * <td>Optional arguments</td>
      * </tr>
      * <tr>
      * <td>4</td>
      * <td>Aggregate function</td>
      * <td>Optional argument</td>
      * </tr>
      * </table>
      * 
      * @param request
      * @return
      */
     public static String parseRequest( List<String> request ) {
         RequestType type = RequestType.valueOf( request.get( 0 ) );
 
         switch( type ) {
         case NAMED_COUNTER:
 
             /* Is ClearCase!? */
             ClearCaseCounter ccc = ClearCaseCounter.fromString( request.get( 1 ) );
             if( ccc != null ) {
                 System.out.println( " $" + request.get( 1 ) );
                 
                 try {
                     Class clazz = Class.forName( "net.praqma.ccanalyzer.clearcase." + ccc.getFunction() );
                     ClearCaseFunction instance = (ClearCaseFunction) clazz.newInstance();
                     return instance.perform( ccc );
                 } catch( ClassNotFoundException e ) {
                     System.err.println( "The requested class " + ccc.getFunction() + " was not found" );
                     return "";
                 } catch( InstantiationException e ) {
                     System.err.println( "Could not instantiate " + ccc.getFunction() );
                     return "";
                 } catch( IllegalAccessException e ) {
                     System.err.println( "Could not get access to instantiate " + ccc.getFunction() );
                     return "";
                 }
             }
 
             AggregateFunction fun = AggregateFunction.NUMERICAL_AVERAGE;
 
             int numberOfSamples = 1;
             int intervalTime = 1;
 
             if( request.size() > 2 ) {
                 numberOfSamples = Integer.parseInt( request.get( 2 ) );
             }
 
             if( request.size() > 3 ) {
                 intervalTime = Integer.parseInt( request.get( 3 ) );
             }
 
             if( request.size() > 4 ) {
                 fun = AggregateFunction.valueOf( request.get( 4 ) );
             }
 
            /* This could potentially cause an exception */
            try {
	            List<String> r = get( request.get( 1 ), numberOfSamples, intervalTime );
	            if( r.size() == 1 ) {
	                return r.get( 0 );
	            } else if( r.size() > 1 ) {
	                return getResult( r, fun );
	            }
            } catch( Exception e ) {
            	return "";
             }
            
             break;
 
         case SHORT_HAND_COUNTER:
             return ShortHandCounters.execute( request );
 
         }
 
         return null;
     }
 
     public static String parseRequest( PerformanceCounterConfiguration pc ) {
 
         List<String> r = get( pc.counter, pc.numberOfSamples, pc.intervalTime );
         if( r.size() == 1 ) {
             return r.get( 0 );
         } else if( r.size() > 1 ) {
             return getResult( r, pc.function );
         }
 
         return null;
     }
 
     public static String getResult( List<String> values, AggregateFunction fun ) {
         switch( fun ) {
         case NUMERICAL_AVERAGE:
             Float avg = 0.0f;
             int total = 0;
             for( String v : values ) {
                 avg += new Float( v );
                 total++;
             }
 
             return new Float( ( avg / total ) ).toString();
         }
 
         return null;
     }
 
     /**
      * For a specific performance counter string, get the values for the class
      * given number of samples and interval
      * 
      * @param performanceString
      * @return
      */
     public static List<String> get( String performanceString ) {
         return get( performanceString, numberOfSamples, timeInterval );
     }
 
     /**
      * 
      * @param performanceString
      * @param numberOfSamples
      * @param timeInterval
      * @return
      */
     public static List<String> get( String performanceString, int numberOfSamples, int timeInterval ) {
         String cmd = prog + " -si " + timeInterval + " -sc " + numberOfSamples + " \"" + performanceString + "\"";
         System.out.print( " " + cmd );
         try {
             CmdResult result = CommandLine.getInstance().run( cmd );
 
             System.out.println( "\r $" + cmd );
 
             List<String> list = new ArrayList<String>();
             boolean first = true;
 
             for( String s : result.stdoutList ) {
                 Matcher m = rx_.matcher( s );
                 if( m.find() ) {
                     if( !first ) {
                         list.add( m.group( 2 ) );
                     } else {
                         first = false;
                     }
                 }
             }
 
             return list;
 
         } catch( AbnormalProcessTerminationException e ) {
             throw new CCAnalyzerException( "Could not get " + performanceString + ", " + e.getMessage() );
         }
     }
 
 }
