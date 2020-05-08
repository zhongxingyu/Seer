 package daifuku;
 
 import java.util.ResourceBundle;
import daifuku.swing.MainInteraction;
 
 /**
  * Creates an Interaction given a Context.
  * This is an abstract class.
  * The Interactions are implemented separately from the Context.  This
  * is for two reasons.  First, it creates a separation from the business
  * logic of the application (which exists in the Context) and the
  * actual GUI (which exists in the Interaction).  Secondly, it allows
  * us to implement the GUI using more than one widget library without
  * changing the business logic.  Unfortunately, creating this separation
  * means that we need to instantiate the Interactions separately from
  * the Contexts. 
  *
  * The InteractionFactory also currently acts as a place to hold
  * string resources for all the Contexts.
  * TODO: It makes some sense to put it here, but something smells bad. 
  * 
  * @author Mike Charlton
  *
  */
 public abstract class InteractionFactory {
 		
 	private ResourceBundle strings;
 
     /**
      * Bundle resource class name for strings in the application.
      * Overload this method to load the correct strings for your application
      */
     public String stringsBundleName() {
         return "daifuku.Strings";
     }
 
     /** 
      * Load the string resources for these Contexts.
      */
 	public void loadStrings() {
 		strings = ResourceBundle.getBundle(stringsBundleName());
 	}
 	
     /**
      * Return the correct string for a given key
      */
 	public String getString(String key) {
 		return strings.getString(key);
 	}
 	 
 	/**
      * Returns true if the Interactions should display on the screen.
 	 * There are times (when testing for instance) when we don't 
      * want to display the UI to the user.  This method should be 
      * called by open() on the interaction before displaying the UI.
 	 * @return true if the UI should be shown.
 	 */
 	public abstract boolean showUI();
 	
	public Main.Interaction create_interaction(Main context) {
        return new MainInteraction(context, this);
    }
 }
