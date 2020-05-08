 package petrinetze.impl;
 
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import petrinetze.IArc;
 import petrinetze.IPetrinet;
 import petrinetze.IPlace;
 import petrinetze.ITransition;
 
 /**
  * 
  * @author Philipp Khn
  *
  */
 public class PetrinetzTest {
 
 	static IPetrinet p;
 	static IPetrinet p2;
 	static IPlace place1;
 	static IPlace place2;
 	static ITransition transition;
 	static SimpleListener listener;
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception 
 	{
 		p = new Petrinet();
 		listener = new SimpleListener();
 		p.addPetrinetListener(listener);
 		p2 = new Petrinet();
 	}
 	
 	@Test
 	public void createPlace()
 	{
 		place1 = p.createPlace("place1");
 		assertEquals(place1.getName(), "place1");
 		assertTrue(p.getAllPlaces().contains(place1));
 		assertTrue(p.getAllGraphElement().getAllNodes().contains(place1));
 		assertTrue(listener.AddedNodes.contains(place1));
 
 		place2 = p.createPlace("place2");
 		assertEquals(place2.getName(), "place2");
 		assertTrue(p.getAllPlaces().contains(place2));
 		assertTrue(p.getAllGraphElement().getAllNodes().contains(place2));
 		assertTrue(listener.AddedNodes.contains(place2));
 	}
 	
 	@Test
 	public void createTransition()
 	{
 		transition = p.createTransition("transition", new RenewCount());
 		assertEquals(transition.getName(), "transition");
 		assertTrue(p.getAllTransitions().contains(transition));
 		assertTrue(p.getAllGraphElement().getAllNodes().contains(transition));
 		assertEquals(new RenewCount(), transition.getRnw());
 		assertTrue(listener.AddedNodes.contains(transition));
 	}
 	
 	@Test
 	public void createArc()
 	{
 		IArc edge1 = p.createArc("edge1", place1, transition);
 		assertEquals("edge1", edge1.getName());
 		assertTrue(p.getAllArcs().contains(edge1));
 		assertTrue(p.getAllGraphElement().getAllArcs().contains(edge1));
 		assertEquals(place1, edge1.getStart());
 		assertEquals(transition, edge1.getEnd());
 		assertEquals(1, edge1.getMark());
 		assertTrue(listener.AddedEdges.contains(edge1));
 
 		IArc edge2 = p.createArc("edge2", transition, place2);
 		assertEquals("edge2", edge2.getName());
 		assertTrue(p.getAllArcs().contains(edge2));
 		assertTrue(p.getAllGraphElement().getAllArcs().contains(edge2));
 		assertEquals(transition, edge2.getStart());
 		assertEquals(place2, edge2.getEnd());
 		assertEquals(1, edge2.getMark());
 		assertTrue(listener.AddedEdges.contains(edge2));
 	}
 	
 	@Test
 	public void tokenFire()
 	{
 		place1.setMark(1);
 		assertEquals(1, place1.getMark());
 		assertEquals(0, place2.getMark());
 		p.fire(transition.getId());
 		assertEquals(0, place1.getMark());
 		assertEquals(1, place2.getMark());
 	}
 	
 	@Test
 	public void randomTokenFire()
 	{
 		IPlace P = p2.createPlace("P");
 		P.setMark(1);
 		ITransition a = p2.createTransition("a", new RenewCount());
 		ITransition b = p2.createTransition("b", new RenewCount());
 		p2.createArc("pa", P, a);
 		p2.createArc("pb", P, b);
 		p2.createArc("ap", a, P);
 		p2.createArc("bp", b, P);
 		a.setRnw(new RenewCount());
 		b.setRnw(new RenewCount());
 		
		int times = 1000000;
 		for(int i = 0; i < times; i++)
 			p2.fire();
 		
 		int distance = Math.abs(Integer.parseInt(a.getTlb()) - Integer.parseInt(b.getTlb()));
 		System.out.println(a.getTlb());
 		System.out.println(b.getTlb());
 		assertTrue("Variation must not be greater than 10%", distance < times * 0.1);
 		
 	}
 
 }
 
 
