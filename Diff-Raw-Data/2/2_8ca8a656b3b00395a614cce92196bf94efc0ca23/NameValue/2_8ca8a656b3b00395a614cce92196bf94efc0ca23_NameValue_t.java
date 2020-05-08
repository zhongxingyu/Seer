 /* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
  *
  * Copyright (c) 2013 Edugility LLC.
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
 package com.edugility.nomen;
 
 import java.io.Serializable;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * An {@link AbstractValued} implementation that represents the value
  * of a name of something.
  *
  * <p>In the nomenclature of the Nomen project, a {@linkplain Name
  * <em>name</em>} is a {@linkplain NameValue <em>name value</em>} that
  * is {@linkplain Name#getNamed() <em>owned</em>} by {@linkplain Named
  * something that is <em>named</em>}.  Each concept has a
  * corresponding class.</p>
  *
  * <p>A {@link NameValue} may be <em>atomic</em>&mdash;basically a
  * glorified {@link String}, indivisible, not subject to any further
  * interpretation&mdash;or not atomic&mdash;in which case it is
  * interpreted as an <a href="http://mvel.codehaus.org/>MVEL</a>
  * template that will be interpolated once the given {@link NameValue}
  * is {@linkplain Name#getNameValue() owned} by a {@link Name}.
  * {@link NameValue}s are <em>not</em> atomic by default.</p>
  *
  * <p>A {@link NameValue}, once initialized with a {@linkplain
  * #setValue(String) value}, whether via {@linkplain
  * #NameValue(String, boolean) the constructor} or the {@link
  * #setValue(String)} and {@link #setAtomic(boolean)} methods, can be
  * treated as though it is immutable.  Subsequent attempts to call
  * either the {@link #setValue(String)} or {@link #setAtomic(boolean)}
  * methods will fail with {@link IllegalStateException}s.</p>
  *
  * <p>Two {@link NameValue}s are {@linkplain #equals(Object) equal} if
  * their {@linkplain AbstractValued#getValue() values} are {@linkplain
  * String#equals(Object) equal} and if they are both {@linkplain
  * #isAtomic() atomic}.</p>
  *
  * <h4>Design Notes</h4>
  *
  * <p>Methods and fields that might otherwise be {@code final} are
  * explicitly left non-{@code final} so that this class may be used as
  * a JPA entity.  Certain methods, like {@link #setAtomic(boolean)}
  * and {@link #setValue(String)}, may only be called once.</p>
  *
  * @author <a href="http://about.me/lairdnelson"
  * target="_parent">Laird Nelson</a>
  *
  * @see Name
  *
  * @see AbstractValued
  *
  * @see Named
  */
 public class NameValue extends AbstractValued {
 
 
   /*
    * Static fields.
    */
 
 
   /**
    * The version of this class for {@linkplain Serializable
    * serialization purposes}.
    *
    * @see Serializable
    */
   private static final long serialVersionUID = 1L;
 
   /**
    * The size of the cache used by the {@link #valueOf(String)}
    * method; {@code 20} by default and linked to the {@code
    * nomen.NameValue.cacheSize} {@linkplain System#getProperty(String,
    * String) system property}.
    *
    * @see #valueOf(String)
    */
   private static final int cacheSize = Integer.getInteger("nomen.NameValue.cacheSize", 20);
 
   /**
    * A cache of {@link NameValue} instances used by the {@link
    * #valueOf(String)} method.  A {@linkplain
    * ReadWriteLock#writeLock() write lock} must be obtained from the
    * {@link #cacheLock} field before modifying this field, and a
    * {@linkplain ReadWriteLock#readLock() read lock} must be obtained
    * from the {@link #cacheLock} field before reading from this field.
    *
    * <p>This field is never {@code null}.</p>
    *
    * @see #valueOf(String)
    */
   private static final Map<String, NameValue> cache = new LinkedHashMap<String, NameValue>(cacheSize, 0.75F, true) {
     private static final long serialVersionUID = 1L;
     @Override
     protected final boolean removeEldestEntry(final Entry<String, NameValue> entry) {
       return this.size() > cacheSize;
     }
   };
 
   /**
    * A {@link ReadWriteLock} used to synchronized access to the {@link
    * #cache} field; used only by the {@link #valueOf(String)} method.
    *
    * <p>This field is never {@code null}.</p>
    */
   private static final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
 
 
   /*
    * Instance fields.
    */
 
 
   /**
    * Whether this {@link NameValue} is considered to be
    * <em>atomic</em>&mdash;i.e. not a template in need of further
    * interpolation, but just a simple textual value.
    *
    * <p>This field may be {@code null}.</p>
    *
    * <h4>Design Notes</h4>
    *
    * <p>This field is a {@link Boolean} and not a {@code boolean} so
    * that its initial setting via the {@link #setAtomic(boolean)}
    * method can be tracked.  It is not {@code final} so that this
    * class may be used as a JPA entity.</p>
    *
    * @see #isAtomic()
    *
    * @see #setAtomic(boolean)
    */
   private Boolean atomic;
 
 
   /*
    * Constructors.
    */
 
 
   /**
    * Creates a new {@link NameValue} in an incomplete state; to fully
    * initialize the new {@link NameValue} callers must invoke the
    * {@link #setValue(String)} method and the {@link
    * #setAtomic(boolean)} method.
    *
    * <h4>Design Notes</h4>
    *
    * <p>This constructor exists for JPA compatibility.  Please
    * consider using the {@link #NameValue(String, boolean)}
    * constructor instead which fully initializes a {@link NameValue}
    * instance as it creates it.</p>
    *
    * @see #NameValue(String, boolean)
    */
   protected NameValue() {
     super();
   }
 
   /**
    * Creates a new {@link NameValue} that is not {@linkplain
    * #isAtomic() atomic}.
    *
    * <p>This constructor calls the {@link #NameValue(String, boolean)}
    * constructor with {@code false} as the value for its second
    * parameter.</p>
    *
    * @param value the {@linkplain #setValue(String) value} for this
    * {@link NameValue}; must not be {@code null}
    *
    * @exception IllegalArgumentException if {@code value} is {@code
    * null}
    *
    * @see #NameValue(String, boolean)
    *
    * @see #isAtomic()
    */
   public NameValue(final String value) {
     this(value, false);
   }
 
   /**
    * Creates a new {@link NameValue}.
    *
    * @param value the {@linkplain #setValue(String) value} for this
    * {@link NameValue}; must not be {@code null}
    *
    * @param atomic whether or not this new {@link NameValue} will be
    * {@linkplain #isAtomic() atomic}
    *
    * @exception IllegalArgumentException if {@code value} is {@code
    * null}
    */
   public NameValue(final String value, final boolean atomic) {
     super(value);
     this.setAtomic(atomic);
   }
 
 
   /*
    * Instance methods.
    */
 
   
   /**
    * Returns {@code true} if this {@link NameValue} is fully
    * initialized and hence immutable.
    *
    * @return {@code true} if this {@link NameValue} is fully
    * initialized
    *
    * @see #setValue(String)
    *
    * @see #setAtomic(boolean)
    */
   public boolean isInitialized() {
    return this.atomic != null && this.getValue() != null;
   }
 
   /**
    * Returns {@code true} if this {@link NameValue} is <em>atomic</em>
    * &mdash;if it is a simple textual value and therefore not a
    * template that must be interpolated.
    *
    * @return {@code true} if this {@link NameValue} is an indivisible
    * textual value; {@code false} otherwise
    */
   public boolean isAtomic() {
     return this.atomic != null && this.atomic.booleanValue();
   }
 
   /**
    * Sets whether this {@link NameValue} is <em>atomic</em> or
    * not&mdash;whether it is a simple textual value or is a template
    * that must be interpolated.
    *
    * <p>This method may only be called once to set the initial value
    * for this property.  If it is called again, an {@link
    * IllegalStateException} will be thrown.</p>
    *
    * @param atomic whether this {@link NameValue} is <em>atomic</em> or
    * not&mdash;whether it is a simple textual value or is a template
    * that must be interpolated
    *
    * @exception IllegalStateException if this method is called more
    * than once
    */
   public void setAtomic(final boolean atomic) {
     if (this.atomic == null) {
       this.atomic = Boolean.valueOf(atomic);
     } else if (this.atomic.booleanValue() != atomic) {
       throw new IllegalStateException();
     }
   }
 
   /**
    * Returns a hashcode for this {@link NameValue} based off the
    * return value of this {@link NameValue}'s {@link #isAtomic()}
    * method as well as its {@linkplain AbstractValued#hashCode()
    * superclass' hashcode}.
    *
    * @return a hashcode for this {@link NameValue}
    */
   @Override
   public int hashCode() {
     int result = 17;
     
     int c = Boolean.valueOf(this.isAtomic()).hashCode();
     result = result * 37 + c;
     
     c = super.hashCode();
     result = result * 37 + c;
     
     return result;
   }
 
   /**
    * Returns {@code true} if the supplied {@link Object} is an
    * instance of {@link NameValue} and returns the same value from its
    * {@link #isAtomic()} method as is returned from this {@link
    * NameValue}'s {@link #isAtomic()} method and returns a value from
    * its {@link AbstractValued#getValue()} method that is {@linkplain
    * String#equals(Object) equal to} the value returned by this {@link
    * NameValue}'s {@link AbstractValued#getValue()} method.
    *
    * @param other the {@link Object} to compare this {@link NameValue}
    * against for equality; may be {@code null}
    *
    * @return {@code true} if the supplied {@link Object} is equal to
    * this {@link NameValue}; {@code false} otherwise
    *
    * @see AbstractValued#equals(Object)
    */
   @Override
   public boolean equals(final Object other) {
     if (other == this) {
       return true;
     } else if (other instanceof NameValue) {
       final NameValue him = (NameValue)other;
       if (this.isAtomic()) {
         if (!him.isAtomic()) {
           return false;
         }
       }
       return super.equals(other);
     } else {
       return false;
     }
   }
 
 
   /*
    * Static methods.
    */
 
 
   /**
    * A convenience method for use in <a
    * href="http://mvel.codehaus.org/">MVEL</a> scripts that returns
    * the return value of the {@link #valueOf(String)} method.  This
    * method's name is shorter and hence makes such scripts slightly
    * easier to read.
    *
    * <p>This method never returns {@code null}.</p>
    *
    * @param value the value for which a {@link NameValue} should be
    * returned; must not be {@code null}
    *
    * @return a non-{@code null} {@link NameValue} whose {@link
    * AbstractValued#getValue()} method will return a {@link String}
    * equal to the supplied {@link String}
    *
    * @exception IllegalArgumentException if {@code value} is {@code
    * null}
    *
    * @see #valueOf(String)
    */
   public static final NameValue nv(final String value) {
     return valueOf(value);
   }
 
   /**
    * Returns a non-{@code null} {@link NameValue} whose {@link
    * AbstractValued#getValue()} method is guaranteed to return a
    * non-{@code null} {@link String} that is {@linkplain
    * String#equals(Object) equal to} the supplied {@code value}.
    *
    * <p>This method never returns {@code null}.</p>
    *
    * @param value the value for which a {@link NameValue} should be
    * returned; must not be {@code null}
    *
    * @return a non-{@code null} {@link NameValue} whose {@link
    * AbstractValued#getValue()} method will return a {@link String}
    * equal to the supplied {@link String}
    *
    * @exception IllegalArgumentException if {@code value} is {@code
    * null}
    */
   public static final NameValue valueOf(final String value) {
     if (value == null) {
       throw new IllegalArgumentException("value", new NullPointerException("value"));
     }
     NameValue nv = null;
     // Because the cache is a LinkedHashMap in access order, get()
     // calls are structural modifications.  Therefore we need a write
     // lock.
     cacheLock.writeLock().lock();
     try {
       nv = cache.get(value);
       if (nv == null) {
         nv = new NameValue(value);
         cache.put(value, nv);
       }
     } finally {
       cacheLock.writeLock().unlock();
     }
     return nv;
   }
 
   /**
    * Returns {@code true} if the supplied {@link String} {@code value}
    * indexes a {@link NameValue} currently contained by this {@link
    * NameValue}'s internal cache of such {@link NameValue}s.
    *
    * @param value the value to check; must not be {@code null}
    *
    * @return {@code true} if a non-{@code null} {@link NameValue}
    * exists in this {@link NameValue}'s internal cache that
    * corresponds to the supplied {@code value}; {@code false}
    * otherwise
    *
    * @exception IllegalArgumentException if {@code value} is {@code
    * null}
    */
   static final boolean isCached(final String value) {
     if (value == null) {
       throw new IllegalArgumentException("value", new NullPointerException("value"));
     }
     try {
       cacheLock.readLock().lock();
       return cache.containsKey(value);
     } finally {
       cacheLock.readLock().unlock();
     }
   }
 
 }
