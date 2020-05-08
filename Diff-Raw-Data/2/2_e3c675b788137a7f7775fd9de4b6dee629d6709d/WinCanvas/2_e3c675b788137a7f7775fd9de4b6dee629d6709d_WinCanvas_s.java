 /*
 Copyright (c) 1998, 1999, 2000 Wabasoft  All rights reserved.
 
 This software is furnished under a license and may be used only in accordance
 with the terms of that license. This software and documentation, and its
 copyrights are owned by Wabasoft and are protected by copyright law.
 
 THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
 OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
 INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
 SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.
 
 WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
 MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
 ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
 YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
 */
 
 package waba.applet;
 
 import waba.ui.*;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseEvent;
 
 public class WinCanvas extends java.awt.Canvas 
 	implements KeyListener, MouseListener, MouseMotionListener, Runnable
 {
 	Window win;
 
 
 	public WinCanvas(Window win)
 	{
 		this.win = win;
 		addKeyListener(this);
 		addMouseListener(this);
 		addMouseMotionListener(this);
 
 		// We want to keep the painting thread from locking for too long.
 		// see the comment on run
 		new Thread(this).start();
 
 	}
 
 	public boolean isFocusTraversable(){return true;}
 
 	public int getWabaModifiers(InputEvent event)
 	{
 		int wMods = 0;
 		int jMods = event.getModifiers();
 
 		if ((jMods & InputEvent.SHIFT_MASK) > 0){
 			wMods |= IKeys.SHIFT;
 		}
 		
 		if ((jMods & InputEvent.CTRL_MASK) > 0){
 			wMods |= IKeys.CONTROL;
 		}
 		
 		if ((jMods & InputEvent.ALT_MASK) > 0){
 			wMods |= IKeys.ALT;
 		}
 
 		return wMods;
 	}
 
 	public void keyPressed(java.awt.event.KeyEvent event){
 		int wMods = getWabaModifiers(event);
 		if (((event.getModifiers() & InputEvent.META_MASK) > 0)){
 			wMods |= IKeys.CONTROL;//dima command macintosh
 		}
 
 		int key = 0;
 		//		if(event.isActionKey()){
 		if(true){
 			int action = event.getKeyCode();
 
 			switch (action){
 			case java.awt.event.KeyEvent.VK_PAGE_UP:    key = IKeys.PAGE_UP; break;
 			case java.awt.event.KeyEvent.VK_PAGE_DOWN:  key = IKeys.PAGE_DOWN; break;
 			case java.awt.event.KeyEvent.VK_HOME:       key = IKeys.HOME; break;
 			case java.awt.event.KeyEvent.VK_END:        key = IKeys.END; break;
 			case java.awt.event.KeyEvent.VK_UP:         key = IKeys.UP; break;
 			case java.awt.event.KeyEvent.VK_DOWN:       key = IKeys.DOWN; break;
 			case java.awt.event.KeyEvent.VK_LEFT:       key = IKeys.LEFT; break;
 			case java.awt.event.KeyEvent.VK_RIGHT:      key = IKeys.RIGHT; break;
 			case java.awt.event.KeyEvent.VK_INSERT:     key = IKeys.INSERT; break;
 			case java.awt.event.KeyEvent.VK_ENTER:      key = IKeys.ENTER; break;
 			case java.awt.event.KeyEvent.VK_TAB:        key = IKeys.TAB; break;
 			case java.awt.event.KeyEvent.VK_BACK_SPACE: key = IKeys.BACKSPACE; break;
 			case java.awt.event.KeyEvent.VK_ESCAPE:     key = IKeys.ESCAPE; break;
 			case java.awt.event.KeyEvent.VK_DELETE:     key = IKeys.DELETE; break;
 			case java.awt.event.KeyEvent.VK_F6:         key = IKeys.MENU; break;
 			case java.awt.event.KeyEvent.VK_F7:         key = 76000; break;
 			case java.awt.event.KeyEvent.VK_SHIFT:
 			case java.awt.event.KeyEvent.VK_ALT:
 				return;
 			default :
 				key = event.getKeyChar(); break;
 			}
 		} else {
 			key = event.getKeyChar();
 			switch (key){
 			case 8:
 			case 65288:   // this is hack that works on linux (don't know why)
 				key = IKeys.BACKSPACE;
 				break;
 			case 10:
 				key = IKeys.ENTER;
 				break;
 			case 127:
 			case 65535:  // this is hack that works on linux (don't know why)
 				key = IKeys.DELETE;
 				break;
 			}
 		}
 
 		postWabaEvent(KeyEvent.KEY_PRESS, key, 0, 0, wMods, (int)event.getWhen());
 	}
 		
 	public void keyReleased(java.awt.event.KeyEvent event){}
 
 	public void keyTyped(java.awt.event.KeyEvent event){}
 
 	public void mouseClicked(MouseEvent e){}
 	public void mouseEntered(MouseEvent e){}
 	public void mouseExited(MouseEvent e){}
 	public void mousePressed(MouseEvent e){_handleMouseEvent(e, PenEvent.PEN_DOWN);}
 	public void mouseReleased(MouseEvent e){_handleMouseEvent(e, PenEvent.PEN_UP);}
 
 	public void mouseDragged(MouseEvent e){_handleMouseEvent(e, PenEvent.PEN_MOVE);}
 	public void mouseMoved(MouseEvent e){_handleMouseEvent(e, PenEvent.PEN_MOVE);}
 
 	public void _handleMouseEvent(MouseEvent e, int wabaType)
 	{
 		int wMods = getWabaModifiers(e);		
 		postWabaEvent(wabaType, 0, e.getX(), e.getY(), wMods, (int)e.getWhen());
 	}
 
 	public void postWabaEvent(int type, int key, int x, int y,
 							  int modifiers, int timestamp)
 	{
 		synchronized(Applet.uiLock){
 			win._postEvent(type, key, x, y, modifiers, timestamp);
 		}
 	}
 
 	public void update(java.awt.Graphics g)
 	{
 		paint(g);
 	}
 
 	java.awt.Rectangle paintClipRect = null;
 
 	public void paint(java.awt.Graphics g)
 	{
 		// getClipRect() is missing in the Kaffe distribution for Linux
 		java.awt.Rectangle r = null;		 
 		try { r = g.getClipBounds(); }
 		catch (NoSuchMethodError e) { r = g.getClipRect(); }
 
 		synchronized(this){
 			if(r != null){
 				if(paintClipRect != null){
 					paintClipRect = paintClipRect.union(r);
 				} else {
 					paintClipRect = r;
 				}
 			} else {
 				paintClipRect = getBounds();
 			}
 			notifyAll();
 		}
 	   
 	}
 
 	/**
 	 * 	We have a seperate painting thread that just waits to be notified
 	 *  when the a new paint has been called.  The wait has a timeout for 
 	 *  deadlock protection.  If it timeouts and the paintrect is null.  Then
 	 *  it just loops again.
 	 *  Notice in the paint that if there is a paint waiting then it 
 	 *  takes the union of the clipping rects
 	 *
 	 *  This is necessary because some times a timer event takes a long time
 	 *  to return.  Then the synchronized(Applet.uiLock) would block until
 	 *  the timer event was finished.  In some Vms the painting thread has the
 	 *  highest priority so it would never give up the lock to the timer 
 	 *  thread.  And hence there is a deadlock situation.  
 	 */
 	public void run()
 	{
 		java.awt.Rectangle r = null;
 		
 		while(true){
 			synchronized(this){
 				if(paintClipRect == null){
 					try{
						wait();
 					} catch (InterruptedException e){
 						e.printStackTrace();
 					}
 				}
 			}
 
 			synchronized(Applet.uiLock){			
 				synchronized(this){
 					r = paintClipRect;
 					paintClipRect = null;
 				}
 				if(r != null){
 					win._doPaint(r.x, r.y, r.width, r.height);
 				}
 			}
 		}
 	}
 }
 
