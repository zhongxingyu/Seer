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
 *     William Chesters <williamc At paneris.org>
  *     http://paneris.org/~williamc
  *     Obrechtstraat 114, 2517VX Den Haag, The Netherlands
  */
 
 package org.melati.poem;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import org.melati.util.MelatiLocale;
 import org.melati.util.StringUtils;
 
 /**
  * A Timestamp.
  */
 public class TimestampPoemType extends AtomPoemType {
 
   /** Simple date format. */
   public static final DateFormat format =
       new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 
  /**
   * Constructor.
   * @param nullable whether nullable
   */
   public TimestampPoemType(boolean nullable) {
     super(Types.TIMESTAMP, "TIMESTAMP", nullable);
   }
  
  /**
  * Constructor.
   * @see org.melati.poem.dbms.SQLServer
   */
   public TimestampPoemType(int sqlTypeCode, String sqlTypeName, boolean nullable) {
     super(sqlTypeCode, sqlTypeName, nullable);
   }
 
   protected void _assertValidRaw(Object raw) {
     if (raw != null && !(raw instanceof Timestamp))
       throw new TypeMismatchPoemException(raw, this);
   }
 
   protected Object _getRaw(ResultSet rs, int col) throws SQLException {
     return rs.getTimestamp(col);
   }
 
   protected void _setRaw(PreparedStatement ps, int col, Object raw)
       throws SQLException {
     ps.setTimestamp(col, (Timestamp)raw);
   }
 
   protected String _stringOfRaw(Object raw) {
     return format.format((java.util.Date)raw);
   }
 
   protected Object _rawOfString(String raw) {
     try {
       return new Timestamp(format.parse(raw).getTime());
     }
     catch (ParseException e) {
       throw new ParsingPoemException(this, raw, e);
     }
   }
 
   protected String _stringOfCooked(Object cooked,
                                    MelatiLocale locale, int style) {
     return locale.timestampFormat(style).format((Timestamp)cooked);
   }
 
   protected boolean _canRepresent(SQLPoemType other) {
     return other instanceof TimestampPoemType;
   }
 
  /**
   * {@inheritDoc}
   * @see org.melati.poem.PoemType#toDsdType()
    */
   public String toDsdType() {
     return "Timestamp";
   }
 
   protected void _saveColumnInfo(ColumnInfo columnInfo)
       throws AccessPoemException {
     columnInfo.setTypefactory(PoemTypeFactory.TIMESTAMP);
   }
 
   protected String _quotedRaw(Object raw) {
     return StringUtils.quoted(_stringOfRaw(raw), '\'');
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.SQLType#sqlDefaultValue()
    */
   public String sqlDefaultValue() {
    return format.format(new Timestamp(System.currentTimeMillis()));
   }
 
 }
