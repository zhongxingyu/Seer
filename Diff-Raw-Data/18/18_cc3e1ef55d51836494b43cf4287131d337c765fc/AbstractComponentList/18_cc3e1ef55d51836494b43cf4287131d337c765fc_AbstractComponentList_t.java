 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.techlogic.BaSliGC.util;
 
 import de.techlogic.BaSliGC.decorated.Clickable;
 import de.techlogic.BaSliGC.decorated.Dragable;
 import de.techlogic.BaSliGC.util.gamecomponent.GameComponent;
 
 /**
  * Abstact implementation of the ComponentList.
  *
  * @author Nils
  */
 public abstract class AbstractComponentList<MouseListener> implements ComponentList {
 
     private MouseListener mouseListener;
     private ComponentLink<GameComponent> componentList;
    private ComponentLink<GameComponent> lastComponent;
     private ComponentLink<Dragable> dragableList;
     private ComponentLink<Clickable> clickableList;
     private Dragable active;
     public int size;
 
     /**
      * Default constructor creates all internal LinkLists with a dummy head.
      *
      */
     public AbstractComponentList() {
         clickableList = new ComponentLink(null);
         dragableList = new ComponentLink(null);
         componentList = new ComponentLink(null);
         active = null;
         size = 0;
 
     }
 
     /**
      * sets the internal mouseListener that is need to react to input made by
      * the user. Needs to been callen in the constuctor of child classes
      *
      * @param mouseListener MouseListenerObject that should be attached to the
      * ComponentList
      */
     protected void setMouseListener(MouseListener mouseListener) {
         this.mouseListener = mouseListener;
     }
 
     @Override
     public void addClickable(Clickable click) {
         addComponent(click);
         ComponentLink<Clickable> link = new ComponentLink(click);
         link.next = clickableList.next;
         clickableList.next = link;
         size++;
     }
 
    private  void addAtLast(GameComponent component) {
        ComponentLink<GameComponent> link = new ComponentLink(component);

        lastComponent.next = link;
        lastComponent = link;

    }

     @Override
     public void addComponent(GameComponent component) {
         ComponentLink<GameComponent> link = new ComponentLink(component);
         link.next = componentList.next;
         componentList.next = link;
        if (lastComponent == null) {
            lastComponent = link;
        }
        System.out.println(lastComponent);
         size++;
     }
 
     @Override
     public void addDragable(Dragable drag) {
         addComponent(drag);
         ComponentLink<Dragable> link = new ComponentLink(drag);
         link.next = dragableList.next;
         dragableList.next = link;
         size++;
     }
 
     private void removeObject(ComponentLink link, GameComponent component) {
         ComponentLink<Clickable> activeLink = link;
         while (activeLink.next != null) {
             if (activeLink.next.content == component) {
                 activeLink.next = activeLink.next.next;
                 break;
             }
             activeLink = activeLink.next;
         }
 
     }
 
     @Override
     public void removesClickable(Clickable click) {
         removeObject(clickableList, click);
     }
 
     @Override
     public void removesComponent(GameComponent component) {
         removeObject(componentList, component);
     }
 
     @Override
     public void removesDragable(Dragable drag) {
         removeObject(dragableList, drag);
     }
 
     @Override
     public void draw() {
         ComponentLink<GameComponent> activeLink = componentList;
         while (activeLink.next != null) {
             activeLink = activeLink.next;
             activeLink.content.draw();
         }
     }
 
     @Override
     public MouseListener getMouseListener() {
         return mouseListener;
     }
 
     /**
      * search in the internal list if a clickable object is clicked and fires
      * the Click event of this Clickable. This method should be invoked when a
      * mouse click event occure in the mouseListener.
      *
      * @param x X coordinate of the mousepointer
      * @param y Y coordinate of the mousepointer
      */
     protected void fireClicked(int x, int y) {
         ComponentLink<Clickable> activeLink = clickableList;
         while (activeLink.next != null) {
             activeLink = activeLink.next;
             if (activeLink.content.fireMouseClick(x, y)) {
                 break;
             }
         }
     }
 
     /**
      * search in the internal list if a Dragable object is press and fires the
      * pressed event of this Dragable. Sets the active object to the found one.
      * This method should be invoked when a mouse click event occure in the
      * mouseListener.
      *
      * @param x X coordinate of the mousepointer
      * @param y Y coordinate of the mousepointer
      */
     protected void firePressed(int x, int y) {
         ComponentLink<Dragable> activeLink = dragableList;
         while (activeLink.next != null) {
             activeLink = activeLink.next;
             if (activeLink.content.fireIsPressed(x, y)) {
                 active = activeLink.content;
                removesComponent(active);
                addAtLast(active);
                 break;
             }
 
         }
     }
 
     /**
      * fires the realease event of the active object and set the element back to
      * null. This method should be invoked when a mouse released event occure in
      * the mouseListener.
      *
      */
     protected void fireRealesed() {
         if (active != null) {
             active.fireIsReleased();
             active = null;
         }
     }
 
     /**
      * fires the dragged event of the active object. This method should be
      * invoked when a mouse dragged event occure in the mouseListener.
      *
      * @param newx new X Coordinate of the mousepointer
      * @param newy new Y Coordinate of the mousepointer
      */
     protected void fireDragged(int newx, int newy) {
         if (active != null) {
             active.fireIsDragged(newx, newy);
 
 
         }
 
     }
 
     private class ComponentLink<T> {
 
         ComponentLink<T> next;
         T content;
 
         public ComponentLink(T content) {
             this.content = content;
         }
     }
 }
