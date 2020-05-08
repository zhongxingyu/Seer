 /*******************************************************************************
  * Copyright 2011: Matthias Beste, Hannes Bischoff, Lisa Doerner, Victor Guettler, Markus Hattenbach, Tim Herzenstiel, Günter Hesse, Jochen Hülß, Daniel Krauth, Lukas Lochner, Mark Maltring, Sven Mayer, Benedikt Nees, Alexandre Pereira, Patrick Pfaff, Yannick Rödl, Denis Roster, Sebastian Schumacher, Norman Vogel, Simon Weber 
  *
  * Copyright 2010: Anna Aichinger, Damian Berle, Patrick Dahl, Lisa Engelmann, Patrick Groß, Irene Ihl, Timo Klein, Alena Lang, Miriam Leuthold, Lukas Maciolek, Patrick Maisel, Vito Masiello, Moritz Olf, Ruben Reichle, Alexander Rupp, Daniel Schäfer, Simon Waldraff, Matthias Wurdig, Andreas Wußler
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
 package org.bh.gui.swing.forms;
 
 import java.awt.Component;
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import org.bh.data.DTOScenario;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.comp.BHCheckBox;
 import org.bh.gui.swing.comp.BHComboBox;
 import org.bh.gui.swing.comp.BHDescriptionLabel;
 import org.bh.gui.swing.comp.BHSelectionList;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.bh.validation.VRListNotEmpty;
 import org.bh.validation.ValidationRule;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * This class contains the form for stochastic processes.
  * 
  * @author Anton Kharitonov
  * @author Patrick Heinz
  * @version 1.0, 22.01.2010
  * @update 23.12.2010 Timo Klein
  * @update 09.12.2011 Yannick Rödl
  * 
  */
 @SuppressWarnings("serial")
 public final class BHStochasticInputForm extends JPanel {
 
 	private BHDescriptionLabel lstochprocess;
 	private BHDescriptionLabel ldcfMethod;
 	//private BHDescriptionLabel lindustry;
 	private BHDescriptionLabel lrepresentative;
 	
 	private BHComboBox cbstochprocess;
 	private BHComboBox cbdcfMethod;
 	private BHComboBox cbrepresentative;
 	private BHCheckBox cbtimeSeriesProcess;
 	private BHCheckBox cbbranchSpecificRepresentative;
 	
 	private BHDescriptionLabel lStochasticKeys;
 	private BHSelectionList liStochasticKeys;
 	private BHDescriptionLabel lNoStochasticKeys;
 	
 	private BHButton bCalcParameters;
 	private BHButton bResetParameters;
 	
 	private Component pParameters;
 	private Component timeSeriesParameters;
 	private Component branchSpecificRepresentativeParameters;
 	
 	ITranslator translator = Services.getTranslator();
 	
 	public enum Key {
 		CALC_PARAMETERS,
 		RESET_PARAMETERS,
 		NO_STOCHASTIC_KEYS;
 
 		public String toString() {
 		    return getClass().getName() + "." + super.toString();
 		}
 	}
 
 	/**
 	 * Constructor.
 	 */
 	public BHStochasticInputForm() {
 		this.initialize();
 	}
 
 	/**
 	 * Initialize method.
 	 */
 	private void initialize() {
 
 		String colDef = "4px,p,4px,p,4px,p,4px,p,120px,p,4px,p,0px:grow,4px";
 		String rowDef = "4px,p,4px,p,4px,p,4px,80px,10px,p,4px,p,4px,p,4px,p,4px";
 		
 		FormLayout layout = new FormLayout(colDef, rowDef);
 		this.setLayout(layout);
 		CellConstraints cons = new CellConstraints();
 		this.add(this.getlDCFmethod(), cons.xywh(2, 2, 1, 1));
 		this.add(this.getcbDCFmethod(), cons.xywh(4, 2, 1, 1));
 		this.add(this.getlstochProcess(), cons.xywh(6, 2, 1, 1));
 		this.add(this.getcbstochProcess(), cons.xywh(8, 2, 1, 1));
 		this.add(this.getcbtimeSeriesProcess(), cons.xywh(10, 2, 1, 1));
 		this.add(this.getcbbranchSpecificRepresentative(), cons.xywh(12, 2, 2, 1));
 		
 		this.add(this.getlrepresentative(), cons.xywh(2, 4, 1, 1));
		this.add(this.getcbrepresentative(), cons.xywh(4, 4, 1, 1));
 		
 		this.add(this.getlStochasticKeysList(), cons.xywh(2, 6, 8, 1));
 		
 		
 		this.add(new JScrollPane(this.getliStochasticKeysList()), cons.xywh(2, 8, 12, 1));
 		this.add(this.getlNoStochasticKeys(), cons.xywh(2, 8, 10, 1));
 		
 		this.add(this.getbCalcParameters(), cons.xywh(2, 12, 12, 1));
 		this.add(this.getbResetParameters(), cons.xywh(2, 12, 12, 1));
 		this.getbResetParameters().setVisible(false);
 	}
 	
 	/**
 	 * Getter method for component lDCFchoise.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlDCFmethod() {
 		if (this.ldcfMethod == null)
 			this.ldcfMethod = new BHDescriptionLabel(DTOScenario.Key.DCF_METHOD);
 		return this.ldcfMethod;
 	}
 	
 	/*public BHDescriptionLabel getlindustry() {
 		if (this.lindustry == null)
 			this.lindustry = new BHDescriptionLabel(DTOScenario.Key.LINDUSTRY);
 		
 		//If branchSpecificRepresentative is selected, then we want to show this checkbox as well
 		if(this.getcbbranchSpecificRepresentative().isSelected()){
 			this.lindustry.setVisible(true);
 		} else {
 			this.lindustry.setVisible(false);
 		}
 		
 		return this.lindustry;
 	}
 	*/
 	
 	public BHDescriptionLabel getlrepresentative() {
 		if (this.lrepresentative == null)
 			this.lrepresentative = new BHDescriptionLabel(DTOScenario.Key.LREPRESENTATIVE);
 		
 		//If branchSpecificRepresentative is selected, then we want to show this checkbox as well
 		if(this.getcbbranchSpecificRepresentative().isSelected()){
 			this.lrepresentative.setVisible(true);
 		} else {
 			this.lrepresentative.setVisible(false);
 		}
 		
 		return this.lrepresentative;
 	}
 	
 	
 	public BHComboBox getcbDCFmethod() {
 		if (this.cbdcfMethod == null) {
 			this.cbdcfMethod = new BHComboBox(DTOScenario.Key.DCF_METHOD, new Object[]{DTOScenario.Key.TOOLTIP_APV});
 			
 		}
 		return this.cbdcfMethod;
 	}
 	
 	
 	public BHComboBox getcbstochProcess() {
 		if (this.cbstochprocess == null) {
 			this.cbstochprocess = new BHComboBox(DTOScenario.Key.STOCHASTIC_PROCESS);
 		}
 		return this.cbstochprocess;
 	}
 	
 	/*public BHComboBox getcbindustry() {
 		if (this.cbindustry == null) {
 			this.cbindustry = new BHComboBox(DTOScenario.Key.INDUSTRY);
 		}
 		
 		//If time branchSpecificRepresentative is selected, then we want to show this checkbox as well
 		if(this.getcbbranchSpecificRepresentative().isSelected()){
 			this.cbindustry.setVisible(true);
 		} else {
 			this.cbindustry.setVisible(false);
 		}
 		
 		return this.cbindustry;
 	}*/
 	
 	
 	public BHComboBox getcbrepresentative() {
 		if (this.cbrepresentative == null) {
 			this.cbrepresentative = new BHComboBox(DTOScenario.Key.REPRESENTATIVE);
 		}
 		
 		//If time branchSpecificRepresentative is selected, then we want to show this checkbox as well
 		if(this.getcbbranchSpecificRepresentative().isSelected()){
 			this.cbrepresentative.setVisible(true);
 		} else {
 			this.cbrepresentative.setVisible(false);
 		}
 		
 		this.cbrepresentative.setPreferredSize(new Dimension(450,25));
 		
 		return this.cbrepresentative;
 	}
 	
 	
 	public BHCheckBox getcbtimeSeriesProcess() {
 		if (this.cbtimeSeriesProcess == null) {
 			this.cbtimeSeriesProcess = new BHCheckBox(DTOScenario.Key.TIMESERIES_PROCESS);
 			this.cbtimeSeriesProcess.setVisible(false);
 			
 		}
 		return this.cbtimeSeriesProcess;
 	}
 	
 	/**
 	 * This method returns a checkbox for a branch specific representative.
 	 * The checkbox is only displayed, when the checkbox {@link cbtimeSeriesProcess }
 	 * is selected.
 	 * 
 	 * @return a checkbox whether you want to include the calculation with
 	 * a branch specific representative.
 	 */
 	public BHCheckBox getcbbranchSpecificRepresentative(){
 		if(this.cbbranchSpecificRepresentative == null){
 			this.cbbranchSpecificRepresentative = new BHCheckBox(DTOScenario.Key.BRANCH_SPECIFIC);
 		}
 		
 		//If time series analysis is selected, then we want to show this checkbox as well
 		if(this.getcbtimeSeriesProcess().isSelected()){
 			this.cbbranchSpecificRepresentative.setVisible(true);
 		} else {
 			this.cbbranchSpecificRepresentative.setVisible(false);
 		}
 		
 		return this.cbbranchSpecificRepresentative;
 	}
 	
 	/**
 	 * Getter method for component lstochProcess.
 	 * 
 	 * @return the initialized component
 	 */
 	public BHDescriptionLabel getlstochProcess() {
 
 		if (this.lstochprocess == null) {
 			this.lstochprocess = new BHDescriptionLabel(DTOScenario.Key.STOCHASTIC_PROCESS);
 		}
 
 		return this.lstochprocess;
 	}
 	
 	public BHDescriptionLabel getlStochasticKeysList() {
 		if (lStochasticKeys == null) {
 			lStochasticKeys = new BHDescriptionLabel(DTOScenario.Key.STOCHASTIC_KEYS);
 		}
 		return lStochasticKeys;
 	}
 	
 	public BHSelectionList getliStochasticKeysList() {
 		if (liStochasticKeys == null) {
 			liStochasticKeys = new BHSelectionList(DTOScenario.Key.STOCHASTIC_KEYS);
 			ValidationRule[] rules = { VRListNotEmpty.INSTANCE };
 			liStochasticKeys.setValidationRules(rules);
 			liStochasticKeys.setDefaultValue(false);
 		}
 		return liStochasticKeys;
 	}
 	
 	public BHDescriptionLabel getlNoStochasticKeys() {
 		if (lNoStochasticKeys == null) {
 			lNoStochasticKeys = new BHDescriptionLabel(Key.NO_STOCHASTIC_KEYS);
 			lNoStochasticKeys.setVisible(false);
 		}
 		return lNoStochasticKeys;
 	}
 	
 	public BHButton getbCalcParameters() {
 		if (bCalcParameters == null) {
 			bCalcParameters = new BHButton(Key.CALC_PARAMETERS);
 		}
 		return bCalcParameters;
 	}
 	
 	public BHButton getbResetParameters() {
 		if (bResetParameters == null) {
 			bResetParameters = new BHButton(Key.RESET_PARAMETERS);
 		}
 		return bResetParameters;
 	}
 	
 	public void setParametersPanel(Component component) {
 		removeParametersPanel();
 		pParameters = component;
 		CellConstraints cons = new CellConstraints();
 		add(pParameters, cons.xywh(2, 16, 10, 1));
 		revalidate();
 	}
 	
 	public void removeParametersPanel() {
 		if (pParameters != null) {
 			remove(pParameters);
 			revalidate();
 		}
 	}
 	
 	/**
 	 * This method sets some space on the UI for the branch specific representative.
 	 * Here for example the goodness and the button for the popup are displayed.
 	 * @param component containing the panel which should be displayed.
 	 */
 	public void setBranchSpecificRepresentativePanel(Component component){
 		removeBranchSpecificRepresentativePanel();
 		branchSpecificRepresentativeParameters = component;
 		CellConstraints cons = new CellConstraints();
 		add(branchSpecificRepresentativeParameters, cons.xywh(2, 14, 10, 1));
 		revalidate();
 	}
 	
 	/**
 	 * Removes the currently active panel for branch specific representative data
 	 */
 	public void removeBranchSpecificRepresentativePanel(){
 		if(this.branchSpecificRepresentativeParameters != null){
 			remove(this.branchSpecificRepresentativeParameters);
 			revalidate();
 		}
 	}
 	
 	
 	/**
 	 * Wird vom ScenarioController ausgefuehrt, wenn auf die Checkbox fuer die Zeitreihenanalyse geklickt wird.
 	 * Fuegt eine neue Komponte (Panel mit Textfeldern für Parameter) hinzu.
 	 * @param component
 	 */
 	public void setTimeSeriesParametersPanel(Component component){
 		removeTimeSeriesParametersPanel();
 		timeSeriesParameters = component;
 		CellConstraints cons = new CellConstraints();
 		add(timeSeriesParameters, cons.xywh(2, 10, 10, 1));
 		revalidate();
 	}
 	
 	/**
 	 * Entfernt das Panel das von setTimeSeriesParametersPanel() hinzugefügt wurde
 	 */
 	public void removeTimeSeriesParametersPanel(){
 		if(this.timeSeriesParameters != null){
 			remove(this.timeSeriesParameters);
 			revalidate();
 		}
 	}
 }
