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
 
 import org.melati.poem.generated.CapabilityBase;
import org.melati.poem.PoemThread;
 
 /**
  * The quality of being able to perform an action.
  * Melati POEM generated, programmer modifiable stub 
  * for a <code>Persistent</code> <code>Capability</code> object.
  * 
  * <p> 
  * Description: 
  *   A capability which users can be required to possess before accessing 
  *   data. 
  * </p>
  * 
  * <table> 
  * <tr><th colspan='3'>
  * Field summary for SQL table <code>Capability</code>
  * </th></tr>
  * <tr><th>Name</th><th>Type</th><th>Description</th></tr>
  * <tr><td> id </td><td> Integer </td><td> The Table Row Object ID </td></tr> 
  * <tr><td> name </td><td> String </td><td> A human-readable name for the 
  * capability </td></tr> 
  * </table> 
  * 
  * @generator org.melati.poem.prepro.TableDef#generateMainJava 
  */
 public class Capability extends CapabilityBase {
 
  /**
   * Constructor 
   * for a <code>Persistent</code> <code>Capability</code> object.
   * <p>
   * Description: 
   *   A capability which users can be required to possess before accessing 
   *   data. 
   * </p>
   * 
   * @generator org.melati.poem.prepro.TableDef#generateMainJava 
   */
   public Capability() {
   }
 
   // programmer's domain-specific code here
 
  /**
   * Constructor 
   * for a <code>Persistent</code> <code>Capability</code> object.
   * <p>
   * Description: 
   *   A capability which users can be required to possess before accessing 
   *   data. 
   * </p>
   * 
   * @param name the name of a <code>Capability</code> to create.
   */
   public Capability(String name) {
     setName_unsafe(name);
   }
 
  /**
   * Allow this object to be read by anyone.
   * 
   * @param token any {@link AccessToken}
   */
   public void assertCanRead(AccessToken token) {}
 
  /**
  * Return the capability name.
  * <p>
  * Not sure what the requirements are here but it is used
  * in exception reporting and in that case we want
  * what information we can get. So a read lock is not
  * required if we are not in a session. Question is, do
  * we want a read lock if we are in a session? That is
  * the way it has been and it probably does not matter
  * so we get one for backward compatibility.
  *
   * @return the name of this <code>Capability</code>
   */
   public String toString() {
    if (PoemThread.inSession()) {
      return getName();
    } else {
      return getName_unsafe();
    }
   }
 }
