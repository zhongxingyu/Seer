 package com.joshondesign.treegui.modes.aminolang;
 
 import com.joshondesign.treegui.docmodel.Resize;
 import com.joshondesign.treegui.model.Metadata;
 import com.joshondesign.treegui.model.Prop;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import com.joshondesign.treegui.modes.aminojs.ActionProp;
 import com.joshondesign.treegui.modes.aminojs.TriggerProp;
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
         @Prop(visible = true, exported = true) public String text = "push button";
         @Prop public boolean enabled = true;
         @Prop(visible = false, bindable = true) public ActionProp action;
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
     public static class ToggleButton extends VisualBase {
         @Prop public String text = "toggle button";
         @Prop public boolean enabled = true;
         @Prop public boolean selected = false;
     }
 
     public static final DynamicNode.DrawDelegate ToggleButtonDelegate = new DynamicNode.DrawDelegate() {
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
         @Prop public FlatColor fill = FlatColor.BLUE;
         @Prop public double opacity = 1.0;
     }
     public static final DynamicNode.DrawDelegate RectDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getWidth();
             double h = node.getHeight();
             FlatColor color = node.getProperty("fill").getColorValue();
             double opacity = node.getProperty("opacity").getDoubleValue();
             g.setPaint(color.deriveWithAlpha(opacity));
             g.fillRect(0, 0, w, h);
             g.setPaint(FlatColor.BLACK);
             g.drawRect(0, 0, w, h);
         }
     };
 
 
     @Metadata(visual = true)
     public static class Slider extends VisualBase {
         @Prop public Double minValue = 0.0;
         @Prop public double maxValue = 100;
         @Prop public double value = 50;
         @Prop public boolean enabled = true;
     }
 
     public static final DynamicNode.DrawDelegate SliderDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getWidth();
             double h = node.getHeight();
             g.setPaint(FlatColor.GRAY);
             g.fillRect(0, 0, w, h);
             g.setPaint(FlatColor.BLACK);
             g.fillRect(0, 0, h, h);
         }
     };
 
     @Metadata(name ="Label")
     public static class Label {
         @Prop(bindable = true)
         public CharSequence text = "a label";
         @Prop  public FlatColor color = FlatColor.BLACK;
         @Prop  public double fontSize = 12.5;
     }
 
     public static final DynamicNode.DrawDelegate LabelDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getProperty("width").getDoubleValue();
             double h = node.getProperty("height").getDoubleValue();
             String t = node.getProperty("text").getStringValue();
             FlatColor color = node.getProperty("color").getColorValue();
             double size = node.getProperty("fontSize").getDoubleValue();
             g.setPaint(color);
             Font font = Font.name(Font.DEFAULT.getName()).size((float) size).resolve();
             double y = font.getAscender() + font.getDescender();
             g.drawText(t,font,5,y);
         }
     };
 
 
     @Metadata(name = "ListView")
     public static class ListView {
         @Prop public double rowheight = 20;
         @Prop public double columnWidth = 100;
         //@Prop(visible = false) public ListView.ItemRenderer renderer = null;
         //@Prop(bindable = true, exported = false) public ListModel model = null;
         //@Prop  public ListView.Orientation orientation = ListView.Orientation.Vertical;
         @Prop(bindable = true) public int selectedIndex = 0;
         @Prop(bindable = true, compound = true, exported = false, master = "model", visible = false)
         public Object selectedObject = null;
     }
 
     public static final DynamicNode.DrawDelegate ListViewDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getProperty("width").getDoubleValue();
             double h = node.getProperty("height").getDoubleValue();
             g.setPaint(FlatColor.GRAY);
             g.fillRect(0, 0, w, h);
             g.setPaint(FlatColor.BLACK);
             g.drawRect(0, 0, w, h);
             //lines
             for (int i = 0; i < 5; i++) {
                 g.drawRect(0, i * 15, w, 15);
             }
         }
     };
 
     @Metadata(name = "AnchorPanel", container = true, resize = Resize.Any)
     public static class AnchorPanel {
         @Prop(bindable = true, visible = false) public Object self = this;
         @Prop public FlatColor fill = FlatColor.GRAY;
     }
 
     public static final DynamicNode.DrawDelegate AnchorPanelDelegate = new DynamicNode.DrawDelegate() {
         public void draw(GFX g, DynamicNode node) {
             double w = node.getProperty("width").getDoubleValue();
             double h = node.getProperty("height").getDoubleValue();
             FlatColor color = node.getProperty("fill").getColorValue();
             g.setPaint(color);
             g.fillRect(0, 0, w, h);
             g.setPaint(FlatColor.BLACK);
             g.drawRect(0, 0, w, h);
         }
     };
 
 
 
     @Metadata(name = "Transition", visual = false)
     public static class Transition {
 
         @Prop(bindable = true, visible = false)
         public TriggerProp pushTrigger;
 
         @Prop(bindable = true, visible = false)
         public Object pushTarget;
 
         //@Prop(visible = false, bindable = true) public ActionProp action;
     }
 
     public static final DynamicNode.DrawDelegate ServiceBaseDelegate = new DynamicNode.DrawDelegate() {
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
 }
