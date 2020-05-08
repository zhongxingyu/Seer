 package interiores.presentation.terminal.commands;
 
 import interiores.business.controllers.RoomController;
 import interiores.business.exceptions.ElementNotFoundBusinessException;
 import interiores.business.exceptions.NoRoomCreatedException;
import interiores.core.business.BusinessException;
 import interiores.core.presentation.terminal.CommandGroup;
 import javax.xml.bind.JAXBException;
 
 /**
  *
  * @author hector
  */
 public class RoomCommands extends CommandGroup
 {
     private RoomController roomController;
     
     public RoomCommands(RoomController roomController) {
         super();
         
         this.roomController = roomController;
     }
     
     public void _new()
            throws ElementNotFoundBusinessException, BusinessException
     {
         String type = readString("Enter the name of the room type you want to create:");
         int width = readInt("Enter the width of the room in centimeters:");
         int depth = readInt("Enter the depth of the room in centimeters:");
         
         roomController.create(type, width, depth);
     }
     
     public void save()
             throws JAXBException, NoRoomCreatedException
     {
         String path = readString("Especifica la ruta donde guardar la habitación");
         
         roomController.save(path);
     }
     
     public void load()
             throws JAXBException
     {
         String path = readString("Especifica la ruta desde donde cargar");
         
         roomController.load(path);
     }
 }
