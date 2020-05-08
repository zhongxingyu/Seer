 import junit.framework.TestCase;
 import pd.spielerverwaltung.Spieler;
 import pd.zugsystem.BankFeld;
 import pd.zugsystem.Feld;
 import pd.zugsystem.Spiel;
 
 public abstract class ProblemDomainTestCase extends TestCase {
 	protected Spiel spiel;
 	protected Spieler spieler;
 	protected BankFeld bankFeld;
 	protected Feld zielFeld;
 	
 	protected void setUp() throws Exception {
 		spiel = new Spiel();
 		
 		for (int i = 0; i < 4; ++i) {
 			spiel.fuegeHinzu(new Spieler("Spieler" + i));
 		}
 		spieler = spiel.getSpieler().get(0);
 		
 		spiel.brettAufstellen();
 		
 		bankFeld = spiel.getBrett().getBankFeldVon(spieler);
 		bankFeld.setFigur(spieler.getFiguren().get(0));
 		
 		zielFeld = bankFeld;
		for (int i = 0; i < 3; ++i) {
 			zielFeld = zielFeld.getNaechstes();
 		}
 	}
 
 }
