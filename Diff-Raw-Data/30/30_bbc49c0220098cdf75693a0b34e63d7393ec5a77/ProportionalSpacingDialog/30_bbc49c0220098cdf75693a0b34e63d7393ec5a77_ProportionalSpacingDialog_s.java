 package edu.mit.wi.haploview;
 
 import javax.swing.*;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import java.util.Hashtable;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class ProportionalSpacingDialog extends JDialog implements ActionListener,
         ChangeListener, Constants{
 
     private HaploView hv;
 
     public ProportionalSpacingDialog(HaploView h, String title){
         super (h, title);
 
         hv = h;
 
         JPanel contents = new JPanel();
         contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
 
         JSlider slider = new JSlider(0,100,
                (int)(Options.getSpacingThreshold()*100));
         Hashtable labeltable = new Hashtable();
         labeltable.put(new Integer(0), new JLabel("None"));
         labeltable.put(new Integer(100), new JLabel ("Full"));
         slider.setLabelTable( labeltable );
         slider.setPaintLabels(true);
         slider.addChangeListener(this);
         contents.add(slider);
 
         JButton doneButton = new JButton("Done");
         doneButton.addActionListener(this);
         contents.add(doneButton);
 
         this.setContentPane(contents);
         this.setLocation(this.getParent().getX() + 100,
                 this.getParent().getY() + 100);
         this.setModal(true);
     }
 
     public void actionPerformed(ActionEvent e) {
         String command = e.getActionCommand();
         if (command.equals("Done")){
             this.dispose();
         }
     }
 
     public void stateChanged(ChangeEvent e) {
         JSlider source = (JSlider)e.getSource();
         if (!source.getValueIsAdjusting()) {
             double thresh = ((double)source.getValue())/100;
            Options.setSpacingThreshold(thresh);
             hv.dPrimeDisplay.computePreferredSize();
             if (hv.dPrimeDisplay != null && hv.tabs.getSelectedIndex() == VIEW_D_NUM){
                 hv.dPrimeDisplay.repaint();
             }
         }
     }
 }
