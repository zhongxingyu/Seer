 package org.wicketstuff.hibernate;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.application.IComponentInstantiationListener;
 
 /**
  * Component listener that will automatically configure a form component
  * based on it's hibernate annotations.
 * 
 * This component helps ensure that an entire application respects the 
  * annotations without adding custom validators or behaviors to each form component.
 * 
  * @author rsonnek
  */
 public class HibernateAnnotationComponentInstantiaionListener implements IComponentInstantiationListener {
 
 	public void onInstantiation(Component component) {
		new HibernateAnnotationComponentConfigurator().configure(component);
 	}
 
 }
