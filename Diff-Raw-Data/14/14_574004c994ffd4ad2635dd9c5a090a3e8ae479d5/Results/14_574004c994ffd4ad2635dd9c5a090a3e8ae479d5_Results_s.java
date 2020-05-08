 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.database;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.List;
 
 public class Results {
     private final String query;
     private final int rows;
    private List<DataList<DataField>> array;
 
     public Results(String query, ResultSet rs) throws SQLException {
         this.query = query;
 
         ResultSetMetaData metaData = rs.getMetaData();
         this.rows = metaData.getColumnCount();
         while (rs.next()) {
            DataList<DataField> data = new DataList<DataField>();
             for (int i = 1; i <= this.rows; i++) {
                 data.add(new DataField(i, metaData, rs.getObject(i)));
             }
             this.array.add(data);
         }
     }
 
     public int getRowsCount() {
         return this.rows;
     }
 
     public String getQuery() {
         return this.query;
     }
 
    public List<DataList<DataField>> getArray() {
         return this.array;
     }
 
    public DataList<DataField> getFirstResult() {
         if (this.array.size() > 0) {
             return this.array.get(0);
         } else {
             return null;
         }
     }
 
    public DataList<DataField> getLastResult() {
         if (this.array.size() > 0) {
             return this.array.get(this.array.size() - 1);
         } else {
             return null;
         }
     }
 }
