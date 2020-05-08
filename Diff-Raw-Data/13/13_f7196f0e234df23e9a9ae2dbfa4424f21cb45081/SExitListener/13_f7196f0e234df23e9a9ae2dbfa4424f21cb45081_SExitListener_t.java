 package org.wings.event;
 
 import java.util.EventListener;
 
 
 
 /**
  * SExitListener.java
  *
  *
  * Created: Fri Dec  6 11:08:26 2002
  *
  * @author <a href="mailto:armin.haaf@mercatis.de">Armin Haaf</a>
  * @version
  */
 
 public interface SExitListener extends EventListener {
     
    public void prepareExit(SExitEvent e) throws ExitVetoException;
     
 }// SExitListener
