 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package evopaint.gui.ruleseteditor;
 
 import evopaint.gui.ruleseteditor.util.NamedObjectListCellRenderer;
 import evopaint.Configuration;
 import evopaint.pixel.rulebased.interfaces.IAction;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.util.LinkedHashMap;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  *
  * @author tam
  */
 public class JAction extends JPanel implements ActionListener {
     private JButton closeButton;
 
     public JAction(final IAction action) {
 
         setLayout(new GridBagLayout());
         GridBagConstraints constraints = new GridBagConstraints();
         setBorder(new BevelBorder(BevelBorder.RAISED));
         constraints.anchor = GridBagConstraints.NORTHWEST;
 
         DefaultComboBoxModel model = new DefaultComboBoxModel();
 
         IAction selection = null;
         for (IAction a : Configuration.availableActions) {
             model.addElement(a);
             if (a.getClass() == action.getClass()) {
                 selection = a;
             }
         }
         assert(selection != null);
 
         JComboBox comboBoxActions = new JComboBox(model);
         comboBoxActions.setRenderer(new NamedObjectListCellRenderer());
         comboBoxActions.setSelectedItem(selection);
         comboBoxActions.addActionListener(new ComboBoxActionsListener(this));
         constraints.gridx = 0;
         constraints.gridy = 0;
         constraints.gridwidth = 2;
         constraints.fill = GridBagConstraints.HORIZONTAL;
         add(comboBoxActions, constraints);
 
         JPanel panelParameters = new JPanel();
         panelParameters.setBorder(new TitledBorder("Parameters"));
         panelParameters.setLayout(new GridBagLayout());
         constraints.gridx = 1;
         constraints.gridy = 1;
         constraints.gridwidth = 1;
         add(panelParameters, constraints);
 
         LinkedHashMap<String,JComponent> parameters = action.getParametersForGUI();
 
         SpinnerNumberModel spinnerModel = new SpinnerNumberModel(action.getCost(), 0, Integer.MAX_VALUE, 1);
         JSpinner costSpinner = new JSpinner(spinnerModel);
         costSpinner.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 action.setCost((Integer) ((JSpinner) e.getSource()).getValue());
             }
         });
         // getting too big because Integer.MAX_VALUE can get pretty long, so let's cut it off
         costSpinner.setPreferredSize(new Dimension(100, costSpinner.getPreferredSize().height));
         final JFormattedTextField spinnerText = ((JSpinner.DefaultEditor)costSpinner.getEditor()).getTextField();
         spinnerText.addFocusListener(new FocusListener() {
             public void focusGained(FocusEvent e) {
                 SwingUtilities.invokeLater(new Runnable() { // only seems to work this way
                         public void run() {
                             spinnerText.selectAll();
                         }
                 });
             }
             public void focusLost(FocusEvent e) {}
         });
        parameters.put("Cost", costSpinner);
 
         constraints.gridx = 0;
         constraints.gridy = 1;
         constraints.anchor = GridBagConstraints.WEST;
         constraints.fill = GridBagConstraints.BOTH;
         for (String string : parameters.keySet()) {
             constraints.gridx = 0;
             constraints.insets = new Insets(0, 0, 5, 10);
             panelParameters.add(new JLabel(string + ":"), constraints);
             constraints.gridx = 1;
             constraints.insets = new Insets(0, 0, 5, 0);
             panelParameters.add(parameters.get(string), constraints);
             constraints.gridy = constraints.gridy + 1;
         }
 
         constraints.fill = GridBagConstraints.NONE;
         constraints.gridwidth = 1;
         constraints.gridx = 0;
         constraints.gridy = 1;
         add(new JTargetPicker(action.getDirections()), constraints);
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
 
     private class ComboBoxActionsListener implements ActionListener {
         private JAction jAction;
 
         public ComboBoxActionsListener(JAction jCondition) {
             this.jAction = jCondition;
         }
 
         public void actionPerformed(ActionEvent e) {
             try {
                 IAction prototype = (IAction)((JComboBox)e.getSource()).getSelectedItem();
                 IAction action = prototype.getClass().newInstance();
                 JAction newJAction = new JAction(action);
                 JPanel parent = (JPanel)jAction.getParent();
                 parent.remove(jAction);
                parent.add(newJAction);
                 parent.revalidate();
                 parent.repaint(); // because of removed component
             } catch (InstantiationException ex) {
                 ex.printStackTrace();
                 System.exit(1);
             } catch (IllegalAccessException ex) {
                 ex.printStackTrace();
                 System.exit(1);
                 //Logger.getLogger(JCondition.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 }
