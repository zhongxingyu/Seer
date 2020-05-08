 package prefwork.rating.datasource;
 
 import java.util.Random;
 
 import prefwork.core.DataSource;
 import prefwork.core.UserEval;
 import prefwork.rating.Rating;
 import weka.core.Attribute;
 import weka.core.Instances;
 
 public abstract class ContentDataSource implements DataSource{
 	/** Attributes of the dataset */
 	protected Instances instances;
 	protected String[] names = null;
 
 	protected String[] attributeNames;
 	protected long seed;
 	protected int from, to; 
 	
 
 	/** Name of dataset, for statistics*/
 	protected String name;
 
 	/** Index of attribute that contains rating*/
 	protected int classIndex;
 
 	/** Count of possible classes*/
 	protected double[] classes = null;
 	
 	/** Array of users' records. User's records are array of Ratings.*/
 	protected Rating[][] userRecords;
 	
 	int objectIndex = 0;
 	boolean fromRange;
 	
 	/**Index of run. Used for setting random values. */
 	int run = 1;
 
 	int userCount = 1;
 	/**UserId of current user*/
 	protected int currentUser = 0;
 	
 	protected int size = 200;
 	protected int maxLoad = 2000;
 	
 	public Instances getInstances(){
     	return instances;
     }
     
 	public void setInstances(Instances attributes){
 		this.instances = attributes;
 	}
     public String[] getAttributesNames(){
     	if(attributeNames != null)
     		return attributeNames;
     	attributeNames = new String[instances.numAttributes()];
     	for (int i = 0; i < attributeNames.length; i++) {
     		Attribute a = instances.attribute(i);
     		attributeNames[i] = a.name();
 		}
     	return attributeNames;
     }
 
 	public void setUserRecords(Rating[][] userRecords) {
 		this.userRecords = userRecords;
 	}
 	
     public int getClassAttributeIndex(){
     	return classIndex;
     }
     
     public double[] getClasses(){
     	if(classes != null)
     		return classes;
     	return instances.attributeToDoubleArray(classIndex);
     }
 	
 
 	@Override
 	public void shuffleInstances() {
 		Random random = new Random(seed);
 		for (int i = 0; i < userRecords.length; i++) {
 		    for (int j = userRecords[i].length -1; j > 0; j--){
 		        Rating r = userRecords[i][j];
 		        int k = random.nextInt(j+1);
 		        userRecords[i][j] = userRecords[i][k];
 		        userRecords[i][k] = r;
 		    }
 		}
 	}
 
 	@Override
 	public int size() {
 		return Math.min(size,userRecords[currentUser].length);
 	}
 
 	@Override
 	public void setLimit(int from, int to, boolean fromRange) {
 		this.from = from;
 		this.to = to;
 		this.fromRange = fromRange;
 		objectIndex = from;
 	}
 
 	@Override
 	public boolean hasNext() {
 		int i = getNextIndex();
 		return (i < size());
 	}
 
 	@Override
 	public UserEval next() {
		if(this.objectIndex >= size())
 			return null;
 		UserEval object = userRecords[currentUser][objectIndex];
 		objectIndex++;
 		objectIndex = getNextIndex();
 		return object;
 	}
 
 	protected Integer getNextIndex(){
 		if(currentUser >= userCount)
 			return null;
 		if (objectIndex >= size())
 			return objectIndex;
 		
 		if(fromRange){
 			if(objectIndex >= from && objectIndex < to )
 				return objectIndex;		
 			// objectIndex is outside of the interval, finish the search.
 			if(objectIndex >= from)
 				return size();
 			// else return from for the start of the current run dataset
 			return from;
 		}
 		if(objectIndex < from || objectIndex >= to )
 				return objectIndex;
 		//Skip the interval between from and to
 		if(from == objectIndex)
 			return to;
 		//End otherwise
 		return size();		
 	}
 	@Override
 	public void restart() {
 		objectIndex = 0;
 		objectIndex = getNextIndex();
 	}
 
 	@Override
 	public void remove() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Integer userId() {
 		if(currentUser+1>=userCount)
 			return null;
 		currentUser++;
 		return currentUser;
 	}
 
 	@Override
 	public boolean hasNextUserId() {
 		if(currentUser+1>=userCount)
 			return false;
 		return true;
 	}
 
 	@Override
 	public void restartUserId() {
 		currentUser=-1;		
 	}
 
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void setName(String name) {
 		this.name = name;		
 	}
 	
 
 	@Override
 	public void setFixedUserId(Integer userId) {
 		currentUser = userId;		
 	}
 
 
 }
