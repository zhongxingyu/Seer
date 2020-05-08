 package org.lehirti.luckysurvivor.peninsulaisthmus;
 
 import org.lehirti.engine.events.SetFlagTextOnlyEvent;
 import org.lehirti.engine.gui.Key;
 import org.lehirti.luckysurvivor.map.Map;
 import org.lehirti.luckysurvivor.peninsulaisthmus.MapToPeninsulaIsthmus.Text;
 
 public class FellTree extends SetFlagTextOnlyEvent {
   public FellTree() {
    super(Map.Path.PENINSULA_ISTHMUS___ISLAND_ENTRY, Key.OPTION_WEST, Text.FELL_TREE, new MapToPeninsulaIsthmus());
   }
 }
