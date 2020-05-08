 package de.uni_hamburg.informatik.sep.zuul.server.inventar;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Das Inventar nimmt {@link Item}s auf oder gibt sie heraus.
  * 
  * @author 1roebe, 1jost
  * 
  */
 public class Inventar
 {
 	private List<Item> _inhalt;
 
 	/**
 	 * Konstruktor des Inventars. Für näheres siehe {@link Inventar}
 	 */
 	public Inventar()
 	{
 		_inhalt = new LinkedList<Item>();
 	}
 
 	/**
 	 * Fügt ein {@link Item} dem Inventar hinzu.
 	 * 
 	 * @param item
 	 *            Das hinzuzufügende Item
 	 * 
 	 * @require item != null
 	 * @ensure isGefuellt() == true
 	 */
 	public void fuegeItemHinzu(Item item)
 	{
 		assert item != null : "Vorbedingung verletzt: item != null";
 
 		if(item != Item.Keins)
 			_inhalt.add(item);
 	}
 
 	/**
 	 * Nimmt das zuletzt hinzugefügte Item aus dem Inventar. Es wird dabei aus
 	 * dem Inventar entfernt. Wenn keines vorhanden ist, ist es {@link Item}
 	 * .Keins.
 	 * 
 	 * @return Das Item
 	 */
 	public Item nehmeLetztesItem()
 	{
 		if(isGefuellt())
 		{
 			return _inhalt.remove(_inhalt.size() - 1);
 		}
 		else
 		{
 			return Item.Keins;
 		}
 	}
 
 	/**
 	 * Gibt an, ob das Inventar mindestens ein Item besitzt.
 	 */
 	public boolean isGefuellt()
 	{
 		return _inhalt.size() > 0;
 	}
 
 	/**
 	 * Gibt das Inventar als Liste zurück.
 	 */
 	public Item[] getInhaltsliste()
 	{
 		return _inhalt.toArray(new Item[0]);
 	}
 
 	/**
 	 * Gibt an, ob der Spieler überhaupt einen Kuchen besitzt
 	 * 
 	 * @return
 	 */
 	public boolean hasAnyKuchen()
 	{
 		for(Item item : _inhalt)
 		{
 			switch (item)
 			{
 			case UKuchen:
 			case IKuchen:
 			case UGiftkuchen:
 			case IGiftkuchen:
 				return true;
 			default:
 				break;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Gibt den entsprechenden Kuchen aus dem Inventar
 	 * 
 	 * @param item
 	 * @return
 	 * 
 	 * @require hasAnyKuchen(item)
 	 */
 	public Item getKuchen(Item item)
 	{
 		assert hasAnyKuchen();
 
 		switch (item)
 		{
 		case IKuchen:
 			if(has(Item.IKuchen))
 			{
 				_inhalt.remove(Item.IKuchen);
 				return (Item.IKuchen);
 			}
 			break;
 		case UKuchen:
 			if(has(Item.UKuchen))
 			{
 				_inhalt.remove(Item.UKuchen);
 				return (Item.UKuchen);
 			}
 			break;
 		case IGiftkuchen:
 			if(has(Item.IGiftkuchen))
 			{
 				_inhalt.remove(Item.IGiftkuchen);
 				return (Item.IGiftkuchen);
 			}
 			break;
 		case UGiftkuchen:
 			if(has(Item.UGiftkuchen))
 			{
 				_inhalt.remove(Item.UGiftkuchen);
 				return (Item.UGiftkuchen);
 			}
 			break;
 		default:
 
 
 		}
 		return null;
 	}
 
 	/**
 	 * @require hasAnyKuchen()
 	 * @return
 	 */
 	public Item getAnyKuchen()
 	{
 		assert hasAnyKuchen();
 
 		for(Item item : _inhalt)
 		{
 			switch (item)
 			{
 			case UKuchen:
 			case IKuchen:
 			case UGiftkuchen:
 			case IGiftkuchen:
 				_inhalt.remove(item);
 				return item;
 			default:
 				break;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @require hasAnyKuchen()
 	 * @return
 	 */
 	public Item getAnyUKuchen()
 	{
 	    assert hasAnyKuchen();
 
 		for(Item item : _inhalt)
 		{
 			switch (item)
 			{
 			case UKuchen:
 			case UGiftkuchen:
 				_inhalt.remove(item);
 				return item;
 			default:
 				break;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Gibt an, ob die Liste das Item enthält
 	 * 
 	 * @param item
 	 * @return
 	 */
 	public boolean has(Item item)
 	{
 		return _inhalt.contains(item);
 	}
 
 	@Override
 	public String toString()
 	{
 		StringBuilder builder = new StringBuilder();
 		
 		for(Item i : _inhalt)
 		{
 			builder.append(i).append(", ");
 		}
 
 		if (builder.length() > 2)
 			builder.setLength(builder.length() -2);
 		return builder.toString();
 	}

	public boolean hasAnyUKuchen()
	{
		for(Item item : _inhalt)
		{
			switch (item)
			{
			case UKuchen:
			case UGiftkuchen:
				return true;
			default:
				break;
			}
		}
		return false;
	}
 }
