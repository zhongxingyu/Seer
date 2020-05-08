 package at.ac.tuwien.sepm.ui.studyProgress.display;
 
 import at.ac.tuwien.sepm.dao.MetaLvaDao;
 import at.ac.tuwien.sepm.entity.LVA;
 import at.ac.tuwien.sepm.entity.MetaLVA;
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.PropertyService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.service.impl.ValidationException;
 import at.ac.tuwien.sepm.ui.StandardInsidePanel;
 import at.ac.tuwien.sepm.ui.UI;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 01.06.13
  * Time: 19:23
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class ViewPanel extends StandardInsidePanel {
     private JLabel majorName = new JLabel("Bachelor - Teststudium");
     private JButton fwd;
     private JButton bwd;
     private SemesterPanel semester;
     // private JList semesterList;
     private LVAService service;
     private PropertyService propertyService;
 
     //private int semesterAnz = 6;
     private int year = 2013;
     private boolean isWinterSemester = true;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public ViewPanel(LVAService service, PropertyService propertyService) {
         this.propertyService=propertyService;
         this.service=service;
         this.setLayout(null);
         this.setOpaque(false);
         loadFonts();
         setBounds((int) startCoordinateOfWhiteSpace.getX(), (int) startCoordinateOfWhiteSpace.getY(), (int) whiteSpace.getWidth(), (int) whiteSpace.getHeight());
         setMajorName();
         //setSemesterAnzahl();
         year = getFirstYear();
         setIsWinterSemester();
         initMajorName();
         initSemesterPanel();
         initButtons();
         // initSkiplist(semesterAnz);
         refresh();
         repaint();
         revalidate();
     }
 
     /*private void initSkiplist(int semesterAnz) {
         JLabel semesters = new JLabel("Semester:");
         semesters.setFont(standardSmallerTitleFont);
         semesters.setBounds(50, majorName.getY() + majorName.getHeight() +semester.getHeight()+50, 150, 25);
         this.add(semesters);
 
         DefaultListModel listModel = new DefaultListModel();
         for(int i = 1; i <= semesterAnz; i++) {
             listModel.addElement(i + ". Semester");
         }
         semesterList = new JList(listModel);
         semesterList.setSelectedIndex(1);
         semesterList.addListSelectionListener(new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent listSelectionEvent) {
                 semester.setSemesterTitle((String)semesterList.getSelectedValue());
             }
         });
         semesterList.setBounds(50, semesters.getY() + semesters.getHeight() + 10, 230, (20 * (semesterAnz)));
         semesterList.setBorder(BorderFactory.createLineBorder(Color.black));
         this.add(semesterList);
     } */
 
     public void refresh() {
         try {
             ArrayList<LVA> temp = new ArrayList<>();
             for (LVA l : service.readByYearAndSemester(year, isWinterSemester)) {
                 if (l.isInStudyProgress())
                     temp.add(l);
             }
             semester.setLvas(temp);
         } catch (ServiceException e) {
             log.error(e.getMessage());
         } catch (ValidationException e) {
             log.error(e.getMessage());
         }
     }
 
 
 
     private void initButtons() {
         fwd = new JButton();
         bwd = new JButton();
 
         try {
             bwd.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navleft.png"))));
             fwd.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navright.png"))));
         } catch (IOException e) {
             log.error(e.getMessage());
         }
 
         bwd.setBounds(10, majorName.getY() + majorName.getHeight() + 30+semester.getHeight()/2, 40, 40);
         bwd.setOpaque(false);
         bwd.setContentAreaFilled(false);
         bwd.setBorderPainted(false);
         bwd.setCursor(new Cursor(Cursor.HAND_CURSOR));  //todo change cursor...
         bwd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 // if (!semesterList.isSelectionEmpty()) {
                 //     semesterList.setSelectedIndex(semesterList.getSelectedIndex()-1);
                 if (year > getFirstYear()) {
                     if (!isWinterSemester)
                         year--;
                     isWinterSemester = !isWinterSemester;
                     refresh();
                 }
                 // }
             }
         });
 
         fwd.setBounds((int) whiteSpace.getWidth() - 50,majorName.getY() + majorName.getHeight() + 30+semester.getHeight()/2, 40, 40);
         fwd.setOpaque(false);
         fwd.setContentAreaFilled(false);
         fwd.setBorderPainted(false);
         fwd.setCursor(new Cursor(Cursor.HAND_CURSOR));  //todo change cursor...
         fwd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 //  if (!semesterList.isSelectionEmpty()) {
                 //     semesterList.setSelectedIndex(semesterList.getSelectedIndex()+1);
                 if (year < getYearNow()) {
                     if (isWinterSemester)
                         year++;
                     isWinterSemester = !isWinterSemester;
                     refresh();
                 }
                 //  }
             }
         });
         this.add(fwd);
         this.add(bwd);
     }
 
     private void initSemesterPanel() {
         ArrayList<LVA> test = new ArrayList<LVA>();
         semester = new SemesterPanel(this.getX(), majorName.getY() + majorName.getHeight() + 30, "1. Semester", test);
         this.add(semester);
     }
 
     private void initMajorName() {
         majorName.setFont(standardTitleFont);
         majorName.setBounds(20,0,(int) whiteSpace.getWidth() - 10,50);
         this.add(majorName);
     }
 
     public void setMajorName() {
         if (propertyService.getProperty("user.majorName") != null) {
             majorName.setText(propertyService.getProperty("user.majorName"));
             repaint();
         }
     }
 
    /* public void setSemesterAnzahl() {
         try {
             this.semesterAnz = service.numberOfSemestersInStudyProgress();
         } catch (ServiceException e) {
             log.error(e.getMessage());
         }
     }*/
 
     public int getFirstYear() {
         try {
             return service.firstYear();
         } catch (ServiceException e) {
            JOptionPane.showMessageDialog(ViewPanel.this, "Sie müssen zuerst den in den Einstellungen angeben, wann Sie mit ihrem Studium begonnen haben!", "Fehler", JOptionPane.ERROR_MESSAGE);
             log.error(e.getMessage());
             return 0;
         }
     }
 
     private int getYearNow() {
         try {
             return getFirstYear() + (service.numberOfSemestersInStudyProgress()/2);
         } catch (ServiceException e) {
             log.error(e.getMessage());
             return 0;
         }
     }
 
     public void setIsWinterSemester() {
         try {
             this.isWinterSemester = service.isFirstSemesterAWinterSemester();
         } catch (ServiceException e) {
             log.error(e.getMessage());
         }
     }
 }
