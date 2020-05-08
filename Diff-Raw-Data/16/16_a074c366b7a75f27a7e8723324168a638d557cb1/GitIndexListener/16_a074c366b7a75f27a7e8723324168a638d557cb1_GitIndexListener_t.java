 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jgit.events.IndexChangedListener;
 import sk.stuba.fiit.perconik.core.Listener;
 
 /**
 * A Git index listener.
  * 
  * @see Listener
  * @see IndexChangedListener
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public interface GitIndexListener extends Listener, IndexChangedListener
 {
 }
