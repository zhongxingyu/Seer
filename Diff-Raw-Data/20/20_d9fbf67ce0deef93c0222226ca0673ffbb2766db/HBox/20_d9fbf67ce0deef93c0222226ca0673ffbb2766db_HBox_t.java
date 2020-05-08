 package eu.xibit.jwui.components;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class HBox extends AComponent {
 
     private final List<AComponent> childs = new ArrayList<AComponent>();
     private final List<Integer> cellWidths = new ArrayList<Integer>();
     
     private int spacing = 0;
     private int padding = 0;
 
 
 
 
     public HBox() {
         super();
     }
 
 
 
 
     public void addComponent(AComponent component, int cellWidth) {
         this.childs.add(component);
         this.cellWidths.add(new Integer(cellWidth));
     }
     
     public AComponent getComponent(int index) {
         return this.childs.get(index);
     }
 
     public int getCellWidth(int index) {
         return this.cellWidths.get(index).intValue();
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
 
     public void insertComponent(int index, AComponent component, int cellWidth) {
         this.childs.add(index, component);
         this.cellWidths.add(index, new Integer(cellWidth));
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
         
 
        String res = "<table style=\"" + style + "\" cellspacing=\"0\" cellpadding=\"0\""; 
         if (this.getHeight() >= 0.0) {
             res += " height=\"";
             if (this.getHeight() == 0.0) {
                 res += "0";
             } else if (this.getHeight() >= 1.0) {
                 res += Integer.toString((int)this.getWidth());
             } else {
                 res += Double.toString((this.getWidth() * 100.0)) + "%;";
             }
             res += "\"";
         }
        res += "\"><tr>\n";
 
         boolean first = true;
         int cellWidth = -1;
         AComponent item = null;
         for (int i = 0; i < this.childs.size(); i++) {
             item = this.childs.get(i);
             //if (this.childs.get(i) != null) {
                 //if (this.childs.get(i){AComponent}.isVisible()) {
                     cellWidth = this.cellWidths.get(i);
                     res += "<td";
                     if (cellWidth > -1) {
                         res += " width=\"" + Integer.toString(cellWidth) + "\"";
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
 
                     res += "</td>\n";
                    if (spacing > 0) {
                        res += "<td style=\"width:" + spacing + "px;\"></td>";
                    }
                 //}
             //}
         }
         res += "</tr></table>\n";
         return res;
     }
 
 }
