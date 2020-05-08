 /**
  * 
  */
 package framework;
 
 import java.util.ArrayList;
 
 import java.beans.PropertyChangeListener;
 
 import app.DisplayPanel;
 import app.RecognisedResource;
 
 /**
  * @author Ben Griffiths
  *
  */
 public interface UserInterface extends PropertyChangeListener {
 	public void createAndShow();
 	public void updateResults(String resultsMessage);
 	public void solveClue();
 	public void findSolutions();
 	public DisplayPanel getDisplayPanel();
 	public void getChosenEntitiesFromUser(ArrayList<RecognisedResource> recognisedResources);
 	public void showNewClueOptions();
 }
