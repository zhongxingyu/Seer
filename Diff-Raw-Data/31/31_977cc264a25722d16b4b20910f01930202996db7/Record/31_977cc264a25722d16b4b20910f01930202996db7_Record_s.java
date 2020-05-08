 import java.util.*;
 
 public class Record {
   private static List<Record> record;

    private int iden;
   private String ttl;
   private List<String> art;
   private List<String> us;
   private List<Integer> rat;
 
     private Record(int i, String t, List <String> a, List <String> u,
                    List <Integer> r) {
 	iden = i;
 	ttl = t;
 	art = a;
 	us = u;
 	rat = r;
     }
 
   public static void initRecord() {
     Record.record = new ArrayList<Record>();
   }
   
     public static void newRecord() {
       record.add(new Record(-1, "null", new ArrayList<String>(), new ArrayList<String>(), new ArrayList<Integer>()));
     }
     
     public static void addTitle(int i, String title) {
 	if (record.get(i).ttl.equals("null")) 
           record.get(i).ttl = title;
 	 else 
 	     System.out.println("That record already has a title, check your " +
 				"index or intent. " + 
 				"call updateTitle if you really need to " +
 				"change it");
     }
     public static void updateTitle(int i, String title) {
 	record.get(i).ttl = title;
     }
     public static void addID(int i, int id) {
 	if (record.get(i).iden == -1) 
 	    record.get(i).iden = id;
 	else
 	    System.out.println("That record already has an id number, check " +
 			       "your index and intent. Call updateID if " +
 			       "you really want to change it.");
     }
     public static void updateID(int i, int id) {
 	record.get(i).iden = id;
     }
     public static void addArtist(int i, String artist) {
 	record.get(i).art.add(artist);
     }
     public static void addUserRat(int i, String user, Integer rating) {
 	record.get(i).us.add(user);
 	record.get(i).rat.add(rating);
     }
     public static void updateRating(int i, String user, Integer rating) {
       int j;
       for (j=0; !record.get(i).us.get(j).equals(user); j++) {}
 	record.get(i).rat.set(j, rating);
     }
     public static Integer getID(int i) {return record.get(i).iden;}
     public static String getTitle(int i) {return record.get(i).ttl;}
     public static int getNumArtists(int i) {return record.get(i).art.size();}
     public static int getNumUsers(int i) {return record.get(i).us.size();}
     public static String[] getArtists(int i) {
       String[] sArray = new String[1];
 	return record.get(i).art.toArray(sArray);
     }
     public static String[] getUsers(int i) {
       String[] sArray = new String[1];
 	return record.get(i).us.toArray(sArray);
     }
     public static Integer getRating(int i, String user) {
       int j;
       for (j=0; !record.get(i).us.get(j).equals(user); j++) {}
 	return record.get(i).rat.get(j);
     }
     public static Record getRecord(int i) { return record.get(i);}
     public static int recordSize() {return record.size();}
 }
