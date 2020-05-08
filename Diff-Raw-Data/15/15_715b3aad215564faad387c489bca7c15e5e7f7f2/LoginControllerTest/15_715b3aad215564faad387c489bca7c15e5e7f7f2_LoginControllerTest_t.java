 package de.mq.merchandise.controller;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 
 import junit.framework.Assert;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mockito;
 
 import de.mq.merchandise.customer.Customer;
 import de.mq.merchandise.customer.CustomerService;
 import de.mq.merchandise.customer.Person;
 import de.mq.merchandise.customer.support.Digest;
 import de.mq.merchandise.customer.support.LoginAO;
 import de.mq.merchandise.model.support.FacesContextFactory;
 
 public class LoginControllerTest {
 
 	private static final String LANGUAGE = "de";
 	private static final String PASSWORD = "fever";
 	private static final String LOGIN = "skype:kinkyKylie";
 	private CustomerService customerService = Mockito.mock(CustomerService.class);
 	private final FacesContextFactory facesContextFactory = Mockito.mock(FacesContextFactory.class);
     private final LoginControllerImpl loginController = new LoginControllerImpl(customerService, facesContextFactory);
     private final LoginAO loginAO = Mockito.mock(LoginAO.class);
     @SuppressWarnings("rawtypes")
     final private ArgumentCaptor<List> customerListCaptor = ArgumentCaptor.forClass(List.class);
 	
 	private final Set<Entry<Customer,Person>> customers = new HashSet<>();
 	@SuppressWarnings("unchecked")
 	private final Entry<Customer, Person> entry = Mockito.mock(Entry.class);
 	private final Person person = Mockito.mock(Person.class);
 	private final Digest digest = Mockito.mock(Digest.class);
 	private final Customer customer = Mockito.mock(Customer.class);
 	
     @Before
 	public final void init() {
     	Mockito.when(person.digest()).thenReturn(digest);
 		
 		
 		Mockito.when(entry.getKey()).thenReturn(customer);
 		Mockito.when(entry.getValue()).thenReturn(person);
 		customers.add(entry);
 		
 		
 		Mockito.when(loginAO.getUser()).thenReturn(LOGIN);
 	    Mockito.when(loginAO.getPassword()).thenReturn(PASSWORD);
 		Mockito.when(customerService.login(LOGIN.toLowerCase())).thenReturn(customers);
 	}
 	
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public final void loginSingleCustomerReturned() {
 		Mockito.when(digest.check(PASSWORD)).thenReturn(true);
 		Assert.assertEquals("overview", loginController.login(loginAO));
 		Mockito.verify(loginAO).setPerson(person);
 		
 		
 		
 		Mockito.verify(loginAO).setCustomers(customerListCaptor.capture());
 		
 		Assert.assertEquals(1, customerListCaptor.getValue().size());
 		Assert.assertEquals(customer, customerListCaptor.getValue().get(0));
 		
 		Mockito.verify(loginAO).setCustomer(customer);
 		Mockito.verify(loginAO).getUser();
 		Mockito.verify(loginAO).getPassword();
 		Mockito.verifyNoMoreInteractions(loginAO);
 	}
 	
 	@Test(expected=SecurityException.class)
 	public final void loginWrongPassword() {
 		loginController.login(loginAO);
 	}
 	
 	@Test
 	@SuppressWarnings("unchecked")
 	public final void loginMultipleCustomerReturned() {
 		Mockito.when(digest.check(PASSWORD)).thenReturn(true);
 		final Entry<Customer,Person> entry = Mockito.mock(Entry.class);
 		Mockito.when(entry.getValue()).thenReturn(person);
 		final Customer customer = Mockito.mock(Customer.class);
 		Mockito.when(entry.getKey()).thenReturn(customer);
 		customers.add(entry);
 		Assert.assertNull(loginController.login(loginAO));
 		
 		Mockito.verify(loginAO).setPerson(person);
 		Mockito.verify(loginAO).setCustomers(customerListCaptor.capture());
 		
 		Assert.assertEquals(2, customerListCaptor.getValue().size());
 		
 		customerListCaptor.getValue().contains(customer);
 		customerListCaptor.getValue().contains(this.customer);
 		Mockito.verify(loginAO).getUser();
 		Mockito.verify(loginAO).getPassword();
 		Mockito.verifyNoMoreInteractions(loginAO);
 	}
 	
 	
 	@Test
 	public final void assignCustomer() {
 		final Customer customer = Mockito.mock(Customer.class);
		Assert.assertEquals("overview?faces-redirect=true", loginController.assignCustomer(loginAO, customer));
 		Mockito.verify(loginAO).setCustomer(customer);
 	}
 	
 	@Test(expected=IllegalArgumentException.class)
 	public final void assignCustomerNullCustomer() {
 		loginController.assignCustomer(loginAO,null);
 	}
 	
 	@Test
 	public final void abort() throws IOException  {
 		final FacesContext facesContext = Mockito.mock(FacesContext.class);
 		Mockito.when(facesContextFactory.facesContext()).thenReturn(facesContext);
 		final ExternalContext externalContext = Mockito.mock(ExternalContext.class);
 		Mockito.when(facesContext.getExternalContext()).thenReturn(externalContext);
 		loginController.abort(LANGUAGE);
 		
 		Mockito.verify(externalContext).invalidateSession();
 		Mockito.verify(externalContext).redirect("login.jsf?language=" + LANGUAGE );
 	}
 	
 	@Test
 	public final void defaultConstructorCoverageOnly() {
 		Assert.assertNotNull(new LoginControllerImpl());
 	}
 }
