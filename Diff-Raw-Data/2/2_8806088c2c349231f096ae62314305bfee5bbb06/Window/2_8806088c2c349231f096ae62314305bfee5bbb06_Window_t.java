 package elements;
 
 import basic.Layout;
 import basic.Oriented;
 import conf.Dialog;
 import values.BrickColor;
 import values.Orientation;
 import values.Settings;
 
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JFrame;
 import javax.swing.event.MouseInputListener;
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.geom.Rectangle2D;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Game window which holds game field, brick collections, etc.
  * 
  * @author X-Stranger
  */
 @SuppressWarnings("serial")
 public class Window extends JPanel implements MouseInputListener, ActionListener, WindowListener {
 
     private Brick activeBrick = null;
     
     private Settings settings;
     private Layout layout;
     private Field field;
     private Corner scores;
     private BrickCollection leftBricks;
     private BrickCollection rightBricks;
     private BrickCollection topBricks;
     private BrickCollection bottomBricks;
     private List<JMenuItem> saveSlots;
     private List<JMenuItem> loadSlots;
     
     /**
      * Default constructor.
      *
      * @param settings - game settings
      */
     public Window(Settings settings) {
         super();
 
         this.saveSlots = new ArrayList<JMenuItem>();
         this.loadSlots = new ArrayList<JMenuItem>();
 
         this.settings = settings;
         this.createBrickElements();        
         
         this.layout = new Layout();
         this.setLayout(this.layout);
         
         scores = new Scores(this.settings);
         Corner level = new Level(this.settings);
         Corner buttons = new Buttons(this.settings);
         Corner severity = new Severity(this.settings);
         
         this.add("corner,0,0", buttons);
         this.add("corner,0,1", severity);
         this.add("corner,1,0", this.scores);
         this.add("corner,1,1", level);
 
         this.updateLayoutFromField();
         this.updateLayoutFromCollections();
         
         addMouseListener(this);
         addMouseMotionListener(this);
     }
  
     /**
      * Method creates brick elements.
      */
     private void createBrickElements() {
         this.settings.setGameOver(false);
         topBricks = new BrickCollection(this.settings.getDifficulty(), this.settings.isArcade(), Orientation.TOP);
         leftBricks = new BrickCollection(this.settings.getDifficulty(), this.settings.isArcade(), Orientation.LEFT);
         rightBricks = new BrickCollection(this.settings.getDifficulty(), this.settings.isArcade(), Orientation.RIGHT);
         bottomBricks = new BrickCollection(this.settings.getDifficulty(), this.settings.isArcade(), Orientation.BOTTOM);
         field = new Field(this.settings.getDifficulty(), this.settings.getLevel() - 1, 
                 leftBricks, rightBricks, topBricks, bottomBricks);
     }
     
     /**
      * Called when mouse pointer moved.
      * 
      * @param e - mouse event object 
      */
     public void mouseMoved(MouseEvent e) {
         for (int i = 0; i < this.settings.getMenu().getComponentCount(); i++) {
             JMenu menu = (JMenu) this.settings.getMenu().getComponent(i);
             if (menu.isSelected()) { return; }
         }
         
         if (this.settings.isGameOver()) { return; }
         Object object = getComponentAt(e.getPoint());
 
         // checking if object is a brick
         if ((object != null) && object.getClass().isAssignableFrom(Brick.class)) {
             Brick brick = (Brick) object;
             Oriented parent = brick.getParentCollection();
             Point point = layout.getPosition(brick);
             
             // redraw previously active brick
             if (this.activeBrick != brick) {
                 if ((this.activeBrick != null) 
                         && (!field.contains(this.activeBrick)) 
                         && (this.activeBrick.getOrientation() != Orientation.NONE)) {
                     this.activeBrick.setOrientation(Orientation.NONE);
                     Graphics g = this.activeBrick.getGraphics();
                     if (g != null) {
                         this.activeBrick.update(g);
                     }
                 }
                 this.activeBrick = brick;
             }
             
             // checking for parent object existanse
             if ((parent != null) && (point != null)) {
                 Orientation orientation = parent.getOrientation(); 
 
                 int v = field.check(point.x - Layout.CORNER, orientation);
                 int h = field.check(point.y - Layout.CORNER, orientation);
 
                 if (((orientation.isVertical() && (v >= 0)) || (orientation.isHorizontal() && (h >= 0))) 
                         && layout.isBorder(brick)) {
 
                     int distance;
                     if (v < 0) {
                         distance = Math.abs(point.x - (h + Layout.CORNER));
                     } else {
                         distance = Math.abs(point.y - (v + Layout.CORNER));
                     }
 
                     if (distance != 1) {
                         brick.setOrientation(Orientation.switchOrientation(orientation));
                         brick.update(brick.getGraphics());
                     }
                 }
             }
         } else {
             if (this.activeBrick != null) {
                 this.activeBrick.setOrientation(Orientation.NONE);
                 this.activeBrick.update(this.activeBrick.getGraphics());
             }
         }
     }
 
     /**
      * Called when mouse button clicked.
      * 
      * @param e - mouse event object 
      */
     public void mouseClicked(MouseEvent e) {
         if (this.settings.isGameOver()) { return; }
 
         if (field.contains(this.activeBrick) && e.isAltDown() && e.isControlDown() && (e.getClickCount() == 3)) {
             int ind = this.activeBrick.getColor().getIndex() + 1;
             if (ind >= this.settings.getDifficulty()) {
                 ind = 0;
             }
             this.activeBrick.setColor(new BrickColor(ind));
             this.activeBrick.repaint();
             
         } else if ((this.activeBrick.getOrientation() != Orientation.NONE) && !field.contains(this.activeBrick)) {
             this.save();
             Point pos = layout.getPosition(this.activeBrick);
             BrickCollection collection = (BrickCollection) this.activeBrick.getParentCollection();
 
             Brick brick;
             int from;
             
             if (collection.getOrientation().isVertical()) {
                 if (collection.getOrientation() == Orientation.TOP) {
                     from = 0;
                 } else {
                     from = Layout.FIELD - 1;    
                 }
                 
                 brick = collection.shift(pos.x - Layout.CORNER);
                 field.setBrick(pos.x - Layout.CORNER, from, this.activeBrick);
             } else { 
                 if (collection.getOrientation() == Orientation.LEFT) {
                     from = 0;
                 } else {
                     from = Layout.FIELD - 1;    
                 }
                 
                 brick = collection.shift(pos.y - Layout.CORNER);
                 field.setBrick(from, pos.y - Layout.CORNER, this.activeBrick);
             }
          
             this.activeBrick = brick;
 
             this.updateLayoutFromField();
             this.updateLayoutFromCollections();
 
             int scores;
             int moves;
             boolean empty = false;
             do {
                 
                 do { 
                     moves = field.move();
                     if (moves != 0) {
                         this.sleep(this.settings.getMoveDelay());
                         empty = this.updateLayoutFromField();
                         this.updateLayoutFromCollections();
                         if (this.settings.getMoveDelay() != 0) {
                             this.paintImmediately(getVisibleRect());
                         }
                     }
                 } while (moves != 0);
                 
                 scores = field.analize();
                 if (scores != 0) {
                     this.settings.addScores(scores);
                     this.scores.repaint();
                     this.highlightLayoutFromField();
                     if (this.settings.getFireDelay() != 0) {
                         this.paintImmediately(getVisibleRect());
                     }
                     this.sleep(this.settings.getFireDelay());
                     empty = this.updateLayoutFromField();
                 }
                 
             } while (scores != 0);
             
             mouseMoved(e);
             
             if (empty) {
                 this.settings.addScores(this.settings.getLevel() * Settings.LEVEL_UP_SCORES);
                 this.settings.upLevel();
                 this.createBrickElements();
                 this.updateLayoutFromField();
                 this.updateLayoutFromCollections();
                 this.activeBrick = null;
                 this.settings.getUndo().setEnabled(false);
                 this.repaint();
             }
             
             if (!field.canPlay()) {
                 JOptionPane.showMessageDialog(this, 
                         String.format(this.settings.getString("MESSAGE_GAME_END"), this.settings.getScores()),
                         this.settings.getString("TITLE_GAME_END"), 
                         JOptionPane.INFORMATION_MESSAGE);
                 Integer hs = this.settings.checkHighScores();
                 if (hs >= 0) {
                     String name = JOptionPane.showInputDialog(this, this.settings.getString("MESSAGE_GAME_HIGHSCORES"), 
                             this.settings.getString("TITLE_GAME_HIGHSCORES"), JOptionPane.PLAIN_MESSAGE);
                     if (name != null && !"".equals(name)) {
                         this.settings.setHighScores(hs, name);
                         this.showHighScores();
                     }
                 }
                 this.settings.setGameOver(true);
                 this.settings.getUndo().setEnabled(false);
                 this.repaint();
             }
         }
     }
      
     /**
      * Method does thread sleep for animation.
      * 
      * @param value - time to sleep in milliseconds
      */
     private void sleep(int value) {
         try { Thread.sleep(value); } catch (InterruptedException ie) { System.out.println(ie.getMessage()); }
     }
     
     /**
      * Updates game window and layout using data from field.
      * 
      * @return true if field is empty
      */
     private boolean updateLayoutFromField() {
         Brick oldBrick, newBrick;
         boolean result = true;
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 oldBrick = layout.getBrick(i + Layout.CORNER, j + Layout.CORNER);
                 newBrick = field.getBrick(i, j);
                 if (result && !newBrick.hasSameColor(BrickColor.BLACK)) {
                     result = false;
                 }
                 if (oldBrick != newBrick) {
                     if (oldBrick != null) { this.remove(oldBrick); }
                     this.add((i + Layout.CORNER) + "," + (j + Layout.CORNER), newBrick);
                 }
             }
         }
         this.doLayout();
         return result;
     }
     
     /**
      * Updates game window and layout using data from field: highligts removed.
      * 
      * @return true if field is empty
      */
     private boolean highlightLayoutFromField() {
         Brick oldBrick, newBrick;
         boolean result = true;
         for (int i = 0; i < Layout.FIELD; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
                 oldBrick = layout.getBrick(i + Layout.CORNER, j + Layout.CORNER);
                 newBrick = field.getBrick(i, j);
                 if (result && !newBrick.hasSameColor(BrickColor.BLACK)) {
                     result = false;
                 }
                 if (oldBrick != newBrick) {
                     if (oldBrick != null) { this.remove(oldBrick); }
                     this.add((i + Layout.CORNER) + "," + (j + Layout.CORNER), new Brick(BrickColor.GRAY));
                 }
             }
         }
         this.doLayout();
         return result;
     }
     
     /**
      * Updates game window and layout using data from brick collections.
      */
     private void updateLayoutFromCollections() {
         Brick oldBrick, newBrick;
         
         for (int i = 0; i < Layout.CORNER; i++) {
             for (int j = 0; j < Layout.FIELD; j++) {
 
                 // checking left collection
                 oldBrick = layout.getBrick(i, j + Layout.CORNER);
                 newBrick = leftBricks.getBrick(i, j);
                 if (oldBrick != newBrick) {
                     if (oldBrick != null) { this.remove(oldBrick); }
                     this.add(i + "," + (j + Layout.CORNER), newBrick);
                 }
 
                 // checking right collecion
                 oldBrick = layout.getBrick(i + Layout.CORNER + Layout.FIELD, j + Layout.CORNER);
                 newBrick = rightBricks.getBrick(i, j);
                 if (oldBrick != newBrick) {
                     if (oldBrick != null) { this.remove(oldBrick); }
                     this.add((i + Layout.CORNER + Layout.FIELD) + "," + (j + Layout.CORNER), newBrick);
                 }
 
                 // checking top collection
                 oldBrick = layout.getBrick(j + Layout.CORNER, i);
                 newBrick = topBricks.getBrick(j, i);
                 if (oldBrick != newBrick) {
                     if (oldBrick != null) { this.remove(oldBrick); }
                     this.add((j + Layout.CORNER) + "," + i, newBrick);
                 }
 
                 // checking bottom collecion
                 oldBrick = layout.getBrick(j + Layout.CORNER, i + Layout.CORNER + Layout.FIELD);
                 newBrick = bottomBricks.getBrick(j, i);
                 if (oldBrick != newBrick) {
                     if (oldBrick != null) { this.remove(oldBrick); }
                     this.add((j + Layout.CORNER) + "," + (i + Layout.CORNER + Layout.FIELD), newBrick);
                 }
             }
         }
         
         this.doLayout();
     }
     
     /**
      * Method called when menu action performed.
      * 
      * @param e - ActionEvent object
      */
     public void actionPerformed(ActionEvent e) {
         if (e.getSource().getClass().isAssignableFrom(JMenuItem.class)) {
             JMenuItem item = (JMenuItem) e.getSource();
             String text = item.getText();
 
             if (text.equals(settings.getString("MENU_GAME_UNDO"))) {
                 this.restore();
                 this.updateLayoutFromField();
                 this.updateLayoutFromCollections();
                 item.setEnabled(false);
                 this.repaint();
                 return;
             } 
 
             if (text.equals(settings.getString("MENU_GAME_SAVE"))) {
                 this.performSave("");
                 return;
             }
 
             if (text.equals(settings.getString("MENU_GAME_LOAD"))) {
                 this.performLoad("");
                 return;
             }
 
             if (text.equals(settings.getString("MENU_ABOUT_HIGHSCORES"))) {
                 this.showHighScores();
                 return;    
             }
 
             if (text.equals(settings.getString("MENU_ABOUT_GAME"))) {
                 this.showAboutBox();
                 return;    
             }
 
             if (text.equals(settings.getString("MENU_GAME_NEW"))) {
                 this.newGame();
                 return;    
             }
 
             if (text.equals(settings.getString("MENU_GAME_OPTIONS"))) {
                 new Dialog(this.settings);
                 return;    
             }
 
             if (text.equals(settings.getString("MENU_GAME_EXIT"))) {
                 this.windowClosing(null);
                 System.exit(0);    
             }
 
             if (text.startsWith(settings.getString("MENU_GAME_SLOT"))) {
                 if (item.getName().startsWith(settings.getString("MENU_GAME_SAVE_SLOT"))) {
                     this.performSave(item.getName().substring(item.getName().length() - 1));
                     this.activateSlots();
                 } else if (item.getName().startsWith(settings.getString("MENU_GAME_LOAD_SLOT"))) {
                     this.performLoad(item.getName().substring(item.getName().length() - 1));
                 }
             }
         }
     }
 
     /**
      * Method to call internal saveToFile procedure, catch errors and show notification.
      *
      * @param sfx - file name suffix to use
      */
     private void performSave(String sfx) {
         File savegame = new File(Settings.HOME + "savegame" + sfx + ".dat");
         if (!"".equals(sfx) && savegame.exists() && savegame.isFile() && savegame.canRead()) {
             if (JOptionPane.showConfirmDialog(this, settings.getString("MESSAGE_GAME_SAVE_OVER"),
                     settings.getString("TITLE_GAME_SAVE"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                 return;
             }
         }
 
         try {
             this.saveToFile(sfx);
             JOptionPane.showMessageDialog(this, settings.getString("MESSAGE_GAME_SAVE"),
                     settings.getString("TITLE_GAME_SAVE"), JOptionPane.INFORMATION_MESSAGE);
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(this, settings.getString("ERROR_SAVE"),
                     settings.getString("TITLE_GAME_SAVE"), JOptionPane.ERROR_MESSAGE);
         }
     }
 
     /**
      * Method to call internal loadFromFile procedure, catch errors and show notification.
      *
      * @param sfx - file name suffix to use
      */
     private void performLoad(String sfx) {
         if (JOptionPane.showConfirmDialog(this, settings.getString("MESSAGE_GAME_LOAD"),
                 settings.getString("TITLE_GAME_LOAD"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
             try {
                 if (!this.loadFromFile(sfx)) {
                     JOptionPane.showMessageDialog(this, settings.getString("MESSAGE_GAME_LOAD_NO_FILE"),
                             settings.getString("TITLE_GAME_LOAD"), JOptionPane.ERROR_MESSAGE);
                 }
             } catch (Exception ex) {
                 JOptionPane.showMessageDialog(this, settings.getString("ERROR_LOAD"),
                         settings.getString("TITLE_GAME_LOAD"), JOptionPane.ERROR_MESSAGE);
             }
         }
     }
 
     /**
      * Method shows about box.
      */
     private void showAboutBox() {
         JOptionPane.showMessageDialog(this, settings.getString("MESSAGE_GAME_ABOUT"), 
                 settings.getString("TITLE_GAME_ABOUT"), JOptionPane.INFORMATION_MESSAGE, 
                 new ImageIcon(ClassLoader.getSystemResource("images/icon.png")));
     }
 
     /**
      * Method shows dialog box with high scores info.
      */
     private void showHighScores() {
         JOptionPane.showMessageDialog(this, this.settings.getHighScores(), 
                 settings.getString("TITLE_GAME_HIGHSCORES"), JOptionPane.INFORMATION_MESSAGE);
     }
     
     /**
      * Method implements functionality to select severity and start new game.
      */
     private void newGame() {
         JLabel typeLabel = new JLabel(settings.getString("MESSAGE_GAME_NEW_TYPE"));
         ButtonGroup type = new ButtonGroup();
         JRadioButton strategy = new JRadioButton(settings.getString("MESSAGE_GAME_NEW_TYPE_STRATEGY"));
         JRadioButton arcade = new JRadioButton(settings.getString("MESSAGE_GAME_NEW_TYPE_ARCADE"));
         type.add(strategy);
         strategy.setSelected(!settings.isArcade());
         type.add(arcade);
         arcade.setSelected(settings.isArcade());
 
         ButtonGroup group = new ButtonGroup();
         JRadioButton colors5 = new JRadioButton("5 " + settings.getString("MESSAGE_GAME_NEW_COLORS"));
         JRadioButton colors6 = new JRadioButton("6 " + settings.getString("MESSAGE_GAME_NEW_COLORS"));
         JRadioButton colors7 = new JRadioButton("7 " + settings.getString("MESSAGE_GAME_NEW_COLORS"));
         JRadioButton colors8 = new JRadioButton("8 " + settings.getString("MESSAGE_GAME_NEW_COLORS"));
         JRadioButton colors9 = new JRadioButton("9 " + settings.getString("MESSAGE_GAME_NEW_COLORS"));
         JRadioButton colors10 = new JRadioButton("10 " + settings.getString("MESSAGE_GAME_NEW_COLORS"));
         group.add(colors5);
         group.add(colors6);
         group.add(colors7);
         group.add(colors8);
         group.add(colors9);
         group.add(colors10);
 
         Object[] severities = {settings.getString("MESSAGE_GAME_NEW_INVITATION"), 
                 colors5, colors6, colors7, colors8, colors9, colors10, new JSeparator(), typeLabel, strategy, arcade};
         ((JRadioButton) severities[this.settings.getDifficulty() - 4]).setSelected(true);
 
         int result = JOptionPane.showConfirmDialog(this, 
                 severities, 
                 settings.getString("TITLE_GAME_NEW"), 
                 JOptionPane.OK_CANCEL_OPTION, 
                 JOptionPane.QUESTION_MESSAGE);
         
         if (result == JOptionPane.OK_OPTION) {
             if (colors5.isSelected()) { this.settings.setDifficulty(5); } 
             if (colors6.isSelected()) { this.settings.setDifficulty(6); } 
             if (colors7.isSelected()) { this.settings.setDifficulty(7); } 
             if (colors8.isSelected()) { this.settings.setDifficulty(8); } 
             if (colors9.isSelected()) { this.settings.setDifficulty(9); } 
             if (colors10.isSelected()) { this.settings.setDifficulty(9 + 1); }
             this.settings.setArcade(arcade.isSelected());
             this.updateTitle();
             this.settings.setLevel(1);
             this.settings.setScores(0);
             this.createBrickElements();
             this.updateLayoutFromField();
             this.updateLayoutFromCollections();
             this.activeBrick = null;
             this.settings.getUndo().setEnabled(false);
             this.repaint();
         }
     }
 
     /**
      * Updates game window title depending on game type.
      */
     public void updateTitle() {
         String type = this.settings.isArcade()
                 ? this.settings.getString("MESSAGE_GAME_NEW_TYPE_ARCADE")
                 : this.settings.getString("MESSAGE_GAME_NEW_TYPE_STRATEGY");
         ((JFrame) this.getParent().getParent().getParent()).setTitle(settings.getString("TITLE") + " - " + type);
     }
 
     /**
      * Saves game state to file.
      *
      * @throws IOException - if any occurs
      */
     public void saveToFile() throws IOException {
         this.saveToFile("");
     }
 
     /**
      * Saves game state to file.
      *
      * @param sfx - file name suffix to use 
      * @throws IOException - if any occurs
      */
    @SuppressWarnings({ "ResultOfMethodCallIgnored" })
     public void saveToFile(String sfx) throws IOException {
         File home = new File(Settings.HOME);
         home.mkdir();
         File savegame = new File(Settings.HOME + "savegame" + sfx + ".dat");
         savegame.createNewFile();
         if (savegame.isFile() && savegame.canWrite()) {
             FileOutputStream out = new FileOutputStream(Settings.HOME + "savegame" + sfx + ".dat");
             this.settings.saveToStream(out);
             this.field.saveToStream(out);
             this.leftBricks.saveToStream(out);
             this.rightBricks.saveToStream(out);
             this.topBricks.saveToStream(out);
             this.bottomBricks.saveToStream(out);
             out.flush();
             out.close();
         } else {
             throw new IOException();
         }
     }
     
     /**
      * Loads game state from file.
      *
      * @throws IOException - if any occurs
      * @return true if savegame file exists and was loaded
      */
     public boolean loadFromFile() throws IOException {
         return this.loadFromFile("");
     }
 
     /**
      * Loads game state from file.
      *
      * @param sfx - file name suffix to use
      * @throws IOException - if any occurs
      * @return true if savegame file exists and was loaded
      */
     public boolean loadFromFile(String sfx) throws IOException {
         File savegame = new File(Settings.HOME + "savegame" + sfx + ".dat");
         if (savegame.exists()) {
             if (savegame.isFile() && savegame.canRead()) {
                 FileInputStream in = new FileInputStream(savegame);
                 this.settings.loadFromStream(in);
                 this.field.loadFromStream(in);
                 this.leftBricks.loadFromStream(in);
                 this.rightBricks.loadFromStream(in);
                 this.topBricks.loadFromStream(in);
                 this.bottomBricks.loadFromStream(in);
                 in.close();
                 this.updateLayoutFromField();
                 this.updateLayoutFromCollections();
                 this.settings.getUndo().setEnabled(false);
                 this.activeBrick = null;
                 this.repaint();
                 return true;
             } else {
                 throw new IOException();
             }
         } else {
             return false;
         }
     }
 
     /**
      * Saves field and collections bricks copy.
      */
     private void save() {
         this.settings.getUndo().setEnabled(true);
 
         field.save();
         leftBricks.save();
         rightBricks.save();
         topBricks.save();
         bottomBricks.save();
 
         settings.saveScores();
         scores.repaint();
     }
     
     /**
      * Restores field and collections bricks from copy.
      */
     private void restore() {
         field.restore();
         leftBricks.restore();
         rightBricks.restore();
         topBricks.restore();
         bottomBricks.restore();
 
         settings.restoreScores();
         scores.repaint();
     }
     
     /**
      * Invoked when the user attempts to close the window from the window's system menu.
      * 
      * @param e - window event object 
      */
     public void windowClosing(WindowEvent e) {
         try {
             this.saveToFile();
             this.settings.saveHighScores();
             this.settings.saveConfiguration();
         } catch (IOException ie) {
             System.out.println(settings.getString("ERROR_SAVE") + " (" + ie.getMessage() + ")");
         }
     }
              
     /**
      * Method draws component`s content.
      * 
      * @param g - Graphics instance to use for draw.
      */
     public void paint(Graphics g) {
         super.paint(g);
         
         if (this.settings.isGameOver()) {
             // preparing variables
             String gameover = settings.getString("GAMEOVER");
             
             // fill rectangle with scores
             Graphics2D g2 = (Graphics2D) g;
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g.setColor(Color.white);
             g.setFont(settings.getFont(true));
             FontMetrics fm = g.getFontMetrics();
             Rectangle2D area = fm.getStringBounds(gameover, g);
             g.drawString(gameover, (int) (getWidth() - area.getWidth()) / 2, 
                     (int) (getHeight() + area.getHeight()) / 2);
         }
     }
 
     /**
      * Setter for game saving slots.
      *
      * @param saveSlot - JMenuItem element that represents game saving slot
      */
     public void addSaveSlot(JMenuItem saveSlot) {
         this.saveSlots.add(saveSlot);
     }
 
     /**
      * Setter for game loading slots.
      *
      * @param loadSlot - JMenuItem element that represents game loading slot
      */
     public void addLoadSlot(JMenuItem loadSlot) {
         this.loadSlots.add(loadSlot);
     }
 
     /**
      * Common method to call savegame slots activation.
      */
     public void activateSlots() {
         this.activateSaveSlots();
         this.activateLoadSlots();
     }
 
     /**
      * Method check for savegames existing and activates save game elements accordingly.
      */
     private void activateSaveSlots() {
         for (JMenuItem slot : saveSlots) {
             String tmp = slot.getName();
             tmp = tmp.substring(tmp.length() - 1);
             File savegame = new File(Settings.HOME + "savegame" + tmp + ".dat");
             if (savegame.exists() && savegame.isFile() && savegame.canRead()) {
                 DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                 slot.setText(settings.getString("MENU_GAME_SLOT") + " " + tmp
                         + " (" + formatter.format(savegame.lastModified()) + ")");
             } else {
                 slot.setText(settings.getString("MENU_GAME_SLOT") + " " + tmp + " (none)");
             }
         }
     }
 
     /**
      * Method check for savegames existing and activates load game elements accordingly. 
      */
     private void activateLoadSlots() {
         for (JMenuItem slot : loadSlots) {
             String tmp = slot.getName();
             tmp = tmp.substring(tmp.length() - 1);
             File savegame = new File(Settings.HOME + "savegame" + tmp + ".dat");
             if (savegame.exists() && savegame.isFile() && savegame.canRead()) {
                 DateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm");
                 slot.setText(settings.getString("MENU_GAME_SLOT") + " " + tmp
                         + " (" + formatter.format(savegame.lastModified()) + ")");
                 slot.setEnabled(true);
             } else {
                 slot.setText(settings.getString("MENU_GAME_SLOT") + " " + tmp + " (none)");
                 slot.setEnabled(false);
             }
         }
     }
 
     /**
      * Called when mouse exited window.
      * 
      * @param e - mouse event object 
      */
     public void mouseExited(MouseEvent e) {
         this.mouseMoved(e);
     }
      
     /**
      * Called when mouse entered window.
      * 
      * @param e - mouse event object 
      */
     public void mouseEntered(MouseEvent e) {
         this.mouseMoved(e);
     }
       
     /**
      * Called when mouse pointer dragged.
      * 
      * @param e - mouse event object 
      */
     public void mouseDragged(MouseEvent e) {
         // do nothing
     }
      
     /**
      * Called when mouse button released.
      * 
      * @param e - mouse event object 
      */
     public void mouseReleased(MouseEvent e) {
         // do nothing
     }
        
     /**
      * Called when mouse button pressed.
      * 
      * @param e - mouse event object 
      */
     public void mousePressed(MouseEvent e) {
         // do nothing
     }
 
     /**
      * Invoked when the Window is set to be the active Window.
      * 
      * @param e - window event object 
      */
     public void windowActivated(WindowEvent e) {
         // do nothing
     }
              
     /**
      * Invoked when a window has been closed as the result of calling dispose on the window.
      * 
      * @param e - window event object 
      */
     public void windowClosed(WindowEvent e) {
         // do nothing
     }
              
     /**
      * Invoked when a Window is no longer the active Window.
      * 
      * @param e - window event object 
      */
     public void windowDeactivated(WindowEvent e) {
         // do nothing
     }
 
     /**
      * Invoked when a window is changed from a minimized to a normal state.
      * 
      * @param e - window event object 
      */
     public void windowDeiconified(WindowEvent e) {
         // do nothing
     }
              
     /**
      * Invoked when a window is changed from a normal to a minimized state.
      * 
      * @param e - window event object 
      */
     public void windowIconified(WindowEvent e) {
         // do nothing
     }
              
     /**
      * Invoked the first time a window is made visible.
      * 
      * @param e - window event object 
      */
     public void windowOpened(WindowEvent e) {
         // do nothing
     }
 }
