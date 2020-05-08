 package org.kalibro.core.persistence.record;
 
 import org.junit.Test;
 
 public class StackTraceElementRecordTest extends RecordTest {
 
 	@Override
 	protected void verifyColumns() {
 		assertManyToOne("throwable", ThrowableRecord.class);
 		shouldHaveId();
 		assertColumn("index", Integer.class).isRequired();
 		assertColumn("declaringClass", String.class).isRequired();
 		assertColumn("methodName", String.class).isRequired();
		assertColumn("fileName", String.class).isNullable();
		assertColumn("lineNumber", Integer.class).isNullable();
 	}
 
 	@Test
 	public void shouldAddElementToStackTrace() {
 		StackTraceElement[] stackTrace = new StackTraceElement[1];
 		((StackTraceElementRecord) dto).addTo(stackTrace);
 		assertDeepEquals(entity, stackTrace[0]);
 	}
 }
