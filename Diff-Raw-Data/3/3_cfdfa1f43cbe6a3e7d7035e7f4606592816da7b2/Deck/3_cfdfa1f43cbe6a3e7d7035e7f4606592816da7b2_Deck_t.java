 /*
  * $Id$
  *
  * Copyright (c) 2004 by Rodney Kinney
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Library General Public
  * License (LGPL) as published by the Free Software Foundation.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Library General Public License for more details.
  *
  * You should have received a copy of the GNU Library General Public
  * License along with this library; if not, copies are available
  * at http://www.opensource.org.
  */
 package VASSAL.counters;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.Iterator;
 import javax.swing.Action;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 import VASSAL.build.GameModule;
 import VASSAL.build.module.Chatter;
 import VASSAL.build.module.Map;
 import VASSAL.build.module.map.DrawPile;
 import VASSAL.build.module.PlayerRoster;
 import VASSAL.command.AddPiece;
 import VASSAL.command.ChangeTracker;
 import VASSAL.command.Command;
 import VASSAL.command.CommandEncoder;
 import VASSAL.command.NullCommand;
 import VASSAL.configure.ColorConfigurer;
 import VASSAL.counters.PropertiesPieceFilter;
 import VASSAL.i18n.Resources;
 import VASSAL.tools.FileChooser;
 import VASSAL.tools.FormattedString;
 import VASSAL.tools.KeyStrokeListener;
 import VASSAL.tools.ScrollPane;
 import VASSAL.tools.SequenceEncoder;
 
 /**
  * A collection of pieces that behaves like a deck, i.e.: Doesn't move. Can't be
  * expanded. Can be shuffled. Can be turned face-up and face-down
  */
 public class Deck extends Stack implements PlayerRoster.SideChangeListener {
   public static final String ID = "deck;"; //$NON-NLS-1$
   public static final String ALWAYS = "Always";
   public static final String NEVER = "Never";
   public static final String USE_MENU = "Via right-click Menu";
   protected static final String NO_USER = "nobody"; // Dummy user ID for turning
   // cards face down
 
   protected boolean drawOutline = true;
   protected Color outlineColor = Color.black;
   protected Dimension size = new Dimension(40, 40);
   protected boolean shuffle = true;
   protected String faceDownOption = ALWAYS;
   protected String shuffleOption = ALWAYS;
   protected boolean allowMultipleDraw = false;
   protected boolean allowSelectDraw = false;
   protected boolean reversible = false;
   protected String reshuffleCommand = ""; //$NON-NLS-1$
   protected String reshuffleTarget;
   protected String reshuffleMsgFormat;
   protected KeyStrokeListener reshuffleListener;
   protected KeyStroke reshuffleKey;
   protected String reverseMsgFormat;
   protected String shuffleMsgFormat;
   protected KeyStrokeListener shuffleListener;
   protected KeyStroke shuffleKey;
   protected String faceDownMsgFormat;
   protected boolean drawFaceUp;
   protected boolean persistable;
 
   protected String deckName;
 
   protected boolean faceDown;
   protected int dragCount = 0;
   protected int maxStack = 10;
   protected CountExpression[] countExpressions;
   protected boolean expressionCounting = false;
   protected ArrayList nextDraw;
   protected KeyCommand[] commands;
   protected CommandEncoder commandEncoder = new CommandEncoder() {
     public Command decode(String command) {
       Command c = null;
       if (command.startsWith(LoadDeckCommand.PREFIX)) {
         c = new LoadDeckCommand(Deck.this);
       }
       return c;
     }
 
     public String encode(Command c) {
       String s = null;
       if (c instanceof LoadDeckCommand) {
         s = LoadDeckCommand.PREFIX;
       }
       return s;
     }
   };
 
   public Deck() {
     this(ID);
     PlayerRoster.addSideChangeListener(this);
   }
 
   public Deck(String type) {
     mySetType(type);
     PlayerRoster.addSideChangeListener(this);
   }
   
   public void sideChanged(String oldSide, String newSide) {
     updateCountsAll();
   } 
 
   /**
   * Update map-level count properties for all "expressions" of pieces that are configured
   * to be counted.  These are held in the String[] countExpressions.
   */
   private void updateCountsAll() {
     if (!doesExpressionCounting() || getMap() == null) {
       return;
     }
     //Clear out all of the registered count expressions
     for (int index = 0; index < countExpressions.length; index++) {
       getMap().getPropertyListener().propertyChange(new PropertyChangeEvent(this,deckName+"_"+countExpressions[index].getName(),null,""+0)); //$NON-NLS-1$ //$NON-NLS-2$
     }
     //Increase all of the pieces with expressions specified in this deck
     for (Enumeration e = getPieces(); e.hasMoreElements();) {
       GamePiece p = (GamePiece) e.nextElement();
       if (p != null) {
         updateCounts(p,true);
       }
     }
   }
   
   /**
   * Update map-level count property for a piece located at index
   * @param index, increase
   */
   private void updateCounts(int index, boolean increase) {
     if (!doesExpressionCounting()) {
       return;
     }
     if (index >= 0 && index < contents.length) {
       GamePiece p = getPieceAt(index);
       if (p == null) {
         //can't figure out the piece, do a full update
         updateCountsAll();
       }
       else {
         updateCounts(p,increase);
       }
     }
     else {
       //can't figure out the piece, do a full update
       updateCountsAll();
     }
   }
   
   /**
   * Update map-level count property for a piece
   * @param piece, increase
   */
   private void updateCounts(GamePiece p, boolean increase) {
     if (!doesExpressionCounting() || getMap() == null) {
       return;
     }
     //test all the expressions for this deck
     for (int index = 0;index < countExpressions.length;index++){ 
       FormattedString formatted = new FormattedString(countExpressions[index].getExpression());
       PieceFilter f = PropertiesPieceFilter.parse(formatted.getText());
       if (f.accept(p)) {
         String mapProperty = (String) getMap().getProperty(deckName+"_"+countExpressions[index].getName()); //$NON-NLS-1$
           if (mapProperty != null) {
             int newValue = (Integer.decode(mapProperty)).intValue();
             if (increase) {
               newValue++;
             }
             else {
               newValue--;
             }
             getMap().getPropertyListener().propertyChange(new PropertyChangeEvent(this,deckName+"_"+countExpressions[index].getName(),null,""+newValue)); //$NON-NLS-1$  //$NON-NLS-2$
           }
       }
     }
   }
   
   /**
    * Set the <deckName>_numPieces property in the containing Map
    * @param oldPieceCount
    */
   protected void fireNumCardsProperty() {
     if (getMap() != null) {
       getMap().getPropertyListener().propertyChange(new PropertyChangeEvent(this,deckName+"_numPieces",null,String.valueOf(pieceCount))); //$NON-NLS-1$
     }
   }
   
   protected void insertPieceAt(GamePiece p, int index) {
     super.insertPieceAt(p, index);
     updateCounts(p,true);
     fireNumCardsProperty();
   }
 
   protected void removePieceAt(int index) {
     updateCounts(index,false);
     super.removePieceAt(index);
     fireNumCardsProperty();
   }
 
   public void removeAll() {
     super.removeAll();
     updateCountsAll();
     fireNumCardsProperty();
   }
 
   public void setMap(Map map) {
     super.setMap(map);
     updateCountsAll();
     fireNumCardsProperty();
   }
 
   protected void mySetType(String type) {
     SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
     st.nextToken();
     drawOutline = st.nextBoolean(true);
     outlineColor = ColorConfigurer.stringToColor(st.nextToken("0,0,0")); //$NON-NLS-1$
     size.setSize(st.nextInt(40), st.nextInt(40));
     faceDownOption = st.nextToken("Always"); //$NON-NLS-1$
     shuffleOption = st.nextToken("Always"); //$NON-NLS-1$
     allowMultipleDraw = st.nextBoolean(true);
     allowSelectDraw = st.nextBoolean(true);
     reversible = st.nextBoolean(true);
     reshuffleCommand = st.nextToken(""); //$NON-NLS-1$
     reshuffleTarget = st.nextToken(""); //$NON-NLS-1$
     reshuffleMsgFormat = st.nextToken(""); //$NON-NLS-1$
     deckName = st.nextToken("Deck");
     shuffleMsgFormat = st.nextToken(""); //$NON-NLS-1$
     reverseMsgFormat = st.nextToken(""); //$NON-NLS-1$
     faceDownMsgFormat = st.nextToken(""); //$NON-NLS-1$
     drawFaceUp = st.nextBoolean(false);
     persistable = st.nextBoolean(false);
     shuffleKey = st.nextKeyStroke(null);
     reshuffleKey = st.nextKeyStroke(null);
     maxStack = st.nextInt(10);
     setCountExpressions(st.nextStringArray(0));
     expressionCounting = st.nextBoolean(false);
     
     if (shuffleListener == null) {
       shuffleListener = new KeyStrokeListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           GameModule.getGameModule().sendAndLog(shuffle());
           map.repaint();
         }
       });
       GameModule.getGameModule().addKeyStrokeListener(shuffleListener);
     }
     shuffleListener.setKeyStroke(getShuffleKey());
 
     if (reshuffleListener == null) {
       reshuffleListener = new KeyStrokeListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           GameModule.getGameModule().sendAndLog(sendToDeck());
           map.repaint();
         }
       });
       GameModule.getGameModule().addKeyStrokeListener(reshuffleListener);
     }
     reshuffleListener.setKeyStroke(getReshuffleKey());
   }
 
   public String getFaceDownOption() {
     return faceDownOption;
   }
 
   /**
    * @return true if cards are turned face up when drawn from this deck
    */
   public boolean isDrawFaceUp() {
     return drawFaceUp;
   }
   
   public void setDrawFaceUp(boolean drawFaceUp) {
     this.drawFaceUp = drawFaceUp;
   }
 
   public void setFaceDownOption(String faceDownOption) {
     this.faceDownOption = faceDownOption;
     faceDown = !faceDownOption.equals(NEVER);
   }
 
   public Dimension getSize() {
     return size;
   }
 
   public void setSize(Dimension size) {
     this.size.setSize(size);
   }
 
   public String getShuffleOption() {
     return shuffleOption;
   }
 
   public void setShuffleOption(String shuffleOption) {
     this.shuffleOption = shuffleOption;
   }
 
   public boolean isShuffle() {
     return shuffle;
   }
 
   public int getMaxStack() {
     return maxStack;
   }
   
   public String[] getCountExpressions() {
     String[] fullstrings = new String[countExpressions.length];
     for (int index = 0; index < countExpressions.length;index++) {
       fullstrings[index] = countExpressions[index].getFullString();
     }
     return fullstrings;
   }
   
   public boolean doesExpressionCounting() {
     return expressionCounting;
   }
   
   public String getFaceDownMsgFormat() {
     return faceDownMsgFormat;
   }
 
   public void setFaceDownMsgFormat(String faceDownMsgFormat) {
     this.faceDownMsgFormat = faceDownMsgFormat;
   }
 
   public String getReverseMsgFormat() {
     return reverseMsgFormat;
   }
 
   public void setReverseMsgFormat(String reverseMsgFormat) {
     this.reverseMsgFormat = reverseMsgFormat;
   }
 
   public String getShuffleMsgFormat() {
     return shuffleMsgFormat;
   }
 
   public void setShuffleMsgFormat(String shuffleMsgFormat) {
     this.shuffleMsgFormat = shuffleMsgFormat;
   }
 
   public KeyStroke getShuffleKey() {
     return shuffleKey;
   }
   
   public void setShuffleKey(KeyStroke shuffleKey) {
     this.shuffleKey = shuffleKey;
   }
   
   public void setShuffle(boolean shuffle) {
     this.shuffle = shuffle;
   }
 
   public boolean isAllowMultipleDraw() {
     return allowMultipleDraw;
   }
 
   public void setAllowMultipleDraw(boolean allowMultipleDraw) {
     this.allowMultipleDraw = allowMultipleDraw;
   }
 
   public boolean isAllowSelectDraw() {
     return allowSelectDraw;
   }
 
   public void setMaxStack(int maxStack) {
     this.maxStack = maxStack;
   }
   
   public void setCountExpressions(String[] countExpressionsString) {
     CountExpression[] c = new CountExpression[countExpressionsString.length];
     int goodExpressionCount = 0;
     for (int index = 0; index < countExpressionsString.length;index++) {
       CountExpression n = new CountExpression(countExpressionsString[index]);
       if (n.getName() != null) {
         c[index] = n;
         goodExpressionCount++;
       }
     }
    this.countExpressions = new CountExpression[goodExpressionCount];
    System.arraycopy(c, 0, countExpressions, 0, goodExpressionCount);
   }
  
   public void setExpressionCounting(boolean expressionCounting) {
     this.expressionCounting = expressionCounting;
   }
   
   public void setAllowSelectDraw(boolean allowSelectDraw) {
     this.allowSelectDraw = allowSelectDraw;
   }
 
   public boolean isReversible() {
     return reversible;
   }
 
   public void setReversible(boolean reversible) {
     this.reversible = reversible;
   }
 
   public void setDeckName(String n) {
     deckName = n;
   }
 
   public String getDeckName() {
     return deckName;
   }
 
   /**
    * The popup menu text for the command that sends the entire deck to another
    * deck
    * 
    * @return
    */
   public String getReshuffleCommand() {
     return reshuffleCommand;
   }
 
   public void setReshuffleCommand(String reshuffleCommand) {
     this.reshuffleCommand = reshuffleCommand;
   }
   
   public KeyStroke getReshuffleKey() {
     return reshuffleKey;
   }
   
   public void setReshuffleKey(KeyStroke reshuffleKey) {
     this.reshuffleKey = reshuffleKey;
   }
   
 
   /**
    * The name of the {@link VASSAL.build.module.map.DrawPile} to which the
    * contents of this deck will be sent when the reshuffle command is selected
    */
   public String getReshuffleTarget() {
     return reshuffleTarget;
   }
 
   public void setReshuffleTarget(String reshuffleTarget) {
     this.reshuffleTarget = reshuffleTarget;
   }
 
   /**
    * The message to send to the chat window when the deck is reshuffled to
    * another deck
    * 
    * @return
    */
   public String getReshuffleMsgFormat() {
     return reshuffleMsgFormat;
   }
 
   public void setReshuffleMsgFormat(String reshuffleMsgFormat) {
     this.reshuffleMsgFormat = reshuffleMsgFormat;
   }
 
   public String getType() {
     SequenceEncoder se = new SequenceEncoder(';');
     se.append(drawOutline).append(ColorConfigurer.colorToString(outlineColor)).append(String.valueOf(size.width)).append(String.valueOf(size.height)).append(
         faceDownOption).append(shuffleOption).append(String.valueOf(allowMultipleDraw)).append(String.valueOf(allowSelectDraw)).append(
         String.valueOf(reversible)).append(reshuffleCommand).append(reshuffleTarget).append(reshuffleMsgFormat).append(deckName).append(shuffleMsgFormat)
         .append(reverseMsgFormat).append(faceDownMsgFormat).append(drawFaceUp).append(persistable).append(shuffleKey).append(reshuffleKey).append(String.valueOf(maxStack))
         .append(getCountExpressions()).append(expressionCounting);
     return ID + se.getValue();
   }
 
   /** Shuffle the contents of the Deck */
   public Command shuffle() {
     ArrayList indices = new ArrayList();
     for (int i = 0; i < getPieceCount(); ++i) {
       indices.add(new Integer(i));
     }
     ArrayList newContents = new ArrayList();
     DragBuffer.getBuffer().clear();
     for (int count = getPieceCount(); count > 0; --count) {
       int i = (int) (GameModule.getGameModule().getRNG().nextFloat() * indices.size());
       int index = ((Integer) indices.get(i)).intValue();
       indices.remove(i);
       newContents.add(getPieceAt(index));
     }
     return setContents(newContents.iterator()).append(reportCommand(shuffleMsgFormat, Resources.getString("Deck.shuffle"))); //$NON-NLS-1$
   }
 
   /**
    * Return an iterator of pieces to be drawn from the Deck. Normally, a random
    * piece will be drawn, but if the Deck supports it, the user may have
    * specified a particular set of pieces or a fixed number of pieces to select
    * with the next draw.
    */
   public PieceIterator drawCards() {
     Iterator it;
     if (nextDraw != null) {
       it = nextDraw.iterator();
     }
     else if (getPieceCount() == 0) {
       it = Collections.EMPTY_LIST.iterator();
     }
     else {
       int count = Math.max(dragCount, Math.min(1, getPieceCount()));
       ArrayList pieces = new ArrayList();
       if (ALWAYS.equals(shuffleOption)) {
         ArrayList indices = new ArrayList();
         for (int i = 0; i < getPieceCount(); ++i) {
           indices.add(new Integer(i));
         }
         while (count-- > 0 && indices.size() > 0) {
           int i = GameModule.getGameModule().getRNG().nextInt(indices.size());
           int index = ((Integer) indices.get(i)).intValue();
           indices.remove(i);
           GamePiece p = getPieceAt(index);
           pieces.add(p);
         }
       }
       else {
         Enumeration e = getPiecesInReverseOrder();
         while (count-- > 0 && e.hasMoreElements()) {
           GamePiece p = (GamePiece) e.nextElement();
           pieces.add(p);
         }
       }
       it = pieces.iterator();
     }
     dragCount = 0;
     nextDraw = null;
     return new PieceIterator(it) {
       public GamePiece nextPiece() {
         GamePiece p = super.nextPiece();
         if (faceDown) {
           p.setProperty(Properties.OBSCURED_BY, NO_USER);
         }
         return p;
       }
     };
   }
 
   /** Set the contents of this Deck to an Enumeration of GamePieces */
   protected Command setContents(Iterator it) {
     ChangeTracker track = new ChangeTracker(this);
     removeAll();
     while (it.hasNext()) {
       GamePiece child = (GamePiece) it.next();
       insertChild(child, pieceCount);
     }
     return track.getChangeCommand();
   }
 
   public String getState() {
     SequenceEncoder se = new SequenceEncoder(';');
     se.append(getMap() == null ? "null" : getMap().getIdentifier()).append(getPosition().x).append(getPosition().y); //$NON-NLS-1$
     se.append(faceDown);
     SequenceEncoder se2 = new SequenceEncoder(',');
     for (Enumeration e = getPieces(); e.hasMoreElements();) {
       GamePiece p = (GamePiece) e.nextElement();
       se2.append(p.getId());
     }
     if (se2.getValue() != null) {
       se.append(se2.getValue());
     }
     return se.getValue();
   }
 
   public void setState(String state) {
     SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(state, ';');
     String mapId = st.nextToken();
     setPosition(new Point(st.nextInt(0), st.nextInt(0)));
     Map m = null;
     if (!"null".equals(mapId)) { //$NON-NLS-1$
       m = Map.getMapById(mapId);
       if (m == null) {
         throw new RuntimeException(Resources.getString("Deck.could_not_find", mapId)); //$NON-NLS-1$
       }
     }
     if (m != getMap()) {
       if (m != null) {
         m.addPiece(this);
       }
       else {
         setMap(null);
       }
     }
     faceDown = "true".equals(st.nextToken()); //$NON-NLS-1$
     ArrayList l = new ArrayList();
     if (st.hasMoreTokens()) {
       SequenceEncoder.Decoder st2 = new SequenceEncoder.Decoder(st.nextToken(), ',');
       while (st2.hasMoreTokens()) {
         GamePiece p = GameModule.getGameModule().getGameState().getPieceForId(st2.nextToken());
         if (p != null) {
           l.add(p);
         }
       }
     }
     setContents(l.iterator());
     commands = null; // Force rebuild of popup menu
   }
 
   public Command setContentsFaceDown(boolean value) {
     ChangeTracker t = new ChangeTracker(this);
     Command c = new NullCommand();
     faceDown = value;
     return t.getChangeCommand().append(c).append(reportCommand(faceDownMsgFormat, value ? Resources.getString("Deck.face_down") : Resources.getString("Deck.face_up"))); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   /** Reverse the order of the contents of the Deck */
   public Command reverse() {
     ArrayList list = new ArrayList();
     for (Enumeration e = getPiecesInReverseOrder(); e.hasMoreElements();) {
       list.add(e.nextElement());
     }
     return setContents(list.iterator()).append(reportCommand(reverseMsgFormat, Resources.getString("Deck.reverse"))); //$NON-NLS-1$
   }
 
   public boolean isDrawOutline() {
     return drawOutline;
   }
 
   public void setOutlineColor(Color outlineColor) {
     this.outlineColor = outlineColor;
   }
 
   public void setDrawOutline(boolean drawOutline) {
     this.drawOutline = drawOutline;
   }
 
   public Color getOutlineColor() {
     return outlineColor;
   }
 
   public boolean isFaceDown() {
     return faceDown;
   }
   
   public Command pieceAdded(GamePiece p) {
     return null;
   }
 
   public Command pieceRemoved(GamePiece p) {
     ChangeTracker tracker = new ChangeTracker(p);
     p.setProperty(Properties.OBSCURED_BY, isFaceDown() && !isDrawFaceUp() ? GameModule.getUserId() : null);
     return tracker.getChangeCommand();
   }
 
   public void setFaceDown(boolean faceDown) {
     this.faceDown = faceDown;
   }
 
   public void draw(java.awt.Graphics g, int x, int y, Component obs, double zoom) {
     int count = Math.min(getPieceCount(), maxStack);
     GamePiece top = nextDraw != null ? (GamePiece) nextDraw.get(0) : topPiece();
 
     if (top != null) {
       Object owner = top.getProperty(Properties.OBSCURED_BY);
       top.setProperty(Properties.OBSCURED_BY, faceDown ? NO_USER : null);
       Color blankColor = getBlankColor();
       Rectangle r = top.getShape().getBounds();
       r.setLocation(x + (int) (zoom * (r.x)), y + (int) (zoom * (r.y)));
       r.setSize((int) (zoom * r.width), (int) (zoom * r.height));
       for (int i = 0; i < count - 1; ++i) {
         if (blankColor != null) {
           g.setColor(blankColor);
           g.fillRect(r.x + (int) (zoom * 2 * i), r.y - (int) (zoom * 2 * i), r.width, r.height);
           g.setColor(Color.black);
           g.drawRect(r.x + (int) (zoom * 2 * i), r.y - (int) (zoom * 2 * i), r.width, r.height);
         }
         else if (faceDown) {
           top.draw(g, x + (int) (zoom * 2 * i), y - (int) (zoom * 2 * i), obs, zoom);
         }
         else {
           getPieceAt(count - i - 1).draw(g, x + (int) (zoom * 2 * i), y - (int) (zoom * 2 * i), obs, zoom);
         }
       }
       top.draw(g, x + (int) (zoom * 2 * (count - 1)), y - (int) (zoom * 2 * (count - 1)), obs, zoom);
       top.setProperty(Properties.OBSCURED_BY, owner);
     }
     else {
       if (drawOutline) {
         Rectangle r = boundingBox();
         r.setLocation(x + (int) (zoom * r.x), y + (int) (zoom * r.y));
         r.setSize((int) (zoom * r.width), (int) (zoom * r.height));
         g.setColor(outlineColor);
         g.drawRect(r.x, r.y, r.width, r.height);
       }
     }
   }
 
   /**
    * The color used to draw boxes representing cards underneath the top one. If
    * null, then draw each card normally for face-up decks, and duplicate the top
    * card for face-down decks
    * 
    * @return
    */
   protected Color getBlankColor() {
     Color c = Color.white;
     if (getMap() != null) {
       c = getMap().getStackMetrics().getBlankColor();
     }
     return c;
   }
 
   public Rectangle boundingBox() {
     GamePiece top = topPiece();
     Dimension d = top == null ? size : top.getShape().getBounds().getSize();
     Rectangle r = new Rectangle(new Point(), d);
     r.translate(-r.width / 2, -r.height / 2);
     return r;
   }
 
   public Shape getShape() {
     return boundingBox();
   }
 
   public Object getProperty(Object key) {
     Object value = null;
     if (Properties.NO_STACK.equals(key)) {
       value = Boolean.TRUE;
     }
     else if (Properties.KEY_COMMANDS.equals(key)) {
       value = getKeyCommands();
     }
     return value;
   }
 
   protected KeyCommand[] getKeyCommands() {
     if (commands == null) {
       ArrayList l = new ArrayList();
       KeyCommand c = null;
       if (USE_MENU.equals(shuffleOption)) {
         c = new KeyCommand(Resources.getString("Deck.shuffle"), getShuffleKey(), this) { //$NON-NLS-1$
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             GameModule.getGameModule().sendAndLog(shuffle());
             map.repaint();
           }
         };
         l.add(c);
       }
       if (reshuffleCommand.length() > 0) {
         c = new KeyCommand(reshuffleCommand, getReshuffleKey(), this) {
           private static final long serialVersionUID = 1L; 
 
           public void actionPerformed(ActionEvent evt) {
             GameModule.getGameModule().sendAndLog(sendToDeck());
             map.repaint();
           }
         };
         l.add(c);
       }
       if (USE_MENU.equals(faceDownOption)) {
         KeyCommand faceDownAction = new KeyCommand(faceDown ? Resources.getString("Deck.face_up") : Resources.getString("Deck.face_down"), null, this) { //$NON-NLS-1$ //$NON-NLS-2$
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             Command c = setContentsFaceDown(!faceDown);
             GameModule.getGameModule().sendAndLog(c);
             map.repaint();
           }
         };
         l.add(faceDownAction);
       }
       if (reversible) {
         c = new KeyCommand(Resources.getString("Deck.reverse_order"), null, this) { //$NON-NLS-1$
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             Command c = reverse();
             GameModule.getGameModule().sendAndLog(c);
             map.repaint();
           }
         };
         l.add(c);
       }
       if (allowMultipleDraw) {
         c = new KeyCommand(Resources.getString("Deck.draw_multiple"), null, this) { //$NON-NLS-1$
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             promptForDragCount();
           }
         };
         l.add(c);
       }
       if (allowSelectDraw) {
         c = new KeyCommand(Resources.getString("Deck.draw_specific"), null, this) { //$NON-NLS-1$
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             promptForNextDraw();
             map.repaint();
           }
         };
         l.add(c);
       }
       if (persistable) {
         c = new KeyCommand(Resources.getString(Resources.SAVE), null, this) {
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             GameModule.getGameModule().sendAndLog(saveDeck());
             map.repaint();
           }
         };
         l.add(c);
         c = new KeyCommand(Resources.getString(Resources.LOAD), null, this) {
           private static final long serialVersionUID = 1L;
 
           public void actionPerformed(ActionEvent e) {
             GameModule.getGameModule().sendAndLog(loadDeck());
             map.repaint();
           }
         };
         l.add(c);
       }
       commands = (KeyCommand[]) l.toArray(new KeyCommand[l.size()]);
     }
     for (int i = 0; i < commands.length; ++i) {
       if (Resources.getString("Deck.face_up").equals(commands[i].getValue(Action.NAME)) && !faceDown) { //$NON-NLS-1$
         commands[i].putValue(Action.NAME, Resources.getString("Deck.face_down")); //$NON-NLS-1$
       }
       else if (Resources.getString("Deck.face_down").equals(commands[i].getValue(Action.NAME)) && faceDown) { //$NON-NLS-1$
         commands[i].putValue(Action.NAME, Resources.getString("Deck.face_up")); //$NON-NLS-1$
       }
     }
     return commands;
   }
 
   /*
    * Format command report as per module designers setup.
    */
   protected Command reportCommand(String format, String commandName) {
     Command c = null;
     FormattedString reportFormat = new FormattedString(format);
     reportFormat.setProperty(DrawPile.DECK_NAME, getDeckName());
     reportFormat.setProperty(DrawPile.COMMAND_NAME, commandName);
     String rep = reportFormat.getText();
     if (rep.length() > 0) {
       c = new Chatter.DisplayText(GameModule.getGameModule().getChatter(), "* " + rep); //$NON-NLS-1$
       c.execute();
     }
 
     return c;
   }
 
   public void promptForDragCount() {
     while (true) {
       String s = JOptionPane.showInputDialog(Resources.getString("Deck.enter_the_number")); //$NON-NLS-1$
       if (s != null) {
         try {
           dragCount = Integer.parseInt(s);
           dragCount = Math.min(dragCount, getPieceCount());
           if (dragCount >= 0) {
             break;
           }
         }
         catch (NumberFormatException ex) {
         }
       }
       else {
         break;
       }
     }
   }
 
   protected void promptForNextDraw() {
     final JDialog d = new JDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class, map.getView()), true);
     d.setTitle(Resources.getString("Deck.draw")); //$NON-NLS-1$
     d.getContentPane().setLayout(new BoxLayout(d.getContentPane(), BoxLayout.Y_AXIS));
     final String[] pieces = new String[getPieceCount()];
     for (int i = 0; i < pieces.length; ++i) {
       pieces[pieces.length - i - 1] = Decorator.getInnermost(getPieceAt(i)).getName();
     }
     final JList list = new JList(pieces);
     list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
     d.getContentPane().add(new ScrollPane(list));
     d.getContentPane().add(new JLabel(Resources.getString("Deck.select_cards"))); //$NON-NLS-1$
     d.getContentPane().add(new JLabel(Resources.getString("Deck.then_click"))); //$NON-NLS-1$
     Box box = Box.createHorizontalBox();
     JButton b = new JButton(Resources.getString(Resources.OK));
     b.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         int[] selection = list.getSelectedIndices();
         if (selection.length > 0) {
           nextDraw = new ArrayList();
           for (int i = 0; i < selection.length; ++i) {
             nextDraw.add(getPieceAt(pieces.length - selection[i] - 1));
           }
         }
         else {
           nextDraw = null;
         }
         d.dispose();
       }
     });
     box.add(b);
     b = new JButton(Resources.getString(Resources.CANCEL));
     b.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent e) {
         d.dispose();
       }
     });
     box.add(b);
     d.getContentPane().add(box);
     d.pack();
     d.setLocationRelativeTo(d.getOwner());
     d.setVisible(true);
   }
 
   /**
    * Combine the contents of this Deck with the contents of the deck specified
    * by {@link #reshuffleTarget}
    */
   public Command sendToDeck() {
     Command c = null;
     DrawPile target = DrawPile.findDrawPile(reshuffleTarget);
     if (target != null) {
       if (reshuffleMsgFormat.length() > 0) {
         c = reportCommand(reshuffleMsgFormat, reshuffleCommand);
         if (c == null) {
           c = new NullCommand();
         }
       }
       else {
         c = new NullCommand();
       }
       // move cards to deck
       int cnt = getPieceCount() - 1;
       for (int i = cnt; i >= 0; i--) {
         c.append(target.addToContents(getPieceAt(i)));
       }
     }
     return c;
   }
 
   public boolean isExpanded() {
     return false;
   }
 
   /** Return true if this deck can be saved to and loaded from a file on disk */
   public boolean isPersistable() {
     return persistable;
   }
 
   public void setPersistable(boolean persistable) {
     this.persistable = persistable;
   }
 
   private File getSaveFileName() {
     FileChooser fc = GameModule.getGameModule().getFileChooser();
     File sf = fc.getSelectedFile();
     if (sf != null) {
       String name = sf.getPath();
       if (name != null) {
         int index = name.lastIndexOf('.');
         if (index > 0) {
           name = name.substring(0, index) + ".sav"; //$NON-NLS-1$
           fc.setSelectedFile(new File(name));
         }
       }
     } 
 
     if (fc.showSaveDialog(map.getView()) != FileChooser.APPROVE_OPTION)
       return null;
     
     File outputFile = fc.getSelectedFile();
     if (outputFile != null &&
         outputFile.exists() &&
         shouldConfirmOverwrite() &&
         JOptionPane.NO_OPTION ==
          JOptionPane.showConfirmDialog(GameModule.getGameModule().getFrame(),
           Resources.getString("Deck.overwrite", outputFile.getName()), Resources.getString("Deck.file_exists"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
           JOptionPane.YES_NO_OPTION)) {
         outputFile = null;
     } 
 
     return outputFile;
   }
 
   private boolean shouldConfirmOverwrite() {
     return System.getProperty("os.name").trim().equalsIgnoreCase("linux"); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   private Command saveDeck() {
     Command c = new NullCommand();
     GameModule.getGameModule().warn(Resources.getString("Deck.saving_deck")); //$NON-NLS-1$
     try {
       File saveFile = getSaveFileName();
       if (saveFile != null) {
         saveDeck(saveFile);
         GameModule.getGameModule().warn(Resources.getString("Deck.deck_saved")); //$NON-NLS-1$
       }
       else {
         GameModule.getGameModule().warn(Resources.getString("Deck.save_canceled")); //$NON-NLS-1$
       }
     }
     catch (IOException err) {
       GameModule.getGameModule().warn(Resources.getString("Deck.save_failed")); //$NON-NLS-1$
     }
     return c;
   }
 
   public void saveDeck(File f) throws IOException {
     Command comm = new LoadDeckCommand(null);
 
     FileWriter dest = new FileWriter(f);
     for (Enumeration e = getPieces(); e.hasMoreElements();) {
       GamePiece p = (GamePiece) e.nextElement();
       comm = comm.append(new AddPiece(p));
 
     }
     GameModule.getGameModule().addCommandEncoder(commandEncoder);
     dest.write(GameModule.getGameModule().encode(comm));
     GameModule.getGameModule().removeCommandEncoder(commandEncoder);
     dest.close();
   }
 
   private File getLoadFileName() {
     FileChooser fc = GameModule.getGameModule().getFileChooser();
     fc.selectDotSavFile();
     if (fc.showOpenDialog(map.getView()) != FileChooser.APPROVE_OPTION)
       return null;
     return fc.getSelectedFile();
   }
 
   private Command loadDeck() {
     Command c = new NullCommand();
     GameModule.getGameModule().warn(Resources.getString("Deck.loading_deck")); //$NON-NLS-1$
     try {
       File saveFile = getLoadFileName();
       if (saveFile != null) {
         c = loadDeck(saveFile);
         GameModule.getGameModule().warn(Resources.getString("Deck.deck_loaded")); //$NON-NLS-1$
       }
       else {
         GameModule.getGameModule().warn(Resources.getString("Deck.load_canceled")); //$NON-NLS-1$
       }
     }
     catch (IOException err) {
       GameModule.getGameModule().warn(Resources.getString("Deck.load_failed")); //$NON-NLS-1$
     }
     return c;
 
   }
 
   public Command loadDeck(File f) throws IOException {
     FileReader src = new FileReader(f);
     char[] data = new char[10000];
     StringBuffer buffer = new StringBuffer();
     int len;
     while ((len = src.read(data)) > 0) {
       buffer.append(data, 0, len);
     }
     src.close();
     GameModule.getGameModule().addCommandEncoder(commandEncoder);
     Command c = GameModule.getGameModule().decode(buffer.toString());
     GameModule.getGameModule().removeCommandEncoder(commandEncoder);
     if (c instanceof LoadDeckCommand) {
       /*
        * A LoadDeckCommand doesn't specify the deck to be changed (since the
        * saved deck can be loaded into any deck) so the Command we send to other
        * players is a ChangePiece command for this deck, which we need to place
        * after the AddPiece commands for the contents
        */
       ChangeTracker t = new ChangeTracker(this);
       c.execute();
       Command[] sub = c.getSubCommands();
       c = new NullCommand();
       for (int i = 0; i < sub.length; ++i) {
         c.append(sub[i]);
       }
       c.append(t.getChangeCommand());
       updateCountsAll();
     }
     else {
       GameModule.getGameModule().warn(Resources.getString("Deck.not_a_saved_deck", f.getName())); //$NON-NLS-1$
       c = null;
     }
     return c;
   }
 
   /**
    * Command to set the contents of this deck from a saved file. The contents
    * are saved with whatever id's the pieces have in the game when the deck was
    * saved, but new copies are created when the deck is re-loaded.
    * 
    * @author rkinney
    * 
    */
   protected static class LoadDeckCommand extends Command {
     public static final String PREFIX = "DECK\t"; //$NON-NLS-1$
     private Deck target;
 
     public LoadDeckCommand(Deck target) {
       this.target = target;
     }
 
     protected void executeCommand() {
       target.removeAll();
       Command[] sub = getSubCommands();
       for (int i = 0; i < sub.length; i++) {
         if (sub[i] instanceof AddPiece) {
           GamePiece p = ((AddPiece) sub[i]).getTarget();
           // We set the id to null so that the piece will get a new id
           // when the AddPiece command executes
           p.setId(null);
           target.add(p);
         }
       }
     }
 
     public String getTargetId() {
       return target == null ? "" : target.getId(); //$NON-NLS-1$
     }
 
     protected Command myUndoCommand() {
       return null;
     }
   }
 
   /**
    * An object that parses expression strings from the config window
    */
   protected class CountExpression {
     private String fullstring;
     private String name;
     private String expression;
     public CountExpression(String expressionString) {
       String[] split = expressionString.split("\\s*:\\s*",2); //$NON-NLS-1$
       if (split.length == 2) {
         name       = split[0];
         expression = split[1];
         fullstring = expressionString;
       }
     }
 
     public String getName() {
       return name;
     }
 
     public String getExpression() {
       return expression;
     }
 
     public String getFullString() {
       return fullstring;
     }
   }
 
     /**
      * Return the number of cards to be returned by next call to {@link #drawCards()}
      */
     public int getDragCount() {
       return dragCount;
     }
 
     /**
      * Set the number of cards to be returned by next call to {@link #drawCards()}
      * @param dragCount
      */
      public void setDragCount(int dragCount) {
        this.dragCount = dragCount;
      }
 }
