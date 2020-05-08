 package ascenseur;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Calendar;
 /**
  * 
  * @author benji13
  * 
  * Classe permettant d'initialiser et instancier nos classes 
  *
  */
 
 public class Init {
 
 	/**
 	 * @param args
 	 * @throws InterruptedException 
 	 */
 	public static void main(String[] args) throws InterruptedException {
 		System.out.println("Simulation en mode manuel -- en mode Semaine ");
 	
 		//Creation du calendrier permettant de gnrer la date de base.
 		Calendar cal1 = Calendar.getInstance(); cal1.set(2012, 01, 15, 15, 00, 00);
 		
		int xtemps = 100;
 		
 		//Cration de plusieurs dates
 		Date date1 = cal1.getTime();
 		System.out.println("Date 1: " + date1);
 		cal1.add(Calendar.SECOND, 1);
 		Date date2 = cal1.getTime();
 		System.out.println("Date 2: " + date2);
 		cal1.add(Calendar.SECOND, 5);
 		Date date3 = cal1.getTime();
 		System.out.println("Date 3: " + date3);
 		cal1.add(Calendar.SECOND, 2);
 		Date date4 = cal1.getTime();
 		System.out.println("Date 4: " +date4);
 		
 		
 		//Cration d'appel  partir des dates prcedentes
 		Appel Appel1 = new Appel(0, 3, date1);
 		Appel Appel2 = new Appel(4, 13, date2);
 		Appel Appel3 = new Appel(10, 13, date3 );
 		Appel Appel4 = new Appel(13, 18, date4);
 		
 		//Ajoute les appel  la liste
 		ArrayList<Appel> ListeAppelsAtraiter = new ArrayList<Appel> ();
 		ListeAppelsAtraiter.add(Appel1);
 		ListeAppelsAtraiter.add(Appel2);
 		ListeAppelsAtraiter.add(Appel3);
 		ListeAppelsAtraiter.add(Appel4);
 		
 		//Cration des ascenseur
 		Ascenseur ascenseur1 = new Ascenseur(0,0,xtemps);
 		Ascenseur ascenseur2 = new Ascenseur(2,17,xtemps);
 		Ascenseur ascenseur3 = new Ascenseur(3,10,xtemps);
 		Ascenseur ascenseur4 = new Ascenseur(4,0,xtemps);
 		Ascenseur ascenseur5 = new Ascenseur(5,0,xtemps);
 		Ascenseur ascenseur6 = new Ascenseur(6,0,xtemps);
 		
 		//Cration du tableau d'ascenseur
 		ArrayList<Ascenseur> tabAscenseur = new ArrayList<Ascenseur>();
 		tabAscenseur.add(ascenseur1);
 		tabAscenseur.add(ascenseur2);
 		tabAscenseur.add(ascenseur3);
 		tabAscenseur.add(ascenseur4);
 		tabAscenseur.add(ascenseur5);
 		tabAscenseur.add(ascenseur6);
 		
 		//Cration de la batterie
 		Batterie laBatterie = new Batterie(xtemps);
 		
 		System.out.println("Assignement des 4 appels...");
 		//Assigne 2 appels  ascenseur 1
 		ascenseur1.addAppel(Appel1);
 		ascenseur1.setMonte(true);
 		//ascenseur1.addAppel(Appel2);
 		//ascenseur1.addAppel(Appel3);
 		//ascenseur1.addAppel(Appel4);
 		
 		
 		//Tri des appels
 		System.out.println("Tri des appels...");
 		ascenseur1.triAppel();
 		System.out.println("Ascenseur 1: Ma liste d'appel " + ascenseur1.getTabDestination());
 		
 		
 		//Cration du calendrier
 		Calendrier monCalendrier = new Calendrier(xtemps);
		System.out.println("Heure debut : " + monCalendrier.calculDateActuelle());
 				
 		System.out.println("Traitement des appels en cours...");
 		//Traitement des appels
 		monCalendrier.getChrono().start();
		System.out.println("Heure fin : " + monCalendrier.calculDateActuelle());
 		ascenseur1.deplacement();
 		ascenseur1.deplacement();
 	//	ascenseur1.deplacement();
 	//	ascenseur1.deplacement();
 	//	ascenseur1.deplacement();
 	//	ascenseur1.deplacement();
 	//	ascenseur1.setMonte(false);
 	//	ascenseur1.deplacement();
 		monCalendrier.getChrono().stop();
 		System.out.println("Heure fin : " + monCalendrier.calculDateActuelle());
 		System.out.println("FIN");
 	}
 
 }
