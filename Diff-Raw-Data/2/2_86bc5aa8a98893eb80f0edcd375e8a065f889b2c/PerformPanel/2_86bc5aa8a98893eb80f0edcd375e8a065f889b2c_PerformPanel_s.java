 /*
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes,
  * Simon Mott
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.components.performpanel;
 
 import com.dmdirc.actions.wrappers.PerformWrapper;
 import com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription;
 import com.dmdirc.addons.ui_swing.components.TextAreaInputField;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Creates a text area that fills whatever space it has available. This panel
  * facilitates modification of performs.
  *
  * @author Simon Mott
  * @since 0.6.4
  */
 public class PerformPanel extends JPanel {
 
     /** Text area that the perform is displayed in. */
     private JTextArea performSpace;
     /** Map of performs this panel can display. */
     private final Map<PerformDescription, String[]> performs = new
             HashMap<PerformDescription, String[]>();
     /** The perform that is displayed in the text area. */
     private PerformDescription visiblePerform;
 
     /**
      * Creates a new instance of PerformPanel that has no knowledge of any
      * performs at the time of creation.
      *
      * By default this panel displays a blank text area.
      */
     public PerformPanel() {
         this(Collections.<PerformDescription>emptyList());
     }
 
     /**
      * Creates a new instance of PerformPanel and prepares the list of
      * PerformDescriptions passed to it for viewing/modification.
      *
      * By default this panel displays a blank text area.
      *
      * @param performs Collection of PerformDescriptions to initiliase
      */
     public PerformPanel(final Collection<PerformDescription> performs) {
         super();
         for (PerformDescription perform : performs) {
             addPerform(perform);
         }
        setLayout(new MigLayout("fill"));
         performSpace = new TextAreaInputField("");
         add(new JScrollPane(performSpace), "grow, push");
         visiblePerform = null;
     }
 
     /**
      * This will add a perform to the internal cache so that switching performs
      * is more efficient. Switching performs can be acheived using
      * {@link #switchPerform(com.dmdirc.actions.wrappers.PerformWrapper.PerformDescription) }
      *
      * @param perform PerformDescription to add
      */
     public void addPerform(final PerformDescription perform) {
         performs.put(perform, PerformWrapper.getPerformWrapper().getPerform(perform));
     }
 
     /**
      * Deletes a perform from the map of performs this PerformPanel can modify.
      *
      * @param perform PerformDescription to remove
      */
     public void delPerform(final PerformDescription perform) {
         performs.remove(perform);
     }
 
     /**
      * Saves modifications to the provided performs.
      */
     public void savePerform() {
         performs.put(visiblePerform, performSpace.getText().split("\n"));
         for (Entry<PerformDescription, String[]> perform : performs.entrySet()) {
             PerformWrapper.getPerformWrapper().setPerform(perform.getKey(),
                     perform.getValue());
         }
     }
 
     /**
      * Displays the specified perform to the user. Edits made to any
      * previously displayed perform are stored, but are not saved
      * until {@link #savePerform()} is called. If the specified perform
      * is not in this panel's cache, it will be added.
      *
      * @param perform Perform to display in the text area
      */
     public void switchPerform(final PerformDescription perform) {
         if (!performs.containsKey(perform)) {
             addPerform(perform);
         }
         if (visiblePerform != null) {
             performs.put(visiblePerform, performSpace.getText().split("\n"));
         }
         performSpace.setText(implode(performs.get(perform)));
         visiblePerform = perform;
     }
 
     /**
      * Implodes the specified string array, joining each line with a LF.
      *
      * @param lines The lines to be joined together
      * @return A string containing each element of lines, separated by a LF.
      */
     private String implode(final String[] lines) {
         final StringBuilder res = new StringBuilder();
 
         for (String line : lines) {
             res.append('\n');
             res.append(line);
         }
 
         return res.length() == 0 ? "" : res.substring(1);
     }
 }
