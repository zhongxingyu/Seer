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
 
 import java.sql.Types;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Enumeration;
 import java.util.NoSuchElementException;
 
 
 /**
  * A <tt>Boolean</tt> <tt>Nullable</tt> {@link Field} type.
  */
 public class BooleanPoemType extends AtomPoemType {
 
   public BooleanPoemType(boolean nullable) {
     super(Types.BIT, "BOOLEAN", nullable);
   }
 
   protected Enumeration _possibleRaws() {
     return new BooleanPossibleRawEnumeration();
   }
 
   protected void _assertValidRaw(Object raw) {
     if (raw != null && !(raw instanceof Boolean))
       throw new TypeMismatchPoemException(raw, this);
   }
 
   protected Object _getRaw(ResultSet rs, int col) throws SQLException {
     synchronized (rs) {
       boolean b = rs.getBoolean(col);
       return rs.wasNull() ? null : b ? Boolean.TRUE : Boolean.FALSE;
     }
   }
 
   protected void _setRaw(PreparedStatement ps, int col, Object bool)
       throws SQLException {
     ps.setBoolean(col, ((Boolean)bool).booleanValue());
   }
 
   protected Object _rawOfString(String rawString)
       throws ParsingPoemException {
     rawString = rawString.trim();
     if (rawString.length() == 1)
       switch (rawString.charAt(0)) {
         case 't': case 'T': case 'y': case 'Y': case '1':
           return Boolean.TRUE;
         case 'f': case 'F': case 'n': case 'N': case '0':
           return Boolean.FALSE;
         default:;
       }
 
     if (rawString.regionMatches(0, "true", 0, 4) ||
              rawString.regionMatches(0, "yes", 0, 3))
       return Boolean.TRUE;
     else if (rawString.regionMatches(0, "false", 0, 5) ||
              rawString.regionMatches(0, "no", 0, 2))
       return Boolean.FALSE;
     else
       throw new ParsingPoemException(this, rawString);
   }
 
   protected boolean _canRepresent(SQLPoemType other) {
     return other instanceof BooleanPoemType;
   }
 
   /*
    * The field type used in the Data Structure Definition language.
    */
   public String toDsdType() {
     return "Boolean";
   }
 
   protected void _saveColumnInfo(ColumnInfo columnInfo)
       throws AccessPoemException {
     columnInfo.setTypefactory(PoemTypeFactory.BOOLEAN);
   }
 
 }
 
 /**
  * The possible raw values of a {@link BooleanPoemType}.
 * 
 * @todo If this works rewrite so it doesn't look as though it has a bug.
  */
 class BooleanPossibleRawEnumeration implements Enumeration {
   private int state = 0;
 
   public boolean hasMoreElements() {
     return state < 2;
   }
 
   public synchronized Object nextElement() {
     if (state == 2)
       throw new NoSuchElementException();
     else //FIXME surely never returns FALSE ?
       return state++ == 0 ? Boolean.FALSE : Boolean.TRUE;
   }
 }
