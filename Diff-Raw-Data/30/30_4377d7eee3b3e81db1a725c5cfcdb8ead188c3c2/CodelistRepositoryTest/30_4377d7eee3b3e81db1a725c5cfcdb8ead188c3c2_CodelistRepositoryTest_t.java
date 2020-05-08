 package org.acme;
 
 import static java.util.Arrays.*;
 import static org.cotrix.domain.dsl.Codes.*;
 import static org.junit.Assert.*;
 
 import java.util.Collection;
import java.util.Collections;
 import java.util.HashSet;
 
 import javax.inject.Inject;
import javax.xml.namespace.QName;
 
 import org.cotrix.domain.Attribute;
 import org.cotrix.domain.Code;
 import org.cotrix.domain.Codelist;
 import org.cotrix.repository.CodelistRepository;
 import org.cotrix.repository.CodelistSummary;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import com.googlecode.jeeunit.JeeunitRunner;
 
 @RunWith(JeeunitRunner.class)
 public class CodelistRepositoryTest {
 
 	@Inject
 	CodelistRepository repository;
 	
 	@Test
 	public void summary() {
 		
 		Attribute a1 = attr().name("name1").value("v1").ofType("t1").in("l1").build();
 		Attribute a2 = attr().name("name2").value("v2").ofType("t2").build();
 		Attribute a3 = attr().name("name1").value("v3").ofType("t2").in("l1").build();
 		Attribute a4 = attr().name("name1").value("v1").ofType("t1").in("l2").build();
 		Attribute a5 = attr().name("name2").value("v2").ofType("t2").build();
 		
 		Attribute aa1 = attr().name("name1").value("v1").ofType("t3").in("l3").build();
 		Attribute aa2 = attr().name("name2").value("v2").ofType("t2").build();
 		Attribute aa3 = attr().name("name3").value("v3").ofType("t3").in("l2").build();
 		Attribute aa4 = attr().name("name1").value("v4").ofType("t1").in("l3").build();
 		
 		Code c1 = code().name("c1").attributes(a1,a2,a3).build();
 		Code c2 = code().name("name1").attributes(a4,a5).build();
 		
 		Codelist list = codelist().name("n").with(c1,c2).attributes(aa1,aa2,aa3,aa4).build();
 		
 		repository.add(list);
 		
 		CodelistSummary summary = repository.summary(list.id());
 		
 		assertEquals(q("n"), summary.name());
 		assertEquals(2, summary.size());
 		
 		
 		assertEqualSets(asList(q("name1"),q("name2"),q("name3")),summary.allNames());
 		assertEqualSets(asList(q("t1"),q("t2"),q("t3")),summary.allTypes());
 		assertEqualSets(asList("l1","l2","l3"),summary.allLanguages());
 		
 		assertEqualSets(asList(q("t1"),q("t2"),q("t3")),summary.allTypesFor(q("name1")));
 		assertEqualSets(asList("l1","l2","l3"),summary.allLanguagesFor(q("name1"),q("t1")));
		assertEqualSets(Collections.<QName>emptyList(),summary.allTypesFor(q("foo")));
		assertEqualSets(Collections.<String>emptyList(),summary.allLanguagesFor(q("foo"),q("boo")));
 		
 		assertEqualSets(asList(q("name1"),q("name2")),summary.codeNames());
 		assertEqualSets(asList(q("t1"),q("t2")),summary.codeTypesFor(q("name1")));
 		assertEqualSets(asList("l1","l2"),summary.codeLanguagesFor(q("name1"),q("t1")));
 		
 		
 		
		
 	}
 	
 	<T> void assertEqualSets(Collection<T> c1, Collection<T> c2) {
 		assertEquals(new HashSet<T>(c1),new HashSet<T>(c2));
 	}
 }
