 // %1329240092:de.hattrickorganizer.gui.transferscout%
 package de.hattrickorganizer.gui.transferscout;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import de.hattrickorganizer.model.HOVerwaltung;
 
 
 /**
  * Parses a player out of a text copied from HT. Tries also to give error informations but this may
  * be wrong!
  *
  * @author Marco Senn
  */
 public class PlayerConverter {
     //~ Instance fields ----------------------------------------------------------------------------
 
     /** List of all 21 ratings for the active language */
     private List skills;
     private List skillvalues;
     private List specialities;
     private List specialitiesvalues;
     private int error;
     final HOVerwaltung homodel = HOVerwaltung.instance();
 
     //~ Constructors -------------------------------------------------------------------------------
 
     /**
      * We prepare our skill and specialities and sort them
      */
     public PlayerConverter() {
         // Get all skills for active language
         // This should be the same language as in Hattrick
         skills = new ArrayList();
         skills.add(homodel.getResource().getProperty("nonexisting").toLowerCase());
         skills.add(homodel.getResource().getProperty("katastrophal").toLowerCase());
         skills.add(homodel.getResource().getProperty("erbaermlich").toLowerCase());
         skills.add(homodel.getResource().getProperty("armselig").toLowerCase());
         skills.add(homodel.getResource().getProperty("schwach").toLowerCase());
         skills.add(homodel.getResource().getProperty("durchschnittlich").toLowerCase());
         skills.add(homodel.getResource().getProperty("passabel").toLowerCase());
         skills.add(homodel.getResource().getProperty("gut").toLowerCase());
         skills.add(homodel.getResource().getProperty("sehr_gut").toLowerCase());
         skills.add(homodel.getResource().getProperty("hervorragend").toLowerCase());
         skills.add(homodel.getResource().getProperty("grossartig").toLowerCase());
         skills.add(homodel.getResource().getProperty("brilliant").toLowerCase());
         skills.add(homodel.getResource().getProperty("fantastisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("Weltklasse").toLowerCase());
         skills.add(homodel.getResource().getProperty("uebernatuerlich").toLowerCase());
         skills.add(homodel.getResource().getProperty("gigantisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("ausserirdisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("mythisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("maerchenhaft").toLowerCase());
         skills.add(homodel.getResource().getProperty("galaktisch").toLowerCase());
         skills.add(homodel.getResource().getProperty("goettlich").toLowerCase());
 
         skillvalues = new ArrayList();
 
         for (int k = 0; k < skills.size(); k++) {
             skillvalues.add(new Integer(k));
         }
 
         // Sort skills by length (shortest first)
         int p = skills.size() - 1;
 
         while (p > 0) {
             int k = p;
 
             while ((k < skills.size())
                    && (skills.get(k - 1).toString().length() > skills.get(k).toString().length())) {
                 final String t = skills.get(k - 1).toString();
                 skills.set(k - 1, skills.get(k).toString());
                 skills.set(k, t);
 
                 final Integer i = (Integer) skillvalues.get(k - 1);
                 skillvalues.set(k - 1, (Integer) skillvalues.get(k));
                 skillvalues.set(k, i);
                 k++;
             }
 
             p--;
         }
 
         // Get all specialities for active language
         // This should be the same language as in Hattrick
         specialities = new ArrayList();
         specialities.add(homodel.getResource().getProperty("sp_Technical").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Quick").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Powerful").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Unpredictable").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Head").toLowerCase());
         specialities.add(homodel.getResource().getProperty("sp_Regainer").toLowerCase());
 
         specialitiesvalues = new ArrayList();
 
         for (int k = 0; k < 6; k++) {
             specialitiesvalues.add(new Integer(k));
         }
 
         // Sort specialities by length (shortest first)
         p = specialities.size() - 1;
 
         while (p > 0) {
             int k = p;
 
             while ((k < specialities.size())
                    && (specialities.get(k - 1).toString().length() > specialities.get(k).toString()
                                                                                  .length())) {
                 final String t = specialities.get(k - 1).toString();
                 specialities.set(k - 1, specialities.get(k).toString());
                 specialities.set(k, t);
 
                 final Integer i = (Integer) specialitiesvalues.get(k - 1);
                 specialitiesvalues.set(k - 1, (Integer) specialitiesvalues.get(k));
                 specialitiesvalues.set(k, i);
                 k++;
             }
 
             p--;
         }
     }
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Returns possible error. If error is nonzero, there was a problem.
      *
      * @return Returns possible error
      */
     public final int getError() {
         return error;
     }
 
     /**
      * Parses the copied text and returns a Player Object
      *
      * @param text the copied text from HT site
      *
      * @return Player a Player object
      *
      * @throws Exception Throws exception on some parse errors
      */
     public final Player build(String text) throws Exception {
         error = 0;
 
         final Player player = new Player();
 
         // Init some helper variables
         String mytext = text;
         final List lines = new ArrayList();
         int p = -1;
         String tmp = "";
 
         // Detect linefeed
         //  \n will do for linux and windows
         //  \r is for mac
         String feed = "";
 
         if (text.indexOf("\n") >= 0) {
             feed = "\n";
         } else {
             if (text.indexOf("\r") >= 0) {
                 feed = "\r";
             }
         }
 
         // If we detected some possible player
         if (!feed.equals("")) {
             //
             // We start reformating given input here and extracting
             // only needed lines for player detection
             //
             // Delete empty lines from input
             String txt = text;
 
             boolean startFound = false;
             while ((p = txt.indexOf(feed)) >= 0) {
                 tmp = txt.substring(0, p).trim();
 
                 if (tmp.indexOf("") > 0) {
                 	startFound = true;
                 }
 
                 if (!tmp.equals("") && startFound) {
                     lines.add(tmp);
                 }
 
                 txt = txt.substring(p + 1);
             }
 
             //-- get name and store club name
             tmp = lines.get(0).toString();
             player.setPlayerName(tmp.substring(tmp.indexOf("")+1).trim());
             String teamname = tmp.substring(0, tmp.indexOf("")).trim();
 
             //-- get playerid
             int found_at_line = -1;
             for (int m = 0; m<10; m++) {
             	tmp = lines.get(m).toString();

            	try {
					if (tmp.indexOf("(") > -1 && tmp.indexOf(")") > -1 && Integer.parseInt(tmp.substring(tmp.indexOf("(")+1, tmp.indexOf(")")).trim()) > 100000) {
						player.setPlayerID(Integer.parseInt(tmp.substring(tmp.indexOf("(")+1, tmp.indexOf(")")).trim()));
						found_at_line = m;
						break;
					}
				} catch (Exception e) {
					continue;
				}
             }
 
             //-- get age
             tmp = lines.get(found_at_line + 1).toString();
 
             String age = "";
             p = 0;
             int n = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) < '0') || (tmp.charAt(p) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     age = age + tmp.charAt(p);
                 } else {
                     break;
                 }
 
                 p++;
             }
 
             if (!age.equals("")) {
                 player.setAge(Integer.valueOf(age).intValue());
             } else {
                 error = 2;
             }
 
             //-- get ageDays
             int ageIndex = tmp.indexOf(age) + age.length();
             tmp = tmp.substring(ageIndex);
 
             String ageDays = "";
             p = 0;
             n = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) < '0') || (tmp.charAt(p) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 p++;
             }
 
             p = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     ageDays = ageDays + tmp.charAt(p);
                 } else {
                     break;
                 }
 
                 p++;
             }
 
             if (!ageDays.equals("")) {
                 player.setAgeDays(Integer.valueOf(ageDays).intValue());
             } else {
                 error = 2;
             }
 
             // clean lines till here
             if (found_at_line > 0) {
             	for (int m=0; m<=(found_at_line+1); m++) {
             		lines.remove(0);
             	}
             }
             // remove club line and all lines until the time info (e.g. "since 06.04.2008")
             boolean teamfound = false;
             boolean datefound = false;
             for (int m = 0; m<12; m++) {
             	tmp = lines.get(m).toString();
             	if (tmp.indexOf(teamname)>-1) {
             		teamfound = true;
             	}
             	if (teamfound && !datefound) {
             		lines.remove(m);
             		m--;
             	}
             	if (teamfound && tmp.indexOf("(")>-1 && tmp.indexOf(")")>-1) {
             		datefound = true;
             		break;
             	}
             }
 
             // Extract TSI-line
             p = 2;
 
             while (p < lines.size()) {
                 //Search for TSI-line (ending in numbers)
                 tmp = lines.get(p).toString();
 
                 if ((tmp.charAt(tmp.length() - 1) >= '0') && (tmp.charAt(tmp.length() - 1) <= '9') ) {
                 	if (tmp.length()>9 && tmp.substring(tmp.length()-9, tmp.length()).indexOf(".")>-1) {
                 		p++;
                 		continue;
                 	}
                 	found_at_line = p;
                     break;
                 }
 
                 p++;
             }
             //-- get tsi
             String tsi = "";
             p = 0;
 
             while (p < tmp.length()) {
                 if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                     tsi = tsi + tmp.charAt(p);
                 }
 
                 p++;
             }
 
             if (!tsi.equals("")) {
                 player.setTSI(Integer.valueOf(tsi).intValue());
             } else {
                 error = 2;
             }
 
             //-- check bookings
             tmp = lines.get(found_at_line+2).toString();
             try {
             	if (tmp.indexOf(":") > -1 && tmp.indexOf("0") == -1) {
             		player.setBooked(tmp);
             	}
             } catch (Exception e) { /* ignore */ }
 
 
             //-- Get injury
             tmp = lines.get(found_at_line+3).toString();
             try {
             	String injury = "";
             	for (int j = 0; j < tmp.length(); j++) {
             		if ((tmp.charAt(j) >= '0') && (tmp.charAt(j) <= '9') && (tmp.charAt(j-1) != '[')) {
             			injury = String.valueOf(tmp.charAt(j));
             			break;
             		}
             	}
 
             	if (!injury.equals("")) {
             		player.setInjury(Integer.valueOf(injury).intValue());
             	}
             } catch (Exception e) { /* ignore */ }
 
             // Search for actual year (expires) and also next year
             // (end of year problem)
             final Date d = new Date();
             SimpleDateFormat f = new SimpleDateFormat("yyyy");
             final String year = f.format(d);
             final String year2 = String.valueOf((Integer.parseInt(year)+1));
 
             p = 0;
             for (int m = 6; m < 8; m++) {
             	// Delete all rows not containing our year
             	tmp = lines.get(m).toString();
 
             	if (p > 10) { // already 10 lines deleted - there must be something wrong, break
             		break;
             	}
 
             	if ((tmp.indexOf(year) > -1) || (tmp.indexOf(year2) > -1)) {
             		found_at_line = m;
             		break;
             	} else {
             		lines.remove(m);
             		m--;
             		p++;
             	}
             }
             String exp = getDeadlineString(tmp);
 
             // Extract minimal bid
             tmp = lines.get(found_at_line+1).toString();
             n = 0;
             int k = 0;
             String bid = "";
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) < '0') || (tmp.charAt(k) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 k++;
             }
             k = 0;
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) >= '0') && (tmp.charAt(k) <= '9')) {
                     bid += tmp.charAt(k);
                 }
 
                 k++;
             }
 
             // Extract current bid if any
             tmp = lines.get(found_at_line + 2).toString();
             n = 0;
             k = 0;
             String curbid = "";
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) < '0') || (tmp.charAt(k) > '9')) {
                     n++;
                 } else {
                     tmp = tmp.substring(n);
                     break;
                 }
 
                 k++;
             }
             k = 0;
             while (k < tmp.length()) {
                 if ((tmp.charAt(k) >= '0') && (tmp.charAt(k) <= '9')) {
                     curbid += tmp.charAt(k);
                 } else if ((tmp.charAt(k) != ' ') && curbid.length()>0) { // avoid to add numbers from bidding team names
                 	break;
                 }
 
                 k++;
             }
 
             player.setPrice(getPrice(bid, curbid));
 
             //--------------------------------------------
 
             // exp is of format: ddmmyyyyhhmm
             try {
 				player.setExpiryDate(exp.substring(0, 2) + "." + exp.substring(2, 4) + "."
 				                     + exp.substring(6, 8));
 				player.setExpiryTime(exp.substring(8, 10) + ":" + exp.substring(10, 12));
 			} catch (RuntimeException e) {
 				// error getting deadline - just set current date
 				f = new SimpleDateFormat("dd.MM.yyyy");
 				player.setExpiryDate(f.format(new Date()));
 				f = new SimpleDateFormat("HH:mm");
 				player.setExpiryTime(f.format(new Date()));
 				if (error == 0) {
                     error = 1;
                 }
 			}
 
             // truncate text from player name to date (year)
             final String name = player.getPlayerName();
             if ((p = mytext.indexOf(name)) >= 0) {
                 mytext = mytext.substring(p + name.length());
             }
             if ((p = mytext.indexOf(name)) >= 0) {
                 mytext = mytext.substring(p);
             }
 
             char[] cs = new char[teamname.length()];
 
             for (int cl = 0; cl < cs.length; cl++) {
                 cs[cl] = '*';
             }
 
             mytext = mytext.replaceAll(teamname, new String(cs)).toLowerCase();
 
             cs = new char[name.length()];
 
             for (int cl = 0; cl < cs.length; cl++) {
                 cs[cl] = '*';
             }
 
             mytext = mytext.replaceAll(name.toLowerCase(), new String(cs)).toLowerCase();
 
             // We can search all the skills in text now
             p = skills.size() - 1;
 
             final List foundskills = new ArrayList();
 
             while (p >= 0) {
                 final String singleskill = skills.get(p).toString();
                 k = mytext.indexOf(singleskill);
 
                 if (k >= 0) {
                     final List pair = new ArrayList();
                     pair.add(new Integer(k));
                     pair.add(singleskill);
                     pair.add(new Integer(p));
                     foundskills.add(pair);
 
                     final char[] ct = new char[singleskill.length()];
 
                     for (int cl = 0; cl < ct.length; cl++) {
                         ct[cl] = '*';
                     }
 
                     mytext = mytext.replaceFirst(singleskill, new String(ct));
                 } else {
                     p--;
                 }
             }
 
             if ((foundskills.size() != 11) && (error == 0)) {
                 error = 1;
             }
 
             // Sort skills by location
             p = foundskills.size() - 1;
 
             while (p > 0) {
                 k = p;
 
                 while (k < foundskills.size()) {
                     final List ts1 = (ArrayList) foundskills.get(k - 1);
                     final List ts2 = (ArrayList) foundskills.get(k);
 
                     if (((Integer) ts1.get(0)).intValue() > ((Integer) ts2.get(0)).intValue()) {
                         foundskills.set(k - 1, ts2);
                         foundskills.set(k, ts1);
                         k++;
                     } else {
                         break;
                     }
                 }
 
                 p--;
             }
 
             // player skills (long default format)
             player.setForm(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(0)).get(2)).intValue())).intValue());
             player.setStamina(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(1)).get(2)).intValue())).intValue());
             player.setExperience(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(2)).get(2)).intValue())).intValue());
             player.setLeadership(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(3)).get(2)).intValue())).intValue());
 
             player.setGoalKeeping(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(4)).get(2)).intValue())).intValue());
             player.setDefense(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(5)).get(2)).intValue())).intValue());
             player.setPlayMaking(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(6)).get(2)).intValue())).intValue());
             player.setWing(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(7)).get(2)).intValue())).intValue());
             player.setPassing(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(8)).get(2)).intValue())).intValue());
             player.setAttack(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(9)).get(2)).intValue())).intValue());
             player.setSetPieces(((Integer) skillvalues.get(((Integer) ((ArrayList) foundskills.get(10)).get(2)).intValue())).intValue());
 
             // We can search the speciality in text now
             p = specialities.size() - 1;
 
             final List foundspecialities = new ArrayList();
 
             while (p >= 0) {
                 final String singlespeciality = specialities.get(p).toString();
                 k = mytext.indexOf(singlespeciality);
 
                 if (k >= 0) {
                     final List pair = new ArrayList();
                     pair.add(new Integer(k));
                     pair.add(singlespeciality);
                     pair.add(new Integer(p));
                     foundspecialities.add(pair);
 
                     final char[] ct = new char[singlespeciality.length()];
 
                     for (int cl = 0; cl < ct.length; cl++) {
                         ct[cl] = '*';
                     }
 
                     mytext = mytext.replaceFirst(singlespeciality, new String(ct));
                 } else {
                     p--;
                 }
             }
 
             if ((foundspecialities.size() > 1) && (error == 0)) {
                 error = 1;
             }
 
             // Sort specialities by location
             p = foundspecialities.size() - 1;
 
             while (p > 0) {
                 k = p;
 
                 while (k < foundspecialities.size()) {
                     final List ts1 = (ArrayList) foundspecialities.get(k - 1);
                     final List ts2 = (ArrayList) foundspecialities.get(k);
 
                     if (((Integer) ts1.get(0)).intValue() > ((Integer) ts2.get(0)).intValue()) {
                         foundspecialities.set(k - 1, ts2);
                         foundspecialities.set(k, ts1);
                         k++;
                     } else {
                         break;
                     }
                 }
 
                 p--;
             }
 
             if (foundspecialities.size() > 0) {
                 player.setSpeciality(((Integer) specialitiesvalues.get(((Integer) ((ArrayList) foundspecialities.get(0)).get(2)).intValue())).intValue() + 1);
             } else {
                 player.setSpeciality(0);
             }
         }
 
         return player;
     }
 
     public static int getPrice(String bid, String curbid) {
     	int price = 0;
     	try {
     		price = Integer.parseInt(bid);
     		if (curbid.length()>0 && Integer.parseInt(curbid) >= Integer.parseInt(bid)) {
     			price = Integer.parseInt(curbid);
     		}
     	} catch (Exception e) { /* nothing */ }
     	return price;
     }
 
     public static String getDeadlineString(String tmp) {
         // get deadline
         String exp = "";
         int p = 0;
         int k = 0;
 
         while (p < tmp.length()) {
             if ((tmp.charAt(p) < '0') || (tmp.charAt(p) > '9')) {
                 k++;
             } else {
                 tmp = tmp.substring(k);
                 break;
             }
 
             p++;
         }
 
         p = 0;
         k = 0;
 
         String part1 = "";
 
         while (p < tmp.length()) {
             if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                 k++;
             } else {
                 part1 = tmp.substring(0, k);
 
                 if (part1.length() < 2) {
                     part1 = "0" + part1;
                 }
 
                 tmp = tmp.substring(k + 1);
                 break;
             }
 
             p++;
         }
 
         p = 0;
         k = 0;
 
         String part2 = "";
 
         while (p < tmp.length()) {
             if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                 k++;
             } else {
                 part2 = tmp.substring(0, k);
 
                 if (part2.length() < 2) {
                     part2 = "0" + part2;
                 }
 
                 tmp = tmp.substring(k + 1);
                 break;
             }
 
             p++;
         }
 
         p = 0;
         k = 0;
 
         String part3 = "";
 
         while (p < tmp.length()) {
             if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                 k++;
             } else {
                 part3 = tmp.substring(0, k);
 
                 if (part3.length() < 2) {
                     part3 = "0" + part3;
                 }
 
                 tmp = tmp.substring(k + 1);
                 break;
             }
 
             p++;
         }
 
         p = 0;
 
         String part4 = "";
 
         while (p < tmp.length()) {
             if ((tmp.charAt(p) >= '0') && (tmp.charAt(p) <= '9')) {
                 part4 = part4 + tmp.charAt(p);
             }
 
             p++;
         }
 
         final Calendar c = Calendar.getInstance();
         SimpleDateFormat f = new SimpleDateFormat("ddMMyyyy");
 
         final Date d1 = c.getTime();
         final String date1 = f.format(d1);
         c.add(Calendar.DATE, 1);
 
         final Date d2 = c.getTime();
         final String date2 = f.format(d2);
         c.add(Calendar.DATE, 1);
 
         final Date d3 = c.getTime();
         final String date3 = f.format(d3);
 
         String date = part1 + part2 + part3;
 
         if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
             exp = date + part4;
         } else {
             date = part1 + part3 + part2;
 
             if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                 exp = date + part4;
             } else {
                 date = part2 + part1 + part3;
 
                 if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                     exp = date + part4;
                 } else {
                     date = part2 + part3 + part1;
 
                     if ((date1.equals(date)) || (date2.equals(date)) || (date3.equals(date))) {
                         exp = date + part4;
                     } else {
                         date = part3 + part1 + part2;
 
                         if ((date1.equals(date))
                             || (date2.equals(date))
                             || (date3.equals(date))) {
                             exp = date + part4;
                         } else {
                             date = part3 + part2 + part1;
 
                             if ((date1.equals(date))
                                 || (date2.equals(date))
                                 || (date3.equals(date))) {
                                 exp = date + part4;
                             } else {
                                 exp = part1 + part2 + part3 + part4;
                             }
                         }
                     }
                 }
             }
         }
         return exp;
     }
 }
