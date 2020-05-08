 package edu.iastate.pdlreasoner.tableau;
 
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAllValues;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAtom;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeNegation;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makePackageID;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeRole;
 import static edu.iastate.pdlreasoner.tableau.TracedConcept.makeOrigin;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URI;
 import java.util.Collections;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.iastate.pdlreasoner.model.AllValues;
 import edu.iastate.pdlreasoner.model.Atom;
 import edu.iastate.pdlreasoner.model.Bottom;
 import edu.iastate.pdlreasoner.model.Negation;
 import edu.iastate.pdlreasoner.model.PackageID;
 import edu.iastate.pdlreasoner.model.Role;
 import edu.iastate.pdlreasoner.tableau.branch.BranchPointSet;
 import edu.iastate.pdlreasoner.tableau.graph.Edge;
 import edu.iastate.pdlreasoner.tableau.graph.Node;
 import edu.iastate.pdlreasoner.tableau.graph.TableauGraph;
 
 public class NodeTest {
 	
 	private Node m_Node;
 	private PackageID m_HomePackageID;
 	private PackageID m_ForeignPackageID;
 	private Role role;
 	private Atom atom;
 	private TracedConcept atomTC;
 	private AllValues all;
 	private TracedConcept allTC;
 	
 	@Before
 	public void setUp() throws Exception {
 		m_HomePackageID = makePackageID(URI.create("#package"));
 		m_ForeignPackageID = makePackageID(URI.create("#package2"));
 		TableauGraph g = new TableauGraph(m_HomePackageID);
		m_Node = g.makeNode(BranchPointSet.EMPTY);
		g.addRoot(m_Node);
 		role = makeRole(URI.create("#role"));
 		
 		atom = makeAtom(m_HomePackageID, "atom");
 		atomTC = makeOrigin(atom);
 		all = makeAllValues(role, atom);
 		allTC = makeOrigin(all);
 	}
 
 	@Test
 	public void testAddAndRemove() {
 		assertFalse(m_Node.containsChild(role, atom));
 		assertEquals(Collections.EMPTY_SET, m_Node.getChildrenWith(role));
 		
 		Node child = m_Node.addChildBy(role, BranchPointSet.EMPTY);
 		assertEquals(BranchPointSet.EMPTY, child.getDependency());
 		
 		child.addLabel(atomTC);
 		assertTrue(m_Node.containsChild(role, atom));
 		assertFalse(m_Node.containsChild(role, all));
 		assertEquals(Collections.singleton(Edge.make(m_Node, role, child)), m_Node.getChildrenWith(role));
 		
 		child.removeFromParent();
 		assertFalse(m_Node.containsChild(role, atom));
 	}
 
 	@Test
 	public void testAddAndContainsLabel() {
 		assertFalse(m_Node.containsLabel(atom));
 		assertNull(m_Node.getTracedConceptWith(atom));
 		assertTrue(m_Node.addLabel(atomTC));
 		assertFalse(m_Node.addLabel(atomTC));
 		assertTrue(m_Node.containsLabel(atom));
 		assertEquals(atomTC, m_Node.getTracedConceptWith(atom));
 	}
 
 	@Test
 	public void testIsComplete() {
 		assertTrue(m_Node.isComplete());
 		m_Node.addLabel(atomTC);
 		assertFalse(m_Node.isComplete());
 	}
 
 	@Test
 	public void testHasClash_Bottom() {
 		assertTrue(m_Node.getClashCauses().isEmpty());
 		m_Node.addLabel(makeOrigin(Bottom.INSTANCE));
 		assertEquals(Collections.singleton(BranchPointSet.EMPTY), m_Node.getClashCauses());
 		
 		m_Node.clearClashCauses();
 		assertTrue(m_Node.getClashCauses().isEmpty());
 	}
 
 	@Test
 	public void testHasClash_Atom1() {
 		Negation homeNotAtom = makeNegation(m_HomePackageID, atom);
 		Negation foreignNotAtom = makeNegation(m_ForeignPackageID, atom);
 		
 		m_Node.addLabel(atomTC);
 		m_Node.addLabel(makeOrigin(foreignNotAtom));
 		assertTrue(m_Node.getClashCauses().isEmpty());
 		
 		m_Node.addLabel(makeOrigin(homeNotAtom));
 		assertEquals(Collections.singleton(BranchPointSet.EMPTY), m_Node.getClashCauses());
 	}
 
 	@Test
 	public void testHasClash_Atom2() {
 		Negation homeNotAtom = makeNegation(m_HomePackageID, atom);
 		m_Node.addLabel(makeOrigin(homeNotAtom));
 		m_Node.addLabel(atomTC);
 		assertEquals(Collections.singleton(BranchPointSet.EMPTY), m_Node.getClashCauses());
 	}
 
 	@Test
 	public void testHasClash_All() {
 		Negation homeNotAll = makeNegation(m_HomePackageID, all);
 		m_Node.addLabel(makeOrigin(homeNotAll));
 		m_Node.addLabel(allTC);
 		//Only NNF support
 		assertTrue(m_Node.getClashCauses().isEmpty());
 	}
 }
