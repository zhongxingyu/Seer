 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Item implements Serializable, Comparable<Item>
 {
 
   private String name;
   private String description;
   private int    IDNumber;
   private int    basePrice;
   private double modifier;
   private double volatility;
   private int    classification;
  private int    modifiedPrice; 
   private static int highestClassification;
   private static Random generator= new Random();     
   private static ArrayList<ArrayList<Item>> Items = new ArrayList<ArrayList<Item>>();
 
   public Item()
   {
     this("", " ", 0, 0, 0, 0, 0);
   }
 
   public Item(String name, String description, int IdNumber, int basePrice,
       double modifier, double volatility, int classification)
   {
     setName(name);
     setDescription(description);
     setIDNumber(IdNumber);
     setBasePrice(basePrice);
     setModifier(modifier);
     setVolatility(volatility);
     setClassification(classification);
   }
 
   public Item(String[] rec) //this constructor should only be called when reading items from a file
   {
     this(rec[0], rec[1], Integer.parseInt(rec[2]), Integer.parseInt(rec[3]),
         Double.parseDouble(rec[4]), Double.parseDouble(rec[5]), Integer
             .parseInt(rec[6]));
     if(this.getClassification()>highestClassification)
     {
     	highestClassification=this.getClassification();
     }
   }
   
   public static void initItems(ArrayList<Item> loaded)
   {
 	  for(int x = 0; x <= highestClassification; x++)
 	  {
 		  Items.add(new ArrayList<Item>());
 	  }
 	  
 	  for(Item x : loaded)
 	  {
 		  Items.get(x.getClassification()).add(x);
 	  }
   }
   
   public static Item getRandomItem(int c)
   {
 	  return Items.get(c).get(generator.nextInt(Items.get(c).size()));
   }
 
   public String getName()
   {
     return name;
   }
 
   public void setName(String name)
   {
     this.name = name;
   }
 
   public int getIDNumber()
   {
     return IDNumber;
   }
 
   public void setIDNumber(int iDNumber)
   {
     IDNumber = iDNumber;
   }
 
   /**
    * Use to modify the price. 
    * 
    */
   public void modify(Orbital locale)
   {
 	/*  
     double number = generator.nextDouble() * 1.5;
     if (number > volatility)
     {
       modifiedPrice = (int) (basePrice / modifier);
     } else if (number < volatility)
     {
       modifiedPrice = (int) (basePrice * modifier);
     } else
     {
       modifiedPrice = basePrice;
     }
     */
 	  
 	  double max = basePrice * modifier;
 	  double min = basePrice / modifier;
 	  double rel = max - min;
 	  if (volatility == 0)
 	  {
 		  modifiedPrice = (int) (1 + generator.nextInt((int) Math.round(rel)));
 		  
 	  }
 	  else
 	  {
 		  if(volatility > 1)
 		  {
 			  modifiedPrice = (int) ((1 + generator.nextInt((int) Math.round(rel)))*(volatility-1));
 			  if (locale.getOrbitalClass().GetProperties()[classification] > 0)
 			  {
 				  modifiedPrice = (int) (max - modifiedPrice)+1;
 			  }
 			  else
 			  {
 				  modifiedPrice = (int) (min + modifiedPrice)+1;
 			  }
 		  }
 		  else
 		  {
 			  modifiedPrice = (int) ((1 + generator.nextInt((int) Math.round(rel)))*volatility)+1;
 		  }
 	  }
 	  if (locale.getTradePort()== null) // if the orbital does not have a space dock
 	  {
 		  if (locale.getClass()==Ring.class) // Pirate bases get special treatment
 		  {
 			  switch (this.getIDNumber())
 			  {
 			  	case 12: // Explosives
 			  	case 13: // Small Arms
 			  	case 16: // Spice
 			  	case 29: // Artifacts
 			  		modifiedPrice = (int)(.5 * modifiedPrice);
 			  		break;
 			  	default: // Anything else
 			  		modifiedPrice = (int)(5 * modifiedPrice);
 			  		break;
 			  }
 		  }
 		  else
 		  {
 			  modifiedPrice = (int)(1.25 * modifiedPrice);
 		  }
 	  }
   }
   
   public static void modifyAll(Orbital locale)
   {
 	  for(int x = 0; x < Items.size(); x++)
 	  {
 		  for(Item cur : Items.get(x))
 		  {
 			  cur.modify(locale);
 		  }
 	  }
   }
   
   public void unModify()
   {
     modifiedPrice = basePrice;
   }
 
   public int getBasePrice()
   {
     return basePrice;
   }
 
   public void setBasePrice(int basePrice)
   {
     this.basePrice = basePrice;
   }
 
   public double getModifier()
   {
     return modifier;
   }
   
   public int getModifiedPrice()
   {
     return modifiedPrice;
   }
 
   public void setModifier(double modifier)
   {
     this.modifier = modifier;
   }
 
   public double getVolatility()
   {
     return volatility;
   }
 
   public void setVolatility(double volatility)
   {
     this.volatility = volatility;
   }
 
   public String getDescription()
   {
     return description;
   }
 
   public void setDescription(String description)
   {
     this.description = description;
   }
 
   public int getClassification()
   {
     return classification;
   }
 
   public void setClassification(int classification)
   {
     this.classification = classification;
   }
 
   public String toString()
   {
     return "$" + getModifiedPrice() + " " + name;
   }
 
   @Override
   public int compareTo(Item comparedItem)
   {
     
     return getModifiedPrice() - comparedItem.getModifiedPrice();
   }
 }
