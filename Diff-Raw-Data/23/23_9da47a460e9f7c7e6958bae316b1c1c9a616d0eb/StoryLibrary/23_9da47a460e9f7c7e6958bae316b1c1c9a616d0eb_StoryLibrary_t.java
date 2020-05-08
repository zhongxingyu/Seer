 package com.example.adventurebook;
 
 import java.util.Collection;
 
 
public class StoryLibrary extends Model{
 
 	/**
 	 * @uml.property   name="eSClient"
 	 * @uml.associationEnd   inverse="onlineLibrary:com.example.adventurebook.SController"
 	 */
 	private SController esClient;
 
 	/** 
 	 * Getter of the property <tt>eSClient</tt>
 	 * @return  Returns the esClient.
 	 * @uml.property  name="eSClient"
 	 */
 	public SController getESClient() {
 		return esClient;
 	}
 
 	/** 
 	 * Setter of the property <tt>eSClient</tt>
 	 * @param eSClient  The esClient to set.
 	 * @uml.property  name="eSClient"
 	 */
 	public void setESClient(SController esClient) {
 		this.esClient = esClient;
 	}
 
 	/**
 	 * @uml.property   name="story"
 	 * @uml.associationEnd   multiplicity="(0 -1)" aggregation="shared" inverse="onlineLibrary:com.example.adventurebook.Story"
 	 */
 	private Collection<Story> story;
 
 	/** 
 	 * Getter of the property <tt>story</tt>
 	 * @return  Returns the story.
 	 * @uml.property  name="story"
 	 */
 	public Collection<Story> getStory() {
 		return story;
 	}
 
 	/** 
 	 * Setter of the property <tt>story</tt>
 	 * @param story  The story to set.
 	 * @uml.property  name="story"
 	 */
 	public void setStory(Collection<Story> story) {
 		this.story = story;
 	}
 
 	/**
 	 * @uml.property  name="sController"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="storyLibrary:com.example.adventurebook.SController"
 	 */
 	private SController sController = new com.example.adventurebook.SController();
 
 	/**
 	 * Getter of the property <tt>sController</tt>
 	 * @return  Returns the sController.
 	 * @uml.property  name="sController"
 	 */
 	public SController getSController() {
 		return sController;
 	}
 
 	/**
 	 * Setter of the property <tt>sController</tt>
 	 * @param sController  The sController to set.
 	 * @uml.property  name="sController"
 	 */
 	public void setSController(SController sController) {
 		this.sController = sController;
 	}
 
 }
