 package main.error;
 
 import logging.Logger;
 
 /**
  * VideothekException.java
  * 
  * @author Christopher Bertels (chbertel@uos.de)
  * @date 11.09.2008
  * 
  * Oberklasse aller Exceptions im Programm. Schreibt automatisch Fehlermeldung
  * via Logger in Logdatei.
  */
 public class VideothekException extends Exception
 {
 	/**
 	 * Konstruktor für VideothekException.
 	 */
 	public VideothekException()
 	{
		this("Fehler; Keine Nachricht angegeben.");
 	}
 
 	/**
 	 * Konstruktor für VideothekException mit Fehlermeldung.
 	 * 
 	 * @param message
 	 *            Fehlernachricht, welche ebenfalls via Logger in die Logdatei
 	 *            geschrieben wird.
 	 */
 	public VideothekException(String message)
 	{
 		super(message);
 		Logger.get().write(message);
 	}
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -281384245546110418L;
 }
