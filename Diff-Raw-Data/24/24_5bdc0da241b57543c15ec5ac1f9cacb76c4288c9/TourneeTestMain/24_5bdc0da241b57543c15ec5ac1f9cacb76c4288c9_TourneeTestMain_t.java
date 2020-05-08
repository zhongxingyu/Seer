 package beans;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import util.PlageHoraire;
 
 
 public class TourneeTestMain {
 
 
 	public static void generationTournee(List<Visite> visiteList, List<Personnal> aideSoignantList){
 
 		//tri de la liste des patients par priorite
 		Collections.sort(visiteList, Visite.VisitePriorityComparator);
 
 		boolean visiteACaser;
 		//utiliser le calendrier par défaut
 		Calendar calendar=Calendar.getInstance();
 
 		//Pour chaque visite de la liste trie
 		for(Visite visite : visiteList){
 			visiteACaser = true;
 			calendar=Calendar.getInstance();
 			calendar.set(Calendar.MINUTE,00);
 			calendar.set(Calendar.SECOND,00);
 			calendar.set(Calendar.MILLISECOND,00);
 
 			do{			
 				calendar.add(Calendar.DAY_OF_MONTH, 1);
 			}while(calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
 
 			while(visiteACaser){
 
 				//recuperer la liste des personnels soignants travaillant ce jour
 				ArrayList<Personnal> aideSoignantJour = new ArrayList<Personnal>();
 				for(Personnal p: aideSoignantList) {
 					if(p.workThisDay(calendar.get(Calendar.DAY_OF_WEEK))) {
 						aideSoignantJour.add(p);
 					}
 				}
 
 				//creer une plage d'horaire de (temps de visite)H commençant a 8h (par exemple)
 				calendar.set(Calendar.HOUR_OF_DAY,8);
 
 				Date debut = calendar.getTime();
 				calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + visite.getDuree());
 				Date fin = calendar.getTime();
 
 				PlageHoraire plageHoraire = new PlageHoraire(debut, fin);
 
 				calendar.set(Calendar.HOUR_OF_DAY, 20);
 				Date finJournee = calendar.getTime();
 
 				//tant que visiteACaser == true et la fin de plage d'horaire <= 20h
 				while(visiteACaser && plageHoraire.getFin().before(finJournee)){
 					System.out.println();
 					System.out.println(plageHoraire);
 					//pour chaque personnel
 					for(Personnal p: aideSoignantJour){
 
 						//verifier si la plage d'horaire est disponible pour le personnel
 						if (p.isAvailable(plageHoraire)){
 							System.out.println("add");
 							visiteACaser = false;
 							//modifier la visite avec les infos
 							visite.setPersonnel(p);
 							visite.setPlageHoraire(plageHoraire);
 							visite.setStatus(VisiteStatus.TO_VISITE);
 							p.addVisite(visite);
							break;
 						}		
 					}
 					//decaler la plage d'horaire d'une heure
 					plageHoraire.decaler(1);
 				}
 				// jour suivant	
 				do{			
 					calendar.add(Calendar.DAY_OF_MONTH, 1);
 				}while(calendar.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY);
 				System.out.println("jour +1");
 			}
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		PatientBean patient = new PatientBean();
 		patient.setName("TONG");
 		PatientBean patient2 = new PatientBean();
 		patient2.setName("ROY");
 
 
 		Visite visite = new Visite();
 		visite.setStatus(VisiteStatus.TO_PLACE);
 		visite.setPatient(patient);
 		visite.setDuree(2);
 		visite.setPriority(1);
 		patient.getVisiteList().add(visite);
 
 		Visite visite2 = new Visite();
 		visite2.setStatus(VisiteStatus.TO_PLACE);
 		visite2.setPatient(patient2);
 		visite2.setDuree(1);
 		visite2.setPriority(2);
 		patient2.getVisiteList().add(visite2);
 		
 		List<Visite> visiteList = new ArrayList<Visite>();
 		visiteList.add(visite);
 		visiteList.add(visite2);
 
 		Personnal personnal = new Personnal();
 		personnal.setName("Tony");
 		Set<Integer> workDay = new HashSet<Integer>();
 		workDay.add(Calendar.WEDNESDAY);
 		personnal.setDaysOfWeek(workDay);
 		
		Personnal personnal2 = new Personnal();
		personnal2.setName("Etienne");
		Set<Integer> workDay2 = new HashSet<Integer>();
		workDay2.add(Calendar.WEDNESDAY);
		personnal2.setDaysOfWeek(workDay);
 		
 		List<Personnal> aideSoignantList = new ArrayList<Personnal>();
 		aideSoignantList.add(personnal);
		aideSoignantList.add(personnal2);
 		
 		
 		generationTournee(visiteList, aideSoignantList);
 		
		for(Personnal p : aideSoignantList){
			System.out.println("--------------------");
			System.out.println(p.getName());
			System.out.println("--------------------");
			for(Visite v: p.getVisitList()){
				System.out.println(v.getPatient().getName());
				System.out.println(v.getPlageHoraire());
				System.out.println();
			}
 		}
 
 
 
 
 
 	}
 
 }
