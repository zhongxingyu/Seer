 package ho.module.ifa;
 
 import ho.core.db.DBManager;
 import ho.core.file.xml.XMLManager;
 import ho.core.gui.HOMainFrame;
 import ho.core.net.MyConnector;
 import ho.core.net.login.LoginWaitDialog;
 import ho.core.util.DateTimeUtils;
 import ho.core.util.HOLogger;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.JWindow;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 public class PluginIfaUtils {
 	public static final boolean HOME = true;
 	public static final boolean AWAY = false;
 
 	private static Document getTeamDetails(int teamID) throws Exception {
 		String teamDetails = MyConnector.instance().getTeamdetails(teamID);
 		return XMLManager.parseString(teamDetails);
 	}
 
 	private static String parseXmlElement(Document doc, String element,	int i, String eleText) {
 		String value = "";
 		try {
 			Element ele = doc.getDocumentElement();
 			Element tmpEle = (Element) ele.getElementsByTagName(eleText)
 					.item(i);
 			tmpEle = (Element) tmpEle.getElementsByTagName(element).item(0);
 			value = XMLManager.getFirstChildNodeValue(
 					tmpEle);
 		} catch (Exception e) {
 			HOLogger.instance().error(PluginIfaUtils.class, e);
 		}
 		return value;
 	}
 
 	static boolean updateMatchesTable() {
 		JWindow waitWindow = new LoginWaitDialog(HOMainFrame.instance());
 		try {
 			waitWindow.setVisible(true);
 
			Date from = DateHelper.getDate(DBManager.instance().getLastIFAMatchDate());
 			try {
 				List<Date[]> times = getTimeIntervalsForRetrieval(from);
 				for (Iterator<Date[]> i = times.iterator(); i.hasNext();) {
 					Date[] fromTo = i.next();
 					insertMatches(fromTo[0], fromTo[1]);
 				}
 			} catch (Exception e) {
 				insertMatches(from, new Date());
 			}
 			waitWindow.setVisible(false);
 		} catch (Exception e) {
 			waitWindow.setVisible(false);
 			HOLogger.instance().error(PluginIfaUtils.class, e);
 			return false;
 		}
 		return true;
 	}
 
 	private static void insertMatches(Date from, Date to) throws Exception {
 		StringBuilder errors = new StringBuilder();
 		String matchDate = null;
 		String matchesArchive = MyConnector.instance().getMatchesArchive(0,from,to);
 		Document doc =  XMLManager.parseString(matchesArchive);
 		
 		int matchesCount = ((Element) doc.getDocumentElement()
 				.getElementsByTagName("MatchList").item(0))
 				.getElementsByTagName("Match").getLength();
 		for (int i = 0; i < matchesCount; i++) {
 			IfaMatch match = new IfaMatch();
 			
 			int matchType = Integer.parseInt(parseXmlElement(doc, "MatchType",i, "Match"));
 			matchDate = parseXmlElement(doc, "MatchDate", i, "Match");
 			if ((matchType == 8) || (matchType == 9)) {
 				int homeTeamID = Integer.parseInt(parseXmlElement(doc,"HomeTeamID", i, "HomeTeam"));
 				int awayTeamID = Integer.parseInt(parseXmlElement(doc,"AwayTeamID", i, "AwayTeam"));
 				int matchID = Integer.parseInt(parseXmlElement(doc, "MatchID",i, "Match"));
 				int homeTeamGoals = Integer.parseInt(parseXmlElement(doc,"HomeGoals", i, "Match"));
 				int awayTeamGoals = Integer.parseInt(parseXmlElement(doc,"AwayGoals", i, "Match"));
 				try {
 					Document docHomeTeam = getTeamDetails(homeTeamID);
 					Document docAwayTeam = getTeamDetails(awayTeamID);
 					
 					if ((docHomeTeam == null) || (docAwayTeam == null)
 							|| (docHomeTeam.getDocumentElement() == null)
 							|| (docAwayTeam.getDocumentElement() == null)) {
 						errors.append("Error 2 getting data for match "
 								+ matchID + " (" + matchDate + " / HomeTeam "
 								+ homeTeamID + " vs. AwayTeam " + awayTeamID
 								+ ")<br>");
 					} else {
 						int homeLeagueIndex = Integer.parseInt(parseXmlElement(docHomeTeam, "LeagueID", 0, "League"));
 						int awayLeagueIndex = Integer.parseInt(parseXmlElement(docAwayTeam, "LeagueID", 0, "League"));
 						
 						
 						match.setMatchId(matchID);
 						match.setPlayedDateString(matchDate);
 						match.setHomeLeagueId(homeLeagueIndex);
 						match.setHomeTeamId(homeTeamID);
 						match.setAwayLeagueId(awayLeagueIndex);
 						match.setAwayTeamId(awayTeamID);
 						match.setHomeTeamGoals(homeTeamGoals);
 						match.setAwayTeamGoals(awayTeamGoals);
 						
 						DBManager.instance().insertIFAMatch(match);
 					}
 				} catch (Exception e) {
 					errors.append("Error 1 getting data for match " + matchID
 							+ " (" + matchDate + " / HomeTeam " + homeTeamID
 							+ " vs. AwayTeam " + awayTeamID + ")<br>");
 				}
 			}
 
 		}
 
 		if (errors.length() > 0) {
 			HOLogger.instance().error(PluginIfaUtils.class,errors.toString());
 		}
 
 		if (matchesCount == 50) {
 			insertMatches(DateHelper.getDate(matchDate), to);
 		}
 	}
 
 	private static List<Date[]> getTimeIntervalsForRetrieval(Date from) {
 		List<Date[]> ret = new ArrayList<Date[]>();
 		Date start = DateTimeUtils.getDateWithMinTime(from);
 		Calendar end = new GregorianCalendar();
 		end.setLenient(true);
 		end.add(5, 1);
 
 		Calendar tmpF = new GregorianCalendar();
 		tmpF.setTime(start);
 		Calendar tmpT = new GregorianCalendar();
 		tmpT.setTime(tmpF.getTime());
 		tmpF.setLenient(true);
 		tmpT.setLenient(true);
 		while (tmpT.before(end)) {
 			tmpT.add(2, 2);
 			if (tmpT.after(end)) {
 				tmpT = end;
 			}
 			ret.add(new Date[] { tmpF.getTime(), tmpT.getTime() });
 			tmpF.setTime(tmpT.getTime());
 		}
 		return ret;
 	}
 }
