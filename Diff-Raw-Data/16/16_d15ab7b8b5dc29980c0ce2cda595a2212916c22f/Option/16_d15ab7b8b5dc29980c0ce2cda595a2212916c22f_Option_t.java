 /*
 The MIT License (MIT)
 
Copyright (c) 2013 Joey Gibson <joey@joeygibson.com>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in
 the Software without restriction, including without limitation the rights to
 use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 the Software, and to permit persons to whom the Software is furnished to do so,
 subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */
 
 package com.joeygibson.option;
 
 import java.util.NoSuchElementException;
 
 /**
  * Simple implementation of Scala-like Option class hierarchy. Call Option.option(foo), passing in your thing that can
  * be null. If it is null, you will get back a None<T>; if it is not null, you'll get a Some<T>. Either way, in your
  * code, you don't need any null checks.
  * @param <T> the type of thing to wrap
  */
 public abstract class Option<T> {
     protected T thing;
     protected boolean isDefined;
 
     protected Option(T thing, boolean isDefined) {
         this.thing = thing;
         this.isDefined = isDefined;
     }
 
     public boolean isDefined() {
         return isDefined;
     }
 
     public boolean isEmpty() {
         return !isDefined;
     }
 
     public static <T> Option<T> option(T thing) {
         return thing == null ?
                 new None<T>(thing) :
                 new Some<T>(thing);
     }
 
     /**
      * If the receiver is a Some<T>, this returns the instance of the T. Otherwise, it's
      * a None<T>, and will throw a NoSuchElementException.
      * @return
      */
     public abstract T get();
 
     /**
      * If the receiver is a Some<T>, this returns the instance of the T. Otherwise, it's
      * a None<T>, and will return the passed-in alternate.
      * @param alternate
      * @return
      */
     public abstract T getOrElse(T alternate);
 }
 
 /**
  * If you call Option.option(foo) with a non-null thing, you'll get a Some<T>.
  * @param <T> the type of thing to wrap
  */
 class Some<T> extends Option<T> {
     protected Some(T thing) {
         super(thing, true);
     }
 
     @Override
     public T get() {
         return thing;
     }
 
     @Override
     public T getOrElse(T alternate) {
         return thing;
     }
 }
 
 /**
  * If you call Option.option(foo) with a null thing, you'll get a None<T>.
  * @param <T> the type of thing to wrap
  */
 class None<T> extends Option<T> {
 
     protected None(T thing) {
         super(thing, false);
     }
 
     @Override
     public T get() {
         throw new NoSuchElementException();
     }
 
     @Override
     public T getOrElse(T alternate) {
         return alternate;
     }
 }
