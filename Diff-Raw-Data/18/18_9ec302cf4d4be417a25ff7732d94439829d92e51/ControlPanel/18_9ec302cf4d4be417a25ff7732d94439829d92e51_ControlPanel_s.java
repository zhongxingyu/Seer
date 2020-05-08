 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  (C) Liam Byrne, 2008.
  */
 
 package gui;
 
 import images.ImageHelper;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.Field;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.Border;
 
 import towers.BasicTower;
 import towers.Tower;
 
 
 @SuppressWarnings("serial")
 public class ControlPanel extends JPanel {
    
    private static final int BASE_TOWER_PRICE = 1000;
    
    private static final DecimalFormat ONE_DP = new DecimalFormat("#0.0");
 
    private final BufferedImage backgroundImage = ImageHelper.makeImage("control_panel",
          "blue_lava.png");;
    private int level = 1;
    // These labels are in the top stats box
    private MyJLabel levelLabel, moneyLabel, livesLabel;
    // These labels are in the current tower stats box
    private MyJLabel damageLabel, rangeLabel, rateLabel, speedLabel, specialLabel;
    // These buttons are in the current tower stats box
    private VanishingButton damageButton, rangeButton, rateButton, speedButton, specialButton;
    private List<VanishingButton> towerStatsButtons = new ArrayList<VanishingButton>(5);
    private Map<VanishingButton, Tower.Attribute> buttonAttributes = new HashMap<VanishingButton,
          Tower.Attribute>(5);
    // These labels are in the level stats box
    private MyJLabel numSpritesLabel, avgHPLabel;
    private final ImageButton start = new ImageButton("start", ".png");
    private final GameMap map;
    private Tower selectedTower;
    private final Color textColour = Color.YELLOW;
    private int money = 2000;
    private int lives = 10;
    private int livesLostOnThisLevel;
 
    public ControlPanel(int width, int height, GameMap map) {
       this.map = map;
       map.setControlPanel(this);
       setPreferredSize(new Dimension(width, height));
       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       setUpJLabels();
       setUpTowerStatsButtons();
       setUpTopStatsBox();
       setUpNewTowers();
       setUpCurrentTowerStats();
       setUpLevelStats();
       setUpStartButton();
       updateMoneyLabel();
       updateLivesLabel();
    }
    
    public void endLevel() {
       money += Formulae.levelEndBonus(level);
       if(livesLostOnThisLevel == 0) {
          money += Formulae.noEnemiesThroughBonus(level);
       }
       updateMoneyLabel();
       level++;
       start.setEnabled(true);
    }
 
    @Override
    public void paintComponent(Graphics g) {
       g.drawImage(backgroundImage, 0, 0, null);
    }
    
    public void selectTower(Tower t) {
       selectedTower = t;
       setStats(t);
       enableUpgradeButtons(true);
    }
    
    public void deselectTower() {
       if(selectedTower != null) {
          selectedTower = null;
          enableUpgradeButtons(false);
       }
    }
    
    public boolean canBuildTower() {
       return money >= BASE_TOWER_PRICE;
    }
    
    public boolean buildTower() {
       money -= BASE_TOWER_PRICE;
       updateMoneyLabel();
       return canBuildTower();
    }
    
    public void incrementMoney(int earnedMoney) {
       money += earnedMoney;
       updateMoneyLabel();
    }
    
    public boolean decrementLives(int livesLost) {
       livesLostOnThisLevel += livesLost;
       lives -= livesLost;
       updateLivesLabel();
       return lives <= 0;
    }
    
    private void updateMoneyLabel() {
       moneyLabel.setText(money);
    }
    
    private void updateLivesLabel() {
       livesLabel.setText(lives);
    }
    
    private void setUpJLabels() {
       for (Field f : getClass().getDeclaredFields()) {
          if (f.getType().equals(MyJLabel.class)) {
             try {
                MyJLabel j = new MyJLabel();
                j.setText("test");
                f.set(this, j);
             } catch(IllegalAccessException e) {
                // This shouldn't ever be thrown
                System.err.println(e);
             }
          }
       }
    }
    
    private void setUpTowerStatsButtons() {
       for (Field f : getClass().getDeclaredFields()) {
          if (f.getType().equals(VanishingButton.class)) {
             try {
                String name = f.getName();
                // Sets its name to be the name of the field, without the
                // button and the first character capitalised
                String text = String.valueOf(name.charAt(0)).toUpperCase();
                text += name.substring(1, name.length() - 6);
                VanishingButton v = new VanishingButton(text, textColour);
                v.setMultiClickThreshhold(5);
                v.addActionListener(new ActionListener(){
                   public void actionPerformed(ActionEvent e) {
                      upgradeButtonPressed(e);
                   }
                });
                f.set(this, v);
                towerStatsButtons.add(v);
                buttonAttributes.put(v, Tower.Attribute.fromString(text));
             } catch(IllegalAccessException e) {
                // This shouldn't ever be thrown
                System.err.println(e);
             }
          }
       }
    }
    
    private void upgradeButtonPressed(ActionEvent e) {
       if(selectedTower != null) {
          if(!(e.getSource() instanceof VanishingButton)) {
             throw new RuntimeException("ActionEvent given doesn't have a VanishingButton as its"
                   + "source");
          }
          VanishingButton b = (VanishingButton)e.getSource();
          Tower.Attribute a = buttonAttributes.get(b);
          int currentLevel = selectedTower.getAttributeLevel(a);
         int cost = Formulae.upgradeCost(currentLevel + 1);
          if(cost <= money) {
             money -= cost;
             selectedTower.raiseAttributeLevel(a);
             setStats(selectedTower);
          }
       }
    }
    
    private void setStats(Tower t) {
       damageLabel.setText(ONE_DP.format(t.getDamage()));
       rangeLabel.setText(t.getRange());
       rateLabel.setText(t.getFireRate());
       speedLabel.setText(ONE_DP.format(t.getBulletSpeed()));
       specialLabel.setText(t.getSpecial());
    }
    
    private void enableUpgradeButtons(boolean a) {
       for(JButton b : towerStatsButtons) {
          b.setEnabled(a);
       }
    }
    
    private void startPressed() {
       if (map.start(level)) {
          updateLevelStats();
          livesLostOnThisLevel = 0;
       }
    }
    
    private void updateLevelStats() {
       levelLabel.setText("Level " + level);
       numSpritesLabel.setText(Formulae.numSprites(level));
       avgHPLabel.setText(Formulae.hp(level));
    }
 
    // -----------------------------------------------------
    // The remaining methods set up the gui bit on the side
    // -----------------------------------------------------
 
    private void setUpTopStatsBox() {
       float textSize = 15F;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));
       panel.setOpaque(false);
       panel.add(createLevelLabel(textColour));
       panel.add(createLeftRightPanel("Money", textSize, textColour, moneyLabel));
       panel.add(createLeftRightPanel("Lives", textSize, textColour, livesLabel));
 
       add(panel);
    }
 
    private JPanel createLevelLabel(Color textColour) {
       levelLabel.setForeground(textColour);
       levelLabel.setHorizontalAlignment(JLabel.CENTER);
       levelLabel.setFont(levelLabel.getFont().deriveFont(25F));
       updateLevelStats();
       return createBorderLayedOutJPanel(levelLabel, BorderLayout.CENTER);
    }
 
    private JPanel createLeftRightPanel(String text, float textSize, Color textColour,
          JLabel label) {
       JLabel leftText = new JLabel(text);
       Font font = leftText.getFont().deriveFont(textSize);
       leftText.setFont(font);
       leftText.setForeground(textColour);
       return createLeftRightPanel(leftText, textSize, textColour, label);
    }
    
    private JPanel createLeftRightPanel(Component c, float textSize, Color textColour,
          JLabel label) {
       Font font = label.getFont().deriveFont(textSize);
       JPanel panel = createBorderLayedOutJPanel();
       panel.add(c, BorderLayout.WEST);
       label.setForeground(textColour);
       label.setFont(font);
       panel.add(label, BorderLayout.EAST);
       return panel;
    }
 
    private void setUpNewTowers() {
       JPanel panel = new JPanel();
       panel.setOpaque(false);
       panel.setLayout(new GridLayout(4, 6));
       // TODO Change this to deal with the other towers when implemented
       for (int a = 0; a < 24; a++) {
          ImageButton button = new ImageButton("Tower", ".png");
          button.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                //System.out.println("Tower button pressed");
                if(canBuildTower()) {
                   map.towerButtonPressed(new BasicTower());
                }
             }
          });
          panel.add(createWrapperPanel(button, 2));
       }
 
       add(panel);
    }
 
    private void setUpCurrentTowerStats() {
       float textSize = 12F;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
       panel.setOpaque(false);
       panel.add(createLeftRightButtonPanel(damageButton, textSize, textColour, damageLabel));
       panel.add(createLeftRightButtonPanel(rangeButton, textSize, textColour, rangeLabel));
       panel.add(createLeftRightButtonPanel(rateButton, textSize, textColour, rateLabel));
       panel.add(createLeftRightButtonPanel(speedButton, textSize, textColour, speedLabel));
       panel.add(createLeftRightButtonPanel(specialButton, textSize, textColour, specialLabel));
 
       add(panel);
    }
    
    private void setUpCurrentTowerStatsButtons(JButton... buttons) {
       for(JButton b : buttons) {
          b.setOpaque(false);
          b.setForeground(textColour);
          b.setContentAreaFilled(false);
          b.setEnabled(false);
       }
    }
    
    private void setUpLevelStats() {
       float textSize = 12F;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
       panel.setOpaque(false);
       panel.add(createLeftRightPanel("Number", textSize, textColour, numSpritesLabel));
       panel.add(createLeftRightPanel("Average HP", textSize, textColour, avgHPLabel));
       
       add(panel);
    }
    
    private JPanel createLeftRightButtonPanel(JButton button, float textSize, Color textColour,
          JLabel damageLabel) {
       /*button.setOpaque(false);
       button.setForeground(textColour);
       button.setContentAreaFilled(false);
       button.setEnabled(false);*/
       return createLeftRightPanel(createWrapperPanel(button, 1), textSize, textColour,
             damageLabel);
    }
 
    private void setUpStartButton() {
       start.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             //System.out.println("Start pressed");
             startPressed();
          }
       });
       add(createWrapperPanel(start, 5));
    }
 
    private JPanel createBorderLayedOutJPanel() {
       JPanel panel = new JPanel();
       panel.setLayout(new BorderLayout());
       panel.setOpaque(false);
       return panel;
    }
 
    private JPanel createBorderLayedOutJPanel(Component comp, String pos) {
       JPanel panel = createBorderLayedOutJPanel();
       panel.add(comp, pos);
       return panel;
    }
 
    /**
     * Creates an empty border with specified width.
     */
    private Border createEmptyBorder(int width) {
       return BorderFactory.createEmptyBorder(width, width, width, width);
    }
 
    /**
     * Creates a panel that wraps this component and has a empty border around
     * it.
     */
    private JPanel createWrapperPanel(JComponent c, int borderWidth) {
       JPanel panel = new JPanel();
       panel.add(c);
       panel.setOpaque(false);
       panel.setBorder(createEmptyBorder(borderWidth));
       return panel;
    }
    
    private class MyJLabel extends JLabel {
       // This class just makes it easier to set the values of the text fields as I
       // can just pass a number instead of changing them to Strings all the time
       
       public void setText(int i) {
          setText(String.valueOf(i));
       }
       
       public void setText(double d){
          setText(String.valueOf(d));
       }      
    }
 
 }
