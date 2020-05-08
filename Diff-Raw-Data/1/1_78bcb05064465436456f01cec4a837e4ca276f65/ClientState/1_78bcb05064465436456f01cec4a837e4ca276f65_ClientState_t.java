 /**
  * 
  */
 package ch.ethz.e4mooc.client;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ch.ethz.e4mooc.shared.ProjectModelDTO;
 
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.user.client.Window;
 import com.google.web.bindery.event.shared.EventBus;
 
 /**
  * This class holds client state.
  * 
  * @author hce
  *
  */
 public class ClientState {
 
 	/** the name of the current project */
 	private String currentProjectName;
 	
 	/** the project model of the current project */
 	private ProjectModelDTO projectModel;
 	/** maps tab indexes to file names */
 	private Map<Integer, String> tabIndexToFileNameMap;
 	/** the event bus used throughout the application */
 	private EventBus eventBus;
 	/** the text that is currently shown in the editor */
 	private String inputFile;
 	
 	/** a map that stores the content of files as they were modified by the user */
 	private Map<String, String> userStorage;
 	
 	/**
 	 * the following fields are about storing Uri parameters
 	 */
 	/** the id of the user */
 	private String userId;
 	/** the group-id of the user */
 	private String groupId;
 	/** the height of the output box that shows the result */
 	private int outputBoxHeight;
 	/** the color of the background */
 	private String backgroundColor;
 	
 	/**
 	 * 
 	 * @param eventBus the eventBus used throughout the application
 	 */
 	public ClientState(EventBus eventBus) {
 		this.eventBus = eventBus;
 		tabIndexToFileNameMap = new HashMap<Integer, String>();
 		userStorage = new HashMap<String, String>();
 		
 		// initialize default values for user properties
 		userId = "";
		groupId = "";
 		outputBoxHeight = 250;
 		backgroundColor = "#FFFFFF"; // set it to white
 		
 		bind();
 	}
 	
 	
 	private void bind() {
 		
 		// on closing the browser window
 		Window.addCloseHandler(new CloseHandler<Window>() {
 			
 			@Override
 			public void onClose(CloseEvent<Window> event) {
 				// before the browser window is closed, store the latest changes of the currently visible tab
 				//storeYourCodeToLocalStorage();
 			}
 		});		
 	}
 	
 	
 	/**
 	 * Stores a project model in the ClientState.
 	 * @param pm the project model
 	 */
 	public void storeProjectModel(ProjectModelDTO pm) {
 		// store the project model
 		this.projectModel = pm;
 		
 		// clear out the user-storage (in case the new PM has a file with the same name)
 		userStorage.clear();
 		
 		// counter for the tab index
 		int counter = 0;
 		
 		// map the files of the project to tab indexes
 		for(String fileName: pm.getFileNames()) {
 			tabIndexToFileNameMap.put(counter, fileName);
 			counter++;
 		}
 	}
 
 	/**
 	 * Returns the name of the project.
 	 * @return the project name
 	 */
 	public String getProjectName() {
 		return projectModel.getProjectName();
 	}
 	
 	/**
 	 * Returns a list of all the file names in the project.
 	 * @return list of file names
 	 */
 	public List<String> getProjectFileNames() {
 		return projectModel.getFileNames();
 	}
 	
 
 	/**
 	 * Returns the content of a file based on the file name.
 	 * @param fileName the file name
 	 * @return the content of the file
 	 */
 	public String getContentOfFile(String fileName) {
 		
 		String result = "";
 	
 		if(userStorage.containsKey(fileName))
 			result = userStorage.get(fileName);
 		else
 			result = projectModel.getFileContent(fileName);
 		
 		return result;
 	}
 	
 	
 	/**
 	 * Returns the content of a file based on the tab index used by the editor.
 	 * @param tabIndex the tab index
 	 * @return the content of the file that belongs to the tab index
 	 */
 	public String getContentOfFile(int tabIndex) {
 				
 		String result = "";
 		
 		String fileName = tabIndexToFileNameMap.get(tabIndex);
 		
 		if(userStorage.containsKey(fileName)) {
 			// get the version from the local storage
 			result = userStorage.get(fileName);
 		} else {
 			result = projectModel.getFileContent(fileName);
 		}
 		return result;
 	}
 	
 	
 	/**
 	 * Returns a map with all the files. Files include the changes by users.
 	 * @return map of all files
 	 */
 	public HashMap<String, String> getContentOfAllFiles() {
 		HashMap<String, String> result = new HashMap<String, String>();
 		
 		for(String fileName: this.getProjectFileNames()) {
 			result.put(fileName, this.getContentOfFile(fileName));
 		}
 		return result;
 	}
 
 	
 	/**
 	 * Stores the given text into session storage, if that's supported by the browser.
 	 * @param tabIndex the index of the tab that should be stored (is used as port of the KEY)
 	 * @param text the text to store
 	 */
 	public void storeTextFromUser(int tabIndex, String text) {
 		String fileName = tabIndexToFileNameMap.get(tabIndex);
 		userStorage.put(fileName, text);
 	}
 
 	
 	/**
 	 * Removes the user version for the given tab index from the storage.
 	 * @param tabIndex the tab index of the file for which to delete the entry
 	 */
 	public void deleteFromStorage(int tabIndex) {	
 		String fileName = tabIndexToFileNameMap.get(tabIndex);
 		
 		if(userStorage.containsKey(fileName))
 			userStorage.remove(fileName);	
 	}
 	
 	
 	/**
 	 * Returns the id of the user who's using the current project.
 	 * @return empty string for unknown users; otherwise the user id; 
 	 */
 	public String getUserId() {
 		return userId;
 	}
 	
 	
 	/**
 	 * Stores the given id in the client state.
 	 * @param userId id of a user
 	 */
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	
 	/**
 	 * Returns the group-id of the user who's using the current project.
 	 * @return empty string for unknown users; otherwise the user id; 
 	 */
 	public String getUserGroupId() {
 		return groupId;
 	}
 	
 	
 	/**
 	 * Stores the given group-id in the client state.
 	 * @param userId id of a user
 	 */
 	public void setUserGroupId(String groupId) {
 		this.groupId = groupId;
 	}
 	
 	
 	/**
 	 * Returns the user's preferred height for the output box.
 	 * @return value of preferred height or a default value
 	 */
 	public int getUserOutputBoxHeight() {
 		return outputBoxHeight;
 	}
 	
 	
 	/**
 	 * Set the user's preferred height for the output box.
 	 * @param outputBoxHeight height value
 	 */
 	public void setUserOutputBoxHeight(int outputBoxHeight) {
 		this.outputBoxHeight = outputBoxHeight;
 	}
 	
 	/**
 	 * Returns the user's preferred background color.
 	 * @return hex-formated color; by default that's white (#FFFFFF)
 	 */
 	public String getUserBackgroundColor() {
 		return this.backgroundColor;
 	}
 	
 	/**
 	 * Set the user's preferred background color
 	 * @param backgroundColor a color formated in hex-format (e.g.#FFFFFF)
 	 */
 	public void setUserBackgroundColor(String backgroundColor) {
 		this.backgroundColor = backgroundColor;
 	}
 }
