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
 package com.google.code.nanorm.test.resultmap;
 
 import java.sql.Date;
 import java.sql.Time;
 import java.sql.Timestamp;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.code.nanorm.annotations.Insert;
 import com.google.code.nanorm.annotations.Mapping;
 import com.google.code.nanorm.annotations.ResultMap;
 import com.google.code.nanorm.annotations.Select;
 import com.google.code.nanorm.test.beans.CoreTypesBean;
 import com.google.code.nanorm.test.common.MapperTestBase;
 
 /**
  * 
  * @author Ivan Dubrov
  * @version 1.0 27.05.2008
  */
 @SuppressWarnings("all")
 public class CoreTypesResultMapTest extends MapperTestBase {
 	public interface Mapper {
 		@ResultMap(mappings = { @Mapping(property = "id", column = "id"),
 				@Mapping(property = "primByte", column = "primByte"),
 				@Mapping(property = "wrapByte", column = "wrapByte"),
 				@Mapping(property = "primShort", column = "primShort"),
 				@Mapping(property = "wrapShort", column = "wrapShort"),
 				@Mapping(property = "primInt", column = "primInt"),
 				@Mapping(property = "wrapInt", column = "wrapInt"),
 				@Mapping(property = "primLong", column = "primLong"),
 				@Mapping(property = "wrapLong", column = "wrapLong"),
 				@Mapping(property = "primBoolean", column = "primBoolean"),
 				@Mapping(property = "wrapBoolean", column = "wrapBoolean"),
 				@Mapping(property = "primChar", column = "primChar"),
 				@Mapping(property = "wrapChar", column = "wrapChar"),
 				@Mapping(property = "primFloat", column = "primFloat"),
 				@Mapping(property = "wrapFloat", column = "wrapFloat"),
 				@Mapping(property = "primDouble", column = "primDouble"),
 				@Mapping(property = "wrapDouble", column = "wrapDouble"),
 				@Mapping(property = "string", column = "string"),
 				@Mapping(property = "date", column = "date"),
 				@Mapping(property = "sqlDate", column = "sqlDate"),
 				@Mapping(property = "sqlTime", column = "sqlTime"),
 				@Mapping(property = "sqlTimestamp", column = "sqlTimestamp") })
 		@Select("SELECT id, primByte, wrapByte, primShort, wrapShort, "
 				+ "primInt, wrapInt, primLong, wrapLong, "
 				+ "primBoolean, wrapBoolean, primChar, wrapChar, "
 				+ "primFloat, wrapFloat, primDouble, wrapDouble, "
 				+ "string, date, sqldate, sqltime, sqltimestamp "
 				+ "FROM core WHERE id = ${1}")
 		CoreTypesBean select(int id);
 
 		@ResultMap(mappings = { @Mapping(property = "id", columnIndex = 1),
 				@Mapping(property = "primByte", columnIndex = 2),
 				@Mapping(property = "wrapByte", columnIndex = 3),
 				@Mapping(property = "primShort", columnIndex = 4),
 				@Mapping(property = "wrapShort", columnIndex = 5),
 				@Mapping(property = "primInt", columnIndex = 6),
 				@Mapping(property = "wrapInt", columnIndex = 7),
 				@Mapping(property = "primLong", columnIndex = 8),
 				@Mapping(property = "wrapLong", columnIndex = 9),
 				@Mapping(property = "primBoolean", columnIndex = 10),
 				@Mapping(property = "wrapBoolean", columnIndex = 11),
 				@Mapping(property = "primChar", columnIndex = 12),
 				@Mapping(property = "wrapChar", columnIndex = 13),
 				@Mapping(property = "primFloat", columnIndex = 14),
 				@Mapping(property = "wrapFloat", columnIndex = 15),
 				@Mapping(property = "primDouble", columnIndex = 16),
 				@Mapping(property = "wrapDouble", columnIndex = 17),
 				@Mapping(property = "string", columnIndex = 18),
 				@Mapping(property = "date", columnIndex = 19),
 				@Mapping(property = "sqlDate", columnIndex = 20),
 				@Mapping(property = "sqlTime", columnIndex = 21),
 				@Mapping(property = "sqlTimestamp", columnIndex = 22) })
 		@Select("SELECT id, primByte, wrapByte, primShort, wrapShort, "
 				+ "primInt, wrapInt, primLong, wrapLong, "
 				+ "primBoolean, wrapBoolean, primChar, wrapChar, "
 				+ "primFloat, wrapFloat, primDouble, wrapDouble, "
 				+ "string, date, sqldate, sqltime, sqltimestamp "
 				+ "FROM core WHERE id = ${1}")
 		CoreTypesBean select2(int id);
 
 		@Insert("INSERT INTO core("
 				+ "id, primByte, wrapByte, primShort, wrapShort, "
 				+ "primInt, wrapInt, primLong, wrapLong, "
 				+ "primBoolean, wrapBoolean, primChar, wrapChar, "
 				+ "primFloat, wrapFloat, primDouble, wrapDouble, "
 				+ "string, date, sqldate, sqltime, sqltimestamp) VALUES("
 				+ "${1}, ${2.primByte}, ${2.wrapByte}, ${2.primShort}, ${2.wrapShort}, "
 				+ "${2.primInt}, ${2.wrapInt}, ${2.primLong}, ${2.wrapLong}, "
 				+ "${2.primBoolean}, ${2.wrapBoolean}, ${2.primChar}, ${2.wrapChar}, "
 				+ "${2.primFloat}, ${2.wrapFloat}, ${2.primDouble}, ${2.wrapDouble}, "
 				+ "${2.string}, ${2.date}, ${2.sqlDate}, ${2.sqlTime}, ${2.sqlTimestamp})")
 		void insert(int id, CoreTypesBean bean);
 	}
 
 	/**
 	 * Test core types mapping (mapped by column name)
 	 */
 	@Test
 	public void testCoreTypes() throws Exception {
 		Mapper mapper = factory.createMapper(Mapper.class);
 		CoreTypesBean bean = mapper.select(1);
 
 		assertData(bean);
 	}
 
 	/**
 	 * Test core types mapping (mapped by column index)
 	 */
 	@Test
 	public void testCoreTypes2() throws Exception {
 		Mapper mapper = factory.createMapper(Mapper.class);
 		CoreTypesBean bean = mapper.select2(1);
 
 		assertData(bean);
 	}
 	
 	/**
 	 * Test core types mapping (parameters setters)
 	 */
 	@Test
 	public void testCoreTypes3() throws Exception {
 		Mapper mapper = factory.createMapper(Mapper.class);
 		
 		CoreTypesBean bean = mapper.select(1);
 		mapper.insert(5, bean);
 		CoreTypesBean bean2 = mapper.select(5);
 		assertData(bean2);
 	}
 	
 	/**
 	 * Test core types mapping (nulls)
 	 */
 	@Test
 	public void testCoreTypes4() throws Exception {
 		Mapper mapper = factory.createMapper(Mapper.class);
 		
 		CoreTypesBean bean = new CoreTypesBean();
 		bean.setId(17);
 		mapper.insert(17, bean);
 		CoreTypesBean bean2 = mapper.select(17);
 
 		// Some values are transformed from NULLs to zero values
 		Assert.assertEquals(Boolean.FALSE, bean2.getWrapBoolean());
 		bean.setWrapBoolean(Boolean.FALSE);
 		
 		Assert.assertEquals(0, (byte) bean2.getWrapByte());
 		bean.setWrapByte((byte) 0);
 		
 		Assert.assertEquals(0, (double) bean2.getWrapDouble(), 0.01);
 		bean.setWrapDouble(0.0);
 		
 		Assert.assertEquals(0, (float) bean2.getWrapFloat(), 0.01);
 		bean.setWrapFloat(0.0F);
 		
 		Assert.assertEquals(0, (int) bean2.getWrapInt());
 		bean.setWrapInt(0);
 		
 		Assert.assertEquals(0, (long) bean2.getWrapLong());
 		bean.setWrapLong(0L);
 		
 		Assert.assertEquals(0, (short) bean2.getWrapShort());
 		bean.setWrapShort((short) 0);
 		
 		// After adjustions, they should be equal
 		Assert.assertEquals(bean, bean2);
 	}
 
 	private void assertData(CoreTypesBean bean) {
 		Assert.assertEquals(37, bean.getPrimByte());
 		Assert.assertEquals(-23, (byte) bean.getWrapByte());
 		Assert.assertEquals(8723, bean.getPrimShort());
 		Assert.assertEquals(-6532, (short) bean.getWrapShort());
 		Assert.assertEquals(824756237, bean.getPrimInt());
 		Assert.assertEquals(-123809163, (int) bean.getWrapInt());
 		Assert.assertEquals(282347987987234987L, bean.getPrimLong());
 		Assert.assertEquals(-23429879871239879L, (long) bean.getWrapLong());
 		Assert.assertEquals(true, bean.isPrimBoolean());
 		Assert.assertEquals(false, (boolean) bean.getWrapBoolean());
 		Assert.assertEquals('a', bean.getPrimChar());
 		Assert.assertEquals('H', (char) bean.getWrapChar());
 		Assert.assertEquals(34.5f, bean.getPrimFloat(), 0.01f);
 		Assert.assertEquals(-25.25f, (float) bean.getWrapFloat(), 0.01f);
 		Assert.assertEquals(44.5, bean.getPrimDouble(), 0.01);
 		Assert.assertEquals(-47.125, (double) bean.getWrapDouble(), 0.01);
 		Assert.assertEquals("Hello, H2!", bean.getString());
 		Assert.assertEquals(new Date(1165795200000L), bean.getSqlDate());
 		Assert.assertEquals(new Time(59521000L), bean.getSqlTime());
 		Assert.assertEquals(new Timestamp(1215540491000L), bean.getSqlTimestamp());
 		Assert.assertEquals(new java.util.Date(1244388214000L), bean.getDate());
 	}
 }
