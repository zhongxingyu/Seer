 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package com.dmdirc.addons.ui_swing.textpane;
 
 import com.dmdirc.addons.ui_swing.BackgroundOption;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.components.LoggingSwingWorker;
 import com.dmdirc.addons.ui_swing.components.frames.TextFrame;
 import com.dmdirc.config.ConfigManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.logger.ErrorLevel;
 import com.dmdirc.logger.Logger;
 import com.dmdirc.ui.messages.IRCDocument;
 import com.dmdirc.ui.messages.IRCTextAttribute;
 import com.dmdirc.ui.messages.LinePosition;
 import com.dmdirc.util.ListenerList;
 import com.dmdirc.util.URLBuilder;
 
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.Toolkit;
 import java.awt.event.AdjustmentEvent;
 import java.awt.event.AdjustmentListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.MouseEvent;
 import java.awt.font.LineBreakMeasurer;
 import java.awt.font.TextAttribute;
 import java.awt.font.TextHitInfo;
 import java.awt.font.TextLayout;
 import java.io.IOException;
 import java.net.URL;
 import java.text.AttributedCharacterIterator;
 import java.text.AttributedString;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 import javax.imageio.ImageIO;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.event.MouseInputListener;
 
 /** Canvas object to draw text. */
 class TextPaneCanvas extends JPanel implements MouseInputListener,
         ComponentListener, AdjustmentListener, ConfigChangeListener {
 
     /**
      * A version number for this class. It should be changed whenever the
      * class structure is changed (or anything else that would prevent
      * serialized objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 8;
     /** Hand cursor. */
     private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
     /** Single Side padding for textpane. */
     private static final int SINGLE_SIDE_PADDING = 3;
     /** Both Side padding for textpane. */
     private static final int DOUBLE_SIDE_PADDING = SINGLE_SIDE_PADDING * 2;
     /** Padding to add to line height. */
     private static final double LINE_PADDING = 0.2;
     /** IRCDocument. */
     private final IRCDocument document;
     /** parent textpane. */
     private final TextPane textPane;
     /** Position -> TextLayout. */
     private final Map<Rectangle, TextLayout> positions;
     /** TextLayout -> Line numbers. */
     private final Map<TextLayout, LineInfo> textLayouts;
     /** Start line. */
     private int startLine;
     /** Selection. */
     private LinePosition selection;
     /** First visible line. */
     private int firstVisibleLine;
     /** Last visible line. */
     private int lastVisibleLine;
     /** Background image. */
     private Image backgroundImage;
     /** Config Manager. */
     private ConfigManager manager;
     /** Config domain. */
     private String domain;
     /** Background image option. */
     private BackgroundOption backgroundOption;
     /** Quick copy? */
     private boolean quickCopy;
     /** Mouse click listenres. */
     private ListenerList listeners;
 
     /**
      * Creates a new text pane canvas.
      *
      * @param parent parent text pane for the canvas
      * @param document IRCDocument to be displayed
      */
     public TextPaneCanvas(final TextPane parent, final IRCDocument document) {
         super();
         this.document = document;
         textPane = parent;
         this.manager = parent.getWindow().getContainer().getConfigManager();
         this.domain = ((TextFrame) parent.getWindow()).getController().
                 getDomain();
         startLine = 0;
         setDoubleBuffered(true);
         setOpaque(true);
         textLayouts = new HashMap<TextLayout, LineInfo>();
         positions = new HashMap<Rectangle, TextLayout>();
         selection = new LinePosition(-1, -1, -1, -1);
         listeners = new ListenerList();
         addMouseListener(this);
         addMouseMotionListener(this);
         addComponentListener(this);
         manager.addChangeListener(domain, "textpanebackground", this);
         manager.addChangeListener(domain, "textpanebackgroundoption", this);
         manager.addChangeListener("ui", "quickCopy", this);
 
         updateCachedSettings();
         ToolTipManager.sharedInstance().registerComponent(this);
     }
 
     /**
      * Paints the text onto the canvas.
      *
      * @param graphics graphics object to draw onto
      */
     @Override
     public void paintComponent(final Graphics graphics) {
         final Graphics2D g = (Graphics2D) graphics;
         final Map desktopHints = (Map) Toolkit.getDefaultToolkit().
                 getDesktopProperty("awt.font.desktophints");
         if (desktopHints != null) {
             g.addRenderingHints(desktopHints);
         }
         g.setColor(textPane.getBackground());
         g.fill(g.getClipBounds());
         UIUtilities.paintBackground(g, getBounds(), backgroundImage,
                 backgroundOption);
         paintOntoGraphics(g);
     }
 
     /**
      * Re calculates positions of lines and repaints if required.
      */
     protected void recalc() {
         if (isVisible()) {
             repaint();
         }
     }
 
     private void updateCachedSettings() {
         final String backgroundPath = manager.getOption(domain,
                 "textpanebackground");
         if (!backgroundPath.isEmpty()) {
             UIUtilities.invokeAndWait(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     final URL url = URLBuilder.buildURL(backgroundPath);
                     new LoggingSwingWorker<Image, Void>() {
 
                         /** {@inheritDoc} */
                         @Override
                         protected Image doInBackground() throws Exception {
                             Image returnValue = null;
                             try {
                                 if (url != null) {
                                     returnValue = ImageIO.read(url);
                                 }
                             } catch (IOException ex) {
                                 returnValue = null;
                             }
                             return returnValue;
                         }
 
                         /** {@inheritDoc} */
                         @Override
                         protected void done() {
                             if (isCancelled()) {
                                 return;
                             }
                             try {
                                 backgroundImage = get();
                                 recalc();
                             } catch (InterruptedException ex) {
                             //Ignore
                             } catch (ExecutionException ex) {
                                 Logger.appError(ErrorLevel.MEDIUM,
                                         ex.getMessage(), ex);
                             }
                         }
                     }.execute();
                 }
             });
         }
         try {
             backgroundOption = BackgroundOption.valueOf(manager.getOption(domain,
                     "textpanebackgroundoption"));
         } catch (IllegalArgumentException ex) {
             backgroundOption = BackgroundOption.CENTER;
         }
         quickCopy = manager.getOptionBool("ui", "quickCopy");
     }
 
     private void paintOntoGraphics(final Graphics2D g) {
         final float formatWidth = getWidth() - DOUBLE_SIDE_PADDING;
         final float formatHeight = getHeight();
         float drawPosY = formatHeight;
 
         int paragraphStart;
         int paragraphEnd;
         LineBreakMeasurer lineMeasurer;
 
         textLayouts.clear();
         positions.clear();
 
         //check theres something to draw and theres some space to draw in
         if (document.getNumLines() == 0 || formatWidth < 1) {
             setCursor(Cursor.getDefaultCursor());
             return;
         }
 
         // Check the start line is in range
         if (startLine >= document.getNumLines()) {
             startLine = document.getNumLines() - 1;
         }
 
         if (startLine <= 0) {
             startLine = 0;
         }
 
         //sets the last visible line
         lastVisibleLine = startLine;
         firstVisibleLine = startLine;
 
         // Iterate through the lines
         for (int line = startLine; line >= 0; line--) {
             float drawPosX;
             final AttributedCharacterIterator iterator = document.getStyledLine(
                     line);
             int lineHeight = document.getLineHeight(line);
             lineHeight += lineHeight * LINE_PADDING;
             paragraphStart = iterator.getBeginIndex();
             paragraphEnd = iterator.getEndIndex();
             lineMeasurer = new LineBreakMeasurer(iterator,
                     g.getFontRenderContext());
             lineMeasurer.setPosition(paragraphStart);
 
             final int wrappedLine = getNumWrappedLines(lineMeasurer,
                     paragraphStart, paragraphEnd,
                     formatWidth);
 
             if (wrappedLine > 1) {
                 drawPosY -= lineHeight * wrappedLine;
             }
 
             if (line == startLine) {
                 drawPosY += DOUBLE_SIDE_PADDING;
             }
 
             int numberOfWraps = 0;
             int chars = 0;
             // Loop through each wrapped line
             while (lineMeasurer.getPosition() < paragraphEnd) {
                 final TextLayout layout = lineMeasurer.nextLayout(formatWidth);
 
                 // Calculate the Y offset
                 if (wrappedLine == 1) {
                     drawPosY -= lineHeight;
                 } else if (numberOfWraps != 0) {
                     drawPosY += lineHeight;
                 }
 
                 // Calculate the initial X position
                 if (layout.isLeftToRight()) {
                     drawPosX = SINGLE_SIDE_PADDING;
                 } else {
                     drawPosX = formatWidth - layout.getAdvance();
                 }
 
                 // Check if the target is in range
                 if (drawPosY >= 0 || drawPosY <= formatHeight) {
 
                     g.setColor(textPane.getForeground());
 
                     layout.draw(g, drawPosX, drawPosY + layout.getDescent());
                     doHighlight(line, chars, layout, g, drawPosY, drawPosX);
                     firstVisibleLine = line;
                     textLayouts.put(layout, new LineInfo(line, numberOfWraps));
                     positions.put(new Rectangle(0,
                             (int) (drawPosY + 1.5- layout.getAscent()
                             + layout.getDescent()),
                             (int) formatWidth + DOUBLE_SIDE_PADDING,
                             (int) (layout.getAscent() + layout.getDescent())),
                             layout);
                 }
 
                 numberOfWraps++;
                 chars += layout.getCharacterCount();
             }
             if (numberOfWraps > 1) {
                 drawPosY -= lineHeight * (wrappedLine - 1);
             }
             if (drawPosY <= 0) {
                 break;
             }
         }
         checkForLink();
     }
 
     /**
      * Returns the number of timesa line will wrap.
      *
      * @param lineMeasurer LineBreakMeasurer to work out wrapping for
      * @param paragraphStart Start index of the paragraph
      * @param paragraphEnd End index of the paragraph
      * @param formatWidth Width to wrap at
      *
      * @return Number of times the line wraps
      */
     private int getNumWrappedLines(final LineBreakMeasurer lineMeasurer,
             final int paragraphStart,
             final int paragraphEnd,
             final float formatWidth) {
         int wrappedLine = 0;
 
         while (lineMeasurer.getPosition() < paragraphEnd) {
             lineMeasurer.nextLayout(formatWidth);
 
             wrappedLine++;
         }
 
         lineMeasurer.setPosition(paragraphStart);
 
         return wrappedLine;
     }
 
     /**
      * Redraws the text that has been highlighted.
      *
      * @param line Line number
      * @param chars Number of characters so far in the line
      * @param layout Current line textlayout
      * @param g Graphics surface to draw highlight on
      * @param drawPosY current y location of the line
      * @param drawPosX current x location of the line
      */
     private void doHighlight(final int line, final int chars,
             final TextLayout layout, final Graphics2D g,
             final float drawPosY, final float drawPosX) {
         int selectionStartLine;
         int selectionStartChar;
         int selectionEndLine;
         int selectionEndChar;
 
         if (selection.getStartLine() > selection.getEndLine()) {
             // Swap both
             selectionStartLine = selection.getEndLine();
             selectionStartChar = selection.getEndPos();
             selectionEndLine = selection.getStartLine();
             selectionEndChar = selection.getStartPos();
         } else if (selection.getStartLine() == selection.getEndLine()
                 && selection.getStartPos() > selection.getEndPos()) {
             // Just swap the chars
             selectionStartLine = selection.getStartLine();
             selectionStartChar = selection.getEndPos();
             selectionEndLine = selection.getEndLine();
             selectionEndChar = selection.getStartPos();
         } else {
             // Swap nothing
             selectionStartLine = selection.getStartLine();
             selectionStartChar = selection.getStartPos();
             selectionEndLine = selection.getEndLine();
             selectionEndChar = selection.getEndPos();
         }
 
         //Does this line need highlighting?
         if (selectionStartLine <= line && selectionEndLine >= line) {
             int firstChar;
             int lastChar;
 
             // Determine the first char we care about
             if (selectionStartLine < line || selectionStartChar < chars) {
                 firstChar = chars;
             } else {
                 firstChar = selectionStartChar;
             }
 
             // ... And the last
             if (selectionEndLine > line || selectionEndChar > chars + layout.
                     getCharacterCount()) {
                 lastChar = chars + layout.getCharacterCount();
             } else {
                 lastChar = selectionEndChar;
             }
 
             // If the selection includes the chars we're showing
             if (lastChar > chars && firstChar < chars
                     + layout.getCharacterCount()) {
                 String text = document.getLine(line).getText();
                 if (firstChar >= 0 && text.length() > lastChar) {
                     text = text.substring(firstChar, lastChar);
                 }
 
                 if (text.isEmpty()) {
                     return;
                 }
 
                 final AttributedCharacterIterator iterator = document.
                         getStyledLine(line);
                if (iterator.getEndIndex() == iterator.getBeginIndex()) {
                    return;
                }
                 final AttributedString as = new AttributedString(iterator,
                         firstChar, lastChar);
 
                 as.addAttribute(TextAttribute.FOREGROUND,
                         textPane.getBackground());
                 as.addAttribute(TextAttribute.BACKGROUND,
                         textPane.getForeground());
                 final TextLayout newLayout = new TextLayout(as.getIterator(),
                         g.getFontRenderContext());
                 final Shape shape = layout.getLogicalHighlightShape(firstChar
                         - chars,
                         lastChar - chars);
                 final int trans = (int) (newLayout.getDescent() + drawPosY);
 
                 if (firstChar != 0) {
                     g.translate(shape.getBounds().getX(), 0);
                 }
 
                 newLayout.draw(g, drawPosX, trans);
 
                 if (firstChar != 0) {
                     g.translate(-1 * shape.getBounds().getX(), 0);
                 }
             }
         }
     }
 
     /**
      * {@{@inheritDoc}
      *
      * @param e Adjustment event
      */
     @Override
     public void adjustmentValueChanged(final AdjustmentEvent e) {
         if (startLine != e.getValue()) {
             startLine = e.getValue();
             recalc();
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseClicked(final MouseEvent e) {
         String clickedText = "";
         final int start;
         final int end;
 
         final LineInfo lineInfo = getClickPosition(getMousePosition(), true);
         fireMouseEvents(getClickType(lineInfo), MouseEventType.CLICK, e);
 
         if (lineInfo.getLine() != -1) {
             clickedText = document.getLine(lineInfo.getLine()).getText();
 
             if (lineInfo.getIndex() == -1) {
                 start = -1;
                 end = -1;
             } else {
                 final int[] extent =
                         getSurroundingWordIndexes(clickedText,
                         lineInfo.getIndex());
                 start = extent[0];
                 end = extent[1];
             }
 
             if (e.getClickCount() == 2) {
                 selection.setStartLine(lineInfo.getLine());
                 selection.setEndLine(lineInfo.getLine());
                 selection.setStartPos(start);
                 selection.setEndPos(end);
                 if (quickCopy) {
                     textPane.copy();
                     clearSelection();
                 }
             } else if (e.getClickCount() == 3) {
                 selection.setStartLine(lineInfo.getLine());
                 selection.setEndLine(lineInfo.getLine());
                 selection.setStartPos(0);
                 selection.setEndPos(clickedText.length());
                 if (quickCopy) {
                     textPane.copy();
                     clearSelection();
                 }
             }
         }
     }
 
     /**
      * Returns the type of text this click represents.
      *
      * @param lineInfo Line info of click.
      *
      * @return Click type for specified position
      */
     public ClickTypeValue getClickType(final LineInfo lineInfo) {
         if (lineInfo.getLine() != -1) {
             final AttributedCharacterIterator iterator = document.getStyledLine(
                     lineInfo.getLine());
             final int index = lineInfo.getIndex();
             if (index >= iterator.getBeginIndex() && index <= iterator.
                     getEndIndex()) {
                 iterator.setIndex(lineInfo.getIndex());
                 Object linkattr =
                         iterator.getAttributes().get(IRCTextAttribute.HYPERLINK);
                 if (linkattr instanceof String) {
                     return new ClickTypeValue(ClickType.HYPERLINK, (String) linkattr);
                 }
                 linkattr =
                         iterator.getAttributes().get(IRCTextAttribute.CHANNEL);
                 if (linkattr instanceof String) {
                     return new ClickTypeValue(ClickType.CHANNEL, (String) linkattr);
                 }
                 linkattr = iterator.getAttributes().get(
                         IRCTextAttribute.NICKNAME);
                 if (linkattr instanceof String) {
                     return new ClickTypeValue(ClickType.NICKNAME, (String) linkattr);
                 }
             } else {
                 return new ClickTypeValue(ClickType.NORMAL, "");
             }
         }
         return new ClickTypeValue(ClickType.NORMAL, "");
     }
 
     /**
      * Returns the indexes for the word surrounding the index in the specified
      * string.
      *
      * @param text Text to get word from
      * @param index Index to get surrounding word
      *
      * @return Indexes of the word surrounding the index (start, end)
      */
     protected int[] getSurroundingWordIndexes(final String text,
             final int index) {
         final int start = getSurroundingWordStart(text, index);
         final int end = getSurroundingWordEnd(text, index);
 
         if (start < 0 || end > text.length() || start > end) {
             return new int[]{0, 0};
         }
 
         return new int[]{start, end};
 
     }
 
     /**
      * Returns the start index for the word surrounding the index in the
      * specified string.
      *
      * @param text Text to get word from
      * @param index Index to get surrounding word
      *
      * @return Start index of the word surrounding the index
      */
     private int getSurroundingWordStart(final String text, final int index) {
         int start = index;
 
         // Traverse backwards
         while (start > 0 && start < text.length() && text.charAt(start) != ' ') {
             start--;
         }
         if (start + 1 < text.length() && text.charAt(start) == ' ') {
             start++;
         }
 
         return start;
     }
 
     /**
      * Returns the end index for the word surrounding the index in the
      * specified string.
      *
      * @param text Text to get word from
      * @param index Index to get surrounding word
      *
      * @return End index of the word surrounding the index
      */
     private int getSurroundingWordEnd(final String text, final int index) {
         int end = index;
 
         // And forwards
         while (end < text.length() && end > 0 && text.charAt(end) != ' ') {
             end++;
         }
 
         return end;
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mousePressed(final MouseEvent e) {
         fireMouseEvents(getClickType(getClickPosition(e.getPoint(), false)),
                 MouseEventType.PRESSED, e);
         if (e.getButton() == MouseEvent.BUTTON1) {
             highlightEvent(MouseEventType.CLICK, e);
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseReleased(final MouseEvent e) {
         fireMouseEvents(getClickType(getClickPosition(e.getPoint(), false)),
                 MouseEventType.RELEASED, e);
         if (quickCopy) {
             textPane.copy();
             SwingUtilities.invokeLater(new Runnable() {
 
                 /** {@inheritDoc} */
                 @Override
                 public void run() {
                     clearSelection();
                 }
             });
         }
         if (e.getButton() == MouseEvent.BUTTON1) {
             highlightEvent(MouseEventType.RELEASED, e);
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseDragged(final MouseEvent e) {
         if (e.getModifiersEx() == MouseEvent.BUTTON1_DOWN_MASK) {
             highlightEvent(MouseEventType.DRAG, e);
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseExited(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseMoved(final MouseEvent e) {
         checkForLink();
     }
 
     /** Checks for a link under the cursor and sets appropriately. */
     private void checkForLink() {
         final AttributedCharacterIterator iterator = getIterator(getMousePosition());
 
         if (iterator != null
                 && (iterator.getAttribute(IRCTextAttribute.HYPERLINK) != null
                 || iterator.getAttribute(IRCTextAttribute.CHANNEL) != null
                 || iterator.getAttribute(IRCTextAttribute.NICKNAME) != null)) {
             setCursor(HAND_CURSOR);
             return;
         }
 
         if (getCursor() == HAND_CURSOR) {
             setCursor(Cursor.getDefaultCursor());
         }
     }
 
     /**
      * Retrieves a character iterator for the text at the specified mouse
      * position.
      *
      * @since 0.6.4
      * @param mousePosition The mouse position to retrieve text for
      * @return A corresponding character iterator, or null if the specified
      * mouse position doesn't correspond to any text
      */
     private AttributedCharacterIterator getIterator(final Point mousePosition) {
         final LineInfo lineInfo = getClickPosition(mousePosition, false);
 
         if (lineInfo.getLine() != -1 && document.getLine(lineInfo.getLine()) != null) {
             final AttributedCharacterIterator iterator
                     = document.getStyledLine(lineInfo.getLine());
 
             if (lineInfo.getIndex() < iterator.getBeginIndex()
                     || lineInfo.getIndex() > iterator.getEndIndex()) {
                 return null;
             }
 
             iterator.setIndex(lineInfo.getIndex());
             return iterator;
         }
 
         return null;
     }
 
     /**
      * Sets the selection for the given event.
      *
      * @param type mouse event type
      * @param e responsible mouse event
      */
     protected void highlightEvent(final MouseEventType type,
             final MouseEvent e) {
         if (isVisible()) {
             Point point = e.getLocationOnScreen();
             SwingUtilities.convertPointFromScreen(point, this);
             if (!contains(point)) {
                 final Rectangle bounds = getBounds();
                 final Point mousePos = e.getPoint();
                 if (mousePos.getX() < bounds.getX()) {
                     point.setLocation(bounds.getX() + SINGLE_SIDE_PADDING,
                             point.getY());
                 } else if (mousePos.getX() > (bounds.getX() + bounds.
                         getWidth())) {
                     point.setLocation(bounds.getX() + bounds.getWidth()
                             - SINGLE_SIDE_PADDING,
                             point.getY());
                 }
                 if (mousePos.getY() < bounds.getY()) {
                     point.setLocation(point.getX(), bounds.getY()
                             + DOUBLE_SIDE_PADDING);
                 } else if (mousePos.getY() > (bounds.getY() + bounds.
                         getHeight())) {
                     point.setLocation(bounds.getX() + bounds.getWidth()
                             - SINGLE_SIDE_PADDING, bounds.getY() + bounds.
                             getHeight() - DOUBLE_SIDE_PADDING - 1);
                 }
             }
             LineInfo info = getClickPosition(point, true);
             final Rectangle first = getFirstLineRectangle();
             final Rectangle last = getLastLineRectangle();
             if (info.getLine() == -1 && info.getPart() == -1 && contains(point)
                     && document.getNumLines() != 0 && first != null && last != null) {
                 if (first.getY() >= point.getY()) {
                     info = getFirstLineInfo();
                 } else if (last.getY() <= point.getY()) {
                     info = getLastLineInfo();
                 }
             }
 
             if (info.getLine() != -1 && info.getPart() != -1) {
                 if (type == MouseEventType.CLICK) {
                     selection.setStartLine(info.getLine());
                     selection.setStartPos(info.getIndex());
                 }
                 selection.setEndLine(info.getLine());
                 selection.setEndPos(info.getIndex());
 
                 recalc();
             }
         }
     }
 
     /**
      * Returns the visible rectangle of the first line.
      *
      * @return First line's rectangle
      */
     private Rectangle getFirstLineRectangle() {
         TextLayout firstLineLayout = null;
         for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
             if (entry.getValue().getLine() == firstVisibleLine) {
                 firstLineLayout = entry.getKey();
             }
         }
         if (firstLineLayout == null) {
             return null;
         }
         for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
             if (entry.getValue() == firstLineLayout) {
                 return entry.getKey();
             }
         }
         return null;
     }
 
     /**
      * Returns the last line's visible rectangle.
      *
      * @return Last line's rectangle
      */
     private Rectangle getLastLineRectangle() {
         TextLayout lastLineLayout = null;
         for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
             if (entry.getValue().getLine() == lastVisibleLine) {
                 lastLineLayout = entry.getKey();
             }
         }
         if (lastLineLayout == null) {
             return null;
         }
         for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
             if (entry.getValue() == lastLineLayout) {
                 return entry.getKey();
             }
         }
         return null;
     }
 
     /**
      * Returns the LineInfo for the first visible line.
      *
      * @return First line's line info
      */
     private LineInfo getFirstLineInfo() {
         int firstLineParts = Integer.MAX_VALUE;
         for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
             if (entry.getValue().getLine() == firstVisibleLine) {
                 if (entry.getValue().getPart() < firstLineParts) {
                     firstLineParts = entry.getValue().getPart();
                 }
             }
         }
         return new LineInfo(firstVisibleLine, firstLineParts);
     }
 
     /**
      * Returns the LineInfo for the last visible line.
      *
      * @return Last line's line info
      */
     private LineInfo getLastLineInfo() {
         int lastLineParts = -1;
         for (Map.Entry<TextLayout, LineInfo> entry : textLayouts.entrySet()) {
             if (entry.getValue().getLine() == lastVisibleLine) {
                 if (entry.getValue().getPart() > lastLineParts) {
                     lastLineParts = entry.getValue().getPart();
                 }
             }
         }
         return new LineInfo(lastVisibleLine + 1, lastLineParts);
     }
 
     /**
      *
      * Returns the line information from a mouse click inside the textpane.
      *
      * @param point mouse position
      * @param selection Are we selecting text?
      *
      * @return line number, line part, position in whole line
      */
     public LineInfo getClickPosition(final Point point, final boolean selection) {
         int lineNumber = -1;
         int linePart = -1;
         int pos = 0;
 
         if (point != null) {
             for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
                 if (entry.getKey().contains(point)) {
                     lineNumber = textLayouts.get(entry.getValue()).getLine();
                     linePart = textLayouts.get(entry.getValue()).getPart();
                 }
             }
 
             pos = getHitPosition(lineNumber, linePart, (int) point.getX(),
                     (int) point.getY(), selection);
         }
 
         return new LineInfo(lineNumber, linePart, pos);
     }
 
     /**
      * Returns the character index for a specified line and part for a specific hit position.
      *
      * @param lineNumber Line number
      * @param linePart Line part
      * @param x X position
      * @param y Y position
      *
      * @return Hit position
      */
     private int getHitPosition(final int lineNumber, final int linePart,
             final int x, final int y, final boolean selection) {
         int pos = 0;
 
         for (Map.Entry<Rectangle, TextLayout> entry : positions.entrySet()) {
             if (textLayouts.get(entry.getValue()).getLine() == lineNumber) {
                 if (textLayouts.get(entry.getValue()).getPart() < linePart) {
                     pos += entry.getValue().getCharacterCount();
                 } else if (textLayouts.get(entry.getValue()).getPart()
                         == linePart) {
                     final TextHitInfo hit = entry.getValue().hitTestChar(x
                             - DOUBLE_SIDE_PADDING, y);
                     if (selection || x > entry.getValue().getBounds().getX()) {
                         pos += hit.getInsertionIndex();
                     } else {
                         pos += hit.getCharIndex();
                     }
                 }
             }
         }
         return pos;
     }
 
     /**
      * Returns the selected range info.
      *
      * @return Selected range info
      */
     protected LinePosition getSelectedRange() {
         if (selection.getStartLine() > selection.getEndLine()) {
             // Swap both
             return new LinePosition(selection.getEndLine(),
                     selection.getEndPos(), selection.getStartLine(),
                     selection.getStartPos());
         } else if (selection.getStartLine() == selection.getEndLine() && selection.
                 getStartPos() > selection.getEndPos()) {
             // Just swap the chars
             return new LinePosition(selection.getStartLine(), selection.
                     getEndPos(), selection.getEndLine(),
                     selection.getStartPos());
         } else {
             // Swap nothing
             return new LinePosition(selection.getStartLine(), selection.
                     getStartPos(), selection.getEndLine(),
                     selection.getEndPos());
         }
     }
 
     /** Clears the selection. */
     protected void clearSelection() {
         selection.setEndLine(selection.getStartLine());
         selection.setEndPos(selection.getStartPos());
         recalc();
     }
 
     /**
      * Selects the specified region of text.
      *
      * @param position Line position
      */
     public void setSelectedRange(final LinePosition position) {
         selection = new LinePosition(position);
         recalc();
     }
 
     /**
      * Returns the first visible line.
      *
      * @return the line number of the first visible line
      */
     public int getFirstVisibleLine() {
         return firstVisibleLine;
     }
 
     /**
      * Returns the last visible line.
      *
      * @return the line number of the last visible line
      */
     public int getLastVisibleLine() {
         return lastVisibleLine;
     }
 
     /**
      * Returns the number of visible lines.
      *
      * @return Number of visible lines
      */
     public int getNumVisibleLines() {
         return lastVisibleLine - firstVisibleLine;
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Component event
      */
     @Override
     public void componentResized(final ComponentEvent e) {
         recalc();
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Component event
      */
     @Override
     public void componentMoved(final ComponentEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Component event
      */
     @Override
     public void componentShown(final ComponentEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Component event
      */
     @Override
     public void componentHidden(final ComponentEvent e) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         updateCachedSettings();
     }
 
     /** {@inheritDoc} */
     @Override
     public String getToolTipText(final MouseEvent event) {
         final AttributedCharacterIterator iterator = getIterator(event.getPoint());
 
         if (iterator != null && iterator.getAttribute(IRCTextAttribute.TOOLTIP) != null) {
             return iterator.getAttribute(IRCTextAttribute.TOOLTIP).toString();
         }
 
         return super.getToolTipText(event);
     }
 
     /**
      * Fires mouse clicked events with the associated values.
      *
      * @param clickType Click type
      * @param eventType Mouse event type
      * @param button Mouse button
      */
     private void fireMouseEvents(final ClickTypeValue clickType,
             final MouseEventType eventType, final MouseEvent event) {
         for (TextPaneListener listener : listeners.get(TextPaneListener.class)) {
             listener.mouseClicked(clickType, eventType, event);
         }
     }
 
     /**
      * Adds a textpane listener.
      *
      * @param listener Listener to add
      */
     public void addTextPaneListener(final TextPaneListener listener) {
         listeners.add(TextPaneListener.class, listener);
     }
 
     /**
      * Removes a textpane listener.
      *
      * @param listener Listener to remove
      */
     public void removeTextPaneListener(final TextPaneListener listener) {
         listeners.remove(TextPaneListener.class, listener);
     }
 }
