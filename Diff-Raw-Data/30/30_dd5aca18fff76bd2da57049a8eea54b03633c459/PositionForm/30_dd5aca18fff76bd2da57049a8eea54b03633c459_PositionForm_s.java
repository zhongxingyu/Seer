 package ch.bli.mez.view.management;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import ch.bli.mez.view.DefaultForm;
 
 public class PositionForm extends DefaultForm {
 
   private static final long serialVersionUID = 7585160645657478969L;
 
   private JTextField positionCode;
   private JTextField positionName;
   private JTextField missionName;
   private JTextField comment;
   
   private JLabel numberLabel;
   private JLabel nameLabel;
   private JLabel commentLabel;  
 
   private JButton saveButton;
   private JButton statusButton;
 
   public PositionForm(boolean isActive) {
     build();
     setActive(isActive);
     setEnterKeyListener(saveButton, new JTextField[] { positionCode, positionName, missionName, comment });
   }
 
   private void build() {
     setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
     
     numberLabel = new JLabel("Position");
     this.add(numberLabel);
     positionCode = new JTextField();
     positionCode.setColumns(7);
     this.add(positionCode);
     numberLabel.setVisible(false);
 
     nameLabel = new JLabel("Name");
     this.add(nameLabel);
     positionName = new JTextField();
     positionName.setColumns(10);
     this.add(positionName);
     nameLabel.setVisible(false);
 
     missionName = new JTextField();
     missionName.setColumns(10);
     missionName.setEnabled(false);
     this.add(missionName);
 
     commentLabel = new JLabel("Kommentar");
     this.add(commentLabel);
     comment = new JTextField();
    comment.setColumns(15);
     this.add(comment);
     commentLabel.setVisible(false);
 
     saveButton = new JButton("Speichern");
     this.add(saveButton);
 
     statusButton = new JButton();
     this.add(statusButton);
   }
 
   public void setActive(boolean status) {
     if (status) {
       statusButton.setText("Deaktivieren");
     } else {
       statusButton.setText("Aktivieren");
     }
     positionName.setEnabled(status);
     positionCode.setEnabled(status);
     comment.setEnabled(status);
     saveButton.setEnabled(status);
   }
 
   public void showAsCreateNew() {
     missionName.setVisible(false);
     statusButton.setVisible(false);
     commentLabel.setVisible(true);
     nameLabel.setVisible(true);
     numberLabel.setVisible(true);
   }
 
   public void cleanFields() {
     positionName.setText("");
     positionCode.setText("");
     comment.setText("");
   }
 
   // Getter & Setter
   public void setPositionCode(String code) {
     positionCode.setText(code);
   }
 
   public String getPositionCode() {
     return positionCode.getText();
   }
 
   public void setMissionName(String mission) {
     missionName.setText(mission);
   }
 
   public void setComment(String comment) {
     this.comment.setText(comment);
   }
 
   public String getComment() {
     return comment.getText();
   }
 
   public void setPositionName(String positionName) {
     this.positionName.setText(positionName);
   }
 
   public String getPositionName() {
     return positionName.getText();
   }
 
   // setListeners
   public void setSaveListener(ActionListener actionListener) {
     saveButton.addActionListener(actionListener);
   }
 
   public void setStatusButtonListener(ActionListener actionListener) {
     statusButton.addActionListener(actionListener);
   }
 
   public Boolean validateFields() {
     if ("".equals(getPositionCode())) {
       getParentPanel().showError("Es wurde keine Position eingegeben.");
       return false;
     }
     if ("".equals(getPositionName())) {
       getParentPanel().showError("Es wurde keine Name eingegeben.");
       return false;
     }
     return true;
   }
 }
