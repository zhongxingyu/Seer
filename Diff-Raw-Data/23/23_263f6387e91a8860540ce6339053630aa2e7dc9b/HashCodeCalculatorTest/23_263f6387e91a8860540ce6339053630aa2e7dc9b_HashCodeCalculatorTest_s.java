 package org.kalibro.core.abstractentity;
 
 import static org.junit.Assert.assertEquals;
 import static org.kalibro.core.abstractentity.HashCodeCalculator.hash;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.tests.UtilityClassTest;
 
 public class HashCodeCalculatorTest extends UtilityClassTest {
 
 	private Person carlos, cristina, isis, paulo;
 	private Programmer programmerCarlos, programmerPaulo;
 
 	@Before
 	public void setUp() {
 		carlos = loadFixture("carlos", Person.class);
 		cristina = loadFixture("cristina", Person.class);
 		isis = loadFixture("isis", Person.class);
 		paulo = loadFixture("paulo", Person.class);
 		programmerCarlos = loadFixture("carlos", Programmer.class);
 		programmerPaulo = loadFixture("paulo", Programmer.class);
 	}
 
 	@Override
 	protected Class<?> utilityClass() {
 		return HashCodeCalculator.class;
 	}
 
 	@Test
 	public void hashCodesShouldBeDistributed() {
 		assertDifferent(hash(carlos), hash(cristina), hash(isis), hash(paulo));
 	}
 
 	@Test
 	public void hashCodesShouldBeEqualForEqualObjects() {
 		assertEquals(hash(carlos), hash(programmerCarlos));
 		assertEquals(hash(paulo), hash(programmerPaulo));
 	}
 
 	@Test
 	public void hashCodesShouldBeEqualForNullIdentities() {
 		Person fulano = new Person(null, "Fulano", "M");
 		Person cicrana = new Person(null, "Cicrana", "F");
 		assertEquals(hash(fulano), hash(cicrana));
 	}
 }
