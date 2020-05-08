 package at.ac.tuwien.sepm.ui;
 
 import at.ac.tuwien.sepm.entity.*;
 import at.ac.tuwien.sepm.service.RoomFinderService;
 import at.ac.tuwien.sepm.service.ServiceException;
 import at.ac.tuwien.sepm.ui.entityViews.*;
 import at.ac.tuwien.sepm.ui.template.PanelTube;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 @UI
 public class BackgroundPanel extends JPanel {
     private JButton properties;
     private JButton tab1;
     private JButton tab2;
     private JButton tab3;
 
     private StandardInsidePanel calPanel;
     private StandardInsidePanel studPanel;
     private StandardInsidePanel propsPanel;
     private StandardInsidePanel lehrPanel;
     private ViewDate viewDate;
     private ViewDeadline viewDeadline;
     private ViewLvaDate viewLVA;
     private ViewTODO viewTodo;
     private ViewLva viewLva;
     private ViewMetaLva viewMetaLva;
     private Image image;
     private Component lastComponent;
 
     @Autowired
     RoomFinderService roomFinderService;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
 
     @Autowired
     public BackgroundPanel(CalendarPanel calPanel, StudiesPanel studPanel, LehrangebotPanel lehrPanel, PropertiesPanel propsPanel, ViewDate viewDate, ViewLvaDate viewLVA, ViewTODO viewTodo, ViewDeadline viewDeadline) {
         this.setLayout(null);
         PanelTube.backgroundPanel=this;
         this.lehrPanel=lehrPanel;
         this.calPanel = calPanel;
         this.studPanel = studPanel;
         this.propsPanel = propsPanel;
         this.viewDate = viewDate;
         this.viewLVA=viewLVA;
         this.viewTodo=viewTodo;
         this.viewDeadline=viewDeadline;
 
         changeImage(1);
         createPropertiesButton();
         createTabButtons();
 
         log.info("Background Panel initialized.");
     }
 
     //wenn neue entity erzeugt werden soll ->  viewDate(null)
     public void viewDate(DateEntity dateEntity) {
         removeAddedPanels();
         viewDate.setDateEntity(dateEntity);
         viewDate.setVisible(true);
         this.add(viewDate);
         this.revalidate();
         this.repaint();
     }
 
     public void viewLvaDate(LvaDate lvaDate) {
         removeAddedPanels();
         viewLVA.setLVADateEntity(lvaDate);
         viewLVA.setVisible(true);
         this.add(viewLVA);
         this.revalidate();
         this.repaint();
     }
 
     public void viewTodo(Todo todo) {
         removeAddedPanels();
         viewTodo.setTodo(todo);
         viewTodo.setVisible(true);
         this.add(viewTodo);
         this.revalidate();
         this.repaint();
     }
 
     public void viewDeadline(LvaDate deadline) {
         removeAddedPanels();
         viewDeadline.setDeadline(deadline);
         viewDeadline.setVisible(true);
         this.add(viewDeadline);
         this.revalidate();
         this.repaint();
     }
 
     public void viewLva(LVA lva) {
         removeAddedPanels();
         viewLva.setLva(lva);
         viewLva.setVisible(true);
         this.add(viewLva);
         this.revalidate();
         this.repaint();
     }
 
     public void viewMetaLva(MetaLVA metalva) {
         removeAddedPanels();
         viewMetaLva.setMetaLva(metalva);
         viewMetaLva.setVisible(true);
         this.add(viewMetaLva);
         this.revalidate();
         this.repaint();
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (image != null) {
             g.drawImage(image, 0, 0, null);
         }
     }
 
     private void removeAddedPanels() {
         this.remove(propsPanel);
         this.remove(calPanel);
         this.remove(studPanel);
         this.remove(viewDate);
         this.remove(viewLVA);
         this.remove(viewTodo);
         this.remove(viewDeadline);
         this.remove(lehrPanel);
     }
 
     private void addPanel(Component c) {
         this.add(c);
         lastComponent = c;
     }
 
     public void showLastComponent() {
         this.add(lastComponent);
     }
 
     private void changeImage(int nmb) {
         try{
             removeAddedPanels();
             switch(nmb) {
                 case 1:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/cal.jpg"));
                     this.addPanel(calPanel);
                     break;
                 case 2:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/stud.jpg"));
                     this.addPanel(studPanel);
                     break;
                 case 3:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/lehr.jpg"));
                     this.addPanel(lehrPanel);
                     break;
                 default:
                     break;
             }
             this.repaint();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void createPropertiesButton() {
         properties = new JButton();
         properties.setBounds(1138,13,38,38);
         properties.setOpaque(false);
         properties.setContentAreaFilled(false);
         properties.setBorderPainted(false);
         properties.setCursor(new Cursor(Cursor.HAND_CURSOR));
         properties.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 removeAddedPanels();
                 propsPanel.setVisible(true);
                 BackgroundPanel.this.add(propsPanel);
                 BackgroundPanel.this.repaint();
             }
         });
         this.add(properties);
     }
 
     private void createTabButtons() {
         tab1 = new JButton();
         tab2 = new JButton();
         tab3 = new JButton();
 
         ArrayList<JButton> tabs = new ArrayList<JButton>();
         tabs.add(tab1);
         tabs.add(tab2);
         tabs.add(tab3);
 
         tab1.setBounds(12,50,55,159);
         tab1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(1);
                 refresh();
             }
         });
 
         tab2.setBounds(12,209,55,159);
         tab2.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(2);
                 refresh();
             }
         });
 
         tab3.setBounds(12,369,55,159);
         tab3.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(3);
                 refresh();
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
 
    public void openRoomBrowser(String room) {
         try {
             UIHelper.openURL(roomFinderService.getRoomURL(room));
         } catch (ServiceException e) {
             log.error("could not find room");
         }
     }
 
     public void refresh(){
         calPanel.refresh();
         studPanel.refresh();
         lehrPanel.refresh();
         propsPanel.refresh();
     }
 }
