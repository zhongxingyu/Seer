 package starter;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.Date;
 import java.util.List;
 
 import statistic.LastYearStatisticWriter;
 import statistic.StatisticDispatcher;
 import statistic.StatisticSettings;
 import utils.parser.ParseException;
 import utils.parser.Parser;
 import experiment.Experiment;
 import experiment.ZoneSettings;
 import experiment.scenario.Scenario;
 
 public class SystemStarter {
 
 	private String	viabilitySettingsPath,
 					posteritySettingPath,
 					movePossibilitiesPath,
 					distributionInfoPath,
 					scenarioPath,
 					dimensionsConfPath;
 	private String	viabilitySettingsContent,
 					posteritySettingContent,
 					movePossibilitiesContent,
 					distributionInfoContent,
 					scenarioContent;
 
 	private DataFiller dataFiller;
 	private StatisticDispatcher statisticDispatcher;
 	private LastYearStatisticWriter lastYearStatisticWriter;
 	private String curStatisticFileURL;
 	private InputsPreparer inputsPreparer;
 	private Experiment activeExperiment;
 	private StringBuilder settingsStatistic = new StringBuilder();
 	
 	private int numberOfModelingExperints;
 	private int curExperiment;
 	private int curPoint;
 	private int numberOfPoints;
 	private int numberOfModelingYears;
 	private String statisticSettings;
 	private double capacityMultiplier;
 	
 	private long timeOfStart;
 
 	public SystemStarter() throws Exception {
 		createFolders();
 		this.curExperiment = (Integer) Argument.CURRENT_EXPERIMENT.getValue();
 		this.curPoint = (Integer) Argument.POINT_NUMBER.getValue();
 		this.numberOfModelingExperints = (Integer) Argument.NUMBER_OF_EXPERIMENTS.getValue();
 		this.numberOfModelingYears = (Integer) Argument.YEARS.getValue();
 		this.statisticSettings = (String) Argument.STATISTIC.getValue();
 		this.capacityMultiplier = (Double) Argument.CAPACITY_MULTIPLIER.getValue();
 		String projectPath = (String) Argument.PROJECT_PATH.getValue();
 		this.viabilitySettingsPath = getPathOf(Argument.VIABILITY, projectPath);
 		this.posteritySettingPath = getPathOf(Argument.POSTERITY, projectPath);
 		this.movePossibilitiesPath = getPathOf(Argument.MOVE_POSSIBILITIES, projectPath);
 		this.scenarioPath = getPathOf(Argument.SCENARIO, projectPath);
 		this.distributionInfoPath = getPathOf(Argument.INITIATION, projectPath);
 		this.dimensionsConfPath = (String) Argument.DIMENSIONS_TO_TEST.getValue();
 	}
 	
 	private void createFolders() {
 		createLogsFolder();
 		createSettingsFolder();
 		createStatisticFolder();
 	}
 	
 	public void startSystem() throws IOException, ParseException {
 		new Parser(new StringReader(statisticSettings));
 		inputsPreparer = new InputsPreparer(dimensionsConfPath);
 		lastYearStatisticWriter = new LastYearStatisticWriter(getShortStatisticFileName());
 		readSettingsFiles();
 		numberOfPoints = inputsPreparer.maxPointNumber()+1;
 		if (curExperiment == -1)
 			curExperiment = 0;
 		if (numberOfModelingExperints == -1)
 			numberOfModelingExperints = 1;
 		if (curPoint == -1)
 			curPoint = 1;
 		else
			numberOfPoints = 1;
 		runPoints();
 		lastYearStatisticWriter.finish();
 	}
 	
 	private void readSettingsFiles() throws IOException {
 		viabilitySettingsContent = null;
 		posteritySettingContent = null;
 		movePossibilitiesContent = null;
 		distributionInfoContent = null;
 		scenarioContent = null;
 		viabilitySettingsContent = getFullFileContent(viabilitySettingsPath);
 		posteritySettingContent = getFullFileContent(posteritySettingPath);
 		movePossibilitiesContent = getFullFileContent(movePossibilitiesPath);
 		distributionInfoContent = getFullFileContent(distributionInfoPath);
 		scenarioContent = getFullFileContent(scenarioPath);
 	}
 	
 	private String getFullFileContent(String inputPath) throws IOException {
 		BufferedReader inputReader = null;
 		FileReader fileReader = null;
 		try {
 			fileReader = new FileReader(inputPath);
 			inputReader = new BufferedReader(fileReader);
 			StringBuilder builder = new StringBuilder();
 			String line;
 			while ((line = inputReader.readLine()) != null)
 				builder.append(line).append('\n');
 			return builder.toString();
 		} finally {
 			if (inputReader != null)
 				inputReader.close();
 			if (fileReader != null)
 				fileReader.close();
 		}
 	}
 	
 	private void runPoints() throws IOException, ParseException {
 		int startExperiment = curExperiment;
 		while (curPoint <= numberOfPoints) {
 			dataFiller = null;
 			dataFiller = getDataFiller();
 			dataFiller.read();
 			saveSettingsPack();
 			runExperints();
 			curPoint++;
 			curExperiment = startExperiment;
 		}
 	}
 	
 	private DataFiller getDataFiller() throws IOException {
 		inputsPreparer.setPoint(curPoint-1);
 		String viability = inputsPreparer.getPreparedContent(viabilitySettingsContent);
 		String posterity = inputsPreparer.getPreparedContent(posteritySettingContent);
 		String movePossibility = inputsPreparer.getPreparedContent(movePossibilitiesContent);
 		String scenario = inputsPreparer.getPreparedContent(scenarioContent);
 		String distributionInfo = inputsPreparer.getPreparedContent(distributionInfoContent);
 		settingsStatistic.setLength(0);
 		settingsStatistic.append(viability).append("\n\n\n");
 		settingsStatistic.append(posterity).append("\n\n\n");
 		settingsStatistic.append(movePossibility).append("\n\n\n");
 		settingsStatistic.append(scenario).append("\n\n\n");
 		settingsStatistic.append(distributionInfo);
 		return new DataFiller(viability, posterity, movePossibility, scenario, distributionInfo, capacityMultiplier);
 	}
 	
 	private StatisticDispatcher createStatisticDispatcher() throws ParseException, IOException {
 		curStatisticFileURL = getStatisticFileName();
 		Parser.ReInit(new StringReader(statisticSettings));
 		StatisticSettings settings = Parser.statisticSettings();
 		return new StatisticDispatcher(curStatisticFileURL, settings,
 				dataFiller.getZonesSettings().get(0));		// #TODO terrible stub!!!
 	}
 	
 	private void saveSettingsPack() throws IOException {
 		BufferedWriter settingsStatisticWriter = null;
 		try {
 			FileWriter fileWriter = new FileWriter(getSettingsStatisticFileName());
 			settingsStatisticWriter = new BufferedWriter(fileWriter);
 			settingsStatisticWriter.write(settingsStatistic.toString());
 			settingsStatisticWriter.flush();
 		}catch (IOException e) {
 			StringBuilder errorMsg = new StringBuilder();
 			errorMsg.append("Writting of settings pack was failed!\n");
 			errorMsg.append(Shared.printStack(e));
 			Shared.problemsLogger.error(errorMsg.toString());
 		}finally {
 			if (settingsStatisticWriter != null)
 				settingsStatisticWriter.close();
 		}
 	}
 
 	private String getStatisticFileName() {
 		String outputFolder = (String) Argument.OUTPUTS_FOLDER.getValue();
 		String statisticsFolder = buildPathName(outputFolder, Shared.STATISTICS_FOLDER);
 		return getStatisticFilePrefix(statisticsFolder).append(".csv").toString();
 	}
 	
 	private String getSettingsStatisticFileName() {
 		String outputFolder = (String) Argument.OUTPUTS_FOLDER.getValue();
 		String settingsFolder = buildPathName(outputFolder, Shared.SETTINGS_FOLDER);
 		return getStatisticFilePrefix(settingsFolder).append(" settings.csv").toString();
 	}
 	
 	private String getShortStatisticFileName() {
 		String outputFolder = (String) Argument.OUTPUTS_FOLDER.getValue();
 		String statisticsFolder = buildPathName(outputFolder, Shared.STATISTICS_FOLDER);
 		return getStatisticFileShortPrefix(statisticsFolder).append(".csv").toString();
 	}
 	
 	private StringBuilder getStatisticFilePrefix(String folderName) {
 		String experimentSeriesName = null;
 		try {
 			experimentSeriesName = (String) Argument.EXPERIMENTS_SERIES_NAME.getValue();
 		} catch (Exception e) {
 			experimentSeriesName = "Statistic";
 		}
 		StringBuilder result = new StringBuilder(folderName).append("/");
 		result.append(experimentSeriesName);
 		int x = signsInPointNumber();
 		result.append(String.format(" -p %0"+x+"d", curPoint));
 		result.append(String.format(" -e %03d", curExperiment));
 		result.append(String.format(" -E %03d", numberOfModelingExperints));
 		Date d = new Date();
 		result.append(String.format(" %tY_%tm_%td %tH-%tM-%tS", d, d, d, d, d, d));
 		return result;
 	}
 	
 	private StringBuilder getStatisticFileShortPrefix(String folderName) {
 		String experimentSeriesName = null;
 		try {
 			experimentSeriesName = (String) Argument.EXPERIMENTS_SERIES_NAME.getValue();
 		} catch (Exception e) {
 			experimentSeriesName = "Statistic";
 		}
 		StringBuilder result = new StringBuilder(folderName).append("/");
 		result.append(experimentSeriesName);
 		int x = signsInPointNumber();
 		if (curPoint == -1)
 			result.append(String.format(" -p %0"+x+"d-%0"+x+"d", 1, inputsPreparer.maxPointNumber()+1));
 		else
 			result.append(String.format(" -p %0"+x+"d", curPoint));
 		result.append(String.format(" -e %03d", curExperiment));
 		return result;
 	}
 	
 	private void runExperints() throws ParseException, IOException {
 		List<ZoneSettings> zonesSettings = dataFiller.getZonesSettings();
 		Scenario scenario = dataFiller.getScenario();
 		lastYearStatisticWriter.openNewPoint(inputsPreparer.getPrevPointValuesMap(), curPoint);
 		int remainingExperints = numberOfModelingExperints;
 		while (remainingExperints > 0) {
 			timeOfStart = System.currentTimeMillis();
 			statisticDispatcher = null;
 			statisticDispatcher = createStatisticDispatcher();
 			activeExperiment = new Experiment(zonesSettings, scenario, numberOfModelingYears);
 			activeExperiment.runWitExperimentNumber(curExperiment++, statisticDispatcher);
 			lastYearStatisticWriter.write(activeExperiment.getLastYearStatistic());
 			remainingExperints--;
 			finish();
 		}
 	}
 	
 	private void finish() {
 		long executingTime = System.currentTimeMillis()-timeOfStart,
 			 hour = executingTime/1000/60/60,
 			 min = executingTime/1000/60 - hour*60,
 			 sec = executingTime/1000 - min*60 - hour*3600,
 			 msec = executingTime - sec*1000 - min*60000 - hour*3600000;
		Shared.infoLogger.info(String.format("Executing time:	[%2s:%2s:%2s.%3s]",hour,min,sec,msec) + "  With args: " + MainClass.getStartArgs());
 		statisticDispatcher.finish();
 	}
 	
 	static private String getPathOf(Argument argument, String projectPath) throws Exception {
 		return projectPath + '/' + (String) argument.getValue();
 	}
 	
 	private static void createLogsFolder() {
 		File logsFolder = new File(Shared.LOGS_FOLDER+"/");
 		if (!logsFolder.exists())
 			logsFolder.mkdir();
 	}
 	
 	private static void createSettingsFolder() {
 		String outputFolder = (String) Argument.OUTPUTS_FOLDER.getValue();
 		String settingsFolder = buildPathName(outputFolder, Shared.SETTINGS_FOLDER);
 		File settingsFolderFile = new File(settingsFolder);
 		if (!settingsFolderFile.exists())
 			settingsFolderFile.mkdir();
 	}
 	
 	private static void createStatisticFolder() {
 		String outputFolder = (String) Argument.OUTPUTS_FOLDER.getValue();
 		String statisticcFolder = buildPathName(outputFolder, Shared.STATISTICS_FOLDER);
 		File statisticsFolderFile = new File(statisticcFolder);
 		if (!statisticsFolderFile.exists())
 			statisticsFolderFile.mkdir();
 	}
 	
 	private static String buildPathName(String prefix, String suffix) {
 		prefix = deleteSlashes(prefix);
 		suffix = deleteSlashes(suffix);
 		if (prefix.isEmpty())
 			return suffix + "/";
 		return prefix + "/" + suffix + "/";
 	}
 	
 	private static String deleteSlashes(String string) {
 		int numberOfFirstSlashes = 0;
 		int numberOfLastSlashes = 0;
 		for (int i=0; i<string.length() && string.charAt(i)=='/'; i++)
 			numberOfFirstSlashes++;
 		for (int i=string.length()-1; i>=0 && string.charAt(i)=='/'; i--)
 			numberOfLastSlashes++;
 		return string.substring(numberOfFirstSlashes, string.length()-numberOfLastSlashes);
 	}
 	
 	private int signsInPointNumber() {
 		int number = inputsPreparer.maxPointNumber() + 1;
 		int signs;
 		for (signs=0; number>0; signs++)
 			number /= 10;
 		return signs==0? 1 : signs;
 	}
 }
