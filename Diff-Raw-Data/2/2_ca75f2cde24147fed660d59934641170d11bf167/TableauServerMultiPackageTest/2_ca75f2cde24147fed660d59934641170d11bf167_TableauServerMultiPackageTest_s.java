 package edu.iastate.pdlreasoner.server;
 
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAllValues;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAnd;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAtom;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeNegation;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeOr;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makePackage;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeRole;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeSomeValues;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeTop;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URI;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.iastate.pdlreasoner.kb.KnowledgeBase;
 import edu.iastate.pdlreasoner.model.And;
 import edu.iastate.pdlreasoner.model.Atom;
 import edu.iastate.pdlreasoner.model.DLPackage;
 import edu.iastate.pdlreasoner.model.Or;
 import edu.iastate.pdlreasoner.model.Role;
 import edu.iastate.pdlreasoner.model.Top;
 
 public class TableauServerMultiPackageTest {
 
 	private DLPackage[] p;
 	private KnowledgeBase[] kb;
 	private TableauServer m_TableauServer;
 	
 	@Before
 	public void setUp() {
 		p = new DLPackage[3];
 		for (int i = 0; i < p.length; i++) {
 			p[i] = makePackage(URI.create("#package" + i));
 		}
 		
 		kb = new KnowledgeBase[p.length];
 		for (int i = 0; i < kb.length; i++) {
 			kb[i] = new KnowledgeBase(p[i]);
 		}
 		
 		m_TableauServer = new TableauServer();
 	}
 
 	@Test
 	public void empty() {
 		m_TableauServer.addKnowledgeBase(kb[0]);
 		m_TableauServer.addKnowledgeBase(kb[1]);
 		m_TableauServer.init();
 		assertTrue(m_TableauServer.isConsistent(p[0]));
 		assertTrue(m_TableauServer.isConsistent(p[1]));
 	}
 
 	@Test
 	public void paperExample1() {
 		Top p0Top = makeTop(p[0]);
 		Role r = makeRole(URI.create("#r"));
 		Atom p0C = makeAtom(p[0], URI.create("#C"));
 		
 		Atom p1D1 = makeAtom(p[1], URI.create("#D1"));
 		Atom p1D2 = makeAtom(p[1], URI.create("#D2"));
 		Atom p1D3 = makeAtom(p[1], URI.create("#D3"));
 		
 		kb[0].addAxiom(p0Top, p1D3);
 		
 		And bigAnd = makeAnd(
 				p1D1,
 				makeSomeValues(r, p0C),
 				makeAllValues(r, makeNegation(p[0], p0C))
 			);
 		Or bigOr = makeOr(bigAnd, makeNegation(p[1], p1D2));
 		kb[0].addAxiom(p0Top, bigOr);
 		
 		kb[1].addAxiom(p1D1, p1D2);
 		
 		for (int i = 0; i <= 1; i++) {
 			m_TableauServer.addKnowledgeBase(kb[i]);
 		}
 		
 		m_TableauServer.init();
 		assertTrue(m_TableauServer.isConsistent(p[0]));
 	}
 	
 	@Test
 	public void paperExample2() {
 		Atom p0A = makeAtom(p[0], URI.create("#A"));
 		Atom p0B = makeAtom(p[0], URI.create("#B"));
 		Atom p1C = makeAtom(p[1], URI.create("#C"));
 		Atom p2D = makeAtom(p[2], URI.create("#D"));
 		
 		kb[0].addAxiom(p0A, p0B);
 		kb[1].addAxiom(p0B, p1C);
 		kb[2].addAxiom(p1C, p2D);
 
 		for (int i = 0; i <= 2; i++) {
 			m_TableauServer.addKnowledgeBase(kb[i]);
 		}
 
 		m_TableauServer.init();
		assertTrue(m_TableauServer.isSubclassOf(p0A, p2D, p[0]));
 	}
 	
 }
 
