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
  *  (C) Liam Byrne, 2008 - 09.
  */
 
 package gui;
 
 import images.ImageHelper;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.plaf.metal.MetalButtonUI;
 
 import logic.Game;
 import logic.Helper;
 import logic.Game.ControlEventProcessor;
 import sprites.Sprite;
 import towers.Tower;
 import towers.Tower.Attribute;
 
 
 @SuppressWarnings("serial")
 public class ControlPanel extends JPanel {
    
    private final BufferedImage backgroundImage;
    // These labels are in the top stats box
    private MyJLabel levelLabel, moneyLabel, livesLabel, interestLabel, upgradesLabel;
    private final Map<OverlayButton, Tower> towerTypes = new HashMap<OverlayButton, Tower>();
    private OverlayButton damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
          specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade;
    // These labels and the sell button below are in the tower info box
    private MyJLabel towerNameLabel, towerLevelLabel, damageDealtLabel, killsLabel;
    private MyJLabel targetLabel;
    private JButton targetButton = createTargetButton();
    private final JButton sellButton = createSellButton();
    // These are in the current tower stats box
    private final List<TowerStat> towerStats = new ArrayList<TowerStat>();
    private final Map<JButton, Attribute> buttonAttributeMap = new HashMap<JButton, Attribute>();
    // These labels are in the level stats box
    private MyJLabel numSpritesLabel, timeBetweenSpritesLabel, hpLabel;
    private MyJLabel currentCostStringLabel, currentCostLabel;
    private final ImageButton start = new ImageButton("start", ".png", true);
    private static final Color defaultTextColour = Color.YELLOW;
    private static final Color costLabelsColour = Color.GREEN;
    private static final float defaultTextSize = 12F;
 
    private final ControlEventProcessor eventProcessor;
 
    public ControlPanel(int width, int height, BufferedImage backgroundImage,
          ControlEventProcessor eventProcessor, List<Tower> towerImplementations) {
       this.backgroundImage = ImageHelper.resize(backgroundImage, width, height);
       this.eventProcessor = eventProcessor;
       setPreferredSize(new Dimension(width, height));
       // Reflective method to set up the MyJLabels
       setUpJLabels();
       // Creates each of the sub panels of this panel      
       setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
       setUpTopStatsBox();
       setUpNewTowers(towerImplementations);
       setUpEndLevelUpgrades();
       setUpCurrentTowerInfo();
       setUpCurrentTowerStats();
       setUpLevelStats();
       setUpCurrentCost();
       setUpStartButton();
       setUpBottomButtons();
    }
 
    @Override
    public void paintComponent(Graphics g) {
       g.drawImage(backgroundImage, 0, 0, null);
    }
    
    public void updateNumberLeft(int number) {
       numSpritesLabel.setText(number);
    }
    
    public void setStats(Tower t) {
       for(int i = 0; i < towerStats.size(); i++) {
          towerStats.get(i).setText(t);
       }
    }
    
    public void setCurrentInfoToTower(Tower t) {
       if(t == null) {
          blankCurrentTowerInfo();
       } else {
         towerNameLabel.setText(t.getName() + " Tower");
          towerLevelLabel.setText("Level: " + t.getExperienceLevel());
          killsLabel.setText("Kills: " + t.getKills() + " (" + t.getKillsForUpgrade() + ")");
          damageDealtLabel.setText("Dmg: " + t.getDamageDealt() + " (" +
                t.getDamageDealtForUpgrade() + ")");
          sellButton.setEnabled(true);
          Comparator<Sprite> c = t.getSpriteComparator();
          if(c != null) {
             targetLabel.setText("Target");
             targetButton.setEnabled(true);
             targetButton.setText(c.toString());
          }
       }
    }
    
    public void setCurrentInfoToSprite(Sprite s) {
       if(s == null) {
          blankCurrentTowerInfo();
       } else {
          towerNameLabel.setText("HP Left: " + Helper.format(s.getHPLeft(), 2));
          towerLevelLabel.setText(" ");
          killsLabel.setText(" ");
          damageDealtLabel.setText("Speed: " + Helper.format(s.getSpeed() *
                Game.CLOCK_TICKS_PER_SECOND, 0) + " pixels/s ");
          sellButton.setEnabled(false);
          targetLabel.setText(" ");
          targetButton.setEnabled(false);
       }
    }
    
    public void enableTowerStatsButtons(boolean enable) {
       for(int i = 0; i < towerStats.size(); i++) {
          towerStats.get(i).enableButton(enable);
       }
    }
    
    public void updateLevelStats(String level, String numSprites, String hp,
          String timeBetweenSprites) {
       levelLabel.setText(level);
       numSpritesLabel.setText(numSprites);
       hpLabel.setText(hp);
       timeBetweenSpritesLabel.setText(timeBetweenSprites);
    }
    
    public void updateCurrentCost(String description, long cost) {
       updateCurrentCost(description, String.valueOf(cost));
    }
    
    public void updateCurrentCost(String description, String cost) {
       currentCostStringLabel.setText(description);
       currentCostLabel.setText(cost);
    }
    
    public void clearCurrentCost() {
       updateCurrentCost(" ", " ");
    }
    
    public void updateEndLevelUpgrades(long upgradesLeft) {
       upgradesLabel.setText(upgradesLeft);
       enableEndLevelUpgradeButtons(upgradesLeft > 0);
    }
    
    public void updateInterest(String value) {
       interestLabel.setText(value);
    }
    
    public void updateMoney(long money) {
       moneyLabel.setText(money);
    }
    
    public void updateLives(int lives) {
       livesLabel.setText(lives);
    }
    
    public void enableStartButton(boolean enable) {
       start.setEnabled(enable);
    }
    
    public void increaseTowersAttribute(Attribute a) {
       for(Tower t : towerTypes.values()) {
          // So that the upgrades are shown when you are building a new tower
          t.raiseAttributeLevel(a, false);
       }
    }
    
    public void restart() {
       for(OverlayButton b : towerTypes.keySet()) {
          towerTypes.put(b, towerTypes.get(b).constructNew(new Point(), null));
       }
       start.setEnabled(true);
    }
    
    private void blankCurrentTowerInfo() {
       // Don't use an empty string here so as not to collapse the label
       towerNameLabel.setText(" ");
       towerLevelLabel.setText(" ");
       killsLabel.setText(" ");
       damageDealtLabel.setText(" ");
       sellButton.setEnabled(false);
       targetLabel.setText(" ");
       targetButton.setEnabled(false);
    }
    
    private void enableEndLevelUpgradeButtons(boolean enable) {
       JButton[] buttons = new JButton[]{damageUpgrade, rangeUpgrade, rateUpgrade, speedUpgrade,
                specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade};
       for(JButton b : buttons) {
          b.setEnabled(enable);
       }
    }
 
    // ---------------------------------------
    // The remaining methods set up the gui
    // ---------------------------------------
 
    private void setUpJLabels() {
       for (Field f : getClass().getDeclaredFields()) {
          if (f.getType().equals(MyJLabel.class)) {
             try {
                MyJLabel j = new MyJLabel();
                // This means it's actually drawn
                j.setText(" ");
                j.setForeground(defaultTextColour);
                j.setFontSize(defaultTextSize);
                f.set(this, j);
             } catch(IllegalAccessException e) {
                // This shouldn't ever be thrown
                System.err.println(e);
             }
          }
       }
    }
    
    private void setUpTopStatsBox() {
       float textSize = defaultTextSize + 1;
       Box box = Box.createVerticalBox();
       box.setBorder(BorderFactory.createEmptyBorder(2, 20, 0, 20));
       box.setOpaque(false);
       box.add(createLevelLabel(defaultTextColour));
       box.add(createLeftRightPanel("Money", textSize, defaultTextColour, moneyLabel));
       box.add(createLeftRightPanel("Lives", textSize, defaultTextColour, livesLabel));
       box.add(createLeftRightPanel("Interest", textSize, defaultTextColour, interestLabel));
       box.add(createLeftRightPanel("Bonuses", textSize, defaultTextColour, upgradesLabel));
       add(box);
    }
 
    private JPanel createLevelLabel(Color textColour) {
       levelLabel.setForeground(textColour);
       levelLabel.setHorizontalAlignment(JLabel.CENTER);
       levelLabel.setFontSize(25);
       return SwingHelper.createBorderLayedOutWrapperPanel(levelLabel, BorderLayout.CENTER);
    }
 
    private JPanel createLeftRightPanel(String text, float textSize, Color textColour,
          MyJLabel label) {
       MyJLabel leftText = createJLabel(text, textSize, textColour);
       return createLeftRightPanel(leftText, textSize, textColour, label);
    }
    
    private MyJLabel createJLabel(String text) {
       return createJLabel(text, defaultTextSize, defaultTextColour);
    }
    
    private MyJLabel createJLabel(String text, float textSize, Color textColour) {
       MyJLabel label = new MyJLabel(text);;
       label.setFontSize(textSize);
       label.setForeground(textColour);
       return label;
    }
    
    private JPanel createLeftRightPanel(Component c, float textSize, Color textColour,
          MyJLabel label) {
       label.setForeground(textColour);
       label.setFontSize(textSize);
       return SwingHelper.createLeftRightPanel(c, label);
    }
 
    private void setUpNewTowers(List<Tower> towers) {
       JPanel panel = new JPanel();
       panel.setOpaque(false);
       panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 4, 0));
       int numX = 6;
       int numY = 3;
       int total = numX * numY;
       GridLayout gl = new GridLayout(numY, numX);
       gl.setVgap(2);
       panel.setLayout(gl);
       assert towers.size() == total : "Number of tower implementations is different to number" +
             " of buttons.";
       for (int a = 0; a < numY * numX; a++) {
          Tower t = towers.get(a);
          OverlayButton button = OverlayButton.makeTowerButton(t.getButtonImage());
          towerTypes.put(button, t);
          button.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                JButton b = (JButton) e.getSource();
                eventProcessor.processTowerButtonPressed(b, towerTypes.get(b));
             }
          });
          button.addChangeListener(new ChangeListener(){
             public void stateChanged(ChangeEvent e) {
                JButton b = (JButton) e.getSource();
                eventProcessor.processTowerButtonChangeEvent(b, towerTypes.get(b));
             }
          });
          panel.add(button);
       }
       add(panel);
    }
    
    private Attribute getAttributeFromButton(OverlayButton b) {
       Attribute a = null;
       if(b.equals(damageUpgrade)) {
          a = Attribute.Damage;
       } else if(b.equals(rangeUpgrade)) {
          a = Attribute.Range;
       } else if(b.equals(rateUpgrade)) {
          a = Attribute.Rate;
       } else if(b.equals(speedUpgrade)) {
          a = Attribute.Speed;
       } else if(b.equals(specialUpgrade)) {
          a = Attribute.Special;
       }
       return a;
    }
    
    private void setUpEndLevelUpgrades() {
       setEndLevelUpgradeButtons();
       Box box = Box.createHorizontalBox();
       box.setOpaque(false);
       OverlayButton[] buttons = new OverlayButton[]{damageUpgrade, rangeUpgrade, rateUpgrade,
             speedUpgrade, specialUpgrade, livesUpgrade, interestUpgrade, moneyUpgrade};
       for(OverlayButton b : buttons) {
          b.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent e) {
                OverlayButton o = (OverlayButton) e.getSource();
                eventProcessor.processEndLevelUpgradeButtonPress(o, o.equals(livesUpgrade),
                      o.equals(interestUpgrade), o.equals(moneyUpgrade), getAttributeFromButton(o));
             }
          });
          b.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                OverlayButton o = (OverlayButton) e.getSource();
                eventProcessor.processEndLevelUpgradeButtonChanged(o, o.equals(livesUpgrade),
                      o.equals(interestUpgrade), o.equals(moneyUpgrade), getAttributeFromButton(o));
             }
          });
          box.add(b);
       }
       add(box);
    }
    
    private void setEndLevelUpgradeButtons() {
       damageUpgrade = OverlayButton.makeUpgradeButton("DamageUpgrade.png");
       rangeUpgrade = OverlayButton.makeUpgradeButton("RangeUpgrade.png");
       rateUpgrade = OverlayButton.makeUpgradeButton("RateUpgrade.png");
       speedUpgrade = OverlayButton.makeUpgradeButton("SpeedUpgrade.png");
       specialUpgrade = OverlayButton.makeUpgradeButton("SpecialUpgrade.png");
       livesUpgrade = OverlayButton.makeUpgradeButton("LivesUpgrade.png");
       interestUpgrade = OverlayButton.makeUpgradeButton("InterestUpgrade.png");
       moneyUpgrade = OverlayButton.makeUpgradeButton("MoneyUpgrade.png");
    }
 
    private void setUpCurrentTowerStats() {
       Box box = Box.createVerticalBox();
       box.setBorder(BorderFactory.createEmptyBorder(0, 1, 1, 5));
       box.setOpaque(false);
       for(int i = 0; i < Attribute.values().length; i++) {
          if(i != 0) {
             box.add(Box.createRigidArea(new Dimension(0, 2)));
          }
          JButton b = createTowerUpgradeButton(defaultTextColour, defaultTextSize);
          MyJLabel l = new MyJLabel();
          l.setFontSize(defaultTextSize);
          l.setForeground(defaultTextColour);
          towerStats.add(new TowerStat(b, l, Attribute.values()[i]));
          box.add(SwingHelper.createLeftRightPanel(b, l));
       }
       add(box);
    }
    
    private JButton createTowerUpgradeButton(final Color textColour, float textSize) {
       JButton b = new JButton();
       // Hack to set the disabled text colour. If you change this UI do the same
       // for the target button so they're consistent.
       b.setUI(new MetalButtonUI(){
          @Override
          public Color getDisabledTextColor() {
             return textColour;
          }
       });
       b.setFont(b.getFont().deriveFont(textSize));
       b.setForeground(textColour);
       b.setOpaque(false);
       b.setContentAreaFilled(false);
       // Hack to make the buttons slightly smaller
       b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(),
             BorderFactory.createEmptyBorder(-2, -5, -2, -5)));
       b.addActionListener(new ActionListener(){
          public void actionPerformed(ActionEvent e) {
             JButton b = (JButton)e.getSource();
             eventProcessor.processUpgradeButtonPressed(e, buttonAttributeMap.get(b));
          }
       });
       b.addChangeListener(new ChangeListener(){
          public void stateChanged(ChangeEvent e) {
             JButton b = (JButton)e.getSource();
             eventProcessor.processUpgradeButtonChanged(b, buttonAttributeMap.get(b));
          }
       });
       return b;
    }
    
    private void setUpCurrentTowerInfo() {
       MyJLabel[] labels = new MyJLabel[]{towerNameLabel, towerLevelLabel, killsLabel,
             damageDealtLabel};
       float textSize = defaultTextSize - 1;
       for(MyJLabel a : labels) {
          a.setFontSize(textSize);
          a.setHorizontalAlignment(JLabel.CENTER);
       }
       towerNameLabel.setFontSize(textSize + 1);
       Box box = Box.createVerticalBox();
       box.add(SwingHelper.createWrapperPanel(towerNameLabel));
       Box centralRow = Box.createHorizontalBox();
       centralRow.add(towerLevelLabel);
       centralRow.add(sellButton);
       centralRow.add(killsLabel);
       box.add(centralRow);
       box.add(SwingHelper.createWrapperPanel(damageDealtLabel));
       targetLabel.setFontSize(textSize);
       targetButton.setFont(targetButton.getFont().deriveFont(textSize));
       JPanel p = SwingHelper.createLeftRightPanel(targetLabel, targetButton);
       p.setBorder(BorderFactory.createEmptyBorder(0, 40, 3, 40));
       box.add(p);
       add(box);
    }
    
    private JButton createTargetButton() {
       JButton b = new JButton(){
          @Override
          public void setEnabled(boolean b) {
             super.setEnabled(b);
             // If the border isn't painted and there is no text it's effectively invisible
             setBorderPainted(b);
             if(!b) {
                setText(" ");
             }
          }
       };
       // Changing this should result in changing the tower upgrade buttons for consistency
       b.setUI(new MetalButtonUI());
       b.setForeground(defaultTextColour);
       b.setOpaque(false);
       b.setContentAreaFilled(false);
       b.setFocusPainted(false);
       // Hack to make the button smaller
       b.setBorder(BorderFactory.createCompoundBorder(b.getBorder(),
             BorderFactory.createEmptyBorder(-10, -5, -10, -5)));
       b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             eventProcessor.processTargetButtonPressed((JButton)e.getSource());
          }
       });
       return b;
    }
    
    private void setUpLevelStats() {
       float textSize = defaultTextSize;
       Box box = Box.createVerticalBox();
       box.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
       box.setOpaque(false);
       box.add(createLeftRightPanel("Number Left", textSize, defaultTextColour, numSpritesLabel));
       box.add(createLeftRightPanel("Between Sprites", textSize, defaultTextColour,
             timeBetweenSpritesLabel));
       box.add(createLeftRightPanel("HP", textSize, defaultTextColour, hpLabel));      
       add(box);
    }
 
    private void setUpStartButton() {
       start.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             eventProcessor.processStartButtonPressed();
          }
       });
       start.setMnemonic(KeyEvent.VK_S);
       add(SwingHelper.createWrapperPanel(start));
    }
    
    private void setUpCurrentCost() {
       float textSize = defaultTextSize - 1;
       currentCostStringLabel.setFontSize(textSize);
       currentCostLabel.setFontSize(textSize);
       currentCostStringLabel.setForeground(costLabelsColour);
       currentCostLabel.setForeground(costLabelsColour);
       JPanel panel = SwingHelper.createBorderLayedOutJPanel();
       panel.setBorder(BorderFactory.createEmptyBorder(2, 5, 0, 5));
       panel.add(currentCostStringLabel, BorderLayout.WEST);
       panel.add(currentCostLabel, BorderLayout.EAST);
       add(panel);
    }
    
    private JButton createSellButton() {
       JButton b = new OverlayButton("buttons", "sell.png");
       // So when disabled it is blank but keeps its size
       b.setDisabledIcon(new ImageIcon());
       b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             eventProcessor.processSellButtonPressed((JButton) e.getSource());
          }
       });
       b.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
             eventProcessor.processSellButtonChanged((JButton) e.getSource());
          }
       });
       return b;
    }
    
    private void setUpBottomButtons() {
       JPanel panel = SwingHelper.createBorderLayedOutJPanel();
       JButton title = new OverlayButton("buttons", "title.png");
       title.setMnemonic(KeyEvent.VK_T);
       title.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             eventProcessor.processTitleButtonPressed();
          }
       });
       JButton restart = new OverlayButton("buttons", "restart.png");
       restart.setMnemonic(KeyEvent.VK_R);
       restart.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             eventProcessor.processRestartPressed();
          }
       });
       panel.add(title, BorderLayout.WEST);
       panel.add(restart, BorderLayout.EAST);
       panel.setBorder(BorderFactory.createEmptyBorder(0, 13, 1, 13));
       add(panel);
    }
 
    private class TowerStat {
       
       private final JButton button;
       private final JLabel label;
       private final Attribute attrib;
       
       private TowerStat(JButton b, JLabel l, Attribute a) {
          if(buttonAttributeMap.put(b, a) != null) {
             throw new IllegalArgumentException("This button has already been used.");
          }
          button = b;
          label = l;
          attrib = a;
       }
       
       private void setText(Tower t) {
          if(t == null) {
             button.setText(attrib.toString());
             label.setText(" ");
          } else {
             button.setText(t.getStatName(attrib));
             label.setText(t.getStat(attrib));
          }
       }
       
       private void enableButton(boolean b) {
          button.setEnabled(b);
       }
    }
    
 }
