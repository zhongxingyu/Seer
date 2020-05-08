 package tetris;
 
 import java.awt.BorderLayout;
import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.text.DecimalFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.ChartUtilities;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 
 import bluetooth.EDAReader;
 
 public class ChartDrawer extends JFrame implements ActionListener {
 	private static final long serialVersionUID = 1L;
 	private CategoryDataset edaData;
 	private JFreeChart edaChart, diffChart;
 	private ChartPanel edaChartPanel;
 	private final long interval = 5000;
 
 	public void reveal() {
 		pack();
 		setVisible(true);
 	}
 
 	public ChartDrawer() {
 		super("Statistics");
 		CategoryDataset diffData = createDiffDataset();
 		diffChart = createDiffChart(diffData, "Difficulty over time");
 		ChartPanel diffChartPanel = new ChartPanel(diffChart);
 		diffChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 
 		edaData = createEDADataset(0);
 		edaChart = createEDAChart(edaData, "EDA over time");
 		edaChartPanel = new ChartPanel(edaChart);
 		edaChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 
 		getContentPane().add(diffChartPanel, BorderLayout.EAST);
 		getContentPane().add(edaChartPanel, BorderLayout.WEST);
 		createMenu();
 	}
 
 	public void createMenu() {
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu = new JMenu("Curve fitting");
 		menuBar.add(menu);
 		JMenuItem menuItem1 = new JMenuItem("Raw");
 		menuItem1.addActionListener(this);
 		JMenuItem menuItem2 = new JMenuItem("Linear Least Squares");
 		menuItem2.addActionListener(this);
 		menu.add(menuItem1);
 		menu.add(menuItem2);
 		JMenu saveMenu = new JMenu("File");
 		JMenuItem saveMenuItem = new JMenuItem("Save test");
 		saveMenuItem.addActionListener(this);
 		saveMenu.add(saveMenuItem);
 		menuBar.add(saveMenu);
 		setJMenuBar(menuBar);
 		menuItem1.setActionCommand("RAW");
 		menuItem2.setActionCommand("LLS");
 		saveMenuItem.setActionCommand("SAVE");
 	}
 
 	private CategoryDataset createDiffDataset() {
 		DefaultCategoryDataset result = new DefaultCategoryDataset();
 		final String series1 = "Session";
		ArrayList<Double> timeStamps = DifficultyManager.getTimeStamps();
		ArrayList<Float> difficultyStamps = DifficultyManager
				.getDifficultyStamps();
 		DecimalFormat ds = new DecimalFormat("#.");
 		for (int i = 0; i < timeStamps.size(); i++) {
 			Double time = Double.valueOf(ds.format(timeStamps.get(i)));
 			result.addValue(difficultyStamps.get(i), series1, time);
 		}
 		return result;
 	}
 
 	private ArrayList<Float> fitCurve(ArrayList<Float> edaStamps,
 			ArrayList<Long> timeStamps) {
 		ArrayList<Float> fitted = new ArrayList<Float>();
 		int points = calculateIndex(0);
 		for (int i = 0; i + points < edaStamps.size();) {
 			float sumC1 = 0;
 			float sumC2 = 0;
 			float sumA2 = 0;
 			float sumB2 = 0;
 			for (int j = i; j < i + points; j++) {
 				sumC1 += edaStamps.get(j);
 				sumC2 += edaStamps.get(j) * timeStamps.get(j);
 				sumA2 += timeStamps.get(j);
 				sumB2 += timeStamps.get(j) * timeStamps.get(j);
 			}
 			float c1 = 2 * sumC1;
 			float c2 = 2 * sumC2;
 			float a2 = 2 * sumA2;
 			float a1 = 2 * points;
 			float b1 = 2 * sumA2;
 			float b2 = 2 * sumB2;
 
 			float b1Sol = ((c1 * b2) - (b1 * c2)) / ((a1 * b2) - (b1 * a2));
 			float b2Sol = ((a1 * c2) - (a2 * c1)) / ((a1 * b2) - (a2 * b1));
 
 			fitted.add(b1Sol + b2Sol * timeStamps.get(i + points));
 			i += points;
 			points = calculateIndex(i);
 		}
 		return fitted;
 	}
 
 	public int calculateIndex(int index) {
 		ArrayList<Long> gsrTimeStamps = EDAReader.getTimeStampsGSR();
 		int nextIndex = 0;
 		int testIndex = index;
 		long firstStamp = gsrTimeStamps.get(testIndex);
 
 		while (testIndex + 1 < gsrTimeStamps.size()) {
 			nextIndex++;
 			testIndex++;
 			long time = gsrTimeStamps.get(testIndex);
 			if (time - firstStamp >= interval) {
 				return nextIndex;
 			}
 		}
 		return nextIndex + 1;
 	}
 
 	public ArrayList<Long> formatStamps(ArrayList<Long> timeStamps) {
 		ArrayList<Long> result = new ArrayList<Long>();
 		int points = calculateIndex(0);
 		for (int i = 0; i + points < timeStamps.size();) {
 			result.add(timeStamps.get(i) / 1000);
 			i += points;
 			points = calculateIndex(i);
 		}
 		return result;
 	}
 
 	private CategoryDataset createEDADataset(int fittingMethod) {
 		DefaultCategoryDataset result = new DefaultCategoryDataset();
 		final String series1 = "Session";
 		ArrayList<Long> timeStamps = EDAReader.getTimeStampsGSR();
 		ArrayList<Float> gsrStamps = EDAReader.getGSRStamps();
 		switch (fittingMethod) {
 		case 1:
 			System.out.println("Fitted using LSM!");
 			gsrStamps = fitCurve(gsrStamps, timeStamps);
 			timeStamps = formatStamps(timeStamps);
 			break;
 		}
 
 		for (int i = 0; i < gsrStamps.size(); i++) {
 			Long time = timeStamps.get(i);
 			result.addValue(gsrStamps.get(i), series1, time);
 		}
 
 		return result;
 	}
 
 	private JFreeChart createEDAChart(CategoryDataset data, String title) {
 		final JFreeChart chart = ChartFactory.createLineChart("EDA over time",
 				"Time (seconds)", "EDA", data, PlotOrientation.VERTICAL, true, // include
 																				// legend
 				true, // tooltips
 				false // urls
 				);
 		return chart;
 	}
 
 	private JFreeChart createDiffChart(CategoryDataset dataset, String title) {
 		final JFreeChart chart = ChartFactory.createLineChart(
 				"Difficulty over time", // chart title
 				"Time (seconds)", // domain axis label
 				"Difficulty", // range axis label
 				dataset, // data
 				PlotOrientation.VERTICAL, // orientation
 				true, // include legend
 				true, // tooltips
 				false // urls
 				);
 		return chart;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		String command = arg0.getActionCommand();
 		if (command.equals("LLS")) {
 			getContentPane().remove(edaChartPanel);
 			edaData = createEDADataset(1);
 			edaChart = createEDAChart(edaData, "EDA over time");
 			edaChartPanel = new ChartPanel(edaChart);
 			edaChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 			getContentPane().add(edaChartPanel, BorderLayout.WEST);
 			this.validate();
 		} else if (command.equals("RAW")) {
 			getContentPane().remove(edaChartPanel);
 
 			edaData = createEDADataset(0);
 			edaChart = createEDAChart(edaData, "EDA over time");
 			edaChartPanel = new ChartPanel(edaChart);
 			edaChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 			getContentPane().add(edaChartPanel, BorderLayout.WEST);
 			this.validate();
 		} else if (command.equals("SAVE")) {
 			saveTestToFiles();
 		}
 	}
 
 	public void saveTestToFiles() {
 		ArrayList<Long> edaTimeStamps = EDAReader.getTimeStampsGSR();
 		ArrayList<Float> gsrStamps = EDAReader.getGSRStamps();
 		ArrayList<Double> diffTimeStamps;
 		ArrayList<Float> difficultyStamps;
 		
 		if (Game.useGSRFeedback) {
 			diffTimeStamps = EDAReader.getTimeStampsDiff();
 			difficultyStamps = EDAReader.getDifficultyStamps();
 		} else {
 			diffTimeStamps = DifficultyManager.getTimeStamps();
 			difficultyStamps = DifficultyManager.getDifficultyStamps();
 		}
 
 		PrintWriter writer;
 		try {
 			Calendar cal = Calendar.getInstance();
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			String time = sdf.format(cal.getTime());
 			String name = JOptionPane.showInputDialog(this,
 					"Enter the name of the test:", null);
 			String useFeedback = Game.useGSRFeedback ? "feedback " : "";
 			writer = new PrintWriter("tests/"
 					+ (time + " " + useFeedback + name).trim() + " (eda).txt",
 					"UTF-8");
 			for (int i = 0; i < gsrStamps.size(); i++) {
 				writer.println("EDA: " + gsrStamps.get(i) + ", Time: "
 						+ edaTimeStamps.get(i));
 			}
 			writer.close();
 			writer = new PrintWriter("tests/"
 					+ (time + " " + useFeedback + name).trim() + " (diff).txt",
 					"UTF-8");
 			for (int i = 0; i < diffTimeStamps.size(); i++) {
 				writer.println("Difficulty: " + difficultyStamps.get(i)
 						+ ", Time: " + diffTimeStamps.get(i));
 			}
 			writer.close();
 			try {
 				ChartUtilities.saveChartAsPNG(new File("tests/"
 						+ (time + " " + useFeedback + name).trim()
 						+ " (diff).png"), edaChart, 1000, 540);
 				ChartUtilities.saveChartAsPNG(new File("tests/"
 						+ (time + " " + useFeedback + name).trim()
 						+ " (eda).png"), diffChart, 1000, 540);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 	}
 }
