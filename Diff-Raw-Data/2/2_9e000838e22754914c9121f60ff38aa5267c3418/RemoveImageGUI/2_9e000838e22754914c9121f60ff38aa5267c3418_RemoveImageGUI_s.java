 package removeImage;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.geometry.HPos;
 import javafx.geometry.Insets;
 import javafx.geometry.Pos;
 import javafx.scene.control.Button;
 import javafx.scene.control.ScrollPane;
 import javafx.scene.layout.ColumnConstraints;
 import javafx.scene.layout.FlowPane;
 import javafx.scene.layout.GridPane;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Priority;
 import repository.DeletePicturesCom;
 
 /**
  *
  * @author Johan LG
  */
 public class RemoveImageGUI extends GridPane {
 
     private final ArrayList<Thumbnail> thumbnails;
     private final ThumbnailLoader pl;
     private final static int imagePerPane = 24;
     private int thumbIndex = 0, maxImages = 0, rem = 0;
     private FlowPane grid;
     private Button next, previous;
     private SelectedThumbnailLister lister;
 
     public RemoveImageGUI() {
         thumbnails = new ArrayList<>();
         lister = new SelectedThumbnailLister(thumbnails);
         int gap = 8;
         setHgap(gap);
         setVgap(gap);
         setPadding(new Insets(gap));
         ScrollPane scroll = new ScrollPane();
         setVgrow(scroll, Priority.ALWAYS);
         ColumnConstraints cc = new ColumnConstraints();
         cc.setHgrow(Priority.ALWAYS);
         getColumnConstraints().add(cc);
         scroll.setFitToWidth(true);
         grid = new FlowPane();
         grid.setPadding(new Insets(8));
         grid.setVgap(gap);
         grid.setHgap(gap);
 
         grid.setAlignment(Pos.CENTER);
 
         scroll.setContent(grid);
         add(scroll, 0, 0, 4, 1);
 
         Button mark = new Button("Merk alle");
         Button unmark = new Button("Avmerk alle");
         Button delete = new Button("Slett merkede");
         previous = new Button("< Forrige");
         next = new Button("Neste >");
         Button markPage = new Button("Merk alle på siden");
         Button unmarkPage = new Button("Avmerk alle på siden");
 
         mark.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 for (Thumbnail thumbnail : thumbnails) {
                     thumbnail.setSelected(true);
                 }
             }
         });
         unmark.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 for (Thumbnail thumbnail : thumbnails) {
                     thumbnail.setSelected(false);
                 }
             }
         });
         delete.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 DeletePicturesCom delCom = new DeletePicturesCom();
                 try {
                     delCom.deletePictures(lister.ListSelectedThumbnails());
                 } catch (IOException ex) {
                     Logger.getLogger(RemoveImageGUI.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 ArrayList<Thumbnail> selected = new ArrayList();
                 for (Thumbnail thumbnail : thumbnails) {
                     if (thumbnail.isSelected()) {
                         selected.add(thumbnail);
                     }
                 }
                 thumbnails.removeAll(selected);
                 updateGrid();
             }
         });
         next.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 showNext();
             }
         });
 
         previous.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 showPrevious();
             }
         });
         previous.setDisable(true);
         markPage.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 int range = imagePerPane;
                 if (maxImages == thumbIndex) {
                     range = rem;
                 }
                 for (int i = thumbIndex; i < thumbIndex + range; i++) {
                     thumbnails.get(i).setSelected(true);
                 }
             }
         });
         unmarkPage.setOnAction(new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 int range = imagePerPane;
                 if (maxImages == thumbIndex) {
                     range = rem;
                 }
                 for (int i = thumbIndex; i < thumbIndex + range; i++) {
                     thumbnails.get(i).setSelected(false);
                 }
             }
         });
 
         HBox pageMarkBox = new HBox(gap);
         pageMarkBox.getChildren().addAll(previous, markPage, unmarkPage, next);
         pageMarkBox.setAlignment(Pos.CENTER);
         add(pageMarkBox, 0, 1, 3, 1);
 
         HBox markBox = new HBox(gap);
         markBox.getChildren().addAll(mark, unmark);
         markBox.setAlignment(Pos.CENTER);
         add(markBox, 0, 2, 3, 1);
 
         setHalignment(delete, HPos.RIGHT);
         add(delete, 2, 2);
 
         pl = new ThumbnailLoader(thumbnails);
 
         maxImages = pl.imageListSize();
 
        if (maxImages < imagePerPane) {
             next.setDisable(true);
         }
         //temp
         for (int i = 0; i < maxImages; i++) {
             Thumbnail tn = new Thumbnail();
             thumbnails.add(tn);
         }
 
         rem = maxImages % imagePerPane;
         maxImages -= rem;
 
         if (maxImages > imagePerPane) {
             for (int i = 0; i < imagePerPane; i++) {
                 grid.getChildren().add(thumbnails.get(i));
             }
         } else {
             grid.getChildren().addAll(thumbnails);
         }
 
         pl.addPictures(thumbnails);
         pl.loadPictures(thumbnails, 0, imagePerPane * 2);
 
         thumbIndex = 0;
 
         GridPane.setHgrow(this, Priority.ALWAYS);
         GridPane.setVgrow(this, Priority.ALWAYS);
 
     }
 
     private void showNext() {
         thumbIndex += imagePerPane;
         if (maxImages >= imagePerPane) {
             grid.getChildren().clear();
             if (thumbIndex == maxImages) {
                 for (int i = thumbIndex; i < (maxImages + rem); i++) {
                     previous.setDisable(false);
                     grid.getChildren().add(thumbnails.get(i));
                 }
                 next.setDisable(true);
             } else {
                 for (int i = thumbIndex; i < (thumbIndex + imagePerPane); i++) {
                     previous.setDisable(false);
                     grid.getChildren().add(thumbnails.get(i));
                 }
             }
 
 
             if (thumbIndex > thumbnails.size() - 1) {
                 next.setDisable(true);
             }
             //Loading the pictures for the next page
             pl.loadPictures(thumbnails, thumbIndex + imagePerPane, imagePerPane);
         }
     }
 
     private void showPrevious() {
         thumbIndex -= imagePerPane;
         grid.getChildren().clear();
         for (int i = thumbIndex; i < ((thumbIndex) + imagePerPane); i++) {
             next.setDisable(false);
             grid.getChildren().add(thumbnails.get(i));
         }
         if (thumbIndex == 0) {
             previous.setDisable(true);
         }
     }
 
     private void updateGrid() {
         maxImages = thumbnails.size();
 
         grid.getChildren().clear();
         rem = maxImages % imagePerPane;
         maxImages -= rem;
         if (thumbIndex == maxImages) {
             if (rem == 0 && thumbIndex != 0) {
                 showPrevious();
                 next.setDisable(true);
 
                 return;
             }
             for (int i = maxImages; i < (maxImages + rem); i++) {
                 grid.getChildren().add(thumbnails.get(i));
             }
             next.setDisable(true);
 
         } else {
             for (int i = thumbIndex; i < (thumbIndex + imagePerPane); i++) {
                 grid.getChildren().add(thumbnails.get(i));
                 thumbnails.get(i).loadImage();
             }
         }
     }
 }
