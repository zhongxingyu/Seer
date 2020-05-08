 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package emergencyroomfx;
 
 import emergencyroom.TreatmentRoom;
 import javafx.scene.control.Button;
 import javafx.scene.control.ListCell;
 
 /**
  *
  * @author es.carlsten
  */
 public class TreatmentRoomListCell extends ListCell<TreatmentRoom> {
     
     public TreatmentRoomListCell()
     {}
     
     @Override
     public void updateItem(TreatmentRoom item, boolean empty)
     {
         super.updateItem(item, empty);
         
         /*
         String label = "ID: " + room.getPatient().getPriority();
         
         Button nodeGraphic = new Button(label);
         
         setGraphic(nodeGraphic);
         */
         
         /*
         Rectangle rect = new Rectangle(100, 100);
         if (item != null)
         {
             rect.setFill(Color.CHOCOLATE);
             setGraphic(rect);
         }
         */
         
         if (item != null)
         {
             String label = "ID: " + item.getPatient().getPriority();
            Button button = new Button(label);
         
             setGraphic(button);
         }
     }
     
 }
