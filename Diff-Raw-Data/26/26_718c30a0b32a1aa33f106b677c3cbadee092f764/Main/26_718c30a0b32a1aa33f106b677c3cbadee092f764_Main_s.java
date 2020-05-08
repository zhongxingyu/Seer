 package net.pixomania.minebackup;
 
 import java.awt.Font;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.URL;
 import java.text.NumberFormat;
 import java.util.Scanner;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import javax.swing.JToggleButton;
 
 /**
  *
  * @author vigge_sWe
  */
 public class Main extends javax.swing.JFrame{
 
     static {
         System.setProperty("swing.defaultlaf", "org.pushingpixels.substance.api.skin.SubstanceGraphiteLookAndFeel");
     }
 
     // Backgorund images
     Image img;
     Image img2;
 
     // Variables for the "backup" text's animation
     public int x = 18;
     public int rot = 0;
     public boolean inc = true;
 
     public Timer timer;
 
     // Initial value of 10 minutes for the interval
     public long remaining = 600000;
     public long lastUpdate;
     NumberFormat format;
     public javax.swing.Timer timer1;
     
     // Starting backup number
     public int number = 1;
 
     // bool to know which filechooser to use, fromfield or tofield
     public boolean from = true;
 
     // icon
     private URL myIconUrl = this.getClass().getResource("/resource/icon.png");
 
     public Main()  {
         initComponents();
 
         // Make a new NumberFormat to format some numbers for the stats
         format = NumberFormat.getNumberInstance();
         format.setMinimumIntegerDigits(2);
 
         // This is the timer's action that shows when next backup is happening
         timer1 = new javax.swing.Timer(1000, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 updateDisplay();
             }
         });
 
         fileChoser.setFileHidingEnabled(false);
 
         // This code is the animation, it basically just changes
         // the size of the text every 50ms
         javax.swing.Timer timr = new javax.swing.Timer(50, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 if(x == 18 && rot !=2){
                     zoomingBackupLbl.setFont(new Font("impact", 1, x));
                     rot++;
                 } else {
                     if(inc){
                         x++;
                     } else {
                         x--;
                     }
                     zoomingBackupLbl.setFont(new Font("impact", 1, x));
 
 
                     if(x == 22){
                         inc = false;
                     }
 
                     if(x == 18){
                         inc = true;
                     }
                     rot = 0;
 
                 }
 
             }
         });
         timr.start();
 
         Scanner sc;
         try {
             // First, as we just started the app, we need to set the
             // ThisTime vars to 0, and update the file
             File stats = new File("stats.txt");
            if(!stats.exists())stats.createNewFile();
             sc = new Scanner(stats);
             int backupsThisTime = sc.nextInt();
             int backupsTotal = sc.nextInt();
             long dataThisTime = sc.nextLong();
             long dataTotal = sc.nextLong();
             long time = sc.nextLong();
 
             backupsThisTime = 0;
             dataThisTime = 0;
 
             sc.close();
             
            FileWriter fstream = new FileWriter(new File("stats.txt"));
             BufferedWriter out = new BufferedWriter(fstream);
 
             out.write(backupsThisTime+" "+ backupsTotal+" "+dataThisTime+" "+dataTotal+" "+time);
             out.close();
 
             // Now here we load the settings from the last run, if they exist
             File settings = new File("settings.txt");
             if(!settings.exists())settings.createNewFile();
             sc = new Scanner(settings);
             if(sc.hasNext()){
                 backupFromField.setText(sc.next());
                 backupToField.setText(sc.next());
                 intervalSpinner.setValue((Object) sc.nextInt());
                 concurrentSpinner.setValue((Object) sc.nextInt());
             }
             sc.close();
         } catch (Exception ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
 
     }
     
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         fileChooserFrame = new javax.swing.JFrame();
         fileChooserBackgroundPane = new org.jdesktop.swingx.JXImagePanel();
         fileChoser = new javax.swing.JFileChooser();
         statsFrame = new javax.swing.JFrame();
         backupsSinceLbl = new javax.swing.JLabel();
         totalBackupLbl = new javax.swing.JLabel();
         dataBackedLbl = new javax.swing.JLabel();
         totalDataBackedLbl = new javax.swing.JLabel();
         backupsinceValueLbl = new javax.swing.JLabel();
         totalBackupValueLbl = new javax.swing.JLabel();
         dataBackedValueLbl = new javax.swing.JLabel();
         totalDataBackedValueLbl = new javax.swing.JLabel();
         timeLbl = new javax.swing.JLabel();
         timeValueLbl = new javax.swing.JLabel();
         backgroundImage = new org.jdesktop.swingx.JXImagePanel();
         minecraftLnk = new org.jdesktop.swingx.JXHyperlink();
         pixomaniaLnk = new org.jdesktop.swingx.JXHyperlink();
         statusLbl = new org.jdesktop.swingx.JXLabel();
         nextBackupLbl = new org.jdesktop.swingx.JXLabel();
         timerLbl = new org.jdesktop.swingx.JXLabel();
         jPanel1 = new javax.swing.JPanel();
         backupToField = new javax.swing.JTextField();
         concurrentSpinner = new javax.swing.JSpinner();
         intervalSpinner = new javax.swing.JSpinner();
         browseToBtn = new javax.swing.JButton();
         backupFromLbl = new javax.swing.JLabel();
         intervalLbl = new javax.swing.JLabel();
         backupFromField = new javax.swing.JTextField();
         toggleBackupBtn = new javax.swing.JToggleButton();
         statsBtn = new javax.swing.JButton();
         closeBtn = new javax.swing.JButton();
         browseFromBtn = new javax.swing.JButton();
         backupToLbl = new javax.swing.JLabel();
         concurrentLbl = new javax.swing.JLabel();
         zoomingBackupLbl = new org.jdesktop.swingx.JXLabel();
 
         fileChooserFrame.setMinimumSize(new java.awt.Dimension(565, 397));
         fileChooserFrame.setResizable(false);
         fileChooserFrame.setUndecorated(true);
 
         fileChoser.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
         fileChoser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fileChoserActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout fileChooserBackgroundPaneLayout = new javax.swing.GroupLayout(fileChooserBackgroundPane);
         fileChooserBackgroundPane.setLayout(fileChooserBackgroundPaneLayout);
         fileChooserBackgroundPaneLayout.setHorizontalGroup(
             fileChooserBackgroundPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(fileChoser, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
         );
         fileChooserBackgroundPaneLayout.setVerticalGroup(
             fileChooserBackgroundPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(fileChoser, javax.swing.GroupLayout.DEFAULT_SIZE, 408, Short.MAX_VALUE)
         );
 
         javax.swing.GroupLayout fileChooserFrameLayout = new javax.swing.GroupLayout(fileChooserFrame.getContentPane());
         fileChooserFrame.getContentPane().setLayout(fileChooserFrameLayout);
         fileChooserFrameLayout.setHorizontalGroup(
             fileChooserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(fileChooserBackgroundPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         fileChooserFrameLayout.setVerticalGroup(
             fileChooserFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(fileChooserBackgroundPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
 
         try {
             URL url2 = getClass().getResource("/resource/minebg.png");
             img2 = ImageIO.read(url2);
             fileChooserBackgroundPane.setImage(img2);
         } catch (Exception ex) {
             System.out.println("error");
         }
 
         statsFrame.setTitle("Because I like stats!");
 
         backupsSinceLbl.setText("Backups since launch:");
 
         totalBackupLbl.setText("Total backups:");
 
         dataBackedLbl.setText("Data backed up since launch:");
 
         totalDataBackedLbl.setText("Total data backed up:");
 
         backupsinceValueLbl.setText("0");
 
         totalBackupValueLbl.setText("0");
 
         dataBackedValueLbl.setText("0 MB");
 
         totalDataBackedValueLbl.setText("0 MB");
 
         timeLbl.setText("Total time taken to backup data:");
 
         timeValueLbl.setText("0 minutes");
 
         javax.swing.GroupLayout statsFrameLayout = new javax.swing.GroupLayout(statsFrame.getContentPane());
         statsFrame.getContentPane().setLayout(statsFrameLayout);
         statsFrameLayout.setHorizontalGroup(
             statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statsFrameLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(statsFrameLayout.createSequentialGroup()
                         .addGroup(statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(statsFrameLayout.createSequentialGroup()
                                 .addComponent(backupsSinceLbl)
                                 .addGap(4, 4, 4)
                                 .addComponent(backupsinceValueLbl))
                             .addGroup(statsFrameLayout.createSequentialGroup()
                                 .addComponent(totalBackupLbl)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(totalBackupValueLbl)))
                         .addGap(18, 18, 18)
                         .addGroup(statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(statsFrameLayout.createSequentialGroup()
                                 .addComponent(totalDataBackedLbl)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(totalDataBackedValueLbl))
                             .addGroup(statsFrameLayout.createSequentialGroup()
                                 .addComponent(dataBackedLbl)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(dataBackedValueLbl))))
                     .addGroup(statsFrameLayout.createSequentialGroup()
                         .addComponent(timeLbl)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(timeValueLbl)))
                 .addContainerGap(64, Short.MAX_VALUE))
         );
         statsFrameLayout.setVerticalGroup(
             statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statsFrameLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(backupsSinceLbl)
                     .addComponent(dataBackedLbl)
                     .addComponent(backupsinceValueLbl)
                     .addComponent(dataBackedValueLbl))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(totalBackupLbl)
                     .addComponent(totalDataBackedLbl)
                     .addComponent(totalBackupValueLbl)
                     .addComponent(totalDataBackedValueLbl))
                 .addGap(18, 18, 18)
                 .addGroup(statsFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(timeLbl)
                     .addComponent(timeValueLbl))
                 .addContainerGap(124, Short.MAX_VALUE))
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setIconImage(new javax.swing.ImageIcon(myIconUrl, "Minecraft Bckup").getImage());
         setResizable(false);
         setUndecorated(true);
 
         backgroundImage.setMinimumSize(new java.awt.Dimension(464, 292));
         backgroundImage.setPreferredSize(new java.awt.Dimension(464, 292));
 
         minecraftLnk.setClickedColor(new java.awt.Color(204, 255, 0));
         minecraftLnk.setText("Minecraft");
         minecraftLnk.setUnclickedColor(new java.awt.Color(255, 255, 0));
         minecraftLnk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 minecraftLnkActionPerformed(evt);
             }
         });
 
         pixomaniaLnk.setClickedColor(new java.awt.Color(204, 255, 0));
         pixomaniaLnk.setText("Pixomania");
         pixomaniaLnk.setUnclickedColor(new java.awt.Color(255, 255, 0));
         pixomaniaLnk.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pixomaniaLnkActionPerformed(evt);
             }
         });
 
         statusLbl.setForeground(new java.awt.Color(255, 255, 0));
         statusLbl.setText("Status");
 
         nextBackupLbl.setForeground(new java.awt.Color(255, 255, 0));
         nextBackupLbl.setText("Next backup:");
 
         timerLbl.setForeground(new java.awt.Color(255, 255, 0));
         timerLbl.setText("10:00");
 
         jPanel1.setOpaque(false);
 
         backupToField.setEditable(false);
 
         concurrentSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(4), Integer.valueOf(1), null, Integer.valueOf(1)));
 
         intervalSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(10), Integer.valueOf(1), null, Integer.valueOf(1)));
         intervalSpinner.setMinimumSize(new java.awt.Dimension(44, 15));
         intervalSpinner.setPreferredSize(new java.awt.Dimension(44, 20));
 
         browseToBtn.setText("...");
         browseToBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 browseToBtnActionPerformed(evt);
             }
         });
 
         backupFromLbl.setText("Backup from:");
 
         intervalLbl.setText("Interval (m):");
 
         backupFromField.setEditable(false);
 
         toggleBackupBtn.setText("Toggle to start backup");
         toggleBackupBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 toggleBackupBtnActionPerformed(evt);
             }
         });
 
         statsBtn.setText("Stats");
         statsBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 statsBtnActionPerformed(evt);
             }
         });
 
         closeBtn.setText("Close");
         closeBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 closeBtnActionPerformed(evt);
             }
         });
 
         browseFromBtn.setText("...");
         browseFromBtn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 browseFromBtnActionPerformed(evt);
             }
         });
 
         backupToLbl.setText("Backup to:");
 
         concurrentLbl.setText("copies:");
 
         javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(backupToLbl)
                     .addComponent(intervalLbl)
                     .addGroup(jPanel1Layout.createSequentialGroup()
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(statsBtn)
                             .addComponent(backupFromLbl))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                     .addComponent(backupToField, javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(backupFromField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 188, javax.swing.GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                     .addComponent(browseFromBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addComponent(browseToBtn)))
                             .addGroup(jPanel1Layout.createSequentialGroup()
                                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                         .addComponent(intervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)
                                         .addGap(18, 18, 18)
                                         .addComponent(concurrentLbl)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                         .addComponent(concurrentSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                     .addComponent(toggleBackupBtn, javax.swing.GroupLayout.Alignment.LEADING))
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(closeBtn)))))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(backupFromField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(backupFromLbl)
                     .addComponent(browseFromBtn))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(backupToLbl)
                     .addComponent(backupToField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(browseToBtn))
                 .addGap(12, 12, 12)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(intervalLbl)
                     .addComponent(intervalSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(concurrentSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(concurrentLbl))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(statsBtn)
                     .addComponent(toggleBackupBtn)
                     .addComponent(closeBtn))
                 .addContainerGap())
         );
 
         zoomingBackupLbl.setBackground(new java.awt.Color(255, 255, 0));
         zoomingBackupLbl.setForeground(new java.awt.Color(255, 255, 0));
         zoomingBackupLbl.setForegroundPainter(null);
         zoomingBackupLbl.setText("Backup");
         zoomingBackupLbl.setTextRotation(-0.7);
         zoomingBackupLbl.setFont(new java.awt.Font("Impact", 1, 18));
 
         javax.swing.GroupLayout backgroundImageLayout = new javax.swing.GroupLayout(backgroundImage);
         backgroundImage.setLayout(backgroundImageLayout);
         backgroundImageLayout.setHorizontalGroup(
             backgroundImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(backgroundImageLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(backgroundImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(backgroundImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                         .addGroup(backgroundImageLayout.createSequentialGroup()
                             .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addGap(31, 31, 31))
                         .addGroup(backgroundImageLayout.createSequentialGroup()
                             .addComponent(minecraftLnk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(nextBackupLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                             .addComponent(timerLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGap(18, 18, 18)
                             .addComponent(statusLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addGap(18, 18, 18)
                             .addComponent(pixomaniaLnk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addContainerGap()))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundImageLayout.createSequentialGroup()
                         .addComponent(zoomingBackupLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(23, 23, 23))))
         );
         backgroundImageLayout.setVerticalGroup(
             backgroundImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundImageLayout.createSequentialGroup()
                 .addGap(18, 18, 18)
                 .addComponent(zoomingBackupLbl, javax.swing.GroupLayout.DEFAULT_SIZE, 59, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(backgroundImageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(minecraftLnk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(nextBackupLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(timerLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(statusLbl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(pixomaniaLnk, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(backgroundImage, javax.swing.GroupLayout.PREFERRED_SIZE, 410, javax.swing.GroupLayout.PREFERRED_SIZE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(backgroundImage, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         try {
             URL url = getClass().getResource("/resource/minebackup.png");
             img = ImageIO.read(url);
             backgroundImage.setImage(img);
         } catch(Exception e) {
             System.out.println("error");
         }
 
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         setBounds((screenSize.width-410)/2, (screenSize.height-280)/2, 410, 280);
     }// </editor-fold>//GEN-END:initComponents
     
     /**
      * Action to update the timer that shows when next backup will run
      */
     void updateDisplay() {
         remaining -= 1000;
 
         // Convert remaining milliseconds to mm:ss format and display
         if (remaining < 0) remaining = 0;
         int minutes = (int)(remaining/60000);
         int seconds = (int)((remaining%60000)/1000);
         timerLbl.setText(format.format(minutes) + ":" + format.format(seconds));
 
     }
 
     /**
      * The action code for starting/stopping backups
      * @param evt
      */
     private void toggleBackupBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleBackupBtnActionPerformed
 
         // Here we save the settings
         FileWriter fstream;
             try {
                 fstream = new FileWriter("settings.txt");
                 BufferedWriter out = new BufferedWriter(fstream);
                 out.write(backupFromField.getText()+"\n"+
                         backupToField.getText()+"\n"+
                         (Integer) intervalSpinner.getValue()+"\n"+
                         (Integer) concurrentSpinner.getValue());
                 out.close();
             } catch (IOException ex) {
                 Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
             }
 
         // if the button is selected we want to backup the files
         JToggleButton btn = (JToggleButton) evt.getSource();
         if(btn.isSelected()){
             int time = (Integer) intervalSpinner.getValue()*60*1000;
             remaining =  (long) time;
             
             if(backupToField.getText().isEmpty()){
                 JOptionPane.showMessageDialog(rootPane, "You need to specify a path first!", "Oh noes, failure!", JOptionPane.ERROR_MESSAGE);
                 btn.setSelected(false);
             } else {
                 // all is fine, make a new TimerTask and run it
                 BackupTask bt = new BackupTask();
             
                 bt.run();
             }
 
         } else {
             // stop the timers and set the timer back to the initial value
             timer.cancel();
             timer1.stop();
             int time = (Integer) intervalSpinner.getValue()*60*1000;
             remaining =  (long) time;
         }
     }//GEN-LAST:event_toggleBackupBtnActionPerformed
 
     private void closeBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeBtnActionPerformed
         // As the window is undecorated, we need a close button
         System.exit(0);
     }//GEN-LAST:event_closeBtnActionPerformed
 
     private void browseToBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseToBtnActionPerformed
         // Chose where to save the backups
         fileChooserFrame.setBounds((int)(this.getX()- this.getX()/3),(int)(this.getY()-this.getY()/3), fileChooserFrame.getWidth(), fileChooserFrame.getHeight());
         from = false;
         fileChooserFrame.pack();
         fileChooserFrame.setVisible(true);
     }//GEN-LAST:event_browseToBtnActionPerformed
 
     private void fileChoserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChoserActionPerformed
         // Action code for the filechooser
         if(evt.getActionCommand().equals("ApproveSelection")){
             if(from) {
                backupFromField.setText(fileChoser.getSelectedFile().getAbsolutePath());
             } else {
                 backupToField.setText(fileChoser.getSelectedFile().getAbsolutePath());
             }
 
            fileChooserFrame.dispose();
         } else {
             fileChooserFrame.dispose();
         }
     }//GEN-LAST:event_fileChoserActionPerformed
 
     private void minecraftLnkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_minecraftLnkActionPerformed
         BareBonesBrowserLaunch.openURL("http://minecraft.net");
     }//GEN-LAST:event_minecraftLnkActionPerformed
 
     private void pixomaniaLnkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pixomaniaLnkActionPerformed
         BareBonesBrowserLaunch.openURL("http://pixomania.net");
     }//GEN-LAST:event_pixomaniaLnkActionPerformed
 
     /**
      * Show the stats window
      * @param evt
      */
     private void statsBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statsBtnActionPerformed
         java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
         statsFrame.setBounds(((screenSize.width/2)-this.getWidth()/2)+10,(int)(this.getY()-this.getY()/20), statsFrame.getWidth(), statsFrame.getHeight());
 
         // Read the stats from the file, and set the labels to the values
         Scanner sc;
         try {
             sc = new Scanner(new File("stats.txt"));
             int backupsThisTime = sc.nextInt();
             int backupsTotal = sc.nextInt();
             long dataThisTime = sc.nextLong();
             long dataTotal = sc.nextLong();
             long time = sc.nextLong();
             
 
             backupsinceValueLbl.setText(Integer.toString(backupsThisTime));
             totalBackupValueLbl.setText(Integer.toString(backupsTotal));
 
             dataBackedValueLbl.setText(Long.toString(dataThisTime/1024/1024)+" MB");
             totalDataBackedValueLbl.setText(Long.toString(dataTotal/1024/1024)+" MB");
 
             timeValueLbl.setText(Long.toString(time/1000)+" seconds");
             sc.close();
         } catch (Exception e){
             System.out.println("lol");
         }
         statsFrame.pack();
         statsFrame.setVisible(true);
     }//GEN-LAST:event_statsBtnActionPerformed
 
     private void browseFromBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseFromBtnActionPerformed
         // Chose which files to backup
         fileChooserFrame.setBounds((int)(this.getX()- this.getX()/3),(int)(this.getY()-this.getY()/3), fileChooserFrame.getWidth(), fileChooserFrame.getHeight());
         from = true;
         fileChooserFrame.pack();
         fileChooserFrame.setVisible(true);
     }//GEN-LAST:event_browseFromBtnActionPerformed
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         
         
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new Main().setVisible(true);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private org.jdesktop.swingx.JXImagePanel backgroundImage;
     private static javax.swing.JTextField backupFromField;
     private javax.swing.JLabel backupFromLbl;
     private static javax.swing.JTextField backupToField;
     private javax.swing.JLabel backupToLbl;
     private javax.swing.JLabel backupsSinceLbl;
     private javax.swing.JLabel backupsinceValueLbl;
     private javax.swing.JButton browseFromBtn;
     private javax.swing.JButton browseToBtn;
     private javax.swing.JButton closeBtn;
     private javax.swing.JLabel concurrentLbl;
     private javax.swing.JSpinner concurrentSpinner;
     private javax.swing.JLabel dataBackedLbl;
     private javax.swing.JLabel dataBackedValueLbl;
     private org.jdesktop.swingx.JXImagePanel fileChooserBackgroundPane;
     private javax.swing.JFrame fileChooserFrame;
     private javax.swing.JFileChooser fileChoser;
     private javax.swing.JLabel intervalLbl;
     private javax.swing.JSpinner intervalSpinner;
     private javax.swing.JPanel jPanel1;
     private org.jdesktop.swingx.JXHyperlink minecraftLnk;
     private org.jdesktop.swingx.JXLabel nextBackupLbl;
     private org.jdesktop.swingx.JXHyperlink pixomaniaLnk;
     private javax.swing.JButton statsBtn;
     private javax.swing.JFrame statsFrame;
     public static org.jdesktop.swingx.JXLabel statusLbl;
     private javax.swing.JLabel timeLbl;
     private javax.swing.JLabel timeValueLbl;
     private org.jdesktop.swingx.JXLabel timerLbl;
     private javax.swing.JToggleButton toggleBackupBtn;
     private javax.swing.JLabel totalBackupLbl;
     private javax.swing.JLabel totalBackupValueLbl;
     private javax.swing.JLabel totalDataBackedLbl;
     private javax.swing.JLabel totalDataBackedValueLbl;
     private org.jdesktop.swingx.JXLabel zoomingBackupLbl;
     // End of variables declaration//GEN-END:variables
 
     /**
      * the timer task to start backup
      */
     class BackupTask extends TimerTask implements PropertyChangeListener {
             public void run() {
                 // Stop the previous timer, if it is already running
                 timer1.stop();
                 if(timer != null){
                     timer.cancel();
                 }
                 statusLbl.setText("Backing up...");
 
                 //run the backup thread
                 BackupWorker bw = new BackupWorker(
                 Main.backupFromField.getText(),
                 Main.backupToField.getText(), number);
 
                 if(number < (Integer) concurrentSpinner.getValue()) {
                     number++;
                 } else {
                     number = 1;
                 }
                 bw.addPropertyChangeListener(this);
                 bw.execute();
                 
             }
 
             /**
              * Stuff to do when the worker is done, like restarting the timer
              * @param evt
              */
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals("progress")) {
                     int progress = (Integer) evt.getNewValue();
                     if(progress == 100){
                         statusLbl.setText("done backing up");
                         int time = (Integer) intervalSpinner.getValue()*60*1000;
                         remaining =  (long) time;
                         timer = new Timer();
                         timer.scheduleAtFixedRate(new BackupTask(), remaining, 10000);
                         timer1.start();
                     }
                 }
             }
         }
 }
