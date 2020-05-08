 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.model.drawing;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Graphics2D;
import java.awt.Paint;
 import java.awt.Stroke;
 
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.Zone;
 
 /**
  * Abstract drawing.  This class takes care of setting up the Pen since that
  * will be the same for all implementing classes.
  */
public abstract class AbstractDrawing implements Drawable {
 
 	/**
 	 * The unique identifier for this drawable. It is immutable.
 	 */
 	private GUID id = new GUID();
 
 	private String layer;
 	
     /* (non-Javadoc)
      * @see maptool.model.drawing.Drawable#draw(java.awt.Graphics2D, maptool.model.drawing.Pen)
      */
     public void draw(Graphics2D g, Pen pen) {
         if (pen == null) {
             pen = Pen.DEFAULT;
         } 
         
         Stroke oldStroke = g.getStroke(); 
         g.setStroke(new BasicStroke(pen.getThickness(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 
         Composite oldComposite = g.getComposite();
         if (pen.isEraser()) {
         	g.setComposite(AlphaComposite.Clear);
         } else if ( pen.getOpacity() != 1)  {
         	g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,pen.getOpacity()));
         }
 
         if (pen.getBackgroundMode() == Pen.MODE_SOLID) {
         	if (pen.getBackgroundPaint() != null) {
        		g.setPaint(pen.getBackgroundPaint().getPaint());
         	} else {
         		// **** Legacy support for 1.1
         		g.setColor(new Color(pen.getBackgroundColor()));
         	}
             
             
             drawBackground(g);
         }
         
         if (pen.getForegroundMode() == Pen.MODE_SOLID) {
         	if (pen.getPaint() != null) {
            	g.setPaint(pen.getPaint().getPaint());
         	} else {
         		// **** Legacy support for 1.1
         		g.setColor(new Color(pen.getColor()));
         	}
         	
             draw(g);
         }
 
         g.setComposite(oldComposite);
         g.setStroke(oldStroke);
     }
     
     protected abstract void draw(Graphics2D g);
     
     protected abstract void drawBackground(Graphics2D g);
 
     /**
      * Get the id for this AbstractDrawing.
      *
      * @return Returns the current value of id.
      */
     public GUID getId() {
       return id;
     }
 
 	public void setLayer(Zone.Layer layer) {
 		this.layer = layer != null ? layer.name() : null;
 	}
 	
 	public Zone.Layer getLayer() {
 		return layer != null ? Zone.Layer.valueOf(layer) : Zone.Layer.BACKGROUND;
 	}
 
 	/**
      * Use the id for equals.
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
       if (!(obj instanceof AbstractDrawing)) return false;
       return id.equals(obj);
     }
 
     /**
      * Use the id for hash code.
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
       return id.hashCode();
     }
 }
