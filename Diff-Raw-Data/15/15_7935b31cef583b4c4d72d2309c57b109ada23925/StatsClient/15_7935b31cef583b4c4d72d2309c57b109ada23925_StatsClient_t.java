 package net.guto.steamer;
 
 import static javax.xml.xpath.XPathConstants.NODESET;
 import static javax.xml.xpath.XPathConstants.STRING;
 import static net.guto.steamer.Steamer.connect;
 import static net.guto.steamer.Steamer.getDocument;
 import static net.guto.steamer.Steamer.getIntValue;
 import static net.guto.steamer.Steamer.getLongValue;
 import static net.guto.steamer.Steamer.getNodeListValue;
 import static net.guto.steamer.Steamer.getStringValue;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class StatsClient {
 
 	private static enum StatsField implements Field {
 		VISIBILITY_STATE("/playerstats/visibilityState", STRING),
 		GAME_FRIENDLY_NAME("/playerstats/game/gameFriendlyName", STRING), GAME_NAME("/playerstats/game/gameName", STRING), GAME_LINK(
 				"/playerstats/game/gameLink",
 				STRING), GAME_ICON("/playerstats/game/gameIcon", STRING), GAME_LOGO("/playerstats/game/gameLogo", STRING), GAME_LOGO_SMALL(
 				"/playerstats/game/gameLogoSmall",
 				STRING), PLAYER_STEAM_ID64("playerstats/player/steamID64", STRING), PLAYER_CUSTOM_URL(
 				"/playerstats/player/customURL",
 				STRING), ACHIEVEMENTS("/playerstats/achievements/achievement | /playerstats/achievements/achievement/*", NODESET);
 
 		private XPathExpression xpath;
 		private final String xpathString;
 		private final QName dataType;
 
 		private StatsField(String xpathString, QName dataType) {
 			this.xpathString = xpathString;
 			this.dataType = dataType;
 			XPathFactory xpathFactory = XPathFactory.newInstance();
 			try {
 				xpath = xpathFactory.newXPath().compile(xpathString);
 			} catch (XPathExpressionException e) {
 				e.printStackTrace();
 			}
 		}
 
 		public String getXPathString() {
 			return xpathString;
 		}
 
 		public QName getDataType() {
 			return dataType;
 		}
 
 		@Override
 		public XPathExpression getXPath() {
 			return xpath;
 		}
 	}
 
 	public Stats getStats(String username, String game) {
 		return getStats(username, game, null);
 	}
	
	
 
 	protected Stats getStats(String username, String game, Document document) {
 		if (document == null) {
 			InputStream in = connect("http://"+Steamer.steamcommunity+"/id/" + username + "/stats/" + game + "?xml=1");
 			document = getDocument(in);
 		}
 		Stats stats = new Stats();
 		stats.visibilityState = getIntValue(StatsField.VISIBILITY_STATE, document);
 		stats.gameFriendlyName = getStringValue(StatsField.GAME_FRIENDLY_NAME, document);
 		stats.gameName = getStringValue(StatsField.GAME_NAME, document);
 		stats.gameLink = getStringValue(StatsField.GAME_LINK, document);
 		stats.gameIcon = getStringValue(StatsField.GAME_ICON, document);
 		stats.gameLogo = getStringValue(StatsField.GAME_LOGO, document);
 		stats.gameLogoSmall = getStringValue(StatsField.GAME_LOGO_SMALL, document);
 		stats.playerCustomURL = getStringValue(StatsField.PLAYER_CUSTOM_URL, document);
 		stats.steamID64 = getLongValue(StatsField.PLAYER_STEAM_ID64, document);
 		return stats;
 	}
 
 	List<Achievement> achievements;
 
 	public List<Achievement> getAchievements(String username, String game) {
 		return getAchievements(username, game, null);
 	}
 
 	protected List<Achievement> getAchievements(String username, String game, Document document) {
 		if (achievements == null) {
 			if (document == null) {
 				InputStream in = connect("http://"+Steamer.steamcommunity+"/id/" + username + "/stats/" + game + "?xml=1");
 				document = getDocument(in);
 			}
 			NodeList nodes = getNodeListValue(StatsField.ACHIEVEMENTS, document);
 			achievements = new ArrayList<Achievement>();
 			Achievement achievement = null;
 			for (int i = 0; i < nodes.getLength(); i++) {
 				Node node = nodes.item(i);
 				String nodeName = node.getNodeName();
 				if ("achievement".equals(nodeName)) {
 					achievement = new Achievement();
 					achievements.add(achievement);
 				} else if ("iconClosed".equals(nodeName)) {
 					achievement.iconClosed = node.getTextContent();
 				} else if ("iconOpen".equals(nodeName)) {
 					achievement.iconOpen = node.getTextContent();
 				} else if ("name".equals(nodeName)) {
 					achievement.name = node.getTextContent();
 				} else if ("apiname".equals(nodeName)) {
 					achievement.apiname = node.getTextContent();
 				} else if ("description".equals(nodeName)) {
 					achievement.description = node.getTextContent();
 				} else if ("unlockTimestamp".equals(nodeName)){
 					achievement.timestamp = new Date( Long.parseLong(node.getTextContent()) * 1000);
 				}
 			}
 		}
 		return achievements;
 	}
 }
