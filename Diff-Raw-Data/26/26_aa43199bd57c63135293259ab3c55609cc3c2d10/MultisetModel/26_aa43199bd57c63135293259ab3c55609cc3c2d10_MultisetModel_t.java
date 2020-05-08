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
 package com.github.fhirschmann.clozegen.lib.generators.model;
 
 import com.github.fhirschmann.clozegen.lib.multiset.ReadMultisets;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.Multiset;
 import java.io.IOException;
 import java.net.URL;
 
 /**
  * A simple multiset-based model. The frequencies will be loaded from a file.
  * Please see {@link ReadMultisets#parseMultiset(URL)} for information on
  * the file format.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class MultisetModel implements URLBasedModel {
     /**
      * The underlying multiset.
      */
     private Multiset<String> multiset;
 
     /**
      * Creates a new empty {@link MultisetModel}.
      */
     public MultisetModel() {
         multiset = HashMultiset.create();
     }
 
     @Override
    public void load(final URL url) throws IOException {
         setMultiset(ReadMultisets.parseMultiset(url));
     }
 
     /**
      * @return the multiset
      */
     public Multiset<String> getMultiset() {
         return multiset;
     }
 
     /**
      * @param multiset the multiset to set
      */
     public void setMultiset(final Multiset<String> multiset) {
         this.multiset = multiset;
     }
 }
