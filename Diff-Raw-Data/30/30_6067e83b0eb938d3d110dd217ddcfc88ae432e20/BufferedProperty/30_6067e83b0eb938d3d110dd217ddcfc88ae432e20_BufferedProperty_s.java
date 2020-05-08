 package pt.ist.vaadinframework.data;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import pt.ist.fenixWebFramework.services.ServiceManager;
 import pt.ist.fenixWebFramework.services.ServicePredicate;
 import pt.ist.vaadinframework.data.util.ServiceUtils;
 
 import com.vaadin.data.BufferedValidatable;
 import com.vaadin.data.Property;
 import com.vaadin.data.Validator;
 import com.vaadin.data.Validator.InvalidValueException;
 import com.vaadin.data.util.ObjectProperty;
 
 public class BufferedProperty<Type> extends AbstractHintedProperty<Type> implements BufferedValidatable {
     protected Type cache;
 
     protected final Property wrapped;
 
     protected boolean modified = false;
 
     private boolean writeThrough = false;
 
     private boolean readThrough = false;
 
     private boolean invalidAllowed = true;
 
     private boolean invalidCommited = false;
 
     private List<Validator> validators;
 
     public BufferedProperty(Property wrapped, Hint... hints) {
 	super(hints);
 	this.wrapped = wrapped;
 	this.cache = convertValue(wrapped.getValue());
     }
 
     public BufferedProperty(Class<? extends Type> type, Hint... hints) {
 	super(hints);
 	this.wrapped = new ObjectProperty<Type>(null, (Class<Type>) type);
 	this.cache = convertValue(null);
     }
 
     public BufferedProperty(Type value, Hint... hints) {
 	super(hints);
 	this.wrapped = new ObjectProperty<Type>(value);
 	this.cache = convertValue(value);
     }
 
     public BufferedProperty(Type value, Class<? extends Type> type, Hint... hints) {
 	super(hints);
 	this.wrapped = new ObjectProperty<Type>(value, (Class<Type>) type);
 	this.cache = convertValue(value);
     }
 
     /**
      * Override to enable property conversion on cache update.
      * 
      * @param value
      *            Value read from the wrapped property.
      * @return Converted value to submit to the cache.
      */
     protected Type convertValue(Object value) throws ConversionException {
 	return (Type) value;
     }
 
     @Override
     public Type getValue() {
 	if (isReadThrough() && !isModified()) {
 	    cache = convertValue(wrapped.getValue());
 	}
 	return cache;
     }
 
     @Override
     public void setValue(Object newValue) throws ReadOnlyException, ConversionException {
 	if (isReadOnly()) {
 	    throw new ReadOnlyException();
 	}
 
 	// If invalid values are not allowed, the value must be checked
 	if (!isInvalidAllowed()) {
 	    final Collection<Validator> v = getValidators();
 	    if (v != null) {
 		for (Validator validator : v) {
 		    validator.validate(newValue);
 		}
 	    }
 	}
 
 	Type current = convertValue(wrapped.getValue());
 	Type prevCache = cache;
 	cache = convertValue(newValue);
 	processNewCacheValue();
 	if (differ(current, cache)) {
 	    modified = true;
 	    if (isWriteThrough()) {
 		commit();
 	    }
 	} else {
 	    modified = false;
 	}
 	if (differ(prevCache, cache)) {
 	    fireValueChange();
 	}
     }
 
     protected void processNewCacheValue() {
     }
 
     protected boolean differ(Object oldV, Object newV) {
 	if (newV != null) {
 	    return !newV.equals(oldV);
 	}
 	if (oldV != null) {
 	    return true;
 	}
 	return false;
     }
 
     @Override
     public Class<? extends Type> getType() {
 	return (Class<? extends Type>) wrapped.getType();
     }
 
     /**
      * @see com.vaadin.data.Buffered#commit()
      */
     @Override
     public void commit() throws SourceException, InvalidValueException {
 	ServiceManager.execute(new ServicePredicate() {
 	    @Override
 	    public void execute() {
 		try {
 		    if (!isInvalidCommitted() && !isValid()) {
 			validate();
 		    }
 		    if (isModified()) {
 			wrapped.setValue(cache);
 		    }
 		    modified = false;
 		} catch (Throwable e) {
 		    ServiceUtils.handleException(e);
 		    throw new SourceException(BufferedProperty.this, e);
 		}
 	    }
 	});
     }
 
     /**
      * @see com.vaadin.data.Buffered#discard()
      */
     @Override
     public void discard() throws SourceException {
 	Type prevCache = cache;
 	cache = convertValue(wrapped.getValue());
 	if (differ(prevCache, cache)) {
 	    fireValueChange();
 	}
 	modified = false;
     }
 
     /**
      * @see com.vaadin.data.Buffered#isWriteThrough()
      */
     @Override
     public boolean isWriteThrough() {
 	return writeThrough;
     }
 
     /**
      * @see com.vaadin.data.Buffered#setWriteThrough(boolean)
      */
     @Override
     public void setWriteThrough(boolean writeThrough) throws SourceException, InvalidValueException {
 	if (writeThrough != this.writeThrough) {
 	    this.writeThrough = writeThrough;
 	    if (writeThrough && modified) {
 		commit();
 	    }
 	}
     }
 
     /**
      * @see com.vaadin.data.Buffered#isReadThrough()
      */
     @Override
     public boolean isReadThrough() {
 	return readThrough;
     }
 
     /**
      * @see com.vaadin.data.Buffered#setReadThrough(boolean)
      */
     @Override
     public void setReadThrough(boolean readThrough) throws SourceException {
 	if (readThrough != this.readThrough) {
 	    this.readThrough = readThrough;
 	}
     }
 
     /**
      * @see com.vaadin.data.Buffered#isModified()
      */
     @Override
     public boolean isModified() {
 	return modified;
     }
 
     /**
      * @see com.vaadin.data.Validatable#addValidator(com.vaadin.data.Validator)
      */
     @Override
     public void addValidator(Validator validator) {
 	if (validators == null) {
 	    validators = new LinkedList<Validator>();
 	}
 	validators.add(validator);
     }
 
     /**
      * @see com.vaadin.data.Validatable#removeValidator(com.vaadin.data.Validator)
      */
     @Override
     public void removeValidator(Validator validator) {
 	if (validators != null) {
 	    validators.remove(validator);
 	}
     }
 
     /**
      * @see com.vaadin.data.Validatable#getValidators()
      */
     @Override
     public Collection<Validator> getValidators() {
 	if (validators == null || validators.isEmpty()) {
 	    return null;
 	}
 	return Collections.unmodifiableCollection(validators);
     }
 
     /**
      * @see com.vaadin.data.Validatable#isValid()
      */
     @Override
     public boolean isValid() {
 	if (validators != null) {
 	    for (Validator validator : validators) {
 		if (!validator.isValid(this.getValue())) {
 		    return false;
 		}
 	    }
 	}
 	return true;
     }
 
     /**
      * @see com.vaadin.data.Validatable#validate()
      */
     @Override
     public void validate() throws InvalidValueException {
 	LinkedList<InvalidValueException> errors = null;
 	if (validators != null) {
 	    for (Validator validator : validators) {
 		try {
 		    validator.validate(this.getValue());
 		} catch (InvalidValueException e) {
 		    if (errors == null) {
 			errors = new LinkedList<InvalidValueException>();
 		    }
 		    errors.add(e);
 		}
 	    }
 	}
 	if (errors != null) {
 	    throw new InvalidValueException(null, errors.toArray(new InvalidValueException[0]));
 	}
     }
 
     /**
      * @see com.vaadin.data.Validatable#isInvalidAllowed()
      */
     @Override
     public boolean isInvalidAllowed() {
 	return invalidAllowed;
     }
 
     /**
      * @see com.vaadin.data.Validatable#setInvalidAllowed(boolean)
      */
     @Override
     public void setInvalidAllowed(boolean invalidAllowed) throws UnsupportedOperationException {
 	this.invalidAllowed = invalidAllowed;
     }
 
     /**
      * @see com.vaadin.data.BufferedValidatable#isInvalidCommitted()
      */
     @Override
     public boolean isInvalidCommitted() {
 	return invalidCommited;
     }
 
     /**
      * @see com.vaadin.data.BufferedValidatable#setInvalidCommitted(boolean)
      */
     @Override
     public void setInvalidCommitted(boolean invalidCommitted) {
 	this.invalidCommited = invalidCommitted;
     }
 }
