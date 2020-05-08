 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.vesalainen.parsers.sql;
 
 /**
  * @author Timo Vesalainen
  */
 public class SubStringFunction extends AbstractFunction
 {
     private int begin;
     private int end;
     /**
      * SubStringFunction.
      * @param inner
      * @param begin Start position. Note! Starts from 0
      * @param length 
      */
     public SubStringFunction(ColumnReference inner, Number begin, Number... length)
     {
         super(inner);
         this.begin = begin.intValue();
         switch (length.length)
         {
             case 0:
                 break;
             case 1:
                 this.end = this.begin + length[0].intValue();
             default:
                 throw new IllegalArgumentException("wrong number of arguments");
         }
     }
 
     @Override
     public Object function(Object value)
     {
         if (value != null)
         {
             if (end > 0)
             {
                 return value.toString().substring(begin, end);
             }
             else
             {
                 return value.toString().substring(begin);
             }
         }
         return null;
     }
 
 }
