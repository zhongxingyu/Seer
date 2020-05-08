 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.*;
 import java.io.IOException;
 import java.net.URL;
 
 public class UpdateDisplayDialog extends JDialog  implements ActionListener, Constants{
 
     public UpdateDisplayDialog(HaploView h, String title, UpdateChecker uc) {
         super(h, title);
 
         JPanel contents = new JPanel();
         contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
 
        Font bigguns = new Font("Default", Font.BOLD, 14);
 
 
         JLabel announceLabel = new JLabel("A newer version of Haploview is available: " + uc.getNewVersion());
         announceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         announceLabel.setFont(bigguns);
         JLabel urlLabel = new JLabel("http://www.broad.mit.edu/mpg/haploview/");
         urlLabel.setFont(bigguns);
         urlLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
         JScrollPane changeScroller = null;
 
         try {
             JEditorPane changePane = new JEditorPane();
             changePane.setEditable(false);
             changePane.setPage(new URL("http://18.157.34.100:8080/hapchanges.html"));
             changeScroller = new JScrollPane(changePane);
            changeScroller.setPreferredSize(new Dimension(150,150));
         } catch(IOException ioe) {
             //if were here then we were able to check for an update, so well just show them a dialog
             //without listing the changes
         }
 
         contents.add(announceLabel);
         contents.add(urlLabel);
         if(changeScroller != null) {
             changeScroller.setAlignmentX(Component.CENTER_ALIGNMENT);
            contents.add(changeScroller);
         }
 
 
         JButton okButton = new JButton("OK");
         okButton.addActionListener(this);
         okButton.setAlignmentX(Component.CENTER_ALIGNMENT);
         contents.add(okButton);
 
 
         this.setContentPane(contents);
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
         this.setResizable(false);
 
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if(command.equals("OK")) {
             this.dispose();
         }
     }
 }
