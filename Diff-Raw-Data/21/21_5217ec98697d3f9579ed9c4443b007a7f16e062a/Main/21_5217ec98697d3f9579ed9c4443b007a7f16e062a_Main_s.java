 package com.github.krassekoder.mm13client;
 
 import com.github.krassekoder.mm13client.gui.MainWindow;
 import com.github.krassekoder.mm13client.network.Connection;
 import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QIcon;
 
 /**
  * The main class. It calls "MainWindow" and "QApplication".
  */
 public class Main {
     public static void main(String[] args) {
         QApplication a = new QApplication(args);
         QIcon icon;
        
        
 
         QApplication.setApplicationName("MensaManager2013Client");
         QApplication.setApplicationVersion("Alpha");
         QApplication.setOrganizationName("KrasseKoder");
         QApplication.setOrganizationDomain("http://www.github.com/KrasseKoder/");
         QApplication.setWindowIcon(icon = new QIcon("classpath:com/github/krassekoder/windowicon.png"));
        
 
         MainWindow w = new MainWindow();
 
         new Connection();
 
         w.show();
         QApplication.exec();
     }
 }
