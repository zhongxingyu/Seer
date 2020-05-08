 package main;
 
 import db.DatabaseHandler;
 import model.Sheep;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mayacat
  * Date: 10/3/13
  * Time: 9:40 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Register {
 
     private DatabaseHandler mHandler;
    private ArrayList<Sheep> mActiveSheeps;
 
     public Register(DatabaseHandler handler){
         this.mHandler = handler;
        this.mActiveSheeps = new ArrayList<Sheep>();
     }
 
     /**
      * Checks if register contains sheep by class
      * @param sheep
      * @return
      */
     public boolean containsSheep(Sheep sheep){
        return this.mActiveSheeps.contains(sheep);
     }
 
     /**
      * Checks if register contains sheep by id
      * @param id
      * @return
      */
     public boolean containsSheepById(int id){
        for(Sheep s : this.mActiveSheeps){
             if(s.getId() == id)
                 return true;
         }
         return false;
     }
 
     /**
      * Adds sheep by class
      * @param sheep
      */
     public void addSheep(Sheep sheep){
        this.mActiveSheeps.add(sheep);
     }
 
    /**
      * Fetches sheep from database by id.
      * @param id
      */
     public void addSheepById(int id){
         try {
             Sheep sheep = mHandler.getSheep(id);
             if(sheep != null)
                 this.mActiveSheeps.add(sheep);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /**
      * Fetches all sheeps for farmer from database.
      * @param id
      */
     public void addSheepsByFarmerId(int id){
         try {
             ArrayList<Sheep> farmerSheeps = mHandler.getSheeps(id);
             if(!farmerSheeps.isEmpty())
                this.mActiveSheeps.addAll(farmerSheeps);
         } catch (SQLException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     /**
      * Gets sheep by id if it exists. Returns null if sheep is not found
      * @param id
      */
     public Sheep getSheepById(int id){
        for(Sheep s : this.mActiveSheeps){
             if(s.getId() == id)
                 return s;
         }
         return null;
     }
 
     /**
      * Get all sheeps for farmerID. Returns null if no sheeps are found
      * @param id
      * @return
      */
     public ArrayList<Sheep> getAllFarmerSheeps(int id){
         ArrayList<Sheep> farmerSheeps = new ArrayList<Sheep>();
 
        for(Sheep s : this.mActiveSheeps){
             if(s.getOwnerid() == id)
                 farmerSheeps.add(s);
         }
         if(farmerSheeps.isEmpty())
             return null;
         return farmerSheeps;
     }
 
 
 }
