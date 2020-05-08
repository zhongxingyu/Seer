 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  *
  * $Id$
  */
 
 package no.feide.moria.authorization;
 
 
 /**
  * This class represents an LDAP attribute and is used for authorization of a
 * web service. Both {@link AuthorizationManager} and {@link AuthorizationClient} have lists of attributes.
  *
  * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
  * @version $Revision$
  */
 final class AuthorizationAttribute {
 
     /**
      * Cached hash code.
      */
     private volatile int hashCode = 0;
 
 
     /**
      * Name of attribute.
      */
     private String name = null;
 
     /**
      * Is this attribute allowed in use with SSO?
      */
     private boolean allowSSO = false;
 
     /**
      * Security level.
      */
     private int secLevel = 2;
 
     /**
      * Constructor. Name of attribute must be a non-empty string. Security
      * level must be > 0.
      *
      * @param name     Name of attribute.
      * @param allowSSO Allow use of SSO with this attribute.
      * @param secLevel The attribute's security level.
      */
     AuthorizationAttribute(final String name, final boolean allowSSO, final int secLevel) {
 
         if (name == null || name.equals("")) {
             throw new IllegalArgumentException("Name must be a non-empty string.");
         }
 
         if (secLevel < 0) {
             throw new IllegalArgumentException("SecLevel must be >= 0, was: " + secLevel);
         }
 
         this.secLevel = secLevel;
         this.allowSSO = allowSSO;
         this.name = name;
     }
 
     /**
      * Returns true if the supplied object is identical to this one.
      *
      * @param object The object to compare with.
      * @return false if any of the attributes are different from the supplied
      *         object.
      */
     public boolean equals(final Object object) {
         if (object == this) {
             return true;
         }
         if (object instanceof AuthorizationAttribute) {
             final AuthorizationAttribute attr = (AuthorizationAttribute) object;
             if (attr.getName().equals(name) && attr.getAllowSSO() == getAllowSSO() && attr.getSecLevel() == secLevel) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Generates a hashCode from the object's attributes. 'name', 'secLevel' 
      * and 'allowSSO' are used for the computation.
      *
      * @return The hashcode for this object.
      */
     public int hashCode() {
         if (hashCode == 0) {
             int result = 17;
             result = 37 * result + name.hashCode();
             result = 37 * result + secLevel;
             result = 37 * result + (allowSSO ? 0 : 1);
             hashCode = result;
         }
         return hashCode;
     }
 
     /**
      * Gets the security level of this attribute.
      *
      * @return Security level.
      */
     public int getSecLevel() {
         return secLevel;
     }
 
     /**
      * Gets the name of attribute.
      *
      * @return Name of the attribute.
      */
     public String getName() {
         return name;
     }
 
     /**
      * Returns true if the attribute is allowed in use with SSO.
      *
      * @return True if the attribute can be used with SSO, else false.
      */
     public boolean getAllowSSO() {
         return allowSSO;
     }
 
     /**
      * Returns a string representation of this object.
      *
      * @return The string representation of this object.
      */
     public String toString() {
         return ("Attribute name: " + name + " secLevel: " + secLevel + " allowSSO: " + allowSSO);
     }
 }
