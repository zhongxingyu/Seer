 package org.bh.plugin.stochasticResultAnalysis;
 
 import info.clearthought.layout.TableLayout;
 
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.bh.gui.chart.BHChartFactory;
 import org.bh.gui.chart.BHChartPanel;
 import org.bh.gui.swing.BHDescriptionLabel;
 import org.bh.gui.swing.BHTextField;
 import org.bh.gui.swing.BHValueLabel;
 
 public class StochasticPanel extends JPanel{
 
 	 private static final Logger log = Logger.getLogger(StochasticPanel.class);
 	    //Chart
 	    private BHChartPanel distributionChart;
 	    
 	    public StochasticPanel(){
 	        this.initialize();
 	    }
 
 	    public void initialize() {
 	        double border = 10;
 	        double size[][] = {{border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border}, // Columns
 	            {border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border, TableLayout.PREFERRED, border,TableLayout.PREFERRED, 
 	        	border,TableLayout.PREFERRED,border,TableLayout.PREFERRED}}; // Rows
 
 
 	        this.setLayout(new TableLayout(size));
 
 	        distributionChart = BHChartFactory.getXYBarChart(BHStochasticResultController.ChartKeys.DISTRIBUTION_CHART);
	        
	        
 	        BHDescriptionLabel sd = new BHDescriptionLabel("standardDeviation");
 	        BHDescriptionLabel ew = new BHDescriptionLabel("average");
 	        BHValueLabel sdValue = new BHValueLabel(BHStochasticResultController.ChartKeys.STANDARD_DEVIATION);
 	        BHValueLabel ewValue = new BHValueLabel(BHStochasticResultController.ChartKeys.AVERAGE);
 	        
 	        BHDescriptionLabel riskAt = new BHDescriptionLabel("riskAtValue");
 	        BHTextField riskAtField = new BHTextField(BHStochasticResultController.ChartKeys.RISK_AT_VALUE,"95", true);
 	        
 	        BHDescriptionLabel min = new BHDescriptionLabel("min");
 	        BHDescriptionLabel max = new BHDescriptionLabel("max");
 	        BHValueLabel minValue = new BHValueLabel(BHStochasticResultController.ChartKeys.RISK_AT_VALUE_MIN);
 	        BHValueLabel maxValue = new BHValueLabel(BHStochasticResultController.ChartKeys.RISK_AT_VALUE_MAX);
 	        
 	        this.add(distributionChart, "3,3"); 
 	        
 	        this.add(sd, "1,5");
 	        this.add(sdValue, "3,5");
 	        
 	        this.add(ew, "1,7");
 	        this.add(ewValue, "3,7");
 	        
 	        this.add(riskAt, "1,9");
 	        this.add(riskAtField, "3,9");
 	        this.add(min, "1,11");
 	        this.add(minValue, "3,11");
 	        this.add(max, "1,13");
 	        this.add(maxValue, "3,13");
 	    }
 	}
