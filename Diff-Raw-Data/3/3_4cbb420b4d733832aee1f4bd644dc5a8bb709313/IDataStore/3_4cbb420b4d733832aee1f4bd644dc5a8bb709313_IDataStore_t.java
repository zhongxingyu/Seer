 package final_project.model;
 
 import java.util.*;
 
 public interface IDataStore {
     //Returns all the objects that implement the appropriate interface
     Collection<IData> getData();
     Collection<IPerson> getPeople();
     Collection<IPlayer> getPlayers();
     Collection<IObservable> getObservables();
    Collection<IReferee> getReferees();
     Collection<IClub> getClubs();
 
     //returns all the people with the correct group
     Collection<IPerson> getPeopleForGroup(String group);
 
     //returns the datum that corresponds to the id.  If the datum does
     //not implement the correct interface or does not exist, returns
     //null
     IData getData(int id);
     IPerson getPerson(int id);
     IPlayer getPlayer(int id);
     IObservable getObservable(int id);
     IClub getClub(int id);
    IReferee getReferee(int id);
 
     //mutator methods must be run in a transaction.  If these are
     //called outside of a transaction, an exception will be thrown
     
     //adds a datum to the datastore
     void putData(IData person);
     //removes the datum from the datastore
     void removeData(IData person);
     void removeID(int id);
 
     //creates an object with an ID that is unique to the DataStore
     IPerson createSpectator(String phoneNumber, String carrier, String group);
     IPlayer createPlayer(String phoneNumber, String carrier, String group,
                          String rating, int rank, PlayerSeed seed);
     IClub createClub(String name);
     IReferee createReferee(String phoneNumber, String carrier, String group);
 
     //Runs the runnable inside a transaction block.
     void runTransaction(Runnable transaction);
 }
