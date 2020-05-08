 /********************************************************************
 *
 * 	Copyright 2011 Brendan O'Fallon
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 ***********************************************************************/
 
 
 package gui.inputPanels;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import gui.document.ACGDocument;
 import gui.inputPanels.Configurator.InputConfigException;
 import gui.inputPanels.DoubleModifierElement.ModType;
 import gui.inputPanels.PopSizeModelElement.PopSizeModel;
 import gui.widgets.Style;
 import gui.widgets.Stylist;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 
 import org.w3c.dom.Element;
 
 /**
  * Represents a CoalescentModelElement graphically. Typically this is included in a DocMemberConfigPanel
  * @author brendano
  *
  */
 public class CoalescentView extends JPanel {
 
 	CoalescentModelElement coalModel;
 	
 	private JComboBox coalModelBox;
 	private String[] coalModels = new String[]{"Constant size", "Exponential growth"};
 	
 	private JComboBox recombModelBox;
 	private String[] recombModels = new String[]{"None", "Constant rate"};
 	
 	List<Element> params = new ArrayList<Element>();
 	List<Element> likelihoods = new ArrayList<Element>();;
 		
 	private JPanel popCenterPanel;
 	private JPanel growthRatePanel;
 	private DoubleParamView constPopView;
 	private DoubleParamView baseSizeView;
 	private DoubleParamView growthRateView;
 	private DoubleParamView recView;
 	
 	private JPanel popPanel;
 	private JPanel recPanel;
 	private JPanel recCenterPanel;
 	
 
 	private Stylist stylist = new Stylist();
 	
 	public CoalescentView(CoalescentModelElement coalModel) {
 		this.coalModel = coalModel;
 		stylist.addStyle(new Style() {
 			public void apply(JComponent comp) {
 				comp.setOpaque(false);
 				comp.setAlignmentX(Component.LEFT_ALIGNMENT);
 			}
 		});
 		
 		initComponents();
 	}
 	
 	public CoalescentView() {
 		this( new CoalescentModelElement() );
 	}
 	
 	/**
 	 * Called when the coalescent model box has a new item selected
 	 */
 	protected void updateCoalModelBox() {
 		if (coalModelBox.getSelectedIndex()==0) {
 			coalModel.getPopSizeModel().setModelType( PopSizeModel.Constant );
 			popCenterPanel.remove(growthRatePanel);
 			popCenterPanel.add(constPopView, BorderLayout.CENTER);
 			popCenterPanel.revalidate();
 			
 		}
 		if (coalModelBox.getSelectedIndex()==1) {
 			coalModel.getPopSizeModel().setModelType( PopSizeModel.ExpGrowth );
 			popCenterPanel.remove(constPopView);
 			popCenterPanel.add(growthRatePanel, BorderLayout.CENTER);
 			popCenterPanel.revalidate();
 		}
 		
 		popCenterPanel.repaint();
 	}
 
 	/**
 	 * Obtain the CoalescentModelElement backing this view
 	 * @return
 	 */
 	public CoalescentModelElement getModel() {
 		return coalModel;
 	}
 	
 	/**
 	 * Replace the model backing this view with the given model. This also re-constructs all UI
 	 * components from scratch
 	 * @param coalescentModel
 	 */
 	public void setCoalModel(CoalescentModelElement coalescentModel) {
 		this.coalModel = coalescentModel;
 		this.removeAll();
 		initComponents();
 		updateView();
 	}
 	
 	/**
 	 * Called when recombination selection has changed
 	 */
 	protected void updateRecombModelBox() {
 		if (recombModelBox.getSelectedIndex()==0) {
 			coalModel.setUseRecombination(false);
 			recCenterPanel.remove(recView);
 			recCenterPanel.revalidate();
 			repaint();
 		}
 		if (recombModelBox.getSelectedIndex()==1) {
 			coalModel.setUseRecombination(true);
 			recCenterPanel.add( recView, BorderLayout.CENTER);
 			recCenterPanel.revalidate();
 			repaint();
 		}
 	}
 
 	/**
 	 * Update view components to reflect changes in model
 	 */
 	public void updateView() {
 		if (coalModel.getPopSizeModel().getModelType() == PopSizeModel.Constant)
 			coalModelBox.setSelectedIndex(0);
 		if (coalModel.getPopSizeModel().getModelType() == PopSizeModel.ExpGrowth)
 			coalModelBox.setSelectedIndex(1);
 		
		if (coalModel.getRecombModel()==null)
			recombModelBox.setSelectedIndex(0);
		else {
 			recombModelBox.setSelectedIndex(1);
 		}
 		
 		constPopView.updateView();
 		baseSizeView.updateView();
 		growthRateView.updateView();
 		recView.updateView();
 		
 		repaint();
 	}
 	
 	public void readNodesFromDocument(ACGDocument doc) throws InputConfigException {
 		coalModel.readElements(doc);
 		updateView();
 	}
 	
 	
 	/**
 	 * Create GUI components
 	 */
 	private void initComponents() {
 		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
 		
 		stylist.applyStyle(this);
 		popPanel = new JPanel();
 		JPanel popTop = new JPanel();
 		stylist.applyStyle(popTop);
 		stylist.applyStyle(popPanel);
 		popPanel.setLayout(new BorderLayout());
 		popTop.add(new JLabel("Coalescent model:"));
 		coalModelBox = new JComboBox(coalModels);
 		coalModelBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				updateCoalModelBox();
 			}
 		});
 		popTop.add(coalModelBox);
 		popPanel.add(popTop, BorderLayout.NORTH);
 		this.add(popPanel);
 		
 		popCenterPanel = new JPanel();
 		popCenterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		stylist.applyStyle(popCenterPanel);
 		popPanel.add(popCenterPanel, BorderLayout.CENTER);
 		constPopView = new DoubleParamView("Population Size", coalModel.getPopSizeModel().getConstSizeModel());
 		constPopView.setPreferredSize(new Dimension(350, 36));
 		popCenterPanel.add( constPopView);
 		
 		baseSizeView = new DoubleParamView("Base Size", coalModel.getPopSizeModel().getBaseSizeModel());
 		baseSizeView.setPreferredSize(new Dimension(350, 36));
 		growthRateView = new DoubleParamView("Growth Rate", coalModel.getPopSizeModel().getGrowthRateModel());
 		growthRateView.setPreferredSize(new Dimension(350, 36));
 		
 		growthRatePanel = new JPanel();
 		growthRatePanel.setOpaque(false);
 		growthRatePanel.setLayout(new BoxLayout(growthRatePanel, BoxLayout.Y_AXIS));
 		growthRatePanel.add(baseSizeView);
 		growthRatePanel.add(growthRateView);
 		
 		
 		JSeparator sep = new JSeparator(JSeparator.VERTICAL);
 		this.add(sep);
 		
 		recPanel = new JPanel();
 		stylist.applyStyle(recPanel);
 		recPanel.setLayout(new BorderLayout());
 		JPanel recTop = new JPanel();
 		stylist.applyStyle(recTop);
 		
 		recCenterPanel = new JPanel();
 		stylist.applyStyle(recCenterPanel);
 		
 		recPanel.add(recTop, BorderLayout.NORTH);
 		recView = new DoubleParamView("Recombination rate", coalModel.getRecombModel().getModel());
 		stylist.applyStyle(recView);
 		recPanel.add(recCenterPanel, BorderLayout.CENTER);
 		
 		recTop.add(new JLabel("Recombination :"));
 		recombModelBox = new JComboBox(recombModels);
 		recombModelBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent e) {
 				updateRecombModelBox();
 			}
 		});
		recombModelBox.setSelectedIndex(1);
 		recTop.add(recombModelBox);
 		this.add(recPanel);
 	}
 
 	
 }
