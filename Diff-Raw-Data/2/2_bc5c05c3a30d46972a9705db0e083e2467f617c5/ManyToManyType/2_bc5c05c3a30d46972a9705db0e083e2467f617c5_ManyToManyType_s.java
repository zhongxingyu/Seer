 /**
  * ***************************************************************************
  * Copyright (c) 2010 Qcadoo Limited
  * Project: Qcadoo Framework
  * Version: 0.4.8
  *
  * This file is part of Qcadoo.
  *
  * Qcadoo is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation; either version 3 of the License,
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  * ***************************************************************************
  */
 package com.qcadoo.model.api.types;
 
 import com.qcadoo.model.api.DataDefinition;
 
 /**
  * Object represents "many to many" field type.
  * 
 * @since 0.4.0
  */
 public interface ManyToManyType extends FieldType {
 
     /**
      * Cascade type.
      */
     enum Cascade {
         NULLIFY, DELETE
     }
 
     /**
      * Returns data definition.
      * 
      * @return data definition
      */
     DataDefinition getDataDefinition();
 
     /**
      * Returns cascade type.
      * 
      * @return cascade type
      */
     Cascade getCascade();
 
     /**
      * Returns true if field should be copied.
      * 
      * @return true if should be copied
      */
     boolean isCopyable();
 
 }
