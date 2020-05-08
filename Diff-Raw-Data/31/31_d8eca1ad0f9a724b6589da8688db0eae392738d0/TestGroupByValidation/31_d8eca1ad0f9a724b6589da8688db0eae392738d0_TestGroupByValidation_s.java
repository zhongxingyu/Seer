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
 import com.google.code.nanorm.annotations.ResultMapRef;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.config.NanormConfiguration;
 import com.google.code.nanorm.exceptions.ConfigurationException;
 
 /**
  * Test groupBy usage validation.
  * 
  * @author Ivan Dubrov
  */
 public class TestGroupByValidation extends TestConfigValidationBase {
 	
 	@ResultMap(id = "nestedmap")
 	private interface Mapper1 {
 		@Select("SELECT 1")
 		@ResultMap(id = "samplemap", groupBy = "prop1", mappings = { @Mapping(property = "prop1", 
 				nestedMap = @ResultMapRef("nestedmap")) })
 		int selectSome(int id);
 	}
 
 	/**
 	 * Test that properties marked by groupBy do not have a nested map specified.
 	 */
 	@Test
 	public void testGroupByNested() {
 		try {
 			new NanormConfiguration().configure(Mapper1.class);
 			Assert.fail();
 		} catch (ConfigurationException e) {
 			assertContains(e, "groupBy", "prop1", "nestedmap", "samplemap");
 		}
 	}
 
 	private interface Mapper2 {
 		@Select("SELECT 1")
 		@ResultMap(id = "samplemap", groupBy = { "prop1", "prop3" }, mappings = { @Mapping(property = "prop1") })
 		int selectSome(int id);
 	}
 
 	/**
 	 * Test that properties marked by groupBy do not have a nested map specified.
 	 */
 	@Test
 	public void testGroupByPropertyNotFound() {
 		try {
 			new NanormConfiguration().configure(Mapper2.class);
 			Assert.fail();
 		} catch (ConfigurationException e) {
			assertContains(e, "groupBy", "prop3", "not", "configured", "samplemap");
 		}
 	}
 }
