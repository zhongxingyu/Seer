 package net.madz.db.core.meta.immutable.impl;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import net.madz.db.core.meta.DottedPath;
import net.madz.db.core.meta.DottedPathImpl;
 import net.madz.db.core.meta.immutable.ColumnMetaData;
 import net.madz.db.core.meta.immutable.ForeignKeyMetaData;
 import net.madz.db.core.meta.immutable.IndexMetaData;
 import net.madz.db.core.meta.immutable.SchemaMetaData;
 import net.madz.db.core.meta.immutable.TableMetaData;
 import net.madz.db.core.meta.immutable.types.TableType;
 
 public class TableMetaDataImpl<SMD extends SchemaMetaData<SMD, TMD, CMD, FMD, IMD>, TMD extends TableMetaData<SMD, TMD, CMD, FMD, IMD>, CMD extends ColumnMetaData<SMD, TMD, CMD, FMD, IMD>, FMD extends ForeignKeyMetaData<SMD, TMD, CMD, FMD, IMD>, IMD extends IndexMetaData<SMD, TMD, CMD, FMD, IMD>>
         implements TableMetaData<SMD, TMD, CMD, FMD, IMD> {
 
     protected final SMD schema;
     protected final DottedPath name; // catalog.schema.name
     protected final TableType type;
     protected final String remarks;
     protected final String idCol, idGeneration;
     protected final Map<String, CMD> columnMap = new TreeMap<String, CMD>(String.CASE_INSENSITIVE_ORDER);
     protected final List<CMD> orderedColumns = new LinkedList<CMD>();
     protected final Map<String, IMD> indexMap = new TreeMap<String, IMD>(String.CASE_INSENSITIVE_ORDER);
     protected final List<FMD> fkList = new LinkedList<FMD>();
     protected IMD primaryKey;
 
     public TableMetaDataImpl(SMD parent, final TMD tableMetaData) {
         this.schema = parent;
         this.name = tableMetaData.getTablePath();
         this.type = tableMetaData.getType();
         this.remarks = tableMetaData.getRemarks();
         this.idCol = tableMetaData.getIdCol();
         this.idGeneration = tableMetaData.getIdGeneration();
     }
 
     @Override
     public SMD getParent() {
         return this.schema;
     }
 
     @Override
     public DottedPath getTablePath() {
         return name;
     }
 
     @Override
     public String getTableName() {
         return name.getName();
     }
 
     @Override
     public IMD getPrimaryKey() {
         return this.primaryKey;
     }
 
     @Override
     public List<CMD> getColumns() {
         Collections.sort(this.orderedColumns, ColumnMetaData.ORDINAL_COMPARATOR);
         return this.orderedColumns;
     }
 
     @Override
     public CMD getColumn(String columnName) {
         return this.columnMap.get(columnName);
     }
 
     @Override
     public Collection<FMD> getForeignKeySet() {
         return this.fkList;
     }
 
     @Override
     public Collection<IMD> getIndexSet() {
         return this.indexMap.values();
     }
 
     @Override
     public IMD getIndex(String indexName) {
         return this.indexMap.get(indexName);
     }
 
     public String getCatalogName() {
         if ( name.size() > 2 ) {
             return this.name.getParent().getParent().getName();
         } else {
             return null;
         }
     }
 
     public String getSchemaName() {
         if ( name.size() > 1 ) {
             return this.name.getParent().getName();
         } else {
             return null;
         }
     }
 
     void addForeignKey(FMD fk) {
         this.fkList.add(fk);
     }
 
     @Override
     public TableType getType() {
         return this.type;
     }
 
     @Override
     public String getRemarks() {
         return this.remarks;
     }
 
     @Override
     public String getIdCol() {
         return this.idCol;
     }
 
     @Override
     public String getIdGeneration() {
         return this.idGeneration;
     }
 
     @Override
     public String toString() {
         return "TableMetaDataImpl [name=" + name + ", type=" + type + ", remarks=" + remarks + ", idCol=" + idCol + ", idGeneration=" + idGeneration
                 + ", columnMap=" + columnMap + ", orderedColumns=" + orderedColumns + ", indexMap=" + indexMap + ", fkList=" + fkList + ", primaryKey="
                 + primaryKey + "]";
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ( ( schema.getSchemaPath() == null ) ? 0 : schema.getSchemaPath().hashCode() );
         result = prime * result + ( ( name == null ) ? 0 : name.hashCode() );
         result = prime * result + ( ( idCol == null ) ? 0 : idCol.hashCode() );
         result = prime * result + ( ( idGeneration == null ) ? 0 : idGeneration.hashCode() );
         result = prime * result + ( ( remarks == null ) ? 0 : remarks.hashCode() );
         result = prime * result + ( ( type == null ) ? 0 : type.hashCode() );
         result = prime * result + ( ( primaryKey == null ) ? 0 : primaryKey.hashCode() );
         result = prime * result + ( ( orderedColumns == null ) ? 0 : orderedColumns.hashCode() );
         result = prime * result + ( ( indexMap == null ) ? 0 : indexMap.hashCode() );
         result = prime * result + ( ( fkList == null ) ? 0 : fkList.hashCode() );
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if ( this == obj ) return true;
         if ( obj == null ) return false;
         if ( getClass() != obj.getClass() ) return false;
         TableMetaDataImpl other = (TableMetaDataImpl) obj;
         if ( schema.getSchemaPath() == null ) {
             if ( other.schema.getSchemaPath() != null ) return false;
         } else if ( !schema.getSchemaPath().equals(other.schema.getSchemaPath()) ) return false;
         if ( name == null ) {
             if ( other.name != null ) return false;
         } else if ( !name.equals(other.name) ) return false;
         if ( idCol == null ) {
             if ( other.idCol != null ) return false;
         } else if ( !idCol.equals(other.idCol) ) return false;
         if ( idGeneration == null ) {
             if ( other.idGeneration != null ) return false;
         } else if ( !idGeneration.equals(other.idGeneration) ) return false;
         if ( remarks == null ) {
             if ( other.remarks != null ) return false;
         } else if ( !remarks.equals(other.remarks) ) return false;
         if ( type != other.type ) return false;
         if ( primaryKey == null ) {
             if ( other.primaryKey != null ) return false;
         } else if ( !primaryKey.equals(other.primaryKey) ) return false;
         if ( orderedColumns == null ) {
             if ( other.orderedColumns != null ) return false;
         } else if ( !orderedColumns.equals(other.orderedColumns) ) return false;
         if ( indexMap == null ) {
             if ( other.indexMap != null ) return false;
         } else if ( !indexMap.equals(other.indexMap) ) return false;
         if ( fkList == null ) {
             if ( other.fkList != null ) return false;
        } else if ( !fkList.equals(other.fkList) ) return false;
         return true;
     }
 }
