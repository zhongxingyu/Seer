 package alice.tuprolog;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class AtomLengthTest {
 	
 	Prolog engine;
 	
 	@Before
 	public void setUp() {
 		engine = new Prolog();
 	}
 	
 	@Test public void simpleAtom() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length('enchanted evening', N).");
 		assertTrue(solution.isSuccess());
 		assertEquals(new Int(17), solution.getTerm("N"));
 	}
 	
 	@Test public void atomWithNewline() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length('enchanted\\\n evening', N).");
 		assertTrue(solution.isSuccess());
 		assertEquals(new Int(17), solution.getTerm("N"));
 	}
 	
 	@Test public void emptyAtom() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length('', N).");
 		assertTrue(solution.isSuccess());
 		assertEquals(new Int(0), solution.getTerm("N"));
 	}
 	
 	@Test public void wrongLength() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length('scarlet', 5).");
 		assertFalse(solution.isSuccess());
 	}
 	
 	@Test public void atomAsVariable() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length(Atom, 4).");
 		assertFalse(solution.isSuccess());
 		// TODO Should throw instantiation_error
 	}
 	
 	@Test public void atomAsNumber() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length(1.23, 4).");
 		assertFalse(solution.isSuccess());
 		// TODO Should throw type_error(atom, 1.23)
 	}
 	
 	@Test public void lengthAsAtom() throws PrologException {
 		SolveInfo solution = engine.solve("atom_length(atom, '4').");
 		assertFalse(solution.isSuccess());
 		// TODO Should throw type_error(integer, 4)
 	}
 
 }
