 package module.vaadin.data.util;
 
 import java.util.Arrays;
 import java.util.Collection;
 
 import pt.ist.vaadinframework.data.HintedProperty;
 
 import com.vaadin.data.util.ObjectProperty;
 
 public class ObjectHintedProperty<T> extends ObjectProperty<T> implements HintedProperty {
 
    private final Collection<Hint> hints;
 
     public ObjectHintedProperty(Object value, Class<T> type, Collection<Hint> hints) {
 	super(value, type);
 	this.hints = hints;
     }
 
     public ObjectHintedProperty(Object value, Class<T> type, Hint... hints) {
 	super(value, type);
	this.hints = Arrays.asList(hints);
     }
 
     @Override
    public Collection<Hint> getHints() {
	return hints;
     }
 
     @Override
    public void addHint(Hint arg0) {
	hints.add(arg0);
     }

 }
