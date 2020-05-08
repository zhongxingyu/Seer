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
  * Class which can hold two integers. This is useful if I want to create array 
  * of two integers to pass as argument to some function.
  *  
  * @author bastafidli
  */
 public class TwoIntStruct extends OSSObject
 {
    // Attributes ///////////////////////////////////////////////////////////////
    
    /**
     * First integer
     */
    protected int m_iFirst;
 
    /**
     * Second integer
     */
    protected int m_iSecond;
 
    // Constructors /////////////////////////////////////////////////////////////
    
    /**
     * Public constructor.
     * 
     * @param iFirst - first integer
     * @param iSecond - second integer
     */
    public TwoIntStruct(int iFirst, int iSecond)
    {
       m_iFirst = iFirst;
       m_iSecond = iSecond;
    }
 
    /**
     * Public constructor.
     * 
     * @param input - TwoIntStruct to copy into
     */
    public TwoIntStruct(TwoIntStruct input)
    {
       assert input != null : "Can't create empty TwoIntStruct";
 
       m_iFirst = input.getFirst();
       m_iSecond = input.getSecond();
    }
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * @return int
     */
    public int getFirst()
    {
       return m_iFirst;
    }
 
    /**
     * @return int
     */
    public int getSecond()
    {
       return m_iSecond;
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
          if (oObject instanceof TwoIntStruct)
          {
             TwoIntStruct input = (TwoIntStruct) oObject;
             return (m_iFirst == input.m_iFirst)
                    && (m_iSecond == input.m_iSecond);
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
       iResult = HashCodeUtils.hash(iResult, m_iFirst);
       iResult = HashCodeUtils.hash(iResult, m_iSecond);
       return iResult;
    }
 }
