 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mvc.views;
 
import views.*;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 /**
  *
  * @author David
  */
 public class RightBottomView {
     
     private JPanel jPanelRightBottom;
     private JButton jButtonTable;
     
     public RightBottomView(JPanel jPanelRight){
         jPanelRightBottom = new JPanel();
         jButtonTable = new JButton();
         
         jButtonTable.setText("jButton2");
 
         GroupLayout jPanelRightBottomLayout = new GroupLayout(jPanelRightBottom);
         jPanelRightBottom.setLayout(jPanelRightBottomLayout);
         jPanelRightBottomLayout.setHorizontalGroup(
             jPanelRightBottomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addComponent(jButtonTable, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 427, Short.MAX_VALUE)
         );
         jPanelRightBottomLayout.setVerticalGroup(
             jPanelRightBottomLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addComponent(jButtonTable, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
         );
 
         jPanelRight.add(jPanelRightBottom);
     }
     
 }
