 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.demo.server;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.demo.common.Address;
 import org.eclipse.riena.demo.common.Contract;
 import org.eclipse.riena.demo.common.Customer;
 
 /**
  * Implementation for accessing Customer Records
  */
 public class CustomerRepository implements ICustomerRepository {
 
 	private final static String[] STATES = { "NEW", "PENDING", "CLOSED", "CANCELED" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 
 	private final static DateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy"); //$NON-NLS-1$
 
 	private final List<Customer> customers = new ArrayList<Customer>();
 	private final Map<String, Customer> email2customer = new HashMap<String, Customer>();
 
 	public CustomerRepository() {
 		System.out.println("repository started"); //$NON-NLS-1$
 		init();
 	}
 
 	public List<Customer> search(final String lastName) {
 		if (StringUtils.isEmpty(lastName)) {
 			return customers;
 		}
 		final String pattern = lastName.toLowerCase();
 		final List<Customer> result = new ArrayList<Customer>();
 		for (final Customer customer : customers) {
 			if (customer.getLastName().toLowerCase().contains(pattern)) {
 				result.add(customer);
 			}
 
 		}
 		return result;
 	}
 
 	public void store(final Customer customer) {
 		customers.add(customer);
 		if (customer.getEmailAddress() != null) {
 			email2customer.put(customer.getEmailAddress(), customer);
 		}
 
 	}
 
 	public Customer findCustomerWithEmailAddress(final String emailAddress) {
 		return email2customer.get(emailAddress);
 	}
 
 	private void init() {
 		try {
 			Customer crv = new Customer();
 			// 1.
 			crv.setLastName("Mundl"); //$NON-NLS-1$
 			crv.setFirstName("Josef"); //$NON-NLS-1$
 			crv.setBirthDate(DATE_FORMAT.parse("01/01/1950")); //$NON-NLS-1$
 			crv.getAddress().setStreet("Musterstr.1"); //$NON-NLS-1$
 			crv.getAddress().setZipCode("12345"); //$NON-NLS-1$
 			crv.getAddress().setCity("Musterstadt"); //$NON-NLS-1$
 			crv.setEmailAddress("Josef.Mundl@mail.org"); //$NON-NLS-1$
			addWorkAddress(crv);
 			customers.add(crv);
 			if (crv.getEmailAddress() != null) {
 				email2customer.put(crv.getEmailAddress(), crv);
 			}
 			// 2.
 			crv = new Customer();
 			crv.setLastName("Muster"); //$NON-NLS-1$
 			crv.setFirstName("Robert"); //$NON-NLS-1$
 			crv.setBirthDate(DATE_FORMAT.parse("01/04/1955")); //$NON-NLS-1$
 			crv.getAddress().setStreet("Willibaldgasse"); //$NON-NLS-1$
 			crv.getAddress().setZipCode("12366"); //$NON-NLS-1$
 			crv.getAddress().setCity("Musterhaus"); //$NON-NLS-1$
 			crv.setEmailAddress("Robert.Muster@mail.org"); //$NON-NLS-1$
			addWorkAddress(crv);
 			customers.add(crv);
 			if (crv.getEmailAddress() != null) {
 				email2customer.put(crv.getEmailAddress(), crv);
 			}
 			// 3.
 			crv = new Customer();
 			crv.setLastName("Muster-Maier"); //$NON-NLS-1$
 			crv.setFirstName("Trulli"); //$NON-NLS-1$
 			crv.setBirthDate(DATE_FORMAT.parse("21/06/1964")); //$NON-NLS-1$
 			crv.getAddress().setStreet("Willibaldgasse"); //$NON-NLS-1$
 			crv.getAddress().setZipCode("12345"); //$NON-NLS-1$
 			crv.getAddress().setCity("Musterstadt"); //$NON-NLS-1$
 			crv.setEmailAddress("Trulli.Muster-Maier@mail.org"); //$NON-NLS-1$
			addWorkAddress(crv);
 			customers.add(crv);
 			if (crv.getEmailAddress() != null) {
 				email2customer.put(crv.getEmailAddress(), crv);
 			}
 			// 4.
 			crv = new Customer();
 			crv.setLastName("Mustermann"); //$NON-NLS-1$
 			crv.setFirstName("Elfriede"); //$NON-NLS-1$
 			crv.setBirthDate(DATE_FORMAT.parse("01/04/1955")); //$NON-NLS-1$
 			crv.getAddress().setStreet("Musterstr.1"); //$NON-NLS-1$
 			crv.getAddress().setZipCode("12345"); //$NON-NLS-1$
 			crv.getAddress().setCity("Musterstadt"); //$NON-NLS-1$
 			crv.setEmailAddress("Elfriede.Mustermann@mail.org"); //$NON-NLS-1$
			addWorkAddress(crv);
 			customers.add(crv);
 			if (crv.getEmailAddress() != null) {
 				email2customer.put(crv.getEmailAddress(), crv);
 			}
 			// 5.
 			crv = new Customer();
 			crv.setLastName("Mustermann"); //$NON-NLS-1$
 			crv.setFirstName("Ingo"); //$NON-NLS-1$
 			crv.setBirthDate(DATE_FORMAT.parse("01/01/1950")); //$NON-NLS-1$
 			crv.getAddress().setStreet("Musterstr.1"); //$NON-NLS-1$
 			crv.getAddress().setZipCode("12345"); //$NON-NLS-1$
 			crv.getAddress().setCity("Musterstadt"); //$NON-NLS-1$
 			crv.setEmailAddress("Ingo.Mustermann@mail.org"); //$NON-NLS-1$
			addWorkAddress(crv);
 			customers.add(crv);
 			if (crv.getEmailAddress() != null) {
 				email2customer.put(crv.getEmailAddress(), crv);
 			}
 		} catch (final ParseException p) {
 			System.out.println(p.getMessage());
 		}
 		for (final Customer customer : customers) {
 			addContractEntries(customer);
 		}
 	}
 
 	public void addContractEntries(final Customer customer) {
 		for (int i = 0; i < 3; i++) {
 			final String status = STATES[new Random().nextInt(STATES.length)];
 			final String contractNo = String.valueOf(Math.abs(new Random().nextInt(10000)));
 			final double nextDouble = new Random().nextDouble() * 1000000;
 			final double contractValue = Math.round(nextDouble * 100d) / 100d;
 			customer.addContract(new Contract(contractNo, "contract desc for " + contractNo, contractValue, status)); //$NON-NLS-1$
 		}
 	}
 
 	public void addWorkAddress(final Customer customer) {
 		final Address workAddress = new Address();
 		workAddress.setCity("Frankfurt"); //$NON-NLS-1$
 		workAddress.setStreet((customer.getAddress().getStreet()));
 		workAddress.setZipCode((customer.getAddress().getZipCode()));
		customer.setWorkAddress(workAddress);
 	}
 }
