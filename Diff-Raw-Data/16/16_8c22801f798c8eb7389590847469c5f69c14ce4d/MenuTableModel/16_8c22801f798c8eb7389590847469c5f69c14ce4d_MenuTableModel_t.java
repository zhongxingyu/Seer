 /**
  * @(#)MenuTableModel.java
  * 
  *
  * @author Alex Kort, Cecilia Tsoy
  * @version 1.00 2007/10/14
  */
  
 import java.util.*;
 import java.text.DecimalFormat;
 
import javax.swing.table.DefaultTableModel;

 /*
  *
  * This class is the model for the JTable 
  * that holds menu items (drinks) in Tapioca King 
  *
  */
 public class MenuTableModel 
 {
 
     DefaultTableModel tm;
     DecimalFormat df = new DecimalFormat("###0.00");
     
     public MenuTableModel(DefaultTableModel t) 
     {
         drinks = new ArrayList<Products>();
         InCost = new EnumMap<Ingredients, Double>(Ingredients.class);
         setUpProducts();
         
         tm = t;
         
     }
     
     public DefaultTableModel getDefMod()
     {
     	return tm;
     }
     
     public void addDrink(Products d)
     {
         drinks.add(d);
         String pName = d.getName();
         String pCost = df.format(d.getCost());
         String pPrice = df.format(d.getPrice());
         String pProfit = df.format(d.getProfit());
         String pIng = d.toStringIngred();
         tm.addRow(new Object[]{pName, pCost, pPrice, pProfit, pIng});
     }
     
     public ArrayList<Products> getDrinks()
     {
     	return drinks;
     }
     
     
     public boolean ingsExist(Products p)
     {
     	Ingredients[] ings = p.getIngredients();
     	for (int i = 0; i < drinks.size(); i++)
     	{
     		boolean flag2 = true;
     			Ingredients[] in = drinks.get(i).getIngredients();
     			
     			for (int y = 0; y < in.length && y < ings.length; y++)
     			{
     				if (!in[y].equals(ings[y]))
     				{
     					
     					flag2 = false;
     				}
     			}
     			if (flag2)
     				return true;
     			else
     				continue;
     	}
     	return false;
     	
      }
      
      
      public boolean drinkExists(Products p)
      {
      	String name = p.getName();
     	
     	for (int i = 0; i < drinks.size(); i++)
     	{
 			if (drinks.get(i).getName().equals(name))
 			{
 				return true;
 			}
 		}
 		return false;
      }
      
      //returns 1 if there are no ingredients,
      //2 if there is only addons
      public int noIngs(Products p)
      {
      	Ingredients[] ings = p.getIngredients();
      	if (ings[0] == Ingredients.None && 
      		ings[1] == Ingredients.None &&
      		ings[2] == Ingredients.None)
      	{
      		return 1;
      	}
      	else if (ings[0] == Ingredients.None && 
      		ings[1] == Ingredients.None &&
      		ings[2] != Ingredients.None)
      	{
      		return 2;	
      	}
      	return 0;
      }
     
     
     
      /* Initialize all the ingredients and their cost. 
      * Build one drink as starter for the game.
      * This function only gets called once at the beginning of the game 
      */
     protected void setUpProducts()
     {
         InCost.put(Ingredients.BlackTea, 0.10);
         InCost.put(Ingredients.GreenTea, 0.10);
         InCost.put(Ingredients.RoseTea, 0.10);
         InCost.put(Ingredients.MilkTea, 0.10);
         InCost.put(Ingredients.Peach, 0.25);
         InCost.put(Ingredients.Lychee, 0.25);
         InCost.put(Ingredients.PassionFruit, 0.6);
         InCost.put(Ingredients.Strawberry, 0.6);
         InCost.put(Ingredients.Mango, 0.25);
         InCost.put(Ingredients.Apple, 0.25);
         InCost.put(Ingredients.Coffee, 0.50);
         InCost.put(Ingredients.Lemon, 0.25);
         InCost.put(Ingredients.Tapioca, 0.5);
         InCost.put(Ingredients.Aloe, 0.5);
         InCost.put(Ingredients.Jelly, 0.3);
         InCost.put(Ingredients.LycheeCoconut, 0.6);
         InCost.put(Ingredients.Cup, 0.05);
         InCost.put(Ingredients.Straw, 0.01);
         InCost.put(Ingredients.Napkin, 0.01);
         InCost.put(Ingredients.None, 0.00);
         InCost.put(Ingredients.Water, 0.00);
     }
     
     public EnumMap<Ingredients, Double> getInCost()
     {
         return InCost;
     }
     
     public boolean isCellEditable(int row, int column)
     {
         if (column == 2)
             return true;
         else
             return false;
     }
     
     public void setNapkinCost(double d)
     {
     	InCost.put(Ingredients.Napkin, d);
     }
     
     public void setCupCost(double d)
     {
     	InCost.put(Ingredients.Cup, d);
     }
     
     public void setStrawCost(double d)
     {
     	InCost.put(Ingredients.Straw, d);
     }
     
     public void printDrinks()
     {
     	for (int i = 0; i < drinks.size(); i++)
     	{
     		System.out.println(drinks.get(i).toStringIngred());
     	}
     }
 
     
     protected ArrayList<Products> drinks;
     protected EnumMap<Ingredients, Double> InCost;
 }
