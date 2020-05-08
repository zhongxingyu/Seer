 /**
  * Copyright (C) 2009 Mads Mohr Christensen, <hr.mohr@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.cubing.liveresults.action.admin;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.usermodel.WorkbookFactory;
 import org.apache.poi.ss.util.CellReference;
 import org.apache.struts2.ServletActionContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.annotation.Secured;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.opensymphony.xwork2.Action;
 
 import dk.cubing.liveresults.action.FrontendAction;
 import dk.cubing.liveresults.model.Competition;
 import dk.cubing.liveresults.model.Competitor;
 import dk.cubing.liveresults.model.Event;
 import dk.cubing.liveresults.model.RegisteredEvents;
 import dk.cubing.liveresults.service.CompetitionService;
 import dk.cubing.liveresults.utilities.CountryUtil;
 import dk.cubing.liveresults.utilities.StringUtil;
 
 @Secured( { "ROLE_USER" })
 public class ScoresheetAction extends FrontendAction {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger log = LoggerFactory.getLogger(ScoresheetAction.class);
 	
 	private CompetitionService competitionService;
 	private final CountryUtil countryUtil;
 	private final SimpleDateFormat birthdayFormat;
 	
 	private final String SHEET_TYPE_REGISTRATION = "Registration";
 	private final String SHEET_TYPE_AVERAGE5S = "average5s";
 	private final String SHEET_TYPE_AVERAGE5M = "average5m";
 	private final String SHEET_TYPE_MEAN3S = "mean3s";
 	private final String SHEET_TYPE_MEAN3M = "mean3m";
 	private final String SHEET_TYPE_BEST1S = "best1s";
 	private final String SHEET_TYPE_BEST1M = "best1m";
 	private final String SHEET_TYPE_BEST1N = "best1n";
 	private final String SHEET_TYPE_TEAMBEST1M = "teambest1m";
 	private final String SHEET_TYPE_BEST2S = "best2s";
 	private final String SHEET_TYPE_BEST2M = "best2m";
 	private final String SHEET_TYPE_BEST2N = "best2n";
 	private final String SHEET_TYPE_TEAMBEST2M = "teambest2m";
 	private final String SHEET_TYPE_BEST3S = "best3s";
 	private final String SHEET_TYPE_BEST3M = "best3m";
 	private final String SHEET_TYPE_BEST3N = "best3n";
 	private final String SHEET_TYPE_TEAMBEST3M = "teambest3m";
 	private final String SHEET_TYPE_MULTIBF1 = "multibf1";
 	private final String SHEET_TYPE_MULTIBF2 = "multibf2";
 	
 	private File csv;
 	private String csvContentType;
 	private String csvFileName;
 
 	private List<Competition> competitions;
 	private String competitionId;
 	private Competition competition;
 	
 	private Map<String, Event> eventNamesMap = new HashMap<String, Event>();
 	private Map<String, String> roundTypesMap = new LinkedHashMap<String, String>();
 	private Map<String, String> formatTypesMap = new LinkedHashMap<String, String>();
 	private Map<String, String> timeFormatTypesMap = new LinkedHashMap<String, String>();
 	private Map<Integer, String> supportedEvents = new HashMap<Integer, String>();
 	
 	private List<String> formats = new ArrayList<String>();
 	private List<String> timeFormats = new ArrayList<String>();
 	private List<String> round1 = new ArrayList<String>();
 	private List<String> round2 = new ArrayList<String>();
 	private List<String> round3 = new ArrayList<String>();
 	private List<String> round4 = new ArrayList<String>();
 	
 	private String spreadSheetFilename;
 	private ByteArrayOutputStream out;
 	
 	public ScoresheetAction() {
 		initMap();
 		countryUtil = new CountryUtil();
 		birthdayFormat = new SimpleDateFormat("yyyy-MM-dd");
 		birthdayFormat.setLenient(false);
 	}
 	
 	/**
 	 * @param competitionService the competitionService to set
 	 */
 	public void setCompetitionService(CompetitionService competitionService) {
 		this.competitionService = competitionService;
 	}
 
 	/**
 	 * @return the competitionService
 	 */
 	public CompetitionService getCompetitionService() {
 		return competitionService;
 	}
 
 	/**
 	 * @param name
 	 * @param format
 	 * @param timeFormat
 	 * @return
 	 */
 	private Event setupEvent(String name, String format, String timeFormat) {
 		Event event = new Event();
 		event.setFormat(format);
 		event.setTimeFormat(timeFormat);
 		event.setName(name);
 		return event;
 	}
 	
 	public void initMap() {
 		// average of 5 with a seconds format
 		eventNamesMap.put("333", setupEvent("3x3", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("222", setupEvent("2x2", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("333oh", setupEvent("oh", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("pyram", setupEvent("pyr", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("clock", setupEvent("clk", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("magic", setupEvent("mgc", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("mmagic", setupEvent("mmgc", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("sq1", setupEvent("sq1", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		
 		// average of 5 with a minutes format
 		eventNamesMap.put("444", setupEvent("4x4", Event.Format.AVERAGE.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		eventNamesMap.put("555", setupEvent("5x5", Event.Format.AVERAGE.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		eventNamesMap.put("minx", setupEvent("minx", Event.Format.AVERAGE.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		
 		// mean of 3 with a minutes format
 		eventNamesMap.put("666", setupEvent("6x6", Event.Format.MEAN.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		eventNamesMap.put("777", setupEvent("7x7", Event.Format.MEAN.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		eventNamesMap.put("333ft", setupEvent("feet", Event.Format.MEAN.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		
 		// best of 3 with a minutes format
 		eventNamesMap.put("333bf", setupEvent("bf", Event.Format.BEST_OF_3.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		
 		// best of 3 with a minutes format
 		eventNamesMap.put("444bf", setupEvent("bf4", Event.Format.BEST_OF_3.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		
 		// best of 2 with a minutes format
 		eventNamesMap.put("555bf", setupEvent("bf5", Event.Format.BEST_OF_2.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		
 		// multi bld format
 		eventNamesMap.put("333mbf", setupEvent("mbf", Event.Format.BEST_OF_1.getValue(), Event.TimeFormat.MULTI_BLD.getValue()));
 		
 		// number format
 		eventNamesMap.put("333fm", setupEvent("fm", Event.Format.BEST_OF_1.getValue(), Event.TimeFormat.NUMBER.getValue()));
 		
 		// unofficial events
 		eventNamesMap.put("333ni", setupEvent("333ni", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("333sbf", setupEvent("333sbf", Event.Format.BEST_OF_3.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("333r3", setupEvent("333r3", Event.Format.BEST_OF_1.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		eventNamesMap.put("333ts", setupEvent("333ts", Event.Format.BEST_OF_1.getValue(), Event.TimeFormat.TEAM.getValue()));
 		eventNamesMap.put("333bts", setupEvent("333bts", Event.Format.BEST_OF_1.getValue(), Event.TimeFormat.TEAM.getValue()));
 		eventNamesMap.put("222bf", setupEvent("222bf", Event.Format.BEST_OF_3.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("333si", setupEvent("333si", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("rainb", setupEvent("rainb", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("snake", setupEvent("snake", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("skewb", setupEvent("skewb", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("mirbl", setupEvent("mirbl", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("222oh", setupEvent("222oh", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("magico", setupEvent("magico", Event.Format.AVERAGE.getValue(), Event.TimeFormat.SECONDS.getValue()));
 		eventNamesMap.put("360", setupEvent("360", Event.Format.AVERAGE.getValue(), Event.TimeFormat.MINUTES.getValue()));
 		
 		// round types map
 		roundTypesMap.put("0", "Qualification round");
 		roundTypesMap.put("1", "First round");
 		roundTypesMap.put("d", "Combined First");
 		roundTypesMap.put("2", "Second round");
 		roundTypesMap.put("3", "Semi Final");
 		roundTypesMap.put("c", "Combined Final"); 
 		roundTypesMap.put("f", "Final");
 		
 		// format types map
 		formatTypesMap.put("a", "Average of 5");
 		formatTypesMap.put("m", "Mean of 3");
 		formatTypesMap.put("1", "Best of 1");
 		formatTypesMap.put("2", "Best of 2");
 		formatTypesMap.put("3", "Best of 3");
 		
 		// time format types map
 		timeFormatTypesMap.put("s", "Seconds");
 		timeFormatTypesMap.put("m", "Minutes");
 		timeFormatTypesMap.put("n", "Number");
 		timeFormatTypesMap.put("b", "Multi BLD");
 		timeFormatTypesMap.put("t", "Team");
 	}
 	
 	/**
 	 * @return the csv
 	 */
 	public File getCsv() {
 		return csv;
 	}
 
 	/**
 	 * @param csv the csv to set
 	 */
 	public void setCsv(File csv) {
 		this.csv = csv;
 	}
 
 	/**
 	 * @return the csvContentType
 	 */
 	public String getCsvContentType() {
 		return csvContentType;
 	}
 
 	/**
 	 * @param csvContentType the csvContentType to set
 	 */
 	public void setCsvContentType(String csvContentType) {
 		this.csvContentType = csvContentType;
 	}
 
 	/**
 	 * @return the csvFileName
 	 */
 	public String getCsvFileName() {
 		return csvFileName;
 	}
 
 	/**
 	 * @param csvFileName the csvFileName to set
 	 */
 	public void setCsvFileName(String csvFileName) {
 		this.csvFileName = csvFileName;
 	}
 	
 	/**
 	 * @param competitions the competitions to set
 	 */
 	public void setCompetitions(List<Competition> competitions) {
 		this.competitions = competitions;
 	}
 
 	/**
 	 * @return the competitions
 	 */
 	@Secured( { "ROLE_ADMIN" })
 	public List<Competition> getCompetitions() {
 		return competitions;
 	}
 
 	/**
 	 * @param competitionId the competitionId to set
 	 */
 	public void setCompetitionId(String competitionId) {
 		this.competitionId = competitionId;
 	}
 
 	/**
 	 * @return the competitionId
 	 */
 	public String getCompetitionId() {
 		return competitionId;
 	}
 
 	/**
 	 * @param competition the competition to set
 	 */
 	public void setCompetition(Competition competition) {
 		this.competition = competition;
 	}
 
 	/**
 	 * @return the competition
 	 */
 	public Competition getCompetition() {
 		return competition;
 	}
 
 	/**
 	 * @return the formats
 	 */
 	public List<String> getFormats() {
 		return formats;
 	}
 
 	/**
 	 * @param formats the formats to set
 	 */
 	public void setFormats(List<String> formats) {
 		this.formats = formats;
 	}
 
 	/**
 	 * @return the timeFormats
 	 */
 	public List<String> getTimeFormats() {
 		return timeFormats;
 	}
 
 	/**
 	 * @param timeFormats the timeFormats to set
 	 */
 	public void setTimeFormats(List<String> timeFormats) {
 		this.timeFormats = timeFormats;
 	}
 
 	/**
 	 * @return the round1
 	 */
 	public List<String> getRound1() {
 		return round1;
 	}
 
 	/**
 	 * @param round1 the round1 to set
 	 */
 	public void setRound1(List<String> round1) {
 		this.round1 = round1;
 	}
 
 	/**
 	 * @return the round2
 	 */
 	public List<String> getRound2() {
 		return round2;
 	}
 
 	/**
 	 * @param round2 the round2 to set
 	 */
 	public void setRound2(List<String> round2) {
 		this.round2 = round2;
 	}
 
 	/**
 	 * @return the round3
 	 */
 	public List<String> getRound3() {
 		return round3;
 	}
 
 	/**
 	 * @param round3 the round3 to set
 	 */
 	public void setRound3(List<String> round3) {
 		this.round3 = round3;
 	}
 
 	/**
 	 * @return the round4
 	 */
 	public List<String> getRound4() {
 		return round4;
 	}
 
 	/**
 	 * @param round4 the round4 to set
 	 */
 	public void setRound4(List<String> round4) {
 		this.round4 = round4;
 	}
 
 	/**
 	 * @param spreadSheetFilename the spreadSheetFilename to set
 	 */
 	public void setSpreadSheetFilename(String spreadSheetFilename) {
 		this.spreadSheetFilename = spreadSheetFilename;
 	}
 
 	/**
 	 * @return the spreadSheetFilename
 	 */
 	public String getSpreadSheetFilename() {
 		return spreadSheetFilename;
 	}
 	
 	/**
 	 * @return
 	 */
 	public InputStream getInputStream () {
 		return new ByteArrayInputStream(out.toByteArray());
 	}
 	
 	/**
 	 * @return
 	 */
 	public int getContentLength() {
 		return out.size();
 	}
 	
 	/**
 	 * @return
 	 */
 	public String getContentDisposition() {
 		return "attachment; filename=" + getCompetitionId() + ".xls";
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.opensymphony.xwork2.ActionSupport#execute()
 	 */
 	public String execute() throws Exception {
 		return Action.SUCCESS;
 	}
 	
 	/* (non-Javadoc)
 	 * @see dk.cubing.liveresults.action.FrontendAction#list()
 	 */
 	@Override
 	public String list() {
 		setCompetitions(getCompetitionService().list(page, size));
 		return Action.SUCCESS;
 	}
 	
 	/**
 	 * @return
 	 */
 	public String parseCsv() {
 		if (csv != null && competitionId != null) {
 			Competition competitionTemplate = getCompetitionService().find(competitionId);
 			if (competitionTemplate == null) {
 				log.error("Could not load competition: {}", competitionId);
 				return Action.ERROR;
 			}
 			
 			// reset
 			setFormats(new ArrayList<String>());
 			setTimeFormats(new ArrayList<String>());
 			setRound1(new ArrayList<String>());
 			setRound2(new ArrayList<String>());
 			setRound3(new ArrayList<String>());
 			setRound4(new ArrayList<String>());
 			supportedEvents.clear();
 			
 			Competition competition = new Competition();
 			competition.setCompetitionId(competitionTemplate.getCompetitionId());
 			competition.setName(competitionTemplate.getName());
 			
 			// parse csv file
 			try {
 				CSVReader reader = new CSVReader(new InputStreamReader(new FileInputStream(csv), "ISO-8859-1"), ',');
 				List<String[]> csvLines = reader.readAll();
 				// first row which includes event names
 				List<Event> events = parseEvents(csvLines.remove(0));
 				
 				// sort events
 				Collections.sort(events, new Comparator<Event>() {
 					public int compare(Event e1, Event e2) {
 						String f1 = e1.getName();
 						String f2 = e2.getName();
 						return f1.compareTo(f2);
 					}
 				});
 				competition.setEvents(events);
 				
 				// the remaining rows contains competitors
 				List<Competitor> competitors = new ArrayList<Competitor>();
 				for (String[] line : csvLines) {
 					Competitor competitor = parseCompetitor(line);
 					if (competitor != null) {
 						competitors.add(competitor);
 					}
 				}
 				
 				// sort competitors
 				Collections.sort(competitors, new Comparator<Competitor>() {
 					public int compare(Competitor c1, Competitor c2) {
 						String f1 = c1.getFirstname() + " " + c1.getSurname();
 						String f2 = c2.getFirstname() + " " + c2.getSurname();
 						return f1.compareTo(f2);
 					}
 				});
 				competition.setCompetitors(competitors);
 				setCompetition(competition);
 				return Action.SUCCESS;
 			} catch (Exception e) {
 				log.error(e.getLocalizedMessage(), e);
 			}
 		}
 		return Action.INPUT;
 	}
 
 	/**
 	 * @param line
 	 * @return
 	 */
 	private Map<Integer, String> parseEventNames(String[] line) {
 		try {
 			for (int i=7; i<line.length; i++) {
 			    String eventName = line[i];
 			    if (eventName != null && !"Email".equals(eventName) && !"Guests".equals(eventName) && !"IP".equals(eventName)) {
 			    	supportedEvents.put(i, eventName);
 			    }
 			}
 		} catch (Exception e) {
 			log.error(e.getLocalizedMessage(), e);
 		}
 		return supportedEvents;
 	}
 
 	/**
 	 * @param line
 	 * @return
 	 */
 	private Competitor parseCompetitor(String[] line) {
 		Competitor competitor = null;
 		if ("a".equals(line[0])) { // only parse accepted competitors
 			competitor = getCompetitor(line);
 			if (competitor != null) {
 				try {
 					RegisteredEvents registeredEvents = new RegisteredEvents();
 					for (int i=0; i<supportedEvents.size(); i++) {
 					    String eventName = line[i+7];
 					    if ("0".equals(eventName) || "1".equals(eventName)) {
 					    	try {
 								Method method = registeredEvents.getClass().getMethod("setSignedUpFor" + getEventName(i+7), boolean.class);
 								method.invoke(registeredEvents, "1".equals(eventName));
 							} catch (Exception e) {
 								log.error("[{}] " + e.getLocalizedMessage(), e);
 							}
 					    }
 					}
 					competitor.setRegisteredEvents(registeredEvents);
 				} catch (Exception e) {
 					log.error(e.getLocalizedMessage(), e);
 				}
 			}
 		}
 		return competitor;
 	}
 	
 	/**
 	 * @param line
 	 * @return
 	 */
 	private Competitor getCompetitor(String[] line) {
 		Competitor competitor = new Competitor();
 		competitor.setFirstname(StringUtil.parseFirstname(line[1]));
 		competitor.setSurname(StringUtil.parseSurname(line[1]));
 		competitor.setCountry(countryUtil.getCountryCodeByName(line[2]));
 		competitor.setWcaId(line[3]);
 		Date date = new Date();
 		if (line[4] != null) {
 			try {
 				date = birthdayFormat.parse(line[4]);
 			} catch (ParseException e) {
 				log.error(e.getLocalizedMessage(), e);
 			}
 		}
 		competitor.setBirthday(date);
 		competitor.setGender(line[5]);
 		return competitor;
 	}
 
 	/**
 	 * @param line
 	 * @return
 	 */
 	private List<Event> parseEvents(String[] line) {
 		List<Event> events = new ArrayList<Event>();
 		for (int idx : parseEventNames(line).keySet()) {
 			Event event = new Event();
 			event.setName(getEventName(idx));
 			event.setFormat(getEventFormat(idx));
 			event.setTimeFormat(getTimeFormat(idx));
 			events.add(event);
 			log.debug("Event: {}, Format: {}, Timeformat: {}", new Object[]{event.getName(), event.getFormat(), event.getTimeFormat()});
 		}
 		return events;
 	}
 	
 	/**
 	 * @param i
 	 * @return
 	 */
 	private String getEventName(int i) {
 		Event event = eventNamesMap.get(supportedEvents.get(i));
 		return StringUtil.ucfirst(event.getName());
 	}
 	
 	/**
 	 * @param i
 	 * @return
 	 */
 	private String getEventFormat(int i) {
 		Event event = eventNamesMap.get(supportedEvents.get(i));
 		return event.getFormat();
 	}
 	
 	/**
 	 * @param i
 	 * @return
 	 */
 	private String getTimeFormat(int i) {
 		Event event = eventNamesMap.get(supportedEvents.get(i));
 		return event.getTimeFormat();
 	}
 	
 	/**
 	 * @return
 	 */
 	public Map<String, String> getRoundTypesMap() {
 		return roundTypesMap;
 	}
 	
 	/**
 	 * @return the formatTypesMap
 	 */
 	public Map<String, String> getFormatTypesMap() {
 		return formatTypesMap;
 	}
 
 	/**
 	 * @return the timeFormatTypesMap
 	 */
 	public Map<String, String> getTimeFormatTypesMap() {
 		return timeFormatTypesMap;
 	}
 
 	/**
 	 * @param workBook
 	 * @param format
 	 * @param timeFormat
 	 * @return
 	 */
 	private Sheet getResultSheet(Workbook workBook, String format, String timeFormat) {
 		Sheet sheet = null;
 		if (Event.Format.AVERAGE.getValue().equals(format)) {
 			if (Event.TimeFormat.SECONDS.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_AVERAGE5S);
 			} else if (Event.TimeFormat.MINUTES.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_AVERAGE5M);
 			} else if (Event.TimeFormat.NUMBER.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Fewest moves uses Best of and not Average.");
 			} else if (Event.TimeFormat.MULTI_BLD.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Multi BLD uses Best of and not Average.");
 			} else if (Event.TimeFormat.TEAM.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Team events uses Best of and not Average.");
 			} else {
 				log.error("Unknown format: {}, time format: {}", format, timeFormat);
 			}
 		} else if (Event.Format.MEAN.getValue().equals(format)) {
 			if (Event.TimeFormat.SECONDS.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_MEAN3S);
 			} else if (Event.TimeFormat.MINUTES.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_MEAN3M);
 			} else if (Event.TimeFormat.NUMBER.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Fewest moves uses Best of and not Mean.");
 			} else if (Event.TimeFormat.MULTI_BLD.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Multi BLD uses Best of and not Mean.");
 			} else if (Event.TimeFormat.TEAM.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Team events uses Best of and not Mean.");
 			} else {
 				log.error("Unknown format: {}, time format: {}", format, timeFormat);
 			}
 		} else if (Event.Format.BEST_OF_1.getValue().equals(format)) {
 			if (Event.TimeFormat.SECONDS.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST1S);
 			} else if (Event.TimeFormat.MINUTES.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST1M);
 			} else if (Event.TimeFormat.NUMBER.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST1N);
 			} else if (Event.TimeFormat.MULTI_BLD.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_MULTIBF1);
 			} else if (Event.TimeFormat.TEAM.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_TEAMBEST1M);
 			} else {
 				log.error("Unknown format: {}, time format: {}", format, timeFormat);
 			}
 		} else if (Event.Format.BEST_OF_2.getValue().equals(format)) {
 			if (Event.TimeFormat.SECONDS.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST2S);
 			} else if (Event.TimeFormat.MINUTES.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST2M);
 			} else if (Event.TimeFormat.NUMBER.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST2N);
 			} else if (Event.TimeFormat.MULTI_BLD.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_MULTIBF2);
 			} else if (Event.TimeFormat.TEAM.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_TEAMBEST2M);
 			} else {
 				log.error("Unknown format: {}, time format: {}", format, timeFormat);
 			}
 		} else if (Event.Format.BEST_OF_3.getValue().equals(format)) {
 			if (Event.TimeFormat.SECONDS.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST3S);
 			} else if (Event.TimeFormat.MINUTES.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST3M);
 			} else if (Event.TimeFormat.NUMBER.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_BEST3N);
 			} else if (Event.TimeFormat.MULTI_BLD.getValue().equals(timeFormat)) {
 				log.error("Unsupported format: Multi BLD uses Best of 1 or Best of 2 and not Best of 3.");
 			} else if (Event.TimeFormat.TEAM.getValue().equals(timeFormat)) {
 				sheet = workBook.getSheet(SHEET_TYPE_TEAMBEST3M);
 			} else {
 				log.error("Unknown format: {}, time format: {}", format, timeFormat);
 			}
 		}
 		return sheet;
 	}
 	
 	/**
 	 * @return
 	 */
 	public String generateScoresheet() {
 		if (!getFormats().isEmpty() && !getTimeFormats().isEmpty() && !getRound1().isEmpty()) {
 			try {
 				// load WCA template from file
 				InputStream is = ServletActionContext.getServletContext().getResourceAsStream(getSpreadSheetFilename());
 				Workbook workBook;
 				workBook = WorkbookFactory.create(is);
 				is.close();
 				
 				// build special registration sheet
 				generateRegistrationSheet(workBook, getCompetition());
 				
 				// build result sheets
 				generateResultSheets(
 						workBook, 
 						getCompetition(), 
 						getFormats(), 
 						getTimeFormats(), 
 						getRound1(),
 						getRound2(),
 						getRound3(),
 						getRound4()
 				);
 
 				// set default selected sheet
 				workBook.setActiveSheet(workBook.getSheetIndex(SHEET_TYPE_REGISTRATION));
 				
 				// output generated spreadsheet
 				log.debug("Ouputting generated workbook");
 				out = new ByteArrayOutputStream();
 				workBook.write(out);
 				out.close();
 				
 				return Action.SUCCESS;
 			} catch (InvalidFormatException e) {
 				log.error("Spreadsheet template are using an unsupported format.", e);
 			} catch (IOException e) {
 				log.error("Error reading spreadsheet template.", e);
 			}
 			return Action.ERROR;
 		} else {
 			return Action.INPUT;
 		}
 	}
 
 	/**
 	 * @param workBook
 	 * @param competition
 	 * @throws RuntimeException
 	 */
 	private void generateRegistrationSheet(Workbook workBook, Competition competition) throws RuntimeException {
 	    Sheet sheet = workBook.getSheet(SHEET_TYPE_REGISTRATION);
 		if (sheet != null) {
 			log.debug("Building registration sheet. Number of competitors: {}", competition.getCompetitors().size());
 			
 			// competition name
 			Cell competitionName = getCell(sheet, 0, 0, Cell.CELL_TYPE_STRING);
 			competitionName.setCellValue(competition.getName());
 			
 			// competitors data and registered events
 			generateCompetitorRows(workBook, sheet, competition.getCompetitors(), 3);
 		} else {
 			log.error("Could not find sheet: {}", SHEET_TYPE_REGISTRATION);
 			throw new RuntimeException("Could not find sheet: " + SHEET_TYPE_REGISTRATION);
 		}
 	}
 
 	/**
 	 * @param workBook
 	 * @param sheet
 	 * @param competitors
 	 * @param startrow
 	 */
 	private void generateCompetitorRows(Workbook workBook, Sheet sheet, List<Competitor> competitors, int startrow) {
 		int line = startrow;
 		for (Competitor competitor : competitors) {
 			// number
 			if (SHEET_TYPE_REGISTRATION.equals(sheet.getSheetName())) {
 				Cell number = getCell(sheet, line, 0, Cell.CELL_TYPE_FORMULA);
 				number.setCellFormula("IF(COUNTBLANK(B"+line+")>0,\"\",ROW()-3)");
 			}	
 			
 			// name
 			Cell name = getCell(sheet, line, 1, Cell.CELL_TYPE_STRING);
 			name.setCellValue(competitor.getFirstname() + " " + competitor.getSurname());
 			
 			// country
 			Cell country = getCell(sheet, line, 2, Cell.CELL_TYPE_STRING);
 			country.setCellValue(countryUtil.getCountryByCode(competitor.getCountry()));
 			
 			// wca id
 			String wcaId = competitor.getWcaId();
 			Cell wcaIdCell = null;
 			if (wcaId == null || "".equals(wcaId)) {
 				wcaIdCell = getCell(sheet, line, 3, Cell.CELL_TYPE_BLANK);
 			} else {
 				wcaIdCell = getCell(sheet, line, 3, Cell.CELL_TYPE_STRING);
 				wcaIdCell.setCellValue(wcaId);
 			}
 			
 			// handle registration sheet
 			if (SHEET_TYPE_REGISTRATION.equals(sheet.getSheetName())) {
 				// gender
 				Cell gender = getCell(sheet, line, 4, Cell.CELL_TYPE_STRING);
 				gender.setCellValue(competitor.getGender());
 				
 				// birthday
 			    Cell birthday = getCell(sheet, line, 5, Cell.CELL_TYPE_NUMERIC);
 			    birthday.setCellValue(competitor.getBirthday());
 			    
 			    // registered events
 			    List<Boolean> signupList = competitor.getRegisteredEvents().getSignupList();
 			    for (int i=0; i<signupList.size(); i++) {
 			    	if (signupList.get(i)) {
 			    		Cell signup = getCell(sheet, line, 7+i, Cell.CELL_TYPE_NUMERIC);
 			    		signup.setCellValue(1);
 			    	}
 			    }
 			}
 			
 		    // loop
 			line++;
 		}
 		
 		// adjust competitors count per event
 		if (SHEET_TYPE_REGISTRATION.equals(sheet.getSheetName())) {
 			for (int i=0; i<33; i++) {
 				Cell count = getCell(sheet, 1, 7+i, Cell.CELL_TYPE_FORMULA);
				String col = CellReference.convertNumToColString(7+i);
 				String ref = col + "4:" + col + line;
 				count.setCellFormula("SUM("+ref+")");
 			}
 		}
 	}
 	
 	/**
 	 * @param workBook
 	 * @param competition
 	 * @param formats
 	 * @param timeFormats
 	 * @param round1
 	 * @param round2
 	 * @param round3
 	 * @param round4
 	 * @throws RuntimeException
 	 */
 	private void generateResultSheets(Workbook workBook,
 			Competition competition, List<String> formats,
 			List<String> timeFormats, List<String> round1,
 			List<String> round2, List<String> round3, List<String> round4) throws RuntimeException {
 
 		int i = 0;
 		for (Event event : competition.getEvents()) {
 			
 			// get sheet template
 			Sheet template = getResultSheet(workBook, formats.get(i), timeFormats.get(i));
 			boolean isTeamEvent = Event.TimeFormat.TEAM.getValue().equals(event.getTimeFormat());
 			if (template != null) {
 				// this order handles the order of which the result sheets appear
 				if (!round4.isEmpty() && !"-1".equals(round4.get(i))) {
 					createResultSheetFromTemplate(workBook, template, competition, event, round4.get(i), false);
 				}
 				if (!round3.isEmpty() && !"-1".equals(round3.get(i))) {
 					createResultSheetFromTemplate(workBook, template, competition, event, round3.get(i), false);
 				}
 				if (!round2.isEmpty() && !"-1".equals(round2.get(i))) {
 					createResultSheetFromTemplate(workBook, template, competition, event, round2.get(i), false);
 				}
 				if (!round1.isEmpty() && !"-1".equals(round1.get(i))) {
 					createResultSheetFromTemplate(workBook, template, competition, event, round1.get(i), !isTeamEvent);
 				}
 				
 			} else {
 				log.warn("Could not get result sheet template. This could be a problem, Format: {}, Time format: {}", "a", "s");
 			}
 			
 			// loop
 			i++;
 		}
 	}
 
 	/**
 	 * @param workBook
 	 * @param template
 	 * @param competition
 	 * @param event
 	 * @param round
 	 * @param includeCompetitors
 	 */
 	private void createResultSheetFromTemplate(Workbook workBook,
 			Sheet template, Competition competition, Event event, String round, boolean includeCompetitors) {
 		Sheet resultSheet = workBook.cloneSheet(workBook.getSheetIndex(template));
 		String sheetName = event.getName() + " - " + getRoundTypesMap().get(round);
 		log.debug("Building result sheet: {}", sheetName);
 		String eventNameFormatted = getText("admin.scoresheet.eventname." + event.getName().toLowerCase()) + " - " + getRoundTypesMap().get(round);
 		workBook.setSheetName(workBook.getSheetIndex(resultSheet), sheetName);
 		workBook.setSheetOrder(sheetName, 1); // first sheet is the registration sheet, let's put results directly after that
 		Cell eventName = getCell(resultSheet, 0, 0, Cell.CELL_TYPE_STRING);
 		eventName.setCellValue(eventNameFormatted);
 
 		// get cell styles from template
 		List<CellStyle> cellStyles = new ArrayList<CellStyle>();
 		Row startRow = template.getRow(4);
 		int numberOfColumns = template.getRow(3).getPhysicalNumberOfCells();
 		if (startRow != null) {
 			log.debug("Start row contains {} cells.", numberOfColumns);
 			for (int i=0; i<numberOfColumns; i++) {
 				Cell cell = startRow.getCell(i);
 				if (cell != null) {
 					cellStyles.add(cell.getCellStyle());
 				} else {
 					cellStyles.add(workBook.createCellStyle());
 				}
 			}
 		}
 
 		// adjust formulas
 		int numberOfCompetitors = competition.getCompetitors().size();
 		for (int i=0; i<numberOfCompetitors; i++) {
 			for (int j=0; j<numberOfColumns; j++) {
 				if (SHEET_TYPE_AVERAGE5S.equals(template.getSheetName())) {
 					String range = "E"+(i+5)+":"+"I"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(AND(M"+(i+4)+"=M"+(i+5)+",J"+(i+4)+"=J"+(i+5)+"),A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 9:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=5,\"\",\"DNF\"))");
 						break;
 					// worst
 					case 11:
 						Cell worst = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						worst.setCellFormula("IF(COUNTBLANK("+range+")>0,\"\",IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>0,\"DNF\",MAX("+range+")))");
 						break;
 					// average
 					case 12:
 						Cell average = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						average.setCellFormula("IF(COUNTBLANK("+range+")>0,\"\",IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>1,\"DNF\",ROUND(IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>0,(SUM("+range+")-J"+(i+5)+")/3,(SUM("+range+")-J"+(i+5)+"-L"+(i+5)+")/3),2)))");
 						break;
 					}
 				} else if (SHEET_TYPE_AVERAGE5M.equals(template.getSheetName())) {
 					String range = "E"+(i+5)+":"+"I"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(AND(M"+(i+4)+"=M"+(i+5)+",J"+(i+4)+"=J"+(i+5)+"),A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 9:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=5,\"\",\"DNF\"))");
 						break;
 					// worst
 					case 11:
 						Cell worst = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						worst.setCellFormula("IF(COUNTBLANK("+range+")>0,\"\",IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>0,\"DNF\",MAX("+range+")))");
 						break;
 					// average
 					case 12:
 						Cell average = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						average.setCellFormula("IF(COUNTBLANK("+range+")>0,\"\",IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>1,\"DNF\",IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>0,(SUM("+range+")-J"+(i+5)+")/3,(SUM("+range+")-J"+(i+5)+"-L"+(i+5)+")/3)))");
 						break;
 					}
 				} else if (SHEET_TYPE_MEAN3S.equals(template.getSheetName()) || SHEET_TYPE_MEAN3M.equals(template.getSheetName())) {
 					String range = "E"+(i+5)+":"+"G"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(AND(H"+(i+4)+"=H"+(i+5)+",J"+(i+4)+"=J"+(i+5)+"),A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 7:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=3,\"\",\"DNF\"))");
 						break;
 					// mean
 					case 9:
 						Cell mean = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						mean.setCellFormula("IF(COUNTBLANK("+range+")>0,\"\",IF(COUNTIF("+range+",\"DNF\")+COUNTIF("+range+",\"DNS\")>0,\"DNF\",ROUND(AVERAGE("+range+"),2)))");
 						break;
 					}
 				} else if (SHEET_TYPE_BEST1S.equals(template.getSheetName()) || SHEET_TYPE_BEST1M.equals(template.getSheetName()) || SHEET_TYPE_BEST1N.equals(template.getSheetName())) {
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(E"+(i+4)+"=E"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					}
 				} else if (SHEET_TYPE_TEAMBEST1M.equals(template.getSheetName())) {
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(H"+(i+4)+"=H"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					}
 				} else if (SHEET_TYPE_BEST2S.equals(template.getSheetName()) || SHEET_TYPE_BEST2M.equals(template.getSheetName()) || SHEET_TYPE_BEST2N.equals(template.getSheetName())) {
 					String range = "E"+(i+5)+":"+"F"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(G"+(i+4)+"=G"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 6:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=2,\"\",\"DNF\"))");
 						break;
 					}
 				} else if (SHEET_TYPE_TEAMBEST2M.equals(template.getSheetName())) {
 					String range = "H"+(i+5)+":"+"I"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(J"+(i+4)+"=J"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 9:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=2,\"\",\"DNF\"))");
 						break;
 					}
 				} else if (SHEET_TYPE_BEST3S.equals(template.getSheetName()) || SHEET_TYPE_BEST3M.equals(template.getSheetName()) || SHEET_TYPE_BEST3N.equals(template.getSheetName())) {
 					String range = "E"+(i+5)+":"+"G"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(H"+(i+4)+"=H"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 7:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=3,\"\",\"DNF\"))");
 						break;
 					}
 				} else if (SHEET_TYPE_TEAMBEST3M.equals(template.getSheetName())) {
 					String range = "H"+(i+5)+":"+"J"+(i+5);
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(K"+(i+4)+"=K"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					// best
 					case 10:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(MIN("+range+")>0,MIN("+range+"),IF(COUNTBLANK("+range+")=2,\"\",\"DNF\"))");
 						break;
 					}
 				} else if (SHEET_TYPE_MULTIBF1.equals(template.getSheetName())) {
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(I"+(i+4)+"=I"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					// result
 					case 8:
 						Cell result = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						result.setCellFormula("IF(E"+(i+5)+"-F"+(i+5)+">F"+(i+5)+",-1,(99-F"+(i+5)+"+E"+(i+5)+"-F"+(i+5)+")*10000000+G"+(i+5)+"*100+E"+(i+5)+"-F"+(i+5)+")");
 						break;
 					}
 				} else if (SHEET_TYPE_MULTIBF2.equals(template.getSheetName())) {
 					switch (j) {
 					// rank
 					case 0:
 						Cell rank = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						rank.setCellFormula("IF(COUNTBLANK(B"+(i+5)+")>0,\"\",IF(I"+(i+4)+"=I"+(i+5)+",A"+(i+4)+",ROW()-4))");
 						break;
 					// result1
 					case 7:
 						Cell result1 = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						result1.setCellFormula("IF(E"+(i+5)+"=\"DNS\",-2,IF(E"+(i+5)+"-F"+(i+5)+">F"+(i+5)+",-1,(99-F"+(i+5)+"+E"+(i+5)+"-F"+(i+5)+")*10000000+G"+(i+5)+"*100+E"+(i+5)+"-F"+(i+5)+"))");
 						break;
 					// result2
 					case 11:
 						Cell result2 = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						result2.setCellFormula("IF(I"+(i+5)+"=\"DNS\",-2,IF(I"+(i+5)+"-J"+(i+5)+">J"+(i+5)+",-1,(99-J"+(i+5)+"+I"+(i+5)+"-J"+(i+5)+")*10000000+K"+(i+5)+"*100+I"+(i+5)+"-J"+(i+5)+"))");
 						break;
 					// best
 					case 12:
 						Cell best = getCell(resultSheet, i+4, j, Cell.CELL_TYPE_FORMULA);
 						best.setCellFormula("IF(AND(H"+(i+5)+"<0,L"+(i+5)+"<0),-1,IF("+(i+5)+"<0,L"+(i+5)+",IF(L"+(i+5)+"<0,H"+(i+5)+",MIN(H"+(i+5)+",L"+(i+5)+"))))");
 						break;
 					}
 				} else {
 					log.error("Unsupported sheet type: {}", template.getSheetName());
 				}
 				
 				// set cell style
 				Row row = resultSheet.getRow(i+4);
 				if (row != null) {
 					Cell cell = row.getCell(j);
 					if (cell != null) {
 						cell.setCellStyle(cellStyles.get(j));
 					}
 				}
 			}
 		}
 		
 		// fill sheet with competitors for this event
 		if (includeCompetitors) {
 			try {
 				generateCompetitorRows(workBook, resultSheet, competition.getCompetitorsByEvent(event), 4);
 			} catch (Exception e) {
 				log.error("[{}] " + e.getLocalizedMessage(), e);
 				throw new RuntimeException("Could not include competitors in this sheet.", e);
 			}
 		}
 	}
 	
 	/**
 	 * @param sheet
 	 * @param rownum
 	 * @param cellnum
 	 * @param cellType
 	 * @return
 	 * @throws RuntimeException
 	 */
 	private Cell getCell(Sheet sheet, int rownum, int cellnum, int cellType) throws RuntimeException {
 		Row row = sheet.getRow(rownum);
 		if (row == null) {
 			row = sheet.createRow(rownum);
 		}
 		Cell cell = row.getCell(cellnum);
 		if (cell == null) {
 			cell = row.createCell(cellnum);
 			cell.setCellType(cellType);
 		} else {
 			if (cell.getCellType() != cellType && cell.getCellType() != Cell.CELL_TYPE_BLANK) {
 				log.error("Unexpected cell type. Sheet: {}, Row: {}, Cell: {}", new Object[]{sheet.getSheetName(), rownum, cellnum});
 				throw new RuntimeException("Unexpected cell type.");
 			}
 		}
 		return cell;
 	}
 }
