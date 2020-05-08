 package petrinetze.test;
 
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import petrinetze.IArc;
 import petrinetze.IPetrinet;
 import petrinetze.IPlace;
 import petrinetze.ITransition;
 import petrinetze.impl.Petrinet;
 import petrinetze.impl.RenewCount;
 
 public class PetrinetzTest {
 
 	IPetrinet p;
 	IPetrinet p2;
 	IPlace place1;
 	IPlace place2;
 	ITransition transition;
 	@Before
 	public void setUp() throws Exception 
 	{
 		p = new Petrinet();
 		p2 = new Petrinet();
 	}
 	
 	@Test
 	public void createPlace()
 	{
 		place1 = p.createPlace("place1");
 		assertEquals(place1.getName(), "place1");
 		assertTrue(p.getAllPlaces().contains(place1));
 		assertTrue(p.getAllGraphElement().getAllNodes().contains(place1));
 
 		place2 = p.createPlace("place2");
 		assertEquals(place2.getName(), "place2");
 		assertTrue(p.getAllPlaces().contains(place2));
 		assertTrue(p.getAllGraphElement().getAllNodes().contains(place2));
 	}
 	
 	@Test
 	public void createTransition()
 	{
 		transition = p.createTransition("transition");
		assertEquals(transition.getName(), "transition");
 		assertTrue(p.getAllTransitions().contains(transition));
 		assertTrue(p.getAllGraphElement().getAllNodes().contains(transition));
 	}
 	
 	@Test
 	public void createArc()
 	{
 		IArc edge1 = p.createArc("edge1");
 		assertEquals("edge1", edge1.getName());
 		assertTrue(p.getAllArcs().contains(edge1));
 		assertTrue(p.getAllGraphElement().getAllArcs().contains(edge1));
 		assertTrue(edge1.getEnd() == null);
 		assertTrue(edge1.getStart() == null);
 		edge1.setStart(place1);
 		edge1.setEnd(transition);
 		assertEquals(edge1.getStart(), place1);
 		assertEquals(edge1.getEnd(), transition);
 
 		IArc edge2 = p.createArc("edge2");
 		assertEquals("edge2", edge2.getName());
 		assertTrue(p.getAllArcs().contains(edge2));
 		assertTrue(p.getAllGraphElement().getAllArcs().contains(edge2));
 		assertTrue(edge2.getEnd() == null);
 		assertTrue(edge2.getStart() == null);
 		edge2.setStart(transition);
 		edge2.setEnd(place2);
 		assertEquals(edge2.getStart(), transition);
 		assertEquals(edge2.getEnd(), place2);
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
 		ITransition a = p2.createTransition("a");
 		ITransition b = p2.createTransition("b");
 		IArc pa = p2.createArc("pa");
 		pa.setStart(P);
 		pa.setEnd(a);
 		IArc pb = p2.createArc("pb");
 		pb.setStart(P);
 		pb.setEnd(b);
 		IArc ap = p2.createArc("ap");
 		ap.setStart(a);
 		ap.setEnd(P);
 		IArc bp = p2.createArc("bp");
 		bp.setStart(bp);
 		bp.setEnd(P);
 		a.setRnw(new RenewCount());
 		b.setRnw(new RenewCount());
 		
 		int times = 10000;
 		for(int i = 0; i < times; i++)
 			p2.fire();
 		
 		int distance = Math.abs(Integer.parseInt(a.getTlb()) - Integer.parseInt(b.getTlb()));
 		assertTrue("Variation must not be greater than 10%", distance < times * 0.1);
 		
 	}
 
 }
