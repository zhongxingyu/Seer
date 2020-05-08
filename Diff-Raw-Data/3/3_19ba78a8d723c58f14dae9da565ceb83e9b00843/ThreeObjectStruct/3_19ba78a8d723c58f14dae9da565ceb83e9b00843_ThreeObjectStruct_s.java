 /*
  * Copyright (C) 2008 - 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
  * Class which can hold three objects. This is useful if one wants to create  
  * array of three objects to pass as argument to some function.
  *  
  * @author bastafidli
  */
 public class ThreeObjectStruct extends TwoObjectStruct 
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * Third object
     */
    protected Object m_objThird;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Public constructor.
     * 
     * @param objFirst - first object
     * @param objSecond - second object
     */
    public ThreeObjectStruct(
       Object objFirst, 
       Object objSecond,
       Object objThird
    )
    {
       super(objFirst, objSecond);
       
       m_objThird = objThird;
    }
 
    /**
     * Public constructor.
     * 
     * @param input - ThreeObjectStruct to copy into
     */
    public ThreeObjectStruct(
       ThreeObjectStruct input
    )
    {
       super(input);
       
       assert input != null : "Can't create empty ThreeObjectStruct";
       
 
       m_objThird = input.getThird();
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * @return object
     */
    public Object getThird()
    {
       return m_objThird;
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
          if (oObject instanceof ThreeObjectStruct)
          {
             ThreeObjectStruct input = (ThreeObjectStruct) oObject;
             
             return (super.equals(oObject)) 
                    && ObjectUtils.equals(m_objThird, input.m_objThird);
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
       iResult = HashCodeUtils.hash(iResult, m_objThird);
       iResult = HashCodeUtils.hash(iResult, super.hashCode());
       return iResult;
    }
 }
