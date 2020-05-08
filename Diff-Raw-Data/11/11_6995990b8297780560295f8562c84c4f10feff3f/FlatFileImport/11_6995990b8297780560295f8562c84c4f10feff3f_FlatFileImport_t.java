 package com.hoegernet.wrsvpdf.fileio.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.hoegernet.common.logger.Logger;
 import com.hoegernet.wrsvpdf.exceptions.ParseException;
 import com.hoegernet.wrsvpdf.exceptions.PdfGeneratorException;
 import com.hoegernet.wrsvpdf.fileio.FileIO;
 import com.hoegernet.wrsvpdf.fileio.IFileImporter;
 import com.hoegernet.wrsvpdf.types.Halle;
 import com.hoegernet.wrsvpdf.types.RankingPos;
 import com.hoegernet.wrsvpdf.types.Spieltag;
 import com.hoegernet.wrsvpdf.types.Staffel;
 import com.hoegernet.wrsvpdf.types.Tabelle;
 import com.hoegernet.wrsvpdf.types.Team;
 import com.hoegernet.wrsvpdf.types.Verein;
 import com.hoegernet.wrsvpdf.types.WRSVPerson;
 
 /**
  * 
  * @author Thorsten Hger
  * 
  *         Projekt: com.hoegernet.wrsvpdf/ Type: FlatFileImport
  * 
  *         created: 20.07.2008
  * 
  */
 public class FlatFileImport implements IFileImporter {
 	
 	public Staffel loadStaffelFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading Staffel from file: " + filename);
 		Staffel staffel = new Staffel();
 		
 		String[] lines = FileIO.getLinesFromFile(filename);
 		if (lines.length < 2) {
 			throw new ParseException("Wrong line count in file: " + filename);
 		}
 		
 		String[] firstline = FileIO.getFieldsFromLine(lines[0]);
 		staffel.setTitle(firstline[0]);
 		staffel.setName(firstline[1]);
 		
 		String[] person = FileIO.getFieldsFromLine(lines[1]);
 		staffel.setStaffelleiter_name(person[0]);
 		staffel.setStaffelleiter_strasse(person[1]);
 		staffel.setStaffelleiter_telfax(person[2]);
 		staffel.setStaffelleiter_mail(person[3]);
 		
 		String reg = "";
 		for (int i = 2; i < lines.length; i++) {
 			reg += FileIO.cleanQuotes(lines[i]);
 		}
 		staffel.setRegelungen(reg);
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return staffel;
 	}
 	
 	public Halle[] loadHallenFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading Hallen from file: " + filename);
 		String[] lines = FileIO.getLinesFromFile(filename);
 		Halle[] hallen = new Halle[lines.length];
 		
 		for (int i = 0; i < hallen.length; i++) {
 			Halle h = new Halle();
 			String[] l = FileIO.getFieldsFromLine(lines[i]);
 			
 			if (l.length != 5) {
 				throw new ParseException("Wrong field count in file: " + filename + " at line: " + lines[i]);
 			}
 			
 			h.setVerein(l[0]);
 			h.setName(l[1]);
 			h.setStrasse(l[2]);
 			h.setOrt(l[3]);
 			h.setTel(l[4]);
 			
 			hallen[i] = h;
 		}
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return hallen;
 	}
 	
 	public Team[] loadTeamsFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading Teams from file: " + filename);
 		
 		String[] lines = FileIO.getLinesFromFile(filename);
 		Team[] teams = new Team[lines.length];
 		
 		for (int i = 0; i < teams.length; i++) {
 			Team t = new Team();
 			String[] l = FileIO.getFieldsFromLine(lines[i]);
 			
 			if (l.length != 5) {
 				throw new ParseException("Wrong field count in file: " + filename + " at line: " + lines[i]);
 			}
 			
 			t.setName(l[0]);
 			t.setPlayer1(l[1]);
 			t.setDate1(l[2]);
 			t.setPlayer2(l[3]);
 			t.setDate2(l[4]);
 			
 			teams[i] = t;
 		}
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return teams;
 	}
 	
 	public Verein[] loadClubsFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading Clubs from file: " + filename);
 		
 		String[] lines = FileIO.getLinesFromFile(filename);
 		Verein[] clubs = new Verein[lines.length];
 		
 		for (int i = 0; i < clubs.length; i++) {
 			Verein v = new Verein();
 			String[] l = FileIO.getFieldsFromLine(lines[i]);
 			
 			if (l.length != 7) {
 				throw new ParseException("Wrong field count in file: " + filename + " at line: " + lines[i]);
 			}
 			
 			v.setName(l[0]);
 			v.setPerson(l[1]);
 			v.setStrasse(l[2]);
 			v.setOrt(l[3]);
 			v.setTel(l[4]);
 			v.setFax(l[5]);
 			v.setMail(l[6]);
 			
 			clubs[i] = v;
 		}
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return clubs;
 	}
 	
 	public Spieltag loadSpieltagFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading Spieltag from file: " + filename);
 		
 		String[] lines = FileIO.getLinesFromFile(filename);
 		if (lines.length < 4) {
 			throw new ParseException("Wrong field count in file: " + filename);
 		}
 		
 		Spieltag tag = new Spieltag();
 		
 		tag.setTitle(FileIO.cleanQuotes(lines[0]));
 		tag.setDatum(FileIO.cleanQuotes(lines[1]));
 		tag.setZeit(FileIO.cleanQuotes(lines[2]));
 		tag.setOrt(FileIO.cleanQuotes(lines[3]));
 		
 		String[] halle = FileIO.getFieldsFromLine(lines[4]);
 		if (halle.length != 4) {
 			throw new ParseException("Wrong field count in file: " + filename);
 		}
 		
 		tag.setHalle_name(halle[0]);
 		tag.setHalle_ort(halle[1]);
 		tag.setHalle_strasse(halle[2]);
 		tag.setHalle_tel(halle[3]);
 		
 		int index = 5;
 		List<String> teams = new ArrayList<String>();
 		while (!lines[index].equals("<Spielfolge>")) {
 			String t = FileIO.cleanQuotes(lines[index]);
 			if (!t.equals("")) {
 				teams.add(t);
 			}
 			index++;
 		}
 		
 		tag.setTeams(teams);
 		
 		index++;
 		
 		List<String[]> games = new ArrayList<String[]>();
 		for (int i = index; i < lines.length; i++) {
 			games.add(FileIO.getSpielFromLine(FileIO.cleanQuotes(lines[i])));
 		}
 		
 		tag.setGames(games);
 		
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return tag;
 	}
 	
 	public WRSVPerson[] loadWRSVPersonenFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading WRSV from file: " + filename);
 		
 		String[] lines = FileIO.getLinesFromFile(filename);
 		List<WRSVPerson> pers = new ArrayList<WRSVPerson>();
 		
 		for (String lin : lines) {
 			WRSVPerson p = new WRSVPerson();
 			String[] fields = FileIO.getFieldsFromLine(lin);
 			if (fields.length != 7) {
 				throw new ParseException("Wrong field count in file: " + filename + " at line: " + lin);
 			}
 			
 			p.setJobTitle(fields[0]);
 			p.setName(fields[1]);
 			p.setStrasse(fields[2]);
 			p.setOrt(fields[3]);
 			p.setTel(fields[4]);
 			p.setFax(fields[5]);
 			p.setMail(fields[6]);
 			pers.add(p);
 		}
 		
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return pers.toArray(new WRSVPerson[lines.length]);
 	}
 	
 	public Tabelle loadRankingFromFile(String filename) throws PdfGeneratorException {
 		Logger.getInstance().logInfo("FileImport", "Loading Ranking from file: " + filename);
 		
 		String[] lines = FileIO.getLinesFromFile(filename);
 		if (lines.length < 3) {
 			throw new ParseException("Wrong field count in file: " + filename);
 		}
 		
 		Tabelle tab = new Tabelle();
 		
 		String[] line1 = FileIO.getFieldsFromLine(lines[0]);
 		if (line1.length != 2) {
 			throw new ParseException("Wrong field count in file: " + filename);
 		}
 		
 		tab.setYear(line1[0]);
 		tab.setStaffel(line1[1]);
 		tab.setHeadline(FileIO.cleanQuotes(lines[1]));
 		
 		if (!lines[2].equals("<Tabelle>")) {
 			return null;
 		}
 		
 		List<RankingPos> ranks = new ArrayList<RankingPos>();
 		
 		for (int i = 3; i < lines.length; i++) {
 			RankingPos rank = new RankingPos();
 			
 			String[] fields = FileIO.getFieldsFromLineWithoutQuotes(lines[i]);
 			if (fields.length != 7) {
 				throw new ParseException("Wrong field count in file: " + filename + " at line: " + lines[i]);
 			}
 			
 			rank.setPos(fields[0]);
 			rank.setName(fields[1]);
 			rank.setSpiele(fields[2]);
 			rank.setTore_pos(fields[3]);
 			rank.setTore_neg(fields[4]);
			rank.setPkt(fields[5]);
			rank.setDiff(fields[6]);
 			
 			ranks.add(rank);
 		}
 		
 		tab.setTable(ranks);
 		
 		Logger.getInstance().logInfo("FileImport", "Loading DONE");
 		
 		return tab;
 	}
 	
 }
