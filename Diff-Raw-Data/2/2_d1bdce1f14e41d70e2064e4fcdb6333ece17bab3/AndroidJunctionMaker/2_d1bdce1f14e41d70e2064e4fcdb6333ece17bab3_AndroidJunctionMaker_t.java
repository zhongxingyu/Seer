 package edu.stanford.junction.android;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.util.Log;
 
 import edu.stanford.junction.Junction;
 import edu.stanford.junction.JunctionMaker;
 import edu.stanford.junction.SwitchboardConfig;
 import edu.stanford.junction.api.activity.ActivityScript;
 import edu.stanford.junction.api.activity.JunctionActor;
 import edu.stanford.junction.provider.xmpp.JunctionProvider;
 import edu.stanford.junction.provider.xmpp.XMPPSwitchboardConfig;
 
 // TODO:
 // Class hierarchy is badly broken.
 // Split out platform methods and impl methods.
 // For now, extending XMPP.JunctionMaker.
 public class AndroidJunctionMaker extends JunctionMaker {
 	
 	private static String JX_LAUNCHER_NAME = "Activity Director";
 	private static String JX_LAUNCHER_URL = "http://prpl.stanford.edu/android/JunctionAppLauncher.apk";
 	private static String JX_LAUNCHER_PACKAGE = "edu.stanford.prpl.junction.applaunch";
 	
 	
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
 	 * a Junction Activity Launcher
 	 * 
 	 * @param bundle
 	 * @param actor
 	 * @return
 	 */
 	public Junction newJunction(Bundle bundle, JunctionActor actor) {
 		if (bundle == null || !bundle.containsKey("junctionVersion")) {
 			Log.d("junction","Could not launch from bundle (" + bundle + ")");
 			return null;
 		}
 		
 		try {
 			JSONObject desc = new JSONObject(bundle.getString("activityDescriptor"));
 			ActivityScript activityDesc = new ActivityScript(desc);
 			Junction jx = newJunction(activityDesc,actor);
 			
 			return jx;
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
