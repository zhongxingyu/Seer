 /**********************************************************************
  * $Source: /cvsroot/jameica/util/src/de/willuhn/util/Settings.java,v $
 * $Revision: 1.15 $
 * $Date: 2007/06/21 09:01:49 $
  * $Author: willuhn $
  * $Locker:  $
  * $State: Exp $
  *
  * Copyright (c) by willuhn.webdesign
  * All rights reserved
  *
  **********************************************************************/
 package de.willuhn.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Properties;
 
 import de.willuhn.logging.Logger;
 
 /**
  * Diese Klasse erweitert Java-Properties um Typsicherheit fuer primitive
  * Typen, Support zum Laden und Speichern von String-Arrays, automatisches
  * Abspeichern beim Aufruf einer Set-Methode und sogar Speichern schon
  * beim Lesen. Das ist nuetzlich, wenn man eine Software ohne properties-Dateien
  * ausliefern will aber dennoch nach dem ersten Start beim Benutzer die
  * Config-Dateien mit den Default-Werten angelegt werden damit dieser
  * nicht in der Dokumentation nach den Schluesselnamen suchen muss
  * sondern sie bereits mit Default-Werten in den Dateien vorfindet.
  * Wird die Properties-Datei von aussen (z.Bsp. mit einem Texteditor)
  * geaendert, wird das automatisch erkannt und die Datei intern neu geladen.
  * TODO: Sollte man mal gegen java.util.prefs.Preferences ersetzen.
  * Allerdings muesste man hier noch klaeren, wie man den Pfad vorgeben
  * kann, ohne das System-Property java.util.prefs.userRoot aendern zu muessen.
  * @author willuhn
  */
 public class Settings
 {
 
   private File file             = null;
   private double lastModified   = 0;
   private Properties properties = null;
 	private boolean storeWhenRead = true;
 
   private Settings()
   {
     // disabled
   }
 
   /**
    * Erzeugt eine neue Instanz der Settings, die exclusiv
    * nur fuer diese Klasse gelten. Existieren bereits Settings
    * fuer die Klasse, werden sie gleich geladen.
    * Hierbei wird eine Properties-Datei
    * [classname].properties im angegebenen Verzeichnis angelegt.
    * @param path Pfad zu den Einstellungen.
    * @param clazz Klasse, fuer die diese Settings gelten.
    */
   public Settings(String path, Class clazz)
   {
     this(null,path,clazz);
   }
 
   /**
    * Erzeugt eine neue Instanz der Settings, die exclusiv
    * nur fuer diese Klasse gelten. Existieren bereits Settings
    * fuer die Klasse, werden sie gleich geladen.
    * Hierbei wird eine Properties-Datei
    * [classname].properties im angegebenen Verzeichnis angelegt.
    * @param systemPath Pfad zu ggf vorhandenen System-Presets.
    * @param userPath Pfad zu den User-Einstellungen.
    * @param clazz Klasse, fuer die diese Settings gelten.
    */
   public Settings(String systemPath, String userPath, Class clazz)
   {
     // Filenamen ermitteln
     this.file = new File(userPath + File.separator + clazz.getName() + ".properties");
 
     Properties presets = null;
 
     // Checken, ob System-Presets existieren
     if (systemPath != null)
     {
       File systemPresets = new File(systemPath + File.separator + clazz.getName() + ".properties");
       if (systemPresets.exists() && systemPresets.canRead())
       {
         try
         {
           Logger.debug("loading system presets from " + systemPresets.getAbsolutePath());
           presets = new Properties();
           presets.load(new FileInputStream(systemPresets));
         }
         catch (Exception e1)
         {
           Logger.error("unable to load system presets from " + systemPresets.getAbsolutePath(),e1);
           presets = null;
         }
       }
     }
     
     this.properties = new Properties(presets);
 
     // wir erzeugen die Datei, wenn sie noch nicht existiert
     if (!this.file.exists())
       store();
     else
       reload();
   }
 
 	/**
 	 * Legt fest, ob die Einstellungen schon beim Lesen gespeichert werden sollen.
 	 * Hintergrund: Jede Get-Funktion (getString(), getBoolean(),..) besitzt einen
 	 * Parameter mit dem Default-Wert falls der Parameter noch nicht existiert.
 	 * Ist dies der Fall und die zugehoerige Set-Methode wird nie aufgerufen,
 	 * dann erscheint der Parameter nie physisch in der properties-Datei.
 	 * Diese muesste dann manuell mit den Parametern befuellt werden, um
 	 * sie aendern zu koennen. Da die Parameter-Namen aber nur in der
 	 * Java-Klasse bekannt sind, wird es einem Fremden schwer fallen, die
 	 * Namen der Parameter zu ermitteln. Fuer genau diesen Fall kann der
 	 * Parameter auf true gesetzt werden. Alle abgefragten Parameter, werden
 	 * dann nach der Abfrage mit dem aktuellen Wert (ggf. dem Default-Wert)
 	 * sofort gespeichert.
    * @param b true, wenn sofort geschrieben werden soll.
    */
   public void setStoreWhenRead(boolean b)
 	{
 		this.storeWhenRead = b;
 	}
 
 	/**
 	 * Liefert eine Liste aller Attribut-Namen, die in dieser Settings-Instanz gespeichert wurden.
    * @return Liste der Attribut-Namen.
    */
   public String[] getAttributes()
 	{
     reload();
 		synchronized (properties)
 		{
 			Iterator it = properties.keySet().iterator();
 			String[] attributes = new String[properties.size()];
 			int i = 0;
 			while (it.hasNext())
 			{
 				attributes[i++] = (String) it.next();
 			}
 			return attributes;
 		}
 	}
 
 	/**
 	 * Liefert den Wert des genannten Attributs als Boolean.
 	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
    * @param name Name des Attributs.
 	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
    * @return true oder false.
    */
   public boolean getBoolean(String name, boolean defaultValue)
 	{
     reload();
 		String s = getProperty(name,defaultValue ? "true" : "false");
 		boolean b = "true".equalsIgnoreCase(s);
 		if (storeWhenRead)
 			setAttribute(name,b);
 		return b;
 	}
 
 	/**
 	 * Liefert den Wert des genannten Attributs als int.
 	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
 	 * Hinweis: Die Funktion wirft keine NumberFormat-Exception, wenn der
 	 * Wert nicht in eine Zahl gewandelt werden kann. Stattdessen wird der
 	 * Default-Wert zurueckgegeben.
 	 * @param name Name des Attributs.
 	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
 	 * @return der Wert des Attributs.
 	 */
 	public int getInt(String name, int defaultValue)
 	{
     reload();
 		String s = getProperty(name,""+defaultValue);
 		int i = defaultValue;
 		try {
 			i = Integer.parseInt(s);
 		}
 		catch (NumberFormatException e)
 		{
 			Logger.error("unable to parse value of param \"" + name + "\", value: " + s,e);
 		}
 		if (storeWhenRead)
 			setAttribute(name,i);
 		return i;
 	}
 
 	/**
 	 * Liefert den Wert des genannten Attributs als double.
 	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
 	 * Hinweis: Die Funktion wirft keine NumberFormat-Exception, wenn der
 	 * Wert nicht in eine Zahl gewandelt werden kann. Stattdessen wird der
 	 * Default-Wert zurueckgegeben.
 	 * @param name Name des Attributs.
 	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
 	 * @return der Wert des Attributs.
 	 */
 	public double getDouble(String name, double defaultValue)
 	{
     reload();
 		String s = getProperty(name,""+defaultValue);
 		double d = defaultValue;
 		try {
 			d = Double.parseDouble(s);
 		}
 		catch (NumberFormatException e)
 		{
 			Logger.error("unable to parse value of param \"" + name + "\", value: " + s,e);
 		}
 		if (storeWhenRead)
 			setAttribute(name,d);
 		return d;
 	}
 
   /**
    * Liefert den Wert des Attributes.
    * @param name
    * @param defaultValue
    * @return der Wert des Attributes.
    */
   private String getProperty(String name, String defaultValue)
 	{
     return properties.getProperty(name, defaultValue);
 	}
 
 	/**
 	 * Liefert den Wert des Attribute.
 	 * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
 	 * @param name Name des Attributs.
 	 * @param defaultValue DefaultWert, wenn das Attribut nicht existiert.
 	 * @return der Wert des Attributs.
 	 */
 	public String getString(String name, String defaultValue)
 	{
     reload();
 		String s = getProperty(name,defaultValue);
 		if (storeWhenRead)
 			setAttribute(name,s);
 		return s;
 	}
 
   /**
    * Liefert ein Array von Werten.
    * Wird das Attribut nicht gefunden oder hat keinen Wert, wird defaultValue zurueckgegeben.
    * Es koennen maximal 256 Werte gelesen oder gespeichert werden.
    * @param name Name des Attributs.
    * @param defaultValues DefaultWert, wenn das Attribut nicht existiert.
    * @return Werte des Attributs in Form eines String-Arrays.
    */
   public String[] getList(String name, String[] defaultValues)
   {
     reload();
     ArrayList l = new ArrayList();
     String s = null;
     for (int i=0;i<255;++i)
     {
       s = getProperty(name + "." + i,null);
       if (s == null) continue;
       l.add(s);
     }
     if (l.size() == 0)
     {
       if (storeWhenRead)
         setAttribute(name,defaultValues);
       return defaultValues;
     }
     String[] result = (String[]) l.toArray(new String[l.size()]);
     if (storeWhenRead)
       setAttribute(name,result);
     return result;
   }
 
 	/**
 	 * Speichert einen boolschen Wert.
    * @param name Name des Attributs.
    * @param value Wert des Attributs.
    */
   public void setAttribute(String name, boolean value)
 	{
 		setAttribute(name, value ? "true" : "false");
 	}
 	
 	/**
 	 * Speichert einen Integer-Wert.
    * @param name Name des Attributs.
    * @param value Wert des Attributs.
    */
   public void setAttribute(String name, int value)
 	{
 		setAttribute(name,""+value);
 	}
 
 	/**
 	 * Speichert einen Double-Wert.
 	 * @param name Name des Attributs.
 	 * @param value Wert des Attributs.
 	 */
 	public void setAttribute(String name, double value)
 	{
 		setAttribute(name,""+value);
 	}
 
   /**
    * Speichert das Attribut <name> mit dem zugehoerigen Wert <value>.
    * Wenn ein gleichnamiges Attribut bereits existiert, wird es ueberschrieben.
    * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
    * @param name Name des Attributs.
    * @param value Wert des Attributs.
    */
   public void setAttribute(String name, String value)
   {
   	if (value == null)
   		properties.remove(name);
   	else
 	    properties.setProperty(name,value);
     store();
   }
 
   /**
    * Speichert das Attribut <name> mit der zugehoerigen Liste von Werten <value>.
    * Wenn ein gleichnamiges Attribut bereits existiert, werden dessen Werte ueberschrieben.
    * Ist der Wert des Attributes <code>null</code>, wird es entfernt.
    * Von dem Array werden die ersten maximal 256 Elemente gespeichert.
    * Alle darueber hinausgehenden Werte, werden ignoriert.
    * @param name Name des Attributs.
    * @param values Werte des Attributs.
    */
   public void setAttribute(String name, String[] values)
   {
     // Wir entfernen immer erst alle Werte. Denn wenn vorher
     // ein laengeres Array drin steht, als wir jetzt reinschreiben,
     // wuerden die alten Werte am Ende des grossen Arrays nicht mehr
     // entfernt.
     for (int i=0;i<255;++i)
     {
       properties.remove(name + "." + i);
     }
     
     if (values == null || values.length == 0)
     {
       store();
       return;
     }
       
     for (int i=0;i<values.length;++i)
     {
       if (i >= 255)
         break; // Schluss jetzt. Das waren genug Werte ;)
       properties.setProperty(name + "." + i,values[i]);
     }
     store();
   }
   
   /**
    * Schreibt die Properties in die Datei.
    * Hinweis: Die Funktion wirft keine IOException, wenn die Datei nicht
    * gespeichert werden kann. Stattdessen wird der Fehler lediglich geloggt.
    */
   private synchronized void store()
   {
     try
     {
       properties.store(new FileOutputStream(this.file),null);
     }
     catch (Exception e1)
     {
       Logger.error("unable to store settings. Do you have write permissions in " + this.file.getAbsolutePath() + " ?",e1);
     }
     finally
     {
       this.lastModified = this.file.lastModified();
     }
   }
 
   /**
    * Laedt die Datei neu.
    */
   private synchronized void reload()
   {
     long modified = this.file.lastModified();
 
     if (this.lastModified == modified)
       return; // Kein Reload noetig
 
     try
     {
       if (this.lastModified > 0) // wenn lastModified 0 ist, wurde die Datei noch gar nicht geladen
         Logger.debug(this.file.getAbsolutePath() + " has changed, reloading");
       this.properties.load(new FileInputStream(this.file));
     }
     catch (FileNotFoundException nfe)
     {
       Logger.warn("file " + this.file.getAbsolutePath() + " has been deleted");
       this.properties.clear();
     }
     catch (Exception e1)
     {
       Logger.error("unable to (re)load settings. Do you have read permissions in " + this.file.getAbsolutePath() + " ?",e1);
     }
     finally
     {
       this.lastModified = modified;
     }
   }
 }
 
 /*********************************************************************
  * $Log: Settings.java,v $
  * Revision 1.15  2007/06/21 09:01:49  willuhn
  * @N System-Presets
  *
  * Revision 1.14  2007/05/29 12:55:59  willuhn
  * @C flush properties if settings file has been deleted
  *
  * Revision 1.13  2007/05/10 16:49:52  willuhn
  * @C changed log level
  *
  * Revision 1.12  2007/05/09 09:42:55  willuhn
  * @N Config-Reload without watcher thread
  *
  * Revision 1.11  2007/03/09 18:03:32  willuhn
  * @N classloader updates
  * @N FileWatch
  *
  * Revision 1.10  2006/09/05 22:02:01  willuhn
  * @C Worker-Redesign in Settings und Session
  *
  * Revision 1.9  2006/05/03 13:14:16  web0
  * *** empty log message ***
  *
  * Revision 1.8  2005/07/24 16:59:17  web0
  * @B fix in settings watcher
  *
  * Revision 1.7  2005/06/27 11:52:14  web0
  * *** empty log message ***
  *
  * Revision 1.6  2005/06/23 20:50:30  web0
  * *** empty log message ***
  *
  * Revision 1.5  2005/05/19 21:40:09  web0
  * *** empty log message ***
  *
  * Revision 1.4  2005/05/19 18:13:35  web0
  * *** empty log message ***
  *
  * Revision 1.3  2005/04/21 23:33:37  web0
  * @N auto reloading of config files after changing
  *
  * Revision 1.2  2005/03/09 01:06:20  web0
  * @D javadoc fixes
  *
  * Revision 1.1  2005/01/13 22:58:05  willuhn
  * @N Settings nach willuhn.util verschoben
  *
  **********************************************************************/
