 package com.roma3.infovideo.utility;
 
 import com.roma3.infovideo.model.Lezione;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 
 /**
  * Version 1.2
  * Copyright (C) 2012 Enrico Candino ( enrico.candino@gmail.com )
  *
  * This file is part of "Roma Tre".
  * "Roma Tre" is released under the General Public Licence V.3 or later
  *
  * @author Enrico Candino
  */
 public class AuleLibereUtility {
 
 	public ArrayList<String> findAuleLibere(ArrayList<String> aule, ArrayList<Lezione> lezioni, int oraSelezionata, int minutiSelezionati) {
 		
 		HashSet<String> auleOccupate = new HashSet<String>();
 		HashSet<String> auleLibere = new HashSet<String>();
 		ArrayList<Lezione> lezioniInCorso = new ArrayList<Lezione>();
 		
 		//GregorianCalendar ora = new GregorianCalendar();
 		//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 		//String oraAdesso = sdf.format(ora.getTime());
 
 		
 		String oraAdesso = Integer.toString(oraSelezionata)+":"+Integer.toString(minutiSelezionati);
 		
 		
 		for(Lezione l : lezioni)
 			if( (oraAdesso.compareTo(l.getDataInizioString())>=0) &&
 					(oraAdesso.compareTo(l.getDataFineString())<=0))
 				lezioniInCorso.add(l);
 			
 		for(Lezione l : lezioniInCorso)
			auleOccupate.add(l.getAula());
		
		for(String aula : aule)
			if(!auleOccupate.contains(aula))
				auleLibere.add(aula);
 
         ArrayList<String> auleOrdinate = new ArrayList<String>(auleLibere);
         Collections.sort(auleOrdinate);
 			
 		return auleOrdinate;
 	}
 
 }
