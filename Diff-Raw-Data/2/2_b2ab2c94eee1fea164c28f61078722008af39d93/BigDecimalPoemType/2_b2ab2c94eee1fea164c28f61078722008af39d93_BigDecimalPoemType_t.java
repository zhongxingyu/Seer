 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2003 Samuel Goldstein
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
  *     Samuel Goldstein <samuel@1969.ws>
  *     http://www.1969.ws
  *     13101 W. Washington Blvd Suite 248, Los Angeles, CA 90066 USA
  */
 
 package org.melati.poem;
 
 import java.sql.Types;
 import java.sql.ResultSet;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.math.BigDecimal;
 import org.melati.poem.dbms.Dbms;
 
 /**
  * Java "BigDecimal", dependant upon the database/SQL implementation.
  *
  * By default, this is a 22 digit number, with 2 digits after the
  * decimal.
  * */
 public class BigDecimalPoemType extends FixedPointAtomPoemType {
 
   public BigDecimalPoemType(boolean nullable) {
     super(Types.DECIMAL, "Big Decimal", nullable, 22, 2);
   }
 
   public BigDecimalPoemType(boolean nullable, int precision, int scale) {
     super(Types.DECIMAL, "Big Decimal", nullable, precision, scale);
     // because a newly added column of this type won't have valid 
     // numbers, we fix that here...
     if (scale < 0) {
       setScale(2);
     }
     if (precision <= 0) {
       setPrecision(22);
     }
   }
 
   protected void _assertValidRaw(Object raw) {
     if (raw != null && !(raw instanceof BigDecimal))
       throw new TypeMismatchPoemException(raw, this);
   }
 
   protected Object _getRaw(ResultSet rs, int col) throws SQLException {
     synchronized (rs) {
       BigDecimal x = rs.getBigDecimal(col);
       return rs.wasNull() ? null : x;
     }
   }
 
   protected void _setRaw(PreparedStatement ps, int col, Object real)
     throws SQLException {
     ps.setBigDecimal(col, ((BigDecimal) real));
   }
 
   protected Object _rawOfString(String rawString) throws ParsingPoemException {
     try {
       return new BigDecimal(rawString);
     } catch (NumberFormatException e) {
       throw new ParsingPoemException(this, rawString, e);
     }
   }
 
   protected String _sqlDefinition(Dbms dbms) {
     try {
       return dbms.getFixedPtSqlDefinition(getScale(), getPrecision());
     } catch (SQLException e) {
       throw new SQLSeriousPoemException(e);
     }
   }
 
   /**
    * Whilst BigDecimal cannot represent all Doubles it can represent 
    * legacy money doubles, so we allow it to enable upgrades from Doubles 
    * to BigDecimals.
   * FIXME is this a potential gotcha? 
    * @see org.melati.poem.BasePoemType#_canRepresent(org.melati.poem.SQLPoemType)
    */
   protected boolean _canRepresent(SQLPoemType other) {
     return other instanceof BigDecimalPoemType || other instanceof DoublePoemType;
   }
 
   /**
    * The field type used in the Data Structure Definition language.
    */
   public String toDsdType() {
     return "BigDecimal";
   }
 
   protected void _saveColumnInfo(ColumnInfo columnInfo)
     throws AccessPoemException {
     columnInfo.setTypefactory(PoemTypeFactory.BIGDECIMAL);
     columnInfo.setPrecision(getPrecision());
     columnInfo.setScale(getScale());
   }
 }
