 package db.migration.app.ui;
 
 import db.migration.app.console.ConsoleMigration;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.io.StringWriter;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 /**
  * 数据库升级的UI
  * 
  * @author <a href="mailto:jun.tsai@gmail.com">Jun Tsai</a>
  * @version $Revision$
  * @since 0.1
  */
 public class DatabaseMigrationUI extends JPanel implements ActionListener {
 
     /**
      * 
      */
     private static final long serialVersionUID = 2406525846412138282L;
 
     JButton openButton, saveButton;
 
     JTextArea log;
 
     JFileChooser fc;
 
     private JTextField configFileField;
 
     private JButton configFileSelectBtn;
 
     private JButton upgradeBtn;
 
     private JTextField dbScritpField;
 
     private JButton dbScriptSelectBtn;
 
     private JTextField entityJarField;
 
     private JButton entityJarSelectBtn;
 
     private String lastDirectory = ".";
 
     public DatabaseMigrationUI() {
         super(new GridBagLayout());
 
         // 配置文件行
         JLabel label = new JLabel();
         label.setText("配置文件：");
         label.setHorizontalAlignment(JLabel.RIGHT);
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 0;
         add(label, c);
 
         configFileField = new JTextField();
         configFileField.setFocusable(false);
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 0;
         c.ipadx = 300;
         add(configFileField, c);
 
         configFileSelectBtn = new JButton();
         configFileSelectBtn.setText("选择");
         configFileSelectBtn.addActionListener(this);
         c = new GridBagConstraints();
         c.gridx = 2;
         c.gridy = 0;
         add(configFileSelectBtn, c);
 
         // 数据库升级脚本
         label = new JLabel();
         label.setText("脚本目录：");
         label.setHorizontalAlignment(JLabel.RIGHT);
         c = new GridBagConstraints();
         c.gridx = 0;
         c.gridy = 1;
         add(label, c);
 
         dbScritpField = new JTextField();
         dbScritpField.setFocusable(false);
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 1;
         c.ipadx = 300;
         add(dbScritpField, c);
 
         dbScriptSelectBtn = new JButton();
         dbScriptSelectBtn.setText("选择");
         dbScriptSelectBtn.addActionListener(this);
         c = new GridBagConstraints();
         c.gridx = 2;
         c.gridy = 1;
         add(dbScriptSelectBtn, c);
 
         // 依赖的jar
         /*
         label = new JLabel();
         label.setText("在目录：");
         c = new GridBagConstraints();
         c.gridx = 0;
         c.gridy = 2;
         add(label, c);
 
         entityJarField = new JTextField();
         entityJarField.setFocusable(false);
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 2;
         c.ipadx = 300;
         add(entityJarField, c);
 
         entityJarSelectBtn = new JButton();
         entityJarSelectBtn.setText("选择");
         entityJarSelectBtn.addActionListener(this);
         c = new GridBagConstraints();
         c.gridx = 2;
         c.gridy = 2;
         add(entityJarSelectBtn, c);
         */
 
         upgradeBtn = new JButton();
         upgradeBtn.setText("升级数据库");
         upgradeBtn.addActionListener(this);
         c = new GridBagConstraints();
         c.gridx = 1;
         c.gridy = 4;
         add(upgradeBtn, c);
 
         log = new JTextArea();
         c = new GridBagConstraints();
         c.fill = GridBagConstraints.HORIZONTAL;
         c.gridx = 0;
         c.gridy = 5;
         c.gridwidth = 3;
         c.weightx = 0;
         c.ipady = 400;
 
         log.setMargin(new Insets(5, 5, 5, 5));
         log.setEditable(false);
         JScrollPane logScrollPane = new JScrollPane(log);
         add(logScrollPane, c);
     }
 
     public void actionPerformed(ActionEvent e) {
         if (configFileSelectBtn == e.getSource()) {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
             fileChooser.setCurrentDirectory(new File(this.lastDirectory));
             int result = fileChooser.showOpenDialog(this);
             if (result == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 if (file.exists() && file.isFile()) {
                     lastDirectory = file.getParent();
                     this.configFileField.setText(file.getAbsolutePath());
                 } else {
                     JOptionPane.showMessageDialog(this, "所选择文件不存在或者不是文件.",
                             "数据库升级脚本", JOptionPane.ERROR_MESSAGE);
                 }
             }
         } else if (dbScriptSelectBtn == e.getSource()) {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             fileChooser.setCurrentDirectory(new File(this.lastDirectory));
             int result = fileChooser.showOpenDialog(this);
             if (result == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 if (file.exists() && file.isDirectory()) {
                     lastDirectory = file.getAbsolutePath();
                     this.dbScritpField.setText(file.getAbsolutePath());
                 } else {
                     JOptionPane.showMessageDialog(this, "所选择文件不存在或者不是文件目录.",
                             "数据库升级脚本", JOptionPane.ERROR_MESSAGE);
                 }
             }
         /*
         } else if (entityJarSelectBtn == e.getSource()) {
             JFileChooser fileChooser = new JFileChooser();
             fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             fileChooser.setCurrentDirectory(new File(this.lastDirectory));
             int result = fileChooser.showOpenDialog(this);
             if (result == JFileChooser.APPROVE_OPTION) {
                 File file = fileChooser.getSelectedFile();
                 if (file.exists() && file.isDirectory()) {
                     lastDirectory = file.getAbsolutePath();
                     this.entityJarField.setText(file.getAbsolutePath());
                 } else {
                     JOptionPane.showMessageDialog(this, "所选择文件不存在或者不是文件目录.",
                             "数据库升级脚本", JOptionPane.ERROR_MESSAGE);
                 }
             }
             */
         } else if (upgradeBtn == e.getSource()) {
             // disable button
             final String configPath = configFileField.getText();
             final String dbScriptDir = dbScritpField.getText();
             //final String entityJarDir = entityJarField.getText();
 
             if (!hasText(configPath)
                     || !hasText(dbScriptDir)
                     ) {
                 JOptionPane.showMessageDialog(this,
                         "未选择配置文件或者脚本目录.", "数据库升级脚本",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
             this.upgradeBtn.setEnabled(false);
             TeeOutputStream pos;
             try {
                 pos = new TeeOutputStream(System.out, log);
                 PrintStream ps = new PrintStream(pos);
                 System.setOut(ps);
                 log.setText("");
 
                 Thread thread = new Thread(new Runnable() {
                     public void run() {
                         try {
                             String version = ConsoleMigration
                                     .upgradeByArgs(new String[]{"-c",
                                             configPath, "-d", dbScriptDir});
                             
                             DatabaseMigrationUI.this.showResult(version);
                             System.setOut(System.out);
                         } catch (Throwable e) {
                             DatabaseMigrationUI.this.showErrorMessage(e);
                         }
                     }
                 });
                 thread.start();
               
             } catch (Exception e1) {
                 DatabaseMigrationUI.this.showErrorMessage(e1);
             }
         }
     }
 
     private boolean hasText(String configPath) {
         return configPath!=null&&configPath.trim().length()>0;
     }
 
     private void showErrorMessage(final Throwable e) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 StringBuffer sb = new StringBuffer();
                 sb.append(e.toString()).append("\n");
                 StackTraceElement[] sts = e.getStackTrace();
                 for(int i=0;i<sts.length;i++){
                     sb.append(sts[i].toString()).append("\n");
                 }
                 JOptionPane.showMessageDialog(DatabaseMigrationUI.this,
                         sb.toString(), "数据库升级脚本", JOptionPane.ERROR_MESSAGE);
                 upgradeBtn.setEnabled(true);
             }
         });
     }
     private void showResult(final String version) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
 		        if("-2".equalsIgnoreCase(version)) {
 		            JOptionPane.showMessageDialog(DatabaseMigrationUI.this,
 		                    "未能升级，因为文件版本小于或等于数据库版本", "数据库升级脚本", JOptionPane.ERROR_MESSAGE);
 		        } else if("-3".equalsIgnoreCase(version)){
                     JOptionPane.showMessageDialog(DatabaseMigrationUI.this,
                             "升级失败，请查看下方日志", "数据库升级脚本", JOptionPane.ERROR_MESSAGE);
                 }else{
                     JOptionPane.showMessageDialog(DatabaseMigrationUI.this,
                             "成功升级数据库至版本["+version+"]", "数据库升级脚本",
                             JOptionPane.INFORMATION_MESSAGE);
 		        }
 		        upgradeBtn.setEnabled(true);
             }
         });
     }
 
     /**
      * Create the GUI and show it. For thread safety, this method should be
      * invoked from the event dispatch thread.
      */
     private static void createAndShowGUI() {
         // Create and set up the window.
        JFrame frame = new JFrame("数据库升级工具("+ConsoleMigration.getVersion()+")  -- Powerd By You!");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //
         // Read the image that will be used as the application icon. Using "/"
         // in front of the image file name will locate the image at the root
         // folder of our application. If you don't use a "/" then the image file
         // should be on the same folder with your class file.
         //
         BufferedImage image = null;
         try {
             image = ImageIO.read(frame.getClass().getResource(
                     "/Database_Find.png"));
             frame.setIconImage(image);
         } catch (IOException e) {
             e.printStackTrace();
         }
         //不能改变大小
         frame.setResizable(false);
         // Add content to the window.
         frame.add(new DatabaseMigrationUI());
         centerWindow(frame);
         // Display the window.
         frame.pack();
         frame.setVisible(true);
     }
 
     private static void centerWindow(Component component) {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension componentSize = component.getSize();
         if (componentSize.height > screenSize.height) {
             componentSize.height = screenSize.height;
         }
         if (componentSize.width > screenSize.width) {
             componentSize.width = screenSize.width;
         }
         component.setLocation((screenSize.width - componentSize.width) / 2,
                 (screenSize.height - componentSize.height) / 2);
     }
 
     public static void main(String[] args) {
         JFrame.setDefaultLookAndFeelDecorated(true);
         JDialog.setDefaultLookAndFeelDecorated(true);
 
         // Schedule a job for the event dispatch thread:
         // creating and showing this application's GUI.
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 try {
                     /*
                     UIManager
                             .setLookAndFeel(new SubstanceOfficeBlue2007LookAndFeel());
                             */
                     UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                 } catch (Exception e) {
                     System.out
                             .println("Substance Graphite failed to initialize");
                 }
                 UIManager.put("swing.boldMetal", Boolean.FALSE);
                 createAndShowGUI();
             }
         });
     }
 
 }
