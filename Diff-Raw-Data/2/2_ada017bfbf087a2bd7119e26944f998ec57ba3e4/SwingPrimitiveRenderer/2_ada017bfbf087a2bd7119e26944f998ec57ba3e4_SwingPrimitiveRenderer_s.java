 /*
  * Copyright (c) 2010, Soar Technology, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * * Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * 
  * * Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the
  *   documentation and/or other materials provided with the distribution.
  * 
  * * Neither the name of Soar Technology, Inc. nor the names of its contributors
  *   may be used to endorse or promote products derived from this software
  *   without the specific prior written permission of Soar Technology, Inc.
  * 
  * THIS SOFTWARE IS PROVIDED BY SOAR TECHNOLOGY, INC. AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL SOAR TECHNOLOGY, INC. OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
  * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * Created on May 21, 2007
  */
 package com.soartech.shapesystem.swing;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.util.List;
 
 import com.soartech.shapesystem.CapStyle;
 import com.soartech.shapesystem.FillStyle;
 import com.soartech.shapesystem.JoinStyle;
 import com.soartech.shapesystem.LineStyle;
 import com.soartech.shapesystem.PrimitiveRenderer;
 import com.soartech.shapesystem.ShapeStyle;
 import com.soartech.shapesystem.SimplePosition;
 import com.soartech.shapesystem.SimpleRotation;
 import com.soartech.shapesystem.TextStyle;
 
 /**
  * @author ray
  */
 public class SwingPrimitiveRenderer implements PrimitiveRenderer
 {
     private static final float[] DASHES = { 10.0f, 10.0f };
     
     private SwingPrimitiveRendererFactory factory;
     private ShapeStyle style;
     private boolean filled;
     
     public SwingPrimitiveRenderer(SwingPrimitiveRendererFactory factory, ShapeStyle style)
     {
         super();
         this.factory = factory;
         this.style = style;
         
         initGraphics();
     }
     
     private void initGraphics()
     {
         Graphics2D g = factory.getGraphics2D();
         
         switchToFillColor();
 
         filled = style.getFillStyle() == FillStyle.FILLED;
         
         float lineThickness = (float) factory.getTransformer().scalarToPixels(style.getLineThickness());
         Stroke stroke = null;
         int capStyle = getSwingCapStyle(style.getCapStyle());
         int joinStyle = getSwingJoinStyle(style.getJoinStyle());
         
         if(style.getLineStyle() == LineStyle.SOLID)
         {
             stroke = new BasicStroke(lineThickness, capStyle, joinStyle);
         }
         else
         {
             stroke = new BasicStroke(lineThickness, capStyle, joinStyle, 10.0f, DASHES, 0.0f);
         }
         
         g.setStroke(stroke);
         
         if(style.getOpacity() != 1.0)
         {
             AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, style.getOpacity());
             g.setComposite(ac);
         }
         else 
         {
             g.setComposite(factory.getOriginalComposite());
         }
     }
     
     private int getSwingCapStyle(CapStyle capStyle)
     {
         switch (capStyle) {
             case BUTT:
                 return BasicStroke.CAP_BUTT;
             case ROUND:
                 return BasicStroke.CAP_ROUND;
             case SQUARE:
                 return BasicStroke.CAP_SQUARE;
             default:
                 return BasicStroke.CAP_BUTT;
         }
     }
     
     private int getSwingJoinStyle(JoinStyle joinStyle)
     {
         switch ( joinStyle ) {
             case BEVEL:
                 return BasicStroke.JOIN_BEVEL;
             case MITER:
                 return BasicStroke.JOIN_MITER;
             case ROUND:
                 return BasicStroke.JOIN_ROUND;
             default:
                 return BasicStroke.JOIN_ROUND;
         }
     }
     
     private void switchToFillColor()
     {
         factory.getGraphics2D().setColor(style.getFillColor());
     }
     
     private void switchToLineColor()
     {
         factory.getGraphics2D().setColor(style.getLineColor());
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#drawArc(com.soartech.shapesystem.SimplePosition, double, com.soartech.shapesystem.SimpleRotation, com.soartech.shapesystem.SimpleRotation)
      */
     public void drawArc(SimplePosition center, double radius,
             SimpleRotation from, SimpleRotation to)
     {
         Graphics2D g = factory.getGraphics2D();
         
         int x = (int)(center.x - radius);
         int y = (int)(center.y - radius);
         int w = (int)(radius * 2.0);
         int h = (int)(radius * 2.0);
         
         int a1 = (int)(  from.inDegrees()              /* * 64.0*/);
         int a2 = (int)( (to.inDegrees() - from.inDegrees())/* * 64.0*/);
         
         if(filled)
         {
             switchToFillColor();
             g.fillArc(x, y, w, h, a1, a2);
         }
         
         if(!filled || style.getLineColor() != style.getFillColor())
         {
             switchToLineColor();
             g.drawArc(x, y, w, h, a1, a2);
         }
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#drawImage(com.soartech.shapesystem.SimplePosition, java.lang.String, double, double)
      */
     public void drawImage(SimplePosition center, SimpleRotation rotation, String imageId, double width,
             double height)
     {
         Graphics2D g = factory.getGraphics2D();
 
         Image image = factory.getImage(imageId);
         if(image == null)
         {
             return;
         }
 
         int w = (int) width;
         int h = (int) height;
         if(w <= 0 && h <= 0)
         {
             return;
         }
         
         int topLeftX = (int) center.x - w / 2;
         int topLeftY = (int) center.y - h / 2;
         
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                            RenderingHints.VALUE_INTERPOLATION_BICUBIC);
         AffineTransform transform = new AffineTransform();
         transform.rotate(-rotation.inRadians(), center.x, center.y);
         transform.translate(topLeftX, topLeftY);
         g.drawImage(image, transform, null);
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#drawImage(com.soartech.shapesystem.SimplePosition, java.lang.String)
      */
     public void drawImage(SimplePosition center, SimpleRotation rotation, String imageId)
     {
         Graphics2D g = factory.getGraphics2D();
 
         Image image = factory.getImage(imageId);
         if(image == null)
         {
             return;
         }
         
         int topLeftX = (int) center.x - image.getWidth(null) / 2;
         int topLeftY = (int) center.y - image.getHeight(null) / 2;
         
         g.drawImage(image, topLeftX, topLeftY, null);
         
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#drawLine(com.soartech.shapesystem.SimplePosition, com.soartech.shapesystem.SimplePosition)
      */
     public void drawLine(SimplePosition start, SimplePosition end)
     {
         Graphics2D g = factory.getGraphics2D();
 
         switchToLineColor();
         g.drawLine((int) start.x, (int) start.y, (int) end.x, (int) end.y);
     }
     
     /* (non-Javadoc)
      * 
      * @see com.soartech.shapesystem.PrimitiveRenderer#drawPolygon(java.util.List)
      */
     public void drawPolygon(List<SimplePosition> bounds)
     {
         Graphics2D g = factory.getGraphics2D();
 
         int[] xPoints = new int[bounds.size()];
         int[] yPoints = new int[bounds.size()];
         
         int n = 0;
         for(SimplePosition p  : bounds)
         {
             xPoints[n] = (int) p.x;
             yPoints[n] = (int) p.y;
             ++n;
         }
         
         if(filled)
         {
             g.fillPolygon(xPoints, yPoints, n);
         }
         if(!filled || style.getLineColor() != style.getFillColor())
         {
             switchToLineColor();
             g.drawPolygon(xPoints, yPoints, n);
         }
     }
     
     public void drawPolyline(List<SimplePosition> polyLinePoints)
     {
         Graphics2D g = factory.getGraphics2D();
 
         int[] xPoints = new int[polyLinePoints.size()];
         int[] yPoints = new int[polyLinePoints.size()];
         
         int n = 0;
         for(SimplePosition p  : polyLinePoints)
         {
             xPoints[n] = (int) p.x;
             yPoints[n] = (int) p.y;
             ++n;
         }
         
         switchToLineColor();
        g.drawPolygon(xPoints, yPoints, n);
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#drawText(com.soartech.shapesystem.SimplePosition, java.lang.String)
      */
     public void drawText(SimplePosition p, String string)
     {
         final Graphics2D g = factory.getGraphics2D();
         /*
         FontRenderContext frc = g.getFontRenderContext();
         AttributedCharacterIterator styledText = new AttributedString(string).getIterator();
         LineBreakMeasurer measurer = new LineBreakMeasurer(styledText, frc);
         double wrappingWidth = getViewportWidth() - p.x ;
         if(wrappingWidth < 0)
         {
             wrappingWidth = 100;
         }
         double x = p.x;
         double y = p.y;
         while (measurer.getPosition() < styledText.getEndIndex()) {
 
             TextLayout layout = measurer.nextLayout((float) wrappingWidth);
 
             y += (layout.getAscent());
             double dx = layout.isLeftToRight() ?
                 0 : (wrappingWidth - layout.getAdvance());
 
             layout.draw(g, (float)(x + dx), (float) y);
             y += layout.getDescent() + layout.getLeading();
         }
         */
        
         final Font oldFont = g.getFont();
         if(style instanceof TextStyle)
         {
             TextStyle ts = (TextStyle) style;
             if(ts.getFont() != null)
             {
                 g.setFont(ts.getFont());
             }
         }
         if(filled)
         {
             switchToFillColor();
             FontMetrics fontMetrics = g.getFontMetrics();
             Rectangle2D rect = fontMetrics.getStringBounds(string, g);
             int pad = 3;
             g.fillRoundRect((int) p.x - pad, (int) p.y - fontMetrics.getAscent() - pad, 
                             (int) rect.getWidth() + 2 * pad, (int) rect.getHeight() + 2 * pad, 3 * pad, 3 * pad);
         }
         switchToLineColor();
         g.drawString(string, (int) p.x, (int) p.y);
         g.setFont(oldFont);
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#getImageHeight(java.lang.String)
      */
     public double getImageHeight(String imageId)
     {
         Image image = factory.getImage(imageId); 
         return image != null ? image.getHeight(null) : 0.0;
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#getImageWidth(java.lang.String)
      */
     public double getImageWidth(String imageId)
     {
         Image image = factory.getImage(imageId); 
         return image != null ? image.getWidth(null) : 0.0;
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#getViewportHeight()
      */
     public double getViewportHeight()
     {
         return factory.getViewportHeight();
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#getViewportWidth()
      */
     public double getViewportWidth()
     {
         return factory.getViewportWidth();
     }
 
     /* (non-Javadoc)
      * @see com.soartech.shapesystem.PrimitiveRenderer#isPointInViewport(com.soartech.shapesystem.SimplePosition)
      */
     public boolean isPointInViewport(SimplePosition p)
     {
         return p.x >= 0 && p.x < factory.getViewportWidth() &&
                p.y >= 0 && p.y < factory.getViewportHeight();
     }
 
 }
