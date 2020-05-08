 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.gui.ruleeditor;
 
 import evopaint.interfaces.IAction;
 import evopaint.util.mapping.RelativeCoordinate;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 
 /**
  *
  * @author tam
  */
 public class JAction extends JPanel implements ActionListener {
     private JButton closeButton;
 
     public JAction(List<IAction> actions) {
 
         DefaultComboBoxModel comboBoxModelForDirections = new DefaultComboBoxModel();
         comboBoxModelForDirections.addElement(RelativeCoordinate.ALL);
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
 
        DefaultComboBoxModel comboBoxModelforActions = new DefaultComboBoxModel();
        for (IAction action : actions) {
            comboBoxModelforActions.addElement(action.toString());
        }
        JComboBox comboBoxActions = new JComboBox(comboBoxModelforActions);
        add(comboBoxActions);

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
