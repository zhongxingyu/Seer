 /*
  * ADA95_Compilador_10611066View.java
  */
 package ada95_compilador_10611066;
 
 import java.awt.Color;
 import java.io.FileNotFoundException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.nio.CharBuffer;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import java_cup.runtime.*;
 /**
  * The application's main frame.
  */
 public class ADA95_Compilador_10611066View extends FrameView {
 
     public ADA95_Compilador_10611066View(SingleFrameApplication app) {
         super(app);
 
         initComponents();
         this.redirectSystemStreams();
         
         /*Setear el highlighter del editor*/
         this.jEditorPaneDocDisplay.setEditorKit(new highlightKit());
         this.jEditorPaneDocDisplay.setDocument(new HighlightDocumentAda95());
         this.jFileChooser1.setFileFilter(new AdaFilter());
         this.jTabbedPane1.setTitleAt(0, "Salida");
         this.errorArea.setForeground(Color.red);
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
     }
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = ADA95_Compilador_10611066App.getApplication().getMainFrame();
             aboutBox = new ADA95_Compilador_10611066AboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         ADA95_Compilador_10611066App.getApplication().show(aboutBox);
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
         jScrollPane1 = new javax.swing.JScrollPane();
         jEditorPaneDocDisplay = new javax.swing.JEditorPane();
         jToolBar1 = new javax.swing.JToolBar();
         jButton1 = new javax.swing.JButton();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jScrollPane2 = new javax.swing.JScrollPane();
         errorArea = new javax.swing.JTextArea();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         jMenuINuevo = new javax.swing.JMenuItem();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenuGuardar = new javax.swing.JMenuItem();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         progressBar = new javax.swing.JProgressBar();
         jFileChooser1 = new javax.swing.JFileChooser();
 
         mainPanel.setName("mainPanel"); // NOI18N
         mainPanel.setPreferredSize(new java.awt.Dimension(1000, 1000));
 
         jScrollPane1.setName("jScrollPane1"); // NOI18N
 
         jEditorPaneDocDisplay.setName("jEditorPaneDocDisplay"); // NOI18N
         jScrollPane1.setViewportView(jEditorPaneDocDisplay);
 
         jToolBar1.setRollover(true);
         jToolBar1.setName("jToolBar1"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(ada95_compilador_10611066.ADA95_Compilador_10611066App.class).getContext().getResourceMap(ADA95_Compilador_10611066View.class);
         jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
         jButton1.setFocusable(false);
         jButton1.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         jButton1.setName("jButton1"); // NOI18N
         jButton1.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
         jToolBar1.add(jButton1);
 
         jTabbedPane1.setName("jTabbedPane1"); // NOI18N
 
         jScrollPane2.setName("jScrollPane2"); // NOI18N
 
         errorArea.setColumns(20);
         errorArea.setEditable(false);
         errorArea.setForeground(resourceMap.getColor("errorArea.foreground")); // NOI18N
         errorArea.setRows(5);
         errorArea.setText(resourceMap.getString("errorArea.text")); // NOI18N
         errorArea.setName("errorArea"); // NOI18N
         jScrollPane2.setViewportView(errorArea);
 
         jTabbedPane1.addTab(resourceMap.getString("jScrollPane2.TabConstraints.tabTitle"), jScrollPane2); // NOI18N
 
         javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 1176, Short.MAX_VALUE)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1152, Short.MAX_VALUE)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1152, Short.MAX_VALUE))
                 .addContainerGap())
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(mainPanelLayout.createSequentialGroup()
                 .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 691, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         jMenuINuevo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         jMenuINuevo.setText(resourceMap.getString("jMenuINuevo.text")); // NOI18N
         jMenuINuevo.setName("jMenuINuevo"); // NOI18N
         jMenuINuevo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuINuevoActionPerformed(evt);
             }
         });
         fileMenu.add(jMenuINuevo);
 
         jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
         jMenuItem1.setName("jMenuItem1"); // NOI18N
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         fileMenu.add(jMenuItem1);
 
         jMenuGuardar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
         jMenuGuardar.setText(resourceMap.getString("jMenuGuardar.text")); // NOI18N
         jMenuGuardar.setName("jMenuGuardar"); // NOI18N
         jMenuGuardar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuGuardarActionPerformed(evt);
             }
         });
         fileMenu.add(jMenuGuardar);
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(ada95_compilador_10611066.ADA95_Compilador_10611066App.class).getContext().getActionMap(ADA95_Compilador_10611066View.class, this);
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setText(resourceMap.getString("exitMenuItem.text")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         menuBar.add(helpMenu);
 
         statusPanel.setName("statusPanel"); // NOI18N
         statusPanel.setPreferredSize(new java.awt.Dimension(1027, 50));
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         jSeparator1.setName("jSeparator1"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(1160, Short.MAX_VALUE))
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                 .addContainerGap(835, Short.MAX_VALUE)
                 .addComponent(statusAnimationLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(278, 278, 278))
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addGap(23, 23, 23)
                 .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 961, Short.MAX_VALUE)
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(statusAnimationLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, statusPanelLayout.createSequentialGroup()
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addComponent(statusMessageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(20, 20, 20))
             .addGroup(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(20, Short.MAX_VALUE))
         );
 
         jFileChooser1.setName("jFileChooser1"); // NOI18N
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     /**Métodos para redirigir las system outs:
      * Sacados de http://unserializableone.blogspot.com/2009/01/redirecting-systemout-and-systemerr-to.html
      */
 
     /*Lo hace en un thread:*/
     private void updateTextArea(final String text) {
         SwingUtilities.invokeLater(new Runnable() {
         public void run() {
         errorArea.append(text);
         }
          });
     }
 
     /*Los redirige:*/
     private void redirectSystemStreams() {
    OutputStream out = new OutputStream() {  
      @Override
      public void write(int b) throws IOException {
        updateTextArea(String.valueOf((char) b));
      }
 
      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        updateTextArea(new String(b, off, len));
      }
 
      @Override
      public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
      }
    };
 
    System.setOut(new PrintStream(out, true));
    System.setErr(new PrintStream(out, true));
  }
     /**Para leer el contenido de un archivo de texto: sacado de 
     http://www.javapractices.com/topic/TopicAction.do?Id=42*/
     static public String getContents(File aFile) {
         //...checks on aFile are elided
         StringBuilder contents = new StringBuilder();
 
         try {
             //use buffering, reading one line at a time
             //FileReader always assumes default encoding is OK!
             BufferedReader input = new BufferedReader(new FileReader(aFile));
             try {
                 String line = null; //not declared within while loop
         /*
                  * readLine is a bit quirky :
                  * it returns the content of a line MINUS the newline.
                  * it returns null only for the END of the stream.
                  * it returns an empty String if two newlines appear in a row.
                  */
                 while ((line = input.readLine()) != null) {
                     contents.append(line);
                     contents.append(System.getProperty("line.separator"));
                 }
             } finally {
                 input.close();
             }
         } catch (IOException ex) {
             ex.printStackTrace();
         }
 
         return contents.toString();
     }
 
     private void abrirArchivo() {
         this.jFileChooser1.setDialogTitle("Abrir");
         int returnVal = this.jFileChooser1.showOpenDialog(this.mainPanel);
 
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             archivo = this.jFileChooser1.getSelectedFile();
             if (archivo.exists()) {
                 textoDeArchivo = getContents(archivo);
                 this.jEditorPaneDocDisplay.setDocument(new HighlightDocumentAda95());
                 this.jEditorPaneDocDisplay.setText(textoDeArchivo);                
             } else {
                 JOptionPane.showMessageDialog(mainPanel, "El archivo " + archivo.getAbsolutePath() + " no existe");
             }
         } /*else {
         //log.append("Open command cancelled by user." + newline);
         }*/
     }
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
         // TODO add your handling code here:
         //Handle open button action.
 //    if (evt.getSource() == openButton) {
         if(archivo==null){
             abrirArchivo();
         }else{//si ya hay un archivo, preguntarle si quiere guardarlo antes:
             int confirmacion = JOptionPane.showConfirmDialog(mainPanel, "¿Desea guardar el archivo " + archivo.getName() + " antes de abrir otro?");
             if (confirmacion == JOptionPane.OK_OPTION) {
                 //guardar el archivo que está abierto
                 guardarArchivoActivo();
                 //borrar
                 this.jEditorPaneDocDisplay.setText("");
 
             } else if (confirmacion == JOptionPane.NO_OPTION) {
                 //solo borrar:
                 this.jEditorPaneDocDisplay.setText("");
             }//fin NO
             //ya terminó con la confirmación, ahora sí abrir el archivo:
             abrirArchivo();
         }
 
         // }
 
 
     }//GEN-LAST:event_jMenuItem1ActionPerformed
     private void guardarArchivoActivo() {
         //si el archivo no existe, crearlo:
         if (archivo==null) {
             this.jFileChooser1.setDialogTitle("Nombrar archivo");
             int returnVal = this.jFileChooser1.showOpenDialog(this.mainPanel);
             if (returnVal == JFileChooser.APPROVE_OPTION) {
             //poner vacío el display
                 archivo = this.jFileChooser1.getSelectedFile();
             }
 
         }
             try {
 
                 //FileWriter aGuardar = new FileWriter(archivo.getName());
                 FileWriter aGuardar = new FileWriter(archivo.getAbsolutePath());
                 aGuardar.write(this.jEditorPaneDocDisplay.getText());
                 aGuardar.close();
                 JOptionPane.showMessageDialog(mainPanel, "El archivo " + archivo.getName() + " se ha guardado exitosamente");
             } catch (IOException ex) {
                 Logger.getLogger(ADA95_Compilador_10611066View.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     
 
     private void jMenuGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuGuardarActionPerformed
         guardarArchivoActivo();
 
     }//GEN-LAST:event_jMenuGuardarActionPerformed
 
     private void jMenuINuevoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuINuevoActionPerformed
         // TODO add your handling code here:
         if (archivo.exists()) {
 
             int confirmacion = JOptionPane.showConfirmDialog(mainPanel, "¿Desea guardar el archivo " + archivo.getName() + " antes de crear uno nuevo?");
             if (confirmacion == JOptionPane.OK_OPTION) {
                 //guardar el archivo que está abierto
                 guardarArchivoActivo();
                 //borrar
                 
 
             } else if (confirmacion == JOptionPane.NO_OPTION) {
                 //solo borrar:
                 //this.jEditorPaneDocDisplay.setText("");
 
             }//fin NO
         }//fin EXISTS
 
         //mostrar el file chooser para poner nuevo nombre:
         this.jFileChooser1.setDialogTitle("Nombrar nuevo archivo");
         int returnVal = this.jFileChooser1.showOpenDialog(this.mainPanel);
         if (returnVal == JFileChooser.APPROVE_OPTION) {
             //poner vacío el display
             this.jEditorPaneDocDisplay.setText("");
             archivo = this.jFileChooser1.getSelectedFile();
         }
     }//GEN-LAST:event_jMenuINuevoActionPerformed
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
         // TODO add your handling code here:
         //prepararla para el output:
         this.errorArea.setForeground(Color.black);
         this.errorArea.setText("");
         
 
 
         
         if(archivo==null){
             this.errorArea.setForeground(Color.red);
             System.err.println("No hay un archivo abierto");
         }else{
             long start=System.currentTimeMillis();
             try {
                 //lo de parsear:
                 
                parser p = new parser(new Ada95Lexer(new FileReader(archivo.getPath())));
                Object result=p.parse();
                 //p.parse();
             } catch (FileNotFoundException ex) {
                 //Logger.getLogger(ADA95_Compilador_10611066View.class.getName()).log(Level.SEVERE, null, ex);
 
             } catch(Exception e){
                 
             }
             long end=System.currentTimeMillis();
             float elapsed=(end-start)/1000;
             //si no dio errores:
             if(!this.errorArea.getForeground().equals(Color.red)){
                 this.errorArea.setForeground(Color.green);
             }
             System.out.println("Compilación exitosa ("+elapsed+" segundos)");
             this.jTabbedPane1.setTitleAt(0, archivo.getName()+" (Compilación)");
         }
         
     }//GEN-LAST:event_jButton1ActionPerformed
 //variables de lfborjas:
     File archivo=null;
     /**El contenido del archivo en texto:*/
     String textoDeArchivo;
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextArea errorArea;
     private javax.swing.JButton jButton1;
     private javax.swing.JEditorPane jEditorPaneDocDisplay;
     private javax.swing.JFileChooser jFileChooser1;
     private javax.swing.JMenuItem jMenuGuardar;
     private javax.swing.JMenuItem jMenuINuevo;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JToolBar jToolBar1;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     // End of variables declaration//GEN-END:variables
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
     private JDialog aboutBox;
 }
