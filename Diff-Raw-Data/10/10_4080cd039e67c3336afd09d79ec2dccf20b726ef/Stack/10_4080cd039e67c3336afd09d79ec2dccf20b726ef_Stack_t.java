 /*
  * $Id$
  *
  * Copyright (c) 2000-2003 by Rodney Kinney
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
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.Area;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import VASSAL.build.GameModule;
 import VASSAL.build.module.Map;
 import VASSAL.build.module.map.StackMetrics;
 import VASSAL.command.Command;
 import VASSAL.tools.SequenceEncoder;
 
 /**
  * A collection of GamePieces which can be moved as a single unit
  */
 public class Stack implements GamePiece, StateMergeable {
   public static final String TYPE = "stack";
   protected static final int INCR = 5;
   protected GamePiece[] contents = new GamePiece[INCR];
   protected int pieceCount = 0;
 
   protected Point pos = new Point(0, 0);
 
   private String id;
   private boolean expanded = false;
 
   protected Map map;
   private static StackMetrics defaultMetrics;
 
   public Stack() {
     this(null);
   }
 
   public Stack(GamePiece p) {
     if (p != null) {
       setMap(p.getMap());
       setPosition(new Point(p.getPosition()));
       add(p);
     }
   }
 
   /**
    * @return an Enumeration of the pieces in the stack, from the bottom up This
    *         is a clone of the contents so add/remove operations during read
    *         won't affect it.
    */
   public Enumeration<GamePiece> getPieces() {
     return new AllPieceEnum();
   }
 
   /**
    * Return an enumeration of the pieces in the start, from the top down
    * 
    * @return
    */
   public Enumeration<GamePiece> getPiecesInReverseOrder() {
     return new ReversePieceEnum();
   }
 
   /**
    * Returns pieces in the order in which they are visible to the player --
    * topmost first In other words, selected pieces first, then unselected pieces
    * from the top to the bottom
    */
   public Enumeration<GamePiece> getPiecesInVisibleOrder() {
     return new VisibleOrderEnum();
   }
 
   public void remove(GamePiece p) {
     removePieceAt(indexOf(p));
     if (getMap() != null) {
       getMap().repaint();
     }
   }
 
   protected void removePieceAt(int index) {
     if (index >= 0 && index < pieceCount) {
       pieceCount--;
       for (int i = index; i < pieceCount; ++i) {
         contents[i] = contents[i + 1];
       }
       expanded = expanded && pieceCount > 1;
     }
   }
   
   /**
    * Perform some action on a GamePiece that has just been removed this Stack
    * @param p
    * @return a {@link Command} that performs the equivalent action when executed
    */
   public Command pieceRemoved(GamePiece p) {
     return null;
   }
 
   protected void insertPieceAt(GamePiece p, int index) {
     if (pieceCount >= contents.length) {
       GamePiece[] newContents = new GamePiece[contents.length + INCR];
       System.arraycopy(contents, 0, newContents, 0, pieceCount);
       contents = newContents;
     }
     for (int i = pieceCount; i > index; --i) {
       contents[i] = contents[i - 1];
     }
     contents[index] = p;
     pieceCount++;
   }
 
   public void removeAll() {
     pieceCount = 0;
     expanded = false;
   }
 
   public int indexOf(GamePiece p) {
     int index = -1;
     for (int i = 0; i < pieceCount; ++i) {
       if (p == contents[i]) {
         index = i;
         break;
       }
     }
     return index;
   }
 
   public GamePiece getPieceAt(int index) {
     return contents[index];
   }
 
   /**
    * Adds a piece to the stack. If the piece already exists in the stack, moves
    * it to the top
    * 
    * @param c
    */
   public void add(GamePiece c) {
     insert(c, pieceCount);
   }
 
   /**
    * Adds a GamePiece to this Stack. Slightly more efficient than
    * {@link #insert} because it assumes the piece does not already belong to
    * this Stack.
    * 
    * @param child
    * @param index
    */
   public void insertChild(GamePiece child, int index) {
     if (child.getParent() != null) {
       child.getParent().remove(child);
     }
     else if (child.getMap() != null) {
       child.getMap().removePiece(child);
     }
     child.setParent(this);
     insertPieceAt(child, index);
   }
 
   public int getPieceCount() {
     return pieceCount;
   }
 
   /**
    * Inserts a child GamePiece at a given index. If the child piece already
    * belongs to this Stack, it will be repositioned to the given index.
    * 
    * @param p
    * @param pos
    */
   public void insert(GamePiece p, int pos) {
     if (p == null) {
       return;
     }
     pos = Math.max(pos, 0);
     pos = Math.min(pos, pieceCount);
     int index = indexOf(p);
     if (index >= 0) {
       if (pos > index) {
         insertPieceAt(p, pos + 1);
         removePieceAt(index);
       }
       else {
         removePieceAt(index);
         insertPieceAt(p, pos);
       }
     }
     else {
       insertChild(p, pos);
     }
   }
   
   /**
    * Perform some action on a GamePiece that has just been added to this Stack
    * @param p
    * @return a {@link Command} that performs the equivalent action when executed
    */
   public Command pieceAdded(GamePiece p) {
     return null;
   }
 
   /**
    * If the <code>obs</code> parameter is a {@link Map}, delegate drawing of
    * this Stack to the {@link StackMetrics} of that Map. If <code>obs</code>
    * is not a Map, use the default StackMetrics
    * 
    * @see StackMetrics#draw
    * @see #getDefaultMetrics
    */
   public void draw(Graphics g, int x, int y, Component obs, double zoom) {
     if (obs instanceof Map.View) {
       ((Map.View) obs).getMap().getStackMetrics().draw(this, g, x, y, obs, zoom);
     }
     else {
       getDefaultMetrics().draw(this, g, x, y, obs, zoom);
     }
   }
 
   /**
    * Return a comma-separated list of the names of the pieces in this Stack
    */
   public String getName(boolean localized) {
     String val = "";
     PieceIterator visibleFilter = PieceIterator.visible(getPiecesInReverseOrder());
     while (visibleFilter.hasMoreElements()) {
       GamePiece p = visibleFilter.nextPiece();
       String s = localized ? p.getLocalizedName() : p.getName();
       val += s;
       if (s.length() > 0 && visibleFilter.hasMoreElements()) {
         val += ", ";
       }
     }
     return val;
   }
   
   public String getName() {
     return getName(false);
   }
   
   public String getLocalizedName() {
     return getName(true);
   }
 
   public Rectangle boundingBox() {
     Rectangle r = new Rectangle();
     Rectangle[] childBounds = new Rectangle[getPieceCount()];
     getMap().getStackMetrics().getContents(this, null, null, childBounds, 0, 0);
     PieceIterator visibleFilter = PieceIterator.visible(getPieces());
     while (visibleFilter.hasMoreElements()) {
       GamePiece p = visibleFilter.nextPiece();
       r = r.union(childBounds[indexOf(p)]);
     }
     return r;
   }
 
   public Shape getShape() {
     Area a = new Area();
     Shape[] childBounds = new Shape[getPieceCount()];
    StackMetrics metrics = getMap() == null ? getDefaultMetrics() : getMap().getStackMetrics();
    metrics.getContents(this, null, childBounds, null, 0, 0);
     PieceIterator visibleFilter = PieceIterator.visible(getPieces());
     while (visibleFilter.hasMoreElements()) {
       GamePiece p = visibleFilter.nextPiece();
       a.add(new Area(childBounds[indexOf(p)]));
     }
     return a;
   }
 
   public void selectNext(GamePiece c) {
     KeyBuffer.getBuffer().remove(c);
     if (pieceCount > 1 && indexOf(c) >= 0) {
       int newSelectedIndex = indexOf(c) == pieceCount - 1 ? pieceCount - 2 : indexOf(c) + 1;
       for (int i = 0; i < pieceCount; ++i) {
         if (indexOf(contents[i]) == newSelectedIndex) {
           KeyBuffer.getBuffer().add(contents[i]);
           return;
         }
       }
     }
   }
 
   public GamePiece getPieceBeneath(GamePiece p) {
     int index = indexOf(p);
     while (index-- > 0) {
       if (!Boolean.TRUE.equals(contents[index].getProperty(Properties.INVISIBLE_TO_ME))) {
         return contents[index];
       }
     }
     return null;
   }
 
   public GamePiece getPieceAbove(GamePiece p) {
     int index = indexOf(p);
     while (++index < getPieceCount()) {
       if (!Boolean.TRUE.equals(contents[index].getProperty(Properties.INVISIBLE_TO_ME))) {
         return contents[index];
       }
     }
     return null;
   }
 
   /** @return the top visible piece in this stack */
   public GamePiece topPiece() {
     for (int i = pieceCount - 1; i >= 0; --i) {
       if (!Boolean.TRUE.equals(contents[i].getProperty(Properties.INVISIBLE_TO_ME))) {
         return contents[i];
       }
     }
     return null;
   }
 
   /**
    * @return the top piece in this stack that is visible to the player with the
    *         given id
    * @param playerId
    * @see GameModule#getUserId
    */
   public GamePiece topPiece(String playerId) {
     for (int i = pieceCount - 1; i >= 0; --i) {
       String hiddenBy = (String) contents[i].getProperty(Properties.HIDDEN_BY);
       if (hiddenBy == null || hiddenBy.equals(playerId)) {
         return contents[i];
       }
     }
     return null;
   }
 
   /**
    * @return the bottom piece in this stack that is visible to the player with
    *         the given id
    * @param playerId
    * @see GameModule#getUserId
    */
   public GamePiece bottomPiece(String playerId) {
     for (int i = 0; i < pieceCount; ++i) {
       String hiddenBy = (String) contents[i].getProperty(Properties.HIDDEN_BY);
       if (hiddenBy == null || hiddenBy.equals(playerId)) {
         return contents[i];
       }
     }
     return null;
   }
 
   /** @return the bottom visible piece in this stack */
   public GamePiece bottomPiece() {
     for (int i = 0; i < pieceCount; ++i) {
       if (!Boolean.TRUE.equals(contents[i].getProperty(Properties.INVISIBLE_TO_ME))) {
         return contents[i];
       }
     }
     return null;
   }
 
   /**
    * @return Number of GamePieces that are visible to me
    */
   protected int nVisible() {
     int nv = 0;
     PieceIterator visibleFilter = PieceIterator.visible(getPieces());
     while (visibleFilter.hasMoreElements()) {
       visibleFilter.nextPiece();
       nv++;
     }
     return nv;
   }
 
   public Command keyEvent(javax.swing.KeyStroke stroke) {
     GamePiece p = topPiece();
     if (p != null) {
       return p.keyEvent(stroke);
     }
     else {
       return null;
     }
   }
 
   public boolean isExpanded() {
     return expanded;
   }
 
   public void setExpanded(boolean b) {
     expanded = b && getPieceCount() > 1;
   }
 
   public String getState() {
     SequenceEncoder se = new SequenceEncoder(';');
     se.append(getMap() == null ? "null" : getMap().getIdentifier()).append(getPosition().x).append(getPosition().y);
     for (int i = 0; i < pieceCount; ++i) {
       se.append(contents[i].getId());
     }
     return se.getValue();
   }
 
   public void setState(String s) {
     SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(s, ';');
     String mapId = st.nextToken();
     setPosition(new Point(st.nextInt(0), st.nextInt(0)));
     pieceCount = 0;
     while (st.hasMoreTokens()) {
       GamePiece child = GameModule.getGameModule().getGameState().getPieceForId(st.nextToken());
       if (child != null) {
         insertChild(child, pieceCount);
       }
     }
     Map m = null;
     if (!"null".equals(mapId)) {
       m = Map.getMapById(mapId);
       if (m == null) {
         throw new RuntimeException("Could not find map " + mapId);
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
   }
 
   /**
    * Compute the difference between <code>newState</code> and
    * <code>oldState</code> and appy that difference to the current state
    * 
    * @param newState
    * @param oldState
    */
   public void mergeState(String newState, String oldState) {
     String mergedState = newState;
     if (!oldState.equals(getState())) {
       SequenceEncoder.Decoder stNew = new SequenceEncoder.Decoder(newState, ';');
       SequenceEncoder.Decoder stOld = new SequenceEncoder.Decoder(oldState, ';');
       SequenceEncoder merge = new SequenceEncoder(';');
       merge.append(stNew.nextToken());
       stOld.nextToken();
       merge.append(stNew.nextToken());
       stOld.nextToken();
       merge.append(stNew.nextToken());
       stOld.nextToken();
       ArrayList<String> newContents = new ArrayList<String>();
       while (stNew.hasMoreTokens()) {
         newContents.add(stNew.nextToken());
       }
       ArrayList<String> oldContents = new ArrayList<String>();
       while (stOld.hasMoreTokens()) {
         oldContents.add(stOld.nextToken());
       }
       for (int i = 0, j = getPieceCount(); i < j; ++i) {
         String id = getPieceAt(i).getId();
         if (!newContents.contains(id) && !oldContents.contains(id)) {
           int index = i == 0 ? -1 :
             newContents.indexOf(getPieceAt(i - 1).getId());
           newContents.add(index + 1, id);
         }
       }
       for (String s : newContents) {
         merge.append(s);
       }
       mergedState = merge.getValue();
     }
     setState(mergedState);
   }
 
   public String getType() {
     return TYPE;
   }
 
   public void setProperty(Object key, Object val) {
   }
   
   public String toString() {
     return super.toString()+"["+getName()+"]";
   }
 
   /**
    * Calls setProperty() on each piece in this stack
    * 
    * @param key
    * @param val
    */
   public void setPropertyOnContents(Object key, Object val) {
     for (Enumeration<GamePiece> e = getPieces(); e.hasMoreElements();) {
       e.nextElement().setProperty(key, val);
     }
   }
 
   public Object getProperty(Object key) {
     return null;
   }
   
   public Object getLocalizedProperty(Object key) {
     return getProperty(key);
   }
 
   public void setMap(Map map) {
     this.map = map;
   }
 
   public Map getMap() {
     return map;
   }
 
   public Point getPosition() {
     return new Point(pos);
   }
 
   public void setPosition(Point p) {
     pos = p;
   }
 
   public Stack getParent() {
     return null;
   }
 
   public void setParent(Stack s) {
     if (s != null) {
       throw new RuntimeException("Cannot add Stack to parent");
     }
   }
 
   public String getId() {
     return id;
   }
 
   public void setId(String id) {
     this.id = id;
   }
 
   public static void setDefaultMetrics(StackMetrics s) {
     defaultMetrics = s;
   }
 
   public StackMetrics getDefaultMetrics() {
     if (defaultMetrics == null) {
       setDefaultMetrics(new StackMetrics());
     }
     return defaultMetrics;
   }
 
   private class VisibleOrderEnum implements Enumeration<GamePiece> {
     private GamePiece next;
     private int index;
     private boolean doingSelected;
 
     public VisibleOrderEnum() {
       doingSelected = true;
       index = pieceCount - 1;
       next = findNext();
     }
 
     public boolean hasMoreElements() {
       return next != null;
     }
 
     public GamePiece nextElement() {
       GamePiece value = next;
       next = findNext();
       return value;
     }
 
     private GamePiece findNext() {
       GamePiece value = null;
       while (index >= 0) {
         GamePiece p = getPieceAt(index--);
         if (doingSelected ^ !Boolean.TRUE.equals(p.getProperty(Properties.SELECTED))) {
           value = p;
           break;
         }
       }
       if (value == null && doingSelected) {
         doingSelected = false;
         index = pieceCount - 1;
         value = findNext();
       }
       return value;
     }
   }
 
   private class AllPieceEnum implements Enumeration<GamePiece> {
     private int index;
     private GamePiece[] p;
 
     public AllPieceEnum() {
       index = 0;
       p = new GamePiece[pieceCount];
       System.arraycopy(contents, 0, p, 0, pieceCount);
     }
 
     public boolean hasMoreElements() {
       return index < p.length;
     }
 
     public GamePiece nextElement() {
       return p[index++];
     }
   }
 
   private class ReversePieceEnum implements Enumeration<GamePiece> {
     private int index;
     private GamePiece[] clone;
 
     public ReversePieceEnum() {
       clone = new GamePiece[pieceCount];
       System.arraycopy(contents, 0, clone, 0, pieceCount);
       index = pieceCount - 1;
     }
 
     public boolean hasMoreElements() {
       return index >= 0;
     }
 
     public GamePiece nextElement() {
       return clone[index--];
     }
   }
 }
