 package com.madalla.webapp.admin.pages;
 
 import com.madalla.webapp.admin.AbstractAdminPage;
 
 public class AdminErrorPage extends AbstractAdminPage {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Constructor.
 	 */
 	public AdminErrorPage() {
 		add(homePageLink("homePageLink"));
 	}
 
 
 	/**
 	 * @see org.apache.wicket.Component#isVersioned()
 	 */
 	@Override
 	public boolean isVersioned() {
 		return false;
 	}
 
 	/**
 	 * @see org.apache.wicket.Page#isErrorPage()
 	 */
 	@Override
 	public boolean isErrorPage() {
 		return true;
 	}
 
 }
