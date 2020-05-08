     
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.report.plugin.util.GraphHelper;
 import com.redhat.engineering.jenkins.testparser.results.Filter;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixProject;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.util.ChartUtil;
 import hudson.util.DataSetBuilder;
 import hudson.util.RunList;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import javax.servlet.ServletException;
 import org.jfree.chart.JFreeChart;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  *
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class ReportPluginProjectAction implements Action{
     private final MatrixProject project;
     private boolean refresh;
     private Filter filter;
     private String combinationFilter;
     // each combination is either checked or unchecked by user or has default value
     private Map<String, Boolean> checkedCombinations;
     
     /**
     * Used to figure out if we need to regenerate the graphs or not.
     * Only used in newGraphNotNeeded() method. Key is the request URI and value
     * is the number of builds for the project.
     */
     private transient Map<String, Integer> requestMap = new HashMap<String, Integer>();
     
     // indicates how should builds be filtered
     private BuildFilteringMethod buildFilteringMethod;
     
     // indicates how should configurations be filtered
     private ConfigurationFilteringMethod confFilteringMethod;
     
     // stores value when buildFilteringMethod is RECENT
     private int numLastBuilds;
     
     // stores builds to be used
     private long firstSelBuildTimestamp;
     private long lastSelBuildTimestamp;
     private RunList<MatrixBuild> builds;
     
     enum BuildFilteringMethod {
 	ALL, RECENT, INTERVAL
     }
     
     enum ConfigurationFilteringMethod{
 	MATRIX, COMBINATIONFILTER
     }
     
     
     public ReportPluginProjectAction(MatrixProject project){
 	this.project = project;
 	this.checkedCombinations = new HashMap<String, Boolean>();
 	refresh = false;
 	/*
 	 * Add all builds by default
 	 */
 	builds = new RunList<MatrixBuild>();
 	buildFilteringMethod = BuildFilteringMethod.ALL;
 	confFilteringMethod = ConfigurationFilteringMethod.MATRIX;
 	combinationFilter = "";
 	numLastBuilds = project.getLastBuild()!= null ? project.getLastBuild().number : 0;
 	String uuid = "RP_" + this.project.getName() + "_" + System.currentTimeMillis();
 	filter = new Filter(uuid, this.project.getAxes());
 	firstSelBuildTimestamp = project.getFirstBuild() != null ? project.getFirstBuild().getTimeInMillis() : 0;
     }
     
     public String getIconFileName() {
 	return Definitions.__ICON_FILE_NAME;
     }
 
     public String getDisplayName() {
 	return Definitions.__DISPLAY_NAME;
     }
 
     public String getUrlName() {
 	return Definitions.__URL_NAME;
     }
     
     public String getPrefix() {
 	return Definitions.__PREFIX;
     }
     
     
     public AbstractProject<?, ?> getProject() {	
 	return project;
     }
     
     
     /**
      * Returns false when combination was unchecked by user.
      * 
      * @param combination
      * @return 
      */
     public boolean isCombinationChecked(Combination combination){	
 	return filter.getConfiguration(combination);	
     }
     
     
     public boolean combinationExists( AbstractProject ap, Combination c){
 	if(ap instanceof MatrixProject){
 	    MatrixProject mp = (MatrixProject) ap;
 	    MatrixConfiguration mc = mp.getItem(c);
 	    
 	    /* Verify matrix configuration */
 	    if( mc != null || mc.isActiveConfiguration()) {
 		return true;
 	    }
 	}	
 	return false;	
     }
     
     
     /**
     * Returns <code>true</code> if there is a graph to plot.
     *
     * @return Value for property 'graphAvailable'.
     */
     public boolean isGraphActive() {
 	AbstractBuild<?, ?> build = getProject().getLastBuild();
 	// in order to have a graph, we must have at least two points.
 	int numPoints = 0;
 	while (numPoints < 2) {
 	    if (build == null) {
 		return false;
 	    }
 	    if (build.getAction(ReportPluginBuildAction.class) != null) {
 		numPoints++;
 	    }
 	    build = build.getPreviousBuild();
 	}
 	return true;
     }
 
 
 
     /**
     * If number of builds hasn't changed and if checkIfModified() returns true,
     * no need to regenerate the graph. Browser should reuse it's cached image
     *
     * @param req
     * @param rsp
     * @return true, if new image does NOT need to be generated, false otherwise
     */
     private boolean newGraphNotNeeded(final StaplerRequest req,
 	    StaplerResponse rsp) {
 	
 	/**
 	 * If refresh is scheduled, then rebuild graph
 	 */
 	if(refresh) {
 	    refresh = false;
 	    return false;
 	}
 	
 	Calendar t = getProject().getLastCompletedBuild().getTimestamp();
 	Integer prevNumBuilds = requestMap.get(req.getRequestURI());
 	int numBuilds = getProject().getBuilds().size();
 
 	//change null to 0
 	prevNumBuilds = prevNumBuilds == null ? 0 : prevNumBuilds;
 	if (prevNumBuilds != numBuilds) {
 	    requestMap.put(req.getRequestURI(), numBuilds);
 	}
 
 	if (requestMap.size() > 10) {
 	    //keep map size in check
 	    requestMap.clear();
 	}
 
 	if (prevNumBuilds == numBuilds && req.checkIfModified(t, rsp)) {
 	    /*
 	    * checkIfModified() is after '&&' because we want it evaluated only
 	    * if number of builds is different
 	    */
 	    return true;
 	}
 	return false;
     }
 
     /**
     * Generates the graph that shows test pass/fail ratio
     * @param req -
     * @param rsp -
     * @throws IOException -
     */
     public void doGraph(final StaplerRequest req,
 			StaplerResponse rsp) throws IOException {
 	if (newGraphNotNeeded(req, rsp)) {
 	    return;
 	}
 
 	final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
 		new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
 
 	populateDataSetBuilder(dataSetBuilder, filter);
 	new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
 	    protected JFreeChart createGraph() {
 		return GraphHelper.createChart(req, dataSetBuilder.build());
 	    }
 	}.doPng(req,rsp);
     }
 
     /**
      * Fill dataset with data. Optionally Filter may be passed to this method, 
      * which will filter configurations (meaningful if we want results aggregated
      * only from some subset of all matrix runs).
      * 
      * @param dataset	
      * @param filter	Optional, can be null.
      */
     protected void populateDataSetBuilder(DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataset,
 	    Filter filter) {
 
 	//updateFilteredBuilds();
 	for (AbstractBuild<?, ?> build : builds) 
 	{
 	    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
 	    ReportPluginBuildAction action = build.getAction(ReportPluginBuildAction.class);
 	    
 	    if (action != null) {
 		
 		if(filter != null){
 		    action.addFilter(filter);
 		}
 		
 		int a = action.getPassedTestCount();
 		dataset.add(a, "Passed", label);
 		a = action.getFailedTestCount();
 		dataset.add(a, "Failed", label);
 		a = action.getSkippedTestCount();
 		dataset.add(a , "Skipped", label);
 		
 		if(filter != null){
 		    action.removeFilter();
 		}
 		
 	    } else {
 		//even if report plugin wasn't run with this build,
 		//we should add this build to the graph
 		dataset.add(0, "Passed", label);
 		dataset.add(0, "Failed", label);
 		dataset.add(0, "Skipped", label);
 	    }
 	}
     }
 
     /**
     * Getter for property 'graphWidth'.
     *
     * @return Value for property 'graphWidth'.
     */
     public int getGraphWidth() {
 	return 500;
     }
 
     /**
 	* Getter for property 'graphHeight'.
 	*
 	* @return Value for property 'graphHeight'.
 	*/
     public int getGraphHeight() {
 	return 200;
     }
     
     public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
 	
 	// refresh page afterwards
 	refresh = true;
 	
 	/*
 	 * Determine how builds are filtered (all, last N builds, interval)
 	 */
 	BuildFilteringMethod bf= BuildFilteringMethod.valueOf(req.getParameter("buildsFilter"));
 	
         if(bf == BuildFilteringMethod.ALL){
             numLastBuilds = project.getBuilds().size();
         }
         
         int n = numLastBuilds;
         
 	if(bf == BuildFilteringMethod.RECENT){
             try{
                 n = Integer.parseInt(req.getParameter("numLastBuilds"));
             }catch (NumberFormatException e){
                 // invalid input, so do nothing
             }
	    int numProjBuilds = project.getBuilds().size();
             
             /**
              * Make sure that submitted value is not higher than possible
              */
	    n = n > numProjBuilds ? numProjBuilds : n; 
             /**
              * If number of recent builds has changed, then update builds
              */
             if(n != numLastBuilds){
                numLastBuilds = n > 1 ? n : 2;
                 updateFilteredBuilds();
             }
 	} 
 	
 	if(bf == BuildFilteringMethod.INTERVAL){
 	    
 	    /*
 	     * Get timestamps of first and last build
 	     */
             try{
                 firstSelBuildTimestamp = Long.parseLong(req.getParameter("firstBuild"));
                 lastSelBuildTimestamp = Long.parseLong(req.getParameter("lastBuild"));
             } catch (NumberFormatException e){
                 // invalid input, so do nothing
             }
 	    /*
 	     * Swap when user entered invalid range
 	     */
 	    if(firstSelBuildTimestamp > lastSelBuildTimestamp){
 		long tmp = firstSelBuildTimestamp;
 		firstSelBuildTimestamp= lastSelBuildTimestamp;
 		lastSelBuildTimestamp = tmp;
 	    }
             updateFilteredBuilds();
 	    
 	}
 	
 	/*
 	 * If method of filtering of builds has changed
 	 */
 	if(!bf.equals(buildFilteringMethod) ){
 	    buildFilteringMethod = bf;
 	    updateFilteredBuilds();
 	}
 	    
 	/*
 	 * Determine how configurations are filtered 
 	 */
 	confFilteringMethod = ConfigurationFilteringMethod.valueOf(req.getParameter("confFilter"));
 	if(confFilteringMethod == ConfigurationFilteringMethod.COMBINATIONFILTER){
 	    
 	    combinationFilter = req.getParameter("combinationFilter");
 	    filter.addCombinationFilter(combinationFilter);
 	    
 	} else {
 	    
 	    // reset all checkboxes
 	    filter.removeCombinationFilter();
 	    
 	    // parse submitted configuration matrix
 	    String input;
 	    for(MatrixConfiguration c : project.getActiveConfigurations()){
 		Combination cb = c.getCombination();
 		input = req.getParameter(cb.toString());
 		if(input != null){
 		    filter.setConfiguration(cb, true);
 		}
 	    }
 	}   
 	   
 	rsp.sendRedirect("../" + Definitions.__URL_NAME);
     }
 
     /*
      * Updates private list <code>builds</code> used for populating dataSetBuilder 
      * according to buildFilteringMethod. 
      */
     public void updateFilteredBuilds(){
 	builds.clear();
 	switch(buildFilteringMethod){
 	    case ALL:
 		for (MatrixBuild build = project.getLastBuild();
 			build != null; build = build.getPreviousBuild()){
 		    builds.add(build);
 		}
 		break;
 	    case RECENT:
 		MatrixBuild build = project.getLastBuild();
 		for (int i=0; i < numLastBuilds; i++ ){
                     if(build == null) break;
 		    builds.add(build);
 		    build = build.getPreviousBuild();  
 		}
                 
 		break;
 	    case INTERVAL:
 		builds = (RunList<MatrixBuild>) project.getBuilds().
 			byTimestamp(firstSelBuildTimestamp -1, lastSelBuildTimestamp +1);
                 numLastBuilds = builds.size();
 		break;
 	}
     }
     
     /**
      * Returns number of recent builds to be included, as it was configured.
      * 
      * @return 
      */
     public int getBuildsRecentNumber(){
 	return numLastBuilds;
     }
     
     /**
      * Returns true if build filtering method is set to ALL.
      * 
      * @return 
      */
     public boolean getBuildsAllChecked(){
 	if(buildFilteringMethod == BuildFilteringMethod.ALL) {
 	    return true;
 	}
 	return false;
     }
     
     /**
      * Returns true if build filtering method is set to RECENT.
      * 
      * @return 
      */
     public boolean getBuildsRecentChecked(){
 	if(buildFilteringMethod == BuildFilteringMethod.RECENT) {
 	    return true;
 	}
 	return false;
     }
     
     /**
      * Returns true if build filtering method is set to INTERVAL
      * 
      * @return 
      */
     public boolean getBuildsIntervalChecked(){
 	if(buildFilteringMethod == BuildFilteringMethod.INTERVAL) {
 	    return true;
 	}
 	return false;
     }
     
     /**
      * Returns true if configurations filtering method is set to MATRIX
      * (Configurations were filtered with matrix last time)
      * 
      * @return 
      */
     public boolean getMatrixChecked(){
 	if(confFilteringMethod == ConfigurationFilteringMethod.MATRIX){
 	    return true;
 	}
 	return false;
     }
     
     /**
      * Returns true if configurations filtering method is set to COMBINATIONFILTER
      * (Combination Filter was submitted last time and now should be checked)
      * 
      * @return 
      */
     public boolean getCombinationFilterChecked(){
 	if(confFilteringMethod == ConfigurationFilteringMethod.COMBINATIONFILTER){
 	    return true;
 	}
 	return false;
     }
     
     /**
      * Used for drop-down list in selection of builds to be filtered 
      * 
      * @return list of all builds for this project
      */
     public RunList<?> getAllBuilds(){
 	return project.getBuilds();
     }
     
     /**
      * Returns timestamp of first selected build - used in dropdown menu for
      * selecting interval
      * 
      * @return  Time in milliseconds
      */
     public long getFirstSelBuildTimestamp() {
         return builds.getFirstBuild() != null ? builds.getFirstBuild().getTimeInMillis() : 0;
     }
     
     /**
      * Returns timestamp of last selected build - used in dropdown menu for 
      * selecting interval
      * 
      * @return  Time in milliseconds
      */
     public long getLastSelBuildTimestamp() {
         return builds.getLastBuild() != null ? builds.getLastBuild().getTimeInMillis() : 0;
     }
     
     /**
      * Returns combinations filter, that was submitted by user last time
      * 
      * @return  
      */
     public String getCombinationFilter(){
 	return combinationFilter;
     }
 
     /**
      * Returns axes and theirs values for this project
      * 
      * @return  String in format [axis={value, value,...}, axis={value, ..}, ..]
      */
     public String getAxes(){
 	return project.getAxes().toString();
     }
 }
