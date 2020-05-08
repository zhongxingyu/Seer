 package org.aldeon.gui.controllers;
 
 import javafx.fxml.Initializable;
 import javafx.geometry.Insets;
 import javafx.scene.Parent;
 import javafx.scene.control.Button;
 import javafx.scene.control.Separator;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.layout.HBox;
 import javafx.scene.layout.Pane;
 import javafx.scene.shape.Rectangle;
 import javafx.scene.text.Text;
 import org.aldeon.core.CoreModule;
 import org.aldeon.core.events.UserAddedEvent;
 import org.aldeon.crypt.Key;
 import org.aldeon.gui.colors.ColorManager;
 import org.aldeon.model.Identity;
 import org.aldeon.model.Message;
 import org.aldeon.model.User;
 import org.aldeon.model.UserImpl;
 
 import java.net.URL;
 import java.util.Map;
 import java.util.ResourceBundle;
 
 /**
  *
  */
 public class ResponseController implements Initializable {
     public Text message;
     public Rectangle backgroundRectangle;
     public Rectangle colorRectangle;
     public Pane respPane;
     public HBox windowContainer;
     public Separator separator;
     public Button showHide;
     public Text msgHash;
 
     public Parent toPass;
     public Text auth;
     public Text pubKey;
 
     private Key author;
     private int nestingLevel;
     private Message msg;
     private boolean showChildren = false;
 
     private String weirdText = "1. Przecz skrżytało pogaństwo a ludzie myślili są proz? \n" +
             "2. Przystajali są krolowie ziemszczy a książęta seszli są sie na gromadę przeciwo Gospodnu i przeciwo \n" +
             "jego pomazańcu. \n" +
             "3. Roztargajmy jich przekowy i srzucimy s nas jarzmo jich. \n" +
             "4. Jen przebywa na niebiesiech, pośmieje sie jim, i Gospodzin zwala śmiech w nich. \n" +
             "5. Tegdy mołwić będzie k nim w gniewie swojem i w rosierdziu swojem zamąci je.  2\n" +
             "6. Ale ja postawion jeśm krol od niego na Syjon, gorze świętej jego, \n" +
             "przepowiadaję kaźń jego. \n" +
             "7. Gospodzin rzekł ku mnie: Syn moj jeś ty, ja dzisia porodził jeśm cie. \n" +
             "8. Pożędaj ote mnie, i dam ci pogany w dziedzicstwo twoje i w trzymanie twoje kraje ziemskie. \n" +
             "9. Włodać będziesz nad nimi w mietle żelaznej a jako ssąd zdunowy \n" +
             "rozbijesz je. \n" +
             "10. A już, krolowie, rozumiejcie, nauczcie sie, cso sądzicie ziemię. \n" +
             "11. Służycie Bogu w strasze i wiesielcie sie jemu se drżenim. \n" +
             "12. Przyjmicie pokaźnienie, bo snadź rozgniewa sie Gospodzin, i \n" +
             "zginiecie z drogi prawej. \n" +
             "13. Gdy rozżgą na krotce gniew jego, błogosławieni wszystcy, jiż imają w niem pwę";
 
     public Message getMsg() { return msg; }
 
     private static final double initialHeight = 132;
 
     ResponseControlListener listener;
 
     public void deleteClicked(MouseEvent event) {
         if (listener != null) listener.responseDeleteClicked(toPass, this);
     }
 
     public void respondClicked(MouseEvent event) {
         if (listener != null) listener.responseClicked(this, message.getText());
     }
 
     public void writeResponseClicked(MouseEvent event) {
         if (listener != null) listener.responseRespondClicked(toPass, this, nestingLevel);
     }
 
     public void showChildren() {
         if (!showChildren) {
             showHide.setText("Hide");
             if (listener != null) listener.responseShowClicked(toPass, this);
             showChildren = !showChildren;
         }
     }
 
     public void hideChildren() {
         if (showChildren) {
             showHide.setText("Show");
             if (listener != null) listener.responseHideClicked(toPass, this);
             showChildren = !showChildren;
         }
     }
 
     public void showHideClicked(MouseEvent event) {
         if (!showChildren) {
             showChildren();
         } else {
             hideChildren();
         }
     }
 
     public void mouseOnIcon(MouseEvent event) {
     }
 
     public void registerListener(ResponseControlListener listener) {
         this.listener = listener;
     }
 
     public void setHasChildren(boolean hidden) {
         this.showHide.setVisible(true);
         showChildren = hidden;
 
         if (hidden == true) {
             showHide.setText("Hide");
         } else {
             showHide.setText("Show");
         }
     }
 
     public void setMessage(Message msg, int nestingLevel) {
         this.showHide.setVisible(false);
         this.msg = msg;
         this.message.setText(msg.getContent());
         this.msgHash.setText(msg.getIdentifier().toString());
         this.nestingLevel = nestingLevel;
         User als=CoreModule.getInstance().getUserManager().getIdentity(msg.getAuthorPublicKey());
         if(als!=null) this.auth.setText(als.getName());
         else{
             als = CoreModule.getInstance().getUserManager().getUser(msg.getAuthorPublicKey());
             if(als!=null) this.auth.setText(als.getName());
             else {
                System.out.println("MAM TAKIE: ");
                for(int key : CoreModule.getInstance().getUserManager().getAllUsers().keySet())
                    System.out.println("KLUCZ: "+key);
                System.out.println("SZUKAM: "+msg.getAuthorPublicKey().hashCode());
                 CoreModule.getInstance().getEventLoop().notify(new UserAddedEvent(UserImpl.create("Anonymous",msg.getAuthorPublicKey())));
                 this.auth.setText("Anonymous");
             }
         }
         this.pubKey.setText(msg.getAuthorPublicKey().toString().substring(0,16) + "...");
         //borderPane.prefWidthProperty().bind(root.widthProperty());
         //respPane.prefHeightProperty().bind(colorRectangle.heightProperty());
         //colorRectangle.heightProperty().bind(respPane.prefHeightProperty());
         //respPane.prefHeightProperty().bindBidirectional(colorRectangle.heightProperty());
         backgroundRectangle.setHeight(
                 Math.max(message.layoutBoundsProperty().get().getHeight()
                         + message.getLayoutY() + 70,
                         initialHeight));
         colorRectangle.setHeight(
                 backgroundRectangle.getHeight()-2);
         respPane.setPrefHeight(colorRectangle.getHeight() + 30);
         respPane.prefWidthProperty().bindBidirectional(windowContainer.prefWidthProperty());
         //separator.prefWidthProperty().bindBidirectional(windowContainer.prefWidthProperty());
         separator.prefHeightProperty().bindBidirectional(colorRectangle.heightProperty());
         colorRectangle.setFill(ColorManager.getColorForKey(msg.getAuthorPublicKey()));
         windowContainer.setPadding(new Insets(0,10,0,35 * nestingLevel)); //top right bottom left
     }
 
     @Override
     public void initialize(URL url, ResourceBundle resourceBundle) {
         //initialHeight = backgroundRectangle.getHeight();
     }
 }
 
 
