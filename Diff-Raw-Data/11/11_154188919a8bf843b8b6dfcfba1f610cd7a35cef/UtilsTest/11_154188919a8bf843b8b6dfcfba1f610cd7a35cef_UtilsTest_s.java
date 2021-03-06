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
 import org.junit.Test;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class UtilsTest {
 
     @Test
     public void testParseGapClasses() {
         Map<String, Integer> map = Utils.parseGapClasses("articles/4,prepositions/2,foo");
         Map<String, Integer> expected = Maps.newHashMap();
         expected.put("articles", 4);
         expected.put("prepositions", 2);
        expected.put("foo", -1);
 
         assertThat(map, is(expected));
     }
 }
