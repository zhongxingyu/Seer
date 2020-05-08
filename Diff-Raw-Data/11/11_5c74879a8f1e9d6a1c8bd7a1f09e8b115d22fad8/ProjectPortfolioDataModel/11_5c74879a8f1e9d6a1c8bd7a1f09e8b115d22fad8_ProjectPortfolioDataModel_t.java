 package org.hackystat.projectbrowser.page.projectportfolio.detailspanel;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.transform.stream.StreamSource;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.model.Model;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserProperties;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.projectbrowser.page.loadingprocesspanel.Processable;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.MiniBarChart;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.StreamClassifier;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.
         StreamParticipationClassifier;
 import org.hackystat.projectbrowser.page.projectportfolio.detailspanel.chart.StreamTrendClassifier;
 import org.hackystat.projectbrowser.page.projectportfolio.jaxb.Measures;
 import org.hackystat.projectbrowser.page.projectportfolio.jaxb.PortfolioDefinitions;
 import org.hackystat.projectbrowser.page.projectportfolio.jaxb.Measures.Measure;
 import org.hackystat.projectbrowser.page.telemetry.TelemetrySession;
 import org.hackystat.sensorbase.resource.projects.ProjectUtils;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.telemetry.analyzer.configuration.ExtensionFileFilter;
 import org.hackystat.telemetry.service.client.TelemetryClient;
 import org.hackystat.telemetry.service.client.TelemetryClientException;
 import org.hackystat.telemetry.service.resource.chart.jaxb.ParameterDefinition;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryChartData;
 import org.hackystat.utilities.logger.HackystatLogger;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.utilities.uricache.UriCache;
 import org.xml.sax.SAXException;
 
 /**
  * Data model to hold state of Project Portfolio.
  * 
  * @author Shaoxuan Zhang
  */
 public class ProjectPortfolioDataModel implements Serializable, Processable {
 
   /** Support serialization. */
   private static final long serialVersionUID = 5465041655927215391L;
   /** state of data loading process. */
   private volatile boolean inProcess = false;
   /** result of data loading. */
   private volatile boolean complete = false;
   /** message to display when data loading is in process. */
   private String processingMessage = "";
 
   /** host of the telemetry host. */
   private String telemetryHost;
   /** email of the user. */
   private String userEmail;
   /** password of the user. */
   private String password;
   /** The telemetry session. */
   private TelemetrySession telemetrySession;
 
   /** the granularity this data model focus. */
   private String granularity = "Week";
   /** If current day, week or month will be included in portfolio. */
   // private boolean includeCurrentWeek = true;
   /** The start date this user has selected. */
   private long startDate = 0;
   /** The end date this user has selected. */
   private long endDate = 0;
   /** List of available stream classifiers. */
   public static final List<String> availableClassifiers = Arrays.asList(new String[]{
                                                   "None",
                                                   StreamTrendClassifier.name,
                                                   StreamParticipationClassifier.name
                                               });
 
   /**
    * the time phrase this data model focus. In scale of telemetryGranularity, from current to the
    * past.
    */
   // private int timePhrase = 5;
   /** The projects this user has selected. */
   private List<Project> selectedProjects = new ArrayList<Project>();
   /** The charts in this model. */
   private Map<Project, List<MiniBarChart>> measuresCharts = 
     new HashMap<Project, List<MiniBarChart>>();
   /** The List of PortfolioMeasureConfiguration. */
   private final List<PortfolioMeasureConfiguration> measures = 
     new ArrayList<PortfolioMeasureConfiguration>();
   /** Alias for measure. Maps names from definition to names for display. */
   //private final Map<String, String> measureAlias = new HashMap<String, String>();
   /** The portfolio measure configuration loaded from xml file. */
   //private PortfolioDefinitions portfolioDefinitions = getPortfolioDefinitions();
   /** The configuration saving capacity. */
   private static Long capacity = 1000L;
   /** The max life of the saved configuration. */
   private static Double maxLife = 300.0;
 
   /** The background color for table cells. */
   private String backgroundColor = "000000";
   /** The font color for table cells. */
   private String fontColor = "ffffff";
   /** The font color for N/A. */
   private String naColor = "888888";
   /** The color for good state. */
   private String goodColor = "00ff00";
   /** The color for soso state. */
   private String averageColor = "ffff00";
   /** The color for bad state. */
   private String poorColor = "ff0000";
 
   /**
    * Constructor that initialize the measures.
    * 
    * @param telemetryHost the telemetry host
    * @param userEmail the user's email
    * @param password the user's passowrd
    */
   public ProjectPortfolioDataModel(String telemetryHost, String userEmail, String password) {
     telemetrySession = ProjectBrowserSession.get().getTelemetrySession();
     this.telemetryHost = telemetryHost;
     this.userEmail = userEmail;
     this.password = password;
     this.initializeMeasures();
     this.loadUserConfiguration();
     // List<ParameterDefinition> paramDefList =
     // telemetrySession.getParameterList(measure.getName());
   }
 
   /**
    * @param startDate the start date.
    * @param endDate the end date.
    * @param selectedProjects the selected projects.
    * @param granularity the granularity this data model focus.
    */
   public void setModel(long startDate, long endDate, List<Project> selectedProjects,
       String granularity) {
     this.startDate = startDate;
     this.endDate = endDate;
     this.granularity = granularity;
     this.selectedProjects = selectedProjects;
     for (PortfolioMeasureConfiguration measure : getEnabledMeasures()) {
       checkMeasureParameters(measure);
     }
     measuresCharts.clear();
   }
 
   /**
    * Initialize the measure configurations.
    */
   private void initializeMeasures() {
     // Load default measures
     measures.clear();
     
     // Load additional user customized measures.
     PortfolioDefinitions portfolioDefinitions = getPortfolioDefinitions();
     if (portfolioDefinitions != null) {
       for (Measure measure : portfolioDefinitions.getMeasures().getMeasure()) {
         measures.add(new PortfolioMeasureConfiguration(measure.getName(), measure.getAlias(), 
             measure.getMerge(), measure.isEnabled(), getClassifier(measure), this));
       }
     }
     for (PortfolioMeasureConfiguration measure : getEnabledMeasures()) {
       checkMeasureParameters(measure);
     }
   }
 
   /**
    * Get a {@link StreamClassifier} according to the given Measure.
    * @param measure a given {@link Measure} object.
    * @return a {@link StreamClassifier} instance, 
    * null if no supported classifier defined in the Measure object.
    */
   protected static StreamClassifier getClassifier(Measure measure) {
    if (measure == null) {
      return null;
    }
     String classifierMethod = measure.getClassifierMethod();
     if ("StreamTrend".equalsIgnoreCase(classifierMethod)) {
       if (measure.getStreamTrendParameters() == null) {
         return new StreamTrendClassifier(0, 0, true);
       }
       Double lowerThreshold = measure.getStreamTrendParameters().getLowerThresold();
       if (lowerThreshold == null) {
         lowerThreshold = 0.0;
       }
       Double higherThreshold = measure.getStreamTrendParameters().getHigherThresold();
       if (higherThreshold == null) {
         higherThreshold = 0.0;
       }
       Boolean higherBetter = measure.getStreamTrendParameters().isHigherBetter();
       if (higherBetter) {
         higherBetter = true;
       }
       return new StreamTrendClassifier(lowerThreshold, higherThreshold, higherBetter);
     }
     else if ("Participation".equalsIgnoreCase(classifierMethod)) {
       if (measure.getParticipationParameters() == null) {
         return new StreamParticipationClassifier(50, 0, 50);
       }
       Double memberPercentage = measure.getParticipationParameters().getMemberPercentage();
       if (memberPercentage == null) {
         memberPercentage = 50.0;
       }
       Double valueThreshold = measure.getParticipationParameters().getThresoldValue();
       if (valueThreshold == null) {
         valueThreshold = 0.0;
       }
       Double frequencyPercentage = 
         measure.getParticipationParameters().getFrequencyPercentage();
       if (frequencyPercentage == null) {
         frequencyPercentage = 50.0;
       }
       return new StreamParticipationClassifier(
           memberPercentage, valueThreshold, frequencyPercentage);
     }
     return null;
   }
   
   /**
    * Print the {@link Measure} for logging purpose.
    * 
    * @param measure the {@link Measure} to log.
    * @return the string
    */
   private String printPortfolioMeasure(Measure measure) {
     String s = "/";
     String classifierParameters = "";
     if (measure.getStreamTrendParameters() != null) {
       classifierParameters = measure.getStreamTrendParameters().getLowerThresold() + s +
                              measure.getStreamTrendParameters().getHigherThresold() + s + 
                              measure.getStreamTrendParameters().isHigherBetter();
     }
     if (measure.getParticipationParameters() != null) {
       classifierParameters = measure.getParticipationParameters().getMemberPercentage() + s +
                              measure.getParticipationParameters().getThresoldValue() + s +
                              measure.getParticipationParameters().getFrequencyPercentage();
     }
     return "<" + measure.getName() + ": " + s + measure.isEnabled() + s
         + measure.getClassifierMethod() + s + classifierParameters + s + 
         measure.getTelemetryParameters() + "> ";
   }
 
   /**
    * Save user's configuration to system's cache.
    * 
    * @return change maked in this saving.
    */
   public String saveUserConfiguration() {
     StringBuffer log = new StringBuffer();
     UriCache userCache = this.getUserConfiguartionCache();
     for (PortfolioMeasureConfiguration measure : this.measures) {
       String uri = userEmail + "/portfolio/" + measure.getName();
       Measure oldMeasure = null;
       if (userCache.get(uri) instanceof Measure) {
         oldMeasure = (Measure) userCache.get(uri);
       }
       else {
         userCache.remove(uri);
       }
       Measure newMeasure = this.getMeasure(measure);
       if (oldMeasure == null || !oldMeasure.equals(newMeasure)) {
         mergeMeasures(newMeasure, oldMeasure);
         userCache.put(uri, newMeasure);
         log.append(printPortfolioMeasure(newMeasure));
       }
     }
 
     if (log.length() > 0) {
       ProjectBrowserSession.get().logUsage("PORTFOLIO CONFIGURATION: {changed} " + log.toString());
     }
     return log.toString();
   }
 
   /**
    * Merge the old measure into the new one.
    * @param newMeasure The new Measure.
    * @param oldMeasure The old Measure.
    */
   private void mergeMeasures(Measure newMeasure, Measure oldMeasure) {
     if (oldMeasure == null) {
       return;
     }
     if (newMeasure.getStreamTrendParameters() == null) {
       newMeasure.setStreamTrendParameters(oldMeasure.getStreamTrendParameters());
     }
     if (newMeasure.getParticipationParameters() == null) {
       newMeasure.setParticipationParameters(oldMeasure.getParticipationParameters());
     }
   }
 
   /**
    * Return a {@link Measure} represents the given PortfolioMeasureConfiguration.
    * @param measureConfiguration a {@link PortfolioMeasureConfiguration}.
    * @return a {@link Measure}.
    */
   private Measure getMeasure(PortfolioMeasureConfiguration measureConfiguration) {
     Measure measure = new Measure();
     measure.setName(measureConfiguration.getName());
     measure.setEnabled(measureConfiguration.isEnabled());
     measure.setTelemetryParameters(measureConfiguration.getParamtersString());
     measure.setClassifierMethod(measureConfiguration.getClassiferName());
     measureConfiguration.saveClassifierSetting(measure);
     
     return measure;
   }
   
   /**
    * Load user's configuration from system's cache.
    */
   private void loadUserConfiguration() {
     for (PortfolioMeasureConfiguration measure : this.measures) {
       Measure savedMeausre = getSavedMeasure(measure);
       if (savedMeausre != null) {
         measure.setMeasureConfiguration(savedMeausre.isEnabled(), getClassifier(savedMeausre), 
                                         savedMeausre.getTelemetryParameters());
       }
     }
   }
 
   /**
    * Get the {@link Measure} instance associated with the given 
    * {@link PortfolioMeasureConfiguration} from UriCache.
    * @param measure the given {@link PortfolioMeasureConfiguration}
    * @return the {@link Measure} instance, null if not matched found in cache.
    */
   protected final Measure getSavedMeasure(PortfolioMeasureConfiguration measure) {
     UriCache userCache = this.getUserConfiguartionCache();
     String uri = userEmail + "/portfolio/" + measure.getName();
     Measure savedMeausre = null;
     if (userCache.get(uri) instanceof Measure) {
       savedMeausre = (Measure) userCache.get(uri);
     }
     else {
       userCache.remove(uri);
     }
     return savedMeausre;
   }
   
   /**
    * Reset user's configuration cache.
    */
   public void resetUserConfiguration() {
     // UriCache userCache = this.getUserConfiguartionCache();
     // userCache.clearAll();
     this.initializeMeasures();
     String msg = this.saveUserConfiguration();
     if (msg.length() > 0) {
       ProjectBrowserSession.get().logUsage("CONFIGURATION: {reset to default} ");
     }
   }
 
   /**
    * Load the portfolio definitions from the xml file.
    * 
    * @return a PortfolioDefinitions instance. null if fail to load the file.
    */
   private PortfolioDefinitions getPortfolioDefinitions() {
     PortfolioDefinitions portfolioDefinitions = new PortfolioDefinitions();
     portfolioDefinitions.setMeasures(new Measures());
     // load the basic default definitions.
     
     getLogger().info("Reading basic portfolio definitions from: basic.portfolio.definition.xml");
     InputStream defStream = ProjectPortfolioDataModel.class
         .getResourceAsStream("basic.portfolio.definition.xml");
     PortfolioDefinitions definition = getDefinitions(defStream);
     if (definition != null) {
       portfolioDefinitions.getMeasures().getMeasure().addAll(definition.getMeasures().getMeasure());
     }
         
 
     // load user defined definitions.
     String defDirString = ((ProjectBrowserApplication) ProjectBrowserApplication.get())
         .getPortfolioDefinitionDir();
     if (defDirString != null && defDirString.length() > 0) {
       File defDir = new File(defDirString);
       File[] xmlFiles = defDir.listFiles(new ExtensionFileFilter(".xml"));
       if (xmlFiles.length > 0) {
         getLogger().info("Reading portfolio definitions from: " + defDirString);
       }
       else {
         getLogger().info("No portfolio definitions found in: " + defDirString);
       }
       for (File xmlFile : xmlFiles) {
         try {
           getLogger().info("Reading portfolio definitions from: " + xmlFile);
           PortfolioDefinitions def = getDefinitions(new FileInputStream(xmlFile));
           if (def != null) {
             portfolioDefinitions.getMeasures().getMeasure().addAll(def.getMeasures().getMeasure());
           }
         }
         catch (FileNotFoundException e) {
           getLogger().info("Error reading definitions from: " + xmlFile + " " + e);
         }
       }
     }
     else {
       getLogger().info(ProjectBrowserProperties.PORTFOLIO_DEFINITION_DIR + " not defined.");
     }
     return portfolioDefinitions;
   }
 
   /**
    * Returns a TelemetryDefinitions object constructed from defStream, or null if unsuccessful.
    * 
    * @param defStream The input stream containing a TelemetryDefinitions object in XML format.
    * @return The TelemetryDefinitions object.
    */
   private PortfolioDefinitions getDefinitions(InputStream defStream) {
     // Read in the definitions file.
     try {
       JAXBContext jaxbContext = JAXBContext
           .newInstance(org.hackystat.projectbrowser.page.projectportfolio.jaxb.ObjectFactory.class);
       Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
       // add schema checking
       SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
 
       InputStream schemaStream = ProjectPortfolioDataModel.class
           .getResourceAsStream("portfolioDefinitions.xsd");
       StreamSource schemaSources = new StreamSource(schemaStream);
       Schema schema = schemaFactory.newSchema(schemaSources);
       unmarshaller.setSchema(schema);
       return (PortfolioDefinitions) unmarshaller.unmarshal(defStream);
     }
     catch (JAXBException e1) {
       Logger logger = getLogger();
       logger.severe("Error occurs when parsing portfolio definition xml file with jaxb > "
           + e1.getErrorCode() + ":" + e1.getMessage());
       e1.printStackTrace();
     }
     catch (SAXException e) {
       Logger logger = getLogger();
       logger.warning("Error occurs when loading schema file: " + defStream.toString() + " > "
           + e.getMessage());
     }
     return null;
   }
 
   /**
    * Load data from Hackystat service.
    */
   public void loadData() {
 
     this.inProcess = true;
     this.complete = false;
     this.processingMessage = "Retrieving data from Hackystat Telemetry service.\n";
     try {
       TelemetryClient telemetryClient = new TelemetryClient(telemetryHost, userEmail, password);
       // prepare start and end time.
       XMLGregorianCalendar startTime = getStartTimestamp();
       XMLGregorianCalendar endTime = getEndTimestamp();
       for (int i = 0; i < this.selectedProjects.size() && inProcess; i++) {
         // prepare project's information
         Project project = this.selectedProjects.get(i);
         String owner = project.getOwner();
         String projectName = project.getName();
 
         this.processingMessage += "Retrieve data for project " + projectName + " (" + (i + 1)
             + " of " + this.selectedProjects.size() + ").\n";
 
         // get charts of this project
         List<MiniBarChart> charts = new ArrayList<MiniBarChart>();
 
         List<PortfolioMeasureConfiguration> enableMeasures = getEnabledMeasures();
 
         for (int j = 0; j < enableMeasures.size(); ++j) {
           PortfolioMeasureConfiguration measure = enableMeasures.get(j);
 
           this.processingMessage += "---> Retrieve " + measure.getName() + "<"
               + measure.getParamtersString() + ">" + " (" + (i + 1) + " .. " + (j + 1) + " of "
               + enableMeasures.size() + ").\n";
           // get data from hackystat
           if (!ProjectUtils.isValidStartTime(project, startTime)) {
             startTime = project.getStartTime();
           }
           String s = "/";
           getLogger().finest(
               "retriving telemetry: " + measure.getName() + s + owner + s + projectName + s
                   + granularity + s + startTime + s + endTime + s + measure.getParamtersString());
           TelemetryChartData chartData = telemetryClient.getChart(measure.getName(), owner,
               projectName, granularity, startTime, endTime, measure.getParamtersString());
           // Log warning when portfolio definition refers to multi-stream telemetry chart.
           if (chartData.getTelemetryStream().size() > 1) {
             String merge = measure.getMerge();
             if ((merge == null || merge.length() <= 0) && i == 0) {
               Logger logger = getLogger();
               logger.warning("Telemetry chart:" + measure.getName() + 
                   " contains multiple streams, but there is no merge method found in portfolio " +
                   "defintion. Please check your portfolio and telemetry definitions");
             }
           }
           MiniBarChart chart = new MiniBarChart(chartData.getTelemetryStream(), measure);
           chart.setTelemetryPageParameters(this.getTelemetryPageParameters(measure, project));
           //chart.setDpdPageParameters(this.getDpdPageParameters(
           //measure, project, chart.getLastValidIndex()));
           charts.add(chart);
         }
         this.measuresCharts.put(project, charts);
       }
     }
     catch (TelemetryClientException e) {
       String errorMessage = "Errors when retrieving telemetry data: " + e.getMessage() + ". "
           + "Please try again.";
       this.processingMessage += errorMessage + "\n";
 
       this.complete = false;
       this.inProcess = false;
       return;
     }
     this.complete = inProcess;
     if (this.complete) {
       this.processingMessage += "All done.\n";
     }
     this.inProcess = false;
   }
 
   /**
    * Check the parameter in the given measure configuration. Set the parameter to default if it is
    * incorrect.
    * 
    * @param measure the given MeasureConfiguration.
    */
   private void checkMeasureParameters(PortfolioMeasureConfiguration measure) {
     List<ParameterDefinition> paramDefList = telemetrySession.getParameterList(measure.getName());
     if (measure.getParameters().size() != paramDefList.size()) {
       measure.getParameters().clear();
       for (ParameterDefinition paramDef : paramDefList) {
         measure.getParameters().add(new Model(paramDef.getType().getDefault()));
       }
     }
   }
 
   /**
    * Cancel the data loading process.
    */
   public void cancelDataUpdate() {
     this.processingMessage += "Process Cancelled.\n";
     this.inProcess = false;
   }
 
   /**
    * Return a PageParameters instance that include necessary information for telemetry.
    * 
    * @param measure the telemetry analysis
    * @param project the project
    * @return the PagaParameters object
    */
   private PageParameters getTelemetryPageParameters(PortfolioMeasureConfiguration measure, 
       Project project) {
     PageParameters parameters = new PageParameters();
 
     parameters.put(TelemetrySession.TELEMETRY_KEY, measure.getName());
     parameters.put(TelemetrySession.START_DATE_KEY, getStartTimestamp().toString());
     parameters.put(TelemetrySession.END_DATE_KEY, getEndTimestamp().toString());
     parameters.put(TelemetrySession.SELECTED_PROJECTS_KEY, 
         ProjectBrowserSession.convertProjectListToString(Arrays.asList(new Project[]{project})));
     parameters.put(TelemetrySession.GRANULARITY_KEY, this.granularity);
     parameters.put(TelemetrySession.TELEMETRY_PARAMERTERS_KEY, measure.getParamtersString());
 
     return parameters;
   }
 
   /**
    * Return a PageParameters instance that include necessary information for telemetry.
    * 
    * @param measure the telemetry analysis
    * @param project the project
    * @param indexOfLastValue the index of last valid value.
    * @return the PagaParameters object, null if indexOfLastValue is negative, 
    * which means last value is not available.
    */
   /*
   private PageParameters getDpdPageParameters(PortfolioMeasureConfiguration measure, 
       Project project, int indexOfLastValue) {
     if (indexOfLastValue < 0) {
       return null;
     }
     PageParameters parameters = new PageParameters();
     
 
     parameters.put(DailyProjectDataSession.ANALYSIS_KEY, measure.getName());
     parameters.put(DailyProjectDataSession.DATE_KEY, );
     parameters.put(DailyProjectDataSession.SELECTED_PROJECTS_KEY, 
         ProjectBrowserSession.convertProjectListToString(Arrays.asList(new Project[]{project})));
 
     return parameters;
   }
   */
   
   /**
    * Return the end time stamp for analysis. If includeCurrentWeek, it will return the time stamp of
    * yesterday. If !includeCurrentWeek, it will return the time stamp of last Saturday.
    * 
    * @return the XMLGregorianCalendar instance.
    */
   private XMLGregorianCalendar getEndTimestamp() {
     /*
      * if (!this.includeCurrentWeek) { GregorianCalendar date = new GregorianCalendar();
      * date.setTime(ProjectBrowserBasePage.getDateBefore(1));
      * date.setFirstDayOfWeek(Calendar.MONDAY); int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
      * XMLGregorianCalendar endTime = Tstamp.makeTimestamp(date.getTimeInMillis()); endTime =
      * Tstamp.incrementDays(endTime, - dayOfWeek); return endTime; } return
      * Tstamp.makeTimestamp(ProjectBrowserBasePage.getDateBefore(1).getTime());
      */
     return Tstamp.makeTimestamp(this.endDate);
   }
 
   /**
    * Return the start time stamp for analysis.
    * 
    * @return the XMLGregorianCalendar instance.
    */
   private XMLGregorianCalendar getStartTimestamp() {
     /*
      * int days; if ("Month".equals(this.telemetryGranularity)) { days = this.timePhrase * 30; }
      * else if ("Week".equals(this.telemetryGranularity)) { days = this.timePhrase * 7; } else {
      * days = this.timePhrase; } return
      * Tstamp.makeTimestamp(ProjectBrowserBasePage.getDateBefore(days).getTime());
      */
     return Tstamp.makeTimestamp(this.startDate);
   }
 
   /**
    * @return the inProcess
    */
   public boolean isInProcess() {
     return inProcess;
   }
 
   /**
    * @return the complete
    */
   public boolean isComplete() {
     return complete;
   }
 
   /**
    * @return the processingMessage
    */
   public String getProcessingMessage() {
     return processingMessage;
   }
 
   /**
    * @return the analysesCharts
    */
   public Map<Project, List<MiniBarChart>> getMeasuresCharts() {
     return measuresCharts;
   }
 
   /**
    * @return the selectedProjects
    */
   public List<Project> getSelectedProjects() {
     return selectedProjects;
   }
 
   /**
    * Returns true if this model does not contain any data.
    * 
    * @return True if no data.
    */
   public boolean isEmpty() {
     return this.selectedProjects.isEmpty();
   }
 
   /**
    * Return the default parameters of the given measure.
    * 
    * @param measure the given measure
    * @return the parameters in a String
    */
   /*
    * public String getDefaultParametersString(String measure) { List<ParameterDefinition>
    * paramDefList = getParameterDefinitions(measure); StringBuffer param = new StringBuffer(); for
    * (int i = 0; i < paramDefList.size(); ++i) { ParameterDefinition paramDef = paramDefList.get(i);
    * param.append(paramDef.getType().getDefault()); if (i < paramDefList.size() - 1) {
    * param.append(','); } } return param.toString(); }
    */
 
   /**
    * @return the measures
    */
   public List<PortfolioMeasureConfiguration> getMeasures() {
     return measures;
   }
 
   /**
    * @return the enabled measures
    */
   public final List<PortfolioMeasureConfiguration> getEnabledMeasures() {
     List<PortfolioMeasureConfiguration> enableMeasures = 
       new ArrayList<PortfolioMeasureConfiguration>();
     for (PortfolioMeasureConfiguration measure : measures) {
       if (measure.isEnabled()) {
         enableMeasures.add(measure);
       }
     }
     return enableMeasures;
   }
 
   /**
    * Return the names of enabled measures. If alias available for the measure, alias will be
    * returned. Otherwise, name in measure's definition will be returned.
    * 
    * @return the names of the enabled measures.
    */
   public List<String> getEnabledMeasuresName() {
     List<String> names = new ArrayList<String>();
     for (PortfolioMeasureConfiguration measure : measures) {
       if (measure.isEnabled()) {
         names.add(measure.getDisplayName());
       }
     }
     return names;
   }
 
   /**
    * @param backgroundColor the backgroundColor to set
    */
   /*
   public void setBackgroundColor(String backgroundColor) {
     this.backgroundColor = backgroundColor;
   }
   */
 
   /**
    * @return the backgroundColor
    */
   public String getBackgroundColor() {
     return backgroundColor;
   }
   
   /**
    * @return the goodColor
    */
   public String getGoodColor() {
     return goodColor;
   }
 
   /**
    * @return the sosoColor
    */
   public String getAverageColor() {
     return averageColor;
   }
 
   /**
    * @return the badColor
    */
   public String getPoorColor() {
     return poorColor;
   }
 
   /**
    * @return the fontColor
    */
   public String getFontColor() {
     return fontColor;
   }
 
   /**
    * @return the naColor
    */
   public String getNAColor() {
     return naColor;
   }
 
   /**
    * @return the userConfiguartionCache
    */
   private UriCache getUserConfiguartionCache() {
     return new UriCache(userEmail, "portfolio", maxLife, capacity);
   }
 
   /**
    * @return the logger that associated to this web application.
    */
   public static Logger getLogger() {
     return HackystatLogger.getLogger("org.hackystat.projectbrowser", "projectbrowser");
   }
 }
