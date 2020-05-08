 package com.brianreber.gitstats;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 /**
  * A generic page for all stats that use a LineChart
  * 
  * @author breber
  */
 public abstract class LineChartPage extends HtmlPage {
 
 	/**
 	 * Creates a new LineChartPage with the given projectTitle, fileName and pageTitle
 	 * 
 	 * @param projectTitle the title of the project
 	 * @param fileName the file to write to
 	 * @param pageTitle the title of the HTML Page
 	 */
 	public LineChartPage(String projectTitle, String fileName, String pageTitle) {
 		super(projectTitle, fileName, pageTitle);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.brianreber.gitstats.HtmlPage#generateBody(java.util.List)
 	 */
 	@Override
 	protected String generateBody(List<Commit> commits) throws JSONException {
 		StringBuilder sb = new StringBuilder();
 		JSONArray data = parseData(commits);
 
 		sb.append("google.load('visualization', '1.0', { 'packages':['corechart'] });");
 		sb.append("google.setOnLoadCallback(drawChart);");
 
 		sb.append("function drawChart() {");
 
 		sb.append("var data = new google.visualization.arrayToDataTable(");
		sb.append(data.toString());
 		sb.append(");");
 
 		sb.append("var chart = new google.visualization.LineChart(document.getElementById('chart_div'));");
 		sb.append("chart.draw(data);");
 
 		sb.append("};");
 
 		return sb.toString();
 	}
 
 	/**
 	 * Go through the list of Commits and create a JSONArray to be used in the
 	 * data visualization.
 	 * 
 	 * @param commits the commits
 	 * @return a JSONArray that the caller can use in the visualization
 	 * @throws JSONException
 	 */
 	protected abstract JSONArray parseData(List<Commit> commits) throws JSONException;
 }
