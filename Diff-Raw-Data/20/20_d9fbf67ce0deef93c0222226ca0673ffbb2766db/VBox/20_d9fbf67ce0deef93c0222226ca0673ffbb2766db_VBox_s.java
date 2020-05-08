 package eu.xibit.jwui.components;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class VBox extends AComponent {
 
     private final List<AComponent> childs = new ArrayList<AComponent>();
     private final List<Integer> cellWidths = new ArrayList<Integer>();
     
     private int spacing = 0;
     private int padding = 0;
 
 
 
 
     public VBox () {
         super();
     }
 
 
 
 
     public void addComponent(AComponent component, int cellHeight) {
         this.childs.add(component);
         this.cellWidths.add(new Integer(cellHeight));
     }
     
     public AComponent getComponent(int index) {
         return this.childs.get(index);
     }
 
     public int getCellHeight(int index) {
         return this.cellWidths.get(index);
     }
 
     public boolean hasComponent(AComponent component) {
         if (component == null) {
             return false;
         }
         return this.childs.contains(component);
     }
 
     public int getComponentsSize() {
         return this.childs.size();
     }
 
     public void removeComponent(AComponent component) {
         if (component == null) {
             return;
         }
         int componentIndex = this.childs.indexOf(component);
         if (componentIndex < 0) {
             return;
         }
         this.childs.remove(componentIndex);
         this.cellWidths.remove(componentIndex);
     }
 
     public void removeComponentAt(int index) {
         this.childs.remove(index);
         this.cellWidths.remove(index);
     }
 
     public void insertComponent(int index, AComponent component, int cellHeight) {
         this.childs.add(index, component);
         this.cellWidths.add(index, new Integer(cellHeight));
     }
 
 
     public int getSpacing() {
         return this.spacing;
     }
 
     public void setSpacing(int spacing) {
         this.spacing = spacing;
     }
 
 
     public int getPadding() {
         return this.padding;
     }
 
     public void setPadding(int padding) {
         this.padding = padding;
     }
 
     @Override
     public double getWidth() {
         return super.getWidth();
     }
 
     @Override
     public void setWidth(double width) {
         super.setWidth(width);
     }
 
     @Override
     public double getHeight() {
         return super.getHeight();
     }
 
     @Override
     public void setHeight(double width) {
         super.setHeight(width);
     }
 
 
 
     @Override
     public String render() {
         String style = this.renderSizeStyle();
 
         String res = "<table style=\"" + style + "\" cellspacing=\"" + Integer.toString(this.spacing) + " cellpadding=\"" + Integer.toString(this.spacing) + "\""; 
         if (this.getWidth() >= 0.0) {
             res += " width=\"";
             if (this.getWidth() == 0.0) {
                 res += "0";
             } else if (this.getWidth() >= 1.0) {
                 res += Integer.toString((int)this.getWidth());
             } else {
                 res += Double.toString((this.getWidth() * 100.0)) + "%;";
             }
             res += "\"";
         }
 
         res += "\">\n";
         boolean first = true;
         int cellWidth = -1;
         AComponent item = null;
         for (int i = 0; i < this.childs.size(); i++) {
             item = this.childs.get(i);
             //if (this.childs.get(i) != null) {
                 //if (this.childs.get(i){AComponent}.isVisible()) {
                     cellWidth = this.cellWidths.get(i);
                     res += "<tr><td";
                     if (cellWidth > -1) {
                         res += " height=\"" + Integer.toString(cellWidth) + "\"";
                     }
                     if (item != null) {
                         if (item.getHorizontalAlign() != null) {
                             res += " align=\"" + item.getHorizontalAlign().toString() + "\"";
                         } else {
                             res += " align=\"left\"";
                         }
                         if (item.getVerticalAlign() != null) {
                             res += " valign=\"" + item.getVerticalAlign().toString() + "\"";
                         } else {
                             res += " valign=\"top\"";
                         }
                     }
                     res += ">\n";
 
                     if (item != null) {
                         if (item.isVisible()) {
                             res = res + item.render() + "\n";
                         }
                     }
 
                     res += "</td></tr>\n";
                 //}
             //}
         }
         res += "</table>\n";
         return res;
     }
 
 }
