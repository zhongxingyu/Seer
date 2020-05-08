 package singleplayer;
 
 import java.awt.Point;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import main.GameConstants;
 import map.Map;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.JDOMException;
 import org.jdom2.input.SAXBuilder;
 
 public class CampaignReader {
 	private Document campaignXML;
 	private Element campaignRoot;
 
 	public CampaignReader(String mn) {
 		try {
 			campaignXML = new SAXBuilder().build(GameConstants.CAMPAIGNS_DIR
 					+ mn + ".xml");
 		} catch (JDOMException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		campaignRoot = campaignXML.getRootElement();
 	}
 
 	public String getHeader(String v) {
 		return campaignRoot.getChild("header").getChildText(v);
 	}
 
 	public Campaign readCampaignFromFile() {
 
 		int levelNumber = Integer.parseInt(campaignRoot.getChild("header")
 				.getChildText("numOfLevels"));
 
 		ArrayList<ArrayList<Campaign.StoryMapContainer>> levels = new ArrayList<ArrayList<Campaign.StoryMapContainer>>();
 		for (int i = 0; i < levelNumber; i++) {
 			levels.add(new ArrayList<Campaign.StoryMapContainer>());
 		}
 		List<Element> maps = campaignRoot.getChild("maps").getChildren();
 
 		for (Element el : maps) {
			String[] mapIntro = el.getChildText("intro").split("#");
 			String mapName = el.getChildText("name");
 			int index = Integer.parseInt(el.getChildText("sequence"));
 			levels.get(index).add(
 					new Campaign.StoryMapContainer(mapName, mapIntro));
 		}
 
 		Element worldMapElement = campaignRoot.getChild("worldmap");
 		String worldMapName = worldMapElement.getChildText("name");
 		List<Element> coords = campaignRoot.getChild("worldmap")
 				.getChild("coordinates").getChildren();
 		Point[] coordinates = new Point[coords.size()];
 		for (int i = 0; i < coords.size(); i++) {
 			Element el = coords.get(i);
 			int x = Integer.parseInt(el.getChildText("cx"));
 			int y = Integer.parseInt(el.getChildText("cy"));
 			coordinates[i] = new Point(x, y);
 		}
 
 		WorldMap worldMap = new WorldMap(coordinates, new Map(worldMapName));
 		Campaign campaign = new Campaign(levels, worldMap);
 		return campaign;
 	}
 
 	public static void main(String[] args) {
		new CampaignReader("campaign1").readCampaignFromFile();
 	}
 }
