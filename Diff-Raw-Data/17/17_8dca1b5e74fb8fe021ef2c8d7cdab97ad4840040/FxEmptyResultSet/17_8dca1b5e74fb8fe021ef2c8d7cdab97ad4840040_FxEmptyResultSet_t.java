 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.shared.search;
 
 import com.flexive.shared.content.FxPK;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * An empty FxResultSet to be used when an exception is thrown but a result set is still needed
  * to render a user interface.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class FxEmptyResultSet implements FxResultSet, Serializable {
 
     private static final long serialVersionUID = 6935555064499770563L;
     public final static FxPK EMPTY_PK = FxPK.createNewPK();
     private long creationTime;
 
     /**
      * {@inheritDoc}
      */
     public FxEmptyResultSet() {
         creationTime = System.currentTimeMillis();
     }
 
     /**
      * {@inheritDoc}
      */
     public long getCreationTime() {
         return creationTime;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getStartIndex() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getMaxFetchRows() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getColumnNames() {
         return new String[0];
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Object[]> getRows() {
         return new ArrayList<Object[]>(0);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getColumnLabel(int index) throws ArrayIndexOutOfBoundsException {
         return "";
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getColumnLabels() {
         return new String[0];
     }
 
     /**
      * {@inheritDoc}
      */
     public String getColumnName(int pos) throws ArrayIndexOutOfBoundsException {
         return "";
     }
 
     /**
      * {@inheritDoc}
      */
     public int getColumnIndex(String name) {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public Map<String, Integer> getColumnIndexMap() {
         return new HashMap<String, Integer>(0);
     }
 
     /**
      * {@inheritDoc}
      */
     public int getColumnCount() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getRowCount() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getTotalRowCount() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isTruncated() {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public Object getObject(int rowIndex, int columnIndex) throws ArrayIndexOutOfBoundsException {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getString(int rowIndex, int columnIndex) throws ArrayIndexOutOfBoundsException {
         return "";
     }
 
     /**
      * {@inheritDoc}
      */
     public int getParserExecutionTime() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getDbSearchTime() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getFetchTime() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public int getTotalTime() {
         return 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public ResultLocation getLocation() {
         return AdminResultLocations.DEFAULT;
     }
 
     /**
      * {@inheritDoc}
      */
     public ResultViewType getViewType() {
         return ResultViewType.LIST;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxFoundType> getContentTypes() {
         return new ArrayList<FxFoundType>(0);
     }
 
     /**
      * Dummy interator
      */
     private class RowIterator implements Iterator<FxResultRow> {
         /**
          * {@inheritDoc}
          */
         public boolean hasNext() {
             return false;
         }
 
         /**
          * {@inheritDoc}
          */
         public FxResultRow next() {
             return new FxResultRow(FxEmptyResultSet.this, 0);
         }
 
         /**
          * {@inheritDoc}
          */
         public void remove() {
             throw new UnsupportedOperationException("Removing rows not supported");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Iterable<FxResultRow> getResultRows() {
         return new Iterable<FxResultRow>() {
             public Iterator<FxResultRow> iterator() {
                 return new RowIterator();
             }
         };
     }
 
     /**
      * {@inheritDoc}
      */
     public FxResultRow getResultRow(int index) {
         return new FxResultRow(this, index);
     }
 
     /**
      * {@inheritDoc}
      */
     public long getCreatedBriefcaseId() {
         return -1;
     }
 
     /**
      * {@inheritDoc}
      */
     public <T> List<T> collectColumn(int columnIndex) {
         return new ArrayList<T>(0);
     }
 
     /**
      * {@inheritDoc}
      */
     public int getUserWildcardIndex() {
         return -1;
     }
 }
