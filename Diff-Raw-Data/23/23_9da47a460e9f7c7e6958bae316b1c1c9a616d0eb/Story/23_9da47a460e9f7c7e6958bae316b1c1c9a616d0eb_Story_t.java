 package com.example.adventurebook;
 
 import java.util.Collection;
 
 
public class Story extends Model{
 
 	/**
 	 * @uml.property   name="onlineLibrary"
 	 * @uml.associationEnd   inverse="story:com.example.adventurebook.StoryLibrary"
 	 */
 	private StoryLibrary onlineLibrary;
 
 	/** 
 	 * Getter of the property <tt>onlineLibrary</tt>
 	 * @return  Returns the onlineLibrary.
 	 * @uml.property  name="onlineLibrary"
 	 */
 	public StoryLibrary getOnlineLibrary() {
 		return onlineLibrary;
 	}
 
 	/** 
 	 * Setter of the property <tt>onlineLibrary</tt>
 	 * @param onlineLibrary  The onlineLibrary to set.
 	 * @uml.property  name="onlineLibrary"
 	 */
 	public void setOnlineLibrary(StoryLibrary onlineLibrary) {
 		this.onlineLibrary = onlineLibrary;
 	}
 
 	/**
 	 * @uml.property  name="storyFragment"
 	 * @uml.associationEnd  multiplicity="(0 -1)" aggregation="shared" inverse="story:com.example.adventurebook.StoryFragment"
 	 */
 	private Collection<StoryFragment> storyFragment;
 
 	/**
 	 * Getter of the property <tt>storyFragment</tt>
 	 * @return  Returns the storyFragment.
 	 * @uml.property  name="storyFragment"
 	 */
 	public Collection<StoryFragment> getStoryFragment() {
 		return storyFragment;
 	}
 
 	/**
 	 * Setter of the property <tt>storyFragment</tt>
 	 * @param storyFragment  The storyFragment to set.
 	 * @uml.property  name="storyFragment"
 	 */
 	public void setStoryFragment(Collection<StoryFragment> storyFragment) {
 		this.storyFragment = storyFragment;
 	}
 
 	/**
 	 * @uml.property  name="sController"
 	 * @uml.associationEnd  inverse="story:com.example.adventurebook.SController"
 	 */
 	private SController sController;
 
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
