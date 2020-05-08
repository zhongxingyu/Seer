 /*******************************************************************************
  * Copyright (c) 2012 Derek Qian.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Derek Qian - initial API and implementation
  ******************************************************************************/
 package edu.pdx.svl.coDoc.poppler.editor;
 
 import java.awt.Dimension;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.MouseMoveListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 
 import edu.pdx.svl.coDoc.poppler.lib.PDFDestination;
 import edu.pdx.svl.coDoc.poppler.lib.GoToAction;
 import edu.pdx.svl.coDoc.poppler.lib.UriAction;
 import edu.pdx.svl.coDoc.poppler.lib.LinkAnnotation;
 
 import edu.pdx.svl.coDoc.poppler.handler.PageSelectCombo;
 import edu.pdx.svl.coDoc.poppler.handler.ToggleLinkHighlightHandler;
 import edu.pdx.svl.coDoc.poppler.lib.PopplerJNI;
 
 
 /**
  * SWT Canvas which shows a whole pdf-page. It also handles click on links.
  * 
  * @author Derek Qian
  *
  */
 public class PDFPageViewer extends Canvas implements PaintListener, IPreferenceChangeListener{
     
     /** The current PDFPage that was rendered into currentImage */
     public int currentPageNum;
     /** the current transform from device space to page space */
     AffineTransform currentXform;
     /** The horizontal offset of the image from the left edge of the panel */
     int offx;
     /** The vertical offset of the image from the top of the panel */
     int offy;
     
     private boolean highlightLinks;
     private Rectangle[] highlight;
     
     IPDFEditor editor;
     private ScrolledComposite sc;    
     private Display display;
     private PopplerJNI poppler;
     
     private float zoomFactor;    
     
     private Vector<PDFSelection> Selection;
     private Vector<String> text;
     private boolean ctrlPressed;
     private Point startPoint;
     private Point stopPoint;
     
 
     /**
      * Create a new PagePanel.
      */
     public PDFPageViewer(Composite parent, final IPDFEditor editor, final PopplerJNI poppler) {
         //super(parent, SWT.NO_BACKGROUND|SWT.NO_REDRAW_RESIZE);
     	//super(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE);
     	super(parent, SWT.NO_BACKGROUND);
     	sc = (ScrolledComposite) parent;
 
     	this.addMouseListener(new MouseListener() {			
 			@Override
 			public void mouseUp(MouseEvent e) {				
 				stopPoint.x = e.x;
 				stopPoint.y = e.y;
 				PDFSelection pdfsel = rectToPdfsel(new Rectangle(startPoint.x,startPoint.y,stopPoint.x-startPoint.x,stopPoint.y-startPoint.y));
 				poppler.document_get_page(pdfsel.getPage());
 				poppler.page_get_size();
 				String tempstr = poppler.page_get_selected_text(1.0, new Rectangle(pdfsel.getLeft(),pdfsel.getTop(),pdfsel.getRight()-pdfsel.getLeft(),pdfsel.getBottom()-pdfsel.getTop()));
 				poppler.document_release_page();
 				System.out.println("\n=======================");
 				System.out.println(tempstr);
 				System.out.println("=======================\n");
 				if(tempstr.length() != 0) {
 					Selection.add(pdfsel);
 					text.add(tempstr);
 				}
 				highlight(Selection);
 				redraw();
 			}
 			
 			@Override
 			public void mouseDown(MouseEvent e) {				
 				if (e.button != 1) return;
 				ctrlPressed = ((e.stateMask & SWT.CONTROL) != 0);
 				if(!ctrlPressed) {
 					Selection.clear();
 					text.clear();
 				}
 				
 				startPoint.x = e.x;
 				startPoint.y = e.y;
 				highlight = null;
 				redraw();
 				
 				//derek List<PDFAnnotation> annos = getPage().getAnnots(PDFAnnotation.LINK_ANNOTATION);
 				List<LinkAnnotation> annos = new ArrayList<LinkAnnotation>();
             	for (LinkAnnotation a : annos) {
             		LinkAnnotation aa = (LinkAnnotation) a;
             		Rectangle2D r = convertPDF2ImageCoord(aa.getRect());
             		if (r.contains(e.x, e.y)) {
             			if (aa.getAction() instanceof GoToAction){
             				final GoToAction action = (GoToAction) aa.getAction();
 
             				Display.getDefault().asyncExec(new Runnable() {
 								@Override
 								public void run() {
 									editor.gotoAction(action.getDestination());
 								}
 							});
             				return;
             			}
             			else if (aa.getAction() instanceof UriAction) {
             				final UriAction action = (UriAction) aa.getAction();
             				Display.getDefault().asyncExec(new Runnable() {
 								
 								@Override
 								public void run() {
 									
 									try {
 										String uri = action.getUri();
 										if (uri.toLowerCase().indexOf("://") < 0) { //$NON-NLS-1$
 											uri = "http://"+uri; //$NON-NLS-1$
 										}
 										PlatformUI.getWorkbench().getBrowserSupport()
 										.createBrowser("PDFBrowser").openURL(new URL(uri)); //$NON-NLS-1$
 									} catch (PartInitException e) {
 										e.printStackTrace();
 									} catch (MalformedURLException e) {
 										editor.writeStatusLineError(e.getMessage());
 									}
 								}
 							});
             				return;
             			}
             		}
 				}
 			}
 			
 			@Override
 			public void mouseDoubleClick(org.eclipse.swt.events.MouseEvent e) {				
 				System.out.println(e);
 				if (e.button != 1) return;
 				
 				final Rectangle2D r = convertImage2PDFCoord(new java.awt.Rectangle(e.x, e.y, 1, 1));
 
 				Display.getDefault().asyncExec(new Runnable() {					
 					@Override
 					public void run() {
 						//derek editor.reverseSearch(r.getX(), currentPageNum.getHeight() - r.getY());
 					}
 				});
 			}
 		});
     	
     	this.addMouseMoveListener(new MouseMoveListener(){
 			@Override
 			public void mouseMove(MouseEvent e) {
 				if((e.stateMask & SWT.BUTTON1) == 0) return;
 				//if((e.stateMask & SWT.COMMAND) != 0);
 				
 				stopPoint.x = e.x;
 				stopPoint.y = e.y;
 				Vector<PDFSelection> tempsel = new Vector<PDFSelection>();
 				tempsel.addAll(Selection);
 				PDFSelection pdfsel = rectToPdfsel(new Rectangle(startPoint.x,startPoint.y,stopPoint.x-startPoint.x,stopPoint.y-startPoint.y));
 				tempsel.add(pdfsel);
 				highlight(tempsel);
 				redraw();
 			}
 		});
 
 		this.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				int height = sc.getClientArea().height;
 				int pInc = 3* height / 4;
 				int lInc = height / 20;
 				int hInc = sc.getClientArea().width / 20;
 				int pheight = sc.getContent().getBounds().height;
 				Point p = sc.getOrigin();
 				if (e.keyCode == SWT.PAGE_DOWN) {
 					if (p.y < pheight - height) {
 						//We are not at the end of the page
 						int y = p.y + pInc;
 						if (y > pheight - height) {
 							y = pheight - height;
 						}
 						sc.setOrigin(sc.getOrigin().x, y);
 					}
 				}
 				else if (e.keyCode == SWT.PAGE_UP) {
 					if (p.y > 0) {
 						//We are not at the top of the page
 						int y = p.y - pInc;
 						if (y < 0) y = 0;
 						sc.setOrigin(sc.getOrigin().x, y);
 					}					
 				}
 				else if (e.keyCode == SWT.ARROW_DOWN) {
 					if (p.y < pheight - height) {
 						sc.setOrigin(sc.getOrigin().x, p.y + lInc);
 					}					
 				}
 				else if (e.keyCode == SWT.ARROW_UP) {
 					if (p.y > 0) {
 						int y = p.y - lInc;
 						if (y < 0) y = 0;
 						sc.setOrigin(sc.getOrigin().x, y);
 					}					
 				}
 				else if (e.keyCode == SWT.ARROW_RIGHT) {
 					if (p.x < sc.getContent().getBounds().width - sc.getClientArea().width) {
 						sc.setOrigin(p.x + hInc, sc.getOrigin().y);
 					}
 				}
 				else if (e.keyCode == SWT.ARROW_LEFT) {
 					if (p.x > 0) {
 						int x = p.x - hInc;
 						if (x < 0) x = 0;
 						sc.setOrigin(x, sc.getOrigin().y);
 					}					
 				}
 				else if (e.keyCode == SWT.HOME) {
 					setOrigin(sc.getOrigin().x, 0);
 				}
 				else if (e.keyCode == SWT.END) {
 					setOrigin(sc.getOrigin().x, pheight);
 				}	
 
 			}
 		});
 		
 		this.editor = editor;
 
 		Selection = new Vector<PDFSelection>();
 		text = new Vector<String>();
 		ctrlPressed = false;
 		startPoint = new Point(-1,-1);
 		stopPoint = new Point(-1,-1);
 
     	display = parent.getDisplay();
     	this.poppler = poppler;
 		int pages = poppler.document_get_n_pages();
     	poppler.document_get_page(1);
     	Dimension size = poppler.page_get_size();
     	poppler.document_release_page();
         setSize(size.width, size.height*pages);
         zoomFactor = 1.f;
         
         if(PageSelectCombo.getInstance() != null) {
 	        PageSelectCombo.getInstance().removeAll();
 	        for(int i=1; i<=pages; i++) {
 		        PageSelectCombo.getInstance().add(Integer.toString(i));        	
 	        }
	        PageSelectCombo.getInstance().select(0);
 	        //PageSelectCombo.getInstance().redraw();        	
         }
         
         this.addPaintListener(this);
         
         IEclipsePreferences prefs = (new InstanceScope()).getNode(edu.pdx.svl.coDoc.poppler.Activator.PLUGIN_ID);
 		prefs.addPreferenceChangeListener(this);
 		
 		highlightLinks = prefs.getBoolean(ToggleLinkHighlightHandler.PREF_LINKHIGHTLIGHT_ID, true);
     }
 
     private Rectangle pdfselToRect(PDFSelection sel) {
     	poppler.document_get_page(1);
     	Dimension size = poppler.page_get_size();
     	poppler.document_release_page();
     	Rectangle rect = new Rectangle(sel.getLeft(),sel.getTop()+(sel.getPage()-1)*size.height,sel.getRight()-sel.getLeft(),sel.getBottom()-sel.getTop());
     	return rect;
     }
     private PDFSelection rectToPdfsel(Rectangle rect) {
     	poppler.document_get_page(1);
     	Dimension size = poppler.page_get_size();
     	poppler.document_release_page();
     	int page = rect.y/size.height + 1;
     	int left = rect.x;
     	int top = rect.y % size.height;
     	int right = left + rect.width;
     	int bottom = top + rect.height;
     	PDFSelection sel = new PDFSelection(page,left,top,right,bottom);
     	return sel;
     }
     private Rectangle filerectToDisprect(int page, Rectangle drect) {
     	poppler.document_get_page(1);
     	Dimension size = poppler.page_get_size();
     	poppler.document_release_page();
     	return new Rectangle(drect.x,drect.y+(page-1)*size.height,drect.width,drect.height);
     }
 
     /**
      * Highlights the rectangle given by the upper left and lower right 
      * coordinates. The highlight is visible after the next redraw.
      * @param x
      * @param y
      * @param x2
      * @param y2
      */
     public void highlight(Vector<PDFSelection> selection2) {
     	Vector<Rectangle> rects = new Vector<Rectangle>();
     	Iterator it = selection2.iterator();
     	while(it.hasNext()) {
     		PDFSelection sel = (PDFSelection) it.next();
     		Rectangle rect = new Rectangle(sel.getLeft(),sel.getTop(),sel.getRight()-sel.getLeft(),sel.getBottom()-sel.getTop());
     		poppler.document_get_page(sel.getPage());
     		poppler.page_get_size();
 	    	for(Rectangle rc : poppler.page_get_selected_region(1.0, rect)) {
 	    		rects.add(filerectToDisprect(sel.getPage(),rc));
 	    	}
 	    	poppler.document_release_page();
     	}
     	highlight = rects.toArray(new Rectangle[0]);
     }
     
     public Vector<PDFSelection> getSelection() {
     	return Selection;
     }
     
     public Vector<String> getSelectedText() {
     	return text;
     }
     
     public void selectText(Vector<PDFSelection> selection) {
 		highlight(selection);
 		redraw();
     }
     
     public void selectTextAndReveal(Vector<PDFSelection> selection) {
 		if(!selection.isEmpty()) {
 			highlight(selection);
 			Rectangle rect = sc.getClientArea();
 			PDFSelection sel = selection.get(0);
 	    	int page = sel.getPage();
 	    	poppler.document_get_page(page);
 	    	Dimension size = poppler.page_get_size();
 	    	poppler.document_release_page();
 	    	int y = (page-1)*size.height;
 	    	if(rect.height/2 < sel.getTop()) {
 	    		y += sel.getTop() - rect.height/2;
 	    	}
 	    	setOrigin(sc.getOrigin().x, y);			
 			redraw();
 		}
     }
 
     /**
      * Stop the generation of any previous page, and draw the new one.
      * @param page the PDFPage to draw.
      */
     public void showPage(int page) {
     	// set up the new page
     	currentPageNum = page;
 
     	Point sz = getSize();
     	if (sz.x == 0 || sz.y == 0) return;
     	
     	poppler.document_get_page(1);
     	Dimension size = poppler.page_get_size();
     	poppler.document_release_page();
     	setOrigin(sc.getOrigin().x, (page-1)*size.height);
 
     	//Dimension pageSize = new Dimension(size.width,size.height);
     	//ImageInfo info = new ImageInfo(pageSize.width, pageSize.height, null, Color.WHITE);
     	
     	// calculate the transform from screen to page space
     	//derek currentXform = page.getInitialTransform(pageSize.width, pageSize.height, null);
     	//derek try {
     	//derek currentXform = currentXform.createInverse();
     	//derek } catch (NoninvertibleTransformException nte) {
     		//System.out.println(Messages.PDFPageViewer_Error1);
     	//derek nte.printStackTrace();
     	//derek }
 
 		//Resize triggers repaint
 		//setSize(Math.round(zoomFactor*size.width), Math.round(zoomFactor*size.height));
 		redraw();
     }
 
     private Rectangle getRectangle(Rectangle2D r) {
     	return new Rectangle((int)Math.round(r.getX()), (int)Math.round(r.getY()), (int)Math.round(r.getWidth()), (int)Math.round(r.getHeight()));
     }
     
     public Rectangle2D convertPDF2ImageCoord(Rectangle2D r) {
     	//derek if (currentImage == null) return null;
     	int imwid = 0;//derek currentImage.getWidth(null);
     	int imhgt = 0;//derek currentImage.getHeight(null);
     	//derek AffineTransform t = currentPageNum.getInitialTransform(imwid, imhgt, null);
     	AffineTransform t = null;
     	Rectangle2D tr = t.createTransformedShape(r).getBounds2D();
     	tr.setFrame(tr.getX() + offx, tr.getY() + offy, tr.getWidth(), tr.getHeight());
     	return tr;    	
     }
     
     public Rectangle2D convertImage2PDFCoord(Rectangle2D r) {
     	//derek if (currentImage == null) return null;
 
     	r.setFrame(r.getX() - offx, r.getY() -offy, 1, 1);
     	Rectangle2D tr = currentXform.createTransformedShape(r).getBounds2D();
     	tr.setFrame(tr.getX(), tr.getY(), tr.getWidth(), tr.getHeight());
     	return tr;    	
     }
     
     /**
      * Sets the zoom factor and rerenders the current page.
      * @param factor 0 < factor < \infty
      */
     public void setZoomFactor(float factor) {
     	assert (factor > 0);
     	zoomFactor = factor;
     	showPage(currentPageNum);
     }
     
     /**
      * Returns the current used zoom factor
      * @return
      */
     public float getZoomFactor() {
     	return zoomFactor;
     }
     
     /**
      * Draw the image.
      */
     public void paintControl(PaintEvent event) 
     {
     	GC g = event.gc;
         Point sz = getSize();
     	
     	int pdfPageHeight = sz.y / poppler.document_get_n_pages();
     	ScrollBar vBar = sc.getVerticalBar();
     	int page1 = vBar.getSelection() / pdfPageHeight;
     	int page2 = (vBar.getSelection() + vBar.getThumb() - 1) / pdfPageHeight;
     	System.out.println(page1 +","+ page2);
     	for(int i=page1; i<=page2; i++) {
 	    	poppler.document_get_page(i+1);
 	    	Dimension size = poppler.page_get_size();
 	    	ImageData imgdata = new ImageData(size.width, size.height, 32, new PaletteData(0x0000FF00, 0x00FF0000, 0xFF000000));
 	    	poppler.page_render(imgdata.data);
 	    	Image img = new Image(display, imgdata);
             offx = 0;
             offy = pdfPageHeight*i;
 	    	g.drawImage(img, offx, offy);
 	    	img.dispose();
 	    	poppler.document_release_page();
     	}
     	if(page1==page2) {
 	    	PageSelectCombo.getInstance().select(page1);    		
     	} else {
     		int page1size = (page1+1)*pdfPageHeight - vBar.getSelection();
     		int page2size = vBar.getSelection() + vBar.getThumb() - (page1+1)*pdfPageHeight;
     		if(page2size > pdfPageHeight) {
     			page2size = pdfPageHeight;
     		}
     		if(page2size>page1size) {
     			PageSelectCombo.getInstance().select(page2);
     		} else {
     			PageSelectCombo.getInstance().select(page1);
     		}
     	}
 
         if(page1>page2) {
             g.setForeground(getBackground());
             g.fillRectangle(0, 0, sz.x, sz.y);
             g.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
             g.drawString("No images to display", 20, 20);
             g.setForeground(display.getSystemColor(SWT.COLOR_RED));
             g.drawLine(0, 0, sz.x, sz.y);
             g.drawLine(0, sz.y, sz.x, 0);
         }
     	
     	// Draw highlight
     	if(highlight != null)
     	{
         	g.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
         	g.setXORMode(true);
     		for(Rectangle rect : highlight)
     		{
     			g.fillRectangle(rect);
     		}
     	}
         
      	//if (highlightLinks) 
     	if (false) 
     	{
     		//derek List<PDFAnnotation> anno = currentPageNum.getAnnots(PDFAnnotation.LINK_ANNOTATION);
     		List<LinkAnnotation> anno = null;
     		g.setForeground(display.getSystemColor(SWT.COLOR_RED));
     		for (LinkAnnotation a : anno) {
     			Rectangle r = getRectangle(convertPDF2ImageCoord(a.getRect()));
     			g.drawRectangle(r);
     		}
     	}
     }
 
     @Override
     public void preferenceChange(PreferenceChangeEvent event) {
     	if (ToggleLinkHighlightHandler.PREF_LINKHIGHTLIGHT_ID.equals(event.getKey())) {
     		highlightLinks = Boolean.parseBoolean((String)(event.getNewValue()));
     		redraw();
     	}
     }
 
     @Override
     public void dispose() {
     	super.dispose();
     	
     	//IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(de.vonloesch.pdf4eclipse.Activator.PLUGIN_ID);
     	IEclipsePreferences prefs = (new InstanceScope()).getNode(edu.pdx.svl.coDoc.poppler.Activator.PLUGIN_ID);
     	prefs.removePreferenceChangeListener(this);
     }
 
 	public void fitHorizontal() {
 		int w = sc.getClientArea().width;
     	Dimension size = poppler.page_get_size();
 		float pw = size.width;
 		setZoomFactor((1.0f*w)/pw);
 	}
 
 	public void fit() {
 		float w = 1.f * sc.getClientArea().width;
 		float h = 1.f * sc.getClientArea().height;
 		Dimension size = poppler.page_get_size();
 		float pw = size.width;
 		float ph = size.height;
 		if (w/pw < h/ph) setZoomFactor(w/pw);
 		else setZoomFactor(h/ph);
 	}
 
 	public Point getOrigin() {
 		if (!sc.isDisposed()) return sc.getOrigin();
 		else return null;
 	}
 
 	public void setOrigin(int x, int y) {
 		sc.setRedraw(false);
 		sc.setOrigin(x, y);
 		sc.setRedraw(true);
 	}
 
 	public interface IPDFEditor {
 		public void showPage(int pageNr);
 		public void showPreviousPage();
 		public void showNextPage();
 		public void gotoAction(PDFDestination dest);
 		public void writeStatusLineError(String text);
 	}
 }
