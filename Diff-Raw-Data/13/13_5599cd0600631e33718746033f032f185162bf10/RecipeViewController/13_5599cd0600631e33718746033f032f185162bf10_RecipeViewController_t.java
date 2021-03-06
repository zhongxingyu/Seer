 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gui.ingredients.controllers;
 
 import database.Database;
 import database.extra.Component;
 import database.extra.Ingredient;
 import database.tables.Recipe;
 import gui.EmptyPanelManager;
 import gui.MasterDetailViewController;
 import gui.ingredients.delegates.AddRecipeDelegate;
 import gui.ingredients.delegates.EditRecipeDelegate;
 import gui.ingredients.dialogs.AddRecipeDialog;
 import gui.ingredients.dialogs.EditRecipeDialog;
 import gui.ingredients.dialogs.RecipePrintDialog;
 import gui.utilities.cell.CellRendererFactory;
 import gui.utilities.list.EditableListModel;
 import gui.utilities.list.ListModelFactory;
 import gui.utilities.table.StaticRecipeTableModel;
 import java.awt.BorderLayout;
 import java.awt.event.KeyEvent;
 import java.text.DecimalFormat;
 import javax.swing.JMenu;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import printer.PrintableRecipe;
 import tools.StringTools;
 import tools.Utilities;
 
 /**
  *
  * @author Warkst
  */
 public class RecipeViewController extends javax.swing.JPanel implements MasterDetailViewController<Recipe>, AddRecipeDelegate, EditRecipeDelegate{
     
     /**
      * Creates new form RecipeViewController
      */
     public RecipeViewController() {
 	initComponents();
 	
 	recipeListOutlet.setModel(ListModelFactory.createRecipeListModel(Database.driver().getRecipesAlphabetically()));
 	recipeListOutlet.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 	recipeListOutlet.addListSelectionListener(new ListSelectionListener() {
 
 	    @Override
 	    public void valueChanged(ListSelectionEvent e) {
 		if (!e.getValueIsAdjusting()) {
 		    updateDetail((Recipe)recipeListOutlet.getSelectedValue());
 		}
 	    }
 	});
 	
 	recipeListOutlet.setSelectedIndex(0);
 	
 	ingredientsOutlet.setRowHeight(ingredientsOutlet.getRowHeight()+Utilities.fontSize()-10);
 	
 	/*
 	 * If there are no ingredients, hide the ugly right detail view
 	 */
 	if (Database.driver().getRecipesAlphabetically().isEmpty()) {
 	    detail.remove(container);
 	    detail.add(EmptyPanelManager.instance(), BorderLayout.CENTER);
 	}
     }
     
     @Override
     public JPanel view(){
 	return this;
     }
     
     @Override
     public void updateDetail(Recipe r){
 	
 	nameOutlet.setText(StringTools.capitalize(r.getName()));
 	dateOutlet.setText(r.getDate());
 	
 	DecimalFormat threeFormatter = new DecimalFormat("0.000");
 	grossWeightOutlet.setText(threeFormatter.format(r.getGrossWeight())+" kg");
 	netWeightOutlet.setText(threeFormatter.format(r.getNetWeight())+" kg");
 	pricePerWeightOutlet.setText(threeFormatter.format(r.getPricePerWeight())+" euro / kg");
 	
 	preparationOutlet.setText(r.getPreparation());
 	
 	ingredientsOutlet.setDefaultRenderer(String.class, CellRendererFactory.createCapitalizedStringCellRenderer());
 	ingredientsOutlet.setDefaultRenderer(Double.class, CellRendererFactory.createTwoDecimalDoubleCellRenderer());
 	ingredientsOutlet.setDefaultRenderer(Ingredient.class, CellRendererFactory.createIngredientCellRenderer());
 	ingredientsOutlet.setDefaultRenderer(Component.class, CellRendererFactory.createThreeDecimalDoubleCellRenderer());
 	
 	ingredientsOutlet.setModel(new StaticRecipeTableModel(r.getComponents()));
 	
 //	ingredientListOutlet.setModel(ListModelFactory.createComponentListModel(r.getComponents()));
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         editMenu = new javax.swing.JMenu();
         addMenuItem = new javax.swing.JMenuItem();
         editMenuItem = new javax.swing.JMenuItem();
         printMenuItem = new javax.swing.JMenuItem();
         jSplitPane1 = new javax.swing.JSplitPane();
         master = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         recipeListOutlet = new javax.swing.JList();
         add = new javax.swing.JButton();
         detail = new javax.swing.JPanel();
         container = new javax.swing.JPanel();
         jPanel5 = new javax.swing.JPanel();
         nameOutlet = new javax.swing.JLabel();
         dateOutlet = new javax.swing.JLabel();
         jPanel7 = new javax.swing.JPanel();
         jScrollPane3 = new javax.swing.JScrollPane();
         preparationOutlet = new javax.swing.JTextArea();
         jPanel6 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         grossWeightOutlet = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         netWeightOutlet = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         pricePerWeightOutlet = new javax.swing.JLabel();
         jPanel1 = new javax.swing.JPanel();
         filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
         jPanel2 = new javax.swing.JPanel();
         print = new javax.swing.JButton();
         edit = new javax.swing.JButton();
         jScrollPane4 = new javax.swing.JScrollPane();
         ingredientsOutlet = new javax.swing.JTable();
 
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
 
         printMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
         printMenuItem.setText("Print");
         printMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 printMenuItemActionPerformed(evt);
             }
         });
         editMenu.add(printMenuItem);
 
         setFocusable(false);
         setLayout(new java.awt.BorderLayout());
 
         jSplitPane1.setDividerLocation(200);
         jSplitPane1.setFocusable(false);
 
         master.setFocusable(false);
         master.setLayout(new java.awt.BorderLayout());
 
         jScrollPane1.setFocusable(false);
 
         recipeListOutlet.setModel(new javax.swing.AbstractListModel() {
             String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
             public int getSize() { return strings.length; }
             public Object getElementAt(int i) { return strings[i]; }
         });
         jScrollPane1.setViewportView(recipeListOutlet);
 
         master.add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
         add.setText("Toevoegen...");
         add.setFocusable(false);
         add.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addActionPerformed(evt);
             }
         });
         master.add(add, java.awt.BorderLayout.SOUTH);
 
         jSplitPane1.setLeftComponent(master);
 
         detail.setFocusable(false);
         detail.setLayout(new java.awt.BorderLayout());
 
         container.setLayout(new java.awt.BorderLayout());
 
         jPanel5.setFocusable(false);
         jPanel5.setLayout(new java.awt.GridLayout(1, 2));
 
         nameOutlet.setText("<nameOutlet>");
         nameOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 0));
         nameOutlet.setFocusable(false);
         jPanel5.add(nameOutlet);
 
         dateOutlet.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
         dateOutlet.setText("<dateOutlet>");
         dateOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 0, 5, 0));
         dateOutlet.setFocusable(false);
         jPanel5.add(dateOutlet);
 
         container.add(jPanel5, java.awt.BorderLayout.NORTH);
 
         jPanel7.setFocusable(false);
         jPanel7.setPreferredSize(new java.awt.Dimension(176, 250));
         jPanel7.setLayout(new java.awt.BorderLayout());
 
         jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder("Bereiding:"));
         jScrollPane3.setFocusable(false);
 
         preparationOutlet.setColumns(20);
         preparationOutlet.setFont(new java.awt.Font("Consolas", 0, 13)); // NOI18N
         preparationOutlet.setRows(5);
         preparationOutlet.setFocusable(false);
         preparationOutlet.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 preparationOutletKeyTyped(evt);
             }
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 preparationOutletKeyPressed(evt);
             }
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 preparationOutletKeyReleased(evt);
             }
         });
         jScrollPane3.setViewportView(preparationOutlet);
 
         jPanel7.add(jScrollPane3, java.awt.BorderLayout.CENTER);
 
         jPanel6.setFocusable(false);
         jPanel6.setLayout(new java.awt.GridLayout(3, 2));
 
         jLabel2.setText("Totaalgewicht ingrediënten");
         jLabel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel2.setFocusable(false);
         jPanel6.add(jLabel2);
 
         grossWeightOutlet.setText("<grossWeightOutlet>");
         grossWeightOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         grossWeightOutlet.setFocusable(false);
         jPanel6.add(grossWeightOutlet);
 
         jLabel4.setBackground(new java.awt.Color(239, 239, 239));
         jLabel4.setText("Gewicht na bereiding");
         jLabel4.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel4.setFocusable(false);
         jLabel4.setOpaque(true);
         jPanel6.add(jLabel4);
 
         netWeightOutlet.setBackground(new java.awt.Color(239, 239, 239));
         netWeightOutlet.setText("<netWeightOutlet>");
         netWeightOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         netWeightOutlet.setFocusable(false);
         netWeightOutlet.setOpaque(true);
         jPanel6.add(netWeightOutlet);
 
         jLabel7.setText("Kostprijs per kg (BTW excl)");
         jLabel7.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 5, 3, 0));
         jLabel7.setFocusable(false);
         jPanel6.add(jLabel7);
 
         pricePerWeightOutlet.setText("<pricePerWeightOutlet>");
         pricePerWeightOutlet.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
         pricePerWeightOutlet.setFocusable(false);
         jPanel6.add(pricePerWeightOutlet);
 
         jPanel7.add(jPanel6, java.awt.BorderLayout.NORTH);
 
         jPanel1.setFocusable(false);
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         filler1.setFocusable(false);
         jPanel1.add(filler1, java.awt.BorderLayout.CENTER);
 
         jPanel2.setFocusable(false);
         jPanel2.setLayout(new java.awt.GridLayout(1, 2));
 
         print.setText("Afdrukken...");
         print.setFocusable(false);
         print.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 printActionPerformed(evt);
             }
         });
         jPanel2.add(print);
 
         edit.setText("Wijzigen...");
         edit.setFocusable(false);
         edit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 editActionPerformed(evt);
             }
         });
         jPanel2.add(edit);
 
         jPanel1.add(jPanel2, java.awt.BorderLayout.EAST);
 
         jPanel7.add(jPanel1, java.awt.BorderLayout.SOUTH);
 
         container.add(jPanel7, java.awt.BorderLayout.SOUTH);
 
         jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder("Ingrediënten:"));
         jScrollPane4.setFocusable(false);
 
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
         ingredientsOutlet.setFocusable(false);
         ingredientsOutlet.setRowSelectionAllowed(false);
         ingredientsOutlet.setSurrendersFocusOnKeystroke(true);
         jScrollPane4.setViewportView(ingredientsOutlet);
 
         container.add(jScrollPane4, java.awt.BorderLayout.CENTER);
 
         detail.add(container, java.awt.BorderLayout.CENTER);
 
         jSplitPane1.setRightComponent(detail);
 
         add(jSplitPane1, java.awt.BorderLayout.CENTER);
     }// </editor-fold>//GEN-END:initComponents
 
     private void addActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addActionPerformed
 	new AddRecipeDialog(null, true, this).setVisible(true);
     }//GEN-LAST:event_addActionPerformed
 
     private void editActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editActionPerformed
         new EditRecipeDialog(null, true, (Recipe)recipeListOutlet.getSelectedValue(), this).setVisible(true);
     }//GEN-LAST:event_editActionPerformed
 
     private void printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printActionPerformed
 //	new RecipePrintDialog(null, true, this, new PrintableRecipe((Recipe)recipeListOutlet.getSelectedValue())).setVisible(true);
 	RecipePrintDialog.getInstance().showDialog(new PrintableRecipe((Recipe)recipeListOutlet.getSelectedValue()));
     }//GEN-LAST:event_printActionPerformed
 
     private void preparationOutletKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_preparationOutletKeyPressed
         notesKeyEvent(evt);
     }//GEN-LAST:event_preparationOutletKeyPressed
 
     private void preparationOutletKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_preparationOutletKeyReleased
         notesKeyEvent(evt);
     }//GEN-LAST:event_preparationOutletKeyReleased
 
     private void preparationOutletKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_preparationOutletKeyTyped
         notesKeyEvent(evt);
     }//GEN-LAST:event_preparationOutletKeyTyped
 
     private void addMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMenuItemActionPerformed
         addActionPerformed(null);
     }//GEN-LAST:event_addMenuItemActionPerformed
 
     private void editMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuItemActionPerformed
         editActionPerformed(null);
     }//GEN-LAST:event_editMenuItemActionPerformed
 
     private void printMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printMenuItemActionPerformed
         printActionPerformed(null);
     }//GEN-LAST:event_printMenuItemActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton add;
     private javax.swing.JMenuItem addMenuItem;
     private javax.swing.JPanel container;
     private javax.swing.JLabel dateOutlet;
     private javax.swing.JPanel detail;
     private javax.swing.JButton edit;
     private javax.swing.JMenu editMenu;
     private javax.swing.JMenuItem editMenuItem;
     private javax.swing.Box.Filler filler1;
     private javax.swing.JLabel grossWeightOutlet;
     private javax.swing.JTable ingredientsOutlet;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSplitPane jSplitPane1;
     private javax.swing.JPanel master;
     private javax.swing.JLabel nameOutlet;
     private javax.swing.JLabel netWeightOutlet;
     private javax.swing.JTextArea preparationOutlet;
     private javax.swing.JLabel pricePerWeightOutlet;
     private javax.swing.JButton print;
     private javax.swing.JMenuItem printMenuItem;
     private javax.swing.JList recipeListOutlet;
     // End of variables declaration//GEN-END:variables
 
     @Override
     public void addRecipe(Recipe select) {
 	EditableListModel<Recipe> dlm = (EditableListModel)recipeListOutlet.getModel();
 	dlm.update();
 	if (dlm.getSize() == 1) {
 	    detail.removeAll();
 	    detail.add(container);
 	}
 	recipeListOutlet.setSelectedValue(select, true);
 	updateDetail(select);
     }
     
     @Override
     public void editRecipe(Recipe n, Recipe o){
 	EditableListModel<Recipe> dlm = (EditableListModel)recipeListOutlet.getModel();
 	dlm.edit(n, o);
 	recipeListOutlet.setSelectedValue(n, true);
 	updateDetail(n);
     }
 
     @Override
     public void electFirstResponder() {
	((EditableListModel)recipeListOutlet.getModel()).update();
 	recipeListOutlet.requestFocus();
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
             recipeListOutlet.requestFocus();
         }
     }
 
     @Override
     public JMenu menu() {
 	return editMenu;
     }
 }
