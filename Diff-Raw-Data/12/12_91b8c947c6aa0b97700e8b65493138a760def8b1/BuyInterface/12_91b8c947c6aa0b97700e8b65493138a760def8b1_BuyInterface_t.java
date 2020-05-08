 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Interface.BuyProperty;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 import talesestateappletv2.BasePanel;
 import talesestateappletv2.TransferContainer;
 
 /**
  *
  * @author NightFiyah
  */
 public class BuyInterface extends BasePanel {
 
     TransferContainer tain;
 
     public BuyInterface(String name, TransferContainer tc) {
         super(name);
         tain = tc;
         init();
     }
 
     public void init() {
 
 
         BuyMenuPicturePanel picmenu = new BuyMenuPicturePanel(tain.ad.ImageAdapter(4), tain.JFXPANEL_WIDTH_INT, tain.JFXPANEL_HEIGHT_INT);
         ((JPanel) picmenu).setPreferredSize(new Dimension(picmenu.w, picmenu.h));
         final JScrollPane PicMenuScrollPane = new JScrollPane(picmenu);
         PicMenuScrollPane.setPreferredSize(new Dimension(tain.JFXPANEL_WIDTH_INT * 3 / 4, tain.JFXPANEL_HEIGHT_INT));
         PicMenuScrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
 
 
         PicMenuScrollPane.getHorizontalScrollBar().setValue(PicMenuScrollPane.getHorizontalScrollBar().getValue() + 500);
         PicMenuScrollPane.getVerticalScrollBar().setValue(PicMenuScrollPane.getVerticalScrollBar().getMaximum() / 4);
 
         MouseListener buyPickClicked;
         buyPickClicked = new MouseListener() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 String duchy = "";
                 String abby = "";
                 int xcord = e.getX() + PicMenuScrollPane.getHorizontalScrollBar().getValue();
                 int ycord = e.getY() + PicMenuScrollPane.getVerticalScrollBar().getValue();
                 //Thegnheim
                 //Aiber
                 if ((xcord > 777 && xcord < 905) && (ycord > 343 && ycord < 395)) {
                     duchy = "Thegnheim";
                     abby = "Aiber";
                 } //Byrnholm
                 else if ((xcord > 811 && xcord < 971) && (ycord > 426 && ycord < 464)) {
                     duchy = "Thegnheim";
                     abby = "Byrnholm";
                 } //Nordafell
                 else if ((xcord > 691 && xcord < 805) && (ycord > 459 && ycord < 574)) {
                     duchy = "Thegnheim";
                     abby = "Nordafell";
                 } //Steadfield
                 else if ((xcord > 822 && xcord < 986) && (ycord > 518 && ycord < 591)) {
                     duchy = "Thegnheim";
                     abby = "Steadfield";
                 } //Meidmar
                 else if ((xcord > 987 && xcord < 1108) && (ycord > 417 && ycord < 613)) {
                     duchy = "Thegnheim";
                     abby = "Meidmar";
                 } //Langzerund
                 //kobberholm
                 else if ((xcord > 210 && xcord < 482) && (ycord > 1010 && ycord < 1116)) {
                     duchy = "Langzerund";
                     abby = "Kobberholm";
                 } //vinhime
                 else if ((xcord > 461 && xcord < 647) && (ycord > 789 && ycord < 882)) {
                     duchy = "Langzerund";
                     abby = "Vinhime";
                 } //Breister
                 else if ((xcord > 609 && xcord < 776) && (ycord > 618 && ycord < 717)) {
                     duchy = "Langzerund";
                     abby = "Breister";
                 } //Svaertstein
                 //Naring
                 else if ((xcord > 785 && xcord < 912) && (ycord > 626 && ycord < 676)) {
                     duchy = "Svaerstein";
                     abby = "Naring";
                 } //Svaertzdalr
                 else if ((xcord > 764 && xcord < 906) && (ycord > 713 && ycord < 762)) {
                     duchy = "Svaerstein";
                     abby = "Svaertzdalr";
                 } //kulletheim
                 else if ((xcord > 918 && xcord < 980) && (ycord > 703 && ycord < 820)) {
                     duchy = "Svaerstein";
                     abby = "Kulletheim";
                 } //kulletfell
                 else if ((xcord > 707 && xcord < 835) && (ycord > 838 && ycord < 912)) {
                     duchy = "Svaerstein";
                     abby = "Kulletfell";
                 } //kornheim
                 else if ((xcord > 846 && xcord < 961) && (ycord > 842 && ycord < 915)) {
                     duchy = "Svaerstein";
                     abby = "Kornheim";
                 } //Sarkland
                 //Vidarkentta
                 else if ((xcord > 1011 && xcord < 1149) && (ycord > 767 && ycord < 839)) {
                     duchy = "Sarkland";
                     abby = "Vidarkentta";
                 } //Granadalr
                 else if ((xcord > 939 && xcord < 1074) && (ycord > 654 && ycord < 692)) {
                     duchy = "Sarkland";
                     abby = "Granadalr";
                 } //Niudottir
                 else if ((xcord > 1102 && xcord < 1186) && (ycord > 654 && ycord < 742)) {
                     duchy = "Sarkland";
                     abby = "Niudottir";
                 } //Luxendalr
                 else if ((xcord > 998 && xcord < 1142) && (ycord > 868 && ycord < 912)) {
                     duchy = "Sarkland";
                     abby = "Luxendalr";
                 } //Liosto
                 else if ((xcord > 1210 && xcord < 1357) && (ycord > 888 && ycord < 967)) {
                     duchy = "Sarkland";
                     abby = "Liosto";
                 } //Ragonvaldr
                 //Sudurfell
                 else if ((xcord > 1018 && xcord < 1196) && (ycord > 1024 && ycord < 1208)) {
                     duchy = "Ragonvaldr";
                     abby = "Sudurfell";
                 } //Jarnholdt
                 else if ((xcord > 1298 && xcord < 1641) && (ycord > 1031 && ycord < 1275)) {
                     duchy = "Ragonvaldr";
                     abby = "Jarnholdt";
                 } //Dwergstein
                 else if ((xcord > 1669 && xcord < 1929) && (ycord > 1174 && ycord < 1342)) {
                     duchy = "Ragonvaldr";
                     abby = "Dwergstein";
                 } //Rotheim
                 //Sternefel
                 else if ((xcord > 672 && xcord < 905) && (ycord > 967 && ycord < 1035)) {
                     duchy = "Rotheim";
                     abby = "Sternefel";
                 } //Edeline
                 else if ((xcord > 908 && xcord < 1015) && (ycord > 1053 && ycord < 1154)) {
                     duchy = "Rotheim";
                     abby = "Edeline";
                 } //Domar
                 else if ((xcord > 658 && xcord < 870) && (ycord > 1186 && ycord < 1274)) {
                     duchy = "Rotheim";
                     abby = "Domar";
                 } //Gnisten-Elv
                 else if ((xcord > 458 && xcord < 663) && (ycord > 1099 && ycord < 1181)) {
                     duchy = "Rotheim";
                     abby = "Gnisten-Elv";
                 } //Swardtoft
                 else if ((xcord > 215 && xcord < 464) && (ycord > 1231 && ycord < 1364)) {
                     duchy = "Rotheim";
                     abby = "Swardtoft";
                 }
 
 
 
                 String[] choices = {"Poor", "Fine", "Exquisite"};
                 String picked;
 
                 // firs check avail if thers is open show this
                 if (!"".equals(abby) && !"".equals(duchy)) {
                     picked = (String) JOptionPane.showInputDialog(PicMenuScrollPane, "Choose what quality : ", "Chose " + abby + " located in " + duchy, JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
                     ArrayList<String[]> result;
                     result = tain.rdb.queryPlotPrice(duchy, picked);
                     int stat;
                     //find out how much cash the dude has
                     ArrayList<String> characterAmounts = tain.rdb.getCharacterAmounts(tain.CharacterName);
                     //user cash in silver
                     int userCash = Integer.parseInt(characterAmounts.get(0)) * 100 + Integer.parseInt(characterAmounts.get(1)) * 10 + Integer.parseInt(characterAmounts.get(2));
                     int buildingCost = Integer.parseInt(result.get(0)[0]) * 100 + Integer.parseInt(result.get(0)[1]) * 10 + Integer.parseInt(result.get(0)[2]);
                     stat = JOptionPane.showConfirmDialog(PicMenuScrollPane, "Will cost: Platinum:" + result.get(0)[0] + " Gold:" + result.get(0)[1] + " Silver:" + result.get(0)[2], "Purchase Confirmation", JOptionPane.YES_NO_OPTION);
 
 
                     if (stat == 0) {
 
                         System.out.println("CASH =" + userCash + " B COST =" + buildingCost);
                         if (userCash >= buildingCost) {
                             int workerMax = 0;
                            double Upkeep = 0;
                             System.out.println("CASH BEFORE = " + userCash);
                             System.out.println("COST " + buildingCost);
                             //update the bros cash
                             userCash = userCash - buildingCost;
                             int nplat = userCash / 100;
                             userCash = userCash - nplat * 100;
                             int ngold = userCash / 10;
                             userCash = userCash - ngold * 10;
                             int nsilver = userCash;
                             System.out.println("CASH AFTER " + (nplat) + (ngold) + nsilver);
                             tain.rdb.modifyAmount(tain.CharacterName, nplat, ngold, nsilver);
 
                             Generator gen = new Generator(3);
                             int quality;
 
                             int[][] groundArray;
 
                             switch (picked) {
                                 case "Poor":
                                     quality = 1;
                                     workerMax = 20;
                                     break;
                                 case "Fine":
                                     quality = 2;
                                     workerMax = 40;
                                     break;
                                 default:
                                     quality = 3;
                                     workerMax = 80;
                                     break;
                             }
 
                             groundArray = gen.generate(duchy, "", quality);
                             int[][] buildingArray = null;
 
                             buildingArray = new int[3][3];
 
                             for (int a = 0; a < 3; a++) {
                                 for (int b = 0; b < 3; b++) {
                                     buildingArray[a][b] = 0;
                                 }
                             }
                            Upkeep = 0 - ((Double.parseDouble(tain.rdb.retrieveMonthlyUpkeep(duchy, picked).get(0))*10) +(Double.parseDouble(tain.rdb.retrieveMonthlyUpkeep(duchy, picked).get(1))) + (Double.parseDouble(tain.rdb.retrieveMonthlyUpkeep(duchy, picked).get(2))/10));
                             tain.rdb.addPlotToCharacter(tain.CharacterName, duchy, picked, 3, groundArray, buildingArray, 0, Upkeep, 0, workerMax);
                             tain.mProp.init();
                             tain.cardlayout.show(tain.contentpane, "MainMenu");
 
                         } else {
                             JOptionPane.showMessageDialog(PicMenuScrollPane, " You do not have enough funds to purchase this plot");
                         }
                     }
                 }
             }
 
             @Override
             public void mousePressed(MouseEvent e) {
             }
 
             @Override
             public void mouseReleased(MouseEvent e) {
             }
 
             @Override
             public void mouseEntered(MouseEvent e) {
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
             }
         };
 
 
 
 
         PicMenuScrollPane.addMouseMotionListener(new MouseMotionListener() {
             int tempx = 0;
             int tempy = 0;
 
             @Override
             public void mouseDragged(MouseEvent e) {
                 PicMenuScrollPane.getHorizontalScrollBar().setValue(PicMenuScrollPane.getHorizontalScrollBar().getValue() + (tempx - e.getX()));
                 tempx = e.getX();
                 PicMenuScrollPane.getVerticalScrollBar().setValue(PicMenuScrollPane.getVerticalScrollBar().getValue() + (tempy - e.getY()));
                 tempy = e.getY();
             }
 
             @Override
             public void mouseMoved(MouseEvent e) {
                 tempx = e.getX();
                 tempy = e.getY();
             }
         });
 
         PicMenuScrollPane.addMouseListener(buyPickClicked);
         add(PicMenuScrollPane, BorderLayout.CENTER);
 
         JButton back = new JButton("Back");
         back.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 tain.cardlayout.show(tain.contentpane, "MainMenu");
             }
         });
         add(back, BorderLayout.SOUTH);
     }
 }
