 package org.acme.codelists;
 
 import static org.junit.Assert.*;
 import static org.acme.codelists.Fixture.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.trait.Status.*;
 
 import org.acme.DomainTest;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.common.Attribute;
 import org.cotrix.domain.memory.CodeMS;
 import org.junit.Test;
 
 public class CodeTest extends DomainTest {
 
 	@Test
 	public void codesCanBeFluentlyConstructed() {
 		
 		Code code = code().name(name).build();
 		
 		assertEquals(name,code.name());
 		
 		Attribute a = attribute().name(name).value(value).ofType(type).build();
 		
 		code = code().name(name).attributes(a).build();
 		
 		assertTrue(code.attributes().contains(a));
 	}
 	
 	
 	@Test
 	public void changesetsCanBeFluentlyConstructed() {
 		
 		
 		Attribute a = attribute().name("name").build();
 		Attribute added = attribute().name("name").build();
 		Attribute modified = modifyAttribute("1").name("name").build();
 		Attribute deleted = deleteAttribute("1");
 		
 		Code c;
 
 		//new codes
 		c = code().name(name).build();
 		c = code().name(name).attributes(a).build();
 
 		assertEquals(null,reveal(c).status());
 		
 		
 		c = modifyCode("1").attributes(modified,added,deleted).build();
 		
 		//changed
 		assertEquals(MODIFIED,reveal(c).status());
 		
 		c = deleteCode("1");
 
 		assertEquals(DELETED,reveal(c).status());
 	}
 	
 	
 	@Test
 	public void cloned() {
 		
 		Attribute a = attribute().name(name).value(value).ofType(type).in(language).build();
 		Code c = like(code().name(name).attributes(a).build());
 		
 		Code.State state = reveal(c).state();
 		CodeMS clone = new CodeMS(state);
 		
		assertEquals(clone,state);
 		
 	}
 	
 	@Test
 	public void changeName() {
 
 		Code code = like(code().name(name).build());
 		
 		Code changeset = modifyCode(code.id()).name(name2).build();
 		
 		reveal(code).update(reveal(changeset));
 		
 		assertEquals(name2,code.name());
 		
 	}
 
 
 	@Test
 	public void addAttribute() {
 
 		Code code = like(code().name(name).build());
 		
 		Attribute added = attribute().name(name).build();
 
 		assertFalse(code.attributes().contains(added));
 		
 		Code changeset = modifyCode(code.id()).attributes(added).build();
 		
 		reveal(code).update(reveal(changeset));
 		
 		assertTrue(code.attributes().contains(added));
 		
 	}
 	
 	@Test
 	public void removeAttribute() {
 
 		Attribute a = attribute().name(name).build();
 		
 		Code code = like(code().name(name).attributes(a).build());
 		
 		assertTrue(code.attributes().contains(a));
 		
 		Attribute deleted = deleteAttribute(a.id());
 
 		Code changeset = modifyCode(code.id()).attributes(deleted).build();
 		
 		reveal(code).update(reveal(changeset));
 
 		assertFalse(code.attributes().contains(a));
 		
 	}
 	
 	@Test
 	public void updateAttribute() {
 
 		Attribute a = attribute().name(name).build();
 		
 		Code code = like(code().name(name).attributes(a).build());
 		
 		assertTrue(code.attributes().contains(a));
 		
 		Attribute modified = modifyAttribute(a.id()).name(name2).build();
 
 		Code changeset = modifyCode(code.id()).attributes(modified).build();
 		
 		reveal(code).update(reveal(changeset));
 
 		assertNotNull(code.attributes().lookup(name2));
 		
 	}
 }
