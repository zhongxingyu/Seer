 package net.rhatec.amtmobile.types;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Vector;
 
 import net.rhatec.amtmobile.R;
 
 import android.content.Context;
 import android.text.format.Time;
 
 /**
  * Classe d'aide pour g�rer les dates.
  * 
  */
 public class DateHelpers
 {
 	static int LUNDI = 0x1;
 	static int MARDI = 0x2;
 	static int MERCREDI = 0x4;
 	static int JEUDI = 0x8;
 	static int VENDREDI = 0x10;
 	static int SAMEDI = 0x20;
 	static int DIMANCHE = 0x40;
 	static int JOURFERIE = 0x80;
 	static int LAST = 0x100;
 	public static String obtenirNomHoraire(int horaireId, HashMap<String,String> jf, Context c)
 	{
 		String nomHoraire = "Unknown";
 			//This need an helper method
 			if (horaireId == 31) //Semaine
 				nomHoraire = c.getString(R.string.jour_semaine);
 			else if (horaireId == 32)
 				nomHoraire = c.getString(R.string.jour_samedi);
 			else if (horaireId == 64)
 				nomHoraire = c.getString(R.string.jour_dimanche);
 			else if(horaireId < 255) //
 			{
 				//TODO: Mettre des majuscule, des . et des et comme il se doit
 				nomHoraire = "";
 				nomHoraire+= (horaireId & LUNDI) != 0 ? c.getString(R.string.jour_lundi) + ", " : "";
 				nomHoraire+= (horaireId & MARDI) != 0 ? c.getString(R.string.jour_mardi) + ", ": "";
 				nomHoraire+= (horaireId & MERCREDI) != 0 ? c.getString(R.string.jour_mercredi) + ", ": "";
 				nomHoraire+= (horaireId & JEUDI) != 0 ? c.getString(R.string.jour_jeudi) + ", ": "";
 				nomHoraire+= (horaireId & VENDREDI) != 0 ? c.getString(R.string.jour_vendredi) + ", ": "";
 				nomHoraire+= (horaireId & SAMEDI) != 0 ? c.getString(R.string.jour_samedi) + ", ": "";
 				nomHoraire+= (horaireId & DIMANCHE) != 0 ? c.getString(R.string.jour_dimanche) + ", ": "";		
 			}
 			else
 			{
 				String nomHoraireF = jf.get(String.valueOf(horaireId));
 				if (nomHoraireF != null)
 					nomHoraire = nomHoraireF;
 			}
 		
 		return nomHoraire;
 	}
 
 	
 	
 	public static Pair<Integer, Boolean> obtenirTypeHoraireActuelAConsulter(Vector<Horaire> listeHoraire)
 	{
 		Calendar unCalendrier = Calendar.getInstance();
 		return obtenirTypeHoraireActuelAConsulter(listeHoraire, unCalendrier);
 	}
 	
 	/**
 	 * Retourne le type d'horaire a consulter (Dimanche, Samedi, Semaine) selon
 	 * l'heure actuelle.
 	 * datecompenser 
 	 * 
 	 * @return Nom horaire
 	 * 
 	 */
 	public static Pair<Integer, Boolean> obtenirTypeHoraireActuelAConsulter(Vector<Horaire> listeHoraire, Calendar unCalendrier)
 	{
 		Pair<Integer, Boolean> horaireAConsulterPair = new Pair<Integer, Boolean>(-1, false);
 
 		
 		//On v�rifie d'abord qu'il ne s'agit pas d'un horaire entre minuit et 4 heure du matin...
 		int hour = unCalendrier.get(Calendar.HOUR_OF_DAY);
		if(hour>=0 && hour<5) //Nous somme entre 5h et minuit... nous prendrons l'horaire pr�c�dent...
 		{
 			unCalendrier.add(Calendar.DATE, -1); //Nous prenons donc l'horaire d'hier
 			horaireAConsulterPair.setSecond(true); //Apr�s minuit
 		}
 
 		
 		// On vérifie ensuite si il s'agit d'un jour  férié.
 	
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
 		String date = sdf.format(unCalendrier.getTime());
 		//AAAAMMJJ
 		Horaire h = null;
 		int nbHoraire = listeHoraire.size();
 		for(int i = 0; i < nbHoraire; ++i)
 		{
 			h = listeHoraire.get(i);
 			if(h.ObtenirNom().equals(date))
 			{
 				horaireAConsulterPair.first = i;
 				break;
 			}
 		}
 		if(horaireAConsulterPair.first == -1)
 		{
 			int jourActuel = unCalendrier.get(Calendar.DAY_OF_WEEK);
 			int jourCourantBit = 0;
 			switch(jourActuel)
 			{
 			case Calendar.MONDAY:
 				jourCourantBit = LUNDI;
 				break;
 			case Calendar.TUESDAY:
 				jourCourantBit = MARDI;
 				break;
 			case Calendar.WEDNESDAY:
 				jourCourantBit = MERCREDI;
 				break;
 			case Calendar.THURSDAY:
 				jourCourantBit = JEUDI;
 				break;
 			case Calendar.FRIDAY:
 				jourCourantBit = VENDREDI;
 				break;
 			case Calendar.SATURDAY:
 				jourCourantBit = SAMEDI;
 				break;
 			case Calendar.SUNDAY:
 				jourCourantBit = DIMANCHE;
 				break;
 			default:
 				break;
 			}
 			
 			
 			for(int i = 0; i < nbHoraire; ++i)
 			{
 				h = listeHoraire.get(i);
 				String horaire = h.ObtenirNom();
 				int horaireId = Integer.parseInt(horaire);
 				if(horaireId<LAST && ((horaireId&jourCourantBit) != 0))
 				{
 					horaireAConsulterPair.setFirst(i);
 				}
 				
 			}		
 		}
 		
 		return horaireAConsulterPair;
 
 	}
 
 	public static boolean horaireApresMinuit(Time actuelTime)
 	{
 		// l'heure actuelle est comprise entre minuit et 5 heure du matin
 		if (actuelTime.hour >= 0 && actuelTime.hour < 4)
 			return true;
 		return false;
 	}
 
 	/**
 	 * @function: obtenirObjetTimeActuel
 	 * @description: Retourne la date et l'heure actuelle
 	 * @author: Hocine
 	 * @params[in]:
 	 * @params[out]:
 	 */
 	public static Time obtenirObjetTimeActuel()
 	{
 		Calendar unCalendrier = Calendar.getInstance();
 		Time time = new Time();
 		time.year = unCalendrier.get(Calendar.YEAR);
 		time.month = unCalendrier.get(Calendar.MONTH);
 		time.monthDay = unCalendrier.get(Calendar.DAY_OF_MONTH);
 		time.hour = unCalendrier.get(Calendar.HOUR_OF_DAY);
 		time.minute = unCalendrier.get(Calendar.MINUTE);
 		return time;
 	}
 
 	/**
 	 * @function: obtenirObjetDateActuel
 	 * @description: Retourne la date courante
 	 * @author: Jeep
 	 * @params[in]:
 	 * @params[out]:
 	 */
 	public static Date obtenirObjetDateActuel()
 	{
 		return Calendar.getInstance().getTime();
 	}
 
 	/**
 	 * @function: obtenirPositionListeProchainPassage
 	 * @description: Retourne la position dans la liste du prochain passage de
 	 *               l'autobus
 	 * @author: JP
 	 * @params[in]:
 	 * @params[out]:
 	 */
 	public static int obtenirPositionListeProchainPassage(ArrayList<Temps> _listeHeure)
 	{
 
 		return 0;
 	}
 
 }
