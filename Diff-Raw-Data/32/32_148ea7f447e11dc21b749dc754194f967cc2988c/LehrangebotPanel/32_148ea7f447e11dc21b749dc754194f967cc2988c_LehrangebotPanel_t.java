 package at.ac.tuwien.sepm.ui;
 
 import at.ac.tuwien.sepm.ui.lehrangebot.LvaFetcherPanel;
 import at.ac.tuwien.sepm.ui.lehrangebot.LvaPanel;
 import at.ac.tuwien.sepm.ui.lehrangebot.StudienplanPanel;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 05.06.13
  * Time: 18:54
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class LehrangebotPanel extends StandardInsidePanel {
     private JButton tab1;
     private JButton tab2;
     private JButton tab3;
 
     private StudienplanPanel studienplanPanel;
     private LvaFetcherPanel lvaFetcherPanel;
     private StandardInsidePanel lvaPanel;
 
     @Autowired
     public LehrangebotPanel(StudienplanPanel studienplanPanel, LvaPanel lvaPanel, LvaFetcherPanel lvaFetcherPanel) {
         init();
         this.studienplanPanel=studienplanPanel;
         this.lvaFetcherPanel=lvaFetcherPanel;
         this.lvaPanel=lvaPanel;
         createTabButtons();
         changeImage(1);
        this.add(studienplanPanel);
         this.repaint();
         this.revalidate();
     }
 
     private void createTabButtons() {
         tab1 = new JButton();
         tab2 = new JButton();
         tab3 = new JButton();
 
         ArrayList<JButton> tabs = new ArrayList<JButton>();
         tabs.add(tab1);
         tabs.add(tab2);
         tabs.add(tab3);
 
         tab1.setBounds(41,30,160,40);
         tab1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(1);
                 remove(lvaFetcherPanel);
                 remove(lvaPanel);
                 add(studienplanPanel);
                 LehrangebotPanel.this.validate();
                 LehrangebotPanel.this.repaint();
             }
         });
 
         tab2.setBounds(41+160,30,160,40);
         tab2.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(2);
                 remove(lvaFetcherPanel);
                 remove(studienplanPanel);
                 add(lvaPanel);
                 LehrangebotPanel.this.validate();
                 LehrangebotPanel.this.repaint();
             }
         });
 
         tab3.setBounds(41+160*2,30,160,40);
         tab3.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 changeImage(3);
                 remove(studienplanPanel);
                 remove(lvaPanel);
                 add(lvaFetcherPanel);
                 LehrangebotPanel.this.validate();
                 LehrangebotPanel.this.repaint();
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
                     image = ImageIO.read(ClassLoader.getSystemResource("img/lehrs.png"));
                     break;
                 case 2:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/lehrl.png"));
                     break;
                 case 3:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/lehri.png"));
                     break;
                 default:
                     break;
             }
             this.repaint();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
