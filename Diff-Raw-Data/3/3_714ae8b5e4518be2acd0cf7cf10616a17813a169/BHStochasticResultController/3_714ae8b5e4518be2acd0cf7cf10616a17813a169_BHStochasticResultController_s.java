 package org.bh.plugin.stochasticResultAnalysis;
 
import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.bh.controller.OutputController;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IntervalValue;
 import org.bh.data.types.StringValue;
 import org.bh.gui.IBHModelComponent;
 import org.bh.gui.chart.BHChartPanel;
 import org.bh.gui.chart.IBHAddValue;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.comp.BHSlider;
 import org.bh.gui.swing.comp.BHTextField;
 import org.bh.gui.swing.importexport.BHDataExchangeDialog;
 import org.bh.gui.view.View;
 import org.bh.platform.IImportExport;
 import org.bh.platform.IPrint;
 import org.bh.platform.Services;
import org.bh.plugin.resultAnalysis.BHResultPanel;
 import org.jfree.chart.JFreeChart;
 
 public class BHStochasticResultController extends OutputController {
     public static enum ChartKeys {
 	DISTRIBUTION_CHART, STANDARD_DEVIATION, AVERAGE, RISK_AT_VALUE, RISK_AT_VALUE_MIN, RISK_AT_VALUE_MAX;
 
 	@Override
 	public String toString() {
 	    return getClass().getName() + "." + super.toString();
 	}
 
     }
 
     public static enum PanelKeys {
 	riskAtValue, AVERAGE, VALUE, PRINTSCENARIO, EXPORTSCENARIO;
 
 	@Override
 	public String toString() {
 	    return getClass().getName() + "." + super.toString();
 	}
 
     }
 
     public BHStochasticResultController(View view, DistributionMap result, DTOScenario scenario) {
 	super(view, result, scenario);
 	// ((BHTextField)(view.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE.toString()))).addCaretListener(new
 	// RiskAtValueListener());
 	BHSlider slider = (BHSlider) view.getBHComponent(ChartKeys.RISK_AT_VALUE.toString());
 	slider.addChangeListener(new SliderChangeListener());
     }
 
     @Override
     public void setResult(DistributionMap result, DTOScenario scenario) {
 	super.setResult(result, scenario);
 	IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.DISTRIBUTION_CHART.toString());
 	comp.addSeries(Services.getTranslator().translate(ChartKeys.DISTRIBUTION_CHART.toString()), result.toDoubleArray(), result.getAmountOfValues(), result.getMaxAmountOfValuesInCluster());
 	comp.addSeries(Services.getTranslator().translate(PanelKeys.AVERAGE.toString()), new double[][] { { result.getAverage(), result.getMaxAmountOfValuesInCluster() } });
 	for (Map.Entry<String, IBHModelComponent> entry : view.getBHModelComponents().entrySet()) {
 	    if (entry.getKey().equals(ChartKeys.STANDARD_DEVIATION.toString()))
 		entry.getValue().setValue(new DoubleValue(result.getStandardDeviation()));
 	    else if (entry.getKey().equals(ChartKeys.AVERAGE.toString()))
 		entry.getValue().setValue(new DoubleValue(result.getAverage()));
 	    // else
 	    // if(entry.getKey().equals(ChartKeys.RISK_AT_VALUE.toString())){
 	    // if(((BHTextField)(view.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE.toString()))).getValue()
 	    // != null){
 	    // double confidence =
 	    // ((DoubleValue)((BHTextField)(view.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE.toString()))).getValue()).getValue();
 	    // calcRiskAtValue(confidence,
 	    // result.getMaxAmountOfValuesInCluster());
 	    // }else
 	    // calcRiskAtValue(null, null);
 	    // }
 	}
 	double confidence = ((BHSlider) view.getBHComponent(ChartKeys.RISK_AT_VALUE.toString())).getValue();
 	calcRiskAtValue(confidence, stochasticResult.getMaxAmountOfValuesInCluster());
 	// calcRiskAtValue(confidence);
     }
 
     public void calcRiskAtValue(Double confidence, Integer maxAmountofValues) {
 	if (confidence == null) {
 	    for (Map.Entry<String, IBHModelComponent> entry : view.getBHModelComponents().entrySet()) {
 		if (entry.getKey().equals(ChartKeys.RISK_AT_VALUE_MAX.toString()))
 		    entry.getValue().setValue(new StringValue(""));
 		else if (entry.getKey().equals(ChartKeys.RISK_AT_VALUE_MIN.toString()))
 		    entry.getValue().setValue(new StringValue(""));
 	    }
 	} else {
 	    IntervalValue interval = stochasticResult.valueAtRisk(confidence);
 
 	    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.DISTRIBUTION_CHART.toString());
 	    comp.removeSeries(2);
 	    comp.addSeries(Services.getTranslator().translate(ChartKeys.RISK_AT_VALUE.toString()),
 		    new double[][] { { interval.getMin(), maxAmountofValues }, { interval.getMax(), maxAmountofValues } });
 	    for (Map.Entry<String, IBHModelComponent> entry : view.getBHModelComponents().entrySet()) {
 		if (entry.getKey().equals(ChartKeys.RISK_AT_VALUE_MAX.toString()))
 		    entry.getValue().setValue(new DoubleValue(interval.getMax()));
 		else if (entry.getKey().equals(ChartKeys.RISK_AT_VALUE_MIN.toString()))
 		    entry.getValue().setValue(new DoubleValue(interval.getMin()));
 	    }
 	}
     }
 
     class RiskAtValueListener implements CaretListener {
 
 	@Override
 	public void caretUpdate(CaretEvent e) {
 	    if (((BHTextField) (view.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE.toString()))).getValue() != null) {
 		double confidence = ((DoubleValue) ((BHTextField) (view.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE.toString()))).getValue()).getValue();
 		calcRiskAtValue(confidence, stochasticResult.getMaxAmountOfValuesInCluster());
 	    } else
 		calcRiskAtValue(null, stochasticResult.getMaxAmountOfValuesInCluster());
 	}
     }
 
     class SliderChangeListener implements ChangeListener {
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 	    double confidence = ((BHSlider) view.getBHComponent(ChartKeys.RISK_AT_VALUE.toString())).getValue();
 	    calcRiskAtValue(confidence, stochasticResult.getMaxAmountOfValuesInCluster());
 	}
 
     }
 
     /* Specified by interface/super class. */
     @Override
     public void actionPerformed(ActionEvent e) {
 	if (e.getSource() instanceof BHButton) {
 	    BHButton b = (BHButton) e.getSource();
 	    if (b.getKey().toString().equals(BHStochasticResultController.PanelKeys.PRINTSCENARIO.toString())) {
 		Map<String, IPrint> pPlugs = Services.getPrintPlugins(IPrint.PRINT_SCENARIO_RES);
 
 		List<JFreeChart> charts = new ArrayList<JFreeChart>();
 		for (Entry<String, IBHAddValue> entry : view.getBHchartComponents().entrySet()) {
 		    if (entry.getValue() instanceof BHChartPanel) {
 			BHChartPanel cp = (BHChartPanel) entry.getValue();
 			charts.add(cp.getChart());
 		    }
 		}
 		((IPrint) pPlugs.values().toArray()[0]).printScenarioResults(scenario, stochasticResult, charts);
 
 	    } else if (b.getKey().toString().equals(BHStochasticResultController.PanelKeys.EXPORTSCENARIO.toString())) {
 		BHDataExchangeDialog dialog = new BHDataExchangeDialog(null, true);
 		dialog.setAction(IImportExport.EXP_SCENARIO_RES);
 		dialog.setModel(scenario);
 		dialog.setResults(stochasticResult);
 
 		dialog.setIconImages(Services.setIcon());
 
 		List<JFreeChart> charts = new ArrayList<JFreeChart>();
 		for (Entry<String, IBHAddValue> entry : view.getBHchartComponents().entrySet()) {
 		    if (entry.getValue() instanceof BHChartPanel) {
 			BHChartPanel cp = (BHChartPanel) entry.getValue();
 			charts.add(cp.getChart());
 		    }
 		}
 		dialog.setCharts(charts);
 
 		dialog.setVisible(true);
 	    }
 	}
     }
 }
