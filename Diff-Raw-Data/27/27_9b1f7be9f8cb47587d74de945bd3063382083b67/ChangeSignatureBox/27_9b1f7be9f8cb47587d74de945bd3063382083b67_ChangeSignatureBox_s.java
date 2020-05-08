 package org.argouml.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 
 import javax.swing.*;
 
 import org.omg.uml.foundation.core.Classifier;
 import org.omg.uml.foundation.core.DataType;
 import org.omg.uml.foundation.core.Feature;
 import org.omg.uml.foundation.core.Generalization;
 import org.omg.uml.foundation.core.Operation;
 import org.omg.uml.foundation.core.Parameter;
 import org.omg.uml.foundation.core.UmlClass;
 
 import org.argouml.kernel.Project;
 import org.argouml.kernel.ProjectManager;
 
 import org.argouml.model.Model;
 
 import org.argouml.refactoring.CheckConstraints;
 
 // Gaurav Nanda
 public class ChangeSignatureBox  extends JFrame implements ActionListener{
     private static final Logger LOG =
         Logger.getLogger(ChangeSignatureBox.class.getName());
 
     private List nodes = new ArrayList();
     private Operation target;
 
     JTextField gui_method_return_type, gui_method_name;
     JTextField return_types[], param_names[];
 
     /**
      * Class constructor.
      *
      * @param title      the title of the help window.
      */
     public ChangeSignatureBox(String title, Object target) {
         super(title);
 
         this.target = (Operation) target;
 
         Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();
         setLocation(scrSize.width / 2 - 400, scrSize.height / 2 - 300);
 
         getContentPane().setLayout(new BorderLayout());
         setSize( 400, 200);
         setRenameNode();
     }
 
     private List<Operation> get_matching_operations(UmlClass klass, Operation
             op_gold) {
         List<Feature> operation_list = klass.getFeature();
         List<Operation> return_list = new ArrayList<Operation>();
         List<Parameter> param_list_gold = op_gold.getParameter();
 
         for (Feature _op_silv : operation_list) {
             boolean match = true;
             Operation op_silv = (Operation) _op_silv;
             if(op_silv.getName().equals(op_gold.getName())) {
                 List<Parameter> param_list_silv = op_silv.getParameter();
                 if (param_list_silv.size() == param_list_gold.size()) {
                     // Return type is not used in matching method signatures.
                     for (int i=1; i<param_list_gold.size(); i++) {
                         Parameter param_gold = param_list_gold.get(i);
                         String type_gold = param_gold.getType().getName();
 
                         Parameter param_silv = param_list_silv.get(i);
                         String type_silv = param_silv.getType().getName();
 
                         if (!type_gold.equals(type_silv)) {
                            LOG.log(Level.DEBUG, "Failed because param types"
                                     " differ: " + type_silv + ", " + type_gold);
                             match = false;
                             break;
                         }
                     }
                 } else {
                    LOG.log(Level.DEBUG, "Failed because param list sizes "
                             "differ" + param_list_silv.size() + ", " +
                             param_list_gold.size());
                     match = false;
                 }
             } else {
                LOG.log(Level.DEBUG, "Failed because function names differ: "
                         + op_silv.getName() + ", " + op_gold.getName());
                 match = false;
             }
 
             if (match) {
                 return_list.add(op_silv);
             }
         }
 
         return return_list;
     }
 
     private List<UmlClass> get_child_classes(UmlClass klass) {
         ArrayList<UmlClass> child_list = new ArrayList<UmlClass>();
         Collection<Generalization> coll = Model.getFacade().getSpecializations(
                 (Object) klass);
 
         for (Generalization gen : coll) {
             UmlClass child = (UmlClass) gen.getChild();
             child_list.add(child);
         }
 
         return child_list;
     }
 
     private void setRenameNode() {
         String method_name = null, return_type = null;
         List<Parameter> param_list = target.getParameter();
         Parameter return_param = param_list.get(0);
 
         method_name = target.getName();
         return_type = return_param.getType() != null ?
             return_param.getType().getName() : "void";
 
         Container cp = getContentPane();
         cp.setLayout(new FlowLayout());
 
         JLabel gui_return_type = new JLabel("Return type:");
         gui_method_return_type = new JTextField(return_type, 15);
 
         JLabel gui_name = new JLabel("Method name:");
         gui_method_name = new JTextField(method_name, 15);
 
         cp.add(gui_return_type);
         cp.add(gui_method_return_type);
         cp.add(gui_name);
         cp.add(gui_method_name);
 
         return_types = new JTextField[param_list.size()];
         param_names = new JTextField[param_list.size()];
 
         for (int i = 1; i < param_list.size(); i++) {
             Parameter param = param_list.get(i);
             String type = param.getType() != null ? param.getType().getName() :
                 "void";
             return_types[i-1] = new JTextField(type, 15);
             param_names[i-1] = new JTextField(param.getName(), 15);
 
             cp.add(return_types[i-1]);
             cp.add(param_names[i-1]);
         }
 
         setSize(350, 400);
 
         JButton submitButton = new JButton("Change method signature");
         cp.add(submitButton);
         submitButton.addActionListener(this);
     }
 
     public boolean is_input_valid() {
         if (this.gui_method_name.getText().length() == 0) return false;
         if (this.gui_method_return_type.getText().length() == 0)  return false;
 
         List<Parameter> param_list = target.getParameter();
         for (int i = 1; i < param_list.size(); i++) {
             if (param_names[i-1].getText().length() == 0) return false;
             if (return_types[i-1].getText().length() == 0) return false;
         }
 
         return true;
     }
 
    private void update_method_signature(Operation operation) {
         Project project = ProjectManager.getManager().getCurrentProject();
         List<Parameter> param_list = operation.getParameter();
 
        String method_name = this.gui_method_name.getText();
         operation.setName(method_name);

        String method_return_type = this.gui_method_return_type.getText();
         Object data_type = project.findType(method_return_type, false);
         if (data_type == null) {
             data_type = project.findType(method_return_type, true);
         }
 
         Parameter return_param = param_list.get(0);
         return_param.setType((Classifier) data_type);
 
         for (int i = 1; i < param_list.size(); i++) {
             Parameter param = param_list.get(i);
             param.setName(param_names[i-1].getText());
 
             data_type = project.findType(return_types[i-1].getText(), false);
             if (data_type == null) {
                 data_type = project.findType(return_types[i-1].getText(), true);
             }
 
             param.setType((Classifier) data_type);
         }
     }
 
     private void propagate_change(UmlClass klass, Operation op_gold) {
         List<UmlClass> child_list = get_child_classes(klass);
         for (UmlClass child : child_list) {
             List<Operation> operation_list = get_matching_operations(child,
                     op_gold);
             for (Operation operation : operation_list) {
                update_method_signature(operation);
             }
 
             propagate_change(child, op_gold);
         }
     }
 
     @Override
         public void actionPerformed(ActionEvent event) {
             if (is_input_valid()) {
                 // Propagate changes to child classes.
                 UmlClass owner = (UmlClass) this.target.getOwner();
                 propagate_change(owner, this.target);
 
                 // Update this method.
                update_method_signature(this.target);
 
                 if (CheckConstraints.validateUML() == false) {
                     // TODO: Undo change.
                     LOG.log(Level.INFO, "Validation failed, but cannot undo.");
                 } else {
                     LOG.log(Level.INFO, "Validation succeeded!");
                 }
 
                 // Get rid of the dialog box.
                 this.dispose();
             } else {
                 JOptionPane.showMessageDialog(null, "Invalid input!",
                         "Input validator", JOptionPane.INFORMATION_MESSAGE);
             }
         }
 }
