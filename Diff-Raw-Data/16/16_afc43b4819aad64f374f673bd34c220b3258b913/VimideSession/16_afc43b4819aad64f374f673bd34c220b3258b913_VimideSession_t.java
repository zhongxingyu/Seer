 /**
  * Copyright (c) 2012 keyhom.c@gmail.com.
  *
  * This software is provided 'as-is', without any express or implied warranty.
  * In no event will the authors be held liable for any damages arising from
  * the use of this software.
  *
  * Permission is granted to anyone to use this software for any purpose
  * excluding commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  *
  *     1. The origin of this software must not be misrepresented; you must not
  *     claim that you wrote the original software. If you use this software
  *     in a product, an acknowledgment in the product documentation would be
  *     appreciated but is not required.
  *
  *     2. Altered source versions must be plainly marked as such, and must not
  *     be misrepresented as being the original software.
  *
  *     3. This notice may not be removed or altered from any source
  *     distribution.
  */
 package org.vimide.core.server;
 
 import java.io.Serializable;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.apache.mina.core.session.IoSession;
 
 import com.google.common.base.Strings;
 
 /**
 * Generic implementation of Vimide's server session.
  * 
  * @author keyhom (keyhom.c@gmail.com)
  */
 public class VimideSession implements Serializable {
 
     private static final long serialVersionUID = -4075775407020227073L;
     private IoSession session;
 
     /**
      * Creates an new VimideSession instance.
      */
     protected VimideSession(IoSession session) {
         this.session = session;
     }
 
     /**
      * Checks if the referenced key existing.
      * 
      * @param key the referenced key.
      * @return true if the key existing, false otherwise.
      */
     public boolean contains(Object key) {
         if (null != key)
             return getIoSession().containsAttribute(key);
         return false;
     }
 
     /**
      * Checks if the referenced key existing and the value is same.
      * 
      * @param key the referenced key.
      * @param value the save object.
      * @return true if the key existing the valid value, false otherwise.
      */
     public boolean contains(Object key, Object value) {
         final Object o = get(key);
         if (o == value || (null != o && o.equals(value))
                 || (null != value && value.equals(o)))
             return true;
         return false;
     }
 
     /**
      * Gets the value referenced by the specified key.
      * 
      * @param key the referenced key.
      * @return the value.
      */
     public Object get(Object key) {
         return get(key, null);
     }
 
     /**
      * Gets the value referenced by the specified key and if doesn't contains
      * the value, then will be returned with the specified
      * <code>defaultValue</code>.
      * 
      * @param key the referenced key.
      * @param defaultValue the default value if non exists.
      * @return the value or the default value.
      */
     public Object get(Object key, Object defaultValue) {
         if (null == key
                 || (key instanceof String && Strings
                         .isNullOrEmpty((String) key)))
             throw new IllegalArgumentException(
                     "Illegal attribute key of VimideSession for getting.");
         return getIoSession().getAttribute(key, defaultValue);
     }
 
     /**
      * Gets the value referenced by the specified key and cast the value with
      * the specified class if not null.
      * 
      * @param key the referenced key.
      * @param clazz the casting class.
      * @return the object which cast as the class.
      */
     @SuppressWarnings("unchecked")
     public <T extends Object> Object get(Object key, Class<T> clazz) {
         final Object value = get(key, null);
         if (null != value) {
             return (T) value;
         }
         return null;
     }
 
     /**
      * Sets the object referenced by the specified key.
      * 
      * @param key the referenced key.
      * @param value the saving object.
      * @return the origin object if exists.
      */
     public Object set(Object key, Object value) {
         if (null == key
                 || (key instanceof String && Strings
                         .isNullOrEmpty((String) key)))
             throw new IllegalArgumentException(
                     "Illegal attribute key of VimideSession for setting.");
         return getIoSession().setAttribute(key, value);
     }
 
     /**
      * Writes the data object to the end-point of the session.
      * 
      * @param obj the data object.
      * @throws Exception
      */
     public void write(Object obj) throws Exception {
         getIoSession().write(obj);
     }
 
     /**
      * Gets the I/O session instance.
      * 
      * @return the i/o session instance.
      */
     protected final IoSession getIoSession() {
         return session;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return new HashCodeBuilder(1, 31).append(session).toHashCode();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (!(obj instanceof VimideSession))
             return false;
 
         VimideSession other = (VimideSession) obj;
         return new EqualsBuilder().append(session, other.session).isEquals();
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see java.lang.Object#toString()
      */
     @Override
     public String toString() {
         return "VimideSession [session=" + session + "]";
     }
 }
