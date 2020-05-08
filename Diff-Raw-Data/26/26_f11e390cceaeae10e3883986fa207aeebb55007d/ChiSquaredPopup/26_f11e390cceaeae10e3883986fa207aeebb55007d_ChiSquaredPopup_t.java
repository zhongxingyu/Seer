 /*
  * This file is part of GenSim.
  *
  * GenSim is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GenSim is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GenSim.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gensim;
 
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 
 /**
  *
  * @author Andrew Vitkus
  */
 public class ChiSquaredPopup extends JFrame {
 
     private int degreesOfFreedom = 0;
     private JTextField[] expected;
     private JTextField[] observed;
     
     public void showTestWindow() {
         boolean cancel = false;
         do {
             try {
                String response = JOptionPane.showInputDialog(ChiSquaredPopup.this, "How many degrees of freedom?", "Enter a number.", JOptionPane.QUESTION_MESSAGE);
                if (response == null) {
                    return;
                }
                degreesOfFreedom = Integer.parseInt(response);
                if (degreesOfFreedom <= 0) {
                    throw new IllegalArgumentException("Degrees of freedom '" + degreesOfFreedom + "' is invalid. Must be 1 or greater!");
                }
            } catch (IllegalArgumentException ex) {
                 if (JOptionPane.showConfirmDialog(ChiSquaredPopup.this, "Invalid degrees of freedom!", "Error!", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                     cancel = true;
                 }
             }
        } while (degreesOfFreedom <= 0 && !cancel);
 
         if (!cancel) {
             buildWindow();
             setSize(75 * (degreesOfFreedom + 2), 150);
             setTitle("Chi-Squared Test");
             
             setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/GenSim icon large.png")));
             setVisible(true);
         }
     }
 
     private void buildWindow() {
         setLocationByPlatform(true);
         setLayout(new GridBagLayout());
 
         final JLabel chiSquaredValue = new JLabel("Chi-squared: ");
         addComponent(chiSquaredValue, 1, degreesOfFreedom + 2, 2);
 
         expected = new JTextField[degreesOfFreedom + 1];
         observed = new JTextField[degreesOfFreedom + 1];
 
         addComponent(new JLabel("Expected:"), degreesOfFreedom + 1, 0, 0);
         addComponent(new JLabel("Observed:"), degreesOfFreedom + 1, 0, 2);
 
         for (int i = 0; i <= degreesOfFreedom; i++) {
             expected[i] = new JTextField(null, 4);
             expected[i].addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (isFilled()) {
                         chiSquaredValue.setText("Chi-squared: " + calculate());
                         repaint();
                     }
                 }
             });
             addComponent(expected[i], 1, i, 1);
 
             observed[i] = new JTextField(null, 4);
             observed[i].addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     if (isFilled()) {
                         chiSquaredValue.setText("Chi-squared: " + calculate());
                         repaint();
                     }
                 }
             });
             addComponent(observed[i], 1, i, 3);
         }
     }
 
     private boolean isFilled() {
         for (int i = 0; i <= degreesOfFreedom; i++) {
             if ((expected[i].getText() == null) || (observed[i].getText() == null)) {
                 return false;
             }
         }
 
         return true;
     }
 
     private double calculate() {
         double value = 0;
 
         for (int i = 0; i <= degreesOfFreedom; i++) {
             try {
                value += (Math.pow(Double.parseDouble(observed[i].getText()) - Double.parseDouble(expected[i].getText()), 2) / Double.parseDouble(expected[i].getText()));
             } catch(NumberFormatException ex) {}
         }
 
         return value;
     }
 
     private void addComponent(Component component, int width, int x, int y) {
         GridBagConstraints c = new GridBagConstraints();
 
         c.weightx = 1;
         c.weighty = 1;
         c.gridwidth = width;
         c.gridx = x;
         c.gridy = y;
 
         add(component, c);
     }
 }
