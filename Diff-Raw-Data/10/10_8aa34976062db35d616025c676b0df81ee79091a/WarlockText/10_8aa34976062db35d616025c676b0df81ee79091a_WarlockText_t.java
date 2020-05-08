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
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
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
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.IWarlockStyle;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.WarlockStringMarker;
 import cc.warlock.core.client.internal.WarlockStyle;
 import cc.warlock.core.client.settings.IHighlightString;
 import cc.warlock.rcp.util.SoundPlayer;
 
 /**
  * This is an extension of the StyledText widget which has special support for
  *  embedding of arbitrary Controls/Links
  * @author Marshall
  */
 public class WarlockText {
 	
 	protected IWarlockClient client;
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
 		markers.clear();
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
 	
 	private Pattern newlinePattern = Pattern.compile("\r?\n");
 	private void removeEmptyLines(int offset) {
 		int line = textWidget.getLineAtOffset(offset);
 		int start = textWidget.getOffsetAtLine(line);
 		int end = textWidget.getCharCount();
 		if(start >= end)
 			return;
 		String str = textWidget.getTextRange(start, end - start);
 		Matcher m = newlinePattern.matcher(str);
 		
 		int lineStart = 0;
 		while(m.find(lineStart)) {
 			if(lineStart == m.start()) {
 				int matchPos = start + m.start();
 				int matchLen = m.end() - m.start();
 				// Add the newline marker. We give it an initial length of 1
 				//   so it gets added correctly into the tree of markers
 				WarlockStringMarker marker = new WarlockStringMarker(
 						new WarlockStyle("newline"), matchPos, matchPos + matchLen);
 				this.addInternalMarker(marker, markers);
 				
 				// then remove the newline from the text
 				textWidget.replaceTextRange(matchPos, matchLen, "");
 				// and shrink down the newline marker because the actual newline is no longer there.
 				marker.setEnd(matchPos);
 				WarlockStringMarker.updateMarkers(-matchLen, marker, markers);
 				// Recursive call. if this could be a tail call, that would be awesome.
 				removeEmptyLines(start);
 				break;
 			} else {
 				lineStart = m.end();
 			}
 		}
 	}
 	
 	private void restoreNewlines(int offset, Collection<WarlockStringMarker> markerList) {
 		for(Iterator<WarlockStringMarker> iter = markerList.iterator();
 		iter.hasNext(); )
 		{
 			WarlockStringMarker marker = iter.next();
 		
 			Collection<WarlockStringMarker> subList = marker.getSubMarkers();
 			if(subList != null)
 				restoreNewlines(offset, subList);
 			
 			// check to make sure we're a newline in the appropriate area
 			if(marker.getStart() < offset)
 				continue;
 			String name = marker.getStyle().getName();
 			if(name == null || !name.equals("newline"))
 				continue;
 			
 			// check if we're an empty line
 			if(marker.getStart() == 0 || textWidget.getTextRange(marker.getStart() - 1, 1).equals("\n"))
 				continue;
 			
 			// we're not an empty line, put us back into action
 			textWidget.replaceTextRange(marker.getStart(), 0, "\n");
 			// TODO: this should actually just affect markers after us... oh well.
 			WarlockStringMarker.updateMarkers(1, marker, markers);
 			iter.remove();
 		}
 	}
 	
 	private Collection<StyleRange> mergeStyleRangeLists(Collection<StyleRange> list1, Collection<StyleRange> list2) {
 		LinkedList<StyleRange> resultList = new LinkedList<StyleRange>();
 		for(StyleRange style : list1) {
 			resultList.add(style);
 		}
 		
 		mergeLoop: for(StyleRange mergingStyle : list2) {
 			for(ListIterator<StyleRange> iter = resultList.listIterator();
 			iter.hasNext(); )
 			{
 				StyleRange style = iter.next();
 				
				// if the mergeStyle came before the current style, add the
				//   mergeStyle before it and go to the next mergeStyle
 				if(style.start >= mergingStyle.start + mergingStyle.length) {
 					iter.previous();
 					iter.add(mergingStyle);
 					continue mergeLoop;
 				}
 				
				// If the mergeStyle came after the current style, continue on
 				if(mergingStyle.start >= style.start + style.length)
 					continue;
 				
 				iter.remove();
 				
 				int subStart;
 				
 				if(style.start < mergingStyle.start) {
 					subStart = mergingStyle.start;
 					StyleRange newStyle = (StyleRange)style.clone();
 					newStyle.length = mergingStyle.start - style.start;
 					iter.add(newStyle);
 				} else if(mergingStyle.start < style.start) {
 					subStart = style.start;
 					StyleRange newStyle = (StyleRange)mergingStyle.clone();
 					newStyle.length = style.start - mergingStyle.start;
 					iter.add(newStyle);
 				} else {
 					subStart = style.start;
 				}
 				
 				int subEnd;
 				if(style.start + style.length < mergingStyle.start + mergingStyle.length) {
 					subEnd = style.start + style.length;
 				} else {
 					subEnd = mergingStyle.start + mergingStyle.length;
 				}
 				
 				StyleRange newStyle = this.mergeStyleRanges(style, mergingStyle);
 				newStyle.start = subStart;
 				newStyle.length = subEnd - subStart;
 				iter.add(newStyle);
 				
 				if(style.start + style.length < mergingStyle.start + mergingStyle.length) {
 					StyleRange endStyle = (StyleRange)mergingStyle.clone();
 					endStyle.start = subEnd;
 					endStyle.length = mergingStyle.start + mergingStyle.length - subEnd;
 					iter.add(endStyle);
 				} else if(mergingStyle.start + mergingStyle.length < style.start + style.length) {
 					StyleRange endStyle = (StyleRange)style.clone();
 					endStyle.start = subEnd;
 					endStyle.length = style.start + style.length - subEnd;
 					iter.add(endStyle);
 				}
 				// else both styles end at the same time
 				
				// We matched a style and inserted the new style, so we're done
 				continue mergeLoop;
 			}
 			
 			resultList.add(mergingStyle);
 		}
 		
 		return resultList;
 	}
 	
 	private void showStyles(Collection<StyleRange> styles, int start, int end) {
 
 		try {
 			Collection<StyleRange> finalList = mergeStyleRangeLists(styles, getHighlights(start, end));
 			
 			for(StyleRange style : finalList) {
 				textWidget.setStyleRange(style);
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private Collection<StyleRange> getHighlights(int start, int end) {
 		ArrayList<StyleRange> highlightList = new ArrayList<StyleRange>();
 		if(client == null)
 			return highlightList;
 		
 		String text = textWidget.getTextRange(start, end - start);
 		
 		for (IHighlightString highlight : client.getClientSettings().getAllHighlightStrings())
 		{
 			Pattern p;
 			try {
 				p = highlight.getPattern();
 			} catch(PatternSyntaxException e) {
 				continue;
 			}
 			if(p == null)
 				continue;
 			Matcher matcher = p.matcher(text);
 			
 			while (matcher.find())
 			{
 				MatchResult result = matcher.toMatchResult();
 				
 				IWarlockStyle style = highlight.getStyle();
 				int highlightStart = result.start() + start;
 				int highlightLength = result.end() - result.start();
 				if(style.isFullLine()) {
 					int lineNum = textWidget.getLineAtOffset(highlightStart);
 					highlightStart = textWidget.getOffsetAtLine(lineNum);
 					if(lineNum + 1 >= textWidget.getLineCount())
 						highlightLength = end - highlightStart;
 					else
 						highlightLength = textWidget.getOffsetAtLine(lineNum + 1) - highlightStart;
 				}
 				highlightList.add(this.warlockStyleToStyleRange(style,
 						highlightStart, highlightLength));
 				
 				try{
 					if (style.getSound() != null && !style.getSound().equals("")){
 						//System.out.println("Playing sound " + style.getSound());
 						SoundPlayer.play(style.getSound());
 						//InputStream soundStream = new FileInputStream(style.getSound());
 						//RCPUtil.playSound(soundStream);
 					}
 				} catch(Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		return highlightList;
 	}
 	
 	private void getMarkerStyles(WarlockStringMarker marker,
 			StyleRange baseStyle, Collection<StyleRange> resultStyles) {
 		int pos = marker.getStart();
 		for(WarlockStringMarker subMarker : marker.getSubMarkers()) {
 			
 			int nextPos = subMarker.getStart();
 			
 			StyleRange styleRange = mergeStyleRanges(baseStyle,
 					warlockStyleToStyleRange(marker.getStyle(), pos, nextPos - pos));
 			
 			if(nextPos > pos)
 				resultStyles.add(styleRange);
 			
 			getMarkerStyles(subMarker, styleRange, resultStyles);
 
 			pos = subMarker.getEnd();
 		}
 		
 		if(marker.getEnd() > pos) {
 			StyleRange styleRange = mergeStyleRanges(baseStyle,
 					warlockStyleToStyleRange(marker.getStyle(), pos, marker.getEnd() - pos));
 			resultStyles.add(styleRange);
 		}
 	}
 	
 	public void append(WarlockString wstring) {
 		boolean atBottom = isAtBottom();
 		
 		int offset = textWidget.getCharCount();
 		textWidget.append(wstring.toString());
 		/* Break up the ranges and merge overlapping styles because SWT only
 		 * allows 1 style per section
 		 */
 		LinkedList<StyleRange> finishedStyles = new LinkedList<StyleRange>();
 		for(WarlockStringMarker strMarker : wstring.getStyles()) {
 			WarlockStringMarker marker = strMarker.copy(offset);
 			addNamedMarkers(marker, wstring);
 			getMarkerStyles(marker, new StyleRangeWithData(), finishedStyles);
 		}
 		showStyles(finishedStyles, offset, textWidget.getCharCount());
 		removeEmptyLines(offset);
 		
 		constrainLineLimit(atBottom);
 
 		postTextChange(atBottom);
 	}
 	
 	private void addNamedMarkers(WarlockStringMarker marker, WarlockString wstring) {
 		if(marker.getName() != null) {
 			IWarlockStyle baseStyle = wstring.getBaseStyle(marker);
 			if(baseStyle != null)
 				marker.setStyle(baseStyle);
 			this.addMarker(marker);
 		} else {
 			for(WarlockStringMarker subMarker : marker.getSubMarkers()) {
 				addNamedMarkers(subMarker, wstring);
 			}
 		}
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
 	
 	private StyleRange mergeStyleRanges(StyleRange style1, StyleRange style2) {
 		if(style1 == null)
 			return style2;
 		if(style2 == null)
 			return style1;
 		
 		StyleRange newStyle;
 		// start with a cloned style1, unless style2 has data, but style1 doesn't
 		if(style2 instanceof StyleRangeWithData && !(style1 instanceof StyleRangeWithData))
 			newStyle = new StyleRangeWithData(style1);
 		else
 			newStyle = (StyleRange)style1.clone();
 		
 		newStyle.start = style2.start;
 		newStyle.length = style2.length;
 		if(style2.font != null)
 			newStyle.font = style2.font;
 		if(style2.background != null)
 			newStyle.background = style2.background;
 		if(style2.foreground != null)
 			newStyle.foreground = style2.foreground;
 		if(style2.fontStyle != SWT.NORMAL)
 			newStyle.fontStyle = style2.fontStyle;
 		if(style2.strikeout) newStyle.strikeout = true;
 		if(style2.underline) newStyle.underline = true;
 		
 		if(style2 instanceof StyleRangeWithData) {
 			StyleRangeWithData _newStyle = (StyleRangeWithData)newStyle;
 			StyleRangeWithData _style2 = (StyleRangeWithData)style2;
 			
 			_newStyle.data.putAll(_style2.data);
 			if(_style2.action != null)
 				_newStyle.action = _style2.action;
 			if(_style2.tooltip != null)
 				_newStyle.tooltip = _style2.tooltip;
 		}
 		
 		return newStyle;
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
 		for(Iterator<WarlockStringMarker> iter = markers.iterator();
 		iter.hasNext(); )
 		{
 			WarlockStringMarker marker = iter.next();
 			
 			// If the marker is moved off the beginning, remove it
 			if(marker.getEnd() + delta < 0) {
 				iter.remove();
 				continue;
 			}
 			// move us accordingly
 			marker.move(delta);
 		}
 	}
 	
 	public void addInternalMarker(WarlockStringMarker marker,
 			LinkedList<WarlockStringMarker> markerList) {
 		ListIterator<WarlockStringMarker> iter = markerList.listIterator();
 		try {
 			while(true) {
 				if(!iter.hasNext()) {
 					iter.add(marker);
 					break;
 				}
 				WarlockStringMarker cur = iter.next();
 				if(cur.getEnd() > marker.getStart()) {
 					if(marker.getEnd() > cur.getStart()) {
 						addInternalMarker(marker, cur.getSubMarkers());
 						return;
 					}
 
 					iter.previous();
 					iter.add(marker);
 					break;
 				}
 			}
 		} catch(Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public void addMarker(WarlockStringMarker marker) {
 		ListIterator<WarlockStringMarker> iter = markers.listIterator();
 		try {
 			while(true) {
 				if(!iter.hasNext()) {
 					iter.add(marker);
 					break;
 				}
 				WarlockStringMarker cur = iter.next();
 				if(cur.getEnd() > marker.getStart()) {
 					if(marker.getEnd() > cur.getStart()) {
 						throw new Exception("Bad marker!");
 					}
 
 					iter.previous();
 					iter.add(marker);
 					break;
 				}
 			}
 		} catch(Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	
 	public void replaceMarker(String name, WarlockString text) {
 		WarlockStringMarker marker = null;
 		IWarlockStyle baseStyle = null;
 		for(WarlockStringMarker subMarker : markers) {
 			marker = getMarker(name, subMarker);
 			if(marker != null) {
 				baseStyle = subMarker.getBaseStyle(marker);
 				break;
 			}
 		}
 		if(marker == null)
 			return;
 		
 		int start = marker.getStart();
 		int length = marker.getEnd() - start;
 		boolean atBottom = isAtBottom();
 		textWidget.replaceTextRange(start, length, text.toString());
 		marker.clear();
 		int newLength = text.length();
 		marker.setEnd(start + newLength);
 		WarlockStringMarker.updateMarkers(newLength - length, marker, markers);
 		
 		// Add the new styles to the existing marker
 		for(WarlockStringMarker newMarker : text.getStyles()) {
 			marker.addMarker(newMarker.copy(start));
 		}
 		
 		/* Break up the ranges and merge overlapping styles because SWT only
 		 * allows 1 style per section
 		 */
 		WarlockStringMarker markerWithStyle = marker.clone();
 		markerWithStyle.setStyle(baseStyle);
 		LinkedList<StyleRange> newStyles = new LinkedList<StyleRange>();
 		getMarkerStyles(markerWithStyle, new StyleRangeWithData(), newStyles);
 		showStyles(newStyles, marker.getStart(), marker.getEnd());
 		
 		removeEmptyLines(start);
 		restoreNewlines(start, markers);
 		postTextChange(atBottom);
 	}
 	
 	private WarlockStringMarker getMarker(String name,
 			WarlockStringMarker marker) {
 		String myName = marker.getName();
 		if(myName.equals(name))
 			return marker;
 		
 		for(WarlockStringMarker subMarker : marker.getSubMarkers()) {
 			WarlockStringMarker result = getMarker(name, subMarker);
 			if(result != null)
 				return result;
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
