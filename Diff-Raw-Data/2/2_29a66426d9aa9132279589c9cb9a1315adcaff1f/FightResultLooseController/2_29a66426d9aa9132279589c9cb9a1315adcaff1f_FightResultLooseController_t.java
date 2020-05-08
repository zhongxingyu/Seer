 package brutes.client.gui.result;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.event.ActionEvent;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.Node;
 import javafx.scene.text.Text;
 import javafx.stage.Stage;
 
 /**
  * FXML Controller class
  *
  * @author Karl
  */
 public class FightResultLooseController implements Initializable {
     private final String[] sentences = {
         "On est rarement plus modeste après une défaite.", 
         "La honte est dans la fuite, non dans la défaite.", 
        "Il y a des défaites triomphantes.", 
         "On apprend plus de ses propres défaites que des défaites des autres.", 
         "Qui ira donc compter les batailles perdues - Le jour de la victoire ?", 
         "La vérité ne vainc que dans la défaite.", 
         "Encore une victoire comme celle-là et nous sommes perdus.", 
         "O Mort où est la victoire ?", 
         "Défaite, déconfiture et pas de pot."
     };
     
     @FXML
     private Text text;
     
     @FXML
     private void handleContinueAction(ActionEvent e){
         Node source = (Node) e.getSource(); 
         Stage stage = (Stage) source.getScene().getWindow();
         stage.close();
     }
 
     /**
      * Initializes the controller class.
      */
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         this.text.setText(this.sentences[(int)(Math.random() * this.sentences.length)]);
     }    
 }
