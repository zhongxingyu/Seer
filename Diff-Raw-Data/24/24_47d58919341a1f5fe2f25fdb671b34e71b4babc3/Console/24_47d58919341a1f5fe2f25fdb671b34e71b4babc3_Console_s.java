 package guiFX;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ListIterator;
 
 import javafx.application.Application;
 import javafx.application.Platform;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.scene.Group;
 import javafx.scene.Scene;
 import javafx.scene.control.Button;
 import javafx.scene.control.ScrollBar;
 import javafx.scene.control.TextArea;
 import javafx.scene.control.TextField;
 import javafx.scene.input.KeyCode;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.StackPane;
 import javafx.scene.layout.VBox;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 import vty.VTYSession;
 
 class Console implements Runnable{
 	String ip;
 	String username;
 	String password;
 	ArrayList<String> cmds;
 	ListIterator itr;
 	
 	public Console(final String ip, final String username, final String password) {
 		System.out.println("New thread running with IP: " + ip);
 		
 		this.ip = ip;
 		this.username = username;
 		this.password = password;
 		cmds = new ArrayList<String>();
 		
 		Stage stage = new Stage();		
         stage.setTitle("Hello World!");
 		VBox root = new VBox();
 		final TextArea textArea = new TextArea();
 		final TextField textField = new TextField();
 		root.getChildren().addAll(textArea, textField);
 		textArea.setStyle("-fx-background-color: DARKGRAY;"
 				+ "-fx-text-fill: BLACK;"
 				+ "-fx-font-size: 14pt;");
 		textArea.setPrefSize(400, 310);
 		textArea.setEditable(false);
 		textArea.setWrapText(true);
 		
//		ScrollBar scrollBarv = (ScrollBar)textArea.lookup(".scroll-bar:vertical");
//		scrollBarv.setDisable(true);  
 
 		textField.setStyle("-fx-background-color: DARKGRAY;"
 				+ "-fx-text-fill: BLACK;"
 				+ "-fx-font-size: 14pt;");
 		textField.setEditable(true);
 		Platform.runLater(new Runnable() {
 	        @Override
 	        public void run() {
 	            textField.requestFocus();
 	        }
 	    });
 		
 		// event listeners
 		textArea.addEventHandler(MouseEvent.MOUSE_CLICKED, 
 				new EventHandler<MouseEvent>() {
 
 					@Override
 					public void handle(MouseEvent arg0) {
 			            textField.requestFocus();
 						
 					}
 		});
 		
 		textField.addEventHandler(KeyEvent.KEY_PRESSED, 
 				new EventHandler<KeyEvent>() {
 			public void handle(KeyEvent event) {
				
 				if(event.getCode() == KeyCode.ENTER) {
					VTYSession vty = new VTYSession();
					vty.openVTY(ip, username, password);
 					String cmd = textField.getText();
 					cmds.add(0, cmd);
 					if (cmd == null)
 						cmd = "";
 					String output = vty.write(cmd); // return a string with one or more lines
 					textArea.appendText(output+"\n");
 					textArea.positionCaret(textArea.getLength());
 					textField.setText(null);
 					itr = cmds.listIterator();
 				} 
 				
 				if(event.getCode() == KeyCode.UP) {
					VTYSession vty = new VTYSession();
					vty.openVTY(ip, username, password);
 					if(itr.hasNext())
 						textField.setText((String) itr.next());
 				} 
 				
 				if(event.getCode() == KeyCode.DOWN) {
					VTYSession vty = new VTYSession();
					vty.openVTY(ip, username, password);
 					if(itr.hasPrevious())
 						textField.setText((String) itr.previous());
 				} 
 			};
 		});
 
 		stage.setScene(new Scene(root, 400, 330));
 		stage.show();
 	}
 
 	@Override
 	public void run() {
 		System.out.println("Hello from thread");
 	}
 }
