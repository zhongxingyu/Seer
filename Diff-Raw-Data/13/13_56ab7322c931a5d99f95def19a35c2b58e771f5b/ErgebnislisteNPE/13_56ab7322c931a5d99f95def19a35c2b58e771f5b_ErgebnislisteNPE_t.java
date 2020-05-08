 /**
  * 
  */
 package swa.runningeasy.data.npe;
 
 import java.util.Map;
 
 import swa.runningeasy.data.be.ErgebnisBE;
 import swa.runningeasy.data.be.LaeuferBE;
 
 /**
  * @author Tim Schmiedl (Cyboot)
  * 
  */
 public abstract class ErgebnislisteNPE extends AbstractNPE {
 
 	private Map<LaeuferBE, ErgebnisBE>	ergebnisMap	= null;
 
 	/**
	 * @return the ergebnisMap
 	 */
	public synchronized Map<LaeuferBE, ErgebnisBE> getErgebnisMap() {
 		return ergebnisMap;
 	}
 
 	/**
	 * @param ergebnisMap
	 *            the ergebnisMap to set
 	 */
	public synchronized void setErgebnisMap(final Map<LaeuferBE, ErgebnisBE> ergebnisMap) {
 		this.ergebnisMap = ergebnisMap;
 	}
 }
