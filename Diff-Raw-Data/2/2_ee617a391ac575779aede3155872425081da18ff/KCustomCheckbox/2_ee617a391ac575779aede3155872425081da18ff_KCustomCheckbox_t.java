 /*
  * Copyright 2010 Dan Fabulich
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License. 
  */
 
 package com.choiceofgames.kindle;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.apache.log4j.Logger;
 
 import com.amazon.kindle.kindlet.ui.KButton;
 
 /**
  * Custom checkbox for use in Kindle KDK.
  * @author dfabulich
  *
  */
 public class KCustomCheckbox extends KButton implements ActionListener {
 	
 	private boolean selected = false;
 	final KCustomCheckboxGroup group;
 
 	/** Is this box checked? */
 	public boolean isSelected() {
 		return selected;
 	}
 
 	/** Check/uncheck this box */
 	public void setSelected(boolean selected) {
 		if (this.selected == selected) return;
 		if (selected && group != null) group.setSelected(this);
 		this.selected = selected;
 		repaint();
 	}
 
 	private static final long serialVersionUID = 8105922331821759692L;
	private static Logger logger = Logger.getLogger(KCustomCheckbox.class);
 	// Should these be configurable?  Probably.  Just steal the code if you want to tweak these numbers.
 	private static final int padding = 1, border = 2;
 	
 	/** Create a checkbox */
 	public KCustomCheckbox() {
 		this(null);
 	}
 	
 	/** Create a radio button belonging to a group */
 	public KCustomCheckbox(KCustomCheckboxGroup group) {
 		this.group = group;
 		addActionListener(this);
 	}
 	
 	public Dimension getPreferredSize() {
 		return getMinimumSize();
 	}
 	
 	public Dimension getMinimumSize() {
 		int d = getFontMetrics(getFont()).getMaxAscent();
 		d += (padding + border) * 2;
 		logger.debug("declared minimum: " + d);
 		return new Dimension(d, d);
 	}
 	
 	public void paint(Graphics g) {
 		logger.debug("I'm painting");
 		//super.paint(g);
         Dimension size = getSize();
         // Max Ascent ~ maximum height of a letter from the baseline
         // We'll use maxAscent to size our checkbox
         int maxAscent = g.getFontMetrics().getMaxAscent();
 		int diameter = maxAscent - border;
 
 		int x = padding + border;
         int y = padding + border;
         logger.debug("size.width " + size.width);
         logger.debug("size.height " + size.height);
         logger.debug("x " + x);
         logger.debug("y " + y);
         logger.debug("d " + diameter);
 
         g.setColor(Color.black);
         if (group != null) {
         	// radio button
         	if (selected) g.fillOval(x, y, diameter, diameter);
             g.drawOval(x, y, diameter, diameter);
             logger.debug("g.drawOval(x, y, d, d);" + x + "," + y + "," + diameter);
         } else {
         	// checkbox
         	if (selected) {
         		// draw X
         		g.drawLine(x, y, x+diameter, y+diameter);
         		g.drawLine(x, y+diameter, x+diameter, y);
         	}
             g.drawRect(x, y, diameter, diameter);
         }
         
         if (!isFocusOwner()) {
         	g.setColor(Color.white);
         }
         for (int i = 0; i < border; i++) {
         	// Draw the border as a sequence of self-contained rectangles
         	// The first border is the full size, the next border is 1px smaller, and so on
         	int rectWidth = diameter+padding*2+border*2-i*2;
         	logger.debug("g.drawRect(i, i, rectWidth, rectWidth);" + i + "," + rectWidth);
         	g.drawRect(i, i, rectWidth, rectWidth);
         }
 	}
 
 	/** Select this box */
 	public void actionPerformed(ActionEvent e) {
 		setSelected(!selected);
 	}
 	
 	
 }
