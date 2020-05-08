 /**
  *
  */
 package com.hoegernet.wrsvpdf.reporting;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import net.sf.jasperreports.engine.JRPrintPage;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 
 import com.hoegernet.jaspertools.HoegernetDataSource;
 import com.hoegernet.wrsvpdf.Configuration;
 import com.hoegernet.wrsvpdf.exceptions.IllegalArgumentException;
 import com.hoegernet.wrsvpdf.exceptions.PdfGeneratorException;
 import com.hoegernet.wrsvpdf.types.Halle;
 import com.hoegernet.wrsvpdf.types.RankingPos;
 import com.hoegernet.wrsvpdf.types.Spieltag;
 import com.hoegernet.wrsvpdf.types.Staffel;
 import com.hoegernet.wrsvpdf.types.Tabelle;
 import com.hoegernet.wrsvpdf.types.Team;
 import com.hoegernet.wrsvpdf.types.Verein;
 import com.hoegernet.wrsvpdf.types.WRSVPerson;
 
 /**
  * This class generates PDF files
  * 
  * type: com.hoegernet.wrsvpdf->Generator
  * 
  * @author Thorsten Hoeger created: 23.07.2007 file: Generator.java
  * 
  */
 public class Generator {
 	
 	/**
 	 * @param staffel
 	 * @param vereine
 	 * @return {@link JasperPrint}
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createClubReport(Staffel staffel, Verein[] vereine) throws PdfGeneratorException {
 		if (staffel == null) {
 			throw new IllegalArgumentException("Staffel mustn't be null");
 		}
 		
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		for (Verein v : vereine) {
 			vecjas.add(new String[] {v.getName(), v.getPerson(), v.getStrasse(), v.getOrt(), v.getTel(), v.getFax(), v.getMail()});
 		}
 		
 		String[] columns = new String[] {"name", "person", "strasse", "ort", "telefon", "telefax", "email"};
 		
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		parameters.put("staffelname", staffel.getName());
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_CLUB;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 	
 	/**
 	 * @param staffel
 	 * @param personen
 	 * @return WRSV
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createWRSVReport(Staffel staffel, WRSVPerson[] personen) throws PdfGeneratorException {
 		if (staffel == null) {
 			throw new IllegalArgumentException("Staffel mustn't be null");
 		}
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		for (WRSVPerson w : personen) {
 			vecjas.add(new String[] {w.getJobTitle(), w.getName(), w.getStrasse(), w.getOrt(), w.getTel(), w.getFax(), w.getMail()});
 		}
 		
 		String[] columns = new String[] {"jobtitle", "person", "strasse", "ort", "telefon", "telefax", "email"};
 		
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_WRSV;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 	
 	/**
 	 * @param staffel
 	 * @param teams
 	 * @return WRSV
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createTeamReport(Staffel staffel, Team[] teams) throws PdfGeneratorException {
 		if (staffel == null) {
 			throw new IllegalArgumentException("Staffel mustn't be null");
 		}
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		for (Team t : teams) {
 			vecjas.add(new String[] {t.getName(), t.getPlayer1(), t.getPlayer2(), t.getDate1(), t.getDate2()});
 		}
 		
 		String[] columns = new String[] {"teamname", "player1", "player2", "year1", "year2"};
 		
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		parameters.put("staffelname", staffel.getName());
 		
 		parameters.put("name", staffel.getStaffelleiter_name());
 		parameters.put("strasseort", staffel.getStaffelleiter_strasse());
 		parameters.put("telfax", staffel.getStaffelleiter_telfax());
 		parameters.put("mail", staffel.getStaffelleiter_mail());
 		parameters.put("regeln", staffel.getRegelungen());
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_TEAM;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 	
 	/**
 	 * @param staffel
 	 * @param hallen
 	 * @return Hallenverzeichnis
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createHallenReport(Staffel staffel, Halle[] hallen) throws PdfGeneratorException {
 		if (staffel == null) {
 			throw new IllegalArgumentException("Staffel mustn't be null");
 		}
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		for (Halle h : hallen) {
 			vecjas.add(new String[] {h.getName(), h.getStrasse(), h.getOrt(), h.getTel(), h.getVerein()});
 		}
 		
 		String[] columns = new String[] {"name", "strasse", "ort", "tel", "verein"};
 		
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		parameters.put("staffelname", staffel.getName());
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_GYM;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 	
 	/**
 	 * @param tabelle
 	 * @return Tabelle
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createRankingReport(Tabelle tabelle) throws PdfGeneratorException {
 		if (tabelle == null) {
 			throw new IllegalArgumentException("Tabelle mustn't be null");
 		}
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		List<RankingPos> ranks = tabelle.getTable();
 		
 		for (RankingPos r : ranks) {
 			vecjas.add(new String[] {r.getPos(), r.getName(), r.getSpiele(), r.getTore_pos(), r.getTore_neg(), r.getDiff(), r.getPkt()});
 		}
 		
 		String[] columns = new String[] {"platz", "name", "spiele", "tore_pos", "tore_neg", "diff", "pkt"};
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
 		
 		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("year", tabelle.getYear());
 		parameters.put("staffelname", tabelle.getStaffel());
 		parameters.put("headline", tabelle.getHeadline());
 		parameters.put("datetime", sdf.format(new Date()));
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_RANKING;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 	
 	/**
 	 * @param tag
 	 * @param staffel
 	 * @param teams
 	 * @param reporting
 	 * @return Spieltag
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createSpieltagReport(Staffel staffel, Spieltag tag, Team[] teams, boolean reporting) throws PdfGeneratorException {
 		if (staffel == null) {
 			throw new IllegalArgumentException("Staffel mustn't be null");
 		}
 		if ((tag == null)) {
 			throw new IllegalArgumentException("Tag musn't be null");
 		}
 		
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		List<String[]> games = tag.getGames();
 		
 		for (String[] g : games) {
 			vecjas.add(g);
 		}
 		
 		String[] columns = new String[] {"number", "team1", "team2"};
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
 		
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		parameters.put("staffelname", staffel.getName());
 		parameters.put("headline", tag.getTitle());
 		parameters.put("datum", tag.getDatum());
 		parameters.put("zeit", tag.getZeit());
 		parameters.put("ort", tag.getOrt());
 		parameters.put("halle", tag.getHalle_name());
 		parameters.put("strasse", tag.getHalle_strasse() + ", " + tag.getHalle_ort());
 		parameters.put("telefon", tag.getHalle_tel());
 		
 		String teamsLine = "";
 		List<Team> actualTeams = new ArrayList<Team>();
 		int len = 0;
 		for (String team : tag.getTeams()) {
 			actualTeams.add(Generator.getTeamFromName(teams, team));
 			teamsLine += team + ", ";
 			len += team.length() + 2;
 			if (len > 65) {
 				teamsLine += "\n";
 				len = 0;
 			}
 		}
 		teamsLine = teamsLine.substring(0, teamsLine.length() - 2);
 		
 		parameters.put("teams", teamsLine);
 		
 		parameters.put("datetime", sdf.format(new Date()));
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_SPIELTAG;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 			jprint.setName(tag.getTitle());
 			
 			if (reporting) {
 				JasperPrint dayrep = Generator.createReportingReport(staffel, tag, actualTeams.toArray(new Team[actualTeams.size()]));
 				
 				for (Object obj : dayrep.getPages()) {
 					if (obj instanceof JRPrintPage) {
 						JRPrintPage page = (JRPrintPage) obj;
 						jprint.addPage(page);
 					}
 				}
 			}
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 	
 	private static Team getTeamFromName(Team[] teams, String name) throws PdfGeneratorException {
 		for (Team team : teams) {
 			if (team.getName().equals(name)) {
 				return team;
 			}
 		}
 		throw new PdfGeneratorException("No Team found for name: " + name);
 	}
 	
 	/**
 	 * @param tag
 	 * @param staffel
 	 * @param teams
 	 * @return Spieltag
 	 * @throws PdfGeneratorException
 	 */
 	public static JasperPrint createReportingReport(Staffel staffel, Spieltag tag, Team[] teams) throws PdfGeneratorException {
 		if (staffel == null) {
 			throw new IllegalArgumentException("Staffel mustn't be null");
 		}
 		if (tag == null) {
 			throw new IllegalArgumentException("Tag mustn't be null");
 		}
 		Vector<Object[]> vecjas = new Vector<Object[]>();
 		
 		Arrays.sort(teams, new Comparator<Team>() {
 			
 			public int compare(Team o1, Team o2) {
 				return o1.getName().compareTo(o2.getName());
 			}
 		});
 		
 		for (Team t : teams) {
 			String[] arr = new String[] {t.getName(), t.getPlayer1(), t.getPlayer2()};
 			vecjas.add(arr);
 		}
 		
 		String[] columns = new String[] {"teamname", "player1", "player2"};
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
 		
 		Map<String, String> parameters = new HashMap<String, String>();
 		parameters.put("title", staffel.getTitle());
 		parameters.put("staffelname", staffel.getName());
 		parameters.put("header", tag.getTitle());
 		parameters.put("ort", tag.getOrt());
 		parameters.put("datetime", sdf.format(new Date()));
 		
 		JasperReport jr;
 		JasperPrint jprint = null;
 		try {
 			String fileName = Configuration.REPORT_REPORTING;
 			jr = JasperCompileManager.compileReport(fileName);
 			jprint = JasperFillManager.fillReport(jr, parameters, new HoegernetDataSource(vecjas, columns));
 		} catch (Exception ex) {
 			throw new PdfGeneratorException(ex);
 		}
 		
 		return jprint;
 	}
 }
