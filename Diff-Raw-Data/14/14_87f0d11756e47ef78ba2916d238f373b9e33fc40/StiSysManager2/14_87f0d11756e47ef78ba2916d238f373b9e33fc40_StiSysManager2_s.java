 package com.example.haw_app.stisysManager;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.jsoup.Connection;
 import org.jsoup.Connection.Method;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.example.haw_app.stisysManager.persistenz.SQLiteConnector;
 
 import android.content.Context;
 import android.util.Log;
 
 public class StiSysManager2 implements IStiSysManager {
 
 	private Session session;
 	private Map<String, String> profile;
 	private int printQuote;
 	private Map<String, String[]> courses;
 	private Map<String, Boolean> solvedTrainings;
 	private Map<String, Integer> solvedTests;
 	private Map<String, DateTime[]> nextApps;
 	private Map<String, String> mailLists;
 	private Document profileDoc;
 	private Document coursesDoc;
 	private Document printDoc;
 	private Document solvedDoc;
 	private Document subscribeDoc;
 	private Document unsubscribeDoc;
 	private Document mailListDocSEM;
 	private Document mailListDocPRA;
 	private Document mailListDocWP;
 	private Document mailListDocPRO;
 	private List<String> subscribleOnes;
 	private List<String> unsubscribleOnes;
 	private Context parent;
 	private SQLiteConnector db;
 	public StiSysManager2(String userName, String password, Context mParent) {
 		
		session = new Session(userName, password);
 		parent = mParent;
 		db = new SQLiteConnector(parent);
 		//syncData();
 	}
 	
 	
 	@Override
 	public void syncData() {
 		if(session.userName.equals("") || session.password.equals(""))
 		{
 			return;
 		}
         Thread stisysThread = new Thread() {
             public void run()
             {
                 nextApplications();
             }
         };
         stisysThread.start();
         try
         {
             stisysThread.join();
         } catch (InterruptedException e)
         {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 		getProfile();
 		getPrintQuote();
 		getCourses();
 		getSolvedOnes();	
 		getUnSubscrible();
 	}
 	
 	private void getUnSubscrible(){
 		subscribleOnes = new ArrayList<String>();
 		unsubscribleOnes = new ArrayList<String>();
 		Elements table1 = subscribeDoc().select("tr[class=tablecontentbackdark]").select("td");
 		Elements table2 = subscribeDoc().select("tr[class=tablecontentbacklight]").select("td");
 		
         for (int i = 1; i < table1.size(); i += 8)
         {
             subscribleOnes.add(table1.get(i).text());
         }
         for (int i = 1; i < table2.size(); i += 8)
         {
             subscribleOnes.add(table2.get(i).text());
         }
 
 		Elements table3 = unsubscribeDoc().select("tr[class=tablecontentbackdark]").select("td");
 		Elements table4 = unsubscribeDoc().select("tr[class=tablecontentbacklight]").select("td");
 
         for (int i = 1; i < table3.size(); i += 8)
         {
             unsubscribleOnes.add(table3.get(i).text());
         }
         for (int i = 1; i < table4.size(); i += 8)
         {
             unsubscribleOnes.add(table4.get(i).text());
         }	
 	}
 	
 
 	@Override
 	public List<String> getUnsubscribeableCourses() {
 		return unsubscribleOnes;
 	}
 
 	@Override
 	public List<String> getSubscribeableCourses() {
 		return subscribleOnes;
 	}
 	
 	
     @Override
     public void subscribeCourse(String name)
     {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void unsubscribeCourse(String name)
     {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 	// noch nciht fertig
 	@Override
 	public double getAverageGrade() {
         double sum = 0;
         for (Map.Entry<String, Integer> entry : solvedTests.entrySet())
         {
             if (entry.getKey().equals("Programmieren 1 (PR1)"))
             {
                 sum += (double) entry.getValue() * 1.5;
             } else if (entry.getKey().equals("Praktikum Programmieren 1 (PRP1)"))
             {
                 sum += (double) entry.getValue() * 0.5;
             } else if (entry.getKey().equals("Programmieren 2 (PR2)"))
             {
                 sum += (double) entry.getValue() * 0.6;
             } else if (entry.getKey().equals("Praktikum Programmieren 2 (PRP2)"))
             {
                 sum += (double) entry.getValue() * 0.3;
             } else
             {
                 sum += entry.getValue();
             }
         }
         db.setAverageGrade(sum / (double) solvedTests.size());
         return sum / (double) solvedTests.size();
 	}
 	
     @Override
     public int getSolvedCP()
     {
                 int sum = 0;
         for (String elem : solvedTests.keySet())
         {
             if (elem.contains("PR1"))
             {
                 sum += 9;
             } else if (elem.contains("PRP1"))
             {
                 sum += 3;
             } else if (elem.contains("PR2"))
             {
                 sum += 4;
             } else if (elem.contains("PRP2"))
             {
                 sum += 2;
             } else
             {
                 sum += 6;
             }
         }
         for (String elem : solvedTrainings.keySet()){
                 if(elem.contains("Projekt")){
                     sum += 9;
                 }
                 else if(!(elem.contains("Praktikum")|| elem.contains("bung") || elem.contains("Studienfachberatung"))){
                     sum += 3;
                 }    
         }
         
         return sum;
     }
 
 	@Override
 	public double getDifferenceToBetterGrade() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public Map<String, DateTime[]> getNextApplicationDates() {
 		return db.getNextApplicationDates();
 	}
 
 	
 	public String getStudentNameIntern() {
 		db.setName(profile.get("Name"));
 		return profile.get("Name");
 
 	}
 
 	
 	public String getUserNameIntern() {
 		db.setUserName(profile.get("HAW-Kennung"));
 		return profile.get("HAW-Kennung");
 	}
 
 	public DateTime getPasswordExpirationDateIntern() {
 		db.setPasswordExpirationDate(StisysUtil.getDateNoTime(profile
 				.get("Ablaufdatum des Passwortes")));
 		return StisysUtil.getDateNoTime(profile
 				.get("Ablaufdatum des Passwortes"));
 	}
 
 	
 	public int getMatriculationNumberIntern() {
 		db.setMatriculationNumber(Integer.parseInt(profile.get("Martrikelnummer")));
 		return Integer.parseInt(profile.get("Martrikelnummer"));
 	}
 
 	
 	public DateTime getBirthdayIntern() {
 		db.setBirthday(StisysUtil.getDateNoTime(profile.get("Geburtstag").split(", ")[1]));
 		return StisysUtil
 				.getDateNoTime(profile.get("Geburtstag").split(", ")[1]);
 	}
 
 	@Override
 	public int getPrintCredit() {
 		return db.getPrintCredit();
 	}
 
 	
 	public Map<String, String[]> getRegisteredSocialSciencesCoursesIntern() {
 		Map<String, String[]> ssc = new HashMap<String, String[]>();
 		for (Map.Entry<String, String[]> entry : courses.entrySet()) {
 			if (entry.getValue()[1].length() == 0
 					&& entry.getValue()[2].length() == 0) {
 				ssc.put(entry.getKey(), entry.getValue());
 			}
 		}
 		db.clearSocialSienceCourses();
 		db.saveSocialSienceCourses(ssc);
 		return ssc;
 	}
 
 	public Map<String, String[]> getRegisteredTestsIntern() {
 		Map<String, String[]> tr = new HashMap<String, String[]>();
 		String f = " ";
 
 		for (Map.Entry<String, String[]> entry : courses.entrySet()) {
 			if (entry.getValue()[2].length() == 0
 					&& entry.getValue()[1].contains("AI")) {
 				tr.put(entry.getKey(), entry.getValue());
 			}
 		}
 		db.clearRegisteredTests();
 		db.saveRegisteredTests(tr);
 		return tr;
 	}
 
 	
 	public Map<String, String[]> getRegisteredChosenCoursesIntern() {
 		Map<String, String[]> tr = new HashMap<String, String[]>();
 		for (Map.Entry<String, String[]> entry : courses.entrySet()) {
 			if (entry.getValue()[2].equals("-")) {
 				tr.put(entry.getKey(), entry.getValue());
 			}
 		}
 		db.clearChosenCourses();
 		db.saveChosenCourses(tr);
 		return tr;
 	}
 
 	
 	public Map<String, String[]> getRegisteredTrainingsIntern() {
 		Map<String, String[]> tr = new HashMap<String, String[]>();
 		for (Map.Entry<String, String[]> entry : courses.entrySet()) {
 			if (!(entry.getValue()[2].length() == 0)
 					&& !(entry.getValue()[2].equals("-"))) {
 				tr.put(entry.getKey(), entry.getValue());
 			}
 		}
 		db.clearRegisteredTrainings();
 		db.saveRegisteredTrainings(tr);
 		return tr;
 	}
 
 	@Override
 	public Map<String, Integer> getSolvedTests() {
 		return db.getSolvedTests();
 	}
 
 	@Override
 	public Map<String, Boolean> getSolvedTrainings() {
 		return db.getSolvedTrainings();
 	}
 
 	private Document profileDoc() {
 		if (profileDoc == null) {
 			profileDoc = session.goToSite(Websites.STISYS_PROFIL);
 		}
 		return profileDoc;
 	}
 
 	private Document coursesDoc() {
 		if (coursesDoc == null) {
 			coursesDoc = session.goToSite(Websites.STISYS_ANMELDUNGEN);
 		}
 		return coursesDoc;
 	}
 
 	private Document printDoc() {
 		if (printDoc == null) {
 			printDoc = session.goToSite(Websites.STISYS_DRUCKQUOTEN);
 		}
 		return printDoc;
 	}
 
 	private Document solvedDoc() {
 		if (solvedDoc == null) {
 			solvedDoc = session.goToSite(Websites.STISYS_ERGEBNISSE);
 		}
 		return solvedDoc;
 
 	}
 	
 	private Document subscribeDoc() {
 		if (subscribeDoc == null) {
 			subscribeDoc = session.goToSite(Websites.STISYS_ANMELDEN);
 		}
 		return subscribeDoc;
 
 	}
 	
 	private Document unsubscribeDoc() {
 		if (unsubscribeDoc == null) {
 			unsubscribeDoc = session.goToSite(Websites.STISYS_ABMELDEN);
 		}
 		return unsubscribeDoc;
 
 	}
 
 	// Seitenaufruf funktioniert nicht
 	@Override
 	public Map<String, String> getMailingLists() {
 		getMailLists();
 		return null;
 	}
 
 	// does not work, site not available
 	private Document mailListDocSEM() {
 		if (mailListDocSEM == null) {
 			mailListDocSEM = session
 					.goToSite(Websites.STISYS_MAILLISTEN_SEMESTERGRP);
 		}
 		return mailListDocSEM;
 	}
 
 	// does not work, site not available
 	private Document mailListDocPRA() {
 		if (mailListDocPRA == null) {
 			mailListDocPRA = session
 					.goToSite(Websites.STISYS_MAILLISTEN_PRAKTIKA);
 		}
 		return mailListDocPRA;
 	}
 
 	// does not work, site not available
 	private Document mailListDocWP() {
 		if (mailListDocWP == null) {
 			mailListDocWP = session.goToSite(Websites.STISYS_MAILLISTEN_WP);
 		}
 		return mailListDocWP;
 	}
 
 	// does not work, site not available
 	private Document mailListDocPRO() {
 		if (mailListDocPRO == null) {
 			mailListDocPRO = session
 					.goToSite(Websites.STISYS_MAILLISTEN_PROJEKT);
 		}
 		return mailListDocPRO;
 	}
 
 	// andere Änderungen:, Notenänderung, Printquoten
 	// Profil-Aktualisierung:
 	// Passwort neu, Hochzeit, Pr�fungsordnung
 	// 1. Tag abpassen, an dem das Passwort neu gesetzt werden muss ->
 	// getProfile()
 	// 2.PW manuell ge�ndert -> mit altem vgl, falls auf dauerhaft gespeichert
 	// eingestellt -> falls eingegebenes != altes pw, -> getProfile()
 	// anderen 3 F�lle: da PW jedes halbe Jahr ge�ndert -> einfach
 	// mit�ndern, da alle Daten neu gezogen werden
 	private void getProfile() {
 		profile = new HashMap<String, String>();
 		Elements table1 = profileDoc().select("tr[class=tablecontentbackdark]")
 				.select("td[align=left]");
 		Elements table2 = profileDoc().select("tr[class=tablecontentbackdark]")
 				.select("td[align=right]");
 		Elements table3 = profileDoc()
 				.select("tr[class=tablecontentbacklight]").select(
 						"td[align=left]");
 		Elements table4 = profileDoc()
 				.select("tr[class=tablecontentbacklight]").select(
 						"td[align=right]");
 
 		for (int i = 0; i < table1.size(); i++) {
 			profile.put(table1.get(i).text(), table2.get(i).text());
 		}
 		for (int i = 0; i < table3.size(); i++) {
 			profile.put(table3.get(i).text(), table4.get(i).text());
 		}
 		getUserNameIntern();
 		getBirthdayIntern();
 		getMatriculationNumberIntern();
 		getPasswordExpirationDateIntern();
 		getStudentNameIntern();		
 	}
 
 	private void getCourses() {
 		courses = new HashMap<String, String[]>();
 		Elements table1 = coursesDoc()
 				.select("tr[class=tablecontentbacklight]").select(
 						"span[class=thin]");
 		Elements table2 = coursesDoc().select("tr[class=tablecontentbackdark]")
 				.select("span[class=thin]");
 
 		for (int i = 0; i < table1.size(); i += 5) {
 			courses.put(table1.get(i).text(), new String[] {
 					table1.get(i + 1).text(), table1.get(i + 2).text(),
 					table1.get(i + 3).text(), table1.get(i + 4).text() });
 		}
 		for (int i = 0; i < table2.size(); i += 5) {
 			courses.put(table2.get(i).text(), new String[] {
 					table2.get(i + 1).text(), table2.get(i + 2).text(),
 					table2.get(i + 3).text(), table2.get(i + 4).text() });
 		}
 		getRegisteredChosenCoursesIntern();
 		getRegisteredSocialSciencesCoursesIntern();
 		getRegisteredTestsIntern();
 		getRegisteredTrainingsIntern();
 	}
 
 	private void getSolvedOnes() {
 		solvedTrainings = new HashMap<String, Boolean>();
 		solvedTests = new HashMap<String, Integer>();
 		Elements table1 = solvedDoc().select("tr[class=tablecontentbacklight]")
 				.select("td");
 		Elements table2 = solvedDoc().select("tr[class=tablecontentbackdark]")
 				.select("td");
 
 		for (int i = 1; i < table1.size(); i += 8) {
 			if (table1.get(i + 6).text().contains("n.e."))
 				continue;
 			if (table1.get(i + 6).text().contains("erf")) {
 				solvedTrainings.put(table1.get(i).text(), Boolean.TRUE);
 			} else {
 				if(Integer.parseInt(table1.get(i + 6).text()) == 0 
 				|| Integer.parseInt(table1.get(i + 6).text()) == 1
 				|| Integer.parseInt(table1.get(i + 6).text()) == 2 
 				|| Integer.parseInt(table1.get(i + 6).text()) == 3
 				|| Integer.parseInt(table1.get(i + 6).text()) == 4) continue;
 				solvedTests.put(table1.get(i).text(),
 						Integer.parseInt(table1.get(i + 6).text()));
 			}
 		}
 
 		for (int i = 1; i < table2.size(); i += 8) {
 			if (table2.get(i + 6).text().contains("n.e."))
 				continue;
 			if (table2.get(i + 6).text().contains("erf")) {
 				solvedTrainings.put(table2.get(i).text(), Boolean.TRUE);
 			} else {
 				solvedTests.put(table2.get(i).text(),
 						Integer.parseInt(table2.get(i + 6).text()));
 			}
 		}
 		db.clearSolvedTrainings();
 		db.saveSolvedTrainings(solvedTrainings);
 		db.clearSolvedTests();
 		db.saveSolvedTests(solvedTests);
 	}
 
 	private void getPrintQuote() {
 		String table1 = printDoc().select("table[class=tablecontent]")
 				.select("td[align=right]").first().text();
 		String a = table1.split("i")[0];
 		printQuote = Integer.parseInt((a.split(",")[0] + a.split(",")[1]
 				.substring(0, 2)));
 		db.setPrintCredit(printQuote);
 	}
 
 	// TODO:does not work, no site available
 	private void getMailLists() {
 		mailLists = new HashMap<String, String>();
 		// Praktika
 		Elements table1 = mailListDocPRA().select(
 				"tr[class=tablecontentbacklight]").select("td[class=thinline]");
 		Elements table2 = mailListDocPRA().select(
 				"tr[class=tablecontentbackdark]").select("td[class=thinline]");
 		for (int i = 0; i < table1.size(); i++) {
 			println(table1.get(i).text());
 		}
 		db.clearMailinglists();
 		db.saveMailinglists(mailLists);
 	}
 
 	// TODO:problems with session -> no need for first site
 	private void nextApplications() {
 		nextApps = new HashMap<String, DateTime[]>();
 		try {
 			Document doc = Jsoup.connect("https://stisys.informatik.haw-hamburg.de/stisys/").execute()
 					.parse();
 			nextApps = StisysUtil.tableToDateMap(doc
 					.select("tr[class=tablecontentbackdark]"));
 			Map<String, DateTime[]> _tmp = StisysUtil.tableToDateMap(doc
 					.select("tr[class=tablecontentbacklight]"));
 			nextApps.putAll(_tmp);
 		} catch (IOException e) {
 			println(e.getMessage());
 		}
 		db.clearNextApplicationDates();
 		db.saveNextApplicationDates(nextApps);
 	}
 
 	private class Session {
 
 		private String userName;
 		private String password;
 		private String sessionId;
 
 		public Session(String userName, String password) {
 			this.userName = userName;
 			this.password = password;
 			makeLogin();
 		}
 
 		private void makeLogin() {
 			try {
 				Connection.Response res = Jsoup
 						.connect(Websites.STISYS_FIRSTSITE)
 						.data("username", userName, "password", password)
 						.method(Method.POST).execute();
 				sessionId = res.cookie("JSESSIONID");
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		private boolean isSessionTimeOut(Document doc) {
 			return doc.body().select("h1").text()
 					.contains("StiSys Session Timeout");
 		}
 
 		private Document goToSite(String site) {
 			Document doc = null;
 			try {
 				doc = Jsoup.connect(site).cookie("JSESSIONID", sessionId)
 						.execute().parse();
 				Log.w(doc.toString(), "blah");
 				if (isSessionTimeOut(doc)) {
 					makeLogin();
 					doc = Jsoup.connect(site).cookie("JSESSIONID", sessionId)
 							.execute().parse();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return doc;
 		}
 	}
 	
 
 	private void println(Object o) {
 		System.out.println(o);
 	}
 
 
 	@Override
 	public String getStudentName() {		
 		return db.getName();
 	}
 
 
 	@Override
 	public String getUserName() {
 		return db.getUserName();
 	}
 
 
 	@Override
 	public DateTime getPasswordExpirationDate() {
 		return db.getPasswordExpirationDate();
 	}
 
 
 	@Override
 	public int getMatriculationNumber() {		
 		return db.getMatricularNumber();
 	}
 
 
 	@Override
 	public DateTime getBirthday() {		
 		return db.getBirthday();
 	}
 
 
 	@Override
 	public Map<String, String[]> getRegisteredSocialSciencesCourses() {
 		return db.getSocialSciencesCourses();
 	}
 
 
 	@Override
 	public Map<String, String[]> getRegisteredTests() {
 		return db.getRegisteredTests();
 	}
 
 
 	@Override
 	public Map<String, String[]> getRegisteredChosenCourses() {
 		return db.getChosenCourses();
 	}
 
 
 	@Override
 	public Map<String, String[]> getRegisteredTrainings() {		
 		return db.getRegisteredTrainings();
 	}
 
 
 
 
 
 
 }
