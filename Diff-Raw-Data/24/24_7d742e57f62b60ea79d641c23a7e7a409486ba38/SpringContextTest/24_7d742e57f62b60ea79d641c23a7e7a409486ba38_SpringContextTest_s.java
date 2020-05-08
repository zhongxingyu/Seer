 package com.rhc.spring;
 
 import java.math.BigDecimal;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 import org.junit.runner.Result;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import com.rhc.drools.reference.StatelessDroolsComponent;
 import com.rhc.mortgage.application.MortgageApplicationRequest;
 import com.rhc.mortgage.application.MortgageApplicationResponse;
 import com.rhc.mortgage.application.MortgageServiceTest;
 import com.rhc.mortgage.domain.Application;
 import com.rhc.mortgage.domain.Customer;
 import com.rhc.mortgage.domain.Mortgage;
 
 /**
  * This will load a spring context definition defined on the classpath at com.rhc.spring.SpringContextTest-context.xml
  */
 @ContextConfiguration
 public class SpringContextTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private static StatelessDroolsComponent droolsComponent;
		
	public void setDroolsComponent(StatelessDroolsComponent droolsComponent){
		SpringContextTest.droolsComponent = droolsComponent;
	}
	
	@Test
	public void test() {
		System.out.println(droolsComponent.getClass());
		Assert.assertNotNull("droolsComponent should not be null", droolsComponent);
	}
 	
 	private final static Long CUSTOMER_ID_1 = Long.valueOf( 1 );
 	private final static Long CUSTOMER_ID_2 = Long.valueOf( 2 );
 	private final static Long CUSTOMER_ID_3 = Long.valueOf( 3 );
 
 	private final static Long APPLICATION_ID_1 = Long.valueOf( 1 );
 	private final static Long APPLICATION_ID_2 = Long.valueOf( 2 );
 	private final static Long APPLICATION_ID_3 = Long.valueOf( 3 );
 
 	/*
 	 * This is just to test that my query worked
 	 */
 	@Test
 	public void shouldApproveCustomerAndApplicationAndCreateOneMortgage() {
 		Application application = new Application( new BigDecimal( 9000 ), 1L, 10L );
 		Customer customer = new Customer( "Bob", 25, 700, 1L );
 
 		HashSet<Application> applications = new HashSet<Application>();
 		applications.add( application );
 
 		HashSet<Customer> customers = new HashSet<Customer>();
 		customers.add( customer );
 
 		MortgageApplicationRequest request = new MortgageApplicationRequest( applications, customers );
 
 		MortgageApplicationResponse response = (MortgageApplicationResponse) droolsComponent.execute( request );
 
 		Assert.assertTrue( response != null );
 
 		Collection<Application> approvedApplications = response.getApprovedApplications();
 		Assert.assertTrue( approvedApplications.size() == 1 );
 
 		Collection<Mortgage> mortgages = response.getNewMortgagesCreated();
 		Assert.assertTrue( mortgages.size() == 1 );
 	}
 
 	@Test
 	public void shouldApproveCustomer1and2DeniedCustomer3() {
 		MortgageApplicationRequest request = new MortgageApplicationRequest( createApplications(), createCustomers() );
 
 		MortgageApplicationResponse response = (MortgageApplicationResponse) droolsComponent.execute( request );
 
 		Assert.assertNotNull( response );
 
 		Assert.assertEquals( 2, response.getApprovedApplications().size() );
 
 		// TODO how do I do this cleaner?
 		boolean containsCustomer1 = false;
 		boolean containsCustomer2 = false;
 		boolean containsCustomer3 = false;
 		for ( Application application : response.getApprovedApplications() ) {
 			if ( application.getCustomerId() == 1L ) {
 				containsCustomer1 = true;
 			} else if ( application.getCustomerId() == 2L ) {
 				containsCustomer2 = true;
 			}
 		}
 		Assert.assertTrue( containsCustomer1 );
 		Assert.assertTrue( containsCustomer2 );
 		Assert.assertFalse( containsCustomer3 );
 
 		Assert.assertEquals( 1, response.getDeniedApplications().size() );
 
 		containsCustomer1 = false;
 		containsCustomer2 = false;
 		containsCustomer3 = false;
 		for ( Application application : response.getDeniedApplications() ) {
 			if ( application.getCustomerId() == 3L ) {
 				containsCustomer3 = true;
 			}
 		}
 		Assert.assertFalse( containsCustomer1 );
 		Assert.assertFalse( containsCustomer2 );
 		Assert.assertTrue( containsCustomer3 );
 	}
 
 	private Set<Customer> createCustomers() {
 
 		Set<Customer> customers = new HashSet<Customer>();
 		customers.add( new Customer( "Dave", 20, 700, CUSTOMER_ID_1 ) );
 		customers.add( new Customer( "Mike", 18, 750, CUSTOMER_ID_2 ) );
 		customers.add( new Customer( "Bryan", 28, 450, CUSTOMER_ID_3 ) );
 
 		return customers;
 	}
 
 	private Set<Application> createApplications() {
 
 		Set<Application> applications = new HashSet<Application>();
 		applications.add( new Application( new BigDecimal( 15000 ), CUSTOMER_ID_1, APPLICATION_ID_1 ) );
 		applications.add( new Application( new BigDecimal( 75000 ), CUSTOMER_ID_3, APPLICATION_ID_2 ) );
 		applications.add( new Application( new BigDecimal( 85000 ), CUSTOMER_ID_2, APPLICATION_ID_3 ) );
 
 		return applications;
 	}
 		
 }
