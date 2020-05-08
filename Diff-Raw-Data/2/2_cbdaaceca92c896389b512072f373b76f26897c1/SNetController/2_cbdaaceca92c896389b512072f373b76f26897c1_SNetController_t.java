 package edu.uw.cs.cse461.SNet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.DB461.DB461.DB461Exception;
 import edu.uw.cs.cse461.DB461.DB461.RecordSet;
 import edu.uw.cs.cse461.HTTP.HTTPProviderInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException;
 import edu.uw.cs.cse461.Net.DDNS.DDNSFullName;
 import edu.uw.cs.cse461.Net.DDNS.DDNSFullNameInterface;
 import edu.uw.cs.cse461.Net.DDNS.DDNSRRecord;
 import edu.uw.cs.cse461.Net.DDNS.DDNSResolverService;
 import edu.uw.cs.cse461.Net.DDNS.DDNSException.DDNSRuntimeException;
 import edu.uw.cs.cse461.Net.RPC.RPCCall;
 import edu.uw.cs.cse461.Net.RPC.RPCCallableMethod;
 import edu.uw.cs.cse461.Net.RPC.RPCService;
 import edu.uw.cs.cse461.SNet.SNetDB461.CommunityRecord;
 import edu.uw.cs.cse461.util.Log;
 
 
 /**
  * Handles UI-independent operations on SNetDB
  * 
  * @author zahorjan
  *
  */
 public class SNetController extends NetLoadableService implements HTTPProviderInterface {
 	private static final String TAG="SNetController";
 	
 	private RPCCallableMethod fetchUpdates;
 	private RPCCallableMethod fetchPhoto;
 	
 	/**
 	 * A full path name to the sqlite database.
 	 */
 	private String mDBName;
 	
 	private DDNSResolverService ddnsResolverService;
 	
 	/**
 	 * IMPLEMENTED: Returns the full path name of the DB file.
 	 */
 	public String DBName() {
 		return mDBName;
 	}
 	
 	/**
 	 * IMPLEMENTED: Ensures that the root member and the member whose name is the argument
 	 * (usually the host name of this node) both have entries in the community table.
 	 * @param user A user name
 	 * @throws DB461Exception Some unanticipated exception occurred.
 	 */
 	public void registerBaseUsers(DDNSFullName user) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(mDBName);
 			db.registerMember( user );
 			db.registerMember( new DDNSFullName("") );  // and the root
 		} catch (Exception e) {
 			Log.e(TAG, "registerBaseUsers caught exception: " + e.getMessage());
 			throw new DB461Exception("registerUser caught exception: " + e.getMessage());
 		}
 		finally {
 			if ( db != null ) db.discard();
 		}
 	}
 
 	/**
 	 * IMPLEMENTED: Helper function that returns a string representing the community table record in the database
 	 * for the user whose name is the node's hostname.
 	 * @return The community table row, as a String.
 	 */
 	synchronized public String getStatusMsg() {
 		SNetDB461 db = null;
 		StringBuilder sb = new StringBuilder();
 		try {
 			sb.append("Host: ").append(NetBase.theNetBase().hostname()).append("\n");
 			RPCService rpcService = (RPCService)NetBase.theNetBase().getService("rpc");
 			sb.append("Location: ").append(rpcService.localIP()).append(":").append(rpcService.localPort()).append("\n");
 			db = new SNetDB461(mDBName);
 			String memberName = new DDNSFullName(NetBase.theNetBase().hostname()).toString();
 			CommunityRecord member = db.COMMUNITYTABLE.readOne(memberName);
 			if ( member != null ) sb.append(member.toString());
 			else sb.append("No data for member '" + memberName + "'");
 		} catch (Exception e) {
 			Log.e(TAG, "getStatusMsg: caught exception: " + e.getMessage());
 		}
 		finally {
 			if ( db != null ) db.discard();
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Implemented: Checks db for consistency violations and tries to fix  them.
 	 * Consistency requirements:
 	 * <ul>
 	 * <li> Every photo hash in community table should have a photo table entry
 	 * <li> Ref counts should be correct
 	 * <li> If a photo table entry has a file name, the file should exist, and the file's hash should correspond to the photo record key
 	 * </ul>
 	 * @param galleryDir A File object representing the gallery directory
 	 */
 	synchronized public void fixDB(File galleryDir) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(mDBName);
 			db.checkAndFixDB(galleryDir);
 		} catch (Exception e) {
 			throw new DB461Exception("fixDB caught exception: "+ e.getMessage());
 		}
 		finally {
 			if ( db != null ) db.discard();
 		}
 	}
 	
 	
 	//////////////////////////////////////////////////////////////////////////////////////
 	// Below here the methods may need implementation, completion, or customization.
 	//////////////////////////////////////////////////////////////////////////////////////
 	
 	public SNetController(String dbDirName) {
 		super("snet", true);
 
 		mDBName = dbDirName + "/" + new DDNSFullName(NetBase.theNetBase().hostname()) + "snet.db";
 
 		 try {
 			ddnsResolverService = new DDNSResolverService();
 
 			fetchUpdates = new RPCCallableMethod(this, "fetchUpdatesCallee");
 			fetchPhoto = new RPCCallableMethod(this, "fetchPhotoCallee");
 
 			RPCService rpcService = (RPCService)NetBase.theNetBase().getService("rpc");
 			rpcService.registerHandler(loadablename(), "fetchupdates", fetchUpdates );
 			rpcService.registerHandler(loadablename(), "fetchphoto", fetchPhoto );
 			
 		} catch (Exception e) {
 			String msg = "SNet constructor caught exception: " + e.getMessage();
 			Log.e(TAG, msg);
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * OPTIONAL: Provides a web interface displaying status of SNet database.
 	 * @param uriVec The parsed URI argument.  (See HTTPProviderInterface.java.)
 	 * @return Text to return to the browser.
 	 */
 	public String httpServe(String[] uriVec) throws DB461Exception {
 		return "Not implemented";
 	}
 	
 	/**
 	 * Assign a photo that exists as a local file as a member's chosen photo.
 	 * Decrement reference count on any existing chosen photo and delete underyling file if appropriate.
 	 * @param memberName Name of member
 	 * @param filename Full path name to new chosen photo file
 	 * @param galleryDir File object representing the gallery directory
 	 * @throws DB461Exception
 	 */
 	synchronized public void setChosenPhoto(DDNSFullNameInterface memberName, String filename, File galleryDir) throws DB461Exception {
 	}
 
 	/**
 	 * This method supports the manual setting of a user's generation number.  It is probably not useful
 	 * in building the SNet application per se, but can be useful in debugging tools you might write.
 	 * @param memberName Name of the member whose generation number you want to set
 	 * @param gen The value the generation number should be set to.
 	 * @return The old value of the member's generation number
 	 * @throws DB461Exception  Member doesn't exist in community db, or some unanticipated exception occurred.
 	 */
 	synchronized public int setGenerationNumber(DDNSFullNameInterface memberName, int gen) throws DB461Exception {
 		return -1;
 	}
 
 	/**
 	 * Sets friend status of a member.  The member must already be in the DB.
 	 * @param friend  Name of member whose friend status should be set.
 	 * @param isfriend true to make a friend; false to unfriend.
 	 * @throws DB461Exception The member is not in the community table in the DB, or some unanticipated exception occurs.
 	 */
 	synchronized public void setFriend(DDNSFullNameInterface friend, boolean isfriend) throws DB461Exception {
 	}
 	
 	/**
 	 * Registers a photo as the "my" photo for a given member.
 	 * Decrements the reference count of any existing my photo for that member, and deletes the underyling file for it
 	 * as appropriate.
 	 * @param memberName Member name
 	 * @param filename  Full path name to new my photo file
 	 * @param galleryDir File object describing directory in which gallery photos live
 	 * @throws DB461Exception  Can't find member in community table, or some unanticipated exception occurs.
 	 */
 	synchronized public void newMyPhoto(DDNSFullNameInterface memberName, String filename, File galleryDir) throws DB461Exception {
 	}
 	
 	/**
 	 * The caller side of fetchUpdates.
 	 * @param friend The friend to be contacted
 	 * @param galleryDir The path name to the gallery directory
 	 * @throws DB461Exception Something went wrong...
 	 */
 	synchronized public void fetchUpdatesCaller( String friend, File galleryDir) throws DB461Exception {
 		SNetDB461 db = null;
 		try {
 			db = new SNetDB461(mDBName);
 			DDNSRRecord.ARecord ddnsResult;
 			JSONObject args;
 
 			// Resolve the friend to a ddns record
 			ddnsResult = ddnsResolverService.resolve(friend);
 
 			Log.d(TAG, "Putting together JSON glob");
 			// Community info glob
 			JSONObject community = new JSONObject();
 			RecordSet<CommunityRecord> communityRecords = db.COMMUNITYTABLE.readAll();
 			for (CommunityRecord cRecord : communityRecords) {
 				JSONObject value = new JSONObject()
 						.put("generation", cRecord.generation)
 						.put("myphotohash", cRecord.myPhotoHash)
 						.put("chosenphotohash", cRecord.chosenPhotoHash);
 
 				community.put(cRecord.name.toString(), value);
 			}
 
 			// Needphotos info glob
 			JSONArray needphotos = new JSONArray();
 
 
 			// fetch the updates
 			args = new JSONObject()
 					.put("community", community)
 					.put("needphotos", needphotos);
 
 			Log.d(TAG, "sending fetchUpdates RPC call with args: "+args);
			JSONObject response = RPCCall.invoke(ddnsResult.ip(), ddnsResult.port(), "snet", "fetchupdates", args);
 
 			Log.d(TAG, "response JSON of "+response);
 
 			Iterator<String> keysIter;
 
 			// iterate over response
 			JSONObject communityUpdates = response.getJSONObject("communityupdates");
 			keysIter = communityUpdates.keys();
 			while (keysIter.hasNext()) {
 				String name = keysIter.next();
 				Log.d(TAG, "looking at: " + name);
 				CommunityRecord record = db.COMMUNITYTABLE.readOne(name);
 				if (record == null) {
 					record = db.COMMUNITYTABLE.createRecord();
 					record.name = new DDNSFullName(name);
 					record.isFriend = false;
 				}
 				JSONObject recordUpdate = communityUpdates.getJSONObject(name);		
 				record.generation = recordUpdate.getInt("generation");
 				record.myPhotoHash = recordUpdate.getInt("myphotohash");
 				record.chosenPhotoHash = recordUpdate.getInt("chosenphotohash");
 				db.COMMUNITYTABLE.write(record);
 			}
 
 			// iterate over photos and retieve data for each
 			JSONArray photoHashes = response.getJSONArray("photoupdates");
 	
 			response = RPCCall.invoke(ddnsResult.ip(), ddnsResult.port(), "snet", "fetchPhoto", args);
 		
 
 		} catch(DDNSException e) {
 			e.printStackTrace();
 		} catch(JSONException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if ( db != null ) db.discard();
 		}
 	}
 	
 	// Traverses the array provided to see if the provided value is present.  Must be linear search because the
 	// values are not ordered
 	private boolean contains(JSONArray array, int value) throws JSONException {
 		for (int i = 0; i < array.length(); i++) {
 			if (array.getInt(i) == value) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * The callee side of fetchUpdates - invoked via RPC.
 	 * @param args JSONObject containing commmunity JSONObject and needphotos JSONArray (described in assignment)
 	 * @return JSONObject containing communityupdates JSONObject and photoupdates JSONArray (described in assignment)
 	 */
 	synchronized public JSONObject fetchUpdatesCallee(JSONObject args) throws Exception {
 		JSONObject community = args.getJSONObject("community"); // Community they know about
 		JSONArray needphotos = args.getJSONArray("needphotos"); // Photos they want
 		
 		JSONArray photoupdates = new JSONArray(); // Photos that I store that they either wanted or may want because the community has changed
 		JSONObject communityupdates = new JSONObject(); // Community members for which I have a better generation number.
 		
 		SNetDB461 database = new SNetDB461(mDBName); // The database of info I have
 		Iterator<String> keys = community.keys();
 		Log.v(TAG, "assumed that the iterator contains strings");
 		while (keys.hasNext()) {
 			// Traverses their community values to see where it differs from mine.  Stores all the information of mine that is more up to date than
 			// theirs.
 			String name = keys.next();
 			CommunityRecord myVals = database.COMMUNITYTABLE.readOne(name);
 			JSONObject theirVals = community.getJSONObject(name);		
 			int genNum = theirVals.getInt("generation");
 			if (myVals == null) {
 				Log.d(TAG, "We don't currently have a record for: " + name);
 				// I don't have this member of the community, so I should store the data for later
 				myVals = database.COMMUNITYTABLE.createRecord();
 				myVals.name = new DDNSFullName(name);
 				myVals.generation = genNum;
 				myVals.isFriend = false;
 				myVals.myPhotoHash = theirVals.getInt("myphotohash");
 				myVals.chosenPhotoHash = theirVals.getInt("chosenphotohash");
 				database.COMMUNITYTABLE.write(myVals);
 			} else if (genNum < myVals.generation) {
 				Log.d(TAG, "Our record is more up to date than theirs for: " + name);
 				// Their data is not up to date, so we need to put our information into the updates to send
 				JSONObject update = new JSONObject();
 				update.put("generation", myVals.generation);
 				update.put("myphotohash", myVals.myPhotoHash);
 				if (database.PHOTOTABLE.readOne(myVals.myPhotoHash) != null) {
 					if (!contains(photoupdates, myVals.myPhotoHash)) { 
 						// check if we need to add the hash to the list of photohashes.  
 						// This will only happen if it is not already present and we actually store the data we have told them about.
 						photoupdates.put(myVals.myPhotoHash);
 					}
 				}
 				update.put("chosenphotohash", myVals.chosenPhotoHash);
 				if (database.PHOTOTABLE.readOne(myVals.chosenPhotoHash) != null) {
 					if (!contains(photoupdates, myVals.chosenPhotoHash)) {
 						// check if we need to add the hash to the list of photohashes
 						// This will only happen if it is not already present and we actually store the data we have told them about.
 						photoupdates.put(myVals.chosenPhotoHash);
 					}
 				}
 				communityupdates.put(name, update);
 			} else if (genNum > myVals.generation) {
 				// My data is not up to date, so I should update it.
 				Log.d(TAG, "Their record is more up to date than ours for: " + name);
 				myVals.generation = genNum;
 				myVals.myPhotoHash = theirVals.getInt("myphotohash");
 				myVals.chosenPhotoHash = theirVals.getInt("chosenphotohash");
 				database.COMMUNITYTABLE.write(myVals);
 			} // If our generation numbers are the same, do nothing
 		}
 		// Traverses all of our community records to see if we know about any member they do not.
 		RecordSet<CommunityRecord> myCom = database.COMMUNITYTABLE.readAll();
 		for (CommunityRecord rec : myCom) {
 			String name = rec.name.toString();
 			if (!community.has(name)) {
 				Log.d(TAG, "We have a record they do not for: " + name);
 				// The name is not in their list of community members, so we must add it to the updates we will send back
 				JSONObject update = new JSONObject();
 				update.put("generation", rec.generation);
 				update.put("myphotohash", rec.myPhotoHash);
 				update.put("chosenphotohash", rec.chosenPhotoHash);
 				if (database.PHOTOTABLE.readOne(rec.myPhotoHash) != null) {
 					if (!contains(photoupdates, rec.myPhotoHash)) { 
 						// check if we need to add the hash to the list of photohashes.  
 						// This will only happen if it is not already present and we actually store the data we have told them about.
 						photoupdates.put(rec.myPhotoHash);
 					}
 				}
 				if (database.PHOTOTABLE.readOne(rec.chosenPhotoHash) != null) {
 					if (!contains(photoupdates, rec.chosenPhotoHash)) {
 						// check if we need to add the hash to the list of photohashes
 						// This will only happen if it is not already present and we actually store the data we have told them about.
 						photoupdates.put(rec.chosenPhotoHash);
 					}
 				}
 				communityupdates.put(name, update);
 			}
 		}
 		
 		for (int i = 0; i < needphotos.length(); i++) {
 			// Traverse the photos they say they need
 			int currenthash = needphotos.getInt(i);
 			if (!contains(photoupdates, currenthash)) {
 				// If we haven't already put that into our updates, check if we have it.
 				if (database.PHOTOTABLE.readOne(currenthash) != null) {
 					// If we have it, tell them we do.
 					Log.d(TAG, "We have a photo they requested of hash: " + currenthash);
 					photoupdates.put(currenthash);
 				}
 			}
 		}
 		Log.d(TAG, "We are returning our results");
 		JSONObject result = new JSONObject();
 		result.put("photoupdates", photoupdates);
 		result.put("communityupdates", communityupdates);
 		return result;
 	}	
 	
 	/**
 	 * Callee side of fetchPhoto (fetch one photo).  To fetch an image file, call this
 	 * method repeatedly, starting at offset 0 and incrementing by the returned length each
 	 * subsequent call.  Repeat until a length of 0 comes back.
 	 * @param args {photoHash: int, maxlength: int, offset: int}
 	 * @return {photoHash: int, photoData: String (base64 encoded byte[]), length: int, offset: int}.  The photoData field may
 	 *     not exist if the length is 0.
 	 * @throws Exception
 	 */
 	synchronized public JSONObject fetchPhotoCallee(JSONObject args) throws Exception {
 		return null;
 	}
 
 	@Override
 	public String dumpState() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 }
 	
 
