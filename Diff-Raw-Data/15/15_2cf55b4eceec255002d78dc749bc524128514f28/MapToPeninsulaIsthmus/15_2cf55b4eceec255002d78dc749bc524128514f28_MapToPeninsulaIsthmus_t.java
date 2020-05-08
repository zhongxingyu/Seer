 package org.lehirti.luckysurvivor.peninsulaisthmus;
 
 import org.lehirti.engine.events.EventNode;
 import org.lehirti.engine.events.TextOnlyEvent;
 import org.lehirti.engine.events.Event.NullState;
 import org.lehirti.engine.gui.Key;
 import org.lehirti.engine.res.images.ImgChange;
 import org.lehirti.engine.res.text.CommonText;
 import org.lehirti.engine.res.text.TextKey;
 import org.lehirti.engine.state.State;
 import org.lehirti.luckysurvivor.islandentry.MapToIslandEntry;
 import org.lehirti.luckysurvivor.map.Map;
 import org.lehirti.luckysurvivor.map.Map.Location;
 
 /**
  * this is the default first event when entering
  */
 public class MapToPeninsulaIsthmus extends EventNode<NullState> {
   public static enum Text implements TextKey {
     DESCRIPTION,
     LEAVE_THE_AREA,
     OPTION_SEARCH_FOR_A_WAY,
     SEARCH_FOR_A_WAY,
     FIND_TREE,
     OPTION_FELL_TREE,
     FELL_TREE,
     OPTION_GO_EAST,
     TEXT_NO_WAY_ACROSS,
   }
   
   @Override
   protected ImgChange updateImageArea() {
     return ImgChange.setBGAndFG(PeninsulaIsthmus.PENINSULA_ISTHMUS);
   }
   
   @Override
   protected void doEvent() {
     setText(Text.DESCRIPTION);
     
     // as long as no way is found
     if (State.getEventCount(FindTree.class) == 0) {
       // search for a ways
       addOption(Key.OPTION_WEST, Text.OPTION_SEARCH_FOR_A_WAY, new SearchForAWay());
       
       // once a suitable tree is found, make felling it available as a one-time event
     } else if (State.getEventCount(FellTree.class) == 0) {
       addOption(Key.OPTION_WEST, Text.OPTION_FELL_TREE, new FellTree());
     }
     
     // as long as there no path across
     if (State.getEventCount(FellTree.class) == 0) {
       // just state that "YOU ... SHALL ... NOT ... PASS ^^"
       addOption(Key.OPTION_EAST, Text.OPTION_GO_EAST, new TextOnlyEvent(Key.OPTION_EAST, Text.TEXT_NO_WAY_ACROSS,
           new MapToPeninsulaIsthmus()));
     } else {
       // once the tree is felled, go east
       addOption(Key.OPTION_EAST, Text.OPTION_GO_EAST, new MapToIslandEntry());
     }
     
     addOption(Key.OPTION_LEAVE, CommonText.OPTION_LEAVE_AREA, new TextOnlyEvent(Key.OPTION_LEAVE, Text.LEAVE_THE_AREA,
         new Map(Location.PENINSULA_ISTHMUS)));
   }
 }
