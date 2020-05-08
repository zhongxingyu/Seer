 package com.samteladze.delta.statistics.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import android.content.Intent;
 import android.net.Uri;
 
 public class CommunicationManager
 {
 	private static final String SERVER_URL = "http://xdp-apps.org/stat-collector-2/collect";
 	private static final String MAIL_TEXT = "Current statistics for your device is attached.";
 	private static final String MAIL_SUBJECT = "DELTA Statistics";
 	
 	public static Intent CreatEmailDataIntent()
 	{
 		ArrayList<Uri> filesUri = new ArrayList<Uri>();
 		
 		if (FileManager.FileExists(FileManager.USER_APP_STAT_FILE_PATH))
 		{
 			Uri appFileUri = Uri.fromFile(FileManager.GetFile(FileManager.USER_APP_STAT_FILE_PATH));
 			filesUri.add(appFileUri);
 		}
 		if (FileManager.FileExists(FileManager.USER_NET_STAT_FILE_PATH))
 		{
 			Uri netFileUri = Uri.fromFile(FileManager.GetFile(FileManager.USER_NET_STAT_FILE_PATH));		
 			filesUri.add(netFileUri);
 		}
 		
 		Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
 		emailIntent.setType("text/plain");
 		emailIntent.putExtra(Intent.EXTRA_SUBJECT, MAIL_SUBJECT);
 		emailIntent.putExtra(Intent.EXTRA_TEXT, MAIL_TEXT);
 		emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUri);
 
 		return emailIntent;
 	}
 
 	public static boolean SendAppStatisticsToServer()
 	{
 		File fileToSend = FileManager.GetFile(FileManager.APP_STAT_FILE_PATH);
 		
 		try
 		{
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(SERVER_URL);
 
 			InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(fileToSend), -1);
 			reqEntity.setContentType("binary/octet-stream");
 			reqEntity.setChunked(true);
 			
 			httppost.setEntity(reqEntity);
 
 			@SuppressWarnings("unused")
 			HttpResponse response = httpclient.execute(httppost);
 			
 			LogManager.Log(CommunicationManager.class.getSimpleName(), "App statistics was sent to the server");
 			
 			return true;
 		} 
 		catch (Exception e)
 		{			
 			e.printStackTrace(System.err);
 			
 			LogManager.Log(CommunicationManager.class.getSimpleName(), "ERROR! Could not send app statistics to the server");
 			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
 		}
 		
 		return false;
 	}
 	
 	// TEMP
 	public static boolean SendTaggedDataToServer(String deviceID)
 	{
 		if (!SendFileToServer(FileManager.TagFile(FileManager.APP_STAT_FILE_PATH, "app", deviceID)) ||
 			!SendFileToServer(FileManager.TagFile(FileManager.NET_STAT_FILE_PATH, "net", deviceID))	||
 			!SendFileToServer(FileManager.TagFile(LogManager.LOG_FILE_PATH, "log", deviceID)))
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	public static boolean SendDataToServer(String deviceID)
 	{
 		File appFile = FileManager.GetFile(FileManager.APP_STAT_FILE_PATH);
 		File netFile = FileManager.GetFile(FileManager.NET_STAT_FILE_PATH);
 		File logFile = FileManager.GetFile(LogManager.LOG_FILE_PATH);
 		
 		try
 		{
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(SERVER_URL);
 
 			FileBody appBin= new FileBody(appFile);
 			FileBody netBin= new FileBody(netFile);
 			FileBody logBin= new FileBody(logFile);
 			
 			MultipartEntity reqEntity = new MultipartEntity();
 			reqEntity.addPart("appFile", appBin);
 			reqEntity.addPart("netFile", netBin);
 			reqEntity.addPart("logFile", logBin);
			reqEntity.addPart("id", new StringBody(deviceID));
 			
 			httppost.setEntity(reqEntity);
 
 			@SuppressWarnings("unused")
 			HttpResponse response = httpclient.execute(httppost);
 			
 			LogManager.Log(CommunicationManager.class.getSimpleName(), "Statistics and log were sent to the server");
 			
 			return true;
 		} 
 		catch (Exception e)
 		{			
 			e.printStackTrace(System.err);
 			
 			LogManager.Log(CommunicationManager.class.getSimpleName(), "ERROR! Could not send statistics and log to the server");
 			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
 		}
 		
 		return false;
 	}
 
 	private static boolean SendFileToServer(File file)
 	{
 		try
 		{
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpPost httppost = new HttpPost(SERVER_URL);
 
 			InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
 			reqEntity.setContentType("binary/octet-stream");
 			reqEntity.setChunked(true);
 			
 			httppost.setEntity(reqEntity);
 
 			@SuppressWarnings("unused")
 			HttpResponse response = httpclient.execute(httppost);
 			
 			LogManager.Log(CommunicationManager.class.getSimpleName(), "File was sent to the server");
 			
 			return true;
 		} 
 		catch (Exception e)
 		{			
 			e.printStackTrace(System.err);
 			
 			LogManager.Log(CommunicationManager.class.getSimpleName(), "ERROR! Could not send file to the server");
 			LogManager.Log(CommunicationManager.class.getSimpleName(), e.toString());
 			
 			return false;
 		}
 	}
 }
