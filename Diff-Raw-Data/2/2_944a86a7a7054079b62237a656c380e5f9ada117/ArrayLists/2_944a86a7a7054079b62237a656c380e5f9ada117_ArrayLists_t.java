 package com.github.CorporateCraft.cceconomy;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 
 public class ArrayLists
 {
 	public static ArrayList<String> Balances = new ArrayList<String>();
 	public static ArrayList<String> SellPrices = new ArrayList<String>();
 	public static ArrayList<String> BuyPrices = new ArrayList<String>();
 	
 	public static void UpdateBalances()
 	{
 		Balances.clear();
 		String file = "plugins/CCEconomy/moneytracker.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        Balances.add(inputText);
 		    }
 		}
 		catch (IOException ex){}
 		Collections.sort(Balances);
 	}
 	
 	public static void UpdateSellPrices()
 	{
 		SellPrices.clear();
 		String file = "plugins/CCEconomy/sellprices.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
 		        SellPrices.add(inputText);
 		    }
 		}
 		catch (IOException ex){}
 		Collections.sort(SellPrices);
 	}
 	
 	public static void UpdateBuyCosts()
 	{
 		BuyPrices.clear();
 		String file = "plugins/CCEconomy/buyprices.txt";
 		try
 		{
 		    FileReader reader = new FileReader(file);
 		    BufferedReader buff = new BufferedReader(reader);
 		    while(true)
 		    {
 		    	String inputText = buff.readLine();
 		        if(inputText == null)
 		        {
 		         	break;
 		        }
		        BuyPrices.add(inputText);
 		    }
 		}
 		catch (IOException ex){}
 		Collections.sort(BuyPrices);
 	}
 }
