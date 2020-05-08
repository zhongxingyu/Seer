 package org.acme.codelists;
 
 import static org.acme.codelists.Fixture.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.links.OccurrenceRanges.*;
 import static org.cotrix.domain.links.ValueFunctions.*;
 import static org.cotrix.domain.trait.Status.*;
 import static org.junit.Assert.*;
 
 import javax.xml.namespace.QName;
 
 import org.acme.DomainTest;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.codelist.CodelistLink;
 import org.cotrix.domain.common.Attribute;
 import org.cotrix.domain.links.AttributeLink;
 import org.cotrix.domain.links.LinkOfLink;
 import org.cotrix.domain.links.NameLink;
 import org.cotrix.domain.memory.CodelistLinkMS;
 import org.cotrix.domain.utils.AttributeTemplate;
 import org.junit.Test;
 
 public class CodelistLinkTest extends DomainTest {
 
 	@Test
 	public void linksCanBeFluentlyConstructed() {
 
 		Codelist target = someCodelist();
 		Attribute a = someAttribute();
 		
 		CodelistLink newlink  = listLink().name(name).target(target).attributes(a).build();
 		
 		assertEquals(name,newlink.name());
 		assertEquals(target,newlink.target());
 		assertTrue(newlink.attributes().contains(a));
 
 		//defaults
 		assertEquals(NameLink.INSTANCE,newlink.valueType());
 		assertEquals(identity,reveal(newlink).state().function());
 		assertEquals(arbitrarily,reveal(newlink).state().range());
 		
 		//name-based
 		newlink  = listLink().name(name).target(target).anchorToName().build();
 		
 		assertEquals(NameLink.INSTANCE,newlink.valueType());
 		
 		//attribute-based
 		Attribute template = attribute().name(q("this")).ofType(q("that")).in("this").build();
 		
 		newlink  = listLink().name(name).target(target).anchorTo(template).build();
 		
 		assertEquals(new AttributeLink(new AttributeTemplate(template)),newlink.valueType());
 		
 		//link-based
 		CodelistLink linktemplate = listLink().name(name).target(target).anchorTo(template).build();
 		
 		newlink  = listLink().name(name).target(target).anchorTo(linktemplate).build();
 		
 		assertEquals(new LinkOfLink(linktemplate),newlink.valueType());
 		
 		
 		//value functions
 		newlink  = listLink().name(name).target(target).transformWith(lowercase).build();
 		
 		assertEquals(lowercase,reveal(newlink).state().function());
 		
 		newlink  = listLink().name(name).target(target).occurs(atmost(5)).build();
 		
 		assertEquals(atmost(5),reveal(newlink).state().range());
 	}
 	
 	@Test
 	public void changesetCanBeFluentlyConstructed() {
 
 		CodelistLink link;
 		
 		//name change
 		link = modifyListLink("1").name(name).build();
 		
 		//attribute change
 		link =  modifyListLink("1").attributes(someAttribute()).build();
 		
 		//value type change
 		link =  modifyListLink("1").anchorTo(someTemplate()).build();
 		link =  modifyListLink("1").anchorToName().build();
 		
 		CodelistLink linktemplate = listLink().name(name).target(someCodelist()).build();
 		link =  modifyListLink("1").anchorTo(linktemplate).build();
 		
 		//function change
 		link =  modifyListLink("1").transformWith(uppercase).build();
 		
 		//occurrence change
 		link =  modifyListLink("1").occurs(atmostonce).build();
 		
 		//full change
 		link =  modifyListLink("1")
 				.name(name)
 				.attributes(someAttribute())
 				.anchorTo(someTemplate())
 				.transformWith(lowercase)
 				.occurs(atmostonce)
 				.build();
 		
 		//changed
 		assertEquals(MODIFIED,((CodelistLink.Private) link).status());
 
 	}
 	
 	@Test
 	public void linksCanBeCloned() {
 		
 		CodelistLink fullLinkWithoutDefaults = like(listLink()
 							.name(name)
 							.target(someCodelist())
 							.anchorTo(someTemplate())
 							.transformWith(lowercase)
 							.occurs(once)
 							.attributes(someAttribute()).build());
 		
 		CodelistLink.State state = reveal(fullLinkWithoutDefaults).state();
 		
 		CodelistLinkMS clone = new CodelistLinkMS(state);
 
		assertEquals(clone,state);
 		
 		assertFalse(clone.id().equals(state.id()));
 		
 		//can persist a link
 		like(new CodelistLink.Private(clone));
 		
 	}
 	
 	@Test
 	public void linksCanChangeName() {
 		
 		CodelistLink link = like(listLink().name(name).target(someCodelist()).build());
 		
 		CodelistLink changeset = modifyListLink(link.id()).name(name2).build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertEquals(name2,link.name());
 		
 	}
 	
 	@Test
 	public void linksCanChangeValueType() {
 		
 		CodelistLink link = like(listLink().name(name).target(someCodelist()).build());
 		
 		Attribute template = someTemplate();
 		
 		CodelistLink changeset = modifyListLink(link.id()).anchorTo(template).build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertEquals(new AttributeLink(new AttributeTemplate(template)),link.valueType());
 		
 	}
 	
 	@Test
 	public void linksCanChangeValueTypeBackToDefault() {
 		
 		CodelistLink linktemplate = like(listLink().name(name).target(someCodelist()).build());
 		
 		CodelistLink link = like(listLink().name(name).target(someCodelist()).anchorTo(linktemplate).build());
 		
 		CodelistLink changeset = modifyListLink(link.id()).anchorToName().build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertEquals(NameLink.INSTANCE,link.valueType());
 		
 	}
 	
 	@Test
 	public void linksCanChangeRange() {
 		
 		CodelistLink link = like(listLink().name(name).target(someCodelist()).build());
 		
 		CodelistLink changeset = modifyListLink(link.id()).occurs(once).build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertEquals(once,link.range());
 		
 	}
 	
 	@Test
 	public void linksCanChangeFunction() {
 		
 		CodelistLink link = like(listLink().name(name).target(someCodelist()).build());
 		
 		CodelistLink changeset = modifyListLink(link.id()).transformWith(custom("'Hello $value'")).build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertEquals(custom("'Hello $value'"),link.function());
 		
 	}
 	
 	
 	
 	//helpers
 
 	private Codelist someCodelist(QName ... n) {
 		
 		return like(codelist().name(n.length>0?n[0]:name).build());
 	}
 	
 	private Attribute someAttribute(QName ... n) {
 		
 		return like(attribute().name(n.length>0?n[0]:name).ofType(q("that")).in("this").build());
 	}
 	
 	private Attribute someTemplate(QName ... n) {
 		
 		return attribute().name(n.length>0?n[0]:name).ofType(q("that")).in("this").build();
 	}
 
 
 }
