 package uk.co.thomasc.wordmaster.objects;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import uk.co.thomasc.wordmaster.BaseGame;
 
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.games.Player;
 import com.google.android.gms.plus.PlusClient.OnPersonLoadedListener;
 import com.google.android.gms.plus.model.people.Person;
 
 public class User {
 
 	/* Properties */
 	private String plusID; 
 	private String name;
 	private String avatarUri;
 	
 	public User(String plusID, String name, Uri avatarUri) {
 		this.plusID = plusID;
 		this.name = name;
 		this.avatarUri = avatarUri.toString();
 	}
 	
 	public User(Player player) {
 		this.plusID = player.getPlayerId();
 		this.name = player.getDisplayName();
 		this.avatarUri = player.getIconImageUri().toString();
 	}
 	
 	public User(String plusID, BaseGame activityReference) {
 		this.plusID = plusID;
 		activityReference.getPlusClient().loadPerson(new OnPersonLoadedListener() {
 			@Override
 			public void onPersonLoaded(ConnectionResult arg0, Person arg1) {
 				System.out.println(arg1.getImage().getUrl());
 			}
 		}, plusID);
 	}
 	
 	/* Getters */
 	public String getPlusID() {
 		return plusID;
 	}
 	public String getName() {
 		return name;
 	}
 	public Uri getAvatarURL() {
 		return Uri.parse(avatarUri);
 	}
	public Drawable getAvatar() {
 		try {
			InputStream is = (InputStream) new URL(avatarUri).getContent();
			Drawable d = Drawable.createFromStream(is, "player avatar");
 			return d;
		} catch (MalformedURLException ex) {
			return null;
 		} catch (IOException ex) {
 			return null;
 		}
 	}
 	
 }
