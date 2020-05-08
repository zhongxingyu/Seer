 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber * : Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
  *
  * Copyright 2009: Manuel Bross, Simon Drees, Marco Hammel, Patrick Heinz, Marcel Hockenberger, Marcus Katzor, Edgar Kauz, Anton Kharitonov, Sarah Kuhn, Michael Löckelt, Heiko Metzger, Jacqueline Missikewitz, Marcel Mrose, Steffen Nees, Alexander Roth, Sebastian Scharfenberger, Carsten Scheunemann, Dave Schikora, Alexander Schmalzhaf, Florian Schultze, Klaus Thiele, Patrick Tietze, Robert Vollmer, Norman Weisenburger, Lars Zuckschwerdt
  *
  * Copyright 2008: Camil Bartetzko, Tobias Bierer, Lukas Bretschneider, Johannes Gilbert, Daniel Huser, Christopher Kurschat, Dominik Pfauntsch, Sandra Rath, Daniel Weber
  *
  * This program is free software: you can redistribute it and/or modify it un-der the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FIT-NESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *******************************************************************************/
 package org.bh.plugin.stochasticResultAnalysis;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.Map.Entry;
 
 import javax.swing.JLabel;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.table.DefaultTableModel;
 
 import org.bh.calculation.ITimeSeriesProcess;
 import org.bh.controller.OutputController;
 import org.bh.data.DTOKeyPair;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.data.types.DistributionMap;
 import org.bh.data.types.DoubleValue;
 import org.bh.data.types.IntervalValue;
 import org.bh.data.types.StringValue;
 import org.bh.gui.IBHModelComponent;
 import org.bh.gui.chart.BHChartPanel;
 import org.bh.gui.chart.IBHAddValue;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.comp.BHCheckBox;
 import org.bh.gui.swing.comp.BHSlider;
 import org.bh.gui.swing.comp.BHTable;
 import org.bh.gui.swing.comp.BHTextField;
 import org.bh.gui.swing.importexport.BHDataExchangeDialog;
 import org.bh.gui.view.View;
 import org.bh.platform.IImportExport;
 import org.bh.platform.IPrint;
 import org.bh.platform.PlatformEvent;
 import org.bh.platform.Services;
 import org.jfree.chart.JFreeChart;
 
/**
 * @author unknown
 * @update 23.12.2010 Timo Klein
 * @update 12.12.2011 Guenter Hesse
 */
 
 public class BHStochasticResultController extends OutputController {
 	public static enum ChartKeys {
 		DISTRIBUTION_CHART, STANDARD_DEVIATION, AVERAGE, RISK_AT_VALUE, RISK_AT_VALUE_MIN, RISK_AT_VALUE_MAX, CASHFLOW_CHART, CASHFLOW_CHART_COMPARE, CASHFLOW_COMPARE_SLIDER, COMPARE_P_HEAD, CASHFLOW_FORECAST;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 
 	}
 
 	int pAlt = 0;
 
 	public static enum PanelKeys {
 		riskAtValue, AVERAGE, VALUE, PRINTSCENARIO, EXPORTSCENARIO, CASHFLOW, CASHFLOW_TABLE, COMPARE_P, CASHFLOW_IS, CASHFLOW_FORECAST;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 
 	}
 
 	public BHStochasticResultController(View view, DistributionMap result,
 			DTOScenario scenario) {
 		super(view, result, scenario);
 		// ((BHTextField)(view.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE.toString()))).addCaretListener(new
 		// RiskAtValueListener());
 		BHSlider slider = (BHSlider) view
 				.getBHComponent(ChartKeys.RISK_AT_VALUE.toString());
 		slider.addChangeListener(new SliderChangeListener());
 
 		if (result.isTimeSeries()) {
 			setResultTimeSeries(result, scenario);
 
 		}
 
 	}
 
 	// Code wird ausgeführt bei Locale Change. Hier müssten dann noch die Charts
 	// neu gezeichnet werden.
 	public void platformEvent(PlatformEvent e) {
 		switch (e.getEventType()) {
 		case LOCALE_CHANGED:
 
 			break;
 		default:
 			break;
 		}
 	}
 
 	@Override
 	public void setResult(DistributionMap result, DTOScenario scenario) {
 		log.info("Distribution Chart "+ ChartKeys.DISTRIBUTION_CHART.toString());
 		super.setResult(result, scenario);
 		IBHAddValue comp = super.view.getBHchartComponents().get(
 				ChartKeys.DISTRIBUTION_CHART.toString());
 		comp.addSeries(
 				Services.getTranslator().translate(
 						ChartKeys.DISTRIBUTION_CHART.toString()),
 				result.toDoubleArray(), result.getAmountOfValues(),
 				result.getMaxAmountOfValuesInCluster());
 		comp.addSeries(
 				Services.getTranslator()
 						.translate(PanelKeys.AVERAGE.toString()),
 				new double[][] { { result.getAverage(),
 						result.getMaxAmountOfValuesInCluster() } });
 		// componenten für LineChart (Zeitreihenanalyse)
 
 		for (Map.Entry<String, IBHModelComponent> entry : view
 				.getBHModelComponents().entrySet()) {
 			if (entry.getKey().equals(ChartKeys.STANDARD_DEVIATION.toString()))
 				entry.getValue().setValue(
 						new DoubleValue(result.getStandardDeviation()));
 			else if (entry.getKey().equals(ChartKeys.AVERAGE.toString()))
 				entry.getValue().setValue(new DoubleValue(result.getAverage()));
 		}
 		double confidence = ((BHSlider) view
 				.getBHComponent(ChartKeys.RISK_AT_VALUE.toString())).getValue();
 		calcRiskAtValue(confidence,
 				stochasticResult.getMaxAmountOfValuesInCluster());
 		// calcRiskAtValue(confidence);
 	}
 
 	public void setResultTimeSeries(DistributionMap result, DTOScenario scenario) {
 
 		BHSlider sliderCompare = (BHSlider) view
 				.getBHComponent(ChartKeys.CASHFLOW_COMPARE_SLIDER.toString());
 		sliderCompare.addChangeListener(new SliderChangeListener());
 
 		IBHAddValue comp2 = super.view.getBHchartComponents().get(
 				ChartKeys.CASHFLOW_CHART.toString());
 		comp2.addSeries(
 				Services.getTranslator().translate(
 						PanelKeys.CASHFLOW.toString()),
 				result.toDoubleArrayTS());
 
 		IBHAddValue comp3 = super.view.getBHchartComponents().get(
 				ChartKeys.CASHFLOW_CHART_COMPARE.toString());
 		comp3.addSeries(
 				Services.getTranslator().translate(
 						PanelKeys.CASHFLOW_IS.toString()),
 				result.getIsCashflow());
 		comp3.addSeries(
 				Services.getTranslator().translate(
 						PanelKeys.CASHFLOW_FORECAST.toString()),
 				result.getCompareCashflow());
 
 		String TableKey = PanelKeys.CASHFLOW_TABLE.toString();
 		BHTable cashTable = ((BHTable) view.getBHComponent(TableKey));
 		Object[][] data = result.toObjectArrayTS();
 		int länge = result.getIsCashflow().length;
 		sliderCompare.setMaximum(länge - 2);
 		Dictionary map = new Hashtable();
 		for (int i = 0; i <= länge; i++) {
 			if ((i % 2) == 0)
 				map.put(i, new JLabel("" + i));
 		}
 		sliderCompare.setLabelTable(map);
 		String[] headers = { "t", "Cashflow" };
 		DefaultTableModel tableModel = new DefaultTableModel(data, headers);
 		cashTable.setTableModel(tableModel);
 
 	}
 
 	public void calcNewComparison(int p) {
 		ITimeSeriesProcess TSprocess = stochasticResult.getTimeSeriesProcess();
 		TreeMap<Integer, Double>[] compareResults = TSprocess
 				.calculateCompare(p);
 		stochasticResult.setTimeSeriesCompare(compareResults);
 		IBHAddValue comp3 = super.view.getBHchartComponents().get(
 				ChartKeys.CASHFLOW_CHART_COMPARE.toString());
 		comp3.removeSeries(1);
 		comp3.addSeries(
 				Services.getTranslator().translate(
 						PanelKeys.CASHFLOW_FORECAST.toString()),
 				stochasticResult.getCompareCashflow());
 
 	}
 
 	public void calcRiskAtValue(Double confidence, Integer maxAmountofValues) {
 		if (confidence == null) {
 			for (Map.Entry<String, IBHModelComponent> entry : view
 					.getBHModelComponents().entrySet()) {
 				if (entry.getKey().equals(
 						ChartKeys.RISK_AT_VALUE_MAX.toString()))
 					entry.getValue().setValue(new StringValue(""));
 				else if (entry.getKey().equals(
 						ChartKeys.RISK_AT_VALUE_MIN.toString()))
 					entry.getValue().setValue(new StringValue(""));
 			}
 		} else {
 			IntervalValue interval = stochasticResult.valueAtRisk(confidence);
 
 			IBHAddValue comp = super.view.getBHchartComponents().get(
 					ChartKeys.DISTRIBUTION_CHART.toString());
 			comp.removeSeries(2);
 			comp.addSeries(
 					Services.getTranslator().translate(
 							ChartKeys.RISK_AT_VALUE.toString()),
 					new double[][] { { interval.getMin(), maxAmountofValues },
 							{ interval.getMax(), maxAmountofValues } });
 			for (Map.Entry<String, IBHModelComponent> entry : view
 					.getBHModelComponents().entrySet()) {
 				if (entry.getKey().equals(
 						ChartKeys.RISK_AT_VALUE_MAX.toString()))
 					entry.getValue().setValue(
 							new DoubleValue(interval.getMax()));
 				else if (entry.getKey().equals(
 						ChartKeys.RISK_AT_VALUE_MIN.toString()))
 					entry.getValue().setValue(
 							new DoubleValue(interval.getMin()));
 			}
 		}
 	}
 
 	class RiskAtValueListener implements CaretListener {
 
 		@Override
 		public void caretUpdate(CaretEvent e) {
 			if (((BHTextField) (view.getBHModelComponents()
 					.get(ChartKeys.RISK_AT_VALUE.toString()))).getValue() != null) {
 				double confidence = ((DoubleValue) ((BHTextField) (view
 						.getBHModelComponents().get(ChartKeys.RISK_AT_VALUE
 						.toString()))).getValue()).getValue();
 				calcRiskAtValue(confidence,
 						stochasticResult.getMaxAmountOfValuesInCluster());
 			} else
 				calcRiskAtValue(null,
 						stochasticResult.getMaxAmountOfValuesInCluster());
 		}
 	}
 
 	class SliderChangeListener implements ChangeListener {
 		boolean erstes_mal = true;// siehe F I X M E un
 
 		public void stateChanged(ChangeEvent e) {
 
 			String key = ((BHSlider) e.getSource()).getKey();
 			if (key.equals(ChartKeys.RISK_AT_VALUE.toString())) {
 				double confidence = ((BHSlider) view
 						.getBHComponent(ChartKeys.RISK_AT_VALUE.toString()))
 						.getValue();
 				calcRiskAtValue(confidence,
 						stochasticResult.getMaxAmountOfValuesInCluster());
 			}
 			if (key.equals(ChartKeys.CASHFLOW_COMPARE_SLIDER.toString())) {
 				int p = ((BHSlider) view
 						.getBHComponent(ChartKeys.CASHFLOW_COMPARE_SLIDER
 								.toString())).getValue();
 				if (!(pAlt == p)) {
 					pAlt = p;
 					calcNewComparison(p);
 				}
 			}
 		}
 
 	}
 
 	/* Specified by interface/super class. */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() instanceof BHButton) {
 			BHButton b = (BHButton) e.getSource();
 			if (b.getKey()
 					.toString()
 					.equals(BHStochasticResultController.PanelKeys.PRINTSCENARIO
 							.toString())) {
 				Map<String, IPrint> pPlugs = Services
 						.getPrintPlugins(IPrint.PRINT_SCENARIO_RES);
 
 				List<JFreeChart> charts = new ArrayList<JFreeChart>();
 				for (Entry<String, IBHAddValue> entry : view
 						.getBHchartComponents().entrySet()) {
 					if (entry.getValue() instanceof BHChartPanel) {
 						BHChartPanel cp = (BHChartPanel) entry.getValue();
 						charts.add(cp.getChart());
 					}
 				}
 				((IPrint) pPlugs.values().toArray()[0]).printScenarioResults(
 						scenario, stochasticResult, charts);
 
 			} else if (b
 					.getKey()
 					.toString()
 					.equals(BHStochasticResultController.PanelKeys.EXPORTSCENARIO
 							.toString())) {
 				BHDataExchangeDialog dialog = new BHDataExchangeDialog(null,
 						true);
 				dialog.setAction(IImportExport.EXP_SCENARIO_RES);
 				dialog.setModel(scenario);
 				dialog.setResults(stochasticResult);
 
 				dialog.setIconImages(Services.setIcon());
 
 				List<JFreeChart> charts = new ArrayList<JFreeChart>();
 				for (Entry<String, IBHAddValue> entry : view
 						.getBHchartComponents().entrySet()) {
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
