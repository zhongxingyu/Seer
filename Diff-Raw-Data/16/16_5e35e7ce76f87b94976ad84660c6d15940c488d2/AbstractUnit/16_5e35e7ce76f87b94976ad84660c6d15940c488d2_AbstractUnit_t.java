 /* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
  * Jimple, a 3-address code Java(TM) bytecode representation.        *
  * Copyright (C) 1997, 1998 Raja Vallee-Rai (kor@sable.mcgill.ca)    *
  * All rights reserved.                                              *
  *                                                                   *
  * Modifications by Patrick Lam (plam@sable.mcgill.ca) are           *
  * Copyright (C) 1999 Patrick Lam.  All rights reserved.             *
  *                                                                   *
  * This work was done as a project of the Sable Research Group,      *
  * School of Computer Science, McGill University, Canada             *
  * (http://www.sable.mcgill.ca/).  It is understood that any         *
  * modification not identified as such is not covered by the         *
  * preceding statement.                                              *
  *                                                                   *
  * This work is free software; you can redistribute it and/or        *
  * modify it under the terms of the GNU Library General Public       *
  * License as published by the Free Software Foundation; either      *
  * version 2 of the License, or (at your option) any later version.  *
  *                                                                   *
  * This work is distributed in the hope that it will be useful,      *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of    *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU *
  * Library General Public License for more details.                  *
  *                                                                   *
  * You should have received a copy of the GNU Library General Public *
  * License along with this library; if not, write to the             *
  * Free Software Foundation, Inc., 59 Temple Place - Suite 330,      *
  * Boston, MA  02111-1307, USA.                                      *
  *                                                                   *
  * Java is a trademark of Sun Microsystems, Inc.                     *
  *                                                                   *
  * To submit a bug report, send a comment, or get the latest news on *
  * this project and other Sable Research Group projects, please      *
  * visit the web site: http://www.sable.mcgill.ca/                   *
  * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
 
 /*
  Reference Version
  -----------------
  This is the latest official version on which this file is based.
 
  Change History
  --------------
  A) Notes:
 
  Please use the following template.  Most recent changes should
  appear at the top of the list.
 
  - Modified on [date (March 1, 1900)] by [name]. [(*) if appropriate]
    [description of modification].
 
  Any Modification flagged with "(*)" was done as a project of the
  Sable Research Group, School of Computer Science,
  McGill University, Canada (http://www.sable.mcgill.ca/).
 
  You should add your copyright, using the following template, at
  the top of this file, along with other copyrights.
 
  *                                                                   *
  * Modifications by [name] are                                       *
  * Copyright (C) [year(s)] [your name (or company)].  All rights     *
  * reserved.                                                         *
  *                                                                   *
 
  B) Changes:
 
 - Modified on June 14, 1999 by Raja Vallee-Rai (kor@sable.mcgill.ca) (*)
   Made the AllMapTo a static variable.
   
  - Modified on May 24, 1999 by Raja Vallee-Rai (kor@sable.mcgill.ca) (*)
    Fixed a bug with getUseDefAndBoxes method.
    
  - Modified on May 13, 1999 by Raja Vallee-Rai (kor@sable.mcgill.ca) (*)
    Moved AbstractStmt's contents to AbstractUnit.
    
  - Modified on February 3, 1999 by Patrick Lam (plam@sable.mcgill.ca) (*)
    Added changes in support of the Grimp intermediate
    representation (with aggregated-expressions).
 
  - Modified on November 2, 1998 by Raja Vallee-Rai (kor@sable.mcgill.ca) (*)
    Repackaged all source files and performed extensive modifications.
    First initial release of Soot.
 
  - Modified on 15-Jun-1998 by Raja Vallee-Rai (kor@sable.mcgill.ca). (*)
    First internal release (Version 0.1).
 */
 
 package ca.mcgill.sable.soot;
 
 import ca.mcgill.sable.soot.*;
 import ca.mcgill.sable.util.*;
 import java.util.*;
 
 public abstract class AbstractUnit implements Unit
 {
    static Map allMapToUnnamed = Collections.unmodifiableMap(new AllMapTo("<unnamed>"));
     
     /**
      * The list of boxes is dynamically updated as the structure changes.
      * Note that they are returned in usual evaluation order.
      * (this is important for aggregation)
      */
 
     public List getUseBoxes()
     {
         return emptyList;
     }
 
     /**
      * The list of boxes is dynamically updated as the structure changes.
      */
 
     public List getDefBoxes()
     {
         return emptyList;
     }
 
     /**
      * The list of boxes is dynamically updated as the structure changes.
      */
 
     public List getUnitBoxes()
     {
         return emptyList;
     }
 
     static final public List emptyList = Collections.unmodifiableList(new ArrayList());
 
     List boxesPointingToThis = new ArrayList();
     List valueBoxes = null;
 
     public List getBoxesPointingToThis()
     {
         return boxesPointingToThis;
     }
 
     public List getUseAndDefBoxes()
     {
         valueBoxes = new ArrayList();
 
         valueBoxes.addAll(getUseBoxes());
         valueBoxes.addAll(getDefBoxes());
 
         valueBoxes = Collections.unmodifiableList(valueBoxes);
 
         return valueBoxes;
     }
 
     public void apply(Switch sw)
     {
     }
 
     public String toBriefString()
     {
         return toString(true, allMapToUnnamed, "");
     }
     
     public String toBriefString(Map stmtToName)
     {
         return toString(true, stmtToName, "");
     }
     
     public String toBriefString(String indentation)
     {
         return toString(true, allMapToUnnamed, indentation);
     }
     
     public String toBriefString(Map stmtToName, String indentation)
     {
         return toString(true, stmtToName, indentation);
     }
     
     public String toString()
     {
         return toString(false, allMapToUnnamed, "");
     }
     
     public String toString(Map stmtToName)
     {
         return toString(false, stmtToName, "");
     }
     
     public String toString(String indentation)
     {
         return toString(false, allMapToUnnamed, indentation);
     }
     
     public String toString(Map stmtToName, String indentation)
     {
         return toString(false, stmtToName, indentation);
     }
     
     abstract protected String toString(boolean isBrief, Map stmtToName, String indentation);
 
    static class AllMapTo extends AbstractMap
     {
         Object dest;
         
         public AllMapTo(Object dest)
         {
             this.dest = dest;
         }
         
         public Object get(Object key)
         {
             return dest;
         }
         
         public Set entrySet()
         {
             throw new UnsupportedOperationException();
         }
     }
     
 }
 
