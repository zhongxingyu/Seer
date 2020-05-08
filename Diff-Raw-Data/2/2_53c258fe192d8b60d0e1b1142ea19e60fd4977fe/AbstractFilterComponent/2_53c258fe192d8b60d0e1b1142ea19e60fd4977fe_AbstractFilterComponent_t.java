 package cz.datalite.zk.components.list.filter.components;
 
 import org.zkoss.zk.ui.WrongValueException;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.impl.InputElement;
 
 /**
  * This class implements FilterComponent interface and defines a standard
  * behaviour of majority of the methods. Every method can be overriden to define
  * a specific implementation.
  * @author Karel Čemus <cemus@datalite.cz>
  */
 public abstract class AbstractFilterComponent<T extends InputElement> implements CloneableFilterComponent {
 
     protected T component;
 
     protected AbstractFilterComponent( final T component ) {
         this.component = component;
        component.setHflex(  "1" );
     }
 
     public void addOnChangeEventListener( final EventListener listener ) {
         component.addEventListener( Events.ON_CHANGE, listener );
     }
 
     public T getComponent() {
         return component;
     }
 
     public Object getValue() {
         return component.getRawValue();
     }
 
     public void setValue( final Object value ) {
         component.setRawValue( value );
     }
 
     /**
      * There is empty implementation of the method. Standard components needn't
      * be validated because components generates some constraionts and in the major
      * part of cases it is enough.
      * @throws WrongValueException validation exception
      */
     public void validate() throws WrongValueException {
         // validation code
         // this is always valid
     }
 }
