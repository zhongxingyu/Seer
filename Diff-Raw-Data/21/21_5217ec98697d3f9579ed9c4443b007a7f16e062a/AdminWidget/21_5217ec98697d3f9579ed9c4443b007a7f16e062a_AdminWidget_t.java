 package com.github.krassekoder.mm13client.gui;
 
 import com.github.krassekoder.mm13client.network.Packet;
 import com.github.krassekoder.mm13client.network.Packet4Admin;
 import com.trolltech.qt.gui.QDialog;
 import com.trolltech.qt.gui.QDoubleSpinBox;
 import com.trolltech.qt.gui.QFormLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QIcon;
 import com.trolltech.qt.gui.QInputDialog;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QMessageBox;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QSpinBox;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The AdminWidget allows you to add new users and
  * products easily by writing the information into
  * the lines and confirming it by pressing "Enter".
  */
 public class AdminWidget extends QWidget
 {
     private QGroupBox newUser;
     private QGroupBox newEntry;
     private QVBoxLayout qv1, qv2, qv3;
     private QFormLayout fLa1, fLa2;
     private QLineEdit username, password, productname;
     private QSpinBox rights, id;
     private QDoubleSpinBox price;
     private QLabel name1Label,name2Label,passwordLabel,priceLabel,IdLabel,rightsLabel;
     private QPushButton enter1,enter2, deleteUser, deleteProduct;
 
     public AdminWidget(QWidget qw)
  {
      super(qw);
      setupUi();
  }
 
     /**
      * This method sets up the User Interface of "AdminWidget" including
      * 'QGroupBox' and 'QPushButton'.
      */
     private void setupUi(){
         setLayout(qv1=new QVBoxLayout());
 
         qv1.addWidget(newEntry = new QGroupBox(tr("Product"), this));
         newEntry.setLayout(qv3 = new QVBoxLayout(newEntry));
         qv3.addLayout(fLa2 = new QFormLayout(newEntry));
         fLa2.addRow(name2Label = new QLabel(tr("Name:"), newEntry), productname = new QLineEdit(newEntry));
         fLa2.addRow(priceLabel = new QLabel(tr("Price:"), newEntry), price = new QDoubleSpinBox(newEntry));
         price.setRange(0, Double.MAX_VALUE);
         price.setSingleStep(0.01);
         price.setSuffix(tr("$"));
         fLa2.addRow(IdLabel = new QLabel(tr("Id:"), newEntry), id = new QSpinBox(newEntry));
         id.setRange(0, Integer.MAX_VALUE);
         qv3.addWidget(enter2 = new QPushButton(tr("Enter / Edit"), this));
         enter2.setIcon(new QIcon("classpath:com/github/krassekoder/save.png"));
 
 
         qv1.addWidget(deleteProduct = new QPushButton(tr("Delete Product..."), this));
         deleteProduct.setIcon(new QIcon("classpath:com/github/krassekoder/delete.png"));
         qv1.addWidget(newUser = new QGroupBox(tr("User"), this));
         newUser.setLayout(qv2 = new QVBoxLayout(newUser));
         qv2.addLayout(fLa1 = new QFormLayout(newUser));
         fLa1.addRow(name1Label = new QLabel(tr("Username:"), newUser), username = new QLineEdit(newUser));
         fLa1.addRow(passwordLabel = new QLabel(tr("Password:"), newUser), password = new QLineEdit(newUser));
         fLa1.addRow(rightsLabel = new QLabel(tr("Rights:"), newUser), rights = new QSpinBox(newUser));
         rights.setRange(1, Byte.MAX_VALUE);
         qv2.addWidget(enter1 = new QPushButton(tr("Enter / Edit"), this));
         enter1.setIcon(new QIcon("classpath:com/github/krassekoder/save.png"));
         qv1.addWidget(deleteUser = new QPushButton(tr("Delete User..."), this));
         deleteUser.setIcon(new QIcon("classpath:com/github/krassekoder/delete.png"));
         enter1.clicked.connect(this,"SaveNewUser()");
         enter2.clicked.connect(this,"SaveEntry()");
         deleteProduct.clicked.connect(this,"deleteProduct()");
         deleteUser.clicked.connect(this,"deleteUser()");
      }
 
   /**
    * Save NewEntry
    * Empty name will delete entry
    */
   public void SaveEntry()
  {
       if(productname.text().isEmpty()) {
           QMessageBox.information(this, tr("Invalid Product"), tr("Productname must not be empty."));
           return;
       }
         try {
            Packet4Admin.instance.editProduct(id.text(), productname.text(), price.value() + "");
         } catch(Packet.InvalidPacketException ex) {
             Logger.getLogger(AdminWidget.class.getName()).log(Level.SEVERE, null, ex);
         }
       System.out.println("Product(" + id.text() + "): " + productname.text() + " : " + price.text());
       productname.clear();
       id.clear();
       price.setValue(0);
       MainWindow.instance.showFoodList();
   }
   /**
    * Save NewUser saving to Library missing
    * rights = 0 will delete user
    */
   public void SaveNewUser()
   {
       if(username.text().isEmpty()) {
           QMessageBox.information(this, tr("Invalid User"), tr("Username must not be empty."));
           return;
       }
         try {
             Packet4Admin.instance.editUser(username.text(), password.text(), rights.text());
         } catch(Packet.InvalidPacketException ex) {
             Logger.getLogger(AdminWidget.class.getName()).log(Level.SEVERE, null, ex);
         }
       System.out.println("User:" + username.text() + " " + password.text() + "(" + rights.text() + ")");
       username.clear();
       password.clear();
       rights.setValue(1);
   }
   //Deletes an User
   private void deleteUser() {
       String name = QInputDialog.getText(this, tr("Delete User"), tr("Username:"));
       if(name.isEmpty())
           return;
         try {
             Packet4Admin.instance.editUser(name, "", "0");
         } catch(Packet.InvalidPacketException ex) {
             Logger.getLogger(AdminWidget.class.getName()).log(Level.SEVERE, null, ex);
         }
   }
     //Deletes an Product
     private void deleteProduct() {
         QInputDialog d = new QInputDialog(this);
         d.setInputMode(QInputDialog.InputMode.IntInput);
         d.setIntRange(0, Integer.MAX_VALUE);
         d.setWindowTitle(tr("Delete Product"));
         d.setLabelText(tr("Id:"));
         d.exec();
         if(d.result() != QDialog.DialogCode.Accepted.value())
             return;
         try {
             Packet4Admin.instance.editProduct(d.intValue() + "", "", "0");
         } catch(Packet.InvalidPacketException ex) {
             Logger.getLogger(AdminWidget.class.getName()).log(Level.SEVERE, null, ex);
         }
         MainWindow.instance.showFoodList();
     }
 }
