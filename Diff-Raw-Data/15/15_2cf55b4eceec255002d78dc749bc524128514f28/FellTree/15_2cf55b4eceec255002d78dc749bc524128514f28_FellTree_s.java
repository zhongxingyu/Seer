 package org.lehirti.luckysurvivor.peninsulaisthmus;
 
 import org.lehirti.engine.events.SetFlagTextOnlyEvent;
 import org.lehirti.engine.gui.Key;
 import org.lehirti.luckysurvivor.map.Map;
 import org.lehirti.luckysurvivor.peninsulaisthmus.MapToPeninsulaIsthmus.Text;
 
 public class FellTree extends SetFlagTextOnlyEvent {
   public FellTree() {
    /*
     * TODO this event is supposed to make the path to the east available, but since this path is not defined, yet, the
     * PENINSULA_BASIN_JUNGLE___PENINSULA_ISTHMUS (which already is available) is there as a place holder. once the path
     * to the east does exist, just replace it here make sure that the Map$Path.properties initializes the new path with
     * FALSE (so it's not available before you fell the tree)
     */
    super(Map.Path.PENINSULA_BASIN_JUNGLE___PENINSULA_ISTHMUS, Key.OPTION_WEST, Text.FELL_TREE,
        new MapToPeninsulaIsthmus());
   }
 }
