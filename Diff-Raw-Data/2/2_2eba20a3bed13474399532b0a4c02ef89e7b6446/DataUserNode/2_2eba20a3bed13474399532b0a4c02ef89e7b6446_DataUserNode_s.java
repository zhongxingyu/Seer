 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.bson.types.ObjectId;
 
 
 public class DataUserNode implements Data {
 	private ObjectId id; //l'id qui est dans mongo
 	private String name;
 	private ArrayList<DataUserRelation> friends; //TODO : changer en recommenders
 	private ArrayList<DataUPage> uPages;
 	double uPageMean; //moyenne des page rank des UPages.
 
 	
 	public DataUserNode(ObjectId id) {
 		this.id = id;
 	}
 	
 	public DataUserNode(ObjectId id, ArrayList<DataUPage> dataupages) {
 		this.id = id;
 		this.uPages = dataUpages;
 		this.friends= new ArrayList<DataUserRelation>();
 		double uPageMean=0;
 		for (DataUPage uPage:uPages)
 		{
 			uPageMean+= uPage.pageRank;
 		}
 		if (uPages.size()!=0)
 			uPageMean= uPageMean/uPages.size();
 	}
 	
 	public DataUserNode(ArrayList<DataUserRelation> friend,ObjectId id) {
 		this.id = id;
 		this.friends = friend;
 	}
 	
 	public DataUserNode(String name, ObjectId id, ArrayList<DataUserRelation> friends, ArrayList<DataUPage> uPages)
 	{
 		this.id = id;
 		this.name=name;
 		this.friends=friends;
 		this.uPages=uPages;
 		uPageMean=0;
 		for (DataUPage uPage:uPages)
 		{
 			uPageMean+= uPage.pageRank;
 		}
 		if (uPages.size()!=0)
 			uPageMean= uPageMean/uPages.size();
 	}
 
 	public ArrayList<DataUserRelation> getFriends() {
 		return this.friends;
 	}
 	
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	@Override
 	public ObjectId getMongoId() {
 		return this.id;
 	}
 	
 	public void setFriends(ArrayList<DataUserRelation> userrelations) {
 		this.friends = userrelations;
 	}
 	
 	public boolean updateProbabilities()
 	{
 		boolean change= false;
 		for (DataUserRelation userR : friends)
 		{
			change= change || userR.updateProbability(this);
 		}
 		
 		return change;
 	}
 	
 	public ArrayList<DataUPage> getUPages() {
 		return this.uPages;
 	}
 
 	public ObjectId getId() {
 		return this.getMongoId();
 	}
 
 	public void setId(ObjectId id) {
 		this.id = id;
 	}
 
 	@Override
 	public String toString() {
 		String uPagesList = "";
 		
 		for (DataUPage uPage : uPages)
 		{
 			uPagesList += uPage.toString()+ "\n";
 		}
 		
 		String friendsList = "";
 		
 		for (DataUserRelation friend : friends)
 		{
 			friendsList += friend+ "\n";
 		}
 		
 		return "DataUserNode [id=" + id + ", name=" + name + ", friends="
 				+ friendsList + ", uPages=" + uPagesList + ", uPageMean=" + uPageMean
 				+ "]";
 	}
 	
 	
 	
 	
 
 
 	
 
 
 
 	
 
 }
