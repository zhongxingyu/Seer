 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * BasicIngredientViewController.java
  *
  * Created on Oct 24, 2012, 3:50:31 PM
  */
 package gui.ingredients.controllers;
 
 import database.*;
 import database.tables.BasicIngredient;
 import gui.EmptyPanelManager;
 import gui.MasterDetailViewController;
 import gui.ingredients.RecipeTabbedViewController;
 import gui.ingredients.delegates.AddBasicIngredientDelegate;
 import gui.ingredients.delegates.EditBasicIngredientDelegate;
 import gui.ingredients.dialogs.AddBasicIngredientDialog;
 import gui.ingredients.dialogs.EditBasicIngredientDialog;
 import gui.utilities.list.EditableListModel;
 import gui.utilities.list.ListModelFactory;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.DecimalFormat;
 import javax.swing.JMenu;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import tools.StringTools;
 
 /**
  *
  * @author Warkst
  */
 public class BasicIngredientViewController extends javax.swing.JPanel implements MasterDetailViewController<BasicIngredient>, AddBasicIngredientDelegate, EditBasicIngredientDelegate{
 
     private RecipeTabbedViewController delegate;
     
     /** Creates new form BasicIngredientViewController */
     public BasicIngredientViewController(RecipeTabbedViewController application) {
 	this.delegate = application;
 	
 	initComponents();
 	
 	listOutlet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	listOutlet.addListSelectionListener(new ListSelectionListener() {
 
 	    @Override
 	    public void valueChanged(ListSelectionEvent e) {
 		if (!e.getValueIsAdjusting()) {
 		    updateDetail((BasicIngredient)listOutlet.getSelectedValue());
 		}
 	    }
 	});
 	
 	// copy from the db
 	listOutlet.setModel(ListModelFactory.createBasicIngredientModel(Database.driver().getBasicIngredientsAlphabetically()));
 	listOutlet.setSelectedIndex(0);
 
 	/*
 	 * If there are no ingredients, hide the ugly right detail view
 	 */
 	if (Database.driver().getBasicIngredients().isEmpty()) {
 	    detail.remove(container);
 	    detail.add(EmptyPanelManager.instance(), BorderLayout.CENTER);
 	}
     }
     
     @Override
     public void updateDetail(BasicIngredient bi){
 	if(bi==null) {
 	    return;
 	}
 	
 	DecimalFormat threeFormatter = new DecimalFormat("0.000");
 	DecimalFormat twoFormatter = new DecimalFormat("0.00");
 	
 	supplierOutlet.setText(StringTools.capitalize(bi.getSupplier().getFirm()));
 	if(bi.getSupplier().isDeleted()){
 	    supplierOutlet.setForeground(Color.RED);
 	    supplierOutlet.setCursor(Cursor.getDefaultCursor());
 	    for (MouseListener mouseListener : supplierOutlet.getMouseListeners()) {
 		supplierOutlet.removeMouseListener(mouseListener);
 	    }
 	} else {
 	    supplierOutlet.setForeground(Color.BLUE);
 	    supplierOutlet.setCursor(new Cursor(Cursor.HAND_CURSOR));
 	    supplierOutlet.addMouseListener(new MouseAdapter() {
 		@Override
 		public void mouseReleased(MouseEvent e) {
 		    supplierOutletMouseReleased(e);
 		}
 		@Override
 		public void mouseEntered(MouseEvent e) {
 		    supplierOutletMouseEntered(e);
 		}
 		@Override
 		public void mouseExited(MouseEvent e) {
 		    supplierOutletMouseExited(e);
 		}
 	    });
 	}
 	
 	//€-sign?
 	nameOutlet.setText(StringTools.capitalize(bi.getName()));
 	brandOutlet.setText(StringTools.capitalize(bi.getBrand()));
 	packagingOutlet.setText(StringTools.capitalize(bi.getPackaging()));
 	pricePerUnitOutlet.setText(""+threeFormatter.format(bi.getPricePerUnit())+" euro / "+bi.getPackaging());
 	weightPerUnitOutlet.setText(""+threeFormatter.format(bi.getWeightPerUnit())+" kg / "+bi.getPackaging());
 	pricePerWeightOutlet.setText(""+threeFormatter.format(bi.getPricePerWeight())+" euro / kg");
 	lossPercentOutlet.setText(""+twoFormatter.format(bi.getLossPercent())+" %");
 	grossPriceOutlet.setText(""+threeFormatter.format(bi.getGrossPrice())+" euro / kg");
 	taxesOutlet.setText(""+twoFormatter.format(bi.getTaxes())+" %");
 	netPriceOutlet.setText(""+twoFormatter.format(bi.getNetPrice())+" euro / kg");
 	dateOutlet.setText(bi.getDate());
 	notesOutlet.setText(bi.getNotes());
     }
     
     @Override
     public JPanel view(){
 	return this;
     }
     
     private void supplierOutletMouseReleased(java.awt.event.MouseEvent evt) {                                             
 	BasicIngredient selectedIngredient = (BasicIngredient)listOutlet.getSelectedValue();
 	delegate.selectAndSwitchToSupplier(selectedIngredient.getSupplier());
     }                                            
 
     private void supplierOutletMouseEntered(java.awt.event.MouseEvent evt) {                                            
         supplierOutlet.setText("<html><u>"+supplierOutlet.getText()+"</u></html>");
     }                                           
 
     private void supplierOutletMouseExited(java.awt.event.MouseEvent evt) {                                           
         BasicIngredient bi = (BasicIngredient)listOutlet.getSelectedValue();
 	String firm = bi.getSupplier().getFirm();
 	supplierOutlet.setText(firm.substring(0,1).toUpperCase()+firm.substring(1).toLowerCase());
     }
     
     @Override
     public void addBasicIngredient(BasicIngredient select){
 	EditableListModel<BasicIngredient> dlm = (EditableListModel)listOutlet.getModel();
 	dlm.update();
 	if (dlm.getSize() == 1) {
 	    detail.removeAll();
 	    detail.add(container);
 	}
 	listOutlet.setSelectedValue(select, true);
 	updateDetail(select);
     }
     
     @Override
     public void editBasicIngredient(BasicIngredient n, BasicIngredient o){
 	EditableListModel<BasicIngredient> dlm = (EditableListModel)listOutlet.getModel();
 	dlm.edit(n, o);
 	listOutlet.setSelectedValue(n, true);
 	updateDetail(n);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         editMenu = new javax.swing.JMenu();
         addMenuItem = new javax.swing.JMenuItem();
         editMenuItem = new javax.swing.JMenuItem();
         jSplitPane1 = new javax.swing.JSplitPane();
         master = new javax.swing.JPanel();
         add = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         listOutlet = new javax.swing.JList();
         detail = new javax.swing.JPanel();
         container = new javax.swing.JPanel();
         fixedFields = new javax.swing.JPanel();
         jLabel1 = new javax.swing.JLabel();
         nameOutlet = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         brandOutlet = new javax.swing.JLabel();
         jLabel6 = new javax.swing.JLabel();
         packagingOutlet = new javax.swing.JLabel();
         jLabel3 = new javax.swing.JLabel();
         supplierOutlet = new javax.swing.JLabel();
         jLabel10 = new javax.swing.JLabel();
         pricePerUnitOutlet = new javax.swing.JLabel();
         jLabel8 = new javax.swing.JLabel();
         weightPerUnitOutlet = new javax.swing.JLabel();
         jLabel12 = new javax.swing.JLabel();
         pricePerWeightOutlet = new javax.swing.JLabel();
         jLabel14 = new javax.swing.JLabel();
         lossPercentOutlet = new javax.swing.JLabel();
         jLabel16 = new javax.swing.JLabel();
         grossPriceOutlet = new javax.swing.JLabel();
         jLabel18 = new javax.swing.JLabel();
         taxesOutlet = new javax.swing.JLabel();
         jLabel20 = new javax.swing.JLabel();
         netPriceOutlet = new javax.swing.JLabel();
         jLabel22 = new javax.swing.JLabel();
         dateOutlet = new javax.swing.JLabel();
         stretchableFields = new javax.swing.JPanel();
         jScrollPane2 = new javax.swing.JScrollPane();
         notesOutlet = new javax.swing.JTextArea();
         jPanel1 = new javax.swing.JPanel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
         edit = new javax.swing.JButton();
 
         editMenu.setText("Edit");
 
         addMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         addMenuItem.setText("Add");
         addMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addMenuItemActionPerformed(evt);
             }
         });
         editMenu.add(addMenuItem);
 
         editMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
         editMenuItem.setText("Edit");
         editMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editMenuItemActionPerformed(evt);
             }
         });
         editMenu.add(editMenuItem);
 
         setFocusable(false);
         setLayout(new java.awt.BorderLayout());
 
         jSplitPane1.setDividerLocation(200);
         jSplitPane1.setFocusable(false);
 
         master.setFocusable(false);
         master.setLayout(new java.awt.BorderLayout());
 
         add.setText("Toevoegen...");
         add.setFocusable(false);
         add.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addActionPerformed(evt);
             }
         });
         master.add(add, java.awt.BorderLayout.SOUTH);
 
         jScrollPane1.setFocusable(false);
 
         listOutlet.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         jScrollPane1.setViewportView(listOutlet);
 
         master.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         jSplitPane1.setLeftComponent(master);
 
         detail.setFocusable(false);
         detail.setLayout(new java.awt.BorderLayout());
 
         container.setLayout(new java.awt.BorderLayout());
 
         fixedFields.setFocusable(false);
         fixedFields.setLayout(new java.awt.GridLayout(12, 2));
 
         jLabel1.setText("Naam");
         jLabel1.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel1.setFocusable(false);
         fixedFields.add(jLabel1);
 
         nameOutlet.setText("<nameOutlet>");
         nameOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         nameOutlet.setFocusable(false);
         fixedFields.add(nameOutlet);
 
         jLabel2.setBackground(new java.awt.Color(239, 239, 239));
         jLabel2.setText("Merk");
         jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel2.setFocusable(false);
         jLabel2.setOpaque(true);
         fixedFields.add(jLabel2);
 
         brandOutlet.setBackground(new java.awt.Color(239, 239, 239));
         brandOutlet.setText("<brandOutlet>");
         brandOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         brandOutlet.setFocusable(false);
         brandOutlet.setOpaque(true);
         fixedFields.add(brandOutlet);
 
         jLabel6.setText("Verpakking");
         jLabel6.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel6.setFocusable(false);
         fixedFields.add(jLabel6);
 
         packagingOutlet.setText("<packagingOutlet>");
         packagingOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         packagingOutlet.setFocusable(false);
         fixedFields.add(packagingOutlet);
 
         jLabel3.setBackground(new java.awt.Color(239, 239, 239));
         jLabel3.setText("Leverancier");
         jLabel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel3.setFocusable(false);
         jLabel3.setOpaque(true);
         fixedFields.add(jLabel3);
 
         supplierOutlet.setBackground(new java.awt.Color(239, 239, 239));
         supplierOutlet.setForeground(new java.awt.Color(0, 0, 255));
         supplierOutlet.setText("<supplierOutlet>");
         supplierOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         supplierOutlet.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
         supplierOutlet.setFocusable(false);
         supplierOutlet.setOpaque(true);
         fixedFields.add(supplierOutlet);
 
         jLabel10.setText("Prijs per verpakking (BTW excl)");
         jLabel10.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel10.setFocusable(false);
         fixedFields.add(jLabel10);
 
         pricePerUnitOutlet.setText("<pricePerUnitOutlet>");
         pricePerUnitOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         pricePerUnitOutlet.setFocusable(false);
         fixedFields.add(pricePerUnitOutlet);
 
         jLabel8.setBackground(new java.awt.Color(239, 239, 239));
         jLabel8.setText("Gewicht per verpakking");
         jLabel8.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel8.setFocusable(false);
         jLabel8.setOpaque(true);
         fixedFields.add(jLabel8);
 
         weightPerUnitOutlet.setBackground(new java.awt.Color(239, 239, 239));
         weightPerUnitOutlet.setText("<weightPerUnitOutlet>");
         weightPerUnitOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         weightPerUnitOutlet.setFocusable(false);
         weightPerUnitOutlet.setOpaque(true);
         fixedFields.add(weightPerUnitOutlet);
 
         jLabel12.setText("Prijs per kg (BTW excl)");
         jLabel12.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel12.setFocusable(false);
         fixedFields.add(jLabel12);
 
         pricePerWeightOutlet.setText("<pricePerWeightOutlet>");
         pricePerWeightOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         pricePerWeightOutlet.setFocusable(false);
         fixedFields.add(pricePerWeightOutlet);
 
         jLabel14.setBackground(new java.awt.Color(239, 239, 239));
         jLabel14.setText("Verliespercentage");
         jLabel14.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel14.setFocusable(false);
         jLabel14.setOpaque(true);
         fixedFields.add(jLabel14);
 
         lossPercentOutlet.setBackground(new java.awt.Color(239, 239, 239));
         lossPercentOutlet.setText("<lossPercentOutlet>");
         lossPercentOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         lossPercentOutlet.setFocusable(false);
         lossPercentOutlet.setOpaque(true);
         fixedFields.add(lossPercentOutlet);
 
         jLabel16.setText("Totaalprijs (BTW excl)");
         jLabel16.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel16.setFocusable(false);
         fixedFields.add(jLabel16);
 
         grossPriceOutlet.setText("<grossPriceOutlet>");
         grossPriceOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         grossPriceOutlet.setFocusable(false);
         fixedFields.add(grossPriceOutlet);
 
         jLabel18.setBackground(new java.awt.Color(239, 239, 239));
         jLabel18.setText("BTW");
         jLabel18.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel18.setFocusable(false);
         jLabel18.setOpaque(true);
         fixedFields.add(jLabel18);
 
         taxesOutlet.setBackground(new java.awt.Color(239, 239, 239));
         taxesOutlet.setText("<taxesOutlet>");
         taxesOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         taxesOutlet.setFocusable(false);
         taxesOutlet.setOpaque(true);
         fixedFields.add(taxesOutlet);
 
         jLabel20.setText("Totaalprijs");
         jLabel20.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel20.setFocusable(false);
         fixedFields.add(jLabel20);
 
         netPriceOutlet.setText("<netPriceOutlet>");
         netPriceOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         netPriceOutlet.setFocusable(false);
         fixedFields.add(netPriceOutlet);
 
         jLabel22.setBackground(new java.awt.Color(239, 239, 239));
         jLabel22.setText("Datum");
         jLabel22.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel22.setFocusable(false);
         jLabel22.setOpaque(true);
         fixedFields.add(jLabel22);
 
         dateOutlet.setBackground(new java.awt.Color(239, 239, 239));
         dateOutlet.setText("<dateOutlet>");
         dateOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         dateOutlet.setFocusable(false);
         dateOutlet.setOpaque(true);
         fixedFields.add(dateOutlet);
 
         container.add(fixedFields, java.awt.BorderLayout.NORTH);
 
         stretchableFields.setFocusable(false);
         stretchableFields.setLayout(new java.awt.BorderLayout());
 
         jScrollPane2.setBorder(javax.swing.BorderFactory.createTitledBorder("Opmerking:"));
         jScrollPane2.setFocusable(false);
 
         notesOutlet.setColumns(20);
         notesOutlet.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
         notesOutlet.setRows(5);
         notesOutlet.setFocusable(false);
         notesOutlet.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 notesOutletKeyTyped(evt);
             }
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 notesOutletKeyPressed(evt);
             }
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 notesOutletKeyReleased(evt);
             }
         });
         jScrollPane2.setViewportView(notesOutlet);
 
         stretchableFields.add(jScrollPane2, java.awt.BorderLayout.CENTER);
 
         container.add(stretchableFields, java.awt.BorderLayout.CENTER);
 
         jPanel1.setFocusable(false);
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         filler1.setFocusable(false);
         jPanel1.add(filler1, java.awt.BorderLayout.CENTER);
 
         edit.setText("Wijzigen...");
         edit.setFocusable(false);
         edit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editActionPerformed(evt);
             }
         });
         jPanel1.add(edit, java.awt.BorderLayout.EAST);
 
         container.add(jPanel1, java.awt.BorderLayout.PAGE_END);
 
         detail.add(container, java.awt.BorderLayout.CENTER);
 
         jSplitPane1.setRightComponent(detail);
 
         add(jSplitPane1, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editActionPerformed
         new EditBasicIngredientDialog(null, true, this, (BasicIngredient)listOutlet.getSelectedValue()).setVisible(true);
     }//GEN-LAST:event_editActionPerformed
 
     private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
 	new AddBasicIngredientDialog(null, true, this).setVisible(true);
     }//GEN-LAST:event_addActionPerformed
 
     private void notesOutletKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_notesOutletKeyPressed
         notesKeyEvent(evt);
     }//GEN-LAST:event_notesOutletKeyPressed
 
     private void notesOutletKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_notesOutletKeyReleased
         notesKeyEvent(evt);
     }//GEN-LAST:event_notesOutletKeyReleased
 
     private void notesOutletKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_notesOutletKeyTyped
         notesKeyEvent(evt);
     }//GEN-LAST:event_notesOutletKeyTyped
 
     private void addMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenuItemActionPerformed
         addActionPerformed(null);
     }//GEN-LAST:event_addMenuItemActionPerformed
 
     private void editMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuItemActionPerformed
         editActionPerformed(null);
     }//GEN-LAST:event_editMenuItemActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton add;
     private javax.swing.JMenuItem addMenuItem;
     private javax.swing.JLabel brandOutlet;
     private javax.swing.JPanel container;
     private javax.swing.JLabel dateOutlet;
     private javax.swing.JPanel detail;
     private javax.swing.JButton edit;
     private javax.swing.JMenu editMenu;
     private javax.swing.JMenuItem editMenuItem;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JPanel fixedFields;
     private javax.swing.JLabel grossPriceOutlet;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel12;
     private javax.swing.JLabel jLabel14;
     private javax.swing.JLabel jLabel16;
     private javax.swing.JLabel jLabel18;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel20;
     private javax.swing.JLabel jLabel22;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JList listOutlet;
     private javax.swing.JLabel lossPercentOutlet;
     private javax.swing.JPanel master;
     private javax.swing.JLabel nameOutlet;
     private javax.swing.JLabel netPriceOutlet;
     private javax.swing.JTextArea notesOutlet;
     private javax.swing.JLabel packagingOutlet;
     private javax.swing.JLabel pricePerUnitOutlet;
     private javax.swing.JLabel pricePerWeightOutlet;
     private javax.swing.JPanel stretchableFields;
     private javax.swing.JLabel supplierOutlet;
     private javax.swing.JLabel taxesOutlet;
     private javax.swing.JLabel weightPerUnitOutlet;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void electFirstResponder() {
	((EditableListModel)listOutlet.getModel()).update();
 	listOutlet.requestFocus();
     }
     
     private void notesKeyEvent(KeyEvent evt){
 	if (!(evt.isControlDown() && evt.getKeyCode() == KeyEvent.VK_C)
                 && !(evt.getKeyCode() == KeyEvent.VK_F1)
                 && !(evt.getKeyCode() == KeyEvent.VK_F2)
                 && !(evt.getKeyCode() == KeyEvent.VK_F3)) {
             evt.consume();
         }
         if (evt.getKeyCode() == KeyEvent.VK_TAB) {
             evt.consume();
             listOutlet.requestFocus();
         }
     }
 
     @Override
     public JMenu menu() {
 	return editMenu;
     }
 }
