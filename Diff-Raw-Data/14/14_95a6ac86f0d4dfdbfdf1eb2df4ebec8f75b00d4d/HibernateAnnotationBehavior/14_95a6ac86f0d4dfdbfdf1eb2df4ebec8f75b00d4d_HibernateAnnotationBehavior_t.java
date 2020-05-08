 package org.wicketstuff.hibernate.behavior;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.wicketstuff.hibernate.HibernateAnnotationComponentConfigurator;
 
 /**
  * Updates behavior of wicket components based on hibernate annotations.
 *
  * @author rsonnek
  */
 public class HibernateAnnotationBehavior extends AbstractBehavior {
 
 	@Override
 	public void bind(Component component) {
 		super.bind(component);
 
 		new HibernateAnnotationComponentConfigurator().configure(component);
 	}
 }
