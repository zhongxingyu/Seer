 package at.ac.tuwien.sepm.ui.entityViews;
 
 import at.ac.tuwien.sepm.entity.Curriculum;
 import at.ac.tuwien.sepm.service.CreateCurriculumService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardSimpleInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 02.07.13
  * Time: 21:46
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class ViewCurriculum extends StandardSimpleInsidePanel {
     private JLabel lnumber;
     private JLabel lname;
     private JLabel ldescription;
     private JLabel ltitle;
     private JLabel lectsc;
     private JLabel lectsf;
     private JLabel lectss;
     private JTextField tnumber;
     private JTextField tname;
     private JTextField tdescription;
     private JTextField ttitle;
     private JTextField tectsc;
     private JTextField tectsf;
     private JTextField tectss;
     private JButton bok;
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     private CreateCurriculumService curriculumService;
 
     @Autowired
     public ViewCurriculum(CreateCurriculumService curriculumService) {
         this.curriculumService=curriculumService;
         init();
         addImage();
         addTitle("Neuer Studienplan");
         addReturnButton();
         addContent();
         this.revalidate();
         this.repaint();
     }
 
     public void setUpForNewCurriculum() {
        changeTitle("Neuer Studienplan");
         tnumber.setText("");
        tname.setText("");
         tdescription.setText("");
         ttitle.setText("");
         tectsc.setText("");
         tectsf.setText("");
         tectss.setText("");
     }
 
     private void addContent() {
         int space = 20;
         int bigSpace=20;
 
         int labelX = (int) (simpleWhiteSpace.getX()+bigSpace);
         int labelWidth = 180;
 
         int inputX = labelX+labelWidth+10;
         int inputWidth = 140;
 
         int oHeight = 25;
 
 
         lnumber = new JLabel("Studienkennzahl: ");
         lname = new JLabel("Name: ");
         ldescription = new JLabel("Beschreibung: ");
         ltitle = new JLabel("Akad. Titel: ");
         lectsc = new JLabel("Wahl-ECTS: ");
         lectsf = new JLabel("Frei-ECTS: ");
         lectss = new JLabel("SoftSkill-ECTS: ");
 
         lnumber.setFont(standardTextFont);
         lname.setFont(standardTextFont);
         ldescription.setFont(standardTextFont);
         ltitle.setFont(standardTextFont);
         lectsc.setFont(standardTextFont);
         lectsf.setFont(standardTextFont);
         lectss.setFont(standardTextFont);
 
         tnumber = new JTextField();
         tname = new JTextField();
         tname.getDocument().addDocumentListener(new DocumentListener() {
             @Override
             public void insertUpdate(DocumentEvent e) {
                 changeTitle(tname.getText());
             }
 
             @Override
             public void removeUpdate(DocumentEvent e) {
                 changeTitle(tname.getText());
             }
 
             @Override
             public void changedUpdate(DocumentEvent e) {
                 changeTitle(tname.getText());
             }
         });
         tdescription = new JTextField();
         ttitle = new JTextField();
         tectsc = new JTextField();
         tectsf = new JTextField();
         tectss = new JTextField();
 
         tnumber.setFont(standardTextFont);
         tname.setFont(standardTextFont);
         tdescription.setFont(standardTextFont);
         ttitle.setFont(standardTextFont);
         tectsc.setFont(standardTextFont);
         tectsf.setFont(standardTextFont);
         tectss.setFont(standardTextFont);
 
         bok = new JButton("Speichern");
         bok.setFont(standardButtonFont);
         bok.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String number = tnumber.getText();
                 String name = tname.getText();
                 String description = tdescription.getText();
                 String title = ttitle.getText();
                 String ectsc = tectsc.getText();
                 String ectsf = tectsf.getText();
                 String ectss = tectss.getText();
                 Integer c;
                 Integer f;
                 Integer s;
 
                 if(number==null || number.equals("")) {
                     JOptionPane.showMessageDialog(new JFrame(), "Bitte geben Sie eine Studienkennzahl an.");
                     return;
                 }
                 if(name==null || name.equals("")) {
                     JOptionPane.showMessageDialog(new JFrame(), "Bitte geben Sie einen Namen an.");
                     return;
                 }
                 if(title==null || title.equals("")) {
                     JOptionPane.showMessageDialog(new JFrame(), "Bitte geben Sie den akademischen Titel an.");
                     return;
                 }
                 if(ectsc==null) {
                     JOptionPane.showMessageDialog(new JFrame(), "Bitte geben Sie die Wahl-ECTS-Punkte an.");
                     return;
                 }
                 if(ectsf==null) {
                     JOptionPane.showMessageDialog(new JFrame(), "Bitte geben Sie die Frei-ECTS-Punkte an.");
                     return;
                 }
                 if(ectss==null) {
                     JOptionPane.showMessageDialog(new JFrame(), "Bitte geben Sie die SoftSkill-ECTS-Punkte an.");
                     return;
                 }
                 try {
                     c = Integer.parseInt(ectsc);
                 } catch (NumberFormatException e1) {
                     JOptionPane.showMessageDialog(new JFrame(), "Die angegebenen Wahl-ECTS-Punkte sind keine gültige zahl.");
                     return;
                 }
                 try {
                     f = Integer.parseInt(ectsf);
                 } catch (NumberFormatException e1) {
                     JOptionPane.showMessageDialog(new JFrame(), "Die angegebenen Frei-ECTS-Punkte sind keine gültige zahl.");
                     return;
                 }
                 try {
                     s = Integer.parseInt(ectss);
                 } catch (NumberFormatException e1) {
                     JOptionPane.showMessageDialog(new JFrame(), "Die angegebenen SoftSkill-ECTS-Punkte sind keine gültige zahl.");
                     return;
                 }
 
                 Curriculum curriculum = new Curriculum();
                 curriculum.setStudyNumber(number);
                 curriculum.setName(name);
                 curriculum.setDescription(description);
                 curriculum.setAcademicTitle(title);
                 curriculum.setEctsChoice(c);
                 curriculum.setEctsFree(f);
                 curriculum.setEctsSoftskill(s);
 
                 try {
                     curriculumService.createCurriculum(curriculum);
                 } catch (ServiceException e1) {
                     PanelTube.backgroundPanel.viewSmallInfoText("Es gab einen Fehler beim Erstellen des Studienplans.", SmallInfoPanel.Error);
                     log.error("Error: " + e1.getMessage());
                 }
                 PanelTube.studienplanPanel.refreshCurriculumComboBox();
                 PanelTube.backgroundPanel.viewSmallInfoText("Studienplan wurde gespeichert.", SmallInfoPanel.Success);
                 setVisible(false);
                 PanelTube.backgroundPanel.showLastComponent();
             }
         });
 
         this.add(lnumber);
         this.add(tnumber);
         this.add(lname);
         this.add(tname);
         this.add(ldescription);
         this.add(tdescription);
         this.add(ltitle);
         this.add(ttitle);
         this.add(lectsc);
         this.add(tectsc);
         this.add(lectsf);
         this.add(tectsf);
         this.add(lectss);
         this.add(tectss);
         this.add(bok);
 
         lname.setBounds((int)simpleWhiteSpace.getX() + bigSpace,(int)simpleWhiteSpace.getY() + bigSpace,labelWidth,oHeight);
         tname.setBounds(inputX, lname.getY(), inputWidth,oHeight);
 
         lnumber.setBounds(labelX, lname.getY() + lname.getHeight() + space, labelWidth,oHeight);
         tnumber.setBounds(inputX, lnumber.getY(), inputWidth,oHeight);
 
         ldescription.setBounds(labelX, lnumber.getY() + lnumber.getHeight() + space, labelWidth,oHeight);
         tdescription.setBounds(inputX, ldescription.getY(), inputWidth,oHeight);
 
         ltitle.setBounds(labelX, ldescription.getY() + ldescription.getHeight() + space, labelWidth,oHeight);
         ttitle.setBounds(inputX, ltitle.getY(), inputWidth,oHeight);
 
         lectsc.setBounds(labelX, ltitle.getY() + ltitle.getHeight() + space, labelWidth,oHeight);
         tectsc.setBounds(inputX, lectsc.getY(), inputWidth,oHeight);
 
         lectsf.setBounds(labelX, lectsc.getY() + lectsc.getHeight() + space, labelWidth,oHeight);
         tectsf.setBounds(inputX, lectsf.getY(), inputWidth,oHeight);
 
         lectss.setBounds(labelX, lectsf.getY() + lectsf.getHeight() + space, labelWidth,oHeight);
         tectss.setBounds(inputX, lectss.getY(), inputWidth,oHeight);
 
         bok.setBounds((int)simpleWhiteSpace.getX()+(int)simpleWhiteSpace.getWidth()*1/3-170-120, (int)simpleWhiteSpace.getY() + (int)simpleWhiteSpace.getHeight()-60, 130,40);
 
     }
 }
 
