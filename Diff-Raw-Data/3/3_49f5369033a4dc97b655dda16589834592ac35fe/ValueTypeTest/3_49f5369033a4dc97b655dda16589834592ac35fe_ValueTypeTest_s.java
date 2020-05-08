 package org.acme.codelists;
 
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.validation.Validators.*;
 import static org.junit.Assert.*;
 
 import org.acme.DomainTest;
 import org.cotrix.domain.values.ValueType;
 import org.junit.Test;
 
 public class ValueTypeTest extends DomainTest {
 	
 	@Test
 	public void canBeFluentlyConstructed() {
 	
 		ValueType minimal = valueType();
 		
 		//defaults
 		
 		assertNotNull(minimal.constraints().asSingleConstraint());
		assertFalse(minimal.isRequired());
 		assertTrue(minimal.isValid("anything"));
 		assertNull(minimal.defaultValue());
 
 		//a maximal sentence
 		ValueType type = valueType()
 				.with(min_length.instance("2"),max_length.instance("3"))
 				.required()
 				.defaultsTo("abc");
 		
 		assertNotNull(type.constraints());
 		assertNotNull(type.constraints().asSingleConstraint());
		assertTrue(type.isRequired());
 		assertFalse(type.isValid("1"));
 		assertTrue(type.isValid("12"));
 		assertEquals("abc",type.defaultValue());
 		assertTrue(type.isValid("123"));
 
 	}
 	
 }
