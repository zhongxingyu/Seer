 package de.uni_hamburg.informatik.sep.zuul.server.features;
 
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.SwingUtilities;
 
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.Befehl;
 import de.uni_hamburg.informatik.sep.zuul.server.befehle.BefehlSchauen;
 import de.uni_hamburg.informatik.sep.zuul.server.inventar.Item;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.Raum;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.RaumStruktur;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spiel;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.SpielLogik;
 import de.uni_hamburg.informatik.sep.zuul.server.spiel.Spieler;
 import de.uni_hamburg.informatik.sep.zuul.server.util.FancyFunction;
 import de.uni_hamburg.informatik.sep.zuul.server.util.ServerKontext;
 import de.uni_hamburg.informatik.sep.zuul.server.util.TextVerwalter;
 
 public class Katze implements Feature, TickListener, BefehlAusfuehrenListener
 {
 	public static final int KATZE_SCHADEN = 2;
 	public static final long SCHLAFZEIT_IN_SEKUNDEN = 10;
 	Raum _raum;
 	boolean _satt = false;
 
 	private Katze(Raum startRaum)
 	{
 		_raum = startRaum;
 	}
 
 	@Override
 	public void tick(ServerKontext kontext)
 	{
 		// dirty fix
		if(!_satt && _raum != null && !istEinSpielerImRaum(kontext, _raum))
 		{
 			bewegeKatze(kontext);
 		}
 
 	}
 
 	private void bewegeKatze(ServerKontext kontext)
 	{
 		_raum.setKatze(null);
 		_raum = waehleNeuenRaum(kontext, _raum);
 		_raum.setKatze(this);
 	}
 
 	private Raum waehleNeuenRaum(ServerKontext kontext, Raum raum)
 	{
 		// Normaler Weise zwei Felder weiter, es sei denn, im ersten Feld ist
 		// der Spieler
 		Raum neuerRaum1 = FancyFunction.getRandomEntry(raum.getAusgaenge());
 
 		// Kann sich die Katze nicht bewegen?
 		if(neuerRaum1 == null)
 			return raum;
 
 		// Katze auf Spieler getroffen?
 		if(istEinSpielerImRaum(kontext, neuerRaum1))
 			return neuerRaum1;
 
 		Raum neuerRaum2 = FancyFunction.getRandomEntry(neuerRaum1
 				.getAusgaenge());
 
 		// Hat neuerRaum1 keine Ausgänge?
 		if(neuerRaum2 == null)
 			return neuerRaum1;
 
 		return neuerRaum2;
 	}
 
 	private boolean istEinSpielerImRaum(ServerKontext kontext, Raum raum)
 	{
 		List<Raum> raeume = kontext.getRaeumeInDemSichSpielerAufhalten();
 		return raeume.contains(raum);
 	}
 
 	public void fuettere(ServerKontext kontext, Spieler spieler, Item kuchen)
 	{
 		switch (kuchen)
 		{
 		case IGiftkuchen:
 		case UGiftkuchen:
 			// TODO: Katze vom Tick deregistieren.
 			_raum.setKatze(null);
 			_raum = null;
 			kontext.schreibeAnSpieler(spieler, TextVerwalter.KATZE_STIRBT);
 			break;
 		case IKuchen:
 		case UKuchen:
 			_satt = true;
 			kontext.schreibeAnSpieler(spieler,
 					TextVerwalter.KATZE_IST_SATT_GEWORDEN);
 			sleep();
 		}
 	}
 
 	private void sleep()
 	{
 		new Timer().schedule(new TimerTask()
 		{
 
 			@Override
 			public void run()
 			{
 				SwingUtilities.invokeLater(new Runnable()
 				{
 
 					@Override
 					public void run()
 					{
 						_satt = false;
 					}
 				});
 			}
 		}, SCHLAFZEIT_IN_SEKUNDEN * Spiel.ONE_SECOND);
 	}
 
 	public Raum getRaum()
 	{
 		return _raum;
 	}
 
 	public boolean isSatt()
 	{
 		return _satt;
 	}
 
 	/**
 	 * Erzeugt eine Katze, registriert sie bei der SpielLogik und setzt sie
 	 * zufällig in einen Raum.
 	 */
 	public static Katze erzeugeKatze(SpielLogik spielLogik)
 	{
 		RaumStruktur raumStruktur = spielLogik.getStruktur();
 		List<Raum> raeume = raumStruktur.getRaeume();
 		Raum raum = FancyFunction.getRandomEntry(raeume);
 
 		Katze katze = new Katze(raum);
 		raum.setKatze(katze);
 		spielLogik.registriereFeature(katze);
 		return katze;
 	}
 
 	@Override
 	public boolean befehlSollAusgefuehrtWerden(ServerKontext kontext,
 			Spieler spieler, Befehl befehl)
 	{
 		// Wenn ein Spieler im gleich Raum ist wie die Katze und 'schauen' ausführt, kriegt er eine gewischt, es sei denn, sie ist satt.
 		if(!_satt && _raum == kontext.getAktuellenRaumZu(spieler)
 				&& befehl instanceof BefehlSchauen)
 		{
 			spieler.setLebensEnergie(spieler.getLebensEnergie() - KATZE_SCHADEN);
 			kontext.schreibeAnSpieler(spieler, TextVerwalter.KATZE_GREIFT_AN);
 
 			bewegeKatze(kontext);
 			return false;
 		}
 		return true;
 	}
 }
