 package final_project.tests.mocks;
 
 import java.util.Collection;
 
 import final_project.model.IClub;
 import final_project.model.IData;
 import final_project.model.IDataStore;
 import final_project.model.IObservable;
 import final_project.model.IPerson;
 import final_project.model.IPlayer;
 import final_project.model.IReferee;
 import final_project.model.PlayerSeed;
 
 public class MockDataStore implements IDataStore {
 
 	public IClub createClub(String name) {
 		return null;
 	}
 
 	public IPlayer createPlayer(String phoneNumber, String firstName,
 			String lastName, String carrier, String group, int rank,
 			PlayerSeed seed) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IReferee createReferee(String phoneNumber, String firstName,
 			String lastName, String carrier, String group) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IPerson createSpectator(String phoneNumber, String firstName,
 			String lastName, String carrier, String group) {
 		System.out.println("Create spectator called");
 
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IClub getClub(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IClub> getClubs() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IData> getData() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IData getData(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IObservable getObservable(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IObservable> getObservables() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IPerson> getPeople() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IPerson> getPeopleForGroup(String group) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IPerson getPerson(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IPlayer getPlayer(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IPlayer> getPlayers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IReferee getReferee(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Collection<IReferee> getReferees() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void putData(IData person) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void removeData(IData person) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void removeID(int id) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void runTransaction(Runnable transaction) {
 		// TODO Auto-generated method stub
 
 	}

    public Collection<IPerson> getPeopleWithoutClub(String club) {
    }
 }
