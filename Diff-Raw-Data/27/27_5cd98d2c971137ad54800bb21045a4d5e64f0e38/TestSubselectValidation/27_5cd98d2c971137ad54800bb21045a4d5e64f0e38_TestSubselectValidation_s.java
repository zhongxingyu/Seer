 /**
  * Copyright (C) 2008 Ivan S. Dubrov
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *         http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.google.code.nanorm.test.config;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 import com.google.code.nanorm.annotations.Mapping;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.config.NanormConfiguration;
 import com.google.code.nanorm.exceptions.ConfigurationException;
 
 /**
  * Test subselect validation.
  * @author Ivan Dubrov
  */
 public class TestSubselectValidation extends TestConfigValidationBase {
 
 	private interface Mapper8 {
 		@Select("SELECT 1")
 		@ResultMap(id = "testmap", mappings = { @Mapping(property = "dummy", subselect = "selectById") })
 		int selectSome(int id);
 	}
 
 	/**
 	 * Test result map referenced as nested map, but the referee is not mapped.
 	 */
 	@Test
 	public void testSubselectValidation1() {
 		try {
 			new NanormConfiguration().configure(Mapper8.class);
 			Assert.fail();
 		} catch (ConfigurationException e) {
 			assertContains(e, "not found");
 			assertContains(e, "Mapper8");
 			assertContains(e, "dummy");
 			assertContains(e, "testmap");
 			assertContains(e, "selectById");
 		}
 	}
 
 	private interface Mapper9 {
 		// Nothing...
 	}
 
 	private interface Mapper10 {
 		@Select("SELECT 1")
 		@ResultMap(id = "testmap", mappings = { @Mapping(property = "dummy", subselect = "selectById", subselectMapper = Mapper9.class) })
 		int selectSome(int id);
 	}
 
 	/**
 	 * Test result map referenced as nested map, but the referee is not mapped.
 	 */
 	@Test
 	public void testSubselectValidation2() {
 		try {
 			new NanormConfiguration().configure(Mapper9.class, Mapper10.class);
 			Assert.fail();
 		} catch (ConfigurationException e) {
 			assertContains(e, "not found");
 			assertContains(e, "Mapper9");
 			assertContains(e, "Mapper10");
 			assertContains(e, "dummy");
 			assertContains(e, "testmap");
 			assertContains(e, "selectById");
 		}
 	}
 	
 	private interface Mapper11 {
 		@Select("SELECT 1")
 		int selectSome(int id, int id2);
 		
 		@Select("SELECT 1")
 		@ResultMap(id = "testmap", mappings = { @Mapping(property = "dummy", subselect = "selectSome") })
 		int selectOther(int id);
 	}
 	
 	/**
 	 * Test that subselect statement has exactly 1 parameter.
 	 */
 	@Test
 	public void testSubselectParameters1() {
 		try {
 			new NanormConfiguration().configure(Mapper11.class);
 			Assert.fail();
 		} catch (ConfigurationException e) {
			e.printStackTrace();
			assertContains(e, "subselect", "exactly one", "Mapper11", "selectSome");
 		}
 	}
 }
