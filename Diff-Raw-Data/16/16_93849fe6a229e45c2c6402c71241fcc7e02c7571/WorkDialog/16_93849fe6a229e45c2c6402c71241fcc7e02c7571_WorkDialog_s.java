 package be.artesis.timelog.gui;
 
 import be.artesis.timelog.clock.*;
 import be.artesis.timelog.controller.Inserter;
 import be.artesis.timelog.model.Validator;
 import be.artesis.timelog.model.WebserviceException;
 import be.artesis.timelog.view.DataInputException;
 import be.artesis.timelog.view.Taak;
 import be.artesis.timelog.view.Tijdspanne;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 
 /**
  * @author Gilliam
  */
 public class WorkDialog extends javax.swing.JDialog {
 
     private Clock clock;
     private Timer timer;
     private TimerTask updater;
    long startTime, stopTime, pauseStart, pauseStop, totalPause;
     Taak currentTask;
     boolean firstRun, minimized;
     Dimension timerSize, frameSize;
     ImageIcon minIcon, maxIcon;
     ArrayList<Tijdspanne> pause;
     Validator validator;
     JLabel minimizedTimerJLabel;
     Point location, minLocation;
 
     public WorkDialog(java.awt.Frame parent, boolean modal, Validator validator) {
         super(parent, modal);
         setLocationRelativeTo(parent);
         initComponents();
         initMyComponents();
         this.validator = validator;
     }
 
     private void initMyComponents() {
         minimizedTimerJLabel = new JLabel();
         minimizedTimerJLabel.setBackground(new java.awt.Color(255, 255, 255));
         minimizedTimerJLabel.setFont(new java.awt.Font("Tahoma", 0, 18));
         minimizedTimerJLabel.setForeground(new java.awt.Color(0, 255, 255));
         minimizedTimerJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         minimizedTimerJLabel.setText("00 : 00 : 00");
         minimizedTimerJLabel.setToolTipText("Click to start/pauze");
         minimizedTimerJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
         minimizedTimerJLabel.setSize(new java.awt.Dimension(150, 37));
         minimizedTimerJLabel.setLocation(minimizeJButton.getWidth() + 30, 10);
         minimizedTimerJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 startPauseJButtonMouseClicked(evt);
             }
         });;
         minimizedTimerJLabel.setVisible(false);
         this.getContentPane().add(minimizedTimerJLabel);
         getContentPane().setBackground(Color.darkGray);
 
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         double width = screenSize.getWidth();
         minLocation = new Point((int) width / 2 - 50, 10);
 
         clock = new Clock();
         timer = new Timer("Clock updater");
         firstRun = true;
         minimized = false;
         pause = new ArrayList<Tijdspanne>();
         timerSize = minimizedTimerJLabel.getSize();
         frameSize = this.getSize();
 
         minIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/be/artesis/timelog/gui/icons/MinimizeIcon.png")).getScaledInstance(minimizeJButton.getWidth() - 5, minimizeJButton.getHeight() - 5, java.awt.Image.SCALE_DEFAULT));
         maxIcon = new ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/be/artesis/timelog/gui/icons/MaximizeIcon.png")).getScaledInstance(minimizeJButton.getWidth() - 5, minimizeJButton.getHeight() - 5, java.awt.Image.SCALE_DEFAULT));
         minimizeJButton.setIcon(minIcon);
         minimizeJButton.setFocusPainted(false);
         minimizeJButton.setVerticalAlignment(SwingConstants.CENTER);
         minimizeJButton.setHorizontalAlignment(SwingConstants.CENTER);
 
         setDefaultCloseOperation(javax.swing.JFrame.DO_NOTHING_ON_CLOSE);
 
         try {
             for (Taak task : UserControl.getCurrentProject().getTaken()) {
                 if (!task.overTijd()) {
                     tasksJComboBox.addItem(task);
                 }
             }
         } catch (GUIException ex) {
             JOptionPane.showMessageDialog(this, ex.getMessage());
         }
     }
 
     private void resetUpdater() {
         updater = new TimerTask() {
             @Override
             public void run() {
                 updateClockAction();
             }
         };
     }
 
     private void resetClockForm() {
         clock = new Clock();
         timerJLabel.setText("00 : 00 : 00");
         startPauseJButton.setText("Start");
         this.setTitle(null);
         getContentPane().setBackground(Color.DARK_GRAY);
         tasksJComboBox.setEnabled(true);
         resetUpdater();
         totalPause = 0;
     }
 
     private void saveTimeSpan(long start, long stop, boolean isPause) {
         try {
             Tijdspanne t = new Tijdspanne(start, stop);
 
             if (isPause) {
                 totalPause += (stop - start);
                 t.setPauze(true);
                 pause.add(t);
             } else {
                 currentTask.addGewerkteTijd(t);
                 currentTask.getPauze().addAll(pause);
             }
             Inserter.inputTijdSpanne(validator.getSessionKey(), t, currentTask.getID());
         } catch (IOException | WebserviceException | DataInputException ex) {
             JOptionPane.showMessageDialog(this, ex.getMessage());
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         timerJLabel = new javax.swing.JLabel();
         currentProjectJLabel = new javax.swing.JLabel();
         stopJButton = new javax.swing.JButton();
         startPauseJButton = new javax.swing.JButton();
         tasksJComboBox = new javax.swing.JComboBox();
         minimizeJButton = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setBackground(new java.awt.Color(64, 64, 64));
         setResizable(false);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 workDialogShown(evt);
             }
         });
 
         timerJLabel.setBackground(new java.awt.Color(255, 255, 255));
         timerJLabel.setFont(new java.awt.Font("Tahoma", 0, 36)); // NOI18N
         timerJLabel.setForeground(new java.awt.Color(0, 255, 255));
         timerJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         timerJLabel.setText("00 : 00 : 00");
         timerJLabel.setToolTipText("");
         timerJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
         timerJLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         timerJLabel.setMinimumSize(new java.awt.Dimension(0, 0));
         timerJLabel.setName(""); // NOI18N
 
         currentProjectJLabel.setBackground(new java.awt.Color(255, 255, 255));
         currentProjectJLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
         currentProjectJLabel.setForeground(new java.awt.Color(0, 204, 204));
         currentProjectJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         currentProjectJLabel.setText("Current project: ...");
         currentProjectJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
         currentProjectJLabel.setName(""); // NOI18N
         currentProjectJLabel.setOpaque(true);
 
         stopJButton.setBackground(new java.awt.Color(51, 255, 204));
         stopJButton.setFont(new java.awt.Font("Wide Latin", 0, 14)); // NOI18N
         stopJButton.setText("Stop");
         stopJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 stopJButtonActionPerformed(evt);
             }
         });
 
         startPauseJButton.setBackground(new java.awt.Color(51, 255, 204));
         startPauseJButton.setFont(new java.awt.Font("Wide Latin", 0, 14)); // NOI18N
         startPauseJButton.setText("Start");
         startPauseJButton.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 startPauseJButtonMouseClicked(evt);
             }
         });
 
         tasksJComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { }));
 
         minimizeJButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         minimizeJButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         minimizeJButton.setMaximumSize(new java.awt.Dimension(33, 33));
         minimizeJButton.setMinimumSize(new java.awt.Dimension(33, 33));
         minimizeJButton.setPreferredSize(new java.awt.Dimension(33, 33));
         minimizeJButton.setVerticalAlignment(javax.swing.SwingConstants.TOP);
         minimizeJButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 minimizeJButtonActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addComponent(startPauseJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 170, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(stopJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE))
                     .addComponent(currentProjectJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(tasksJComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(minimizeJButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(timerJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(timerJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE)
                     .addComponent(minimizeJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(currentProjectJLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(tasksJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(startPauseJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(stopJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 51, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void workDialogShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_workDialogShown
         try {
             currentProjectJLabel.setText(UserControl.getCurrentProject().getNaam());
         } catch (GUIException ex) {
             JOptionPane.showMessageDialog(this, ex.getMessage());
             this.dispose();
         }
     }//GEN-LAST:event_workDialogShown
 
     private void updateClockAction() {
         if (minimized) {
             if (!clock.isPaused()) {
                 minimizedTimerJLabel.setText(Clock.longTimeToHMS(clock.getRuntime() - totalPause));
             } else {
                 minimizedTimerJLabel.setText(Clock.longTimeToHMS(Clock.generateUnixTimestamp() - pauseStart + totalPause));
             }
         } else {
             // !! zorgen dat indien tijd nog niet geset is juist waarde weergeeft
             if (!clock.isPaused()) {
                 timerJLabel.setText(Clock.longTimeToHMS(clock.getRuntime() - totalPause));
             } else {
                 timerJLabel.setText(Clock.longTimeToHMS(Clock.generateUnixTimestamp() - pauseStart + totalPause));
             }
         }
     }
 
     private void stopJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopJButtonActionPerformed
         stopTimerAction();
     }//GEN-LAST:event_stopJButtonActionPerformed
 
     private void stopTimerAction() {
         try {
             if (clock.isPaused()) {
                 pauseStop = clock.pauseToggle();
                 saveTimeSpan(pauseStart, pauseStop, true);
             }
             stopTime = clock.stop();
             updater.cancel();
             firstRun = true;
             saveTimeSpan(startTime, stopTime, false);
             JOptionPane.showMessageDialog(this, "You worked for " + Clock.longTimeToString((stopTime - startTime) - totalPause, true) + "\nand took " + Clock.longTimeToString(totalPause, true) + " pause");
             resetClockForm();
         } catch (ClockException ex) {
             JOptionPane.showMessageDialog(this, ex.getMessage());
         }
     }
 
     private void minimizeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minimizeJButtonActionPerformed
         boolean value;
         Dimension frameDim;
         ImageIcon icon;
 
         if (!minimized) {
             value = false;
             frameDim = new Dimension((int) minimizedTimerJLabel.getWidth() + 85, (int) minimizedTimerJLabel.getHeight() + 50);
             icon = maxIcon;
             location = this.getLocation();
             this.setLocation(minLocation);
         } else {
             value = true;
             frameDim = frameSize;
             icon = minIcon;
             minLocation = this.getLocation();
             this.setLocation(location);
         }
 
         minimized = !value;
 
         minimizedTimerJLabel.setVisible(minimized);
         timerJLabel.setVisible(!minimized);
         minimizeJButton.setIcon(icon);
         startPauseJButton.setVisible(value);
         stopJButton.setVisible(value);
         tasksJComboBox.setVisible(value);
         currentProjectJLabel.setVisible(value);
 
         this.setAlwaysOnTop(!value);
         this.setSize(frameDim);
         updateClockAction();  
     }//GEN-LAST:event_minimizeJButtonActionPerformed
 
     private void startPauseJButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_startPauseJButtonMouseClicked
         if (!clock.isRunning()) {
             try {
                 resetUpdater();
                 startTime = clock.start();
                 currentTask = (Taak) tasksJComboBox.getSelectedItem();
             } catch (ClockException ex) {
                 JOptionPane.showMessageDialog(this, ex.getMessage());
             }
 
             timer.schedule(updater, 0, 1000);
             startPauseJButton.setText("Pause");
             tasksJComboBox.setEnabled(false);
 
 
         } else if (clock.isPaused()) {
             try {
                 pauseStop = clock.pauseToggle();
             } catch (ClockException ex) {
                 JOptionPane.showMessageDialog(this, ex.getMessage());
             }
 
             getContentPane().setBackground(Color.DARK_GRAY);
             startPauseJButton.setText("Pause");
 
             saveTimeSpan(pauseStart, pauseStop, true);
             this.setTitle("Total paused: " + Clock.longTimeToString(totalPause, false));
 
         } else {
             try {
                 pauseStart = clock.pauseToggle();
             } catch (ClockException ex) {
                 JOptionPane.showMessageDialog(this, ex.getMessage());
             }
 
             getContentPane().setBackground(Color.LIGHT_GRAY);
             startPauseJButton.setText("Resume");
             this.setTitle("Total worked: " + Clock.longTimeToString(clock.getRuntime() - totalPause, false));
         }
     }//GEN-LAST:event_startPauseJButtonMouseClicked
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         if (clock.isRunning()) {
             int result = JOptionPane.showConfirmDialog(tasksJComboBox, "Do you want to save?");
             if (result == JOptionPane.YES_OPTION) {
                 if (clock.isPaused()) {
                     try {
                         pauseStop = clock.pauseToggle();
                         saveTimeSpan(pauseStart, pauseStop, true);
                     } catch (ClockException e) {
                     }
                 }
                 stopTimerAction();
                 this.dispose();
             } else if (result == JOptionPane.NO_OPTION) {
                 this.dispose();
             }
         } else {
             this.dispose();
         }
 
     }//GEN-LAST:event_formWindowClosing
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel currentProjectJLabel;
     private javax.swing.JButton minimizeJButton;
     private javax.swing.JButton startPauseJButton;
     private javax.swing.JButton stopJButton;
     private javax.swing.JComboBox tasksJComboBox;
     private javax.swing.JLabel timerJLabel;
     // End of variables declaration//GEN-END:variables
 }
