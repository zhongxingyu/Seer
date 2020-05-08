 package at.ac.tuwien.sepm.ui;
 
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
 
 /**
  * Created with IntelliJ IDEA.
  * User: Flo
  * Date: 19.06.13
  * Time: 17:43
  * To change this template use File | Settings | File Templates.
  */
 @UI
 public class SmallInfoPanel extends StandardInsidePanel {
     private JLabel infoText;
     private JButton but;
 
     private Logger log = LogManager.getLogger(this.getClass().getSimpleName());
     private static final int MAX_LETTERS = 75;
     public static int Error = 1;
     public static int Info = 2;
     public static int Warning = 3;
     public static int Success = 4;
     private String message="";
     private boolean showMore=false;
 
     public SmallInfoPanel() {
         this.setLayout(null);
         this.setOpaque(false);
         this.setBounds(330,5,540,40);
         loadFonts();
         changeImage(1);
         loadText();
         loadMoreButton();
         repaint();
         revalidate();
     }
 
     private void loadMoreButton() {
         but = new JButton();
         but.setBounds(500,0,38,38);
         but.setOpaque(false);
         but.setContentAreaFilled(false);
         but.setBorderPainted(false);
         but.setCursor(new Cursor(Cursor.HAND_CURSOR));
         changeButtonImage("img/downwards.png");
         but.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent actionEvent) {
                 if (showMore) {
                     changeButtonImage("img/downwards.png");
                     PanelTube.backgroundPanel.hideBiggerInfoText();
                     PanelTube.backgroundPanel.startInfoTextTimer();
                     showMore=false;
                 } else {
                     PanelTube.backgroundPanel.stopInfoTextTimer();
                     PanelTube.backgroundPanel.viewBiggerInfoText(message);
                     changeButtonImage("img/upwards.png");
                     showMore=true;
                 }
             }
         });
         this.add(but);
     }
 
     private void changeButtonImage(String s) {
         try {
             but.setIcon(new ImageIcon(ImageIO.read(ClassLoader.getSystemResource(s))));
         } catch (IOException e) {
             log.error(e.getMessage());
         }
     }
 
     private void loadText() {
         infoText = new JLabel("dummy");
         infoText.setForeground(new Color(198, 205, 199));
         infoText.setBounds(47,0, 843, 40);
         this.add(infoText);
     }
 
     public void setInfoText(String s, int nmb) {
         this.message = s;
         if(s.length() >= MAX_LETTERS || s.contains("(\r\n|\n)")) {
            this.add(but);
             switch(nmb) {
                 case 1:
                     infoText.setText("Fehler! Um mehr zu erfahren, druecken Sie bitte rechts.");
                     break;
                 case 2:
                     infoText.setText("Information! Um mehr zu erfahren, druecken Sie bitte rechts.");
                     break;
                 case 3:
                     infoText.setText("Warnung! Um mehr zu erfahren, druecken Sie bitte rechts.");
                     break;
                 case 4:
                     infoText.setText("Success! Um mehr zu erfahren, druecken Sie bitte rechts.");
                     break;
                 default:
                     break;
             }
         } else {
            this.remove(but);
             infoText.setText(this.message);
         }
         infoText.setFont(standardButtonFont);
         changeImage(nmb);
     }
 
     private void changeImage(int nmb) {
         try{
             switch(nmb) {
                 case 1:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/error.png"));
                     break;
                 case 2:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/info.png"));
                     break;
                 case 3:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/warning.png"));
                     break;
                 case 4:
                     image = ImageIO.read(ClassLoader.getSystemResource("img/success.png"));
                     break;
                 default:
                     break;
             }
             this.repaint();
         } catch (IOException e) {
             log.error("Error: " + e.getMessage());
         }
     }
 
     @Override
     protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         if (image != null) {
             g.drawImage(image, 0,0, null);
         }
     }
 }
