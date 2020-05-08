 package tk.bnbm.clockdrive4j.view;
 
 import javafx.application.Application;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.stage.Stage;
 import javafx.stage.WindowEvent;
 
 /**
  * デバッグ用ダイアログ。
  * @author kazuhito_m
  */
 public class DebugForm extends Application {
 
 	/** デバッグ対象の「アプリケーションメイン画面」 */
 	protected MainView debugTarget;
 
 	/** サイズ等を弄うためのステージオブジェクト。 */
 	protected Stage self;
 
 	/**
 	 * コンストラクタで親画面を保存。
 	 * @param target 親画面。
 	 */
 	public DebugForm(MainView target) {
 		this.debugTarget = target;
 	}
 
 	/**
 	 * 画面開始。
 	 */
 	@Override
 	public void start(Stage stage) throws Exception {
 		// デバッグダイアログ用意。
		stage.setTitle("Debug Form");
 		Parent root = FXMLLoader.load(getClass().getResource("debugForm.fxml"));
 		Scene scene = new Scene(root);
 		stage.setScene(scene);
 		stage.setResizable(false);
 		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
 			public void handle(WindowEvent we) {
 				// コールバックメソッド登録。
 				debugTarget.endDebug();
 			}
 		});
 		stage.show();
 		self = stage;
 	}
 
 }
