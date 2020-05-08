 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package ui.menu;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.ListIterator;
 
 import org.newdawn.slick.Graphics;
 
 public class Menu {
   private final LinkedList<MenuItem> items;
   private ListIterator<MenuItem> iterator;
   private MenuItem current;
 
   public Menu(Collection<MenuItem> items) {
     this.items = new LinkedList<MenuItem>(items);
     this.iterator = this.items.listIterator();
     this.current = iterator.next();
    this.current.setState(MenuItemState.ACTIVE);
   }
 
   public void click() {
     if (current instanceof MenuButton) {
       ((MenuButton) current).click();
     }
   }
 
   public void render(Graphics g) {
     for (MenuItem i : items) {
       i.render(g);
     }
   }
 
   public void up() {
     if (iterator.hasPrevious())
       current.setState(MenuItemState.NORMAL);
 
     while (iterator.hasPrevious()) {
       current = iterator.previous();
 
       if (current.getState() == MenuItemState.NORMAL) {
         current.setState(MenuItemState.ACTIVE);
         break;
       }
     }
   }
 
   public void down() {
     if (iterator.hasNext())
       current.setState(MenuItemState.NORMAL);
 
     while (iterator.hasNext()) {
       current = iterator.next();
 
       if (current.getState() == MenuItemState.NORMAL) {
         current.setState(MenuItemState.ACTIVE);
         break;
       }
     }
   }
 }
