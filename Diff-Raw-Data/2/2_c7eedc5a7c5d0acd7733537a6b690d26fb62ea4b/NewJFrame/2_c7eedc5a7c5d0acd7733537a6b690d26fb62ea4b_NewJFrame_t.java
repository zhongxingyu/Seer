 package Jform;
 
 import java.io.*;
 import java.util.Set;
 import java.util.TreeSet;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 
 public class NewJFrame extends javax.swing.JFrame implements Runnable{
     GroupTask Task;
     File textFile;
     File HTMLFile;
     int sizeRestriction;
     int progress = 0;
     boolean isStopped = false;
     
     public NewJFrame() {
         initComponents();
         SpinnerModel model = new SpinnerNumberModel(0, //initial value
                 0, //min
                 100000, //max
                 100);
         SpinnerSizeRestriction.setModel(model);
         
         ProgressBarByBytes.setStringPainted(true);
         ProgressBarByBytes.setValue(0);
         
         ButtonStop.setEnabled(false);
         
         Task = new GroupTask();
     }
     
     private void changeEnable(){
         ButtonChooseDictFile.setEnabled(!ButtonChooseDictFile.isEnabled());
         ButtonChooseHTMLFile.setEnabled(!ButtonChooseHTMLFile.isEnabled());
         ButtonChooseTextFile.setEnabled(!ButtonChooseTextFile.isEnabled());
         ButtonStartMainProcess.setEnabled(!ButtonStartMainProcess.isEnabled());
         CheckBoxIsRestrictOutSize.setEnabled(!CheckBoxIsRestrictOutSize.isEnabled());
         SpinnerSizeRestriction.setEnabled(false);
         ButtonStop.setEnabled(!ButtonStop.isEnabled());
     }
     
     @Override
     public void run(){
         ProgressBarByBytes.setValue(progress);
         if(isStopped){
            Task.stop();
            isStopped = false;
            changeEnable();
            JOptionPane.showMessageDialog(null, "Прервано пользователем.", "Прервано", JOptionPane.WARNING_MESSAGE);
         }
     }
     
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         LabelTextFileName = new javax.swing.JLabel();
         ButtonChooseDictFile = new javax.swing.JButton();
         LabelDictFileName = new javax.swing.JLabel();
         CheckBoxIsReadyTextFile = new javax.swing.JCheckBox();
         CheckBoxIsReadyDictFile = new javax.swing.JCheckBox();
         ButtonChooseTextFile = new javax.swing.JButton();
         ButtonChooseHTMLFile = new javax.swing.JButton();
         SpinnerSizeRestriction = new javax.swing.JSpinner();
         CheckBoxIsReadyHTMLFile = new javax.swing.JCheckBox();
         LabelHTMLFileName = new javax.swing.JLabel();
         CheckBoxIsRestrictOutSize = new javax.swing.JCheckBox();
         ButtonStartMainProcess = new javax.swing.JButton();
         ProgressBarByBytes = new javax.swing.JProgressBar();
         ButtonStop = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         LabelTextFileName.setText("<< имя файла с текстом>>");
 
         ButtonChooseDictFile.setText("Выбрать файл со словарем");
         ButtonChooseDictFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ButtonChooseDictFileActionPerformed(evt);
             }
         });
 
         LabelDictFileName.setText("<< имя файла со словарем>>");
 
         CheckBoxIsReadyTextFile.setText("Готовность к обработке");
         CheckBoxIsReadyTextFile.setEnabled(false);
 
         CheckBoxIsReadyDictFile.setText("Готовность к обработке");
         CheckBoxIsReadyDictFile.setEnabled(false);
 
         ButtonChooseTextFile.setText("Выбрать файл с текстом");
         ButtonChooseTextFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ButtonChooseTextFileActionPerformed(evt);
             }
         });
 
         ButtonChooseHTMLFile.setText("Выбрать выходной файл");
         ButtonChooseHTMLFile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ButtonChooseHTMLFileActionPerformed(evt);
             }
         });
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, CheckBoxIsRestrictOutSize, org.jdesktop.beansbinding.ELProperty.create("${selected}"), SpinnerSizeRestriction, org.jdesktop.beansbinding.BeanProperty.create("enabled"));
         bindingGroup.addBinding(binding);
 
         CheckBoxIsReadyHTMLFile.setText("Готовность к обработке");
         CheckBoxIsReadyHTMLFile.setEnabled(false);
 
         LabelHTMLFileName.setText("<< имя выходного файла>>");
 
         CheckBoxIsRestrictOutSize.setText("Ограничить размер выходного файла");
 
         ButtonStartMainProcess.setText("Начать обработку");
         ButtonStartMainProcess.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ButtonStartMainProcessActionPerformed(evt);
             }
         });
 
         ButtonStop.setText("Прервать");
         ButtonStop.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ButtonStopActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addComponent(CheckBoxIsRestrictOutSize, javax.swing.GroupLayout.PREFERRED_SIZE, 303, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(SpinnerSizeRestriction, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addGap(0, 0, Short.MAX_VALUE))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(LabelTextFileName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addComponent(CheckBoxIsReadyTextFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                     .addComponent(ButtonChooseTextFile, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                 .addGap(18, 18, 18)
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(ButtonChooseDictFile)
                                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                         .addGap(3, 3, 3)
                                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                             .addComponent(CheckBoxIsReadyDictFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                             .addComponent(LabelDictFileName))))))
                         .addGap(18, 18, 18)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                             .addComponent(CheckBoxIsReadyHTMLFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(ButtonChooseHTMLFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(LabelHTMLFileName, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .addComponent(ButtonStartMainProcess, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(ProgressBarByBytes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addGap(18, 18, 18)
                         .addComponent(ButtonStop)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(ButtonChooseHTMLFile)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(ButtonChooseTextFile)
                         .addGap(18, 18, 18)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(LabelTextFileName)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(CheckBoxIsReadyTextFile))
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(LabelHTMLFileName)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addComponent(CheckBoxIsReadyHTMLFile))))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(ButtonChooseDictFile)
                         .addGap(18, 18, 18)
                         .addComponent(LabelDictFileName)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(CheckBoxIsReadyDictFile)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(CheckBoxIsRestrictOutSize)
                     .addComponent(SpinnerSizeRestriction, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(ButtonStartMainProcess))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(ButtonStop, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(ProgressBarByBytes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         bindingGroup.bind();
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void ButtonChooseTextFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonChooseTextFileActionPerformed
         if ((textFile = Task.selectFile("open")) != null) {
             LabelTextFileName.setText(textFile.getName());
             CheckBoxIsReadyTextFile.setSelected(true);
         }
     }//GEN-LAST:event_ButtonChooseTextFileActionPerformed
 
     private void ButtonChooseDictFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonChooseDictFileActionPerformed
         File dictFile;
         if ((dictFile = Task.selectFile("open")) != null) {
             Task.readDict(dictFile);
             if (Task.dict != null) {
                 LabelDictFileName.setText(dictFile.getName());
                 CheckBoxIsReadyDictFile.setSelected(true);
             }
         }
     }//GEN-LAST:event_ButtonChooseDictFileActionPerformed
 
     private void ButtonChooseHTMLFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonChooseHTMLFileActionPerformed
         if ((HTMLFile = Task.selectFile("save")) != null) {
             LabelHTMLFileName.setText(HTMLFile.getName());
             CheckBoxIsReadyHTMLFile.setSelected(true);
         }
     }//GEN-LAST:event_ButtonChooseHTMLFileActionPerformed
 
     private void ButtonStartMainProcessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ButtonStartMainProcessActionPerformed
         if(!(CheckBoxIsReadyHTMLFile.isSelected() && 
                 CheckBoxIsReadyTextFile.isSelected()&& 
                 CheckBoxIsReadyDictFile.isSelected())){
             JOptionPane.showMessageDialog(null, "Ошибка! Указаны не все файлы.", "Ошибка", JOptionPane.ERROR_MESSAGE);
             return;
         }
         sizeRestriction = Integer.valueOf(SpinnerSizeRestriction.getValue().toString());
         if (CheckBoxIsRestrictOutSize.isSelected()) {
             if(sizeRestriction < 10){
                 JOptionPane.showMessageDialog(null, "Ошибка! Ограничение должно быть больше 9.", "Ошибка", JOptionPane.ERROR_MESSAGE);
                 return;
             }
         }
         changeEnable();
         isStopped = false;
         Task = new GroupTask();
         Task.start();
     }//GEN-LAST:event_ButtonStartMainProcessActionPerformed
 
     private void ButtonStopActionPerformed(java.awt.event.ActionEvent evt) {
         isStopped = true;
     }
 
     public static void main(String args[]) {
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
 
 
 
 
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new NewJFrame().setVisible(true);
             }
         });
     }
     
     public class GroupTask extends Thread {
         Set<String> dict = new TreeSet();//{"a", "strong", "kill", ""};
         private long curProgress = 0;
         
         private BufferedReader openFileForRead(File file) {
             try {
                 return new BufferedReader(new InputStreamReader(new FileInputStream(file)));
             } catch (FileNotFoundException ex) {
                 JOptionPane.showMessageDialog(null, ex.getMessage(), "FileNotFoundException", JOptionPane.ERROR_MESSAGE);
                 return null;
             }
         }
 
         private BufferedWriter openFileForWrite(File file) {
             try {
                 return new BufferedWriter(new FileWriter(file));
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(null, ex.getMessage(), "FileNotFoundException", JOptionPane.ERROR_MESSAGE);
                 return null;
             }
         }
 
         public File selectFile(String mode) {
            JFileChooser FileChooserOpen = new JFileChooser("FilesForTests");
             int ret = (mode.equalsIgnoreCase("save")) ? FileChooserOpen.showSaveDialog(null) : FileChooserOpen.showOpenDialog(null);
             if (ret == JFileChooser.APPROVE_OPTION) {
                 return FileChooserOpen.getSelectedFile();
             }
             return null;
         }
 
         private void closeReadStream(BufferedReader bufReader) {
             if (bufReader != null) {
                 try {
                     bufReader.close();
                 } catch (IOException ex) {
                     JOptionPane.showMessageDialog(null, ex.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
 
         private void closeWriteStream(BufferedWriter bufWriter) {
             if (bufWriter != null) {
                 try {
                     bufWriter.close();
                 } catch (IOException ex) {
                     JOptionPane.showMessageDialog(null, ex.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
                 }
             }
         }
 
         public void readDict(File dictFile) {
             BufferedReader bufReader;
             if ((bufReader = openFileForRead(dictFile)) != null) {
                 String line;
                 try {
                     while ((line = bufReader.readLine()) != null) {
                         dict.add(line);
                     }
                 } catch (IOException ex) {
                     JOptionPane.showMessageDialog(null, ex.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
                 } finally {
                     closeReadStream(bufReader);
                 }
             } else {
                 dict = null;
             }
         }
 
         private String processOneString(String str) {
             final int SIZE_OF_CHANGING = 7;// 7 = <b></b>.length()
             StringBuilder res = new StringBuilder(str);
             for (String wordFromDict : dict) {
                 if (wordFromDict.length() > str.length()) {
                     continue;
                 }
                 for (int replaceIndex = res.indexOf(wordFromDict, 0); replaceIndex != -1; replaceIndex = res.indexOf(wordFromDict, replaceIndex + SIZE_OF_CHANGING)) {
                     String separators = "!@#$%^&*():;\"\' ,\\./[]{}|-+=";
                     int endWordIndex = replaceIndex + wordFromDict.length();
                     if (endWordIndex != res.length() && (separators.indexOf(res.charAt(endWordIndex)) == -1)) {
                         continue;
                     }
                     StringBuilder replacedStr = new StringBuilder(wordFromDict.length() + SIZE_OF_CHANGING);
                     replacedStr.append("<b>");
                     replacedStr.append(wordFromDict);
                     replacedStr.append("</b>");
                     res.replace(replaceIndex, endWordIndex, replacedStr.toString());
                 }
             }
             return res.toString();
         }
 
         /**
          * Основная обработка исходного текстового файла в HTML файл длиной не более sizeRestriction,
          * после достижения этой длины выбирается другой выходной файл.
          * Открываем потоки на чтение и запись. 
          * Построчно счтываем, обрабатываем строку функцией processOneString(String) и записываем в выходной файл.
          * После достижения допустимой длины закрывается поток на запись, выбирается новый выходной файл,
          * снова открывается поток на запись и дальше процесс повторяется.
          * @param sizeRestriction максимально допустимый размер выходного файла.
          */
         
         public int currentProgress(long length, String cur){
             curProgress += cur.getBytes().length * 100;
             progress = (int) (curProgress/length);
             return progress;
         }
         
         @Override
         public void run() {
             BufferedReader bufReader = openFileForRead(textFile);
             BufferedWriter bufWriter = openFileForWrite(HTMLFile);
             progress = 0;
             SwingUtilities.invokeLater( NewJFrame.this );
             long fullLength = textFile.length();
             int counter = (sizeRestriction > 0) ? 0 : -1;
             try {
                 bufWriter.write("<html>\n<body>\n");
                 String line;
                 while ((line = bufReader.readLine()) != null) {
                     if(isStopped){
                         break;
                     }
                     progress = currentProgress(fullLength, line);
                     line = "<p>" + processOneString(line) + "</p>";
                     bufWriter.write(line);
                     bufWriter.newLine();
                     SwingUtilities.invokeLater( NewJFrame.this );
                     if(counter == -1){
                         continue;
                     }
                     counter++;
                     if (counter >= sizeRestriction) {
                         counter = 0;
                         bufWriter.write("</html>\n</body>\n");
                         closeWriteStream(bufWriter);
                         HTMLFile = null;
                         if ((HTMLFile = selectFile("save")) != null) {
                             bufWriter = openFileForWrite(HTMLFile);
                             bufWriter.write("<html>\n<body>\n");
                         }
                     }
                 }
                 if(!isStopped){
                     progress = 100;
                     SwingUtilities.invokeLater( NewJFrame.this );
                     bufWriter.write("</html>\n</body>\n");
                     JOptionPane.showMessageDialog(null, "Готово!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                 }
             } catch (IOException ex) {
                 JOptionPane.showMessageDialog(null, ex.getMessage(), "IOException", JOptionPane.ERROR_MESSAGE);
             } finally {
                 closeReadStream(bufReader);
                 closeWriteStream(bufWriter);
                 NewJFrame.this.changeEnable();
                 progress = 0;
                 SwingUtilities.invokeLater( NewJFrame.this );
             }
         }
     }
 
     
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton ButtonChooseDictFile;
     private javax.swing.JButton ButtonChooseHTMLFile;
     private javax.swing.JButton ButtonChooseTextFile;
     private javax.swing.JButton ButtonStartMainProcess;
     private javax.swing.JButton ButtonStop;
     private javax.swing.JCheckBox CheckBoxIsReadyDictFile;
     private javax.swing.JCheckBox CheckBoxIsReadyHTMLFile;
     private javax.swing.JCheckBox CheckBoxIsReadyTextFile;
     private javax.swing.JCheckBox CheckBoxIsRestrictOutSize;
     private javax.swing.JLabel LabelDictFileName;
     private javax.swing.JLabel LabelHTMLFileName;
     private javax.swing.JLabel LabelTextFileName;
     private javax.swing.JProgressBar ProgressBarByBytes;
     private javax.swing.JSpinner SpinnerSizeRestriction;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
 }
