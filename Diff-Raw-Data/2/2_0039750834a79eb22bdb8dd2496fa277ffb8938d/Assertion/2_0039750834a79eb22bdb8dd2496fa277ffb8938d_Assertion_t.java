 package jdressel.Derporia64;
 import java.util.*;
 import java.util.UUID;
 /**	
  * 	This class contains assertions made by users
  * 	@author James Robertson
  */
 public class Assertion {
 	private int convinced = 0;
 	private int disagree = 0;
 	private int unsure = 0;
 	private final String UN;
 	private String title;
 	private String body;
 	private UUID id;
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
 		if(u.getDisagree().contains(this))
 			disagree--;
 		else if(u.getConvinced().contains(this))
 			return false;
 		else if(u.getUnsure().contains(this))
 			unsure--;
 		if(!voters.contains(u))
 			voters.add(u);
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
 		if(u.getDisagree().contains(this))
 			return false;
 		else if(u.getConvinced().contains(this))
 			convinced--;
 		else if(u.getUnsure().contains(this))
 			unsure--;
 		if(!voters.contains(u))
 			voters.add(u);
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
 		if(u.getDisagree().contains(this))
 			disagree--;
 		else if(u.getConvinced().contains(this))
 			convinced--;
 		else if(u.getUnsure().contains(this))
 			return false;
 		if(!voters.contains(u))
 			voters.add(u);
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
 	
 }
