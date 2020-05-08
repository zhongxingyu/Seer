 package phyml.gui.view;
 
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.FormSpecs;
 import com.jgoodies.forms.layout.RowSpec;
 import org.jdesktop.swingx.VerticalLayout;
 import phyml.gui.control.NodeController;
 import phyml.gui.model.Node;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Project: grisu
  * <p/>
  * Written by: Markus Binsteiner
  * Date: 21/11/13
  * Time: 11:02 AM
  */
 public class SubmitPanel extends JPanel {
 
     public static final int SIMPLE_LAYOUT = 1;
     public static final int TABBED_LAYOUT = 2;
     private final int layout;
     private InputFormPanel inputFormPanel;
     private final boolean displayDebug;
     private final NodeController controller;
     private JButton submitButton;
 
     public SubmitPanel(NodeController controller, int layout, boolean displayDebug) {
         super();
         this.controller = controller;
         this.layout = layout;
         this.displayDebug = displayDebug;
 
         setLayout(new VerticalLayout());
        add(getForm().getPanel());
//        JScrollPane scrollPane = new JScrollPane(getForm().getPanel());
//        add(scrollPane)
// ;
         JPanel buttonPanel = new JPanel();
         ColumnSpec[] cs = new ColumnSpec[]{
                 FormSpecs.RELATED_GAP_COLSPEC,
                 ColumnSpec.decode("right:pref:grow"),
                 FormSpecs.RELATED_GAP_COLSPEC
         };
         RowSpec[] rs = new RowSpec[]{
                 FormSpecs.RELATED_GAP_ROWSPEC,
                 RowSpec.decode("pref"),
                 FormSpecs.RELATED_GAP_ROWSPEC,
         };
         FormLayout formLayout = new FormLayout(cs, rs);
         buttonPanel.setLayout(formLayout);
         buttonPanel.add(getSubmitButton(), "2, 2");
         add(buttonPanel);
     }
 
     public InputFormPanel getForm() {
 
         if (inputFormPanel == null) {
             switch (layout) {
                 case SIMPLE_LAYOUT:
                     inputFormPanel = new InputFormPanelSimple();
                     break;
                 case TABBED_LAYOUT:
                     inputFormPanel = new InputFormPanelTabbed();
                     break;
                 default:
                     inputFormPanel = new InputFormPanelCollapsible();
             }
 
             for (Node n : controller.getNodes()) {
                 inputFormPanel.addNode(n);
             }
 
             if (displayDebug) {
                 JPanel cmd = new CommandlineDebugPanel();
                 cmd.setBorder(new TitledBorder("Current commandLine"));
                 inputFormPanel.getPanel().add(cmd);
             }
 
         }
 
         return inputFormPanel;
     }
 
 
     private void submit() {
 
         SwingUtilities.invokeLater(new Thread() {
             public void run() {
                 getSubmitButton().setEnabled(false);
             }
         });
         try {
             controller.executeCommand();
         } finally {
             SwingUtilities.invokeLater(new Thread() {
                 public void run() {
                     getSubmitButton().setEnabled(true);
                 }
             });
         }
     }
 
     public JButton getSubmitButton() {
         if (submitButton == null) {
             submitButton = new JButton("Run PhyML");
             submitButton.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     submit();
                 }
             });
         }
         return submitButton;
     }
 }
