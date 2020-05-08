 /*
     SaxonType.java
 
     @author: <a href="mailto:ovidiu@cup.hp.com">Ovidiu Predescu</a>
     Date: March 22, 2001
 
     Copyright (C) 2001 Ovidiu Predescu
 
     This program is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public License as
     published by the Free Software Foundation; either version 2 of the
     License, or (at your option) any later version.
    
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
    
     You should have received a copy of the GNU General Public License
     along with this program; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
     02111-1307, USA.
  */
 
 package xslt.debugger.saxon;
 
 import com.icl.saxon.expr.Value;
 
 import xslt.debugger.Type;
 
 public class SaxonType extends Type
 {
   public SaxonType(int saxonType)
   {
     super(null);
 
     switch (saxonType) {
     case Value.BOOLEAN:
       type = Type.BOOLEAN;
       break;
     case Value.NUMBER:
      type = Type.BOOLEAN;
       break;
     case Value.STRING:
       type = Type.STRING;
       break;
     case Value.NODESET:
       type = Type.NODESET;
       break;
     case Value.OBJECT:
       type = Type.OBJECT;
       break;
     default:
       type = Type.ANY;
       break;
     }
   }
 }
