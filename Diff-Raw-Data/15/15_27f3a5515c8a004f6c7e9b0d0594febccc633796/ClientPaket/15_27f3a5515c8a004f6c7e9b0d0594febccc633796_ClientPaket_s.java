 package de.uni_hamburg.informatik.sep.zuul.client;
 
 import java.io.Serializable;
 import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import de.uni_hamburg.informatik.sep.zuul.server.inventar.Item;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.RaumArt;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 
 public class ClientPaket implements Remote, Serializable
 {
 
 	private boolean _katze;
 	private boolean _maus;
 	private Collection<Item> _items;
 	private String _nachricht; // Terminal ausgabe
 	private int _lebensEnergie;
 	private List<String> _andereSpieler;
 	private RaumArt _raumArt;
 	private String _spielerName;
 	private String[] _moeglicheAusgaenge;
 
 	public ClientPaket(ServerKontext kontext, Spieler spieler, String nachricht)
 	{
 		 Raum aktuellerRaum = kontext.getAktuellenRaumZu(spieler);
 
 		_katze = aktuellerRaum.hasKatze();
 		_maus = aktuellerRaum.hasMaus();
 		_items = new ArrayList<Item>(aktuellerRaum.getItems());
 		_nachricht = nachricht;
 		_lebensEnergie = spieler.getLebensEnergie();
 		_andereSpieler = kontext.getSpielerNamenInRaum(aktuellerRaum);
 		_raumArt = aktuellerRaum.getRaumart();
 		_spielerName = spieler.getName();
 		_moeglicheAusgaenge = aktuellerRaum.getMoeglicheAusgaenge();
 	}
 
 	public boolean hasKatze()
 	{
 		return _katze;
 	}
 
 	public boolean hasMaus()
 	{
 		return _maus;
 	}
 
 	public Collection<Item> getItems()
 	{
 		return _items;
 	}
 
 	public String getNachricht()
 	{
 		return _nachricht;
 	}
 
 	public int getLebensEnergie()
 	{
 		return _lebensEnergie;
 	}
 
 	public List<String> getAndereSpieler()
 	{
 		return _andereSpieler;
 	}
 
 	public RaumArt getRaumArt()
 	{
 		return _raumArt;
 	}
 
 	public String getSpielerName()
 	{
 		return _spielerName;
 	}
 	
 	public String[] getMoeglicheAusgaenge()
 	{
 		return _moeglicheAusgaenge;
 	}
 
 }
