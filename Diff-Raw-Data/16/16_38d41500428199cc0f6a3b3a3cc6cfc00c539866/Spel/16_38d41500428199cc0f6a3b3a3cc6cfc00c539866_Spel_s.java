 package controllers;
 
 import java.awt.EventQueue;
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
 import views.MainWindow;
 import data.Content;
 import exceptions.DataException;
 
 /**
  * @author tim, nanne
  *
  */
 public class Spel {
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					new Spel();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private Speler				speler;
 	private Timer				timer;
 	private JokerUitrekenaar	joker;
 	private Onderwerp			_onderwerp;
 
 	private MainWindow			window;
 	private int					huidigeRonde	= 0;
 	private Content				content;
 
 	private List<Vraag>			vragen;
 
 	
 	/**
 	 * Start spel op.
 	 */
 	public Spel() {
 		speler = new Speler();
 		timer = new Timer();
 		joker = new JokerUitrekenaar();
 
 		try {
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
 	 * Ga terug naar het hoofdmenu.
 	 */
 	public void backToMainMenu() {
 		window.reset();
 	}
 
 	/**
 	 * Geef het huidige onderdeel terug
 	 * @return huidige onderdeel
 	 */
 	public logic.Onderdeel getHuidigeOnderdeel() {
 		return getHuidigeVraag().getHuidigeOnderdeel();
 	}
 
 	/**
 	 * Geef de huidige vraag terug
 	 * @return huidige vraag
 	 */
 	private Vraag getHuidigeVraag() {
 		return vragen.get(huidigeRonde);
 	}
 	
 	/**
 	 * @return hoeveel jokers
 	 */
 	public int getJokerAantal() {
 		return joker.getAantal();
 	}
 
 	/**
 	 * Verkrijg onderdelen van huidige vraag
 	 * @return alle onderdelen
 	 */
 	public ArrayList<Onderdeel> getOnderdelen() {
 		return getHuidigeVraag().getOnderdelen();
 	}
 
 	/**
 	 * Verkrijg alle onderwerpen
 	 * @return Lijst met onderwerpen
 	 * @throws DataException wanneer onderwerpen niet ingelezen kunnen worden 
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
 	 * @param optie gekozen onderdeel
 	 * @return gekozenantwoord, hiermee kan ook gekeken worden of het goed is
 	 */
 	public GekozenAntwoord kiesOnderdeel(Onderdeel optie) {
 		return getHuidigeVraag().kiesAntwoord(optie);
 	}
 
 	/**
 	 * Open een scherm.
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
 		} catch (DataException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
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
 		huidigeRonde++;
 	}
 
 	/**
 	 * Antwoord wijzigen
 	 * @param van
 	 * @param naar
 	 */
 	public void wijzigAntwoord(Onderdeel van, Onderdeel naar) {
 		getHuidigeVraag().wijzigAntwoord(van, naar);
 	}
 
 	/**
 	 * Jokers inzetten
 	 * @param jokers hoeveel jokers
 	 */
 	public void zetJokersIn(int jokers) {
 		joker.zetJokersIn(jokers);
 	}
 }
