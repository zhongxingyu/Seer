 package org.kalibro.core.persistence.record;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Modifier;
 import java.util.Random;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Table;
 
 import org.junit.Test;
 import org.kalibro.ProcessState;
 import org.kalibro.core.reflection.FieldReflector;
 import org.kalibro.tests.UnitTest;
 
 public class ProcessTimeRecordTest extends UnitTest {
 
 	@Test
 	public void shouldHavePublicDefaultConstructor() throws Exception {
 		Constructor<ProcessTimeRecord> constructor = ProcessTimeRecord.class.getConstructor();
 		assertTrue(Modifier.isPublic(constructor.getModifiers()));
 	}
 
 	@Test
 	public void shouldConvertProperly() {
 		ProcessState[] states = ProcessState.values();
 		ProcessState state = states[new Random().nextInt(states.length)];
 		Long time = mock(Long.class);
		ProcessTimeRecord record = new ProcessTimeRecord(state, time);
 		assertSame(state, record.state());
 		assertSame(time, record.time());
 	}
 
 	@Test
 	public void shouldHaveClassAnnotations() {
 		Class<ProcessTimeRecord> recordClass = ProcessTimeRecord.class;
 		assertEquals("ProcessTime", recordClass.getAnnotation(Entity.class).name());
 		assertEquals("\"PROCESS_TIME\"", recordClass.getAnnotation(Table.class).name());
 	}
 
 	@Test
 	public void shouldHaveFieldAnnotations() {
 		FieldReflector reflector = new FieldReflector(new ProcessTimeRecord());
 		assertNotNull(reflector.getFieldAnnotation("id", Column.class));
 		assertNotNull(reflector.getFieldAnnotation("state", Column.class));
 		assertNotNull(reflector.getFieldAnnotation("time", Column.class));
 	}
 }
