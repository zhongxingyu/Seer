 package org.uniqush.android;
 
 import org.uniqush.client.Message;
 
 import com.google.android.gcm.GCMRegistrar;
 
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 
 public class MessageCenter {
 
 	private static String TAG = "UniqushMessageCenter";
 
 	/**
 	 * This method has to be called in the very first.
 	 * 
 	 * The user should provide a class name of an implementation of
 	 * UserInfoProvider. The implementation should follows the following
 	 * requirements: 1. It has to implement org.uniqush.android.UserInfoProvider
 	 * 2. It must have a constructor which takes one and only one parameter, a
 	 * Context
 	 * 
 	 * Exceptions may be thrown if the class does not satisfy the requirement
 	 * mentioned above.
 	 * 
 	 * @param context
 	 * @param userInfoProviderClassName
 	 * @throws SecurityException
 	 * @throws ClassNotFoundException
 	 * @throws NoSuchMethodException
 	 */
 	public static void init(Context context, String userInfoProviderClassName)
 			throws SecurityException, ClassNotFoundException,
 			NoSuchMethodException {
 		GCMRegistrar.checkManifest(context);
 		ResourceManager.setUserInfoProvider(context, userInfoProviderClassName);
 	}
 
 	private static void startService(Context context, int cmd, int id) {
 		Intent intent = new Intent(context, MessageCenterService.class);
 		intent.putExtra("c", cmd);
 		intent.putExtra("id", id);
 		context.startService(intent);
 	}
 
 	public static void connect(Context context, int id) {
 		Log.i(TAG, "connect: " + id);
 		startService(context, MessageCenterService.CMD_CONNECT, id);
 	}
 
 	/**
 	 * Asynchronously send the message to server. The result will be reported
 	 * through the handler's onResult() method if id > 0.
 	 * 
 	 * @param context
 	 *            The context
 	 * @param id
 	 *            If id > 0, then any result (error or success) will be reported
 	 *            through the handler's onResult() method. Otherwise (i.e. id <=
 	 *            0), only error will be reported through the handler's
 	 *            onError() method.
 	 * @param msg
 	 */
 	public static void sendMessageToServer(Context context, int id, Message msg) {
 		Log.i(TAG, "send message to server: " + id);
 		Intent intent = new Intent(context, MessageCenterService.class);
 		intent.putExtra("c", MessageCenterService.CMD_SEND_MSG_TO_SERVER);
 		intent.putExtra("id", id);
 		intent.putExtra("msg", msg);
 		context.startService(intent);
 	}
 
 	/**
 	 * Asynchronously send the message to another user. The result will be
 	 * reported through the handler's onResult() method if id > 0.
 	 * 
 	 * The message is actually forwarded by the server. The server may decline
 	 * the forwarding request depending on the implementation of the server.
 	 * 
 	 * @param context
 	 * @param id
 	 *            If id > 0, then any result (error or success) will be reported
 	 *            through the handler's onResult() method. Otherwise (i.e. id <=
 	 *            0), only error will be reported through the handler's
 	 *            onError() method.
 	 * @param service
 	 *            Receiver's service name.
 	 * @param username
 	 *            Receiver's user name.
 	 * @param msg
 	 * @param ttl
 	 *            Time-to-live of the message in number of seconds.
 	 */
 	public static void sendMessageToUser(Context context, int id, String service,
 			String username, Message msg, int ttl) {
 		Intent intent = new Intent(context, MessageCenterService.class);
 		intent.putExtra("c", MessageCenterService.CMD_SEND_MSG_TO_USER);
 		intent.putExtra("id", id);
 		intent.putExtra("service", service);
 		intent.putExtra("username", username);
 		intent.putExtra("ttl", ttl);
 		intent.putExtra("msg", msg);
 		context.startService(intent);
 	}
 
 	public static void stop(Context context, int id) {
 		Log.i(TAG, "stop: " + id);
		Intent intent = new Intent(context, MessageCenterService.class);
		context.stopService(intent);
 	}
 }
