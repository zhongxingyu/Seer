 package at.ac.tuwien.sepm.ui;
 
 import at.ac.tuwien.sepm.service.AuthService;
 import at.ac.tuwien.sepm.service.PropertyService;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import com.toedter.calendar.JYearChooser;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 @UI
 public class SettingsPanel extends StandardSimpleInsidePanel {
     private JLabel tissLogin;
     private JLabel facebookLogin;
     private JLabel defaultEcts;
     private JLabel pickMajor;
     private JLabel begin;
 
     private JLabel nameLabelFacebook;
     private JLabel nameLabelTISS;
 
     private JButton tiss;
     private JButton facebook;
     private JTextField ects;
     private JComboBox major;
     private JButton deleteALL;
 
     private JYearChooser year;
     private JComboBox semester;
 
     private JButton save;
 
     private PropertyService propertyService;
 
     @Autowired
     private AuthService authService;
 
     @Autowired
     public SettingsPanel(PropertyService propertyService) {
         this.propertyService=propertyService;
         init();
         addImage();
         addTitle("Einstellungen");
         addReturnButton();
         addContent();
         addSaveButton();
         repaint();
         revalidate();
     }
 
     private void addSaveButton() {
         save = new JButton("Speichern");
         save.setFont(standardButtonFont);
         save.setBounds(deleteALL.getX() + deleteALL.getWidth()+100, deleteALL.getHeight() + deleteALL.getY() + 50, 110, 30);
         save.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 propertyService.setProperty("user.firstYear", Integer.toString(year.getYear()));
                 propertyService.setProperty("user.firstSemester", (String)semester.getSelectedItem());
                 propertyService.setProperty("user.majorName", (String)major.getSelectedItem());
                 setVisible(false);
                 PanelTube.backgroundPanel.showLastComponent();
             }
         });
         this.add(save);
     }
 
     private void addContent() {
         int textHeight = 25;
         int textWidth = 200;
         int verticalSpace = 10;
         int verticalSpaceVast = 40;
 
         begin = new JLabel("Anfang des Studiums:");
         begin.setFont(standardTextFont);
         begin.setBounds((int)simpleWhiteSpace.getX()+70, (int)simpleWhiteSpace.getY()+30, textWidth, textHeight);
         this.add(begin);
 
         semester = new JComboBox();
         semester.setFont(standardButtonFont);
         semester.addItem("SS");
         semester.addItem("WS");
         if (propertyService.getProperty("user.firstSemester") != null) {
             semester.setSelectedItem(propertyService.getProperty("user.firstSemester"));
         } else {
             semester.setSelectedItem("WS");
         }
 
         semester.setBounds(begin.getX() + begin.getWidth() -10, begin.getY(), 45,25);
         this.add(semester);
 
         year = new JYearChooser();
        if (propertyService.getProperty("user.firstYear") != null && !propertyService.getProperty("user.firstYear").isEmpty()) {
             year.setYear(Integer.parseInt(propertyService.getProperty("user.firstYear")));
         } else {
             year.setYear(2013);
         }
         year.setFont(standardTextFont);
         year.setBounds(semester.getX() + semester.getWidth() + 5, semester.getY(), 65,25);
         this.add(year);
 
         tissLogin = new JLabel("TISS Login Daten");
         tissLogin.setFont(standardTextFont);
         tissLogin.setBounds(begin.getX(), begin.getHeight() + begin.getY() + 20, textWidth, textHeight);
         this.add(tissLogin);
 
         tiss = new JButton("eingeben");
         tiss.setFont(standardButtonFont);
         tiss.setBounds(tissLogin.getX(), tissLogin.getY() + tissLogin.getHeight() + verticalSpace, textWidth - 20, textHeight);
 
         nameLabelTISS = new JLabel();
         nameLabelTISS.setFont(standardTextFont);
         nameLabelTISS.setBounds(tiss.getX()+tiss.getWidth()+30, tiss.getY(), 500, textHeight);
         this.add(nameLabelTISS);
 
         tiss.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFrame newframe = new JFrame("Login Daten");
                 newframe.setIconImage(new ImageIcon("src/main/resources/img/icon.jpg").getImage());
                 final LoginDataFrame temp = new LoginDataFrame();
                 newframe.add(temp);
                 newframe.setLocationRelativeTo(null);
                 newframe.pack();
                 newframe.setVisible(true);
                 newframe.setResizable(false);
 
                 newframe.addWindowListener(new WindowAdapter() {
                     @Override
                     public void windowClosed(WindowEvent e) {
                         super.windowClosed(e);
                         try {
                             authService.authenticate(temp.getName(), temp.getPassword());
                             nameLabelTISS.setText("Eingeloggt als: " + temp.getName());
                             propertyService.setProperty("tiss.user", temp.getName());
                             propertyService.setProperty("tiss.password", temp.getPassword());
                         } catch (ServiceException ex) {
                             nameLabelTISS.setText("Login Daten ungültig");
                         }
                         SettingsPanel.this.revalidate();
                         SettingsPanel.this.setVisible(true);
                     }
                 });
             }
         });
         this.add(tiss);
 
 
         facebookLogin = new JLabel("Facebook Login Daten");
         facebookLogin.setFont(standardTextFont);
         facebookLogin.setBounds(tissLogin.getX(), tiss.getHeight() + tiss.getY() + 20, textWidth, textHeight);
         this.add(facebookLogin);
 
         facebook = new JButton("eingeben");
         facebook.setFont(standardButtonFont);
         facebook.setBounds(facebookLogin.getX(), facebookLogin.getY() + facebookLogin.getHeight() + verticalSpace, textWidth-20, textHeight);
 
         nameLabelFacebook= new JLabel();
         nameLabelFacebook.setFont(standardTextFont);
         nameLabelFacebook.setBounds(facebook.getX()+facebook.getWidth()+30, facebook.getY(), 500, textHeight);
         this.add(nameLabelFacebook);
 
         facebook.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFrame newframe = new JFrame("Login Daten");
                 newframe.setIconImage(new ImageIcon("src/main/resources/img/icon.jpg").getImage());
                 final LoginDataFrame temp = new LoginDataFrame();
                 newframe.add(temp);
                 newframe.setLocationRelativeTo(null);
                 newframe.pack();
                 newframe.setVisible(true);
                 newframe.setResizable(false);
 
                 newframe.addWindowListener(new WindowAdapter() {
                     @Override
                     public void windowClosed(WindowEvent e) {
                         super.windowClosed(e);
                         nameLabelFacebook.setText("Eingeloggt als: " + temp.getName());
                         propertyService.setProperty("facebook.user", temp.getName());
                         propertyService.setProperty("facebook.password", temp.getPassword());
                         SettingsPanel.this.revalidate();
                         SettingsPanel.this.setVisible(true);
                     }
                 });
             }
         });
         this.add(facebook);
 
         defaultEcts = new JLabel("Default ECTS angeben");
         defaultEcts.setFont(standardTextFont);
         defaultEcts.setBounds(tissLogin.getX(), facebook.getHeight() + facebook.getY() + verticalSpaceVast, textWidth, textHeight);
         this.add(defaultEcts);
 
         ects = new JTextField("30");
         ects.setFont(standardButtonFont);
         ects.setBounds(defaultEcts.getX() + defaultEcts.getWidth() - 10, defaultEcts.getY(), 25, textHeight);
         this.add(ects);
 
         pickMajor = new JLabel("Studium auswählen");
         pickMajor.setFont(standardTextFont);
         pickMajor.setBounds(tissLogin.getX(), ects.getHeight() + ects.getY() + verticalSpaceVast, textWidth, textHeight);
         this.add(pickMajor);
 
         major = new WideComboBox();                 //todo echte studien einfügen ^.-
         major.addItem("Bachelor - Teststudium 1");
         major.addItem("Master - Teststudium 2");
         major.addItem("Bachelor - Teststudium 3");
         major.setFont(standardButtonFont);
 
         if (propertyService.getProperty("user.majorName") != null) {
             semester.setSelectedItem(propertyService.getProperty("user.majorName"));
         } else {
             semester.setSelectedIndex(0);
         }
         major.setBounds(pickMajor.getX() + pickMajor.getWidth() - 10, pickMajor.getY(), 250, textHeight);
         this.add(major);
 
 
         deleteALL = new JButton("ALLE Daten zurücksetzen");
         deleteALL.setFont(standardTextFont);
         deleteALL.setBounds(pickMajor.getX(), pickMajor.getHeight() + pickMajor.getY() + verticalSpaceVast, textWidth+50, textHeight);
         deleteALL.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 Object[] options = {"Ja", "Abbrechen"};
                  if (JOptionPane.showOptionDialog(SettingsPanel.this, "Wollen Sie wirklich alle gespeicherten Daten des Programms löschen?", "Bestätigung",
                          JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]) == JOptionPane.YES_OPTION) {
                      propertyService.setProperty("user.firstYear", "");
                      propertyService.setProperty("user.firstSemester", "");
                      propertyService.setProperty("user.majorName", "");
                      propertyService.setProperty("facebook.user", "");
                      propertyService.setProperty("facebook.password", "");
                      propertyService.setProperty("tiss.user", "");
                      propertyService.setProperty("tiss.password", "");
                     //todo alle anderen daten löschen?
                  }
             }
         });
         this.add(deleteALL);
     }
 
 }
