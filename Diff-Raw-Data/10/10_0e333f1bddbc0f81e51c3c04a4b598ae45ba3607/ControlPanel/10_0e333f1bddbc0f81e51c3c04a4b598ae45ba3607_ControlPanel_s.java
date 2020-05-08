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
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import towers.BasicTower;
 import towers.Tower;
 
 
 @SuppressWarnings("serial")
 public class ControlPanel extends JPanel {
    
    private static final int BASE_TOWER_PRICE = 1000;
    
    private static final DecimalFormat ONE_DP = new DecimalFormat("#0.0");
    private static final DecimalFormat ZERO_DP = new DecimalFormat("#0");
 
    private final BufferedImage backgroundImage = ImageHelper.makeImage("control_panel",
          "blue_lava.jpg");;
    private int level = 1;
    // These labels are in the top stats box
    private MyJLabel levelLabel, moneyLabel, livesLabel, interestLabel;
    // These labels are in the current tower stats box
    private MyJLabel damageLabel, rangeLabel, rateLabel, speedLabel, specialLabel;
    private MyJLabel damageDealtLabel, killsLabel;
    // These buttons are in the current tower stats box
    private TowerUpgradeButton damageButton, rangeButton, rateButton, speedButton, specialButton;
    private List<TowerUpgradeButton> towerStatsButtons = new ArrayList<TowerUpgradeButton>(5);
    private Map<TowerUpgradeButton, Tower.Attribute> buttonAttributes =
          new HashMap<TowerUpgradeButton, Tower.Attribute>(5);
    // These labels are in the level stats box
    private MyJLabel numSpritesLabel, avgHPLabel;
    private MyJLabel currentCostLabel;
    private final ImageButton start = new ImageButton("start", ".png");
    private final GameMap map;
    private Tower selectedTower;
    private final Color textColour = Color.YELLOW;
    private final float defaultTextSize = 12F;
    private int money = 2000;
    private int lives = 10;
    private int livesLostOnThisLevel;
    private double interestRate = 1.01;
 
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
       setUpCurrentCost();
       setUpStartButton();
       updateMoneyLabel();
       updateLivesLabel();
    }
    
    public void endLevel() {
       money *= interestRate;
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
       updateStats();
       updateDamageAndKillsLabels();
       enableUpgradeButtons(true);
    }
    
    public void deselectTower() {
       if(selectedTower != null) {
          updateDamageAndKillsLabels();
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
       // If a tower is selected, more money earnt means it could've done more damage
       updateDamageAndKillsLabels();
       // And this may have caused its stats to be upgraded
       updateStats();
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
    
    private void updateInterestLabel() {
       interestLabel.setText(ONE_DP.format(((interestRate - 1) * 100)) + "%");
    }
    
    private void updateCurrentCostLabel(int cost) {
       currentCostLabel.setText(cost);
    }
    
    private void updateStats() {
       if(selectedTower != null) {
          setStats(selectedTower);
       }
    }
    
    private void updateDamageAndKillsLabels() {
       if(selectedTower == null) {
          killsLabel.setText(" ");
          damageDealtLabel.setText(" ");
       } else {
          killsLabel.setText("Kills: " + selectedTower.getKills() + " (" +
                selectedTower.getKillsForUpgrade() + ")");
          damageDealtLabel.setText("Dmg: " + ZERO_DP.format(selectedTower.getDamageDealt())
                + " (" + selectedTower.getDamageDealtForUpgrade() + ")");
       }
    }
    
    private void setUpJLabels() {
       for (Field f : getClass().getDeclaredFields()) {
          if (f.getType().equals(MyJLabel.class)) {
             try {
                MyJLabel j = new MyJLabel();
                // This means it's actually drawn
                j.setText(" ");
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
          if (f.getType().equals(TowerUpgradeButton.class)) {
             try {
                String name = f.getName();
                // Sets its name to be the name of the field, without the
                // button and the first character capitalised
                String text = String.valueOf(name.charAt(0)).toUpperCase();
                text += name.substring(1, name.length() - 6);
                TowerUpgradeButton v = new TowerUpgradeButton(text, textColour);
                v.setMultiClickThreshhold(5);
                v.addActionListener(new ActionListener(){
                   public void actionPerformed(ActionEvent e) {
                      upgradeButtonPressed(e);
                   }
                });
                v.addChangeListener(new ChangeListener(){
                   public void stateChanged(ChangeEvent e) {
                      upgradeButtonChanged(e);
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
          if(!(e.getSource() instanceof TowerUpgradeButton)) {
             throw new RuntimeException("ActionEvent given doesn't have a TowerStatsButton as its"
                   + "source");
          }
          TowerUpgradeButton b = (TowerUpgradeButton)e.getSource();
          Tower.Attribute a = buttonAttributes.get(b);
          int currentLevel = selectedTower.getAttributeLevel(a);
          int cost = Formulae.upgradeCost(currentLevel);
          if(cost <= money) {
             money -= cost;
             selectedTower.raiseAttributeLevel(a);
             setStats(selectedTower);
          }
       }
    }
    
    private void upgradeButtonChanged(ChangeEvent e) {
       if(selectedTower != null) {
          //System.out.println("Button changed");
          if(!(e.getSource() instanceof TowerUpgradeButton)) {
             throw new RuntimeException("ActionEvent given doesn't have a TowerStatsButton as its"
                   + "source");
          }
          TowerUpgradeButton b = (TowerUpgradeButton)e.getSource();
          Tower.Attribute a = buttonAttributes.get(b);
          int currentLevel = selectedTower.getAttributeLevel(a);
          int cost = Formulae.upgradeCost(currentLevel);
          updateCurrentCostLabel(cost);
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
       float textSize = defaultTextSize + 3;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));
       panel.setOpaque(false);
       panel.add(createLevelLabel(textColour));
       panel.add(createLeftRightPanel("Money", textSize, textColour, moneyLabel));
       panel.add(createLeftRightPanel("Lives", textSize, textColour, livesLabel));
       panel.add(createLeftRightPanel("Interest", textSize, textColour, interestLabel));
       updateInterestLabel();
 
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
       int height = 4;
       int width = 6;
       panel.setLayout(new GridLayout(height, width));
       // TODO Change this to deal with the other towers when implemented
       for (int a = 0; a < height * width; a++) {
          ImageButton button = new ImageButton("Tower", ".png");
          button.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                //System.out.println("Tower button pressed");
                if(canBuildTower()) {
                   if(map.towerButtonPressed(new BasicTower())) {
                      updateCurrentCostLabel(BASE_TOWER_PRICE);
                   }
                }
             }
          });
          button.addChangeListener(new ChangeListener(){
             public void stateChanged(ChangeEvent e) {
                updateCurrentCostLabel(BASE_TOWER_PRICE);
             }
          });
          panel.add(createWrapperPanel(button, 1));
       }
 
       add(panel);
    }
 
    private void setUpCurrentTowerStats() {
       setUpDamageAndKillsLabels();
       float textSize = defaultTextSize;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(1, 20, 1, 20));
       panel.setOpaque(false);
       panel.add(createLeftRightButtonPanel(damageButton, textSize, textColour, damageLabel));
       panel.add(createLeftRightButtonPanel(rangeButton, textSize, textColour, rangeLabel));
       panel.add(createLeftRightButtonPanel(rateButton, textSize, textColour, rateLabel));
       panel.add(createLeftRightButtonPanel(speedButton, textSize, textColour, speedLabel));
       panel.add(createLeftRightButtonPanel(specialButton, textSize, textColour, specialLabel));
 
       add(panel);
    }
    
    private void setUpDamageAndKillsLabels() {
       MyJLabel[] labels = new MyJLabel[]{killsLabel, damageDealtLabel};
       Font f = killsLabel.getFont().deriveFont(defaultTextSize - 1.5f);
       for(MyJLabel a : labels) {
          a.setForeground(textColour);
          a.setFont(f);
          a.setHorizontalAlignment(JLabel.CENTER);
       }
       JPanel panel = createBorderLayedOutJPanel();
       panel.add(createWrapperPanel(killsLabel), BorderLayout.WEST);
       panel.add(createWrapperPanel(damageDealtLabel), BorderLayout.EAST);
       add(panel);
    }
    
    private void setUpLevelStats() {
       float textSize = defaultTextSize;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(5, 30, 5, 30));
       panel.setOpaque(false);
       panel.add(createLeftRightPanel("Number", textSize, textColour, numSpritesLabel));
       panel.add(createLeftRightPanel("Average HP", textSize, textColour, avgHPLabel));
       
       add(panel);
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
    
    private void setUpCurrentCost() {
       float textSize = defaultTextSize;
       JPanel panel = new JPanel();
       panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
       panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 20));
       panel.setOpaque(false);
       panel.add(createLeftRightPanel("Cost", textSize, textColour, currentCostLabel));
       
       add(panel);
    }
    
    private JPanel createLeftRightButtonPanel(JButton button, float textSize, Color textColour,
          JLabel damageLabel) {
       return createLeftRightPanel(createWrapperPanel(button), textSize, textColour,
             damageLabel);
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
    
    private JPanel createWrapperPanel(JComponent c) {
       JPanel panel = new JPanel();
       panel.add(c);
       panel.setOpaque(false);
       return panel;
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
