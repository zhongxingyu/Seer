 package source.code;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 
 import android.app.Application;
 import android.content.Intent;
 
 public class Global extends Application 
 {
 	private ArrayList<Lunch> lunchInvites;
 	private ArrayList<Lunch> lunchesAttending;
 	private ArrayList<Lunch> lunchReminders;
 	private Lunch currentCreatingLunch;
 	private Lunch currentClickedLunch;
 	private ArrayList<Friend> lunchFriends;
 	private FriendListAdapter<Friend> friendListAdapter;
 	private Calendar startTime;
 	private Lunch fakeInvite;
 	private boolean fakeInviteBool = false;
 
 	public void removeOldLunches(){
 		ArrayList<Lunch> toberemoved = new ArrayList<Lunch>();
 		for(Lunch lunch:lunchInvites){
 			if(lunch.getLunchTime().getTimeInMillis()<System.currentTimeMillis()){
 				toberemoved.add(lunch);
 			}
 		}
 		lunchInvites.removeAll(toberemoved);
 		toberemoved.clear();
 		for(Lunch lunch:lunchesAttending){
 			if(lunch.getLunchTime().getTimeInMillis()<System.currentTimeMillis()){
 				toberemoved.add(lunch);
 				System.out.println("removed"+ lunch.getTitle());
 			}
 		}
 		lunchesAttending.removeAll(toberemoved);
 		toberemoved.clear();
 	}	
 	
     public void makeLunches()
     {
         if (lunchInvites == null) {
             
             Calendar systemTime = Calendar.getInstance();
             startTime = (Calendar)systemTime.clone();
             Calendar firstLunchTime = (Calendar)systemTime.clone();
             firstLunchTime.add(Calendar.DAY_OF_YEAR, 1);  
             firstLunchTime.add(Calendar.MINUTE, 35);  
         	ArrayList<Friend> attending = new ArrayList<Friend>();
         	attending.add(new Friend("Anjali Muralidhar"));
         	attending.add(new Friend("Michael Puncel"));
         	attending.add(new Friend("Pallavi Powale"));
             lunchInvites = new ArrayList<Lunch>();
             lunchesAttending = new ArrayList<Lunch>();
             lunchReminders = new ArrayList<Lunch>();
             Lunch tbell = new Lunch("Taco Bell");
             tbell.setLunchTime(firstLunchTime);
             tbell.setFriends(attending);
             tbell.addAcceptedFriend(attending.get(0));
             tbell.addAcceptedFriend(attending.get(1));
             Lunch cosi = new Lunch("Cosi");
             Calendar secondLunchTime = (Calendar)firstLunchTime.clone();
             secondLunchTime.add(Calendar.DAY_OF_YEAR, 1);
             secondLunchTime.add(Calendar.MINUTE, 35);
             cosi.setLunchTime(secondLunchTime);
             cosi.setFriends(attending);
             cosi.addAcceptedFriend(attending.get(2));
             cosi.addAcceptedFriend(attending.get(1));
             Lunch masa = new Lunch("Masa");
             Calendar thirdLunchTime = (Calendar)secondLunchTime.clone();
             thirdLunchTime.add(Calendar.MINUTE, 35);
             thirdLunchTime.add(Calendar.DAY_OF_YEAR, 1);
             masa.setLunchTime(thirdLunchTime);
             masa.setFriends(attending);
             masa.addAcceptedFriend(attending.get(0));
             masa.addAcceptedFriend(attending.get(1));
             lunchInvites.add(tbell);
             lunchInvites.add(cosi);
             lunchInvites.add(masa);
             
             Lunch dhaba = new Lunch("Desi Dhaba");
             Calendar attendTime = (Calendar)systemTime.clone();
             attendTime.add(Calendar.MINUTE, 35);
             dhaba.setLunchTime(attendTime);
             dhaba.setFriends(attending);
             dhaba.addAcceptedFriend(attending.get(0));
             dhaba.addAcceptedFriend(attending.get(1));
             dhaba.setConfirmationRequested(true);
             dhaba.setReminderTime(34);
             
             
             Lunch maggianos = new Lunch("Maggiano's");
             Calendar attendTime2 = (Calendar)systemTime.clone();
             attendTime2.add(Calendar.MINUTE, 36);
             maggianos.setLunchTime(attendTime2);
             maggianos.setFriends(attending);
             maggianos.addAcceptedFriend(attending.get(2));
             maggianos.addAcceptedFriend(attending.get(1));
             addLunchAttending(dhaba);
             addLunchAttending(maggianos);
             
             Intent intent = new Intent(this, NotificationService.class);
             startService(intent);
             
             fakeInvite = new Lunch("Cinderella's");
            Calendar fakeTime = (Calendar)attendTime.clone();
            fakeTime.add(Calendar.DAY_OF_MONTH, 1);
             fakeInvite.setLunchTime(attendTime);
             fakeInvite.setFriends(attending);
             fakeInvite.addAcceptedFriend(attending.get(2));
         }
     }
     
     public Lunch getFakeLunch() {
         return this.fakeInvite;
     }
     
     
     public Calendar getStartTime() {
         return this.startTime;
     }
     
     
 	public void createLunchDone() {
 		
 	    int insertionindex = 0;
 	    for(Lunch lunch:lunchesAttending){
 	    	int compare=currentCreatingLunch.compareTo(lunch);
 	    	if(compare>0){
 	    		insertionindex +=1;
 	    	}
 	    }
 	    addLunchAttending(currentCreatingLunch, insertionindex);
 	}
 	
 	public Lunch getCurrentCreatingLunch() {
 	    return currentCreatingLunch;
 	}
 	public void setCurrentCreatingLunch(Lunch l) { 
 	    currentCreatingLunch = l;
 	}
 	
 	public Lunch getCreatingLunch() {
 	    return this.currentCreatingLunch;
 	}
 	
 	public void setCurrentClickedLunch(Lunch l) { 
 	    currentClickedLunch = l;
 	}
 	
 	public Lunch getCurrentClickedLunch() {
 	    return this.currentClickedLunch;
 	}
 	
 	
 	public ArrayList<Friend> getLunchFriends() {
 	    return lunchFriends;
 	}
 	
 	public void setFakeInviteBool(boolean bool) {
 	    this.fakeInviteBool = bool;
 	}
 	
 	public boolean getFakeInviteBool() {
 	    return this.fakeInviteBool;
 	}
 	
 	public void addLunchInvite(Lunch lunch){
 	    int insertionindex = 0;
 	    for(Lunch l:lunchInvites){
 	    	int compare=lunch.compareTo(l);
 	    	if(compare>0){
 	    		insertionindex +=1;
 	    	}
 	    }
 		lunchInvites.add(insertionindex,lunch);
 	}
 	
 	public synchronized void addLunchAttending(Lunch lunch) {
        lunchesAttending.add(lunch);
        if(lunch.getReminderTime() != null)
        {
 	        if (lunchReminders.size() == 0 ) {
 	            lunchReminders.add(lunch);
 	        }
 	        
 	        else {
 	            for (int i = 0; i < lunchReminders.size(); i++) {
 	                if (lunchReminders.get(i).getReminderTime().after(lunch.getReminderTime())){
 	                    lunchReminders.add(i, lunch);
 	                    break;
 	                }
 	                if (i == lunchReminders.size() - 1) {
 	                    lunchReminders.add(lunch);
 	                    break;
 	                }
 	            }
 	        }
        }
 	}
 	
 	public synchronized void addLunchAttending(Lunch lunch, int index){
 	    lunchesAttending.add(index, lunch);
 	       if(lunch.getReminderTime() != null)
 	       {
 		        if (lunchReminders.size() == 0 ) {
 		            lunchReminders.add(lunch);
 		        }
 		        
 		        else {
 		            for (int i = 0; i < lunchReminders.size(); i++) {
 		                if (lunchReminders.get(i).getReminderTime().after(lunch.getReminderTime())){
 		                    lunchReminders.add(i, lunch);
 		                    break;
 		                }
 		                if (i == lunchReminders.size() - 1) {
 		                    lunchReminders.add(lunch);
 		                    break;
 		                }
 		            }
 		        }
 	       }
 	}
 	
 	public synchronized Lunch getNextReminder() {
 	    return lunchReminders.get(0);
 	}
 	
 	public synchronized int numLunchReminders() {
 	    return lunchReminders.size();
 	}
 	
 	public synchronized void lunchReminded() {
 	    lunchReminders.remove(0);
 	}
 	
 	public void removeLunchInvite(String lunchTitle){
 		for (int i = 0; i < lunchInvites.size(); i++)
 		{
 			if (lunchTitle.startsWith(lunchInvites.get(i).getTitle()))
 			{
 				lunchInvites.remove(i);
 				break;
 			}
 		}
 	}
 	
 	public void removeLunchesAttending(String lunchTitle){
 		for (int i = 0; i < lunchesAttending.size(); i++)
 		{
 			if (lunchTitle.startsWith(lunchesAttending.get(i).getTitle()))
 			{
 				lunchesAttending.remove(i);
 				break;
 			}
 		}
 		
 		for (int i = 0; i < lunchReminders.size(); i++) {
 		    if (lunchTitle.startsWith(lunchReminders.get(i).getTitle())) {
 		        lunchReminders.remove(i);
 		        break;
 		    }
 		}
 	}
 	
 	public void setLunchInvites(ArrayList<Lunch> lunches)
 	{
 		lunchInvites = lunches;
 	}
 	
 	public void setLunchesAttending(ArrayList<Lunch> lunches)
 	{
 		lunchesAttending = lunches;
 	}
 	
 	public ArrayList<Lunch> getLunchInvites()
 	{
 		return lunchInvites;
 	}
 	
 	public Lunch getLunchInvite(int position) {
 	    return lunchInvites.get(position);
 	}
 	
 	public void removeLunchInvite(int position) {
 	    lunchInvites.remove(position);
 	}
 	
 	public synchronized ArrayList<Lunch> getLunchesAttending() {
 		return lunchesAttending;
 	} 
 	
 	public synchronized int numLunchesAttending() {
 	    return lunchesAttending.size();
 	}
 	
 	public Lunch getLunchAttending(int position) {
 	    return lunchesAttending.get(position);
 	}
 	
 	public void removeLunchAttending(int position) {
 	    lunchesAttending.remove(position);
 	}
 	
 	public void setFriendListAdapter(FriendListAdapter<Friend> friendListAdapter) {
 	    this.friendListAdapter = friendListAdapter;
 	}
 	
 	public FriendListAdapter<Friend> getFriendListAdapter() {
 	    return this.friendListAdapter;
 	}
 	
 }
