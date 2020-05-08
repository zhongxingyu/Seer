 package com.chromabits.ugaacm.WarpDrive.UI;
 
 import java.util.Vector;
import com.chromabits.ugaacm.WarpDrive.render.GlProgram;
 
 public class Component implements ComponentInterface {
 
     protected static Vector<Component> components;
     protected int x, y, height, width, priority;
     protected String ID; // ID is used for searching
     protected Text text; // text is printed on top of the component
 
     // Constructors
     public Component(int x, int y, int height, int width, int priority, String ID, Text text) {
 
         this.x = x;
         this.y = y;
         this.height = height;
         this.width = width;
         this.priority = priority;
         this.ID = ID;
         this.text = text;
 
     }
     public Component(int x, int y, int height, int width, int priority, String ID) {
 
         this(x, y, height, width, priority, ID, null);
     }
     public Component(Component component) { // Copy a component's structure.
 
         this(component.x, component.y, component.height, component.width, component.priority, component.ID, component.text);
     }
 
     public static Component FindComponent(String ID) { // Looks through components
 
         for (int i = 0; i < components.size(); i++) {
             if (components.elementAt(i).ID.equals(ID)) {
                 return components.elementAt(i);
             }
         }
         return null;
 
     }
 
     // X
     public int getX() {
         return this.x;
     }
     public void setX(int x) {
         this.x = x;
     }
 
     // Y
     public int getY() {
         return this.y;
     }
     public void setY(int y) {
         this.y = y;
     }
 
     // Height
     public int getHeight() {
         return this.height;
     }
     public void setHeight(int height) {
         this.height = height;
     }
 
     // Width
     public int getWidth() {
         return this.width;
     }
     public void setWidth(int width) {
         this.width = width;
     }
 
     // Priority - Higher priority value means it is printed in front of other components
     public int getPriority() {
         return this.priority;
     }
     public void setPriority(int priority) {
         this.priority = priority;
         // TODO Re-sort components based on priority
     }
 
     // ID - Used for finding a component in the vector of components
     public String getID() {
         return this.ID;
     }
     public void setID(String ID) {
         this.ID = ID;
     }
 
     // Text
     public Text getText() {
         return this.text;
     }
     public void setText(Text text) {
         this.text = text;
     }
 
 
    public void draw(GlProgram world) {
 
         // TODO Implement this method once Rendering is complete
 
     }
 
 }
