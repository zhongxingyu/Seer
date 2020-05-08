 package com.brianreber.gitstats;
 
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 
 public abstract class BarChartPage extends HtmlPage {
 
 	public BarChartPage(String projectTitle, String fileName, String pageTitle) {
 		super(projectTitle, fileName, pageTitle);
 	}
 
 	@Override
 	protected String generateBody(List<Commit> commits) throws JSONException {
 		StringBuilder sb = new StringBuilder();
 		JSONArray data = parseData(commits);
 
 		sb.append("google.load('visualization', '1.0', { 'packages':['corechart'] });");
 		sb.append("google.setOnLoadCallback(drawChart);");
 
 		sb.append("function drawChart() {");
 
 		sb.append("var data = new google.visualization.arrayToDataTable(");
		sb.append(data.toString(5));
 		sb.append(");");
 
 		sb.append("var chart = new google.visualization.ColumnChart(document.getElementById('chart_div'));");
 		sb.append("chart.draw(data);");
 
 		sb.append("};");
 
 		return sb.toString();
 	}
 
 	protected abstract JSONArray parseData(List<Commit> commits) throws JSONException;
 }
