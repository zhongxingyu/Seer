 package ss.apiImpl.charts;
 
 import java.awt.BasicStroke;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import javax.swing.JFrame;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.time.Day;
 import org.jfree.data.time.TimeSeries;
 import org.jfree.data.time.TimeSeriesCollection;
 import org.jfree.ui.ApplicationFrame;
 import org.jfree.ui.RefineryUtilities;
 
 import ss.api.Project;
 
 public class StrategiesChart extends ApplicationFrame {
 
 	private List<TimeSeries> series;
 	private BlockingQueue<Project> projects;
 	private BlockingQueue<TimeSeriesProject> timeSeriesProject;
 	
 	private Day day = new Day();
 	
 	private JFrame frame;
 	private ChartPanel label;
 	private JFreeChart chart;
 	
 	private TimeSeriesCollection dataset;
 
 	private class TimeSeriesProject {
 		TimeSeries timeSeries;
 		Project project;
 
 		public TimeSeriesProject(TimeSeries timeSeries, Project project) {
 			this.timeSeries = timeSeries;
 			this.project = project;
 		}
 
 		public Project getProject() {
 			return project;
 		}
 
 		public TimeSeries getTimeSeries() {
 			return timeSeries;
 		}
 	}
 
 	public StrategiesChart(List<Project> projects) {
 		super("Simulador");
 		initialize(projects);
 	}
 
 	private void initialize(List<Project> projects) {
 		System.out.println("entro al intialize");
 		this.series = new ArrayList<>();
 		this.projects = new LinkedBlockingQueue<>();
 		this.timeSeriesProject = new LinkedBlockingQueue<>();
 		for (Project project : projects) {
 			this.projects.add(project);
 			TimeSeries ts = new TimeSeries("Projecto " + project.getId(),
 					Day.class);
 			series.add(ts);
 			timeSeriesProject.add(new TimeSeriesProject(ts, project));
 		}
 
 		dataset = new TimeSeriesCollection();
 		for (TimeSeries serie : series) {
 			dataset.addSeries(serie);
 		}
 		chart = ChartFactory.createTimeSeriesChart("Simulador",
 				"Tiempo", "Programadores", dataset, true, true, false);
 		final XYPlot plot = chart.getXYPlot();
 		ValueAxis axis = plot.getDomainAxis();
 		axis.setAutoRange(true);
 		axis.setFixedAutoRange(10);
 
 		frame = new JFrame("Simulador");
 		frame.setSize(500, 500);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		label = new ChartPanel(chart);
 		frame.getContentPane().add(label);
 
 		frame.pack();
 		RefineryUtilities.centerFrameOnScreen(frame);
 		frame.setVisible(true);
 		System.out.println("salgo del initialize");
 	}
 
 	
 	public void removeProject(Project p) {
 		System.out.println("entro al remove");
 		TimeSeriesProject toRemove = null;
 		for(TimeSeriesProject tsp: timeSeriesProject) {
 			if(tsp.project.equals(p)) {
 				toRemove = tsp;
 			}
 		}
 		series.remove(toRemove.timeSeries);
 		timeSeriesProject.remove(toRemove);
 		System.out.println("salgo del remove");
 	}
 
 	public void addProject(Project project) {
 		System.out.println("entro al add");
 		projects.add(project);
 		TimeSeries ts = new TimeSeries("Projecto " + project.getId(),
 				Day.class);
 		series.add(ts);
 		timeSeriesProject.add(new TimeSeriesProject(ts, project));
 		dataset.addSeries(ts);
 		System.out.println("salgo del add");
 		
 	}
 
 	public void restart(List<Project> projects) {
 		initialize(projects);
 	}
 	
 	public void updateTime() {
 		System.out.println("entro al update");
 		for (TimeSeriesProject ts : timeSeriesProject) {
 			ts.getTimeSeries().addOrUpdate(day,
 					ts.getProject().getProgrammersWorking());
 		}
 		System.out.println("salgo for 1");
//		try {
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
 		dataset.removeAllSeries();
 		System.out.println("salgo remove all");
 		for (TimeSeriesProject ts : timeSeriesProject) {
 			dataset.addSeries(ts.getTimeSeries());
 		}
 		System.out.println("salgo for 2");
 		chart = ChartFactory.createTimeSeriesChart("Simulador",
 				"Tiempo", "Programadores", dataset, true, true, false);
 		final XYPlot plot = chart.getXYPlot();
 		ValueAxis axis = plot.getDomainAxis();
 		axis.setAutoRange(true);
 		axis.setFixedAutoRange(10);
 		System.out.println("pepe");
 //		frame.revalidate();
 		System.out.println("1");
 		frame.getContentPane().removeAll();
 		System.out.println("2");
		frame.getContentPane().remove(label);
 		ChartPanel label = new ChartPanel(chart);
 		System.out.println("3");
 		frame.getContentPane().add(label);
		frame.pack();
 		System.out.println("4");
 
 		System.out.println("5");
 
 		day = (Day) day.next();
 		repaint();
 		System.out.println("jorge");
 		try {
 			Thread.sleep(350);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		System.out.println("salgo del update");
 	}
 	
 }
