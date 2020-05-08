 package ca.couchware.wezzle2d.dialog;
 
 import java.awt.*;
 import java.text.ParseException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import javax.swing.border.*;
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.manager.Settings;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.manager.SettingsManager;
 import ca.couchware.wezzle2d.util.CouchLogger;
 import ca.couchware.wezzle2d.util.PartialMaskFormatter;
 import java.awt.Image;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import javax.swing.JFormattedTextField;
 import javax.swing.UIManager;
 import javax.swing.text.DefaultFormatterFactory;
 import javax.swing.text.MaskFormatter;
 
 /*
  * Created by JFormDesigner on Tue Feb 09 01:03:32 EST 2010
  */
 /**
  * @author Cameron McKay
  */
 public class LicenseDialog extends JDialog
 {
 
     private static final int SERIAL_NUMBER_MIN = 8;
    private static final int LICENSE_KEY_MIN = 32;    
 
     final private static String ICON_16_PATH = Settings.getResourcesPath() + "/" + "Icon_16x16.png";
     final private static String ICON_32_PATH = Settings.getResourcesPath() + "/" + "Icon_32x32.png";
 
     public LicenseDialog(Frame owner)
     {
         super(owner, true);
         initComponents();
 
         try
         {
             URL icon16Url = LicenseDialog.class.getClassLoader().getResource(ICON_16_PATH);
             URL icon32Url = LicenseDialog.class.getClassLoader().getResource(ICON_32_PATH);
             List<Image> icons = new ArrayList<Image>();
             icons.add(ImageIO.read(icon16Url));
             icons.add(ImageIO.read(icon32Url));
             this.setIconImages(icons);
         }
         catch (IOException ex)
         {
             CouchLogger.get().recordException(this.getClass(), ex);
         }
 
         try
         {
             MaskFormatter serialNumberFormatter =
                     new PartialMaskFormatter("HHHHHHHHHHHHHHHH");
             serialNumberFormatter.setValueContainsLiteralCharacters(false);
 
             serialNumberField.setFormatterFactory(new DefaultFormatterFactory(
                     serialNumberFormatter));
 
             MaskFormatter licenseKeyFormatter =
                     new PartialMaskFormatter("HHHHHHHH-HHHHHHHH-HHHHHHHH-HHHHHHHH");
             licenseKeyFormatter.setValueContainsLiteralCharacters(false);
 
             licenseKeyField.setFormatterFactory(new DefaultFormatterFactory(
                     licenseKeyFormatter));
         }
         catch (ParseException ex)
         {
             Logger.getLogger(LicenseDialog.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         okButton.addMouseListener(new java.awt.event.MouseAdapter()
         {
             @Override
             public void mouseClicked(java.awt.event.MouseEvent evt)
             {
                 okButtonMouseClicked(evt);
             }
         });
         
         cancelButton.addMouseListener(new java.awt.event.MouseAdapter()
         {
             @Override
             public void mouseClicked(java.awt.event.MouseEvent evt)
             {
                 cancelButtonMouseClicked(evt);
             }
         });
     }
 
     private void okButtonMouseClicked(java.awt.event.MouseEvent evt)
     {
         boolean problem = false;
         String title = "";
         String text = "";
 
        final String serialNumber = (String) serialNumberField.getValue();
        final String licenseKey = (String) licenseKeyField.getValue();

        if (serialNumber == null || serialNumber.length() < SERIAL_NUMBER_MIN)
         {
             problem = true;
             title = "Serial Number Problem";
             text = "Serial number must be between 8 and 16 characters in length.";
             serialNumberField.requestFocus();
         }
        else if (licenseKey == null || licenseKey.length() < LICENSE_KEY_MIN)
         {
             problem = true;
             title = "License Key Problem";
             text = "The license key must be 4 groups of 8 characters (32 charaters total).";
             licenseKeyField.requestFocus();
         }
 
         if (!problem)
         {
            // Try to validate.            
             if (!Game.validateLicenseInformation(serialNumber, licenseKey))
             {
                 problem = true;
                 title = "Invalid License Key";
                 text = "The license key does not match this serial number.";
             }
             else
             {
                 SettingsManager.get().setString(Key.USER_SERIAL_NUMBER, serialNumber);
                 SettingsManager.get().setString(Key.USER_LICENSE_KEY, licenseKey);
                 setVisible(false);
                 dispose();
             }
         }
 
         if (problem)
         {
             JOptionPane.showMessageDialog(
                     rootPane, text, title, JOptionPane.INFORMATION_MESSAGE);
         }
     }
 
     private void cancelButtonMouseClicked(java.awt.event.MouseEvent evt)
     {
         setVisible(false);
         dispose();
     }
 
     public static void run() throws InterruptedException, InvocationTargetException
     {
         java.awt.EventQueue.invokeAndWait(new Runnable()
         {
 
             public void run()
             {
                 try
                 {
                     UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                 }
                 catch (Exception e)
                 {
                     CouchLogger.get().recordException(LicenseDialog.class, e, true /* Fatal */);
                 }
 
                 LicenseDialog dialog = new LicenseDialog(new javax.swing.JFrame());
 
                 //dialog.setLocationRelativeTo(null);
                 dialog.addWindowListener(new java.awt.event.WindowAdapter()
                 {
                     @Override
                     public void windowClosing(java.awt.event.WindowEvent e)
                     {
 
                     }
                 });
 
                 dialog.setVisible(true);
             }
         });
     }
 
     private void initComponents()
     {
 		// JFormDesigner - Component initialization - DO NOT MODIFY//GEN-BEGIN:initComponents
 		// Generated using JFormDesigner Evaluation license - Cameron McKay
 		dialogPane = new JPanel();
 		contentPanel = new JPanel();
 		instructionsLabel = new JLabel();
 		licensePanel = new JPanel();
 		serialNumberLabel = new JLabel();
 		serialNumberField = new JFormattedTextField();
 		licenseKeyLabel = new JLabel();
 		licenseKeyField = new JFormattedTextField();
 		buttonPanel = new JPanel();
 		okButton = new JButton();
 		cancelButton = new JButton();
 
 		//======== this ========
 		setTitle("Wezzle");
 		Container contentPane = getContentPane();
 		contentPane.setLayout(new BorderLayout());
 
 		//======== dialogPane ========
 		{
 			dialogPane.setBorder(new EmptyBorder(20, 20, 20, 20));		
 			dialogPane.setLayout(new BorderLayout());
 
 			//======== contentPanel ========
 			{
 
 				//---- instructionsLabel ----
 				instructionsLabel.setText("<html>Please enter your serial number and license key.<br/>\n<br/>\nRemember to enter your serial number and license key exactly as it appears in the e-mail you received from Couchware Inc.\n</html>");
 				instructionsLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 				//======== licensePanel ========
 				{
 					licensePanel.setBorder(new TitledBorder(null, "", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION));
 
 					//---- serialNumberLabel ----
 					serialNumberLabel.setText("Serial Number");
 
 					//---- serialNumberField ----
 					serialNumberField.setFont(new Font("Courier New", Font.PLAIN, 12));
 
 					//---- licenseKeyLabel ----
 					licenseKeyLabel.setText("License Key");
 
 					//---- licenseKeyField ----
 					licenseKeyField.setFont(new Font("Courier New", Font.PLAIN, 12));
 
 					GroupLayout licensePanelLayout = new GroupLayout(licensePanel);
 					licensePanel.setLayout(licensePanelLayout);
 					licensePanelLayout.setHorizontalGroup(
 						licensePanelLayout.createParallelGroup()
 							.addGroup(GroupLayout.Alignment.TRAILING, licensePanelLayout.createSequentialGroup()
 								.addContainerGap()
 								.addGroup(licensePanelLayout.createParallelGroup()
 									.addComponent(licenseKeyLabel, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
 									.addComponent(serialNumberLabel, GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
 								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(licensePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
 									.addComponent(licenseKeyField, GroupLayout.Alignment.TRAILING)
 									.addComponent(serialNumberField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE))
 								.addContainerGap())
 					);
 					licensePanelLayout.setVerticalGroup(
 						licensePanelLayout.createParallelGroup()
 							.addGroup(licensePanelLayout.createSequentialGroup()
 								.addContainerGap()
 								.addGroup(licensePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 									.addComponent(serialNumberField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(serialNumberLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 								.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 								.addGroup(licensePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 									.addComponent(licenseKeyField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 									.addComponent(licenseKeyLabel, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE))
 								.addContainerGap())
 					);
 				}
 
 				//======== buttonPanel ========
 				{
 
 					//---- okButton ----
 					okButton.setText("OK");
 
 					//---- cancelButton ----
 					cancelButton.setText("Cancel");
 
 					GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
 					buttonPanel.setLayout(buttonPanelLayout);
 					buttonPanelLayout.setHorizontalGroup(
 						buttonPanelLayout.createParallelGroup()
 							.addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
 								.addContainerGap(223, Short.MAX_VALUE)
 								.addComponent(okButton, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
 								.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
 								.addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
 								.addGap(9, 9, 9))
 					);
 					buttonPanelLayout.setVerticalGroup(
 						buttonPanelLayout.createParallelGroup()
 							.addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
 								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 								.addGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 									.addComponent(cancelButton)
 									.addComponent(okButton))
 								.addContainerGap())
 					);
 				}
 
 				GroupLayout contentPanelLayout = new GroupLayout(contentPanel);
 				contentPanel.setLayout(contentPanelLayout);
 				contentPanelLayout.setHorizontalGroup(
 					contentPanelLayout.createParallelGroup()
 						.addComponent(licensePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(instructionsLabel, GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
 				);
 				contentPanelLayout.setVerticalGroup(
 					contentPanelLayout.createParallelGroup()
 						.addGroup(GroupLayout.Alignment.TRAILING, contentPanelLayout.createSequentialGroup()
 							.addComponent(instructionsLabel, GroupLayout.DEFAULT_SIZE, 90, Short.MAX_VALUE)
 							.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
 							.addComponent(licensePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
 							.addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 				);
 			}
 			dialogPane.add(contentPanel, BorderLayout.CENTER);
 		}
 		contentPane.add(dialogPane, BorderLayout.CENTER);
 		pack();
 		setLocationRelativeTo(null);
 		// JFormDesigner - End of component initialization//GEN-END:initComponents
     }
 	// JFormDesigner - Variables declaration - DO NOT MODIFY//GEN-BEGIN:variables
 	// Generated using JFormDesigner Evaluation license - Cameron McKay
 	private JPanel dialogPane;
 	private JPanel contentPanel;
 	private JLabel instructionsLabel;
 	private JPanel licensePanel;
 	private JLabel serialNumberLabel;
 	private JFormattedTextField serialNumberField;
 	private JLabel licenseKeyLabel;
 	private JFormattedTextField licenseKeyField;
 	private JPanel buttonPanel;
 	private JButton okButton;
 	private JButton cancelButton;
 	// JFormDesigner - End of variables declaration//GEN-END:variables
 }
