 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.gui.ruleeditor;
 
 import evopaint.interfaces.ICondition;
 import evopaint.util.mapping.RelativeCoordinate;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author tam
  */
 public class JCondition extends JPanel implements ActionListener {
     private JButton closeButton;
 
     public JCondition(List<ICondition> conditions) {
         DefaultComboBoxModel comboBoxModelForDirections = new DefaultComboBoxModel();
        comboBoxModelForDirections.addElement(RelativeCoordinate.SELF);
         comboBoxModelForDirections.addElement(RelativeCoordinate.ALL);
         comboBoxModelForDirections.addElement(RelativeCoordinate.ANY);
         comboBoxModelForDirections.addElement(RelativeCoordinate.NORTH);
         comboBoxModelForDirections.addElement(RelativeCoordinate.NORTH_EAST);
         comboBoxModelForDirections.addElement(RelativeCoordinate.EAST);
         comboBoxModelForDirections.addElement(RelativeCoordinate.SOUTH_EAST);
         comboBoxModelForDirections.addElement(RelativeCoordinate.SOUTH);
         comboBoxModelForDirections.addElement(RelativeCoordinate.SOUTH_WEST);
         comboBoxModelForDirections.addElement(RelativeCoordinate.WEST);
         comboBoxModelForDirections.addElement(RelativeCoordinate.NORTH_WEST);
 
         JComboBox comboBoxDirections = new JComboBox(comboBoxModelForDirections);
         add(comboBoxDirections);
 
         DefaultComboBoxModel comboBoxModelforConditions = new DefaultComboBoxModel();
         for (ICondition condition : conditions) {
             comboBoxModelforConditions.addElement(condition.toString());
         }
         JComboBox comboBoxConditions = new JComboBox(comboBoxModelforConditions);
         add(comboBoxConditions);
 
         this.closeButton = new JButton("X");
         this.closeButton.addActionListener(this);
         add(this.closeButton);
     }
 
     public void actionPerformed(ActionEvent e) {
         if (e.getSource() == this.closeButton &&
                 ((JPanel)getParent()).getComponentCount() > 1) {
             JPanel parent = (JPanel)getParent();
             parent.remove(this);
             parent.revalidate();
             return;
         }
     }
 
     
 }
