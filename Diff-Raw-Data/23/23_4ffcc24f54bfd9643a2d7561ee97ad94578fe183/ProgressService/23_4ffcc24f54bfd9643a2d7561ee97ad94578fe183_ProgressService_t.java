 package view.util;
 
 import javafx.concurrent.Service;
 import javafx.concurrent.Task;
 import javafx.stage.Stage;
 
 /**
  * This abstract class is a service that shows a progress dialog while running.
  */
 public abstract class ProgressService extends Service<String> {
 
 	private Stage primaryStage;
	private boolean success;
 
 	public ProgressService(Stage primaryStage) {
 		super();
 		this.primaryStage = primaryStage;
 	}
 
 	@Override
 	public void start() {
 		super.start();
 		new ProgressDialog(this, primaryStage).showDialog();
 	}
 
	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean getSuccess() {
		return success;
	}

 	@Override
 	protected abstract Task<String> createTask();
 
 }
