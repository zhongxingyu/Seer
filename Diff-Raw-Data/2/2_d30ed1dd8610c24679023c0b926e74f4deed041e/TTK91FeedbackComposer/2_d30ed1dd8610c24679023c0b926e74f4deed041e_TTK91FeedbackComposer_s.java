 package fi.helsinki.cs.koskelo.analyser;
 
 import fi.helsinki.cs.koskelo.common.*;
 
 import fi.hy.eassari.showtask.trainer.AttributeCache;
 import fi.hy.eassari.showtask.trainer.Feedback;
 import fi.hy.eassari.showtask.trainer.CacheException;
 
 
 /**
  * Luokka, joka luo kriteerien perusteella palautteen.
  * @author Tom Bertell, Koskelo-projekti
  */
 
 public class TTK91FeedbackComposer{
 
 	public TTK91FeedbackComposer() {
 	}
 
 
 	/**
 	 * Metodi, joka muodostaa palautteen. Ensin tutkii jokaista kriteeri 
 	 * kohden onko se oikein ja muodostaa palautteen sen mukaisesti. 
 	 * Seuraavaksi haetaan rekisterien arvot, statistiikka ja tulosteet, 
 	 * jotka laitetaan taulukoihin. Palaute on html-taulukoissa.
 	 *
 	 * @param analyseResults Analyserin tehtvn antama analyysin tulos
 	 * @param taskFeedback   tehtvn liittyvt palautteet
 	 * @param cache          haetaan tehtvn liittyvi attribuutteja
 	 * @param taskID         tehtvn tunnus
 	 * @param language       kytetty kieli
 	 * @return Feedback-olion, joka sislt palautteen
 	 * @throws CacheException jos tulee ongelmia attribuuttien 
 	 *                        hakemisessa cachesta
 	 */
 
 	public static Feedback formFeedback(TTK91AnalyseResults analyseResults,
 																			TTK91TaskFeedback taskFeedback,
 																			AttributeCache cache,
 																			String taskID,
 																			String language) throws CacheException {
 		
 		System.err.println("Tultiin formFeedbackiin");
 
 		StringBuffer feedbackTable = new StringBuffer(); // palaute html-muodossa
 
 		String criteriaHeader = ""; // ksiteltvn kriteerin nimi
 		String feedback = "";       // ksiteltv palaute
 		String quality = "";        // ksiteltv laadullinen palaute
 		int evaluation = 100;       // oletetaan aluksi, ett tehtv on oikein
 		Boolean correct = null;     // sislt tiedon kriteerin oikeellisuudesta
 
 
 		String criteriaLabel = cache.getAttribute // kriteerin otsikko
 	    ("A", "ttk91feedbackcomposer", "criteriaLabel", language);
 
 		String feedbackLabel = cache.getAttribute // palautteen otsikko
 	    ("A", "ttk91feedbackcomposer", "feedbackLabel", language);
 
 		String qualityLabel = cache.getAttribute // laadullisen pal. otsikko
 	    ("A", "ttk91feedbackcomposer", "qualityLabel", language);
 
 
 
 		/************************************************
 		 * Kriteerikohtaisen palautteen lisminen
 		 ************************************************/
 		
 		// Luodaan taulukon alkuosa.
 
 		feedbackTable.append("<table width=\"100%\" border=\"1\" cellspacing=\"0\"" 
 												 +"cellpadding=\"3\">"
 												 +"<tr align=\"center\">" 
 												 +"<td class=\"tableHeader\" align =\"left\"" 
 												 +"width=\"20%\">" +criteriaLabel +"</td>"
 												 +"<td class=\"tableHeader\" align =\"left\""
 												 +"width=\"40%\">"+feedbackLabel +"</td>"
 												 +"<td class=\"tableHeader\" align =\"left\""
 												 +"width=\"40%\">"+qualityLabel +"</td>"
 												 +"</tr>");
 	
 		/**
 		 * Seuraavassa kydn lpi jokainen kriteerityyppi ja
 		 * katsotaan onko kriteerityyppi oikein, vrin tai onko
 		 * sit mritelty ollenkaan. Jos kriteeri
 		 * on oikein/vrin, haetaan positiivinen/negatiivinen palaute.
 		 * Lisksi tarkistetaan onko kriteerityypin laadullinen osa oikein
 		 * ja haetaan tarvittaessa laadullinen palaute. 
 		 */
 
 			
 		// Hyvksytty koko, kriteeriin ei liity laadullista palautetta
 
 		correct = analyseResults.getAcceptedSize();
 		if (correct != null) { // null tarkoittaa, ett kriteerityyppi ei ole
 
 	    // haetaan kriiterin kielikohtainen otsikko
 
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"acceptedSizeHeader", language);
 	    if (correct.booleanValue()) { // kriteeri oikein
 				
 				feedback = taskFeedback.getAcceptedSizeFeedbackPositive();
 
 	    } else { // kriteeri vrin
 
 				feedback = taskFeedback.getAcceptedSizeFeedbackNegative();
 
 				// Jos yksikin kriteeri on vrin, vastausta ei hyvksyt.
 
 				evaluation = 0;
 	    } 
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 
 		// Optimaalinen koko, kriteeriin ei liity laadullista palautetta
 
 		correct = analyseResults.getOptimalSize();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"optimalSizeHeader", language);
 	    if (correct.booleanValue()) {
 				
 				feedback = taskFeedback.getOptimalSizeFeedbackPositive();
 	    } 
 	    else {
 			
 				feedback = taskFeedback.getOptimalSizeFeedbackNegative();
 	    }
 
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 
 		// Muistiin viittaukset, kriteeriin ei liity laadullista palautetta
 
 		correct = analyseResults.getMemoryReferences();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"memoryReferencesHeader", language);
 	    if (correct.booleanValue()) { 
 			
 				feedback = taskFeedback.getMemoryReferencesFeedbackPositive();
 
 	    } else { 
 
 				feedback = taskFeedback.getMemoryReferencesFeedbackNegative();
 				evaluation = 0;
 	    } 
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 
 		// Vaaditut kskyt
 		
 		correct = analyseResults.getRequiredCommands();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"requiredCommandsHeader", language);
 	    if (correct.booleanValue()) {
 				
 				feedback = taskFeedback.getRequiredCommandsFeedbackPositive();
 
 	    } else {
 
 				feedback = taskFeedback.getRequiredCommandsFeedbackNegative();
 				evaluation = 0;
 	    } 
 	    // tutkitaan onko laadullinen kriteeri oikein
 	    Boolean res = analyseResults.getRequiredCommandsQuality();
 	    
 	    if (  (res != null) && (res.booleanValue()) ) { 
 				quality = taskFeedback.getRequiredCommandsFeedbackQuality();
 	    } else {
 				quality = "";
 	    }
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 	
 
 		// Kielletyt kskyt
 		
 		correct = analyseResults.getForbiddenCommands();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"forbiddenCommandsHeader", language);
 	    if (correct.booleanValue()) {
 			
 				feedback = taskFeedback.getForbiddenCommandsFeedbackPositive();
 
 	    } else {
 
 				feedback = taskFeedback.getForbiddenCommandsFeedbackNegative();
 				evaluation = 0;
 	    } 
 	    Boolean res = analyseResults.getForbiddenCommandsQuality();
 
 	    if ( (res != null) && (res.booleanValue()) ) { 
 				quality = taskFeedback.getForbiddenCommandsFeedbackQuality();
 	    } else {
 				quality = "";
 	    }
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 				
 				
 		// Muisti
 
 		correct = analyseResults.getMemory();
 
 		//System.err.println("korrektin arvo, n. rivill 219 on: "+correct.booleanValue());
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"memoryValuesHeader", language);
 	    if (correct.booleanValue()) {
 			
 				feedback = taskFeedback.getMemoryFeedbackPositive();
 
 	    } else {
 
 				feedback = taskFeedback.getMemoryFeedbackNegative();
 				evaluation = 0;
 	    } 
 		
 	    Boolean res = analyseResults.getMemoryQuality();
 	    if ( (res != null) && (res.booleanValue()) ) { 
 				quality = taskFeedback.getMemoryFeedbackQuality();
 	    } else {
 				quality = "";
 	    }
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 
 		// Rekisterit
 
 		correct = analyseResults.getRegisters();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"registerValuesHeader", language);
 	    if (correct.booleanValue()) {
 			
 				feedback = taskFeedback.getRegisterFeedbackPositive();
 
 	    } else {
 
 				feedback = taskFeedback.getRegisterFeedbackNegative();
 				evaluation = 0;
 	    } 
 
 	    Boolean res = analyseResults.getRegistersQuality();
 	    if ( (res != null) && (res.booleanValue()) ) { 
 				quality = taskFeedback.getRegisterFeedbackQuality();
 	    } else {
 				quality = "";
 	    }
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 
 		// Tulosteet nytlle
 		
 		correct = analyseResults.getScreenOutput();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"screenOutputHeader", language);
 	    if (correct.booleanValue()) {
 			
 				feedback = taskFeedback.getScreenOutputFeedbackPositive();
 
 	    } else {
 
 				feedback = taskFeedback.getScreenOutputFeedbackNegative();
 				evaluation = 0;
 	    } 
 
 	    Boolean res = analyseResults.getScreenOutputQuality();
 	    if ( (res != null) && (res.booleanValue()) ) { 
 				quality = taskFeedback.getScreenOutputFeedbackQuality();
 	    } else {
 				quality = "";
 	    }
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 	
 		}//if
 
 		// Tulosteet tiedostoon
 		
 		correct = analyseResults.getFileOutput();
 		if (correct != null) {
 	    criteriaHeader = cache.getAttribute("D","staticttk91taskcomposer", 
 																					"fileOutputHeader", language);
 	    if (correct.booleanValue()) {
 			
 				feedback = taskFeedback.getFileOutputFeedbackPositive();
 
 	    } else {
 
 				feedback = taskFeedback.getFileOutputFeedbackNegative();
 				evaluation = 0;
 	    } 
 	    
 	    Boolean res = analyseResults.getFileOutputQuality();
 	    if ( (res != null) && (res.booleanValue()) ) { 
 				quality = taskFeedback.getFileOutputFeedbackQuality();
 	    } else {
 				quality = "";
 	    }
 		
 	    feedbackTable.append(getHTMLElementFeedbackRow(criteriaHeader, feedback, 
 																										 quality, 
 																										 correct.booleanValue()));
 		}//if
 
 		// taulukko loppuu
  
 		feedbackTable.append("</table><br>");
 
 		/************************************************
 		 * Rekisterien sislln lisminen
 		 ************************************************/
 
 		
 		// Haetaan rekisterien arvot int-taulukkoon ja tarkistetaan ettei ole null.
 
 		int[] registers = analyseResults.getRegisterValues();
 		if (registers != null) { 
 		
 			String registervaluesLabel = cache.getAttribute // rekisterien otsikko
 				("A", "ttk91feedbackcomposer", "registervaluesLabel", language);
 	
 		
 			feedbackTable.append("<table width=\"30%\" border=\"1\" cellspacing=\"0\"" 
 													 +"cellpadding=\"3\">"
 													 +"<tr align=\"center\">" 
 													 +"<td class=\"tableHeader\" align =\"left\">" 
 													 +registervaluesLabel +"</td>"
 													 +"</tr>");
 		
 	    for(int i = 0; i < registers.length; ++i) {
 				feedbackTable.append("<tr><td>R" +i +": "
 														 +registers[i] +"</td>"
 														 +"</tr>");
 	    }
 		}
 		feedbackTable.append("</table><br>");
 
 		/************************************************
 		 * Statistiikan lisminen
 		 ************************************************/
 		
 		// Kielikohtaisten otsikkojen hakeminen cachesta
 
 		String statisticsLabel = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "statisticsLabel", language);
 		String memoryReference = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "memoryReference", language);
 		String stackSize = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "stackSize", language);
 		String codeSegment = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "codeSegment", language);
 		String dataSegment = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "dataSegment", language);
 		String executedCommands = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "executedCommands", language);
 		
 
 		feedbackTable.append("<table width=\"30%\" border=\"1\" cellspacing=\"0\"" 
 												 +"cellpadding=\"3\">"
 												 +"<tr align=\"center\">" 
 												 +"<td class=\"tableHeader\" align =\"left\">" 
 												 +statisticsLabel +"</td>"
 												 +"</tr>");
 
 		feedbackTable.append("<tr><td>" +	memoryReference +": "
 												 +analyseResults.getMemoryReferenceCount()
 												 +"</td></tr>");
 		feedbackTable.append("<tr><td>" +	stackSize +": "
 												 +analyseResults.getStackSize()
 												 +"</td></tr>");
 		feedbackTable.append("<tr><td>" +	codeSegment +": "
 												 +analyseResults.getCodeSegmentSize()
 												 +"</td></tr>");
 		feedbackTable.append("<tr><td>" +	dataSegment +": "
 												 +analyseResults.getDataSegmentSize()
 												 +"</td></tr>");	
 		feedbackTable.append("<tr><td>" +	executedCommands +": "
 												 +analyseResults.getExecutedCommandsCount()
 												 +"</td></tr>");
 		feedbackTable.append("</table><br>");
 
 		/************************************************
 		 * Tulosteiden (CRT, file) lisys palautteeseen
 		 ************************************************/
 
 		String outputLabel = cache.getAttribute
 	    ("A", "ttk91feedbackcomposer", "outputLabel", language);
 
 		int[] crt = analyseResults.getCrt();
 		int[] file = analyseResults.getFile();
 
 		if ((crt != null) && (file != null)) { // Testataan onko tulosteita
 
 			feedbackTable.append("<table width=\"30%\" border=\"1\" cellspacing=\"0\"" 
 													 +"cellpadding=\"3\">"
 													 +"<tr align=\"center\">" 
 													 +"<td class=\"tableHeader\" align =\"left\">" 
 													 +outputLabel +"</td>"
 													 +"</tr>");
 
 			if (crt != null) { 
 				for(int i = 0; i < crt.length; ++i) {
 					feedbackTable.append("<tr><td>CRT " +(i+1) +":  " +crt[i] 
 															 +"</td></tr>");
 				}
 			}
 			if (file != null) {
 				for(int i = 0; i < file.length; ++i) {
 					feedbackTable.append("<tr><td>FILE " +(i+1) +": " +file[i] 
 															 +"</td></tr>");
 				}
 			}
 			feedbackTable.append("</table><br>");
 		}
 
 		/** 
 		 * Haetaan tehtv vastaava yleinen positiivinen 
 		 * ja negatiivinen palaute.
 		 */
 
 		String feedbackSummaryPos = 
 	    cache.getAttribute("T", taskID, "positivefeedback", language);	
 
 		String feedbackSummaryNeg = 
 	    cache.getAttribute("T", taskID, "negativefeedback", language);
 
 		if (feedbackSummaryPos == null) { // Jos null, niin laitetaan oletusviesti
 	    if (language.equals("FI")) 
 					feedbackSummaryPos = "Oikea vastaus";
 			if (language.equals("EN")) 
 				feedbackSummaryNeg = "Correct answer";
 		}
 
 		if (feedbackSummaryNeg == null) { // Jos null, niin laitetaan oletusviesti
 	    if (language.equals("FI"))
 					feedbackSummaryNeg = "Vr vastaus";
 			if (language.equals("EN"))
 				feedbackSummaryNeg = "Wrong answer";
 		}
 		
 		/**
 		 * Lopuksi luodaan uusi Feedback-olio. Parametreja ovat:
 		 * 0 (onnistumisen koodi), evaluation (oikeellisuusprosentti),
 		 * feedbackSummary/Pos/Neg (tehtvn positiivinen/negatiivinen palaute),
 		 * new String(feedbackTable) (palaute).
 		 */
 
 		return new Feedback(TTK91Constant.NO_ERROR, 
 												evaluation, 
 												feedbackSummaryPos, 
 												feedbackSummaryNeg, 
 												new String(feedbackTable));
 		
 	}//formFeedback
 
 	/**
 	 * Muodostaa virheilmoituksen.
 	 *
 	 * @param errorMessage virheilmoitus
 	 * @return sislt virheilmoituksen
 	 */
 
 
 	public static Feedback formFeedback(String errorMessage) {
 
 		// Virhekoodi 2 on fataalivirhe.
 
		return new Feedback(TTK91Constant.FATAL_ERROR, errorMessage);
 	}//formFeedback
 
 	/**
 	 * Luo parametrien perusteella yhden html-taulukon rivin.
 	 *
 	 * @param criteriaName kriteerin nimi
 	 * @param feedback     palaute
 	 * @param quality      mahdollinen laadullinen palaute
 	 * @param correct      oliko kriteeri oikein
 	 * @return html-taulukon rivi             
 	 */
 
 	private static String getHTMLElementFeedbackRow(String criteriaName,
 																									String feedback,
 																									String quality,
 																									boolean correct) {
 
 		String feedbackRow; // palautettava html-taulukon rivi
 		
 		feedbackRow = ("<tr><td width=\"20\"><strong>" +criteriaName 
 									 +"</strong></td>");
 
 		if (correct) { // jos kriteeri oikein
 	    feedbackRow += ("<td class=\"positivefeedback\" width=\"40\">" 
 											+feedback +"</td>"); 
 										
 		} else {       // jos kriteeri vrin
 	    feedbackRow += ("<td class=\"negativefeedback\" width=\"40\">" 
 											+feedback +"</td>");
 		}
 							 	  
 		// Listn laadullinen palaute.
 
 		feedbackRow +=("<td class=\"positivefeedback\" width=\"40\">" 
 									 +quality +"</td></tr>");
 
 		return feedbackRow;
 	
 	}//getHTMLElementFeedbackRow
 
 		
   /* 
 	public static void main(String [] args){
 		FeedbackTestCache cache = new FeedbackTestCache();
 		TTK91FeedbackComposer fbcomposer = new TTK91FeedbackComposer();
 		TTK91AnalyseResults analyseRe = new TTK91AnalyseResults();
 		TTK91TaskFeedback taskFb = new TTK91TaskFeedback();
 		analyseRe.setAcceptedSize(false);
 		analyseRe.setRequiredCommands(true);
 		analyseRe.setRequiredCommandsQuality(true);
 		int[] temp = {1,2,3,4,5,6,7};
 		analyseRe.setRegisterValues(temp);
 		analyseRe.setMemoryReferenceCount(20);
 		analyseRe.setStackSize(15);
 		analyseRe.setDataSegmentSize(50);
 		analyseRe.setCodeSegmentSize(5);
 		analyseRe.setStackSize(30);
 		analyseRe.setCrt(temp);
 		analyseRe.setFile(temp);
 		analyseRe.setExecutedCommandsCount(100);
 		taskFb.setAcceptedSizeFeedback("hyv","huono", "tosi hyv");
 		taskFb.setRequiredCommandsFeedback("hyv com","huono com", "tosi hyv com");
 		String [] crit = {"acceptedSize","requiredCommands"};
 		boolean [] boo = {true,true};
 		try{
       Feedback fb = fbcomposer.formFeedback(analyseRe, taskFb,
 																						cache, "106", "FI");
 			
       System.out.println(fb.getExtra());
 		}
 		catch(Exception e){System.err.println(e);}
 		
 	}
 	*/ 
 }//class
