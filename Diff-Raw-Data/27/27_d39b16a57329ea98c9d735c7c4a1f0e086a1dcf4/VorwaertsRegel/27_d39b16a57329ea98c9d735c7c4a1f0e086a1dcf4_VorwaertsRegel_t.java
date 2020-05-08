 package pd.regelsystem;
 
 import pd.zugsystem.Bewegung;
 import pd.zugsystem.Feld;
 import pd.zugsystem.Zug;
 
 public class VorwaertsRegel extends Regel {
 	private int schritte;
 	
 	public VorwaertsRegel(int schritte) {
 		this.schritte = schritte;
 	}
 
 	public Regel validiere(Zug zug) {
 		Bewegung bewegung = zug.getBewegung();
 		Feld start = bewegung.getStart();
 		
 		/* TODO: Ist das bei allen Regeln so? -> Auslagern */
 		if (start.getFigur().getSpieler() != zug.getSpieler()) {
 			return null;
 		}
 		
		if (start.getWeg(bewegung.getZiel()).size()-1 != schritte) {
			System.out.println(start.getWeg(bewegung.getZiel()).size() + " " + schritte);
 			return null;
 		}
 		
 		return this;
 	}
 
 	/* TODO: Andere Figur auf Ziel heim schicken */
 	public void ausfuehren(Zug zug) {
 		Feld start = zug.getBewegung().getStart();
 		Feld ziel  = zug.getBewegung().getZiel();
 		start.versetzeFigurAuf(ziel);
 	}
 
 }
