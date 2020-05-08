 package com.huskycode.jpaquery.populator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.huskycode.jpaquery.link.Attribute;
 import com.huskycode.jpaquery.link.AttributeImpl;
 import com.huskycode.jpaquery.testmodel.pizza.Customer;
 
 public class ValuesPopulatorImplTest {
 	@Test
 	public void testValueIsPopulatedForTheGivenAttributeValues() throws SecurityException, NoSuchFieldException {
 		ValuesPopulatorImpl populator = ValuesPopulatorImpl.getInstance();
 		
 		Long expectedValue = 1L;
 		Attribute<Customer, Long> customerIdAttr = AttributeImpl.newInstance(Customer.class,
 														Customer.class.getDeclaredField("customerId"));
 		AttributeValue<Customer, Long> customerIdValue = AttributeValue.newInstance(customerIdAttr, expectedValue);
 		List<AttributeValue<Customer, ?>> attributeValues
 			= new ArrayList<AttributeValue<Customer, ?>>();
 		attributeValues.add(customerIdValue);
 		
 		Customer customer = new Customer();
 		populator.populateValue(customer, attributeValues);	
 		
		Assert.assertEquals(expectedValue, customer.getCustomerId());	
 	}
 }
