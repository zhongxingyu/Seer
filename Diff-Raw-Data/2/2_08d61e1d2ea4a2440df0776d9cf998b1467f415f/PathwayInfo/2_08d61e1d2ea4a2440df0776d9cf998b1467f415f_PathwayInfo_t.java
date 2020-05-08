 /*
  * Copyright (C) 2008-2009 Institute for Computational Biomedicine,
  *                         Weill Medical College of Cornell University
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.bdval.pathways;
 
 import it.unimi.dsi.fastutil.ints.IntList;
 import it.unimi.dsi.fastutil.objects.ObjectSet;
 import it.unimi.dsi.lang.MutableString;
 
 /**
  * Stores information about which probes belong in a pathway.
  *
  * @author Fabien Campagne
  *         Date: Mar 2, 2008
  *         Time: 2:57:44 PM
  */
 public class PathwayInfo {
     public MutableString pathwayId;
     public int pathwayIndex;
     public ObjectSet<MutableString> probesetIds;
    // probeset indices must be stored in a consistent order. A List is used.
     public IntList probesetIndices;
 
 }
