 package gui;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.LayoutManager;
 import java.util.HashMap;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JPanel;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 
 import controller.Instruction;
 import controller.Condition;
 import controller.Action;
 import controller.IntStringMap;
 import controller.SqlCommunicator;
 
 public class RulePanel extends JPanel {
     private RuleTreePanel parent_panel_;
     private HashMap brick_images_;
     private ConditionPanel condition_panel_;
     private ActionPanel acition_panel_;
     private JCheckBox active_switch_;
     private JButton delete_button_;
     private int id_;
     private boolean selected_;
 
     public RulePanel(HashMap brick_images, IntStringMap mark_array,
             IntStringMap state_array, int id, RuleTreePanel parent_panel) {
         // super();
         parent_panel_ = parent_panel;
         brick_images_ = brick_images;
         id_ = id;
         selected_ = true;
         acition_panel_ = new ActionPanel(brick_images_, mark_array,
                 state_array, this, id_);
         condition_panel_ = new ConditionPanel(brick_images_, mark_array,
                 state_array, this, id_);
         initComponents();
     }
 
     public RulePanel(HashMap brick_images, IntStringMap mark_array,
             IntStringMap state_array, Instruction instruction, int id,
             RuleTreePanel parent_panel) {
         // super();
         parent_panel_ = parent_panel;
         brick_images_ = brick_images;
         id_ = id;
         selected_ = instruction.isAktive();
         acition_panel_ = new ActionPanel(brick_images_, mark_array,
                 state_array, instruction.getAction(), this, id_);
         condition_panel_ = new ConditionPanel(brick_images_, mark_array,
                 state_array, instruction.getCondition(), this, id_);
         initComponents();
     }
 
     private void initComponents() {
         active_switch_ = new JCheckBox();
         delete_button_ = new JButton();
 
         this.setLayout(null);
         this.add(acition_panel_);
         this.add(condition_panel_);
         acition_panel_.setBounds(80, 25, 145, 50);
        condition_panel_.setBounds(2, 2, 95, 76);
 
         this.add(active_switch_);
         active_switch_.setBounds(190, 2, 20, 20);
         active_switch_.setOpaque(false);
         active_switch_.setSelected(selected_);
         active_switch_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 if (active_switch_.isSelected()) {
                     selected_ = true;
                     SqlCommunicator.add_log("Regel aktiviert. Regel id: "
                             + Integer.toString(id_), 256);
                 } else {
                     SqlCommunicator.add_log("Regel deaktiviert. Regel id: "
                             + Integer.toString(id_), 256);
                     selected_ = false;
                 }
                 changedSelected();
             }
         });
 
         this.add(delete_button_);
         delete_button_.setBounds(210, 6, 13, 13);
         delete_button_.setOpaque(false);
         delete_button_.setIcon((ImageIcon) brick_images_.get("X_BUTTON"));
         delete_button_.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 SqlCommunicator.add_log(
                         "Regel entfernt. Regel id: " + Integer.toString(id_),
                         256);
                 parent_panel_.deleteRule(id_);
             }
         });
         if (selected_ == true)
             setBackGround(Color.GRAY, Color.GRAY.darker());
         else
             setBackGround(Color.GRAY.brighter(), Color.GRAY.brighter());
 
         this.setBorder(new javax.swing.border.SoftBevelBorder(
                 javax.swing.border.BevelBorder.RAISED));
         repaint();
     }
 
     private void changedSelected() {
         if (selected_ == true) {
             active_switch_.setSelected(true);
             setBackGround(Color.GRAY, Color.GRAY.darker());
         } else {
             active_switch_.setSelected(false);
             setBackGround(Color.GRAY.brighter(), Color.GRAY.brighter());
         }
         parent_panel_.ruleUpdated(id_, getInstruction());
     }
 
     private void setSelected(boolean selected) {
         changedSelected();
     }
 
     private boolean getSelected() {
         return selected_;
     }
 
     public int getID() {
         return id_;
     }
 
     public void setBackGround(Color condition, Color action) {
         this.setBackground(condition);
         acition_panel_.setBackground(action);
         condition_panel_.setBackground(condition);
         repaint();
     }
 
     public Instruction getInstruction() {
         return new Instruction(condition_panel_.getCondition(),
                 acition_panel_.getAction(), id_, selected_);
     }
 
     public void actionUpdated() {
         parent_panel_.ruleUpdated(id_, getInstruction());
     }
 
     public void conditionUpdated() {
         parent_panel_.ruleUpdated(id_, getInstruction());
     }
 
     public void updateInstruction(Instruction instruction) {
         acition_panel_.updateAction(instruction.getAction());
         condition_panel_.updateCondition(instruction.getCondition());
         selected_ = instruction.isAktive();
         if (selected_ == true) {
             active_switch_.setSelected(true);
             setBackGround(Color.GRAY, Color.GRAY.darker());
         } else {
             active_switch_.setSelected(false);
             setBackGround(Color.GRAY.brighter(), Color.GRAY.brighter());
         }
     }
 }
