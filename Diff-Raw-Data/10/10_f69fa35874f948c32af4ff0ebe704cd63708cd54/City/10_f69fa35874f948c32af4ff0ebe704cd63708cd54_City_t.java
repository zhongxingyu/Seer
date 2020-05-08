 package com.infinitemule.espn.common.api;
 
 public class City {
 
  public static final City Boston     = new City("boston",      "Boston");
  public static final City Chicago    = new City("chicago",     "Chicago");
  public static final City Dallas     = new City("dallas",      "Dallas");
  public static final City LosAngeles = new City("los-angeles", "Los Angeles");
  public static final City NewYork    = new City("new-york",    "New York");
   
   private final String id;
   private final String name;
   
   private City(String id, String name) {
     this.id = id;
     this.name = name;
   }
 
   public final String getId() {
     return id;
   }
 
   public String getName() {
     return name;
   }
     
 }
