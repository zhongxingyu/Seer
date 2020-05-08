 /*
  * Copyright 2004-2009 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.dolteng.core.types.impl;
 
 import java.sql.Types;
 
 import org.seasar.dolteng.core.entity.ColumnMetaData;
 import org.seasar.dolteng.core.types.TypeMapping;
 
 /**
  * @author taichi
  * 
  */
 public class StandardTypeMappingRegistry extends BasicTypeMappingRegistry {
 
     public StandardTypeMappingRegistry() {
         super();
     }
 
     @Override
     public TypeMapping toJavaClass(ColumnMetaData meta) {
         TypeMapping result = null;
         if (Types.NUMERIC == meta.getSqlType()
                 || Types.DECIMAL == meta.getSqlType()
                 || "NUMERIC".equalsIgnoreCase(meta.getSqlTypeName())) {
             if (0 < meta.getColumnSize() && meta.getDecimalDigits() < 1) {
                if (meta.getColumnSize() < 9) {
                     result = find(this.sqlTypes, "INTEGER");
                 } else if (meta.getColumnSize() < 20) {
                     result = find(this.sqlTypes, "BIGINT");
                 }
             }
         }
         if (result == null) {
             result = super.toJavaClass(meta);
         }
 
         return result;
     }
 }
