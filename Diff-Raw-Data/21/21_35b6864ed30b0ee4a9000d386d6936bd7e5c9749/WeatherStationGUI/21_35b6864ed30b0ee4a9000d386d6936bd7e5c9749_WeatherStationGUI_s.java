 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.event.*;
 import java.awt.Dimension;
 
 public class WeatherStationGUI extends JFrame{
     private static final int UPDATE_MIN = 0;
     private static final int UPDATE_MAX = 30;
     private static final int UPDATE_INIT = 1;
     private WeatherStation weatherStation = new WeatherStation();
     private JCheckBox jCheckBox1;
     private JCheckBox jCheckBox3;
     private JComboBox jComboBox1;
     private JLabel jLabel1;
     private JLabel jLabel10;
     private JLabel jLabel2;
     private JLabel jLabel3;
     private JLabel jLabel4;
     private JLabel jLabel5;
     private JLabel jLabel6;
     private JLabel jLabel7;
     private JLabel jLabel8;
     private JLabel jLabel9;
     private JPanel jPanel1;
     private JPanel jPanel2;
     private JPanel jPanel3;
     private JPanel jPanel4;
     private JSlider jSlider1;
     private JTabbedPane jTabbedPane1;
     private JTextField jTextField1;
     private JTextField jTextField2;
     private JTextField jTextField3;
     private Timer updateTimer; 
 
     /**
      * Creates new form WeatherStationGUI
      */
     public WeatherStationGUI() {
         initComponents();
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      */
     private void initComponents() {
         this.setTitle("Weatherstation");
         weatherStation.update();
         jTabbedPane1 = new  JTabbedPane();
         jPanel2 = new JPanel();
         jLabel1 = new JLabel();
         jComboBox1 = new JComboBox();
         jLabel2 = new JLabel();
         jTextField1 = new JTextField();
         jPanel4 = new JPanel();
         jLabel3 = new JLabel();
         jLabel4 = new JLabel();
         jTextField2 = new JTextField();
         jTextField3 = new JTextField();
         jCheckBox1 = new JCheckBox();
         jCheckBox3 = new JCheckBox();
         jSlider1 = new JSlider(JSlider.HORIZONTAL,
             UPDATE_MIN,UPDATE_MAX, UPDATE_INIT);
         jPanel1 = new JPanel();
         jLabel5 = new JLabel();
         jPanel3 = new JPanel();
         jLabel6 = new JLabel();
         jLabel7 = new JLabel();
         jLabel10 = new JLabel();
         jLabel8 = new JLabel();
         jLabel9 = new JLabel();
 
         setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE);
 
         jLabel1.setText("Stadtname");
 
         jComboBox1.setModel(new DefaultComboBoxModel(new String[] { "Berlin", "Frankfurt", "Hamburg", "Kapstadt", "Karlsruhe", "Koeln", 
                     "Mannheim", "Muenchen", "Rio de Janeiro", "Rom", "Speyer" }));
         jComboBox1.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt) {
                     jComboBox1ActionPerformed(evt);
                 }
             });
 
         jLabel2.setText("Citycode");
 
         jTextField1.setText("Citycode eingben...");
         jTextField1.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt){
                     jTextField1ActionPerformed(evt);
                 }
             });
         jTextField1.addFocusListener(new FocusListener(){
                 public void focusGained(FocusEvent evt){
                     jTextField1FocusGained(evt);
                 }
 
                 public void focusLost(FocusEvent evt){
                 }
             });
 
         jLabel3.setText("Hostname");
         jLabel3.setEnabled(false);
 
         jLabel4.setText("Port");
         jLabel4.setEnabled(false);
 
         jTextField2.setText("Host");
         jTextField2.setEnabled(false);
         jTextField2.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt){
                     jTextField1ActionPerformed(evt);
                 }
             });
         jTextField2.addFocusListener(new FocusListener(){
                 public void focusGained(FocusEvent evt){
                     jTextField2FocusGained(evt);
                 }
 
                 public void focusLost(FocusEvent evt){
                 }
             });
 
         jTextField3.setText("Port");
         jTextField3.setEnabled(false);
         jTextField3.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent evt){
                     jTextField3ActionPerformed(evt);
                 }
             });
         jTextField1.addFocusListener(new FocusListener(){
                 public void focusGained(FocusEvent evt){
                     jTextField3FocusGained(evt);
                 }
 
                 public void focusLost(FocusEvent evt){
                 }
             });
 
         GroupLayout jPanel4Layout = new  GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .
                 addContainerGap()
                 .
 
                 addGroup(jPanel4Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                     .
                     addComponent(jLabel3)
                     .
 
                     addComponent(jLabel4))
                 .
                 addGroup(jPanel4Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                     .
                     addGroup(jPanel4Layout.createSequentialGroup()
                         .
                         addPreferredGap( LayoutStyle.ComponentPlacement.RELATED,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .
                         addComponent(jTextField3,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE))
                     .
                     addGroup( GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                         .
                         addPreferredGap( LayoutStyle.ComponentPlacement.RELATED,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .
                         addComponent(jTextField2,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)))
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .
                 addGroup(jPanel4Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                     .
                     addComponent(jTextField2,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                     .
                     addComponent(jLabel3))
                 .
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED)
                 .
 
                 addGroup(jPanel4Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                     .
                     addComponent(jLabel4)
                     .
 
                     addComponent(jTextField3,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)))
         );
 
         jCheckBox1.setText("Use Proxy");
         jCheckBox1.setSelected(false);
         jCheckBox1.addItemListener(new ItemListener(){
                 public void itemStateChanged(ItemEvent evt){
                     jCheckBox1ItemStateChanged(evt);
                 }
             });
 
         jCheckBox3.setText("Automatische Updates");
         jCheckBox3.setSelected(true);
         jCheckBox3.addItemListener(new ItemListener(){
                 public void itemStateChanged(ItemEvent evt){
                     jCheckBox3ItemStateChanged(evt);
                 }
             });
 
         jSlider1.addChangeListener(new ChangeListener(){
                 public void stateChanged(ChangeEvent evt) {
                     jSlider1StateChanged(evt);
                 }
             });
         jSlider1.setMajorTickSpacing(10);
         jSlider1.setMinorTickSpacing(1);
         jSlider1.setPaintTicks(true);
         jSlider1.setPaintLabels(true);
 
         GroupLayout jPanel2Layout = new  GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .
                 addComponent(jCheckBox1)
                 .
 
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .
             addGroup(jPanel2Layout.createSequentialGroup()
                 .
                 addGroup(jPanel2Layout.createParallelGroup( GroupLayout.Alignment.TRAILING, false)
                     .
                     addComponent(jPanel4,  GroupLayout.DEFAULT_SIZE,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .
                     addGroup(jPanel2Layout.createSequentialGroup()
                         .
                         addComponent(jCheckBox3)
                         .
 
                         addPreferredGap( LayoutStyle.ComponentPlacement.RELATED,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .
                         addComponent(jSlider1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE))
                     .
                     addGroup(jPanel2Layout.createSequentialGroup()
                         .
                         addContainerGap()
                         .
 
                         addGroup(jPanel2Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                             .
                             addComponent(jLabel1)
                             .
 
                             addComponent(jLabel2))
                         .
                         addGap(94, 94, 94)
                         .
                         addGroup(jPanel2Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                             .
                             addComponent(jTextField1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                             .
                             addComponent(jComboBox1,  GroupLayout.PREFERRED_SIZE, 259,  GroupLayout.PREFERRED_SIZE))))
                 .
                 addContainerGap(16, Short.MAX_VALUE))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .
                 addContainerGap()
                 .
 
                 addGroup(jPanel2Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                     .
                     addComponent(jLabel1)
                     .
 
                     addComponent(jComboBox1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE))
                 .
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED)
                 .
 
                 addGroup(jPanel2Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                     .
                     addComponent(jLabel2)
                     .
 
                     addComponent(jTextField1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE))
                 .
                 addPreferredGap( LayoutStyle.ComponentPlacement.UNRELATED)
                 .
 
                 addGroup(jPanel2Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                     .
                     addComponent(jCheckBox3)
                     .
 
                     addComponent(jSlider1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE))
                 .
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addComponent(jCheckBox1)
                 .
 
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED)
                 .
 
                 addComponent(jPanel4,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                 .
                 addContainerGap())
         );
 
         jTabbedPane1.addTab("Einstellungen", jPanel2);
 
         jLabel5.setText(weatherStation.getCityName());
 
         jLabel6.setIcon(weatherStation.getIcon());
 
         jLabel7.setText(String.valueOf(weatherStation.getHumidity()));
 
         jLabel10.setText(weatherStation.getSunset());
 
         jLabel8.setText(String.valueOf(weatherStation.getTemperature()));
 
         jLabel9.setText(weatherStation.getSunrise());
 
         GroupLayout jPanel3Layout = new  GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addGroup(jPanel3Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                     .
                     addComponent(jLabel9)
                     .
 
                     addComponent(jLabel8))
                 .
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED, 170, Short.MAX_VALUE)
                 .
                 addGroup(jPanel3Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
                     .
                     addComponent(jLabel10)
                     .
 
                     addComponent(jLabel7))
                 .
                 addContainerGap())
             .
             addGroup(jPanel3Layout.createSequentialGroup()
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addComponent(jLabel6)
                 .
 
                 addGap(0, 0, Short.MAX_VALUE))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addGroup(jPanel3Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                     .
                     addComponent(jLabel8,  GroupLayout.PREFERRED_SIZE, 24,  GroupLayout.PREFERRED_SIZE)
                     .
                     addComponent(jLabel7))
                 .
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED, 45, Short.MAX_VALUE)
                 .
                 addComponent(jLabel6)
                 .
 
                 addPreferredGap( LayoutStyle.ComponentPlacement.RELATED, 66, Short.MAX_VALUE)
                 .
                 addGroup(jPanel3Layout.createParallelGroup( GroupLayout.Alignment.BASELINE)
                     .
                     addComponent(jLabel9)
                     .
 
                     addComponent(jLabel10))
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         GroupLayout jPanel1Layout = new  GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup( GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .
                 addContainerGap()
                 .
 
                 addComponent(jPanel3,  GroupLayout.DEFAULT_SIZE,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addContainerGap())
             .
             addGroup(jPanel1Layout.createSequentialGroup()
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addComponent(jLabel5,  GroupLayout.PREFERRED_SIZE, 76,  GroupLayout.PREFERRED_SIZE)
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .
                 addComponent(jLabel5,  GroupLayout.PREFERRED_SIZE, 21,  GroupLayout.PREFERRED_SIZE)
                 .
                 addGap(12, 12, 12)
                 .
                 addComponent(jPanel3,  GroupLayout.DEFAULT_SIZE,  GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addContainerGap())
         );
 
         jTabbedPane1.addTab("Wetterstation", jPanel1);
 
         GroupLayout layout = new  GroupLayout(getContentPane());
         getContentPane().
 
         setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addComponent(jTabbedPane1,  GroupLayout.PREFERRED_SIZE,  GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE)
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup( GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .
                 addComponent(jTabbedPane1)
                 .
 
                 addContainerGap( GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
 
         timerInit(UPDATE_INIT);
     }
 
     public void timerInit(int upm){
         ActionListener updatesPerMinutes = new ActionListener(){
                 public void actionPerformed(ActionEvent evt){
                     weatherStation.update();
                     updateLabels();
                     JOptionPane.showMessageDialog(null, "Debugging");
                 }
             };
 
        upm *= 60 * 60 * 1000;
         updateTimer = new Timer(upm,updatesPerMinutes);
         if(upm >0)
        updateTimer.start();
     }
 
     public void updateLabels(){
         jLabel5.setText(weatherStation.getCityName());
         jLabel6.setIcon(weatherStation.getIcon());
         jLabel7.setText(String.valueOf(weatherStation.getHumidity()));
         jLabel10.setText(weatherStation.getSunset());
         jLabel8.setText(String.valueOf(weatherStation.getTemperature()));
         jLabel9.setText(weatherStation.getSunrise());
     }
 
     //Proxy
     private void jCheckBox1ItemStateChanged(ItemEvent evt){
         if(evt.getStateChange() == ItemEvent.DESELECTED){
             jLabel3.setEnabled(false);
             jLabel4.setEnabled(false);
             jTextField2.setEnabled(false);
             jTextField3.setEnabled(false);
         }else{
             jLabel3.setEnabled(true);
             jLabel4.setEnabled(true);
             jTextField2.setEnabled(true);
             jTextField3.setEnabled(true);
         }
     }
 
     //Updates
     private void jCheckBox3ItemStateChanged(ItemEvent evt) {
         if(evt.getStateChange() == ItemEvent.DESELECTED){
             jSlider1.setEnabled(false);
            updateTimer.start();
         }else{
             jSlider1.setEnabled(true);
            updateTimer.stop();
         }
     }
 
     private void jComboBox1ActionPerformed(ActionEvent evt) {
         JComboBox cb = (JComboBox) evt.getSource();
         String choice = (String) cb.getSelectedItem();
         weatherStation.setCityName(choice);
         weatherStation.update();
         updateLabels();
     }
 
     private void jTextField1ActionPerformed(ActionEvent evt){
         String cityCode = jTextField1.getText();
         weatherStation.setCityCode(cityCode);
         weatherStation.update();
         updateLabels();
     }
 
     private void jTextField1FocusGained(FocusEvent evt){
         jTextField1.setText("");
     }
 
     private void jTextField2ActionPerformed(ActionEvent evt){
         String host = jTextField2.getText();
         weatherStation.setProxyHost(host);
     }
 
     private void jTextField2FocusGained(FocusEvent evt){
         jTextField2.setText("");
     }
 
     private void jTextField3ActionPerformed(ActionEvent evt){
         String port = jTextField2.getText();
         try
         {
             int portAsInt= Integer.parseInt(port);
             weatherStation.setProxyPort(portAsInt);
         }
         catch(NumberFormatException nfe)
         {
             jTextField3.setText("Ungültiger Wert");
         }
     }
 
     private void jTextField3FocusGained(FocusEvent evt){
         jTextField3.setText("");
     }
 
     private void jSlider1StateChanged(ChangeEvent evt){
         JSlider source = (JSlider)evt.getSource();
         if (!source.getValueIsAdjusting()) {
             int upm = (int)source.getValue();
             updateTimer.stop();
             System.gc();
             timerInit(upm);
             System.out.println(upm);
         }
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         try {
             for ( UIManager.LookAndFeelInfo info :  UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(WeatherStationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(WeatherStationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(WeatherStationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch ( UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(WeatherStationGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
 
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
                 public void run() {
                     new WeatherStationGUI().setVisible(true);
                 }
             });
     }
 }
