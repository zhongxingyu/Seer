 /**
  * @author Tadanori TERUYA &lt;tadanori.teruya@gmail.com&gt; (2012)
  */
 /*
  * Copyright (c) 2012 Tadanori TERUYA (tell) <tadanori.teruya@gmail.com>
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation files
  * (the "Software"), to deal in the Software without restriction,
  * including without limitation the rights to use, copy, modify, merge,
  * publish, distribute, sublicense, and/or sell copies of the Software,
  * and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  * @license: The MIT license <http://opensource.org/licenses/MIT>
  */
 package com.github.tell.util.datastructure;
 
 import java.io.Serializable;
 
 public final class ImmutableObject<T extends Serializable> implements
         Serializable, Cloneable {
 
     /**
      *
      */
     private static final long serialVersionUID = -8177196769410093651L;
 
     private T o;
     private CopyFunction<T> copyFunc;
 
     public String toString() {
         return o.toString();
     }
 
     public int hashCode() {
         return o.hashCode();
     }
 
     public boolean equals(final Object o) {
         if (o instanceof ImmutableObject<?>) {
             @SuppressWarnings("unchecked")
             final ImmutableObject<T> x = (ImmutableObject<T>) o;
             return this.o.equals(x.o);
         } else {
             return false;
         }
     }
 
     public ImmutableObject() {
         this.o = null;
         this.copyFunc = null;
     }
 
     public ImmutableObject(final T o) {
         this.o = o;
         this.copyFunc = new CopyFunction<T>(o);
     }
 
     @SuppressWarnings("unchecked")
     public ImmutableObject<T> clone() {
         try {
            final ImmutableObject<T> result = (ImmutableObject<T>) super
                    .clone();
             result.o = this.copyFunc.call();
             result.copyFunc = this.copyFunc;
             return result;
         } catch (CloneNotSupportedException e) {
             e.printStackTrace();
             return null;
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 
     public T getObject() {
         try {
             return this.copyFunc.call();
         } catch (Exception e) {
             e.printStackTrace();
             return null;
         }
     }
 }
