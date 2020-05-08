 package ambiorix;
 
 import java.util.Vector;
 
 import ambiorix.acties.Actie;
 import ambiorix.acties.ActieBestuurder;
 import ambiorix.spelbord.BordPositie;
 import ambiorix.spelbord.Pion;
 import ambiorix.spelbord.PionTypeVerzameling;
 import ambiorix.spelbord.Spelbord;
 import ambiorix.spelbord.Tegel;
 import ambiorix.spelbord.Terrein;
 import ambiorix.spelers.Antwoord;
 import ambiorix.spelers.Speler;
 
// TODO_S eigelijk met reflectie via uitbreidingen
import ambiorix.acties.basisspel.*;

 public class SpelToolkit {
 	private Vector<Speler> spelers;
 	private Spelbord spelbord;
 	private ActieBestuurder actiebestuurder;
 
 	public SpelToolkit(Vector<Speler> spelers) {
 		this.spelers = spelers;
 		actiebestuurder = new ActieBestuurder();
 		// spelbord = new Spelbord();
 	}
 	
 	@Deprecated
 	public ActieBestuurder getActiebestuurder() {
 		return actiebestuurder;
 	}
 	
 	// van Actiebestuurder	
 	
 	public void start(Actie start) {
 		actiebestuurder.start(start);
 	}
 	
 	public void stop() {
 		actiebestuurder.stop();
 	}
 
 	// van Spel
 	
 	public int getAantalSpelers() {
 		return spelers.size();
 	}
 
 	public Vector<Speler> getSpelers() {
 		return spelers;
 	}	
 
 	public Speler getActieveSpeler() {
 		for(Speler s : spelers) {
 			if(s.isActief())
 				return s;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Hier gaat een alternatief voor komen (volgendeBeurt of zo).
 	 */
 	@Deprecated
 	public void setActieveSpeler(Speler actieveSpeler) {
 		Speler nuActief = getActieveSpeler();
 		nuActief.zetActief(false);
 		actieveSpeler.zetActief(true);
 	}
 
 	// van Spelbord
 	
 	public Tegel getVolgendeTegel() {
 		return spelbord.getVolgendeTegel();
 	}
 	
 	// van typeverzamelingen
 	
 	public Pion getPion(String piontype) {
 		return new Pion(0,PionTypeVerzameling.getInstantie().getType(piontype));
 	}
 
 	public void setTegelAantal(String tegelType, int hoeveelheid) {
 		spelbord.setTegelAantal(tegelType, hoeveelheid);
 	}
 	
 	// Van Speler
 	//  1) Input
 	public BordPositie selecteerBordPositie(Speler s) {
 		Antwoord a = s.selecteerBordPositie();
 		return a.getPosities().get(0);
 	}
 
 	//public void positieToestaan(boolean toegestaan, BordPositie b);
 	
 	public Tegel selecteerSpelerTegel(Speler s) {
 		Antwoord a = s.selecteerSpelerTegel();
 		return a.getTegels().get(0);
 	}
 	
 
 	public Terrein selecteerTegelGebied(Speler s) {
 		Antwoord a = s.selecteerTegelGebied();
 		return a.getTerreinen().get(0);
 	}
 	
 	public Pion selecteerSpelerPion(Speler s) {
 		Antwoord a = s.selecteerSpelerPion();
 		return a.getPionnen().get(0);
 	}
 	
 	//  2) Output
 	public void zetTegel(Speler s, Tegel t, BordPositie p) {
 		s.zetTegel(t, p);
 	}
 }
