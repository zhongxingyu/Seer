 package game;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 
 public class NotificationDialog extends JDialog
 {
     private ActionListener _myListener;
     private JButton _yes;
     private JLabel _label;
     private JButton _no;
 
     public NotificationDialog(JFrame parent, String text)
     {
         super(parent, "Wanna play?");
 
         ImageIcon icon = new ImageIcon(getClass().getResource("Information24.gif"));
 
         setLayout(new BorderLayout());
         setModalityType(ModalityType.TOOLKIT_MODAL);
 //        setFocusable(true);
 
         JPanel labelPanel = new JPanel(new BorderLayout());
         _yes = new JButton("Yes");
         _no = new JButton("No");
         JLabel _label = new JLabel(text, icon, JLabel.CENTER);
 
         _yes.setFocusCycleRoot(true);
         _yes.setMnemonic('y');
         _no.setMnemonic('n');
 
//        _no.addActionListener(new ActionListener()
//        {
//            public void actionPerformed(ActionEvent e)
//            {
//                setVisible(false);
//            }
//        });
 
         labelPanel.add(_label, BorderLayout.CENTER);
 
         JPanel buttonPanel = new JPanel(new FlowLayout());
         buttonPanel.add(_yes, BorderLayout.SOUTH);
         buttonPanel.add(_no, BorderLayout.SOUTH);
 
         add(labelPanel, BorderLayout.CENTER);
         add(buttonPanel, BorderLayout.SOUTH);
 
         setLocationRelativeTo(parent);
         setSize(350, 125);
     }
 
     public void addYesButtonListener(ActionListener actionListener)
     {
         _yes.addActionListener(actionListener);
     }
 
     public void addNoButtonListener(ActionListener actionListener)
     {
         _no.addActionListener(actionListener);
     }
 }
