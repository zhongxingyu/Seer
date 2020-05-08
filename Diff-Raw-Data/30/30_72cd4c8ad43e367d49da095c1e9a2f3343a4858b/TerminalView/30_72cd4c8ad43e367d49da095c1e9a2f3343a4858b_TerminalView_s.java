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
 
 public class TerminalView extends JComponent implements FocusListener, Observer {
 	private static final Stopwatch paintComponentStopwatch = Stopwatch.get("TerminalView.paintComponent");
 	private static final Stopwatch paintStyledTextStopwatch = Stopwatch.get("TerminalView.paintStyledText");
         private static final Font font = new Font("DejaVu Sans Mono", Font.PLAIN, 14);
 	
 	private TerminalModel model;
 	private Location cursorPosition;
 	private boolean hasFocus = false;
 	private boolean displayCursor;
 	
 	public TerminalView() {
                 /* TODO: Or just set to 80×24 and then maximize the window and
                  * have the model resize itself? */
                 Rectangle2D charBounds = font.getMaxCharBounds(new FontRenderContext(null, true, true));
                 Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                 int columns = (int)(screenSize.getWidth() * 0.8 / charBounds.getWidth());
                 int rows = (int)(screenSize.getHeight() * 0.9 / charBounds.getHeight());
 		model = new TerminalModel(new Dimension(columns, rows));
                 cursorPosition = model.getCursorPosition();
                 displayCursor = model.getCursorVisible();
                 model.addObserver(this);
 
 		ComponentUtilities.disableFocusTraversal(this);
 		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
 		setOpaque(true);
                 setFont(font);
                 sizeChanged();
 		addFocusListener(this);
 		addMouseListener(new MouseAdapter() {
 			public void mouseClicked(MouseEvent event) {
 				requestFocus();
 			}
 		});
                 addMouseMotionListener(new MouseMotionAdapter() {
                         public void mouseMoved(MouseEvent event) {
                                 setCursor(Cursor.getDefaultCursor());
                         }
                 });
 		addMouseWheelListener(HorizontalScrollWheelListener.INSTANCE);
 	}
 	
 	public void userIsTyping() {
 		redrawCursorPosition();
                 setCursor(GuiUtilities.INVISIBLE_CURSOR);
 	}
 	
 	public TerminalModel getModel() {
 		return model;
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
	public Dimension getCharUnitSize() {
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
 	public Dimension getVisibleSizeInCharacters(Dimension paneSize) {
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
 	
 	private void sizeChanged() {
 		Dimension size = getOptimalViewSize();
 		setMaximumSize(size);
 		setPreferredSize(size);
 		setSize(size);
 		revalidate();
                 repaint();
 	}
 	
	public void sizeChanged(Dimension oldSizeInChars, Dimension newSizeInChars) {
		sizeChanged();
	}

         public void update(Observable o, Object arg) {
                 if (o != model)
                         return;
                 if (model.linesHaveChanged())
                         linesChangedFrom(model.getFirstLineChanged());
                 setCursorPosition(model.getCursorPosition());
                 setCursorVisible(model.getCursorVisible());
         }
 	
	public void linesChangedFrom(int lineIndex) {
 		Point redrawTop = modelToView(new Location(lineIndex, 0)).getLocation();
 		Dimension size = getSize();
 		repaint(redrawTop.x, redrawTop.y, size.width, size.height - redrawTop.y);
 	}
 	
	public void setCursorPosition(Location newCursorPosition) {
 		if (cursorPosition.equals(newCursorPosition)) {
 			return;
 		}
 		redrawCursorPosition();
 		cursorPosition = newCursorPosition;
 		redrawCursorPosition();
 	}
 	
 	/** Sets whether the cursor should be displayed. */
	public void setCursorVisible(boolean displayCursor) {
 		if (this.displayCursor != displayCursor) {
 			this.displayCursor = displayCursor;
 			redrawCursorPosition();
 		}
 	}
 	
	public boolean shouldShowCursor() {
 		return displayCursor;
 	}
 	
	public Rectangle modelToView(Location charCoords) {
 		// We can be asked the view rectangle of locations that are past the bottom of the text in various circumstances. Examples:
 		// 1. If the user sweeps a selection too far.
 		// 2. If the user starts a new shell, types "man bash", and then clears the history; we move the cursor, and want to know the old cursor location to remove the cursor from, even though there's no longer any text there.
 		// Rather than have special case code in each caller, simply return a reasonable result.
 		// Note that it's okay to have the empty string as the default here because we'll pad if necessary later in this method.
 		String line = "";
 		if (charCoords.getLineIndex() < model.getLineCount()) {
 			line = model.getTextLine(charCoords.getLineIndex()).getString();
 		}
 		
 		final int offset = Math.max(0, charCoords.getCharOffset());
 		
 		String characterAtLocation;
 		if (line.length() == offset) {
 			// A very common case is where the location is one past the end of the line.
 			// We don't need to add a single space if we're just going to  pull it off again.
 			// This might not seem like much, but it can be costly if you've got very long lines.
 			characterAtLocation = " ";
 		} else {
 			// Pad the line if we need to.
 			final int desiredLength = offset + 1;
 			if (line.length() < desiredLength) {
 				final int charactersOfPaddingRequired = desiredLength - line.length();
 				line += StringUtilities.nCopies(charactersOfPaddingRequired, " ");
 			}
 			characterAtLocation = line.substring(offset, offset + 1);
 		}
 		
 		String lineBeforeOffset = line.substring(0, offset);
 		FontMetrics fontMetrics = getFontMetrics(getFont());
 		Insets insets = getInsets();
 		final int x = insets.left + fontMetrics.stringWidth(lineBeforeOffset);
 		final int width = fontMetrics.stringWidth(characterAtLocation);
 		final int height = getCharUnitSize().height;
 		final int y = insets.top + charCoords.getLineIndex() * height;
 		return new Rectangle(x, y, width, height);
 	}
 	
	public Dimension getOptimalViewSize() {
 		Dimension character = getCharUnitSize();
 		Insets insets = getInsets();
                 Dimension size = model.getCurrentSizeInChars();
 		// FIXME: really, we need to track the maximum pixel width.
 		final int width = insets.left + size.width * character.width + insets.right;
 		final int height = insets.top + size.height * character.height + insets.bottom;
 		return new Dimension(width, height);
 	}
 	
 	// Redraw code.
 	
 	private void redrawCursorPosition() {
 		Rectangle cursorRect = modelToView(cursorPosition);
 		repaint(cursorRect);
 	}
 	
 	public void paintComponent(Graphics oldGraphics) {
 		Stopwatch.Timer timer = paintComponentStopwatch.start();
 		try {
 			Graphics2D g = (Graphics2D) oldGraphics;
 			
 			Object antiAliasHint = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
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
 				boolean drawCursor = (shouldShowCursor() && i == cursorPosition.getLineIndex());
 				int x = insets.left;
 				int baseline = insets.top + charUnitSize.height * (i + 1) - metrics.getMaxDescent();
 				int startOffset = 0;
 				Iterator<StyledText> it = getLineStyledText(i, widthHintInChars).iterator();
 				while (it.hasNext() && x < maxX) {
 					StyledText chunk = it.next();
 					x += paintStyledText(g, metrics, chunk, x, baseline);
 					String chunkText = chunk.getText();
 					if (drawCursor && cursorPosition.charOffsetInRange(startOffset, startOffset + chunkText.length())) {
 						final int charOffsetUnderCursor = cursorPosition.getCharOffset() - startOffset;
 						paintCursor(g, chunkText.substring(charOffsetUnderCursor, charOffsetUnderCursor + 1), baseline);
 						drawCursor = false;
 					}
 					startOffset += chunkText.length();
 				}
 				if (drawCursor) {
 					// A cursor at the end of the line is in a position past the end of the text.
 					paintCursor(g, "", baseline);
 				}
 			}
 			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasHint);
 		} finally {
 			timer.stop();
 		}
 	}
 	
 	private List<StyledText> getLineStyledText(int line, int widthHintInChars) {
 		return model.getTextLine(line).getStyledTextSegments(widthHintInChars);
 	}
 	
 	/**
 	 * Paints the cursor, which is either a solid block or an underline.
 	 * The cursor may actually be invisible because it's blinking and in
 	 * the 'off' state.
 	 */
 	private void paintCursor(Graphics2D g, String characterUnderCursor, int baseline) {
 		g.setColor(Color.black);
 		Rectangle cursorRect = modelToView(cursorPosition);
 		final int bottomY = cursorRect.y + cursorRect.height - 1;
 		if (hasFocus) {
                         // Paint over the character underneath.
                         g.fill(cursorRect);
                         // Redraw the character in the
                         // background color.
                         g.setColor(getBackground());
                         g.drawString(characterUnderCursor, cursorRect.x, baseline);
 		} else {
 			// For some reason, terminals always seem to use an
 			// empty block for the unfocused cursor, regardless
 			// of what shape they're using for the focused cursor.
 			// It's not obvious what else they could do that would
 			// look better.
 			g.drawRect(cursorRect.x, cursorRect.y, cursorRect.width - 1, cursorRect.height - 1);
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
 	
	public boolean hasFocus() {
		return hasFocus;
	}
	
 	//
 	// FocusListener interface.
 	//
 	
 	public void focusGained(FocusEvent event) {
 		hasFocus = true;
 		redrawCursorPosition();
 	}
 	
 	public void focusLost(FocusEvent event) {
 		hasFocus = false;
 		redrawCursorPosition();
 	}
 }
