 package com.scarytom;
 
 import static com.google.common.collect.Sets.newHashSet;
 import static junit.framework.Assert.assertEquals;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.google.common.collect.Sets;
 import org.junit.Test;
 
 import com.scarytom.pronet.Programmer;
 
 public class UndirectedGraphBuilderTest {
 	@Test
 	public void createsGraphWithTwoNodes() throws Exception {
 		Programmer recommendee = new Programmer("davie", new HashSet<String>());
 		
 		Programmer tom = new Programmer("tom", Sets.<String>newHashSet());
         tom.addRecommendation(recommendee);
 		
 		UndirectedGraphBuilder graphBuilder = new UndirectedGraphBuilder(newHashSet(recommendee, tom));
 		Set<Programmer> recommenders = graphBuilder.whoRecommends(recommendee);
 		assertEquals(newHashSet(tom), recommenders);
 	}
 	
 	@Test
 	public void canRecommendMikeInstead() throws Exception {
 		Programmer recommendee = new Programmer("mike", new HashSet<String>());
 		
 		Programmer tom = new Programmer("tom", new HashSet<String>());
 		tom.addRecommendation(recommendee);
 		
 		UndirectedGraphBuilder graphBuilder = new UndirectedGraphBuilder(newHashSet(recommendee, tom));
 		Set<Programmer> recommenders = graphBuilder.whoRecommends(recommendee);
 		assertEquals(newHashSet(tom), recommenders);
 	}
 }
