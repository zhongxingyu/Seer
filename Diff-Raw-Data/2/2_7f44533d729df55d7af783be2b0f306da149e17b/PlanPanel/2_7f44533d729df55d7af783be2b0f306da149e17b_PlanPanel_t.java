 package at.ac.tuwien.sepm.ui.verlauf;
 
 import at.ac.tuwien.sepm.dao.DateDao;
 import at.ac.tuwien.sepm.dao.LvaDao;
 import at.ac.tuwien.sepm.dao.MetaLvaDao;
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.service.DateService;
 import at.ac.tuwien.sepm.service.Semester;
 import at.ac.tuwien.sepm.service.semesterPlanning.IntelligentSemesterPlaner;
 import at.ac.tuwien.sepm.service.semesterPlanning.LVAUtil;
 import at.ac.tuwien.sepm.ui.MetaLva.MetaLVADisplayPanel;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.util.List;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo, Georg
  * Date: 01.06.13
  * Time: 16:19
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class PlanPanel extends StandardInsidePanel {
     MetaLvaDao metaLVADAO;
     LvaDao lvaDAO;
     DateDao dateDAO;
 
     /*@Autowired
     LVAService lvaService;
 */
     DateService dateService;
 
     Logger logger = LogManager.getLogger(this.getClass().getSimpleName());
 
     private Rectangle outputPlane = new Rectangle(483,12,521,496);
     private MetaLVADisplayPanel pane;
     private List<MetaLVA> plannedMetaLVAs = new ArrayList<MetaLVA>(0);
     private int plannedYear =-1;
     private Semester plannedSemester =null;
 
     private JButton plan;
     private JButton take;
 
     // < basic settings >
     private JLabel desiredECTSTextLabel;
     private JTextField desiredECTSText;
     private JLabel yearTextLabel;
     private JTextField yearText;
     private JLabel semesterDropLabel;
     private JComboBox  semesterDrop;
     // </ basic settings >
 
     // < advanced settings >
     private JButton showAdvancedOptions;
     private JLabel intersectVOCheckLabel;
     private JCheckBox intersectVOCheck;
     private JLabel intersectUECheckLabel;
     private JCheckBox intersectUECheck;
     private JLabel intersectExamCheckLabel;
     private JCheckBox intersectExamCheck;
     private JLabel intersectCustomCheckLabel;
     private JCheckBox intersectCustomCheck;
     private JLabel considerStudyProgressCheckLabel;
     private JCheckBox considerStudyProgressCheck;
     private JLabel timeBetweenLabel;
     private JComboBox timeBetween;
     private String[] timeBetweenTextLabelStrings;
     private JLabel timeBetweenTextLabel;
     private JTextField timeBetweenText;
     // </ advanced settings >
 
     private boolean advancedShown = true;
     private boolean showTimeBetweenText = false;
     @Autowired
     public PlanPanel(MetaLvaDao metaLVADAO,DateDao dateDao,DateService dateService,LvaDao lvaDAO) {
         this.lvaDAO=lvaDAO;
         this.dateService=dateService;
         this.metaLVADAO= metaLVADAO;
         this.dateDAO = dateDao;
 
 
         this.setLayout(null);
         this.setOpaque(false);
         loadFonts();
         setBounds((int)StudStartCoordinateOfWhiteSpace.getX(), (int)StudStartCoordinateOfWhiteSpace.getY(),(int)whiteSpaceStud.getWidth(),(int)whiteSpaceStud.getHeight());
         initTextAndLabels();
         toggleAdvanced();
         initButtons();
         initLVAPane();
     }
     private void refreshMetaLVAs(List<MetaLVA> metaLVAs){
         plannedMetaLVAs =metaLVAs;
         remove(pane);
         pane = new MetaLVADisplayPanel(plannedMetaLVAs, (int)outputPlane.getWidth(), (int)outputPlane.getHeight());
         pane.setBounds(outputPlane);
         add(pane);
         repaint();
         revalidate();
     }
 
      /*-----------------------RIGHT SIDE  PLAN ANZEIGEN-------------------*/
 
     private void initLVAPane() {
         //plannedMetaLVAs = metaLVADAO.readByYearSemesterStudyProgress(dateService.getCurrentYear(), dateService.getCurrentSemester(), true);
 
         plannedMetaLVAs = new ArrayList<MetaLVA>(); //metaLVADAO.readUncompletedByYearSemesterStudyProgress(2013,Semester.S, true);
         pane = new MetaLVADisplayPanel(plannedMetaLVAs, (int)outputPlane.getWidth(), (int)outputPlane.getHeight()); //todo plannedMetaLVAs anzeigen die schon geplant sind
         pane.setBounds(outputPlane);
         this.add(pane);
     }
 
 
     /*-----------------------LEFT SIDE  PLANEN-------------------*/
     private void initButtons() {
         take = new JButton("Übernehmen");
         take.setFont(standardButtonFont);
         take.setBounds((int) outputPlane.getX() - 150, (int) outputPlane.getY() + (int) outputPlane.getHeight() - 40, 130, 40);
         take.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 //todo warning alert: all data from year x, sem y will be overriden
                List<LVA> toRemove = lvaDAO.readUncompletedByYearSemesterStudyProgress(plannedYear,plannedSemester,true);
                 logger.debug("deleting from studyProgress: "+LVAUtil.formatLVA(toRemove,1));
                 for(LVA lva:toRemove){
                     //logger.debug("deleting from studyProgress: "+lva);
                     lva.setInStudyProgress(false);
                     try {
                         lvaDAO.update(lva);
 
                     } catch (IOException e1) {
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
                 logger.debug("adding to studyProgress: \n"+LVAUtil.formatMetaLVA(plannedMetaLVAs, 1));
                 for(MetaLVA m:plannedMetaLVAs){
 
                     LVA temp = m.getLVA(plannedYear,plannedSemester);
                     temp.setInStudyProgress(true);
                     //logger.debug("adding to studyProgress: "+temp);
                     try {
                         lvaDAO.update(temp);
                     } catch (IOException e1) {
                         e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                     }
                 }
 
             }
         });
         this.add(take);
 
         plan = new JButton("Planen");
         plan.setFont(standardButtonFont);
         plan.setBounds((int) outputPlane.getX() - 130 -take.getWidth(), (int) outputPlane.getY() + (int) outputPlane.getHeight() - 40, 90, 40);
         plan.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 new Thread(){
                     private IntelligentSemesterPlaner planer = new IntelligentSemesterPlaner();
                     public void start(){
                         super.start();
                     }
                     @Override
                     public void run(){
                         float goalECTS = Float.parseFloat(desiredECTSText.getText());
                         //boolean vointersect =  intersectVOCheck.isSelected();
                         plannedYear = Integer.parseInt(yearText.getText());
                         plannedSemester = Semester.S;
                         if (semesterDrop.getSelectedIndex() == 0) {
                             plannedSemester = Semester.W;
                         }
                         List<MetaLVA> forced;
                         List<MetaLVA> pool= metaLVADAO.readUncompletedByYearSemesterStudyProgress(plannedYear,plannedSemester,false);
                         if (considerStudyProgressCheck.isSelected()){
                             forced = metaLVADAO.readUncompletedByYearSemesterStudyProgress(plannedYear, plannedSemester, true);
 
                         }else{
                             forced = new ArrayList<>(0);
                             pool.addAll(metaLVADAO.readUncompletedByYearSemesterStudyProgress(plannedYear, plannedSemester, true));
                         }
                         MetaLVA customMetaLVA = new MetaLVA();
 
                         if(intersectCustomCheck.isSelected()){
                             customMetaLVA.setLVA(dateDAO.readNotToIntersectByYearSemester(plannedYear,plannedSemester));
                             logger.debug(customMetaLVA.getLVA(plannedYear,plannedSemester));
                             customMetaLVA.setName("custom dates");
                             customMetaLVA.setNr("-1");
                             forced.add(customMetaLVA);
                         }
 
 
                         planer.setLVAs(forced, pool);
                         ArrayList<MetaLVA> solution = planer.planSemester(goalECTS, plannedYear, plannedSemester);
                         if(intersectCustomCheck.isSelected()){
                             solution.remove(customMetaLVA);
                         }
 
 
                         logger.debug("solution provided by planner:\n"+ LVAUtil.formatShortMetaLVA(solution, 1));
 
                         refreshMetaLVAs(solution);
 
                     }
                 }.start();
 
             }
         });
         this.add(plan);
     }
 
     private void toggleAdvanced() {
         if (advancedShown == false) {
             intersectVOCheckLabel.setVisible(true);
             intersectVOCheck.setVisible(true);
 
             intersectUECheckLabel.setVisible(true);
             intersectUECheck.setVisible(true);
 
             intersectExamCheckLabel.setVisible(true);
             intersectExamCheck.setVisible(true);
 
             intersectCustomCheckLabel.setVisible(true);
             intersectCustomCheck.setVisible(true);
 
             considerStudyProgressCheck.setVisible(true);
             considerStudyProgressCheckLabel.setVisible(true);
 
             timeBetweenLabel.setVisible(true);
             timeBetween.setVisible(true);
 
             timeBetweenTextLabel.setVisible(true);
             if(showTimeBetweenText)
                 timeBetweenText.setVisible(true);
             advancedShown=true;
         } else {
             intersectVOCheckLabel.setVisible(false);
             intersectVOCheck.setVisible(false);
 
             intersectUECheckLabel.setVisible(false);
             intersectUECheck.setVisible(false);
 
             intersectExamCheckLabel.setVisible(false);
             intersectExamCheck.setVisible(false);
 
             intersectCustomCheckLabel.setVisible(false);
             intersectCustomCheck.setVisible(false);
 
             considerStudyProgressCheck.setVisible(false);
             considerStudyProgressCheckLabel.setVisible(false);
 
             timeBetweenLabel.setVisible(false);
             timeBetween.setVisible(false);
 
             timeBetweenTextLabel.setVisible(false);
             timeBetweenText.setVisible(false);
             advancedShown=false;
         }
     }
 
     private void initTextAndLabels() {
         int verticalSpace = 10;
         int textHeight = 25;
         int textWidth = 150;
 
 
         // < basic settings >
         desiredECTSTextLabel = new JLabel("Gewünschte ECTS:");
         desiredECTSTextLabel.setFont(standardTextFont);
         desiredECTSTextLabel.setBounds(10, 10, textWidth, textHeight);
         this.add(desiredECTSTextLabel);
 
         desiredECTSText = new JTextField("30");
         desiredECTSText.setBounds(desiredECTSTextLabel.getX()+desiredECTSTextLabel.getWidth()+5,desiredECTSTextLabel.getY(),25,textHeight);
         desiredECTSText.setFont(standardTextFont);
         this.add(desiredECTSText);
 
         yearTextLabel = new JLabel("Jahr:");
         yearTextLabel.setBounds(desiredECTSTextLabel.getX(),desiredECTSTextLabel.getY()+desiredECTSTextLabel.getHeight()+verticalSpace, textWidth, textHeight);
         yearTextLabel.setFont(standardTextFont);
         this.add(yearTextLabel);
 
         yearText = new JTextField(""+dateService.getCurrentYear());
         yearText.setBounds(yearTextLabel.getX() + yearTextLabel.getWidth() +5, yearTextLabel.getY(), 50, textHeight);
         yearText.setFont(standardTextFont);
         this.add(yearText);
         plannedYear=Integer.parseInt(yearText.getText());
         yearText.addKeyListener(new KeyListener() {
             @Override
             public void keyTyped(KeyEvent e) {}
             @Override
             public void keyPressed(KeyEvent e) {}
             @Override
             public void keyReleased(KeyEvent e) {
                 refreshMetaLVAs(new ArrayList<MetaLVA>(0));
             }
         });
 
         semesterDropLabel = new JLabel("Semester:");
         semesterDropLabel.setFont(standardTextFont);
         semesterDropLabel.setBounds(yearTextLabel.getX(),yearTextLabel.getY()+yearTextLabel.getHeight()+verticalSpace, textWidth, textHeight);
         this.add(semesterDropLabel);
 
 
         semesterDrop = new JComboBox (new String[]{"Winter","Sommer"});
         semesterDrop.setFont(standardButtonFont);
         semesterDrop.setBounds(semesterDropLabel.getX()+semesterDropLabel.getWidth()+5, semesterDropLabel.getY(), 100, textHeight);
         this.add(semesterDrop);
         if(dateService.getCurrentSemester().equals(Semester.W)){
             plannedSemester=Semester.W;
         }else{
             plannedSemester = Semester.S;
             semesterDrop.setSelectedIndex(1);
         }
         semesterDrop.addItemListener(new ItemListener() {
             @Override
             public void itemStateChanged(ItemEvent e) {
                 if (e.getStateChange() == ItemEvent.SELECTED) {
                     refreshMetaLVAs(new ArrayList<MetaLVA>(0));
                 }
             }
         });
         // </ basic settings >
 
         // < advanced settings >
         showAdvancedOptions = new JButton("Erweiterte Optionen");
         showAdvancedOptions.setFont(standardButtonFont);
         showAdvancedOptions.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 toggleAdvanced();
                 PlanPanel.this.repaint();
             }
         });
         showAdvancedOptions.setBounds(semesterDropLabel.getX()+50, semesterDropLabel.getY()+semesterDropLabel.getHeight()+verticalSpace, textWidth+20, textHeight);
         this.add(showAdvancedOptions);
 
         textWidth=410;
 
         intersectVOCheckLabel = new JLabel("Überprüfe Vorlesungstermine auf Überschneidungen:");
         intersectVOCheckLabel.setFont(standardTextFont);
         intersectVOCheckLabel.setBounds(semesterDropLabel.getX(), showAdvancedOptions.getY()+showAdvancedOptions.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(intersectVOCheckLabel);
 
         intersectVOCheck = new JCheckBox();
         intersectVOCheck.setBackground(new Color(0,0,0,0));
         intersectVOCheck.setBounds(intersectVOCheckLabel.getX()+intersectVOCheckLabel.getWidth()+5, intersectVOCheckLabel.getY()+5, 20, 20);
         this.add(intersectVOCheck);
         intersectVOCheck.setSelected(true);
         intersectVOCheck.setEnabled(false);
 
         intersectUECheckLabel = new JLabel("Überprüfe Übungstermine auf Überschneidungen:");
         intersectUECheckLabel.setFont(standardTextFont);
         intersectUECheckLabel.setBounds(intersectVOCheckLabel.getX(), intersectVOCheckLabel.getY()+intersectVOCheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(intersectUECheckLabel);
 
         intersectUECheck = new JCheckBox();
         intersectUECheck.setBackground(new Color(0,0,0,0));
         intersectUECheck.setBounds(intersectUECheckLabel.getX()+intersectUECheckLabel.getWidth()+5, intersectUECheckLabel.getY()+5, 20, 20);
         this.add(intersectUECheck);
         intersectUECheck.setSelected(true);
         intersectUECheck.setEnabled(false);
 
         intersectExamCheckLabel = new JLabel("Überprüfe Prüfungstermine auf Überschneidungen:");
         intersectExamCheckLabel.setFont(standardTextFont);
         intersectExamCheckLabel.setBounds(intersectUECheckLabel.getX(), intersectUECheckLabel.getY()+intersectUECheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(intersectExamCheckLabel);
 
         intersectExamCheck = new JCheckBox();
         intersectExamCheck.setBackground(new Color(0,0,0,0));
         intersectExamCheck.setBounds(intersectExamCheckLabel.getX()+intersectExamCheckLabel.getWidth()+5, intersectExamCheckLabel.getY()+5, 20, 20);
         this.add(intersectExamCheck);
         intersectExamCheck.setSelected(true);
         intersectExamCheck.setEnabled(false);
 
         intersectCustomCheckLabel = new JLabel("Überprüfe private Termine auf Überschneidungen:");
         intersectCustomCheckLabel.setFont(standardTextFont);
         intersectCustomCheckLabel.setBounds(intersectExamCheckLabel.getX(), intersectExamCheckLabel.getY()+intersectExamCheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(intersectCustomCheckLabel);
 
         intersectCustomCheck = new JCheckBox();
         intersectCustomCheck.setBackground(new Color(0,0,0,0));
         intersectCustomCheck.setBounds(intersectCustomCheckLabel.getX()+intersectCustomCheckLabel.getWidth()+5, intersectCustomCheckLabel.getY()+5, 20, 20);
         this.add(intersectCustomCheck);
 
 
         considerStudyProgressCheckLabel = new JLabel("Inkludiere bereits verplante LVAs in dem Semester:");
         considerStudyProgressCheckLabel.setFont(standardTextFont);
         considerStudyProgressCheckLabel.setBounds(intersectCustomCheckLabel.getX(), intersectCustomCheckLabel.getY()+intersectCustomCheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(considerStudyProgressCheckLabel);
 
         considerStudyProgressCheck = new JCheckBox();
         considerStudyProgressCheck.setBackground(new Color(0,0,0,0));
         considerStudyProgressCheck.setBounds(considerStudyProgressCheckLabel.getX()+considerStudyProgressCheckLabel.getWidth()+5, considerStudyProgressCheckLabel.getY()+5, 20, 20);
         this.add(considerStudyProgressCheck);
         considerStudyProgressCheck.setSelected(true);
 
 
         timeBetweenLabel = new JLabel("Zeit zwischen Terminen:");
         timeBetweenLabel.setFont(standardTextFont);
         timeBetweenLabel.setBounds(considerStudyProgressCheckLabel.getX(), considerStudyProgressCheckLabel.getY()+considerStudyProgressCheckLabel.getHeight()+verticalSpace, textWidth,textHeight);
         this.add(timeBetweenLabel);
 
         timeBetween= new JComboBox(new String[]{"exakte Zeiten verwenden","Termine dürfen sich überschneiden","Zwischen Terminen Zeit erzwingen"});
         timeBetween.setFont(standardButtonFont);
         timeBetween.setBounds(timeBetweenLabel.getX()+timeBetweenLabel.getWidth()-197, timeBetweenLabel.getY(), 220, textHeight);
         timeBetween.setSelectedIndex(0);
         this.add(timeBetween);
 
         timeBetweenTextLabelStrings= new String[]{"","Zeit um die sich Termine schneiden dürfen:","Zeit die zwischen zwei Terminen liegen muss:"};
 
         timeBetweenTextLabel = new JLabel(timeBetweenTextLabelStrings[0]);
         timeBetweenTextLabel.setFont(standardTextFont);
         timeBetweenTextLabel.setBounds(timeBetweenLabel.getX(), timeBetweenLabel.getY()+timeBetweenLabel.getHeight()+verticalSpace, textWidth,textHeight);
         timeBetweenTextLabel.setVisible(false);
         this.add(timeBetweenTextLabel);
 
         timeBetweenText = new JTextField("0");
         timeBetweenText.setBounds(timeBetweenTextLabel.getX()+timeBetweenTextLabel.getWidth(), timeBetweenTextLabel.getY(), 21, textHeight);
         timeBetweenText.setVisible(false);
         this.add(timeBetweenText);
         timeBetweenText.setEnabled(false);
 
         timeBetween.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 switch(timeBetween.getSelectedIndex()){
                     case 0:
                         timeBetweenText.setVisible(false);
                         showTimeBetweenText=false;
                         break;
                     case 1:
                         timeBetweenText.setVisible(true);
                         showTimeBetweenText=true;
                         break;
                     case 2:
                         timeBetweenText.setVisible(true);
                         showTimeBetweenText=true;
                         break;
                 }
                 timeBetweenTextLabel.setText(timeBetweenTextLabelStrings[timeBetween.getSelectedIndex()]);
             }
         });
         // </ advanced settings >
         this.repaint();
     }
 }
