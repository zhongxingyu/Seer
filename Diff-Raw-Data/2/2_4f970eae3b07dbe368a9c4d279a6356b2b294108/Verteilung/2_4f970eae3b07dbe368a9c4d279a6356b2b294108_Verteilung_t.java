 package Automat;
 
 public class Verteilung {
 	
 //Variablen
 
 		private Auswahlklappe m_Auswahlklappe;
 		
 		private Laufband m_HinteresLaufband;
 		
 		private Sensor s_AuswahlklappeEingangsLichtschranke;
 		private Sensor s_MehrwegBehaelterLichtschranke;
 		private Sensor s_PetBehaelterLichtschranke;
 		
 		private ParallelWarteClass workerThread;
 	
 //Konstruktor
 		
 		public Verteilung(){
 
 			m_Auswahlklappe = new Auswahlklappe(Adressen.Auswahlklappe.ordinal());
 			m_HinteresLaufband = new Laufband(Adressen.LaufbandAusgang.ordinal());
 			s_AuswahlklappeEingangsLichtschranke = new Sensor(Adressen.AuswahlklappeEingangslichtschranke.ordinal());
 			s_MehrwegBehaelterLichtschranke = new Sensor(Adressen.UebergabelichtschrankeMehrweg.ordinal());
 			s_PetBehaelterLichtschranke = new Sensor(Adressen.UebergabelichtschrankePET.ordinal());
 			
 			workerThread = new ParallelWarteClass(10000);
 		}
 
 //Methoden
 		
 		protected boolean Durchlauf(Sensor s, FlaschenType Flasche){
 			
 			//Vorwrts + Auswahlklappe
 			m_HinteresLaufband.vorwaerts();
 			m_Auswahlklappe.stellen(Flasche);
 			
 			workerThread.run();
 			
 			//Warten
 			while(!s.read() && workerThread.isAlive());
 			
 			//Wenn nicht mehr Aktiv, dann Fehler
 			if (!workerThread.isAlive()){
 				
 				return false;
 			}
 			
 			//Wenn Aktiv, dann Unterbrechen
 			if(workerThread.isAlive()){
 				workerThread.interrupt();			
 			}
 			
 			//Wieder Starten
 			workerThread.run();
 			
 			//Warten
 			while(s.read() && workerThread.isAlive());
 			
 			//Wenn nicht mehr Aktiv, dann Fehler
 			if (!workerThread.isAlive()){
 				
 				return false;
 			}
 			
 			//Stopp
 			m_HinteresLaufband.stop();

 			return true;
 		}
 		
 		public boolean Flasche_weiterleiten(FlaschenType Flasche){
 			
 			switch (Flasche) {
 			
 				case PET:
 									
 						return Durchlauf(s_PetBehaelterLichtschranke, Flasche);
 				
 				default:		
 					
 				case Mehrweg:
 												
 						return Durchlauf(s_MehrwegBehaelterLichtschranke, Flasche);
 		
 			}
 		}
 	
 		
 //OLD Version
 		
 //		public boolean getUebergabeLichtschrankeMehrweg(){
 //		
 //			return Durchlauf(s_MehrwegBehaelterLichtschranke);
 //			
 //		}
 //		
 //		public boolean getUebergabeLichtschrankePET(){
 //		
 //			return Durchlauf(s_PetBehaelterLichtschranke);
 //		}
 		
 }
