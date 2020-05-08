 package terminator.model;
 
 import java.awt.Dimension;
 import java.util.*;
 import e.util.*;
 import terminator.terminal.*;
 
 public class TerminalModel {
         private class TerminalListeners implements TerminalListener {
                 private List<TerminalListener> listeners = new ArrayList<TerminalListener>();
 
                 public void add(TerminalListener l) {
                         listeners.add(l);
                 }
 
                 public void contentsChanged(int fromLine) {
                         for (TerminalListener l : listeners)
                                 l.contentsChanged(fromLine);
                 }
 
                 public void cursorPositionChanged(Cursor oldPosition, Cursor newPosition) {
                         for (TerminalListener l : listeners)
                                 l.cursorPositionChanged(oldPosition, newPosition);
                 }
 
                 public void cursorVisibilityChanged(boolean isVisible) {
                         for (TerminalListener l : listeners)
                                 l.cursorVisibilityChanged(isVisible);
                 }
         }
 
         private TerminalListeners listeners = new TerminalListeners();
         private TextLines textLines = new TextLines(new Dimension(0, 0));
         private Cursor cursor = Cursor.origo();
 
 	public void addListener(TerminalListener l) {
 		listeners.add(l);
 	}
 
 	public int getLineCount() {
                 return textLines.count();
 	}
 
 	public TextLine getTextLine(int index) {
                 return textLines.get(index);
 	}
 
 	public Cursor getCursor() {
 		return cursor;
 	}
 
 	public void processActions(TerminalAction[] actions) {
                 modifier.reset();
 		for (TerminalAction action : actions)
 			action.perform(modifier);
                 modifier.notifyListeners();
 	}
 
         private TerminalModelModifier modifier = new TerminalModelModifier() {
                 private Style style = Style.DEFAULT;
                 private Region scrollingRegion = new Region(0, 0);
                 private boolean insertMode = false;
                 private int firstLineChanged;
                 private Cursor oldCursor;
 
                 public void reset() {
                         firstLineChanged = Integer.MAX_VALUE;
                         oldCursor = cursor;
                 }
 
                 public void notifyListeners() {
                         if (!oldCursor.equals(cursor))
                                 listeners.cursorPositionChanged(oldCursor, cursor);
                         if (firstLineChanged != Integer.MAX_VALUE)
                                 listeners.contentsChanged(firstLineChanged);
                 }
 
                 private void linesChangedFrom(int line) {
                         firstLineChanged = Math.min(firstLineChanged, line);
                 }
 
                 private void linesChangedFromCursor() {
                         linesChangedFrom(cursor.getRow());
                 }
 
                 public void setSize(Dimension size) {
                         textLines.setSize(size);
                         scrollingRegion.set(0, getLineCount() - 1);
                         cursor = cursor.constrain(size);
                 }
 
                 public void setStyle(Style style) {
                        this.style = style;
                 }
 
                 public Style getStyle() {
                         return style;
                 }
 
                 public void insertLines(int count) {
                         insertLines(cursor.getRow(), count);
                 }
 
                 private void insertLines(int at, int count) {
                         int above = textLines.insertLines(at,
                                                           count,
                                                           scrollingRegion.top(),
                                                           scrollingRegion.bottom());
                         linesChangedFrom(above == count ? cursor.getRow() : scrollingRegion.top());
                 }
 
                 public void setInsertMode(boolean newInsertMode) {
                         insertMode = newInsertMode;
                 }
 
                 public void addText(String text) {
                         TextLine textLine = getCursorTextLine();
                         if (insertMode) {
                                 //Log.warn("Inserting text \"" + line + "\" at " + cursor + ".");
                                 textLine.insertTextAt(cursor.getColumn(), text, style);
                         } else {
                                 //Log.warn("Writing text \"" + line + "\" at " + cursor + ".");
                                 textLine.writeTextAt(cursor.getColumn(), text, style);
                         }
                         textAdded(text.length());
                 }
 
                 private TextLine getCursorTextLine() {
                         return textLines.get(cursor.getRow());
                 }
 
                 private void textAdded(int length) {
                         moveCursorHorizontally(length);
                         linesChangedFromCursor();
                 }
 
                 public void horizontalTabulation() {
                         int nextTabLocation = getNextTabPosition(cursor.getColumn());
                         TextLine textLine = getCursorTextLine();
                         int startOffset = cursor.getColumn();
                         int tabLength = nextTabLocation - startOffset;
                         // We want to insert our special tabbing characters
                         // (see getTabString) when inserting a tab or
                         // outputting one at the end of a line, so that text
                         // copied from the output of (say) cat(1) will be
                         // pasted with tabs preserved.
                         boolean endOfLine = (startOffset == textLine.length());
                         if (insertMode || endOfLine) {
                                 textLine.insertTabAt(startOffset, tabLength, style);
                         } else {
                                 // Emacs, source of all bloat, uses \t\b\t
                                 // sequences around tab stops (in lines with no
                                 // \t characters) if you hold down right arrow.
                                 // The call to textAdded below moves the
                                 // cursor, which is all we're supposed to do.
                         }
                         textAdded(tabLength);
                 }
 
                 private int getNextTabPosition(int column) {
                         return (column + 8) & ~7;
                 }
 
                 public void lineFeed() {
                         int row = cursor.getRow() + 1;
                         if (row > scrollingRegion.bottom())
                                 insertLines(row, 1);
                         else
                                 cursor = cursor.moveToRow(row);
                 }
 
                 public void carriageReturn() {
                         cursor = cursor.moveToColumn(0);
                 }
 
                 public void setCursorVisible(boolean visible) {
                         if (cursor.isVisible() == visible)
                                 return;
                         cursor = cursor.setVisible(visible);
                         listeners.cursorVisibilityChanged(visible);
                 }
 
                 public void deleteCharacters(int count) {
                         getCursorTextLine().killText(cursor.getColumn(),
                                                      cursor.getColumn() + count);
                         linesChangedFromCursor();
                 }
 
                 public void clearToEndOfLine() {
                         TextLine line = getCursorTextLine();
                         line.killText(cursor.getColumn(), line.length());
                         linesChangedFromCursor();
                 }
 
                 public void clearToBeginningOfLine() {
                         getCursorTextLine().killText(0, cursor.getColumn());
                         linesChangedFromCursor();
                 }
 
                 public void clearToEndOfScreen() {
                         clearToEndOfLine();
                         for (TextLine line : textLines.region(cursor.getRow() + 1, getLineCount()))
                                 line.clear();
                 }
 
                 public void setCursorPosition(int row, int column) {
                         cursor = cursor.moveToRow(row).moveToColumn(column);
                 }
 
                 public void moveCursorHorizontally(int delta) {
                         cursor = cursor.adjustColumn(delta);
                 }
 
                 public void moveCursorVertically(int delta) {
                         cursor = cursor.adjustRow(delta);
                 }
 
                 public void setScrollingRegion(int top, int bottom) {
                         if (bottom > getLineCount() - 1) {
                                 Log.warn("Tried to set scrolling region bottom beyond last line" +
                                          " (" + bottom + " > " + (getLineCount() - 1) + ")");
                                 return;
                         }
                         if (top < 0)
                                 top = 0;
                         if (bottom < 0)
                                 bottom = getLineCount() - 1;
                         scrollingRegion.set(top, bottom);
                 }
 
                 public void scrollDisplayUp() {
                         insertLines(scrollingRegion.top(), 1);
                         linesChangedFrom(scrollingRegion.top());
                 }
 
                 public void deleteLines(int count) {
                         textLines.insertLines(scrollingRegion.bottom() + 1,
                                               count,
                                               cursor.getRow(),
                                               scrollingRegion.bottom());
                         linesChangedFrom(cursor.getRow());
                 }
         };
 }
