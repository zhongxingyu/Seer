 package ru.ifmo.vizi.base.ui;
 
 import ru.ifmo.vizi.base.Configuration;
 
 /**
  * Multistate button.
  * Each press changes button caption.
  * Separate hints supported for each caption.
  * <p>
  * States are numbered from zero.
  * <p>
  * Used configuration parameters:
  * <table border="1">
  *      <tr>
  *          <th>Name</th>
  *          <th>Description</th>
  *      </th>
  *      <tr>
  *          <td><b>state[i]</b></td>
  *          <td>i-th button caption</td>
  *      </tr>
  *      <tr>
  *          <td><b>state[i]</b>-hint</td>
  *          <td>i-th button hint</td>
  *      </tr>
  * </table>
  *
  * @author  Georgiy Korneev
  * @version $Id: MultiButton.java,v 1.3 2004/06/07 13:59:42 geo Exp $
  */
 public class MultiButton extends HintedButton {
     /**
      * Button captions.
      */
     private final String captions[];
 
     /**
      * Per-caption hints.
      */
     private final String hints[];
 
     /**
      * Per-caption hot keys.
      */
     private final String hotKeys[];
 
     /**
      * Current state.
      */
     private int state;
 
     /**
      * Creates a new <code>MultiButton</code>.
      *
      * @param config visualizer configuration.
      * @param states name of the paremeter to get configuration from.
      */
     public MultiButton(Configuration config, String[] states) {
         captions = new String[states.length];
         hints = new String[states.length];
         hotKeys = new String[states.length];
         for (int i = 0; i < states.length; i++) {
             captions[i] = config.getParameter(states[i]);
             hints[i] = config.getParameter(states[i] + "-hint");
             hotKeys[i] = config.getParameter(states[i] + "-hotKey", null);
         }
 
         state = 1;
         setState(0);
     }
 
     /**
      * Gets state of the button.
      *
      * @return current state.
      */
     public final int getState() {
         return state;
     }
 
     /**
      * Sets state of the button.
      *
      * @param state new state.
      */
     public final void setState(int state) {
         if (state < 0) state = 0;
         if (state >= captions.length) {
             state = captions.length - 1;
         }
         if (this.state != state) {
             this.state = state;
            setLabel(captions[state]);
             setHint(hints[state], hotKeys[state]);
         }
    }
 
     /**
      * Invoked when users click on button.
      *
      * @param state current button state.
      *
      * @return button state to set.
      */
     protected int click(int state) {
         return state;
     }
 
     /**
      * Invoked when user has clicked on button.
      */
     protected final void click() {
         setState(click(state));
     }
 }
