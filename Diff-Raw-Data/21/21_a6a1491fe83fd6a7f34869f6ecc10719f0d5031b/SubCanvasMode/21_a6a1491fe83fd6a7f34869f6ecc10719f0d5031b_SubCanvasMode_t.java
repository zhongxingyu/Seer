 package net.bodz.swt.draw.app;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyEvent;
 
 import net.bodz.swt.c.canvas.Canvas;
 import net.bodz.swt.c.canvas.DecoratedCanvasMode;
 import net.bodz.swt.c.canvas.ICanvasMode;
 
 public class SubCanvasMode
         extends DecoratedCanvasMode {
 
     private static final long serialVersionUID = 1L;
 
     private Canvas canvas;
    GraphDesignerContext context;
 
     public SubCanvasMode(ICanvasMode previous) {
         super(previous);
     }
 
     public SubCanvasMode(Canvas canvas, ICanvasMode parent) {
         super(parent);
         this.canvas = canvas;
     }
 
     public Canvas getCanvas() {
         return canvas;
     }
 
     @Override
     public void keyUp(KeyEvent e) {
         if (e.keyCode == SWT.ESC)
             canvas.setMode(getWrapped());
     }
 
    public GraphDesignerContext getContext() {
        return context;
    }

 }
