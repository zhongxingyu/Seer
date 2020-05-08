 package jp.ac.oit.sclab.hoernchen.main;
 
 
 
 
 import java.io.IOException;
 import java.util.List;
 
 import javafx.application.Application;
 import javafx.event.EventHandler;
 import javafx.fxml.FXMLLoader;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.stage.Stage;
 
 
 
 public class LissMain001 extends Application {
 
 	private static final String fxmlFileName = "layout_example.fxml";
 	MainLayoutController mlController = null;
 
 
 
     @Override
     public void start(final Stage stage) throws IOException{
         // ステージのタイトルを設定
         stage.setTitle("Hello World!");
 
 
 
 
        stage.addEventHandler(KeyEvent.KEY_RELEASED,new EventHandler<KeyEvent>()  {
 
 			@Override
 			public void handle(KeyEvent arg0) {
 				// TODO 自動生成されたメソッド・スタブ
 				System.out.println("type : "+arg0.getEventType()+"  pressed : "+ arg0.getCode());
 				if(arg0.getCode() == KeyCode.ESCAPE)
 					System.exit(0);
 				if(arg0.isAltDown()&&arg0.getCode().equals(KeyCode.ENTER))
 					stage.setFullScreen( !stage.isFullScreen() );
 			}
 		});
 
 
         FXMLLoader fxmlLoader = new FXMLLoader();
 		fxmlLoader.setLocation(getClass().getClassLoader().getResource(fxmlFileName));
 
         Parent root = (Parent)fxmlLoader.load();
         mlController = (MainLayoutController)fxmlLoader.getController();
 
 
 
         List<Seat> seats = Seat.getSeatArrayList();
 
         int grade;
 
 
 
 
 
         for(Seat seat : seats){
 
         	grade = seat.getmStudent().getGrade();
 
         	switch(grade){
 
         		case Student.GRADE_3RD:
 
         			mlController.setSeminar3Membar(new StudentButton(stage, seat));
 
         			break;
 
         		case Student.GRADE_4TH:
 
         			mlController.setSeminar4Membar(new StudentButton(stage,seat));
 
         			break;
 
         		case Student.GRADE_MASTER:
         			mlController.setMasterMembar(new StudentButton(stage,seat));
         			break;
 
         		case Student.GRADE_ETC:
         			mlController.setOtherMembar(new StudentButton(stage, seat));
         			break;
 
         		default:
         			break;
 
 
 
 
         	}
 
 
 
 
 
 
 
 
 
 
 
 
 
         }
 
 
 
 
 
 
 
 
 
 
 
         Button b = new Button("TEST");
         mlController.setOtherMembar(b);
 
 
         // シーンの生成
         Scene scene = new Scene(root);
 
         // シーンをステージに貼る
         stage.setScene(scene);
         stage.setFullScreen(true);
         stage.setResizable(false);
 
 
 
         // ステージの表示
         stage.show();
     }
 
 	public static void main(String[] args){
 
 		launch(args);
 	}
 
 
 }
