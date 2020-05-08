 package de.unihd.dbs.uima.annotator.heideltime;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.uima.UIMAFramework;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.util.Level;
 import org.apache.uima.util.Logger;
 import org.cleartk.timeml.type.DocumentCreationTime;
 import org.cleartk.token.type.Sentence;
 import org.cleartk.token.type.Token;
 
 import de.unihd.dbs.uima.types.heideltime.Timex3;
 
 public class FullSpecifier {
 	Map<String, String> hmAllRePattern;
 	Map<String, String> normDayInWeek;
 	Map<String, String> normNumber;
 	Map<String, String> normMonthName;
 	Map<String, String> normMonthInSeason;
 	Map<String, String> normMonthInQuarter;
 	
 	Logger logger;
 	
 	public FullSpecifier(Map<String, String> hmAllRePattern,
 			Map<String, String> normDayInWeek, Map<String, String> normNumber,
 			Map<String, String> normMonthName,
 			Map<String, String> normMonthInSeason,
 			Map<String, String> normMonthInQuarter) {
 		super();
 		this.hmAllRePattern = hmAllRePattern;
 		this.normDayInWeek = normDayInWeek;
 		this.normNumber = normNumber;
 		this.normMonthName = normMonthName;
 		this.normMonthInSeason = normMonthInSeason;
 		this.normMonthInQuarter = normMonthInQuarter;
 		logger = UIMAFramework.getLogger(FullSpecifier.class);
 	}
 	
 	public FullSpecifier(Map<String, String> hmAllRePattern) {
 		this(hmAllRePattern, new HashMap<String, String>(),
 				new HashMap<String, String>(), new HashMap<String, String>(),
 				new HashMap<String, String>(), new HashMap<String, String>());
 
 		// MONTH IN QUARTER
 		normMonthInQuarter.put("01","1");
 		normMonthInQuarter.put("02","1");
 		normMonthInQuarter.put("03","1");
 		normMonthInQuarter.put("04","2");
 		normMonthInQuarter.put("05","2");
 		normMonthInQuarter.put("06","2");
 		normMonthInQuarter.put("07","3");
 		normMonthInQuarter.put("08","3");
 		normMonthInQuarter.put("09","3");
 		normMonthInQuarter.put("10","4");
 		normMonthInQuarter.put("11","4");
 		normMonthInQuarter.put("12","4");
 	
 		// MONTH IN SEASON
 		normMonthInSeason.put("", "");
 		normMonthInSeason.put("01","WI");
 		normMonthInSeason.put("02","WI");
 		normMonthInSeason.put("03","SP");
 		normMonthInSeason.put("04","SP");
 		normMonthInSeason.put("05","SP");
 		normMonthInSeason.put("06","SU");
 		normMonthInSeason.put("07","SU");
 		normMonthInSeason.put("08","SU");
 		normMonthInSeason.put("09","FA");
 		normMonthInSeason.put("10","FA");
 		normMonthInSeason.put("11","FA");
 		normMonthInSeason.put("12","WI");
 	
 		// DAY IN WEEK
 		normDayInWeek.put("sunday","1");
 		normDayInWeek.put("monday","2");
 		normDayInWeek.put("tuesday","3");
 		normDayInWeek.put("wednesday","4");
 		normDayInWeek.put("thursday","5");
 		normDayInWeek.put("friday","6");
 		normDayInWeek.put("saturday","7");
 		normDayInWeek.put("Sunday","1");
 		normDayInWeek.put("Monday","2");
 		normDayInWeek.put("Tuesday","3");
 		normDayInWeek.put("Wednesday","4");
 		normDayInWeek.put("Thursday","5");
 		normDayInWeek.put("Friday","6");
 		normDayInWeek.put("Saturday","7");
 //		normDayInWeek.put("sunday","7");
 //		normDayInWeek.put("monday","1");
 //		normDayInWeek.put("tuesday","2");
 //		normDayInWeek.put("wednesday","3");
 //		normDayInWeek.put("thursday","4");
 //		normDayInWeek.put("friday","5");
 //		normDayInWeek.put("saturday","6");
 //		normDayInWeek.put("Sunday","7");
 //		normDayInWeek.put("Monday","1");
 //		normDayInWeek.put("Tuesday","2");
 //		normDayInWeek.put("Wednesday","3");
 //		normDayInWeek.put("Thursday","4");
 //		normDayInWeek.put("Friday","5");
 //		normDayInWeek.put("Saturday","6");
 	
 	
 		// NORM MINUTE
 		normNumber.put("0","00");
 		normNumber.put("00","00");
 		normNumber.put("1","01");
 		normNumber.put("01","01");
 		normNumber.put("2","02");
 		normNumber.put("02","02");
 		normNumber.put("3","03");
 		normNumber.put("03","03");
 		normNumber.put("4","04");
 		normNumber.put("04","04");
 		normNumber.put("5","05");
 		normNumber.put("05","05");
 		normNumber.put("6","06");
 		normNumber.put("06","06");
 		normNumber.put("7","07");
 		normNumber.put("07","07");
 		normNumber.put("8","08");
 		normNumber.put("08","08");
 		normNumber.put("9","09");
 		normNumber.put("09","09");
 		normNumber.put("10","10");
 		normNumber.put("11","11");
 		normNumber.put("12","12");
 		normNumber.put("13","13");
 		normNumber.put("14","14");
 		normNumber.put("15","15");
 		normNumber.put("16","16");
 		normNumber.put("17","17");
 		normNumber.put("18","18");
 		normNumber.put("19","19");
 		normNumber.put("20","20");
 		normNumber.put("21","21");
 		normNumber.put("22","22");
 		normNumber.put("23","23");
 		normNumber.put("24","24");
 		normNumber.put("25","25");
 		normNumber.put("26","26");
 		normNumber.put("27","27");
 		normNumber.put("28","28");
 		normNumber.put("29","29");
 		normNumber.put("30","30");
 		normNumber.put("31","31");
 		normNumber.put("32","32");
 		normNumber.put("33","33");
 		normNumber.put("34","34");
 		normNumber.put("35","35");
 		normNumber.put("36","36");
 		normNumber.put("37","37");
 		normNumber.put("38","38");
 		normNumber.put("39","39");
 		normNumber.put("40","40");
 		normNumber.put("41","41");
 		normNumber.put("42","42");
 		normNumber.put("43","43");
 		normNumber.put("44","44");
 		normNumber.put("45","45");
 		normNumber.put("46","46");
 		normNumber.put("47","47");
 		normNumber.put("48","48");
 		normNumber.put("49","49");
 		normNumber.put("50","50");
 		normNumber.put("51","51");
 		normNumber.put("52","52");
 		normNumber.put("53","53");
 		normNumber.put("54","54");
 		normNumber.put("55","55");
 		normNumber.put("56","56");
 		normNumber.put("57","57");
 		normNumber.put("58","58");
 		normNumber.put("59","59");
 		normNumber.put("60","60");
 	
 		// NORM MONTH
 		normMonthName.put("january","01");
 		normMonthName.put("february","02");
 		normMonthName.put("march","03");
 		normMonthName.put("april","04");
 		normMonthName.put("may","05");
 		normMonthName.put("june","06");
 		normMonthName.put("july","07");
 		normMonthName.put("august","08");
 		normMonthName.put("september","09");
 		normMonthName.put("october","10");
 		normMonthName.put("november","11");
 		normMonthName.put("december","12");
 	}
 	
 	private int getOffsetForTense(String tense, int referenceValue, int timeValue) {
 		if ("PAST".equals(tense) && referenceValue >= timeValue) {
 			return 1;
 		}
 		if (("FUTURE".equals(tense) || "PRESENTFUTURE".equals(tense)) && referenceValue <= timeValue) {
 			return -1;
 		}
 		return 0;
 	}
 
 	public void process(JCas jcas, String typeToProcess) {
 		
 		// build up a list with all found TIMEX expressions
 		List<Timex3> linearDates = new ArrayList<Timex3>();
 		FSIterator iterTimex = jcas.getAnnotationIndex(Timex3.type).iterator();
 
 		// Create List of all Timexes of types "date" and "time"
 		while (iterTimex.hasNext()) {
 			Timex3 timex = (Timex3) iterTimex.next();
 			if ((timex.getTimexType().equals("DATE")) || (timex.getTimexType().equals("TIME"))) {
 				linearDates.add(timex);
 			}
 		}
 
 		////////////////////////////////////////
 		// IS THERE A DOCUMENT CREATION TIME? //
 		////////////////////////////////////////
 		boolean documentTypeNews = false;
 		boolean dctAvailable = false;
 		if (typeToProcess.equals("news")){
 			documentTypeNews = true;
 		}
 		// get the dct information
 		String dctValue   = "";
 		int dctCentury    = 0;
 		int dctYear       = 0;
 		int dctDecade     = 0;
 		int dctMonth      = 0;
 		int dctDay	= 0;
 		String dctSeason  = "";
 		String dctQuarter = "";
 		String dctHalf    = "";
 		int dctWeekday    = 0;
 		int dctWeek       = 0;
 	
 		//////////////////////////////////////////////
 		// INFORMATION ABOUT DOCUMENT CREATION TIME //
 		//////////////////////////////////////////////
 		FSIterator dctIter = jcas.getAnnotationIndex(DocumentCreationTime.type).iterator();
 		if (dctIter.hasNext()) {
 			dctAvailable = true;
 			DocumentCreationTime dct = (DocumentCreationTime) dctIter.next();
 			dctValue = dct.getValue();
 			// year, month, day as mentioned in the DCT
 			if (dctValue.matches("\\d\\d\\d\\d\\d\\d\\d\\d")){
 				dctCentury   = Integer.parseInt(dctValue.substring(0, 2));
 				dctYear      = Integer.parseInt(dctValue.substring(0, 4));
 				dctDecade    = Integer.parseInt(dctValue.substring(2, 3));
 				dctMonth     = Integer.parseInt(dctValue.substring(4, 6));
 				dctDay       = Integer.parseInt(dctValue.substring(6, 8));
 				logger.log(Level.FINE, "dctCentury:"+dctCentury);
 				logger.log(Level.FINE, "dctYear:"+dctYear);
 				logger.log(Level.FINE, "dctDecade:"+dctDecade);
 				logger.log(Level.FINE, "dctMonth:"+dctMonth);
 				logger.log(Level.FINE, "dctDay:"+dctDay);
 			}else{
 				dctCentury   = Integer.parseInt(dctValue.substring(0, 2));
 				dctYear      = Integer.parseInt(dctValue.substring(0, 4));
 				dctDecade    = Integer.parseInt(dctValue.substring(2, 3));
 				dctMonth     = Integer.parseInt(dctValue.substring(5, 7));
 				dctDay       = Integer.parseInt(dctValue.substring(8, 10));
 				logger.log(Level.FINE, "dctCentury:"+dctCentury);
 				logger.log(Level.FINE, "dctYear:"+dctYear);
 				logger.log(Level.FINE, "dctDecade:"+dctDecade);
 				logger.log(Level.FINE, "dctMonth:"+dctMonth);
 				logger.log(Level.FINE, "dctDay:"+dctDay);
 			}
 			dctQuarter = "Q"+normMonthInQuarter.get(normNumber.get(dctMonth+""));
 			dctHalf = "H1";
 			if (dctMonth > 6){
 				dctHalf = "H2";
 			}
 		
 			// season, week, weekday, have to be calculated
 			dctSeason    = normMonthInSeason.get(normNumber.get(dctMonth+"")+"");
 			dctWeekday   = getWeekdayOfDate(dctYear+"-"+normNumber.get(dctMonth+"")+"-"+ normNumber.get(dctDay+""));
 			dctWeek      = getWeekOfDate(dctYear+"-"+normNumber.get(dctMonth+"") +"-"+ normNumber.get(dctDay+""));
 			logger.log(Level.FINE, "dctQuarter:"+dctQuarter);
 			logger.log(Level.FINE, "dctSeason:"+dctSeason);
 			logger.log(Level.FINE, "dctWeekday:"+dctWeekday);
 			logger.log(Level.FINE, "dctWeek:"+dctWeek);
 		}
 		else{
 			logger.log(Level.FINE, "No DCT available...");
 		}
 	
 		//////////////////////////////////////////////
 		// go through list of Date and Time timexes //
 		//////////////////////////////////////////////
 		for (int i = 0; i < linearDates.size(); i++) {
 			Timex3 t_i = (Timex3) linearDates.get(i);
 			String value_i = t_i.getTimexValue();
 
 			// check if value_i has month, day, season, week (otherwise no UNDEF-year is possible)
 			Boolean viHasMonth   = false;
 			Boolean viHasDay     = false;
 			Boolean viHasSeason  = false;
 			Boolean viHasWeek    = false;
 			Boolean viHasQuarter = false;
 			Boolean viHasHalf    = false;
 			int viThisMonth      = 0;
 			int viThisDay        = 0;
 			String viThisSeason  = "";
 			String viThisQuarter = "";
 			String viThisHalf    = "";
 			String[] valueParts  = value_i.split("-");
 			// check if UNDEF-year or UNDEF-century
 			if ((value_i.startsWith("UNDEF-year")) || (value_i.startsWith("UNDEF-century"))){
 				if (valueParts.length > 2){
 					// get vi month
 					if (valueParts[2].matches("\\d\\d")) {
 						viHasMonth  = true;
 						viThisMonth = Integer.parseInt(valueParts[2]);
 					}
 					// get vi season
 					else if ((valueParts[2].equals("SP")) || (valueParts[2].equals("SU")) || (valueParts[2].equals("FA")) || (valueParts[2].equals("WI"))) {
 						viHasSeason  = true;
 						viThisSeason = valueParts[2];
 					}
 					// get v1 quarter
 					else if ((valueParts[2].equals("Q1")) || (valueParts[2].equals("Q2")) || (valueParts[2].equals("Q3")) || (valueParts[2].equals("Q4"))) {
 						viHasQuarter  = true;
 						viThisQuarter = valueParts[2];
 					}
 					else if ((valueParts[2].equals("H1")) || (valueParts[2].equals("H2"))){
 						viHasHalf  = true;
 						viThisHalf = valueParts[2];
 					}
 					// get vi day
 					if ((valueParts.length > 3) && (valueParts[3].matches("\\d\\d"))) {
 						viHasDay = true;
 						viThisDay = Integer.parseInt(valueParts[3]);
 					}
 				}
 			}
 			else{
 				if (valueParts.length > 1){
 					// get vi month
 					if (valueParts[1].matches("\\d\\d")) {
 						viHasMonth  = true;
 						viThisMonth = Integer.parseInt(valueParts[1]);
 					}
 					// get vi season
 					else if ((valueParts[1].equals("SP")) || (valueParts[1].equals("SU")) || (valueParts[1].equals("FA")) || (valueParts[1].equals("WI"))) {
 						viHasSeason  = true;
 						viThisSeason = valueParts[1];
 					}
 					// get vi day
 					if ((valueParts.length > 2) && (valueParts[2].matches("\\d\\d"))) {
 						viHasDay = true;
 						viThisDay = Integer.parseInt(valueParts[2]);
 					}
 				}
 			}
 			// get the last tense (depending on the part of speech tags used in front or behind the expression)
 			String last_used_tense = getLastTense(t_i, jcas);
 
 			//////////////////////////
 			// DISAMBIGUATION PHASE //
 			//////////////////////////
 		
 			////////////////////////////////////////////////////
 			// IF YEAR IS COMPLETELY UNSPECIFIED (UNDEF-year) //
 			////////////////////////////////////////////////////
 			String valueNew = value_i;
 			if (value_i.startsWith("UNDEF-year")){
 				String newYearValue = dctYear+"";
 				if (documentTypeNews && dctAvailable) {
 					if (viHasMonth && !viHasSeason) {
 						newYearValue = dctYear + getOffsetForTense(last_used_tense, dctMonth, viThisMonth) + "";
 					}
 					if (viHasQuarter){
 						newYearValue = dctYear + getOffsetForTense(last_used_tense,
 								Integer.parseInt(dctQuarter.substring(1)),
 								Integer.parseInt(viThisQuarter.substring(1))) + "";
 					}
 					if (viHasHalf){
 						newYearValue = dctYear + getOffsetForTense(last_used_tense,
 								Integer.parseInt(dctHalf.substring(1)),
 								Integer.parseInt(viThisHalf.substring(1))) + "";
 					}
 					if (!viHasMonth && !viHasDay && viHasSeason) {
 						// TODO check tenses?
 						newYearValue = dctYear+"";
 					}
 					// vi has week
 					if (viHasWeek){
 						newYearValue = dctYear+"";
 					}
 				}
 				else {
 					newYearValue = getLastMentionedX(linearDates, i, "year");
 				}
 
 				// REPLACE THE UNDEF-YEAR WITH THE NEWLY CALCULATED YEAR AND ADD TIMEX TO INDEXES
 				if (newYearValue.equals("")){
 					valueNew = value_i.replaceFirst("UNDEF-year", "XXXX");
 				}
 				else{
 					valueNew = value_i.replaceFirst("UNDEF-year", newYearValue);
 				}
 			}
 
 			///////////////////////////////////////////////////
 			// just century is unspecified (UNDEF-century86) //
 			///////////////////////////////////////////////////
 			else if ((value_i.startsWith("UNDEF-century"))){
 				String newCenturyValue = dctCentury+"";
 				int viThisDecade = Integer.parseInt(value_i.substring(13, 14));
 				// NEWS DOCUMENTS
 				if ((documentTypeNews) && (dctAvailable)){
 					logger.log(Level.FINE, "dctCentury"+dctCentury);
 					newCenturyValue = dctCentury+"";
 					logger.log(Level.FINE, "dctCentury"+dctCentury);
 					//  Tense is FUTURE
 					if ((last_used_tense.equals("FUTURE")) || (last_used_tense.equals("PRESENTFUTURE"))) {
 						if (viThisDecade < dctDecade){
 							newCenturyValue = dctCentury + 1+"";
 						}
 						else{
 							newCenturyValue = dctCentury+"";
 						}
 					}
 					// Tense is PAST
 					if ((last_used_tense.equals("PAST"))){
 						if (dctDecade <= viThisDecade){
 							newCenturyValue = dctCentury - 1+"";
 						}
 						else{
 							newCenturyValue = dctCentury+"";
 						}
 					}
 				}
 				// NARRATIVE DOCUMENTS
 				else{
 					newCenturyValue = getLastMentionedX(linearDates, i, "century");
 				}
 				if (newCenturyValue.equals("")){
 					// always assume that sixties, twenties, and so on are 19XX (changed 2011-09-08)
 					valueNew = value_i.replaceFirst("UNDEF-century", "19");
 				}
 				else{
 					valueNew = value_i.replaceFirst("UNDEF-century", newCenturyValue+"");
 				}
 				// always assume that sixties, twenties, and so on are 19XX (changed 2011-09-08)
 				if (valueNew.matches("\\d\\d\\dX")){
 					valueNew = "19" + valueNew.substring(2);
 				}
 			}
 		
 			////////////////////////////////////////////////////
 			// CHECK IMPLICIT EXPRESSIONS STARTING WITH UNDEF //
 			////////////////////////////////////////////////////
 			else if (value_i.startsWith("UNDEF")){
 				valueNew = value_i;
 			
 				//////////////////
 				// TO CALCULATE //
 				//////////////////
 				// year to calculate
 				if (value_i.matches("^UNDEF-(this|REFUNIT|REF)-(.*?)-(MINUS|PLUS)-([0-9]+).*")){
 					for (MatchResult mr : HeidelTime.findMatches(Pattern.compile("^(UNDEF-(this|REFUNIT|REF)-(.*?)-(MINUS|PLUS)-([0-9]+)).*"), value_i)){
 						String checkUndef = mr.group(1);
 						String ltn  = mr.group(2);
 						String unit = mr.group(3);
 						String op   = mr.group(4);
 						int diff    = Integer.parseInt(mr.group(5));
 					
 						// check for REFUNIT (only allowed for "year")
 						if ((ltn.equals("REFUNIT")) && (unit.equals("year"))){
 							String dateWithYear = getLastMentionedX(linearDates, i, "dateYear");
 							if (dateWithYear.equals("")){
 								valueNew = valueNew.replace(checkUndef, "XXXX");
 							}
 							else{
 								if (op.equals("MINUS")){
 									diff = diff * (-1);
 								}
 								int yearNew = Integer.parseInt(dateWithYear.substring(0,4)) + diff;
 								String rest = dateWithYear.substring(4);
 								valueNew = valueNew.replace(checkUndef, yearNew+rest);
 							}
 						}
 					
 					
 					
 						// REF and this are handled here
 						if (unit.equals("century")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								int century = dctCentury;
 								if (op.equals("MINUS")){
 									century = dctCentury - diff;
 								}
 								else if (op.equals("PLUS")){
 									century = dctCentury + diff;
 								}
 								valueNew = valueNew.replace(checkUndef, century+"XX");
 							}
 							else{
 								String lmCentury = getLastMentionedX(linearDates, i, "century");
 								if (lmCentury.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XX");
 								}
 								else{
 									if (op.equals("MINUS")){
 										lmCentury = Integer.parseInt(lmCentury) - diff + "XX";
 									}
 									else if (op.equals("PLUS")){
 										lmCentury = Integer.parseInt(lmCentury) + diff + "XX";
 									}
 									valueNew = valueNew.replace(checkUndef, lmCentury);
 								}
 							}
 						}
 						else if (unit.equals("decade")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								int decade = dctDecade;
 								if (op.equals("MINUS")){
 									decade = dctDecade - diff;
 								}
 								else if (op.equals("PLUS")){
 									decade = dctDecade + diff;
 								}
 								valueNew = valueNew.replace(checkUndef, decade+"X");
 							}
 							else{
 								String lmDecade = getLastMentionedX(linearDates, i, "decade");
 								if (lmDecade.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXX");
 								}
 								else{
 									if (op.equals("MINUS")){
 										lmDecade = Integer.parseInt(lmDecade) - diff + "X";
 									}
 									else if (op.equals("PLUS")){
 										lmDecade = Integer.parseInt(lmDecade) + diff + "X";
 									}
 									valueNew = valueNew.replace(checkUndef, lmDecade);
 								}
 							}
 						}
 						else if (unit.equals("year")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								int intValue = dctYear;
 								if (op.equals("MINUS")){
 									intValue = dctYear - diff;
 								}
 								else if (op.equals("PLUS")){
 									intValue = dctYear + diff;
 								}
 								valueNew = valueNew.replace(checkUndef, intValue + "");
 							}
 							else{
 								String lmYear = getLastMentionedX(linearDates, i, "year");
 								if (lmYear.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX");
 								}
 								else{
 									int intValue = Integer.parseInt(lmYear);
 									if (op.equals("MINUS")){
 										intValue = Integer.parseInt(lmYear) - diff;
 									}
 									else if (op.equals("PLUS")){
 										intValue = Integer.parseInt(lmYear) + diff;
 									}
 									valueNew = valueNew.replace(checkUndef, intValue+"");
 								}
 							}
 						}
 						else if (unit.equals("quarter")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								int intYear    = dctYear;
 								int intQuarter = Integer.parseInt(dctQuarter.substring(1));
 								int diffQuarters = diff % 4;
 								diff = diff - diffQuarters;
 								int diffYears    = diff / 4;
 								if (op.equals("MINUS")){
 									diffQuarters = diffQuarters * (-1);
 									diffYears    = diffYears    * (-1);
 								}
 								intYear    = intYear + diffYears;
 								intQuarter = intQuarter + diffQuarters;
 								valueNew = valueNew.replace(checkUndef, intYear+"-Q"+intQuarter);
 							}
 							else{
 								String lmQuarter = getLastMentionedX(linearDates, i, "quarter");
 								if (lmQuarter.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									int intYear    = Integer.parseInt(lmQuarter.substring(0, 4));
 									int intQuarter = Integer.parseInt(lmQuarter.substring(6));
 									int diffQuarters = diff % 4;
 									diff = diff - diffQuarters;
 									int diffYears    = diff / 4;
 									if (op.equals("MINUS")){
 										diffQuarters = diffQuarters * (-1);
 										diffYears    = diffYears    * (-1);
 									}
 									intYear    = intYear + diffYears;
 									intQuarter = intQuarter + diffQuarters;
 									valueNew = valueNew.replace(checkUndef, intYear+"-Q"+intQuarter);
 								}
 							}
 						}
 						else if (unit.equals("month")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								if (op.equals("MINUS")){
 									diff = diff * (-1);
 								}
 								valueNew = valueNew.replace(checkUndef, getXNextMonth(dctYear + "-" + normNumber.get(dctMonth+""), diff));
 							}
 							else{
 								String lmMonth = getLastMentionedX(linearDates, i, "month");
 								if (lmMonth.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									if (op.equals("MINUS")){
 										diff = diff * (-1);
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextMonth(lmMonth, diff));
 								}
 							}
 						}
 						else if (unit.equals("week")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								if (op.equals("MINUS")){
 									diff = diff * 7 * (-1);
 								}
 								else if (op.equals("PLUS")){
 									diff = diff * 7;
 								}
 								valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + normNumber.get(dctMonth+"") + "-"	+ dctDay, diff));
 							}
 							else{
 								String lmDay = getLastMentionedX(linearDates, i, "day");
 								if (lmDay.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 								}
 								else{
 									if (op.equals("MINUS")){
 										diff = diff * 7 * (-1);
 									}
 									else if (op.equals("PLUS")){
 										diff = diff * 7;
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay, diff));
 								}
 							}
 						}
 						else if (unit.equals("day")){
 							if ((documentTypeNews) && (dctAvailable) && (ltn.equals("this"))){
 								if (op.equals("MINUS")){
 									diff = diff * (-1);
 								}
 								valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + normNumber.get(dctMonth+"") + "-"	+ dctDay, diff));
 							}
 							else{
 								String lmDay = getLastMentionedX(linearDates, i, "day");
 								if (lmDay.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 								}
 								else{
 									if (op.equals("MINUS")){
 										diff = diff * (-1);
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay, diff));
 								}
 							}
 						}
 					}
 				}
 		
 				// century
 				else if (value_i.startsWith("UNDEF-last-century")){
 					String checkUndef = "UNDEF-last-century";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, normNumber.get(dctCentury - 1 +"") + "XX");
 					}
 					else{
 						String lmCentury = getLastMentionedX(linearDates,i,"century");
 						if (lmCentury.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, normNumber.get(Integer.parseInt(lmCentury) - 1 +"") + "XX");
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-century")){
 					String checkUndef = "UNDEF-this-century";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, normNumber.get(dctCentury+"") + "XX");
 					}
 					else{
 						String lmCentury = getLastMentionedX(linearDates,i,"century");
 						if (lmCentury.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, normNumber.get(Integer.parseInt(lmCentury)+"") + "XX");
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-next-century")){
 					String checkUndef = "UNDEF-next-century";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, normNumber.get(dctCentury + 1+"") + "XX");
 					}
 					else{
 						String lmCentury = getLastMentionedX(linearDates,i,"century");
 						if (lmCentury.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, normNumber.get(Integer.parseInt(lmCentury) + 1+"") + "XX");
 						}
 					}
 				}
 
 				// decade
 				else if (value_i.startsWith("UNDEF-last-decade")){
 					String checkUndef = "UNDEF-last-decade";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, (dctYear - 10+"").substring(0,3)+"X");
 					}
 					else{
 						String lmDecade = getLastMentionedX(linearDates,i,"decade");
 						if (lmDecade.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmDecade)-1+"X");
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-decade")){
 					String checkUndef = "UNDEF-this-decade";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, (dctYear+"").substring(0,3)+"X");
 					}
 					else{
 						String lmDecade = getLastMentionedX(linearDates,i,"decade");
 						if (lmDecade.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, lmDecade+"X");					
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-next-decade")) {
 					String checkUndef = "UNDEF-next-decade";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, (dctYear + 10+"").substring(0,3)+"X");
 					}
 					else{
 						String lmDecade = getLastMentionedX(linearDates,i,"decade");
 						if (lmDecade.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmDecade)+1+"X");
 						}
 					}
 				}
 			
 				// year
 				else if (value_i.startsWith("UNDEF-last-year")) {
 					String checkUndef = "UNDEF-last-year";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, dctYear -1 +"");
 					}
 					else{
 						String lmYear = getLastMentionedX(linearDates,i,"year");
 						if (lmYear.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmYear)-1+"");
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-year")){
 					String checkUndef = "UNDEF-this-year";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, dctYear +"");
 					}
 					else{
 						String lmYear = getLastMentionedX(linearDates,i,"year");
 						if (lmYear.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, lmYear);
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-next-year")) {
 					String checkUndef = "UNDEF-next-year";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, dctYear +1 +"");
 					}
 					else{
 						String lmYear = getLastMentionedX(linearDates,i,"year");
 						if (lmYear.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmYear)+1+"");					
 						}
 					}
 				}
 			
 				// month
 				else if (value_i.startsWith("UNDEF-last-month")) {
 					String checkUndef = "UNDEF-last-month";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, getXNextMonth(dctYear + "-" + normNumber.get(dctMonth+""), -1));
 					}
 					else{
 						String lmMonth = getLastMentionedX(linearDates,i,"month");
 						if (lmMonth.equals("")){
 							valueNew =  valueNew.replace(checkUndef, "XXXX-XX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, getXNextMonth(lmMonth, -1));
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-month")){
 					String checkUndef = "UNDEF-this-month";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, dctYear + "-" + normNumber.get(dctMonth+""));
 					}
 					else{
 						String lmMonth = getLastMentionedX(linearDates,i,"month");
 						if (lmMonth.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, lmMonth);
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-next-month")) {
 					String checkUndef = "UNDEF-next-month";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, getXNextMonth(dctYear + "-" + normNumber.get(dctMonth+""), 1));
 					}
 					else{
 						String lmMonth = getLastMentionedX(linearDates,i,"month");
 						if (lmMonth.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, getXNextMonth(lmMonth, 1));
 						}
 					}
 				}
 			
 				// day
 				else if (value_i.startsWith("UNDEF-last-day")) {
 					String checkUndef = "UNDEF-last-day";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + normNumber.get(dctMonth+"") + "-"+ dctDay, -1));
 					}
 					else{
 						String lmDay = getLastMentionedX(linearDates,i,"day");
 						if (lmDay.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay,-1));
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-day")){
 					String checkUndef = "UNDEF-this-day";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, dctYear + "-" + normNumber.get(dctMonth+"") + "-"+ normNumber.get(dctDay+""));
 					}
 					else{
 						String lmDay = getLastMentionedX(linearDates,i,"day");
 						if (lmDay.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, lmDay);
 						}
 						if (value_i.equals("UNDEF-this-day")){
 							valueNew = "PRESENT_REF";
 						}
 					}				
 				}
 				else if (value_i.startsWith("UNDEF-next-day")) {
 					String checkUndef = "UNDEF-next-day";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + normNumber.get(dctMonth+"") + "-"+ dctDay, 1));
 					}
 					else{
 						String lmDay = getLastMentionedX(linearDates,i,"day");
 						if (lmDay.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay,1));
 						}
 					}
 				}
 
 				// week
 				else if (value_i.startsWith("UNDEF-last-week")) {
 					String checkUndef = "UNDEF-last-week";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, getXNextWeek(dctYear+"-W"+normNumber.get(dctWeek+""),-1));
 					}
 					else{
 						String lmWeek = getLastMentionedX(linearDates,i,"week");
 						if (lmWeek.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-WXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, getXNextWeek(lmWeek,-1));
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-week")){
 					String checkUndef = "UNDEF-this-week";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef,dctYear+"-W"+normNumber.get(dctWeek+""));
 					}
 					else{
 						String lmWeek = getLastMentionedX(linearDates,i,"week");
 						if (lmWeek.equals("")){
 							valueNew = valueNew.replace(checkUndef,"XXXX-WXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef,lmWeek);
 						}
 					}				
 				}
 				else if (value_i.startsWith("UNDEF-next-week")) {
 					String checkUndef = "UNDEF-next-week";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, getXNextWeek(dctYear+"-W"+normNumber.get(dctWeek+""),1));
 					}
 					else{
 						String lmWeek = getLastMentionedX(linearDates,i,"week");
 						if (lmWeek.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-WXX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, getXNextWeek(lmWeek,1));
 						}
 					}
 				}
 			
 				// quarter
 				else if (value_i.startsWith("UNDEF-last-quarter")) {
 					String checkUndef = "UNDEF-last-quarter";
 					if ((documentTypeNews) && (dctAvailable)){
 						if (dctQuarter.equals("Q1")){
 							valueNew = valueNew.replace(checkUndef, dctYear-1+"-Q4");
 						}
 						else{
 							int newQuarter = Integer.parseInt(dctQuarter.substring(1,2))-1;
 							valueNew = valueNew.replace(checkUndef, dctYear+"-Q"+newQuarter);
 						}
 					}
 					else{
 						String lmQuarter  = getLastMentionedX(linearDates, i, "quarter");
 						if (lmQuarter.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-QX");
 						}
 						else{
 							int lmQuarterOnly = Integer.parseInt(lmQuarter.substring(6,7));
 							int lmYearOnly    = Integer.parseInt(lmQuarter.substring(0,4));
 							if (lmQuarterOnly == 1){
 								valueNew = valueNew.replace(checkUndef, lmYearOnly-1+"-Q4");
 							}
 							else{
 								int newQuarter = lmQuarterOnly-1;
 								valueNew = valueNew.replace(checkUndef, dctYear+"-Q"+newQuarter);
 							}
 						}
 					}
 				}
 				else if (value_i.startsWith("UNDEF-this-quarter")){
 					String checkUndef = "UNDEF-this-quarter";
 					if ((documentTypeNews) && (dctAvailable)){
 						valueNew = valueNew.replace(checkUndef, dctYear+"-"+dctQuarter);
 					}
 					else{
 						String lmQuarter = getLastMentionedX(linearDates, i, "quarter");
 						if (lmQuarter.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-QX");
 						}
 						else{
 							valueNew = valueNew.replace(checkUndef, lmQuarter);
 						}
 					}				
 				}
 				else if (value_i.startsWith("UNDEF-next-quarter")) {
 					String checkUndef = "UNDEF-next-quarter";
 					if ((documentTypeNews) && (dctAvailable)){
 						if (dctQuarter.equals("Q4")){
 							valueNew = valueNew.replace(checkUndef, dctYear+1+"-Q1");
 						}
 						else{
 							int newQuarter = Integer.parseInt(dctQuarter.substring(1,2))+1;
 							valueNew = valueNew.replace(checkUndef, dctYear+"-Q"+newQuarter);
 						}					
 					}
 					else{
 						String lmQuarter  = getLastMentionedX(linearDates, i, "quarter");
 						if (lmQuarter.equals("")){
 							valueNew = valueNew.replace(checkUndef, "XXXX-QX");
 						}
 						else{
 							int lmQuarterOnly = Integer.parseInt(lmQuarter.substring(6,7));
 							int lmYearOnly    = Integer.parseInt(lmQuarter.substring(0,4));
 							if (lmQuarterOnly == 4){
 								valueNew = valueNew.replace(checkUndef, lmYearOnly+1+"-Q1");
 							}
 							else{
 								int newQuarter = lmQuarterOnly+1;
 								valueNew = valueNew.replace(checkUndef, dctYear+"-Q"+newQuarter);
 							}
 						}
 					}
 				}
 			
 				// MONTH NAMES
 				else if (value_i.matches("UNDEF-(last|this|next)-(january|february|march|april|may|june|july|august|september|october|november|december).*")){
 					for (MatchResult mr : HeidelTime.findMatches(Pattern.compile("(UNDEF-(last|this|next)-(january|february|march|april|may|june|july|august|september|october|november|december)).*"),value_i)){
 						String checkUndef = mr.group(1);
 						String ltn      = mr.group(2);
 						String newMonth = normMonthName.get((mr.group(3)));
 						int newMonthInt = Integer.parseInt(newMonth);
 						if (ltn.equals("last")){
 							if ((documentTypeNews) && (dctAvailable)){
 								if (dctMonth <= newMonthInt){
 									valueNew = valueNew.replace(checkUndef, dctYear-1+"-"+newMonth);
 								}
 								else{
 									valueNew = valueNew.replace(checkUndef, dctYear+"-"+newMonth);
 								}
 							}
 							else{
 								String lmMonth = getLastMentionedX(linearDates, i, "month");
 								if (lmMonth.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									int lmMonthInt = Integer.parseInt(lmMonth.substring(5,7));
 									if (lmMonthInt <= newMonthInt){
 										valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmMonth.substring(0,4))-1+"-"+newMonth);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, lmMonth.substring(0,4)+"-"+newMonth);
 									}
 								}
 							}
 						}
 						else if (ltn.equals("this")){
 							if ((documentTypeNews) && (dctAvailable)){
 								valueNew = valueNew.replace(checkUndef, dctYear+"-"+newMonth);
 							}
 							else{
 								String lmMonth = getLastMentionedX(linearDates, i, "month");
 								if (lmMonth.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									valueNew = valueNew.replace(checkUndef, lmMonth.substring(0,4)+"-"+newMonth);
 								}
 							}
 						}
 						else if (ltn.equals("next")){
 							if ((documentTypeNews) && (dctAvailable)){
 								if (dctMonth >= newMonthInt){
 									valueNew = valueNew.replace(checkUndef, dctYear+1+"-"+newMonth);
 								}
 								else{
 									valueNew = valueNew.replace(checkUndef, dctYear+"-"+newMonth);
 								}
 							}
 							else{
 								String lmMonth = getLastMentionedX(linearDates, i, "month");
 								if (lmMonth.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									int lmMonthInt = Integer.parseInt(lmMonth.substring(5,7));
 									if (lmMonthInt >= newMonthInt){
 										valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmMonth.substring(0,4))+1+"-"+newMonth);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, lmMonth.substring(0,4)+"-"+newMonth);
 									}
 								}
 							}						
 						}
 					}
 				}
 			
 				// SEASONS NAMES
 				else if (value_i.matches("^UNDEF-(last|this|next)-(SP|SU|FA|WI).*")){
 					for (MatchResult mr : HeidelTime.findMatches(Pattern.compile("(UNDEF-(last|this|next)-(SP|SU|FA|WI)).*"),value_i)){
 						String checkUndef = mr.group(1);
 						String ltn       = mr.group(2);
 						String newSeason = mr.group(3);
 						if (ltn.equals("last")){
 							if ((documentTypeNews) && (dctAvailable)){
 								if (dctSeason.equals("SP")){
 									valueNew = valueNew.replace(checkUndef, dctYear-1+"-"+newSeason);
 								}
 								else if (dctSeason.equals("SU")){
 									if (newSeason.equals("SP")){
 										valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, dctYear-1+"-"+newSeason);
 									}
 								}
 								else if (dctSeason.equals("FA")){
 									if ((newSeason.equals("SP")) || (newSeason.equals("SU"))){
 										valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, dctYear-1+"-"+newSeason);
 									}
 								}
 								else if (dctSeason.equals("WI")){
 									if (newSeason.equals("WI")){
 										valueNew = valueNew.replace(checkUndef, dctYear-1+"-"+newSeason);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 									}
 								}
 							}
 							else{ // NARRATVIE DOCUMENT
 								String lmSeason = getLastMentionedX(linearDates, i, "season");
 								if (lmSeason.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									if (lmSeason.substring(5,7).equals("SP")){
 										valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))-1+"-"+newSeason);
 									}
 									else if (lmSeason.substring(5,7).equals("SU")){
 										if (lmSeason.substring(5,7).equals("SP")){
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+"-"+newSeason);
 										}
 										else{
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))-1+"-"+newSeason);
 										}
 									}
 									else if (lmSeason.substring(5,7).equals("FA")){
 										if ((newSeason.equals("SP")) || (newSeason.equals("SU"))){
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+"-"+newSeason);
 										}
 										else{
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))-1+"-"+newSeason);
 										}
 									}
 									else if (lmSeason.substring(5,7).equals("WI")){
 										if (newSeason.equals("WI")){
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))-1+"-"+newSeason);
 										}
 										else{
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+"-"+newSeason);
 										}
 									}
 								}
 							}
 						}
 						else if (ltn.equals("this")){
 							if ((documentTypeNews) && (dctAvailable)){
 								// TODO include tense of sentence?
 								valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 							}
 							else{
 								// TODO include tense of sentence?
 								String lmSeason = getLastMentionedX(linearDates, i, "season");
 								if (lmSeason.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									valueNew = valueNew.replace(checkUndef, lmSeason.substring(0,4)+"-"+newSeason);
 								}
 							}						
 						}
 						else if (ltn.equals("next")){
 							if ((documentTypeNews) && (dctAvailable)){
 								if (dctSeason.equals("SP")){
 									if (newSeason.equals("SP")){
 										valueNew = valueNew.replace(checkUndef, dctYear+1+"-"+newSeason);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 									}
 								}
 								else if (dctSeason.equals("SU")){
 									if ((newSeason.equals("SP")) || (newSeason.equals("SU"))){
 										valueNew = valueNew.replace(checkUndef, dctYear+1+"-"+newSeason);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 									}
 								}
 								else if (dctSeason.equals("FA")){
 									if (newSeason.equals("WI")){
 										valueNew = valueNew.replace(checkUndef, dctYear+"-"+newSeason);
 									}
 									else{
 										valueNew = valueNew.replace(checkUndef, dctYear+1+"-"+newSeason);
 									}
 								}
 								else if (dctSeason.equals("WI")){
 									valueNew = valueNew.replace(checkUndef, dctYear+1+"-"+newSeason);
 								}
 							}
 							else{ // NARRATIVE DOCUMENT
 								String lmSeason = getLastMentionedX(linearDates, i, "season");
 								if (lmSeason.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX");
 								}
 								else{
 									if (lmSeason.substring(5,7).equals("SP")){
 										if (newSeason.equals("SP")){
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+1+"-"+newSeason);
 										}
 										else{
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+"-"+newSeason);
 										}
 									}
 									else if (lmSeason.substring(5,7).equals("SU")){
 										if ((newSeason.equals("SP")) || (newSeason.equals("SU"))){
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+1+"-"+newSeason);
 										}
 										else{
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+"-"+newSeason);
 										}
 									}
 									else if (lmSeason.substring(5,7).equals("FA")){
 										if (newSeason.equals("WI")){
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+"-"+newSeason);
 										}
 										else{
 											valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+1+"-"+newSeason);
 										}
 									}
 									else if (lmSeason.substring(5,7).equals("WI")){
 										valueNew = valueNew.replace(checkUndef, Integer.parseInt(lmSeason.substring(0,4))+1+"-"+newSeason);
 									}
 								}
 							}
 						}
 					}
 				}
 			
 				// WEEKDAY NAMES
 				// TODO the calculation is strange, but works
 				// TODO tense should be included?!
 				else if (value_i.matches("^UNDEF-(last|this|next|day)-(monday|tuesday|wednesday|thursday|friday|saturday|sunday).*")){
 					for (MatchResult mr : HeidelTime.findMatches(Pattern.compile("(UNDEF-(last|this|next|day)-(monday|tuesday|wednesday|thursday|friday|saturday|sunday)).*"),value_i)){
 						String checkUndef = mr.group(1);
 						String ltnd       = mr.group(2);
 						String newWeekday = mr.group(3);
 						int newWeekdayInt = Integer.parseInt(normDayInWeek.get(newWeekday));
 						if (ltnd.equals("last")){
 							if ((documentTypeNews) && (dctAvailable)){
 								int diff = (-1) * (dctWeekday - newWeekdayInt);
 								if (diff >= 0) {
 									diff = diff - 7;
 								}
 								valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + dctMonth + "-" + dctDay, diff));
 							}
 							else{
 								String lmDay     = getLastMentionedX(linearDates, i, "day");
 								if (lmDay.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 								}
 								else{
 									int lmWeekdayInt = getWeekdayOfDate(lmDay);
 									int diff = (-1) * (lmWeekdayInt - newWeekdayInt);
 									if (diff >= 0) {
 										diff = diff - 7;
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay, diff));
 								}
 							}
 						}
 						else if (ltnd.equals("this")){
 							if ((documentTypeNews) && (dctAvailable)){
 								// TODO tense should be included?!	
 								int diff = (-1) * (dctWeekday - newWeekdayInt);
 								if (diff >= 0) {
 									diff = diff - 7;
 								}
 								if (diff == -7) {
 									diff = 0;
 								}
 
 							
 								valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + dctMonth + "-"+ dctDay, diff));
 							}
 							else{
 								// TODO tense should be included?!
 								String lmDay     = getLastMentionedX(linearDates, i, "day");
 								if (lmDay.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 								}
 								else{
 									int lmWeekdayInt = getWeekdayOfDate(lmDay);
 									int diff = (-1) * (lmWeekdayInt - newWeekdayInt);
 									if (diff >= 0) {
 										diff = diff - 7;
 									}
 									if (diff == -7) {
 										diff = 0;
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay, diff));
 								}
 							}						
 						}
 						else if (ltnd.equals("next")){
 							if ((documentTypeNews) && (dctAvailable)){
 								int diff = newWeekdayInt - dctWeekday;
 								if (diff <= 0) {
 									diff = diff + 7;
 								}
 								valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + dctMonth + "-"+ dctDay, diff));
 							}
 							else{
 								String lmDay     = getLastMentionedX(linearDates, i, "day");
 								if (lmDay.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 								}
 								else{
 									int lmWeekdayInt = getWeekdayOfDate(lmDay);
 									int diff = newWeekdayInt - lmWeekdayInt;
 									if (diff <= 0) {
 										diff = diff + 7;
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay, diff));
 								}
 							}
 						}
 						else if (ltnd.equals("day")){
 							if ((documentTypeNews) && (dctAvailable)){
 								// TODO tense should be included?!
 								int diff = (-1) * (dctWeekday - newWeekdayInt);
 								if (diff >= 0) {
 									diff = diff - 7;
 								}
 								if (diff == -7) {
 									diff = 0;
 								}
 								//  Tense is FUTURE
 								if ((last_used_tense.equals("FUTURE")) || (last_used_tense.equals("PRESENTFUTURE"))) {
 									diff = diff + 7;
 								}
 								// Tense is PAST
 								if ((last_used_tense.equals("PAST"))){
 							
 								}
 								valueNew = valueNew.replace(checkUndef, getXNextDay(dctYear + "-" + dctMonth + "-"+ dctDay, diff));
 							}
 							else{
 								// TODO tense should be included?!
 								String lmDay     = getLastMentionedX(linearDates, i, "day");
 								if (lmDay.equals("")){
 									valueNew = valueNew.replace(checkUndef, "XXXX-XX-XX");
 								}
 								else{
 									int lmWeekdayInt = getWeekdayOfDate(lmDay);
 									int diff = (-1) * (lmWeekdayInt - newWeekdayInt);
 									if (diff >= 0) {
 										diff = diff - 7;
 									}
 									if (diff == -7) {
 										diff = 0;
 									}
 									valueNew = valueNew.replace(checkUndef, getXNextDay(lmDay, diff));
 								}
 							}
 						}
 					}
 				
 				}
 				else {
 					logger.log(Level.WARNING, "ATTENTION: UNDEF value for: " + valueNew+" is not handled in disambiguation phase!");
 				}
 			}
 			t_i.removeFromIndexes();
 			logger.log(Level.FINE, t_i.getTimexId()+" DISAMBIGUATION PHASE: foundBy:"+t_i.getFoundByRule()+" text:"+t_i.getCoveredText()+" value:"+t_i.getTimexValue()+" NEW value:"+valueNew);
 			t_i.setTimexValue(valueNew);
 			t_i.addToIndexes();
 			linearDates.set(i, t_i);
 		}
 	}
 	
 	/**
 	 * Get the last tense used in the sentence
 	 *
 	 * @param timex
 	 * @return
 	 */
 	public String getLastTense(Timex3 timex, JCas jcas) {
 	
 		String lastTense = "";
 
 		// Get the sentence
 		FSIterator iterSentence = jcas.getAnnotationIndex(Sentence.type).iterator();
 		Sentence s = new Sentence(jcas);
 		while (iterSentence.hasNext()) {
 			s = (Sentence) iterSentence.next();
 			if ((s.getBegin() < timex.getBegin())
 					&& (s.getEnd() > timex.getEnd())) {
 				break;
 			}
 		}
 
 		// Get the tokens
 		TreeMap<Integer, Token> tmToken = new TreeMap<Integer, Token>();
 		FSIterator iterToken = jcas.getAnnotationIndex(Token.type).subiterator(s);
 		while (iterToken.hasNext()) {
 			Token token = (Token) iterToken.next();
 			tmToken.put(token.getEnd(), token);
 		}
 
 		// Get the last VERB token
 		for (Integer tokEnd : tmToken.keySet()) {
 			if (tokEnd < timex.getBegin()) {
 				Token token = tmToken.get(tokEnd);
 				logNearTense("GET LAST TENSE", token);
 				if (token.getPos() == null){
 				
 				}
 				else if ((hmAllRePattern.containsKey("tensePos4PresentFuture")) && (token.getPos().matches(hmAllRePattern.get("tensePos4PresentFuture")))){
 					lastTense = "PRESENTFUTURE";
 				}
 				else if ((hmAllRePattern.containsKey("tensePos4Past")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Past")))){
 					lastTense = "PAST";
 				}
 				else if ((hmAllRePattern.containsKey("tensePos4Future")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Future")))){
 					if (token.getCoveredText().matches(hmAllRePattern.get("tenseWord4Future"))){
 						lastTense = "FUTURE";
 					}
 				}
 				if (token.getCoveredText().equals("since")){
 					lastTense = "PAST";
 				}
 			}
 			if (lastTense.equals("")) {
 				if (tokEnd > timex.getEnd()) {
 					Token token = tmToken.get(tokEnd);
 					logNearTense("GET NEXT TENSE", token);
 					if (token.getPos() == null){
 					
 					}
 					else if ((hmAllRePattern.containsKey("tensePos4PresentFuture")) && (token.getPos().matches(hmAllRePattern.get("tensePos4PresentFuture")))){
 						lastTense = "PRESENTFUTURE";
 					}
 					else if ((hmAllRePattern.containsKey("tensePos4Past")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Past")))){
 						lastTense = "PAST";
 					}
 					else if ((hmAllRePattern.containsKey("tensePos4Future")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Future")))){
 						if (token.getCoveredText().matches(hmAllRePattern.get("tenseWord4Future"))){
 							lastTense = "FUTURE";
 						}
 					}
 				}
 			}
 		}
 		// check for double POS Constraints (not included in the rule language, yet) TODO
 		// VHZ VNN and VHZ VNN and VHP VNN and VBP VVN
 		String prevPos = "";
 		String longTense = "";
 		if (lastTense.equals("PRESENTFUTURE")){
 			for (Integer tokEnd : tmToken.keySet()) {
 				if (tokEnd < timex.getBegin()) {
 					Token token = tmToken.get(tokEnd);
 					if ((prevPos.equals("VHZ")) || (prevPos.equals("VBZ")) || (prevPos.equals("VHP")) || (prevPos.equals("VBP"))){
 						if (token.getPos().equals("VVN")){
 							if ((!(token.getCoveredText().equals("expected"))) && (!(token.getCoveredText().equals("scheduled")))){
 								lastTense = "PAST";
 								longTense = "PAST";
 							}
 						}
 					}
 					prevPos = token.getPos();
 				}
 				if (longTense.equals("")) {
 					if (tokEnd > timex.getEnd()) {
 						Token token = tmToken.get(tokEnd);
 						if ((prevPos.equals("VHZ")) || (prevPos.equals("VBZ")) || (prevPos.equals("VHP")) || (prevPos.equals("VBP"))){
 							if (token.getPos().equals("VVN")){
 								if ((!(token.getCoveredText().equals("expected"))) && (!(token.getCoveredText().equals("scheduled")))){
 									lastTense = "PAST";
 									longTense = "PAST";
 								}
 							}
 						}
 						prevPos = token.getPos();
 					}
 				}
 			}
 		}
 		logger.log(Level.FINE, "TENSE: "+lastTense);
 		return lastTense;
 	}
 
 	/**
 	 * Get the last tense used in the sentence
 	 *
 	 * @param timex
 	 * @return
 	 */
 	public String getClosestTense(Timex3 timex, JCas jcas) {
 	
 		String lastTense = "";
 		String nextTense = "";
 	
 		int tokenCounter = 0;
 		int lastid = 0;
 		int nextid = 0;
 		int tid    = 0;
 
 		// Get the sentence
 		FSIterator iterSentence = jcas.getAnnotationIndex(Sentence.type).iterator();
 		Sentence s = new Sentence(jcas);
 		while (iterSentence.hasNext()) {
 			s = (Sentence) iterSentence.next();
 			if ((s.getBegin() < timex.getBegin())
 					&& (s.getEnd() > timex.getEnd())) {
 				break;
 			}
 		}
 
 		// Get the tokens
 		TreeMap<Integer, Token> tmToken = new TreeMap<Integer, Token>();
 		FSIterator iterToken = jcas.getAnnotationIndex(Token.type).subiterator(s);
 		while (iterToken.hasNext()) {
 			Token token = (Token) iterToken.next();
 			tmToken.put(token.getEnd(), token);
 		}
 	
 		// Get the last VERB token
 		for (Integer tokEnd : tmToken.keySet()) {
 			tokenCounter++;
 			if (tokEnd < timex.getBegin()) {
 				Token token = tmToken.get(tokEnd);
 				logNearTense("GET LAST TENSE", token);
 				if (token.getPos() == null){
 				
 				}
 				else if ((hmAllRePattern.containsKey("tensePos4PresentFuture")) && (token.getPos().matches(hmAllRePattern.get("tensePos4PresentFuture")))){
 					lastTense = "PRESENTFUTURE";
 					lastid = tokenCounter;
 				}
 				else if ((hmAllRePattern.containsKey("tensePos4Past")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Past")))){
 					lastTense = "PAST";
 					lastid = tokenCounter;
 				}
 				else if ((hmAllRePattern.containsKey("tensePos4Future")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Future")))){
 					if (token.getCoveredText().matches(hmAllRePattern.get("tenseWord4Future"))){
 						lastTense = "FUTURE";
 						lastid = tokenCounter;
 					}
 				}
 			}
 			else{
 				if (tid == 0){
 					tid = tokenCounter;
 				}
 			}
 		}
 		tokenCounter = 0;
 		for (Integer tokEnd : tmToken.keySet()) {
 			tokenCounter++;
 			if (nextTense.equals("")) {
 				if (tokEnd > timex.getEnd()) {
 					Token token = tmToken.get(tokEnd);
 					logNearTense("GET NEXT TENSE", token);
 					if (token.getPos() == null){
 					
 					}
 					else if ((hmAllRePattern.containsKey("tensePos4PresentFuture")) && (token.getPos().matches(hmAllRePattern.get("tensePos4PresentFuture")))){
 						nextTense = "PRESENTFUTURE";
 						nextid = tokenCounter;
 					}
 					else if ((hmAllRePattern.containsKey("tensePos4Past")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Past")))){
 						nextTense = "PAST";
 						nextid = tokenCounter;
 					}
 					else if ((hmAllRePattern.containsKey("tensePos4Future")) && (token.getPos().matches(hmAllRePattern.get("tensePos4Future")))){
 						if (token.getCoveredText().matches(hmAllRePattern.get("tenseWord4Future"))){
 							nextTense = "FUTURE";
 							nextid = tokenCounter;
 						}
 					}
 				}
 			}
 		}
 		if (lastTense.equals("")){
 			logger.log(Level.FINE, "TENSE: "+nextTense);
 			return nextTense;
 		}
 		else if (nextTense.equals("")){
 			logger.log(Level.FINE, "TENSE: "+lastTense);
 			return lastTense;
 		}
 		else{
 			// If there is tense before and after the timex token,
 			// return the closer one:
 			if ((tid - lastid) > (nextid - tid)){
 				logger.log(Level.FINE, "TENSE: "+nextTense);
 				return nextTense;
 			}
 			else{
 				logger.log(Level.FINE, "TENSE: "+lastTense);
 				return lastTense;
 			}
 		}
 	}
 
 	private void logNearTense(String title, Token token) {
 		logger.log(Level.FINE, title + ": string:"+token.getCoveredText()+" pos:"+token.getPos() + "\n" +
 				"hmAllRePattern.containsKey(tensePos4PresentFuture):"+hmAllRePattern.get("tensePos4PresentFuture") + "\n" +
 				"hmAllRePattern.containsKey(tensePos4Future):"+hmAllRePattern.get("tensePos4Future") + "\n" +
 				"hmAllRePattern.containsKey(tensePos4Past):"+hmAllRePattern.get("tensePos4Past") + "\n" +
 				"CHECK TOKEN:"+token.getPos());
 	}
 	
 	static final Map<String, Pattern> xPatternMap = new HashMap<String, Pattern>();
 	
 	static {
 		xPatternMap.put("century", Pattern.compile("^(\\d{2})...*"));
 		xPatternMap.put("decade", Pattern.compile("^(\\d{3})..*"));
 		xPatternMap.put("year", Pattern.compile("^(\\d{4}).*"));
 		xPatternMap.put("dateYear", Pattern.compile("^(\\d{4}.*)"));
 		xPatternMap.put("month", Pattern.compile("^(\\d{4}-\\d{2}).*"));
 		xPatternMap.put("day", Pattern.compile("^(\\d{4}-\\d{2}-\\d{2}).*"));
 		xPatternMap.put("week", Pattern.compile("^(\\d{4}-(?:\\d{2}-\\d{2}|W\\d{2})).*"));
 		xPatternMap.put("quarter", Pattern.compile("^(\\d{4}-(?:\\d{2}|Q[1-4])).*"));
 		xPatternMap.put("dateQuarter", Pattern.compile("^(\\d{4}-Q[1-4]).*"));
 		xPatternMap.put("season", Pattern.compile("^(\\d{4}-(?:\\d{2}|SP|SU|FA|WI)).*"));
 	}
 
 	/**
 	 * The value of the x of the last mentioned Timex is calculated.
 	 * @param linearDates
 	 * @param i
 	 * @param x
 	 * @return
 	 */
 	public String getLastMentionedX(List<Timex3> linearDates, int i, String x){
 	
 		String xValue = getLastMentionedX(linearDates.get(i),
 				linearDates.listIterator(i), xPatternMap.get(x));
 		
 		// Change full date to W/Q/S representation
 		if ("week".equals(x) && !xValue.contains("W")) {
 			xValue = xValue.substring(0, 4) + "-W" + getWeekOfDate(xValue);
 		}
 		else if ("quarter".equals(x) && !xValue.contains("Q")) {
 			xValue = xValue.substring(0, 4) + "-Q" + normMonthInQuarter.get(xValue.substring(5, 7));
 		}
 		else if ("season".equals(x) && !xValue.contains("S")) {
 			xValue = xValue.substring(0, 4) + "-S" + normMonthInSeason.get(xValue.substring(5, 7));
 		}
 		return xValue;
 	}
 
 	private String getLastMentionedX(Timex3 t_i, ListIterator<Timex3> iter, Pattern xPattern) {
 		while (iter.hasPrevious()) {
 			Timex3 timex = iter.previous();
 			if (timex.getBegin() == t_i.getBegin()) {
 				continue;
 			}
 			Matcher m = xPattern.matcher(timex.getTimexValue());
 			if (m.find()) {
 				return m.group(1);
 			}
 		}
 		return "";
 	}
 	
 	private String getXNext(String date, String fmt, int field, int offset) {
 		SimpleDateFormat formatter = new SimpleDateFormat(fmt);
 		Calendar c = Calendar.getInstance();
 		try {
 			c.setTime(formatter.parse(date));
 			c.add(field, offset);
 			c.getTime();
 			return formatter.format(c.getTime());
 		} catch (ParseException e) {
 			e.printStackTrace();
 			return "";
 		}
 	}
 
 	/**
 	 * get the x-next day of date.
 	 *
 	 * @param date
 	 * @param x
 	 * @return
 	 */
 	public String getXNextDay(String date, Integer x) {
 		return getXNext(date, "yyyy-MM-dd", Calendar.DAY_OF_MONTH, x);
 	}
 
 	/**
 	 * get the x-next month of date
 	 *
 	 * @param date
 	 * @param x
 	 * @return
 	 */
 	public String getXNextMonth(String date, Integer x) {
 		return getXNext(date, "yyyy-MM", Calendar.MONTH, x);
 	}
 
 	public String getXNextWeek(String date, Integer x){
		String res = getXNext(date.replace("-W", "-"), "yyyy-w", Calendar.WEEK_OF_YEAR, x);
		return res.substring(0,4) + "-W" + normNumber.get(res.substring(5));
 	}
 	
 	/**
 	 * Get the weekday of date
 	 *
 	 * @param date
 	 */
 	public int getWeekdayOfDate(String date) {
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		int weekday = 0;
 		Calendar c = Calendar.getInstance();
 		try {
 			c.setTime(formatter.parse(date));
 			weekday = c.get(Calendar.DAY_OF_WEEK);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return weekday;
 	}
 
 	/**
 	 * Get the week of date
 	 *
 	 * @param date
 	 * @return
 	 */
 	public int getWeekOfDate(String date) {
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
 		int week = 0;
 		;
 		Calendar c = Calendar.getInstance();
 		try {
 			c.setTime(formatter.parse(date));
 			week = c.get(Calendar.WEEK_OF_YEAR);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 		return week;
 	}
 }
