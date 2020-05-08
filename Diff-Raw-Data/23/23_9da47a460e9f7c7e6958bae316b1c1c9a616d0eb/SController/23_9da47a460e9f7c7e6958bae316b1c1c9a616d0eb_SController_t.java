 package com.example.adventurebook;
 
 import java.util.Collection;
 
 
 public class SController {
 
 	/**
 	 * @uml.property   name="onlineLibrary"
 	 * @uml.associationEnd   inverse="eSClient:com.example.adventurebook.StoryLibrary"
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
 	 * @uml.property   name="remoteDatabase"
 	 * @uml.associationEnd   inverse="eSClient:com.example.adventurebook.OnlineLibrary"
 	 */
 	private OnlineLibrary remoteDatabase;
 
 	/**
 	 * Getter of the property <tt>remoteDatabase</tt>
 	 * @return  Returns the remoteDatabase.
 	 * @uml.property  name="remoteDatabase"
 	 */
 	public OnlineLibrary getRemoteDatabase() {
 		return remoteDatabase;
 	}
 
 	/**
 	 * Setter of the property <tt>remoteDatabase</tt>
 	 * @param remoteDatabase  The remoteDatabase to set.
 	 * @uml.property  name="remoteDatabase"
 	 */
 	public void setRemoteDatabase(OnlineLibrary remoteDatabase) {
 		this.remoteDatabase = remoteDatabase;
 	}
 
 	/** 
 	 * @uml.property name="offlineLibraryActivity"
 	 * @uml.associationEnd aggregation="shared" inverse="controller:com.example.adventurebook.OfflineLibraryActivity"
 	 */
 	private OfflineLibraryActivity offlineLibraryActivity;
 
 	/** 
 	 * Getter of the property <tt>offlineLibraryActivity</tt>
 	 * @return  Returns the offlineLibraryActivity.
 	 * @uml.property  name="offlineLibraryActivity"
 	 */
 	public OfflineLibraryActivity getOfflineLibraryActivity() {
 		return offlineLibraryActivity;
 	}
 
 	/** 
 	 * Setter of the property <tt>offlineLibraryActivity</tt>
 	 * @param offlineLibraryActivity  The offlineLibraryActivity to set.
 	 * @uml.property  name="offlineLibraryActivity"
 	 */
 	public void setOfflineLibraryActivity(
 			OfflineLibraryActivity offlineLibraryActivity) {
 				this.offlineLibraryActivity = offlineLibraryActivity;
 			}
 
 	/** 
 	 * @uml.property name="onlineLibraryActivity"
 	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared" inverse="sController:com.example.adventurebook.OnlineLibraryActivity"
 	 */
 	private OnlineLibraryActivity onlineLibraryActivity = new com.example.adventurebook.OnlineLibraryActivity();
 
 	/** 
 	 * Getter of the property <tt>onlineLibraryActivity</tt>
 	 * @return  Returns the onlineLibraryActivity.
 	 * @uml.property  name="onlineLibraryActivity"
 	 */
 	public OnlineLibraryActivity getOnlineLibraryActivity() {
 		return onlineLibraryActivity;
 	}
 
 	/** 
 	 * Setter of the property <tt>onlineLibraryActivity</tt>
 	 * @param onlineLibraryActivity  The onlineLibraryActivity to set.
 	 * @uml.property  name="onlineLibraryActivity"
 	 */
 	public void setOnlineLibraryActivity(
 			OnlineLibraryActivity onlineLibraryActivity) {
 				this.onlineLibraryActivity = onlineLibraryActivity;
 			}
 
 	/**
 	 * @uml.property  name="createStoryActivity"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="sController:com.example.adventurebook.CreateStoryActivity"
 	 */
 	private CreateStoryActivity createStoryActivity = new com.example.adventurebook.CreateStoryActivity();
 
 	/**
 	 * Getter of the property <tt>createStoryActivity</tt>
 	 * @return  Returns the createStoryActivity.
 	 * @uml.property  name="createStoryActivity"
 	 */
 	public CreateStoryActivity getCreateStoryActivity() {
 		return createStoryActivity;
 	}
 
 	/**
 	 * Setter of the property <tt>createStoryActivity</tt>
 	 * @param createStoryActivity  The createStoryActivity to set.
 	 * @uml.property  name="createStoryActivity"
 	 */
 	public void setCreateStoryActivity(CreateStoryActivity createStoryActivity) {
 		this.createStoryActivity = createStoryActivity;
 	}
 
 	/** 
 	 * @uml.property name="createStoryActivity1"
 	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared" inverse="sController1:com.example.adventurebook.CreateStoryActivity"
 	 */
 	private CreateStoryActivity createStoryActivity1 = new com.example.adventurebook.CreateStoryActivity();
 
 	/** 
 	 * Getter of the property <tt>createStoryActivity1</tt>
 	 * @return  Returns the createStoryActivity1.
 	 * @uml.property  name="createStoryActivity1"
 	 */
 	public CreateStoryActivity getCreateStoryActivity1() {
 		return createStoryActivity1;
 	}
 
 	/** 
 	 * Setter of the property <tt>createStoryActivity1</tt>
 	 * @param createStoryActivity1  The createStoryActivity1 to set.
 	 * @uml.property  name="createStoryActivity1"
 	 */
 	public void setCreateStoryActivity1(CreateStoryActivity createStoryActivity1) {
 		this.createStoryActivity1 = createStoryActivity1;
 	}
 
 	/**
 	 * @uml.property  name="editStoryActivity"
 	 * @uml.associationEnd  multiplicity="(1 1)" inverse="sController:com.example.adventurebook.EditStoryActivity"
 	 */
 	private EditStoryActivity editStoryActivity = new com.example.adventurebook.EditStoryActivity();
 
 	/**
 	 * Getter of the property <tt>editStoryActivity</tt>
 	 * @return  Returns the editStoryActivity.
 	 * @uml.property  name="editStoryActivity"
 	 */
 	public EditStoryActivity getEditStoryActivity() {
 		return editStoryActivity;
 	}
 
 	/**
 	 * Setter of the property <tt>editStoryActivity</tt>
 	 * @param editStoryActivity  The editStoryActivity to set.
 	 * @uml.property  name="editStoryActivity"
 	 */
 	public void setEditStoryActivity(EditStoryActivity editStoryActivity) {
 		this.editStoryActivity = editStoryActivity;
 	}
 
 	/**
 	 * @uml.property  name="viewPageActivity"
 	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="sController:com.example.adventurebook.ViewPageActivity"
 	 */
 	private ViewPageActivity viewPageActivity = new com.example.adventurebook.ViewPageActivity();
 
 	/**
 	 * Getter of the property <tt>viewPageActivity</tt>
 	 * @return  Returns the viewPageActivity.
 	 * @uml.property  name="viewPageActivity"
 	 */
 	public ViewPageActivity getViewPageActivity() {
 		return viewPageActivity;
 	}
 
 	/**
 	 * Setter of the property <tt>viewPageActivity</tt>
 	 * @param viewPageActivity  The viewPageActivity to set.
 	 * @uml.property  name="viewPageActivity"
 	 */
 	public void setViewPageActivity(ViewPageActivity viewPageActivity) {
 		this.viewPageActivity = viewPageActivity;
 	}
 
 	/** 
 	 * @uml.property name="viewPageActivity1"
 	 * @uml.associationEnd multiplicity="(1 1)" aggregation="shared" inverse="sController1:com.example.adventurebook.ViewPageActivity"
 	 */
 	private ViewPageActivity viewPageActivity1 = new com.example.adventurebook.ViewPageActivity();
 
 	/** 
 	 * Getter of the property <tt>viewPageActivity1</tt>
 	 * @return  Returns the viewPageActivity1.
 	 * @uml.property  name="viewPageActivity1"
 	 */
 	public ViewPageActivity getViewPageActivity1() {
 		return viewPageActivity1;
 	}
 
 	/** 
 	 * Setter of the property <tt>viewPageActivity1</tt>
 	 * @param viewPageActivity1  The viewPageActivity1 to set.
 	 * @uml.property  name="viewPageActivity1"
 	 */
 	public void setViewPageActivity1(ViewPageActivity viewPageActivity1) {
 		this.viewPageActivity1 = viewPageActivity1;
 	}
 
 	/**
 	 * @uml.property  name="story"
 	 * @uml.associationEnd  multiplicity="(0 -1)" aggregation="shared" inverse="sController:com.example.adventurebook.Story"
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
 	 * @uml.property  name="editStoryActivity1"
 	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="sController1:com.example.adventurebook.EditStoryActivity"
 	 */
 	private EditStoryActivity editStoryActivity1 = new com.example.adventurebook.EditStoryActivity();
 
 	/**
 	 * Getter of the property <tt>editStoryActivity1</tt>
 	 * @return  Returns the editStoryActivity1.
 	 * @uml.property  name="editStoryActivity1"
 	 */
 	public EditStoryActivity getEditStoryActivity1() {
 		return editStoryActivity1;
 	}
 
 	/**
 	 * Setter of the property <tt>editStoryActivity1</tt>
 	 * @param editStoryActivity1  The editStoryActivity1 to set.
 	 * @uml.property  name="editStoryActivity1"
 	 */
 	public void setEditStoryActivity1(EditStoryActivity editStoryActivity1) {
 		this.editStoryActivity1 = editStoryActivity1;
 	}
 
 	/**
 	 * @uml.property  name="editOptionActivity"
 	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="sController:com.example.adventurebook.EditOptionActivity"
 	 */
 	private EditOptionActivity editOptionActivity = new com.example.adventurebook.EditOptionActivity();
 
 	/**
 	 * Getter of the property <tt>editOptionActivity</tt>
 	 * @return  Returns the editOptionActivity.
 	 * @uml.property  name="editOptionActivity"
 	 */
 	public EditOptionActivity getEditOptionActivity() {
 		return editOptionActivity;
 	}
 
 	/**
 	 * Setter of the property <tt>editOptionActivity</tt>
 	 * @param editOptionActivity  The editOptionActivity to set.
 	 * @uml.property  name="editOptionActivity"
 	 */
 	public void setEditOptionActivity(EditOptionActivity editOptionActivity) {
 		this.editOptionActivity = editOptionActivity;
 	}
 
 	/**
 	 * @uml.property  name="takePhotoActivity"
 	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="sController:com.example.adventurebook.TakePhotoActivity"
 	 */
 	private TakePhotoActivity takePhotoActivity = new com.example.adventurebook.TakePhotoActivity();
 
 	/**
 	 * Getter of the property <tt>takePhotoActivity</tt>
 	 * @return  Returns the takePhotoActivity.
 	 * @uml.property  name="takePhotoActivity"
 	 */
 	public TakePhotoActivity getTakePhotoActivity() {
 		return takePhotoActivity;
 	}
 
 	/**
 	 * Setter of the property <tt>takePhotoActivity</tt>
 	 * @param takePhotoActivity  The takePhotoActivity to set.
 	 * @uml.property  name="takePhotoActivity"
 	 */
 	public void setTakePhotoActivity(TakePhotoActivity takePhotoActivity) {
 		this.takePhotoActivity = takePhotoActivity;
 	}
 
 	/**
 	 * @uml.property  name="annotationActivity"
 	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="sController:com.example.adventurebook.AnnotationActivity"
 	 */
 	private AnnotationActivity annotationActivity = new com.example.adventurebook.AnnotationActivity();
 
 	/**
 	 * Getter of the property <tt>annotationActivity</tt>
 	 * @return  Returns the annotationActivity.
 	 * @uml.property  name="annotationActivity"
 	 */
 	public AnnotationActivity getAnnotationActivity() {
 		return annotationActivity;
 	}
 
 	/**
 	 * Setter of the property <tt>annotationActivity</tt>
 	 * @param annotationActivity  The annotationActivity to set.
 	 * @uml.property  name="annotationActivity"
 	 */
 	public void setAnnotationActivity(AnnotationActivity annotationActivity) {
 		this.annotationActivity = annotationActivity;
 	}
 
 	/**
 	 * @uml.property  name="storyLibrary"
 	 * @uml.associationEnd  multiplicity="(1 1)" aggregation="shared" inverse="sController:com.example.adventurebook.StoryLibrary"
 	 */
	private StoryLibrary storyLibrary = null;
 
 	/**
 	 * Getter of the property <tt>storyLibrary</tt>
 	 * @return  Returns the storyLibrary.
 	 * @uml.property  name="storyLibrary"
 	 */
 	public StoryLibrary getStoryLibrary() {
 		return storyLibrary;
 	}
 
 	/**
 	 * Setter of the property <tt>storyLibrary</tt>
 	 * @param storyLibrary  The storyLibrary to set.
 	 * @uml.property  name="storyLibrary"
 	 */
 	public void setStoryLibrary(StoryLibrary storyLibrary) {
 		this.storyLibrary = storyLibrary;
 	}
 
 }
