 /*****************************************************************************
  * Copyright (C) 2008 EnterpriseDB Corporation.
  * Copyright (C) 2011 Stado Global Development Group.
  *
  * This file is part of Stado.
  *
  * Stado is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Stado is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Stado.  If not, see <http://www.gnu.org/licenses/>.
  *
  * You can find Stado at http://www.stado.us
  *
  ****************************************************************************/
 package org.postgresql.stado.metadata;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Vector;
 
 import org.postgresql.stado.engine.XDBSessionContext;
 import org.postgresql.stado.exception.ErrorMessageRepository;
 import org.postgresql.stado.exception.XDBGeneratorException;
 import org.postgresql.stado.exception.XDBServerException;
 import org.postgresql.stado.optimizer.SqlExpression;
 import org.postgresql.stado.parser.ExpressionType;
 import org.postgresql.stado.parser.Parser;
 import org.postgresql.stado.parser.SqlCreateTableColumn;
 import org.postgresql.stado.parser.SqlSelect;
 import org.postgresql.stado.parser.core.ParseException;
 
 
 //redo these as properties...
 //-----------------------------------------
 public class SysColumn {
     private SysTable table;
 
     private int colid;
 
     private int colseq;
 
     private String colname;
 
     private int coltype;
 
     private int collength;
 
     private int colscale;
 
     private int colprecision;
 
     private boolean isnullable;
 
     private boolean isserial;
 
     private String nativecoldef;
 
     private int indextype; // not part of xsyscolumns, but convenient here
 
     private double selectivity;
 
     private String defaultExprStr = "";
 
     private SqlExpression defaultExpr;
 
     public boolean isWithTimeZone;
 
     /**
      * Tracks which indexes use this column
      */
     private Vector<SysIndex> indexUsage;
 
     /**
      * The best index to use if just accessing based on this column
      */
     public SysIndex bestIndex;
 
     public int bestIndexColPos;
 
     public int bestIndexRowsPerPage;
 
     // Constructor
     public SysColumn(SysTable table, int colID, int colSeq, String colName,
             int colType, int colLength, int colScale, int colPrecision,
             boolean isNullable, boolean isSerial, String nativeColDef,
             double selectivity, String defaultExpr) {
         this.table = table;
         colid = colID;
         colseq = colSeq;
         colname = colName;
         coltype = colType;
         collength = colLength;
         colscale = colScale;
         colprecision = colPrecision;
         isnullable = isNullable;
         isserial = isSerial;
         nativecoldef = nativeColDef;
         indextype = MetaData.INDEX_TYPE_NONE;
         this.selectivity = selectivity;
         this.defaultExprStr = defaultExpr;
     }
 
     /*
      * TODO Should we execute statements against database or get the database in
      * the memory and then execute the statements. Presntly I am going to get to
      * the query the xSysForegintable
      */
 
     public Enumeration<Integer> getChildColumns() {
         Vector<Integer> childcols = new Vector<Integer>();
         String sqlstmt = "select colid from xsysforeignkeys where refcolid = '"
             + colid + " '";
 
         try {
             ResultSet result = MetaData.getMetaData().executeQuery(sqlstmt);
             while (result.next()) {
                 int colidentifier = result.getInt("colid");
                 childcols.add(new Integer(colidentifier));
             }
             return childcols.elements();
         } catch (SQLException e) {
             throw new XDBServerException(
                     e.getMessage() + "\nQUERY: " + sqlstmt, e,
                     ErrorMessageRepository.SQL_EXEC_FAILURE_CODE);
         }
     }
 
     @Override
     public int hashCode() {
         return table == null ? colname.hashCode() : table.hashCode()
                 + colname.hashCode();
     }
 
     @Override
     public boolean equals(Object other) {
         if (other == this) {
             return true;
         }
         if (other instanceof SysColumn) {
             SysColumn col = (SysColumn) other;
             return col.table == this.table && col.colname.equals(this.colname);
         } else {
             return false;
         }
     }
 
     /*
      * This function will fire up query the database allowing it to get
      * information on its selectivity
      */
     public void setSelectivity(double selectivity) {
         this.selectivity = selectivity;
     }
 
     /**
      * Returns the column length in bytes
      */
     public int getColumnLength() {
         int colLength;
 
         switch (this.coltype) {
         case java.sql.Types.BIT:
             colLength = 1;
             break;
 
         case java.sql.Types.CHAR:
             colLength = this.collength;
             break;
 
         case java.sql.Types.VARCHAR:
            colLength = this.collength / 3;
             break;
 
         case java.sql.Types.SMALLINT:
             colLength = 2;
             break;
 
         case java.sql.Types.INTEGER:
             colLength = 4;
             break;
 
         case java.sql.Types.DECIMAL:
             colLength = 4; // should be ok?
             break;
 
         case java.sql.Types.NUMERIC:
             colLength = 4; // should be ok?
             break;
 
         case java.sql.Types.REAL:
             colLength = 8;
             break;
 
         case java.sql.Types.FLOAT:
             colLength = 8;
             break;
 
         case java.sql.Types.DATE:
             colLength = 2;
             break;
 
         case java.sql.Types.TIME:
             colLength = 2;
             break;
 
         case java.sql.Types.TIMESTAMP:
             colLength = 4;
             break;
         case java.sql.Types.DOUBLE:
             colLength = 8;
             break;
 
         case java.sql.Types.BOOLEAN:
             colLength = 1;
             break;
 
         default:
             colLength = 0;
         // throw new XDBServerException
         // (ErrorMessageRepository.INVALID_DATATYPE + "(" + type + " )"
         // ,0,ErrorMessageRepository.INVALID_DATATYPE_CODE);
         }
 
         return colLength;
     }
 
     /**
      * Note that this column appears in an index
      *
      * SysIndex - the index it appears in columnPos - the ordinal position of
      * the column within the index
      */
     public void addIndexUsage(SysIndex aSysIndex, int columnPos) {
         // Also track the "best index" to use when accessing via
         // this column
 
         if (indexUsage == null
                 || columnPos < bestIndexColPos
                 || (columnPos == bestIndexColPos && aSysIndex.estRowsPerPage < bestIndexRowsPerPage)) {
             bestIndex = aSysIndex;
             bestIndexColPos = columnPos;
             bestIndexRowsPerPage = aSysIndex.estRowsPerPage;
         }
 
         if (indexUsage == null) {
             indexUsage = new Vector<SysIndex>();
         }
 
         indexUsage.add(aSysIndex);
     }
 
     /**
      * This function will return the default value For present we use the native
      * column definition later on we will use a modified system generated column
      * definition.
      *
      * @return the native column definition
      */
     public String getColumnDefinition() {
         return nativecoldef;
     }
 
     /**
      * This function is supposed to give back information on if it has a default
      * value set
      */
     public boolean hasDefault() {
         if (defaultExpr == null || defaultExpr.equals("null")) {
             return false;
         } else {
             return true;
         }
     }
 
     public SysTable getSysTable() {
         return table;
     }
 
     /**
      * @return Returns the colid.
      */
     public int getColID() {
         return colid;
     }
 
     /**
      * @return Returns the colseq.
      */
     public int getColSeq() {
         return colseq;
     }
 
     /**
      * @return Returns the colname.
      */
     public String getColName() {
         return colname;
     }
 
     /**
      * @return Returns the coltype.
      */
     public int getColType() {
         return coltype;
     }
 
     /**
      * @return Returns the collength.
      */
     public int getColLength() {
         // It is expected that column length for Numeric equals to precision
         return collength == 0 ? colprecision : collength;
     }
 
     /**
      * @return Returns the colscale.
      */
     public int getColScale() {
         return colscale;
     }
 
     /**
      * @return Returns the colprecision.
      */
     public int getColPrecision() {
         return colprecision;
     }
 
     /**
      * @return Returns the isnullable.
      */
     public boolean isNullable() {
         return isnullable;
     }
 
     /**
      * @return Returns the isserial.
      */
     public boolean isSerial() {
         return isserial;
     }
 
     /**
      * @return Returns the nativecoldef.
      */
     public String getNativeColDef() {
         return nativecoldef;
     }
 
     /**
      * @param indextype
      *            The indextype to set.
      */
     public void setIndexType(int indextype) {
         this.indextype = indextype;
     }
 
     /**
      * @return Returns the indextype.
      */
     public int getIndexType() {
         return indextype;
     }
 
     /**
      * @return Returns the selectivity.
      */
     public double getSelectivity() {
         return selectivity;
     }
 
     /**
      * @return Returns the defaultExpr.
      */
     public String getDefaultExpr() {
         return defaultExprStr;
     }
 
     /**
      * Return default expression for the column as an SqlExpression
      * @param client
      * @return
      * @throws XDBGeneratorException
      */
     public SqlExpression getDefaultExpr(XDBSessionContext client)
             throws XDBGeneratorException {
         if (isserial) {
             return new SqlExpression(""
                     + table.getSerialHandler().allocateRange(1, client),
                     new ExpressionType(this));
         } else if (SqlCreateTableColumn.XROWID_NAME.equals(colname)) {
             return new SqlExpression(""
                     + table.getRowIDHandler().allocateRange(1, client),
                     new ExpressionType(this));
         } else if (defaultExpr == null) {
             if (defaultExprStr == null || defaultExprStr.trim().length() == 0) {
                 return null;
             }
             Parser parser = new Parser(client);
             try {
                 parser.parseStatement("SELECT " + defaultExprStr);
                 SqlSelect select = (SqlSelect) parser.getSqlObject();
                 List<SqlExpression> projList = select.aQueryTree.getProjectionList();
                 if (projList.size() == 1) {
                     defaultExpr = projList.get(0);
                     defaultExpr.setExprDataType(new ExpressionType(this));
                 }
             } catch (ParseException ex) {
                 throw new XDBServerException(
                         "Failed to parse default expression", ex);
             }
         }
         return defaultExpr;
     }
 }
