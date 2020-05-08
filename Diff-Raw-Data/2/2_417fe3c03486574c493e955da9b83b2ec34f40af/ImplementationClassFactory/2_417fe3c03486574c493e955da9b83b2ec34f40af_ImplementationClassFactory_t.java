 /*
  * Copyright (C) 2003 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 package org.opensubsystems.core.util;
 
 import java.util.List;
 
 import org.opensubsystems.core.error.OSSException;
 
 /**
  * Class factory responsible for instantiation of implementation classes which 
  * are classes that exists in the impl package or have Impl postfix or both.
  * 
  * Assuming name of the interface aaa.AAA 
  * 1. try class in the form of aaa.impl.AAAImpl
  * 2. try aaa.AAAImpl
  * 3. try directly class aaa.AAA (from base class)
  * 
  * @author bastafidli
  */
 public class ImplementationClassFactory<T> extends ClassFactory<T>
 {
    /** 
     * Creates a new instance of ImplementationClassFactory
     * 
     * @param type - type for objects instantiated by this factory 
     */
    public ImplementationClassFactory(
       Class<? extends T> type
    )
    {
       super(type);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createDefaultClassNames(
       String       strClassIdentifier,
       String       strModifier,
       List<String> lstClassNames
    ) throws OSSException
    {
       int          iIndex;
      StringBuilder sbClassName = new StringBuilder();
       
       // Assuming name of the class aaa.AAA
       
       // Find package separator
       iIndex = strClassIdentifier.lastIndexOf('.');
       // Transform to the class aaa.impl.AAAImpl
       sbClassName.append(strClassIdentifier.substring(0, iIndex + 1));
       sbClassName.append("impl.");
       sbClassName.append(strClassIdentifier.substring(iIndex + 1));
       sbClassName.append("Impl");
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       // Then try modifier independent class aaa.AAAImpl
       sbClassName.append(strClassIdentifier);
       sbClassName.append("Impl");
       lstClassNames.add(sbClassName.toString());
       sbClassName.delete(0, sbClassName.length());
       
       super.createDefaultClassNames(strClassIdentifier, strModifier, lstClassNames);
    }
 }
