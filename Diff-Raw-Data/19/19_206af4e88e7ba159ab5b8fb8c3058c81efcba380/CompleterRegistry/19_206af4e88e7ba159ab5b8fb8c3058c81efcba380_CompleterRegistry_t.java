 package jisssea.ui.completers;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import charvax.swing.JTextField;
 
 public class CompleterRegistry {
 
 	private static final Log log = LogFactory.getLog(CompleterRegistry.class);
 
 	public final static CompleterRegistry v = new CompleterRegistry();
 
 	private final List<Completer> listeners;
 
 	private CompleterRegistry() {
 		listeners = new ArrayList<Completer>();
		final String[] names = { "jisssea.ui.completers.HistoryCompleter", "jisssea.ui.completers.ShortcutsCompleter", };
 		for (String name : names) {
 			try {
 				Class<?> cls = Class.forName(name);
 				if (Arrays.asList(cls.getInterfaces()).contains(Completer.class)) {
 
 					if (cls.isEnum()) {
 						for (Object e : cls.getEnumConstants()) {
 							log.debug("Adding completer: " + e);
 							listeners.add((Completer) e);
 						}
 					} else {
 						Completer cmd = (Completer) cls.newInstance();
 						log.debug("Adding completer: " + name);
 						listeners.add(cmd);
 					}
 				} else {
 					log.error("Unable to load completer: " + name);
 				}
 			} catch (Exception e) {
 				log.error("Error initialising completer: ", e);
 			}
 		}
 	}
 
 	public void addCompleters(JTextField field) {
 		log.debug("Adding: " + listeners + " to " + field);
 		for (Completer comp : listeners) {
 			try {
 				final Method meth = comp.getClass().getMethod("complete", String.class);
 				final Key key = meth.getAnnotation(Key.class);
 				field.addKeyListener(new KeyHook(key.value(), comp));
 			} catch (Exception e) {
 				log.error("Error adding completers", e);
 			}
 		}
 	}
 }
