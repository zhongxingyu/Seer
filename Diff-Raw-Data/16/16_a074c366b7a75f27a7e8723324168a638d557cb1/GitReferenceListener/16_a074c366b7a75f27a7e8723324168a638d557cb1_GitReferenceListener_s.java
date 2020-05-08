 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jgit.events.RefsChangedListener;
 import sk.stuba.fiit.perconik.core.Listener;
 
 /**
 * A <i>git</i> reference listener.
  * 
  * @see Listener
  * @see RefsChangedListener
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public interface GitReferenceListener extends Listener, RefsChangedListener
 {
 }
