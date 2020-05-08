 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eagerlogic.cubee.client.properties;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  *
  * @author dipacs
  */
public class Property<T> implements IProperty<T>, IAnimateable<T>, IBindable<IProperty<T>> {
     
     private LinkedList<IChangeListener> changeListeners = new LinkedList<IChangeListener>();
     private T value;
     private boolean valid = false;
     private final boolean nullable;
     private final boolean readonly;
    private IProperty<T> bindingSource;
     private IChangeListener bindListener;
     private IProperty<T> readonlyBind;
 
     public Property(T defaultValue, boolean nullable, boolean readonly) {
         this.value = defaultValue;
         this.nullable = nullable;
         this.readonly = readonly;
         
         if (value == null && nullable == false) {
             throw new IllegalArgumentException("A nullable property can not be null.");
         }
 		
 		invalidate();
     }
     
     public final void initReadonlyBind(IProperty<T> readonlyBind) {
         if (this.readonlyBind != null) {
             throw new IllegalStateException("The readonly bind is already initialized.");
         }
         this.readonlyBind = readonlyBind;
     }
     
     public final T get() {
 		this.valid = true;
 		
         if (bindingSource != null) {
            return (T) bindingSource.getObjectValue();
         }
         
         if (readonlyBind != null) {
             return (T) readonlyBind.getObjectValue();
         }
         
         return this.value;
     }
     
     public final void set(T newValue) {
         if (this.readonly) {
             throw new IllegalStateException("Can not change the value of a readonly property.");
         }
         
         if (this.bindingSource != null) {
             throw new IllegalStateException("Can not set the value of a bound property");
         }
         
         if (!this.nullable && newValue == null) {
             throw new IllegalStateException("Can not set the value to null of a non nullable property.");
         }
         
         if (this.value == newValue) {
             return;
         }
         
         if (this.value != null && this.value.equals(newValue)) {
             return;
         }
 		
 		this.value = newValue;
         
         invalidate();
     }
     
     @Override
     public final void invalidate() {
         this.valid = false;
         fireChangeListeners();
     }
     
     private void fireChangeListeners() {
         for (IChangeListener listener : changeListeners) {
             listener.onChanged(this);
         }
     }
 
     @Override
     public Object getObjectValue() {
         return this.get();
     }
 
     @Override
     public void addChangeListener(IChangeListener listener) {
         if (listener == null) {
             throw new NullPointerException("The listener parameter can not be null.");
         }
         
         if (hasChangeListener(listener)) {
             return;
         }
         
         changeListeners.add(listener);
     }
 
     @Override
     public void removeChangeListener(IChangeListener listener) {
         Iterator<IChangeListener> iterator = changeListeners.iterator();
         while (iterator.hasNext()) {
             if (iterator.next() == listener) {
                 iterator.remove();
                 return;
             }
         }
     }
 
     @Override
     public boolean hasChangeListener(IChangeListener listener) {
         for (IChangeListener l : changeListeners) {
             if (l == listener) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public T animate(double pos, T startValue, T endValue) {
         if (pos < 0.5) {
             return startValue;
         }
         
         return endValue;
     }
 
     @Override
    public void bind(IProperty<T> source) {
         if (source == null) {
             throw new NullPointerException("The source can not be null.");
         }
         
         if (this.bindingSource != null) {
             this.bindingSource.removeChangeListener(this.bindListener);
         }
         
         if (readonly) {
             throw new IllegalStateException("Can't bind a readonly property.");
         }
         
         this.bindingSource = source;
         this.bindListener = new IChangeListener() {
 
             @Override
             public void onChanged(Object sender) {
                 invalidate();
             }
         };
         this.bindingSource.addChangeListener(bindListener);
         this.invalidate();
     }
 
     @Override
     public void unbind() {
         if (this.bindingSource != null) {
             this.bindingSource.removeChangeListener(this.bindListener);
             invalidate();
         }
     }
     
 	@Override
     public final boolean isBound() {
         return this.bindingSource != null;
     }
 
 	public final boolean isNullable() {
 		return nullable;
 	}
 
 	public final boolean isReadonly() {
 		return readonly;
 	}
 	
 	PropertyLine<T> createPropertyLine(LinkedList<KeyFrame> keyFrames) {
 		return new PropertyLine<T>(keyFrames);
 	}
     
 }
