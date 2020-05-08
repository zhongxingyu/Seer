 ///////////////////////////////////////////////////////////////////////////////
 // JMouseWheel: Mouse wheel support for Java applications on Win32 platforms
 // Copyright (C) 2001 Davanum Srinivas (dims@geocities.com)
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, see http://www.gnu.org
 ///////////////////////////////////////////////////////////////////////////////
 
 package gui;
 
 import javax.swing.*;
 import java.awt.*;
 import com.organic.maynard.outliner.DummyJScrollPane;
 
 /**
  * Helper class to the JMouseWheelDialog and JMouseWheelFrame.  Encapsulates the
  * scrolling code into one object shared by both types of Window ancestors.
  */
 public abstract class JMouseWheelSupport {
 
 	private static int minScrollDistance = 15;
 	
 	public static void setMinScrollDistance(int i) {
 		minScrollDistance = i;
 	}
 
 	public static int getMinScrollDistance() {
 		return minScrollDistance;
 	}
 	
 	static void notifyMouseWheel(Component owner, int scrollSpeed, short fwKeys,short zDelta,long xPos, long yPos)
 	{
 		// Convert screen coordinates to component specific offsets.
 		Point p = new Point((int)xPos,(int)yPos);
 		SwingUtilities.convertPointFromScreen(p, owner);
 
 		// Find the embedded Swing component which should receive the scroll messages
 		Component c = SwingUtilities.getDeepestComponentAt(owner, p.x, p.y);
 
 		// Get the scroll pane for the widget, if any
 		while ( c != null ) {
 			if ( c instanceof JScrollPane ) {
 				// Get the vertical scrollbar for the scroll pane.
 				JScrollBar scrollBar = null;
 				if (c instanceof DummyJScrollPane) {
 					scrollBar = ((DummyJScrollPane) c).getVerticalScrollBarProxy(); // MD: Modifed to use a different method name so that other processes don't get access to the scrollbar by when they shouldn't.
 				} else {
 					scrollBar = ((JScrollPane) c).getVerticalScrollBar(); // MD: Modifed to use a different method name so that other processes don't get access to the scrollbar by when they shouldn't.
 				}
 				BoundedRangeModel model = scrollBar.getModel();
 
 				// If there's room to scroll, update this scrollbar and return.
 				if ( model.getMinimum() + model.getExtent() != model.getMaximum() ) {
 					// Get the current value and set the new value depending on
 					// the direction of the mouse wheel.
 					int nValue = scrollBar.getValue();
 					int nIncrement = scrollBar.getUnitIncrement((zDelta > 0) ? -1 : 1);
 					nIncrement = Math.max( nIncrement, minScrollDistance ) * scrollSpeed; 	// (15 is not too annoying yet still less than table row increment)
 					nValue = nValue + ((zDelta > 0) ? -nIncrement : nIncrement);
 					SwingUtilities.invokeLater(new ScrollBarAdjuster(scrollBar, nValue));
 					return;
                 }
             } else if ( c instanceof JComboBox ) {
 				JComboBox cb = (JComboBox) c;
 				
 				// if the mouse is over a combo box, we
 				// should change it's scroll value too...
 				
 				// (only adjust if it's enabled...)
 				if (!cb.isEnabled())
 					return;
 				
 				SwingUtilities.invokeLater(new ComboBoxAdjuster(cb, zDelta));
 				return;
 			}
 			
 			// See if parent is a scroll pane that can scroll
 			c = c.getParent();
         }
 
 	}
 
 	// utility class to change the scrollbar position, from the event thread
 	private static class ScrollBarAdjuster implements Runnable {
 		private JScrollBar scrollBar;
 		private int value;
 		public ScrollBarAdjuster(JScrollBar scrollBar, int value) {
 			this.scrollBar=scrollBar;
 			this.value=value;
 		}
 		public void run() {
 			scrollBar.setValue(value);
 		}
 	}
 
 	// utility class to change the combo box selection, from the event thread
 	private static class ComboBoxAdjuster implements Runnable
 	{
 		private JComboBox comboBox;
 		private int zDelta;
 
 		public ComboBoxAdjuster(JComboBox aComboBox, int aDelta)
 		{
 			comboBox = aComboBox;
 			zDelta   = aDelta;
 		}
 
 		public void run()
 		{
 			int oldIdx = comboBox.getSelectedIndex();
 			if (oldIdx < 0)
 				oldIdx = 0;
 
 			int newIdx = oldIdx;
 
 			// adjust the scroll increment
 			if (zDelta > 0)
 				newIdx--;
 			else
 				newIdx++;
 
 			// check we are still in bounds
 			if (newIdx >= comboBox.getItemCount())
 				newIdx = comboBox.getItemCount() - 1;
 			else if (newIdx < 0)
 				newIdx = 0;
 
 			// if we still have a valid selection index, use it...
 			if (newIdx != oldIdx)
 				comboBox.setSelectedIndex(newIdx);
 		}
 	}
 }
 
 /*
  * $Log$
  * Revision 1.2  2001/09/28 07:50:22  maynardd
  * Had to make this explicitly use DummyJScrollPane to fix the jump scrolling bug.
  *
  * Revision 1.1  2001/09/21 07:37:13  maynardd
  * modified to let us get at scroll speed.
  *
  * Revision 1.1  2001/08/13 02:15:51  davidconnard
  * Added support for JComboBoxes.  Moved shared scrolling code into
  * a helper class - JMouseWheelSupport so that it does not have
  * to be duplicated between the Frame and Dialog flavours.
  *
  */
 
