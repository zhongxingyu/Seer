 package edu.uw.cs.cse461.ConsoleApps;
 
 import java.io.File;
 import java.util.Scanner;
 
 import org.json.JSONObject;
 
 import edu.uw.cs.cse461.DB461.DB461.DB461Exception;
 import edu.uw.cs.cse461.HTTP.HTTPProviderInterface;
 import edu.uw.cs.cse461.Net.Base.NetBase;
 import edu.uw.cs.cse461.Net.Base.NetLoadable.NetLoadableConsoleApp;
 import edu.uw.cs.cse461.Net.DDNS.DDNSFullName;
 import edu.uw.cs.cse461.Net.RPC.RPCCallableMethod;
 import edu.uw.cs.cse461.Net.RPC.RPCService;
 import edu.uw.cs.cse461.SNet.SNetController;
 import edu.uw.cs.cse461.SNet.SNetDB461;
 import edu.uw.cs.cse461.util.Log;
 
 /**
  * This is basically a driver to exercise your SNetController code.
  * It supports exchanging photos with otherinstances, but provides no way
  * to create photos.  Instead, photos are simply entered into the gallery
  * by some external means, and then made the user's my or chosen photo
  * by issuing the appropriate command.
  * 
  * @author zahorjan
  *
  */
 public class SNet extends NetLoadableConsoleApp implements HTTPProviderInterface {
 	private static final String TAG="SNet";
 	private static final String GALLERYDIR = "../ConsoleRun/SNetGallery-";
 	private static final String SNETDBDIR = "../ConsoleRun";
 	
 	/*private RPCCallableMethod fetchupdates;
 	private RPCCallableMethod fetchphoto;*/
 	
 	private File mGalleryDir;
 	private DDNSFullName mMyName;
 	
 	private SNetController mSNetController;
 	
 	public String httpServe(String[] uriVec) throws DB461Exception {
 		return mSNetController.httpServe(uriVec);
 	}
 	
 	/**
 	 * The constructor registers the RPC-callable methods of the service portion of SNET.
 	 */
 	public SNet() throws Exception {
		super("snet", true);
 		
 		// check for gallery
 		mGalleryDir = new File(GALLERYDIR + NetBase.theNetBase().hostname());
 		if ( !mGalleryDir.exists() ) {
 			String msg = "Gallery directory " + mGalleryDir.getCanonicalPath() + " doesn't seem to exist.  Creating it."; 
 			Log.w(TAG, msg);
 			mGalleryDir.mkdirs();
 		}
 		Log.w(TAG, "Gallery directory is " + mGalleryDir.getCanonicalPath());
 		
 		// set up SNetController -- tell it where the db is
 
 		mSNetController = new SNetController(SNETDBDIR);
 		/*
 		// register rpc interface
 		fetchupdates = new RPCCallableMethod(this, "_rpcFetchUpdates");
 		fetchphoto = new RPCCallableMethod(this, "_rpcFetchPhoto");
 
 		RPCService rpcService = (RPCService)NetBase.theNetBase().getService("rpc");
 		if ( rpcService == null) throw new Exception("The SNet app requires that the RPC resolver service be loaded");
 		rpcService.registerHandler(loadablename(), "fetchUpdates", fetchupdates );
 		rpcService.registerHandler(loadablename(), "fetchPhoto", fetchphoto );
 		*/
 		// make sure user is in db
 		mMyName = new DDNSFullName(NetBase.theNetBase().hostname());
 		mSNetController.registerBaseUsers(mMyName);
 	}
 	
 	@Override
 	public void run() {
 		System.out.println(mSNetController.getStatusMsg());
 		Scanner scanner = new Scanner(System.in);
 		do {
 			try {
 				System.out.println("Commands:");
 				System.out.println("c filename - make filename your chosen photo");
 				System.out.println("d - dump database");
 				System.out.println("g n - set my generation number to n");
 				System.out.println("i - check and correct database integrity");
 				System.out.println("l - list gallery directory");
 				System.out.println("m ddnsname - make ddnsname a friend");
 				System.out.println("n filename - make filename your new photo");
 				System.out.println("x ddnsname - attempt update protocol with ddnsname");
 				System.out.println("exit - exit");
 				String cmd = scanner.next();
 
 				if ( cmd.equals("exit")) break;
 
 				else if ( cmd.equals("c") ) {
 					String filename = scanner.next();
 					mSNetController.setChosenPhoto(mMyName, mGalleryDir.getAbsolutePath() + "/" + filename, mGalleryDir);
 				}
 				
 				else if ( cmd.equals("d")) {
 					SNetDB461 db = null;
 					try {
 						db = new SNetDB461(mSNetController.DBName());
 						System.out.println(db.toString());
 					} finally {
 						if ( db != null ) db.close();
 					}
 				}
 				
 				else if ( cmd.equals("g")) {
 					int gen = scanner.nextInt();
 					mSNetController.setGenerationNumber(mMyName, gen);
 				}
 				
 				else if ( cmd.equals("i")) {
 					mSNetController.fixDB(mGalleryDir);
 				}
 				
 				else if ( cmd.equals("l") ) {
 					System.out.println("Gallery = " + GALLERYDIR + "[" + mGalleryDir.getAbsolutePath() + "]");
 				}
 
 				else if ( cmd.equals("m")) {
 					String ddnsname = scanner.next();
 					if ( ddnsname.equals("root")) ddnsname = "";
 					mSNetController.setFriend(new DDNSFullName(ddnsname), true);
 				}
 
 				else if ( cmd.equals("n") ) {
 					String filename = scanner.next();
 					mSNetController.newMyPhoto(mMyName, mGalleryDir.getAbsolutePath() + "/" + filename, mGalleryDir);
 				}
 
 				else if ( cmd.equals("x")) {
 					String ddnsname = scanner.next();
 					if ( ddnsname.equals("root")) ddnsname = "";
 					mSNetController.fetchUpdatesCaller(ddnsname, mGalleryDir);
 				}
 				
 				else {
 					Log.e(TAG, "Unrecognized command: " + cmd);
 				}
 				
 			} catch (Exception e) {
 				Log.e(TAG, "Caught exception: " + e.getMessage());
 				scanner.skip(".*\n");  // try to flush rest of line
 			}
 
 		} while ( true );
 	}
 	
 	public JSONObject _rpcFetchUpdates(JSONObject args) throws Exception {
 		JSONObject resultObj = mSNetController.fetchUpdatesCallee(args);
 		return resultObj;
 	}
 
 	public JSONObject _rpcFetchPhoto(JSONObject args) throws Exception {
 		JSONObject resultObj = mSNetController.fetchPhotoCallee(args);
 		return resultObj;
 	}
 }
