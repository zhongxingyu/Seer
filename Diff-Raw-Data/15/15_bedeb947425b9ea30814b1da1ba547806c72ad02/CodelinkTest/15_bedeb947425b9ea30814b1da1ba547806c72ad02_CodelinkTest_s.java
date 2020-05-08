 package org.acme.codelists;
 
 import static org.acme.codelists.Fixture.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.cotrix.domain.trait.Status.*;
 import static org.cotrix.domain.utils.Constants.*;
 import static org.cotrix.domain.values.ValueFunctions.*;
 import static org.junit.Assert.*;
 
 import java.util.Arrays;
 import java.util.HashMap;
 
 import javax.xml.namespace.QName;
 
 import org.acme.DomainTest;
 import org.cotrix.domain.attributes.Attribute;
 import org.cotrix.domain.codelist.Code;
 import org.cotrix.domain.codelist.Codelink;
 import org.cotrix.domain.codelist.Codelist;
 import org.cotrix.domain.codelist.LinkDefinition;
 import org.cotrix.domain.memory.CodelinkMS;
 import org.junit.Test;
 
 public class CodelinkTest extends DomainTest {
 
 	@Test
 	public void linksCanBeFluentlyConstructed() {
 		
 		Code code = someTarget();
 		LinkDefinition listLink = someCodelistLink();
 		
 		Attribute a = attribute().name(name).value(value).ofType(type).in(language).build();
 		
 		Codelink link  = like(link().instanceOf(listLink).target(code).attributes(a).build());
 		
 		assertEquals(listLink,link.definition());
 		assertEquals(link.definition().qname(),link.qname());
 		
 		assertEquals(a,link.attributes().lookup(a.qname()));
 	}
 	
 	@Test
 	public void changesetCanBeFluentlyConstructed() {
 
 		Codelink link = modifyLink("1").target(someTarget()).build();
 		
 		assertEquals(MODIFIED,((Codelink.Private) link).status());
 
 	}
 
 	@Test
 	public void resolveLinkToNames() {
 		
 		
 		Code code = like(code().name("a").build());
 		Codelist list = like(codelist().name("A").build());  
 		
 		LinkDefinition listLink = like(linkdef().name("link").target(list).transformWith(lowercase).anchorToName().build());
 		
 		Codelink link  = like(link().instanceOf(listLink).target(code).build());
 		
 		assertEquals(new QName(code.qname().getNamespaceURI(),lowercase.apply(code.qname().getLocalPart())),link.value().iterator().next());
 
 	}
 	
 	@Test
 	public void resolveLinkToAttributes() {
 		
 		
 		Attribute a = like(attribute().name(name).value(value).ofType(DEFAULT_TYPE).in(language).build());
 		Code code = like(code().name("b").attributes(a).build());
 		Codelist list = like(codelist().name("B").with(code).build());  
 
 		Attribute template = attribute().name(name).ofType(NULL_QNAME).build();
 		
 		LinkDefinition listLink = like(linkdef().name("link").target(list).anchorTo(template).transformWith(lowercase).build());
 		Codelink link  = like(link().instanceOf(listLink).target(code).build());
 		
 		assertEquals(Arrays.asList(lowercase.apply(a.value())),link.value());
 		
 	}
 	
 	
 	@Test
 	public void resolveLinkToAttributesWithFunction() {
 		
 		
 		Attribute a = like(attribute().name(name).value(value).ofType(DEFAULT_TYPE).in(language).build());
 		Code code = like(code().name("b").attributes(a).build());
 		Codelist list = like(codelist().name("B").with(code).build());  
 
 		Attribute template = attribute().name(name).ofType(NULL_QNAME).build();
 		
 		LinkDefinition listLink = like(linkdef().name("link").target(list).anchorTo(template).build());
 		Codelink link  = like(link().instanceOf(listLink).target(code).build());
 		
 		assertEquals(Arrays.asList(a.value()),link.value());
 		
 	}
 	
 	@Test
 	public void resolveLinkToLinks() {
 		
 		
 		Code code = like(code().name("c").build());
 		Codelist list = like(codelist().name("C").build());  
 		
 		LinkDefinition listlink = like(linkdef().name("link-to-c").target(list).build());
 		
 		Codelink link = like(link().instanceOf(listlink).target(code).build());
 		code = like(code().name("b").links(link).build());
 		list = like(codelist().name("B").links(listlink).with(code).build());
 		
 		
 		listlink = like(linkdef().name("link-to-b").target(list).anchorTo(listlink).build());
 		link = like(link().instanceOf(listlink).target(code).build());
 		
 		assertEquals(q("c"),link.value().iterator().next());
 		
 	}
 	
 	@Test
 	public void cloned() {
 		
 		Attribute a = attribute().name(name).value(value).ofType(type).in(language).build();
 		
 		Codelink link = link().instanceOf(someCodelistLink()).target(someTarget()).attributes(a).build();
 		
 		Codelink.State state = reveal(link).state();
		CodelinkMS clone = new CodelinkMS(state,new HashMap<String, Object>());
 
 		assertEquals(state.qname(),clone.qname());
 		
 		for (Attribute.State attr : clone.attributes()) {
 			//System.out.println(attr.name());
 			assertTrue(clone.attributes().contains(attr.qname()));
 		}
 		
 		assertEquals(clone.id(),state.id());
 		
 	}
 	
 	
 	@Test
 	public void changeLinkGroup() {
 		
 		Attribute a = attribute().name(name).value(value).ofType(type).in(language).build();
 		
 		Code code1 = someTarget("t1");
 		Code code2 = someTarget("t2");
 		Code code3 = someTarget("t3");
 		Code code4 = someTarget("t4");
 		Code code5 = someTarget("t5");
 		
 		LinkDefinition listLink = someCodelistLink();
 		
 		Codelink link1  = link().instanceOf(listLink).target(code1).build();
 		Codelink link2  = link().instanceOf(listLink).target(code1).build();
 		Codelink link3  = link().instanceOf(listLink).target(code3).build();
 		Codelink link4  = link().instanceOf(listLink).target(code3).build();
 		Codelink link5  = link().instanceOf(listLink).target(code5).build();
 		
 		Code code = like(code().name("s").links(link1,link2,link3,link4,link5).build());
 		
 		//first group member changes target
 		Codelink delta1 = modifyLink(link1.id()).target(code2).build();
 		//non-first group member changes target and other
 		Codelink delta4 = modifyLink(link4.id()).target(code4).attributes(a).build();
 		//an isolated link changes an attribute
 		Codelink delta5 = modifyLink(link5.id()).attributes(a).build();
 		
 		Code delta = modifyCode(code.id()).links(delta1,delta4,delta5).build();
 				
 		reveal(code).update(reveal(delta));
 		
 		//verify expectations
 		for (Codelink link : code.links()) {
 			
 			//normal update
 			if (link.id().equals(link1.id()))
 				assertEquals(code2.id(),link.target().id());
 		
 			//group semantics
 			else if (link.id().equals(link2.id()))
 				assertEquals(code2.id(),link.target().id());
 			
 			//normal update
 			else if (link.id().equals(link4.id())) {
 				assertEquals(code4.id(),link.target().id());
 				assertTrue(link.attributes().contains(a.qname()));
 			}	
 			
 			//group semantics
 			else if (link.id().equals(link3.id())) {
 				assertEquals(code4.qname(),link.target().qname());
 				assertFalse(link.attributes().contains(a.qname()));
 			}	
 				
 			//untouched
 			else if (link.id().equals(link5.id())) {
 				assertEquals(code5.id(),link.target().id());
 				assertTrue(link.attributes().contains(a.qname()));
 			}
 		}
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public void linkGroupMustChangeConsistently() {
 		
 		Code code1 = someTarget("t1");
 		Code code2 = someTarget("t2");
 		Code code3 = someTarget("t3");
 		
 		
 		LinkDefinition listLink = someCodelistLink();
 		
 		Codelink link1  = link().instanceOf(listLink).target(code1).build();
 		Codelink link2  = link().instanceOf(listLink).target(code1).build();
 		
 		Code code = like(code().name("s").links(link1,link2).build());
 		
 		//inconsistent changes
 		Codelink linkchange1 = modifyLink(link1.id()).target(code2).build();
 		Codelink linkchange3 = modifyLink(link2.id()).target(code3).build();
 		
 		Code codechange = modifyCode(code.id()).links(linkchange1,linkchange3).build();
 				
 		try {
 			reveal(code).update(reveal(codechange));
 			fail();
 		}
 		catch(IllegalArgumentException e) {}
 		
 		//enforce consistency
 		linkchange3 = modifyLink(link2.id()).target(code2).build();
 		
 		//doesn't fail
 		reveal(code).update(reveal(codechange));
 		
 	}
 	
 	@Test(expected=IllegalStateException.class)
 	public void  linkCyclesAreDetected() {
 
 		//two codelists one code each
 		Code code1 = someTarget("c1");
 		Codelist list1 = like(codelist().name("list1").with(code1).build());
 
 		Code code2 = someTarget("c2");
 		Codelist list2 = like(codelist().name("list2").with(code2).build());
 
 		//first links to second's name
 		LinkDefinition clLink1 = linkdef().name("1-2").target(list2).build();
 		Codelist changeset = modifyCodelist(list1.id()).links(clLink1).build();
 		reveal(list1).update(reveal(changeset));
 		
 		//second links to first's link
 		LinkDefinition clLink2 = linkdef().name("2-1").target(list1).anchorTo(clLink1).build();
 		changeset = modifyCodelist(list2.id()).links(clLink2).build();
 		reveal(list2).update(reveal(changeset));
 		
 		//modify first link to point to second link (allowed)
 		changeset = modifyCodelist(list1.id()).links(
 				modifyLinkDef(clLink1.id()).anchorTo(clLink2).build()
 		).build();
 		
 		reveal(list1).update(reveal(changeset));
 		
 		//create cycle in instances
 		Codelink link1 = link().instanceOf(clLink1).target(code2).build();
 		Codelink link2 = link().instanceOf(clLink2).target(code1).build();
 		
 		changeset = modifyCodelist(list1.id()).with(
 					modifyCode(code1.id()).links(link1).build()
 				).build();
 
 		reveal(list1).update(reveal(changeset));
 		
 		changeset = modifyCodelist(list2.id()).with(
 				modifyCode(code2.id()).links(link2).build()
 			).build();
 
 		reveal(list2).update(reveal(changeset));
 			
 		//detect cycle
 		code1.links().lookup(link1.qname()).value();
 		
 		
 		
 		
 		
 	}
 	
 	@Test
 	public void changeTarget() {
 		
 		
 		Code code = someTarget();
 		
 		LinkDefinition listLink = someCodelistLink();
 		
 		Codelink link  = link().instanceOf(listLink).target(code).build();
 		
 		code = someTarget("new");
 		
 		Codelink changeset = modifyLink(link.id()).target(code).build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertEquals(code.qname(),link.value().iterator().next());
 		
 	}
 	
 	@Test
 	public void changeAttribute() {
 		
 		
 		Code code = someTarget();
 		
 		LinkDefinition listLink = someCodelistLink();
 		
 		Codelink link  = link().instanceOf(listLink).target(code).build();
 		
 		Attribute a = attribute().name(name).value(value).ofType(type).in(language).build();
 		
 		Codelink changeset = modifyLink(link.id()).attributes(a).build();
 		
 		reveal(link).update(reveal(changeset));
 		
 		assertNotNull(link.attributes().lookup(a.qname()));
 		
 	}
 	
 	private LinkDefinition someCodelistLink() {
 		
 		return like(linkdef().name("link").target(someCodelist()).build());
 	}
 	
 	private Codelist someCodelist() {
 		
 		return like(codelist().name("name").build());
 	}
 
 	private Code someTarget() {
 		
 		return someTarget("name");
 	}
 
 	private Code someTarget(String name) {
 		
 		return like(code().name(name).build());
 	}
 
 
 }
