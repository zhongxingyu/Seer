 package org.bh.plugin.branchSpecificRepresentative.swing;
 
import java.awt.Color;
import java.util.ServiceLoader;
 import java.util.TreeMap;
 
 import javax.swing.JPanel;
 
import org.apache.log4j.Logger;
import org.bh.calculation.IBranchSpecificCalculator;
 import org.bh.calculation.ITimeSeriesProcess;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.DistributionMap;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.comp.BHDescriptionLabel;
 import org.bh.gui.swing.comp.BHProgressBar;
 import org.bh.gui.swing.comp.BHTextField;
import org.bh.platform.PluginManager;
 import org.bh.platform.i18n.BHTranslator;
 import org.bh.platform.i18n.ITranslator;
 
import com.ibm.icu.text.DecimalFormat;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * This class should provide GUI data for showing the popup and goodness.
  * 
  * <p>
  * This class should provide GUI data for showing a popup and providing data how
  * good the calculated values are.
  * 
  * @author Yannick Rödl
  * @version 0.1, 12.12.2011
  * 
  */
 public class BranchSpecificRepresentative implements ITimeSeriesProcess {
 
 	private enum Elements {
 		BRANCH_SPECIFIC_REPRESENTATIVE_GOODNESS, POPUP_GOODNESS_BRANCH_SPECIFIC_REPRESENTATIVE, NO_GOODNESS;
 
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	private DTOScenario scenario;
 
 	private boolean goodness_calculated;
 
 	private ITranslator translator = BHTranslator.getInstance();
 
 	private BHTextField tfBranchSpecGoodness;
 
 	@Override
 	public JPanel calculateParameters(boolean branchSpecific) {
 		JPanel result = new JPanel();
 
 		// create layout
 		String rowDef = "4px,p";
 		String colDef = "4px,right:pref,4px,60px:grow,8px:grow,right:pref,4px,max(35px;pref):grow,4px:grow";
 		FormLayout layout = new FormLayout(colDef, rowDef);
 		result.setLayout(layout);
 		layout.setColumnGroups(new int[][] { { 4, 8 } });
 		CellConstraints cons = new CellConstraints();
 
 		BHDescriptionLabel dlGoodnessDescription = new BHDescriptionLabel(
 				Elements.BRANCH_SPECIFIC_REPRESENTATIVE_GOODNESS);
 		dlGoodnessDescription.setToolTipText(translator.translate(
 				Elements.BRANCH_SPECIFIC_REPRESENTATIVE_GOODNESS,
 				ITranslator.LONG));
 
 		BHButton bShowDeviationAnalysis = new BHButton(
 				Elements.POPUP_GOODNESS_BRANCH_SPECIFIC_REPRESENTATIVE);
 		bShowDeviationAnalysis.setToolTipText(translator.translate(
 				Elements.POPUP_GOODNESS_BRANCH_SPECIFIC_REPRESENTATIVE,
 				ITranslator.LONG));
 		bShowDeviationAnalysis.addActionListener(new DeviationAnalysisListener(
 				scenario));
 
 		// add components to panel
 		result.add(dlGoodnessDescription, cons.xywh(2, 2, 1, 1));
 		result.add(getBranchSpecGoodness(), cons.xywh(4, 2, 1, 1));
 		result.add(bShowDeviationAnalysis, cons.xywh(6, 2, 1, 1));
 
 		return result;
 	}
 
 	/**
 	 * This method calculates the (best) goodness of the branch specific
 	 * representative and returns the value of it.
 	 * 
 	 * @return goodness of branch specific representative
 	 */
 	private BHTextField getBranchSpecGoodness() {
 		// create goodness field
 		if (tfBranchSpecGoodness == null) {
 			tfBranchSpecGoodness = new BHTextField(
 					Elements.BRANCH_SPECIFIC_REPRESENTATIVE_GOODNESS);
 		}
 		tfBranchSpecGoodness.setEditable(false);
 		tfBranchSpecGoodness.setToolTipText(translator.translate(
 				Elements.BRANCH_SPECIFIC_REPRESENTATIVE_GOODNESS,
 				ITranslator.LONG));
 
 		// TODO integrate goodness calculation here - use all the business data
 		// we have and
 		// calculate with all available variations
 		goodness_calculated = false;// to know whether we got a result.
 		double goodness = Double.MAX_VALUE; // we're searching minimum.
 
 		goodness_calculated = true;
 		goodness = this.scenario.getBsrCalculatorWithRating().getRating();
 		tfBranchSpecGoodness.setBackground(this.scenario
 				.getBsrCalculatorWithRating().getEvaluationOfRating());
 
 		// mindestens 1 Vorkommastelle, genau 2 Nachkommastellen
 		DecimalFormat format = new DecimalFormat("#0.00");
 		
 		if (goodness_calculated) {
 			tfBranchSpecGoodness.setText("" + (format.format(goodness)) + " %");
 		} else {
 			tfBranchSpecGoodness.setText("N/A");
 		}
 
 		return tfBranchSpecGoodness;
 	}
 
 	@Override
 	public String getGuiKey() {
 		return ITimeSeriesProcess.Key.BRANCH_SPECIFIC_REPRESENTATIVE.toString();
 	}
 
 	@Override
 	public String getUniqueId() {
 		return ITimeSeriesProcess.Key.BRANCH_SPECIFIC_REPRESENTATIVE.toString();
 	}
 
 	@Override
 	public ITimeSeriesProcess createNewInstance(DTOScenario scenario) {
 		BranchSpecificRepresentative instance = new BranchSpecificRepresentative();
 		instance.scenario = scenario;
 		return instance;
 	}
 
 	@Override
 	public void updateParameters() {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public TreeMap<Integer, Double>[] calculateCompare(int p) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void setProgressB(BHProgressBar bhComponent) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void setInterrupted() {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public DistributionMap calculate() {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 	@Override
 	public void setBranchSpecific(boolean branchSpecific) {
 		throw new UnsupportedOperationException(
 				"This method has not been implemented");
 	}
 
 }
