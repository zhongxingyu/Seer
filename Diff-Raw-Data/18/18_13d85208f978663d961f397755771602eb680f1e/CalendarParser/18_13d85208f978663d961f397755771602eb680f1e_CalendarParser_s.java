 package com.iut.ptut.ctrl;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.fortuna.ical4j.data.CalendarBuilder;
 import net.fortuna.ical4j.data.ParserException;
 import net.fortuna.ical4j.model.Calendar;
 import net.fortuna.ical4j.model.Component;
 
 import com.iut.ptut.model.Day;
 import com.iut.ptut.model.Lesson;
 import com.iut.ptut.model.TimeTable;
 
 /**
  * Cette classe regroupe les mthodes permettant de parser les fichiers ICS de l'IUT.
  * @author Benot Sauvre
  */
 public class CalendarParser {
 
 	
 	private static Logger _log = Logger.getLogger(CalendarParser.class.getName());
 	
 	
 	
 	/**
 	 * @param args
 	 * @throws ParserException 
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) throws IOException, ParserException {
		Calendar calendar = CalendarParser.convertirICSEnICal4J("S4_08.ics");
//		CalendarParser.convertirCalendarEnTimeTable(calendar, "2B");
		System.out.println(CalendarParser.convertirCalendarEnTimeTable(calendar, "2B"));
 	}
 	
 	private CalendarParser() {
 	}
 	
 	/**
 	 * Convertit un Calendar de type "iCal4j" en TimeTable de notre application.
 	 * Ce TimeTable contientra tout les cours pour tout les groupes.
 	 * @param cal Un calendar "iCal4j"
 	 * @return Un TimeTable avec toute les donnes du calendar, pour tout les groupes.
 	 */
 	public static TimeTable convertirCalendarEnTimeTable(Calendar cal) {
 		return CalendarParser.convertirCalendarEnTimeTable(cal, "");
 	}
 	
 	/**
 	 * Convertit un Calendar de type "iCal4j" en TimeTable de notre application.
 	 * Ce TimeTable contientra tout les cours pour tout les groupes.
 	 * @param cal Un calendar "iCal4j"
 	 * @param groupe Le groupe pour lequel on veut l'emploi du temps. Si la chaine est vide, on prendre tout les cours de tout les groupes.
 	 * Il peut tre de la forme : "2", "2B", "A" (cours communs). 
 	 * @return Un TimeTable avec les donnes du calendar
 	 */
 	@SuppressWarnings("rawtypes")
 	public static TimeTable convertirCalendarEnTimeTable(Calendar cal, String groupe) {
 		
 		TimeTable tt = new TimeTable();
 		// Une map associant le timestamp du dbut du jour  l'objet du jour correspondant
 		HashMap<Long, Day> mapJours = new HashMap<Long, Day>();
 		
 		// On parcourt le ICS "lu"
 		for (Iterator i = cal.getComponents().iterator(); i.hasNext();) {
 		    Component component = (Component) i.next();
 		    
 		    // Si c'est un cours
 		    if("VEVENT".equals(component.getName())) {
 		    	
 		    	try{ 
 		    		Lesson cour = new Lesson(component);
 
 		    		// Si le cours appartient au groupe choisis
 		    		if(cour.estDansLeGroupe(groupe)) {
 			    		long debutJourCours = DateTools.calculeTimestampDebutJour(cour.getDateDebut());
 			    		Day jour = mapJours.get(debutJourCours);
 			    		
 			    		// Si le jour n'a pas encore t cre, on l'ajoute  la map
 			    		if(jour == null){
 			    			jour = new Day();
 			    			mapJours.put(debutJourCours, jour);
 			    		}
 			    		// On ajoute le cours au jour
 			    		jour.addLesson(cour);
 		    		}
 		    		
 		    	} catch (Exception e) {
 		    		_log.log(Level.SEVERE, "Impossible de convertir le VEVENT en Lesson.");
 		    		e.printStackTrace();
 		    	}
 		    }
 		}
 		
 		// Une fois tout les cours lu, on ajoute les jours  l'emploi du temps
 		for(long key : mapJours.keySet()) {
 			tt.addDay(mapJours.get(key));
 		}
 		
 		return tt;
 	}
 	
 	
 	
 	/**
 	 * Parse un fichier ICS en un objet de type Calendar de le librairie iCal4J
 	 * @param cheminFichierICS Le chemin vers le ficheir ICS
 	 * @return Un objet de type net.fortuna.ical4j.model.Calendar
 	 * @throws IOException
 	 * @throws ParserException
 	 */
 	public static Calendar convertirICSEnICal4J(String cheminFichierICS) throws IOException, ParserException {
 		// On ouvre un flux de lecture sur le fichier : 
 		FileInputStream fin = new FileInputStream(cheminFichierICS);
 		// On le fait parser par iCal4J
 		CalendarBuilder builder = new CalendarBuilder();
 		Calendar result = builder.build(fin);
 		// On ferme le flux
 		fin.close();
 		return result;
 	}
 	
 	
 
 	/**
 	 * Lit le contenu du summary pour en recuprer les informations.
 	 * @param summaryFromICS le contenu d'une ligne de fichier ICS<br/> Format : "formation specialite/semestre matiere type groupe -- intervenant"
 	 * @return HashMap contenant les entres :<br/>
 	 * <ul>
 	 * 	<li>formation</li>
 	 * 	<li>specialite</li>
 	 * 	<li>semestre</li>
 	 * 	<li>matiere</li>
 	 * 	<li>intervenant</li>
 	 * 	<li>type (TD ou TP) (sauf cours amphi)</li>
 	 * 	<li>groupe (ex : 2 ou 2A) (sauf cours amphi)</li>
 	 * </ul>
 	 */
 	public static HashMap<String, String> parseSummary(String summaryFromICS) throws ParsingProblemException {
 		
 		HashMap<String, String> resultat = new HashMap<String, String>();
 		String[] split = summaryFromICS.split(" ");
 		
 		// Premire vrification de format :
 		if(split.length != 8 && split.length != 6) {
 			throw new ParsingProblemException("Le summary ne contient pas le bon nombre d'espaces.");
 		}
 		
 		// Si le cours est un amphi
 		if(split.length == 6) {
 			// On lit les diffrentes informations
 			resultat.put("formation", split[0]);
 			resultat.put("matiere", split[2]);
 			resultat.put("intervenant", split[5]);
 			
 			// SPECIAL - On spare la spcialit du semestre (un /)
 			String[] splitPartie2 = split[1].split("/");
 			if(splitPartie2.length != 2) {
 				throw new ParsingProblemException("Le deuxime bloc du summary est malform.");
 			}
 			resultat.put("specialite", splitPartie2[0]);
 			resultat.put("semestre", splitPartie2[1]);
 		
 		// Si c'est un cours normal
 		} else {
 		
 			// On lit les diffrentes informations
 			resultat.put("formation", split[0]);
 			resultat.put("matiere", split[2]);
 			resultat.put("type", split[3]);
 			resultat.put("groupe", split[5]);
 			resultat.put("intervenant", split[7]);
 	
 			
 			// SPECIAL - On spare la spcialit du semestre (un /)
 			String[] splitPartie2 = split[1].split("/");
 			if(splitPartie2.length != 2) {
 				throw new ParsingProblemException("Le deuxime bloc du summary est malform.");
 			}
 			resultat.put("specialite", splitPartie2[0]);
 			resultat.put("semestre", splitPartie2[1]);
 		}
 		
 		// On vrifie qu'il n'y ai pas de valeurs vide
 		for(String key : resultat.keySet()) {
 			if(resultat.get(key).equals("")) {
 				throw new ParsingProblemException("Une des valeurs est vide.");
 			}
 		}
 		return resultat;
 	}
 	
 	/**
 	 * Filtre une liste de Lesson pour en extraire les cours d'un groupe de TP
 	 * @param lessons La liste de lessons  filtrer
 	 * @param groupeTP Le groupe de TP. Le numro de sous groupe peut-tre omis. Le groupe "A" dsigne seulement les cours communs  tous.
 	 * @return La liste des cours filtrs.
 	 */
 	public static List<Lesson> filterCoursGroupeDonne(List<Lesson> lessons, String groupeTP) {
 		
 		if(groupeTP.length() > 2) {
 			throw new InvalidParameterException("Le paramtre groupeTP n'est pas correctement renseign. Il doit ressembler  par exemple  3 ou 3B.");
 		}
 		
 		List<Lesson> resultat = new ArrayList<Lesson>();
 		
 		for(Lesson lesson : lessons) {
 			// Si le cours est pour le groupe de TD/TP ou pour tout le monde (Amphi...)
 			if(lesson.estDansLeGroupe(groupeTP))
 				resultat.add(new Lesson(lesson));
 		}
 		
 		return resultat;
 	}
 	
 }
