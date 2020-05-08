 package com.seitenbau.testing.dbunit.internal;
 
 import org.dbunit.dataset.DataSetException;
 import org.dbunit.dataset.ITable;
 import org.dbunit.dataset.ITableMetaData;
 
 /**
 * Helper class to decorate a Table. This Class just delegates all
  * {@link ITable} Methods to the delegate Target.
  */
 public class TableDecorator implements ITable
 {
   protected ITable fDelegated;
 
   public TableDecorator(ITable delegateTo)
   {
     fDelegated = delegateTo;
   }
 
   /**
    * {@inheritDoc}
    */
   public ITableMetaData getTableMetaData()
   {
     return fDelegated.getTableMetaData();
   }
 
   /**
    * {@inheritDoc}
    */
   public int getRowCount()
   {
     return fDelegated.getRowCount();
   }
 
   /**
    * {@inheritDoc}
    */
   public Object getValue(int row, String column) throws DataSetException
   {
     return fDelegated.getValue(row, column);
   }
 
   /**
    * Getter
    * @return the delegate Table
    */
   protected ITable getDelegate()
   {
     return fDelegated;
   }
 
 }
