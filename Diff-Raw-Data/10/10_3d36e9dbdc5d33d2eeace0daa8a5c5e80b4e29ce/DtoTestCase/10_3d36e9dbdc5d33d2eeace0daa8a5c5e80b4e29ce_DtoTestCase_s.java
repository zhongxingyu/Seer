 package org.kalibro;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.Collection;
 
 import org.junit.Test;
 import org.kalibro.core.model.abstracts.AbstractEntity;
 import org.kalibro.core.util.DataTransferObject;
 import org.powermock.reflect.Whitebox;
 
 public abstract class DtoTestCase<ENTITY, RECORD extends DataTransferObject<ENTITY>> extends KalibroTestCase {
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void defaultConstructorShouldDoNothing() throws Exception {
 		RECORD record = newDtoUsingDefaultConstructor();
 		for (Field field : record.getClass().getDeclaredFields())
			if (!Modifier.isStatic(field.getModifiers()))
				assertNull(Whitebox.getInternalState(record, field.getName()));
 	}
 
 	protected abstract RECORD newDtoUsingDefaultConstructor();
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void checkCorrectConversion() {
 		for (ENTITY entity : entitiesForTestingConversion())
 			assertCorrectConversion(entity, createDto(entity).convert());
 	}
 
 	protected abstract Collection<ENTITY> entitiesForTestingConversion();
 
 	protected abstract RECORD createDto(ENTITY entity);
 
 	protected void assertCorrectConversion(ENTITY original, ENTITY converted) {
 		assertDeepEquals((AbstractEntity<?>) original, (AbstractEntity<?>) converted);
 	}
 }
