 package keytool.controller;
  
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFileChooser;
 
 import keytool.model.Model;
 import keytool.view.MainWindow;
 import keytool.view.View;
  
 public class Controller {
   protected View view;
   protected Model model;
  
   public Controller(Model model, View view){
     this.view = view;
     this.model = model;
     
     initMainWindowListener();
     initFOWindowListener();
     
   }
  
   private void initMainWindowListener() {
 	MainWindow mw = this.view.getMainWindow();
 	mw.addItemQuitListener(new ItemQuitListener());
 	mw.addItemOpenListener(new ItemOpenListener());
 	mw.addBtnImportListener(new BtnImportListener());
 	mw.addBtnExportListener(new BtnExportListener());
   }
   
   class ItemQuitListener implements ActionListener {
     public void actionPerformed(ActionEvent e) {
    	view.getMainWindow().dispose();
     }
   }
   
   class ItemOpenListener implements ActionListener {
     public void actionPerformed(ActionEvent e) {
     	view.showFileOpenWindow();
     }
   }
   
   class BtnImportListener implements ActionListener {
     public void actionPerformed(ActionEvent e) {
 
     }
   }
   
   class BtnExportListener implements ActionListener {
     public void actionPerformed(ActionEvent e) {
 
     }
   }
   
   private void initFOWindowListener() {
 	  this.view.getFileOpenWindow().addFOActionListener(new FOActionListener());
 	  this.view.getFileOpenWindow().addFOWindowListener(new FOWindowListener());
   }
   
   class FOActionListener implements ActionListener {
 	    public void actionPerformed(ActionEvent e) {
 	        if (JFileChooser.APPROVE_SELECTION.equals(e.getActionCommand())) {
 	            System.out.println("Ouverture !");
 	        } else if (JFileChooser.CANCEL_SELECTION.equals(e.getActionCommand())) {
 	            view.hideFileOpenWindow();
 	        }
 	    }
   }
   
   class FOWindowListener extends WindowAdapter {
 	    public void windowClosing(WindowEvent e) {
 	    	view.hideFileOpenWindow();
 	    }
 	  }
   
 }
