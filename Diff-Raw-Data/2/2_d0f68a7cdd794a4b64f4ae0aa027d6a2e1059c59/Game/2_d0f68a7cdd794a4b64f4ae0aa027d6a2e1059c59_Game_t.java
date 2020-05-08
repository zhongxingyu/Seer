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
 
 package logic;
 
 import gui.ControlPanel;
 import gui.Drawable;
 import gui.GameMapPanel;
 import gui.SelectionScreens;
 import gui.Title;
 import gui.maps.MapParser.GameMap;
 import images.ImageHelper;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.KeyStroke;
 
 import sprites.Pacman;
 import sprites.Sprite;
 import towers.AidTower;
 import towers.BeamTower;
 import towers.BomberTower;
 import towers.Bullet;
 import towers.ChargeTower;
 import towers.CircleTower;
 import towers.FreezeTower;
 import towers.Ghost;
 import towers.HomingTower;
 import towers.JumperTower;
 import towers.LaserTower;
 import towers.MultiShotTower;
 import towers.OmnidirectionalTower;
 import towers.PoisonTower;
 import towers.ScatterTower;
 import towers.SlowLengthTower;
 import towers.Tower;
 import towers.WaveTower;
 import towers.WeakenTower;
 import towers.ZapperTower;
 import towers.Tower.Attribute;
 
 
 public class Game {
    
    public static final int WIDTH = 800;
    public static final int HEIGHT = 600;
    public static final int MAP_WIDTH = WIDTH - 200;
    public static final int MAP_HEIGHT = HEIGHT;
    public static final int CONTROLS_WIDTH = WIDTH - MAP_WIDTH;
    public static final int CONTROLS_HEIGHT = MAP_HEIGHT;
    
    // Time between each update in ms
    public static final int CLOCK_TICK = 30;
    public static final double CLOCK_TICKS_PER_SECOND = (double)1000 / CLOCK_TICK;
    
    private final boolean debugTimes;
    private final boolean debugPath;
    
    private final List<Sprite> sprites = new ArrayList<Sprite>();
    private final List<Tower> towers = Collections.synchronizedList(new ArrayList<Tower>());
    private final List<Bullet> bullets = new ArrayList<Bullet>();
    private List<Tower> towersToAdd = new ArrayList<Tower>();
    private List<Tower> towersToRemove = new ArrayList<Tower>();
    
    private Clock clock;
    
    private List<Polygon> path;
    private List<Shape> pathBounds;
    private List<Point> pathPoints;
 
    private final Container outerContainer;
    private final Title title = createTitle();
    private SelectionScreens selectionScreens;
    private ControlPanel controlPanel;
    private GameMapPanel gameMap;
    private ControlEventProcessor eventProcessor = new ControlEventProcessor();
    
    private final Map<Attribute, Integer> upgradesSoFar =
          new EnumMap<Attribute, Integer>(Attribute.class);
 
    private static final List<Comparator<Sprite>> comparators = createComparators();
    
    // Last position inside GameMapPanel, should be null otherwise
    private Point lastMousePosition;
    
    private int level;
    private boolean levelInProgress;
    private long money;
    private int nextGhostCost;
    private int lives;
    private int livesLostOnThisLevel;
    private double interestRate;
    private int endLevelUpgradesLeft;
    private static final int upgradeLives = 5;
    private static final int upgradeMoney = 1000;
    private static final double upgradeInterest = 0.01;
    
    // These should only be set during a level using their set methods. Only one should be non null
    // at any particular time
    // The currently selected tower
    private Tower selectedTower;
    // The tower that is being built
    private Tower buildingTower;
    // The tower whose button is rolled over in the control panel
    private Tower rolloverTower;
    // The tower that is being hovered over on the map
    private Tower hoverOverTower;
    
    private Sprite selectedSprite;
    private Sprite hoverOverSprite;
    
    public Game(Container c, boolean debugTimes, boolean debugPath) {
       this.debugTimes = debugTimes;
       this.debugPath = debugPath;
       outerContainer = c;
       outerContainer.setLayout(new BorderLayout());
       outerContainer.add(title);
    }
    
    public void end() {
       if(clock != null) {
          clock.end();
       }
       title.setVisible(false);
       selectionScreens.setVisible(false);
       if(gameMap != null) {
          gameMap.setVisible(false);
       }
       if(controlPanel != null) {
          controlPanel.setVisible(false);
       }
    }
    
    private void setSelectedTower(Tower t) {
       if(selectedTower != null) {
          // If there was a selected tower before deselect it
          selectedTower.select(false);
       }
       selectedTower = t;
       if(selectedTower != null) {
          // and if a tower has been selected, select it
          selectedTower.select(true);
       }
    }
    
    private void setBuildingTower(Tower t) {
       if(t != null) {
          setSelectedTower(null);
       }
       controlPanel.enableTowerStatsButtons(t == null);
       buildingTower = t;
    }
    
    private void setRolloverTower(Tower t) {
       rolloverTower = t;
    }
    
    private void setHoverOverTower(Tower t) {
       hoverOverTower = t;
    }
    
    private void setSelectedSprite(Sprite s) {
       selectedSprite = s;
    }
    
    private void setHoverOverSprite(Sprite s) {
       hoverOverSprite = s;
    }
    
    private Title createTitle() {
       return new Title(WIDTH, HEIGHT, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             outerContainer.remove(title);
             selectionScreens = createSelectionScreens();
             outerContainer.add(selectionScreens);
             outerContainer.validate();
             outerContainer.repaint();
          }
       });
    }
    
    private SelectionScreens createSelectionScreens() {
       return new SelectionScreens(WIDTH, HEIGHT, new GameStarter());
    }
    
    private GameMapPanel createGameMapPanel(GameMap g) {
       // Give null here for the background image as for jar file size
       // concerns I'm just using the one image now.
       GameMapPanel gmp = new GameMapPanel(MAP_WIDTH, MAP_HEIGHT, g, debugTimes, debugPath);
       gmp.addMouseListener(new MouseAdapter(){
          @Override
          public void mouseReleased(MouseEvent e) {
             processMouseReleased(e);
          }
       });
       gmp.addMouseMotionListener(new MouseMotionListener() {
          @Override
          public void mouseMoved(MouseEvent e) {
             lastMousePosition = e.getPoint();
          }
          @Override
          public void mouseDragged(MouseEvent e) {
             lastMousePosition = e.getPoint();
          }
       });
       return gmp;
    }
    
    private ControlPanel createControlPanel() {
       ControlPanel cp = new ControlPanel(CONTROLS_WIDTH, CONTROLS_HEIGHT,
             ImageHelper.makeImage("control_panel", "blue_lava_blurred.jpg"), eventProcessor,
             createTowerImplementations());
       cp.addMouseMotionListener(new MouseMotionListener() {
          @Override
          public void mouseMoved(MouseEvent e) {
             lastMousePosition = null;
          }
          @Override
          public void mouseDragged(MouseEvent e) {
             lastMousePosition = null;
          }
       });
       addKeyboardShortcuts(cp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), cp.getActionMap());
       return cp;
    }
    
    @SuppressWarnings("serial")
    private void addKeyboardShortcuts(InputMap inputMap, ActionMap actionMap) {
       // Sets 1+ as the keyboard shortcuts for the tower upgrades
       for(int i = 1; i <= Attribute.values().length; i++) {
          final Attribute a = Attribute.values()[i - 1];
          Character c = Character.forDigit(i, 10);
          inputMap.put(KeyStroke.getKeyStroke(c), a);
          actionMap.put(a, new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                controlPanel.clickTowerUpgradeButton(a);
             }
          });
       }
       inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0), "Change target up");
       actionMap.put("Change target up", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
             controlPanel.clickTargetButton(true);
          }
       });
       inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0), "Change target down");
       actionMap.put("Change target down", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
             controlPanel.clickTargetButton(false);
          }
       });
       inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, 0), "Speed Up");
       inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "Speed Up");
       actionMap.put("Speed Up", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
             controlPanel.clickFastButton(true);
          }
       });
       inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "Slow Down");
       actionMap.put("Slow Down", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
             controlPanel.clickFastButton(false);
          }
       });
       inputMap.put(KeyStroke.getKeyStroke('s'), "Sell");
       actionMap.put("Sell", new AbstractAction() {
          public void actionPerformed(ActionEvent e) {
             controlPanel.clickSellButton();
          }
       });
    }
    
    private List<Tower> createTowerImplementations() {
       List<Tower> towerTypes = new ArrayList<Tower>();
       towerTypes.add(new BomberTower(new Point(), null));
       towerTypes.add(new SlowLengthTower(new Point(), null));
       towerTypes.add(new FreezeTower(new Point(), null));
       towerTypes.add(new JumperTower(new Point(), null));
       towerTypes.add(new CircleTower(new Point(), null));
       towerTypes.add(new ScatterTower(new Point(), null));
       towerTypes.add(new MultiShotTower(new Point(), null));
       towerTypes.add(new LaserTower(new Point(), null));
       towerTypes.add(new PoisonTower(new Point(), null));
       towerTypes.add(new OmnidirectionalTower(new Point(), null));
       towerTypes.add(new WeakenTower(new Point(), null));
       towerTypes.add(new WaveTower(new Point(), null));
       towerTypes.add(new HomingTower(new Point(), null));
       towerTypes.add(new ChargeTower(new Point(), null));
       towerTypes.add(new ZapperTower(new Point(), null));
       towerTypes.add(new BeamTower(new Point(), null));
       towerTypes.add(new AidTower(new Point(), null));
       towerTypes.add(new Ghost(new Point()));
       return towerTypes;
    }
    
    private static List<Comparator<Sprite>> createComparators() {
       List<Comparator<Sprite>> list = new ArrayList<Comparator<Sprite>>();
       list.add(new Sprite.FirstComparator());
       list.add(new Sprite.LastComparator());
       list.add(new Sprite.FastestComparator());
       list.add(new Sprite.SlowestComparator());
       list.add(new Sprite.MostHPComparator());
       list.add(new Sprite.LeastHPComparator());
       list.add(new Sprite.DistanceComparator(null, true));
       list.add(new Sprite.DistanceComparator(null, false));
       list.add(new Sprite.RandomComparator());
       return list;
    }
    
    private long costToUpgradeAllTowers(Attribute a) {
       return costToUpgradeTowers(a, towers);
    }
    
    private long costToUpgradeTowers(Attribute a, List<Tower> towers) {
       long cost = 0;
       for(Tower t : towers) {
          cost += Formulae.upgradeCost(t.getAttributeLevel(a));
       }
       return cost;
    }
    
    private void endLevel() {
       levelInProgress = false;
       endLevelUpgradesLeft++;
       long moneyBefore = money;
       multiplyMoney(interestRate);
       long interest = money - moneyBefore;
       int levelEndBonus = Formulae.levelEndBonus(level);
       int noEnemiesThroughBonus = 0;
       StringBuilder text = new StringBuilder();
       text.append("Level ");
       text.append(level);
       text.append(" finished. ");
       text.append(interest);
      text.append(" interest earned. ");
       text.append(levelEndBonus);
       text.append(" for finishing the level.");
       if(livesLostOnThisLevel == 0) {
          noEnemiesThroughBonus = Formulae.noEnemiesThroughBonus(level);
          text.append(" ");
          text.append(noEnemiesThroughBonus);
          text.append(" bonus for losing no lives.");
       }
       increaseMoney(levelEndBonus + noEnemiesThroughBonus);
       updateAllButLevelStats();
       gameMap.displayText(text.toString());
       controlPanel.enableStartButton(true);
       // Remove all the ghosts at the end of the level
       for(Tower t : towers) {
          if(t instanceof Ghost) {
             towersToRemove.add(t);
          }
       }
       // If the level is just finished it's a good time to run the garbage
       // collector rather than have it run during a level.
       System.gc();
    }
    
    private boolean canBuildTower(Class<? extends Tower> towerType) {
       return money >= getNextTowerCost(towerType);
    }
    
    private void buildTower(Tower t) {
       // New towers get half the effect of all the bonus upgrades so far, rounded down
       for(Attribute a : upgradesSoFar.keySet()) {
          for(int i = 0; i < upgradesSoFar.get(a) / 2; i++) {
             t.raiseAttributeLevel(a, false);
          }
       }
       decreaseMoney(getNextTowerCost(t.getClass()));
       updateMoney();
    }
    
    private void increaseMoney(long amount) {
       money += amount;
       updateMoney();
    }
    
    private void decreaseMoney(long amount) {
       money -= amount;
    }
    
    private void multiplyMoney(double factor) {
       money *= factor;
    }
    
    private void setStartingStats() {
       level = 0;
       towers.clear();
       bullets.clear();
       sprites.clear();
       upgradesSoFar.clear();
       selectedTower = null;
       buildingTower = null;
       rolloverTower = null;
       hoverOverTower = null;
       money = 4000;
       nextGhostCost = Formulae.towerCost(0, 0);
       lives = 25;
       livesLostOnThisLevel = 0;
       interestRate = 1.03;
       endLevelUpgradesLeft = 0;
       updateAll();
    }
    
    private int getNextTowerCost(Class<? extends Tower> towerType) {
       if(towerType.equals(Ghost.class)) {
          return nextGhostCost;
       } else {
          int numGhosts = 0;
          for(Tower t : towers) {
             if(t instanceof Ghost) {
                numGhosts++;
             }
          }
          return Formulae.towerCost(towers.size() - numGhosts, numTowersOfType(towerType));
       }
    }
    
    private int numTowersOfType(Class<? extends Tower> towerType) {
       int num = 0;
       for(Tower t : towers) {
          if(t.getClass() == towerType) {
             num++;
          }
       }
       return num;
    }
    
    private void updateAll() {
       updateLevelStats();
       updateAllButLevelStats();
    }
    
    private void updateAllButLevelStats() {
       // Level stats are a bit slower, and only need to be done once per level
       updateEndLevelUpgradesLabel();
       updateInterestLabel();
       updateLives();
       updateMoney();
       updateTowerStats();
    }
    
    private void updateMoney() {
       controlPanel.updateMoney(money);
    }
    
    private void updateLives() {
       controlPanel.updateLives(lives);
       if(lives <= 0) {
          clock.gameOver = true;
          gameMap.signalGameOver();
       }
    }
    
    private void updateLevelStats() {
       int level = this.level;
       if(level == 0) {
          level = 1;
       }
       String levelText = "Level " + level;
       String numSprites = String.valueOf(Formulae.numSprites(level));
       long hp = Formulae.hp(level);
       String hpText = String.valueOf((long)(0.5 * hp) + " - " + hp * 2);
       String timeBetweenSprites = "0 - " + Helper.format(Formulae.
             ticksBetweenAddSprite(level) * 2 / Game.CLOCK_TICKS_PER_SECOND, 2) + "s";
       controlPanel.updateLevelStats(levelText, numSprites, hpText, timeBetweenSprites);
    }
    
    private void updateInterestLabel() {
       controlPanel.updateInterest(Helper.format(((interestRate - 1) * 100), 0) + "%");
    }
    
    private void updateEndLevelUpgradesLabel() {
      controlPanel.updateEndLevelUpgrades(endLevelUpgradesLeft);
    }
    
    private void updateTowerStats() {
       Tower t = null;
       if(rolloverTower != null || buildingTower != null) {
          // This needs to be first as rollover tower takes precedence over selected
          t = rolloverTower != null ? rolloverTower : buildingTower;
          controlPanel.updateCurrentCost(t.getName(), getNextTowerCost(t.getClass()));
          controlPanel.setCurrentInfoToTower(null);
       } else if(selectedTower != null || hoverOverTower != null) {
          t = selectedTower != null ? selectedTower : hoverOverTower;
          controlPanel.setCurrentInfoToTower(t);
       } else {
          updateSpriteInfo();
       }
       controlPanel.setStats(t);
    }
    
    private void updateSpriteInfo() {
       Sprite s = null;
       if(selectedSprite != null) {
          s = selectedSprite;
       }
       if(s == null && hoverOverSprite != null) {
          s = hoverOverSprite;
       }
       controlPanel.setCurrentInfoToSprite(s);
    }
    
    private long sellValue(Tower t) {
       return Formulae.sellValue(t, towers.size(), numTowersOfType(t.getClass()));
    }
    
    private void processMouseReleased(MouseEvent e) {
       if(clock.gameOver) {
          return;
       }
       setSelectedTower(null);
       setSelectedSprite(null);
       if(e.getButton() == MouseEvent.BUTTON3) {
          // Stop everything if it's the right mouse button
          setBuildingTower(null);
          return;
       }
       Point p = e.getPoint();
       Tower t = getTowerContaining(p);
       if(t == null) {
          if(buildingTower == null) {
             setSelectedSprite(getSpriteContaining(p));
          } else {
             tryToBuildTower(p);
          }
       } else {
          // Select a tower if one is clicked on
          setSelectedTower(t);
          setBuildingTower(null);
       }
       updateTowerStats();
    }
    
    private void updateHoverOverStuff(Point p) {
       if(p == null) {
          setHoverOverTower(null);
          setHoverOverSprite(null);
       } else if (selectedTower == null && buildingTower == null) {
          setHoverOverTower(getTowerContaining(p));
          if(hoverOverTower == null) {
             setHoverOverSprite(getSpriteContaining(p));
          }
       }
    }
    
    private void stopRunning() {
       clock.end();
       sprites.clear();
       towers.clear();
       bullets.clear();
       levelInProgress = false;
    }
    
    private Tower getTowerContaining(Point p) {
       for(Tower t : towers) {
          if(t.contains(p)) {
             return t;
          }
       }
       return null;
    }
    
    private Sprite getSpriteContaining(Point p) {
       for(Sprite s : sprites) {
          if(s.intersects(p)) {
             // intersects returns false if the sprite is dead so don't have to check that
             return s;
          }
       }
       return null;
    }
    
    private void tryToBuildTower(Point p) {
       if(buildingTower == null) {
          return;
       }
       if(isValidTowerPos(p)) {
          Tower toBuild = buildingTower.constructNew(p, pathBounds);
          if(canBuildTower(toBuild.getClass())) {
             buildTower(toBuild);
             // Have to add after telling the control panel otherwise the price will be wrong
             towersToAdd.add(toBuild);
             if(toBuild instanceof AidTower) {
                ((AidTower) toBuild).setTowers(Collections.unmodifiableList(towers));
             } else if(toBuild instanceof Ghost) {
                nextGhostCost *= 2;
             }
             // If another tower can't be built, set the building tower to null
             if(!canBuildTower(buildingTower.getClass())) {
                setBuildingTower(null);
             }
          }
       }
    }
    
    private boolean isValidTowerPos(Point p) {
       if(buildingTower == null || p == null) {
          return false;
       }
       Tower toBuild = buildingTower.constructNew(p, pathBounds);
       // Checks that the point isn't on the path
       if(!toBuild.canTowerBeBuilt(path)) {
          return false;
       }
       for(Tower t : towers) {
          // Checks that the point doesn't clash with another tower
          if(t.doesTowerClashWith(toBuild)) {
             return false;
          }
       }
       return true;
    }
    
    private class Clock extends Thread {
       
       private final int[] fastModes = new int[]{1, 2, 5};
       private int currentMode = 0;
       
       private int spritesToAdd;
       private long levelHP;
       
       private int ticksBetweenAddSprite;
       private int addSpriteIn = 0;
       
       private final int timesLength = (int)(CLOCK_TICKS_PER_SECOND / 2);
       private int timesPos = 0;
       // In each of these the last position is used to store the last time
       // and is not used for calculating the average
       private final long[] processTimes        = new long[timesLength + 1];
       private final long[] processSpritesTimes = new long[timesLength + 1];
       private final long[] processBulletsTimes = new long[timesLength + 1];
       private final long[] processTowersTimes  = new long[timesLength + 1];
       private final long[] drawTimes           = new long[timesLength + 1];
       // These are used to store the calculated average value
       private long processTime = 0;
       private long processSpritesTime = 0;
       private long processBulletsTime = 0;
       private long processTowersTime = 0;
       private long drawTime = 0;
       
       private boolean keepRunning = true;
 
       // I tried 1x, 2x, 8x, 10x, 16x, and 20x and 16x was the best (then 10x, then 8x) on my dual
       // core machine - think some callables will be more work, even though they have the same
       // number of bullets
       // Set it to zero if only one processor to signal that single threaded versions should be used
       private final int numCallables = MyExecutor.NUM_PROCESSORS == 1 ? 0 :
          MyExecutor.NUM_PROCESSORS * 16;
       
       private boolean gameOver = false;
       
       // I know I shouldn't really use a double, but it should be fine
       // This is a field so fractional amounts can be saved between ticks and so that the
       // BulletTickCallables can access it directly, rather than having to pass the amount back
       private volatile double moneyEarnt = 0;
       
       // For performance testing, goes with the code in tickBullets
 //      private long time = 0;
 //      private int ticks = 0;
       
       public Clock() {
          super("Pac Defence Clock");
 //         System.out.println("Using " + numCallables + " callables.");
          start();
       }
             
       @Override
       public void run() {
          while(keepRunning) {
             // Used nanoTime as many OS, notably windows, don't record ms times less than 10ms
             long beginTime = System.nanoTime();
             if(!gameOver) {
                doTicks();
                if(debugTimes) {
                   processTimes[timesLength] = calculateElapsedTimeMillis(beginTime);
                }
             }
             long drawingBeginTime = draw();
             gameMap.repaint();
             if(debugTimes) {
                drawTimes[timesLength] = calculateElapsedTimeMillis(drawingBeginTime);
                calculateTimesTaken();
             }
             long elapsedTime = calculateElapsedTimeMillis(beginTime);
             if(elapsedTime < CLOCK_TICK) {
                try {
                   Thread.sleep(CLOCK_TICK - elapsedTime);
                } catch(InterruptedException e) {
                   // The sleep should never be interrupted
                   e.printStackTrace();
                }
             }
          }
       }
       
       public void end() {
          keepRunning = false;
       }
       
       public void switchFastMode(boolean b) {
          // Can't just subtract one as the % operator would leave it negative
          currentMode += b ? 1 : fastModes.length - 1;
          currentMode %= fastModes.length;
       }
       
       private void doTicks() {
          int ticksToDo = fastModes[currentMode];
          for(int i = 0; i < ticksToDo; i++) {
                tick();
          }
          // Catches any new sprites that may have moved under the cursor
          // Save the mouse position from mouseMotionListeners rather than use getMousePosition as it
          // is much faster
          updateHoverOverStuff(lastMousePosition);
          updateTowerStats();
       }
       
       private void tick() {
          List<Sprite> unmodifiableSprites = Collections.unmodifiableList(sprites);
          if(debugTimes) {
             // Make sure any changes here or below are reflected in both, bar the timing bits
             long beginTime = System.nanoTime();
             tickSprites();
             // Use += so the sum of all of these in the tick is calculated
             processSpritesTimes[timesLength] += calculateElapsedTimeMillis(beginTime);
             beginTime = System.nanoTime();
             tickBullets(unmodifiableSprites);
             processBulletsTimes[timesLength] += calculateElapsedTimeMillis(beginTime);
             beginTime = System.nanoTime();
             tickTowers(unmodifiableSprites);
             processTowersTimes[timesLength] += calculateElapsedTimeMillis(beginTime);
          } else {
             tickSprites();
             tickBullets(unmodifiableSprites);
             tickTowers(unmodifiableSprites);
          }
       }
       
       private long draw() {
          long drawingBeginTime = System.nanoTime();
          drawingBeginTime -= gameMap.draw(getDrawableIterable(), processTime, processSpritesTime,
                processBulletsTime, processTowersTime, drawTime, bullets.size());
          return drawingBeginTime;
       }
       
       private Iterable<Drawable> getDrawableIterable() {
          List<Drawable> drawables = new ArrayList<Drawable>(sprites.size() + towers.size() +
                bullets.size() + 1);
          // Watch the order that things are added to the list. Things added later will be drawn 'on
          // top' of things added earlier
          drawables.addAll(sprites);
          drawables.addAll(towers);
          // Set the selected tower to be drawn last, so its range is drawn over
          // all the other towers, rather than some drawn under and some over
          if(selectedTower != null) {
             Collections.swap(drawables, drawables.indexOf(selectedTower), drawables.size() - 1);
          }
          drawables.addAll(bullets);
          // Displays the tower on the cursor that could be built
          drawables.add(new Drawable() {
             public void draw(Graphics g) {
                if(buildingTower != null && lastMousePosition != null) {
                   buildingTower.drawShadowAt(g, lastMousePosition,
                         isValidTowerPos(lastMousePosition));
                }
             }
          });
          return drawables;
       }
       
       private long calculateElapsedTimeMillis(long beginTime) {
          // Convert to ms, as beginTime is in ns
          return (System.nanoTime() - beginTime) / 1000000;
       }
       
       private void calculateTimesTaken() {
          processTime        = insertAndReturnAverage(processTimes);
          processSpritesTime = insertAndReturnAverage(processSpritesTimes);
          processBulletsTime = insertAndReturnAverage(processBulletsTimes);
          processTowersTime  = insertAndReturnAverage(processTowersTimes);
          drawTime           = insertAndReturnAverage(drawTimes);
          timesPos = (timesPos + 1) % timesLength;
       }
       
       private long insertAndReturnAverage(long[] array) {
          array[timesPos] = array[timesLength];
          array[timesLength] = 0;
          long sum = 0;
          for(int i = 0; i < timesLength; i++) {
             sum += array[i];
          }
          return sum / timesLength;
       }
       
       private void tickSprites() {
          if(levelInProgress && sprites.isEmpty() && spritesToAdd <= 0) {
             endLevel();
          }
          lookAfterAddingNewSprites();
          int livesLost = 0;
          // Count down as sprites are being removed
          for(int i = sprites.size() - 1; i >= 0; i--) {
             Sprite s = sprites.get(i);
             if(s.tick()) {
                // True if sprite has either been killed and is gone from screen or has finished
                sprites.remove(i);
                if(s.isAlive()) { // If the sprite is still alive, it means it finished
                   livesLost++;
                }
                if(selectedSprite == s) { // Deselect this sprite as it is dead/finished
                   setSelectedSprite(null);
                }
             }
          }
          controlPanel.updateNumberLeft(spritesToAdd + sprites.size());
          livesLostOnThisLevel += livesLost;
          lives -= livesLost;
          updateLives();
       }
       
       private void lookAfterAddingNewSprites() {
          if(spritesToAdd > 0) {
             if(addSpriteIn < 1) { // If the time has got to zero, add a sprite
                sprites.add(new Pacman(level, levelHP, new ArrayList<Point>(pathPoints)));
                // Adds a sprite in somewhere between 0 and twice the designated time
                addSpriteIn = (int)(Math.random() * (ticksBetweenAddSprite * 2 + 1));
                spritesToAdd--;
             } else { // Otherwise decrement the time until the next sprite will be added
                addSpriteIn--;
             }
          }
       }
       
       private void tickTowers(List<Sprite> unmodifiableSprites) {
          // Use these rather than addAll/removeAll and then clear as there's a chance a tower could
          // be added to one of the lists before they are cleared but after the addAll/removeAll
          // methods are finished, meaning it'd do nothing but still take/give you money.
          if(!towersToRemove.isEmpty()) {
             List<Tower> toRemove = towersToRemove;
             towersToRemove = new ArrayList<Tower>();
             towers.removeAll(toRemove);
          }
          if(!towersToAdd.isEmpty()) {
             List<Tower> toAdd = towersToAdd;
             towersToAdd = new ArrayList<Tower>();
             towers.addAll(toAdd);
          }
          // I tried multi-threading this but it made it slower in my limited testing
          for(Tower t : towers) {
             if(!towersToRemove.contains(t)) {
                List<Bullet> toAdd = t.tick(unmodifiableSprites, levelInProgress);
                if(toAdd == null) {
                   // Signals that a ghost is finished
                   towersToRemove.add(t);
                } else {
                   bullets.addAll(toAdd);
                }
             }
          }
       }
       
       private void tickBullets(List<Sprite> unmodifiableSprites) {
          // This and the bit at the end is performance testing - times the first x ticks
 //         if(bullets.size() > 0) {
 //            ticks++;
 //            time -= System.nanoTime();
 //         }
          if(numCallables < 2 || bullets.size() <= 1) {
             // If only one processor or 1 or fewer bullets this will be faster.
             tickBulletsSingleThread(unmodifiableSprites);
          } else {
             tickBulletsMultiThread(unmodifiableSprites);
          }
          increaseMoney((long)moneyEarnt);
          // Fractional amounts of money are kept until the next tick
          moneyEarnt -= (long)moneyEarnt;
 //         if(bullets.size() > 0) {
 //            time += System.nanoTime();
 //            if(ticks == 300) {
 //               System.out.println(time / 1000000.0);
 //            }
 //         }
       }
       
       private void tickBulletsMultiThread(List<Sprite> unmodifiableSprites) {
          int bulletsPerThread = bullets.size() / numCallables;
          int remainder = bullets.size() % numCallables;
          int firstPos, lastPos = 0;
          // Each Callable returns a List of Integer positions of Bullets to be removed
          List<Future<List<Integer>>> futures = new ArrayList<Future<List<Integer>>>();
          // No point in making more callables than there are bullets
          int n = Math.min(numCallables, bullets.size());
          for(int i = 0; i < n; i++) {
             firstPos = lastPos;
             // Add 1 to callables 1, 2, ... , (remainder - 1), remainder
             lastPos = firstPos + bulletsPerThread + (i < remainder ? 1 : 0);
             // Copying the list should reduce the lag of each thread trying to access the same list
             futures.add(MyExecutor.submit(new BulletTickCallable(firstPos, lastPos, bullets,
                   new ArrayList<Sprite>(sprites))));
          }
          processTickFutures(futures);
       }
       
       private void processTickFutures(List<Future<List<Integer>>> futures) {
          List<Integer> bulletsToRemove = null;
          for(Future<List<Integer>> f : futures) {
             try {
                if(bulletsToRemove == null) {
                   bulletsToRemove = f.get();
                } else {
                   bulletsToRemove.addAll(f.get());
                }
             } catch(InterruptedException e) {
                // Should never happen
                throw new RuntimeException(e);
             } catch(ExecutionException e) {
                // Should never happen
                throw new RuntimeException(e);
             }
          }
          // bulletsToRemove will be sorted smallest to largest as each List returned by a
          // BulletTickCallable is ordered, and the ordering is kept in the list of Futures, so no
          // need to sort it here
          
          // Remove all the completed bullets from the main list
          Helper.removeAll(bullets, bulletsToRemove);
       }
       
       private void tickBulletsSingleThread(List<Sprite> unmodifiableSprites) {
          // Run through from last to first as bullets are being removed
          for(int i = bullets.size() - 1; i >= 0 ; i--) {
             double money = bullets.get(i).tick(unmodifiableSprites);
             if(money >= 0) {
                moneyEarnt += money;
                bullets.remove(i);
             }
          }
       }
       
       private class BulletTickCallable implements Callable<List<Integer>> {
          
          private final int firstPos, lastPos;
          private final List<Bullet> bullets;
          private final List<Sprite> sprites;
          
          public BulletTickCallable(int firstPos, int lastPos, List<Bullet> bullets,
                List<Sprite> sprites) {
             this.firstPos = firstPos;
             this.lastPos = lastPos;
             this.bullets = bullets;
             this.sprites = sprites;
          }
 
          @Override
          public List<Integer> call() {
             List<Integer> toRemove = new ArrayList<Integer>();
             for(int i = firstPos; i < lastPos; i++) {
                double money = bullets.get(i).tick(sprites);
                if(money >= 0) {
                   moneyEarnt += money;
                   toRemove.add(i);
                }
             }
             return toRemove;
          }
          
       }
    }
    
    public class ControlEventProcessor {
       
       public void processStartButtonPressed() {
          if(!levelInProgress) {
             controlPanel.enableStartButton(false);
             level++;
             gameMap.removeText();
             livesLostOnThisLevel = 0;
             levelInProgress = true;
             clock.spritesToAdd = Formulae.numSprites(level);
             clock.levelHP = Formulae.hp(level);
             clock.ticksBetweenAddSprite = Formulae.ticksBetweenAddSprite(level);
             updateLevelStats();
          }
       }
       
       public void processUpgradeButtonPressed(ActionEvent e, Attribute a) {
          int numTimes = (e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK ? 5 : 1;
          Tower toAffect = towerToAffect();
          for(int i = 0; i < numTimes; i++) {
             if(toAffect == null) {
                long cost = costToUpgradeTowers(a, towers);
                if(cost <= money) {
                   decreaseMoney(cost);
                   for(Tower t : towers) {
                      t.raiseAttributeLevel(a, true);
                   }
                }
             } else {
                long cost = Formulae.upgradeCost(toAffect.getAttributeLevel(a));
                if(cost <= money) {
                   decreaseMoney(cost);
                   toAffect.raiseAttributeLevel(a, true);
                }
             }
          }
          updateTowerStats();
       }
       
       public void processUpgradeButtonChanged(JButton b, Attribute a) {
          if(checkIfRolledOver(b)) {
             String description = a.toString() + " Upgrade";
             Tower toAffect = towerToAffect();
             long cost = 0;
             if(toAffect == null) {
                description += " (all)";
                cost = costToUpgradeAllTowers(a);
             } else {
                cost = Formulae.upgradeCost(toAffect.getAttributeLevel(a));
             }
             controlPanel.updateCurrentCost(description, cost);
          } else {
             controlPanel.clearCurrentCost();
          }
       }
       
       public void processTowerButtonPressed(JButton b, Tower t) {
          if(money >= getNextTowerCost(t.getClass())) {
             setSelectedTower(null);
             setBuildingTower(t);
             updateTowerStats();
          }
       }
       
       public void processTowerButtonChangeEvent(JButton b, Tower t) {
          if(!checkIfRolledOver(b)) {
             t = null;
             if(buildingTower == null) {
                controlPanel.clearCurrentCost();
             }
          }
          setRolloverTower(t);
          updateTowerStats();
       }
       
       public void processEndLevelUpgradeButtonPress(JButton b, boolean livesUpgrade,
             boolean interestUpgrade, boolean moneyUpgrade, Attribute a) {
          if(endLevelUpgradesLeft > 0) {
             endLevelUpgradesLeft--;
             if(a != null) {
                int nextValue = upgradesSoFar.containsKey(a) ? upgradesSoFar.get(a) + 1 : 1;
                if(nextValue % 2 == 0) {
                   // It is even, i.e every second time
                   controlPanel.increaseTowersAttribute(a);
                }
                upgradesSoFar.put(a, nextValue);
                for(Tower t : towers) {
                   t.raiseAttributeLevel(a, false);
                }
             } else if(livesUpgrade) {
                lives += upgradeLives;
             } else if(interestUpgrade) {
                interestRate += upgradeInterest;
             } else if(moneyUpgrade) {
                increaseMoney(upgradeMoney);
             }
             updateAllButLevelStats();
          }
       }
       
       public void processEndLevelUpgradeButtonChanged(JButton b, boolean livesUpgrade,
             boolean interestUpgrade, boolean moneyUpgrade, Attribute a) {
          if(checkIfRolledOver(b)) {
             String description = new String();
             String cost = "Free";
             if(a != null) {
                description = a.toString() + " Upgrade (all)";
             } else if(livesUpgrade) {
                description = upgradeLives + " bonus lives";
             } else if(interestUpgrade) {
                description = "+" + Helper.format(upgradeInterest * 100, 0) + "% interest rate";
             } else if(moneyUpgrade) {
                description = upgradeMoney + " bonus money";
             }
             controlPanel.updateCurrentCost(description, cost);
          } else {
             controlPanel.clearCurrentCost();
          }
       }
       
       public void processSellButtonPressed(JButton b) {
          Tower toAffect = towerToAffect();
          if(toAffect != null) {
             increaseMoney(sellValue(toAffect));
             toAffect.sell();
             towersToRemove.add(toAffect);
             setSelectedTower(null);
          }
       }
       
       public void processSellButtonChanged(JButton b) {
          Tower toAffect = towerToAffect();
          if(toAffect != null && checkIfRolledOver(b)) {
             controlPanel.updateCurrentCost("Sell " + toAffect.getName(),
                   sellValue(toAffect));
          } else {
             controlPanel.clearCurrentCost();
          }
       }
       
       public void processTargetButtonPressed(JButton b, boolean direction) {
          String s = b.getText();
          for(int i = 0; i < comparators.size(); i++) {
             if(comparators.get(i).toString().equals(s)) {
                int nextIndex = (i + (direction ? 1 : -1)) % comparators.size();
                if(nextIndex < 0) {
                   nextIndex += comparators.size();
                }
                Comparator<Sprite> c = comparators.get(nextIndex);
                b.setText(c.toString());
                selectedTower.setSpriteComparator(c);
                return;
             }
          }
       }
       
       public void processFastButtonPressed(boolean wasLeftClick) {
          clock.switchFastMode(wasLeftClick);
       }
       
       public void processTitleButtonPressed() {
          stopRunning();
          outerContainer.remove(gameMap);
          outerContainer.remove(controlPanel);
          outerContainer.add(title);
          outerContainer.validate();
          outerContainer.repaint();
       }
       
       public void processRestartPressed() {
          stopRunning(); // This ends the clock
          gameMap.restart();
          controlPanel.restart();
          setStartingStats();
          clock = new Clock();
       }
       
       private boolean checkIfRolledOver(JButton b) {
          return b.getModel().isRollover();
       }
       
       private Tower towerToAffect() {
          return selectedTower != null ? selectedTower :
                hoverOverTower != null ? hoverOverTower : null;
       }
    }
    
    public class GameStarter {
       
       public void startGame(GameMap g) {
          pathPoints = g.getPathPoints();
          path = g.getPath();
          pathBounds = g.getPathBounds();
          pathBounds = Collections.unmodifiableList(pathBounds);
          gameMap = createGameMapPanel(g);
          controlPanel = createControlPanel();
          outerContainer.remove(selectionScreens);
          // Releases memory used by the images in the GameMaps, ~20 MB when last checked
          selectionScreens = null;
          outerContainer.add(gameMap, BorderLayout.WEST);
          outerContainer.add(controlPanel, BorderLayout.EAST);
          outerContainer.validate();
          outerContainer.repaint();
          controlPanel.requestFocus();
          setStartingStats();
          clock = new Clock();
       }
       
    }
 
 }
