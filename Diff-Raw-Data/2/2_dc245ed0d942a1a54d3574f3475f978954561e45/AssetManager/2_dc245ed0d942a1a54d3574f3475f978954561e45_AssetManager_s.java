 package com.brotherlogic.booser.storage;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.brotherlogic.booser.Config;
 import com.brotherlogic.booser.atom.Atom;
 import com.brotherlogic.booser.atom.Beer;
 import com.brotherlogic.booser.atom.Drink;
 import com.brotherlogic.booser.atom.User;
 import com.brotherlogic.booser.atom.Venue;
 import com.brotherlogic.booser.storage.db.DatabaseFactory;
 import com.brotherlogic.booser.storage.db.DatabaseLayer;
 import com.brotherlogic.booser.storage.web.WebLayer;
 
 /**
  * Central point for dealing with any assets in the system
  * 
  * @author simon
  * 
  */
 public class AssetManager
 {
    // Base (i.e. unlogged in) Asset Manager
    private static AssetManager base;
 
    // Maps from user tokens to managers
    private static Map<String, AssetManager> singletons = new TreeMap<String, AssetManager>();
 
    public static void clear()
    {
       singletons = new TreeMap<String, AssetManager>();
    }
 
    public static AssetManager getManager()
    {
       return getManager(null);
    }
 
    public static AssetManager getManager(String userToken)
    {
       // Token for an unlogged in user
       if (userToken == null)
       {
          if (base == null)
             base = new AssetManager(null);
 
          return base;
       }
 
       if (singletons.get(userToken) == null)
          singletons.put(userToken, new AssetManager(userToken));
       return singletons.get(userToken);
    }
 
    String currentToken;
    private final WebLayer localWeb;
 
    private final Map<String, DatabaseLayer> storageMap = new TreeMap<String, DatabaseLayer>();
 
    /**
     * Blocking constructor
     */
    private AssetManager(String token)
    {
       init();
       localWeb = new WebLayer();
       localWeb.manager = this;
       currentToken = token;
    }
 
    public URL embellish(URL url)
    {
       try
       {
          if (currentToken != null)
          {
             if (url.toString().contains("?"))
                return new URL(url.toString() + "&access_token=" + currentToken);
             else
                return new URL(url.toString() + "?access_token=" + currentToken);
          }
          else if (url.toString().contains("?"))
             return new URL(url.toString() + "&client_id=" + Config.getUntappdClient()
                   + "&client_secret=" + Config.getUntappdSecret());
          else
             return new URL(url.toString() + "?client_id=" + Config.getUntappdClient()
                   + "&client_secret=" + Config.getUntappdSecret());
 
       }
       catch (MalformedURLException e)
       {
          e.printStackTrace();
       }
 
       return null;
    }
 
    public Beer getBeer(String id) throws IOException
    {
       Beer b = (Beer) storageMap.get(Beer.class.getCanonicalName()).get(new Beer(id));
 
       if (b == null)
       {
          WebLayer wl = new WebLayer(this);
          b = wl.getBeer(id);
          if (b != null)
             store(b);
       }
 
       return b;
    }
 
    public Drink getDrink(String id)
    {
       return (Drink) storageMap.get(Drink.class.getCanonicalName()).get(new Drink(id));
    }
 
    public String getToken()
    {
       return currentToken;
    }
 
    public User getUser() throws IOException
    {
       WebLayer wl = new WebLayer(this);
       User webUser = wl.getUserInfo();
       User u = pullUser(webUser.getId());
       if (u == null)
       {
          u = new User(webUser.getId());
 
          // Get all the drinks
          List<Drink> drinks = wl.getDrinks(0, Integer.MAX_VALUE);
          for (Drink d : drinks)
          {
             u.addDrink(d);
 
             Drink alt = getDrink(d.getId());
             if (alt == null)
                store(d);
 
          }
 
          u = pullUser(webUser.getId());
       }
       else if (u.getDrinks().size() != webUser.getNumberOfCheckins())
       {
          // Get all the drinks
          List<Drink> drinks = wl.getDrinks(u.getDrinks().get(0).getIdNumber(),
                u.getDrinks().get(u.getDrinks().size() - 1).getIdNumber());
          for (Drink d : drinks)
          {
             u.addDrink(d);
 
             Drink alt = getDrink(d.getId());
             if (alt == null)
                store(d);
          }
       }
 
       return u;
    }
 
    public User getUser(String id) throws IOException
    {
       WebLayer wl = new WebLayer(this);
       User u = pullUser(id);
      if (u == null)
       {
          u = new User(id);
 
          // Get all the drinks
          List<Drink> drinks = wl.getDrinks(0, Integer.MAX_VALUE);
          for (Drink d : drinks)
          {
             u.addDrink(d);
 
             Drink alt = getDrink(d.getId());
             if (alt == null)
                store(d);
 
          }
 
          u = pullUser(id);
       }
       else if (u.getDrinks().size() != u.getNumberOfCheckins())
       {
          // Get all the drinks
          List<Drink> drinks = wl.getDrinks(u.getDrinks().get(0).getIdNumber(),
                u.getDrinks().get(u.getDrinks().size() - 1).getIdNumber());
          for (Drink d : drinks)
          {
             u.addDrink(d);
 
             Drink alt = getDrink(d.getId());
             if (alt == null)
                store(d);
          }
       }
 
       return u;
    }
 
    public List<Atom> getUsers()
    {
       return storageMap.get(User.class.getCanonicalName()).get(User.class, false);
    }
 
    /**
     * Gets the venue from the foursquare address
     * 
     * @param id
     * @return
     */
    public Venue getVenue(String id) throws IOException
    {
       WebLayer wl = new WebLayer(this);
 
       Venue v = (Venue) storageMap.get(Venue.class.getCanonicalName()).get(new Venue(id));
       if (v == null)
          v = wl.getVenue(id);
 
       // Refresh this venue
       List<Drink> drinks = new LinkedList<Drink>();
 
       if (v.getDrinks() == null || v.getDrinks().size() == 0)
          drinks = wl.getVenueDrinks(v.getUntappdId());
       else
       {
          int latest = v.getDrinks().get(0).getIdNumber();
          drinks = wl.getVenueDrinks(v.getUntappdId(), latest);
          latest++;
       }
 
       for (Drink drink : drinks)
          v.addDrink(drink);
       store(v);
 
       return v;
    }
 
    private void init()
    {
       // Create the storage mapper for the beer
       storageMap.put(Beer.class.getCanonicalName(), new DatabaseLayer(DatabaseFactory.getInstance()
             .getDatabase()));
       storageMap.put(Venue.class.getCanonicalName(), new DatabaseLayer(DatabaseFactory
             .getInstance().getDatabase()));
       storageMap.put(Drink.class.getCanonicalName(), new DatabaseLayer(DatabaseFactory
             .getInstance().getDatabase()));
       storageMap.put(User.class.getCanonicalName(), new DatabaseLayer(DatabaseFactory.getInstance()
             .getDatabase()));
    }
 
    public User pullUser(String id)
    {
       return (User) storageMap.get(User.class.getCanonicalName()).get(new User(id));
    }
 
    public void store(Object o) throws IOException
    {
       if (storageMap.containsKey(o.getClass().getCanonicalName()))
          storageMap.get(o.getClass().getCanonicalName()).store((Atom) o);
       else
          throw new IOException("Unable to store objects of type " + o.getClass());
    }
 
 }
