 package org.dawb.common.ui.image;
 
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.PointList;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.PaletteData;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Display;
 
 public class IconUtils {
 	
 
 	public static ImageDescriptor createIconDescriptor(String iconText) {
 
		final ImageData data  = new ImageData(16, 16, 16, new PaletteData(0xFF, 0xFF00, 0xFF0000));
         final Image     image = new Image(Display.getCurrent(), data);
         
         final GC gc = new GC(image);
        
         gc.setForeground(ColorConstants.white);
         gc.fillRectangle(new Rectangle(0,0,16,16));
         gc.setForeground(ColorConstants.darkGray);
         gc.setFont(new Font(Display.getCurrent(), new FontData("Dialog", 6, SWT.BOLD)));
         gc.drawText(iconText, 4, 2);
         
         gc.dispose();
 		
 		return new ImageDescriptor() {			
 			@Override
 			public ImageData getImageData() {
 				return image.getImageData();
 			}
 		};
 	}
 
 
 	public static ImageDescriptor createPenDescriptor(int penSize) {
 
		final ImageData data  = new ImageData(16, 16, 16, new PaletteData(0xFF, 0xFF00, 0xFF0000));
         final Image     image = new Image(Display.getCurrent(), data);
         
         final GC gc = new GC(image);
        
         gc.setBackground(Display.getDefault().getActiveShell().getBackground());
         gc.fillRectangle(new Rectangle(0,0,16,16));
         gc.setBackground(ColorConstants.gray);
         gc.fillRectangle(2, 2, penSize, penSize);
         
         gc.dispose();
 		
 		return new ImageDescriptor() {			
 			@Override
 			public ImageData getImageData() {
 				return image.getImageData();
 			}
 		};
 	}
 	
 
 	/**
 	 * Icon for the cursor 
 	 * @param pensize
 	 * @param square
 	 * @return
 	 */
 	public static Cursor getPenCursor(int pensize, ShapeType shape) {
 		
 		if (shape==ShapeType.NONE) return null;
 
 		final Image image  = new Image(Display.getCurrent(), new Rectangle(0, 0, pensize+4, pensize+4));
 		final GC    gc     = new GC(image, SWT.NONE);
 

 		gc.setBackground(Display.getDefault().getActiveShell().getBackground());
 		gc.setAlpha(0);
 		gc.fillRectangle(new Rectangle(0,0,pensize+4,pensize+4));
 
 		gc.setAlpha(255);
 		
 		// Draw a cross for the center.
 		final PointList x = new PointList(pensize+4);
 		final PointList y = new PointList(pensize+4);
 		for (int i = 0; i < pensize+4; i++) {
 			x.addPoint(new Point(i, (int)((pensize+4)/2)));
 			y.addPoint(new Point((int)((pensize+4)/2), i));
 		}
 		drawPointList(x, gc, false);
 		drawPointList(y, gc, true);
 		
         // Draw the shape.
 		gc.setForeground(ColorConstants.black);
 		switch (shape) {
 		case SQUARE:
 			gc.drawRectangle(2,2,pensize,pensize);
 			break;
 		case TRIANGLE:
 			final PointList pl = new PointList();
 			pl.addPoint(2+(pensize/2), 2);
 			pl.addPoint(2+pensize, 2+pensize);
 			pl.addPoint(2, 2+pensize);
 			gc.drawPolygon(pl.toIntArray());
 			break;
 		case CIRCLE:
 			gc.drawOval(2,2,pensize,pensize);
 			break;
 		default:
 			break;
 		}
 		
 		gc.dispose();
 		
 		return new Cursor(Display.getDefault(), image.getImageData(), (pensize+4)/2, (pensize+4)/2);
 
 	}
 
 
 	private static void drawPointList(PointList pl, GC gc, boolean isY) {
 		int xOff = isY?0:1;
 		int yOff = isY?1:0;
 		for (int i = 0; i < pl.size(); i++) {
 			gc.setForeground((i%2==0) ? ColorConstants.black : ColorConstants.white); 
 			gc.drawLine(pl.getPoint(i).x, pl.getPoint(i).y, pl.getPoint(i).x+xOff, pl.getPoint(i).y+yOff);
 		}		
 	}
 
 
 	/**
 	 * Icon for the actions to choose brush type.
 	 * @param size
 	 * @param shape
 	 * @param maskRGB
 	 * @return
 	 */
 	public static ImageDescriptor getBrushIcon(int size, ShapeType shape, RGB maskRGB) {
 		
 		if (shape==ShapeType.NONE) return null;
 
		final ImageData data  = new ImageData(16, 16, 16, new PaletteData(0xFF, 0xFF00, 0xFF0000));
         final Image     image = new Image(Display.getCurrent(), data);
         
         final GC gc = new GC(image);
        
         gc.setBackground(Display.getDefault().getActiveShell().getBackground());
         gc.fillRectangle(new Rectangle(0,0,16,16));
         gc.setForeground(ColorConstants.gray);
         gc.setLineWidth(2);
 		if (maskRGB!=null) {
 			final Color     maskColour = new Color(null, maskRGB);
 	        gc.setBackground(maskColour);
 		}
         switch (shape) {
         case SQUARE:
             if (maskRGB!=null) gc.fillRectangle(2,2,size,size);
             gc.drawRectangle(2,2,size,size);
             break;
         case TRIANGLE:
         	final PointList pl = new PointList();
         	pl.addPoint(2+(size/2), 2);
            	pl.addPoint(2+size, 2+size);
            	pl.addPoint(2, 2+size);
            	if (maskRGB!=null) gc.fillPolygon(pl.toIntArray());
             gc.drawPolygon(pl.toIntArray());
             break;
         case CIRCLE:
         	if (maskRGB!=null) gc.fillOval(2,2,size,size);
             gc.drawOval(2,2,size,size);
             break;
 		default:
 			break;
         }
             
         
         gc.dispose();
 		
 		return new ImageDescriptor() {			
 			@Override
 			public ImageData getImageData() {
 				return image.getImageData();
 			}
 		};
 	}
 
 	
 }
