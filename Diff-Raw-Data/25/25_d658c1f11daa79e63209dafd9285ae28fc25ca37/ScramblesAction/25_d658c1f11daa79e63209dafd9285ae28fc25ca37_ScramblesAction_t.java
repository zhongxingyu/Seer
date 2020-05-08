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
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.sax.SAXResult;
 
 import dk.cubing.liveresults.utilities.CsvConverter;
 import org.apache.fop.apps.Fop;
 import org.apache.fop.apps.FopFactory;
 import org.apache.fop.apps.MimeConstants;
 import org.apache.fop.servlet.ServletContextURIResolver;
 import org.apache.struts2.ServletActionContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.annotation.Secured;
 import org.w3c.dom.DOMImplementation;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.opensymphony.xwork2.Action;
 
 import dk.cubing.liveresults.action.FrontendAction;
 import dk.cubing.liveresults.model.Competition;
 import dk.cubing.liveresults.model.Competitor;
 import dk.cubing.liveresults.model.Event;
 import dk.cubing.liveresults.model.RegisteredEvents;
 import dk.cubing.liveresults.service.CompetitionService;
 import dk.cubing.liveresults.utilities.StringUtil;
 
 @Secured( { "ROLE_USER" })
 public class ScramblesAction extends FrontendAction {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final Logger log = LoggerFactory.getLogger(ScramblesAction.class);
 	
 	private CompetitionService competitionService;
     private final CsvConverter csvConverter;
 	
 	private String xsltTemplate;
 	private FopFactory fopFactory;
 	private TransformerFactory tFactory;
 	private Templates cachedXSLT;
 	private ServletContextURIResolver uriResolver;
 	private DOMImplementation docImplementation;
 	
 	private File csv;
 	private String csvContentType;
 	private String csvFileName;
     private boolean csvConvert = false;
     private List<String> csvFileEncodings = new ArrayList<String>();
     private String csvFileEncoding = "ISO-8859-1";
 
 	private List<Competition> competitions;
 	private String competitionId;
 	private Competition competition;
 	
 	private Map<String, Event> eventNamesMap = new HashMap<String, Event>();
 	private Map<String, String> formatTypesMap = new LinkedHashMap<String, String>();
 	private Map<String, String> roundTypesMap = new LinkedHashMap<String, String>();
 	private Map<Integer, String> numberOfGroupsMap = new LinkedHashMap<Integer, String>();
 	private Map<Integer, String> supportedEvents = new HashMap<Integer, String>();
 	private Map<String, Integer> unitSizeMap = new HashMap<String, Integer>();
 	
 	private List<String> formats = new ArrayList<String>();
 	private List<String> round1 = new ArrayList<String>();
 	private List<String> round2 = new ArrayList<String>();
 	private List<String> round3 = new ArrayList<String>();
 	private List<String> round4 = new ArrayList<String>();
 	private List<Integer> group1 = new ArrayList<Integer>();
 	private List<Integer> group2 = new ArrayList<Integer>();
 	private List<Integer> group3 = new ArrayList<Integer>();
 	private List<Integer> group4 = new ArrayList<Integer>();
 	private boolean includeExtraScrambles = true;
 	
 	private ByteArrayOutputStream out;
 	
 	public ScramblesAction() {
 		try {
 			// init FOP
 			fopFactory = FopFactory.newInstance();
 			tFactory = TransformerFactory.newInstance();
 			docImplementation = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
             csvConverter = new CsvConverter();
 			
 			// init maps
 			initMap();
 		} catch (Exception e) {
 			log.error(e.getMessage(), e);
 			throw new RuntimeException("Could not init ScramblesAction", e);
 		}
 	}
 	
 	public void initMap() {
        // supported file encodings
        csvFileEncodings.add("ISO-8859-1");
        csvFileEncodings.add("UTF-8");

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
 		
 		// format types map
 		formatTypesMap.put("a", "Average of 5");
 		formatTypesMap.put("m", "Mean of 3");
 		formatTypesMap.put("1", "Best of 1");
 		formatTypesMap.put("2", "Best of 2");
 		formatTypesMap.put("3", "Best of 3");
 		
 		// round types map
 		roundTypesMap.put("0", "Qualification round");
 		roundTypesMap.put("1", "First round");
 		roundTypesMap.put("d", "Combined First");
 		roundTypesMap.put("2", "Second round");
 		roundTypesMap.put("3", "Semi Final");
 		roundTypesMap.put("c", "Combined Final"); 
 		roundTypesMap.put("f", "Final");
 		
 		// number of groups map
 		for (int i=1; i<=25; i++) {
 			numberOfGroupsMap.put(i, "" + i);
 		}
 		
 		// unit size map
 		unitSizeMap.put("2x2", 11);
 		unitSizeMap.put("3x3", 11);
 		unitSizeMap.put("4x4", 8);
 		unitSizeMap.put("5x5", 8);
 		unitSizeMap.put("6x6", 7);
 		unitSizeMap.put("7x7", 7);
 		unitSizeMap.put("fm", 11);
 		unitSizeMap.put("oh", 11);
 		unitSizeMap.put("bf", 11);
 		unitSizeMap.put("bf4", 8);
 		unitSizeMap.put("bf5", 8);
 		unitSizeMap.put("feet", 11);
 		unitSizeMap.put("clk", 120);
 		unitSizeMap.put("minx", 25);
 		unitSizeMap.put("sq1", 15);
 		unitSizeMap.put("pyr", 20);
 		unitSizeMap.put("mbf", 11);
 	}
 	
 	/**
 	 * @param name
 	 * @param format
 	 * @return
 	 */
 	private Event setupEvent(String name, String format, String timeFormat) {
 		Event event = new Event();
 		event.setFormat(format);
 		event.setTimeFormat(timeFormat);
 		event.setName(name);
 		return event;
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
 	 * @return the xsltTemplate
 	 */
 	public String getXsltTemplate() {
 		return xsltTemplate;
 	}
 
 	/**
 	 * @param xsltTemplate the xsltTemplate to set
 	 */
 	public void setXsltTemplate(String xsltTemplate) {
 		this.xsltTemplate = xsltTemplate;
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
      * @return
      */
     public String getCsvFileEncoding() {
         return csvFileEncoding;
     }
 
     /**
      * @param csvFileEncoding
      */
     public void setCsvFileEncoding(String csvFileEncoding) {
         this.csvFileEncoding = csvFileEncoding;
     }
 
     /**
      * @return
      */
     public List<String> getCsvFileEncodings() {
         return csvFileEncodings;
     }
 
     /**
      * @return
      */
     public boolean isCsvConvert() {
         return csvConvert;
     }
 
     /**
      * @param csvConvert
      */
     public void setCsvConvert(boolean csvConvert) {
         this.csvConvert = csvConvert;
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
 	 * @return the group1
 	 */
 	public List<Integer> getGroup1() {
 		return group1;
 	}
 
 	/**
 	 * @param group1 the group1 to set
 	 */
 	public void setGroup1(List<Integer> group1) {
 		this.group1 = group1;
 	}
 
 	/**
 	 * @return the group2
 	 */
 	public List<Integer> getGroup2() {
 		return group2;
 	}
 
 	/**
 	 * @param group2 the group2 to set
 	 */
 	public void setGroup2(List<Integer> group2) {
 		this.group2 = group2;
 	}
 
 	/**
 	 * @return the group3
 	 */
 	public List<Integer> getGroup3() {
 		return group3;
 	}
 
 	/**
 	 * @param group3 the group3 to set
 	 */
 	public void setGroup3(List<Integer> group3) {
 		this.group3 = group3;
 	}
 
 	/**
 	 * @return the group4
 	 */
 	public List<Integer> getGroup4() {
 		return group4;
 	}
 
 	/**
 	 * @param group4 the group4 to set
 	 */
 	public void setGroup4(List<Integer> group4) {
 		this.group4 = group4;
 	}
 
 	/**
 	 * @param includeExtraScrambles the includeExtraScrambles to set
 	 */
 	public void setIncludeExtraScrambles(boolean includeExtraScrambles) {
 		this.includeExtraScrambles = includeExtraScrambles;
 	}
 
 	/**
 	 * @return the includeExtraScrambles
 	 */
 	public boolean isIncludeExtraScrambles() {
 		return includeExtraScrambles;
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
 		return "attachment; filename=" + getCompetition().getCompetitionId() + ".pdf";
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
 			setRound1(new ArrayList<String>());
 			setRound2(new ArrayList<String>());
 			setRound3(new ArrayList<String>());
 			setRound4(new ArrayList<String>());
 			setGroup1(new ArrayList<Integer>());
 			setGroup2(new ArrayList<Integer>());
 			setGroup3(new ArrayList<Integer>());
 			setGroup4(new ArrayList<Integer>());
 			setIncludeExtraScrambles(true);
 			supportedEvents.clear();
 			
 			Competition competition = new Competition();
 			competition.setCompetitionId(competitionTemplate.getCompetitionId());
 			competition.setName(competitionTemplate.getName());
 			
 			// parse csv file
 			try {
 				// convert the CSV file
                 CSVReader reader;
                 if (isCsvConvert()) {
                     StringWriter sw = new StringWriter();
                     csvConverter.compTool2Wca(new InputStreamReader(new FileInputStream(csv), getCsvFileEncoding()), sw);
                     reader = new CSVReader(new StringReader(sw.toString()), ',');
                     setCsvConvert(false);
                 } else {
                     reader = new CSVReader(new InputStreamReader(new FileInputStream(csv), getCsvFileEncoding()), ',');
                 }
                setCsvFileEncoding("ISO-8859-1");
 				List<String[]> csvLines = reader.readAll();
 				// first row which includes event names
 				List<Event> events = parseEvents(csvLines.remove(0));
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
 			    	if (!"magic".equals(eventName) && !"mmagic".equals(eventName)) {
 			    		supportedEvents.put(i, eventName);
 			    	}
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
 					    		String event = getEventName(i+7);
 					    		if (event != null) {
 					    			Method method = registeredEvents.getClass().getMethod("setSignedUpFor" + event, boolean.class);
 					    			method.invoke(registeredEvents, "1".equals(eventName));
 					    		}
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
 		competitor.setWcaId(line[3]);
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
 			String eventName = getEventName(idx);
 			Event event = new Event();
 			event.setName(eventName);
 			event.setFormat(getEventFormat(idx));
 			event.setTimeFormat(getTimeFormat(idx));
 			events.add(event);
 		}
 		return events;
 	}
 	
 	/**
 	 * @param i
 	 * @return
 	 */
 	private String getEventName(int i) {
 		Event event = eventNamesMap.get(supportedEvents.get(i));
 		if (event == null)
 			return null;
 		else 
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
 	public Map<String, String> getFormatTypesMap() {
 		return formatTypesMap;
 	}
 	
 	/**
 	 * @return
 	 */
 	public Map<String, String> getRoundTypesMap() {
 		return roundTypesMap;
 	}
 	
 	/**
 	 * @return
 	 */
 	public Map<Integer, String> getNumberOfGroupsMap() {
 		return numberOfGroupsMap;
 	}
 	
 	/**
 	 * @return
 	 */
 	public String generateScrambles() {
 		if (!getFormats().isEmpty() && !getRound1().isEmpty() && !getGroup1().isEmpty()) {
 			try {
 				// root element
 				Document doc = docImplementation.createDocument(null, "scrambles", null);
 				Element rootElement = doc.getDocumentElement();
 				rootElement.setAttribute("competitionId", getCompetition().getCompetitionId());
 				
 				// competition data
 				Element competitionName = doc.createElement("competitionName");
 				competitionName.appendChild(doc.createTextNode(getCompetition().getName()));
 				rootElement.appendChild(competitionName);
 				
 				// generate xml for each event and round/group
 				Element events = doc.createElement("events");
 				int i = 0;
 				for (Event event : getCompetition().getEvents()) {
 					if ((!round1.isEmpty() && !"-1".equals(round1.get(i))) && (!group1.isEmpty() && group1.get(i) != null && group1.get(i) > 0)) { // only include events that has 1st rounds
 						int numberOfScrambles = getNumberOfScrambles(event);
 						for (int j=0; j<group1.get(i); j++) {
 							events.appendChild(generateXML(doc, event, numberOfScrambles, roundTypesMap.get(round1.get(i)), j+1));
 						}
 						
 						if ((!round2.isEmpty() && !"-1".equals(round2.get(i))) && (!group2.isEmpty() && group2.get(i) > 0)) {
 							for (int j=0; j<group2.get(i); j++) {
 								events.appendChild(generateXML(doc, event, numberOfScrambles, roundTypesMap.get(round2.get(i)), j+1));
 							}
 						}
 						if ((!round3.isEmpty() && !"-1".equals(round3.get(i))) && (!group3.isEmpty() && group3.get(i) > 0)) {
 							for (int j=0; j<group3.get(i); j++) {
 								events.appendChild(generateXML(doc, event, numberOfScrambles, roundTypesMap.get(round3.get(i)), j+1));
 							}
 						}
 						if ((!round4.isEmpty() && !"-1".equals(round4.get(i))) && (!group4.isEmpty() && group4.get(i) > 0)) {
 							for (int j=0; j<group4.get(i); j++) {
 								events.appendChild(generateXML(doc, event, numberOfScrambles, roundTypesMap.get(round4.get(i)), j+1));
 							}
 						}
 						
 						// do we need extra scrambles?
 						boolean fm = Event.TimeFormat.NUMBER.getValue().equals(event.getTimeFormat());
 						boolean mbf = Event.TimeFormat.MULTI_BLD.getValue().equals(event.getTimeFormat());
 						boolean bf5 = "bf5".equalsIgnoreCase(event.getName());
 						if (isIncludeExtraScrambles() && !fm && !mbf && !bf5) {
 							events.appendChild(generateXML(doc, event, 5, "Extra", 1)); // let's just share extra scrambles for all groups
 						}
 					}
 					
 					// loop
 					i++;
 				}
 				rootElement.appendChild(events);
 				
 				if (doc.hasChildNodes()) {
 					// output generated scrambles
 					log.info("Ouputting generated scrambles");
 					out = new ByteArrayOutputStream();
 					uriResolver = new ServletContextURIResolver(ServletActionContext.getServletContext());
 					fopFactory.setURIResolver(uriResolver);
 					tFactory.setURIResolver(uriResolver);
 					if (cachedXSLT == null) {
 						Source xsltSrc = uriResolver.resolve("servlet-context:" + xsltTemplate, null);
 						cachedXSLT = tFactory.newTemplates(xsltSrc);
 					}
 					Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
 					Result res = new SAXResult(fop.getDefaultHandler());
 					Transformer transformer = cachedXSLT.newTransformer();
 					transformer.setURIResolver(uriResolver);
 					transformer.transform(new DOMSource(doc), res);
 					out.close();
 					return Action.SUCCESS;
 				} else {
 					log.error("Error generating XML!");
 					return Action.ERROR;
 				}
 			} catch (IOException e) {
 				log.error("Error generating scrambles", e);
 			} catch (Exception e) {
 				log.error("[{}] " + e.getLocalizedMessage(), e);
 				throw new RuntimeException("Could not include competitors in this sheet.", e);
 			}
 			return Action.ERROR;
 		} else {
 			return Action.INPUT;
 		}
 	}
 
 	private int getNumberOfScrambles(Event event) {
 		int numberOfScrambles = 5;
 		try {
 			// print the same scramble for all competitors for fewest moves
 			if (Event.TimeFormat.NUMBER.getValue().equals(event.getTimeFormat())) {
 				numberOfScrambles = competition.getCompetitorsByEvent(event).size();
 			// multi bld
 			} else if (Event.TimeFormat.MULTI_BLD.getValue().equals(event.getTimeFormat())) {
 				numberOfScrambles = 24; // for now just use a high fixed number
 			// other events
 			} else {
 				if (Event.Format.AVERAGE.getValue().equals(event.getFormat())) {
 					numberOfScrambles = 5;
 				} else if (Event.Format.MEAN.getValue().equals(event.getFormat())) {
 					numberOfScrambles = 3;
 				} else if (Event.Format.BEST_OF_1.getValue().equals(event.getFormat())) {
 					numberOfScrambles = 1;
 				} else if (Event.Format.BEST_OF_2.getValue().equals(event.getFormat())) {
 					numberOfScrambles = 2;
 				} else if (Event.Format.BEST_OF_3.getValue().equals(event.getFormat())) {
 					numberOfScrambles = 3;
 				}
 			}
 		} catch (Exception e) {
 			log.error("[{}] " + e.getLocalizedMessage(), e);
 			throw new RuntimeException("Could not get number of scrambles for this event", e);
 		}
 		return numberOfScrambles;
 	}
 
 	private Element generateXML(Document doc, Event event, int numberOfScrambles, String roundName, int groupNumber) {
 		Element eventNode = doc.createElement("event");
 		eventNode.setAttribute("variant", event.getName().toLowerCase());
 		eventNode.setAttribute("unitSize", Integer.toString(getUnitSize(event)));
 		eventNode.setAttribute("numberOfScrambles", Integer.toString(numberOfScrambles));
 		eventNode.setAttribute("sameScramble", (Event.TimeFormat.NUMBER.getValue().equals(event.getTimeFormat()))? "true" : "false");
 		
 		Element eventName = doc.createElement("name");
 		eventName.appendChild(doc.createTextNode(getText("admin.scoresheet.eventname." + event.getName().toLowerCase())));
 		eventNode.appendChild(eventName);
 		
 		Element roundNode = doc.createElement("round");
 		roundNode.appendChild(doc.createTextNode(roundName));
 		eventNode.appendChild(roundNode);
 		
 		Element groupNode = doc.createElement("group");
 		groupNode.appendChild(doc.createTextNode(Integer.toString(groupNumber)));
 		eventNode.appendChild(groupNode);
 		
 		return eventNode;
 	}
 
 	private int getUnitSize(Event event) {
 		return unitSizeMap.get(event.getName().toLowerCase());
 	}
 }
