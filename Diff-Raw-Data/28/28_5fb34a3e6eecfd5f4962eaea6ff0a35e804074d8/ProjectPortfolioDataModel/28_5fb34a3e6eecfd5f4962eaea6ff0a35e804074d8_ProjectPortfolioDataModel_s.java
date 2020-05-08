 package org.hackystat.projectbrowser.page.projectportfolio.detailspanel;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.apache.wicket.PageParameters;
 import org.hackystat.projectbrowser.page.ProjectBrowserBasePage;
 import org.hackystat.projectbrowser.page.loadingprocesspanel.Processable;
 import org.hackystat.projectbrowser.page.telemetry.TelemetrySession;
 import org.hackystat.sensorbase.resource.projects.ProjectUtils;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.telemetry.service.client.TelemetryClient;
 import org.hackystat.telemetry.service.client.TelemetryClientException;
 import org.hackystat.telemetry.service.resource.chart.jaxb.ParameterDefinition;
 import org.hackystat.telemetry.service.resource.chart.jaxb.TelemetryChartData;
 import org.hackystat.utilities.tstamp.Tstamp;
 
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
   /** message to display when data loading is in process.*/
   private String processingMessage = "";
 
   /** host of the telemetry host. */
   private String telemetryHost;
   /** email of the user. */
   private String email;
   /** password of the user. */
   private String password;
   
   /** the granularity this data model focus. */
   private String telemetryGranularity = "Week";
   /** The available granularities. */
   private final String[] granularities = {"Day", "Week", "Month"};
   
   /** 
    * the time phrase this data model focus. 
    * In scale of telemetryGranularity, from current to the past. 
    * */
   private int timePhrase = 5;
   
   /** The projects this user has selected. */
   private List<Project> selectedProjects = new ArrayList<Project>();
   /** The charts in this model. */
   private Map<Project, List<MiniBarChart>> measuresCharts = 
         new HashMap<Project, List<MiniBarChart>>();
   /** The thresholds. */
   private final List<MeasureConfiguration> measures = new ArrayList<MeasureConfiguration>();
 
   /** The background color for table cells. */
   private String backgroundColor = "000000";
   /** The font color for table cells. */
   private String fontColor = "ffffff";
   /** The color for good state. */
   private String goodColor = "00ff00";
   /** The color for soso state. */
   private String sosoColor = "ffff00";
   /** The color for bad state. */
   private String badColor = "ff0000";
   
   /**
    * Initialize the measure configurations
    */
   public ProjectPortfolioDataModel() {
     measures.add(new MeasureConfiguration("Coverage", true, 40, 90, true, this));
     measures.add(new MeasureConfiguration("CyclomaticComplexity", true, 10, 20, false, this));
     measures.add(new MeasureConfiguration("Coupling", true, 10, 20, false, this));
    measures.add(new MeasureConfiguration("Churn", true, 35, 85, false, this));
     measures.add(new MeasureConfiguration("DevTime", false, 0, 0, true, this));
     measures.add(new MeasureConfiguration("Build", false, 0, 0, true, this));
     measures.add(new MeasureConfiguration("UnitTest", false, 0, 0, true, this));
     measures.add(new MeasureConfiguration("Commit", false, 0, 0, true, this));
    measures.add(new MeasureConfiguration("CodeIssue", false, 0, 0, true, this));
     measures.add(new MeasureConfiguration("FileMetric", false, 0, 0, true, this));
   }
   
   /**
    * @param startDate the start date.
    * @param endDate the end date.
    * @param selectedProjects the selected projects.
    * @param telemetryHost the telemetry host
    * @param email the user's email
    * @param password the user's passowrd
    */
   public void setModel(long startDate, long endDate, List<Project> selectedProjects, 
       String telemetryHost, String email, String password) {
     this.telemetryHost = telemetryHost;
     this.email = email;
     this.password = password;
     this.selectedProjects = selectedProjects;
   }
 
   /**
    * Load data from Hackystat service.
    */
   public void loadData() {
 
     this.inProcess = true;
     this.complete = false;
     this.processingMessage = "Retrieving data from Hackystat Telemetry service.\n";
     try {
       measuresCharts.clear();
       TelemetryClient telemetryClient = new TelemetryClient(telemetryHost, email, password);
       for (int i = 0; i < this.selectedProjects.size() && inProcess; i++) {
         //prepare project's information
         Project project = this.selectedProjects.get(i);
         String owner = project.getOwner();
         String projectName = project.getName();
 
         this.processingMessage += "Retrieve data for project " + projectName + 
         " (" + (i + 1) + " of " + this.selectedProjects.size() + ").\n";
         
         //get charts of this project
         List<MiniBarChart> charts = new ArrayList<MiniBarChart>();
         
         List<MeasureConfiguration> enableMeasures = getEnabledMeasures();
         
         for (int j = 0; j < enableMeasures.size(); ++j) {
           MeasureConfiguration measure = enableMeasures.get(j);
           this.processingMessage += "---> Retrieve " + measure.getName() + 
               " (" + (i + 1) + " .. " + (j + 1) + " of " + enableMeasures.size() + ").\n";
           
           //set default parameter if no parameter is assigned.
           if (measure.getParameters().isEmpty()) {
             List<String> params = new ArrayList<String>();
             List<ParameterDefinition> paramDefList = getParameterDefinitions(measure.getName());
             for (ParameterDefinition paramDef : paramDefList) {
               params.add(paramDef.getType().getDefault());
             }
             measure.setParameters(params);
           }
           //get data from hackystat
           XMLGregorianCalendar startTime = Tstamp.makeTimestamp(getStartDate());
           XMLGregorianCalendar endTime = Tstamp.makeTimestamp(getEndDate());
           if (!ProjectUtils.isValidStartTime(project, startTime)) {
             startTime = project.getStartTime();
           }
           TelemetryChartData chartData = telemetryClient.getChart(measure.getName(), 
               owner, projectName, telemetryGranularity, startTime, endTime, 
               measure.getParamtersString());
           
           MiniBarChart chart = new MiniBarChart(chartData.getTelemetryStream().get(0), measure);
           chart.setTelemetryPageParameters(getTelemetryPageParameters(measure, project));
           charts.add(chart);
         }
         this.measuresCharts.put(project, charts);
       }
     }
     catch (TelemetryClientException e) {
       String errorMessage = "Errors when retrieving telemetry data: " + e.getMessage() + ". " +
       		"Please try again.";
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
    * Cancel the data loading process.
    */
   public void cancelDataUpdate() {
     this.processingMessage += "Process Cancelled.\n";
     this.inProcess = false;
   }
   
   /**
    * Return a PageParameters instance that include necessary information for telemetry.
    * @param measure the telemetry analysis
    * @param project the project
    * @return the PagaParameters object
    */
   private PageParameters getTelemetryPageParameters(MeasureConfiguration measure, Project project) {
     PageParameters parameters = new PageParameters();
     
     parameters.put(TelemetrySession.TELEMETRY_KEY, measure.getName());
     parameters.put(
         TelemetrySession.START_DATE_KEY, Tstamp.makeTimestamp(getStartDate()).toString());
     parameters.put(TelemetrySession.END_DATE_KEY, Tstamp.makeTimestamp(getEndDate()).toString());
     parameters.put(TelemetrySession.SELECTED_PROJECTS_KEY, 
         project.getName() + TelemetrySession.PROJECT_NAME_OWNER_SEPARATR + project.getOwner());
     parameters.put(TelemetrySession.GRANULARITY_KEY, this.telemetryGranularity);
     parameters.put(TelemetrySession.TELEMETRY_PARAMERTERS_KEY, measure.getParamtersString());
     
     return parameters;
   }
 
   /**
    * Return the end date for analysis.
    * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT.
    */
   private long getEndDate() {
     return ProjectBrowserBasePage.getDateBefore(1).getTime();
   }
 
   /**
    * Return the end date for analysis.
    * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT.
    */
   private long getStartDate() {
     int daysInGranularity;
     if ("Month".equals(this.telemetryGranularity)) {
       daysInGranularity = 30;
     }
     else if ("Week".equals(this.telemetryGranularity)) {
       daysInGranularity = 7;
     }
     else {
       daysInGranularity = 1;
     }
     return ProjectBrowserBasePage.getDateBefore(this.timePhrase * daysInGranularity).getTime();
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
    * @param measure the given measure
    * @return the parameters in a String
    */
   public String getDefaultParametersString(String measure) {
     List<ParameterDefinition> paramDefList = getParameterDefinitions(measure);
       StringBuffer param = new StringBuffer();
       for (int i = 0; i < paramDefList.size(); ++i) {
         ParameterDefinition paramDef = paramDefList.get(i);          
         param.append(paramDef.getType().getDefault());
           if (i < paramDefList.size() - 1) {
             param.append(',');
           }
         }
       return param.toString();
   }
 
   /**
    * Return the parameter definitions of the given measure.
    * @param measure the given measure
    * @return the parameter definitions.
    */
   public List<ParameterDefinition> getParameterDefinitions(String measure) {
     TelemetryClient telemetryClient = new TelemetryClient(telemetryHost, email, password);
     try {
         return telemetryClient.getChartDefinition(measure).getParameterDefinition();
     }
     catch (TelemetryClientException e) {
       e.printStackTrace();
     }
     return new ArrayList<ParameterDefinition>();
     
   }
 
   /**
    * @return the measures
    */
   public List<MeasureConfiguration> getMeasures() {
     return measures;
   }
   
   /**
    * @return the enabled measures
    */
   public List<MeasureConfiguration> getEnabledMeasures() {
     List<MeasureConfiguration> enableMeasures = new ArrayList<MeasureConfiguration>();
     for (MeasureConfiguration measure : measures) {
       if (measure.isEnabled()) {
         enableMeasures.add(measure);
       }
     }
     return enableMeasures;
   }
   
   /**
    * @return the names of the enabled measures.
    */
   public List<String> getEnabledMeasuresName() {
     List<String> names = new ArrayList<String>();
     for (MeasureConfiguration measure : measures) {
       if (measure.isEnabled()) {
         names.add(measure.getName());
       }
     }
     return names;
   }
 
   /**
    * @param backgroundColor the backgroundColor to set
    */
   public void setBackgroundColor(String backgroundColor) {
     this.backgroundColor = backgroundColor;
   }
 
   /**
    * @return the backgroundColor
    */
   public String getBackgroundColor() {
     return backgroundColor;
   }
 
   /**
    * @param goodColor the goodColor to set
    */
   public void setGoodColor(String goodColor) {
     this.goodColor = goodColor;
   }
 
   /**
    * @return the goodColor
    */
   public String getGoodColor() {
     return goodColor;
   }
 
   /**
    * @param sosoColor the sosoColor to set
    */
   public void setSosoColor(String sosoColor) {
     this.sosoColor = sosoColor;
   }
 
   /**
    * @return the sosoColor
    */
   public String getSosoColor() {
     return sosoColor;
   }
 
   /**
    * @param badColor the badColor to set
    */
   public void setBadColor(String badColor) {
     this.badColor = badColor;
   }
 
   /**
    * @return the badColor
    */
   public String getBadColor() {
     return badColor;
   }
 
   /**
    * @param fontColor the fontColor to set
    */
   public void setFontColor(String fontColor) {
     this.fontColor = fontColor;
   }
 
   /**
    * @return the fontColor
    */
   public String getFontColor() {
     return fontColor;
   }
 
   /**
    * @param telemetryGranularity the telemetryGranularity to set
    */
   public void setTelemetryGranularity(String telemetryGranularity) {
     this.telemetryGranularity = telemetryGranularity;
   }
 
   /**
    * @return the telemetryGranularity
    */
   public String getTelemetryGranularity() {
     return telemetryGranularity;
   }
 
   /**
    * @return the granularities
    */
   public List<String> getGranularities() {
     return Arrays.asList(this.granularities);
   }
 }
