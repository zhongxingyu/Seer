 package logic;
 
 import java.util.ArrayList;
 
 import exceptions.DataException;
 
 /**
  * De vraag, hier staat alle informatie en logica over de vraag in.
  */
 public class Vraag {
 	private String								_tekst;
 	private ArrayList<Onderdeel>				_onderdelen;
 	private ArrayList<logic.GekozenAntwoord>	_antwoorden		= new ArrayList<logic.GekozenAntwoord>();
 	private int									huidigOnderdeel	= -1;
 	private int									moetGoedHebben = 9;
 	private int									hoeveelWaard = 20;
 
 	/**
 	 * @param vraag
 	 * @param onderdelen
 	 * @throws DataException
 	 */
 	public Vraag(String vraag, ArrayList<Onderdeel> onderdelen) throws DataException {
 		setTekst(vraag);
 		setOnderdelen(onderdelen);
 	}
 
 	/**
 	 * @return
 	 */
 	public ArrayList<GekozenAntwoord> getGekozenAntwoorden() {
 		return _antwoorden;
 	}
 
 	/**
 	 * @return
 	 */
 	public Onderdeel getHuidigeOnderdeel() {
 		if (huidigOnderdeel > _onderdelen.size() - 1) return null;
 
 		return _onderdelen.get(huidigOnderdeel);
 	}
 
 	/**
 	 * @return
 	 */
 	public ArrayList<Onderdeel> getOnderdelen() {
 		return _onderdelen;
 	}
 
 	/**
 	 * @return
 	 */
	public int getScore() {
 		int score = 0;
 
 		for (GekozenAntwoord gk : _antwoorden)
 			if (gk.isGoed()) score++;
 
 		return score;
 	}
 
 	public String getTekst() {
 		return _tekst;
 	}
 
 	public GekozenAntwoord kiesAntwoord(logic.Onderdeel gekozenOnderdeel) {
 		GekozenAntwoord gk = new GekozenAntwoord(getHuidigeOnderdeel(), gekozenOnderdeel);
 		_antwoorden.add(gk);
 		return gk;
 	}
 
 	private void setOnderdelen(ArrayList<Onderdeel> onderdelen) throws DataException {
 		if (onderdelen.size() != 9) throw new DataException("De hoeveelheid vragen klopt niet.");
 		_onderdelen = onderdelen;
 	}
 
 	public void setTekst(String tekst) {
 		_tekst = tekst;
 	}
 
 	public void volgendeOnderdeel() {
 		huidigOnderdeel++;
 	}
 
 	public void wijzigAntwoord(Onderdeel van, Onderdeel naar) {
 		GekozenAntwoord gkVan = null;
 		GekozenAntwoord gkNaar = null;
 
 		for (GekozenAntwoord gk : _antwoorden) {
 			if (gk.getGekozenOnderdeel() == van) gkVan = gk;
 
 			if (gk.getGekozenOnderdeel() == naar) gkNaar = gk;
 		}
 		if (gkVan != null) gkVan.setGekozenOnderdeel(naar);
 		if (gkNaar != null) gkNaar.setGekozenOnderdeel(van);
 	}
 
 	/**
 	 * @return the hoeveelWaard
 	 */
 	public int getHoeveelWaard() {
 		return hoeveelWaard;
 	}
 
 	/**
 	 * @param hoeveelWaard the hoeveelWaard to set
 	 */
 	public void setHoeveelWaard(int hoeveelWaard) {
 		this.hoeveelWaard = hoeveelWaard;
 	}
 
 	/**
 	 * @return mag doorspelen?
 	 */
 	public boolean magDoorspelen() {
		return getScore() >= getMoetGoedHebben();
 	}
 
 	/**
 	 * @return the moetGoedHebben
 	 */
 	public int getMoetGoedHebben() {
 		return moetGoedHebben;
 	}
 
 	/**
 	 * @param moetGoedHebben the moetGoedHebben to set
 	 */
 	public void setMoetGoedHebben(int moetGoedHebben) {
 		this.moetGoedHebben = moetGoedHebben;
 	}
 
 }
