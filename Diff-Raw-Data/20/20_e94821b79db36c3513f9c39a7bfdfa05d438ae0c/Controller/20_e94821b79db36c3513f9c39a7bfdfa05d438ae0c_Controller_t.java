 package bosbrand.controller;
 
 import java.awt.event.*;
 import java.awt.*;
 import java.util.*;
 import javax.swing.Timer;
 
 import bosbrand.view.*;
 import bosbrand.model.*;
 
 public class Controller implements IController {
 
 	private IBosbrandModel grond;
 	private IView view;
 	private ActionListener listener;
 	private Timer displayTimer;
 	private boolean timerRunning;
 	private IAfbeeldbaar af;
 	
 	/**
 	 * De controller maakt ene grond object aan en initialiseerd de wereld.
 	 * @param wereld de string met de input, of de random grond.
 	 * De Timer staat in de constructor zodat we hem door de hele Controller kunnen starten/stoppen.
 	 */
 	public Controller(String[] wereld) {
 		timerRunning = false;
 		af = new Afbeeldbaar(0,0,' ');
 		grond = new Grond();
 		grond.initialiseer(wereld);
 		listener = new ActionListener(){
 			public void actionPerformed(ActionEvent event){
 				doen();
 			}
 		};
 		displayTimer = new Timer(1000, listener);
 		view = new View(this);
 	}
 
 	/**
 	 * Laat het model weer vanaf het begin beginnen:
 	 * Kavels terug naar origineel, boswachters weg, update-counter weer naar 0.
 	 * Pauzeer de timer tijdens de reset om te voorkomen dat er ongelukken gebeuren als nog niet het hele veld gereset is
 	 * maar het veld wel weer geupdate wordt, bijvoorbeeld.
 	 */
 	public void doeReset() {
 		//Kavels terug naar origineel en boswachters weg, en opnieuw afbeelden.
 		grond.reset();
 		afbeelden();
 	}
 	
 	/**
 	 * Methode die uitvoert wat er moet gebeuren als de Timer een Action Event gooit.
 	 */
 	private void doen() {
 		//De grond naar de volgende toestand updaten volgens dew regels van Opdracht 3.
 		grond.update();
 		
 		//Alle boswachters hun ding laten doen
 		IBoswachter[] boswachters = grond.getBoswachters();
 		for (IBoswachter bw: boswachters) {
 			bw.update();
 		}
 		
 		//De nieuwe grond afbeelden
 		afbeelden();
 	}
 	
 	/**
 	 * Methode voor de 'Simuleer'-knop.
 	 */
 	public void doeSimuleer() {
 		if (!timerRunning) {
 			displayTimer.start();
 			timerRunning = true;
 		}
 		else if (timerRunning) {
 			displayTimer.stop();
 			timerRunning = false;
 		}
 	}
 
 	/**
 	 * Maak van ieder kavel een afbeeeldbaar object en stop die objecten in een Array.
 	 */
 	public void afbeelden() {
 		IKavel[][] kavels = grond.getKavels();
 		ArrayList<IAfbeeldbaar> afb = new ArrayList<IAfbeeldbaar>();
 		
 		//Haal de coordinaten uit het kavel en de kleur. Dus of ie in brand staat of leeg is.
 		//De randgevallen skipt ie.
 		//Opties voor kleuren zijn Rood, Groen en B voor standaard.
		//Van linksboven naar rechtsonder de kavels omzetten en de bijbehorende coordinaten meegeven.
		int y = (int) setStartpunt().getY();
 		for (int r=0;r<kavels.length;r++) {
			int x = (int) setStartpunt().getX();
 			for (int c=0;c<kavels[r].length;c++) {
 				if (kavels[r][c] instanceof LeegKavel) {
 					af = new Afbeeldbaar(x , y, 'B');
 				}
 				else {
 					if (kavels[r][c].voortBranden()) {
 						af = new Afbeeldbaar(x , y, 'R');
 					}
 					else {
 						af = new Afbeeldbaar(x , y, 'G');
 					}
 				}
 				afb.add(af);
 				x = x + af.getZijde();
 			}
 			y = y + af.getZijde();
 		}
 		IAfbeeldbaar[] afbeeldData = afb.toArray(new Afbeeldbaar[afb.size()]);
 		//view.afbeelden(afbeeldData);
 		view.afbeelden(afbeeldData);
 	}
 	
 	/**
 	 * Deze methode berekent op welk punt moet worden begonnen met afdrukken om de grond mooi in het midden te hebben.
 	 * In de praktijk werkt dit nog niet in verticale richting door een onbekende fout in een van de tekenmethodes (die niet van ons zijn).
 	 * @return een Java Point met de cordinaten.
 	 */
 	private Point setStartpunt() {
 		double x = view.getMidden().getX();
 		double y = view.getMidden().getY();
 		
 		double 	xOffsetKavel = grond.getKavels()[0].length / 2.0;
 				xOffsetKavel = xOffsetKavel * af.getZijde();
 		double 	yOffsetKavel = grond.getKavels().length / 2.0;
 				yOffsetKavel = yOffsetKavel * af.getZijde();
 				
 		Double startX = x - xOffsetKavel;
 		Double startY = y - yOffsetKavel;
 		
 		return new Point (startX.intValue(), startY.intValue());
 	}
 
 	/**
 	 * Deze methode doet niets anders dan de toggleBoswachtermethode in Grond aanroepen.
 	 * @param rij de rij van het kavel, die hij meekrijgt uit View.
 	 * @param kolom de kolom van het kavel, die hij meekrijgt van View
 	 */
 	public void toggleBoswachter(int rij, int kolom) {
 		grond.toggleBoswachter(rij, kolom);
 	}
 
 	/**
 	 * Deze methode doet niets anders dan de toggleVuurmethode in Grond aanroepen.
 	 * @param rij de rij van het kavel, die hij meekrijgt uit View.
 	 * @param kolom de kolom van het kavel, die hij meekrijgt van View
 	 */
 	public void toggleVuur(int rij, int kolom) {
 		grond.toggleVuur(rij, kolom);
 	}
 
 }
