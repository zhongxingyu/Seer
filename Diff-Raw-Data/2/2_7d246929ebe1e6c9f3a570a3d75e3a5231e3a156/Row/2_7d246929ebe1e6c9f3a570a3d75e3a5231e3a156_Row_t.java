 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import;
 
 import com.treasure_data.bulk_import.prepare_parts.ExtStrftime;
 import com.treasure_data.bulk_import.prepare_parts.PreparePartsException;
 import com.treasure_data.bulk_import.writer.FileWriter;
 
 public class Row {
     private ColumnValue[] values;
     private boolean needAdditionalTimeColumn = false;
     private Row.TimeColumnValue timeColumnValue;
     private int timeColumnIndex = -1;
 
     public Row(ColumnValue[] values, Row.TimeColumnValue timeColumnValue) {
         this.values = values;
         needAdditionalTimeColumn =
                 timeColumnValue instanceof Row.AliasTimeColumnValue ||
                 timeColumnValue instanceof Row.TimeValueTimeColumnValue;
         if (!needAdditionalTimeColumn) {
             timeColumnIndex = ((Row.TimeColumnValue) timeColumnValue).getIndex();
         }
         this.timeColumnValue = timeColumnValue;
     }
 
     public void setValues(ColumnValue[] values) {
         this.values = values;
     }
 
     public void setValue(int i, ColumnValue value) {
         this.values[i] = value;
     }
 
     public ColumnValue[] getValues() {
         return values;
     }
 
     public ColumnValue getValue(int i) {
         return values[i];
     }
 
     public boolean needAdditionalTimeColumn() {
         return needAdditionalTimeColumn;
     }
 
     public int getTimeColumnIndex() {
         return timeColumnIndex;
     }
 
     public Row.TimeColumnValue getTimeColumnValue() {
         return timeColumnValue;
     }
 
     public static interface ColumnValue {
         ColumnType getColumnType();
         void set(String v);
         void write(FileWriter with) throws PreparePartsException;
     }
 
     public static abstract class AbstractColumnValue implements ColumnValue {
         private ColumnType columnType;
 
         public AbstractColumnValue(ColumnType columnType) {
             this.columnType = columnType;
         }
 
         public ColumnType getColumnType() {
             return columnType;
         }
     }
 
     public static class StringColumnValue extends AbstractColumnValue {
         private String v;
 
         public StringColumnValue(ColumnType columnType) {
             super(columnType);
         }
 
         public void set(String v) {
             this.v = v;
         }
 
         public String getString() {
             return v;
         }
 
         @Override
         public void write(FileWriter with) throws PreparePartsException {
             if (v != null) {
                 with.write(v);
             } else {
                 with.writeNil();
             }
         }
     }
 
     public static class IntColumnValue extends AbstractColumnValue {
         private int v;
 
         public IntColumnValue(ColumnType columnType) {
             super(columnType);
         }
 
         public void set(String v) {
             this.v = Integer.parseInt(v);
         }
 
         public int getInt() {
             return v;
         }
 
         @Override
         public void write(FileWriter with) throws PreparePartsException {
             with.write(v);
         }
     }
 
     public static class LongColumnValue extends AbstractColumnValue {
         private long v;
 
         public LongColumnValue(ColumnType columnType) {
             super(columnType);
         }
 
         public void set(String v) {
             this.v = Long.parseLong(v);
         }
 
         public long getLong() {
             return v;
         }
 
         @Override
         public void write(FileWriter with) throws PreparePartsException {
             with.write(v);
         }
     }
 
     public static class DoubleColumnValue extends AbstractColumnValue {
         private double v;
 
         public DoubleColumnValue(ColumnType columnType) {
             super(columnType);
         }
 
         public void set(String v) {
             this.v = Double.parseDouble(v);
         }
 
         public double getDouble() {
             return v;
         }
 
         @Override
         public void write(FileWriter with) throws PreparePartsException {
             with.write(v);
         }
     }
 
     public static class TimeColumnValue {
         protected int index;
         protected ExtStrftime timeFormat;
 
         public TimeColumnValue(int index, ExtStrftime timeFormat) {
             this.index = index;
             this.timeFormat = timeFormat;
         }
 
         public int getIndex() {
             return index;
         }
 
         public ExtStrftime getTimeFormat() {
             return timeFormat;
         }
 
         public void write(FileWriter with) throws PreparePartsException {
             throw new PreparePartsException("not implemented method");
         }
 
         public void write(Row.ColumnValue v, FileWriter with) throws PreparePartsException {
             v.getColumnType().filterAndWrite(v, this, with);
         }
     }
 
     public static class AliasTimeColumnValue extends TimeColumnValue {
         public AliasTimeColumnValue(int index, ExtStrftime timeFormat) {
             super(index, timeFormat);
         }
     }
 
     public static class TimeValueTimeColumnValue extends TimeColumnValue {
         private long timeValue;
 
         public TimeValueTimeColumnValue(long timeValue) {
            super(0, null);
             this.timeValue = timeValue;
         }
 
         public void write(FileWriter with) throws PreparePartsException {
             with.write(timeValue);
         }
 
         public void write(Row.ColumnValue v, FileWriter with) throws PreparePartsException {
             this.write(with); // v is ignore
         }
     }
 }
