 /***********************************************************************************************
  * Copyright (c) Microsoft Corporation All rights reserved.
  * 
  * MIT License:
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  ***********************************************************************************************/
 
 package com.microsoft.gittf.core.util.shelveset;
 
 import java.util.Comparator;
 
 import com.microsoft.gittf.core.util.Check;
 import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Shelveset;
 
 /**
  * Compares two shelvesets based on the sort options specified
  * 
  */
 public class ShelvesetCompartor
     implements Comparator<Shelveset>
 {
     private final ShelvesetSortOption sortOption;
 
     /**
      * Constructor
      * 
      * @param sortOption
      */
     public ShelvesetCompartor(ShelvesetSortOption sortOption)
     {
         Check.notNull(sortOption, "sortOption"); //$NON-NLS-1$
 
         this.sortOption = sortOption;
     }
 
     public int compare(Shelveset arg0, Shelveset arg1)
     {
         switch (sortOption)
         {
             case NAME:
                return arg0.getName().compareTo(arg1.getName());
             case OWNER:
                return arg0.getOwnerName().compareTo(arg1.getOwnerName());
             case DATE:
                return arg1.getCreationDate().compareTo(arg0.getCreationDate());
         }
 
         return 0;
     }
 }
