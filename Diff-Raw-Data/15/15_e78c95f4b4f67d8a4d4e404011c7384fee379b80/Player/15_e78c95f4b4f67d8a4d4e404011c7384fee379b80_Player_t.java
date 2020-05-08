 import java.util.Vector;
 
 public class Player implements Merchant
 {
 
   private String            name;
   private double            bank;
   private Vector<Item>      inventory;
   private Vector<Item>      brokenDrive;
   private int               fuel;
   private Location          currentlocation;
   private static final int  MAX_PORT_HISTORY = 15;
   private static final int  FUEL_CONSTANT    = 25;
   private static final int  MAX_FUEL_LEVEL   = 50;
   private static final int  FUEL_CONSUMPTION = 5;
 
   private Vector<TradePort> recentLocations;
 
   public Player()
   {
     this("Player", new Vector<Item>());
   }
 
   public Player(String name, Vector<Item> inventory)
   {
     this.name = name;
     bank = 500;
     fuel = 20;
     this.inventory = inventory;
     recentLocations = new Vector<TradePort>();
     brokenDrive = new Vector<Item>();
 
   }
 
   @Override
   public double getMoney()
   {
     return bank;
   }
 
   @Override
   public Vector<Item> getInventory()
   {
     return inventory;
   }
 
   @Override
   public void modifyPrice()
   {
     // WE DON'T MODIFY THE PLAYER'S PRICE
 
   }
 
   @Override
   public void sale(double price, Item item)
   {
     bank = bank + price;
     inventory.remove(item);
 
   }
 
   @Override
   public void purchase(double price, Item item)
   {
     bank = bank - price;
     inventory.add(item);
   }
 
   @Override
   public String toString()
   {
     return inventory.toString();
   }
 
   public void addPort(TradePort port)
   {
     if (recentLocations.size() < MAX_PORT_HISTORY
         && !recentLocations.contains(port))
     {
       recentLocations.add(port);
     } else
     {
       recentLocations.remove(0);
       recentLocations.add(port);
     }
   }
 
   public boolean hasBeenHere(Location locale)
   {
     boolean test = false;
     for (int i = 0; i < recentLocations.size(); i++)
     {
       if (recentLocations.get(i).getLocale().equals(locale))
       {
         test = true;
       }
     }
     return test;
   }
 
   public Location getLoc()
   {
     return currentlocation;
   }
 
   public void setLoc(Location locale)
   {
     currentlocation = locale;
   }
 
   public TradePort getPort(Location locale)
   {
     TradePort port = null;
     for (int i = 0; i < recentLocations.size(); i++)
     {
       if (recentLocations.get(i).getLocale().equals(locale))
       {
         port = recentLocations.get(i);
       }
     }
     return port;
   }
 
   public void useFuel()
   {
     fuel -= FUEL_CONSUMPTION;
   }
 
   public String use(Item item)
   {
     if (item.getIDNumber() == 4)
     {
       if (fuel + FUEL_CONSTANT <= MAX_FUEL_LEVEL)
       {
         fuel += FUEL_CONSTANT;
       } else
       {
         fuel = MAX_FUEL_LEVEL;
       }
       inventory.remove(item);
 
       return String.format("Fuel Level was increased to: %d", getFuelLevel());
     }
     if (item.getIDNumber() == 28) // Antimatter fully refuels the ship
     {
     	fuel = MAX_FUEL_LEVEL;
     	inventory.remove(item);
     	return String.format("Fuel Level was increased to: %d", getFuelLevel());
     }
     if (item.getIDNumber() == 32 && !brokenDrive.contains(item))
     {
       brokenDrive.add(item);
       inventory.remove(item);
       if(tryToWin())
       {
         return "You have rebuilt your drive click the back button in the main panel to win the game!!";
       }
       return String.format("You added %s to your broken drive", item.getName());
     }
     if (item.getIDNumber() == 31 && !brokenDrive.contains(item))
     {
       brokenDrive.add(item);
       inventory.remove(item);
       if(tryToWin())
       {
         return "You have rebuilt your drive click the back button in the main panel to win the game!!";
       }
       return String.format("You added %s to your broken drive", item.getName());
     }
     if (item.getIDNumber() == 30 && !brokenDrive.contains(item))
     {
       brokenDrive.add(item);
       inventory.remove(item);
       if(tryToWin())
       {
         return "You have rebuilt your drive click the back button in the main panel to win the game!!";
       }
       return String.format("You added %s to your broken drive", item.getName());
     }
     
     return "You already have that in your drive";
   }
 
   public boolean tryToWin()
   {
     if (brokenDrive.size() == 3)
     {
       return true;
     }
     else
     {
       return false;
     }
   }
 
   public void drop(Item item)
   {
     inventory.remove(item);
   }
 
   /*
    * Ryan: I really think that this is a bad way of returning the fuel level
    * The max fuel level is easily passable and it opens the problem of using some fuel,
    * selling it, then getting it back like new.
    * 
    * 
    */
   public int getFuelLevel()
   {

 	  //int extraFuel = 0;
 	  /*for(Item x : inventory)
/*	  int extraFuel = 0;
	  for(Item x : inventory)
>>>>>>> b85bd1b093f7de0183df259eaf9820fadd55cf09
 	  {
 		  if(x.getIDNumber()==4)
 		  {
 			  extraFuel += FUEL_CONSTANT;
 		  }
<<<<<<< HEAD
 		  if(x.getIDNumber()==28) 
 		  {
 			  extraFuel += MAX_FUEL_LEVEL;
 		  }
 	  }*/
    //return fuel;// + extraFuel;
	  
    return fuel;
   }
   
   /*
    * Checks fuel then uses fuel in inventory if needed.
    */
   public boolean hasFuel()
   {
 	  if (fuel>0)
 	  {
 		  return true;
 	  }
 	  for(Item x : inventory)
 	  {
		  if(x.getIDNumber() == 4)
 		  {
 			  this.use(x);
 			  return true;
 		  }
 	  }
 	  return false;
   }
   
   public void revertPrices()
   {
 	  for(int x = 0; x < inventory.size(); x++)
 	  {
 		  inventory.get(x).unModify();
 	  }
   }
   
 }
