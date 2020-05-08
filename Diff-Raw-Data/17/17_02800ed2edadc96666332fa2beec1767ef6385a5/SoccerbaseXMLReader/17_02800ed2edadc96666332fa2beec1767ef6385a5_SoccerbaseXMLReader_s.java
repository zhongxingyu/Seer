 package uk.co.mindbadger.footballresults.reader;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class SoccerbaseXMLReader implements FootballResultsReader {
 	private XMLFileReader xmlFileReader;
 	private String rootDirectory;
 
 	@Override
 	public List<ParsedFixture> readFixturesForSeason(int seasonNumber) {
 		List<ParsedFixture> fixtures = new ArrayList<ParsedFixture>();
 
 		List<String> fullyQualifiedSeasonFileNames = xmlFileReader.getFilesInDirectory(rootDirectory + "\\" + seasonNumber);
 
 		for (String fileName : fullyQualifiedSeasonFileNames) {
 			try {
 				Document doc = xmlFileReader.readXMLFile(fileName);
 
 				Element rootElement = doc.getDocumentElement();
 
 				Calendar fixtureDate = convertDateStringToCalendar(rootElement.getAttribute("date"));
 
 				NodeList competitions = rootElement.getElementsByTagName("Competition");
 
 				for (int i = 0; i < competitions.getLength(); i++) {
 					Element competition = (Element) competitions.item(i);
 
 					Integer seasonId = Integer.parseInt(competition.getAttribute("seasonId")) + 1870;
 					if (seasonId != seasonNumber) {
						// TODO need a test for this scenario
 						throw new FootballResultsXMLException("Your xml file contains a season that is not in the correct folder");
 					}
 
 					Integer competitionId = Integer.parseInt(competition.getAttribute("competitionId"));
 					String competitionName = competition.getAttribute("competitionName");
 
 					NodeList games = competition.getElementsByTagName("Game");
 					for (int j = 0; j < games.getLength(); j++) {
 						Element game = (Element) games.item(j);
 
 						Integer fixtureId = Integer.parseInt(game.getAttribute("gameId"));
 						Integer homeTeamId = Integer.parseInt(game.getAttribute("homeTeamId"));
 						Integer awayTeamId = Integer.parseInt(game.getAttribute("awayTeamId"));
 						String homeTeamName = game.getAttribute("homeTeamName");
 						String awayTeamName = game.getAttribute("awayTeamName");
 
 						ParsedFixture fixture = new ParsedFixture();
 						fixture.setFixtureId(fixtureId);
 						fixture.setSeasonId(seasonId);
 						fixture.setDivisionId(competitionId);
 						fixture.setDivisionName(competitionName);
 						fixture.setFixtureDate(fixtureDate);
 						fixture.setHomeTeamId(homeTeamId);
 						fixture.setHomeTeamName(homeTeamName);
 						fixture.setAwayTeamId(awayTeamId);
 						fixture.setAwayTeamName(awayTeamName);
 						
 						fixtures.add(fixture);
 
 						NodeList scores = game.getElementsByTagName("Score");
 						Element score = (Element) scores.item(0);
 						if (score != null) {
 							fixture.setHomeGoals(Integer.parseInt(score.getAttribute("homeGoals")));
 							fixture.setAwayGoals(Integer.parseInt(score.getAttribute("awayGoals")));
 						}
 					}
 				}
 			} catch (Exception e) {
 				throw new FootballResultsXMLException(e);
 			}
 		}
 
 		return fixtures;
 	}
 
 	private Calendar convertDateStringToCalendar(String dateString) {
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 		try {
 			cal.setTime(sdf.parse(dateString));
 			return cal;
 		} catch (ParseException e) {
 			// TODO Need to add a test to deal with being unable to parse the date
 			throw new FootballResultsXMLException(e);
 		}
 	}
 
 	public XMLFileReader getXmlFileReader() {
 		return xmlFileReader;
 	}
 
 	public void setXmlFileReader(XMLFileReader xmlFileReader) {
 		this.xmlFileReader = xmlFileReader;
 	}
 
 	public String getRootDirectory() {
 		return rootDirectory;
 	}
 
 	public void setRootDirectory(String rootDirectory) {
 		this.rootDirectory = rootDirectory;
 	}
 
 }
