 package ascenseur;
 
 import java.util.Calendar;
 import java.util.Date;
 
 public class Calendrier extends Thread{
 
 	public Calendrier(int xtemps,Seconde sec) throws InterruptedException {
 		// TODO Auto-generated constructor stub
 		this.chrono = new Chronometre(xtemps,sec);
 		this.dateDebutSimu = Calendar.getInstance();
 		this.dateDebutSimu.set(1970, 0, 1, 3, 59,00);
 		this.dateActuelle = Calendar.getInstance();
 		this.dateActuelle.set(1970, 0, 1, 3, 59,00);
 		this.xtemps = xtemps;
 		this.sec = sec;
 	}
 	
 	public Calendrier(int xtemps,Seconde sec, int jour, int mois, int annee, int heure) throws InterruptedException {
 		this.chrono = new Chronometre(xtemps,sec);
 		this.dateDebutSimu = Calendar.getInstance();
 		this.dateDebutSimu.set(annee, mois, jour, heure, 00,00);
 		this.dateActuelle = Calendar.getInstance();
 		this.dateActuelle.set(annee, mois,jour , heure, 00,00);
 		this.xtemps = xtemps;
 		this.sec = sec;
 	}
 	
 	
 	// VARIABLES
 	private Chronometre chrono;
 	private boolean isWeek;
 	private Calendar dateActuelle;
 	private Calendar dateDebutSimu;
 	private int xtemps;
 	private Seconde sec;
 	
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
 	
 	public Calendar getDateActuelle() {
 		return this.dateActuelle;
 	}
 	
 	public Calendar getDateDebutSimu() {
 		return dateDebutSimu;
 	}
 	
 	public int getXtemps() {
 		return xtemps;
 	}
 	
 	public void setDateActuelle(Calendar dateActuelle) {
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
 		isWeek = true;
        System.out.println(getDateActuelle().getTime()+"////"+getDateActuelle().get(Calendar.HOUR_OF_DAY)+" ///// "+getDateActuelle().get(Calendar.DAY_OF_WEEK));
		if(getDateActuelle().get(Calendar.HOUR_OF_DAY) > 18 || getDateActuelle().get(Calendar.HOUR_OF_DAY) < 7 || getDateActuelle().get(Calendar.DAY_OF_WEEK)== Calendar.SATURDAY || getDateActuelle().get(Calendar.DAY_OF_WEEK)== Calendar.SUNDAY){
 			isWeek = false;
 		}
 		Batterie.setSemaine(isWeek);
 	}
 
 	public void afficherHeure() throws InterruptedException{
 		for(;;){
 			sec.attenteSeconde(1);
 			this.dateActuelle.add(Calendar.SECOND, 1);
 			//System.out.println(this.dateActuelle.getTime());
 			if(this.dateActuelle.get(Calendar.MINUTE) == 0 && this.dateActuelle.get(Calendar.SECOND) == 0){
 				determinerPlageHoraire();
 				Batterie.setSemaine(this.isWeek);
 				Batterie.setChangement(true);
 			}
 
 		}
 	}
 	
 	public void run(){
 		try {
 			afficherHeure();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
