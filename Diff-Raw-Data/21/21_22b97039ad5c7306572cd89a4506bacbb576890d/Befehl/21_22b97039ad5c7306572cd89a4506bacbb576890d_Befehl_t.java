 package de.uni_hamburg.informatik.sep.zuul.befehle;
 
 import de.uni_hamburg.informatik.sep.zuul.SpielKontext;
 
 /**
  * Informationen über einen vom Benutzer eingegebenen Befehl. Ein Befehl besteht
  * aus zwei Zeichenketten: einem Befehlswort und einem zweiten Wort. Beim Befehl
  * "go west" beispielsweise sind die beiden Zeichenketten "go" und "west". Wenn
  * der Befehl nur aus einem Wort bestand, dann ist das zweite Wort
  * <code>null</code>.
  * 
  * Benutzer dieser Klasse sind dafür zuständig, Befehle auf ihre Gültigkeit zu
  * prüfen. Wenn ein Spieler einen ungültigen Befehl eingegeben hat, sollte das
  * Befehlswort auf <code>null</code> gesetzt werden.
  */
 public abstract class Befehl
 {
 	private String[] _parameters;
 	
 	/**
 	 * Setzt die Parameter, mit denen dieser Befehl ausgeführt werden soll
 	 * @param parameters die Parameter, mit denen dieser Befehl ausgeführt werden soll
 	 */
 	void setParameter(String[] parameters)
 	{
 		_parameters = parameters;
 	}
 	
 	/**
 	 * Führt den Befehl aus.
 	 */
 	public abstract void ausfuehren(SpielKontext kontext);
 
 	/**
 	 * Gibt den Namen des Befehls zurück.
 	 */
 	public abstract String getBefehlsname();
 
	/**
	 * @return Die Parameter dieses Befehls
	 */
	protected String[] getParameters()
	{
		return _parameters;
	}
 	
 	@Override
 	public boolean equals(Object obj)
 	{
 		boolean result = false;
 		if(obj instanceof Befehl)
 		{
 			Befehl other = (Befehl) obj;
 			result = other.getBefehlsname().equals(getBefehlsname()) && other._parameters.equals(_parameters);
 		}
 		return result;
 	}
 	@Override
 	public int hashCode()
 	{
 		return getBefehlsname().hashCode() ^ _parameters.hashCode();
 	}
 	
 	@Override
 	public Befehl clone()
 	{
 		// TODO ugly!!
 		Befehl newBefehl = null;
 		try
 		{
 			newBefehl = this.getClass().newInstance();
 		}
 		catch(InstantiationException e)
 		{
 			e.printStackTrace();
 		}
 		catch(IllegalAccessException e)
 		{
 			e.printStackTrace();
 		}
 		
 		if(_parameters != null)
 			newBefehl._parameters = _parameters.clone();
 		
 		return newBefehl;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return getBefehlsname();
 	}
 	
 //	/**
 //	 * Liefert das zweite Wort dieses Befehls. Liefert 'null', wenn es kein
 //	 * zweites Wort gab.
 //	 */
 //	public String getZweitesWort()
 //	{
 //		return _zweitesWort;
 //	}
 //
 //	/**
 //	 * Liefert 'true', wenn dieser Befehl ein zweites Wort hat.
 //	 */
 //	public boolean hatZweitesWort()
 //	{
 //		return (_zweitesWort != null);
 //	}
 }
