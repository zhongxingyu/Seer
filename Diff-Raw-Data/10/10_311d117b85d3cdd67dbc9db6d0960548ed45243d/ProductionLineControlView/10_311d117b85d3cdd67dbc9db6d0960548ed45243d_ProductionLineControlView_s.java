 package mvc.views;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 /**
  *
  * @author John
  */
 public class ProductionLineControlView {
     
     private JPanel controlPanel;
     
     public JButton addButton;
     public JButton deleteButton;
     public JTextField textField;
     
     public ProductionLineControlView(PlanningView view) {
         Dimension controlSize = new Dimension(ChartView.LEFT_OFFSET, 0);
         
         // Create control panel
         SpringLayout layout = new SpringLayout();
         controlPanel = new JPanel(layout);
         controlPanel.setSize(controlSize);
         controlPanel.setPreferredSize(controlSize);
         
         Dimension buttonSize = new Dimension(75, 20);
         int margin = 3;
         
         JLabel label = new JLabel("Production lines");
         label.setFont(new Font("Verdana", Font.BOLD, 14));
         layout.putConstraint(
                 SpringLayout.NORTH, label, margin,
                 SpringLayout.NORTH, controlPanel);
         layout.putConstraint(
                 SpringLayout.WEST, label, margin,
                 SpringLayout.WEST, controlPanel);
         controlPanel.add(label);
         
         textField = new JTextField("Production line name...");
         layout.putConstraint(
                 SpringLayout.SOUTH, textField, - (buttonSize.height + margin * 2),
                 SpringLayout.SOUTH, controlPanel);
         layout.putConstraint(
                 SpringLayout.WEST, textField, margin,
                 SpringLayout.WEST, controlPanel);
         layout.putConstraint(
                 SpringLayout.EAST, textField, -margin,
                 SpringLayout.EAST, controlPanel);
         controlPanel.add(textField);
         
         addButton = new JButton("Add");
         addButton.setSize(buttonSize);
         addButton.setPreferredSize(buttonSize);
         layout.putConstraint(
                 SpringLayout.SOUTH, addButton, -margin,
                 SpringLayout.SOUTH, controlPanel);
         layout.putConstraint(
                 SpringLayout.WEST, addButton, margin,
                 SpringLayout.WEST, controlPanel);
         controlPanel.add(addButton);
         
         deleteButton = new JButton("Delete");
         deleteButton.setSize(buttonSize);
         deleteButton.setPreferredSize(buttonSize);
         layout.putConstraint(
                 SpringLayout.SOUTH, deleteButton, -margin,
                 SpringLayout.SOUTH, controlPanel);
         layout.putConstraint(
                 SpringLayout.EAST, deleteButton, -margin,
                 SpringLayout.EAST, controlPanel);
         controlPanel.add(deleteButton);
     }
     
     public JPanel getComponent() {
         return controlPanel;
     }
     
 }
