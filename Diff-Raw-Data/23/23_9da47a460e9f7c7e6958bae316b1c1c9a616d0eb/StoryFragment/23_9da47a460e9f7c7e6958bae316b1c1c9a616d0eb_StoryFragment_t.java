 package com.example.adventurebook;
 
 import java.util.Collection;
 
 
public class StoryFragment extends Model{
 
 	/** 
 	 * @uml.property name="video"
 	 * @uml.associationEnd multiplicity="(0 -1)" aggregation="shared" inverse="storyFragment:com.example.adventurebook.Video"
 	 */
 	private Collection<Video> video;
 
 	/** 
 	 * Getter of the property <tt>video</tt>
 	 * @return  Returns the video.
 	 * @uml.property  name="video"
 	 */
 	public Collection<Video> getVideo() {
 		return video;
 	}
 
 	/** 
 	 * Setter of the property <tt>video</tt>
 	 * @param video  The video to set.
 	 * @uml.property  name="video"
 	 */
 	public void setVideo(Collection<Video> video) {
 		this.video = video;
 	}
 
 	/**
 	 * @uml.property  name="sound"
 	 * @uml.associationEnd  multiplicity="(0 -1)" aggregation="shared" inverse="storyFragment:com.example.adventurebook.Sound"
 	 */
 	private Collection<Sound> sound;
 
 	/**
 	 * Getter of the property <tt>sound</tt>
 	 * @return  Returns the sound.
 	 * @uml.property  name="sound"
 	 */
 	public Collection<Sound> getSound() {
 		return sound;
 	}
 
 	/**
 	 * Setter of the property <tt>sound</tt>
 	 * @param sound  The sound to set.
 	 * @uml.property  name="sound"
 	 */
 	public void setSound(Collection<Sound> sound) {
 		this.sound = sound;
 	}
 
 	/**
 	 * @uml.property  name="photo"
 	 * @uml.associationEnd  multiplicity="(0 -1)" aggregation="shared" inverse="storyFragment:com.example.adventurebook.Photo"
 	 */
 	private Collection<Photo> photo;
 
 	/**
 	 * Getter of the property <tt>photo</tt>
 	 * @return  Returns the photo.
 	 * @uml.property  name="photo"
 	 */
 	public Collection<Photo> getPhoto() {
 		return photo;
 	}
 
 	/**
 	 * Setter of the property <tt>photo</tt>
 	 * @param photo  The photo to set.
 	 * @uml.property  name="photo"
 	 */
 	public void setPhoto(Collection<Photo> photo) {
 		this.photo = photo;
 	}
 
 	/**
 	 * @uml.property  name="story"
 	 * @uml.associationEnd  inverse="storyFragment:com.example.adventurebook.Story"
 	 */
 	private Story story;
 
 	/**
 	 * Getter of the property <tt>story</tt>
 	 * @return  Returns the story.
 	 * @uml.property  name="story"
 	 */
 	public Story getStory() {
 		return story;
 	}
 
 	/**
 	 * Setter of the property <tt>story</tt>
 	 * @param story  The story to set.
 	 * @uml.property  name="story"
 	 */
 	public void setStory(Story story) {
 		this.story = story;
 	}
 
 }
