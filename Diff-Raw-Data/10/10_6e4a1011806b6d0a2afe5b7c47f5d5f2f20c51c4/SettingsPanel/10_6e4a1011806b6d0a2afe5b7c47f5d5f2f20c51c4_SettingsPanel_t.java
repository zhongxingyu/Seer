 package at.ac.tuwien.sepm.ui;
 
 import at.ac.tuwien.sepm.entity.Curriculum;
 import at.ac.tuwien.sepm.service.*;
 import at.ac.tuwien.sepm.ui.startUp.SimpleDisplayPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.SelectItem;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import com.toedter.calendar.JYearChooser;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 /**
  * Author: Georg Plaz
  */
 @UI
 public class SettingsPanel extends StandardSimpleInsidePanel {
 
 
     private JButton showWizard;
     private JButton tiss;
     private JButton facebook;
     // JTextField ects;
     private JComboBox major;
     private JButton deleteALL;
     private JButton deleteAllDatesBtn;
     private JYearChooser year;
     private WideComboBox semester;
     private JButton save;
 
     private static final String DELETE_ALL_DATES_MESSAGE = "Es werden alle privaten Termine gelöscht.\nLVA-Termine bleiben gespeichert." +
             "\nMöchten Sie fortfahren?";
     private static final String DELETE_ALL_DATES_TITLE = "Termine wirklich löschen?";
     private static final String[] DELETE_ALL_DATES_BUTTON_TEXT = {"Ja", "Nein"};
 
     private PropertyService propertyService;
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
     private CreateCurriculumService createCurriculumService;
     private DateService dateService;
 
     private MySimplePanel content;
     @Autowired
     private AuthService authService;
     @Autowired
     private FacebookService facebookService;
 
     @Autowired
     private DeleteWholeDBService deleteWholeDBService;
 
 
     @Autowired
     public SettingsPanel(PropertyService propertyService, CreateCurriculumService createCurriculumService, DateService dateService) {
         this.createCurriculumService=createCurriculumService;
         this.propertyService=propertyService;
         this.dateService=dateService;
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
                 propertyService.setProperty(PropertyService.FIRST_YEAR, Integer.toString(year.getYear()));
                 propertyService.setProperty(PropertyService.FIRST_SEMESTER, semester.getSelectedItem().toString());
                 if (!major.getSelectedItem().toString().equals("Bitte Importieren sie ein Studium!")) {
                     propertyService.setProperty(PropertyService.MAJOR, major.getSelectedItem().toString());
                 } else {
                     PanelTube.backgroundPanel.viewSmallInfoText("Der Studiumsname konnte nicht gespeichert werden.", SmallInfoPanel.Info);
                 }
                 //propertyService.setProperty(PropertyService."user.defaultECTS", ects.getText());
                 setVisible(false);
                 PanelTube.calendarPanel.refreshTop();
                 PanelTube.backgroundPanel.viewSmallInfoText("Die Daten wurden gespeichert.", SmallInfoPanel.Success);
                 PanelTube.backgroundPanel.showLastComponent();
             }
         });
         //this.add(save);
     }
 
     private void addContent() {
         //int textHeight = 25;
 
         showWizard = new JButton("anzeigen");
         showWizard.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 PanelTube.backgroundPanel.viewStartup(false);
             }
         });
 
 
         semester = new WideComboBox(new Semester[]{Semester.W,Semester.S});
         semester.setSelectedItem(Semester.parse(propertyService.getProperty(PropertyService.FIRST_SEMESTER,Semester.W.toString())));
 
 
         year = new JYearChooser();
         year.setYear(Integer.parseInt(propertyService.getProperty(PropertyService.FIRST_YEAR,""+DateTime.now().getYear())));
         year.setStartYear(1900);
         year.setEndYear(DateTime.now().getYear());
 
         tiss = new JButton("eingeben");
 
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
                             propertyService.setProperty(PropertyService.TISS_USER, temp.getName());
                             propertyService.setProperty(PropertyService.TISS_PASSWORD, temp.getPassword());
                         } catch (ServiceException ex) {
                             PanelTube.backgroundPanel.viewSmallInfoText("Login Daten ungültig",SmallInfoPanel.Error);
                         }
                         SettingsPanel.this.revalidate();
                         SettingsPanel.this.setVisible(true);
                         content.refresh();
                     }
                 });
                 content.refresh();
             }
         });
         //this.add(tiss);
 
         facebook = new JButton("eingeben");
         facebook.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 JFrame newframe = new JFrame("Login Daten");
                 newframe.setIconImage(new ImageIcon("src/main/resources/img/icon.jpg").getImage());
                 final LoginDataFacebookFrame temp = new LoginDataFacebookFrame();
                 newframe.add(temp);
                 newframe.setLocationRelativeTo(null);
                 newframe.pack();
                 newframe.setVisible(true);
                 newframe.setResizable(false);
 
                 newframe.addWindowListener(new WindowAdapter() {
                     @Override
                     public void windowClosed(WindowEvent e) {
                         super.windowClosed(e);
                         //nameLabelFacebook.setText("Eingeloggt als: " + temp.getName());
                         propertyService.setProperty(PropertyService.FACEBOOK_KEY, temp.getKey());
                         facebookService.authenticate();
                         SettingsPanel.this.revalidate();
                         SettingsPanel.this.setVisible(true);
                         content.refresh();
                     }
                 });
                 content.refresh();
             }
         });
 
         
         major = new WideComboBox();
         try {
             if (!createCurriculumService.readAllCurriculum().isEmpty())
                 for (Curriculum c : createCurriculumService.readAllCurriculum())
                     major.addItem(new CurriculumSelectItem(c));
             else
                 major.addItem("Bitte Importieren sie ein Studium!");
         } catch (ServiceException e) {
             log.error("Error: " + e.getMessage());
             PanelTube.backgroundPanel.viewSmallInfoText("Fehler beim Laden der Studien",SmallInfoPanel.Error);
         }
 
         if (propertyService.getProperty(PropertyService.MAJOR) != null && !propertyService.getProperty(PropertyService.MAJOR).isEmpty()) {
             semester.setSelectedItem(propertyService.getProperty(PropertyService.MAJOR));
         } else {
             semester.setSelectedIndex(0);
         }
 
 
         deleteALL = new JButton("ALLE Daten zurücksetzen");
 
         deleteALL.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 Object[] options = {"Ja", "Abbrechen"};
                 if (JOptionPane.showOptionDialog(SettingsPanel.this, "Wollen Sie wirklich alle gespeicherten Daten des Programms löschen?", "Bestätigung",
                         JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]) == JOptionPane.YES_OPTION) {
                     propertyService.removeProperty(PropertyService.FIRST_YEAR);
                     propertyService.removeProperty(PropertyService.FIRST_SEMESTER);
                     propertyService.removeProperty(PropertyService.MAJOR);
                     propertyService.removeProperty(PropertyService.FACEBOOK_KEY);
                     propertyService.removeProperty(PropertyService.TISS_USER);
                     propertyService.removeProperty(PropertyService.TISS_PASSWORD);
                     propertyService.removeProperty(PropertyService.FIRST_RUN);
 
                     try {
                         deleteWholeDBService.deleteAll();
                     } catch (ServiceException e) {
                         PanelTube.backgroundPanel.viewSmallInfoText("Es ist ein Fehler beim loeschen der Datenbank aufgetreten!", SmallInfoPanel.Error);
                     }
 
                     PanelTube.backgroundPanel.viewSmallInfoText("Daten wurden erfolgreich gelöscht!", SmallInfoPanel.Success);
                    PanelTube.studienplanPanel.refreshCurriculumComboBox();
                     PanelTube.backgroundPanel.viewStartup(true);
                 }
             }
         });
         this.add(deleteALL);
 
         deleteAllDatesBtn = new JButton("Private Termine löschen");
         deleteAllDatesBtn.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
                     int i = JOptionPane.showOptionDialog(new JFrame(),
                             DELETE_ALL_DATES_MESSAGE,
                             DELETE_ALL_DATES_TITLE,
                             JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE,
                             null,
                             DELETE_ALL_DATES_BUTTON_TEXT,
                             DELETE_ALL_DATES_BUTTON_TEXT[1]);
 
                     if (i == 0) {
                         dateService.deleteAllDates();
                         PanelTube.backgroundPanel.viewSmallInfoText("Termine erfolgreich gelöscht", SmallInfoPanel.Success);
                         PanelTube.calendarPanel.refresh();
                     }
                 } catch (ServiceException e1) {
                     PanelTube.backgroundPanel.viewSmallInfoText(e1.getMessage(), SmallInfoPanel.Error);
                 }
             }
         });
         content = new MySimplePanel(simpleWhiteSpace.getWidth(),simpleWhiteSpace.getHeight(),this);
         this.add(content);
         content.setBounds(simpleWhiteSpace);
     }
     @Override
     public void refresh(){
         semester.setSelectedItem(Semester.parse(propertyService.getProperty(PropertyService.FIRST_SEMESTER,Semester.W.toString())));
         year.setYear(Integer.parseInt(propertyService.getProperty(PropertyService.FIRST_YEAR, ""+DateTime.now().getYear())));
         content.refresh();
 
     }
 
     private static class CurriculumSelectItem extends SelectItem<Curriculum> {
         CurriculumSelectItem(Curriculum item) {
             super(item);
         }
 
         @Override
         public String toString() {
             return item.getName();
         }
     }
     private class MySimplePanel extends SimpleDisplayPanel{
         int buttonWidthInit =buttonWidth;
         public MySimplePanel(double width, double height, StandardInsidePanel standardInsidePanel) {
             super(width, height, standardInsidePanel);
             //inputWidthLeft-= 40;
             //labelWidthLeft-=30;
             //labelXLeft+=bigSpace;
             //inputXLeft=labelXLeft+labelWidthLeft+smallSpace;
 
 
             refresh();
 
         }
         public void refresh(){
             buttonWidth= inputWidthLeft;
             lastLeftBottomHeight=getHeight()-bigSpace;
             lastRightBottomHeight=lastLeftBottomHeight;
             lastLeftTopHeight=bigSpace;
             lastRightTopHeight=bigSpace;
             removeAll();
             init();
         }
 
         @Override
         public void reset() {
             refresh();
         }
 
         private void init(){
             addEmptyArea(bigSpace*2,true);
             addText("Studienbeginn",true);
             addRow(new JTextArea("Jahr"),year,true);
             addRow(new JTextArea("Semester"),semester,true);
             addEmptyArea(bigSpace*3,true);
             buttonWidth=buttonWidthInit;
             addRow(new JTextArea("Startup-Wizard"),showWizard,true);
 
             addEmptyArea(bigSpace*2,false);
             addRow(new JTextArea("TISS-login"),tiss,false);
             if (propertyService.getProperty(PropertyService.TISS_USER) != null && !propertyService.getProperty(PropertyService.TISS_USER).isEmpty() &&
                     propertyService.getProperty(PropertyService.TISS_PASSWORD) != null && !propertyService.getProperty(PropertyService.TISS_PASSWORD).isEmpty()) {
                 addText("Eingeloggt als: " + propertyService.getProperty(PropertyService.TISS_USER),Font.BOLD,false);
                 //nameLabelTISS.setText("Eingeloggt als: " + propertyService.getProperty(PropertyService.TISS_USER));
             }
             addEmptyArea(bigSpace*2,false);
             addRow(new JTextArea("Facebook-login"),facebook,false);
             if (propertyService.getProperty(PropertyService.FACEBOOK_KEY) != null && !propertyService.getProperty(PropertyService.FACEBOOK_KEY).isEmpty()) {
                 addText("Eingeloggt",Font.BOLD,false);
                 //nameLabelTISS.setText("Eingeloggt als: " + propertyService.getProperty(PropertyService.TISS_USER));
             }
 
             buttonWidth=labelWidthRight+20;
             addRow(deleteALL,deleteAllDatesBtn,true,false);
             addRow(null,save,false,false);
 
         }
     }
 }
