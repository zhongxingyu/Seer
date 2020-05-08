 package sk.stuba.fiit.perconik.core.listeners;
 
 import org.eclipse.jgit.events.ConfigChangedListener;
 import sk.stuba.fiit.perconik.core.Listener;
 
 /**
 * A Git configuration listener.
  * 
  * @see Listener
  * @see ConfigChangedListener
  * 
  * @author Pavol Zbell
  * @since 1.0
  */
 public interface GitConfigurationListener extends Listener, ConfigChangedListener
 {
 }
