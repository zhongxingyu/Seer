 package Automat;
 
 import Fassade.Fassade;
 
 /**
  * @author Rensky
  * @version 1.0
  * @created 18-Nov-2012 16:41:59
  */
 public class Anzeige {
 
 	private Leuchte m_Leuchte;
 	private Aktor m_Troete;
 	private Display m_Display;
 	private SimpleThread leuchte = null, troete = null;
 
 	public Anzeige(Fassade fassade) {
 		m_Leuchte = new Leuchte(Adressen.Leuchte.ordinal(), Adressen.Leuchte_Frabe.ordinal());
 		m_Troete = new Aktor(Adressen.Troete.ordinal());
 		m_Display = new Display(Adressen.Display.ordinal());
 	}
 
 	/**
 	 * 
 	 * @param Text
 	 */
 	public void Display_akt(String text) {
 		m_Display.setText(text);
 	}
 
 	/**
 	 * 
 	 * @param errno
 	 */
 	public void FehlerMelden(int errno) {
		int pulseEin = 10, pulseAus = 10, wdh = 5;
 		
 
 		switch (errno) {
		case -1: m_Leuchte.setColor(2);
				 m_Leuchte.einschalten();
				 m_Troete.ausschalten();
				 return;
 		default:
 		case 0:
 			wdh = 5;
 			pulseEin = 800;
 			pulseAus = 800;
 			m_Leuchte.setColor(0);
 			break;
 		case 1:
 			wdh = 10;
 			pulseEin = 1500;
 			pulseAus = 1000;
 			m_Leuchte.setColor(1);
 			break;
 		case 2:
 			wdh = 3;
 			pulseEin = 500;
 			pulseAus = 500;
 			m_Leuchte.setColor(2);
 			break;
 		}
 		if(leuchte != null && leuchte.isAlive()){
 			leuchte.interrupt();			
 		}
 		if(troete != null && troete.isAlive()){
 			troete.interrupt();			
 		}
 		
 		leuchte = new SimpleThread(m_Leuchte, wdh, pulseEin, pulseAus, true);
 		troete = new SimpleThread(m_Troete, wdh, pulseEin+300, pulseAus-300, false);
 
 		leuchte.start();
 		troete.start();
 	}
 
 	public class SimpleThread extends Thread {
 
 		private Aktor aktor;
 		private int wdh, pulse_ein, pulse_aus;
 		private boolean endbehavior;
 
 		public SimpleThread(Aktor aktor, int wdh, int pulse_ein, 
 				int pulse_aus, boolean endbehavior) {
 			this.aktor = aktor;
 			this.wdh = wdh;
 			this.pulse_ein = pulse_ein;
 			this.pulse_aus = pulse_aus;
 			this.endbehavior = endbehavior;
 		}
 
 		public void run() {
 			for (int i = 0; i <= wdh; i++) {
 				if(isInterrupted()){
 					i=0;
 					break;
 				}
 				aktor.einschalten();
 				try {
 					sleep(pulse_ein);
 				} catch (InterruptedException ie) {
 					interrupt();
 					//leer
 				}
 				
 				if(isInterrupted()){
 					i=0;
 					break;
 				}
 				aktor.ausschalten();
 				try {
 					sleep(pulse_aus);
 				} catch (InterruptedException ie) {
 					interrupt();
 					//leer
 				}
 			}
 			if(endbehavior){
 				aktor.einschalten();
 			}else{
 				aktor.ausschalten();
 			}
 		}
 	}
 
 }
