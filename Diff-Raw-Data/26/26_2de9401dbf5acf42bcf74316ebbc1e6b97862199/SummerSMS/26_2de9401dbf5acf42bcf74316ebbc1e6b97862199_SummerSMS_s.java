 /**
  * 
  */
 package bzb.gae.summer;
 
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.jdo.JDOObjectNotFoundException;
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 
 import bzb.gae.PMF;
 import bzb.gae.exceptions.BadArrivalTimeException;
 import bzb.gae.exceptions.TooFewArgumentsException;
 import bzb.gae.exceptions.TooManyArgumentsException;
 import bzb.gae.exceptions.UserAlreadyExistsException;
 import bzb.gae.summer.jdo.Group;
 import bzb.gae.summer.jdo.User;
 
 import com.google.appengine.api.datastore.Key;
 
 /**
  * @author bzb
  * 
  */
 public class SummerSMS {
 
 	private static final Logger log = Logger.getLogger(SummerSMS.class.getName());
 	private final static int EXPECTED_PARAMETERS = 3;
 
 	private String originator;
 	private String username;
 	private String arrivalTime;
 	private Key groupKey;
 
 	public SummerSMS(String originator, String[] smsChunks)
 			throws UserAlreadyExistsException, BadArrivalTimeException,
 			TooManyArgumentsException, TooFewArgumentsException {
 		if (smsChunks.length > EXPECTED_PARAMETERS) {
 			throw new TooManyArgumentsException();
 		} else if (smsChunks.length < EXPECTED_PARAMETERS) {
 			throw new TooFewArgumentsException();
 		} else {
 			setOriginator(originator);
 			checkDetails(smsChunks);
 			checkGroup();
 			if (getUsername() != null) {
 				PersistenceManager pm = PMF.get().getPersistenceManager();
 				User user = new User(getUsername(), getOriginator(),
 						getArrivalTime(), getGroupKey());
 				try {
 					pm.makePersistent(user);
 				} finally {
 					pm.close();
 				}
 			} else {
 				throw new UserAlreadyExistsException();
 			}
 		}
 	}
 
 	private void setOriginator(String originator) {
 		this.originator = originator.trim();
 	}
 
 	public String getOriginator() {
 		return originator;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getUsername() {
 		return username;
 	}
 
 	public void checkDetails(String[] smsChunks) throws BadArrivalTimeException {
 		String username = smsChunks[1].trim();
 		String arrivalTime = parseArrivalTime(smsChunks[2].trim());
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			try {
 				User user = pm.getObjectById(User.class, username);
 				setUsername(username);
 				user.setArrivalTime(arrivalTime);
 				setArrivalTime(arrivalTime);
 				user.setPhoneNumber(getOriginator());
 			} catch (JDOObjectNotFoundException je) {
 				setArrivalTime(arrivalTime);
 				setUsername(username);
 			}
 		} finally {
 			pm.close();
 		}
 	}
 
 	public String parseArrivalTime(String rawArrivalTime)
 			throws BadArrivalTimeException {
 		String arrivalTime = null;
 		Pattern p = Pattern
 				.compile("^((?:[0-1]?[0-9])|(?:2[0-3]))(?:[\\:\\.])?(([0-5][0-9]))$");
 		Matcher m = p.matcher(rawArrivalTime);
 		if (m.matches()) {
 			arrivalTime = m.group(1); // hours
 			if (arrivalTime.length() == 1) {
 				arrivalTime = '0' + arrivalTime;
 			}
 			arrivalTime += m.group(2); // minutes
 		} else {
 			throw new BadArrivalTimeException();
 		}
 		return arrivalTime;
 	}
 
 	public void setArrivalTime(String arrivalTime) {
 		this.arrivalTime = arrivalTime;
 	}
 
 	public String getArrivalTime() {
 		return arrivalTime;
 	}
 
 	public void setGroupKey(Key groupKey) {
 		this.groupKey = groupKey;
 	}
 
 	public Key getGroupKey() {
 		return groupKey;
 	}
 	
 	public void checkGroup () {
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			try {
 				User user = pm.getObjectById(User.class, getUsername());
 				log.warning("User already exists");
 	
 				if (user.getGroupKey() == null || !pm.getObjectById(Group.class, user.getGroupKey()).getArrivalTime().equals(getArrivalTime())) {
					Query q = pm.newQuery("select from " + Group.class.getName() + " where arrivalTime == " + getArrivalTime());
					List<Group> groups = (List<Group>) q.execute();
					if (groups.size() > 0) {
 				    	log.warning("Assigning user to different existing group");
				    	user.setGroupKey(groups.get(0).getKey());
				    	setGroupKey(groups.get(0).getKey());
 				    } else {
 				    	log.warning("Creating a new group for user");
 				    	Group newGroup = new Group(getArrivalTime());
 				    	pm.makePersistent(newGroup);
 				    	user.setGroupKey(newGroup.getKey());
 				    	setGroupKey(newGroup.getKey());
 				    }
 				} else {
 					log.warning("User's existing group is fine");
 				}
 			} catch (JDOObjectNotFoundException je) {
 				log.warning("User doesn't exist yet");
				Query q = pm.newQuery("select key from " + Group.class.getName() + " where arrivalTime == " + getArrivalTime());
 			    List<Key> keys = (List<Key>) q.execute();
			    if (keys.size() > 0) {
 			    	log.warning("Assigning user to different existing group");
 			    	setGroupKey(keys.get(0));
 			    } else {
 			    	log.warning("Creating a new group for user");
 			    	Group newGroup = new Group(getArrivalTime());
 			    	pm.makePersistent(newGroup);
 			    	setGroupKey(newGroup.getKey());
 			    }
 			}
 		} finally {
 			pm.close();
 		}
 	}
 
 }
