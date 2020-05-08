 package ru.cut2drox.ui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Canvas;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Scrollable;
 import org.eclipse.swt.widgets.Shell;
 
 
 public class ResizableCanvas extends Canvas {
 
 	Display display;
 	int x;
 	int y;
 	int width;
 	int height;
 	Image image=null;
 	String s="";
	int textX=0;
	int textY=0;
 	int canvasShiftX=0;
 	int canvasShiftY=0;
 	Font font;
 	
 	public ResizableCanvas(Composite arg0, int arg1,Display disp) {
 		super(arg0, arg1);
 		this.display=disp;
 	}
 	
 	ScrollBar hBar = this.getHorizontalBar();
 	ScrollBar vBar = this.getVerticalBar();
 	
 	
 	final Point origin = new Point(0, 0);
 	
 	
 	
 	Listener hLis = new Listener()
 	{
 		public void handleEvent(Event e) {
 			int hSelection = hBar.getSelection();
 	        int destX = -hSelection - origin.x;
 	        Rectangle rect = image.getBounds();
 	       	scroll(destX, 0, 0, 0, rect.width, rect.height, false);
 	        origin.x = -hSelection;
 	        canvasShiftX=origin.x;
 		}
 
 	};
 	
     Listener vLis = new Listener() 
     {
       public void handleEvent(Event e) {
         int vSelection = vBar.getSelection();
         int destY = -vSelection - origin.y;
         Rectangle rect = image.getBounds();
         scroll(0, destY, 0, 0, rect.width, rect.height, false);
         origin.y = -vSelection;
         canvasShiftY=origin.y;
       }
 
     };
     Listener rLis = new Listener() {
       public void handleEvent(Event e) {
     	setBounds(x,y,width,height);
         Rectangle rect = image.getBounds();
         Rectangle client = getClientArea();
         hBar.setMaximum(rect.width);
         vBar.setMaximum(rect.height);
         hBar.setThumb(Math.min(rect.width, client.width));
         vBar.setThumb(Math.min(rect.height, client.height));
         int hPage = rect.width - client.width;
         int vPage = rect.height - client.height;
         int hSelection = hBar.getSelection();
         int vSelection = vBar.getSelection();
         if (hSelection >= hPage) {
           if (hPage <= 0) hSelection = 0;
           origin.x = -hSelection;
          canvasShiftX=origin.x;
         }
         if (vSelection >= vPage) {
           if (vPage <= 0) vSelection = 0;
           origin.y = -vSelection;
          canvasShiftY=origin.y;
         }
         redraw();
       }
     };
     Listener pLis = new Listener() {
       public void handleEvent(Event e) {
         GC gc = e.gc;
 		gc.drawImage(image, origin.x, origin.y);
         Rectangle rect = image.getBounds();
         Rectangle client = getClientArea();
         int marginWidth = client.width - rect.width;
         if (marginWidth > 0) {
           gc.fillRectangle(rect.width, 0, marginWidth, client.height);
         }
         int marginHeight = client.height - rect.height;
         if (marginHeight > 0) {
           gc.fillRectangle(0, rect.height, client.width, marginHeight);
         }
         gc.setFont(font); 
        gc.drawText(s, textX, textY,true);
       }
     };
 	
     public void addScrolling()
     {
     	hBar.addListener(SWT.Selection,hLis);
     	vBar.addListener(SWT.Selection, vLis);
     	this.addListener(SWT.Resize, rLis);
     	this.addListener(SWT.Paint, pLis);
     }
     
     public void setRBounds(int x,int y,int width,int height)
     {
     	this.x=x;
     	this.y=y;
     	this.width=width;
     	this.height=height;
     }
     
     public void setImage(Image image)
     {
     	this.image=image;
     }
     
     public void drawRText(String s,int i,int j, Font font) //заняться методом!!!
     {
     	this.s=s;
    	this.textX=i;
    	this.textY=j;
     	this.font=font;
     	redraw();
     }
     public int getCanvasShiftX()
     {
     	return -canvasShiftX;
     }
     
     public int getCanvasShiftY()
     {
     	return -canvasShiftY;
     }
     
 
 }
 
