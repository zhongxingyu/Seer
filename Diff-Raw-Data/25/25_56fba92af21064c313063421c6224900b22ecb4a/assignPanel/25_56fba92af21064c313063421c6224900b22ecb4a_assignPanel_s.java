 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.event.*;
 import java.util.*;
 
 public class assignPanel extends JPanel
 {
     private JButton add, edit, delete;
     private JList assigns;
     private JPanel buttons;
     private Vector<Assignment> assignList;
     private addAssignPopUp aAssign; 
     private editAssignPopup eAssign;
     
 
    public assignPanel(Instructor loggedInInstr)
 
     {
         aAssign = new addAssignPopUp();
         
         eAssign = new editAssignPopup();
         
         assignList = loggedInInstr.getAList();
         
         add = new JButton("Add Assignment");
         add.addActionListener(new addListener());
         
         edit = new JButton("Edit Assignment");
         edit.addActionListener(new editListener());
         
         delete = new JButton("Delete Assignment");
         
         assigns = new JList();
         assigns.setListData(assignList);
         
      //   assigns.addActionListener(new updateListener()) ;
         
         buttons = new JPanel();
         buttons.setLayout(new GridLayout(1,3));
         buttons.add(add);
         buttons.add(edit);
         buttons.add(delete);
         
         setLayout(new GridLayout(2,1));
         add(assigns);
         add(buttons);
         assigns.setVisible(true);
         buttons.setVisible(true);
         
     }
     
 	private class addListener implements ActionListener {
 		public void actionPerformed(ActionEvent e) {
 			aAssign.setVisible(true);
 			if (aAssign.submitClicked()) {
 				int aNum = 3; // for now
 				try {
 					String x = aAssign.getName();
 					String y = (aAssign.getDate());
 					assignList.add(new Assignment(x, y, aNum));
					// assigns.setListData(assignList);
 
 				} finally {
 					assigns.setListData(assignList);
 				}
 			}
 			// assigns.setListData(assignList);
 			// assigns.updateUI();
 			// assignList.add(aAssign.getName());
 
 		}
 	}
 
     
     private class editListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e)
         {
             eAssign.setVisible(true);
         }
     }
     
 //     private class updateListener implements ListDataListener
 //     {
 //         public void contentsChanged(ListDataEvent e) 
 //         {
 //             
 //         }
 //     }
     
 }
