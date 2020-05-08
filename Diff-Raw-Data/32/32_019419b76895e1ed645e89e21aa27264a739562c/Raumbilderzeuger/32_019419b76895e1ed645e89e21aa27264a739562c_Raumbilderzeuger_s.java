 package de.uni_hamburg.informatik.sep.zuul.client;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 import de.uni_hamburg.informatik.sep.zuul.server.inventar.Item;
 import de.uni_hamburg.informatik.sep.zuul.server.raum.RaumArt;
 import de.uni_hamburg.informatik.sep.zuul.server.util.FancyFunction.SuperFancyReproducibleRandomEntryPicker;
 
 /**
  * 
  * @author 1roebe
  * 
  */
 public class Raumbilderzeuger
 {
 
 	//	private final String PATH = getClass().getResource("bilder/").getPath();
 
 	private final BufferedImage WIN = ladeBild(getClass().getResource(
 			"bilder/win.png"));
 	private final BufferedImage GAMEOVER = ladeBild(getClass().getResource(
 			"bilder/gameover.png"));
 	private final BufferedImage RAUMTUERNORD = ladeBild(getClass().getResource(
 			"bilder/door_n.png"));
 	private final BufferedImage RAUMTUEROST = ladeBild(getClass().getResource(
 			"bilder/door_e.png"));
 	private final BufferedImage RAUMTUERSUED = ladeBild(getClass().getResource(
 			"bilder/door_s.png"));
 	private final BufferedImage RAUMTUERWEST = ladeBild(getClass().getResource(
 			"bilder/door_w.png"));
 	private final BufferedImage SCHAUENSCHATTEN = ladeBild(getClass()
 			.getResource("bilder/peek.png"));
 
 	private final BufferedImage MAUS = ladeBild(getClass().getResource(
 			"bilder/maus.png"));
 	private final BufferedImage KATZE = ladeBild(getClass().getResource(
 			"bilder/katze.png"));
 	private final BufferedImage KRUEMEL = ladeBild(getClass().getResource(
 			"bilder/kruemel.png"));
 	private final BufferedImage GEGENGIFT = ladeBild(getClass().getResource(
 			"bilder/gegengift.png"));
 	private final BufferedImage DRLITTLE = ladeBild(getClass().getResource(
 			"bilder/drlittle.png"));
 	private final BufferedImage DREVENBIGGER = ladeBild(getClass().getResource(
 			"bilder/drevenbigger.png"));
 
 	private LinkedList<Tupel> _drlittlepositionen;
 	private LinkedList<Tupel> _mauspositionen;
 	private LinkedList<Tupel> _itemPositionen;
 
 	private int _breitehoehe;
 	private Random _random;
 	private int _raumID;
 	private int[] _besuchteRaeume;
 	private String _gegangeneRichtung;
 
 	private ClientPaket _paket;
 	private boolean _schauenAnsicht;
 	private final Color[] KITTELFARBEN = new Color[] { Color.BLUE, Color.RED,
 			Color.GREEN, Color.YELLOW, Color.PINK, Color.MAGENTA };
 
 	public Raumbilderzeuger()
 	{
 		_drlittlepositionen = new LinkedList<Tupel>();
 		_mauspositionen = new LinkedList<Tupel>();
 		_itemPositionen = new LinkedList<Tupel>();
 		_random = new Random();
 
 	}
 
 	/**
 	 * Malt den aktuellen Raum sowie die Items und Charaktere die sich in ihm
 	 * befinden
 	 * 
 	 * @param breitehoehe
 	 *            die Höhe/Breite des Raumbildes
 	 * @param paket
 	 *            Das Paket mit den Informationen zum Raum
 	 * @param vorschau
 	 *            Gibt an ob in einen benachbarten Raum geschaut wird
 	 * @return
 	 */
 	public BufferedImage getRaumansicht(int breitehoehe, ClientPaket paket,
 			boolean vorschau)
 	{
 		_paket = paket;
 		if(breitehoehe == 0)
 			_breitehoehe = 3;
 		_breitehoehe = breitehoehe;
 		_schauenAnsicht = vorschau;
 
 		return erzeugeRaumansicht();
 	}
 
 	private BufferedImage erzeugeRaumansicht()
 	{
 		SuperFancyReproducibleRandomEntryPicker entryPicker = new SuperFancyReproducibleRandomEntryPicker(
 				_paket.buildUniqueID());
 
 		BufferedImage raum = null;
 
 		RaumArt raumArt = _paket.getRaumArt();
 		switch (raumArt)
 		{
 		case Normal:
 			raum = ladeBild(getClass().getResource("bilder/raum_normal.png"));
 			break;
 		case Start:
 			raum = ladeBild(getClass().getResource("bilder/raum_labor.png"));
 			break;
 		case Ende:
 			raum = ladeBild(getClass().getResource("bilder/raum_ende.png"));
 			break;
 		}
 
 		Graphics2D g2d = (Graphics2D) raum.getGraphics();
 
 		for(String richtung : _paket.getMoeglicheAusgaenge())
 		{
 
 			if(richtung.equals("nord"))
 			{
 				g2d.drawImage(RAUMTUERNORD, 272, 20, 97, 50, null);
 			}
 			else if(richtung.equals("ost"))
 			{
 				g2d.drawImage(RAUMTUEROST, 570, 272, 50, 97, null);
 			}
 			else if(richtung.equals("süd"))
 			{
 				g2d.drawImage(RAUMTUERSUED, 272, 570, 97, 50, null);
 			}
 			else if(richtung.equals("west"))
 			{
 				g2d.drawImage(RAUMTUERWEST, 20, 272, 50, 97, null);
 			}
 
 		}
 
 		setPositionen();
 		int x = 0;
 		int y = 0;
 		Tupel position;
 
 		// Male Dr.Little
 
 		//		Tupel position = _drlittlepositionen.get(_random.nextInt((_drlittlepositionen.size())));
 		//		int x = position.getX();
 		//		int y = position.getY();
 		//		g2d.drawImage(DRLITTLE, x, y, 54, 54, null);
 
 		//andere spieler malen
 		for(String s : _paket.getAndereSpieler())
 		{
			position = entryPicker.pick(_drlittlepositionen);
 			x = position.getX();
 			y = position.getY();
 			g2d.drawImage(DRLITTLE, x, y, 54, 54, null);
 		}
 
 		//Male Maus
 
 		if(_paket.hasMaus())
 		{
			Tupel mausposition = entryPicker.pick(_mauspositionen);
 			x = mausposition.getX();
 			y = mausposition.getY();
 
 			g2d.drawImage(MAUS, x, y, 100, 51, null);
 		}
 
 		//Male Katze
 		else if(_paket.hasKatze())
 		{
			Tupel pos = entryPicker.pick(_mauspositionen);
 			x = pos.getX();
 			y = pos.getY();
 			g2d.drawImage(KATZE, x, y, 100, 100, null);
 		}
 
 		// Male Gegenstände
 		int anzahlKruemel = 0;
 		boolean gegengiftDa = false;
 
 		Collection<Item> raumItems = _paket.getItems();
 
 		for(Item item : raumItems)
 		{
 			switch (item)
 			{
 			case IKuchen:
 			case UKuchen:
 			case IGiftkuchen:
 			case UGiftkuchen:
 				anzahlKruemel++;
 				break;
 			case Gegengift:
 				gegengiftDa = true;
 				break;
 			default:
 				break;
 			}
 
 		}
 
 		for(int i = 0; i < anzahlKruemel; i++)
 		{
			Tupel itempos = entryPicker.pick(_itemPositionen);
			_itemPositionen.remove(itempos);
 			x = itempos.getX();
 			y = itempos.getY();
 
 			g2d.drawImage(KRUEMEL, x, y, 30, 30, null);
 		}
 
 		if(gegengiftDa)
 		{
			Tupel itempos = entryPicker.pick(_itemPositionen);
 			_itemPositionen.remove(itempos);
 			x = itempos.getX();
 			y = itempos.getY();
 
 			g2d.drawImage(GEGENGIFT, x, y, 30, 30, null);
 
			Tupel pos = entryPicker.pick(_mauspositionen);
 			x = pos.getX();
 			y = pos.getY();
 			g2d.drawImage(DREVENBIGGER, x, y, 100, 100, null);
 
 		}
 
 		if(_schauenAnsicht)
 		{
 			g2d.drawImage(SCHAUENSCHATTEN, 0, 0, 640, 640, null);
 		}
 
 		raum = skaliereBild(raum, _breitehoehe);
 
 		return raum;
 
 	}
 
 	private void setPositionen()
 	{
 		_itemPositionen = new LinkedList<Tupel>();
 
 		_itemPositionen.add(new Tupel(200, 180));
 		_itemPositionen.add(new Tupel(155, 250));
 		_itemPositionen.add(new Tupel(220, 300));
 		_itemPositionen.add(new Tupel(165, 395));
 		_itemPositionen.add(new Tupel(380, 155));
 		_itemPositionen.add(new Tupel(330, 230));
 		_itemPositionen.add(new Tupel(430, 205));
 		_itemPositionen.add(new Tupel(300, 375));
 		_itemPositionen.add(new Tupel(400, 330));
 		_itemPositionen.add(new Tupel(430, 440));
 
 		_mauspositionen = new LinkedList<Tupel>();
 		_mauspositionen.add(new Tupel(70, 70));
 		_mauspositionen.add(new Tupel(70, 470));
 		_mauspositionen.add(new Tupel(470, 70));
 		_mauspositionen.add(new Tupel(470, 470));
 
 		_drlittlepositionen = new LinkedList<Tupel>();
 		_drlittlepositionen.add(new Tupel(73, 320));
 		_drlittlepositionen.add(new Tupel(520, 320));
 		_drlittlepositionen.add(new Tupel(320, 70));
 		_drlittlepositionen.add(new Tupel(320, 520));
 
 	}
 
 	/**
 	 * Skaliert ein Bild neu und gib es dann zurück
 	 * 
 	 * @param img
 	 *            Das zu skalierende Bild
 	 * @param breiteHoehe
 	 *            Die neue Höhe/Breite
 	 * @return Das skalierte Bild
 	 */
 	private BufferedImage skaliereBild(BufferedImage img, int breiteHoehe)
 	{
 
 		BufferedImage ergebnis = new BufferedImage(breiteHoehe, breiteHoehe,
 				BufferedImage.TYPE_INT_ARGB); // bzw. TYPE_INT_RGB falls du kein Alphakanal brauchst
 		ergebnis.getGraphics().drawImage(img, 0, 0, breiteHoehe, breiteHoehe,
 				null); // oder ein anderer drawImage Befehl
 
 		return ergebnis;
 	}
 
 	/**
 	 * Lädt ein Bild aus einer Datei und gibt dieses Bild zurück
 	 * 
 	 * @param url
 	 * @return -Das geladene Bild
 	 */
 
 	private BufferedImage ladeBild(URL url)
 	{
 		try
 		{
 			BufferedImage tmp = ImageIO.read(url);
 			return tmp;
 
 		}
 		catch(IOException e)
 		{
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * Gibt ein Bild von dem zuletzt gemalten Raum zurück
 	 * 
 	 * @param hoehebreite
 	 * @return ein neu skaliertes Bild vom zuletzt gemaltem Raum
 	 */
 
 	public BufferedImage zeichneBildErneut(int hoehebreite)
 	{
 		_breitehoehe = hoehebreite;
 		return erzeugeRaumansicht();
 	}
 
 	/**
 	 * Malt die Verloren-Anzeige auf den aktuellen Raum und gibt das entstandene
 	 * Bild zurück
 	 * 
 	 * @param breitehoehe
 	 * @return Den Verlierenbildschirm
 	 */
 	public BufferedImage getGameOverScreen(int breitehoehe)
 	{
 		BufferedImage raum = getRaumansicht(breitehoehe, _paket,
 				_schauenAnsicht);
 		Graphics2D g2d2 = (Graphics2D) raum.getGraphics();
 		g2d2.drawImage(GAMEOVER, 0, 0, breitehoehe, breitehoehe, null);
 		return skaliereBild(raum, breitehoehe);
 	}
 
 	/**
 	 * Malt die Gewonnen-Anzeige auf den aktuellen Raum und gibt das entstandene
 	 * Bild zurück
 	 * 
 	 * @param hoehebreite
 	 *            -die Hoehe bzw Breite des Raumbildes
 	 * @return Den Gewinnbildschirm
 	 */
 	public BufferedImage getWinScreen(int hoehebreite)
 	{
 		BufferedImage raum = getRaumansicht(hoehebreite, _paket,
 				_schauenAnsicht);
 		Graphics2D g2d2 = (Graphics2D) raum.getGraphics();
 		g2d2.drawImage(WIN, 0, 0, hoehebreite, hoehebreite, null);
 		return skaliereBild(raum, hoehebreite);
 
 	}
 
 	private static class Tupel
 	{
 		private int _x;
 		private int _y;
 
 		public Tupel(int x, int y)
 		{
 			_x = x;
 			_y = y;
 		}
 
 		public int getX()
 		{
 			return _x;
 		}
 
 		public int getY()
 		{
 			return _y;
 		}
 	}
 
 	public void setPaket(ClientPaket paket)
 	{
 		_paket = paket;
 	}
 
 }
