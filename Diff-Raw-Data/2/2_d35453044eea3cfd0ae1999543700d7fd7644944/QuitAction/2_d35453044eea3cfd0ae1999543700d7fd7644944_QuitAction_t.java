 package controller;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import model.Environment;
 
 public class QuitAction implements ActionListener {
     private JFrame frame;
 
     public QuitAction(JFrame f) {
 	frame = f;
     }
 
     public void actionPerformed(ActionEvent e) {
 	if (Environment.wrkman.unsavedProjects()) {
	    int option = JOptionPane.showConfirmDialog(frame, "Istniej niezapisane projekty. Czy chcesz je zapisa przed wyjciem?", "Zapisz zmiany",
 						       JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
 	    switch (option) {
 	    case JOptionPane.YES_OPTION:
 		Environment.wrkman.saveAllProjects();
 		frame.dispose();
 		break;
 	    case JOptionPane.NO_OPTION:
 		frame.dispose();
 		break;
 	    }
 	} else {
 	    frame.dispose();
 	}
     }
 }
