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
 
 package logic;
 
 import gui.Circle;
 import gui.ControlPanel;
 import gui.GameMapPanel;
 import gui.SelectionScreens;
 import gui.Title;
 import gui.GameMapPanel.GameMap;
 import images.ImageHelper;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.LockSupport;
 
 import javax.swing.JButton;
 
 import sprites.Pacman;
 import sprites.Sprite;
 import sprites.Sprite.FirstComparator;
 import towers.AidTower;
 import towers.BeamTower;
 import towers.BomberTower;
 import towers.Bullet;
 import towers.ChargeTower;
 import towers.CircleTower;
 import towers.FreezeTower;
 import towers.HomingTower;
 import towers.JumpingTower;
 import towers.LaserTower;
 import towers.MultiShotTower;
 import towers.OmnidirectionalTower;
 import towers.PiercerTower;
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
    
    private final boolean debugTimes = true;
    private final boolean debugPath = false;
    
    private final List<Sprite> sprites = Collections.synchronizedList(new ArrayList<Sprite>());
    private final List<Tower> towers = Collections.synchronizedList(new ArrayList<Tower>());
    private final List<Bullet> bullets = Collections.synchronizedList(new ArrayList<Bullet>());
    
    private Clock clock;
    
    private Polygon path;
    private List<Point> pathPoints;
 
    private final Container outerContainer;
    private final Title title = createTitle();
    private final SelectionScreens selectionScreens = createSelectionScreens();
    private ControlPanel controlPanel;
    private GameMapPanel gameMap;
    
    private final Map<Attribute, Integer> upgradesSoFar =
          new EnumMap<Attribute, Integer>(Attribute.class);
 
    private List<Comparator<Sprite>> comparators = createComparators();
    
    private int level;
    private boolean levelInProgress;
    private long money;
    private int lives;
    private int livesLostOnThisLevel;
    private double interestRate;
    private int endLevelUpgradesLeft;
    private static final int upgradeLives = 5;
    private static final int upgradeMoney = 1000;
    private static final double upgradeInterest = 0.005;
    // These should only be set during a level using their set methods. Only one should
    // be non null at any particular time
    // The currently selected tower
    private Tower selectedTower;
    // The tower that is being built
    private Tower buildingTower;
    // The tower whose button is rolled over in the control panel
    private Tower rolloverTower;
    // The tower that is being hovered over on the map
    private Tower hoverOverTower;
    
    public Game(Container c) {
       outerContainer = c;
       outerContainer.setLayout(new BorderLayout());
       outerContainer.add(title);
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
    
    private Title createTitle() {
       return new Title(WIDTH, HEIGHT, new ActionListener() {
          public void actionPerformed(ActionEvent e) {
             outerContainer.remove(title);
             outerContainer.add(selectionScreens);
             outerContainer.validate();
             outerContainer.repaint();
          }
       });
    }
    
    private SelectionScreens createSelectionScreens() {
       return new SelectionScreens(WIDTH, HEIGHT, new ContinueOn());
    }
    
    private GameMapPanel createGameMapPanel(GameMap g) {
       GameMapPanel gmp = new GameMapPanel(MAP_WIDTH, MAP_HEIGHT, ImageHelper.makeImage("maps",
             "rainbowColours.jpg"), g, debugTimes, debugPath);
       gmp.addMouseListener(new MouseAdapter(){
          @Override
          public void mouseReleased(MouseEvent e) {
             processMouseReleased(e);
          }
       });
       gmp.addMouseMotionListener(new MouseMotionAdapter(){
          @Override
          public void mouseMoved(MouseEvent e) {
             processMouseMoved(e);
          }
       });
       return gmp;
    }
    
    private List<Tower> createTowerImplementations() {
       List<Tower> towerTypes = new ArrayList<Tower>();
       towerTypes.add(new BomberTower(new Point(), null));
       towerTypes.add(new PiercerTower(new Point(), null));
       towerTypes.add(new SlowLengthTower(new Point(), null));
       towerTypes.add(new FreezeTower(new Point(), null));
       towerTypes.add(new JumpingTower(new Point(), null));
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
       return towerTypes;
    }
    
    private List<Comparator<Sprite>> createComparators() {
       List<Comparator<Sprite>> list = new ArrayList<Comparator<Sprite>>();
       list.add(new Sprite.FirstComparator());
       list.add(new Sprite.LastComparator());
       list.add(new Sprite.FastestComparator());
       list.add(new Sprite.SlowestComparator());
       list.add(new Sprite.MostHPComparator());
       list.add(new Sprite.LeastHPComparator());
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
       text.append(" interest earnt. ");
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
       // If a tower is selected, more money earnt means it could've done more
       // damage and this may have caused its stats to be upgraded
       updateTowerStats();
    }
    
    private void decreaseMoney(long amount) {
       money -= amount;
    }
    
    private void multiplyMoney(double factor) {
       money *= factor;
    }
    
    private boolean decrementLives(int livesLost) {
       livesLostOnThisLevel += livesLost;
       lives -= livesLost;
       updateLives();
       return lives <= 0;
    }
    
    private void setStartingStats() {
       level = 0;
       upgradesSoFar.clear();
       selectedTower = null;
       buildingTower = null;
       rolloverTower = null;
       hoverOverTower = null;
       money = 4000;
       lives = 25;
       livesLostOnThisLevel = 0;
       interestRate = 1.03;
       endLevelUpgradesLeft = 0;
       updateAll();
    }
    
    private int getNextTowerCost(Class<? extends Tower> towerType) {
       return Formulae.towerCost(towers.size(), numTowersOfType(towerType));
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
       controlPanel.updateInterest(Helper.format(((interestRate - 1) * 100), 2) + "%");
    }
    
    private void updateEndLevelUpgradesLabel() {
      controlPanel.updateEndLevelUpgrades(endLevelUpgradesLeft);
    }
    
    private void updateTowerStats() {
       Tower t = null;
       if(selectedTower != null) {
          t = selectedTower;
       } else if(buildingTower != null) {
          t = buildingTower;
          controlPanel.updateCurrentCost(buildingTower.getName() + " Tower",
                getNextTowerCost(t.getClass()));
       } else if(hoverOverTower != null) {
          t = hoverOverTower;
       } else if(rolloverTower != null) {
          t = rolloverTower;
          controlPanel.updateCurrentCost(rolloverTower.getName() + " Tower",
                getNextTowerCost(t.getClass()));
       }
       controlPanel.setStats(t);
       updateCurrentTowerInfo();
    }
    
    private void updateCurrentTowerInfo() {
       Tower t = null;
       if(selectedTower != null) {
          t = selectedTower;
       } else if(hoverOverTower != null) {
          t = hoverOverTower;
       }
       controlPanel.setCurrentTowerInfo(t);
    }
    
    private long sellValue(Tower t) {
       return Formulae.sellValue(t, towers.size(), numTowersOfType(t.getClass()));
    }
    
    private void processMouseReleased(MouseEvent e) {
       setSelectedTower(null);
       if(e.getButton() == MouseEvent.BUTTON3) {
          // Stop everything if it's the right mouse button
          setBuildingTower(null);
          return;
       }
       Point p = e.getPoint();
       Tower t = getTowerContaining(p);
       if(t == null) {
          tryToBuildTower(p);
       } else {
          // Select a tower if one is clicked on
          setSelectedTower(t);
          setBuildingTower(null);
       }    
    }
    
    private void processMouseMoved(MouseEvent e) {
       if(selectedTower == null && buildingTower == null) {
          Tower t = getTowerContaining(e.getPoint());
          setHoverOverTower(t);
       }
    }
    
    private void removeTower(Tower t) {
       if(!towers.remove(t)) {
          throw new RuntimeException("Tower that wasn't on screen is being removed.");
       }
       if(t == selectedTower) {
          setSelectedTower(null);
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
    
    private void tryToBuildTower(Point p) {
       if(buildingTower == null) {
          return;
       }
       Tower toBuild = buildingTower.constructNew(p, path.getBounds2D());
       for(Tower t : towers) {
          // Checks that the point doesn't clash with another tower
          if(t.doesTowerClashWith(toBuild)) {
             return;
          }
       }
       // Checks that the point isn't on the path
       Shape bounds = toBuild.getBounds();
       if(bounds instanceof Circle) {
          if(((Circle) bounds).intersects(path)) {
             return;
          }
       } else {
          if(path.intersects(bounds.getBounds())) {
             return;
          }
       }
       if(canBuildTower(toBuild.getClass())) {
          buildTower(toBuild);
          // Have to add after telling the control panel otherwise
          // the price will be wrong
          towers.add(toBuild);
          if(toBuild instanceof AidTower) {
             ((AidTower) toBuild).setTowers(Collections.unmodifiableList(towers));
          }
          if(!canBuildTower(buildingTower.getClass())) {
             setBuildingTower(null);
          }
       }
    }
    
    private class Clock extends Thread {
       
       private int spritesToAdd;
       private long levelHP;
       
       private int ticksBetweenAddSprite;
       private int addSpriteIn = 0;
       private final int timesLength = (int)(CLOCK_TICKS_PER_SECOND / 2);
       private int timesPos = 0;
       // In each of these the last position is used to store the last time
       // and is not used for calculating the average
       private final long[] processTimes = new long[timesLength + 1];
       private final long[] processSpritesTimes = new long[timesLength + 1];
       private final long[] processBulletsTimes = new long[timesLength + 1];
       private final long[] processTowersTimes = new long[timesLength + 1];
       private final long[] drawTimes = new long[timesLength + 1];
       private boolean keepRunning = true;
       private boolean isWaiting;
       private final int numThreads;
       private final BulletTickThread[] bulletTickThreads;
       private final boolean[] isThreadRunning;
       private final List<Integer> toRemove = new ArrayList<Integer>();
       private double moneyEarnt = 0;
       
       private long processTime = 0;
       private long processSpritesTime = 0;
       private long processBulletsTime = 0;
       private long processTowersTime = 0;
       private long drawTime = 0;
       
       private boolean gameOver = false;
       
       public Clock() {
          super("Pac Defence Clock");
          numThreads = Runtime.getRuntime().availableProcessors();
          System.out.println(numThreads + " processor(s) detected.");
          if(numThreads > 1) {
             bulletTickThreads = new BulletTickThread[numThreads];
             isThreadRunning = new boolean[numThreads];
             for(int i = 0; i < numThreads; i++) {
                bulletTickThreads[i] = new BulletTickThread(i);
                bulletTickThreads[i].start();
                isThreadRunning[i] = false;
             }
          } else {
             // If there is only one core, use the original single threaded version.
             bulletTickThreads = null;
             isThreadRunning = null;
          }
          start();
       }
             
       @Override
       public void run() {
          while(keepRunning) {
             // Used nanoTime as many OS, notably windows, don't record ms times less than 10ms
             long beginTime = System.nanoTime();
             if(!gameOver) {
                tick();
                if(debugTimes) {
                   calculateTimesTaken();
                   processTimes[timesLength] = calculateElapsedTime(beginTime);
                }
             }
             long drawingBeginTime = draw();
             if(debugTimes) {
                drawTimes[timesLength] = calculateElapsedTime(drawingBeginTime);
             }
             long elapsedTime = calculateElapsedTime(beginTime);
             if(elapsedTime < CLOCK_TICK) {
                try {
                   Thread.sleep(CLOCK_TICK - elapsedTime);
                } catch(InterruptedException e) {
                   e.printStackTrace();
                   // The sleep should never be interrupted
                }
             }
          }
       }
       
       public void end() {
          keepRunning = false;
       }
       
       private void tick() {
          // I don't want to sort the actual list of sprites as that would affect
          // the order they're drawn which looks weird, and the order can change
          // tick by tick so it's easiest to sort them once each time.
          List<Sprite> sortedSprites = new ArrayList<Sprite>(sprites);
          Collections.sort(sortedSprites, new FirstComparator());
          List<Sprite> unmodifiableSprites = Collections.unmodifiableList(sortedSprites);
          if(controlPanel != null) {
             long beginTime;
             if(debugTimes) {
                beginTime = System.nanoTime();
             }
             if(decrementLives(tickSprites())) {
                gameOver = true;
                gameMap.signalGameOver();
             }
             if(debugTimes) {
                processSpritesTimes[timesLength] = calculateElapsedTime(beginTime);
                beginTime = System.nanoTime();
             }
             increaseMoney(tickBullets(unmodifiableSprites));
             if(debugTimes) {
                processBulletsTimes[timesLength] = calculateElapsedTime(beginTime);
                beginTime = System.nanoTime();
             }
             tickTowers(unmodifiableSprites);
             if(debugTimes) {
                processTowersTimes[timesLength] = calculateElapsedTime(beginTime);
             }
          }
       }
       
       private long draw() {
          long drawingBeginTime = System.nanoTime();
          // Copy the list of towers as they could be sold while this is running
          List<Tower> towersToDraw = new ArrayList<Tower>(towers);
          if(selectedTower != null) {
             // Set the selected tower to be drawn last, so its range is drawn
             // over all the other towers, rather than some drawn under it and
             // some over it.
             Collections.swap(towers, towers.indexOf(selectedTower), towers.size() - 1);
          }
          drawingBeginTime -= gameMap.draw(towersToDraw, buildingTower,
                Collections.unmodifiableList(sprites), Collections.unmodifiableList(bullets),
                processTime, processSpritesTime, processBulletsTime, processTowersTime,
                drawTime, bullets.size());
          gameMap.repaint();
          return drawingBeginTime;
       }
       
       private long calculateElapsedTime(long beginTime) {
          // Returns in ms, though the beginTime is in ns
          return (System.nanoTime() - beginTime) / 1000000;
       }
       
       private void calculateTimesTaken() {
          processTime = insertAndReturnSum(processTimes);
          processSpritesTime = insertAndReturnSum(processSpritesTimes);
          processBulletsTime = insertAndReturnSum(processBulletsTimes);
          processTowersTime = insertAndReturnSum(processTowersTimes);
          drawTime = insertAndReturnSum(drawTimes);
          timesPos = (timesPos + 1) % timesLength;
       }
       
       private long insertAndReturnSum(long[] array) {
          array[timesPos] = array[timesLength];
          array[timesLength] = 0;
          long sum = 0;
          for(int i = 0; i < timesLength; i++) {
             sum += array[i];
          }
          return sum / timesLength;
       }
       
       private int tickSprites() {
          int livesLost = 0;
          if(levelInProgress && sprites.isEmpty() && spritesToAdd <= 0) {
             endLevel();
             levelInProgress = false;
             // If the level is just finished it's a good time to run the garbage
             // collector rather than have it run during a level.
             System.gc();
          }
          if(spritesToAdd > 0) {
             if(addSpriteIn < 1) {
                sprites.add(new Pacman(level, levelHP, clonePathPoints()));
                // Adds a sprite in somewhere between 0 and twice the designated time
                addSpriteIn = (int)(Math.random() * (ticksBetweenAddSprite * 2 + 1));
                spritesToAdd--;
                controlPanel.updateNumberLeft(spritesToAdd);
             } else {
                addSpriteIn--;
             }
          }
          List<Integer> toRemove = new ArrayList<Integer>();
          for(int i = 0; i < sprites.size(); i++) {
             Sprite s = sprites.get(i);
             if(s.tick()) {
                toRemove.add(i);
                if(s.isAlive()) {
                   // If the sprite is being removed and is still alive, it went
                   // off the edge of the screen
                   livesLost++;
                }
             }
          }
          Helper.removeAll(sprites, toRemove);
          return livesLost;
       }
       
       private List<Point> clonePathPoints() {
          List<Point> clone = new ArrayList<Point>(pathPoints.size());
          for(Point p : pathPoints) {
             clone.add(new Point(p));
          }
          return clone;
       }
       
       private void tickTowers(List<Sprite> unmodifiableSprites) {
          // Only do this if there's a level going, to decrease load and
          // so towers such as charge towers don't charge between levels
          if(levelInProgress) {
             // Don't use for each loop here as a new tower can be built
             for(int i = 0; i < towers.size(); i++ ) {
                bullets.addAll(towers.get(i).tick(unmodifiableSprites));
             }
          }
       }
       
       private long tickBullets(List<Sprite> unmodifiableSprites) {
          if(numThreads == 1 || bullets.size() <= 1) {
             // Use single thread version if only one processor or 1 or fewer bullets
             // as it will be faster.
             return tickBulletsSingleThread(unmodifiableSprites);
          }
          isWaiting = true;
          int bulletsPerThread = bullets.size() / numThreads;
          int remainder = bullets.size() - bulletsPerThread * numThreads;
          Arrays.fill(isThreadRunning, true);
          int firstPos, lastPos = 0;
          for(int i = 0; i < numThreads; i++) {
             firstPos = lastPos;
             lastPos = firstPos + bulletsPerThread + (i < remainder ? 1 : 0);
             // Copying the list should reduce the lag of each thread trying to access
             // the same list
             bulletTickThreads[i].tickBullets(firstPos, lastPos, bullets,
                   new ArrayList<Sprite>(unmodifiableSprites));
          }
          while(isWaiting) {
             LockSupport.park();
          }
          Collections.sort(toRemove);
          Helper.removeAll(bullets, toRemove);
          toRemove.clear();
          long toReturn = (long)moneyEarnt;
          // Fractional amounts of money are kept until the next tick
          moneyEarnt -= toReturn;
          return toReturn;
       }
       
       private synchronized void informFinished(int threadNumber, double moneyEarnt,
             List<Integer> toRemove) {
          this.moneyEarnt += moneyEarnt;
          this.toRemove.addAll(toRemove);
          isThreadRunning[threadNumber] = false;
          for(boolean b : isThreadRunning) {
             if(b) {
                return;
             }
          }
          isWaiting = false;
          LockSupport.unpark(this);
       }
       
       private long tickBulletsSingleThread(List<Sprite> unmodifiableSprites) {
          List<Integer> toRemove = new ArrayList<Integer>();
          for(int i = 0; i < bullets.size(); i++) {
             double money = bullets.get(i).tick(unmodifiableSprites);
             if(money >= 0) {
                moneyEarnt += money;
                toRemove.add(i);
             }
          }
          Helper.removeAll(bullets, toRemove);
          long toReturn = (long)moneyEarnt;
          // Fractional amounts of money are kept until the next tick
          moneyEarnt -= toReturn;
          return toReturn;
       }
       
       private class BulletTickThread extends Thread {
          
          private final int threadNumber;
          private int firstPos, lastPos;
          private List<Bullet> bulletsToTick;
          private List<Sprite> sprites;
          private boolean doTick;
          
          public BulletTickThread(int number) {
             super("Bullet Tick Thread #" + number);
             this.threadNumber = number;
          }
          
          public void tickBullets(int firstPos, int lastPos, List<Bullet> bulletsToTick,
                List<Sprite> sprites) {
             this.firstPos = firstPos;
             this.lastPos = lastPos;
             this.bulletsToTick = bulletsToTick;
             this.sprites = sprites;
             doTick = true;
             LockSupport.unpark(this);
          }
          
          @Override
          public void run() {
             while(true) {
                if(doTick) {
                   double moneyEarnt = 0;
                   List<Integer> toRemove = new ArrayList<Integer>();
                   for(int i = firstPos; i < lastPos; i++) {
                      double money = bulletsToTick.get(i).tick(sprites);
                      if(money >= 0) {
                         moneyEarnt += money;
                         toRemove.add(i);
                      }
                   }
                   doTick = false;
                   informFinished(threadNumber, moneyEarnt, toRemove);
                }
                LockSupport.park();
             }
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
       
       public void processUpgradeButtonPressed(ActionEvent e, JButton b, Attribute a) {
          int numTimes = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0 ? 5 : 1;
          for(int i = 0; i < numTimes; i++) {
             if(selectedTower == null) {
                long cost = costToUpgradeTowers(a, towers);
                if(cost <= money) {
                   decreaseMoney(cost);
                   for(Tower t : towers) {
                      t.raiseAttributeLevel(a, true);
                   }
                }
             } else {
                long cost = Formulae.upgradeCost(selectedTower.getAttributeLevel(a));
                if(cost <= money) {
                   decreaseMoney(cost);
                   selectedTower.raiseAttributeLevel(a, true);
                }
             }
          }
          updateTowerStats();
       }
       
       public void processUpgradeButtonChanged(JButton b, Attribute a) {
          if(checkIfMovedOff(b)) {
             controlPanel.clearCurrentCost();
          } else {
             String description = a.toString() + " Upgrade";
             long cost = 0;
             if(selectedTower == null) {
                description += " (all)";
                cost = costToUpgradeAllTowers(a);
             } else {
                cost = Formulae.upgradeCost(selectedTower.getAttributeLevel(a));
             }
             controlPanel.updateCurrentCost(description, cost);
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
          if(checkIfMovedOff(b)) {
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
          if(checkIfMovedOff(b)) {
             controlPanel.clearCurrentCost();
          } else {
             String description = new String();
             String cost = "Free";
             if(a != null) {
                description = a.toString() + " Upgrade (all)";
             } else if(livesUpgrade) {
                description = upgradeLives + " bonus lives";
             } else if(interestUpgrade) {
                description = "+" + upgradeInterest * 100 + "% interest rate";
             } else if(moneyUpgrade) {
                description = upgradeMoney + " bonus money";
             }
             controlPanel.updateCurrentCost(description, cost);
          }
       }
       
       public void processSellButtonPressed(JButton b) {
          if(selectedTower != null) {
             increaseMoney(sellValue(selectedTower));
             removeTower(selectedTower);
             setSelectedTower(null);
          }
       }
       
       public void processSellButtonChanged(JButton b) {
          if(selectedTower != null && !checkIfMovedOff(b)) {
             controlPanel.updateCurrentCost("Sell " + selectedTower.getName() + " Tower",
                   sellValue(selectedTower));
          }
       }
       
       public void processTargetButtonPressed(JButton b) {
          String s = b.getText();
          for(int i = 0; i < comparators.size(); i++) {
             if(comparators.get(i).toString().equals(s)) {
                int nextIndex = (i + 1) % comparators.size();
                Comparator<Sprite> c = comparators.get(nextIndex);
                b.setText(c.toString());
                selectedTower.setSpriteComparator(c);
                return;
             }
          }
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
          stopRunning();
          gameMap.restart();
          controlPanel.restart();
          setStartingStats();
          clock = new Clock();
       }
       
       private boolean checkIfMovedOff(JButton b) {
          // null means the cursor isn't over the button, so the mouse was moved off it
          return b.getMousePosition() == null;
       }
    }
    
    public class ContinueOn {
       public void continueOn(GameMap g) {
          pathPoints = g.getPathPoints();
          path = g.getPath();
          gameMap = createGameMapPanel(g);
          controlPanel = new ControlPanel(CONTROLS_WIDTH, CONTROLS_HEIGHT,
                ImageHelper.makeImage("control_panel", "blue_lava.jpg"),
                new ControlEventProcessor(), createTowerImplementations());
          outerContainer.remove(selectionScreens);
          outerContainer.add(gameMap, BorderLayout.WEST);
          outerContainer.add(controlPanel, BorderLayout.EAST);
          outerContainer.validate();
          outerContainer.repaint();
          setStartingStats();
          clock = new Clock();
       }
    }
 
 }
