 package com.joshondesign.treegui.modes.aminolang;
 
 import com.joshondesign.treegui.model.Metadata;
 import com.joshondesign.treegui.model.Prop;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import org.joshy.gfx.draw.FlatColor;
 import org.joshy.gfx.draw.Font;
 import org.joshy.gfx.draw.GFX;
 
 public abstract class Defs {
 
    @Metadata(visual = true)
     public static class VisualBase {
         @Prop public double translateX = 0;
         @Prop public double translateY = 0;
         @Prop public double width = 100;
         @Prop public double height = 50;
     }
 
     public static final  DynamicNode.DrawDelegate VisualBaseDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getWidth();
             double h = node.getHeight();
             g.setPaint(FlatColor.YELLOW);
             g.fillRoundRect(0, 0, w, h, 10, 10);
             g.setPaint(FlatColor.BLACK);
             g.drawRoundRect(0, 0, w, h, 10, 10);
             g.drawText(node.getName(), Font.DEFAULT, 5, 15);
         }
     };
 
    @Metadata(visual = true)
     public static class PushButton extends VisualBase {
         @Prop public String text = "pushbutton";
         @Prop public boolean enabled = true;
     }
 
     public static final DynamicNode.DrawDelegate PushButtonDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getWidth();
             double h = node.getHeight();
             String t = node.getProperty("text").getStringValue();
             g.setPaint(FlatColor.GRAY);
             g.fillRect(0, 0, w, h);
             g.setPaint(FlatColor.BLACK);
             g.drawText(t, Font.DEFAULT, 5, 15);
             g.drawRect(0, 0, w, h);
         }
     };
 
    @Metadata(visual = true)
     public static class Rect extends VisualBase {
 
     }
     public static final DynamicNode.DrawDelegate RectDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getWidth();
             double h = node.getHeight();
             g.setPaint(FlatColor.GRAY);
             g.fillRect(0, 0, w, h);
             g.setPaint(FlatColor.BLACK);
             g.drawRect(0, 0, w, h);
         }
     };
 
 }
