 package analyzation;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
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
 import org.jfree.chart.plot.CategoryPlot;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.category.CategoryDataset;
 import org.jfree.data.category.DefaultCategoryDataset;
 
 import tetris.Game;
 import tetris.GamePlayState;
 
 public class EDAChartDrawer extends JFrame implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private static int interval = 5000;
 	private CategoryDataset edaData;
 	private JFreeChart edaChart;
 	private ChartPanel edaChartPanel;
 	private File testFile;
 	private ArrayList<Long> timeStamps;
 	private ArrayList<Float> edaStamps;
 	private boolean fitted;
 
 	public EDAChartDrawer(File file) {
 		testFile = file;
 		fitted = false;
 		edaData = createEDADataset(0);
 		edaChart = createEDAChart(edaData, "EDA over time");
 		edaChartPanel = new ChartPanel(edaChart);
 		edaChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 		getContentPane().add(edaChartPanel);
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
 		JMenu saveMenu = new JMenu("Analyze");
 		JMenuItem saveMenuItem = new JMenuItem("Correlation analysis");
 		saveMenuItem.addActionListener(this);
 		saveMenu.add(saveMenuItem);
 		menuBar.add(saveMenu);
 		setJMenuBar(menuBar);
 		menuItem1.setActionCommand("RAW");
 		menuItem2.setActionCommand("LLS");
 		saveMenuItem.setActionCommand("ANA");
 	}
 
 	public ArrayList<Float> getEDA() {
 		ArrayList<Float> edaStamps = new ArrayList<Float>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(testFile));
 			try {
 				String line = in.readLine();
 				while (line != null) {
 					if (!testFile.getName().contains("csv")) {
 						edaStamps.add(Float.parseFloat(line.split(" ")[1]
 								.replaceAll(",", "")));
 					} else {
 						System.out.println(line);
 						edaStamps.add(Float.parseFloat(line.split(",")[0]));
 					}
 					line = in.readLine();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		return edaStamps;
 	}
 
 	public ArrayList<Long> getTime() {
 		ArrayList<Long> edaTimeStamps = new ArrayList<Long>();
 		try {
 			BufferedReader in = new BufferedReader(new FileReader(testFile));
 			try {
 				String line = in.readLine();
 				while (line != null) {
 					if (!testFile.getName().contains("csv")) {
 						edaTimeStamps.add(Long.parseLong(line.split(" ")[3]));
 					} else {
 						edaTimeStamps.add(Long.parseLong(line.split(",")[1]));
 					}
 					line = in.readLine();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		return edaTimeStamps;
 	}
 
 	private CategoryDataset createEDADataset(int fittingMethod) {
 
 		DefaultCategoryDataset result = new DefaultCategoryDataset();
 		final String series1 = "Session";
 		timeStamps = getTime();
 		edaStamps = getEDA();
 		fitted = false;
 		switch (fittingMethod) {
 		case 1:
 			System.out.println("Fitted using LSM!");
 			Object[] options = { "5", "1" };
 			int n = JOptionPane.showOptionDialog(this,
 					"Please choose an interval length", "Fitting interval",
 					JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
 			if (n == 1) {
 				interval = 1000;
 			} else {
 				interval = 5000;
 			}
 			edaStamps = fitCurve(edaStamps, timeStamps);
 			timeStamps = formatStamps(timeStamps);
 			fitted = true;
 			break;
 		}
 
 		for (int i = 0; i < edaStamps.size(); i++) {
 			Long time = timeStamps.get(i);
 			result.addValue(edaStamps.get(i), series1, time);
 		}
 
 		return result;
 	}
 
 	public ArrayList<Long> formatStamps(ArrayList<Long> timeStamps) {
 		ArrayList<Long> result = new ArrayList<Long>();
 		int points = calculateIndex(0);
 		result.add(timeStamps.get(0)/1000);
 		
 		for (int i = 0; i + points < timeStamps.size();) {
 			i += points;
 			result.add((Long)timeStamps.get(i) / 1000);
 			points = calculateIndex(i);
 		}
 		return result;
 	}
 
 	private JFreeChart createEDAChart(CategoryDataset data, String title) {
 		final JFreeChart chart = ChartFactory.createLineChart("EDA over time",
				"Time (seconds)", "EDA", data, PlotOrientation.VERTICAL, false,
 				true, false);
 		CategoryPlot plot = (CategoryPlot) chart.getPlot();
 		plot.setBackgroundPaint(Color.white);
 		plot.setRangeGridlinePaint(Color.DARK_GRAY);
		plot.setDomainGridlinesVisible(false);
		plot.setRangeGridlinesVisible(false);
		
 		return chart;
 	}
 
 	private ArrayList<Float> fitCurve(ArrayList<Float> edaStamps,
 			ArrayList<Long> timeStamps) {
 		ArrayList<Float> fitted = new ArrayList<Float>();
 		int points = calculateIndex(0);
 		fitted.add(edaStamps.get(0));
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
 		ArrayList<Long> gsrTimeStamps = getTime();
 		int nextIndex = 0;
 		int testIndex = index;
 		long firstStamp = gsrTimeStamps.get(testIndex);
		int second = (int)(firstStamp/1000);
		int lastSecond = second;
 		while (testIndex + 1 < gsrTimeStamps.size()) {
 			nextIndex++;
 			testIndex++;
 			long time = gsrTimeStamps.get(testIndex);	
			second = ((int) (time / 1000));
			if (second % 5 == 0 && lastSecond != second) {				
 				return nextIndex;
 			}
 		}
 		return nextIndex + 1;
 	}
 
 	public void reveal() {
 		pack();
 		setTitle("Data analysis");
 		setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		String command = arg0.getActionCommand();
 		if (command.equals("LLS")) {
 			edaData = createEDADataset(1);
 			redrawChartPanel();
 			this.validate();
 		} else if (command.equals("RAW")) {
 			edaData = createEDADataset(0);
 			redrawChartPanel();
 			this.validate();
 		} else if (command.equals("ANA")) {
 			correlationAnalysis();
 		}
 	}
 
 	private void redrawChartPanel() {
 		getContentPane().remove(edaChartPanel);
 		edaChart = createEDAChart(edaData, "EDA over time");
 
 		edaChartPanel = new ChartPanel(edaChart);
 		edaChartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
 		edaChartPanel.setHorizontalAxisTrace(true);
 		edaChartPanel.setVerticalAxisTrace(true);
 		getContentPane().add(edaChartPanel);
 	}
 
 	public void correlationAnalysis() {
 		RegressionAnalysist analysist = new RegressionAnalysist();
 
 		if (fitted) {
 			if (!testFile.getName().contains("feedback")) {
 				ArrayList<Float> diffStamps = new ArrayList<Float>();
 				diffStamps = getDifficultySet();
 				edaStamps = getEDASet(diffStamps);
 				for (Float l : diffStamps) {
 					System.out.println(l);
 				}
 				for (Float l : edaStamps) {
 					System.out.println(l);
 				}
 				JOptionPane.showMessageDialog(
 						this,
 						"Correlation coefficient: "
 								+ analysist.correlationAnalysisEDA(diffStamps,
 										edaStamps), "Result",
 						JOptionPane.INFORMATION_MESSAGE);
 
 			}
 		} else {
 			if (!testFile.getName().contains("feedback")) {
 				ArrayList<Float> diffStamps = new ArrayList<Float>();
 				ArrayList<Float> edaStamps = new ArrayList<Float>();
 				edaStamps = formatRawEDA(fitted);
 				diffStamps = getDifficultySetRaw(edaStamps, 1);
 				edaStamps = getEDASetRaw(edaStamps, diffStamps);
 
 				JOptionPane.showMessageDialog(
 						this,
 						"Correlation coefficient: "
 								+ analysist.correlationAnalysisEDA(diffStamps,
 										edaStamps), "Result",
 						JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 	}
 
 	public ArrayList<Float> formatRawEDA(boolean fitted) {
 		ArrayList<Float> rawEDAStamps = new ArrayList<Float>();
 		ArrayList<Long> gsrTimeStamps = timeStamps;
 		long firstStamp = gsrTimeStamps.get(0);
 		int lastSecond;
 		if (!fitted)
 			lastSecond = ((int) (firstStamp / 1000));
 		else
 			lastSecond = (int) (firstStamp);
 
 		rawEDAStamps.add(edaStamps.get(0));
 		for (int i = 0; i < edaStamps.size(); i++) {
 			long time = gsrTimeStamps.get(i);
 			int second;
 			if (!fitted)
 				second = ((int) (time / 1000));
 			else
 				second = (int) (time);
 			if (second % 5 == 0 && second != lastSecond) {
 				lastSecond = second;
 				rawEDAStamps.add(edaStamps.get(i));
 			}
 		}
 		return rawEDAStamps;
 	}
 
 	public ArrayList<Float> getEDASetRaw(ArrayList<Float> edaStamps,
 			ArrayList<Float> diffStamps) {
 		ArrayList<Float> edaStampsShort = new ArrayList<Float>();
 		for (int i = 0; i < diffStamps.size(); i++) {
 			edaStampsShort.add(edaStamps.get(i));
 		}
 		return edaStampsShort;
 	}
 
 	public ArrayList<Float> getEDASet(ArrayList<Float> diffStamps) {
 		ArrayList<Float> edaStampsShort = new ArrayList<Float>();
 		for (int i = 0; i < diffStamps.size(); i++) {
 			edaStampsShort.add(edaStamps.get(i));
 		}
 		return edaStampsShort;
 	}
 
 	public ArrayList<Float> getDifficultySetRaw(ArrayList<Float> edaStamps,
 			int seconds) {
 		ArrayList<Float> diffStamps = new ArrayList<Float>();
 		System.out.println("Seconds : " + seconds);
 		try {
 			String difFileName = testFile.getAbsolutePath().replaceFirst(
 					"-eda", "-dif");
 			difFileName = difFileName.replaceFirst(" \\(FIXED\\)", "");
 			System.out.println(difFileName);
 			File difFile = new File(difFileName);
 			BufferedReader in = new BufferedReader(new FileReader(difFile));
 			try {
 				String line = in.readLine();
 				int i = 1;
 				diffStamps.add(Float.parseFloat(line.split(",")[0]));
 				System.out.println("EDA-Stamps: " + edaStamps.size());
 				while (line != null && i <= edaStamps.size()) {
 					line = in.readLine();
 					if (line != null) {
 						diffStamps.add(Float.parseFloat(line.split(",")[0]));
 						i++;
 					}
 					System.out.println("Line: " + line);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Length: " + diffStamps.size());
 		return diffStamps;
 	}
 	
 	public ArrayList<Float> getDifficultySet() {
 		ArrayList<Float> diffStamps = new ArrayList<Float>();
 		try {
 			String difFileName = testFile.getAbsolutePath().replaceFirst(
 					"-eda", "-dif");
 			difFileName = difFileName.replaceFirst(" \\(FIXED\\)", "");
 			System.out.println(difFileName);
 			File difFile = new File(difFileName);
 			BufferedReader in = new BufferedReader(new FileReader(difFile));
 			try {
 				String line = in.readLine();
 				int i = 1;
 				diffStamps.add(Float.parseFloat(line.split(",")[0]));
 				System.out.println("EDA-Stamps: " + edaStamps.size());
 				while (line != null && i < edaStamps.size()) {
 					line = in.readLine();
 					if (line != null) {
 						diffStamps.add(Float.parseFloat(line.split(",")[0]));
 						i++;
 						System.out.println("i: " + i);
 					}
 					System.out.println("Line: " + line);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		System.out.println("Length: " + diffStamps.size() + ", EDASTAMPS LENGTH: " + edaStamps.size());
 		return diffStamps;
 	}
 
 	public void saveTestToFiles() {
 		PrintWriter writer;
 
 		try {
 			Calendar cal = Calendar.getInstance();
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 			String time = sdf.format(cal.getTime());
 			String name = JOptionPane.showInputDialog(this,
 					"Enter the name of the test:", null);
 			name = name.replace(" ", "-").toLowerCase();
 			String useFeedback = Game.useGSRFeedback ? "feedback-" : "";
 			String score = "-" + GamePlayState.getScore() + "pts";
 
 			// Create tests folder
 			File dir = new File("tests/" + name);
 			dir.mkdir();
 
 			String fileName = "tests/" + name + "/"
 					+ (time + " " + useFeedback + score).trim();
 
 			writer = new PrintWriter(fileName + "-eda.txt", "UTF-8");
 			for (int i = 0; i < edaStamps.size(); i++) {
 				writer.println("EDA: " + edaStamps.get(i) + ", Time: "
 						+ timeStamps.get(i));
 			}
 			writer.close();
 
 			try {
 				ChartUtilities.saveChartAsPNG(new File(fileName + "-eda.png"),
 						edaChart, 1000, 540);
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
