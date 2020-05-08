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
 package com.github.fhirschmann.clozegen.lib.multiset;
 
 import com.google.common.collect.LinkedHashMultiset;
 import com.google.common.collect.Multiset;
 import java.io.File;
 import java.io.IOException;
import java.net.URISyntaxException;
 import org.junit.Before;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class WriteMultisetTest {
     private Multiset<String> multiset1;
     private Multiset<String> multiset2;
 
     @Before
     public void setUp() {
         multiset1 = LinkedHashMultiset.create();
         multiset1.add("foo", 4);
         multiset1.add("bar", 9);
         multiset2 = LinkedHashMultiset.create();
         multiset2.add("foo", 42);
         multiset2.add("bar2", 11);
     }
 
     @Test
    public void testWriteMultiset() throws IOException, URISyntaxException {
         File file = File.createTempFile("multisetutils", "txt");
         WriteMultisets.writeMultiSet(multiset1, file);
         Multiset<String> multisetr = ReadMultisets.parseMultiset(file.toURI().toURL());
         assertEquals(multiset1, multisetr);
     }
 
     @Test
    public void testWriteSortedMultiset() throws IOException, URISyntaxException {
         File file = File.createTempFile("multisetutils", "txt");
         WriteMultisets.writeSortedMultiSet(multiset2, file);
         Multiset<String> multisetr = ReadMultisets.parseMultiset(file.toURI().toURL());
         assertEquals("[foo x 42, bar2 x 11]", multisetr.toString());
     }
 }
