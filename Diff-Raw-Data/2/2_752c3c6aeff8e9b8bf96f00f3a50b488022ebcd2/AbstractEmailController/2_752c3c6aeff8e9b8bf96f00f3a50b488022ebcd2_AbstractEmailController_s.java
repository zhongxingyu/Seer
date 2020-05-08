 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.demo.client.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import org.eclipse.riena.beans.common.TypedComparator;
 import org.eclipse.riena.core.wire.InjectService;
 import org.eclipse.riena.demo.common.Customer;
 import org.eclipse.riena.demo.common.Email;
 import org.eclipse.riena.demo.common.ICustomerService;
 import org.eclipse.riena.demo.common.IEmailService;
 import org.eclipse.riena.navigation.NavigationArgument;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ILabelRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 
 /**
  * abstract email controller
  */
 public class AbstractEmailController extends SubModuleController {
 
 	protected IEmailService mailDemoService;
 	protected Email selectedEmail;
 
 	@InjectService(useRanking = true)
 	public void bind(IEmailService mailDemoService) {
 		this.mailDemoService = mailDemoService;
 	}
 
 	public void unbind(IEmailService mailDemoService) {
 		this.mailDemoService = null;
 	}
 
 	private ICustomerService customerDemoService;
 
 	@InjectService(useRanking = true)
 	public void bind(ICustomerService customerDemoService) {
 		this.customerDemoService = customerDemoService;
 	}
 
 	public void unbind(ICustomerService customerDemoService) {
 		this.customerDemoService = null;
 	}
 
 	protected EmailsResult emailsResult = new EmailsResult();
 
 	/*
 	 * @seeorg.eclipse.riena.navigation.ui.controllers.SubModuleController#
 	 * configureRidgets()
 	 */
 	@Override
 	public void configureRidgets() {
 		final ITableRidget emails = (ITableRidget) getRidget("emailsTable"); //$NON-NLS-1$
 		final ILabelRidget emailSubject = (ILabelRidget) getRidget("emailSubject"); //$NON-NLS-1$
 		final ILabelRidget emailFrom = (ILabelRidget) getRidget("emailFrom"); //$NON-NLS-1$
 		final ILabelRidget emailTo = (ILabelRidget) getRidget("emailTo"); //$NON-NLS-1$
 		final ILabelRidget emailDate = (ILabelRidget) getRidget("emailDate"); //$NON-NLS-1$
 		final ITextRidget emailBody = (ITextRidget) getRidget("emailBody"); //$NON-NLS-1$
 
 		emails.setComparator(3, new TypedComparator<Date>());
 		emails.addPropertyChangeListener(new PropertyChangeListener() {
 
 			public void propertyChange(PropertyChangeEvent evt) {
 				if (evt.getPropertyName() == "selection") { //$NON-NLS-1$
 					selectedEmail = (Email) emails.getSelection().get(0);
 					emailSubject.setText(selectedEmail.getEmailSubject());
 					emailFrom.setText(selectedEmail.getEmailFrom());
 					emailBody.setText(selectedEmail.getEmailBody());
 					emailTo.setText(selectedEmail.getEmailTo());
 					DateFormat formatter;
 					formatter = new SimpleDateFormat("E dd.MM.yyyy HH:mm"); //$NON-NLS-1$
 					emailDate.setText(formatter.format(selectedEmail.getEmailDate()));
 
 				}
 			}
 		});
 
 		if (getNavigationNode().isJumpTarget()) {
 			final IActionRidget openCustomerAction = (IActionRidget) getRidget("openCustomer");
 			openCustomerAction.setText("Back to Customer");
 			openCustomerAction.addListener(new IActionListener() { //$NON-NLS-1$
 						public void callback() {
 							getNavigationNode().jumpBack();
							getNavigationNode().dispose();
 						}
 					});
 
 		} else {
 
 			((IActionRidget) getRidget("openCustomer")).addListener(new IActionListener() { //$NON-NLS-1$
 						public void callback() {
 							if (selectedEmail != null) {
 								String selectedEmailAddress = openCustomerWithEmailAddress();
 								if (selectedEmailAddress != null) {
 									Customer customer = customerDemoService
 											.findCustomerWithEmailAddress(selectedEmailAddress);
 
 									System.out.println("customer " + customer); //$NON-NLS-1$
 
 									if (customer != null) {
 										getNavigationNode().navigate(
 												new NavigationNodeId(
 														"riena.demo.client.CustomerRecord", selectedEmailAddress), //$NON-NLS-1$
 												new NavigationArgument(customer));
 									}
 								}
 							}
 						}
 					});
 		}
 
 	}
 
 	/**
 	 * @return the email address of the customer that should be opened
 	 */
 	protected String openCustomerWithEmailAddress() {
 		return selectedEmail.getEmailFrom();
 	}
 }
