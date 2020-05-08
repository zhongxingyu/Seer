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
 
 /**
  * Class which can hold two objects. This is useful if one wants to create array 
  * of two objects to pass as argument to some function.
  *  
  * @author OpenSubsystems
  */
 public class TwoObjectStruct extends OSSObject
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * First object
     */
    protected Object m_objFirst;
 
    /**
     * Second object
     */
    protected Object m_objSecond;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Public constructor.
     * 
     * @param objFirst - first object
     * @param objSecond - second object
     */
    public TwoObjectStruct(
       Object objFirst, 
       Object objSecond
    )
    {
       m_objFirst = objFirst;
       m_objSecond = objSecond;
    }
 
    /**
     * Public constructor.
     * 
     * @param input - TwoObjectStruct to copy into
     */
    public TwoObjectStruct(
       TwoObjectStruct input
    )
    {
       assert input != null : "Can't create empty TwoObjectStruct";
 
       m_objFirst = input.getFirst();
       m_objSecond = input.getSecond();
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * @return object
     */
    public Object getFirst()
    {
       return m_objFirst;
    }
 
    /**
     * @return object
     */
    public Object getSecond()
    {
       return m_objSecond;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean equals(
       Object oObject
    )
    {
       boolean bRetval = false;
 
       if (oObject == this)
       {
          return true;
       }
       else if (oObject != null)
       {
          if (oObject instanceof TwoObjectStruct)
          {
             TwoObjectStruct input = (TwoObjectStruct) oObject;
             
             return ObjectUtils.equals(m_objFirst, input.m_objFirst)
                    && ObjectUtils.equals(m_objSecond, input.m_objSecond);
          }
       }
 
       return bRetval;
    }
 
    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
       int iResult = HashCodeUtils.SEED;
       iResult = HashCodeUtils.hash(iResult, m_objFirst);
       iResult = HashCodeUtils.hash(iResult, m_objSecond);
       return iResult;
    }
 }
