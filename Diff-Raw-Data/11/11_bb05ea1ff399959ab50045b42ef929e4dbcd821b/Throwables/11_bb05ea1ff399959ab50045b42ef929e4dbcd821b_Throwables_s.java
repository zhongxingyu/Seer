 /* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil -*-
  *
  * Copyright (c) 2011 Edugility LLC.
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use, copy,
  * modify, merge, publish, distribute, sublicense and/or sell copies
  * of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THIS SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT.  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
  * DEALINGS IN THE SOFTWARE.
  *
  * The original copy of this license is available at
  * http://www.opensource.org/license/mit-license.html.
  */
 package com.edugility.throwables;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 /**
  * A class to assist with processing {@link Throwable} instances.
  *
  * @author <a href="mailto:ljnelson@gmail.com">Laird Nelson</a>
  *
  * @since 1.0
  */
 public final class Throwables {
 
   /**
    * Creates a new {@link Throwables} object.
    */
   private Throwables() {
     super();
   }
 
   /**
    * Returns the "deepest" {@linkplain Throwable#getCause() cause}
    * reachable from the supplied {@link Throwable}.
    *
    * @param t the {@link Throwable} to investigate; may be {@code null}
    *
    * @return the "deepest" {@linkplain Throwable#getCause() cause}
    * reachable from the supplied {@link Throwable}, or {@code null}
    */
   public static final Throwable getPrimordialCause(Throwable t) {
     if (t == null) {
       return null;
     }
     Throwable cause = t;
     while ((cause = cause.getCause()) != null) {
       t = cause;
     }
     return t;
   }
 
   /**
    * Creates and returns a view of the supplied {@link Throwable} and
    * its {@linkplain Throwable#getCause() causal chain} as a {@link
    * List}.
    *
    * <p>This method never returns {@code null}.</p>
    *
   * <p>This method typically allocates a new {@link List} when
   * called.  If only the ability to iterate is desired, see the
   * {@link #asIterable(Throwable)} or {@link #asIterator(Throwable)}
   * methods; neither of those methods creates a list.</p>
    *
    * @param throwable the {@link Throwable} in question; may be {@code null}
    *
    * @return a {@link List} of {@link Throwable}s; never {@code null};
    * the return value will always contain the supplied {@link
    * Throwable} as its first element unless the supplied {@link
    * Throwable} is {@code null}
    */
   public static final List<Throwable> toList(final Throwable throwable) {
     final List<Throwable> l = toList(throwable, null);
     if (l == null) {
       return Collections.emptyList();
     } else {
       return Collections.unmodifiableList(l);
     }
   }
 
   private static final List<Throwable> toList(final Throwable throwable, List<Throwable> l) {
     final List<Throwable> returnValue;
     if (throwable == null) {
       returnValue = Collections.emptyList();
     } else {
 
       final Iterable<?> throwables;
       if (throwable instanceof Iterable) {
         throwables = (Iterable<?>)throwable;
       } else {
         throwables = Collections.singleton(throwable);
       }
 
       if (l == null) {
         l = new ArrayList<Throwable>();
       }
 
       final Iterator<?> iterator = throwables.iterator();
       boolean found = false;
       if (iterator != null) {
 
         while (iterator.hasNext()) {
           final Object o = iterator.next();
           if (o instanceof Throwable) {
 
             final Throwable t = (Throwable)o;
             found = found || t == throwable;
             l.add(t);
 
             final Throwable cause = t.getCause();
             if (cause != null) {
               assert cause != t; // shouldn't be possible according to Throwable contract
               final List<Throwable> list = toList(cause, l); // recursive call
               assert list == l;
               found = found || l.contains(throwable);
             }              
      
           }
         }
       }
       if (!found) {
         l.add(0, throwable);
       }
       returnValue = l;
     }
     return returnValue;
   }
 
   /**
    * Returns the first {@link Throwable} in the {@linkplain
    * Throwable#getCause() causal chain} of the supplied {@link
    * Throwable} (or the supplied {@link Throwable} itself) that is an
    * instance of the supplied {@link Class}.
    *
    * <p>This method may return {@code null}.</p>
    *
    * @param t the {@link Throwable} whose causal chain should be
    * investigated; may be {@code null} in which case {@code null} will
    * be returned
    *
    * @param throwableClass the {@link Class} whose {@link
    * Class#isInstance(Object)} method will be called; if {@code null}
    * then {@code null} will be returned
    *
    * @return the first {@link Throwable} in the causal chain that is
    * an instance of the supplied {@link Class}, or {@code null}
    */
   public static final <T extends Throwable> T firstInstance(Throwable t, final Class<T> throwableClass) {
     return nthInstance(t, throwableClass, 0);
   }
 
   /**
    * Returns a {@link Throwable} in the {@linkplain
    * Throwable#getCause() causal chain} of the supplied {@link
    * Throwable} (or the supplied {@link Throwable} itself) that is the
    * appropriate instance of the supplied {@link Class}.
    *
    * <p>This method may return {@code null}.</p>
    *
    * @param throwable the {@link Throwable} whose causal chain should
    * be investigated; may be {@code null} in which case {@code null}
    * will be returned
    *
    * @param throwableClass the {@link Class} whose {@link
    * Class#isInstance(Object)} method will be called; if {@code null}
    * then {@code null} will be returned
    *
    * @param zeroBasedOccurrence the {@code 0}-based number that
    * identifies which of the potentially many matches to return.
    * {@code 0} will return the first {@link Throwable} that matches,
    * {@code 1} will return the second one, and so on.
    *
    * @return the {@code n}<sup>th</sup> {@link Throwable} in the
    * causal chain that is an instance of the supplied {@link Class},
    * or {@code null}
    */
   public static final <T extends Throwable> T nthInstance(Throwable throwable, final Class<T> throwableClass, int zeroBasedOccurrence) {
     zeroBasedOccurrence = Math.max(0, zeroBasedOccurrence);
     T returnValue = null;
     if (throwableClass != null) {
       final Iterable<?> throwables;
       if (throwable instanceof Iterable) {
         throwables = (Iterable<?>)throwable;
       } else {
         throwables = Collections.singleton(throwable);
       }
       final Iterator<?> iterator = throwables.iterator();
       if (iterator != null) {
         while (iterator.hasNext()) {
           final Object o = iterator.next();
           if (o instanceof Throwable) {
             Throwable t = (Throwable)o;
             int index = 0;
             while (t != null) {
               if (throwableClass.isInstance(t)) {
                 if (zeroBasedOccurrence == index++) {
                   returnValue = throwableClass.cast(t);
                   break;
                 }
               }
               t = t.getCause();
             }
           }
         }
       }
     }
     return returnValue;
   }
 
   /**
    * Returns the last {@link Throwable} in the {@linkplain
    * Throwable#getCause() causal chain} of the supplied {@link
    * Throwable} (or the supplied {@link Throwable} itself) that is an
    * instance of the supplied {@link Class}.
    *
    * <p>This method may return {@code null}.</p>
    *
    * @param t the {@link Throwable} whose causal chain should be
    * investigated; may be {@code null} in which case {@code null} will
    * be returned
    *
    * @param throwableClass the {@link Class} whose {@link
    * Class#isInstance(Object)} method will be called; if {@code null}
    * then {@code null} will be returned
    *
    * @return the last {@link Throwable} in the causal chain that is
    * an instance of the supplied {@link Class}, or {@code null}
    */
   public static final <T extends Throwable> T lastInstance(Throwable t, final Class<T> throwableClass) {
     T returnValue = null;
     if (throwableClass != null) {
       while (t != null) {
         if (throwableClass.isInstance(t)) {
           returnValue = throwableClass.cast(t);
         }
         t = t.getCause();
       }
     }
     return returnValue;
   }
 
 }
