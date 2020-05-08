package org.rsbot.script.util;
 
 import java.awt.*;
 
 public class PaintUIUtil {
 	private Graphics2D g2;
 	boolean checked = false;
 
 	public PaintUIUtil(Graphics render) {
 		g2 = (Graphics2D) render;
 	}
 
 	/**
 	 * Draws a check box for interactive paints
 	 *
 	 * @param boxRect   Rect to draw checkBox at
 	 * @param text      Text to draw after checkBox
 	 * @param textColor Text color
 	 * @author jtryba
 	 */
 	public void drawCheckBox(final Rectangle boxRect, final String text, final Color textColor) {
 		g2.setColor((checked ? Color.green : Color.red));
 		g2.fillRect(boxRect.x, boxRect.y, boxRect.width, boxRect.height);
 		g2.setColor(textColor);
 		g2.drawString(text, boxRect.x + boxRect.width + 3, boxRect.y + boxRect.height - 2);
 		g2.setColor(Color.black);
 		if (checked) {
 			g2.drawLine(boxRect.x, boxRect.y, boxRect.x + boxRect.width, boxRect.y + boxRect.height);
 			g2.drawLine(boxRect.x + boxRect.width, boxRect.y, boxRect.x, boxRect.y + boxRect.height);
 		}
 		g2.drawRect(boxRect.x, boxRect.y, boxRect.width, boxRect.height);
 	}
 }
