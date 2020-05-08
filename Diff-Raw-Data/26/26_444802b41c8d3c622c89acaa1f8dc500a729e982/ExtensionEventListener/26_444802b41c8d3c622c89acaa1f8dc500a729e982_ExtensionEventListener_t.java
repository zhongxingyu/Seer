 package org.apache.myfaces.extensions.scripting.sandbox.extensionevents;
 
import java.util.Set;
 
 /**
  * @author Werner Punz (latest modification by $Author$)
  * @version $Revision$ $Date$
  *          <p/>
  *          Central external listener interface for the extension events
  */
 public interface ExtensionEventListener {
 
     /**
      * The central event handling callback which gets
      * the callback back
      *
      * @param ev
      */
     public void handleEvent(ExtensionEvent ev);
 
     /**
     * returns the event identifiers this listener
      * is a listener for (additional hints which will speed up the event handling)
      *
     * @param evt the event to be triggered for
     * @return true if the listener is a listener for the specific event
      */
    public boolean isListenerFor(ExtensionEvent evt);
 
 }
