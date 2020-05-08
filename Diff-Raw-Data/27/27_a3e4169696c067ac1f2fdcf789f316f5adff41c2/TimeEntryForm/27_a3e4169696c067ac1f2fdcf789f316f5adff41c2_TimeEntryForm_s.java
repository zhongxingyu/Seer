 package ch.bli.mez.view.time;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 import java.util.Calendar;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import ch.bli.mez.util.Parser;
 import ch.bli.mez.view.DefaultForm;
 
 public class TimeEntryForm extends DefaultForm {
   private static final long serialVersionUID = 9171774652563879025L;
 
   private JTextField date;
   private JTextField missionName;
   private JTextField positionCode;
   private JTextField worktime;
   private JButton saveButton;
   private JButton deleteButton;
   private JLabel dateLabel;
   private JLabel positionLabel;
   private JLabel auftragLabel;
   private JLabel zeitLabel;
   private JPanel datePanel;
   private JPanel positionPanel;
   private JPanel missionPanel;
   private JPanel timePanel;
 
   public TimeEntryForm() {
     build();
     setEnterKeyListener(saveButton, new JTextField[] {missionName, worktime, positionCode, date});
   }
 
   private void build() {
     setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
 
     datePanel = new JPanel();
     add(datePanel);
 
     dateLabel = new JLabel("Datum");
     datePanel.add(dateLabel);
 
     date = new JTextField();
     date.setToolTipText("Datumsformat: 01.01.2014 oder 01.01.14");
     datePanel.add(date);
     date.setColumns(10);
 
     missionPanel = new JPanel();
     add(missionPanel);
 
     auftragLabel = new JLabel("Auftrag");
     missionPanel.add(auftragLabel);
 
     missionName = new JTextField();
     missionPanel.add(missionName);
     missionName.setColumns(10);
 
     positionPanel = new JPanel();
     add(positionPanel);
 
     positionLabel = new JLabel("Position");
     positionPanel.add(positionLabel);
 
     positionCode = new JTextField();
     positionCode.setToolTipText("Position ist vom Auftrag abhängig.");
     positionPanel.add(positionCode);
     positionCode.setColumns(10);
 
     timePanel = new JPanel();
     add(timePanel);
 
     zeitLabel = new JLabel("Zeit");
     timePanel.add(zeitLabel);
 
     worktime = new JTextField();
     timePanel.add(worktime);
     worktime.setColumns(7);
 
     saveButton = new JButton("Speichern");
     add(saveButton);
 
     deleteButton = new JButton("Löschen");
     add(deleteButton);
 
     dateLabel.setVisible(false);
     positionLabel.setVisible(false);
     auftragLabel.setVisible(false);
     zeitLabel.setVisible(false);
   }
 
   public void showAsCreateNew() {
     deleteButton.setVisible(false);
     dateLabel.setVisible(true);
     positionLabel.setVisible(true);
     auftragLabel.setVisible(true);
     zeitLabel.setVisible(true);
   }
 
   public void setSaveListener(ActionListener actionListener) {
     saveButton.addActionListener(actionListener);
   }
 
   public void setDeleteListener(ActionListener actionListener) {
     deleteButton.addActionListener(actionListener);
   }
 
   public void cleanFields() {
     worktime.setText("");
   }
 
  public Calendar getDate() {
    return Parser.parseDateStringToCalendar(date.getText());
   }
 
   public String getPositionCode() {
     return positionCode.getText();
   }
 
   public String getMissionName() {
     return missionName.getText();
   }
 
   public Integer getWorktime() {
     return Parser.parseMinuteStringToInteger(worktime.getText());
   }
 
   public void setDate(Calendar calendar) {
     this.date.setText(Parser.parseDateCalendarToString(calendar));
   }
 
   public void setPosition(String position) {
     this.positionCode.setText(position);
   }
 
   public void setMission(String mission) {
     this.missionName.setText(mission);
   }
 
   public void setWorktime(Integer worktime) {
     this.worktime.setText(Parser.parseMinutesIntegerToString(worktime));
   }
 
   public Boolean validateFields() {
    if (getDate().equals("")) {
       getParentPanel().showError("Es muss ein Datum eingegeben werden.");
       return false;
     }
     if (getMissionName().equals("")) {
       getParentPanel().showError("Es muss ein Auftrag eingegeben werden.");
       return false;
     }
     if (getPositionCode().equals("")) {
       getParentPanel().showError("Es muss eine Position angegeben werden.");
       return false;
     }
     if (worktime.getText().equals("")
         || (worktime.getText().matches("[0-9]*[:,.]{1}[0-9]{2}") || worktime.getText().matches("[0-9]*")) == false) {
       getParentPanel().showError("Die Zeit muss korrekt angegeben werden (hh:mm oder hh.mm)");
       return false;
     }
     return true;
   }
 }
