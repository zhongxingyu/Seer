 package edu.stanford.junction.android;
 
 import java.net.URI;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 import edu.stanford.junction.Junction;
 import edu.stanford.junction.JunctionMaker;
 import edu.stanford.junction.SwitchboardConfig;
 import edu.stanford.junction.api.activity.ActivityScript;
 import edu.stanford.junction.api.activity.Cast;
 import edu.stanford.junction.api.activity.JunctionActor;
 
 // TODO:
 // Class hierarchy is badly broken.
 // Split out platform methods and impl methods.
 // For now, extending XMPP.JunctionMaker.
 public class AndroidJunctionMaker extends JunctionMaker {
 	
 	public static final int JUNCTION_VERSION = 1;
 	
 	public static class Intents {
 		public static final String ACTION_JOIN = "junction.intent.action.JOIN";
 		public static final String ACTION_CAST = "junction.intent.action.CAST";
 		
 		public static final String EXTRA_CAST_ROLES = "castingRoles";
 		public static final String EXTRA_CAST_DIRECTORS = "castingDirectors";
 		public static final String EXTRA_CAST_PACKAGE = "joiningPackage";
 		public static final String EXTRA_ACTIVITY_SCRIPT = "activityScript";
 		public static final String EXTRA_JUNCTION_VERSION = "junctionVersion";
 		
 		public static final String PACKAGE_DIRECTOR = "edu.stanford.prpl.junction.applaunch";
 	}
 	
 	private static String JX_LAUNCHER_NAME = "Activity Director";
 	private static String JX_LAUNCHER_URL = "http://prpl.stanford.edu/android/JunctionAppLauncher.apk";
 	private static String JX_LAUNCHER_PACKAGE = Intents.PACKAGE_DIRECTOR;
 	
 	public static final URI CASTING_DIRECTOR;
 	static {
 		URI dumbWayToAssignStaticFinalURI;
 		try {
 			dumbWayToAssignStaticFinalURI = new URI("junction://android-local/cast");
 		} catch (Exception e ) {
 			dumbWayToAssignStaticFinalURI = null;
 		}
 		CASTING_DIRECTOR = dumbWayToAssignStaticFinalURI;
 	}
 		
 	
 	public static AndroidJunctionMaker getInstance(SwitchboardConfig config) {
 		AndroidJunctionMaker maker = new AndroidJunctionMaker();
 		maker.mProvider = maker.getProvider(config);
 		maker.mProvider.setJunctionMaker(maker);
 		return maker;
 	}
 	
 	private AndroidJunctionMaker() {
 		
 	}
 	
 	
 	public URI getInvitationForActivity(Activity activity) {
 		try {
 			return new URI(activity.getIntent().getExtras().getString("invitationURI"));
 		} catch (Exception e) {
 			Log.e("junction","could not get invitation URI",e);
 			return null;
 		}
 	}
 	
 	/**
 	 * Returns an Intent than can be used to join a Junction activity.
 	 * If you want a specific class to handle this intent,
 	 * you can specify the package/class on the returned
 	 * Intent before using it to start an activity.
 	 * 
 	 * @param junctionInvitation
 	 * @return
 	 */
 	public static Intent getIntentForActivityJoin(URI junctionInvitation) {
 		Intent launchIntent = new Intent(Intents.ACTION_JOIN);
 		launchIntent.putExtra("junctionVersion", 1);
 		//launchIntent.putExtra("activityDescriptor", invitation.toString());
		// TODO: keep URI?
		launchIntent.putExtra("invitationURI", junctionInvitation.toString());
 		
 		return launchIntent;
 	}
 	
 
 	/**
 	 * Sends an intent to complete the casting of an activity.
 	 * 
 	 * The result will be an Intent issued, of action ACTION_JOIN.
 	 * The Intent will be populated with casting information, and should
 	 * be handled in your android.app.Activity:
 	 * 
 	 * if AndroidJunctionMaker.isJoinable(this) {
 	 * 	  maker.newJunction(this,mActor);
 	 * }
 	 * 
 	 * @param context
 	 * @param script
 	 * @param support
 	 */
 	public static void castActivity(Context context, ActivityScript script, Cast support) {
 		Intent castingIntent = new Intent(Intents.ACTION_CAST);
 
 		int size=support.size();
 		String[] castingRoles = new String[size];
 		String[] castingDirectors = new String[size];
 		
 		for (int i=0;i<size;i++) {
 			castingRoles[i] = support.getRole(i);
 			castingDirectors[i] = support.getDirector(i).toString();
 		}
 
 		castingIntent.putExtra(Intents.EXTRA_CAST_ROLES, castingRoles);
 		castingIntent.putExtra(Intents.EXTRA_CAST_DIRECTORS, castingDirectors);
 		castingIntent.putExtra(Intents.EXTRA_CAST_PACKAGE, context.getPackageName());
 		castingIntent.putExtra(Intents.EXTRA_ACTIVITY_SCRIPT, script.getJSON().toString());
 		
 		context.startActivity(castingIntent);
 	}
 	
 	/**
 	 * Joins a Junction Activity based on the android.app.Activity's bundle.
 	 * 
 	 * @param bundle
 	 * @param actor
 	 * @return
 	 */
 	public Junction newJunction(Activity activity, JunctionActor actor) {
 		return newJunction(activity.getIntent().getExtras(),actor);
 	}
 	
 	/**
 	 * Junction creator from a bundle passed from
 	 * a Junction Activity Director.
 	 * 
 	 * The bundle may contain the URI of an existing activity session
 	 * or an activity script for creating a new session. It may also
 	 * contain casting information.
 	 * 
 	 * @param bundle
 	 * @param actor
 	 * @return
 	 */
 	public Junction newJunction(Bundle bundle, JunctionActor actor) {
 		if (bundle == null || !bundle.containsKey(Intents.EXTRA_JUNCTION_VERSION)) {
 			Log.d("junction","Could not launch from bundle (" + bundle + ")");
 			return null;
 		}
 		
 		
 		try {
 			if (bundle.containsKey("invitationURI")) {
 				// TODO: pass both activity script and uri if available?
 				URI uri = new URI(bundle.getString("invitationURI"));
 				Junction jx = newJunction(uri,actor);
 				return jx;
 			} else {
 				JSONObject desc = new JSONObject(bundle.getString(Intents.EXTRA_ACTIVITY_SCRIPT));
 				ActivityScript activityDesc = new ActivityScript(desc);
 				Junction jx;
 				if (bundle.containsKey(Intents.EXTRA_CAST_ROLES)) {
 					Log.d("junction","casting roles");
 					String[] aroles = bundle.getStringArray(AndroidJunctionMaker.Intents.EXTRA_CAST_ROLES);
 					String[] adirectors = bundle.getStringArray(AndroidJunctionMaker.Intents.EXTRA_CAST_DIRECTORS);
 					
 					List<String>roles = Arrays.asList(aroles);
 					List<URI>directors = new LinkedList<URI>();
 					for (int i=0;i<adirectors.length;i++) {
 						directors.add(new URI(adirectors[i]));
 					}
 					Log.d("junction","going to request casting for " + directors.size() + " roles");
 					Cast support = new Cast(roles,directors);
 					jx = newJunction(activityDesc,actor,support);
 				} else {
 					jx = newJunction(activityDesc,actor);
 				}
 				return jx;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public static boolean isJoinable(Activity a) {
 		if (a.getIntent() == null || a.getIntent().getExtras() == null) return false;
 		if (a.getIntent().getExtras().containsKey("junctionVersion")) return true;
 		return false;
 	}
 	
 	/**
 	 * Finds a pre-existing Junction activity by scanning for a QR code.
 	 * @param context
 	 */
 	public static void findActivityByScan(final Activity activity) {
 		WaitForInternetCallback callback =
 			new WaitForInternetCallback(activity) {
 			@Override
 			public void onConnectionFailure() {
 				activity.finish();
 			}
 			
 			@Override
 			public void onConnectionSuccess() {
 				Intent intent = new Intent("junction.intent.action.join.SCAN");
 				intent.putExtra("package", activity.getPackageName());
 				IntentLauncher.launch(activity, 
 									intent,
 									"edu.stanford.prpl.junction.applaunch",
 									"http://prpl.stanford.edu/android/JunctionAppLauncher.apk",
 									"Activity Director");
 			}
 		};
 		
 		try {
 			WaitForInternet.setCallback(callback);
 		} catch (SecurityException e) {
 			Log.w("junction","Could not check network state. If you'd like this functionality, please add the permission: android.permission.ACCESS_NETWORK_STATE", e);
 			callback.onConnectionSuccess();
 		}
 	}
 	
 	/**
 	 * Launch the Activity Director to join an existing
 	 * Junction activity by some user-specified method.
 	 * 
 	 * @param context
 	 */
 	public static void joinActivity(Context context) {
 		Intent intent = new Intent("junction.intent.action.join.ANY");
 		intent.putExtra("package", context.getPackageName());
 		IntentLauncher.launch(context, 
 							intent,
 							"edu.stanford.prpl.junction.applaunch",
 							"http://prpl.stanford.edu/android/JunctionAppLauncher.apk",
 							"Activity Director");
 	}
 	
 	/**
 	 * Invites another actor by some unspecified means.
 	 * @param context
 	 * @param Junction
 	 * @param role
 	 */
 	public void inviteActor(Context context, Junction junction, String role) {
 		Intent intent = new Intent("junction.intent.action.invite.ANY");
 		intent.putExtra("package", context.getPackageName());
 		intent.putExtra("uri", junction.getInvitationURI(role).toString());
 		//intent.putExtra("activityDescriptor", junction.getActivityDescription().getJSON());
 		
 		IntentLauncher.launch(context, 
 							intent,
 							JX_LAUNCHER_PACKAGE,
 							JX_LAUNCHER_URL,
 							JX_LAUNCHER_NAME);
 	}
 	
 	
 	/**
 	 * Invites another actor by some unspecified means.
 	 * @param context
 	 * @param Junction
 	 * @param role
 	 */
 	public void inviteActor(Context context, URI invitation) {
 		Intent intent = new Intent("junction.intent.action.invite.ANY");
 		intent.putExtra("package", context.getPackageName());
 		intent.putExtra("uri", invitation.toString());
 		//intent.putExtra("activityDescriptor", junction.getActivityDescription().getJSON());
 		
 		IntentLauncher.launch(context, 
 							intent,
 							JX_LAUNCHER_PACKAGE,
 							JX_LAUNCHER_URL,
 							JX_LAUNCHER_NAME);
 	}
 	
 	/**
 	 * Invites an actor to an activity by presenting a QR code on screen. 
 	 * @param context
 	 * @param junction
 	 * @param role
 	 */
 	public void inviteActorByQR(Context context, Junction junction, String role) {
 		Intent intent = new Intent("junction.intent.action.invite.QR");
 		intent.putExtra("package", context.getPackageName());
 		intent.putExtra("uri", junction.getInvitationURI(role).toString());
 		//intent.putExtra("activityDescriptor", junction.getActivityDescription().getJSON());
 		
 		IntentLauncher.launch(context, 
 							intent,
 							JX_LAUNCHER_PACKAGE,
 							JX_LAUNCHER_URL,
 							JX_LAUNCHER_NAME);
 	}
 	
 	/**
 	 * Scan for a Listening service and send it a 'join activity' request.
 	 * @param context
 	 * @param junction
 	 * @param role
 	 */
 	public void inviteActorByScan(Context context, Junction junction, String role) {
 		Intent intent = new Intent("junction.intent.action.invite.SCAN");
 		intent.putExtra("package", context.getPackageName());
 		intent.putExtra("uri",junction.getInvitationURI(role).toString());
 		//intent.putExtra("activityDescription", junction.getActivityDescription().getJSON().toString());
 		//intent.putExtra("role",role);
 		
 		IntentLauncher.launch(context, 
 							intent,
 							JX_LAUNCHER_PACKAGE,
 							JX_LAUNCHER_URL,
 							JX_LAUNCHER_NAME);
 	}
 	
 	/**
 	 * Send an invitation to join an activity by text message.
 	 * @param context
 	 * @param junction
 	 * @param role
 	 */
 	public void inviteActorBySMS(Context context, Junction junction, String role) {
 		Intent intent = new Intent("junction.intent.action.invite.TEXT");
         String uri = junction.getInvitationURI(role).toString();
         intent.putExtra("invitation", uri);
         
         IntentLauncher.launch(context, 
 				intent,
 				JX_LAUNCHER_PACKAGE,
 				JX_LAUNCHER_URL,
 				JX_LAUNCHER_NAME);
 	}
 	
 	/**
 	 * Send an invitation to join an activity by text message.
 	 * @param context
 	 * @param junction
 	 * @param role
 	 * @param phoneNumber
 	 */
 	public void inviteActorBySMS(Context context, Junction junction, String role, String phoneNumber) {
 		Intent intent = new Intent("junction.intent.action.invite.TEXT");
         String uri = junction.getInvitationURI(role).toString();
         intent.putExtra("invitation", uri);
         intent.putExtra("phoneNumber",phoneNumber);
         
         IntentLauncher.launch(context, 
 				intent,
 				JX_LAUNCHER_PACKAGE,
 				JX_LAUNCHER_URL,
 				JX_LAUNCHER_NAME);
 	}
 }
