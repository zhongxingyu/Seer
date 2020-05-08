 package kkckkc.jsourcepad.theme.osx;
 
 import sun.swing.SwingUtilities2;
 
import javax.swing.*;
 import javax.swing.plaf.ComponentUI;
 import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
 
 public class DocumentTabbedPaneUI extends BasicTabbedPaneUI {
 	private static final Insets NO_INSETS = new Insets(3, 0, 0, 0);
 
 	private Font boldFont;
 	private FontMetrics boldFontMetrics;
 	private Color fillColor;
 
 	private boolean focused = true;
 
 	public static ComponentUI createUI(JComponent component) {
 		return new DocumentTabbedPaneUI();
 	}
 
 	protected void installDefaults() {
 		super.installDefaults();
 		tabAreaInsets.left = 0;
 		selectedTabPadInsets = new Insets(0, 0, 0, 0);
 		tabInsets = selectedTabPadInsets;
 		tabAreaInsets = selectedTabPadInsets;
 
 		tabPane.setFont(tabPane.getFont().deriveFont(11f));
 		
 		Color background = tabPane.getBackground();
 		fillColor = background.darker();
 
 		boldFont = tabPane.getFont().deriveFont(Font.BOLD);
 		boldFontMetrics = tabPane.getFontMetrics(boldFont);
 	}
 
 	public int getTabRunCount(JTabbedPane pane) {
 		return 1;
 	}
 
 	protected Insets getContentBorderInsets(int tabPlacement) {
 		return NO_INSETS;
 	}
 
 	protected int calculateTabHeight(int tabPlacement, int tabIndex,
 			int fontHeight) {
 		int vHeight = fontHeight;
 		if (vHeight % 2 > 0) {
 			vHeight += 1;
 		}
 		vHeight += 4;
 		return vHeight;
 	}
 
 	protected int calculateTabWidth(int tabPlacement, int tabIndex,
 			FontMetrics metrics) {
 		return super.calculateTabWidth(tabPlacement, tabIndex, metrics)
 				+ metrics.getHeight();
 	}
 
 	protected void paintTabBackground(Graphics g, int tabPlacement,
 			int tabIndex, int x, int y, int w, int h, boolean isSelected) {
 		Graphics2D g2d = (Graphics2D) g;
 		
 		Color c1 = new Color(218, 218, 218);
 		Color c2 = new Color(182, 182, 182);
 		
 		if (isSelected) {
 			c1 = new Color(182, 182, 182);
 			c2 = new Color(152, 152, 152);
 		}
 		
 		if (! focused) {
 			if (isSelected) {
 				c1 = new Color(207, 207, 207);
 				c2 = new Color(187, 187, 187);
 			} else {
 				c1 = new Color(222, 222, 222);
 				c2 = new Color(199, 199, 199);
 			}
 		}
 		
 		GradientPaint gradient = new GradientPaint(0,0,c1,0,h,c2,true);
 	    g2d.setPaint(gradient);
 	    g2d.fillRect(x, y, w, h + 2);
 
 		g.setColor(new Color(1, 1, 1, 0.3f));
 		if (tabIndex > 0) 
 			g.drawRect(x + 1, y, w - 2, h + 1);
 		else 
 			g.drawRect(x, y, w - 1, h + 1);
 	}
 
 	protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex,
 			int x, int y, int w, int h, boolean isSelected) {
 		g.setColor(new Color(121, 121, 121));
 		if (tabIndex > 0) 
 			g.drawLine(x, y, x, y + h + 1);
 		g.drawLine(x + w, y, x + w, y + h + 1);
 	}
 
 	protected void paintContentBorderTopEdge(Graphics g, int tabPlacement,
 			int selectedIndex, int x, int y, int w, int h) {
 	}
 
 	protected void paintContentBorderRightEdge(Graphics g, int tabPlacement,
 			int selectedIndex, int x, int y, int w, int h) {
 		// Do nothing
 	}
 
 	protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement,
 			int selectedIndex, int x, int y, int w, int h) {
 		// Do nothing
 	}
 
 	protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement,
 			int selectedIndex, int x, int y, int w, int h) {
 		// Do nothing
 	}
 
 	protected void paintFocusIndicator(Graphics g, int tabPlacement,
 			Rectangle[] rects, int tabIndex, Rectangle iconRect,
 			Rectangle textRect, boolean isSelected) {
 		// Do nothing
 	}
 
 	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
 		int tw = tabPane.getBounds().width;
 		
 		Graphics2D g2d = (Graphics2D) g;
 		
 		g2d.setColor(new Color(237, 237, 237));
 		g2d.fill(g2d.getClipBounds());
 		
 		Color c1 = new Color(218, 218, 218);
 		Color c2 = new Color(182, 182, 182);
 
 		int h = boldFontMetrics.getHeight() + 6; 
 		
 		GradientPaint gradient = new GradientPaint(0,0,c1,0,h,c2,true);
 	    g2d.setPaint(gradient);
 	    g2d.fillRect(0, 0, tw, h);
 		
 		g.setColor(fillColor);
 		
 		g.setColor(new Color(1, 1, 1, 0.3f));
 		g.drawLine(0, 0, tw, 0);
 		g.drawLine(0, h - 1, tw, h - 1);
 
 		g.setColor(new Color(85, 85, 85));
 		g.drawLine(0, h, tw, h);
 		
 		super.paintTabArea(g, tabPlacement, selectedIndex);
 	}
 
 	protected void paintText(Graphics g, int tabPlacement, Font font,
 			FontMetrics metrics, int tabIndex, String title,
 			Rectangle textRect, boolean isSelected) {
 		textRect.y += 1;
 		if (isSelected) {
 			int vDifference = (int) (boldFontMetrics.getStringBounds(title, g)
 					.getWidth())
 					- textRect.width;
 			textRect.x -= (vDifference / 2);
 			
 			paintText2(g, tabPlacement, boldFont, boldFontMetrics,
 					tabIndex, title, textRect, isSelected);
 		} else {
 			paintText2(g, tabPlacement, font, metrics, tabIndex, title,
 					textRect, isSelected);
 		}
 	}
 
 	protected void paintText2(Graphics g, int tabPlacement, Font font,
 			FontMetrics metrics, int tabIndex, String title,
 			Rectangle textRect, boolean isSelected) {
 
 		g.setFont(font);
 
 		// plain text
 		int mnemIndex = tabPane.getDisplayedMnemonicIndexAt(tabIndex);
 
 		if (tabPane.isEnabled() && tabPane.isEnabledAt(tabIndex)) {
 			Color fg = new Color(68, 68, 68);
 			Color shadow = new Color(220, 220, 220);
 			if (isSelected && focused) {
 				fg = Color.black;
 				shadow = new Color(192, 192, 192);
 			}
 			
 			g.setColor(shadow);
 			SwingUtilities2.drawStringUnderlineCharAt(tabPane, g, title,
 					mnemIndex, textRect.x, textRect.y + metrics.getAscent() + 1);
 
 			g.setColor(fg);
 			SwingUtilities2.drawStringUnderlineCharAt(tabPane, g, title,
 					mnemIndex, textRect.x, textRect.y + metrics.getAscent());
 
 		} else { // tab disabled
 			g.setColor(tabPane.getBackgroundAt(tabIndex).brighter());
 			SwingUtilities2.drawStringUnderlineCharAt(tabPane, g, title,
 					mnemIndex, textRect.x, textRect.y + metrics.getAscent());
 			g.setColor(tabPane.getBackgroundAt(tabIndex).darker());
 			SwingUtilities2.drawStringUnderlineCharAt(tabPane, g, title,
 					mnemIndex, textRect.x - 1, textRect.y + metrics.getAscent()
 							- 1);
 
 		}
 	}
 	
 	protected int getTabLabelShiftY (int tabPlacement, int tabIndex, boolean isSelected) {
 		return 0;
 	}
 
 	public void setFocused(boolean focused) {
 		this.focused  = focused;
 	}
 }
