 package com.arcaner.warlock.rcp.ui;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Hashtable;
 import java.util.Iterator;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.PaintObjectEvent;
 import org.eclipse.swt.custom.PaintObjectListener;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.GlyphMetrics;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 /**
  * This is an extension of the StyledText widget which has special support for embedding of arbitrary Controls/Links
  * 
  * To embed a control in the widget, add the constant OBJECT_HOLDER where you want to see your control, and then use
  * addLink / addImage / addControl to add a control for that placeholder.
  * 
  * @author Marshall
  */
 public class WarlockText extends StyledText {
 
 	public static final char OBJECT_HOLDER = '\uFFFc';
 	
 	private Hashtable<Object, StyleRangeWithData> objects = new Hashtable<Object, StyleRangeWithData>();
 	private Hashtable<Control, Rectangle> anchoredControls = new Hashtable<Control, Rectangle>();
 	private Color linkColor;
 	private Cursor handCursor, defaultCursor;
 	private PaletteData palette;
 	
 	public WarlockText(Composite parent, int style) {
 		super(parent, style);
 		
 		Display display = parent.getDisplay();
 		linkColor = new Color(display, 0xF0, 0x80, 0);
 		handCursor = new Cursor(display, SWT.CURSOR_HAND);
 		defaultCursor = parent.getCursor();
 		palette = new PaletteData(new RGB[] { display.getSystemColor(SWT.COLOR_WHITE).getRGB(), display.getSystemColor(SWT.COLOR_BLACK).getRGB() });
 		
 		addVerifyListener(new VerifyListener()  {
 			public void verifyText(VerifyEvent e) {
 				int start = e.start;
 				int replaceCharCount = e.end - e.start;
 				int newCharCount = e.text.length();
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
 		
 		addPaintObjectListener(new PaintObjectListener() {
 			public void paintObject(PaintObjectEvent event) {
 				GC gc = event.gc;
 				FontMetrics metrics = gc.getFontMetrics();
 				StyleRange style = event.style;
 				for (Iterator<Object> iter = objects.keySet().iterator(); iter.hasNext(); )
 				{
 					Object object = iter.next();
 					
 					if (object instanceof Control)
 					{
 						Control control = (Control) object;
 						style.metrics.ascent =  control.getBounds().height;
 
 						int x = event.x;
 						int y = event.y + event.ascent - style.metrics.ascent;
 						control.setLocation(x, y);
 					}
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
 							openURL(range2.data.get("link.url"));
 						}
 					}
 				} catch (IllegalArgumentException ex) {
 					// swallow -- see note above
 				}
 			}
 		});
 		
 		addPaintListener(new PaintListener () {
 			public void paintControl(PaintEvent e) {
 				for (Control anchoredControl : anchoredControls.keySet())
 				{
 					Rectangle rect = anchoredControls.get(anchoredControl);
 									
 					anchoredControl.setLocation(rect.x, rect.y);
 					anchoredControl.setSize(rect.width, rect.height);
 					
 //					ImageData data = new ImageData(rect.width, rect.height, 24, palette);
 //					
 //					data.transparentPixel = SWT.COLOR_BLUE;
 //					anchoredControl.setBackgroundImage(new Image(getDisplay(), data));
 				}
				update();
 			}
 		});
 	}
 	
 	private int getCurrentHolderOffset ()
 	{
 		return getHolderOffset(objects.keySet().size());
 	}
 	
 	private int getHolderOffset (int nHolder)
 	{
 		String text = getText();
 		return text.indexOf(OBJECT_HOLDER);
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
 			setStyleRange(style);
 			
 			i++;
 		}
 	}
 	
 	public void addImage (Image image) {
 		Label label = new Label(this, SWT.NONE);
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
 		setStyleRange(range);
 		range.data.put("link.url", url);
 	}
 	
 	private void openURL (String url)
 	{
 		System.out.println("open  url: " + url);
 		try {
 			PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
 		} catch (PartInitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
