 package ve.com.fml.view;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.AbstractTableModel;
 
 import ve.com.fml.model.datasource.GlobalData;
 import ve.com.fml.model.fuzzy.FuzzyInstances;
 import weka.filters.Filter;
 import weka.filters.unsupervised.attribute.Normalize;
 import weka.filters.unsupervised.attribute.ReplaceMissingValues;
 
 public class EditDataWindow extends JDialog {
 
 	private JScrollPane scrollPane;
 	private JTable table;
 	private AbstractTableModel abstractTableModel;
 	/**
 	 * Edit data pane
 	 */
 	private static final long serialVersionUID = 1L;
 	private javax.swing.JLabel attLabel;
 	private javax.swing.JComboBox<String> attributeList;
 	private HashMap<String, Integer> attributes;
 	private javax.swing.JButton delAttButton;
 	private javax.swing.JLabel delAttLabel;
 	private javax.swing.JButton delInstanceButton;
 	private javax.swing.JLabel delInstanceLabel;
 	private javax.swing.JLabel jLabel3;
 	private javax.swing.JPanel jPanel1;
 	private javax.swing.JSeparator jSeparator1;
 	private javax.swing.JSeparator jSeparator2;
 	private javax.swing.JSeparator jSeparator3;
 	private javax.swing.JButton normalizeButton;
 	private javax.swing.JButton replaceButton;
 	private javax.swing.JLabel replaceLabel;
 	private javax.swing.JButton undoButton;
 	private javax.swing.JLabel undoLabel;
 
 
 	public EditDataWindow(JFrame parent, boolean isModal){
 		super(parent, isModal);
 		GlobalData.getInstance().storeInstancesBackup();
 		initComponents();
 	}
 
 
 	private void initComponents() {
 
 		setTitle("Configuracin de datos");
 
 		jSeparator1 = new javax.swing.JSeparator();
 		normalizeButton = new javax.swing.JButton();
 		jLabel3 = new javax.swing.JLabel();
 		replaceLabel = new javax.swing.JLabel();
 		replaceButton = new javax.swing.JButton();
 		jSeparator2 = new javax.swing.JSeparator();
 		jPanel1 = new javax.swing.JPanel();
 		attLabel = new javax.swing.JLabel();
 		attributeList = new javax.swing.JComboBox<String>();
 		delAttButton = new javax.swing.JButton();
 		delAttLabel = new javax.swing.JLabel();
 		undoButton = new javax.swing.JButton();
 		undoLabel = new javax.swing.JLabel();
 		jSeparator3 = new javax.swing.JSeparator();
 		delInstanceButton = new javax.swing.JButton();
 		delInstanceLabel = new javax.swing.JLabel();
 
 		
 		refreshAttributeList();
 		
 		normalizeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/btn_Resultados.png"))); // NOI18N
 		normalizeButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		normalizeButton.setPreferredSize(new java.awt.Dimension(76, 50));
 		normalizeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Normalize normalize = new Normalize();
 				try {
 					normalize.setInputFormat(GlobalData.getInstance().getFuzzyInstances());
 					GlobalData.getInstance().setFuzzyInstances(new FuzzyInstances(Filter.useFilter(GlobalData.getInstance().getFuzzyInstances(), normalize)));
 					repaint();
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		jLabel3.setText("Normalizar conjunto");
 
 		replaceLabel.setText("Reemplazar valores ausentes");
 
 		replaceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/table_replace.png"))); // NOI18N
 		replaceButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		replaceButton.setPreferredSize(new java.awt.Dimension(76, 50));
 		replaceButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ReplaceMissingValues replace = new ReplaceMissingValues();
 				try {
 					replace.setInputFormat(GlobalData.getInstance().getFuzzyInstances());
 					GlobalData.getInstance().setFuzzyInstances(new FuzzyInstances(Filter.useFilter(GlobalData.getInstance().getFuzzyInstances(), replace)));
 					repaint();
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
 		jPanel1.setLayout(jPanel1Layout);
 		jPanel1Layout.setHorizontalGroup(
 				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGap(0, 0, Short.MAX_VALUE)
 				);
 		jPanel1Layout.setVerticalGroup(
 				jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGap(0, 305, Short.MAX_VALUE)
 				);
 
 		attLabel.setText("Atributos ");
 
 		delAttButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/delete.png"))); // NOI18N
 		delAttButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		delAttButton.setPreferredSize(new java.awt.Dimension(76, 50));
 		delAttButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(attributeList.getSelectedIndex() != 0){
 					int attrIndex = attributes.get(attributeList.getSelectedItem());
 					GlobalData.getInstance().getFuzzyInstances().deleteAttributeAt(attrIndex);
 					//initComponents();
 
 					abstractTableModel.fireTableStructureChanged();
 					//table.removeColumn(table.getColumn(attrIndex));
 					refreshAttributeList();
 					repaint();
 				}
 			}
 		});
 
 		delAttLabel.setText("Eliminar atributo seleccionado");
 
 		undoButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/undo.png"))); // NOI18N
 		undoButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		undoButton.setPreferredSize(new java.awt.Dimension(76, 50));
 		undoButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					GlobalData.getInstance().restoreInstancesBackup();
 					repaint();
 				} catch (Exception e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 			}
 		});
 
 		undoLabel.setText("Deshacer cambios");
 
 		delInstanceButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Images/delete.png"))); // NOI18N
 		delInstanceButton.setMinimumSize(new java.awt.Dimension(76, 50));
 		delInstanceButton.setPreferredSize(new java.awt.Dimension(76, 50));
 		delInstanceButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int[] rows = table.getSelectedRows();
 				if(rows.length > 0){
 					for (int i = rows.length-1; i >= 0; i--){
 						GlobalData.getInstance().getFuzzyInstances().delete(rows[i]);
 					}
 					//abstractTableModel.fireTableRowsDeleted(rows[0], rows[rows.length-1]);
 					repaint();
 				}
 
 			}
 		});
 		delInstanceLabel.setText("Eliminar registro seleccionado");
 
 		
 		//table.setBounds(100, 100, getWidth(), getHeight());
 		refreshTableModel();
 
 		
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addGap(61, 61, 61)
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
 								.addComponent(normalizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 								.addComponent(jLabel3))
 								.addGap(52, 52, 52)
 								.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
 										.addComponent(replaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addComponent(replaceLabel)
 										.addComponent(delInstanceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addComponent(delInstanceLabel))
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 55, Short.MAX_VALUE)
 										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
 												.addComponent(undoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
 												.addComponent(undoLabel))
 												.addGap(84, 84, 84))
 												.addGroup(layout.createSequentialGroup()
 														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																.addGroup(layout.createSequentialGroup()
 																		.addGap(10, 10, 10)
 																		.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																				.addComponent(jSeparator3)
 																				.addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING)
 																				.addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING)
 																				.addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
 																				.addGroup(layout.createSequentialGroup()
 																						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																								.addGroup(layout.createSequentialGroup()
 																										.addGap(57, 57, 57)
 																										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
 																												.addComponent(attLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 																												.addComponent(attributeList, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 254, javax.swing.GroupLayout.PREFERRED_SIZE)))
 																												.addGroup(javax.swing.GroupLayout.Alignment.CENTER, layout.createSequentialGroup()
 																														.addGap(374, 374, 374)
 																														.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 																																.addComponent(delAttLabel, javax.swing.GroupLayout.Alignment.CENTER)
 																																.addComponent(delAttButton, javax.swing.GroupLayout.Alignment.CENTER, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))
 																																.addGap(0, 0, Short.MAX_VALUE)))
 																																.addContainerGap())
 				);
 		layout.setVerticalGroup(
 				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 				.addGroup(layout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
 								.addComponent(delAttButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 								.addGroup(layout.createSequentialGroup()
 										.addComponent(attLabel)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 										.addComponent(attributeList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
 										.addGap(7, 7, 7)
 										.addComponent(delAttLabel)
 										.addGap(13, 13, 13)
 										.addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
 										.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
 												.addGroup(layout.createSequentialGroup()
 														.addComponent(replaceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 														.addGap(7, 7, 7)
 														.addComponent(replaceLabel))
 														.addGroup(layout.createSequentialGroup()
 																.addComponent(normalizeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 																.addGap(7, 7, 7)
 																.addComponent(jLabel3))
 																.addGroup(layout.createSequentialGroup()
 																		.addComponent(undoButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 																		.addGap(7, 7, 7)
 																		.addComponent(undoLabel)))
 																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
 																		.addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
 																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																		.addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
 																		.addGap(18, 18, 18)
 																		.addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 12, javax.swing.GroupLayout.PREFERRED_SIZE)
 																		.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
 																		.addComponent(delInstanceButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
 																		.addGap(7, 7, 7)
 																		.addComponent(delInstanceLabel)
 																		.addContainerGap(16, Short.MAX_VALUE))
 				);
 
 		pack();
 	}
 	
 	private void refreshAttributeList(){
 		attributeList.removeAllItems();
 		attributeList.addItem("Seleccione un atributo");
 		attributes = new HashMap<String, Integer>();
 		for(int i = 0; i < GlobalData.getInstance().getFuzzyInstances().numAttributes(); i++){
 			attributes.put(GlobalData.getInstance().getFuzzyInstances().attribute(i).name(), i);
 			attributeList.addItem(GlobalData.getInstance().getFuzzyInstances().attribute(i).name());
 		}
 	}
 	
 	private void refreshTableModel(){
 		abstractTableModel = new AbstractTableModel() {
 			/**
 			 * Table to show from FuzzyInstance (Singleton)
 			 */
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public Object getValueAt(int rowIndex, int columnIndex) {
 				//System.out.println(rowIndex+" "+columnIndex);
 				if (GlobalData.getInstance().getFuzzyInstances().attribute(columnIndex).isNominal()){
 					return ""+GlobalData.getInstance().getFuzzyInstances().attribute(columnIndex).value(
 							(int)GlobalData.getInstance().getFuzzyInstances().instance(rowIndex).value(columnIndex));
 				}
 				return ""+GlobalData.getInstance().getFuzzyInstances().instance(rowIndex).value(columnIndex);
 			}
 
 			@Override
 			public int getRowCount() {
 				return GlobalData.getInstance().getFuzzyInstances().numInstances();
 			}
 
 			@Override
 			public int getColumnCount() {
 				return GlobalData.getInstance().getFuzzyInstances().numAttributes();
 			}
 
 			@Override
 			public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
 				// TODO Revisar el tipo del atributo a editar
 				//GlobalData.getInstance().getFuzzyInstances().instance(rowIndex).setValue(columnIndex, Double.parseDouble(aValue.toString()));
 			}
 
 			@Override
 			public String getColumnName(int column) {
 				return GlobalData.getInstance().getFuzzyInstances().attribute(column).name();
 			}
 
 			@Override
 			public boolean isCellEditable(int rowIndex, int columnIndex) {
 				return true;
 			}
 
 
 		};
 		table = new JTable(abstractTableModel);
 		scrollPane = new JScrollPane(table);
 	}
 }
