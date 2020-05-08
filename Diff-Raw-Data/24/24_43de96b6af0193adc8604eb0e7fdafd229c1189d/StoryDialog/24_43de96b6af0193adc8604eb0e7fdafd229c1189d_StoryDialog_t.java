 /*
  *  Copyright (C) 2013 caryoscelus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gg;
 
 import java.util.HashMap;
 
 public class StoryDialog {
     public final String text;
     public final HashMap <String, StoryEvent> options;
     
    /**
     * Create dialog displaying text with "close" button
     */
     public StoryDialog (String t) {
         text = t;
         options = new HashMap();
         options.put("Close", new StoryEvent() {
             public boolean trigger () {
                 return false;
             }
         });
     }
     
    /**
     * Create dialog displaying text and "next" button leading to some
     * event
     */
    public StoryDialog (String t, StoryEvent nexte) {
        text = t;
        options = new HashMap();
        options.put("Next", nexte);
    }
    
    /**
     * Create dialog with text and options
     * NOTE: if opts is empty and dialog launched, something bad will happen
     */
    public StoryDialog (String t, HashMap<String,StoryEvent> opts) {
         text = t;
         options = opts;
     }
     
    /**
     * Add dialog option
     */
     public void add (String t, StoryEvent event) {
         options.put(t, event);
     }
 }
