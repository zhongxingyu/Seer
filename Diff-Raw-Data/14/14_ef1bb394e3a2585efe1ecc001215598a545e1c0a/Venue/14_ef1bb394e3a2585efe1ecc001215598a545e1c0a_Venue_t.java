 package com.brotherlogic.booser.atom;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  * Represents a location within the system
  * 
  * @author simon
  * 
  */
 public class Venue extends Atom
 {
   List<Drink> drinks;
    String foursquare_id;
    long lastUpdated;
    int venue_id;
    String venue_name;
    int venueType;
 
    public Venue(String id)
    {
       super(id);
       foursquare_id = id;
       drinks = new LinkedList<Drink>();
    }
 
    public Venue(String id, String nme)
    {
       this(id);
       venue_name = nme;
       venueType = 0;
    }
 
    public void addDrink(Drink d)
    {
      if (drinks == null)
         drinks = new LinkedList<Drink>();
       drinks.add(d);
    }
 
    @Override
    public boolean equals(Object obj)
    {
       if (!(obj instanceof Venue))
          return false;
       Venue v = (Venue) obj;
 
       if (!venue_name.equals(v.venue_name))
          return false;
 
       if (venueType != v.venueType)
          return false;
 
       // Also check all the venues
       if (drinks.size() != v.drinks.size())
          return false;
 
       return super.equals(obj);
    }
 
    public Map<Beer, Integer> getBeerCounts()
    {
       Map<Beer, Integer> counts = new TreeMap<Beer, Integer>();
 
       for (Drink d : drinks.subList(0, Math.min(100, drinks.size())))
          if (counts.containsKey(d.getBeer()))
             counts.put(d.getBeer(), counts.get(d.getBeer()) + 1);
          else
             counts.put(d.getBeer(), 1);
 
       return counts;
    }
 
    public Map<Beer, Integer> getBeerCounts(long cutoff)
    {
       Map<Beer, Integer> counts = new TreeMap<Beer, Integer>();
 
       for (Drink d : drinks)
          if (d.getTimestamp().getTime() > cutoff)
             if (counts.containsKey(d.getBeer()))
                counts.put(d.getBeer(), counts.get(d.getBeer()) + 1);
             else
                counts.put(d.getBeer(), 1);
 
       return counts;
    }
 
    public List<Drink> getDrinks()
    {
       return drinks;
    }
 
    @Override
    public String getId()
    {
       return foursquare_id;
    }
 
    public long getLastUpdated()
    {
       return lastUpdated;
    }
 
    public String getName()
    {
       return venue_name;
    }
 
    public int getUntappdId()
    {
       return venue_id;
    }
 
    public int getVenueType()
    {
       return venueType;
    }
 
    @Override
    public boolean hasDecayed()
    {
       // Users decay after 1 minute
       return (System.currentTimeMillis() - lastUpdated) > 1000 * 60;
    }
 
    public void setDrinks(List<Drink> drinks)
    {
       this.drinks = drinks;
    }
 
    public void setLastUpdated(long lastUpdated)
    {
       this.lastUpdated = lastUpdated;
    }
 
    public void setName(String name)
    {
       this.venue_name = name;
    }
 
    public void setUntappdId(int untappdId)
    {
       this.venue_id = untappdId;
    }
 
    public void setVenueType(int venueType)
    {
       this.venueType = venueType;
    }
 
    @Override
    public String toString()
    {
       return venue_name + " (" + venueType + ") " + drinks;
    }
 }
