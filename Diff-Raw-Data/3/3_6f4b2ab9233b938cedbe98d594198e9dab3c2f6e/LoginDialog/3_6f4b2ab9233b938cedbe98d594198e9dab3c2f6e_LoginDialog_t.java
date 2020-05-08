 package com.github.krassekoder.mm13client.gui;
 
 import com.github.krassekoder.mm13client.network.Connection;
 import com.github.krassekoder.mm13client.network.Packet;
 import com.github.krassekoder.mm13client.network.Packet0Login;
 import com.trolltech.qt.QVariant;
 import com.trolltech.qt.core.QSettings;
 import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QBoxLayout;
 import com.trolltech.qt.gui.QCheckBox;
 import com.trolltech.qt.gui.QDialog;
 import com.trolltech.qt.gui.QFormLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QMessageBox;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QShowEvent;
 import com.trolltech.qt.gui.QSpinBox;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class LoginDialog extends QDialog{
 
     private QLabel nameLabel, passwordLabel, ipLabel, portLabel;
     private QFormLayout loginLayout;
     private QLineEdit username, password, ip;
     private QSpinBox port;
     private QGroupBox server;
     private QBoxLayout vLa1, hLa1, hLa2;
     private QCheckBox atStart;
     private QPushButton login;
 
     public byte rights = 0;
 
     /**
      * The "LoginDialog" offers the possibility to log in to the server and to
      * open a Server-Client connection.
      *
      */
     public LoginDialog(QWidget qw) {
         super(qw);
 
         setupUi();
         load();
         QApplication.instance().aboutToQuit.connect(this, "finish()");
         if(atStart.isChecked())
             show();
     }
     /**
      * This method sets up the User Interface of the "LoginDialog" including
      * 'Spinbox' and 'Checkbox'.
      */
     private void setupUi() {
         setWindowTitle(tr("Login"));
         setLayout(vLa1 = new QVBoxLayout(this));
         vLa1.addLayout(loginLayout = new QFormLayout(this));
         //username edit
         loginLayout.addRow(nameLabel = new QLabel(tr("&Username:"), this), username = new QLineEdit(this));
         nameLabel.setBuddy(username);
         username.setFocus();
         //password edit
         loginLayout.addRow(passwordLabel = new QLabel(tr("&Password:"), this), password = new QLineEdit(this));
         passwordLabel.setBuddy(password);
         password.setEchoMode(QLineEdit.EchoMode.Password);
         //server box
         vLa1.addWidget(server = new QGroupBox(tr("Server"), this));
         server.setLayout(hLa1 = new QHBoxLayout(server));
         //ip edit
         hLa1.addWidget(ipLabel = new QLabel(tr("&Ip:"), server));
         hLa1.addWidget(ip = new QLineEdit(server));
         ipLabel.setBuddy(ip);
         //port spinBox
         hLa1.addWidget(portLabel = new QLabel("P&ort:", server));
         hLa1.addWidget(port = new QSpinBox(server));
         portLabel.setBuddy(port);
         port.setMaximum(99999);
         port.setValue(1996);
         //button panel
         vLa1.addLayout(hLa2 = new QHBoxLayout(this));
         hLa2.addWidget(atStart = new QCheckBox(tr("Show this dialog at start"), this));
         atStart.setChecked(true);
         hLa2.addStretch();
         hLa2.addWidget(login = new QPushButton(tr("&Login"), this));
         login.setAutoDefault(true);
         login.clicked.connect(this, "login()");
         username.returnPressed.connect(password, "setFocus()");
         password.returnPressed.connect(login, "animateClick()");
     }
 
     public void login() {
        if(rights > 0)
            return;

         if(!Connection.instance.connect(ip.text(), port.value())) {
             System.out.println("Error connecting");
             return;
         }
         try {
             rights = Packet0Login.instance.login(username.text(), password.text());
             if(rights > 0) {
                 System.out.println("Logged in as " + username.text());
                 MainWindow.instance.setWindowTitle(MainWindow.instance.windowTitle() + " - " + username.text());
                 if(rights > 1)
                     MainWindow.instance.unlockAdminWidget();
                 MainWindow.instance.disableLogin();
                 hide();
             }
             else {
                 System.out.println("Failed to log in as " + username.text());
                 QMessageBox.information(this, tr("Login"), tr("Invalid username or password."));
             }
         } catch(Packet.InvalidPacketException ex) {
             Logger.getLogger(LoginDialog.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private void load() {
         QSettings s = new QSettings();
         s.beginGroup("login");
         ip.setText(QVariant.toString(s.value("ip")));
         port.setValue(QVariant.toInt(s.value("port")));
         atStart.setChecked(QVariant.toBoolean(s.value("show")));
     }
 
     private void finish() {
         QSettings s = new QSettings();
         s.beginGroup("login");
         s.setValue("ip", ip.text());
         s.setValue("port", port.value());
         s.setValue("show", atStart.isChecked());
     }
 
 }
