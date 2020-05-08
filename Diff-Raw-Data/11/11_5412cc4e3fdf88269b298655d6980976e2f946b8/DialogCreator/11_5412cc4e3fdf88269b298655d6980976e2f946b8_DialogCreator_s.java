 package com.dfgames.lastplanet.tools.dialog_creator;
 
 import com.dfgames.lastplanet.tools.dialog_creator.utils.FileOperator;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Author: Ivan Melnikov
  * Date: 11.11.12 11:26
  */
 public class DialogCreator extends JFrame {
     private JTextArea textArea;
 
     public DialogCreator() {
         textArea = new JTextArea("");
         initGUI();
         initMenu();
     }
 
     private void initMenu() {
         final JMenuBar menuBar = new JMenuBar();
 
         JMenu file = new JMenu("File");
         JMenu example = new JMenu("Example");
 
         file.setMnemonic(KeyEvent.VK_F);
 
         JMenuItem exitMenuItem = new JMenuItem("Exit");
         exitMenuItem.setToolTipText("Exit application");
         exitMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent event) {
                 close();
             }
         });
 
         JMenuItem newFileMenuItem = new JMenuItem("New file");
         newFileMenuItem.setToolTipText("Create new file");
         newFileMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 newFile();
             }
         });
 
         final JMenuItem openFileMenuItem = new JMenuItem("Open file");
         openFileMenuItem.setToolTipText("Open file");
         openFileMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 openFile(openFileMenuItem);
             }
         });
 
         final JMenuItem saveFileMenuItem = new JMenuItem("Save file");
         saveFileMenuItem.setToolTipText("Save file");
         saveFileMenuItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 saveFile(saveFileMenuItem);
             }
         });
 
         JMenuItem exampleInputText = new JMenuItem("Example input text");
         exampleInputText.setToolTipText("Example input text");
         exampleInputText.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 showExampleText();
             }
         });
 
         JMenuItem exampleXmlStricture = new JMenuItem("Example xml stricture");
         exampleXmlStricture.setToolTipText("Example xml stricture");
         exampleXmlStricture.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 showExampleXml();
             }
         });
 
         file.add(newFileMenuItem);
         file.add(openFileMenuItem);
         file.add(saveFileMenuItem);
         file.add(exitMenuItem);
         example.add(exampleInputText);
         example.add(exampleXmlStricture);
         menuBar.add(file);
         menuBar.add(example);
 
         setJMenuBar(menuBar);
     }
 
     private void initGUI() {
         setTitle("Dialog Creator");
         setSize(640, 640);
         setLayout(new BorderLayout());
 
         JPanel panel = new JPanel();
         JScrollPane pane = new JScrollPane();
         pane.setSize(640, 640);
 
         pane.getViewport().add(textArea);
         panel.add(pane);
 
         add(initContent(), BorderLayout.CENTER);
 
         add(new JLabel("Notice:  Before the first message should not be a gap after each one empty line."), BorderLayout.NORTH);
         setLocationRelativeTo(null);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setVisible(true);
     }
 
     private JPanel initContent() {
         JPanel panel = new JPanel();
         panel.setLayout(new BorderLayout());
         JScrollPane pane = new JScrollPane();
         textArea.setLineWrap(true);
         textArea.setWrapStyleWord(true);
         pane.getViewport().add(textArea);
         panel.add(pane);
         return panel;
     }
 
     private void newFile() {
         textArea.setText("");
     }
 
     private void openFile(JMenuItem menuItem) {
         JFileChooser openFileChooser = new JFileChooser();
         FileFilter filter = new FileNameExtensionFilter("xml files", "xml");
         openFileChooser.setFileFilter(filter);
         openFileChooser.setCurrentDirectory(new File("C:" + File.separator));
         int ret = openFileChooser.showDialog(menuItem, "Open file");
 
         if (ret == JFileChooser.APPROVE_OPTION) {
             File file = openFileChooser.getSelectedFile();
             String text = FileOperator.readFile(file);
             textArea.setText(text);
         }
     }
 
     private void saveFile(JMenuItem menuItem) {
         JFileChooser saveFileChooser = new JFileChooser();
         FileFilter filter = new FileNameExtensionFilter("xml files", "xml");
         saveFileChooser.setFileFilter(filter);
         saveFileChooser.setCurrentDirectory(new File("C:" + File.separator));
         int ret = saveFileChooser.showSaveDialog(menuItem);
 
         if (ret == JFileChooser.APPROVE_OPTION) {
             File file = saveFileChooser.getSelectedFile();
             FileWriter fileWriter = null;
             try {
                 fileWriter = new FileWriter(file);
                 String content = FileOperator.buildContent(textArea.getText());
                 fileWriter.write(content);
             } catch (IOException e) {
                 e.printStackTrace();
             } finally {
                 try {
                     fileWriter.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
     private void showExampleText() {
        String text = "Message1\n\nMessage2\n\nMessage3\nMessage3\n\nMessage4";
         textArea.setText(text);
     }
 
     private void showExampleXml() {
         String xml = "<last_planet>\n    <message>Message1</message>\n    <message>Message2</message>\n</last_planet>";
         textArea.setText(xml);
     }
 
     private void close() {
         System.exit(0);
     }
 }
