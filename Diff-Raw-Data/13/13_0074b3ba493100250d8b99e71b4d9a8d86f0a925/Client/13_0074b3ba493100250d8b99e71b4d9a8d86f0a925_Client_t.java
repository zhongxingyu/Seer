 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package client;
 
 import interfaces.Function;
 
 import java.rmi.Naming;
 import java.rmi.RemoteException;
 
 import com.trolltech.qt.QThread;
 import com.trolltech.qt.core.QTimer;
 import com.trolltech.qt.gui.QApplication;
 import com.trolltech.qt.gui.QHBoxLayout;
 import com.trolltech.qt.gui.QLabel;
 import com.trolltech.qt.gui.QPushButton;
 import com.trolltech.qt.gui.QTextEdit;
 import com.trolltech.qt.gui.QVBoxLayout;
 import com.trolltech.qt.gui.QWidget;
 
 import exception.BadRequest;
 import exception.UnloggedUserException;
 
 /**
  *
  * @author Nicolas
  */
 public class Client extends QWidget {
        protected String id;
        protected Function f;
        private Log log;
        private boolean connected;
 
        protected QVBoxLayout mainLayout;
        protected QVBoxLayout bpLayout;
        protected QHBoxLayout sendLayout;
        protected QLabel label;
        protected QTextEdit te;
        protected QPushButton bp;
        protected QPushButton bpMess;
        
        public Client(){
     	   connected=false;
     	   label = new QLabel("");
     	   te = new QTextEdit("Enter your request");
     	   bp = new QPushButton("Send");
     	   bp.clicked.connect(this, "sendRequest()");
     	   bpMess = new QPushButton("Actu");
     	   bpMess.clicked.connect(this, "setText()");
 
     	   mainLayout = new QVBoxLayout();
     	   bpLayout = new QVBoxLayout();
     	   sendLayout = new QHBoxLayout();
   	       label.setFixedHeight(500);
   	       te.setFixedHeight(100);
   	       bp.resize(200, 200);
     	   
     	   this.sendLayout.addWidget(te);
     	   this.sendLayout.addLayout(bpLayout);
     	   this.bpLayout.addWidget(bp);
     	   this.bpLayout.addWidget(bpMess);
     	   this.mainLayout.addWidget(label);
     	   this.mainLayout.addLayout(sendLayout);
     	   this.setLayout(mainLayout);
 
  	      log = new Log();
  	      log.show();
  	      log.getId.connect(this, "connect(String)");
  	      
  	      this.resize(400, 600);
  	      this.move(0, 0);
     	   
     	   try{
     		   //f=(Function)Naming.lookup("//192.168.0.17/C:/users/Nicolas/Desktop/ei3/info/objet/tp_tchat/tchat/");
    		   f=(Function) Naming.lookup("//127.0.0.1/daddyServer");
     		   label.setText(label.text()+"\n"+"Server found");
     	   }
     	   catch (Exception e){
     		   e.printStackTrace();
     	   }

            label.setText(label.text()+"\n"+"Please enter your name to enter the tchat");
        }
        
        public void setText(){
     	   if(connected){
 			   try {
 				   String[] s = f.getMessage(id);
 				   for(int i =0; i<s.length; i++){
 					   label.setText(label.text()+"\n"+s[i]);
 				   }
 			   } catch (RemoteException | UnloggedUserException| BadRequest e) {
 				   e.printStackTrace();
 			   }
     	   }
        }
        
        public void sendRequest(){
     	   String request="";
     	   String in = te.toPlainText();
     	   te.setText("");
     	   int i = in.indexOf(' ');
     	   //If the request is made of only one word such as 'who'
     	   if(in.equals("who")){
     		   request=in;
     	   }
     	   else if(i==-1){
     		   request += " "+id;
     	   }
     	   //Else it insert the id of the user just after the request word
     	   else{
 	    	   request = in.substring(0, i);
 	    	   request += " "+id+in.substring(i);
     	   }
     	   try {
     		   label.setText(label.text()+"\n"+f.request(request));
 			} catch (RemoteException | BadRequest e) {
 				label.setText(label.text()+"\n"+e.getMessage());
 			}
        }
        
        
        
        public void connect(String s){
     	   try {
     		   label.setText(label.text()+"\n"+f.request("connect "+s));
     		   id=s;
     		   connected=true;
 		} catch (RemoteException | BadRequest e) {
 			e.printStackTrace();
 		}
        }
        
        public static void main(String[] args){
     	   QApplication.initialize(args);
     	   Client user = new Client();
     	   user.show();
     	   QApplication.exec();
        }
 }
