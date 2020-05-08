 package com.jshnd.virp.hector;
 
 import com.google.common.collect.Sets;
import com.jshnd.virp.*;
 import com.jshnd.virp.config.RowMapperMetaData;
 import me.prettyprint.cassandra.serializers.LongSerializer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.assertSame;
 
 public class HectorActionFactoryTest {
 
 	private HectorSessionFactory testObj;
 
 	@Before
 	public void setup() {
 		testObj = new HectorSessionFactory();
 	}
 
	private static class TestValueAccessor<T> extends StaticValueAccessor<T> implements ValueManipulator<T> {

		private TestValueAccessor(T value, Class<T> type) {
			super(value, type);
		}

		@Override
		public void setValue(Object sourceObject, T value) {

		}
	}

 	@Test
 	public void testStringSerializationRegistration() {
 		RowMapperMetaData type = new RowMapperMetaData(Object.class);
 		ValueAccessor<String> accessor1 = new StaticValueAccessor<String>("foo", String.class);
 		ValueAccessor<String> accessor2 = new StaticValueAccessor<String>("foo", String.class);
		TestValueAccessor<String> accessor3 = new TestValueAccessor<String>("foo", String.class);
 		type.setKeyValueAccessor(accessor1);
 		BasicColumnAccessor<String, String> columnAccessor =
 				new BasicColumnAccessor<String, String>(accessor2, accessor3);
 		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
 		testObj.setupClass(type);
 		assertSame(StringSerializer.get(), accessor1.getActionFactoryMeta());
 		assertSame(StringSerializer.get(), accessor2.getActionFactoryMeta());
 		assertSame(StringSerializer.get(), accessor3.getActionFactoryMeta());
 	}
 
 	@Test
 	public void testLongSerializationRegistration() {
 		RowMapperMetaData type = new RowMapperMetaData(Object.class);
 		ValueAccessor<Long> accessor1 = new StaticValueAccessor<Long>(Long.valueOf(1), Long.class);
 		ValueAccessor<Long> accessor2 = new StaticValueAccessor<Long>(Long.valueOf(1), Long.class);
		TestValueAccessor<Long> accessor3 = new TestValueAccessor<Long>(Long.valueOf(1), Long.class);
 		type.setKeyValueAccessor(accessor1);
 		BasicColumnAccessor<Long, Long> columnAccessor =
 				new BasicColumnAccessor<Long, Long>(accessor2, accessor3);
 		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
 		testObj.setupClass(type);
 		assertSame(LongSerializer.get(), accessor1.getActionFactoryMeta());
 		assertSame(LongSerializer.get(), accessor2.getActionFactoryMeta());
 		assertSame(LongSerializer.get(), accessor3.getActionFactoryMeta());
 	}
 
 	@Test
 	public void testPrimitiveLongSerializationRegistration() {
 		RowMapperMetaData type = new RowMapperMetaData(Object.class);
 		ValueAccessor<Long> accessor1 = new StaticValueAccessor<Long>(Long.valueOf(1), long.class);
 		ValueAccessor<Long> accessor2 = new StaticValueAccessor<Long>(Long.valueOf(1), long.class);
		TestValueAccessor<Long> accessor3 = new TestValueAccessor<Long>(Long.valueOf(1), long.class);
 		type.setKeyValueAccessor(accessor1);
 		BasicColumnAccessor<Long, Long> columnAccessor =
 				new BasicColumnAccessor<Long, Long>(accessor2, accessor3);
 		type.setColumnAccessors(Sets.<ColumnAccessor<?,?>>newHashSet(columnAccessor));
 		testObj.setupClass(type);
 		assertSame(LongSerializer.get(), accessor1.getActionFactoryMeta());
 		assertSame(LongSerializer.get(), accessor2.getActionFactoryMeta());
 		assertSame(LongSerializer.get(), accessor3.getActionFactoryMeta());
 	}
 
 }
