 package edu.columbia.e6998.cloudexchange.toolkit;
 import edu.columbia.e6998.cloudexchange.aws.AWSCodes;
 import edu.columbia.e6998.cloudexchange.channel.Msg;
 import edu.columbia.e6998.cloudexchange.client.UserProfile;
 
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.google.appengine.api.users.UserService;
 import com.google.appengine.api.users.UserServiceFactory;
 
 public class GenericToolkit {
 	
 	private DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 	private MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
 	
 		
 	private static GenericToolkit instance = null;
 	
 	private GenericToolkit() {
 		
 	}
 	
 	public static GenericToolkit getInstance() {
 		if (instance == null) {
 			instance = new GenericToolkit();
 		}
 		return instance;
 	}
 
 	SimpleDateFormat dateYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
 	
 	final int REGION 		= 0;
 	final int ZONE 			= 1;
 	final int OS 			= 2;
 	final int INSTANCE_TYPE = 3;
 	final int DATE 			= 4;
 	
 	public String generateProfileKey(String region, String zone, String OS, String instanceType, Date date){
 		StringBuilder sDate = new StringBuilder(dateYYYYMMDD.format(date));
 		
 		String profile =  String.format("%02d", AWSCodes.Region.valueOf(region).ordinal()) 
 						+ String.format("%02d", AWSCodes.Zone.valueOf(zone).ordinal())
 						+ String.format("%02d", AWSCodes.OS.valueOf(OS).ordinal())
 						+ String.format("%02d", AWSCodes.InstanceType.valueOf(instanceType).ordinal())
 						+ sDate;
 		return profile;
 	}
 	
 	public String[] reverseLookUpProfile(String profile){
 		String [] lookup = new String[5];
 
 		lookup[REGION] 			= AWSCodes.Region.values()[Integer.valueOf(profile.substring(0, 2))].toString();
 		lookup[ZONE] 			= AWSCodes.Zone.values()[Integer.valueOf(profile.substring(2, 4))].getZone();
 		lookup[OS] 				= AWSCodes.OS.values()[Integer.valueOf(profile.substring(4, 6))].toString();
 		lookup[INSTANCE_TYPE] 	= AWSCodes.InstanceType.values()[Integer.valueOf(profile.substring(6, 8))].getCode();//.toString();;
 		lookup[DATE] = profile.substring(8, 16);
 		return lookup;
 		
 	}
 	
 	public Date dateConvert(String date) {
 		//TODO needs error handling here
 		try {
 			return (Date) dateYYYYMMDD.parse(date);
 		} catch (ParseException e) {
 			return new Date();
 		}
 	}
 
 	private ArrayList<String> removeProfile(String profile){
 		//TODO fix unchecked
 		@SuppressWarnings("unchecked")
 		ArrayList<String> profiles = (ArrayList<String>) syncCache.get("Profiles");
 		if (profiles == null)
 			return null;
 		if(profiles.remove(profile));
 			syncCache.put("Profiles", profiles);
 		return profiles;
 	}
 
 	private ArrayList<String> addProfile(String profile){
 		//TODO fix unchecked
 		@SuppressWarnings("unchecked")
 		ArrayList<String> profiles = (ArrayList<String>) syncCache.get("Profiles");
 		if (profiles == null)
 			profiles = new ArrayList<String>();
 		if (profiles.contains(profile))
 			return profiles;
 
 		profiles.add(profile);
 		syncCache.put("Profiles", profiles);
 		return profiles;
 
 	}
 	
 	public ArrayList<String> getProfiles(){
 		return removeProfile("xxx");
 	}
 	
 	private ArrayList<String> removeDelete(String delete){
 		//TODO fix unchecked
 		@SuppressWarnings("unchecked")
 		ArrayList<String> deletes = (ArrayList<String>) syncCache.get("Deletes");
 		if (deletes == null)
 			return null;
 		if(deletes.remove(delete));
 			syncCache.put("Deletes", deletes);
 		return deletes;
 	}
 
 	private ArrayList<String> addDelete(String delete){
 		//TODO fix unchecked
 		@SuppressWarnings("unchecked")
 		ArrayList<String> deletes = (ArrayList<String>) syncCache.get("Deletes");
 		if (deletes == null)
 			deletes = new ArrayList<String>();
 		if (deletes.contains(delete))
 			return deletes;
 
 		deletes.add(delete);
 		syncCache.put("Deletes", deletes);
 		return deletes;
 
 	}
 	
 	public ArrayList<String> getDeletes(){
 		return removeDelete("xxx");
 	}
 	
 	
 	public String[][] getBidsOffers(String profile){
 		String[][] results = new String[2][24];
 		//System.out.println("GetBidOffer::Start::" + profile);
 		//if(!syncCache.contains("Profiles"))
 		queryDataStore(profile, "");
 		
 		//can't iterate through null list (but can an empty list..)
 		if (!syncCache.contains(profile)){
 			//System.out.println("Profile is not in memcache yet!");
 			return results;	
 		}
 		for(Entity t : (Entity[]) syncCache.get(profile)){
 			if (t!= null){
 				//System.out.println(t.toString());
 				if ((Boolean) t.getProperty("seller"))
 					results[1][Integer.parseInt(((Entity) t).getProperty("hour").toString())] = ((Entity) t).getProperty("price").toString();
 				else
 					results[0][Integer.parseInt(((Entity) t).getProperty("hour").toString())] = ((Entity) t).getProperty("price").toString();
 			}
 		}
 		//System.out.println("GetBidOffer::End::" + profile);
 		return results;
 	}
 	
 	public String queryDataStore(){
 		syncCache.clearAll();
 		queryDataStore("", "");
 		return "Done";
 	}
 	
 	public Entity[] queryDataStore(String optProfile, String forcedDelete){
 
 		Entity[] tmpList = new Entity[48];
 		String memKey = "";
 		int index = 0;
 //		ArrayList<String> deletes = new ArrayList<String>();
 //		deletes = getDeletes();
 //		if (deletes==null)
 //			deletes = 
 //		deletes.add(forcedDelete);
 		
 		Query qSeller = new Query("Contract");
 		qSeller.addFilter("active", Query.FilterOperator.EQUAL, true);
 		qSeller.addFilter("seller", FilterOperator.EQUAL, true);
 		if (!optProfile.equals("")){
 			qSeller.addFilter("profile", FilterOperator.EQUAL, optProfile);
 			syncCache.delete(optProfile);
 			removeProfile(optProfile);
 			}
 		qSeller.addSort("price", Query.SortDirection.DESCENDING);
 
 		List<Entity> rSellers = datastore.prepare(qSeller).asList(FetchOptions.Builder.withDefaults());
 		for(Entity e: rSellers){
 			memKey = (String) e.getProperty("profile");
 			tmpList = (Entity[]) syncCache.get(memKey);
 			index = hourToIndex(((String) e.getProperty("hour")), ((Boolean) e.getProperty("seller")));
 			if (e!=null)
 				//System.out.println("index:" + index + e.toString());			
 //			if (tmpList != null  && !deletes.contains(e.getKey().toString())){
 			if (tmpList != null){
 				tmpList[index] = e;
 			}else{
 				tmpList = new Entity[48];
 				tmpList[index] = e;
 			}
 			addProfile(memKey);
 			syncCache.put(memKey, tmpList);
 		}
 		
 		Query qBuyer = new Query("Contract");
 		qBuyer.addFilter("active", Query.FilterOperator.EQUAL, true);
 		qBuyer.addFilter("seller", FilterOperator.EQUAL, false);
 		if (!optProfile.equals(""))
 			qBuyer.addFilter("profile", FilterOperator.EQUAL, optProfile);
 		qBuyer.addSort("price", Query.SortDirection.ASCENDING);
 
 		
 		List<Entity> rBuyers = datastore.prepare(qBuyer).asList(FetchOptions.Builder.withDefaults());
 		for(Entity e: rBuyers){
 			memKey = (String) e.getProperty("profile");
 			tmpList = (Entity[]) syncCache.get(memKey);
 			index = hourToIndex(((String) e.getProperty("hour")), ((Boolean) e.getProperty("seller")));
 			if (e!=null)
 				//System.out.println("index:" + index + e.toString());
 //			if (tmpList != null && !deletes.contains(e.getKey().toString())){
 			if (tmpList != null){
 					tmpList[index] = e;
 			}else{
 				tmpList = new Entity[48];
 				tmpList[index] = e;
 			}
 			
 			addProfile(memKey);
 			syncCache.put(memKey, tmpList);
 		}
 				
 		return tmpList;
 	}
 
 	public Msg createBidOffer(String profile, double price, String user, String arrayIndex){
 		String ami = AWSCodes.getDefaultAMI(profile);
 		
 		//Check if autotransact applies
 		if(autoTransaction(Integer.valueOf(arrayIndex)%2 != 0, price, profile, arrayIndex)){
 			int index = Integer.valueOf(arrayIndex);
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			if (Integer.valueOf(arrayIndex)%2 != 0){
 				//hits
 				return createTransaction(profile, String.format("%02d", index - 1), user, ami, "SG", "KP");
 			}else{
 				//lifts
 				return createTransaction(profile, String.format("%02d", index + 1), user, ami, "SG", "KP");
 			}
 		}
 		
 		Transaction txn = datastore.beginTransaction();
 		
 		Entity contract = new Entity("Contract");
 		String[] lookup = reverseLookUpProfile(profile);
 		contract.setProperty("profile", 		profile);
 		contract.setProperty("date", 			dateConvert(lookup[DATE]));
 		contract.setProperty("qty", 			1);
 		contract.setProperty("price", 			price);
 		contract.setProperty("hour", 			indexToHour(arrayIndex));
 		contract.setProperty("user", 			user);
 		contract.setProperty("region", 			lookup[REGION]);
 		contract.setProperty("zone", 			lookup[ZONE]);
 		contract.setProperty("OS", 				lookup[OS]);
 		contract.setProperty("instanceType", 	lookup[INSTANCE_TYPE]);
 		contract.setProperty("seller", 			Integer.valueOf(arrayIndex)%2 != 0);
 		contract.setProperty("active", 			true);
 
 		datastore.put(contract);
 		txn.commit();
 		return updateMemcache((Boolean) contract.getProperty("seller"), profile, arrayIndex, contract);
 	}
 	
 	public Boolean autoTransaction(Boolean seller, double price, String profile, String arrayIndex){
 		//check if there is a counterpart for it
 		Entity[] tmpList = (Entity[]) syncCache.get(profile);
 		int index = Integer.valueOf(arrayIndex);
 		if(tmpList == null){
 			//Shouldnt happen
 			return false;
 		}
 		
 		Entity m;
 		
 		if (seller){
 			m = tmpList[index - 1];
 		}else{
 			m = tmpList[index+1];
 		}
 		
 		if (m==null){
 			return false;
 		}
 		
 		 if(((Double) m.getProperty("price")).equals(price))
 			 return true;
 		 
 		return false;
 	}
 	public Msg createTransaction(String profile, String arrayIndex, String buyer, String ami, String securityGroupName, String keyPairName){
 		
 		Entity offer = ((Entity[])syncCache.get(profile))[Integer.valueOf(arrayIndex)];
 
 		String[] lookup = reverseLookUpProfile(profile);
 		
 		Entity transaction = new Entity("Transaction");
 		transaction.setProperty("profile",profile);
 		transaction.setProperty("buyer", buyer);
 		transaction.setProperty("seller", 			offer.getProperty("user"));
 		transaction.setProperty("is_buy", 			offer.getProperty("seller"));
 		transaction.setProperty("price", 			offer.getProperty("price"));
 		transaction.setProperty("date", 			offer.getProperty("date"));
 		transaction.setProperty("time", 			indexToHour(arrayIndex));	//calendar always 1 hour after this
 		transaction.setProperty("ami", 				ami);			//variable - user provided
 		transaction.setProperty("instanceType", 	lookup[INSTANCE_TYPE]);	//micro, large
 		transaction.setProperty("region", 			lookup[REGION]);//usa, jp etc
 		transaction.setProperty("zone", 			lookup[ZONE]);	//variable
 	//	transaction.setProperty("securityGroup", 	securityGroupName);	//user provided or system generated by http and ssh access only
 	//	transaction.setProperty("keyPair",			keyPairName);
 		transaction.setProperty("instanceID", 		"N/A");
 		transaction.setProperty("priceExecuted", 	"N/A");
 		
 		datastore.put(transaction);
 		//Entity[] mem = deleteBidOffer(profile, arrayIndex);
 		deleteBidOffer(profile, arrayIndex);
 //		for(Entity e: mem){
 //			if(e!=null)
 //				System.out.println(e.toString());
 //		}
 //		System.out.println("delete done");
 		return updateMemcache((Boolean) offer.getProperty("seller"), profile, arrayIndex, null);
 	}
 	
 	public String createTestTransaction(Date date, String time) {
 		
 		Entity transaction = new Entity("Transaction");
 		transaction.setProperty("profile", "0000000020110101");
 		transaction.setProperty("buyer", "114224896744063045840"); // fedotoveugene@gmail.com
 		transaction.setProperty("seller", "110709289717792221869"); // 
 		transaction.setProperty("is_buy", true);
 		transaction.setProperty("price",  0.05);
 		transaction.setProperty("date",  date); // date in MMM-dd format
 		transaction.setProperty("time",  time); // time in 24-hour format (16 = 4pm)
 		transaction.setProperty("ami", 	"ami-8c1fece5"); //variable - user provided
 		transaction.setProperty("instanceType", "t1.micro");	//micro, large
 		transaction.setProperty("region", "US East");//usa, jp etc
 		transaction.setProperty("zone", "us-east-1a");	//variable
 		//transaction.setProperty("securityGroup", "NewSecurityGroup");	user provided or system generated by http and ssh access only
 		//transaction.setProperty("keyPair",	"MyKeyPair");
 		transaction.setProperty("instanceID",  "N/A"); // will be populated once the instance is launched
 		transaction.setProperty("priceExecuted", "N/A"); // actual spot price - will be populated once the instance is lanched
 		datastore.put(transaction);
 		return "put into datastore";
 	}
 	
 	// Get the list of transaction for which we have not yet launched an instance
 	public List<Entity> getOpenTransactions() {
 		List<Entity> openTransactions = null;
 		Query q = new Query("Transaction");
 		q.addFilter("instanceID", Query.FilterOperator.EQUAL, "N/A");
 		openTransactions = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
 		return openTransactions;
 	}
 	
 	// update transaction entry once the instance is launched
 	public void updateTransaction(Key key, String propertyName, String value) {
 		try {
 			Entity transaction = datastore.get(key);
 			transaction.setProperty(propertyName, value);
 			datastore.put(transaction);
 		} catch (EntityNotFoundException e) {
 			System.err.print(e.getMessage());
 		}
 	}
 	
 	// Get user profile entity for a specified userId
 	public Entity getUserProfileForUser(String userId) {
 		Query q = new Query("UserProfile");
 		q.addFilter("userId", Query.FilterOperator.EQUAL, userId);
 		Entity userProfile = datastore.prepare(q).asSingleEntity();
 		return userProfile;
 	}
 	
 	// update user profile property
 	public void updateUserProfile(String userId, String propertyName, String value) {
 		try {
 			Query q = new Query("UserProfile");
 			q.addFilter("userId", Query.FilterOperator.EQUAL, userId);
 			Entity userProfile = datastore.prepare(q).asSingleEntity();
 			userProfile.setProperty(propertyName, value);
 			datastore.put(userProfile);
 		} catch (Exception e) {
 			System.err.print(e.getMessage());
 		}
 	}
 	
 	// Return e-mail addresses for buyer - String[0], and seller - String[1]
 	public UserProfile[] getBuyerSellerProfileForTransaction(Key key) {
 		UserProfile[] profiles = new UserProfile[2];
 		try {
 			Entity transaction = datastore.get(key);
 			String buyerId = (String) transaction.getProperty("buyer");
 			String sellerId = (String) transaction.getProperty("seller");
 			String buyerEmail = (String) getUserProfileForUser(buyerId).getProperty("email");
 			String sellerEmail = (String) getUserProfileForUser(sellerId).getProperty("email");
 			profiles[0] = new UserProfile(buyerId, buyerEmail);
 			profiles[1] = new UserProfile(sellerId, sellerEmail);
 		} catch (EntityNotFoundException e) {
 			System.err.print(e.getMessage());
 		}
 		return profiles;
 	}
 	
 	// Create charge for the seller
 	public void createCharge(Key key, String instanceId, String userId, String amount, String type) {
 		Entity charge = new Entity("Charge");
 		charge.setProperty("transactionKey", key);
 		charge.setProperty("type", type);
 		charge.setProperty("userId", userId);
 		charge.setProperty("amount", amount);
 		datastore.put(charge);
 	}
 	
 	// Return all charges for a given user
 	public List<Entity> getChargesForUser(String userId) {
 		Query q = new Query("Charge");
 		q.addFilter("userId", Query.FilterOperator.EQUAL, userId);
 		List<Entity> charges = datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
 		return charges;
 	}
 	
 	public Msg cancelBidOffer(String key, String userId){
 		Msg msg = null;
 		System.out.println("Key is:" + key);
 		//Key myKey = KeyFactory.stringToKey(key.trim());
 		List<Entity> orders = getMyOrders(userId);
 		Entity c = null;
 		for (Entity e : orders) {
 			if(e.getKey().toString().equals(key))
 				c = e;
 		}
 
 		Transaction txn = datastore.beginTransaction();
 		//Entity e = null;
 
 		datastore.delete(c.getKey());
 		txn.commit();
 		
 		//Check if the key matches what is displayed
 		String profile = (String) c.getProperty("profile");
 		int arrayIndex = hourToIndex((String) c.getProperty("hour"), (Boolean) c.getProperty("seller"));
 		int index = Integer.valueOf(arrayIndex);
 		Entity m = ((Entity[]) syncCache.get(profile))[index];
 		
 		if(m.getKey().equals(c.getKey())){
 			//Entity[] mem = queryDataStore(profile, c.getKey().toString());
 			try {
 				Thread.sleep(500);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			return updateMemcache((Boolean) c.getProperty("seller"), profile, String.format("%02d", arrayIndex), null);
 		}
 		
 		return msg;
 	}
  
 	
 	private void deleteBidOffer(String profile, String arrayIndex) {
 		int index = Integer.valueOf(arrayIndex);
 		Entity e;
 		Transaction txn = datastore.beginTransaction();
 		//e = datastore.get(((Entity[]) syncCache.get(profile))[index].getKey());
 		e = ((Entity[]) syncCache.get(profile))[index];
 		//e.setProperty("active", false);
 		//datastore.put(e);
 		datastore.delete(e.getKey());
 		syncCache.put(profile, null);
 		txn.commit();
 		
 		addDelete(e.getKey().toString());
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		//return null;
 		//return queryDataStore(profile, e.getKey().toString()); 
 	}
 
 	public String profileToKey(String profile, String arrayIndex){
 		return profile + arrayIndex;
 	}
 	
 	private Msg sendChannelMessage(String type, String action, String value, String qty, String profile, String arrayIndex){
 		//Msg msg =  new Msg(type, action, value, qty, profile);
 		int i = Integer.valueOf(arrayIndex);
 		
 		Msg msg = new Msg(type, action, value, "1", profile + String.format("%d", i), "");
 		return msg;
 	}
 	
 	private String indexToHour(String arrayIndex){
 		int index = Integer.valueOf(arrayIndex);
 		if(index%2 == 0)
 			return String.format("%02d", index/2);
 		else
 			return String.format("%02d", (index - 1)/2);
 	}
 	
 	private int hourToIndex(String hour, Boolean seller){
 		int h = Integer.valueOf(hour);
 		if(seller)
 			return (h*2)+1;
 		else
 			return (h*2);
 	}
 	
 	private Msg updateMemcache(Boolean flag, String profile, String arrayIndex, Entity e){
 		Entity[] tmpList = null;
 		Entity m = null;
 		int index = Integer.valueOf(arrayIndex);
 	
 		if(!syncCache.contains(profile)){
 			try {
 				Thread.sleep(1000);
 				queryDataStore(profile, "");
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		}
 
 		System.out.println("updateMemCache::1");
 		//check if this is post record deletion
 		if(e==null){
 			//update memcache for that profile
 			queryDataStore(profile, "");
 			//
 			if(!syncCache.contains(profile)){
 				return sendChannelMessage("REFRESH", 
						"refresh", 
 						"0", 
 						"1", 
 						profile,
 						"0");
 			}else{
 				//assume memcache now has the next best bid
 				m = ((Entity[]) syncCache.get(profile))[index];
 				if(m!= null)
 					return sendChannelMessage("UPDATE", 
 							"bidOffer", 
 							String.valueOf((Double) m.getProperty("price")), 
 							"1", 
 							profile,
 							String.valueOf(index));
 				else{
 					//System.out.println("No next best bid/offer");
 					return sendChannelMessage("UPDATE", 
 							"bidOffer", 
 							"", 
 							"1", 
 							profile,
 							String.valueOf(index));
 				}
 			}
 		}
 		
 		//assume memcache is updated by now
 		if(syncCache.contains(profile))
 			tmpList = (Entity[]) syncCache.get(profile);
 		else
 			tmpList = new Entity[48];
 		
 		m = tmpList[index];
 		
 		if(m==null){
 			//Simple insert
 			tmpList[index] = e;
 			syncCache.put(profile, tmpList);
 
 			return sendChannelMessage("UPDATE", 
 					"bidOffer", 
 					String.valueOf((Double) e.getProperty("price")), 
 					"1", 
 					profile,
 					String.valueOf(index));
 		}
 
 		System.out.println("updateMemCache::4");
 		if((Boolean) m.getProperty("seller")){
 			//compare
 			System.out.println("updateMemCache::5");
 			if((Double) m.getProperty("price") <= (Double) e.getProperty("price") && !m.equals(e))
 				return null;
 		}else{
 			System.out.println("updateMemCache::6");
 			if((Double) m.getProperty("price") >= (Double) e.getProperty("price") && !m.equals(e))
 				return null;
 		}
 		
 		System.out.println("updateMemCache::7");
 		tmpList[index] = e;
 		syncCache.put(profile, tmpList);
 		return sendChannelMessage("UPDATE", 
 				"bidOffer", 
 				String.valueOf((Double) e.getProperty("price")), 
 				"1", 
 				(String) e.getProperty("profile"), String.valueOf(index));
 		
 	}
 	
 	
 	
 		
 	public String test(){
 		String s = "\n";
 //		s+= createBidOffer("0000000020110101", 0.3, "batman", "46");
 //		s+= "\n";
 //		s+= createBidOffer("0000000020110101", 0.2, "robin", "47");
 //		s+= "\n";
 //		s+= createBidOffer("0000000020110201", 0.3, "lisa", "26");
 //		s+= "\n";
 //		s+= createBidOffer("0000000020110201", 0.3, "bart", "27");
 //		s+= "\n";
 //		s+= createBidOffer("0000000020110101", 0.3, "batman", "06");
 //		s+= "\n";
 //		s+="Query Data Store:\n";
 //		s+= dumpMemCache();
 //		s+= "\n";
 //
 //		s+= createTransaction("0000000020111216", "01", "joker", "ami", "SG", "KP");
 //		s+= "\n";
 //		s+= "After Buy:\n";
 //		s+= dumpMemCache();
 //		for(int i = 0; i <= 100000; i++){
 //			//do nottin mon
 //		}
 //			
 //		s+="After sell:\n";
 //		s+= dumpMemCache();
 //		s+= indexToHour("46");
 
 		return s;
 		
 	}
 	
 	public String dumpMemCache(){
 		String s = "";
 		queryDataStore();
 		for (String k : getProfiles()){
 			for(Object t : (Entity[]) syncCache.get(k)){
 				if (t!= null)
 					s 	+= "\nProfile: " + ((Entity) t).getProperty("profile").toString()
 						+ " \thour: " + ((Entity) t).getProperty("hour").toString()
 						+ " \towner: " + ((Entity) t).getProperty("user").toString()
 						+ " \tprice: " + ((Entity) t).getProperty("price").toString()
 						+ " \tseller: " + ((Entity) t).getProperty("seller").toString();
 			}
 			s 	+= "\n";
 		}
 		return s;
 	}
 
 	public List<Entity> getMyOrders(String user) {
 		Query qOrder = new Query("Contract");
 		qOrder.addFilter("active", Query.FilterOperator.EQUAL, true);
 		qOrder.addFilter("user", FilterOperator.EQUAL, user);
 		List<Entity> orders = datastore.prepare(qOrder).asList(FetchOptions.Builder.withDefaults());
 		return orders;
 	}
 	
 	public String resolveDefaultAMI(String profile){
 		String[] lookup = reverseLookUpProfile(profile);
 		
 		return "";
 	}
 		
 }
