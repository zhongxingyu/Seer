 package de.mq.merchandise.controller;
 
 import javax.faces.context.FacesContext;
 
 import org.springframework.dao.EmptyResultDataAccessException;
 
 import de.mq.mapping.util.proxy.ActionEvent;
 import de.mq.mapping.util.proxy.ExceptionTranslation;
 import de.mq.mapping.util.proxy.MethodInvocation;
 import de.mq.mapping.util.proxy.Parameter;
 import de.mq.merchandise.customer.Customer;
 import de.mq.merchandise.customer.Person;
 import de.mq.merchandise.customer.support.LoginAO;
 import de.mq.merchandise.model.User;
 
 public interface LoginController {
 	
 	@MethodInvocation(value={
        @ExceptionTranslation( action = SimpleFacesExceptionTranslatorImpl.class, source = EmptyResultDataAccessException.class  , bundle="login_user_not_found" ),
        @ExceptionTranslation( action = SimpleFacesExceptionTranslatorImpl.class, source = SecurityException.class  , bundle="login_invalid_password" )
 	},  clazz = LoginControllerImpl.class, actions={@ActionEvent(params={@Parameter(clazz=LoginAO.class)})})
 	
 	String login();
 	
 	
 	@MethodInvocation(value={
         @ExceptionTranslation( action = SimpleFacesExceptionTranslatorImpl.class, source = IllegalArgumentException.class  , bundle="login_customer_mandatory" )
	},  clazz = LoginControllerImpl.class, actions={@ActionEvent(params={@Parameter(clazz=LoginAO.class , el="#arg.person.person", elResultType=Person.class), @Parameter(clazz=LoginAO.class, el="#arg.customer.customer", elResultType=Customer.class), @Parameter(clazz=LoginAO.class, el="#arg.password" , elResultType=String.class)})})
 	
 	String assignCustomer();
 	
 	@MethodInvocation(clazz=LoginControllerImpl.class, actions={@ActionEvent(params={@Parameter(clazz=User.class, el="#arg.language" , elResultType=String.class), @Parameter(clazz=FacesContext.class)})})
 	public void abort(); 
 
 }
