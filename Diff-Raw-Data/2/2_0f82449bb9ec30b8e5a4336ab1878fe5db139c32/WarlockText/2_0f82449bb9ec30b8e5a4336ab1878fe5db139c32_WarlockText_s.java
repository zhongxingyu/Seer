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
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ExtendedModifyListener;
 import org.eclipse.swt.custom.LineBackgroundEvent;
 import org.eclipse.swt.custom.LineBackgroundListener;
 import org.eclipse.swt.custom.PaintObjectListener;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.StyledTextContent;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.ControlListener;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GlyphMetrics;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Caret;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.ScrollBar;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.WarlockString;
 import cc.warlock.core.client.WarlockString.WarlockStringStyleRange;
 import cc.warlock.rcp.ui.style.CompassThemes;
 import cc.warlock.rcp.ui.style.StyleProviders;
 import cc.warlock.rcp.util.ColorUtil;
 import cc.warlock.rcp.util.RCPUtil;
 
 /**
  * This is an extension of the StyledText widget which has special support for embedding of arbitrary Controls/Links
  * 
  * To embed a control in the widget, add the constant OBJECT_HOLDER where you want to see your control, and then use
  * addLink / addImage / addControl to add a control for that placeholder.
  * 
  * @author Marshall
  */
 public class WarlockText implements LineBackgroundListener {
 
 	public static final char OBJECT_HOLDER = '\uFFFc';
 	
 	private StyledText textWidget;
 	private Hashtable<Object, StyleRangeWithData> objects = new Hashtable<Object, StyleRangeWithData>();
 	private Hashtable<Control, Rectangle> anchoredControls = new Hashtable<Control, Rectangle>();
 	private Color linkColor;
 	private Cursor handCursor, defaultCursor;
 	private ScrollBar vscroll;
 	private int lineLimit = 5000;
 	private int doScrollDirection = SWT.UP;
 	private IWarlockClient client;
 	private WarlockCompass compass;
 	private Menu contextMenu;
 	
 	protected Hashtable<Integer, Color> lineBackgrounds = new Hashtable<Integer,Color>();
 	
 	public WarlockText(Composite parent, int style, IWarlockClient client) {
 		textWidget = new StyledText(parent, style);
 		this.client = client;
 		
 		Display display = parent.getDisplay();
 		linkColor = new Color(display, 0xF0, 0x80, 0);
 		handCursor = new Cursor(display, SWT.CURSOR_HAND);
 		defaultCursor = parent.getCursor();
 		vscroll = getVerticalBar ();
 		contextMenu = new Menu(textWidget);
 		MenuItem itemCopy = new MenuItem(contextMenu, SWT.PUSH);
 		itemCopy.addSelectionListener(new SelectionAdapter() {
 		            public void widgetSelected(SelectionEvent arg0) {
 		                textWidget.copy();
 		            }
 		        });
 		itemCopy.setText("Copy");
 		textWidget.setMenu(contextMenu);
 		
 		addVerifyListener(new VerifyListener()  {
 			public void verifyText(VerifyEvent e) {
 				int start = e.start;
 				int replaceCharCount = e.end - e.start;
 				int nHolder = 0;
 				for (Iterator<Object> iter = objects.keySet().iterator(); iter.hasNext(); )
 				{
 					Object object = iter.next();
 					
 					int offset = getHolderOffset(nHolder);
 					if (start <= offset && offset < start + replaceCharCount) {
 						// this control is being deleted from the text
 						if (object instanceof Control)
 						{
 							Control control = (Control) object;
 							if (control != null && !control.isDisposed()) {
 								control.dispose();
 								iter.remove();
 							}
 							offset = -1;
 						}
 					}
 					nHolder++;
 				}
 			}
 		});
 		
 		addMouseMoveListener(new MouseMoveListener() {
 			public void mouseMove(MouseEvent e) {
 				try {
 					if (!isDisposed() && isVisible())
 					{
 						Point point = new Point(e.x, e.y);
 						int offset = getOffsetAtLocation(point);
 						StyleRange range = getStyleRangeAtOffset(offset);
 						if (range != null && range instanceof StyleRangeWithData)
 						{
 							StyleRangeWithData range2 = (StyleRangeWithData) range;
 							if (range2.data.containsKey("link.url"))
 							{
 								setCursor(handCursor);
 								return;
 							}
 						
 						}
 						setCursor(defaultCursor);
 					}
 				} catch (IllegalArgumentException ex) {
 					// swallow -- this happens if the mouse cursor moves to an area not covered by the imaginary rectangle surround the current text
 					setCursor(defaultCursor);
 				}
 			}
 		});
 		
 		addMouseListener(new MouseListener () {
 			public void mouseDoubleClick(MouseEvent e) {}
 			public void mouseDown(MouseEvent e) {}
 			public void mouseUp(MouseEvent e) {
 				try {
 					Point point = new Point(e.x, e.y);
 					int offset = getOffsetAtLocation(point);
 					StyleRange range = getStyleRangeAtOffset(offset);
 					if (range != null && range instanceof StyleRangeWithData)
 					{
 						StyleRangeWithData range2 = (StyleRangeWithData) range;
 						if (range2.data.containsKey("link.url"))
 						{
 							RCPUtil.openURL(range2.data.get("link.url"));
 						}
 					}
 				} catch (IllegalArgumentException ex) {
 					// swallow -- see note above
 				}
 			}
 		});
 		
 		addLineBackgroundListener(this);
 		addControlListener(new ControlListener () {
 			public void controlMoved(ControlEvent e) {}
 			public void controlResized(ControlEvent e) {
 				redraw();
 			}
 		});
 	}
 	
 	public void addCompass ()
 	{
 		compass = new WarlockCompass(this, CompassThemes.getCompassTheme("small"));
 	}
 	
 	public WarlockCompass getCompass ()
 	{
 		return compass;
 	}
 	
 	public void addLineBackgroundListener(LineBackgroundListener listener) {
 		textWidget.addLineBackgroundListener(listener);
 	}
 	
 	public void addControlListener(ControlListener listener) {
 		textWidget.addControlListener(listener);
 	}
 	
 	public void addPaintListener(PaintListener listener) {
 		textWidget.addPaintListener(listener);
 	}
 	
 	public void addMouseListener(MouseListener listener) {
 		textWidget.addMouseListener(listener);
 	}
 	
 	public void addMouseMoveListener(MouseMoveListener listener) {
 		textWidget.addMouseMoveListener(listener);
 	}
 	
 	public void addPaintObjectListener(PaintObjectListener listener) {
 		textWidget.addPaintObjectListener(listener);
 	}
 	
 	public void addVerifyListener(VerifyListener verifyListener) {
 		textWidget.addVerifyListener(verifyListener);
 	}
 	
 	public void addExtendedModifyListener(ExtendedModifyListener extendedModifyListener) {
 		textWidget.addExtendedModifyListener(extendedModifyListener);
 	}
 	
 	public void addFocusListener(FocusListener listener) {
 		textWidget.addFocusListener(listener);
 	}
 	
 	public void addKeyListener(KeyListener listener) {
 		textWidget.addKeyListener(listener);
 	}
 	
 	public Rectangle getBounds() {
 		return textWidget.getBounds();
 	}
 	
 	public Display getDisplay() {
 		return textWidget.getDisplay();
 	}
 	
 	public ScrollBar getVerticalBar() {
 		return textWidget.getVerticalBar();
 	}
 	
 	public boolean isDisposed() {
 		return textWidget.isDisposed();
 	}
 	
 	public boolean isVisible() {
 		return textWidget.isVisible();
 	}
 	
 	public void setBackgroundMode(int mode) {
 		textWidget.setBackgroundMode(mode);
 	}
 	
 	public void setCursor(Cursor cursor) {
 		textWidget.setCursor(cursor);
 	}
 	
 	public int getOffsetAtLocation(Point point) {
 		return textWidget.getOffsetAtLocation(point);
 	}
 	
 	public StyleRange getStyleRangeAtOffset(int offset) {
 		return textWidget.getStyleRangeAtOffset(offset);
 	}
 	
 	public void redraw() {
 		textWidget.redraw();
 	}
 	
 	public void redraw(int x, int y, int width, int height, boolean all) {
 		textWidget.redraw(x, y, width, height, all);
 	}
 	
 	public void selectAll() {
 		textWidget.selectAll();
 	}
 	
 	public void copy() {
 		textWidget.copy();
 	}
 	
 	public void setFocus() {
 		textWidget.setFocus();
 	}
 	
 	public Rectangle getClientArea() {
 		return textWidget.getClientArea();
 	}
 	
 	public void setLayoutData(Object layoutData) {
 		textWidget.setLayoutData(layoutData);
 	}
 	
 	public void setEditable(boolean editable) {
 		textWidget.setEditable(editable);
 	}
 	
 	public void setWordWrap(boolean wrap) {
 		textWidget.setWordWrap(wrap);
 	}
 	
 	public void setBackground(Color color) {
 		textWidget.setBackground(color);
 	}
 	
 	public void setForeground(Color color) {
 		textWidget.setForeground(color);
 	}
 	
 	public void setText(String text) {
 		textWidget.setText(text);
 	}
 	
 	public Font getFont() {
 		return textWidget.getFont();
 	}
 	
 	public void setFont(Font font) {
 		textWidget.setFont(font);
 	}
 	
 	public int getCharCount() {
 		return textWidget.getCharCount();
 	}
 	
 	private int getCurrentHolderOffset ()
 	{
 		return getHolderOffset(objects.keySet().size());
 	}
 	
 	public String getText() {
 		return textWidget.getText();
 	}
 	
 	private int getHolderOffset (int nHolder)
 	{
 		String text = getText();
 		return text.indexOf(OBJECT_HOLDER);
 	}
 	
 	public void update() {
 		textWidget.update();
 	}
 	
 	public void addAnchoredControl (Control control, Rectangle dimensions)
 	{
 		anchoredControls.put(control, dimensions);
 		
 		update();
 	}
 	
 	public void addControls (Control[] controls)
 	{
 		int i = 0;
 		for (Control ctrl : controls)
 		{
 			StyleRangeWithData style = new StyleRangeWithData();
 			style.start = getHolderOffset(this.objects.keySet().size() + i);
 			style.length = 1;
 			Rectangle rect = ctrl.getBounds();
 			style.metrics = new GlyphMetrics(rect.height, 0, rect.width);
 			
 			this.objects.put(ctrl, style);
 			textWidget.setStyleRange(style);
 			
 			i++;
 		}
 		update();
 	}
 	
 	public void addImage (Image image) {
 		Label label = new Label(textWidget, SWT.NONE);
 		label.setImage(image);
 		label.setSize(image.getBounds().width, image.getBounds().width);
 		
 		addControls (new Control[] { label });		
 	}
 	
 	public void addLink (String url, String description)
 	{
 		int start = getCurrentHolderOffset();
 		replaceTextRange(start, 1, description);
 		
 		StyleRangeWithData range = new StyleRangeWithData();
 		range.foreground = linkColor;
 		range.underline = true;
 		range.start = start;
 		range.length = description.length();
 		textWidget.setStyleRange(range);
 		range.data.put("link.url", url);
 	}
 	
 	public void setLineLimit(int limit) {
 		lineLimit = limit;
 	}
 	
 	public void append(String string) {
 		ControlStatus status = preTextChange();
 		
 		textWidget.append(string);
 		status = constrainLineLimit(status);
 
 		postTextChange(status);
 	}
 	
 	public void append(WarlockString string) {
 		ControlStatus status = preTextChange();
 		
 		int charCount = textWidget.getCharCount();
 		textWidget.append(string.toString());
 		
 		/* Break up the ranges and merge overlapping styles because SWT only
 		 * allows 1 style per section
 		 */
 		ArrayList<WarlockStringStyleRange> currentStyles = new ArrayList<WarlockStringStyleRange>();
 		ArrayList<StyleRangeWithData> finishedStyles = new ArrayList<StyleRangeWithData>();
 		List<WarlockStringStyleRange> styles = string.getStyles();
 		int pos = 0;
 		while(pos >= 0) {
 			// update current styles
 			for(WarlockStringStyleRange style : styles) {
 				if(style.start == pos) {
 					currentStyles.add(style);
 				} else if(style.start + style.length == pos) {
 					currentStyles.remove(style);
 				}
 			}
 			
 			// create style segment for pos to next pos
 			int foundPos = findNextEvent(styles, pos + 1);
 			int nextPos;
 			if(foundPos < 0)
 				nextPos = string.length() - 1;
 			else
 				nextPos = foundPos;
 			if(currentStyles.size() > 0) {
 				// merge all of the styles
 				StyleRangeWithData style = warlockStringStyleRangeToStyleRange(currentStyles.get(0), charCount);
 				for(int i = 1; i < currentStyles.size(); i++) {
 					StyleRangeWithData nextStyle = warlockStringStyleRangeToStyleRange(currentStyles.get(i), charCount);
 					if(nextStyle.font != null)
 						style.font = nextStyle.font;
 					if(nextStyle.background != null)
 						style.background = nextStyle.background;
 					if(nextStyle.foreground != null)
 						style.foreground = nextStyle.foreground;
 					if(nextStyle.fontStyle != SWT.NORMAL)
 						style.fontStyle = nextStyle.fontStyle;
 					if(nextStyle.strikeout) style.strikeout = true;
 					if(nextStyle.underline) style.underline = true;
 				}
 				style.start = charCount + pos;
 				style.length = nextPos - pos;
 				finishedStyles.add(style);
 			}
 			
 			pos = foundPos;
 		}
 		
 		for(StyleRangeWithData style : finishedStyles) {
 			textWidget.setStyleRange(style);
 		}
 		
 		status = constrainLineLimit(status);
 
 		postTextChange(status);
 	}
 	
 	/* find an element in styles that intersects with the first element of styles, starting at pos */
 	private int findNextEvent(List<WarlockStringStyleRange> styles, int pos) {
 		int nextPos = -1;
 		for(WarlockStringStyleRange style : styles) {
 			if(style.start >= pos) {
 				if(nextPos < 0 || style.start < nextPos)
 					nextPos = style.start;
 			} else if(style.start + style.length >= pos) {
 				if(nextPos < 0 || style.start + style.length < nextPos)
 					nextPos = style.start + style.length;
 			}
 		}
 		return nextPos;
 	}
 	
 	private StyleRangeWithData warlockStringStyleRangeToStyleRange(WarlockStringStyleRange range, int offset) {
 		StyleRangeWithData styleRange = (StyleRangeWithData)StyleProviders.getStyleProvider(client).getStyleRange(range.style);
 		if(styleRange == null)
 			return null;
 		if(range.style.getFGColor() != null)
 			styleRange.foreground = ColorUtil.warlockColorToColor(range.style.getFGColor());
 		if(range.style.getBGColor() != null)
 			styleRange.background = ColorUtil.warlockColorToColor(range.style.getBGColor());
 		if(range.style.isFullLine()) {
 			int lineNum = textWidget.getLineAtOffset(offset + range.start);
 			styleRange.start = textWidget.getOffsetAtLine(lineNum);
 			styleRange.length = textWidget.getOffsetAtLine(lineNum + 1) - styleRange.start;
 			setLineBackground(lineNum, styleRange.background);
 		} else {
 			styleRange.start = offset + range.start;
 			styleRange.length = range.length;
 		}
 		return styleRange;
 	}
 	
 	class ControlStatus {
 		boolean atBottom;
 		int caretOffset;	
 		Point selection;
 	}
 	
 	private ControlStatus preTextChange() {
 		ControlStatus status = new ControlStatus();
 		status.atBottom = atBottom();
 		status.caretOffset = getCaretOffset();
 		status.selection = textWidget.getSelection();
 		return status;
 	}
 	
 	private void postTextChange(ControlStatus status) {
 		if (status.atBottom) scrollToBottom();
 		if (status.selection.x != status.selection.y) // Only set it if there is something selected
			textWidget.setSelection(status.selection);
 		setCaretOffset(status.caretOffset);
 	}
 	
 	private void scrollToBottom() {
 		if (doScrollDirection == SWT.DOWN) {
 			textWidget.invokeAction(ST.TEXT_END);
 			if (compass != null)
 				compass.redraw();
 		}
 	}
 	
 	private boolean atBottom() {
 		return vscroll.getSelection() >= (vscroll.getMaximum()
 				- (vscroll.getPageIncrement() * 1.5));
 	}
 	
 	public void replaceTextRange(int start, int length, String text) {
 		ControlStatus status = preTextChange();
 		textWidget.replaceTextRange(start, length, text);
 		postTextChange(status);
 	}
 	
 	public int getLineCount() {
 		return textWidget.getLineCount();
 	}
 	
 	public int getOffsetAtLine(int lineIndex) {
 		return textWidget.getOffsetAtLine(lineIndex);
 	}
 	
 	public int getTopIndex() {
 		return textWidget.getTopIndex();
 	}
 	
 	private ControlStatus constrainLineLimit(ControlStatus status) {
 		// 'status' is a pointer that allows us to change the object in our parent..
 		// in this method... it is intentional.
 		if (lineLimit > 0) {
 			int len = getLineCount();
 			if (len > lineLimit) {
 				int top = getTopIndex();
 				int toRemove = len - lineLimit;
 				int offset = getOffsetAtLine(toRemove);
 				
 				// adjust ending status
 				status.caretOffset = status.caretOffset - offset;
 				if (status.caretOffset < 0) status.caretOffset = 0; // Don't let this go negative
 				status.selection.x = status.selection.x - offset;
 				if (status.selection.x < 0) status.selection.x = 0; // Don't let this go negative
 				status.selection.y = status.selection.y - offset;
 				if (status.selection.y < 0) status.selection.y = 0; // Don't let this go negative
 				
 				replaceTextRange(0,offset,"");
 				updateLineBackgrounds(toRemove);
 				if(!status.atBottom)
 					setTopIndex(top - toRemove);
 			}
 		}
 		return status;
 	}
 	
 	private void updateLineBackgrounds (int lines)
 	{
 		Hashtable<Integer, Color> copy = (Hashtable<Integer, Color>) lineBackgrounds.clone(); 
 		lineBackgrounds.clear();
 		
 		for (int lineIndex : copy.keySet())
 		{
 			if (lineIndex > lines)
 			{
 				lineBackgrounds.put(lineIndex - lines, copy.get(lineIndex));
 			}
 		}
 	}
 	
 	public void lineGetBackground(LineBackgroundEvent event) {
 		int lineIndex = getLineAtOffset(event.lineOffset);
 		boolean hasBackground = lineBackgrounds.containsKey(lineIndex);
 		
 		if (hasBackground)
 		{
 			event.lineBackground = lineBackgrounds.get(lineIndex);
 		}
 	}
 	
 	public void setLineBackground (int line, Color background)
 	{
 		lineBackgrounds.put(line, background);
 	}
 	
 	public int getLineAtOffset(int offset) {
 		return textWidget.getLineAtOffset(offset);
 	}
 	
 	public void setTopIndex(int topIndex) {
 		textWidget.setTopIndex(topIndex);
 	}
 	
 	public void setScrollDirection(int dir) {
 		if (dir == SWT.DOWN || dir == SWT.UP)
 			doScrollDirection = dir;
 		// TODO: Else throw an error
 	}
 	
 	public int getCaretOffset() {
 		return textWidget.getCaretOffset();
 	}
 	
 	public void setCaretOffset(Caret caret) {
 		textWidget.setCaret(caret);
 	}
 	
 	public void setCaretOffset(int offset) {
 		textWidget.setCaretOffset(offset);
 	}
 	
 	public void showSelection() {
 		textWidget.showSelection();
 	}
 	
 	public StyledTextContent getContent() {
 		return textWidget.getContent();
 	}
 
 	public StyledText getTextWidget() {
 		return textWidget;
 	}
 	
 	public IWarlockClient getClient() {
 		return client;
 	}
 }
