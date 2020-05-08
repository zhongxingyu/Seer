 package project.client.login;
 
 
 import project.client.tos.TOSWidget;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.LayoutPanel;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.HasHorizontalAlignment;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.RichTextArea;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 
 public class LoginWidget extends LayoutPanel{
 	
 	private Label loginLabel = new Label("Please sign in to your Google account");
 	private Anchor signInLink = new Anchor("Sign in");
 	private Anchor tos = new Anchor("Terms of Service");
 	private LoginInfo loginInfo;
 	private int width, height;
 	private AbsolutePanel absolutePanel;
 	private PopupPanel pPanel = new PopupPanel();;
 	private boolean checked;
 	
 	public LoginWidget(final LoginInfo loginInfo){
 		//setSize("1024px","768px");
 		width=Window.getClientWidth();
 		height=Window.getClientHeight();
 		setSize("1150px", "768px");
 		this.loginInfo=loginInfo;
 		createLogin();
 		
 	}
 	public void onResize(){
 		setPixelSize(Window.getClientWidth(), Window.getClientHeight());
 		clear();
 		createLogin();
 	}
 
 	
 	private void createLogin(){
 		TextArea area = new TextArea();
 		DOM.setStyleAttribute(area.getElement(), "border", "1px");  //removes border
 		DOM.setStyleAttribute(area.getElement(), "width", "100%");  //fixes size error on firefox
 		DOM.setStyleAttribute(area.getElement(), "height", "80%");
 
 		System.out.println("updated");
 		area.setReadOnly(true);
 		area.setText(
 			"Here is example advertisement text for our study, which may be supplemented with or\n"+
 			"replaced by text from the consent document description.\n "+
 			"Come code with us! Program in small steps and help explore a new paradigm of\n"+
 			"crowdsourcing coding. Complete only as many microtasks as you want, earn points, gain\n"+
 			"experience, and help build something cool. Sign up today, at <URL>.\n\n\n\n\n\n"+
 			"This project is about crowdsourcing programming. We're trying to see if a complex task like "+
 			"coding can successfully be broken up into a large number of small pieces. You can be part "+
 			"of this project and help us test the new paradigm, especially if you have some background "+
 			"knowledge about programming. We'll present you with small programming-related tasks (on "+
 			"the order of seconds to minutes), and ask you to complete them as best as you can. You are "+
 			"welcome to use reference resources that we provide, or that you can find on the Internet and "+
 			"in other places, if you want to. You may find that you are learning as you go, and that's great. "+
 			"As you go, you'll earn experience points, visible in the <corner location> of this site. You can "+
 			"use these points to highlight your contributions, on your profile page, where you're welcome to "+
 			"share a bit about who you are and showcase some of your work. If you like to rack 'em up, you "+
 			"can check out our <link>leaderboards</link> and jockey for position there. Occasionally, we "+
 			"may offer raffles and other special rewards, and your experience points will be redeemable as "+
 			"entries. You are not obligated to complete any particular task, and you can stop at any time. "+
 			"If you go on to do something awesome with the knowledge you gained here, or if you get a "+
 			"job/promotion/connection/recognition based on we'd love to hear about it. If you have any "+
 			"questions about this study, now or later, you can ask us at <project e-mail address>. If this "+
 			"sounds good to you, click the <\"button\" or \"link\" depending on media> below to get started! "+ 
 			"We look forward to you being part of the project.\n\n"+
 
 			"Version 8/ 2009");
 		
 		absolutePanel = new AbsolutePanel();
 		
 	    
 	    tos.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 	    absolutePanel.add(tos, 0, 86);
 	    tos.setSize("408px", "20px");
 	    add(area);
 	    setWidgetLeftWidth(area, 250.0, Unit.PX, 1150.0, Unit.PX);
 	    setWidgetTopHeight(area, 100.0, Unit.PX, 491.0, Unit.PX);
 	    
 	    CheckBox chckbxNewCheckBox = new CheckBox("I am 18 years of age or older and have read and understood the text");
 	    chckbxNewCheckBox.setDirectionEstimator(false);
 	    add(chckbxNewCheckBox);
 	    setWidgetLeftWidth(chckbxNewCheckBox, 250.0, Unit.PX, 1150.0, Unit.PX);
 	    setWidgetTopHeight(chckbxNewCheckBox, 525.0, Unit.PX, 21.0, Unit.PX);
 		//pPanel.add(absolutePanel);
 	    chckbxNewCheckBox.addClickHandler(new ClickHandler(){
 	    	public void onClick(ClickEvent event){
 	    		
 	    		pPanel.setWidget(absolutePanel);
 	    		checked=!checked;
 	    	
 	    		pPanel.setAnimationEnabled(true);
 	    		pPanel.setGlassEnabled(true);
 		    	//setWidgetLeftWidth(absolutePanel, 345.0, Unit.PX, 408.0, Unit.PX);
 			    //setWidgetTopHeight(absolutePanel, 607.0, Unit.PX, 126.0, Unit.PX);
 			    absolutePanel.setSize("408px", "126px");
 			    loginLabel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			    absolutePanel.add(loginLabel, 0, 0);
 			    loginLabel.setStyleName("dialogVPanel");
 			    loginLabel.setSize("398px", "53px");
 			    signInLink.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
 			    
 			    signInLink.setHref(loginInfo.getLoginUrl());
 			    absolutePanel.add(signInLink, 0, 47);
 			    signInLink.setSize("408px", "33px");
 			    tos.addClickHandler(new ClickHandler() {
 			    	public void onClick(ClickEvent event) {
 /*			    		RootLayoutPanel.get().clear();
 			    		RootPanel.get().clear();*/
			    		RootPanel.get().clear();
 			    		System.out.println("step1");
			    		RootPanel.get().add(new TOSWidget(loginInfo));
 			    		System.out.println("step2");
 			    	}
 			    });
 				if(checked)
 	    			pPanel.center();
 	    		else{pPanel.hide();}
 		}
 	    	});
 	    	
 }
 }
