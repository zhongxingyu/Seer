 package GUI;
 import java.awt.Dimension;
 import java.awt.Image;
 
 import Logik.Logikgatter;
 
 
 import javax.swing.ImageIcon;
 
 public class ImageCreator 
 {
 	private static final String VERZEICHNIS = "bin/GUI/grafiken/";
 	private static final String DATEIENDUNG = ".png";
 	private static final String PLATZHALTER = "platzhalter";
 	private int anzahlVersionen;
 	private Dimension size;
 	private boolean spiegeln;
 	
 	public ImageCreator(Dimension size, int anzahlVersionen,boolean spiegeln)
 	{
 		this.setAufloesung(size);	
 		this.anzahlVersionen = anzahlVersionen;
 		this.spiegeln = spiegeln;
 	}
 	
 	public ImageCreator(Dimension size)
 	{
 		this.setAufloesung(size);	
 		this.anzahlVersionen = 6;
 		this.spiegeln = false;
 	}
 	/**
 	 * Generiert ein Array mit allen Versionen(Grafiken) des uebergebenen Logikgatters
 	 * @param logikgatter Logikgatter von dem man alle Grafikversionen haben will.
 	 * @return Array mit Grafiken
 	 */
 	public ImageIcon[] getImage(Logikgatter logikgatter)
 	{
 		ImageIcon[] grafiken = new ImageIcon[this.anzahlVersionen];
 		
 		for(Integer i = 0; i < anzahlVersionen;i++  )
 		{
 			try
 			{
 				if(this.spiegeln)
 				{
 					grafiken[i] = new MirrorImageIcon( ImageCreator.VERZEICHNIS + logikgatter.toString() + i.toString() + ImageCreator.DATEIENDUNG ); // Grafik erstellen
 					grafiken[i] = new MirrorImageIcon(grafiken[i].getImage().getScaledInstance((int)this.size.getHeight(), (int)this.size.getWidth(), Image.SCALE_DEFAULT)); //Skallieren
 				}
 				else
 				{
 					grafiken[i] = new ImageIcon( ImageCreator.VERZEICHNIS + logikgatter.toString() + i.toString() + ImageCreator.DATEIENDUNG ); // Grafik erstellen
 					grafiken[i] = new ImageIcon(grafiken[i].getImage().getScaledInstance((int)this.size.getHeight(), (int)this.size.getWidth(), Image.SCALE_DEFAULT)); //Skallieren
 				}
 				
 			}
 			catch(InstantiationError e)// Grafik nicht vorhanden
 			{
 				grafiken[i] = null; 
 			}
 		}
 		
 		return grafiken;
 	}
 	
 	
 	/**
 	 * Generiert ein Array mit allen Versionen(Grafiken) des uebergebenen Bits
 	 * @param bit Bit von dem man alle Grafikversionen haben will.
 	 * @return Array mit Grafiken
 	 */
 	public ImageIcon[] getImage(boolean bit)
 	{
 		Boolean castBit = (Boolean)bit; // Um spaeter die toString() zu nutzen.
 		
 		ImageIcon[] grafiken = new ImageIcon[this.anzahlVersionen];
 		
 		for(Integer i = 0; i < anzahlVersionen;i++  )
 		{
 			// Bild muss noch mit richtiger Aufloesung zurueckgegeben werden
 			try
 			{
 				if(this.spiegeln)
 				{
 					grafiken[i] = new MirrorImageIcon( ImageCreator.VERZEICHNIS + castBit.toString() + i.toString() + ImageCreator.DATEIENDUNG ); // Grafik erstellen
 					grafiken[i] = new MirrorImageIcon(grafiken[i].getImage().getScaledInstance((int)this.size.getHeight(), (int)this.size.getWidth(), Image.SCALE_DEFAULT)); //Skallieren
 
 				}
 				else
 				{
 					grafiken[i] = new ImageIcon( ImageCreator.VERZEICHNIS + castBit.toString() + i.toString() + ImageCreator.DATEIENDUNG ); // Grafik erstellen
 					grafiken[i] = new ImageIcon(grafiken[i].getImage().getScaledInstance((int)this.size.getHeight(), (int)this.size.getWidth(), Image.SCALE_DEFAULT)); //Skallieren
 				}		
 			}
 			catch(InstantiationError e)// Grafik nicht vorhanden
 			{
 				grafiken[i] = null; 
 			}
 		}
 
 		return grafiken;
 	}
 	
 	public ImageIcon[] getImage()
 	{
 		ImageIcon[] grafiken = new ImageIcon[this.anzahlVersionen];
 		
		for(Integer i = 0; i < anzahlVersionen;i++  )
 		{
 			// Bild muss noch mit richtiger Aufloesung zurueckgegeben werden
 			try
 			{
 				{
 					grafiken[i] = new ImageIcon( ImageCreator.VERZEICHNIS + ImageCreator.PLATZHALTER + i.toString() + ImageCreator.DATEIENDUNG ); // Grafik erstellen
 					grafiken[i] = new ImageIcon(grafiken[i].getImage().getScaledInstance((int)this.size.getHeight(), (int)this.size.getWidth(), Image.SCALE_DEFAULT)); //Skallieren
 				}		
 			}
 			catch(InstantiationError e)// Grafik nicht vorhanden
 			{
 				grafiken[i] = null; 
 			}
 		}
 		
 		return grafiken;
 	}
 	
 	public void setAufloesung(Dimension size)
 	{
 		this.size = size;
 	}
 	
 	
 }
