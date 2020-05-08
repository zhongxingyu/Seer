 package at.ac.tuwien.sepm.ui.startUp;
 
 import at.ac.tuwien.sepm.service.EscapeException;
 import at.ac.tuwien.sepm.service.PropertyService;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import at.ac.tuwien.sepm.ui.template.WideComboBox;
 import com.toedter.calendar.JYearChooser;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 
 import javax.swing.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * Author: Georg Plaz
  */
 public class Second extends SimpleDisplayPanel {
     private static Logger logger = LogManager.getLogger(Second.class);
 
     private JTextField facebookKey = new JTextField();
     private JButton progressFurther= new JButton("Weiter");
     private JButton goBack= new JButton("Zurück");
     private JProgressBar progressBar = new JProgressBar();
     private WideComboBox semesterDrop = new WideComboBox(new Semester[]{Semester.W,Semester.S});
     private JYearChooser year = new JYearChooser();
     private ViewStartUp startUp;
 
     public Second(double width, double height, ViewStartUp parent) {
         super(width, height,parent);
         this.startUp=parent;
         subInit();
     }
     public void subInit(){
         year.setStartYear(1900);
         year.setEndYear(DateTime.now().getYear());
         refresh();
         progressFurther.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try {
 
                     int un = facebookKey.getText().length();
                     if (un > 0) {
                         startUp.propertyService.setProperty(PropertyService.FACEBOOK_KEY, facebookKey.getText());
                        startUp.facebookService.authenticate();
                     } else if (un == 0) {
                         PanelTube.backgroundPanel.viewSmallInfoText("Die Facebook-Daten sind ungültig!", SmallInfoPanel.Warning);
                         throw new EscapeException();
                     }
                     startUp.next();
                     startUp.propertyService.setProperty(PropertyService.FIRST_SEMESTER, ((Semester) semesterDrop.getSelectedItem()).toShortString());
                     startUp.propertyService.setProperty(PropertyService.FIRST_YEAR, ""+year.getYear());
                 } catch (EscapeException ignore) {      }
             }
         });
         goBack.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 startUp.back();
             }
         });
 
 
         addText("Gratuliere!\n\nDu hast es durch das erste Panel geschafft!", true);
 
         addText("Wir würden gerne von dir wissen, in welchem Semester du angefangen hast zu studieren!", true);
         addText("Diese Angabe ist leider verpflichtend!\n", true);
         addRow(new JTextArea("Jahr"), year, true);
         addRow(new JTextArea("Semester"),semesterDrop,true);
 
         addRow(goBack,null,true,false);
 
         addText("\n\nUnser App bietet auch eine Facebookintegration an!", false);
         addText("Wenn du diesen Service nutzen willst, kannst du deine Facebookdaten hier eintragen.",false);
         addText("Die Angabe ist optional.\n", false);
 
         addRow(new JTextArea("Facebook-API-Key"), facebookKey, false);
 
         addRow(null,progressFurther,false,false);
 
 
 
     }
     private void setWaiting(boolean b){
         progressFurther.setEnabled(!b);
         progressBar.setVisible(b);
         facebookKey.setEnabled(!b);
         //studyDrop.setEnabled(!b);
     }
 
 
     @Override
     public void refresh() {
         facebookKey.setText(startUp.propertyService.getProperty(PropertyService.FACEBOOK_KEY, ""));
         year.setYear(Integer.parseInt(startUp.propertyService.getProperty(PropertyService.FIRST_YEAR,""+ DateTime.now().getYear())));
         semesterDrop.setSelectedItem(Semester.parse(startUp.propertyService.getProperty(PropertyService.FIRST_SEMESTER, Semester.W.toString())));
     }
 }
