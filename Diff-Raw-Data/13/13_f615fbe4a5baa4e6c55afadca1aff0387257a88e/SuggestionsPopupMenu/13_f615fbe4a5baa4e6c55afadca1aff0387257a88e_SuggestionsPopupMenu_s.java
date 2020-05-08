 /*
  * Quill and Parchment, a WYSIWYN document editor and rendering engine. 
  *
  * Copyright © 2010 Operational Dynamics Consulting, Pty Ltd
  *
  * The code in this file, and the program it is a part of, is made available
  * to you by its authors as open source software: you can redistribute it
  * and/or modify it under the terms of the GNU General Public License version
  * 2 ("GPL") as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GPL for more details.
  *
  * You should have received a copy of the GPL along with this program. If not,
  * see http://www.gnu.org/licenses/. The authors of this program may be
  * contacted through http://research.operationaldynamics.com/projects/quill/.
  */
 package quill.ui;
 
 import org.gnome.gdk.Screen;
 import org.gnome.gtk.Alignment;
 import org.gnome.gtk.ImageMenuItem;
 import org.gnome.gtk.Label;
 import org.gnome.gtk.Menu;
 import org.gnome.gtk.MenuItem;
 import org.gnome.gtk.Requisition;
 import org.gnome.gtk.SeparatorMenuItem;
 import org.gnome.gtk.Stock;
 
 import static quill.client.Quill.ui;
 
 /**
  * Since Enchant can get a bit out of control with the suggestions it offers,
  * limit the displayed menu to 10 suggestions, thereby preventing the Menu
  * from wrapping top/bottom.
  * 
  * @author Andrew Cowie
  */
 class SuggestionsPopupMenu extends Menu
 {
     private Menu menu;
 
     private MenuItem.Activate picked;
 
     private SuggestionsPopupMenu.WordSelected handler;
 
     SuggestionsPopupMenu() {
         super();
         menu = this;
 
         picked = new MenuItem.Activate() {
             public void onActivate(MenuItem source) {
                 final WordMenuItem local;
                 final String suggestion;
 
                 local = (WordMenuItem) source;
                 suggestion = local.getWord();
 
                 handler.onWordSelected(suggestion, true);
             }
         };
 
         hookupBehaviourHandlers();
     }
 
     private void hookupBehaviourHandlers() {}
 
     void populateSuggestions(final String word) {
         final String[] suggestions;
         MenuItem item;
         Label label;
         int i;
 
        if (word == null) {
            return;
        }

         /*
          * Add a MenuItem for each suggestion provided by Enchant. Reuse the
          * same signal handler for each one; calls the WordSelected handler
          * passed in by the EditorTextView if it is Activated.
          */
 
         suggestions = ui.dict.suggest(word);
 
         if (suggestions == null) {
             label = new Label("<i>no suggestions</i>");
             label.setUseMarkup(true);
             label.setAlignment(Alignment.LEFT, Alignment.CENTER);
 
             item = new MenuItem();
             item.add(label);
 
             menu.append(item);
 
         } else {
             for (i = 0; i < suggestions.length; i++) {
                 if (i > 10) {
                     break;
                 }
 
                 label = new Label("<b>" + suggestions[i] + "</b>");
                 label.setUseMarkup(true);
                 label.setAlignment(Alignment.LEFT, Alignment.CENTER);
 
                 item = new WordMenuItem(suggestions[i]);
                 item.add(label);
 
                 item.connect(picked);
 
                 menu.append(item);
             }
         }
 
         /*
          * Now, since we allow asking for suggestions even on correctly
          * spelled words [which is a prelude to completion operations], we
          * need to NOT offer to add the word to a custom dictionary if the
          * word is not already unknown.
          */
         if (!ui.dict.check(word)) {
             item = new SeparatorMenuItem();
             menu.append(item);
             item = new ImageMenuItem(Stock.ADD);
             menu.append(item);
 
             item.connect(new MenuItem.Activate() {
                 public void onActivate(MenuItem source) {
                     menu.hide();
                     ui.dict.add(word);
 
                     handler.onWordSelected(word, false);
                 }
             });
         }
 
         menu.showAll();
     }
 
     /**
      * Present the suggestions menu at x, y (and supplying current line's
      * y-range [height] as R).
      */
     void presentAt(final int x, final int y, final int R) {
         final Screen screen;
         final int h, H;
         int target;
         final Requisition req;
 
         /*
          * Get the available height. We use the user's screen rather than the
          * parent window because there's nothing wrong with a popup menu
          * overlapping (say) the gnome-panel. Just use the the UserInterface
          * global as an easily available toplevel Window.
          */
 
         screen = ui.primary.getScreen();
         H = screen.getHeight();
 
         req = menu.getRequisition();
         h = req.getHeight();
 
         /*
          * Figure out if there is sufficient room for the popup below, as we
          * would prefer. The plus 3 is sufficient to continue to show the red
          * squiggle.
          */
 
         target = y + R + 3;
 
         if (target + h > H) {
             target = y - h;
             if (target < 0) {
                 target = y;
             }
         }
         menu.popup(x, target);
     }
 
     /**
      * Allow the parent EditorTextView to react to a selection being chosen
      */
     void connect(SuggestionsPopupMenu.WordSelected handler) {
         this.handler = handler;
     }
 
     /**
      * Return the word chosen by the user (if any) to the EditorTextView
      * asking for suggestions.
      */
     static interface WordSelected
     {
         /**
          * @param replacement
          *            Indicates whether the word being returned is different
          *            from the word that was queried.
          */
         void onWordSelected(String word, boolean replacement);
     }
 
     /**
      * Wrapper class to let us get at the word that was passed in as a
      * suggestion.
      * 
      * @author Andrew Cowie
      */
     private static class WordMenuItem extends MenuItem
     {
         private String word;
 
         private WordMenuItem(final String suggestion) {
             this.word = suggestion;
         }
 
         private String getWord() {
             return word;
         }
     }
 }
