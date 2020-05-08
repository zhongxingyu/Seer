 package views;
 
 import controllers.SettingsCtrl;
 import utils.Helper;
 import utils.Logging;
 import utils.UserSession;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 
 /**
  * Created: 23-12-2012
  * @version: 0.1
  * Filename: PrintSettingsUI.java
  * Description:
  * @changes
  */
 
 public class PrintSettingsUI
 {
     private SettingsCtrl _settingsCtrl;
 
     private static JFrame _frame;
     private static PrintSettingsUI _instance;
     private JPanel pnlSettings;
     private JTextField txtPath;
     private JCheckBox chkExportToPdf;
 
     public static JFrame createWindow()
     {
         if(_instance == null)
             _instance = new PrintSettingsUI();
 
         return _frame;
     }
 
     private PrintSettingsUI()
     {
         _settingsCtrl = new SettingsCtrl();
 
         _frame = new JFrame();
         _frame.setTitle("Indstillinger");
         _frame.setIconImage(Toolkit.getDefaultToolkit().getImage(SystemUI.class.getResource("/icons/48x48/app_settings.png")));
         _frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         _frame.setSize(300, 110);
         Helper.centerOnScreen(_frame);
         _frame.toFront();
         _frame.setResizable(false);
         _frame.setVisible(true);
         _frame.addWindowListener(new WindowAdapter()
         {
             public void windowClosing(WindowEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
 
         pnlSettings = new JPanel();
         pnlSettings.setBorder(new EmptyBorder(5, 5, 5, 5));
         pnlSettings.setLayout(null);
         pnlSettings.setBounds(0, 0, 300, 157);
         _frame.setContentPane(pnlSettings);
 
         chkExportToPdf = new JCheckBox("Udskriv til PDF");
         chkExportToPdf.setHorizontalTextPosition(SwingConstants.LEFT);
         chkExportToPdf.setFont(new Font("Dialog", Font.PLAIN, 12));
         chkExportToPdf.setBounds(5, 5, 160, 20);
         chkExportToPdf.setFocusable(false);
         pnlSettings.add(chkExportToPdf);
 
         txtPath = new JTextField();
         txtPath.setBounds(5, 30, 200, 21);
         txtPath.setColumns(10);
         pnlSettings.add(txtPath);
 
         JButton btnBrowse = new JButton("V\u00E6lg");
         btnBrowse.setBounds(207, 28, 87, 23);
         pnlSettings.add(btnBrowse);
 
         btnBrowse.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("V\u00E6lg hvor PDF filerne skal gemmes");
                 fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                 fileChooser.setAcceptAllFileFilterUsed(false);
 
                 int rVal = fileChooser.showOpenDialog(null);
                 if (rVal == JFileChooser.APPROVE_OPTION)
                     txtPath.setText(fileChooser.getSelectedFile().toString());
             }
         });
 
        JButton btnUpdate = new JButton("Gem");
         btnUpdate.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 setData();
                 _instance = null;
                 _frame.dispose();
             }
         });
         btnUpdate.setBounds(5, 60, 125, 23);
         pnlSettings.add(btnUpdate);
 
         JButton btnCancel = new JButton("Annuller");
         btnCancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
         btnCancel.setBounds(169, 60, 125, 23);
         pnlSettings.add(btnCancel);
 
         getData();
     }
 
     private void getData()
     {
         try
         {
             boolean exportToPdf = Boolean.parseBoolean(_settingsCtrl.getProperty("exportToPdf"));
             String exportPath = _settingsCtrl.getProperty("exportPath");
 
             chkExportToPdf.setSelected(exportToPdf);
             txtPath.setText(exportPath);
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(null, Logging.handleException(e, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
         }
     }
 
     private void setData()
     {
         try
         {
             _settingsCtrl.setProperty("exportToPdf", chkExportToPdf.isSelected() + "");
             _settingsCtrl.setProperty("exportPath", txtPath.getText() + File.separator);
             UserSession.setExportToPdf(chkExportToPdf.isSelected());
             UserSession.setOutputPath(txtPath.getText() + File.separator);
             JOptionPane.showMessageDialog(null, "Udskrifts indstillingerne er gemt", "Information!", JOptionPane.INFORMATION_MESSAGE);
         }
         catch (Exception e)
         {
             JOptionPane.showMessageDialog(null, Logging.handleException(e, 0), "Fejl!", JOptionPane.ERROR_MESSAGE);
         }
     }
 }
