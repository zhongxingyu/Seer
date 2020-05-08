 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.cli;
 
 import com.google.common.collect.Maps;
 import java.util.Map;
 
 /**
  * Utility class for the CLI.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public final class Utils {
     /** Utility classes should not be instantiated. */
     private Utils() {
     }
 
     /**
      * Extracts a map where K → V corresponds to k₁/v₁,k₂/v₂,k₃/-1,k_n/v_n.
      *
      * @param str the string to parse
      * @return a map created from the string
      */
     public static Map<String, Integer> parseGapClasses(final String str) {
         Map<String, Integer> map = Maps.newHashMap();
         for (String clazz : str.split(",")) {
             if (clazz.contains("/")) {
                 String[] opts = clazz.split("/");
                 map.put(opts[0], Integer.parseInt(opts[1]));
             } else {
                map.put(clazz, -1);
             }
         }
 
         return map;
     }
 
 }
