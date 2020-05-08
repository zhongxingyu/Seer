 /*
  * jCleaningSchedule - program for printing house cleaning schedules
  * Copyright (C) 2012  Martin Mareš <mmrmartin[at]gmail[dot]com>
  *
  * This file is part of jCleaningSchedule.
  *
  * jCleaningSchedule is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * jCleaningSchedule is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with jCleaningSchedule.  If not, see <http://www.gnu.org/licenses/>.
  */
 package cz.martinmares.jcleaningschedule.gui;
 
 import cz.martinmares.jcleaningschedule.core.JCleaningScheduleData;
 import cz.martinmares.jcleaningschedule.core.Main;
 import java.awt.Color;
 import java.io.IOException;
 import java.util.GregorianCalendar;
 import java.util.ResourceBundle;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.event.ListDataListener;
 import javax.swing.table.DefaultTableModel;
 
 /**
  * The jCleaningSchedule settings frame.
  * @author Martin Mareš
  */
 public class SettingsFrame extends javax.swing.JDialog {
 
     private JCleaningScheduleData data;
     private String floors[][] = new String[5][];
     private int lastFloorId = 1;
     private int minFloor = 1;
     private int numOfPeople = 0;
     private SettingsChangeListener scl;
     private Color ScBgColor = Main.DEFAULT_SC_BG_COLOR;
     private Color ScTxtColor = Main.DEFAULT_SC_TXT_COLOR;
     final JColorChooser jColorChooser = new JColorChooser();
     private static final ResourceBundle str = ResourceBundle.getBundle("cz/martinmares/jcleaningschedule/resources/lang");
     
     /**
      * Creates new form SettingsFrame
      */
     public SettingsFrame(java.awt.Frame parent, boolean modal, 
             JCleaningScheduleData indata) {
         super(parent, modal);
         initComponents();
         jComboBoxWeekStarts.setSelectedItem(str.getString("DAY_FIRST_DAY_OF_WEEK"));
         
         /*
          * See 
          * http://stackoverflow.com/questions/1652942/can-a-jtable-save-data-whenever-a-cell-loses-focus
          * for more info...
          */
         jTableMain.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
         jTableFloor.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
         
         //Loading data
         data = indata;
         reloadSettings();
     }
     
     public void setSettingsChangeListener(SettingsChangeListener scl){
         this.scl = scl;
     }
     
     public SettingsChangeListener getSettingsChangeListener(){
         return scl;
     }
     
     public JCleaningScheduleData getJCleaningScheduleData() {
         return data;
     }
     
     public void setAutoUpdates(boolean val) {
         jCheckBoxUpdates.setSelected(val);
     }
     
     public void reloadSettings() {
         try {
             //Decisive year
             jSpinnerDecisiveYear.setValue(data.getDecisiveYear());
             //Floors
             floors = data.getFloors();
             reloadFloorTableData((int)jSpinnerFloorId.getValue());
             if (floors[0].length!=0) {
                 jCheckBoxGroundFloor.setSelected(true);
             }
             jSpinnerNumFloors.setValue(floors.length-1);
             //Main
             final String main[] = data.getMain();
             ((DefaultTableModel)jTableMain.getModel()).setNumRows(0);
             if(main!=null) {
                 for(int i=0;i<main.length;i++) {
                     ((DefaultTableModel)jTableMain.getModel())
                     .addRow(new Object[]{i+1,main[i]});
                     numOfPeople++;
                 }
             }
             jButtonMainRemove.setEnabled(main.length!=0);
             
             for(int i=0;i<floors.length;i++) {
                 numOfPeople+=floors[i].length;
             }
             
             jComboBoxWeekStarts.setSelectedIndex(data.getWeekStarts());
             ScBgColor = data.getScBgColor();
             ScTxtColor = data.getScTxtColor();
             jTextFieldFont.setText(data.getDefaultFontName());
             jComboBoxWeekStarts.setSelectedIndex(data.getWeekStarts());
             setAutoUpdates(data.isAutoUpdatesEnabled());
             
             
         } catch(NullPointerException ex) {
             //Don't wory about it - its could be firs run of application
             for(int i=0;i<floors.length;i++) { //Working with null arrays make NullPointerException
                 if(floors[i]==null) {
                     floors[i] = new String[0];
                 }
             }
             jSpinnerDecisiveYear.setValue(GregorianCalendar
                     .getInstance().get(GregorianCalendar.YEAR));
             jComboBoxWeekStarts.setSelectedIndex(GregorianCalendar.getInstance()
                     .getFirstDayOfWeek() - GregorianCalendar.SUNDAY);
             jTextFieldFont.setText(Main.DEFAULT_FONT_NAME);
         }
     }
     
     private void saveSettings() {
         try {
             //Main schedule
             String main[] = new String[jTableMain.getRowCount()];
             for(int i=0;i<jTableMain.getRowCount();i++) {
                 main[i] = (String) jTableMain.getValueAt(i,1);
             }
             //Floor schedule - ground floor
             presaveFloorTableData();
             if(!jCheckBoxGroundFloor.isSelected()) {
                 numOfPeople-=floors[0].length;
                 floors[0] = new String[0];
             }
             //Data checking
             if(numOfPeople==0) {
                 JOptionPane.showMessageDialog(this,
                         str.getString("SETTINGS_NO_PEOPLE"),
                         str.getString("DIALOG_ERROR"), JOptionPane.ERROR_MESSAGE);
                jTabbedPaneRoot.setSelectedIndex(1);
                 throw new IllegalArgumentException("I need at least one name of worker");
             }
             
             //Saving to file
             if(data == null) {
                 data = new JCleaningScheduleData((int)jSpinnerDecisiveYear.getValue(), 
                     main, floors);
             } else {
                 data.setData((int)jSpinnerDecisiveYear.getValue(), 
                     main, floors);
             }
             data.setWeekStarts(jComboBoxWeekStarts.getSelectedIndex());
             data.setScBgColor(ScBgColor);
             data.setScTxtColor(ScTxtColor);
             data.setDefaultFontName(jTextFieldFont.getText()); 
             data.setAutoUpdatesEnabled(jCheckBoxUpdates.isSelected());
             data.saveData();
         } catch (IOException ex) {
             System.err.println("There is something wrong with IO:\n"+ 
                     ex.toString());
         }
     }
     
     private void resetFloorIdSpinnerModule() {
         presaveFloorTableData();
         lastFloorId = minFloor;
         if(minFloor>(int)jSpinnerNumFloors.getValue()) {
             floorPanelEnable(false);
             return;
         } else {
             floorPanelEnable(true);
         }    
         jSpinnerFloorId.setModel(new SpinnerNumberModel(minFloor,minFloor,
                 (int)jSpinnerNumFloors.getValue(),1));
         reloadFloorTableData(minFloor);
     }
     
     private void reloadFloorTableData(int floorId) {
         ((DefaultTableModel)jTableFloor.getModel()).setNumRows(0);
         if(floors[floorId].length==0) {
             jButtonFloorRemove.setEnabled(false); //Disable remove floor button
             return;
         } else {
             jButtonFloorRemove.setEnabled(true);
         } //Enable remove floor button
         for(int i=0;i<floors[floorId].length;i++) {
             ((DefaultTableModel)jTableFloor.getModel())
                 .addRow(new Object[]{i+1,floors[floorId][i]});
         }
     }
     
     private void presaveFloorTableData() {
         if(lastFloorId>=floors.length) {
             return; //I don't need save it
         } 
         floors[lastFloorId] = new String[jTableFloor.getRowCount()];
         for(int i=0;i<jTableFloor.getRowCount();i++) {
             floors[lastFloorId][i] = (String) jTableFloor.getValueAt(i,1);
         }
     }
     
     private void floorPanelEnable(boolean en) {
         jButtonFloorAdd.setEnabled(en);
         jTableFloor.setEnabled(en);
         jSpinnerFloorId.setEnabled(en);
         if(!en) {
             jButtonFloorRemove.setEnabled(false);
             ((DefaultTableModel)jTableFloor.getModel()).setNumRows(0);
         }
     }
     
     private void showColorChooser(JButton jButton, Color c, java.awt.event.ActionListener okAction) {
         JDialog colorDialog = JColorChooser.createDialog(jButton, str.getString("SETTINGS_SELECT_A_COLOR"),
                 true, jColorChooser, okAction, null);
         jColorChooser.setColor(c);
         colorDialog.setVisible(true);
     }
     
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jTabbedPaneRoot = new javax.swing.JTabbedPane();
         jPanelGeneral = new javax.swing.JPanel();
         jPanelDecisiveYear = new javax.swing.JPanel();
         jSpinnerDecisiveYear = new javax.swing.JSpinner();
         jLabel2 = new javax.swing.JLabel();
         jPanelFloors = new javax.swing.JPanel();
         jLabel5 = new javax.swing.JLabel();
         jSpinnerNumFloors = new javax.swing.JSpinner();
         jCheckBoxGroundFloor = new javax.swing.JCheckBox();
         jPanelLocale = new javax.swing.JPanel();
         jLabel3 = new javax.swing.JLabel();
         jComboBoxWeekStarts = new javax.swing.JComboBox();
         jPanelMain = new javax.swing.JPanel();
         jLabel4 = new javax.swing.JLabel();
         jScrollPane3 = new javax.swing.JScrollPane();
         jTableMain = new javax.swing.JTable();
         jButtonMainAdd = new javax.swing.JButton();
         jButtonMainRemove = new javax.swing.JButton();
         jPanelFloor = new javax.swing.JPanel();
         jLabel7 = new javax.swing.JLabel();
         jSpinnerFloorId = new javax.swing.JSpinner();
         jButtonFloorAdd = new javax.swing.JButton();
         jButtonFloorRemove = new javax.swing.JButton();
         jScrollPane4 = new javax.swing.JScrollPane();
         jTableFloor = new javax.swing.JTable();
         jLabel1 = new javax.swing.JLabel();
         jPanelOptimal = new javax.swing.JPanel();
         jPanelLook = new javax.swing.JPanel();
         jTextFieldFont = new javax.swing.JTextField();
         jLabel6 = new javax.swing.JLabel();
         jPanelCode = new javax.swing.JPanel();
         jLabel8 = new javax.swing.JLabel();
         jButtonBgColor = new javax.swing.JButton();
         jButtonTxtColor = new javax.swing.JButton();
         jPanel1 = new javax.swing.JPanel();
         jCheckBoxUpdates = new javax.swing.JCheckBox();
         jButtonCancel = new javax.swing.JButton();
         jButtonSave = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("cz/martinmares/jcleaningschedule/resources/lang"); // NOI18N
         setTitle(bundle.getString("SETTINGS")); // NOI18N
 
         jPanelDecisiveYear.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SETTINGS_DECISIVE_YEAR"))); // NOI18N
 
         jSpinnerDecisiveYear.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(2012), null, null, Integer.valueOf(1)));
 
         jLabel2.setText(bundle.getString("SETTINGS_DECISIVE_YEAR_DES")); // NOI18N
 
         javax.swing.GroupLayout jPanelDecisiveYearLayout = new javax.swing.GroupLayout(jPanelDecisiveYear);
         jPanelDecisiveYear.setLayout(jPanelDecisiveYearLayout);
         jPanelDecisiveYearLayout.setHorizontalGroup(
             jPanelDecisiveYearLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelDecisiveYearLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSpinnerDecisiveYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jLabel2)
                 .addContainerGap(130, Short.MAX_VALUE))
         );
         jPanelDecisiveYearLayout.setVerticalGroup(
             jPanelDecisiveYearLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelDecisiveYearLayout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addGroup(jPanelDecisiveYearLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jSpinnerDecisiveYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanelFloors.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SETTINGS_FLOORS"))); // NOI18N
 
         jLabel5.setLabelFor(jSpinnerNumFloors);
         jLabel5.setText(bundle.getString("SETTINGS_NUMBER_OF_FLOORS")); // NOI18N
 
         jSpinnerNumFloors.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(4), Integer.valueOf(0), null, Integer.valueOf(1)));
         jSpinnerNumFloors.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jSpinnerNumFloorsStateChanged(evt);
             }
         });
 
         jCheckBoxGroundFloor.setText(bundle.getString("SETTINGS_GROUND_FLOOR_SUPPORT")); // NOI18N
         jCheckBoxGroundFloor.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jCheckBoxGroundFloorActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanelFloorsLayout = new javax.swing.GroupLayout(jPanelFloors);
         jPanelFloors.setLayout(jPanelFloorsLayout);
         jPanelFloorsLayout.setHorizontalGroup(
             jPanelFloorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelFloorsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelFloorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanelFloorsLayout.createSequentialGroup()
                         .addComponent(jLabel5)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jSpinnerNumFloors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jCheckBoxGroundFloor))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanelFloorsLayout.setVerticalGroup(
             jPanelFloorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelFloorsLayout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addGroup(jPanelFloorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel5)
                     .addComponent(jSpinnerNumFloors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jCheckBoxGroundFloor)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanelLocale.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SETTINGS_LOCALE"))); // NOI18N
 
         jLabel3.setLabelFor(jComboBoxWeekStarts);
         jLabel3.setText(bundle.getString("SETTINGS_WEEK_STARTS")); // NOI18N
 
         jComboBoxWeekStarts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { str.getString("DAY_SUNDAY"), str.getString("DAY_MONDAY"), str.getString("DAY_TUESDAY"), str.getString("DAY_WEDNESDAY"), str.getString("DAY_THURSDAY"), str.getString("DAY_FRIDAY"), str.getString("DAY_SATURDAY") }));
 
         javax.swing.GroupLayout jPanelLocaleLayout = new javax.swing.GroupLayout(jPanelLocale);
         jPanelLocale.setLayout(jPanelLocaleLayout);
         jPanelLocaleLayout.setHorizontalGroup(
             jPanelLocaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelLocaleLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel3)
                 .addGap(33, 33, 33)
                 .addComponent(jComboBoxWeekStarts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(248, Short.MAX_VALUE))
         );
         jPanelLocaleLayout.setVerticalGroup(
             jPanelLocaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelLocaleLayout.createSequentialGroup()
                 .addGroup(jPanelLocaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel3)
                     .addComponent(jComboBoxWeekStarts, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanelGeneralLayout = new javax.swing.GroupLayout(jPanelGeneral);
         jPanelGeneral.setLayout(jPanelGeneralLayout);
         jPanelGeneralLayout.setHorizontalGroup(
             jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelGeneralLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jPanelLocale, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanelDecisiveYear, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanelFloors, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanelGeneralLayout.setVerticalGroup(
             jPanelGeneralLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelGeneralLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanelDecisiveYear, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanelFloors, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanelLocale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(85, Short.MAX_VALUE))
         );
 
         jTabbedPaneRoot.addTab(bundle.getString("SETTINGS_GENERAL"), jPanelGeneral); // NOI18N
 
         jLabel4.setText(bundle.getString("SETTINGS_FILLING_DES")); // NOI18N
 
         jTableMain.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 bundle.getString("SETTINGS_ORDER"), bundle.getString("SETTINGS_NAME") // NOI18N
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, true
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTableMain.getTableHeader().setReorderingAllowed(false);
         jScrollPane3.setViewportView(jTableMain);
 
         jButtonMainAdd.setText(bundle.getString("SETTINGS_ADD_NAME")); // NOI18N
         jButtonMainAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonMainAddActionPerformed(evt);
             }
         });
 
         jButtonMainRemove.setText(bundle.getString("SETTINGS_REMOVE_LAST_NAME")); // NOI18N
         jButtonMainRemove.setEnabled(false);
         jButtonMainRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonMainRemoveActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanelMainLayout = new javax.swing.GroupLayout(jPanelMain);
         jPanelMain.setLayout(jPanelMainLayout);
         jPanelMainLayout.setHorizontalGroup(
             jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelMainLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                     .addGroup(jPanelMainLayout.createSequentialGroup()
                         .addComponent(jLabel4)
                         .addGap(0, 1, Short.MAX_VALUE))
                     .addGroup(jPanelMainLayout.createSequentialGroup()
                         .addComponent(jButtonMainAdd)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jButtonMainRemove)))
                 .addContainerGap())
         );
         jPanelMainLayout.setVerticalGroup(
             jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelMainLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButtonMainAdd)
                     .addComponent(jButtonMainRemove))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneRoot.addTab(bundle.getString("MAIN_MAIN_SCHEDULE"), jPanelMain); // NOI18N
 
         jLabel7.setText(jLabel4.getText());
 
         jSpinnerFloorId.setModel(new javax.swing.SpinnerNumberModel(1, 1, 4, 1));
         jSpinnerFloorId.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 onFloorIdChange(evt);
             }
         });
 
         jButtonFloorAdd.setText(bundle.getString("SETTINGS_ADD_NAME")); // NOI18N
         jButtonFloorAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonFloorAddActionPerformed(evt);
             }
         });
 
         jButtonFloorRemove.setText(bundle.getString("SETTINGS_REMOVE_LAST_NAME")); // NOI18N
         jButtonFloorRemove.setEnabled(false);
         jButtonFloorRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonFloorRemoveActionPerformed(evt);
             }
         });
 
         jTableFloor.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             new String [] {
                 bundle.getString("SETTINGS_ORDER"), bundle.getString("SETTINGS_NAME") // NOI18N
             }
         ) {
             Class[] types = new Class [] {
                 java.lang.Integer.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, true
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         jTableFloor.getTableHeader().setReorderingAllowed(false);
         jScrollPane4.setViewportView(jTableFloor);
 
         jLabel1.setFont(new java.awt.Font("Cantarell", 1, 15)); // NOI18N
         jLabel1.setText(bundle.getString("SETTINGS_FLOOR")); // NOI18N
 
         javax.swing.GroupLayout jPanelFloorLayout = new javax.swing.GroupLayout(jPanelFloor);
         jPanelFloor.setLayout(jPanelFloorLayout);
         jPanelFloorLayout.setHorizontalGroup(
             jPanelFloorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelFloorLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelFloorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                     .addGroup(jPanelFloorLayout.createSequentialGroup()
                         .addComponent(jLabel7)
                         .addGap(0, 1, Short.MAX_VALUE))
                     .addGroup(jPanelFloorLayout.createSequentialGroup()
                         .addComponent(jButtonFloorAdd)
                         .addGap(41, 41, 41)
                         .addComponent(jLabel1)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jSpinnerFloorId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(jButtonFloorRemove)))
                 .addContainerGap())
         );
         jPanelFloorLayout.setVerticalGroup(
             jPanelFloorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelFloorLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanelFloorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButtonFloorAdd)
                     .addComponent(jButtonFloorRemove)
                     .addComponent(jSpinnerFloorId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jLabel1))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jTabbedPaneRoot.addTab(bundle.getString("SETTINGS_FLOORS_SCHEDULING"), jPanelFloor); // NOI18N
 
         jPanelLook.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SETTINGS_LOOK_AND_FEEL"))); // NOI18N
 
         jTextFieldFont.setText("Arial");
 
         jLabel6.setText(bundle.getString("SETTINGS_DEFAULT_FONT_NAME")); // NOI18N
 
         jPanelCode.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("SETTINGS_COLORS"))); // NOI18N
 
         jLabel8.setLabelFor(jButtonTxtColor);
         jLabel8.setText(bundle.getString("SETTINGS_SECOND_ROWS")); // NOI18N
         jLabel8.setToolTipText("");
 
         jButtonBgColor.setText(bundle.getString("SETTINGS_BACKGROUND")); // NOI18N
         jButtonBgColor.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonBgColorActionPerformed(evt);
             }
         });
 
         jButtonTxtColor.setText(bundle.getString("SETTINGS_TEXT")); // NOI18N
         jButtonTxtColor.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonTxtColorActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout jPanelCodeLayout = new javax.swing.GroupLayout(jPanelCode);
         jPanelCode.setLayout(jPanelCodeLayout);
         jPanelCodeLayout.setHorizontalGroup(
             jPanelCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelCodeLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel8)
                 .addGap(18, 18, 18)
                 .addComponent(jButtonTxtColor)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jButtonBgColor)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanelCodeLayout.setVerticalGroup(
             jPanelCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelCodeLayout.createSequentialGroup()
                 .addGroup(jPanelCodeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel8)
                     .addComponent(jButtonBgColor)
                     .addComponent(jButtonTxtColor))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanelLookLayout = new javax.swing.GroupLayout(jPanelLook);
         jPanelLook.setLayout(jPanelLookLayout);
         jPanelLookLayout.setHorizontalGroup(
             jPanelLookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelLookLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelLookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(jPanelLookLayout.createSequentialGroup()
                         .addComponent(jPanelCode, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addContainerGap())
                     .addGroup(jPanelLookLayout.createSequentialGroup()
                         .addComponent(jLabel6)
                         .addGap(18, 18, 18)
                         .addComponent(jTextFieldFont, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                         .addGap(163, 163, 163))))
         );
         jPanelLookLayout.setVerticalGroup(
             jPanelLookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelLookLayout.createSequentialGroup()
                 .addGroup(jPanelLookLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jLabel6)
                     .addComponent(jTextFieldFont, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(jPanelCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(14, 14, 14))
         );
 
         jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("LICENSE_INTERNET_ACCESS"))); // NOI18N
 
         jCheckBoxUpdates.setSelected(Main.DEFAULT_AUTO_UPDATES_ENABLED);
         jCheckBoxUpdates.setText(bundle.getString("SETTINGS_AUTO_UPDATES")); // NOI18N
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jCheckBoxUpdates)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jCheckBoxUpdates)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         javax.swing.GroupLayout jPanelOptimalLayout = new javax.swing.GroupLayout(jPanelOptimal);
         jPanelOptimal.setLayout(jPanelOptimalLayout);
         jPanelOptimalLayout.setHorizontalGroup(
             jPanelOptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOptimalLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanelOptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(jPanelLook, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanelOptimalLayout.setVerticalGroup(
             jPanelOptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanelOptimalLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jPanelLook, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(113, Short.MAX_VALUE))
         );
 
         jTabbedPaneRoot.addTab(bundle.getString("SETTINGS_OPTIMAL"), jPanelOptimal); // NOI18N
 
         jButtonCancel.setText(bundle.getString("DIALOG_CANCEL")); // NOI18N
         jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonCancelActionPerformed(evt);
             }
         });
 
         jButtonSave.setText(bundle.getString("DIALOG_SAVE")); // NOI18N
         jButtonSave.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButtonSaveActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGap(0, 0, Short.MAX_VALUE)
                         .addComponent(jButtonCancel)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButtonSave))
                     .addComponent(jTabbedPaneRoot))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jTabbedPaneRoot)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(jButtonSave)
                     .addComponent(jButtonCancel))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButtonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCancelActionPerformed
         /*
          * This window can be showen after first run, so it need a diferent 
          * handling for a diferent situations.
          * see Main.showSettingsFrame() for more info...
          */       
         this.getWindowListeners()[0].windowClosing(null);
     }//GEN-LAST:event_jButtonCancelActionPerformed
 
     private void jButtonSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSaveActionPerformed
         saveSettings();
         scl.onSettingsChange();
         this.setVisible(false);
     }//GEN-LAST:event_jButtonSaveActionPerformed
 
     private void jSpinnerNumFloorsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinnerNumFloorsStateChanged
         //I need resize original array
         String new_floors[][] = new String[(int)jSpinnerNumFloors.getValue()+1][]; //One for ground floor
         for(int i=0;i<new_floors.length;i++) {
             if(i<floors.length) {
                 new_floors[i] = floors[i]; //Copy oldones to new array
             } 
             else {
                 new_floors[i] = new String[0];
             }
         }
         floors = new_floors;
         //I need to change restrictions
         resetFloorIdSpinnerModule();
     }//GEN-LAST:event_jSpinnerNumFloorsStateChanged
 
     private void onFloorIdChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_onFloorIdChange
         //Save new data into RAM
         presaveFloorTableData();
         //Reload previos data from memory
         lastFloorId = (int) jSpinnerFloorId.getValue();
         reloadFloorTableData(lastFloorId);
     }//GEN-LAST:event_onFloorIdChange
 
     private void jButtonMainRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMainRemoveActionPerformed
         final int rows = jTableMain.getRowCount();
         if(rows == 1) {
             jButtonMainRemove.setEnabled(false); //Disable this button
         } 
         ((DefaultTableModel)jTableMain.getModel()).removeRow(rows-1);
         numOfPeople--;
     }//GEN-LAST:event_jButtonMainRemoveActionPerformed
 
     private void jButtonMainAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonMainAddActionPerformed
         jButtonMainRemove.setEnabled(true);
         ((DefaultTableModel)jTableMain.getModel())
                 .addRow(new Object[]{jTableMain.getRowCount()+1,""});
         numOfPeople++;
         
     }//GEN-LAST:event_jButtonMainAddActionPerformed
 
     private void jCheckBoxGroundFloorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGroundFloorActionPerformed
         if(jCheckBoxGroundFloor.isSelected()) {
             minFloor = 0;
         }
         else {
             minFloor = 1;
         }
         resetFloorIdSpinnerModule();
     }//GEN-LAST:event_jCheckBoxGroundFloorActionPerformed
 
     private void jButtonFloorAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFloorAddActionPerformed
         jButtonFloorRemove.setEnabled(true);
         ((DefaultTableModel)jTableFloor.getModel())
                 .addRow(new Object[]{jTableFloor.getRowCount()+1,""});
         numOfPeople++;
     }//GEN-LAST:event_jButtonFloorAddActionPerformed
 
     private void jButtonFloorRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFloorRemoveActionPerformed
         final int rows = jTableFloor.getRowCount();
         if(rows == 1) {
             jButtonFloorRemove.setEnabled(false);
         } //Disable this button
         ((DefaultTableModel)jTableFloor.getModel()).removeRow(rows-1);
         numOfPeople--;
     }//GEN-LAST:event_jButtonFloorRemoveActionPerformed
 
     private void jButtonBgColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBgColorActionPerformed
         showColorChooser(jButtonBgColor, ScBgColor, new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ScBgColor = jColorChooser.getColor();
             }
         });   
     }//GEN-LAST:event_jButtonBgColorActionPerformed
 
     private void jButtonTxtColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonTxtColorActionPerformed
         showColorChooser(jButtonTxtColor, ScTxtColor, new java.awt.event.ActionListener() {
             @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ScTxtColor = jColorChooser.getColor();
             }
         });      
     }//GEN-LAST:event_jButtonTxtColorActionPerformed
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButtonBgColor;
     private javax.swing.JButton jButtonCancel;
     private javax.swing.JButton jButtonFloorAdd;
     private javax.swing.JButton jButtonFloorRemove;
     private javax.swing.JButton jButtonMainAdd;
     private javax.swing.JButton jButtonMainRemove;
     private javax.swing.JButton jButtonSave;
     private javax.swing.JButton jButtonTxtColor;
     private javax.swing.JCheckBox jCheckBoxGroundFloor;
     private javax.swing.JCheckBox jCheckBoxUpdates;
     private javax.swing.JComboBox jComboBoxWeekStarts;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanelCode;
     private javax.swing.JPanel jPanelDecisiveYear;
     private javax.swing.JPanel jPanelFloor;
     private javax.swing.JPanel jPanelFloors;
     private javax.swing.JPanel jPanelGeneral;
     private javax.swing.JPanel jPanelLocale;
     private javax.swing.JPanel jPanelLook;
     private javax.swing.JPanel jPanelMain;
     private javax.swing.JPanel jPanelOptimal;
     private javax.swing.JScrollPane jScrollPane3;
     private javax.swing.JScrollPane jScrollPane4;
     private javax.swing.JSpinner jSpinnerDecisiveYear;
     private javax.swing.JSpinner jSpinnerFloorId;
     private javax.swing.JSpinner jSpinnerNumFloors;
     private javax.swing.JTabbedPane jTabbedPaneRoot;
     private javax.swing.JTable jTableFloor;
     private javax.swing.JTable jTableMain;
     private javax.swing.JTextField jTextFieldFont;
     // End of variables declaration//GEN-END:variables
 }
