 package org.bh.plugin.branchSpecificRepresentative.swing;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Image;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import org.apache.log4j.Logger;
 import org.bh.data.DTOAccessException;
 import org.bh.data.DTOBranch;
 import org.bh.data.DTOCompany;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.DoubleValue;
 import org.bh.gui.chart.BHChartFactory;
 import org.bh.gui.chart.BHChartPanel;
 import org.bh.gui.swing.BHAboutBox;
 import org.bh.gui.swing.BHPopupFrame;
 import org.bh.gui.swing.comp.BHComboBox;
 import org.bh.gui.swing.comp.BHDescriptionLabel;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.BHTranslator;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * Popup for deviation analysis.
  *
  * <p>
  * This popup shows the deviation analysis of the calculation
  * of the branch specific representative. This means, that we will create a
  * chart displaying the different company values normed to an index and then
  * write all the business data we have into that chart including the 
  * industry specific representative. This last one must be better viewable
  * than all the other entries. So it should be added last.
  *
  * @author Yannick Rödl
  * @version 0.1, 12.12.2011
  * @author Yannick Rödl
  * @version 0.2, 29.12.2011
  *
  */
 
 public class BHDeviationAnalysisFrame extends BHPopupFrame {
 
 	public enum GUI_KEYS{
 		TITLE,
 		LINE_CHART_DEVIATION_ANALYSIS,
 		NORMING,
 		ALGORITHM_REPRESENTATIVE;
 		
 		public String toString(){
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 	
 	private BHChartPanel chartPanel;
 	private BHComboBox cbAvailableAlgorithms, cbNorming;
 	private DTOScenario scenario;
 	private Logger log = Logger.getLogger(BHDeviationAnalysisFrame.class);
 	
 	/**
 	 * Automatically generated <code>serialVersionUID</code>.
 	 */
 	private static final long serialVersionUID = 9090107160317277320L;
 
 	/**
 	 * Standard constructor initializing the chart and all the comboBoxes needed.
 	 */
 	public BHDeviationAnalysisFrame(DTOScenario scenario){
 		super();
 		
 		log.debug("Create Frame for deviation analysis");
 
 		this.scenario = scenario;
 		
 		this.setSize(1000, 700); //We need more space.
 		
 		//Chart to display the deviation analysis
 		chartPanel = BHChartFactory.getLineChart(BHDeviationAnalysisFrame.GUI_KEYS.LINE_CHART_DEVIATION_ANALYSIS);
 		fillChartPanel();
 		/*
 		 * Testdaten für das Chart
 		 */
 //		chartPanel.addSeries("BMW", new double[][]{{2005, 100.0}, {2006, 110.2}, {2007, 120}});
 //		chartPanel.addSeries("Mercedes", new double[][]{{2005, 100.0}, {2006, 87.5}, {2007, 120}});
 //		chartPanel.addSeries("VW", new double[][]{{2005, 100.0}, {2006.0, 98.2}, {2007, 120}});
 //		chartPanel.addSeries("Peugeot", new double[][]{{2005.0, 100.0}, {2006.0, 115}, {2007, 120}});
 //		chartPanel.addSeries("Renault", new double[][]{{2005.0, 100.0}, {2006, 113.2}, {2007, 120}});
 		
 		JScrollPane scrollPane = new JScrollPane(chartPanel);
 		
 		this.setLayout(new BorderLayout());
 		this.add(scrollPane, BorderLayout.CENTER);
 //		this.add(this.getFurtherAnalysisOptionsPanel(), BorderLayout.EAST);
 //		this.add(chartPanel);
 		this.setVisible(true);
 		this.setMinimumSize(new Dimension(900,550));
 		
 		this.setIconImages(Services.setIcon());
 		
 		log.debug("Deviation analysis frame is visible");
 	}
 	
 	@Override
 	public String getTitleKey(){
 		return BHDeviationAnalysisFrame.GUI_KEYS.TITLE.toString();
 	}
 	
 	/**
 	 * This method returns a JPanel providing further ComboBoxes to select
 	 * a specific algorithm for one of the three calculation possibilities.
 	 * 1. Norming of the data
 	 * 2. Calculating representative
 	 * 3. Calculation of goodness of industry specific representative
 	 * 
 	 * @return
 	 */
 	public JPanel getFurtherAnalysisOptionsPanel(){
 		JPanel panel = new JPanel();
 		panel.setMaximumSize(new Dimension(200,800));
 		String colDef = "4px,p,4px";
 		String rowDef = "4px,p,4px,p,4px,p,4px,p,4px";
 		
 		FormLayout layout = new FormLayout(colDef, rowDef);
 		panel.setLayout(layout);
 		CellConstraints cons = new CellConstraints();
 		
 		panel.add(new BHDescriptionLabel(BHDeviationAnalysisFrame.GUI_KEYS.NORMING), cons.xywh(2, 2, 1, 1));
 		panel.add(getComboBoxAvailableNorming(), cons.xywh(2, 4, 1, 1));
 		panel.add(new BHDescriptionLabel(BHDeviationAnalysisFrame.GUI_KEYS.ALGORITHM_REPRESENTATIVE), cons.xywh(2, 6, 1, 1));
 		panel.add(getComboBoxAvailableAlgorithms(), cons.xywh(2, 8, 1, 1));
 		
 		return panel;
 	}
 	
 	/**
 	 * Returns a comboBox providing all available algorithms
 	 * to norm the data, we got for each business.
 	 * @return
 	 */
 	public BHComboBox getComboBoxAvailableNorming(){
 		if(cbNorming == null){
 			cbNorming = new BHComboBox("PSEUDO");
 		}
 		return cbNorming;
 	}
 	
 	/**
 	 * Returns a comboBox providing all available algorithms
 	 * to calculate the industry specific representative.
 	 * @return
 	 */
 	public BHComboBox getComboBoxAvailableAlgorithms(){
 		if(cbAvailableAlgorithms == null){
 			cbAvailableAlgorithms = new BHComboBox("PSEUDO");
 		}
 		return cbAvailableAlgorithms;
 	}
 
 	/**
 	 * This method fills the Chart with all relevant data, meaning all periods of the
 	 * branch specific representative and accordingly all data for every company, which was used
 	 * in the analysis.
 	 */
 	private void fillChartPanel(){
 		DTOCompany branchSpecRep = scenario.getBranchSpecificRep();
 		
 		List<DTOPeriod> periods = branchSpecRep.getChildren();
 		
 		double[][] chartData = createPeriodData(periods);
 		chartPanel.addSeries(BHTranslator.getInstance().translate(DTOScenario.Key.REPRESENTATIVE), chartData);
 		
		DTOBranch branch = scenario.getNormedBranch();
 		
 		for(DTOCompany company: branch.getChildren()){
 			log.debug(company);
 			chartData = createPeriodData(company.getChildren());
 			chartPanel.addSeries(company.get(DTOCompany.Key.NAME).toString(), chartData);
 		}
 		
 	}
 	
 	private double[][] createPeriodData(List<DTOPeriod> periods){
 		double[][] chartData = new double[periods.size()][2];
 		
 		int i = periods.size() -1;
 		
 		for(DTOPeriod period: periods){
 			try{
 				chartData[i][0] = ((DoubleValue) (period.get(DTOPeriod.Key.NAME))).getValue();
 			} catch (ClassCastException cce){
 				chartData[i][0] = Double.parseDouble(period.get(DTOPeriod.Key.NAME).toString());
 			}
 			try{
 				chartData[i][1] = ((DoubleValue) (period.get(DTOPeriod.Key.FCF))).getValue();
 			} catch (DTOAccessException ae){
 				log.debug("Period not readable: " + period, ae);
 				chartData[i][1] = 0;
 			}
 //			log.debug("" + chartData[i][0]);
 //			log.debug("" + chartData[i][1]);
 			i--;
 		}
 		
 		return chartData;
 	}
 	
 }
