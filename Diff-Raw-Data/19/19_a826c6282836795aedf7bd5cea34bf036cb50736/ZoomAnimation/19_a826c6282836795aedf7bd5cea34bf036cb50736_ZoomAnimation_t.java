 package ca.couchware.wezzle2d.animation;
 
 import ca.couchware.wezzle2d.util.WDimension;
 import ca.couchware.wezzle2d.util.WPosition;
 import ca.couchware.wezzle2d.util.Util;
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.graphics.IEntity;
 
 /**
  * An animation that can zoom an entity in, out, or in and out in a loop.
  * 
  * @author cdmckay
  */
 public class ZoomAnimation extends AbstractAnimation
 {  
     
     /**
      * The zoom types.
      */
     public static enum ZoomType
     {
         IN, OUT, LOOP_IN, LOOP_OUT
     }        
     
     /**
      * The entity being animated.
      */
     private IEntity entity;
     
     /**
      * The counter.
      */
     private long counter;
     
     /**
      * The speed of the zoom in.
      */
     private double vin;      
     
     /**
      * The speed of the zoom in.
      */
     private double vout;      
           
     /**
      * The minimum width the entity may become before switching 
      * states.
      */
     final private int minWidth;
     
     /**
      * The maximum width the entity may become before switching
      * states.
      */
     final private int maxWidth;
     
     /**
      * The current zoom type.
      */
     private ZoomType type;
     
     /**
      * The initial dimensions of the entity.
      */
     final private WDimension d;
     
     /**
      * The initial position of the entity.
      */
     final private WPosition p;
     
     /**
      * The amount of time, in ms, to wait before fading out.
      */
     private int wait;
     
     /**
      * The max time for the animation to run for, in ms.
      */
     private int duration;
     
     private ZoomAnimation(Builder builder)
     {                        
         // Save a reference to the entity.
         this.type = builder.type;
         this.entity = builder.entity;   
         this.vin = builder.vin;
         this.vout = builder.vout;
         this.wait = builder.wait;
         //this.duration = builder.duration;                
         
         // Round min width to nearest even number.
         minWidth = builder.minWidth;
                 
         // Round max width to nearest even number.
         maxWidth = builder.maxWidth;
                
         // Remember initial dimensions.
         d = new WDimension(entity.getWidth(), entity.getHeight());
         
         // Remember initial position.
         p = new WPosition(entity.getX(), entity.getY());      
         
         // Set the entity up based on what type of zoom we're doing.'
         int dw = 0;
         switch (type)
         {
             case IN:
             case LOOP_IN:
                 
                 entity.setWidth(maxWidth);
                 entity.setHeight(maxWidth);
                 dw = maxWidth - d.getWidth();
                 entity.setX(p.getX() - dw);
                 entity.setY(p.getY() - dw);
                 
                 break;
 
             case OUT:
             case LOOP_OUT:
                 
                 entity.setWidth(minWidth);
                 entity.setHeight(minWidth);
                 dw = d.getWidth() - minWidth;
                 entity.setX(p.getX() + dw);
                 entity.setY(p.getY() + dw);
                 
                 break;
 
             default:
                 throw new AssertionError();
         }
     }
     
     /**
      * Creates a new zoom animation with the given parameters.  
      * If the type is:
      * 
      *   IN: The entity will start at the max size and reduce itself to its
      *       min size.  At that point the animation will report as done.
      * 
      *   OUT: The entity will start at the min size and increase to its max
      *        size.  At that point it will report done.
      * 
      *   LOOP_IN: The entity will act that same as the case of IN, except will
      *            not end once min size has been reached.  Instead, it will
      *            switch its type to LOOP_OUT and grow to max size.  This will
      *            continue indefinitely until the animation is manually stopped
      *            or cleaned up.
      * 
      *   LOOP_OUT: Same as LOOP_IN, but starts in the min size and expands to
      *             the max size.
      */
     public static class Builder implements IBuilder<ZoomAnimation>
     {
         private final ZoomType type;
         private final IEntity entity;
         
         private int wait = 0;
         //private int duration = -1;
         private int minWidth = -1;
         private int maxWidth = -1;
         private double vin = 0.008;
         private double vout = 0.008;
         
         public Builder(ZoomType type, IEntity entity)
         {
             assert type != null;
             assert entity != null;
             
             this.type = type;
             this.entity = entity;
             this.minWidth = 0;
             this.maxWidth = entity.getWidth();
         }
         
         public Builder wait(int val) { wait = val; return this; }
         //public Builder duration(int val) { duration = val; return this; }        
         public Builder minWidth(int val) { minWidth = val; return this; }
         public Builder maxWidth(int val) { maxWidth = val; return this; }
         public Builder vin(double val) { vin = val; return this; }
         public Builder vout(double val) { vout = val; return this; }
         public Builder v(double val) { vin = val; vout = val; return this; }
 
         public ZoomAnimation end()
         {
             return new ZoomAnimation(this);
         }                
     }
 
     public void nextFrame(long delta)
     {               
         // Add to counter.
         counter += delta;
         
         // The time.
         double t = (double) counter;
         
         // See if the wait has expired.
         int dx = 0;
         int w = 0;
         //int newHeight = 0;
         if (counter > wait)
         {
             switch (type)
             {                
                 case IN:
                 case LOOP_IN:                                                           
                     
                     dx = (int) (t * vin);                         
                     w = maxWidth - (dx * 2);                    
                     
                     w = (w <= minWidth) ? minWidth : w;
                     entity.setWidth(w);
                     entity.setHeight(w);
                     entity.setX(p.getX() + dx);
                     entity.setY(p.getY() + dx);
                     
                     if (entity.getWidth() == minWidth)                    
                     {                                               
                         switch (type)
                         {
                             case IN:
                                 setDone(true);
                                 break;
                                 
                             case LOOP_IN:
                                 counter = 0;
                                 type = ZoomType.LOOP_OUT;
                                 break;
                                 
                             default: throw new AssertionError();
                         }                                                                                                        
                     } // end if
                                        
                     break;
                     
                 case OUT:
                 case LOOP_OUT:                                        
                     
                     dx = (int) (t * vout);                    
                     w = minWidth + (dx * 2);                    
                     
                     w = (w >= maxWidth) ? maxWidth : w;
                     entity.setWidth(w);
                     entity.setHeight(w);
                     entity.setX(p.getX() + (maxWidth - minWidth) / 2 - dx);
                     entity.setY(p.getY() + (maxWidth - minWidth) / 2 - dx);
                     
                     if (entity.getWidth() == maxWidth)                    
                    {         
                        // Make sure they're at the right spot.
                        entity.setX(p.getX());
                        entity.setY(p.getY());
                        
                         switch (type)
                         {
                             case OUT:
                                 setDone(true);
                                 break;
                                 
                             case LOOP_OUT:
                                 counter = 0;
                                 type = ZoomType.LOOP_IN;
                                 break;
                                 
                             default: throw new AssertionError();
                         }                                                                                                        
                     } // end if
                     
                     break;
                                                         
                 default:
                     throw new AssertionError();
             }                       
         } // end if            
     }
 
     @Override
     public void cleanUp()
     {
         switch (type)
         {
             case LOOP_IN:
             case LOOP_OUT:
                 
                 // Resize entity to original dimensions.
                 entity.setWidth(d.getWidth());
                 entity.setHeight(d.getHeight());
 
                 // Move back to original position.
                 entity.setX(p.getX());
                 entity.setY(p.getY());
 
                 // Mark the animation as done.
                 setDone(true);
                 
                 break;
                 
             case IN:
             case OUT:
                 
                 break;
                 
             default: throw new AssertionError();
         }
     }
 
     public void vin(double val)
     {
         vin = val;
     }   
     
     public double vin()
     {
         return vin;
     }
     
     public void vout(double val)
     {
         vout = val;
     }
     
     public double vout()
     {
         return vout;
     }
     
     public void v(double val)
     {
         vin(val);
         vout(val);
     }
 
 }
