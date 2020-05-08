 package com.github.krassekoder.mm13client;
 
 import com.trolltech.qt.gui.QBoxLayout;
 import com.trolltech.qt.gui.QCheckBox;
 import com.trolltech.qt.gui.QDialog;
 import com.trolltech.qt.gui.QFormLayout;
 import com.trolltech.qt.gui.QGroupBox;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QLineEdit;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QSpinBox;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 public class LoginDialog extends QDialog{
 
     private QLabel nameLabel, passwordLabel, ipLabel, portLabel;
     private QFormLayout loginLayout;
     private QLineEdit username, password, ip;
     private QSpinBox port;
     private QGroupBox server;
     private QBoxLayout vLa1, hLa1, hLa2;
     private QCheckBox atStart;
     private QPushButton login;
 
     public LoginDialog(QWidget qw) {
         super(qw);
 
         setupUi();
     }
 
     private void setupUi() {
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
         port.setMaximum(9999);
         port.setValue(1996);
         //button panel
         vLa1.addLayout(hLa2 = new QHBoxLayout(this));
         hLa2.addWidget(atStart = new QCheckBox(tr("Show this dialog at start"), this));
         hLa2.addStretch();
         hLa2.addWidget(login = new QPushButton(tr("&Login"), this));
         login.setAutoDefault(false);
         login.clicked.connect(this, "accept()");
         username.returnPressed.connect(password, "setFocus()");
         password.returnPressed.connect(login, "animateClick()");
     }
 }
