 /*
  * Copyright (C) 2013 Dabo Ross <http://www.daboross.net/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.skywars.api.parent;
 
import lombok.Getter;

 /**
  *
  * @author Dabo Ross <http://www.daboross.net/>
  * @param <T> This class
  */
public class Parentable<T extends Parentable<?>>{
 
    @Getter
     protected T parent;
 
     public Parentable() {
     }
 
     public Parentable(T parent) {
         this.parent = parent;
         checkParentCycle();
     }
 
     public T getParent() {
         return parent;
     }
 
     /**
      * Sets the parent
      *
      * @param parent the new parent to inherit values from
      */
     public void setParent(T parent) {
         this.parent = parent;
         checkParentCycle();
     }
 
     /**
      * Makes sure that there are no parent inheritance loops
      *
      * @throws IllegalArgumentException if there are parent inheritance loops
      */
     public final void checkParentCycle() throws IllegalArgumentException {
        if (checkParentCycle(this)) {
            throw new IllegalArgumentException("Parent inheritance loop.");
        }
     }
 
     /**
      * Makes sure that there are no parent inheritance loops.
      *
      * @param original The original parent that checkParentCycle() was called on
     * @return true if there are parent inheritance loops, false otherwise
      */
    public final boolean checkParentCycle(Object original) {
         if (parent == null) {
            return false;
         }
        if (parent.equals(original)) {
            return true;
         }
        return parent.checkParentCycle(original);
     }
 }
