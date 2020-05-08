 package ve.com.fml.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Vector;
 
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.axis.ValueAxis;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.data.xy.XYDataset;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.RefineryUtilities;
 
 import ve.com.fml.model.datasource.GlobalData;
 import ve.com.fml.model.fuzzy.FuzzyVariable;
 import ve.com.fml.model.fuzzy.membership.FuzzyMembership;
 import ve.com.fml.model.fuzzy.membership.SingletonFuzzyMembership;
 import ve.com.fml.model.fuzzy.membership.TrapFuzzyMembership;
 import ve.com.fml.model.fuzzy.membership.TriangleFuzzyMembership;
 
 public class DefineFuzzySetsWindow extends JDialog {
 
 	private javax.swing.JComboBox<String> attributeList;
 	private javax.swing.JComboBox<String> fuzzySetsList;
 	private HashMap<String, Integer> numericAtts = GlobalData.getInstance().getFuzzyInstances().getNumericAttributes();
 
 	private javax.swing.JLabel jLabel1;
 	private javax.swing.JLabel jLabel2;
 	private javax.swing.JLabel jLabel3;
 	private javax.swing.JLabel jLabel4;
 	private javax.swing.JLabel jLabel5;
 	private javax.swing.JPanel jPanel1;
 	private javax.swing.JSeparator jSeparator1;
 	private javax.swing.JSeparator jSeparator2;
 	private javax.swing.JButton addFuzzySetButton;
 	private javax.swing.JButton deleteFuzzySetButton;
 	private javax.swing.JButton editFuzzySetButton;
 
 	private JFreeChart chart;
 	private ChartPanel chartPanel;
 	/**
 	 * Edit data pane
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public DefineFuzzySetsWindow(JFrame topFrame, boolean isModal) {
 		super(topFrame, isModal);
 		initComponents();
 	}
 
 	private static XYSeries createSeries(String name, FuzzyMembership fuzzyMembership){
 		XYSeries xySeries = new XYSeries(name);
 		if(fuzzyMembership instanceof TriangleFuzzyMembership){
 			xySeries.add(((TriangleFuzzyMembership)fuzzyMembership).getLowerBound(), 0D);
 			xySeries.add(((TriangleFuzzyMembership)fuzzyMembership).getTopTriangle(), 1D);
 			xySeries.add(((TriangleFuzzyMembership)fuzzyMembership).getUpperBound(), 0D);
 		}else if(fuzzyMembership instanceof TrapFuzzyMembership){
 			xySeries.add(((TrapFuzzyMembership)fuzzyMembership).getLowerBound(), 0D);
 			xySeries.add(((TrapFuzzyMembership)fuzzyMembership).getTopTrap1(), 1D);
 			xySeries.add(((TrapFuzzyMembership)fuzzyMembership).getTopTrap2(), 1D);
 			xySeries.add(((TrapFuzzyMembership)fuzzyMembership).getUpperBound(), 0D);
 		}else if(fuzzyMembership instanceof SingletonFuzzyMembership){
 			xySeries.add(((SingletonFuzzyMembership)fuzzyMembership).getX(), 1D);
 		}
 		return xySeries;
 
 	}
 
 	private static XYDataset createDataset(FuzzyVariable fuzzyVariable) {
 		XYSeriesCollection xyseriescollection = new XYSeriesCollection();
 		if(fuzzyVariable != null){
 			for (String name : fuzzyVariable.getFuzzySets().keySet()) {
 				xyseriescollection.addSeries(createSeries(name, fuzzyVariable.getFuzzySets().get(name)));
 			}
 		}
 		xyseriescollection.setIntervalWidth(0.0D);
 		return xyseriescollection;
 	}
 
 	private void refreshChart(Integer attrIndx){
		String atrName = GlobalData.getInstance().getFuzzyInstances().attribute(attrIndx).name();
 		System.out.println(GlobalData.getInstance().getFuzzyInstances().getMembership());
		XYDataset xydataset = createDataset(GlobalData.getInstance().getFuzzyInstances().getMembership().get(attrIndx));
 		chart = ChartFactory.createXYAreaChart("Conjuntos difusos para el atributo "+atrName,"Dominio (x)", "Funcin de pertenencia \u03BC(x)", xydataset, PlotOrientation.VERTICAL, true, true, false);
 		chart.setBackgroundPaint(Color.white);
 		XYPlot xyplot = (XYPlot) chart.getPlot();
 		xyplot.setBackgroundPaint(Color.lightGray);
 		xyplot.setForegroundAlpha(0.65F);
 		xyplot.setDomainGridlinePaint(Color.white);
 		xyplot.setRangeGridlinePaint(Color.white);
 		ValueAxis valueaxis = xyplot.getDomainAxis();
 		valueaxis.setTickMarkPaint(Color.black);
 		valueaxis.setLowerMargin(0.0D);
 		valueaxis.setUpperMargin(0.0D);
 		ValueAxis valueaxis1 = xyplot.getRangeAxis();
 		valueaxis1.setTickMarkPaint(Color.black);
 		
 		jPanel1.removeAll();
 		chartPanel = new ChartPanel(chart);
 		chartPanel.setPreferredSize(new Dimension(500, 270));
 		jPanel1.add(chartPanel);
 		//chartPanel.repaint();
 		jPanel1.repaint();
 		//repaint();
 		pack();
 	}
 
 	private void initComponents() {
 
 		jSeparator1 = new javax.swing.JSeparator();
 		addFuzzySetButton = new javax.swing.JButton();
 		jLabel3 = new javax.swing.JLabel();
 		jLabel4 = new javax.swing.JLabel();
 		deleteFuzzySetButton = new javax.swing.JButton();
 		jLabel5 = new javax.swing.JLabel();
 		editFuzzySetButton = new javax.swing.JButton();
 		jSeparator2 = new javax.swing.JSeparator();
 		jPanel1 = new javax.swing.JPanel();
 		jLabel1 = new javax.swing.JLabel();
 		attributeList = new JComboBox<String>();
 		jLabel2 = new javax.swing.JLabel();
 		fuzzySetsList = new JComboBox<String>();
 
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
 		addFuzzySetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/add.png"))); // NOI18N
 		addFuzzySetButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		addFuzzySetButton.setPreferredSize(new java.awt.Dimension(76, 50));
 
 		jLabel3.setText("Agregar Conjunto Difuso");
 
 		jLabel4.setText("Eliminar Conjunto Difuso");
 
 		deleteFuzzySetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/delete.png"))); // NOI18N
 		deleteFuzzySetButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		deleteFuzzySetButton.setPreferredSize(new java.awt.Dimension(76, 50));
 
 		jLabel5.setText("Editar Conjunto Difuso");
 
 		editFuzzySetButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/pencil.png"))); // NOI18N
 		editFuzzySetButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		editFuzzySetButton.setPreferredSize(new java.awt.Dimension(76, 50));
 
 		/*javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
 		jPanel1.setLayout(jPanel1Layout);
 		jPanel1Layout.setHorizontalGroup(
 				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGap(0, 0, Short.MAX_VALUE)
 				);
 		jPanel1Layout.setVerticalGroup(
 				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGap(0, 310, Short.MAX_VALUE)
 				);*/
 
 		jLabel1.setText("Atributos Difusificables");
 
 		//attributeList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
 		jLabel2.setText("Conjuntos Difusos");
 
 		//fuzzySetsList.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 								.addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 								.addComponent(jSeparator1)
 								.addComponent(jSeparator2)
 								.addGroup(layout.createSequentialGroup()
 										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
 												.addComponent(addFuzzySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addComponent(jLabel3))
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 98, Short.MAX_VALUE)
 												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
 														.addComponent(deleteFuzzySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 														.addComponent(jLabel4))
 														.addGap(113, 113, 113)
 														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
 																.addComponent(editFuzzySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 																.addComponent(jLabel5)))
 																.addGroup(layout.createSequentialGroup()
 																		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
 																				.addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
 																				.addComponent(attributeList, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
 																						.addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																						.addComponent(fuzzySetsList, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE))))
 																						.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addGap(16, 16, 16)
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 								.addGroup(layout.createSequentialGroup()
 										.addComponent(jLabel1)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(attributeList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
 										.addGroup(layout.createSequentialGroup()
 												.addComponent(jLabel2)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 												.addComponent(fuzzySetsList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 												.addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 														.addGroup(layout.createSequentialGroup()
 																.addComponent(addFuzzySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 																.addGap(7, 7, 7)
 																.addComponent(jLabel3))
 																.addGroup(layout.createSequentialGroup()
 																		.addComponent(editFuzzySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 																		.addGap(7, 7, 7)
 																		.addComponent(jLabel5))
 																		.addGroup(layout.createSequentialGroup()
 																				.addComponent(deleteFuzzySetButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 																				.addGap(7, 7, 7)
 																				.addComponent(jLabel4)))
 																				.addGap(18, 18, 18)
 																				.addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
 																				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																				.addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																				.addContainerGap())
 				);
 
 
 		numericAtts = GlobalData.getInstance().getFuzzyInstances().getNumericAttributes();
 		attributeList.addItem("Seleccione un atributo numrico...");
 		for (String numAttLabel : numericAtts.keySet()) {
 			attributeList.addItem(numAttLabel);
 		}
 
 		/** Control para seleccin de conjunto difuso */
 		fuzzySetsList.addItem("Seleccione un conjunto difuso...");
 
 
 		attributeList.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				refreshFuzzySetList();
 				repaint();
 			}
 		});
 
 		addFuzzySetButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(attributeList.getSelectedIndex() != 0){
 					AddFuzzySetWindow addFuzzySetWindow = new AddFuzzySetWindow(numericAtts.get(attributeList.getSelectedItem()),(JDialog)SwingUtilities.getWindowAncestor(addFuzzySetButton),true);
 					addFuzzySetWindow.pack();
 					RefineryUtilities.centerFrameOnScreen(addFuzzySetWindow);
 					addFuzzySetWindow.setVisible(true);
 					refreshFuzzySetList();
 					repaint();
 				}else{
 					//alert, tiene que seleccionar un atribruto
 				}
 			}
 		});
 
 		deleteFuzzySetButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				//TODO prompt para eliminar conjunto difuso sobre la variable seleccionada
 				if(fuzzySetsList.getSelectedIndex() == 0){
 					//error no se ha seleccionado conjunto difuso
 				}else{
 					//prompt de si est seguro
 					GlobalData.getInstance().getFuzzyInstances().removeFuzzySet(numericAtts.get(attributeList.getSelectedItem()), (String) fuzzySetsList.getSelectedItem());
 					refreshFuzzySetList();
 					repaint();
 				}
 			}
 		});
 
 		editFuzzySetButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				//TODO prompt para eliminar conjunto difuso sobre la variable seleccionada
 				if(fuzzySetsList.getSelectedIndex() == 0){
 					//error no se ha seleccionado conjunto difuso
 				}else{
 					AddFuzzySetWindow addFuzzySetWindow = new AddFuzzySetWindow(numericAtts.get(attributeList.getSelectedItem()),(String)fuzzySetsList.getSelectedItem(),(JDialog)SwingUtilities.getWindowAncestor(editFuzzySetButton),true);
 					addFuzzySetWindow.pack();
 					RefineryUtilities.centerFrameOnScreen(addFuzzySetWindow);
 					addFuzzySetWindow.setVisible(true);
 					refreshFuzzySetList();
 					repaint();
 				}
 			}
 		});
 		//refreshChart(attributeList.getSelectedIndex());
 		pack();
 	}
 
 	private void refreshFuzzySetList(){
 		fuzzySetsList.removeAllItems();
 		fuzzySetsList.addItem("Seleccione un conjunto difuso...");
 		if(attributeList.getSelectedIndex() != 0){
 			Vector<String> currentFS = GlobalData.getInstance().getFuzzyInstances().getFuzzySets(numericAtts.get(attributeList.getSelectedItem()));
 			for (String fuzzySetLabel : currentFS) {
 				fuzzySetsList.addItem(fuzzySetLabel);
 			}
			refreshChart(numericAtts.get(attributeList.getSelectedItem()));
 		}
 	}
 
 }
