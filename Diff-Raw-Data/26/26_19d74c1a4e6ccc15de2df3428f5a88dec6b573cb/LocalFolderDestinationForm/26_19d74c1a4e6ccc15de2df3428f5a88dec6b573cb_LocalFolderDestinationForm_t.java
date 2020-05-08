 package info.mikaelsvensson.ftpbackup.gui;
 
 import info.mikaelsvensson.ftpbackup.model.impl.LocalFolderDestinationImpl;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class LocalFolderDestinationForm extends FormWrapper<LocalFolderDestinationImpl> {
     public LocalFolderDestinationForm() {
         browseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(final ActionEvent e) {
                 GuiComponentFactory.chooseDirectory(folderPath, getRootPanel());
             }
         });
     }
 
     public static void main(final String[] args) {
         GuiComponentFactory.show(new LocalFolderDestinationForm());
     }
 
     private JPanel rootPanel;
     private JButton browseButton;
     private JTextField folderPath;
 
     private void createUIComponents() {
         // TODO: place custom component creation code here
     }
 
     @Override
     public JPanel getRootPanel() {
         return rootPanel;
     }
 
     @Override
     public void loadBean(final LocalFolderDestinationImpl bean) {
         folderPath.setText(bean.getPathToBaseFromRoot());
     }
 
     @Override
     public void fillBean(final LocalFolderDestinationImpl bean) {
        bean.setPathToBaseFromRoot(folderPath.getText());
     }
 
     @Override
     protected LocalFolderDestinationImpl createModel() {
         return new LocalFolderDestinationImpl();
     }
 
     public boolean isModified(LocalFolderDestinationImpl data) {
         if (folderPath.getText() != null ? !folderPath.getText().equals(data.getPathToBaseFromRoot()) : data.getPathToBaseFromRoot() != null) {
             return true;
         }
         return false;
     }
 }
