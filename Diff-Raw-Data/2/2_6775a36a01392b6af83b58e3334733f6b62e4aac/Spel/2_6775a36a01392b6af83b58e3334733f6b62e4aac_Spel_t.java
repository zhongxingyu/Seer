 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import logic.GekozenAntwoord;
 import logic.JokerUitrekenaar;
 import logic.Onderdeel;
 import logic.Onderwerp;
 import logic.Speler;
 import logic.Timer;
 import logic.Vraag;
 import views.GameOver;
 import views.GameWon;
 import views.MainWindow;
 import views.SpeelScherm;
 import data.Content;
 import exceptions.DataException;
 
 /**
  * @author tim, nanne
  * 
  */
 public class Spel {
 	private Speler				speler;
 	private Timer				timer;
 	private JokerUitrekenaar	joker;
 	private Onderwerp			_onderwerp;
 
 	private MainWindow			window;
 	private int					huidigeRonde	= 0;
 	private Content				content;
 
 	private List<Vraag>			vragen;
 	private Applicatie			applicatie;
 	private data.Highscore		highscores;
 
 	/**
 	 * Start spel op.
 	 */
 	public Spel() {
 		startSpel();
 	}
 
 	/**
 	 * Start spel op, moet applicatie hebben voor het wissen van alle data.
 	 * @param applicatie
 	 */
 	public Spel(Applicatie applicatie) {
 		this.applicatie = applicatie;
 		startSpel();
 	}
 
 	/**
 	 * Ga terug naar het hoofdmenu.
 	 */
 	public void backToMainMenu() {
 		if (applicatie != null) {
 			window.close();
 			applicatie.nieuwSpel();
 		} else System.exit(0);
 	}
 
 	/**
 	 * Breng door de gebruiker gekozen antwoorden terug.
 	 * 
 	 * @return gekozen antwoorden
 	 */
 	public ArrayList<GekozenAntwoord> getGekozenAntwoorden() {
 		return getHuidigeVraag().getGekozenAntwoorden();
 	}
 
 	/**
 	 * Geef het huidige onderdeel terug
 	 * 
 	 * @return huidige onderdeel
 	 */
 	public logic.Onderdeel getHuidigeOnderdeel() {
 		return getHuidigeVraag().getHuidigeOnderdeel();
 	}
 
 	/**
 	 * Geef de huidige vraag terug
 	 * 
 	 * @return huidige vraag
 	 */
 	private Vraag getHuidigeVraag() {
 		return vragen.get(huidigeRonde);
 	}
 
 	/**
 	 * @return hoeveel jokers
 	 */
 	public int getJokerAantal() {
 		return joker.getAantalOver();
 	}
 
 	/**
 	 * Geef terug hoeveel een joker kost.
 	 * @return jokerkosten
 	 */
 	public int getJokerKosten() {
 		return joker.getKosten();
 	}
 
 	/**
 	 * Geef terug hoeveel jokers de gebuiker maximaal mag inzetten
 	 * @return jokeraantal
 	 */
 	public int getMaxJokers() {
 		return joker.getMaxJokers(timer);
 	}
 
 	/**
 	 * Verkrijg onderdelen van huidige vraag
 	 * 
 	 * @return alle onderdelen
 	 */
 	public ArrayList<Onderdeel> getOnderdelen() {
 		return getHuidigeVraag().getOnderdelen();
 	}
 
 	/**
 	 * Verkrijg alle onderwerpen
 	 * 
 	 * @return Lijst met onderwerpen
 	 * @throws DataException
 	 *             wanneer onderwerpen niet ingelezen kunnen worden
 	 */
 	public List<Onderwerp> getOnderwerpen() throws DataException {
 		return content.getOnderwerpen();
 	}
 
 	/**
 	 * @return Spelernaam
 	 */
 	public String getSpelerNaam() {
 		return speler.getNaam();
 	}
 
 	/**
 	 * @return vraag tekst
 	 */
 	public String getVraagTekst() {
 		return getHuidigeVraag().getTekst();
 	}
 
 	/**
 	 * Kies een onderdeel, hiermee wordt een GekozenAntwoord aangemaakt en teruggestuurd.
 	 * 
 	 * @param optie
 	 *            gekozen onderdeel
 	 * @return gekozenantwoord, hiermee kan ook gekeken worden of het goed is
 	 */
 	public GekozenAntwoord kiesOnderdeel(Onderdeel optie) {
 		return getHuidigeVraag().kiesAntwoord(optie);
 	}
 
 	/**
 	 * Open een scherm.
 	 * 
 	 * @param panel
 	 */
 	public void openPanel(JPanel panel) {
 		window.openPanel(panel);
 	}
 
 	/**
 	 * Stel het ontwerp in.
 	 * 
 	 * @param onderwerp
 	 */
 	public void setOnderwerp(Onderwerp onderwerp) {
 		_onderwerp = onderwerp;
 		try {
 			vragen = content.getVragen(_onderwerp.getNaam());
 			
 			vragen.get(0).setMoetGoedHebben(5);
 			vragen.get(0).setHoeveelWaard(20);
 			
 			vragen.get(1).setMoetGoedHebben(6);
 			vragen.get(0).setHoeveelWaard(25);
 			
 			vragen.get(2).setMoetGoedHebben(7);
 			vragen.get(0).setHoeveelWaard(30);
 			
 			vragen.get(3).setMoetGoedHebben(9);
 			vragen.get(0).setHoeveelWaard(35);
 		} catch (DataException e) {
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
 			throw new RuntimeException("Kan niet verder gaan.");
 		}
 	}
 
 	/**
 	 * Spelermethodes
 	 * 
 	 * @param naam
 	 */
 	public void setSpelerNaam(String naam) {
 		speler.setNaam(naam);
 	}
 
 	private void startSpel() {
 		speler = new Speler();
 		timer = new Timer();
 		joker = new JokerUitrekenaar();
 		try {
 			highscores = new data.Highscore();
 			content = new Content();
 		} catch (DataException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			JOptionPane.showMessageDialog(new JFrame(), e.getMessage());
 			System.exit(0);
 			return;
 		}
 		window = new MainWindow(this);
 	}
 
 	/**
 	 * @param aantal
 	 */
 	public void verwijderJokers(int aantal) {
 		joker.verwijderJokers(aantal);
 	}
 
 	/**
 	 * 
 	 */
 	public void volgendeOnderdeel() {
 		getHuidigeVraag().volgendeOnderdeel();
 	}
 
 	/**
 	 * Verhoog ronde.
 	 */
 	public void volgendeVraag() {
 		if(!magDoorspelen()) {
 			openPanel(new GameOver(this, GameOver.Reason.MISTAKES));
 			return;
 		}
 		huidigeRonde++;
 		if(vragen.size() > huidigeRonde){
 			openPanel(new SpeelScherm(this));	
 		} else {
 			openPanel(new GameWon(this));
 		}
 	}
 
 	/**
 	 * Antwoord wijzigen
 	 * 
 	 * @param van
 	 * @param naar
 	 */
 	public void wijzigAntwoord(Onderdeel van, Onderdeel naar) {
 		getHuidigeVraag().wijzigAntwoord(van, naar);
 	}
 
 	/**
 	 * Jokers inzetten
 	 * 
 	 * @param jokers
 	 *            hoeveel jokers
 	 */
 	public void zetJokersIn(int jokers) {
 		joker.zetJokersIn(jokers);
 	}
 	
 	/**
 	 * Mag de speler nog doorspelen?
 	 * @return antwoord hierop
 	 */
 	public boolean magDoorspelen() {
 		boolean magDoorspelen = true;
		for (int i = 0; i <= huidigeRonde; i++) {
 			magDoorspelen = magDoorspelen && vragen.get(i).magDoorspelen();
 		}
 		return magDoorspelen;
 	}
 	
 	/**
 	 * Moet de speler doorspelen?
 	 * @return antwoord hierop
 	 */
 	public boolean moetDoorspelen() {
 		return huidigeRonde == 0;
 	}
 
 	/**
 	 * Geef timer terug.
 	 * @return timer
 	 */
 	public logic.Timer getTimer() {
 		return timer;
 	}
 
 	/**
 	 * Krijg score
 	 * @return huidige score
 	 */
 	public int getScore() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	/**
 	 * Voeg highscore toe aan highscore lijst.
 	 */
 	public void addHighScore() {
 		logic.Highscore highscore = new logic.Highscore();
 		highscore.setSpelerNaam(getSpelerNaam());
 		highscore.setTijdOver(getTimer().getTime() + "");
 		highscore.setScore(getScore() + "");
 		highscores.addHighscore(highscore);
 	}
 	
 	/**
 	 * Verkrijg highscores.
 	 * @param hoeveel highscores terug
 	 * @return highscores
 	 * 
 	 */
 	public ArrayList<logic.Highscore> getHighscores(int hoeveel) {
 		return highscores.getHighscores(hoeveel);
 	}
 }
