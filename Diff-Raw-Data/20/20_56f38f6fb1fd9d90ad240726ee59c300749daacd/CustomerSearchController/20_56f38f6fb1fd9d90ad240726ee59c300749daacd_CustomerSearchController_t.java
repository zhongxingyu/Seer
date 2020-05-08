 /*******************************************************************************
  * Copyright (c) 2007, 2010 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.demo.client.controllers;
 
 import java.util.Date;
 
 import org.eclipse.riena.core.wire.InjectService;
 import org.eclipse.riena.demo.common.Customer;
 import org.eclipse.riena.demo.common.ICustomerService;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ILabelRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.DateColumnFormatter;
 
 /**
  * customer search view controller
  */
 public class CustomerSearchController extends SubModuleController {
 
 	private ICustomerService customerDemoService;
 
 	@InjectService(useRanking = true)
 	public void bind(final ICustomerService customerDemoService) {
 		this.customerDemoService = customerDemoService;
 	}
 
 	public void unbind(final ICustomerService customerDemoService) {
 		this.customerDemoService = null;
 	}
 
 	private final SearchBean customerSearchBean = new SearchBean();
 	private final SearchResult result = new SearchResult();
 
 	@Override
 	public void configureRidgets() {
 		final ITextRidget suchName = (ITextRidget) getRidget("searchLastName"); //$NON-NLS-1$
 		suchName.bindToModel(customerSearchBean, "lastName"); //$NON-NLS-1$
 		suchName.setMandatory(true);
 
 		((ILabelRidget) getRidget("hits")).bindToModel(result, "hits"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		final ITableRidget kunden = ((ITableRidget) getRidget("result")); //$NON-NLS-1$
 		final String[] columnNames = { "lastname", "firstname", "birthdate", "street", "city" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 		final String[] propertyNames = { "lastName", "firstName", "birthDate", "address.street", "address.city" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 
 		kunden.bindToModel(result, "customers", Customer.class, propertyNames, columnNames); //$NON-NLS-1$
 
 		kunden.setColumnFormatter(2, new DateColumnFormatter("dd.MM.yyyy") { //$NON-NLS-1$
 
 					@Override
 					protected Date getDate(final Object element) {
 						return ((Customer) element).getBirthDate();
 					}
 				});
 
 		((IActionRidget) getRidget("search")).addListener(new IActionListener() { //$NON-NLS-1$
 					public void callback() {
 						result.setCustomers(null);
 						getRidget("result").updateFromModel(); //$NON-NLS-1$
 
						result.setCustomers(customerDemoService.search(customerSearchBean.getLastName()));
 
 						getRidget("result").updateFromModel(); //$NON-NLS-1$
 						getRidget("hits").updateFromModel(); //$NON-NLS-1$
 					}
 				});
 
 		((IActionRidget) getRidget("new")).addListener(new IActionListener() { //$NON-NLS-1$
 					public void callback() {
 						getNavigationNode().navigate(new NavigationNodeId("riena.demo.client.CustomerRecord")); //$NON-NLS-1$
 
 					}
 				});
 
 		((IActionRidget) getRidget("open")).addListener(new IActionListener() { //$NON-NLS-1$
 					public void callback() {
 						final int selectionIndex = kunden.getSelectionIndex();
 						if (selectionIndex >= 0) {
 							getNavigationNode()
 									.navigate(
 											new NavigationNodeId(
 													"riena.demo.client.CustomerRecord", result.getCustomers().get(selectionIndex).getEmailAddress()), //$NON-NLS-1$
 											new NavigationArgument(result.getCustomers().get(selectionIndex)));
 						}
 					}
 				});
 	}
 }
