 package org.jabox.its.redmine;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 
 import org.apache.wicket.markup.html.form.PasswordTextField;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.validation.IValidatable;
 import org.apache.wicket.validation.validator.UrlValidator;
 import org.xml.sax.SAXException;
 
 public class RedmineLoginValidator extends UrlValidator {
 	private static final long serialVersionUID = 2635593287132542621L;
 	private final TextField<String> _url;
 	private final TextField<String> _username;
 	private final PasswordTextField _password;
 
	public RedmineLoginValidator(TextField<String> url,
			TextField<String> username, PasswordTextField password) {
 		_url = url;
 		_username = username;
 		_password = password;
 	}
 
 	@Override
	protected void onValidate(IValidatable<String> validatable) {
		super.onValidate(validatable);
 		try {
 			boolean login = new RedmineRepository().login(_url.getValue(),
 					_username.getValue(), _password.getValue());
 			if (!login) {
 				error(_url.newValidatable());
 			}
 		} catch (MalformedURLException e) {
 			error(_url.newValidatable());
 		} catch (IOException e) {
 			error(_url.newValidatable());
 		} catch (SAXException e) {
 			error(_url.newValidatable());
 		}
 	}
 }
