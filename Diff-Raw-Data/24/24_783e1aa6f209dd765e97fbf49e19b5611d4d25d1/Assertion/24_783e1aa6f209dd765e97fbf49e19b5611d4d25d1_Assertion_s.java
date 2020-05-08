 package jdressel.DerporiaPrime;
 import java.util.*;
 import java.util.UUID;
 /**	
  * 	This class contains assertions made by users
  * 	@author James Robertson
  */
 public class Assertion implements Comparable{
 	private int convinced = 0;
 	private int disagree = 0;
 	private int unsure = 0;
 	private final String UN;
 	private String title;
 	private String body;
	private UUID id;
 	
 	public List<User> getVoters() {
 		return voters;
 	}
 
 	private List<User> voters;
 	
		public Assertion(String username, String title, String body) {
 		super();
 		UN = username;
 		this.title= title;
 		this.body = body;
		id = UUID.randomUUID();
 	}
 
 	public String getId(){
 		return id.toString();
 	}
 		
 	public String getName() {
 		return title;
 	}
 	public void setName(String title) {
 		this.title= title;
 	}
 	
 	public String getBody() {
 		return body;
 	}
 	public void setBody(String body) {
 		this.body = body;
 	}
 	
 	public String getUN() {
 		return UN;
 	}
 	
 	public int getConvinced() {
 		return convinced;
 	}
 	
 	public void setConvinced(int i) {
 		convinced = i;
 	}
 
 	public void setUnsure(int i) {
 		unsure = i;
 	}
 
 	public int getDisagree() {
 		return disagree;
 	}
 
 	public void setDisagree(int i) {
 		disagree = i;
 	}
 	
 	public int getUnsure() {
 		return unsure;
 	}
 	
 	/**		Increments convinced if u has not already voted on this Assertion
 	 * 		@author James Robertson
 	 * 
 	 * 		@return True if the vote was added successfully
 	 * 		@return False if the user has already voted on this Assertion in this way
 	 * 		@throws NullPointerException if u is null
 	 */
 	public boolean voteConvinced(User u){
 		if(u==null)
 			throw new NullPointerException("User cannot be null");
 		u.voteConvinced(this);
 		convinced++;
 		return true;
 	}
 	
 	/**		Increments disagree if u has not already voted on this Assertion
 	 * 		@author James Robertson
 	 * 
 	 * 		@return True if the vote was added successfully
 	 * 		@return False if the user has already voted on this Assertion in this way
 	 * 		@throws NullPointerException if u is null
 	 */
 	public boolean voteDisagree(User u){
 		if(u==null)
 			throw new NullPointerException("User cannot be null");
 		u.voteDisagree(this);
 		disagree++;
 		return true;
 	}
 
 	/**		Increments unsure if u has not already voted on this Assertion
 	 * 		@author James Robertson
 	 * 
 	 * 		@return True if the vote was added successfully
 	 * 		@return False if the user has already voted on this Assertion in this way
 	 * 		@throws NullPointerException if u is null
 	 */
 	public boolean voteUnsure(User u){
 		if(u==null)
 			throw new NullPointerException("User cannot be null");
 		u.voteUnsure(this);
 		unsure++;
 		return true;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 17;
 		int result = 1;
 		result = prime * result + ((UN == null) ? 0 : UN.hashCode());
 		result = prime * result + ((body == null) ? 0 : body.hashCode());
 		result = prime * result + ((title == null) ? 0 : title.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		Assertion other = (Assertion) obj;
 		if (UN == null) {
 			if (other.UN != null)
 				return false;
 		} else if (!UN.equals(other.UN))
 			return false;
 		if (body == null) {
 			if (other.body != null)
 				return false;
 		} else if (!body.equals(other.body))
 			return false;
 		if (title == null) {
 			if (other.title != null)
 				return false;
 		} else if (!title.equals(other.title))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString(){
 		return("User name: " + UN + " Claim: "+ title + " Body: "+body + " Convinced: " + convinced + " Disagree: " + disagree + " unsure: " + unsure + "UUID: " + id);
 	}
 	
 	/**		Compares two Assertions by their number of votes, first by convinced then by subtracting disagree
 	 * 		@author James Robertson
 	 * 
 	 * 		@param o the Object to be compared to
 	 * 		@return 1 if this is greater than o
 	 * 		@return 0 if they are equal
 	 * 		@return -1 if this is less than o
 	 * 		@throws ClassCastException if o is not an Assertion
 	 */
 	public int compareTo(Object o){
 		if (getClass() != o.getClass())
 			throw new ClassCastException();
 		if(this.getConvinced()==((Assertion) o).getConvinced()){
 			
 			if(this.getDisagree()==((Assertion) o).getDisagree())
 				return 0;
 			
 			else if(this.getDisagree()>((Assertion) o).getDisagree())
 				return 1;
 			
 			else return -1;
 		}
 		
 		else if(this.getConvinced()>((Assertion) o).getConvinced())
 			return -1;
 		
 		else return 1;
 	}
 }
