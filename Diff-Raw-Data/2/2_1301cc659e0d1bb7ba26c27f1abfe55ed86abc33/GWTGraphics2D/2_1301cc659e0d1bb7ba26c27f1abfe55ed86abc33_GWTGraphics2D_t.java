 package org.openstreetmap.josm.data.osm.visitor.paint;
 
 // import org.openstreetmap.josm.tools.Color;
 // //import java.awt.Composite;
 // //import java.awt.Font;
 // //import java.awt.FontMetrics;
 // //import java.awt.Graphics;
 // //import java.awt.Graphics2D;
 // //import java.awt.GraphicsConfiguration;
 // //import java.awt.Image;
 // //import java.awt.Paint;
 // //import java.awt.Polygon;
 // ////import java.awt.Rectangle;
 // //import java.awt.RenderingHints;
 // //import java.awt.Shape;
 // //import java.awt.Stroke;
 // //import java.awt.RenderingHints.Key;
 // //import java.awt.font.FontRenderContext;
 // //import java.awt.font.GlyphVector;
 // //import java.awt.geom.AffineTransform;
 // //import java.awt.image.BufferedImage;
 // //import java.awt.image.BufferedImageOp;
 // //import java.awt.image.ImageObserver;
 // //import java.awt.image.RenderedImage;
 // //import java.awt.image.renderable.RenderableImage;
 ////import java.awt.Graphics2D;
 ////import java.awt.RenderingHints.Key;
 ////import java.awt.geom.GeneralPath;
 ////import java.awt.Graphics;
 ////import java.awt.Polygon;
 ////import java.awt.RenderingHints.Key;
 ////import java.awt.geom.GeneralPath;
 //import java.awt.Graphics;
 //import java.text.AttributedCharacterIterator;
 import java.util.Map;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.VerticalPanel;
 import com.google.gwt.user.client.ui.Widget;
 
//import org.openstreetmap.gwt.client.GWTGraphics2DTest;
 import org.openstreetmap.josm.data.osm.Color;
 //import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 import org.vaadin.gwtgraphics.client.DrawingArea;
 import org.vaadin.gwtgraphics.client.Image;
 import org.vaadin.gwtgraphics.client.Shape;
 import org.vaadin.gwtgraphics.client.VectorObject;
 import org.vaadin.gwtgraphics.client.animation.Animate;
 import org.vaadin.gwtgraphics.client.shape.Circle;
 import org.vaadin.gwtgraphics.client.shape.Path;
 import org.vaadin.gwtgraphics.client.shape.Text;
 //import org.vaadin.gwtgraphics.client.shape.Rectangle;
 import org.vaadin.gwtgraphics.client.shape.path.LineTo;
 
 public class GWTGraphics2D //extends Graphics2D
  implements IGwtGraphics2DSimple
 {
 
     private DrawingArea canvas;
     
     public GWTGraphics2D() {
 	canvas = new DrawingArea(400,400);
 	// 
     }
 
     public void clear()
     {
     	canvas.clear();
     	
     }
 
 	public void drawString(String t, int x, int y) {
 	    Text o = new Text( x, y,t);    
 	    GWT.log("drawRect2" +o .toString());
 	    canvas.add(o);
 	}
 
 	
 	public void drawString(String t, float x, float y) {
 		// TODO Auto-generated method stub
 	    Text o = new Text((int)x, (int)y,t); 
 	    GWT.log("drawString" +o .toString());
 	    canvas.add(o);
 		
 	}
 
 	
 	public void fill(Shape arg0) {
 		// TODO Auto-generated method stub
 		canvas.add(arg0);
 	}
 
 	
 	/* (non-Javadoc)
 	 * @see org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D#draw(org.vaadin.gwtgraphics.client.Shape)
 	 */
 	public void draw(Shape createStrokedShape) {
 		// TODO Auto-generated method stub
 		canvas.add(createStrokedShape);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D#setFont(org.openstreetmap.josm.data.osm.visitor.paint.Font)
 	 */
 	public void setFont(Font orderFont) {
 		// TODO Auto-generated method stub
 		 GWT.log( "TODO: set font " + orderFont .toString());
 	}
 
 	/* (non-Javadoc)
 	 * @see org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D#drawRect(long, long, int, int)
 	 */
 	public void drawRect(long l, long m, int k, int l2) {
 		// TODO Auto-generated method stub
 		
 	    Rectangle o = new Rectangle( l,m,k,l2);    
 	   // GWT.log("drawRect" + o .toString()); // called many times
 	    canvas.add(o);
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D#setStroke(org.openstreetmap.josm.data.osm.visitor.paint.BasicStroke)
 	 */
 	public void setStroke(BasicStroke basicStroke) {
 		// TODO Auto-generated method stub
 		// GWT.log( "TODO: set stroke " + basicStroke .toString());
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D#draw(org.openstreetmap.josm.data.osm.visitor.paint.GeneralPath)
 	 */
 	public void draw(
 			org.openstreetmap.josm.data.osm.visitor.paint.GeneralPath path) {
 	
 		canvas.add(path);
 	};
 
 
 	/* (non-Javadoc)
 	 * @see org.openstreetmap.josm.data.osm.visitor.paint.IGwtGraphics2D#fillRect(long, long, int, int)
 	 */
 	public void fillRect(long l, long m, int size, int size2) {
 		Rectangle o = new Rectangle( l,m,size,size2);
 		GWT.log("fillRect" + o .toString());
 	    canvas.add(o);
 		
 	}
 
 
 
 	@Override
 	public void drawString(String t, long x, long y) {
 		GWT.log( "draw string" + t+ x + ":"+ y );
 		Text o = new Text((int)x, (int)y,t); 
 		GWT.log("draw String:" +o .toString());
 		canvas.add(o);
 	}
 
 	@Override
 	public void fillPolygon(Polygon polygon) {
 			canvas.add(polygon);
 	}
 
 
 
 	@Override
 	public IGwtGraphics2DSimple g() {
 		// TODO Auto-generated method stub
 		GWT.log( "get g" );
 		return null;
 	}
 
 
 
 	@Override
 	public Rectangle getClipBounds() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 	@Override
 	public Color getColor() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 	@Override
 	public Font getFont() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 	@Override
 	public FontMetrics getFontMetrics(Font orderFont) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 
 	@Override
 	public void setColor(Color color) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 
 	@Override
 	public Widget getDrawingArea() {
 		// TODO Auto-generated method stub
 		return canvas;
 	}
 
 
 }
