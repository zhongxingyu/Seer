 package net.erickelly.huskyhunters.data;
 
 public class CluesDao {
 	
	private CluesDao() {}
	private CluesDao INSTANCE;
	
	public CluesDao getInstance() {
		if (INSTANCE != null) {
			INSTANCE = new CluesDao();
		}
		return INSTANCE;
	}
 }
