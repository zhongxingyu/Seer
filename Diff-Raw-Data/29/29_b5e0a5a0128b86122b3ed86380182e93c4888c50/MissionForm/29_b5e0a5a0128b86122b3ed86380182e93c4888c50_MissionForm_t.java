 package ch.bli.mez.view.management;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class MissionForm extends JPanel {
 
   private static final long serialVersionUID = -2823140194213618642L;
 
   private JTextField missionName;
   private JTextField comment;
   private JCheckBox isOrgan;
   private JButton saveButton;
   private JButton statusButton;
 
   /**
    * Create the panel.
    */
   public MissionForm(boolean isActive) {
    build();
    setActive(isActive);
  }
  
  private void build(){
     setLayout(new FlowLayout(FlowLayout.LEFT));
 
     JLabel nameLabel = new JLabel("Auftragsname");
     this.add(nameLabel);
 
     missionName = new JTextField();
     missionName.setColumns(10);
     this.add(missionName);
 
     JLabel commentLabel = new JLabel("Kommentar");
     this.add(commentLabel);
 
     comment = new JTextField();
     comment.setColumns(25);
     this.add(comment);
 
     isOrgan = new JCheckBox("Orgel-Code", true);
     this.add(isOrgan);
 
     saveButton = new JButton("Speichern");
     this.add(saveButton);
    
    statusButton = new JButton();
    this.add(statusButton);
 
     addGuiFeatureListener();
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
 
   public boolean validateFields() {
     if (getMissionName().equals("")) {
       return false;
     }
     return true;
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
 
   private void addGuiFeatureListener() {
     KeyListener enterKeyListener = new KeyListener() {
       public void keyTyped(KeyEvent arg0) {
       }
 
       public void keyReleased(KeyEvent arg0) {
       }
 
       public void keyPressed(KeyEvent arg0) {
         if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
           saveButton.doClick();
         }
       }
     };
     missionName.addKeyListener(enterKeyListener);
     comment.addKeyListener(enterKeyListener);
   }
 }
