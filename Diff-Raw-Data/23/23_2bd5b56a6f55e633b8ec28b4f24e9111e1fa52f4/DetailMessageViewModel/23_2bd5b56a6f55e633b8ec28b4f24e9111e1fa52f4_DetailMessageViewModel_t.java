 package ch.inftec.ju.fx;
 
 import ch.inftec.ju.util.JuStringUtils;
 import javafx.beans.property.SimpleStringProperty;
 import javafx.beans.property.StringProperty;
 
 /**
  * ViewModel that contains a message a long with a mode detailed message
  * (like a stack trace) to be displayed to the user.
  * @author Martin
  *
  */
 public class DetailMessageViewModel {
 	private StringProperty title = new SimpleStringProperty();
 	private StringProperty message = new SimpleStringProperty();
 	private StringProperty detailedMessage = new SimpleStringProperty();
 	
 	public StringProperty titleProperty() {
 		return this.title;
 	}
 	
 	public StringProperty messageProperty() {
 		return this.message;
 	}
 	
 	public StringProperty detailedMessageProperty() {
 		return this.detailedMessage;
 	}
 	
 	/**
 	 * Creates a model for a Throwable, containing the throwable message
 	 * and the stack trace as a detailed message.
 	 * @param ex Throwable
 	 * @return Model
 	 */
 	public static DetailMessageViewModel createByThrowable(Throwable ex) {
 		DetailMessageViewModel model = new DetailMessageViewModel();
		if (ex != null) {
			model.titleProperty().set(ex.getClass().getSimpleName());
			model.messageProperty().set(ex.getMessage());
			model.detailedMessageProperty().set(JuStringUtils.getStackTrace(ex));
		} else {
			model.title.set("Exception");
			model.messageProperty().set("No Exception details available");
		}
 		return model;
 	}
 }
