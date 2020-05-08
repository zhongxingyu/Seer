 package com.ecommuters;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInput;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.nio.channels.FileChannel;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.net.ConnectivityManager;
 import android.text.Html;
 import android.text.Spanned;
 import android.text.method.LinkMovementMethod;
 import android.util.Log;
 import android.widget.TextView;
 
 public class Helper {
 
 	private static final String regExp = "[^a-zA-Z0-9.-]";
 	static boolean isOnline(Context context) {
 		try {
 			ConnectivityManager cm = (ConnectivityManager) context
 					.getSystemService(Context.CONNECTIVITY_SERVICE);
 			return cm.getActiveNetworkInfo().isConnected();
 		} catch (Exception e) {
 			return false;
 		}
 
 	}
 
 	static boolean matchProtocolVersion(Context context) {
 		try {
 			if (!isOnline(context))
 				return true;
 			return Const.PROTOCOL_VERSION.equals(HttpManager
 					.getProtocolVersion());
 		} catch (Exception e) {
 			return true;//se ho problemi di comunicazione, nel dubbio considero buona la versione
 		}
 	}
 
 	public static boolean isNullOrEmpty(String s) {
 		return s == null || s.length() == 0;
 	}
 
 	public static void dialogMessage(final Context context, String message,
 			String title, DialogInterface.OnClickListener okListener,
 			DialogInterface.OnClickListener cancelListener) {
 		new AlertDialog.Builder(context)
 				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(title)
 				.setMessage(message)
 				.setPositiveButton(R.string.yes, okListener)
 				.setNegativeButton(R.string.no, cancelListener).show();
 
 	}
 	
 	
 	public static void dialogMessage(final Context context, String message,
 			DialogInterface.OnClickListener okListener,
 			DialogInterface.OnClickListener cancelListener) {
 		dialogMessage(context, message, context.getString(R.string.app_name), okListener, cancelListener);
 
 	}
 	public static void dialogMessage(final Context context, int message,
 			DialogInterface.OnClickListener okListener,
 			DialogInterface.OnClickListener cancelListener) {
 		dialogMessage(context, context.getString(message), okListener, cancelListener);
 
 	}
 	public static void dialogMessage(final Context context, int message,
 			int title, DialogInterface.OnClickListener okListener,
 			DialogInterface.OnClickListener cancelListener) {
 		dialogMessage(context, context.getString(message),
 				context.getString(title), okListener, cancelListener);
 
 	}
 
 	public static void hideableMessage(Context context, final int messageId) {
		//if (MySettings.isHiddenMessage(messageId))
			//return;
 		Spanned msg = Html.fromHtml(context.getString(messageId));
 		AlertDialog dialog = new AlertDialog.Builder(context)
 				.setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.app_name)
 				.setMessage(msg)
 				.setPositiveButton(android.R.string.ok, null)
 				.setNegativeButton(R.string.no_show_again, new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 						MySettings.setHiddenMessage(messageId, true);
 						dialog.dismiss();
 					}
 				}).show();
 		TextView messageView = (TextView) dialog
 				.findViewById(android.R.id.message);
 		messageView.setLinksClickable(true);
 		messageView.setMovementMethod(LinkMovementMethod.getInstance());
 	}
 	public static List<File> getFiles(Context context, String ext) {
 		File dir = context.getFilesDir();
 		final List<File> files = new ArrayList<File>();
 		File[] subFiles = dir.listFiles();
 		if (subFiles != null) {
 			for (File file : subFiles) {
 				if (file.isFile() && file.getName().endsWith(ext)) {
 					files.add(file);
 				}
 			}
 		}
 		return files;
 	}
 
 	public static boolean isValidRouteName(String routeName) {
 		return !isNullOrEmpty(routeName)
 				&& routeName.replaceAll(regExp, "_").equals(routeName);
 	}
 	public static String getRouteFile(String routeName) {
 		return routeName + Const.ROUTEEXT;
 	}
 
 	
 	public static void copyFile(File aSourceFile, File aTargetFile, boolean aAppend) throws IOException {
 		FileChannel inChannel = null;
 		FileChannel outChannel = null;
 		FileInputStream inStream = null;
 		FileOutputStream outStream = null;
 		try {
 			inStream = new FileInputStream(aSourceFile);
 			inChannel = inStream.getChannel();
 			outStream = new FileOutputStream(aTargetFile, aAppend);
 			outChannel = outStream.getChannel();
 			long bytesTransferred = 0;
 			// defensive loop - there's usually only a single iteration :
 			while (bytesTransferred < inChannel.size()) {
 				bytesTransferred += inChannel.transferTo(0, inChannel.size(),
 						outChannel);
 			}
 		} finally {
 			// being defensive about closing all channels and streams
 			if (inChannel != null)
 				inChannel.close();
 			if (outChannel != null)
 				outChannel.close();
 			if (inStream != null)
 				inStream.close();
 			if (outStream != null)
 				outStream.close();
 		}
 	}
 
 	public static final String md5(final String s) {
 		try {
 			// Create MD5 Hash
 			MessageDigest digest = java.security.MessageDigest
 					.getInstance("MD5");
 			digest.update(s.getBytes());
 			byte messageDigest[] = digest.digest();
 
 			// Create Hex String
 			StringBuffer hexString = new StringBuffer();
 			for (int i = 0; i < messageDigest.length; i++) {
 				String h = Integer.toHexString(0xFF & messageDigest[i]);
 				while (h.length() < 2)
 					h = "0" + h;
 				hexString.append(h);
 			}
 			return hexString.toString();
 
 		} catch (NoSuchAlgorithmException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 
 	@SuppressWarnings("deprecation")
 	public static int compare(Date a, Date b) {
 		Integer ha = a.getHours();
 		Integer hb = b.getHours();
 		int cmp = ha.compareTo(hb);
 		if (cmp != 0)
 			return cmp;
 		Integer ma = a.getMinutes();
 		Integer mb = b.getMinutes();
 		cmp = ma.compareTo(mb);
 		if (cmp != 0)
 			return cmp;
 		
 		Integer sa = a.getSeconds();
 		Integer sb = b.getSeconds();
 		cmp = sa.compareTo(sb);
 		return cmp;
 	}
 
 	public static String formatElapsedTime(long totalTimeSeconds) { 
 		int seconds = (int) (totalTimeSeconds) % 60 ;
 		int minutes = (int) ((totalTimeSeconds / (60)) % 60);
 		int hours   = (int) ((totalTimeSeconds / (60*60)) % 24);
 		return String.format("%dh,  %dm, %ds", hours, minutes, seconds);
 	}
 
 	public static void saveObject(Context context, String fileName, Object obj) throws IOException {
 		FileOutputStream fos = context.openFileOutput(fileName,
 				Context.MODE_PRIVATE);
 		ObjectOutput out = null;
 		try {
 			out = new ObjectOutputStream(fos);
 			out.writeObject(obj);
 			out.flush();
 		} finally {
 			out.close();
 			fos.close();
 		}
 		
 	}
 
 	public static Object readObject(Context context, String fileName) {
 		File file = context.getFileStreamPath(fileName);
 		if (file.exists()) {
 			try {
 				FileInputStream fis = context.openFileInput(fileName);
 				ObjectInput in = null;
 				try {
 					in = new ObjectInputStream(fis);
 					try {
 						return in.readObject();
 					} catch (Exception ex) {
 						Log.e(Const.ECOMMUTERS_TAG, Log.getStackTraceString(ex)); 
 					}
 				} catch (Exception e) {
 					Log.e(Const.ECOMMUTERS_TAG, Log.getStackTraceString(e)); 
 				} finally {
 					in.close();
 					fis.close();
 				}
 			} catch (Exception e) {
 				Log.e(Const.ECOMMUTERS_TAG, Log.getStackTraceString(e)); 
 			}
 
 		}
 		return null;
 	}
 }
