 /**
  * ********
  * Copyright © 2010-2012 Olanto Foundation Geneva
  *
  * This file is part of myCAT.
  *
  * myCAT is free software: you can redistribute it and/or modify it under the
  * terms of the GNU Affero General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  *
  * myCAT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with myCAT. If not, see <http://www.gnu.org/licenses/>.
  *
  *********
  */
 package org.olanto.util;
 
 /**
  * pour permettre l'ajout et l'enlèvement des espaces entre les tokens
  *
  * @author NG
  */
 public class StringManipulation {
 
     public String addSpace(String s) {
         StringBuilder res = new StringBuilder("");
 
         for (int i = 0; i < s.length(); i++) {
             res.append(s.charAt(i));
            if (isCJKChar(s.charAt(i))) {
                 res.append(" ");
             }
         }
         return res.toString();
     }
    
    public static boolean isCJKChar(char s) {
        if (s > 0x0370) {
            return true;
        }
        return false;
    }
 
     public String removeSpace(String s) {
         if ((!(s == null)) && (!s.isEmpty())) {
             if (s.length() < 3) {
                 return s;
             }
             StringBuilder res = new StringBuilder("");
            if (isCJKChar(s.charAt(0))) {
                 res.append(s.charAt(0));
             } else if (!(s.charAt(0) == ' ')) {
                 res.append(s.charAt(0));
             }
             for (int i = 0; i < s.length() - 1; i++) {
                if (isCJKChar(s.charAt(i))) {
                     res.append(s.charAt(i));
                 } else {
                     if (s.charAt(i) == ' ') {
                        if (((s.charAt(i - 1) <= 0x0370) && (isCJKChar(s.charAt(i + 1)))) || ((isCJKChar(s.charAt(i - 1))) && (s.charAt(i + 1) <= 0x0370))) {
                             res.append(s.charAt(i));
                         }
                     } else {
                         res.append(s.charAt(i));
                     }
                 }
             }
             return res.toString();
         } else {
             return null;
         }
     }
 }
