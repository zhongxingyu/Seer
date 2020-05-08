 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 package simplemail.errorhandling;
 
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.*;
 import simplemail.Command;
 
 /**
  *
  * @author Student
  */
 public class ErrorDlg extends JDialog implements ActionListener {
     String eMessage;
     ErrorMediator em;
     
     public ErrorDlg(Frame owner, String message)
     {
         super(owner, "Error", true);
         
         eMessage = message;
         em = new ErrorMediator();
         
         initComponents();
         formatComponents();
     }
     
     /* Components Methods */
     JLabel errorMessageL;
     OKButton okB;
     
     private void initComponents()
     {
         // Register components to mediator for logic.
         em.registerGUI(this);
         
         errorMessageL = new JLabel("Error: " + eMessage);
         
         okB = new OKButton(this, em);
     }
     
     // GUI Designer generated code
     private void formatComponents()
     {
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(0, 107, Short.MAX_VALUE)
                         .addComponent(okB, GroupLayout.PREFERRED_SIZE, 67, GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(errorMessageL)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(GroupLayout.Alignment.LEADING)
             .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(errorMessageL)
                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 26, Short.MAX_VALUE)
                 .addComponent(okB)
                 .addContainerGap())
         );
 
         pack();
     }
     
     // Send all actions to mediator class
     @Override
     public void actionPerformed(ActionEvent ae) {
         // Divert action events to the mediator
         Command comd = (Command) ae.getSource();
         comd.execute();
     }
     
     private class OKButton extends JButton implements Command
     {
         ErrorMediator em;
         public OKButton(ActionListener al, ErrorMediator em)
         {
            super("Ok");
             addActionListener(al);
             this.em = em;
         }
         
         @Override
         public void execute()
         {
             em.closeGUI();
         }
     }
 }
