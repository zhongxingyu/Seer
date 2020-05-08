 /**
  * Koordiniert das Spiel.
  * 
  * @author Mathias Garbe
  *
  */
 public class MemoryController {
 	private MemoryModel model;
 	private MemoryView view;
 	
 	public MemoryController(MemoryModel model, MemoryView view){
 		this.model = model;
 		this.view = view;
 	}
 	
 	/**
 	 * Mischt die Karten und startet die Spielschleife.
 	 */
 	public void starteSpiel()
 	{
 		this.model.mischeKarten();
 		
 		this.spielSchleife();
 	}
 	
 	private void spielSchleife()
 	{
 		
 		while(!this.model.istAbgeschlossen())
 		{
 			this.view.zeigeSpiel();
 			
 			this.view.zeigeTextAn("\nGeben Sie das aufzudeckende Paar Karten an.\n");
 			
 			int[][] koordinaten;
 			int kartenStatus = 0;
 			do
 			{
				if(kartenStatus == -1) this.view.zeigeTextAn("Ungltiger Zug! Bitte Eingabe berprfen!\n");
 				koordinaten = this.koordinatenEingabe();
 				kartenStatus = this.model.kartenAufdecken(this.view.uebersetzeKoordinaten(koordinaten[0]), 
 						                                  this.view.uebersetzeKoordinaten(koordinaten[1]));
 			} while(kartenStatus == -1);
 			
 			if(kartenStatus == 0)
 			{
 				this.view.zeigeTextAn("\nFalscher zug... \n");
 				
 				this.view.zeigeKarten(this.view.uebersetzeKoordinaten(koordinaten[0]), 
 									  this.view.uebersetzeKoordinaten(koordinaten[1]));
 				this.view.zeigeTextAn("\n");
 			}
 			else
 			{
 				this.view.zeigeTextAn("\nRichtiger zug, +1 Punkt ... \n\n");
 			}
 						
 		}
 		
 		this.view.zeigeTextAn("\nDas Spiel ist beendet. Der Sieger ist:");
 		int[] spielerPunkte = this.model.getSpielerPunkte();
 		if(spielerPunkte[0] > spielerPunkte[1])
 		{
 			this.view.zeigeTextAn("Spieler A.");
 		}
 		else if(spielerPunkte[0] < spielerPunkte[1])
 		{
 			this.view.zeigeTextAn("Spieler B.");
 		}
 		else
 		{
 			this.view.zeigeTextAn("Keiner, da unentschieden.");
 		}
 	}
 	
 	private int[][] koordinatenEingabe()
 	{
 		int[][] koordinaten = new int[2][2];
 
 		this.view.zeigeTextAn("Spalte Karte 1: ");
 		koordinaten[0][0] = Eingabe.readInt();
 		this.view.zeigeTextAn("Zeile Karte 1: ");
 		koordinaten[0][1] = Eingabe.readInt();
 		
 		this.view.zeigeTextAn("Spalte Karte 2: ");
 		koordinaten[1][0] = Eingabe.readInt();
 		this.view.zeigeTextAn("Zeile Karte 2: ");
 		koordinaten[1][1] = Eingabe.readInt();
 		
 		return koordinaten;
 	}
 }
