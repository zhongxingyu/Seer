 package de.mq.merchandise.controller;
 
 import java.io.IOException;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javassist.expr.NewArray;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.data.authentication.UserCredentials;
 import org.springframework.security.authentication.AbstractAuthenticationToken;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.User;
 import org.springframework.transaction.annotation.Transactional;
 
 import de.mq.mapping.util.proxy.ExceptionTranslation;
 import de.mq.mapping.util.proxy.ExceptionTranslations;
 import de.mq.merchandise.customer.Customer;
 import de.mq.merchandise.customer.CustomerRole;
 import de.mq.merchandise.customer.CustomerService;
 import de.mq.merchandise.customer.Person;
 import de.mq.merchandise.customer.PersonRole;
 import de.mq.merchandise.customer.support.LoginAO;
 import de.mq.merchandise.model.support.FacesContextFactory;
 
 
 public class LoginControllerImpl {
 	
 	
 	@Autowired
 	private   CustomerService customerService;
 	@Autowired
 	private  FacesContextFactory facesContextFactory;
 	
 	protected  LoginControllerImpl() {
 	}
 	
 	
 	
 	public LoginControllerImpl(final CustomerService customerService, final FacesContextFactory facesContextFactory){
 		this.customerService=customerService;
 		this.facesContextFactory=facesContextFactory;
 	}
 	
 	@ExceptionTranslations(value={
             @ExceptionTranslation( action = SimpleFacesExceptionTranslatorImpl.class, source = EmptyResultDataAccessException.class  , bundle="login_user_not_found" ),
             @ExceptionTranslation( action = SimpleFacesExceptionTranslatorImpl.class, source = SecurityException.class  , bundle="login_invalid_password" )
 	
 	
 	},  clazz = LoginControllerImpl.class)
 	
 	public String login(final LoginAO login ) {
 		
 		final Collection<Entry<Customer,Person>> customerEntries = customerService.login(login.getUser().toLowerCase());
         
 		final Person person = customerEntries.iterator().next().getValue();
 		if(!person.digest().check(login.getPassword())) {
 			throw new SecurityException("Wrong password, login  failed");
 		}
 		
 		login.setCustomers(customerAsList(customerEntries));
 		login.setPerson(person);
 		
 		if( customerEntries.size() > 1){
 			//login.setCustomer(new CustomerBuilderFactoryImpl().customerBuilder().build());
 			return null;
 		}
 		login.setCustomer(customerEntries.iterator().next().getKey());
 		return "overview" ;
 	}
 	
 	@ExceptionTranslations(value={
           
             @ExceptionTranslation( action = SimpleFacesExceptionTranslatorImpl.class, source = IllegalArgumentException.class  , bundle="login_customer_mandatory" )
 	
 	
 	},  clazz = LoginControllerImpl.class)
 	
 	public String assignCustomer(final LoginAO login, final Customer customer ) {
 		
 		if ( customer == null){
 			throw new IllegalArgumentException("Customer is mandatory");
 		}
 		
		
 		
 		final AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken("skype:kinkykylie" , "fever" );
 		authentication.setDetails(new AbstractMap.SimpleEntry<>(customer, login.getPersonDomain()));
 		
 		SecurityContextHolder.getContext().setAuthentication(authentication);
 		
 		return "overview?faces-redirect=true";
 	}
 	
 	public void abort(final String language) throws IOException {
 		facesContextFactory.facesContext().getExternalContext().invalidateSession();
 		facesContextFactory.facesContext().getExternalContext().redirect("login.jsf?language=" + language);
 	}
 	
 	
 
 	private List<Customer> customerAsList(final Collection<Entry<Customer, Person>> customerEntries) {
 		final List<Customer> customers = new ArrayList<>();
 		for(final Entry<Customer,Person> entry : customerEntries){
 			customers.add(entry.getKey());
 		}
 		return customers;
 	}
 	
 
 }
