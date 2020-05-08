 package edu.iastate.pdlreasoner.master;
 
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAllValues;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAnd;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeAtom;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeNegation;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeOr;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makePackageID;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeRole;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeSomeValues;
 import static edu.iastate.pdlreasoner.model.ModelFactory.makeTop;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URI;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.iastate.pdlreasoner.PDLReasonerCentralizedWrapper;
 import edu.iastate.pdlreasoner.kb.Ontology;
 import edu.iastate.pdlreasoner.kb.OntologyPackage;
 import edu.iastate.pdlreasoner.kb.Query;
 import edu.iastate.pdlreasoner.model.And;
 import edu.iastate.pdlreasoner.model.Atom;
 import edu.iastate.pdlreasoner.model.Bottom;
 import edu.iastate.pdlreasoner.model.Concept;
 import edu.iastate.pdlreasoner.model.Negation;
 import edu.iastate.pdlreasoner.model.Or;
 import edu.iastate.pdlreasoner.model.PackageID;
 import edu.iastate.pdlreasoner.model.Role;
 import edu.iastate.pdlreasoner.model.Top;
 
 public class TableauMasterMultiPackagePaperExampleTest {
 
 	private PackageID[] pID;
 	private OntologyPackage[] p;
 	private Top[] top;
 	
 	@Before
 	public void setUp() {
 		pID = new PackageID[3];
 		for (int i = 0; i < pID.length; i++) {
 			pID[i] = makePackageID(URI.create("p" + i));
 		}
 		
 		p = new OntologyPackage[pID.length];
 		for (int i = 0; i < p.length; i++) {
 			p[i] = new OntologyPackage(pID[i]);
 		}
 		
 		top = new Top[pID.length];
 		for (int i = 0; i < p.length; i++) {
 			top[i] = makeTop(pID[i]);
 		}
 	}
 	
 	private boolean runQuery(Concept sub, Concept sup, PackageID witness) {
 		if (Bottom.INSTANCE.equals(sup)) {
 			return !runQuery(sub, witness);
 		} else {
 			Negation notSup = makeNegation(witness, sup);
 			Concept query = makeAnd(sub, notSup);
 			return !runQuery(query, witness);
 		}	
 	}
 	
 	private boolean runQuery(Concept satConcept, PackageID witness) {
 		Query query = new Query(new Ontology(p), null, satConcept, witness);
 		PDLReasonerCentralizedWrapper reasoner = new PDLReasonerCentralizedWrapper();
 		return reasoner.run(query).isSatisfiable();
 	}
 	
 	@Test
 	public void paperExample1() {
 		Top p0Top = makeTop(pID[0]);
 		Role r = makeRole(URI.create("#r"));
 		Atom p0C = makeAtom(pID[0], "C");
 		
 		Atom p1D1 = makeAtom(pID[1], "D1");
 		Atom p1D2 = makeAtom(pID[1], "D2");
 		Atom p1D3 = makeAtom(pID[1], "D3");
 		
 		p[0].addAxiom(p0Top, p1D3);
 		
 		And bigAnd = makeAnd(
 				p1D1,
 				makeSomeValues(r, p0C),
 				makeAllValues(r, makeNegation(pID[0], p0C))
 			);
		Or bigOr = makeOr(bigAnd, makeNegation(pID[0], p1D2));
 		p[0].addAxiom(p0Top, bigOr);
 		
 		p[1].addAxiom(p1D1, p1D2);
		p = new OntologyPackage[] {p[0], p[1]};
 		assertTrue(runQuery(top[0], pID[0]));
 	}
 	
 	@Test
 	public void paperExample2() {
 		Atom p0A = makeAtom(pID[0], "A");
 		Atom p0B = makeAtom(pID[0], "B");
 		Atom p1C = makeAtom(pID[1], "C");
 		Atom p2D = makeAtom(pID[2], "D");
 		
 		p[0].addAxiom(p0A, p0B);
 		p[1].addAxiom(p0B, p1C);
 		p[2].addAxiom(p1C, p2D);
 
 		assertTrue(runQuery(p0A, p2D, pID[2]));
 	}
 
 
 	@Test
 	public void paperExample3() {
 		Atom p0B = makeAtom(pID[0], "B");
 		Atom p0F = makeAtom(pID[0], "F");
 		Atom p1P = makeAtom(pID[1], "P");
 		
 		p[0].addAxiom(p0B, p0F);
 		p[1].addAxiom(p1P, p0B);
 		p[1].addAxiom(p1P, makeNegation(pID[1], p0F));
 		
 		assertFalse(runQuery(p1P, pID[1]));
 	}
 
 	@Test
 	public void paperExample4() {
 		Atom p0A = makeAtom(pID[0], "A");
 		Atom p0C = makeAtom(pID[0], "C");
 		Role p1r = makeRole(URI.create("#r"));
 		Atom p1B = makeAtom(pID[1], "B");
 		
 		p[0].addAxiom(p0A, p0C);
 		p[1].addAxiom(p0A, makeSomeValues(p1r, p1B));
 		p[1].addAxiom(p1B, makeAnd(p0A, makeNegation(pID[1], p0C)));
 		
 		assertTrue(runQuery(p0A, pID[0]));
 		assertFalse(runQuery(p0A, pID[1]));
 	}
 
 }
