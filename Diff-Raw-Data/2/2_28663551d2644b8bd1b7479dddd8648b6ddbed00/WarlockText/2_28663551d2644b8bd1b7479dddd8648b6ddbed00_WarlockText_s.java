 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.rcp.ui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.WarlockStringMarker;
 import cc.warlock.core.client.internal.WarlockStyle;
 
 /**
  * This is an extension of the StyledText widget which has special support for
  *  embedding of arbitrary Controls/Links
  * @author Marshall
  */
 public class WarlockText {
 	
 	private StyledText textWidget;
 	private Cursor handCursor, defaultCursor;
 	private int lineLimit = 5000;
 	private int doScrollDirection = SWT.DOWN;
 	private IStyleProvider styleProvider;
 	private Menu contextMenu;
 	private LinkedList<WarlockStringMarker> markers = new LinkedList<WarlockStringMarker>();
 	
 	public WarlockText(Composite parent) {
 		textWidget = new StyledText(parent, SWT.V_SCROLL);
 
 		textWidget.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
 		textWidget.setEditable(false);
 		textWidget.setWordWrap(true);
 		textWidget.setIndent(1);
 
 		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
 
 		Display display = parent.getDisplay();
 		handCursor = new Cursor(display, SWT.CURSOR_HAND);
 		defaultCursor = parent.getCursor();
 
 		contextMenu = new Menu(textWidget);
 		MenuItem itemCopy = new MenuItem(contextMenu, SWT.PUSH);
 		itemCopy.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				textWidget.copy();
 			}
 		});
 		itemCopy.setText("Copy");
 		itemCopy.setImage(images.getImage(ISharedImages.IMG_TOOL_COPY));
 		MenuItem itemClear = new MenuItem(contextMenu, SWT.PUSH);
 		itemClear.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent arg0) {
 				textWidget.setText("");
 			}
 		});
 		itemClear.setText("Clear");
 		itemClear.setImage(images.getImage(ISharedImages.IMG_TOOL_DELETE));
 		textWidget.setMenu(contextMenu);
 
 		textWidget.addMouseMoveListener(new MouseMoveListener() {
 			public void mouseMove(MouseEvent e) {
 				try {
 					if (!textWidget.isDisposed() && textWidget.isVisible())
 					{
 						Point point = new Point(e.x, e.y);
 						int offset = textWidget.getOffsetAtLocation(point);
 						StyleRange range = textWidget.getStyleRangeAtOffset(offset);
 						if (range != null && range instanceof StyleRangeWithData)
 						{
 							StyleRangeWithData range2 = (StyleRangeWithData) range;
 							if (range2.action != null)
 							{
 								textWidget.setCursor(handCursor);
 								return;
 							}
 
 						}
 						textWidget.setCursor(defaultCursor);
 					}
 				} catch (IllegalArgumentException ex) {
 					// swallow -- this happens if the mouse cursor moves to an
 					// area not covered by the imaginary rectangle surround the
 					// current text
 					textWidget.setCursor(defaultCursor);
 				}
 			}
 		});
 
 		textWidget.addMouseListener(new MouseListener () {
 			public void mouseDoubleClick(MouseEvent e) {}
 			public void mouseDown(MouseEvent e) {}
 			public void mouseUp(MouseEvent e) {
 				try {
 					Point point = new Point(e.x, e.y);
 					int offset = textWidget.getOffsetAtLocation(point);
 					StyleRange range = textWidget.getStyleRangeAtOffset(offset);
 					if (range != null && range instanceof StyleRangeWithData)
 					{
 						StyleRangeWithData range2 = (StyleRangeWithData) range;
 						if (range2.action != null)
 						{
 							range2.action.run();
 						}
 					}
 				} catch (IllegalArgumentException ex) {
 					// swallow -- see note above
 				}
 			}
 		});
 	}
 	
 	public void selectAll() {
 		textWidget.selectAll();
 	}
 	
 	public void copy() {
 		textWidget.copy();
 	}
 	
 	public void pageUp() {
 		if (isAtBottom()) {
 			textWidget.setCaretOffset(textWidget.getCharCount());
 		}
 		textWidget.invokeAction(ST.PAGE_UP);
 	}
 	
 	public void pageDown() {
 		textWidget.invokeAction(ST.PAGE_DOWN);
 	}
 	
 	public void setBackground(Color color) {
 		textWidget.setBackground(color);
 	}
 	
 	public void setForeground(Color color) {
 		textWidget.setForeground(color);
 	}
 	
 	public void setFont(Font font) {
 		textWidget.setFont(font);
 	}
 	
 	public void clearText() {
 		textWidget.setText("");
 		clearMarkers();
 	}
 	
 	public void setLineLimit(int limit) {
 		lineLimit = limit;
 	}
 	
 	public void append(String string) {
 		boolean atBottom = isAtBottom();
 		int charCount = textWidget.getCharCount();
 		textWidget.append(string);
 		removeEmptyLines(charCount);
 		constrainLineLimit(atBottom);
 
 		postTextChange(atBottom);
 	}
 	
 	private void removeEmptyLines(int offset) {
 		int line = textWidget.getLineAtOffset(offset);
 		int start = textWidget.getOffsetAtLine(line);
 		int end = textWidget.getCharCount() - 1;
 		if(start >= end)
 			return;
 		String str = textWidget.getText(start, end);
 		
 		int lineStart = 0;
 		while(true) {
 			int lineEnd = str.indexOf("\n", lineStart);
 			if(lineEnd < 0)
 				break;
 			if(lineStart == lineEnd) {
 				textWidget.replaceTextRange(offset + lineEnd, 1, "");
 				WarlockStringMarker marker = new WarlockStringMarker(WarlockStringMarker.Type.EMPTY,
 						new WarlockStyle("newline"), lineEnd);
 				this.addMarker(marker);
 				updateMarkers(marker, marker, -1);
 				// Recursive call. if the could be a tail call, that would be awesome.
				removeEmptyLines(lineEnd);
 				break;
 			} else {
 				lineStart = lineEnd + 1;
 			}
 		}
 	}
 	
 	private void restoreNewlines(int offset) {
 		for(Iterator<WarlockStringMarker> iter = markers.iterator();
 		iter.hasNext(); )
 		{
 			WarlockStringMarker marker = iter.next();
 		
 			// check to make sure we're a newline in the appropriate area
 			if(marker.offset < offset || !marker.style.getName().equals("newline"))
 				continue;
 			
 			// check if we're an empty line
 			if(marker.offset == 0 || textWidget.getTextRange(marker.offset - 1, 1).equals("\n"))
 				continue;
 			
 			// we're not an empty line, put us back into action
 			textWidget.replaceTextRange(marker.offset, 0, "\n");
 			// TODO: this should actually just affect markers after us... oh well.
 			updateMarkers(marker, marker, 1);
 			iter.remove();
 		}
 	}
 	
 	private void addStyles(List<WarlockStringMarker> styles, int offset) {
 		// add a marker for each style with a name
 		for(WarlockStringMarker marker : styles) {
 			String name = marker.style.getName();
 			if(name != null) {
 				this.addMarker(new WarlockStringMarker(marker.type, marker.style, offset + marker.offset));
 			}
 		}
 		
 		/* Break up the ranges and merge overlapping styles because SWT only
 		 * allows 1 style per section
 		 */
 		ArrayList<WarlockStringMarker> currentStyles = new ArrayList<WarlockStringMarker>();
 		ArrayList<StyleRangeWithData> finishedStyles = new ArrayList<StyleRangeWithData>();
 		int pos = 0;
 		for(WarlockStringMarker style : styles) {
 			
 			int nextPos = style.offset;
 
 			if(nextPos > pos && currentStyles.size() > 0) {
 				// merge all of the styles
 				StyleRangeWithData styleRange = warlockStyleToStyleRange(currentStyles.get(0).style, offset + pos, nextPos - pos);
 				for(int i = 1; i < currentStyles.size(); i++) {
 					StyleRangeWithData nextStyle = warlockStyleToStyleRange(currentStyles.get(i).style, offset + pos, nextPos - pos);
 					if(nextStyle.font != null)
 						styleRange.font = nextStyle.font;
 					if(nextStyle.background != null)
 						styleRange.background = nextStyle.background;
 					if(nextStyle.foreground != null)
 						styleRange.foreground = nextStyle.foreground;
 					if(nextStyle.fontStyle != SWT.NORMAL)
 						styleRange.fontStyle = nextStyle.fontStyle;
 					if(nextStyle.strikeout) styleRange.strikeout = true;
 					if(nextStyle.underline) styleRange.underline = true;
 					styleRange.data.putAll(nextStyle.data);
 					if(nextStyle.action != null)
 						styleRange.action = nextStyle.action;
 					if(nextStyle.tooltip != null)
 						styleRange.tooltip = nextStyle.tooltip;
 				}
 				finishedStyles.add(styleRange);
 			}
 			
 			// update current styles
 			if(style.type == WarlockStringMarker.Type.BEGIN) {
 				currentStyles.add(style);
 			} else if(style.type == WarlockStringMarker.Type.END) {
 				WarlockStringMarker.removeStyle(currentStyles, style.style);
 			}
 
 			pos = nextPos;
 		}
 		
 		for(StyleRangeWithData style : finishedStyles) {
 			textWidget.setStyleRange(style);
 		}
 	}
 	
 	public void append(WarlockString wstring) {
 		boolean atBottom = isAtBottom();
 		
 		int charCount = textWidget.getCharCount();
 		String string = wstring.toString();
 		textWidget.append(string);
 		addStyles(wstring.getStyles(), charCount);
 		removeEmptyLines(charCount);
 		
 		constrainLineLimit(atBottom);
 
 		postTextChange(atBottom);
 	}
 	
 	private StyleRangeWithData warlockStyleToStyleRange(IWarlockStyle style, int start, int length) {
 		if(styleProvider == null)
 			return null;
 		
 		StyleRangeWithData styleRange = styleProvider.getStyleRange(style);
 		if(styleRange == null)
 			return null;
 
 		styleRange.start = start;
 		styleRange.length = length;
 		
 		if(style.isFullLine())
 			textWidget.setLineBackground(textWidget.getLineAtOffset(styleRange.start), 1, styleRange.background);
 		if(style.getAction() != null)
 			styleRange.action = style.getAction();
 		if(style.getName() != null)
 			styleRange.data.put("name", style.getName());
 		
 		return styleRange;
 	}
 	
 	public boolean isAtBottom() {
 		return textWidget.getLinePixel(textWidget.getLineCount()) <= textWidget.getClientArea().height;
 	}
 	
 	public void postTextChange(boolean atBottom) {
 		// TODO: Make preTextChange private
 		// Explination: right now we can't listen for the before and after of our resize, so this must be called
 		//     after an action that will cause a resize.
 		if (atBottom && doScrollDirection == SWT.DOWN) {
 			textWidget.setTopPixel(textWidget.getTopPixel()
 					+ textWidget.getLinePixel(textWidget.getLineCount()));
 			
 			// FIXME: is this still needed?
 			//if (Platform.getOS().equals(Platform.OS_MACOSX)) {
 			//	textWidget.redraw();
 			//}
 		}
 	}
 	
 	// this function removes the first "delta" amount of characters
 	private void updateMarkers(int delta) {
 		// FIXME: if only half of a style is removed, we break
 		for(Iterator<WarlockStringMarker> iter = markers.iterator();
 		iter.hasNext(); )
 		{
 			WarlockStringMarker marker = iter.next();
 			
 			// If the replaced text contains us, remove us (replaced text must
 			// remove a character before and after (can't get rid of markers on
 			// borders, unfortunately) this shouldn't happen much, if ever.
 			if(marker.offset + delta < 0) {
 				iter.remove();
 				continue;
 			}
 			// move us accordingly
 			marker.offset += delta;
 		}
 	}
 	
 	// this function removes the first "delta" amount of characters
 	private void updateMarkers(WarlockStringMarker startMarker,
 			WarlockStringMarker endMarker, int delta) {
 		// remove markers between start and end.
 		// all markers after start need to be adjusted by offset.
 		boolean started = false;
 		boolean ended = false;
 		for(Iterator<WarlockStringMarker> iter = markers.iterator();
 		iter.hasNext(); )
 		{
 			WarlockStringMarker marker = iter.next();
 		
 			if(marker == endMarker) {
 				ended = true;
 			}
 			if(started) {
 				if(!ended) {
 					iter.remove();
 					continue;
 				}
 				marker.offset += delta;
 			}
 			if(marker == startMarker) {
 				started = true;
 			}
 		}
 	}
 	
 	public void addMarker(WarlockStringMarker marker) {
 		ListIterator<WarlockStringMarker> iter = markers.listIterator();
 		while(true) {
 			if(!iter.hasNext()) {
 				iter.add(marker);
 				break;
 			}
 			WarlockStringMarker cur = iter.next();
 			if(cur.offset > marker.offset) {
 				iter.previous();
 				iter.add(marker);
 				break;
 			}
 		}
 	}
 	
 	public void clearMarkers() {
 		markers.clear();
 	}
 	
 	public void replaceMarker(String name, WarlockString text) {
 		WarlockStringMarker startMarker = getBeginMarker(name);
 		WarlockStringMarker endMarker = getEndMarker(name);
 		if(startMarker == null || endMarker == null)
 			return;
 		
 		int start = startMarker.offset;
 		int length = endMarker.offset - startMarker.offset;
 		boolean atBottom = isAtBottom();
 		textWidget.replaceTextRange(start, length, text.toString());
 		updateMarkers(startMarker, endMarker, text.length() - length);
 		addStyles(text.getStyles(), start);
 		removeEmptyLines(start);
 		restoreNewlines(start);
 		postTextChange(atBottom);
 	}
 	
 	private WarlockStringMarker getBeginMarker(String name) {
 		for(WarlockStringMarker marker : markers) {
 			if(marker.type == WarlockStringMarker.Type.BEGIN
 					&& marker.style.getName().equals(name))
 				return marker;
 		}
 		return null;
 	}
 	
 	private WarlockStringMarker getEndMarker(String name) {
 		for(WarlockStringMarker marker : markers) {
 			if(marker.type == WarlockStringMarker.Type.END
 					&& marker.style.getName().equals(name))
 				return marker;
 		}
 		return null;
 	}
 	
 	private void constrainLineLimit(boolean atBottom) {
 		// 'status' is a pointer that allows us to change the object in our parent..
 		// in this method... it is intentional.
 		if (lineLimit > 0) {
 			int lines = textWidget.getLineCount();
 			if (lines > lineLimit) {
 				int linesToRemove = lines - lineLimit;
 				int charsToRemove = textWidget.getOffsetAtLine(linesToRemove);
 				if(atBottom) {
 					textWidget.replaceTextRange(0, charsToRemove, "");
 					updateMarkers(-charsToRemove);
 				} else {
 					int pixelsToRemove = textWidget.getLinePixel(linesToRemove);
 					textWidget.replaceTextRange(0, charsToRemove, "");
 					updateMarkers(-charsToRemove);
 					if(pixelsToRemove < 0)
 						textWidget.setTopPixel(-pixelsToRemove);
 				}
 			}
 		}
 	}
 	
 	public void setScrollDirection(int dir) {
 		if (dir == SWT.DOWN || dir == SWT.UP)
 			doScrollDirection = dir;
 		// TODO: Else throw an error
 	}
 	
 	public StyledText getTextWidget() {
 		return textWidget;
 	}
 	
 	public void setStyleProvider(IStyleProvider styleProvider) {
 		this.styleProvider = styleProvider;
 	}
 }
