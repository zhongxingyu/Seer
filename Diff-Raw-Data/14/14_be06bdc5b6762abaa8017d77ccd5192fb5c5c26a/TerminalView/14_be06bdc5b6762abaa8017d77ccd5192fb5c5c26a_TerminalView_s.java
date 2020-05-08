 package terminator.view;
 
 import java.awt.*;
 import java.awt.datatransfer.*;
 import java.awt.event.*;
 import java.awt.font.*;
 import java.awt.geom.*;
 import java.util.*;
 import java.util.List;
 import javax.swing.*;
 import e.gui.*;
 import e.util.*;
 
 import terminator.*;
 import terminator.model.*;
 import terminator.terminal.*;
 
 public class TerminalView extends JComponent implements FocusListener, TerminalListener {
 	private static final Stopwatch paintComponentStopwatch = Stopwatch.get("TerminalView.paintComponent");
 	private static final Stopwatch paintStyledTextStopwatch = Stopwatch.get("TerminalView.paintStyledText");
         private static final Font font = new Font("DejaVu Sans Mono", Font.PLAIN, 14);
 	
 	private TerminalModel model;
         private CursorPainter cursorPainter;
 	
 	public TerminalView(TerminalModel model) {
 		ComponentUtilities.disableFocusTraversal(this);
 		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
 		setOpaque(true);
                 setFont(font);
 
                 this.model = model;
                 cursorPainter = new UnfocusedCursorPainter();
                 model.addListener(this);
 
                 setFixedSize(getOptimalViewSize());
 
 		addFocusListener(this);
 		addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent event) {
 				requestFocus();
 			}
 		});
                 addMouseMotionListener(new MouseMotionAdapter() {
                         public void mouseMoved(MouseEvent event) {
                                 setCursor(java.awt.Cursor.getDefaultCursor());
                         }
                 });
 	}
 	
 	public void userIsTyping() {
                 redrawCursorPosition();
                 setCursor(GuiUtilities.INVISIBLE_CURSOR);
 	}
 	
 	/**
 	 * Returns the dimensions of an average character. Note that even though
 	 * we use a fixed-width font, some glyphs for non-ASCII characters can
 	 * be wider than this. See Markus Kuhn's UTF-8-demo.txt for examples,
 	 * particularly among the Greek (where some glyphs are normal-width
 	 * and others are wider) and Japanese (where most glyphs are wide).
 	 * 
 	 * This isn't exactly deprecated, but you should really think hard
 	 * before using it.
 	 */
 	private Dimension getCharUnitSize() {
 		FontMetrics metrics = getFontMetrics(getFont());
 		int width = metrics.charWidth('W');
 		int height = metrics.getHeight();
 		// Avoid divide by zero errors, so the user gets a chance to change their font.
 		if (width == 0) {
 			Log.warn("Insane font width for " + getFont());
 			width = 1;
 		}
 		if (height == 0) {
 			Log.warn("Insane font height for " + getFont());
 			height = 1;
 		}
 		return new Dimension(width, height);
 	}
 	
 	/**
 	 * Returns our size in character units, where 'width' is the number of
 	 * columns and 'height' the number of rows. (In case you were concerned
 	 * about the fact that terminals tend to refer to y,x coordinates.)
 	 */
 	public Dimension getSizeInChars(Dimension paneSize) {
 		Dimension result = paneSize;
 		Insets insets = getInsets();
 		result.width -= (insets.left + insets.right);
 		result.height -= (insets.top + insets.bottom);
 		Dimension character = getCharUnitSize();
 		result.width /= character.width;
 		result.height /= character.height;
 		return result;
 	}
 	
 	// Methods used by TerminalModel in order to update the display.
 	
 	private void setFixedSize(Dimension size) {
 		setMaximumSize(size);
 		setPreferredSize(size);
 		setSize(size);
 		revalidate();
                 repaint();
 	}
 
 	public void contentsChanged(int row) {
 		Point redrawTop = modelToView(row, 0).getLocation();
 		Dimension size = getSize();
 		repaint(redrawTop.x, redrawTop.y, size.width, size.height - redrawTop.y);
 	}
 	
 	public void cursorPositionChanged(terminator.model.Cursor oldCursorPosition, terminator.model.Cursor newCursorPosition) {
                 redrawPosition(oldCursorPosition);
 		redrawCursorPosition();
 	}
 
         public void cursorVisibilityChanged(boolean isVisible) {
                 redrawCursorPosition();
         }
 
         private Rectangle modelToView(terminator.model.Cursor cursor) {
                 return modelToView(cursor.getRow(), cursor.getColumn());
         }
 
 	private Rectangle modelToView(int row, int column) {
                 String line = model.getTextLine(row).getString();
                 String c = column < line.length() ? line.substring(column, column + 1) : " ";
                 String prefix = column < line.length() ? line.substring(0, column) : line;
                 FontMetrics metrics = getFontMetrics(getFont());
                 Insets insets = getInsets();
                 int x = insets.left +
                         metrics.stringWidth(prefix) +
                         (column < line.length() ?
                                 0 :
                                 metrics.stringWidth(" ") * (column - line.length()));
                 int width = metrics.stringWidth(c);
                 int height = metrics.getHeight();
                 int y = insets.top + row * height;
                 return new Rectangle(x, y, width, height);
 	}
 	
 	private Dimension getOptimalViewSize() {
                 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                 Dimension adjustedSize = new Dimension((int)(screenSize.width * 0.85), screenSize.height);
                 Dimension size = getSizeInChars(adjustedSize);
                 size.width = Math.min(size.width, 132);
 		Dimension character = getCharUnitSize();
 		Insets insets = getInsets();
 		final int width = insets.left + size.width * character.width + insets.right;
 		final int height = insets.top + size.height * character.height + insets.bottom;
 		return new Dimension(width, height);
 	}
 	
 	// Redraw code.
 	
 	private void redrawCursorPosition() {
                 redrawPosition(model.getCursor());
 	}
 
         private void redrawPosition(terminator.model.Cursor p) {
                 repaint(modelToView(p));
         }
 	
 	public void paintComponent(Graphics oldGraphics) {
 		Stopwatch.Timer timer = paintComponentStopwatch.start();
 		try {
 			Graphics2D g = (Graphics2D) oldGraphics;
 			
 			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 			
 			FontMetrics metrics = getFontMetrics(getFont());
 			Dimension charUnitSize = getCharUnitSize();
 			
 			Rectangle rect = g.getClipBounds();
 			g.setColor(getBackground());
 			g.fill(rect);
 			
 			// We manually "clip" for performance, but we're quite loose about it.
 			// This avoids accidental pathological cases (hopefully) and doesn't seem to have any significant cost.
 			final int maxX = rect.x + rect.width;
 			final int widthHintInChars = maxX / charUnitSize.width * 2;
 			
 			Insets insets = getInsets();
 			int firstTextLine = (rect.y - insets.top) / charUnitSize.height;
 			int lastTextLine = (rect.y - insets.top + rect.height + charUnitSize.height - 1) / charUnitSize.height;
 			lastTextLine = Math.min(lastTextLine, model.getLineCount() - 1);
 			for (int i = firstTextLine; i <= lastTextLine; i++) {
 				int x = insets.left;
 				int baseline = insets.top + charUnitSize.height * (i + 1) - metrics.getMaxDescent();
                                 for (StyledText text : getLineStyledText(i, widthHintInChars)) {
                                         if (x >= maxX) // XXX: Off by one here?
                                                 break;
                                         x += paintStyledText(g, metrics, text, x, baseline);
                                 }
 			}
                         cursorPainter.paint(g, firstTextLine, lastTextLine);
 		} finally {
 			timer.stop();
 		}
 	}
 	
 	private List<StyledText> getLineStyledText(int line, int widthHintInChars) {
 		return model.getTextLine(line).getStyledTextSegments(widthHintInChars);
 	}
 	
         private abstract class CursorPainter {
                 public void paint(Graphics2D g, int firstLine, int lastLine) {
                         terminator.model.Cursor p = model.getCursor();
 
                         if (!p.isInsideLines(firstLine, lastLine))
                                 return;
                         g.setColor(Color.black);
                         paintCursor(g, modelToView(p));
                 }
 
                 protected abstract void paintCursor(Graphics2D g, Rectangle r);
         }
 
         private class FocusedCursorPainter extends CursorPainter {
                 @Override
                 protected void paintCursor(Graphics2D g, Rectangle r) {
                         g.setXORMode(Color.white);
                         g.fill(r);
                         g.setPaintMode();
                 }
         }
 
 
         private class UnfocusedCursorPainter extends CursorPainter {
                 @Override
                 protected void paintCursor(Graphics2D g, Rectangle r) {
                         g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
                 }
         }
 
 	/**
 	 * Paints the text. Returns how many pixels wide the text was.
 	 */
 	private int paintStyledText(Graphics2D g, FontMetrics metrics, StyledText text, int x, int y) {
 		Stopwatch.Timer timer = paintStyledTextStopwatch.start();
 		try {
 			Style style = text.getStyle();
 			Color foreground = style.getForeground();
 			Color background = style.getBackground();
 			
 			if (style.isReverseVideo()) {
 				Color oldForeground = foreground;
 				foreground = background;
 				background = oldForeground;
 			}
 			
 			int textWidth = metrics.stringWidth(text.getText());
 			if (background.equals(getBackground()) == false) {
 				g.setColor(background);
 				// Special continueToEnd flag used for drawing the backgrounds of Highlights which extend over the end of lines.
 				// Used for multi-line selection.
 				int backgroundWidth = text.continueToEnd() ? (getSize().width - x) : textWidth;
 				g.fillRect(x, y - metrics.getMaxAscent() - metrics.getLeading(), backgroundWidth, metrics.getHeight());
 			}
 			if (style.isUnderlined()) {
 				g.setColor(new Color(foreground.getRed(), foreground.getGreen(), foreground.getBlue(), 128));
 				g.drawLine(x, y + 1, x + textWidth, y + 1);
 			}
 			g.setColor(foreground);
 			g.drawString(text.getText(), x, y);
 			return textWidth;
 		} finally {
 			timer.stop();
 		}
 	}
 	
 	public void focusGained(FocusEvent event) {
                 setCursorPainter(new FocusedCursorPainter());
 	}
 	
 	public void focusLost(FocusEvent event) {
                 setCursorPainter(new UnfocusedCursorPainter());
 	}
 
         private void setCursorPainter(CursorPainter cursorPainter) {
                 this.cursorPainter = cursorPainter;
                 redrawCursorPosition();
         }
 }
