 package Prop;
 
 import Application.GameException;
 import Player.Movement;
 import Player.Position;
 import UI.UIObserver;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class PropMap {
     private static final int CLEAN_RANGE = 10;
     private final Map<Position, Prop> props = new HashMap<Position, Prop>();
     private final UIObserver ui;
 
     public PropMap(UIObserver ui) {
         this.ui = ui;
     }
 
     public void put(Position position, Prop prop) {
         if (props.containsKey(position)) {
             throw new GameException("此处已有道具，不能再放置其他道具了。");
         }
         props.put(position, prop);
         prop.addToUI(ui, position);
     }
 
     public void cleanTheFront(Position position) {
         for (int step = 1; step <= CLEAN_RANGE; ++step) {
             removeProp(position.offset(step));
         }
     }
 
     public void trigger(Movement movement) {
         if (props.containsKey(movement.currentPosition())) {
            Prop prop = props.get(movement.currentPosition());
             removeProp(movement.currentPosition());
            prop.handle(movement);
         }
     }
 
     public boolean equals(Object object) {
         return getClass() == object.getClass() && props.equals(((PropMap) object).props);
     }
 
     private void removeProp(Position position) {
         if (props.containsKey(position)) {
             props.get(position).removeFromUI(ui, position);
             props.remove(position);
         }
     }
 }
