 package org.aimas.craftingquest.core;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.aimas.craftingquest.core.energyreplenishmodels.ReplenishType;
 import org.aimas.craftingquest.mapeditor.MapCell;
 import org.aimas.craftingquest.state.Blueprint;
 import org.aimas.craftingquest.state.CellState;
 import org.aimas.craftingquest.state.GameState;
 import org.aimas.craftingquest.state.MapState;
 import org.aimas.craftingquest.state.Point2i;
 import org.aimas.craftingquest.state.objects.CraftedObjectType;
 import org.aimas.craftingquest.state.resources.ResourceType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class GamePolicy {
 
 	/* server configuration */
 	public static String servername = "CraftingQuest";
     public static int serverPort = 1198;
 	public static int connectWaitTime = 5000;
     public static int initializationWaitTime = 3000;
 	
 	/* number of players */
     public static int maxPlayers = 4;
 	public static int nrPlayers = 2;
	public static int nrPlayerUnits = 3;
 
 	/* durations associated with a player */
 	public static long playerActionTime = 1000;
 	public static long playerLateTime = 0;
 	public static long playerTotalTime;
 
 	/* total duration of all players during a round */
 	public static long PLAYERSTotalTime;
 
 	/* total duration of server update time during a round */
 	public static long updateTime = 100;
 
 	/* duration of an entire round */
 	public static long roundTime;
 
 	/* number of turns */
 	public static int lastTurn = 150;
 	
 	/* game map */
 	public static String mapName = "map_cq3_v1.cqm";
 	public static MapState map;
 	
 	/* initial player positions */
 	public static HashMap<Integer, Point2i> initialPlayerPositions;
 	
 	/* general */
	public static Point2i mapsize = new Point2i(80, 80);
 	public static int moveBase = 20;
 	public static int initialTeamGold = 200;
 	public static int initialUnitMaxLife = 200;
 	public static ReplenishType energyReplenishModel = ReplenishType.FullReplenish;
 	public static int movePenaltyWeight = 50;
 	
 	/* action costs */
 	public static int pickupCost = 5;
 	public static int dropCost = 0;
 	public static int scanCost = 10;
 	public static int digCost = 10;
 	public static int buildCost = 0;
 	public static int scanRadius = 7;
 	public static int sightRadius = 5;
 	public static int placeTowerCost = 50;
 	public static int placeTrapCost = 40;
 
 	/* tower related */
 	public static int towerBaseRadius = 4;
 	public static int towerBaseDrain = 100;
 	public static int towerBaseEnergy = 250;
 	
 	public static int leatherWeight = 1;
 	public static int stoneWeight = 1;
 	public static int woodWeight = 1;
 	public static int bronzeWeight = 1;
 	public static int titaniumWeight = 1;
 	public static int ironWeight = 1;
 	
 	/* leveling */
 	public static int maxLevels = 3;
 	public static int[] levelIncrease;
 	
 	/* scoring (and bonuses) */
 	public static int killingSpreeThreshold = 5;
 	public static int killingSpreeBonus = 15;
 	public static int firstBloodBonus = 10;
 	public static int buildTowerBonus = 20;
 	
 	/* blueprints */
 	public static List<Blueprint> blueprints;
 	
 	public static void initScenario() {
 		
 		Document paramDoc = GameUtils.readXMLDocument("GamePolicy.xml");
 		MapReader.readMap("mapdata");
 		
 		mapsize = new Point2i(MapReader.mapWidth, MapReader.mapHeight);
 		mapName = MapReader.mapName;
 		
 //		if (MapReader.mapWidth >= 60 && MapReader.mapWidth < 70) {
 //			lastTurn = 160;
 //		}
 //		if (MapReader.mapWidth >= 70 && MapReader.mapWidth < 80) {
 //			lastTurn = 180;
 //		}
 //		if (MapReader.mapWidth >= 80) {
 //			lastTurn = 200;
 //		}
 		
 		map = new MapState();
 		map.cells = MapReader.cells;
 		map.mapHeight = MapReader.mapHeight;
 		map.mapWidth = MapReader.mapWidth;
 		
 		readParametersFrom(paramDoc);
 		
 		playerTotalTime = playerActionTime + playerLateTime;
 		PLAYERSTotalTime = nrPlayers * playerTotalTime;
 		roundTime = PLAYERSTotalTime + updateTime;
 	}
 	
 	
 	public static void readParametersFrom(Document doc) {
 		Element root = (Element)doc.getDocumentElement();
 		Element parametersNode = (Element)root.getElementsByTagName("parameters").item(0);
 		Element blueprintsNode = (Element)root.getElementsByTagName("blueprints").item(0);
 		//Element ruleNode = (Element)root.getElementsByTagName("rules").item(0);
 		
 		readServerParameters(parametersNode);
 		readScenarioParameters(parametersNode);
 		readScenarioBlueprints(blueprintsNode);
 		
 		//readScenarioRules(ruleNode);
 	}
 	
 	private static void readServerParameters(Element parametersNode) {
 		maxPlayers = Integer.parseInt(parametersNode.getElementsByTagName("maxPlayers").item(0).getTextContent());
 		nrPlayers = Integer.parseInt(parametersNode.getElementsByTagName("nrPlayers").item(0).getTextContent());
 		playerActionTime = Integer.parseInt(parametersNode.getElementsByTagName("playerActionTime").item(0).getTextContent());
 		playerLateTime = Integer.parseInt(parametersNode.getElementsByTagName("playerLateTime").item(0).getTextContent());
 		updateTime = Integer.parseInt(parametersNode.getElementsByTagName("updateTime").item(0).getTextContent());
 		connectWaitTime = Integer.parseInt(parametersNode.getElementsByTagName("connectWaitTime").item(0).getTextContent());
 		lastTurn = Integer.parseInt(parametersNode.getElementsByTagName("nrTurns").item(0).getTextContent());
 		nrPlayerUnits = Integer.parseInt(parametersNode.getElementsByTagName("nrPlayerUnits").item(0).getTextContent());
 		movePenaltyWeight = Integer.parseInt(parametersNode.getElementsByTagName("movePenaltyWeight").item(0).getTextContent());
 		mapName = parametersNode.getElementsByTagName("mapName").item(0).getTextContent();
 	}
 	
 	private static void readScenarioParameters(Element parametersNode) {
 		initialUnitMaxLife = Integer.parseInt(parametersNode.getElementsByTagName("unitEnergy").item(0).getTextContent());
 		initialTeamGold = Integer.parseInt(parametersNode.getElementsByTagName("initialTeamGold").item(0).getTextContent());
 		
 		moveBase = Integer.parseInt(parametersNode.getElementsByTagName("moveBaseCost").item(0).getTextContent());
 		pickupCost = Integer.parseInt(parametersNode.getElementsByTagName("pickupCost").item(0).getTextContent());
 		dropCost = Integer.parseInt(parametersNode.getElementsByTagName("dropCost").item(0).getTextContent());
 		digCost = Integer.parseInt(parametersNode.getElementsByTagName("digCost").item(0).getTextContent());
 		buildCost = Integer.parseInt(parametersNode.getElementsByTagName("buildCost").item(0).getTextContent());
 		
 		placeTowerCost = Integer.parseInt(parametersNode.getElementsByTagName("placeTowerCost").item(0).getTextContent());
 		sightRadius = Integer.parseInt(parametersNode.getElementsByTagName("sightRadius").item(0).getTextContent());
 		towerBaseRadius = Integer.parseInt(parametersNode.getElementsByTagName("towerBaseRadius").item(0).getTextContent());
 		towerBaseDrain = Integer.parseInt(parametersNode.getElementsByTagName("towerBaseDrain").item(0).getTextContent());
 		towerBaseEnergy = Integer.parseInt(parametersNode.getElementsByTagName("towerBaseEnergy").item(0).getTextContent());
 		
 		// level increments
 		Element levelsElement = (Element)parametersNode.getElementsByTagName("levels").item(0);
 		maxLevels = Integer.parseInt(levelsElement.getAttribute("max"));
 		levelIncrease = new int[maxLevels];
 		
 		NodeList levelInfoList = levelsElement.getElementsByTagName("level");
 		for (int i = 0; i < levelInfoList.getLength(); i++) {
 			Element level = (Element)levelInfoList.item(i);
 			int levelIndex = Integer.parseInt(level.getAttribute("value"));
 			int levelIncrement = Integer.parseInt(level.getAttribute("percentage"));
 			
 			levelIncrease[levelIndex - 1] = levelIncrement;
 		}
 		
 		// energy repleneshing
 		String replenishType = parametersNode.getElementsByTagName("energyReplenishModel").item(0).getTextContent();
 		if (replenishType.equals("FullReplenish"))
 			energyReplenishModel = ReplenishType.FullReplenish;
 		else if (replenishType.equals("ExponentialReplenish"))
 			energyReplenishModel = ReplenishType.ExponentialReplenish;
 		else
 			energyReplenishModel = null;
 		placeTrapCost = Integer.parseInt(parametersNode.getElementsByTagName("placeTrapCost").item(0).getTextContent());
 		
 		killingSpreeThreshold = Integer.parseInt(parametersNode.getElementsByTagName("killingSpreeThreshold").item(0).getTextContent());
 		killingSpreeBonus = Integer.parseInt(parametersNode.getElementsByTagName("killingSpreeBonus").item(0).getTextContent());
 		firstBloodBonus = Integer.parseInt(parametersNode.getElementsByTagName("firstBloodBonus").item(0).getTextContent());
 		buildTowerBonus = Integer.parseInt(parametersNode.getElementsByTagName("buildTowerBonus").item(0).getTextContent());
 		
 		initialPlayerPositions = new HashMap<Integer, Point2i>();
 		NodeList initialPlayerPositionNodeList = parametersNode.getElementsByTagName("initialPlayerPositions").item(0).getChildNodes();
 		for (int i = 0; i < initialPlayerPositionNodeList.getLength(); i++) {
 			Node initialPlayerPositionNode = initialPlayerPositionNodeList.item(i);
 			
 			int playerID = Integer.parseInt(initialPlayerPositionNode.getAttributes().getNamedItem("playerID").getTextContent());
 			int x = Integer.parseInt(initialPlayerPositionNode.getAttributes().getNamedItem("x").getTextContent());
 			int y = Integer.parseInt(initialPlayerPositionNode.getAttributes().getNamedItem("y").getTextContent());
 			initialPlayerPositions.put(playerID, new Point2i(x, y));
 		}
 		System.out.println("Initial player positions: " + initialPlayerPositions);
 	}
 	
 	private static void readScenarioBlueprints(Element blueprintsNode) {
 		blueprints = new ArrayList<Blueprint>();
 		
 		NodeList blueprintNodeList = blueprintsNode.getElementsByTagName("blueprint");
 		System.out.println(blueprintNodeList.getLength() + " blueprints found in GamePolicy.xml");
 		for (int i = 0; i < blueprintNodeList.getLength(); i++) {
 			Node blueprintNode = blueprintNodeList.item(i);
 			
 			// Get object type
 			CraftedObjectType craftedObjectType = CraftedObjectType.valueOf(blueprintNode.getAttributes().getNamedItem("type").getTextContent().toUpperCase());
 			
 			// Get object level
 			int level = Integer.parseInt(blueprintNode.getAttributes().getNamedItem("level").getTextContent());
 			
 			// Get object upgradeCost (if available)
 			int upgradeCost = 0;
 			Node upgradeCostNode = blueprintNode.getAttributes().getNamedItem("upgradeCost");
 			if(upgradeCostNode != null)
 				Integer.parseInt(blueprintNode.getAttributes().getNamedItem("upgradeCost").getTextContent());
 			
 			// Get object weight (if available)
 			int weight = 0;
 			Node weightNode = blueprintNode.getAttributes().getNamedItem("weight");
 			if(weightNode != null)
 				weight = Integer.parseInt(weightNode.getTextContent());
 			
 			// Get the object's required resources
 			HashMap<ResourceType, Integer> requiredResources = new HashMap<ResourceType, Integer>();
 			NodeList resourceNodeList = blueprintNode.getChildNodes();
 			for (int j = 0; j < resourceNodeList.getLength(); j++) {
 				ResourceType resourceType = ResourceType.valueOf(resourceNodeList.item(j).getAttributes().getNamedItem("type").getTextContent().toUpperCase());
 				int quantity = Integer.parseInt(resourceNodeList.item(j).getAttributes().getNamedItem("quantity").getTextContent());
 				requiredResources.put(resourceType, quantity);
 			}
 			
 			Blueprint readBlueprint = new Blueprint(craftedObjectType, level, maxLevels, weight, requiredResources, upgradeCost);
 			
 			if (craftedObjectType == CraftedObjectType.TOWER) {
 				int towerInitialStrength = towerBaseEnergy * (100 + levelIncrease[level - 1]) / 100;
 				int towerRange = towerBaseRadius * (100 + levelIncrease[level - 1]) / 100;
 				int towerDrain = towerBaseDrain * (100 + levelIncrease[level - 1]) / 100;
 				
 				readBlueprint.setSpecificValues(towerInitialStrength, towerRange, towerDrain, 0, 0);
 			}
 			else if (craftedObjectType == CraftedObjectType.SWORD) {
 				int swordAttack = levelIncrease[level - 1];
 				readBlueprint.setSpecificValues(0, 0, 0, swordAttack, 0);
 			}
 			else if (craftedObjectType == CraftedObjectType.ARMOUR) {
 				int armourDefense = levelIncrease[level - 1];
 				readBlueprint.setSpecificValues(0, 0, 0, 0, armourDefense);
 			}
 			
 			blueprints.add(readBlueprint);
 		}
 	}
 	
 	public static void saveMapResources(GameState state) {
 		MapResourceWriter.saveMapResources(mapName, state); 
 	}
 
 }
 
 class MapResourceWriter {
 	public static void saveMapResources(String mapName, GameState game) {
 		String mapFile = "maps/" + mapName + ".cqres";
 		
 		try {
 			FileOutputStream fout = new FileOutputStream(mapFile);
 			ObjectOutputStream objout = new ObjectOutputStream (fout);
 			
 			HashMap<Point2i, HashMap<ResourceType, Integer>> cellResources = new HashMap<Point2i, HashMap<ResourceType,Integer>>();
 			
 			for (int i = 0; i < game.map.mapHeight; i++) {
 				for (int j = 0; j < game.map.mapWidth; j++) {
 					cellResources.put(game.map.cells[i][j].pos, new HashMap<ResourceType, Integer>(game.map.cells[i][j].resources));
 				}
 			}
 			
 			// write game data
 			objout.writeObject(game);
 			
 			// write resource and scan data
 			objout.writeObject(cellResources);
 			
 			objout.flush();
 			objout.close();
 			fout.close();
 		}catch(FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
 
 class MapReader {
 	public static int mapWidth;
 	public static int mapHeight;
 	
 	public static String mapName = "map_cq3_v1.cqm"; 
 	public static CellState[][] cells;
 	
 	public static void readMap(String mapDatafilename) {
 		
 		BufferedReader reader = null;
 		try {
 			reader = new BufferedReader(new FileReader(mapDatafilename));
 			mapName = reader.readLine().trim();
 			System.out.println("Read map name from \"mapdata\" file.");
 			reader.close();
 		} catch(IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (reader != null) {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		String mapFile = "maps/" + mapName;
 		
 		FileInputStream fis = null;
 		ObjectInputStream objin = null;
 		
 		try {
 			fis = new FileInputStream(mapFile);
 			objin = new ObjectInputStream(fis);
 			
 			// read terrain data
 			MapCell[][] terrain = (MapCell[][])objin.readObject();
 						
 			// set general info
 			mapHeight = terrain.length;
 			mapWidth = terrain[0].length;
 			
 			// read resources map and set resources in cells
 			@SuppressWarnings("unchecked")
 			HashMap<Point2i, HashMap<ResourceType, Integer>> cellResourceMap = 
 				(HashMap<Point2i, HashMap<ResourceType, Integer>>)objin.readObject();
 			
 			// read terrain info
 			cells = new CellState[mapHeight][mapWidth];
 			for (int i = 0; i < mapHeight; i++) {
 				for (int j = 0; j < mapWidth; j++) {
 					Point2i pos = new Point2i(j, i);
 					cells[i][j] = new CellState(terrain[i][j].cellType, new Point2i(j, i));
 					cells[i][j].resources.putAll(cellResourceMap.get(pos));
 					cells[i][j].resourceTypes.addAll(cellResourceMap.get(pos).keySet()); 
 				}
 			}
 			
 			
 		} catch(FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} finally{
 			//Close the ObjectInputStream
             try {
                 if (objin != null) {
                     objin.close();
                 }
                 
                 if (fis != null) {
                 	fis.close();
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
 		}
 	}
 }
