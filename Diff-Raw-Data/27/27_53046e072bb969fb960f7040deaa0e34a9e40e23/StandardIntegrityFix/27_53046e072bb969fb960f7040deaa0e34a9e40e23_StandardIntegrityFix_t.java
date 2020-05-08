 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 William Chesters
  *
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     William Chesters <williamc@paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem;
 
 import java.util.*;
 import org.melati.util.*;
 
 public abstract class StandardIntegrityFix implements IntegrityFix {
 
   public final Integer index;
   public final String name;
 
   /* private -- makes Sun compiler barf ... */ StandardIntegrityFix(int index, String name) {
     this.index = new Integer(index);
     this.name = name;
   }
 
   public static final StandardIntegrityFix delete, clear, prevent;
 
   private static final StandardIntegrityFix[] fixes = { 
     delete = new StandardIntegrityFix(0, "delete") {
       public Enumeration referencesTo(Persistent referee, Column column,
                                       Enumeration refs,
                                       Map referenceFixOfColumn) {
        while (refs.hasMoreElements()) {
          try {
            ((Persistent)refs.nextElement()).delete(referenceFixOfColumn);
          }
          catch (RowDisappearedPoemException e) {
            // This is possible if the table has a (currently non-standard)
            // integrity fix that deletes subsequent rows. ResultSetEnumeration
            // allows us to carry on in that case.
            if (! (refs instanceof ResultSetEnumeration)) {
              throw e;
            }
          }
        }
         return EmptyEnumeration.it;
       }
     },
     clear = new StandardIntegrityFix(1, "clear") {
       public Enumeration referencesTo(Persistent referrer, Column column,
                                       Enumeration refs,
                                       Map referenceFixOfColumn) {
         while (refs.hasMoreElements())
           column.setRaw((Persistent)refs.nextElement(), null);
         return EmptyEnumeration.it;
       }
     },
     prevent = new StandardIntegrityFix(2, "prevent") {
       public Enumeration referencesTo(Persistent referrer, Column column,
                                       Enumeration refs,
                                       Map referenceFixOfColumn) {
         return refs;
       }
     }
   };
 
   public static StandardIntegrityFix forIndex(int i) {
     return fixes[i];
   }
 
   public static int count() {
     return fixes.length;
   }
 
   private static final Hashtable fixOfName = new Hashtable();
 
   static {
     for (int i = 0; i < fixes.length; ++i)
       fixOfName.put(fixes[i].name, fixes[i]);
   }
 
   public static class NameUnrecognisedException extends PoemException {
     public String name;
 
     public NameUnrecognisedException(String name) {
       this.name = name;
     }
 
     public String getMessage() {
       return "No integrity fix found which goes by the name `" + name + "'";
     }
   }
 
   public static StandardIntegrityFix named(String name) {
     StandardIntegrityFix it = (StandardIntegrityFix)fixOfName.get(name);
     if (it == null)
       throw new NameUnrecognisedException(name);
     return it;
   }
 }
