 package at.ac.tuwien.sepm.ui;
 
 import at.ac.tuwien.sepm.service.LVAService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.calender.cal.CalMonthGenerator;
 import at.ac.tuwien.sepm.ui.calender.cal.CalWeekGenerator;
 import at.ac.tuwien.sepm.ui.calender.cal.CalendarInterface;
 import at.ac.tuwien.sepm.ui.calender.todo.TodoPanel;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 //import org.joda.time.DateTime;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.annotation.PostConstruct;
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 //import java.util.Locale;
 
 @UI
 public class CalendarPanel extends StandardInsidePanel {
     private JButton tab1;
     private JButton tab2;
     private JButton tab3;
     private JButton fwd;
     private JButton bwd;
     private JButton importBtn;
     private JLabel month;
     private JComboBox semester;
 
     static DefaultTableModel mtblCalendar; //Table model
     static JScrollPane stblCalendar; //The scrollpane
     static JPanel pnlCalendar;
     static JTable tblCalendar;
 
     private CalMonthGenerator calPanelMonth;
     private CalWeekGenerator calPanelWeek;
     private CalendarInterface activeView;
     private TodoPanel todoPanel;
 
     private LVAService lvaService;
 
     private boolean showTodo = false;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public CalendarPanel(CalMonthGenerator calPanelMonth, CalWeekGenerator calPanelWeek, TodoPanel todoPanel, LVAService lvaService) {
         init();
         PanelTube.calendarPanel=this;
         this.lvaService=lvaService;
         this.calPanelMonth=calPanelMonth;
         this.calPanelWeek=calPanelWeek;
         this.activeView=calPanelWeek;
         this.todoPanel=todoPanel;
         add(calPanelWeek);
 
         createTabButtons();
         createNavButtons();
         createImportButton();
         changeImage(1);
         createTop();
         this.revalidate();
         this.repaint();
     }
 
     @PostConstruct
     public void initGenerators() {
         calPanelWeek.init();
         calPanelMonth.init();
     }
 
     public void showTodo(boolean show) {
         showTodo = show;
         changeImage(3);
     }
 
     private void createTop() {
         month = new JLabel(activeView.getTimeIntervalInfo().toUpperCase());
         month.setBounds((int)((size.getWidth()/2)-(image.getWidth(null)/2))+5, (int)(size.getHeight()/2-image.getHeight(null)/2)-31, 500, 30);
         month.setForeground(Color.WHITE);
         month.setFont(standardTitleFont);
 
         semester = new JComboBox();
         semester.setBounds((int)((size.getWidth()/2)-(image.getWidth(null)/2))+5+295, (int)(size.getHeight()/2-image.getHeight(null)/2)+5, 90, 20);
         semester.setFont(standardButtonFont);
         refreshTop();
         this.add(month);
         this.add(semester);
     }
 
     private void refreshTop() {
         try {
             boolean winterSem = lvaService.isFirstSemesterAWinterSemester();
             int semesters = lvaService.numberOfSemestersInStudyProgress();
             int year = lvaService.firstYearInStudyProgress();
             for (int x = 0; x < semesters; x++) {
                 semester.addItem(winterSem ? "WS" : "SS" + year);
                 if (winterSem) {
                     year++;
                 }
                 winterSem = !winterSem;
             }
         } catch (ServiceException e) {
            // TODO implement the PanelTube
            //PanelTube.backgroundPanel.viewInfoText("Die Anzahl der Semester konnte nicht geladen werden.", SmallInfoPanel.Error);
         }
     }
 
     private void createImportButton() {
         importBtn = new JButton("Importieren");
         importBtn.setBounds(910, 581, 110, 38);
         importBtn.setFont(standardButtonFont);
         importBtn.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 PanelTube.backgroundPanel.viewImport();
             }
         });
 
         this.add(importBtn);
     }
 
     private void createNavButtons() {
         fwd = new JButton();
         bwd = new JButton();
 
         try {
             bwd.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navleft.png"))));
             fwd.setIcon(new ImageIcon(ImageIO.read(new File("src/main/resources/img/navright.png"))));
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         bwd.setBounds(65, 160, 40, 40);
         bwd.setOpaque(false);
         bwd.setContentAreaFilled(false);
         bwd.setBorderPainted(false);
         bwd.setCursor(new Cursor(Cursor.HAND_CURSOR));
         bwd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     activeView.last();
                     month.setText(activeView.getTimeIntervalInfo().toUpperCase());
                 } catch (ServiceException e) {
                     PanelTube.backgroundPanel.viewInfoText("Fehler beim Laden des Kalenders.", SmallInfoPanel.Error);
                     month.setText("ERROR");
                 }
             }
         });
 
         fwd.setBounds((int)size.getWidth()-65-40,160, 40, 40);
         fwd.setOpaque(false);
         fwd.setContentAreaFilled(false);
         fwd.setBorderPainted(false);
         fwd.setCursor(new Cursor(Cursor.HAND_CURSOR));
         fwd.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 try {
                     activeView.next();
                     month.setText(activeView.getTimeIntervalInfo().toUpperCase());
                 } catch (ServiceException e) {
                     PanelTube.backgroundPanel.viewInfoText("Fehler beim Laden des Kalenders.", SmallInfoPanel.Error);
                     month.setText("ERROR");
                 }
             }
         });
 
         this.add(fwd);
         this.add(bwd);
     }
 
     private void createTabButtons() {
         tab1 = new JButton();
         tab2 = new JButton();
         tab3 = new JButton();
 
         ArrayList<JButton> tabs = new ArrayList<JButton>();
         tabs.add(tab1);
         tabs.add(tab2);
         tabs.add(tab3);
 
         tab1.setBounds(97, 63, 142, 36);
         tab1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(1);
                 remove(calPanelMonth);
                 remove(todoPanel);
                 add(calPanelWeek);
                 calPanelWeek.refresh();
                 activeView = calPanelWeek;
                 month.setText(activeView.getTimeIntervalInfo().toUpperCase());
                 calPanelWeek.revalidate();
                 calPanelWeek.repaint();
 
             }
         });
 
         tab2.setBounds(97+142,63,142,36);
         calPanelMonth.setBounds(size);
         calPanelWeek.setBounds(size);
         tab2.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(2);
                 remove(calPanelWeek);
                 remove(todoPanel);
                 add(calPanelMonth);
                 calPanelMonth.refresh();
                 activeView = calPanelMonth;
                 month.setText(activeView.getTimeIntervalInfo().toUpperCase());
                 calPanelMonth.revalidate();
                 calPanelMonth.repaint();
             }
         });
 
         tab3.setBounds(878, 63, 142, 36);
         todoPanel.setBounds(110, 110, 900, 450);
         tab3.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(3);
                 remove(calPanelMonth);
                 remove(calPanelWeek);
                 add(todoPanel);
                 revalidate();
                 repaint();
             }
         });
 
         for (int i = 0; i < 3; i++) {
             tabs.get(i).setCursor(new Cursor(Cursor.HAND_CURSOR));
             tabs.get(i).setOpaque(false);
             tabs.get(i).setContentAreaFilled(false);
             tabs.get(i).setBorderPainted(false);
             this.add(tabs.get(i));
         }
     }
 
     private void changeImage(int nmb) {
         try{
             switch(nmb) {
                 case 1:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/calw.png"));
                     toggleComponents("show");
                     break;
                 case 2:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/calm.png"));
                     toggleComponents("show");
                     break;
                 case 3:
                     if (showTodo) {
                         image = ImageIO.read(ClassLoader.getSystemResource("img/caldt.png"));
                     } else {
                         image = ImageIO.read(ClassLoader.getSystemResource("img/caldd.png"));
                     }
                     toggleComponents("hide");
                     break;
                 default:
                     break;
             }
             this.repaint();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void toggleComponents(String s) {
         if (month != null && semester != null && fwd != null && bwd != null) {
             if (s.equals("show")) {
                 month.setVisible(true);
                 semester.setVisible(true);
                 fwd.setVisible(true);
                 bwd.setVisible(true);
                 importBtn.setVisible(true);
             } else if (s.equals("hide")) {
                 month.setVisible(false);
                 semester.setVisible(false);
                 fwd.setVisible(false);
                 bwd.setVisible(false);
                 importBtn.setVisible(false);
             } else {
                 //troll out loud
             }
         }
     }
 
     @Override
     public void refresh() {
         calPanelMonth.refresh();
         calPanelWeek.refresh();
         refreshTop();
     }
 
     public void jumpToDate(DateTime anyDateOfWeek) {
         calPanelWeek.goToDay(anyDateOfWeek);
         changeImage(1);
         remove(calPanelMonth);
         remove(todoPanel);
         add(calPanelWeek);
         calPanelWeek.refresh();
         activeView = calPanelWeek;
         month.setText(activeView.getTimeIntervalInfo().toUpperCase());
         calPanelWeek.revalidate();
         calPanelWeek.repaint();
     }
 }
