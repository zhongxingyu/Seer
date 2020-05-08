 package com.columbia.server;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.ParseException;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.methods.HttpGet;
 
 import com.columbia.location.BoundaryPoint;
 import com.columbia.location.CenterPoint;
 import com.columbia.quest.Quest;
 
 import android.content.Intent;
 import android.os.Bundle;
 
 public class CreateQuestQuery extends ServerQuery {
 	
 	Intent intent;
 	String query;
 	int interactionType;
 	String name;
 	String description;
 	double cLat;
 	double cLong;
 	String b1Lat;
 	String b1Long;
 	String b2Lat;
 	String b2Long;
 	String b3Lat;
 	String b3Long;
 	String b4Lat;
 	String b4Long;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		intent = getIntent();
 		interactionType = intent.getIntExtra("interactionType",0);
 		this.execute();
 	}
 	
 	public void run() {
 		name = intent.getStringExtra("name");
		name = name.replaceAll(" ", "_");
 		description = intent.getStringExtra("description");
		description = description.replaceAll(" ", "_");
 		CenterPoint c = Quest.getCenterPoint(intent);
 		List<BoundaryPoint> bounds = Quest.getBoundaryPoints(intent);
 		cLat = c.getLatitudeE6() / 1E6;
 		cLong = c.getLongitudeE6() / 1E6;
 		b1Lat = bounds.get(0).getLatitudeE6() + "";
 		b1Long = bounds.get(0).getLongitudeE6() + "";
 		b2Lat = bounds.get(1).getLatitudeE6() + "";
 		b2Long = bounds.get(1).getLongitudeE6() + "";
 		b3Lat = bounds.get(2).getLatitudeE6() + "";
 		b3Long = bounds.get(2).getLongitudeE6() + "";
 		b4Lat = bounds.get(3).getLatitudeE6() + "";
 		b4Long = bounds.get(3).getLongitudeE6() + "";
 		
 		query = address + "/addquest/?name=" + name + "&latitude=" + cLat + "&longitude=" + cLong + 
 				"&description=" + description + "&b1_lon=" + b1Long + "&b1_lat=" + b1Lat + "&b2_lon="
 				+ b2Long + "&b2_lat=" + b2Lat +"&b3_lon=" + b3Long + "&b3_lat=" + b3Lat + 
 				"&b4_lon=" + b4Long + "&b4_lat=" + b4Lat + "&username=" + ServerHelper.email + 
 				"&password=" + ServerHelper.password;
 		
 		HttpResponse response = null;
 		if (ServerQuery.GET == interactionType) {
 			response = get();
 		}
 		else if (ServerQuery.POST == interactionType) {
 			response = post();
 		}
 		HttpEntity entity = response.getEntity();
 		String sResponse = null;
 		try {
 			sResponse = ServerHelper.getResponseBody(entity);
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		
 		intent.putExtra("xml", sResponse);
     	setResult(0,intent);
     	finish();
 	}
 
 	@Override
 	public HttpResponse get() {		// get quests
 		HttpGet httpGet = new HttpGet(query);
 		try {
 			return httpClient.execute(httpGet);
 		} catch (ClientProtocolException e) {
 			return null;
 		} catch (IOException e) {
 			return null;
 		}
 	}
 
 	@Override
 	public HttpResponse post() { // check answer
 		return null;
 	}
 
 }
