 package project.client.login;
 
 import project.client.tos.TOSWidget;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /* This is the widget shown when first loading the page
  * the user must check the box, then a popup dialog is shown.
  * The user may then login and be given a microtask, or may view the terms
  * of service.
  */
 public class LoginWidget extends VerticalPanel {
 
 	private final Label loginLabel = new Label(
 			"Please sign in to your Google account");
 	private final Anchor signInLink = new Anchor("Sign in");
 	private final Anchor tos = new Anchor("Terms of Service");
 	private final LoginInfo loginInfo;
 	private AbsolutePanel absolutePanel;
 	private final PopupPanel pPanel = new PopupPanel();;
 	private boolean checked;
 	private final HTML h = new HTML();
 
 	public LoginWidget(final LoginInfo loginInfo) {
		setSize("1150px", "768px");
 		this.loginInfo = loginInfo;
 		createLogin();
 		RootPanel.get("ace").getElement().setAttribute("align", "center");
 		DOM.setStyleAttribute(RootPanel.get("ace").getElement(), "marginLeft",
 				"auto"); // These two lines center the page
 		DOM.setStyleAttribute(RootPanel.get("ace").getElement(), "marginRight",
 				"auto"); // Make sure to use RootPanel.get("ace")
 	}
 
 	public void onResize() {
 		setPixelSize(Window.getClientWidth(), Window.getClientHeight());
 		clear();
 		createLogin();
 	}
 
 	private void createLogin() {
 		VerticalPanel vPanel = new VerticalPanel(); // This vPanel gets rid of
 													// superfluous spacing
 		h.setHTML("This project is about crowdsourcing programming. We're trying to see if a complex task like "
 				+ "coding can successfully be broken up into a large number of small pieces. You can be part "
 				+ "of this project and help us test the new paradigm, especially if you have some background "
 				+ "knowledge about programming. We'll present you with small programming-related tasks (on "
 				+ "the order of seconds to minutes), and ask you to complete them as best as you can. You are "
 				+ "welcome to use reference resources that we provide, or that you can find on the Internet and "
 				+ "in other places, if you want to. You may find that you are learning as you go, and that's great. "
 				+ "As you go, you'll earn experience points, visible in the left column of this site. You can "
 				+ "use these points to highlight your contributions, on your profile page, where you're welcome to "
 				+ "share a bit about who you are and showcase some of your work. If you like to rack 'em up, you "
 				+ "can check out our leaderboard in the right column, and jockey for position there. Occasionally, we "
 				+ "may offer raffles and other special rewards, and your experience points will be redeemable as "
 				+ "entries. You are not obligated to complete any particular task, and you can stop at any time. "
 				+ "If you go on to do something awesome with the knowledge you gained here, or if you get a "
 				+ "job/promotion/connection/recognition based on it we'd love to hear about it. If you have any "
 				+ "questions about this study, now or later, you can <a href=mailto:\"crowdcodingstudy@gmail.com\">email us</a>. If this "
 				+ "sounds good to you, check the box and login to your Google account below to get started! "
 				+ "We look forward to you being part of the project.");
 		absolutePanel = new AbsolutePanel();
 		Label lblCrowdcoding = new Label("Crowd Coding"); // main title
 		lblCrowdcoding.setHeight("30px");
 		vPanel.add(lblCrowdcoding);
 		lblCrowdcoding.setStyleName("gwt-Title");
 		lblCrowdcoding
 				.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_LEFT);
 
 		tos.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 		absolutePanel.add(tos, 0, 86);
 		tos.setSize("408px", "20px");
 		vPanel.add(h);
		vPanel.setSize("1150px", "250px");
 		add(vPanel);
 
 		CheckBox chckbxNewCheckBox = new CheckBox(
 				"I am 18 years of age or older and have read and understood the text");
 		chckbxNewCheckBox.setDirectionEstimator(false);
 		vPanel.add(chckbxNewCheckBox);
 		chckbxNewCheckBox.addClickHandler(new ClickHandler() { // Using checkbox
 					// like a button
 					// w/ click
 					// handler
 					@Override
 					public void onClick(ClickEvent event) {
 						pPanel.setWidget(absolutePanel);
 						checked = !checked; // toggle checkbox value
 						pPanel.setAnimationEnabled(true); // these two lines
 															// make the popup
 															// look nice
 						pPanel.setGlassEnabled(true);
 						absolutePanel.setSize("408px", "126px");
 						loginLabel
 								.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 						absolutePanel.add(loginLabel, 0, 0);
 						loginLabel.setStyleName("dialogVPanel");
 						loginLabel.setSize("398px", "53px");
 						signInLink
 								.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 						signInLink.setHref(loginInfo.getLoginUrl());
 						absolutePanel.add(signInLink, 0, 47);
 						signInLink.setSize("408px", "33px");
 						tos.addClickHandler(new ClickHandler() {
 							@Override
 							public void onClick(ClickEvent event) {
 								pPanel.hide();
 								System.out.println("step1");
 								RootPanel.get("ace").clear();
 								RootPanel.get("ace").add(
 										new TOSWidget(loginInfo)); // clear
 																	// panel and
 																	// add new
 																	// page (how
 																	// we do all
 																	// page
 																	// changes)
 								System.out.println("step2");
 							}
 						});
 						if (checked)
 							pPanel.center(); // shows and centers popup window
 						else {
 							pPanel.hide();
 						}
 					}
 				});
 	}
 }
