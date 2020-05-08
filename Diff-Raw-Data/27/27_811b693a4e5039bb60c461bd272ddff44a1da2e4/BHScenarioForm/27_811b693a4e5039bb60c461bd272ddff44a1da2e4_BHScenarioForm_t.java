 package org.bh.gui.swing;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.border.EtchedBorder;
 
 import org.apache.log4j.Logger;
 import org.bh.platform.PlatformKey;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * This class contains the form for head-data
  * 
  * @author Anton Kharitonov
  * @author Patrick Heinz
  * @version 0.4, 04.01.2010
  * 
  */
 @SuppressWarnings("serial")
 public class BHScenarioForm extends JPanel {
 	
 	public enum Type{
 		/**
 		 * 
 		 */
 		STOCHASTIC,
 		
 		/**
 		 * 
 		 */
 		DETERMINISTIC;
 	}
 	
 	public enum Key {
 		/**
 		 * 
 		 */
 		SCENARIO_HEADDATA,
 		
 		/**
 		 * 
 		 */
 		PROCESS_DATA,
 		
 		CANNOT_CALCULATE_HINT;
 
 		@Override
 		public String toString() {
 		    return getClass().getName() + "." + super.toString();
 		}
 	}
 	
 	private JPanel pscenario;
 	private JPanel pprocess;
 	private BHButton bcalculate;
 	private BHDescriptionLabel lCannotCalculateHint;
 	private JPanel topPanel;
 	private JPanel bottomPanel;
 
 
 	ITranslator translator = Services.getTranslator();
 
 	/**
 	 * Constructor
 	 */
 	public BHScenarioForm(BHScenarioForm.Type type, boolean isIntervalArithmethic) {
 		super(new FormLayout("4px,pref:grow,4px", "4px,fill:0px:grow,4px,pref,4px"));
 		this.initialize(type, isIntervalArithmethic);
 	}
 
 	/**
 	 * Initialize method.
 	 */
 	private void initialize(BHScenarioForm.Type type, boolean isIntervalArithmethic) {
 		
 		/*
 		 * Structure of ScenarioForm:
 		 * The scenario form consists of 2 areas: 
 		 * a big one top which shows all fields and a smaller one bottom
 		 * which contains A) the button to start calculations and B) a short Info-Bar 
 		 * with shortview of calculated UW with a button to start "slider view" (which holds
 		 * all analysis information).
 		 */
 		
 		topPanel = new JPanel();
 		bottomPanel = new JPanel();
 		
 		/*
 		 * build topPanel
 		 */
 		String colDef = "4px,pref:grow,4px";
 		String rowDef = "4px,p,14px,p,4px";
 		
 		FormLayout topLayout = new FormLayout(colDef, rowDef);
 		topPanel.setLayout(topLayout);
 
 		CellConstraints cons = new CellConstraints();
 		
 		setHeadPanel(isIntervalArithmethic);
 		topPanel.add(this.getPprocess(type), cons.xywh(2, 4, 1, 1));
 		JScrollPane scrollPane = new JScrollPane(topPanel);
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
 		
 		//add topPanel to ScenarioForm
 		this.add(scrollPane, cons.xywh(2, 2, 1, 1));
 
 		
 		
 		/*
 		 * build bottomPanel
 		 */
 		colDef = "4px,4px:grow,4px,right:pref,4px";
 		rowDef = "4px,p,4px";
 		
 		FormLayout bottomLayout = new FormLayout(colDef, rowDef);
 		bottomPanel.setLayout(bottomLayout);
 
 		bottomPanel.add(this.getCannotCalculateHint(), cons.xywh(2, 2, 1, 1));
 		bottomPanel.add(this.getBcalculate(), cons.xywh(4, 2, 1, 1));
 		
 		this.add(bottomPanel, cons.xywh(2, 4, 1, 1));
 		
 	}
 
 	
 	public void setHeadPanel(boolean isIntervalArithmethic) {
 		if (pscenario != null)
 			topPanel.remove(pscenario);
 		
 		if (!isIntervalArithmethic)
 			pscenario = new BHScenarioHeadForm();
 		else
 			pscenario = new BHScenarioHeadIntervalForm();
 		
 		//TODO add locale change handler
 		pscenario.setBorder(BorderFactory
 				.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),translator.translate(BHScenarioForm.Key.SCENARIO_HEADDATA)));
 		
 		CellConstraints cons = new CellConstraints();
 		topPanel.add(pscenario, cons.xywh(2, 2, 1, 1));
 		this.revalidate();
 	}
 
 	private JPanel getPprocess(BHScenarioForm.Type type) {
 		switch (type){
 		case STOCHASTIC:
 			pprocess = new BHStochasticProcessForm();
 			Logger.getLogger(BHScenarioForm.class).debug("--StochasticProcessForm is chosen");
 			break;
 			
 		case DETERMINISTIC:
 			pprocess = new BHDeterministicProcessForm();
 			Logger.getLogger(BHScenarioForm.class).debug("--DeterministicProcessForm is chosen");
 			break;
 			
 		default:
 			pprocess = new BHDeterministicProcessForm();
 		}
 		// TODO add locale change handler
 		pprocess.setBorder(BorderFactory
 				.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),translator.translate(BHScenarioForm.Key.PROCESS_DATA)));
 		return pprocess;
 	}
 	
 	public JPanel getProcessForm(){
 		return pprocess;
 	}
 
 	public BHButton getBcalculate() {
 		if (this.bcalculate == null) {
 			this.bcalculate = new BHButton(PlatformKey.CALCSHAREHOLDERVALUE);
 		}
 		return bcalculate;
 	}
 	
 	public BHDescriptionLabel getCannotCalculateHint() {
 		if (lCannotCalculateHint == null) {
 			lCannotCalculateHint = new BHDescriptionLabel(Key.CANNOT_CALCULATE_HINT);
 			lCannotCalculateHint.setVisible(false);
 		}
 		return lCannotCalculateHint;
 	}
 	
 
 	
 //	// TODO remove main later
 //	/**
 //	 * Test main method.
 //	 */
 //	@SuppressWarnings("deprecation")
 //	public static void main(String args[]) {
 //
 //		JFrame test = new JFrame("Test for ViewHeadData1");
 //		test.setContentPane(new BHScenarioHeadForm());
 //		test.addWindowListener(new WindowAdapter() {
 //			@Override
 //			public void windowClosing(WindowEvent e) {
 //				System.exit(0);
 //			}
 //		});
 //		test.pack();
 //		test.show();
 //	}
 }
