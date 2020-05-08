 package com.melexis;
 
 import org.apache.wicket.PageParameters;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PageLink;
 
 /**
  * Homepage
  */
 public class HomePage extends WebPage {
 
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Constructor that is invoked when page is invoked without a session.
 	 * 
 	 * @param parameters
 	 *            Page parameters
 	 */
 	public HomePage(final PageParameters parameters) {
 
 		// Add the simplest type of label
 		add(new Label("message", "Welcome to lunch."));
 		add(new Link("productlist") {
 
 			@Override
 			public void onClick() {
 				setResponsePage(ProductList.class);
 			}
 		});
 		add(new Link("orderproduct") {
 
 			@Override
 			public void onClick() {
 				setResponsePage(OrderProduct.class);
 			}
 		});
 		add(new Link("userprofiles") {
 
 			@Override
 			public void onClick() {
 				setResponsePage(UserProfilePage.class);
 			}
 		});
 
		add(new PageLink("logout", LogoutPage.class));

 	}
 }
