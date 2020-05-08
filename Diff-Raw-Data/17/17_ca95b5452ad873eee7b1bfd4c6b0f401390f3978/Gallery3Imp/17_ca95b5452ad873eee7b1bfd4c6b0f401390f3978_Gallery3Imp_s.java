 /*
  * GalDroid - a webgallery frontend for android
  * Copyright (C) 2011  Raptor 2101 [raptor2101@gmx.de]
  *		
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.  
  */
 
 package de.raptor2101.GalDroid.WebGallery.Gallery3;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.StringBody;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.util.FloatMath;
 
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryObject;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.GalleryProgressListener;
 import de.raptor2101.GalDroid.WebGallery.Interfaces.WebGallery;
 
 public class Gallery3Imp implements WebGallery {
 	private HttpClient mHttpClient;
 	private String mSecurityToken;
 	private float mMaxImageDiag;
 	public final String LinkRest_LoadSecurityToken;
 	public final String LinkRest_LoadItem;
 	public final String LinkRest_LoadBunchItems;
 	public final String LinkRest_LoadPicture;
 
 	private static int MAX_REQUEST_SIZE = 4000;
 	
 	public Gallery3Imp(String rootLink)
 	{
 		LinkRest_LoadSecurityToken = rootLink+"/index.php/rest";
 		LinkRest_LoadItem = rootLink+"/index.php/rest/item/%d";
 		LinkRest_LoadBunchItems = rootLink +"/index.php/rest/items?urls=[%s]";
 		LinkRest_LoadPicture = rootLink +"/index.php/rest/data/%s?size=%s";
 	}
 	
 	public String getItemLink(int id){
 		return String.format(LinkRest_LoadItem, id);
 	}
 	
 	private JSONObject parseJSON(InputStream inputStream)
 			throws IOException, JSONException {
 		String content = loadContent(inputStream);
 		//System.out.print(content);
 		return new JSONObject(content);
 	}
 
 	private JSONArray parseJSONArray(InputStream inputStream)
 			throws IOException, JSONException {
 		String content = loadContent(inputStream);
 		return new JSONArray(content);
 	}
 	private String loadContent(InputStream inputStream)
 			throws IOException {
 		InputStreamReader streamReader = new InputStreamReader(inputStream);
 		BufferedReader reader = new BufferedReader(streamReader);
 		StringBuilder stringBuilder = new StringBuilder();
 		try {
 			String line = null;
 
 			while ((line = reader.readLine()) != null) {
 				stringBuilder.append(line + '\n');
 			}
 		} 
 		finally
 		{
 			reader.close();
 			inputStream.close();
 		}
 		System.out.print(stringBuilder.toString());
 		return stringBuilder.toString();
 	}
 	
 	private HttpUriRequest buildRequest(String url){
 		HttpGet httpRequest = new HttpGet(url);
         
 		httpRequest.addHeader("X-Gallery-Request-Method", "get");
 		httpRequest.addHeader("X-Gallery-Request-Key", mSecurityToken);
 		
 		return httpRequest;
 	}
 	
 	private InputStream openRestCall(String url) throws IOException, ClientProtocolException {
 		
 		HttpUriRequest httpRequest = buildRequest(url);
         HttpResponse response = mHttpClient.execute(httpRequest);
         
         return response.getEntity().getContent();
 	}
 	
 	public JSONObject loadJSONObject(String url) throws ClientProtocolException, IOException, JSONException{
 		InputStream inputStream = openRestCall(url);
 		return parseJSON(inputStream);
 	}
 	
 	public Entity loadGalleryEntity(String url) throws ClientProtocolException, IOException, JSONException
 	{
 		JSONObject jsonObject = loadJSONObject(url);
 		return loadGalleryEntity(jsonObject);
 	}
 
 	private Entity loadGalleryEntity(JSONObject jsonObject)
 			throws JSONException, ClientProtocolException, IOException {
 		String type = jsonObject.getJSONObject("entity").getString("type");
 		if(type.equals("album")){
 			return new AlbumEntity(jsonObject, this, mMaxImageDiag);
 		}
 		else {
 			return new PictureEntity(jsonObject, this, mMaxImageDiag);
 		}
 		
 	}
 	
 	public List<GalleryObject> getDisplayObjects() {
 		return getDisplayObjects(String.format(LinkRest_LoadItem, 1), null);
 	}
 	
 	public List<GalleryObject> getDisplayObjects(String url) {
 		return getDisplayObjects(url, null);
 	}
 	
 	public List<GalleryObject> getDisplayObjects(GalleryProgressListener progressListener) {
 		return getDisplayObjects(String.format(LinkRest_LoadItem, 1), progressListener);
 	}
 
 	public List<GalleryObject> getDisplayObjects(String url,GalleryProgressListener progressListener) {
 		ArrayList<GalleryObject> displayObjects;
 		
 		try {
 			GalleryObject galleryObject = loadGalleryEntity(url);
 			if(galleryObject.hasChildren()){
 				AlbumEntity album = (AlbumEntity) galleryObject;
 				List<String> members = album.getMembers();
 				int memberSize = members.size();
 				int objectCount = 0;
 				displayObjects = new ArrayList<GalleryObject>(memberSize);
 				
 				for(int i=0; i<memberSize; ){
 					StringBuilder urls = new StringBuilder(MAX_REQUEST_SIZE);
 					
 					/*
 					 *  If a Album contains a large number of Images
 					 *  load it in a bunch of 10 items.
 					 *  
 					 *  Otherwise the max length of a GET-Request might be exceeded
 					 */
 					
 					for( ; i<memberSize; i++){
 						String memberUrl = members.get(i);
 						if(urls.length()+memberUrl.length() > MAX_REQUEST_SIZE)
 						{
 							break;
 						}
 						urls.append("%22" + memberUrl + "%22,");
 					}
 					
 					urls.deleteCharAt(urls.length() - 1);
 					url = String.format(LinkRest_LoadBunchItems, urls);
 					
 					InputStream inputStream = openRestCall(url);
 					JSONArray jsonArray = parseJSONArray(inputStream);
 					int length = jsonArray.length();
 					for (int pos = 0; pos < length ; pos++) {
 						JSONObject jsonObject = jsonArray.getJSONObject(pos);
 						galleryObject = loadGalleryEntity(jsonObject);
 						displayObjects.add(galleryObject);
 						if (progressListener != null) {
 							objectCount++; 
 							progressListener.handleProgress(objectCount, memberSize);
 						}
 					}
 				}
 			}
 			else displayObjects = new ArrayList<GalleryObject>(0);
 				
 			
 		} catch (ClientProtocolException e) {
 			displayObjects = new ArrayList<GalleryObject>(0);
 			e.printStackTrace();
 		} catch (IOException e) {
 			displayObjects = new ArrayList<GalleryObject>(0);
 			e.printStackTrace();
 		} catch (JSONException e) {
 			displayObjects = new ArrayList<GalleryObject>(0);
 			e.printStackTrace();
 		}
 		System.out.print(displayObjects.size());
 		return displayObjects;
 	}
 
 	public InputStream getImageRawData(GalleryObject galleryObject, ImageSize imageSize) throws ClientProtocolException, IOException{
 		
 		String dataLink;
 		if(galleryObject instanceof AlbumEntity || galleryObject instanceof PictureEntity){
 			Entity galleryEntity = (Entity) galleryObject;
 			dataLink = galleryEntity.getUniqueId(imageSize);
 		}
 		else
 		{
 			throw new ClassCastException("Gallery3 implementation can't handle objects of type "+galleryObject.getClass().getName());
 		}
 		
 		return openRestCall(dataLink);
 		
 	}
 	
 	public void setHttpClient(HttpClient httpClient) {
 		mHttpClient = httpClient;
 	}
 
 	public String getSecurityToken(String user, String password) throws SecurityException {
		HttpPost httpRequest = new HttpPost(LinkRest_LoadSecurityToken);
        
		httpRequest.addHeader("X-Gallery-Request-Method", "post");
		MultipartEntity mpEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
 		try {
 			mpEntity.addPart("user", new StringBody(user));
 			mpEntity.addPart("password", new StringBody(password));
 			
 			httpRequest.setEntity(mpEntity);
 	        HttpResponse response;
 		
 			response = mHttpClient.execute(httpRequest);
 			InputStream  inputStream = response.getEntity().getContent();
 			String content = loadContent(inputStream);
 			if(content.length()==0 || content.startsWith("[]"))
 			{
 				throw new SecurityException("Couldn't verify user-credentials");
 			}
 			
			return content.trim().replace("\"", "");  
		} catch (ClientProtocolException e) {
			throw new SecurityException("Couldn't verify user-credentials", e);
		} catch (IOException e) {
 			throw new SecurityException("Couldn't verify user-credentials", e);
 		}
 	}
 
 	// TODO Remove
 	public HttpClient getHttpClient() {
 		return mHttpClient;
 	}
 
 	public void setSecurityToken(String securityToken) {
 		mSecurityToken = securityToken;
 	}
 
 	public void setPreferedDimensions(int height, int width) {
 		mMaxImageDiag = FloatMath.sqrt(height*height+width*width);
 	}	
 }
