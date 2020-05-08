 package org.i4qwee.chgk.trainer.view.dialogs;
 
 import org.i4qwee.chgk.trainer.controller.brain.ScoreManagerSingleton;
 import org.i4qwee.chgk.trainer.view.DefaultUIProvider;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * User: 4qwee
  * Date: 02.11.11
  * Time: 22:23
  */
 public class NewGameDialog extends AbstractDialog
 {
     private JFrame owner;
 
     public NewGameDialog(JFrame owner)
     {
         super(owner);
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
 
         this.owner = owner;
 
         JPanel mainPanel = new JPanel();
         mainPanel.setBorder(DefaultUIProvider.getDefaultEmptyBorder());
 
         BoxLayout boxLayout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);
         mainPanel.setLayout(boxLayout);
 
         JPanel namesPanel = new JPanel(new GridLayout(2, 2));
         namesPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
         mainPanel.add(namesPanel);
 
         namesPanel.add(new JLabel("Имя левого игрока"));
 
         final JTextField leftNameTextField = new JTextField();
         namesPanel.add(leftNameTextField);
 
         namesPanel.add(new JLabel("Имя правого игрока"));
 
         final JTextField rightNameTextField = new JTextField();
         namesPanel.add(rightNameTextField);
 
         JPanel buttonsPanel = new JPanel();
         mainPanel.add(buttonsPanel);
 
         BoxLayout buttonsBoxLayout = new BoxLayout(buttonsPanel, BoxLayout.X_AXIS);
         buttonsPanel.setLayout(buttonsBoxLayout);
 
         JButton okButton = new JButton("Готово");
         okButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 setVisible(false);
                 ScoreManagerSingleton.getInstance().setNames(leftNameTextField.getText(), rightNameTextField.getText());
                 ScoreManagerSingleton.getInstance().newGame();
             }
         });
 
         buttonsPanel.add(Box.createHorizontalGlue());
         buttonsPanel.add(okButton);
 
         JButton cancelButton = new JButton("Отмена");
         cancelButton.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 setVisible(false);
             }
         });
 
        buttonsPanel.add(Box.createHorizontalStrut(10));
         buttonsPanel.add(cancelButton);
 
         setContentPane(mainPanel);
 
         setSize(300, 130);
         setResizable(false);
     }
 }
