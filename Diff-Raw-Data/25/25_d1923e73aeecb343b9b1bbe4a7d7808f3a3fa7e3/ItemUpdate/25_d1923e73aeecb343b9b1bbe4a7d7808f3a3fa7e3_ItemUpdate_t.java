 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package inventory.itemPage;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Image;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.Date;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 /**
  *
  * @author Kind
  */
 public class ItemUpdate extends inventory.myClasses.MyJPanel {
     private String originalName = null;
     private Integer category_id = null;
     private Integer model_id = null;
     private Integer package_id = null;
     private Integer nation_id = null;
     private java.sql.Date expiredate = null;
     private Integer id = 0;
     private Integer originalCurrent = 0;
     
     private int imageWidth = 491;
     private int imageHeight = 328;
     
     private ArrayList<String> possibilities = null;
     private ArrayList<Integer> ids = null;
     
     private String imgSource = null;
     /**
      * Creates new form ItemUpdate
      */
     public void setElements(Integer id, String categoryName, String modelName, String nationName, String packageNumber){
         //set this values
         this.id = id;
         
         this.imageLabel.setIcon(null);
         
         if(id == 0){
             this.clearElements();
             this.updateButton.setText("Register");
         }else if(id > 0){
             this.updateButton.setText("Edit");
             try {
                 ResultSet rs = inventory.core.DBConnection.executeQuery("SELECT * FROM inventory.item WHERE id = "+id+";");
                 if(rs.next()){
                     originalName = rs.getString("name");
                     category_id = rs.getInt("category_id");
                     model_id = rs.getInt("model_id");
                     package_id = rs.getInt("package_id");
                     nation_id = rs.getInt("nation_id");
                     expiredate = rs.getDate("expiredate");
                     
                     possibilities = null;
                     
                     this.nameTextField.setText(originalName);
                     this.priceTextField.setText(rs.getFloat("price")+"");
                     this.descriptionTextArea.setText(rs.getString("description"));
                     this.originalCurrent = rs.getInt("current");
                     this.currentTextField.setText(originalCurrent+"");
                     this.expiredateTextField.setText(expiredate.toString());
                     
                     this.categoryTextField.setText(categoryName);
                     this.modelTextField.setText(modelName);
                     this.nationTextField.setText(nationName);
                     this.packageTextField.setText(packageNumber);
                 }else{
                     this.clearElements();
                     ((inventory.itemPage.ItemManage)inventory.core.ProjectBOMStockMain.getPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"))).loadDataByName("");
                     inventory.core.ProjectBOMStockMain.setPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"));
                     return;
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         this.imageLabel.setIcon(getItemImage(this.nameTextField.getText()));
         
         this.imageLabel.updateUI();
         this.imageLabel.repaint();
         this.repaint();
         this.updateUI();
     }
     
     public void clearElements(){
         originalName = null;
         category_id = null;
         model_id = null;
         package_id = null;
         nation_id = null;
         expiredate = null;
         id = 0;
         this.imgSource = null;
         
         possibilities = null;
         ids = null;
         
         this.categoryTextField.setText("");
         this.currentTextField.setText("");
         this.descriptionTextArea.setText("");
         this.expiredateTextField.setText("");
         this.modelTextField.setText("");
         this.nameTextField.setText("");
         this.nationTextField.setText("");
         this.packageTextField.setText("");
         this.priceTextField.setText("");
         this.imageLabel.setIcon(null);
     }
     
     public ItemUpdate() {
         super();
         Font ft = updateButton.getFont().deriveFont(Font.ITALIC,26.0f);
         this.updateButton.setForeground(Color.red);
         this.updateButton.setFont(ft);
     }
     
     protected ImageIcon getItemImage(String name){
         try {
             InputStream is = null;
             /*
             if(inventory.core.ProjectBOMStockMain.destIp != null && (!inventory.core.ProjectBOMStockMain.destIp.equals("localhost") && !inventory.core.ProjectBOMStockMain.destIp.split("\\.")[0].equals("192"))){
                 this.imageLabel.setIcon(null);
                 is = this.getClass().getResourceAsStream("itemImange/No_Image.jpg");
                 ImageIcon ii = new ImageIcon(ImageIO.read(is).getScaledInstance(this.imageWidth, this.imageHeight, Image.SCALE_DEFAULT));
                 is.close();
                 return ii;
             }*/
         
             String src = null;
             if(name.length()>1){
                 src = name.substring(0, name.length()-1);
             }
             
             this.imageWidth = 368;
             this.imageHeight = 459;
             
             String sql = "SELECT * FROM inventory.item_image WHERE name = '"+src+"';";
             
             ResultSet rs = inventory.core.DBConnection.executeQuery(sql);
             
             if(rs.next()){
                 this.imageLabel.setIcon(null);
                 is = rs.getBinaryStream("image");
                 ImageIcon ii = new ImageIcon(ImageIO.read(is).getScaledInstance(this.imageWidth, this.imageHeight, Image.SCALE_DEFAULT));
                 is.close();
                 return ii;
             }else{
                 this.imageLabel.setIcon(null);
                 is = this.getClass().getResourceAsStream("itemImange/No_Image.jpg");
                 ImageIcon ii = new ImageIcon(ImageIO.read(is).getScaledInstance(this.imageWidth, this.imageHeight, Image.SCALE_DEFAULT));
                 is.close();
                 return ii;
             }
             /*
             try {
                 this.imageLabel.setIcon(null);
                 is = this.getClass().getResourceAsStream(src);
                 ImageIcon ii = new ImageIcon(ImageIO.read(is).getScaledInstance(this.imageWidth, this.imageHeight, Image.SCALE_DEFAULT));
                 is.close();
                 return ii;
             } catch (Exception ex) {
                 try {
                     this.imageLabel.setIcon(null);
                     is = this.getClass().getResourceAsStream("itemImange/No_Image.jpg");
                     ImageIcon ii = new ImageIcon(ImageIO.read(is).getScaledInstance(this.imageWidth, this.imageHeight, Image.SCALE_DEFAULT));
                     is.close();
                     return ii;
                 } catch (IOException ex1) {
                     Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex1);
                 }
             }
             return null;
             */
         } catch (SQLException ex) {
             Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         nameLabel = new javax.swing.JLabel();
         categoryLabel = new javax.swing.JLabel();
         modelLabel = new javax.swing.JLabel();
         packageLabel = new javax.swing.JLabel();
         priceLabel = new javax.swing.JLabel();
         nationLabel = new javax.swing.JLabel();
         currentLabel = new javax.swing.JLabel();
         expiredateLabel = new javax.swing.JLabel();
         descriptionLabel = new javax.swing.JLabel();
         nameTextField = new inventory.myClasses.MyTextField();
         categoryTextField = new inventory.myClasses.MyTextField();
         modelTextField = new inventory.myClasses.MyTextField();
         packageTextField = new inventory.myClasses.MyTextField();
         priceTextField = new inventory.myClasses.MyTextField();
         nationTextField = new inventory.myClasses.MyTextField();
         currentTextField = new inventory.myClasses.MyTextField();
         expiredateTextField = new inventory.myClasses.MyTextField();
         descriptionScrollPane = new javax.swing.JScrollPane();
         descriptionTextArea = new inventory.myClasses.MyTextArea();
         backButton = new inventory.myClasses.MyButton();
         updateButton = new inventory.myClasses.MyButton();
         categoryButton = new inventory.myClasses.MyButton();
         modelButton = new inventory.myClasses.MyButton();
         packageButton = new inventory.myClasses.MyButton();
         nationButton = new inventory.myClasses.MyButton();
         expireDateButton = new inventory.myClasses.MyButton();
         imageLabel = new javax.swing.JLabel();
         imageEditButton = new inventory.myClasses.MyButton();
 
         nameLabel.setText("Name");
 
         categoryLabel.setText("Category");
 
         modelLabel.setText("Model");
 
         packageLabel.setText("Package");
 
         priceLabel.setText("Price");
 
         nationLabel.setText("Nation for Price");
 
         currentLabel.setText("Current Remain");
 
         expiredateLabel.setText("ExpireDate");
 
         descriptionLabel.setText("Description");
 
         categoryTextField.setEnabled(false);
 
         modelTextField.setEnabled(false);
 
         packageTextField.setEnabled(false);
 
         priceTextField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 priceTextFieldKeyTyped(evt);
             }
         });
 
         nationTextField.setEnabled(false);
 
         currentTextField.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 currentTextFieldKeyTyped(evt);
             }
         });
 
         expiredateTextField.setEnabled(false);
 
         descriptionTextArea.setColumns(20);
         descriptionTextArea.setRows(5);
         descriptionScrollPane.setViewportView(descriptionTextArea);
 
         backButton.setText("Back");
         backButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 backButtonActionPerformed(evt);
             }
         });
 
         updateButton.setText("Update");
         updateButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 updateButtonActionPerformed(evt);
             }
         });
 
         categoryButton.setText("CategorySet");
         categoryButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 categoryButtonActionPerformed(evt);
             }
         });
         categoryButton.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 categoryButtonKeyTyped(evt);
             }
         });
 
         modelButton.setText("ModelSet");
         modelButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 modelButtonActionPerformed(evt);
             }
         });
         modelButton.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 modelButtonKeyTyped(evt);
             }
         });
 
         packageButton.setText("Package Set");
         packageButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 packageButtonActionPerformed(evt);
             }
         });
         packageButton.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 packageButtonKeyTyped(evt);
             }
         });
 
         nationButton.setText("NationSet");
         nationButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 nationButtonActionPerformed(evt);
             }
         });
         nationButton.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 nationButtonKeyTyped(evt);
             }
         });
 
         expireDateButton.setText("ExpireDateSet");
         expireDateButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 expireDateButtonActionPerformed(evt);
             }
         });
         expireDateButton.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 expireDateButtonKeyTyped(evt);
             }
         });
 
         imageLabel.setIcon(null);
         imageLabel.setOpaque(true);
         imageLabel.setPreferredSize(new java.awt.Dimension(368, 328));
 
         imageEditButton.setText("Image");
         imageEditButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 imageEditButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(currentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(nationLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(priceLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(packageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(modelLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(categoryLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(nameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(expiredateLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(descriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(modelTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(packageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(priceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(nationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(currentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(descriptionScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(categoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(expiredateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(12, 12, 12)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(categoryButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(modelButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(packageButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(nationButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(expireDateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(imageEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(imageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 459, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addGroup(layout.createSequentialGroup()
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(nameLabel)
                                 .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(categoryLabel)
                                 .addComponent(categoryTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(categoryButton))
                             .addGap(8, 8, 8)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(modelLabel)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(modelTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(modelButton)))
                             .addGap(8, 8, 8)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(packageLabel)
                                 .addComponent(packageTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(packageButton))
                             .addGap(9, 9, 9)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(priceLabel)
                                 .addComponent(priceTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGap(9, 9, 9)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                 .addComponent(nationLabel)
                                 .addComponent(nationTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addComponent(nationButton))
                             .addGap(9, 9, 9)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(currentLabel)
                                 .addComponent(currentTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                             .addGap(9, 9, 9)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(expiredateLabel)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                     .addComponent(expiredateTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                     .addComponent(expireDateButton)))
                             .addGap(9, 9, 9)
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                 .addComponent(descriptionLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(descriptionScrollPane, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)))
                         .addGroup(layout.createSequentialGroup()
                             .addComponent(imageEditButton, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGap(124, 124, 124)
                             .addComponent(updateButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(backButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         imageLabel.getAccessibleContext().setAccessibleDescription("");
     }// </editor-fold>//GEN-END:initComponents
 
     private void backButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backButtonActionPerformed
         // TODO add your handling code here:
         ((inventory.itemPage.ItemManage)inventory.core.ProjectBOMStockMain.getPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"))).loadDataByName("");
         //inventory.core.ProjectBOMStockMain.setPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"));
         if(this.originalName != null && !this.originalName.trim().equals("")){
             ((inventory.itemPage.ItemManage)inventory.core.ProjectBOMStockMain.getPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"))).setSelectedListItem(originalName);
         }
         if(javax.swing.SwingUtilities.getWindowAncestor(this) !=null && javax.swing.SwingUtilities.getWindowAncestor(this) instanceof javax.swing.JFrame){
            ((inventory.core.ShowingFrame)javax.swing.SwingUtilities.getWindowAncestor(this)).dispose();
         }
         //this.clearElements();    
     }//GEN-LAST:event_backButtonActionPerformed
 
     private void currentTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_currentTextFieldKeyTyped
         // TODO add your handling code here:
         if(!Character.isDigit(evt.getKeyChar())){
             evt.consume();
         }
     }//GEN-LAST:event_currentTextFieldKeyTyped
 
     private void priceTextFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_priceTextFieldKeyTyped
         // TODO add your handling code here:
         if(!Character.isDigit(evt.getKeyChar())&& evt.getKeyChar() != '.'){
             evt.consume();
         }
     }//GEN-LAST:event_priceTextFieldKeyTyped
 
     private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateButtonActionPerformed
         // TODO add your handling code here:
         if(id == 0){
             try {
                 if(!register()){
                     JOptionPane.showMessageDialog(this, "Register Fail","Warning",JOptionPane.OK_OPTION);
                     return;
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }else{
             try {
                 if(!edit()){
                     JOptionPane.showMessageDialog(this, "Edit Fail","Warning",JOptionPane.OK_OPTION);
                     return;
                 }
             } catch (SQLException ex) {
                 Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         
         if(this.originalCurrent != Integer.parseInt(this.currentTextField.getText())){
             if(this.id != 0){
                 String sql = "INSERT INTO `inventory`.`change` (`item_id`, `amount`, `changetype_id`, `editor_id`, `date`) VALUES ('"+this.id+"', '"+this.currentTextField.getText()+"', '3', '"+inventory.core.MainFrame.user_id+"', now());";
                 inventory.core.DBConnection.updateQuery(sql);
             }
         }
         
         if(this.imgSource != null){
             updateItemImage();
         }
         
         /*
         String src = "inventory/itemPage/itemImange/"+nameTextField.getText().substring(0, nameTextField.getText().length()-1)+".jpg";
         System.out.println(src);
         
         String dest = "";
         //dest = this.getClass().getResource("/").getPath();
         //System.out.println(dest);
         
         if(this.imgSource != null){
             inventory.core.ProjectBOMStockMain.itemImagefileCopy(imgSource, dest+src);
         }
         */
         //if(JOptionPane.showConfirmDialog(this, "Save was done!, Continue to Update?","Confirm",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.CANCEL_OPTION){
             ((inventory.itemPage.ItemManage)inventory.core.ProjectBOMStockMain.getPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"))).loadDataByName("");
             //inventory.core.ProjectBOMStockMain.setPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"));
             this.originalName = this.nameTextField.getText();
             ((inventory.itemPage.ItemManage)inventory.core.ProjectBOMStockMain.getPage(inventory.core.ProjectBOMStockMain.PageList.indexOf("ItemManage"))).setSelectedListItem(this.originalName);
         //}
         if(javax.swing.SwingUtilities.getWindowAncestor(this) !=null && javax.swing.SwingUtilities.getWindowAncestor(this) instanceof javax.swing.JFrame){
            ((inventory.core.ShowingFrame)javax.swing.SwingUtilities.getWindowAncestor(this)).dispose();
         }
         //if(id == 0)
             //this.clearElements();
     }//GEN-LAST:event_updateButtonActionPerformed
     
     private void updateItemImage(){
         /*
         if(!inventory.core.ProjectBOMStockMain.destIp.equals("localhost") && !inventory.core.ProjectBOMStockMain.destIp.split("\\.")[0].equals("192")){
             JOptionPane.showMessageDialog(this, "You are Not in Private Network. So, You Can Not Upload Image. But Text Upload will be Done.","Alert",JOptionPane.OK_OPTION);
             return;
         }*/
         
         String sql = "SELECT id,name FROM inventory.item_image WHERE name = '"+this.nameTextField.getText().substring(0, this.nameTextField.getText().length()-1)+"';";
             
         ResultSet rs = inventory.core.DBConnection.executeQuery(sql);
         
         String INSERT_PICTURE = null;
         String UPDATE_PICTURE = null;
         //`name`='1', `description`='1' 
         
         boolean isInsert = false;
         
         try {
             if(rs.next()){
                 UPDATE_PICTURE = "UPDATE `inventory`.`item_image` SET `name`=?, `image`=? WHERE `id`='"+rs.getInt("id")+"';";
             }else{
                 INSERT_PICTURE = "INSERT INTO `inventory`.`item_image` (`name`,`image`) VALUES (?,?);";
                 isInsert = true;
             }
         } catch (SQLException ex) {
             Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
         }
         
         FileInputStream fis = null;
         PreparedStatement ps = null;
         try {
             Connection conn = inventory.core.DBConnection.getWriteConnection();
             conn.setAutoCommit(false);
             
             File file = new File(this.imgSource);
             
             fis = new FileInputStream(file);
             if(isInsert){
                 ps = conn.prepareStatement(INSERT_PICTURE);
             }else{
                 ps = conn.prepareStatement(UPDATE_PICTURE);
             }
             ps.setString(1, this.nameTextField.getText().substring(0, this.nameTextField.getText().length()-1));
             ps.setBinaryStream(2, fis, (int) file.length());
 
             ps.executeUpdate();
             conn.commit();
         } catch (SQLException | FileNotFoundException ex) {
             Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
         } finally {
             try {
                 if(ps!=null&&fis!=null){
                     ps.close();
                     fis.close();
                 }
             } catch (    SQLException | IOException ex) {
                 Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
     
     private boolean edit() throws SQLException{
         boolean validation = false;
         
         if(JOptionPane.showConfirmDialog(this, "This will be saved. Are you Sure?!","Confirm",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.CANCEL_OPTION){
             return validation;
         }//check save
         
         if(this.nameTextField.getText().trim().equals("")){
                                 //name is empty
             JOptionPane.showMessageDialog(this, "Neme is empty.","Warning",JOptionPane.OK_OPTION);
             return validation;
         }else{
             try {
                 //name should be unique
                 if(!this.nameTextField.getText().equals(originalName)){
                     ResultSet rs2 = inventory.core.DBConnection.executeQuery("SELECT name FROM inventory.item where name = '"+this.nameTextField.getText()+"';");
                     if(rs2.next()){
                         if(JOptionPane.showConfirmDialog(this, "Name : "+this.nameTextField.getText()+" is duplicated. Will you going on?!","Warning",JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION){
                             return validation;
                         }
                     }
                 }
             }catch (SQLException ex) {
                 Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }//name check
         
         if(!this.validations()){
             return validation;
         }
         
         String sql = "UPDATE `inventory`.`item` SET `name`='"+this.nameTextField.getText()+"', `description`='"+this.descriptionTextArea.getText()+"', `model_id`='"+this.model_id
                 +"', `package_id`='"+this.package_id+"', `category_id`='"+category_id+"', `price`='"+Float.parseFloat(this.priceTextField.getText())
                 +"', `nation_id`='"+this.nation_id+"', `current`='"+this.currentTextField.getText()+"',  `expiredate`='"+this.expiredate.toString()+"' WHERE `id`='"+this.id+"';";
         
         inventory.core.DBConnection.updateQuery(sql);
         
         return true;
     }
     
     private boolean validations(){
         boolean validatation = false;
         
         if(this.category_id == null || this.category_id == 0){
             JOptionPane.showMessageDialog(this, "Category is empty.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }//category check
         
         if(this.model_id == null || this.model_id == 0){
             if(JOptionPane.showConfirmDialog(this, "Model is empty. Would you set this attribute \"NONE\"?","Confirm",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION){
                 try {
                     ResultSet rs = inventory.core.DBConnection.executeQuery("SELECT id FROM inventory.model where name = 'none';");
                     if(rs.next()){
                         model_id = rs.getInt("id");
                         this.modelTextField.setText("None");
                     }
                 } catch (SQLException ex) {
                     Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }else{
                 return validatation;
             }
         }//model check
         
         if(this.package_id == null || this.package_id == 0){
             JOptionPane.showMessageDialog(this, "Package is empty.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }//package check
         
         if(this.priceTextField.getText().trim().equals("")){
             //price is empty or wrong
             JOptionPane.showMessageDialog(this, "Price is empty.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }else if(!this.isFloat(this.priceTextField.getText())){
             JOptionPane.showMessageDialog(this, "Price is Wrong Tpye.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }//price check
         
         if(this.nation_id == null || this.nation_id == 0){
             JOptionPane.showMessageDialog(this, "Nation is empty.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }//nation check
         
         if(this.currentTextField.getText().trim().equals("")){
             JOptionPane.showMessageDialog(this, "Current Remain is empty or Wrong input.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }//current check
         
         if(expiredate == null){
             JOptionPane.showMessageDialog(this, "Expiredate is empty.","Warning",JOptionPane.OK_OPTION);
             return validatation;
         }//ecpiredate check
         
         if(this.descriptionTextArea.getText().trim().equals("")){
             this.descriptionTextArea.setText(" ");
         }//description check
         
         validatation = true;
         return validatation;
     }
     
     private boolean register() throws SQLException{
         boolean validation = false;
         if(JOptionPane.showConfirmDialog(this, "This will be saved. Are you Sure?!","Confirm",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.CANCEL_OPTION){
             return validation;
         }
         
         if(this.nameTextField.getText().trim().equals("")){
                                 //name is empty
             JOptionPane.showMessageDialog(this, "Neme is empty.","Warning",JOptionPane.OK_OPTION);
             return validation;
         }else{
             try {
                 //name should be unique
                 ResultSet rs2 = inventory.core.DBConnection.executeQuery("SELECT name FROM inventory.item where name = '"+this.nameTextField.getText()+"';");
                 if(rs2.next()){
                     if(JOptionPane.showConfirmDialog(this, "Name : "+this.nameTextField.getText()+" is duplicated. Will you going on?!","Warning",JOptionPane.OK_OPTION) != JOptionPane.OK_OPTION){
                         return validation;
                     }
                 }
             }catch (SQLException ex) {
                 Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         if(!this.validations()){
             return validation;
         }
         
         String sql = "INSERT INTO `inventory`.`item` (`name`, `description`, `model_id`, `package_id`, `category_id`, `price`, `nation_id`, `current`, `register_id`, `disable_id`, `expiredate`) "+
                 "VALUES ('"+this.nameTextField.getText()+"', '"+this.descriptionTextArea.getText()+"', '"+this.model_id+"', '"+this.package_id+
                 "', '"+this.category_id+"', '"+Float.parseFloat(this.priceTextField.getText())+"', '"+this.nation_id+"', '"+this.currentTextField.getText()+"', '"+inventory.core.MainFrame.user_id+
                 "', '1', '"+this.expiredate.toString()+"');";
         
         ResultSet rs = inventory.core.DBConnection.updateQueryGetID(sql);
         
         if(rs.next()){
             this.id = (int)rs.getLong(1);
             System.out.println(this.id + "in edit");
         }
         
         validation = true;
         return validation;
     }
     
     private boolean isFloat(String s){
         try{
             Float.parseFloat(s);
             return true;
         }catch(Exception e){
             return false;
         }
     }
     
     private void categoryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_categoryButtonActionPerformed
         // TODO add your handling code here:
         String s = selectDialog("Category Select", "Please Check a Category","SELECT * FROM inventory.category ORDER BY name;","name");
         if ((s != null) && (s.length() > 0)) {
             this.category_id = ids.get(possibilities.indexOf(s));
             this.categoryTextField.setText(s);
             return;
         }
     }//GEN-LAST:event_categoryButtonActionPerformed
 
     private void modelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelButtonActionPerformed
         // TODO add your handling code here:
         String s = selectDialog("Model Select", "Please Select a Model","SELECT * FROM inventory.model ORDER BY name;","name");
         if ((s != null) && (s.length() > 0)) {
             this.model_id = ids.get(possibilities.indexOf(s));
             this.modelTextField.setText(s);
             return;
         }
     }//GEN-LAST:event_modelButtonActionPerformed
 
     private void packageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packageButtonActionPerformed
         // TODO add your handling code here:
         String s = selectDialog("Package Select", "Please Select a Package","SELECT * FROM inventory.package ORDER BY count;","count");
         if ((s != null) && (s.length() > 0)) {
             this.package_id = ids.get(possibilities.indexOf(s));
             this.packageTextField.setText(s);
             return;
         }
     }//GEN-LAST:event_packageButtonActionPerformed
 
     private void nationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nationButtonActionPerformed
         // TODO add your handling code here:
         String s = selectDialog("Nation Select", "Please Select a Nation","SELECT * FROM inventory.nation ORDER BY name DESC;","name");
         if ((s != null) && (s.length() > 0)) {
             this.nation_id = ids.get(possibilities.indexOf(s));
             this.nationTextField.setText(s);
             return;
         }
     }//GEN-LAST:event_nationButtonActionPerformed
 
     private void expireDateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_expireDateButtonActionPerformed
         // TODO add your handling code here:
        Date utilDate = null;
        if(this.expiredate != null){
            inventory.core.DateChooser dc = new inventory.core.DateChooser(new JDialog());
            utilDate = dc.select(this.expiredate);
//            utilDate =  dc.select();
         }else{
            utilDate =  new inventory.core.DateChooser(new JDialog()).select();
        }
        if(utilDate != null){
            java.sql.Date date = new java.sql.Date(utilDate.getTime());
            if(date != null){
                this.expiredate = date;
            }
        }
        
        if(this.expiredate != null){
            this.expiredateTextField.setText(this.expiredate.toString());    
         }
     }//GEN-LAST:event_expireDateButtonActionPerformed
 
     private void categoryButtonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_categoryButtonKeyTyped
         // TODO add your handling code here:
         if(evt.getKeyChar()=='\n'){
             this.categoryButtonActionPerformed(null);
         }
     }//GEN-LAST:event_categoryButtonKeyTyped
 
     private void modelButtonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_modelButtonKeyTyped
         // TODO add your handling code here:
         if(evt.getKeyChar()=='\n'){
             this.modelButtonActionPerformed(null);
         }
     }//GEN-LAST:event_modelButtonKeyTyped
 
     private void packageButtonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_packageButtonKeyTyped
         // TODO add your handling code here:
         if(evt.getKeyChar()=='\n')
             this.packageButtonActionPerformed(null);
     }//GEN-LAST:event_packageButtonKeyTyped
 
     private void nationButtonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nationButtonKeyTyped
         // TODO add your handling code here:
         if(evt.getKeyChar()=='\n')
             this.nationButtonActionPerformed(null);
     }//GEN-LAST:event_nationButtonKeyTyped
 
     private void expireDateButtonKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_expireDateButtonKeyTyped
         // TODO add your handling code here:
         if(evt.getKeyChar()=='\n')
             this.expireDateButtonActionPerformed(null);
     }//GEN-LAST:event_expireDateButtonKeyTyped
 
     private void imageEditButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_imageEditButtonActionPerformed
         try {
             // TODO add your handling code here:
             File a = null;
             a = inventory.core.ProjectBOMStockMain.openImageFile();
             
             if(a != null){
                 this.imgSource = a.getAbsolutePath();
             }else{
                 return;
             }
             
             this.imageLabel.setIcon(new ImageIcon(ImageIO.read(new File(this.imgSource)).getScaledInstance(this.imageWidth, this.imageHeight, Image.SCALE_DEFAULT)));
         } catch (IOException ex) {
             Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
         }
     }//GEN-LAST:event_imageEditButtonActionPerformed
 
     private String selectDialog(String Title, String Message, String sql, String possibleTarget){
         try {
             ResultSet rs = inventory.core.DBConnection.executeQuery(sql);
             
             possibilities = new ArrayList<String>();
             ids = new ArrayList<Integer>();
             
             if(rs != null){
                 while(rs.next()){
                     possibilities.add(rs.getString(possibleTarget));
                     ids.add(rs.getInt("id"));
                 }
             }
             String s = (String)JOptionPane.showInputDialog(
                                 null,
                                 Message,
                                 Title,
                                 JOptionPane.PLAIN_MESSAGE,
                                 null,
                                 possibilities.toArray(),
                                 null);
             
             return s;
         } catch (SQLException ex) {
             Logger.getLogger(ItemUpdate.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton backButton;
     private javax.swing.JButton categoryButton;
     private javax.swing.JLabel categoryLabel;
     private javax.swing.JTextField categoryTextField;
     private javax.swing.JLabel currentLabel;
     private javax.swing.JTextField currentTextField;
     private javax.swing.JLabel descriptionLabel;
     private javax.swing.JScrollPane descriptionScrollPane;
     private javax.swing.JTextArea descriptionTextArea;
     private javax.swing.JButton expireDateButton;
     private javax.swing.JLabel expiredateLabel;
     private javax.swing.JTextField expiredateTextField;
     private javax.swing.JButton imageEditButton;
     private javax.swing.JLabel imageLabel;
     private javax.swing.JButton modelButton;
     private javax.swing.JLabel modelLabel;
     private javax.swing.JTextField modelTextField;
     private javax.swing.JLabel nameLabel;
     private javax.swing.JTextField nameTextField;
     private javax.swing.JButton nationButton;
     private javax.swing.JLabel nationLabel;
     private javax.swing.JTextField nationTextField;
     private javax.swing.JButton packageButton;
     private javax.swing.JLabel packageLabel;
     private javax.swing.JTextField packageTextField;
     private javax.swing.JLabel priceLabel;
     private javax.swing.JTextField priceTextField;
     private javax.swing.JButton updateButton;
     // End of variables declaration//GEN-END:variables
 
     @Override
     protected void myInitComponents() {
         this.initComponents();
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void LoadData() {
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 
     @Override
     public void setComponetsColor(Color transparent) {
         this.backButton.setBackground(transparent);
         this.categoryButton.setBackground(transparent);
         this.categoryTextField.setBackground(transparent);
         this.currentTextField.setBackground(transparent);
         this.descriptionScrollPane.setBackground(transparent);
         this.descriptionTextArea.setBackground(transparent);
         this.expiredateTextField.setBackground(transparent);
         this.expireDateButton.setBackground(transparent);
         this.modelButton.setBackground(transparent);
         this.modelTextField.setBackground(transparent);
         this.nameTextField.setBackground(transparent);
         this.nationButton.setBackground(transparent);
         this.nationTextField.setBackground(transparent);
         this.packageButton.setBackground(transparent);
         this.packageTextField.setBackground(transparent);
         this.priceTextField.setBackground(transparent);
         this.updateButton.setBackground(transparent);
         this.imageEditButton.setBackground(transparent);
         //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
     }
 }
