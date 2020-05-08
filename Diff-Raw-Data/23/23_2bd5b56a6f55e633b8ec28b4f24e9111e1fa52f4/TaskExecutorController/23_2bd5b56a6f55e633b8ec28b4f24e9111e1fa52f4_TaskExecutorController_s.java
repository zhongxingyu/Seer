 package ch.inftec.ju.fx.concurrent;
 
 import javafx.concurrent.Task;
 import javafx.concurrent.WorkerStateEvent;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.scene.control.Hyperlink;
 import javafx.scene.control.Label;
 import javafx.scene.control.ProgressBar;
 import ch.inftec.ju.util.AssertUtil;
 import ch.inftec.ju.util.fx.JuFxUtils;
 
 /**
  * Controller class that can be used to execute a JavaFX class providing
  * visual feedback (and cancellation option) on a Pane.
  * @author tgdmemae
  *
  */
 public class TaskExecutorController {
 	@FXML private Label txtTitle;
 	@FXML private Label txtMessage;
 	@FXML private ProgressBar pbProgress;
 	@FXML private Hyperlink hlCancel;
 	@FXML private Hyperlink hlError;
 	
 	private TaskExecutorViewModel model;
 	
 	/**
 	 * Executes the specified task in this controller.
 	 * @param task Task that hasn't been started yet.
 	 * @param doneEventHandlerFx EventHandler that is called when the task is done (either cancelled or successfully completed).
 	 * The handler will run in the FX application thread
 	 */
 	public void executeTask(final Task<?> task, EventHandler<WorkerStateEvent> doneEventHandler) {
 		AssertUtil.assertNull("Controller supports only one task execution", this.model);
 		this.initModel(task, doneEventHandler);
 	}
 	
 	private void initModel(final Task<?> task, final EventHandler<WorkerStateEvent> doneEventHandler) {
 		// Make sure the model is initialized in the FX thread, otherwise
 		// the Task will complain...
 		
 		JuFxUtils.runInFxThread(new Runnable() {
 			@Override
 			public void run() {
 				// Not working, results in java.lang.OutOfMemoryError: Java heap space
 //				AssertUtil.assertEquals(State.READY, task.getState());
 				
 				model = new TaskExecutorViewModel(task);
 				model.setOnDone(doneEventHandler);
 				
 				
 				txtTitle.textProperty().bind(model.titleProperty());
 				txtMessage.textProperty().bind(model.messageProperty());
 				
 				pbProgress.progressProperty().bind(model.progressProperty());
 
 				hlError.visibleProperty().bind(model.exceptionProperty().isNotNull());
 				hlCancel.visibleProperty().bind(model.cancelEnabledProperty());
 				
 				// Run the task
 				model.start();
 			}
 		});
 	}
 	
 	public void cancel(ActionEvent ev) {
 		model.cancel();
 	}
 	
 	public void showError(ActionEvent ev) {
		// Show Error
		System.err.println("Error");
 	}
 }
