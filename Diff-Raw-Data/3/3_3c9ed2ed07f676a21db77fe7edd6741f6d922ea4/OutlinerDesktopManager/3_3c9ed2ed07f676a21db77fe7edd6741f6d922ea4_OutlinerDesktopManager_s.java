 /**
  * Copyright (C) 2000 Maynard Demmon, maynard@organic.com
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
  
 package com.organic.maynard.outliner;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Window;
 import java.awt.datatransfer.*;
 
 import java.io.*;
 import java.util.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 public class OutlinerDesktopManager extends DefaultDesktopManager {
 	
 	// Directions
 	public static final int NORTH = 1;
 	public static final int NORTHEAST = 2;
 	public static final int EAST = 3;
 	public static final int SOUTHEAST = 4;
 	public static final int SOUTH = 5;
 	public static final int SOUTHWEST = 6;
 	public static final int WEST = 7;
 	public static final int NORTHWEST = 8;
 	
 	public static final int ICON_WIDTH = 150;
 	public static final int ICON_HEIGHT = 25;
 	
 	private boolean isDragging = false;
 	
 	//JInternalFrame State
 	private int resizeDirection = 0;
 	private int startingX = 0;
 	private int startingWidth = 0;
 	private int startingY = 0;
 	private int startingHeight = 0;
 	
 	//JDesktopPane State
 	//private int desktopWidth = 0;
 	//private int desktopHeight = 0;
 	
 	// The Constructor
 	public OutlinerDesktopManager() {
 		super();
 	}
 	
 	public boolean isDragging() {return isDragging;}
 	public boolean isMaximized() {return Preferences.IS_MAXIMIZED.cur;}
 	public void setMaximized(boolean b) {Preferences.IS_MAXIMIZED.cur = b;}
 	
 	// DesktopManagerInterface
 	public void beginResizingFrame(JComponent f, int direction) {
 		//System.out.println("beginResizingFrame");
 		resizeDirection = direction;
 		startingX = f.getLocation().x;
 		startingWidth = f.getWidth();
 		startingY = f.getLocation().y;
 		startingHeight = f.getHeight();
 		super.beginResizingFrame(f,direction);
 	}
 
 	public void resizeFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
 		//System.out.println("resizingFrame");
 		// Ensure a minimum size for the window.
 		if (f instanceof JInternalFrame) {
 			int minWidth = OutlinerDocument.MIN_WIDTH;
 			int minHeight = OutlinerDocument.MIN_HEIGHT;
 			
 			if (f instanceof FindReplaceFrame) {
 				minWidth = FindReplaceFrame.MIN_WIDTH;
 				minHeight = FindReplaceFrame.MIN_HEIGHT;
 			}
 			
 			if (newWidth < minWidth) {
 				newWidth = minWidth;
 				if ((resizeDirection == NORTHWEST) || (resizeDirection == WEST) || (resizeDirection == SOUTHWEST)) {
 					newX = (startingX + startingWidth - minWidth);
 				} else {
 					newX = f.getLocation().x;
 				}
 			}
 			
 			if (newHeight < minHeight) {
 				newHeight = minHeight;
 				if ((resizeDirection == NORTHEAST) || (resizeDirection == NORTH) || (resizeDirection == NORTHWEST)) {
 					newY = (startingY + startingHeight - minHeight);
 				} else {
 					newY = f.getLocation().y;
 				}
 			}
 		}
 		
 		super.resizeFrame(f,newX,newY,newWidth,newHeight);
 		updateDesktopSize(false);
 	}
 
 	public void endResizingFrame(JComponent f) {
 		//System.out.println("endResizingFrame");
 		super.endResizingFrame(f);
 	}
 	
 	public void beginDraggingFrame(JComponent f) {
 		//System.out.println("beginDraggingFrame");
 		isDragging = true;
 		//desktopWidth = Outliner.desktop.getWidth();
 		//desktopHeight = Outliner.desktop.getHeight();
 		super.beginDraggingFrame(f);
 	}
 	
 	public void dragFrame(JComponent f, int newX, int newY) {
 		//System.out.println("dragFrame");
 		super.dragFrame(f,newX,newY);
 		updateDesktopSize(false);
 	}
 
 	public void endDraggingFrame(JComponent f) {
 		//System.out.println("endDraggingFrame");
 		isDragging = false;
 		super.endDraggingFrame(f);
 		updateDesktopSize(true);
 	}
 
 	public static boolean activationBlock = false;
 	
 	public void activateFrame(JInternalFrame f) {
 		if (activationBlock) {return;}
 		
 		//System.out.println("activateFrame " + f.getTitle());
 		super.activateFrame(f);
 
 		if (f instanceof OutlinerDocument) {
 			OutlinerDocument doc = (OutlinerDocument) f;
 			
 			Outliner.setMostRecentDocumentTouched(doc);
 			
 			// Update the Menus
 			Outliner.updateSaveMenuItem();
 			Outliner.updateSaveAllMenuItem();
 			
 			EditMenu.updateEditMenu(doc);
 			OutlineMenu.updateOutlineMenu(doc);
 			SearchMenu.updateSearchMenu(doc);
 			WindowMenu.updateWindowMenu(doc);
 		}
 	}
 
 	public void deactivateFrame(JInternalFrame f) {
 		//System.out.println("deactivateFrame");
 		super.deactivateFrame(f);
 	}
 
 	public void openFrame(JInternalFrame f) {
 		//System.out.println("openFrame");
 		super.openFrame(f);
 		updateDesktopSize(false);
 	}
 
 	public void closeFrame(JInternalFrame f) {
 		//System.out.println("closeFrame");
 		super.closeFrame(f);
 		updateDesktopSize(false);
 	}
 
 	public void iconifyFrame(JInternalFrame f) {
 		//System.out.println("iconifyFrame");
 		super.iconifyFrame(f);
 		f.getDesktopIcon().setSize(ICON_WIDTH,ICON_HEIGHT);
 	}
 
 	public void deiconifyFrame(JInternalFrame f) {
 		//System.out.println("deiconifyFrame");
 		super.deiconifyFrame(f);
 		updateDesktopSize(false);
 	}
 
 	public void maximizeFrame(JInternalFrame f) {
 		//System.out.println("maximizeFrame");
 		setMaximized(true);
 		super.maximizeFrame(f);
 		
 		// Disable Stack Menu Item
 		Outliner.menuBar.windowMenu.windowStackItem.setEnabled(false);
 		
 		// Remove the border
 		((OutlinerDocument) f).hideBorder();
 		
 		// Make sure JInternalFrame is sized to the viewport, not the desktop.
 		f.setSize(new Dimension(Outliner.jsp.getViewport().getWidth(), Outliner.jsp.getViewport().getHeight()));
 		updateDesktopSize(false);
 	}
 
 	public void minimizeFrame(JInternalFrame f) {
 		//System.out.println("minimizeFrame");
 		setMaximized(false);
 		super.minimizeFrame(f);
 
 		// Enable Stack Menu Item
 		Outliner.menuBar.windowMenu.windowStackItem.setEnabled(true);
 
 		// Restore the border
 		((OutlinerDocument) f).showBorder();
 		
 		updateDesktopSize(false);
 	}
 
 	public void setBoundsForFrame(JComponent f, int newX, int newY, int newWidth, int newHeight) {
 		//System.out.println("setBoundsForFrame");
  		if (!(f instanceof JInternalFrame)) {
 			newWidth = ICON_WIDTH;
 			newHeight = ICON_HEIGHT;
 						
 			newX = findNearest(newX,ICON_WIDTH) * ICON_WIDTH;
 			newY = findNearest(newY,ICON_HEIGHT) * ICON_HEIGHT;
 		}
 		
 		super.setBoundsForFrame(f,newX,newY,newWidth,newHeight);
 	}
 
 		
 	// Utility Methods
 	private int findNearest(int value, int partition) {
 		return value/partition;
 	}
 
 	public void updateDesktopSize(boolean repaint) {
 		//Outliner.desktop.setSize(Outliner.desktop.getPreferredSize());
 		
 		Outliner.jsp.revalidate();
 		Outliner.jsp.validate();
 		
 		//System.out.println(Outliner.desktop.getSize());
 		
 		Outliner.jsp.getHorizontalScrollBar().repaint();
 		Outliner.jsp.getVerticalScrollBar().repaint();
 		
 		if (repaint) {
 			Outliner.desktop.repaint();
 		}
 	}
 }
