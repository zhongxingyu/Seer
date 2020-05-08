 package com.game.rania.controller;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Vector;
 
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.math.Vector2;
 import com.game.rania.Config;
 import com.game.rania.model.Group;
 import com.game.rania.model.RegionID;
 import com.game.rania.model.element.Domain;
 import com.game.rania.model.element.Location;
 import com.game.rania.model.element.Nebula;
 import com.game.rania.model.element.ParallaxLayer;
 import com.game.rania.model.element.Planet;
 import com.game.rania.model.element.Player;
 import com.game.rania.model.element.Radar;
 import com.game.rania.model.element.Star;
 import com.game.rania.model.element.User;
 import com.game.rania.model.items.ItemCollection;
 import com.game.rania.net.NetController;
 import com.game.rania.view.MainView;
 
 public class LocationController
 {
   public boolean         active      = false;
   private MainView       mView       = null;
   private MainController mController = null;
   private NetController  nController = null;
 
   public LocationController(MainController mController, MainView mView, NetController cController)
   {
     this.mView = mView;
     this.mController = mController;
     this.nController = cController;
   }
 
   public void loadTextures()
   {
     for (int i = 0; i < 18; i++)
       mView.loadTexture("data/location/planets.png", RegionID.fromInt(RegionID.PLANET_0.ordinal() + i), i % 5 * 204, i / 5 * 204, 204, 204);
 
     for (int i = 0; i < 8; i++)
       mView.loadTexture("data/backgrounds/nebulas.png", RegionID.fromInt(RegionID.NEBULA_0.ordinal() + i), i % 4 * 512, i / 4 * 512, 512, 512);
 
     mView.loadTexture("data/location/boom.png");
 
     mView.loadTexture("data/location/clouds.png", RegionID.CLOUDS);
     mView.getTexture(RegionID.CLOUDS).setWrap(TextureWrap.Repeat, TextureWrap.Repeat);
     mView.loadTexture("data/location/star.png", RegionID.STAR);
     mView.loadTexture("data/location/radar.png", RegionID.RADAR);
     mView.loadTexture("data/location/sensor.png", RegionID.RADAR_SENSOR);
     mView.loadTexture("data/location/radarObject.png", RegionID.RADAR_OBJECT);
     mView.loadTexture("data/location/SpaceShip.png", RegionID.SHIP);
     mView.loadTexture("data/location/target.png", RegionID.TARGET);
     mView.loadTexture("data/backgrounds/space.png", RegionID.BACKGROUND_SPACE);
     mView.loadTexture("data/backgrounds/stars.png", RegionID.BACKGROUND_STARS);
 
     mView.loadTexture("data/ammunition/laser.png", RegionID.LASER);
     mView.loadTexture("data/ammunition/rocket.png", RegionID.ROCKET);
     mView.loadTexture("data/ammunition/repair.png", RegionID.REPAIR);
     mView.loadTexture("data/ammunition/bfg.png", RegionID.BFG);
   }
 
   // list objects
   private HashMap<Integer, Location> locations       = new HashMap<Integer, Location>();
   private HashMap<Integer, Domain>   domains         = null;
   private HashMap<Integer, Nebula>   nebulas         = null;
   private HashMap<Integer, Planet>   planets         = new HashMap<Integer, Planet>();
   private HashMap<Integer, User>     users           = new HashMap<Integer, User>();
   private ItemCollection             items           = null;
   // objects
   private Player                     player          = null;
   private Group                      background      = null;
   private Radar                      radar           = null;
   private Star                       star            = null;
   private Location                   currentLocation = null;
   private Vector<Nebula>             showNebulas     = new Vector<Nebula>();
   // help objects
   private PlayerController           pController     = null;
 
   public void clearObjects()
   {
     active = false;
     player.stop();
     removePlayer();
     removeBackground();
     removeRadar();
     removePlanets();
    removeNebulas();
     removeUsers();
   }
 
   public void addLocation(Location location)
   {
     if (locations.containsKey(location.id))
       return;
     locations.put(location.id, location);
     if (player != null)
       switchLocation(getNearLocation());
   }
 
   public void loadDomains()
   {
     if (domains == null)
     {
       domains = nController.getAllDomains();
     }
     else
     {
       for (Location location : locations.values())
       {
         if (location.star != null)
         {
           location.star.reloadTexture();
           for (Planet planet : location.planets.values())
           {
             planet.reloadTexture();
           }
         }
       }
     }
   }
 
   public void loadItems()
   {
     if (items == null)
     {
       items = nController.getItems();
     }
   }
 
   public ItemCollection getItems()
   {
     return items;
   }
 
   // player
   public boolean loadPlayer()
   {
     player = nController.getUserData();
     currentLocation = getNearLocation();
     if (currentLocation == null)
       return false;
     return true;
   }
 
   public void setPlayer(Player newPlayer)
   {
     removePlayer();
     player = newPlayer;
     addPlayer();
   }
 
   public void addPlayer()
   {
     if (player != null)
     {
       mController.addObject(player);
       pController = new PlayerController(player);
       mController.addProcessor(pController);
     }
   }
 
   public void removePlayer()
   {
     if (player != null)
     {
       mController.removeObject(player);
       if (pController != null)
         mController.removeProcessor(pController);
       player = null;
     }
   }
 
   public PlayerController getPlayerController()
   {
     return pController;
   }
 
   // background
   public void loadBackground()
   {
     background = new Group();
     background.addElement(new ParallaxLayer(RegionID.BACKGROUND_SPACE, 250, 300, -0.35f));
     background.addElement(new ParallaxLayer(RegionID.BACKGROUND_STARS, -150, 0, -0.25f));
   }
 
   public void setBackground(Group newBackground)
   {
     removeBackground();
     background = newBackground;
     addBackground();
   }
 
   public void addBackground()
   {
     if (background != null)
       mController.addObject(background);
   }
 
   public void removeBackground()
   {
     if (background != null)
     {
       mController.removeObject(background);
       background = null;
     }
   }
 
   // nebulas
   public void loadNebulas()
   {
     if (nebulas == null)
       nebulas = nController.getAllNebulas();
    else
    {
      for(Nebula nebula : nebulas.values())
      {
        nebula.reloadTexture();
      }
    }
   }
 
   public void addNebulas()
   {
     updateNearNebulas();
   }
 
   public void removeNebulas()
   {
     for (Nebula nebula : showNebulas)
     {
       mController.removeObject(nebula);
     }
     showNebulas.clear();
   }
 
   public void updateNearNebulas()
   {
     if (player == null)
       return;
 
     for (Nebula nebula : nebulas.values())
     {
       distanceVec.set(nebula.parallaxPosition.x - player.position.x, nebula.parallaxPosition.y - player.position.y);
       if (distanceVec.len() < nebula.getMaxSize() * 0.5f + Config.nebulaRadius)
       {
        if (!showNebulas.contains(nebula))
        {
          mController.addObject(nebula);
          showNebulas.add(nebula);
        }
       }
       else if (showNebulas.contains(nebula))
       {
         mController.removeObject(nebula);
         showNebulas.remove(nebula);
       }
 
     }
   }
 
   // radar
   public void loadRadar()
   {
     radar = new Radar(player,
                       (mView.getHUDCamera().getWidth() - mView.getTextureRegion(RegionID.RADAR).getRegionWidth()) * 0.5f,
                       (mView.getHUDCamera().getHeight() - mView.getTextureRegion(RegionID.RADAR).getRegionHeight()) * 0.5f);
   }
 
   public void setRadar(Radar newRadar)
   {
     removeRadar();
     radar = newRadar;
     addRadar();
   }
 
   public void addRadar()
   {
     if (radar != null)
       mController.addHUDObject(radar);
   }
 
   public void removeRadar()
   {
     if (radar != null)
     {
       mController.removeObject(radar);
       radar = null;
     }
   }
 
   // planets
   public void loadPlanets()
   {
     if (currentLocation == null)
       return;
 
     if (currentLocation.star == null)
     {
       currentLocation.star = new Star(currentLocation.id,
                                       currentLocation.starName,
                                       currentLocation.starType,
                                       currentLocation.x,
                                       currentLocation.y,
                                       currentLocation.starRadius);
     }
 
     star = currentLocation.star;
 
     if (currentLocation.planets == null)
       currentLocation.planets = nController.getPlanets(currentLocation.id, true);
     planets = currentLocation.planets;
   }
 
   public void switchLocation(Location newLocation)
   {
     if (newLocation == currentLocation)
       return;
 
     removePlanets();
     currentLocation = newLocation;
     if (currentLocation == null)
       return;
 
     if (currentLocation.star == null)
     {
       currentLocation.star = new Star(currentLocation.id,
                                       currentLocation.starName,
                                       currentLocation.starType,
                                       currentLocation.x,
                                       currentLocation.y,
                                       currentLocation.starRadius);
     }
 
     star = currentLocation.star;
 
     if (currentLocation.planets != null)
     {
       planets = currentLocation.planets;
     }
     else
     {
       planets = currentLocation.planets = new HashMap<Integer, Planet>();
       nController.getPlanets(currentLocation.id, false);
     }
     addPlanets();
   }
 
   public void addPlanets()
   {
     mController.addObject(star);
     for (Planet planet : planets.values())
     {
       mController.addObject(planet);
     }
   }
 
   public void removePlanets()
   {
     if (star != null)
     {
       mController.removeObject(star);
       star = null;
     }
     for (Planet planet : planets.values())
     {
       mController.removeObject(planet);
     }
   }
 
   public void addPlanet(Planet planet)
   {
     Location location = locations.get(planet.idLocation);
     if (location.planets == null)
       location.planets = new HashMap<Integer, Planet>();
     else if (location.planets.containsKey(planet.id))
       return;
     location.planets.put(planet.id, planet);
     if (location == currentLocation)
     {
       mController.addObject(planet);
     }
   }
 
   public void removePlanet(Planet planet)
   {
     Location location = locations.get(planet.idLocation);
     if (!location.planets.containsKey(planet.id))
       return;
     location.planets.remove(planet.id);
     if (location == currentLocation)
     {
       mController.removeObject(planet);
     }
   }
 
   public void removePlanet(int id)
   {
     Planet planet = planets.get(id);
     if (planet == null)
       return;
     planets.remove(id);
     mController.removeObject(planet);
   }
 
   // users
   public void loadComplete()
   {
     nController.loadComplite();
     active = true;
   }
 
   public void removeUsers()
   {
     for (User user : users.values())
     {
       mController.removeObject(user);
     }
     users.clear();
   }
 
   public void addUser(User user)
   {
     if (users.containsKey(user.id))
       return;
     users.put(user.id, user);
     mController.addObject(user);
   }
 
   public void removeUser(User user)
   {
     if (!users.containsKey(user.id))
       return;
     users.remove(user.id);
     mController.removeObject(user);
   }
 
   public void removeUser(int id)
   {
     User user = users.get(id);
     if (user == null)
       return;
     users.remove(id);
     mController.removeObject(user);
   }
 
   // get objects
   public Player getPlayer()
   {
     return player;
   }
 
   public Radar getRadar()
   {
     return radar;
   }
 
   public Star getStar()
   {
     return star;
   }
 
   // get locations
   public Collection<Location> getLocations()
   {
     return locations.values();
   }
 
   public Location getLocation(int id)
   {
     return locations.get(id);
   }
 
   public Collection<Domain> getDomains()
   {
     return domains.values();
   }
 
   public Domain getDomain(int id)
   {
     return domains.get(id);
   }
 
   private Vector2 distanceVec    = new Vector2();
   private float   distanceBuffer = 0.0f;
 
   public Location getCurrentLocation()
   {
     return currentLocation;
   }
 
   public Location getNearLocation()
   {
     return getNearLocation(player.position.x, player.position.y);
   }
 
   public Location getNearLocation(float x, float y)
   {
     Location nearLocation = null;
     float distance = Float.MAX_VALUE;
     for (Location location : locations.values())
     {
       distanceVec.set(location.x - x, location.y - y);
       distanceBuffer = distanceVec.len();
       if (distance > distanceBuffer)
       {
         distance = distanceBuffer;
         nearLocation = location;
       }
     }
     return nearLocation;
   }
 
   // get planets
   public Collection<Planet> getPlanets()
   {
     return planets.values();
   }
 
   public Star getStar(int idLocation)
   {
     Location location = locations.get(idLocation);
     if (location.star == null)
       location.star = new Star(location.id, location.starName, location.starType, location.x, location.y, location.starRadius);
     return location.star;
   }
 
   public Collection<Planet> getPlanets(int idLocation)
   {
     Location location = locations.get(idLocation);
     if (location == null)
       return null;
     if (location.planets == null)
       location.planets = nController.getPlanets(idLocation, true);
     return location.planets.values();
   }
 
   public Planet getPlanet(int idPlanet)
   {
     return planets.get(idPlanet);
   }
 
   public Planet getPlanet(String name)
   {
     for (Planet planet : planets.values())
     {
       if (planet.name.compareTo(name) == 0)
         return planet;
     }
     return null;
   }
 
   public Planet getPlanet(int idLocation, int idPlanet)
   {
     Location location = locations.get(idLocation);
     if (location == null)
       return null;
     if (location.planets == null)
       location.planets = nController.getPlanets(idLocation, true);
     return location.planets.get(idPlanet);
   }
 
   public Planet getPlanet(int idLocation, String name)
   {
     Location location = locations.get(idLocation);
     if (location == null)
       return null;
     if (location.planets == null)
       location.planets = nController.getPlanets(idLocation, true);
     for (Planet planet : location.planets.values())
     {
       if (planet.name.compareTo(name) == 0)
         return planet;
     }
     return null;
   }
 
   // get users
   public Collection<User> getUsers()
   {
     return users.values();
   }
 
   public User getUser(int id)
   {
     if (player.id == id)
       return player;
     return users.get(id);
   }
 
   public User getPilot(String name)
   {
     if (player.pilotName.compareTo(name) == 0)
       return player;
     for (User user : users.values())
     {
       if (user.pilotName.compareTo(name) == 0)
         return user;
     }
     return null;
   }
 
   public User getShip(String name)
   {
     if (player.shipName.compareTo(name) == 0)
       return player;
     for (User user : users.values())
     {
       if (user.shipName.compareTo(name) == 0)
         return user;
     }
     return null;
   }
 
   // update
   private float updateTime = 0.0f;
 
   public void update(float deltaTime)
   {
     if (!active || player == null)
       return;
     updateTime += deltaTime;
     if (updateTime > 1.0f)
     {
       updateNearNebulas();
       switchLocation(getNearLocation());
       updateTime -= 1.0f;
     }
   }
 
   public int getOutputX(float x)
   {
     return (int) ((x - player.domain.x) * 0.001);
   }
 
   public int getOutputY(float y)
   {
     return (int) ((y - player.domain.y) * 0.001);
   }
 }
