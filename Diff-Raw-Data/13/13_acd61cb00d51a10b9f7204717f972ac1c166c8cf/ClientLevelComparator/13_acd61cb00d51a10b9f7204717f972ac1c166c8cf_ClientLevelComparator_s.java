 /**
  * This file is part of Orion source code
  * 
  * Copyright (C) 2012 [Gore]Clan - http://www.goreclan.net
  *
  * Orion is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Lesser Public
  * License as published by the Free Software Foundation; either
  * version 2 of the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Orion. If not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
  * 
  * @author      Daniele Pantaleone
  * @version     1.0
  * @copyright   Daniele Pantaleone, 11 March, 2013
  * @package     com.orion.comparator
  **/
 
 package com.orion.comparator;
 
 import java.util.Comparator;
 
 import com.google.common.collect.ComparisonChain;
 import com.orion.domain.Client;
 
 
 public class ClientLevelComparator implements Comparator<Client> {
     
     /**
      * Compare two <tt>Client</tt> objects according to their <tt>Group</tt> level
      * 
      * @author Daniele Pantaleone
      * @return The comparison between the given <tt>Client</tt> objects according to their <tt>Group</tt> level
      **/
     public int compare(Client c1, Client c2) {
         return ComparisonChain.start()
                 .compare(c1.group.level, c2.group.level)
                 .compare(c1.name, c2.name)
                  .result();
     }
     
 }
 
