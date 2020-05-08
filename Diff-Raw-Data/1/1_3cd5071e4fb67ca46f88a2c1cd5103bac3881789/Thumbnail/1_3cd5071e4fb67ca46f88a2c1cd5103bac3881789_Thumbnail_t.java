 package removeImage;
 
 import javafx.application.Platform;
 import javafx.concurrent.Task;
 import javafx.event.EventHandler;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.Cursor;
 import javafx.scene.control.CheckBox;
 import javafx.scene.effect.Glow;
 import javafx.scene.image.Image;
 import javafx.scene.image.ImageView;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.StackPane;
 import javafx.scene.shape.Rectangle;
 
 /**
  * 
  * @author Johan LG
  */
 public class Thumbnail extends StackPane {
 
     private ImageView imageView;
     private CheckBox cb;
     private Image image;
     private String url;
 
     public Thumbnail() {
         setPrefSize(150, 150);
         Rectangle frame = new Rectangle(152, 152);
         getChildren().add(frame);
         imageView = new ImageView();
 
 
         setOnMousePressed(new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent evt) {
                 cb.setSelected(!cb.isSelected());
             }
         });
         setOnMouseEntered(new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent evt) {
                 imageView.setEffect(new Glow(.5));
             }
         });
         setOnMouseExited(new EventHandler<MouseEvent>() {
             @Override
             public void handle(MouseEvent evt) {
                 imageView.setEffect(null);
             }
         });
         cb = new CheckBox();
 
         setAlignment(cb, Pos.TOP_RIGHT);
         setMargin(cb, new Insets(8, 8, 8, 8));
         getChildren().addAll(imageView, cb);
         setCursor(Cursor.HAND);
     }
 
     public boolean isSelected(){
         return cb.isSelected();
     }
     
     public void setSelected(boolean selected){
         cb.setSelected(selected);
     }
     
     public void loadImage(final String pic) {
         url = pic;
         Thread t = new Thread(new Task<Void>() {
             @Override
             protected Void call() throws Exception {
                 Platform.runLater(new Runnable() {
                     @Override
                     public void run() {
                         image = new Image(pic);
                         imageView.setImage(image);
                     }
                 });
                 return null;
             }
         });
         t.start();
     }
 
     public String getUrl() {
         return url;
     }
 }
