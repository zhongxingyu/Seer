 import java.util.Vector;
 
 import pd.zugsystem.Feld;
 
 
 public class FeldTest extends ProblemDomainTestCase {
 	public void testNaechstesVorheriges() {
 		Feld feld = bankFeld.getNaechstes().getVorheriges();
 		assertEquals(bankFeld, feld);
 	}
 	
 	public void testWeg() {
 		Vector<Feld> weg = bankFeld.getWeg(zielFeld);
		assertEquals(4, weg.size());
 		assertEquals(bankFeld, weg.get(0));
		assertEquals(zielFeld, weg.get(3));
 	}
 }
