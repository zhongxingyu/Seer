package cytoscape.dialogs;
 import java.awt.Color;
 public class MutableColor {
     private Color color;
     public MutableColor(Color color) {
 	this.color = color;
     }
     public Color getColor() {
 	return this.color;
     }
     public void setColor(Color c) {
 	this.color = c;
     }
 }
