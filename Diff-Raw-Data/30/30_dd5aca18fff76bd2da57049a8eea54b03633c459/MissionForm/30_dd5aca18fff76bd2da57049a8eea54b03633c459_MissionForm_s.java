 package ch.bli.mez.view.management;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import ch.bli.mez.view.DefaultForm;
 
 public class MissionForm extends DefaultForm {
 
   private static final long serialVersionUID = -2823140194213618642L;
 
   private JTextField missionName;
   private JTextField comment;
   private JCheckBox isOrgan;
   private JButton saveButton;
   private JButton statusButton;
   private JLabel nameLabel;
   private JLabel commentLabel;
 
   /**
    * Create the panel.
    */
   public MissionForm(boolean isActive) {
     build();
     setActive(isActive);
     setEnterKeyListener(saveButton, new JTextField[] { missionName, comment });
   }
 
   private void build() {
     setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
 
     nameLabel = new JLabel("Auftragsname");
     this.add(nameLabel);
     nameLabel.setVisible(false);
 
     missionName = new JTextField();
     missionName.setColumns(10);
     this.add(missionName);
 
     commentLabel = new JLabel("Kommentar");
     this.add(commentLabel);
     commentLabel.setVisible(false);
 
     comment = new JTextField();
    comment.setColumns(15);
     this.add(comment);
 
     isOrgan = new JCheckBox("Orgel-Code", true);
     this.add(isOrgan);
 
     saveButton = new JButton("Speichern");
     this.add(saveButton);
 
     statusButton = new JButton();
     this.add(statusButton);
   }
 
   public void setActive(boolean isActive) {
     if (isActive) {
       statusButton.setText("Deaktivieren");
     } else {
       statusButton.setText("Aktivieren");
     }
     missionName.setEnabled(isActive);
     comment.setEnabled(isActive);
     saveButton.setEnabled(isActive);
     isOrgan.setEnabled(isActive);
   }
 
   public void cleanFields() {
     setMissionName("");
     setComment("");
   }
 
   public Boolean validateFields() {
     if ("".equals(getMissionName())) {
       getParentPanel().showError("Es wurde kein Auftragname eingegeben");
       return false;
     }
     return true;
   }
 
   public void showAsCreateNew() {
     statusButton.setVisible(false);
     commentLabel.setVisible(true);
     nameLabel.setVisible(true);
   }
 
   public void setSaveListener(ActionListener actionListener) {
     saveButton.addActionListener(actionListener);
   }
 
   public void setStatusButtonListener(ActionListener actionListener) {
     statusButton.addActionListener(actionListener);
   }
 
   public String getMissionName() {
     return missionName.getText();
   }
 
   public String getComment() {
     return comment.getText();
   }
 
   public boolean getIsOrgan() {
     return isOrgan.isSelected();
   }
 
   public void setMissionName(String missionName) {
     this.missionName.setText(missionName);
   }
 
   public void setComment(String value) {
     comment.setText(value);
   }
 
   public void setIsOrgan(boolean value) {
     isOrgan.setSelected(value);
   }
 }
