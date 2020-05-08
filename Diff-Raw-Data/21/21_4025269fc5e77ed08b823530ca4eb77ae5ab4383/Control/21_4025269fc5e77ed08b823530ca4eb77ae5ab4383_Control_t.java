 /*
 Copyright (c) 1998, 1999 Wabasoft  All rights reserved.
 
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
 
 package waba.ui;
 
 import waba.fx.*;
 import waba.sys.*;
 
 /**
  * Control is the base class for user-interface objects.
  */
 
 public class Control
 {
 /** The control's x location */
 protected int x;
 /** The control's y location */
 protected int y;
 /** The control's width */
 protected int width;
 /** The control's height */
 protected int height;
 /** The parent of the control. */
 protected Container parent;
 /** The control's next sibling. */
 protected Control next;
 /** The control's previous sibling. */
 protected Control prev;
 
 /**
  * Adds a timer to a control. Each time the timer ticks, a TIMER
  * event will be posted to the control. The timer does
  * not interrupt the program during its execution at the timer interval,
  * it is scheduled along with application events. The timer object
  * returned from this method can be passed to removeTimer() to
  * remove the timer. Under Windows, the timer has a minimum resolution
  * of 55ms due to the native Windows system clock resolution of 55ms.
  *
  * @param millis the timer tick interval in milliseconds
  * @see ControlEvent
  */
 public Timer addTimer(int millis)
 	{
 	MainWindow win = MainWindow.getMainWindow();
 	return win.addTimer(this, millis);
 	}
 
 /**
  * Removes a timer from a control. True is returned if the timer was
  * found and removed and false is returned if the timer could not be
  * found (meaning it was not active).
  */
 public boolean removeTimer(Timer timer)
 	{
 	MainWindow win = MainWindow.getMainWindow();
 	return win.removeTimer(timer);
 	}
 
 /** Returns the font metrics for a given font. */
 public FontMetrics getFontMetrics(Font font)
 	{
 	MainWindow win = MainWindow.getMainWindow();
 	return win.getFontMetrics(font);
 	}
 
 /** Sets or changes a control's position and size. */
 public void setRect(int x, int y, int width, int height)
 	{
 	if (parent != null)
 		repaint();
 	this.x = x;
 	this.y = y;
 	this.width = width;
 	this.height = height;
 	if (parent != null)
 		repaint();
 	}
 
 /**
  * Returns a copy of the control's rectangle. A control's rectangle
  * defines its location and size.
  */
 public Rect getRect()
 	{
 	return new Rect(this.x, this.y, this.width, this.height);
 	}
 
 /** Returns the control's parent container. */
 public Container getParent()
 	{
 	return parent;
 	}
 
 /** Returns the next child in the parent's list of controls. */
 public Control getNext()
 	{
 	return next;
 	}
 
 /**
  * Returns true if the given x and y coordinate in the parent's
  * coordinate system is contained within this control.
  */
 public boolean contains(int x, int y)
 	{
 	int rx = this.x;
 	int ry = this.y;
 	if (x < rx || x >= rx + this.width || y < ry || y > ry + this.height)
 		return false;
 	return true;
 	}
 
 /** Redraws the control. */
 public void repaint()
 	{
 	int x = 0;
 	int y = 0;
 	Control c = this;
 	while (!(c instanceof Window))
 		{
 		x += c.x;
 		y += c.y;
 		c = c.parent;
 		if (c == null)
 			return;
 		}
 	Window win = (Window)c;
 	win.damageRect(x, y, this.width, this.height);
 	}
 
 /**
  * Creates a Graphics object which can be used to draw in the control.
  * This method finds the surface associated with the control, creates
  * a graphics assoicated with it and translates the graphics to the
  * origin of the control. It does not set a clipping rectangle on the
  * graphics.
  */
 public Graphics createGraphics()
 	{
 	int x = 0;
 	int y = 0;
 	Control c = this;
 	while (!(c instanceof Window))
 		{
 		x += c.x;
 		y += c.y;
 		c = c.parent;
 		if (c == null)
 			return null;
 		}
 	Window win = (Window)c;	
	Graphics g = new Graphics(win);
 	g.translate(x, y);
 	return g;
 	}
 
 /**
  * Posts an event. The event pass will be posted to this control
  * and all the parent controls of this control (all the containers
  * this control is within).
  * @see Event
  */
 public void postEvent(Event event)
 	{
 	Control c;
 
 	c = this;
 	while (c != null)
 		{
 		c.onEvent(event);
  		c = c.parent;
 		}
 	}
 
 /**
  * Called to process key, pen, control and other posted events.
  * @param event the event to process
  * @see Event
  * @see KeyEvent
  * @see PenEvent
  */
 public void onEvent(Event event)
 	{
 	}
 
 /**
  * Called to draw the control. When this method is called, the graphics
  * object passed has been translated into the coordinate system of the
  * control and the area behind the control has
  * already been painted. The background is painted by the top-level
  * window control.
  * @param g the graphics object for drawing
  * @see Graphics
  */
 public void onPaint(Graphics g)
 	{
 	}
 }
 
