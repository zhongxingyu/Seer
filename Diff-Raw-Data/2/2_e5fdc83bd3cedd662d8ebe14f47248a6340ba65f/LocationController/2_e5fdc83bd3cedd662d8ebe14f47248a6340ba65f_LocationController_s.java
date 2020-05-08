 package com.game.rania.controller;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.model.Location;
 import com.game.rania.model.ParallaxLayer;
 import com.game.rania.model.ParallaxObject;
 import com.game.rania.model.Planet;
 import com.game.rania.model.Player;
 import com.game.rania.model.Radar;
 import com.game.rania.model.Star;
 import com.game.rania.model.User;
 import com.game.rania.model.element.Group;
 import com.game.rania.model.element.RegionID;
 import com.game.rania.view.MainView;
 
 public class LocationController {
 	
 	private MainView mView = null;
 	private MainController mController = null;
 	private ClientController cController = null;
 	
 	public LocationController(MainController mController, MainView mView, ClientController cController) {
 		this.mView = mView;
 		this.mController = mController;
 		this.cController = cController;
 	}
 	
 	public void loadTextures(){
 		for (int i = 0; i < 18; i++)
 			mView.loadTexture("data/location/planets.png", RegionID.fromInt(RegionID.PLANET_0.ordinal() + i), i % 5 * 204, i / 5 * 204, 204, 204);
 		
 		for (int i = 0; i < 8; i++)
			mView.loadTexture("data/backgrounds/nebulas(512x512).png", RegionID.fromInt(RegionID.NEBULA_0.ordinal() + i), i % 4 * 512, i / 4 * 512, 512, 512);
 
 		mView.loadTexture("data/location/star.png",        RegionID.STAR);
 		mView.loadTexture("data/location/radar.png",       RegionID.RADAR);
 		mView.loadTexture("data/location/sensor.png",      RegionID.RADAR_SENSOR);
 		mView.loadTexture("data/location/radarObject.png", RegionID.RADAR_OBJECT);
 		mView.loadTexture("data/location/SpaceShip.png",   RegionID.SHIP);
 		mView.loadTexture("data/backgrounds/space.png",    RegionID.BACKGROUND_SPACE);
 		mView.loadTexture("data/backgrounds/stars.png",    RegionID.BACKGROUND_STARS);
 	}
 
 	//list objects
 	private HashMap<Integer, Location> locations = null;
 	private HashMap<Integer, Planet>   planets   = new HashMap<Integer, Planet>();
 	private HashMap<Integer, User> 	   users     = null;
 	//objects
 	private Player   player 		 = null;
 	private Group	 background		 = null;
 	private Radar    radar  		 = null;
 	private Star	 star 			 = null;
 	private	Location currentLocation = null;
 	//help objects
 	private ShipController pController = null;
 	
 	public void clearObjects(){
 		removePlayer();
 		removeBackground();
 		removeRadar();
 		removePlanets();
 		removeUsers();
 	}
 
 	public void loadLocations() {
 		locations = cController.getLocationList();
 	}
 	
 	//player
 	public boolean loadPlayer(){
 		player = cController.getPlayerData();
 		if (player == null)
 			return false;
 		currentLocation = getNearLocation();
 		if (currentLocation == null)
 			return false;
 		return true;
 	}
 	
 	public void setPlayer(Player newPlayer){
 		removePlayer();
 		player = newPlayer;
 		addPlayer();
 	}
 	
 	public void addPlayer(){
 		if (player != null)
 		{
 			mController.addObject(player);
 			pController = new ShipController(player);
 			mController.addProcessor(pController);
 		}
 	}
 	
 	public void removePlayer(){
 		if (player != null) {
 			mController.removeObject(player);
 			if (pController != null)
 				mController.removeProcessor(pController);
 			player = null;
 		}
 	}
 	
 	//background
 	public void loadBackground(){
 		background = new Group();
 		background.addElement(new ParallaxLayer(RegionID.BACKGROUND_SPACE, 250, 300, -0.35f));
 		background.addElement(new ParallaxLayer(RegionID.BACKGROUND_STARS, -150, 0, -0.25f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_1, 500, 500, 45, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_2, -500, 500, -45, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_3, 500, -500, 0, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_4, -500, -500, 200, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_5, 1500, 1500, 45, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_6, -1500, 1500, -45, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_7, 1500, -1500, 0, 2, 2, -0.4f));
 		background.addElement(new ParallaxObject(RegionID.NEBULA_0, -1500, -1500, 200, 2, 2, -0.4f));
 	}
 	
 	public void setBackground(Group newBackground){
 		removeBackground();
 		background = newBackground;
 		addBackground();
 	}
 	
 	public void addBackground(){
 		if (background != null)
 			mController.addObject(background);
 	}
 
 	public void removeBackground(){
 		if (background != null) {
 			mController.removeObject(background);
 			background = null;
 		}
 	}
 	
 	//radar
 	public void loadRadar(){
 		radar = new Radar(player,
 						 (mView.getHUDCamera().getWidth()  - mView.getTextureRegion(RegionID.RADAR).getRegionWidth())  * 0.5f,
 						 (mView.getHUDCamera().getHeight() - mView.getTextureRegion(RegionID.RADAR).getRegionHeight()) * 0.5f,
 						  2000);
 	}
 	
 	public void setRadar(Radar newRadar){
 		removeRadar();
 		radar = newRadar;
 		addRadar();
 	}
 	
 	public void addRadar(){
 		if (radar != null)
 			mController.addHUDObject(radar);
 	}
 
 	public void removeRadar(){
 		if (radar != null) {
 			mController.removeObject(radar);
 			radar = null;
 		}
 	}
 	
 	//planets
 	public void loadPlanets(){
 		if (currentLocation == null)
 			return;
 		if (currentLocation.star == null)
 			currentLocation.star = new Star(currentLocation.starType, currentLocation.x, currentLocation.y, currentLocation.starRadius);
 		if (currentLocation.planets == null)
 			currentLocation.planets = cController.getPlanetList(currentLocation);
 		star = currentLocation.star;
 		planets.putAll(currentLocation.planets);
 	}
 
 	public void addPlanets(){
 		mController.addObject(star);
 		for (Planet planet : planets.values()) {
 			mController.addObject(planet);
 		}
 	}
 
 	public void removePlanets(){
 		if (star != null) {
 			mController.removeObject(star);
 			star = null;
 		}
 		for (Planet planet : planets.values()) {
 			mController.removeObject(planet);
 		}
 		planets.clear();
 	}
 
 	public void addPlanet(Planet planet){
 		if (planets.containsKey(planet.id))
 			return;
 		planets.put(planet.id, planet);
 		mController.addObject(planet);
 	}
 	
 	public void removePlanet(Planet planet){
 		if (!planets.containsKey(planet.id))
 			return;
 		planets.remove(planet.id);
 		mController.removeObject(planet);
 	}
 
 	public void removePlanet(int id){
 		Planet planet = planets.get(id);
 		if (planet == null)
 			return;
 		planets.remove(id);
 		mController.removeObject(planet);
 	}
 	
 	//users
 	public void loadUsers(){
 		users = cController.getUsersList();
 	}
 	
 	public void updateUsers(){
 		removeUsers();
 		users = cController.getUsersList();
 		addUsers();
 	}
 	
 	public void addUsers(){
 		for (User user : users.values()) {
 			mController.addObject(user);
 		}
 	}
 
 	public void removeUsers(){
 		for (User user : users.values()) {
 			mController.removeObject(user);
 		}
 		users.clear();
 	}
 
 	public void addUser(User user) {
 		if (users.containsKey(user.id))
 			return;
 		users.put(user.id, user);
 		mController.addObject(user);
 	}
 	
 	public void removeUser(User user) {
 		if (!users.containsKey(user.id))
 			return;
 		users.remove(user.id);
 		mController.removeObject(user);
 	}
 	
 	public void removeUser(int id) {
 		User user = users.get(id);
 		if (user == null)
 			return;
 		users.remove(id);
 		mController.removeObject(user);
 	}
 	
 	//get objects
 	public Player getPlayer(){
 		return player;
 	}
 	
 	public Radar getRadar(){
 		return radar;
 	}
 
 	public Star getStar(){
 		return star;
 	}
 
 	//get locations
 	public Collection<Location> getLocations(){
 		return locations.values();
 	}
 
 	public Location getLocation(int id){
 		return locations.get(id);
 	}
 	
 	private Vector2 distanceVec = new Vector2();
 	private float distanceBuffer = 0.0f;
 	
 	public Location getCurrentLocation(){
 		return currentLocation;
 	}
 	
 	public Location getNearLocation(){
 		Location nearLocation = null;
 		float distance = Float.MAX_VALUE;
 		
 		for (Location location : locations.values()) {
 			distanceVec.set(location.x - player.position.x, location.y - player.position.y);
 			distanceBuffer = distanceVec.len();
 			if (distance > distanceBuffer) {
 				distance = distanceBuffer;
 				nearLocation = location;
 			}
 		}
 		
 		return nearLocation;
 	}
 
 	public Location getLocation(int x, int y){
 		for (Location loc : locations.values()) {
 			if (loc.x == x && loc.y == y)
 				return loc;
 		}
 		return null;
 	}
 	
 	//get planets
 	public Collection<Planet> getPlanets(){
 		return planets.values();
 	}
 	
 	public Collection<Planet> getPlanets(int idLocation){
 		Location location = locations.get(idLocation);
 		if (location == null)
 			return null;
 		if (location.planets == null)
 			location.planets = cController.getPlanetList(location);
 		return location.planets.values();
 	}
 	
 	public Planet getPlanet(int id){
 		return planets.get(id);
 	}
 
 	public Planet getPlanet(String name){
 		for (Planet planet : planets.values()) {
 			if (planet.name.compareTo(name) == 0)
 				return planet;
 		}
 		return null;
 	}
 	
 	//get users
 	public Collection<User> getUsers(){
 		return users.values();
 	}
 	
 	public User getUser(int id){
 		return users.get(id);
 	}
 	
 	public User getPilot(String name){		
 		for (User user : users.values()) {
 		if (user.pilotName.compareTo(name) == 0)
 			return user;
 		}
 		return null;
 	}
 	
 	public User getShip(String name){		
 		for (User user : users.values()) {
 		if (user.shipName.compareTo(name) == 0)
 			return user;
 		}
 		return null;
 	}
 	
 	private float updateTime = 0.0f;
 	public void update(float deltaTime){
 		updateTime += deltaTime;
 		if (updateTime > 1.0f){
 			Location newLocation = getNearLocation();
 			if (newLocation.id != currentLocation.id){
 				removePlanets();
 				currentLocation = newLocation;
 				loadPlanets();
 				addPlanets();
 			}
 			updateTime -= 1.0f;
 		}
 	}
 }
