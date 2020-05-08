 package org.bh.plugin.resultAnalysis;
 
 import info.clearthought.layout.TableLayout;
 import info.clearthought.layout.TableLayoutConstants;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.gui.ViewException;
 import org.bh.gui.swing.BHButton;
 import org.bh.gui.swing.BHDescriptionTextArea;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 import org.jfree.chart.ChartPanel;
 
 import com.jgoodies.forms.layout.CellConstraints;
 
 /**
  * 
  * Class to create one ResultPanel with two different Charts and Descriptions
  * 
  * <p>
  * This class creates a <code>JPanel</code> with the Results. There are two
  * different charts and the descriptions.
  * 
  * @author Lars.Zuckschwerdt
  * @version 0.1, 30.12.2009
  * 
  * @author Norman
  * @version 0.2, 10.01.2010
  * 
  * @author Marco Hammel
  * @version 0.3 11.01.2010
  *
  * @version 1.0
  */
 public final class BHResultPanel extends JPanel {
 
 	static final Logger log = Logger.getLogger(BHResultPanel.class);
 	private JPanel panel;
 	private ChartPanel lineChartLabel;
 	private ChartPanel pieChartLabel;
 	private ChartPanel barChartLabel;
 	private BHDescriptionTextArea pieChartTextArea;
 	//check Marco, Lars: Really necessary for every procedure?
 	private CellConstraints cons;
 	// formulas
 	private Component finiteFormula;
 	private Component infiniteFormula;
 	// export button
 	private BHButton exportButton;
 	// probably not necessary in a later version
 	JPanel procedurePanel = null;
 	// print Button
 	private BHButton printButton;
 	DTOScenario scenario;
 	Map<String, Calculable[]> result;
 	ITranslator translator = Services.getTranslator();
 
 	BHFormulaPanel formulaArea;
 	 
 	public enum Keys{
 		EXPORTSCENARIO,
 		PRINTSCENARIO;
 		
 		@Override
 		public String toString() {
 			return getClass().getName() + "." + super.toString();
 		}
 	}
 	
 	/**
 	 * Constructor
 	 * 
 	 * @throws ViewException
 	 */
 	public BHResultPanel() {
 		double border = 10;
 		double size[][] = {
				{ border, TableLayoutConstants.PREFERRED, border }, // Columns
 				{ border, TableLayoutConstants.PREFERRED, border,
 					      TableLayoutConstants.PREFERRED, border,
 						  TableLayoutConstants.PREFERRED, border } };
 		setLayout(new TableLayout(size));
 		
 		
 		double size2[][] = {
 			{ border, TableLayoutConstants.PREFERRED, border, TableLayoutConstants.PREFERRED, border }, // Columns
 			{ border, TableLayoutConstants.PREFERRED, border} };
 		JPanel exportArea = new JPanel(new TableLayout(size2));
 		exportButton = new BHButton(Keys.EXPORTSCENARIO);
 		exportArea.add(exportButton, "1,1" );
 		
 		printButton = new BHButton(Keys.PRINTSCENARIO);
 		exportArea.add(printButton, "3,1");
 		exportArea.setMaximumSize(new Dimension(200, 40));
 		
 		add(exportArea, "1,1");
 		//initialize();
 	}
 	
 	void setFormulaArea(BHFormulaPanel c) {
 		formulaArea = c;
 		formulaArea.setBorder(BorderFactory.createTitledBorder(BorderFactory
 				.createEtchedBorder(), translator
 				.translate("result_headline")));
 		add(formulaArea, "1,3");
 	}
 	
 	void setChartArea(Component c) {
 		add(c, "1,5");
 	}
 	
 	BHFormulaPanel getFormulaArea() {
 		return formulaArea;
 	}	
 }
