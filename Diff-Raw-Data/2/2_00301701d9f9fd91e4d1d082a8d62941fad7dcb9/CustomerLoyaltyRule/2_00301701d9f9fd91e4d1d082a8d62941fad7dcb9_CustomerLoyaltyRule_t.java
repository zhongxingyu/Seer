 package com.mckinsey.exercise.rules;
 
 import org.joda.time.DateTime;
 import org.joda.time.Years;
 
 import com.mckinsey.exercise.domain.Order;
 
 /**
  * Business rule to test whether customer is loyal.
  */
 public class CustomerLoyaltyRule {
 
 	public static final int YEARS_REQUIRED_TO_BECOME_LOYAL_CUSTOMER = 2;
 
 	public boolean isCustomerLoyaltyApplicable(Order order) {
 		int registrationDurationInYears = Years.yearsBetween(
 				new DateTime(order.getUser().getRegistrationDate()),
 				new DateTime(order.getOrderDate())).getYears();
		return (registrationDurationInYears >= YEARS_REQUIRED_TO_BECOME_LOYAL_CUSTOMER);
 	}
 }
