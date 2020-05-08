 package de.uni_hamburg.informatik.sep.zuul.spiel;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Stack;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 /**
  * Ein Raum in der Welt von Zuul. Ein Raum ist mit anderen Räumen über Ausgänge
  * verbunden, die in unterschiedlichen Richtungen liegen. In manchen Räumen
  * liegen Items, die von dem Spieler automatisch eingesammelt werden.
  * Standardmäßig sind die Räume leer.
  */
 @XmlRootElement(name = "raum")
 @XmlType(propOrder = { "_name", "_id", "_beschreibung", "_raumart", "_items" })
 public class Raum
 {
 	private @XmlElement(name = "beschreibung")
 	String _beschreibung;
 	private @XmlTransient
 	Map<String, Raum> _ausgaenge;
 	private @XmlElement(name = "item")
 	Stack<Item> _items;
 	private @XmlTransient
 	Maus _maus;
 	private @XmlElement(name = "raumart")
 	RaumArt _raumart;
 	private @XmlElement(name = "id")
 	int _id;
 	private @XmlElement(name = "name")
 	String _name;
 
 	/**
 	 * Nur für JAXB
 	 */
 	private Raum()
 	{
 		_ausgaenge = new HashMap<String, Raum>();
 		setItems(new Stack<Item>());
 	}
 
 	/**
 	 * Erzeugt einen Raum mit einer Beschreibung. Ein Raum hat anfangs keine
 	 * Ausgänge.
 	 * 
 	 * @param beschreibung
 	 *            die Beschreibung des Raums.
 	 * 
 	 * @require name != null
 	 * @require beschreibung != null
 	 */
 	public Raum(String name, String beschreibung)
 	{
 		assert beschreibung != null : "Vorbedingung verletzt: beschreibung != null";
 		assert name != null : "Vorbedingung verletzt: name != null";
 
 		this._beschreibung = beschreibung;
 		this._ausgaenge = new HashMap<String, Raum>();
 
 		setItems(new Stack<Item>());
 
 		_name = name;
 
 		_id = _name.hashCode();
 	}
 
 	/**
 	 * Setzt einen Ausgang aus diesem Raum zu einem Nachbarraum.
 	 * 
 	 * @param richtung
 	 *            die Richtung, in der ein Nachbarraum hinzugefügt wird.
 	 * @param nachbar
 	 *            der Nachbarraum.
 	 * @require richtung != null
 	 * @require nachbar != null
 	 */
 	public void setAusgang(String richtung, Raum nachbar)
 	{
 		assert richtung != null : "Vorbedingung verletzt: richtung != null";
 		assert nachbar != null : "Vorbedingung verletzt: nachbar != null";
 
 		_ausgaenge.put(richtung, nachbar);
 	}
 
 	/**
 	 * Verbindet die beiden übergebenen Räume in der entsprechenden Richtung.
 	 * Dabei wird Raum1 in Richtung richtung mit Raum2 verbunden und Raum2 in
 	 * Richtung gegenRichtung mit Raum1 zurück verbunden.
 	 * 
 	 * @param richtung
 	 *            die Verbindungsrichtung zu Raum2
 	 * @param nachbar
 	 *            der zu verbindende Raum
 	 * @param gegenRichtung
 	 *            die Verbindungsrichtung zu Raum1
 	 */
 	public void verbindeZweiRaeume(String richtung, Raum nachbar,
 			String gegenRichtung)
 	{
 		//TODO: 
 		//Abbrechen wenn null übergeben wird. Dies darf vorkommen, soll aber keinen effekt haben.
 		if(nachbar == null)
 		{
 			return;
 		}
 		this.setAusgang(richtung, nachbar);
 		nachbar.setAusgang(gegenRichtung, this);
 
 	}
 
 	public ArrayList<Raum> getAusgaenge()
 	{
 		return new ArrayList<Raum>(_ausgaenge.values());
 	}
 
 	/**
 	 * Gibt den Nachbarraum zurück, der in der angegebenen Richtung liegt. Wenn
 	 * in der Richtung kein Nachbarraum liegt, wird <code>null</code>
 	 * zurückgegeben.
 	 * 
 	 * @param richtung
 	 *            die Richtung des Nachbarraums.
 	 * @require richtung != null
 	 */
 	public Raum getAusgang(String richtung)
 	{
 		assert richtung != null : "Vorbedingung verletzt: richtung != null";
 
 		return _ausgaenge.get(richtung);
 	}
 
 	public String[] getMoeglicheAusgaenge()
 	{
 		return _ausgaenge.keySet().toArray(new String[0]);
 	}
 
 	/**
 	 * Setze ein Item in diesen Raum. Default ist {@link Item}.keins .
 	 * 
 	 * @param item
 	 *            Das neue Item
 	 * 
 	 * @require item != Item.Keins
 	 */
 	public void addItem(Item item)
 	{
 		assert item != Item.Keins : "Vorbedingung verletzt: item != Item.Keins";
 
 		getItems().push(item);
 
 		Collections.shuffle(getItems());
 	}
 
 	/**
 	 * Entfernt das nächste Item aus dem Raum, wenn es eines gibt
 	 */
 	public void loescheItem()
 	{
 		if(!getItems().empty())
 			getItems().pop();
 	}
 	
 	/**
 	 * Setzt den Item-Stack neu
 	 * Für den Editor relevant
 	 */
 	public void setItems(Stack<Item> items)
 	{
 		_items = items;
 	}
 	
 	/**
 	 * Gibt die Items zurück
 	 * Für den Editor relevant
 	 */
 	public Stack<Item> getItems()
 	{
 		return _items;
 	}
 
 	/**
 	 * Setzt die Beschreibung des Raumes
 	 * Für den Editor relevant
 	 */
 	public void setBescheibung(String beschreibung)
 	{
 		_beschreibung = beschreibung;
 	}
 	
 	/**
 	 * Liefert die Beschreibung dieses Raums (die dem Konstruktor übergeben
 	 * wurde).
 	 * 
 	 * @ensure ergebnis != null
 	 */
 	public String getBeschreibung()
 	{
 		return _beschreibung;
 	}
 
 	/**
 	 * liefert das Nächste Item, entfernt es jedoch nicht
 	 * 
 	 * @return Item
 	 * @ensure Item != null
 	 */
 	public Item getNaechstesItem()
 	{
 		if(getItems().empty())
 		{
 			return Item.Keins;
 		}
 		return getItems().peek();
 	}
 
 	public boolean hasMaus()
 	{
 		return _maus != null;
 	}
 
 	/**
 	 * @return the _maus
 	 * @require hasMaus()
 	 */
 	@XmlTransient
 	public Maus getMaus()
 	{
 		assert hasMaus();
 
 		return _maus;
 	}
 
 	/**
 	 * @param _maus
 	 *            the _maus to set
 	 */
 	public void setMaus(Maus maus)
 	{
 		_maus = maus;
 	}
 
 	public RaumArt getRaumart()
 	{
 		return _raumart;
 	}
 
 	void setRaumart(RaumArt raumart)
 	{
 		_raumart = raumart;
 	}
 
 	public int getId()
 	{
 		return _id;
 	}
 
 	private void setId(int id)
 	{
 		_id = id;
 	}
 
 	public String getName()
 	{
 		return _name;
 	}
 
 	private void setName(String name)
 	{
 		_name = name;
 	}
 
 }
