 package org.lawrencebower.docgen.core.document.component.table;
 
 import org.lawrencebower.docgen.core.document.component.DocComponent;
 import org.lawrencebower.docgen.core.document.component.TableTextComponent;
 import org.lawrencebower.docgen.core.document.component.position.VerticalAlignment;
 import org.lawrencebower.docgen.core.document.component.text.TextBlock;
 
 import java.awt.*;
 
 public class TableCell {
 
     private DocComponent component;
 
     /**
      * horizontal alignment is sourced from the inner DocComponent
      */
     private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;//default
 
     private Color backgroundColor;
    private float padding;//not set by default - tableCellPadding is used
     private int rowSpan = 1;//default
     private int colSpan = 1;//default
 
     public TableCell() {
     }
 
     public TableCell(DocComponent component) {
         this.component = component;
     }
 
     public TableCell(String text) {
         TextBlock textBlock = new TextBlock(text);
         this.component = new TableTextComponent(textBlock);
     }
 
     public void setComponent(DocComponent component) {
         this.component = component;
     }
 
     public DocComponent getComponent() {
         return component;
     }
 
     public void setText(String text) {
         TextBlock textBlock = new TextBlock(text);
         this.component = new TableTextComponent(textBlock);
     }
 
     public VerticalAlignment getVerticalAlignment() {
         return verticalAlignment;
     }
 
     public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
         this.verticalAlignment = verticalAlignment;
     }
 
     public void setBackgroundColor(Color backgroundColor) {
         this.backgroundColor = backgroundColor;
     }
 
     public Color getBackgroundColor() {
         return backgroundColor;
     }
 
     public boolean hasBackgroundColor(){
         return backgroundColor != null;
     }
 
     public void setPadding(float padding) {
         this.padding = padding;
     }
 
     public float getPadding() {
         return padding;
     }
 
     public void setRowSpan(int rowSpan) {
         this.rowSpan = rowSpan;
     }
 
     public int getRowSpan() {
         return rowSpan;
     }
 
     public void setColSpan(int colSpan) {
         this.colSpan = colSpan;
     }
 
     public int getColSpan() {
         return colSpan;
     }
 
 }
