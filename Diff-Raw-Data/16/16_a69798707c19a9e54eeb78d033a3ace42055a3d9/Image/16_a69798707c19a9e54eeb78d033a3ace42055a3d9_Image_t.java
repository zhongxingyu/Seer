 package com.joshondesign.treegui.modes.amino;
 
 import com.joshondesign.treegui.docmodel.ResizableRectNode;
 import com.joshondesign.treegui.docmodel.SketchNode;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.GFX;
 
 /**
  * Created with IntelliJ IDEA.
  * User: josh
  * Date: 1/8/13
  * Time: 10:18 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Image extends ResizableRectNode {
    private String src;

     public Image() {
         setWidth(60);
         setHeight(60);
        setSrc("http://projects.joshy.org/demos/AnimatedStartup/earth.gif");
     }
 
     @Override
     public void draw(GFX g) {
         g.setPaint(FlatColor.GRAY);
         g.fillRect(0,0,getWidth(),getHeight());
         g.setPaint(FlatColor.BLACK);
         g.drawRect(0+10,0+10,getWidth()-10*2,getHeight()-10*2);
     }
 
     @Override
     public SketchNode duplicate(SketchNode node) {
         if(node == null) {
             node = new Image();
            ((Image)node).setSrc(getSrc());
         }
         return super.duplicate(node);
     }

    public Image setSrc(String src) {
        this.src = src;
        return this;
    }

    public String getSrc() {
        return src;
    }
 }
