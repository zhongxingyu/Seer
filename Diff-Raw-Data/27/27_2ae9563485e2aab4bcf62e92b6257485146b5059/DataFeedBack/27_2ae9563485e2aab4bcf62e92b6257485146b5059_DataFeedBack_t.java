 import org.bson.types.ObjectId;
 
 
 public class DataFeedBack implements Data {
 
 	private ObjectId id;
 	private boolean clicked;
 	private ObjectId recoGiver; //la personne qui a donné la recommendation (le recommendeur)
 	private ObjectId recoReceiver; // la persionne qui a recu la recommendation
 	
 	public DataFeedBack(ObjectId id, boolean clicked, ObjectId recoGiver, ObjectId recoReceiver) {
 			this.id =id;
 			this.clicked= clicked;
 			this.recoGiver= recoGiver;
 			this.recoReceiver= recoReceiver;
 	}
 	
 	
	@Override
	public String toString() {
		return "DataFeedBack [clicked=" + clicked + ", id=" + id
				+ ", recoGiver=" + recoGiver + ", recoReceiver=" + recoReceiver
				+ "]";
	}


 	public boolean clicked() {
 		return this.clicked;
 	}
 
 
 	@Override
 	public ObjectId getMongoId() {
 		return this.id;
 	}
 
 
 	public ObjectId recoGiver() {
 		return this.recoGiver;
 	}
 	
 	public ObjectId recoReceiver() {
 		return this.recoReceiver;
 	}
 }
