 package at.ac.tuwien.sepm.ui.studyProgress.display;
 
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.service.*;
 import at.ac.tuwien.sepm.service.impl.ValidationException;
 import at.ac.tuwien.sepm.ui.SmallInfoPanel;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 @UI
 public class ViewPanel extends StandardInsidePanel {
     private JLabel majorName = new JLabel("dummy");
     private JButton fwd;
     private JButton bwd;
     private SemesterPanel semester;
     private LVAService service;
     private PropertyService propertyService;
     private boolean refreshing;
 
     private SemesterList semesterList;
     private ArrayList<LVA> makeSure = null;
 
     private ArrayList<LVA> lvas;
 
     @Autowired
     private FacebookService facebookService;
 
     @Autowired
     private MetaLVAService metaLVAService;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public ViewPanel(LVAService service, PropertyService propertyService) {
         this.propertyService=propertyService;
         this.service=service;
         this.semesterList = new SemesterList();
         this.setLayout(null);
         this.setOpaque(false);
         loadFonts();
         setBounds((int) startCoordinateOfWhiteSpace.getX(), (int) startCoordinateOfWhiteSpace.getY(),(int) whiteSpace.getWidth(),(int) whiteSpace.getHeight());
 
         setMajorName();
         initMajorName();
         initSemesterPanel();
         initButtons();
         refresh();
     }
 
     public synchronized void refresh() {
         if(refreshing){
             return;
         }
         refreshing = true;
         setCursor(new Cursor(Cursor.WAIT_CURSOR));
         new Thread(){
             @Override
             public void run(){
                 try {
                     semesterList.refresh();
                     ArrayList<LVA> temp = null;
                     setMajorName();
                     if (getSemesterAnzahl() <= 1 && makeSure == null) {
                         bwd.setVisible(false);
                         fwd.setVisible(false);
                     } else {
                         bwd.setVisible(true);
                         fwd.setVisible(true);
                     }
                         temp = new ArrayList<>();
                         for (LVA l : service.readByYearAndSemester(semesterList.getCurrentYear(), semesterList.getCurrentSemesterIsWinterSemester())) {
                             if (l.isInStudyProgress())
                                 temp.add(l);
                         }
                         lvas = temp;
                         semester.setLvas(temp);
                     if (getSemesterAnzahl() != 0 && semesterList.getCurrentSemester() != 0 && temp != null) {
                         if (temp.isEmpty() && makeSure == null) {
                             semester.setSemesterTitle("Bitte planen Sie ein Semester!");
                             bwd.setVisible(false);
                             fwd.setVisible(false);
                         } else {
                             makeSure=temp;
                             String tempo = semesterList.getCurrentSemesterIsWinterSemester()? "WS" : "SS";
                             semester.setSemesterTitle(semesterList.getCurrentSemester() + ". Semester (" + semesterList.getCurrentYear() + ", " + tempo + ")");
                         }
                     } else {
                         semester.setSemesterTitle("Bitte planen Sie ein Semester!");
                         bwd.setVisible(false);
                         fwd.setVisible(false);
                     }
                     ViewPanel.this.repaint();
                     refreshing = false;
                 } catch (ServiceException e) {
                     log.error(e);
                     PanelTube.backgroundPanel.viewSmallInfoText("Fehler beim Laden des Studienverlaufs", SmallInfoPanel.Error);
                 } catch (ValidationException e) {
                     log.error(e);
                     PanelTube.backgroundPanel.viewSmallInfoText("Fehler beim Laden des Studienverlaufs", SmallInfoPanel.Error);
                 }
                 ViewPanel.this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
             }
         }.start();
     }
 
     public void refreshSemesterList() {
         semesterList.refresh();
     }
 
     private void initButtons() {
         fwd = new JButton();
         bwd = new JButton();
 
         try {
             bwd.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navleft.png"))));
             fwd.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navright.png"))));
             bwd.setRolloverIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navlefthighlight.png"))));
             fwd.setRolloverIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navrighthighlight.png"))));
         } catch (IOException e) {
             log.error(e.getMessage());
         }
         bwd.setCursor(new Cursor(Cursor.HAND_CURSOR));
         bwd.setBounds(10, majorName.getY() + majorName.getHeight() + semester.getHeight() / 2, 40, 40);
         bwd.setOpaque(false);
         bwd.setContentAreaFilled(false);
         bwd.setBorderPainted(false);
         bwd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 semesterList.last();
                 refresh();
             }
         });
 
         fwd.setBounds((int) whiteSpace.getWidth() - 50,majorName.getY() + majorName.getHeight() +semester.getHeight()/2, 40, 40);
         fwd.setOpaque(false);
         fwd.setContentAreaFilled(false);
         fwd.setBorderPainted(false);
         fwd.setCursor(new Cursor(Cursor.HAND_CURSOR));
         fwd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 semesterList.next();
                 refresh();
             }
         });
         this.add(bwd);
 
         JButton shareButton = new JButton("Auf Facebook teilen");
         shareButton.setFont(standardButtonFont);
         //shareButton.setBounds((int) paneExams.getX(), (int) paneExams.getY() + (int) paneExams.getHeight() + 8, 150, 30);
         shareButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ArrayList<MetaLVA> metaLVAs = new ArrayList<MetaLVA>();
                 for(LVA lva : lvas) {
                     try {
                         if(lva.getMetaLVA() != null && lva.getMetaLVA().getName() != null)
                             metaLVAs.add(lva.getMetaLVA());
                         else
                             metaLVAs.add(metaLVAService.readById(lva.getMetaLVA().getId()));
                     } catch (ServiceException e1) {
                         log.error("meta lva not found", e1);
                     }
                 }
                 facebookService.postLvasToWall(metaLVAs);
             }
         });
         shareButton.setEnabled(true);
         shareButton.setVisible(true);
        shareButton.setBounds(semester.getX(),semester.getY()+semester.getHeight()+5,200,40);
         this.add(shareButton);
     }
 
     private void initSemesterPanel() {
         semester = new SemesterPanel(this.getX(), majorName.getY() + majorName.getHeight() + 30);
         this.add(semester);
     }
 
     private void initMajorName() {
         majorName.setFont(standardTitleFont);
         majorName.setBounds(20,0,(int) whiteSpace.getWidth() - 10,50);
         this.add(majorName);
     }
 
     public void setMajorName() {
         String temp = propertyService.getProperty(PropertyService.MAJOR);
         if (temp == null || temp.isEmpty()) {
             majorName.setText("Kein Studium angegeben");
         }
         majorName.setText(temp);
         repaint();
     }
 
     private int getSemesterAnzahl() {
         try {
             return service.numberOfSemestersInStudyProgress();
         } catch (ServiceException e) {
             log.error(e.getMessage());
             return 0;
         }
     }
 
     private int getFirstYear() {
         try {
             return service.firstYearInStudyProgress();
         } catch (ServiceException e) {
             log.error(e.getMessage());
             return DateTime.now().getYear();
         }
     }
 
     private boolean isFirstSemesterWinter() {
         try {
             return service.isFirstSemesterAWinterSemester();
         } catch (ServiceException e) {
             log.error(e.getMessage());
             return true;
         }
     }
 
 
 
     private class SemesterList {
         private int semesterAnz;
         private boolean currWinterSemester;
         private int currSemester = 0;
 
         SemesterList() {
             refresh();
         }
 
         void refresh() {
             this.semesterAnz = getSemesterAnzahl();
             if (currSemester == 0) {
                 this.currSemester = semesterAnz;
                 this.currWinterSemester = isFirstSemesterWinter();
                 for (int i = 1; i  < semesterAnz; i++) {
                     this.currWinterSemester =!this.currWinterSemester;
                 }
             }
         }
 
         int getCurrentYear() {
             if (isFirstSemesterWinter()) {
                 return getFirstYear() + (currSemester/2);
             } else {
                 if (currWinterSemester) {
                     return getFirstYear() + (currSemester/2) -1;
                 } else {
                     return getFirstYear() + (currSemester/2);
                 }
             }
         }
 
         int getCurrentSemester() {
             return currSemester;
         }
 
         boolean getCurrentSemesterIsWinterSemester() {
             return currWinterSemester;
         }
 
         void next() {
             if (currSemester < semesterAnz) {
                 ViewPanel.this.add(bwd);
                 ViewPanel.this.add(fwd);
                 currSemester++;
                 currWinterSemester =!currWinterSemester;
                 if (currSemester == semesterAnz) {
                     ViewPanel.this.remove(fwd);
                 }
                 ViewPanel.this.repaint();
             }
         }
 
         void last() {
             if (currSemester > 1) {
                 ViewPanel.this.add(bwd);
                 ViewPanel.this.add(fwd);
                 currSemester--;
                 currWinterSemester =!currWinterSemester;
                 if (currSemester == 1) {
                     ViewPanel.this.remove(bwd);
                 }
                 ViewPanel.this.repaint();
             }
         }
     }
 }
