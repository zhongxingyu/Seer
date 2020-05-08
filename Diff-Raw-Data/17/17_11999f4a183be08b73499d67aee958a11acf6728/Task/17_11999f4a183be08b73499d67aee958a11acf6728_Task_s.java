 package ca.ualberta.cs.completemytask.userdata;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 import android.util.Log;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * Stores a information about a task.
  * 
  * @author Michael Feist
  * 
  */
 
 public class Task extends UserData {
 
 	private static final String TAG = "Task";
 
 	private boolean shared;
 	private boolean complete;
 	private boolean local;
 
 	private String name;
 	private String description;
 	
 	private boolean needsComment;
 	private boolean needsPhoto;
 	private boolean needsAudio;
 	
 	private List<Comment> comments;
 	private List<MyPhoto> photos;
 	private List<MyAudio> audios;
 	
 	private Set<Long> localComments;
 	private Set<Long> globalComments;
 	private Set<Long> localPhotos;
 	private Set<Long> globalPhotos;
 	private Set<Long> localAudios;
 	private Set<Long> globalAudios;
 	
 	private int numOfCommentsOnServer;
 	private int numOfPhotosOnServer;
 	private int numOfAudiosOnServer;
 
 	public Task() {
 		this("New Task", "No Description");
 	}
 
 	public Task(String name, String description) {
 		super();
 		this.shared = false;
 		this.complete = false;
 		this.local = true;
 
 		this.name = name;
 		this.description = description;
 		
 		this.needsComment = false;
 		this.needsPhoto = false;
 		this.needsAudio = false;
 		
 		this.comments = new ArrayList<Comment>();
 		this.photos = new ArrayList<MyPhoto>();
 		this.audios = new ArrayList<MyAudio>();
 		
 		this.localComments = new TreeSet<Long>();
 		this.globalComments = new TreeSet<Long>();
 		this.localPhotos = new TreeSet<Long>();
 		this.globalPhotos = new TreeSet<Long>();
 		this.localAudios = new TreeSet<Long>();
 		this.globalAudios = new TreeSet<Long>();
 		
 		this.numOfCommentsOnServer = 0;
 		this.numOfPhotosOnServer = 0;
 		this.numOfAudiosOnServer = 0;
 	}
 	
 	public void setNumberOfAttachments(int comments, int photos, int audios) {
 		this.numOfCommentsOnServer = comments;
 		this.numOfPhotosOnServer = photos;
 		this.numOfAudiosOnServer = audios;
 	}
 	
 	public int getNumberOfCommentsOnServer() {
 		return this.numOfCommentsOnServer;
 	}
 	
 	public int getNumberOfPhotosOnServer() {
 		return this.numOfPhotosOnServer;
 	}
 	
 	public int getNumberOfAudiosOnServer() {
 		return this.numOfAudiosOnServer;
 	}
 	
 	/**
 	 * Get the number of comments contained in this task.
 	 * @return Number of comments
 	 */
 	public int getNumberOfComments() {
 		return this.comments.size();
 	}
 	
 	/**
 	 * Add a comment to this task.
 	 * @param A comment
 	 */
 	public void addComment(Comment comment) {
 		
		if (this.localComments.contains(comment.getLocalId()))
 			return;
 		
		if (this.globalComments.contains(comment.getId()))
 			return;
 		
 		this.localComments.add(comment.getLocalId());
 		this.globalComments.add(comment.getId());
 		
 		comment.setLocalParentId(this.getLocalId());
 		this.comments.add(comment);
 	}
 	
 	/**
 	 * Get the comment at the given position.
 	 * @param The position in the comments array.
 	 * @return The comment at that position.
 	 */
 	public Comment getCommentAt(int position) {
 		return this.comments.get(position);
 	}
 	
 	/**
 	 * Get the number of photos contained in this task.
 	 * @return Number of photos
 	 */
 	public int getNumberOfPhotos() {
 		return this.photos.size();
 	}
 	
 	/**
 	 * Add a photo to this task.
 	 * @param A photo
 	 */
 	public void addPhoto(MyPhoto photo) {
		if (this.localPhotos.contains(photo.getLocalId()))
 			return;
 		
		if (this.globalPhotos.contains(photo.getId()))
 			return;
 		
 		this.localPhotos.add(photo.getLocalId());
 		this.globalPhotos.add(photo.getId());
 		
 		photo.setLocalParentId(this.getLocalId());
 		this.photos.add(photo);
 	}
 	
 	/**
 	 * Get the photo at the given position.
 	 * @param The position in the photos array.
 	 * @return The photo at that position.
 	 */
 	public MyPhoto getPhotoAt(int position) {
 		return this.photos.get(position);
 	}
 	
 	/**
 	 * Get the number of audio files contained in this task.
 	 * @return Number of audio files
 	 */
 	public int getNumberOfAudios() {
 		return this.audios.size();
 	}
 	
 	/**
 	 * Add an audio file to this task.
 	 * @param An audio file
 	 */
 	public void addAudio(MyAudio audio) {
		if (this.localAudios.contains(audio.getLocalId()))
 			return;
 		
		if (this.globalAudios.contains(audio.getId()))
 			return;
 		
 		this.localAudios.add(audio.getLocalId());
 		this.globalAudios.add(audio.getId());
 		
 		audio.setLocalParentId(this.getLocalId());
 		this.audios.add(audio);
 	}
 	
 	/**
 	 * Get the audio file at the given position.
 	 * @param The position in the audios array.
 	 * @return The audio at that position.
 	 */
 	public MyAudio getAudioAt(int position) {
 		return this.audios.get(position);
 	}
 	
 	/**
 	 * Set whether or not this task should be saved locally.
 	 * @param is local
 	 */
 	public void setLocal(boolean local) {
 		this.local = local;
 	}
 	
 	/**
 	 * Should the task be saved to the local storage. 
 	 * @return true if it should be saved locally
 	 */
 	public boolean isLocal() {
 		return this.local;
 	}
 	
 	/**
 	 * Set whether or not this task should shared with
 	 * the public.
 	 * @param true if it should shared with public
 	 */
 	public void setPublic(boolean shared) {
 		this.shared = shared;
 	}
 	
 	/**
 	 * Should this be shared with the public.
 	 * @return true if it should shared with public
 	 */
 	public boolean isPublic() {
 		return shared;
 	}
 	
 	/**
 	 * Set if this task is completed or not.
 	 * @param true if complete
 	 */
 	public void setComplete(boolean complete) {
 		this.complete = complete;
 		this.sync = true;
 	}
 	
 	/**
 	 * Check if this task was completed.
 	 * @return true if completed
 	 */
 	public boolean isComplete() {
 		return complete;
 	}
 
 	/**
 	 * Set the name of the task.
 	 * @param name of task
 	 */
 	public void setName(String name) {
 		this.name = name;
 		this.sync = true;
 	}
 	
 	/**
 	 * Gets the given name of the task.
 	 * @return name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Set the description of the task.
 	 * @param description
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 		this.sync = true;
 	}
 	/**
 	 * Gets the given description of the task.
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 	
 	/**
 	 * Set what this task requires.
 	 * @param comment
 	 * @param photo
 	 * @param audio
 	 */
 	public void setRequirements(boolean comment, boolean photo, boolean audio) {
 		this.needsComment = comment;
 		this.needsPhoto = photo;
 		this.needsAudio = audio;
 	}
 	
 	/**
 	 * Checks if the task needs a comment.
 	 * @return true if task needs a comment
 	 */
 	public boolean needsComment() {
 		return this.needsComment;
 	}
 
 	/**
 	 * Checks if the task needs a photo.
 	 * @return true if task needs a photo
 	 */
 	public boolean needsPhoto() {
 		return this.needsPhoto;
 	}
 	
 	/**
 	 * Checks if the task needs a audio stream.
 	 * @return true if task needs a audio stream
 	 */
 	public boolean needsAudio() {
 		return this.needsAudio;
 	}
 	
 	/**
 	 * Check if the task needs to be synchronized with the database.
 	 * @return true if task needs to be synchronized with the database
 	 */
 	public boolean needsSync() {
 		return this.sync && this.shared;
 	}
 	
 	/**
 	 * Returns the task as a string.
 	 * @return A string
 	 */
 	public String toString() {
 		
 		String yes = "YES";
 		String no = "NO";
 		
 		String publicTask = no;
 		
 		String comments = no;
 		String photos = no;
 		String audios = no;
 		
 		String userName = "Unknown";
 		
 		if (needsComment) {
 			comments = yes;
 		}
 		
 		if (needsPhoto) {
 			photos = yes;
 		}
 		
 		if (needsAudio) {
 			audios = yes;
 		}
 		
 		if (shared) {
 			publicTask = yes;
 		}
 		
 		if (hasUser()) {
 			userName = getUser().getUserName();
 		}
 
 		String task = String.format(
 				"Task: \n\t " +
 				"Name: %s\n\t " +
 				"Description: %s\n\t " +
 				"Created By: %s\n\t " +
 				"Id: %s \n\t " +
 				"Public: %s \n\n\t " +
 				"Requirements: \n\t\t " +
 				"Needs Comments: %s \n\t\t Needs Photos: %s \n\t\t Needs Audio: %s \n\t",
 				this.name, this.description,
 				userName,
 				this.id, publicTask,
 				comments, photos, audios);
 
 		return task;
 	}
 	
 	/**
 	 * Gets the task as a string in JSON form.
 	 * @return String in JSON form
 	 */
 	public String toJSON() {
 		
 		JSONObject json = new JSONObject();
 		
 		String userName = "Unknown";
 		String email = "";
 		
 		if (hasUser()) {
 			userName = this.user.getUserName();
 			email = this.user.getEmail();
 		}
 		
         try {
 			json.put( "type", "Task");
 			json.put( "name", this.name);
 			json.put( "description", this.description);
 			json.put( "user", userName);
 			json.put( "email", email);
 			json.put( "needsComment", this.needsComment);
 			json.put( "needsPhoto", this.needsPhoto);
 			json.put( "needsAudio", this.needsAudio);
 			json.put( "isComplete", this.complete);
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		
         Log.v(TAG, "JSON: " + json.toString());
         
 		return json.toString();
 	}
 	
 	/**
 	 * From the given JSONObject retrive the needed
 	 * info for the task.
 	 * @param JSON data
 	 * @return A task
 	 */
 	public void decodeTask(JSONObject data) {
 		String userName = "Unknown";
 		String email = "";
 		
 		try {
 			this.name = data.getString("name");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get task name.");
 			this.name = "Unknown";
 		}
 		
 		try {
 			this.description = data.getString("description");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get task description.");
 			this.description = "Unknown";
 		}		
 		
 		try {
 			userName = data.getString("user");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get user.");
 			userName = "Unknown";
 		} 
 		
 		try {
 			email = data.getString("email");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get user email.");
 			email = "";
 		}
 		
 		try {
 			this.needsComment = data.getBoolean("needsComment");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get needsComment.");
 			this.needsComment = false;
 		} 
 		
 		try {
 			this.needsPhoto = data.getBoolean("needsPhoto");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get needsPhoto.");
 			this.needsPhoto = false;
 		} 
 		
 		try {
 			this.needsAudio = data.getBoolean("needsAudio");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get needsAudio.");
 			this.needsAudio = false;
 		} 
 		
 		try {
 			this.complete = data.getBoolean("isComplete");
 		} catch (JSONException e) {
 			Log.w(TAG, "Failed to get completion.");
 			this.complete = false;
 		} 
 		
 		User user = new User(userName, email);
 		this.setUser(user);
 		
 		this.setPublic(true);
 		this.setLocal(false);
 		this.syncFinished();
 		
 	}
 }
