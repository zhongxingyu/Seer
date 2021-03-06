 // Copyright (c) 1995, 1996 Regents of the University of California.
 // All rights reserved.
 //
 // This software was developed by the Arcadia project
 // at the University of California, Irvine.
 //
 // Redistribution and use in source and binary forms are permitted
 // provided that the above copyright notice and this paragraph are
 // duplicated in all such forms and that any documentation,
 // advertising materials, and other materials related to such
 // distribution and use acknowledge that the software was developed
 // by the University of California, Irvine.  The name of the
 // University may not be used to endorse or promote products derived
 // from this software without specific prior written permission.
 // THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
 // IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
 // WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
 // File: RedrawManager.java
 // Classes: RedrawManager
 // Original Author: jrobbins@ics.uci.edu
 // $Id$
 
 package uci.gef;
 
 import java.awt.*;
 import java.awt.image.*;
 import java.util.*;
 
 
 /** Stores a list of rectangles (and sometimes merges them) for use in
  *  determing the invalid region of a Editor. When a Fig
  *  changes state, it notifies all Editor's that are viewing it, and
  *  the Editor adds bbox of that Fig to its
  *  RedrawManager. Eventually, the RedrawManager will ask the editor
  *  to repaint damaged regions. It is important that the redraw not
  *  happen too soon after the damage, because we would like to use a
  *  single repaint to repair multple damages to the same part of the
  *  screen.
  *  <A HREF="../features.html#visual_updates">
  *  <TT>FEATURE: visual_updates</TT></A>
  *  <A HREF="../features.html#redraw_minimal">
  *  <TT>FEATURE: redraw_minimal</TT></A>
  */
 
 public class RedrawManager implements Runnable {
 
   ////////////////////////////////////////////////////////////////
   // constants
 
   /** The maximum number of damaged rectangles. */
   private final int MAX_NUM_RECTS = 10;
 
   ////////////////////////////////////////////////////////////////
   // instance variables
 
   /** The array of damaged rectangles. */
   private Rectangle[] _rects = new Rectangle[MAX_NUM_RECTS];
 
   /** The current number of damaged rectangles. */
   private int _nRects = 0;
 
   /** The editor that controls this RedrawManager. */
   private Editor _ed;
 
   /** The thread spawned to periodically request repaints. */
   private Thread _repairThread;
 
   /** Time at which to request next repaint. */
   private long deadline = 0;
 
   /** Milliseconds between most recent addition of damage and next
    *  redraw. */
   private static long _timeDelay = 25;
 
   private static String LOCK = new String("LOCK"); // could be any object
   private static int _lockLevel = 0;
 
   ////////////////////////////////////////////////////////////////
   // constructors
 
   /** Construct a new RedrawManager */
   public RedrawManager(Editor ed) {
     for (int i = 0; i < MAX_NUM_RECTS; ++i) _rects[i] = new Rectangle();
     _ed = ed;
     _repairThread = new Thread(this, "RepairThread");
     // Needs-More-Work: this causes a security violation in Netscape
     // _repairThread.setDaemon(true);
     // _repairThread.setPriority(Thread.MAX_PRIORITY);
     _repairThread.start();
   }
 
   ////////////////////////////////////////////////////////////////
   // accessors
 
   /** Get the minimum number of milliseconds between damage being
    *  added and a repair being done. */
   public static long getTimeBetweenRepairs() { return _timeDelay; }
 
   /** Set the minimum number of milliseconds between damage being
    *  added and a repair being done. */
   public static void setTimeBetweenRepairs(long t) { _timeDelay = t; }
 
   /** If screen repainting is fast enough, then try do it more
    *  often. This can be called from the Editor to try to optimize the
    *  tradeoff between screen updates and latency in event
    *  processing. */
   public static void moreRepairs() {
     _timeDelay -= 5;
     if (_timeDelay < 10) _timeDelay = 10;
   }
 
   /** If screen repainting is getting really slow and the application
    *  cannot process events, then make redraws less frequent */
   public static void fewerRepairs() {
     _timeDelay += 50;
     if (_timeDelay > 2000) _timeDelay = 2000;
   }
 
   public static void setFramesPerSecond(float fps) {
     if (fps > 100.0 || fps < 0.5) return;
    int _timeDelay = (int) (1000/fps);
   }
 
  public static float getFramesPerSecond() { return (float) (1000.0 / _timeDelay); }
 
   ////////////////////////////////////////////////////////////////
   // managing damage
 
   /** Internal function to forget all damage. */
   private void removeAllElements() { _nRects = 0; }
 
   /** Reply true iff some damage has been added but not yet redrawn. */
   public boolean pendingDamage() { return _nRects != 0; }
 
   /** Add a new rectangle of damage, it will soon be
    * redrawn. needs-more-work: how much time is spent here? Could it
    * be faster if I pass in Figs instead of Rectangles?  How much
    * garbage is generated when I call Fig.getBounds()? */
   public void add(Rectangle r) {
     if (r.isEmpty()) return;
     synchronized (LOCK) {
       if (!merge(r)) _rects[_nRects++].reshape(r.x, r.y, r.width, r.height);
       if (_nRects == MAX_NUM_RECTS) forceMerge();
       if (deadline == 0) deadline = System.currentTimeMillis() + _timeDelay;
     }
   }
 
   public void add(Fig f) {
     synchronized (LOCK) {
       f.stuffBounds(_rects[_nRects]);
       if (!merge(_rects[_nRects])) _nRects++;
       if (_nRects == MAX_NUM_RECTS) forceMerge();
       if (deadline == 0) deadline = System.currentTimeMillis() + _timeDelay;
     }
   }
 
   public void add(Selection sel) {
     synchronized (LOCK) {
       sel.stuffBounds(_rects[_nRects]);
       if (!merge(_rects[_nRects])) _nRects++;
       if (_nRects == MAX_NUM_RECTS) forceMerge();
       if (deadline == 0) deadline = System.currentTimeMillis() + _timeDelay;
     }
   }
 
   /** Try to merge the given rect into one of my existing damaged
    *  rects. Rects can be merged if they are overlapping. In general,
    *  they shuold be merged when the area added by the merger is
    *  small enough that one large repaint is faster than two smaller
    *  repaints. Reply true on success. */
   private boolean merge(Rectangle r) {
     for (int i = 0; i < _nRects; ++i)
       if (r.intersects(_rects[i])) { _rects[i].add(r); return true; }
     return false;
   }
 
   /** Merge all the rectangles together, even if that means that a lot
    *  more area is redrawn than needs to be. */
   public void forceMerge() {
     for (int i = 1; i < _nRects; ++i) _rects[0].add(_rects[i]);
     _nRects = 1;
     deadline = 1;
   }
 
   ////////////////////////////////////////////////////////////////
   // locking
 
   /** Lock all RedrawManagers during changes to the diagram. This
    *  prevents repainting of Fig's that are in invalid
    *  states. Needs-More-Work: This takes a fair amount of time. */
   public static void lock() { synchronized (LOCK) { _lockLevel++; } }
 
   /** Unlock all RedrawManager after changes to the diagram. This
    *  prevents repainting of Fig's that are in invalid
    *  states. Needs-More-Work: This takes a fair amount of time. */
   public static void unlock() {
     synchronized (LOCK) {
       _lockLevel--;
       if (_lockLevel < 0) _lockLevel = 0;
     }
   }
 
   ////////////////////////////////////////////////////////////////
   // repaint and frame-rate logic
 
   /** The main method of the _repairThread, basically it just keeps
    *  checking if there is damage that has not been repaired, and that
    *  damage is old enough. */
   public void run() {
     while (true) {
       try { _repairThread.sleep(_timeDelay * 10); }
       catch (InterruptedException ignore) { }
       repairDamage();
     }
   }
 
   /** Ask the Editor to repaint all damaged regions, if the Editor is
    *  ready and there are not Fig transactions in progress. */
   public void repairDamage() {
     Graphics g = _ed.getGraphics();
     if (_lockLevel == 0 && g != null)
       synchronized (LOCK) { if (_lockLevel == 0) paint(_ed, g); }
   }
 
   /** Ask the Editor to repaint all damaged regions, either on screen
    *  or off screen */
   private void paint(Editor ed, Graphics g) {
     long startTime = System.currentTimeMillis();
     if (startTime > deadline) {
       if (Globals.getPrefs().shouldPaintOffScreen()) paintOffscreen(ed, g);
       else paintOnscreen(ed, g);
       Globals.getPrefs().lastRedrawTime(System.currentTimeMillis() - startTime);
       deadline = 0;
     }
   }
 
   /** Ask the Editor to repaint damaged Rectangle's on the Graphics for
    *  the drawing window. This allows the user to see some flicker, but
    *  it gives more feedback that something is happening if the
    *  computer is slow or the diagram is complex. */
   private void paintOnscreen(Editor ed, Graphics g) {
     int F = 16; // fudgefactor, extra repaint area;
     if (ed == null || g == null) return;
     for (int i = 0; i < _nRects; ++i) {
       Rectangle r = _rects[i];
       Graphics offG = g.create();
       //offG.setColor(ed.getBackground());
       // offG.fillRect(r.x, r.y, r.width, r.height);
       offG.clearRect(r.x-F, r.y-F, r.width+F*2, r.height+F*2);
       offG.clipRect(r.x-F-1, r.y-F-1, r.width+F*2+2, r.height+F*2+2);
       ed.paintRect(r, offG);
       offG.dispose();
     }
     removeAllElements();
   }
 
   /** Ask the Editor to repaint damaged Rectangle's on an off screen
    *  Image, and then bitblt that Image to the Graphics used by the
    *  drawing window. This takes more time, but the user will never
    *  see any flicker.
    *  <A HREF="../features.html#redraw_off_screen">
    *  <TT>FEATURE: redraw_off_screen</TT></A>
    */
   private void paintOffscreen(Editor ed, Graphics g) {
     int F = 16; // fudgefactor, extra redraw area;
     Image offscreen;
 
     if (ed == null || g == null) return;
     for (int i = 0; i < _nRects; ++i) {
       Rectangle r = _rects[i];
       r.reshape(r.x-F, r.y-F, r.width+F*2, r.height+F*2);
       offscreen = findReusedImage(r.width, r.height, ed);
       r.width = offscreen.getWidth(null);
       r.height = offscreen.getHeight(null);
       if (offscreen == null) {
 	System.out.println("failed to alloc image!!!");
 	paintOnscreen(ed, g);
 	return;
       }
       Graphics offG = offscreen.getGraphics();
       offG.translate(-r.x, -r.y);
       offG.setColor(ed.getBackground());
       offG.fillRect(r.x, r.y, r.width, r.height);
       //offG.clearRect(r.x, r.y, r.width, r.height);
       offG.clipRect(r.x-1, r.y-1, r.width+2, r.height+2);
       ed.paintRect(r, offG);
       // It is important that paintRect and the various Fig paint()
       // routines do not attempt to call add to register damage. There
       // should not be any reason to do that. It would cause deadlock...
       g.drawImage(offscreen, r.x, r.y, null);
       offG.dispose();
       offscreen.flush();
     }
     removeAllElements();
   }
 
   /** Images used to draw off screen in common sizes. Creating these
    * images on first use and saving them for later uses, avoids all
    * lot of allocation and deallocation of large objects. */
   protected Image image64x64, image256x64, image64x256, image256x256,
     image64x512, image512x64, image512x512;
 
   /** Reply an Image that is as least as big as (x, y), hopefully not
    * too much larger.  If no retained image is suitable, ask the given
    * Editor to make a new one. */
   protected Image findReusedImage(int x, int y, Editor ed) {
     if (x < 64 && y < 64) {
       if (image64x64 == null) image64x64 = ed.createImage(64, 64);
       return image64x64;
     }
     else if (x < 256 && y < 64) {
       if (image256x64 == null) image256x64 = ed.createImage(256, 64);
       return image256x64;
     }
     else if (x < 64 && y < 256) {
       if (image64x256 == null) image64x256 = ed.createImage(64, 256);
       return image64x256;
     }
     else if (x < 256 && y < 256) {
       if (image256x256 == null) image256x256 = ed.createImage(256, 256);
       return image256x256;
     }
     else if (x < 64 && y < 512) {
       if (image64x512 == null) image64x512 = ed.createImage(64, 512);
       return image64x512;
     }
     else if (x < 512 && y < 64) {
       if (image512x64 == null) image512x64 = ed.createImage(512, 64);
       return image512x64;
     }
     else if (x < 512 && y < 512) {
       if (image512x512 == null) image512x512 = ed.createImage(512, 512);
       return image512x512;
     }
     else return ed.createImage(x, y);
   }
 
 } /* end class RedrawManager */
 
