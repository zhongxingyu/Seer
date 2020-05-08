 
 package com.redhat.engineering.jenkins.report.plugin;
 
 import com.redhat.engineering.jenkins.report.plugin.util.GraphHelper;
 import com.redhat.engineering.jenkins.testparser.results.Filter;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixConfiguration;
 import hudson.matrix.MatrixProject;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.util.ChartUtil;
 import hudson.util.DataSetBuilder;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import javax.servlet.ServletException;
 import net.sf.json.JSONException;
 import net.sf.json.JSONObject;
 import org.jfree.chart.JFreeChart;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 /**
  *
  * @author Jan Rusnacko (jrusnack at redhat.com)
  */
 public class ReportPluginProjectAction implements Action{
     private int numLastBuilds;
     private final AbstractProject<?, ?> project;
     private boolean refresh;
     private Filter filter;
     // each combination is either checked or unchecked by user or has default value
     private Map<String, Boolean> checkedCombinations;
     
     /**
 	* Used to figure out if we need to regenerate the graphs or not.
 	* Only used in newGraphNotNeeded() method. Key is the request URI and value
 	* is the number of builds for the project.
 	*/
     private transient Map<String, Integer> requestMap = new HashMap<String, Integer>();
     
     public ReportPluginProjectAction(AbstractProject<?, ?> project){
 	this.project = project;
 	this.checkedCombinations = new HashMap<String, Boolean>();
 	refresh = false;
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
      * Specify whether combination should be checked or not 
      */
     public void setCombinationChecked(Combination combination, boolean val){
 	
 	// FIXME: fix unchenking
 	checkedCombinations.put(combination.toString(), val);
     }
     
     /**
      * Returns true when combination was checked by user
      */
     public boolean isCombinationChecked(Combination combination){
 	if(this.checkedCombinations.containsKey(combination.toString())){
 	    return this.checkedCombinations.get(combination.toString());		    
 	}
 	return false;
     }
     
     // TODO: optimize
     public boolean combinationExists( AbstractProject ap, Combination c){
 	if(ap instanceof MatrixProject){
 	    MatrixProject mp = (MatrixProject) ap;
 	    MatrixConfiguration mc = mp.getItem(c);
 	    
 	    /* Verify matrix configuration */
 	    if( mc == null || !mc.isActiveConfiguration()) {
 		return false;
 	    }
 	    
 	    return true;
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
 
     public void doGraphMap(final StaplerRequest req,
 	    StaplerResponse rsp) throws IOException {
 	if (newGraphNotNeeded(req, rsp)) {
 	    return;
 	}
 
 	final DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel> dataSetBuilder =
 	new DataSetBuilder<String, ChartUtil.NumberOnlyBuildLabel>();
 
 	//TODO: optimize by using cache
 	populateDataSetBuilder(dataSetBuilder,null);
 	new hudson.util.Graph(-1, getGraphWidth(), getGraphHeight()) {
 	    protected JFreeChart createGraph() {
 	    return GraphHelper.createChart(req, dataSetBuilder.build());
 	    }
 	}.doMap(req, rsp);
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
 
 	for (AbstractBuild<?, ?> build = getProject().getLastBuild();
 		build != null; build = build.getPreviousBuild()) 
 	{
 	    ChartUtil.NumberOnlyBuildLabel label = new ChartUtil.NumberOnlyBuildLabel(build);
 	    ReportPluginBuildAction action = build.getAction(ReportPluginBuildAction.class);
 	    
 	    if (action != null) {
 		// TODO: optimize
 		if(filter != null){
 		    action.addFilter(filter);
 		}
 		
 		int a = action.getPassedTestCount();
 		dataset.add(a, "Passed", label);
 		a = action.getFailedTestCount();
 		dataset.add(a, "Failed", label);
 		a = action.getSkippedTestCount();
 		dataset.add(a , "Skipped", label);
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
     
     //FIXME: implement
     public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
             IOException, InterruptedException {
 	AbstractProject project = req.findAncestorObject(AbstractProject.class);
 	
 	JSONObject formData = req.getSubmittedForm();
 	
 	String uuid = "RP_" + project.getName() + "_" + System.currentTimeMillis();
	
 	filter = new Filter(uuid);
 	refresh = true;
         Map map = req.getParameterMap();
 	Set<String> keys = map.keySet();
 	for(String key : keys){
 	    /* Check the fields of the form */
             if (key.startsWith(Definitions.__PREFIX)) {
                 String[] vs = key.split(Definitions.__DELIMITER, 2);
 		try {
                     if (vs.length > 1) {
 			Combination c = Combination.fromString(vs[1]);
 			setCombinationChecked(c, true);
                     	filter.addConfiguration(c, true);
                     }
 
                 } catch (JSONException e) {
                     /* No-op, not the field we were looking for. */
                 }
 	    }
 	}
 	
 	rsp.sendRedirect("../" + Definitions.__URL_NAME);
     }
 
     public void setNumLastBuilds(int i){
 	this.numLastBuilds = i;
     }
     
     public int getNumLastBuilds(){
 	return this.numLastBuilds;
     }
     
 }
