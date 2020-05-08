 package org.wicketstuff.hibernate;
 
 import junit.framework.TestCase;
 
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.util.tester.WicketTester;
 import org.hibernate.validator.Length;
 import org.hibernate.validator.NotNull;
 
 public class HibernateAnnotationComponentInstantiaionListenerTest extends TestCase {
 
 	public void setUp() {
 		WicketTester tester = new WicketTester();
 		tester.getApplication().addComponentInstantiationListener(new HibernateAnnotationComponentInstantiaionListener());
 	}
 
 	public void testNotNullAnnotationUpdatesComponentToBeRequired() {
 		TextField component = new TextField("test", new HibernateAnnotationPropertyModel(new MyObject(), "id"));
		
 		assertTrue(component.isRequired());
 	}
 
 	public class MyObject {
 		@NotNull
 		private String id;
		
 		@Length(max=50)
 		private String name;
 	}
 }
