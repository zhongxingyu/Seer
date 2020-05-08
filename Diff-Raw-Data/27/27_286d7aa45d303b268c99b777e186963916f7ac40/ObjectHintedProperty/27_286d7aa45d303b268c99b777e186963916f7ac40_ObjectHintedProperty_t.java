 package module.vaadin.data.util;
 
import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
import java.util.Collections;
 
 import pt.ist.vaadinframework.data.HintedProperty;
 
 import com.vaadin.data.util.ObjectProperty;
 
 public class ObjectHintedProperty<T> extends ObjectProperty<T> implements HintedProperty {
 
    private Collection<Hint> hints;
 
     public ObjectHintedProperty(Object value, Class<T> type, Collection<Hint> hints) {
 	super(value, type);
 	this.hints = hints;
     }
 
     public ObjectHintedProperty(Object value, Class<T> type, Hint... hints) {
 	super(value, type);
	this.hints = new ArrayList<Hint>(Arrays.asList(hints));
     }
 
     @Override
    public void addHint(Hint hint) {
	if (hints == null) {
	    hints = new ArrayList<Hint>();
	}
	hints.add(hint);
     }
 
     @Override
    public Collection<Hint> getHints() {
	if (hints != null) {
	    return Collections.unmodifiableCollection(hints);
	}
	return Collections.emptyList();
     }
 }
