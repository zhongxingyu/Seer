 package org.zaproxy.zap.extension.saml;
 
 import org.parosproxy.paros.view.View;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 public class AutoChangerSettingUI extends JFrame implements DesiredAttributeChangeListener {
 
     private JScrollPane attributeScrollPane;
 
     private Properties configuration;
 
     private Map<String,String> valueMap;
 
 	/**
 	 * Create the frame.
 	 */
 	public AutoChangerSettingUI(final SAMLProxyListener listener) {
 		setTitle("SAML Automatic Request Changer Settings");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setSize(800, 700);
 		setLocationRelativeTo(null);
         JPanel contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JLabel lblHeaderlabel = new JLabel("<html><h2>Add/Edit autochange attributes/values</h2><p>Following attributes will be changed to the given values automatically. Add/Edit the attributes and values below</p></html>");
 		contentPane.add(lblHeaderlabel, BorderLayout.NORTH);
 		
 		attributeScrollPane = new JScrollPane();
         contentPane.add(attributeScrollPane, BorderLayout.CENTER);
 		
 
 
 		JPanel footerPanel = new JPanel();
 		contentPane.add(footerPanel, BorderLayout.SOUTH);
 		footerPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 		
 		JButton btnAdd = new JButton("Add more attributes");
         btnAdd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 AddNewAttribute dialog = new AddNewAttribute(AutoChangerSettingUI.this);
                 dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                 dialog.setVisible(true);
             }
         });
 		footerPanel.add(btnAdd);
 		
 		JButton btnSaveChanges = new JButton("Save Changes");
         btnSaveChanges.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     configuration.clear();
                     for (Map.Entry<String, String> conf : valueMap.entrySet()) {
                         configuration.put(conf.getKey(),conf.getValue());
                     }
                     SAMLUtils.saveConfigurations(configuration);
                     listener.loadAutoChangeAttributes();
                 } catch (SAMLException e1) {
                     View.getSingleton().showWarningDialog("Save Failed");
                 }
             }
         });
 		footerPanel.add(btnSaveChanges);
 		
 		JButton btnResetChanges = new JButton("Reset changes");
         btnResetChanges.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 initConfigurations();
                 initAttributes();
             }
         });
 		footerPanel.add(btnResetChanges);
 		
 		JButton btnExit = new JButton("Exit");
         btnExit.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 AutoChangerSettingUI.this.setVisible(false);
             }
         });
 		footerPanel.add(btnExit);
         initConfigurations();
         initAttributes();
 	}
 
     private void initConfigurations() {
         configuration = SAMLUtils.loadConfigurations();
         valueMap = new LinkedHashMap<>();
 
         for (Map.Entry<Object, Object> attribute : configuration.entrySet()) {
             valueMap.put(attribute.getKey().toString(),attribute.getValue().toString());
         }
     }
 
     private void initAttributes(){
         JPanel attributePanel = new JPanel();
         attributeScrollPane.setViewportView(attributePanel);
         attributePanel.setLayout(new GridLayout(15, 1, 0, 0));
         for (final Map.Entry<String, String> entry : valueMap.entrySet()) {
             JPanel panel = new JPanel();
             attributePanel.add(panel);
             panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
 
             final JLabel lblAttribute = new JLabel(SAMLUtils.getAttributeViewValue(entry.getKey()));
             Dimension size = lblAttribute.getPreferredSize();
             size.width = 200;
             lblAttribute.setMinimumSize(size);
             lblAttribute.setPreferredSize(size);
             panel.add(lblAttribute);
 
             JTextField txtValue = new JTextField();
             lblAttribute.setLabelFor(txtValue);
             txtValue.setText(entry.getValue());
             panel.add(txtValue);
             txtValue.setColumns(20);
 
             JButton btnAddeditValues = new JButton("Add/Edit Values");
             btnAddeditValues.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                     AddNewAttribute editDialog = new AddNewAttribute(AutoChangerSettingUI.this);
                     editDialog.getComboBoxAttribSelect().removeAllItems();
                     editDialog.getComboBoxAttribSelect().addItem(lblAttribute.getText());
                     editDialog.getTextAreaAttribValues().setText(entry.getValue().replaceAll(",","\n"));
                     editDialog.setVisible(true);
                 }
             });
             panel.add(btnAddeditValues);
 
             JButton btnRemoveAttribute = new JButton("Remove Attribute");
             btnRemoveAttribute.addActionListener(new ActionListener() {
                 @Override
                 public void actionPerformed(ActionEvent e) {
                    int response = JOptionPane.showConfirmDialog(AutoChangerSettingUI.this,
                            "Are you sure to remove the attribute","Confirm",JOptionPane.YES_NO_OPTION);
                     if(response == JOptionPane.YES_OPTION){
                         onDeleteDesiredAttribute(entry.getKey());
                     }
                 }
             });
             panel.add(btnRemoveAttribute);
         }
     }
 
     @Override
     public void onDesiredAttributeValueChange(String attribute, String value) {
         onAddDesiredAttribute(attribute,value);
         initAttributes();
     }
 
     @Override
     public void onAddDesiredAttribute(String attribute, String values) {
         valueMap.put(attribute,values);
         initAttributes();
     }
 
     @Override
     public void onDeleteDesiredAttribute(String attribute) {
         if(valueMap.containsKey(attribute)){
             valueMap.remove(attribute);
             initAttributes();
         }
     }
 
     @Override
     public Set<String> getDesiredAttributes() {
         return valueMap.keySet();
     }
 }
