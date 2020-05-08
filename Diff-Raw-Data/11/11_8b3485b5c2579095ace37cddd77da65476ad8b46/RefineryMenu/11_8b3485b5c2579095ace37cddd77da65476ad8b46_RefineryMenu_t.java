 package view;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.border.*;
 import javax.swing.*;
 
 import static util.OilImpDesignFactory.*;
 
 import model.OilImp;
 import model.FactoryInformation;
 
 /**
  *
  * @author Paranoide
  */
 public class RefineryMenu extends OilImpMenu
 {
     
     OilImp game;
     
     String currentOilField;
     
     JPanel infoPanel;
     JPanel ressLabelPanel;
     JLabel[] ressLabels;
     JPanel currentRessPanel;
     JLabel[] currentRessLabels;
     JPanel afterwardsRessPanel;
     JLabel[] afterwardsRessLabels;
     JPanel capacityPanel;
     JLabel[] capacityLabels;
     
     
     JPanel actionPanel;
     
     public RefineryMenu(OilImp game, String startOilField)
     {
         this.game = game;
         this.currentOilField = startOilField;
         
         this.setLayout(new GridLayout(1, 2));
         
         this.infoPanel           = new JPanel(new GridLayout(1, 4));
         this.ressLabelPanel      = new JPanel(new GridLayout(5, 1));
         this.currentRessPanel    = new JPanel(new GridLayout(5, 1));
         this.afterwardsRessPanel = new JPanel(new GridLayout(5, 1));
         this.capacityPanel       = new JPanel(new GridLayout(5, 1));
         
         this.ressLabels = new JLabel[5];
         this.ressLabels[0] = new JLabel("Freie MAs");
         this.ressLabels[1] = new JLabel("Rohoel");
         this.ressLabels[2] = new JLabel("Kerosin");
         this.ressLabels[3] = new JLabel("Diesel");
         this.ressLabels[4] = new JLabel("Benzin");
         for (int t = 0; t < this.ressLabels.length; t++)
         {
            this.ressLabels[t].setFont(LABEL_FONT);
             this.ressLabelPanel.add(this.ressLabels[t]);
         }
         
         this.currentRessLabels = new JLabel[5];
         this.currentRessLabels[0] = new JLabel("100");
         this.currentRessLabels[1] = new JLabel("53343");
         this.currentRessLabels[2] = new JLabel("20000");
         this.currentRessLabels[3] = new JLabel("15000");
         this.currentRessLabels[4] = new JLabel("50232");
         for (int t = 0; t < this.currentRessLabels.length; t++)
         {
            this.currentRessLabels[t].setFont(LABEL_FONT);
             this.currentRessPanel.add(this.currentRessLabels[t]);
         }
         
         this.afterwardsRessLabels = new JLabel[5];
         this.afterwardsRessLabels[0] = new JLabel("70");
         this.afterwardsRessLabels[1] = new JLabel("33343");
         this.afterwardsRessLabels[2] = new JLabel("24000");
         this.afterwardsRessLabels[3] = new JLabel("15000");
         this.afterwardsRessLabels[4] = new JLabel("50232");
         for (int t = 0; t < this.afterwardsRessLabels.length; t++)
         {
            this.afterwardsRessLabels[t].setFont(LABEL_FONT);
             this.afterwardsRessPanel.add(this.afterwardsRessLabels[t]);
         }
         
         this.capacityLabels = new JLabel[5];
         this.capacityLabels[0] = new JLabel("70");
         this.capacityLabels[1] = new JLabel("33343");
         this.capacityLabels[2] = new JLabel("24000");
         this.capacityLabels[3] = new JLabel("15000");
         this.capacityLabels[4] = new JLabel("50232");
         for (int t = 0; t < this.capacityLabels.length; t++)
         {
            this.capacityLabels[t].setFont(LABEL_FONT);
             this.capacityPanel.add(this.capacityLabels[t]);
         }
         
         this.infoPanel.add(this.ressLabelPanel);
         this.infoPanel.add(this.currentRessPanel);
         this.infoPanel.add(this.afterwardsRessPanel);
         this.infoPanel.add(this.capacityPanel);
         
         
         
         this.actionPanel = new JPanel(new GridLayout(1, 1));
         this.actionPanel.add(new Button("OK"));
         
         
         
         this.add(this.infoPanel);
         this.add(this.actionPanel);
     }
     
     
 
     @Override
     public void reset()
     {
         
     }
 
     @Override
     public void reset(String oilField)
     {
         
     }
 
     @Override
     public void defaultAction()
     {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void setCurrentOilField(String oilField)
     {
         this.currentOilField = oilField;
     }
     
 }
