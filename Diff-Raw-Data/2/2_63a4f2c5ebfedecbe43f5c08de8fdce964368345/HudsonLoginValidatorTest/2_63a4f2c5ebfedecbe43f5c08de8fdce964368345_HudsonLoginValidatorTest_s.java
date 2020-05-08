 package org.jabox.cis.hudson;
 
 import junit.framework.Assert;
 import junit.framework.TestCase;
 
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.tester.WicketTester;
 import org.apache.wicket.validation.Validatable;
 
public class HudsonLoginValidatorTest extends TestCase {
 
 	public void testOnValidateFails() {
 		new WicketTester();
 		TextField<String> url = new TextField<String>("url", new Model<String>(
 				"http://localhost:9090/hudson/"));
 		TextField<String> username = new TextField<String>("admin",
 				new Model<String>("admin"));
 		PasswordTextField password = new PasswordTextField("admin",
 				new Model<String>("admin2"));
 		HudsonLoginValidator hlvalidator = new HudsonLoginValidator(url,
 				username, password);
 		Validatable<String> validatable = new Validatable<String>();
 		hlvalidator.onValidate(validatable);
 		Assert.assertEquals(1, validatable.getErrors().size());
 	}
 
 	public void testOnValidateSucceeds() {
 		new WicketTester();
 		TextField<String> url = new TextField<String>("url", new Model<String>(
 				"http://localhost:9090/hudson/"));
 		TextField<String> username = new TextField<String>("admin",
 				new Model<String>("admin"));
 		PasswordTextField password = new PasswordTextField("admin",
 				new Model<String>("admin"));
 		HudsonLoginValidator hlvalidator = new HudsonLoginValidator(url,
 				username, password);
 		Validatable<String> validatable = new Validatable<String>();
 		hlvalidator.onValidate(validatable);
 		Assert.assertEquals(0, validatable.getErrors().size());
 	}
 
 }
