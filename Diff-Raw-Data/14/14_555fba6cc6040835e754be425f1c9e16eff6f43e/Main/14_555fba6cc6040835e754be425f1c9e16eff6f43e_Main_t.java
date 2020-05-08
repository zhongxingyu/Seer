 package fom.application;
 
 import jargs.gnu.CmdLineParser;
 import jargs.gnu.CmdLineParser.IllegalOptionValueException;
 import jargs.gnu.CmdLineParser.Option;
 import jargs.gnu.CmdLineParser.UnknownOptionException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 import fom.properties.PropertyHandler;
 import fom.resultlogging.ResultLogger;
 import fom.resultlogging.logengines.CSVLogger;
 import fom.resultlogging.logengines.ConsoleLogger;
 import fom.resultlogging.logengines.FolderLogger;
 import fom.resultlogging.logengines.RPCRemoteLogger;
 import fom.search.sources.SourceFactory;
 import fom.search.sources.SourceFactory.SourceType;
 
 public class Main {
 	
     private static void printUsage() {
         System.err.println("Usage:" +
         					"\n" +
         					"\n" +
         					"CLUSTER ANALYSIS:" +
         					"\n" +
         					"fom --clusterAnalysis\n" +
         					"[--sourceName <source name>]" +
         					"{--rangeStartDay YYYY-MM-DD --rangeEndDay YYYY-MM-DD, --day YYYY-MM-DD, --hour YYYY-MM-DD-HH}\n" +
         					"{--geoGran {poi, neighborhood, city, <custom km radius>}}\n" +
         					"[--considerApproxGeolocations]\n" +
         					"[--consoleLog] [--csvLog] [--folderLog] [--rpcLog]" +
         					"\n" +
         					"\n" +
         					"QUERY:" +
         					"\n" +
         					"fom [--expEng {wikiminer}] [--twitter] [--teamlife] [--localDB]\n" +
         					"[--query queryString] [--since YYYY-MM-DD] [--until YYYY-MM-DD]\n" +
         					"[--nearLat latitude] [--nearLon longitude] [--radius radius]\n" +
         					"[--timeGran {hour, day, week}] [--geoGran {poi, neighborhood, city, <custom value>}]\n" +
         					"[--consoleLog] [--csvLog] [--folderLog] [--rpcLog]" +
         					"\n" +
         					"\n" +
         					"STREAM CAPTURING:" +
         					"\n" +
         					"fom --captureStream [--filterGeoTagged]" +
         					"\n" +
         					"\n" +
         					"FIRST RUN:" +
         					"fom --firstRun");
     }
 
 	public static void main(String[] args){
 		//Define command line options:
 		
 		CmdLineParser parser = new CmdLineParser();
 		
 		Option clusterAnalysisOpt = parser.addBooleanOption("clusterAnalysis");
 		Option sourceNameOpt = parser.addStringOption("sourceName");
 		Option rangeStartDayAnalysisOpt = parser.addStringOption("rangeStartDay");
 		Option rangeEndDayAnalysisOpt = parser.addStringOption("rangeEndDay");
 		Option dayAnalysisOpt = parser.addStringOption("day");
 		Option hourAnalysisOpt = parser.addStringOption("hour");
 		Option considerApproxGeoOpt = parser.addBooleanOption("considerApproxGeolocations");
 		
 		Option firstRunOpt = parser.addBooleanOption("firstRun");
 		
 		Option captureStreamOpt = parser.addBooleanOption("captureStream");
 		Option filterGeoTaggedOpt = parser.addBooleanOption("filterGeoTagged");
 		
 		Option expEngineNameOpt = parser.addStringOption("expEng");
 		
 		Option twitterSrcOpt = parser.addBooleanOption("twitter");
 		Option teamlifeSrcOpt = parser.addBooleanOption("teamlife");
 		Option localDBSrcOpt = parser.addBooleanOption("localDB");
 		
 		Option queryStringOpt = parser.addStringOption("query");
 		Option startTimeOpt = parser.addStringOption("since");
 		Option endTimeOpt = parser.addStringOption("until");
 		Option nearLatOpt = parser.addDoubleOption("nearLat");
 		Option nearLonOpt = parser.addDoubleOption("nearLon");
 		Option radiusOpt = parser.addIntegerOption("radius");
 		Option timeGranOpt = parser.addStringOption("timeGran");
 		Option geoGranOpt = parser.addStringOption("geoGran");
 		
 		Option consoleLogOpt = parser.addBooleanOption("consoleLog");
 		Option csvLogOpt = parser.addBooleanOption("csvLog");
 		Option folderLogOpt = parser.addBooleanOption("folderLog");
 		Option rpcLogOpt = parser.addBooleanOption("rpcLog");
 		
 		try {
 			parser.parse(args);
 		} catch (IllegalOptionValueException e) {
 			printUsage();
 			System.exit(-1);
 		} catch (UnknownOptionException e) {
 			printUsage();
 			System.exit(-1);
 		}
 		
 		//Parse options:
 		
 		//First Run
 		if(parser.getOptionValue(firstRunOpt)!=null){
 			FirstRun.main(new String[0]);
 			return;
 		}
 		
 		//CaptureStream
 		if(parser.getOptionValue(captureStreamOpt)!=null){
 			if(parser.getOptionValue(filterGeoTaggedOpt)!=null){
 				new Thread(new StreamCapturer(true, new String[0], parseGeoBoxes())).start();				
 			}else{
 				new Thread(new StreamCapturer(false, new String[0], parseGeoBoxes())).start();				
 			}
 			return;
 		}
 		
 		//Expansion Engine
 		String expEngineName = (String)parser.getOptionValue(expEngineNameOpt, "wikiminer");
 		if(!expEngineName.equalsIgnoreCase("wikiminer")){
 			printUsage();
 			System.exit(-1);
 		}
 		
 		//Sources
 		List<SourceType> sources = new ArrayList<SourceType>();
 		if(parser.getOptionValue(twitterSrcOpt)!=null){
 			sources.add(SourceFactory.SourceType.TWITTER);
 		}
 		if(parser.getOptionValue(teamlifeSrcOpt)!=null){
 			sources.add(SourceFactory.SourceType.TEAMLIFE);
 		}
 		if(parser.getOptionValue(localDBSrcOpt)!=null){
 			sources.add(SourceFactory.SourceType.LOCALDB);
 		}
 		
 		//Loggers
 		ResultLogger logger = new ResultLogger();
 		if(parser.getOptionValue(consoleLogOpt)!=null){
 			logger.addLogEngine(new ConsoleLogger());			
 		}
 		if(parser.getOptionValue(csvLogOpt)!=null){
 			logger.addLogEngine(new CSVLogger());			
 		}
 		if(parser.getOptionValue(folderLogOpt)!=null){
 			logger.addLogEngine(new FolderLogger());			
 		}
 		if(parser.getOptionValue(rpcLogOpt)!=null){
 			logger.addLogEngine(new RPCRemoteLogger());			
 		}
 		
 		//UserId (Fixed ATM)
 		long userId = 1; //TODO: handle multiple users
 		
 		//Query
 		String queryString = (String)parser.getOptionValue(queryStringOpt, "");		
 		
 		//Since and Until
 		DateTimeFormatter dateParser = DateTimeFormat.forPattern("yyyy-MM-dd");
 		
 		DateTime startTime = new DateTime().minusDays(0);
 		String startTimeOptString = (String)parser.getOptionValue(startTimeOpt);
 		if(startTimeOptString!=null){
 			startTime = dateParser.parseDateTime(startTimeOptString);
 		}
 		
 		DateTime endTime = new DateTime().minusDays(0);
 		String endTimeOptString = (String)parser.getOptionValue(endTimeOpt);
 		if(endTimeOptString!=null){
 			endTime = dateParser.parseDateTime(endTimeOptString);
 		}
 		
 		//Near location
 		double nearLat = (Double)parser.getOptionValue(nearLatOpt, new Double(0));
 		double nearLon = (Double)parser.getOptionValue(nearLonOpt, new Double(0));
 		int radius = (Integer)parser.getOptionValue(radiusOpt, 0);
 		
 		//Granularities
 		String timeGranularity = (String)parser.getOptionValue(timeGranOpt, "day");
 		if(!(timeGranularity.equalsIgnoreCase("hour") || timeGranularity.equalsIgnoreCase("day") || timeGranularity.equalsIgnoreCase("week"))){
 			printUsage();
 			System.exit(-1);
 		}
 		String geoGranularity = (String)parser.getOptionValue(geoGranOpt, "city");
 		if(geoGranularity==null){
 			printUsage();
 			System.exit(-1);
 		}
 		
 		if(parser.getOptionValue(clusterAnalysisOpt)!=null){
 			//CLUSTER ANALYSIS
 			String sourceName = (String)parser.getOptionValue(sourceNameOpt, "twitter");
 			String dayAnalysis = (String)parser.getOptionValue(dayAnalysisOpt);
 			String hourAnalysis = (String)parser.getOptionValue(hourAnalysisOpt);
 			String rangeAnalysisStartDay = (String)parser.getOptionValue(rangeStartDayAnalysisOpt);
 			String rangeAnalysisEndDay = (String)parser.getOptionValue(rangeEndDayAnalysisOpt);
 			boolean considerApproxGeolocations = (Boolean)parser.getOptionValue(considerApproxGeoOpt, false);
 			
 			if(dayAnalysis!= null){
 				dateParser = DateTimeFormat.forPattern("yyyy-MM-dd");
 				startTime = dateParser.parseDateTime(dayAnalysis);
 				endTime = startTime.plusDays(1);
 				new Thread(new ClusterAnalysis(logger, userId, startTime, endTime, "day", geoGranularity, sourceName, considerApproxGeolocations)).start();
 				return;
 			} else if(hourAnalysis!=null){
 				dateParser = DateTimeFormat.forPattern("yyyy-MM-dd-kk");
 				startTime = dateParser.parseDateTime(hourAnalysis);
 				endTime = startTime.plusHours(1);
 				new Thread(new ClusterAnalysis(logger, userId, startTime, endTime, "hour", geoGranularity, sourceName, considerApproxGeolocations)).start();				
 				return;
 			} else if(rangeAnalysisStartDay!=null){
 			//	System.out.println(rangeAnalysisStartDay);
 				if(rangeAnalysisEndDay!=null){
 					dateParser = DateTimeFormat.forPattern("yyyy-MM-dd");
 					startTime = dateParser.parseDateTime(rangeAnalysisStartDay);
 					endTime = dateParser.parseDateTime(rangeAnalysisEndDay).plusDays(1);
 					new Thread(new ClusterAnalysis(logger, userId, startTime, endTime, "range", geoGranularity, sourceName, considerApproxGeolocations)).start();
 					return;
 				} else {
 					printUsage();
 					System.exit(-1);
 				}
 			} else {
 				printUsage();
 				System.exit(-1);
 			}
 			return;
 		}
 		
 		QueryHandler qHandler = new QueryHandler(expEngineName, sources, logger, userId, queryString, startTime, endTime, timeGranularity, geoGranularity, nearLat, nearLon, radius);
 		new Thread(qHandler).start();
 	}
 
 	private static double[][] parseGeoBoxes() {
 		String geoBoxes = PropertyHandler.getStringProperty("GeoBoxes");
 		String[] coords = geoBoxes.split(",");
		if(coords.length < 4){
 			return new double[0][0];
 		}
		if(coords.length > 100){
 			System.err.println("A maximum of 25 GeoBoxes is allowed, the list will be truncated");
 		}
		int limit = coords.length > 100 ? 100 : coords.length;
		double[][] result = new double[limit / 2][2];
		for(int i = 0; i < limit; i++){
			result[i/2][i%2] = Double.parseDouble( coords[i] );
 		}
 		return result;
 	}
 }
