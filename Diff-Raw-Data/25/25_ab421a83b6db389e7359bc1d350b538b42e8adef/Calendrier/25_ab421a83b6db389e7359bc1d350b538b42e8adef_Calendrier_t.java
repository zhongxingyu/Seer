 package ascenseur;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class Calendrier {
 
 	public Calendrier(int xtemps) throws InterruptedException {
 		// TODO Auto-generated constructor stub
 		this.chrono = new Chronometre();
 		this.isWeek = true;
		this.dateActuelle = new Date(2012, 01, 15, 00, 00,00);
 		this.dateDebutSimu = Calendar.getInstance();
		dateDebutSimu.set(2012, 01, 15, 00, 00,00);
 		this.xtemps = xtemps;
 	}
 	
 	// VARIABLES
 	private Chronometre chrono;
 	private boolean isWeek;
 	private Date dateActuelle;
 	private Calendar dateDebutSimu;
 	private int xtemps;
 	
 	// GETTER SETTER
 	public Chronometre getChrono() {
 		return chrono;
 	}
 	public void setChrono(Chronometre chrono) {
 		this.chrono = chrono;
 	}
 	
 	public boolean isWeek() {
 		return isWeek;
 	}
 	
 	public void setWeek(boolean isWeek) {
 		this.isWeek = isWeek;
 	}
 	
 	public Date getDateActuelle() {
 		return dateActuelle;
 	}
 	
 	public Calendar getDateDebutSimu() {
 		return dateDebutSimu;
 	}
 	
 	public int getXtemps() {
 		return xtemps;
 	}
 	
 	public void setDateActuelle(Date dateActuelle) {
 		this.dateActuelle = dateActuelle;
 	}
 	
 	public void setDateDebutSimu(Calendar dateDebutSimu) {
 		this.dateDebutSimu = dateDebutSimu;
 	}
 	
 	public void setXtemps(int xtemps) {
 		this.xtemps = xtemps;
 	}
 	//FONCTIONS
 	public void determinerPlageHoraire(){
 		
 	}
 	/**
 	 * Fonction permettant d'ajouter le temps coul  la date de dpart de la simulation pour savoir la date actuelle
 	 * @return
 	 */
 	public Date calculDateActuelle(){
 		this.dateDebutSimu.add(Calendar.SECOND, (int) (this.chrono.getTempsEcouleSecs()*this.xtemps));
 		this.dateActuelle = this.dateDebutSimu.getTime();
 		return this.dateActuelle;
 	}
 
 }
