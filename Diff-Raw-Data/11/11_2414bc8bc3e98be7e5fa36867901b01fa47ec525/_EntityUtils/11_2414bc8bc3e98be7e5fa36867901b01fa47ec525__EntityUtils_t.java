 /**
  *   textManager, a GUI for managing bills for texter jobs
  *
  *   Copyright (C) 2013 philnate
  *
  *   This file is part of textManager.
  *
  *   textManager is free software: you can redistribute it and/or modify it under the terms of the
  *   GNU General Public License as published by the Free Software Foundation, either version 3 of the
  *   License, or (at your option) any later version.
  *
  *   textManager is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
  *   without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  *   See the GNU General Public License for more details. You should have received a copy of the GNU
  *   General Public License along with textManager. If not, see <http://www.gnu.org/licenses/>.
  */
 package me.philnate.textmanager.entities;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.util.Set;
 
 import me.philnate.textmanager.TestBase;
 import me.philnate.textmanager.entities.annotations.Named;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 public class _EntityUtils extends TestBase {
 
     @Test
     public void testFieldReading() {
 	Set<String> fields = EntityUtils.getFields(Fields.class);
 	assertEquals(2, fields.size());
 	assertTrue("missing field 'field'", fields.contains("field"));
 	assertTrue("missing field 'value'", fields.contains("value"));
     }
 
     @Test
     @Ignore
     public void testParamNameFromMethod() {
	// TODO requires powermock
 	// TODO tests retrieval of paramname
 	// TODO tets retrieval of paramname without Named
     }
 
     private static interface Fields extends Entity {
 	public Fields setField(String field);
 
 	@Named("value")
 	public Fields setVal(String value);
     }
 }
