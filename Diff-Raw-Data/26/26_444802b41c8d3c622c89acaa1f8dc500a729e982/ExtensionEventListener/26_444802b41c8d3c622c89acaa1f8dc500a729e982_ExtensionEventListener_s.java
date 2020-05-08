 package org.apache.myfaces.extensions.scripting.sandbox.extensionevents;
 
import java.util.List;
 
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
     * returns the event idenitifiers this listener
      * is a listener for (additional hints which will speed up the event handling)
      *
     * @return
      */
    public List<Integer> listenerFor();
 
 }
