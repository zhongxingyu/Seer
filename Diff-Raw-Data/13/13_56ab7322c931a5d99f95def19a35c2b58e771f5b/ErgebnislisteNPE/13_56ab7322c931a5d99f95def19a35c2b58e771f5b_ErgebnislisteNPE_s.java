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
	 * @return the lauefer
 	 */
	public synchronized Map<LaeuferBE, ErgebnisBE> getLauefer() {
 		return ergebnisMap;
 	}
 
 	/**
	 * @param lauefer
	 *            the lauefer to set
 	 */
	public synchronized void setLauefer(final Map<LaeuferBE, ErgebnisBE> ergebnisMap) {
 		this.ergebnisMap = ergebnisMap;
 	}
 }
