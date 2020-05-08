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
  * Class which can hold three integers. This is useful if I want to create array 
  * of three integers to pass as argument to some function.
  *  
  * @author bastafidli
  */
 public class ThreeIntStruct extends TwoIntStruct
 {
    // Attributes ///////////////////////////////////////////////////////////////   
    
    /**
     * Third integer
     */
    protected int m_iThird;
    
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Public constructor.
     * 
     * @param iFirst - first integer
     * @param iSecond - second integer
     * @param iThird - third integer
     */
    public ThreeIntStruct(
       int iFirst,
       int iSecond,
       int iThird
    )
    {
       super(iFirst, iSecond);
       
       m_iThird = iThird;
    }
    
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * @return int
     */
    public int getThird()
    {
       return m_iThird;
    }
 
    /**
     * {@inheritDoc}
     */
   @Override
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
          if (oObject instanceof ThreeIntStruct)
          {
             ThreeIntStruct input = (ThreeIntStruct) oObject;
             return (super.equals(oObject)) && (m_iThird == input.m_iThird);
          }
       }
       return bRetval;
    }   
 
    /**
     * {@inheritDoc}
     */
   @Override
    public int hashCode()
    {
       int iResult = HashCodeUtils.SEED;
       iResult = HashCodeUtils.hash(iResult, m_iThird);
       iResult = HashCodeUtils.hash(iResult, super.hashCode());
       return iResult;
    }
 
 }
