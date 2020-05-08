 /**
  *
  */
 package driver.applet;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.InputStream;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 
 import cobweb.Environment.EnvironmentStats;
 import cobweb.LocalUIInterface.TickEventListener;
 import driver.Parser;
 import driver.SimulatorUI;
 
 
 /**
  * Applet version of COBWEB2
  * @author igor
  *
  */
 public class Cobweb2Applet extends JApplet {
 
 	/**
 	 *
 	 */
 	private static final long serialVersionUID = 3127350835002502812L;
 
 	Map<String, String> experements = new LinkedHashMap<String, String>();
 
 
 	SimulatorUI ui;
 	JPanel controls;
 	Parser parser;
 	String currentexp;
 	ExperementSelector expselector;
 
 	JLabel statsLabel;
 
 	@Override
 	public void init() {
 		super.init();
 
 		setSize(580,660);
 
 		setLayout(new BorderLayout());
 
		experements.put("Baseline 2009", "baseline 2009.xml");
		experements.put("Boom and Bust", "boom and bust 2 applet.xml");
 		experements.put("Exponential Growth", "Exponential Growth Experiment.xml");
		experements.put("Central Place", "central place applet.xml");
 		experements.put("Cheaters vs Cooperators", "cheaters vs cooperators.xml");
 
		currentexp = "Baseline 2009";
 
 		controls = new JPanel();
 
 		JLabel selectorlabel = new JLabel("Experiment:");
 		controls.add(selectorlabel);
 
 		expselector = new ExperementSelector(experements);
 		controls.add(expselector);
 
 		JButton resetbutton = new JButton("Reset");
 		controls.add(resetbutton);
 		resetbutton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				loadSimulation(currentexp);
 			}
 		});
 
 		statsLabel = new JLabel("Statistics:");
 		controls.add(statsLabel);
 
 		JButton statsButton = new JButton("Graph");
 		statsButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				statsUpdater.toggleGraphVisible();
 			}
 
 		});
 
 		controls.add(statsButton);
 
 
 
 
 		add(controls, BorderLayout.NORTH);
 		expselector.setVisible(true);
 
 		expselector.addActionListener(new ExpSelectorListener());
 
 		loadSimulation(currentexp);
 
 	}
 
 	private class ExpSelectorListener implements ActionListener {
 
 		/* (non-Javadoc)
 		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 		 */
 		public void actionPerformed(ActionEvent e) {
 			JComboBox cb = (JComboBox)e.getSource();
 			String expname = (String)cb.getSelectedItem();
 			loadSimulation(expname);
 		}
 
 	}
 
 	/**
 	 * @param expname
 	 */
 	private void loadSimulation(String expname) {
 		this.currentexp = expname;
 		if (ui != null) {
 
 			ui.RemoveTickEventListener(statsUpdater);
 			remove(ui);
 		}
 
 
 		InputStream datafile = getClass().getResourceAsStream("/resources/" + experements.get(expname));
 
 		parser = new Parser(datafile);
 		ui = new SimulatorUI(parser);
 
 		//FIX: DisplayPanel is buggy, so we have to hide and show it for it to redraw
 		ui.setVisible(false);
 		add(ui, BorderLayout.CENTER);
 
 		ui.setVisible(true);
 		statsUpdater = new StatsUpdater();
 		ui.AddTickEventListener(statsUpdater);
 	}
 
 	StatsUpdater statsUpdater;
 
 	private class StatsUpdater implements TickEventListener {
 
 		JFrame graph = new JFrame("Statistics");
 
 		public StatsUpdater() {
 			graph.setSize(500, 500);
 			ChartPanel cp = new ChartPanel(plot, true);
 			graph.add(cp);
 			plot.setAntiAlias(true);
 			plot.setNotify(false);
 
 			data.addSeries(agentData);
 			data.addSeries(foodData);
 		}
 
 
 
 		public void toggleGraphVisible() {
 			graph.setVisible(!graph.isVisible());
 		}
 
 
 
 		int frame = 0;
 		static final int frameskip = 50;
 
 		XYSeries agentData = new XYSeries("Agents");
 		XYSeries foodData = new XYSeries("Food");
 
 		XYSeriesCollection data = new XYSeriesCollection();
 
 		JFreeChart plot = ChartFactory.createXYLineChart(
 				"Agent and Food count"
 				, "Time"
 				, "Count"
 				, data
 				, PlotOrientation.VERTICAL
 				, true
 				, false
 				, false);
 
 		public void TickPerformed(long currentTick) {
 
 
 			EnvironmentStats stats = ui.getStatistics();
 			long agentCount = 0;
 			for (long count : stats.agentCounts) {
 				agentCount += count;
 			}
 			long foodCount = 0;
 			for (long count : stats.foodCounts) {
 				foodCount += count;
 			}
 
 			agentData.add(currentTick, agentCount);
 			foodData.add(currentTick, foodCount);
 
 			if (frame++ == frameskip) {
 				frame = 0;
 				plot.setNotify(true);
 				plot.setNotify(false);
 			}
 		}
 
 	}
 
 
 
 
 }
