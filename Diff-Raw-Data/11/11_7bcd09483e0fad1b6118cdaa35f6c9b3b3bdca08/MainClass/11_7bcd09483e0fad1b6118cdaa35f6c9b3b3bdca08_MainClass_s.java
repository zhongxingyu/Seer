 package starter;
 
 import individual.IndividualsManagerDispatcher;
 import jade.core.NotFoundException;
 import jade.core.Profile;
 import jade.core.ProfileImpl;
 import jade.core.Runtime;
 import jade.wrapper.AgentController;
 import jade.wrapper.ContainerController;
 import jade.wrapper.StaleProxyException;
 
 import java.util.Hashtable;
 
 public class MainClass {
 		
 	final static String DEFAULT_MAP = "*";
 	private static Hashtable<String, ArgPair> arguments = new Hashtable<String, ArgPair>();
 	private static CmdLineParser parser = new CmdLineParser();
 	static Runtime runtime;
 	static ContainerController container;		
 	static AgentController starter;
 	
 	private static class ArgPair {
 		public CmdLineParser.Option option;
 		public Object defaultValue;
 		public ArgPair(CmdLineParser.Option opt, Object def) { this.option = opt; this.defaultValue = def; }
 	}
 	
 	@SuppressWarnings("static-access")
 	public static void main(String[] args) {
 		parseArgs(args);
 		
 		MainClass main = new MainClass();
 		main.initContainerControllers();
 		main.start();
 	}
 	
 	private static void printUsage() {
         System.out.println(
 		"Usage: [--help] " +
 			"	[{-y, --years} int]  number of simulated years | DEFAULT 1\n" +
 			"	[{-e, --cur_experiment} int]  curent experiment number | DEFAULT -1\n" +
 			"	                              (!= -1: for runing on cluster,\n" +
 			"	                              DON'T use this argument whit -E)\n" +
 			"	[{-E, --number_of_experiments} int]  number of simulated experiments | DEFAULT -1\n" +
 			"	                              (== -1: for runing on cluster,\n" +
 			"	                              DON'T use this argument whit -e)\n" +
 			"	[{-M, --indiv_multiplier} int]  temporary argument for control size of population | DEFAULT 10\n" +
 			"	[{-z, --zone_multiplier} int]  temporary argument for control number of zones | DEFAULT 1\n" +
 			"	[{-f, --project_path} string]  directory for files with settings | DEFAULT user.dir\n" +
 			"	[{-v, --viability} string]  name of file with viability settings | DEFAULT \'Viability.csv\'\n" +
 			"	[{-m, --map} string]  name of file with map settings | DEFAULT \'DEFAULT_MAP\'\n" +
 			"	[{-p, --posterity} string]  name of file with posterity settings | DEFAULT \'Posterity.csv\'\n" +
 			"	[{-s, --scenario} string]  name of file with scenario settings | DEFAULT \'Scenario.scn\'\n" +
 			"	[{-i, --initiation} string]  name of file with initiation settings | DEFAULT \'Initiation.hpsi\'\n" +
 			"	[{-o, --object_manager} int(0|1|2)]  0 - simple creation of individuals,\n" +
 			"	                                     1 - creation of individuals with using of object pull,\n" +
 			"	                                     2 - creation of individuals with using of object pulls\n" +
 			"	                                     | DEFAULT 0\n" +
 			"	[{-P, --port} int]");
     }
 	
 	private static void parseArgs(String[] args) {
 		arguments.put("help",
 				new ArgPair(parser.addBooleanOption("help"), Boolean.FALSE));
 		
 		arguments.put("years",
 				new ArgPair(parser.addIntegerOption('y', "years"), new Integer(1)));
 		arguments.put("cur_experiment",
 				new ArgPair(parser.addIntegerOption('e', "cur_experiment"), new Integer(-1)));
 		arguments.put("number_of_experiments",
 				new ArgPair(parser.addIntegerOption('E', "number_of_experiments"), new Integer(-1)));
 		arguments.put("indiv_multiplier",
 				new ArgPair(parser.addIntegerOption('M', "indiv_multiplier"), new Integer(10)));
 		arguments.put("zone_multiplier",
 				new ArgPair(parser.addIntegerOption('z', "zone_multiplier"), new Integer(1)));
 		arguments.put("object_manager",
 				new ArgPair(parser.addIntegerOption('o', "object_manager"), new Integer(0)));
 		arguments.put("sniffer",
 				new ArgPair(parser.addBooleanOption("sniffer"), Boolean.FALSE));
 		arguments.put("introspector",
 				new ArgPair(parser.addBooleanOption("introspector"), Boolean.FALSE));
 		
 		arguments.put("project_path",
 				new ArgPair(parser.addStringOption('f', "project_path"), Pathes.PROJECT_PATH));
 		arguments.put("viability",
 				new ArgPair(parser.addStringOption('v', "viability"), "Viability.csv"));
 		arguments.put("posterity",
 				new ArgPair(parser.addStringOption('p', "posterity"), "Posterity.csv"));
 		//QM
 		arguments.put("port",
 				new ArgPair(parser.addIntegerOption('P', "port"), new Integer(0)));
 		arguments.put("movePossibilities",
 				new ArgPair(parser.addStringOption('m', "map"), DEFAULT_MAP));			// movePossibilities
 		arguments.put("scenario",
 				new ArgPair(parser.addStringOption('s', "scenario"), "Scenario.scn"));
 		arguments.put("initiation",
 				new ArgPair(parser.addStringOption('i', "initiation"), "Initiation.hpsi"));
 		try {
             parser.parse(args);
         }
         catch(CmdLineParser.OptionException e) {
             System.out.println(e.getMessage());
             printUsage();
             System.exit(2);
         }
 		
 		if(parser.getOptionValue(arguments.get("help").option) != null) {
 			printUsage();
             System.exit(0);
 		}
 	}
 	
 	public static Object getArgument(String name) throws NotFoundException {
 		if(!arguments.containsKey(name)) throw new NotFoundException();
 		ArgPair pair = arguments.get(name);
 		return parser.getOptionValue(pair.option, pair.defaultValue);
 	}
 	
 	static void initContainerControllers(){
 		runtime = Runtime.instance();
 		Profile pf = null;
 		try {
 			pf = new ProfileImpl(null, (Integer)getArgument("port") + 8899, null);
 		}
 		catch (NotFoundException e) {
 			e.printStackTrace();
 		}
 		OSInfoOverride osio = new OSInfoOverride();
 		try {
 			container = runtime.createMainContainer(pf);
 		}
 		finally {
 			osio.dispose();
 		}
 	}
 	
 	static void start(){
 		try {
 			String proj_path = (String)getArgument("project_path");
 			
 			Object[] startArgs = new Object[] {
 					proj_path + '/' + (String)getArgument("viability"),
 					proj_path + '/' + (String)getArgument("posterity"),
 					proj_path + '/' + (String)getArgument("movePossibilities"),
 					proj_path + '/' + (String)getArgument("scenario"),
 					proj_path + '/' + (String)getArgument("initiation"),
 					(Integer)getArgument("indiv_multiplier"),
 					(Integer)getArgument("zone_multiplier"),
 					(Integer)getArgument("cur_experiment"),
 					(Boolean)getArgument("sniffer"),
 					(Boolean)getArgument("introspector")
 			};
			
 			IndividualsManagerDispatcher.setDispatchingMode((Integer)getArgument("object_manager"));
 			starter = container.createNewAgent("SystemStarter", "starter.SystemStarter", startArgs);
 			starter.start();
 		}
 		catch (StaleProxyException e) {
 			e.printStackTrace();
 		}
 		catch (NotFoundException e) {
 			e.printStackTrace();
 		} 
 	}
 	
 	static void shutDown() {
 		runtime.shutDown();
 		System.exit(0);
 	}
 }
