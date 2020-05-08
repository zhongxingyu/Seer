 /*
  * Copyright 2002 - 2013 Pentaho Corporation.  All rights reserved.
  * 
  * This software was developed by Pentaho Corporation and is provided under the terms
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use
  * this file except in compliance with the license. If you need a copy of the license,
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. TThe Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
  * the license for the specific language governing your rights and limitations.
  */
 
 package org.pentaho.commons.connection;
 
 public abstract class AbstractPentahoMetaData implements IPentahoMetaData {
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getColumnIndex(java.lang.String)
    */
   public int getColumnIndex( String value ) {
     Object[][] columnHeaders = getColumnHeaders();
     if ( columnHeaders == null || columnHeaders.length != 1 ) {
       return -1;
     }
     for ( int idx = 0; idx < columnHeaders[0].length; idx++ ) {
       if ( columnHeaders[0][idx].toString().equalsIgnoreCase( value ) ) {
         return idx;
       }
     }
     return -1;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getColumnIndex(java.lang.String[])
    */
   public int getColumnIndex( String[] values ) {
     Object[][] columnHeaders = getColumnHeaders();
     if ( columnHeaders == null || columnHeaders.length != values.length ) {
       return -1;
     }
     for ( int columnIdx = 0; columnIdx < columnHeaders[0].length; columnIdx++ ) {
       boolean match = true;
       for ( int rowIdx = 0; rowIdx < columnHeaders.length && match; rowIdx++ ) {
         match = columnHeaders[rowIdx][columnIdx].toString().equalsIgnoreCase( values[rowIdx] );
       }
       if ( match ) {
         return columnIdx;
       }
     }
     return -1;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getRowIndex(java.lang.String)
    */
   public int getRowIndex( String value ) {
     Object[][] rowHeaders = getRowHeaders();
     if ( rowHeaders == null || rowHeaders[0].length != 1 ) {
       return -1;
     }
     for ( int rowIdx = 0; rowIdx < rowHeaders.length; rowIdx++ ) {
       if ( rowHeaders[rowIdx][0].toString().equalsIgnoreCase( value ) ) {
         return rowIdx;
       }
     }
     return -1;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getRowIndex(java.lang.String[])
    */
   public int getRowIndex( String[] values ) {
     Object[][] rowHeaders = getRowHeaders();
     if ( rowHeaders == null || rowHeaders[0].length != values.length ) {
       return -1;
     }
     for ( int rowIdx = 0; rowIdx < rowHeaders.length; rowIdx++ ) {
       boolean match = true;
       for ( int columnIdx = 0; columnIdx < rowHeaders[rowIdx].length && match; columnIdx++ ) {
         match = rowHeaders[rowIdx][columnIdx].toString().equalsIgnoreCase( values[columnIdx] );
       }
       if ( match ) {
         return rowIdx;
       }
     }
     return -1;
   }
 
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getColumnHeaders()
    */
   public abstract Object[][] getColumnHeaders();
 
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getRowHeaders()
    */
   public abstract Object[][] getRowHeaders();
 
   /*
    * (non-Javadoc)
    * 
    * @see org.pentaho.connection.IPentahoMetaData#getColumnCount()
    */
   public int getColumnCount() {
     Object[][] columnHeaders = getColumnHeaders();
     if ( columnHeaders == null || columnHeaders.length <= 0 ) {
       return 0;
     }
     return columnHeaders[0].length;
   }
 
   public Object getAttribute( int rowNo, int columnNo, String attributeName ) {
     throw new UnsupportedOperationException();
   }
 }
