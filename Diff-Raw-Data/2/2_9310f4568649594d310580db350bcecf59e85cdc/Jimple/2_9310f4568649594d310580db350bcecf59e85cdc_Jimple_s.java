 package org.jimple;
 /*
  * This file is part of Jimple.
  *
  * Copyright (c) 2013 Kuzmin Leonid
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is furnished
  * to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 import java.util.HashMap;
 
 /**
  * Jimple main class.
  *
  * @author Leonid Kuzmin
  */
 public class Jimple extends HashMap<String, Object> {
     /**
      * Jimple Item
      */
     public interface Item {
         public Object create(Jimple c);
     }
 
     /**
      * Jimple Item extender
      */
     public interface Extender {
         public Object extend(Object object);
     }
 
     private class SimpleItem {
         private final Item item;
         private final Jimple c;
 
         private SimpleItem(Item item, Jimple c) {
             this.item = item;
             this.c = c;
         }
 
         protected Object create() {
             return this.item.create(this.c);
         }
 
         private Item getItem() {
             return item;
         }
     }
 
     private class SharedItem extends SimpleItem {
         private Object instance;
 
         private SharedItem(Item item, Jimple c) {
             super(item, c);
         }
 
         @Override
         protected Object create() {
             if (this.instance == null) {
                 this.instance = super.create();
             }
             return instance;
         }
     }
 
     private class ExtendedItem extends SimpleItem {
         private Extender extender;
 
         private ExtendedItem(Item item, Jimple c, Extender extender) {
             super(item, c);
             this.extender = extender;
         }
 
         @Override
         protected Object create() {
             return this.extender.extend(super.create());
         }
     }
 
     /**
      * Returns a SharedItem that stores the result of the given Item for
      * uniqueness in the scope of this instance of Jimple.
      *
      * @param item A Item to wrap for uniqueness
      * @return The wrapped Item
      */
     public SharedItem share(Item item) {
         return new SharedItem(item, this);
     }
 
     /**
      * Extends and Item definition
      * Useful when you want to extend an existing Item definition,
      * without necessarily loading that object.
      *
      * @param key      The unique identifier for the Item
      * @param extender A Extender for the original
      * @return The wrapped Item
      * @throws IllegalArgumentException if the identifier is not defined
      */
     public ExtendedItem extend(String key, Extender extender) {
         if (!super.containsKey(key)) {
             throw new IllegalArgumentException("Identifier " + key + " is not defined.");
         }
         Object item = super.get(key);
         if (item instanceof SimpleItem) {
            return new ExtendedItem((Item) super.get(key), this, extender);
         }
         throw new IllegalArgumentException("Identifier " + key + " does not contain an object definition.");
     }
 
     @Override
     public Object get(Object key) {
         Object item = super.get(key);
         if (item instanceof SimpleItem) {
             item = ((SimpleItem) item).create();
         }
         return item;
     }
 
     @Override
     public Object put(String key, Object value) {
         if (value instanceof Item) {
             value = new SimpleItem((Item) value, this);
         }
         return super.put(key, value);
     }
 
     /**
      * Gets a parameter or the Item defining an object.
      *
      * @param key The unique identifier for the parameter or object
      * @return The value of the parameter or the Item defining an object
      */
     public Object raw(String key) {
         Object value = super.get(key);
         if (value instanceof SimpleItem) {
             value = ((SimpleItem) super.get(key)).getItem();
         }
         return value;
     }
 }
