 package edu.jhu.twacker.client.manager;
 
import java.util.Calendar;
 import java.util.Date;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.visualization.client.AbstractDataTable;
 import com.google.gwt.visualization.client.DataTable;
 import com.google.gwt.visualization.client.AbstractDataTable.ColumnType;
 import com.google.gwt.visualization.client.visualizations.corechart.AxisOptions;
 import com.google.gwt.visualization.client.visualizations.corechart.LineChart;
 import com.google.gwt.visualization.client.visualizations.corechart.Options;
 import com.google.gwt.visualization.client.visualizations.corechart.PieChart;
 
 public class ChartManager {
 		
 	private static DataTable table;
 	private static int count;
 	
 	public static PieChart createPieChart(String result) {
 		return new PieChart(createPieData(result), createPieOptions(result));
 	}
 	
 	private static AbstractDataTable createPieData(String result) {
 		DataTable data2 = DataTable.create();
 		data2.addColumn(ColumnType.STRING, "Sentiment");
 		data2.addColumn(ColumnType.NUMBER, "Number");
 		data2.addRows(4);
 		
 		String[] days = result.substring(result.indexOf("positive"), result.indexOf("}, \"experts\"")).split(",");
 		int[] counts = new int[days.length];
 		for (int i = 0; i < days.length; i++) {
 			counts[i] = Integer.parseInt(days[i].replaceAll("\\D", ""));
 		}
 		
 		data2.setValue(0, 0, "Positive");
 		data2.setValue(0, 1, counts[0]);
 		
 		data2.setValue(1, 0, "Negative");
 		data2.setValue(1, 1, counts[1]);
 		
 		data2.setValue(2, 0, "Neutral");
 		data2.setValue(2, 1, counts[2]);
 		
 		data2.setValue(3, 0, "ERROR");
 		data2.setValue(3, 1, counts[4]);
 
 		return data2;
 	}
 	
 	private static Options createPieOptions(String result) {
 		Options options = Options.create();
 		options.setWidth(700);
 		options.setHeight(175);
 		options.setTitle("Sentiment Results for "	+ result.substring(result.indexOf(": \"")+3, result.indexOf("\", ")));
 		return options;
 	}
 	
 	public static LineChart createLineChart(String result) {
 		return new LineChart(table, createLineOptions(result));
 	}
 	
 	public static void initDataTable() {
 		table = DataTable.create();
 		table.addColumn(ColumnType.DATE, "Day");
 		count = 0;
 	}	
 	
 	public static void updateTable(String result) {
 		table.addColumn(ColumnType.NUMBER, 
 						result.substring(result.indexOf(": \"")+3, result.indexOf("\",")));
 		
 		String[] days = result.substring(result.indexOf("data"), result.indexOf("],")).split(",");
 		int[] counts = new int[days.length];
 		for (int i = 0; i < days.length; i++) {
 			counts[i] = Integer.parseInt(days[i].replaceAll("\\D", ""));
 		}
 		
		if (count == 0)
 			table.addRows(counts.length);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());		
 
 		for (int i = 0; i < counts.length; i++) {
 			if (count == 0) {
				cal.add(Calendar.DAY_OF_YEAR, -1);
				table.setValue(i, 0, cal.getTime());
 			}
 			table.setValue(i, count + 1, counts[i]);
 		}
 		count++;
 	}
 	
 	public static Options createLineOptions(String result) {
 		Options options = Options.create();
 		options.setWidth(700);
 		options.setHeight(525);
 		options.setTitle("Twacker Frequency");
 		options.setPointSize(5);
//		DateFormat.Options o = DateFormat.Options.create();
//		o.setPattern(DateFormat.FormatType.SHORT);
//		DateFormat f = DateFormat.create(o);
//		f.format(table, 0);
 		AxisOptions opt = AxisOptions.create();
		opt.set("viewWindowMode", "pretty");
 		options.setHAxisOptions(opt);
 		return options;
 	}
 	
 	public static HorizontalPanel createExpertsPanel(String result) {
 		String expertStr = result.substring(result.indexOf("experts"), result.indexOf("] } }"));
 		System.out.println(expertStr);
 		String[] experts = expertStr.split("} , \\{");
 		
 		HorizontalPanel panel = new HorizontalPanel();
 		for (int i = 0; i < experts.length; i++) {
 			VerticalPanel expert = new VerticalPanel();
 			String photoUrl = experts[i].substring(experts[i].indexOf("\"photoUrl\" : \"")+14, experts[i].indexOf("\", \"influenceLevel\""));
 			Image im = new Image(photoUrl);
 			im.getElement().getStyle().setWidth(100, Unit.PX);
 			im.getElement().getStyle().setHeight(100, Unit.PX);
 			expert.add(im);
 			String name = experts[i].substring(experts[i].indexOf("username\" : \"")+13, experts[i].indexOf("\", \"photoUrl"));
 			String url = experts[i].substring(experts[i].indexOf("\"url\" : \"")+9);
 			url = url.substring(0, url.indexOf("\""));
 			expert.add(new HTML("<a href='" + url + "'>"+name+"</a>"));
 			String influence = experts[i].substring(experts[i].indexOf("\"influenceLevel\" : \"")+20, experts[i].indexOf("\", \"url\""));
 			expert.add(new Label("Influence : " + influence));
 			expert.getElement().getStyle().setPadding(20, Unit.PX);
 			panel.add(expert);
 		}
 		return panel;
 	}
 	
 	public static int getCount() {
 		return count;
 	}
 }
