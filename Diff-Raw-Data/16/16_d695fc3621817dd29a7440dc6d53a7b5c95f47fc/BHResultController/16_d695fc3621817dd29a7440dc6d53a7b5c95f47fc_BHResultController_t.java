 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.bh.plugin.resultAnalysis;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
import javax.swing.BorderFactory;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
 
 import org.apache.log4j.Logger;
 import org.bh.controller.OutputController;
 import org.bh.data.DTOPeriod;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.gui.View;
 import org.bh.gui.ViewException;
 import org.bh.gui.chart.BHChartPanel;
 import org.bh.gui.chart.IBHAddGroupValue;
 import org.bh.gui.chart.IBHAddValue;
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHDataExchangeDialog;
 import org.bh.platform.IImportExport;
 import org.bh.platform.IPrint;
 import org.bh.platform.Services;
 import org.bh.platform.formula.FormulaException;
 import org.bh.platform.formula.IFormulaFactory;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.chart.JFreeChart;
 
 /**
  * controller definition for result plugin
  * @author Marco Hammel
  * @author Sebastian Scharfenberger
  * @version 1.0
  */
 public class BHResultController extends OutputController {
 
     protected static Logger log = Logger.getLogger(BHResultController.class);
     private static final ITranslator translator = Services.getTranslator();
     IFormulaFactory ff;
     /**
      * chart keys for result to chart mapping used in <code>BH_APV_ResultPanel>/code>
      * or <code>BH_FCF_ResultPanel>/code> or <code>BH_FTE_ResultPanel>/code>
      */
     public static enum ChartKeys {
 
 	APV_WF_SV, APV_BC_CS, FCF_WF_SV, FCF_BC_CS, FCF_BC_FCF, FCF_BC_RR, FTE_BC_SV, FTE_BC_CS, FTE_BC_FTE, FTE_BC_RR;
 
 	@Override
 	public String toString() {
 	    return getClass().getName() + "." + super.toString();
 	}
     }
 
     public static enum Keys {
 
 	FORMULABOX;
 
 	@Override
 	public String toString() {
 	    return getClass().getName() + "." + super.toString();
 	}
     }
 
     public BHResultController(View view, Map<String, Calculable[]> result, DTOScenario scenario) {
 	super(view, result, scenario);
     }
 
     @Override
     public void setResult(Map<String, Calculable[]> result, DTOScenario scenario) {
 	super.setResult(result, scenario);
 
 	BHResultPanel rp = (BHResultPanel) view.getViewPanel();
 
 	if (scenario.getDCFMethod().getUniqueId().equals("apv")) {
 
 	    rp.setChartArea(new BH_APV_ResultPanel());
 	    rp.setFormulaArea(initFormulaPanel(scenario));
 	    rp.getFormulaArea().setSelectedIndex(0);
 	    try {
 		view.setViewPanel(rp);
 	    } catch (ViewException e) {
 		log.error(e);
 	    }
 	} else if (scenario.getDCFMethod().getUniqueId().equals("fcf")) {
 
 	    rp.setChartArea(new BH_FCF_ResultPanel(false));
 	    rp.setFormulaArea(initFormulaPanel(scenario));
 	    rp.getFormulaArea().setSelectedIndex(0);
 	    try {
 		view.setViewPanel(rp);
 	    } catch (ViewException e) {
 		log.error(e);
 	    }
 	} else if (scenario.getDCFMethod().getUniqueId().equals("fte")) {
 
 	    rp.setChartArea(new BH_FTE_ResultPanel(false));
 	    rp.setFormulaArea(initFormulaPanel(scenario));
 	    rp.getFormulaArea().setSelectedIndex(0);
 	    try {
 		view.setViewPanel(rp);
 	    } catch (ViewException e) {
 		log.error(e);
 	    }
 	} else if (scenario.getDCFMethod().getUniqueId().equals("all")) {
 	    JPanel allPanel = new JPanel(new BorderLayout());
	    BH_APV_ResultPanel apv_panel = new BH_APV_ResultPanel();
	    apv_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),translator.translate("apv")));
	    BH_FCF_ResultPanel fcf_panel = new BH_FCF_ResultPanel(true);
	    fcf_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),translator.translate("fcf")));
	    BH_FTE_ResultPanel fte_panel = new BH_FTE_ResultPanel(true);
	    fte_panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),translator.translate("fte")));
	    allPanel.add(apv_panel, BorderLayout.NORTH);
	    allPanel.add(fcf_panel, BorderLayout.CENTER);
	    allPanel.add(fte_panel, BorderLayout.SOUTH);
 	    rp.setChartArea(allPanel);
 	    try {
 		view.setViewPanel(rp);
 	    } catch (ViewException e) {
 		log.error(e);
 	    }
 	} else {
 	}
 
 	if (scenario.isIntervalArithmetic()) {
 	    log.debug("generate charts for intervall input");
 
 	    for (String chartKey : super.view.getBHchartComponents().keySet()) {
 		// Fill APV waterfall chart capital structure
 		if (chartKey.equals(ChartKeys.APV_WF_SV.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.APV_WF_SV.toString());
 
 		    comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0].getMin(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")
 			    + " " + translator.translate("min"));
 		    comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0].getMax()
 			    - result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0].getMin(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")
 			    + " " + translator.translate("max"));
 		    comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0].getMin(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")
 			    + " " + translator.translate("min"));
 		    comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0].getMax()
 			    - result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0].getMin(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")
 			    + " " + translator.translate("max"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].getMin() * -1, translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")
 			    + " " + translator.translate("min"));
 		    comp.addValue((result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].getMax() - result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0]
 			    .getMin())
 			    * -1, translator.translate(ChartKeys.APV_WF_SV), translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " " + translator.translate("max"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].getMin() * -1, translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")
 			    + " " + translator.translate("min"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].getMax()
 			    - result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].getMin(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")
 			    + " " + translator.translate("max"));
 		} // Fill APV BarChart for capital structure
 		else if (chartKey.equals(ChartKeys.APV_BC_CS.toString())) {
 		    IBHAddGroupValue comp = (IBHAddGroupValue) super.view.getBHchartComponents().get(ChartKeys.APV_BC_CS.toString());
 		    comp.setDefaultGroupSettings(IBHAddGroupValue.MIN_MAX_GROUP);
 
 		    int length = result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF").length;
 		    for (int i = 0; i < length; i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[i].getMin(), translator
 				.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")
 				+ " " + translator.translate("min"), name, IBHAddGroupValue.MIN_POS);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].getMin(), translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")
 				+ " " + translator.translate("min"), name, IBHAddGroupValue.MIN_POS);
 			comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[i].getMax(), translator
 				.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")
 				+ " " + translator.translate("max"), name, IBHAddGroupValue.MAX_POS);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].getMax(), translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")
 				+ " " + translator.translate("max"), name, IBHAddGroupValue.MAX_POS);
 
 		    }
 		}// Fill FCF waterfall chart capital structure
 		else if (chartKey.equals(ChartKeys.FCF_WF_SV.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_WF_SV.toString());
 
 		    comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0].getMin(), translator.translate(ChartKeys.FCF_WF_SV), translator
 			    .translate("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")
 			    + " " + translator.translate("min"));
 		    comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0].getMax() - result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0].getMin(),
 			    translator.translate(ChartKeys.FCF_WF_SV), translator.translate("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL") + " " + translator.translate("max"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].getMin() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")
 			    + " " + translator.translate("min"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].getMax() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")
 			    + " " + translator.translate("max"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].getMin() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()),
 			    translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE") + " " + translator.translate("min"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].getMax(), translator.translate(ChartKeys.FCF_WF_SV.toString()), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")
 			    + " " + translator.translate("max"));
 		}// Fill FCF BarChart FreeCashFlow Distribution
 		else if (chartKey.equals(ChartKeys.FCF_BC_FCF.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_FCF.toString());
 		    if (result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0] != null) {
 			String name = scenario.getChildren().get(0).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0].getMin(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " "
 				+ translator.translate("min"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0].getMax(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " "
 				+ translator.translate("max"), name);
 		    }
 		    for (int i = 1; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[i].getMin(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " "
 				+ translator.translate("min"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[i].getMax(), translator.translate(ChartKeys.FCF_BC_FCF.toString()) + " "
 				+ translator.translate("max"), name);
 
 		    }
 		}// Fill FCF BarChart marge
 		else if (chartKey.equals(ChartKeys.FCF_BC_RR.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_RR.toString());
 		    for (int i = 0; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")[i].getMin() * 100, translator
 				.translate("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")
 				+ " " + translator.translate("min"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")[0].getMin() * 100, translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")
 				+ " " + translator.translate("min"), name);
 			comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")[i].getMax() * 100, translator
 				.translate("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")
 				+ " " + translator.translate("max"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")[0].getMax() * 100, translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")
 				+ " " + translator.translate("max"), name);
 		    }
 		}// Not necessary ct. Pohl
 		else if (chartKey.equals(ChartKeys.FTE_BC_SV.toString())) {
 		}// Fill FTE BarChart Flow To Equity
 		else if (chartKey.equals(ChartKeys.FTE_BC_FTE.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_FTE.toString());
 		    for (int i = 1; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")[i].getMin(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")
 				+ " " + translator.translate("min"), name);
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")[i].getMax(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")
 				+ " " + translator.translate("max"), name);
 		    }
 		} else if (chartKey.equals(ChartKeys.FTE_BC_RR.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_RR.toString());
 		    for (int i = 1; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE")[i].getMin() * 100, translator
 				.translate("org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE")
 				+ " " + translator.translate("min"), name);
 			comp.addValue(((Calculable) scenario.get(DTOScenario.Key.RFK)).getMin() * 100, translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " "
 				+ translator.translate("min"), name);
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE")[i].getMax() * 100, translator
 				.translate("org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE")
 				+ " " + translator.translate("max"), name);
 			comp.addValue(((Calculable) scenario.get(DTOScenario.Key.RFK)).getMax() * 100, translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT") + " "
 				+ translator.translate("max"), name);
 		    }
 		}
 	    }
 	} else {
 	    log.debug("generate charts for non intervall deterministic input");
 
 	    for (String chartKey : super.view.getBHchartComponents().keySet()) {
 		// Fill APV waterfall capital structure
 		if (chartKey.equals(ChartKeys.APV_WF_SV.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.APV_WF_SV.toString());
 		    comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[0].parse(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF"));
 		    comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD")[0].parse(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD"));
 		    comp.addValue((result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].parse().doubleValue() * -1), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].parse(), translator.translate(ChartKeys.APV_WF_SV), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE"));
 		}// Fill APV BarChart capital structure
 		else if (chartKey.equals(ChartKeys.APV_BC_CS.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.APV_BC_CS.toString());
 		    int length = scenario.getChildrenSize();
 		    for (int i = 0; i < length; i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF")[i].parse(), translator
 				.translate("org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].parse(), translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"), name);
 		    }
 		}// Fill FCF waterfall capital structure
 		else if (chartKey.equals(ChartKeys.FCF_WF_SV.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_WF_SV.toString());
 		    comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL")[0].parse(), translator.translate(ChartKeys.FCF_WF_SV), translator
 			    .translate("org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[0].parse().doubleValue() * -1, translator.translate(ChartKeys.FCF_WF_SV.toString()),
 			    translator.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"));
 		    comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE")[0].parse(), translator.translate(ChartKeys.FCF_WF_SV.toString()), translator
 			    .translate("org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE"));
 		}// Fill FCF BarChart FreeCashFlow
 		else if (chartKey.equals(ChartKeys.FCF_BC_FCF.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_FCF.toString());
 		    if (result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0] != null) {
 			String name = scenario.getChildren().get(0).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[0].parse(), translator.translate(ChartKeys.FCF_BC_FCF.toString()), name);
 		    }
 		    for (int i = 1; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW")[i].parse(), translator.translate(ChartKeys.FCF_BC_FCF.toString()), name);
 		    }
 		}// Fill FCF BarChart return
 		else if (chartKey.equals(ChartKeys.FCF_BC_RR.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FCF_BC_RR.toString());
 		    for (int i = 0; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF")[i].parse().doubleValue() * 100, translator
 				.translate("org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE")[0].parse().doubleValue() * 100, translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE"), name);
 		    }
 		}// Not necessary ct Pohl
 		else if (chartKey.equals(ChartKeys.FTE_BC_SV.toString())) {
 		}// Fill FTE BarChart FlowToEquity distribution
 		else if (chartKey.equals(ChartKeys.FTE_BC_FTE.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_FTE.toString());
 		    for (int i = 1; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY")[i].parse(), translator.translate("org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY"),
 				name);
 		    }
 		}// Fill BarChart capital structure -->not in
 		else if (chartKey.equals(ChartKeys.FTE_BC_CS.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_CS.toString());
 		    for (int i = 0; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD")[i].parse(), translator
 				.translate("org.bh.plugin.fte.FTECalculator$Result.PRESENT_VALUE_TAX_SHIELD"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].parse(), translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"), name);
 		    }
 		} else if (chartKey.equals(ChartKeys.FTE_BC_RR.toString())) {
 		    IBHAddValue comp = super.view.getBHchartComponents().get(ChartKeys.FTE_BC_RR.toString());
 		    for (int i = 1; i < scenario.getChildrenSize(); i++) {
 			String name = scenario.getChildren().get(i).get(DTOPeriod.Key.NAME).toString();
 			comp.addValue(result.get("org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE")[i].parse(), translator
 				.translate("org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE"), name);
 			comp.addValue(result.get("org.bh.calculation.IShareholderValueCalculator$Result.DEBT")[i].parse(), translator
 				.translate("org.bh.calculation.IShareholderValueCalculator$Result.DEBT"), name);
 		    }
 		}
 	    }
 	}
     }
 
     public static Map<String, Calculable> getFormulaMap(DTOScenario scenario, Map<String, Calculable[]> result, int t) {
 	log.debug("generate map for formular parser");
 	HashMap<String, Calculable> formulaMap = new HashMap<String, Calculable>();
 
 	if (t == scenario.getChildrenSize() - 1) {
 	    // General
 	    putFormulaValue(formulaMap, "FCFT", result, "org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW", t);
 	    putFormulaValue(formulaMap, "FKT", result, "org.bh.calculation.IShareholderValueCalculator$Result.DEBT", t);
 
 	    // APV
 	    putFormulaValue(formulaMap, "UWAPV,T", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
 
 	    // FCF
 	    putFormulaValue(formulaMap, "UWFCF,T", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
 	    putFormulaValue(formulaMap, "rvTEK", result, "org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF", t);
 
 	    // FTE
 	    putFormulaValue(formulaMap, "UWFTE,T", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
 	    putFormulaValue(formulaMap, "FTET", result, "org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY", t);
 	    putFormulaValue(formulaMap, "rvTEK", result, "org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE", t);
 	} else {
 
 	    // General
 	    putFormulaValue(formulaMap, "FCFt+1", result, "org.bh.calculation.IShareholderValueCalculator$Result.FREE_CASH_FLOW", t + 1);
 	    putFormulaValue(formulaMap, "FKt", result, "org.bh.calculation.IShareholderValueCalculator$Result.DEBT", t);
 
 	    // APV
 	    putFormulaValue(formulaMap, "UWAPV,t", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
 	    putFormulaValue(formulaMap, "Vu,t+1", result, "org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_FCF", t + 1);
 	    putFormulaValue(formulaMap, "Vs,t+1", result, "org.bh.plugin.apv.APVCalculator$Result.PRESENT_VALUE_TAX_SHIELD", t + 1);
 
 	    // FCF
 	    putFormulaValue(formulaMap, "UWFCF,t", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
 	    putFormulaValue(formulaMap, "GKt+1", result, "org.bh.plugin.fcf.FCFCalculator$Result.TOTAL_CAPITAL", t + 1);
 	    putFormulaValue(formulaMap, "rvt+1EK", result, "org.bh.plugin.fcf.FCFCalculator$Result.EQUITY_RETURN_RATE_FCF", t + 1);
 
 	    // FTE
 	    putFormulaValue(formulaMap, "UWFTE,t", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t);
 	    putFormulaValue(formulaMap, "UWFTE,t+1", result, "org.bh.calculation.IShareholderValueCalculator$Result.SHAREHOLDER_VALUE", t + 1);
 	    putFormulaValue(formulaMap, "FTEt+1", result, "org.bh.plugin.fte.FTECalculator$Result.FLOW_TO_EQUITY", t + 1);
 	    putFormulaValue(formulaMap, "rvt+1EK", result, "org.bh.plugin.fte.FTECalculator$Result.EQUITY_RETURN_RATE_FTE", t + 1);
 	}
 
 	// General
 	putFormulaValue(formulaMap, "s", result, "org.bh.calculation.IShareholderValueCalculator$Result.TAXES", 0);
 	putFormulaValue(formulaMap, "rFK", result, "org.bh.calculation.IShareholderValueCalculator$Result.DEBT_RETURN_RATE", 0);
 	putFormulaValue(formulaMap, "ruEK", result, "org.bh.calculation.IShareholderValueCalculator$Result.EQUITY_RETURN_RATE", 0);
 
 	return formulaMap;
     }
 
     protected static void putFormulaValue(Map<String, Calculable> formulaMap, String formulaKey, Map<String, Calculable[]> resultMap, String resultKey, int resultIndex) {
 	Calculable[] result = resultMap.get(resultKey);
 	if (result == null || resultIndex >= result.length) {
 	    return;
 	}
 
 	formulaMap.put(formulaKey, result[resultIndex]);
     }
 
     BHFormulaPanel initFormulaPanel(DTOScenario scenario) {
 	BHFormulaPanel fPanel;
 	fPanel = new BHFormulaPanel(this);
 	for (int i = 0; i < scenario.getChildrenSize(); i++) {
 	    fPanel.addEntry("T" + i);
 	}
 
 	return fPanel;
     }
 
     /* Specified by interface/super class. */
     @Override
     public void actionPerformed(ActionEvent e) {
 	try {
 	    // formula combobox changed its selected item
 	    if (e.getSource() instanceof JComboBox) {
 		JComboBox cb = (JComboBox) e.getSource();
 		int t = cb.getSelectedIndex();
 		log.debug("formula for period t" + t + "selected");
 		final BHResultPanel rp = (BHResultPanel) view.getViewPanel();
 		BHFormulaPanel fp = rp.getFormulaArea();
 		if (ff == null) {
 		    ff = IFormulaFactory.instance;
 		}
 
 		if (fp != null) {
 		    if (scenario.getDCFMethod().getUniqueId().equals("apv")) {
 			if (t == scenario.getChildrenSize() - 1) {
 			    fp.setFormula(ff.createFormula("apv_t", getClass().getResourceAsStream("APV_SHV_T.xml"), false));
 			    fp.setValues(getFormulaMap(scenario, result, t));
 			} else {
 			    fp.setFormula(ff.createFormula("apv_T", getClass().getResourceAsStream("APV_SHV_t1.xml"), false));
 			    fp.setValues(getFormulaMap(scenario, result, t));
 			}
 		    } else if (scenario.getDCFMethod().getUniqueId().equals("fcf")) {
 			if (t == scenario.getChildrenSize() - 1) {
 			    fp.setFormula(ff.createFormula("fcf_T", getClass().getResourceAsStream("FCF_SHV_T.xml"), false));
 			    fp.setValues(getFormulaMap(scenario, result, t));
 			} else {
 			    fp.setFormula(ff.createFormula("fcf_t", getClass().getResourceAsStream("FCF_SHV_t1.xml"), false));
 			    fp.setValues(getFormulaMap(scenario, result, t));
 			}
 		    } else if (scenario.getDCFMethod().getUniqueId().equals("fte")) {
 			if (t == scenario.getChildrenSize() - 1) {
 			    fp.setFormula(ff.createFormula("fte_T", getClass().getResourceAsStream("FTE_SHV_T.xml"), false));
 			    fp.setValues(getFormulaMap(scenario, result, t));
 			} else {
 			    fp.setFormula(ff.createFormula("fte_t", getClass().getResourceAsStream("FTE_SHV_t1.xml"), false));
 			    fp.setValues(getFormulaMap(scenario, result, t));
 			}
 		    }
 		    fp.revalidate();
 		}
 		// export print button pressed
 	    } else if (e.getSource() instanceof BHButton) {
 		BHButton b = (BHButton) e.getSource();
 		if (b.getKey().toString().equals(BHResultPanel.Keys.EXPORTSCENARIO.toString())) {
 
 		    BHDataExchangeDialog dialog = new BHDataExchangeDialog(null, true);
 		    dialog.setAction(IImportExport.EXP_SCENARIO_RES);
 		    dialog.setModel(scenario);
 		    dialog.setResults(result);
 
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
 
 		} else if (b.getKey().toString().equals(BHResultPanel.Keys.PRINTSCENARIO.toString())) {
 		    Map<String, IPrint> pPlugs = Services.getPrintPlugins(IPrint.PRINT_SCENARIO_RES);
 
 		    List<JFreeChart> charts = new ArrayList<JFreeChart>();
 		    for (Entry<String, IBHAddValue> entry : view.getBHchartComponents().entrySet()) {
 			if (entry.getValue() instanceof BHChartPanel) {
 			    BHChartPanel cp = (BHChartPanel) entry.getValue();
 			    charts.add(cp.getChart());
 			}
 		    }
 		    ((IPrint) pPlugs.values().toArray()[0]).printScenarioResults(scenario, result, charts);
 		}
 	    }
 	} catch (FormulaException fe) {
 	    log.error(fe);
 	}
     }
 }
