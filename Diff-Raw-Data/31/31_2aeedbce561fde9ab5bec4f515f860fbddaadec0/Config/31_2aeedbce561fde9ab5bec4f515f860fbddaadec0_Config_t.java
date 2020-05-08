 package com.gamecook.jit.config;
 
 import com.gamecook.jit.events.RandomEvent;
 import com.gamecook.jit.items.Item;
 
 import java.util.ArrayList;
 
 /**
  * User: Jesse Freeman
  * Date: 11/28/10
  * Time: 9:59 PM
  */
 public interface Config
 {
     int getGameID();
 
     void setDifficultyLevel(int difficultyLevel);
 
     int getCurrentLocation();
 
     void setCurrentLocation(int currentLocation);
 
     double getSavings();
 
     void setSavings(double savings);
 
     double getLoan();
 
     void setLoan(double loan);
 
     double getInterest();
 
     void setInterest(double interest);
 
     double getCash();
 
     void setCash(double cash);
 
     int getDays();
 
     void setDays(int days);
 
     int getTotalDays();
 
     void setTotalDays(int totalDays);
 
     ArrayList<Item> getItems();
 
     void setItems(ArrayList<Item> items);
 
     int getMaxTotal();
 
     int getMaxCurrentInventory();
 
     ArrayList<RandomEvent> getRandomEvents();
 
     int getRandomEventOdds();
 
    //int getDefaultRandomEventID();
 
     String getGameName();
 }
