 package com.brotherlogic.booser.storage.web;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import com.brotherlogic.booser.atom.Atom;
 import com.brotherlogic.booser.atom.Beer;
 import com.brotherlogic.booser.atom.Brewery;
 import com.brotherlogic.booser.atom.Drink;
 import com.brotherlogic.booser.atom.FoursquareVenue;
 import com.brotherlogic.booser.atom.User;
 import com.brotherlogic.booser.atom.Venue;
 import com.brotherlogic.booser.storage.AssetManager;
 import com.brotherlogic.booser.storage.CascadingAtomBuilder;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 public class WebLayer extends CascadingAtomBuilder
 {
    BeerFinder bFinder = new BeerFinder("NO_TOKEN");
    Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss Z").create();
    String untappdBase = "http://api.untappd.com/v4/";
    VenueFinder vFinder = new VenueFinder("NO_TOKEN");
 
    public WebLayer()
    {
 
    }
 
    public WebLayer(AssetManager manager)
    {
       super(manager);
    }
 
    public List<Drink> getDrinks(int firstId, int lastId)
    {
       List<Drink> retDrinks = new LinkedList<Drink>();
 
       // Fill out the top end
       boolean done = false;
       String url = "http://api.untappd.com/v4/user/checkins?limit=50&min_id=" + firstId;
 
       try
       {
          while (!done)
          {
             String response = Downloader.getInstance().download(manager.embellish(new URL(url)));
 
             JsonObject objResponse = new JsonParser().parse(response).getAsJsonObject();
             JsonArray drinks = objResponse.get("response").getAsJsonObject().get("checkins")
                   .getAsJsonObject().get("items").getAsJsonArray();
             for (int i = 0; i < drinks.size(); i++)
             {
                Drink d = gson.fromJson(drinks.get(i), Drink.class);
                d.setUnderlyingRep(drinks.get(i).toString());
                if (d.getIdNumber() <= firstId)
                {
                   done = true;
                   break;
                }
                else
                   retDrinks.add(d);
             }
 
             // Get the following URL
             url = objResponse.get("response").getAsJsonObject().get("pagination").getAsJsonObject()
                   .get("next_url").getAsString();
 
             // We get empty url when we're done
             if (url.length() == 0)
                done = true;
          }
       }
       catch (IOException e)
       {
 
       }
 
       done = false;
       url = "http://api.untappd.com/v4/user/checkins?limit=50&max_id=" + lastId;
 
       try
       {
          while (!done)
          {
             String response = Downloader.getInstance().download(manager.embellish(new URL(url)));
 
             JsonObject objResponse = new JsonParser().parse(response).getAsJsonObject();
             JsonArray drinks = objResponse.get("response").getAsJsonObject().get("checkins")
                   .getAsJsonObject().get("items").getAsJsonArray();
             for (int i = 0; i < drinks.size(); i++)
             {
                Drink d = gson.fromJson(drinks.get(i), Drink.class);
                d.setUnderlyingRep(drinks.get(i).toString());
                if (d.getIdNumber() <= firstId)
                {
                   done = true;
                   break;
                }
                else
                   retDrinks.add(d);
             }
 
             // Get the following URL
             url = objResponse.get("response").getAsJsonObject().get("pagination").getAsJsonObject()
                   .get("next_url").getAsString();
 
             // We get empty url when we're done
             if (url.length() == 0)
                done = true;
          }
       }
       catch (IOException e)
       {
 
       }
 
       return retDrinks;
    }
 
    @Override
    protected Atom getLocal(Atom embellish)
    {
       try
       {
          if (embellish.getClass().equals(Beer.class))
             return bFinder.getScore(embellish.getId());
          else if (embellish.getClass().equals(Venue.class))
             return vFinder.getVenue(embellish.getId());
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
 
       return null;
    }
 
    @Override
    protected List<Atom> getLocal(Atom context, Class<?> cls)
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    @Override
    public List<Atom> getLocal(Class<?> classToRetrieve, boolean deep)
    {
       try
       {
          if (classToRetrieve.equals(User.class))
             return getLocal(classToRetrieve, new URL(untappdBase + "user/info/"));
          else if (classToRetrieve.equals(Drink.class))
             return getLocal(classToRetrieve, new URL(untappdBase + "user/checkins/"));
 
       }
       catch (MalformedURLException e)
       {
          e.printStackTrace();
       }
 
       return null;
    }
 
    public List<Atom> getLocal(Class<?> cls, URL url)
    {
      return getLocal(cls, url, true);
   }

   public List<Atom> getLocal(Class<?> cls, URL url, boolean embellish)
   {
       List<Atom> atoms = new LinkedList<Atom>();
       try
       {
         String jsonResponse = "";
         if (embellish)
            jsonResponse = Downloader.getInstance().download(manager.embellish(url));
         else
            jsonResponse = Downloader.getInstance().download(url);
          if (cls.equals(FoursquareVenue.class))
          {
             JsonParser parser = new JsonParser();
             JsonElement parsed = parser.parse(jsonResponse);
             JsonArray places = parsed.getAsJsonObject().get("response").getAsJsonObject()
                   .get("venues").getAsJsonArray();
             for (int i = 0; i < places.size(); i++)
             {
                FoursquareVenue venue = gson.fromJson(places.get(i), FoursquareVenue.class);
                venue.setUnderlyingRep(places.get(i).toString());
                atoms.add(venue);
             }
          }
          else if (cls.equals(Drink.class))
          {
             JsonParser parser = new JsonParser();
             JsonElement parsed = parser.parse(jsonResponse);
             JsonArray drinks = parsed.getAsJsonObject().get("response").getAsJsonObject()
                   .get("checkins").getAsJsonObject().get("items").getAsJsonArray();
             for (int i = 0; i < drinks.size(); i++)
             {
                Drink drink = gson.fromJson(drinks.get(i), Drink.class);
                drink.setUnderlyingRep(drinks.get(i).toString());
                atoms.add(drink);
             }
          }
          else if (cls.equals(User.class))
          {
             JsonParser parser = new JsonParser();
             JsonElement parsed = parser.parse(jsonResponse);
             JsonObject user = parsed.getAsJsonObject().get("response").getAsJsonObject()
                   .get("user").getAsJsonObject();
 
             User u = gson.fromJson(user, User.class);
             u.setUnderlyingRep(user.toString());
             atoms.add(u);
          }
       }
       catch (IOException e)
       {
          // Ignore
       }
 
       return atoms;
    }
 
    public User getUserInfo() throws IOException
    {
       String url = "http://api.untappd.com/v4/user/info/";
       String response = Downloader.getInstance().download(manager.embellish(new URL(url)));
       JsonObject user = new JsonParser().parse(response).getAsJsonObject().get("response")
             .getAsJsonObject().get("user").getAsJsonObject();
       Integer checkins = user.get("stats").getAsJsonObject().get("total_checkins").getAsInt();
 
       User u = gson.fromJson(user, User.class);
       u.setNumberOfCheckins(checkins);
       return u;
    }
 
    public Venue getVenue(String fourSquareId) throws IOException
    {
       String url = "http://api.untappd.com/v4/venue/foursquare_lookup/" + fourSquareId;
       String response = Downloader.getInstance().download(manager.embellish(new URL(url)));
       JsonObject objResponse = new JsonParser().parse(response).getAsJsonObject();
 
       return new Gson().fromJson(objResponse.get("response").getAsJsonObject().get("venue")
             .getAsJsonObject().get("items").getAsJsonArray().get(0), Venue.class);
    }
 
    // Get 100 drinks
    public List<Drink> getVenueDrinks(int id)
    {
       List<Drink> retDrinks = new LinkedList<Drink>();
 
       // Fill out the top end
       boolean done = false;
       String url = "http://api.untappd.com/v4/venue/checkins/" + id + "?limit=50";
 
       try
       {
          while (!done)
          {
             String response = Downloader.getInstance().download(manager.embellish(new URL(url)));
 
             JsonObject objResponse = new JsonParser().parse(response).getAsJsonObject();
             JsonArray drinks = objResponse.get("response").getAsJsonObject().get("checkins")
                   .getAsJsonObject().get("items").getAsJsonArray();
             for (int i = 0; i < drinks.size(); i++)
             {
                Drink d = gson.fromJson(drinks.get(i), Drink.class);
                d.getBeer().setBrewery(
                      gson.fromJson(
                            drinks.get(i).getAsJsonObject().get("brewery").getAsJsonObject(),
                            Brewery.class));
                retDrinks.add(d);
             }
 
             // Get the following URL
             url = objResponse.get("response").getAsJsonObject().get("pagination").getAsJsonObject()
                   .get("next_url").getAsString();
 
             // We get empty url when we're done
             if (url.length() == 0 || retDrinks.size() >= 100)
                done = true;
          }
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
 
       return retDrinks;
    }
 
    // Get 100 drinks
    public List<Drink> getVenueDrinks(int id, int latest)
    {
       List<Drink> retDrinks = new LinkedList<Drink>();
 
       // Fill out the top end
       boolean done = false;
       String url = "http://api.untappd.com/v4/venue/checkins/" + id + "?limit=50&min_id=" + latest;
 
       try
       {
          while (!done)
          {
             String response = Downloader.getInstance().download(manager.embellish(new URL(url)));
 
             JsonObject objResponse = new JsonParser().parse(response).getAsJsonObject();
             JsonArray drinks = objResponse.get("response").getAsJsonObject().get("checkins")
                   .getAsJsonObject().get("items").getAsJsonArray();
             for (int i = 0; i < drinks.size(); i++)
             {
                Drink d = gson.fromJson(drinks.get(i), Drink.class);
                d.setUnderlyingRep(drinks.get(i).toString());
                if (d.getIdNumber() <= latest)
                {
                   done = true;
                   break;
                }
                else
                   retDrinks.add(d);
             }
 
             // Get the following URL
             url = objResponse.get("response").getAsJsonObject().get("pagination").getAsJsonObject()
                   .get("next_url").getAsString();
 
             // We get empty url when we're done
             if (url.length() == 0 || retDrinks.size() >= 100)
                done = true;
          }
       }
       catch (IOException e)
       {
          System.out.println("Download error: " + e.getLocalizedMessage());
       }
 
       return retDrinks;
    }
 
    @Override
    protected boolean storeLocal(Atom obj)
    {
       // Web layer doesn't store
       return false;
    }
 
 }
