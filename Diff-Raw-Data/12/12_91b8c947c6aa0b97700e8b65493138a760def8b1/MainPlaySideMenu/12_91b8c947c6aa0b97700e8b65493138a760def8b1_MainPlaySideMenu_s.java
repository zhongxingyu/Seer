 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Interface.TextManage;
 
 import Interface.BuyBuilding.BuildtabPanel;
 import Interface.BuyProperty.Generator;
 import Interface.PlayInterface.PlayInterface;
 import Interface.RightsManagement.RightsInterface;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextPane;
 import talesestateappletv2.TransferContainer;
 
 /**
  *
  * @author NightFiyah
  */
 public class MainPlaySideMenu extends JPanel {
 
     public JButton Report, Deposite, Withdraw, exspand, listBuildings, addBuildings, VisualInterface, RightsManagement;
     final MainPlaySideMenu ref = this;
     int size;
     int[][] tiles;
     int[][] buildings;
     public ArrayList<String> amount1;
     public ArrayList<String> amount2;
     public int pId;
     String duchy_;
 
     public MainPlaySideMenu(final JTextPane textZone, final TransferContainer tc, int p) {
         pId = p;
         Report = new JButton("Status Report");
         Deposite = new JButton("Deposit gold");
         Withdraw = new JButton("Withdraw gold");
         exspand = new JButton("Exspand");
         listBuildings = new JButton("List Buildings");
         addBuildings = new JButton("Add Building");
         VisualInterface = new JButton("Visual Interface");
         RightsManagement = new JButton("Rights Management");
 
         ArrayList<String> retrievePlotDetails = tc.rdb.retrievePlotDetails(pId);
         tiles = tc.rdb.convertFromArray(retrievePlotDetails.get(5));
         buildings = tc.rdb.convertFromArray(retrievePlotDetails.get(6));
         size = tiles.length;
         duchy_ = retrievePlotDetails.get(3);
 
 
 
 
         setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
 
 
         c.gridx = 2;
         c.gridy = 0;
         Report.setPreferredSize(new Dimension(150, 60));
         add(Report, c);
         c.gridx = 2;
         c.gridy = 1;
         // c.insets = new Insets(30, 0, 0, 0);
         Deposite.setPreferredSize(new Dimension(150, 60));
         add(Deposite, c);
         c.gridx = 2;
         c.gridy = 2;
         //  c.insets = new Insets(30, 0, 0, 0);
         Withdraw.setPreferredSize(new Dimension(150, 60));
         add(Withdraw, c);
         c.gridx = 2;
         c.gridy = 3;
         // c.insets = new Insets(30, 0, 0, 0);
         exspand.setPreferredSize(new Dimension(150, 60));
         add(exspand, c);
         c.gridy = 4;
         //  c.insets = new Insets(30, 0, 0, 0);
         listBuildings.setPreferredSize(new Dimension(150, 60));
         add(listBuildings, c);
         c.gridy = 5;
         //  c.insets = new Insets(30, 0, 0, 0);
         addBuildings.setPreferredSize(new Dimension(150, 60));
         add(addBuildings, c);
         c.gridy = 6;
         //  c.insets = new Insets(30, 0, 0, 0);
         VisualInterface.setPreferredSize(new Dimension(150, 60));
         add(VisualInterface, c);
 
         c.gridy = 7;
         RightsManagement.setPreferredSize(new Dimension(150, 60));
         add(RightsManagement, c);
 
         Report.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 textZone.setText(tc.rdb.getStatus(pId));
             }
         });
         Report.doClick();
         Deposite.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 amount1 = tc.rdb.getCharacterAmounts(tc.CharacterName);
                 amount2 = tc.rdb.getCurrentAmount(pId);
                 double gold = Integer.parseInt(amount1.get(0)) * 10.0 + Integer.parseInt(amount1.get(1)) + (Integer.parseInt(amount1.get(2)) * 1.0 / 10);
                 String mes = "How much gold do you wish to deposit, available gold " + gold;
                 try {
                     double amountz = 0;
                     amountz = Double.parseDouble(JOptionPane.showInputDialog(mes));
                     if (amountz == 0) {
                         return;
                     }
                     if (gold >= amountz) {
 
                         int tempa = Integer.parseInt(amount2.get(0)) * 100 + Integer.parseInt(amount2.get(1)) * 10 + Integer.parseInt(amount2.get(2));
                         System.out.println(Integer.parseInt(amount2.get(0)) + " " + Integer.parseInt(amount2.get(1)) + " " + Integer.parseInt(amount2.get(2)));
                         //plots gold so it becomes less
                         // System.out.println(amountz);
                         tempa = tempa + (int) (amountz * 10);
                         int nplat = tempa / 100;
                         tempa = tempa - nplat * 100;
                         int ngold = tempa / 10;
                         tempa = tempa - ngold * 10;
                         int nsilver = tempa;
                         //plot 
 
                         tc.rdb.modifyAmount(pId, nplat, ngold, nsilver);
                         amount2 = tc.rdb.getCurrentAmount(pId);
                         System.out.println(Integer.parseInt(amount2.get(0)) + " " + Integer.parseInt(amount2.get(1)) + " " + Integer.parseInt(amount2.get(2)));
 
                         // users gold so it becomes more
                         tempa = Integer.parseInt(amount1.get(0)) * 100 + Integer.parseInt(amount1.get(1)) * 10 + Integer.parseInt(amount1.get(2));
                         System.out.println(Integer.parseInt(amount1.get(0)) + " " + Integer.parseInt(amount1.get(1)) + " " + Integer.parseInt(amount1.get(2)));
                         tempa = tempa - (int) amountz * 10;
                         nplat = tempa / 100;
                         tempa = tempa - nplat * 100;
                         ngold = tempa / 10;
                         tempa = tempa - ngold * 10;
                         nsilver = tempa;
 
                         //user 
                         tc.rdb.modifyAmount(tc.CharacterName, nplat, ngold, nsilver);
                         System.out.println(Integer.parseInt(amount1.get(0)) + " " + Integer.parseInt(amount1.get(1)) + " " + Integer.parseInt(amount1.get(2)));
                     } else {
                         JOptionPane.showMessageDialog(textZone, "You do not have enough gold");
                     }
                 } catch (Exception ex) {
                     System.out.println(ex.getMessage());
                 }
                 Report.doClick();
             }
         });
         Withdraw.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 amount1 = tc.rdb.getCharacterAmounts(tc.CharacterName);
                 amount2 = tc.rdb.getCurrentAmount(pId);
                 double gold = Integer.parseInt(amount2.get(0)) * 10.0 + Integer.parseInt(amount2.get(1)) + (Integer.parseInt(amount2.get(2)) * 1.0 / 10);
                 String mes = "How much gold do you wish to Withdraw, available gold " + gold;
                 try {
                     double amountz = 0;
                     amountz = Double.parseDouble(JOptionPane.showInputDialog(mes));
                     if (amountz == 0) {
                         return;
                     }
                     if (gold >= amountz) {
 
                         int tempa = Integer.parseInt(amount2.get(0)) * 100 + Integer.parseInt(amount2.get(1)) * 10 + Integer.parseInt(amount2.get(2));
                         System.out.println(Integer.parseInt(amount2.get(0)) + " " + Integer.parseInt(amount2.get(1)) + " " + Integer.parseInt(amount2.get(2)));
                         //plots gold so it becomes less
                         tempa = tempa - (int) (amountz * 10);
                         int nplat = tempa / 100;
                         tempa = tempa - nplat * 100;
                         int ngold = tempa / 10;
                         tempa = tempa - ngold * 10;
                         int nsilver = tempa;
                         //plot 
 
                         tc.rdb.modifyAmount(pId, nplat, ngold, nsilver);
                         amount2 = tc.rdb.getCurrentAmount(pId);
                         System.out.println(Integer.parseInt(amount2.get(0)) + " " + Integer.parseInt(amount2.get(1)) + " " + Integer.parseInt(amount2.get(2)));
 
                         // users gold so it becomes more
                         tempa = Integer.parseInt(amount1.get(0)) * 100 + Integer.parseInt(amount1.get(1)) * 10 + Integer.parseInt(amount1.get(2));
                         System.out.println(Integer.parseInt(amount1.get(0)) + " " + Integer.parseInt(amount1.get(1)) + " " + Integer.parseInt(amount1.get(2)));
                         tempa = tempa + (int) amountz * 10;
                         nplat = tempa / 100;
                         tempa = tempa - nplat * 100;
                         ngold = tempa / 10;
                         tempa = tempa - ngold * 10;
                         nsilver = tempa;
 
                         //user 
                         tc.rdb.modifyAmount(tc.CharacterName, nplat, ngold, nsilver);
                         System.out.println(Integer.parseInt(amount1.get(0)) + " " + Integer.parseInt(amount1.get(1)) + " " + Integer.parseInt(amount1.get(2)));
                     } else {
                         JOptionPane.showMessageDialog(textZone, "You do not have enough gold");
                     }
                 } catch (Exception ex) {
                     System.out.println(ex.getMessage());
                 }
                 Report.doClick();
             }
         });
 
         exspand.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 ArrayList<String> retrievePlotDetails = tc.rdb.retrievePlotDetails(pId);
 
                 String[] choices = {"Poor", "Fine", "Exquisite"};
                 String picked = (String) JOptionPane.showInputDialog(ref, "Choose what quality : ", "", JOptionPane.QUESTION_MESSAGE, null, choices, choices[0]);
 
                 ArrayList<String[]> cost;
                 if (picked == null) {
                     return;
                 }
                 cost = tc.rdb.queryPlotPrice(retrievePlotDetails.get(3), picked);
                 int stat;
 
                 stat = JOptionPane.showConfirmDialog(ref, "Will cost: Platinum:" + cost.get(0)[0] + " Gold:" + cost.get(0)[1] + " Silver:" + cost.get(0)[2], "Purchase Confirmation", JOptionPane.YES_NO_OPTION);
 
                 if (stat == 0) {
 
                     amount2 = tc.rdb.getCurrentAmount(pId);
                     //how much gold the plot has
                     int tempa = Integer.parseInt(amount2.get(0)) * 100 + Integer.parseInt(amount2.get(1)) * 10 + Integer.parseInt(amount2.get(2));
 
                     // the amoun the plot will cost
                     int costr = 100 * Integer.parseInt(cost.get(0)[0]) + 10 * Integer.parseInt(cost.get(0)[1]) + Integer.parseInt(cost.get(0)[2]);
                     System.out.println(tempa + " " + costr);
                     if (tempa >= costr) {
 
                         tempa = tempa - costr;
 
                         int nplat = tempa / 100;
                         tempa = tempa - nplat * 100;
                         int ngold = tempa / 10;
                         tempa = tempa - ngold * 10;
                         int nsilver = tempa;
 
 
 
                         Generator gen = new Generator(3);
 
                         tiles = tc.rdb.convertFromArray(retrievePlotDetails.get(5));
                         buildings = tc.rdb.convertFromArray(retrievePlotDetails.get(6));
 
                         int old = tiles.length;
                         System.out.println("SIZE BEFORE EXSPAND " + old);
                         int[][] tilesz = gen.ExspandGenerate(retrievePlotDetails.get(3), picked, size, tiles);
                         size = tilesz.length;
                         System.out.println("SIZE AFTER EXSPAND " + size);
 
                         //   if (old != size) {
                         //      buildings = gen.ArrayCopy(buildings, old, size);
                         //   }
 
                         int[][] temp = new int[buildings.length][buildings.length];
                         for (int a = 0; a < buildings.length; a++) {
                             System.arraycopy(buildings[a], 0, temp[a], 0, buildings.length);
                         }
 
                         buildings = new int[tilesz.length][tilesz.length];
                         for (int a = 0; a < temp.length; a++) {
                             System.arraycopy(temp[a], 0, buildings[a], 0, temp.length);
                         }
 
 
 
                         tc.rdb.expandPlot(pId, picked, tilesz);
 
                         //ok so now the property is exspanded in terms of acres and stuff but I still need to edit max workers and income
 
                         int workerMax;
                         int quality;
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
                         double Upkeep;
                         String pc;
                         duchy_ = retrievePlotDetails.get(3);
                         ArrayList<String> retrieveMonthlyUpkeep = tc.rdb.retrieveMonthlyUpkeep(retrievePlotDetails.get(3), picked);
                         pc = retrieveMonthlyUpkeep.get(0);
                         String gc = retrieveMonthlyUpkeep.get(1);
                         String sc = retrieveMonthlyUpkeep.get(2);
 
                        Upkeep = Double.parseDouble(retrievePlotDetails.get(8)) - (100 * Double.parseDouble(pc) + 10 * Double.parseDouble(gc) + Double.parseDouble(sc));
 
                         workerMax = workerMax + Integer.parseInt(retrievePlotDetails.get(10));
                         System.out.println(pc + " " + gc + " " + sc + " " + Upkeep);
                         // modifyPlot(int plotId, String characterName,int plotAmount, String duchyName, int sizeValue,int[][] groundArray, int[][] buildingArray, int happiness, double monthlyIncome,int workersUsed, int workerMax, double exquisiteUsed,int exquisiteMax,double fineUsed,int fineMax,double poorUsed,int poorMax
                         tc.rdb.modifyPlot(pId, tc.CharacterName, retrievePlotDetails.get(2), retrievePlotDetails.get(3), tilesz.length, tilesz, buildings, Integer.parseInt(retrievePlotDetails.get(7)), Upkeep, Integer.parseInt(retrievePlotDetails.get(9)), workerMax, Double.parseDouble(retrievePlotDetails.get(11)), Integer.parseInt(retrievePlotDetails.get(12)), Double.parseDouble(retrievePlotDetails.get(13)), Integer.parseInt(retrievePlotDetails.get(14)), Double.parseDouble(retrievePlotDetails.get(15)), Integer.parseInt(retrievePlotDetails.get(16)));
 
                         tc.rdb.modifyAmount(pId, nplat, ngold, nsilver);
 
                         Report.doClick();
 
                     } else {
                         JOptionPane.showMessageDialog(textZone, "There is not enough funds in the property");
                     }
 
 
 
 
                 }
 
             }
         });
 
         //use the property to get the info,then list all the buildins that are build on it
 
         listBuildings.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 ArrayList<String> retrieveAllBuildingsOwnedByCharacter = tc.rdb.retrieveAllBuildingsOwnedByCharacter(tc.CharacterID, pId);
                 textZone.setText("");
                 StringBuilder html = new StringBuilder();
                 html.append("<h1> <font color=\"blue\">Current Plot Buildings</font></h1>");
                 html.append("<table border=\"1\">");
                 html.append("<th>Building ID</th>");
                 html.append("<th>Building name </th>");
                 html.append("<th>Income</th>");
                 html.append("<th>Happiness</th>");
 
                 ArrayList<String[]> tempresult;
                 for (int a = 0; a < retrieveAllBuildingsOwnedByCharacter.size(); a++) {
                     tempresult = tc.rdb.retrieveBuildingDetailsById(Integer.parseInt(retrieveAllBuildingsOwnedByCharacter.get(a)));
                     html.append("	<tr>");
                     html.append(" <td>").append(retrieveAllBuildingsOwnedByCharacter.get(a)).append("</td>");
                     html.append(" <td>").append(tempresult.get(0)[1]).append("</td>");
                     html.append(" <td>").append(tempresult.get(0)[6]).append("</td>");
                     html.append(" <td>").append(tempresult.get(0)[10]).append("</td>");
                     html.append("	</tr>");
 
                 }
 
                 html.append("</table>");
 
                 textZone.setText(html.toString());
             }
         });
 
 
         final MainPlaySideMenu tr = this;
         addBuildings.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 tc.Build.init(pId, duchy_, tr);
                 tc.cardlayout.show(tc.contentpane, "Build");
             }
         });
         VisualInterface.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                tc.visual.init(tc, pId);
                 tc.cardlayout.show(tc.contentpane, "visual");
             }
         });
 
         RightsManagement.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 tc.cardlayout.show(tc.contentpane, "right");
             }
         });
 
     }
 }
