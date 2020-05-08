 package starter;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.StringReader;
 import java.util.Date;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import settings.Settings;
 import statistic.StatisticDispatcher;
 import statistic.StatisticSettings;
 import utils.parser.ParseException;
 import utils.parser.Parser;
 import distribution.ExperimentDistribution;
 import experiment.Experiment;
 import experiment.scenario.Scenario;
 
 public class SystemStarter {
 
 	private DataFiller dataFiller;
 	private StatisticDispatcher statisticDispatcher;
 	private String curStatisticFileURL;
 	
 	
 	private String	viabilitySettingsPath,
 					posteritySettingPath,
 					movePossibilitiesPath,
 					experimentInfoPath,
 					scenarioPath;
	private int remainingExperints;
 	private int curExperiment;
 	private int numberOfModelingYears;
 	private int zoneMultiplier;
 	private double capacityMultiplier;
 	private String statisticSettingsString;
 	
 	long timeOfStart;
 
 	public SystemStarter(
 			Map<Input,String> pathesMap,
 			int zoneMultiplier,
 			double capacityMultiplier,
 			int curExperiment,
 			int numberOfExperints,
 			int numberOfYears,
 			String statisticSettings){
 		this.viabilitySettingsPath = pathesMap.get(Input.VIABILITY);
 		this.posteritySettingPath = pathesMap.get(Input.POSTERITY);
 		this.movePossibilitiesPath = pathesMap.get(Input.MOVE_POSSIBILITIES);
 		this.scenarioPath = pathesMap.get(Input.SCENARIO);
 		this.experimentInfoPath = pathesMap.get(Input.INITIATION);
 		this.zoneMultiplier = zoneMultiplier;
 		this.capacityMultiplier = capacityMultiplier;
 		this.curExperiment = curExperiment;
		this.remainingExperints = numberOfExperints;
 		this.numberOfModelingYears = numberOfYears;
 		this.statisticSettingsString = statisticSettings;
 		new Parser(new StringReader(statisticSettingsString));
 	}
 	
 	public void startSystem() throws Exception {
 		createStatisticDispatcher(statisticSettingsString);
 		timeOfStart = System.currentTimeMillis();
 		readData();
 		Settings.init(dataFiller.getViabilityTable(), dataFiller.getPosterityTable(), dataFiller.getMovePosibilitiesTable());
 		if (curExperiment == -1)
 			curExperiment = 0;
		if (remainingExperints == -1)
			remainingExperints = 1;
 		runExperints();
 		finish();
 	}
 	
 	private void readData() throws Exception {
 		BufferedReader posteritySettingsReader;
 		BufferedReader viabilitySettingsReader;
 		BufferedReader movePossibilitiesReader;
 		BufferedReader scenarioReader;
 		BufferedReader experimentInfoReader;
 		viabilitySettingsReader = new BufferedReader(new FileReader(viabilitySettingsPath));
 		posteritySettingsReader = new BufferedReader(new FileReader(posteritySettingPath));
 		if (movePossibilitiesPath.endsWith(Shared.DEFAULT_MAP_FILE))
 			movePossibilitiesReader = null;
 		else
 			movePossibilitiesReader = new BufferedReader(new FileReader(movePossibilitiesPath));
 		experimentInfoReader = new BufferedReader(new FileReader(experimentInfoPath));
 		scenarioReader = new BufferedReader(new FileReader(scenarioPath));
 		dataFiller = new DataFiller(viabilitySettingsReader, posteritySettingsReader, movePossibilitiesReader, scenarioReader, experimentInfoReader, zoneMultiplier);
 	}
 	
 	private void createStatisticDispatcher(String statisticSettings) throws ParseException, Exception {
 		Date d = new Date();
 		curStatisticFileURL = String.format("statistics/%tY_%tm_%td %tH-%tM-%tS", d, d, d, d, d, d) + 
 							  ((curExperiment==-1)?(""):(" e"+curExperiment)) + ".csv";
 		Parser.ReInit(new StringReader(statisticSettings));
 		StatisticSettings settings = Parser.statisticSettings();
 		statisticDispatcher	= new StatisticDispatcher(curStatisticFileURL, settings);
 	}
 	
 	private void runExperints() {
 		ExperimentDistribution firstDistribution = dataFiller.getExperimentDistribution();
 		Scenario scenario = dataFiller.getScenario();
 		Experiment experiment = new Experiment(firstDistribution, scenario, numberOfModelingYears, statisticDispatcher, capacityMultiplier);
		while (remainingExperints > 0) {
 			experiment.runWitExperimentNumber(curExperiment);
			remainingExperints--;
 		}
 	}
 	
 	private void finish() {
 		long executingTime = System.currentTimeMillis()-timeOfStart,
 			 hour = executingTime/1000/60/60,
 			 min = executingTime/1000/60 - hour*60,
 			 sec = executingTime/1000 - min*60 - hour*3600,
 			 msec = executingTime - sec*1000 - min*60000 - hour*3600000;
 		Logger.getLogger("runningTimeLogger").info(String.format("Executing time:	[%2s:%2s:%2s.%3s]",hour,min,sec,msec) + "  With args: " + MainClass.getStartArgs());
 		statisticDispatcher.finish();
 	}
 }
