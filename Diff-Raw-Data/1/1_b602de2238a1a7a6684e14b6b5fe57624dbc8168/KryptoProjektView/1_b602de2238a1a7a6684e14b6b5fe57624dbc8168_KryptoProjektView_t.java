 /*
  * KryptoProjektView.java
  */
 package kryptoprojekt;
 
 import kryptoprojekt.basicFrames.AdditionFrame;
 import kryptoprojekt.basicFrames.SubtractFrame;
 import kryptoprojekt.basicFrames.ZFrame;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import kryptoprojekt.basicFrames.DivisionFrame;
 import kryptoprojekt.basicFrames.ExtendedGCDFrame;
 import kryptoprojekt.basicFrames.GCDFrame;
 import kryptoprojekt.basicFrames.ModuloFrame;
 import kryptoprojekt.basicFrames.MultiplicationFrame;
 import kryptoprojekt.basicFrames.PhiFrame;
 import kryptoprojekt.basicFrames.PrimeFieldElementFrame;
 import kryptoprojekt.basicFrames.SaMFrame;
 import kryptoprojekt.basicFrames.SaMModFrame;
 import kryptoprojekt.coderFrames.CreateHammingErrorFrame;
 import kryptoprojekt.coderFrames.DecodeHammingCodeFrame;
 import kryptoprojekt.coderFrames.EncodeHammingCodeFrame;
 import kryptoprojekt.coderFrames.HammingDistanceFrame;
 import kryptoprojekt.coderFrames.HammingSyndromFrame;
 import kryptoprojekt.coderFrames.HammingWeightFrame;
 import kryptoprojekt.coderFrames.InitHammingFrame;
 import kryptoprojekt.controller.XMLReader;
 import kryptoprojekt.matrixFrames.MatrixAddFrame;
 import kryptoprojekt.matrixFrames.MatrixFrame;
 import kryptoprojekt.matrixFrames.MatrixMultiplyFrame;
 import kryptoprojekt.matrixFrames.MatrixPrimeFieldFrame;
 import kryptoprojekt.primeFrames.FermatFrame;
 import kryptoprojekt.primeFrames.LucasFrame;
 import kryptoprojekt.primeFrames.MillerRabinFrame;
 
 /**
  * The application's main frame.
  */
 public class KryptoProjektView extends FrameView {
 
     private Desktop desktop;
     private ConnectionHandler handler = new ConnectionHandler();
     private Executor executor;
 
     public KryptoProjektView(SingleFrameApplication app) {
         super(app);
 
         initComponents();
 
         desktop = new Desktop(handler);
 
         javax.swing.GroupLayout mainFormLayout = new javax.swing.GroupLayout(mainForm);
         mainForm.setLayout(mainFormLayout);
         mainFormLayout.setHorizontalGroup(
                 mainFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).
                 addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE));
         mainFormLayout.setVerticalGroup(
                 mainFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).
                 addComponent(desktop, javax.swing.GroupLayout.Alignment.TRAILING,
                              javax.swing.GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE));
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
 
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                 } else if ("message".equals(propertyName)) {
                     String text = (String) (evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer) (evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);
                 }
             }
         });
         initializeControlsLanguage(Kit.xmlReader);
     }
 
     private void initializeControlsLanguage(XMLReader xml) {
         startMenuItem.setText(xml.getTagElement("KryptoView", "startMenuItem"));
         basicMenu.setText(xml.getTagElement("KryptoView", "basicMenu"));
         basicArithmeticMenu.setText(xml.getTagElement("KryptoView", "basicArithmeticMenu"));
         zMenuItem.setText(xml.getTagElement("KryptoView", "zMenuItem"));
         primeFieldMenuItem.setText(xml.getTagElement("KryptoView", "primeFieldMenuItem"));
         additionMenuItem.setText(xml.getTagElement("KryptoView", "additionMenuItem"));
         subtractionMenuItem.setText(xml.getTagElement("KryptoView", "subtractionMenuItem"));
         multiplicationMenuItem.setText(xml.getTagElement("KryptoView", "multiplicationMenuItem"));
         divisionMenuItem.setText(xml.getTagElement("KryptoView", "divisionMenuItem"));
         modMenuItem.setText(xml.getTagElement("KryptoView", "modMenuItem"));
         samMenuItem.setText(xml.getTagElement("KryptoView", "samMenuItem"));
         sammodMenuItem.setText(xml.getTagElement("KryptoView", "sammodMenuItem"));
         gcdMenuItem.setText(xml.getTagElement("KryptoView", "gcdMenuItem"));
        extendedGCDItem.setText(xml.getTagElement("KryptoView", "extendedGCDItem"));
         phiMenuItem.setText(xml.getTagElement("KryptoView", "phiMenuItem"));
         primeTestMenu.setText(xml.getTagElement("KryptoView", "primeTestMenu"));
         fermatMenuItem.setText(xml.getTagElement("KryptoView", "fermatMenuItem"));
         lucasMenuItem.setText(xml.getTagElement("KryptoView", "lucasMenuItem"));
         coderMenu.setText(xml.getTagElement("KryptoView", "coderMenu"));
         hammingMenu.setText(xml.getTagElement("KryptoView", "hammingMenu"));
         initHammingCodeMenuItem.setText(xml.getTagElement("KryptoView", "initHammingCodeMenuItem"));
         encodeMenuItem.setText(xml.getTagElement("KryptoView", "encodeMenuItem"));
         syndromMenuItem.setText(xml.getTagElement("KryptoView", "syndromMenuItem"));
         decodeMenuItem.setText(xml.getTagElement("KryptoView", "decodeMenuItem"));
         createErrorsMenuItem.setText(xml.getTagElement("KryptoView", "createErrorsMenuItem"));
         hammingDistanceMenuItem.setText(xml.getTagElement("KryptoView", "hammingDistanceMenuItem"));
         vectorWeightMenuItem.setText(xml.getTagElement("KryptoView", "vectorWeightMenuItem"));
         startBtnOnBar.setText(xml.getTagElement("KryptoView", "startBtnOnBar"));
         cancelBtnOnBar.setText(xml.getTagElement("KryptoView", "cancelBtnOnBar"));
         clearBtnOnBar.setText(xml.getTagElement("KryptoView", "clearBtnOnBar"));
         matrixMenu.setText(xml.getTagElement("KryptoView", "matrixMenu"));
         newMatrixMenuItem.setText(xml.getTagElement("KryptoView", "newMatrixMenuItem"));
         matrixMultiplyMenuItem.setText(xml.getTagElement("KryptoView", "matrixMultiplyMenuItem"));
         newMatrixPrimeFieldMenuItem.setText(xml.getTagElement("KryptoView", "newMatrixPrimeFieldMenuItem"));
         addMatrixMenuItem.setText(xml.getTagElement("KryptoView", "addMatrixMenuItem"));
     }
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = KryptoProjektApp.getApplication().getMainFrame();
             aboutBox = new KryptoProjektAboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         KryptoProjektApp.getApplication().show(aboutBox);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         mainForm = new javax.swing.JPanel();
         secondBar = new javax.swing.JPanel();
         startBtnOnBar = new javax.swing.JButton();
         cancelBtnOnBar = new javax.swing.JButton();
         clearBtnOnBar = new javax.swing.JButton();
         mainMenuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         startMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         basicMenu = new javax.swing.JMenu();
         basicArithmeticMenu = new javax.swing.JMenu();
         additionMenuItem = new javax.swing.JMenuItem();
         subtractionMenuItem = new javax.swing.JMenuItem();
         multiplicationMenuItem = new javax.swing.JMenuItem();
         divisionMenuItem = new javax.swing.JMenuItem();
         modMenuItem = new javax.swing.JMenuItem();
         samMenuItem = new javax.swing.JMenuItem();
         sammodMenuItem = new javax.swing.JMenuItem();
         gcdMenuItem = new javax.swing.JMenuItem();
         extendedGCDItem = new javax.swing.JMenuItem();
         phiMenuItem = new javax.swing.JMenuItem();
         matrixMenu = new javax.swing.JMenu();
         newMatrixMenuItem = new javax.swing.JMenuItem();
         newMatrixPrimeFieldMenuItem = new javax.swing.JMenuItem();
         addMatrixMenuItem = new javax.swing.JMenuItem();
         matrixMultiplyMenuItem = new javax.swing.JMenuItem();
         zMenuItem = new javax.swing.JMenuItem();
         primeFieldMenuItem = new javax.swing.JMenuItem();
         primeTestMenu = new javax.swing.JMenu();
         fermatMenuItem = new javax.swing.JMenuItem();
         lucasMenuItem = new javax.swing.JMenuItem();
         rabinMenuItem = new javax.swing.JMenuItem();
         coderMenu = new javax.swing.JMenu();
         hammingMenu = new javax.swing.JMenu();
         initHammingCodeMenuItem = new javax.swing.JMenuItem();
         encodeMenuItem = new javax.swing.JMenuItem();
         syndromMenuItem = new javax.swing.JMenuItem();
         decodeMenuItem = new javax.swing.JMenuItem();
         createErrorsMenuItem = new javax.swing.JMenuItem();
         hammingDistanceMenuItem = new javax.swing.JMenuItem();
         vectorWeightMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
         jMenu3 = new javax.swing.JMenu();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu5 = new javax.swing.JMenu();
         jMenu6 = new javax.swing.JMenu();
         jMenuBar2 = new javax.swing.JMenuBar();
         jMenu7 = new javax.swing.JMenu();
         jMenu8 = new javax.swing.JMenu();
         jMenuBar3 = new javax.swing.JMenuBar();
         jMenu9 = new javax.swing.JMenu();
         jMenu10 = new javax.swing.JMenu();
 
         mainPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         mainPanel.setName("mainPanel"); // NOI18N
 
         mainForm.setName("mainForm"); // NOI18N
 
         javax.swing.GroupLayout mainFormLayout = new javax.swing.GroupLayout(mainForm);
         mainForm.setLayout(mainFormLayout);
         mainFormLayout.setHorizontalGroup(
             mainFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         mainFormLayout.setVerticalGroup(
             mainFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 324, Short.MAX_VALUE)
         );
 
         secondBar.setName("secondBar"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(kryptoprojekt.KryptoProjektApp.class).getContext().getResourceMap(KryptoProjektView.class);
         startBtnOnBar.setText(resourceMap.getString("startBtnOnBar.text")); // NOI18N
         startBtnOnBar.setName("startBtnOnBar"); // NOI18N
         startBtnOnBar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 startBtnOnBarActionPerformed(evt);
             }
         });
 
         cancelBtnOnBar.setText(resourceMap.getString("cancelBtnOnBar.text")); // NOI18N
         cancelBtnOnBar.setName("cancelBtnOnBar"); // NOI18N
         cancelBtnOnBar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cancelBtnOnBarActionPerformed(evt);
             }
         });
 
         clearBtnOnBar.setText(resourceMap.getString("clearBtnOnBar.text")); // NOI18N
         clearBtnOnBar.setName("clearBtnOnBar"); // NOI18N
         clearBtnOnBar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 clearBtnOnBarActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout secondBarLayout = new javax.swing.GroupLayout(secondBar);
         secondBar.setLayout(secondBarLayout);
         secondBarLayout.setHorizontalGroup(
             secondBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(secondBarLayout.createSequentialGroup()
                 .addComponent(startBtnOnBar)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(cancelBtnOnBar)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(clearBtnOnBar)
                 .addContainerGap(201, Short.MAX_VALUE))
         );
         secondBarLayout.setVerticalGroup(
             secondBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(secondBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                 .addComponent(startBtnOnBar)
                 .addComponent(cancelBtnOnBar)
                 .addComponent(clearBtnOnBar))
         );
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(secondBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
             .addComponent(mainForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addComponent(secondBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(mainForm, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         mainMenuBar.setName("mainMenuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         startMenuItem.setText(resourceMap.getString("startMenuItem.text")); // NOI18N
         startMenuItem.setName("startMenuItem"); // NOI18N
         startMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 startMenuItemActionPerformed(evt);
             }
         });
         fileMenu.add(startMenuItem);
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(kryptoprojekt.KryptoProjektApp.class).getContext().getActionMap(KryptoProjektView.class, this);
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         mainMenuBar.add(fileMenu);
 
         basicMenu.setText(resourceMap.getString("basicMenu.text")); // NOI18N
         basicMenu.setName("basicMenu"); // NOI18N
 
         basicArithmeticMenu.setText(resourceMap.getString("basicArithmeticMenu.text")); // NOI18N
         basicArithmeticMenu.setName("basicArithmeticMenu"); // NOI18N
 
         additionMenuItem.setText(resourceMap.getString("additionMenuItem.text")); // NOI18N
         additionMenuItem.setName("additionMenuItem"); // NOI18N
         additionMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 additionMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(additionMenuItem);
 
         subtractionMenuItem.setText(resourceMap.getString("subtractionMenuItem.text")); // NOI18N
         subtractionMenuItem.setName("subtractionMenuItem"); // NOI18N
         subtractionMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 subtractionMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(subtractionMenuItem);
 
         multiplicationMenuItem.setText(resourceMap.getString("multiplicationMenuItem.text")); // NOI18N
         multiplicationMenuItem.setName("multiplicationMenuItem"); // NOI18N
         multiplicationMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 multiplicationMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(multiplicationMenuItem);
 
         divisionMenuItem.setText(resourceMap.getString("divisionMenuItem.text")); // NOI18N
         divisionMenuItem.setName("divisionMenuItem"); // NOI18N
         divisionMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 divisionMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(divisionMenuItem);
 
         modMenuItem.setText(resourceMap.getString("modMenuItem.text")); // NOI18N
         modMenuItem.setName("modMenuItem"); // NOI18N
         modMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 modMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(modMenuItem);
 
         samMenuItem.setText(resourceMap.getString("samMenuItem.text")); // NOI18N
         samMenuItem.setName("samMenuItem"); // NOI18N
         samMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 samMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(samMenuItem);
 
         sammodMenuItem.setText(resourceMap.getString("sammodMenuItem.text")); // NOI18N
         sammodMenuItem.setName("sammodMenuItem"); // NOI18N
         sammodMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 sammodMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(sammodMenuItem);
 
         gcdMenuItem.setText(resourceMap.getString("gcdMenuItem.text")); // NOI18N
         gcdMenuItem.setName("gcdMenuItem"); // NOI18N
         gcdMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 gcdMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(gcdMenuItem);
 
         extendedGCDItem.setText(resourceMap.getString("extendedGCDItem.text")); // NOI18N
         extendedGCDItem.setName("extendedGCDItem"); // NOI18N
         extendedGCDItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 extendedGCDItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(extendedGCDItem);
 
         phiMenuItem.setText(resourceMap.getString("phiMenuItem.text")); // NOI18N
         phiMenuItem.setName("phiMenuItem"); // NOI18N
         phiMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 phiMenuItemActionPerformed(evt);
             }
         });
         basicArithmeticMenu.add(phiMenuItem);
 
         basicMenu.add(basicArithmeticMenu);
 
         matrixMenu.setText(resourceMap.getString("matrixMenu.text")); // NOI18N
         matrixMenu.setName("matrixMenu"); // NOI18N
 
         newMatrixMenuItem.setText(resourceMap.getString("newMatrixMenuItem.text")); // NOI18N
         newMatrixMenuItem.setName("newMatrixMenuItem"); // NOI18N
         newMatrixMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newMatrixMenuItemActionPerformed(evt);
             }
         });
         matrixMenu.add(newMatrixMenuItem);
 
         newMatrixPrimeFieldMenuItem.setText(resourceMap.getString("newMatrixPrimeFieldMenuItem.text")); // NOI18N
         newMatrixPrimeFieldMenuItem.setName("newMatrixPrimeFieldMenuItem"); // NOI18N
         newMatrixPrimeFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 newMatrixPrimeFieldMenuItemActionPerformed(evt);
             }
         });
         matrixMenu.add(newMatrixPrimeFieldMenuItem);
 
         addMatrixMenuItem.setText(resourceMap.getString("addMatrixMenuItem.text")); // NOI18N
         addMatrixMenuItem.setName("addMatrixMenuItem"); // NOI18N
         addMatrixMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addMatrixMenuItemActionPerformed(evt);
             }
         });
         matrixMenu.add(addMatrixMenuItem);
 
         matrixMultiplyMenuItem.setText(resourceMap.getString("matrixMultiplyMenuItem.text")); // NOI18N
         matrixMultiplyMenuItem.setName("matrixMultiplyMenuItem"); // NOI18N
         matrixMultiplyMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 matrixMultiplyMenuItemActionPerformed(evt);
             }
         });
         matrixMenu.add(matrixMultiplyMenuItem);
 
         basicMenu.add(matrixMenu);
 
         zMenuItem.setText(resourceMap.getString("zMenuItem.text")); // NOI18N
         zMenuItem.setName("zMenuItem"); // NOI18N
         zMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 zMenuItemActionPerformed(evt);
             }
         });
         basicMenu.add(zMenuItem);
 
         primeFieldMenuItem.setText(resourceMap.getString("primeFieldMenuItem.text")); // NOI18N
         primeFieldMenuItem.setName("primeFieldMenuItem"); // NOI18N
         primeFieldMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 primeFieldMenuItemActionPerformed(evt);
             }
         });
         basicMenu.add(primeFieldMenuItem);
 
         mainMenuBar.add(basicMenu);
 
         primeTestMenu.setText(resourceMap.getString("primeTestMenu.text")); // NOI18N
         primeTestMenu.setName("primeTestMenu"); // NOI18N
         primeTestMenu.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 primeTestMenuActionPerformed(evt);
             }
         });
 
         fermatMenuItem.setText(resourceMap.getString("fermatMenuItem.text")); // NOI18N
         fermatMenuItem.setName("fermatMenuItem"); // NOI18N
         fermatMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 fermatMenuItemActionPerformed(evt);
             }
         });
         primeTestMenu.add(fermatMenuItem);
 
         lucasMenuItem.setText(resourceMap.getString("lucasMenuItem.text")); // NOI18N
         lucasMenuItem.setName("lucasMenuItem"); // NOI18N
         lucasMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lucasMenuItemActionPerformed(evt);
             }
         });
         primeTestMenu.add(lucasMenuItem);
 
         rabinMenuItem.setText(resourceMap.getString("rabinMenuItem.text")); // NOI18N
         rabinMenuItem.setName("rabinMenuItem"); // NOI18N
         rabinMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 rabinMenuItemActionPerformed(evt);
             }
         });
         primeTestMenu.add(rabinMenuItem);
 
         mainMenuBar.add(primeTestMenu);
 
         coderMenu.setText(resourceMap.getString("coderMenu.text")); // NOI18N
         coderMenu.setName("coderMenu"); // NOI18N
 
         hammingMenu.setText(resourceMap.getString("hammingMenu.text")); // NOI18N
         hammingMenu.setName("hammingMenu"); // NOI18N
 
         initHammingCodeMenuItem.setText(resourceMap.getString("initHammingCodeMenuItem.text")); // NOI18N
         initHammingCodeMenuItem.setName("initHammingCodeMenuItem"); // NOI18N
         initHammingCodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 initHammingCodeMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(initHammingCodeMenuItem);
 
         encodeMenuItem.setText(resourceMap.getString("encodeMenuItem.text")); // NOI18N
         encodeMenuItem.setName("encodeMenuItem"); // NOI18N
         encodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 encodeMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(encodeMenuItem);
 
         syndromMenuItem.setText(resourceMap.getString("syndromMenuItem.text")); // NOI18N
         syndromMenuItem.setName("syndromMenuItem"); // NOI18N
         syndromMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 syndromMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(syndromMenuItem);
 
         decodeMenuItem.setText(resourceMap.getString("decodeMenuItem.text")); // NOI18N
         decodeMenuItem.setName("decodeMenuItem"); // NOI18N
         decodeMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 decodeMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(decodeMenuItem);
 
         createErrorsMenuItem.setText(resourceMap.getString("createErrorsMenuItem.text")); // NOI18N
         createErrorsMenuItem.setName("createErrorsMenuItem"); // NOI18N
         createErrorsMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createErrorsMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(createErrorsMenuItem);
 
         hammingDistanceMenuItem.setText(resourceMap.getString("hammingDistanceMenuItem.text")); // NOI18N
         hammingDistanceMenuItem.setName("hammingDistanceMenuItem"); // NOI18N
         hammingDistanceMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 hammingDistanceMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(hammingDistanceMenuItem);
 
         vectorWeightMenuItem.setText(resourceMap.getString("vectorWeightMenuItem.text")); // NOI18N
         vectorWeightMenuItem.setName("vectorWeightMenuItem"); // NOI18N
         vectorWeightMenuItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vectorWeightMenuItemActionPerformed(evt);
             }
         });
         hammingMenu.add(vectorWeightMenuItem);
 
         coderMenu.add(hammingMenu);
 
         mainMenuBar.add(coderMenu);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         mainMenuBar.add(helpMenu);
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(statusMessageLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 230, Short.MAX_VALUE)
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(statusAnimationLabel)
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(statusMessageLabel)
                     .addComponent(statusAnimationLabel)
                     .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(3, 3, 3))
         );
 
         jMenu3.setText(resourceMap.getString("jMenu3.text")); // NOI18N
         jMenu3.setName("jMenu3"); // NOI18N
 
         jMenuBar1.setName("jMenuBar1"); // NOI18N
 
         jMenu5.setText(resourceMap.getString("jMenu5.text")); // NOI18N
         jMenu5.setName("jMenu5"); // NOI18N
         jMenuBar1.add(jMenu5);
 
         jMenu6.setText(resourceMap.getString("jMenu6.text")); // NOI18N
         jMenu6.setName("jMenu6"); // NOI18N
         jMenuBar1.add(jMenu6);
 
         jMenuBar2.setName("jMenuBar2"); // NOI18N
 
         jMenu7.setText(resourceMap.getString("jMenu7.text")); // NOI18N
         jMenu7.setName("jMenu7"); // NOI18N
         jMenuBar2.add(jMenu7);
 
         jMenu8.setText(resourceMap.getString("jMenu8.text")); // NOI18N
         jMenu8.setName("jMenu8"); // NOI18N
         jMenuBar2.add(jMenu8);
 
         jMenuBar3.setName("jMenuBar3"); // NOI18N
 
         jMenu9.setText(resourceMap.getString("jMenu9.text")); // NOI18N
         jMenu9.setName("jMenu9"); // NOI18N
         jMenuBar3.add(jMenu9);
 
         jMenu10.setText(resourceMap.getString("jMenu10.text")); // NOI18N
         jMenu10.setName("jMenu10"); // NOI18N
         jMenuBar3.add(jMenu10);
 
         setComponent(mainPanel);
         setMenuBar(mainMenuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     private void zMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zMenuItemActionPerformed
         Kit kit = new ZFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_zMenuItemActionPerformed
 
     private void additionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_additionMenuItemActionPerformed
         Kit kit = new AdditionFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_additionMenuItemActionPerformed
 
     private void subtractionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_subtractionMenuItemActionPerformed
         Kit kit = new SubtractFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_subtractionMenuItemActionPerformed
 
     private void multiplicationMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiplicationMenuItemActionPerformed
         Kit kit = new MultiplicationFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_multiplicationMenuItemActionPerformed
 
     private void divisionMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_divisionMenuItemActionPerformed
         Kit kit = new DivisionFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_divisionMenuItemActionPerformed
 
     private void decodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decodeMenuItemActionPerformed
         Kit kit = new DecodeHammingCodeFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_decodeMenuItemActionPerformed
 
     private void startMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startMenuItemActionPerformed
         startCalculation();
     }//GEN-LAST:event_startMenuItemActionPerformed
 
     private void startCalculation() {
         ResultFrame rf = new ResultFrame();
         rf.setSize(desktop.getWidth(), desktop.getHeight() / 2);
         rf.setLocation(0, desktop.getHeight() / 2);
         rf.setVisible(true);
         desktop.add(rf);
         executor = new Executor(handler, rf, progressBar);
         executor.start();
     }
 
     private void primeFieldMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primeFieldMenuItemActionPerformed
         Kit kit = new PrimeFieldElementFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_primeFieldMenuItemActionPerformed
 
     private void modMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modMenuItemActionPerformed
         Kit kit = new ModuloFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_modMenuItemActionPerformed
 
     private void samMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samMenuItemActionPerformed
         Kit kit = new SaMFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_samMenuItemActionPerformed
 
     private void sammodMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sammodMenuItemActionPerformed
         Kit kit = new SaMModFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_sammodMenuItemActionPerformed
 
     private void gcdMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gcdMenuItemActionPerformed
         Kit kit = new GCDFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_gcdMenuItemActionPerformed
 
     private void initHammingCodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_initHammingCodeMenuItemActionPerformed
         Kit kit = new InitHammingFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_initHammingCodeMenuItemActionPerformed
 
     private void hammingDistanceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hammingDistanceMenuItemActionPerformed
         Kit kit = new HammingDistanceFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_hammingDistanceMenuItemActionPerformed
 
     private void vectorWeightMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_vectorWeightMenuItemActionPerformed
         Kit kit = new HammingWeightFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_vectorWeightMenuItemActionPerformed
 
     private void encodeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encodeMenuItemActionPerformed
         Kit kit = new EncodeHammingCodeFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_encodeMenuItemActionPerformed
 
     private void syndromMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syndromMenuItemActionPerformed
         Kit kit = new HammingSyndromFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_syndromMenuItemActionPerformed
 
     private void phiMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_phiMenuItemActionPerformed
         Kit kit = new PhiFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_phiMenuItemActionPerformed
 
     private void fermatMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fermatMenuItemActionPerformed
         Kit kit = new FermatFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_fermatMenuItemActionPerformed
 
     private void createErrorsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createErrorsMenuItemActionPerformed
         Kit kit = new CreateHammingErrorFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_createErrorsMenuItemActionPerformed
 
     private void rabinMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rabinMenuItemActionPerformed
         Kit kit = new MillerRabinFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_rabinMenuItemActionPerformed
 
     private void primeTestMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_primeTestMenuActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_primeTestMenuActionPerformed
 
     private void lucasMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lucasMenuItemActionPerformed
         Kit kit = new LucasFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_lucasMenuItemActionPerformed
 
     private void newMatrixMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMatrixMenuItemActionPerformed
         Kit kit = new MatrixFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_newMatrixMenuItemActionPerformed
 
     private void addMatrixMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addMatrixMenuItemActionPerformed
         Kit kit = new MatrixAddFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_addMatrixMenuItemActionPerformed
 
     private void matrixMultiplyMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matrixMultiplyMenuItemActionPerformed
         Kit kit = new MatrixMultiplyFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_matrixMultiplyMenuItemActionPerformed
 
     private void matrixPrimeFieldItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matrixPrimeFieldItemActionPerformed
         Kit kit = new MatrixPrimeFieldFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_matrixPrimeFieldItemActionPerformed
 
     private void extendedGCDItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_extendedGCDItemActionPerformed
         Kit kit = new ExtendedGCDFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_extendedGCDItemActionPerformed
 
     private void startBtnOnBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startBtnOnBarActionPerformed
         startCalculation();
     }//GEN-LAST:event_startBtnOnBarActionPerformed
 
     private void cancelBtnOnBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelBtnOnBarActionPerformed
         executor.stop();
         progressBar.setVisible(false);
     }//GEN-LAST:event_cancelBtnOnBarActionPerformed
 
     private void clearBtnOnBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearBtnOnBarActionPerformed
         desktop.removeAll();
         desktop.repaint();
     }//GEN-LAST:event_clearBtnOnBarActionPerformed
 
     private void newMatrixPrimeFieldMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newMatrixPrimeFieldMenuItemActionPerformed
         Kit kit = new MatrixPrimeFieldFrame(handler);
         kit.setVisible(true);
         desktop.add(kit);
     }//GEN-LAST:event_newMatrixPrimeFieldMenuItemActionPerformed
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JMenuItem addMatrixMenuItem;
     private javax.swing.JMenuItem additionMenuItem;
     private javax.swing.JMenu basicArithmeticMenu;
     private javax.swing.JMenu basicMenu;
     private javax.swing.JButton cancelBtnOnBar;
     private javax.swing.JButton clearBtnOnBar;
     private javax.swing.JMenu coderMenu;
     private javax.swing.JMenuItem createErrorsMenuItem;
     private javax.swing.JMenuItem decodeMenuItem;
     private javax.swing.JMenuItem divisionMenuItem;
     private javax.swing.JMenuItem encodeMenuItem;
     private javax.swing.JMenuItem extendedGCDItem;
     private javax.swing.JMenuItem fermatMenuItem;
     private javax.swing.JMenuItem gcdMenuItem;
     private javax.swing.JMenuItem hammingDistanceMenuItem;
     private javax.swing.JMenu hammingMenu;
     private javax.swing.JMenuItem initHammingCodeMenuItem;
     private javax.swing.JMenu jMenu10;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenu jMenu5;
     private javax.swing.JMenu jMenu6;
     private javax.swing.JMenu jMenu7;
     private javax.swing.JMenu jMenu8;
     private javax.swing.JMenu jMenu9;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuBar jMenuBar2;
     private javax.swing.JMenuBar jMenuBar3;
     private javax.swing.JMenuItem lucasMenuItem;
     private javax.swing.JPanel mainForm;
     private javax.swing.JMenuBar mainMenuBar;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenu matrixMenu;
     private javax.swing.JMenuItem matrixMultiplyMenuItem;
     private javax.swing.JMenuItem modMenuItem;
     private javax.swing.JMenuItem multiplicationMenuItem;
     private javax.swing.JMenuItem newMatrixMenuItem;
     private javax.swing.JMenuItem newMatrixPrimeFieldMenuItem;
     private javax.swing.JMenuItem phiMenuItem;
     private javax.swing.JMenuItem primeFieldMenuItem;
     private javax.swing.JMenu primeTestMenu;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JMenuItem rabinMenuItem;
     private javax.swing.JMenuItem samMenuItem;
     private javax.swing.JMenuItem sammodMenuItem;
     private javax.swing.JPanel secondBar;
     private javax.swing.JButton startBtnOnBar;
     private javax.swing.JMenuItem startMenuItem;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     private javax.swing.JMenuItem subtractionMenuItem;
     private javax.swing.JMenuItem syndromMenuItem;
     private javax.swing.JMenuItem vectorWeightMenuItem;
     private javax.swing.JMenuItem zMenuItem;
     // End of variables declaration//GEN-END:variables
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
     private JDialog aboutBox;
 }
