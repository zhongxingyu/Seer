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
 package org.bh.plugin.resultAnalysis;
 
 
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.util.Map;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.apache.log4j.Logger;
 import org.bh.data.DTOScenario;
 import org.bh.data.types.Calculable;
 import org.bh.gui.swing.comp.BHButton;
 import org.bh.gui.swing.forms.border.BHBorderFactory;
 import org.bh.gui.view.ViewException;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 
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
 @SuppressWarnings("serial")
 public final class BHResultPanel extends JPanel {
 
 	static final Logger log = Logger.getLogger(BHResultPanel.class);
 	
 	// export button
 	private BHButton exportButton;
 	GridBagConstraints d;
 	JLabel space;
 	// probably not necessary in a later version
 	JPanel procedurePanel = null;
 	// print Button
	private BHButton printButton;
 	DTOScenario scenario;
 	Map<String, Calculable[]> result;
 	ITranslator translator = Services.getTranslator();
 
 	BHFormulaPanel formulaArea;
         Component chartArea;
 	 
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
 
 		setLayout(new GridBagLayout());
 		d = new GridBagConstraints();
 		
 		JPanel exportArea = new JPanel(new GridBagLayout());
 		exportButton = new BHButton(Keys.EXPORTSCENARIO);
 		
 		d.fill = GridBagConstraints.HORIZONTAL;
 		d.gridx = 0;
 		d.gridy = 0;
 		d.insets = new Insets(10,10,10,0); //border top 30
 		//c.weightx = 1.0;
 		exportArea.add(exportButton, d);
 		
 		printButton = new BHButton(Keys.PRINTSCENARIO);
 		d.fill = GridBagConstraints.HORIZONTAL;
 		d.gridx = 1;
 		d.gridy = 0;
 		d.insets = new Insets(10,10,10,10); //border top 30
 		exportArea.add(printButton, d);
 		
 		
 		space = new JLabel();
 		d.gridx = 2;
 		d.gridy = 0;
 		d.weightx = 1.0;
 		exportArea.add(space, d);
 		
 		exportArea.setMaximumSize(new Dimension(200, 40));
 		d.gridx = 1;
 		d.gridy = 1;
 		add(exportArea, d);
 		//add(exportArea, "1,1");
 		//initialize();
 		space = new JLabel();
 		d.gridx = 2;
 		d.gridy = 0;
 		d.weightx = 1.0;
 		add(space, d);
 	}
 	
 	void setFormulaArea(BHFormulaPanel c) {
 		formulaArea = c;
 		formulaArea.setBorder(BHBorderFactory.getInstacnce().createTitledBorder(BHBorderFactory.getInstacnce()
 				.createEtchedBorder(),
 				"result_headline"));
 		d.gridx = 1;
 		d.gridy = 3;
 		add(formulaArea, d);
 	}
 	
 	void setChartArea(Component c) {
             if(this.chartArea != null){
                 remove(this.chartArea);
             }
             this.chartArea = c;
             d.gridx = 1;
     		d.gridy = 5;
             add(c, d);
 	}
 	
 	BHFormulaPanel getFormulaArea() {
 		return formulaArea;
 	}	
 }
