 package com.brotherlogic.booser.atom;
 
 import java.io.IOException;
 
 import com.brotherlogic.booser.storage.AssetManager;
 import com.google.gson.Gson;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 
 /**
  * Represents a beer within the system
  * 
  * @author simon
  * 
  */
 public class Beer extends Atom implements Comparable<Beer>
 {
    private Double beer_abv;
    private String beer_name;
    private String beer_style;
    private Integer bid;
    private String brewery_name;
 
    // This is the overall rating score
    private Double rating_score;
 
    public Beer(Integer bid)
    {
       super(bid.toString());
       this.bid = bid;
    }
 
    public Beer(Integer bid, String name, double alcohol)
    {
       super(bid.toString());
       this.bid = bid;
       beer_name = name;
       beer_abv = alcohol;
    }
 
    public Beer(String bid)
    {
       super(bid);
       setId(bid);
    }
 
    @Override
    public int compareTo(Beer o)
    {
       return beer_name.compareTo(o.beer_name);
    }
 
    public Double getAbv()
    {
       return beer_abv;
    }
 
    public String getBeerName()
    {
       return beer_name;
    }
 
    public String getBeerStyle()
    {
       if (beer_style == null)
       {
          beer_style = (String) refresh("beer_style", String.class);
         try
         {
            AssetManager.getManager().store(this);
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
       }
       return beer_style;
    }
 
    public String getBreweryName()
    {
       if (brewery_name == null)
       {
          JsonObject obj = new JsonParser().parse(getUnderlyingRep()).getAsJsonObject();
          brewery_name = new Gson().fromJson(obj.get("brewery").getAsJsonObject()
                .get("brewery_name"), String.class);
 
          try
          {
             AssetManager.getManager().store(this);
          }
          catch (IOException e)
          {
             e.printStackTrace();
          }
       }
       return beer_style;
    }
 
    @Override
    public String getId()
    {
       return bid.toString();
    }
 
    public Double getScore()
    {
       return rating_score;
    }
 
    @Override
    public boolean hasDecayed()
    {
       // Only decay can occur if we don't have the rating score
       return rating_score == null;
    }
 
    public void setAbv(Double val)
    {
       beer_abv = val;
    }
 
    public void setBeerName(String name)
    {
       beer_name = name;
    }
 
    public void setBeerStyle(String beer_style)
    {
       this.beer_style = beer_style;
    }
 
    @Override
    public void setId(String id)
    {
       if (id.length() > 0)
          this.bid = Integer.parseInt(id);
       else
          this.bid = 0;
    }
 
    public void setScore(Double sc)
    {
       rating_score = sc;
    }
 
    @Override
    public String toString()
    {
       return "ID = " + bid + ", Name = " + beer_name;
    }
 
 }
