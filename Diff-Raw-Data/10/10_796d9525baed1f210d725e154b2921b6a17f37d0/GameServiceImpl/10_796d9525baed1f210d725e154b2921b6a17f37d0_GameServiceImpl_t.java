 package edu.cmu.cs.cimds.geogame.server;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.HashSet;
 import java.util.Collections;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import org.apache.commons.lang.StringUtils;
 import org.gwtwidgets.client.ui.pagination.Results;
 import org.hibernate.HibernateException;
 import org.hibernate.JDBCException;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.jasypt.util.password.BasicPasswordEncryptor;
 import org.jgrapht.*;
 import org.jgrapht.graph.*;
 import org.jgrapht.alg.util.VertexDegreeComparator;
 import org.apache.log4j.Logger;
 //import org.apache.log4j.Level;
 
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 import edu.cmu.cs.cimds.geogame.client.ActionResult;
 import edu.cmu.cs.cimds.geogame.client.GameTime;
 import edu.cmu.cs.cimds.geogame.client.MapInformation;
 import edu.cmu.cs.cimds.geogame.client.MoveResult;
 import edu.cmu.cs.cimds.geogame.client.NetworkType;
 import edu.cmu.cs.cimds.geogame.client.ServerSettingsResult;
 import edu.cmu.cs.cimds.geogame.client.exception.AuthorizationException;
 import edu.cmu.cs.cimds.geogame.client.exception.DBException;
 import edu.cmu.cs.cimds.geogame.client.exception.DuplicateUsernameException;
 import edu.cmu.cs.cimds.geogame.client.exception.GameEndedException;
 import edu.cmu.cs.cimds.geogame.client.exception.GameNotStartedException;
 import edu.cmu.cs.cimds.geogame.client.exception.GeoGameException;
 import edu.cmu.cs.cimds.geogame.client.exception.InvalidArgsException;
 import edu.cmu.cs.cimds.geogame.client.exception.InvalidMoveException;
 import edu.cmu.cs.cimds.geogame.client.exception.InvalidPurchaseException;
 import edu.cmu.cs.cimds.geogame.client.model.db.AcceptanceForm;
 import edu.cmu.cs.cimds.geogame.client.model.db.AcceptedForm;
 import edu.cmu.cs.cimds.geogame.client.model.db.Action;
 import edu.cmu.cs.cimds.geogame.client.model.db.Item;
 import edu.cmu.cs.cimds.geogame.client.model.db.ItemType;
 import edu.cmu.cs.cimds.geogame.client.model.db.Location;
 import edu.cmu.cs.cimds.geogame.client.model.db.PersistentEntity;
 import edu.cmu.cs.cimds.geogame.client.model.db.Road;
 import edu.cmu.cs.cimds.geogame.client.model.db.RoadMovement;
 import edu.cmu.cs.cimds.geogame.client.model.db.Synonym;
 import edu.cmu.cs.cimds.geogame.client.model.db.User;
 import edu.cmu.cs.cimds.geogame.client.model.db.RoadMovement.Status;
 import edu.cmu.cs.cimds.geogame.client.model.db.ServerSettingsStruct;
 import edu.cmu.cs.cimds.geogame.client.model.dto.GameStruct;
 import edu.cmu.cs.cimds.geogame.client.model.dto.ItemDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.ItemTypeDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.LocationDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.MessageDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.RoadDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.RoadMovementDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.ScoreMessage;
 import edu.cmu.cs.cimds.geogame.client.model.dto.ServerSettingsStructDTO;
 import edu.cmu.cs.cimds.geogame.client.model.dto.UserDTO;
 import edu.cmu.cs.cimds.geogame.client.model.enums.ActionType;
 import edu.cmu.cs.cimds.geogame.client.services.GameService;
 import edu.cmu.cs.cimds.geogame.client.services.GameServices;
 import edu.cmu.cs.cimds.geogame.client.util.Pair;
 import edu.cmu.cs.cimds.geogame.client.util.UserDegreeComparator;
 
 
 /**
  * Provides service functions for text messages on the server side.
  * @author Prasanna Velagapudi <psigen@gmail.com>
  */
 
 public class GameServiceImpl extends RemoteServiceServlet implements GameService {
 
 	private static final long serialVersionUID = -232445840085668456L;
 
 	private static final String DEFAULT_USER_ICON_FILENAME = "person.png";
 	
 	private static Logger logger = Logger.getRootLogger();
 	
 	public static ConcurrentHashMap<Long, TCPThread> threadMap = new ConcurrentHashMap<Long, TCPThread>();
 
 	private static final int MAX_INVENTORY_SIZE = Integer.MAX_VALUE;
 	public static Runnable tcpServer = new TCPServer();
 	//public static TCPServer server = new TCPServer();
 //	private static final int DELAY_FACTOR = 20000;
 	public static final int NUM_QUEST_ITEMS = 1;
 	public static int MAXIMUM_ITEMS_PER_LOCATION = 12;
 	
 	private static ConcurrentLinkedQueue<String> incomingMessages;
 	private static ConcurrentLinkedQueue<String> outgoingMessages;
 	
 	private static UndirectedGraph <User, DefaultEdge> userGraph = new SimpleGraph<User, DefaultEdge>(DefaultEdge.class);
 	
 	public static boolean isGameStarted = false;
 	public static boolean isGameFinished = false;
 	private static final long GAME_DURATION_DEFAULT = 100*60*1000; //10 minutes
 	public static long gameDuration;
 	private static Timer gameTimer = new Timer();
 	
 	public static double[] dangerProbs=new double[]{0.3,0.5,0.2};
 	public static double[] dangerMaxes=new double[]{0.2,0.6,0.9};
 	public static double[] timeStretches=new double[]{1,4};
 	public static final int DANGER_ROAD_TRIP_INCREMENT=30000;
 	public static final int MINIMUM_DURATION = 5000;
 	public static final int PENALTY_DURATION = 30000;
 	public static final double USER_GRAPH_DENSITY = 0.03;
 	public static final double AVG_NETWORK_DEGREE = 3.3;
 	
 	public static long endTime;
 //	private static final double burglarRatio = 0.05;
 	public static final int SCORE_PER_ITEM=1000;
 	public static final int SCORE_PROPAGATION_CUTOFF_DISTANCE = 3;
 	public static Map<Pair<String,String>,Integer> distanceMap;
 	public static String debugPlayerUsername = "TestUser0";
 	
 	public static ServerSettingsStructDTO serverSettings = new ServerSettingsStructDTO();
 	
 	static {
 		MessageServiceImpl.initialize();
 		Thread serverThread = new Thread(tcpServer);
 		serverThread.start();
 	}
 
 	@Override
 	public void startPeriodicGame() throws GeoGameException {
 		
 		GameServiceImpl.serverSettings = getServerSettings();
 		
 		TimerTask periodicGameTask = new TimerTask() {
 			@Override
 			public void run() {
 				DevServiceImpl devService = new DevServiceImpl();
 				
 				try {
 					devService.resetAllDB(serverSettings);
 					startGameTimer(serverSettings.getGameDuration());
 					
 				} catch (GeoGameException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		};
 		
 		gameTimer.schedule(periodicGameTask, 10000, serverSettings.getGameInterval());
 	}
 	
 	@Override
 	public void startGameTimer(long gameDuration) throws GeoGameException {
 //		if(isGameStarted && !isGameFinished) {
 //			return;
 //		}
 		if(gameDuration > 0) {
 			GameServiceImpl.gameDuration = gameDuration;
 		} else {
 			GameServiceImpl.gameDuration = GAME_DURATION_DEFAULT;
 			gameDuration = GAME_DURATION_DEFAULT;
 		}
 		
 		/*if(gameTimer!=null) {
 			gameTimer.cancel();
 			gameTimer = new Timer();
 		}*/
 		
 		TimerTask endGameTask = new TimerTask() {
 			@Override
 			public void run() {
 				isGameFinished = true;
 				Session session = PersistenceManager.getSession();
 				Transaction tx = session.beginTransaction();
 				
 				try {
 					Action endGameAction = new Action(null, ActionType.GAME_END);
 					session.save(endGameAction);
 					tx.commit();
 				} catch(HibernateException ex) {
 					tx.rollback();
 					logger.error(ex.getMessage(), ex);
 				}
 			}
 		};
 		gameTimer.schedule(endGameTask, gameDuration);
 		endTime = new Date().getTime() + gameDuration;
 		isGameStarted = true;
 		isGameFinished = false;
 		
 //		MessageServiceImpl.clearAll();
 //		MessageServiceImpl.populateMessagesMaps();
 
 
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			Action resetDbAction = new Action(null, ActionType.GAME_START);
 			session.save(resetDbAction);
 			tx.commit();
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 	}
 	
 	@Override
 	public void stopGameTimer() throws GeoGameException {
 		// GameServiceImpl.serverSettings = getServerSettings();
 		
 		/*if(!isGameStarted) {
 			return;
 		}*/
 	      
 		if(gameTimer!=null) {
 			gameTimer.cancel();
 			gameTimer=null;
 		}
 		endTime = new Date().getTime();
 		isGameStarted=true;
 		isGameFinished=true;
 
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			Action resetDbAction = new Action(null, ActionType.GAME_END);
 			session.save(resetDbAction);
 			tx.commit();
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 		}
 	}
 	
 	public GameStruct getGameInformationNew(UserDTO player) throws GeoGameException {
 
 		if(player!=null) {
 			logger.warn("Player " + player.getUsername() + " requests map information");
 		}
 
 		GameStruct gameInformation = new GameStruct();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			//FIXME: This is a hack used to allow the MapOverviewer to get map information without authenticating.
 			User user = null;
 			if(player != null) {
 				user = AuthenticationUtil.authenticatePlayer(session, player);
 			}
 			
 			List<Location> locations;
 			List<Road> roads;
 			
 			locations = (List<Location>)session.createCriteria(Location.class).list();
 			roads = (List<Road>)session.createCriteria(Road.class).list();
 
 			MapInformation mapInformation = new MapInformation();
 			for(Location location : locations) {
 				mapInformation.locations.add(new LocationDTO(location));
 			}
 			for(Road road : roads) {
 				mapInformation.roads.add(new RoadDTO(road));
 			}
 			//FIXME: This is a hack used to allow the MapOverviewer to get map information without authenticating.
 			if(user!=null) {
 				gameInformation.player = MessageServiceImpl.userCacheMap.get(player.getId());
 			}
 			gameInformation.mapInformation = mapInformation;
 			gameInformation.timeRemaining = endTime - new Date().getTime();
 			gameInformation.gameDuration = gameDuration;
 			
 			gameInformation.gameStarted = isGameStarted;
 			gameInformation.gameFinished = isGameFinished;
 
 			tx.commit();
 		}
 		catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		catch (NullPointerException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		return gameInformation;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public GameStruct getGameInformation(UserDTO player) throws GeoGameException {
 //		logger.error("MESSAGE GETTING GAME INFORMATION!!");
 		// Add message to list
 		if(player!=null) {
 			logger.warn(/*Thread.currentThread().getId() + */"Player " + player.getUsername() + " requests map information");
 		}
 
 		GameStruct gameInformation = new GameStruct();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 //			if(player!=null) {
 //				TransactionLog tl = new TransactionLog("getGameInformation", player.getUsername());
 //				tl.startTime = System.currentTimeMillis();
 //			}
 			
 			//FIXME: This is a hack used to allow the MapOverviewer to get map information without authenticating.
 			User user = null;
 			if(player!=null) {
 				user = AuthenticationUtil.authenticatePlayer(session, player);
 			}
 			
 			//Determine which parts of the map does the user have access to see
 			//For now, send all information about roads and locations
 			
 //			List<Location> locations = (List<Location>)session.createCriteria(Location.class)
 //				.list();
 //			List<Road> roads = (List<Road>)session.createCriteria(Road.class)
 //				.list();
 			
 			List<Location> locations;
 			List<Road> roads;
 //			if(user.isAdmin()) {
 				locations = (List<Location>)session.createCriteria(Location.class).list();
 				roads = (List<Road>)session.createCriteria(Road.class).list();
 //			} else {
 //				locations = user.getKnownLocations();
 //				roads = user.getKnownRoads();
 //			}
 			MapInformation mapInformation = new MapInformation();
 			for(Location location : locations) {
 				mapInformation.locations.add(new LocationDTO(location));
 			}
 			for(Road road : roads) {
 				mapInformation.roads.add(new RoadDTO(road));
 			}
 			//FIXME: This is a hack used to allow the MapOverviewer to get map information without authenticating.
 			if(user!=null) {
 				gameInformation.player = MessageServiceImpl.updateCacheMap(session, user, true);
 			}
 			gameInformation.mapInformation = mapInformation;
 			gameInformation.timeRemaining = endTime - new Date().getTime();
 			gameInformation.gameDuration = gameDuration;
 			
 			gameInformation.gameStarted = isGameStarted;
 			gameInformation.gameFinished = isGameFinished;
 			
 //			session.update(user);
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 			tx.commit();
 		}
 		catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		catch (NullPointerException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		return gameInformation;
 	}
 
 	public LocationDTO getLocationInformationNew (UserDTO player, Long locationId) throws GeoGameException {
 //		return GameLogic.getInstance().getLocationInformation(player, locationId);
 		
 		LocationDTO locationInformation;
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {	
 			User user = AuthenticationUtil.authenticatePlayer(session, player);
 			
 			Location location = (Location)session.createCriteria(Location.class)
 				.add(Restrictions.idEq(locationId))
 				.uniqueResult();
 			
 			locationInformation = new LocationDTO(location);
 			
 			if(locationInformation != player.getCurrentLocation()) {
 				throw new InvalidArgsException("ERR - " + player.getUsername() + " is not currently in " + location.getName());
 			}	
 
 			tx.commit();
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": GET LOCATION INFORMATION FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		}catch(HibernateException ex) {
 			tx.rollback();
 			logger.warn(ex.getMessage());
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			logger.warn(ex.getMessage());
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} 
 		
 		return locationInformation;
 	}
 	
 	@Override
 	public LocationDTO getLocationInformation(UserDTO player, Long locationId) throws GeoGameException {
 //		return GameLogic.getInstance().getLocationInformation(player, locationId);
 		
 		LocationDTO locationInformation;
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {	
 //			TransactionLog tl = new TransactionLog("getLocationInformation", player.getUsername());
 //			tl.startTime = System.currentTimeMillis();
 
 			User user = AuthenticationUtil.authenticatePlayer(session, player);
 			
 			Location location = (Location)session.createCriteria(Location.class)
 				.add(Restrictions.idEq(locationId))
 				.uniqueResult();
 			
 			if(location!=user.getCurrentLocation()) {
 				throw new InvalidArgsException("ERR - " + player.getUsername() + " is not currently in " + location.getName());
 			}
 
 			locationInformation = new LocationDTO(location);
 
 //			session.update(user);
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 			tx.commit();
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": GET LOCATION INFORMATION FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		}catch(HibernateException ex) {
 			tx.rollback();
 			logger.warn(ex.getMessage());
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			logger.warn(ex.getMessage());
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} 
 		
 		return locationInformation;
 	}
 	
 	public static int getUsersTravellingOnTheRoad(List<User> users, String source, String destination) {
 		
 		int numOfUsers = 0;
 		
 		for(User user : users) {
 			if(MessageServiceImpl.userCacheMap.containsKey(user.getId())) {
 				UserDTO userDTO = MessageServiceImpl.userCacheMap.get(user.getId());
 				
 				
 				if(userDTO.getCurrentRoad() != null)
 				{
 					/* User is moving on a road */
 					String src = userDTO.getCurrentRoad().getLocation1().getName();
 					String dst =  userDTO.getCurrentRoad().getLocation2().getName();
 					System.out.println("Soruce = " + src);
 					System.out.println("Destination = " + dst);
 					
 					if(source.equals(src) && destination.equals(dst))
 					{
 						numOfUsers++;
 					}
 					else if (destination.equals(src) && source.equals(dst))
 					{
 						numOfUsers++;
 					}
 				}
 				else
 				{
 					System.out.println("getCurrentRoad doesnt work for " + user.getUsername());
 				}
 				
 			}
 		}
 		System.out.println("Number of users = " + numOfUsers);
 		return numOfUsers; 
 	}
 	
 	
 	public MoveResult moveToLocationNew (UserDTO player, Long locationId) throws GeoGameException {
 		System.out.println("Calling move to Location new");
 		if(!isGameStarted) {
 			throw new GameNotStartedException();
 		}
 		if(isGameFinished) {
 			throw new GameEndedException();
 		}
 
 		MoveResult moveResult = new MoveResult();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 
 		try {
 
 			synchronized(MessageServiceImpl.userCacheMap.get(player.getId())) {
 				User user = AuthenticationUtil.authenticatePlayer(session, player);
 				UserDTO cachePlayer = MessageServiceImpl.userCacheMap.get(player.getId());
 				
 				if(user.getCurrentLocation() == null) {
 					throw new InvalidMoveException("Player cannot move - still moving!");
 				}
 	
 				
 				Location destination = (Location)session.createCriteria(Location.class)
 					.add(Restrictions.idEq(locationId))
 					.uniqueResult();
 				
 				Location sourceLocation = (Location)session.createCriteria(Location.class)
 					.add(Restrictions.idEq(cachePlayer.getCurrentLocation().getId()))
 					.uniqueResult();
 	
 				String travelString = cachePlayer.getCurrentLocation().getName() + " -> " + destination.getName();
 
 				Road road = (Road)session.createCriteria(Road.class)
 					.add(Restrictions.or(
 							Restrictions.and(
 									Restrictions.eq("location1.id", cachePlayer.getCurrentLocation().getId()),
 									Restrictions.eq("location2.id", locationId))
 									,
 							Restrictions.and(
 									Restrictions.eq("location1.id", locationId),
 									Restrictions.eq("location2.id", cachePlayer.getCurrentLocation().getId()))
 									)
 						)
 					.uniqueResult();
 				if(road==null) {
 					throw new InvalidMoveException(/*counter.getCounter() + ": " + threadIdString + */" No road exists: " + travelString);
 				}
 
 				cachePlayer.setCurrentRoad(new RoadDTO(road));
 
 				List<User> users = (List<User>) session.createCriteria(User.class).list();
 				double frac = ((double)(getUsersTravellingOnTheRoad(users, sourceLocation.getName(), destination.getName()) - 1)  / users.size());
 				System.out.println("Frac = " + frac);
 				int duration = (int) Math.ceil(road.getTravelDuration() * ( 1 +  frac));
 				System.out.println("Duration = " + duration);
 				//int duration = road.getTravelDuration();
 				
 				moveResult.setDuration(duration);
 				moveResult.setSource(new LocationDTO(sourceLocation));
 				moveResult.setDestination(new LocationDTO(destination));
 				
 				cachePlayer.setCurrentLocation(null);
 				cachePlayer.setCurrentRoad(new RoadDTO(road));
 				
 				if(road.getLocation2()==destination) {
 					cachePlayer.setForward(true);
 				} else {
 					cachePlayer.setForward(false);
 				}
 				
 				Date moveStartDate = new Date();
 				
 				RoadMovement roadMovement = new RoadMovement();
 				roadMovement.setRoad(road);
 				roadMovement.setMoveStart(moveStartDate);
 				roadMovement.setDuration(moveResult.getDuration());
 				roadMovement.setForward(cachePlayer.getForward());
 				roadMovement.setStatus(Status.ACTIVE);
 				roadMovement.setUser(user);
 				
 				RoadMovementDTO roadMovementDTO = new RoadMovementDTO();
 				roadMovementDTO.setRoad(new RoadDTO(road));
 				roadMovementDTO.setMoveStart(moveStartDate);
 				roadMovementDTO.setDuration(moveResult.getDuration());
 				roadMovementDTO.setForward(cachePlayer.getForward());
 				roadMovementDTO.setStatus(RoadMovementDTO.Status.ACTIVE);
 				
 				cachePlayer.setCurrentRoadMovement(roadMovementDTO);
 				cachePlayer.setMoving(true);
 				session.saveOrUpdate(roadMovement);
 				
 				//Adding mapInformation the player gained...
 				GameStruct gameInformation = new GameStruct();
 				gameInformation.mapInformation = new MapInformation();
 				MessageServiceImpl.updateCacheMapFromClient(cachePlayer);
 				gameInformation.player = cachePlayer;
 	
 				gameInformation.gameStarted = isGameStarted;
 				gameInformation.gameFinished = isGameFinished;
 	
 				String message = "";
 
 				message += "Moving " + travelString + "." + " The trip will last " + Math.rint(moveResult.getDuration()/100)/10 + "s";
 				moveResult.setMessage(message);
 				moveResult.setGameInformation(gameInformation);
 				moveResult.setSuccess(true);
 				
 				Action moveAction = new Action(user, ActionType.MOVE);
 				moveAction.setSourceLocation(sourceLocation);
 				moveAction.setDestinationLocation(destination);
 
 				session.save(moveAction);
 
 				tx.commit();
 			}
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		} catch(HibernateException ex) {
 			tx.rollback();
 			System.out.println(player.getUsername() + " MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			System.out.println(player.getUsername() + " MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		
 		return moveResult;
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	public ActionResult takeItemNew(UserDTO player, long itemId) throws GeoGameException {
 		if(!isGameStarted) {
 			throw new GameNotStartedException();
 		}
 		if(isGameFinished) {
 			throw new GameEndedException();
 		}
 		
 		ActionResult takeResult = new ActionResult();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		
 		try {
 			synchronized(MessageServiceImpl.userCacheMap.get(player.getId())) {
 
 				User user = AuthenticationUtil.authenticatePlayer(session, player);
 				
 				UserDTO cachePlayer = MessageServiceImpl.userCacheMap.get(player.getId());
 
 				if(cachePlayer.getInventory().size()==MAX_INVENTORY_SIZE) {
 					throw new InvalidPurchaseException("Inventory full!");
 				}
 	
 				//Make sure player is in the same location as the item
 				
 				//Determine what happens when he takes the item, whether his gold decreases,
 				//make sure of whether the player really needed it,
 				//or whether something else happens. For now, just take the item.
 				Item item = (Item)session.createCriteria(Item.class)
 					.add(Restrictions.idEq(itemId))
 					.uniqueResult();
 				ItemDTO itemDTO = new ItemDTO(item);
 				Location itemLocation = item.getLocation();
 				LocationDTO itemLocationDTO = new LocationDTO(itemLocation);
 				Location playerLocation = (Location)session.createCriteria(Location.class)
 					.add(Restrictions.idEq(cachePlayer.getCurrentLocation().getId()))
 					.uniqueResult();
 				LocationDTO playerLocationDTO = new LocationDTO(playerLocation);
 				
 				if(!cachePlayer.getCurrentLocation().equals(itemLocationDTO)) {
 					throw new InvalidPurchaseException("Player is not in the same location as " + item.getItemType().getName() + ".");
 				}
 	
 				List<ItemTypeDTO> goalItemsList = new ArrayList<ItemTypeDTO>(cachePlayer.getItemsToCollect());
 				for(ItemDTO inventoryItem : cachePlayer.getInventory()) {
 					goalItemsList.remove(inventoryItem.getItemType());
 				}
 				if(!goalItemsList.contains(itemDTO.getItemType())) {
 					throw new InvalidPurchaseException(item.getItemType().getName() + " is not needed by player!");
 				}
 				
 				item.getLocation().removeItem(item);
 				cachePlayer.addInventory(itemDTO);
 				//cachePlayer.setScore(cachePlayer.getScore() + SCORE_PER_ITEM);
 				// we're updating user anyway here, gotta keep track of their score in one place.
 				// otherwise message service sometimes overrides it
 				user.setScore(user.getScore() + SCORE_PER_ITEM);
 				cachePlayer.setScore(user.getScore());
 				
 				Action scoreIncreaseAction = new Action(user, ActionType.SCORE_INCREASE);
 				scoreIncreaseAction.setScoreIncrease(SCORE_PER_ITEM);
 				scoreIncreaseAction.setNewScore(user.getScore());
 				session.save(scoreIncreaseAction);
 
 				if(distanceMap==null) {
 					List<User> allUsers = (List<User>)session.createCriteria(User.class).list();
 					distanceMap = DevServiceImpl.calculateDistanceMap(allUsers);
 				}
 				
 				List<UserDTO> otherPlayers = new ArrayList<UserDTO>();
 				for (Map.Entry<Long, UserDTO> otherPlayerEntry : MessageServiceImpl.userCacheMap.entrySet()) {
 					if (otherPlayerEntry.getKey() != cachePlayer.getId()) {
 						otherPlayers.add(otherPlayerEntry.getValue());
 					}
 				}
 				
 				
 				for(UserDTO otherPlayer : otherPlayers) {
 
 					Pair<String,String> pair = new Pair<String,String>(cachePlayer.getUsername(), otherPlayer.getUsername());
 					if(distanceMap.containsKey(pair) && distanceMap.get(pair) <= SCORE_PROPAGATION_CUTOFF_DISTANCE) {
 						int distance = distanceMap.get(pair);
 						int scoreObtained = 0;
 						scoreObtained = SCORE_PER_ITEM/(1<<distance);
 
 						ScoreMessage scoreMessage = new ScoreMessage(MessageServiceImpl.userCacheMap.get(cachePlayer.getId()),
 									MessageServiceImpl.userCacheMap.get(otherPlayer.getId()), item, scoreObtained);
 						MessageServiceImpl.scoreMessagesMap.get(otherPlayer.getId()).add(scoreMessage);
 						
 						User otherUser = AuthenticationUtil.authenticatePlayer(session, otherPlayer);
 						otherUser.setScore(otherUser.getScore() + scoreObtained);
 						otherPlayer.setScore(otherUser.getScore());
 						MessageServiceImpl.updateCacheMapFromClient(otherPlayer);
 					}
 				}
 				
 				//IF Item is replenishable,
 				//Creates another item of the same kind in the same location, SIMULATING the "replenishability" of the item
 				if(item.getItemType().isReplenishable()) {
 					createItem(session, item.getItemType(), itemLocation);
 				}
 				
 				List<ItemType> allItemTypes = (List<ItemType>)session.createCriteria(ItemType.class).list();
 				List<Location> allLocations = (List<Location>)session.createCriteria(Location.class).list();
 				List<Item> allItems = (List<Item>)session.createCriteria(Item.class).list();
 				
 				List<Item> distanceItems = DevServiceImpl.findItemsAtDistances(allItems, playerLocation, 0, DevServiceImpl.MIN_ITEM_DISTANCE-1, allLocations, MessageServiceImpl.locationDistanceMap);
 				List<Location> distanceLocations = DevServiceImpl.locationsAtDistances(playerLocation, DevServiceImpl.MIN_ITEM_DISTANCE, DevServiceImpl.MAX_ITEM_DISTANCE, allLocations, MessageServiceImpl.locationDistanceMap);
 				for(Item distanceItem : distanceItems) {
 					allItemTypes.remove(distanceItem);
 				}
 				//Adds another random itemType to look for on the map.
 				int randomIndex = (int)Math.floor((Math.random()*allItemTypes.size()));
 				ItemType randomItemType = allItemTypes.get(randomIndex);
 				// ensure user is not assigned an item that is present in the current location
 				while (cachePlayer.getCurrentLocation().getItems().contains(randomItemType)) {
 					randomIndex = (int)Math.floor((Math.random()*allItemTypes.size()));
 					randomItemType = allItemTypes.get(randomIndex);
 				}
 				
 				cachePlayer.addItemToCollect(new ItemTypeDTO(randomItemType));
 				Action newGoalItemAction = new Action(user, ActionType.NEW_GOAL_ITEM);
 				newGoalItemAction.setItemType(randomItemType);
 				session.save(newGoalItemAction);
 				
 				cachePlayer.setCurrentGoalItemType(randomItemType);
 				
 				//Creates a corresponding item in the map (IF it is not replenishable)
 				if(!randomItemType.isReplenishable()) {
 					randomIndex = (int)Math.floor((Math.random()*distanceLocations.size()));
 					Location randomLocation = distanceLocations.get(randomIndex);
 
 					createItem(session, randomItemType, randomLocation);
 				}
 					
 				Action takeAction = new Action(user, ActionType.PURCHASE);
 				takeAction.setSourceLocation(playerLocation);
 				takeAction.setItem(item);
 				takeAction.setLocationItems(makeIdsString(playerLocation.getItems()));
 				session.save(takeAction);
 	
 				//***Give the player any new information about the map.
 				//***In this case, the location is updated to reflect that it no longer
 				//***has the item that the player took.
 				MapInformation mapInformation = new MapInformation();
 				mapInformation.locations.add(new LocationDTO(itemLocation));
 	
 				takeResult.setMessage(item.getItemType().getName() + " taken by " + player.getUsername());
 				//***Give the player any new information changed in the player.
 				//***In this method, a new item was added to the inventory.
 				MessageServiceImpl.updateCacheMapFromClient(cachePlayer);
 				GameStruct gameStruct = new GameStruct(cachePlayer, mapInformation);
 				gameStruct.gameStarted = isGameStarted;
 				gameStruct.gameFinished = isGameFinished;
 	
 				takeResult.setGameInformation(gameStruct);
 				takeResult.setSuccess(true);
 
 				tx.commit();
 			}
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": TAKE ITEM FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + " TAKE ITEM FAILED!");
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + " TAKE ITEM FAILED!");
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		} 
 
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " exiting takeItem method - " + itemId);
 //		}
 		
 //		session = PersistenceManager.getSession();
 //		tx = session.beginTransaction();
 //		
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			if(oldScore==user.getScore()) {
 //				logger.error("ERROR! USER'S SCORE WAS NOT UPDATED!!");
 //			}
 //			if(newScore==user.getScore()) {
 //				logger.info("WOW! SCORE WAS UPDATED CORRECTLY??" + newScore + "," + MessageServiceImpl.userCacheMap.get(player.getId()).getScore());
 //			}
 //			if(oldScore==newScore) {
 //				logger.info("oldScore = " + oldScore + " = " + newScore + " = newScore");
 //			}
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 		
 		return takeResult;		
 	}
 
 	@Override
 	public MoveResult moveToLocation(UserDTO player, Long locationId) throws GeoGameException {
 		System.out.println("Move to location 1 getting called ");
 		if(!isGameStarted) {
 			throw new GameNotStartedException();
 		}
 		if(isGameFinished) {
 			throw new GameEndedException();
 		}
 
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " entering moveToLocation " + locationId);
 //		}
 		
 //		logger.warn
 //		System.out.println("Entered moveToLocation, going to " + locationId);
 		MoveResult moveResult = new MoveResult();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 //		String threadIdString = String.valueOf(Thread.currentThread().getId()) + "," + session.hashCode();
 		try {
 //			TransactionLog tl = new TransactionLog("moveToLocation", player.getUsername());
 //			tl.startTime = System.currentTimeMillis();
 
 //			Counter counter = (Counter)session.get(Counter.class, 1L);
 //			moveResult.setCounter(counter.getCounter());
 //			counter.setCounter(counter.getCounter()+1);
 //			session.saveOrUpdate(counter);
 
 			synchronized(MessageServiceImpl.userCacheMap.get(player.getId())) {
 				User user = AuthenticationUtil.authenticatePlayer(session, player);
 
 				UserDTO cachePlayer = MessageServiceImpl.userCacheMap.get(player.getId());
 				MessageServiceImpl.updateUserWithPlayer(session, user, cachePlayer);
 				
 	
 				updateUserLocationIfArrived(session, user);
 				if(user.getCurrentLocation()==null) {
 					throw new InvalidMoveException("Player cannot move - still moving!");
 				}
 	
 				Location destination = (Location)session.createCriteria(Location.class)
 					.add(Restrictions.idEq(locationId))
 					.uniqueResult();
 				Location sourceLocation = user.getCurrentLocation();
 	
 //				if(player.getUsername().equals(debugPlayerUsername)) {
 //					logger.info(player.getUsername() + " inside moveToLocation transaction, moving to " + destination.getName());
 //				}
 	
 				String travelString = user.getCurrentLocation().getName() + " -> " + destination.getName();
 				
 	//			System.out.println(/*Thread.currentThread().getId() + " -- " + */player.getUsername() + " moving from " + user.getCurrentLocation().getName() + " to " + destination.getName());
 	
 				Road road = (Road)session.createCriteria(Road.class)
 					.add(Restrictions.or(
 							Restrictions.and(
 									Restrictions.eq("location1.id", user.getCurrentLocation().getId()),
 									Restrictions.eq("location2.id", locationId))
 									,
 							Restrictions.and(
 									Restrictions.eq("location1.id", locationId),
 									Restrictions.eq("location2.id", user.getCurrentLocation().getId()))
 									)
 						)
 					.uniqueResult();
 				if(road==null) {
 					throw new InvalidMoveException(/*counter.getCounter() + ": " + threadIdString + */" No road exists: " + travelString);
 				}
 	
 	//			boolean burglarFlag = false;
 	//			int burglarCost = 0;
 	//			if(Math.random()<road.getDanger()) {
 	//				//BURGLAR ATTACK!!
 	//				logger.warn(Thread.currentThread().getId() + " -- " + user.getUsername() + " was robbed!");
 	//				burglarFlag = true;
 	//				burglarCost = (int)(user.getScore()*burglarRatio);
 	//				user.setScore(user.getScore()-burglarCost);
 	//				
 	////				int randomInventoryIndex = (int)Math.floor(Math.random()*user.getInventory().size());
 	////				Item item = user.getInventory().get(randomInventoryIndex);
 	////				user.getInventory().remove(item);
 	//			}
 				
 	//			int movePrice=0;
 	//			switch(road.getRoadType()) {
 	//				case HIGHWAY:
 	//					movePrice = 5;
 	//					break;
 	//				case GOOD:
 	//					movePrice = 10;
 	//					break;
 	//				case BAD:
 	//					movePrice = 25;
 	//					break;
 	//			}
 	//			if(user.getScore()<movePrice) {
 	//				throw new InvalidMoveException(sourceLocation, destination, threadIdString + " -- You need " + movePrice + "G to travel " + travelString + ". Not enough gold!");
 	//			}
 				
 	//			user.setScore(user.getScore()-movePrice);
 	//			double duration = 0;
 	//			int duration = 0;
 				
 	//			Random r = new Random();
 	
 	//			double randomFactor = r.nextGaussian();
 	//			double randomFactor = r.nextGaussian() + 1; 	//Obtains the distance from the center of the normal plus 1
 																		//Distributed by a doubly thick half-bell sliding down from 1.
 																		//Represents the standard delay.
 				
 	//			duration = DELAY_FACTOR*road.getLength()*randomFactor;
 	//			duration = DELAY_FACTOR*road.calculateLength();
 				int duration = road.getTravelDuration();
 				
 	//			if(r.nextDouble() <= road.getDanger()) {
 	//				duration +=DANGER_ROAD_TRIP_INCREMENT;
 	////				randomFactor *= timeStretches[1];
 	//				//Abnormal delay.
 	////				randomFactor *= 4;
 	//			} else {
 	//				duration+=0;
 	//			}
 	//			if(road.isPenalty()) {
 	//				duration+=PENALTY_DURATION;
 	//			}
 				
 	//			if(duration<=MINIMUM_DURATION) {
 	//				duration = Math.max(MINIMUM_DURATION, duration+MINIMUM_DURATION);
 	//			}
 				
 	//			moveResult.setDuration((int)(/*road.getDanger()**/DELAY_FACTOR*road.getLength()*randomFactor));
 				moveResult.setDuration(duration);
 				moveResult.setSource(new LocationDTO(sourceLocation));
 				moveResult.setDestination(new LocationDTO(destination));
 				
 				user.setCurrentLocation(null);
 				user.setCurrentRoad(road);
 				if(road.getLocation2()==destination) {
 					user.setForward(true);
 				} else {
 					user.setForward(false);
 				}
 	//			user.setMoveStart(new Date());
 				
 				RoadMovement roadMovement = new RoadMovement();
 				roadMovement.setRoad(user.getCurrentRoad());
 				roadMovement.setMoveStart(new Date());
 				roadMovement.setDuration(moveResult.getDuration());
 				roadMovement.setForward(user.getForward());
 				roadMovement.setStatus(Status.ACTIVE);
 				roadMovement.setUser(user);
 				user.setCurrentRoadMovement(roadMovement);
 				session.saveOrUpdate(roadMovement);
 				user.setMoving(true);
 				
 				//Adding mapInformation the player gained...
 				GameStruct gameInformation = new GameStruct();
 				gameInformation.mapInformation = new MapInformation();
 				
 	//			*******	CODE THAT ADDS KNOWN LOCATIONS AND ROADS TO A USER
 	//			*******	COMMENTED OUT NOW BECAUSE EVERYTHING IS KNOWN TO EVERYONE NOW
 	
 	//			List<Road> newRoads = (List<Road>)session.createCriteria(Road.class)
 	//				.add(Restrictions.or(
 	//					Restrictions.eq("location1", destination),
 	//					Restrictions.eq("location2", destination)
 	//					)
 	//				)
 	//				.list();
 	//
 	//			for(Road newRoad : newRoads) {
 	//				if(!user.getKnownRoads().contains(newRoad)) {
 	//					user.addKnownRoad(newRoad);
 	//					gameInformation.mapInformation.roads.add(new RoadDTO(newRoad));
 	//				}
 	//				if(!user.getKnownLocations().contains(newRoad.getLocation1())) {
 	//					user.addKnownLocation(newRoad.getLocation1());
 	//					gameInformation.mapInformation.locations.add(new LocationDTO(newRoad.getLocation1()));
 	//				}
 	//				if(!user.getKnownLocations().contains(newRoad.getLocation2())) {
 	//					user.addKnownLocation(newRoad.getLocation2());
 	//					gameInformation.mapInformation.locations.add(new LocationDTO(newRoad.getLocation2()));
 	//				}
 	//			}
 	//			gameInformation.player = MessageServiceImpl.updateNewUserDTO(session, user, true);
 				
 	//			String userLocation = user.getCurrentLocation()==null ? "NULL" : user.getCurrentLocation().getName();
 	//			logger.info("Inside MoveToLocation - User's location is " + userLocation);
 	//			UserDTO player3 = MessageServiceImpl.userCacheMap.get(user.getId());
 	//			String player3Location = player3.getCurrentLocation()==null ? "NULL" : player3.getCurrentLocation().getName();
 	//			logger.info("Inside MoveToLocation - Player's location is " + player3Location);
 				
 				gameInformation.player = MessageServiceImpl.updateCacheMap(session, user, true);
 	//			player3Location = player3.getCurrentLocation()==null ? "NULL" : player3.getCurrentLocation().getName();
 	//			logger.info("Inside MoveToLocation - Player's location is " + player3Location);
 				
 	//			session.update(user);
 	
 				gameInformation.gameStarted = isGameStarted;
 				gameInformation.gameFinished = isGameFinished;
 	
 	//			System.out.println(/*Thread.currentThread().getId() + " -- " + */player.getUsername() + " moved from " + travelString + "!!");
 							
 				//***Give the player any new information changed in the player.
 				//***In this method, only the location changes.
 				String message = /*threadIdString + " -- "*/"";
 	//			if(burglarFlag) {
 	//				message += "You were robbed!! (-" + burglarCost + " G)\n";
 	//			}
 	//			message += travelString + " is a " + road.getRoadType().toString() + " road." + " Your cost was " + movePrice + "G";
 				message += "Moving " + travelString + "." + " The trip will last " + Math.rint(moveResult.getDuration()/100)/10 + "s";
 				moveResult.setMessage(message);
 	//			System.out.println(message);
 				
 				moveResult.setGameInformation(gameInformation);
 				
 				moveResult.setSuccess(true);
 				
 	//			System.out.println(/*Thread.currentThread().getId() + " -- " + */user.getUsername() + "'s move successful");
 				
 				Action moveAction = new Action(user, ActionType.MOVE);
 				moveAction.setSourceLocation(sourceLocation);
 				moveAction.setDestinationLocation(destination);
 	//			moveAction.setPrice(movePrice);
 	//			moveAction.setBurglary(burglarFlag);
 	//			moveAction.setBurglaryCost(burglarCost);
 				session.save(moveAction);
 	
 //				if(player.getUsername().equals(debugPlayerUsername)) {
 //					logger.info(player.getUsername() + " exiting moveToLocation transaction, moving to " + destination.getName());
 //				}
 	
 //				tl.endTime = System.currentTimeMillis();
 //				transactionLog.add(tl);
 				tx.commit();
 			}
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		} catch(HibernateException ex) {
 			tx.rollback();
 			System.out.println(player.getUsername() + " MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			System.out.println(player.getUsername() + " MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " exiting moveToLocation method");
 //		}
 //		
 //		session = PersistenceManager.getSession();
 //		tx = session.beginTransaction();
 //		
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			if(!user.isMoving()) {
 //				logger.error("ERROR! USER JUST STARTED TO MOVE AND IS 'NOT MOVING'!!");
 //			}
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 		
 		return moveResult;
 	}
 	
 //	@Override
 //	public ActionResult moveOnRoad(UserDTO player, Long roadId) throws GeoGameException {
 //		ActionResult moveResult = new ActionResult();
 //		
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			
 //			//Determine what happens when he moves, whether he has access
 //			//or whether something else happens. For now, just move him there.
 //			Road road = (Road)session.createCriteria(Road.class)
 //				.add(Restrictions.idEq(roadId))
 //				.uniqueResult();
 //			if(road.getLocation1()==user.getCurrentLocation()) {
 //				user.setCurrentLocation(road.getLocation2());
 //				session.saveOrUpdate(user);
 //			} else if(road.getLocation2()==user.getCurrentLocation()) {
 //				user.setCurrentLocation(road.getLocation1());
 //				session.saveOrUpdate(user);
 //			} else {
 //				moveResult.setSuccess(false);
 //				moveResult.setMessage("Player is not adjacent to road");
 //			}
 //			
 //			//***Give the player any new information he discovered about the map.
 //			//***For now, nothing is returned.
 //			//moveResult.setMapInformation(new MapInformation());
 //			
 //			//***Give the player any new information changed in the player.
 //			//***In this method, only the location changes.
 //			moveResult.setGameInformation(new GameStruct(new UserDTO(user, true),null));
 //			moveResult.setSuccess(true);
 //			tx.commit();
 //			//session.clear();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 //		
 //		return moveResult;		
 //	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public ActionResult takeItem(UserDTO player, long itemId) throws GeoGameException {
 		if(!isGameStarted) {
 			throw new GameNotStartedException();
 		}
 		if(isGameFinished) {
 			throw new GameEndedException();
 		}
 
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " entering takeItem " + itemId);
 //		}
 
 //		long oldScore = 0;
 //		long newScore = 0;
 		
 		ActionResult takeResult = new ActionResult();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		
 		try {
 //			TransactionLog tl = new TransactionLog("takeItem", player.getUsername());
 //			tl.startTime = System.currentTimeMillis();
 
 			synchronized(MessageServiceImpl.userCacheMap.get(player.getId())) {
 
 				User user = AuthenticationUtil.authenticatePlayer(session, player);
 				
 				UserDTO cachePlayer = MessageServiceImpl.userCacheMap.get(player.getId());
 				MessageServiceImpl.updateUserWithPlayer(session, user, cachePlayer);
 
 				if(user.getInventory().size()==MAX_INVENTORY_SIZE) {
 					throw new InvalidPurchaseException("Inventory full!");
 				}
 	
 				//Make sure player is in the same location as the item
 				
 				//Determine what happens when he takes the item, whether his gold decreases,
 				//make sure of whether the player really needed it,
 				//or whether something else happens. For now, just take the item.
 				Item item = (Item)session.createCriteria(Item.class)
 					.add(Restrictions.idEq(itemId))
 					.uniqueResult();
 				Location itemLocation = item.getLocation();
 
 //				if(player.getUsername().equals(debugPlayerUsername)) {
 //					logger.info(player.getUsername() + " inside takeItem transaction" + item.getItemType().getName() + " in " + itemLocation.getName());
 //				}
 				
 				if(user.getCurrentLocation()!=itemLocation) {
 					throw new InvalidPurchaseException("Player is not in the same location as " + item.getItemType().getName() + ".");
 				}
 	
 				List<ItemType> goalItemsList = new ArrayList<ItemType>(user.getItemsToCollect());
 	//			goalItemsList.removeAll(user.getInventory());
 				for(Item inventoryItem : user.getInventory()) {
 					goalItemsList.remove(inventoryItem.getItemType());
 				}
 				if(!goalItemsList.contains(item.getItemType())) {
 					throw new InvalidPurchaseException(item.getItemType().getName() + " is not needed by player!");
 				}
 	
 				//			int itemPrice = item.getPrice();
 	//			if(player.getScore()<itemPrice) {
 	//				throw new InvalidPurchaseException(item, "Not enough gold");
 	//			}
 	//			user.setScore(player.getScore()-itemPrice);
 				
 				item.getLocation().removeItem(item);
 	//			item.getLocation().getItems().remove(item);
 	//			item.setPrice(0);
 	//			item.setLocation(null);
 				user.addInventory(item);
 //				oldScore = user.getScore();
 				user.setScore(user.getScore()+SCORE_PER_ITEM);
 
 				Action scoreIncreaseAction = new Action(user, ActionType.SCORE_INCREASE);
 				scoreIncreaseAction.setScoreIncrease(SCORE_PER_ITEM);
 				scoreIncreaseAction.setNewScore(user.getScore());
 				session.save(scoreIncreaseAction);
 
 //				newScore = user.getScore();
 
 				if(distanceMap==null) {
 					List<User> allUsers = (List<User>)session.createCriteria(User.class).list();
 					distanceMap = DevServiceImpl.calculateDistanceMap(allUsers);
 				}
 				List<User> users = (List<User>)session.createCriteria(User.class)
 									.add(Restrictions.ne("id", user.getId()))
 									.list();
 				for(User otherUser : users) {
 	//				for(Map.Entry<Pair<String,String>,Integer> entry : distanceMap.entrySet()) {
 	//					List<Object> trioList = new ArrayList<Object>();
 	//					trioList.add(entry.getKey().t);
 	//					trioList.add(entry.getKey().u);
 	//					trioList.add(entry.getValue().toString());
 	//					System.out.println(entry.getKey().t + "-" + entry.getKey().u + "-" + entry.getValue());
 	//				}
 					Pair<String,String> pair = new Pair<String,String>(user.getUsername(), otherUser.getUsername());
 					if(distanceMap.containsKey(pair) && distanceMap.get(pair) <= SCORE_PROPAGATION_CUTOFF_DISTANCE) {
 						int distance = distanceMap.get(pair);
 						int scoreObtained = 0;
 //						int distance = distanceMap.get(pair);
 //						if(distance<=2) {
 							scoreObtained = SCORE_PER_ITEM/(1<<distance);
 //						}
 //						otherUser.setScore(otherUser.getScore()+scoreObtained);
 						
 						ScoreMessage scoreMessage = new ScoreMessage(MessageServiceImpl.userCacheMap.get(user.getId()), MessageServiceImpl.userCacheMap.get(otherUser.getId()), item, scoreObtained);
 	//					if(!MessageServiceImpl.scoreMessagesMap.containsKey(otherUser.getId())) {
 	//						MessageServiceImpl.scoreMessagesMap.put(otherUser.getId(), new ArrayList<ScoreMessage>());
 	//					}
 						MessageServiceImpl.scoreMessagesMap.get(otherUser.getId()).add(scoreMessage);
 	//					session.update(otherUser);
 					}
 				}
 				
 				//IF Item is replenishable,
 				//Creates another item of the same kind in the same location, SIMULATING the "replenishability" of the item
 				if(item.getItemType().isReplenishable()) {
 					createItem(session, item.getItemType(), itemLocation);
 				}
 				
 				List<ItemType> allItemTypes = (List<ItemType>)session.createCriteria(ItemType.class).list();
 				List<Location> allLocations = (List<Location>)session.createCriteria(Location.class).list();
 				List<Item> allItems = (List<Item>)session.createCriteria(Item.class).list();
 				
 				List<Item> distanceItems = DevServiceImpl.findItemsAtDistances(allItems, user.getCurrentLocation(), 0, DevServiceImpl.MIN_ITEM_DISTANCE-1, allLocations, MessageServiceImpl.locationDistanceMap);
 				List<Location> distanceLocations = DevServiceImpl.locationsAtDistances(user.getCurrentLocation(), DevServiceImpl.MIN_ITEM_DISTANCE, DevServiceImpl.MAX_ITEM_DISTANCE, allLocations, MessageServiceImpl.locationDistanceMap);
 				for(Item distanceItem : distanceItems) {
 					allItemTypes.remove(distanceItem);
 				}
 				//Adds another random itemType to look for on the map.
 				int randomIndex = (int)Math.floor((Math.random()*allItemTypes.size()));
 				ItemType randomItemType = allItemTypes.get(randomIndex);
 				// ensure user is not assigned an item that is present in the current location
 				while (user.getCurrentLocation().getItems().contains(randomItemType)) {
 					randomIndex = (int)Math.floor((Math.random()*allItemTypes.size()));
 					randomItemType = allItemTypes.get(randomIndex);
 				}
 				
 				createGoalItem(session, user, randomItemType);
 				
 				cachePlayer.setCurrentGoalItemType(randomItemType);
 				
 				//Creates a corresponding item in the map (IF it is not replenishable)
 				if(!randomItemType.isReplenishable()) {
 					randomIndex = (int)Math.floor((Math.random()*distanceLocations.size()));
 					Location randomLocation = distanceLocations.get(randomIndex);
 
 					createItem(session, randomItemType, randomLocation);
 				}
 					
 	//			session.update(item);
 				Action takeAction = new Action(user, ActionType.PURCHASE);
 				takeAction.setSourceLocation(user.getCurrentLocation());
 				takeAction.setItem(item);
 				takeAction.setLocationItems(makeIdsString(user.getCurrentLocation().getItems()));
 	//			takeAction.setPrice(0);
 				session.save(takeAction);
 	
 				//***Give the player any new information about the map.
 				//***In this case, the location is updated to reflect that it no longer
 				//***has the item that the player took.
 				MapInformation mapInformation = new MapInformation();
 				mapInformation.locations.add(new LocationDTO(itemLocation));
 	
 				takeResult.setMessage(item.getItemType().getName() + " taken by " + player.getUsername());
 				//***Give the player any new information changed in the player.
 				//***In this method, a new item was added to the inventory.
 				GameStruct gameStruct = new GameStruct(MessageServiceImpl.updateCacheMap(session, user, true),mapInformation);
 				gameStruct.gameStarted = isGameStarted;
 				gameStruct.gameFinished = isGameFinished;
 	
 				takeResult.setGameInformation(gameStruct);
 				takeResult.setSuccess(true);
 
 				tx.commit();
 //				logger.info(user.getUsername() + " has now " + user.getInventory().size() +  " items");
 			}
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": TAKE ITEM FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + " TAKE ITEM FAILED!");
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + " TAKE ITEM FAILED!");
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		} 
 
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " exiting takeItem method - " + itemId);
 //		}
 		
 //		session = PersistenceManager.getSession();
 //		tx = session.beginTransaction();
 //		
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			if(oldScore==user.getScore()) {
 //				logger.error("ERROR! USER'S SCORE WAS NOT UPDATED!!");
 //			}
 //			if(newScore==user.getScore()) {
 //				logger.info("WOW! SCORE WAS UPDATED CORRECTLY??" + newScore + "," + MessageServiceImpl.userCacheMap.get(player.getId()).getScore());
 //			}
 //			if(oldScore==newScore) {
 //				logger.info("oldScore = " + oldScore + " = " + newScore + " = newScore");
 //			}
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 		
 		return takeResult;		
 	}
 
 //	@SuppressWarnings("unchecked")
 	@Override
 	public List<UserDTO> getAllPlayers() throws GeoGameException {
 		List<UserDTO> players = new ArrayList<UserDTO>();
 		
 		if(DevServiceImpl.resetFlag) {
 			throw new AuthorizationException(null, "Game is resetting");
 		}
 		
 		synchronized(MessageServiceImpl.userCacheMap) {
 			for(Map.Entry<Long, UserDTO> entry : MessageServiceImpl.userCacheMap.entrySet()) {
 				UserDTO player = entry.getValue();
 				player.updateSeemsActive();
 	//			player.setScore(player.getScore()+1000);
 	//			if(player.isMoving()) {
 	//				double oldLatitude = player.getLatitude();
 	//				player.updatePosition();
 	//				double newLatitude = player.getLatitude();
 	//				if(oldLatitude==newLatitude) {
 	//					logger.info("weird " + player.getUsername());
 	//				}
 	//			}
 //				if(player.getUsername().equals(debugPlayerUsername)) {
 //					player=player;
 //				}
 				
 				// don't return the admins!
 				if (!player.isAdmin()) {
 					players.add(player);
 				}
 			}
 		}
 		return players;
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			TransactionLog tl = new TransactionLog("getAllPlayers", "");
 //			tl.startTime = System.currentTimeMillis();
 //
 //			List<User> users = (List<User>)session.createCriteria(User.class).list();
 //			for(User user : users) {
 //				AuthenticationUtil.updateLastRequestDate(session, user);
 //				players.add(MessageServiceImpl.updateNewUserDTO(user, true));
 //			}
 //
 //			if(distanceMap==null) {
 //				distanceMap = DevServiceImpl.calculateDistanceMap(users);
 //			}
 //
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 //			tx.rollback();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			ex.printStackTrace();
 //			throw new DBException(ex);
 //		}
 //		return players;
 	}
 	
 	@Override
 	public GameTime getGameTime() throws GeoGameException {
 		GameTime result = new GameTime();
 		result.timeRemaining = endTime - new Date().getTime();
 		result.gameDuration = gameDuration;
 		result.gameStarted = isGameStarted;
 		result.gameFinished = isGameFinished;
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void createUser(String username, String firstName, String lastName, String email, String password, boolean AMTVisitor) throws GeoGameException {
 //		
 		logger.log(org.apache.log4j.Level.INFO, "Creating User" + username );
 		
 		MessageServiceImpl.populateMessagesMaps();
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 //			TransactionLog tl = new TransactionLog("createUser", username);
 //			tl.startTime = System.currentTimeMillis();
 
 			User newUser = (User)session.createCriteria(User.class).add(Restrictions.eq("username", username)).uniqueResult();
 			if(newUser!=null) {
 				throw new DuplicateUsernameException(username);
 			}
 			
 			BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
 			String encryptedPassword = passwordEncryptor.encryptPassword(password);
 			
 			newUser = new User();
 			newUser.setUsername(username);
 			newUser.setPassword(encryptedPassword);
 			newUser.setFirstName(firstName);
 			newUser.setLastName(lastName);
 			newUser.setEmail(email);
 			newUser.setLastRequest(new Date());
 			
 			newUser.setIconFilename(DEFAULT_USER_ICON_FILENAME);
 			newUser.setAMTVisitor(AMTVisitor);
 			if (AMTVisitor) {
 				SecureRandom random = new SecureRandom();
 				String AMTCode = new BigInteger(130, random).toString(32);
 				newUser.setAMTCode(AMTCode);
				newUser.setVerifyCode(java.util.UUID.randomUUID().toString());
 			}
 			
 			List<Location> locations = (List<Location>)session.createCriteria(Location.class).list();
 			Location location = locations.get((int)Math.floor(new Random().nextDouble()*locations.size()));
 			newUser.setCurrentLocation(location);
 			List<ItemType> itemTypes = (List<ItemType>)session.createCriteria(ItemType.class).list();
 			ItemType itemType = itemTypes.get((int)Math.floor(new Random().nextDouble()*itemTypes.size()));
 			createGoalItem(session, newUser, itemType);
 //
 //			newUser.addItemToCollect(itemType);
 			session.save(newUser);
 			
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 			tx.commit();
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 
 		logger.log(org.apache.log4j.Level.INFO, "User Account made for " + username );
 		
 		session = PersistenceManager.getSession();
 		tx = session.beginTransaction();
 		try {
 			logger.log(org.apache.log4j.Level.INFO, "Creating game for " + username );
 			User newUser = (User)session.createCriteria(User.class).add(Restrictions.eq("username", username)).uniqueResult();
 			Long newUserId = newUser.getId();
 			MessageServiceImpl.hasMessagesMap.put(newUserId, Boolean.TRUE);
 			MessageServiceImpl.scoreMessagesMap.put(newUserId, new ArrayList<ScoreMessage>());
 			MessageServiceImpl.updateCacheMap(session, newUser, true);
 			MessageServiceImpl.messagesMap.put(newUserId, new ConcurrentLinkedQueue<MessageDTO>());
 			tx.commit();
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void createUserNetwork(List<UserDTO> playersInNetwork, Double graphDensity) throws GeoGameException {
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		
 		try {
 //			TransactionLog tl = new TransactionLog("createUserNetwork", "");
 //			tl.startTime = System.currentTimeMillis();
 
 			session.createSQLQuery("DELETE FROM user_graph").executeUpdate();
 			
 			if(graphDensity==null) {
 				graphDensity = USER_GRAPH_DENSITY;
 			}
 			for(int i=0;i<playersInNetwork.size()-1;i++) {
 				UserDTO player1 = playersInNetwork.get(i);
 				UserDTO player2 = playersInNetwork.get(i+1);
 				
 //				userGraph.addVertex(player1);
 //				userGraph.addVertex(player2);
 //				userGraph.addEdge(player1, player2);
 				
 				((User)session.get(User.class, player1.getId())).addNeighbor((User)session.get(User.class, player2.getId()));
 				((User)session.get(User.class, player2.getId())).addNeighbor((User)session.get(User.class, player1.getId()));
 				session.saveOrUpdate(session.get(User.class, player1.getId()));
 				session.saveOrUpdate(session.get(User.class, player2.getId()));
 			}
 			
 			if(playersInNetwork.size()>2) {
 				UserDTO player1 = playersInNetwork.get(0);
 				UserDTO player2 = playersInNetwork.get(playersInNetwork.size()-1);
 				((User)session.get(User.class, player1.getId())).addNeighbor((User)session.get(User.class, player2.getId()));
 				((User)session.get(User.class, player2.getId())).addNeighbor((User)session.get(User.class, player1.getId()));
 				
 //				userGraph.addVertex(player1);
 //				userGraph.addVertex(player2);
 //				userGraph.addEdge(player1, player2);
 			}
 			
 			Random r = new Random();
 			for(int i=0;i<playersInNetwork.size();i++) {
 				for(int j=i+2;j<playersInNetwork.size();j++) {
 					if(i==0 && j==playersInNetwork.size()-1) {
 						continue;
 					}
 					if(r.nextDouble()<=graphDensity) {
 						UserDTO player1 = playersInNetwork.get(i);
 						UserDTO player2 = playersInNetwork.get(j);
 						((User)session.get(User.class, player1.getId())).addNeighbor((User)session.get(User.class, player2.getId()));
 						((User)session.get(User.class, player2.getId())).addNeighbor((User)session.get(User.class, player1.getId()));
 						
 //						userGraph.addVertex(player1);
 //						userGraph.addVertex(player2);
 //						userGraph.addEdge(player1, player2);
 						
 						session.saveOrUpdate(session.get(User.class, player1.getId()));
 						session.saveOrUpdate(session.get(User.class, player2.getId()));
 					}
 				}
 			}
 			
 			List<User> users = (List<User>)session.createCriteria(User.class).list();
 			distanceMap = DevServiceImpl.calculateDistanceMap(users);
 
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 			tx.commit();
 			
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			ex.printStackTrace();
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		catch(NullPointerException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			ex.printStackTrace();
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 	}
 
 	private static float averageNetworkDegree(List <User> playersInNetwork) throws GeoGameException {
 		float degree = 0;
 		
 		for (User player : playersInNetwork) {
 			degree += player.getNeighbors().size();
 		}
 		
 		degree /= playersInNetwork.size();
 		
 		return degree;
 	}
 	
 	@SuppressWarnings("unused")
 	private static boolean networkFull (List <User> playersInNetwork) throws GeoGameException {
 		if (playersInNetwork.size() == 1) {
 			return true;
 		}
 		
 		boolean networkFull = true;
 		for (User player : playersInNetwork) {
 			if (player.getNeighbors().size() < playersInNetwork.size() - 1) {
 				networkFull = false;
 				break;
 			}
 		}
 		return networkFull;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void createUserNetwork(Session session, List<User> playersInNetwork, NetworkType networkType, Double graphDensity) throws GeoGameException {
 		session.createSQLQuery("DELETE FROM user_graph").executeUpdate();
 		
 		UndirectedGraph <User, DefaultEdge> newUserGraph = new SimpleGraph<User, DefaultEdge>(DefaultEdge.class);
 		
 		int numPlayers = playersInNetwork.size();
 		
 		if(graphDensity==null) {
 			graphDensity = AVG_NETWORK_DEGREE;
 		}
 		if (graphDensity == 0) {
 			return;
 		}
 		switch(networkType) {
 			case NULL_NETWORK:
 				// do nothing
 				
 				break;
 			case SMALL_WORLD:
 				
 				for(int i=0;i<numPlayers-1;i++) {
 					User player1 = playersInNetwork.get(i);
 					User player2 = playersInNetwork.get(i+1);
 					player1.addNeighbor(player2);
 					player2.addNeighbor(player1);
 					
 					newUserGraph.addVertex(player1);
 					newUserGraph.addVertex(player2);
 					newUserGraph.addEdge(player1, player2);
 				}
 				
 				if(numPlayers>2) {
 					User player1 = playersInNetwork.get(0);
 					User player2 = playersInNetwork.get(numPlayers-1);
 					player1.addNeighbor(player2);
 					player2.addNeighbor(player1);
 					
 					newUserGraph.addVertex(player1);
 					newUserGraph.addVertex(player2);
 					newUserGraph.addEdge(player1, player2);
 				}
 				
 				Random r = new Random();
 				int maxConnections = playersInNetwork.size() * (playersInNetwork.size() - 1) / 2;
 				
 				while ((averageNetworkDegree(playersInNetwork) < graphDensity) && (newUserGraph.edgeSet().size() < maxConnections)) {
 					int player1_ind = r.nextInt(playersInNetwork.size());
 					int player2_ind = r.nextInt(playersInNetwork.size());
 					
 					if (player1_ind != player2_ind) {
 						User player1 = playersInNetwork.get(player1_ind);
 						User player2 = playersInNetwork.get(player2_ind);
 						player1.addNeighbor(player2);
 						player2.addNeighbor(player1);
 						
 						newUserGraph.addVertex(player1);
 						newUserGraph.addVertex(player2);
 						newUserGraph.addEdge(player1, player2);
 					}
 				}
 				
 				logger.info("Player graph constructed.");
 				logger.info("Number of nodes: " + String.valueOf(newUserGraph.vertexSet().size()));
 				logger.info("Number of edges: " + String.valueOf(newUserGraph.edgeSet().size()));
 				logger.info("Max possible connections: " + maxConnections);
 				
 
 				break;
 			case GRID:
 				int shortSide = (int) Math.floor(Math.sqrt(numPlayers));
 				int longSide = shortSide;
 				if(numPlayers>=shortSide*(longSide+1)) {
 					longSide=shortSide+1;
 				}
 				
 				for(int i=0;i<shortSide*longSide;i++) {
 					int col = i/shortSide;
 					int row = i%shortSide;
 					
 					User player = playersInNetwork.get(i);
 					User topPlayer = row==0 ? null : playersInNetwork.get(i-1);
 					User bottomPlayer = row==shortSide-1 ? null : playersInNetwork.get(i+1);
 					User leftPlayer = col==0 ? null : playersInNetwork.get(i-shortSide);
 					User rightPlayer = col==longSide-1 ? null : playersInNetwork.get(i+shortSide);
 					if(topPlayer!=null) {
 						player.addNeighbor(topPlayer);
 					}
 					if(bottomPlayer!=null) {
 						player.addNeighbor(bottomPlayer);
 					}
 					if(leftPlayer!=null) {
 						player.addNeighbor(leftPlayer);
 					}
 					if(rightPlayer!=null) {
 						player.addNeighbor(rightPlayer);
 					}
 				}
 				
 				for(int i=(shortSide*longSide);i<numPlayers;i++) {
 					int col = i-(shortSide*longSide);
 					
 					User player = playersInNetwork.get(i);
 					
 					User bottomPlayer = playersInNetwork.get(shortSide*col);
 					User leftPlayer = col==0 ? null : playersInNetwork.get(i-1);
 					User rightPlayer = i==numPlayers-1 ? null : playersInNetwork.get(i+1);
 					
 					player.addNeighbor(bottomPlayer);
 					bottomPlayer.addNeighbor(player);
 					if(leftPlayer!=null) {
 						player.addNeighbor(leftPlayer);
 					}
 					if(rightPlayer!=null) {
 						player.addNeighbor(rightPlayer);
 					}
 				}
 				//Makes square...
 				
 				//If floorSqrt*(floorSqrt+1)>= numPlayers
 					//Only need to add an extra row to the grid, and done.
 				//Else,
 					//Add a full row
 				break;
 			case LINE:
 				for(int i=0;i<numPlayers-1;i++) {
 					User player1 = playersInNetwork.get(i);
 					User player2 = playersInNetwork.get(i+1);
 					player1.addNeighbor(player2);
 					player2.addNeighbor(player1);
 				}
 				break;
 			case RING:
 				for(int i=0;i<numPlayers-1;i++) {
 					User player1 = playersInNetwork.get(i);
 					User player2 = playersInNetwork.get(i+1);
 					player1.addNeighbor(player2);
 					player2.addNeighbor(player1);
 				}
 				
 				if(numPlayers>2) {
 					User player1 = playersInNetwork.get(0);
 					User player2 = playersInNetwork.get(numPlayers-1);
 					player1.addNeighbor(player2);
 					player2.addNeighbor(player1);
 				}
 				break;
 			case RANDOM:
 				Random r2=new Random();
 				double possibilityConnection = 2/numPlayers;
 				for(int i=0;i<numPlayers-1;i++) {
 					for(int j=0;j<numPlayers-1;j++) {
 						if(r2.nextDouble()>possibilityConnection) {
 							continue;
 						}
 						User player1 = playersInNetwork.get(i);
 						User player2 = playersInNetwork.get(i+1);
 						player1.addNeighbor(player2);
 						player2.addNeighbor(player1);
 					}
 				}
 				break;
 			case HIERARCHY:
 				break;
 		}
 		userGraph = newUserGraph;
 		List<User> users = (List<User>)session.createCriteria(User.class).list();
 		distanceMap = DevServiceImpl.calculateDistanceMap(users);
 	}
 	
 	
 	
 //	@Override
 //	public Integer getSalePrice(UserDTO player, long itemId) throws GeoGameException {
 //		Integer returnPrice = 0;
 //
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			AuthenticationUtil.authenticatePlayer(session, player);
 //			
 //			//TODO: Determine the correct sale price for an item.
 //			//Somehow determine the price of the item
 //			//Right now, randomize a price (it will change between calls,
 //			//which makes it pretty inconsistent.
 //			
 //			//returnPrice = (int) Math.round(Math.random()*10);
 //
 //			Item item = (Item)session.get(Item.class, itemId);
 //			returnPrice = item.getItemType().getBasePrice();
 //			int noise = (int)Math.round((Math.random()-0.5)*returnPrice/10);
 //			returnPrice += noise;
 //
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 //		
 //		return returnPrice;
 //	}
 //
 //	@Override
 //	//TODO: Change this vulnerable signature - price can be easily manipulated...
 //	//This signature MUST be changed - the price is being passed as a parameter, but it is the
 //	//server who should decide how much the price is (ideally by calling the same method as the
 //	//getSalePrice() method should also call.
 //	public ActionResult sellItem(UserDTO player, long itemId, int price) throws GeoGameException {
 //		ActionResult saleResult = new ActionResult();
 //		
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			
 //			Item item = (Item)session.createCriteria(Item.class)
 //				.add(Restrictions.idEq(itemId))
 //				.uniqueResult();
 //			Location location = user.getCurrentLocation();
 //
 //			if(location.getItems().size()==MAX_INVENTORY_SIZE) {
 //				throw new InvalidPurchaseException("Location cannot buy more items");
 //			}
 //
 //			//Sells the item, puts it in the location, and doubles the price necessary
 //			//to buy it back.
 //			user.setScore(user.getScore() + price);
 //			location.getItems().add(item);
 //
 //			int returnPrice = item.getItemType().getBasePrice();
 //			int noise = (int)Math.round((Math.random())*returnPrice/20);
 //			returnPrice += noise;
 //
 ////			item.setPrice(returnPrice);
 //			item.setLocation(location);
 //			user.getInventory().remove(item);
 //			item.setOwner(null);
 //			session.saveOrUpdate(item);
 //			session.saveOrUpdate(user);
 //			
 //			Action saleAction = new Action(user, ActionType.SALE);
 //			saleAction.setSourceLocation(user.getCurrentLocation());
 //			saleAction.setItem(item);
 ////			saleAction.setPrice(price);
 //			session.saveOrUpdate(saleAction);
 //
 //			//***Give the player any new information about the map.
 //			//***In this case, the location is updated to reflect that it has the item
 //			//***the player just sold.
 //			MapInformation mapInformation = new MapInformation();
 //			mapInformation.locations.add(new LocationDTO(location));
 //			
 //			//***Give the player any new information changed in the player.
 //			//***In this method, a new item was added to the inventory.
 //			saleResult.setGameInformation(new GameStruct(new UserDTO(user, true),mapInformation));
 //			saleResult.setSuccess(true);
 //			
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}		
 //		return saleResult;		
 //	}
 
 //	@Override
 //	public ActionResult sellCombo(UserDTO player, long comboId) throws GeoGameException {
 //		ActionResult saleResult = new ActionResult();
 //		
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			Combo combo = (Combo)session.createCriteria(Combo.class)
 //				.add(Restrictions.idEq(comboId))
 //				.uniqueResult();
 //		
 //			int n=combo.getComboType().getItemTypes().size();
 //			for(ItemType itemType : combo.getComboType().getItemTypes()) {
 //				for(Item item : user.getInventory()) {
 //					if(item.getItemType()==itemType) {
 //						n--;
 //						break;
 //					}
 //				}
 //			}
 //			if(n>0) {
 //				throw new InvalidArgsException("Not enough items for the combo!");
 //			}
 //			
 //			for(ItemType itemType : combo.getComboType().getItemTypes()) {
 //				for(Item item : user.getInventory()) {
 //					if(item.getItemType()==itemType) {
 //						//TODO: Make the recycling of the elements more elegant... for now, it is totally random.
 //						//What happens to the items? Are they recycled and thrown away to other random locations? Or are they simply deleted? For now, simply leave them in limbo.
 //						//UPDATE: Items are now placed in another random location in the map.
 //						user.getInventory().remove(item);
 //						item.setOwner(null);
 //						
 //						List<Location> newLocationList = (List<Location>)session.createCriteria(Location.class)
 //							.list();
 //						int newLocationIndex = (int)(Math.random()*newLocationList.size());
 //						Location newLocation = newLocationList.get(newLocationIndex);
 //						item.setLocation(newLocation);
 //						item.setPrice(item.getItemType().getBasePrice());
 //						session.saveOrUpdate(item);
 //						session.saveOrUpdate(newLocation);
 //						break;
 //					}
 //				}
 //			}
 //			
 //			user.setScore(user.getScore() + combo.getPrice());
 //			
 //			session.saveOrUpdate(user);
 //			
 //			Action saleAction = new Action(user, ActionType.SALE);
 //			saleAction.setSourceLocation(user.getCurrentLocation());
 //			saleAction.setCombo(combo);
 //			saleAction.setPrice(combo.getPrice());
 //			session.saveOrUpdate(saleAction);
 //
 //			//***Give the player any new information about the map.
 //			//***In this case, the location is updated but it shouldn't
 //			//***reflect any changes.
 //			MapInformation mapInformation = new MapInformation();
 //			mapInformation.locations.add(new LocationDTO(user.getCurrentLocation()));
 //			
 //			//***Give the player any new information changed in the player.
 //			//***In this method, items were removed from the inventory and gold was increased.
 //			saleResult.setGameInformation(new GameStruct(new UserDTO(user, true),mapInformation));
 //			saleResult.setSuccess(true);
 //			
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}		
 //		return saleResult;
 //	}
 //
 //	@Override
 //	public ActionResult sendTradeOffer(UserDTO player, long player2Id, TradeOfferDTO tradeOfferDTO) throws GeoGameException {
 //		logger.warn(Thread.currentThread().getId() + " -- Player " + player.getUsername() + " is sending a trade offer to: " + player2Id);
 //		
 //		ActionResult result = new ActionResult();
 //		
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			User player2 = (User)session.get(User.class, player2Id);
 //			
 //			//Verify that players have enough money for the trade:
 //			if(tradeOfferDTO.getMoney1()>user.getScore()) {
 //				throw new InvalidArgsException(user.getUsername() + " does not have enough money!");
 //			}
 //			if(tradeOfferDTO.getMoney2()>player2.getScore()) {
 //				throw new InvalidArgsException(player2.getUsername() + " does not have enough money!");
 //			}
 //			//Verify that players have all the items required for the trade:
 //			if(!user.getInventory().containsAll(tradeOfferDTO.getItems1())) {
 //				throw new InvalidArgsException(user.getUsername() + " does not have the required items!");
 //			}
 //			if(!player2.getInventory().containsAll(tradeOfferDTO.getItems2())) {
 //				throw new InvalidArgsException(player2.getUsername() + " does not have the required items!");
 //			}
 //			
 //			TradeOffer staleOffer = (TradeOffer)session.createCriteria(TradeOffer.class)
 //				.add(Restrictions.eq("player1", user))
 //				.add(Restrictions.eq("player2", player2))
 //				.add(Restrictions.eq("status", TradeOffer.Status.PENDING))
 //				.uniqueResult();
 //			
 //			if(staleOffer!=null) {
 //				staleOffer.setStatus(TradeOffer.Status.STALE);
 //				session.saveOrUpdate(staleOffer);
 //			}
 //			
 //			TradeOffer tradeOffer = new TradeOffer();
 //			tradeOffer.setPlayer1(user);
 //			tradeOffer.setPlayer2(player2);
 //			tradeOffer.setMoney1(tradeOfferDTO.getMoney1());
 //			tradeOffer.setMoney2(tradeOfferDTO.getMoney2());
 //			tradeOffer.setItemsString1(Util.getIdsString(tradeOfferDTO.getItems1()));
 //			tradeOffer.setItemsString2(Util.getIdsString(tradeOfferDTO.getItems2()));
 //			tradeOffer.setStatus(TradeOffer.Status.PENDING);
 //			session.saveOrUpdate(tradeOffer);
 //			
 //			result.setGameInformation(new GameStruct(null,null));
 //			tx.commit();
 //			//session.clear();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 //		return result;
 //	}
 //	public ActionResult replyTradeOffer(UserDTO player, long tradeOfferId, boolean accept) throws GeoGameException {
 //		logger.warn(Thread.currentThread().getId() + " -- Player " + player.getUsername() + " is replying to a trade offer with " + accept);
 //		
 //		ActionResult result = new ActionResult();
 //		
 //		Session session = PersistenceManager.getSession();
 //		Transaction tx = session.beginTransaction();
 //		try {
 //			User player2 = AuthenticationUtil.authenticatePlayer(session, player);
 //			TradeOffer tradeOffer = (TradeOffer) session.get(TradeOffer.class, tradeOfferId);
 //			User player1 = tradeOffer.getPlayer1();
 //			
 //			//Verify authenticity of the player replying to the tradeOffer.
 //			if(player.getId()!=player2.getId()) {
 //				throw new AuthorizationException(player, player.getUsername() + " may not reply to trade offer ID#" + tradeOfferId);
 //			}
 //			
 //			if(!accept) {
 //				tradeOffer.setStatus(TradeOffer.Status.REFUSED);
 //				tradeOffer.setDelivered(false);
 //				session.saveOrUpdate(tradeOffer);
 //			} else {
 //				//Verify that players have enough money for the trade:
 //				//TODO: If the trade cannot be completed because of lack of either money or items,
 //				//the system should render the tradeOffer Stale and notify both parties.
 //				if(tradeOffer.getMoney1()>player1.getScore()) {
 //					throw new InvalidArgsException(player1.getUsername() + " does not have enough money!");
 //				}
 //				if(tradeOffer.getMoney2()>player2.getScore()) {
 //					throw new InvalidArgsException(player2.getUsername() + " does not have enough money!");
 //				}
 //				
 //				for(ItemDTO itemDTO : Util.getItemsFromIdsString(session, tradeOffer.getItemsString1())) {
 //					Item item = (Item)session.get(Item.class, itemDTO.getId());
 //					if(!player1.getInventory().contains(item)) {
 //						//Verify that player1 has all the items required for the trade:
 //						throw new InvalidArgsException(player1.getUsername() + "does not have item #" + item.getId());
 //					}
 //					player1.getInventory().remove(item);
 //					player2.addInventory(item);
 //					session.saveOrUpdate(item);
 //				}
 //				for(ItemDTO itemDTO : Util.getItemsFromIdsString(session, tradeOffer.getItemsString2())) {
 //					Item item = (Item)session.get(Item.class, itemDTO.getId());
 //					if(!player2.getInventory().contains(item)) {
 //						//Verify that player2 has all the items required for the trade:
 //						throw new InvalidArgsException(player2.getUsername() + "does not have item #" + item.getId());
 //					}
 //					player2.getInventory().remove(item);
 //					player1.addInventory(item);
 //					session.saveOrUpdate(item);
 //				}
 //
 //				
 //				player2.setScore(player2.getScore() + tradeOffer.getMoney1() - tradeOffer.getMoney2());
 //				player1.setScore(player1.getScore() - tradeOffer.getMoney1() + tradeOffer.getMoney2());
 //				//Execute trade
 //				tradeOffer.setStatus(TradeOffer.Status.ACCEPTED);
 //				session.saveOrUpdate(player2);
 //				session.saveOrUpdate(player1);
 //			}
 //			result.setGameInformation(new GameStruct(new UserDTO(player2, true),null));
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 //		return result;	
 //	}
 
 //	@Override
 //	public ActionResult sendCommand(UserDTO player, String commandString) throws GeoGameException {
 //		logger.warn("Player " + player.getUsername() + " sends a command: " + commandString);
 //		
 //		ActionResult result = new ActionResult();
 //		
 //		return result;
 //	}
 	
 
 //	@Override
 //	public void getSqlDump(UserDTO player) throws GeoGameException, HibernateException, SQLException, IOException {
 //		this.sendLogout(player);
 //		
 //		logger.info("Resetting Database");
 //		Process proc = Runtime.getRuntime().exec("mysqldump -u geogame -pgeogame --database > geogame.sql");
 //		BufferedReader br = new BufferedReader(proc.getOutputStream());
 //	}
 	
 	public static Item createItem(Session session, ItemType itemType, Location location) {
 		Item newItem = new Item();
 		newItem.setItemType(itemType);
 		location.addItem(newItem);
 		newItem.setOwner(null);
 		session.save(newItem);
 		
 		Action newItemAction = new Action(null, ActionType.NEW_ITEM_CREATED);
 		newItemAction.setItem(newItem);
 		newItemAction.setSourceLocation(location);
 		newItemAction.setLocationItems(makeIdsString(location.getItems()));
 		session.save(newItemAction);
 
 		return newItem;
 	}
 
 	public static void createGoalItem(Session session, User user, ItemType itemType) {
 		user.addItemToCollect(itemType);
 //		session.saveOrUpdate(user);
 		session.save(itemType);
 		
 		Action newGoalItemAction = new Action(user, ActionType.NEW_GOAL_ITEM);
 		newGoalItemAction.setItemType(itemType);
 		session.save(newGoalItemAction);
 	}
 	
 	public static boolean hasUserArrived(UserDTO player) {
 //		if(player.isMoving()) {
 //			logger.trace("Evaluating hasUserArrived - new Date is: " + new Date().getTime() + ", moveStart = " + player.getCurrentRoadMovement().getMoveStart().getTime() + ", duration = " + player.getCurrentRoadMovement().getDuration());
 //		}
 		if (player.isMoving() && player.getCurrentRoadMovement() == null) {
 			return true;
 		}
 		if(player.isMoving() && new Date().getTime() > player.getCurrentRoadMovement().getMoveStart().getTime() + player.getCurrentRoadMovement().getDuration()) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean updateUserLocationIfArrived(Session session, User user) {
 		
 		/*if (user.getCurrentRoadMovement() != null) {
 			if (new Date().getTime() > user.getCurrentRoadMovement().getMoveStart().getTime() + user.getCurrentRoadMovement().getDuration()) {
 				user.setCurrentLocation(user.getDestination());
 				user.setLatitude(user.getCurrentLocation().getLatitude());
 				user.setLongitude(user.getCurrentLocation().getLongitude());
 				user.setMoving(false);
 				user.getCurrentRoadMovement().setStatus(Status.COMPLETE);
 				user.setCurrentRoad(null);
 				user.setCurrentRoadMovement(null);
 	//			session.update(user);
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}*/
 		
 		
 		try{
 			if(user.isMoving() && new Date().getTime() > user.getCurrentRoadMovement().getMoveStart().getTime() + user.getCurrentRoadMovement().getDuration()) {
 	//			if(user.getUsername().equals(debugPlayerUsername)) {
 	//				logger.info(user.getUsername() + " arrived at " + user.getDestination().getName());
 	//			}
 				user.setCurrentLocation(user.getDestination());
 				user.setLatitude(user.getCurrentLocation().getLatitude());
 				user.setLongitude(user.getCurrentLocation().getLongitude());
 				user.setMoving(false);
 				user.getCurrentRoadMovement().setStatus(Status.COMPLETE);
 				user.setCurrentRoad(null);
 				user.setCurrentRoadMovement(null);
 				//logger.warn("Set currentRoadMovement to null for " + user.getUsername());
 	//			session.update(user);
 				return true;
 			} else {
 				return false;
 			}
 		} catch(NullPointerException ex) {
 			return false;
 //			user = user;
 		}
 	}
 	
 	public static UserDTO updateUserLocation (UserDTO player) {
 		
 		try {
 			boolean arrivalFlag = new Date().getTime() > 
 										player.getCurrentRoadMovement().getMoveStart().getTime() + 
 										player.getCurrentRoadMovement().getDuration();
 								
 			if (player.isMoving() && arrivalFlag) {
 				player.setCurrentLocation(player.getDestination());
 				player.setLatitude(player.getCurrentLocation().getLatitude());
 				player.setLongitude(player.getCurrentLocation().getLongitude());
 				player.setMoving(false);
 				player.getCurrentRoadMovement().setStatus(RoadMovementDTO.Status.COMPLETE);
 				player.setCurrentRoad(null);
 				player.setCurrentRoadMovement(null);
 			}
 			
 			return player;
 		} catch(NullPointerException ex) {
 			return player;
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<List<Object>> getDistanceMap() throws GeoGameException {
 		if(distanceMap==null) {
 			Session session = PersistenceManager.getSession();
 			Transaction tx = session.beginTransaction();
 			try {
 				List<User> users = (List<User>)session.createCriteria(User.class).list();
 				GameServiceImpl.distanceMap = DevServiceImpl.calculateDistanceMap(users);
 				tx.commit();
 			} catch (JDBCException ex) {
 				tx.rollback();
 				String error_sql = ex.getSQL();
 				if (error_sql != null) {
 					logger.error("The following SQL caused the exception: " + ex.getSQL());
 				}
 			}
 			catch(HibernateException ex) {
 				tx.rollback();
 				throw new DBException(ex);
 			}
 		}
 		List<List<Object>> distanceList = new ArrayList<List<Object>>();
 		for(Map.Entry<Pair<String,String>,Integer> entry : distanceMap.entrySet()) {
 			List<Object> trioList = new ArrayList<Object>();
 			trioList.add(entry.getKey().t);
 			trioList.add(entry.getKey().u);
 			trioList.add(entry.getValue().toString());
 			distanceList.add(trioList);
 		}
 		return distanceList;
 	}
 
 	@Override
 	public Boolean sendFormAcceptance(UserDTO player, long id, Boolean result) throws GeoGameException {
 		logger.log(org.apache.log4j.Level.INFO, "Form#" + id + " being signed by " + player.getUsername() + " to " + result);
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			User user = AuthenticationUtil.authenticatePlayer(session, player);
 			
 			AcceptedForm newlyAcceptedForm = new AcceptedForm();
 			
 			AcceptanceForm acceptanceForm = (AcceptanceForm)session.get(AcceptanceForm.class, id);
 			
 			newlyAcceptedForm.setAcceptanceForm(acceptanceForm);
 			newlyAcceptedForm.setUser(user);
 			newlyAcceptedForm.setTimestamp(new Date());
 			newlyAcceptedForm.setSuccess(result);
 			newlyAcceptedForm.setResultString(result.toString());
 			session.save(newlyAcceptedForm);
 			
 			tx.commit();
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		
 		logger.log(org.apache.log4j.Level.INFO, "Form#" + id + " has been signed by " + player.getUsername() + " to " + result);
 		
 		return true;
 	}
 
 //	@SuppressWarnings("unchecked")
 //	public static List<Location> getAdjacentLocations(Session session, Location location, Map<Location, List<Location>> locationAdjacencyMap) {
 //		List<Road> roads = session.createCriteria(Road.class)
 //			.add(Restrictions.or(
 //					Restrictions.eq("location1.id", location.getId()),
 //					Restrictions.eq("location2.id", location.getId())
 //					)
 //				).list();
 //		List<Location> locations = new ArrayList<Location>();
 //		for(Road road : roads) {
 //			if(road.getLocation1()==location) {
 //				locations.add(road.getLocation2());
 //			} else {
 //				locations.add(road.getLocation1());
 //			}
 //		}
 //		return locations;
 //	}
 	
 	public static String makeIdsString(List<? extends PersistentEntity> entities) {
 		if(entities.isEmpty()) {
 			return "";
 		}
 		StringBuilder idsString = new StringBuilder();
 		for(PersistentEntity entity : entities) {
 			idsString.append(entity.getId() + ",");
 		}
 		idsString.delete(idsString.length()-1,idsString.length());
 		return idsString.toString();
 	}
 
 	public static void setIncomingMessages(ConcurrentLinkedQueue<String> incomingMessages) {
 		GameServiceImpl.incomingMessages = incomingMessages;
 	}
 
 	public static ConcurrentLinkedQueue<String> getIncomingMessages() {
 		return incomingMessages;
 	}
 	
 	public static void addIncomingMessage(String incomingMessage) {
 		GameServiceImpl.incomingMessages.add(incomingMessage);
 	}
 	
 	public static String getNextLoginCommand() {
 		
 		Iterator threadIterator = threadMap.entrySet().iterator();
 		
 		while (threadIterator.hasNext()) {
 			Map.Entry<Integer, TCPThread> currentThread = (Map.Entry) threadIterator.next();
 			String command = currentThread.getValue().getNextLoginCommand();
 		}
 		
 		return null;
 	}
 	
 	
 
 	
 	// static functions for doing stuff with remote agents
 	
 	public static MoveResult moveAgentToLocation(UserDTO player, Long locationId) throws GeoGameException {
 		System.out.println("Move Agent to Location getting called ");
 		if(!isGameStarted) {
 			throw new GameNotStartedException();
 		}
 		if(isGameFinished) {
 			throw new GameEndedException();
 		}
 
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " entering moveToLocation " + locationId);
 //		}
 		
 //		logger.warn
 //		System.out.println("Entered moveToLocation, going to " + locationId);
 		MoveResult moveResult = new MoveResult();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 //		String threadIdString = String.valueOf(Thread.currentThread().getId()) + "," + session.hashCode();
 		try {
 //			TransactionLog tl = new TransactionLog("moveToLocation", player.getUsername());
 //			tl.startTime = System.currentTimeMillis();
 
 //			Counter counter = (Counter)session.get(Counter.class, 1L);
 //			moveResult.setCounter(counter.getCounter());
 //			counter.setCounter(counter.getCounter()+1);
 //			session.saveOrUpdate(counter);
 
 			synchronized(MessageServiceImpl.userCacheMap.get(player.getId())) {
 				User user = AuthenticationUtil.authenticatePlayer(session, player);
 
 				UserDTO cachePlayer = MessageServiceImpl.userCacheMap.get(player.getId());
 				MessageServiceImpl.updateUserWithPlayer(session, user, cachePlayer);
 				
 	
 				updateUserLocationIfArrived(session, user);
 				if(user.getCurrentLocation()==null) {
 					throw new InvalidMoveException("Player cannot move - still moving!");
 				}
 	
 				Location destination = (Location)session.createCriteria(Location.class)
 					.add(Restrictions.idEq(locationId))
 					.uniqueResult();
 				Location sourceLocation = user.getCurrentLocation();
 	
 //				if(player.getUsername().equals(debugPlayerUsername)) {
 //					logger.info(player.getUsername() + " inside moveToLocation transaction, moving to " + destination.getName());
 //				}
 	
 				String travelString = user.getCurrentLocation().getName() + " -> " + destination.getName();
 				
 	//			System.out.println(/*Thread.currentThread().getId() + " -- " + */player.getUsername() + " moving from " + user.getCurrentLocation().getName() + " to " + destination.getName());
 	
 				Road road = (Road)session.createCriteria(Road.class)
 					.add(Restrictions.or(
 							Restrictions.and(
 									Restrictions.eq("location1.id", user.getCurrentLocation().getId()),
 									Restrictions.eq("location2.id", locationId))
 									,
 							Restrictions.and(
 									Restrictions.eq("location1.id", locationId),
 									Restrictions.eq("location2.id", user.getCurrentLocation().getId()))
 									)
 						)
 					.uniqueResult();
 				if(road==null) {
 					throw new InvalidMoveException(/*counter.getCounter() + ": " + threadIdString + */" No road exists: " + travelString);
 				}
 	
 	//			boolean burglarFlag = false;
 	//			int burglarCost = 0;
 	//			if(Math.random()<road.getDanger()) {
 	//				//BURGLAR ATTACK!!
 	//				logger.warn(Thread.currentThread().getId() + " -- " + user.getUsername() + " was robbed!");
 	//				burglarFlag = true;
 	//				burglarCost = (int)(user.getScore()*burglarRatio);
 	//				user.setScore(user.getScore()-burglarCost);
 	//				
 	////				int randomInventoryIndex = (int)Math.floor(Math.random()*user.getInventory().size());
 	////				Item item = user.getInventory().get(randomInventoryIndex);
 	////				user.getInventory().remove(item);
 	//			}
 				
 	//			int movePrice=0;
 	//			switch(road.getRoadType()) {
 	//				case HIGHWAY:
 	//					movePrice = 5;
 	//					break;
 	//				case GOOD:
 	//					movePrice = 10;
 	//					break;
 	//				case BAD:
 	//					movePrice = 25;
 	//					break;
 	//			}
 	//			if(user.getScore()<movePrice) {
 	//				throw new InvalidMoveException(sourceLocation, destination, threadIdString + " -- You need " + movePrice + "G to travel " + travelString + ". Not enough gold!");
 	//			}
 				
 	//			user.setScore(user.getScore()-movePrice);
 	//			double duration = 0;
 	//			int duration = 0;
 				
 	//			Random r = new Random();
 	
 	//			double randomFactor = r.nextGaussian();
 	//			double randomFactor = r.nextGaussian() + 1; 	//Obtains the distance from the center of the normal plus 1
 																		//Distributed by a doubly thick half-bell sliding down from 1.
 																		//Represents the standard delay.
 				
 	//			duration = DELAY_FACTOR*road.getLength()*randomFactor;
 	//			duration = DELAY_FACTOR*road.calculateLength();
 				
 				List<User> users = (List<User>) session.createCriteria(User.class).list();
 				int duration = (int) Math.ceil(road.getTravelDuration() * ( 1 +  ((double)(getUsersTravellingOnTheRoad(users, sourceLocation.getName(), destination.getName())) / users.size())));
 				
 	//			if(r.nextDouble() <= road.getDanger()) {
 	//				duration +=DANGER_ROAD_TRIP_INCREMENT;
 	////				randomFactor *= timeStretches[1];
 	//				//Abnormal delay.
 	////				randomFactor *= 4;
 	//			} else {
 	//				duration+=0;
 	//			}
 	//			if(road.isPenalty()) {
 	//				duration+=PENALTY_DURATION;
 	//			}
 				
 	//			if(duration<=MINIMUM_DURATION) {
 	//				duration = Math.max(MINIMUM_DURATION, duration+MINIMUM_DURATION);
 	//			}
 				
 	//			moveResult.setDuration((int)(/*road.getDanger()**/DELAY_FACTOR*road.getLength()*randomFactor));
 				moveResult.setDuration(duration);
 				moveResult.setSource(new LocationDTO(sourceLocation));
 				moveResult.setDestination(new LocationDTO(destination));
 				
 				user.setCurrentLocation(null);
 				user.setCurrentRoad(road);
 				if(road.getLocation2()==destination) {
 					user.setForward(true);
 				} else {
 					user.setForward(false);
 				}
 	//			user.setMoveStart(new Date());
 				
 				RoadMovement roadMovement = new RoadMovement();
 				roadMovement.setRoad(user.getCurrentRoad());
 				roadMovement.setMoveStart(new Date());
 				roadMovement.setDuration(moveResult.getDuration());
 				roadMovement.setForward(user.getForward());
 				roadMovement.setStatus(Status.ACTIVE);
 				roadMovement.setUser(user);
 				user.setCurrentRoadMovement(roadMovement);
 				session.saveOrUpdate(roadMovement);
 				user.setMoving(true);
 				
 				//Adding mapInformation the player gained...
 				GameStruct gameInformation = new GameStruct();
 				gameInformation.mapInformation = new MapInformation();
 				
 	//			*******	CODE THAT ADDS KNOWN LOCATIONS AND ROADS TO A USER
 	//			*******	COMMENTED OUT NOW BECAUSE EVERYTHING IS KNOWN TO EVERYONE NOW
 	
 	//			List<Road> newRoads = (List<Road>)session.createCriteria(Road.class)
 	//				.add(Restrictions.or(
 	//					Restrictions.eq("location1", destination),
 	//					Restrictions.eq("location2", destination)
 	//					)
 	//				)
 	//				.list();
 	//
 	//			for(Road newRoad : newRoads) {
 	//				if(!user.getKnownRoads().contains(newRoad)) {
 	//					user.addKnownRoad(newRoad);
 	//					gameInformation.mapInformation.roads.add(new RoadDTO(newRoad));
 	//				}
 	//				if(!user.getKnownLocations().contains(newRoad.getLocation1())) {
 	//					user.addKnownLocation(newRoad.getLocation1());
 	//					gameInformation.mapInformation.locations.add(new LocationDTO(newRoad.getLocation1()));
 	//				}
 	//				if(!user.getKnownLocations().contains(newRoad.getLocation2())) {
 	//					user.addKnownLocation(newRoad.getLocation2());
 	//					gameInformation.mapInformation.locations.add(new LocationDTO(newRoad.getLocation2()));
 	//				}
 	//			}
 	//			gameInformation.player = MessageServiceImpl.updateNewUserDTO(session, user, true);
 				
 	//			String userLocation = user.getCurrentLocation()==null ? "NULL" : user.getCurrentLocation().getName();
 	//			logger.info("Inside MoveToLocation - User's location is " + userLocation);
 	//			UserDTO player3 = MessageServiceImpl.userCacheMap.get(user.getId());
 	//			String player3Location = player3.getCurrentLocation()==null ? "NULL" : player3.getCurrentLocation().getName();
 	//			logger.info("Inside MoveToLocation - Player's location is " + player3Location);
 				
 				gameInformation.player = MessageServiceImpl.updateCacheMap(session, user, true);
 	//			player3Location = player3.getCurrentLocation()==null ? "NULL" : player3.getCurrentLocation().getName();
 	//			logger.info("Inside MoveToLocation - Player's location is " + player3Location);
 				
 	//			session.update(user);
 	
 				gameInformation.gameStarted = isGameStarted;
 				gameInformation.gameFinished = isGameFinished;
 	
 	//			System.out.println(/*Thread.currentThread().getId() + " -- " + */player.getUsername() + " moved from " + travelString + "!!");
 							
 				//***Give the player any new information changed in the player.
 				//***In this method, only the location changes.
 				String message = /*threadIdString + " -- "*/"";
 	//			if(burglarFlag) {
 	//				message += "You were robbed!! (-" + burglarCost + " G)\n";
 	//			}
 	//			message += travelString + " is a " + road.getRoadType().toString() + " road." + " Your cost was " + movePrice + "G";
 				message += "Moving " + travelString + "." + " The trip will last " + Math.rint(moveResult.getDuration()/100)/10 + "s";
 				moveResult.setMessage(message);
 	//			System.out.println(message);
 				
 				moveResult.setGameInformation(gameInformation);
 				
 				moveResult.setSuccess(true);
 				
 	//			System.out.println(/*Thread.currentThread().getId() + " -- " + */user.getUsername() + "'s move successful");
 				
 				Action moveAction = new Action(user, ActionType.MOVE);
 				moveAction.setSourceLocation(sourceLocation);
 				moveAction.setDestinationLocation(destination);
 	//			moveAction.setPrice(movePrice);
 	//			moveAction.setBurglary(burglarFlag);
 	//			moveAction.setBurglaryCost(burglarCost);
 				session.save(moveAction);
 	
 //				if(player.getUsername().equals(debugPlayerUsername)) {
 //					logger.info(player.getUsername() + " exiting moveToLocation transaction, moving to " + destination.getName());
 //				}
 	
 //				tl.endTime = System.currentTimeMillis();
 //				transactionLog.add(tl);
 				tx.commit();
 			}
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		} catch(HibernateException ex) {
 			tx.rollback();
 			System.out.println(player.getUsername() + " MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			System.out.println(player.getUsername() + " MOVE FAILED!");
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		
 //		if(player.getUsername().equals(debugPlayerUsername)) {
 //			logger.info(player.getUsername() + " exiting moveToLocation method");
 //		}
 //		
 //		session = PersistenceManager.getSession();
 //		tx = session.beginTransaction();
 //		
 //		try {
 //			User user = AuthenticationUtil.authenticatePlayer(session, player);
 //			if(!user.isMoving()) {
 //				logger.error("ERROR! USER JUST STARTED TO MOVE AND IS 'NOT MOVING'!!");
 //			}
 //			tx.commit();
 //		} catch(HibernateException ex) {
 //			tx.rollback();
 //			logger.error(ex.getMessage(), ex);
 //			throw new DBException(ex);
 //		}
 		
 		return moveResult;
 	}
 	
 	public static LocationDTO getAgentLocationInformation(UserDTO player, Long locationId) throws GeoGameException {
 //		return GameLogic.getInstance().getLocationInformation(player, locationId);
 		
 		LocationDTO locationInformation;
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {	
 //			TransactionLog tl = new TransactionLog("getLocationInformation", player.getUsername());
 //			tl.startTime = System.currentTimeMillis();
 
 			User user = AuthenticationUtil.authenticatePlayer(session, player);
 			
 			Location location = (Location)session.createCriteria(Location.class)
 				.add(Restrictions.idEq(locationId))
 				.uniqueResult();
 			
 			if(location!=user.getCurrentLocation()) {
 				throw new InvalidArgsException("ERR - " + player.getUsername() + " is not currently in " + location.getName());
 			}
 
 			locationInformation = new LocationDTO(location);
 
 //			session.update(user);
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 			tx.commit();
 		} catch (JDBCException ex) {
 			tx.rollback();
 			logger.error(player.getUsername() + ": GET LOCATION INFORMATION FAILED!");
 			logger.error(ex.getMessage(), ex);
 			String error_sql = ex.getSQL();
 			if (error_sql != null) {
 				logger.error("The following SQL caused the exception: " + ex.getSQL());
 			}
 			throw new DBException(ex);
 		}catch(HibernateException ex) {
 			tx.rollback();
 			logger.warn(ex.getMessage());
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} catch(NullPointerException ex) {
 			tx.rollback();
 			logger.warn(ex.getMessage());
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		} 
 		
 		return locationInformation;
 	}
 	
 	public static GameStruct getAgentGameInformation(UserDTO player) throws GeoGameException {
 //		logger.error("MESSAGE GETTING GAME INFORMATION!!");
 		// Add message to list
 		if(player!=null) {
 			logger.warn(/*Thread.currentThread().getId() + */"Player " + player.getUsername() + " requests map information");
 		}
 
 		GameStruct gameInformation = new GameStruct();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 //			if(player!=null) {
 //				TransactionLog tl = new TransactionLog("getGameInformation", player.getUsername());
 //				tl.startTime = System.currentTimeMillis();
 //			}
 			
 			//FIXME: This is a hack used to allow the MapOverviewer to get map information without authenticating.
 			User user = null;
 			if(player!=null) {
 				user = AuthenticationUtil.authenticatePlayer(session, player);
 			}
 			
 			//Determine which parts of the map does the user have access to see
 			//For now, send all information about roads and locations
 			
 //			List<Location> locations = (List<Location>)session.createCriteria(Location.class)
 //				.list();
 //			List<Road> roads = (List<Road>)session.createCriteria(Road.class)
 //				.list();
 			
 			List<Location> locations;
 			List<Road> roads;
 //			if(user.isAdmin()) {
 				locations = (List<Location>)session.createCriteria(Location.class).list();
 				roads = (List<Road>)session.createCriteria(Road.class).list();
 //			} else {
 //				locations = user.getKnownLocations();
 //				roads = user.getKnownRoads();
 //			}
 			MapInformation mapInformation = new MapInformation();
 			for(Location location : locations) {
 				mapInformation.locations.add(new LocationDTO(location));
 			}
 			for(Road road : roads) {
 				mapInformation.roads.add(new RoadDTO(road));
 			}
 			//FIXME: This is a hack used to allow the MapOverviewer to get map information without authenticating.
 			if(user!=null) {
 				gameInformation.player = MessageServiceImpl.updateCacheMap(session, user, true);
 			}
 			gameInformation.mapInformation = mapInformation;
 			gameInformation.timeRemaining = endTime - new Date().getTime();
 			gameInformation.gameDuration = gameDuration;
 			
 			gameInformation.gameStarted = isGameStarted;
 			gameInformation.gameFinished = isGameFinished;
 			
 //			session.update(user);
 //			tl.endTime = System.currentTimeMillis();
 //			transactionLog.add(tl);
 			tx.commit();
 		}
 		catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		catch (NullPointerException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		return gameInformation;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<ItemTypeDTO> getAllItemTypes() throws GeoGameException {
 		// TODO Auto-generated method stub
 		List<ItemTypeDTO> items = new ArrayList<ItemTypeDTO>();
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		
 		try {
 			List<ItemType> allItemTypes = (List<ItemType>)session.createCriteria(ItemType.class).list();
 			
 			for (ItemType newItemType : allItemTypes) {
 				items.add(new ItemTypeDTO(newItemType));
 			}
 			
 		} catch(HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		
 		return items;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setSynonyms(List<ItemTypeDTO> itemTypes) throws GeoGameException {
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			List<ItemType> allItemTypes = (List<ItemType>)session.createCriteria(ItemType.class).list();
 			session.createQuery("DELETE FROM Synonym").executeUpdate();
 			
 			if (itemTypes.size() == allItemTypes.size()) {
 				for (int i = 0; i < itemTypes.size(); i++) {
 					System.out.println(itemTypes.get(i).getName());
 					System.out.println(itemTypes.get(i).getSynonymNames());
 					List<Synonym> synList = new ArrayList<Synonym>();
 					for (String synName : itemTypes.get(i).getSynonymNames()) {
 						Synonym newSyn = new Synonym();
 						newSyn.setSynonym(synName);
 						synList.add(newSyn);
 					}
 					allItemTypes.get(i).setSynonyms(synList);
 					session.update(allItemTypes.get(i));
 				}
 			}
 			tx.commit();
 		} catch (HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		logger.warn("setSynonyms called");
 		return;
 	}
 	
 	public ServerSettingsResult sendServerSettings(ServerSettingsStructDTO serverSettings) throws GeoGameException {
 
 		ServerSettingsResult result = new ServerSettingsResult();
 		GameServiceImpl.serverSettings = serverSettings;
 		result.setSuccess(false);
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			ServerSettingsStruct newServerSettings = new ServerSettingsStruct();
 			newServerSettings.setGameDuration(serverSettings.getGameDuration());
 			newServerSettings.setGraphDensity(serverSettings.getGraphDensity());
 			newServerSettings.setMaxRoadTime(serverSettings.getMaxRoadTime());
 			newServerSettings.setMinRoadTime(serverSettings.getMinRoadTime());
 			newServerSettings.setNetworkType(serverSettings.getNetworkType());
 			newServerSettings.setNewItemAssignment(serverSettings.isNewItemAssignment());
 			newServerSettings.setRebuildRoadNetwork(serverSettings.isRebuildRoadNetwork());
 			newServerSettings.setRebuildUserNetwork(serverSettings.isRebuildUserNetwork());
 			newServerSettings.setCommAllowed(serverSettings.getCommAllowed());
 			newServerSettings.setPeriodicGame(serverSettings.isPeriodicGame());
 			newServerSettings.setGameInterval(serverSettings.getGameInterval());
 			
 			session.save(newServerSettings);
 			tx.commit();
 		} catch (HibernateException ex) {
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		
 		result.setSuccess(true);
 		return result;
 	}
 	
 	public ServerSettingsStructDTO getServerSettings() throws GeoGameException {
 		
 		ServerSettingsStructDTO serverSettings; // = new ServerSettingsStructDTO();
 		
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		try {
 			ServerSettingsStruct currentServerSettings = (ServerSettingsStruct)session.createCriteria(ServerSettingsStruct.class).addOrder(Order.desc("created")).list().get(0);
 			serverSettings = new ServerSettingsStructDTO(currentServerSettings);
 		} catch (HibernateException ex) {
 			serverSettings = null;
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			throw new DBException(ex);
 		}
 		return serverSettings;
 	}
 	
 	public String userGraphToJSON () {
 		
 		if (userGraph.vertexSet().isEmpty()) return "";
 		
 		HashSet<HashSet<User>> userPairs = new HashSet<HashSet<User>>();
 		
 		String graphJSON = "";
 		List<String> userObjects = new ArrayList<String>();
 		
 		for (User user : userGraph.vertexSet()) {
 			
 			List<String> adjacencies = new ArrayList<String>();
 			
 			for (DefaultEdge edge : userGraph.edgesOf(user)) {
 				User otherUser = userGraph.getEdgeTarget(edge);
 				if (otherUser.equals(user)) {
 					otherUser = userGraph.getEdgeSource(edge);
 				}
 				HashSet<User> userPair = new HashSet<User>();
 				userPair.add(user);
 				userPair.add(otherUser);
 				
 				if (!userPairs.contains(userPair)) {
 					adjacencies.add("\"" + otherUser.getUsername() + "\"");
 					userPairs.add(userPair);
 				}
 			}
 			
 			if (!adjacencies.isEmpty()) {
 				String userJSON = "";
 				userJSON = userJSON.concat("{ \"id\" : \"" + user.getUsername() + "\", ");
 				userJSON = userJSON.concat("\"name\" : " + "\"" + user.getUsername() + "\", ");
 				userJSON = userJSON.concat("\"adjacencies\" : [");
 				userJSON = userJSON.concat(StringUtils.join(adjacencies, ","));
 				userJSON = userJSON.concat(" ]} ");
 				userObjects.add(userJSON);
 			}
 		}
 		if (!userObjects.isEmpty()) {
 			graphJSON = "[ " + StringUtils.join(userObjects, ",") + " ]";
 		}
 		
 		logger.info("Graph converted to JSON string.");
 		return graphJSON;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public HashMap<String, String> getItemNameSynPairsForUser(long user) throws GeoGameException {
 		Session session = PersistenceManager.getSession();
 		Transaction tx = session.beginTransaction();
 		HashMap<String, String> itemNameSynPairs = new HashMap<String, String>();
 		try {
 			
 			List<String> itemName =  (List<String>)session.createSQLQuery("SELECT item_type_name FROM syns_per_user WHERE user_id = " + user).list();
 			List<String> assignedSyn = (List<String>)session.createSQLQuery("SELECT assigned_syn FROM syns_per_user WHERE user_id = " + user).list();
 
 			for(int i=0; i < itemName.size(); i++)
 			{
 				itemNameSynPairs.put(itemName.get(i), assignedSyn.get(i));
 			}
 			
 		} catch(HibernateException ex) {
 			System.out.println("Error in getItemNameSynPairsForUser Function");
 			tx.rollback();
 			logger.error(ex.getMessage(), ex);
 			StackTraceElement elements[] = ex.getStackTrace();
 			for (StackTraceElement element: elements) {
 				logger.error(element.getMethodName() + element.getLineNumber());
 			}
 			throw new DBException(ex);
 		}
 		return itemNameSynPairs;	
 	}
 	
 }
