 /*
  * Copyright (C) 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
 
 package org.opensubsystem.pattern.reference.data;
 
 import org.opensubsystems.core.error.OSSException;
 
 /**
  * Interface to programmatically access references from one data object to 
  * another.
  * 
  * @author bastafidli
  */
 public interface Reference 
 {
    /**
     * Get id of the object of particular data type that is being referred to 
     * by the implementor of this interface.
     * 
     * @param iReferenceDataType - data type of the referenced object, id of which
     *                             is being queried.
     * @param strAdditionalClassifier - if the implementor contains multiple 
     *                                  references of the same type, this 
     *                                  additional classifier allows to distinguish
     *                                  among them
     * @return Long - id of the referenced object or null if there is no reference
    * @throws OSSException - an error has occurred
     */
    Long getReferenceId(
       int    iReferenceDataType,
       String strAdditionalClassifier
    ) throws OSSException;
 }
