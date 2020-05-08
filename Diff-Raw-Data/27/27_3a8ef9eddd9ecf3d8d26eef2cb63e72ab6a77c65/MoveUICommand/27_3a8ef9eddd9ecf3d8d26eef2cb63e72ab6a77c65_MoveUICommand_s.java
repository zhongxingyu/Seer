 package pl.edu.agh.two.mud.server.command;
 
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.annotation.Alias;
 import pl.edu.agh.two.mud.common.command.annotation.OrderedParam;
 import pl.edu.agh.two.mud.server.world.model.Direction;
 
 import static pl.edu.agh.two.mud.server.world.model.Direction.*;
 
 @Alias({"idz", "i"})
 public class MoveUICommand extends UICommand {
 
     @OrderedParam(1)
     private String direction;
 
     @Override
     public String getDescription() {
         return "Komenda przejscia na inne pole. Jej uzycie spowoduje przejscie na inne pole.\n"
                 + "\t idz [n|s|e|w]";
     }
 
     public Direction getDirection() {
        if (direction.equals("n")) {
             return N;
        } else if (direction.equals("s")) {
             return S;
        } else if (direction.equals("e")) {
             return E;
        } else if (direction.equals("w")) {
             return W;
         }
         return null;
     }
 
     public MoveUICommand withDirection(String direction) {
         this.direction = direction;
         return this;
     }
 }
