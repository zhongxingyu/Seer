 package data;
 
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 public class User {
 	public HashSet<User> family = new HashSet<User>();
 	public List<Update> updates = new LinkedList<Update>();
 
 	public List<RatingUpdate> myUpdates = new LinkedList<RatingUpdate>();
 
 	public String id;
 	
 	public User(String id) {
 		this.id = id;
 	}
 	
 	public ArrayList<Update> getFutureUpdates(Time t) {
 		ArrayList<Update> output = new ArrayList<Update>();
 		for (Update u: updates) {
 			if (u.time.after(t)) {
 				output.add(u);
 			} else {
 				break;
 			}
 		}
 		return output;
 	}
 	
 	public void addUpdate(Update u) {
 		for (int i=0;i<updates.size();i++) {
 			if (updates.get(i).time.before(u.time)) {
 				updates.add(i,u);
 				break;
 			}
 		}
 		if (updates.size()==0) {
 			updates.add(0, u);
 		}
 	}
 	
 	public void receiveUpdate(RatingUpdate u) {
 			for (User user: family) {
 				if (!user.equals(this)) {
 					user.addUpdate(u);
 				}
 		}
 		myUpdates.add(u);
 	}
 	
 	/**
 	 * This has been added or removed from a family
 	 * @param u
 	 */
 	public void receiveUpdate(MemberUpdate u) {
 		addUpdate(u);

		if (u.add) {
			family.add(u.adder);
			u.adder.family.add(this);
			for (Update up:u.adder.myUpdates) {
				addUpdate(up);
			}
			for (Update up:myUpdates) {
				u.adder.addUpdate(up);
			}
		} else {
			try {
				family.remove(u.adder);
				u.adder.family.remove(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return id.toString();
 	}
 }
