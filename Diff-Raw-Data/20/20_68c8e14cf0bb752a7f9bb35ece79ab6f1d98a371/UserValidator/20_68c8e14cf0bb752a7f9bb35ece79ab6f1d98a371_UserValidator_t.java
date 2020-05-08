 package br.com.zeng.validator;
 
 import br.com.caelum.vraptor.Validator;
 import br.com.caelum.vraptor.ioc.Component;
 import br.com.caelum.vraptor.validator.ValidationMessage;
 import br.com.zeng.controller.UserController;
 import br.com.zeng.model.User;
 
 @Component
 public class UserValidator {
 
 	private final Validator validator;
 
 	public UserValidator(Validator validator) {
 		this.validator = validator;
 	}
 
 	public void validate(User user) {
 		if(user == null) {
 			validator.add(new ValidationMessage("Usuario invalido", "error"));
 		}
		validator.onErrorRedirectTo(UserController.class).home();
 	}
 }
