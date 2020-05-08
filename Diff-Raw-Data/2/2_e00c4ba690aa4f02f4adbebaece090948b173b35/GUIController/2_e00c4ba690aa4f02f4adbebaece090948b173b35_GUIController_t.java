 package ui;
 
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 import ui.lobby.LobbyView;
 import ui.spiel.SpielView;
 import ui.spiel.brett.JokerView;
 import ui.verbinden.VerbindenView;
 import applikation.client.controller.Controller;
 import applikation.client.pd.Chat;
 import applikation.client.pd.Spiel;
 import applikation.client.pd.Spieler;
 import dienste.eventqueue.EventQueue;
 
 public class GUIController extends Controller {
 	private VerbindenView verbindenView;
 	private LobbyView lobbyView;
 	private SpielView spielView;
 	private String defaultName;
 	private JokerView jv;
 
 	public GUIController(EventQueue eventQueue, String defaultName) {
 		this.defaultName = defaultName;
 		this.eventQueue = eventQueue;
 	}
 
 	public void zeigeVerbinden() {
 		verbindenView = new VerbindenView(this, defaultName);
 		verbindenView.setVisible(true);
 	}
 
 	public void zeigeLobby(List<Spieler> spieler, Chat chat) {
 		verbindenView.setVisible(false);
 		lobbyView = new LobbyView(spieler, this, chat);
 		lobbyView.setVisible(true);
 	}
 
 	public void zeigeSpiel(Spiel spiel) {
 		// Warte 3 Sekunden, damit alle auch den zuletzt beigetretenen Spieler
 		// noch in der Lobby sehen.
 		try {
 			Thread.sleep(3000);
 		} catch (InterruptedException e) {
 			throw new RuntimeException("Das Warten in der Lobby wurde durch " +
 					"einen anderen Thread unterbrochen.");
 		}
 		lobbyView.setVisible(false);
 		lobbyView.dispose();
 		spielView = new SpielView(this, spiel);
 		spielView.setVisible(true);
		jv = new JokerView(this);
 		spielView.setGlassPane(jv);
 	}
 
 	public void zeigeJokerauswahl(boolean aktiv) {
 		jv.setVisible(aktiv);
 	}
 
 	public void zeigeFehlermeldung(String fehlermeldung) {
 		JOptionPane.showMessageDialog(null, fehlermeldung);
 	}
 
     public void zeigeMeldung(String meldung) {
     	JOptionPane.showMessageDialog(null, meldung);
     }
 
 	public void beenden() {
 		if (verbindenView != null) {
 			verbindenView.setVisible(false);
 			verbindenView.dispose();
 		}
 
 		if (lobbyView != null) {
 			lobbyView.setVisible(false);
 			lobbyView.dispose();
 		}
 
 		if (spielView != null) {
 			spielView.setVisible(false);
 			spielView.dispose();
 		}
 
 		super.beenden();
 	}
 }
