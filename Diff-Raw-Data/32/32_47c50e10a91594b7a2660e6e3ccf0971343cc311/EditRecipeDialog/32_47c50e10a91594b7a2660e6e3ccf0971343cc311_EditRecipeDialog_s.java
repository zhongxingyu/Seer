 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui.ingredients.dialogs;
 
 import com.michaelbaranov.microba.calendar.DatePicker;
 import database.Database;
 import database.Ingredient;
 import database.Recipe;
 import gui.ingredients.controllers.ComboCoxCallback;
 import gui.ingredients.controllers.MasterDetailViewController;
 import gui.utilities.cell.CellEditorFactory;
 import gui.utilities.cell.CellRendererFactory;
 import gui.utilities.table.EditableTableModel;
 import gui.utilities.table.TableRowTransferHandler;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DecimalFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.ComponentInputMap;
 import javax.swing.DropMode;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.plaf.ActionMapUIResource;
 import javax.swing.table.TableCellEditor;
 import javax.swing.text.DateFormatter;
 import utilities.Utilities;
 
 /**
  *
  * @author Robin jr
  */
 public class EditRecipeDialog extends javax.swing.JDialog implements ComboCoxCallback{
 
     private final Recipe model;
     private final Recipe defaultModel;
     private final DatePicker dp;
     private final MasterDetailViewController delegate;
     /**
      * Creates new form EditRecipeDialog
      */
     public EditRecipeDialog(java.awt.Frame parent, boolean modal, Recipe model, MasterDetailViewController delegate) {
 	super(parent, modal);
 	initComponents();
 	
 	dp = new DatePicker(new Date(), new SimpleDateFormat("dd/MM/yyyy"));
 	jPanel5.add(dp);
 	
 	setTitle("Recept wijzigen");
 	setLocationRelativeTo(null);
 	
 	this.delegate = delegate;
 	this.model = model;
 	this.defaultModel = new Recipe(model);
 	
 	/*
 	 * Configure the ingredient list. Add a listener that when the table's data changes, the proper fields are updated.
 	 */
 	this.ingredientsOutlet.setRowHeight(ingredientsOutlet.getRowHeight()+Utilities.fontSize()-10);
 	this.ingredientsOutlet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	this.ingredientsOutlet.setModel(new EditableTableModel(model.getIngredients()));
 	this.ingredientsOutlet.getModel().addTableModelListener(new TableModelListener() {
 
 	    @Override
 	    public void tableChanged(TableModelEvent e) {
 		updateGrossWeightOutlet();
 		updatePricePerWeightOutlet();
 	    }
 	});
 	
 	List ingredients = new ArrayList();
 	ingredients.add("");
	ingredients.addAll(Database.driver().getIngredients().values());
 	@SuppressWarnings("LeakingThisInConstructor")
 	TableCellEditor ce = CellEditorFactory.createComboBoxEditor(ingredients, this);
 	this.ingredientsOutlet.setDefaultEditor(Ingredient.class, ce);
 	this.ingredientsOutlet.setDefaultEditor(Double.class, CellEditorFactory.createDoubleEditor());
 	this.ingredientsOutlet.setDefaultRenderer(Ingredient.class, CellRendererFactory.createCapitalizedStringCellRenderer());
 	this.ingredientsOutlet.setDefaultRenderer(Double.class, CellRendererFactory.createThreeDecimalDoubleCellRenderer());
 	
 	this.ingredientsOutlet.setDragEnabled(true);
 	this.ingredientsOutlet.setDropMode(DropMode.INSERT_ROWS);
 	this.ingredientsOutlet.setTransferHandler(new TableRowTransferHandler(this.ingredientsOutlet)); 
 	
 	loadModel();
 	
 	// add accelerator to "add" button
 	addAccelerator(addComponent, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK), new KeyAction() {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		addComponentActionPerformed(e);
 	    }
 	});
 	addAccelerator(removeComponent, KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), new KeyAction() {
 	    @Override
 	    public void actionPerformed(ActionEvent e) {
 		removeComponentActionPerformed(e);
 	    }
 	});
     }
     
     private void addAccelerator(JComponent comp, KeyStroke ks, Action a){
 	InputMap keyMap = new ComponentInputMap(comp);
 	keyMap.put(ks, "action");
 
 	ActionMap actionMap = new ActionMapUIResource();
 	actionMap.put("action", a);
 
 	SwingUtilities.replaceUIActionMap(comp, actionMap);
 	SwingUtilities.replaceUIInputMap(comp, JComponent.WHEN_IN_FOCUSED_WINDOW, keyMap);
     }
     
     @Override
     public void ingredientBoxCallback(){
 	ingredientsOutlet.editingStopped(null);
     }
     
     private void loadModel(){
 	nameOutlet.setText(model.getName());
 	netWeightOutlet.setText(""+model.getNetWeight());
 	
 	updateGrossWeightOutlet();
 	updatePricePerWeightOutlet();
 	updateNetWeightFormattedOutlet();
     }
     
     private void updateGrossWeightOutlet(){
 	grossWeightOutlet.setText(new DecimalFormat("0.000").format(model.getGrossWeight())+" kg");
     }
     
     private void updatePricePerWeightOutlet(){
 	pricePerWeightOutlet.setText(new DecimalFormat("0.000").format(model.getPricePerWeight())+" euro/kg");
     }
     
     private void updateNetWeightFormattedOutlet(){
 	try {
 	    double d = Double.parseDouble(netWeightOutlet.getText());
 	    model.setNetWeight(d);
 	    netWeightFormattedOutlet.setText(new DecimalFormat("0.00").format(d)+" kg");
 	    netWeightFormattedOutlet.setForeground(Color.black);
 	    netWeightOutlet.setForeground(Color.black);
 	} catch (Exception e){
 	    netWeightFormattedOutlet.setForeground(Color.red);
 	    netWeightFormattedOutlet.setText("???");
 	    netWeightOutlet.setForeground(Color.red);
 	}
     }
     
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jPanel5 = new javax.swing.JPanel();
         nameOutlet = new javax.swing.JTextField();
         jPanel7 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         preparationOutlet = new javax.swing.JTextArea();
         jPanel6 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         grossWeightOutlet = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         netWeightOutlet = new javax.swing.JTextField();
         netWeightFormattedOutlet = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         pricePerWeightOutlet = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
         jPanel2 = new javax.swing.JPanel();
         save = new javax.swing.JButton();
         cancel = new javax.swing.JButton();
         jPanel4 = new javax.swing.JPanel();
         jScrollPane4 = new javax.swing.JScrollPane();
         jPanel8 = new javax.swing.JPanel();
         jPanel9 = new javax.swing.JPanel();
         addComponent = new javax.swing.JButton();
         removeComponent = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setMinimumSize(new java.awt.Dimension(400, 500));
         setPreferredSize(new java.awt.Dimension(462, 500));
 
         jPanel5.setLayout(new java.awt.GridLayout(1, 2));
 
         nameOutlet.setText("<nameOutlet>");
         jPanel5.add(nameOutlet);
 
         getContentPane().add(jPanel5, java.awt.BorderLayout.NORTH);
 
         jPanel7.setPreferredSize(new java.awt.Dimension(176, 250));
         jPanel7.setLayout(new java.awt.BorderLayout());
 
         jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Bereiding:"));
 
         preparationOutlet.setColumns(20);
         preparationOutlet.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
         preparationOutlet.setRows(5);
         jScrollPane3.setViewportView(preparationOutlet);
 
         jPanel7.add(jScrollPane3, java.awt.BorderLayout.CENTER);
 
         jPanel6.setLayout(new java.awt.GridLayout(3, 2));
 
         jLabel2.setText("  Totaalgewicht ingrediënten");
         jPanel6.add(jLabel2);
 
         grossWeightOutlet.setText("<grossWeightOutlet>");
         jPanel6.add(grossWeightOutlet);
 
         jLabel4.setText("  Gewicht na bereiding");
         jPanel6.add(jLabel4);
 
         jPanel3.setLayout(new java.awt.GridLayout(1, 0));
 
         netWeightOutlet.setText("<netWeightOutlet>");
         netWeightOutlet.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 netWeightOutletKeyReleased(evt);
             }
         });
         jPanel3.add(netWeightOutlet);
 
         netWeightFormattedOutlet.setText("<netWeightFormattedOutlet>");
         jPanel3.add(netWeightFormattedOutlet);
 
         jPanel6.add(jPanel3);
 
         jLabel7.setText("  Kostprijs per kg (BTW excl)");
         jPanel6.add(jLabel7);
 
         pricePerWeightOutlet.setText("<pricePerWeightOutlet>");
         jPanel6.add(pricePerWeightOutlet);
 
         jPanel7.add(jPanel6, java.awt.BorderLayout.NORTH);
 
         jPanel1.setLayout(new java.awt.BorderLayout());
         jPanel1.add(filler1, java.awt.BorderLayout.CENTER);
 
         jPanel2.setLayout(new java.awt.GridLayout(1, 0));
 
         save.setText("Opslaan");
         save.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 saveActionPerformed(evt);
             }
         });
         jPanel2.add(save);
 
         cancel.setText("Terug");
         cancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelActionPerformed(evt);
             }
         });
         jPanel2.add(cancel);
 
         jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);
 
         jPanel7.add(jPanel1, java.awt.BorderLayout.SOUTH);
 
         getContentPane().add(jPanel7, java.awt.BorderLayout.SOUTH);
 
         jPanel4.setLayout(new java.awt.BorderLayout());
 
         jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Ingrediënten:"));
 
         ingredientsOutlet.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 153), 1, true));
         ingredientsOutlet.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null},
                 {null, null, null, null}
             },
             new String [] {
                 "Title 1", "Title 2", "Title 3", "Title 4"
             }
         ));
        ingredientsOutlet.setSurrendersFocusOnKeystroke(true);
         jScrollPane4.setViewportView(ingredientsOutlet);
 
         jPanel4.add(jScrollPane4, java.awt.BorderLayout.CENTER);
 
         jPanel8.setLayout(new java.awt.BorderLayout());
 
         jPanel9.setLayout(new java.awt.GridLayout(1, 0));
 
         addComponent.setText("Toevoegen");
         addComponent.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addComponentActionPerformed(evt);
             }
         });
         jPanel9.add(addComponent);
 
         removeComponent.setText("Verwijderen");
         removeComponent.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 removeComponentActionPerformed(evt);
             }
         });
         jPanel9.add(removeComponent);
 
         jPanel8.add(jPanel9, java.awt.BorderLayout.EAST);
 
         jPanel4.add(jPanel8, java.awt.BorderLayout.SOUTH);
 
         getContentPane().add(jPanel4, java.awt.BorderLayout.CENTER);
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
         // revert the model to the default (undo all changes)
 	model.copy(defaultModel);
 	this.dispose();
     }//GEN-LAST:event_cancelActionPerformed
 
     private void saveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveActionPerformed
 	try {
 	    model.setName(nameOutlet.getText());
 	    model.setDate(new DateFormatter(dp.getDateFormat()).valueToString(dp.getDate()));
 	    model.setNetWeight(Double.parseDouble(netWeightOutlet.getText()));
 	    model.setPreparation(preparationOutlet.getText());
 	    
 	    if(model.update()){
 		delegate.updateList();
 		this.dispose();
 	    } else {
 		JOptionPane.showMessageDialog(null, "Er is een fout opgetreden bij het opslaan van deze leverancier in de databank.", "Fout!", JOptionPane.ERROR_MESSAGE);
 	    }
 	} catch (ParseException ex) {
 	    System.err.println(ex.getMessage());
 	    JOptionPane.showMessageDialog(null, Utilities.incorrectFormMessage, "Fout!", JOptionPane.ERROR_MESSAGE);
 	}
     }//GEN-LAST:event_saveActionPerformed
 
     private void netWeightOutletKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_netWeightOutletKeyReleased
 	updateNetWeightFormattedOutlet();
 	updatePricePerWeightOutlet();
     }//GEN-LAST:event_netWeightOutletKeyReleased
 
     private void removeComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeComponentActionPerformed
         int selected = ingredientsOutlet.getSelectedRow();
 	if(ingredientsOutlet.getSelectedRow()>-1 && ingredientsOutlet.getSelectedRow() < ingredientsOutlet.getModel().getRowCount()){
 	    ((EditableTableModel)ingredientsOutlet.getModel()).removeRow(ingredientsOutlet.getSelectedRow());
 	    int row = Math.max(0, Math.min(selected, ingredientsOutlet.getModel().getRowCount()-1));
 	    if (ingredientsOutlet.getModel().getRowCount() > 0) {
 		ingredientsOutlet.getSelectionModel().setSelectionInterval(row, row);
 	    }	    
 	}
     }//GEN-LAST:event_removeComponentActionPerformed
 
     private void addComponentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addComponentActionPerformed
         ((EditableTableModel)ingredientsOutlet.getModel()).addRow();
 	int lastIndex = ingredientsOutlet.getModel().getRowCount()-1;
 	ingredientsOutlet.getSelectionModel().setSelectionInterval(lastIndex, lastIndex);
 	ingredientsOutlet.editCellAt(lastIndex, 0);
 	ingredientsOutlet.transferFocus();
     }//GEN-LAST:event_addComponentActionPerformed
 
     private abstract class KeyAction implements Action{
 
 	@Override
 	public Object getValue(String key) {
 	    return null;
 	}
 
 	@Override
 	public void putValue(String key, Object value) {
 	    //
 	}
 
 	@Override
 	public void setEnabled(boolean b) {
 	    //
 	}
 
 	@Override
 	public boolean isEnabled() {
 	    return true;
 	}
 
 	@Override
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 	    //
 	}
 
 	@Override
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 	    //
 	}
 
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton addComponent;
     private javax.swing.JButton cancel;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JLabel grossWeightOutlet;
     private final javax.swing.JTable ingredientsOutlet = new javax.swing.JTable();
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JTextField nameOutlet;
     private javax.swing.JLabel netWeightFormattedOutlet;
     private javax.swing.JTextField netWeightOutlet;
     private javax.swing.JTextArea preparationOutlet;
     private javax.swing.JLabel pricePerWeightOutlet;
     private javax.swing.JButton removeComponent;
     private javax.swing.JButton save;
     // End of variables declaration//GEN-END:variables
 }
